package com.limenets.eorder.util;

import java.security.MessageDigest;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class PasswordSHA {
	
	/**
	 * 비밀번호 암호화 처리.
	 */
	public static String getShaPasswd(String password) throws Exception{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		String salt = ""; // 임의의 문자열.
		password = salt + password;
		
	    byte[] mdResult = md.digest(password.getBytes());

	    BASE64Encoder encoder = new BASE64Encoder();
	    String shaPasswd = encoder.encode(mdResult);

	    return shaPasswd;
	}

	
	/**
	 * 임시비밀번호 생성하기.
	 */
	public static String getTempPasswd() throws Exception {
		String rnd_password = "";
		for (int i=1; i<=8; i++) {
			int rnd_val = 1 + (int)(Math.random() * 52.0D);
			if (rnd_val > 26) rnd_val += 70;
			else rnd_val += 64;
			rnd_password = rnd_password + (char)rnd_val;
		}
	    return rnd_password;
	}
}
