package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 路由配置表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class RouteConf implements Serializable {

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
     * 路由编号
     */
    private String routeCode;

    /**
     * 微信或支付宝应用APPID
     */
    private String appId;

    /**
     * 交易类型 JSAPI：公众号支付,APP：app支付,NATIVE：扫码支付,MICROPAY：刷卡支付
     */
    private String tradeType;

    /**
     * 银行分配商户号
     */
    private String bankMercId;

    /**
     * 证书路径
     */
    private String keyPath;
    
    /**
     * 公钥路径
     */
    private String publicKeyPath;

    /**
     * 银行分配私钥/密钥
     */
    private String privateKey;

    /**
     * 银行分配公钥
     */
    private String publicKey;

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
