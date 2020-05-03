package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 用户实名认证信息明细表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
@Data
public class UserRealNameDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家：MPWJMALL
     */
    private String platform;

    /**
     * 用户操作号
     */
    private String userOperNo;

    /**
     * 商城用户id
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别 0:未知 1:男 2:女
     */
    private Integer gender;

    /**
     * 证件类型 1:身份证 2:护照  3:军官证  4:回乡证  5:台胞证  6:港澳通行证  7:国际海员证  8:外国人永久居住证 9:其它
     */
    private Integer cardType;

    /**
     * 证件号
     */
    private String cardNo;

    /**
     * 证件号简称
     */
    private String cardNoAbbr;

    /**
     * 状态 0:注销 1:实名 2：冻结
     */
    private Integer status;

    /**
     * 实名标识 0：未实名 1：弱实名 2：强实名
     */
    private Integer realed;

    /**
     * 生日yyyy-MM-dd
     */
    private String birthday;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 县/区
     */
    private String country;

    /**
     * 身份证所在地
     */
    private String address;

    /**
     * 地区代码
     */
    private String addressCode;

    /**
     * 身份证校验码
     */
    private String lastCode;

    /**
     * 有效开始日期yyyy-MM-dd
     */
    private String effStartDate;

    /**
     * 有效结束日期yyyy-MM-dd
     */
    private String effEndDate;

    /**
     * 实名日期yyyy-MM-dd
     */
    private String realDate;

    /**
     * 实名时间HH:mm:ss
     */
    private String realTime;

    /**
     * 来源 0：我的、1：提交订单 2：快捷支付
     */
    private Integer realSource;

    /**
     * 终端系统 IOS、ANDROI、H5、WX-APPLET
     */
    private String sysCnl;

    /**
     * 身份证正面
     */
    private String imgFront;

    /**
     * 身份证反面
     */
    private String imgBack;

    /**
     * 返回码
     */
    private String returnCode;

    /**
     * 返回信息
     */
    private String returnMsg;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 实名类型 0：用户本人实名 1：订购人
     */
    private String realType;


}
