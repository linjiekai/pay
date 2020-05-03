package com.mppay.gateway.handler.quick;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.mppay.core.constant.*;
import com.mppay.service.service.common.ICipherService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.gateway.handler.impl.BaseBusiHandlerImpl;
import com.mppay.service.entity.BankRoute;
import com.mppay.service.entity.Route;
import com.mppay.service.entity.SmsOrder;
import com.mppay.service.entity.TradeOrder;
import com.mppay.service.service.IBankRouteService;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.IRouteService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ISmsOrderService;
import com.mppay.service.service.ITradeOrderService;

import lombok.extern.slf4j.Slf4j;

@Service("quickUnifiedOrderHandler")
@Slf4j
public class QuickUnifiedOrderHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private ISmsOrderService smsOrderService;
    @Autowired
    private ISeqIncrService seqIncrService;
    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private IBankRouteService bankRouteService;
    @Autowired
    private IRouteService routeService;
    @Autowired
    private ICipherService iCipherService;

    @Override
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|快捷支付|交易订单处理|开始 参数：{}", JSON.toJSONString(requestMsg));
        String bankCode = (String) requestMsg.get("bankCode");
        String routeCode = (String) requestMsg.get("routeCode");
        String agrNo = requestMsg.get("agrNo").toString();
        BankRoute bankRoute = bankRouteService.getOne(new QueryWrapper<BankRoute>().eq("bank_code", bankCode)
                .eq("trade_code", TradeCode.CONSUMER.getId())
                .eq("route_code", routeCode)
                .eq("merc_id", requestMsg.get("mercId"))
                .last(" limit 1")
        );

        if (null == bankRoute) {
            log.error(ApplicationYmlUtil.get(30002) + JSON.toJSONString(requestMsg));
            throw new BusiException(30002);
        }

        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", bankRoute.getRouteCode()));

        // 如果银行渠道路由状态不正常，异常返回
        if (null == bankRoute || route.getStatus().intValue() != RouteStatus.NORMAL.getId()) {
            log.error(ApplicationYmlUtil.get(11118) + requestMsg.toString());
            throw new BusiException(11118);
        }

        String smsOrderNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.SMS_ORDER_NO.getId(), 10, Align.LEFT);
        requestMsg.put("smsOrderNo", smsOrderNo);
        SmsOrder smsOrder = new SmsOrder();
        BeanUtils.populate(smsOrder, requestMsg.getMap());
        smsOrder.setBindDate(DateTimeUtil.date10());
        smsOrder.setBindTime(DateTimeUtil.time8());
        //保存短信订单表
        smsOrderService.save(smsOrder);
        log.info("|快捷支付|交易订单处理|保存短信订单 smsOrderNo：{}", smsOrderNo);

        TradeOrder tradeOrder = new TradeOrder();
        String tradeNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.TRADE_NO.getId(), SeqIncrType.TRADE_NO.getLength(), Align.LEFT);
        requestMsg.put("tradeNo", tradeNo);
        BeanUtils.populate(tradeOrder, requestMsg.getMap());
        tradeOrder.setTradeDate(DateTimeUtil.date10());
        tradeOrder.setTradeTime(DateTimeUtil.time8());
        tradeOrder.setOrderStatus(OrderStatus.ADVANCE.getId());
        tradeOrder.setTradeType(TradeType.QUICK.getId());
        tradeOrderService.save(tradeOrder);
        log.info("|快捷支付|交易订单处理|保存交易订单 tradeNo：{}", tradeNo);
        //拼出service name
        String serviceName = tradeOrder.getRouteCode().toLowerCase() + ConstEC.QUICKBUSIHANDLER;
        //通过spring ApplicationContext获取service对象
        QuickBusiHandler quickBusiHandler = (QuickBusiHandler) SpringContextHolder.getBean(serviceName);
        if (null == quickBusiHandler) {
            log.error("|快捷支付|交易订单处理|serviceName[{}]业务处理服务不存在!", serviceName);
            throw new BusiException(11114);
        }

        log.info("|快捷支付|交易订单处理|外部交易订单,开始，tradeNo：{} 路由routeCode：{}", tradeNo, routeCode);
        //请求下单
        quickBusiHandler.unifiedOrder(requestMsg, responseMsg);
        log.info("|快捷支付|交易订单处理|外部交易订单,结束，tradeNo：{} 路由routeCode：{},responseMsg:{}", tradeNo, routeCode, JSON.toJSONString(responseMsg));

        String returnCode = (String) responseMsg.get(ConstEC.RETURNCODE);
        String returnMsg = (String) responseMsg.get(ConstEC.RETURNMSG);
        if (StringUtils.isBlank(returnCode)) {
            throw new BusiException(11001);
        }

        if (!returnCode.trim().equals(ConstEC.SUCCESS_10000)) {
            throw new BusiException(returnCode, returnMsg);
        }

        //将交易订单表状态改为W
        tradeOrder.setOrderStatus(OrderStatus.WAIT_PAY.getId());
        tradeOrder.setOutTradeNo((String) responseMsg.get("outTradeNo"));
        tradeOrder.setReturnCode(returnCode);
        tradeOrder.setReturnMsg(returnMsg);
        tradeOrderService.updateById(tradeOrder);
        smsOrder.setOutTradeNo((String) responseMsg.get("outTradeNo"));
        smsOrderService.updateById(smsOrder);
        log.info("|快捷支付|交易订单处理|交易订单更改状态 W, tradeNo：{},outTradeNo:{}", tradeNo, tradeOrder.getOutTradeNo());

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("agrNo",  iCipherService.encryptAES(agrNo));
        data.put("outTradeNo", tradeOrder.getOutTradeNo());
        data.put("smsOrderNo", smsOrder.getSmsOrderNo());
        data.put("needSms", "Y");
        data.put("prePayNo", requestMsg.get("prePayNo"));
        responseMsg.put(ConstEC.DATA, data);
        log.info("|快捷支付|交易订单处理|结束：{}", JSON.toJSONString(responseMsg));
    }

}
