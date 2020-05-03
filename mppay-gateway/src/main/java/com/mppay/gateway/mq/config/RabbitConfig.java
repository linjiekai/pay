package com.mppay.gateway.mq.config;


import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mppay.gateway.config.QueueConfig;

@Configuration
public class RabbitConfig {

    @Autowired
    private QueueConfig queueConfig;

    @Bean
    public Queue userQueue() {
        return new Queue(queueConfig.getQueues().getUser().getQueue());
    }
}
