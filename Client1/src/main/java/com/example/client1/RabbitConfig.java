package com.example.client1;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
public class RabbitConfig {
    @Value("${spring.application.name}")
    private String clientId;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange aggregatedWeightsExchange() {
        return new DirectExchange("aggregated_weights_exchange");
    }

    @Bean
    public Queue receivedWeightsDiabetesQueue() {
        return new Queue("received_weights_diabetes_queue", true);
    }

    @Bean
    public Queue receivedWeightsAnemiaQueue() {
        return new Queue("received_weights_anemia_queue", true);
    }

    @Bean
    public Queue receivedWeightsHeartAttackQueue() {
        return new Queue("received_weights_heart_attack_queue", true);
    }


    @Bean
    public Queue aggregatedWeightsDiabetesQueue() {
        return new Queue("aggregated_weights_diabetes_queue", true);
    }

    @Bean
    public Queue aggregatedWeightsAnemiaQueue() {
        return new Queue("aggregated_weights_anemia_queue", true);
    }

    @Bean
    public Queue aggregatedWeightsHeartAttackQueue() {
        return new Queue("aggregated_weights_heart_attack_queue", true);
    }


    @Bean
    public Binding diabetesBinding(DirectExchange aggregatedWeightsExchange) {
        return BindingBuilder
                .bind(aggregatedWeightsDiabetesQueue())
                .to(aggregatedWeightsExchange)
                .with("diabetes");
    }

    @Bean
    public Binding anemiaBinding(DirectExchange aggregatedWeightsExchange) {
        return BindingBuilder
                .bind(aggregatedWeightsAnemiaQueue())
                .to(aggregatedWeightsExchange)
                .with("anemia");
    }

    @Bean
    public Binding heartAttackBinding(DirectExchange aggregatedWeightsExchange) {
        return BindingBuilder
                .bind(aggregatedWeightsHeartAttackQueue())
                .to(aggregatedWeightsExchange)
                .with("heart_attack");
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

}
