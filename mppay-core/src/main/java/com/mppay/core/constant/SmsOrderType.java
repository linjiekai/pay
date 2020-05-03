package com.mppay.core.constant;

/**
 * 短信订单类型:
 */
public enum SmsOrderType {
    /**
     * 签约+支付验证码
     */
    SIGN_PAY(0, "签约+支付验证码"),
    /**
     * 签约验证码
     */
    SIGN(1, "签约验证码"),
    /**
     * 支付验证码
     */
    PAY(2, "支付验证码"),
    /**
     * 解约验证码
     */
    CANCEL(3, "解约验证码"),
    /**
     * 提现绑卡验证码
     */
    WITHDRAW_CARD_BIND(4, "提现绑卡验证码"),
    /**
     * 提现验证码
     */
    WITHDRAW(5, "提现验证码");

    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private SmsOrderType(int id, String name) {
        this.id = id;
        this.name = name;
    }

}
