package com.mppay.gateway.mq.receiver;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Message;

import com.mppay.core.utils.MapUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseReceiver {

	/**
     * 把message转成需要的map
     * @param message
     * @return
     */
	protected Map messageToMap(Message message){

        try {
            return MapUtil.of("exchange", message.getMessageProperties().getReceivedExchange(), "routingKey", message.getMessageProperties().getReceivedRoutingKey(),
                    "queue", message.getMessageProperties().getConsumerQueue(), "msg", new String(message.getBody(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("【messageToMap】:{}", message);
        }
        return new HashMap();
    }
}
