package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 商户表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class Merc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户号
     */
    private String mercId;

    /**
     * 商户名称
     */
    private String mercName;

    /**
     * 商户简称
     */
    private String mercAbbr;

    /**
     * 私钥/密钥
     */
    private String privateKey;

    /**
     * 状态 0:审核 1:正常 2:冻结
     */
    private Integer status;

    /**
     * 注册日期yyyy-MM-dd
     */
    private String regDate;

    /**
     * 注册时间HH:mm:ss
     */
    private String regTime;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
