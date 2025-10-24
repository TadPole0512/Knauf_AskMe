package com.limenets.eorder.ctrl.admin;

//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import com.google.gson.Gson;
import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
//import com.limenets.common.util.LimitOrder;
import com.limenets.common.util.MailUtil;
import com.limenets.eorder.excel.OrderExcel;
import com.limenets.eorder.excel.QmsOrderExcel;
import com.limenets.eorder.excel.SalesOrderExcel;
import com.limenets.eorder.svc.CommonCodeSvc;
import com.limenets.eorder.svc.CommonSvc;
import com.limenets.eorder.svc.ConfigSvc;
import com.limenets.eorder.svc.CustomerSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.PostalCodeSvc;
import com.limenets.eorder.svc.TempleteSvc;
import com.limenets.eorder.svc.UserSvc;
import com.limenets.eorder.util.StatusUtil;

/**
 * Admin 주문 컨트롤러.
 */
@Controller
@RequestMapping("/admin/order/*")
public class OrderCtrl {
    private static final Logger logger = LoggerFactory.getLogger(OrderCtrl.class);
    
    @Inject private CommonCodeSvc commonCodeSvc;
    @Inject private CommonSvc commonSvc;
    @Inject private ConfigSvc configSvc;
    @Inject private CustomerSvc customerSvc;
    @Inject private OrderSvc orderSvc;
    @Inject private UserSvc userSvc;
    @Inject private TempleteSvc templeteSvc;
    @Inject private PostalCodeSvc postalCodeSvc;
    
    @Value("${https.url}") private String httpsUrl;
    @Value("${mail.smtp.sender.addr}") private String smtpSender;
    @Value("${shop.name}") private String shopName;
    @Value("${mail.smtp.url}") private String smtpHost;
    @Value("${operation.type}") private String operationType;
    
    /**
     * OK.
     * 주문등록 폼.
     * @작성일 : 2020. 3. 12.
     * @작성자 : kkyu
     */
    @GetMapping(value="orderAdd")
    public String orderAdd(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        String reqNo = Converter.toStr(params.get("r_reqno"));
        String status = Converter.toStr(params.get("status"));
        //String userId = loginDto.getUserId();
        
        // 임시저장(99) 상태에서 수정 폼.
        if(!StringUtils.equals("", reqNo) && status.equals("99")) {
            //데이터 쿼리날려서 조회하는부분
            Map<String, Object> custOrderH = orderSvc.getCustOrderHOne(reqNo, "", "99");
            if(CollectionUtils.isEmpty(custOrderH)) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);           
            model.addAttribute("pageType", "EDIT");
            model.addAttribute("custOrderH", custOrderH);
            model.addAttribute("custOrderD", orderSvc.getCustOrderDList(reqNo, Converter.toStr(custOrderH.get("CUST_CD")), Converter.toStr(custOrderH.get("SHIPTO_CD")), "LINE_NO ASC ", 0, 0));
        // 주문등록 후(00) 수정 폼.
        }else if(!StringUtils.equals("", reqNo) && status.equals("00")){
            Map<String, Object> custOrderH = orderSvc.getCustOrderHOne(reqNo);
            if(CollectionUtils.isEmpty(custOrderH)) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
            model.addAttribute("pageType", "EDIT");
            model.addAttribute("custOrderH", custOrderH);
            model.addAttribute("custOrderD", orderSvc.getCustOrderDList(reqNo, Converter.toStr(custOrderH.get("CUST_CD")), Converter.toStr(custOrderH.get("SHIPTO_CD")), "LINE_NO ASC ", 0, 0));
        }else {
            model.addAttribute("pageType", "ADD");
        }
        
        model.addAttribute("todayDate", Converter.dateToStr("yyyy-MM-dd"));
        model.addAttribute("orderStatus", StatusUtil.ORDER.getMap()); // 주문상태 Map형태로 가져오기.
        model.addAttribute("orderStatusToJson", StatusUtil.ORDER.getMapToJson()); // 주문상태 JSON형태로 가져오기.
        return "admin/order/orderAdd";
    }
    
    /**
     * OK.
     * O_CUST_ORDER_Detail 리스트 가져오기 Ajax.
     * @작성일 : 2020. 3. 27.
     * @작성자 : kkyu
     * @param r_pagetype : ADD/EDIT/VIEW
     */
    @ResponseBody
    @PostMapping(value="getCustOrderDetailListAjax")
    public Object getCustOrderDetailListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "orderadd");
        
        String r_pagetype = Converter.toStr(params.get("r_pagetype"));
        if(StringUtils.equals("ADD", r_pagetype)) return null;
        return orderSvc.getCustOrderDetailList(params, req, loginDto);
    }
    
    /**
     * OK.
     * 최근에 주문한 주문 한 건(O_CUST_ORDER_Header) 가져오기 Ajax.
     * @작성일 : 2020. 3. 31.
     * @작성자 : kkyu
     */
    @ResponseBody
    @PostMapping(value="getRecentOrderAjax")
    public Object getRecentOrderAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        return orderSvc.getRecentCustOrderHeader(params, req, loginDto);
    }
    
    /**
     * OK.
     * O_CUST_ORDER 임시저장(99) 및 주문접수(00) 처리 Ajax.
     * @작성일 : 2020. 4. 1.
     * @작성자 : kkyu
     */
    @ResponseBody
    @PostMapping(value="insertCustOrderAjax")
    public Object insertCustOrderAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        return orderSvc.insertCustOrderTransaction(params, req, loginDto);
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
     * OK.
     * O_CUST_ORDER 임시저장(99) 시 사전입력 취소
     * @작성일 : 2020. 7. 12.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="setQmsFirstOrderCancelAjax")
    public Object setQmsFirstOrderCancelAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        return orderSvc.setQmsFirstOrderCancelAjax(params);
    }
    
    
    /**
     * OK.
     * 웹주문현황 폼.
     * @작성일 : 2020. 4. 8.
     * @작성자 : kkyu
     */
    @GetMapping(value="orderList")
    public String orderList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        model.addAttribute("orderStatus", StatusUtil.ORDER.getMap()); // 주문상태 Map형태로 가져오기.
        model.addAttribute("orderStatusToJson", StatusUtil.ORDER.getMapToJson()); // 주문상태 JSON형태로 가져오기.
        
        String[] removeStatusArr = {"99"}; //임시저장
        model.addAttribute("orderStatusList", StatusUtil.ORDER.getList(removeStatusArr)); // 주문상태 List<Map>형태로 가져오기 > 임시저장 상태 제외.
        
        // CS 리스트 가져오기.
        model.addAttribute("csUserList", userSvc.getUserList("CS", "USER_SORT2 ASC ", 0, 0));

        // 주문일 세팅.
        //Date date = new Date();
        //String preday = Converter.dateToStr("yyyy-MM-dd", Converter.dateAdd(date, 5, -7));
        String today = Converter.dateToStr("yyyy-MM-dd");
        model.addAttribute("insdate", today);
        model.addAttribute("inedate", today);
        
        return "admin/order/orderList";
    }
    
    /**
     * OK.
     * 웹주문현황 폼 > 주문 헤더 리스트 가져오기 Ajax.
     * @작성일 : 2020. 4. 8.
     * @작성자 : kkyu
     */
    @ResponseBody
    @PostMapping(value="getOrderHeaderListAjax")
    public Object getOrderHeaderListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");

        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        return orderSvc.getOrderHeaderList(params, req, loginDto);
    }
    
    /**
     * OK.
     * 웹주문현황 폼 > 주문 헤더 리스트 > 주문 상세 리스트 가져오기 Ajax.
     * @작성일 : 2020. 4. 8.
     * @작성자 : kkyu
     * @param r_reqno [필수]
     */
    @ResponseBody
    @PostMapping(value="getOrderDetailListAjax")
    public Object getOrderDetailListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        return orderSvc.getCustOrderDetailList(params, req, loginDto);
    }
    
    /**
     * OK.
     * 웹주문현황 폼 > 웹주문상세 폼.
     * @작성일 : 2020. 4. 10.
     * @작성자 : kkyu
     */
    @GetMapping(value="orderView")
    public String orderView(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);

        // O_CUST_ORDER_Header 하나 가져오기.
        String r_reqno = Converter.toStr(params.get("r_reqno"));
        if(StringUtils.equals("", r_reqno)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);

        Map<String, Object> orderHeader = orderSvc.getCustOrderHOne(params); // 별도 권한을 위해 params로 넘기자.
        //Map<String, Object> orderHeader = orderSvc.getCustOrderHOne(r_reqno);
        if(CollectionUtils.isEmpty(orderHeader)) throw new LimeBizException(MsgCode.DATA_AUTH_ERROR);
        
        // O_CUST_ORDER_Detail 리스트 가져오기.
        // 2024-10-16 hsg 별칭 오류가 나서 수정. COD -> XX)
        List<Map<String, Object>> orderDetailList = orderSvc.getCustOrderDList(r_reqno, "", "", "XX.LINE_NO ASC ", 0, 0);
        
        // O_CONFIRM_ORDER_Detail 리스트 가져오기.
        List<Map<String, Object>> orderConfirmDetailList = orderSvc.getOrderConfirmDList(r_reqno, "", "", 0, 0);
        //logger.debug("orderConfirmDetailList : {}", orderConfirmDetailList);
        
        model.addAttribute("orderHeader", orderHeader);
        model.addAttribute("orderDetailList", orderDetailList);
        model.addAttribute("orderConfirmDetailList", orderConfirmDetailList);
        
        model.addAttribute("orderStatus", StatusUtil.ORDER.getMap()); // 주문상태 Map형태로 가져오기.
        model.addAttribute("orderStatusToJson", StatusUtil.ORDER.getMapToJson()); // 주문상태 JSON형태로 가져오기.
        
        return "admin/order/orderView";
    }
    
    /**
     * 권한 필요없음.
     * 웹주문현황 폼 > 주문처리 전 상태값 체크.
     * @작성일 : 2020. 4. 14.
     * @작성자 : kkyu
     */
    @ResponseBody
    @PostMapping(value="checkOrderEditStatusAjax")
    public Object checkOrderEditStatusAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
    	/*if( !LimitOrder.checkLimitOrderDate(operationType) ) {
			throw new LimeBizException(MsgCode.DATA_LIMIT_CONFIRM_DATE_ERROR);
		}*/
    	
    	String r_reqno = Converter.toStr(params.get("r_reqno"));
        if(StringUtils.equals("", r_reqno)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
        
        // 주문처리 할수 있는 상태값인지 체크. 주문접수(00)만 가능.
        Map<String, Object> orderHeader = orderSvc.getCustOrderHOne(r_reqno);
        if(!StringUtils.equals("00", Converter.toStr(orderHeader.get("STATUS_CD")))) throw new LimeBizException(MsgCode.DATA_STATUS_ERROR);
        
        return MsgCode.getResultMap(MsgCode.SUCCESS);
    }
    
    /**
     * OK.
     * 웹주문현황 폼 > 주문처리 폼.
     * @작성일 : 2020. 4. 10.
     * @작성자 : kkyu
     */
    @GetMapping(value="orderEdit")
    public String orderEdit(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        // O_CUST_ORDER_Header 하나 가져오기.
        String r_reqno = Converter.toStr(params.get("r_reqno"));
        if(StringUtils.equals("", r_reqno)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);

        Map<String, Object> orderHeader = orderSvc.getCustOrderHOne(params); // 별도 권한을 위해 params로 넘기자.
        //Map<String, Object> orderHeader = orderSvc.getCustOrderHOne(r_reqno);
        if(CollectionUtils.isEmpty(orderHeader)) throw new LimeBizException(MsgCode.DATA_AUTH_ERROR);
        
        // 주문처리 할수 있는 상태값인지 체크. 주문접수(00)만 가능.
        if(!StringUtils.equals("00", Converter.toStr(orderHeader.get("STATUS_CD")))) throw new LimeBizException(MsgCode.DATA_STATUS_ERROR);
        
        // Get OrderHeaderHistory Recent One. > 거래처에서 주문수정했는지 확인을 위해 필요.
        Map<String, Object> orderHeaderHistoryRecnetOne = orderSvc.getOrderHeaderHistoryOneByRecentList(r_reqno);
        logger.debug("orderHistoryOne : {}", orderHeaderHistoryRecnetOne);
        model.addAttribute("orderHistoryOne", orderHeaderHistoryRecnetOne);
        model.addAttribute("orderHistorySeq", Converter.toStr(orderHeaderHistoryRecnetOne.get("OHH_SEQ")));
        logger.debug("orderHistorySeq : {}", Converter.toStr(orderHeaderHistoryRecnetOne.get("OHH_SEQ")));
        
        // O_CUST_ORDER_Detail 리스트 가져오기.
     // 2024-10-21 hsg 별칭 오류가 나서 수정. COD -> XX)
        List<Map<String, Object>> orderDetailList = orderSvc.getCustOrderDList(r_reqno, "", "", "XX.LINE_NO ASC ", 0, 0);
        
        // 납품처 정보 가져오기 > 거래처 주의사항(O_SHIPTO.ADD2) 저장을 위한.
        String shiptoCd = Converter.toStr(orderHeader.get("SHIPTO_CD"));
        Map<String, Object> shipToOne = null;
        if(!StringUtils.equals("", shiptoCd)) {
            shipToOne = customerSvc.getShipTo(shiptoCd); 
        }
        model.addAttribute("shipToOne", shipToOne);
        
        // 출고지 리스트 가져오기.
        List<Map<String, Object>> plantList = configSvc.getPlantList("", 0, 0);
        
        // 휴일 리스트 가져오기.
        List<Map<String, Object>> holyDayList = commonSvc.getHolyDayList(params);
        //logger.debug("holyDayList : {}", holyDayList);
        
        // 주차 리스트 가져오기.
        List<Map<String, Object>> orderWeekList = commonSvc.getOrderWeekList(params);
        
        // 단위 리스트 가져오기.
        List<Map<String, Object>> unitList = commonCodeSvc.getList("C05", "Y");
        
        model.addAttribute("orderHeader", orderHeader);
        model.addAttribute("orderDetailList", orderDetailList);
        
        Gson gson = new Gson();
        model.addAttribute("plantList", plantList);
        model.addAttribute("plantListToJson", gson.toJson(plantList));
        model.addAttribute("holyDayList", holyDayList);
        model.addAttribute("holyDayListToJson", gson.toJson(holyDayList));
        model.addAttribute("orderWeekList", orderWeekList);
        model.addAttribute("orderWeekListToJson", gson.toJson(orderWeekList));
        model.addAttribute("unitList", unitList);
        model.addAttribute("unitListToJson", gson.toJson(unitList));
        
        model.addAttribute("orderStatus", StatusUtil.ORDER.getMap()); // 주문상태 Map형태로 가져오기.
        model.addAttribute("orderStatusToJson", StatusUtil.ORDER.getMapToJson()); // 주문상태 JSON형태로 가져오기.
        
        return "admin/order/orderEdit";
    }
    
    /**
     * 웹주문현황 폼 > 주문처리 폼 > 주문처리 Ajax.
     * @작성일 : 2020. 4. 23.
     * @작성자 : kkyu
     */
    @ResponseBody
    @PostMapping(value="insertOrderConfirmAjax")
    public Object insertOrderConfirmAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        return orderSvc.insertOrderConfirmTransaction(params, req, loginDto);
    }
    
    /**
     * 전체주문현황 리스트 폼.
     * @작성일 : 2020. 4. 25.
     * @작성자 : kkyu
     */
    @GetMapping(value="salesOrderList")
    public String salesOrderList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        //model.addAttribute("salesOrderStatus", StatusUtil.SALESORDER.getMap()); // 출하상태 Map형태로 가져오기.
        //model.addAttribute("salesOrderStatusToJson", StatusUtil.SALESORDER.getMapToJson()); // 출하상태 JSON형태로 가져오기.
        model.addAttribute("salesOrderStatusList", StatusUtil.SALESORDER.getList()); // 출하상태 List<Map>형태로 가져오기.
        
        // 주문일 세팅.
        String toDay = Converter.dateToStr("yyyy-MM-dd",new Date());
        String fromDay = Converter.dateToStr("yyyy-MM-dd");
        model.addAttribute("ordersdt", toDay);
        model.addAttribute("orderedt", fromDay);
        
        return "admin/order/salesOrderList";
    }

    /**
     * 전체주문현황 리스트 가져오기 Ajax.
     * @작성일 : 2020. 4. 25.
     * @작성자 : kkyu
     */
    @ResponseBody
    @PostMapping(value="getSalesOrderListAjax")
    public Object getSalesOrderListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        return orderSvc.getSalesOrderList(params, req, loginDto);
    }
    
    /**
     * QMS 조회 및 등록.
     * @작성일 : 2021. 3. 29.
     * @작성자 : jihye lee
     */
    @RequestMapping(value="qmsOrderList")
    public String qmsItemList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        //model.addAttribute("salesOrderStatus", StatusUtil.SALESORDER.getMap()); // 출하상태 Map형태로 가져오기.
        //model.addAttribute("salesOrderStatusToJson", StatusUtil.SALESORDER.getMapToJson()); // 출하상태 JSON형태로 가져오기.
        //model.addAttribute("qmlStatusList", StatusUtil.QMS.getList()); // QMS 상태 List<Map>형태로 가져오기.
        
        // 주문일 세팅.
        // jsh 시작일자 -90일 기본값 적용
        //Calendar cal = Calendar.getInstance();
        /*Date toDayDate = Converter.dateAdd(new Date(),5,-90);
        String toDay = Converter.dateToStr("yyyy-MM-dd",toDayDate);
        String fromDay = Converter.dateToStr("yyyy-MM-dd");
        model.addAttribute("ordersdt", toDay);
        model.addAttribute("orderedt", fromDay);*/
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        Map<String, Object> quater = Converter.getQuaterDate(cal);
        model.addAttribute("ordersdt", quater.get("ordersdt"));
        model.addAttribute("orderedt", quater.get("orderedt"));
        
        model.addAttribute("preYear", quater.get("preYear"));
        model.addAttribute("preQuat", quater.get("preQuat"));
        
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        List<Map<String, Object>> orderYearList = orderSvc.getQmsOrderYearList(params, req);
        List<Map<String, Object>> releaseYearList = orderSvc.getQmsReleaseYearList(params, req);
        
        model.addAttribute("orderYearList", orderYearList);
        model.addAttribute("releaseYearList", releaseYearList);
        
        // 현재날짜 기준 직전분기
        /*String preYear = fromDay.substring(0,4) + "년";
        String preQuat = "";
        String tmpYear = fromDay.substring(0,4);
        String tmpDay = fromDay.substring(5);
        String[] rangeDay = tmpDay.split("-");
        if(Integer.parseInt(rangeDay[0]) <= 3) {
            preYear = String.valueOf(Integer.parseInt(fromDay.substring(0,4)) - 1) + "년";
            preQuat = "4분기";
        }else if(Integer.parseInt(rangeDay[0]) >= 4 && Integer.parseInt(rangeDay[0]) <= 6){
            preQuat = "1분기";
        }else if(Integer.parseInt(rangeDay[0]) >= 7 && Integer.parseInt(rangeDay[0]) <= 9){
            preQuat = "2분기";
        }else{
            preQuat = "3분기";
        }
        
        model.addAttribute("preYear", preYear);
        model.addAttribute("preQuat", preQuat);*/
        
        return "admin/order/qmsOrderList";
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
        
        System.out.println("==================================================");
        System.out.println(params);
        System.out.println(loginDto);
        System.out.println(model);
        
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
     * OK.
     * 웹주문현황 리스트 > 엑셀다운로드
     * @작성일 : 2020. 4. 9.
     * @작성자 : kkyu
     */
    @PostMapping(value="orderExcelDown")
    public ModelAndView orderExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        // 주문상태 Map형태로 가져오기.
        resMap.put("orderStatus", StatusUtil.ORDER.getMap());
        
        // Start. 뷰단에서 넘어온 파일 쿠키 put.
        String fileToken = Converter.toStr(params.get("filetoken"));
        resMap.put("fileToken", fileToken);
        // End.
        
        params.put("where", "excel");
        resMap.putAll(orderSvc.getOrderHeaderList(params, req, loginDto));
        return new ModelAndView(new OrderExcel(), resMap);
    }
    
    /**
     * 전체주문현황 리스트 > 엑셀다운로드
     * @작성일 : 2020. 4. 26.
     * @작성자 : an
     */
    @PostMapping(value="salesOrderExcelDown")
    public ModelAndView salesOrderExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        // Start. 뷰단에서 넘어온 파일 쿠키 put.
        String fileToken = Converter.toStr(params.get("filetoken"));
        resMap.put("fileToken", fileToken);
        // End.
        
        params.put("where", "excel");
        resMap.putAll(orderSvc.getSalesOrderList(params, req, loginDto));
        return new ModelAndView(new SalesOrderExcel(), resMap);
    }
    
    /**
     * 전체 QMS 주문현황 리스트 > 엑셀다운로드
     * @작성일 : 2020. 4. 26.
     * @작성자 : an
     */
    @PostMapping(value="qmsOrderExcelDown")
    public ModelAndView qmsOrderExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        // Start. 뷰단에서 넘어온 파일 쿠키 put.
        String fileToken = Converter.toStr(params.get("filetoken"));
        resMap.put("fileToken", fileToken);
        // End.
        
        params.put("where", "excel");
        resMap.putAll(orderSvc.getQmsOrderList(params, req, loginDto));
        return new ModelAndView(new QmsOrderExcel(), resMap);
    }
    
    /**
     * QMS 입력 팝업 
     * @작성일 : 2021. 4. 26.
     * @작성자 : jsh
     */
    @RequestMapping(value="qmsOrderPop" ,method= {RequestMethod.GET,RequestMethod.POST})
    public Object qmsOrderPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
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
        
        return "admin/order/qmsOrderPop";
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
        
        String qmsId = (params.get("qmsId") == null) ? "" : (String) params.get("qmsId");
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
        return "admin/order/qmsOrderPopView";
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
        
        return "admin/order/qmsOrderPopEmail";
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
        mail.sendMail(smtpHost,title, r_shiptorepNm, r_shiptoEmail, shopName, smtpSender, contentStr, null, "");
        
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
        return "admin/order/qmsCorpListPop";
    }
    
    /**
     * 거래처 리스트 가져오기 Ajax.
     * @작성일 : 2021. 5. 10.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="/admin/order/getQmsCorpList")
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
        
        return "admin/order/qmsOrderPopFile";
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
    @PostMapping(value="/admin/order/qmsOrderFileDown")
    public ModelAndView qmsOrderFileDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {        
        return orderSvc.qmsOrderFileDown(params, req, model, loginDto);
    }
    
    /**
     * QMS 파일 일괄 or 개별 다운로드.
     * @작성일 : 2021. 4. 26.
     * @작성자 : jsh
     * @수정일 : 2021. 11. 15
     * @수정자 : ljh
     */
    @PostMapping(value="qmsOrderFileAllDown")
    public ModelAndView qmsOrderFileAllDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model, LoginDto loginDto) throws Exception {
        //특정 QMS 번호만 다운로드시..
        Object qmsIdToDownTxt = params.get("qmsIdToDown");
        if(qmsIdToDownTxt!=null) {
            String qmsId = null;
            qmsId = params.get("qmsIdToDown").toString();
            String[] qmsIdToDown = qmsId.split(",");
            logger.info("qmsIdToDownList: " + qmsIdToDown);
            params.put("qmsIdToDown", qmsIdToDown);
        }
        
        //조건 없이 전체 조회시 qmsIdToDown Parameter는 NULL
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
    @PostMapping(value="/admin/order/getQmsOrderQtyCheck")
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
    @RequestMapping(value="/admin/order/setQmsOrderTempReset")
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
    @RequestMapping(value="/admin/order/getQmsOrderDupCheck")
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
    @RequestMapping(value="/admin/order/setQmsOrderAllAdd")
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
    @PostMapping(value="/admin/order/getShiptoListAjax")
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
    @PostMapping(value="/admin/order/setQmsOrderRemove")
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
    @RequestMapping(value="/admin/order/getOrderDupCheck")
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
    @RequestMapping(value="/admin/order/getOrderCustDupCheck")
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
        
        return "admin/order/qmsOrderPrePop";
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
        Map<String, Object> result = new HashMap<>();
        
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
     * QMS 사전입력 강제생성.
     * @작성일 : 2021. 7. 11.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="/admin/order/setQmsPreTestInsert")
    public Object setQmsPreTestInsert(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        return orderSvc.setQmsPreTestInsert(params);
    }
    
    
    /**
     * QMS 사전입력 생성.
     * @작성일 : 2021. 7. 14.
     * @작성자 : jsh
     */
    /*@ResponseBody
    @PostMapping(value="/admin/order/syncQmsSalesOrder")
    public Object syncQmsSalesOrder() throws Exception {
        return orderSvc.syncQmsSalesOrder();
    }*/
    
    
    /**
     * QMS 메일발송 대상 조회
     * @작성일 : 2021. 7. 14.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="getMailPreQmsOrderList")
    public Object getMailPreQmsOrderList() throws Exception {
        return orderSvc.getMailPreQmsOrderList();
    }
    
    
    /**
     * QMS 사전입력 삭제(취소).
     * @작성일 : 2022. 12. 4.
     * @작성자 : jsh
     */
    @ResponseBody
    @PostMapping(value="/admin/order/setQmsPreOrderRemove")
    public Object setQmsPreOrderRemove(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        return orderSvc.setQmsPreOrderRemove(params);
    }
    
    @ResponseBody
    @PostMapping(value="/admin/order/getPostalCodeCount")
    public Map<String, Object> getPostalCodeCount(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        return postalCodeSvc.getPostalCodeCount(params);
    }
}
