package com.Nook.backend.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

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
                // Disable CSRF — not needed for stateless JWT APIs
                // CSRF is only relevant for browser session-based auth
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless — Spring should never create a session
                // Every request must carry its own JWT
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Define which endpoints need a token and which don't
                .authorizeHttpRequests(auth -> auth
                        // These are public — no token needed
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public room browsing — anyone can see the list
                        .requestMatchers(HttpMethod.GET, "/api/rooms").permitAll()

                        .requestMatchers("/api/faker/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rooms").permitAll()
                        .requestMatchers("/api/faker/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/graphql").permitAll()
                        .requestMatchers("/graphiql/**").permitAll()

                        // Everything else requires a valid JWT token
                        .anyRequest().authenticated()
                )

                // Register our JWT filter BEFORE Spring's default auth filter
                // This ensures our filter runs first and sets up the SecurityContext
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCryptPasswordEncoder — the standard for hashing passwords in Spring
    // BCrypt automatically adds a "salt" (random data) to each hash
    // so two users with the same password get different hashes
    // @Bean makes this available for injection anywhere in the app
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}