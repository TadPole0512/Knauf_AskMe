package com.limenets.eorder.ctrl.admin;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.eorder.svc.PromotionSvc;

/**
 * Admin 프로모션 컨트롤러.
 */
@Controller
@RequestMapping("/admin/promotion/*")
public class PromotionCtrl {
	private static final Logger logger = LoggerFactory.getLogger(PromotionCtrl.class);
	@Inject PromotionSvc promotionSvc;

	/**
	 * 이벤트(Promotion)현황 폼
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 */
	@GetMapping(value="promotionList")
	public String promotionList(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) {
		return "admin/promotion/promotionList";
	}

	/**
	 * 이벤트 폼 > 팝업 폼.
	 * @작성일 : 2020. 4. 17.
	 * @작성자 : isaac
	 */
	@PostMapping(value="promotionViewPop")
	public String promotionViewPop(@RequestParam Map<String, Object> params, Model model,HttpServletRequest req) throws LimeBizException {

		//진행중 이벤트
		model.addAttribute("promotionOne",promotionSvc.getPromotionOne(params));
		//진행완료 이벤트
//		model.addAttribute("promotionItemList",promotionSvc.getPromotionItemListForPop(params,req));
		model.addAllAttributes(promotionSvc.getPromotionItemListForPop(params,req));

		return "admin/promotion/promotionViewPop";
	}

	/**
	 * 이벤트(Promotion) 폼 > 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="getPromotionListAjax")
	public Object getPromotionListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
		Map<String, Object> resMap = promotionSvc.getPromotionListAjax(params, req);
		return resMap;
	}


	/**
	 * 이벤트 등록/수정 폼.
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 */
	@GetMapping(value="promotionAdd")
	public String promotionAdd(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		String r_prmseq = Converter.toStr(params.get("r_prmseq"));
		String userId = loginDto.getUserId();
		if(StringUtils.equals("", r_prmseq)){
			model.addAttribute("pageType", "ADD");
		}else{
			if(StringUtils.equals("",r_prmseq)) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);

			model.addAttribute("promotionOne", promotionSvc.getPromotionOneBySeq(r_prmseq));
			model.addAttribute("pageType", "EDIT");
		}

		return "admin/promotion/promotionAdd";
	}

	/**
	 * 이벤트(Promotion) 폼 > 저장/수정 Ajax.
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="insertUpdatePromotionAjax")
	public Object insertUpdatePromotionAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		Map<String, Object> resMap = promotionSvc.insertUpdatePromotionTransaction(params, req, loginDto);
		return resMap;
	}

	/**
	 * 이벤트(Promotion) 파일 다운로드.
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 */
	@PostMapping(value="promotionFileDown")
	public ModelAndView promotionFileDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model) throws LimeBizException {
		ModelAndView mv = promotionSvc.promotionFileDown(params, req, model);

		// 파일 다운로드 종료후 뷰단에서 종료여부 체크중인 쿠키 생성.
		//logger.debug("### 파일다운로드 종료 ###");
		String fileToken = Converter.toStr(params.get("filetoken"));
		Cookie fileCookie = new Cookie(fileToken, "true");
		fileCookie.setMaxAge(60*1); // 1분만 저장.
		res.addCookie(fileCookie);
		//logger.debug("fileToken : {}", fileToken);

		return mv;
	}

	/**
	 * 이벤트(Promotion) 폼 > 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="getPromotionItemListAjax")
	public Object getPromotionItemListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
		Map<String, Object> resMap = new HashMap<>();

		if(StringUtils.equals("EDIT", Converter.toStr(params.get("r_pagetype")))){
			resMap = promotionSvc.getPromotionItemListAjax(params, req);
		}else{
			resMap = null;
		}

		return resMap;
	}
}
