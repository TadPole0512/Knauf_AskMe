package com.limenets.common.filter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class XSSMultipartRequestWrapper extends XSSRequestWrapper implements MultipartHttpServletRequest {
	public XSSMultipartRequestWrapper(final HttpServletRequest request) {
		super(request);
		Assert.isInstanceOf(MultipartHttpServletRequest.class, request);
	}
	
	public MultipartHttpServletRequest getMultipartHttpServletRequest() {
		return (MultipartHttpServletRequest)super.getRequest();
	}

	@Override
	public MultipartFile getFile(String arg0) {
		return getMultipartHttpServletRequest().getFile(arg0);
	}

	@Override
	public Map<String, MultipartFile> getFileMap() {
		return getMultipartHttpServletRequest().getFileMap();
	}

	@Override
	public Iterator<String> getFileNames() {
		return getMultipartHttpServletRequest().getFileNames();
	}

	@Override
	public List<MultipartFile> getFiles(String arg0) {
		return getMultipartHttpServletRequest().getFiles(arg0);
	}

	@Override
	public MultiValueMap<String, MultipartFile> getMultiFileMap() {
		return getMultipartHttpServletRequest().getMultiFileMap();
	}

	@Override
	public String getMultipartContentType(String arg0) {
		return getMultipartHttpServletRequest().getMultipartContentType(arg0);
	}

	@Override
	public HttpHeaders getMultipartHeaders(String arg0) {
		return getMultipartHttpServletRequest().getMultipartHeaders(arg0);
	}

	@Override
	public HttpHeaders getRequestHeaders() {
		return getMultipartHttpServletRequest().getRequestHeaders();
	}

	@Override
	public HttpMethod getRequestMethod() {
		return getMultipartHttpServletRequest().getRequestMethod();
	}
}
