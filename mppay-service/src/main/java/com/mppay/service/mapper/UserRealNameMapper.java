package com.mppay.service.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mppay.service.entity.UserRealName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户实名认证信息表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-19
 */
public interface UserRealNameMapper extends BaseMapper<UserRealName> {

    /**
     * 根据姓名,证件类型,证件号查询
     * @param userRealNames
     * @return
     */
    @Select("<script> SELECT id,name,gender,card_type,card_no,card_no_abbr,status,realed,birthday,province,city,country,address," +
            "  address_code,last_code,eff_start_date,eff_end_date,real_date,real_time,img_front,img_back,update_time,create_time " +
            " FROM user_real_name" +
            " WHERE 1=1 " +
            " <if test='list != null and list.size > 0' > AND " +
            " <foreach item='item' index='index' collection='list' open='(' close=')' separator='or'> " +
            "    ( name = #{item.name} AND card_type = #{item.cardType} AND card_no = #{item.cardNo} ) " +
            " </foreach> " +
            " </if>" +
            " </script> ")
    List<UserRealName> listByNameAndCarcNoAndCardType(@Param("list") List<UserRealName> userRealNames);

}
