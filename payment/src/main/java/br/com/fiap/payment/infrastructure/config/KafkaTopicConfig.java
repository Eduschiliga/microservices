package br.com.fiap.payment.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentRequestsTopic(@Value("${app.kafka.topics.payment-requests}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentResultsTopic(@Value("${app.kafka.topics.payment-results}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
