package com.limenets.eorder.ctrl.admin;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.util.Converter;
import com.limenets.eorder.svc.CommonCodeSvc;
import com.limenets.eorder.svc.CommonSvc;
import com.limenets.eorder.svc.ConfigSvc;
import com.limenets.eorder.svc.CustomerSvc;
import com.limenets.eorder.svc.ItemSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.UserSvc;
import com.limenets.eorder.svc.ReportSvc;
import com.limenets.eorder.svc.SapRestApiSvc;

/**
 * Admin 공통 컨트롤러 > 로그인상태에서만  접근가능.
 */
@Controller
public class AdminBaseCtrl {
	private static final Logger logger = LoggerFactory.getLogger(AdminBaseCtrl.class);
	
	@Inject CommonSvc commonSvc;
	@Inject CommonCodeSvc commonCodeSvc;
	@Inject ConfigSvc configSvc;
	@Inject CustomerSvc customerSvc;
	@Inject ItemSvc itemSvc;
	@Inject OrderSvc orderSvc;
	@Inject ReportSvc reportSvc;
	@Inject UserSvc userSvc;
	@Inject SapRestApiSvc sapApiSvc;
	
	/**
	 * 엑셀 샘플파일 다운로드.
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="/admin/base/sampleFileDown")
	public ModelAndView sampleFileDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		return commonSvc.sampleFileDown(params, req, model, loginDto);
	}
	
	/**
	 * CKEditor 이미지 업로드.
	 * PC용.
	 */
	@PostMapping(value="/admin/base/editorFileUpload")
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
	@PostMapping(value="/admin/base/editorFileUploadForMobile")
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
	 * 내부 사용자 리스트 팝업 폼.
	 * @작성일 : 2020. 3. 11.
	 * @작성자 : kkyu
	 * @param ri_authority : 권한 ,로구분
	 * @param r_multiselect : 행 단위 선택여부 true/false
	 */
	@PostMapping(value="/admin/base/pop/adminUserListPop")
	public String adminUserAddEditViewPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String ri_authority = Converter.toStr(params.get("ri_authority"));
		String r_multiselect = Converter.toStr(params.get("r_multiselect"));
		model.addAttribute("ri_authority", ri_authority);
		model.addAttribute("r_multiselect", r_multiselect);
		
		if(StringUtils.isEmpty(ri_authority)) {
			String authoritys = "AD,CS,MK,SH,SM,SR";
			model.addAttribute("ri_authority", authoritys);
			model.addAttribute("r_multiselect", "true");
		}
		
		return "admin/base/adminUserListPop";
	}
	
	/**
	 * 내부 사용자 공통 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 5.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/getAdminUserListAjax")
	public Object getAdminUserListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String ri_authority = Converter.toStr(params.get("ri_authority"));
		if(!StringUtils.equals("", ri_authority)) params.put("ri_authority", ri_authority.split(",", -1));

		// adminUserListPop에서만, 영업사원 선택 팝업.
		String page_type = Converter.toStr(params.get("page_type"));
		if(StringUtils.equals("orderlist_salesuser", page_type)) {
			// 내부사용자 웹주문현황  > 별도 권한 설정.
			orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
		}
		
		return userSvc.getUserList(params, req, loginDto);
	}
	
	/**
	 * 내부 사용자 공통 업데이트 Ajax.
	 * @작성일 : 2020. 3. 5.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/updateUserAjax")
	public Object updateUserAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return userSvc.updateUserTransaction(params, req, loginDto);
	}
	
	/**
	 * 카테고리 단계별 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 16.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/getCategoryListAjax")
	public Object getCategoryListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.getCategoryList(params, req, loginDto);
	}
	
	/**
	 * 품목관리 리스트 팝업 폼.
	 * @작성일 : 2020. 3. 18.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="/admin/base/pop/itemListPop")
	public String itemListPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_multiselect = Converter.toStr(params.get("r_multiselect"));
		model.addAttribute("r_multiselect", r_multiselect);
		return "admin/base/itemListPop";
	}
	
	/**
	 * 품목관리 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 18.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/getItemListAjax")
	public Object getItemListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.getItemList(params, req, loginDto);
	}
	
	/**
	 * 주문 > 선택한 출고지(사업장) and 품목명으로 품목코드 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 4. 17.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/getItemMcuListAjax")
	public Object getItemMcuListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.getItemMcuList(params, req, loginDto);
	}
	
	/**
	 * 주문 > 선택한 출고지(사업장) and 품목코드로  재고 가져오기 Ajax.
	 * + 품목코드로 중량 가져오기.
	 * @작성일 : 2020. 4. 17.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/getItemStockAjax")
	public Object getItemStockAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		//return itemSvc.getItemStock(params, req, loginDto);

		/*System.out.println(params);
		Map<String, Object> pm = new HashMap<>();
		pm.put("PLANT", params.get("PLANT"));
		pm.put("MATERIAL", params.get("MATERIAL"));
		pm.put("UNIT", params.get("UNIT"));
		pm.put("CHECK_RULE", params.get("CHECK_RULE"));
		return sapApiSvc.getATPCheckList(pm);*/
		
		return sapApiSvc.getATPCheckList(params);
	}
	
	/**
	 * CS반려사유 팝업 폼.
	 * @작성일 : 2020. 4. 21.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="/admin/base/pop/orderReturnPop")
	public String orderReturnPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		model.addAttribute("cancelList", commonCodeSvc.getList("C01", "Y"));
		return "admin/base/orderReturnPop";
	}
	
	/**
	 * 거래처 리스트 팝업 폼.
	 * @작성일 : 2020. 3. 30.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="/admin/base/pop/customerListPop")
	public String customerListPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_multiselect = Converter.toStr(params.get("r_multiselect"));
		model.addAttribute("r_multiselect", r_multiselect);
		return "admin/base/customerListPop";
	}
	
	/**
	 * 거래처 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 30.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/getCustomerListAjax")
	public Object getCustomerListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		//String page_type = Converter.toStr(params.get("page_type"));
		/*
		if(StringUtils.equals("orderadd", page_type)) { // 주문등록 팝업인 경우, 영업사원 아이디 매핑.
			params.put("r_salesrepcd", loginDto.getUserId());
		}
		*/
		
		// 내부사용자 웹주문현황  > 별도 권한 설정.
		orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
		return customerSvc.getCustomerList(params, req, loginDto);
	}
	
	/**
	 * 납품처 리스트 팝업 폼.
	 * @작성일 : 2020. 3. 30.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="/admin/base/pop/shiptoListPop")
	public String shiptoListPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_multiselect = Converter.toStr(params.get("r_multiselect"));
		model.addAttribute("r_multiselect", r_multiselect);
		
		Object o = params.get("rl_quoteqt");
		if(o != null)
			return "admin/base/shiptoqtListPop";
			
		return "admin/base/shiptoListPop";
	}
	
	/**
	 * 납품처 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 30.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/getShiptoListAjax")
	public Object getShiptoListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String page_type = Converter.toStr(params.get("page_type"));
		if(StringUtils.equals("orderadd", page_type)) { // 주문등록 팝업인 경우, 거래처 매핑.
			params.put("r_custcd", params.get("r_custcd"));
		}
		return customerSvc.getShipToList(params, req, loginDto);
	}
	
	/**
	 * 납품확인서 폼 > 납품처 리스트 가져오기 By O_SALESORDER Ajax.
	 * @작성일 : 2020. 6. 1.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/getShiptoListBySalesOrderAjax")
	public Object getShiptoListBySalesOrderAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return customerSvc.getShipToListBySalesOrder(params, req, loginDto);
	}
	
	/**
	 * 납품확인서 폼 > 착지주소 리스트 가져오기 By O_SALESORDER Ajax.
	 * @작성일 : 2020. 6. 2.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/getAdd1ListBySalesOrderAjax")
	public Object getAdd1ListBySalesOrderAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return customerSvc.getAdd1ListBySalesOrder(params, req, loginDto);
	}
	
	/**
	 * 납품확인서 폼 > 품목명 리스트 가져오기 By O_SALESORDER Ajax.
	 * @작성일 : 2020. 6. 4.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/getItemDescListBySalesOrderAjax")
	public Object getItemDescListBySalesOrderAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return customerSvc.getItemDescListBySalesOrder(params, req, loginDto);
	}
	
	/**
	 * O_CUST_ORDER_Header 주소록 폼.
	 * @작성일 : 2020. 4. 1.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="/admin/base/pop/orderAddressBookmarkPop")
	public String orderAddressBookmarkPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/base/orderAddressBookmarkPop";
	}
	
	/**
	 * O_CUST_ORDER_Header 주소록 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 4. 1.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/getOrderAddressBookmarkAjax")
	public Object getOrderAddressBookmarkAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("r_oabuserid", loginDto.getUserId());
		return orderSvc.getOrderAddressBookmark(params, req, loginDto);
	}
	
	/**
	 * O_CUST_ORDER_Header 주소록 삭제하기 Ajax.
	 * @작성일 : 2020. 4. 1.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/pop/deleteOrderAddressBookmarkAjax")
	public Object deleteOrderAddressBookmarkAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("r_oabuserid", loginDto.getUserId());
		return orderSvc.deleteOrderAddressBookmark(params, req, loginDto);
	}
	
	/**
	 * 비밀번호 변경요청 페이지 
	 */
	//@PostMapping(value="/admin/base/userPswdEdit")
	@RequestMapping(value="/admin/base/userPswdEdit", method={RequestMethod.GET, RequestMethod.POST})
	public String userPswdEditPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "/admin/base/userPswdEdit";
	}
	
	/**
	 * 자재주문서 출력 팝업 폼.
	 * @작성일 : 2020. 5. 1.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="/admin/base/orderPaperPop")
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
	@PostMapping(value="/admin/base/salesOrderPaper")
	public String salesOrderPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("where", "admin"); // admin/front
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
	 * 품목현황 팝업 > 즐겨찾기 저장/삭제 Ajax.
	 * @작성일 : 2020. 3. 26.
	 * @작성자 : kkyu
	 * @수정일 : 2020. 05. 21.
	 * @수정자 : isaac
	 * @수정내용 : ADMIN 품목팝업에 즐겨찾기 기능 추가 요청
	 */
	@ResponseBody
	@PostMapping(value="/admin/base/setItemBookmarkAjax")
	public Object setItemBookmarkAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.setItemBookmarkTransaction(params, req, loginDto);
	}
	
	
//	@ResponseBody
//	@PostMapping(value="/admin/base/getsTransactionSupplierAjax")
//	public Object getsTransactionSupplierAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
//		return sapApiSvc.getTransactionSupplier(params);
//	}
}
