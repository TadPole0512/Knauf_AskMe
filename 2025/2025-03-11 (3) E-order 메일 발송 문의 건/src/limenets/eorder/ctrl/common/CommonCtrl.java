package com.limenets.eorder.ctrl.common;


import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.limenets.common.dto.LoginDto;
import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;
import com.limenets.common.listener.SessionListener;
import com.limenets.common.util.AccumSapUtils;
import com.limenets.common.util.Converter;
import com.limenets.common.util.HttpUtils;
import com.limenets.eorder.svc.AccumSapSvc;
import com.limenets.eorder.svc.BoardSvc;
import com.limenets.eorder.svc.CommonSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.UserSvc;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * 공통 컨트롤러 > 비로그인상태에서만 접근가능. 
 */
@Controller
public class CommonCtrl {
	private static final Logger logger = LoggerFactory.getLogger(CommonCtrl.class);
	
	@Value("${kakao.map.app.key}") private String kakaoMapAppKey;
	
	@Inject private BoardSvc boardSvc;
	@Inject private CommonSvc commonSvc;
	@Inject private OrderSvc orderSvc;
	@Inject private UserSvc userSvc;
	@Inject private AccumSapSvc accumSapSvc;

	/**
	 * Admin 로그인 폼.
	 */
	@GetMapping(value="/admin/login/login")
	public String login(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, HttpSession session, Model model) throws Exception {
		// 로그인 성공 이후, 계정 보안 관련 처리 떄문에 아래와 같이 처리 하면 안됨.
		/*
		LoginDto loginDto = (LoginDto) session.getAttribute(LimeLoginArgResolver.SESSION_ATTRIBUTE);
		logger.debug("loginDto : {}", loginDto);
		if(null != loginDto) {
			if (!StringUtils.equals("CO", loginDto.getAuthority()) && !StringUtils.equals("CT", loginDto.getAuthority())) {
				return "redirect:/admin/index/index.lime";
			}
		}
		*/
		
		String loginToken = HttpUtils.newUuid();
		model.addAttribute("loginToken", loginToken);
		session.setAttribute("loginToken", loginToken);

		return "/admin/login/login";
	}
	
	/**
	 * Admin 로그인 처리 Ajax.
	 */
	@ResponseBody
	@PostMapping(value="/admin/login/loginAjax")
	public Object loginAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, HttpSession session, Model model) throws Exception {
		params.put("where", "admin");
		
		String decPw = aesDecrpytion(params.get("r_userpwd").toString());
		params.put("r_userpwd", decPw);
		
		if(decPw==null)
			return MsgCode.getResultMap(MsgCode.DATA_PSWD_ERROR);
		
		Map<String, Object> resMap = userSvc.doLogin(params, req, res, session);

		return resMap;
	}
	
	/**
	 * Admin 로그인 > 이미 아이디가 접속중인 경우, 기존 연결을 종료후 새롭게 로그인 처리 Ajax.
	 */
	@ResponseBody
	@PostMapping(value="/common/disconnectPreLoginAjax")
	public Object disconnectPreLoginAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, HttpSession session, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<>();
		
		String r_userid = Converter.toStr(params.get("r_userid"));
		String where = Converter.toStr(params.get("where")); // appajax=APP Ajax에서 호출.
		
		String setSessionUserId = r_userid;
		if(StringUtils.equals("appdirect", where) || StringUtils.equals("appajax", where)) {
			setSessionUserId += "_app";
		}
		
		SessionListener.getInstance().removeSession(setSessionUserId); // 기존 접속한 유저 아이디 세션 종료.
		//logger.debug("session 1 : {}", session);
		session = req.getSession();
		//logger.debug("session 2 : {}", session);
		
		String loginToken = HttpUtils.newUuid();
		session.setAttribute("loginToken", loginToken);
		resMap.put("loginToken", loginToken);
		resMap.putAll(MsgCode.getResultMap(MsgCode.SUCCESS));
		return resMap;
	}
	/**
	@PostMapping(value="/admin/login/disconnectPreLogin")
	public String disconnectPreLogin(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, HttpSession session, Model model) throws Exception {
		// 기존 접속한 유저 아이디 세션 종료.
		SessionListener.getInstance().removeSession(Converter.toStr(params.get("r_userid")));
		
		// 다시 로그인.
		Map<String, Object> resMap = userSvc.doLogin(params, req, session);
		String resCode = Converter.toStr(resMap.get("RES_CODE"));
		String resMsg = Converter.toStr(resMap.get("RES_MSG"));
		if(StringUtils.equals("0000", resCode)) {
			return "redirect:/admin/index/index.lime";
		}
		
		req.setAttribute("resultAjax", "<script>location.href='./login.lime'; alert('"+resMsg+"');</script>");
		return "textAjax";
	}	
	*/
	
	/**
	 * Admin 로그아웃.
	 */
	@ResponseBody
	@PostMapping(value="/admin/login/logout")
	public Object logout(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, HttpSession session, Model model) {
		session.invalidate();
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * Admin & Front 공통 아이디 중복체크 Ajax.
	 */
	@ResponseBody
	@PostMapping(value="/common/checkUserIdAjax")
	public Object checkUserIdAjax(HttpServletResponse res, HttpServletRequest req, @RequestParam Map<String, Object> params, HttpSession session) throws Exception {
		String r_userid = Converter.toStr(params.get("r_userid"));
		if (StringUtils.equals("", r_userid)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR, "아이디");
		
		int cnt = userSvc.getUserCntAll(r_userid);
		if(0 < cnt) {
			throw new LimeBizException(MsgCode.ERROR, "이미 사용하고 있는 아이디입니다. 다른 아이디를 입력해 주세요.");
		}
		
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * front 로그아웃.
	 */
	@ResponseBody
	@PostMapping(value="/front/login/logout")
	public Object logoutForFront(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, HttpSession session, Model model) {
		// 자동 로그인 토큰 쿠키값 제거.
		userSvc.deleteAutoLoginCookie(res);
				
		session.invalidate();
		return MsgCode.getResultMap(MsgCode.SUCCESS);
	}
	
	/**
	 * Front 로그인 폼.
	 */
	@GetMapping(value="/front/login/login")
	public String frontLogin(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, HttpSession session, Model model) throws Exception {
		// 자동 로그인 토큰 쿠키가 있는 경우.
		Cookie [] cookies = req.getCookies();
		String auth_token = "";
		if(!ArrayUtils.isEmpty(cookies)) {
			for(Cookie c : cookies) {
				if(StringUtils.equals("auth_token", c.getName())) auth_token = c.getValue();
			}
		}
		if(!StringUtils.equals("", auth_token)) {
			params.put("pageType", "F");
			//params.put("r_autologinyn", "Y");
			params.put("where", "appdirect");
			Map<String, Object> resMap = userSvc.doLogin(params, req, res, session);
			
			String resCode = Converter.toStr(resMap.get("RES_CODE"));
			String resMSg = Converter.toStr(resMap.get("RES_MSG"));
			logger.debug("******* resCode : {}", resCode);
			if(StringUtils.equals("0000", resCode)) {
				return "redirect:/front/index/index.lime";
			}
			else if(StringUtils.equals("0316", resCode)) { // 0316=개인정보 입력후 이용해 주시기 바랍니다.
				return "redirect:/front/mypage/myInformation.lime";
			}
			else if(StringUtils.equals("0317", resCode) || StringUtils.equals("0312", resCode)) { // 0317=임시 비밀번호 입니다. / 0312=비밀번호가 %s개월이상 변경되지 않았습니다.
				return "redirect:/front/base/userPswdEdit.lime?resCode="+resCode+"&resMsg="+URLEncoder.encode(resMSg, "UTF-8");
			}
			else {
				userSvc.deleteAutoLoginCookie(res);
			}
		}
		
		String loginToken = HttpUtils.newUuid();
		model.addAttribute("loginToken", loginToken);
		session.setAttribute("loginToken", loginToken);

		//BN_TYPE = 1 : 로그인메인배너
		model.addAttribute("loginBannerList",boardSvc.getBannerListForFront("1",10));
		//로그인 출력 공지사항
		model.addAttribute("noticeList",boardSvc.getBoardListForLogin("notice","login"));

		Gson gson = new Gson();
		//PU_TYPE = 1 : 로그인
		model.addAttribute("popupList", gson.toJson(boardSvc.getPopupListForFront(params,"1")));

		return "/front/login/login";
	}
	
	/**
	 * front 로그인 처리 Ajax.
	 */
	@ResponseBody
	@PostMapping(value="/front/login/loginAjax")
	public Object frontLoginAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, HttpSession session, Model model) throws Exception {
		String where = Converter.toStr(params.get("where"));
		if(StringUtils.equals("", where)) {
			params.put("where", "front");
		}
		
		String decPw = aesDecrpytion(params.get("r_userpwd").toString());
		params.put("r_userpwd", decPw);
		
		if(decPw==null)
			return MsgCode.getResultMap(MsgCode.DATA_PSWD_ERROR);
			
		Map<String, Object> resMap = userSvc.doLogin(params, req, res, session);
		return resMap;
	}
	
	/**
	 * 비밀번호 변경요청에 폼 > 비밀번호 변경 Ajax.
	 */
	@ResponseBody
	@PostMapping(value="/common/updateUserPswdAjax")
	public Object updateUserPswdAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap = userSvc.updateUserPswd(params, loginDto);
		return resMap;
	}
	
	
	/**
	 * 비밀번호 찾기 팝업 
	 */
	@PostMapping(value="/admin/common/userPswdSearchPop")
	public String userPswdSearchPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "/admin/base/userPswdSearchPop";
	}
	
	
	/**
	 * 비밀번호 찾기 팝업 - 프론트
	 */
	@RequestMapping(value="/front/common/userPswdSearchPop", method={RequestMethod.GET, RequestMethod.POST})
	public String frontUserPswdSearchPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "/front/base/userPswdSearchPop";
	}
	
	/**
	 * 비밀번호 찾기 Ajax.
	 */
	@ResponseBody
	@PostMapping(value="/common/userPswdSearchAjax")
	public Object userPswdSearchAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, HttpSession session) throws Exception {
		Map<String, Object> resMap = userSvc.userPswdSearchTransaction(params, req, session);
		return resMap;
	}
	
	/**
	 * 납품처계정 자동생성 Ajax.
	 * @작성일 : 2020. 3. 23.
	 * @작성자 : an
	 */
	@ResponseBody
	@PostMapping(value="/common/insertShiptoUserAjax")
	public Object insertShiptoUserAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return userSvc.insertShiptoUserTransaction(params, req, loginDto);
	}

	/**
	 * 이미지팝업
	 * @작성일 : 2020. 4. 22.
	 * @작성자 : isaac
	 */
	@GetMapping(value="common/imagePop")
	public String imagePop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		model.addAttribute("popOne", boardSvc.getPopupOne(params));
		return "/front/base/imagePop";
	}

	/**
	 * 이용수칙 팝업
	 * @작성일 : 2020. 4. 23.
	 * @작성자 : isaac
	 */
	@GetMapping(value="common/termsOfServicePop")
	public String termsOfServicePop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		model.addAttribute("restoreXSSContent", HttpUtils.restoreXss(Converter.toStr(boardSvc.getTosConfigOneForIndex(params).getTSC_CONTENT())));
		return "/front/base/termsOfServicePop";
	}

	/**
	 * 개인정보취급방침 팝업 (현재 하드코딩)
	 * @작성일 : 2020. 4. 24.
	 * @작성자 : isaac
	 */
	@GetMapping(value="common/privacyPolicyPop")
	public String privacyPolicyPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "/front/base/privacyPolicyPop";
	}
	
	/**
	 * 배송조회 팝업
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value="/common/pop/deliveryTrackingPop", method={RequestMethod.GET, RequestMethod.POST})
	public String deliveryTrackingPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		
		Map<String, Object> salesOrder = orderSvc.getSalesOrder(params);
		logger.debug("salesOrder : {}", salesOrder);
		
		String truckNo = Converter.toStr(salesOrder.get("TRUCK_NO"));
		String driverName = "";
		if(!StringUtils.equals("", truckNo)) {
			driverName = truckNo.substring(9, truckNo.length()); // ex.정우성
			truckNo = truckNo.substring(0, 9); // ex.서울90바5516
		}
		model.addAttribute("r_truckno", truckNo);
		model.addAttribute("r_plantdesc", Converter.toStr(salesOrder.get("PLANT_DESC")));
		model.addAttribute("r_actualshipdt", Converter.toStr(salesOrder.get("ACTUAL_SHIP_DT")));
		model.addAttribute("r_driverphone", Converter.toStr(salesOrder.get("DRIVER_PHONE")));
		model.addAttribute("r_drivername", driverName);
		model.addAttribute("r_add1", Converter.toStr(salesOrder.get("ADD1")));
		
		params.put("r_truckno", truckNo);
		
		// 한진.
		if(StringUtils.equals("H", Converter.toStr(params.get("r_delitype")))) {
			String retStr = commonSvc.hanjinDelivery(params);
			if(!StringUtils.equals("", retStr)) {
				HashMap<String, Object> retMap = new ObjectMapper().readValue(retStr, HashMap.class) ;
				List<HashMap<String, Object>> retMapList = (List<HashMap<String, Object>>)retMap.get("locations");
				HashMap<String, Object> resultMap = retMapList.get(0);
				logger.debug("retMap : {}", retMap);
				logger.debug("resultMap : {}", resultMap);
				model.addAttribute("x", resultMap.get("longitude"));
				model.addAttribute("y", resultMap.get("latitude"));
			}
			logger.debug("retStr : {}", retStr);
		}
		// 동원.
		if(StringUtils.equals("D", Converter.toStr(params.get("r_delitype")))) {
			//params.put("r_truckNo", params.get("r_truckno"));
			String retStr = commonSvc.dongwonDelivery(params); 
			if(!StringUtils.equals("", retStr)) {
				HashMap<String, Object> retMap = new ObjectMapper().readValue(retStr, HashMap.class) ;
				HashMap<String, Object> resultMap = (HashMap)retMap.get("result");
				logger.debug("retMap : {}", retMap);
				logger.debug("resultMap : {}", resultMap);
				model.addAttribute("x", resultMap.get("lon"));
				model.addAttribute("y", resultMap.get("lat"));
			}
			logger.debug("retStr : {}", retStr);
		}
		
		model.addAttribute("kakaoMapAppKey", kakaoMapAppKey);
		
		return "common/deliveryTrackingPop";
	}
	
	/**
	 * 배송조회 Ajax
	 */
	@ResponseBody
	@PostMapping(value="/common/delTrackingAjax")
	public Object delTrackingAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		if(StringUtils.equals("1", Converter.toStr(params.get("type")))) {
			return commonSvc.hanjinDelivery(params);  //한진
		}else {
			return commonSvc.dongwonDelivery(params); //동원
		}
	}
	
	private static String aesDecrpytion(String encPw) {		
		String encryptionKey = "Knauf2023";
        
		try {
            final int keySize = 256;
            final int ivSize = 128;
 
            // 텍스트를 BASE64 형식으로 디코드 한다.
            byte[] ctBytes = Base64.decodeBase64(encPw.getBytes("UTF-8"));
 
            // 솔트를 구한다. (생략된 8비트는 Salted__ 시작되는 문자열이다.) 
            byte[] saltBytes = Arrays.copyOfRange(ctBytes, 8, 16);
            System.out.println( Hex.encodeHexString(saltBytes) );
            
            // 암호화된 테스트를 구한다.( 솔트값 이후가 암호화된 텍스트 값이다.)
            byte[] ciphertextBytes = Arrays.copyOfRange(ctBytes, 16, ctBytes.length);
                       
            // 비밀번호와 솔트에서 키와 IV값을 가져온다.
            byte[] key = new byte[keySize / 8];
            byte[] iv = new byte[ivSize / 8];
            EvpKDF(encryptionKey.getBytes("UTF-8"), keySize, ivSize, saltBytes, key, iv);
            
            // 복호화 
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] recoveredPlaintextBytes = cipher.doFinal(ciphertextBytes);
 
            System.out.println(new String(recoveredPlaintextBytes));
            return new String(recoveredPlaintextBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return null;
	}
	
	private static byte[] EvpKDF(byte[] password, int keySize, int ivSize, byte[] salt, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
        return EvpKDF(password, keySize, ivSize, salt, 1, "MD5", resultKey, resultIv);
    }
 
    private static byte[] EvpKDF(byte[] password, int keySize, int ivSize, byte[] salt, int iterations, String hashAlgorithm, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
        keySize = keySize / 32;
        ivSize = ivSize / 32;
        int targetKeySize = keySize + ivSize;
        byte[] derivedBytes = new byte[targetKeySize * 4];
        int numberOfDerivedWords = 0;
        byte[] block = null;
        MessageDigest hasher = MessageDigest.getInstance(hashAlgorithm);
        while (numberOfDerivedWords < targetKeySize) {
            if (block != null) {
                hasher.update(block);
            }
            hasher.update(password);            
            // Salting 
            block = hasher.digest(salt);
            hasher.reset();
            // Iterations : 키 스트레칭(key stretching)  
            for (int i = 1; i < iterations; i++) {
                block = hasher.digest(block);
                hasher.reset();
            }
            System.arraycopy(block, 0, derivedBytes, numberOfDerivedWords * 4, Math.min(block.length, (targetKeySize - numberOfDerivedWords) * 4));
            numberOfDerivedWords += block.length / 4;
        }
        System.arraycopy(derivedBytes, 0, resultKey, 0, keySize * 4);
        System.arraycopy(derivedBytes, keySize * 4, resultIv, 0, ivSize * 4);
        return derivedBytes; // key + iv
    }
    
    
    
	/**
	 * Accumulated Volume 테이블 화면.
	 */
	@GetMapping(value="/accumsap/accumlated_volume")
	public String accumlated_volume(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, HttpSession session, Model model) throws Exception {		
		
		Device device = DeviceUtils.getCurrentDevice(req);
		String accessDevice = "pc";
		String ip = "";
		if(device.isMobile() || device.isTablet()) {
			ip = AccumSapUtils.getMobileClientIp(req);
			String[] info = ip.split(":");
			if(info.length != 1) {
				return "/error_sap";
			}
		} else {
			ip = AccumSapUtils.getClientIp(req);
			if(!ip.equalsIgnoreCase("REMOTE_ADDR")) {
				return "/error_sap";
			}
		}
		
		Map<String,Object> resMap =  accumSapSvc.getNotShippedVolume(null);
		model.addAttribute("fromDt", resMap.get("fromDt"));
		model.addAttribute("toDt", resMap.get("toDt"));
		model.addAttribute("nShippedVolumes", resMap.get("nShippedVolumes"));
		model.addAttribute("dailyAccumlist", resMap.get("dailyAccumlist"));
		
//		model.addAttribute("device", accessDevice);
//		model.addAttribute("ip", ip);

		return "/accumsap/ACCUMULATED_VOLUME";
	}
}

