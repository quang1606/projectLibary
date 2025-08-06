package com.example.projectlibary.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic userRegistrationTopic() {
        return TopicBuilder.name("user-registration-events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic bookEventsTopic(){
        return TopicBuilder.name("book-events").partitions(1).replicas(1).build();
    }
    @Bean
    public NewTopic bookEventTopic(){
        return TopicBuilder.name("loan-confirmation-events").partitions(1).replicas(1).build();
    }
     @Bean
    public NewTopic borrowingCartTopic(){
        return TopicBuilder.name("book-status-changed-events").partitions(1).replicas(1).build();
     }
     @Bean
     public NewTopic fineIssuedEventTopic(){
        return TopicBuilder.name("fine-issued-events").partitions(1).replicas(1).build();
     }
     @Bean
    public NewTopic borrowingOrderTopic(){
        return TopicBuilder.name("payments-completed-events").partitions(1).replicas(1).build();
     }

     @Bean
    public NewTopic paymentsTopic(){
        return TopicBuilder.name("forgot-password-events").partitions(1).replicas(1).build();
     }

}
