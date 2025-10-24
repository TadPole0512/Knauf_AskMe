package com.limenets.common.resolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.limenets.common.dto.LoginDto;

public class LimeLoginArgResolver implements HandlerMethodArgumentResolver {
	public static final String SESSION_ATTRIBUTE = "loginDto";

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return LoginDto.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	@SuppressWarnings("static-access")
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = (HttpServletRequest)webRequest.getNativeRequest();
		HttpSession session = request.getSession();
		
		LoginDto loginDto = (LoginDto)session.getAttribute(this.SESSION_ATTRIBUTE);
		return loginDto;
	}
}
