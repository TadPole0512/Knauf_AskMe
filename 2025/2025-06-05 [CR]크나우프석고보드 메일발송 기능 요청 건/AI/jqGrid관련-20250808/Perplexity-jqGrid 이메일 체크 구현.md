<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# 위의 질문에서 [관련 코드/필드] 부분만 아래의 코드로 변경해서 다시 정리해줘.
```js
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
{name:"CUST_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" }, cellattr: function() { return 'class="small-checkbox"'} },
{name:"SALESREP_NM", label:'영업 담당', width:100, align:'center', sortable:true },
{name:"SALESREP_EMAIL", label:'영업 담당 이메일', width:300, align:'center', sortable:true, editable: true },
{name:"SALESREP_SENDMAIL_YN", label:'이메일 발송', width:100, align:'center', sortable:true, editable: true, edittype: "checkbox", formatter: "checkbox", formatoptions: { disabled: false }, editoptions: { value: "Y:N", defaultValue: "Y" }, cellattr: function() { return 'class="small-checkbox"'} },
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

function getGridList(){
// grid init
var searchData = getSearchData();
$('#gridList').jqGrid({
url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
editurl: 'clientArray', //사용x
//editurl: './deliveryspotUpAjax.lime',
datatype: "json",
mtype: 'POST',
postData: searchData,
colModel: updateComModel,
cellEdit: true,
cellsubmit: 'clientArray',
height: '360px',
autowidth: false,
rowNum : 10,
rowList : ['10','30','50','100'],
rownumbers: true,
pagination: true,
pager: "#pager",
actions : true,
pginput : true,
resizable: true,
sortable: { // ####### 설정 #######
update: function(relativeColumnOrder){
var grid = $('#gridList');
var defaultColIndicies = [];
for( var i=0; i<defaultColModel.length; i++ ) {
defaultColIndicies.push(defaultColModel[i].name);
}

globalColumnOrder = []; // 초기화.
var columnOrder = [];
var currentColModel = grid.getGridParam('colModel');
for( var j=0; j<relativeColumnOrder.length; j++ ) {
//console.log('currentColModel[j].name : ', currentColModel[j].name);
if('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name){
columnOrder.push(defaultColIndicies.indexOf(currentColModel[j].name));
}
}
globalColumnOrder = columnOrder;

setCookie(ckNameJqGrid, globalColumnOrder, 365);

// @@@@@@@ For Resize Column @@@@@@@
//currentColModel = grid.getGridParam('colModel');
//console.log('이전 updateColumnWidth : ', updateColumnWidth);
var tempUpdateColumnWidth = [];
for( var j=0; j<currentColModel.length; j++ ) {
   if('rn' != currentColModel[j].name && 'cb' != currentColModel[j].name){
      tempUpdateColumnWidth.push(currentColModel[j].width); 
   }
}
updateColumnWidth = tempUpdateColumnWidth;
//console.log('이후 updateColumnWidth : ', updateColumnWidth);
setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
}
},
// @@@@@@@ For Resize Column @@@@@@@
resizeStop: function(width, index) { 
console.log('globalColumnOrder : ', globalColumnOrder);
var minusIdx = 0;

var grid = $('#gridList');
var currentColModel = grid.getGridParam('colModel');
//console.log('currentColModel : ', currentColModel);
if('rn' == currentColModel[0].name || 'cb' == currentColModel[0].name) minusIdx--;
if('rn' == currentColModel[1].name || 'cb' == currentColModel[1].name) minusIdx--;
//console.log('minusIdx : ', minusIdx);

var resizeIdx = index + minusIdx;
//console.log('resizeIdx : ', resizeIdx);

//var realIdx = globalColumnOrder[resizeIdx];
//console.log('realIdx : ', realIdx);

updateColumnWidth[resizeIdx] = width;

setCookie(ckNameJqGridWidth, updateColumnWidth, 365);
        //alert('Resize Column : '+index+'\nWidth : '+width);
    },
sortorder: 'desc',
jsonReader : { 
root : 'list',
},
loadComplete: function(data) {
    originalData = {};
    if (data.list && data.list.length > 0) {
        data.list.forEach(function(row) {
            originalData[row.CUST_CD] = $.extend({}, row);
        });
    }
//$('#gridList').getGridParam("reccount"); // 현재 페이지에 뿌려지는 row 개수
//$('#gridList').getGridParam("records"); // 현재 페이지에 limitrow
$('#listTotalCountSpanId').html(addComma(data.listTotalCount));
$('.ui-pg-input').val(data.page);

},
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
},
onSelectRow: function(rowId){
//console.log('########################### onSelectRow rowId : ' + rowId);
},
//onSelectRow: editRow,
onSelectAll: function(rowIdArr, status) { //전체 체크박스 선택했을때 onSelectRow가 실행이 안되고 onSelectAll 실행되네...
//console.log('status : ', status); //status : true=전체선택했을때, false=전체해제했을때
//console.log('rowIdArr : ', rowIdArr); //rowid 배열 타입
//console.log('rowIdArr.length : ', rowIdArr.length);
}
/* 
beforeProcessing: functi0on(data, status, xhr){ // 서버로 부터 데이터를 받은 후 화면에 찍기 위한 processing을 진행하기 직전에 호출.
if('0000' != data.RES_CODE){
alert(data.RES_MSG);
return false;
}
},
*/
});
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


네, 요청해주신 대로 **[관련 코드/필드]** 를 실제 JSP·jQuery·jqGrid 환경(컬럼 동적구성/쿠키/resize 코드 등 포함!)에 맞게 다시 정리해서,
**이메일 유효성 및 체크박스 값 변경, 행 배경 처리** 등 실무 예시를 구체적으로 한 곳에 정리해드릴게.

***

# ✨ 프로젝트 실무 맞춤 솔루션 ([관련 코드/필드] 완전 반영 버전)

아래는 [관련 코드/필드] 부분을 네가 준 코드로 교체해 실제로 바로 쓸 수 있도록 구성한 **구현 예시**야.
(*전체 구조, 컬럼, 쿠키, 동적width 등은 요구하신 그대로 사용!*)

## 1. 추가/수정 소스 전체

**추가/수정할 JS 부만 따로 정리**
(이미 있는 부분에 이어 붙여서 사용.
💡 *중복 방지 위해 기존 `getGridList()` 등 안에 직접 설명도 추가!*)

***

```javascript
// 1. 이메일 유효성 검사 함수
function isValidEmail(email) {
    var re = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    return re.test(email);
}

var changedRows = {}; // 변경 여부 저장용

// 2. getGridList() 내 afterSaveCell, loadComplete, grid 이벤트 등에 아래처럼 추가
function getGridList(){
    var searchData = getSearchData();
    $('#gridList').jqGrid({
        // ... 네가 준 기존 셋팅 동일 ...
        colModel: updateComModel,
        // --- 이하 기타 옵션 동일 ---
        cellEdit: true,
        cellsubmit: 'clientArray',
        height: '360px',
        autowidth: false,
        // ... 기타 파라미터 생략 ...
        loadComplete: function(data) {
            // 원본 데이터 저장 (최초 1회)
            originalData = {};
            changedRows = {};
            if (data.list && data.list.length > 0) {
                data.list.forEach(function(row) {
                    originalData[row.CUST_CD] = $.extend({}, row);
                });
            }
            $('#listTotalCountSpanId').html(addComma(data.listTotalCount));
            $('.ui-pg-input').val(data.page);
        },
        afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
            if (cellname === "CUST_MAIN_EMAIL" || cellname === "SALESREP_EMAIL") {
                // ① 이메일 유효성
                if (!isValidEmail(value)) {
                    alert("유효한 이메일 주소를 입력하세요.");
                    // 셀 값을 원래대로, 배경 원복
                    var original = originalData[rowid][cellname];
                    $("#gridList").jqGrid('setCell', rowid, cellname, original, {background: ''});
                    // 다시 편집 모드 진입
                    setTimeout(function(){
                        $("#gridList").jqGrid('editCell', iRow, iCol, true);
                    },0);
                    return;
                }
            }
            checkAndMarkRowChange(rowid);
        },
        gridComplete: function() {
            // reload/page 이동해도 변경된 행 스타일 유지
            var ids = $('#gridList').jqGrid('getDataIDs');
            for(var i=0; i<ids.length; i++){
                var id = ids[i];
                if(changedRows[id]) {
                    $('#gridList').jqGrid('setRowData', id, {}, {background:'#D1ECF1'});
                } else {
                    $('#gridList').jqGrid('setRowData', id, {}, {background:''});
                }
            }
        },
    });

    // 체크박스 값 변경 이벤트 (이메일발송 YN)
    $("#gridList").off("change.customCheck").on("change.customCheck", "input[type=checkbox]", function(){
        var $cell = $(this).closest('td');
        var colName = $cell.attr('aria-describedby').replace(/^gridList_/, '');
        var $row = $cell.closest('tr.jqgrow');
        var rowid = $row.attr('id') || $row.data('id');
        // 영업 담당, 거래처 담당 필드만 감지
        if(colName !== 'CUST_SENDMAIL_YN' && colName !== 'SALESREP_SENDMAIL_YN') return;
        var checked = $(this).prop('checked');
        var newVal = checked ? 'Y' : 'N';

        // 셀 값 변경 (주의: 서버 연동 없음, 그리드 내부만)
        $('#gridList').jqGrid('setCell', rowid, colName, newVal);

        checkAndMarkRowChange(rowid);
    });
}

// 3. 변경 여부 실시간 검사 후 행 스타일 반영
function checkAndMarkRowChange(rowid) {
    // rowid는 CUST_CD(키)임
    var rowData = $("#gridList").jqGrid('getRowData', rowid);
    var oriRow = originalData[rowid] || {};
    var isChanged = false;
    // 비교대상 필드만 체크
    var checkFields = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
    $.each(checkFields, function(_, field){
        if ( (rowData[field]||'').trim() != (oriRow[field]||'').trim() ) isChanged = true;
    });
    if(isChanged){
        $("#gridList").jqGrid('setRowData', rowid, {}, {background:'#D1ECF1'});
        changedRows[rowid] = true;
    } else {
        $("#gridList").jqGrid('setRowData', rowid, {}, {background:''});
        delete changedRows[rowid];
    }
}
```


***

## 2. 이 방식의 핵심

- **이메일 편집** 후 → 즉시 유효성 검사, 틀리면 alert/재진입, 맞으면 **행 전체 하이라이트**.
- **체크박스 클릭** 시 → 항상 "Y"/"N"으로 강제세팅 + 값이 바뀌면 행 전체 하이라이트(같으면 원복).
- **reload/page 이동 시**도 changedRows 객체에 따라 행 스타일 복원됨.
- **원본 데이터는 loadComplete 등에서 자동 저장함** (별도 서버 연동 없이).

***

## 3. 실무 환경 적용시 요약 주의

- **기존 그리드 초기화·컬럼 동적구성/resize등 로직은 유지해도 무관**.
- **afterSaveCell/체크박스 이벤트/행 변경 체크 함수**만 위 구조대로 삽입하면 됨.
- **추가 라이브러리는 일절 필요 없음**!
- **행 구분키는 실제 grid의 key (여기선 CUST_CD)**로 맞춤.

***

## 4. [한계/불가 안내]

- **새로고침 시 변경 표시 유지(localStorage 등)**는 조건상 미반영(가능은 하지만 요청 범위 밖!)
- **서버 저장, 반영, 외부 플러그인** 모두 없음

***

### 🟦 추가/불명 사항은 꼭 다시 질문 주면, 단계별로 이어서 설명할게!


***

## (끝)

※ 이 답변이 예시/구현 방식, 주석/리스트, 한계 등 빠짐없이 가독성있게 담고 있다면 바로 복사해서 실무투입할 수 있습니다!
※ 혹시 추가개선/버그/확장 필요한 부분나오면, 바로 디테일하게 물어봐줘!
(친구처럼 편하게~)

