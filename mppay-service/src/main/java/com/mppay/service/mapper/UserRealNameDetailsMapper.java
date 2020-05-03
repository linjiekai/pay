package com.mppay.service.mapper;

import com.mppay.service.entity.UserRealNameDetails;

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 * 用户实名认证信息明细表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-14
 */
public interface UserRealNameDetailsMapper extends BaseMapper<UserRealNameDetails> {

	@Select("<script>" +
            " select user_id,name,gender,card_type,card_no,card_no_abbr,status,birthday,address_code,last_code,real_date,real_time,real_source,sys_cnl,img_front,img_back from user_real_name_details  where 1 = 1 " +
            "<if test='params.mercId != null and params.mercId != \"\"'> and merc_id = #{params.mercId} </if>" +
            "<if test='params.userId != null and params.userId != \"\"'> and user_id = #{params.userId} </if>" +
            "<if test='params.cardNo != null and params.cardNo != \"\"'> and card_no = #{params.cardNo} </if>" +
            "<if test='params.name != null and params.name != \"\"'> and name like CONCAT('%',#{params.name},'%') </if>" +
            " order by id desc </script>")
	IPage<UserRealNameDetails> page(Page<UserRealNameDetails> ipage, @Param("params")Map<String, Object> params);

}
