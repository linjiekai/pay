package com.mppay.gateway.handler.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mppay.core.constant.CardBindStatus;
import com.mppay.service.entity.CardBind;
import com.mppay.service.service.ICardBindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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

/**
 * 提现订单审核处理
 * @author chenfeihang
 *
 */
@Service("withdrAuditHandler")
@Slf4j
public class WithdrAuditHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

	@Autowired
	private IWithdrOrderService withdrOrderService;

	@Autowired
	private IMasterAccountBalService masterAccountBalService;

	@Autowired
	private ICardBindService cardBindService;

	@Override
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		log.info("|提现审核|接收到请求报文:{}", requestMsg);
		String orderStatus = (String) requestMsg.get("orderStatus");
		if (!(WithdrOrderStatus.WAIT.getId().equalsIgnoreCase(orderStatus) || WithdrOrderStatus.REFUSE.getId().equalsIgnoreCase(orderStatus))) {
			log.error(ApplicationYmlUtil.get("11125")+ ",orderStatus=[" + orderStatus +"]," + requestMsg.toString());
			throw new BusiException("11125", ApplicationYmlUtil.get("11125"));
		}

		//查出订单
		WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("merc_id", requestMsg.get("mercId")).eq("order_no", requestMsg.get("orderNo")));
		if (null == withdrOrder) {
			log.error(ApplicationYmlUtil.get("11124") + requestMsg.toString());
			throw new BusiException(11124);
		}
		if (!WithdrOrderStatus.AUDIT.getId().equalsIgnoreCase(withdrOrder.getOrderStatus())) {
			log.error(ApplicationYmlUtil.get("11125")+ ",orderStatus=[" + withdrOrder.getOrderStatus() +"]," + requestMsg.toString());
			throw new BusiException(11125);
		}

		boolean operFlag = false;
		// 如果提现拒绝，返还余额
		if (WithdrOrderStatus.REFUSE.getId().equalsIgnoreCase(orderStatus)) {
			operFlag = masterAccountBalService.backWithdrUavaBal(withdrOrder);
			if (!operFlag) {
				log.error("更新账户余额失败， user_no=" + withdrOrder.getUserNo() + ", withdrOrderNo=" + withdrOrder.getWithdrOrderNo() + ", price=" + withdrOrder.getPrice());
				throw new BusiException("15101", ApplicationYmlUtil.get("15101"));
			}
			// 提现订单状态更改为[R:审核拒绝]
			withdrOrderService.update(new UpdateWrapper<WithdrOrder>().set("order_status",WithdrOrderStatus.REFUSE.getId()).eq("order_no", withdrOrder.getOrderNo()));
			responseMsg.put(ConstEC.DATA, new HashMap<String, Object>());
			responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
			responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
			return;
		}

		// 根据协议号,校验卡绑定状态
		String agrNo = withdrOrder.getAgrNo();
		CardBind cardBind = cardBindService.getOne(new QueryWrapper<CardBind>().eq("agr_no", agrNo));
		Optional.ofNullable(cardBind).orElseThrow(()->new BusiException(15002));
		Integer status = cardBind.getStatus();
		if(CardBindStatus.BINDING.getId() != status.intValue()){
			log.error("|提现审核|绑定关系校验|绑定关系无效,绑定状态:{}", status);
//			throw new BusiException(15014);
		}

		operFlag = withdrOrderService.update(new UpdateWrapper<WithdrOrder>().set("order_status", WithdrOrderStatus.WAIT.getId()).eq("order_no", withdrOrder.getOrderNo()).eq("order_status", WithdrOrderStatus.AUDIT.getId()));
		if (!operFlag) {
			log.error(ApplicationYmlUtil.get("11125")+ ",orderStatus=[" + orderStatus +"]," + requestMsg.toString());
			throw new BusiException("11125", ApplicationYmlUtil.get("11125"));
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("mercId", withdrOrder.getMercId());
		data.put("orderNo", withdrOrder.getOrderNo());
		data.put("orderDate", withdrOrder.getOrderDate());
		data.put("orderTime", withdrOrder.getOrderTime());
		data.put("bankWithdrDate", withdrOrder.getBankWithdrDate());
		data.put("bankWithdrTime", withdrOrder.getBankWithdrTime());
		data.put("outTradeNo", withdrOrder.getOutTradeNo());
		data.put("price", withdrOrder.getPrice());
		data.put("bankCode", withdrOrder.getBankCode());
		data.put("bankCardNo", withdrOrder.getBankCardNo());
		data.put("userId", withdrOrder.getUserId());
		data.put("orderStatus", withdrOrder.getOrderStatus());

		responseMsg.put(ConstEC.DATA, data);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	}
}
