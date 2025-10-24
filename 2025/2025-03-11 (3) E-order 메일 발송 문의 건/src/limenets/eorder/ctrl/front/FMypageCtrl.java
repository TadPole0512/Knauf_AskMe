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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.eorder.svc.BoardSvc;
import com.limenets.eorder.svc.CommonSvc;
import com.limenets.eorder.svc.CustomerSvc;
import com.limenets.eorder.svc.UserSvc;

/**
 * Front 마이페이지 컨트롤러.
 */
@Controller
@RequestMapping("/front/mypage/*")
public class FMypageCtrl {
	private static final Logger logger = LoggerFactory.getLogger(FMypageCtrl.class);
	
	@Inject private UserSvc userSvc;
	@Inject private CustomerSvc customerSvc;
	@Inject private CommonSvc commonSvc;
	@Inject private BoardSvc boardSvc;
	
	/**
	 * 나의정보 폼.
	 * @작성일 : 2020. 4. 2.
	 * @작성자 : an
	 */
	//@GetMapping(value="myInformation")
	@RequestMapping(value="myInformation", method={RequestMethod.GET, RequestMethod.POST})
	public String myInformation(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		model.addAttribute("user", userSvc.getUserOne(loginDto.getUserId()));
		
		model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));
		
		return "front/mypage/myInformation";
	}
	
	
	/**
	 * 나의정보 수정 Ajax.
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="myInformationUpdateAjax")
	public Object myInformationUpdateAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = userSvc.insertUpdateUser(params, req, loginDto);
		
		if(StringUtils.equals("0000", Converter.toStr(resMap.get("RES_CODE")))) {
			loginDto.setFirstLogin("N"); //개인정보 수정시 최초 로그인 해제
		}
		return resMap;
	}
	
	/**
	 * 자사정보관리
	 * @작성일 : 2020. 4. 7.
	 * @작성자 : an
	 * @수정일 : 2020. 5. 19
	 * @수정자 : lee
	 * @수정내용 : 고객 요청으로 FRONT 자사정보관리 사용하지 않음
	 */
	/*
	@GetMapping(value="customerList")
	public String customerList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		model.addAttribute("customer", customerSvc.getCustomer(loginDto.getCustCd()));
		return "front/mypage/customerList";
	}
	*/
	
	/**
	 * 거래처계정 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 4. 7.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="getCoUserListAjax")
	public Object getCsSalesUserListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return userSvc.getUserList(params, req, loginDto);
	}
	
	/**
	 * 납품처 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 4. 7.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="getShipToListAjax")
	public Object getShipToListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return customerSvc.getShipToList(params, req, loginDto);			
	}
	
	/**
	 * 자사정보관리 > 계정관리
	 * @작성일 : 2020. 4. 7.
	 * @작성자 : an
	 */
	@PostMapping(value="customerDetail")
	public String customerDetail(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		
		//계정리스트
		params.put("r_custcd", loginDto.getCustCd());
		params.put("r_authority", "CO");
		params.put("r_orderby", "USER_EORDER DESC");
		List<Map<String, Object>> userList = userSvc.getUserList(params);
		model.addAttribute("userList", userList);
		
		//납품처리스트
		params.put("r_custcd", loginDto.getCustCd());
		params.put("r_orderby", "ST.SHIPTO_CD DESC ");
		List<Map<String, Object>> shiptoList = customerSvc.getShipToList(params);
		model.addAttribute("shiptoList", shiptoList);
		
		return "front/mypage/customerDetail";
	}
	
	/**
	 * 계정관리상세 폼 > 팝업 폼. 
	 * {process} 정의.
	 * - addEditPop : 외부사용자 등록/수정 팝업 폼.
	 * - viewPop : 외부사용자 확인 팝업 폼.
	 * @param r_userid 수정할 사용자 아이디. [수정시에만 필수]
	 */
	@RequestMapping(value="/user/pop/{process}", method={RequestMethod.GET, RequestMethod.POST})
	public String userAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
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
		
		return "front/mypage/userAddEditPop";
	}
	
	/**
	 * 자사정보관리 상세 폼 > 팝업 폼 > 거래처계정 저장 또는 수정 Ajax.
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateUserAjax")
	public Object insertUpdateUserAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		return userSvc.insertUpdateUser(params, req, loginDto);
	}
	
	/**
	 * 계정 업데이트 Ajax.
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="updateUserAjax")
	public Object updateUserAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return userSvc.updateUserTransaction(params, req, loginDto);
	}
	
	/**
	 * 자사정보관리 > 납품처리스트 > 즐겨찾기 추가삭제 Ajax.
	 * @작성일 : 2020. 4. 9.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="setShiptoBookmarkAjax")
	public Object setShiptoBookmarkAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		return customerSvc.setShiptoBookmarkAjax(params, req, loginDto);
	}
	
}
