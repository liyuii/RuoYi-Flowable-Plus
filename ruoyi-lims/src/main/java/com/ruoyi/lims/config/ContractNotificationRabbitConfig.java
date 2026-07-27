package com.ruoyi.lims.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration
public class ContractNotificationRabbitConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 主队列
    @Bean
    public Queue contractNotificationQueue() {
        return QueueBuilder.durable("contract.notification.queue")
            .withArgument("x-dead-letter-exchange", "contract.notification.dlq.exchange")
            .withArgument("x-dead-letter-routing-key", "contract.notification.approve.dlq")
            .build();
    }

    // 主交换机
    @Bean
    public DirectExchange contractNotificationExchange() {
        return new DirectExchange("contract.notification.exchange");
    }

    // 绑定
    @Bean
    public Binding contractNotificationBinding() {
        return BindingBuilder.bind(contractNotificationQueue())
            .to(contractNotificationExchange())
            .with("contract.notification.approve");
    }

    // 死信队列
    @Bean
    public Queue contractNotificationDlq() {
        return QueueBuilder.durable("contract.notification.dlq.queue").build();
    }

    // 死信交换机
    @Bean
    public DirectExchange contractNotificationDlqExchange() {
        return new DirectExchange("contract.notification.dlq.exchange");
    }

    // 死信绑定
    @Bean
    public Binding contractNotificationDlqBinding() {
        return BindingBuilder.bind(contractNotificationDlq())
            .to(contractNotificationDlqExchange())
            .with("contract.notification.approve.dlq");
    }
}
