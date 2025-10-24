package com.limenets.eorder.ctrl.front;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.limenets.common.dto.LoginDto;
import com.limenets.common.util.Converter;
import com.limenets.eorder.svc.BoardSvc;
import com.limenets.eorder.svc.CommonSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.util.StatusUtil;

/**
 * Front 인덱스 컨트롤러.
 */
@Controller
@RequestMapping("/front/index/*")
public class FIndexCtrl {
	private static final Logger logger = LoggerFactory.getLogger(FIndexCtrl.class);
	
	@Inject private BoardSvc boardSvc;
	@Inject private CommonSvc commonSvc;
	@Inject private OrderSvc orderSvc;

	/**
	 * 메인 인덱스 폼.
	 */
	//@GetMapping(value="index")
	@RequestMapping(value="index", method={RequestMethod.GET, RequestMethod.POST})
	public String index(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);

		model.addAttribute("orderStatus", StatusUtil.ORDER.getMap()); // 주문상태 Map형태로 가져오기.
		model.addAttribute("salesOrderStatus", StatusUtil.SALESORDER.getMap()); // 출하상태 List<Map>형태로 가져오기.
		
		//공지사항
		model.addAllAttributes(boardSvc.getBoardListForIndex("notice","1","5"));
		//FAQ
		model.addAllAttributes(boardSvc.getBoardListForIndex("faq","1","5"));
		//자료실
		model.addAllAttributes(boardSvc.getBoardListForIndex("reference","1","5"));
		//배너리스트 BN_TYPE = 2 : 거래처 메인배너1 (대시보드를 제외한 최대 2개 )
		model.addAttribute("main1BannerList",boardSvc.getBannerListForFront("2",2));
		//배너리스트 BN_TYPE = 3 : 거래처 메인배너2
		model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));

		Gson gson = new Gson();
		//팝업리스트 PU_TYPE = 2 : 메인화면 팝업
		model.addAttribute("popupList", gson.toJson(boardSvc.getPopupListForFront(params,"2")));
		
		// 가장 마지막에 놓자...
		params.put("r_custcd", loginDto.getCustCd());
		if(!StringUtils.equals("CO", loginDto.getAuthority())) {
			params.put("r_shiptocd", loginDto.getShiptoCd());
		}
		// 내부사용자 웹주문현황  > 별도 권한 설정.
		orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
		String today = Converter.dateToStr("yyyy-MM-dd");
		String today2 = Converter.dateToStr("yyyyMMdd");
		
		// 주문접수 리스트
		params.put("r_statuscd", "00");
		params.put("r_startrow", 1);
		params.put("r_endrow", 3);
		params.put("r_orderby", "XX.INDATE DESC ");
		List<Map<String, Object>> listFor00 = orderSvc.getCustOrderDList(params);
		params.put("r_startrow", "");
		params.put("r_endrow", "");
		params.put("r_orderby", "");
		
		// 웹주문현황[TODAY]
		params.put("r_insdate", today);
		params.put("r_inedate", today);
		int cntFor00 = orderSvc.getCustOrderHCnt(params); // 주문접수.
		
		params.put("r_ordersdt", today2);
		params.put("r_orderedt", today2);
		params.put("wherebody_status", "STATUS2 IN (522, 525, 527) ");
		int cntFor522 = orderSvc.getSalesOrderGroupCnt(params); // 주문확정 522 >> 주문확정 재정의 : 오더접수+신용체크+출하접수
		
		params.put("wherebody_status", "STATUS2 IN (530) ");
		int cntFor530 = orderSvc.getSalesOrderGroupCnt(params); // 배차완료.
		
		params.put("wherebody_status", "STATUS_DESC = '출하완료' ");
		//params.put("wherebody_status", "(STATUS2 >= 560) ");
		int cntFor560 = orderSvc.getSalesOrderGroupCnt(params); // 출하완료.
		
		model.addAttribute("listFor00", listFor00);
		model.addAttribute("cntFor00", cntFor00);
		model.addAttribute("cntFor522", cntFor522);
		model.addAttribute("cntFor530", cntFor530);
		model.addAttribute("cntFor560", cntFor560);
		
		return "front/index/index";
	}

}
