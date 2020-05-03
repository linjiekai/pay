package com.mppay.service.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.TradeOrderWeixin;
import com.mppay.service.mapper.TradeOrderWeixinMapper;
import com.mppay.service.service.ITradeOrderWeixinService;

/**
 * <p>
 * 微信交易订单流水表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-01
 */
@Service
public class TradeOrderWeixinServiceImpl extends ServiceImpl<TradeOrderWeixinMapper, TradeOrderWeixin> implements ITradeOrderWeixinService {

	@Override
	public Map<String, Object> statPrice(TradeOrderWeixin tradeOrderWeixin) {
		return baseMapper.statPrice(tradeOrderWeixin);
	}

}
