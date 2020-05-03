package com.mppay.service.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuickAgrVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 身份证号
     */
    private String cardNo;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
