package com.limenets.common.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;

public class Pager {
	/**
	 * 그리드 페이징 & 폼 페이징
	 * page:페이지, rows:레코드수
	 */
	public void gridSetInfo(int totalCnt, Map<String, Object> params, HttpServletRequest req) {
		Device device = DeviceUtils.getCurrentDevice(req);
		
		int r_page = Converter.toInt(params.get("page"), 1); // 페이지
		int r_limitrow = Converter.toInt(params.get("rows"), 10); // 한페이지에 보여줄 레코드수
		
		if (r_page == 0) r_page = 1;
		if (r_limitrow == 0) r_limitrow = 10;
		
		if (r_limitrow > 0) {
			int totalPage = ( totalCnt % r_limitrow == 0 ) ? totalCnt / r_limitrow : totalCnt / r_limitrow + 1; // 전체페이지 수 
			if (r_page > totalPage) r_page = 1;

			int r_endrow = r_page * r_limitrow; // 끝지점
			int r_startrow = r_endrow - r_limitrow + 1; // 시작지점
			
			// 파라미터 설정 For Form & Grid.  
			params.put("r_startrow", r_startrow);
			params.put("r_endrow", r_endrow);
			params.put("totpage", totalPage);
			
			// 파라미터 설정 For Only Form. 
			int startnumber = totalCnt - ( r_limitrow * ( r_page - 1 ) ); // 일련번호 설정
			int viewpageea = (device.isMobile() || device.isTablet()) ? 5 : 10; // 페이징처리시 보여지는 페이지수 > 접속기기가 모바일(태블릿)=5, PC=10.
			//int viewpageea = Converter.toInt(params.get("r_viewpageea"), 10);
			int startpage = r_page - (r_page - 1) % viewpageea; // 페이징처리시 처음 보여지는 페이지수
			int endpage = startpage + viewpageea - 1; // 페이징처리시 마지막에 보여지는 페이지수
			if(totalPage < endpage) endpage = totalPage;
			params.put("startnumber", startnumber);
			params.put("r_page", r_page);
			params.put("startpage", startpage);
			params.put("endpage", endpage);
			params.put("r_limitrow", r_limitrow);
			params.put("limitrow"+r_limitrow, "selected='selected'");
			
		}
		else { // r_limitrow = -1 로 설정하여 페이징 안타게 처리.
			params.put("r_startrow", "");
			params.put("r_endrow", "");			
		}
	}
	
	/*
	public void setInfo(int totalCnt, Map<String, Object> params, Model model) {
		int r_page = Converter.toInt(params.get("r_page"), 1); // 페이지
		int r_limitrow = Converter.toInt(params.get("r_limitrow"), 10); // 한페이지에 보여줄 레코드수
		
		if (r_page == 0) r_page = 1;
		if (r_limitrow == 0) r_limitrow = 10;
		
		if (r_limitrow > 0) {
			int totalPage = ( totalCnt % r_limitrow == 0 ) ? totalCnt / r_limitrow : totalCnt / r_limitrow + 1; // 전체페이지 수 
			if (r_page > totalPage) r_page = 1;

			int r_endrow = r_page * r_limitrow; // 끝지점
			int r_startrow = r_endrow - r_limitrow + 1; // 시작지점
			
			// xml에 보낼 파라미터 생성
			params.put("r_startrow", r_startrow);
			params.put("r_endrow", r_endrow);
			params.put("totpage", totalPage);
			
			int viewPageEa = Converter.toInt(params.get("r_viewPageEa"), 10); // 페이징처리시 보여지는 페이지수
			int startpage = r_page - (r_page - 1) % viewPageEa; // 페이징처리시 처음 보여지는 페이지수
			int endpage = startpage + viewPageEa - 1; // 페이징처리시 마지막에 보여지는 페이지수
			
			if ( totalPage < endpage) endpage = totalPage;
			
			// 일련번호 설정
			int startnumber = totalCnt - ( r_limitrow * ( r_page - 1 ) );
			
			// jsp에 보낼 파라미터 설정
			model.addAttribute("startnumber", startnumber);
			model.addAttribute("totpage", totalPage);
			model.addAttribute("r_page", r_page);
			model.addAttribute("startpage", startpage);
			model.addAttribute("endpage", endpage);
			model.addAttribute("limitrow"+r_limitrow, "selected='selected'");
		}
		else { // r_limitrow = -1 로 설정하여 페이징 안타게 처리.
			params.put("r_startrow", "");
			params.put("r_endrow", "");			
		}
	}
	*/
	
}
