package site.esvitlo.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import site.esvitlo.backend.service.DeviceService;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, DeviceService deviceService) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/ping",
                                "/h2-console/**"
                        ).permitAll()
                        .anyRequest().denyAll()
                )

                .headers(headers ->
                        headers.frameOptions(frame -> frame.disable())
                )

                .addFilterBefore(
                        new DeviceAuthFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
