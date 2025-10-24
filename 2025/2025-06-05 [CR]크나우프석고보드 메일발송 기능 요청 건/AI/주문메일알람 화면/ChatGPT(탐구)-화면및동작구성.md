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


좋아. 아래는 네가 준 3번 JSP(화면) 안에 **1. 화면 사양 + 2. 동작/유효성 검사**를 그대로 녹여 넣은 **완성 스크립트 포함 버전**이야.
백엔드 URL은 예시로 넣었으니(목록/저장) 실제 프로젝트 엔드포인트에 맞춰 바꿔 쓰면 돼.
포인트는 ▼

* jqGrid 컬럼: 메일/체크박스/비고 편집 가능(이메일 형식 체크), 체크박스 즉시 토글
* 변경 감지: 값 바뀌면 `changed-row` 배경 적용, 원복하면 해제
* 저장 버튼: 변경행 수집 → 유효성 검사(형식, 필수) → 문제가 없으면 AJAX 저장
* 조회: 상단 조건으로 `dataSearch()` 호출 시 그리드 재조회, TOTAL 카운트 갱신

---

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
    .selected-row { background-color: #E8F5E8 !important; }  /* 선택된 행 */
    .small-checkbox input[type="checkbox"] { width: 16px; height: 16px; }
    .invalid-input {
        outline: 2px solid #e57373 !important;  /* 빨간 테두리 */
        background-color: #fdecea !important;   /* 연한 빨간 배경 */
    }
</style>

<script type="text/javascript">
/* =========================
 * 주문메일알람 화면 스크립트
 * ========================= */
var originalRows = {};                 // 최초 로딩 원본값 저장용
var changedRowIds = new Set();         // 변경된 rowid 집합
var editingPos = { row: null, col: null }; // 현재 편집중 셀 좌표 저장

$(function () {
    initGrid();
    bindSearch();
});

/* -------------------------
 * Grid 초기화
 * ------------------------- */
function initGrid() {
    var colModel = [
        { name: "CUST_CD", key: true, label: '코드', width: 120, align: 'center', sortable: true },
        { name: "CUST_NM", label: '거래처명', width: 220, align: 'left', sortable: true },

        // 수정 가능. 이메일 형식 유효성 검사
        { name: "CUST_MAIN_EMAIL", label: '담당자 이메일', width: 220, align: 'center', sortable: true,
          editable: true, edittype: 'text' },

        // 체크박스: 선택/해제 토글 (Y/N)
        { name: "CUST_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center', sortable: true,
          editable: true, formatter: 'checkbox', edittype: 'checkbox',
          editoptions: { value: "Y:N" }, formatoptions: { disabled: false }, classes: 'small-checkbox' },

        { name: "SALESREP_NM", label: '영업 담당', width: 100, align: 'center', sortable: true },

        // 수정 가능. 이메일 형식 유효성 검사
        { name: "SALESREP_EMAIL", label: '영업 담당 이메일', width: 300, align: 'center', sortable: true,
          editable: true, edittype: 'text' },

        // 체크박스: 선택/해제 토글 (Y/N)
        { name: "SALESREP_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center', sortable: true,
          editable: true, formatter: 'checkbox', edittype: 'checkbox',
          editoptions: { value: "Y:N" }, formatoptions: { disabled: false }, classes: 'small-checkbox' },

        // 수정 가능
        { name: "COMMENTS", label: '비고', width: 450, align: 'left', sortable: true,
          editable: true, edittype: 'text' }
    ];

    $("#gridList").jqGrid({
        url: "${url}/admin/base/getOrderMailAlarmList.lime",   // 목록 조회 API (예시)
        mtype: "POST",
        datatype: "json",
        postData: getSearchParams,
        colModel: colModel,
        rowNum: 50,
        pager: "#pager",
        viewrecords: true,
        autowidth: true,
        shrinkToFit: false,
        height: 'auto',

        // 셀 단위 편집 (Enter/blur 저장)
        cellEdit: true,
        cellsubmit: 'clientArray',

        jsonReader: { repeatitems: false, id: "CUST_CD", root: "rows", page: "page", total: "total", records: "records" },

        // 로드 완료 시 원본 데이터 스냅샷/상태 초기화
        loadComplete: function () {
            var ids = $(this).jqGrid('getDataIDs');
            originalRows = {};
            changedRowIds.clear();
            for (var i = 0; i < ids.length; i++) {
                var id = ids[i];
                var r = $("#gridList").jqGrid('getLocalRow', id);
                originalRows[id] = pickRow(r);
                $("#" + id).removeClass("changed-row");
            }
            updateTotal();
        },
        gridComplete: updateTotal,

        // 편집 시작: blur 시 자동 저장
        afterEditCell: function (rowid, cellname, value, iRow, iCol) {
            editingPos = { row: iRow, col: iCol };
            var $input = $('input,textarea', this.rows[iRow].cells[iCol]);
            if ($input.length) {
                $input.off('blur.jg').on('blur.jg', function () {
                    $("#gridList").jqGrid('saveCell', iRow, iCol);
                });
            }
        },

        // 셀 저장 후 변경감지
        afterSaveCell: function (rowid, cellname, value, iRow, iCol) {
            evaluateRowChange(rowid);
        },

        // 체크박스 클릭 시 즉시 변경감지
        onCellSelect: function (rowid, iCol) {
            var cm = this.p.colModel[iCol];
            if (cm && (cm.name === 'CUST_SENDMAIL_YN' || cm.name === 'SALESREP_SENDMAIL_YN')) {
                setTimeout(function () { evaluateRowChange(rowid); }, 0);
            }
        },

        loadError: function (xhr, status, err) {
            alert('목록 조회 중 오류가 발생했습니다.\n' + status + ' ' + err);
        }
    });
}

/* -------------------------
 * 공통 유틸
 * ------------------------- */
function pickRow(r) {
    return {
        CUST_MAIN_EMAIL: (r.CUST_MAIN_EMAIL || "").trim(),
        CUST_SENDMAIL_YN: normYN(r.CUST_SENDMAIL_YN),
        SALESREP_EMAIL: (r.SALESREP_EMAIL || "").trim(),
        SALESREP_SENDMAIL_YN: normYN(r.SALESREP_SENDMAIL_YN),
        COMMENTS: (r.COMMENTS || "").trim()
    };
}

function getRowNow(rowid) {
    var r = $("#gridList").jqGrid('getLocalRow', rowid) || $("#gridList").jqGrid('getRowData', rowid);
    return pickRow(r);
}

function normYN(v) {
    if (v === true) return "Y";
    if (v === false) return "N";
    if (v == null) return "";
    v = ("" + v).toUpperCase();
    if (v === "YES" || v === "TRUE" || v === "1") return "Y";
    if (v === "NO" || v === "FALSE" || v === "0") return "N";
    return v; // Y/N 그대로
}

function evaluateRowChange(rowid) {
    var now = getRowNow(rowid);
    var origin = originalRows[rowid] || {};
    var changed =
        now.CUST_MAIN_EMAIL !== origin.CUST_MAIN_EMAIL ||
        now.CUST_SENDMAIL_YN !== origin.CUST_SENDMAIL_YN ||
        now.SALESREP_EMAIL !== origin.SALESREP_EMAIL ||
        now.SALESREP_SENDMAIL_YN !== origin.SALESREP_SENDMAIL_YN ||
        now.COMMENTS !== origin.COMMENTS;

    if (changed) {
        $("#" + rowid).addClass("changed-row");
        changedRowIds.add(rowid);
    } else {
        $("#" + rowid).removeClass("changed-row");
        changedRowIds.delete(rowid);
    }
}

function updateTotal() {
    $("#listTotalCountSpanId").text($("#gridList").getGridParam('records') || 0);
}

function isEmail(v) {
    // 간단하고 실전적인 메일 형식 체크
    var re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return !!re.test(v);
}

function markInvalidCell(rowid, col) {
    var sel = '#' + $.jgrid.jqID(rowid) + ' td[aria-describedby="gridList_' + col + '"]';
    $(sel).addClass('invalid-input');
}

/* -------------------------
 * 조회(Search)
 * ------------------------- */
function bindSearch() {
    window.dataSearch = function () {
        // 편집중 셀 저장
        if (editingPos.row != null) {
            $("#gridList").jqGrid('saveCell', editingPos.row, editingPos.col);
            editingPos = { row: null, col: null };
        }
        $("#gridList")
            .jqGrid('setGridParam', { page: 1, postData: getSearchParams })
            .trigger("reloadGrid");
    };
}

// 조회조건 수집
function getSearchParams() {
    var yn = [];
    $('input[name="r_salesrepcdyn"]:checked').each(function () { yn.push(this.value); });
    return {
        rl_custcd: $('input[name="rl_custcd"]').val(),
        rl_custnm: $('input[name="rl_custnm"]').val(),
        rl_salesrepnm: $('input[name="rl_salesrepnm"]').val(),
        r_salesrepcdyn: yn.join(',')
    };
}

/* -------------------------
 * 저장(In/Up) + 유효성 검사
 * ------------------------- */
function dataInUp(btn, etc) {
    // 편집중 셀 저장
    if (editingPos.row != null) {
        $("#gridList").jqGrid('saveCell', editingPos.row, editingPos.col);
        editingPos = { row: null, col: null };
    }

    // 기존 에러 표시 제거
    $('#gridList td').removeClass('invalid-input');

    if (changedRowIds.size === 0) {
        alert('수정된 내용이 없습니다.');
        return;
    }

    var errors = [];
    var changedData = [];

    changedRowIds.forEach(function (id) {
        var base = $("#gridList").jqGrid('getLocalRow', id) || {};
        var row = getRowNow(id);

        // 이메일 형식 체크(값이 있을 때)
        if (row.CUST_MAIN_EMAIL && !isEmail(row.CUST_MAIN_EMAIL)) {
            markInvalidCell(id, 'CUST_MAIN_EMAIL');
            errors.push('[' + (base.CUST_NM || base.CUST_CD || id) + '] 담당자 이메일 형식이 올바르지 않습니다.');
        }
        if (row.SALESREP_EMAIL && !isEmail(row.SALESREP_EMAIL)) {
            markInvalidCell(id, 'SALESREP_EMAIL');
            errors.push('[' + (base.CUST_NM || base.CUST_CD || id) + '] 영업 담당 이메일 형식이 올바르지 않습니다.');
        }

        // 발송 체크 시 이메일 필수
        if (row.CUST_SENDMAIL_YN === 'Y' && !row.CUST_MAIN_EMAIL) {
            markInvalidCell(id, 'CUST_MAIN_EMAIL');
            errors.push('[' + (base.CUST_NM || base.CUST_CD || id) + '] 발송 여부(Y)인데 담당자 이메일이 비어 있습니다.');
        }
        if (row.SALESREP_SENDMAIL_YN === 'Y' && !row.SALESREP_EMAIL) {
            markInvalidCell(id, 'SALESREP_EMAIL');
            errors.push('[' + (base.CUST_NM || base.CUST_CD || id) + '] 발송 여부(Y)인데 영업 담당 이메일이 비어 있습니다.');
        }

        changedData.push({
            CUST_CD: base.CUST_CD,
            CUST_MAIN_EMAIL: row.CUST_MAIN_EMAIL,
            CUST_SENDMAIL_YN: row.CUST_SENDMAIL_YN,
            SALESREP_EMAIL: row.SALESREP_EMAIL,
            SALESREP_SENDMAIL_YN: row.SALESREP_SENDMAIL_YN,
            COMMENTS: row.COMMENTS
        });
    });

    if (errors.length > 0) {
        alert(errors.join('\n'));
        return;
    }

    var schedH = $('select[name="scheduleTime"]').val() || '';
    var schedM = $('select[name="scheduleMinute"]').val() || '';

    $.ajax({
        url: "${url}/admin/base/saveOrderMailAlarm.lime",     // 저장 API (예시)
        type: "POST",
        contentType: "application/json; charset=UTF-8",
        data: JSON.stringify({
            scheduleHour: schedH,
            scheduleMinute: schedM,
            rows: changedData
        }),
        success: function (res) {
            alert('저장되었습니다.');
            $("#gridList").trigger('reloadGrid');
        },
        error: function (xhr) {
            alert('저장 중 오류가 발생했습니다.\n' + xhr.status + ' ' + xhr.statusText);
        }
    });
}

/* -------------------------
 * 기타(엑셀다운로드)
 * ------------------------- */
function excelDown(btn) {
    alert('엑셀 다운로드는 서버 구현 후 사용하세요.');
}
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

---

### 사용 팁(요점 정리)

* **체크박스 토글 불가 이슈 방지**: jqGrid 컬럼에 `formatter:'checkbox'`, `edittype:'checkbox'`, `formatoptions:{disabled:false}`, `editoptions:{value:"Y:N"}` 조합을 써서 클릭 즉시 값이 Y/N으로 반영되도록 했어.
* **변경 감지/원복 처리**: 최초 로드 시 `originalRows` 스냅샷 저장 → 셀 저장/체크박스 클릭 때마다 현재값과 원본 비교 → 다르면 행에 `changed-row` 클래스, 원복되면 제거.
* **유효성 검사**: 저장 전에

  * 변경 행이 없으면 “수정된 내용이 없습니다.” 알림
  * 이메일 형식 체크(값이 있을 때만)
  * 발송여부=Y면 해당 이메일 필수
  * 에러셀은 `invalid-input` 스타일로 표시
* **TOTAL 카운트**: `gridComplete/loadComplete`에서 records 읽어 갱신.
* **조회/저장**: `url`은 예시이니 프로젝트 실경로로 바꾸면 바로 붙일 수 있어.
