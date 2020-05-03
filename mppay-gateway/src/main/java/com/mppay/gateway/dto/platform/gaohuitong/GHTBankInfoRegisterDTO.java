package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 商户银行卡信息登记DTO [提现绑卡DTO]
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTBankInfoRegisterDTO extends AbstractObject {

    /**
     * 子商户编码
     * （商户基础信息登记接口返回的:merchantId）
     */
    @XmlElement
    private String merchantId;
    /**
     * 持卡人手机号
     * （如果是实体商户,则该参数必传。）
     */
    @XmlElement
    private String mobileNo;
    /**
     * 持卡人手机号
     * （如果是 version=2.0.0，该参数为必传。）
     */
    @XmlElement
    private String mobileNo2;
    /**
     * 操作类型
     * （0：新增 1：删除 2：修改
     * 注：
     * 1、如果不上传该字段则默认为“0：新增”银行卡信息，
     * 2、该参数为1删除时，必须指定：子商户编码（merchantId）
     * 和银行卡号（bankaccountNo），其它字段可以不上送。）
     */
    @XmlElement
    private String handleType;
    /**
     * 银行代码
     * （注：1、参见附录的开户行3位银行代码。
     * 2、当操作类型为“新增”，该参数必填。）
     */
    @XmlElement
    private String bankCode;
    /**
     * 账户属性
     * （注：1、账户属性0：私人，1：公司。2、当操作类型为“新增”，该参数必填。）
     */
    private Integer bankaccProp;
    /**
     * 持卡人姓名
     * （注：当操作类型为“新增”，该参 数必填。若该子商户为对私商户，
     * 则持卡人姓名需与基础信息入驻接口的法人姓名（corpmanName）相同）
     */
    @XmlElement
    private String name;
    /**
     * 银行卡号
     */
    @XmlElement
    private String bankaccountNo;
    /**
     * 银行卡类型
     * （注：当操作类型为“新增”，该参数必填。1-借记卡（账户属性为0-私人时，只能上送1）；2-贷记卡；3-存折）
     */
    private Integer bankaccountType;
    /**
     * 办卡证件类型
     */
    @XmlElement
    private Integer certCode;
    /**
     * 证件号码
     * （注：当操作类型为“新增”，该参
     * 数必填。）
     */
    private String certNo;
    /**
     * 联行号
     */
    @XmlElement
    private String bankbranchNo;
    /**
     * 默认账户：0:否 1：是。
     */
    @XmlElement
    private Integer defaultAcc;
    /**
     * 省
     */
    @XmlElement
    private String province;
    /**
     * 市
     */
    @XmlElement
    private String city;
    /**
     * 支行名称
     */
    @XmlElement
    private String bankBranchName;
}
