package com.limenets.eorder.ctrl.common;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.limenets.common.dto.LoginDto;
import com.limenets.eorder.svc.TestSvc;

/**
 * Test 컨트롤러.
 */
@Controller
@RequestMapping("/test/*")
public class TestCtrl {
	private static final Logger logger = LoggerFactory.getLogger(TestCtrl.class);
	
	@Inject private TestSvc testSvc;
	
	/****************************************************************************************************************************************************
	 * Start. Test For Velocity 
	 ****************************************************************************************************************************************************/
	@GetMapping(value="/vtlByFile")
	public String vtlByFile(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String textBody = testSvc.createTempleteByFile(params);	
		req.setAttribute("resultAjax", textBody);
		return "textAjax";
	}
	@GetMapping(value="/vtlByString")
	public String vtlByString(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String textBody = testSvc.createTempleteByString(params);	
		req.setAttribute("resultAjax", textBody);
		return "textAjax";
	}
	/****************************************************************************************************************************************************
	 * End. Test For Velocity 
	 ****************************************************************************************************************************************************/

	
	/**
	 *
	 * @작성일 : 2020. 2. 14.
	 * @작성자 : kkyu
	 * @params
	 * @return
	 */
	@GetMapping(value="/test1")
	public String test1(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		model.addAttribute("list", testSvc.getTestList(params));
		return "test/test1";
	}
	
	@PostMapping(value="/insertTestByForm")
	public String insertTestByForm(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		testSvc.insertTestTransaction(params, loginDto);
		return "redirect:/test/test1.lime";
	}
	
	@ResponseBody
	@PostMapping(value="/insertTestByAjax")
	public Object insertTestByAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		resMap.putAll(testSvc.insertTestTransaction(params, loginDto));
		resMap.put("list", testSvc.getTestList(params));
		return resMap;
	}
	
	/**
	 *
	 * @작성일 : 2020. 2. 14.
	 * @작성자 : kkyu
	 * @params
	 * @return
	 */
	@GetMapping(value="/test2")
	public String test2(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "test/test2";
	}
	
	/**
	 *
	 * @작성일 : 2020. 2. 14.
	 * @작성자 : kkyu
	 * @params
	 * @return
	 */
	@GetMapping(value="/test3")
	public String test3(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "test/test3";
	}
	
}
