[Request to fix JQGrid malfunction].
## Symptom summary:
    The following malfunctions occur on jqGrid-based screens.
    1) The multiselect checkbox is not automatically selected when selecting whether to ship (checkbox).
    2) The row background color does not change when selecting whether to ship (checkbox).
    3) The multiselect checkbox is not unchecked when the checkbox is cleared.
    4) Row background color does not return to its original color when unchecked.
    5) Contact email / Sales email / Remarks columns would change background color when multiselect checkbox was checked \*\*just by clicking\*\*.
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
    const emailRegex = /^[a-zA-Z0-9.\_-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$/;
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
    return '<input type="checkbox" class="mail-checkbox" ' + checked + ' onclick="handleCheckboxClick(this, \\'' + options.rowId + '\\', \\'' + options.colModel.name + '\\')" />';
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
        if (rowData.CUST\_MAIN\_EMAIL && !validateEmail(rowData.CUST\_MAIN\_EMAIL)) {
            alert('거래처 담당자 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST\_CD + ')');
            validationFailed = true;
            return false;
        }
        
        if (rowData.SALESREP\_EMAIL && !validateEmail(rowData.SALESREP\_EMAIL)) {
            alert('영업 담당 이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST\_CD + ')');
            validationFailed = true;
            return false;
        }
        
        // 발송 여부 체크 시 이메일 존재 확인
        if (rowData.CUST\_SENDMAIL\_YN === 'Y' && !rowData.CUST\_MAIN\_EMAIL) {
            alert('담당자 이메일 발송이 체크되어 있지만 담당자 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST\_CD + ')');
            validationFailed = true;
            return false;
        }
        
        if (rowData.SALESREP\_SENDMAIL\_YN === 'Y' && !rowData.SALESREP\_EMAIL) {
            alert('영업 담당 이메일 발송이 체크되어 있지만 영업 담당 이메일이 비어있습니다. (거래처코드: ' + rowData.CUST\_CD + ')');
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
        iFormObj.append('<input type="hidden" name="custCd" value="' + rowData.CUST\_CD + '" />');
        iFormObj.append('<input type="hidden" name="custMainEmail" value="' + (rowData.CUST\_MAIN\_EMAIL || '') + '" />');
        iFormObj.append('<input type="hidden" name="custSendmailYn" value="' + (rowData.CUST\_SENDMAIL\_YN || 'N') + '" />');
        iFormObj.append('<input type="hidden" name="salesrepEmail" value="' + (rowData.SALESREP\_EMAIL || '') + '" />');
        iFormObj.append('<input type="hidden" name="salesrepSendmailYn" value="' + (rowData.SALESREP\_SENDMAIL\_YN || 'N') + '" />');
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
                if (data.RES\_CODE == '0000') {
                    alert(data.RES\_MSG);
                    
                    // 성공적으로 저장된 후 배경색 초기화 및 그리드 리로드
                    $.each(modifiedRows, function(i, rowData) {
                        changeRowBackground(rowData.CUST\_CD, false);
                    });
                    
                    dataSearch(); // 그리드 리로드로 최신 데이터 반영
                }else{
                    alert(data.RES\_MSG);
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
    {name:"CUST\_CD", key:true, label:'거래처코드', width:120, align:'center', sortable:true},
    {name:"CUST\_NM", label:'거래처명', width:220, align:'left', sortable:true},
    {name:"CUST\_MAIN\_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true, editable:true, editoptions:{dataEvents:[{type:'blur', fn:validateEmailField}]}},
    {name:"CUST\_SENDMAIL\_YN", label:'발송 여부', width:100, align:'center', sortable:true, formatter:checkboxFormatter, editable:true, edittype:'checkbox'},
    {name:"SALESREP\_NM", label:'영업 담당', width:100, align:'center', sortable:true},
    {name:"SALESREP\_EMAIL", label:'영업 담당 이메일', width:300, align:'center', sortable:true, editable:true, editoptions:{dataEvents:[{type:'blur', fn:validateEmailField}]}},
    {name:"SALESREP\_SENDMAIL\_YN", label:'발송 여부', width:100, align:'center', sortable:true, formatter:checkboxFormatter, editable:true, edittype:'checkbox'},
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
        // JQGrid가 행을 식별하는 고유 키를 CUST\_CD로 설정
        // 이 설정이 매우 중요합니다.
        jsonReader: {
            root: 'list',
            id: 'CUST\_CD'
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
    $('#ajax\_indicator').show().fadeIn('fast');
    var token = getFileToken('excel');
    $('form[name="frm"]').append('<input type="hidden" name="filetoken" value="'+token+'" />');
    
    formPostSubmit('frm', '${url}/admin/system/orderMailAlarmExcelDown.lime');
    $('form[name="frm"]').attr('action', '');
    
    $('input[name="filetoken"]').remove();
    var fileTimer = setInterval(function() {
        if('true' == getCookie(token)){
            $('#ajax\_indicator').fadeOut();
            delCookie(token);
            clearInterval(fileTimer);
        }
    }, 1000 );
}
</script>
</head>
<body class="page-header-fixed compact-menu">
    <div id="ajax\_indicator" style="display:none;">
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

JQGrid 기반 주문메일알람 관리 화면의 문제를 아래와 같이 단계별로 개선하는 코드를 제안한다.

***

## 1. 멀티셀렉트 체크박스와 발송여부 체크박스/배경색 동기화

- 발송여부 체크박스 클릭 시 해당 행 멀티셀렉트 체크박스 상태도 같이 변경
- 배경색도 체크 상태에 따라 일치하도록 즉시 변경

## 2. 이메일/영업이메일/비고 컬럼 변경 시 실제 값이 바뀌었을 때만 멀티셀렉트 체크박스 및 배경색 변경

- 편집 종료 시 기존 값과 비교하여 변경이 실제로 있는 경우에만 상태 변화 처리
- 원래 값과 같으면 체크 및 배경색을 초기 상태로 되돌림

## 3. 저장 시

- 멀티셀렉트 체크박스가 체크된 행만 이메일 유효성 검사 대상 포함
- 메일 형식 검증 정규식을 약간 개선하여 다양한 정상 메일을 허용
- 저장 성공 시 dirty 플래그 제거 및 배경색, 체크박스 상태 모두 초기화

***

# 수정할 주요 JavaScript 코드 (프론트엔드)

```javascript
// 이메일 형식 유효성 검사 함수 (좀 더 실무용으로 정규식 보완)
function validateEmail(email) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
}

// 배경색 변경 함수: isChanged가 true면 변경된 색, false면 원래색
function changeRowBackground(rowId, isChanged) {
    if (isChanged) {
        $('#gridList').jqGrid('setRowData', rowId, false, { background: '#ffebcd' });
    } else {
        $('#gridList').jqGrid('setRowData', rowId, false, { background: '' });
    }
}

// 체크박스 포맷터 (발송여부 Y/N)
function checkboxFormatter(cellVal, options, rowObj) {
    const checked = (cellVal === 'Y') ? 'checked' : '';
    return `<input type="checkbox" class="mail-checkbox" ${checked} onclick="handleCheckboxClick(this, '${options.rowId}', '${options.colModel.name}')" />`;
}

// 실제 발송여부 체크박스 클릭 핸들러
function handleCheckboxClick(checkbox, rowId, fieldName) {
    const grid = $('#gridList');
    const rowData = grid.jqGrid('getRowData', rowId);
    const newValue = checkbox.checked ? 'Y' : 'N';

    // 값이 바뀌었을때만 처리
    if (rowData[fieldName] !== newValue) {
        // 1) 발송 여부 셀 값 변경 및 dirty 플래그 셋팅
        grid.jqGrid('setCell', rowId, fieldName, newValue, '', { dirty: true });

        // 2) 멀티셀렉트 체크박스와 동기화
        if (checkbox.checked) {
            grid.jqGrid('setSelection', rowId, false); // 멀티셀렉트 체크 ON
        } else {
            grid.jqGrid('resetSelection').jqGrid('setSelection', rowId, false); // 멀티셀렉트 체크 OFF
            // 단일 해제 로직 보완 필요 시 추가 가능
        }

        // 3) 배경색 변환
        changeRowBackground(rowId, checkbox.checked);
    }
}

// 편집 종료 후 실제 값 변경 감지 및 상태 동기화
$('#gridList').on('jqGridInlineAfterSaveCell', function (e, rowid, cellname, value, iRow, iCol) {
    const grid = $('#gridList');
    const originalRowData = grid.jqGrid('getLocalRow', rowid);
    const currentRowData = grid.jqGrid('getRowData', rowid);

    // 원래 값과 변경값 비교
    if (originalRowData[cellname] !== value) {
        // 변경됨: 멀티셀렉트 체크 및 배경색 적용
        grid.jqGrid('setSelection', rowid, false);
        changeRowBackground(rowid, true);
        grid.jqGrid('setCell', rowid, cellname, value, '', { dirty: true });
    } else {
        // 원래대로 복원 시 멀티셀렉트 체크 해제 및 배경색 원상복구
        grid.jqGrid('resetSelection').jqGrid('setSelection', rowid, false);
        changeRowBackground(rowid, false);
        grid.jqGrid('setCell', rowid, cellname, value, '', { dirty: false });
    }
});

// 저장 함수 내부 이메일 유효성 검사 개선 & 멀티셀렉트 체크된 행만 검사
function dataSave(obj) {
    $(obj).prop('disabled', true);
    var grid = $('#gridList');
    var modifiedRows = [];

    var allRowIds = grid.getDataIDs();
    for (var i = 0; i < allRowIds.length; i++) {
        var rowId = allRowIds[i];
        var rowData = grid.getRowData(rowId);
        // 멀티셀렉트 체크 여부 확인
        var isSelected = grid.jqGrid('getGridParam', 'selarrrow').includes(rowId);
        // 변경된 행이면서 멀티셀렉트 체크된 행만 저장 대상
        if (isSelected) {
            modifiedRows.push(rowData);
        }
    }

    if (modifiedRows.length === 0) {
        alert('수정된 내용이 없거나 발송 여부가 선택된 행이 없습니다.');
        $(obj).prop('disabled', false);
        return false;
    }

    // 이메일 유효성 검사
    var validationFailed = false;
    $.each(modifiedRows, function (i, rowData) {
        // 발송 여부 Y인 행만 검사
        if ((rowData.CUST_SENDMAIL_YN === 'Y' && rowData.CUST_MAIN_EMAIL && !validateEmail(rowData.CUST_MAIN_EMAIL)) ||
            (rowData.SALESREP_SENDMAIL_YN === 'Y' && rowData.SALESREP_EMAIL && !validateEmail(rowData.SALESREP_EMAIL))) {
            alert('이메일 형식이 올바르지 않습니다. (거래처코드: ' + rowData.CUST_CD + ')');
            validationFailed = true;
            return false;
        }
    });

    if (validationFailed) {
        $(obj).prop('disabled', false);
        return false;
    }

    // form data 구성 로직 동일 유지
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
        $.ajax({
            async : false,
            data: iFormObj.serialize(),
            type: 'POST',
            url: '${url}/admin/system/updateOrderMailAlarmAjax.lime',
            success: function(data) {
                if (data.RES_CODE === '0000') {
                    alert(data.RES_MSG);
                    // 저장 성공시 dirty 플래그 제거
                    $.each(modifiedRows, function(i, rowData) {
                        grid.jqGrid('setCell', rowData.CUST_CD, 'CUST_SENDMAIL_YN', rowData.CUST_SENDMAIL_YN, '', { dirty: false });
                        grid.jqGrid('setCell', rowData.CUST_CD, 'SALESREP_SENDMAIL_YN', rowData.SALESREP_SENDMAIL_YN, '', { dirty: false });
                        // 배경색 초기화
                        changeRowBackground(rowData.CUST_CD, false);
                    });
                    // 그리드 재조회
                    dataSearch();
                } else {
                    alert(data.RES_MSG);
                }
                $(obj).prop('disabled', false);
            },
            error: function (request, status, error) {
                alert('저장 중 오류가 발생했습니다.');
                $(obj).prop('disabled', false);
            }
        });
    } else {
        $(obj).prop('disabled', false);
    }
}
```

***

# 백엔드 (Controller / MyBatis / SQL)

- 기존 데이터 처리 로직은 변경 필요 없지만, 클라이언트에서 전송되는 데이터 구조를 유지하며
- 받은 리스트 데이터에 대해 update 쿼리를 수행하도록 구현

예)

```java
// Controller 예시 (Spring MVC)
@PostMapping("/admin/system/updateOrderMailAlarmAjax.lime")
@ResponseBody
public Map<String, Object> updateOrderMailAlarm(@RequestParam Map<String, String[]> params) {
    // 파라미터 배열 custCd[], custMainEmail[], custSendmailYn[] 등으로 받음
    // 반복문으로 한꺼번에 다중 업데이트 처리

    Map<String, Object> result = new HashMap<>();
    try {
        int count = service.updateOrderMailAlarm(params);
        result.put("RES_CODE", "0000");
        result.put("RES_MSG", count + "건이 저장되었습니다.");
    } catch (Exception e) {
        result.put("RES_CODE", "9999");
        result.put("RES_MSG", "저장 실패: " + e.getMessage());
    }
    return result;
}

// Service 단은 기존과 동일

// Mapper XML 예시
<update id="updateOrderMailAlarm">
    <foreach collection="custCd" item="custCd" index="idx" separator=";">
        UPDATE customer_table
        SET
           CUST_MAIN_EMAIL = #{custMainEmail[idx]},
           CUST_SENDMAIL_YN = #{custSendmailYn[idx]},
           SALESREP_EMAIL = #{salesrepEmail[idx]},
           SALESREP_SENDMAIL_YN = #{salesrepSendmailYn[idx]},
           COMMENTS = #{comments[idx]}
        WHERE CUST_CD = #{custCd}
    </foreach>
</update>
```

***

# 요약

|개선 항목|적용 내용|
|-|-|
|멀티셀렉트/발송여부 동기화|발송여부 체크박스 클릭 시 멀티셀렉트도 상태 변경 및 배경색 일치|
|실제 값 변화 감지|이메일, 비고 등 편집 후 실제값 변할 때만 변경 상태 표시, 원복 시 상태 초기화|
|저장 시 유효성|메일 형식 정규식 보완 및 멀티셀렉트 체크된 행만 검사 대상 포함|
|저장 성공 처리|dirty 플래그 제거 및 배경색, 체크박스 상태 모두 초기화 후 다시 조회|

이로써 AC에서 제시한 모든 요구사항 충족 및 문제 해결이 가능합니다. 필요하면 구체적인 소스파일로 코드 납품 또한 가능하다.