package com.limenets.eorder.ctrl.front;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.eorder.svc.PromotionSvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.limenets.eorder.svc.BoardSvc;
import com.limenets.eorder.svc.CommonSvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Front 프로모션 컨트롤러.
 */
@Controller
@RequestMapping("/front/promotion/*")
public class FPromotionCtrl {
	private static final Logger logger = LoggerFactory.getLogger(FPromotionCtrl.class);
	
	@Inject private CommonSvc commonSvc;
	@Inject private PromotionSvc promotionSvc;
	@Inject private BoardSvc boardSvc;

	/**
	 * 이벤트 리스트 폼.
	 * @작성일 : 2020. 4. 15.
	 * @작성자 : isaac
	 */
	@RequestMapping(value="promotionList", method={RequestMethod.GET, RequestMethod.POST})
	public String promotionList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		// bottom영역 영업사원, CS담당자 정보 가져오기
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		//진행중 이벤트
		model.addAllAttributes(promotionSvc.getPromotionListForFront(params,req,"Y"));
		//진행완료 이벤트
		model.addAllAttributes(promotionSvc.getPromotionListForFront(params,req,"N"));
		
		model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));

		return "front/promotion/promotionList";
	}

	/**
	 * 이벤트 상세 폼.
	 * @작성일 : 2020. 4. 16.
	 * @작성자 : isaac
	 */
	@RequestMapping(value="promotionDetail", method={RequestMethod.GET, RequestMethod.POST})
	public String promotionDetail(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		// bottom영역 영업사원, CS담당자 정보 가져오기
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		//진행중 이벤트
		model.addAttribute("promotionOne",promotionSvc.getPromotionOne(params));
		//진행완료 이벤트
		model.addAttribute("promotionItemList",promotionSvc.getPromotionItemListForFront(params));

		return "front/promotion/promotionDetail";
	}
}
