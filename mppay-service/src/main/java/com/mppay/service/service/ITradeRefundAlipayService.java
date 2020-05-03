package com.mppay.service.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mppay.service.entity.TradeRefundAlipay;

/**
 * <p>
 * 支付宝交易退款流水表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-02
 */
public interface ITradeRefundAlipayService extends IService<TradeRefundAlipay> {

	Map<String, Object> statPrice(TradeRefundAlipay tradeRefundAlipay);
}
