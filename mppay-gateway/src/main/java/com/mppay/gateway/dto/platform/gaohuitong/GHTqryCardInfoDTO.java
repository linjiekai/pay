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
public class GHTqryCardInfoDTO extends AbstractObject {

    /**
     * 子商户编码
     * （商户基础信息登记接口返回的:merchantId）
     */
    @XmlElement
    private String merchantId;

    /**
     * 银行卡信息列表数
     */
    @XmlElement
    private String listNum;

    //银行卡号
    @XmlElement
    private String bankaccountNo;

    //银行卡信息列表
    @XmlElement(name = "bankaccounList")
    private GHTBankaccounListDTO bankaccounList;


}
