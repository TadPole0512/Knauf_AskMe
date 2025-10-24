package com.limenets.eorder.svc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.common.util.FileDown;
import com.limenets.common.util.FileUpload;
import com.limenets.common.util.Pager;
import com.limenets.eorder.dao.ConfigDao;
import com.limenets.eorder.dao.PlantDao;
import com.limenets.eorder.dao.PostalCodeDao;
import com.limenets.eorder.dao.UserDao;
import com.limenets.eorder.dao.FireproofDao;

/**
 * 시스템 환경설정 서비스.
 */
/**
 * @author hbkyu
 *
 */
@Service
public class ConfigSvc {
	private static final Logger logger = LoggerFactory.getLogger(ConfigSvc.class);
	
	@Resource(name="fileUpload") private FileUpload fileUpload;
	@Inject private ConfigDao configDao;
	@Inject private PlantDao plantDao;
	@Inject private PostalCodeDao postalDao;
	@Inject private FireproofDao fireDao;
	@Inject private UserDao userDao;
	
	/**
	 * 환경설정 리스트 가져오기
	 * @param params
	 */
	public Map<String, Object> getConfigList(Map<String,Object> params) {
		Map<String, Object> resMap = new HashMap<>();
		List<Map<String, Object>> configList = configDao.list( params );
		
		for( Map<String, Object> config : configList ){
			resMap.put( Converter.toStr( config.get("CF_ID") ), config.get("CF_VALUE") );
		}
		return resMap;
	}
	
	public Map<String, Object> getConfig(Map<String,Object> params) {
		Map<String, Object> svcMap = new HashMap<>();
		
		svcMap.put("r_cfid", Converter.toStr(params.get("r_cfid")));
		Map<String, Object> config = configDao.one(svcMap);
		
		return config;
	}
	
	/**
	 * 환경설정값 가져오기
	 * @param params
	 */
	public String getConfigValue(String cfid) {
		Map<String, Object> svcMap = new HashMap<>();
		
		svcMap.put("r_cfid", cfid);
		Map<String, Object> config = configDao.one(svcMap);
		String cfValue = Converter.toStr(config.get("CF_VALUE"));
		
		return cfValue;
	}
	
	
	/**
	 * 환경설정 수정
	 * @param params
	 * @return
	 */
	public Map<String, Object> updateSystemConfigTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> svcMap = new HashMap<>();
	
		// 파일업로드 : 동일한 파일명으로 덮어쓰기.
		String sepa = System.getProperty("file.separator");
		String folderName = "config";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
		List<Map<String, Object>> fList = new ArrayList<>();
		
		try {
			if (!MultipartHttpServletRequest.class.isInstance(req)) {
				throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
			}

			MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;
			fList = fileUpload.execManyFieldConfig(mtreq, "imageFiles", uploadDir, "m_systemlogo", "m_ceoseal", "m_mailbottomimg");
			logger.debug("fList : {}", fList);
		} catch (Exception e) {
			fileUpload.deleteList(fList, uploadDir);
			throw e;
		}
		
		for(Map<String, Object> file : fList) {
			logger.debug("fieldName : {}", Converter.toStr(file.get("fieldName")));
			
			if(StringUtils.equals("m_systemlogo", Converter.toStr(file.get("fieldName")))) {
				svcMap.put("r_cfid", "SYSTEMLOGO"); // 로고 이미지
				svcMap.put("m_cfvalue", Converter.toStr(file.get("saveFileName")));
				configDao.up(svcMap);
				
			}
			else if(StringUtils.equals("m_ceoseal", Converter.toStr(file.get("fieldName")))) {
				svcMap.put("r_cfid", "CEOSEAL"); // 대표자 직인 이미지
				svcMap.put("m_cfvalue", Converter.toStr(file.get("saveFileName")));
				configDao.up(svcMap);
				
			}
			else if(StringUtils.equals("m_mailbottomimg", Converter.toStr(file.get("fieldName")))) {
                svcMap.put("r_cfid", "MAILBOTTOMIMG"); // 대표자 직인 이미지
                svcMap.put("m_cfvalue", Converter.toStr(file.get("saveFileName")));
                configDao.up(svcMap);
                
            }
		}
		svcMap.clear();
		
		// 메일 타이틀 변경.
        svcMap.put("r_cfid", "MAILTITLE");
        svcMap.put("m_cfvalue", Converter.toStr(params.get("m_mailtitle")));
        configDao.up(svcMap);
        svcMap.clear();
        
		// 브라우저타이틀 변경.
		svcMap.put("r_cfid", "BROWSERTITLE");
		svcMap.put("m_cfvalue", Converter.toStr(params.get("m_browsertitle")));
		configDao.up(svcMap);
		svcMap.clear();
		
		// 시스템관리자(비번,이름수정)
		svcMap.put("r_userid", Converter.toStr(params.get("r_systemadmin")));
		svcMap.put("m_userpwd", Converter.toStr(params.get("m_userpwd")));
		svcMap.put("m_usernm", Converter.toStr(params.get("m_usernm")));
		userDao.up(svcMap);
		svcMap.clear();
		
		// 내부사용자 [?]개월 단위 비밀번호 변경 여부
		svcMap.put("r_cfid", "USERPSWDMONTHADMINUSE");
		svcMap.put("m_cfvalue", Converter.toStr(params.get("m_userpswdmonthadminuse")));
		configDao.up(svcMap);
		svcMap.clear();
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 출고지 리스트 가져오기.
	 * @작성일 : 2020. 3. 7.
	 * @작성자 : kkyu
	 */
	public List<Map<String, Object>> getPlantList(String order_by, int start_row, int end_row){
		Map<String, Object> svcMap = new HashMap<String, Object>();
		svcMap.put("r_orderby", (!StringUtils.equals("", order_by)) ? order_by : "PT_SORT ASC, PT_CODE ASC ");
		svcMap.put("r_startrow", start_row);
		svcMap.put("r_endrow", end_row);
		return this.getPlantList(svcMap);
	}
	public List<Map<String, Object>> getPlantList(Map<String, Object> svcMap){
		return plantDao.list(svcMap);
	}
	
	/**
	 * 출고지 리스트 가져오기.
	 * @작성일 : 2020. 3. 7.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> getPlantList(Map<String, Object> params, HttpServletRequest req){
		Map<String, Object> resMap = new HashMap<>();
		
		int totalCnt = plantDao.cnt(params);

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
		
		String r_orderby = "";
		String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
		String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
		r_orderby = sidx + " " + sord;
		if(StringUtils.equals("", sidx)) { r_orderby = "PT_SORT ASC, PT_CODE ASC "; } //디폴트 지정
		params.put("r_orderby", r_orderby);
		
		// 엑셀 다운로드.
		String where = Converter.toStr(params.get("where"));
		if ("excel".equals(where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}
		
		List<Map<String, Object>> list = this.getPlantList(params);
		resMap.put("list", list);
		
		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		
		return resMap;
	}
	
	/**
	 * 우편번호 리스트 가져오기.
	 * @작성일 : 2023. 9. 12.
	 * @작성자 : Squall. Koh
	 */
	public Map<String, Object> getPostalList(Map<String, Object> params, HttpServletRequest req){
		Map<String, Object> resMap = new HashMap<>();
		
		String r_useflagarr = Converter.toStr(params.get("r_useflag")); // 검색조건
		if(!StringUtils.equals("", r_useflagarr)) params.put("r_useflags", r_useflagarr.split(",", -1));
		
		int totalCnt = postalDao.cnt(params);

		Pager pager = new Pager();
		params.put("rows", 10);
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
		
		String r_orderby = "";
		String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
		String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
		r_orderby = sidx + " " + sord;
		if(StringUtils.equals("", sidx)) { r_orderby = "ZIP_CD ASC "; } //디폴트 지정
		params.put("r_orderby", r_orderby);
		
		// 엑셀 다운로드.
		String where = Converter.toStr(params.get("where"));
		if ("excel".equals(where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}
		
		
		
		
		List<Map<String, Object>> list = this.getPostalCodeList(params);
		resMap.put("list", list);
		
		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		
		resMap.put("startnumber", 1);
		System.out.println("startnumber>>>" + resMap.get("startnumber"));
		System.out.println("r_page>>>" + resMap.get("r_page"));
		System.out.println("startpage>>>" + resMap.get("startpage"));
		System.out.println("endpage>>>" + resMap.get("endpage"));
		System.out.println("r_limitrow>>>" + resMap.get("r_limitrow"));
		return resMap;
	}
	
	public List<Map<String, Object>> getPostalCodeList(Map<String, Object> svcMap){
		return postalDao.list(svcMap);
	}
	
	/**
	 * 우편번호 저장/수정.
	 * @작성일 : 2023. 9. 12.
	 * @작성자 : Squall. Koh
	 * @param r_processtype : ADD=저장 / EDIT=수정
	 * @param r_ptcode
	 */
	public Map<String, Object> insertUpdatePostalCodeTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws LimeBizException {
		Map<String, Object> svcMap = new HashMap<>();
		
		System.out.println(loginDto.getUserId());
	
		// 파라미터 정의.
		String [] r_processtypearr = req.getParameterValues("r_proctype");
		String [] r_zipcd = req.getParameterValues("r_zipcd");
		
		String [] r_addr = req.getParameterValues("r_addr");
		String [] r_ref = req.getParameterValues("r_ref");
		String [] r_usef = req.getParameterValues("r_usef");

		// 저장/수정 처리.
		for(int i=0,j=r_processtypearr.length; i<j; i++){
			String r_processtype = Converter.toStr(r_processtypearr[i]);
			
			svcMap.put("r_zipcd", Converter.toStr(r_zipcd[i]));
			svcMap.put("r_addr", Converter.toStr(r_addr[i]));
			svcMap.put("r_ref", Converter.toStr(r_ref[i]));
			svcMap.put("r_usef", Converter.toStr(r_usef[i]));
			svcMap.put("r_userid", loginDto.getUserId());
			
			// 저장
			if("ADD".equals(r_processtype)) {
				postalDao.in(svcMap);
			}
			// 수정
			else {
				postalDao.up(svcMap);
			}
			svcMap.clear();
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 내화구조 리스트 가져오기.
	 * @작성일 : 2021. 4. 5
	 * @작성자 : jihye lee
	 */
	public List<Map<String, Object>> getFireproofList(String order_by, int start_row, int end_row){
		Map<String, Object> svcMap = new HashMap<String, Object>();
		svcMap.put("r_orderby", (!StringUtils.equals("", order_by)) ? order_by : "displayOrder ASC ");
		svcMap.put("r_startrow", start_row);
		svcMap.put("r_endrow", end_row);
		return this.getPlantList(svcMap);
	}
	public List<Map<String, Object>> getFireproofList(Map<String, Object> svcMap){
		return fireDao.list(svcMap);
	}
	
	
	/**
	 * 내화구조 리스트 가져오기.
	 * @작성일 : 2021. 4. 5
	 * @작성자 : jihye lee
	 */
	public Map<String, Object> getFireproofList(Map<String, Object> params, HttpServletRequest req){
		Map<String, Object> resMap = new HashMap<>();
		
		int totalCnt = fireDao.cnt(params);

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
		
		String r_orderby = "";
		String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
		String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
		r_orderby = sidx + " " + sord;
		if(StringUtils.equals("", sidx)) { r_orderby = " FIRETIME,DISPLAYORDER ASC "; } //디폴트 지정
		params.put("r_orderby", r_orderby);
		
		// 엑셀 다운로드.
		String where = Converter.toStr(params.get("where"));
		if ("excel".equals(where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}
		
		List<Map<String, Object>> list = this.getFireproofList(params);
		resMap.put("list", list);
		resMap.put("page", params.get("r_page"));
		
		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		
		return resMap;
	}
	
	/**
	 * 내화구조 저장/수정.
	 * @작성일 : 2021. 4. 5 -> 2021. 7. 15
	 * @작성자 : jihye lee -> jsh
	 * @param r_processtype : ADD=저장 / EDIT=수정 / DEL=삭제
	 * @param r_ptcode
	 */
	public Map<String, Object> insertUpdateFireproofTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws LimeBizException {
		Map<String, Object> svcMap = new HashMap<>();
	
		// 파라미터 정의.
		String [] r_processtypearr = req.getParameterValues("r_processtype");
		String [] r_ptcodearr = req.getParameterValues("r_keycode");
		
		String [] m_active = req.getParameterValues("m_active");
		String [] m_displayorder = req.getParameterValues("m_displayorder");
		String [] m_fireprooftype = req.getParameterValues("m_fireprooftype");
		String [] m_desc1 = req.getParameterValues("m_desc1");
		String [] m_desc2 = req.getParameterValues("m_desc2");
		String [] m_firetime = req.getParameterValues("m_firetime");
		//String [] m_filepath = req.getParameterValues("m_filepath");
		String [] m_createtime = req.getParameterValues("m_createtime");
				

		// 저장/수정 처리.
		for(int i=0,j=r_ptcodearr.length; i<j; i++){
			String r_processtype = Converter.toStr(r_processtypearr[i]);
			String r_ptcode = Converter.toStr(r_ptcodearr[i]);
			
			svcMap.put("m_active", Converter.toStr(m_active[i]));
			svcMap.put("m_displayorder", Converter.toStr(m_displayorder[i]));
			svcMap.put("m_fireprooftype", Converter.toStr(m_fireprooftype[i]));
			svcMap.put("m_desc1", Converter.toStr(m_desc1[i]));
			svcMap.put("m_desc2", Converter.toStr(m_desc2[i]));
			svcMap.put("m_firetime", Converter.toStr(m_firetime[i]));
			//svcMap.put("m_ptaddr2", Converter.toStr(m_ptaddr2arr[i]));
			svcMap.put("m_createtime", Converter.toStr(m_createtime[i]));
			
			// 저장
			if("ADD".equals(r_processtype)) {
				svcMap.put("m_keycode", r_ptcode);
				svcMap.put("m_inid", loginDto.getUserId());
				fireDao.in(svcMap);
			}
			// 삭제
            else if("DEL".equals(r_processtype)){
                svcMap.put("m_keycode", r_ptcode);
                svcMap.put("m_upid", loginDto.getUserId());
                fireDao.del(svcMap);
            }
			// 수정
			else {
				svcMap.put("m_keycode", r_ptcode);
				svcMap.put("m_upid", loginDto.getUserId());
				fireDao.up(svcMap);
			}
			svcMap.clear();
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	
	/**
	 * 품목 수정.
	 * @작성일 : 2020. 3. 18.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> updateFireproofTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		// 필수 파라미터 체크.
		String r_keycode = Converter.toStr(params.get("r_itemcd"));
		if(StringUtils.equals("", r_keycode)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		
		// 품목 데이터 유무 체크.
		Map<String, Object> item = this.getFireProofOne(r_keycode);
		if (CollectionUtils.isEmpty(item)) throw new LimeBizException(MsgCode.DATA_NOT_FIND_ERROR, "품목");

		String sepa = System.getProperty("file.separator");
		String folderName = "fireproof";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append("data").append(sepa).append(folderName).toString();
		
		//# 0. ItemInfo Update인 경우 파일삭제 여부 체크 후 삭제.
		logger.debug("내화구조타입 수정여부 > KEYCODE가 빈값이 아닌경우 수정 : {}", Converter.toStr(item.get("KEYCODE")));
		boolean fileDeleteTf = false;
		if(!StringUtils.equals("", Converter.toStr(item.get("KEYCODE")))) {
			if(StringUtils.equals("Y", Converter.toStr(params.get("m_itifile1_delyn")))) {
				fileDeleteTf = true;
				params.put("r_delfile1", "Y");
			}
			
			if(fileDeleteTf) {
				params.put("r_keycode", r_keycode);
				fireDao.upInfoForFile(params);
			}
		}
		
		//# 1. Insert or Update ItemInfo.
		String m_itifile1 = "";
		String m_itifile1type = "";
		
		// 파일 저장.
		List<Map<String, Object>> fList = new ArrayList<>();
		try {
			if (!MultipartHttpServletRequest.class.isInstance(req)) {
				throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
			}
			MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest) req;
			fList = fileUpload.execManyField(mtreq, "imageFiles", uploadDir, "m_itifile1");
			logger.debug("fList : {}", fList);
			
			for(Map<String, Object> file : fList) {
				if(StringUtils.equals("m_itifile1", Converter.toStr(file.get("fieldName")))) {
					m_itifile1 = Converter.toStr(fList.get(0).get("saveFileName"));
					m_itifile1type = Converter.toStr(fList.get(0).get("mimeType"));
				}
			}
			
		} catch (Exception e) {
			fileUpload.deleteList(fList, uploadDir);
			throw e;
		}
		
		String r_edituserid = loginDto.getUserId();
		
		params.put("m_keycode", r_keycode);
		params.put("m_upid", r_edituserid);
		
		params.put("m_fileName", m_itifile1);
		params.put("m_fileType", m_itifile1type);
		fireDao.upForFile(params);
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 출고지 저장/수정.
	 * @작성일 : 2020. 3. 8.
	 * @작성자 : kkyu
	 * @param r_processtype : ADD=저장 / EDIT=수정
	 * @param r_ptcode
	 */
	public Map<String, Object> insertUpdatePlantTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws LimeBizException {
		Map<String, Object> svcMap = new HashMap<>();
	
		// 파라미터 정의.
		String [] r_processtypearr = req.getParameterValues("r_processtype");
		String [] r_ptcodearr = req.getParameterValues("r_ptcode");
		
		String [] m_ptusearr = req.getParameterValues("m_ptuse");
		String [] m_ptsortarr = req.getParameterValues("m_ptsort");
		String [] m_ptnamearr = req.getParameterValues("m_ptname");
		String [] m_ptzonecodearr = req.getParameterValues("m_ptzonecode");
		String [] m_ptzipcodearr = req.getParameterValues("m_ptzipcode");
		String [] m_ptaddr1arr = req.getParameterValues("m_ptaddr1");
		String [] m_ptaddr2arr = req.getParameterValues("m_ptaddr2");
		String [] m_pttelarr = req.getParameterValues("m_pttel");

		// 저장/수정 처리.
		for(int i=0,j=r_ptcodearr.length; i<j; i++){
			String r_processtype = Converter.toStr(r_processtypearr[i]);
			String r_ptcode = Converter.toStr(r_ptcodearr[i]);
			
			svcMap.put("m_ptuse", Converter.toStr(m_ptusearr[i]));
			svcMap.put("m_ptsort", Converter.toStr(m_ptsortarr[i]));
			svcMap.put("m_ptname", Converter.toStr(m_ptnamearr[i]));
			svcMap.put("m_ptzonecode", Converter.toStr(m_ptzonecodearr[i]));
			svcMap.put("m_ptzipcode", Converter.toStr(m_ptzipcodearr[i]));
			svcMap.put("m_ptaddr1", Converter.toStr(m_ptaddr1arr[i]));
			svcMap.put("m_ptaddr2", Converter.toStr(m_ptaddr2arr[i]));
			svcMap.put("m_pttel", Converter.toStr(m_pttelarr[i]));
			
			// 저장
			if("ADD".equals(r_processtype)) {
				svcMap.put("m_ptcode", r_ptcode);
				svcMap.put("m_ptinid", loginDto.getUserId());
				plantDao.in(svcMap);
			}
			// 수정
			else {
				svcMap.put("r_ptcode", r_ptcode);
				svcMap.put("m_ptmoid", loginDto.getUserId());
				plantDao.up(svcMap);
			}
			svcMap.clear();
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 파일다운로드
	 * @param model
	 * @param req
	 * @return 
	 * @throws LimeBizException 
	 */
	public ModelAndView fileDown(Map<String, Object> params, HttpServletRequest req, Model model, LoginDto loginDto) throws LimeBizException {
		String sepa = System.getProperty("file.separator");
		String folderName = "config";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
		
		String r_cfid = Converter.toStr(params.get("r_cfid"));
		if(StringUtils.equals("", r_cfid)){
			throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR, "고유아이디");
		}
		
		params.put("r_cfid", r_cfid);
		Map<String, Object> config = getConfig(params);
		
		Map<String, Object> afMap = new HashMap<>();
		afMap.put("FOLDER_NAME", uploadDir);
		afMap.put("FILE_NAME", Converter.toStr(config.get("CF_VALUE")));
		model.addAttribute("afMap", afMap);
		
		return new ModelAndView(new FileDown());
	}
	
	
	/**
	 * Get O_ITEM One.
	 * @작성일 : 2021. 4. 5.
	 * @작성자 : jihye lee
	 */
	public Map<String, Object> getFireProofOne(String r_keycode){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_keycode", r_keycode);
		return this.getFireProofOne(svcMap);
	}
	
	public Map<String, Object> getFireProofOne(Map<String, Object> svcMap){
		return fireDao.one(svcMap);
	}
	
	/**
     * Get 내화구조 코드 중복체크
     * @작성일 : 2021. 7. 15.
     * @작성자 : jsh
     */
	public int getFireProofCheck(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto){
	    Map<String, Object> svcMap = new HashMap<>();
	    
        // 파라미터 정의.
        String [] r_ptcodearr = req.getParameterValues("r_keycode");
                
        int result = 0;
        
        // 기존에 이미 등록된 코드인지 체크
        for(int i=0,j=r_ptcodearr.length; i<j; i++){
            String r_ptcode = Converter.toStr(r_ptcodearr[i]);
            svcMap.put("m_keycode", r_ptcode);
            
            result += fireDao.getFireProofCheck(svcMap);
            svcMap.clear();
        }
        return result;
    }

	
}
