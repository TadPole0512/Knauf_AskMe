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

아래 답변은 네 요청을 더 정확하고 빠르게 처리하기 위한 “요청 템플릿”을 주는 거야.
먼저 어떤 사고 공식을 썼는지 짚고, 그다음 바로 가져다 붙여 쓸 수 있는 템플릿/예시/체크리스트를 제공할게.

---

## 내가 적용한 사고 공식과 구성

* **PR(문제 재정의)**: \*“1·2의 요구사항을 3 소스(JSP)에 반영하는 작업”\*으로 재정의, 화면·동작·유효성 3축으로 분해.
* **MDA(다차원 분석)**: 과거(현행 JSP), 현재(추가 요구), 미래(확장: 예약발송/검증/상태표시) 관점으로 필드·이벤트·API를 정리.
* **GI(통찰)**: jqGrid 편집/검증/행 상태(변경/원복)를 \*\*한 곳(JS 모듈)\*\*에서 관리하면 리스크 최소.
* **CC/IS(창의적 결합)**: “검증 규칙 + 변경 추적 + 저장 배치”를 공통 헬퍼로 묶어 재사용.
* \*\*IA(Why x5)\*\*로 요구의 핵심을 \*“수정된 행만 저장 + 메일형식 검증 + 체크박스-값 의존성”\*까지 떨어뜨림.
* **CS(복잡도 분해)**: (1) 조회조건 바인딩 → (2) jqGrid 편집모드/체크박스/스타일 → (3) 유효성 → (4) 저장 API.
* **IW(지식·이해·지혜)**: 바로 실행 가능한 **요청 템플릿 + 예시 + 산출물 형식** 제공.

---

## 네가 나에게 이렇게 요청하면 된다 (복붙용 템플릿)

### 1) 작업 목표

* **목표**: “주문메일알람” 화면(3번 JSP)에 \*\*\[1. 화면]\*\*과 \*\*\[2. 동작/유효성]\*\*을 **그대로 반영**해줘.
* **범위**: JSP(프론트) + JS(jqGrid 초기화/이벤트/검증/저장) + 컨트롤러/서비스/DAO(선택) + SQL(선택).

### 2) 개발 환경

* OS: Windows / STS 4.5 / Java: Zulu-8
* WAS: Tomcat 9.0.99 / DB: MS-SQL 2019
* UI: jQuery + jqGrid / 템플릿: JSP

### 3) 입력(내가 제공하는 자료)

* **현행 JSP/JS 소스**: (파일 경로/내용 붙여넣기)
* **엔드포인트**

  * 조회: `GET /admin/mailalarm/list` (파라미터: `rl_custcd, rl_custnm, rl_salesrepnm, r_salesrepcdyn[]`)
  * 저장: `POST /admin/mailalarm/save` (본문: 수정된 행 배열)
* **DB 테이블/뷰**: (테이블명·PK·컬럼·타입·NULL 여부)

  * 예) `TB_CUSTOMER(CUST_CD PK, CUST_NM, CUST_MAIN_EMAIL, CUST_SENDMAIL_YN, SALESREP_NM, SALESREP_EMAIL, SALESREP_SENDMAIL_YN, COMMENTS)`
* **메일 형식 검증 규칙**: RFC5322 느슨(정규식 제공 가능/불가)
* **체크박스-값 의존 규칙**

  * `CUST_SENDMAIL_YN=Y` → `CUST_MAIN_EMAIL` 필수
  * `SALESREP_SENDMAIL_YN=Y` → `SALESREP_EMAIL` 필수

### 4) 출력(내가 받아야 할 산출물)

1. **수정된 JSP/JS 전체 소스**

   * jqGrid `colModel`(편집 가능/체크박스 렌더러/스타일 클래스)
   * 변경추적(원본값 캐시 → 변경 시 `.changed-row` 적용, 원복 시 제거)
   * 이메일 유효성 검사(실시간 + 저장 시)
   * 저장 API 호출(수정된 행만 전송) / 실패-성공 토스트 처리
2. **백엔드 컨트롤러/서비스/DAO/Mapper**(선택)

   * 저장 시 MERGE/UPSERT SQL (MS-SQL 2019용)
   * 트랜잭션/예외 메시지 표준화
3. **SQL(선택)**

   * 조회용 SELECT (검색조건 반영)
   * 저장용 `MERGE` 또는 `UPDATE` Batch
4. **테스트 시나리오 & 체크리스트**

   * 수정 없음 경고, 이메일 형식 오류, 체크박스-값 의존 위반, 다중행 저장 등

### 5) UI 동작 요약(반영 지시)

* **편집 가능**: `CUST_MAIN_EMAIL`, `SALESREP_EMAIL`, `COMMENTS`
* **체크박스 토글**: `CUST_SENDMAIL_YN`, `SALESREP_SENDMAIL_YN`
* **행 상태 스타일**: 변경 시 `.changed-row` / 선택 시 `.selected-row`
* **원복 처리**: 값이 원본과 동일해지면 변경 상태 해제
* **저장 전 유효성**:

  1. 수정행 존재 검사 → 없으면 “수정된 내용이 없습니다.”
  2. 이메일 형식 검사(두 필드)
  3. 체크박스-필수 값 검사(의존 규칙)

### 6) 제약/선호

* **Knauf-SQL-Style v1.1** 준수
* 프론트 JS **하나의 모듈**로 캡슐화(네임스페이스: `MailAlarm`)
* i18n 메시지 훅(추후 다국어)
* 코드 주석: 한국어, 함수/이벤트 단위

### 7) 예산/일정/리스크(선택)

* 일정: (원하는 일정)
* 리스크: jqGrid 버전/기존 CSS 충돌/엔드포인트 인증

---

## 예시 요청(짧게 쓰는 버전)

> “아래 3번 JSP에 1·2의 요구를 반영해줘. 편집 필드/체크박스/유효성/변경행만 저장까지 포함.
> 조회는 `GET /admin/mailalarm/list`, 저장은 `POST /admin/mailalarm/save`.
> 테이블은 `TB_CUSTOMER`(스키마 아래 첨부).
> 완성 산출물: 수정된 JSP/JS 전체, 컨트롤러/서비스/DAO(스켈레톤), Mapper XML, SELECT/MERGE SQL, 테스트 시나리오.
> Knauf-SQL-Style v1.1, 네임스페이스는 `MailAlarm`.
> 첨부: (현행 JSP/JS/스키마 붙여넣기)”

---

## 체크리스트 (붙여서 보내면 더 정확함)

* [ ] 조회 API 명세와 파라미터 결정
* [ ] 저장 API URL/형식(JSON 스키마) 확정
* [ ] 테이블 스키마/PK/인덱스 공유
* [ ] 이메일 정규식 강도(느슨/엄격) 선택
* [ ] 변경 추적 기준(공백 트림 여부) 정의
* [ ] 실패/성공 메시지 문구 확정
* [ ] 엑셀다운로드/예약발송 시간과의 연계 여부

---

## 내가 제공할 산출물 형태(요청 시 그대로 전달)

1. **완성 JSP/JS 코드**(전체 파일, 부분 아님)
2. **Controller/Service/DAO/Mapper XML**(동작 가능한 템플릿)
3. **MS-SQL 쿼리**(조회/저장; MERGE 포함)
4. **테스트 케이스 표**(엣지 포함)

---

원하는 방식으로 위 템플릿을 채워서 메시지로 보내줘. 그러면 **수정된 전체 소스**와 **백엔드/SQL**까지 한 번에 완성해서 줄게.
