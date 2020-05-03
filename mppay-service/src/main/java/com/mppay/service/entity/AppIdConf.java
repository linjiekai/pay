package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * appId配置表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-12-09
 */
@Data
public class AppIdConf implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL;合伙人：PTMALL
     */
    private String platform;

    /**
     * 银行编号
     */
    private String bankCode;

    /**
     * 微信或支付宝应用APPID
     */
    private String appId;

    /**
     * 交易类型 JSAPI：公众号支付,APP：app支付,NATIVE：扫码支付,MICROPAY：刷卡支付
     */
    private String tradeType;

    /**
     * 密钥
     */
    private String secrect;

    /**
     * 操作类型
     */
    private Integer operType;
    
    /**
     * 区域 1：境内 2：境外
     */
    private Integer region;

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
