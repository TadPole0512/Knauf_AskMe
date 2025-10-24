package com.limenets.eorder.svc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.common.util.FileDown;
import com.limenets.common.util.FileUpload;
import com.limenets.common.util.FileUtil;
import com.limenets.common.util.HttpUtils;
import com.limenets.common.util.Pager;
import com.limenets.eorder.dao.CommonDao;
import com.limenets.eorder.dao.ItemDao;

/**
 * 품목 서비스.
 */
@Service
public class ItemSvc {
	private static final Logger logger = LoggerFactory.getLogger(ItemSvc.class);
	
	@Resource(name="fileUpload") private FileUpload fileUpload;
	
	@Inject private CommonDao commonDao;
	@Inject private ItemDao itemDao;
	
	/**
	 * 인자값 품목코드가 배송비 품목이면 true, 아니면 false.
	 * @작성일 : 2020. 4. 26.
	 * @작성자 : kkyu
	 */
	public boolean deliveryItemTf(String item_cd) {
		/*if(StringUtils.equals("W1", item_cd) || StringUtils.equals("R1", item_cd) || StringUtils.equals("P1", item_cd) 
			|| StringUtils.equals("F3", item_cd) || StringUtils.equals("F2", item_cd) || StringUtils.equals("F1", item_cd)) {
			return true;
		}*/ 
		if(StringUtils.equals("177306", item_cd) || StringUtils.equals("254636", item_cd) 
			|| StringUtils.equals("63111", item_cd) || StringUtils.equals("65440", item_cd) ) {
			return true;
		}
		return false;
	}
	
	/**
	 * 카테고리 리스트 가져오기.
	 * @작성일 : 2020. 3. 16.
	 * @작성자 : kkyu
	 * @param r_ctlevel : 가져오려는 카테고리 단계.
	 */
	public List<Map<String, Object>> getCategoryList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		int r_ctlevel = Converter.toInt(params.get("r_ctlevel"));
		String r_salescd1nm = Converter.toStr(params.get("r_salescd1nm"));
		String r_salescd2nm = Converter.toStr(params.get("r_salescd2nm"));
		String r_salescd3nm = Converter.toStr(params.get("r_salescd3nm"));
		//String r_salescd4nm = Converter.toStr(params.get("r_salescd4nm"));
		
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> svcMap = new HashMap<>();
				
		svcMap.put("r_ctlevel", r_ctlevel);
		if(1 == r_ctlevel) {
			svcMap.put("r_groupby", "SALES_CD1_NM");
			//svcMap.put("r_orderby", "SALES_CD1_NM ASC ");
		}
		else if(2 == r_ctlevel) {
			if(StringUtils.equals("", r_salescd1nm)) return list;
			
			svcMap.put("r_salescd1nm", r_salescd1nm);
			svcMap.put("r_groupby", "SALES_CD2_NM");
			//svcMap.put("r_orderby", "SALES_CD2_NM ASC ");
		}
		else if(3 == r_ctlevel) {
			if(StringUtils.equals("", r_salescd1nm) || StringUtils.equals("", r_salescd2nm)) return list;
			
			svcMap.put("r_salescd1nm", r_salescd1nm);
			svcMap.put("r_salescd2nm", r_salescd2nm);
			svcMap.put("r_groupby", "SALES_CD3_NM");
			//svcMap.put("r_orderby", "SALES_CD3_NM ASC ");
		}
		else if(4 == r_ctlevel) {
			if(StringUtils.equals("", r_salescd1nm) || StringUtils.equals("", r_salescd2nm) || StringUtils.equals("", r_salescd3nm)) return list;
			
			svcMap.put("r_salescd1nm", r_salescd1nm);
			svcMap.put("r_salescd2nm", r_salescd2nm);
			svcMap.put("r_salescd3nm", r_salescd3nm);
			svcMap.put("r_groupby", "SALES_CD4_NM");
			//svcMap.put("r_orderby", "SALES_CD4_NM ASC ");
		}
		
		list = itemDao.listForCategory(svcMap);
		return list;
	}
	
	/**
	 * Get O_ITEM One.
	 * @작성일 : 2020. 3. 17.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> getItemOne(String item_cd){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_itemcd", item_cd);
		return this.getItemOne(svcMap);
	}
	public Map<String, Object> getItemOne(Map<String, Object> svcMap){
		return itemDao.one(svcMap);
	}
	
	/**
	 * Get ITEMINFO One.
	 * @작성일 : 2020. 3. 19.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> getItemInfoOne(Map<String, Object> svcMap){
		return itemDao.oneInfo(svcMap);
	}
	
	/**
	 * Get O_ITEM List.
	 * @작성일 : 2020. 3. 17.
	 * @작성자 : kkyu
	 */
	public List<Map<String, Object>> getItemList(Map<String, Object> svcMap){
		return itemDao.list(svcMap);
	}
	
	/**
	 * Get ItemRecommend List.
	 * @작성일 : 2020. 3. 17.
	 * @작성자 : kkyu
	 */
	public List<Map<String, Object>> getItemRecommendList(String itr_itemcd, String order_by){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_itritemcd", itr_itemcd);
		svcMap.put("r_orderby", (StringUtils.equals("", order_by) ? "ITR_SORT ASC " : order_by));
		return this.getItemRecommendList(svcMap);
	}
	public List<Map<String, Object>> getItemRecommendList(Map<String, Object> svcMap){
		return itemDao.listRecommend(svcMap);
	}
	
	/**
	 * 품목 리스트 가져오기.
	 * @작성일 : 2020. 3. 17.
	 * @작성자 : kkyu
	 * @param where : front=프론트, frontexcel=프론트 엑셀다운로드 / 빈값=관리자 , excel=관리자 엑셀다운로드.
	 * @param r_itemdeliverytype : Y=배송비 아이템만 출력, 빈값=배송비 아이템 제외하고 출력, ALL=모두출력.
	 */
	public Map<String, Object> getItemList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		String where = Converter.toStr(params.get("where"));
		String r_checkitembookmark = Converter.toStr(params.get("r_checkitembookmark"));
		//if(StringUtils.equals("front", where) || StringUtils.equals("frontexcel", where)) {
			params.put("r_itbuserid", loginDto.getUserId());
			params.put("r_checkitembookmark", r_checkitembookmark);
		//}
		
		// 검색 파라미터 정의.
		//String r_salescd1nm = Converter.toStr(params.get("r_salescd1nm")); // 카테고리명1.
		//String r_salescd2nm = Converter.toStr(params.get("r_salescd2nm")); // 카테고리명2.
		//String r_salescd3nm = Converter.toStr(params.get("r_salescd3nm")); // 카테고리명3.
		//String r_salescd4nm = Converter.toStr(params.get("r_salescd4nm")); // 카테고리명4.
		
		//String rl_desc1 = Converter.toStr(params.get("rl_desc1")); // 품목명1.
		//String rl_desc2 = Converter.toStr(params.get("rl_desc2")); // 품목명2.
		//String rl_searchtext = Converter.toStr(params.get("rl_searchtext")); // SEARCH-TEXT.
		
		//String rl_itemcd = Converter.toStr(params.get("rl_itemcd")); // 품목코드.
		//String rl_thicknm = Converter.toStr(params.get("rl_thicknm")); // 품목 두께.
		//String rl_widthnm = Converter.toStr(params.get("rl_widthnm")); // 품목 폭.
		//String rl_lengthnm = Converter.toStr(params.get("rl_lengthnm")); // 품목 길이.
		
		String ri_existimageyn = Converter.toStr(params.get("ri_existimageynarr")); // N=이미지 등록 안되어있는 품목 검색.
		String ri_existrecommend = Converter.toStr(params.get("ri_existrecommendarr")); // N=추천품목이 없는 품목 검색.
		params.put("ri_existimageyn", ri_existimageyn);
		params.put("ri_existrecommend", ri_existrecommend);
		
		logger.debug("ri_existimageyn : {}", ri_existimageyn);
		logger.debug("ri_existrecommend : {}", ri_existrecommend);

		int totalCnt = itemDao.cnt(params);

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
		if(StringUtils.equals("", sidx) || StringUtils.equals("frontexcel", where)) { r_orderby = "ISNULL(ITI_SORT, 9999999) ASC, DESC1 ASC "; } //디폴트 지정
		
		if(StringUtils.equals("Y", r_checkitembookmark) ) { 
		//if(StringUtils.equals("front", where) && StringUtils.equals("Y", r_checkitembookmark) ) { 
			r_orderby = "ITB_SORT ASC "; 
		} // 프론트이고, 즐겨찾기만 불러왔을 경우.
		//if((StringUtils.equals("front", where) || StringUtils.equals("frontexcel", where)) && StringUtils.equals("Y", r_checkitembookmark) ) { r_orderby = "ITB_SORT ASC "; } // 프론트이고, 즐겨찾기만 불러왔을 경우.

		params.put("r_orderby", r_orderby);

		// 엑셀 다운로드.
		if(StringUtils.equals("excel", where) || StringUtils.equals("frontexcel", where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}
		
		List<Map<String, Object>> list = this.getItemList(params);
		resMap.put("list", list);
		resMap.put("data", list);
		resMap.put("page", params.get("r_page"));
		
		resMap.put("where", where);
		
		return resMap;
	}
	
	public Map<String, Object> getItemManageList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		String where = Converter.toStr(params.get("where"));
		String r_checkitembookmark = Converter.toStr(params.get("r_checkitembookmark"));
		params.put("r_itbuserid", loginDto.getUserId());
		params.put("r_checkitembookmark", r_checkitembookmark);
		
		
		String ri_existimageyn = Converter.toStr(params.get("ri_existimageynarr")); // N=이미지 등록 안되어있는 품목 검색.
		String ri_existrecommend = Converter.toStr(params.get("ri_existrecommendarr")); // N=추천품목이 없는 품목 검색.
		params.put("ri_existimageyn", ri_existimageyn);
		params.put("ri_existrecommend", ri_existrecommend);
		
		logger.debug("ri_existimageyn : {}", ri_existimageyn);
		logger.debug("ri_existrecommend : {}", ri_existrecommend);

		int totalCnt = itemDao.itemManageCnt(params);

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

		r_orderby = sidx + " " + sord;
		if(StringUtils.equals("", sidx)) { r_orderby = "ITEM_CD ASC "; } //디폴트 지정
		params.put("r_orderby", r_orderby);

		// 엑셀 다운로드.
		if(StringUtils.equals("excel", where) || StringUtils.equals("frontexcel", where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}
		
		List<Map<String, Object>> list = itemDao.itemManageList(params);
		resMap.put("list", list);
		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		resMap.put("page", params.get("r_page"));
		
		
		resMap.put("startnumber", 1);
		
		return resMap;
	}
	
	/**
	 * 품목 수정.
	 * @작성일 : 2020. 3. 18.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> updateItemTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		// 필수 파라미터 체크.
		String r_itemcd = Converter.toStr(params.get("r_itemcd"));
		if(StringUtils.equals("", r_itemcd)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		
		// 품목 데이터 유무 체크.
		Map<String, Object> item = this.getItemOne(r_itemcd);
		if (CollectionUtils.isEmpty(item)) throw new LimeBizException(MsgCode.DATA_NOT_FIND_ERROR, "품목");

		String sepa = System.getProperty("file.separator");
		String folderName = "item";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
		
		//# 0. ItemInfo Update인 경우 파일삭제 여부 체크 후 삭제.
		logger.debug("품목 수정여부 > ITI_ITEMCD가 빈값이 아닌경우 수정 : {}", Converter.toStr(item.get("ITI_ITEMCD")));
		boolean fileDeleteTf = false;
		if(!StringUtils.equals("", Converter.toStr(item.get("ITI_ITEMCD")))) {
			if(StringUtils.equals("Y", Converter.toStr(params.get("m_itifile1_delyn")))) {
				fileDeleteTf = true;
				params.put("r_delfile1", "Y");
			}
			if(StringUtils.equals("Y", Converter.toStr(params.get("m_itifile2_delyn")))) {
				fileDeleteTf = true;
				params.put("r_delfile2", "Y");
			}
			if(StringUtils.equals("Y", Converter.toStr(params.get("m_itifile3_delyn")))) {
				fileDeleteTf = true;
				params.put("r_delfile3", "Y");
			}
			if(StringUtils.equals("Y", Converter.toStr(params.get("m_itifile4_delyn")))) {
				fileDeleteTf = true;
				params.put("r_delfile4", "Y");
			}
			if(StringUtils.equals("Y", Converter.toStr(params.get("m_itifile5_delyn")))) {
				fileDeleteTf = true;
				params.put("r_delfile5", "Y");
			}
			
			if(fileDeleteTf) {
				params.put("r_itiitemcd", r_itemcd);
				itemDao.upInfoForFile(params);
			}
		}
		
		//# 1. Insert or Update ItemInfo.
		String m_itifile1 = "";
		String m_itifile1type = "";
		String m_itifile2 = "";
		String m_itifile2type = "";
		String m_itifile3 = "";
		String m_itifile3type = "";
		String m_itifile4 = "";
		String m_itifile4type = "";
		String m_itifile5 = "";
		String m_itifile5type = "";
		
		// 파일 저장.
		List<Map<String, Object>> fList = new ArrayList<>();
		try {
			if (!MultipartHttpServletRequest.class.isInstance(req)) {
				throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
			}
			MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest) req;
			fList = fileUpload.execManyField(mtreq, "imageFiles", uploadDir, "m_itifile1", "m_itifile2", "m_itifile3", "m_itifile4", "m_itifile5");
			logger.debug("fList : {}", fList);
			
			for(Map<String, Object> file : fList) {
				if(StringUtils.equals("m_itifile1", Converter.toStr(file.get("fieldName")))) {
					m_itifile1 = Converter.toStr(fList.get(0).get("saveFileName"));
					m_itifile1type = Converter.toStr(fList.get(0).get("mimeType"));
				}
				if(StringUtils.equals("m_itifile2", Converter.toStr(file.get("fieldName")))) {
					m_itifile2 = Converter.toStr(fList.get(1).get("saveFileName"));
					m_itifile2type = Converter.toStr(fList.get(1).get("mimeType"));
				}
				if(StringUtils.equals("m_itifile3", Converter.toStr(file.get("fieldName")))) {
					m_itifile3 = Converter.toStr(fList.get(2).get("saveFileName"));
					m_itifile3type = Converter.toStr(fList.get(2).get("mimeType"));
				}
				if(StringUtils.equals("m_itifile4", Converter.toStr(file.get("fieldName")))) {
					m_itifile4 = Converter.toStr(fList.get(3).get("saveFileName"));
					m_itifile4type = Converter.toStr(fList.get(3).get("mimeType"));
				}
				if(StringUtils.equals("m_itifile5", Converter.toStr(file.get("fieldName")))) {
					m_itifile5 = Converter.toStr(fList.get(4).get("saveFileName"));
					m_itifile5type = Converter.toStr(fList.get(4).get("mimeType"));
				}
			}
			
		} catch (Exception e) {
			fileUpload.deleteList(fList, uploadDir);
			throw e;
		}
		
		String m_itiitemcd = r_itemcd;
		String m_itipallet = Converter.toStr(params.get("m_itipallet")).replaceAll(",", "");
		String m_itisearchword = Converter.toStr(params.get("m_itisearchword")).trim(); // 연관검색어 ,로 구분 & 앞뒤공백 제거.
		String m_itiinid = loginDto.getUserId();
		
		params.put("m_itiitemcd", m_itiitemcd);
		params.put("m_itipallet", m_itipallet);
		params.put("m_itisearchword", m_itisearchword);
		params.put("m_itiinid", m_itiinid);
		
		params.put("m_itifile1", m_itifile1);
		params.put("m_itifile1type", m_itifile1type);
		params.put("m_itifile2", m_itifile2);
		params.put("m_itifile2type", m_itifile2type);
		params.put("m_itifile3", m_itifile3);
		params.put("m_itifile3type", m_itifile3type);
		params.put("m_itifile4", m_itifile4);
		params.put("m_itifile4type", m_itifile4type);
		params.put("m_itifile5", m_itifile5);
		params.put("m_itifile5type", m_itifile5type);
		itemDao.mergeInfo(params);
		
		//# 2. Delete ItemRecommend And Insert ItemRecommend.
		params.put("r_itritemcd", r_itemcd);
		itemDao.delRecommend(params);
		
		List<Map<String, Object>> itemRecommendList = new ArrayList<>();
		String[] mi_itrrecitemcd = req.getParameterValues("m_itrrecitemcd"); 
		String[] mi_itrsort = req.getParameterValues("m_itrsort");
		if(!ArrayUtils.isEmpty(mi_itrrecitemcd)) {
			for(int i=0,j=mi_itrrecitemcd.length; i<j; i++) {
				if(!StringUtils.equals("", mi_itrrecitemcd[i])) {
					Map<String, Object> itemRecommend = new HashMap<>();
					itemRecommend.put("ITR_ITEMCD", r_itemcd);
					itemRecommend.put("ITR_RECITEMCD", Converter.toStr(mi_itrrecitemcd[i]));
					itemRecommend.put("ITR_SORT", Converter.toInt(mi_itrsort[i]));
					itemRecommendList.add(itemRecommend);
				}
			}
			
			if(!itemRecommendList.isEmpty()) {
				params.put("itemRecommendList", itemRecommendList);
				itemDao.inRecommendByArr(params);
			}
		}
		
		//# 3. Delete ItemSearch And Insert ItemSearch.
		params.put("r_itsitemcd", r_itemcd);
		itemDao.delSearch(params);
		
		List<Map<String, Object>> itemSearchList = this.getItemSearchList(r_itemcd, m_itisearchword, "1");
		params.put("itemSearchList", itemSearchList);
		itemDao.inSearchByArr(params);
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 선택한 출고지(사업장) and 품목명으로 품목코드 리스트 가져오기.
	 * @작성일 : 2020. 4. 17.
	 * @작성자 : kkyu
	 * @param ri_trid : 뷰단 table > tr > id. ||로 구분한 문자열.
	 * @param ri_itemmcu : 출고지(사업장)코드. ||로 구분한 문자열.
	 * @param ri_desc1 : 품목명1. ||로 구분한 문자열.
	 */
	public Map<String, Object> getItemMcuList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		String ri_trid = Converter.toStr(params.get("ri_trid"));
		String ri_itemmcu = Converter.toStr(params.get("ri_itemmcu"));
		String ri_desc1 = Converter.toStr(params.get("ri_desc1"));
		
		if(StringUtils.equals("", ri_trid) || StringUtils.equals("", ri_itemmcu) || StringUtils.equals("", ri_desc1)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		
		String divider = "!!";
		String[] ri_tridarr = ri_trid.split(divider);
		String[] ri_itemmcuarr = ri_itemmcu.split(divider);
		String[] ri_desc1arr = ri_desc1.split(divider);
		
		
		for(int i=0,j=ri_itemmcuarr.length; i<j; i++) {
			params.clear();
			params.put("r_itemmcu", ri_itemmcuarr[i]);
			params.put("r_desc1", ri_desc1arr[i]);
			System.out.println(params);
			List<Map<String, Object>> itemMcuList = itemDao.listItemMcu(params);
			
			String gyptext = "";
			String gyp_1 = "805672";
			String gyp_2 = "805673"; 
			if( ( (ri_itemmcuarr[i].compareTo("4636") == 0) 
					|| (ri_itemmcuarr[i].compareTo("5618") == 0)
					|| (ri_itemmcuarr[i].compareTo("5619") == 0)
					|| (ri_itemmcuarr[i].compareTo("5620") == 0)
					|| (ri_itemmcuarr[i].compareTo("5615") == 0)
					|| (ri_itemmcuarr[i].compareTo("5616") == 0)
					|| (ri_itemmcuarr[i].compareTo("5617") == 0) ) && gyptext.isEmpty() ) {
				for(Map<String, Object> o : itemMcuList) {
					if( o.get("ITEM_CD") != null ){
						if( o.get("ITEM_CD").toString().compareTo(gyp_1) == 0 )
							gyptext = gyp_2;
						else if ( o.get("ITEM_CD").toString().compareTo(gyp_2) == 0 )
							gyptext = gyp_1;
					}
				}
			}
			if(!gyptext.isEmpty()) {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("ITEM_CD", gyptext);
				itemMcuList.add(m);
			}
			
			resMap.put(ri_tridarr[i], itemMcuList);
		}
		
		logger.debug("resMap : {}", resMap);
		return resMap;
	}
	
	/**
	 * 주문 > 선택한 출고지(사업장) and 품목코드로  재고 가져오기.
	 * + 품목코드로 중량 가져오기.
	 * @작성일 : 2020. 4. 17.
	 * @작성자 : kkyu
	 * @param ri_trid : 뷰단 table > tr > id. ||로 구분한 문자열.
	 * @param ri_itemmcu : 출고지(사업장)코드. ||로 구분한 문자열.
	 * @param ri_itemcd : 품목코드. ||로 구분한 문자열.
	 */
	/*public Map<String, Object> getItemStock(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		String ri_trid = Converter.toStr(params.get("ri_trid"));
		String ri_itemmcu = Converter.toStr(params.get("ri_itemmcu"));
		String ri_itemcd = Converter.toStr(params.get("ri_itemcd"));
		
		if(StringUtils.equals("", ri_trid) || StringUtils.equals("", ri_itemmcu) || StringUtils.equals("", ri_itemcd)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		
		String divider = "!!";
		String[] ri_tridarr = ri_trid.split(divider);
		String[] ri_itemmcuarr = ri_itemmcu.split(divider);
		String[] ri_itemcdarr = ri_itemcd.split(divider);
		
		for(int i=0,j=ri_itemmcuarr.length; i<j; i++) {
			params.put("r_itemcd", ri_itemcdarr[i]);
			params.put("r_mcucd", ri_itemmcuarr[i]);
			Map<String, Object> itemStock = commonDao.getItemStock(params);
			//Map<String, Object> itemStock = commonDao.getItemStock2(params);
			Map<String, Object> item = this.getItemOne(ri_itemcdarr[i]);
			
			logger.debug("itemStock : {}", itemStock);
			
			// 임시 테스트
//			itemStock = new HashMap<>();
//			if(0==i) itemStock.put("TOT_AVAIL","12315.181");
//			if(1==i) itemStock.put("TOT_AVAIL","-18544123.11");
//			if(2==i) itemStock.put("TOT_AVAIL","-13513135");
			// End
			
			resMap.put(ri_tridarr[i], itemStock);
			resMap.put(ri_tridarr[i]+"_WEIGHT", Converter.toStr(item.get("WEIGHT")));
		}
		logger.debug("resMap : {}", resMap);
		return resMap;
	}*/
	
	/**
	 * 연관검색어 리스트 만들기.
	 * @작성일 : 2020. 3. 18.
	 * @작성자 : kkyu
	 * @param its_itemcd : ItemSearch.ITS_ITEMCD
	 * @param iti_searchword : ItemInfo.ITI_SEARCHWORD
	 * @param its_type : ItemSearch.ITS_TYPE (1=연관검색어)
	 */
	public List<Map<String, Object>> getItemSearchList(String its_itemcd, String iti_searchword, String its_type) throws Exception{
		List<Map<String, Object>> itemSearchList = new ArrayList<>();
		
		String[] searchWord = iti_searchword.split(" ");
		String[] word;
		for(String words : searchWord){
			word = words.trim().split(",");
			for(String text : word){
				if(!"".equals(text)){
					Map<String, Object> itemSearch = new HashMap<>();
					itemSearch.put("ITS_ITEMCD", its_itemcd);
					itemSearch.put("ITS_TEXT", text.toUpperCase()); // 대문자 전환.
					itemSearch.put("ITS_TYPE", its_type);
					itemSearchList.add(itemSearch);
				}
			}
		}
		logger.debug("itemSearchList : {}", itemSearchList);
		
		return itemSearchList;
	}
	
	/**
	 * 품목 즐겨찾기 저장/삭제.
	 * @작성일 : 2020. 3. 26.
	 * @작성자 : kkyu
	 * @param [필수] r_bookmarkprocesstype : IN=저장,DEL=삭제. 
	 * @param [필수] ri_itbitemcd : 품목코드 ,로 구분.
	 * @param ri_itbsort : 즐겨찾기 순서 ,로 구분.
	 */
	public Map<String, Object> setItemBookmarkTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		String r_bookmarkprocesstype = Converter.toStr(params.get("r_bookmarkprocesstype"));
		String ri_itbitemcds = Converter.toStr(params.get("ri_itbitemcd"));
		String ri_itbsorts = Converter.toStr(params.get("ri_itbsort"));
		
		if(StringUtils.equals("", r_bookmarkprocesstype) || StringUtils.equals("", ri_itbitemcds)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		
		String[] ri_itbitemcd = ri_itbitemcds.split(",", -1);
		String[] ri_itbsort = ri_itbsorts.split(",", -1);
		
		// Insert Bookmark.
		if(StringUtils.equals("IN", r_bookmarkprocesstype)) {
			params.put("m_itbuserid", loginDto.getUserId());
			params.put("m_itbinid", loginDto.getUserId());
			for(int i=0,j=ri_itbitemcd.length; i<j; i++) {
				params.put("m_itbitemcd", ri_itbitemcd[i]);
				params.put("m_itbsort", ri_itbsort[i]);
				itemDao.mergeBookmark(params);
			}
		}
		// Delete Bookmark.
		else if(StringUtils.equals("DEL", r_bookmarkprocesstype)) {
			params.put("r_itbuserid", loginDto.getUserId());
			params.put("ri_itbitemcd", ri_itbitemcd);
			itemDao.delBookmark(params);
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 품목 파일 다운로드.
	 * @작성일 : 2020. 3. 19.
	 * @작성자 : kkyu
	 * @수정일 : 2020. 4. 27.
	 * @수정자 : isaac
	 * @수정내용 : 일괄관리 구현 중 상품이미지 다운로드 시 filetype 체크 로직 주석
	 */
	public ModelAndView itemFileDown(Map<String, Object> params, HttpServletRequest req, Model model) throws Exception {
		String sepa = System.getProperty("file.separator");
		String folderName = "item";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
		
		String r_filecode = Converter.toStr(params.get("r_filecode")); // ITI_ITEMCD
		String r_filenum = Converter.toStr(params.get("r_filenum")); // 몇번째 파일인지 1,2,3,4,5
		String r_filename = HttpUtils.restoreXss(Converter.toStr(params.get("r_filename")));
//		String r_filetype = Converter.toStr(params.get("r_filetype"));
		
		if(StringUtils.equals("", r_filecode) || StringUtils.equals("", r_filenum) || StringUtils.equals("", r_filename) ) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR, "파일 고유번호");
//		if(StringUtils.equals("", r_filecode) || StringUtils.equals("", r_filenum) || StringUtils.equals("", r_filename) || StringUtils.equals("", r_filetype)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR, "파일 고유번호");

		params.put("r_itiitemcd", r_filecode);
		params.put("r_itifile"+r_filenum, r_filename);
//		params.put("r_itifile"+r_filenum+"type", r_filetype);
		Map<String, Object> itemFile = this.getItemInfoOne(params);
		logger.debug("itemFile : {}", itemFile);
		
		Map<String, Object> afMap = new HashMap<>();
		afMap.put("FOLDER_NAME", uploadDir);
		afMap.put("FILE_NAME", itemFile.get("ITI_FILE1"));
//		afMap.put("FILE_TYPE", itemFile.get("ITI_FILE1TYPE"));
		model.addAttribute("afMap", afMap);
		
		return new ModelAndView(new FileDown());
	}

	/**
	 * 품목 엑셀 대량 등록/수정
	 * @작성일 : 2020. 4. 24.
	 * @작성자 : isaac
	 */
	public Map<String, Object> updateItemExcelTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		if (!MultipartHttpServletRequest.class.isInstance(req)) {
			throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
		}

		Map<String, Object> svcMap = new HashMap<>();
		Map<String, Object> itemMap = new HashMap<>();
		List<Map<String, Object>> itemSearchList = new ArrayList<>();
		List<Map<String, Object>> itemRecommendList = new ArrayList<>();

		String m_itiinid = loginDto.getUserId();

		FileUtil fileUtil = new FileUtil();
		MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;
		MultipartFile mpf = mtreq.getFile("file");
		Workbook workbook = WorkbookFactory.create(mpf.getInputStream());
		Sheet sheet = workbook.getSheetAt(0);

		DataFormatter formatter = new DataFormatter();
		Iterator<Row> it = sheet.iterator();

//		long start = System.currentTimeMillis();
//		long end = 0;
		int rowNum = 0, cellNum = 0;

		Map<String, Object> resMap = new HashMap<>();
		while (it.hasNext()) {
			Row row = it.next();

			if(rowNum > 0){
				cellNum=0;
				//Working!
				String m_itiitemcd = formatter.formatCellValue(row.getCell(cellNum++)).replaceAll("\\s","");

				if (StringUtils.equals("", m_itiitemcd)) {
					if(rowNum == 1){
						resMap.put("RES_MSG",(rowNum+1)+"행의 품목코드가 누락되었습니다.");
					}else{
						resMap.put("RES_MSG",rowNum+"행까지 정상적으로 처리되었습니다.");
					}

					resMap.put("RES_CODE","0010");
//					end = System.currentTimeMillis();
					return resMap;
				}

				//품목코드 체크
				itemMap = this.getItemOne(m_itiitemcd);
				if(CollectionUtils.isEmpty(itemMap)){
					throw new LimeBizException(MsgCode.DATA_EXCEL_NOT_FIND_ERROR, rowNum+1);
				}


				String m_itipallet = formatter.formatCellValue(row.getCell(cellNum++)).replaceAll("[, ;]", ""); //쉼표, 공백제거

				String m_itisearchword = formatter.formatCellValue(row.getCell(cellNum++)).trim();
				String m_itifile1 = formatter.formatCellValue(row.getCell(cellNum++)).trim();
				String m_itifile2 = formatter.formatCellValue(row.getCell(cellNum++)).trim();
				String m_itifile3 = formatter.formatCellValue(row.getCell(cellNum++)).trim();
				String m_itifile4 = formatter.formatCellValue(row.getCell(cellNum++)).trim();
				String m_itifile5 = formatter.formatCellValue(row.getCell(cellNum++)).trim();

				if (!"".equals(m_itifile1)) m_itifile1 = fileUtil.fileUrlDownload(m_itifile1, req.getSession().getServletContext().getRealPath("/data/item"));
				if (!"".equals(m_itifile1)) m_itifile2 = fileUtil.fileUrlDownload(m_itifile2, req.getSession().getServletContext().getRealPath("/data/item"));
				if (!"".equals(m_itifile1)) m_itifile3 = fileUtil.fileUrlDownload(m_itifile3, req.getSession().getServletContext().getRealPath("/data/item"));
				if (!"".equals(m_itifile1)) m_itifile4 = fileUtil.fileUrlDownload(m_itifile4, req.getSession().getServletContext().getRealPath("/data/item"));
				if (!"".equals(m_itifile1)) m_itifile5 = fileUtil.fileUrlDownload(m_itifile5, req.getSession().getServletContext().getRealPath("/data/item"));

				String m_itrrecitemcd =  formatter.formatCellValue(row.getCell(cellNum++)).trim();
				String m_itilink = formatter.formatCellValue(row.getCell(cellNum++)).trim();
				String m_itisort = formatter.formatCellValue(row.getCell(cellNum++)).trim();

				svcMap.put("m_itiitemcd", m_itiitemcd);
				svcMap.put("m_itipallet", m_itipallet);
				svcMap.put("m_itisort", m_itisort);

				//연관검색어 빈값인 경우 품목명1 저장
				if(StringUtils.equals("", m_itisearchword)){
					svcMap.put("r_itemcd",m_itiitemcd);
//					int iteminfoCnt = itemDao.iteminfoCnt(svcMap);
					if(StringUtils.equals("",Converter.toStr(itemMap.get("ITI_ITEMCD")))){
						m_itisearchword = Converter.toStr(itemMap.get("DESC1"));
						itemMap.clear();
					}
				}

				svcMap.put("m_itisearchword", m_itisearchword);
				svcMap.put("m_itifile1", m_itifile1);
				svcMap.put("m_itifile2", m_itifile2);
				svcMap.put("m_itifile3", m_itifile3);
				svcMap.put("m_itifile4", m_itifile4);
				svcMap.put("m_itifile5", m_itifile5);
				svcMap.put("m_itilink", m_itilink);
				svcMap.put("m_itiinid", m_itiinid);

				itemDao.mergeInfo(svcMap);

				//ITEMSEARCH 연관검색어
				if(!StringUtils.equals("", m_itisearchword)){
					svcMap.put("r_itsitemcd", m_itiitemcd);
					itemDao.delSearch(svcMap);

					itemSearchList = this.getItemSearchList(m_itiitemcd, m_itisearchword, "1");
					svcMap.put("itemSearchList", itemSearchList);
					itemDao.inSearchByArr(svcMap);

					itemSearchList.clear();
				}

				//ITEMRECOMMEND 추천상품
				if(!StringUtils.equals("", m_itrrecitemcd)){
					svcMap.put("r_itritemcd", m_itiitemcd);
					itemDao.delRecommend(svcMap);

					itemRecommendList = this.recStringToRecArray(m_itiitemcd,m_itrrecitemcd);
					if(!itemRecommendList.isEmpty()){
						svcMap.put("itemRecommendList", itemRecommendList);
						itemDao.inRecommendByArr(svcMap);

					}

					itemRecommendList.clear();
				}

				svcMap.clear();

			}
			rowNum++;

		}
		/*
		end = System.currentTimeMillis();
		logger.debug("################## 반복문 수행 시간 = "+ (end - start)/1000.0);
		*/
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}

	/**
	 * 추천상품 리스트 만들기 (상품개수 최대 10개)
	 * @작성일 : 2020. 4. 26.
	 * @작성자 : isaac
	 * @param itr_itemcd : ItemRecommend.ITR_ITEMCD
	 * @param itr_recitemcd : ItemRecommend.ITR_RECITEMCD
	 * @param itr_sort : ItemRecommend.ITR_SORT (노출순서)
	 */
	public List<Map<String, Object>> recStringToRecArray(String itr_itemcd,String itr_recitemcd) throws Exception{
		List<Map<String, Object>> itemRecommendList = new ArrayList<>();
		String[] recommendItem = itr_recitemcd.replaceAll("\\s","").split(","); //쉼표

		ArrayList<String> recommendItemList = new ArrayList<>();

		//중복값 제거
		for(String data : recommendItem){
			if(!recommendItemList.contains(data)){
				recommendItemList.add(data);
			}
		}

		int sortNum = 1;
		for(String text : recommendItemList){

			String m_itrrecitemcd = text.toUpperCase(); // 모든 공백 제거

			if(!"".equals(text) && sortNum < 11){ //최대 10개까지
				Map<String, Object> itemRecommend = new HashMap<>();

				//품목 여부 확인
				itemRecommend.put("r_itemcd", m_itrrecitemcd);
				//등록되지 않은 품목코드는 넘어감
				if(itemDao.cntByItemcd(itemRecommend) == 0){
					continue;
				}

				itemRecommend.put("ITR_ITEMCD", itr_itemcd);
				itemRecommend.put("ITR_RECITEMCD", m_itrrecitemcd); // 대문자 전환.
				itemRecommend.put("ITR_SORT", sortNum++);
				itemRecommendList.add(itemRecommend);
			}
		}

		return itemRecommendList;
	}
	/**
	 * 품목정보 노출순서 수정
	 * @작성일 : 2020. 5. 12.
	 * @작성자 : isaac
	 */
	public Map<String, Object> updateItemInfoSort(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
			String[] r_itemcdArr = req.getParameterValues("r_itemcdArr");
			String[] m_itisortArr = req.getParameterValues("m_itisortArr");
			String userId = loginDto.getUserId();

			if(ArrayUtils.isEmpty(r_itemcdArr)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);

			Map<String, Object> svcMap = new HashMap<>();
			Map<String, Object> itemMap = new HashMap<>();

			int index = 0;
			for(String itemcd : r_itemcdArr){
				itemMap.putAll(this.getItemOne(itemcd));
				if(CollectionUtils.isEmpty(itemMap)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);

				svcMap.put("m_itiitemcd",itemcd);
				svcMap.put("m_itiinid",userId);

				if(StringUtils.equals("",Converter.toStr(itemMap.get("ITI_ITEMCD")))){
					svcMap.put("m_itisearchword",Converter.toStr(itemMap.get("DESC1")));
				}

				svcMap.put("m_itisort", m_itisortArr[index]);

				itemDao.mergeInfo(svcMap);

				index++;
				itemMap.clear();
				svcMap.clear();
			}

			return MsgCode.getResultMap(MsgCode.SUCCESS);
	}

	public Map<String, Object> updateLineTy(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
//		Map<String, Object> resMap = new HashMap<>();
		
		int ret = itemDao.updateLineTy(params);
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
}

