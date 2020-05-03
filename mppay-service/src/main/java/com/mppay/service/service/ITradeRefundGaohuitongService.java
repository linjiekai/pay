package com.mppay.service.service;

import com.mppay.service.entity.TradeOrderGaohuitong;
import com.mppay.service.entity.TradeRefundGaohuitong;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 高汇通交易退款流水表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
public interface ITradeRefundGaohuitongService extends IService<TradeRefundGaohuitong> {

    Map<String, Object> statPrice(TradeRefundGaohuitong tradeRefundGaohuitong);
}
