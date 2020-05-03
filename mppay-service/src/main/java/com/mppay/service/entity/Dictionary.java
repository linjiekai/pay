package com.mppay.service.entity;

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
public class Dictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户号
     */
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

    /**
     * 是否展示 [0:否, 1:是]
     */
    private Integer visible;

    /**
     * 描述
     */
    private String description;

    /**
     * 操作人id
     */
    private Long adminId;

    /**
     * 操作人
     */
    private String operator;

    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;


}
