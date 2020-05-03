package com.mppay.core.constant;

public enum WithdrOrderStatus {
	AUDIT("A", "审核中"), WAIT("W", "待提现"), SUCCESS("S", "提现成功"),
	REFUSE("R", "审核拒绝"), WAIT_PAY("WP", "代付"),
    FAIL("F", "提现失败"), BANK_WAIT("BW", "银行受理中");

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

    private WithdrOrderStatus(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
