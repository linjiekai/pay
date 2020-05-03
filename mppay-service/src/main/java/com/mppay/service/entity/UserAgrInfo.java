package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 用户协议基础信息表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-11
 */
@Data
public class UserAgrInfo implements Serializable {

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
     * 协议编号
     */
    private String agrNo;

    /**
     * 个企标志 0：个人 1：企业
     */
    private Integer agrFlag;

    /**
     * 协议买卖属性 0：买家 1：卖家
     */
    private Integer buySalFlag;

    /**
     * 内部用户号
     */
    private String userNo;

    /**
     * 用户操作号
     */
    private String userOperNo;

    /**
     * 状态 0:待生效 1:正常 2:解约
     */
    private Integer status;

    /**
     * 路由编号
     */
    private String routeCode;

    /**
     * 外部商户号
     */
    private String outMercId;

    /**
     * 机构号
     */
    private String orgNo;

    /**
     * 终端号
     */
    private String terminalNo;

    /**
     * 协议日期YYYY-MM-DD
     */
    private String agrDate;

    /**
     * 协议时间HH:mm:ss
     */
    private String agrTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
    /**
     * 银行卡照正面图片url地址
     */
    private String bankCardImgFront;
    /**
     * 身份证正面
     */
    private String IdCardImgFront;

    /**
     * 身份证反面
     */
    private String IdCardImgBack;


}
