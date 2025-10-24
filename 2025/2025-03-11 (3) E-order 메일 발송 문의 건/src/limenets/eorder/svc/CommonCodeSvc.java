package com.limenets.eorder.svc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.eorder.dao.CommonCodeDao;

@Service
public class CommonCodeSvc {
	private static final Logger logger = LoggerFactory.getLogger(CommonCodeSvc.class);
	
	@Inject private CommonCodeDao commonCodeDao;
	
	public Map<String, Object> getOne(String cc_code) {
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_cccode", cc_code);
		return commonCodeDao.one(svcMap);
	}
	
	public List<Map<String, Object>> getList(String cc_parent, String cc_use){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_ccparent", cc_parent);
		svcMap.put("r_ccuse", cc_use);
		//svcMap.put("r_orderby", "CC_SORT1 ASC, CC_SORT2 ASC, CC_SORT3 ASC");
		return this.getList(svcMap);
	}
	public List<Map<String, Object>> getList(Map<String, Object> params) {
		return commonCodeDao.list(params);
	}
	
	public List<Map<String, Object>> getTreeList() {
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_ccuse", "Y");
		List<Map<String, Object>> ccList = commonCodeDao.list(svcMap);
		svcMap.clear();
		
		List<Map<String, Object>> resList = new ArrayList<>();
		for(Map<String, Object> ccMap : ccList) {
			// 최상위 노드만 보이게 하드코딩.
			if (Converter.toInt(ccMap.get("CC_DEPTH")) == 1) {
				ccMap.put("EXPANDED", true);
				ccMap.put("ISLEAF", true);
				resList.add(ccMap);
			}
		}
		
		logger.debug("resList : {}", resList);
		return resList;
	}

	public List<Map<String, Object>> getDetailList(String cc_parent, String cc_use) {
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_ccparent", cc_parent);
		svcMap.put("r_ccuse", cc_use);
		return this.getDetailList(svcMap);
	}

	public List<Map<String, Object>> getDetailList(String cc_parent) {
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_ccparent", cc_parent);
		return this.getDetailList(svcMap);
	}

	public List<Map<String, Object>> getDetailList(Map<String, Object> svcMap) {
		return commonCodeDao.list(svcMap);
	}
	
	public List<Map<String, Object>> getCategoryListWithDepth(Map<String, Object> svcMap) {
		List<Map<String, Object>> ret = commonCodeDao.getCategoryListWithDepth(svcMap);
		return ret;
	}
	
	/**
	 * TreeGrid 리스트 데이터 저장.
	 * @작성일 : 2020. 3. 6.
	 * @작성자 : kkyu
	 */
	public Object insertUpdateCommonRootCodeTransaction(Map<String, Object> params, HttpServletRequest req) {
		Map<String, Object> svcMap = new HashMap<>();
		
		String[] r_cccode = req.getParameterValues("r_cccode");
		String[] m_ccname = req.getParameterValues("m_ccname");
		
		for (int i=0; i<r_cccode.length; i++) {
			if (StringUtils.equals("", r_cccode[i])) {	// 저장.
				svcMap.put("r_ccdepth", 1);
				svcMap.put("r_ccparent", "");
				int newNum = commonCodeDao.maxCode(svcMap);
				svcMap.clear();
				
				String m_cccode = "C" + Converter.zeroPlus(newNum, 2);
				
				// 정렬값 구함.
				svcMap.put("r_ccdepth", 1);
				svcMap.put("r_ccparent", "");
				int newSort = commonCodeDao.maxSort(svcMap);
				svcMap.clear();

				svcMap.put("m_cccode", m_cccode);
				svcMap.put("m_ccname", m_ccname[i]);
				svcMap.put("m_ccparent", "");
				svcMap.put("m_ccdepth", 1);
				svcMap.put("m_ccsort1", newSort);
				svcMap.put("m_ccsort2", 0);
				svcMap.put("m_ccsort3", 0);
				svcMap.put("m_ccuse", "Y");
				commonCodeDao.in(svcMap);
				svcMap.clear();
			} else {		// 수정
				svcMap.put("r_cccode", r_cccode[i]);
				svcMap.put("m_ccname", m_ccname[i]);
				commonCodeDao.up(svcMap);
				svcMap.clear();
			}
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 공통코드 상세 저장/수정.
	 * @작성일 : 2020. 3. 6.
	 * @작성자 : kkyu
	 */
	public Object inserUpdateCommonCodeTransaction(Map<String, Object> params, HttpServletRequest req) {
		Map<String, Object> svcMap = new HashMap<>();
		
		String m_ccuseyn = Converter.toStr(params.get("m_ccuseyn"));

		String[] r_cccode = req.getParameterValues("r_cccode");
		String[] m_ccname = req.getParameterValues("m_ccname");
		String[] m_ccuse = req.getParameterValues("m_ccuse");
		
		if (!StringUtils.equals("", m_ccuseyn)) { // 사용여부만 업데이트
			svcMap.put("ri_cccode", r_cccode);
			svcMap.put("m_ccuse", m_ccuseyn);
			commonCodeDao.upArr(svcMap);
			svcMap.clear();
		} else { // 저장/수정 처리
			String m_ccparent = Converter.toStr(params.get("m_ccparent"));
			Map<String, Object> ccMap = this.getOne(m_ccparent);

			int depth = Converter.toInt(ccMap.get("CC_DEPTH"));
			int sort1 = Converter.toInt(ccMap.get("CC_SORT1"));
			int sort2 = Converter.toInt(ccMap.get("CC_SORT2"));
			int sort3 = Converter.toInt(ccMap.get("CC_SORT3"));
			
			for (int i=0; i<r_cccode.length; i++) {
				if (StringUtils.equals("", r_cccode[i])) { // 저장.
					// 신규코드 생성. 최상위 생성은 따로 만들어야 함.
					svcMap.put("r_ccdepth", depth+1);
					svcMap.put("r_ccparent", m_ccparent);
					// C01=반려사유 같은경우 AS_IS O_CUST_ORDER_H.RETURN_CD로 00,01,02,03 값으로 사용중
					int newNum = (StringUtils.equals("C01", m_ccparent)) ? commonCodeDao.maxCodeForC01(svcMap) : commonCodeDao.maxCode(svcMap); 
					svcMap.clear();
					
					String m_cccode = (StringUtils.equals("C01", m_ccparent)) ? Converter.zeroPlus(newNum, 2) : m_ccparent + Converter.zeroPlus(newNum, 3);
					
					// 정렬값 구함.
					svcMap.put("r_ccdepth", depth+1);
					svcMap.put("r_ccparent", m_ccparent);
					int newSort = commonCodeDao.maxSort(svcMap);
					svcMap.clear();

					if (depth == 1) { // 2단계 추가시.
						sort2 = newSort;
					} else if (depth == 2) { // 3단계 추가시.
						sort3 = newSort;
					}
					
					svcMap.put("m_cccode", m_cccode);
					svcMap.put("m_ccname", m_ccname[i]);
					svcMap.put("m_ccparent", m_ccparent);
					svcMap.put("m_ccdepth", depth+1);
					svcMap.put("m_ccsort1", sort1);
					svcMap.put("m_ccsort2", sort2);
					svcMap.put("m_ccsort3", sort3);
					svcMap.put("m_ccuse", m_ccuse[i]);
					commonCodeDao.in(svcMap);
					svcMap.clear();
				} else { // 수정.
					svcMap.put("r_cccode", r_cccode[i]);
					svcMap.put("m_ccname", m_ccname[i]);
					svcMap.put("m_ccuse", m_ccuse[i]);
					commonCodeDao.up(svcMap);
					svcMap.clear();
				}
			}
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 공통코드 상세 로우 순서 변경.
	 * @작성일 : 2020. 3. 7.
	 * @작성자 : kkyu
	 */
	public Object updateCommonCodeSortTransaction(Map<String, Object> params, HttpServletRequest req) throws Exception{
		String[] ri_cccode = req.getParameterValues("r_cccode");
		String[] mi_ccsort2 = req.getParameterValues("m_ccsort2");
		if(ArrayUtils.isEmpty(ri_cccode) || ArrayUtils.isEmpty(mi_ccsort2) || ri_cccode.length != mi_ccsort2.length) {
			throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		}
		
		for(int i=0,j=ri_cccode.length; i<j; i++) {
			params.put("r_cccode", ri_cccode[i]);
			params.put("m_ccsort2", mi_ccsort2[i]);
			commonCodeDao.up(params);
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
}
