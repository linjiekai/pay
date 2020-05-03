package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 高汇通-快捷支付- 转账
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTTransferDTO extends AbstractObject {

    /**
     * 转账类型
     */
    @XmlElement
    private String transferType;
    /**
     * 出款子商户 编码
     */
    @XmlElement
    private String outMerchantId;
    /**
     * 入款子商户 编码
     */
    @XmlElement
    private String inMerchantId;
    /**
     * 机构终端号
     */
    @XmlElement
    private String terminalNo;
    /**
     * 转账金额
     */
    @XmlElement
    private String amount;
}
