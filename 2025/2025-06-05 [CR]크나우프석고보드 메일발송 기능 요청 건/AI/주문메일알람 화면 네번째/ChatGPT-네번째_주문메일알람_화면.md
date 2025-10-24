[Request to fix JQGrid malfunction].

## Symptom summary:
    The following malfunctions occur on jqGrid-based screens.

    1) The multiselect checkbox is not automatically selected when selecting whether to ship (checkbox).
    2) The row background color does not change when selecting whether to ship (checkbox).
    3) The multiselect checkbox is not unchecked when the checkbox is cleared.
    4) Row background color does not return to its original color when unchecked.
    5) Contact email / Sales email / Remarks columns would change background color when multiselect checkbox was checked **just by clicking**.
    6) The multiselect checkbox/background color does not return to its original state after modifying the Contact Email / Sales Email / Remarks values and then reverting them back to their original values.
    7) "Mail format error" warning pops up when clicking save button even though the email address is correct.
    8) "Mail format error" warning pops up when clicking the Save button even though the multiselect checkbox is unchecked.

## Development Environment:
    - OS: Windows
    - IDE: STS4.5
    - Java: Zulu-8 (zulu8.88.0.19)
    - WAS: Tomcat 9.0.99
    - DB: MSSQL 2019 (15.0.4415.2, RTM, Standard Edition 64-bit)
    - Build: Maven
    - ORM: MyBatis
    - Grid: JQGrid

## Requests:
    A. Please fix the multiselect checkbox and the row background color to synchronize when the sendability checkbox is checked/unchecked.
    B. Please make the multiselect checkbox and background color change "only if the actual value changes" for emails/sales emails/remarks.
    C. Make sure the multiselect checkbox and background color are restored when reverting to the original value.
    D. Check the email format validation logic and fix it so that valid email addresses pass without error.
    E. Exclude rows with unchecked multiselect checkboxes from email format validation on save.
    F. Please provide the full code of the required modifications on the frontend (JS/JQGrid) and backend (Controller/MyBatis/SQL).
    G. Please ensure that the multiselect checkbox and background color are returned to their initial state after a successful save.


## Acceptance Criteria (AC):

    1. background color is consistent with multiselect when checking/unchecking sendability
    2. email/sales email/remarks columns only change state on "actual value change"
    3. multiselect/background color is also restored when restoring original value
    4. email format validation on save now works correctly (correct mails pass, only incorrect mails error)
    5. multiselect unchecked rows are not subject to validation
    6. state reset works correctly after successful save

## Current code :
```html
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

<script type="text/javascript" src="${url}/include/js/common/select2/select2.js"></script>
<link rel="stylesheet" href="${url}/include/js/common/select2/select2.css" />

<script type="text/javascript">
// 이메일 형식 유효성 검사 함수
function validateEmail(email) {
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
}

// 배경색 변경 함수
function changeRowBackground(rowId, isChanged) {
    if (isChanged) {
        $('#gridList #' + rowId).css('background-color', '#ffebcd'); // 연한 주황색
    } else {
        $('#gridList #' + rowId).css('background-color', ''); // 원래 색상
    }
}

// 체크박스 포맷터
function checkboxFormatter(cellVal, options, rowObj) {
    var checked = (cellVal === 'Y') ? 'checked' : '';
    return '<input type="checkbox" class="mail-checkbox" ' + checked + ' onclick="handleCheckboxClick(this, \'' + options.rowId + '\', \'' + options.colModel.name + '\')" />';
}

// 체크박스 클릭 이벤트 핸들러
function handleCheckboxClick(checkbox, rowId, fieldName) {
    var newValue = checkbox.checked ? 'Y' : 'N';
    
    // JQGrid의 setCell 메소드를 사용하여 값 변경 및 dirty 플래그 설정
    // 이렇게 해야 JQGrid가 이 셀이 변경되었음을 인지합니다.
    $('#gridList').jqGrid('setCell', rowId, fieldName, newValue, '', {dirty: true});
    
    // 즉시 배경색 변경
    changeRowBackground(rowId, true);
}

// 이메일 필드 유효성 검사
function validateEmailField(e) {
    var email = $(e.target).val();
    if (email && !validateEmail(email)) {
        alert('올바른 이메일 형식을 입력해주세요.');
        $(e.target).focus();
        return false;
    }
}

$(function(){
    getGridList();
});

// JQGrid 변경된 행을 가져오는 함수
function getModifiedRows() {
    const grid = $('#gridList');
    const ids = grid.getDataIDs();
    const modifiedRows = [];
    
    $.each(ids, function(i, rowId) {
        // JQGrid의 getRowData(rowId, true)를 사용하면 dirty 플래그가 있는 행만 가져올 수 있습니다.
        const rowData = grid.getRowData(rowId, true);
        if (rowData) {
            modifiedRows.push(rowData);
        }
    });
    
    return modifiedRows;
}

var lastSelection;
function editRow(id){
    if (id && id !== lastSelection) {
        var grid = $('#gridList');
        grid.jqGrid('editRow', id, {keys: true, focusField: true});
        lastSelection = id;
    }
}

function dataSave(obj) {
    $(obj).prop('disabled', true);
    
    // JQGrid의 getRowData(rowId, true)를 사용해 dirty 플래그가 있는 행만 가져옴
    var modifiedRows = getModifiedRows();

    if (modifiedRows.length === 0) {
        alert('수정된 내용이 없습니다.');
        $(obj).prop('disabled', false);
        return false;
    }
    
    // 유효성 검사
    var validationFailed = false;
    $.each(modifiedRows, function(i, rowData) {
        // 이메일 형식 검사
        if (rowData.CUST_MAIN_EMAIL && !validateEmail(rowData.CUST_MAIN_EMAIL)) {
            alert('거래처 담당자 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
        
        if (rowData.SALESREP_EMAIL && !validateEmail(rowData.SALESREP_EMAIL)) {
            alert('영업 담당 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
        
        // 발송 여부 체크 시 이메일 존재 확인
        if (rowData.CUST_SENDMAIL_YN === 'Y' && !rowData.CUST_MAIN_EMAIL) {
            alert('담당자 이메일 발송이 체크되어 있지만 담당자 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
        
        if (rowData.SALESREP_SENDMAIL_YN === 'Y' && !rowData.SALESREP_EMAIL) {
            alert('영업 담당 이메일 발송이 체크되어 있지만 영업 담당 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
    });
    
    if (validationFailed) {
        $(obj).prop('disabled', false);
        return false;
    }
    
    // 데이터 준비
    var iFormObj = $('form[name="iForm"]');
    iFormObj.empty();
    
    $.each(modifiedRows, function(i, rowData) {
        iFormObj.append('<input type="hidden" name="custCd" value="' + rowData.CUST_CD + '" />');
        iFormObj.append('<input type="hidden" name="custMainEmail" value="' + (rowData.CUST_MAIN_EMAIL || '') + '" />');
        iFormObj.append('<input type="hidden" name="custSendmailYn" value="' + (rowData.CUST_SENDMAIL_YN || 'N') + '" />');
        iFormObj.append('<input type="hidden" name="salesrepEmail" value="' + (rowData.SALESREP_EMAIL || '') + '" />');
        iFormObj.append('<input type="hidden" name="salesrepSendmailYn" value="' + (rowData.SALESREP_SENDMAIL_YN || 'N') + '" />');
        iFormObj.append('<input type="hidden" name="comments" value="' + (rowData.COMMENTS || '') + '" />');
    });
    
    if (confirm('저장 하시겠습니까?')) {
        var iFormData = iFormObj.serialize();
        var url = '${url}/admin/system/updateOrderMailAlarmAjax.lime'; 
        $.ajax({
            async : false,
            data : iFormData,
            type : 'POST',
            url : url,
            success : function(data) {
                if (data.RES_CODE == '0000') {
                    alert(data.RES_MSG);
                    
                    // 성공적으로 저장된 후 배경색 초기화 및 그리드 리로드
                    $.each(modifiedRows, function(i, rowData) {
                        changeRowBackground(rowData.CUST_CD, false);
                    });
                    
                    dataSearch(); // 그리드 리로드로 최신 데이터 반영
                }else{
                    alert(data.RES_MSG);
                }
                $(obj).prop('disabled', false);
            },
            error : function(request,status,error){
                alert('Error');
                $(obj).prop('disabled', false);
            }
        });
    }else{
        $(obj).prop('disabled', false);
    }
}

// ==================================================================================
// jqGrid Columns Order 설정
// ==================================================================================
//Start. Setting Jqgrid Columns Order.
var ckNameJqGrid = 'admin/customer/customerList/jqGridCookie'; // 페이지별 쿠키명 설정
ckNameJqGrid += '/gridList'; // 그리드명별 쿠키명 설정

var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
var globalColumnOrder = globalColumnOrderStr.split(',');

var defaultColModel = [
    {name:"CUST_CD", key:true, label:'거래처코드', width:120, align:'center', sortable:true},
    {name:"CUST_NM", label:'거래처명', width:220, align:'left', sortable:true},
    {name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true, editable:true, editoptions:{dataEvents:[{type:'blur', fn:validateEmailField}]}},
    {name:"CUST_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true, formatter:checkboxFormatter, editable:true, edittype:'checkbox'},
    {name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true},
    {name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:300, align:'center', sortable:true, editable:true, editoptions:{dataEvents:[{type:'blur', fn:validateEmailField}]}},
    {name:"SALESREP_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true, formatter:checkboxFormatter, editable:true, edittype:'checkbox'},
    {name:"COMMENTS", label:'비고', width:450, align:'left', sortable:true, editable:true}
];

var defaultColumnOrder = writeIndexToStr(defaultColModel.length);
var updateComModel = [];

// 쿠키에서 컬럼 순서 복원
if (0 < globalColumnOrder.length) { // 쿠키값이 있을때
    if (defaultColModel.length == globalColumnOrder.length) {
        for (var i = 0, j = globalColumnOrder.length; i < j; i++) {
            updateComModel.push(defaultColModel[globalColumnOrder[i]]);
        }
        setCookie(ckNameJqGrid, globalColumnOrder, 365);
    } else {
        updateComModel = defaultColModel;
        setCookie(ckNameJqGrid, defaultColumnOrder, 365);
    }
} else { // 쿠키값이 없을때
    updateComModel = defaultColModel;
    setCookie(ckNameJqGrid, defaultColumnOrder, 365);
}

// ==================================================================================
// jqGrid Column Width 설정
// ==================================================================================
var ckNameJqGridWidth = ckNameJqGrid + '/width'; // 페이지별 쿠키명 설정
var globalColumnWidthStr = toStr(decodeURIComponent(getCookie(ckNameJqGridWidth)));
var globalColumnWidth = globalColumnWidthStr.split(',');
var defaultColumnWidthStr = '';
var defaultColumnWidth;
var updateColumnWidth;

if ('' != globalColumnWidthStr) { // 쿠키값이 있을때
    if (updateComModel.length == globalColumnWidth.length) {
        updateColumnWidth = globalColumnWidth;
    } else {
        for (var j = 0; j < updateComModel.length; j++) {
            if ('rn' != updateComModel[j].name && 'cb' != updateComModel[j].name) {
                var v = ('' != toStr(updateComModel[j].width)) ? toStr(updateComModel[j].width) : '0';
                if ('' == defaultColumnWidthStr) {
                    defaultColumnWidthStr = v;
                } else {
                    defaultColumnWidthStr += ',' + v;
                }
            }
        }
        defaultColumnWidth = defaultColumnWidthStr.split(',');
        updateColumnWidth = defaultColumnWidth;
        setCookie(ckNameJqGridWidth, defaultColumnWidth, 365);
    }
} else { // 쿠키값이 없을때
    for (var j = 0; j < updateComModel.length; j++) {
        if ('rn' != updateComModel[j].name && 'cb' != updateComModel[j].name) {
            var v = ('' != toStr(updateComModel[j].width)) ? toStr(updateComModel[j].width) : '0';
            if ('' == defaultColumnWidthStr) {
                defaultColumnWidthStr = v;
            } else {
                defaultColumnWidthStr += ',' + v;
            }
        }
    }
    defaultColumnWidth = defaultColumnWidthStr.split(',');
    updateColumnWidth = defaultColumnWidth;
    setCookie(ckNameJqGridWidth, defaultColumnWidth, 365);
}

// 컬럼 너비 적용
if (updateComModel.length == globalColumnWidth.length) {
    for (var j = 0; j < updateComModel.length; j++) {
        updateComModel[j].width = toStr(updateColumnWidth[j]);
    }
}


function getGridList(){
    var searchData = getSearchData();
    $('#gridList').jqGrid({
        url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
        editurl: 'clientArray',
        datatype: "json",
        mtype: 'POST',
        postData: searchData,
        colModel: updateComModel,
        height: '360px',
        autowidth: false,
        multiselect: true,
        rowNum: 10,              // 기본 페이지당 출력 행 수
        rowList: ['10', '30', '50', '100'], // 페이지당 행 수 옵션
        rownumbers: true,         // 행 번호 표시
        pagination: true,
        pager: "#pager",
        actions : true,
        pginput : true,
        // JQGrid가 행을 식별하는 고유 키를 CUST_CD로 설정
        // 이 설정이 매우 중요합니다.
        jsonReader: {
            root: 'list',
            id: 'CUST_CD'
        },
        
        // 열 순서 변경 이벤트
        sortable: {
            update: function(relativeColumnOrder) {
                var grid = $('#gridList');
                var defaultColIndicies = [];
                for (var i = 0; i < defaultColModel.length; i++) {
                    defaultColIndicies.push(defaultColModel[i].name);
                }
                globalColumnOrder = [];
                var columnOrder = [];
                var currentColModel = grid.getGridParam('colModel');
                for (var j = 0; j < relativeColumnOrder.length; j++) {
                    if ('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name) {
                        columnOrder.push(defaultColIndicies.indexOf(currentColModel[j].name));
                    }
                }
                globalColumnOrder = columnOrder;
                setCookie(ckNameJqGrid, globalColumnOrder, 365);
                var tempUpdateColumnWidth = [];
                for (var j = 0; j < currentColModel.length; j++) {
                    if ('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name) {
                        tempUpdateColumnWidth.push(currentColModel[j].width);
                    }
                }
                updateColumnWidth = tempUpdateColumnWidth;
                setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
            }
        },

        // 열 크기 조정 후 실행되는 이벤트
        resizeStop: function(width, index) {
            console.log('globalColumnOrder : ', globalColumnOrder);
            var minusIdx = 0;
            var grid = $('#gridList');
            var currentColModel = grid.getGridParam('colModel');
            if ('rn' == currentColModel[0].name || 'cb' == currentColModel[0].name) minusIdx--;
            if ('rn' == currentColModel[1].name || 'cb' == currentColModel[1].name) minusIdx--;
            var resizeIdx = index + minusIdx;
            updateColumnWidth[resizeIdx] = width;
            setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
        },

        sortorder: 'desc',
        
        // 인라인 편집 종료 시 이벤트
        afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
            // 변경된 셀이 있는 행에 배경색 적용
            changeRowBackground(rowid, true);
        },

        onSelectRow: function(rowId){
            // 행 선택 시 인라인 편집 모드 진입
            editRow(rowId);
        }
    });
}

function getSearchData(){
    var searchData = {
        custCd : $('input[name="searchCustCd"]').val(),
        custNm : $('input[name="searchCustNm"]').val(),
        salesrepNm : $('input[name="searchSalesrepNm"]').val()
    };
    return searchData;
}

// 조회
function dataSearch() {
    var searchData = getSearchData();
    $('#gridList').setGridParam({
        postData : searchData
    }).trigger("reloadGrid");
}

// 엑셀다운로드 (기존 코드와 동일)
function excelDown(obj){
    $('#ajax_indicator').show().fadeIn('fast');
    var token = getFileToken('excel');
    $('form[name="frm"]').append('<input type="hidden" name="filetoken" value="'+token+'" />');
    
    formPostSubmit('frm', '${url}/admin/system/orderMailAlarmExcelDown.lime');
    $('form[name="frm"]').attr('action', '');
    
    $('input[name="filetoken"]').remove();
    var fileTimer = setInterval(function() {
        if('true' == getCookie(token)){
            $('#ajax_indicator').fadeOut();
            delCookie(token);
            clearInterval(fileTimer);
        }
    }, 1000 );
}
</script>
</head>

<body class="page-header-fixed compact-menu">
    <div id="ajax_indicator" style="display:none;">
        <p style="position: absolute; top: 50%; left: 50%; margin: -110px 0 0 -110px;">
            <img src="${url}/include/images/common/loadingbar.gif" />
        </p>
    </div>

    <main class="page-content content-wrap">
    
        <%@ include file="/WEB-INF/views/include/admin/header.jsp" %>
        <%@ include file="/WEB-INF/views/include/admin/left.jsp" %>
        
        <%-- 임의 form --%>
        <form name="iForm" method="post"></form>
        
        <form name="frm" method="post">
        
        <div class="page-inner">
            <div class="page-title">
                <h3>
                    주문메일알람 관리
                    <div class="page-right">
                        <button type="button" class="btn btn-line f-black" title="검색" onclick="dataSearch();"><i class="fa fa-search"></i><em>검색</em></button>
                        <button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();"><i class="fa fa-refresh"></i><em>새로고침</em></button>
                        <button type="button" class="btn btn-line f-black" title="엑셀다운로드" onclick="excelDown(this);"><i class="fa fa-file-excel-o"></i><em>엑셀다운로드</em></button>
                    </div>
                </h3>
            </div>
            
            <div id="main-wrapper">
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
                                                    <input type="text" class="search-input" name="searchCustCd" value="${param.searchCustCd}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                </div>
                                            </li>
                                            <li>
                                                <label class="search-h">거래처명</label>
                                                <div class="search-c">
                                                    <input type="text" class="search-input" name="searchCustNm" value="${param.searchCustNm}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                </div>
                                            </li>
                                            <li>
                                                <label class="search-h">영업담당</label>
                                                <div class="search-c">
                                                    <input type="text" class="search-input" name="searchSalesrepNm" value="${param.searchSalesrepNm}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                                </div>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="panel-body">
                                <h5 class="table-title listT">TOTAL <span id="listTotalCountSpanId">0</span>EA</h5>
                                <div class="btnList writeObjectClass">
                                    <button type="button" class="btn btn-info" onclick="dataSave(this);">저장</button>
                                </div>
                                <div class="table-responsive in">
                                    <table id="gridList" class="display table tableList nowrap" width="100%" border="0" cellpadding="0" cellspacing="0"></table>
                                    <div id="pager"></div>
                                </div>
                            </div>
                            
                        </div>
                    </div>
                </div>
                </div>
            <%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>
            
        </div>
        
        </form>
    </main>
    
</body>

</html>
```

---------------------------------------------------------------------
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
---------------------------------------------------------------------

좋아, 증상/요구사항 딱 핵심이야. 아래처럼 “실제 값 변화만 더티 처리(+배경색/멀티셀렉트 동기화)”로 전면 개편해줄게.
프론트(JS/JQGrid) 전체 수정안 + 백엔드(Controller/MyBatis/SQL)까지 “바로 붙여 쓸 수 있는” **완전 코드**로 제공해.

---

# 설계 요약

* **원본 스냅샷(origMap)**: `loadComplete` 때 행별 원본값 저장 → 이후 어떤 이벤트(체크박스/에디트/블러 등)에서도 **현재값 vs 원본값**을 비교하여 “실변경 여부” 판단.
* **더티 마킹 규칙**

  * 값이 **원본과 다를 때만**: 행에 `changed-row` 클래스 부여 + jqGrid multiselect(체크박스) **선택**.
  * 값이 **원본과 동일해지면**: `changed-row` 제거 + multiselect **해제**.
* **체크박스(발송 여부) 동작**

  * `CUST_SENDMAIL_YN`, `SALESREP_SENDMAIL_YN` 클릭 시 셀값을 ‘Y/N’로 토글 → 즉시 `applyDirtyRule(rowId)` 호출(= 배경색/선택 동기화).
  * 체크만 했다고 더티가 되지 않음(= 원본과 같으면 더티 아님).
* **행 선택(beforeSelectRow)**

  * 에디터/체크박스 클릭이 **멀티셀렉트 오동작**을 유발하지 않도록: **멀티셀렉트 전용 칼럼(cb) 외 클릭은 선택 토글 금지**.
* **이메일 유효성 검사**

  * 저장 시 **멀티셀렉트로 선택된 행만** 검사/저장(AC-5).
  * 정규식: `^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$` (프론트/백 동일 적용).
  * 선택되어도 두 발송여부가 모두 ‘N’이면 이메일 공란 허용.
* **저장 성공 후 초기화**

  * 서버 반영 성공 시 **origMap 재생성** + `changed-row` 전부 제거 + 멀티셀렉트 전부 해제(AC-6).

---

# 프론트엔드 (JSP/JS) ― **완전치환 코드**

아래 `<script>` 블록만 기존 페이지에 **치환**하면 됨. (그 외 HTML/JSP는 그대로 사용 가능)

```html
<script type="text/javascript">
// =========================== 공통 유틸 ===========================
const EMAIL_RE = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
function isValidEmail(v){ return EMAIL_RE.test((v||'').trim()); }

// 행 배경색 제어
function setRowChangedVisual(rowId, changed){
    const $tr = $('#gridList').find('#'+rowId);
    if (changed) $tr.addClass('changed-row');
    else $tr.removeClass('changed-row');
}

// jqGrid multiselect 동기화
function setRowSelected(rowId, selected){
    const grid = $('#gridList');
    const sel = grid.jqGrid('getGridParam','selarrrow') || [];
    const isSelected = sel.indexOf(String(rowId)) >= 0;
    if (selected && !isSelected) grid.jqGrid('setSelection', rowId, false);
    if (!selected && isSelected) grid.jqGrid('resetSelection', rowId);
}

// =========================== 더티 추적 ===========================
// 원본 스냅샷: { rowId: {CUST_MAIN_EMAIL:'', CUST_SENDMAIL_YN:'N', ...} }
var origMap = {};
// 더티 판단 대상 컬럼
var TRACK_COLS = [
  'CUST_MAIN_EMAIL','CUST_SENDMAIL_YN',
  'SALESREP_EMAIL','SALESREP_SENDMAIL_YN',
  'COMMENTS'
];

function takeSnapshot(){
    const grid = $('#gridList');
    origMap = {};
    const ids = grid.getDataIDs();
    ids.forEach(function(id){
        const rd = grid.getRowData(id);
        origMap[id] = {};
        TRACK_COLS.forEach(function(c){ origMap[id][c] = (rd[c]||'').trim(); });
    });
}

function rowIsDirty(rowId){
    const grid = $('#gridList');
    const cur = grid.getRowData(rowId);
    const base = origMap[rowId] || {};
    for (let c of TRACK_COLS){
        const a = (cur[c]||'').trim();
        const b = (base[c]||'').trim();
        if (a !== b) return true;
    }
    return false;
}

// 값 적용 + 비주얼/선택 동기화
function applyDirtyRule(rowId){
    const dirty = rowIsDirty(rowId);
    setRowChangedVisual(rowId, dirty);
    setRowSelected(rowId, dirty);
}

// =========================== 편집 헬퍼 ===========================
function editRow(rowId){
    const grid = $('#gridList');
    // 이미 편집중이면 패스
    const tr = $('#'+rowId);
    if (tr.hasClass('jqgrid-new-row')) return;
    grid.jqGrid('editRow', rowId, {
        keys: true,
        oneditfunc: function(){},
        aftersavefunc: function(){ applyDirtyRule(rowId); },
        restoreAfterError: true
    });
}

// 셀 값 토글(Y/N)
function toggleYN(grid, rowId, colName){
    const cur = (grid.getCell(rowId, colName)+'').trim().toUpperCase();
    const next = (cur === 'Y') ? 'N' : 'Y';
    grid.jqGrid('setCell', rowId, colName, next);
    applyDirtyRule(rowId);
}

// =========================== 그리드 ===========================
var colModel = [
  {name:"CUST_CD", key:true, label:"거래처코드", width:120, align:"center", sortable:true},
  {name:"CUST_NM", label:"거래처명", width:220, align:"left", sortable:true},
  {name:"CUST_MAIN_EMAIL", label:"담당자 이메일", width:220, align:"center", sortable:true, editable:true,
    edittype:'text',
    editoptions:{ size:30, maxlength:200,
      dataEvents:[
        { type:'blur', fn:function(e){ 
            // 프론트 즉시 경고는 완화(저장 시 최종검사). 단, 빨간 테두리 정도만.
            const v = $(this).val();
            $(this).toggleClass('invalid-input', !!v && !isValidEmail(v));
            const rowId = $(this).closest('tr.jqgrow').attr('id');
            applyDirtyRule(rowId);
        }},
        { type:'keyup', fn:function(e){
            const rowId = $(this).closest('tr.jqgrow').attr('id');
            applyDirtyRule(rowId);
        }}
      ]
    }
  },
  {name:"CUST_SENDMAIL_YN", label:"발송 여부", width:100, align:"center", sortable:true,
    formatter:function(cell, opt, row){
      const checked = (String(cell).toUpperCase()==='Y') ? 'checked' : '';
      return '<input type="checkbox" class="ynbox yn-cust" '+checked+' data-row="'+opt.rowId+'" />';
    }
  },
  {name:"SALESREP_NM", label:"영업 담당", width:100, align:"center", sortable:true},
  {name:"SALESREP_EMAIL", label:"영업 담당 이메일", width:300, align:"center", sortable:true, editable:true,
    edittype:'text',
    editoptions:{ size:40, maxlength:200,
      dataEvents:[
        { type:'blur', fn:function(e){ 
            const v = $(this).val();
            $(this).toggleClass('invalid-input', !!v && !isValidEmail(v));
            const rowId = $(this).closest('tr.jqgrow').attr('id');
            applyDirtyRule(rowId);
        }},
        { type:'keyup', fn:function(e){
            const rowId = $(this).closest('tr.jqgrow').attr('id');
            applyDirtyRule(rowId);
        }}
      ]
    }
  },
  {name:"SALESREP_SENDMAIL_YN", label:"발송 여부", width:100, align:"center", sortable:true,
    formatter:function(cell,opt,row){
      const checked = (String(cell).toUpperCase()==='Y') ? 'checked' : '';
      return '<input type="checkbox" class="ynbox yn-sales" '+checked+' data-row="'+opt.rowId+'" />';
    }
  },
  {name:"COMMENTS", label:"비고", width:450, align:"left", sortable:true, editable:true,
    edittype:'text',
    editoptions:{ maxlength:4000,
      dataEvents:[
        { type:'blur', fn:function(e){
            const rowId = $(this).closest('tr.jqgrow').attr('id');
            applyDirtyRule(rowId);
        }},
        { type:'keyup', fn:function(e){
            const rowId = $(this).closest('tr.jqgrow').attr('id');
            applyDirtyRule(rowId);
        }}
      ]
    }
  }
];

$(function(){
  buildGrid();
  bindGridDomEvents();
});

function buildGrid(){
  $('#gridList').jqGrid({
    url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
    datatype: "json",
    mtype: "POST",
    editurl: "clientArray",
    colModel: colModel,
    height: 360,
    multiselect: true,
    rowNum: 10,
    rowList: [10,30,50,100],
    rownumbers: true,
    pager: "#pager",
    jsonReader: { root:'list', id:'CUST_CD' },
    onCellSelect: function(rowId, iCol, cellContent){
      const grid = $(this);
      const cm = grid.jqGrid('getGridParam','colModel');
      const colName = cm[iCol] && cm[iCol].name;
      // 클릭만으로 멀티셀렉트가 바뀌지 않도록, 여기서는 편집만
      if (['CUST_MAIN_EMAIL','SALESREP_EMAIL','COMMENTS'].includes(colName)){
          editRow(rowId);
      }
      // 발송여부 칼럼은 토글 처리
      if (colName === 'CUST_SENDMAIL_YN' || colName === 'SALESREP_SENDMAIL_YN'){
          toggleYN(grid, rowId, colName);
      }
    },
    beforeSelectRow: function(rowId, e){
      // multiselect 토글은 **cb 칼럼 클릭시에만 허용**
      const $t = $(e.target);
      const $td = $t.closest('td');
      const iCol = $.jgrid.getCellIndex($td[0]);
      const cm = $(this).jqGrid('getGridParam','colModel');
      const colName = cm[iCol] && cm[iCol].name;
      // cb(멀티셀렉트)일 때만 true. 나머지는 선택 토글 금지.
      return (colName === 'cb');
    },
    loadComplete: function(){
      takeSnapshot(); // 원본 스냅샷
      // 초기 비주얼 정리
      const grid = $('#gridList');
      const ids = grid.getDataIDs();
      ids.forEach(function(id){
        setRowChangedVisual(id, false);
      });
      grid.jqGrid('resetSelection');
      // 동적 이벤트(체크박스) 위임
      bindYnBoxEvents();
    },
    aftersavefunc: function(rowId){ applyDirtyRule(rowId); },
    restoreAfterError: true
  });
}

function bindGridDomEvents(){
  // 검색 엔터
  $('input[name="searchCustCd"], input[name="searchCustNm"], input[name="searchSalesrepNm"]').on('keypress', function(e){
    if (e.which === 13) dataSearch();
  });
}

function bindYnBoxEvents(){
  // 발송여부 체크박스 위임
  $('#gridList').off('change','.ynbox').on('change','.ynbox',function(e){
    const rowId = $(this).data('row');
    const grid = $('#gridList');
    const colName = $(this).hasClass('yn-cust') ? 'CUST_SENDMAIL_YN' : 'SALESREP_SENDMAIL_YN';
    const next = this.checked ? 'Y' : 'N';
    grid.jqGrid('setCell', rowId, colName, next);
    applyDirtyRule(rowId);
    e.stopPropagation();
  });
}

// 조회
function getSearchData(){
  return {
    custCd : $('input[name="searchCustCd"]').val(),
    custNm : $('input[name="searchCustNm"]').val(),
    salesrepNm : $('input[name="searchSalesrepNm"]').val()
  };
}
function dataSearch(){
  $('#gridList').setGridParam({ postData: getSearchData() }).trigger('reloadGrid');
}

// =========================== 저장 ===========================
function dataSave(btn){
  const grid = $('#gridList');
  const selectedIds = grid.jqGrid('getGridParam','selarrrow') || [];

  if (selectedIds.length === 0){
    alert('수정된 내용이 없습니다.');
    return;
  }

  // 유효성: 선택된 행만 검사 (AC-5)
  for (let rowId of selectedIds){
    const rd = grid.getRowData(rowId);
    const custYn = (rd.CUST_SENDMAIL_YN||'').toUpperCase()==='Y';
    const salesYn = (rd.SALESREP_SENDMAIL_YN||'').toUpperCase()==='Y';
    const custMail = (rd.CUST_MAIN_EMAIL||'').trim();
    const salesMail = (rd.SALESREP_EMAIL||'').trim();

    // 발송여부가 Y인 이메일만 형식검사(AC-4)
    if (custYn && (!custMail || !isValidEmail(custMail))){
      alert('거래처 담당자 이메일 형식이 올바르지 않습니다. (거래처코드: '+rd.CUST_CD+')');
      return;
    }
    if (salesYn && (!salesMail || !isValidEmail(salesMail))){
      alert('영업 담당 이메일 형식이 올바르지 않습니다. (거래처코드: '+rd.CUST_CD+')');
      return;
    }
  }

  if (!confirm('저장 하시겠습니까?')) return;

  // 폼 데이터 구성: rowList[i].field 형태로 전송 → Spring 바인딩
  const $f = $('form[name="iForm"]');
  $f.empty();
  selectedIds.forEach(function(rowId, idx){
    const rd = grid.getRowData(rowId);
    function hid(n,v){ $f.append('<input type="hidden" name="rowList['+idx+'].'+n+'" value="'+(v||'')+'"/>'); }
    hid('custCd', rd.CUST_CD);
    hid('custMainEmail', rd.CUST_MAIN_EMAIL);
    hid('custSendmailYn', rd.CUST_SENDMAIL_YN);
    hid('salesrepEmail', rd.SALESREP_EMAIL);
    hid('salesrepSendmailYn', rd.SALESREP_SENDMAIL_YN);
    hid('comments', rd.COMMENTS);
  });

  $(btn).prop('disabled', true);
  $.ajax({
    url: '${url}/admin/system/updateOrderMailAlarmAjax.lime',
    type: 'POST',
    data: $f.serialize(),
    success: function(res){
      if (res && res.RES_CODE === '0000'){
        alert(res.RES_MSG||'저장되었습니다.');
        // 성공 후 초기화(AC-6)
        $('#gridList').jqGrid('resetSelection');
        $('#gridList').find('tr.jqgrow').removeClass('changed-row');
        dataSearch(); // 재조회 → loadComplete에서 새 스냅샷
      }else{
        alert((res && res.RES_MSG) || '저장 실패');
      }
    },
    error: function(){ alert('서버 오류가 발생했습니다.'); },
    complete: function(){ $(btn).prop('disabled', false); }
  });
}
</script>
```

> 포인트
>
> * **AC-1\~3**: `applyDirtyRule` 한 군데에서 배경색/멀티셀렉트 동기화, 실제 값이 원본과 다를 때만 더티 처리.
> * **AC-4\~5**: 저장 시 **선택행만** 검증/저장. 발송여부 ‘Y’인 이메일만 형식 검사.
> * **AC-6**: 저장 성공 시 재조회 + 스냅샷 갱신으로 초기 상태 복귀.

---

# 백엔드 (Spring MVC) ― **컨트롤러/DTO/서비스/매퍼 XML 전부**

## 1) DTO

```java
// MailInfoDto.java
package com.company.project.admin.system.dto;

public class MailInfoDto {
    private String custCd;
    private String custMainEmail;
    private String custSendmailYn;      // 'Y' or 'N'
    private String salesrepEmail;
    private String salesrepSendmailYn;  // 'Y' or 'N'
    private String comments;

    // getter/setter ...
}
```

폼 바인딩용 래퍼:

```java
// MailInfoBulkForm.java
package com.company.project.admin.system.dto;

import java.util.List;

public class MailInfoBulkForm {
    private List<MailInfoDto> rowList;
    public List<MailInfoDto> getRowList() { return rowList; }
    public void setRowList(List<MailInfoDto> rowList) { this.rowList = rowList; }
}
```

## 2) Controller

```java
// AdminSystemController.java
package com.company.project.admin.system;

import com.company.project.admin.system.dto.MailInfoBulkForm;
import com.company.project.admin.system.dto.MailInfoDto;
import com.company.project.admin.system.service.MailInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/system")
public class AdminSystemController {

    @Resource
    private MailInfoService mailInfoService;

    private static final Pattern EMAIL_RE =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @PostMapping(value="/updateOrderMailAlarmAjax.lime")
    @ResponseBody
    public Map<String,Object> updateOrderMailAlarmAjax(MailInfoBulkForm form) {
        Map<String,Object> res = new HashMap<>();
        try{
            List<MailInfoDto> rows = (form!=null) ? form.getRowList() : Collections.emptyList();
            if (rows==null || rows.isEmpty()){
                res.put("RES_CODE","1001");
                res.put("RES_MSG","저장할 데이터가 없습니다.");
                return res;
            }

            // 최종 서버 유효성(멀티셀렉트된 행만 넘어옴: 프론트 구성상)
            for (MailInfoDto d : rows){
                boolean custY = "Y".equalsIgnoreCase(d.getCustSendmailYn());
                boolean salesY = "Y".equalsIgnoreCase(d.getSalesrepSendmailYn());
                if (custY){
                    String v = safe(d.getCustMainEmail());
                    if (v.isEmpty() || !EMAIL_RE.matcher(v).matches()){
                        res.put("RES_CODE","2001");
                        res.put("RES_MSG","거래처 담당자 이메일 형식 오류: "+safe(d.getCustCd()));
                        return res;
                    }
                }
                if (salesY){
                    String v = safe(d.getSalesrepEmail());
                    if (v.isEmpty() || !EMAIL_RE.matcher(v).matches()){
                        res.put("RES_CODE","2002");
                        res.put("RES_MSG","영업 담당 이메일 형식 오류: "+safe(d.getCustCd()));
                        return res;
                    }
                }
            }

            int cnt = mailInfoService.upsertList(rows);

            res.put("RES_CODE","0000");
            res.put("RES_MSG","저장 완료 ("+cnt+"건)");
            return res;
        }catch(Exception ex){
            res.put("RES_CODE","9999");
            res.put("RES_MSG","오류가 발생했습니다.");
            return res;
        }
    }

    private String safe(String s){ return (s==null) ? "" : s.trim(); }
}
```

## 3) Service

```java
// MailInfoService.java
package com.company.project.admin.system.service;

import com.company.project.admin.system.dto.MailInfoDto;
import java.util.List;

public interface MailInfoService {
    int upsertList(List<MailInfoDto> list);
}
```

```java
// MailInfoServiceImpl.java
package com.company.project.admin.system.service.impl;

import com.company.project.admin.system.dto.MailInfoDto;
import com.company.project.admin.system.service.MailInfoService;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MailInfoServiceImpl implements MailInfoService {

    private static final String NS = "AdminSystem.MailInfoMapper.";

    @Resource
    private SqlSessionTemplate sst;

    @Override
    public int upsertList(List<MailInfoDto> list) {
        if (list==null || list.isEmpty()) return 0;
        return sst.insert(NS+"mergeList", list);
    }
}
```

## 4) MyBatis Mapper (XML)

```xml
<!-- MailInfoMapper.xml -->
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="AdminSystem.MailInfoMapper">

  <insert id="mergeList">
    <foreach collection="list" item="it" separator=";">
      /* O_CUSTOMER_MAILINFO UPSERT */
      MERGE dbo.O_CUSTOMER_MAILINFO AS T
      USING (
        SELECT
              #{it.custCd}               AS CUST_CD
            , #{it.custMainEmail}        AS CUST_MAIN_EMAIL
            , #{it.custSendmailYn}       AS CUST_SENDMAIL_YN
            , #{it.salesrepEmail}        AS SALESREP_EMAIL
            , #{it.salesrepSendmailYn}   AS SALESREP_SENDMAIL_YN
            , #{it.comments}             AS COMMENTS
            , SYSTEM_USER                AS INID
            , GETDATE()                  AS INDATE
            , SYSTEM_USER                AS MOID
            , GETDATE()                  AS MODATE
      ) AS S
      ON T.CUST_CD = S.CUST_CD

      WHEN MATCHED THEN
        UPDATE SET
              T.CUST_MAIN_EMAIL       = S.CUST_MAIN_EMAIL
            , T.CUST_SENDMAIL_YN      = S.CUST_SENDMAIL_YN
            , T.SALESREP_EMAIL        = S.SALESREP_EMAIL
            , T.SALESREP_SENDMAIL_YN  = S.SALESREP_SENDMAIL_YN
            , T.COMMENTS              = S.COMMENTS
            , T.MOID                  = S.MOID
            , T.MODATE                = S.MODATE

      WHEN NOT MATCHED THEN
        INSERT (
              CUST_CD
            , CUST_MAIN_EMAIL
            , CUST_SENDMAIL_YN
            , SALESREP_EMAIL
            , SALESREP_SENDMAIL_YN
            , COMMENTS
            , INID, INDATE, MOID, MODATE
        )
        VALUES (
              S.CUST_CD
            , S.CUST_MAIN_EMAIL
            , S.CUST_SENDMAIL_YN
            , S.SALESREP_EMAIL
            , S.SALESREP_SENDMAIL_YN
            , S.COMMENTS
            , S.INID, S.INDATE, S.MOID, S.MODATE
        );
    </foreach>
  </insert>

</mapper>
```

> **주의 (사용자 스타일 반영)**
>
> * 키워드 대문자, 컬럼/조건 세로정렬, 스키마 한정(dbo.), `MERGE` 파라미터 전부 바인딩, `GETDATE()`, `SYSTEM_USER` 사용.
> * 필요 시 `WITH (HOLDLOCK)` 등은 트랜잭션 정책에 맞춰 추가 가능.

---

# 수용 기준(AC) 체크

1. 체크/언체크 시 배경색과 멀티셀렉트 **동기화** → `applyDirtyRule`로 보장.
2. 이메일/영업이메일/비고는 **실제 값 변경 시에만** 상태 변화 → 원본 스냅샷 비교.
3. 원래 값으로 **되돌리면** 멀티셀렉트/배경색 자동 복구.
4. 저장 시 이메일 **형식 검증 정확**(프론트 경고 + 서버 최종검증).
5. **선택되지 않은 행은 검사/저장 제외**.
6. 저장 성공 후 **상태 리셋**(재조회 + 스냅샷 재생성).

---

# 추가 팁 (문제 재발 방지)

* jqGrid에서 **multiselect 오동작**의 대부분은 `beforeSelectRow` 미제어/이벤트 버블링 때문임. 이번처럼 **cb 칼럼에서만 선택 허용**으로 안정화.
* 더티 관리 로직은 **한 곳**(applyDirtyRule)에만 두고, 모든 이벤트에서 **그 함수만 호출**하게 단순화하면 사이드이펙트가 급감.
* 백엔드는 **선택 행만** 받도록 프론트에서 폼을 구성했기 때문에, 불필요한 서버 로직 분기 최소화.

---

필요하면 \*\*JSON 방식(@RequestBody)\*\*으로도 바로 바꿔줄 수 있어. 우선은 기존 폼 POST와 완벽 호환되는 형태로 구성했어.

---------------------------------------------------------------------
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
---------------------------------------------------------------------

\[Request to fix JQGrid malfunction].

## Symptom summary:

```
The following malfunctions occur on jqGrid-based screens.

1) The multiselect checkbox is not automatically selected when selecting whether to ship (checkbox).
2) The row background color does not change when selecting whether to ship (checkbox).
3) The multiselect checkbox is not unchecked when the checkbox is cleared.
4) Row background color does not return to its original color when unchecked.
5) Contact email / Sales email / Remarks columns would change background color when multiselect checkbox was checked **just by clicking**.
6) The multiselect checkbox/background color does not return to its original state after modifying the Contact Email / Sales Email / Remarks values and then reverting them back to their original values.
7) "Mail format error" warning pops up when clicking save button even though the email address is correct.
8) "Mail format error" warning pops up when clicking the Save button even though the multiselect checkbox is unchecked.
```

## Development Environment:

```
- OS: Windows
- IDE: STS4.5
- Java: Zulu-8 (zulu8.88.0.19)
- WAS: Tomcat 9.0.99
- DB: MSSQL 2019 (15.0.4415.2, RTM, Standard Edition 64-bit)
- Build: Maven
- ORM: MyBatis
- Grid: JQGrid
```

## Requests:

```
A. Please fix the multiselect checkbox and the row background color to synchronize when the sendability checkbox is checked/unchecked.
B. Please make the multiselect checkbox and background color change "only if the actual value changes" for emails/sales emails/remarks.
C. Make sure the multiselect checkbox and background color are restored when reverting to the original value.
D. Check the email format validation logic and fix it so that valid email addresses pass without error.
E. Exclude rows with unchecked multiselect checkboxes from email format validation on save.
F. Please provide the complete modification code needed on the frontend (JS/JQGrid).
G. Please ensure that the multiselect checkbox and background color are returned to their initial state after a successful save.
```

## Acceptance Criteria (AC):

```
1. background color is consistent with multiselect when checking/unchecking sendability
2. email/sales email/remarks columns only change state on "actual value change"
3. multiselect/background color is also restored when restoring original value
4. email format validation on save now works correctly (correct mails pass, only incorrect mails error)
5. multiselect unchecked rows are not subject to validation
6. state reset works correctly after successful save
```

## Current code :
```html
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>

<!DOCTYPE html>

<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

<script type="text/javascript" src="${url}/include/js/common/select2/select2.js"></script>

<link rel="stylesheet" href="${url}/include/js/common/select2/select2.css" />

<script type="text/javascript">
// 이메일 형식 유효성 검사 함수
function validateEmail(email) {
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
}

// 배경색 변경 함수
function changeRowBackground(rowId, isChanged) {
    if (isChanged) {
        $('#gridList #' + rowId).css('background-color', '#ffebcd'); // 연한 주황색
    } else {
        $('#gridList #' + rowId).css('background-color', ''); // 원래 색상
    }
}

// 체크박스 포맷터
function checkboxFormatter(cellVal, options, rowObj) {
    var checked = (cellVal === 'Y') ? 'checked' : '';
    return '<input type="checkbox" class="mail-checkbox" ' + checked + ' onclick="handleCheckboxClick(this, \'' + options.rowId + '\', \'' + options.colModel.name + '\')" />';
}

// 체크박스 클릭 이벤트 핸들러
function handleCheckboxClick(checkbox, rowId, fieldName) {
    var newValue = checkbox.checked ? 'Y' : 'N';
    
    // JQGrid의 setCell 메소드를 사용하여 값 변경 및 dirty 플래그 설정
    // 이렇게 해야 JQGrid가 이 셀이 변경되었음을 인지합니다.
    $('#gridList').jqGrid('setCell', rowId, fieldName, newValue, '', {dirty: true});
    
    // 즉시 배경색 변경
    changeRowBackground(rowId, true);
}

// 이메일 필드 유효성 검사
function validateEmailField(e) {
    var email = $(e.target).val();
    if (email && !validateEmail(email)) {
        alert('올바른 이메일 형식을 입력해주세요.');
        $(e.target).focus();
        return false;
    }
}

$(function(){
    getGridList();
});

// JQGrid 변경된 행을 가져오는 함수
function getModifiedRows() {
    const grid = $('#gridList');
    const ids = grid.getDataIDs();
    const modifiedRows = [];
    
    $.each(ids, function(i, rowId) {
        // JQGrid의 getRowData(rowId, true)를 사용하면 dirty 플래그가 있는 행만 가져올 수 있습니다.
        const rowData = grid.getRowData(rowId, true);
        if (rowData) {
            modifiedRows.push(rowData);
        }
    });
    
    return modifiedRows;
}

var lastSelection;
function editRow(id){
    if (id && id !== lastSelection) {
        var grid = $('#gridList');
        grid.jqGrid('editRow', id, {keys: true, focusField: true});
        lastSelection = id;
    }
}

function dataSave(obj) {
    $(obj).prop('disabled', true);
    
    // JQGrid의 getRowData(rowId, true)를 사용해 dirty 플래그가 있는 행만 가져옴
    var modifiedRows = getModifiedRows();

    if (modifiedRows.length === 0) {
        alert('수정된 내용이 없습니다.');
        $(obj).prop('disabled', false);
        return false;
    }
    
    // 유효성 검사
    var validationFailed = false;
    $.each(modifiedRows, function(i, rowData) {
        // 이메일 형식 검사
        if (rowData.CUST_MAIN_EMAIL && !validateEmail(rowData.CUST_MAIN_EMAIL)) {
            alert('거래처 담당자 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
        
        if (rowData.SALESREP_EMAIL && !validateEmail(rowData.SALESREP_EMAIL)) {
            alert('영업 담당 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
        
        // 발송 여부 체크 시 이메일 존재 확인
        if (rowData.CUST_SENDMAIL_YN === 'Y' && !rowData.CUST_MAIN_EMAIL) {
            alert('담당자 이메일 발송이 체크되어 있지만 담당자 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
        
        if (rowData.SALESREP_SENDMAIL_YN === 'Y' && !rowData.SALESREP_EMAIL) {
            alert('영업 담당 이메일 발송이 체크되어 있지만 영업 담당 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
    });
    
    if (validationFailed) {
        $(obj).prop('disabled', false);
        return false;
    }
    
    // 데이터 준비
    var iFormObj = $('form[name="iForm"]');
    iFormObj.empty();
    
    $.each(modifiedRows, function(i, rowData) {
        iFormObj.append('<input type="hidden" name="custCd" value="' + rowData.CUST_CD + '" />');
        iFormObj.append('<input type="hidden" name="custMainEmail" value="' + (rowData.CUST_MAIN_EMAIL || '') + '" />');
        iFormObj.append('<input type="hidden" name="custSendmailYn" value="' + (rowData.CUST_SENDMAIL_YN || 'N') + '" />');
        iFormObj.append('<input type="hidden" name="salesrepEmail" value="' + (rowData.SALESREP_EMAIL || '') + '" />');
        iFormObj.append('<input type="hidden" name="salesrepSendmailYn" value="' + (rowData.SALESREP_SENDMAIL_YN || 'N') + '" />');
        iFormObj.append('<input type="hidden" name="comments" value="' + (rowData.COMMENTS || '') + '" />');
    });
    
    if (confirm('저장 하시겠습니까?')) {
        var iFormData = iFormObj.serialize();
        var url = '${url}/admin/system/updateOrderMailAlarmAjax.lime'; 
        $.ajax({
            async : false,
            data : iFormData,
            type : 'POST',
            url : url,
            success : function(data) {
                if (data.RES_CODE == '0000') {
                    alert(data.RES_MSG);
                    
                    // 성공적으로 저장된 후 배경색 초기화 및 그리드 리로드
                    $.each(modifiedRows, function(i, rowData) {
                        changeRowBackground(rowData.CUST_CD, false);
                    });
                    
                    dataSearch(); // 그리드 리로드로 최신 데이터 반영
                }else{
                    alert(data.RES_MSG);
                }
                $(obj).prop('disabled', false);
            },
            error : function(request,status,error){
                alert('Error');
                $(obj).prop('disabled', false);
            }
        });
    }else{
        $(obj).prop('disabled', false);
    }
}

// ==================================================================================
// jqGrid Columns Order 설정
// ==================================================================================
//Start. Setting Jqgrid Columns Order.
var ckNameJqGrid = 'admin/customer/customerList/jqGridCookie'; // 페이지별 쿠키명 설정
ckNameJqGrid += '/gridList'; // 그리드명별 쿠키명 설정

var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
var globalColumnOrder = globalColumnOrderStr.split(',');

var defaultColModel = [
    {name:"CUST_CD", key:true, label:'거래처코드', width:120, align:'center', sortable:true},
    {name:"CUST_NM", label:'거래처명', width:220, align:'left', sortable:true},
    {name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true, editable:true, editoptions:{dataEvents:[{type:'blur', fn:validateEmailField}]}},
    {name:"CUST_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true, formatter:checkboxFormatter, editable:true, edittype:'checkbox'},
    {name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true},
    {name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:300, align:'center', sortable:true, editable:true, editoptions:{dataEvents:[{type:'blur', fn:validateEmailField}]}},
    {name:"SALESREP_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true, formatter:checkboxFormatter, editable:true, edittype:'checkbox'},
    {name:"COMMENTS", label:'비고', width:450, align:'left', sortable:true, editable:true}
];

var defaultColumnOrder = writeIndexToStr(defaultColModel.length);
var updateComModel = [];

// 쿠키에서 컬럼 순서 복원
if (0 < globalColumnOrder.length) { // 쿠키값이 있을때
    if (defaultColModel.length == globalColumnOrder.length) {
        for (var i = 0, j = globalColumnOrder.length; i < j; i++) {
            updateComModel.push(defaultColModel[globalColumnOrder[i]]);
        }
        setCookie(ckNameJqGrid, globalColumnOrder, 365);
    } else {
        updateComModel = defaultColModel;
        setCookie(ckNameJqGrid, defaultColumnOrder, 365);
    }
} else { // 쿠키값이 없을때
    updateComModel = defaultColModel;
    setCookie(ckNameJqGrid, defaultColumnOrder, 365);
}

// ==================================================================================
// jqGrid Column Width 설정
// ==================================================================================
var ckNameJqGridWidth = ckNameJqGrid + '/width'; // 페이지별 쿠키명 설정
var globalColumnWidthStr = toStr(decodeURIComponent(getCookie(ckNameJqGridWidth)));
var globalColumnWidth = globalColumnWidthStr.split(',');
var defaultColumnWidthStr = '';
var defaultColumnWidth;
var updateColumnWidth;

if ('' != globalColumnWidthStr) { // 쿠키값이 있을때
    if (updateComModel.length == globalColumnWidth.length) {
        updateColumnWidth = globalColumnWidth;
    } else {
        for (var j = 0; j < updateComModel.length; j++) {
            if ('rn' != updateComModel[j].name && 'cb' != updateComModel[j].name) {
                var v = ('' != toStr(updateComModel[j].width)) ? toStr(updateComModel[j].width) : '0';
                if ('' == defaultColumnWidthStr) {
                    defaultColumnWidthStr = v;
                } else {
                    defaultColumnWidthStr += ',' + v;
                }
            }
        }
        defaultColumnWidth = defaultColumnWidthStr.split(',');
        updateColumnWidth = defaultColumnWidth;
        setCookie(ckNameJqGridWidth, defaultColumnWidth, 365);
    }
} else { // 쿠키값이 없을때
    for (var j = 0; j < updateComModel.length; j++) {
        if ('rn' != updateComModel[j].name && 'cb' != updateComModel[j].name) {
            var v = ('' != toStr(updateComModel[j].width)) ? toStr(updateComModel[j].width) : '0';
            if ('' == defaultColumnWidthStr) {
                defaultColumnWidthStr = v;
            } else {
                defaultColumnWidthStr += ',' + v;
            }
        }
    }
    defaultColumnWidth = defaultColumnWidthStr.split(',');
    updateColumnWidth = defaultColumnWidth;
    setCookie(ckNameJqGridWidth, defaultColumnWidth, 365);
}

// 컬럼 너비 적용
if (updateComModel.length == globalColumnWidth.length) {
    for (var j = 0; j < updateComModel.length; j++) {
        updateComModel[j].width = toStr(updateColumnWidth[j]);
    }
}


function getGridList(){
    var searchData = getSearchData();
    $('#gridList').jqGrid({
        url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
        editurl: 'clientArray',
        datatype: "json",
        mtype: 'POST',
        postData: searchData,
        colModel: updateComModel,
        height: '360px',
        autowidth: false,
        multiselect: true,
        rowNum: 10,              // 기본 페이지당 출력 행 수
        rowList: ['10', '30', '50', '100'], // 페이지당 행 수 옵션
        rownumbers: true,         // 행 번호 표시
        pagination: true,
        pager: "#pager",
        actions : true,
        pginput : true,
        // JQGrid가 행을 식별하는 고유 키를 CUST_CD로 설정
        // 이 설정이 매우 중요합니다.
        jsonReader: {
            root: 'list',
            id: 'CUST_CD'
        },
        
        // 열 순서 변경 이벤트
        sortable: {
            update: function(relativeColumnOrder) {
                var grid = $('#gridList');
                var defaultColIndicies = [];
                for (var i = 0; i < defaultColModel.length; i++) {
                    defaultColIndicies.push(defaultColModel[i].name);
                }
                globalColumnOrder = [];
                var columnOrder = [];
                var currentColModel = grid.getGridParam('colModel');
                for (var j = 0; j < relativeColumnOrder.length; j++) {
                    if ('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name) {
                        columnOrder.push(defaultColIndicies.indexOf(currentColModel[j].name));
                    }
                }
                globalColumnOrder = columnOrder;
                setCookie(ckNameJqGrid, globalColumnOrder, 365);
                var tempUpdateColumnWidth = [];
                for (var j = 0; j < currentColModel.length; j++) {
                    if ('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name) {
                        tempUpdateColumnWidth.push(currentColModel[j].width);
                    }
                }
                updateColumnWidth = tempUpdateColumnWidth;
                setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
            }
        },

        // 열 크기 조정 후 실행되는 이벤트
        resizeStop: function(width, index) {
            console.log('globalColumnOrder : ', globalColumnOrder);
            var minusIdx = 0;
            var grid = $('#gridList');
            var currentColModel = grid.getGridParam('colModel');
            if ('rn' == currentColModel[0].name || 'cb' == currentColModel[0].name) minusIdx--;
            if ('rn' == currentColModel[1].name || 'cb' == currentColModel[1].name) minusIdx--;
            var resizeIdx = index + minusIdx;
            updateColumnWidth[resizeIdx] = width;
            setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
        },

        sortorder: 'desc',
        
        // 인라인 편집 종료 시 이벤트
        afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
            // 변경된 셀이 있는 행에 배경색 적용
            changeRowBackground(rowid, true);
        },

        onSelectRow: function(rowId){
            // 행 선택 시 인라인 편집 모드 진입
            editRow(rowId);
        }
    });
}

function getSearchData(){
    var searchData = {
        custCd : $('input[name="searchCustCd"]').val(),
        custNm : $('input[name="searchCustNm"]').val(),
        salesrepNm : $('input[name="searchSalesrepNm"]').val()
    };
    return searchData;
}

// 조회
function dataSearch() {
    var searchData = getSearchData();
    $('#gridList').setGridParam({
        postData : searchData
    }).trigger("reloadGrid");
}

// 엑셀다운로드 (기존 코드와 동일)
function excelDown(obj){
    $('#ajax_indicator').show().fadeIn('fast');
    var token = getFileToken('excel');
    $('form[name="frm"]').append('<input type="hidden" name="filetoken" value="'+token+'" />');
    
    formPostSubmit('frm', '${url}/admin/system/orderMailAlarmExcelDown.lime');
    $('form[name="frm"]').attr('action', '');
    
    $('input[name="filetoken"]').remove();
    var fileTimer = setInterval(function() {
        if('true' == getCookie(token)){
            $('#ajax_indicator').fadeOut();
            delCookie(token);
            clearInterval(fileTimer);
        }
    }, 1000 );
}
</script>

</head>

<body class="page-header-fixed compact-menu">
    <div id="ajax_indicator" style="display:none;">
        <p style="position: absolute; top: 50%; left: 50%; margin: -110px 0 0 -110px;">
            <img src="${url}/include/images/common/loadingbar.gif" />
        </p>
    </div>

```
<main class="page-content content-wrap">

    <%@ include file="/WEB-INF/views/include/admin/header.jsp" %>
    <%@ include file="/WEB-INF/views/include/admin/left.jsp" %>
    
    <%-- 임의 form --%>
    <form name="iForm" method="post"></form>
    
    <form name="frm" method="post">
    
    <div class="page-inner">
        <div class="page-title">
            <h3>
                주문메일알람 관리
                <div class="page-right">
                    <button type="button" class="btn btn-line f-black" title="검색" onclick="dataSearch();"><i class="fa fa-search"></i><em>검색</em></button>
                    <button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();"><i class="fa fa-refresh"></i><em>새로고침</em></button>
                    <button type="button" class="btn btn-line f-black" title="엑셀다운로드" onclick="excelDown(this);"><i class="fa fa-file-excel-o"></i><em>엑셀다운로드</em></button>
                </div>
            </h3>
        </div>
        
        <div id="main-wrapper">
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
                                                <input type="text" class="search-input" name="searchCustCd" value="${param.searchCustCd}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                            </div>
                                        </li>
                                        <li>
                                            <label class="search-h">거래처명</label>
                                            <div class="search-c">
                                                <input type="text" class="search-input" name="searchCustNm" value="${param.searchCustNm}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                            </div>
                                        </li>
                                        <li>
                                            <label class="search-h">영업담당</label>
                                            <div class="search-c">
                                                <input type="text" class="search-input" name="searchSalesrepNm" value="${param.searchSalesrepNm}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
                                            </div>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                        
                        <div class="panel-body">
                            <h5 class="table-title listT">TOTAL <span id="listTotalCountSpanId">0</span>EA</h5>
                            <div class="btnList writeObjectClass">
                                <button type="button" class="btn btn-info" onclick="dataSave(this);">저장</button>
                            </div>
                            <div class="table-responsive in">
                                <table id="gridList" class="display table tableList nowrap" width="100%" border="0" cellpadding="0" cellspacing="0"></table>
                                <div id="pager"></div>
                            </div>
                        </div>
                        
                    </div>
                </div>
            </div>
            </div>
        <%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>
        
    </div>
    
    </form>
</main>
```

</body>

</html>
```

---------------------------------------------------------------------
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
/* ******************************************************************************************************************************* */
---------------------------------------------------------------------

오케이. “실제 값 변화만 더티 처리하고(행 배경+멀티셀렉트 동기화), 저장/검증은 선택된 행만”으로 전면 개편한 **프론트엔드 전용(JS/JQGrid) 완전치환 코드**를 줄게. JSP의 `<script>…</script>` 블록만 아래 코드로 통째로 교체하면 돼. (HTML 틀/폼/버튼/URL은 그대로 사용)

---

# 적용 가이드(요약)

* **원본 스냅샷(`origMap`)**: `loadComplete` 때 행별 원본값 저장 → 이후 변화 여부를 항상 “원본 vs 현재값” 비교로 판단.
* **더티 처리 원칙**

  * 원본과 **다를 때만**: `changed-row` 클래스 추가 + jqGrid **multiselect 체크**.
  * 원본과 **같아지면**: `changed-row` 제거 + multiselect **해제**.
* **체크박스(발송 여부)**: 클릭 시 ‘Y/N’ 토글 → 즉시 `applyDirtyRule(rowId)` 호출(배경/선택 동기).
* **이메일/비고**: blur/keyup 시 값 변화 감지 → `applyDirtyRule(rowId)`.
* **멀티셀렉트 오동작 방지**: `beforeSelectRow`에서 **cb 칼럼 클릭일 때만** 행 선택 허용.
* **저장/검증**: **선택된 행만** 검증/전송(AC-5). 발송여부가 ‘Y’인 칼럼만 메일 형식 검사(AC-4).
* **저장 성공 후 초기화**: 재조회 → `loadComplete`에서 스냅샷 재생성(AC-6).

---

# CSS (페이지 `<head>` 안 어디든 추가 가능)

```html
<style>
  .changed-row { background-color:#ffebcd !important; } /* 연한 주황 */
  .invalid-input { outline:2px solid #e57373 !important; background:#fdecea !important; }
</style>
```

---

# JS/JQGrid **완전 치환 코드**

```html
<script type="text/javascript">
// =========================== 이메일 유틸 ===========================
const EMAIL_RE = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
function isValidEmail(v){ return EMAIL_RE.test((v||'').trim()); }

// =========================== 행 비주얼/선택 동기 ===========================
function setRowChangedVisual(rowId, changed){
  const $tr = $('#gridList').find('#'+rowId);
  if (changed) $tr.addClass('changed-row'); else $tr.removeClass('changed-row');
}
function setRowSelected(rowId, selected){
  const grid = $('#gridList');
  const sel = grid.jqGrid('getGridParam','selarrrow') || [];
  const has = sel.indexOf(String(rowId)) >= 0;
  if (selected && !has) grid.jqGrid('setSelection', rowId, false);
  if (!selected && has)  grid.jqGrid('resetSelection', rowId);
}

// =========================== 더티 추적(스냅샷) ===========================
var origMap = {}; // { rowId: {CUST_MAIN_EMAIL:'', CUST_SENDMAIL_YN:'N', ...} }
var TRACK_COLS = [
  'CUST_MAIN_EMAIL','CUST_SENDMAIL_YN',
  'SALESREP_EMAIL','SALESREP_SENDMAIL_YN',
  'COMMENTS'
];

function takeSnapshot(){
  const grid = $('#gridList');
  origMap = {};
  grid.getDataIDs().forEach(function(id){
    const rd = grid.getRowData(id);
    origMap[id] = {};
    TRACK_COLS.forEach(function(c){ origMap[id][c] = (rd[c]||'').trim(); });
  });
}

function rowIsDirty(rowId){
  const grid = $('#gridList'), cur = grid.getRowData(rowId), base = origMap[rowId] || {};
  for (let c of TRACK_COLS){
    if ((cur[c]||'').trim() !== (base[c]||'').trim()) return true;
  }
  return false;
}

function applyDirtyRule(rowId){
  const dirty = rowIsDirty(rowId);
  setRowChangedVisual(rowId, dirty);
  setRowSelected(rowId, dirty);
}

// =========================== 편집 & 토글 ===========================
function editRow(rowId){
  const grid = $('#gridList');
  grid.jqGrid('editRow', rowId, {
    keys: true,
    aftersavefunc: function(){ applyDirtyRule(rowId); },
    restoreAfterError: true
  });
}
function toggleYN(grid, rowId, colName){
  const cur = (grid.getCell(rowId, colName)+'').trim().toUpperCase();
  const next = (cur === 'Y') ? 'N' : 'Y';
  grid.jqGrid('setCell', rowId, colName, next);
  applyDirtyRule(rowId);
}

// =========================== 컬럼 모델 ===========================
var updateComModel = [
  {name:"CUST_CD", key:true, label:'거래처코드', width:120, align:'center', sortable:true},
  {name:"CUST_NM", label:'거래처명', width:220, align:'left', sortable:true},

  {name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true, editable:true,
    edittype:'text',
    editoptions:{ size:30, maxlength:200,
      dataEvents:[
        { type:'blur',  fn:function(){ 
            const v = $(this).val();
            $(this).toggleClass('invalid-input', !!v && !isValidEmail(v));
            const rowId = $(this).closest('tr.jqgrow').attr('id'); applyDirtyRule(rowId);
        }},
        { type:'keyup', fn:function(){ 
            const rowId = $(this).closest('tr.jqgrow').attr('id'); applyDirtyRule(rowId);
        }}
      ]
    }
  },

  {name:"CUST_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true,
    formatter:function(cell, opt){
      const checked = (String(cell).toUpperCase()==='Y') ? 'checked' : '';
      return '<input type="checkbox" class="ynbox yn-cust" '+checked+' data-row="'+opt.rowId+'"/>';
    }
  },

  {name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true},

  {name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:300, align:'center', sortable:true, editable:true,
    edittype:'text',
    editoptions:{ size:40, maxlength:200,
      dataEvents:[
        { type:'blur',  fn:function(){
            const v = $(this).val();
            $(this).toggleClass('invalid-input', !!v && !isValidEmail(v));
            const rowId = $(this).closest('tr.jqgrow').attr('id'); applyDirtyRule(rowId);
        }},
        { type:'keyup', fn:function(){
            const rowId = $(this).closest('tr.jqgrow').attr('id'); applyDirtyRule(rowId);
        }}
      ]
    }
  },

  {name:"SALESREP_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true,
    formatter:function(cell, opt){
      const checked = (String(cell).toUpperCase()==='Y') ? 'checked' : '';
      return '<input type="checkbox" class="ynbox yn-sales" '+checked+' data-row="'+opt.rowId+'"/>';
    }
  },

  {name:"COMMENTS", label:'비고', width:450, align:'left', sortable:true, editable:true,
    edittype:'text',
    editoptions:{ maxlength:4000,
      dataEvents:[
        { type:'blur',  fn:function(){ const rowId = $(this).closest('tr.jqgrow').attr('id'); applyDirtyRule(rowId); }},
        { type:'keyup', fn:function(){ const rowId = $(this).closest('tr.jqgrow').attr('id'); applyDirtyRule(rowId); }}
      ]
    }
  }
];

// =========================== 그리드 빌드 ===========================
$(function(){ buildGrid(); });

function buildGrid(){
  $('#gridList').jqGrid({
    url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
    datatype: "json",
    mtype: 'POST',
    editurl: 'clientArray',
    postData: getSearchData(),
    colModel: updateComModel,
    height: 360,
    autowidth: false,
    multiselect: true,
    rowNum: 10,
    rowList: [10,30,50,100],
    rownumbers: true,
    pager: "#pager",
    jsonReader: { root:'list', id:'CUST_CD' },
    sortorder: 'desc',

    // 셀 클릭 동작: 에디터/체크박스/멀티셀렉트 충돌 방지
    onCellSelect: function(rowId, iCol){
      const grid = $(this);
      const cm = grid.jqGrid('getGridParam','colModel');
      const colName = cm[iCol] && cm[iCol].name;
      if (['CUST_MAIN_EMAIL','SALESREP_EMAIL','COMMENTS'].includes(colName)) editRow(rowId);
      if (colName === 'CUST_SENDMAIL_YN' || colName === 'SALESREP_SENDMAIL_YN') toggleYN(grid, rowId, colName);
    },

    // 멀티셀렉트는 cb 칼럼에서만
    beforeSelectRow: function(rowId, e){
      const iCol = $.jgrid.getCellIndex($(e.target).closest('td')[0]);
      const cm   = $(this).jqGrid('getGridParam','colModel');
      const colName = cm[iCol] && cm[iCol].name;
      return (colName === 'cb');
    },

    loadComplete: function(){
      takeSnapshot(); // 초기 스냅샷
      // 초기 비주얼/선택 초기화
      const grid = $('#gridList'); grid.jqGrid('resetSelection');
      grid.getDataIDs().forEach(function(id){ setRowChangedVisual(id, false); });

      // 동적 체크박스 이벤트 위임(발송 여부)
      $('#gridList').off('change','.ynbox').on('change','.ynbox',function(e){
        const rowId = $(this).data('row');
        const grid = $('#gridList');
        const colName = $(this).hasClass('yn-cust') ? 'CUST_SENDMAIL_YN' : 'SALESREP_SENDMAIL_YN';
        grid.jqGrid('setCell', rowId, colName, this.checked ? 'Y' : 'N');
        applyDirtyRule(rowId);
        e.stopPropagation();
      });
    }
  });
}

// =========================== 조회 ===========================
function getSearchData(){
  return {
    custCd    : $('input[name="searchCustCd"]').val(),
    custNm    : $('input[name="searchCustNm"]').val(),
    salesrepNm: $('input[name="searchSalesrepNm"]').val()
  };
}
function dataSearch(){
  $('#gridList').setGridParam({ postData:getSearchData() }).trigger('reloadGrid');
}

// =========================== 저장(선택행만) ===========================
function dataSave(btn){
  const grid = $('#gridList');
  const selected = grid.jqGrid('getGridParam','selarrrow') || [];
  if (selected.length === 0){ alert('수정된 내용이 없습니다.'); return; }

  // 검증: 선택된 행만, 발송여부 Y인 이메일만 형식 검사
  for (let rowId of selected){
    const rd = grid.getRowData(rowId);
    const custY  = (rd.CUST_SENDMAIL_YN||'').toUpperCase()==='Y';
    const salesY = (rd.SALESREP_SENDMAIL_YN||'').toUpperCase()==='Y';
    const cmail  = (rd.CUST_MAIN_EMAIL||'').trim();
    const smail  = (rd.SALESREP_EMAIL||'').trim();

    if (custY && (!cmail || !isValidEmail(cmail))){
      alert('거래처 담당자 이메일 형식이 올바르지 않습니다. (거래처코드: '+rd.CUST_CD+')');
      return;
    }
    if (salesY && (!smail || !isValidEmail(smail))){
      alert('영업 담당 이메일 형식이 올바르지 않습니다. (거래처코드: '+rd.CUST_CD+')');
      return;
    }
  }

  if (!confirm('저장 하시겠습니까?')) return;

  // 폼 구성: 선택된 행만 전송
  const $f = $('form[name="iForm"]'); $f.empty();
  selected.forEach(function(rowId, idx){
    const rd = grid.getRowData(rowId);
    function hid(n,v){ $f.append('<input type="hidden" name="rowList['+idx+'].'+n+'" value="'+(v||'')+'"/>'); }
    hid('custCd', rd.CUST_CD);
    hid('custMainEmail', rd.CUST_MAIN_EMAIL);
    hid('custSendmailYn', rd.CUST_SENDMAIL_YN);
    hid('salesrepEmail', rd.SALESREP_EMAIL);
    hid('salesrepSendmailYn', rd.SALESREP_SENDMAIL_YN);
    hid('comments', rd.COMMENTS);
  });

  $(btn).prop('disabled', true);
  $.ajax({
    url: '${url}/admin/system/updateOrderMailAlarmAjax.lime',
    type: 'POST',
    data: $f.serialize(),
    success: function(res){
      if (res && res.RES_CODE === '0000'){
        alert(res.RES_MSG || '저장되었습니다.');
        // 초기화: 재조회 → loadComplete에서 스냅샷/비주얼 초기화
        dataSearch();
      }else{
        alert((res && res.RES_MSG) || '저장 실패');
      }
    },
    error: function(){ alert('서버 오류가 발생했습니다.'); },
    complete: function(){ $(btn).prop('disabled', false); }
  });
}
</script>
```

---

## 왜 이게 AC를 충족하는가?

* **AC-1**: 발송여부 클릭 시 `toggleYN` → `applyDirtyRule`가 **행 배경색+멀티셀렉트**를 항상 동시에 반영.
* **AC-2**: 이메일/비고는 **원본 스냅샷 비교**로 “실제 값 변경”일 때만 더티 표시/선택.
* **AC-3**: 값을 원래대로 돌리면 `rowIsDirty=false` → 배경/선택 자동 복원.
* **AC-4**: 저장 시 **발송여부 Y인 칼럼만** 이메일 형식 검사. 정규식 강화.
* **AC-5**: **선택된 행(selected)** **만** 검증/전송.
* **AC-6**: 저장 성공 후 `dataSearch()`로 재조회 → `loadComplete`에서 스냅샷 재생성 + 전체 초기화.

---

## 빠른 테스트 체크리스트

1. 발송여부(Y/N) 토글 → 행 배경/멀티셀렉트 즉시 동기화.
2. 이메일 수정 → 값이 달라지면만 배경/선택, 원복하면 해제.
3. 선택 안 된 행은 저장 시 전송/검증 제외.
4. 발송여부=Y인 행만 이메일 형식 검사.
5. 저장 성공 후 모든 행 `changed-row` 제거 + 선택 해제 + 재조회.

필요하면 이 로직을 **JSON 전송(@RequestBody)** 방식으로 바꾸는 버전도 바로 만들어줄게.
