package com.mppay.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Data
public class QueueConfig {

    private Integer enable;
    private String exchange;
    private Queues queues;

    @Data
    public static class Queues{
        private QConfig user;
        private QConfig order;
        private QConfig level;
        private QConfig levelQuick;
    }

    @Data
    public static class QConfig{
        private String queue;
        private String routingKey;
    }
}
