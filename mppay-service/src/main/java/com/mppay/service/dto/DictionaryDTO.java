package com.mppay.service.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 数据字典表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-23
 */
@Data
public class DictionaryDTO extends AdminBaseRequestDTO{

    private Long id;
    
    private String mercId;

    /**
     * 类别
     */
    private String category;

    /**
     * 名称
     */
    private String name;

    /**
     * 字符串值
     */
    private String strVal;

    /**
     * 长/整形值
     */
    private Long longVal;
    
    private Integer visible;

    /**
     * 描述
     */
    private String description;


}
