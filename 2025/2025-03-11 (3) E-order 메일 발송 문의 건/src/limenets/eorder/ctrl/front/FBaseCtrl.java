package com.limenets.eorder.ctrl.front;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.eorder.svc.CommonSvc;
import com.limenets.eorder.svc.ConfigSvc;
import com.limenets.eorder.svc.CustomerSvc;
import com.limenets.eorder.svc.ItemSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.UserSvc;

/**
 * front 공통 컨트롤러 > 로그인상태에서만  접근가능.
 */
@Controller
public class FBaseCtrl {
	private static final Logger logger = LoggerFactory.getLogger(FBaseCtrl.class);
	
	@Inject CommonSvc commonSvc;
	@Inject ConfigSvc configSvc;
	@Inject CustomerSvc customerSvc;
	@Inject ItemSvc itemSvc;
	@Inject OrderSvc orderSvc;
	@Inject UserSvc userSvc;
	
	/**
	 * CKEditor 이미지 업로드.
	 * PC용.
	 */
	@GetMapping(value="/front/base/editorFileUpload")
	public Object editorFileUpload(HttpServletRequest req, HttpServletResponse res) throws Exception {
		Map<String, Object> resMap = commonSvc.editorFileUpload(req);
		
		String funcNum = req.getParameter("CKEditorFuncNum");
		String retText = "";
		
		retText = "<script>window.parent.CKEDITOR.tools.callFunction('"+funcNum+"','/eorder/data/editor/"
				+Converter.toStr(resMap.get("editorFileName"))+"','"
				+Converter.toStr(resMap.get("RES_MSG"))+"')</script>";
		req.setAttribute("resultAjax", retText);

		return "textAjax";
	}
	
	/**
	 * CKEditor 이미지 업로드.
	 * 모바일용.
	 */
	@GetMapping(value="/front/base/editorFileUploadForMobile")
	public Object editorFileUploadForMobile(HttpServletRequest req, HttpServletResponse res) throws Exception {
		Map<String, Object> resMap = commonSvc.editorFileUploadForMobile(req);
		
		String funcNum = req.getParameter("CKEditorFuncNum");
		String retText = "";
		
		retText = "<script>window.parent.CKEDITOR.tools.callFunction('"+funcNum+"','/eorder/data/editor/"
				+Converter.toStr(resMap.get("editorFileName"))+"','"
				+Converter.toStr(resMap.get("RES_MSG"))+"')</script>";
		req.setAttribute("resultAjax", retText);
		
		return "textAjax";
	}
	
	/**
	 * 카테고리 단계별 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 25.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/front/base/getCategoryListAjax")
	public Object getCategoryListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.getCategoryList(params, req, loginDto);
	}
	
	/**
	 * 비밀번호 변경요청 팝업 
	 */
	@PostMapping(value="/front/base/userPswdEditPop")
	public String userPswdEditPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "front/base/userPswdEditPop";
	}
	
	/**
	 * 비밀번호 변경요청 페이지
	 */
	//@PostMapping(value="/front/base/userPswdEdit")
	@RequestMapping(value="/front/base/userPswdEdit", method={RequestMethod.GET, RequestMethod.POST})
	public String userPswdEdit(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "front/base/userPswdEdit";
	}

	/**
	 * 납품처 리스트 팝업 폼.
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : kkyu
	 */
	@RequestMapping(value="/front/base/pop/shiptoListPop", method={RequestMethod.GET, RequestMethod.POST})
	public String shiptoListPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		// 납품처 계정은 접근 불가.
		if(StringUtils.equals("CT", loginDto.getAuthority())) throw new LimeBizException(MsgCode.DATA_AUTH_ERROR);
		
		String r_multiselect = Converter.toStr(params.get("r_multiselect"));
		model.addAttribute("r_multiselect", r_multiselect);
		
		if(StringUtils.equals("", Converter.toStr(params.get("where")))) {
			params.put("where", "front");
		}

		// APP인경우 Layer 팝업 호출.
		String layer_pop = Converter.toStr(params.get("layer_pop"));
		if(StringUtils.equals("Y", layer_pop)) {
			return "front/base/shiptoListPopLayer";
		}
		
		params.put("r_stbuserid", loginDto.getUserId());
		params.put("r_custcd", loginDto.getCustCd());
		Map<String, Object> resMap = customerSvc.getShipToList(params, req, loginDto);
		model.addAllAttributes(resMap);
		
		return "front/base/shiptoListPop";
	}
	
	/**
	 * 납품처 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 7. 1.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@RequestMapping(value="/front/base/getShiptoListAjax", method={RequestMethod.GET, RequestMethod.POST})
	public Object getShiptoListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		if(StringUtils.equals("", Converter.toStr(params.get("where")))) {
			params.put("where", "front");
		}
		params.put("r_stbuserid", loginDto.getUserId());
		params.put("r_custcd", loginDto.getCustCd());
		Map<String, Object> resMap = customerSvc.getShipToList(params, req, loginDto);
		
		return resMap;
	}
	
	/**
	 * O_CUST_ORDER_Header 주소록 폼.
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : kkyu
	 */
	@RequestMapping(value="/front/base/pop/orderAddressBookmarkPop", method={RequestMethod.GET, RequestMethod.POST})
	public String orderAddressBookmarkPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("r_oabuserid", loginDto.getUserId());
		Map<String, Object> resMap = orderSvc.getOrderAddressBookmark(params, req, loginDto);
		model.addAllAttributes(resMap);
		
		return "front/base/orderAddressBookmarkPop";
	}
	
	/**
	 * O_CUST_ORDER_Header 주소록 삭제하기 Ajax.
	 * @작성일 : 2020. 4. 7.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/front/base/pop/deleteOrderAddressBookmarkAjax")
	public Object deleteOrderAddressBookmarkAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("r_oabuserid", loginDto.getUserId());
		return orderSvc.deleteOrderAddressBookmark(params, req, loginDto);
	}
	
	/**
	 * 품목관리 리스트 팝업 폼.
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : kkyu
	 */
	@RequestMapping(value="/front/base/pop/itemListPop", method={RequestMethod.GET, RequestMethod.POST})
	public String itemListPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_multiselect = Converter.toStr(params.get("r_multiselect"));
		model.addAttribute("r_multiselect", r_multiselect);
		
		params.put("where", "front");
		
		// APP인경우 Layer 팝업 호출.
		String layer_pop = Converter.toStr(params.get("layer_pop"));
		if(StringUtils.equals("Y", layer_pop)) {
			return "front/base/itemListPopLayer";
		}
		
		Map<String, Object> resMap = itemSvc.getItemList(params, req, loginDto);
		model.addAllAttributes(resMap);
		
		return "front/base/itemListPop";
	}
	
	/**
	 * 납품처 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 7. 1.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@RequestMapping(value="/front/base/getItemListAjax", method={RequestMethod.GET, RequestMethod.POST})
	public Object getItemListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("where", "front");
		Map<String, Object> resMap = itemSvc.getItemList(params, req, loginDto);
		return resMap;
	}
	
	/**
	 * 관련품목 리스트 팝업 폼.
	 * @작성일 : 2020. 5. 12.
	 * @작성자 : kkyu
	 */
	@RequestMapping(value="/front/base/pop/itemRecommendPop", method={RequestMethod.GET, RequestMethod.POST})
	public String itemRecommendPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_multiselect = Converter.toStr(params.get("r_multiselect"));
		model.addAttribute("r_multiselect", r_multiselect);
		
		params.put("where", "front");
		
		String r_itritemcd = Converter.toStr(params.get("r_itritemcd"));
		//model.addAttribute("item", itemSvc.getItemOne(r_itritemcd)); // 품목상세
		model.addAttribute("itemRecommendList", itemSvc.getItemRecommendList(r_itritemcd, "")); // 관련품목 리스트.
		
		return "front/base/itemRecommendPop";
	}
	
	
	/**
	 * 프론트 bottom 거래처별 입금 가상계좌 정보 가져오기 Ajax.
	 * @작성일 : 2020. 4. 26.
	 * @작성자 : kkyu
	 */
	/*@ResponseBody
	@PostMapping(value="/front/base/getCustVAccountAjax")
	public Object getCustVAccountAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return commonSvc.getCustVAccount(params, req, loginDto);
	}*/
	
	/**
	 * 자재주문서 출력 팝업 폼.
	 * @작성일 : 2020. 5. 1.
	 * @작성자 : kkyu
	 */
	@RequestMapping(value="/front/base/orderPaperPop", method={RequestMethod.GET, RequestMethod.POST})
	public Object orderPaperPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		List<Map<String, Object>> resList = orderSvc.getCustOrderList(req, params);
		model.addAttribute("list", resList);
		return "common/orderPaperPop";
	}
	
	/**
	 * 전체주문현황 리스트 거래명세표
	 * @작성일 : 2020. 5. 4.
	 * @작성자 : an
	 */
	@RequestMapping(value="/front/base/salesOrderPaper", method={RequestMethod.GET, RequestMethod.POST})
	public String salesOrderPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("where", "front"); // admin/front
		model.addAttribute("where", "admin");
		
		// 거래내역
		final List<Map<String, Object>> list = orderSvc.getOrderPaper(params, loginDto);
		model.addAttribute("list", list);
		
		// 대표자 직인이미지
		String ceosealImg = Converter.toStr(configSvc.getConfigValue("CEOSEAL"));
		model.addAttribute("ceosealImg", ceosealImg);
		
		return "common/salesOrderPaper";
	}
	
	/**
	 * 공통 납품처선택 팝업 > 즐겨찾기 추가삭제 Ajax.
	 * @작성일 : 2020. 5. 8.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="/front/base/setShiptoBookmarkAjax")
	public Object setShiptoBookmarkAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		return customerSvc.setShiptoBookmarkAjax(params, req, loginDto);
	}
	
	/**
	 * 리포트 > 납품확인서 폼 > 납품처 리스트 가져오기 By O_SALESORDER Ajax.
	 * @작성일 : 2020. 6. 8.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/front/base/getShiptoListBySalesOrderAjax")
	public Object getShiptoListBySalesOrderAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return customerSvc.getShipToListBySalesOrder(params, req, loginDto);
	}
	
	/**
	 * 리포트 > 납품확인서 폼 > 착지주소 리스트 가져오기 By O_SALESORDER Ajax.
	 * @작성일 : 2020. 6. 8.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/front/base/getAdd1ListBySalesOrderAjax")
	public Object getAdd1ListBySalesOrderAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return customerSvc.getAdd1ListBySalesOrder(params, req, loginDto);
	}
	
	/**
	 * 리포트 > 납품확인서 폼 > 품목명 리스트 가져오기 By O_SALESORDER Ajax.
	 * @작성일 : 2020. 6. 8.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/front/base/getItemDescListBySalesOrderAjax")
	public Object getItemDescListBySalesOrderAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return customerSvc.getItemDescListBySalesOrder(params, req, loginDto);
	}

	
	/**
	 * 앱 푸시 알림 여부 가져오기 Ajax.
	 * @작성일 : 2020. 7. 3.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@RequestMapping(value="/front/base/app/getPushYNAjax", method=RequestMethod.POST)
	public Object getPushYNAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto) throws Exception {
		return userSvc.getPushYN(params, req, loginDto);
	}
	
	/**
	 * 앱 푸시 알림 여부 Y/N 업데이트 Ajax.
	 * @작성일 : 2020. 7. 3.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@RequestMapping(value="/front/base/app/updatePushYNAjax", method=RequestMethod.POST)
	public Object updatePushYNAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto) throws Exception {
		return userSvc.updatePushYN(params, req, loginDto);
	}
	
	/**
	 * 앱 푸시키 DB에 저장 및 세션에 set Ajax.
	 * @작성일 : 2020. 7. 2.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@RequestMapping(value="/front/base/app/setPushIdAjax", method=RequestMethod.POST)
	public Object setPushIdAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto) throws Exception {
		return userSvc.setPushId(params, req, loginDto);
	}
}
