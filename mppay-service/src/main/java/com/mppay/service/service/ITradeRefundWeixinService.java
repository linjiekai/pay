package com.mppay.service.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mppay.service.entity.TradeRefundWeixin;

/**
 * <p>
 * 微信交易退款流水表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-02
 */
public interface ITradeRefundWeixinService extends IService<TradeRefundWeixin> {

	Map<String, Object> statPrice(TradeRefundWeixin tradeRefundWeixin);

}
