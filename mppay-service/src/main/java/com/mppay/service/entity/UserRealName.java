package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 用户实名认证信息表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
public class UserRealName implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 姓名
     */
    private String name;

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
     * 状态 0:注销 1:实名 2：冻结
     */
    private Integer status;

    /**
     * 实名标识 0：未实名 1：弱实名 2：强实名
     */
    private Integer realed;

    /**
     * 生日yyyy-MM-dd
     */
    private String birthday;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 县/区
     */
    private String country;

    /**
     * 身份证所在地
     */
    private String address;

    /**
     * 地区代码
     */
    private String addressCode;

    /**
     * 身份证校验码
     */
    private String lastCode;

    /**
     * 有效开始日期yyyy-MM-dd
     */
    private String effStartDate;

    /**
     * 有效结束日期yyyy-MM-dd
     */
    private String effEndDate;

    /**
     * 实名日期yyyy-MM-dd
     */
    private String realDate;

    /**
     * 实名时间HH:mm:ss
     */
    private String realTime;

    /**
     * 身份证正面
     */
    private String imgFront;

    /**
     * 身份证反面
     */
    private String imgBack;

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
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getAddressCode() {
        return addressCode;
    }

    public void setAddressCode(String addressCode) {
        this.addressCode = addressCode;
    }
    public String getLastCode() {
        return lastCode;
    }

    public void setLastCode(String lastCode) {
        this.lastCode = lastCode;
    }
    public String getEffStartDate() {
        return effStartDate;
    }

    public void setEffStartDate(String effStartDate) {
        this.effStartDate = effStartDate;
    }
    public String getEffEndDate() {
        return effEndDate;
    }

    public void setEffEndDate(String effEndDate) {
        this.effEndDate = effEndDate;
    }
    public String getRealDate() {
        return realDate;
    }

    public void setRealDate(String realDate) {
        this.realDate = realDate;
    }
    public String getRealTime() {
        return realTime;
    }

    public void setRealTime(String realTime) {
        this.realTime = realTime;
    }
    public String getImgFront() {
        return imgFront;
    }

    public void setImgFront(String imgFront) {
        this.imgFront = imgFront;
    }
    public String getImgBack() {
        return imgBack;
    }

    public void setImgBack(String imgBack) {
        this.imgBack = imgBack;
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
        return "UserRealName{" +
        "id=" + id +
        ", name=" + name +
        ", gender=" + gender +
        ", cardType=" + cardType +
        ", cardNo=" + cardNo +
        ", cardNoAbbr=" + cardNoAbbr +
        ", status=" + status +
        ", realed=" + realed +
        ", birthday=" + birthday +
        ", province=" + province +
        ", city=" + city +
        ", country=" + country +
        ", address=" + address +
        ", addressCode=" + addressCode +
        ", lastCode=" + lastCode +
        ", effStartDate=" + effStartDate +
        ", effEndDate=" + effEndDate +
        ", realDate=" + realDate +
        ", realTime=" + realTime +
        ", imgFront=" + imgFront +
        ", imgBack=" + imgBack +
        ", updateTime=" + updateTime +
        ", createTime=" + createTime +
        "}";
    }
}
