package com.limenets.common.dto;

public class LoginDto {
	private String userId;
	private String userNm;
	private String authority;
	private String userFile;
	private String userEmail;
	private String userPushId;
	
	private String sessId;
	private String loginToken;
	private String remoteIp;
	private String menu;
	private String roleCode;
	
	private String accessDevice;
	private String firstLogin;
	private String custNm;
	private String custCd;
	private String shiptoNm;
	private String shiptoCd;
	
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	
	public String getUserNm() { return userNm; }
	public void setUserNm(String userNm) { this.userNm = userNm; }
	
	public String getAuthority() { return authority; }
	public void setAuthority(String authority) { this.authority = authority; }
	
	public String getUserFile() { return userFile; }
	public void setUserFile(String userFile) { this.userFile = userFile; }
	
	public String getUserEmail() { return userEmail; }
	public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
	
	public String getUserPushId() { return userPushId; }
	public void setUserPushId(String userPushId) { this.userPushId = userPushId; }
	
	public String getSessId() { return sessId; }
	public void setSessId(String sessId) { this.sessId = sessId; }
	
	public String getLoginToken() { return loginToken; }
	public void setLoginToken(String loginToken) { this.loginToken = loginToken; }
	
	public String getRemoteIp() { return remoteIp; }
	public void setRemoteIp(String remoteIp) { this.remoteIp = remoteIp; }
	
	public String getMenu() { return menu; }
	public void setMenu(String menu) { this.menu = menu; }
	
	public String getRoleCode() { return roleCode; }
	public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
	
	public String getAccessDevice() { return accessDevice; }
	public void setAccessDevice(String accessDevice) { this.accessDevice = accessDevice; }
	
	public String getFirstLogin() { return firstLogin; }
	public void setFirstLogin(String firstLogin) { this.firstLogin = firstLogin; }
	
	public String getCustNm() { return custNm; }
	public void setCustNm(String custNm) { this.custNm = custNm; }
	
	public String getCustCd() { return custCd; }
	public void setCustCd(String custCd) { this.custCd = custCd; }
	
	public String getShiptoNm() { return shiptoNm; }
	public void setShiptoNm(String shiptoNm) { this.shiptoNm = shiptoNm; }
	
	public String getShiptoCd() { return shiptoCd; }
	public void setShiptoCd(String shiptoCd) { this.shiptoCd = shiptoCd; }
	

}
