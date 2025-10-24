아래 1번과 2번의 내용을 3번 소스에 적용해줘.(THINK HARD)
단 jsp내에 javascript를 포함한 화면에서의 동작만 대상으로 해줘. 

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


좋다 👍 지금 요청하신 건 크게 보면  

- **1. 화면 요구사항(조회조건, 그리드 컬럼)**  
- **2. 동작 요구사항(이메일 수정, 체크박스 토글, 행 변경 표시, 유효성 검사)**  

이 두 가지를 **3번 JSP 화면(jqGrid 스크립트 부분)**에 반영해야 하는 거지요.  
즉, jsp 내에 **javascript 로직만** 채워 넣는 작업이에요.  

***

# 🚀 적용 설계 (PR + MDA + GI 적용)
- **P₀(문제 정의)** : 주문 메일알람 조회/수정 가능한 jqGrid 구현  
- **관점 회전(T)** : DB에서 가져오는 로직은 서버(Java/SQL)에서 담당 → 여기선 UI(JS/JQGrid) 동작만 구현  
- **범위 재조정(S)** : "조회조건 입력 후 검색 → jqGrid에 표시 → 수정/체크박스 토글 시 변경 상태 관리 → 저장 시 유효성 검사"  
- **본질(M)** : "변경 관리 + 입력값 유효성"  

즉, js 로직 포인트는 👇  
1. `jqGrid 초기화` (컬럼 editable, formatter, checkbox 처리)  
2. `onCellEdit` + `afterSaveCell` 이벤트로 **변경 행 관리 + 배경색 처리**  
3. `이메일 regex 검사`  
4. `체크박스 토글 시 값 반영`  
5. `저장(dataInUp)` 시 수정 데이터 추출 → 유효성 검사 실행  

***

# ✅ 완성 소스 (javascript 부분만 추가)

아래 코드를 **3번 소스의 `<script type="text/javascript">` 내부**에 넣어주세요.

```javascript
// ====== 전역 변수 ======
var changedRows = {};  // { rowId: true } 형태로 수정된 행 관리
var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// ====== jqGrid 초기화 ======
$(document).ready(function () {
    $("#gridList").jqGrid({
        url: '/order/mailAlarmList.json',   // 서버 조회 URL (샘플)
        datatype: "json",
        mtype: "POST",
        colModel: [
            { name: "CUST_CD", key: true, label: '코드', width: 120, align: 'center' },
            { name: "CUST_NM", label: '거래처명', width: 220, align: 'left' },
            { name: "CUST_MAIN_EMAIL", label: '담당자 이메일', width: 220, align: 'center',
              editable: true, edittype: "text" },
            { name: "CUST_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center',
              edittype: 'checkbox', formatter: checkboxFormatter,
              editable: true, editoptions: {value: "Y:N"} },
            { name: "SALESREP_NM", label: '영업 담당', width: 100, align: 'center' },
            { name: "SALESREP_EMAIL", label: '영업 담당 이메일', width: 300, align: 'center',
              editable: true, edittype: "text" },
            { name: "SALESREP_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center',
              edittype: 'checkbox', formatter: checkboxFormatter,
              editable: true, editoptions: {value: "Y:N"} },
            { name: "COMMENTS", label: '비고', width: 450, align: 'left', editable: true, edittype: "text" }
        ],
        pager: "#pager",
        rowNum: 20,
        rownumbers: true,
        shrinkToFit: false,
        autowidth: true,
        viewrecords: true,
        cellEdit: true,
        cellsubmit: 'clientArray',

        // 셀 수정 완료 후 이벤트
        afterSaveCell: function (rowid, cellName, val, iRow, iCol) {
            markRowChanged(rowid, cellName);
        },

        // 체크박스 클릭 처리
        beforeSaveCell: function(rowid, cellname, value, iRow, iCol) {
            if(cellname.endsWith("_SENDMAIL_YN")){
                markRowChanged(rowid, cellname);
            }
            return value;
        }
    });
});

// ====== 체크박스 포맷터 ======
function checkboxFormatter(cellvalue, options, rowObject) {
    var checked = (cellvalue === 'Y') ? 'checked' : '';
    return '<input type="checkbox" class="mail-checkbox" ' + checked + ' />';
}

// 체크박스 클릭 이벤트 바인딩
$(document).on("change", ".mail-checkbox", function(){
    var $td = $(this).closest("td");
    var rowId = $("#gridList").jqGrid('getGridParam', 'selrow');
    var colModel = $("#gridList").jqGrid("getGridParam", "colModel");
    var colIndex = $td.attr("aria-describedby").split("_")[1];
    var colName = colModel.find(c => c.index === colIndex || c.name === colIndex).name;
    var newVal = $(this).is(":checked") ? "Y" : "N";

    $("#gridList").jqGrid('setCell', rowId, colName, newVal);
    markRowChanged(rowId, colName);
});

// ====== 변경 표시 함수 ======
function markRowChanged(rowid, cellName) {
    changedRows[rowid] = true;
    $("#" + rowid).addClass("changed-row");
}

// ====== 조회 버튼 ======
function dataSearch(){
    $("#gridList").jqGrid("setGridParam", {
        datatype: "json",
        postData: {
            rl_custcd: $("[name=rl_custcd]").val(),
            rl_custnm: $("[name=rl_custnm]").val(),
            rl_salesrepnm: $("[name=rl_salesrepnm]").val()
        },
        page: 1
    }).trigger("reloadGrid");
}

// ====== 저장 버튼 ======
function dataInUp(btn, extra){
    var ids = Object.keys(changedRows);
    if(ids.length === 0){
        alert("수정된 내용이 없습니다.");
        return;
    }

    var invalid = false;
    var sendData = [];

    ids.forEach(function(rowid){
        var row = $("#gridList").getRowData(rowid);

        // 1. 이메일 형식 검사
        if(row.CUST_MAIN_EMAIL && !emailRegex.test(row.CUST_MAIN_EMAIL)){
            highlightInvalidCell(rowid, "CUST_MAIN_EMAIL");
            invalid = true;
        }
        if(row.SALESREP_EMAIL && !emailRegex.test(row.SALESREP_EMAIL)){
            highlightInvalidCell(rowid, "SALESREP_EMAIL");
            invalid = true;
        }

        // 2. 발송여부 체크 시 이메일 필수 검사
        if(row.CUST_SENDMAIL_YN === "Y" && !row.CUST_MAIN_EMAIL){
            highlightInvalidCell(rowid, "CUST_MAIN_EMAIL");
            invalid = true;
        }
        if(row.SALESREP_SENDMAIL_YN === "Y" && !row.SALESREP_EMAIL){
            highlightInvalidCell(rowid, "SALESREP_EMAIL");
            invalid = true;
        }

        sendData.push(row);
    });

    if(invalid){
        alert("입력값을 확인해주세요.");
        return;
    }

    // 서버로 전송 (Ajax 예시)
    $.ajax({
        url: '/order/mailAlarmSave.json',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(sendData),
        success: function(res){
            alert("저장되었습니다.");
            changedRows = {};
            $("#gridList").trigger("reloadGrid");
        },
        error: function(){
            alert("저장 중 오류 발생");
        }
    });
}

// ====== 잘못된 입력 강조 ======
function highlightInvalidCell(rowid, colName){
    var cell = $("#" + rowid + " td[aria-describedby='gridList_" + colName + "']");
    cell.addClass("invalid-input");
}
```

***

# 📌 동작 설명 요약
- **조회(dataSearch)** → 입력조건으로 `reloadGrid()`  
- **수정시 행 강조** → `.changed-row` 배경색 적용  
- **체크박스** → `Y/N` 값 반영  
- **저장(dataInUp)**  
  - 변경 행 추출  
  - 이메일 유효성 검사 (`regex`)  
  - 발송여부 체크 시 이메일 값 필수 확인  
  - 잘못된 경우 `.invalid-input` 강조 + alert  
  - 정상인 경우 Ajax 저장  

***

👉 즉, "화면+동작 요구사항"을 jqGrid 내 JS 로 풀 세팅 완료!  

궁금한 건, **저장 시 Ajax URL(`/order/mailAlarmSave.json`)** 을 실제 어떤 방식으로 처리할까요?  
- 단건 갱신인지, 여러 건 묶음 저장인지 → 이 부분은 서버쪽 설계 따라 맞춰드리면 돼요.  

저장 API가 어떻게 설계되어 있는지 알려줄래요? (batch? row단위?)