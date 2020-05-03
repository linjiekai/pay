package com.mppay.gateway.scheduled;

import java.util.Date;
import java.util.List;

import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.TradeCode;
import com.mppay.gateway.controller.notify.MonitorController;
import com.mppay.gateway.handler.CheckCenterHandler;
import com.mppay.service.entity.CheckControl;
import com.mppay.service.service.ICheckControlService;

import lombok.extern.slf4j.Slf4j;

/**
 * 定时下载对账文件和定时对账轮询，早上十点开始轮询(微信早上九点才提供对账文件）
 *
 */
@Component
@Slf4j
public class CheckCenterScheduled {

	@Autowired
	private ICheckControlService checkControlService;
	
	// 用来标识定时器状态 1：开启 0：关闭
	public static Integer SCHEDULER_STATUS = 0;
	
	@Scheduled(cron = "${scheduled.check-center}")
    public void execute() {
		
		if (MonitorController.SCHEDULED_SWITCH == 0) {
			SCHEDULER_STATUS = 0;
			return;
		}
		
		SCHEDULER_STATUS = 1;

		String s = DateTimeUtil.formatTimestamp2String(new Date(), "yyyy-MM-dd HH:mm:ss");
		LogUtil.SCHEDULED.info("定时器::对账批次处理::开始，时间段：{}", s);
		try {
			List<CheckControl> checkControlList = checkControlService.list(
					new QueryWrapper<CheckControl>().in("check_status", 0, 1, 2)
					.orderByAsc("id")
					);
			LogUtil.SCHEDULED.info("定时器::对账批次处理::批次 list.size()：{}", checkControlList.size());
			CheckCenterHandler checkCenterHandler = null;
			String serviceName = null;
			for (CheckControl checkControl : checkControlList) {
				Long id = checkControl.getId();
				try {
					if (MonitorController.SCHEDULED_SWITCH == 0) {
						SCHEDULER_STATUS = 0;
						log.info("定时下载对账文件和定时对账轮询定时器已关闭SCHEDULER_SWITCH[{}]........", SCHEDULER_STATUS);
						return;
					}
					//拼出service name
					serviceName = checkControl.getRouteCode().toLowerCase();
					
					if (checkControl.getTradeCode().equals(TradeCode.TRADEREFUND.getId())) {
						serviceName += "Refund";
					}
					serviceName += ConstEC.CHECKCENTERHANDLER;
					checkCenterHandler = SpringContextHolder.getBean(serviceName);
					checkCenterHandler.check(id);
					LogUtil.SCHEDULED.info("定时器::对账批次处理::完成，批次：{}", id);
				} catch (Exception e) {
					LogUtil.SCHEDULED.info("定时器::对账批次处理::失败，批次：{}", id);
					log.error("定时器::对账批次处理::失败，批次：{},error:{}",id,e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogUtil.SCHEDULED.info("定时器::对账批次处理::结束，时间段：{}", s);
		SCHEDULER_STATUS = 0;
	}
}
