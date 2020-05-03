package com.mppay.core.constant;

/**
 * 签约类型
 */
public enum SmsOrderAgrType {

    /**
     * 签约类型:快捷
     */
    QUICK("01", "快捷");

    private String id;

    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private SmsOrderAgrType(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
