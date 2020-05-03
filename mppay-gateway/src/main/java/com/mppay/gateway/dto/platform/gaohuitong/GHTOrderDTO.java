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
public class GHTOrderDTO extends AbstractObject {

    @XmlElement
    private String terminalId; //商户终端号
    @XmlElement
    private String userId; //商户用户号
    @XmlElement
    private String bindId; //绑卡ID（签 约编号）
    @XmlElement
    private String childMerchantId; //子商户号
    @XmlElement
    private String currency; //交易币种
    @XmlElement
    private String valid; //有效期
    @XmlElement
    private String cvn2; //CVN2
    @XmlElement
    private String amount; //交易金额
    @XmlElement
    private String fcCardNo; //入金卡号
    @XmlElement
    private String userFee; //用 户 手 续  费
    @XmlElement
    private String productCategory; //商品类别
    @XmlElement
    private String productName; //商品名称
    @XmlElement
    private String reckonCurrency; //清算币种
    @XmlElement
    private String chnSerialNo; //交易使用 商户号
    @XmlElement
    private String cityCode; //城市编号
    @XmlElement
    private String categoryUnion; //银联行业类型
    @XmlElement
    private String notify_url; //异步通知地址

}
