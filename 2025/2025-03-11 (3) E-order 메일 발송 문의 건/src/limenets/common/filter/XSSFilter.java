package com.limenets.common.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class XSSFilter extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		
		if (MultipartHttpServletRequest.class.isInstance(request)) {
			filterChain.doFilter(new XSSMultipartRequestWrapper(request), response);
		} else {
			filterChain.doFilter(new XSSRequestWrapper(request), response);
		}
	}
}
