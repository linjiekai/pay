package com.mppay.gateway.handler.quick;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.PlatformType;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.impl.BaseBusiHandlerImpl;
import com.mppay.service.entity.PrePayOrder;
import com.mppay.service.service.IPrePayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

@Service("quickPayReqHandler")
@Slf4j
public class QuickPayReqHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

    @Autowired
    private IPrePayOrderService prePayOrderService;

    @Override
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        String prePayNo = (String) requestMsg.get("prePayNo");
        String mercId = (String) requestMsg.get("mercId");
        log.info("|快捷支付|预支付订单处理|开始 requestMsg：{}", JSON.toJSONString(requestMsg));

        PrePayOrder prePayOrder = prePayOrderService.getOne(new QueryWrapper<PrePayOrder>().eq("pre_pay_no", prePayNo));
        Optional.ofNullable(prePayOrder).orElseThrow(() -> new BusiException(11117));

        String expTime = DateTimeUtil.date14();

        //判断预支付号是否超时
        if (expTime.compareTo(prePayOrder.getOrderExpTime()) >= 0) {
            throw new BusiException(11007);
        }

        prePayOrder.setId(null);
        prePayOrder.setCreateTime(null);
        prePayOrder.setUpdateTime(null);
        prePayOrder.setClientIp(null);
        prePayOrder.setPlatform(requestMsg.get("platform").toString());

        Map<String, Object> params = BeanMap.create(prePayOrder);
        requestMsg.putAll(params);


        //赚播跟引力 做随机立减
        if (mercId.equals(PlatformType.XFYLMALL.getId())
                || mercId.equals(PlatformType.ZBMALL.getId())) {
            BigDecimal bigDecimal = new BigDecimal(RandomUtil.randomDouble(0.5, 1.0)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal realPrice = prePayOrder.getPrice().subtract(bigDecimal);
            //如果减后金额<0 ,则不进行随机立减
            if (realPrice.compareTo(new BigDecimal(0)) == -1) {
                requestMsg.put("reducePrice", new BigDecimal(0));
            } else {
                requestMsg.put("reducePrice", bigDecimal);
                requestMsg.put("price", realPrice);
            }
        }

        log.info("|快捷支付|预支付订单处理|结束 requestMsg：{}", JSON.toJSONString(requestMsg));
    }

}
