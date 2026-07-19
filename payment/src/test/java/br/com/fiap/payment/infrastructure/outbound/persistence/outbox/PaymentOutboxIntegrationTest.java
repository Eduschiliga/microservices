package br.com.fiap.payment.infrastructure.outbound.persistence.outbox;

import br.com.fiap.payment.application.ports.inbound.payment.ForProcessingPayment;
import br.com.fiap.payment.application.ports.inbound.payment.PaymentOutput;
import br.com.fiap.payment.application.ports.inbound.payment.ProcessPaymentInput;
import br.com.fiap.payment.application.domain.payment.PaymentStatus;
import br.com.fiap.payment.infrastructure.outbound.kafka.PaymentResultEvent;
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
class PaymentOutboxIntegrationTest {
    private static final String PAYMENT_RESULTS_TOPIC = "payment-results-test";

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("paymentdb")
            .withUsername("root")
            .withPassword("root");

    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    static {
        kafka.start();
        postgres.start();
    }

    @Autowired
    private ForProcessingPayment forProcessingPayment;

    @Autowired
    private PaymentOutboxJpaRepository outboxJpaRepository;

    @Autowired
    private PaymentOutboxPublisher outboxPublisher;

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
    void shouldPersistOutboxAndPublishPaymentResultToKafka() {
        UUID orderId = UUID.randomUUID();
        PaymentOutput payment = forProcessingPayment.process(new ProcessPaymentInput(
                orderId,
                UUID.randomUUID(),
                BigDecimal.TEN
        ));

        assertThat(payment.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(outboxJpaRepository.countByStatus(OutboxStatus.PENDING)).isEqualTo(1);

        try (Consumer<String, PaymentResultEvent> consumer = createConsumer()) {
            consumer.subscribe(List.of(PAYMENT_RESULTS_TOPIC));

            outboxPublisher.publishPending();

            ConsumerRecord<String, PaymentResultEvent> record = KafkaTestUtils.getSingleRecord(
                    consumer,
                    PAYMENT_RESULTS_TOPIC,
                    Duration.ofSeconds(20)
            );

            assertThat(record.key()).isEqualTo(orderId.toString());
            assertThat(record.value().orderId()).isEqualTo(orderId);
            assertThat(record.value().approved()).isTrue();
        }

        assertThat(outboxJpaRepository.countByStatus(OutboxStatus.PENDING)).isZero();
        assertThat(outboxJpaRepository.countByStatus(OutboxStatus.PUBLISHED)).isEqualTo(1);
    }

    private Consumer<String, PaymentResultEvent> createConsumer() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "payment-outbox-integration-test",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );
        JacksonJsonDeserializer<PaymentResultEvent> deserializer = new JacksonJsonDeserializer<>(PaymentResultEvent.class);
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
