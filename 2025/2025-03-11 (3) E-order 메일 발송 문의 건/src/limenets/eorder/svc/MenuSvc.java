package com.limenets.eorder.svc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeAuthException;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.util.Converter;
import com.limenets.eorder.dao.MenuDao;

/**
 * Admin 메뉴 서비스.
 */
@Service
public class MenuSvc {
	private static final Logger logger = LoggerFactory.getLogger(MenuSvc.class);
	
	@Inject private MenuDao menuDao;
	
	/**
	 * 권한체크
	 * @param url
	 * @param ss_menu
	 * @return
	 * @throws Exception
	 */
	public int getNowMnSeq(String url, Map<String, String> urlParam, String ss_menu) throws Exception {
		Map<String, Object> svcMap = new HashMap<>();
		
		logger.debug("url : {}", url);
		
		svcMap.put("r_muurl", url);
		List<Map<String, Object>> mpList = menuDao.getMenuUrl(svcMap);
		svcMap.clear();
		
		if (CollectionUtils.isEmpty(mpList)) {
			// 2019-07-26. MenuUrl 에 작성하지 않은 것들은 기본적으로 처리되게 한다. 예) BaseCtrl
			return 0;
			// throw new LimeAuthException(MsgCode.PAGE_SET_ERROR);
		} 
		else {
			boolean chk = false;
			for (Map<String, Object> map : mpList) {
				String muAuth = Converter.toStr(map.get("MU_AUTH"));

				int mnSeq = Converter.toInt(map.get("MU_MNSEQ"));
				if (mnSeq == 0) {
					return 0;
				}
					
				String chkTxt1 = "|" + Converter.toStr(mnSeq) + ":" + muAuth + "|";
				String chkTxt2 = "|" + Converter.toStr(mnSeq) + ":W|";
				
				if (ss_menu.indexOf(chkTxt1) > -1) chk = true;
				if ("R".equals(muAuth) && ss_menu.indexOf(chkTxt2) > -1) chk = true;
				
				// Chae. 2019-07-31. 파라미터 체크. url에서 true 되었어도, 파라미터 체크 통과 안되면 다시 false 만듬.
				Map<String, String> dbParam = getMapByParam(Converter.toStr(map.get("MU_PARAM")));
				logger.debug("dbParam : {}", dbParam);
				logger.debug("urlParam : {}", urlParam);
				if (!CollectionUtils.isEmpty(dbParam)) {
					boolean paramChk = checkParam(urlParam, dbParam);
					logger.debug("mnSeq : {}, paramChk : {}", mnSeq, paramChk);
					if (!paramChk) chk = false;
				}
				
				if (chk) {
					return mnSeq;
				}
			}
			
			if (!chk) {
				throw new LimeAuthException(MsgCode.AUTH_DENY_ERROR);
			} else {
				return 0;
			}
		}
	}
	
	private boolean checkParam(Map<String, String> urlParam, Map<String, String> dbParam) {
		for (Entry<String, String> elem : dbParam.entrySet()) {
			if (!StringUtils.equals(urlParam.get(elem.getKey()), elem.getValue())) {	// 디비 파라미터와 url 파라미터가 다르면
				return false;
			}
		}
		return true;
	}
	
	public Map<String, String> getMapByParam(String param) {
		Map<String, String> resMap = new HashMap<>();
		String[] paramArr = param.split("\\&");
		for (String one : paramArr) {
			if (!"".equals(Converter.toStr(one))) {
				String[] oneArr = one.split("\\=", 2);
				resMap.put(oneArr[0], oneArr[1]);
			}
		}
		return resMap;
	}
	
	/**
	 * Get Role Table List.
	 * @작성일 : 2020. 2. 26.
	 * @작성자 : kkyu
	 */
	public List<Map<String, Object>> getRoleList(String rl_viewyn){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_rlviewyn", rl_viewyn);
		return this.getRoleList(svcMap);
	}
	public List<Map<String, Object>> getRoleList(Map<String, Object> svcMap){
		return this.menuDao.getRoleList(svcMap);
	}
	
	
	/**
	 * 권한설정을 위한 메뉴리스트 가져오기.
	 */
	public List<Map<String, Object>> getMenuListForAuthority(Map<String, Object> params) throws Exception{
		int r_rmrlseq = Converter.toInt(params.get("r_rmrlseq"));
		String r_mnuse = Converter.toStr(params.get("r_mnuse"));
		String r_mnbase = Converter.toStr(params.get("r_mnbase"));
		
		params.put("r_rmrlseq", r_rmrlseq);
		params.put("r_mnuse", r_mnuse);
		params.put("r_mnbase", r_mnbase);
		List<Map<String, Object>> rmList = menuDao.getMenuListForAuthority(params); 
		
		for(Map<String, Object> rmMap : rmList){
			if (Converter.toInt(rmMap.get("CHILD_COUNT")) == 0) rmMap.put("IS_LEAF", true);
			else rmMap.put("IS_LEAF", false);
			rmMap.put("IS_EXPAND", true);
		}
		logger.debug("rmList : {}", rmList);
		return rmList;
	}
	
	/**
	 * 접근권한 저장
	 */
	public Map<String, Object> insertRoleMenuTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> svcMap = new HashMap<>();
		
		int r_rmrlseq = Converter.toInt(params.get("r_rmrlseq"));
		String [] mnSeqArr = Converter.toStr(params.get("r_mnseqs")).split(",", -1);
		String [] rmAuthArr = Converter.toStr(params.get("r_rmauths")).split(",", -1);
		logger.debug("r_rmrlseq : {}", r_rmrlseq);
		
		if (0 == r_rmrlseq) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR, "권한고유번호");
		
		// Deleta RoleMenu.
		svcMap.put("r_rmrlseq", r_rmrlseq);
		menuDao.delRoleMenu(svcMap);
		svcMap.clear();
		
		// Insert RoleMenu.
		for (int i=0; i<mnSeqArr.length; i++) {
			if (!"".equals(rmAuthArr[i])) {
				svcMap.put("m_rmrlseq", r_rmrlseq);
				svcMap.put("m_rmmnseq", mnSeqArr[i]);
				svcMap.put("m_rmauth", rmAuthArr[i]);
				menuDao.inRoleMenu(svcMap);
				svcMap.clear();
			}
		}
		
		// Insert RoleMenu For MB_BASE=Y
		svcMap.put("m_rmrlseq", r_rmrlseq);
		menuDao.inRoleMenuBase(svcMap);
		svcMap.clear();

		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
}
