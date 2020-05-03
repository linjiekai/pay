package com.mppay.gateway.mq.receiver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.utils.HttpClientUtil;
import com.mppay.core.utils.MQLogUtil;
import com.mppay.service.entity.Merc;
import com.mppay.service.entity.MercOrder;
import com.mppay.service.service.IMercOrderService;
import com.mppay.service.service.IMercService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotifyReceiver extends BaseReceiver{

	@Autowired
	private IMercService mercService;
	
	@Autowired
	protected IMercOrderService mercOrderService;
	
    @RabbitListener(autoStartup = "${mq.listener.switch}",bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.notify.queue}", durable = "true"), exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC), key = "${spring.rabbitmq.queues.notify.routing-key}"))
    public void process( Channel channel, Message message) throws IOException {

		String msg = new String(message.getBody(),"UTF-8");
		log.info("NotifyReceiver 收到消息：{}",msg);
		try {
			Map<String, Object> bodyMap = JSONObject.parseObject(msg, Map.class);
	    	
			MercOrder mercOrder = mercOrderService.getOne(new QueryWrapper<MercOrder>()
					.eq("order_no", bodyMap.get("orderNo"))
					.eq("order_status", bodyMap.get("orderStatus"))
					.eq("user_id", bodyMap.get("userId"))
					);
			Merc merc = mercService.getOne(new QueryWrapper<Merc>().eq("merc_id", mercOrder.getMercId()));
			
			Map<String, Object> notifyMsg = new HashMap<String, Object>();
			notifyMsg.put("mercId", mercOrder.getMercId());
			notifyMsg.put("payNo", mercOrder.getOutTradeNo());
			notifyMsg.put("orderNo", mercOrder.getOrderNo());
			notifyMsg.put("orderStatus", OrderStatus.SUCCESS.getId());
			notifyMsg.put("userId", mercOrder.getUserId());
			notifyMsg.put("orderDate", mercOrder.getOrderDate());
			notifyMsg.put("orderTime", mercOrder.getOrderTime());
			notifyMsg.put("payDate", mercOrder.getPayDate());
			notifyMsg.put("payTime", mercOrder.getPayTime());
			notifyMsg.put("price", mercOrder.getPrice());
			notifyMsg.put("bankCode", mercOrder.getBankCode());
			notifyMsg.put("tradeType", mercOrder.getTradeType());
			notifyMsg.put("openId", mercOrder.getOpenId());
			
			String plain = Sign.getPlain(notifyMsg);
			plain += "&key=" + merc.getPrivateKey();
			String sign = Sign.sign(plain);

			Map<String, Object> headerMap = new HashMap<String, Object>();
			headerMap.put("X-MPMALL-SignVer", "v1");
			headerMap.put("X-MPMALL-Sign", sign);
			
			if (!StringUtils.isBlank(mercOrder.getNotifyUrl())) {
				log.info("支付结果通知订单系统请求， 通知地址={}，通知参数={}", mercOrder.getNotifyUrl(), notifyMsg);
				String result = HttpClientUtil.sendPostJson(mercOrder.getNotifyUrl(), notifyMsg, headerMap);
				
				if (StringUtils.isBlank(result)) {
					log.error("支付结果通知订单系统失败， 通知地址={}，通知参数={}", mercOrder.getNotifyUrl(), notifyMsg);
					throw new BusiException(15201);
				}
				
				log.info("支付结果通知订单系统响应， 通知地址={}，通知参数={}", mercOrder.getNotifyUrl(), result);
			}
			
        } catch (Exception e) {
        	log.error("支付结果后台通知消息处理失败:{} ,error:{}",msg, e);
        	MQLogUtil.info(JSON.toJSONString(messageToMap(message)));// 日志
        	//ack返回false，并重新回到队列
//          channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        } finally {
        	channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
		}
		
    }
    
}


