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
public class GHTQuickBindDTO extends AbstractObject {

    @XmlElement
    private String bankCardNo; //银行卡号
    @XmlElement
    private String bankCardType; //银行类型
    @XmlElement
    private String mobilePhone; //手机号
    @XmlElement
    private String terminalId; //商户终端号
    @XmlElement
    private String childMerchantId; //子商户号
    @XmlElement
    private String userId; //商户用户号
    @XmlElement
    private String bankCode; //银行编号
    @XmlElement
    private String  bindId; //绑卡ID（签 约编号）
    @XmlElement
    private String  needSms; //是否需要短验
    @XmlElement
    private String  valid; //有效期
    @XmlElement
    private String  cvn2; //CVN2
    @XmlElement
    private String  accountName; //持 卡 人 姓 名
    @XmlElement
    private String  certificateNo; //证件号码
    @XmlElement
    private String  certificateType; //证件类型


}
