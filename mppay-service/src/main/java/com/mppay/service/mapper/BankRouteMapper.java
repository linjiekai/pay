package com.mppay.service.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mppay.service.entity.BankRoute;
import com.mppay.service.vo.BankRouteVO;

/**
 * <p>
 * 银行路由关联表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
public interface BankRouteMapper extends BaseMapper<BankRoute> {

	@Select("<script> select b.bank_code, b.bank_name, b.bank_abbr, b.bank_type, b.logo, r.trade_code, r.bank_card_type"
    		+ " from bank b,bank_route r where b.bank_code=r.bank_code "
    		+ " <if test='params.tradeCode != null'> and r.trade_code = #{params.tradeCode} </if> "
    		+ " <if test='params.status != null'> and b.status = #{params.status} </if> "
    		+ " <if test='params.bankType != null'> and b.bank_type = #{params.bankType} </if> "
    		+ " <if test='params.bankCardType != null'> and r.bank_card_type = #{params.bankCardType} </if> "
    		+ " <if test='params.mercId != null'> and r.merc_id = #{params.mercId} </if> "
    		+ " order by b.bank_type asc,r.indexs desc "
    		+ "</script> " 
    		)
	List<BankRouteVO> findBankRoute(@Param("params") Map<String, Object> params);

}
