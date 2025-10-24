package com.limenets.eorder.ctrl.admin;

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
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.util.Converter;
import com.limenets.eorder.excel.SendMailHistoryExcel;
import com.limenets.eorder.excel.factReportExcel;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.ReportSvc;
import com.limenets.eorder.util.StatusUtil;

/**
 * Admin 리포트 컨트롤러.
 */
@Controller
@RequestMapping("/admin/report/*")
public class ReportCtrl {
	private static final Logger logger = LoggerFactory.getLogger(ReportCtrl.class);

	@Inject private OrderSvc orderSvc;
	@Inject private ReportSvc reportSvc;
	
	/**
	 * 납품확인서 폼.
	 * @작성일 : 2020. 5. 4.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="deliveryReport")
	public String deliveryReport(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String today = Converter.dateToStr("yyyy-MM-dd");
		model.addAttribute("insdate", today);
		model.addAttribute("inedate", today);
		
		return "admin/report/deliveryReport";
	}
	
	/**
	 * 납품확인서 폼 > 전체주문현황 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 5. 4.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getDeliveryReportListAjax")
	public Object getSalesOrderListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("where", "admin");
		params.put("ri_status2", "560"); // 출하완료 기준.
		
		// 내부사용자 웹주문현황  > 별도 권한 설정.
		//orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
		
		return orderSvc.getSalesOrderList(params, req, loginDto);
	}
	
	/**
	 * 거래사실확인서
	 * @작성일 : 2020. 4. 14.
	 * @작성자 : an
	 */
	@GetMapping(value="factReport")
	public String customerList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "admin/report/factReport";
	}
	
	/**
	 * 거래사실확인서 > 엑셀다운로드
	 * @작성일 : 2020. 4. 16.
	 * @작성자 : an
	 */
	@PostMapping(value="factReportExcelDown")
	public ModelAndView factReportExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// 공급자,공급받는자
		resMap.put("supplier", reportSvc.getVsupplier(params, req, loginDto));
		
		// 거래내역
		resMap.put("salesOrderList", reportSvc.getVclosedSalesOrder(params));
		
		// 전월채권,당월채권,현금수금,어음수금, 당월채권
		resMap.put("sumPrice", reportSvc.getvSumPrice(params));
		
		// 미도래어음
		resMap.put("failPrice", reportSvc.getFailPrice(params, req, loginDto));
		
		// 거래처 입금계좌
		resMap.put("vAccount", reportSvc.getAccount(params, req, loginDto));
		
		return new ModelAndView(new factReportExcel(), resMap);
	}
	
	/**
	 * 거래사실확인서 메일전송 Ajax.
	 */
	@ResponseBody
	@PostMapping(value="factReportSendMailAjax")
	public Object factReportSendMailAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = reportSvc.factReportSendMail(params, req, loginDto);
		return resMap;
	}
	
	/**
	 * 거래사실확인서 - 이메일 전송기록 엑셀다운로드
	 * @작성일 : 2020. 4. 21.
	 * @작성자 : an
	 */
	@PostMapping(value="sendMailHistoryExcelDown")
	public ModelAndView sendMailHistoryExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// 주문상태 Map형태로 가져오기.
		resMap.put("orderStatus", StatusUtil.ORDER.getMap());
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		resMap.put("list", reportSvc.sendMailHistoryList(loginDto.getUserId()));
		return new ModelAndView(new SendMailHistoryExcel(), resMap);
	}
	
	/**
	 * 자재납품 확인서 폼.
	 * @작성일 : 2020. 5. 6.
	 * @작성자 : kkyu
	 * @param ri_orderno : O_SALESORDER.ORDERNO
	 * @param paper_type : 10=개별아이템-현장, 20=개별아이템-착지, 11=아이템별-현장, 21=아이템별-현장
	 */
	@PostMapping(value="deliveryPaperPop")
	public String deliveryPaperPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		reportSvc.getDeliveryPaper2(params, req, loginDto, model);
		//reportSvc.getDeliveryPaper(params, req, loginDto, model);
		return "common/deliveryPaperPop";
	}
	
	
}
