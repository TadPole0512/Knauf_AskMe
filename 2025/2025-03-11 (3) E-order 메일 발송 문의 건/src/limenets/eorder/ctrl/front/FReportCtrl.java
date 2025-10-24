package com.limenets.eorder.ctrl.front;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.util.Converter;
import com.limenets.eorder.excel.SendMailHistoryExcel;
import com.limenets.eorder.svc.BoardSvc;
import com.limenets.eorder.svc.CommonSvc;
import com.limenets.eorder.svc.ConfigSvc;
import com.limenets.eorder.svc.CustomerSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.ReportSvc;
import com.limenets.eorder.util.StatusUtil;

/**
 * Front 리포트 컨트롤러.
 */
@Controller
@RequestMapping("/front/report/*")
public class FReportCtrl {
	private static final Logger logger = LoggerFactory.getLogger(FReportCtrl.class);

	@Inject private CommonSvc commonSvc;
	@Inject private ReportSvc reportSvc;
	@Inject private CustomerSvc customerSvc;
	@Inject private OrderSvc orderSvc;
	@Inject private ConfigSvc configSvc;
	@Inject private BoardSvc boardSvc;
	
	/**
	 * 거래사실확인서
	 * @작성일 : 2020. 5. 7.
	 * @작성자 : an
	 */
	@GetMapping(value="factReport")
	public String customerList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);

		Map<String, Object> customer = customerSvc.getCustomer(loginDto.getCustCd());
		model.addAttribute("cust_cd", Converter.toStr(customer.get("CUST_CD")));
		model.addAttribute("cust_nm", Converter.toStr(customer.get("CUST_NM")));
		model.addAttribute("salesrep_nm", Converter.toStr(customer.get("SALESREP_NM")));
		model.addAttribute("shipto_nm", Converter.toStr(loginDto.getShiptoNm()));
		model.addAttribute("shipto_cd", Converter.toStr(loginDto.getShiptoCd()));
		model.addAttribute("user_email", Converter.toStr(loginDto.getUserEmail()));
		
		model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));
		
		return "front/report/factReport";
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
	 * 리포트 > 납품확인서 폼.
	 * @작성일 : 2020. 6. 8.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="deliveryReport")
	public String deliveryReport(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		commonSvc.getFrontCommonData(params, req, model, loginDto);
		
		String today = Converter.dateToStr("yyyy-MM-dd");
		model.addAttribute("insdate", today);
		model.addAttribute("inedate", today);
		
		return "front/report/deliveryReport";
	}
	
	/**
	 * 리포트 > 납품확인서 폼 > 양식 팝업 폼.
	 * @작성일 : 2020. 6. 8.
	 * @작성자 : kkyu
	 * @param ri_orderno : O_SALESORDER.ORDERNO
	 * @param paper_type : 10=개별아이템-현장, 20=개별아이템-착지, 11=아이템별-현장, 21=아이템별-현장
	 */
	@RequestMapping(value="deliveryPaperPop", method={RequestMethod.GET, RequestMethod.POST})
	public String deliveryPaperPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		reportSvc.getDeliveryPaper2(params, req, loginDto, model);
		//reportSvc.getDeliveryPaper(params, req, loginDto, model);
		return "common/deliveryPaperPop";
	}
	
    /**
     * qmsReport
     * @작성일 : 2021. 5. 9.
     * @작성자 : an
     */
    @RequestMapping(value="qmsReport" ,method= {RequestMethod.GET,RequestMethod.POST})
    public String qmsReport(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        String qmsId = "";
        String qmsIdTxt = null;
        String pageType = null;
        
        
        if(params.get("page_type")!= null) {
            pageType = params.get("page_type").toString();
        }else {
            pageType = "qmsReport";
        }
        
        // 단일 선택 업로드
        if(pageType.equals("qmsReport")) {
            if(params.get("qmsId")!= null) {
                qmsIdTxt = params.get("qmsId").toString();
            }
            qmsId = qmsIdTxt;
            
        } else if(pageType.equals("qmsAllReport")) {
            qmsIdTxt = "";
            List<Map<String, Object>> qmsPopList = orderSvc.getQmsPopList(params);
            for(int i = 0; i < qmsPopList.size(); i++) {
                qmsIdTxt += qmsPopList.get(i).get("QMS_ORD_NO");
                if(i < qmsPopList.size()-1) {
                    qmsIdTxt += ",";
                }
            }
            qmsId = qmsIdTxt;
            
            //정렬조건 오류 방지 초기화
            params.put("r_orderby", null);
        }
        
        int qmsIdSize = qmsId.split(",").length;
        
        List<Map<String, Object>> reportList = new ArrayList<Map<String, Object>>();
        
        Map<String, Object> result = new HashMap<>();
        
        params.put("work", "mod");
        
        for(int i = 0; i < qmsIdSize; i++) {
            
            result = new HashMap<>();
            
         // QMS 시퀀스와 함께 입력된 경우 처리
            if(qmsId.split(",")[i].indexOf("-") > 0) {
                String[] qmsArr = qmsId.split(",")[i].split("-");
                params.put("qmsId", qmsArr[0]);
                params.put("qmsSeq", qmsArr[1]);
            }else {
              //기본 qms 시퀀스 입력
              params.put("qmsSeq",params.get("qmsSeq")!=null?params.get("qmsSeq"):1);
            }
            
            /*
             * 회사 직인 
             */
            Map<String, Object> resMap = configSvc.getConfigList(params);
            //model.addAttribute("configList", resMap);
            result.put("configList", resMap);
            
            /*
             *  MAST
             */
            List<Map<String, Object>> getQmsPopMastList = orderSvc.getQmsPopMastList(params);
            //model.addAttribute("qmsMastList", getQmsPopMastList);
            //result.put("qmsMastList", getQmsPopMastList);
            /*
             *  ITEM
             */
            List<Map<String, Object>> getQmsPopDetlGridList = orderSvc.getQmsPopDetlGridList(params);
            //model.addAttribute("qmsPopDetlGridList", getQmsPopDetlGridList);
            result.put("qmsPopDetlGridList", getQmsPopDetlGridList);
            /*
             *  FIREPROOF CONSTRUCTION TYPE
             */
            List<Map<String, Object>> getQmsFireproofList = orderSvc.getQmsFireproofList(params);
            //model.addAttribute("qmsFireproofList", getQmsFireproofList);
            result.put("qmsFireproofList", getQmsFireproofList);
            
            String FIRETIME_05 = "N";
            String FIRETIME_10 = "N";
            String FIRETIME_15 = "N";
            String FIRETIME_20 = "N";
            String FIRETIME_30 = "N";
            String BEAM_CHECK = "N";
            String PILLAR_CHECK = "N";
            String NONWALL_CHECK = "N";
            String STRUCT_NM = "";
            String Q_RECOG_NUM = "";
            int count = 0;
            for(Map<String, Object> m : getQmsFireproofList) {
                if(m.get("CHK_YN").toString().compareTo("Y") == 0) {
                    switch(m.get("FIRETIME").toString()) {
                        case "0.5":
                            FIRETIME_05 = "Y";
                            break;
                        case "1":
                            FIRETIME_10 = "Y";
                            break;
                        case "1.5":
                            FIRETIME_15 = "Y";
                            break;
                        case "2":
                            FIRETIME_20 = "Y";
                            break;
                        case "3":
                            FIRETIME_30 = "Y";
                            break;
                        default:
                            break;
                    }
                    
                    String fType = m.get("FIREPROOFTYPE").toString();
                    int idx_l = fType.lastIndexOf("(");
                    int idx_r = fType.lastIndexOf(")");
                    String s1 = idx_l<fType.length() ? fType.substring(0, idx_l) : "";
                    String s2 = idx_r<fType.length() ? fType.substring(idx_l+1, idx_r) : "";
                    STRUCT_NM = String.format("%s%s%s", STRUCT_NM, count==0 ? "" : ",", s1);
                    Q_RECOG_NUM = String.format("%s%s%s", Q_RECOG_NUM, count==0 ? "" : ",", s2);
                    count++;
                    
                    if(s1.toUpperCase().contains("BEAM"))
                        BEAM_CHECK="Y";
                    else if(s1.toUpperCase().contains("COLUMN"))
                        PILLAR_CHECK="Y";
                    else if(s1.contains("기둥-3"))
                        PILLAR_CHECK="Y";
                    else 
                        NONWALL_CHECK="Y";
                }
            }
            
            if(getQmsPopMastList.size() > 0) {     
                Map<String, Object> map = getQmsPopMastList.get(0);
                map.put("FIRETIME_05", FIRETIME_05);
                map.put("FIRETIME_10", FIRETIME_10);
                map.put("FIRETIME_15", FIRETIME_15);
                map.put("FIRETIME_20", FIRETIME_20);
                map.put("FIRETIME_30", FIRETIME_30);
                map.put("STRUCT_NM", STRUCT_NM);
                map.put("Q_RECOG_NUM", Q_RECOG_NUM);
                map.put("BEAM_CHECK", BEAM_CHECK);
                map.put("PILLAR_CHECK", PILLAR_CHECK);
                map.put("NONWALL_CHECK", NONWALL_CHECK);
            }
            
            result.put("qmsMastList", getQmsPopMastList);
            
            reportList.add(result);
            
        }
        
        model.addAttribute("reportList", reportList);
        
        return "front/report/qmsReport";
    }
}
