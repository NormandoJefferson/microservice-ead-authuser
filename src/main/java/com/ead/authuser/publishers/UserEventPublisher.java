package com.ead.authuser.publishers;

import com.ead.authuser.dtos.UserEventDto;
import com.ead.authuser.enums.ActionType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Componente responsável por publicar eventos de usuário no broker RabbitMQ.
 * <p>
 * Utiliza um exchange do tipo {@code fanout} para distribuir mensagens de eventos de usuário
 * para todas as filas vinculadas, independentemente de routing key.
 * <p>
 * O envio é realizado por meio do {@link RabbitTemplate}, com os dados encapsulados em um {@link UserEventDto}.
 */
@Component
public class UserEventPublisher {

    /**
     * Template do Spring AMQP utilizado para envio de mensagens ao RabbitMQ.
     * É automaticamente injetado pelo Spring.
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Nome do exchange do tipo fanout utilizado para eventos relacionados a usuários.
     * O valor é configurado através da propriedade {@code ead.broker.exchange.userevent}.
     */
    @Value("${ead.broker.exchange.userevent}")
    private String exchangeUserEvent;

    /**
     * Publica um evento de usuário no exchange configurado.
     * <p>
     * O evento contém os dados do usuário e o tipo da ação realizada (como criação, atualização ou remoção).
     * O envio é feito ao exchange do tipo fanout, portanto será entregue a todas as filas interessadas.
     *
     * @param userEventDto objeto contendo os dados do usuário a serem enviados
     * @param actionType tipo da ação que está sendo representada no evento
     */
    public void publishUserEvent(UserEventDto userEventDto, ActionType actionType) {
        userEventDto.setActionType(actionType.toString());
        rabbitTemplate.convertAndSend(exchangeUserEvent, "", userEventDto);
    }
}