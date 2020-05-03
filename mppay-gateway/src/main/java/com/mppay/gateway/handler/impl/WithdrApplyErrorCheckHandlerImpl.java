package com.mppay.gateway.handler.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.WithdrOrderStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.WithdrOrder;
import com.mppay.service.service.IMasterAccountBalService;
import com.mppay.service.service.IWithdrOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @description: 异常提现订单检查
 * @date 2019/10/30 15:36
 */
@Service("withdrApplyErrorCheckHandler")
@Slf4j
public class WithdrApplyErrorCheckHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

    @Autowired
    private IWithdrOrderService withdrOrderService;
    @Autowired
    private IMasterAccountBalService masterAccountBalService;

    @Override
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        // 查出订单
        WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>()
                .eq("merc_id", requestMsg.get("mercId"))
                .eq("order_no", requestMsg.get("orderNo"))
                .eq("order_status", WithdrOrderStatus.AUDIT.getId()));

        if (withdrOrder != null) {
            boolean operFlag = masterAccountBalService.backWithdrUavaBal(withdrOrder);
            if (!operFlag) {
                log.error("更新账户余额失败， user_no=" + withdrOrder.getUserNo() + ", withdrOrderNo=" + withdrOrder.getWithdrOrderNo() + ", price=" + withdrOrder.getPrice());
                throw new BusiException("15101", ApplicationYmlUtil.get("15101"));
            }

            withdrOrder.setOrderStatus(WithdrOrderStatus.FAIL.getId());
            withdrOrderService.updateById(withdrOrder);
        }

        responseMsg.put(ConstEC.DATA, new HashMap<String, Object>(0));
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        return;
    }
}
