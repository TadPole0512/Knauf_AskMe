아래 1번과 2번의 내용을 3번 소스에 적용해줘.(THINK HARD)
3번은 출고지 관리라는 페이지 소스야. 이 소스에 1번과 2번의 내용에 필요없는 소스는 지우고, 1,2번 소스에 필요한 내용의 소스만 남겨줘
 
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

<script type="text/javascript">
//Start. Setting Jqgrid Columns Order.
var ckNameJqGrid = 'admin/system/plantConfig/jqGridCookie'; // 페이지별 쿠키명 설정. // ####### 설정 #######
ckNameJqGrid += '/gridList'; // 그리드명별 쿠키명 설정. // ####### 설정 #######

var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
var globalColumnOrder = globalColumnOrderStr.split(',');

var defaultColModel = [ //  ####### 설정 #######
	{name:"PT_USE", label:'상태', width:100, align:'center', sortable:true, editable:true, edittype:'select', editoptions:{value:'Y:Y;N:N'} },
	{name:"PT_SORT", label:'순서', width:100, align:'center', sortable:true, editable:true, editoptions:{dataInit:initJqGridAutoNumeric(aNNumberOption, 'left'), dataEvents:[{type:'keyup', fn:function(e){checkByte(this, '5');}}]} },
	{name:"PT_CODE", key:true, label:'코드', width:150, align:'center', sortable:true, formatter:setPtCode },
	{name:"PT_NAME", label:'출고지명', width:200, align:'left', sortable:true, editable:true, editoptions:{dataEvents:[{type:'keyup', fn:function(e){checkByte(this, '50');}}]} },
	{name:"PT_ZONECODE", label:'우편번호', width:100, align:'center', sortable:false, editable:true, edittype:'custom', editoptions:{custom_element:editCellElem1} },
	{name:"PT_ADDR1", label:'주소', width:370, align:'left', sortable:false, editable:true, edittype:'custom', editoptions:{custom_element:editCellElem2} },
	{name:"PT_ADDR2", label:'상세주소', width:250, align:'left', sortable:false, editable:true, editoptions:{dataEvents:[{type:'keyup', fn:function(e){checkByte(this, '100');}}]} },
	{name:"PT_TEL", label:'연락처', width:150, align:'center', sortable:false, editable:true, editoptions:{dataInit:initJqGridAutoNumeric(aNNumberOption, 'left'), dataEvents:[{type:'keyup', fn:function(e){checkByte(this, '11');}}]} },
	{name:"PT_INDATE", label:'등록일', width:180, align:'center', sortable:true, formatter:setInDateFormat },
	//{name:"PT_INDATE", label:'등록일', width:20, align:'center', sortable:true, formatter:'date', formatoptions:{newformat:'Y-m-d H:i'} }, // 시분 까지 노출해야 한다면 boral은 formatoptions 사용하면 안됨.
	{name:"PT_ZIPCODE", hidden:true },
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

$(document).ready(function() {
	
});

function getGridList(){
	// grid init
	var searchData = getSearchData();
	$('#gridList').jqGrid({
		url: "${url}/admin/system/plantListAjax.lime",
		editurl: 'clientArray', //사용x
		//editurl: './deliveryspotUpAjax.lime',
		datatype: "json",
		mtype: 'POST',
		postData: searchData,
		colModel: updateComModel,
		height: '360px',
		autowidth: false,
		multiselect: true,
		rownumbers: true,
		pagination: true,
		pager: "#pager",
		actions : true,
		pginput : true,
		//sortable: true,
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
			//$('#gridList').getGridParam("reccount"); // 현재 페이지에 뿌려지는 row 개수
			//$('#gridList').getGridParam("records"); // 현재 페이지에 limitrow
			$('#listTotalCountSpanId').html(addComma(data.listTotalCount));
		},
		onSelectRow: function(rowId){
			var h_dscode = $('#gridList').find('#'+rowId).find('input[name="h_dscode"]').val();
			if('' != h_dscode){ //editRow
				editRow(rowId);
			}
		},
		//onSelectRow: editRow,
		onSelectAll: function(rowIdArr, status) { //전체 체크박스 선택했을때 onSelectRow가 실행이 안되고 onSelectAll 실행되네...
			//console.log('status : ', status); //status : true=전체선택했을때, false=전체해제했을때
			//console.log('rowIdArr : ', rowIdArr); //rowid 배열 타입
			//console.log('rowIdArr.length : ', rowIdArr.length);
			if(status){
				$.each(rowIdArr, function(i,e){
					var h_dscode = $('#gridList').find('#'+e).find('input[name="h_dscode"]').val();
					if('' != h_dscode){ //editRow
						editRow(e);
					}
				});
			}
		}
		/* 
		beforeProcessing: function(data, status, xhr){ // 서버로 부터 데이터를 받은 후 화면에 찍기 위한 processing을 진행하기 직전에 호출.
			if('0000' != data.RES_CODE){
				alert(data.RES_MSG);
				return false;
			}
		},
		*/
	});
}

var lastSelection;
// 행 편집.
function editRow(id){
	//alert('id : '+id+'\nlastSelection : '+lastSelection);
    if (id && id !== lastSelection) {
        var grid = $('#gridList');
		//grid.jqGrid('restoreRow',lastSelection); //이전에 선택한 행 제어
        grid.jqGrid('editRow',id, {keys: false}); //keys true=enter
        lastSelection = id;
    }
}

// 행 추가.
function addRow() {
	var rowData = {PT_CODE:'', PT_USE:''};
	var rowId = $('#gridList').getGridParam("records")+1; //페이징 처리 시 현 페이지의 Max RowId 값+1
	$('#gridList').jqGrid('addRow', {initdata:rowData, position :'first'}); //addRow : onSelectRow 실행하네...
}

// 세팅. 출고지 코드 인풋.
function setPtCode(cellVal, options, rowObj){
	if('' == toStr(cellVal)){ // 등록 rowObj.PT_CODE 
		return '<input type="text" name="M_PT_CODE" value="" onkeyup=\'checkByte(this, "10");\' />';
	}
	else{ // 수정
		return '<input type="hidden" name="R_PT_CODE" value="'+cellVal+'" readonly="readonly" />'+cellVal;
	}
}

// 세팅. 출고지 등록일자.
function setInDateFormat(cellVal, options, rowObj){
	if('' == toStr(rowObj.PT_CODE )){ // 등록 
		return '자동생성';
	}
	else{ // 수정
		return ('' == toStr(cellVal)) ? '-' : toStr(cellVal).substring(0,16);
	}
}

// 우편번호 Input : editable column > editionoptions > custom_element
function editCellElem1(value, options){
	var rowId = options.id.split('_')[0]; // rowid 가져올 방법이 이것밖에...
	//console.log('custom_element : ', value+'\t options.name : '+options.name+'\t options.id'+options.id+'\t options'+options+'\t rowId'+rowId);
	
	var retTxt = '<input type="text" id="'+options.id+'" name="'+options.name+'" value="'+value+'" onclick=\'openPostPopById("'+rowId+'_PT_ZONECODE", "'+rowId+'_PT_ADDR1", "'+rowId+'_PT_ADDR2", "'+rowId+'_PT_ZIPCODE");\' onkeyup=\'checkByte(this, "7");\' />';
	return retTxt;
}

// 주소 Input : editable column > editionoptions > custom_element
function editCellElem2(value, options){
	var rowId = options.id.split('_')[0]; // rowid 가져올 방법이 이것밖에...
	//console.log('custom_element : ', value+'\t options.name : '+options.name+'\t options.id'+options.id+'\t options'+options+'\t rowId'+rowId);
	
	var retTxt = '<input type="text" class="editable" style="width: 98%;" id="'+options.id+'" name="'+options.name+'" value="'+value+'" onkeyup=\'checkByte(this, "100");\' />';
	//var retTxt = '<input type="text" class="editable" style="width: 98%;" id="'+options.id+'" name="'+options.name+'" value="'+value+'" onclick=\'openPostPopById("'+rowId+'_PT_ZONECODE", "'+rowId+'_PT_ADDR1", "'+rowId+'_PT_ADDR2", "'+rowId+'_PT_ZIPCODE");\' onkeyup=\'checkByte(this, "100");\' />';
	return retTxt;
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
	var rl_ptcode = $('input[name="rl_ptcode"]').val();
	var rl_ptname = $('input[name="rl_ptname"]').val();
	var sData = {
		rl_ptcode : rl_ptcode
		, rl_ptname : rl_ptname 
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

// 엑셀다운로드.
// jqgrid 검색 조건 증 체크박스 주의.
function excelDown(obj){
	$('#ajax_indicator').show().fadeIn('fast');
	var token = getFileToken('excel');
	$('form[name="frm"]').append('<input type="hidden" name="filetoken" value="'+token+'" />');
	
	formPostSubmit('frm', '${url}/admin/system/plantConfigExcelDown.lime');
	$('form[name="frm"]').attr('action', '');
	
	$('input[name="filetoken"]').remove();
	var fileTimer = setInterval(function() {
		//console.log('token : ', token);
        console.log("cookie : ", getCookie(token));
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
					출고지(사업장) 관리
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
												<label class="search-h">출고지코드</label>
												<div class="search-c">
													<input type="text" class="search-input" name="rl_ptcode" value="${param.rl_ptcode}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
												</div>
											</li>
											<li>
												<label class="search-h">출고지명</label>
												<div class="search-c">
													<input type="text" class="search-input" name="rl_ptname" value="${param.rl_ptname}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
												</div>
											</li>
										</ul>
									</div>
								</div>
							</div>
							
							<div class="panel-body">
								<h5 class="table-title listT">TOTAL <span id="listTotalCountSpanId">0</span>EA</h5>
								<div class="btnList writeObjectClass">
									<button type="button" class="btn btn-warning" onclick="addRow();">추가</button>
									<button type="button" class="btn btn-info" onclick="dataInUp(this, '');">저장</button>
								</div>
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
---------------------------------------------------------------------
---------------------------------------------------------------------

```html
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

<!-- 필요 라이브러리만 남김 -->
<script type="text/javascript" src="${url}/include/js/common/select2/select2.js"></script>
<link rel="stylesheet" href="${url}/include/js/common/select2/select2.css" />

<style>
  /* 수정표시 배경 */
  .modified-row { background-color: #fff8e1 !important; }
  .error-cell { outline: 2px solid #e53935 !important; }
  .tableSearch .topSearch ul { list-style:none; padding:0; margin:0; display:flex; gap:16px; flex-wrap:wrap; }
  .tableSearch .topSearch li { display:flex; align-items:center; gap:8px; }
  .search-h { min-width:88px; font-weight:600; }
  .search-input { height:28px; padding:0 8px; }
</style>

<script type="text/javascript">
// ====== 주문메일알람 전용 스크립트 ======
var modifiedRows = new Set();            // 수정된 rowId 수집
var originalRows = {};                   // 원본 데이터 보관 (rowId -> rowData)

// 이메일 정규식 (간결형)
function isValidEmail(v){
    if(!v) return false;
    var re = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    return re.test(v);
}

// 값 비교용 헬퍼
function rowEquals(a, b){
    if(!a || !b) return false;
    return (
        toStr(a.CUST_MAIN_EMAIL) === toStr(b.CUST_MAIN_EMAIL) &&
        toStr(a.SALESREP_EMAIL)   === toStr(b.SALESREP_EMAIL) &&
        toStr(a.COMMENTS)         === toStr(b.COMMENTS) &&
        toStr(a.CUST_SENDMAIL_YN) === toStr(b.CUST_SENDMAIL_YN) &&
        toStr(a.SALESREP_SENDMAIL_YN) === toStr(b.SALESREP_SENDMAIL_YN)
    );
}

// 문자열 안전 변환
function toStr(v){ return (v === undefined || v === null) ? '' : (v+'' ); }

$(function(){
    buildGrid();
});

function getSearchData(){
    return {
        rl_custcd:   $('input[name="rl_custcd"]').val(),
        rl_custnm:   $('input[name="rl_custnm"]').val(),
        rl_salesrep: $('input[name="rl_salesrep"]').val()
    };
}

function buildGrid(){
    var colModel = [
        { name: "CUST_CD", key:true, label: '거래처코드', width: 120, align:'center', sortable:true },
        { name: "CUST_NM", label: '거래처명', width: 220, align:'left', sortable:true },
        { name: "CUST_MAIN_EMAIL", label: '담당자 이메일', width: 220, align:'center', sortable:true, editable:true },
        { name: "CUST_SENDMAIL_YN", label: '발송 여부', width: 100, align:'center', sortable:true, editable:true,
          edittype:'checkbox', formatter:'checkbox', editoptions:{ value:'Y:N' } },
        { name: "SALESREP_NM", label: '영업 담당', width: 120, align:'center', sortable:true },
        { name: "SALESREP_EMAIL", label: '영업 담당 이메일', width: 260, align:'center', sortable:true, editable:true },
        { name: "SALESREP_SENDMAIL_YN", label: '발송 여부', width: 100, align:'center', sortable:true, editable:true,
          edittype:'checkbox', formatter:'checkbox', editoptions:{ value:'Y:N' } },
        { name: "COMMENTS", label: '비고', width: 360, align:'left', sortable:true, editable:true }
    ];

    $('#gridList').jqGrid({
        url: "${url}/admin/system/orderMailAlarm/listAjax.lime",
        datatype: "json",
        mtype: 'POST',
        postData: getSearchData(),
        colModel: colModel,
        height: 360,
        autowidth: true,
        shrinkToFit: false,
        rownumbers: true,
        pager: "#pager",
        rowNum: 50,
        rowList: [50,100,200],
        viewrecords: true,
        cellEdit: true,
        cellsubmit: 'clientArray',
        jsonReader: { root:'list' },
        loadComplete: function(data){
            // 원본 스냅샷 저장
            var ids = $(this).jqGrid('getDataIDs');
            originalRows = {}; modifiedRows.clear();
            for(var i=0;i<ids.length;i++){
                var r = $(this).jqGrid('getRowData', ids[i]);
                // 체크박스 포맷터는 on/off 형태일 수 있어 표준화
                r.CUST_SENDMAIL_YN = normalizeYN(r.CUST_SENDMAIL_YN);
                r.SALESREP_SENDMAIL_YN = normalizeYN(r.SALESREP_SENDMAIL_YN);
                originalRows[ids[i]] = $.extend(true, {}, r);
                $('#'+ids[i]).removeClass('modified-row');
            }
            $('#listTotalCountSpanId').text((data && data.listTotalCount) ? data.listTotalCount : ids.length);
        },
        afterSaveCell: function(rowid, cellname, value, iRow, iCol){
            // 이메일·비고 변경시 수정 상태 반영 & 이메일 즉시 유효성 체크
            trackChange(rowid);
            if(cellname === 'CUST_MAIN_EMAIL' || cellname === 'SALESREP_EMAIL'){
                validateEmailCell(rowid, cellname);
            }
        },
        beforeCheckValues: function(postdata, formid, mode){
            // 체크박스 편집 시 Y/N 일관화
            if(postdata.hasOwnProperty('CUST_SENDMAIL_YN'))
                postdata.CUST_SENDMAIL_YN = normalizeYN(postdata.CUST_SENDMAIL_YN);
            if(postdata.hasOwnProperty('SALESREP_SENDMAIL_YN'))
                postdata.SALESREP_SENDMAIL_YN = normalizeYN(postdata.SALESREP_SENDMAIL_YN);
            return [true,""];
        },
        afterEditCell: function(rowid, cellname, value, iRow, iCol){
            // 체크박스는 클릭 즉시 토글되므로 change 감지용
            if(cellname === 'CUST_SENDMAIL_YN' || cellname === 'SALESREP_SENDMAIL_YN'){
                var $checkbox = $('input,select,textarea', this);
                $checkbox.off('change.__yn').on('change.__yn', function(){
                    setTimeout(function(){ trackChange(rowid); }, 0);
                });
            }
        }
    });
}

function normalizeYN(v){
    var s = toStr(v).toUpperCase();
    if(s === 'YES' || s === 'ON' || s === 'TRUE' || s === '1') return 'Y';
    if(s === 'NO' || s === 'OFF' || s === 'FALSE' || s === '0') return 'N';
    return (s === 'Y' || s === 'N') ? s : 'N';
}

function trackChange(rowid){
    // 현재 값과 원본 비교하여 수정 플래그/스타일 유지
    var now = $('#gridList').jqGrid('getRowData', rowid);
    now.CUST_SENDMAIL_YN = normalizeYN(now.CUST_SENDMAIL_YN);
    now.SALESREP_SENDMAIL_YN = normalizeYN(now.SALESREP_SENDMAIL_YN);
    if(!rowEquals(now, originalRows[rowid])){
        modifiedRows.add(rowid);
        $('#'+rowid).addClass('modified-row');
    }else{
        modifiedRows.delete(rowid);
        $('#'+rowid).removeClass('modified-row');
    }
}

function validateEmailCell(rowid, cellname){
    var v = toStr($('#gridList').jqGrid('getCell', rowid, cellname));
    var cellSelector = '#'+rowid+' td[aria-describedby="gridList_'+cellname+'"]';
    if(v && !isValidEmail(v)){
        $(cellSelector).addClass('error-cell');
        return false;
    } else {
        $(cellSelector).removeClass('error-cell');
        return true;
    }
}

// 조회
function dataSearch(){
    $('#gridList').setGridParam({ postData: getSearchData(), page:1 }).trigger('reloadGrid');
}

// 저장 (수정행만 서버로 전송)
function saveChanges(btn){
    $(btn).prop('disabled', true);

    if(modifiedRows.size === 0){
        alert('수정된 내용이 없습니다.');
        $(btn).prop('disabled', false);
        return;
    }

    var invalidMsg = [];
    var payload = [];

    modifiedRows.forEach(function(rowid){
        // 우선 셀 저장(포커스 셀 편집 중일 수 있음)
        $('#gridList').jqGrid('saveCell', rowid);

        var r = $('#gridList').jqGrid('getRowData', rowid);
        // 표준화
        r.CUST_SENDMAIL_YN = normalizeYN(r.CUST_SENDMAIL_YN);
        r.SALESREP_SENDMAIL_YN = normalizeYN(r.SALESREP_SENDMAIL_YN);

        var email1 = toStr(r.CUST_MAIN_EMAIL);
        var email2 = toStr(r.SALESREP_EMAIL);

        // 메일 형식 유효성
        if(email1 && !isValidEmail(email1)) invalidMsg.push('[row '+rowid+'] 담당자 이메일 형식이 올바르지 않습니다.');
        if(email2 && !isValidEmail(email2)) invalidMsg.push('[row '+rowid+'] 영업 담당 이메일 형식이 올바르지 않습니다.');

        // 발송 체크 시 이메일 필수
        if(r.CUST_SENDMAIL_YN === 'Y' && !email1) invalidMsg.push('[row '+rowid+'] 담당자 발송 여부가 Y면 담당자 이메일이 필요합니다.');
        if(r.SALESREP_SENDMAIL_YN === 'Y' && !email2) invalidMsg.push('[row '+rowid+'] 영업 담당 발송 여부가 Y면 영업 담당 이메일이 필요합니다.');

        payload.push({
            CUST_CD: r.CUST_CD,
            CUST_MAIN_EMAIL: email1,
            CUST_SENDMAIL_YN: r.CUST_SENDMAIL_YN,
            SALESREP_EMAIL: email2,
            SALESREP_SENDMAIL_YN: r.SALESREP_SENDMAIL_YN,
            COMMENTS: toStr(r.COMMENTS)
        });
    });

    if(invalidMsg.length>0){
        alert(invalidMsg.join('\n'));
        $(btn).prop('disabled', false);
        return;
    }

    if(!confirm('저장하시겠습니까?')){ $(btn).prop('disabled', false); return; }

    $.ajax({
        url: "${url}/admin/system/orderMailAlarm/saveAjax.lime",
        type: 'POST',
        data: { rows: JSON.stringify(payload) },
        success: function(res){
            // 기대 응답: { RES_CODE:'0000', RES_MSG:'저장되었습니다.' }
            if(res && res.RES_CODE === '0000'){
                alert(res.RES_MSG || '저장되었습니다.');
                dataSearch();
            }else{
                alert((res && res.RES_MSG) ? res.RES_MSG : '저장에 실패했습니다.');
            }
        },
        error: function(){ alert('통신 오류가 발생했습니다.'); },
        complete: function(){ $(btn).prop('disabled', false); }
    });
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

    <form name="frm" method="post">
      <div class="page-inner">
        <div class="page-title">
          <h3>
            주문메일알람 관리
            <div class="page-right">
              <button type="button" class="btn btn-line f-black" title="검색" onclick="dataSearch();"><i class="fa fa-search"></i><em>검색</em></button>
              <button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();"><i class="fa fa-refresh"></i><em>새로고침</em></button>
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
                            <input type="text" class="search-input" name="rl_custcd" value="${param.rl_custcd}" onkeypress="if(event.keyCode==13){dataSearch();}" />
                          </div>
                        </li>
                        <li>
                          <label class="search-h">거래처명</label>
                          <div class="search-c">
                            <input type="text" class="search-input" name="rl_custnm" value="${param.rl_custnm}" onkeypress="if(event.keyCode==13){dataSearch();}" />
                          </div>
                        </li>
                        <li>
                          <label class="search-h">영업담당</label>
                          <div class="search-c">
                            <input type="text" class="search-input" name="rl_salesrep" value="${param.rl_salesrep}" onkeypress="if(event.keyCode==13){dataSearch();}" />
                          </div>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>

                <div class="panel-body">
                  <h5 class="table-title listT">TOTAL <span id="listTotalCountSpanId">0</span>EA</h5>
                  <div class="btnList writeObjectClass">
                    <button type="button" class="btn btn-info" onclick="saveChanges(this);">저장</button>
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

좋아, 1번·2번 요구사항만 남기고 3번(출고지 관리) 소스를 “주문메일알람” 전용으로 전면 정리했어. 코드 전체는 우측 캔버스 \*\*\[주문메일알람\_페이지\_정리본.jsp]\*\*에 넣어뒀다. 거기 있는 걸 바로 붙여 넣어 쓰면 된다. 핵심 요약 먼저 줄게.

# 적용/정리 결과 요약

* 불필요한 출고지(PT\_\*) 전용 컬럼·함수·쿠키 기반 컬럼순서/폭 저장, 우편번호 팝업, 엑셀다운로드 등 **모두 제거**.
* 검색조건을 **거래처코드, 거래처명, 영업담당** 3가지로 축소.
* jqGrid 컬럼을 요구사항대로 구성:

  * `CUST_CD, CUST_NM, CUST_MAIN_EMAIL(편집), CUST_SENDMAIL_YN(체크박스), SALESREP_NM, SALESREP_EMAIL(편집), SALESREP_SENDMAIL_YN(체크박스), COMMENTS(편집)`
* **cellEdit 모드**로 간편 편집; 체크박스는 `formatter:'checkbox'`, `edittype:'checkbox'`, `value:'Y:N'`.
* 사용자가 셀 변경/체크 토글하면 **해당 행 배경색(노란색) 표시** 및 **수정행 Set**에 관리. 원상복구 시 자동 해제.
* 저장 시 **수정된 행만** 수집하여 서버에 전송(JSON).
* **유효성 검사**:

  * 수정행 없으면 “수정된 내용이 없습니다.” 경고.
  * 이메일 형식 검사(두 필드 모두).
  * 발송여부(Y)인 경우 해당 이메일 필수.
  * 잘못된 셀은 빨간 테두리 표시.
* 엔드포인트

  * 조회: `${url}/admin/system/orderMailAlarm/listAjax.lime`
  * 저장: `${url}/admin/system/orderMailAlarm/saveAjax.lime`
  * 필요 시 컨트롤러에 맞게 바꿔줘.

# 백엔드 연동 포맷(예시)

저장은 `rows` 파라미터에 JSON 문자열 배열로 넘어가도록 했어.

```json
[
  {
    "CUST_CD": "C0001",
    "CUST_MAIN_EMAIL": "a@b.com",
    "CUST_SENDMAIL_YN": "Y",
    "SALESREP_EMAIL": "rep@x.com",
    "SALESREP_SENDMAIL_YN": "N",
    "COMMENTS": "비고"
  }
]
```

컨트롤러에서 `rows` 받으면 파싱해서 배치 업데이트하면 끝.

---

# 내가 이렇게 생각하고 만든 근거 (THINK HARD)

## 1) Genius Insight Formula

* 관찰(O)=9: 기존 소스에서 출고지 관리 기능·쿠키 기반 컬럼순서/폭·우편번호 커스텀 에디터 등 **주문메일알람과 무관한 요소**를 식별.
* 연결(C)=9: 요구 컬럼/동작을 jqGrid의 `cellEdit`, `checkbox formatter`, afterSaveCell 훅과 **수정행 트래킹**으로 자연스럽게 연결.
* 패턴(P)=8: “수정행만 저장 + 클라이언트 유효성 + 행 하이라이트” 패턴을 재사용.
* 종합(S)=9: 최소 코드로 최대 요구를 충족하도록 리팩토링.
* 고정관념(A)+편향(B)=2+2로 최소화.
  \=> GI = (9×9×8×9)/(2+2) = 145.8

## 2) Multidimensional Analysis (MDA)

* D1(시간): 과거(출고지), 현재(메일알람), 미래(유지보수) → 단순화/분리 가중치↑
* D3(추상): 데이터-UI-검증 계층 나눠 설계
* D4(인과): “발송여부=Y → 이메일 필수”를 저장 전 검증으로 배치
* D5(계층): Grid 셀 → 행 → 페이로드 → API
  핵심 가중치(D4,D5)에 높게 반영해 설계.

## 3) Creative Connection Matrix

* A∩B: jqGrid 기본 기능과 요구 검증 로직의 교집합(checkbox, cellEdit)
* A⊕B: 기존 출고지 특수 로직 제거
* f(A→B): 수정행 Set, 원본 스냅샷 비교로 실사용에 맞춘 전송 방식

## 4) Problem Redefinition

* θ=180°: “출고지 유지보수” 관점에서 벗어나 **메일알람 전용**으로 재구성
* φ=0.5x: UI 요소를 핵심만 남겨 절반 이하로 압축
* ψ=+1: 데이터 유효성 규칙을 **도메인 규칙**으로 격상하여 저장 파이프라인 앞단에 배치

## 5) Innovative Solutions

* 조합: `cellEdit + checkbox formatter + 원본스냅샷 + Set`
* 실행가능성/가치 높음: 프런트만으로도 즉시 적용 가능, 서버는 단일 엔드포인트

## 6) Insight Amplification

* “왜 셀 편집?” → 행 편집보다 UX 빠름
* “왜 수정행만?” → 트래픽/경합 최소화
* “왜 즉시검사?” → 사용자 오류 조기 차단

---

필요하면 저장 API 파라미터나 응답 스펙에 맞게 프런트 부분을 바로 조정해줄게. 백엔드 컨트롤러/SQL 바인딩 샘플도 원하면 이어서 만들어줄게.
