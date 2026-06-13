package br.com.schiliga.gateway.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationWebFilterTest {
    private static final String SECRET = "test-secret";

    private final JwtAuthenticationWebFilter filter = new JwtAuthenticationWebFilter(SECRET);

    @Test
    void shouldAllowPublicLoginEndpointWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/auth/login")
        );
        RecordingWebFilterChain chain = new RecordingWebFilterChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.wasCalled()).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldAllowPublicUserCreationWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.POST, "/api/v1/users")
        );
        RecordingWebFilterChain chain = new RecordingWebFilterChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.wasCalled()).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldRejectProtectedEndpointWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users")
        );
        RecordingWebFilterChain chain = new RecordingWebFilterChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.wasCalled()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectProtectedEndpointWithInvalidToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
        );
        RecordingWebFilterChain chain = new RecordingWebFilterChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.wasCalled()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAllowProtectedEndpointWithValidToken() {
        String token = JWT.create()
                .withIssuer("restaurant-api")
                .withSubject("admin")
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(Algorithm.HMAC256(SECRET));
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        );
        RecordingWebFilterChain chain = new RecordingWebFilterChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.wasCalled()).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    private static class RecordingWebFilterChain implements WebFilterChain {
        private boolean called;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.called = true;
            return Mono.empty();
        }

        private boolean wasCalled() {
            return called;
        }
    }
}
