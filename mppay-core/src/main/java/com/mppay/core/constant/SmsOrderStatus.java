package com.mppay.core.constant;

/**
 * 短信订单表.卡状态 0:待绑定 1：绑定成功
 */
public enum SmsOrderStatus {
    /**
     * 卡状态 0:待绑定
     */
    CHECK(0, "待绑定"),
    /**
     * 卡状态 1：绑定成功
     */
    SUCCESS(1, "绑定成功");

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

    private SmsOrderStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
