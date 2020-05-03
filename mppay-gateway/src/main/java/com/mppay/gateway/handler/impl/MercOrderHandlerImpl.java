package com.mppay.gateway.handler.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.constant.*;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.BankRoute;
import com.mppay.service.entity.MercOrder;
import com.mppay.service.entity.Route;
import com.mppay.service.service.IBankRouteService;
import com.mppay.service.service.IMercOrderService;
import com.mppay.service.service.IRouteService;
import com.mppay.service.service.ISeqIncrService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service("mercOrderHandler")
@Slf4j
public class MercOrderHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

    @Autowired
    private IMercOrderService mercOrderService;

    @Autowired
    private ISeqIncrService seqIncrService;

    @Autowired
    private IBankRouteService bankRouteService;

    @Autowired
    private IRouteService routeService;

    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {

        String tradeType = (String) requestMsg.get("tradeType");
        String bankCode = (String) requestMsg.get("bankCode");
        String mercId = (String) requestMsg.get("mercId");
        String bankCardType = (String) requestMsg.get("bankCardType");
        if (StringUtils.isBlank(bankCardType)) {
            bankCardType = "08";
        }

        BankRoute bankRoute = bankRouteService.getOne(new QueryWrapper<BankRoute>().eq("bank_code", requestMsg.get("bankCode"))
                .eq("trade_code", requestMsg.get("tradeCode"))
                .eq("bank_card_type", bankCardType)
                .eq("merc_id", mercId)
                .last(" limit 1")
        );

        if (null == bankRoute) {
            log.error("{}::{}", ApplicationYmlUtil.get(30002), JSONUtil.toJsonStr(requestMsg));
            throw new BusiException(30002);
        }

        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", bankRoute.getRouteCode()));

        // 如果银行渠道状态不正常，异常返回
        if (null == bankRoute || route.getStatus().intValue() != RouteStatus.NORMAL.getId()) {
            log.error("{}::{}", ApplicationYmlUtil.get(11118), JSONUtil.toJsonStr(requestMsg));
            throw new BusiException(11118);
        }

        requestMsg.put("routeCode", bankRoute.getRouteCode());

        String orderNo = (String) requestMsg.get("orderNo");

        Integer period = Integer.parseInt(requestMsg.get("period").toString());
        String periodUnit = requestMsg.get("periodUnit").toString();

        //计算失效时间
        String expTime = DateTimeUtil.formatTimestamp2String(DateTimeUtil.getPeriodTime(period, periodUnit), "yyyyMMddHHmmss");
        MercOrder mercOrder = mercOrderService.getOne(new QueryWrapper<MercOrder>().eq("merc_id", requestMsg.get("mercId")).eq("order_no", orderNo));

        //如果订单不存在，则创建新的订单
        if (null == mercOrder) {
            mercOrder = new MercOrder();
            String mercOrderNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.MERC_ORDER_NO.getId(), SeqIncrType.MERC_ORDER_NO.getLength(), Align.LEFT);
            requestMsg.put("mercOrderNo", mercOrderNo);
            BeanUtils.populate(mercOrder, requestMsg.getMap());
            mercOrder.setOrderExpTime(expTime);
            mercOrder.setOrderStatus(OrderStatus.WAIT_PAY.getId());
            mercOrder.setMercOrderNo(mercOrderNo);
            mercOrderService.save(mercOrder);

            return;
        }

        if (!(mercOrder.getOrderStatus().equals(OrderStatus.WAIT_PAY.getId())
                || mercOrder.getOrderStatus().equals(OrderStatus.FAIL.getId()))) {
            log.error("{}::{}", ApplicationYmlUtil.get(11002), JSONUtil.toJsonStr(requestMsg));
            throw new BusiException(11002);
        }

        //当前时间
        String currentTime = DateTimeUtil.date14();
        //如果订单超时，则不允许该订单继续交易
        if (mercOrder.getOrderExpTime().compareTo(currentTime) < 0) {
            log.error("{}::{}", ApplicationYmlUtil.get(11007), JSONUtil.toJsonStr(requestMsg));
            throw new BusiException(11007);
        }

        BeanUtils.populate(mercOrder, requestMsg.getMap());
        mercOrder.setOrderExpTime(expTime);
        mercOrder.setOrderStatus(OrderStatus.WAIT_PAY.getId());

        boolean flag = mercOrderService.update(mercOrder, new UpdateWrapper<MercOrder>()
                .eq("id", mercOrder.getId())
                .in("order_status", OrderStatus.WAIT_PAY.getId(), OrderStatus.FAIL.getId())
        );

        if (!flag) {
            log.error("{}::{}", ApplicationYmlUtil.get(11002), JSONUtil.toJsonStr(requestMsg));
            throw new BusiException(11002);
        }

        requestMsg.put("mercOrderNo", mercOrder.getMercOrderNo());

    }






}
