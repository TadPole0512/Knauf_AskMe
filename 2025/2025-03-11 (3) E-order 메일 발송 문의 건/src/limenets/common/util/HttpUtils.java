package com.limenets.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

public class HttpUtils {
	public static String newUuid() {
		return UUID.randomUUID().toString();
	}
	
	private static final Map<String, String> XSS;
	static {
		Map<String, String> xss = new LinkedHashMap<>();
		//xss.put("&", "&#38;");
		//xss.put("=", "&#61;");
		xss.put("<", "&#60;");
		xss.put(">", "&#62;");
		//xss.put("(", "&#40;");
		//xss.put(")", "&#41;");
		//xss.put("'", "&#39;");
		//xss.put("\"", "&#34;");
		//xss.put(":", "&#58;");
		xss.put("\\", "&#92;");
		XSS = Collections.unmodifiableMap(xss);
	}
	
	private static final Map<String, String> RestoreXSS;
	static {
		Map<String, String> xss = new LinkedHashMap<>();
		//xss.put("&#38;", "&");
		//xss.put("&#61;", "=");
		xss.put("&#60;", "<");
		xss.put("&#62;", ">");
		//xss.put("&#40;", "(");
		//xss.put("&#41;", ")");
		//xss.put("&#39;", "'");
		//xss.put("&#34;", "\"");
		//xss.put("&#58;", ":");
		xss.put("&#92;", "\\");
		RestoreXSS = Collections.unmodifiableMap(xss);
	}

	public static String restoreXss(String str) {
		if (StringUtils.isBlank(str)) {
			return str;
		}
		String rst = str;
		for (Entry<String, String> xss : RestoreXSS.entrySet()) {
			rst = StringUtils.replace(rst, xss.getKey(), xss.getValue());
		}
		return rst;
	}
	
	public static String replaceXss(String str) {
		if (StringUtils.isBlank(str)) {
			return str;
		}
		String rst = str;
		for (Entry<String, String> xss : XSS.entrySet()) {
			rst = StringUtils.replace(rst, xss.getKey(), xss.getValue());
		}
		return rst;
	}
	
	public static String uriEncoding(String val, Charset charset){
		return uriEncoding(val, charset.displayName());
	}
	
	public static String uriEncoding(String val, String charset){
		try{
			return URLEncoder.encode(val, charset);
		}catch(UnsupportedEncodingException e){
			
		}
		return val;
	}
	
	public static String uriEncoding(String val){
		return uriEncoding(val, "UTF-8");
	}
	
	public static String getRemoteAddr(HttpServletRequest request){
		List<String> headerNames = Arrays.asList("X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP","HTTP_X_FORWARDED_FOR");	
		for(String name : headerNames){
			String value = request.getHeader(name);
			if(StringUtils.isNotEmpty(value) && !"unknown".equalsIgnoreCase(value)){
				return value;
			}
		}
		return request.getRemoteAddr();
	}
	
	@SuppressWarnings("unchecked")
	public static HttpHeaders getHttpHeader(HttpServletRequest request){
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()){
			String name = headerNames.nextElement();
			String val = request.getHeader(name);
			headers.add(name, val);
		}
		return headers;
	}
	
	public static String createUriString(String path, Map<String, ?> parameterMap){
		StringBuilder url = new StringBuilder();
		url.append(path);
		if(StringUtils.contains(path, '?')){
			url.append('&');
		}else{
			url.append('?');
		}
		url.append(createQueryString(parameterMap));
		return url.toString();
	}
	
	public static String createQueryString(Map<String,?> parameterMap){
		List<String> queryString = new LinkedList<>();
		for(Entry<String, ?> entry : parameterMap.entrySet()){
			String name = entry.getKey();
			Object values = entry.getValue();
			if(values != null){
				if (values instanceof Object[]) {
					Object[] vals = (Object[]) values;
					for(Object value : vals){
						queryString.add(new StringBuilder().append(uriEncoding(name)).append("=").append(uriEncoding(String.valueOf(value))).toString());
					}
				}else if(values instanceof Collection<?>){
					Collection<?> vals = (Collection<?>) values;
					for(Object value : vals){
						queryString.add(new StringBuilder().append(uriEncoding(name)).append("=").append(uriEncoding(String.valueOf(value))).toString());
					}
				}else{
					queryString.add(new StringBuilder().append(uriEncoding(name)).append("=").append(uriEncoding(String.valueOf(values))).toString());
				}
			}
		}
		return StringUtils.join(queryString, '&');
	}
	
}
