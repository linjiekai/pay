package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 绑卡表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
@Data 
public class CardBind implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 协议号
     */
    private String agrNo;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL，合伙人：PTMALL
     */
    private String platform;

    /**
     * 用户操作号
     */
    private String userOperNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 卡状态 1：绑定 2：解绑 3：冻结
     */
    private Integer status;

    /**
     * 支付终端系统 IOS、ANDROI、WEB、H5、WX-APPLET、WX-PUBLIC
     */
    private String sysCnl;

    /**
     * 交易类型 JSAPI：公众号或小程序支付 APP：app支付 NATIVE：扫码支付 MICROPAY：刷卡支付 MWEB：H5支付
     */
    private String tradeType;
    
    /**
     * 手机号
     */
    private String mobile;
    
    /**
     * 证件类型 1:身份证 2:护照  3:军官证  4:回乡证  5:台胞证  6:港澳通行证  7:国际海员证  8:外国人永久居住证 9:其它
     */
    private Integer cardType;

    /**
     * 证件号
     */
    private String cardNo;

    /**
     * 绑定日期 YYYY-MM-DD
     */
    private String bindDate;

    /**
     * 绑定时间 HH:mm:ss
     */
    private String bindTime;

    /**
     * 开户行银行联行号
     */
    private String bankNo;

    /**
     * 银行编号：支付宝：ALIPAY 微信：WEIXIN 名品猫：MPPAY
     */
    private String bankCode;

    /**
     * 银行简称
     */
    private String bankAbbr;

    /**
     * 银行类型 0:第三方平台;1:银行;9:未知
     */
    private Integer bankType;

    /**
     * 银行卡姓名
     */
    private String bankCardName;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 银行卡类型 01:借记卡;02:贷记卡;08:第三方平台;
     */
    private String bankCardType;
    
    /**
     * 银行卡照正面图片url地址
     */
    private String bankCardImgFront;

    /**
     * 开户省代码
     */
    private String bankProv;

    /**
     * 开户市代码
     */
    private String bankCity;

    /**
     * 用户客户端IP
     */
    private String clientIp;

    /**
     * 备注
     */
    private String remark;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    
}
