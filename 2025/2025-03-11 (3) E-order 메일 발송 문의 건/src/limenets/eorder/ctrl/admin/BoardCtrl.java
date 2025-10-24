package com.limenets.eorder.ctrl.admin;

import java.util.HashMap;
import java.util.Map;

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
import org.springframework.web.bind.annotation.GetMapping;
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
import com.limenets.eorder.dto.TosConfigDto;
import com.limenets.eorder.svc.BoardSvc;
import com.limenets.eorder.svc.CommonCodeSvc;
import com.limenets.eorder.svc.ConfigSvc;
import com.limenets.eorder.svc.CustomerSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.TempleteSvc;
import com.limenets.eorder.svc.UserSvc;

/**
 * Admin 게시판 컨트롤러.
 */
@Controller
@RequestMapping("/admin/board/*")
public class BoardCtrl {
	private static final Logger logger = LoggerFactory.getLogger(BoardCtrl.class);
	
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
	 * 공지사항 폼
	 * @작성일 : 2020. 3. 12.
	 * @작성자 : isaac
	 */
	@GetMapping(value="noticeList")
	public String noticeList(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) {		
		return "admin/board/noticeList";
	}
	
	/**
	 * FAQ 폼
	 * @작성일 : 2020. 3. 22.
	 * @작성자 : isaac
	 */
	@GetMapping(value="faqList")
	public String faqList(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) {		
		model.addAttribute("faqCategoryList", commonCodeSvc.getDetailList("C03"));
		return "admin/board/faqList";
	}
	//
	
	//#########################################
	/**
     * QnA 폼
     * @작성일 : 2021. 10. 12.
     * @작성자 : 
     */
	@GetMapping(value="QnAList")
    public String QnAList(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) {       
        model.addAttribute("QnACategoryList", commonCodeSvc.getDetailList("C03"));
        return "admin/board/QnAList";
    }
	//#########################################
	
	
	/**
	 * 자료실 폼
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 */
	@GetMapping(value="referenceList")
	public String referenceList(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) {		
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
		
		return "admin/board/referenceList";
	}
	
	/**
	 * 공지사항 폼 > 팝업 폼. 
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 * @param process = addEditPop , viewPop
	 */
	@PostMapping(value="/notice/pop/{process}")
	public String noticeAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {
		
		String r_bdseq = Converter.toStr(params.get("r_bdseq"));
		
		//등록/수정 분기처리
		if(!StringUtils.isEmpty(r_bdseq)) {
			if(boardSvc.getBoardCnt(r_bdseq) < 1)  throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
			
			BoardDto resMap = boardSvc.getBoardOne(params);
			model.addAttribute("boardOne", resMap);
			model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));
		}
			
		return "admin/board/"+boardSvc.getPopPageName(params, process);
	}
	
	/**
	 * FAQ 폼 > 팝업 폼. 
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 * @param process = addEditPop , viewPop
	 */
	@PostMapping(value="/faq/pop/{process}")
	public String faqAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {
		
		String r_bdseq = Converter.toStr(params.get("r_bdseq"));
		logger.debug("***********************************************");
        logger.debug("admin단"+ r_bdseq);
		//등록/수정 분기처리
        logger.debug("######### StringUtils.isEmpty(r_bdseq) ############ l");
        //logger.debug(StringUtils.isEmpty(r_bdseq));   ///// false라고 뜸
        
		if(!StringUtils.isEmpty(r_bdseq)) { //trues
			if(boardSvc.getBoardCnt(r_bdseq) < 1)  throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
			
			BoardDto resMap = boardSvc.getBoardOne(params);
			
			logger.debug("######### 분기점 확인중 ############ l");
	        //logger.debug(resMap);			
			
			model.addAttribute("boardOne", resMap);
			model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));
		}
		
	    model.addAttribute("faqCategoryList", commonCodeSvc.getDetailList("C03"));//FAQ 유형
	
	    logger.debug("==========================================");
	    logger.debug("admin단"+ r_bdseq);
	    logger.debug("======================프로세스");
        logger.debug(process);
        logger.debug("======================리턴값=========================");

        logger.debug("admin/board/"+boardSvc.getPopPageName(params, process));
		return "admin/board/"+boardSvc.getPopPageName(params, process);
	}
//#############################################
	@PostMapping(value="/QnA/pop/{process}")
    public String QnAAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {
        
        String r_bdseq = Converter.toStr(params.get("r_bdseq"));
        
        //등록/수정 분기처리
        if(!StringUtils.isEmpty(r_bdseq)) {
            if(boardSvc.getBoardCnt(r_bdseq) < 1)  throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
            
            BoardDto resMap = boardSvc.getBoardOne(params);
            model.addAttribute("boardOne", resMap);
            model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));
        }
        
        model.addAttribute("QnACategoryList", commonCodeSvc.getDetailList("C03"));//QnA 유형
            
        return "admin/board/"+boardSvc.getPopPageName(params, process);
    }
	//################################################
	/**
	 * 자료실 폼 > 팝업 폼. 
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 * @param process = addEditPop , viewPop
	 */
	@PostMapping(value="/reference/pop/{process}")
	public String referenceAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {
		
		String r_bdseq = Converter.toStr(params.get("r_bdseq"));
		
		//등록/수정 분기처리
		if(!StringUtils.isEmpty(r_bdseq)) {
			if(boardSvc.getBoardCnt(r_bdseq) < 1)  throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
			
			BoardDto resMap = boardSvc.getBoardOne(params);
			model.addAttribute("boardOne", resMap);
			model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));
			
//			if(process.contains("viewPop")) {
//				boardSvc.addViewCnt(params);
//			}
			
			String m_bdtype3 = resMap.getBD_TYPE3();
			
			if( (m_bdtype3 != null) && (m_bdtype3.length() == 10) ) {
				model.addAttribute("referenceCategory2List", commonCodeSvc.getDetailList(m_bdtype3.substring(0, 6)));
				model.addAttribute("referenceCategory3List", commonCodeSvc.getDetailList(m_bdtype3.substring(0, 8)));//자료실 유형
			}
		} 
		
		model.addAttribute("referenceCategoryList", commonCodeSvc.getDetailList("C04"));//자료실 유형
			
		return "admin/board/"+boardSvc.getPopPageName(params, process);
	}
    //################################################
	
	/**
	 * 게시판(공지사항,FAQ,자료실) 폼 > 저장/수정 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateNoticeAjax")
	public Object insertUpdateNoticeAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		Map<String, Object> resMap = boardSvc.insertUpdateBoard(params, req, loginDto);
		return resMap;
	}
	
	/**
	 * FAQ 폼 > 저장/수정 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateFaqAjax")
	public Object insertUpdateFaqAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		Map<String, Object> resMap = boardSvc.insertUpdateBoard(params, req, loginDto);
		logger.debug("insertUpdateFaqAjzx");
		return resMap;
	}
	//################################################
    /**
     * QNA 폼 > 저장/수정 Ajax.
     * @작성일 : 2020. 3. 24.
     * @작성자 : isaac
     */
    @ResponseBody
    @PostMapping(value="insertUpdateQnAAjax")
    public Object insertUpdateQnAAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
        Map<String, Object> resMap = boardSvc.insertUpdateBoard(params, req, loginDto);
        return resMap;
    }
	
	/**
	 * 자료실 폼 > 저장/수정 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateReferenceAjax")
	public Object insertUpdateReferenceAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		
	    logger.debug("######insertUpdateREFERENCEAjax#########BoarCTRL   PARAMS AJAX ERROR CHECK CTRL CONTROLLL#######################");
	       
//	    for( String key : params.keySet() ){
//	        String value = String.valueOf( params.get(key)); 
//	        logger.debug( String.format("키 : "+key+", 값 : "+value));
//	    }
	    
	    Map<String, Object> resMap = boardSvc.insertUpdateBoard(params, req, loginDto);
		
	    
	    logger.debug("######insertUpdateREFERENCEAjax#########BoarCTRL   resMap  AJAX ERROR CHECK CTRL CONTROLLL#######################");
	       
//	       for( String key : resMap.keySet() ){
//	           String value = String.valueOf( resMap.get(key)); 
//	           logger.debug( String.format("키 : "+key+", 값 : "+value));
//	       }
		
		return resMap;
	}
	
	/**
	 * 공지사항 폼 > 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="getNoticeListAjax")
	public Object getNoticeListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
		Map<String, Object> resMap = boardSvc.getBoardList(params, req);
		return resMap;
	}
	
	/**
	 *FAQ 폼 > 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="getFaqListAjax")
	public Object getFaqListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
		Map<String, Object> resMap = boardSvc.getBoardList(params, req);
		logger.debug("#########BoardCtr############l");
		for(String key : resMap.keySet()) { 
		    String value = String.valueOf( resMap.get(key)); 
            logger.debug(key + " : " + value); 
            }
		return resMap;
	}
	   //################################################
    @ResponseBody
    @PostMapping(value="getQnAListAjax")
    public Object getQnAListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
        Map<String, Object> resMap = boardSvc.getBoardList(params, req);
        return resMap;
    }
	
	   //################################################

	/**
	 * 자료실 폼 > 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="getReferenceListAjax")
	public Object getReferenceListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
		Map<String, Object> resMap = boardSvc.getBoardList(params, req);
		return resMap;
	}
	
	@ResponseBody
	@PostMapping(value="getPopupReferenceListAjax")
	public Object getPopupReferenceListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
		Map<String, Object> resMap = boardSvc.getPopupReferenceList(params, req);
		return resMap;
	}
	
	/**
	 * 자료실 폼 > 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="addViewCountAjax")
	public Object addViewCountAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
		//Map<String, Object> resMap = boardSvc.getBoardList(params, req);
		int n = boardSvc.addViewCnt(params);
		return n;
	}
	
	/**
	 * 공지사항 폼 > 삭제 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@RequestMapping(value="deleteNoticeAjax", method=RequestMethod.POST)
	public Object deleteNoticeAjax(@RequestParam Map<String, Object> params) {
		Map<String, Object> resMap = boardSvc.deleteBoard(params);			
		return resMap;			
	}
	
	/**
	 * FAQ 폼 > 삭제 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@RequestMapping(value="deleteFaqAjax", method=RequestMethod.POST)
	public Object deleteFaqAjax(@RequestParam Map<String, Object> params) {
	    logger.debug("#########DELETE FAQ#####ADMIN CTRL#######  PARAM");
        for(String key : params.keySet()) { 
            String value = String.valueOf( params.get(key)); 
            logger.debug(key + " : " + value); 
            }
	    
	    
	    Map<String, Object> resMap = boardSvc.deleteBoard(params);
		logger.debug("#########DELETE FAQ#####ADMIN CTRL####### RESMAPl");
        for(String key : resMap.keySet()) { 
            String value = String.valueOf( resMap.get(key)); 
            logger.debug(key + " : " + value); 
            }
		return resMap;			
	}
	
	
	   //################################################
	@ResponseBody
    @RequestMapping(value="deleteQnAAjax", method=RequestMethod.POST)
    public Object deleteQnAAjax(@RequestParam Map<String, Object> params) {
        Map<String, Object> resMap = boardSvc.deleteBoard(params);          
        return resMap;          
    }
	
	
	
	
	//################################################

	/**
	 * 자료실 폼 > 삭제 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@RequestMapping(value="deleteReferenceAjax", method=RequestMethod.POST)
	public Object deleteReferenceAjax(@RequestParam Map<String, Object> params) {
		Map<String, Object> resMap = boardSvc.deleteBoard(params);			
		return resMap;			
	}
	
	/**
	 * 공지사항 파일 다운로드.
	 * @작성일 : 2020. 3. 24.
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
	 * @작성일 : 2020. 3. 24.
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
	
	/**
	 * 약관관리 폼
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 */
	@GetMapping(value="tosConfigList")
	public String tosConfigList(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) {		
		return "admin/board/tosConfigList";
	}
	
	/**
	 * 약관관리 폼 > 팝업 폼. 
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 * @param process = addEditPop , viewPop
	 */
	@PostMapping(value="/tosConfig/pop/{process}")
	public String tosConfigAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		
		String r_tscseq = Converter.toStr(params.get("r_tscseq"));
		
		//등록/수정 분기처리
		if(!StringUtils.isEmpty(r_tscseq)) {
			if(boardSvc.getTosConfigCnt(r_tscseq) < 1)  throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
			
			TosConfigDto resMap = boardSvc.getTosConfigOne(params);
			model.addAttribute("tosConfigOne", resMap);
			model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getTSC_CONTENT())));
		}
		
		return "admin/board/"+boardSvc.getPopPageName(params, process);
	}
	
	/**
	 * 약관관리 폼 > 저장/수정 Ajax.
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateTosConfigAjax")
	public Object insertUpdateTosConfigAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		Map<String, Object> resMap = boardSvc.insertUpdateTosConfig(params, req, loginDto);
		return resMap;
	}
	
	/**
	 * 약관관리 폼 > 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="getTosConfigListAjax")
	public Object tosConfigListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
		Map<String, Object> resMap = boardSvc.getTosConfigListAjax(params, req);
		return resMap;
	}
	
	/**
	 * 약관관리 폼 > 삭제 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@RequestMapping(value="deleteTosConfigAjax", method=RequestMethod.POST)
	public Object deleteTosConfigAjax(@RequestParam Map<String, Object> params) {
		Map<String, Object> resMap = boardSvc.deleteTosConfig(params);			
		return resMap;			
	}
	
	/**
	 * 월간일정 폼
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@GetMapping(value="scheduleList")
	public String scheduleList(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) {		
		return "admin/board/scheduleList";
	}
	
	/**
	 * 월간일정 폼 > 모든 일정 가져오기 Ajax.
	 * @작성일 : 2020. 3. 25.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@GetMapping(value="getScheduleListAjax")
	public Object getScheduleListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		return boardSvc.getScheduleAjax(params);
	}
	
	/**
	 * 월간일정 폼 >  특정날짜 일정 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 25.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="getScheduleListByDateAjax")
	public Object getScheduleListByDateAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {	
//		Map<String, Object> resMap = boardSvc.getScheduleList(params);
		return boardSvc.getScheduleList(params);
	}
	
	/**
	 * 월간일정 폼 >  특정날짜 일정 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 25.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="getScheduleOneAjax")
	public Object getScheduleOneAjax(@RequestParam Map<String, Object> params,HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {	
//		Map<String, Object> resMap = boardSvc.getScheduleList(params);
		return boardSvc.getScheduleOne(params);
	}
	
	/**
	 * 월간일정 폼 >  일정 저장 Ajax.
	 * @작성일 : 2020. 3. 25.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateScheduleAjax")
	public Object setShareWorkAjax(@RequestParam final Map<String, Object> params,HttpServletRequest req, HttpServletResponse res, LoginDto loginDto) throws LimeBizException{
		return boardSvc.insertUpdateSchedule(params, loginDto);
	}
	
	/**
	 * 월간일정 폼 > 삭제 Ajax.
	 * @작성일 : 2020. 3. 31.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@RequestMapping(value="deleteScheduleAjax", method=RequestMethod.POST)
	public Object deleteScheduleAjax(@RequestParam Map<String, Object> params) {
		Map<String, Object> resMap = boardSvc.deleteSchedule(params);			
		return resMap;			
	}
	
	/**
	 * 배너관리 폼
	 * @작성일 : 2020. 3. 31.
	 * @작성자 : isaac
	 */
	@GetMapping(value="bannerList")
	public String bannerList(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) {		
		return "admin/board/bannerList";
	}
	
	/**
	 * 배너관리 폼 > 배너관리 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 31.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="getBannerListAjax")
	public Object getBannerListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
		Map<String, Object> resMap = boardSvc.getBannerListAjax(params, model, req);
		return resMap;
	}
	
	/**
	 * 배너관리 폼 > 팝업 폼.
	 * @작성일 : 2020. 3. 31.
	 * @작성자 : isaac
	 * {process} 정의.
	 * - addEditPop : 배너관리 등록/수정 팝업 폼.
	 * - viewPop : 배너관리 확인 팝업 폼.
	 * @param 'r_bnseq' 수정할 배너 시퀀스 [수정시에만 필수]
	 */
	@PostMapping(value="/banner/pop/{process}")
	public String bannerAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		 String r_bnseq = Converter.toStr(params.get("r_bnseq"));
		 String page_type = (StringUtils.equals("addEditPop", process)) ? "ADD" : "VIEW";

		 if(!StringUtils.equals("", r_bnseq)) { // 수정 Map<String, Object> userMap =
			 Map<String, Object> bannerMap = boardSvc.getBannerOneBySeq(r_bnseq);
			 if(CollectionUtils.isEmpty(bannerMap)) {
				throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR); }
			 model.addAttribute("bannerOne", bannerMap);
			 page_type = (StringUtils.equals("addEditPop", process)) ? "EDIT" : "VIEW";
		 }

		 model.addAttribute("page_type", page_type); // return ADD/EDIT/VIEW.
		
		 return "admin/board/bannerAddEditPop";
	}
	
	
	
	/**
	 * 배너관리 폼 > 저장/수정 Ajax.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="insertUpdateBannerAjax")
	public Object insertUpdateBannerAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		Map<String, Object> resMap = boardSvc.insertUpdateBanner(params, req, loginDto);
		return resMap;
	}

	/**
	 * 배너이미지 다운로드.
	 * @작성일 : 2020. 4. 2.
	 * @작성자 : isaac
	 */
	@PostMapping(value="bannerFileDown")
	public ModelAndView bannerFileDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model) throws LimeBizException {
		ModelAndView mv = boardSvc.bnPuFileDown(params,req,model,"banner");

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
	 * 배너관리 폼 > 삭제 Ajax.
	 * @작성일 : 2020. 4. 4.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@RequestMapping(value="deleteBannerAjax", method=RequestMethod.POST) 
	public Object deleteBannerAjax(@RequestParam Map<String, Object> params) throws LimeBizException {
		Map<String, Object> resMap = boardSvc.deleteBanner(params);
		return resMap;
	}
	
	/**
	 * 팝업관리 폼
	 * @작성일 : 2020. 3. 31.
	 * @작성자 : isaac
	 */
	@GetMapping(value="popupList")
	public String popupList(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) {		
		return "admin/board/popupList";
	}
	
	/**
	 * 팝업관리 폼 > 팝업관리 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 31.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="getPopupListAjax")
	public Object getPopupListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		Map<String, Object> resMap = boardSvc.getPopupListAjax(params, model, req);
		return resMap;
	}
	
	/**
	 * 팝업관리 폼 > 팝업 폼.
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : isaac
	 * {process} 정의.
	 * - addEditPop : 배너관리 등록/수정 팝업 폼.
	 * - viewPop : 배너관리 확인 팝업 폼.
	 * @param 'r_bnseq' 수정할 배너 [수정시에만 필수]
	 */
	@PostMapping(value="/popup/pop/{process}")
	public String popupAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		String r_puseq = Converter.toStr(params.get("r_puseq"));
		String page_type = (StringUtils.equals("addEditPop", process)) ? "ADD" : "VIEW";
		logger.debug("#######################################");
		logger.debug("popupAddEditViewPop");
		logger.debug(r_puseq+ " "+ page_type);
        
		
		
		
		if(!StringUtils.equals("", r_puseq)) { // 수정 Map<String, Object> userMap =
			 Map<String, Object> popupMap = boardSvc.getPopupOneBySeq(r_puseq);
			 if(CollectionUtils.isEmpty(popupMap)) {
				throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR); }
			 model.addAttribute("popupOne", popupMap);
			 page_type = (StringUtils.equals("addEditPop", process)) ? "EDIT" : "VIEW";
		 }
		
		model.addAttribute("page_type", page_type); // return ADD/EDIT/VIEW.
		return "admin/board/popupAddEditPop";
	}
	
	/**
	 * 팝업관리 폼 > 저장/수정 Ajax.
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@PostMapping(value="insertUpdatePopupAjax")
	public Object insertUpdatePopupAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
		Map<String, Object> resMap = boardSvc.insertUpdatePopup(params, req, loginDto);
		return resMap;
	}

	/**
	 * 팝업관리 폼 > 삭제 Ajax.
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : isaac
	 */
	@ResponseBody
	@RequestMapping(value="deletePopupAjax", method=RequestMethod.POST)
	public Object deletePopupAjax(@RequestParam Map<String, Object> params) {
		Map<String, Object> resMap = boardSvc.deletePopup(params);
		return resMap;
	}
	
	/**
	 * 팝업이미지 다운로드.
	 * @작성일 : 2020. 4. 2.
	 * @작성자 : isaac
	 */
	@PostMapping(value="popupFileDown")
	public ModelAndView popupFileDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, Model model) throws LimeBizException {
		ModelAndView mv = boardSvc.bnPuFileDown(params,req,model,"popup");

		// 파일 다운로드 종료후 뷰단에서 종료여부 체크중인 쿠키 생성.
		//logger.debug("### 파일다운로드 종료 ###");
		String fileToken = Converter.toStr(params.get("filetoken"));
		Cookie fileCookie = new Cookie(fileToken, "true");
		fileCookie.setMaxAge(60*1); // 1분만 저장.
		res.addCookie(fileCookie);
		//logger.debug("fileToken : {}", fileToken);

		return mv;
	}
	
	
    //=======================================================================================================
	
	/**
     * sample 폼
     * @작성일 : 2020. 3. 23.
     * @작성자 : isaac
     */
    @GetMapping(value="sampleList")
    public String sampleList(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) {     
        model.addAttribute("sampleCategoryList", commonCodeSvc.getDetailList("C04"));//자료실 유형
        return "admin/board/sampleList";
    }
    
    /**
     * chatFeedback 폼
     * @작성일 : 2020. 3. 23.
     * @작성자 : isaac
     */
    @GetMapping(value="chatFeedback")
    public String chatFeedback(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) {     
        //model.addAttribute("chatFeedbackCategoryList", commonCodeSvc.getDetailList("C04"));//자료실 유형
        return "admin/board/chatFeedback";
    }
	
	
	
    /**
    * 샘플 폼 > 팝업 폼. 
    * @작성일 : 2020. 3. 23.
    * @작성자 : isaac
    * @param process = addEditPop , viewPop
    */
   @PostMapping(value="/sample/pop/{process}")
   public String sampleAddEditViewPop(@PathVariable("process") String process, @RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {
       
       String r_bdseq = Converter.toStr(params.get("r_bdseq"));
       
       //등록/수정 분기처리
       if(!StringUtils.isEmpty(r_bdseq)) {
           if(boardSvc.getBoardCnt(r_bdseq) < 1)  throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
           
           
           logger.debug("###############BoardCTRL   PARAMS AJAX ERROR CHECK#######################");
           for( String key : params.keySet() ){
               String value = String.valueOf( params.get(key)); 
               logger.debug( String.format("키 : "+key+", 값 : "+value));
               
           }
           
           BoardDto resMap = boardSvc.getBoardOne(params);

           
           
           model.addAttribute("boardOne", resMap);
           model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));
           model.addAttribute("restoreXSSContent2", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_REPLY())));

       }
       
       model.addAttribute("sampleCategoryList", commonCodeSvc.getDetailList("C04"));//자료실 유형
           
       return "admin/board/"+boardSvc.getPopPageName(params, process);
   }
	
   /**
    * 샘플 폼 > 저장/수정 Ajax.
    * @작성일 : 2020. 3. 24.
    * @작성자 : isaac
    */
   @ResponseBody
   @PostMapping(value="insertUpdatesampleAjax")
   public Object insertUpdatesampleAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws LimeBizException {
       logger.debug("######insertUpdatesampleAjax#########BoarCTRL   PARAMS AJAX ERROR CHECK CTRL CONTROLLL#######################");
       
       for( String key : params.keySet() ){
           String value = String.valueOf( params.get(key)); 
           logger.debug( String.format("키 : "+key+", 값 : "+value));
       }
       
       Map<String, Object> resMap = boardSvc.insertUpdateBoard(params, req, loginDto);
       
       
          logger.debug("######insertUpdatesampleAjax#########BoarCTRL   RESMAP AJAX ERROR CHECK CTRL CONTROLLL#######################");
       
       for( String key : resMap.keySet() ){
           String value = String.valueOf( resMap.get(key)); 
           logger.debug( String.format("키 : "+key+", 값 : "+value));
       }
       
       return resMap;
   }
   
   /**
    * chat feedback list 가져오기 Ajax.
    * @작성일 : 2024.01.30.
    * @작성자 : squall
    */
   @ResponseBody
   @PostMapping(value="getchatFeedbackListAjax")
   public Object getchatFeedbackListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
	   params.put("r_bdid","chat");
	   Map<String, Object> resMap = boardSvc.getChatFeedbackList(params,req);
       return resMap;
   }
   
   @RequestMapping(value="chatViewPop", method={RequestMethod.GET, RequestMethod.POST})
   public String chatViewPop(@RequestParam Map<String, Object> params, LoginDto loginDto, Model model) throws LimeBizException {

      BoardDto resMap = boardSvc.getBoardOne(params);
      BoardDto resMap2 = boardSvc.getBoardOne2(params);

      model.addAttribute("boardOne", resMap);
      model.addAttribute("boardOne2", resMap2);

      model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(resMap.getBD_CONTENT())));

      return "admin/board/chatViewPop";
   }
   
   /**
    * 샘플 폼 > 리스트 가져오기 Ajax.
    * @작성일 : 2020. 3. 24.
    * @작성자 : isaac
    */
   @ResponseBody
   @PostMapping(value="getsampleListAjax")
   public Object getsampleListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) {
       Map<String, Object> resMap = boardSvc.getBoardList(params, req);
       return resMap;
   }
   
   /**
    * 샘플 폼 > 삭제 Ajax.
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
    * 샘플 파일 다운로드.
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
    * 샘플 이메일 발송 Ajax.
    * @작성일 : 2021. 4. 29.
    * @작성자 : jsh
    */
  /* 
   @ResponseBody
   @PostMapping(value="setsampleMailLog")
   public Object setsampleMailLog(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
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
       //mail.sendMail(smtpHost,title, r_shiptorepNm, r_shiptoEmail, shopName, smtpSender, contentStr, null, "");
       mail.sendGMail(title, r_shiptorepNm, r_shiptoEmail, shopName, smtpSender, contentStr, null, "");
       
     
       params.put("userId", loginDto.getUserId());
       int result = orderSvc.setQmsMailLog(params);
       return true;
   }  
   
   */
   
   
   
    //=======================================================================================================

}
