package com.mppay.gateway.scheduled;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.service.entity.CheckControl;
import com.mppay.service.entity.CheckParam;
import com.mppay.service.service.ICheckControlService;
import com.mppay.service.service.ICheckParamService;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.controller.notify.MonitorController;

import lombok.extern.slf4j.Slf4j;

/**
 * 定时生成对账批次,同时需要兼容异常情况下能手工触发生成批次,一天情况下只能生成一个对账批次,每天凌晨01:00:00时候触发
 *
 */
@Component
@Slf4j
public class CheckControlScheduled {

	@Autowired
	private ICheckParamService checkParamService;
	
	@Autowired
	private ICheckControlService checkControlService;
	
	//标识定时器状态 1：开启 0：关闭
	public static Integer SCHEDULER_STATUS = 0;
	
	@Scheduled(cron = "${scheduled.check-control}")
    public void execute() {

		if (MonitorController.SCHEDULED_SWITCH == 0) {
			SCHEDULER_STATUS = 0;
			return;
		}
		
		SCHEDULER_STATUS = 1;
		try {
			List<CheckParam> checkParamList = checkParamService.list(new QueryWrapper<CheckParam>());
			
			CheckControl checkControl = null;
			for (CheckParam checkParam : checkParamList) {
				try {
					if (MonitorController.SCHEDULED_SWITCH == 0) {
						SCHEDULER_STATUS = 0;
						log.info("定时生成对账批次已关闭SCHEDULER_STATUS[{}]........", SCHEDULER_STATUS);
						return;
					}
					checkControl = new CheckControl();
					checkControl.setAccountDate(DateTimeUtil.date10(DateTimeUtil.beforeDay(1)));
					checkControl.setRouteCode(checkParam.getRouteCode());
					checkControl.setTradeCode(checkParam.getTradeCode());
					checkControl.setCreateDate(DateTimeUtil.date10());
					
					checkControlService.save(checkControl);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SCHEDULER_STATUS = 0;
	}
}
