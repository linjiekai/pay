package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 用户操作基础信息表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
public class UserOper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户操作号
     */
    private String userOperNo;

    /**
     * 内部用户号
     */
    private String userNo;

    /**
     * 商城用户id
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别 0:未知 1:男 2:女
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 电子邮箱
     */
    private String email;

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
     * 状态 0:待审核 1:正常 2:冻结/黑名单
     */
    private Integer status;

    /**
     * 实名标识 0：未实名 1：弱实名 2：强实名
     */
    private Integer realed;

    /**
     * 用户登录密码
     */
    private String loginPwd;

    /**
     * 盐值
     */
    private String salt;

    /**
     * 登录密码错误次数
     */
    private Integer errPwdCount;

    /**
     * 登录密码错误日期yyyy-MM-dd
     */
    private String errPwdDate;

    /**
     * 商户号
     */
    private String mercId;

    /**
     * 最后登陆IP
     */
    private String lastLoginIp;

    /**
     * 最后一次登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 注册日期YYYY-MM-DD
     */
    private String regDate;

    /**
     * 注册时间HH:mm:ss
     */
    private String regTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getUserOperNo() {
        return userOperNo;
    }

    public void setUserOperNo(String userOperNo) {
        this.userOperNo = userOperNo;
    }
    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    public Integer getRealed() {
        return realed;
    }

    public void setRealed(Integer realed) {
        this.realed = realed;
    }
    public String getLoginPwd() {
        return loginPwd;
    }

    public void setLoginPwd(String loginPwd) {
        this.loginPwd = loginPwd;
    }
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
    public Integer getErrPwdCount() {
        return errPwdCount;
    }

    public void setErrPwdCount(Integer errPwdCount) {
        this.errPwdCount = errPwdCount;
    }
    public String getErrPwdDate() {
        return errPwdDate;
    }

    public void setErrPwdDate(String errPwdDate) {
        this.errPwdDate = errPwdDate;
    }
    public String getMercId() {
        return mercId;
    }

    public void setMercId(String mercId) {
        this.mercId = mercId;
    }
    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }
    public String getRegTime() {
        return regTime;
    }

    public void setRegTime(String regTime) {
        this.regTime = regTime;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "UserOper{" +
        "id=" + id +
        ", userOperNo=" + userOperNo +
        ", userNo=" + userNo +
        ", userId=" + userId +
        ", nickname=" + nickname +
        ", name=" + name +
        ", gender=" + gender +
        ", mobile=" + mobile +
        ", email=" + email +
        ", cardType=" + cardType +
        ", cardNo=" + cardNo +
        ", cardNoAbbr=" + cardNoAbbr +
        ", status=" + status +
        ", realed=" + realed +
        ", loginPwd=" + loginPwd +
        ", salt=" + salt +
        ", errPwdCount=" + errPwdCount +
        ", errPwdDate=" + errPwdDate +
        ", mercId=" + mercId +
        ", lastLoginIp=" + lastLoginIp +
        ", lastLoginTime=" + lastLoginTime +
        ", regDate=" + regDate +
        ", regTime=" + regTime +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        "}";
    }
}
