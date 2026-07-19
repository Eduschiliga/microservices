package br.com.schiliga.gateway.observability;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdWebFilterTest {
    private final CorrelationIdWebFilter filter = new CorrelationIdWebFilter();

    @Test
    void shouldCreateCorrelationIdWhenHeaderIsMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
        );
        RecordingWebFilterChain chain = new RecordingWebFilterChain();

        filter.filter(exchange, chain).block();

        String responseCorrelationId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdWebFilter.CORRELATION_ID_HEADER);
        assertThat(responseCorrelationId).isNotBlank();
        assertThat(chain.correlationId()).isEqualTo(responseCorrelationId);
    }

    @Test
    void shouldReuseCorrelationIdWhenHeaderAlreadyExists() {
        String correlationId = "request-123";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header(CorrelationIdWebFilter.CORRELATION_ID_HEADER, correlationId)
        );
        RecordingWebFilterChain chain = new RecordingWebFilterChain();

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdWebFilter.CORRELATION_ID_HEADER))
                .isEqualTo(correlationId);
        assertThat(chain.correlationId()).isEqualTo(correlationId);
    }

    private static class RecordingWebFilterChain implements WebFilterChain {
        private String correlationId;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.correlationId = exchange.getRequest()
                    .getHeaders()
                    .getFirst(CorrelationIdWebFilter.CORRELATION_ID_HEADER);
            return Mono.empty();
        }

        private String correlationId() {
            return correlationId;
        }
    }
}
