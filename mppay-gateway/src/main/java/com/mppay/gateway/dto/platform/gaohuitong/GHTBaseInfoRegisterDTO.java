package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 商户基础信息 登记 DTO
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTBaseInfoRegisterDTO extends AbstractObject {

    /**
     * 操作类型 0：新增 1：修改。
     */
    @XmlElement
    private String handleType;
    /**
     * 商户名称
     */
    @XmlElement
    private String merchantName;
    /**
     * 商户简称
     */
    @XmlElement
    private String shortName;
    /**
     * 商户城市
     */
    @XmlElement
    private String city;
    /**
     * 商户地址
     */
    @XmlElement
    private String merchantAddress;
    /**
     * 客服电话
     */
    @XmlElement
    private String servicePhone;
    /**
     * 商户类型 00-公司商户；01-个体商户
     */
    @XmlElement
    private String merchantType;
    /**
     * 经营类目代码
     */
    @XmlElement
    private String category;
    /**
     * 法人姓名
     */
    @XmlElement
    private String corpmanName;
    /**
     * 法人身份证
     */
    @XmlElement
    private String corpmanId;
    /**
     * 法人联系手机
     */
    @XmlElement
    private String corpmanMobile;
    /**
     * 银行代码
     */
    @XmlElement
    private String bankCode;
    /**
     * 开户行全称
     */
    @XmlElement
    private String bankName;
    /**
     * 开户行账号
     */
    @XmlElement
    private String bankaccountNo;
    /**
     *开户户名
     */
    @XmlElement
    private String bankaccountName;
    /**
     * 自动提现  0：不自动提现 1：自动提现
     */
    @XmlElement
    private String autoCus;
    /**
     * 备注
     */
    @XmlElement
    private String remark;
    /**
     * 子商户号
     */
    @XmlElement
    private String subMerchantNo;
    /**
     * 上级商户
     * 号（连锁
     * 总店商户
     * 号）  版本号2.0.0不适用
     */
    @XmlElement
    private String inviteMerNo;
    /**
     * 微信APPID
     */
    @XmlElement
    private String appid;
    /**
     * 支付宝PID
     */
    @XmlElement
    private String pid;
    /**
     * 银行卡是否设定
     * 为结算卡：0或null-不设定；1-设定
     */
    @XmlElement
    private String settingSettCard;
    /**
     *  账户属性 当settingSettCard上传时，此参数必
     * 传：账户属性:0-私人;1-公司
     */
    @XmlElement
    private Integer bankaccProp;
    /**
     * 办卡证件类型
     */
    @XmlElement
    private Integer certCode;
    /**
     * 银行卡类 型
     */
    @XmlElement
    private Integer bankaccountType;
    /**
     * 子商户号
     */
    @XmlElement
    private String merchantId;




    /**
     * 路由编号，无用字段
     */
    private String routeCode;
}
