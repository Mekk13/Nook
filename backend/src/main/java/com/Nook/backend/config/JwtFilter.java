package com.Nook.backend.config;

import com.Nook.backend.auth.JwtUtil;
import com.Nook.backend.domain.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter — Spring guarantees this runs exactly once per request
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final IUserRepository userRepository;


    public JwtFilter(JwtUtil jwtUtil, IUserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Read the Authorization header
        // It should look like: "Bearer eyJhbGci..."
        String authHeader = request.getHeader("Authorization");

        // 2. If there's no token (e.g. hitting /api/auth/login), just continue
        // Spring Security will handle whether the endpoint needs auth or not
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the token part (everything after "Bearer ")
        String token = authHeader.substring(7);
        System.out.println("Token received: " + token.substring(0, 20) + "...");
        System.out.println("Token valid: " + jwtUtil.isTokenValid(token));


        // 4. Validate the token
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Extract the userId from the token
        String userId = jwtUtil.extractUserId(token);
        System.out.println("Extracted userId: " + userId);
        boolean userExists = userRepository.findById(userId).isPresent();
        System.out.println("User exists: " + userExists);

        userRepository.findById(userId).ifPresent(nookUser -> {
            UserDetails userDetails = User.builder()
                    .username(nookUser.getId())
                    .password("")
                    .authorities("ROLE_" + nookUser.getRole().name())
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        });

        // 8. Continue to the next filter / controller
        filterChain.doFilter(request, response);
    }
}