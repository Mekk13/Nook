package com.Nook.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestClient;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/auth/magic-link", "/api/auth/magic-link/verify", "/api/auth/magic-link/request").permitAll()
                        .requestMatchers("/api/auth/oauth/**").permitAll()

                        // MFA login steps 2 & 3 are public (no JWT yet — mid-login)
                        .requestMatchers("/api/auth/mfa/verify-email-otp").permitAll()
                        .requestMatchers("/api/auth/mfa/verify-totp").permitAll()

                        // MFA setup endpoints require authentication (user must be logged in)
                        .requestMatchers("/api/auth/mfa/setup").authenticated()
                        .requestMatchers("/api/auth/mfa/setup/confirm").authenticated()

                        // Everything else
                        .requestMatchers(HttpMethod.GET, "/api/rooms").permitAll()
                        // /ws/** stays public at the HTTP handshake layer;
                        // STOMP CONNECT carries the JWT for actual auth.
                        .requestMatchers("/ws/**").permitAll()
                        // /graphql + /graphiql: kept open so the playground works locally.
                        // graphiql is disabled in prod via spring.graphql.graphiql.enabled.
                        .requestMatchers("/graphql").permitAll()
                        .requestMatchers("/graphiql/**").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        // Authenticated-only:
                        .requestMatchers("/api/faker/**").authenticated()
                        .requestMatchers("/api/chat/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}