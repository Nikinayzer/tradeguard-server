package korn03.tradeguardserver.security.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filter to restrict access to internal endpoints. Internal endpoints are only accessible from whitelisted IPs.
 */
@Component
public class InternalFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_IPS = Set.of(
            "127.0.0.1",
            "0:0:0:0:0:0:0:1"
    );
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        if (pathMatcher.match("/internal/**", path)) {
            String ip = request.getRemoteAddr();

            if (!ALLOWED_IPS.contains(ip)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for IP: " + ip);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
