package com.limenets.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.util.Converter;
import com.limenets.common.util.HttpUtils;
import com.limenets.eorder.dao.ErrorLogDao;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@Autowired private ErrorLogDao errorLogDao;
	
	public static final String DEFAULT_ERROR_VIEW = "error";
	public static final String TEXT_ERROR_VIEW = "textAjax";
	
	@ExceptionHandler(value = Exception.class)
	public Object defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
		makeLog(req, e);
		throw e;
	}
	
	@ExceptionHandler(value = LimeBizException.class)
	public Object limeBizExceptionHandler(HttpServletRequest request, HttpServletResponse response, LimeBizException e) throws Exception {
		// makeLog(request, e);
		
		String acceptHeader = request.getHeader("Accept");
		
		logger.debug("error : {}", e.getMessage());

		if(StringUtils.contains(acceptHeader, "application/json")) {	// Ajax Call 요청시 View 설정
			Map<String, String> value = new LinkedHashMap<>();
			value.put("RES_CODE", e.getErrorCode());
			value.put("RES_MSG", e.getMessage());
			
			request.setAttribute("resultAjax", Converter.toJson(value));
			request.setCharacterEncoding("UTF-8");
			
			ModelAndView mav = new ModelAndView();
			mav.addObject("exception", e);
			mav.setViewName(TEXT_ERROR_VIEW);
			return mav;
		} 
		else {
			ModelAndView mav = new ModelAndView();
			mav.addObject("exception", e);
			mav.setViewName(DEFAULT_ERROR_VIEW);
			return mav;
		}
	}

	@ExceptionHandler(value = MaxUploadSizeExceededException.class)
	public Object maxUploadSizeExceededErrorHandler(HttpServletRequest request, HttpServletResponse response, MaxUploadSizeExceededException e) throws Exception{
		// makeLog(request, e);
		
		String acceptHeader = request.getHeader("Accept");
		
		//MaxUploadSizeExceededException ex = e;
		//String maxSize = Converter.addComma(ex.getMaxUploadSize());
		
		if (StringUtils.contains(acceptHeader, "application/json")) {
			request.setAttribute("resultAjax", Converter.toJson(MsgCode.getResultMap(MsgCode.FILE_MAX_SIZE_ERROR)));
			request.setCharacterEncoding("UTF-8");
			
			ModelAndView mav = new ModelAndView();
			mav.addObject("exception", e);
			mav.setViewName(TEXT_ERROR_VIEW);
			return mav;
		}
		else {
			ModelAndView mav = new ModelAndView();
			mav.addObject("exception", e);
			mav.setViewName(DEFAULT_ERROR_VIEW);
			return mav;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void makeLog(HttpServletRequest req, Exception e) {
		//에러내용
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		printWriter.flush();
		String m_elcontent = writer.toString();
		
		//파라미터
		String m_elparam = "";
		Enumeration<String> params = req.getParameterNames();
		while(params.hasMoreElements()) {
			String name = params.nextElement();
			String[] values = req.getParameterValues(name);
			
			if(values.length > 0){
				for(int i=0; i<values.length; i++){
					if("".equals(m_elparam)){
						m_elparam += name + "=" + values[i];
					}else{
						m_elparam += "&" + name + "=" + values[i];
					}
				}
			}
		}

		//세션
		String m_elsession = "";
		HttpSession session = req.getSession();
		Enumeration<String> se = session.getAttributeNames();
		while(se.hasMoreElements()) {
			String getse = se.nextElement();
			if("".equals(m_elsession)){
				m_elsession += getse + "=" + session.getAttribute(getse);
			}else{
				m_elsession += "&" + getse + "=" + session.getAttribute(getse);
			}
		}
		
		String m_elmessage = e.getClass().getName() + ": " + e.getMessage();		
		String m_elurl = Converter.toStr(req.getRequestURI());
		String m_elip = HttpUtils.getRemoteAddr(req);
		
		//에러로그 남김
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("m_elcontent", m_elcontent);
		svcMap.put("m_elmessage", m_elmessage);
		svcMap.put("m_elurl", m_elurl);
		svcMap.put("m_elparam", m_elparam);
		svcMap.put("m_elip", m_elip);
		svcMap.put("m_elsession", m_elsession);
		errorLogDao.in(svcMap);
		svcMap.clear();
	}
}
