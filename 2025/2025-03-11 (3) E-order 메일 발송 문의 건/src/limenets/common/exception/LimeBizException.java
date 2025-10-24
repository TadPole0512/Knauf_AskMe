package com.limenets.common.exception;

/**
 * 비즈니스 로직상에 결함이 있을경우 사용.
 */
public class LimeBizException extends LimeException {
	private static final long serialVersionUID = 1L;

	public LimeBizException() {
		super();
	}

	public LimeBizException(String errMsg) {
		super(errMsg);
	}

	public LimeBizException(String errMsg, Throwable e) {
		super(errMsg, e);
	}

	public LimeBizException(String errCode, String errMsg) {
		super(errCode, errMsg);
	}

	public LimeBizException(String errCode, String errMsg, Throwable e) {
		super(errCode, errMsg, e);
	}

	public LimeBizException(MsgCode msgCode) {
		super(msgCode);
	}

	public LimeBizException(MsgCode msgCode, Object... txtParam) {
		super(msgCode, txtParam);
	}
}
