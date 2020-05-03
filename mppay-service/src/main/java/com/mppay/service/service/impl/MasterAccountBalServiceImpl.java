package com.mppay.service.service.impl;

import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.service.entity.MasterAccountBal;
import com.mppay.service.entity.MasterAccountBalDetails;
import com.mppay.service.entity.TradeOrder;
import com.mppay.service.entity.WithdrOrder;
import com.mppay.service.mapper.MasterAccountBalMapper;
import com.mppay.service.service.IMasterAccountBalDetailsService;
import com.mppay.service.service.IMasterAccountBalService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 主账户余额表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-23
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class MasterAccountBalServiceImpl extends ServiceImpl<MasterAccountBalMapper, MasterAccountBal> implements IMasterAccountBalService {

	@Autowired
	private IMasterAccountBalDetailsService masterAccountBalDetailsService;
	
	@Override
	public boolean addAcBal(TradeOrder tradeOrder) {
		MasterAccountBal bal = this.getOne(new QueryWrapper<MasterAccountBal>().eq("user_no", tradeOrder.getUserNo()));
		
		MasterAccountBalDetails details = new MasterAccountBalDetails();
		details.setMercId(tradeOrder.getMercId());
		details.setAcNo(bal.getAcNo());
		details.setUserNo(bal.getUserNo());
		details.setAcType(bal.getAcType());
		details.setAcDate(DateTimeUtil.date10());
		details.setTradeNo(tradeOrder.getTradeNo());
		details.setTradeCode(tradeOrder.getTradeCode());
		details.setBusiType(tradeOrder.getBusiType());
		details.setPrice(tradeOrder.getPrice());
		details.setAcBal(bal.getAcBal());
		details.setUavaBal(bal.getUavaBal());
		details.setNotTxBal(bal.getNotTxAvaBal());
		details.setSctBal(bal.getSctBal());
		details.setTradeDate(tradeOrder.getTradeDate());
		details.setTradeTime(tradeOrder.getTradeTime());
		details.setSysDate(DateTimeUtil.date10());
		details.setSysTime(DateTimeUtil.time8());
		
		masterAccountBalDetailsService.save(details);
		
		MasterAccountBal masterAccountBal = new MasterAccountBal();
		masterAccountBal.setAcNo(bal.getAcNo());
		masterAccountBal.setUserNo(bal.getUserNo());
		masterAccountBal.setAcBal(tradeOrder.getPrice());
		
		
		log.info("添加账户余额 AcNo:{} UserNo:{} TradeNo:{} Price:{}", bal.getAcNo(), bal.getUserNo(), tradeOrder.getTradeNo(), tradeOrder.getPrice());
		
		return SqlHelper.retBool(baseMapper.addAcBal(masterAccountBal));
	}

	@Override
	public boolean subtractAcBal(TradeOrder tradeOrder) {
		MasterAccountBal bal = this.getOne(new QueryWrapper<MasterAccountBal>().eq("user_no", tradeOrder.getUserNo()));
		
		MasterAccountBalDetails details = new MasterAccountBalDetails();
		details.setMercId(tradeOrder.getMercId());
		details.setAcNo(bal.getAcNo());
		details.setUserNo(bal.getUserNo());
		details.setAcType(bal.getAcType());
		details.setAcDate(DateTimeUtil.date10());
		details.setTradeNo(tradeOrder.getTradeNo());
		details.setTradeCode(tradeOrder.getTradeCode());
		details.setBusiType(tradeOrder.getBusiType());
		details.setPrice(tradeOrder.getPrice());
		details.setAcBal(bal.getAcBal());
		details.setUavaBal(bal.getUavaBal());
		details.setNotTxBal(bal.getNotTxAvaBal());
		details.setSctBal(bal.getSctBal());
		details.setTradeDate(tradeOrder.getTradeDate());
		details.setTradeTime(tradeOrder.getTradeTime());
		details.setSysDate(DateTimeUtil.date10());
		details.setSysTime(DateTimeUtil.time8());
		
		masterAccountBalDetailsService.save(details);
		
		MasterAccountBal masterAccountBal = new MasterAccountBal();
		masterAccountBal.setAcNo(bal.getAcNo());
		masterAccountBal.setUserNo(bal.getUserNo());
		masterAccountBal.setAcBal(tradeOrder.getPrice());
		
		log.info("扣减账户余额 AcNo:{} UserNo:{} TradeNo:{} Price:{}", bal.getAcNo(), bal.getUserNo(), tradeOrder.getTradeNo(), tradeOrder.getPrice());
		
		return SqlHelper.retBool(baseMapper.subtractAcBal(masterAccountBal));
	}
	
	@Override
	public boolean addWithdrUavaBal(WithdrOrder withdrOrder) {
		MasterAccountBal bal = this.getOne(new QueryWrapper<MasterAccountBal>().eq("user_no", withdrOrder.getUserNo()));

		if (null == bal) {
			log.error(ApplicationYmlUtil.get("11302") + ", user_no=" + withdrOrder.getUserNo());
			throw new BusiException(11302);
		}
		
		if (bal.getAcBal().compareTo(withdrOrder.getPrice()) < 0) {
			log.error(ApplicationYmlUtil.get("11305") + ", user_no=" + withdrOrder.getUserNo()
			+ ", price=" + withdrOrder.getPrice() + ", withdrOrderNo=" + withdrOrder.getWithdrOrderNo() + ", acBal=" + bal.getAcBal());
			throw new BusiException(11305);
		}
		
		MasterAccountBal masterAccountBal = new MasterAccountBal();
		masterAccountBal.setAcNo(bal.getAcNo());
		masterAccountBal.setUserNo(bal.getUserNo());
		masterAccountBal.setUavaBal(withdrOrder.getPrice());
		
		log.info("添加提现不可用余额 AcNo:{} UserNo:{} WithdrOrderNo:{} Price:{}", bal.getAcNo(), bal.getUserNo(), withdrOrder.getWithdrOrderNo(), withdrOrder.getPrice());
		
		return SqlHelper.retBool(baseMapper.addUavaBal(masterAccountBal));
	}
	
	@Override
	public boolean backWithdrUavaBal(WithdrOrder withdrOrder) {
		MasterAccountBal bal = this.getOne(new QueryWrapper<MasterAccountBal>().eq("user_no", withdrOrder.getUserNo()));

		if (null == bal) {
			log.error(ApplicationYmlUtil.get("11302") + ", user_no=" + withdrOrder.getUserNo());
			throw new BusiException(11302);
		}
		
		if (bal.getUavaBal().compareTo(withdrOrder.getPrice()) < 0) {
			log.error(ApplicationYmlUtil.get("11310") + ", user_no=" + withdrOrder.getUserNo()
			+ ", price=" + withdrOrder.getPrice() + ", withdrOrderNo=" + withdrOrder.getWithdrOrderNo() + ", acBal=" + bal.getAcBal());
			throw new BusiException(11305);
		}
		
		MasterAccountBal masterAccountBal = new MasterAccountBal();
		masterAccountBal.setAcNo(bal.getAcNo());
		masterAccountBal.setUserNo(bal.getUserNo());
		masterAccountBal.setUavaBal(withdrOrder.getPrice());
		
		log.info("回退账户余额 AcNo:{} UserNo:{} WithdrOrderNo:{} Price:{}", bal.getAcNo(), bal.getUserNo(), withdrOrder.getWithdrOrderNo(), withdrOrder.getPrice());
		
		return SqlHelper.retBool(baseMapper.backUavaBal(masterAccountBal));
	}

	@Override
	public boolean subtractWithdrUavaBal(WithdrOrder withdrOrder) {
		MasterAccountBal bal = this.getOne(new QueryWrapper<MasterAccountBal>().eq("user_no", withdrOrder.getUserNo()));
		
		if (null == bal) {
			log.error(ApplicationYmlUtil.get("11302") + ", user_no=" + withdrOrder.getUserNo());
			throw new BusiException(11302);
		}
		
		if (bal.getUavaBal().compareTo(withdrOrder.getPrice()) < 0) {
			log.error(ApplicationYmlUtil.get("11305") + ", user_no=" + withdrOrder.getUserNo()
			+ ", price=" + withdrOrder.getPrice() + ", withdrOrderNo=" + withdrOrder.getWithdrOrderNo() + ", uavaBal=" + bal.getUavaBal());
			throw new BusiException(11305);
		}
		
		MasterAccountBal masterAccountBal = new MasterAccountBal();
		masterAccountBal.setAcNo(bal.getAcNo());
		masterAccountBal.setUserNo(bal.getUserNo());
		masterAccountBal.setUavaBal(withdrOrder.getPrice());
		
		MasterAccountBalDetails details = new MasterAccountBalDetails();
		details.setMercId(withdrOrder.getMercId());
		details.setAcNo(bal.getAcNo());
		details.setUserNo(bal.getUserNo());
		details.setAcType(bal.getAcType());
		details.setAcDate(DateTimeUtil.date10());
		details.setTradeNo(withdrOrder.getWithdrOrderNo());
		details.setTradeCode(withdrOrder.getTradeCode());
		details.setBusiType(withdrOrder.getBusiType());
		details.setPrice(withdrOrder.getPrice());
		details.setAcBal(bal.getAcBal());
		details.setUavaBal(bal.getUavaBal());
		details.setNotTxBal(bal.getNotTxAvaBal());
		details.setSctBal(bal.getSctBal());
		details.setTradeDate(withdrOrder.getBankWithdrDate());
		details.setTradeTime(withdrOrder.getBankWithdrTime());
		details.setSysDate(DateTimeUtil.date10());
		details.setSysTime(DateTimeUtil.time8());
		
		masterAccountBalDetailsService.save(details);
		
		log.info("扣减冻结余额 AcNo:{} UserNo:{} WithdrOrderNo:{} Price:{}", bal.getAcNo(), bal.getUserNo(), withdrOrder.getWithdrOrderNo(), withdrOrder.getPrice());

		return SqlHelper.retBool(baseMapper.subtractUavaBal(masterAccountBal));
	}

	@Override
	public boolean addSctBal(TradeOrder tradeOrder) {
		MasterAccountBal bal = this.getOne(new QueryWrapper<MasterAccountBal>().eq("user_no", tradeOrder.getUserNo()));
		
		MasterAccountBalDetails details = new MasterAccountBalDetails();
		details.setMercId(tradeOrder.getMercId());
		details.setAcNo(bal.getAcNo());
		details.setUserNo(bal.getUserNo());
		details.setAcType(bal.getAcType());
		details.setAcDate(DateTimeUtil.date10());
		details.setTradeNo(tradeOrder.getTradeNo());
		details.setTradeCode(tradeOrder.getTradeCode());
		details.setBusiType(tradeOrder.getBusiType());
		details.setPrice(tradeOrder.getPrice());
		details.setAcBal(bal.getAcBal());
		details.setUavaBal(bal.getUavaBal());
		details.setNotTxBal(bal.getNotTxAvaBal());
		details.setSctBal(bal.getSctBal());
		details.setTradeDate(tradeOrder.getTradeDate());
		details.setTradeTime(tradeOrder.getTradeTime());
		details.setSysDate(DateTimeUtil.date10());
		details.setSysTime(DateTimeUtil.time8());
		
		masterAccountBalDetailsService.save(details);
		
		MasterAccountBal masterAccountBal = new MasterAccountBal();
		masterAccountBal.setAcNo(bal.getAcNo());
		masterAccountBal.setUserNo(bal.getUserNo());
		masterAccountBal.setSctBal(tradeOrder.getPrice());
		
		log.info("添加保证金 AcNo:{} UserNo:{} TradeNo:{} Price:{}", bal.getAcNo(), bal.getUserNo(), tradeOrder.getTradeNo(), tradeOrder.getPrice());

		return SqlHelper.retBool(baseMapper.addSctBal(masterAccountBal));
	}

	@Override
	public boolean subtractSctBal(TradeOrder tradeOrder) {
		MasterAccountBal bal = this.getOne(new QueryWrapper<MasterAccountBal>().eq("user_no", tradeOrder.getUserNo()));
		
		if (bal.getSctBal().compareTo(tradeOrder.getPrice()) < 0) {
			log.error(ApplicationYmlUtil.get("11305") + ", user_no=" + tradeOrder.getUserNo()
			+ ", price=" + tradeOrder.getPrice() + ", tradeNo=" + tradeOrder.getTradeNo() + ", sctBal=" + bal.getSctBal());
			throw new BusiException(11305);
		}
		
		MasterAccountBalDetails details = new MasterAccountBalDetails();
		details.setMercId(tradeOrder.getMercId());
		details.setAcNo(bal.getAcNo());
		details.setUserNo(bal.getUserNo());
		details.setAcType(bal.getAcType());
		details.setAcDate(DateTimeUtil.date10());
		details.setTradeNo(tradeOrder.getTradeNo());
		details.setTradeCode(tradeOrder.getTradeCode());
		details.setBusiType(tradeOrder.getBusiType());
		details.setPrice(tradeOrder.getPrice());
		details.setAcBal(bal.getAcBal());
		details.setUavaBal(bal.getUavaBal());
		details.setNotTxBal(bal.getNotTxAvaBal());
		details.setSctBal(bal.getSctBal());
		details.setTradeDate(tradeOrder.getTradeDate());
		details.setTradeTime(tradeOrder.getTradeTime());
		details.setSysDate(DateTimeUtil.date10());
		details.setSysTime(DateTimeUtil.time8());
		
		masterAccountBalDetailsService.save(details);
		
		MasterAccountBal masterAccountBal = new MasterAccountBal();
		masterAccountBal.setAcNo(bal.getAcNo());
		masterAccountBal.setUserNo(bal.getUserNo());
		masterAccountBal.setSctBal(tradeOrder.getPrice());
		
		log.info("扣减保证金 AcNo:{} UserNo:{} TradeNo:{} Price:{}", bal.getAcNo(), bal.getUserNo(), tradeOrder.getTradeNo(), tradeOrder.getPrice());
		
		return SqlHelper.retBool(baseMapper.subtractSctBal(masterAccountBal));
	}

	/**
	 * 根据用户id和商户id查询
	 *
	 * @param userIds
	 * @param mercId
	 * @return
	 */
	@Override
	public List<Map<String, Object>> listByUserIdAndMercId(List<Long> userIds, String mercId) {
		return baseMapper.listByUserIdAndMercId(userIds, mercId);
	}

}
