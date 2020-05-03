package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 高汇通-快捷支付- 代付
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTPayDTO extends AbstractObject {

    /**
     * 业务代码
     */
    @XmlElement
    private String business_code;
    /**
     * 子商户号，子商户号与商户
     * 入驻的法人手机
     * 号一致（机构代付
     * 时上送机构号）
     */
    @XmlElement
    private String user_id;
    /**
     * 代付类型
     */
    @XmlElement
    private String DF_type;
    /**
     * 银行代码
     */
    @XmlElement
    private String bank_code;
    /**
     * 账号
     */
    @XmlElement
    private String account_no;
    /**
     * 账号名
     */
    @XmlElement
    private String account_name;
    /**
     * 金额 整数，单位分
     */
    @XmlElement
    private String amount;
    /**
     * 机构终端号
     */
    @XmlElement
    private String terminal_no;
    /**
     *  证件号
     */
    @XmlElement
    private String ID;

    //-------响应字段------
    /**
     *  账号
     */
    @XmlElement
    private String  ACCOUNT_NO;
    /**
     *  账号名
     */
    @XmlElement
    private String  ACCOUNT_NAME;
    /**
     *  金额
     */
    @XmlElement
    private String  AMOUNT;
    /**
     *  自定义用户
     * 号
     */
    @XmlElement
    private String  CUST_USERID;
    /**
     *  备注
     */
    @XmlElement
    private String  REMARK;
    /**
     *  保留域 1
     */
    @XmlElement
    private String  RESERVE1;
    /**
     *  保留域 2
     */
    @XmlElement
    private String  RESERVE2;
    /**
     *  机构余额
     */
    @XmlElement
    private String  merchantBalance;
    /**
     *  商户余额
     */
    @XmlElement
    private String  merchantBalance2;

}
