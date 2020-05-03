package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 快捷签约协议表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-06
 */
public class QuickAgr implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 快捷签约协议号
     */
    private String agrNo;

    /**
     * 签约类型 01:快捷
     */
    private String agrType;

    /**
     * 内部用户号
     */
    private String userNo;

    /**
     * 用户操作号
     */
    private String userOperNo;

    /**
     * 商城用户号
     */
    private Long userId;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 银行绑定协议号
     */
    private String bindAgrNo;

    /**
     * 卡状态 1：绑定 2：解绑 3：冻结
     */
    private Integer status;

    /**
     * 快捷路由编号
     */
    private String routeCode;

    /**
     * 银行预留手机号
     */
    private String mobile;

    /**
     * 银行预留手机号简称
     */
    private String mobileAbbr;

    /**
     * 性别 0:未知 1:男 2:女
     */
    private Integer gender;

    /**
     * 证件类型 1:身份证 2:护照  3:军官证  4:回乡证  5:台胞证  6:港澳通行证  7:国际海员证  8:外国人永久居住证 9:其它
     */
    private Integer cardType;

    /**
     * 证件号
     */
    private String cardNo;
    
    /**
     * 证件号简称
     */
    private String cardNoAbbr;

    /**
     * 绑定日期 YYYY-MM-DD
     */
    private String bindDate;

    /**
     * 绑定时间 HH:mm:ss
     */
    private String bindTime;

    /**
     * 银行编号：支付宝：ALIPAY 微信：WEIXIN 名品猫：MPPAY
     */
    private String bankCode;

    /**
     * 银行简称
     */
    private String bankAbbr;

    /**
     * 银行卡姓名
     */
    private String bankCardName;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 银行卡号简称
     */
    private String bankCardNoAbbr;

    /**
     * 银行卡类型 01:借记卡;02:贷记卡
     */
    private String bankCardType;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
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
    public String getAgrNo() {
        return agrNo;
    }

    public void setAgrNo(String agrNo) {
        this.agrNo = agrNo;
    }
    public String getAgrType() {
        return agrType;
    }

    public void setAgrType(String agrType) {
        this.agrType = agrType;
    }
    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }
    public String getUserOperNo() {
        return userOperNo;
    }

    public void setUserOperNo(String userOperNo) {
        this.userOperNo = userOperNo;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getMercId() {
        return mercId;
    }

    public void setMercId(String mercId) {
        this.mercId = mercId;
    }
    public String getBindAgrNo() {
        return bindAgrNo;
    }

    public void setBindAgrNo(String bindAgrNo) {
        this.bindAgrNo = bindAgrNo;
    }
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    public String getMobileAbbr() {
        return mobileAbbr;
    }

    public void setMobileAbbr(String mobileAbbr) {
        this.mobileAbbr = mobileAbbr;
    }
    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }
    public Integer getCardType() {
        return cardType;
    }

    public void setCardType(Integer cardType) {
        this.cardType = cardType;
    }
    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
    
    public String getCardNoAbbr() {
		return cardNoAbbr;
	}

	public void setCardNoAbbr(String cardNoAbbr) {
		this.cardNoAbbr = cardNoAbbr;
	}

	public String getBindDate() {
        return bindDate;
    }

    public void setBindDate(String bindDate) {
        this.bindDate = bindDate;
    }
    public String getBindTime() {
        return bindTime;
    }

    public void setBindTime(String bindTime) {
        this.bindTime = bindTime;
    }
    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    public String getBankAbbr() {
        return bankAbbr;
    }

    public void setBankAbbr(String bankAbbr) {
        this.bankAbbr = bankAbbr;
    }
    public String getBankCardName() {
        return bankCardName;
    }

    public void setBankCardName(String bankCardName) {
        this.bankCardName = bankCardName;
    }
    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }
    public String getBankCardNoAbbr() {
        return bankCardNoAbbr;
    }

    public void setBankCardNoAbbr(String bankCardNoAbbr) {
        this.bankCardNoAbbr = bankCardNoAbbr;
    }
    public String getBankCardType() {
        return bankCardType;
    }

    public void setBankCardType(String bankCardType) {
        this.bankCardType = bankCardType;
    }
    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
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
        return "QuickAgr{" +
        "id=" + id +
        ", agrNo=" + agrNo +
        ", agrType=" + agrType +
        ", userNo=" + userNo +
        ", userOperNo=" + userOperNo +
        ", userId=" + userId +
        ", mercId=" + mercId +
        ", bindAgrNo=" + bindAgrNo +
        ", status=" + status +
        ", routeCode=" + routeCode +
        ", mobile=" + mobile +
        ", mobileAbbr=" + mobileAbbr +
        ", gender=" + gender +
        ", cardType=" + cardType +
        ", cardNo=" + cardNo +
        ", bindDate=" + bindDate +
        ", bindTime=" + bindTime +
        ", bankCode=" + bankCode +
        ", bankAbbr=" + bankAbbr +
        ", bankCardName=" + bankCardName +
        ", bankCardNo=" + bankCardNo +
        ", bankCardNoAbbr=" + bankCardNoAbbr +
        ", bankCardType=" + bankCardType +
        ", clientIp=" + clientIp +
        ", updateTime=" + updateTime +
        ", createTime=" + createTime +
        "}";
    }
}
