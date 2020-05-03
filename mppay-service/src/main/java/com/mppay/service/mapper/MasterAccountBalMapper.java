package com.mppay.service.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mppay.service.entity.MasterAccountBal;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 主账户余额表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-23
 */
public interface MasterAccountBalMapper extends BaseMapper<MasterAccountBal> {

	/**
	 * 添加余额
	 * @param masterAccountBal
	 * @return
	 */
	@Update("update master_account_bal set ac_bal=ac_bal+#{acBal} where ac_no=#{acNo}")
	public int addAcBal(MasterAccountBal masterAccountBal);

	/**
	 * 扣减余额
	 * @param masterAccountBal
	 * @return
	 */
	@Update("update master_account_bal set ac_bal=ac_bal-#{acBal} where ac_no=#{acNo} and ac_bal>=#{acBal}")
	public int subtractAcBal(MasterAccountBal masterAccountBal);
	
	/**
	 * 添加冻结余额
	 * @param masterAccountBal
	 * @return
	 */
	@Update("update master_account_bal set ac_bal=ac_bal-#{uavaBal}, uava_bal=uava_bal+#{uavaBal} where ac_no=#{acNo} and ac_bal>=#{uavaBal}")
	public Integer addUavaBal(MasterAccountBal masterAccountBal);

	/**
	 * 扣减冻结余额
	 * @param masterAccountBal
	 * @return
	 */
	@Update("update master_account_bal set uava_bal=uava_bal-#{uavaBal},withdr_bal=withdr_bal+#{uavaBal} where ac_no=#{acNo} and uava_bal>=#{uavaBal}")
	public Integer subtractUavaBal(MasterAccountBal masterAccountBal);

	/**
	 * 回退账户余额
	 * @param masterAccountBal
	 * @return
	 */
	@Update("update master_account_bal set ac_bal=ac_bal+#{uavaBal}, uava_bal=uava_bal-#{uavaBal} where ac_no=#{acNo} and uava_bal>=#{uavaBal}")
	public Integer backUavaBal(MasterAccountBal masterAccountBal);

	/**
	 * 添加保证金
	 * @param masterAccountBal
	 * @return
	 */
	@Update("update master_account_bal set sct_bal=sct_bal+#{sctBal} where ac_no=#{acNo}")
	public Integer addSctBal(MasterAccountBal masterAccountBal);
	
	/**
	 * 扣减保证金
	 * @param masterAccountBal
	 * @return
	 */
	@Update("update master_account_bal set sct_bal=sct_bal-#{sctBal} where ac_no=#{acNo} and sct_bal >= #{sctBal}")
	public Integer subtractSctBal(MasterAccountBal masterAccountBal);

	/**
	 * 批量查询: 根据用户id和商户id查询用户余额信息
	 *
	 * @param userIds
	 * @param mercId
	 * @return
	 */
	@Select(" <script> " +
			" SELECT o.merc_id, o.user_id, b.ac_bal, b.uava_bal, b.not_tx_ava_bal, b.sct_bal, b.withdr_bal, o.card_type, o.card_no FROM master_account_bal b ,user_oper o" +
			" WHERE b.user_no = o.user_no" +
			" <if test='userIds != null and userIds.size > 0' > AND o.user_id IN" +
			" <foreach item='item' index='index' collection='userIds' open='(' close=')' separator=','> " +
			"    #{item} " +
			" </foreach>" +
			" </if> " +
			" AND o.merc_id = #{mercId} " +
			" </script> ")
	List<Map<String,Object>> listByUserIdAndMercId(@Param("userIds") List<Long> userIds, @Param("mercId") String mercId);

}
