package com.mppay.gateway.scheduled;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.WithdrOrderStatus;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.LogUtil;
import com.mppay.gateway.controller.notify.MonitorController;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.service.entity.WithdrOrder;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.IWithdrOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提现订单定时器
 */
@Component
@Slf4j
public class WithdrOrderScheduled {

    @Autowired
    private IWithdrOrderService withdrOrderService;

    @Autowired
    private UnifiedHandler withdrOrderHandler;

    @Autowired
    private UnifiedHandler withdrOrderQueryHandler;

    @Autowired
    private IDictionaryService dictionaryService;

    //标识定时器状态 1：开启 0：关闭
    public static Integer SCHEDULER_STATUS = 0;

    //标识定时器状态 1：开启 0：关闭
    public static Integer SCHEDULER_STATUS2 = 0;

    @Scheduled(cron = "${scheduled.withdr-order}")
    public void withdrOrder() {

        if (MonitorController.SCHEDULED_SWITCH == 0) {
            SCHEDULER_STATUS = 0;
            return;
        }
        SCHEDULER_STATUS = 1;

        String s = DateTimeUtil.formatTimestamp2String(new Date(), "yyyy-MM-dd HH:mm:ss");
        LogUtil.SCHEDULED.info("定时器::提现订单，W/WP::开始，时间段：{}",s);
        try {
            Long withdrOper = dictionaryService.findForLong("withdr", "open","0");
            if (null != withdrOper && withdrOper.longValue() > 0) {
                LocalDateTime time = LocalDateTime.now();
                time = time.minusMinutes(2);
                List<WithdrOrder> withdrOrderList = withdrOrderService.list(new QueryWrapper<WithdrOrder>()
                        .in("order_status", WithdrOrderStatus.WAIT.getId(), WithdrOrderStatus.WAIT_PAY.getId())
                        .le("create_time", time)
                );
                LogUtil.SCHEDULED.info("定时器::提现订单，W/WP::list.size()：{}",withdrOrderList.size());
                RequestMsg requestMsg = null;
                Map<String, Object> data = null;
                for (WithdrOrder withdrOrder : withdrOrderList) {
                    String withdrOrderNo = withdrOrder.getWithdrOrderNo();
                    try {
                        if (MonitorController.SCHEDULED_SWITCH == 0) {
                            SCHEDULER_STATUS = 0;
                            LogUtil.SCHEDULED.info("定时器::提现订单，W/WP::定时器已关闭SCHEDULER_SWITCH：{}", SCHEDULER_STATUS);
                            return;
                        }
                        data = new HashMap<String, Object>();
                        data.putAll(BeanUtil.beanToMap(withdrOrder));
                        requestMsg = new RequestMsg(data);
                        withdrOrderHandler.execute(requestMsg);
                        LogUtil.SCHEDULED.info("定时器::提现订单，W/WP::完成，订单号withdrOrderNo：{}",withdrOrderNo);
                    } catch (Exception e) {
                        LogUtil.SCHEDULED.info("定时器::提现订单，W/WP::失败，订单号withdrOrderNo：{}",withdrOrderNo);
                        log.error("定时器::提现订单，W/WP::失败，订单号withdrOrderNo：{},error:{}", withdrOrderNo,e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("提现订单失败", e);
        }
        LogUtil.SCHEDULED.info("定时器::提现订单，W/WP::结束，时间段：{}",s);
        SCHEDULER_STATUS = 0;
    }

    @Scheduled(cron = "${scheduled.query-withdr-order}")
    public void queryWithdrOrder() {
        if (MonitorController.SCHEDULED_SWITCH == 0) {
            SCHEDULER_STATUS2 = 0;
            return;
        }
        SCHEDULER_STATUS2 = 1;
        String s = DateTimeUtil.formatTimestamp2String(new Date(), "yyyy-MM-dd HH:mm:ss");
        LogUtil.SCHEDULED.info("定时器::提现订单，BW::开始，时间段：{}",s);
        try {
            List<WithdrOrder> withdrOrderList = withdrOrderService.list(new QueryWrapper<WithdrOrder>()
                    .eq("order_status", WithdrOrderStatus.BANK_WAIT.getId()));
            LogUtil.SCHEDULED.info("定时器::提现订单，BW::list.size()：{}",withdrOrderList.size());
            RequestMsg requestMsg = null;
            Map<String, Object> data = null;
            for (WithdrOrder withdrOrder : withdrOrderList) {
                String withdrOrderNo = withdrOrder.getWithdrOrderNo();
                try {
                    if (MonitorController.SCHEDULED_SWITCH == 0) {
                        SCHEDULER_STATUS2 = 0;
                        log.info("交易提现订单定时器已关闭SCHEDULER_SWITCH:{}", SCHEDULER_STATUS);
                        return;
                    }
                    data = new HashMap<String, Object>();
                    data.putAll(BeanUtil.beanToMap(withdrOrder));
                    requestMsg = new RequestMsg(data);
                    withdrOrderQueryHandler.execute(requestMsg);
                    LogUtil.SCHEDULED.info("定时器::提现订单，BW::完成，订单号withdrOrderNo：{}",withdrOrderNo);
                } catch (Exception e) {
                    LogUtil.SCHEDULED.info("定时器::提现订单，BW::失败，订单号withdrOrderNo：{}",withdrOrderNo);
                    log.error("定时器::提现订单，BW::失败,withdrOrderNo:{},error:{}", withdrOrderNo,e);
                }
            }
        } catch (Exception e) {
            log.error("提现订单失败", e);
        }
        LogUtil.SCHEDULED.info("定时器::提现订单，BW::结束，时间段：{}",s);
        SCHEDULER_STATUS2 = 0;
    }

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime = now.minusMinutes(2);
        System.out.println(localDateTime);

    }
}
