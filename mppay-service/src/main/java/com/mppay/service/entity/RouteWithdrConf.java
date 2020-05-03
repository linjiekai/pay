package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 路由配置表
 * </p>
 *
 * @author chenfeihang
 * @since 2020-04-15
 */
public class RouteWithdrConf implements Serializable {

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
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getMercId() {
        return mercId;
    }

    public void setMercId(String mercId) {
        this.mercId = mercId;
    }
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }
    public String getBankMercId() {
        return bankMercId;
    }

    public void setBankMercId(String bankMercId) {
        this.bankMercId = bankMercId;
    }
    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }
    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }
    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "RouteWithdrConf{" +
        "id=" + id +
        ", mercId=" + mercId +
        ", platform=" + platform +
        ", routeCode=" + routeCode +
        ", appId=" + appId +
        ", tradeType=" + tradeType +
        ", bankMercId=" + bankMercId +
        ", keyPath=" + keyPath +
        ", publicKeyPath=" + publicKeyPath +
        ", privateKey=" + privateKey +
        ", publicKey=" + publicKey +
        ", updateTime=" + updateTime +
        ", createTime=" + createTime +
        "}";
    }
}
