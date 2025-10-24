package com.limenets.eorder.svc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.AppPushUtil;
import com.limenets.common.util.Converter;
import com.limenets.common.util.FileDown;
import com.limenets.common.util.FileUpload;
import com.limenets.common.util.HttpUtils;
import com.limenets.common.util.Pager;
import com.limenets.eorder.dao.AppPushDao;
import com.limenets.eorder.dao.PromotionDao;
import com.limenets.eorder.dao.PromotionItemDao;
import com.limenets.eorder.dao.UserDao;

/**
 * 게시판 서비스
 */
@Service
public class PromotionSvc {
	private static final Logger logger = LoggerFactory.getLogger(PromotionSvc.class);
	
	@Value("${https.url}") private String httpsUrl;
	
	@Resource(name="fileUpload") private FileUpload fileUpload;
	
	@Inject private ConfigSvc configSvc;
	
	@Inject private AppPushDao appPushDao;
	@Inject private UserDao userDao;
	@Inject private PromotionDao promotionDao;
	@Inject private PromotionItemDao promotionItemDao;

	/**
	 * 이벤트(PROMOTION) 저장/수정 Ajax.
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 */
	public Map<String, Object> insertUpdatePromotionTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws LimeBizException {
									
		String r_prmseq = Converter.toStr(params.get("r_prmseq"));
		String m_userid = loginDto.getUserId();

		String process_type = Converter.toStr(params.get("r_processtype"));

		if(StringUtils.equals("EDIT", process_type)) {
			if(StringUtils.equals("", r_prmseq)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);

			this.setPromotionFiles(params, req);

			params.put("nowSeq", r_prmseq);
			params.put("m_prmmoid", m_userid);

			promotionDao.up(params);

			this.deletePromotionItem(r_prmseq);

		}else {			
			this.setPromotionFiles(params, req);

			params.put("m_prminid", m_userid);

			String m_prmcode = promotionDao.maxCode(params);

			params.put("m_prmcode", m_prmcode);
			promotionDao.in(params);
		}

		//품목이벤트 품목코드 저장
		if(StringUtils.equals("2",Converter.toStr(params.get("m_prmtype")))){
			this.insertPromotionItem(params,req,m_userid);
		}
		
		// APP PUSH 발송. => 최초 등록시 에만
		if(!StringUtils.equals("EDIT", process_type)) {
			Map<String, Object> svcMap = new HashMap<>();
			
			// 푸시 발송 대상자 리스트 가져오기.
			svcMap.put("r_userapppushyn3", "Y"); // 이벤트 푸시 수신여부=Y인 사용자들만.
			List<Map<String, Object>> userList = userDao.listForAppPush(svcMap);
			
			if(!userList.isEmpty()) {
				List<String> ri_userid = new ArrayList<>(); // 푸시 보낼 사용지 아이디 리스트.
				List<String> pushKeyList = new ArrayList<>(); // 푸시 보낼 사용자의 푸시키 리스트.
				
				for(Map<String, Object> user : userList) {
					String userid = Converter.toStr(user.get("USERID"));
					String user_apppushkey = Converter.toStr(user.get("USER_APPPUSHKEY"));
					// PUSH_KEY가 있는 회원들만 저장 후 발송.
					if(!StringUtils.equals("", user_apppushkey)) {
						ri_userid.add(userid);
						pushKeyList.add(user_apppushkey);
					}
				}
				
				// 푸시 설정.
				String title = "USG BORAL e-Ordering";
				String contents = "";
				String move_url = httpsUrl + req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath();
				move_url += "/front/promotion/promotionList.lime";
				
				// 내용 가지고 오기.
				contents = configSvc.getConfigValue("PUSH3");
				contents = contents.replace("#MSG1#", Converter.toStr(params.get("m_prmtitle")));
				logger.debug("이벤트 푸시 내용 : {}", contents);
			
				if(!ri_userid.isEmpty()) {
					// 푸시 테이블 저장.
					svcMap.put("ri_userid", ri_userid);
					svcMap.put("m_aptitle", title);
					svcMap.put("m_apcontent", contents);
					svcMap.put("m_apsendyn", "Y"); // 푸시 발송여부 Y/N
					svcMap.put("m_apsendtype", "1"); // 1=바로 전송, 2=스케쥴러 전송
					svcMap.put("m_apmoveurl", move_url); // 푸시 클릭시 이동할 URL
					svcMap.put("m_aptype", "3"); // 1=주문확정,2=배차완료,3=이벤트최초등록
					
					appPushDao.inByUser(svcMap);
					svcMap.clear();
					
					// 푸시 발송.
					AppPushUtil appPush = new AppPushUtil();
					appPush.sendAppPush(title, contents, pushKeyList, move_url);
				}
			}
		}

		return MsgCode.getResultMap(MsgCode.SUCCESS);		
	}

	/**
	 * 품목이벤트 품목코드 저장
	 * @작성일 : 2020. 4. 9.
	 * @작성자 : isaac
	 * @param params
	 */
	private void insertPromotionItem(Map<String, Object> params, HttpServletRequest req, String user_id){
		//품목 이벤트 처리
		String m_prmtype = Converter.toStr(params.get("m_prmtype"));
		String m_prmiprmseq = Converter.toStr(params.get("nowSeq"));

		if(StringUtils.equals("2",m_prmtype)){

			String[] itemcdArr = req.getParameterValues("m_itemcd");

			if(!ArrayUtils.isEmpty(itemcdArr)) {
				int itemcdLen = itemcdArr.length;
				for(int i=0,j=itemcdArr.length; i<j; i++) {
					params.put("m_prmiprmseq", m_prmiprmseq);
					params.put("m_prmiitemcd", itemcdArr[i]);
					params.put("m_prmiinid", user_id);
					promotionItemDao.in(params);
				}
			}
		}
	}

	/**
	 * 품목이벤트 품목코드 삭제
	 * @작성일 : 2020. 4. 9.
	 * @작성자 : isaac
	 * @param prmi_prmseq
	 * @throws LimeBizException
	 */
	private void deletePromotionItem(String prmi_prmseq) throws LimeBizException {
		String r_prmiprmseq = prmi_prmseq;
		Map<String, Object> svcMap = new HashMap<>();
		if(StringUtils.equals("",r_prmiprmseq)){
			throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		}else {

			svcMap.put("r_prmiprmseq", r_prmiprmseq);
			promotionItemDao.del(svcMap);
		}

	}

	/**
	 * 이벤트(PROMOTION)  첨부파일체크 및 업로드 후 param에 파일명 및 확장자 저장
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 *
	 */
	public void setPromotionFiles(Map<String, Object> params, HttpServletRequest req) throws LimeBizException {
		//업로드 파일 처리
		List<Map<String, Object>> fList = new ArrayList<>();

		// 파일업로드 : 동일한 파일명으로 덮어쓰기.
		String sepa = System.getProperty("file.separator");
		String folderName = "promotion";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
		List<Map<String, Object>> retFileList = new ArrayList<>();

		//파일업로드
		try {
			if (!MultipartHttpServletRequest.class.isInstance(req)) {
				throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
			}
			MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;

			//m_prmimage1 : 메인이미지 , m_prmimage2 : 리스트이미지
			retFileList = fileUpload.execManyField(mtreq, "imageFiles", uploadDir, "m_prmimage1","m_prmimage2");
			fList.addAll(retFileList);

			logger.debug("fList : {}", fList);
		} catch (Exception e) {
			fileUpload.deleteList(fList, uploadDir);
			throw e;
		}

		if(!fList.isEmpty()){

			for(Map<String, Object> file : fList) {
				if(StringUtils.equals("m_prmimage1", Converter.toStr(file.get("fieldName")))) {
					params.put("m_prmimage1", Converter.toStr(file.get("saveFileName")));
					params.put("m_prmimage1type", Converter.toStr(file.get("mimeType")));
				}else if(StringUtils.equals("m_prmimage2", Converter.toStr(file.get("fieldName")))) {
					params.put("m_prmimage2", Converter.toStr(file.get("saveFileName")));
					params.put("m_prmimage2type", Converter.toStr(file.get("mimeType")));
				}
			}

		}
	}

	/**
	 * 이벤트(PROMOTION) 리스트 가져오기
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 * r_prmongoing  Y : 진행중 N : 진행완료
	 */
	public Map<String, Object> getPromotionListForFront(Map<String, Object> params, HttpServletRequest req, String r_prmongoing){
		Map<String, Object> resMap = new HashMap<>();
		String resListName = (StringUtils.equals("Y",r_prmongoing)) ? "currentList" : "pastList" ;
		String resCntName = (StringUtils.equals("Y",r_prmongoing)) ? "currentCnt" : "pastCnt" ;

		params.put("r_prmongoing", r_prmongoing);
		int totalCnt = this.getPromotionCnt(params);

		params.put("r_startrow", "1");
		params.put("r_endrow", "8");
		this.initPager(params,resMap,req,totalCnt);
		params.put("_orderby", "PRM_SDATE DESC");

		List<Map<String ,Object>> list = promotionDao.listForFront(params);

		resMap.put(resCntName, totalCnt);
		resMap.put(resListName, list);

		return resMap;
	}

	/**
	 * 이벤트(PROMOTION) 리스트 가져오기
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 */
	public Map<String, Object> getPromotionListAjax(Map<String, Object> params, HttpServletRequest req){
		Map<String, Object> resMap = new HashMap<>();

		//검색조건 진행여부(체크박스) 처리
		String[] r_prmongoingArr = Converter.toStr(params.get("r_prmongoingArr")).split(",");
		params.put("r_prmongoing",this.getPromotionTermCondition(r_prmongoingArr));

		int totalCnt = this.getPromotionCnt(params);

		this.initPager(params,resMap,req,totalCnt);

		String r_orderby = "";
		String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
		String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
		r_orderby = sidx + " " + sord;
		if(StringUtils.equals("", sidx))  r_orderby = "PRM_INDATE DESC ";
		params.put("r_orderby", r_orderby);

		// 엑셀 다운로드.
		String where = Converter.toStr(params.get("where"));
		if ("excel".equals(where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}

		resMap.put("listTotalCount", totalCnt);
		List<Map<String ,Object>> list = this.getPromotionList(params);
		resMap.put("list", list);

		return resMap;
	}

	/**
	 * 진행여부 기간조회 조건 가져옴
	 * @작성일 : 2020. 4. 10.
	 * @작성자 : isaac
	 * W : 진행예정, Y:진행중, N:진행완료
	 * @param r_prmongoingArr
	 * @return
	 */
	private String getPromotionTermCondition(String[] r_prmongoingArr){
		String retStr = "";
		int r_prmongoingArrLen = r_prmongoingArr.length;
		for (int i=0; i<r_prmongoingArrLen; i++){
			retStr += r_prmongoingArr[i];
		}
		if(StringUtils.equals("WYN",retStr)) retStr = null;
		return retStr;
	}

	public List<Map<String ,Object>> getPromotionList(Map<String, Object> params){
		return promotionDao.list(params);
	}

	public Map<String ,Object> getPromotionOne(Map<String, Object> params){
		return promotionDao.one(params);
	}

	public Map<String ,Object> getPromotionOneBySeq(String prm_seq){
		Map<String, Object> svcMap = new HashMap<>();

		svcMap.put("r_prmseq", prm_seq);
		return this.getPromotionOne(svcMap);
	}

	public int getPromotionCnt(Map<String, Object> params) {
		return promotionDao.cnt(params);
	}

	/**
	 * 이벤트(PROMOTION) 시퀀스 번호로 개수 조회
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 * @param prm_seq
	 * @return
	 */
	public int getPromotionCntBySeq(String prm_seq) {
		Map<String, Object> svcMap = new HashMap<>();

		svcMap.put("r_prmseq", prm_seq);
		return this.getPromotionCnt(svcMap);
	}

	/**
	 * 이벤트(PROMOTION) 삭제
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 * @return
	 */
	public Map<String, Object> deletePromotion(Map<String, Object> params) {
		Map<String, Object> resMap = new HashMap<>();
		Map<String, Object> svcMap = new HashMap<>();

		String r_prmseq = Converter.toStr(params.get("r_prmseq"));

		if(!StringUtils.equals("", r_prmseq)) {
			svcMap.put("r_prmseq", r_prmseq);
			promotionDao.del(svcMap);
			resMap = MsgCode.getResultMap(MsgCode.SUCCESS);
		}else {
			return MsgCode.getResultMap(MsgCode.DATA_NOT_FOUND_ERROR);
		}

		return resMap;
	}

	/**
	 * 이벤트(PROMOTION) 이미지 다운로드
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : isaac
	 * @param params m_prmimage1(메인이미지) , m_prmimage2(리스트이미지)
	 * @param req
	 * @param model
	 * @return
	 */
	public ModelAndView promotionFileDown(Map<String, Object> params, HttpServletRequest req, Model model) {
		String sepa = System.getProperty("file.separator");
		String folderName=  "promotion";
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
	 * 이벤트품목(PROMOTIONITEM) 리스트 가져오기
	 * @작성일 : 2020. 4. 10.
	 * @작성자 : isaac
	 */
	public Map<String, Object> getPromotionItemListAjax(Map<String, Object> params, HttpServletRequest req){
		Map<String, Object> resMap = new HashMap<>();

		int totalCnt = this.getPromotionItemCnt(params);

		this.initPager(params,resMap,req,totalCnt);

		//sort 사용 X
		/*String r_orderby = "";
		String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
		String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
		r_orderby = sidx + " " + sord;
		if(StringUtils.equals("", sidx))  r_orderby = "PRM_INDATE DESC ";  //BDA_NOTICHK 공지사항은 리스트 항시 상단에 출력
		params.put("r_orderby", r_orderby);*/

		resMap.put("listTotalCount", totalCnt);
		List<Map<String ,Object>> list = this.getPromotionItemList(params);
		resMap.put("list", list);

		return resMap;
	}

	/**
	 * 이벤트품목(PROMOTIONITEM) 리스트 가져오기
	 * @작성일 : 2020. 4. 10.
	 * @작성자 : isaac
	 */
	public Map<String, Object> getPromotionItemListForPop(Map<String, Object> params, HttpServletRequest req){
		Map<String, Object> resMap = new HashMap<>();
		params.put("r_prmiprmseq",params.get("r_prmseq"));
//		params.put("r_limitrow","4");
		int totalCnt = this.getPromotionItemCnt(params);

		this.initPager(params,resMap,req,totalCnt);

		resMap.put("listTotalCount", totalCnt);
		List<Map<String ,Object>> list = this.getPromotionItemList(params);
		resMap.put("promotionItemList", list);

		return resMap;
	}

	/**
	 * 이벤트품목(PROMOTIONITEM) 리스트 가져오기
	 * @작성일 : 2020. 4. 16.
	 * @작성자 : isaac
	 */
	public List<Map<String ,Object>> getPromotionItemListForFront(Map<String, Object> params){
		String r_prmiprmseq = Converter.toStr(params.get("r_prmseq"));

		params.put("r_prmiprmseq", r_prmiprmseq);
		return promotionItemDao.list(params);
	}

	public List<Map<String ,Object>> getPromotionItemList(Map<String, Object> params){
		return promotionItemDao.list(params);
	}

	public int getPromotionItemCnt(Map<String, Object> params) {
		return promotionItemDao.cnt(params);
	}

	/**
	 * 페이징 관련 변수
	 * count는 호출한 메서드에서 resMap put
	 * @작성일 : 2020. 4. 15.
	 * @작성자 : isaac
	 * @param params
	 * @param resMap
	 * @param req
	 */
	public void initPager(Map<String, Object> params, Map<String, Object> resMap ,HttpServletRequest req, int totalCnt){
		Pager pager = new Pager();
		pager.gridSetInfo(totalCnt, params, req);
		resMap.put("total", Converter.toInt(params.get("totpage")));

		// Start. Define Only For Form-Paging.
		resMap.put("startnumber", params.get("startnumber"));
		resMap.put("r_page", params.get("r_page"));
		resMap.put("startpage", params.get("startpage"));
		resMap.put("endpage", params.get("endpage"));
		resMap.put("r_limitrow", params.get("r_limitrow"));
		// End.
	}
}
