package com.limenets.eorder.svc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.common.util.FileDown;
import com.limenets.common.util.FileUpload;
import com.limenets.common.util.HttpUtils;
import com.limenets.common.util.Pager;
import com.limenets.eorder.dao.BannerDao;
import com.limenets.eorder.dao.BoardDao;
import com.limenets.eorder.dao.PopupDao;
import com.limenets.eorder.dao.ScheduleDao;
import com.limenets.eorder.dao.TosConfigDao;
import com.limenets.eorder.dto.BoardDto;
import com.limenets.eorder.dto.TosConfigDto;

/**
 * 게시판 서비스
 */
@Service
public class BoardSvc {
	private static final Logger logger = LoggerFactory.getLogger(BoardSvc.class);
	
	@Resource(name="fileUpload") private FileUpload fileUpload;
	@Inject private BoardDao boardDao;	
	@Inject private TosConfigDao tosConfigDao;	
	@Inject private ScheduleDao scheduleDao;	
	@Inject private BannerDao bannerDao;	
	@Inject private PopupDao popupDao;	
	@Inject private CommonCodeSvc commonCodeSvc;
	
	/**
	 * 게시판(공지사항,FAQ,자료실) 저장/수정 Ajax.
	 * @작성일 : 2020. 3. 13.
	 * @작성자 : isaac
	 */
	public Map<String, Object> insertUpdateBoard(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws LimeBizException {		
									
		
		String r_bdseq = Converter.toStr(params.get("r_bdseq"));
		
		String process_type = Converter.toStr(params.get("r_processtype"));
	    String m_bdreply = Converter.toStr(params.get("m_bdreply"));
        System.out.println("존재 하나요?" + m_bdreply);

		System.out.println("###############BoardSvc   PARAMS AJAX ERROR CHECK0000000000#######################");
//        for( String key : params.keySet() ){
//            String value = String.valueOf( params.get(key)); 
//            System.out.println( String.format("키 : "+key+", 값 : "+value));
//        }
        
		if(StringUtils.equals("EDIT", process_type))    // 수정처리 되고 있을때
		{
			if(StringUtils.equals("", r_bdseq)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2); 
			this.setBoardFiles(params, req);
			
			params.put("m_bdmoid", loginDto.getUserId());
			params.put("m_bdcontent", HttpUtils.restoreXss(Converter.toStr(params.get("m_bdcontent"))));
			//params.put("m_bdcontent", HttpUtils.restoreXss(Converter.toStr(params.get("m_bd"))));
			System.out.println("###############BoardSvc   PARAMS AJAX ERROR CHECK111111111111#######################");
//	        for( String key : params.keySet() ){
//	            String value = String.valueOf( params.get(key)); 
//	            System.out.println( String.format("키 : "+key+", 값 : "+value));
//	        }
			boardDao.up(params);
		}
		else if(StringUtils.equals("REDIT", process_type))
		{
            System.out.println("###############HERE IS RERERERE EDIT#######################");

		    if(StringUtils.equals("", r_bdseq)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2); 
            this.setBoardFiles(params, req);
            
            params.put("m_bdmoid", loginDto.getUserId());
            params.put("m_bdreply", HttpUtils.restoreXss(Converter.toStr(params.get("m_bdreply"))));

            System.out.println("###############BoardSvc   PARAMS AJAX ERROR CHECK111111111111#######################");
//            for( String key : params.keySet() ){
//                String value = String.valueOf( params.get(key)); 
//                System.out.println( String.format("키 : "+key+", 값 : "+value));
//            }
            boardDao.reply(params);
        }

        else if(StringUtils.equals("SFEDIT", process_type))   // 샘플게시판 프론트에서 수정할때,  수정 날짜 바뀌는 것을 막기 위해서 여기서는 modate 를 update에서 제외함
        {
            System.out.println("###############HERE IS SFSFSFSFSFSF EDIT#######################");

            if(StringUtils.equals("", r_bdseq)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2); 
            this.setBoardFiles(params, req);
            
            params.put("m_bdmoid", loginDto.getUserId());
            params.put("m_bdreply", HttpUtils.restoreXss(Converter.toStr(params.get("m_bdreply"))));

            System.out.println("###############BoardSvc   PARAMS AJAX ERROR CHECK111111111111#######################");
//            for( String key : params.keySet() ){
//                String value = String.valueOf( params.get(key)); 
//                System.out.println( String.format("키 : "+key+", 값 : "+value));
//            }
            boardDao.sampleFrontUp(params);
        }
		else    // 처음 입력 처리 되고 있을때	    
		{			
			this.setBoardFiles(params, req);
			
			
			params.put("m_bdinid", loginDto.getUserId());
			params.put("m_bdcontent", HttpUtils.restoreXss(Converter.toStr(params.get("m_bdcontent"))));
            params.put("m_bdreply", HttpUtils.restoreXss(Converter.toStr(params.get("m_bdreply"))));


			boardDao.in(params);
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);		
	}
	
	/**
	 * 게시판(공지사항,FAQ,자료실) 첨부파일체크 및 업로드 후 param에 파일명 및 확장자 저장
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 */
	public void setBoardFiles(Map<String, Object> params, HttpServletRequest req) throws LimeBizException {
		//업로드 파일 처리
		List<Map<String, Object>> fList = new ArrayList<>();
		
		// 파일업로드 : 동일한 파일명으로 덮어쓰기.
		String sepa = System.getProperty("file.separator");
		String folderName = "board";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
		List<Map<String, Object>> retFileList = new ArrayList<>();
		
		//파일업로드	
		try {
			if (!MultipartHttpServletRequest.class.isInstance(req)) {
				throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
			}
			
			MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;
			
			//허용 가능한 파일 확장자가 다르기 때문에 따로 처리함
			retFileList = fileUpload.execOneField(mtreq, "docFiles", uploadDir, "m_bdfile");
			fList.addAll(retFileList);
			retFileList.clear();
			
			retFileList = fileUpload.execOneField(mtreq, "imageFiles", uploadDir, "m_bdimage");
			fList.addAll(retFileList);
			retFileList.clear();
			
			logger.debug("fList : {}", fList);
		} catch (Exception e) {
			fileUpload.deleteList(fList, uploadDir);
			throw e;
		}
				
		if(!fList.isEmpty()){
			
			for(Map<String, Object> file : fList) {
				if(StringUtils.equals("m_bdfile", Converter.toStr(file.get("fieldName")))) {
					params.put("m_bdfile", Converter.toStr(file.get("saveFileName")));
					params.put("m_bdfiletype", Converter.toStr(file.get("mimeType")));					
				}
				else if(StringUtils.equals("m_bdimage", Converter.toStr(file.get("fieldName")))) 
				{
					params.put("m_bdimage", Converter.toStr(file.get("saveFileName")));
					params.put("m_bdimagetype", Converter.toStr(file.get("mimeType")));
				}
			}			
			
		}	
	}
	
	/**
	 * 게시판 리스트 가져오기
	 * @작성일 : 2020. 3. 16.
	 * @작성자 : isaac
	 */
	public Map<String, Object> getBoardList(Map<String, Object> params, HttpServletRequest req){
		Map<String, Object> resMap = new HashMap<>();
		
		int totalCnt = this.getBoardCnt(params);
		String r_bdid = Converter.toStr(params.get("r_bdid")); 
       /* if(r_bdid == "sample")
        {
            int replycnt = this.getBoardCntrep(params);
            System.out.println("---------***********-----페이징 뜯어고치기 시작--------***********---------------");
            System.out.println(replycnt);
            totalCnt = totalCnt + replycnt;
        }
		*/
		this.initPager(params,resMap,req,totalCnt);
		
		
		String r_orderby = "";
		String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
		String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
	    //System.out.println(sidx+" " +sord);
        //System.out.println("확인중 확인중");

		r_orderby = sidx + " " + sord;		 
		if(StringUtils.equals("", sidx))  r_orderby = "BD_NOTICEYN DESC , BD_INDATE DESC ";  //BDA_NOTICHK 공지사항은 리스트 항시 상단에 출력
		params.put("r_orderby", r_orderby);
		
		// 엑셀 다운로드.
		String where = Converter.toStr(params.get("where"));
		if ("excel".equals(where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}
		
		String bdtype = params.get("r_bdtype")==null ? "" : params.get("r_bdtype").toString();
		String bdtype2 = params.get("r_bdtype2")==null ? "" : params.get("r_bdtype2").toString();
		
		List<BoardDto> list = this.getBoardList(params);
		resMap.put("list", list);		
		//System.out.println(list);

		Map<String, Object> svcMap = new HashMap<>();	
		svcMap.put("r_cccode", bdtype);
		svcMap.put("r_depth", 3);
		resMap.put("referenceCategory2List", commonCodeSvc.getCategoryListWithDepth(svcMap));
		
		svcMap.clear();
		svcMap.put("r_cccode", bdtype2);
		svcMap.put("r_depth", 4);
		resMap.put("referenceCategory3List", commonCodeSvc.getCategoryListWithDepth(svcMap));
		
		/*System.out.println("###############BoardSvc   PARAMS#######################");
        for( String key : params.keySet() ){
            String value = String.valueOf( params.get(key)); 
            System.out.println( String.format("키 : "+key+", 값 : "+value));
        }
		
		System.out.println("####################BoardSvc   RESMAP###################");
		for(String key : resMap.keySet()) { 
		    String value = String.valueOf( resMap.get(key)); 
		    System.out.println("키 : "+key+", 값 : "+value); 
            }*/

		return resMap;
	}
	
	public Map<String, Object> getPopupReferenceList(Map<String, Object> params, HttpServletRequest req){
		Map<String, Object> resMap = new HashMap<>();
		String bdtype = params.get("r_bdtype")==null ? "" : params.get("r_bdtype").toString();
		String bdtype2 = params.get("r_bdtype2")==null ? "" : params.get("r_bdtype2").toString();
		String bdtype3 = params.get("r_bdtype3")==null ? "" : params.get("r_bdtype3").toString();
		
//		List<BoardDto> list = this.getBoardList(params);
//		resMap.put("list", list);		
//		//System.out.println(list);

		Map<String, Object> svcMap = new HashMap<>();
		if(bdtype.length() == 6) {
			svcMap.clear();
			svcMap.put("r_cccode", bdtype);
			svcMap.put("r_depth", 3);
			resMap.put("referenceCategory2List", commonCodeSvc.getCategoryListWithDepth(svcMap));
			resMap.put("r_bdtype", bdtype);
		} else {
			resMap.put("r_bdtype", "");
		}
		
		if(bdtype2.length() == 8) {
			svcMap.clear();
			svcMap.put("r_cccode", bdtype2);
			svcMap.put("r_depth", 4);
			resMap.put("referenceCategory3List", commonCodeSvc.getCategoryListWithDepth(svcMap));
			resMap.put("r_bdtype2", bdtype2);
		} else {
			resMap.put("r_bdtype2", "");
		}
		
		resMap.put("r_bdtype3", bdtype3);
		
		return resMap;
	}
		
	public List<BoardDto> getBoardList(Map<String, Object> params){
		return boardDao.list(params);
	}

	public Map<String, Object> getBoardListForIndex(String bd_id, String r_startrow, String r_endrow){
		Map<String,Object> svcMap = new HashMap<>();
		Map<String,Object> resMap = new HashMap<>();

		svcMap.put("r_bdid", bd_id);
		svcMap.put("r_startrow", r_startrow);
		svcMap.put("r_endrow", r_endrow);
		svcMap.put("r_orderby", "BD_INDATE DESC");

		List<BoardDto> boardList = boardDao.listForIndex(svcMap);
		resMap.put(bd_id+"List", boardList);

		if(boardList.size() != 0){
			resMap.put(bd_id+"FirstContent", boardList.get(0).getBD_CONTENT());
		}

		return resMap;
	}

	public List<BoardDto> getBoardListForLogin(String bd_id,String bd_displaytype){
		Map<String,Object> svcMap = new HashMap<>();
		//FIXME : 개수제한 없을 시 startorw,endrow 조건절 삭제
		svcMap.put("r_bdid", bd_id);
		svcMap.put("r_bddisplaytype", bd_displaytype);
		svcMap.put("r_orderby", "BD_INDATE DESC");
		return boardDao.listForLogin(svcMap);
	}

	public BoardDto getBoardOne(Map<String, Object> params){
		return boardDao.one(params);
	}

    public BoardDto getBoardOne2(Map<String, Object> params){
        return boardDao.one2(params);
    }
	
	
		
	public int getBoardCnt(Map<String, Object> params) {
		return boardDao.cnt(params);
	}
	
	
    public int getBoardCntrep(Map<String, Object> params) {
        return boardDao.cntrep(params);
    }
    	
    public int addViewCnt(Map<String, Object> params) {
    	return boardDao.addViewCnt(params);
    }
	
	/**
	 * 게시판 시퀀스 번호로 개수 조회
	 * @작성일 : 2020. 3. 16.
	 * @작성자 : isaac
	 * @param r_bdseq
	 * @return
	 */
	public int getBoardCnt(String r_bdseq) {
		Map<String, Object> svcMap = new HashMap<>();
		
		svcMap.put("r_bdseq", r_bdseq);
		return boardDao.cnt(svcMap);
	}
	
	/**
	 * 팝업 페이지 명 조합 후 반환 boardType(notice,faq,reference,tosConfig) + process(addEditPop , viewPop) ex) return noticeAddEditPop , noticeViewPop 
	 * @작성일 : 2020. 3. 17.
	 * @작성자 : isaac
	 * @param params
	 * @param process : addEditPop , viewPop
	 * @return
	 * @throws LimeBizException
	 */
	public String getPopPageName(Map<String, Object> params, String process) throws LimeBizException {
		String bd_id = Converter.toStr(params.get("r_bdid_pop"));
		//System.out.println(bd_id);
		if(CollectionUtils.isEmpty(params) || StringUtils.isEmpty(bd_id)) throw new LimeBizException(MsgCode.DATA_PROCESS_ERROR);			
        //System.out.println("======================프로세스다");

		//System.out.println(process);

		String pageSuffix = process.substring(0,1).toUpperCase() + process.substring(1); //첫 글자 대문자로 변경 ex) addEditPop -> AddEditPop  
		//System.out.println("===================================");
		//System.out.println(pageSuffix);
		return bd_id+pageSuffix;
	}
	
	/**
	 * 게시글 삭제
	 * @작성일 : 2020. 3. 17.
	 * @작성자 : isaac
	 * @return
	 */
	public Map<String, Object> deleteBoard(Map<String, Object> params) {
		Map<String, Object> resMap = new HashMap<>();
		Map<String, Object> svcMap = new HashMap<>();
		
		String r_bdseq = Converter.toStr(params.get("r_bdseq"));
		System.out.println("#########DELETE FAQ#####ADMIN SVC  PARAM#######l");
        for(String key : params.keySet()) { 
            String value = String.valueOf( params.get(key)); 
            System.out.println(key + " : " + value); 
            }
		if(!StringUtils.equals("", r_bdseq)) {
			svcMap.put("r_bdseq", r_bdseq);
			boardDao.del(svcMap);
			resMap = MsgCode.getResultMap(MsgCode.SUCCESS);
		}else {
			return MsgCode.getResultMap(MsgCode.DATA_NOT_FOUND_ERROR);
		}
		System.out.println("#########DELETE FAQ#####ADMIN SVC  SVCMAP#######l");
        for(String key : svcMap.keySet()) { 
            String value = String.valueOf( svcMap.get(key)); 
            System.out.println(key + " : " + value); 
            }
		return resMap;
	}

	/**
	 * 게시판 파일 및 이미지 다운로드
	 * @작성일 : 2020. 3. 22.
	 * @작성자 : isaac
	 * @param params m_bdfile , m_bdimage
	 * @param req
	 * @param model
	 * @return
	 */
	public ModelAndView boardFileDown(Map<String, Object> params, HttpServletRequest req, Model model) {
		String sepa = System.getProperty("file.separator");
		String folderName = "board";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();			
		
//		String m_bdfile = Converter.toStr(params.get("m_bdfile"));
//		String m_bdimage = Converter.toStr(params.get("m_bdimage"));
		String r_filename = HttpUtils.restoreXss(Converter.toStr(params.get("r_filename")));
		String r_filetype = Converter.toStr(params.get("r_filetype"));
				
		Map<String, Object> afMap = new HashMap<>();
		afMap.put("FOLDER_NAME", uploadDir);
		afMap.put("FILE_NAME", r_filename);
		afMap.put("FILE_TYPE", r_filetype);
		model.addAttribute("afMap", afMap);
		
		return new ModelAndView(new FileDown());
	}
	
	
	/**
	 * 약관관리 저장/수정 Ajax.
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 */
	public Map<String, Object> insertUpdateTosConfig(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws LimeBizException {		
									
		
		String r_tscseq = Converter.toStr(params.get("r_tscseq"));
		
		String process_type = Converter.toStr(params.get("r_processtype"));
		
		if(StringUtils.equals("EDIT", process_type)) {
			if(StringUtils.equals("", r_tscseq)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2); 
			
			params.put("m_tscmoid", loginDto.getUserId());
			params.put("m_tsccontent", HttpUtils.restoreXss(Converter.toStr(params.get("m_tsccontent"))));
					
			tosConfigDao.up(params);
		}else {			
			
//			params.put("m_tscauthority", loginDto.getAuthority());		
			params.put("m_tscinid", loginDto.getUserId());				
			params.put("m_tsccontent", HttpUtils.restoreXss(Converter.toStr(params.get("m_tsccontent"))));
			
			tosConfigDao.in(params);
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);		
	}
	
	/**
	 * 약관관리 한 건
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 * @return
	 */
	public TosConfigDto getTosConfigOne(Map<String, Object> params){
		return tosConfigDao.one(params);
	}

	/**
	 * 인덱스 약관
	 * @작성일 : 2020. 4. 24.
	 * @작성자 : isaac
	 * @return
	 */
	public TosConfigDto getTosConfigOneForIndex(Map<String, Object> params){
		return tosConfigDao.oneForIndex(params);
	}

	/**
	 * 약관관리 리스트 가져오기
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 */
	public Map<String, Object> getTosConfigListAjax(Map<String, Object> params, HttpServletRequest req){
		Map<String, Object> resMap = new HashMap<>();
		
		int totalCnt = this.getTosConfigCnt(params);

		this.initPager(params,resMap,req,totalCnt);
		
		String r_orderby = "";
		String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
		String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
		r_orderby = sidx + " " + sord;		 
		if(StringUtils.equals("", sidx))  r_orderby = "TSC_INDATE DESC "; 
		params.put("r_orderby", r_orderby);
		
		// 엑셀 다운로드.
		String where = Converter.toStr(params.get("where"));
		if ("excel".equals(where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}
		
		List<TosConfigDto> list = this.getTosConfigList(params);
		resMap.put("list", list);		
		
		return resMap;
	}
	
	/**
	 * 약관관리 리스트
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : isaac
	 * @param params
	 * @return
	 */
	public List<TosConfigDto> getTosConfigList(Map<String, Object> params){
		return tosConfigDao.list(params);
	}
	
	/**
	 * 약관관리 개수
 	 * @작성일 : 2020. 3. 23.
	 * @param params
	 * @return
	 */
	public int getTosConfigCnt(Map<String, Object> params) {
		return tosConfigDao.cnt(params);
	}
	
	public int getTosConfigCnt(String r_tscseq) {
		Map<String, Object> svcMap = new HashMap<>();
		
		svcMap.put("r_tscseq", r_tscseq);
		return tosConfigDao.cnt(svcMap);
	}
	
	/**
	 * 약관관리 삭제
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : isaac
	 * @return
	 */
	public Map<String, Object> deleteTosConfig(Map<String, Object> params) {
		Map<String, Object> resMap = new HashMap<>();
		Map<String, Object> svcMap = new HashMap<>();
		
		String r_tscseq = Converter.toStr(params.get("r_tscseq"));
		
		if(!StringUtils.equals("", r_tscseq)) {
			svcMap.put("r_tscseq", r_tscseq);
			tosConfigDao.del(svcMap);
			resMap = MsgCode.getResultMap(MsgCode.SUCCESS);
		}else {
			return MsgCode.getResultMap(MsgCode.DATA_NOT_FOUND_ERROR);
		}
		
		return resMap;
	}
	
	/**
	 * 월간일정 가져오기
	 * @작성일 : 2020. 3. 25.
	 * @작성자 : isaac
	 * @param 'start' , 'end'  format ISO 8601 ex) date in "2006-09-01T07:00:00.000+0000"
	 */
	public List<Map<String, Object>> getScheduleAjax(Map<String, Object> params){
		Map<String, Object> svcMap = new HashMap<>();
		//param 형식{start=2020-03-01T00&#58;00&#58;00+09&#58;00, end=2020-04-12T00&#58;00&#58;00+09&#58;00}
		String start = HttpUtils.restoreXss(Converter.toStr(params.get("start")));
		String end = HttpUtils.restoreXss(Converter.toStr(params.get("end")));
		
		String r_start = start.substring(0, 10);
		String r_end = end.substring(0, 10);
		
		svcMap.put("r_start", r_start);
		svcMap.put("r_end", r_end);
		
		List<Map<String, Object>> scdList = scheduleDao.listGroup(svcMap);
		svcMap.clear();
		
		List<Map<String, Object>> resList = new ArrayList<>();
		for (Map<String, Object> scdMap : scdList) {
			Map<String, Object> map = new HashMap<>();
			map.put("start", scdMap.get("SCD_DATE"));
			//map.put("end", endDate);

			map.put("id", scdMap.get("SCD_SEQ"));
//			map.put("id", scdMap.get("SCD_DATE"));
			map.put("url", "#");
//			map.put("title", scdMap.get("SCD_TITLE")); //활성화 시 캘린더에 타이틀 출력

			resList.add(map);
		}
		
		return resList;
	}
	
	
	public List<Map<String, Object>> getScheduleList(Map<String, Object> params){
//		final Map<String, Object> svcMap = new HashMap<>();
//		svcMap.put("r_scddate", sdate);
		return scheduleDao.list(params);
	}
	
	public Map<String, Object> getScheduleOne(Map<String, Object> params) {
		return scheduleDao.one(params);
	}
	
	public int getScheduleCnt(Map<String, Object> params) {
		return scheduleDao.cnt(params);
	}
	
	public int getScheduleCntBySeq(long scd_seq) {
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_scdseq", scd_seq);		
		return scheduleDao.cnt(svcMap);
	}
	
	/**
	 * 월간일정 저장/수정 Ajax.
	 * @작성일 : 2020. 3. 25.
	 * @작성자 : isaac
	 */
	public Map<String, Object> insertUpdateSchedule(Map<String, Object> params, LoginDto loginDto) throws LimeBizException {				
		Map<String, Object> resMap = new HashMap<>();		
		
		long r_scdseq = Converter.toLong(params.get("r_scdseq"));					
		String userId = loginDto.getUserId();
		
		if(r_scdseq > 0) {
			params.put("m_scdmoid", userId);
			scheduleDao.up(params);
		}else {			
			params.put("m_scdinid", userId);
			
			scheduleDao.in(params);
			resMap.put("RES_TITLE", params.get("m_scdtitle"));
			resMap.put("RES_START", params.get("m_scddate"));
			resMap.put("RES_KEY", params.get("nowSeq"));
		}
		
		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		return resMap;
	}
	
	/**
	 * 일정 삭제
	 * @작성일 : 2020. 3. 31.
	 * @작성자 : isaac
	 * @return
	 */
	public Map<String, Object> deleteSchedule(Map<String, Object> params) {
		Map<String, Object> resMap = new HashMap<>();
		Map<String, Object> svcMap = new HashMap<>();
		
		String r_bdseq = Converter.toStr(params.get("r_scdseq"));
		//FIXME : DB에 시퀀스 조회 후 분기처리
		if(!StringUtils.equals("", r_bdseq)) {
			svcMap.put("r_scdseq", r_bdseq);
			scheduleDao.del(svcMap);
			resMap = MsgCode.getResultMap(MsgCode.SUCCESS);
		}else {
			return MsgCode.getResultMap(MsgCode.DATA_NOT_FOUND_ERROR);
		}
		
		return resMap;
	}
	
	/**
	 * 배너 리스트 페이지 가져오기 
	 * @작성일 : 2020. 3. 31. 
	 * @작성자 : isaac
	 * @param 'r_bntype' : 1=로그인 배너, 2: 메인1 배너(상단), 3: 메인2 배너(중단)
	 */
	public Map<String, Object> getBannerListAjax(Map<String, Object> params, Model model,HttpServletRequest req) {
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		String r_bntype = Converter.toStr(params.get("r_bntype"));
		
		int totalCnt = bannerDao.cnt(params);

		this.initPager(params,resMap,req,totalCnt);
		
		List<Map<String, Object>> bannerList = new ArrayList<Map<String ,Object>>();
		// 정렬 설정. orderArr는 그리드의 필드순으로 작성한다. 첫번째것은 디폴트이다.
		
		String r_orderby = "";
		String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
		String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
		r_orderby = sidx + " " + sord;		 
		if(StringUtils.equals("", sidx))  r_orderby = "BN_INDATE DESC ";  //BDA_NOTICHK 공지사항은 리스트 항시 상단에 출력
		params.put("r_orderby", r_orderby);
		
		// 엑셀 다운로드.
		String where = Converter.toStr(params.get("where"));
		if ("excel".equals(where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}
		
		bannerList = this.getBannerList(params);
		resMap.put("list", bannerList);
		
		return resMap;
	}

	/**
	 * 프론트(로그인,인덱스)에 출력할 배너리스트
	 * @param bn_type 1: 로그인메인배너 , 2: 메인1 배너(상단), 3: 메인2 배너  (중단)
	 * @param limitrow 출력개수
	 * @return
	 */
	public List<Map<String ,Object>> getBannerListForFront(String bn_type,int limitrow){
		Map<String, Object> svcMap = new HashMap<String, Object>();

		svcMap.put("r_bntype", bn_type);
		svcMap.put("r_orderby", "BN_INDATE DESC");
		svcMap.put("r_limitrow", limitrow);
		return bannerDao.listForFront(svcMap);
	}

	public List<Map<String ,Object>> getBannerList(Map<String ,Object> params){
		return bannerDao.list(params);
	}
	
	public int getBannerCnt(Map<String, Object> params) {
		return bannerDao.cnt(params);
	}

	public int getBannerCntBySeq(String bn_seq) {
		Map<String, Object> svcMap = new HashMap<>();

		svcMap.put("r_bnseq", bn_seq);
		return bannerDao.cnt(svcMap);
	}
	
	public int getBannerCntByType(String bn_type) {
		Map<String, Object> svcMap = new HashMap<>();

		svcMap.put("r_bntype", bn_type);
		return bannerDao.cnt(svcMap);
	}

	public Map<String ,Object> getBannerOne(Map<String, Object> svcMap){
		return bannerDao.one(svcMap);
	}

	public Map<String ,Object> getBannerOneBySeq(String bn_seq){
		Map<String, Object> svcMap = new HashMap<>();

		svcMap.put("r_bnseq", bn_seq);
		return this.getBannerOne(svcMap);
	}

	/**
	 * 팝업리스트 Ajax
	 * @작성일 : 2020. 3. 31. 
	 * @작성자 : isaac
	 * @param 'r_putype' : 1=로그인 , 2=메인화면
	 * @설명 : r_bntype에 따라 카운트와 리스트를 가져오는 쿼리가 다르다.
	 */
	public Map<String, Object> getPopupListAjax(Map<String, Object> params, Model model, HttpServletRequest req) throws LimeBizException {
		Map<String, Object> resMap = new HashMap<String, Object>();

		int totalCnt = popupDao.cnt(params);

		this.initPager(params,resMap,req,totalCnt);
		
		String r_orderby = "";
		String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
		String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
		r_orderby = sidx + " " + sord;		 
		if(StringUtils.equals("", sidx))  r_orderby = "PU_INDATE DESC ";  //BDA_NOTICHK 공지사항은 리스트 항시 상단에 출력
		params.put("r_orderby", r_orderby);
		
		// 엑셀 다운로드.
		String where = Converter.toStr(params.get("where"));
		if ("excel".equals(where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}

		List<Map<String, Object>> popupList = this.getPopupList(params);
		resMap.put("list", popupList);
		
		return resMap;
					
	}

	/**
	 * 프론트(로그인,인덱스) 팝업리스트 Ajax
	 * @작성일 : 2020. 3. 31.
	 * @작성자 : isaac
	 * @param 'r_putype' : 1=로그인 , 2=메인화면
	 */
	public Map<String, Object> getPopupListForFrontAjax(Map<String, Object> params) throws LimeBizException {
		Map<String, Object> resMap = new HashMap<String, Object>();

		int totalCnt = popupDao.cnt(params);

		String r_orderby = "PU_SDATE DESC";
		params.put("r_orderby", r_orderby);
		List<Map<String, Object>> popupList = popupDao.listForFront(params);
		resMap.put("list", popupList);
		resMap.put("popupCnt", totalCnt);

		return resMap;
	}

	public List<Map<String ,Object>> getPopupList(Map<String ,Object> params){
		return popupDao.list(params);
	}

	public List<Map<String ,Object>> getPopupListForFront(Map<String ,Object> params, String pu_type){
		params.put("r_putype",pu_type);
		return popupDao.listForFront(params);
	}

	public int getPopupCnt(Map<String, Object> params) {
		return popupDao.cnt(params);
	}

	public int getPopupCntBySeq(String pu_seq) {
		Map<String, Object> svcMap = new HashMap<>();

		svcMap.put("r_puseq", pu_seq);
		return popupDao.cnt(svcMap);
	}
	
	public Map<String ,Object> getPopupOne(Map<String, Object> svcMap){
		return popupDao.one(svcMap);
	}
	
	public Map<String ,Object> getPopupOneBySeq(String pu_seq){
		Map<String, Object> svcMap = new HashMap<>();

		svcMap.put("r_puseq", pu_seq);
		return this.getPopupOne(svcMap);
	}
	
	/**
	 * 배너관리 저장/수정 Ajax.
	 * @작성일 : 2020. 4. 2.
	 * @작성자 : isaac
	 * @param params
	 * @param req
	 * @param loginDto
	 */
	public Map<String, Object> insertUpdateBanner(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws LimeBizException {
		Map<String, Object> resMap = new HashMap<>();

		String r_bnseq = Converter.toStr(params.get("r_bnseq"));
		String userId = loginDto.getUserId();

		String m_bnimage = Converter.toStr(params.get("m_bnimage"));

		if(!StringUtils.equals("",r_bnseq)) {
			int bannerCnt = getBannerCntBySeq(r_bnseq);
			if(bannerCnt == 0) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);

//			if (!StringUtils.equals("",m_bnimage)) setBnPuFiles(params,req,"banner");
			this.setBnPuFiles(params,req,"banner");

			params.put("m_bnmoid", userId);
			bannerDao.up(params);
		}else {
//			if (!StringUtils.equals("",m_bnimage)) setBnPuFiles(params,req,"banner");
			this.setBnPuFiles(params,req,"banner");

			params.put("m_bninid", userId);
			params.put("m_bnuseyn","Y");
			bannerDao.in(params);
		}

		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		return resMap;
	}

	/**
	 * 배너 삭제
	 * @작성일 : 2020. 4. 4.
	 * @작성자 : isaac
	 * @return
	 */
	public Map<String, Object> deleteBanner(Map<String, Object> params) throws LimeBizException{
		Map<String, Object> resMap = new HashMap<>();
		Map<String, Object> svcMap = new HashMap<>();

		String r_bnseq = Converter.toStr(params.get("r_bnseq"));
		String r_bntype = Converter.toStr(params.get("r_bntype"));
			
		//로그인메인배너는 1건 이상의 데이터가 있어야 함
		if(StringUtils.equals("1", r_bntype)) {
			int mainBannerCnt = this.getBannerCntByType(r_bntype);
			
			if(mainBannerCnt == 1) throw new LimeBizException(MsgCode.DATA_PROCESS_ERROR);
		}

		if(!StringUtils.equals("", r_bnseq)) {
			svcMap.put("r_bnseq", r_bnseq);
			bannerDao.del(svcMap);
			resMap = MsgCode.getResultMap(MsgCode.SUCCESS);
		}else {
			return MsgCode.getResultMap(MsgCode.DATA_NOT_FOUND_ERROR);
		}

		return resMap;
	}
	
	/**
	 * 팝업관리 저장/수정 Ajax.
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : isaac
	 * @param params
	 * @param req
	 * @param loginDto
	 */
	public Map<String, Object> insertUpdatePopup(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws LimeBizException {
		Map<String, Object> resMap = new HashMap<>();

		String r_puseq = Converter.toStr(params.get("r_puseq"));
		String userId = loginDto.getUserId();
		String userAuthority = loginDto.getAuthority();
		if(!StringUtils.equals("",r_puseq)) {
			int popupCnt = getPopupCntBySeq(r_puseq);
			if(popupCnt == 0) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);

			this.setBnPuFiles(params,req,"popup");

			params.put("m_pumoid", userId);
			popupDao.up(params);
		}else {
			this.setBnPuFiles(params,req,"popup");
			
			params.put("m_puinid", userId);
			params.put("m_puuseyn","Y");
			popupDao.in(params);
		}

		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		return resMap;
	}

	/**
	 * 팝업 삭제
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : isaac
	 * @return
	 */
	public Map<String, Object> deletePopup(Map<String, Object> params) {
		Map<String, Object> resMap = new HashMap<>();
		Map<String, Object> svcMap = new HashMap<>();

		String r_puseq = Converter.toStr(params.get("r_puseq"));

		if(!StringUtils.equals("", r_puseq)) {
			svcMap.put("r_puseq", r_puseq);
			popupDao.del(svcMap);
			resMap = MsgCode.getResultMap(MsgCode.SUCCESS);
		}else {
			return MsgCode.getResultMap(MsgCode.DATA_NOT_FOUND_ERROR);
		}

		return resMap;
	}
	
	/**
	 * 배너관리, 팝업관리 첨부파일체크 및 업로드 후 param에 파일명 및 확장자 저장
	 * @작성일 : 2020. 4. 02.
	 * @작성자 : isaac
	 */
	public void setBnPuFiles(Map<String, Object> params, HttpServletRequest req,String pageType) throws LimeBizException {
		//업로드 파일 처리
		List<Map<String, Object>> fList = new ArrayList<>();

		// 파일업로드 : 동일한 파일명으로 덮어쓰기.
		String sepa = System.getProperty("file.separator");
		String folderName = pageType;
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();

		//파일업로드
		try {
			if (!MultipartHttpServletRequest.class.isInstance(req)) {
				throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
			}

			MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;

			if (StringUtils.equals("banner",pageType)) {
				fList = fileUpload.execManyField(mtreq,"imageFiles",uploadDir,"m_bnimage","m_bnmobileimage");
			}else{
				fList = fileUpload.execOneField(mtreq,"imageFiles", uploadDir, "m_puimage");
			}

			logger.debug("fList : {}", fList);
		} catch (Exception e) {
			fileUpload.deleteList(fList, uploadDir);
			throw e;
		}

		if(!fList.isEmpty()){
			for(Map<String, Object> file : fList) {
				if(StringUtils.equals("m_bnimage", Converter.toStr(file.get("fieldName")))) {
					params.put("m_bnimage", Converter.toStr(file.get("saveFileName")));
					params.put("m_bnimagetype", Converter.toStr(file.get("mimeType")));
				}else if(StringUtils.equals("m_bnmobileimage", Converter.toStr(file.get("fieldName")))) {
					params.put("m_bnmobileimage", Converter.toStr(file.get("saveFileName")));
					params.put("m_bnmobileimagetype", Converter.toStr(file.get("mimeType")));
				}

				if(StringUtils.equals("m_puimage", Converter.toStr(file.get("fieldName")))) { //팝업이미지
					params.put("m_puimage", Converter.toStr(file.get("saveFileName")));
					params.put("m_puimagetype", Converter.toStr(file.get("mimeType")));
				}

			}
		}
	}

	/**
	 * 배너이미지,팝업이미지 다운로드
	 * @작성일 : 2020. 4. 2.
	 * @작성자 : isaac
	 * @param params
	 * @param req
	 * @param model
	 * @param pageType : 파일폴더  banner , popup
	 * @return
	 */
	public ModelAndView bnPuFileDown(Map<String, Object> params, HttpServletRequest req, Model model, String pageType ) {
		String sepa = System.getProperty("file.separator");
		String folderName = pageType;
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();

		String r_filename = HttpUtils.restoreXss(Converter.toStr(params.get("r_filename")));
		String r_filetype = Converter.toStr(params.get("r_filetype"));

		Map<String, Object> afMap = new HashMap<>();
		afMap.put("FOLDER_NAME", uploadDir);
		afMap.put("FILE_NAME", r_filename);
		afMap.put("FILE_TYPE", r_filetype);
		model.addAttribute("afMap", afMap);

		return new ModelAndView(new FileDown());
	}

	/**
	 * 페이징 관련 변수
	 * @작성일 : 2020. 4. 13.
	 * @작성자 : isaac
	 * @param params
	 * @param resMap
	 * @param req
	 */
	public void initPager(Map<String, Object> params, Map<String, Object> resMap ,HttpServletRequest req, int totalCnt){
		Pager pager = new Pager();
		pager.gridSetInfo(totalCnt, params, req);
		resMap.put("total", Converter.toInt(params.get("totpage")));
		resMap.put("listTotalCount", totalCnt);

		// Start. Define Only For Form-Paging.
		resMap.put("startnumber", params.get("startnumber"));
		resMap.put("r_page", params.get("r_page"));
		resMap.put("startpage", params.get("startpage"));
		resMap.put("endpage", params.get("endpage"));
		resMap.put("r_limitrow", params.get("r_limitrow"));
		// End.
		System.out.println("###############BoardSvc initPager  PARAMS#######################");
//        for( String key : params.keySet() ){
//            String value = String.valueOf( params.get(key)); 
//            System.out.println( String.format("키 : "+key+", 값 : "+value));
//        }
		
        
		System.out.println("###############BoardSvc initPager   RESMAP#######################");
//        for( String key : resMap.keySet() ){
//            String value = String.valueOf( resMap.get(key)); 
//            System.out.println( String.format("키 : "+key+", 값 : "+value));
//        }
	}
	
	
	public Map<String, Object> getChatFeedbackList(Map<String, Object> params, HttpServletRequest req){
		Map<String, Object> resMap = new HashMap<>();
		
		int totalCnt = this.getBoardCnt(params);
		this.initPager(params,resMap,req,totalCnt);
		
		String r_orderby = "";
		String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
		String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순

		r_orderby = sidx + " " + sord;		 
		if(StringUtils.equals("", sidx))  r_orderby = "BD_NOTICEYN DESC , BD_INDATE DESC ";  //BDA_NOTICHK 공지사항은 리스트 항시 상단에 출력
		params.put("r_orderby", r_orderby);
		
		List<BoardDto> list = this.getBoardList(params);
		resMap.put("list", list);		

		return resMap;
	}
}
