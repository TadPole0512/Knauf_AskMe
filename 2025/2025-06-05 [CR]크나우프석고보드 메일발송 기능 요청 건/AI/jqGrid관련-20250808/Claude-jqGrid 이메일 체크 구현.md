**\[환경]**

* jqGrid 4.15, jQuery 3.6, IE11/Chrome 최신 지원
* Spring + JSP
* 외부 플러그인 추가 불가

---

**\[시나리오/구현 목표]**

1. 사용자가 'CUST\_MAIN\_EMAIL' 또는 'SALESREP\_EMAIL' 셀을 수정하면

   * 유효하지 않은 이메일일 경우 경고 및 재입력
   * 유효하면 해당 행 전체 배경색 변경
2. 'CUST\_SENDMAIL\_YN' 또는 'SALESREP\_SENDMAIL\_YN' 체크박스 클릭 시

   * 체크=‘Y’, 해제=‘N’ 값 저장
   * 값이 원래와 다르면 줄 배경색 변경
3. 값/체크박스를 원래대로 돌리면 배경색도 원복
4. 그리드 reload/page 이동해도 변경 표시 유지(가능하다면)
5. 불필요: 서버 반영/자동 저장/외부 플러그인

---

**\[관련 코드/필드]**

```javascript
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
...
```

* afterSaveCell, beforeSelectRow, gridComplete 사용 가능
* 체크박스 값은 반드시 'Y'/'N'

---

**\[요구/조건/우선순위]**

* js/jqGrid만으로 구현, 외부 플러그인 불가
* 안 되는 건 “불가/모름”으로 명확히 알려주길 요청
* 실제 예제 코드와 설명 둘 다 필수!

---

**\[마지막 한마디]**

* 실무에서 바로 쓸 수 있는 코드+로직 예시로 부탁
* 부정확하거나 대충 때우는 답변은 사양

---


안녕! 친구처럼 편하게 답변할게. jqGrid에서 셀 편집 시 행 배경색 변경하는 기능을 구현해보자.

## 구현 방법

jqGrid의 `afterSaveCell` 이벤트를 사용해서 구현할 수 있어. 그런데 먼저 한 가지 제한사항을 말해줄게.

**제한사항**: 그리드 reload나 페이지 이동 후에도 변경 표시를 유지하는 건 **불가능**해. jqGrid는 데이터를 다시 로드하면서 DOM을 완전히 새로 그리기 때문이야.

## 실제 구현 코드

```javascript
// 원본 데이터 저장용 객체
var originalData = {};

// 이메일 유효성 검사 함수
function isValidEmail(email) {
    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// 행 배경색 변경 함수
function updateRowBackground(rowid, isChanged) {
    if (isChanged) {
        $("#gridList").jqGrid('setRowData', rowid, {}, 'changed-row');
    } else {
        $("#gridList").jqGrid('setRowData', rowid, {}, '');
    }
}

// 행의 변경 상태 확인 함수
function checkRowChanged(rowid) {
    var currentData = $("#gridList").jqGrid('getRowData', rowid);
    var original = originalData[rowid];
    
    if (!original) return false;
    
    return (currentData.CUST_MAIN_EMAIL !== original.CUST_MAIN_EMAIL ||
            currentData.SALESREP_EMAIL !== original.SALESREP_EMAIL ||
            currentData.CUST_SENDMAIL_YN !== original.CUST_SENDMAIL_YN ||
            currentData.SALESREP_SENDMAIL_YN !== original.SALESREP_SENDMAIL_YN ||
            currentData.COMMENTS !== original.COMMENTS);
}

function getGridList(){
    var searchData = getSearchData();
    $('#gridList').jqGrid({
        url: "${url}/admin/customer/getOrderEmailAlarmAjax.lime",
        editurl: 'clientArray',
        datatype: "json",
        mtype: 'POST',
        postData: searchData,
        colModel: defaultColModel, // updateComModel -> defaultColModel로 수정
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
        
        // 그리드 로드 완료 후 원본 데이터 저장
        gridComplete: function() {
            var rowIds = $("#gridList").jqGrid('getDataIDs');
            for (var i = 0; i < rowIds.length; i++) {
                var rowid = rowIds[i];
                var rowData = $("#gridList").jqGrid('getRowData', rowid);
                originalData[rowid] = {
                    CUST_MAIN_EMAIL: rowData.CUST_MAIN_EMAIL,
                    SALESREP_EMAIL: rowData.SALESREP_EMAIL,
                    CUST_SENDMAIL_YN: rowData.CUST_SENDMAIL_YN,
                    SALESREP_SENDMAIL_YN: rowData.SALESREP_SENDMAIL_YN,
                    COMMENTS: rowData.COMMENTS
                };
            }
        },
        
        // 셀 편집 완료 후 처리
        afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
            // 이메일 필드 유효성 검사
            if (cellname === 'CUST_MAIN_EMAIL' || cellname === 'SALESREP_EMAIL') {
                if (value && !isValidEmail(value)) {
                    alert('올바른 이메일 형식을 입력해주세요.');
                    // 포커스를 다시 해당 셀로 이동
                    $("#gridList").jqGrid('editCell', iRow + 1, iCol, true);
                    return;
                }
            }
            
            // 체크박스 필드 값 정규화 (jqGrid가 자동으로 처리하지만 확실히 하기 위해)
            if (cellname === 'CUST_SENDMAIL_YN' || cellname === 'SALESREP_SENDMAIL_YN') {
                var normalizedValue = (value === 'Y' || value === true || value === 'true') ? 'Y' : 'N';
                $("#gridList").jqGrid('setCell', rowid, cellname, normalizedValue);
            }
            
            // 행 변경 상태 확인 및 배경색 업데이트
            var isChanged = checkRowChanged(rowid);
            updateRowBackground(rowid, isChanged);
        }
    });
}
```

## CSS 스타일 추가

```css
/* 변경된 행 스타일 */
.changed-row {
    background-color: #fff3cd !important;
}

.changed-row td {
    background-color: #fff3cd !important;
}

/* 체크박스 스타일 (기존 코드에 있던 것) */
.small-checkbox {
    /* 기존 스타일 유지 */
}
```

## 동작 원리 설명

1. **원본 데이터 저장**: `gridComplete` 에서 각 행의 원본 데이터를 `originalData` 객체에 저장
2. **셀 편집 완료 감지**: `afterSaveCell` 이벤트에서 편집 완료를 감지
3. **이메일 유효성 검사**: 이메일 필드 편집 시 정규식으로 유효성 검사
4. **체크박스 값 정규화**: Y/N 값으로 정규화
5. **변경 상태 확인**: 현재 값과 원본 값을 비교해서 변경 여부 확인
6. **배경색 업데이트**: 변경된 경우 CSS 클래스 적용, 원복된 경우 제거

## 주의사항

- **페이지 이동/새로고침 시 변경 표시 초기화**: jqGrid의 특성상 불가능
- **체크박스 클릭**: jqGrid가 자동으로 Y/N 값을 토글함
- **IE11 호환성**: 사용한 모든 코드는 IE11에서 동작

이 코드를 그대로 사용하면 요구사항대로 동작할 거야. 혹시 안 되는 부분이 있으면 알려줘!


---------------------------------------------------------------------



위의 질문에서 [관련 코드/필드] 부분만 아래의 코드로 변경해서 다시 정리해줘.
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


알겠어! 전체 코드를 보니까 구조를 더 정확히 파악했어. 기존 코드에 맞춰서 셀 편집 시 행 배경색 변경 기능을 구현해볼게.

## 구현 방법

기존 `loadComplete`와 `afterSaveCell` 이벤트를 활용해서 구현할 수 있어. CSS도 이미 정의되어 있으니 바로 사용하면 돼.

## 실제 구현 코드

### 1. 유틸리티 함수들 추가 (상단에 추가)

```javascript
// 이메일 유효성 검사 함수
function isValidEmail(email) {
    if (!email || email.trim() === '') return true; // 빈 값은 허용
    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email.trim());
}

// 행 배경색 변경 함수
function updateRowBackground(rowid, isChanged) {
    if (isChanged) {
        $("#gridList").jqGrid('setRowData', rowid, {}, 'changed-row');
    } else {
        $("#gridList").jqGrid('setRowData', rowid, {}, '');
    }
}

// 행의 변경 상태 확인 함수
function checkRowChanged(rowid) {
    var currentData = $("#gridList").jqGrid('getRowData', rowid);
    var original = originalData[rowid];
    
    if (!original) return false;
    
    // 체크박스 값 정규화 (jqGrid에서 체크박스가 다양한 형태로 올 수 있음)
    var currentCustSendmail = (currentData.CUST_SENDMAIL_YN === 'Y' || currentData.CUST_SENDMAIL_YN === true || currentData.CUST_SENDMAIL_YN === 'true') ? 'Y' : 'N';
    var currentSalesrepSendmail = (currentData.SALESREP_SENDMAIL_YN === 'Y' || currentData.SALESREP_SENDMAIL_YN === true || currentData.SALESREP_SENDMAIL_YN === 'true') ? 'Y' : 'N';
    
    var originalCustSendmail = (original.CUST_SENDMAIL_YN === 'Y' || original.CUST_SENDMAIL_YN === true || original.CUST_SENDMAIL_YN === 'true') ? 'Y' : 'N';
    var originalSalesrepSendmail = (original.SALESREP_SENDMAIL_YN === 'Y' || original.SALESREP_SENDMAIL_YN === true || original.SALESREP_SENDMAIL_YN === 'true') ? 'Y' : 'N';
    
    return (currentData.CUST_MAIN_EMAIL !== original.CUST_MAIN_EMAIL ||
            currentData.SALESREP_EMAIL !== original.SALESREP_EMAIL ||
            currentCustSendmail !== originalCustSendmail ||
            currentSalesrepSendmail !== originalSalesrepSendmail ||
            currentData.COMMENTS !== original.COMMENTS);
}
```

### 2. loadComplete 수정

기존 `loadComplete` 함수를 다음과 같이 수정해:

```javascript
loadComplete: function(data) {
    originalData = {};
    if (data.list && data.list.length > 0) {
        data.list.forEach(function(row) {
            originalData[row.CUST_CD] = $.extend({}, row);
        });
    }
    
    $('#listTotalCountSpanId').html(addComma(data.listTotalCount));
    $('.ui-pg-input').val(data.page);
},
```

### 3. afterSaveCell 구현

기존 빈 `afterSaveCell` 함수를 다음과 같이 수정해:

```javascript
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    // 이메일 필드 유효성 검사
    if (cellname === 'CUST_MAIN_EMAIL' || cellname === 'SALESREP_EMAIL') {
        if (value && !isValidEmail(value)) {
            alert('올바른 이메일 형식을 입력해주세요.');
            // 해당 셀을 다시 편집 모드로 전환
            setTimeout(function() {
                $("#gridList").jqGrid('editCell', iRow + 1, iCol, true);
            }, 100);
            return;
        }
    }
    
    // 체크박스 필드 값 정규화
    if (cellname === 'CUST_SENDMAIL_YN' || cellname === 'SALESREP_SENDMAIL_YN') {
        var normalizedValue = (value === 'Y' || value === true || value === 'true') ? 'Y' : 'N';
        $("#gridList").jqGrid('setCell', rowid, cellname, normalizedValue);
    }
    
    // 행 변경 상태 확인 및 배경색 업데이트
    setTimeout(function() {
        var isChanged = checkRowChanged(rowid);
        updateRowBackground(rowid, isChanged);
    }, 50); // 약간의 딜레이를 줘서 값이 완전히 저장된 후 체크
},
```

### 4. CSS 수정 (기존 것 업데이트)

```css
.changed-row {
    background-color: #D1ECF1 !important;
}

.changed-row td {
    background-color: #D1ECF1 !important;
}

.small-checkbox input[type="checkbox"] {
    width: 16px;
    height: 16px;
}
```

## 전체 수정된 부분

### 1. 스크립트 상단에 함수 추가 ($(function() 위에)

```javascript
// 이메일 유효성 검사 함수
function isValidEmail(email) {
    if (!email || email.trim() === '') return true;
    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email.trim());
}

// 행 배경색 변경 함수
function updateRowBackground(rowid, isChanged) {
    if (isChanged) {
        $("#gridList").jqGrid('setRowData', rowid, {}, 'changed-row');
    } else {
        $("#gridList").jqGrid('setRowData', rowid, {}, '');
    }
}

// 행의 변경 상태 확인 함수
function checkRowChanged(rowid) {
    var currentData = $("#gridList").jqGrid('getRowData', rowid);
    var original = originalData[rowid];
    
    if (!original) return false;
    
    var currentCustSendmail = (currentData.CUST_SENDMAIL_YN === 'Y' || currentData.CUST_SENDMAIL_YN === true || currentData.CUST_SENDMAIL_YN === 'true') ? 'Y' : 'N';
    var currentSalesrepSendmail = (currentData.SALESREP_SENDMAIL_YN === 'Y' || currentData.SALESREP_SENDMAIL_YN === true || currentData.SALESREP_SENDMAIL_YN === 'true') ? 'Y' : 'N';
    
    var originalCustSendmail = (original.CUST_SENDMAIL_YN === 'Y' || original.CUST_SENDMAIL_YN === true || original.CUST_SENDMAIL_YN === 'true') ? 'Y' : 'N';
    var originalSalesrepSendmail = (original.SALESREP_SENDMAIL_YN === 'Y' || original.SALESREP_SENDMAIL_YN === true || original.SALESREP_SENDMAIL_YN === 'true') ? 'Y' : 'N';
    
    return (currentData.CUST_MAIN_EMAIL !== original.CUST_MAIN_EMAIL ||
            currentData.SALESREP_EMAIL !== original.SALESREP_EMAIL ||
            currentCustSendmail !== originalCustSendmail ||
            currentSalesrepSendmail !== originalSalesrepSendmail ||
            currentData.COMMENTS !== original.COMMENTS);
}
```

### 2. afterSaveCell 수정

```javascript
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    if (cellname === 'CUST_MAIN_EMAIL' || cellname === 'SALESREP_EMAIL') {
        if (value && !isValidEmail(value)) {
            alert('올바른 이메일 형식을 입력해주세요.');
            setTimeout(function() {
                $("#gridList").jqGrid('editCell', iRow + 1, iCol, true);
            }, 100);
            return;
        }
    }
    
    if (cellname === 'CUST_SENDMAIL_YN' || cellname === 'SALESREP_SENDMAIL_YN') {
        var normalizedValue = (value === 'Y' || value === true || value === 'true') ? 'Y' : 'N';
        $("#gridList").jqGrid('setCell', rowid, cellname, normalizedValue);
    }
    
    setTimeout(function() {
        var isChanged = checkRowChanged(rowid);
        updateRowBackground(rowid, isChanged);
    }, 50);
},
```

## 동작 설명

1. **원본 데이터 저장**: `loadComplete`에서 각 행의 원본 데이터를 `originalData`에 저장 (기존 코드 활용)
2. **이메일 유효성 검사**: 이메일 필드 편집 시 정규식으로 검사, 잘못된 경우 경고 후 재편집
3. **체크박스 값 정규화**: Y/N으로 값 통일
4. **변경 감지**: 현재 값과 원본 값을 비교해서 변경 여부 확인
5. **배경색 적용**: 변경된 경우 `.changed-row` 클래스 적용

## 제한사항

- **페이지 이동/새로고침 시 변경 표시 초기화**: jqGrid 특성상 불가능
- **그리드 reload 시**: `originalData`가 새로 설정되므로 변경 표시 사라짐

기존 코드 구조를 최대한 유지하면서 구현했으니까, 이 코드를 그대로 적용하면 될 거야!
