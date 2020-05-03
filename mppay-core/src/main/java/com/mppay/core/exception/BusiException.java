package com.mppay.core.exception;

import com.mppay.core.utils.ApplicationYmlUtil;

public class BusiException extends RuntimeException {

	private static final long serialVersionUID = -4207273862464193292L;

	private String code;

	private String msg;

	public BusiException() {
		super(ApplicationYmlUtil.get(11001));
		this.code = 11001 + "";
		this.msg = ApplicationYmlUtil.get(11001);
	}
	public BusiException(Integer code) {
		super(ApplicationYmlUtil.get(code));
		this.code = code + "";
		this.msg = ApplicationYmlUtil.get(code);
	}

	public BusiException(String code) {
		super(ApplicationYmlUtil.get(code));
		this.code = code;
		this.msg = ApplicationYmlUtil.get(code);
	}
	
	public BusiException(String code, String msg) {
		super(msg);
		this.code = code;
		this.msg = msg;
	}
	public BusiException(Integer code, String msg) {
		super(msg);
		this.code = code+"";
		this.msg = msg;
	}

	public BusiException(String code, Throwable e) {
		super(ApplicationYmlUtil.get(code), e);
		this.code = code;
		this.msg = msg;
	}

	public BusiException(Integer code, Throwable e) {
		super(ApplicationYmlUtil.get(code), e);
		this.code = code+"";
		this.msg = msg;
	}

	public BusiException(String code, String msg, Throwable e) {
		super(msg, e);
		this.code = code;
		this.msg = msg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
