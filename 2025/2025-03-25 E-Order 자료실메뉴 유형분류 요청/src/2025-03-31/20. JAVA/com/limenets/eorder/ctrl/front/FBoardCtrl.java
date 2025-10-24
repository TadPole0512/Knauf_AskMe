package com.limenets.eorder.ctrl.front;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.common.util.HttpUtils;
import com.limenets.common.util.MailUtil;
import com.limenets.eorder.dto.BoardDto;
import com.limenets.eorder.svc.BoardSvc;
import com.limenets.eorder.svc.CommonCodeSvc;
import com.limenets.eorder.svc.CommonSvc;
import com.limenets.eorder.svc.ConfigSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.TempleteSvc;

/**
 * Front 게시판 컨트롤러.
 */
@Controller
@RequestMapping("/front/board/*")
public class FBoardCtrl {
	private static final Logger logger = LoggerFactory.getLogger(FBoardCtrl.class);

    @Inject private CommonSvc commonSvc;
    @Inject private CommonCodeSvc commonCodeSvc;
    @Inject private BoardSvc boardSvc;
    @Inject private OrderSvc orderSvc;
    @Inject private ConfigSvc configSvc;
    @Inject private TempleteSvc templeteSvc;
    
    @Value("${https.url}") private String httpsUrl;
    @Value("${mail.smtp.sender.addr}") private String smtpSender;
    @Value("${shop.name}") private String shopName;
    @Value("${mail.smtp.url}") private String smtpHost;

	/**
	 * 공지사항 리스트 폼.
	 * @작성일 : 2020. 3. 25.
	 * @작성자 : isaac
	 */
	@RequestMapping(value="noticeList", method={RequestMethod.GET, RequestMethod.POST})
	public String noticeList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		// bottom영역 영업사원, CS담당자 정보 가져오기
		commonSvc.getFrontCommonData(params, req, model, loginDto);

		params.put("r_bdid","notice");
		Map<String, Object> resMap = boardSvc.getBoardList(params,req);

		model.addAllAttributes(resMap);
		
		model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));

		return "front/board/noticeList";
	}

	/**
	 * FAQ 리스트 폼.
	 * @작성일 : 2020. 4. 13.
	 * @작성자 : isaac
	 */
	@RequestMapping(value="faqList", method={RequestMethod.GET, RequestMethod.POST})
	public String faqList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		// bottom영역 영업사원, CS담당자 정보 가져오기
		commonSvc.getFrontCommonData(params, req, model, loginDto);

		params.put("r_bdid","faq");
		Map<String, Object> resMap = boardSvc.getBoardList(params,req);

		model.addAllAttributes(resMap);
		model.addAttribute("faqCategoryList", commonCodeSvc.getDetailList("C03"));
		
		model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));

		return "front/board/faqList";
	}
///##############################################################
	   /**
     * QnA 리스트 폼.
     * @작성일 : 2020. 4. 13.
     * @작성자 : isaac
     */
    @RequestMapping(value="qnaList", method={RequestMethod.GET, RequestMethod.POST})
    public String qnaList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
        // bottom영역 영업사원, CS담당자 정보 가져오기
        commonSvc.getFrontCommonData(params, req, model, loginDto);
        params.put("r_bdid","qna");
        Map<String, Object> resMap = boardSvc.getBoardList(params,req);

        model.addAllAttributes(resMap);
        model.addAttribute("qnaCategoryList", commonCodeSvc.getDetailList("C03"));

        return "front/board/qnaList";
    }

    //########################################################
	/**
	 * 자료실 리스트 폼.
	 * @작성일 : 2020. 4. 13.
	 * @작성자 : isaac
	 * @수정 : 2025-03-27 hsg Dragonrana : 유형분려에서 중,소 분류 적용되게 수정링 필요해서 오래된 것처럼 이름변경
	 */
	@RequestMapping(value="referenceListOld", method={RequestMethod.GET, RequestMethod.POST})
	public String referenceListOld(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		// bottom영역 영업사원, CS담당자 정보 가져오기
		commonSvc.getFrontCommonData(params, req, model, loginDto);

		params.put("r_bdid","reference");
		Map<String, Object> resMap = boardSvc.getBoardList(params,req);

		model.addAllAttributes(resMap);
		model.addAttribute("referenceCategoryList", commonCodeSvc.getDetailList("C04"));//자료실 유형
		
		model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));

		return "front/board/referenceList";
	}

	/**
	 * 자료실 리스트 폼.
	 * @작성일 : 2020. 4. 13.
	 * @작성자 : isaac
	 */
	@RequestMapping(value="referenceList", method={RequestMethod.GET, RequestMethod.POST})
	public String referenceList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		// bottom영역 영업사원, CS담당자 정보 가져오기
		model.addAttribute("referenceCategoryList", commonCodeSvc.getDetailList("C04"));//자료실 유형
		
		Map<String, Object> svcMap = new HashMap<>();
		
		String rbttype2 = params.get("r_bdtype2") == null ? "C04001" : params.get("r_bdtype2").toString();
		String rbttype3 = params.get("r_bdtype2") == null ? "C0400101" : params.get("r_bdtype2").toString();

		svcMap.put("r_cccode", rbttype2);
		svcMap.put("r_depth", 3);
		model.addAttribute("referenceCategory2List", commonCodeSvc.getCategoryListWithDepth(svcMap));//카테고리 2
		
		svcMap.clear();
		svcMap.put("r_cccode", rbttype3);
		svcMap.put("r_depth", 4);
		model.addAttribute("referenceCategory3List", commonCodeSvc.getCategoryListWithDepth(svcMap));//카테고리 3
		
		return "front/board/referenceList";
	}

	/**
	 * 공지사항 폼 > 팝업 폼.
	 * @작성일 : 2020. 4. 13.
	 * @작성자 : isaac
	 */
	@RequestMapping(value="noticeViewPop", method={RequestMethod.GET, RequestMethod.POST})
	public String noticeViewPop(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {
		BoardDto resMap = boardSvc.getBoardOne(params);
		model.addAttribute("boardOne", resMap);
		model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));

		return "front/board/noticeViewPop";
	}

	
	///==================================================================
    /**
  * sample 리스트 폼.
  * @작성일 : 2020. 4. 13.
  * @작성자 : isaac
  */
 @RequestMapping(value="sampleList", method={RequestMethod.GET, RequestMethod.POST})
 public String sampleList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
     // bottom영역 영업사원, CS담당자 정보 가져오기
     commonSvc.getFrontCommonData(params, req, model, loginDto);
     params.put("r_bdid","sample");
     Map<String, Object> resMap = boardSvc.getBoardList(params,req);

     model.addAllAttributes(resMap);
     model.addAttribute("sampleCategoryList", commonCodeSvc.getDetailList("C03"));
     
     model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));

     return "front/board/sampleList";
 }
 
 /**
  * chat 리스트 폼.
  * @작성일 : 2020. 4. 13.
  * @작성자 : isaac
  */
 @RequestMapping(value="chatList", method={RequestMethod.GET, RequestMethod.POST})
 public String chatList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
     // bottom영역 영업사원, CS담당자 정보 가져오기
     commonSvc.getFrontCommonData(params, req, model, loginDto);
     params.put("r_bdid","chat");
     Map<String, Object> resMap = boardSvc.getBoardList(params,req);

     model.addAllAttributes(resMap);
     
     model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));

     return "front/board/chatList";
 }
 
 @RequestMapping(value="chatViewPop", method={RequestMethod.GET, RequestMethod.POST})
 public String chatViewPop(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {

    BoardDto resMap = boardSvc.getBoardOne(params);
    BoardDto resMap2 = boardSvc.getBoardOne2(params);

    model.addAttribute("boardOne", resMap);
    model.addAttribute("boardOne2", resMap2);

    model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));

    return "front/board/chatViewPop";
 }
 
 @PostMapping(value="/chat/pop/addEditPop")
 public String chatAddEditViewPop(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {
	 String r_bdseq = Converter.toStr(params.get("r_bdseq"));
	 System.out.println("SEQ: " + r_bdseq);
	 
	 for(String key : params.keySet()) { 
        String value = String.valueOf( params.get(key)); 
        System.out.println(key + " : " + value); 
     }
	 
	 if(!StringUtils.isEmpty(r_bdseq)) {
        if(boardSvc.getBoardCnt(r_bdseq) < 1)  throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
        
        BoardDto resMap = boardSvc.getBoardOne(params);
        model.addAttribute("boardOne", resMap);
        model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));
        model.addAttribute("restoreXSSContent2", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_REPLY())));
     }
	 
     return "front/board/chatAddEditPop";
 }
 
 
 @ResponseBody
 @PostMapping(value="insertUpdateChatAjax")
 public Object insertUpdateChatAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
     Map<String, Object> resMap = boardSvc.insertUpdateBoard(params, req, loginDto);
     
     logger.debug("*********************FBoardCTRL   RESMAP AJAX CHECK***********************************");
     for( String key : resMap.keySet() ){
         String value = String.valueOf( resMap.get(key)); 
         logger.debug( String.format("키 : "+key+", 값 : "+value));
     }
     
     return resMap;
 }
 
 @ResponseBody
 @RequestMapping(value="deleteChatAjax", method=RequestMethod.POST)
 public Object deleteChatAjax(@RequestParam Map<String, Object> params) {
     Map<String, Object> resMap = boardSvc.deleteBoard(params);          
     return resMap;          
 }

 /**
* 공지사항 폼 > 팝업 폼.
* @작성일 : 2020. 4. 13.
* @작성자 : isaac
*/
@RequestMapping(value="sampleViewPop", method={RequestMethod.GET, RequestMethod.POST})
public String sampleViewPop(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {

   BoardDto resMap = boardSvc.getBoardOne(params);
   BoardDto resMap2 = boardSvc.getBoardOne2(params);

   model.addAttribute("boardOne", resMap);
   model.addAttribute("boardOne2", resMap2);

   model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));

   return "front/board/sampleViewPop";
}


@PostMapping(value="/sample/pop/{process}")
public String sampleAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {
    String r_bdseq = Converter.toStr(params.get("r_bdseq"));
    logger.debug("========================================== FQA ->> sample");
    for(String key : params.keySet()) { 
        String value = String.valueOf( params.get(key)); 
        logger.debug(key + " : " + value); 
        }
    //등록/수정 분기처리
    logger.debug("######### StringUtils.isEmpty(r_bdseq) ############ l");
    //logger.debug(StringUtils.isEmpty(r_bdseq));   ///// false라고 뜸
    
    if(!StringUtils.isEmpty(r_bdseq)) {
        if(boardSvc.getBoardCnt(r_bdseq) < 1)  throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
        
        BoardDto resMap = boardSvc.getBoardOne(params);
        model.addAttribute("boardOne", resMap);
        model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));
        model.addAttribute("restoreXSSContent2", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_REPLY())));

    }
   
    logger.debug("========================================== FQA ->> sample");
    for(String key : params.keySet()) { 
        String value = String.valueOf( params.get(key)); 
        logger.debug(key + " : " + value); 
        }
    model.addAttribute("sampleCategoryList", commonCodeSvc.getDetailList("C03"));//sample 유형
    logger.debug("==========================================");
    logger.debug("front단"+ r_bdseq);
    logger.debug("======================프로세스");
    logger.debug(process);
    logger.debug("======================리턴값=========================");
    logger.debug("::::::::  admin/board/"+boardSvc.getPopPageName(params, process));
    return "front/board/"+boardSvc.getPopPageName(params, process);
    //return "admin/board/sampleAddEditPop";
}

/**
 * 자료실 폼 > 저장/수정 Ajax.
 * @작성일 : 2020. 3. 24.
 * @작성자 : isaac
 */
@ResponseBody
@PostMapping(value="insertUpdatesampleAjax")
public Object insertUpdatesampleAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
    Map<String, Object> resMap = boardSvc.insertUpdateBoard(params, req, loginDto);
    
    logger.debug("*********************FBoardCTRL   RESMAP AJAX CHECK***********************************");
    for( String key : resMap.keySet() ){
        String value = String.valueOf( resMap.get(key)); 
        logger.debug( String.format("키 : "+key+", 값 : "+value));
    }
    
    
    
    return resMap;
}
 
/**
 * sample 파일 다운로드.
 * @작성일 : 2020. 3. 24.
 * @작성자 : isaac
 */
@PostMapping(value="sampleFileDown")
public ModelAndView sampleFileDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model) throws LimeBizException {
    ModelAndView mv = boardSvc.boardFileDown(params, req, model); 
    
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
 * sample 폼 > 삭제 Ajax.
 * @작성일 : 2020. 3. 24.
 * @작성자 : isaac
 */
@ResponseBody
@RequestMapping(value="deletesampleAjax", method=RequestMethod.POST)
public Object deletesampleAjax(@RequestParam Map<String, Object> params) {
    Map<String, Object> resMap = boardSvc.deleteBoard(params);          
    return resMap;          
}


/**
 * 샘플 이메일 발송 Ajax.
 * @작성일 : 2021. 4. 29.
 * @작성자 : jsh
 */
@ResponseBody
@PostMapping(value="setsampleMailLog")
public Object setsampleMailLog(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
 // 내부사용자 웹주문현황  > 별도 권한 설정.
    orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
    
    for( String key : params.keySet() ){
        String value = String.valueOf( params.get(key)); 
        logger.debug( String.format("키 : "+key+", 값 : "+value));
    }
  //====================================================================================================================================
    logger.debug("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&     setsampleMailLog BEGIN    &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    String r_shiptorepNm = Converter.toStr(params.get("shiptorepNm"));
    String r_shiptoEmail = Converter.toStr(params.get("shiptoEmail"));
    //메일에 넣어줄 것들
    String m_bdindate = Converter.toStr(params.get("m_bdindate"));
    String m_bdtitle = Converter.toStr(params.get("m_bdtitle"));
    String  m_bduser = Converter.toStr(params.get("m_bduser"));
  
    Map<String, Object> mailData = new HashMap<String, Object>();//new에서 타입 파라미터 생략가능
    mailData.put("m_bdindate", m_bdindate);
    mailData.put("m_bdtitle", m_bdtitle);
    mailData.put("m_bduser", m_bduser);
    

    for( String key : mailData.keySet() ){
        String value = String.valueOf( mailData.get(key)); 
        logger.debug( String.format("키 : "+key+", 값 : "+value));
    }
    //int tbdseq = Converter.toInt(params.get("bdseq"));
    
    String protocol = "";
    if(req.isSecure()) {
        protocol="https";
    }else {
        protocol="http";
    }
    // 메일보내기
    Map<String, Object> resMap = configSvc.getConfigList(params);
    String title = Converter.toStr(resMap.get("MAILTITLE2"));
    String url =  protocol+"://"+ req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath(); //httpsUrl
    String sampleReportUrl = url + "/front/report/sampleReport.lime";
    String mailBottomImg = url + "/data/config/" + Converter.toStr(resMap.get("MAILBOTTOMIMG"));
    
    String contentStr = templeteSvc.sampleReportEmail(sampleReportUrl, url, mailBottomImg, m_bdindate,m_bdtitle ,m_bduser );
    
    
    //메일 발송시 바꿔줘야 함
    MailUtil mail = new MailUtil();
    mail.sendMail(smtpHost,title, r_shiptorepNm, r_shiptoEmail, shopName, smtpSender, contentStr, null, "");
    //mail.sendGMail(title, r_shiptorepNm, r_shiptoEmail, shopName, smtpSender, contentStr, null, ""   );
    

    params.put("userId", loginDto.getUserId());
   // int result = orderSvc.setQmsMailLog(params);
    logger.debug("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&      setsampleMailLog END           &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    return true;
}  









 //========================================================================
	
	
	/**
	 * 공지사항 폼 > 팝업 폼.
	 * @작성일 : 2020. 4. 13.
	 * @작성자 : isaac
	 */
	@RequestMapping(value="faqViewPop", method={RequestMethod.GET, RequestMethod.POST})
	public String faqViewPop(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {

		BoardDto resMap = boardSvc.getBoardOne(params);
		model.addAttribute("boardOne", resMap);
		model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));

		return "front/board/faqViewPop";
	}
	
	//#################################################################
	   /**
     * 공지사항 폼 > 팝업 폼.
     * @작성일 : 2020. 4. 13.
     * @작성자 : isaac
     */
    @RequestMapping(value="qnaViewPop", method={RequestMethod.GET, RequestMethod.POST})
    public String qnaViewPop(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {

        BoardDto resMap = boardSvc.getBoardOne(params);
        model.addAttribute("boardOne", resMap);
        model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));

        return "front/board/qnaViewPop";
    }
	//#################################################################

	/**
	 * 자료실 폼 > 팝업 폼.
	 * @작성일 : 2020. 4. 13.
	 * @작성자 : isaac
	 */
	@RequestMapping(value="referenceViewPop", method={RequestMethod.GET, RequestMethod.POST})
	public String referenceViewPop(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {

		BoardDto resMap = boardSvc.getBoardOne(params);
		model.addAttribute("boardOne", resMap);
		model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));

		return "front/board/referenceViewPop";
	}

	/**
	 * 공지사항 파일 다운로드.
	 * @작성일 : 2020. 4. 13.
	 * @작성자 : isaac
	 */
	@PostMapping(value="noticeFileDown")
	public ModelAndView noticeFileDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model) throws LimeBizException {
		ModelAndView mv = boardSvc.boardFileDown(params, req, model);

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
	 * 자료실 파일 다운로드.
	 * @작성일 : 2020. 4. 13.
	 * @작성자 : isaac
	 */
	@PostMapping(value="referenceFileDown")
	public ModelAndView referenceFileDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model) throws LimeBizException {
		ModelAndView mv = boardSvc.boardFileDown(params, req, model);

		// 파일 다운로드 종료후 뷰단에서 종료여부 체크중인 쿠키 생성.
		//logger.debug("### 파일다운로드 종료 ###");
		String fileToken = Converter.toStr(params.get("filetoken"));
		Cookie fileCookie = new Cookie(fileToken, "true");
		fileCookie.setMaxAge(60*1); // 1분만 저장.
		res.addCookie(fileCookie);
		//logger.debug("fileToken : {}", fileToken);

		return mv;
	}
	
	//##################################################################
	  
/*
	  @RequestMapping(value="faqViewPop", method={RequestMethod.GET, RequestMethod.POST})
	   public String faqViewPop2(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {

	       BoardDto resMap = boardSvc.getBoardOne(params);
	       model.addAttribute("boardOne", resMap);
	       model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));

	       return "front/board/faqViewPop";
	   }
	   */
	/**
     * QNA 폼 ADD> 팝업 폼. 
     * @작성일 : 2020. 3. 23.
     * @작성자 : 
     * @param process = addEditPop , viewPop
     */
/*	    @PostMapping(value="qnaAddEditViewPop")
	    public String qnaAddEditViewPop(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {
	        String r_bdseq = Converter.toStr(params.get("r_bdseq"));
	        //등록/수정 분기처리
	        if(!StringUtils.isEmpty(r_bdseq)) {
	            if(boardSvc.getBoardCnt(r_bdseq) < 1)  throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
	            
	            BoardDto resMap = boardSvc.getBoardOne(params);
	            model.addAttribute("boardOne", resMap);
	            model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));
	        }
	        logger.debug("==========================================");
	        logger.debug("front단"+ r_bdseq);

	        model.addAttribute("qnaCategoryList", commonCodeSvc.getDetailList("C03"));//QNA 유형
	            
	        return "front/board/qnaAddEditViewPop";
	    }
*/	
	 
    @PostMapping(value="/qna/pop/{process}")
    public String qnaAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {
        String r_bdseq = Converter.toStr(params.get("r_bdseq"));
        for(String key : params.keySet()) { 
            String value = String.valueOf( params.get(key)); 
            logger.debug(key + " : " + value); 
            }
        //등록/수정 분기처리
        if(!StringUtils.isEmpty(r_bdseq)) {
            if(boardSvc.getBoardCnt(r_bdseq) < 1)  throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
            
            BoardDto resMap = boardSvc.getBoardOne(params);
            model.addAttribute("boardOne", resMap);
            model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));
        }
       
        for(String key : params.keySet()) { 
            String value = String.valueOf( params.get(key)); 
            logger.debug(key + " : " + value); 
            }
        model.addAttribute("qnaCategoryList", commonCodeSvc.getDetailList("C03"));//QNA 유형
        logger.debug("==========================================");
        logger.debug("front단"+ r_bdseq);
        logger.debug("======================프로세스");
        logger.debug(process);
        logger.debug("======================리턴값=========================");
        logger.debug("::::::::  admin/board/"+boardSvc.getPopPageName(params, process));
        return "front/board/"+boardSvc.getPopPageName(params, process);
        //return "admin/board/qnaAddEditPop";
    }
    
    /**
     * 자료실 폼 > 저장/수정 Ajax.
     * @작성일 : 2020. 3. 24.
     * @작성자 : isaac
     */
    @ResponseBody
    @PostMapping(value="insertUpdateqnaAjax")
    public Object insertUpdateqnaAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
        Map<String, Object> resMap = boardSvc.insertUpdateBoard(params, req, loginDto);
        return resMap;
    }
    
    
	//##################################################################
	
 
    /**
     * 자료실 폼 > 유형분류 Ajax.
     * @작성일 : 2025. 3. 27.
     * @작성자 : 2025-03-27 hsg Dragonrana
     */
    @ResponseBody
    @PostMapping(value="eorderReferenceCategoryList")
    public Object eorderReferenceCategoryList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
    	Map<String, Object> svcMap = new HashMap<>();

    	String depths = params.get("depth") == null ? "3" : params.get("depth").toString();
    	String depth = depths.equals("1") ? "3" : "4";
    	String rbttype = params.get("r_bdtype") == null ? depth.equals("3")?"C04001" : "C0400101" : params.get("r_bdtype").toString();

    	svcMap.put("r_cccode", rbttype);
		svcMap.put("r_depth", depth);


		svcMap.put("referenceCategoryList", commonCodeSvc.getCategoryListWithDepth(svcMap));//카테고리

		return svcMap;
    }
    
    
}
