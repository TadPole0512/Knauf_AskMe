package com.limenets.common.util;

import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

public class AccumSapUtils {
	public static String getClientIp(HttpServletRequest request) throws UnknownHostException {
		String ip = request.getHeader("X-Forwarded-For");
		String prox = "X-Forwarded-For"; 
		
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("Proxy-Client-IP");
			prox = "Proxy-Client-IP";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("WL-Proxy-Client-IP");
			prox = "WL-Proxy-Client-IP";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("HTTP_CLIENT_IP");
			prox = "HTTP_CLIENT_IP";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			prox = "HTTP_X_FORWARDED_FOR";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("X-Real-IP");
			prox = "X-Real-IP";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("X-RealIP");
			prox = "X-RealIP";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("REMOTE_ADDR");
			prox = "REMOTE_ADDR";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getRemoteAddr();
			prox = "REMOTE_ADDR";
		}

		return prox;
	}
	
	public static String getMobileClientIp(HttpServletRequest request) throws UnknownHostException {
		String ip = request.getHeader("X-Forwarded-For");
		//String prox = "X-Forwarded-For"; 
		
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("Proxy-Client-IP");
			//prox = "Proxy-Client-IP";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("WL-Proxy-Client-IP");
			//prox = "WL-Proxy-Client-IP";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("HTTP_CLIENT_IP");
			//prox = "HTTP_CLIENT_IP";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			//prox = "HTTP_X_FORWARDED_FOR";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("X-Real-IP");
			//prox = "X-Real-IP";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("X-RealIP");
			//prox = "X-RealIP";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getHeader("REMOTE_ADDR");
			//prox = "REMOTE_ADDR";
		}
		if( (ip == null) || (ip.length() == 0) || "unknown".equalsIgnoreCase(ip) ) {
			ip = request.getRemoteAddr();
			//prox = "REMOTE_ADDR";
		}

		return ip;
	}
}
