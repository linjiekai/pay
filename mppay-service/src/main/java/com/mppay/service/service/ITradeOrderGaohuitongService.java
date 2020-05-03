package com.mppay.service.service;

import com.mppay.service.entity.TradeOrderGaohuitong;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mppay.service.entity.TradeOrderWeixin;

import java.util.Map;

/**
 * <p>
 * 高汇通交易订单流水表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
public interface ITradeOrderGaohuitongService extends IService<TradeOrderGaohuitong> {

    Map<String, Object> statPrice(TradeOrderGaohuitong tradeOrderWeixin);
}
