package com.limenets.eorder.ctrl.front;

//import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

//import com.google.gson.Gson;
import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
//import com.limenets.common.util.LimitOrder;
import com.limenets.common.util.MailUtil;
import com.limenets.eorder.excel.FSalesOrderExcel;
import com.limenets.eorder.excel.OrderExcel;
//import com.limenets.eorder.excel.SalesOrderExcel;
import com.limenets.eorder.excel.SalesOrderItemExcel;
import com.limenets.eorder.excel.SalesOrderMainExcel;
import com.limenets.eorder.svc.BoardSvc;
//import com.limenets.eorder.svc.CommonCodeSvc;
import com.limenets.eorder.svc.CommonSvc;
import com.limenets.eorder.svc.ConfigSvc;
import com.limenets.eorder.svc.CustomerSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.PostalCodeSvc;
import com.limenets.eorder.svc.TempleteSvc;
//import com.limenets.eorder.svc.UserSvc;
import com.limenets.eorder.util.StatusUtil;

/**
 * Front 주문 컨트롤러.
 */
@Controller
@RequestMapping("/front/order/*")
public class FOrderCtrl {
    private static final Logger logger = LoggerFactory.getLogger(FOrderCtrl.class);
    
    //@Inject private CommonCodeSvc commonCodeSvc;
    @Inject private CommonSvc commonSvc;
    @Inject private ConfigSvc configSvc;
    @Inject private CustomerSvc customerSvc;
    @Inject private OrderSvc orderSvc;
    //@Inject private UserSvc userSvc;
    @Inject private TempleteSvc templeteSvc;
    @Inject private PostalCodeSvc postalCodeSvc;
    @Inject private BoardSvc boardSvc;
    
    @Value("${https.url}") private String httpsUrl;
    @Value("${mail.smtp.sender.addr}") private String smtpSender;
    @Value("${shop.name}") private String shopName;
    @Value("${mail.smtp.url}") private String smtpHost;
    @Value("${kakao.map.app.key}") private String kakaoMapAppKey;
    
    /**
	 * 웹주문현황 리스트 폼.
	 * @작성일 : 2020. 4. 3.
	 * @작성자 : kkyu
	 */
	@RequestMapping(value="orderList", method={RequestMethod.GET, RequestMethod.POST})
	public String itemList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		
		
		// 주문일 세팅.
		String today = Converter.dateToStr("yyyy-MM-dd");
		String r_insdate = Converter.toStr(params.get("r_insdate"));
		String r_inedate = Converter.toStr(params.get("r_inedate"));
		logger.debug("r_insdate : {}", r_insdate);
		logger.debug("r_inedate : {}", r_inedate);
		if(StringUtils.equals("", r_insdate)) {
			params.put("r_insdate", today);
			model.addAttribute("insdate", today);
		}
		else {
			params.put("r_insdate", r_insdate);
			model.addAttribute("insdate", r_insdate);
		}
		
		if(StringUtils.equals("", r_inedate)) {
			params.put("r_inedate", today);
			model.addAttribute("inedate", today);
		}
		else {
			params.put("r_inedate", r_inedate);
			model.addAttribute("inedate", r_inedate);
		}
		
		params.put("where", "front");
		params.put("r_custcd", loginDto.getCustCd());
		if(!StringUtils.equals("CO", loginDto.getAuthority())) {
			params.put("r_shiptocd", loginDto.getShiptoCd());
		}
		
		Map<String, Object> resMap = orderSvc.getOrderHeaderList(params, req, loginDto);
		model.addAllAttributes(resMap);
		
		model.addAttribute("orderStatus", StatusUtil.ORDER.getMap()); // 주문상태 Map형태로 가져오기.
		
		String[] removeStatusArr = {"99"}; //임시저장
		model.addAttribute("orderStatusList", StatusUtil.ORDER.getList(removeStatusArr)); // 주문상태 List<Map>형태로 가져오기.
		
		model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));
		
		return "front/order/orderList";
	}
	
	/**
	 * 웹주문현황 리스트 폼 > 주문품목수 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 4. 27.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getOrderDetailListAjax")
	public Object getOrderDetailListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("where", "front");
		params.put("r_custcd", loginDto.getCustCd());
		params.put("r_shiptocd", loginDto.getShiptoCd());
		logger.debug("r_custcd : {}", loginDto.getCustCd());
		logger.debug("r_shiptocd : {}", loginDto.getShiptoCd());
		
		return orderSvc.getCustOrderDetailList(params, req, loginDto);
	}
	
	/**
	 * 웹주문현황 리스트 폼 > 주문상태값 변경 Ajax.
	 * @작성일 : 2020. 4. 27.
	 * @작성자 : kkyu
	 * @param r_reqno [필수]
	 * @param m_statuscd [필수] 변경할 상태값 : 90=임시저장 삭제, 01=고객취소
	 */
	@ResponseBody
	@PostMapping(value="updateStatusAjax")
	public Object updateStatusAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("where", "front");
		String m_statuscd = Converter.toStr(params.get("m_statuscd"));
		
		if(StringUtils.equals("01", m_statuscd)) { // 고객취소.
			return orderSvc.insertOrderConfirmTransaction(params, req, loginDto);
		}
		else if(StringUtils.equals("90", m_statuscd)) { // 임시저장 삭제.
			return orderSvc.deleteTempSaveTransaction(params, req, loginDto);
		}
		
		return MsgCode.getResultMap(MsgCode.DATA_STATUS_ERROR); // 처리 할 수 없는 상태입니다.
	}
	
	/**
	 * 주문등록 폼.
	 * @작성일 : 2020. 4. 3.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="orderAdd")
	public String orderAdd(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		
		String copyReqNo = Converter.toStr(params.get("copy_reqno")); // 다른 주문건의 주문번호를 받아와 그 주문정보를 그대로 입력한다.
		
		String reqNo = Converter.toStr(params.get("r_reqno")); // 임시저장 상태의 건을 수정 또는 주문접수로 넘길때 파라미터.
		
		//String userId = loginDto.getUserId();
		String custCd = loginDto.getCustCd();
		String shiptoCd = loginDto.getShiptoCd();
		String authority = loginDto.getAuthority(); // CO=거래처,CT=납풉처. 
		
		//params.put("where", "front");
		//params.put("r_custcd", loginDto.getCustCd());
		//if(!StringUtils.equals("CO", loginDto.getAuthority())) {
		//	params.put("r_shiptocd", loginDto.getShiptoCd());
		//}
		
		// 웹주문현황 리스트 폼에서 복사 폼.
		if(!StringUtils.equals("", copyReqNo)) {
			Map<String, Object> custOrderH = orderSvc.getCustOrderHOne(loginDto, copyReqNo);
			if(CollectionUtils.isEmpty(custOrderH)) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
			
			model.addAttribute("pageType", "COPY");
			model.addAttribute("custOrderH", custOrderH);
			model.addAttribute("custOrderD", orderSvc.getCustOrderDList(copyReqNo, custCd, shiptoCd, "LINE_NO ASC ", 0, 0));
		}
		
		// 임시저장(99) 상태에서 수정 폼.
		// 주문접수(00) 상태에서 수정 폼 이동 추가.
		if(!StringUtils.equals("", reqNo)) {
			String m_statuscd = Converter.toStr(params.get("m_statuscd"));
			logger.debug("m_statuscd : {}", m_statuscd);
			
			Map<String, Object> custOrderH = orderSvc.getCustOrderHOne(loginDto, reqNo);
			if(CollectionUtils.isEmpty(custOrderH)) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
			
			// 접근가능한 상태인지 체크.
			if(!StatusUtil.ORDER.statusCheck(Converter.toStr(custOrderH.get("STATUS_CD")), m_statuscd)) { 
				req.setAttribute("resultAjax", "<script>alert('"+MsgCode.DATA_STATUS_ERROR.getMessage()+"'); history.back();</script>");
				//req.setAttribute("resultAjax", "<script>window.open('about:blank','_self').close(); alert('"+MsgCode.DATA_STATUS_ERROR.getMessage()+"');</script>");
				return "textAjax";
			}
			
			model.addAttribute("pageType", "EDIT");
			model.addAttribute("custOrderH", custOrderH);
			model.addAttribute("custOrderD", orderSvc.getCustOrderDList(reqNo, custCd, shiptoCd, "LINE_NO ASC ", 0, 0));
		}else {
			model.addAttribute("pageType", "ADD");
			if(StringUtils.equals("CT", authority)) model.addAttribute("shipto", customerSvc.getShipTo(shiptoCd));
		}
		
		model.addAttribute("todayDate", Converter.dateToStr("yyyy-MM-dd"));
		model.addAttribute("orderStatus", StatusUtil.ORDER.getMap()); // 주문상태 Map형태로 가져오기.
		model.addAttribute("orderStatusToJson", StatusUtil.ORDER.getMapToJson()); // 주문상태 JSON형태로 가져오기.
		
		model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));
		
		return "front/order/orderAdd";
	}
	
	/**
	 * 웹주문상세 폼.
	 * @작성일 : 2020. 4. 28.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="orderView")
	public String orderView(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		
		String reqNo = Converter.toStr(params.get("r_reqno"));
		if(StringUtils.equals("", reqNo)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		
		//String userId = loginDto.getUserId();
		String custCd = loginDto.getCustCd();
		String shiptoCd = loginDto.getShiptoCd();
		//String authority = loginDto.getAuthority(); // CO=거래처,CT=납풉처. 
		
		//params.put("where", "front");
		//params.put("r_custcd", loginDto.getCustCd());
		//if(!StringUtils.equals("CO", loginDto.getAuthority())) {
		//	params.put("r_shiptocd", loginDto.getShiptoCd());
		//}
		
		// Get O_CUST_ORDER_H One & O_CUST_ORDER_D List.
		Map<String, Object> custOrderH = orderSvc.getCustOrderHOne(loginDto, reqNo);
		if(CollectionUtils.isEmpty(custOrderH)) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
		
		model.addAttribute("custOrderH", custOrderH);
		model.addAttribute("custOrderD", orderSvc.getCustOrderDList(reqNo, custCd, shiptoCd, "LINE_NO ASC ", 0, 0));
		
		// Get O_CONFIRM_ORDER_D List.
		List<Map<String, Object>> orderConfirmD = orderSvc.getOrderConfirmDList(reqNo, "", "", 0, 0);
		model.addAttribute("orderConfirmD", orderConfirmD);
		
		model.addAttribute("todayDate", Converter.dateToStr("yyyy-MM-dd"));
		model.addAttribute("orderStatus", StatusUtil.ORDER.getMap()); // 주문상태 Map형태로 가져오기.
		model.addAttribute("orderStatusToJson", StatusUtil.ORDER.getMapToJson()); // 주문상태 JSON형태로 가져오기.
		return "front/order/orderView";
	}
	
	/**
	 * 최근에 주문한 주문 한 건(O_CUST_ORDER_Header) 가져오기 Ajax.
	 * @작성일 : 2020. 4. 7.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getRecentOrderAjax")
	public Object getRecentOrderAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return orderSvc.getRecentCustOrderHeader(params, req, loginDto);
	}
	
	/**
	 * O_CUST_ORDER 임시저장(99) 및 주문접수(00) 처리 Ajax.
	 * @작성일 : 2020. 4. 7.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="insertCustOrderAjax")
	public Object insertCustOrderAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("where", "front");
		return orderSvc.insertCustOrderTransaction(params, req, loginDto);
	}
	
	/**
	 * 전체주문현황 리스트 폼.
	 * = 거래장(주문) 리스트 폼과 동일.
	 * @작성일 : 2020. 4. 24.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="salesOrderList")
	public String salesOrderList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		
		//model.addAttribute("salesOrderStatus", StatusUtil.SALESORDER.getMap()); // 출하상태 Map형태로 가져오기.
		//model.addAttribute("salesOrderStatusToJson", StatusUtil.SALESORDER.getMapToJson()); // 출하상태 JSON형태로 가져오기.
		model.addAttribute("salesOrderStatusList", StatusUtil.SALESORDER.getList()); // 출하상태 List<Map>형태로 가져오기.
		
		// 주문일 세팅.
		String today = Converter.dateToStr("yyyy-MM-dd");
		model.addAttribute("ordersdt", today);
		model.addAttribute("orderedt", today);
		
		model.addAttribute("kakaoMapAppKey", kakaoMapAppKey);
		
		model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));
		
		return "front/order/salesOrderList";
	}
	
	/**
	 * 거래장(주문) 리스트 폼.
	 * = 전체주문현황 리스트 폼과 동일.
	 * @작성일 : 2020. 4. 25.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="salesOrderMainList")
	public String salesOrderMainList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		
		//model.addAttribute("salesOrderStatus", StatusUtil.SALESORDER.getMap()); // 출하상태 Map형태로 가져오기.
		//model.addAttribute("salesOrderStatusToJson", StatusUtil.SALESORDER.getMapToJson()); // 출하상태 JSON형태로 가져오기.
		//model.addAttribute("salesOrderStatusList", StatusUtil.SALESORDER.getList()); // 출하상태 List<Map>형태로 가져오기.
		
		// 주문일 세팅.
		String today = Converter.dateToStr("yyyy-MM-dd");
		model.addAttribute("actualshipsdt", today);
		model.addAttribute("actualshipedt", today);
		
		return "front/order/salesOrderMainList";
	}
	
	/**
	 * 전체주문현황 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 4. 24.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="getSalesOrderListAjax")
	public Object getSalesOrderListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("where", "front");
		
		params.put("r_custcd", loginDto.getCustCd());
		if(!StringUtils.equals("CO", loginDto.getAuthority())) {
			params.put("r_shiptocd", loginDto.getShiptoCd());
		}
		
		return orderSvc.getSalesOrderList(params, req, loginDto);
	}
	
	/**
	 * 거래장(품목) 리스트 폼.
	 * @작성일 : 2020. 4. 25.
	 * @작성자 : kkyu
	 */
	@RequestMapping(value="salesOrderItemList", method={RequestMethod.GET, RequestMethod.POST})
	public String salesOrderItemList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		
		params.put("r_custcd", loginDto.getCustCd());
		if(!StringUtils.equals("CO", loginDto.getAuthority())) {
			params.put("r_shiptocd", loginDto.getShiptoCd());
		}
		
		// 주문일 세팅.
		String today = Converter.dateToStr("yyyyMMdd");
		//String today = Converter.dateToStr("yyyy-MM-dd");
		String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt"));
		String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt"));
		logger.debug("r_actualshipsdt : {}", r_actualshipsdt);
		logger.debug("r_actualshipedt : {}", r_actualshipedt);
		if(StringUtils.equals("", r_actualshipsdt)) {
			params.put("r_actualshipsdt", today);
			model.addAttribute("actualshipsdt", today);
		}
		else {
			params.put("r_actualshipsdt", r_actualshipsdt);
			model.addAttribute("actualshipsdt", r_actualshipsdt);
		}
		
		if(StringUtils.equals("", r_actualshipedt)) {
			params.put("r_actualshipedt", today);
			model.addAttribute("actualshipedt", today);
		}
		else {
			params.put("r_actualshipedt", r_actualshipedt);
			model.addAttribute("actualshipedt", r_actualshipedt);
		}
		
		// 리스트 가져오기.
		Map<String, Object> resMap = orderSvc.getSalesOrderItemList(params, req, loginDto);
		model.addAllAttributes(resMap);
		
		return "front/order/salesOrderItemList";
	}
	
	/**
     * 전체주문현황 리스트 > 엑셀다운로드.
     * @작성일 : 2020. 4. 26.
     * @작성자 : an
     */
    @PostMapping(value="salesOrderExcelDown")
    public ModelAndView salesOrderExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        // Start. 뷰단에서 넘어온 파일 쿠키 put.
        String fileToken = Converter.toStr(params.get("filetoken"));
        resMap.put("fileToken", fileToken);
        // End.
        
        params.put("r_custcd", loginDto.getCustCd());
        if(!StringUtils.equals("CO", loginDto.getAuthority())) {
            params.put("r_shiptocd", loginDto.getShiptoCd());
        }
        
        params.put("where", "frontexcel");
        resMap.putAll(orderSvc.getSalesOrderList(params, req, loginDto));
        return new ModelAndView(new FSalesOrderExcel(), resMap);
    }
	
	/**
	 * 웹주문현황 > 거래장(주문) > 엑셀다운로드
	 * @작성일 : 2020. 4. 26.
	 * @작성자 : an
	 */
	@PostMapping(value="salesOrderMainExcelDown")
	public ModelAndView salesOrderMainExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		params.put("r_custcd", loginDto.getCustCd());
		if(!StringUtils.equals("CO", loginDto.getAuthority())) {
			params.put("r_shiptocd", loginDto.getShiptoCd());
		}
		
		params.put("where", "frontexcel");
		resMap.putAll(orderSvc.getSalesOrderList(params, req, loginDto));
		return new ModelAndView(new SalesOrderMainExcel(), resMap);
	}
	
	/**
	 * 웹주문현황 > 거래장(품목) > 엑셀다운로드
	 * @작성일 : 2020. 4. 26.
	 * @작성자 : an
	 */
	@PostMapping(value="salesOrderItemExcelDown")
	public ModelAndView salesOrderItemExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		params.put("r_custcd", loginDto.getCustCd());
		if(!StringUtils.equals("CO", loginDto.getAuthority())) {
			params.put("r_shiptocd", loginDto.getShiptoCd());
		}
		
		// 주문일 세팅.
		String today = Converter.dateToStr("yyyyMMdd");
		//String today = Converter.dateToStr("yyyy-MM-dd");
		String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt"));
		String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt"));
		logger.debug("r_actualshipsdt : {}", r_actualshipsdt);
		logger.debug("r_actualshipedt : {}", r_actualshipedt);
		if(StringUtils.equals("", r_actualshipsdt)) {
			params.put("r_actualshipsdt", today);
			model.addAttribute("actualshipsdt", today);
		}
		else {
			params.put("r_actualshipsdt", r_actualshipsdt);
			model.addAttribute("actualshipsdt", r_actualshipsdt);
		}
		
		if(StringUtils.equals("", r_actualshipedt)) {
			params.put("r_actualshipedt", today);
			model.addAttribute("actualshipedt", today);
		}
		else {
			params.put("r_actualshipedt", r_actualshipedt);
			model.addAttribute("actualshipedt", r_actualshipedt);
		}
		
		params.put("where", "frontexcel");
		resMap.putAll(orderSvc.getSalesOrderItemList(params, req, loginDto));
		return new ModelAndView(new SalesOrderItemExcel(), resMap);
	}
	
	/**
	 * 웹주문현황 > 엑셀다운로드
	 * @작성일 : 2020. 4. 26.
	 * @작성자 : an
	 */
	@PostMapping(value="orderExcelDown")
	public ModelAndView orderExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		// Start. 뷰단에서 넘어온 파일 쿠키 put.
		String fileToken = Converter.toStr(params.get("filetoken"));
		resMap.put("fileToken", fileToken);
		// End.
		
		// 주문상태 Map형태로 가져오기.
		resMap.put("orderStatus", StatusUtil.ORDER.getMap());
		
		params.put("r_custcd", loginDto.getCustCd());
		if(!StringUtils.equals("CO", loginDto.getAuthority())) {
			params.put("r_shiptocd", loginDto.getShiptoCd());
		}
		
		params.put("where", "frontexcel");
		resMap.putAll(orderSvc.getOrderHeaderList(params, req, loginDto));
		return new ModelAndView(new OrderExcel(), resMap);
	}
	
	/**
	 * 모바일 상세팝업
	 * @작성일 : 2020. 5. 11.
	 * @작성자 : an
	 */
	@RequestMapping(value="salesOrderViewPop", method={RequestMethod.GET, RequestMethod.POST})
	public String salesOrderViewPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> salesOrder = orderSvc.getSalesOrder(params);
		model.addAttribute("salesOrder", salesOrder);
		return "front/order/salesOrderViewPop";
	}
    
    /**
     * OK.
     * O_CUST_ORDER 임시저장(99) 시 사전입력 저장
     * @작성일 : 2020. 6. 22.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsFirstOrderAjax")
    public Object insertQmsTempOrderAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        return orderSvc.setQmsFirstOrderAjax(params, req, loginDto);
    }
    
    /**
     * QMS 조회 및 등록.
     * @작성일 : 2021. 3. 29.
     * @작성자 : jihye lee
     */
    @RequestMapping(value="qmsOrderList")
    public String qmsOrderList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        //model.addAttribute("salesOrderStatus", StatusUtil.SALESORDER.getMap()); // 출하상태 Map형태로 가져오기.
        //model.addAttribute("salesOrderStatusToJson", StatusUtil.SALESORDER.getMapToJson()); // 출하상태 JSON형태로 가져오기.
        //model.addAttribute("qmlStatusList", StatusUtil.QMS.getList()); // QMS 상태 List<Map>형태로 가져오기.
        
        // 주문일 세팅.
        // jsh 시작일자 -30일 기본값 적용
        Date toDayDate = Converter.dateAdd(new Date(),5,-90);
        String toDay = Converter.dateToStr("yyyy-MM-dd",toDayDate);
        String fromDay = Converter.dateToStr("yyyy-MM-dd");
        model.addAttribute("ordersdt", toDay);
        model.addAttribute("orderedt", fromDay);
        
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        List<Map<String, Object>> orderYearList = orderSvc.getQmsOrderYearList(params, req);
        List<Map<String, Object>> releaseYearList = orderSvc.getQmsReleaseYearList(params, req);
        
        model.addAttribute("orderYearList", orderYearList);
        model.addAttribute("releaseYearList", releaseYearList);
        
        model.addAttribute("kakaoMapAppKey", kakaoMapAppKey);
        
        return "front/order/qmsOrderList";
    }   
    
    /**
     * 전체 QMS 리스트 가져오기 Ajax.
     * @작성일 : 2021. 3. 29.
     * @작성자 : jihye lee
     */
    @ResponseBody
    @PostMapping(value="getQmsOrderListAjax")
    public Object getQmsItemListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);

        if(loginDto.getCustCd()!=null) {
            params.put("r_custcd", loginDto.getCustCd());
        }
        if(loginDto.getShiptoCd()!=null) {
            //params.put("r_shiptocd", loginDto.getShiptoCd());
        }
        return orderSvc.getQmsOrderList(params, req, loginDto);
    }   
    
    /**
     * 전체 QMS 디테일 항목 리스트 가져오기 Ajax.
     * @작성일 : 2021. 3. 29.
     * @작성자 : jihye lee
     */
    @ResponseBody
    @PostMapping(value="getQmsPopDetlGridList")
    public Object getQmsPopDetlGridList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        
        //페이징 무력화
        params.put("page", null);
        params.put("rows", null);
        List<Map<String, Object>> list = orderSvc.getQmsPopDetlGridList(params);
        
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        return list;
    }   
    
   
    
    
    
    /**
     * QMS 입력 팝업 
     * @작성일 : 2021. 4. 26.
     * @작성자 : jsh
     */
    @RequestMapping(value="qmsOrderPop" ,method= {RequestMethod.GET,RequestMethod.POST})
    public Object orderPaperPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        String qmsId = (String) params.get("qmsId");
        String qmsSeq = (String) params.get("qmsSeq");
        String work = params.get("work") != null? (String) params.get("work") :null;
        
        // QMS 시퀀스와 함께 입력된 경우 처리
        if(qmsId!=null && qmsId.indexOf("-") > 0) {
            String[] qmsArr = qmsId.split("-");
            params.put("qmsId", qmsArr[0]);
            params.put("qmsSeq", qmsArr[1]);
        }else {
            //기본 qms 시퀀스 입력
            params.put("qmsSeq",params.get("qmsSeq")!=null?params.get("qmsSeq"):1);
        }
        
        List<Map<String, Object>> getQmsPopMastList = orderSvc.getQmsPopMastList(params);
        model.addAttribute("qmsMastList", getQmsPopMastList);
        
        Map<String, Object> tempMast = null;
        if(getQmsPopMastList.size() > 0) {
            tempMast = getQmsPopMastList.get(0);
            model.addAttribute("createUser", tempMast.get("CREATEUSER"));
            model.addAttribute("qmsSplitYn", tempMast.get("QMS_SPLIT_YN"));
        }else {
            model.addAttribute("qmsSplitYn",'N');
        }
        
        List<Map<String, Object>> getQmsPopDetlList = orderSvc.getQmsPopDetlList(params);
        model.addAttribute("qmsDetlList", getQmsPopDetlList);
        
        List<Map<String, Object>> getQmsFireproofList = orderSvc.getQmsFireproofList(params);
        model.addAttribute("qmsFireproofList", getQmsFireproofList);
        
        model.addAttribute("qmsId" , params.get("qmsId"));
        model.addAttribute("qmsSeq", params.get("qmsSeq"));
        model.addAttribute("work"  , params.get("work"));
        
        return "front/order/qmsOrderPop";
    }
    
    /**
     * QMS 미리보기 팝업 
     * @작성일 : 2021. 4. 26.
     * @작성자 : jsh
     */
    @RequestMapping(value="qmsOrderPopView" ,method= {RequestMethod.GET,RequestMethod.POST})
    public Object qmsOrderPopView(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        String qmsId = (String) params.get("qmsId");
        // QMS 시퀀스와 함께 입력된 경우 처리
        if(qmsId.indexOf("-") > 0) {
            String[] qmsArr = qmsId.split("-");
            params.put("qmsId", qmsArr[0]);
            params.put("qmsSeq", qmsArr[1]);
        }else {
          //기본 qms 시퀀스 입력
          params.put("qmsSeq",params.get("qmsSeq")!=null?params.get("qmsSeq"):1);
        }
        
        List<Map<String, Object>> getQmsPopMastList = orderSvc.getQmsPopMastList(params);
        model.addAttribute("qmsMastList", getQmsPopMastList);
        
        Map<String, Object> tempMast = null;
        if(getQmsPopMastList.size() > 0) {
            tempMast = getQmsPopMastList.get(0);
            model.addAttribute("createUser", tempMast.get("CREATEUSER"));
            model.addAttribute("qmsSplitYn", tempMast.get("QMS_SPLIT_YN"));
        }else {
            model.addAttribute("qmsSplitYn",'N');
        }
        
        List<Map<String, Object>> getQmsPopDetlList = orderSvc.getQmsPopDetlList(params);
        model.addAttribute("qmsDetlList", getQmsPopDetlList);
        
        List<Map<String, Object>> getQmsFireproofList = orderSvc.getQmsFireproofList(params);
        model.addAttribute("qmsFireproofList", getQmsFireproofList);
        
        model.addAttribute("qmsId", params.get("qmsId"));
        model.addAttribute("qmsSeq", params.get("qmsSeq"));
        return "front/order/qmsOrderPopView";
    }
    
    
    
    /**
     * QMS ID 채번 Ajax.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="getQmsOrderId")
    public Object getQmsOrderId(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        return orderSvc.getQmsOrderId(params);
    }
    
    /**
     * QMS Master 입력 Ajax.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderMast")
    public Object setQmsOrderMast(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        
        params.put("userId", loginDto.getUserId());
        
        if(params.get("deleteYN")==null) {
            params.put("deleteYN", "T");
        }
        
        return orderSvc.setQmsOrderMast(params);
    }  
    
    /**
     * QMS Master 현장분할 Ajax.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderMastSplit")
    public Object setQmsOrderMastSplit(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        
        params.put("userId", loginDto.getUserId());
        
        //int result1 = orderSvc.setQmsOrderDetlClear(params);
        //int result2 = orderSvc.setQmsOrderMastClear(params);
        
        int result3 = orderSvc.setQmsOrderMastSplit(params);
        int result4 = orderSvc.setQmsOrderDetlSplit(params);
        return true;
    }  
    
    /**
     * QMS Detail 입력 Ajax.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderDetl")
    public Object setQmsOrderDetl(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        
        params.put("userId", loginDto.getUserId());
        
        if(params.get("deleteYN")==null) {
            params.put("deleteYN", "T");
        }
        
        return orderSvc.setQmsOrderDetl(params);
    }
    
    /**
     * QMS Master 임시저장 Ajax.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderMastTempUpdate")
    public Object setQmsOrderMastTempUpdate(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        String userId = loginDto.getUserId();
        params.put("userId", userId);
        
        //MAST SAVE 
        return orderSvc.setQmsOrderMastTempUpdate(params);
    }
    
    /**
     * QMS Detail 임시저장 Ajax.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderDetlTempUpdate")
    public Object setQmsOrderDetlTempUpdate(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        String userId = loginDto.getUserId();
        params.put("userId", userId);
        
        //DETAIL SAVE 
        return orderSvc.setQmsOrderDetlTempUpdate(params);
    }  
    
    /**
     * QMS Master 입력 Ajax.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderMastUpdate")
    public Object setQmsOrderMastUpdate(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        String userId = loginDto.getUserId();
        params.put("userId", userId);
        
        Object shiptoCd = params.get("shiptoCd");
        if(shiptoCd == null || (shiptoCd != null && shiptoCd.equals(""))) {
            Map<String, Object> shiptoCdResult = orderSvc.getQmsCorpShiptoCd(params);
            shiptoCd = shiptoCdResult.get("SHIPTO_CD");
            params.put("shiptoCd", shiptoCd);
        }
        
        //MAST HISTORY
        orderSvc.setQmsOrderMastHistory(params);
        //MAST SAVE 
        return orderSvc.setQmsOrderMastUpdate(params);
    }  
    
    /**
     * QMS Master 삭제 Ajax.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderMastDelete")
    public Object setQmsOrderMastDelete(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        String userId = loginDto.getUserId();
        params.put("userId", userId);
        
        orderSvc.setQmsOrderDetlDelete(params);
        
        //MAST SAVE 
        return orderSvc.setQmsOrderMastDelete(params);
    }  
    
    /**
     * QMS Detail 입력 Ajax.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderDetlUpdate")
    public Object setQmsOrderDetlUpdate(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        
        params.put("userId", loginDto.getUserId());
        return orderSvc.setQmsOrderDetlUpdate(params);
    }
    

    /**
     * QMS Detail Fireproof Ajax. 초기화
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderFireproofInit")
    public Object setQmsOrderFireproofInit(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        
        params.put("userId", loginDto.getUserId());
        return orderSvc.setQmsOrderFireproofInit(params);
    }
    
    /**
     * QMS Detail Fireproof Ajax.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderFireproofUpdate")
    public Object setQmsOrderFireproofUpdate(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        
        params.put("userId", loginDto.getUserId());
        return orderSvc.setQmsOrderFireproofUpdate(params);
    }
    
    
    /**
     * QMS 이메일 화면 
     * @작성일 : 2021. 4. 26.
     * @작성자 : jsh
     */
    @RequestMapping(value="qmsOrderPopEmail" ,method= {RequestMethod.GET,RequestMethod.POST})
    public Object qmsOrderPopEmail(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        String qmsIdTxt = "";
        String pageType = null;
        
        if(params.get("page_type")!= null) {
            pageType = params.get("page_type").toString();
        }
        
        // Email 단건발송 일괄발송 체크
        if(pageType.equals("qmsOrderPopEmail")) {
            if(params.get("qmsIdTxt")!= null) {
                qmsIdTxt = params.get("qmsIdTxt").toString();
            }
            
            String[] qmsIdTxtArr = qmsIdTxt.split(",");
            
            params.put("userId", loginDto.getUserId());
            model.addAttribute("qmsIdTxt", qmsIdTxt);
            
        } else if(pageType.equals("orderAllEmailPop")) {
            
            //email 미발송 건만 리스트에 추가
            params.put("qms_status2","N");
            
            List<Map<String, Object>> qmsEmailList = orderSvc.getQmsPopList(params);
            for(int i = 0; i < qmsEmailList.size(); i++) {
                qmsIdTxt += qmsEmailList.get(i).get("QMS_ORD_NO");
                if(i != qmsEmailList.size()-1) {
                    qmsIdTxt += ",";
                }
            }
            
            String[] qmsIdTxtArr = qmsIdTxt.split(",");
            
            params.put("userId", loginDto.getUserId());
            model.addAttribute("qmsIdTxt", qmsIdTxt);
        }
        
        return "front/order/qmsOrderPopEmail";
    }
    
    /**
     * QMS 이메일 그리드 조회
     * @작성일 : 2021. 4. 26.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="getQmsPopEmailGridList")
    public Object getQmsPopEmailGridList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        String qmsIdTxt = "";
        if(params.get("qmsIdTxt")!= null) {
            qmsIdTxt = params.get("qmsIdTxt").toString();
        }
        String[] qmsIdTxtArr = qmsIdTxt.split(",");
        
        params.put("qmsIdArr", qmsIdTxtArr);
        return orderSvc.getQmsPopEmailGridList(params);
    }
    
    
    /**
     * QMS 이메일 발송 Ajax.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsMailLog")
    public Object setQmsMailLog(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
     // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        String r_shiptorepNm = Converter.toStr(params.get("shiptorepNm"));
        String r_shiptoEmail = Converter.toStr(params.get("shiptoEmail"));
        String r_qmsId = Converter.toStr(params.get("qmsId"));
        String r_qmsSeq = Converter.toStr(params.get("qmsSeq"));
        
        String protocol = "";
        if(req.isSecure()) {
            protocol="https";
        }else {
            protocol="http";
        }
        
        // 메일보내기
        Map<String, Object> resMap = configSvc.getConfigList(params);
        String title = Converter.toStr(resMap.get("MAILTITLE"));
        String url =  protocol+"://"+ req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath(); //httpsUrl
        String qmsReportUrl = url + "/front/report/qmsReport.lime?qmsId=" + r_qmsId + "-" + r_qmsSeq;
        String mailBottomImg = url + "/data/config/" + Converter.toStr(resMap.get("MAILBOTTOMIMG"));
        
        String contentStr = templeteSvc.qmsReportEmail(qmsReportUrl, url, mailBottomImg);
        
        MailUtil mail = new MailUtil();
        mail.sendMail(smtpHost, title, r_shiptorepNm, r_shiptoEmail, shopName, smtpSender, contentStr, null, "");
        
        params.put("userId", loginDto.getUserId());
        int result = orderSvc.setQmsMailLog(params);
        return true;
    }  
    
    
    /**
     * 현장명 팝업 폼.
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @PostMapping(value="/qmsCorpListPop")
    public String customerListPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        String r_multiselect = Converter.toStr(params.get("r_multiselect"));
        model.addAttribute("r_multiselect", r_multiselect);
        
        model.addAttribute("qmsId" ,params.get("qmsId"));
        model.addAttribute("qmsSeq",params.get("qmsSeq"));
        return "front/order/qmsCorpListPop";
    }
    
    /**
     * 거래처 리스트 가져오기 Ajax.
     * @작성일 : 2021. 5. 10.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="/front/order/getQmsCorpList")
    public Object getQmsCorpList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        return orderSvc.getQmsCorpList(params, req, loginDto);
    }
    
    
    /**
     * QMS 파일업로드 화면 
     * @작성일 : 2021. 4. 26.
     * @작성자 : jsh
     */
    @RequestMapping(value="qmsOrderPopFile" ,method= {RequestMethod.GET,RequestMethod.POST})
    public Object qmsOrderPopFile(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        String qmsIdTxt = null;
        String pageType = null;
        
        if(params.get("page_type")!= null) {
            pageType = params.get("page_type").toString();
        }
        
        // 단일 선택 업로드
        if(pageType.equals("qmsOrderPopFile")) {
            if(params.get("qmsIdTxt")!= null) {
                qmsIdTxt = params.get("qmsIdTxt").toString();
            }
            
            String[] qmsIdTxtArr = qmsIdTxt.split(",");
            
            params.put("qmsIdArr", qmsIdTxtArr);
            
            List<Map<String, Object>> qmsFileList = orderSvc.getQmsPopList(params);
            model.addAttribute("qmsFileList", qmsFileList);
        } else if(pageType.equals("qmsOrderPopAllFile")) {
            List<Map<String, Object>> qmsFileList = orderSvc.getQmsPopList(params);
            model.addAttribute("qmsFileList", qmsFileList);
        }
        
        return "front/order/qmsOrderPopFile";
    }
    
    /**
     * QMS 파일업로드 저장
     * @작성일 : 2021. 4. 26.
     * @작성자 : jsh
     */
    @PostMapping(value="qmsOrderPopFileSave")
    public String qmsOrderPopFileSave(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = orderSvc.qmsOrderPopFileTransaction(params, req, loginDto);
        String resMsg = Converter.toStr(resMap.get("RES_MSG"));
        req.setAttribute("resultAjax", "<script>opener.parent.dataSearch(); alert('"+resMsg+"'); self.close();</script>");
        return "textAjax";
    }
    
    /**
     * QMS 파일 다운로드.
     * @작성일 : 2021. 4. 26.
     * @작성자 : jsh
     */
    @PostMapping(value="qmsOrderFileDown")
    public ModelAndView qmsOrderFileDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
        return orderSvc.qmsOrderFileDown(params, req, model, loginDto);
    }
    
    /**
     * QMS 파일 일괄다운로드.
     * @작성일 : 2021. 4. 26.
     * @작성자 : jsh
     */
    @PostMapping(value="qmsOrderFileAllDown")
    public ModelAndView qmsOrderFileAllDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
        List<Map<String, Object>> qmsFileList = orderSvc.getQmsPopList(params);
        List<String> files = new ArrayList<String>();
        for(int i = 0; i < qmsFileList.size(); i++) {
            Map<String, Object> qmsFile = qmsFileList.get(i);
            if(qmsFile.get("QMS_FILE_NM") != null) {
                files.add(qmsFile.get("QMS_ORD_NO").toString() + ";" + qmsFile.get("QMS_FILE_NM").toString());
            }
        }
        return orderSvc.qmsOrderFileAllDown(params, req, model, loginDto, files);
    }
    
    /**
     * QMS 수량체크.
     * @작성일 : 2021. 5. 15.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="/front/order/getQmsOrderQtyCheck")
    public Object getQmsOrderQtyCheck(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        //orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        String qmsId = params.get("QMS_ORD_NO").toString();
        // QMS 시퀀스와 함께 입력된 경우 처리
        if(qmsId.indexOf("-") > 0) {
            String[] qmsArr = qmsId.split("-");
            params.put("qmsId", qmsArr[0]);
            params.put("qmsSeq", qmsArr[1]);
        }else {
            //기본 qms 시퀀스 입력
            params.put("qmsSeq",params.get("qmsSeq")!=null?params.get("qmsSeq"):1);
        }
        
        return orderSvc.getQmsOrderQtyCheck(params);
    }
    
    /**
     * QMS 임시수량 초기화.
     * @작성일 : 2021. 5. 15.
     * @작성자 : jsh
     */
    @ResponseBody
    @RequestMapping(value="/front/order/setQmsOrderTempReset")
    public Object setQmsOrderMastTempReset(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        //orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        return orderSvc.setQmsOrderTempReset(params);
    }
    

    /**
     * QMS 일괄등록 중복체크.
     * @작성일 : 2021. 5. 24.
     * @작성자 : jsh
     */
    @ResponseBody
    @RequestMapping(value="/front/order/getQmsOrderDupCheck")
    public Object getQmsOrderDupCheck(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        //orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        return orderSvc.getQmsOrderDupCheck(params, req, loginDto); 
    }
    
    /**
     * QMS 일괄등록.
     * @작성일 : 2021. 5. 24.
     * @작성자 : jsh
     */
    @ResponseBody
    @RequestMapping(value="/front/order/setQmsOrderAllAdd")
    public Object setQmsOrderAllAdd(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        //orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        return orderSvc.setQmsOrderAllAdd(params, req, loginDto); 
    }
    
    /**
     * 납품처 리스트 가져오기 Ajax.
     * @작성일 : 2020. 5. 30.
     * @작성자 : kkyu
     */
    @ResponseBody
    @PostMapping(value="/front/order/getShiptoListAjax")
    public Object getShiptoListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        return orderSvc.getShipToList(params, req, loginDto);
    }
    
    
    /**
     * QMS 삭제.
     * @작성일 : 2021. 5. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="/front/order/setQmsOrderRemove")
    public Object setQmsOrderRemove(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        String qmsIdTxt = null;
        if(params.get("qmsIdTxt")!= null) {
            qmsIdTxt = params.get("qmsIdTxt").toString();
        }
        String[] qmsIdTxtArr = qmsIdTxt.split(",");
        
        params.put("qmsIdArr", qmsIdTxtArr);
        
        return orderSvc.setQmsOrderRemove(params);
    }
    
    /**
     * QMS 리포트 건수 체크
     * @작성일 : 2021. 06. 21.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="getQmsOrderCnt")
    public Object getQmsReportCheck(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        return orderSvc.getQmsOrderCnt(params);
    }
    
    /**
     * 조회조건의 거래처 납품처 오더 중복체크
     * @작성일 : 2021. 5. 24.
     * @작성자 : jsh
     */
    @ResponseBody
    @RequestMapping(value="/front/order/getOrderDupCheck")
    public Object getOrderDupCheck(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        //orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        return orderSvc.getOrderDupCheck(params, req, loginDto); 
    }
    
    /**
     * 메일 일괄발송 거래처 중복체크
     * @작성일 : 2021. 5. 24.
     * @작성자 : jsh
     */
    @ResponseBody
    @RequestMapping(value="/front/order/getOrderCustDupCheck")
    public Object getOrderCustDupCheck(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        //orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        return orderSvc.getOrderDupCheck(params, req, loginDto); 
    }
    
    
    /**
     * QMS 사전 입력 팝업 
     * @작성일 : 2021. 6. 21.
     * @작성자 : jsh
     */
    @RequestMapping(value="qmsOrderPrePop" ,method= {RequestMethod.GET,RequestMethod.POST})
    public Object qmsOrderPrePop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        String qmsTempId = (String) params.get("qmsTempId");
        params.put("qmsTempId", qmsTempId);
        model.addAttribute("qmsTempId", qmsTempId);
        
        List<Map<String, Object>> getQmsPopPreMastList = orderSvc.getQmsPopPreMastList(params);
        model.addAttribute("qmsMastList", getQmsPopPreMastList);
        
        List<Map<String, Object>> getQmsPopPreDetlList = orderSvc.getQmsPopPreDetlList(params);
        model.addAttribute("qmsDetlList", getQmsPopPreDetlList);
        
        List<Map<String, Object>> getQmsPreFireproofList = orderSvc.getQmsPreFireproofList(params);
        model.addAttribute("qmsFireproofList", getQmsPreFireproofList);
        
        return "front/order/qmsOrderPrePop";
    }
    
    /**
     * 사전입력 QMS 디테일 항목 리스트 가져오기 Ajax.
     * @작성일 : 2021. 3. 29.
     * @작성자 : jihye lee
     */
    @ResponseBody
    @PostMapping(value="getQmsPopPreDetlGridList")
    public Object getQmsPopPreDetlGridList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        params.put("where", "admin");
        
        //페이징 무력화
        params.put("page", null);
        params.put("rows", null);
        List<Map<String, Object>> list = orderSvc.getQmsPopPreDetlList(params);
        
        
        return list;
    }
    
    
    /**
     * QMS Master 사전 입력 Ajax.
     * @작성일 : 2021. 6. 21.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderPreMastUpdate")
    public Object setQmsOrderPreMastUpdate(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        String userId = loginDto.getUserId();
        params.put("userId", userId);
        
        //MAST HISTORY
        orderSvc.setQmsOrderMastHistory(params);
        
        //MAST SAVE 
        return orderSvc.setQmsOrderPreMastUpdate(params);
    }
    
    
    /**
     * QMS Detail 사전입력 Ajax. 수량 수정 및 분할기능 없음
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderPreDetlUpdate")
    public Object setQmsOrderPreDetlUpdate(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        
        params.put("userId", loginDto.getUserId());
        return orderSvc.setQmsOrderPreDetlUpdate(params);
    }
    
    
    /**
     * QMS Detail Fireproof Ajax. 사전입력시 초기화
     * @작성일 : 2021. 6. 21.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderPreFireproofInit")
    public Object setQmsOrderPreFireproofInit(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        
        params.put("userId", loginDto.getUserId());
        return orderSvc.setQmsOrderPreFireproofInit(params);
    }
    
    /**
     * QMS Detail Fireproof Ajax. 내화구조 사전입력
     * @작성일 : 2021. 4. 29.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsOrderPreFireproofUpdate")
    public Object setQmsOrderPreFireproofUpdate(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        
        params.put("userId", loginDto.getUserId());
        return orderSvc.setQmsOrderPreFireproofUpdate(params);
    }
    
    /**
     * OK.
     * O_CUST_ORDER 임시저장(99) 시 사전입력 취소
     * @작성일 : 2020. 7. 12.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsFirstOrderCancelAjax")
    public Object setQmsFirstOrderCancelAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        return orderSvc.setQmsFirstOrderCancelAjax(params);
    }
    /**
     * QMS 사전입력 삭제(취소).
     * @작성일 : 2022. 12. 4.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsPreOrderRemove")
    public Object setQmsPreOrderRemove(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        return orderSvc.setQmsPreOrderRemove(params);
    }
    
    @ResponseBody
    @PostMapping(value="getPostalCodeCount")
    public Map<String, Object> getPostalCodeCount(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        return postalCodeSvc.getPostalCodeCount(params);
    }
}
