package korn03.tradeguardserver.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import korn03.tradeguardserver.exception.UnauthorizedException;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.security.CustomUserDetailsService;
import korn03.tradeguardserver.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final List<String> publicPaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService, List<String> publicPaths1) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.publicPaths = publicPaths1;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        System.out.println("Request path: " + path);
        System.out.println("Match paths: " + publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path)));
        return publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = getJwtFromRequest(request);

            if (token != null && jwtService.validateToken(token)) {
                String username = jwtService.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username); // or extract ID instead
                User user = (User) userDetails;

                Instant tokenPwdUpdatedAt = jwtService.extractPasswordUpdatedAt(token);
                Instant dbPwdChangedAt = user.getPasswordUpdatedAt();
                //todo uncomment
//                if (tokenPwdUpdatedAt.isBefore(dbPwdChangedAt)) {
//                    log.warn("Token is outdated due to password change.");
//                    log.warn("Token password updated at: {}, DB password updated at: {}",
//                            tokenPwdUpdatedAt, dbPwdChangedAt);
//                    throw new UnauthorizedException("token_invalid");
//                }

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (UsernameNotFoundException e) {
            log.debug("User not found for JWT: {}", e.getMessage());
            throw new UnauthorizedException("token_invalid");
        } catch (Exception e) {
            log.debug("JWT authentication failed: {}", e.getMessage());
            throw new UnauthorizedException("token_invalid");
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
