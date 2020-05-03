package com.mppay.gateway.scheduled;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.utils.LogUtil;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.service.entity.MercOrder;
import com.mppay.service.entity.MercOrderRefund;
import com.mppay.service.service.IMercOrderRefundService;
import com.mppay.service.service.IMercOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.controller.notify.MonitorController;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.service.entity.TradeRefund;
import com.mppay.service.service.ITradeRefundService;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 交易订单退款定时器
 *
 */
@Component
@Slf4j
public class TradeRefundScheduled {

	@Autowired
	private ITradeRefundService tradeRefundService;
	
	@Autowired
	private UnifiedHandler tradeRefundHandler;
	
	@Autowired
	private UnifiedHandler tradeRefundQueryHandler;

	@Autowired
	private IMercOrderRefundService iMercOrderRefundService;
	@Autowired
	private IMercOrderService iMercOrderService;

	//标识定时器状态 1：开启 0：关闭
	public static Integer SCHEDULER_STATUS = 0;
	
	//标识定时器状态 1：开启 0：关闭
	public static Integer SCHEDULER_STATUS2 = 0;
	
	/**
	 * 交易退款
	 */
	@Scheduled(cron = "${scheduled.trade-refund}")
    public void tradeRefund() {
		
		if (MonitorController.SCHEDULED_SWITCH == 0) {
			SCHEDULER_STATUS = 0;
			return;
		}
		SCHEDULER_STATUS = 1;
		String s = DateTimeUtil.formatTimestamp2String(new Date(), "yyyy-MM-dd HH:mm:ss");
		LogUtil.SCHEDULED.info("定时器::交易订单退款，处理::开始，时间段：{}",s);
		try {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -2);
			Date startTime = cal.getTime();
			cal.add(Calendar.MINUTE, - 60 * 24 * 2);
			Date endTime = cal.getTime();
			
			List<TradeRefund> tradeRefundList = tradeRefundService.list(
					new QueryWrapper<TradeRefund>().eq("order_status", OrderStatus.REFUND.getId())
					.ge("create_time", endTime)
					.le("create_time", startTime)
					);
			LogUtil.SCHEDULED.info("定时器::交易订单退款，处理:: List.size()：{}",tradeRefundList.size());
			RequestMsg requestMsg = null;
			Map<String, Object> data = null;
			for (TradeRefund tradeRefund : tradeRefundList) {
				String refundNo = tradeRefund.getRefundNo();
				try  {
					if (MonitorController.SCHEDULED_SWITCH == 0) {
						SCHEDULER_STATUS = 0;
						LogUtil.SCHEDULED.info("定时器::交易订单退款，预登记::定时器已关闭SCHEDULER_SWITCH：{}", SCHEDULER_STATUS);
						return;
					}
					data = new HashMap<String, Object>();
					data.putAll(BeanUtils.beanToMap(tradeRefund));
					requestMsg = new RequestMsg(data);
					tradeRefundHandler.execute(requestMsg);
					LogUtil.SCHEDULED.info("定时器::交易订单退款，处理::完成，订单号refundNo：{},outTradeNo：{}",refundNo,tradeRefund.getOutTradeNo());
				} catch (Exception e) {
					LogUtil.SCHEDULED.info("定时器::交易订单退款，处理::失败，订单号refundNo：{},outTradeNo：{}",refundNo,tradeRefund.getOutTradeNo());
					log.error("订单退款失败，订单号refundNo:{},outTradeNo：{},error：{}", refundNo,tradeRefund.getOutTradeNo(), e);
				}
			}
		} catch (Exception e) {
			log.error("订单退款失败", e);
		}
		LogUtil.SCHEDULED.info("定时器::交易订单退款，处理::结束，时间段：{}",s);
		SCHEDULER_STATUS = 0;
    }
	
	/**
	 * 交易退款查询
	 */
	@Scheduled(cron = "${scheduled.trade-refund-query}")
    public void tradeRefundQuery() {
		
		if (MonitorController.SCHEDULED_SWITCH == 0) {
			SCHEDULER_STATUS2 = 0;
			return;
		}
		SCHEDULER_STATUS2 = 1;

		String s = DateTimeUtil.formatTimestamp2String(new Date(), "yyyy-MM-dd HH:mm:ss");
		LogUtil.SCHEDULED.info("定时器::交易订单退款，查询::开始，时间段：{}",s);
		try {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -2);
			Date endTime = cal.getTime();
			cal.add(Calendar.MINUTE, - 60 * 24 * 15);
			Date startTime = cal.getTime();
			
			List<TradeRefund> tradeRefundList = tradeRefundService.list(
					new QueryWrapper<TradeRefund>().eq("order_status", OrderStatus.REFUND_WAIT.getId())
					.ge("create_time", DateTimeUtil.formatTimestamp2String(startTime, "yyyy-MM-dd HH:mm:ss"))
					.le("create_time", DateTimeUtil.formatTimestamp2String(endTime, "yyyy-MM-dd HH:mm:ss"))
					);
			LogUtil.SCHEDULED.info("定时器::交易订单退款，查询::  List.size()：{}",tradeRefundList.size());
			RequestMsg requestMsg = null;
			Map<String, Object> data = null;
			for (TradeRefund tradeRefund : tradeRefundList) {
				String refundNo = tradeRefund.getRefundNo();
				String outTradeNo = tradeRefund.getOutTradeNo();
				try  {
					if (MonitorController.SCHEDULED_SWITCH == 0) {
						SCHEDULER_STATUS2 = 0;
						LogUtil.SCHEDULED.info("定时器::交易订单退款，等待退款::定时器已关闭SCHEDULER_SWITCH：{}", SCHEDULER_STATUS);
						return;
					}
					
					data = new HashMap<String, Object>();
					data.putAll(BeanUtils.beanToMap(tradeRefund));
					requestMsg = new RequestMsg(data);
					ResponseMsg responseMsg = tradeRefundQueryHandler.execute(requestMsg);

					MercOrder mercOrder = iMercOrderService.getOne(new QueryWrapper<MercOrder>().eq("out_trade_no", outTradeNo).last("limit 1"));
					//商户退款订单改状态 S，shop定时器捞到这个S状态代表退款成功
					iMercOrderRefundService.update(new UpdateWrapper<MercOrderRefund>()
							.set("order_status", (String)responseMsg.get("orderStatus"))
							.set("return_code", (String)responseMsg.get(ConstEC.RETURNCODE))
							.set("return_msg", (String)responseMsg.get(ConstEC.RETURNMSG))
							.set("refund_price", (BigDecimal)responseMsg.get("actualPrice"))
							.set("bank_code", tradeRefund.getBankCode())
							.set("open_id", mercOrder.getOpenId())
							.set("agr_no", mercOrder.getAgrNo())
							.set("app_id", mercOrder.getAppId())
							.set("out_refund_no", responseMsg.get("outRefundNo"))
							.eq("merc_refund_no", tradeRefund.getMercRefundNo()));
					LogUtil.SCHEDULED.info("定时器::交易订单退款，查询::完成，订单号refundNo：{},outTradeNo：{}",refundNo, outTradeNo);
				} catch (Exception e) {
					LogUtil.SCHEDULED.info("定时器::交易订单退款，查询::失败，订单号refundNo：{}，outTradeNo：{}",refundNo, outTradeNo);
					log.error("定时器::交易订单退款，查询::失败，订单号refundNo：{}，outTradeNo：{}，error:{}", refundNo, outTradeNo,e);
				}
			}
		} catch (Exception e) {
			log.error("订单退款失败", e);
		}
		LogUtil.SCHEDULED.info("定时器::交易订单退款，查询::结束，时间段：{}",s);
		SCHEDULER_STATUS2 = 0;
    }
	
}
