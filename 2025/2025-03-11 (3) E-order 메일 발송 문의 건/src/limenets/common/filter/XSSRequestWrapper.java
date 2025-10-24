package com.limenets.common.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.limenets.common.util.HttpUtils;

public class XSSRequestWrapper extends HttpServletRequestWrapper {
	public XSSRequestWrapper(final HttpServletRequest request) {
		super(request);
	}
	
	private String replaceTag(String val) {
		if (val == null) {
			return null;
		}
		return HttpUtils.replaceXss(val);
	}

	private String[] replaceTag(String[] vals) {
		String[] res = new String[vals.length];
		System.arraycopy(vals, 0, res, 0, vals.length);
		for (int i=0; i<res.length; i++) {
			res[i] = replaceTag(res[i]);
		}
		return res;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> map = new HashMap<>(super.getParameterMap());
		for (Entry<String, String[]> elem : map.entrySet()) {
			elem.setValue(replaceTag(elem.getValue()));
		}
		return Collections.unmodifiableMap(map);
	}
	
	@Override
	public String[] getParameterValues(String name) {
		String[] values = super.getParameterValues(name);
		if (values == null) {
			return null;
		}
		return replaceTag(values);
	}
	
	@Override
	public String getParameter(String name) {
		String value = super.getParameter(name);
		return replaceTag(value);
	}
}
