package com.payment.gateway.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for async messaging:
 * <ul>
 *   <li>payment.notify — payment result notifications</li>
 *   <li>payment.alert — discrepancy / error alerts (simulating DingTalk webhook)</li>
 * </ul>
 */
@Configuration
public class RabbitMQConfig {

    public static final String TOPIC_EXCHANGE = "payment.exchange";
    public static final String NOTIFY_QUEUE = "payment.notify.queue";
    public static final String ALERT_QUEUE = "payment.alert.queue";

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Queue notifyQueue() {
        return new Queue(NOTIFY_QUEUE, true);
    }

    @Bean
    public Queue alertQueue() {
        return new Queue(ALERT_QUEUE, true);
    }

    @Bean
    public Binding notifyBinding() {
        return BindingBuilder.bind(notifyQueue()).to(paymentExchange()).with("payment.success");
    }

    @Bean
    public Binding alertBinding() {
        return BindingBuilder.bind(alertQueue()).to(paymentExchange()).with("reconciliation.error");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
