package com.limenets.common.exception;

/**
 * 권한이 없거나 세션이 끊겼을때 사용.
 */
public class LimeAuthException extends LimeException {
	private static final long serialVersionUID = 1L;
	
	public LimeAuthException() {
		super();
	}

	public LimeAuthException(String errMsg) {
		super(errMsg);
	}

	public LimeAuthException(String errMsg, Throwable e) {
		super(errMsg, e);
	}

	public LimeAuthException(String errCode, String errMsg) {
		super(errCode, errMsg);
	}

	public LimeAuthException(String errCode, String errMsg, Throwable e) {
		super(errCode, errMsg, e);
	}

	public LimeAuthException(MsgCode msgCode) {
		super(msgCode);
	}  

	public LimeAuthException(MsgCode msgCode, Object... txtParam) {
		super(msgCode, txtParam);
	}
}
