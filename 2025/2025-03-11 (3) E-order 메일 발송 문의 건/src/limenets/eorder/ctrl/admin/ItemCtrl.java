package com.limenets.eorder.ctrl.admin;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
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
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.util.Converter;
import com.limenets.eorder.excel.ItemExcel;
import com.limenets.eorder.svc.ItemSvc;

/**
 * Admin 품목 컨트롤러.
 */
@Controller
@RequestMapping("/admin/item/*")
public class ItemCtrl {
	private static final Logger logger = LoggerFactory.getLogger(ItemCtrl.class);

	@Inject private ItemSvc itemSvc;
	
	/**
	 * OK !!!
	 * 품목관리 리스트 폼.
	 * @작성일 : 2020. 3. 13.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="itemList")
	public String itemList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/item/itemList";
	}
	
	/**
	 * OK !!!
	 * 품목관리 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 13.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getItemListAjax")
	public Object getItemListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.getItemList(params, req, loginDto);
	}
	
	@ResponseBody
	@PostMapping(value="getItemManageListAjax")
	public Object getItemManageListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.getItemManageList(params, req, loginDto);
	}
	
	/**
	 * OK !!!
	 * itemExcelDown => 품목관리 리스트 > 엑셀다운로드.
	 * itemExcelDown2 => 품목일괄관리 > 품목코드 다운로드.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : kkyu
	 */
	@PostMapping(value= {"itemExcelDown", "itemExcelDown2"})
	public ModelAndView itemExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		params.put("where", "excel");
		resMap.putAll(itemSvc.getItemList(params, req, loginDto));
		return new ModelAndView(new ItemExcel(), resMap);
	}
	
	/**
	 * OK !!!
	 * 품목관리 리스트 > 확인 팝업 폼.
	 * @작성일 : 2020. 3. 13.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="itemViewPop")
	public String itemViewPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_itemcd = Converter.toStr(params.get("r_itemcd"));
		model.addAttribute("item", itemSvc.getItemOne(r_itemcd));
		model.addAttribute("itemRecommendList", itemSvc.getItemRecommendList(r_itemcd, ""));
		return "admin/item/itemViewPop";
	}
	
	/**
	 * OK !!!
	 * 품목관리 리스트 > 수정 폼.
	 * @작성일 : 2020. 3. 13.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="itemEdit")
	public String itemEdit(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_itemcd = Converter.toStr(params.get("r_itemcd"));
		model.addAttribute("item", itemSvc.getItemOne(r_itemcd));
		model.addAttribute("itemRecommendList", itemSvc.getItemRecommendList(r_itemcd, ""));
		return "admin/item/itemEdit";
	}
	
	/**
	 * OK
	 * 품목 파일 다운로드.
	 * @작성일 : 2020. 3. 19.
	 * @작성자 : kkyu
	 */
	@PostMapping(value="itemFileDown")
	public ModelAndView itemFileDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model) throws Exception {
		ModelAndView mv = itemSvc.itemFileDown(params, req, model);
		
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
	 * OK !!!
	 * 품콕관리 리스트 > 일괄관리 : 엑셀대량수정 폼.
	 * @작성일 : 2020. 3. 13.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="itemEditExcel")
	public String itemEditExcel(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/item/itemEditExcel";
	}
	
	/**
	 * OK !!!
	 * 품목 수정 폼 > 업데이트 Ajax.
	 * @작성일 : 2020. 3. 13.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="updateItemAjax")
	public Object updateItemAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.updateItemTransaction(params, req, loginDto);
	}

	/**
	 * 품목 대량 엑셀 수정 폼 > 업데이트 Ajax
	 * @작성일 : 2020. 4. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="updateItemExcelAjax")
	public Object updateItemExcelAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.updateItemExcelTransaction(params, req, loginDto);
	}

	/**
	 * 품목리스트 > SORT 수정 Ajax
	 * @작성일 : 2020. 5. 12.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="updateItemInfoSortAjax")
	public Object updateItemInfoSortAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.updateItemInfoSort(params,req,loginDto);
	}

	/**
	 * 품목리스트 > SORT 수정 Ajax
	 * @작성일 : 2020. 5. 12.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="updateLineTyAjax")
	public Object updateLineTyAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.updateLineTy(params,req,loginDto);
	}
}
