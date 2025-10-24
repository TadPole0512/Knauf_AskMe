package com.limenets.eorder.ctrl.admin;

//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import javax.crypto.Cipher;
//import javax.crypto.spec.IvParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.codec.binary.Hex;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.limenets.common.dto.LoginDto;
import com.limenets.common.util.Converter;
import com.limenets.eorder.ctrl.common.TestCtrl;
import com.limenets.eorder.svc.BoardSvc;
import com.limenets.eorder.svc.OrderSvc;
import com.limenets.eorder.svc.UserSvc;
import com.limenets.eorder.util.StatusUtil;

/**
 * Admin 인덱스 컨트롤러.
 */
@Controller
@RequestMapping("/admin/index/*")
public class IndexCtrl {
//	private static final Logger logger = LoggerFactory.getLogger(IndexCtrl.class);

	@Inject private BoardSvc boardSvc;
	@Inject private UserSvc userSvc;
	@Inject private OrderSvc orderSvc;
	
	@Inject private TestCtrl testCtrl;

	/**
	 * 메인 인덱스 폼.
	 */
	//@GetMapping(value="index")
	@RequestMapping(value="index", method={RequestMethod.GET, RequestMethod.POST})
	public String index(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		model.addAttribute("orderStatus", StatusUtil.ORDER.getMap()); // 주문상태 Map형태로 가져오기.
		model.addAttribute("salesOrderStatus", StatusUtil.SALESORDER.getMap()); // 출하상태 List<Map>형태로 가져오기.
		
		// 공지사항
		model.addAllAttributes(boardSvc.getBoardListForIndex("notice","1","10"));
		
//		String decPw = aesDecrpytion(params.get("r_userpwd").toString());
//		params.put("r_userpwd", decPw);
		
		// 사용자
		model.addAttribute("userMap", userSvc.getUserMap(loginDto.getUserId()));
		
		// 가장 마지막에 놓자...
		// 내부사용자 웹주문현황  > 별도 권한 설정.
		//orderSvc.setParamsForAdminOrderList(params, req, loginDto, model); // 내부용 인덱스는 모두 동일하게 보여주는걸로 변경. 2020-05-20.
		String today = Converter.dateToStr("yyyy-MM-dd");
		String today2 = Converter.dateToStr("yyyyMMdd");
		
		// 주문접수 리스트
		params.put("r_statuscd", "00");
		params.put("r_startrow", 1);
		params.put("r_endrow", 5);
//	<!-- 2024-10-14 HSG 홈화면에서 별칭(COH)이 오류가 남. 별칭을 'XX'로 수정 -->
//		params.put("r_orderby", "COH.INDATE DESC ");
		params.put("r_orderby", "XX.INDATE DESC ");
		List<Map<String, Object>> listFor00 = orderSvc.getCustOrderHList(params);
		params.put("r_startrow", "");
		params.put("r_endrow", "");
		params.put("r_orderby", "");
		
		// 웹주문현황[TODAY]
		params.put("r_insdate", today);
		params.put("r_inedate", today);
		int cntFor00 = orderSvc.getCustOrderHCnt(params); // 주문접수.
		
		params.put("r_ordersdt", today2);
		params.put("r_orderedt", today2);
		params.put("wherebody_status", "STATUS2 IN (522, 525, 527) ");
		int cntFor522 = orderSvc.getSalesOrderGroupCnt(params); // 주문확정 522 >> 주문확정 재정의 : 오더접수+신용체크+출하접수
		//int cntFor522 = orderSvc.getSalesOrderCnt(params); // 주문확정.
		
		params.put("wherebody_status", "STATUS2 IN (530) ");
		int cntFor530 = orderSvc.getSalesOrderGroupCnt(params); // 배차완료.
		
		params.put("wherebody_status", "STATUS_DESC = '출하완료' ");
		//params.put("wherebody_status", "(STATUS2 >= 560) ");
		int cntFor560 = orderSvc.getSalesOrderGroupCnt(params); // 출하완료.
		
		model.addAttribute("listFor00", listFor00);
		model.addAttribute("cntFor00", cntFor00);
		model.addAttribute("cntFor522", cntFor522);
		model.addAttribute("cntFor530", cntFor530);
		model.addAttribute("cntFor560", cntFor560);
		
		// For MSSQL Query test
		//testCtrl.test1(params, req, res, loginDto, model);
		
		return "admin/index/index";
	}
	
	@RequestMapping(value="gotoIndex", method={RequestMethod.GET, RequestMethod.POST})
	public String gotoIndex(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		return "redirect:/admin/index/index";
	}
	
	/*private static String aesDecrpytion(String encPw) {		
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
    }*/
}
