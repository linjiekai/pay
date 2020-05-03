package com.mppay.service.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mppay.service.entity.TradeOrderWeixin;

/**
 * <p>
 * 微信交易订单流水表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-01
 */
public interface ITradeOrderWeixinService extends IService<TradeOrderWeixin> {

	Map<String, Object> statPrice(TradeOrderWeixin tradeOrderWeixin);

}
