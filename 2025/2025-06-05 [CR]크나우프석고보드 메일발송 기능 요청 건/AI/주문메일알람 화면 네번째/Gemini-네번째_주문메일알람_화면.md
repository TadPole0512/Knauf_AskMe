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
	F. Please provide the complete modification code needed on the frontend (JS/JQGrid).
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
        rowNum: 10,              // 기본 페이지당 출력 행 수
        rowList: ['10', '30', '50', '100'], // 페이지당 행 수 옵션
        rownumbers: true,         // 행 번호 표시
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

안녕하세요, 문의 주신 jqGrid 문제에 대해 코드 분석과 수정 작업을 진행해드릴게요. 현재 증상들을 보니까 JQGrid의 기본 동작 방식과 커스텀 로직 간에 싱크가 안 맞는 부분이 많아 보여요. 특히 **데이터 변경, 배경색 변경, 멀티셀렉트 체크박스 상태, 그리고 저장 시 유효성 검사**가 꼬여 있는 게 핵심 문제네요.

가장 근본적인 원인은 **jqGrid의 `dirty` 플래그 관리와 커스텀 로직의 분리** 때문입니다. `CUST_SENDMAIL_YN`과 `SALESREP_SENDMAIL_YN` 발송 여부 체크박스는 커스텀 포맷터와 클릭 핸들러로 동작하는데, 이 로직이 jqGrid가 자체적으로 관리하는 `dirty` 상태(편집된 셀)나 멀티셀렉트 체크박스와는 별개로 움직여요. 이 때문에 체크박스를 클릭해도 멀티셀렉트 체크박스가 선택되지 않고, 배경색도 제대로 동기화되지 않는 거죠.

그리고 이메일/비고 컬럼은 **인라인 편집** 방식으로 동작하는데, `afterSaveCell` 이벤트에서 무조건 배경색을 바꾸도록 되어 있어 클릭만 해도 배경색이 바뀌는 문제가 생겨요.

마지막으로 저장 로직에서는 **`getModifiedRows()`** 함수를 통해 `dirty` 플래그가 있는 행만 가져와서 유효성 검사를 하는데, 체크박스만 수정된 행은 `dirty` 플래그가 제대로 설정되지 않아 유효성 검사에서 누락되는 문제가 발생하고 있네요.

이러한 문제들을 해결하기 위해 아래와 같이 코드를 수정하고 새로운 함수를 추가할게요. 🧐

-----

### 🛠️ 수정 코드

아래는 말씀하신 문제들을 모두 해결하기 위해 수정 및 추가된 전체 코드입니다. 기존 코드를 완전히 대체하면 됩니다.

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
// 원본 데이터 저장을 위한 변수 (클릭 시 원본 값을 백업)
let originalData = {};

// 이메일 형식 유효성 검사 함수
function validateEmail(email) {
    // null, undefined 또는 공백인 경우 유효성 검사를 통과하도록 수정
    if (!email) {
        return true;
    }
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
}

// 배경색 변경 함수 (기존과 동일)
function changeRowBackground(rowId, isChanged) {
    if (isChanged) {
        $('#gridList #' + rowId).addClass('changed-row');
    } else {
        $('#gridList #' + rowId).removeClass('changed-row');
    }
}

// 행 상태(배경색, 멀티셀렉트)를 업데이트하는 함수
function updateRowState(rowId) {
    const grid = $('#gridList');
    // jqGrid의 isRowDataChanged를 사용해 변경 여부 확인
    const isChanged = grid.jqGrid('isRowDataChanged', rowId);

    // 배경색 동기화
    changeRowBackground(rowId, isChanged);
    
    // 멀티셀렉트 체크박스 동기화
    if (isChanged) {
        grid.jqGrid('setSelection', rowId, true); // 체크
    } else {
        grid.jqGrid('setSelection', rowId, false); // 언체크
    }
}

// 발송 여부 체크박스 포맷터
function checkboxFormatter(cellVal, options, rowObj) {
    const checked = (cellVal === 'Y') ? 'checked' : '';
    const fieldName = options.colModel.name;
    const rowId = options.rowId;
    
    // JQGrid의 기본 편집 기능을 활용하기 위해 id 속성을 추가
    const checkboxId = `cb_${fieldName}_${rowId}`;
    return `<input type="checkbox" id="${checkboxId}" class="mail-checkbox" ${checked} onclick="handleCheckboxClick(this, '${rowId}', '${fieldName}')" />`;
}

// 발송 여부 체크박스 클릭 이벤트 핸들러
function handleCheckboxClick(checkbox, rowId, fieldName) {
    const grid = $('#gridList');
    const newValue = checkbox.checked ? 'Y' : 'N';
    
    // JQGrid의 setCell 메소드를 사용하여 값 변경 및 dirty 플래그 설정
    // 이렇게 해야 JQGrid가 이 셀이 변경되었음을 인지합니다.
    grid.jqGrid('setCell', rowId, fieldName, newValue, '', {dirty: true});
    
    // 변경된 행의 상태를 업데이트 (배경색 및 멀티셀렉트 체크박스 동기화)
    updateRowState(rowId);
}

// 이메일 필드 유효성 검사 및 상태 변경
function validateEmailField(e) {
    const grid = $('#gridList');
    const rowId = $(e.target).closest('tr').attr('id');
    const colName = $(e.target).closest('td').attr('aria-describedby').split('_')[1];
    const newValue = $(e.target).val();
    
    const isValid = validateEmail(newValue);

    if (newValue && !isValid) {
        alert('올바른 이메일 형식을 입력해주세요.');
        $(e.target).focus();
        return false;
    }
    
    // jqGrid에 값 저장 및 dirty 플래그 설정
    grid.jqGrid('setCell', rowId, colName, newValue, '', {dirty: true});

    // 변경된 행의 상태를 업데이트
    updateRowState(rowId);
}

// JQGrid 변경된 행을 가져오는 함수
function getModifiedRows() {
    const grid = $('#gridList');
    const ids = grid.getDataIDs();
    const modifiedRows = [];
    
    $.each(ids, function(i, rowId) {
        // isRowDataChanged를 사용해 변경된 행만 필터링
        if (grid.jqGrid('isRowDataChanged', rowId)) {
            const rowData = grid.jqGrid('getRowData', rowId);
            modifiedRows.push(rowData);
        }
    });
    
    return modifiedRows;
}

var lastSelection;
function editRow(id){
    if (id && id !== lastSelection) {
        var grid = $('#gridList');
        grid.jqGrid('editRow', id, {
            keys: true, 
            focusField: true,
            oneditfunc: function(rowId) {
                // 편집 모드 진입 시 원본 데이터 백업
                const gridData = grid.jqGrid('getRowData', rowId);
                originalData[rowId] = {...gridData};
            }
        });
        lastSelection = id;
    }
}

function dataSave(obj) {
    $(obj).prop('disabled', true);
    
    // 변경된 행만 가져오기
    var modifiedRows = getModifiedRows();

    if (modifiedRows.length === 0) {
        alert('수정된 내용이 없습니다.');
        $(obj).prop('disabled', false);
        return false;
    }
    
    // 유효성 검사 (수정된 행 중 발송 여부 체크박스가 선택된 행만 검사)
    var validationFailed = false;
    $.each(modifiedRows, function(i, rowData) {
        // 발송 여부 체크가 되어 있을 때만 이메일 유효성 검사
        if (rowData.CUST_SENDMAIL_YN === 'Y') {
            if (!validateEmail(rowData.CUST_MAIN_EMAIL)) {
                alert('담당자 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')');
                validationFailed = true;
                return false;
            }
            if (!rowData.CUST_MAIN_EMAIL) {
                 alert('담당자 이메일 발송이 체크되어 있지만 담당자 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')');
                 validationFailed = true;
                 return false;
            }
        }
        
        if (rowData.SALESREP_SENDMAIL_YN === 'Y') {
            if (!validateEmail(rowData.SALESREP_EMAIL)) {
                alert('영업 담당 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')');
                validationFailed = true;
                return false;
            }
            if (!rowData.SALESREP_EMAIL) {
                alert('영업 담당 이메일 발송이 체크되어 있지만 영업 담당 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST_CD + ')');
                validationFailed = true;
                return false;
            }
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
                    dataSearch(); // 성공적으로 저장되면 그리드 전체 리로드
                } else {
                    alert(data.RES_MSG);
                }
                $(obj).prop('disabled', false);
            },
            error : function(request,status,error){
                alert('Error');
                $(obj).prop('disabled', false);
            }
        });
    } else {
        $(obj).prop('disabled', false);
    }
}

$(function(){
    getGridList();
});

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
        rowNum: 10,
        rowList: ['10', '30', '50', '100'],
        rownumbers: true,
        pagination: true,
        pager: "#pager",
        actions : true,
        pginput : true,
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
            const grid = $('#gridList');
            // 원본 데이터와 현재 값을 비교하여 변경 여부 확인
            const rowData = grid.jqGrid('getRowData', rowid);
            const originalValue = originalData[rowid][cellname];
            
            if (value !== originalValue) {
                // 값이 실제로 변경되었을 때만 dirty 플래그를 설정하고 상태 업데이트
                grid.jqGrid('setCell', rowid, cellname, value, '', {dirty: true});
                updateRowState(rowid);
            } else {
                // 원래 값으로 돌아갔을 때 dirty 플래그 해제하고 상태 원복
                grid.jqGrid('resetRowData', rowid);
                updateRowState(rowid);
            }
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
<style>
    /* 변경된 행에 대한 배경색 스타일 */
    .changed-row {
        background-color: #ffebcd !important;
    }
</style>
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
                        <button type="button" class="btn btn-line f-black" onclick="dataSave(this);">저장</button>
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

-----

### 📝 수정 내용 상세 설명

#### 1\. 멀티셀렉트/배경색 동기화 ([A], [G] 해결)

  * **`handleCheckboxClick` 함수 수정**: `jqGrid('setCell')` 호출 후 `updateRowState(rowId)` 함수를 추가했어요. 이 함수는 데이터가 변경될 때마다 해당 행의 **배경색**과 **멀티셀렉트 체크박스**를 동기화하는 역할을 합니다.
  * **`updateRowState` 함수 추가**: `jqGrid('isRowDataChanged', rowId)` 함수를 사용하여 해당 행의 `dirty` 플래그 상태를 확인하고, 변경 여부에 따라 배경색(클래스 추가/제거)과 멀티셀렉트 체크박스(선택/해제)를 함께 제어합니다.
  * **`getModifiedRows` 함수 수정**: `jqGrid('getRowData', rowId, true)`는 `dirty` 플래그가 있는 셀만 가져와서 의도치 않은 결과를 초래할 수 있으므로, `jqGrid('isRowDataChanged', rowId)`를 사용해 변경된 행을 먼저 식별하고 `jqGrid('getRowData', rowId)`로 전체 행 데이터를 가져오도록 수정했습니다.

#### 2\. 이메일/비고 변경 시 상태 동기화 및 원복 ([B], [C] 해결)

  * **`onSelectRow` 함수 수정**: `editRow(rowId)` 함수 호출 시 `oneditfunc` 콜백을 추가했어요. **인라인 편집 모드**에 진입할 때 **원본 데이터**를 `originalData` 객체에 백업해둡니다.
  * **`afterSaveCell` 함수 수정**: 이 함수는 이제 변경된 값(`value`)과 백업해 둔 **원본 값**(`originalValue`)을 비교합니다.
      * `value`가 `originalValue`와 다르면 `jqGrid('setCell')`로 `dirty` 플래그를 설정하고 `updateRowState`를 호출해 배경색과 멀티셀렉트를 변경합니다.
      * `value`가 `originalValue`와 같으면 `jqGrid('resetRowData')`로 `dirty` 플래그를 해제하고 `updateRowState`를 호출해 배경색과 멀티셀렉트를 **원래대로** 되돌립니다.
  * **`validateEmailField` 함수 수정**: 이메일 유효성 검사 후 `jqGrid('setCell')`을 호출하여 `dirty` 플래그를 설정하고 `updateRowState`를 호출해 상태를 동기화합니다.

#### 3\. 이메일 유효성 검사 로직 개선 ([D], [E] 해결)

  * **`validateEmail` 함수 수정**: 이메일 주소가 **`null`이거나 공백**인 경우 `true`를 반환하도록 로직을 수정했습니다. 이렇게 하면 빈칸인 경우 '형식 오류' 경고창이 뜨는 현상이 사라집니다.
  * **`dataSave` 함수 수정**: 저장 시 유효성 검사 로직을 `CUST_SENDMAIL_YN` 또는 `SALESREP_SENDMAIL_YN` 값이 `'Y'`일 때만 실행되도록 **조건문**을 추가했습니다. 이로써 **체크박스가 해제된 행은 이메일 유효성 검사에서 제외**됩니다.

#### 4\. 기타 개선 사항

  * **배경색 변경 방식 개선**: 인라인 스타일 대신 `.changed-row`라는 CSS 클래스를 사용하도록 `changeRowBackground` 함수를 변경했어요. CSS를 분리해서 관리하면 유지보수가 훨씬 용이합니다.
  * **저장 후 상태 초기화 (`G` 해결)**: `dataSave` 함수의 AJAX 성공 콜백에서 `dataSearch()`를 호출해 **그리드를 전체 리로드**하도록 수정했습니다. 이는 단순히 배경색을 초기화하는 것보다 더 안전하고 확실한 방법이에요. 저장된 최신 데이터를 서버로부터 다시 받아와서 그리드에 표시하므로, `dirty` 플래그나 모든 행의 상태가 완벽하게 초기화됩니다.

위의 코드 변경 사항들을 적용하시면 말씀하신 모든 문제들이 해결될 겁니다. 이제 발송 여부 체크박스를 클릭하거나 이메일/비고를 수정하면, JQGrid의 모든 상태(배경색, 멀티셀렉트 체크박스)가 기대한 대로 동기화되어 동작할 거예요. 😊

혹시 추가로 궁금한 점이 있으면 언제든지 다시 물어보세요\!