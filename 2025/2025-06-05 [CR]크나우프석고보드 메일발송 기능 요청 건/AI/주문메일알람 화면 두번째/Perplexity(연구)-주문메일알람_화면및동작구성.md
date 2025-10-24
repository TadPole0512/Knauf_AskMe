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

좋은 요청이야 👍  
정리하자면 지금 주신 **3번 소스(출고지 관리)**를, 1번/2번의 **주문 메일 알람 기능**에 맞게 리팩토링해서  
필요없는 소스(출고지 관리 관련 부분)들은 모두 제거하고, 1번/2번에서 정의한 **조회조건 + jqGrid 컬럼 + 동작**만 살려달라는 거지?  

***

## 🔎 분석 (THINK HARD)
- **1번/2번 요구사항**
  - 조회조건: 거래처코드, 거래처명, 영업담당
  - jqGrid 컬럼: 거래처코드, 거래처명, 담당자, 담당자 이메일, 담당자 발송 여부, 영업담당, 영업담당 이메일, 영업담당 발송 여부, 비고
  - 동작:  
    - CUST_MAIN_EMAIL / SALESREP_EMAIL / 비고 수정 가능 (+ 이메일 유효성 검사)  
    - 발송여부 체크박스 토글 → 변경된 경우 행 색상 변경  
    - 저장 시 유효성 검사 로직 필요(발송여부 체크되어 있으면 이메일 값 필수)

- **3번 소스(출고지 관리)**  
  - 출고지 코드, 주소, 연락처 등 불필요한 컬럼이 많음  
  - 쿠키 기반 컬럼 순서, 리사이즈, 엑셀다운, 신규 추가 행 추가 등등 ⇒ 대부분 불필요  
  - jqGrid 기본 구조는 재사용 가능  
  - 조회조건, 그리드, 저장/조회/엑셀 함수는 주문메일알람 요구에 맞춰 다시 설계

즉 → **필요한 jqGrid + 조건 검색 + 저장 함수만 남기고 나머지 싹 정리**해야 함.

***

## ✅ 리팩토링된 결과 (주문 메일 알람)

```html
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

<script type="text/javascript">

// jqGrid 컬럼 정의 (주문메일 알람)
var defaultColModel = [
    { name: "CUST_CD", key: true, label: '거래처코드', width: 120, align: 'center', sortable: true },
    { name: "CUST_NM", label: '거래처명', width: 220, align: 'left', sortable: true },
    { name: "CONTACT_NM", label: '담당자', width: 120, align: 'center', sortable: true },
    { name: "CUST_MAIN_EMAIL", label: '담당자 이메일', width: 220, align: 'center', editable:true }, 
    { name: "CUST_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center', edittype:'checkbox', editoptions:{value:"Y:N"} },
    { name: "SALESREP_NM", label: '영업 담당', width: 120, align: 'center' },
    { name: "SALESREP_EMAIL", label: '영업 담당 이메일', width: 220, align: 'center', editable:true },
    { name: "SALESREP_SENDMAIL_YN", label: '발송 여부', width: 100, align: 'center', edittype:'checkbox', editoptions:{value:"Y:N"} },
    { name: "COMMENTS", label: '비고', width: 300, align: 'left', editable:true }
];


$(function(){
    getGridList();
});

//==========================================
// 조회 조건 데이터
//==========================================
function getSearchData(){
    var sData = {
        cust_cd : $('input[name="cust_cd"]').val(),
        cust_nm : $('input[name="cust_nm"]').val(),
        salesrep_nm : $('input[name="salesrep_nm"]').val()
    };
    return sData;
}

//==========================================
// 조회 실행
//==========================================
function dataSearch() {
    $('#gridList').setGridParam({
        postData: getSearchData()
    }).trigger("reloadGrid");
}

//==========================================
// 그리드 초기화
//==========================================
function getGridList(){
    $('#gridList').jqGrid({
        url: "${url}/admin/order/mailAlarmListAjax.lime",
        datatype: "json",
        mtype: 'POST',
        postData: getSearchData(),
        colModel: defaultColModel,
        height: '360px',
        autowidth: true,
        multiselect: true,
        rownumbers: true,
        pager: "#pager",
        jsonReader : { root : 'list' },
        cellEdit: true,
        cellsubmit: 'clientArray',
        loadComplete: function(data) {
            $('#listTotalCountSpanId').html(addComma(data.listTotalCount));
        },
        afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
            // 수정 시 행 강조
            $('#'+rowid).addClass("editedRow");
        }
    });
}

//==========================================
// 저장 기능
//==========================================
function dataInUp() {
    var changedRows = $('#gridList').jqGrid('getChangedCells','all');
    if(changedRows.length == 0){
        alert('수정된 내용이 없습니다.');
        return;
    }

    // 유효성 검사
    for(var i=0; i<changedRows.length; i++){
        var row = changedRows[i];

        // 이메일 유효성 검사
        if(row.CUST_MAIN_EMAIL && !isValidEmail(row.CUST_MAIN_EMAIL)){
            alert("담당자 이메일 형식이 올바르지 않습니다.");
            return;
        }
        if(row.SALESREP_EMAIL && !isValidEmail(row.SALESREP_EMAIL)){
            alert("영업 담당 이메일 형식이 올바르지 않습니다.");
            return;
        }

        // 발송여부 체크되었는데 이메일 없을 때
        if(row.CUST_SENDMAIL_YN == 'Y' && !row.CUST_MAIN_EMAIL){
            alert("담당자 이메일이 없으면 발송할 수 없습니다.");
            return;
        }
        if(row.SALESREP_SENDMAIL_YN == 'Y' && !row.SALESREP_EMAIL){
            alert("영업 담당 이메일이 없으면 발송할 수 없습니다.");
            return;
        }
    }

    if(confirm('저장하시겠습니까?')){
        $.ajax({
            type : 'POST',
            url : '${url}/admin/order/updateMailAlarmAjax.lime',
            data : JSON.stringify(changedRows),
            contentType : 'application/json; charset=UTF-8',
            success : function(data) {
                alert(data.RES_MSG);
                dataSearch();
            }
        });
    }
}

// 이메일 검증
function isValidEmail(email){
    var re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// 엑셀 다운로드
function excelDown(){
    formPostSubmit('frm', '${url}/admin/order/mailAlarmExcelDown.lime');
}
</script>

<style>
/* 수정된 행 강조 */
.editedRow { background-color:#fff3cd !important; }
</style>
</head>

<body>
    <main class="page-content content-wrap">
        <%@ include file="/WEB-INF/views/include/admin/header.jsp" %>
        <%@ include file="/WEB-INF/views/include/admin/left.jsp" %>

        <form name="frm" method="post">
        <div class="page-inner">
            <div class="page-title">
                <h3>
                    주문 메일 알람 관리
                    <div class="page-right">
                        <button type="button" class="btn btn-line f-black" onclick="dataSearch();"><i class="fa fa-search"></i><em>검색</em></button>
                        <button type="button" class="btn btn-line f-black" onclick="window.location.reload();"><i class="fa fa-refresh"></i><em>새로고침</em></button>
                        <button type="button" class="btn btn-line f-black" onclick="excelDown();"><i class="fa fa-file-excel-o"></i><em>엑셀다운로드</em></button>
                    </div>
                </h3>
            </div>

            <!-- 조회조건 -->
            <div class="panel-body no-p">
                <div class="tableSearch">
                    <div class="topSearch">
                        <ul>
                            <li>
                                <label class="search-h">거래처코드</label>
                                <div class="search-c"><input type="text" name="cust_cd" class="search-input" /></div>
                            </li>
                            <li>
                                <label class="search-h">거래처명</label>
                                <div class="search-c"><input type="text" name="cust_nm" class="search-input" /></div>
                            </li>
                            <li>
                                <label class="search-h">영업담당</label>
                                <div class="search-c"><input type="text" name="salesrep_nm" class="search-input" /></div>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>

            <!-- 목록 -->
            <div class="panel-body">
                <h5 class="table-title listT">TOTAL <span id="listTotalCountSpanId">0</span>EA</h5>
                <div class="btnList">
                    <button type="button" class="btn btn-info" onclick="dataInUp();">저장</button>
                </div>
                <div class="table-responsive in">
                    <table id="gridList"></table>
                    <div id="pager"></div>
                </div>
            </div>
        </div>
        </form>
        <%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>
    </main>
</body>
</html>
```

***

## 🔥 핵심 변경 포인트
- **출고지 관리 컬럼(주소, 연락처 등) 전부 제거**
- **1번 요구 jqGrid 컬럼만 구성**
- **조회조건 → 거래처코드, 거래처명, 영업담당**
- **저장 시 유효성 검사 + 수정 행 강조 배경색 기능 추가**
- **엑셀 다운로드 API 변경 (`mailAlarmExcelDown.lime`)**
- 불필요한 쿠키 기반 column 순서/가변 너비 관리 로직 제거 → 깔끔한 jqGrid 유지

***

👉 이렇게 하면 1번/2번 요구사항에 맞는 "주문 메일 알람 관리 페이지"로 작동할 수 있어.  

혹시 저장할 때 **멀티행 한번에 저장** vs **행 단위 실시간 저장(Auto Save)** 중 어떤 방식을 원하는 거야?