package com.mppay.gateway.mq.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.mppay.core.constant.WithdrOrderStatus;
import com.mppay.core.utils.MQLogUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.gateway.mq.sender.ISenderService;
import com.mppay.service.entity.WithdrOrder;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.IWithdrOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @title: WithdrOrderReceiver
 * @description: 提现mq
 * @date 2020/4/16 14:08
 */
@Component
@Slf4j
public class WithdrReceiver extends BaseReceiver{

    private static final String BANK_CODE_WEIXIN = "WEIXIN";
    private static final String BANK_CODE_ALIPAY = "ALIPAY";
    private static final String DICTIONARY_CATEGORY_WITHDR = "withdr";
    private static final String DICTIONARY_NAME_OPEN = "open";
    private static final String DICTIONARY_NAME_BANKOPEN = "bank_open";
    private static final String DICTIONARY_NAME_WEIXINOPEN = "weixin_open";
    private static final int DICTIONARY_FALG_CLOSE = 0;

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.queues.query-withdr-order.routing-key}")
    private String queryWithdrOrderroutingKye;

    @Resource
    private IWithdrOrderService iWithdrOrderService;
    @Resource
    private IDictionaryService iDictionaryService;
    @Resource
    private UnifiedHandler withdrOrderHandler;
    @Resource
    private UnifiedHandler withdrOrderQueryHandler;
    @Resource
    private ISenderService iSenderService;

    /**
     * 提现订单处理
     *
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitListener(autoStartup = "${mq.listener.switch}", bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.withdr-order.queue}", durable = "true"),
            exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC), key = "${spring.rabbitmq.queues.withdr-order.routing-key}"))
    public void withdrOrder(Channel channel, Message message) throws IOException {
        String msg = new String(message.getBody(),"UTF-8");
        log.info("|WithdrOrderReceiver|收到消息：{}", msg);
        try {
            WithdrOrder withdrOrder = JSONObject.parseObject(msg, WithdrOrder.class);
            Long id = withdrOrder.getId();
            String orderNo = withdrOrder.getOrderNo();
            String withdrOrderNo = withdrOrder.getWithdrOrderNo();
            withdrOrder = iWithdrOrderService.getById(id);
            if (withdrOrder == null) {
                log.info("|WithdrOrderReceiver|异常|提现订单不存在,id:{}, orderNo:{}, withdrOrderNo:{}", id, orderNo, withdrOrderNo);
                return;
            }
            log.info("|WithdrOrderReceiver|提现订单:{}", withdrOrder);

            Long withdrOpen = iDictionaryService.findForLong(DICTIONARY_CATEGORY_WITHDR, DICTIONARY_NAME_OPEN);
            if (null == withdrOpen || withdrOpen.longValue() == 0) {
                log.info("|WithdrOrderReceiver|异常|提现开关关闭, id:{}, orderNo:{}, withdrOrderNo:{}", id, orderNo, withdrOrderNo);
                return;
            }
            orderNo = withdrOrder.getOrderNo();
            String bankCode = withdrOrder.getBankCode();
            String mercId = withdrOrder.getMercId();

            // 提现开关校验
            switch (bankCode) {
                case BANK_CODE_WEIXIN:
                    Long weixinOpen = iDictionaryService.findForLong(DICTIONARY_CATEGORY_WITHDR, DICTIONARY_NAME_WEIXINOPEN, mercId);
                    if (weixinOpen == null || DICTIONARY_FALG_CLOSE == weixinOpen) {
                        log.info("|WithdrOrderReceiver|异常|提现到微信关闭, id:{}, orderNo:{}, withdrOrderNo:{}", id, orderNo, withdrOrderNo);
                        return;
                    }
                    break;
                case BANK_CODE_ALIPAY:
                    log.info("|WithdrOrderReceiver|异常|提现到支付宝关闭,id:{}, orderNo:{}, withdrOrderNo:{}", id, orderNo, withdrOrderNo);
                    return;
                default:
                    Long bankOpen = iDictionaryService.findForLong(DICTIONARY_CATEGORY_WITHDR, DICTIONARY_NAME_BANKOPEN, mercId);
                    if (bankOpen == null || DICTIONARY_FALG_CLOSE == bankOpen) {
                        log.info("|WithdrOrderReceiver|异常|提现到银行关闭,id:{}, orderNo:{}, withdrOrderNo:{}", id, orderNo, withdrOrderNo);
                        return;
                    }
                    break;
            }
            // 提现处理
            Map<String, Object> data = new HashMap<>();
            data.putAll(BeanUtils.beanToMap(withdrOrder));
            RequestMsg requestMsg = new RequestMsg(data);
            log.info("|WithdrOrderReceiver|处理|提现处理开始,id:{}, orderNo:{}, withdrOrderNo:{}", id, orderNo, withdrOrderNo);
            withdrOrderHandler.execute(requestMsg);
            log.info("|WithdrOrderReceiver|完成|提现处理完成,id:{}, orderNo:{}, withdrOrderNo:{}", id, orderNo, withdrOrderNo);
        } catch (Exception e) {
            log.error("|WithdrOrderReceiver|异常|提现订单处理,接收到消息:{} ,错误信息:{}", msg, e.getMessage());
            e.printStackTrace();
            // MQ日志
            MQLogUtil.info(JSON.toJSONString(messageToMap(message)));
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * 提现订单查询
     *
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitListener(autoStartup = "${mq.listener.switch}", bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.query-withdr-order.queue}", durable = "true"),
            exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC), key = "${spring.rabbitmq.queues.query-withdr-order.routing-key}"))
    public void queryWithdrOrder(Channel channel, Message message) throws IOException {
        String msg = new String(message.getBody(),"UTF-8");
        log.info("|QueryWithdrOrderReceiver|收到消息：{}", msg);
        try {
            WithdrOrder withdrOrder = JSONObject.parseObject(msg, WithdrOrder.class);
            Long id = withdrOrder.getId();
            String orderNo = withdrOrder.getOrderNo();
            String withdrOrderNo = withdrOrder.getWithdrOrderNo();
            withdrOrder = iWithdrOrderService.getById(id);
            if (withdrOrder == null) {
                log.info("|QueryWithdrOrderReceiver|异常|提现订单不存在, id:{}, orderNo:{}, withdrOrderNo:{}", id, orderNo, withdrOrderNo);
                return;
            }
            log.info("|QueryWithdrOrderReceiver|提现订单:{}", withdrOrder);

            orderNo = withdrOrder.getOrderNo();
            String orderStatus = withdrOrder.getOrderStatus();
            if (!WithdrOrderStatus.BANK_WAIT.getId().equals(orderStatus)) {
                log.info("|QueryWithdrOrderReceiver|结束|提现订单状态无效,状态:{},不能进行提现订单查询, id:{}, orderNo:{}, withdrOrderNo:{}", orderStatus, id, orderNo, withdrOrderNo);
                return;
            }

            // 提现处理
            Map<String, Object> data = new HashMap<>();
            data.putAll(BeanUtils.beanToMap(withdrOrder));
            RequestMsg requestMsg = new RequestMsg(data);
            log.info("|QueryWithdrOrderReceiver|处理|查询提现处理开始, id:{}, orderNo:{}, withdrOrderNo:{}", id, orderNo, withdrOrderNo);
            withdrOrderQueryHandler.execute(requestMsg);
            log.info("|QueryWithdrOrderReceiver|完成|查询提现处理完成, id:{}, orderNo:{}, withdrOrderNo:{}", id, orderNo, withdrOrderNo);
        } catch (Exception e) {
            log.error("|QueryWithdrOrderReceiver|异常|查询提现订单处理,接收到消息:{} ,错误信息:{}", msg, e.getMessage());
            e.printStackTrace();
            // MQ日志
            MQLogUtil.info(JSON.toJSONString(messageToMap(message)));
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
