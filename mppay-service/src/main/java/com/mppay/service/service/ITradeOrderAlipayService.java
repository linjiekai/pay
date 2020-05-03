package com.mppay.service.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mppay.service.entity.TradeOrderAlipay;

/**
 * <p>
 * 支付宝交易订单流水表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-01
 */
public interface ITradeOrderAlipayService extends IService<TradeOrderAlipay> {

	Map<String, Object> statPrice(TradeOrderAlipay tradeOrderAlipay);

}
