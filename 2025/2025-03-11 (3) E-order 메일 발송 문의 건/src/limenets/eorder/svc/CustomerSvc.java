package com.limenets.eorder.svc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.common.util.Pager;
import com.limenets.eorder.dao.CustomerDao;
import com.limenets.eorder.dao.SalesOrderDao;
import com.limenets.eorder.dao.ShipToDao;

/**
 * 거래처 & 납품처 서비스
 */
@Service
public class CustomerSvc {
	private static final Logger logger = LoggerFactory.getLogger(CustomerSvc.class);
	
	@Inject private CustomerDao customerDao;
	@Inject private ShipToDao shipToDao;
	@Inject private SalesOrderDao salesOrderDao;
	
	
	public List<Map<String, Object>> getCustomerList(Map<String, Object> svcMap){
		return customerDao.list(svcMap);
	}
	
	public int getCustomerCnt(Map<String, Object> svcMap){
		return customerDao.cnt(svcMap);
	}
	
	public List<Map<String, Object>> getShipToList(Map<String, Object> svcMap){
		return shipToDao.list(svcMap);
	}
	
	public Map<String, Object> getCustomer(String r_custcd){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_custcd", r_custcd);
		return this.getCustomer(svcMap);
	}
	public Map<String, Object> getCustomer(Map<String, Object> svcMap){
		return customerDao.one(svcMap);
	}
	
	public Map<String, Object> getShipTo(String r_shiptocd){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_shiptocd", r_shiptocd);
		return shipToDao.one(svcMap);
	}
	
	public int getBookmarkCnt(String r_userid, String r_shiptocd){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_stbuserid", r_userid);
		svcMap.put("r_stbshiptocd", r_shiptocd);
		return shipToDao.cntBookmark(svcMap);
	}
	
	public Map<String, Object> getOneViewSupplier(String r_custcd){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_custcd", r_custcd);
		return customerDao.getOneViewSupplier(svcMap);
	}
	
	/**
	 * 거래처 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 10.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> getCustomerList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();

		int totalCnt = customerDao.cnt(params);

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
		// 2024-10-16 hsg 별칭 오류가 나서 수정. CU -> XX)
		if(StringUtils.equals("", sidx)) { r_orderby = "XX.CUST_CD ASC"; } //디폴트 지정
		params.put("r_orderby", r_orderby);

		// 엑셀 다운로드.
		String where = Converter.toStr(params.get("where"));
		if ("excel".equals(where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}
		
		List<Map<String, Object>> list = this.getCustomerList(params);
		resMap.put("list", list);
		resMap.put("data", list);
		resMap.put("page", params.get("r_page"));
		
		return resMap;
	}
	
	/**
	 * 납품처 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 3. 12.
	 * @작성자 : an
	 */
	public Map<String, Object> getShipToList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();

		int totalCnt = shipToDao.cnt(params);

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
		if(StringUtils.equals("", sidx)) { r_orderby = "SHIPTO_CD DESC"; } //디폴트 지정
		params.put("r_orderby", r_orderby);

		// 엑셀 다운로드.
		String where = Converter.toStr(params.get("where"));
		if ("excel".equals(where) || "mypage".equals(where) || "all".equals(where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}
		
		List<Map<String, Object>> list = this.getShipToList(params);
		resMap.put("list", list);
		resMap.put("data", list);
		
		return resMap;
	}
	
	/**
	 * 납품처 리스트 가져오기 By O_SALESORDER.
	 * @작성일 : 2020. 6. 1.
	 * @작성자 : kkyu
	 * @param 
	 */
	public Map<String, Object> getShipToListBySalesOrder(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		String authority = loginDto.getAuthority();
		if(StringUtils.equals("CO", authority) && StringUtils.equals("CT", authority)) {
			params.put("r_custcd", loginDto.getCustCd());
			if(StringUtils.equals("CT", authority)) {
				params.put("r_shiptocd", loginDto.getShiptoCd());
			}
		}
		
		//String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt"));
		//String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt"));
		String r_custcd = Converter.toStr(params.get("r_custcd"));
		if(StringUtils.equals("", r_custcd)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		
		List<Map<String, Object>> list = salesOrderDao.getOrderShipNmGroup(params);
		resMap.put("list", list);
		resMap.put("data", list);
		
		return resMap;
	}
	
	/**
	 * 착지주소 리스트 가져오기 By O_SALESORDER.
	 * @작성일 : 2020. 6. 2.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> getAdd1ListBySalesOrder(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		String authority = loginDto.getAuthority();
		if(StringUtils.equals("CO", authority) && StringUtils.equals("CT", authority)) {
			params.put("r_custcd", loginDto.getCustCd());
		}
		
		//String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt"));
		//String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt"));
		String r_custcd = Converter.toStr(params.get("r_custcd"));
		if(StringUtils.equals("", r_custcd)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		
		List<Map<String, Object>> list = salesOrderDao.getOrderAdd1Group2(params);
		resMap.put("list", list);
		resMap.put("data", list);
		
		return resMap;
	}
	
	/**
	 * 품목 리스트 가져오기 By O_SALESORDER.
	 * @작성일 : 2020. 6. 2.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> getItemDescListBySalesOrder(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		String authority = loginDto.getAuthority();
		if(StringUtils.equals("CO", authority) && StringUtils.equals("CT", authority)) {
			params.put("r_custcd", loginDto.getCustCd());
		}
		
		String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt"));
		String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt"));
		params.put("r_actualshipsdt", r_actualshipsdt.replaceAll("-", "").trim());
		params.put("r_actualshipedt", r_actualshipedt.replaceAll("-", "").trim());
		
		String r_custcd = Converter.toStr(params.get("r_custcd"));
		if(StringUtils.equals("", r_custcd)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		
//		String r_shiptocd = Converter.toStr(params.get("r_shiptocd"));
		String r_shiptonm = Converter.toStr(params.get("r_shiptonm"));
		String[] ri_add1 = req.getParameterValues("ri_add1");
//		if(StringUtils.equals("", r_shiptocd) && ArrayUtils.isEmpty(ri_add1)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		if(StringUtils.equals("", r_shiptonm) && ArrayUtils.isEmpty(ri_add1)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		params.put("ri_add1", ri_add1);
		
		List<Map<String, Object>> list = salesOrderDao.getOrderItemDescGroup(params);
		resMap.put("list", list);
		resMap.put("data", list);
		
		return resMap;
	}

	
	/**
	 * 납품처 즐겨찾기 저장 Ajax.
	 * @작성일 : 2020. 4. 9.
	 * @작성자 : an
	 */
	public Map<String, Object> setShiptoBookmarkAjax(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		Map<String, Object> svcMap = new HashMap<>();
		
		String m_stbuserid = Converter.toStr(loginDto.getUserId());
		String m_stbshiptocd = Converter.toStr(params.get("r_stbshiptocd"));
		int inCnt = 0, delCnt = 0;
		
		if(0 < getBookmarkCnt(m_stbuserid, m_stbshiptocd)) {
			svcMap.put("r_stbuserid", m_stbuserid);
			svcMap.put("r_stbshiptocd", m_stbshiptocd);
			delCnt = shipToDao.delBookmark(svcMap); //삭제
			svcMap.clear();
			
		}else {
			svcMap.put("m_stbuserid", m_stbuserid);
			svcMap.put("m_stbshiptocd", m_stbshiptocd);
			svcMap.put("m_stbinid", m_stbuserid);
			inCnt = shipToDao.inBookmark(svcMap); //추가
			svcMap.clear();
		}
		
		resMap.put("delCnt", delCnt);
		resMap.put("inCnt", inCnt);
		
		return resMap;
	}
	
}
