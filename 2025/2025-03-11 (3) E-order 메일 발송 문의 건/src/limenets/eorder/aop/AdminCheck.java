package com.limenets.eorder.aop;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeAuthException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.common.util.HttpUtils;
import com.limenets.eorder.svc.MenuSvc;

@Aspect
public class AdminCheck {
	private static final Logger logger = LoggerFactory.getLogger(AdminCheck.class);
	
	@Value("${https.url}") private String httpsUrl;
	@Autowired MenuSvc menuSvc;
	
	@Around(
		"execution(* com.limenets.eorder.ctrl.admin.AdminBaseCtrl.*(..))"
		+ " or execution(* com.limenets.eorder.ctrl.admin.BoardCtrl.*(..))"
		+ " or execution(* com.limenets.eorder.ctrl.admin.CustomerCtrl.*(..))"
		+ " or execution(* com.limenets.eorder.ctrl.admin.IndexCtrl.*(..))"
		+ " or execution(* com.limenets.eorder.ctrl.admin.ItemCtrl.*(..))"
		+ " or execution(* com.limenets.eorder.ctrl.admin.MypageCtrl.*(..))"
		+ " or execution(* com.limenets.eorder.ctrl.admin.OrderCtrl.*(..))"
		+ " or execution(* com.limenets.eorder.ctrl.admin.PromotionCtrl.*(..))"
		+ " or execution(* com.limenets.eorder.ctrl.admin.SystemCtrl.*(..))"
		+ " or execution(* com.limenets.eorder.ctrl.admin.ReportCtrl.*(..))"
	)
	public Object ex(ProceedingJoinPoint pjp) throws Throwable {
		logger.debug("getTarget : {}", pjp.getTarget());
		
		HttpServletRequest req = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		HttpSession session = req.getSession();
		LoginDto loginDto = (LoginDto)session.getAttribute("loginDto");
		logger.debug("loginDto : {}", loginDto);
		
		
		if (loginDto == null) { // 로그인 하지 않았을 경우
			return viewControl(req, MsgCode.getResultMap(MsgCode.SESSION_DENY_ERROR));
		} 
		else {            
			boolean authFlag = true;
			
			String[] l_remoteIP = loginDto.getRemoteIp().split(":");
			String[] h_remoteIP = HttpUtils.getRemoteAddr(req).split(":");
			
			String lrm = (l_remoteIP.length > 0) ? l_remoteIP[0] : "";
			String hrm = (h_remoteIP.length > 0) ? h_remoteIP[0] : "";
			
			logger.debug("[A]l_remoteIP : {}", l_remoteIP);
			logger.debug("[A]h_remoteIP : {}", h_remoteIP);
			
			if(loginDto != null) {
				logger.debug("[A]SessId_DTO : {}", loginDto.getSessId());
				logger.debug("[A]SessId_SESS : {}", session.getId());
				logger.debug("[A]Authority : {}", loginDto.getAuthority());
				logger.debug("[A]ServletPath : {}", req.getServletPath());
				logger.debug("[A]FirstLogin : {}", loginDto.getFirstLogin());
			}

			if (!StringUtils.equals(loginDto.getSessId(), session.getId())) authFlag = false;
			//if (!StringUtils.equals(loginDto.getRemoteIp(), HttpUtils.getRemoteAddr(req))) authFlag = false;
			if (!StringUtils.equals(lrm, hrm)) authFlag = false;
			if (!authFlag){ // 로그인 정보를 위조하였을 경우
				return this.viewControl(req, MsgCode.getResultMap(MsgCode.SESSION_DENY_ERROR));
			}
			
			// admin/front 접근 체크.
			if (StringUtils.equals("CO", loginDto.getAuthority()) || StringUtils.equals("CT", loginDto.getAuthority())) authFlag = false;
			if (!authFlag){
				return this.viewControl(req, MsgCode.getResultMap(MsgCode.DATA_ACCESS_ERROR));
			}
			
			// 개인정보 미입력시 정보수정페이지 외 다른메뉴 접근불가
			if( !req.getServletPath().contains("myInformation") && StringUtils.equals("Y", loginDto.getFirstLogin())) {
				return this.viewControl3(req);
			}
			
			// 권한 체크하고. 현재 메뉴시퀀스를 request에 담는다(jsp 에서 사용)
			String menu = loginDto.getMenu();
			String url = req.getServletPath();
			
			Map<String, String> paramMap = getMapRequestParam(req);
			logger.debug("paramMap : {}", paramMap);
			
			try {
				int mnSeq = menuSvc.getNowMnSeq(url, paramMap, menu);
				logger.debug("mnSeq : {}", mnSeq);
				req.setAttribute("mnSeq", mnSeq);
				
			} catch(LimeAuthException e) {
				return this.viewControl2(req);
			}
			
		}
		
		return pjp.proceed();
	}
	
	@SuppressWarnings({"unchecked"})
	private Map<String, String> getMapRequestParam(HttpServletRequest req) {
		Map<String, String> resMap = new HashMap<>();
		
		Enumeration<String> params = req.getParameterNames();
		while(params.hasMoreElements()) {
			String name = params.nextElement();
			String val = req.getParameter(name);
			resMap.put(name, val);
		}
		
		return resMap;
	}

	/**
	 * 세션 컨트롤.
	 */
	private Object viewControl(HttpServletRequest req, Map<String, Object> resultMap) {
		String acceptHeader = req.getHeader("Accept");
		if (acceptHeader.contains("application/json")) {
			req.setAttribute("resultAjax", Converter.toJson(resultMap));
			ModelAndView mav = new ModelAndView();
			mav.setViewName("textAjax");
			return mav;
		}
		/*
		else if (acceptHeader.contains("application/xhtml+xml")) { //From Excel-DownLoad 
			req.setAttribute("resultAjax", "<script>alert('"+MsgCode.getResultMap(MsgCode.AUTH_DENY_ERROR).get("RES_MSG")+"'); history.back();</script>");
			ModelAndView mav = new ModelAndView();
			mav.setViewName("textAjax");
			return mav;
		}
		*/
		else if ("1".equals(Converter.toStr(req.getParameter("pop"), "0"))) {
			req.setAttribute("resultAjax", "<script>window.close(); opener.location.href='" + httpsUrl + req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath() + "/admin/login/login.lime';</script>");
			//req.setAttribute("resultAjax", "<script>alert('"+resultMap.get("RES_MSG")+"'); window.close(); opener.location.href='" + httpsUrl + req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath() + "/admin/login/login.lime';</script>");
			return "textAjax";
		}
		else {
			req.setAttribute("resultAjax", "<script>document.location.href='" + httpsUrl + req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath() + "/admin/login/login.lime';</script>");
			//req.setAttribute("resultAjax", "<script>alert('"+resultMap.get("RES_MSG")+"'); document.location.href='" + httpsUrl + req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath() + "/admin/login/login.lime';</script>");
			return "textAjax";
		}
	}
	
	/**
	 * 접근권한 컨트롤.
	 */
	private Object viewControl2(HttpServletRequest req) {
		String acceptHeader = req.getHeader("Accept");
		if (acceptHeader.contains("application/json")) {
			req.setAttribute("resultAjax", Converter.toJson(MsgCode.getResultMap(MsgCode.AUTH_DENY_ERROR)));
			ModelAndView mav = new ModelAndView();
			mav.setViewName("textAjax");
			return mav;
		}
		else if ("1".equals(Converter.toStr(req.getParameter("pop"), "0"))) {
			req.setAttribute("resultAjax", "<script>alert('"+MsgCode.getResultMap(MsgCode.AUTH_DENY_ERROR).get("RES_MSG")+"'); window.close();</script>");
			return "textAjax";
		}
		else {
			req.setAttribute("resultAjax", "<script>alert('"+MsgCode.getResultMap(MsgCode.AUTH_DENY_ERROR).get("RES_MSG")+"'); history.back();</script>");
			return "textAjax";
		}
	}
	
	/**
	 * 접근권한 컨트롤
	 * 개인정보 미입력시 모든 메뉴 접근불가
	 */
	private Object viewControl3(HttpServletRequest req) {
		String acceptHeader = req.getHeader("Accept");
		if (acceptHeader.contains("application/json")) {
			req.setAttribute("resultAjax", Converter.toJson(MsgCode.getResultMap(MsgCode.DATA_FIRST_LOGIN)));
			ModelAndView mav = new ModelAndView();
			mav.setViewName("textAjax");
			return mav;
		}
		else if ("1".equals(Converter.toStr(req.getParameter("pop"), "0"))) {
			req.setAttribute("resultAjax", "<script>alert('"+MsgCode.getResultMap(MsgCode.DATA_FIRST_LOGIN).get("RES_MSG")+"'); window.close();</script>");
			return "textAjax";
		}
		else {
			req.setAttribute("resultAjax", "<script>alert('"+MsgCode.getResultMap(MsgCode.DATA_FIRST_LOGIN).get("RES_MSG")+"'); history.back();</script>");
			return "textAjax";
		}
	}
	
}
