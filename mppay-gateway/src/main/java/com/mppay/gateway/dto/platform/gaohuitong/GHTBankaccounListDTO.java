package com.mppay.gateway.dto.platform.gaohuitong;


import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTBankaccounListDTO extends AbstractObject {


    /**
     * 银行卡号
     */
    @XmlElement
    private String bankaccountNo;
    /**
     * 银行卡号
     */
    @XmlElement
    private String bankCode;

    @XmlElement
    private String bankaccProp; //账户属性
    @XmlElement
    private String name; //持卡人姓名
    @XmlElement
    private String bankaccountType; //银行卡类型
    @XmlElement
    private String certCode; //办卡证件类型
    @XmlElement
    private String certNo; //证件号码
    @XmlElement
    private String defaultAcc; //默认账户
    @XmlElement
    private String authResult; //实名验证结果

}
