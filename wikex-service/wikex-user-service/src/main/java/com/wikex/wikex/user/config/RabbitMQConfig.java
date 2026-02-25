package com.wikex.wikex.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RabbitMQConfig {

  @Value("${spring.rabbitmq.deposit.exchange}")
  private String depositExchangeName;

  @Value("${spring.rabbitmq.deposit.queue}")
  private String depositWsQueueName;

  @Value("${spring.rabbitmq.deposit.routing-key}")
  private String depositRoutingKey;

  @Bean
  public TopicExchange depositExchange() {
    return new TopicExchange(depositExchangeName, true, false);
  }

  @Bean
  public Queue depositWsQueue() {
    return QueueBuilder.durable(depositWsQueueName).build();
  }

  @Bean
  public Binding depositWsBinding(
      Queue depositWsQueue,
      TopicExchange depositExchange) {
    return BindingBuilder
        .bind(depositWsQueue)
        .to(depositExchange)
        .with(depositRoutingKey);
  }
}
