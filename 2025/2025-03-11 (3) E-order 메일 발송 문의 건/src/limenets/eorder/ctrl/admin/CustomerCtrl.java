package com.limenets.eorder.ctrl.admin;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.eorder.excel.customerExcel;
import com.limenets.eorder.svc.CustomerSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.UserSvc;

/**
 * Admin 거래처 컨트롤러.
 */
@Controller
@RequestMapping("/admin/customer/*")
public class CustomerCtrl {
	private static final Logger logger = LoggerFactory.getLogger(CustomerCtrl.class);
	
	@Inject private CustomerSvc customerSvc;
	@Inject private OrderSvc orderSvc;
	@Inject private UserSvc userSvc;
	
	/**
	 * 거래처 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 10.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getCustomerListAjax")
	public Object getCustomerListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("where", "excel");
		return customerSvc.getCustomerList(params, req, loginDto);
	}
	
	/**
	 * 거래처 리스트.
	 * @작성일 : 2020. 3. 12.
	 * @작성자 : an
	 */
	@GetMapping(value="customerList")
	public String customerList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/customer/customerList";
	}
	
	/**
	 * 거래처 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 13.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="getCustomerListPagerAjax")
	public Object getCustomerListPagerAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		// 내부사용자 웹주문현황  > 별도 권한 설정.
		orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
		return customerSvc.getCustomerList(params, req, loginDto);
	}
	
	/**
	 * 거래처 폼 > 거래처계정 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 12.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="getCoUserListAjax")
	public Object getCsSalesUserListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return userSvc.getUserList(params, req, loginDto);
	}
	
	
	/**
	 * 거래처 폼 > 납품처 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 12.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="getShipToListAjax")
	public Object getShipToListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return customerSvc.getShipToList(params, req, loginDto);			
	}
	
	
	/**
	 * 거래처 계정관리 상세.
	 * @작성일 : 2020. 3. 13.
	 * @작성자 : an
	 */
	@GetMapping(value="customerDetail")
	public String customerDetail(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_custcd = Converter.toStr(params.get("r_custcd"));
		if (StringUtils.equals("", r_custcd)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR, "거래처코드");
		
		// 내부사용자 웹주문현황  > 별도 권한 설정.
		orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
		model.addAttribute("customer", customerSvc.getCustomer(params));
		
		return "admin/customer/customerDetail";
	}
	
	
	/**
	 * 계정관리상세 폼 > 팝업 폼. 
	 * {process} 정의.
	 * - addEditPop : 외부사용자 등록/수정 팝업 폼.
	 * - viewPop : 외부사용자 확인 팝업 폼.
	 * @param r_userid 수정할 사용자 아이디. [수정시에만 필수]
	 */
	@PostMapping(value="/user/pop/{process}")
	public String adminUserAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_userid = Converter.toStr(params.get("r_userid"));
		String page_type = (StringUtils.equals("addEditPop", process)) ? "ADD" : "VIEW";
		
		if(!StringUtils.equals("", r_userid)) { // 수정
			Map<String, Object> userMap = userSvc.getUserOne(r_userid); 
			if(CollectionUtils.isEmpty(userMap)) {
				throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
			}
			model.addAttribute("userMap", userMap);
			page_type = (StringUtils.equals("addEditPop", process)) ? "EDIT" : "VIEW";
		}
		
		model.addAttribute("page_type", page_type); // return ADD/EDIT/VIEW.
		
		return "admin/customer/userAddEditPop";
	}
	
	
	
	/**
	 * 거래처현황 상세 폼 > 팝업 폼 > 거래처계정 저장 또는 수정 Ajax.
	 * @작성일 : 2020. 3. 13.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateUserAjax")
	public Object insertUpdateUserAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		return userSvc.insertUpdateUser(params, req, loginDto);
	}
	
	/**
	 * 거래처계정 업데이트 Ajax.
	 * @작성일 : 2020. 3. 18.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="updateUserAjax")
	public Object updateUserAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return userSvc.updateUserTransaction(params, req, loginDto);
	}
	
	/**
	 * 거래처현황 상세 폼 > 납품처 계정 수정 Ajax.
	 * @작성일 : 2020. 3. 18.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="updateShiptoUserAjax")
	public Object updateShiptoUserAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		return userSvc.updateShiptoUser(params, req, loginDto);
	}
	
	/**
	 * 거래처 엑셀다운로드
	 * @작성일 : 2020. 4. 24.
	 * @작성자 : an
	 */
	@PostMapping(value="customerExcelDown")
	public ModelAndView customerExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		params.put("where", "excel");
		resMap.putAll(customerSvc.getCustomerList(params, req, loginDto));
		return new ModelAndView(new customerExcel(), resMap);
	}
	
}
