<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

<script type="text/javascript">
console.log('Order ADD');
var pageType = '${pageType}'; <%-- ADD/EDIT --%>

//Start. Setting Jqgrid Columns Order.
var ckNameJqGrid = 'admin/order/orderAdd/jqGridCookie'; // 페이지별 쿠키명 설정. // ####### 설정 #######
ckNameJqGrid += '/gridList'; // 그리드명별 쿠키명 설정. // ####### 설정 #######

var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
var globalColumnOrder = globalColumnOrderStr.split(',');

var defaultColModel = [ //  ####### 설정 #######
	{ name:"ITEM_CD", label:'품목코드', key:true, sortable:false, width:200, align:'center', formatter:setItemCd },
	{ name:"DESC1", classes:"descClass", label:'품목명', sortable:false, width:670, align:'left' }, 
	{ name:"UNIT", label:'구매단위', sortable:false, width:100, align:'left', formatter:setUnit4 },
	{ name:"QUANTITY", label:'수량', sortable:false, width:200, align:'center', formatter:setQuantity },
	{ name:"ITI_PALLET", label:'파레트적재단위', sortable:false, width:200, align:'right', formatter:setItiPallet},
	{ name:"FIREPROOF_YN", label:'내화구조', width:50, sortable:false, align:'center',formatter:setFireProof},
	{ name:"", label:'기능', sortable:false, width:150, align:'center', formatter:setButton },
	{ name:"REQ_NO", label:'주문번호', hidden:true },
	{ name:"LINE_NO", label:'주문일련번호', hidden:true },
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
	//var startDateTimeClass = 'bdStartDateClass';
	//var endDateTimeInClass = 'bdEndDateClass';
	var getDateVal = toStr($('input[name="v_requestdate"]').val());
	var dateNow = ('' == getDateVal) ? new Date() : new Date(getDateVal);
	$('#dateTimePickerDivId').DateTimePicker({
		mode:'datetime', // date, time or datetime
		defaultDate: moment(dateNow).hours(0).minutes(0).seconds(0).milliseconds(0),
		dateSeparator:'-',
		timeSeparator:':',
		timeMeridiemSeparator:' ',
		dateTimeSeparator:' ',
		monthYearSeparator:' ',
		dateTimeFormat:'yyyy-MM-dd HH:mm',
		dateFormat:'yyyy-MM-dd',
		timeFormat:'HH:mm',
		maxDate:null,
		minDate: null,
		maxTime:null,
		minTime:null,
		//maxDateTime:null,
		//minDateTime:null,
		
		
		/* addEventHandlers: function(){ // 최소 일자 : 오늘날짜/시간 이전은 선택 안되게 설정.  전체 다 적용되어버려서 일단 없앰
			var oDTP = this;
			oDTP.settings.minDateTime = oDTP.getDateTimeStringInFormat('DateTime', 'yyyy-MM-dd HH:mm', moment(dateNow).hours(0).minutes(0).seconds(0).milliseconds(0));
			//oDTP.settings.minDateTime = oDTP.getDateTimeStringInFormat('DateTime', 'yyyy-MM-dd HH:mm', new Date());
		}, */


		settingValueOfElement: function(sElemValue, dElemValue, oElem){ // 시작 <--> 종료 컨트롤.
			var oDTP = this;
			//if(oElem.hasClass(startDateTimeClass)){s
			//	$('.'+endDateTimeInClass).data('min', $('.'+startDateTimeClass).val());
			//}
			//if(oElem.hasClass(endDateTimeInClass)){
			//	$('.'+startDateTimeClass).data('max', $('.'+endDateTimeInClass).val());
			//}
		},
		shortDayNames: ['일','월','화','수','목','금','토'],
		fullDayNames: ['일요일','월요일','화요일','수요일','목요일','금요일','토요일'],
		shortMonthNames: ['1','2','3','4','5','6','7','8','9','10','11','12'],
		fullMonthNames: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
		labels: {'year': '년', 'month': '월', 'day': '일', 'hour': '시', 'minutes': '분', 'seconds': '초', 'meridiem': ''},
		minuteInterval: 30, // 분 증감 단위
		roundOffMinutes:true,
		secondsInterval: 1,
		roundOffSeconds:true,
		showHeader:true,
		formatHumanDate:function(oDateTime, sMode, sFormat){ // 헤더 날짜 포맷 세팅.
		  return oDateTime.yyyy+'년 '+oDateTime.MM+'월 '+oDateTime.dd+'일 ('+oDateTime.dayShort+') '+oDateTime.HH+'시 '+oDateTime.mm+'분';
		  //return oDateTime.dayShort +", " + oDateTime.month +" " + oDateTime.dd +", " + oDateTime.yyyy;
		},
		titleContentDate:'납품요청일 설정',
		titleContentTime:'납품요청일 설정',
		titleContentDateTime:'납품요청일 설정',
		buttonsToDisplay: ['HeaderCloseButton','SetButton','ClearButton'],
		setButtonContent:'설정',
		clearButtonContent:'초기화',
		buttonClicked:function(sButtonType, oInputElement, c){ // SET, CLEAR, CANCEL, TAB
			//console.log('sButtonType : ', sButtonType);
			//console.log('oInputElement : ', oInputElement);
			if('SET' == sButtonType){
				
			}
			if('CLEAR' == sButtonType){
				//$('input[name="m_requestdt"]').val('');
				//$('input[name="m_requesttime"]').val('');
			}
		},
		settingValueOfElement:function(sValue, dDateTime, oInputElement){
			var setVal = toStr(sValue);
			var setDate = '', setTime = '';
			if('' != setVal){
				setDate = setVal.substring(0, 10).replaceAll('-', '');
				setTime = setVal.substring(11, 16).replaceAll(':', '');
			}

			let hour = Number(setVal.substring(11,13));
			if( (hour<5) & (hour>=0) ) {
				$('input[name="v_requestdate"]').val('');
				$('input[name="m_requestdt"]').val('');
				$('input[name="m_requesttime"]').val('');
				
				alert('00시에서 04시까지는 선택이 불가능한 시간입니다.\n시간 설정을 다시해 주세요.');
			} else {
				$('input[name="m_requestdt"]').val(setDate);
				$('input[name="m_requesttime"]').val(setTime);
			}
		},
		incrementButtonContent:'+',
		decrementButtonContent:'-',
		setValueInTextboxOnEveryClick:false, //true=날짜가변경 됨에 따라 input에 입력.
		readonlyInputs:false,
		animationDuration: 400,
		touchHoldInterval: 300,// in Milliseconds
		captureTouchHold:false,// capture Touch Hold Event
		mouseHoldInterval: 50,// in Milliseconds
		captureMouseHold:false,// capture Mouse Hold Event
		isPopup:true,
		parentElement:'body',
		isInline:false,
		inputElement:null,
		//language:'ko',
	});
});

$(document).ready(function(){ 
	if('EDIT' == pageType){
		$('#noList').hide();
		getItemListAjax('${custOrderH.REQ_NO}', '${custOrderH.CUST_CD}', '${custOrderH.SHIPTO_CD}', 'EDIT');
	}

});

// 품목 리스트 grid 가져오기.
function getItemListAjax(req_no, cust_cd, shipto_cd, page_type){
	$("#gridList").jqGrid({
		url: "${url}/admin/order/getCustOrderDetailListAjax.lime",
		datatype: "json",
		mtype: 'POST',
		postData: {
			r_reqno : req_no
			, r_custcd : cust_cd
			, r_shiptocd : shipto_cd
			, r_pagetype : page_type
		},
		colModel: updateComModel,
		height: '360px',
		autowidth: false,
		rownumbers: true,
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
		multiselect:false,
		jsonReader : {
			root : 'list'
		},
		loadComplete: function(data){
			//console.log($('#gridList').getGridParam('records'));
			initAutoNumeric();
		},
		gridComplete: function(){ 
			//alert($('#gridList').getGridParam('records'));
			// 조회된 데이터가 없을때
			var grid = $('#gridList');
		    var emptyText = grid.getGridParam('emptyDataText'); //NO Data Text 가져오기.
		    var container = grid.parents('.ui-jqgrid-view'); //Find the Grid's Container
			if(0 == grid.getGridParam('records')){
				//container.find('.ui-jqgrid-hdiv, .ui-jqgrid-bdiv').hide(); //Hide The Column Headers And The Cells Below
		        //container.find('.ui-jqgrid-titlebar').after(''+emptyText+'');
            }
		},
		//emptyDataText: '조회된 데이터가 없습니다.', //NO Data Text
		//onSelectRow: editRow,
	});
	
// 	if('ADD' == pageType){
// 		var colCnt = $('.ui-jqgrid-htable > thead > tr > th:visible').length; //gridList_rn
// 		//alert('colCnt1 : '+colCnt);
// 		$('.ui-jqgrid-htable').append('<tr><td colspan="'+colCnt+'" class="text-center">품목을 선택해 주세요.</td></tr>');
// 	}
}

function setItemCd(cellValue, options, rowdata) {
	return cellValue+'<input type="hidden" name="m_itemcd" value="'+cellValue+'" />';
}

function setFireProof(cellValue, options, rowdata) {
	return cellValue+'<input type="hidden" name="m_fireproof" readonly="readonly" value="'+cellValue+'" />';
}

function setUnit4(cellValue, options, rowdata) {
	return '<input type="text" name="m_unit" value="'+toStr(cellValue)+'" onkeyup="checkByte(this, 3);" readonly="readonly" />';
	//return toStr(cellValue);
}

function setQuantity(cellValue, options, rowdata) {
	return '<input type="text" name="m_quantity" class="amountClass2 text-right" value="'+toStr(cellValue)+'" />';
}

function setButton(cellValue, options, rowdata) {
	return '<button type="button" class="btn btn-default btn-xs" onclick=\'delItem(this, "'+options.rowId+'");\'>삭제</button>';
}

function setItiPallet(cellValue, options, rowdata) {
	return addComma(toFloat(toStr(cellValue).replaceAll(',', '')));
	//return '<input type="hidden" name="c_itipallet" value="'+cellValue+'" />;'
}

// 거래처 선택 팝업 띄우기.
function openCustomerPop(obj){
	const urlStr = window.location.href;
	const url = new URL(urlStr);
	const urlParams = url.searchParams;
	const reqNo = urlParams.get('r_reqno');
	if( (reqNo != null) && (reqNo != '') ){
		alert('거래처코드는 수정할 수 없습니다.');
		return;
	}
	
	// 팝업 세팅.
	var widthPx = 1200;
	var heightPx = 805;
	var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx;
	
	$('form[name="frm_pop"]').remove();
	
	var htmlText = '';
	htmlText += '<form name="frm_pop" method="post" target="customerListPop">';
	htmlText += '	<input type="hidden" name="pop" value="1" />';
	htmlText += '	<input type="hidden" name="page_type" value="orderadd" />';
	htmlText += '	<input type="hidden" name="r_multiselect" value="false" />'; // 행 단위 선택 가능여부 T/F
	htmlText += '</form>';
	$('body').append(htmlText);
	
	// #POST# 팝업 열기.
	var popUrl = '${url}/admin/base/pop/customerListPop.lime';
	window.open('', 'customerListPop', options);
	$('form[name="frm_pop"]').prop('action', popUrl);
	$('form[name="frm_pop"]').submit().remove();
}

//return 거래처 팝업에서 개별 선택.
function setCustomerFromPop(jsonData){
	$('input[name="m_custcd"]').val(toStr(jsonData.CUST_CD));
	$('input[name="v_custnm"]').val(toStr(jsonData.CUST_NM));
	
	setDefaultShipTo();
}

// 납품처 초기화.
function setDefaultShipTo(){
	$('input[name="m_shiptocd"]').val('');
	$('input[name="v_shiptonm"]').val('');
	$('input[name="v_shiptoqt"]').val('');
}

// 납품처 선택 팝업 띄우기.
function openShiptoPop(obj){
	var selectedCustCd = toStr($('input[name="m_custcd"]').val());
	if('' == selectedCustCd){
		alert('거래처를 선택 후 진행해 주세요.');
		return;
	}
	
	// 팝업 세팅.
	var widthPx = 1200;
	var heightPx = 805;
	var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx;
	
	$('form[name="frm_pop"]').remove();
	
	var htmlText = '';
	htmlText += '<form name="frm_pop" method="post" target="shiptoListPop">';
	htmlText += '	<input type="hidden" name="pop" value="1" />';
	htmlText += '	<input type="hidden" name="page_type" value="orderadd" />';
	htmlText += '	<input type="hidden" name="r_multiselect" value="false" />'; // 행 단위 선택 가능여부 T/F
	htmlText += '	<input type="hidden" name="r_custcd" value="'+selectedCustCd+'" />';
	htmlText += '</form>';
	$('body').append(htmlText);
	
	// #POST# 팝업 열기.
	var popUrl = '${url}/admin/base/pop/shiptoListPop.lime';
	window.open('', 'shiptoListPop', options);
	$('form[name="frm_pop"]').prop('action', popUrl);
	$('form[name="frm_pop"]').submit().remove();
}

function openShiptoQtPop(obj){
	var selectedCustCd = toStr($('input[name="m_custcd"]').val());
	var quoteQt = toStr($('input[name="v_shiptoqt"]').val());
	if('' == selectedCustCd){
		alert('거래처를 선택 후 진행해 주세요.');
		return;
	}
	
	// 팝업 세팅.
	var widthPx = 1200;
	var heightPx = 805;
	var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx;
	
	$('form[name="frm_pop"]').remove();
	
	var htmlText = '';
	htmlText += '<form name="frm_pop" method="post" target="shiptoListPop">';
	htmlText += '	<input type="hidden" name="pop" value="1" />';
	htmlText += '	<input type="hidden" name="page_type" value="orderadd" />';
	htmlText += '	<input type="hidden" name="r_multiselect" value="false" />'; // 행 단위 선택 가능여부 T/F
	htmlText += '	<input type="hidden" name="r_custcd" value="'+selectedCustCd+'" />';
	htmlText += '	<input type="hidden" name="rl_quoteqt" value="'+quoteQt+'" />';
	htmlText += '</form>';
	$('body').append(htmlText);
	
	// #POST# 팝업 열기.
	var popUrl = '${url}/admin/base/pop/shiptoListPop.lime';
	window.open('', 'shiptoListPop', options);
	$('form[name="frm_pop"]').prop('action', popUrl);
	$('form[name="frm_pop"]').submit().remove();
}


// return 납품처 팝업에서 개별 선택.
function setShiptoFromPop(jsonData){
	var quoteQt = toStr(jsonData.QUOTE_QT);
	var shiptoCd = (quoteQt==='') ? '' : toStr(jsonData.SHIPTO_CD);
	var shiptoNm = (quoteQt==='') ? '' : toStr(jsonData.SHIPTO_NM);
	$('input[name="m_shiptocd"]').val(shiptoCd);
	$('input[name="v_shiptonm"]').val(shiptoNm);
	$('input[name="v_shiptoqt"]').val(quoteQt);
	$('input[name="m_zipcd"]').val(toStr(jsonData.ZIP_CD));
	$('input[name="m_add1"]').val(toStr(jsonData.ADD1));
	$('input[name="m_add2"]').val('');
	//$('input[name="m_add2"]').val(toStr(jsonData.ADD2));
	
	var tels = toStr(jsonData.ADD3);
	tels = tels.replaceAll(' ', '');
	tels = tels.replaceAll('-', '');
	var telArr = tels.split(',');
	if('' != tels){
		var alltlp_reg = /^(01[016789]{1}|02|0[3-9]{1}[0-9]{1})?[0-9]{3,4}?[0-9]{4}$/; // 휴대폰+일반전화번호+050+070 체크, '-' 제외
		for(var i=0,j=telArr.length; i<j; i++){ // 데이터가 엉뚱한 값이 많이 들어가다보니... 거르자.
			// 전화번호 형식인 경우에만 입력.
			if(alltlp_reg.test(telArr[i].replaceAll(' ',''))){	
				if(0==i){
					$('input[name="m_tel1"]').val(telArr[i].replaceAll(' ',''));
				}else if(1==i){
					$('input[name="m_tel2"]').val(telArr[i].replaceAll(' ',''));
				}
			}
		}
	}
}

// 품목 선택 팝업 띄우기.
function openItemPop(obj){
	// 팝업 세팅.
	var widthPx = 1200;
	var heightPx = 900;
	var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx;
	
	$('form[name="frm_pop"]').remove();
	
	var htmlText = '';
	htmlText += '<form name="frm_pop" method="post" target="itemListPop">';
	htmlText += '	<input type="hidden" name="pop" value="1" />';
	htmlText += '	<input type="hidden" name="page_type" value="orderadd" />';
	htmlText += '	<input type="hidden" name="r_multiselect" value="true" />'; // 행 단위 선택 가능여부 T/F
	htmlText += '</form>';
	$('body').append(htmlText);
	
	// #POST# 팝업 열기.
	var popUrl = '${url}/admin/base/pop/itemListPop.lime';
	window.open('', 'itemListPop', options);
	$('form[name="frm_pop"]').prop('action', popUrl);
	$('form[name="frm_pop"]').submit().remove();
}

//return 품목 팝업에서 다중 선택.
function setItemFromPop(jsonArray){
	//console.log('jsonArray : ', jsonArray);
	
	if($('#noList').is(':visible')){
		$('#noList').hide();
		getItemListAjax('', '', '', 'ADD');
	}
	
	// 행 추가.
	var jsonNewArray = new Array();
	
	var nowViewItemCd = '';
	$('input[name="m_itemcd"]').each(function(i,e) {
		nowViewItemCd += $(e).val()+',';
	});
	
	for(var x=0,y=jsonArray.length; x<y; x++){
		//console.log('jsonArray[] : ', jsonArray[x]);
		var selectedItemCd = jsonArray[x]['ITEM_CD'];
		if(0 > nowViewItemCd.indexOf(selectedItemCd+',')){
			jsonNewArray.push(jsonArray[x]);
		}
	}	
	//console.log('jsonNewArray : ', jsonArray[x]);
	
	var newRow = {position:"last", initdata:jsonNewArray};
	$("#gridList").jqGrid('addRow', newRow);
	initAutoNumeric();
}

// 품목 삭제.
function delItem(obj, rowId){
	$("#gridList").jqGrid("delRowData", rowId); // 행 삭제

	// 행이 모두 삭제된 경우 초기화.
	var rowCnt = $("#gridList").getGridParam("reccount");
	if(0 == rowCnt){
		$('#noList').show();
		$("#gridList").jqGrid("GridUnload");
	}
}

// 주소록 선택 합업 띄우기.
function openOrderAddressBookmarkPop(obj){
	// 팝업 세팅.
	var widthPx = 1200;
	var heightPx = 805;
	var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx;
	
	$('form[name="frm_pop"]').remove();
	
	var htmlText = '';
	htmlText += '<form name="frm_pop" method="post" target="orderAddressBookmarkPop">';
	htmlText += '	<input type="hidden" name="pop" value="1" />';
	htmlText += '	<input type="hidden" name="page_type" value="orderadd" />';
	htmlText += '	<input type="hidden" name="r_multiselect" value="false" />'; // 행 단위 선택 가능여부 T/F
	htmlText += '</form>';
	$('body').append(htmlText);
	
	// #POST# 팝업 열기.
	var popUrl = '${url}/admin/base/pop/orderAddressBookmarkPop.lime';
	window.open('', 'orderAddressBookmarkPop', options);
	$('form[name="frm_pop"]').prop('action', popUrl);
	$('form[name="frm_pop"]').submit().remove();
}

//return 주소록 팝업에서 개별 선택.
function setOrderAddressBookmarkFromPop(jsonData){
	$('input[name="m_zipcd"]').val(toStr(jsonData.OAB_ZIPCD));
	$('input[name="m_add1"]').val(escapeXss(toStr(jsonData.OAB_ADD1)));
	$('input[name="m_add2"]').val(escapeXss(toStr(jsonData.OAB_ADD2)));
	$('input[name="m_receiver"]').val(escapeXss(toStr(jsonData.OAB_RECEIVER)));
	$('input[name="m_tel1"]').val(toStr(jsonData.OAB_TEL1));
	$('input[name="m_tel2"]').val(toStr(jsonData.OAB_TEL2));
}

// 최근주소 불러오기.
function getRecentOrderAddress(obj){
	$(obj).prop('disabled', true);
	
	if(confirm('최근에 주문한 주소를 불러 오시겠습니까?')){
		$.ajax({
			async : false,
			url : '${url}/admin/order/getRecentOrderAjax.lime',
			cache : false,
			type : 'POST',
			dataType: 'json',
			data : {  },
			success : function(data){
				var recent = data.recent;
				
				if('' == recent){
					alert('최근 주문 내역이 없습니다.');
					$(obj).prop('disabled', false);
					return;
				}
				
				var add1 = toStr(recent[0].ADD1);
				console.log(add1);
				add1 = escapeXss(add1);
				console.log(add1);
				
				//alert(toStr(recent[0].ADD1));
				//alert(escapeXss(toStr(recent[0].ADD1)));
				
				$('input[name="m_zipcd"]').val(toStr(recent[0].ZIP_CD));
				$('input[name="m_add1"]').val(escapeXss(toStr(recent[0].ADD1)));
				$('input[name="m_add2"]').val(escapeXss(toStr(recent[0].ADD2)));
				$('input[name="m_receiver"]').val(escapeXss(toStr(recent[0].RECEIVER)));
				$('input[name="m_tel1"]').val(toStr(recent[0].TEL1).replaceAll('-', ''));
				$('input[name="m_tel2"]').val(toStr(recent[0].TEL2).replaceAll('-', ''));
				
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

//
function checkDataIn(obj, status, reqNo) {
	var params = {r_zipcd : $('input[name="m_zipcd"]').val()};
	$.ajax({
		async : false,
		url : '${url}/admin/order/getPostalCodeCount.lime',
		cache : false,
		type : 'POST',
		dataType: 'json',
		data : params,
		success : function(data){
			if(data.useFlag === 'Y') {
				dataIn(obj, status, reqNo);
			} else {
				alert('해당 우편번호는 시스템에 존재하지 않습니다. 담당CS직원에게 문의해 주세요.');
			}
		},
		error : function(request,status,error){
			alert('해당 우편번호는 시스템에 존재하지 않습니다. 담당CS직원에게 문의해 주세요.');
		}
	});
}

//2024-11-28 hsg German Suplex 중복 클릭을 막기 위해 setTimeout 함수를 이용하도록 수정
var clickCnt = 0;
function dataIn(obj, status, reqNo){
	if(clickCnt > 0){
		//setTimeout(function () { $(obj).prop('disabled', false); alert('a'); }, 2000);
		setTimeout(() => clickCnt=0, 3000);
	} else {
		clickCnt++;
		dataIn2(obj, status, reqNo);
	}
}

// 주문상태 변경.
function dataIn2(obj, status, reqNo){

	$(obj).prop('disabled', true);

	var postalCodeCheck = false;
	var params = {r_zipcd : $('input[name="m_zipcd"]').val()};
	$.ajax({
		async : false,
		url : '${url}/admin/order/getPostalCodeCount.lime',
		cache : false,
		type : 'POST',
		dataType: 'json',
		data : params,
		success : function(data){
			if(data.useFlag === 'Y') {
				postalCodeCheck = true;
			} 
		},
		error : function(request,status,error){
		}
	});

	if(!postalCodeCheck) {
		alert('해당 우편번호는 시스템에 존재하지 않습니다. 담당CS직원에게 문의해 주세요.');
		$(obj).prop('disabled', false);
		return;
	}

	var ckflag = dataValidation();
	if(!ckflag){
		$(obj).prop('disabled', false);
		return;
	}

	var insertFlag = true;
	
	var confirmText = '주문접수 하시겠습니까?';
	if('99' == status){ 
		confirmText = '임시저장 하시겠습니까?';
		insertFlag = false;
	}
	if(reqNo){ 
		confirmText = '주문접수 상태 입니다.\n수정 하시겠습니까?';
		insertFlag = false;
	}
	
	// 파레트 적재단위 수량 > 주문수량 경우 알림. => 리스트에 뿌려주는걸로.
	/* 
	var trObj = $('#gridList > tbody > tr');
	$(trObj).each(function(i,e){
		if(0 != i){ // i==0 class="jqgfirstrow"로 실제 데이터가 아님.
			var pallet = Number($(e).find('input[name="c_itipallet"]').val());
			var quantity = Number($(e).find('input[name="m_quantity"]').val());
			var itemNm = $(e).find('.descClass').html();
			if(pallet > quantity){
				alert(escapeXss('품목 '+itemNm+'의 팔레트 구성수량은'+addComma(pallet)+'개 입니다.'));
			}
		}
	});
	*/
	
	// 요청사항 행바꿈/엔터 제거. 
	var m_remark = $('input[name="m_remark"]').val();
	if('' != m_remark){
		m_remark = m_remark.replace(/\n/g, ' '); // 행바꿈 제거
		m_remark = m_remark.replace(/\r/g, ' '); // 엔터 제거
		$('input[name="m_remark"]').val(m_remark);
	}
	
	$('input[name="m_statuscd"]').val(status);
	
	if(confirm(confirmText)){
		//var m_transty = $('input:radio[name="m_transty"]:checked').val();
		//if('AB' == m_transty){ //운송수단이 자차운송인 경우는 우편번호를 90000으로 픽스.
		//	$('input[name="m_zipcd"]').val('90000');
		//}
		$('#ajax_indicator').show().fadeIn('fast');
		
		var trObj = $('#gridList > tbody > tr');
		var fireproofFlag = false;
		$(trObj).each(function(i,e){
			if(0 != i){ // i==0 class="jqgfirstrow"로 실제 데이터가 아님.
				var fireproofYn = $($(e).find('input[name="m_fireproof"]')[0]).val();
				if(fireproofYn=='Y'){
					fireproofFlag = true;
				}
			}
		});
		
		
		$('form[name="frm"]').ajaxSubmit({
			dataType : 'json',
			type : 'POST',
			url : '${url}/admin/order/insertCustOrderAjax.lime',
			//async : false, //사용x
			//data : param, //사용x
			success : function(data) {
				if(data.RES_CODE == '0000') {
					$('#m_reqNo').val(data.m_ohhreqno);
					
					//접수버튼 QMS 입력 중 숨김
					$('.order-save-btn').css('display','none');
					//최초 오더접수 입력시에만 사전입력 진행
					if(insertFlag){
						$('form[name="frm"]').ajaxSubmit({
							dataType : 'json',
							type : 'POST',
							url : '${url}/admin/order/setQmsFirstOrderAjax.lime',
							success : function(data) {
								$('#m_qmsTempId').val(data.qmsTempId);
								
								if(fireproofFlag){
									alert('선택하신 품목 중 내화구조 품목이 포함되어 있습니다.\rQMS 입력화면으로 이동합니다.');
									$('.qmspop-btn').css('display','block');
									// POST 팝업 열기.
									var widthPx = 1000;
									var heightPx = 800;
									var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx;
									var popup = window.open('qmsOrderPrePop.lime?qmsTempId='+data.qmsTempId, 'qmsOrderPrePop', options);
									if(popup){
										popup.focus();
									}
									/* $.ajax({
										async : false,
										url : '${url}/admin/order/setQmsFirstOrderCancelAjax.lime',
										cache : false,
										type : 'POST',
										dataType: 'json',
										data : { 'qmsTempId' : data['qmsTempId'] },
										success : function(data){
											alert('내화구조 사전입력을 취소했습니다.');
										},
										error : function(request,status,error){
											alert('Error');
										}
									});	 */
								}else{
									moveOrderList();
								}
								
							},error : function(request,status,error){
								alert('Error');
								$('#ajax_indicator').fadeOut();
							}
						});
					}else{
						moveOrderList();
					}
				}
				$('#ajax_indicator').fadeOut();
			},
			error : function(request,status,error){
				alert('Error');
				$(obj).prop('disabled', false);
				$('.qmspop-btn').css('display','none');
				$('.order-save-btn').css('display','inline-block');
				$('#ajax_indicator').fadeOut();
			}
			
		});
		
		$(obj).prop('disabled', false);
	}
	else{
		$(obj).prop('disabled', false);
	}
}


function dataQMS(){
	var trObj = $('#gridList > tbody > tr');
	var fireproofFlag = false;
	
	$(trObj).each(function(i,e){
		if(0 != i){ // i==0 class="jqgfirstrow"로 실제 데이터가 아님.
			var fireproofYn = $($(e).find('input[name="m_fireproof"]')[0]).val();
			if(fireproofYn=='Y'){
				fireproofFlag = true;
			}
		}
	});

	if(fireproofFlag){
		// POST 팝업 열기.
		var widthPx = 1000;
		var heightPx = 800;
		var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx;
		var popup = window.open('qmsOrderPrePop.lime?qmsTempId='+$('#m_qmsTempId').val(), 'qmsOrderPrePop', options);
		if(popup){
			popup.focus();
		} else if(popup == null || popup.screenLeft == 0){
			alert('[안내] 브라우저의 팝업차단 기능이 활성화 되어 있을 경우\rQMS 입력 팝업 창이 열리지 않아 서비스 사용에 문제가 있을 수 있습니다.\r반드시 팝업차단을 해제해주세요.');
		}
	}else{
		moveOrderList();
	}
}


function moveOrderList(){
	formGetSubmit('${url}/admin/order/orderList.lime', '');
}

// 유효성 체크.
function dataValidation(){
	var ckflag = true;
	
	if(ckflag) ckflag = validation($('input[name="m_custcd"]')[0], '거래처', 'value', '선택해 주세요.');
	if(ckflag) ckflag = validation($('input[name="m_zipcd"]')[0], '납품 주소 우편번호', 'value');
	if(ckflag) ckflag = validation($('input[name="m_add1"]')[0], '납품 주소', 'value');
	//if(ckflag) ckflag = validation($('input[name="m_add2"]')[0], '납품 상세 주소', 'value');
	if(ckflag) {
		//ckflag = validation($('input[name="m_tel1"]')[0], '연락처', 'value,alltlp');
		var pNum = $('input[name="m_tel1"]').val();
		var tVal = pNum.substring(0, 3);
		if( ((tVal === '010') && (pNum.length !== 11)) || (pNum.length < 10) ) {
			ckflag = false;
			alert("연락처 형식이 일치하지 않습니다.");
			$('input[name="m_tel1"]')[0].focus();
		}
	}
	if(ckflag) {
		//if(ckflag) ckflag = validation($('input[name="m_tel2"]')[0], '연락처2', 'alltlp');
		var pNum = $('input[name="m_tel2"]').val();
		var tVal = pNum.substring(0, 3);
		if( (tVal === '010') && (pNum.length !== 11) ) {
			ckflag = false;
			alert("연락처 형식이 일치하지 않습니다.");
			$('input[name="m_tel1"]')[0].focus();
		}
	}
	
	if(ckflag) ckflag = validation($('input[name="v_requestdate"]')[0], '납품요청일', 'value', '선택해 주세요.');

	if($('input[name="m_zipcd"]').val().length != 5) {
		alert('우편번호는 5자리만 입력 가능합니다.');
		ckflag = false;
	}

	if(ckflag) {
		const targetDate = new Date($('input[name="v_requestdate"]').val());
		const currDate = new Date();
		const futureDate = new Date();
		futureDate.setDate(futureDate.getDate() + 60);
		if(targetDate > futureDate) {
			ckflag = false;
			alert("납기요청일은 현재일 ~ 현재일+60일 이내까지 지정 가능합니다.");
		}

		if(currDate >= targetDate) {
			ckflag = false;
			alert("납기요청일은 현재일 ~ 현재일+60일 이내까지 지정 가능합니다.");
		}
	}

	// 품목선택 및 품목관련 입력 여부.
	if(ckflag){
		if($('#noList').is(':visible')){
			alert('품목을 선택해 주세요.');
			ckflag = false;
		}
		else{
			var trObj = $('#gridList > tbody > tr');
			$(trObj).each(function(i,e){
				if(0 != i){ // i==0 class="jqgfirstrow"로 실제 데이터가 아님.
					ckflag = validation($(e).find('input[name="m_quantity"]')[0], '품목 수량', 'value');
					if(!ckflag) return false;
				}
			});
		}
	}

	return ckflag;
}

// 자재주문서 출력 팝업 띄우기.
function viewOrderPaper(obj){
	
}

function limitInputLength(inField) {
	//checkByte(this, '60');
	let inputField = document.getElementById(inField);
	let inputValue = inputField.value;
	if(inputValue.length > 40) {
		alert("글자수를 40자 이상 초과하여 입력할 수 없습니다.");
		inputField.value=inputValue.slice(0, 40);
	}
}

</script>
</head>

<body class="page-header-fixed pace-done compact-menu searchHidden">
	
	<!-- Page Content -->
	<main class="page-content content-wrap">
	
		<%@ include file="/WEB-INF/views/include/admin/header.jsp" %>
		<%@ include file="/WEB-INF/views/include/admin/left.jsp" %>
		
		
		<!-- Page Inner -->
		<div class="page-inner">
			<div class="page-title">
				<h3>
					<c:choose>
						<c:when test="${'ADD' eq pageType}">주문등록</c:when>
						<c:when test="${'EDIT' eq pageType}">주문등록</c:when>
						<c:otherwise></c:otherwise>
					</c:choose>
					<div class="page-right">
						<button type="button" class="btn btn-line f-black" title="목록" onclick="document.location.href='${url}/admin/order/orderList.lime'"><i class="fa fa-list-ul"></i><em>목록</em></button>
						<button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();"><i class="fa fa-refresh"></i><em>새로고침</em></button>
					</div>
				</h3>
			</div>
			
			<!-- Main Wrapper -->
			<div id="main-wrapper">
				<!-- Row -->
				<div class="row">
					<div class="col-md-12">
						<div class="panel panel-white">
							<form method="post" name="frm">
							<input type="hidden" name="r_reqno" value="${custOrderH.REQ_NO}" /> <%-- 주문번호 --%>
							<input type="hidden" name="m_statuscd" value="" /> <%-- 상태값 99=임시저장,00=주문접수 --%>
							<input type="hidden" name="m_todayDate" value="${todayDate}" /> <%-- 주문일자 --%>
							<input type="hidden" id="m_reqNo" name="m_reqNo" value="" /> <%-- 주문일자 --%>
							<input type="hidden" id="m_qmsTempId" name="m_qmsTempId" value="" /> <%-- 임시 QMS 번호 --%>
							
							<%-- Create an empty container for the picker popup. --%>
							<div id="dateTimePickerDivId"></div> 
							
							<div class="panel-body">
								<h5 class="table-title">관리자 주문</h5>
								<div class="btnList">
									<%-- <button type="button" class="btn btn-warning" onclick="viewOrderPaper(this);">자재주문서</button> --%>
									<c:choose>
										<c:when test="${empty custOrderH.REQ_NO}">
											<button type="button" class="btn btn-info writeObjectClass order-save-btn" onclick="dataIn(this, '00', '${custOrderH.REQ_NO}');"><c:out value="${orderStatus['00']}" /></button> <%-- 주문접수 --%>
										</c:when>
										<c:when test="${not empty custOrderH.REQ_NO}">
											<button type="button" class="btn btn-info writeObjectClass order-save-btn" onclick="dataIn(this, '00', '${custOrderH.REQ_NO}');"><c:out value="적용" /></button> <%-- 주문수정 --%>
											<button type="button" class="btn btn-info writeObjectClass order-save-btn" onclick="history.back();"><c:out value="취소" /></button> <%-- 주문수정 --%>
											
										</c:when>
									</c:choose>
									<button type="button" class="btn btn-warning qmspop-btn" onclick="dataQMS();"  style="display:none;" >QMS 입력</button> <%-- 주문접수 --%>
									<%-- 
									<button type="button" class="btn btn-github writeObjectClass" onclick="dataIn(this, '99');"><c:out value="${orderStatus['99']}" /></button> <!-- 임시저장 -->
									--%>
								</div>
								
								<div class="table-responsive b-n-b">
									<table id="" class="display table dataTable" width="100%" border="0" cellpadding="0" cellspacing="0">
										<colgroup>
											<col width="12%" />
											<col width="38%" />
											<col width="12%" />
											<col width="38%" />
										</colgroup>
										<tbody>
											<tr>
												<th>거래처 *</th>
												<td>
													<input type="text" class="w-40" name="v_custnm" value="${custOrderH.CUST_NM}" readonly="readonly" onclick="openCustomerPop(this);" />
													<input type="hidden" name="m_custcd" value="${custOrderH.CUST_CD}" />
													<a href="javascript:;" onclick="openCustomerPop(this);"><i class="fa fa-search i-search"></i></a>
												</td>
												<th>주문자 / 주문일자</th>
												<td>${sessionScope.loginDto.userNm} / ${todayDate}</td>
											</tr>
											<tr>
												<th>납품처</th>
												<td colspan="3">
													<c:set var="shiptoCd">${custOrderH.SHIPTO_CD}</c:set>
													<c:if test="${'0' eq shiptoCd}"><c:set var="shiptoCd" value="" /></c:if>
													
													<input type="text" class="w-16" name="m_shiptocd" placeholder="납품처코드" value="${shiptoCd}" readonly="readonly" onclick="openShiptoPop(this);" />
													<input type="text" class="w-25" name="v_shiptonm" placeholder="납품처명" value="${custOrderH.SHIPTO_NM}" readonly="readonly" onclick="openShiptoPop(this);" />
													<a href="javascript:;" onclick="openShiptoPop(this);"><i class="fa fa-search i-search"></i></a>
													<input type="text" class="w-15" name="v_shiptoqt" placeholder="Quation QT" value="${custOrderH.QUOTE_QT}" onkeypress="if(event.keyCode == 13){openShiptoQtPop(this);}"/>
													<a href="javascript:;" onclick="openShiptoQtPop(this);"><i class="fa fa-search i-search"></i></a>
													<button type="button" class="btn btn-line btn-xs writeObjectClass" onclick="setDefaultShipTo();">초기화</button>
												</td>
											</tr>
											<tr>
												<th>납품주소 *</th>
												<td colspan="3" class="checkbox">
												<div class="orderadd">
													<input type="text" class="w-16 numberClass" name="m_zipcd" placeholder="우편번호" value="${custOrderH.ZIP_CD}" onkeyup="checkByte(this, '12')"; />
													<button type="button" class="btn btn-black btn-xs writeObjectClass" onclick="openPostPop2('m_zipcd', 'm_add1', 'm_add2', '', '40');">우편번호찾기</button>
													<button type="button" class="btn btn-line btn-xs writeObjectClass" onclick="getRecentOrderAddress(this);">최근주소</button>
													<button type="button" class="btn btn-line btn-xs writeObjectClass" onclick="openOrderAddressBookmarkPop(this);">주소록</button>
													<label class="m-l-xs m-t-xxxs"><input type="checkbox" name="r_savebookmark" value="Y" />주소록저장</label>
												</div>
													
													<input type="text" class="w-40" name="m_add1" placeholder="주소" value="${custOrderH.ADD1}" onkeyup="checkByte(this, '180')"; />
													<input type="text" class="w-40" name="m_add2" placeholder="상세주소" value="${custOrderH.ADD2}" onkeyup="checkByte(this, '60')"; />
												</td>
											</tr>
											<tr>
												<th>인수자명</th>
												<td>
													<input type="text" class="w-40" name="m_receiver" value="${custOrderH.RECEIVER}" onkeyup="checkByte(this, '40')"; />
												</td>
												
												<th>연락처1 * / 연락처2</th>
												<td>
													<input type="text" class="w-40" name="m_tel1" placeholder="연락처 필수, 숫자만 입력해 주세요." value="${custOrderH.TEL1}" onkeyup="checkByte(this, '40')"; />
													<input type="text" class="w-40" name="m_tel2" placeholder="연락처2, 숫자만 입력해 주세요." value="${custOrderH.TEL2}" onkeyup="checkByte(this, '40')"; />
												</td>
											</tr>
											<tr>
											
											
											
												<th>납품요청일 * </th>
												<td>
													<fmt:parseDate value="${custOrderH.REQUEST_DT}" var="requestDate" pattern="yyyyMMdd"/>
													<fmt:parseDate value="${custOrderH.REQUEST_TIME}" var="requestTime" pattern="HHmm"/>
													<c:set var="v_requestdatedt"><fmt:formatDate value="${requestDate}" pattern="yyyy-MM-dd"/> <fmt:formatDate value="${requestTime}" pattern="HH:mm"/></c:set>
													
													<input type="text" class="w-40 p-r-md bdStartDateClass" name="v_requestdate" data-field="datetime" data-startend="start" data-startendelem=".bdEndDateClass" value="${v_requestdatedt}" readonly="readonly" /><i class="fa fa-calendar i-calendar"></i>
													<input type="hidden" name="m_requestdt" value="<fmt:formatDate value="${requestDate}" pattern="yyyyMMdd"/>" /> <%-- YYYYMMDD --%>
													<input type="hidden" name="m_requesttime" value="<fmt:formatDate value="${requestTime}" pattern="HHmm"/>" /> <%-- HHMM --%>
												</td>
												
												
												
												<th>운송수단</th>
												<td class="radio">
													<label><input type="radio" name="m_transty" value="AA" <c:if test="${empty custOrderH.TRANS_TY or 'AA' eq custOrderH.TRANS_TY}">checked="checked"</c:if> />기본운송</label>
													<label><input type="radio" name="m_transty" value="AB" <c:if test="${'AB' eq custOrderH.TRANS_TY}">checked="checked"</c:if> />자차운송(주문처운송)</label>
												</td>
											</tr>
											<tr>
												<th>요청사항</th>
												<td colspan="3">
													<input type="text" class="" name="m_remark" value="${custOrderH.REMARK}" onkeyup="checkByte(this, '40');" />
													<!-- <input type="text" class="" name="m_remark" id="m_remark" value="${custOrderH.REMARK}" placeholder="글자수를 40자 이내로 제한합니다.";
														onkeyup="limitInputLength('m_remark')" /> -->
												</td>
											</tr>
										</tbody>
									</table>
									<div class="m-t-xs f-red">* 생산요청 제품을 선택하신 경우에는 <strong>거래처 요청사항</strong>란에 제품의 규격 및 세부사항을 반드시 입력해 주시기 바랍니다.</div>
								</div>
							</div>
								
							<div class="panel-body">
								<h5 class="table-title">품목</h5>
								<div class="btnList writeObjectClass">
									<a href="javascript:;" onclick="openItemPop(this);" class="btn btn-line">품목선택</a>
									<!-- <a href="javascript:;" onclick="delItem(this);" class="btn btn-danger">삭제</a> -->
								</div>
								<div class="table-responsive in min">
									<table id="gridList" class="display table tableList nowrap" width="100%" border="0" cellpadding="0" cellspacing="0">
									</table>
									
									<table id="noList" class="display table noList" width="100%" border="0" cellpadding="0" cellspacing="0">
										<tbody>
											<tr>
												<td>품목을 선택해 주세요.</td>
											</tr>
										</tbody>
									</table>
								</div>
							</div>
							
							</form>
						</div>
					</div>
				</div>
				<!-- //Row -->
			</div>
			<!-- //Main Wrapper -->
			
			<%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>
			
		</div>
		<!-- //Page Inner -->
	</main>
	<!-- //Page Content -->
	
</body>
</html>