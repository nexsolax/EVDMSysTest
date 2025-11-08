package com.evdealer.config;

import com.evdealer.enums.Role;
import com.evdealer.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        logger.info("JWT Filter: Processing request to {}", request.getRequestURI());
        final String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;
        boolean invalidToken = false;
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            logger.info("JWT Filter: Found Bearer token");
            final String raw = authorizationHeader.substring(7);
            final String token = raw == null ? null : raw.trim();
            if (token != null && !token.isEmpty()) {
                jwt = token;
                logger.info("JWT Filter: Extracting username from token...");
                try {
                    username = jwtUtil.extractUsername(jwt);
                    logger.info("JWT Filter: Username extracted: {}", username);
                } catch (ExpiredJwtException e) {
                    invalidToken = true;
                    logger.warn("JWT expired: " + e.getMessage());
                } catch (MalformedJwtException e) {
                    invalidToken = true;
                    logger.warn("JWT malformed: " + e.getMessage());
                } catch (SignatureException e) {
                    invalidToken = true;
                    logger.warn("JWT signature invalid: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    invalidToken = true;
                    logger.warn("JWT illegal argument: " + e.getMessage());
                } catch (JwtException e) {
                    invalidToken = true;
                    logger.warn("JWT parse error: " + e.getMessage());
                } catch (Exception e) {
                    invalidToken = true;
                    logger.warn("JWT token extraction failed: " + e.getMessage());
                }
            } else {
                // Bearer header nhưng không có token
                invalidToken = true;
                logger.warn("Authorization header has Bearer prefix but empty token");
            }
        } else {
            logger.info("JWT Filter: No Authorization header found");
        }
        
        if (username != null) {
            logger.info("JWT Filter: Validating token for user: {}", username);
            if (jwtUtil.validateToken(jwt)) {
                String roleStr = jwtUtil.getRoleFromToken(jwt);
                String userId = jwtUtil.getUserIdFromToken(jwt);
                
                // Normalize role: chuyển đổi role string thành Role enum và lấy authority
                Role role = Role.fromString(roleStr);
                String normalizedRole = role != null ? role.getValue() : Role.normalize(roleStr);
                String authority = role != null ? role.getAuthority() : "ROLE_" + normalizedRole;
                
                logger.info("JWT validated successfully for user: {}, role: {} (normalized: {}), userId: {}", 
                    username, roleStr, normalizedRole, userId);
                
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        username, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority(authority))
                    );
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                // Add user info to request attributes for easy access (normalized role)
                request.setAttribute("userId", userId);
                request.setAttribute("userRole", normalizedRole);
                
                logger.info("Authentication set in SecurityContext for user: {}, role: {}, authority: {}", 
                    username, normalizedRole, authority);
            } else {
                logger.warn("JWT token validation failed for user: {}", username);
                invalidToken = true;
            }
        } else if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            logger.warn("JWT Filter: Authorization header present but username extraction failed");
            invalidToken = true;
        }
        
        // Nếu client gửi Bearer nhưng token không hợp lệ → trả 401 thay vì âm thầm bỏ qua
        if (invalidToken) {
            logger.warn("JWT Filter: Invalid token, returning 401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Log authentication status before continuing
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.info("JWT Filter: Authentication present in SecurityContext: {}", auth.getName());
        } else {
            logger.warn("JWT Filter: No authentication in SecurityContext after processing");
        }
        
        logger.info("JWT Filter: Continuing filter chain");
        filterChain.doFilter(request, response);
    }
}
