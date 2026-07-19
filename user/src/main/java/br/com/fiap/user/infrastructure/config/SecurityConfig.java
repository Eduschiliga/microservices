package br.com.fiap.user.infrastructure.config;

import br.com.fiap.user.infrastructure.inbound.security.filter.SecurityFilter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@SecurityScheme(
        name = SecurityConfig.SECURITY_SCHEME_NAME,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SecurityConfig {
    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    private final SecurityFilter securityFilter;
    private final HandlerExceptionResolver exceptionResolver;

    public SecurityConfig(
            SecurityFilter securityFilter,
            @Qualifier("handlerExceptionResolver")HandlerExceptionResolver exceptionResolver
    ) {
        this.securityFilter = securityFilter;
        this.exceptionResolver = exceptionResolver;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests((authorize) -> {
                    authorize.requestMatchers(
                            "/api/v1/auth/**",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/swagger-resources/**",
                            "/swagger-ui.html",
                            "/actuator/health/**",
                            "/actuator/info",
                            "/actuator/metrics/**",
                            "/actuator/prometheus"
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST, "/swagger-ui").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                    .anyRequest().authenticated();
                }
                )
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) ->
                        exceptionResolver.resolveException(request, response, null, authException)
                    )
                    .accessDeniedHandler((request, response, accessDeniedException) ->
                        exceptionResolver.resolveException(request, response, null, accessDeniedException)
                    )
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
