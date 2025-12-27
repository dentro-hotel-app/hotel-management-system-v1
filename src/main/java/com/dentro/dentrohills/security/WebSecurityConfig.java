package com.dentro.dentrohills.security;

import com.dentro.dentrohills.security.jwt.AuthTokenFilter;
import com.dentro.dentrohills.security.jwt.JwtAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final AuthTokenFilter authTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // 🔓 CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🔓 AUTH (FIXED)
                        .requestMatchers(
                                "/auth/**",
                                "/api/auth/**"
                        ).permitAll()

                        // 🔓 HOSPITALS
                        .requestMatchers("/hospitals/**").permitAll()

                        // 🔓 ROOMS
                        .requestMatchers("/rooms/**").permitAll()

                        // 🔓 BOOKINGS (GUEST)
                        .requestMatchers(
                                "/bookings/room/**",
                                "/bookings/confirmation/**",
                                "/bookings/room/*/booked-dates"
                        ).permitAll()

                        // 🔒 ADMIN
                        .requestMatchers(
                                "/hospitals/*/upload-photo",
                                "/rooms/add/**",
                                "/rooms/update/**",
                                "/rooms/delete/**"
                        ).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )


                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                        .accessDeniedHandler(jwtAuthEntryPoint)
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
