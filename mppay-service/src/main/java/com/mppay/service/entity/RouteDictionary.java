package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 路由数据字典表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-04
 */
@Data
public class RouteDictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 路由编号
     */
    private String route;

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
     * 描述
     */
    private String description;

    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

}
