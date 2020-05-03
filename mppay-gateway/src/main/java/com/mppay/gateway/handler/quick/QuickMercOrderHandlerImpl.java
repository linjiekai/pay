package com.mppay.gateway.handler.quick;

import com.alibaba.fastjson.JSON;
import com.mppay.core.constant.*;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.impl.BaseBusiHandlerImpl;
import com.mppay.service.entity.MercOrder;
import com.mppay.service.entity.QuickAgr;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.IMercOrderService;
import com.mppay.service.service.IQuickAgrService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.common.ICipherService;

import lombok.extern.slf4j.Slf4j;

@Service("quickMercOrderHandler")
@Slf4j
public class QuickMercOrderHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler{
	
	@Autowired
	private IMercOrderService mercOrderService;
	
	@Autowired
	private ISeqIncrService seqIncrService;
	
	@Autowired
	private IQuickAgrService quickAgrService;
	
	@Autowired
	private ICipherService cipherServiceImpl;
	
	public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		log.info("|快捷支付|商户订单处理|开始 参数：{}", JSON.toJSONString(requestMsg));
		String orderNo = (String) requestMsg.get("orderNo");
		Integer period = Integer.parseInt(requestMsg.get("period").toString());
		String periodUnit = requestMsg.get("periodUnit").toString();
		
		//计算失效时间
		String expTime = DateTimeUtil.formatTimestamp2String(DateTimeUtil.getPeriodTime(period, periodUnit), "yyyyMMddHHmmss");
		String agrNoCipher = (String) requestMsg.get("agrNo");
		//解密协议号
		String agrNo = cipherServiceImpl.decryptAES(agrNoCipher);
		
		requestMsg.put("agrNo", agrNo);
		QuickAgr quickAgr = quickAgrService.getOne(new QueryWrapper<QuickAgr>().eq("agr_no", agrNo).eq("status", QuickAgrStatus.NORMAL.getId()));
		if (null == quickAgr) {
			log.error(ApplicationYmlUtil.get(31003) + requestMsg.toString());
			throw new BusiException(31003);
		}
		
		MercOrder mercOrder = mercOrderService.getOne(new QueryWrapper<MercOrder>().eq("merc_id", requestMsg.get("mercId")).eq("order_no", orderNo));
		//如果订单不存在，则创建新的订单
		if (null == mercOrder) {
			mercOrder = new MercOrder();
			String mercOrderNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.MERC_ORDER_NO.getId(), SeqIncrType.MERC_ORDER_NO.getLength(), Align.LEFT);
			requestMsg.put("mercOrderNo", mercOrderNo);
			quickAgr.setId(null);
			quickAgr.setCreateTime(null);
			quickAgr.setUpdateTime(null);
			
			requestMsg.putAll(BeanMap.create(quickAgr));
			requestMsg.put("openId", quickAgr.getBankCardNo());
			
			BeanUtils.populate(mercOrder, requestMsg.getMap());
			mercOrder.setOrderExpTime(expTime);
			mercOrder.setOrderStatus(OrderStatus.WAIT_PAY.getId());
			mercOrder.setMercOrderNo(mercOrderNo);
			mercOrder.setTradeType(TradeType.QUICK.getId());
			mercOrder.setOpenId(quickAgr.getBankCardNo());
			mercOrderService.save(mercOrder);
			return;
		}
		
		if (!(mercOrder.getOrderStatus().equals(OrderStatus.WAIT_PAY.getId())
				|| mercOrder.getOrderStatus().equals(OrderStatus.FAIL.getId())
				)) {
			log.error(ApplicationYmlUtil.get(11002) + requestMsg.toString());
			throw new BusiException(11002);
		}
		
		//当前时间
		String currentTime = DateTimeUtil.date14();
		//如果订单超时，则不允许该订单继续交易
		if (mercOrder.getOrderExpTime().compareTo(currentTime) < 0) {
			log.error(ApplicationYmlUtil.get(11007) + requestMsg.toString());
			throw new BusiException(11007);
		}
		
		BeanUtils.populate(mercOrder, requestMsg.getMap());
		mercOrder.setOrderExpTime(expTime);
		mercOrder.setOrderStatus(OrderStatus.WAIT_PAY.getId());
		mercOrder.setOpenId(quickAgr.getBankCardNo());
		boolean flag = mercOrderService.update(mercOrder, new UpdateWrapper<MercOrder>()
				.eq("id", mercOrder.getId())
				.in("order_status", OrderStatus.WAIT_PAY.getId(), OrderStatus.FAIL.getId())
				);
		
		if (!flag) {
			log.error(ApplicationYmlUtil.get(11002) + requestMsg.toString());
			throw new BusiException(11002);
		}
		requestMsg.put("routeCode", quickAgr.getRouteCode());
		requestMsg.put("mobile", quickAgr.getMobile());
		requestMsg.put("bankCode", quickAgr.getBankCode());
		requestMsg.put("mercOrderNo", mercOrder.getMercOrderNo());
		requestMsg.put("openId", quickAgr.getBankCardNo());
		log.info("|快捷支付|商户订单处理|结束 响应：{}", JSON.toJSONString(responseMsg));
	}

}
