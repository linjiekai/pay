package com.mppay.service.mapper;

import com.mppay.service.entity.QuickAgr;
import com.mppay.service.vo.QuickAgrBankVO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 快捷签约协议表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-06
 */
public interface QuickAgrMapper extends BaseMapper<QuickAgr> {

	@Select("<script> select b.bank_code, b.bank_name, b.bank_abbr, b.bank_type, b.logo, r.agr_no,r.bank_card_type,r.mobile,r.bank_card_no "
    		+ " from bank b,quick_agr r where b.bank_code=r.bank_code "
    		+ " <if test='params.userOperNo != null'> and r.user_oper_no = #{params.userOperNo} </if> "
    		+ " <if test='params.status != null'> and r.status = #{params.status} </if> "
			+ " <if test='params.bankCardTypes != null'> and r.bank_card_type in <foreach collection=\"params.bankCardTypes\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
    		+ " order by r.id desc "
    		+ "</script> " 
    		)
	List<QuickAgrBankVO> findQuickAgrBank(@Param("params") Map<String, Object> params);

}
