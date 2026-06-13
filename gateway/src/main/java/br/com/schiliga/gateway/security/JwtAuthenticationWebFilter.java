package br.com.schiliga.gateway.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthenticationWebFilter implements WebFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_ISSUER = "restaurant-api";

    private final JWTVerifier jwtVerifier;

    public JwtAuthenticationWebFilter(@Value("${api.security.token.secret}") String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        this.jwtVerifier = JWT.require(algorithm)
                .withIssuer(TOKEN_ISSUER)
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (isPublicEndpoint(exchange)) {
            return chain.filter(exchange);
        }

        String token = recoverToken(exchange);

        if (token == null) {
            return unauthorized(exchange);
        }

        try {
            jwtVerifier.verify(token);
            return chain.filter(exchange);
        } catch (JWTVerificationException exception) {
            return unauthorized(exchange);
        }
    }

    private boolean isPublicEndpoint(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        return HttpMethod.OPTIONS.equals(method)
                || path.startsWith("/api/v1/auth/")
                || (HttpMethod.POST.equals(method) && "/api/v1/users".equals(path))
                || path.startsWith("/actuator/health");
    }

    private String recoverToken(ServerWebExchange exchange) {
        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorization.substring(BEARER_PREFIX.length()).trim();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
