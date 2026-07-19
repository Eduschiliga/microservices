package br.com.fiap.order.infrastructure.outbound.persistence.outbox;

import br.com.fiap.order.application.ports.inbound.order.CreateOrderInput;
import br.com.fiap.order.application.ports.inbound.order.ForCreatingOrder;
import br.com.fiap.order.application.ports.inbound.order.OrderOutput;
import br.com.fiap.order.infrastructure.outbound.kafka.PaymentRequestedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.kafka.topics.payment-requests=payment-requests-test",
        "app.kafka.topics.payment-results=payment-results-test",
        "app.outbox.initial-delay-ms=600000",
        "spring.kafka.listener.auto-startup=false"
})
class OrderOutboxIntegrationTest {
    private static final String PAYMENT_REQUESTS_TOPIC = "payment-requests-test";

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("orderdb")
            .withUsername("root")
            .withPassword("root");

    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    static {
        kafka.start();
        postgres.start();
    }

    @Autowired
    private ForCreatingOrder forCreatingOrder;

    @Autowired
    private OrderOutboxJpaRepository outboxJpaRepository;

    @Autowired
    private OrderOutboxPublisher outboxPublisher;

    @Autowired
    private ProducerFactory<String, Object> producerFactory;

    @Autowired
    private DataSource dataSource;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @AfterAll
    static void stopContainers() {
        postgres.stop();
        kafka.stop();
    }

    @Test
    void shouldPersistOutboxAndPublishPaymentRequestToKafka() {
        OrderOutput order = forCreatingOrder.create(new CreateOrderInput(
                UUID.randomUUID(),
                List.of(new CreateOrderInput.CreateOrderItemInput("p1", "Pizza", 2, BigDecimal.TEN))
        ));

        assertThat(outboxJpaRepository.countByStatus(OutboxStatus.PENDING)).isEqualTo(1);

        try (Consumer<String, PaymentRequestedEvent> consumer = createConsumer()) {
            consumer.subscribe(List.of(PAYMENT_REQUESTS_TOPIC));

            outboxPublisher.publishPending();

            ConsumerRecord<String, PaymentRequestedEvent> record = KafkaTestUtils.getSingleRecord(
                    consumer,
                    PAYMENT_REQUESTS_TOPIC,
                    Duration.ofSeconds(20)
            );

            assertThat(record.key()).isEqualTo(order.orderId().toString());
            assertThat(record.value().orderId()).isEqualTo(order.orderId());
            assertThat(record.value().amount()).isEqualByComparingTo("20");
        }

        assertThat(outboxJpaRepository.countByStatus(OutboxStatus.PENDING)).isZero();
        assertThat(outboxJpaRepository.countByStatus(OutboxStatus.PUBLISHED)).isEqualTo(1);
    }

    private Consumer<String, PaymentRequestedEvent> createConsumer() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "order-outbox-integration-test",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );
        JacksonJsonDeserializer<PaymentRequestedEvent> deserializer = new JacksonJsonDeserializer<>(PaymentRequestedEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        ).createConsumer();
    }

    @AfterEach
    void closeResourcesBeforeContainersStop() {
        if (producerFactory instanceof DefaultKafkaProducerFactory<String, Object> defaultProducerFactory) {
            defaultProducerFactory.destroy();
        }
        if (dataSource instanceof HikariDataSource hikariDataSource && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
        }
    }
}
