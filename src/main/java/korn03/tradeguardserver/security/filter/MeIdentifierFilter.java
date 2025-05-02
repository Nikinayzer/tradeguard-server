package korn03.tradeguardserver.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import korn03.tradeguardserver.exception.UnauthorizedException;
import korn03.tradeguardserver.model.entity.user.Role;
import korn03.tradeguardserver.security.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter that:
 * 1. Replaces "/me" with the actual user ID in request paths
 * 2. Enforces user access control (users can only access their own data unless they're an admin)
 * 
 * This runs AFTER JwtAuthFilter has authenticated the user but BEFORE Spring MVC maps the request
 * to controllers. This ensures authentication is already in the SecurityContext when we check permissions.
 */
@Component
@Slf4j
public class MeIdentifierFilter extends OncePerRequestFilter {

    private static final Pattern ME_PATTERN = Pattern.compile("/(api/)?users/me(/|$)");
    private static final Pattern USER_ID_PATTERN = Pattern.compile("/(api/)?((?!internal/)users/(\\d+)(/|$))");
    private static final Pattern INTERNAL_PATH_PATTERN = Pattern.compile("/(api/)?internal/.*");
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();

        if (INTERNAL_PATH_PATTERN.matcher(path).matches()) {
            filterChain.doFilter(request, response);
            return;
        }

        Matcher meMatcher = ME_PATTERN.matcher(path);
        if (meMatcher.find()) {
            try {
                Long userId = AuthUtil.getCurrentUserId();
                String newPath = path.replaceFirst("/me(/|$)", "/" + userId + "$1");
                
                log.debug("Rewriting path from {} to {}", path, newPath);

                MePathRewriteRequestWrapper wrappedRequest = 
                    new MePathRewriteRequestWrapper(request, newPath);
                filterChain.doFilter(wrappedRequest, response);
                return;
            } catch (UnauthorizedException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                return;
            } catch (Exception e) {
                log.error("Error in MeIdentifierFilter", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
                return;
            }
        }
        
        // Case 2: Handle numeric user ID paths to enforce access control
        Matcher userIdMatcher = USER_ID_PATTERN.matcher(path);
        if (userIdMatcher.find()) {
            try {
                String userIdStr = userIdMatcher.group(3); // Updated group index due to regex change
                Long requestedUserId = Long.parseLong(userIdStr);
                Long authenticatedUserId = AuthUtil.getCurrentUserId();
                if (!authenticatedUserId.equals(requestedUserId) && !isAdmin()) {
                    log.warn("User {} attempted to access data for user {}", authenticatedUserId, requestedUserId);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this user's data");
                    return;
                }
            } catch (UnauthorizedException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                return;
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
                return;
            } catch (Exception e) {
                log.error("Error checking user permissions", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
    
    /**
     * Check if the current user has admin role
     */
    private boolean isAdmin() {
        try {
            return AuthUtil.getCurrentUser().getRoles().stream()
                    .anyMatch(role -> role == Role.ADMIN);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Custom request wrapper that overrides getRequestURI() to return our modified path
     */
    private static class MePathRewriteRequestWrapper extends HttpServletRequestWrapper {
        private final String newPath;
        
        public MePathRewriteRequestWrapper(HttpServletRequest request, String newPath) {
            super(request);
            this.newPath = newPath;
        }
        
        @Override
        public String getRequestURI() {
            return newPath;
        }
        
        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer();
            String scheme = getScheme();
            int port = getServerPort();
            
            url.append(scheme);
            url.append("://");
            url.append(getServerName());
            
            if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
                url.append(':');
                url.append(port);
            }
            
            url.append(newPath);
            return url;
        }
        
        @Override
        public String getServletPath() {
            return newPath;
        }
    }
} 