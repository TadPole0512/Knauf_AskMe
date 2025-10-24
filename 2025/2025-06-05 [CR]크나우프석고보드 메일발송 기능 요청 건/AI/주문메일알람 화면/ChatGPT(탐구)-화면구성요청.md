아래 1번과 2번의 내용을 3번 소스에 적용하고 싶은데 네게 어떤식으로 요청을 하면 될지 알려줘.(THINK HARD)

# 주문메일알람
## 1. 화면
###   [조회조건]
     - 거래처코드, 거래처명, 영업담당

###   [jqGrid(주문메일알람)]
     - 거래처코드, 거래처명, 담당자, 담장자 이메일, 담당자 이메일 발송 여부, 영업 담당 이메일, 영업 담당 이메일 발송 여부, 비고

```js
     소스)
       // 기본 컬럼 모델 정의
       var defaultColModel = [
           { name: "CUST_CD", key: true, label: '코드', width: 120, align: 'center', sortable: true },
           { name: "CUST_NM", label: '거래처명', width: 220, align: 'left', sortable: true },
           { name: "CUST_MAIN_EMAIL", label: '담당자 이메일', width: 220, align: 'center', sortable: true }, //<-- 수정 가능. 메일 형식 유효성 체크
           { name: "CUST_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center', sortable: true }, //<-- 체크박스. 선택/해제 토글
           { name: "SALESREP_NM", label: '영업 담당', width: 100, align: 'center', sortable: true },
           { name: "SALESREP_EMAIL", label: '영업 담당 이메일', width: 300, align: 'center', sortable: true },//<-- 수정 가능. 메일 형식 유효성 체크
           { name: "SALESREP_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center', sortable: true }, //<-- 체크박스. 선택/해제 토글
   		{ name: "COMMENTS", label: '비고', width: 450, align: 'left', sortable: true } //<-- 수정 가능
       ];
```
## 2. 동작
###   2.1. 조회
        - 조회 조건에 입력된 내용으로 조문 메일 알람을 조회한다.
		- 거래처현황에 등록된 거래처를 조회 대상으로 한다.

###   2.2. jqGrid(목록)
        - 담당자 이메일(CUST_MAIN_EMAIL)과 영업 담당 이메일(SALESREP_EMAIL), 비고(COMMENTS)를 수정한다.
		- 담당자 이메일(CUST_MAIN_EMAIL)과 영업 담당 이메일(SALESREP_EMAIL)을 수정할 경우 메일 형식 유효성 검사를 진행한다.
		- 발송여부(CUST_SENDMAIL_YN)과 발송여부(SALESREP_SENDMAIL_YN)는 체크박스로 사용자가 선택/해제할 수 있다.
		- 사용자에 의해 내용이 수정되거나 체크박스의 선택/해제 될 경우 줄 배경색이 바뀐다.
		- 사용자가 내용을 원래대로 되돌리거나 체크박스를 원상태로 하면 배경색을 원래대로 바꾼다.

###   2.3. 유효성 검사
        - 수정된 행이 없을 경우 '수정된 내용이 없습니다.'라는 경고창을 띄운다.
		- 수정된 행의 담당자 이메일(CUST_MAIN_EMAIL)과 영업 담당 이메일(SALESREP_EMAIL)에 대한 메일 형식 유효성 검사를 진행한다.
		- 발송여부(CUST_SENDMAIL_YN)가 체크되어 있을 경우 담당자 이메일(CUST_MAIN_EMAIL)에 값이 있는지와
		  발송여부(SALESREP_SENDMAIL_YN)가 체크되어 있을 경우 영업 담당 이메일(SALESREP_EMAIL)에 값이 있는지 검사한다.
		- 여러 행이 수정되었을 경우 위의 내용을 수정된 행에 대해서 수행한다.


## 3. 소스
```html
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

<script type="text/javascript" src="${url}/include/js/common/select2/select2.js"></script>
<link rel="stylesheet" href="${url}/include/js/common/select2/select2.css" />

<style>
    .changed-row {
        background-color: #D1ECF1 !important;
    }

    .selected-row {
        background-color: #E8F5E8 !important;  /* 연한 초록색 - 선택된 행 */
    }

    .small-checkbox input[type="checkbox"] {
        width: 16px;
        height: 16px;
    }

    .invalid-input {
        outline: 2px solid #e57373 !important;  /* 빨간 테두리 */
        background-color: #fdecea !important;   /* 연한 빨간 배경 */
    }
</style>

<script type="text/javascript">

</script>

</head>

<body class="page-header-fixed compact-menu">

    <!-- Page Content -->
    <main class="page-content content-wrap">

        <%@ include file="/WEB-INF/views/include/admin/header.jsp" %>
        <%@ include file="/WEB-INF/views/include/admin/left.jsp" %>

        <%-- 임의 form --%>
        <form name="iForm" method="post"></form>

        <form name="frm" method="post">

            <!-- Page Inner -->
            <div class="page-inner">
                <div class="page-title">
                    <h3>
                        거래처현황
                        <div class="page-right">
                            <button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();">
                                <i class="fa fa-refresh"></i><em>새로고침</em>
                            </button>
                            <button type="button" class="btn btn-line f-black" title="엑셀다운로드" onclick="excelDown(this);">
                                <i class="fa fa-file-excel-o"></i><em>엑셀다운로드</em>
                            </button>
                        </div>
                    </h3>
                </div>

                <!-- Main Wrapper -->
                <div id="main-wrapper">
                    <!-- Row -->
                    <div class="row">
                        <div class="col-md-12">
                            <div class="panel panel-white">
                                <div class="panel-body no-p">
                                    <div class="tableSearch">
                                        <div class="topSearch">
                                            <ul>
                                                <li>
                                                    <label class="search-h">거래처코드</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="rl_custcd"
                                                               value="${param.rl_custcd}"
                                                               onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">거래처명</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="rl_custnm"
                                                               value="${param.rl_custnm}"
                                                               onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">영업담당</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="rl_salesrepnm"
                                                               value="${param.rl_salesrepnm}"
                                                               onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">메일발송여부</label>
                                                    <div class="search-c checkbox">
                                                        <label>
                                                            <input type="checkbox" name="r_salesrepcdyn" value="Y" onclick="dataSearch();" />Y
                                                        </label>
                                                        <label>
                                                            <input type="checkbox" name="r_salesrepcdyn" value="N" onclick="dataSearch();" />N
                                                        </label>
                                                    </div>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>

                                <div class="panel-body">
                                    <h5 class="table-title listT">
                                        TOTAL <span id="listTotalCountSpanId">0</span>EA
                                    </h5>
                                    <div class="btnList writeObjectClass">
                                        <%-- 예약발송 시각 설정 --%>
                                        <label>예약발송 시간:</label>
                                        <select name="scheduleTime">
                                            <option value="">선택</option>
                                            <% 
                                            String currentHour = request.getParameter("scheduleTime") != null ? request.getParameter("scheduleTime") : "";
                                            for(int i = 0; i <= 23; i++) { 
                                                String hourStr = String.format("%02d", i);
                                                String selected = hourStr.equals(currentHour) ? "selected" : "";
                                            %>
                                                <option value="<%= hourStr %>" <%= selected %>><%= hourStr %>시</option>
                                            <% } %>
                                        </select>
                                        <select name="scheduleMinute">
                                            <option value="">선택</option>
                                            <% 
                                            String currentMinute = request.getParameter("scheduleMinute") != null ? request.getParameter("scheduleMinute") : "";
                                            int[] minutes = {0, 10, 20, 30, 40, 50};
                                            for(int minute : minutes) { 
                                                String minuteStr = String.format("%02d", minute);
                                                String selected = minuteStr.equals(currentMinute) ? "selected" : "";
                                            %>
                                                <option value="<%= minuteStr %>" <%= selected %>><%= minuteStr %>분</option>
                                            <% } %>
                                        </select>
                                        <button type="button" class="btn btn-info" onclick="dataInUp(this, '');">저장</button>
                                    </div>
                                    <div class="table-responsive in">
                                        <table id="gridList" class="display table tableList nowrap"
                                               width="100%" border="0" cellpadding="0" cellspacing="0"></table>
                                        <div id="pager"></div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>
                    <!-- //Row -->
                </div>
                <!-- //Main Wrapper -->

                <%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>

            </div>

        </form>
        <!-- //Page Inner -->
    </main>
    <!-- //Page Content -->

</body>

</html>
```

---------------------------------------------------------------------

좋아, “어떻게 요청하면 내가 ①화면 요구사항과 ②동작/검증 로직을 ③소스에 정확히 반영해 주는가”가 핵심이지. 아래 템플릿 그대로 복붙해서 보내면 바로 작업에 들어갈 수 있어. 환경(Windows/STS4.5/Java8/Tomcat9/MSSQL2019/MyBatis/jqGrid) 기준으로 처리할게.

# 요청 템플릿(바로 복붙용)

```
[목표]
아래 1,2의 요구사항을 3의 JSP 소스에 반영해서 동작하도록 만들어줘.
- 환경: Windows, STS4.5, Java 8 (Zulu-8), Tomcat 9.0.99, MSSQL 2019(Std), Maven, MyBatis, jqGrid, GitHub Desktop
- 페이지 목적: 주문메일알람 관리(조회/수정/유효성검사/저장)

[1. 화면 요구사항]
- 조회조건: 거래처코드, 거래처명, 영업담당
- jqGrid 컬럼: 
  CUST_CD(키), CUST_NM, CUST_MAIN_EMAIL(편집/메일형식검사),
  CUST_SENDMAIL_YN(체크박스), SALESREP_NM,
  SALESREP_EMAIL(편집/메일형식검사),
  SALESREP_SENDMAIL_YN(체크박스), COMMENTS(편집)
- 수정/체크 변경 시 행 배경색 변경, 원복 시 색상 원복

[2. 동작/검증]
- 조회: 조회조건으로 거래처현황 기준 데이터 조회
- 목록 편집: 위 편집 가능 필드 inline 편집
- 체크박스: CUST_SENDMAIL_YN, SALESREP_SENDMAIL_YN 토글
- 유효성 검사:
  * 수정된 행 없으면 경고 “수정된 내용이 없습니다.”
  * 이메일 필드(담당/영업) 형식 검사
  * CUST_SENDMAIL_YN=Y → CUST_MAIN_EMAIL 필수
  * SALESREP_SENDMAIL_YN=Y → SALESREP_EMAIL 필수
  * 여러 행 수정 시 각 행별 검사
- 저장: 수정된 행만 모아 Ajax로 일괄 저장(트랜잭션)

[3. 현재 JSP 소스]
(여기에 네가 준 3번 소스 전체 붙여넣기)

[추가 구현 지시]
- jqGrid 옵션: cellEdit 또는 inlineNav 중 권장 방식 적용, Enter/Blur 저장(로컬), 서버 저장은 [저장] 버튼으로 일괄
- 메일 정규식: RFC 완벽 X, 실무형(대소문자/숫자/._%+-@도메인.최상위) 사용
- 변경 추적: originalData 스냅샷 vs currentData diff, 행별 changed-row 클래스 토글
- 체크박스 렌더러: Y/N ↔ 체크/언체크 양방향 바인딩
- 예약발송 시간/분 파라미터도 함께 전송(선택 시)
- 백엔드: Controller, Service, MyBatis Mapper/SQL, DTO 생성
  * SELECT: 검색조건 동적 WHERE
  * UPDATE: 변경된 컬럼만 업데이트(SET 절 동적), 업데이트 수/에러 행 반환
  * 트랜잭션: 전체 성공/부분 실패 시 실패 행 목록 반환
- 예외/알림 UX:
  * 검증 실패: 해당 셀 invalid 스타일 강조 및 스크롤 포커스
  * 저장 결과: 성공/실패 건수 토스트, 실패 행 이유 표시
- 성능: 서버 페이징(페이지당 200), 정렬/검색 서버측 처리
- 로깅: 변경 전/후 값, 사용자ID, IP 기록(테이블: MAIL_ALARM_AUDIT)
- 보안: 입력값 XSS sanitize, 이메일 320자 제한, 코멘트 500자 제한
- 테스트: 프론트/백엔드 단위 시나리오 포함해서 작성

[산출물 형식]
1) 변경된 JSP(스크립트 포함) 전체
2) JS 모듈(필요 시 분리) 전체
3) Controller/Service/Mapper/Mapper.xml/DTO 전체 코드
4) DDL(감사테이블), 샘플 파라미터 예시, Postman 컬렉션 JSON
5) README(빌드/배포/롤백/테스트 방법)

[주의]
- 내 개발환경(STS4.5, Tomcat 9.0.99, MSSQL 2019)에서 그대로 빌드/배포 가능해야 함
- 라이브러리(jqGrid/Select2) 버전 충돌 없게 작성
```

# 체크리스트(요청 보낼 때 옵션)

* 저장 방식: \[ ] 행단위 즉시 저장  \[x] 버튼으로 일괄 저장
* 편집 모드: \[x] cellEdit  \[ ] inlineRow
* 서버 페이징: \[x] 사용(200/페이지)  \[ ] 미사용
* 실패 처리: \[x] 부분 실패 허용 후 실패행만 재시도  \[ ] 올오어낫싱 롤백

# 테스트 시나리오(자동/수동)

1. 수정 없음 → 저장 → “수정된 내용이 없습니다.”
2. 이메일 잘못 입력 → 저장 → 해당 셀 강조 + 메시지
3. CUST\_SENDMAIL\_YN=Y & CUST\_MAIN\_EMAIL='' → 저장 차단
4. 여러 행 수정(정상/오류 혼재) → 결과 토스트 + 실패행 마킹
5. 원복 → 행 배경색 원래대로
6. 서버 페이징/정렬/검색 동작 확인

---

## 내가 왜 이렇게 구성했는지(생각 과정 요약)

* **Genius Insight (GI)**
  관찰(O)=8: 너의 1·2번 요구가 구체적임, 3번은 마크업 중심.
  연결(C)=8: “변경 추적/검증/일괄저장/UX”를 한 묶음으로 연결.
  패턴(P)=9: jqGrid 실무 패턴(셀 편집+로컬 diff+Ajax batch).
  종합(S)=8: 프론트·백·DB 산출물 패키징.
  편견/바이어스(A+B)=3+2로 낮춤 → GI ↑. 이 점수를 바탕으로 요청 템플릿에 “산출물·검증·로깅·성능”까지 포함시켰어.

* **MDA(다차원 분석)**
  D1 시간(과거 실무 패턴→현재 구현→향후 유지보수),
  D3 추상(요구→컴포넌트→코드),
  D4 인과(요구→검증/저장→감사/에러 핸들),
  D5 계층(뷰/서비스/DAO/DB). 각 가중치를 높여 템플릿 구조화.

* **Creative Connection Matrix**
  “UI 변경색상” ↔ “데이터 diff”를 연결하고, “체크박스 Y/N” ↔ “이메일 필수”의 규칙을 명시적으로 매핑하도록 요청 항목에 포함.

* **Problem Redefinition**
  “코드를 짜줘”에서 “재현 가능한 산출물 패키지와 테스트 포함”으로 문제 정의를 상향.

* **IS(혁신적 해법)**
  기존 jqGrid 패턴 + 감사로그 + 부분실패 재시도 옵션 조합 → 실무 가치/실행가능성↑.

* **IA/TE/CS/IL/IW**
  반복 적용을 고려해 체크리스트·테스트 시나리오·주의사항을 같이 요청하도록 설계(재사용/확장/품질 담보).

---

필요하면 위 템플릿에서 체크박스만 바꿔서 다시 보내. 그러면 바로 전체 코드(프론트/백/SQL/README)까지 한 번에 만들어서 줄게.


---------------------------------------------------------------------


아래 1번과 2번의 내용을 3번 소스에 적용하고 싶은데 네게 어떤식으로 요청을 하면 될지 알려줘.(THINK HARD)
단 jsp와 javascript를 포함한 화면에서의 동작만 대상으로 해줘. 

# 주문메일알람
## 1. 화면
###   [조회조건]
     - 거래처코드, 거래처명, 영업담당

###   [jqGrid(주문메일알람)]
     - 거래처코드, 거래처명, 담당자, 담장자 이메일, 담당자 이메일 발송 여부, 영업 담당 이메일, 영업 담당 이메일 발송 여부, 비고

```js
     소스)
       // 기본 컬럼 모델 정의
       var defaultColModel = [
           { name: "CUST_CD", key: true, label: '코드', width: 120, align: 'center', sortable: true },
           { name: "CUST_NM", label: '거래처명', width: 220, align: 'left', sortable: true },
           { name: "CUST_MAIN_EMAIL", label: '담당자 이메일', width: 220, align: 'center', sortable: true }, //<-- 수정 가능. 메일 형식 유효성 체크
           { name: "CUST_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center', sortable: true }, //<-- 체크박스. 선택/해제 토글
           { name: "SALESREP_NM", label: '영업 담당', width: 100, align: 'center', sortable: true },
           { name: "SALESREP_EMAIL", label: '영업 담당 이메일', width: 300, align: 'center', sortable: true },//<-- 수정 가능. 메일 형식 유효성 체크
           { name: "SALESREP_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center', sortable: true }, //<-- 체크박스. 선택/해제 토글
   		{ name: "COMMENTS", label: '비고', width: 450, align: 'left', sortable: true } //<-- 수정 가능
       ];
```
## 2. 동작
###   2.1. 조회
        - 조회 조건에 입력된 내용으로 조문 메일 알람을 조회한다.
		- 거래처현황에 등록된 거래처를 조회 대상으로 한다.

###   2.2. jqGrid(목록)
        - 담당자 이메일(CUST_MAIN_EMAIL)과 영업 담당 이메일(SALESREP_EMAIL), 비고(COMMENTS)를 수정한다.
		- 담당자 이메일(CUST_MAIN_EMAIL)과 영업 담당 이메일(SALESREP_EMAIL)을 수정할 경우 메일 형식 유효성 검사를 진행한다.
		- 발송여부(CUST_SENDMAIL_YN)과 발송여부(SALESREP_SENDMAIL_YN)는 체크박스로 사용자가 선택/해제할 수 있다.
		- 사용자에 의해 내용이 수정되거나 체크박스의 선택/해제 될 경우 줄 배경색이 바뀐다.
		- 사용자가 내용을 원래대로 되돌리거나 체크박스를 원상태로 하면 배경색을 원래대로 바꾼다.

###   2.3. 유효성 검사
        - 수정된 행이 없을 경우 '수정된 내용이 없습니다.'라는 경고창을 띄운다.
		- 수정된 행의 담당자 이메일(CUST_MAIN_EMAIL)과 영업 담당 이메일(SALESREP_EMAIL)에 대한 메일 형식 유효성 검사를 진행한다.
		- 발송여부(CUST_SENDMAIL_YN)가 체크되어 있을 경우 담당자 이메일(CUST_MAIN_EMAIL)에 값이 있는지와
		  발송여부(SALESREP_SENDMAIL_YN)가 체크되어 있을 경우 영업 담당 이메일(SALESREP_EMAIL)에 값이 있는지 검사한다.
		- 여러 행이 수정되었을 경우 위의 내용을 수정된 행에 대해서 수행한다.


## 3. 소스
```html
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

<script type="text/javascript" src="${url}/include/js/common/select2/select2.js"></script>
<link rel="stylesheet" href="${url}/include/js/common/select2/select2.css" />

<style>
    .changed-row {
        background-color: #D1ECF1 !important;
    }

    .selected-row {
        background-color: #E8F5E8 !important;  /* 연한 초록색 - 선택된 행 */
    }

    .small-checkbox input[type="checkbox"] {
        width: 16px;
        height: 16px;
    }

    .invalid-input {
        outline: 2px solid #e57373 !important;  /* 빨간 테두리 */
        background-color: #fdecea !important;   /* 연한 빨간 배경 */
    }
</style>

<script type="text/javascript">

</script>

</head>

<body class="page-header-fixed compact-menu">

    <!-- Page Content -->
    <main class="page-content content-wrap">

        <%@ include file="/WEB-INF/views/include/admin/header.jsp" %>
        <%@ include file="/WEB-INF/views/include/admin/left.jsp" %>

        <%-- 임의 form --%>
        <form name="iForm" method="post"></form>

        <form name="frm" method="post">

            <!-- Page Inner -->
            <div class="page-inner">
                <div class="page-title">
                    <h3>
                        거래처현황
                        <div class="page-right">
                            <button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();">
                                <i class="fa fa-refresh"></i><em>새로고침</em>
                            </button>
                            <button type="button" class="btn btn-line f-black" title="엑셀다운로드" onclick="excelDown(this);">
                                <i class="fa fa-file-excel-o"></i><em>엑셀다운로드</em>
                            </button>
                        </div>
                    </h3>
                </div>

                <!-- Main Wrapper -->
                <div id="main-wrapper">
                    <!-- Row -->
                    <div class="row">
                        <div class="col-md-12">
                            <div class="panel panel-white">
                                <div class="panel-body no-p">
                                    <div class="tableSearch">
                                        <div class="topSearch">
                                            <ul>
                                                <li>
                                                    <label class="search-h">거래처코드</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="rl_custcd"
                                                               value="${param.rl_custcd}"
                                                               onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">거래처명</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="rl_custnm"
                                                               value="${param.rl_custnm}"
                                                               onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">영업담당</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="rl_salesrepnm"
                                                               value="${param.rl_salesrepnm}"
                                                               onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">메일발송여부</label>
                                                    <div class="search-c checkbox">
                                                        <label>
                                                            <input type="checkbox" name="r_salesrepcdyn" value="Y" onclick="dataSearch();" />Y
                                                        </label>
                                                        <label>
                                                            <input type="checkbox" name="r_salesrepcdyn" value="N" onclick="dataSearch();" />N
                                                        </label>
                                                    </div>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>

                                <div class="panel-body">
                                    <h5 class="table-title listT">
                                        TOTAL <span id="listTotalCountSpanId">0</span>EA
                                    </h5>
                                    <div class="btnList writeObjectClass">
                                        <%-- 예약발송 시각 설정 --%>
                                        <label>예약발송 시간:</label>
                                        <select name="scheduleTime">
                                            <option value="">선택</option>
                                            <% 
                                            String currentHour = request.getParameter("scheduleTime") != null ? request.getParameter("scheduleTime") : "";
                                            for(int i = 0; i <= 23; i++) { 
                                                String hourStr = String.format("%02d", i);
                                                String selected = hourStr.equals(currentHour) ? "selected" : "";
                                            %>
                                                <option value="<%= hourStr %>" <%= selected %>><%= hourStr %>시</option>
                                            <% } %>
                                        </select>
                                        <select name="scheduleMinute">
                                            <option value="">선택</option>
                                            <% 
                                            String currentMinute = request.getParameter("scheduleMinute") != null ? request.getParameter("scheduleMinute") : "";
                                            int[] minutes = {0, 10, 20, 30, 40, 50};
                                            for(int minute : minutes) { 
                                                String minuteStr = String.format("%02d", minute);
                                                String selected = minuteStr.equals(currentMinute) ? "selected" : "";
                                            %>
                                                <option value="<%= minuteStr %>" <%= selected %>><%= minuteStr %>분</option>
                                            <% } %>
                                        </select>
                                        <button type="button" class="btn btn-info" onclick="dataInUp(this, '');">저장</button>
                                    </div>
                                    <div class="table-responsive in">
                                        <table id="gridList" class="display table tableList nowrap"
                                               width="100%" border="0" cellpadding="0" cellspacing="0"></table>
                                        <div id="pager"></div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>
                    <!-- //Row -->
                </div>
                <!-- //Main Wrapper -->

                <%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>

            </div>

        </form>
        <!-- //Page Inner -->
    </main>
    <!-- //Page Content -->

</body>

</html>
```

---------------------------------------------------------------------

좋아, 핵심은 “네가 원하는 동작(1·2번 사양)”을 “기존 JSP(3번 소스)” 위에 **프런트엔드(JSP+JavaScript)만**으로 완성해 달라고 나한테 정확히 지시하는 거야.
아래 템플릿을 그대로 복붙해서 쓰면 되고, 상황에 따라 “짧은 버전/자세한 버전/부분수정 버전” 중 하나를 골라 보내면 된다.

---

# 어떻게 요청하면 되나? (복붙용)

## 1) 초간단 요청(빠르게)

```
[요청]
아래 1·2번 사양을 3번 JSP 소스에 “프런트엔드(JSP+JS, jqGrid)만”으로 반영해줘.
- 서버통신은 모의처리(또는 주석으로 인터페이스만)하고, 화면 동작과 유효성 검사, 편집/체크박스 토글, 변경행 하이라이트까지 동작하게 완성 코드로 제공해.
- 기존 CSS 클래스(.changed-row, .selected-row, .invalid-input)는 그대로 사용해.
- 최종 산출물은: ①변경된 전체 JSP 완성본, ②핵심 JS 스니펫, ③테스트 절차(수행순서) 순서로 줘.

[사양 1·2 전문]
(내가 보낸 1번 화면/그리드 정의와 2번 동작/유효성 사양을 그대로 붙여넣기)

[기존 3번 JSP 소스]
(내가 보낸 3번 소스를 그대로 붙여넣기)
```

## 2) 자세한 버전(완성도 높게)

```
[목표]
- 1·2번 사양을 3번 JSP에 반영하여 “화면 동작만” 완성.
- jqGrid 셀 편집, 체크박스 토글(Y/N), 이메일 형식검사, 변경행 하이라이트, 원복 시 하이라이트 제거까지 구현.

[환경/제약]
- STS4.5 / Tomcat 9 / jQuery + jqGrid
- 백엔드 호출 금지(또는 모의). 프런트만 구현.
- CSS 클래스: .changed-row, .selected-row, .invalid-input 그대로 사용.

[필수 구현 항목]
1) 조회
   - 상단 검색조건(거래처코드/명/영업담당, 메일발송여부 Y/N)을 읽어 그리드 갱신하는 dataSearch() 구현
   - 실제 데이터는 더미 JSON 사용 또는 AJAX 호출 주석 인터페이스 제공

2) jqGrid 목록
   - 컬럼: CUST_CD(key), CUST_NM, CUST_MAIN_EMAIL(수정/이메일검사), CUST_SENDMAIL_YN(체크박스 Y/N),
           SALESREP_NM, SALESREP_EMAIL(수정/이메일검사), SALESREP_SENDMAIL_YN(체크박스 Y/N), COMMENTS(수정)
   - cellEdit=true, edittype=text/checkbox, formatter=checkbox(값 ‘Y: N’)
   - 편집/체크 토글 시 행 변경 상태 추적(dirty map) 및 .changed-row 적용
   - 원래값으로 되돌리면 .changed-row 제거

3) 유효성 검사
   - 저장(dataInUp) 시: 변경행 없으면 alert('수정된 내용이 없습니다.')
   - 각 변경행에 대해:
     a) 이메일 형식 검사(CUST_MAIN_EMAIL, SALESREP_EMAIL, 빈값은 허용/불허 여부는 사양대로)
     b) CUST_SENDMAIL_YN=Y이면 CUST_MAIN_EMAIL 필수
     c) SALESREP_SENDMAIL_YN=Y이면 SALESREP_EMAIL 필수
   - invalid 시 .invalid-input 표시 및 해당 셀 포커스

4) 선택/행 강조
   - 행 클릭 시 .selected-row 적용(단일 선택)

5) 예약발송 UI
   - scheduleTime, scheduleMinute 값은 그대로 유지/전달(프런트 보존만)

[산출물 형식]
A. 변경 적용된 JSP 전체 코드(그대로 교체 가능)
B. 핵심 JS 함수 정리: initGrid(), bindEvents(), validateRow(), emailRegex(), markRowChanged(), serializeChanges()
C. 수동 테스트 시나리오(1~10단계)
D. 추후 백엔드 연동용 AJAX 인터페이스 주석(URI, 파라미터, 요청/응답 예시)

[참고 자료]
- (여기에 1·2번 사양과 3번 JSP 소스를 그대로 붙여넣기)
```

## 3) 부분 수정 요청(한 영역만 바꿀 때)

```
[요청]
3번 JSP의 jqGrid 초기화/이벤트 처리 부분만 교체할 JS 코드를 작성해줘.
- 요구: 셀 편집(이메일/비고), 체크박스(Y/N), 변경행 하이라이트/원복, 이메일 정규식 검사, 저장 시 변경행 직렬화.
- 나머지 HTML·CSS는 건드리지 말고, 교체가 필요한 <script> 블록만 완성 코드로 줘.

[현재 스크립트 블록]
(3번에서 <script type="text/javascript"> ~ </script> 내부를 그대로 붙여넣기)

[사양 1·2 전문]
(1·2번 붙여넣기)
```

## 4) 코드 리뷰/리팩터만 원할 때

```
[요청]
아래에 내가 임시로 구현한 JS를 코드리뷰/리팩터링해줘.
- 성능(대량행), 유지보수성, 이벤트 누수방지, 정규식/엣지케이스, jqGrid 옵션 최적화 관점에서 지적사항 + 수정코드 동봉.
(여기에 내 임시 JS 붙여넣기)
```

---

# 내가 알아야 더 잘 만드는 정보(체크리스트)

* [ ] 변경 추적 방식 선호: 행 단위(dirtyRows) vs 셀 단위(dirtyCells)
* [ ] 이메일 허용 규칙: 빈값 허용? 국제 도메인/서브도메인 허용? 다중 이메일(콤마) 허용 여부
* [ ] 체크박스 값 매핑: `Y:N` 고정? 공백/NULL 허용? 초기값 디폴트?
* [ ] 조회 결과 페이징/정렬 범위(프런트 가짜 페이징 OK?)
* [ ] 저장 시 직렬화 포맷: `[{CUST_CD, CUST_MAIN_EMAIL, ...}]` 맞는지
* [ ] alert/confirm 스타일: 기본 alert vs 커스텀 모달
* [ ] 선택행 단일/다중 여부, 행 더블클릭 편집 허용 여부
* [ ] 예약발송 시간/분 필수 여부

---

# 내가 제공하는 산출물 형태(선택)

1. **전체 JSP 완성본** (그대로 교체 가능)
2. **Diff 패치** (바뀐 부분만)
3. **순수 JS 블록만** (기존 JSP에 붙여넣기)
4. **스니펫 + 설명서 + 테스트 시나리오**

요청할 때 위 옵션 중 원하는 걸 함께 적어줘.

---

# 품질 보증(내가 기본으로 넣는 것)

* 견고한 **email 정규식** + 경계조건(공백, 대문자, 한글 이름 필드 오입력 방지)
* **원복 감지**(현재값 === 원래값이면 하이라이트 제거)
* **포커스/스크롤 보정**(유효성 실패 셀로 이동)
* **대량행 성능 고려**(이벤트 위임, 최소 reflow, 필요 시 `grid.setCell` 배치 적용)
* **테스트 시나리오**와 **체크리스트** 동봉

---

## 아주 짧은 한 줄 버전

```
1·2번 사양을 3번 JSP에 프런트(JSP+JS)만으로 완성 적용해줘—jqGrid 셀편집/체크박스/이메일검사/변경행하이라이트/원복/저장직렬화까지. 결과는 “전체 JSP”와 “핵심 JS 함수/테스트 절차”로 줘.
```

---

# (요청하신 대로) THINK HARD — 내가 이렇게 구성했어

* **Genius Insight(GI)**: 관찰 9, 연결 8, 패턴 8, 종합 8, 가정 2, 편향 1 → GI≈**1536**. 높은 GI를 목표로 “프롬프트 템플릿+체크리스트+산출물 옵션” 구조로 설계.
* **MDA**:

  * D1(시간): 기존 소스 보존 vs 향후 확장(가중 0.3)
  * D3(추상↔구체): 요구를 “프롬프트 템플릿”으로 구체화(0.3)
  * D4(인과): 요청 → 구현 산출물 형태 → 테스트(0.2)
  * D5(계층): 화면(UI)·이벤트·검증·상태추적 계층 분리(0.2)
* **PR(문제재정의)**: “코드 작성”이 아니라 “정확히 시키는 법”을 만드는 문제로 전환 → 복붙형 템플릿 제시.
* **IS(혁신해법)**: 기존 요구를 **템플릿화** + **체크리스트** + **산출물 옵션화**로 결합(실행 가능·가치 높음, 리스크 낮음).
* **IA(증폭)**: 이후 네가 체크리스트를 채워 재요청하면 정확도가 반복적으로 상승하도록 설계.

---

원하는 버전을 골라서 지금 그대로 붙여 보내줘. 그러면 **프런트(JSP+JS)만**으로 동작하게 완성 코드를 바로 내줄게.

---------------------------------------------------------------------

## 1번 고고

---------------------------------------------------------------------

좋지! “1번(초간단 요청)”대로 바로 적용해서, \*\*프런트(JSP+JavaScript·jqGrid만)\*\*으로 동작 완성본을 내줄게.
아래 구성으로 제공해:

1. 변경 적용된 **JSP 전체 코드(교체 가능)**
2. 핵심 **JS 함수 요약 스니펫**
3. 수동 **테스트 절차(체크리스트)**

---

# ① JSP 전체 코드 (프런트만, 더미데이터/모의조회/유효성/하이라이트/토글 포함)

```html
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

<script type="text/javascript" src="${url}/include/js/common/select2/select2.js"></script>
<link rel="stylesheet" href="${url}/include/js/common/select2/select2.css" />

<style>
    .changed-row { background-color: #D1ECF1 !important; }
    .selected-row { background-color: #E8F5E8 !important; }
    .small-checkbox input[type="checkbox"] { width: 16px; height: 16px; }
    .invalid-input { outline: 2px solid #e57373 !important; background-color: #fdecea !important; }
</style>

<script type="text/javascript">
/* =============================
   주문메일알람 — 프런트 전용 스크립트
   기능: 조회(모의), 셀편집, 체크박스 Y/N 토글, 변경행 하이라이트/원복, 이메일 유효성, 저장 직렬화
   ============================= */

var gridId = "#gridList";
var pagerId = "#pager";

// 초기 데이터(모의). 실제 연동 시 dataSearch()에서 AJAX로 교체.
var sampleData = [
  { CUST_CD:"C001", CUST_NM:"알파상사", CUST_MAIN_EMAIL:"alpha@ex.com", CUST_SENDMAIL_YN:"N",
    SALESREP_NM:"김영업", SALESREP_EMAIL:"sales.alpha@ex.com", SALESREP_SENDMAIL_YN:"Y", COMMENTS:"" },
  { CUST_CD:"C002", CUST_NM:"베타유통", CUST_MAIN_EMAIL:"", CUST_SENDMAIL_YN:"Y",
    SALESREP_NM:"이영업", SALESREP_EMAIL:"", SALESREP_SENDMAIL_YN:"N", COMMENTS:"담당자 변경 예정" },
  { CUST_CD:"C003", CUST_NM:"감마전자", CUST_MAIN_EMAIL:"cs@gamma.co.kr", CUST_SENDMAIL_YN:"N",
    SALESREP_NM:"박영업", SALESREP_EMAIL:"pyo@gamma.co.kr", SALESREP_SENDMAIL_YN:"N", COMMENTS:"" }
];

// 원본 스냅샷 / 변경행 트래킹
var originalMap = {};   // {rowid: {필드스냅샷}}
var dirtyMap = {};      // {rowid: true}

// 편집/검증 대상 필드
var editableFields = ["CUST_MAIN_EMAIL","SALESREP_EMAIL","COMMENTS","CUST_SENDMAIL_YN","SALESREP_SENDMAIL_YN"];

// 간단 이메일 정규식 (RFC 완전 엄격 X, 실무 안전선)
function isEmailValid(v){
  if(!v) return true; // 빈값 자체는 형식검사 통과(필수 여부는 별도 로직에서)
  var re=/^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
  return re.test(v.trim());
}

// 행의 현재값 vs 원본값 비교
function isRowChanged(rowid){
  var row = $(gridId).jqGrid('getRowData', rowid);
  var org = originalMap[rowid];
  if(!org) return false;
  for(var i=0;i<editableFields.length;i++){
    var k = editableFields[i];
    if((row[k]||"").trim() !== (org[k]||"").trim()) return true;
  }
  return false;
}

// 하이라이트 토글
function applyRowHighlight(rowid){
  var tr = $(gridId).jqGrid('getInd', rowid, true);
  if(isRowChanged(rowid)){
    $(tr).addClass('changed-row');
    dirtyMap[rowid] = true;
  }else{
    $(tr).removeClass('changed-row');
    delete dirtyMap[rowid];
  }
}

// 특정 셀에 invalid 표시/해제
function markCellInvalid(rowid, colName, invalid){
  var tr = $(gridId).jqGrid('getInd', rowid, true);
  var $td = $(tr).find('td[aria-describedby="gridList_'+colName+'"]');
  if(invalid){ $td.addClass('invalid-input'); } else { $td.removeClass('invalid-input'); }
}

// 행 유효성 검사
function validateRow(row){
  var ok = true;

  // 형식검사
  if(!isEmailValid(row.CUST_MAIN_EMAIL)){
    markCellInvalid(row.CUST_CD, "CUST_MAIN_EMAIL", true);
    ok=false;
  } else {
    markCellInvalid(row.CUST_CD, "CUST_MAIN_EMAIL", false);
  }
  if(!isEmailValid(row.SALESREP_EMAIL)){
    markCellInvalid(row.CUST_CD, "SALESREP_EMAIL", true);
    ok=false;
  } else {
    markCellInvalid(row.CUST_CD, "SALESREP_EMAIL", false);
  }

  // 필수성(발송여부가 Y일 때 이메일 필수)
  if((row.CUST_SENDMAIL_YN||"N") === "Y" && !(row.CUST_MAIN_EMAIL||"").trim()){
    markCellInvalid(row.CUST_CD, "CUST_MAIN_EMAIL", true);
    ok=false;
  }
  if((row.SALESREP_SENDMAIL_YN||"N") === "Y" && !(row.SALESREP_EMAIL||"").trim()){
    markCellInvalid(row.CUST_CD, "SALESREP_EMAIL", true);
    ok=false;
  }

  return ok;
}

// 변경행 직렬화
function serializeChanges(){
  var ids = Object.keys(dirtyMap);
  var list = [];
  for(var i=0;i<ids.length;i++){
    var id = ids[i];
    var row = $(gridId).jqGrid('getRowData', id);
    list.push({
      CUST_CD: row.CUST_CD,
      CUST_MAIN_EMAIL: row.CUST_MAIN_EMAIL || "",
      CUST_SENDMAIL_YN: (row.CUST_SENDMAIL_YN||"N"),
      SALESREP_EMAIL: row.SALESREP_EMAIL || "",
      SALESREP_SENDMAIL_YN: (row.SALESREP_SENDMAIL_YN||"N"),
      COMMENTS: row.COMMENTS || ""
    });
  }
  return list;
}

// Y/N 체크박스 토글 처리(셀 클릭 시)
function toggleYN(rowid, colName){
  var row = $(gridId).jqGrid('getRowData', rowid);
  var cur = (row[colName]||"N").trim().toUpperCase();
  var next = cur === "Y" ? "N" : "Y";
  // 화면값/내부값 반영
  $(gridId).jqGrid('setCell', rowid, colName, next);
  applyRowHighlight(rowid);
}

// jqGrid 초기화
function initGrid(){
  $(gridId).jqGrid({
    datatype: 'local',
    data: [],
    colModel: [
      { name: "CUST_CD", key: true, label: '코드', width: 120, align: 'center', sortable: true },
      { name: "CUST_NM", label: '거래처명', width: 220, align: 'left', sortable: true },
      { name: "CUST_MAIN_EMAIL", label: '담당자 이메일', width: 220, align: 'center', sortable: true, editable: true, edittype:'text' },
      { name: "CUST_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center', sortable: true,
        editable: true, edittype:'checkbox', formatter:'checkbox',
        editoptions: { value:"Y:N" }, formatoptions:{ disabled:false } },
      { name: "SALESREP_NM", label: '영업 담당', width: 100, align: 'center', sortable: true },
      { name: "SALESREP_EMAIL", label: '영업 담당 이메일', width: 300, align: 'center', sortable: true, editable: true, edittype:'text' },
      { name: "SALESREP_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center', sortable: true,
        editable: true, edittype:'checkbox', formatter:'checkbox',
        editoptions: { value:"Y:N" }, formatoptions:{ disabled:false } },
      { name: "COMMENTS", label: '비고', width: 450, align: 'left', sortable: true, editable: true, edittype:'text' }
    ],
    cmTemplate: { sortable:true },
    rowNum: 1000,
    height: 420,
    autowidth: true,
    shrinkToFit: false,
    pager: pagerId,
    viewrecords: true,
    cellEdit: true,
    cellsubmit: 'clientArray',
    // 셀 편집 이벤트
    beforeSaveCell: function(rowid, cellname, value, iRow, iCol){
      return (value||"").trim();
    },
    afterSaveCell: function(rowid, cellname, value, iRow, iCol){
      // 이메일 형식 즉시검사 + 변경하이라이트
      if(cellname==="CUST_MAIN_EMAIL" || cellname==="SALESREP_EMAIL"){
        var ok = isEmailValid(value);
        markCellInvalid(rowid, cellname, !ok);
      }
      applyRowHighlight(rowid);
    },
    // 행 선택 강조
    onSelectRow: function(id){
      var $trs = $(gridId).closest(".ui-jqgrid-bdiv").find("tr.jqgrow");
      $trs.removeClass("selected-row");
      var tr = $(gridId).jqGrid('getInd', id, true);
      $(tr).addClass("selected-row");
    },
    // 체크박스 컬럼 셀 클릭 시 토글
    onCellSelect: function(rowid, iCol, cellcontent, e){
      var cm = $(gridId).jqGrid("getGridParam", "colModel")[iCol];
      if(!cm) return;
      if(cm.name==="CUST_SENDMAIL_YN" || cm.name==="SALESREP_SENDMAIL_YN"){
        toggleYN(rowid, cm.name);
      }
    },
    // 데이터 로드 후 원본 스냅샷 저장
    gridComplete: function(){
      var ids = $(gridId).jqGrid('getDataIDs');
      originalMap = {};
      dirtyMap = {};
      for(var i=0;i<ids.length;i++){
        var id = ids[i];
        var row = $(gridId).jqGrid('getRowData', id);
        originalMap[id] = {
          CUST_MAIN_EMAIL: row.CUST_MAIN_EMAIL||"",
          SALESREP_EMAIL: row.SALESREP_EMAIL||"",
          COMMENTS: row.COMMENTS||"",
          CUST_SENDMAIL_YN: (row.CUST_SENDMAIL_YN||"N"),
          SALESREP_SENDMAIL_YN: (row.SALESREP_SENDMAIL_YN||"N")
        };
      }
      $("#listTotalCountSpanId").text(ids.length);
    }
  });

  // 반응형 리사이즈
  $(window).on('resize', function(){
    var newWidth = $("#gbox_gridList").parent().width();
    $(gridId).jqGrid('setGridWidth', newWidth, true);
  }).trigger('resize');
}

// 조회(모의)
// 실제 연동 시 AJAX로 교체하고, 성공 콜백에서 setGridParam({datatype:'local', data: result}).trigger('reloadGrid')
function dataSearch(){
  // 검색조건 읽기
  var custCd = $('input[name="rl_custcd"]').val().trim();
  var custNm = $('input[name="rl_custnm"]').val().trim();
  var salesNm = $('input[name="rl_salesrepnm"]').val().trim();
  var mailYnFilters = $('input[name="r_salesrepcdyn"]:checked').map(function(){return $(this).val();}).get(); // Y/N 0~2개

  // 간단 필터
  var filtered = sampleData.filter(function(r){
    var ok = true;
    if(custCd) ok = ok && (r.CUST_CD||"").toUpperCase().indexOf(custCd.toUpperCase())>=0;
    if(custNm) ok = ok && (r.CUST_NM||"").indexOf(custNm)>=0;
    if(salesNm) ok = ok && (r.SALESREP_NM||"").indexOf(salesNm)>=0;
    if(mailYnFilters.length>0){
      ok = ok && (mailYnFilters.indexOf((r.SALESREP_SENDMAIL_YN||"N"))>=0);
    }
    return ok;
  });

  $(gridId).jqGrid('clearGridData');
  for(var i=0;i<filtered.length;i++){
    $(gridId).jqGrid('addRowData', filtered[i].CUST_CD, filtered[i]);
  }
}

// 저장(유효성 검사 + 변경행 직렬화)
// 실제 연동 시 AJAX 요청부를 주석 부분에 삽입
function dataInUp(btn, extra){
  // 셀 편집 중이면 저장
  try { $(gridId).jqGrid('saveCell', lastrow, lastcell); } catch(e){/* ignore */};

  var changes = serializeChanges();
  if(changes.length===0){
    alert('수정된 내용이 없습니다.');
    return;
  }

  // 각 변경행 유효성 검사
  var allOk = true, firstBad = null;
  for(var i=0;i<changes.length;i++){
    var id = changes[i].CUST_CD;
    var row = $(gridId).jqGrid('getRowData', id);
    if(!validateRow(row)){
      allOk = false;
      if(!firstBad) firstBad = { id:id };
    }
  }
  if(!allOk){
    // 최초 실패 셀로 스크롤/포커스
    if(firstBad){
      var tr = $(gridId).jqGrid('getInd', firstBad.id, true);
      var $bad = $(tr).find('td.invalid-input:first');
      if($bad.length){
        var $bdiv = $("#gbox_gridList .ui-jqgrid-bdiv");
        $bdiv.animate({ scrollTop: $bdiv.scrollTop() + $bad.position().top - 40 }, 200);
      }
    }
    alert('유효성 검사에 실패했습니다. 빨간 표기 셀을 확인하세요.');
    return;
  }

  // 예약발송 UI 값
  var scheduleTime = $('select[name="scheduleTime"]').val() || "";
  var scheduleMinute = $('select[name="scheduleMinute"]').val() || "";

  // 실제로는 AJAX로 서버 전송
  // $.ajax({
  //   url: '/admin/order-mail-alert/save',
  //   method: 'POST',
  //   contentType: 'application/json; charset=UTF-8',
  //   data: JSON.stringify({ scheduleTime, scheduleMinute, items: changes }),
  //   success: function(res){ /* 성공 처리 및 원본 동기화 */ },
  //   error: function(xhr){ alert('저장 실패: ' + xhr.statusText); }
  // });

  // 데모: 콘솔 출력 + 원본 동기화(하이라이트 제거)
  console.log('SAVE PAYLOAD =>', { scheduleTime, scheduleMinute, items: changes });

  // 저장 성공 가정: 원본 스냅샷 갱신 & 하이라이트 제거
  changes.forEach(function(ch){
    var id = ch.CUST_CD;
    var row = $(gridId).jqGrid('getRowData', id);
    originalMap[id] = {
      CUST_MAIN_EMAIL: row.CUST_MAIN_EMAIL||"",
      SALESREP_EMAIL: row.SALESREP_EMAIL||"",
      COMMENTS: row.COMMENTS||"",
      CUST_SENDMAIL_YN: (row.CUST_SENDMAIL_YN||"N"),
      SALESREP_SENDMAIL_YN: (row.SALESREP_SENDMAIL_YN||"N")
    };
    applyRowHighlight(id); // 원복 여부 반영
  });
  alert('모의 저장 완료(콘솔 확인).');
}

// 이벤트 바인딩
function bindEvents(){
  // Enter로 조회
  $('.search-input').on('keypress', function(e){
    if(e.which===13) dataSearch();
  });

  // 메일발송여부(Y/N) 체크 콤비 — 한쪽만 체크도 허용 (다중체크 = 필터 OR)
  $('input[name="r_salesrepcdyn"]').on('click', function(){
    // 즉시 조회
    dataSearch();
  });
}

// 초기 로딩
$(function(){
  initGrid();
  bindEvents();
  dataSearch(); // 첫 로드 시 모의 데이터 표시
});
</script>

</head>

<body class="page-header-fixed compact-menu">

    <!-- Page Content -->
    <main class="page-content content-wrap">

        <%@ include file="/WEB-INF/views/include/admin/header.jsp" %>
        <%@ include file="/WEB-INF/views/include/admin/left.jsp" %>

        <%-- 임의 form --%>
        <form name="iForm" method="post"></form>

        <form name="frm" method="post">

            <!-- Page Inner -->
            <div class="page-inner">
                <div class="page-title">
                    <h3>
                        거래처현황
                        <div class="page-right">
                            <button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();">
                                <i class="fa fa-refresh"></i><em>새로고침</em>
                            </button>
                            <button type="button" class="btn btn-line f-black" title="엑셀다운로드" onclick="excelDown(this);">
                                <i class="fa fa-file-excel-o"></i><em>엑셀다운로드</em>
                            </button>
                        </div>
                    </h3>
                </div>

                <!-- Main Wrapper -->
                <div id="main-wrapper">
                    <!-- Row -->
                    <div class="row">
                        <div class="col-md-12">
                            <div class="panel panel-white">
                                <div class="panel-body no-p">
                                    <div class="tableSearch">
                                        <div class="topSearch">
                                            <ul>
                                                <li>
                                                    <label class="search-h">거래처코드</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="rl_custcd"
                                                               value="${param.rl_custcd}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">거래처명</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="rl_custnm"
                                                               value="${param.rl_custnm}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">영업담당</label>
                                                    <div class="search-c">
                                                        <input type="text" class="search-input" name="rl_salesrepnm"
                                                               value="${param.rl_salesrepnm}" />
                                                    </div>
                                                </li>
                                                <li>
                                                    <label class="search-h">메일발송여부</label>
                                                    <div class="search-c checkbox">
                                                        <label>
                                                            <input type="checkbox" name="r_salesrepcdyn" value="Y" />Y
                                                        </label>
                                                        <label>
                                                            <input type="checkbox" name="r_salesrepcdyn" value="N" />N
                                                        </label>
                                                    </div>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>

                                <div class="panel-body">
                                    <h5 class="table-title listT">
                                        TOTAL <span id="listTotalCountSpanId">0</span>EA
                                    </h5>
                                    <div class="btnList writeObjectClass">
                                        <%-- 예약발송 시각 설정 --%>
                                        <label>예약발송 시간:</label>
                                        <select name="scheduleTime">
                                            <option value="">선택</option>
                                            <% 
                                            String currentHour = request.getParameter("scheduleTime") != null ? request.getParameter("scheduleTime") : "";
                                            for(int i = 0; i <= 23; i++) { 
                                                String hourStr = String.format("%02d", i);
                                                String selected = hourStr.equals(currentHour) ? "selected" : "";
                                            %>
                                                <option value="<%= hourStr %>" <%= selected %>><%= hourStr %>시</option>
                                            <% } %>
                                        </select>
                                        <select name="scheduleMinute">
                                            <option value="">선택</option>
                                            <% 
                                            String currentMinute = request.getParameter("scheduleMinute") != null ? request.getParameter("scheduleMinute") : "";
                                            int[] minutes = {0, 10, 20, 30, 40, 50};
                                            for(int minute : minutes) { 
                                                String minuteStr = String.format("%02d", minute);
                                                String selected = minuteStr.equals(currentMinute) ? "selected" : "";
                                            %>
                                                <option value="<%= minuteStr %>" <%= selected %>><%= minuteStr %>분</option>
                                            <% } %>
                                        </select>
                                        <button type="button" class="btn btn-info" onclick="dataInUp(this, '');">저장</button>
                                    </div>
                                    <div class="table-responsive in">
                                        <table id="gridList" class="display table tableList nowrap"
                                               width="100%" border="0" cellpadding="0" cellspacing="0"></table>
                                        <div id="pager"></div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>
                    <!-- //Row -->
                </div>
                <!-- //Main Wrapper -->

                <%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>

            </div>

        </form>
        <!-- //Page Inner -->
    </main>
    <!-- //Page Content -->

</body>

</html>
```

---

# ② 핵심 JS 스니펫(요약·포인트만)

```js
// 1) 그리드 초기화
initGrid();

// 2) 조회(모의 필터링)
dataSearch();

// 3) 변경행 판정/하이라이트
function applyRowHighlight(rowid){ /* 원본 vs 현재 비교 → .changed-row */ }

// 4) 체크박스 Y/N 토글
function toggleYN(rowid, colName){ /* setCell로 값 Y/N 전환 */ }

// 5) 이메일 검증 + 발송여부 시 필수 체크
function validateRow(row){ /* isEmailValid + 필수성 */ }

// 6) 저장
function dataInUp(){
  // 편집 저장 → 변경행 직렬화 → 각 행 validate → (모의) 저장 → 원본 동기화
}
```

---

# ③ 테스트 절차 (수동 시나리오)

1. 페이지 진입 시 샘플 3건이 표시되는지 확인(TOTAL 갯수 확인).
2. 아무 것도 수정하지 않고 \[저장] → `수정된 내용이 없습니다.` 알림 확인.
3. C001의 **담당자 이메일**을 `alpha2@ex.com`으로 수정 → 행 배경이 파란색(변경)으로 표시.
4. 같은 셀을 다시 `alpha@ex.com`으로 되돌림 → 파란색 하이라이트 제거.
5. C002의 \*\*발송 여부(담당자)\*\*를 `Y`로 토글 → **담당자 이메일**이 빈칸이므로 \[저장] 시 유효성 실패(빨간표시+알림).
6. C002의 **담당자 이메일**을 `boss@beta.co.kr`로 입력 → \[저장] 성공(모의), 콘솔 payload 확인, 하이라이트 제거.
7. **영업 담당 발송 여부**를 `Y`로 바꾸고 **영업 담당 이메일**을 `aaa@` 같이 잘못 입력 → \[저장] 시 형식 오류로 실패.
8. 틀린 이메일을 올바르게 수정 후 \[저장] → 성공 처리 및 하이라이트 제거.
9. 상단 검색조건에서 “영업담당 = 김영업” 입력 후 Enter → 필터링 결과 확인.
10. 메일발송여부 체크박스 ‘Y’만 체크 → `SALESREP_SENDMAIL_YN=Y`인 행만 걸러지는지 확인.

---

원하면 이 버전을 **백엔드(AJAX) 연동**까지 바로 이어서 붙일 수 있게 URI/파라미터 스펙도 만들어줄게.
다음 단계로 가자: **실데이터 스키마/저장 API 스펙** 알려주면 그 형태로 직렬화/전송 로직 딱 맞춰 줄게.
