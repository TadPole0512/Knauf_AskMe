package com.limenets.eorder.ctrl.front;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.limenets.eorder.excel.ItemExcel;
import com.limenets.eorder.svc.BoardSvc;
import com.limenets.eorder.svc.CommonSvc;
import com.limenets.eorder.svc.ItemSvc;

/**
 * Front 품목 컨트롤러.
 */
@Controller
@RequestMapping("/front/item/*")
public class FItemCtrl {
	private static final Logger logger = LoggerFactory.getLogger(FItemCtrl.class);
	
	@Inject private CommonSvc commonSvc;
	@Inject private ItemSvc itemSvc;
	@Inject private BoardSvc boardSvc;
	
	/**
	 * 품목현황 리스트 폼.
	 * @작성일 : 2020. 3. 25.
	 * @작성자 : kkyu
	 */
	@RequestMapping(value="itemList", method={RequestMethod.GET, RequestMethod.POST})
	public String itemList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		
		params.put("where", "front");
		Map<String, Object> resMap = itemSvc.getItemList(params, req, loginDto);
		model.addAllAttributes(resMap);
		
		model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));

		
		return "front/item/itemList";
	}
	
	/**
	 * 품목현황 리스트 > 엑셀다운로드.
	 * @작성일 : 2020. 3. 26.
	 * @작성자 : kkyu
	 */
	@RequestMapping(value="itemExcelDown", method={RequestMethod.GET, RequestMethod.POST})
	public ModelAndView itemExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		params.put("where", "frontexcel");
		resMap.putAll(itemSvc.getItemList(params, req, loginDto));
		return new ModelAndView(new ItemExcel(), resMap);
	}
	
	/**
	 * 품목현황 리스트 > 즐겨찾기 저장/삭제 Ajax.
	 * @작성일 : 2020. 3. 26.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="setItemBookmarkAjax")
	public Object setItemBookmarkAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return itemSvc.setItemBookmarkTransaction(params, req, loginDto);
	}
	
	/**
	 * 품목현황 리스트 > 상세 팝업 폼.
	 * @작성일 : 2020. 3. 26.
	 * @작성자 : kkyu
	 */
	@RequestMapping(value="itemViewPop", method={RequestMethod.GET, RequestMethod.POST})
	public String itemViewPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_itemcd = Converter.toStr(params.get("r_itemcd"));
		model.addAttribute("item", itemSvc.getItemOne(r_itemcd));
		model.addAttribute("itemRecommendList", itemSvc.getItemRecommendList(r_itemcd, ""));
		return "front/item/itemViewPop";
	}
	
}
