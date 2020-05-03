package com.mppay.gateway.scheduled;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mppay.core.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.PayType;
import com.mppay.gateway.controller.notify.MonitorController;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.handler.BankBusiHandler;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.service.entity.TradeOrder;
import com.mppay.service.service.ITradeOrderService;
import com.mppay.core.utils.DateTimeUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 查询补单
 *
 */
@Component
@Slf4j
public class TradeOrderQueryScheduled {

	@Autowired
	private ITradeOrderService tradeOrderService;
	
	//标识定时器状态 1：开启 0：关闭
	public static Integer SCHEDULER_STATUS = 0;
		
	@Scheduled(cron = "${scheduled.trade-order-query}")
    public void execute() {
		if (MonitorController.SCHEDULED_SWITCH == 0) {
			SCHEDULER_STATUS = 0;
			return;
		}
		SCHEDULER_STATUS = 1;
		String s = DateTimeUtil.formatTimestamp2String(new Date(), "yyyy-MM-dd HH:mm:ss");
		LogUtil.SCHEDULED.info("定时器::查询补单::开始，时间段：{}", s);
		try {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -2);
			Date endTime = cal.getTime();
			cal.add(Calendar.MINUTE, -60);
			Date startTime = cal.getTime();
			
			List<TradeOrder> tradeOrderList = tradeOrderService.list(
					new QueryWrapper<TradeOrder>().eq("order_status", OrderStatus.WAIT_PAY.getId())
					.eq("pay_type", PayType.UNIFIED.getId())
					.ge("create_time", DateTimeUtil.formatTimestamp2String(startTime, "yyyy-MM-dd HH:mm:ss"))
					.le("create_time", DateTimeUtil.formatTimestamp2String(endTime, "yyyy-MM-dd HH:mm:ss"))
					);
			LogUtil.SCHEDULED.info("定时器::查询补单::统一支付订单, list.size：{}", tradeOrderList.size());
			RequestMsg requestMsg = null;
			String serviceName = null;
			BankBusiHandler bankBusiHandler = null;
			Map<String, Object> data = null;
			for (TradeOrder tradeOrder : tradeOrderList) {
				String outTradeNo = tradeOrder.getOutTradeNo();
				try  {
					if (MonitorController.SCHEDULED_SWITCH == 0) {
						SCHEDULER_STATUS = 0;
						LogUtil.SCHEDULED.info("定时器::查询补单::统一支付订单::定时器已关闭 SCHEDULER_SWITCH：{}", SCHEDULER_STATUS);
						return;
					}
					//拼出service name
					serviceName = tradeOrder.getRouteCode().toLowerCase() + ConstEC.BANKBUSIHANDLER;
					//通过spring ApplicationContext获取service对象
					bankBusiHandler = (BankBusiHandler) SpringContextHolder.getBean(serviceName);
					if (null == bankBusiHandler) {
						log.error("serviceName[{}]业务处理服务不存在!", serviceName);
						continue;
					}
					data = new HashMap<String, Object>();
					data.put("mercId", tradeOrder.getMercId());
					data.put("outTradeNo", outTradeNo);
					data.put("userId", tradeOrder.getUserId());
					data.put("bankCode", tradeOrder.getBankCode());
					data.put("tradeType", tradeOrder.getTradeType());
					data.put("platform", tradeOrder.getPlatform());
					data.put("routeCode", tradeOrder.getRouteCode());
					requestMsg = new RequestMsg(data);
					bankBusiHandler.queryOrder(requestMsg);
					//LogUtil.SCHEDULED.info("定时器::查询补单::统一支付订单::完成，订单号outTradeNo：{}", outTradeNo);
				} catch (Exception e) {
					LogUtil.SCHEDULED.info("定时器::查询补单::统一支付订单::失败，订单号outTradeNo：{}", outTradeNo);
					log.error("定时器::查询补单::失败，订单号outTradeNo：{},error:{}",outTradeNo,e);
				}
			}
			LogUtil.SCHEDULED.info("定时器::查询补单::统一支付订单, 结束 ");

			tradeOrderList = tradeOrderService.list(
					new QueryWrapper<TradeOrder>().eq("order_status", OrderStatus.WAIT_PAY.getId())
					.eq("pay_type", PayType.QUICK.getId())
					.ge("create_time", DateTimeUtil.formatTimestamp2String(startTime, "yyyy-MM-dd HH:mm:ss"))
					.le("create_time", DateTimeUtil.formatTimestamp2String(endTime, "yyyy-MM-dd HH:mm:ss"))
					);
			LogUtil.SCHEDULED.info("定时器::查询补单::快捷支付, list.size：{}", tradeOrderList.size());
			QuickBusiHandler quickBusiHandler = null;
			for (TradeOrder tradeOrder : tradeOrderList) {
				String outTradeNo = tradeOrder.getOutTradeNo();
				try  {
					if (MonitorController.SCHEDULED_SWITCH == 0) {
						SCHEDULER_STATUS = 0;
						LogUtil.SCHEDULED.info("定时器::查询补单::快捷支付::定时器已关闭SCHEDULER_SWITCH：{}", SCHEDULER_STATUS);
						return;
					}
					//拼出service name
					serviceName = tradeOrder.getRouteCode().toLowerCase() + ConstEC.QUICKBUSIHANDLER;
					
					//通过spring ApplicationContext获取service对象
					quickBusiHandler = (QuickBusiHandler) SpringContextHolder.getBean(serviceName);
					
					if (null == bankBusiHandler) {
						log.error("serviceName[{}]业务处理服务不存在!", serviceName);
						continue;
					}
					
					data = new HashMap<String, Object>();
					data.put("mercId", tradeOrder.getMercId());
					data.put("outTradeNo", outTradeNo);
					data.put("userId", tradeOrder.getUserId());
					data.put("bankCode", tradeOrder.getBankCode());
					data.put("tradeType", tradeOrder.getTradeType());
					data.put("platform", tradeOrder.getPlatform());
					data.put("routeCode", tradeOrder.getRouteCode());
					requestMsg = new RequestMsg(data);
					quickBusiHandler.queryOrder(requestMsg);
					LogUtil.SCHEDULED.info("定时器::查询补单::快捷支付::完成，订单号outTradeNo：{}", outTradeNo);
				} catch (Exception e) {
					LogUtil.SCHEDULED.info("定时器::查询补单::快捷支付::失败，订单号outTradeNo：{}", outTradeNo);
					log.error("定时器::查询补单::快捷支付::失败，订单号outTradeNo：{},error:{}", outTradeNo,e);
				}
			}
			LogUtil.SCHEDULED.info("定时器::查询补单::快捷支付, 结束 ");
		} catch (Exception e) {
			LogUtil.SCHEDULED.info("定时器::查询补单::失败，时间段：{}", s);
		}
		LogUtil.SCHEDULED.info("定时器::查询补单::结束，时间段：{}", s);
		SCHEDULER_STATUS = 0;
    }
}
