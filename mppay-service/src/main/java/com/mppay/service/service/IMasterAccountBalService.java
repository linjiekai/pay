package com.mppay.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mppay.service.entity.MasterAccountBal;
import com.mppay.service.entity.TradeOrder;
import com.mppay.service.entity.WithdrOrder;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 主账户余额表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-23
 */
public interface IMasterAccountBalService extends IService<MasterAccountBal> {

	/**
	 * 添加账号余额
	 * @param tradeOrder
	 * @return
	 */
	public boolean addAcBal(TradeOrder tradeOrder);
	
	/**
	 * 扣减账号余额
	 * @param tradeOrder
	 * @return
	 */
	public boolean subtractAcBal(TradeOrder tradeOrder);

	/**
	 * 添加提现不可用余额
	 * @param masterAccountBal
	 * @return
	 */
	public boolean addWithdrUavaBal(WithdrOrder withdrOrder);
	
	/**
	 * 返还提现不可用余额
	 * @param masterAccountBal
	 * @return
	 */
	public boolean backWithdrUavaBal(WithdrOrder withdrOrder);
	
	/**
	 * 减少提现不可用余额
	 * @param masterAccountBal
	 * @return
	 */
	public boolean subtractWithdrUavaBal(WithdrOrder withdrOrder);
	
	/**
	 * 添加保证金余额
	 * @param tradeOrder
	 * @return
	 */
	public boolean addSctBal(TradeOrder tradeOrder);
	
	/**
	 * 扣减账号保证金余额
	 * @param tradeOrder
	 * @return
	 */
	public boolean subtractSctBal(TradeOrder tradeOrder);

	/**
	 * 根据用户id和商户id查询
	 *
	 * @param userIds
	 * @param mercId
	 * @return
	 */
	List<Map<String,Object>> listByUserIdAndMercId(List<Long> userIds, String mercId);

}
