package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author: Jiekai Lin
 * @Description(描述):    基础头信息，请求和响应通用
 * @date: 2019/9/5 12:03
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTHeadDTO extends AbstractObject {

    @XmlElement
    private String version; //版本号
    @XmlElement
    private String agencyId; //机构标识,报文发送方的机构标识，由支付平台分配
    @XmlElement
    private String msgType; //报文类型,商户相关报文：01   支付平台相关报文：02
    @XmlElement
    private String tranCode; //交易服务码
    @XmlElement
    private String reqMsgId; //请求交易流水号, 商户请求交易流水号，唯一
    @XmlElement
    private String payMsgId; //平台流水号 ，平台返回流水号
    @XmlElement
    private String reqDate; //请求日期时间，格式为yyyyMMddHHmmss
    @XmlElement
    private String respDate; //应答日期时间，格式为yyyyMMddHHmmss
    @XmlElement
    private String respType; //应答类型 ，S：成功    E：失败    R：不确定（处理中）
    @XmlElement
    private String respCode; //应答码 成功：000000    失败：返回具体的响应码。
    @XmlElement
    private String respMsg; //应答描述

}
