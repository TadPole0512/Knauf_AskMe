package com.limenets.common.exception;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class MsgCode {
	public static final MsgCode SUCCESS = new MsgCode("0000", "정상처리 되었습니다.");
	public static final MsgCode ALERT = new MsgCode("0001", "%s");
	public static final MsgCode ERROR = new MsgCode("9998", "%s");
	public static final MsgCode SYS_ERROR = new MsgCode("9999", "시스템에러가 발생하였습니다.");
	
	/* 01** 로그인, 세션, 권한 관련 */
	public static final MsgCode ACCOUNT_ERROR = new MsgCode("0100", "계정정보가 일치하지 않습니다.");
	public static final MsgCode SESSION_DENY_ERROR = new MsgCode("0101", "로그인 시간이 지났습니다. 다시 로그인해 주세요."); //세션이 만료 되었습니다.
	public static final MsgCode AUTH_DENY_ERROR = new MsgCode("0102", "권한이 없습니다.");
	public static final MsgCode PAGE_SET_ERROR = new MsgCode("0103", "페이지 권한이 설정되지 않았습니다.");
	public static final MsgCode LOGIN_TOKEN_ERROR = new MsgCode("0104", "로그인 토큰이 유효하지 않습니다.");
	public static final MsgCode ACCOUNT_INVALID_ERROR = new MsgCode("0105", "등록되지 않은 고객정보입니다");
	public static final MsgCode ACCOUNT_READY_ERROR = new MsgCode("0106", "이미 아이디가 접속중 입니다.");
	public static final MsgCode ACCOUNT_WARNING_ERROR = new MsgCode("0107", "계정정보가 일치하지 않습니다.\n%s회이상 로그인 정보가 일치하지 않을 경우 자동 잠금 처리됩니다.");
	public static final MsgCode LOGIN_TOKEN_FALSIFY = new MsgCode("0108", "로그인 토큰이 변조 되었습니다..");
	
	/* 02** 파일관련 */
	public static final MsgCode FILE_DENY_ERROR = new MsgCode("0200", "첨부할 수 없는 확장자 입니다.");
	public static final MsgCode FILE_EXT_CHK_ERROR = new MsgCode("0201", "첨부할 수 없는 확장자 입니다.");
	public static final MsgCode FILE_UPLOAD_ERROR = new MsgCode("0202", "파일 업로드시 에러가 발생하였습니다.");
	public static final MsgCode FILE_REMOVE_ERROR = new MsgCode("0203", "삭제시 에러가 발생하였습니다.");
	public static final MsgCode FILE_REQUEST_ERROR = new MsgCode("0204", "파일폼이 아닙니다.");
	public static final MsgCode FILE_MAX_SIZE_ERROR = new MsgCode("0205", "파일 업로드 최대용량을 초과하였습니다.");
	public static final MsgCode FILE_NOT_FOUND_ERROR = new MsgCode("0206", "파일이 존재하지 않습니다.");
	
	/* 03** 데이터 관련 */
	public static final MsgCode DATA_NOT_FOUND_ERROR = new MsgCode("0300", "존재하지 않는 데이터 입니다.");
	public static final MsgCode DATA_REQUIRE_ERROR = new MsgCode("0301", "%s는 필수입니다.");
	public static final MsgCode DATA_VALIDATION_ERROR = new MsgCode("0302", "%s의 유형성 체크를 만족하지 않습니다.");
	public static final MsgCode DATA_PROCESS_ERROR = new MsgCode("0303", "처리할 수 없는 데이터입니다.");
	public static final MsgCode DATA_OVERLAP_ERROR = new MsgCode("0304", "%s는 중복된 데이터입니다.");
	public static final MsgCode DATA_AUTH_ERROR = new MsgCode("0305", "권한 없는 데이터를 요청하였습니다.");
	public static final MsgCode DATA_STATUS_ERROR = new MsgCode("0306", "처리할 수 없는 상태입니다.");
	public static final MsgCode DATA_NOT_COMPLETE_ERROR = new MsgCode("0307", "%s가 완료되지 않았습니다.");
	public static final MsgCode DATA_ACCESS_ERROR = new MsgCode("0308", "잘못된 접근 입니다.");
	public static final MsgCode DATA_NOT_FIND_ERROR = new MsgCode("0309", "%s이 존재하지 않습니다.");
	public static final MsgCode DATA_REQUIRE_ERROR2 = new MsgCode("0310", "필수 데이터가 누락 되었습니다.");
	
	public static final MsgCode DATA_REUSE_ERROR = new MsgCode("0311", "최근 %s회 이내 사용한 비밀번호는 재사용 불가합니다.");
	public static final MsgCode DATA_CHANGE_ERROR = new MsgCode("0312", "비밀번호가 %s개월이상 변경되지 않았습니다.");
	public static final MsgCode DATA_PSWD_ERROR = new MsgCode("0313", "현재 패스워드가 일치하지 않습니다.");
	public static final MsgCode DATA_LOCK_ERROR = new MsgCode("0314", "로그인시도 횟수가 초과되어 계정이 잠금 처리되었습니다.");
	public static final MsgCode DATA_PSWD_SUCCESS = new MsgCode("0315", "성공적으로 패스워드가 변경되었습니다.");
	public static final MsgCode DATA_FIRST_LOGIN = new MsgCode("0316", "개인정보 입력후 이용해 주시기 바랍니다.");
	public static final MsgCode DATA_PSWD_CHANGE = new MsgCode("0317", "임시 비밀번호 입니다.");
	public static final MsgCode DATA_PSWD_EMAIL = new MsgCode("0318", "등록된 이메일로 초기화된 비밀번호를 전송하였습니다.");
	public static final MsgCode DATA_NOT_USE = new MsgCode("0319", "해당계정은 비활성화된 계정입니다.");
	public static final MsgCode DATA_ORDER_MODIFY = new MsgCode("0320", "주문내용이 변경되었습니다.");

	public static final MsgCode DATA_EXCEL_NOT_FIND_ERROR = new MsgCode("0320", "%s행 품목코드는 등록되지 않은 코드입니다.");
	
	public static final MsgCode DATA_LIMIT_CONFIRM_TIME_ERROR = new MsgCode("0330", "지금은 주문처리 시간이 아닙니다.");
	public static final MsgCode DATA_LIMIT_CONFIRM_DATE_ERROR = new MsgCode("0340", "지금은 주문처리 일자가 아닙니다.");
	
	private final String code;
	private final String message;
	
	
	public MsgCode(String code, String message) {
		super();
		this.code = code;
		this.message = message;
	}
	
	public static Map<String, Object> getResultMap(MsgCode msgCode) {
		Map<String, Object> resMap = new HashMap<>();
		resMap.put("RES_CODE", msgCode.getCode());
		resMap.put("RES_MSG", msgCode.getMessage());
		return resMap;
	}

	public static Map<String, Object> getResultMap(MsgCode msgCode, Object... txtParam) {
		Map<String, Object> resMap = new HashMap<>();
		resMap.put("RES_CODE", msgCode.getCode());
		resMap.put("RES_MSG", String.format(msgCode.getMessage(), txtParam));
		return resMap;
	}
	
	public String getCode(){
		return code;
	}
	
	public String getMessage(){
		return message;
	}
	
	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		
		MsgCode other = (MsgCode) obj;
		if(code == null){
			if(other.code != null){
				return false;
			}
		}else if(!code.equals(other.code)){
			return false;
		}
		return false;
	}
	
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
