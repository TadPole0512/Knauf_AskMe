# 아래 소스에서 "CUST_MAIN_EMAIL"과 "SALESREP_EMAIL" 컬럼을 수정할 때 이메일 유효성 검사를 체크하는 로직을 추가해줘.
```html
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>

<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

< script type="text/javascript" src="${url}/include/js/common/select2/select2.js"></script>
<link rel="stylesheet" href="${url}/include/js/common/select2/select2.css" />

<style>
.changed-row {
    background-color: #D1ECF1 !important;
}

.small-checkbox input[type="checkbox"] {
    width: 16px;
    height: 16px;
}
</style>

< script type="text/javascript">
//Start. Setting Jqgrid Columns Order.
var ckNameJqGrid = 'admin/customer/customerList/jqGridCookie'; // 페이지별 쿠키명 설정. // ####### 설정 #######
ckNameJqGrid += '/gridList'; // 그리드명별 쿠키명 설정. // ####### 설정 #######

var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
var globalColumnOrder = globalColumnOrderStr.split(',');

var defaultColModel = [ //  ####### 설정 #######
{name:"CUST_CD", key:true, label:'코드', width:120, align:'center', sortable:true },
{name:"CUST_NM", label:'거래처명', width:220, align:'left', sortable:true },
{name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true, editable: true  },
{name:"CUST_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" }, cellattr: function() { return 'class="small-checkbox"'} },
{name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true },
{name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:300, align:'center', sortable:true, editable: true },
{name:"SALESREP_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" }, cellattr: function() { return 'class="small-checkbox"'} },
{name:"COMMENTS", label:'비고', width:450, align:'left', sortable:true, editable: true },
];
var defaultColumnOrder = writeIndexToStr(defaultColModel.length);
//console.log('defaultColumnOrder : ', defaultColumnOrder);
var updateComModel = []; // 전역.

if(0 < globalColumnOrder.length){ // 쿠키값이 있을때.
if(defaultColModel.length == globalColumnOrder.length){
for(var i=0,j=globalColumnOrder.length; i<j; i++){
updateComModel.push(defaultColModel[globalColumnOrder[i]]);
}

setCookie(ckNameJqGrid, globalColumnOrder, 365); // 여기서 계산을 다시 해줘야겠네.
//delCookie(ckNameJqGrid); // 쿠키삭제
}else{
updateComModel = defaultColModel;

setCookie(ckNameJqGrid, defaultColumnOrder, 365);
}
}
else{ // 쿠키값이 없을때.
updateComModel = defaultColModel;
setCookie(ckNameJqGrid, defaultColumnOrder, 365);
}
//console.log('defaultColModel : ', defaultColModel);
//console.log('updateComModel : ', updateComModel);
// End.

//### 1 WIDTH ###################################################################################################
// @@@@@@@ For Resize Column @@@@@@@
//Start. Setting Jqgrid Columns Order.
var ckNameJqGridWidth = ckNameJqGrid+'/width'; // 페이지별 쿠키명 설정.
var globalColumnWidthStr = toStr(decodeURIComponent(getCookie(ckNameJqGridWidth)));
var globalColumnWidth = globalColumnWidthStr.split(',');
// console.log('globalColumnWidthStr : ', globalColumnWidthStr);
// console.log('globalColumnWidth : ', globalColumnWidth);
var defaultColumnWidthStr = '';
var defaultColumnWidth;
var updateColumnWidth;
if('' != globalColumnWidthStr){ // 쿠키값이 있을때.
if(updateComModel.length == globalColumnWidth.length){
updateColumnWidth = globalColumnWidth;
}else{
for( var j=0; j<updateComModel.length; j++ ) {
//console.log('currentColModel[j].name : ', currentColModel[j].name);
if('rn' != updateComModel[j].name && 'cb' != updateComModel[j].name){
var v = ('' != toStr(updateComModel[j].width)) ? toStr(updateComModel[j].width) : '0';
if('' == defaultColumnWidthStr) defaultColumnWidthStr = v;
else defaultColumnWidthStr += ','+v;
}
}
defaultColumnWidth = defaultColumnWidthStr.split(',');
updateColumnWidth = defaultColumnWidth;
setCookie(ckNameJqGridWidth, defaultColumnWidth, 365);
}
}
else{ // 쿠키값이 없을때.
//console.log('updateComModel : ', updateComModel);

for( var j=0; j<updateComModel.length; j++ ) {
//console.log('currentColModel[j].name : ', currentColModel[j].name);
if('rn' != updateComModel[j].name && 'cb' != updateComModel[j].name){
var v = ('' != toStr(updateComModel[j].width)) ? toStr(updateComModel[j].width) : '0';
if('' == defaultColumnWidthStr) defaultColumnWidthStr = v;
else defaultColumnWidthStr += ','+v;
}
}
defaultColumnWidth = defaultColumnWidthStr.split(',');
updateColumnWidth = defaultColumnWidth;
setCookie(ckNameJqGridWidth, defaultColumnWidth, 365);
}
//console.log('### defaultColumnWidthStr : ', defaultColumnWidthStr);
//console.log('### updateColumnWidth : ', updateColumnWidth);

if(updateComModel.length == globalColumnWidth.length){
//console.log('이전 updateComModel : ',updateComModel);
for( var j=0; j<updateComModel.length; j++ ) {
updateComModel[j].width = toStr(updateColumnWidth[j]);
}
//console.log('이후 updateComModel : ',updateComModel);
}
// End.

$(function(){
getGridList();
});

var originalData = {}; // 최상단에 선언

//그리드 데이터 로드 및 초기화 함수
function getGridList(){
    // 검색조건 데이터 가져오기
    var searchData = getSearchData();

    // jqGrid 초기화
    $('#gridList').jqGrid({
        url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime", // 서버 호출 URL
        editurl: 'clientArray', // 행 편집 시 서버로 전송되지 않도록 설정(로컬에서만 처리)
        // editurl: './deliveryspotUpAjax.lime', // 사용 가능하지만 현재 주석처리
        datatype: "json", // 서버에서 응답받을 데이터 타입
        mtype: 'POST',    // 서버 요청 방식
        postData: searchData, // 서버로 전송할 검색 조건 데이터
        colModel: updateComModel, // 컬럼 및 속성 정의 배열
        cellEdit: true,           // 셀 단위 편집 허용
        cellsubmit: 'clientArray',// 편집결과를 로컬에 저장
        height: '360px',          // 그리드 높이
        autowidth: false,         // 가로 폭 자동 조정 안함
        rowNum : 10,              // 기본 페이지당 출력 행 수
        rowList : ['10','30','50','100'], // 페이지당 행 수 옵션
        rownumbers: true,         // 행 번호 표시
        pagination: true,         // 페이지네이션 활성화
        pager: "#pager",          // 페이지 네비게이션 영역 ID
        actions : true,           // 기본 액션 버튼 표시 여부
        pginput : true,           // 페이지 번호 직접 입력 가능
        resizable: true,          // 열 크기 조절 가능

        // 열 순서 변경 이벤트
        sortable: { 
            update: function(relativeColumnOrder){
                var grid = $('#gridList');
                
                // 기본 컬럼 이름 배열
                var defaultColIndicies = [];
                for( var i=0; i<defaultColModel.length; i++ ) {
                    defaultColIndicies.push(defaultColModel[i].name);
                }

                // 새로운 컬럼 순서 계산
                globalColumnOrder = []; 
                var columnOrder = [];
                var currentColModel = grid.getGridParam('colModel');

                for( var j=0; j<relativeColumnOrder.length; j++ ) {
                    // Row 번호(rn)나 Checkbox(cb) 제외
                    if('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name){
                        columnOrder.push(defaultColIndicies.indexOf(currentColModel[j].name));
                    }
                }
                globalColumnOrder = columnOrder;

                // 변경된 순서를 쿠키로 저장
                setCookie(ckNameJqGrid, globalColumnOrder, 365);

                // 열 너비도 함께 저장
                var tempUpdateColumnWidth = [];
                for( var j=0; j<currentColModel.length; j++ ) {
                    if('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name){
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

            // row number, row checkbox 컬럼이 맨 앞에 있으면 index 조정
            if('rn' == currentColModel[0].name || 'cb' == currentColModel[0].name) minusIdx--;
            if('rn' == currentColModel[1].name || 'cb' == currentColModel[1].name) minusIdx--;

            // 실제 조정된 컬럼 인덱스 계산
            var resizeIdx = index + minusIdx;

            // 변경된 너비 배열 반영
            updateColumnWidth[resizeIdx] = width;

            // 쿠키에 저장
            setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
        },

        sortorder: 'desc', // 정렬 순서 기본값

        jsonReader : { 
            root : 'list', // 서버 응답 JSON에서 데이터 배열 경로
        },

        // 데이터 로드 완료 후 실행
        loadComplete: function(data) {
            // 원본 데이터 저장 (변경 감지용)
            originalData = {};
            if (data.list && data.list.length > 0) {
                data.list.forEach(function(row) {
                    originalData[row.CUST_CD] = $.extend({}, row);
                });
            }

            // 체크박스 클릭 이벤트 바인딩(비동기 렌더 문제 방지 위해 setTimeout 사용)
            registerCheckboxEvents();

            // 총 건수 표시
            $('#listTotalCountSpanId').html(addComma(data.listTotalCount));
            // 현재 페이지 번호 표시
            $('.ui-pg-input').val(data.page);
        },

        // 셀 편집 저장 후 실행되는 이벤트
        afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
            //console.log("afterSaveCell - rowid: " + rowid + ", cellname: " + cellname + ", value: " + value);
            // 체크박스 관련 컬럼이면 이벤트 재바인딩으로 확실하게 보장
            if (cellname === 'CUST_SENDMAIL_YN' || cellname === 'SALESREP_SENDMAIL_YN') {
                setTimeout(function() {
                    registerCheckboxEvents();
                }, 50);
            }
            
            checkRowChanges(rowid); // 해당 행의 변경 감지 실행
        },

        // 행 선택 이벤트
        onSelectRow: function(rowId){
            // 현재는 동작 없음, 필요시 구현 가능
        },

        // 전체 선택/해제 이벤트
        onSelectAll: function(rowIdArr, status) {
            // 모든 행 선택했을 때 실행됨
            // status: true=전체선택, false=전체해제
        }
    });
}

// 체크박스 클릭 이벤트를 jqGrid 행에 연결
function registerCheckboxEvents() {
    // 이벤트 위임 방식으로 변경 - 완벽한 해결
    $('#gridList').off('click.customCheckbox', 'input[type="checkbox"]').on('click.customCheckbox', 'input[type="checkbox"]', function() {
        var $checkbox = $(this);
        var $cell = $checkbox.closest('td');
        var rowid = $cell.closest('tr').attr('id');
        var colname = '';
        
        // 클릭한 체크박스의 컬럼명 찾기
        var colModel = $('#gridList').jqGrid('getGridParam', 'colModel');
        for (var i = 0; i < colModel.length; i++) {
            if (colModel[i].name === 'CUST_SENDMAIL_YN' || colModel[i].name === 'SALESREP_SENDMAIL_YN') {
                var $cellAtIndex = $('#gridList tr[id="' + rowid + '"] td:eq(' + i + ')');
                if ($cellAtIndex.is($cell)) {
                    colname = colModel[i].name;
                    break;
                }
            }
        }
        
        if (colname) {
            //console.log("체크박스 클릭 - rowid: " + rowid + ", column: " + colname + ", checked: " + $checkbox.is(':checked'));
            
            // 체크박스 상태를 'Y'/'N'으로 setCell
            var newValue = $checkbox.is(':checked') ? 'Y' : 'N';
            $('#gridList').jqGrid('setCell', rowid, colname, newValue);
            
            // 변경 여부 체크
            setTimeout(function() {
                checkRowChanges(rowid);
            }, 50);
        }
    });
}

// 특정 행의 변경 여부를 체크하여 배경색을 적용/제거
function checkRowChanges(rowid) {
    // 비교 대상 컬럼 목록
    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
    var isChanged = false;
    var rowData = $('#gridList').jqGrid('getRowData', rowid);

    //console.log("checkRowChanges - rowid: " + rowid);
    //console.log("현재 rowData:", rowData);
    //console.log("원본 originalData:", originalData[rowid]);

    for (var i = 0; i < compareColumns.length; i++) {
        var col = compareColumns[i];
        var original = originalData[rowid] && originalData[rowid][col];
        var current = rowData[col];

        // null, undefined, 공백 보정
        current = current === undefined || current === null ? "" : (current + "").trim();
        original = original === undefined || original === null ? "" : (original + "").trim();

        // Y/N 컬럼이면 통일된 값으로 변환
        if (col.endsWith('_YN')) {
            current = toYN(current);
            original = toYN(original);
        }

        //console.log(col + " - 원본: '" + original + "', 현재: '" + current + "'");

        // 값이 다르면 변경 발생
        if (current != original) {
            isChanged = true;
            //console.log(col + " 컬럼이 변경되었습니다!");
            break;
        }
    }

    // row에 변경 색상 적용 여부 반영
    var $tr = $('#gridList tr[id="' + rowid + '"]');
    if (isChanged) {
        $tr.addClass("changed-row");
        //console.log("배경색 변경 적용: " + rowid);
    } else {
        $tr.removeClass("changed-row");
        //console.log("배경색 변경 제거: " + rowid);
    }
}

// 값이 Y/N 성격일 때, 여러 표현을 통일하여 'Y' 또는 'N'으로 변환
function toYN(val) {
    if (val === undefined || val === null) return "";
    val = (val + "").toUpperCase().trim();
    if (val === "Y" || val === "YES" || val === "1" || val === "TRUE") return "Y";
    if (val === "N" || val === "NO"  || val === "0" || val === "FALSE") return "N";
    return val; // 위 조건 외 값은 그대로 반환
}

// 저장/수정.
function dataInUp(obj, val) {
$(obj).prop('disabled', true);

var chk = $('#gridList').jqGrid('getGridParam','selarrrow');
chk += '';
var chkArr = chk.split(",");
if (chk == '') {
alert("선택 후 진행해 주십시오.");
$(obj).prop('disabled', false);
return false;
}

var iFormObj = $('form[name="iForm"]');
iFormObj.empty();

var ckflag = true;
for (var i=0; i<chkArr.length; i++){
var trObj = $('#jqg_gridList_' + chkArr[i]).closest('tr');

var process_type = ('' == toStr(trObj.find('input[name="R_PT_CODE"]').val())) ? 'ADD' : 'EDIT';

//validation
if(ckflag && '' == trObj.find('select[name="PT_USE"]').val()){
alert('상태를 선택해 주세요.');
trObj.find('select[name="PT_USE"]').focus();
ckflag = false;
}
if(ckflag) ckflag = validation(trObj.find('input[name="PT_SORT"]')[0], '출력순서', 'value');
if(ckflag && 'ADD' == process_type) ckflag = validation(trObj.find('input[name="M_PT_CODE"]')[0], '출고지 코드', 'value');
if(ckflag) ckflag = validation(trObj.find('input[name="PT_NAME"]')[0], '출고지명', 'value');
//if(ckflag) ckflag = validation(trObj.find('input[name="PT_ZONECODE"]')[0], '우편번호', 'value');
//if(ckflag) ckflag = validation(trObj.find('input[name="PT_ADDR2"]')[0], '상세주소', 'value');
if(ckflag) ckflag = validation(trObj.find('input[name="PT_TEL"]')[0], '연락처', 'alltlp'); //alltlp=휴대폰+일반전화번호+050+070 체크, '-' 제외

if(!ckflag){
$(obj).prop('disabled', false);
return false;
}

// aForm append.
iFormObj.append('<input type="hidden" name="r_processtype" value="' + process_type + '" />');
if('ADD' == process_type){
iFormObj.append('<input type="hidden" name="r_ptcode" value="' + toStr(trObj.find('input[name="M_PT_CODE"]').val()) + '" />');
}else{
iFormObj.append('<input type="hidden" name="r_ptcode" value="' + toStr(trObj.find('input[name="R_PT_CODE"]').val()) + '" />');
}

iFormObj.append('<input type="hidden" name="m_ptuse" value="' + toStr(trObj.find('select[name="PT_USE"]').val()) + '" />');
iFormObj.append('<input type="hidden" name="m_ptsort" value="' + toStr(trObj.find('input[name="PT_SORT"]').val()) + '" />');
iFormObj.append('<input type="hidden" name="m_ptname" value="' + toStr(trObj.find('input[name="PT_NAME"]').val()) + '" />');
iFormObj.append('<input type="hidden" name="m_ptzonecode" value="' + toStr(trObj.find('input[name="PT_ZONECODE"]').val()) + '" />');
iFormObj.append('<input type="hidden" name="m_ptzipcode" value="' + toStr(trObj.find('input[name="PT_ZIPCODE"]').val()) + '" />');
iFormObj.append('<input type="hidden" name="m_ptaddr1" value="' + toStr(trObj.find('input[name="PT_ADDR1"]').val()) + '" />');
iFormObj.append('<input type="hidden" name="m_ptaddr2" value="' + toStr(trObj.find('input[name="PT_ADDR2"]').val()) + '" />');
iFormObj.append('<input type="hidden" name="m_pttel" value="' + toStr(trObj.find('input[name="PT_TEL"]').val()) + '" />');
}
console.log($(iFormObj).html());

if(!ckflag){
$(obj).prop('disabled', false);
return false;
}

if (confirm('저장 하시겠습니까?')) {
var iFormData = iFormObj.serialize();
var url = '${url}/admin/system/insertUpdatePlantAjax.lime'; 
$.ajax({
async : false,
data : iFormData,
type : 'POST',
url : url,
success : function(data) {
if (data.RES_CODE == '0000') {
alert(data.RES_MSG);
dataSearch();
}else{

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

function getSearchData(){
var rl_custcd = $('input[name="rl_custcd"]').val();
var rl_custnm = $('input[name="rl_custnm"]').val();
var rl_salesrepnm = $('input[name="rl_salesrepnm"]').val();

var r_salesepcY = '', r_salesepcN = ''; //영업담당자 YN
if($('input[name="r_salesrepcdyn"]:checked').length == 1){
if($('input[name="r_salesrepcdyn"]:checked').val() == 'Y') r_salesepcN = '0';
else r_salesepcY = '0';
}

var sData = {
rl_custcd : rl_custcd
, rl_custnm : rl_custnm
, rl_salesrepnm : rl_salesrepnm
, r_salesrepcdyn : r_salesepcY
, rn_salesrepcdyn : r_salesepcN
};
return sData;
}

// 조회
function dataSearch() {
var searchData = getSearchData();
$('#gridList').setGridParam({
postData : searchData
}).trigger("reloadGrid");
}

// 엑셀다운로드
function excelDown(obj){
$('#ajax_indicator').show().fadeIn('fast');
var token = getFileToken('excel');
$('form[name="frm"]').append('<input type="hidden" name="filetoken" value="'+token+'" />');

formPostSubmit('frm', '${url}/admin/customer/customerExcelDown.lime');
$('form[name="frm"]').attr('action', '');

$('input[name="filetoken"]').remove();
var fileTimer = setInterval(function() {
//console.log('token : ', token);
        //console.log("cookie : ", getCookie(token));
if('true' == getCookie(token)){
$('#ajax_indicator').fadeOut();
delCookie(token);
clearInterval(fileTimer);
}
    }, 1000 );
}

//gridList Reload.
function reloadGridList(){
$('#gridList').trigger('reloadGrid', [{current:true}]); // 리로드후 현재 유지.

var gridRowId = toStr($('#gridList').getGridParam('selrow'));
if('' != toStr(gridRowId)){
$('#gridList').setSelection(gridRowId, true); // 리로드후 선택
}
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
<%-- <form name="uForm" method="post" action="${url}/admin/system/deliverySpotEditPop.lime" target="deliverySpotEditPop"></form> --%>

<form name="frm" method="post">

<!-- Page Inner -->
<div class="page-inner">
<div class="page-title">
<h3>
거래처현황
<div class="page-right">
<button type="button" class="btn btn-line f-black" title="검색" onclick="dataSearch();"><i class="fa fa-search"></i><em>검색</em></button>
<button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();"><i class="fa fa-refresh"></i><em>새로고침</em></button>
<button type="button" class="btn btn-line f-black" title="엑셀다운로드" onclick="excelDown(this);"><i class="fa fa-file-excel-o"></i><em>엑셀다운로드</em></button>
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
<input type="text" class="search-input" name="rl_custcd" value="${param.rl_custcd}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
</div>
</li>
<li>
<label class="search-h">거래처명</label>
<div class="search-c">
<input type="text" class="search-input" name="rl_custnm" value="${param.rl_custnm}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
</div>
</li>
<li>
<label class="search-h">영업담당</label>
<div class="search-c">
<input type="text" class="search-input" name="rl_salesrepnm" value="${param.rl_salesrepnm}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
</div>
</li>
<li>
<label class="search-h">영업담당자</label>
<div class="search-c checkbox">
<label><input type="checkbox" name="r_salesrepcdyn" value="Y" onclick="dataSearch();" />Y</label>
<label><input type="checkbox" name="r_salesrepcdyn" value="N" onclick="dataSearch();" />N</label>
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

이메일 유효성 검사를 추가하는 방법을 단계별로 설명해줄게.

## 1. 이메일 유효성 검사 함수 추가

먼저 script 태그 안에 이메일 유효성 검사 함수를 추가해:

```javascript
// 이메일 유효성 검사 함수
function validateEmail(email) {
    if (!email || email.trim() === '') {
        return true; // 빈 값은 허용 (필수가 아닌 경우)
    }
    
    var emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email.trim());
}

// 이메일 유효성 검사 및 에러 표시
function checkEmailValidation(rowid, cellname, value) {
    if (cellname === 'CUST_MAIN_EMAIL' || cellname === 'SALESREP_EMAIL') {
        if (!validateEmail(value)) {
            alert(cellname === 'CUST_MAIN_EMAIL' ? 
                '담당자 이메일 형식이 올바르지 않습니다.' : 
                '영업 담당 이메일 형식이 올바르지 않습니다.');
            
            // 잘못된 값 원래대로 복원
            var originalValue = originalData[rowid] ? originalData[rowid][cellname] : '';
            $('#gridList').jqGrid('setCell', rowid, cellname, originalValue);
            
            return false;
        }
    }
    return true;
}
```

## 2. jqGrid 설정에 이벤트 핸들러 추가

기존 `afterSaveCell` 이벤트를 수정해:

```javascript
// 셀 편집 저장 후 실행되는 이벤트
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    // 이메일 유효성 검사
    if (!checkEmailValidation(rowid, cellname, value)) {
        return; // 유효하지 않으면 중단
    }
    
    // 체크박스 관련 컬럼이면 이벤트 재바인딩
    if (cellname === 'CUST_SENDMAIL_YN' || cellname === 'SALESREP_SENDMAIL_YN') {
        setTimeout(function() {
            registerCheckboxEvents();
        }, 50);
    }
    
    checkRowChanges(rowid); // 해당 행의 변경 감지 실행
},

// 셀 편집 시작 전 검사 (선택사항)
beforeEditCell: function(rowid, cellname, value, iRow, iCol) {
    // 편집 시작 전에도 체크할 수 있음
    return true;
},
```

## 3. 컬럼 모델에 editrules 추가

기존 컬럼 정의를 수정해:

```javascript
var defaultColModel = [
    {name:"CUST_CD", key:true, label:'코드', width:120, align:'center', sortable:true },
    {name:"CUST_NM", label:'거래처명', width:220, align:'left', sortable:true },
    {
        name:"CUST_MAIN_EMAIL", 
        label:'담당자 이메일', 
        width:220, 
        align:'center', 
        sortable:true, 
        editable: true,
        editrules: {
            custom: true,
            custom_func: function(value, colname) {
                if (!validateEmail(value)) {
                    return [false, "올바른 이메일 형식을 입력해주세요."];
                }
                return [true, ""];
            }
        }
    },
    {name:"CUST_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" }, cellattr: function() { return 'class="small-checkbox"'} },
    {name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true },
    {
        name:"SALESREP_EMAIL", 
        label:'영업 담당 이메일', 
        width:300, 
        align:'center', 
        sortable:true, 
        editable: true,
        editrules: {
            custom: true,
            custom_func: function(value, colname) {
                if (!validateEmail(value)) {
                    return [false, "올바른 이메일 형식을 입력해주세요."];
                }
                return [true, ""];
            }
        }
    },
    {name:"SALESREP_SENDMAIL_YN", label:'발송 여부', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" }, cellattr: function() { return 'class="small-checkbox"'} },
    {name:"COMMENTS", label:'비고', width:450, align:'left', sortable:true, editable: true },
];
```

## 4. 실시간 유효성 검사 (선택사항)

더 나은 사용자 경험을 위해 실시간 검사를 추가할 수도 있어:

```javascript
// jqGrid 설정에 추가
onCellSelect: function(rowid, iCol, cellcontent, e) {
    var colModel = $('#gridList').jqGrid('getGridParam', 'colModel');
    var colname = colModel[iCol].name;
    
    if (colname === 'CUST_MAIN_EMAIL' || colname === 'SALESREP_EMAIL') {
        // 포커스 아웃 시 유효성 검사
        setTimeout(function() {
            $('input[type="text"]:focus').on('blur', function() {
                var value = $(this).val();
                if (value && !validateEmail(value)) {
                    $(this).css('border', '2px solid red');
                    $(this).attr('title', '올바른 이메일 형식을 입력해주세요');
                } else {
                    $(this).css('border', '');
                    $(this).removeAttr('title');
                }
            });
        }, 100);
    }
}
```

## 주요 특징

1. **정규식 패턴**: 표준 이메일 형식을 검증
2. **빈 값 허용**: 필수 항목이 아닌 경우 빈 값 허용
3. **에러 복원**: 잘못된 값 입력 시 원래 값으로 복원
4. **사용자 친화적**: 명확한 에러 메시지 제공
5. **실시간 피드백**: 입력 중에도 유효성 검사 가능

이 코드를 추가하면 이메일 컬럼 수정 시 자동으로 유효성 검사가 실행되고, 잘못된 형식일 때 사용자에게 알림이 표시될 거야.
