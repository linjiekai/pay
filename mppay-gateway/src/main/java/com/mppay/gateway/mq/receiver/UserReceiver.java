package com.mppay.gateway.mq.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mppay.core.utils.MQLogUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class UserReceiver extends BaseReceiver {

    @Autowired
    protected BaseBusiHandler userChangeHandler;

    @RabbitListener(autoStartup = "${mq.listener.switch}", bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.user.queue}", durable = "true"), exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC), key = "${spring.rabbitmq.queues.user.routing-key}"), concurrency = "3")
    public void process(Channel channel, Message message) throws IOException {

        String msg = new String(message.getBody(), "UTF-8");
        log.info("UserReceiver 收到消息：{}", msg);
        try {

            Map<String, Object> bodyMap = JSONObject.parseObject(msg, Map.class);

            RequestMsg requestMsg = new RequestMsg(bodyMap);

            ResponseMsg responseMsg = null;

            userChangeHandler.doBusi(requestMsg, responseMsg);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("用户注册消息处理失败:" + msg, e);
            MQLogUtil.info(JSON.toJSONString(messageToMap(message)));// 日志
            //ack返回false，并重新回到队列
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

}


