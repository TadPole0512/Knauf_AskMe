package com.limenets.eorder.svc;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.listener.SessionListener;
import com.limenets.common.resolver.LimeLoginArgResolver;
import com.limenets.common.util.Converter;
import com.limenets.common.util.FileUpload;
import com.limenets.common.util.HttpUtils;
import com.limenets.common.util.MailUtil;
import com.limenets.common.util.Pager;
import com.limenets.eorder.dao.CsSalesMapDao;
import com.limenets.eorder.dao.LoginAuthDao;
import com.limenets.eorder.dao.MenuDao;
import com.limenets.eorder.dao.ShipToDao;
import com.limenets.eorder.dao.UserDao;
import com.limenets.eorder.dao.UserPswdHistoryDao;

/**
 * 유저 서비스.
 */
@Service
public class UserSvc {
	private static final Logger logger = LoggerFactory.getLogger(UserSvc.class);
	
	@Resource(name = "fileUpload") private FileUpload fileUpload;
	
	@Value("${https.url}") private String httpsUrl;
	
	@Inject private ConfigSvc configSvc;
	@Inject private JwtSvc jwtSvc;
	@Inject private TempleteSvc templeteSvc;
	
	@Inject private CsSalesMapDao csSalesMapDao;
	@Inject private LoginAuthDao loginAuthDao;
	@Inject private MenuDao menuDao;
	@Inject private UserDao userDao;
	@Inject private UserPswdHistoryDao userPswdHistoryDao;
	@Inject private ShipToDao shipToDao;
	
	@Value("${mail.smtp.url}") private String smtpHost;
	@Value("${mail.smtp.sender.addr}") private String smtpSender;
	@Value("${shop.name}") private String shopName;
	
	public Map<String, Object> getUserOne(String userid){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_userid", userid);
		return this.getUserOne(svcMap);
	}
	public Map<String, Object> getUserOne(Map<String, Object> svcMap){
		return userDao.one(svcMap);
	}
	
	public Map<String, Object> getUser(String r_userid, String r_userpwd) {
		Map<String, Object> svcMap = new HashMap<String, Object>();
		svcMap.put("r_userid", r_userid);
		svcMap.put("r_userpwd", r_userpwd);
		return this.getUserOne(svcMap);
	}
	
	/**
	 * AND USERID NOT IN ('0', '1','2','3','4') => 이 조건 없이 모든 데이터 조회용.
	 */
	public Map<String, Object> getUserOneAll(String userid){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_userid", userid);
		return this.getUserOneAll(svcMap);
	}
	public Map<String, Object> getUserOneAll(Map<String, Object> svcMap){
		return userDao.oneAll(svcMap);
	}
	
	public int getUserCntAll(String userid) {
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_userid", userid);
		return this.getUserCntAll(svcMap);
	}
	public int getUserCntAll(Map<String, Object> svcMap) {
		return userDao.cntAll(svcMap);
	}
	
	public List<Map<String, Object>> getUserList(String authority, String order_by, int start_row, int end_row){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_authority", authority);
		svcMap.put("r_orderby", (StringUtils.equals("", order_by)) ? "" : order_by);
		svcMap.put("r_startrow", start_row);
		svcMap.put("r_endrow", end_row);
		return this.getUserList(svcMap);
	}
	public List<Map<String, Object>> getUserList(Map<String, Object> svcMap){
		return userDao.list(svcMap);
	}
	
	public int getUserSortMax(String user_parent, String max_column) {
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_userparent", user_parent);
		svcMap.put("r_maxcolumn", max_column);
		return this.getUserSortMax(svcMap);
	}
	public int getUserSortMax(Map<String, Object> svcMap) {
		return userDao.getUserSortMax(svcMap);
	}
	
	public Map<String, Object> getCsSalesMapOne(String csuserid, String fixedyn){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_csuserid", csuserid);
		svcMap.put("r_fixedyn", fixedyn);
		return this.getCsSalesMapOne(svcMap);
	}
	public Map<String, Object> getCsSalesMapOne(Map<String, Object> svcMap){
		return csSalesMapDao.one(svcMap);
	}
	
	public Map<String, Object> getUserMap(String r_userid){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_userid", r_userid);
		return userDao.indexCntMap(svcMap);
	}
	
	/**
	 * 자동 로그인 쿠키 삭제.
	 * @작성일 : 2020. 7. 7.
	 * @작성자 : kkyu
	 */
	public void deleteAutoLoginCookie(HttpServletResponse res) {
		Cookie cookie = new Cookie("auth_token", null);
		cookie.setMaxAge(0);
		res.addCookie(cookie);
	}
	
	/**
	 * 로그인 처리.
	 * @작성일 : 2020. 2. 21.
	 * @작성자 : kkyu
	 * @param loginToken 
	 * @param  
	 * @return
	 */
	public Map<String, Object> doLogin(Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, HttpSession session) throws Exception {
		Map<String, Object> svcMap = new HashMap<>();

		String r_userid = Converter.toStr(params.get("r_userid"));
		String pageType = Converter.toStr(params.get("pageType"));
		String r_autologinyn = Converter.toStr(params.get("r_autologinyn"));
		String where = Converter.toStr(params.get("where")); // admin / front / appajax=앱 로그인페이지에서 넘어온경우 from Ajax. / appdirect=앱에서 자동로그인인경우로 auth_token 쿠키가 있을때 바로 호출한경우.
		logger.debug("params : {}", params);

		String auth_token = "";
		if(StringUtils.equals("appajax", where) || StringUtils.equals("appdirect", where)) {
			////logger.debug("####### 자동로그인 처리 시작 where : {}", where);
			// ####### Start. 자동 로그인 처리 #######
			// 1. 자동 로그인 요청 인지 확인.
			Cookie [] cookies = req.getCookies();
			if(!ArrayUtils.isEmpty(cookies)) {
				for(Cookie c : cookies) {
					if(StringUtils.equals("auth_token", c.getName())) auth_token = c.getValue();
				}
				////logger.debug("####### 1. 자동 로그인 요청 인지 확인 auth_token : {}", auth_token);
			}
			// 2. 최초 자동 로그인 요청.
			if(StringUtils.equals("", auth_token) && StringUtils.equals("Y", r_autologinyn)) { 
				// 토큰을 생성.
				auth_token = this.makeAutoLoginByJwt(req, r_userid);
				
				// DB에 토큰 저장.
				params.put("r_lauserid", r_userid);
				params.put("m_latoken", auth_token);
				loginAuthDao.merge(params);
				
				// 쿠키에 저장.
		        Cookie cookie = new Cookie("auth_token", auth_token);
		        cookie.setMaxAge(60*60*24*7); // 7일간 저장.
		        res.addCookie(cookie);
		        
		        ////logger.debug("####### 2. 최초 자동 로그인 요청 auth_token : {}", auth_token);
			}
			// 3. 최초가 아니고, 빈값이 아니면 자동 로그인 요청.
			if(!StringUtils.equals("", auth_token)) { 
				////logger.debug("####### 3. 최초가 아니고, 빈값이 아니면 자동 로그인 요청 : {}", auth_token);
				
				// 쿠키에 저장된 토큰값을 파싱해서 MB_ID를 가지고 온다.
				Map<String, Object> auth = jwtSvc.parseJWT(req, auth_token);
				String userIdByTokenKey = Converter.toStr(auth.get("key"));
				String token_check = Converter.toStr(auth.get("token_check"));
				
				// 토큰이 만료된 경우. => 재발급.
				if(StringUtils.equals("EXPIRE", token_check)) {
					// 토큰을 생성.
					auth_token = this.makeAutoLoginByJwt(req, r_userid);
					
					// DB에 토큰 저장.
					params.put("r_lauserid", r_userid);
					params.put("m_latoken", auth_token);
					loginAuthDao.merge(params);
					
					// 쿠키에 저장.
			        Cookie cookie = new Cookie("auth_token", auth_token);
			        cookie.setMaxAge(60*60*24*7); // 7일간 저장.
			        res.addCookie(cookie);
				}
				
				// 토큰이 변조된 경우.
				if(StringUtils.equals("FALSIFY", token_check)) {
					session.invalidate();
					
					if(StringUtils.equals("appdirect", where)) {
						this.deleteAutoLoginCookie(res);
						return MsgCode.getResultMap(MsgCode.LOGIN_TOKEN_FALSIFY);
					}
					
					throw new LimeBizException(MsgCode.LOGIN_TOKEN_FALSIFY);
				}
				
				// 토큰이 정상인 경우. DB에 저장된 토큰값과 다르면 토큰 쿠키 삭제 및 세션 무효화.
				Map<String, Object> loginAutoAuth = this.getLoginAuth(userIdByTokenKey, auth_token);
				if(CollectionUtils.isEmpty(loginAutoAuth)) {
					this.deleteAutoLoginCookie(res);
					
					if(session != null && !req.isRequestedSessionIdValid()) {
						////logger.debug("토큰이 정상인 경우. DB에 저장된 토큰값과 다르면 토큰 쿠키 삭제 및 세션 무효화. : {}", r_userid);
						session.invalidate();
						return MsgCode.getResultMap(MsgCode.LOGIN_TOKEN_ERROR);
					}
					//throw new LimeBizException(MsgCode.LOGIN_TOKEN_ERROR);
				}
				
				// DB에 저장된 토큰값과 같으면 새로 토큰을 생성한후 DB에 저장한다.
				auth_token = this.makeAutoLoginByJwt(req, userIdByTokenKey);
				params.put("r_lauserid", userIdByTokenKey);
				params.put("m_latoken", auth_token);
				loginAuthDao.merge(params);
				
				// 새로 생성된 토큰값을 쿠키에도 새로 생성한다.
				Cookie cookie1 = new Cookie("auth_token", auth_token);
				cookie1.setMaxAge(60*60*24*7); // 7일간 저장.
				res.addCookie(cookie1);
				
				r_userid = userIdByTokenKey;
				params.put("r_userid", userIdByTokenKey);
			}
			// ####### END. 자동 로그인 처리 #######
		}
		
		// 토큰 체크
		String loginToken = Converter.toStr(params.get("loginToken"));
		String prevLoginToken = Converter.toStr(session.getAttribute("loginToken"));
		
		// 자동 로그인이 아닌 경우, 입력방식 로그인 토큰 체크.
        
        if(StringUtils.equals("", auth_token)) { 
            if(!StringUtils.equals(loginToken, prevLoginToken)) { 
                throw new LimeBizException(MsgCode.LOGIN_TOKEN_ERROR); 
            } 
        }
        
		
		/*
		1. 토큰체크
		2. 로그인
		 *2-1. 로그인 시도 횟수 초과시 계정잠금 ==> loginLog
		 **2-2. 비밀번호 변경시 잠금 해제 (실패한 이력중 가장 최근 날짜보다 비밀번호 변경일이 클때) ==> loginLog 실패한 가장 최근 내역 / userPasswordHistory 자장 최근 변경 내역
		 *
		3. 계정체크
		4. 로그인 로그 저장
		 
		 ========================================================================== 위 전 / 아래 후
		 
		5. 세션저장
		6. 최초 로그인 체크
		 6-1. 로그인 로그 0인 최초 로그인일때 마이페이지 ==> loginLog
		 6-2. 관리자가 비밀번호 변경 후 로그인일때 비밀번호 변경 ==> userPasswordHistory
		 6-3. 관리자가 비밀번호 초기화 후 로그인일때 비밀번호 변경 ==> userPasswordHistory
		7. 비밀번호 변경요청 팝업(권장)
		 7-1. 내부사용자 팝업 사용체크 ==> config
		 7-2. 비밀번호 변경 후 3개월 이후 팝업출력 ==> userPasswordHistory
		      [다음에하기] 이력이 있다면 3개월 후 팝업출력
		      
		      case1. 1/1 => 4/1 로그인 (권장팝업) => 다음에 하기(3개월) => 7/1 권장 팝업 뜨기시작
		      case2. 1/1 => 5/1 로그인 (권장팝업) => 다음에 하기(3개월) => 8/1 권장 팝업 뜨기시작
		      
		8. 최근 비밀번호 변경 불가 처리
			
		*/
		
		// 가장 최근 비밀번호 변경 이력
		Map<String, Object> hMap = this.getLastHistory(r_userid);
		
		int userPswdLock = Converter.toInt((configSvc.getConfigValue("USERPSWDLOCK"))); //로그인시도 허용횟수 
		int failCnt = this.getLoginFailCnt(hMap, r_userid); //로그인 실패횟수
		
		// 로그인시도 횟수 초과시 계정 잠금
		if(userPswdLock <= failCnt) {
			if(StringUtils.equals("appdirect", where)) {
				this.deleteAutoLoginCookie(res);
				return MsgCode.getResultMap(MsgCode.DATA_LOCK_ERROR);
			}
			else if(StringUtils.equals("appajax", where)) {
				this.deleteAutoLoginCookie(res);
			}
			
			throw new LimeBizException(MsgCode.DATA_LOCK_ERROR); // 0314=로그인시도 횟수가 초과되어 계정이 잠금 처리되었습니다.
		}

		// 계정 체크
		//params.put("r_useruse", "Y");
		Map<String, Object> userMap = this.getUserOne(params);

		if(!CollectionUtils.isEmpty(userMap) && StringUtils.equals("N", Converter.toStr(userMap.get("USER_USE")))) {
			if(StringUtils.equals("appdirect", where)) {
				this.deleteAutoLoginCookie(res);
				return MsgCode.getResultMap(MsgCode.DATA_NOT_USE);
			}
			else if(StringUtils.equals("appajax", where)) {
				this.deleteAutoLoginCookie(res);
			}
			
			throw new LimeBizException(MsgCode.DATA_NOT_USE); //비활성계정 0319=해당계정은 비활성화된 계정입니다.
		}
		
		// 로그인 로그 저장
		String m_llpsyn = "Y";
		if(CollectionUtils.isEmpty(userMap)) m_llpsyn = "N";
		
		svcMap.put("m_lluserid", r_userid);
		svcMap.put("m_llpsyn", m_llpsyn);
		svcMap.put("m_lltype", pageType);
		userDao.inLoginLog(svcMap);
		svcMap.clear();

		// 로그인 실패시
		if(CollectionUtils.isEmpty(userMap)) {
			if(failCnt > 1) {
				if(StringUtils.equals("appdirect", where)) {
					this.deleteAutoLoginCookie(res);
					return MsgCode.getResultMap(MsgCode.ACCOUNT_WARNING_ERROR);
				}
				else if(StringUtils.equals("appajax", where)) {
					this.deleteAutoLoginCookie(res);
				}
				
				throw new LimeBizException(MsgCode.ACCOUNT_WARNING_ERROR, userPswdLock); // 0107=계정정보가 일치하지 않습니다.\n%s회이상 로그인 정보가 일치하지 않을 경우 자동 잠금 처리됩니다.
			}
			else {
				if(StringUtils.equals("appdirect", where)) {
					this.deleteAutoLoginCookie(res);
					return MsgCode.getResultMap(MsgCode.ACCOUNT_ERROR);
				}
				else if(StringUtils.equals("appajax", where)) {
					this.deleteAutoLoginCookie(res);
				}
				
				throw new LimeBizException(MsgCode.ACCOUNT_ERROR); // 0100=계정정보가 일치하지 않습니다.
			}
		}
		
		
		String userId = Converter.toStr(userMap.get("USERID"));
		String authority = Converter.toStr(userMap.get("AUTHORITY")); // 내부사용자 : AD,SH,SM,SR,CS,MK / 외부사용자 : CO,CT / 내부 시스템 관리자 : AS
		//String passChangeYn = Converter.toStr(userMap.get("PASSCHANGE_YN")); // N:초기비밀번호
		String custNm = Converter.toStr(userMap.get("CUST_NM"));
		String custCd = Converter.toStr(userMap.get("CUST_CD"));
		
		// 최초 로그인 체크
		String firstLoginYN = "N";
		if(1 == this.getLoginLogCnt(r_userid, "Y") || "".equals(Converter.toStr(userMap.get("CELL_NO"))) || "".equals(Converter.toStr(userMap.get("TEL_NO")))|| "".equals(Converter.toStr(userMap.get("USER_EMAIL"))) ) {
			firstLoginYN = "Y";
		}
		
		if(StringUtils.equals("CO", authority) || StringUtils.equals("CT", authority)) { //외부사용자는 동의여부도 체크
			if(!"Y".equals(Converter.toStr(userMap.get("USER_AGREE")))){
				firstLoginYN = "Y";
			}
		}
		
		// 권한 설정 : 내부 사용자만 사용.
		StringBuilder sMenu = new StringBuilder("|");
		if(!StringUtils.equals("CO", authority) && !StringUtils.equals("CT", authority)) {
			List<Map<String, Object>> rmList = null;
			
			if(StringUtils.equals("AS", authority)){ // 시스템관리자인경우
				rmList = menuDao.roleMenuListForLoginBySystemAdmin(svcMap);
			}else {
				svcMap.put("r_rlcode", authority);
				rmList = menuDao.getRoleMenuListForLogin(svcMap);
			}
			svcMap.clear();
			 
			for(Map<String, Object> map :rmList){ 
				if(StringUtils.isNotEmpty(Converter.toStr(map.get("RM_AUTH")))){
					sMenu.append(Converter.toStr(map.get("RM_MNSEQ")))
						.append(':')
						.append(Converter.toStr(map.get("RM_AUTH")))
						.append('|'); 
				} 
			}
			logger.debug("sMenu : {}", sMenu);
		}
		
		// 이미 접속한 아이디인지 체크.
		SessionListener.getInstance().printloginUsers(); // 현재 접속한 사람들 출력.
		String setSessionUserId = userId;
		if(StringUtils.equals("appdirect", where) || StringUtils.equals("appajax", where)) {
			setSessionUserId += "_app";
		}
		if(SessionListener.getInstance().isUsing(setSessionUserId)) {
			//logger.debug("이미 아이디가 접속중 입니다.");
			if(StringUtils.equals("appdirect", where)) {
				//this.deleteAutoLoginCookie(res);
				// 앱에서 auth_token 값을 가지고 바로 호출한 경우는 강제로 기존 연결을 종료 시킨다.
				SessionListener.getInstance().removeSession(setSessionUserId); // 기존 접속한 유저 아이디 세션 종료.
				session = req.getSession();
			}
			else if(StringUtils.equals("appajax", where)) {
				//this.deleteAutoLoginCookie(res);
				// 강제로 기존 연결을 종료 시킨다.
				if(!StringUtils.equals("", auth_token)) {
					SessionListener.getInstance().removeSession(setSessionUserId); // 기존 접속한 유저 아이디 세션 종료.
					session = req.getSession();
				}
				else {
					throw new LimeBizException(MsgCode.ACCOUNT_READY_ERROR);
				}
			}
			else {
				throw new LimeBizException(MsgCode.ACCOUNT_READY_ERROR); // 0106=이미 아이디가 접속중 입니다.
			}
		}

		// 접속 기기(모바일/태블릿/데스크탑) 가져오기.
		Device device = DeviceUtils.getCurrentDevice(req);
		String accessDevice = "pc";
		if(device.isMobile() || device.isTablet()) accessDevice = "mobile";
		
		// 세션설정.
		LoginDto loginDto = new LoginDto();
		loginDto.setUserId(userId);
		loginDto.setUserNm(Converter.toStr(userMap.get("USER_NM")));
		loginDto.setAuthority(authority);
		loginDto.setUserFile(Converter.toStr(userMap.get("USER_FILE")));
		loginDto.setCustNm(custNm);
		loginDto.setCustCd(custCd);
		loginDto.setShiptoNm(Converter.toStr(userMap.get("SHIPTO_NM")));
		loginDto.setShiptoCd(Converter.toStr(userMap.get("SHIPTO_CD")));
		loginDto.setUserEmail(Converter.toStr(userMap.get("USER_EMAIL")));
		
		loginDto.setUserPushId(Converter.toStr(userMap.get("USER_APPPUSHKEY")));
		
		loginDto.setMenu(sMenu.toString());
		loginDto.setRemoteIp(HttpUtils.getRemoteAddr(req));
		loginDto.setSessId(session.getId());
		loginDto.setLoginToken(Converter.toStr(params.get("loginToken")));
		loginDto.setRoleCode(authority);
		
		loginDto.setAccessDevice(accessDevice);
		loginDto.setFirstLogin(firstLoginYN); 
		
		session.setAttribute(LimeLoginArgResolver.SESSION_ATTRIBUTE, loginDto);
		
		SessionListener.getInstance().setSession(session, setSessionUserId); // 세션 리스너에 회원아이디 담디.
		SessionListener.getInstance().printloginUsers(); // 현재 접속한 사람들 출력.

		//환경설정(브라우저타이틀)
		String browserTitle = configSvc.getConfigValue("BROWSERTITLE");
		session.setAttribute("BROWSERTITLE", browserTitle);
		
		// CS > O_CSSALESMAP 임시저장된 영업사원 삭제.
		if(StringUtils.equals("CS", authority)) {
			params.put("r_csuserid", userId);
			params.put("r_fixedyn", "N");
			csSalesMapDao.del(params);
		}
		
		// 최초 로그인시 개인정보수정 페이지로 이동
		if(StringUtils.equals("Y", firstLoginYN)) {
			return MsgCode.getResultMap(MsgCode.DATA_FIRST_LOGIN); // 0316=개인정보 입력후 이용해 주시기 바랍니다.
		}
		
		// 관리자가 비밀번호 변경 후 로그인일때 비밀번호 변경, 관리자가 비밀번호 초기화 후 로그인일때 비밀번호 변경
		// (10:회원가입,20:사용자 직접변경,30:관리자 변경,40:변경요청페이지에서직접변경,41:변경요청페이지에서변경연장,50:비밀번호찾기변경,60:비밀번호초기화)
		String[] ri_uphcode = {"30","50","60"};
		if(!CollectionUtils.isEmpty(hMap)) {
			boolean userPassChk = Arrays.asList(ri_uphcode).contains(Converter.toStr(hMap.get("UPH_CODE")));
			if(userPassChk) {
				return MsgCode.getResultMap(MsgCode.DATA_PSWD_CHANGE); // 0317=임시 비밀번호 입니다.
			}
		}
		
		// 비밀번호 변경요청(M)
		int userPswdMonth = Converter.toInt(configSvc.getConfigValue("USERPSWDMONTH"));   
		
		// 비밀번호 변경요청일이 지난 경우.
		if(0 < this.checkCntForUserPswd(hMap, userId, userPswdMonth)){
			if(!StringUtils.equals("CO", authority) && !StringUtils.equals("CT", authority)) {	  // 내부 사용자일때
				String userPswdMonthAdminUse = configSvc.getConfigValue("USERPSWDMONTHADMINUSE"); // 내부 사용자 비밀번호 변경요청(M) 사용여부 Y/N
				if("Y".equals(userPswdMonthAdminUse)) {
					return MsgCode.getResultMap(MsgCode.DATA_CHANGE_ERROR, userPswdMonth); // 0312=비밀번호가 %s개월이상 변경되지 않았습니다.
				}
				
			}else {
				return MsgCode.getResultMap(MsgCode.DATA_CHANGE_ERROR, userPswdMonth); // 0312=비밀번호가 %s개월이상 변경되지 않았습니다.
			}
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 비밀번호 변경요청을 위한 체크.
	 * @작성일 : 2020. 3. 24.
	 * @작성자 : an
	 * @return 0 이상이면 비밀번호 변경요청 페이지로 이동.
	 */
	public int checkCntForUserPswd(Map<String, Object> hMap, String uph_id, int month){
		Map<String, Object> svcMap = new HashMap<String, Object>();
		
		int chkCnt = 0;
		if(!CollectionUtils.isEmpty(hMap)) {
			String uphCode = Converter.toStr(hMap.get("UPH_CODE"));
			int userPswdMonth = month * -1;
			
			// 마지막 저장된 코드가 41(다음에하기)일 경우 ?개월 후 다시출력
			if(StringUtils.equals("41", uphCode)) {
				int userPswdNext = Converter.toInt((configSvc.getConfigValue("USERPSWDNEXT")));
				userPswdMonth = userPswdNext * -1;
			}
			
			svcMap.put("r_uphseq", Converter.toStr(hMap.get("UPH_SEQ")));
			svcMap.put("r_userpswdmonth", userPswdMonth);
			chkCnt = userPswdHistoryDao.cntForUpdateCheck(svcMap);
		}
		
		return chkCnt;
	}
	
	
	/**
	 * 최근 비밀번호 변경내역 가져오기
	 */
	public Map<String, Object> getLastHistory(String uph_id){
		Map<String, Object> svcMap = new HashMap<String, Object>();
		svcMap.put("r_uphid", uph_id);
		Map<String, Object> lastMap = userPswdHistoryDao.getLastHistory(svcMap);
		
		return lastMap;
	}
	
	/**
	 * 최근 로그인 실패내역 가져오기
	 * @throws Exception 
	 */
	public Map<String, Object> getFailLoginLog(String r_lluserid, String r_llindate) throws Exception{
		Map<String, Object> svcMap = new HashMap<String, Object>();
		svcMap.put("r_lluserid", r_lluserid);
		svcMap.put("r_llindate", r_llindate);
		Map<String, Object> logMap = userDao.checkForLock(svcMap);
		return logMap;
	}
	
	/**
	 * 로그인 로그 카운트
	 */
	public int getLoginLogCnt(String userid, String r_llpsyn) {
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_userid", userid);
		svcMap.put("r_llpsyn", r_llpsyn);
		return userDao.loginCnt(svcMap);
	}
	
	/**
	 * 비밀번호 [?]회 이상 틀린경우 계정 잠금
	 * @throws Exception 
	 * @작성일 : 2020. 3. 27.
	 * @작성자 : an
	 */
	public int getLoginFailCnt(Map<String, Object> hMap, String r_userid) throws Exception{
		
		// 최근 비밀번호 변경내역
		String m_uphindate = "";
		if(!CollectionUtils.isEmpty(hMap)) {
			m_uphindate = Converter.toStr(hMap.get("UPH_INDATE")).substring(0, 19);
		}
		
		// 로그인 실패내역 
		int failCnt = 0;
		Map<String, Object> loginMap = this.getFailLoginLog(r_userid, m_uphindate); 
		if(!CollectionUtils.isEmpty(loginMap)) {
			failCnt = Converter.toInt(loginMap.get("FAIL_CNT"));
			logger.debug("failCnt : " + failCnt);
		}
		
		return failCnt;
	}
	
	
	/**
	 * 내부사용자 권한별 리스트 가져오기.
	 * @작성일 : 2020. 2. 27.
	 * @작성자 : kkyu
	 */
	public List<Map<String, Object>> getAdminUserAuthorityList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		String ri_authority = Converter.toStr(params.get("ri_authority"));
		
		if(StringUtils.equals("", ri_authority)) {
			ri_authority = "AD,SH,SM,SR,CS,MK,QM,CI";
		}
		params.put("ri_authority", ri_authority.split(",", -1));
		List<Map<String, Object>> list = userDao.getUserAuthorityList(params);
		
		for(Map<String, Object> one : list){
			int CHILD_COUNT = Converter.toInt(one.get("CHILD_COUNT"));
			
			if (0 == CHILD_COUNT) one.put("IS_LEAF", true);
			else one.put("IS_LEAF", false);
			one.put("IS_EXPAND", true);
		}
		//logger.debug("userAuthorityList : {}", list);
		
		return list;
	}
	
	public List<Map<String, Object>> getAdminUserAuthorityTempList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		String ri_authority = "SH,SM,SR";
		params.put("ri_authority", ri_authority.split(",", -1));
		List<Map<String, Object>> list = userDao.getUser2AuthorityList(params);
		
		for(Map<String, Object> one : list){
			int CHILD_COUNT = Converter.toInt(one.get("CHILD_COUNT"));
			
			if (0 == CHILD_COUNT) one.put("IS_LEAF", true);
			else one.put("IS_LEAF", false);
			one.put("IS_EXPAND", true);
		}
		//logger.debug("userAuthorityList : {}", list);
		
		return list;
	}
	
	/**
	 * 사용자(내부 및 외부 사용자) 리스트 가져오기.
	 * @작성일 : 2020. 3. 2.
	 * @작성자 : kkyu
	 * @param r_gridselectyn : Y=내부사용자 > 시스템  > 사용자현황 페이지에서 trgrid를 선택했을때. /admin/system/adminUserConfig.jsp.
	 */
	public Map<String, Object> getUserList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
//		Map<String, Object> svcMap = new HashMap<>();

		String r_gridselectyn = Converter.toStr(params.get("r_gridselectyn"));
		String r_authority = Converter.toStr(params.get("r_authority"));
		String r_userid = Converter.toStr(params.get("r_userid"));
		String r_userdepth = Converter.toStr(params.get("r_userdepth"));
		
		String rl_userid = Converter.toStr(params.get("rl_userid")); // 검색조건.
		String rl_usernm = Converter.toStr(params.get("rl_usernm")); // 검색조건.
		String ri_userusearr = Converter.toStr(params.get("ri_userusearr")); // 검색조건.
		logger.debug("r_gridselectyn : {}", r_gridselectyn);
		logger.debug("r_authority : {}", r_authority);
		logger.debug("r_userid : {}", r_userid);
		logger.debug("r_userdepth : {}", r_userdepth);
		logger.debug("rl_userid : {}", rl_userid);
		logger.debug("rl_usernm : {}", rl_usernm);
		
		// Define Paramter Only for ExcelDown.
		if(StringUtils.equals("", r_gridselectyn)) r_gridselectyn = Converter.toStr(params.get("excel_gridselectyn")); 
		if(StringUtils.equals("", r_authority)) r_authority = Converter.toStr(params.get("excel_authority")); 
		if(StringUtils.equals("", r_userid)) r_userid = Converter.toStr(params.get("excel_userid")); 
		if(StringUtils.equals("", r_userdepth)) r_userdepth = Converter.toStr(params.get("excel_userdepth")); 
		
		if(!StringUtils.equals("", ri_userusearr)) params.put("ri_useruse", ri_userusearr.split(",", -1));
		
		if(StringUtils.equals("Y", r_gridselectyn)) {
			params.put("r_userid", "");
			params.put("r_userdepth", "");
			params.put("r_authority", "");
			
			
			
			if(StringUtils.equals("1", r_userdepth)) {
				if(StringUtils.equals("SH", r_authority) || StringUtils.equals("SM", r_authority) || StringUtils.equals("SR", r_authority)) { // 영업사원.
					String[] ri_authority = {"SH","SM","SR"};
					params.put("ri_authority", ri_authority);
				}else {
					params.put("r_authority", r_authority);
				}
			}
			if(StringUtils.equals("2", r_userdepth)) {
				if(StringUtils.equals("SH", r_authority) || StringUtils.equals("SM", r_authority) || StringUtils.equals("SR", r_authority)) { // 영업사원.
					params.put("r_userdepth", "3");
					params.put("r_usercate2", r_userid);
				}else {
					params.put("r_authority", r_authority);
					params.put("r_usercate2", r_userid);
				}
			}
			if(StringUtils.equals("3", r_userdepth)) { // 영업사원만.
				if(StringUtils.equals("SH", r_authority) || StringUtils.equals("SM", r_authority) || StringUtils.equals("SR", r_authority)) { // 영업사원.
					params.put("r_userdepth", "4");
					params.put("r_usercate3", r_userid);
				}else {}
			}
			if(StringUtils.equals("4", r_userdepth)) { // 영업사원만.
				if(StringUtils.equals("SH", r_authority) || StringUtils.equals("SM", r_authority) || StringUtils.equals("SR", r_authority)) { // 영업사원.
					params.put("r_userdepth", "4");
					params.put("r_usercate4", r_userid);
				}
			}
			if(StringUtils.equals("5", r_userdepth)) { // QMS
                if(StringUtils.equals("SH", r_authority) || StringUtils.equals("SM", r_authority) || StringUtils.equals("SR", r_authority)) { // 영업사원.
                    params.put("r_userdepth", "5");
                    params.put("r_usercate5", r_userid);
                }
            }
		}
		
		int totalCnt = userDao.cnt(params);

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
		if(StringUtils.equals("", sidx)) { r_orderby = "USER_SORT1 ASC, USER_SORT2 ASC, USER_SORT3 ASC, USER_SORT4 ASC"; } //디폴트 지정
		params.put("r_orderby", r_orderby);

		// 엑셀 다운로드.
		String where = Converter.toStr(params.get("where"));
		if ("excel".equals(where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}
		
		List<Map<String, Object>> list = this.getUserList(params);
		resMap.put("list", list);
		resMap.put("data", list);
		
		System.out.println("startnumber>>>" + resMap.get("startnumber"));
		System.out.println("r_page>>>" + resMap.get("r_page"));
		System.out.println("startpage>>>" + resMap.get("startpage"));
		System.out.println("endpage>>>" + resMap.get("endpage"));
		System.out.println("r_limitrow>>>" + resMap.get("r_limitrow"));
		return resMap;
	}
	
	
	/**
	 * 내부 사용자 저장 또는 수정
	 * @작성일 : 2020. 3. 5.
	 * @작성자 : kkyu
	 * @param r_parentuserid : 부모 내부사용자의 아이디. 저장 일때만 [필수]
	 * @param r_userid : 빈값이면 Insert / 빈값이 아니면 Update.
	 */
	public Map<String, Object> insertUpdateAdminUser(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		String r_parentuserid = Converter.toStr(params.get("r_parentuserid"));
		String r_userid = Converter.toStr(params.get("r_userid"));
		String m_userid = Converter.toStr(params.get("m_userid"));
		logger.debug("r_parentuserid : {}, r_userid : {}", r_parentuserid, r_userid);
		
		String process_type = (StringUtils.equals("", r_userid)) ? "ADD" : "EDIT";
		String m_uphcode = "30", m_uphmsg = "관리자변경";
		
		if(StringUtils.equals("ADD", process_type) && StringUtils.equals("", r_parentuserid)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		
		Map<String, Object> parentUserMap = null;
		String parentAuthority = "", parentUserId = ""; int parentUserDepth = 0;
		if(StringUtils.equals("ADD", process_type)) {
			parentUserMap = this.getUserOneAll(r_parentuserid);
			logger.debug("parentUserMap : {}", parentUserMap);
			if(CollectionUtils.isEmpty(parentUserMap)) throw new LimeBizException(MsgCode.DATA_PROCESS_ERROR);
			
			parentUserId = Converter.toStr(parentUserMap.get("USERID"));
			parentAuthority = Converter.toStr(parentUserMap.get("AUTHORITY"));
			
			if(StringUtils.equals("2", parentUserId)) {
				parentAuthority = "SH";
			}
			else {
				if(StringUtils.equals("SH", parentAuthority)) parentAuthority = "SM";
				else if(StringUtils.equals("SM", parentAuthority)) parentAuthority = "SR";
				else if(StringUtils.equals("SR", parentAuthority)) parentAuthority = "SR";
			}
			
			parentUserDepth = Converter.toInt(parentUserMap.get("USER_DEPTH"));
			logger.debug("parentAuthority : {}, parentUserDepth : {}", parentAuthority, parentUserDepth);
		}
		
		// 파일 업로드.
		String sepa = System.getProperty("file.separator");
		String folderName = "user";
		String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
		List<Map<String, Object>> fList = new ArrayList<>();
		
		try {
			if (!MultipartHttpServletRequest.class.isInstance(req)) {
				throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
			}

			MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;
			fList = fileUpload.execManyField(mtreq, "imageFiles", uploadDir, "m_userfile");
			logger.debug("fList : {}", fList);
		} catch (Exception e) {
			fileUpload.deleteList(fList, uploadDir);
			throw e;
		}
		
		String m_userfile = "";
		String m_userfiletype = "";
		for(Map<String, Object> file : fList) {
			if(StringUtils.equals("m_userfile", Converter.toStr(file.get("fieldName")))) {
				m_userfile = Converter.toStr(file.get("saveFileName"));
				m_userfiletype = Converter.toStr(file.get("mimeType"));
			}
		}
		
		// 저장.
		if(StringUtils.equals("ADD", process_type)) {
			//params.put("m_userid", m_userid);
			params.put("m_passchangeyn", "N");
			//params.put("m_failcnt", 0);
			params.put("m_authority", parentAuthority);
			
			params.put("m_useruse", "Y");
			//params.put("m_userview", "Y");
			params.put("m_userdepth", parentUserDepth+1);
			params.put("m_userparent", r_parentuserid);
			
			params.put("m_userfile", m_userfile);
			params.put("m_userfiletype", m_userfiletype);
			
			params.put("m_insertid", loginDto.getUserId());
			
			if(1 == parentUserDepth) { // 2
				int userSort = this.getUserSortMax(r_parentuserid, "USER_SORT2");
				params.put("m_usercate1", Converter.toStr(parentUserMap.get("USER_CATE1")));
				params.put("m_usercate2", m_userid);
				
				params.put("m_usersort1", Converter.toInt(parentUserMap.get("USER_SORT1")));
				params.put("m_usersort2", userSort);
			}
			else if(2 == parentUserDepth) { // 3
				int userSort = this.getUserSortMax(r_parentuserid, "USER_SORT3");
				params.put("m_usercate1", Converter.toStr(parentUserMap.get("USER_CATE1")));
				params.put("m_usercate2", Converter.toStr(parentUserMap.get("USER_CATE2")));
				params.put("m_usercate3", m_userid);
				
				params.put("m_usersort1", Converter.toInt(parentUserMap.get("USER_SORT1")));
				params.put("m_usersort2", Converter.toInt(parentUserMap.get("USER_SORT2")));
				params.put("m_usersort3", userSort);
			}
			else if(3 == parentUserDepth) { // 4
				int userSort = this.getUserSortMax(r_parentuserid, "USER_SORT3");
				params.put("m_usercate1", Converter.toStr(parentUserMap.get("USER_CATE1")));
				params.put("m_usercate2", Converter.toStr(parentUserMap.get("USER_CATE2")));
				params.put("m_usercate3", Converter.toStr(parentUserMap.get("USER_CATE3")));
				params.put("m_usercate4", m_userid);
				
				params.put("m_usersort1", Converter.toInt(parentUserMap.get("USER_SORT1")));
				params.put("m_usersort2", Converter.toInt(parentUserMap.get("USER_SORT2")));
				params.put("m_usersort3", Converter.toInt(parentUserMap.get("USER_SORT3")));
				params.put("m_usersort4", userSort);
			}
			
			userDao.in(params);
			
			// 비밀번호 변경이력 저장
			insertUserPswdHistory("1", r_userid, Converter.toStr(params.get("m_userpwd")), m_uphcode, m_uphmsg, loginDto.getUserId());
		}
		// 수정.
		else {
			Map<String, Object> svcMap = new HashMap<>();
			
			String m_userpwd = Converter.toStr(params.get("m_userpwd"));
			if(!StringUtils.equals("", m_userpwd)) {
				svcMap.put("m_userpwd", m_userpwd);
			
				// 비밀번호 변경 이력중 최근 ?회 동일여부 체크. 0인 경우 체크 하지 않음.
				this.checkUsableUserPswd(r_userid, m_userpwd);
			}
			
			svcMap.put("r_userid", r_userid);
			svcMap.put("m_usernm", Converter.toStr(params.get("m_usernm")));
			svcMap.put("m_userposition", Converter.toStr(params.get("m_userposition")));
			svcMap.put("m_cellno", Converter.toStr(params.get("m_cellno")));
			svcMap.put("m_telno", Converter.toStr(params.get("m_telno")));
			svcMap.put("m_usernm", Converter.toStr(params.get("m_usernm")));
			svcMap.put("m_useremail", Converter.toStr(params.get("m_useremail")));
			
			svcMap.put("m_userfile", m_userfile);
			svcMap.put("m_userfiletype", m_userfiletype);

			svcMap.put("m_updateid", loginDto.getUserId());
			
			userDao.up(svcMap);
			
			if(StringUtils.equals("Y", Converter.toStr(params.get("isMine")))) {
				loginDto.setUserFile(m_userfile);
				
				m_uphcode = "20"; m_uphmsg = "사용자직접변경";
			}
			
			if(!StringUtils.equals("", m_userpwd)) {
				insertUserPswdHistory("1", r_userid, Converter.toStr(params.get("m_userpwd")), m_uphcode, m_uphmsg, loginDto.getUserId());
			}
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 사용자 업데이트.
	 * @작성일 : 2020. 3. 5.
	 * @작성자 : kkyu
	 * @param ri_userid : ,로 구분한 사용자 아이디.
	 * @param r_initpwd : 비밀번호 초기화(=Y)
	 */
	public Map<String, Object> updateUserTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		String ri_userid = Converter.toStr(params.get("ri_userid"));
		String r_initpwd = Converter.toStr(params.get("r_initpwd"));
		
		String [] ri_useridarr = null;
		if(!StringUtils.equals("", ri_userid)) ri_useridarr = ri_userid.split(",", -1);
		if(ArrayUtils.isEmpty(ri_useridarr)) {
			throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
		}
		params.put("ri_userid", ri_useridarr);
		params.put("m_updateid", loginDto.getUserId());
		
		// 공통 업데이트.
		if(!StringUtils.equals("Y", r_initpwd)) {
			userDao.upByArr(params);
		}
		else { // 개별 업데이트 : 비밀번호 초기화.
			for(String r_userid : ri_useridarr) {
				//params.put("r_initpwd", r_initpwd);
				params.put("r_userid", r_userid);
				params.put("m_userpwd", r_userid);
				params.put("m_passchangeyn", "N");
				userDao.up(params);
				
				// 비밀번호 변경 이력 저장.
				insertUserPswdHistory("1", r_userid, r_userid, "60", "비밀번호 초기화", loginDto.getUserId());
			}
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * CSSALESMAP 고정.
	 * @작성일 : 2020. 3. 11.
	 * @작성자 : kkyu
	 * @수정일 : 2020. 5. 19.
	 * @수정자 : lee
	 * @수정내용 : CS가 임시저장 되어 있는 영업담당자를 고정으로 등록하면 에러 출력 수정
	 * 고정담당등록 insert -> merge로 변경
	 * @param m_cssuserid : CS 담당자 아이디.
	 * @param mi_salesuserid : 영업사원 아이디 ,로 구분한 문자열.
	 */
	public Map<String, Object> updateFixedCsSalesMapTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		//String m_fixedyn = Converter.toStr(params.get("m_fixedyn"));
		String m_csuserid = Converter.toStr(params.get("m_csuserid"));
		String mi_salesuserid = Converter.toStr(params.get("mi_salesuserid"));
		logger.debug("m_csuserid : {}", m_csuserid);
		logger.debug("mi_salesuserid : {}", mi_salesuserid);
		
		if(StringUtils.equals("", m_csuserid) || StringUtils.equals("", mi_salesuserid)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		Map<String, Object> svcMap = new HashMap<>();
		
		String[] mi_salesuseridarr = mi_salesuserid.split(",", -1);
		for(String m_salesuserid : mi_salesuseridarr) {
			svcMap.put("r_salesuserid", m_salesuserid);
			svcMap.put("r_fixedyn", "Y");
			csSalesMapDao.del2(svcMap);
			
			svcMap.put("m_csuserid", m_csuserid);
			svcMap.put("m_salesuserid", m_salesuserid);
			svcMap.put("m_fixedyn", "Y");
			svcMap.put("m_insertid", loginDto.getUserId());
			csSalesMapDao.merge(svcMap);
			svcMap.clear();
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * CSSALESMAP 임시저장.
	 * @작성일 : 2020. 3. 10.
	 * @작성자 : kkyu
	 * @수정일 : 2020. 5. 18
	 * @수정자 : lee
	 * @수정내용 : CS유저 자신이 고정으로 등록된 데이터 임시저장 시 고정이 풀리는 현상 수정
	 * @param mi_salesuserid : 영업사원 아이디 배열.
	 */
	public Map<String, Object> insertCsSalesMapTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		//String m_fixedyn = Converter.toStr(params.get("m_fixedyn"));
		String[] mi_salesuserid = req.getParameterValues("mi_salesuserid");
		String userid = loginDto.getUserId();
		if(!ArrayUtils.isEmpty(mi_salesuserid)) {

			params.put("m_csuserid", userid);
			params.put("m_insertid", userid);
			for(String m_salesuserid : mi_salesuserid) {
				params.put("r_csuserid", userid);
				params.put("r_salesuserid", m_salesuserid);
				params.put("r_fixedyn", "Y");

				if(csSalesMapDao.cnt(params) < 1){
 					params.put("m_salesuserid", m_salesuserid);
					csSalesMapDao.merge(params);
				}

			}
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}

	
	/**
	 * 거래처 계정(외부사용자) 저장 또는 수정
	 * @작성일 : 2020. 3. 13.
	 * @작성자 : an
	 * @param r_userid : 빈값이면 Insert / 빈값이 아니면 Update.
	 */
	public Map<String, Object> insertUpdateUser(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		String r_userid = Converter.toStr(params.get("r_userid"));
		String process_type = (StringUtils.equals("", r_userid)) ? "ADD" : "EDIT";
		String pageType = Converter.toStr(params.get("pageType"));		
		
		// 저장.
		if(StringUtils.equals("ADD", process_type)) {
			String m_userid = Converter.toStr(params.get("m_userid"));
			String m_userpswd = Converter.toStr(params.get("m_userpswd"));
			
			Map<String, Object> userMap = getUserOne(m_userid);
			if(!CollectionUtils.isEmpty(userMap)) {
				throw new LimeBizException(MsgCode.DATA_OVERLAP_ERROR, "아이디");
			}
			
			params.put("m_custcd", Converter.toStr(params.get("r_custcd")));
			params.put("m_passchangeyn", "N");
			params.put("m_agreeyn", "N");
			params.put("m_authority", "CO"); //외부사용자
			params.put("m_useruse", "Y");	 //사용여부
			params.put("m_usereorder", "Y"); //eorder사용자 Y/N
			params.put("m_insertid", loginDto.getUserId());
			
			userDao.in(params);
			
			// 비밀번호 변경이력 저장
			insertUserPswdHistory("1", m_userid, m_userpswd, "30", "관리자 변경", loginDto.getUserId());
		}
		// 수정.
		else {
			Map<String, Object> svcMap = new HashMap<>();
			
			String m_userpwd = Converter.toStr(params.get("m_userpwd"));
			if(!StringUtils.equals("", m_userpwd)) {
				// 비밀번호 변경 이력중 최근 ?회 동일여부 체크. 0인 경우 체크 하지 않음.
				this.checkUsableUserPswd(r_userid, m_userpwd);
				
				svcMap.put("m_userpwd", m_userpwd);
			}
			
			// 프론트 마이페이지 > 프로필저장
			if(StringUtils.equals("mypage", pageType)) { 
				String sepa = System.getProperty("file.separator");
				String folderName = "user";
				String uploadDir = new StringBuilder(req.getSession().getServletContext().getRealPath("/")).append(sepa).append("data").append(sepa).append(folderName).toString();
				
				String m_userfile = "";
				String m_userfiletype = "";
				
				List<Map<String, Object>> fList = new ArrayList<>();
				try {
					if (!MultipartHttpServletRequest.class.isInstance(req)) {
						throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
					}
					MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest) req;
					fList = fileUpload.execManyField(mtreq, "imageFiles", uploadDir, "m_userfile");
					logger.debug("fList : {}", fList);
					
					for(Map<String, Object> file : fList) {
						if(StringUtils.equals("m_userfile", Converter.toStr(file.get("fieldName")))) {
							m_userfile = Converter.toStr(fList.get(0).get("saveFileName"));
							m_userfiletype = Converter.toStr(fList.get(0).get("mimeType"));
						}
					}
					
				} catch (Exception e) {
					fileUpload.deleteList(fList, uploadDir);
					throw e;
				}
				
				svcMap.put("m_userfile", m_userfile);
				svcMap.put("m_userfiletype", m_userfiletype);
			}
			
			svcMap.put("r_userid", r_userid);
			svcMap.put("m_usernm", Converter.toStr(params.get("m_usernm")));
			svcMap.put("m_cellno", Converter.toStr(params.get("m_cellno")).replaceAll("-", ""));
			svcMap.put("m_telno", Converter.toStr(params.get("m_telno")).replaceAll("-", ""));
			svcMap.put("m_useremail", Converter.toStr(params.get("m_useremail")));
			svcMap.put("m_updateid", loginDto.getUserId());
			
			if(!"".equals(Converter.toStr(params.get("m_useragree")))) {
				svcMap.put("m_useragree", Converter.toStr(params.get("m_useragree"))); //이용수칙동의여부
			}
			
			userDao.up(svcMap);
			
			// 비밀번호 변경이력 저장
			if(!StringUtils.equals("", m_userpwd)) {
				this.insertUserPswdHistory("1", r_userid, m_userpwd, "30", "관리자 변경", loginDto.getUserId());
			}
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	
	/**
	 * 납품처 계정 수정
	 * @작성일 : 2020. 3. 18.
	 * @작성자 : an
	 * @param ri_userid : ,로 구분한 회원아이디.
	 */
	public Map<String, Object> updateShiptoUser(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> svcMap = new HashMap<>();
		
		String ri_userid = Converter.toStr(params.get("ri_userid"));
		String m_useruse = Converter.toStr(params.get("m_useruse"));
		
		String [] ri_useridArr = null;
		if(!StringUtils.equals("", ri_userid)) ri_useridArr = ri_userid.split(",", -1);
		if(ArrayUtils.isEmpty(ri_useridArr)) {
			throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		}
		
		for(String r_userid : ri_useridArr) {
			svcMap.put("r_userid", r_userid);
			svcMap.put("m_useruse", m_useruse);
			svcMap.put("m_updateid", loginDto.getUserId());
			userDao.up(svcMap);
			svcMap.clear();
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	
	/**
	 * 비밀번호 변경이력 저장.
	 * @작성일 : 2020. 3. 20.
	 * @작성자 : an
	 * @param insert_type : 1=uph_pswd값이 암호화 안된값으로 암호화해서 인서트. 2=uph_pswd값이 암호화 된값으로 바로 인서트. 
	 */
	public int insertUserPswdHistory(String insert_type, String uph_mbid, String uph_pswd, String uph_code, String uph_msg, String uph_inid){
		Map<String, Object> svcMap = new HashMap<String, Object>();
		svcMap.put("m_uphid", uph_mbid);
		svcMap.put("m_uphpswd", uph_pswd);
		svcMap.put("m_uphcode", uph_code);
		svcMap.put("m_uphmsg", uph_msg);
		svcMap.put("m_uphinid", uph_inid);
		
		if(StringUtils.equals("2", insert_type)){
			return this.insertUserPswdHistory2(svcMap);
		}else {
			return this.insertUserPswdHistory(svcMap);
		}
	}
	public int insertUserPswdHistory(Map<String, Object> svcMap){
		return userPswdHistoryDao.in(svcMap);
	}
	public int insertUserPswdHistory2(Map<String, Object> svcMap){
		return userPswdHistoryDao.in2(svcMap);
	}

	
	/**
	 * 비밀번호 변경시 사용가능여부 확인.
	 * 비밀번호 변경 이력중 최근 ?회 동일여부 체크. 0인 경우 체크 하지 않음.
	 * @param uph_pswd_now : 변경할 비밀번호
	 * @throws LimeBizException
	 */
	public Map<String, Object> checkUsableUserPswd(String uph_id, String uph_pswd_now) throws LimeBizException{
		int userPswdCountCheck = Converter.toInt((configSvc.getConfigValue("USERPSWDCHECK"))); // ?회
		if(0 < userPswdCountCheck){
			Map<String, Object> svcMap = new HashMap<String, Object>();
			svcMap.put("r_uphid", uph_id);
			svcMap.put("r_uphpswd_now", uph_pswd_now);
			svcMap.put("r_startrow", 1);
			svcMap.put("r_endrow", userPswdCountCheck);
			svcMap.put("r_orderby", "UPH_SEQ DESC ");
			List<Map<String, Object>> userPswdHistoryList = userPswdHistoryDao.list(svcMap);
			logger.debug("# usertPswdHistoryList : {}", userPswdHistoryList);
			if(!userPswdHistoryList.isEmpty()){
				String nowPswdBySecurity = Converter.toStr(userPswdHistoryList.get(0).get("NOW_PASSWORD_SECURITY"));
				for(Map<String, Object> one : userPswdHistoryList){
					if(nowPswdBySecurity.equals(Converter.toStr(one.get("UPH_PSWD")))){
						throw new LimeBizException(MsgCode.DATA_REUSE_ERROR, userPswdCountCheck);
					}
				}
			}
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	
	/**
	 * 납품처계정 자동생성
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : an
	 */
	public Map<String, Object> insertShiptoUserTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> svcMap = new HashMap<>();
		
		String r_custcd = Converter.toStr(params.get("r_custcd"));
		if (StringUtils.equals("", r_custcd)) {
			throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR, "거래처코드");
		}
		
		// 계정이 없는 거래처
		Map<String, Object> custParam = new HashMap<>();
		custParam.put("mCustCd", r_custcd);
		
		int custCnt = userDao.cntCustomerUser(custParam);
		if(custCnt < 1) {
			userDao.inCustomerUser(custParam);
			custParam.clear();
		}
		
		// 계정이 없는 납품처
		svcMap.put("r_custcd", r_custcd);
		List<Map<String, Object>> newShiptoList = shipToDao.getNewShiptoList(svcMap);
		svcMap.clear();
		
		if(!newShiptoList.isEmpty()) {
			for(Map<String, Object> stMap : newShiptoList) {
				
				// 납품처 계정 자동생성
				svcMap.put("m_userid", Converter.toStr(stMap.get("SHIPTO_USERID")));
				svcMap.put("m_userpwd", Converter.toStr(stMap.get("SHIPTO_USERID")));
				svcMap.put("m_usernm", Converter.toStr(stMap.get("SHIPTO_NM")));
				svcMap.put("m_custcd", Converter.toStr(stMap.get("CUST_CD")));
				svcMap.put("m_usershiptocd", Converter.toStr(stMap.get("SHIPTO_CD")));
				svcMap.put("m_useruse", "N");
				svcMap.put("m_userview", "Y");
				svcMap.put("m_usereorder", "Y");
				svcMap.put("m_agreeyn", "N");
				svcMap.put("m_authority", "CT");
				svcMap.put("m_insertid", loginDto.getUserId());
				userDao.in(svcMap);
				svcMap.clear();
				
				//비밀번호 이력저장
				svcMap.put("m_uphid", Converter.toStr(stMap.get("SHIPTO_USERID")));
				svcMap.put("m_uphpswd", Converter.toStr(stMap.get("SHIPTO_USERID")));
				svcMap.put("m_uphcode", "30");
				svcMap.put("m_uphmsg", "관리자 변경");
				svcMap.put("m_uphinid", loginDto.getUserId());
				userPswdHistoryDao.in(svcMap);
				svcMap.clear();
			}
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	
	/**
	 * 비밀번호 변경하기. 
	 * @param r_processtype : UP=비밀번호 변경, AFTER=?개월 후 다시변경. 
	 * @param r_userpwd 현재 비밀번호.
	 * @param m_userpwd 변경할 비밀번호.
	 * @param c_userpwd 변경할 비밀번호 확인.
	 */
	public Map<String, Object> updateUserPswd(Map<String, Object> params, LoginDto loginDto) throws Exception{
		// 세션.
		String r_userid = Converter.toStr(loginDto.getUserId());
		
		// 파라미터 & 변수정의.
		String r_processtype = Converter.toStr(params.get("r_processtype"));
		String r_userpwd = Converter.toStr(params.get("r_userpwd"));
		String m_userpwd = Converter.toStr(params.get("m_userpwd"));
		
		// 필수 파라미터 체크.
		if("".equals(r_userid) || ("UP".equals(r_processtype) && "".equals(m_userpwd))){
			throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		}
		
		// 현재 비밀번호 체크.
		Map<String, Object> userMap = this.getUser(r_userid, r_userpwd);
		logger.debug("# userMap : {}", userMap);
		
		if(null == userMap){
			throw new LimeBizException(MsgCode.DATA_PSWD_ERROR);
		}
		
		if("UP".equals(r_processtype)){
			// 비밀번호 변경 이력중 최근 ?회 동일여부 체크. 0인 경우 체크 하지 않음.
			this.checkUsableUserPswd(r_userid, m_userpwd);
			
			// 비밀번호 업데이트.
			params.put("r_userid", r_userid);
			params.put("m_passchangeyn", "Y");
			userDao.up(params);
			
			// 비밀번호 변경이력 인서트.
			this.insertUserPswdHistory("1", r_userid, m_userpwd, "40", "변경요청페이지에서직접변경", loginDto.getUserId());
			
		}else{
			// 비밀번호 변경이력 인서트.
			this.insertUserPswdHistory("2", r_userid, Converter.toStr(userMap.get("USER_PWD")), "41", "변경요청페이지에서변경연장", loginDto.getUserId());
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);	
	}
	
	/**
	 * 임시 비밀번호 생성
	 * @param model
	 * @param req
	 * @return
	 */
	public static String temporaryPassword(int size) {
		StringBuffer buffer = new StringBuffer();
		SecureRandom random = new SecureRandom();
		String chars[] = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,0,1,2,3,4,5,6,7,8,9,!,@,#,$,%,^,&,*".split(",");
				
		for (int i = 0; i < size; i++) {
			buffer.append(chars[random.nextInt(chars.length)]);
		}
		return buffer.toString();
	}
	
	/**
	 * 비밀번호 찾기
	 * @작성일 : 2020. 4. 1.
	 * @작성자 : an
	 * @param  
	 * @return
	 */
	public Map<String, Object> userPswdSearchTransaction(Map<String, Object> params, HttpServletRequest req, HttpSession session) throws Exception {
		Map<String, Object> svcMap = new HashMap<>();
		
		String r_userid = Converter.toStr(params.get("r_userid"));
		String r_useremail = Converter.toStr(params.get("r_useremail"));
		String popType = Converter.toStr(params.get("popType")); // A:내부,F:외부
		
		String[] ri_authority = {"AD","SH","SM","SR","CS","MK","AS","CI"}; // 내부사용자 : AD,SH,SM,SR,CS,MK,AS
		String[] ri_authority_f = {"CO","CT"};						  // 외부사용자 : CO,CT
		
		// 계정 체크
		if(StringUtils.equals("F", popType)) params.put("ri_authority", ri_authority_f);
		else params.put("ri_authority", ri_authority);
		
		Map<String, Object> userMap = this.getUserOne(params);
		
		if(CollectionUtils.isEmpty(userMap)) {
			throw new LimeBizException(MsgCode.ACCOUNT_ERROR);
		
		}else {
			// 임시비밀번호 생성
			String tempPswd = temporaryPassword(10); 
			
			//회원정보 임시비밀번호로 업데이트
			svcMap.put("r_userid", r_userid);
			svcMap.put("m_userpwd", tempPswd);
			svcMap.put("m_updateid", r_userid);
			userDao.up(svcMap);
			
			// 비밀번호 변경이력 저장
			insertUserPswdHistory("1", r_userid, tempPswd, "50", "비밀번호찾기변경", r_userid);
			
			// 메일보내기
			String title = "["+shopName+"] 임시 비밀번호가 발급되었습니다.";
			String url = httpsUrl + req.getServerName() + (req.getServerPort() == 80 ? "" : ":" + Converter.toStr(req.getServerPort())) +  req.getContextPath();
			String contentStr = templeteSvc.userPswdResetEmail(r_userid, Converter.toStr(userMap.get("USER_NM")), tempPswd, url);
			
			MailUtil mail = new MailUtil();
			mail.sendMail(smtpHost, title, r_userid, r_useremail, shopName, smtpSender, contentStr, null, "");
		}
		
		return MsgCode.getResultMap(MsgCode.DATA_PSWD_EMAIL);
	}
	
	
	/**
	 * 영업조직 구조 변경 샘플파일 리스트 가져오기.
	 * @작성일 : 2020. 6. 9.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> salesUserCategoryEditExcel(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		resMap.put("list", userDao.listForSalesUserCategory(params));
		return resMap;
	}
	
	
	/**
	 * 영업조직 구조 수정  By Excel Upload.
	 * @작성일 : 2020. 6. 9.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> updateSalesUserCategoryEditExceTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		if (!MultipartHttpServletRequest.class.isInstance(req)) {
			throw new LimeBizException(MsgCode.FILE_REQUEST_ERROR);
		}
		
		MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;
		MultipartFile mpf = mtreq.getFile("file");
		Workbook workbook = WorkbookFactory.create(mpf.getInputStream());
		Sheet sheet = workbook.getSheetAt(0);

		DataFormatter formatter = new DataFormatter();
		Iterator<Row> it = sheet.iterator();

		int rowNum = 0, cellNum = 0;
		
		List<Map<String, String>> SHList = new ArrayList<>();
		List<Map<String, String>> SMList = new ArrayList<>();
		List<Map<String, String>> SRList = new ArrayList<>();
		List<Map<String, String>> RTList = new ArrayList<>();
		String checkUsers = ""; 

		while (it.hasNext()) {
			Row row = it.next();

			if(rowNum > 0){
				Map<String, String> salesUser = new HashMap<>();
				
				cellNum=0;
				
				String authority = Converter.toStr(formatter.formatCellValue(row.getCell(cellNum++)).replaceAll("\\s","")); // 권한, 모든 공백제거
				String userid = Converter.toStr(formatter.formatCellValue(row.getCell(cellNum++)).replaceAll("\\s","")); // 영업담당자 아이디
				String usernm = Converter.toStr(formatter.formatCellValue(row.getCell(cellNum++)).replaceAll("\\s","")); // 영업담당자 이름 >>> 사용은안함
				String user_cate2 = Converter.toStr(formatter.formatCellValue(row.getCell(cellNum++)).replaceAll("\\s","")); // 상위 SH 아이디
				String user_cate2_nm = Converter.toStr(formatter.formatCellValue(row.getCell(cellNum++)).replaceAll("\\s","")); // 상위 SH 이름 >>> 사용은안함
				String user_cate3 = Converter.toStr(formatter.formatCellValue(row.getCell(cellNum++)).replaceAll("\\s","")); // 상위 SM 아이디
				String user_cate3_nm = Converter.toStr(formatter.formatCellValue(row.getCell(cellNum++)).replaceAll("\\s","")); // 상위 SM 이름 >>> 사용은안함
				
				//logger.debug("authority : {} / userid : {}", authority, userid);
				//logger.debug("user_cate2 : {} / user_cate3 : {}", user_cate2, user_cate3);

				if(StringUtils.equals("19000101", userid)) { // 비사용자그룹=19000101
					continue;
				}
				
				// 다음행에 공통 필수값이 하나라도 없는 경우 끝냄.
				if(StringUtils.equals("", authority) || StringUtils.equals("", userid)) {
					break;
				}
				
				// SH 필수값 체크.
				if(StringUtils.equals("SH", authority)) {
					user_cate2 = "";
					user_cate3 = "";
				}
				// SM 필수값 체크.
				if(StringUtils.equals("SM", authority)) {
					user_cate3 = "";
					
					if(StringUtils.equals("", user_cate2)) {
						throw new LimeBizException(MsgCode.ALERT, "SM 영업담당자 아이디 "+userid+"의 상위 SH 아이디 값을 입력해 주세요.");
					}
				}
				// SR 필수값 체크.
				if(StringUtils.equals("SR", authority)) {
					if(StringUtils.equals("", user_cate2)) {
						throw new LimeBizException(MsgCode.ALERT, "SR 영업담당자 아이디 "+userid+"의 상위 SH 아이디 값을 입력해 주세요.");
					}
					if(StringUtils.equals("", user_cate3)) {
						throw new LimeBizException(MsgCode.ALERT, "SR 영업담당자 아이디 "+userid+"의 상위 SM 아이디 값을 입력해 주세요.");
					}
				}
				// RT Default 값 정의.
				if(StringUtils.equals("RT", authority)) {
					user_cate2 = "19000101";
					user_cate3 = "";
				}

				salesUser.put("AUTHORITY", authority);
				salesUser.put("USERID", userid);
				//salesUser.put("USERNM", usernm);
				salesUser.put("USER_CATE2", user_cate2);
				salesUser.put("USER_CATE3", user_cate3);
				
				if(StringUtils.equals("SH", authority)) {
					if(!StringUtils.equals("19000101", userid)) { // 엑셀데이터에 비사용자그룹 아이디 비사용자그룹(19000101)는 SHList에서는 무조건 제외하고, 따로 인서트 해준다.
						if(StringUtils.equals("", checkUsers)) checkUsers = userid;
						else checkUsers += ","+userid;
						
						SHList.add(salesUser);
					}
				}
				if(StringUtils.equals("SM", authority)) {
					if(StringUtils.equals("", checkUsers)) checkUsers = userid;
					else checkUsers += ","+userid;
					
					SMList.add(salesUser);
				}
				if(StringUtils.equals("SR", authority)) {
					if(StringUtils.equals("", checkUsers)) checkUsers = userid;
					else checkUsers += ","+userid;
					
					SRList.add(salesUser);
				}
				if(StringUtils.equals("RT", authority)) {
					if(StringUtils.equals("", checkUsers)) checkUsers = userid;
					else checkUsers += ","+userid;
					
					RTList.add(salesUser);
				}
			}
			
			rowNum++;
		}
		
		// 리스트 정렬 > USERID 기준.
		SHList = SHList.stream().sorted((o1, o2) -> o1.get("USERID").toString().compareTo(o2.get("USERID").toString()) ).collect(Collectors.toList());
		
		SMList = SMList.stream().sorted((o1, o2) -> o1.get("USER_CATE2").toString().compareTo(o2.get("USER_CATE2").toString()) ).collect(Collectors.toList());
		SMList = SMList.stream().sorted((o1, o2) -> o1.get("USERID").toString().compareTo(o2.get("USERID").toString()) ).collect(Collectors.toList());
		
		SRList = SRList.stream().sorted((o1, o2) -> o1.get("USER_CATE2").toString().compareTo(o2.get("USER_CATE2").toString()) ).collect(Collectors.toList());
		SRList = SRList.stream().sorted((o1, o2) -> o1.get("USER_CATE3").toString().compareTo(o2.get("USER_CATE3").toString()) ).collect(Collectors.toList());
		SRList = SRList.stream().sorted((o1, o2) -> o1.get("USERID").toString().compareTo(o2.get("USERID").toString()) ).collect(Collectors.toList());
		
		RTList = RTList.stream().sorted((o1, o2) -> o1.get("USERID").toString().compareTo(o2.get("USERID").toString()) ).collect(Collectors.toList());
		
		//logger.debug("SHList : {}", SHList);
		//logger.debug("SMList : {}", SMList);
		//logger.debug("SRList : {}", SRList);
		//logger.debug("RTList : {}", RTList);
		
		// 체크 1.
		String[] checkUserArr = checkUsers.split(",", -1);
		for(int i=0,j=checkUserArr.length; i<j; i++) {
			String str1 = checkUserArr[i];
			for(int x=0,y=checkUserArr.length; x<y; x++) {
				if (i==x) continue;
				String str2 = checkUserArr[x];
				if(StringUtils.equals(str1, str2)) {
					return MsgCode.getResultMap(MsgCode.ALERT, "영엉담당자 아이디에 중복된 값이 있습니다.("+str1+")"); 
				}
			}
		}
		
		// 체크 2.
		params.put("rni_userid", checkUsers.split(",", -1));
		int checkCnt = userDao.checkSalesUser(params);
		logger.debug("checkCnt : {}", checkCnt);
		if(0 < checkCnt) {
			return MsgCode.getResultMap(MsgCode.ALERT, "현재 DB에 저장되어있는 모든 영업사원의 아이디를 입력해 주세요."); 
		}
		
		// INSERT O_USER2.
		Map<String, Object> svcMap = new HashMap<>();
		int m_historyseq = userDao.getUser2SeqMax(null);
		if(!CollectionUtils.isEmpty(SHList)) {
			// O_USER.USERID = 9 && 2 && 비사용자그룹(19000101)는 default로 인서트.
			svcMap.put("m_historyseq", m_historyseq);
			userDao.inUser2ForDefault(svcMap);
			svcMap.clear();
			
			int sort2 = 1;
			for(Map<String, String> one : SHList) {
				svcMap.put("r_userid", one.get("USERID"));
				svcMap.put("m_historyseq", m_historyseq);
				svcMap.put("m_authority", one.get("AUTHORITY"));
				svcMap.put("m_userdepth", 2);
				svcMap.put("m_userparent", "2");
				svcMap.put("m_usercate1", "2");
				svcMap.put("m_usercate2", one.get("USERID"));
				svcMap.put("m_usercate3", null);
				svcMap.put("m_usercate4", null);
				svcMap.put("m_usersort1", 2);
				svcMap.put("m_usersort2", sort2);
				svcMap.put("m_usersort3", 0);
				svcMap.put("m_usersort4", 0);
				svcMap.put("m_updateid", loginDto.getUserId());
				
				userDao.inUser2(svcMap);
				svcMap.clear();
				
				sort2++;
			}
		}
		
		if(!CollectionUtils.isEmpty(SMList)) {
			int sort3 = 1;
			for(Map<String, String> one : SMList) {
				svcMap.put("r_sortcolumn", "USER_SORT2");
				svcMap.put("r_userparent", one.get("USER_CATE2"));
				svcMap.put("r_historyseq", m_historyseq);
				int sort2 = userDao.getUser2ParentSort(svcMap);
				svcMap.clear();
				
				svcMap.put("r_userid", one.get("USERID"));
				svcMap.put("m_historyseq", m_historyseq);
				svcMap.put("m_authority", one.get("AUTHORITY"));
				svcMap.put("m_userdepth", 3);
				svcMap.put("m_userparent", one.get("USER_CATE2"));
				svcMap.put("m_usercate1", "2");
				svcMap.put("m_usercate2", one.get("USER_CATE2"));
				svcMap.put("m_usercate3", one.get("USERID"));
				svcMap.put("m_usercate4", null);
				svcMap.put("m_usersort1", 2);
				svcMap.put("m_usersort2", sort2);
				svcMap.put("m_usersort3", sort3);
				svcMap.put("m_usersort4", 0);
				svcMap.put("m_updateid", loginDto.getUserId());
				
				userDao.inUser2(svcMap);
				svcMap.clear();
				
				sort3++;
			}
		}
		
		if(!CollectionUtils.isEmpty(SRList)) {
			int sort4 = 1;
			for(Map<String, String> one : SRList) {
				svcMap.put("r_sortcolumn", "USER_SORT2");
				svcMap.put("r_userparent", one.get("USER_CATE2"));
				svcMap.put("r_historyseq", m_historyseq);
				int sort2 = userDao.getUser2ParentSort(svcMap);
				svcMap.clear();
				
				svcMap.put("r_sortcolumn", "USER_SORT3");
				svcMap.put("r_userparent", one.get("USER_CATE3"));
				svcMap.put("r_historyseq", m_historyseq);
				int sort3 = userDao.getUser2ParentSort(svcMap);
				svcMap.clear();
				
				svcMap.put("r_userid", one.get("USERID"));
				svcMap.put("m_historyseq", m_historyseq);
				svcMap.put("m_authority", one.get("AUTHORITY"));
				svcMap.put("m_userdepth", 4);
				svcMap.put("m_userparent", one.get("USER_CATE3"));
				svcMap.put("m_usercate1", "2");
				svcMap.put("m_usercate2", one.get("USER_CATE2"));
				svcMap.put("m_usercate3", one.get("USER_CATE3"));
				svcMap.put("m_usercate4", one.get("USERID"));
				svcMap.put("m_usersort1", 2);
				svcMap.put("m_usersort2", sort2);
				svcMap.put("m_usersort3", sort3);
				svcMap.put("m_usersort4", sort4);
				svcMap.put("m_updateid", loginDto.getUserId());
				
				userDao.inUser2(svcMap);
				svcMap.clear();
				
				sort4++;
			}
		}
		
		if(!CollectionUtils.isEmpty(RTList)) {
			int sort3 = 1;
			for(Map<String, String> one : RTList) {
				svcMap.put("r_userid", one.get("USERID"));
				svcMap.put("m_historyseq", m_historyseq);
				svcMap.put("m_authority", "SM");
				svcMap.put("m_userdepth", 3);
				svcMap.put("m_userparent", one.get("USER_CATE2"));
				svcMap.put("m_usercate1", "2");
				svcMap.put("m_usercate2", one.get("USER_CATE2"));
				svcMap.put("m_usercate3", one.get("USERID"));
				svcMap.put("m_usercate4", null);
				svcMap.put("m_usersort1", 2);
				svcMap.put("m_usersort2", 9999);
				svcMap.put("m_usersort3", sort3);
				svcMap.put("m_usersort4", 0);
				userDao.inUser2(svcMap);
				svcMap.put("m_updateid", loginDto.getUserId());
				
				svcMap.clear();
				
				sort3++;
			}
		}
		
		svcMap.clear();
		svcMap.put("historySeq", m_historyseq);
		svcMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		
		return svcMap;
	}
	
	/**
	 * 엑셀 업로드로 임시 테이블에 저장 된 데이터를 O_USER 테이블에 반영.
	 * > delete 후에 insert 처리.
	 * @작성일 : 2020. 6. 12.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> updateSalesUserCategoryTransaction(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		String r_historyseq = Converter.toStr(params.get("r_historyseq"));
		if(StringUtils.equals("", r_historyseq)){
			throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		}
		logger.debug("r_historyseq : {}", r_historyseq);
		
		// O_USER.AUTHORITY IN ('SH', 'SM', 'SR') 데이터 전체 삭제. 
		userDao.delForUser2Insert(params);
		
		// O_USER2 에서 O_USER로 인서트. where O_USER2.HISTORY_SEQ = r_historyseq 
		userDao.inForUser2(params);
		
		// O_USER2.UPDATE_YN = 'Y'로 업데이트. where O_USER2.HISTORY_SEQ = r_historyseq
		userDao.upUser2(params);
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 엑셀 업로드로 임시 테이블에 저장 된 데이터 삭제.
	 * @작성일 : 2020. 6. 12.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> deleteTempSalesUserCategory(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		String r_historyseq = Converter.toStr(params.get("r_historyseq"));
		if(StringUtils.equals("", r_historyseq)){
			throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);
		}
		logger.debug("r_historyseq : {}", r_historyseq);
		
		// O_USER2 임시 데이터 삭제. 
		userDao.delUser2(params);
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * APP 푸시 알림 여부 가져오기.
	 * @작성일 : 2020. 7. 3.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> getPushYN(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		resMap.put("userOne", this.getUserOne(loginDto.getUserId()));
		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		return resMap;
	}
	
	/**
	 * APP 푸시 알림 여부 Y/N 업데이트.
	 * @작성일 : 2020. 7. 3.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> updatePushYN(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		params.put("r_userid", loginDto.getUserId());
		userDao.up(params);
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * APP 푸시키 DB에 저장 및 세션에 set.
	 * @작성일 : 2020. 7. 3.
	 * @작성자 : kkyu
	 */
	public Map<String, Object> setPushId(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		String r_userid = Converter.toStr(loginDto.getUserId());
		String m_userapppushkey = Converter.toStr(params.get("pushid"));
		
		if(!StringUtils.equals("", r_userid) && !StringUtils.equals("", m_userapppushkey)) {
			// DB저장.
			params.put("r_userid", r_userid);
			params.put("m_userapppushkey", m_userapppushkey);
			userDao.up(params);
			
			// Set Session.
			loginDto.setUserPushId(m_userapppushkey);
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * 자동 로그인 JWT 토큰 생성.  
	 */
	public String makeAutoLoginByJwt(HttpServletRequest req, String mb_id) throws Exception{
		Map<String, Object> jwtInfoMap = new HashMap<>();
		String key = mb_id;
        jwtInfoMap.put("key",  key);
        
        String jwtToken = jwtSvc.makeJWT(req, jwtInfoMap);
        logger.debug("jwtToken : {}", jwtToken);
        
        return jwtToken;
	}
	
	/**
	 * 자동 로그인 토큰체크 하나 가져오기.
	 */
	public Map<String, Object> getLoginAuth(String la_userid, String la_token){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_lauserid", la_userid);
		svcMap.put("r_latoken", la_token);
		return this.getLoginAuth(svcMap);
	}
	public Map<String, Object> getLoginAuth(Map<String, Object> svcMap){
		return loginAuthDao.one(svcMap);
	}
	
}
