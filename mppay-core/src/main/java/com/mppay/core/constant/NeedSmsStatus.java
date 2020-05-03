package com.mppay.core.constant;

/**
 * 是否需要发送短信
 */
public enum NeedSmsStatus {

    /**
     * 是否需要发送短信:是
     */
    YES("Y", "是"),
    /**
     * 是否需要发送短信:否
     */
    NO("N", "否"),
    ;
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

    private NeedSmsStatus(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
