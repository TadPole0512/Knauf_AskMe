package com.limenets.eorder.dto;

import java.io.Serializable;

public class OrderExcelUpdateDto implements Serializable{

    private long orderNo;
    private long lineNo;
    private String reason;
    private String ds;
    private String os;
    private String userId;
	
	public OrderExcelUpdateDto() {}

	public long getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(long orderNo) {
		this.orderNo = orderNo;
	}

	public long getLineNo() {
		return lineNo;
	}

	public void setLineNo(long lineNo) {
		this.lineNo = lineNo;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getDs() {
		return ds;
	}

	public void setDs(String ds) {
		this.ds = ds;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	
}
