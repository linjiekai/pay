package com.mppay.gateway.mq.sender;

public interface ISenderService {

    /**
     * 发送消息
     * @param exchange 交换机
     * @param routingKey 路由key
     * @param message 消息
     */
    void send(String exchange, String routingKey, String message);

    /**
     * 发送延时消息(要配置相关的延时队列)
     * @param exchange 交换机
     * @param routingKey 路由key
     * @param message 消息
     * @param delay 延时时间，单位：毫秒
     */
    void send(String exchange, String routingKey, String message, Long delay);
}
