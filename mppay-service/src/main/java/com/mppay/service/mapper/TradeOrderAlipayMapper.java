package com.mppay.service.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mppay.service.entity.TradeOrderAlipay;

/**
 * <p>
 * 支付宝交易订单流水表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-01
 */
public interface TradeOrderAlipayMapper extends BaseMapper<TradeOrderAlipay> {
	
	@Select("select sum(price) total_price, count(1) counts "
			+ " from trade_order_alipay where trade_date = #{tradeDate} and check_status=#{checkStatus}"
			+ " and order_status=#{orderStatus}"
			)
	Map<String, Object> statPrice(TradeOrderAlipay tradeOrderAlipay);

}
