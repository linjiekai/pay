package com.mppay.service.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mppay.service.entity.CardBind;
import com.mppay.service.vo.CardBindVO;

/**
 * <p>
 * 绑卡表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-29
 */
public interface CardBindMapper extends BaseMapper<CardBind> {

	@Select("select b.bank_code,b.bank_name,b.logo,b.bank_abbr,b.bank_type,c.bind_date,c.bind_time,c.bank_no,c.bank_card_name,c.bank_card_no,b.bank_card_type,c.agr_no "
			+ "from (select a.*,r.bank_card_type from bank a, bank_route r where a.bank_code=r.bank_code and r.trade_code=#{tradeCode} and r.merc_id=#{mercId}) b left join (select * from card_bind where user_id=#{userId} and status=#{status} and merc_id=#{mercId}) c on b.bank_code=c.bank_code where b.status=#{status}"
			)
	List<CardBindVO> withdrBankList(Map<String, Object> params);

	@Select("<script> "
			+ "select b.bank_code,b.bank_name,b.logo,b.bank_abbr,b.bank_type,c.bind_date,c.bind_time,c.bank_no,c.bank_card_name,c.bank_card_no,c.bank_card_type,c.agr_no"
			+ " from bank b , bank_route r, card_bind c where b.bank_code=r.bank_code and b.bank_code=c.bank_code "
			+ " <if test='params.userId != null'> and c.user_id=#{params.userId} </if> "
			+ " <if test='params.userOperNo != null'> and c.user_oper_no=#{params.userOperNo} </if> "
			+ " <if test='params.mercId != null'> and c.merc_id=#{params.mercId} and r.merc_id=#{params.mercId} </if> "
			+ " <if test='params.tradeCode != null'> and r.trade_code=#{params.tradeCode} </if> "
			+ " <if test='params.status != null'> and b.status=#{params.status} and c.status=#{params.status} </if> "
			+ " <if test='params.bankType != null'> and b.bank_type=#{params.bankType} </if> "
			+ " <if test='params.bankCode != null and params.bankCode != \"\"'> and b.bank=#{params.bankCode} </if> "
			+ "</script> "
			)
	List<CardBindVO> cardBindList(@Param("params") Map<String, Object> params);

	/**
	 * 根据用户id查询,用户最近的绑卡信息
	 *
	 * @param status
	 * @param bankCardType
	 * @param mercId
	 * @param userIds
	 * @return
	 */
	@Select("<script> " +
			" SELECT * FROM card_bind " +
			" WHERE id IN( " +
			" 	(SELECT MAX(id) bind_id FROM card_bind WHERE 1 = 1 " +
			"		AND status = #{status} " +
			"		AND merc_id = #{mercId} " +
			"		AND bank_card_type = #{bankCardType}" +
			" 		<if test='userIds != null and userIds.size > 0' > " +
			"			AND user_id IN " +
			"			<foreach item='item' index='index' collection='userIds' open='(' close=')' separator=','>  " +
			"				#{item} " +
			"			</foreach> " +
			"		 </if> " +
			"		GROUP BY user_id)" +
			" ) " +
			"</script>")
	List<CardBind> lastBindByUserIds(@Param("status") Integer status,
									 @Param("bankCardType") String bankCardType,
									 @Param("mercId") String mercId,
									 @Param("userIds") List<Integer> userIds);

}


