package com.mppay.service.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mppay.service.entity.TradeRefundAlipay;

/**
 * <p>
 * 支付宝交易退款流水表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-02
 */
public interface TradeRefundAlipayMapper extends BaseMapper<TradeRefundAlipay> {

	@Select("select sum(price) total_price, count(1) counts "
			+ " from trade_refund_alipay where refund_date = #{refundDate} and check_status=#{checkStatus}"
			+ " and order_status=#{orderStatus}"
			)
	Map<String, Object> statPrice(TradeRefundAlipay tradeRefundAlipay);

}
