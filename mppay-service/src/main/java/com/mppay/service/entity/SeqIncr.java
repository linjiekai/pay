package com.mppay.service.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;

/**
 * <p>
 * 序列号表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-30
 */
@Data
public class SeqIncr implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 序列名称
     */
    private String name;

    /**
     * 当前序列值
     */
    private Long currentValue;

    /**
     * 每次增长值
     */
    private Integer increment;

    private String remark;
    
    /**
     * 下一个增长的值
     */
    @TableField(exist = false)
    private Long nextValue;

}
