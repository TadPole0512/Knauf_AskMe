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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.util.Converter;
import com.limenets.eorder.svc.UserSvc;

/**
 * Admin 마이페이지 컨트롤러.
 */
@Controller
@RequestMapping("/admin/mypage/*")
public class MypageCtrl {
	private static final Logger logger = LoggerFactory.getLogger(MypageCtrl.class);

	@Inject private UserSvc userSvc;
	
	/**
	 * 조직설정 폼.
	 * @작성일 : 2020. 3. 10.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="csSalesUserList")
	public String csSalesUserList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String[] ri_authority = {"SH", "SM", "SR"};
		params.put("ri_authority", ri_authority);
		userSvc.getUserList(params);
		
		
		return "admin/mypage/csSalesUserList";
	}
	
	/**
	 * 조직설정 폼 > 영업사원 전체 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 10.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getCsSalesUserListAjax")
	public Object getCsSalesUserListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String[] ri_authority = {"SH", "SM", "SR"};
		params.put("ri_authority", ri_authority);
		params.put("r_csuserid", loginDto.getUserId());
		
		String[] rni_userid = {"19000101"}; // 비사용자그룹
		params.put("rni_userid", rni_userid);
		
		return userSvc.getUserList(params, req, loginDto);
	}
	
	/**
	 * 조직설정 폼 > 고정등록 저장 Ajax.
	 * @작성일 : 2020. 3. 11.
	 * @작성자 : kkyu
	 * @params
	 * @return
	 */
	@ResponseBody
	@PostMapping(value="updateFixedCsSalesMapAjax")
	public Object updateFixedCsSalesMapAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		return userSvc.updateFixedCsSalesMapTransaction(params, req, loginDto);
	}
	
	/**
	 * 조직설정 폼 > 임시저장 Ajax.
	 * @작성일 : 2020. 3. 10.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="insertCsSalesMapAjax")
	public Object insertCsSalesMapAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		return userSvc.insertCsSalesMapTransaction(params, req, loginDto);
	}
	
	/**
	 * 나의정보 폼.
	 * @작성일 : 2020. 3. 9.
	 * @작성자 : kkyu
	 */
	//@GetMapping(value="myInformationView")
	@RequestMapping(value="myInformationView", method={RequestMethod.GET, RequestMethod.POST})
	public String myInformationView(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		model.addAttribute("adminUser", userSvc.getUserOne(loginDto.getUserId()));
		return "admin/mypage/myInformationView";
	}
	
	/**
	 * 나의정보 폼 > 수정하기 Ajax.
	 * @작성일 : 2020. 3. 9.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="myInformationUpdateAjax")
	public Object myInformationUpdateAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
		params.put("r_userid", loginDto.getUserId());
		params.put("isMine", "Y");
		Map<String, Object> resMap = userSvc.insertUpdateAdminUser(params, req, loginDto);
		
		if(StringUtils.equals("0000", Converter.toStr(resMap.get("RES_CODE")))) {
			loginDto.setFirstLogin("N"); //개인정보 수정시 최초 로그인 해제
		}
		return resMap;
	}
	
}
