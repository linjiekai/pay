package com.mppay.core.constant;

public enum OrderStatus {
	WAIT_PAY("W", "待支付"),
	SUCCESS("S", "已完成"),
	CANCEL("C", "已取消"),
	DELETE("D", "已删除"),
	ADVANCE("A", "预登记 "),
	FAIL("F", "交易失败"),
	REFUND("R", "退款预登记"),
	REFUND_AUDIT("RC", "退款审核中 "),
	REFUND_WAIT("RW", "等待退款"),
	REFUND_PART("RP", "部分退款"),
	REFUND_FULL("RF", "全额退款"),
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

	private OrderStatus(String id, String name) {
		this.id = id;
		this.name = name;
	}


	public static OrderStatus parse (String id) {
		for (OrderStatus obj : OrderStatus.values()) {
			if (obj.getId().equals(id)) {
				return obj;
			}
		}
		return null;
	}
}
