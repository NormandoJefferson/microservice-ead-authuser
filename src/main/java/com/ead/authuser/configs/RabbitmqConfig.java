package com.ead.authuser.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para a aplicação.
 * Define beans relacionados à conexão, troca de mensagens e conversores.
 */
@Configuration
public class RabbitmqConfig {

    /**
     * Fábrica de conexões com cache para o RabbitMQ.
     * Injetada automaticamente pelo Spring.
     */
    @Autowired
    CachingConnectionFactory cachingConnectionFactory;

    /**
     * Nome do exchange do tipo fanout utilizado para eventos de usuário.
     * Valor configurado no arquivo de propriedades da aplicação.
     */
    @Value(value = "${ead.broker.exchange.userevent}")
    private String exchangeUserEvent;

    /**
     * Cria e configura o {@link RabbitTemplate}, utilizado para envio de mensagens ao RabbitMQ.
     * Define o conversor de mensagens como JSON.
     *
     * @return instância de {@link RabbitTemplate}
     */
    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(cachingConnectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    /**
     * Cria um conversor de mensagens baseado em JSON usando Jackson.
     * Também registra o módulo {@link JavaTimeModule} para suportar tipos de data/hora do Java 8+.
     *
     * @return instância de {@link MessageConverter} configurada com Jackson
     */
    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * Cria um exchange do tipo {@link FanoutExchange} com o nome definido na propriedade
     * {@code ead.broker.exchange.userevent}.
     * Exchanges do tipo fanout enviam mensagens para todas as filas vinculadas.
     *
     * @return instância de {@link FanoutExchange}
     */
    @Bean
    public FanoutExchange fanoultUserEvent() {
        return new FanoutExchange(exchangeUserEvent);
    }

}