package com.mppay.gateway.mq.sender.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mppay.gateway.mq.sender.ISenderService;

@Component
public class SenderService implements ISenderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void send(String exchange, String routingKey, String message){
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    @Override
    public void send(String exchange, String routingKey, String message, Long delay) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            msg.getMessageProperties().setExpiration(String.valueOf(delay));// 单位毫秒
            return msg;
        });
    }
}
