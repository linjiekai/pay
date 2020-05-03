package com.mppay.service.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mppay.service.dto.DictionaryDTO;
import com.mppay.service.entity.Dictionary;

/**
 * <p>
 * 数据字典表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-23
 */
public interface DictionaryMapper extends BaseMapper<Dictionary> {

	@Select(" <script> " +
			" SELECT * from dictionary" +
			" WHERE 1=1" +
			" <if test='ew.mercId != null and ew.mercId != \"\"' > and merc_id = #{ew.mercId} </if>" +
			" <if test='ew.category != null' > and category = #{ew.category} </if>" +
			" <if test='ew.visible != null' > and visible = #{ew.visible} </if>" +
			" </script> ")
	List<Dictionary> page(Page<Dictionary> page, @Param("ew") DictionaryDTO dictionaryDTO);

}
