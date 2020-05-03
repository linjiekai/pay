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
public class GHTBalanceInfoDTO extends AbstractObject {

    /**
     * 子商户编码
     * （商户基础信息登记接口返回的:merchantId）
     */
    @XmlElement
    private String merchantId;
    /**
     * 账户余额
     */
    @XmlElement
    private String balanceAmount;
    /**
     * 冻结金额
     */
    @XmlElement
    private String freezeAmount;

}
