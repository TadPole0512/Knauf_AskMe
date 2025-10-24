package com.limenets.common.exception;

public class LimeException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private String errorCode = "";
	
	public LimeException() {
		super();
	}
  
	public LimeException(String errMsg) {
		super(errMsg);
	}
	
	public LimeException(String errMsg, Throwable e) {
		super(errMsg, e);
	}
	
	public LimeException(String errCode, String errMsg) {
		super("[" + errCode + "]" + errMsg);
		this.errorCode = errCode;
	}

	public LimeException(String errCode, String errMsg, Throwable e) {
		super("[" + errCode + "]" + errMsg, e);
		this.errorCode = errCode;
	}
	
	public String getErrorCode() {
		return this.errorCode;
	}
	
	public LimeException(MsgCode msgCode) {
		super("[" + msgCode.getCode() + "]" + msgCode.getMessage());
		this.errorCode = msgCode.getCode();
	}  

	public LimeException(MsgCode msgCode, Object... txtParam) {
		super("[" + msgCode.getCode() + "]" + String.format(msgCode.getMessage(), txtParam));
		this.errorCode = msgCode.getCode();
	}
}
