<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/front/commonimport.jsp" %>
<!DOCTYPE HTML>
<!--[if IE 8]><html lang="ko" class="ie8"><![endif]-->
<!--[if gt IE 8]><!--><html lang="ko"><!--<![endif]-->
<head>
<%@ include file="/WEB-INF/views/include/front/commonhead.jsp" %>

<script type="text/javascript">
(function(){var w=window;if(w.ChannelIO){return w.console.error("ChannelIO script included twice.");}var ch=function(){ch.c(arguments);};ch.q=[];ch.c=function(args){ch.q.push(args);};w.ChannelIO=ch;function l(){if(w.ChannelIOInitialized){return;}w.ChannelIOInitialized=true;var s=document.createElement("script");s.type="text/javascript";s.async=true;s.src="https://cdn.channel.io/plugin/ch-plugin-web.js";var x=document.getElementsByTagName("script")[0];if(x.parentNode){x.parentNode.insertBefore(s,x);}}if(document.readyState==="complete"){l();}else{w.addEventListener("DOMContentLoaded",l);w.addEventListener("load",l);}})();
ChannelIO('boot', {
  "pluginKey": "db38b412-585f-4b04-a939-8ea08c3f9e8d"
});

var pageType = '${pageType}'; <%-- ADD/EDIT/COPY --%>
//alert(pageType);

$(function(){
	var accessDevice = (!isApp()) ? '1' : '2';
	//alert(accessDevice);
	$('input[name="m_accessdevice"]').val(accessDevice);
	
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
		/* addEventHandlers: function(){ // 최소 일자 : 오늘날짜/시간 이전은 선택 안되게 설정. 전체 설정 되어서 삭제
			var oDTP = this;
			oDTP.settings.minDateTime = oDTP.getDateTimeStringInFormat('DateTime', 'yyyy-MM-dd HH:mm', moment(dateNow).hours(0).minutes(0).seconds(0).milliseconds(0));
			//oDTP.settings.minDateTime = oDTP.getDateTimeStringInFormat('DateTime', 'yyyy-MM-dd HH:mm', new Date());
		}, */
		settingValueOfElement: function(sElemValue, dElemValue, oElem){ // 시작 <--> 종료 컨트롤.
			var oDTP = this;
			//if(oElem.hasClass(startDateTimeClass)){
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

$(document).ready(function() {
	if('ADD' == pageType && 'CT' == '${sessionScope.loginDto.authority}'){
		setShipto('${shipto.SHIPTO_CD}', '${shipto.SHIPTO_NM}', '${shipto.ZIP_CD}', '${shipto.ADD1}', '${shipto.ADD2}', '${shipto.ADD3}');
	}
});

//납품처 선택 팝업 띄우기.
function openShiptoPop(obj){
// 	var selectedCustCd = toStr($('input[name="m_custcd"]').val());
// 	if('' == selectedCustCd){
// 		alert('거래처를 선택 후 진행해 주세요.');
// 		return;
// 	}

	if(!isApp()){
		// 팝업 세팅.
		var widthPx = 795;
		var heightPx = 652;
		
		var sw=screen.width;
		var sh=screen.height;
		var px=(sw-widthPx)/2;
		var py=(sh-heightPx)/2;
		
		var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx+', top='+py+', left='+px;
		
		$('form[name="frm_pop"]').remove();
		
		var htmlText = '';
		htmlText += '<form name="frm_pop" method="post" target="shiptoListPop">';
		htmlText += '	<input type="hidden" name="pop" value="1" />';
		htmlText += '	<input type="hidden" name="page_type" value="orderadd" />';
		htmlText += '	<input type="hidden" name="r_multiselect" value="false" />'; // 행 단위 선택 가능여부 T/F
		//htmlText += '	<input type="hidden" name="r_custcd" value="'+selectedCustCd+'" />';
		htmlText += '</form>';
		$('body').append(htmlText);
		
		// #POST# 팝업 열기.
		var popUrl = '${url}/front/base/pop/shiptoListPop.lime';
		window.open('', 'shiptoListPop', options);
		$('form[name="frm_pop"]').prop('action', popUrl);
		$('form[name="frm_pop"]').submit().remove();
	}
	else{
		// 모달팝업 >> 새롭게 페이지 추가.
		//$('#shiptoListPopMId').modal('show');
		var link = '${url}/front/base/pop/shiptoListPop.lime?page_type=orderadd&r_multiselect=false&layer_pop=Y&';
		// 부모창refresh
		$(document).on('hidden.bs.modal', function (e) { // bootstrap modal refresh
	       $(e.target).removeData('bs.modal');
	    });
		$('#shiptoListPopMId').modal({
			remote: link
		});
	}

}

// return 납품처 팝업에서 개별 선택.
function setShiptoFromPop(jsonData){
	setShipto(toStr(jsonData.SHIPTO_CD), toStr(jsonData.SHIPTO_NM), toStr(jsonData.ZIP_CD), toStr(jsonData.ADD1), toStr(jsonData.ADD2), toStr(jsonData.ADD3));
}
function setShipto(shipto_cd, shipto_nm, zip_cd, add1, add2, add3){
	$('input[name="m_shiptocd"]').val(shipto_cd);
	$('input[name="v_shiptonm"]').val(shipto_nm);
	$('input[name="m_zipcd"]').val(zip_cd);
	$('input[name="m_add1"]').val(add1);
	$('input[name="m_add2"]').val('');
	//$('input[name="m_add2"]').val(add2);
	
	var tels = toStr(add3);
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

//납품처 초기화.
function setDefaultShipTo(){
	$('input[name="m_shiptocd"]').val('');
	$('input[name="v_shiptonm"]').val('');
}

// 주소록 선택 팝업 띄우기.
function openOrderAddressBookmarkPop(obj){
	if(!isApp()){
		// 팝업 세팅.
		var widthPx = 955;
		var heightPx = 733;
		
		var sw=screen.width;
		var sh=screen.height;
		var px=(sw-widthPx)/2;
		var py=(sh-heightPx)/2;
		
		var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx+', top='+py+', left='+px;
		
		$('form[name="frm_pop"]').remove();
		
		var htmlText = '';
		htmlText += '<form name="frm_pop" method="post" target="orderAddressBookmarkPop">';
		htmlText += '	<input type="hidden" name="pop" value="1" />';
		htmlText += '	<input type="hidden" name="page_type" value="orderadd" />';
		htmlText += '</form>';
		$('body').append(htmlText);
		
		// #POST# 팝업 열기.
		var popUrl = '${url}/front/base/pop/orderAddressBookmarkPop.lime';
		window.open('', 'orderAddressBookmarkPop', options);
		$('form[name="frm_pop"]').prop('action', popUrl);
		$('form[name="frm_pop"]').submit().remove();
	}
	else{
		// 모달팝업
		//$('#openOrderAddressBookmarkPopMId').modal('show');
		var link = '${url}/front/base/pop/orderAddressBookmarkPop.lime?page_type=orderadd&layer_pop=Y&';
		// 부모창refresh
		$(document).on('hidden.bs.modal', function (e) { // bootstrap modal refresh
	       $(e.target).removeData('bs.modal');
	    });
		$('#openOrderAddressBookmarkPopMId').modal({
			remote: link
		});
	}
}

// return 주소록 팝업에서 개별 선택.
function setOrderAddressBookmarkFromPop(jsonData){
	$('input[name="m_zipcd"]').val(toStr(jsonData.OAB_ZIPCD));
	$('input[name="m_add1"]').val(escapeXss(toStr(jsonData.OAB_ADD1)));
	$('input[name="m_add2"]').val(escapeXss(toStr(jsonData.OAB_ADD2)));
	$('input[name="m_receiver"]').val(escapeXss(toStr(jsonData.OAB_RECEIVER)));
	$('input[name="m_tel1"]').val(toStr(jsonData.OAB_TEL1));
	$('input[name="m_tel2"]').val(toStr(jsonData.OAB_TEL2));
}

// 관련품목 팝업 띄우기.
function openRecommendItemPop(obj, itr_itemcd){
	if(!isApp()){
		// 팝업 세팅.
		var widthPx = 955;
		var heightPx = 738;
		
		var sw=screen.width;
		var sh=screen.height;
		var px=(sw-widthPx)/2;
		var py=(sh-heightPx)/2;
		
		var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx+', top='+py+', left='+px;
		
		$('form[name="frm_pop"]').remove();
		
		var htmlText = '';
		htmlText += '<form name="frm_pop" method="post" target="itemRecommendPop">';
		htmlText += '	<input type="hidden" name="pop" value="1" />';
		htmlText += '	<input type="hidden" name="page_type" value="orderadd" />';
		htmlText += '	<input type="hidden" name="r_itritemcd" value="'+itr_itemcd+'" />';
		htmlText += '	<input type="hidden" name="r_multiselect" value="true" />'; // 행 단위 선택 가능여부 T/F.
		htmlText += '</form>';
		$('body').append(htmlText);
		
		// #POST# 팝업 열기.
		var popUrl = '${url}/front/base/pop/itemRecommendPop.lime';
		window.open('', 'itemRecommendPop', options);
		$('form[name="frm_pop"]').prop('action', popUrl);
		$('form[name="frm_pop"]').submit().remove();
	}
	else{
		// 모달팝업
		//$('#openRecommendItemPopMId').modal('show');
		var link = '${url}/front/base/pop/itemRecommendPop.lime?page_type=orderadd&r_itritemcd='+itr_itemcd+'&r_multiselect=true&layer_pop=Y&';
		// 부모창refresh
		$(document).on('hidden.bs.modal', function (e) {   // bootstrap modal refresh
	       $(e.target).removeData('bs.modal');
	    });
		$('#openRecommendItemPopMId').modal({
			remote: link
		});
	 }
}

// 품목 선택 팝업 띄우기.
function openItemPop(obj){
	if(!isApp()){
		// 팝업 세팅.
		var widthPx = 1050;
		var heightPx = 738;
		
		var sw=screen.width;
		var sh=screen.height;
		var px=(sw-widthPx)/2;
		var py=(sh-heightPx)/2;
		
		var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx+', top='+py+', left='+px;
		
		$('form[name="frm_pop"]').remove();
		
		var htmlText = '';
		htmlText += '<form name="frm_pop" method="post" target="itemListPop">';
		htmlText += '	<input type="hidden" name="pop" value="1" />';
		htmlText += '	<input type="hidden" name="page_type" value="orderadd" />';
		htmlText += '	<input type="hidden" name="r_multiselect" value="true" />'; // 행 단위 선택 가능여부 T/F.
		htmlText += '	<input type="hidden" name="r_checkitembookmark" value="N" />'; // 즐겨찾기 체크박스 N.
		htmlText += '</form>';
		$('body').append(htmlText);
		
		// #POST# 팝업 열기.
		var popUrl = '${url}/front/base/pop/itemListPop.lime';
		window.open('', 'itemListPop', options);
		$('form[name="frm_pop"]').prop('action', popUrl);
		$('form[name="frm_pop"]').submit().remove();
	}
	else{
		// 모달팝업 >> 새롭게 페이지 추가.
		//$('#openItemPopMId').modal('show');
		var link = '${url}/front/base/pop/itemListPop.lime?page_type=orderadd&r_multiselect=true&r_checkitembookmark=N&layer_pop=Y&';
		// 부모창refresh
		$(document).on('hidden.bs.modal', function (e) { // bootstrap modal refresh
	       $(e.target).removeData('bs.modal');
	    });
		$('#openItemPopMId').modal({
			remote: link
		});
	}
}

// return 품목 팝업에서 다중 선택. 
function setItemList(jsonArray){
	//console.log('jsonArray : ', jsonArray);
	var div = ($('div .full-desktop').css('display') == 'none') ? 'm' : '';
	
	$('#'+div+'noList').hide();
	
	var nowViewItemCd = '';
	$('#'+div+'itemListTbodyId').find('input[name="m_itemcd"]').each(function(i,e) {
		nowViewItemCd += $(e).val()+',';
	});
	
	var htmlText = '';
	var rowNum = $('#'+div+'itemListTbodyId').find('tr').length;
	for(var i=0,j=jsonArray.length; i<j; i++){
		var itemCd = jsonArray[i]['ITEM_CD'];
		
		if(0 > nowViewItemCd.indexOf(itemCd+',')){
			if('' == div){ // PC
				htmlText += '<tr class="itemListTrClass">';
				htmlText += '	<td class="rowNumClass">'+addComma(rowNum++)+'</td>';
				htmlText += '	<td class="text-left">';
				htmlText += '		'+itemCd;
				htmlText += '		<input type="hidden" name="m_itemcd" value="'+itemCd+'" />';
				htmlText += '	</td>';
				htmlText += '	<td class="text-left">'+jsonArray[i]['DESC1']+'</td>';
				htmlText += '	<td>';
				//htmlText += '		'+jsonArray[i]['UNIT'];
				htmlText += '		<input type="text" class="form-control text-center" name="m_unit" value="'+jsonArray[i]['UNIT']+'" onkeyup="checkByte(this, 3);" readonly="readonly" />';
				htmlText += '	</td>';
				htmlText += '	<td>';
				htmlText += '		<input type="text" class="form-control text-right amountClass2" name="m_quantity" value="" />';
				htmlText += '	</td>';
				htmlText += '	<td class="text-right">';
				htmlText += '		'+addComma(jsonArray[i]['ITI_PALLET']);
				htmlText += '	</td>';
				htmlText += '	<td>';
				htmlText += '		<input type="text" class="form-control text-right" name="m_fireproof" value="'+jsonArray[i]['FIREPROOF_YN']+'" readonly="readonly"/>';
				htmlText += '	</td>';
				htmlText += '	<td>';
				if(0 == Number(jsonArray[i]['RECOMMEND_ITEM_COUNT'])){
					htmlText += '-';
				}else{
					htmlText += '	<button type="button" class="btn btn-green" onclick=\'openRecommendItemPop(this, "'+itemCd+'");\'>보기</button>';
					//htmlText += '	<a href="javascript:;" onclick=\'openRecommendItemPop(this, "'+itemCd+'");)\'>'+addComma(jsonArray[i]['RECOMMEND_ITEM_COUNT'])+'</a>';
				}
				htmlText += '	</td>';
				htmlText += '	<td>';
				htmlText += '		<button type="button" class="btn btn-light-gray" onclick="delItem(this);">삭제하기</button>';
				htmlText += '	</td>';
				htmlText += '</tr>';
			}
			else{ // MOBILE
				htmlText += '<tr class="itemListTrClass">';
				htmlText += '	<td class="rowNumClass">'+addComma(rowNum++)+'</td>';
				htmlText += '	<td class="text-left">';
				htmlText += '		'+jsonArray[i]['DESC1'];
				htmlText += '		<input type="hidden" name="m_itemcd" value="'+itemCd+'" />';
				//htmlText += '		<input type="hidden" class="form-control" name="m_unit" value="'+jsonArray[i]['UNIT']+'" />';
				htmlText += '	</td>';
				htmlText += '	<td>';
				htmlText += '		<input type="text" class="form-control text-center" name="m_unit" value="'+jsonArray[i]['UNIT']+'" onkeyup="checkByte(this, 3);" readonly="readonly" />';
				htmlText += '	</td>';
				htmlText += '	<td>';
				htmlText += '		<input type="text" class="form-control text-right amountClass2" name="m_quantity" value="" />';
				htmlText += '	</td>';
				htmlText += '	<td>';
				htmlText += '		<input type="text" class="form-control text-right" name="m_fireproof" value="" readonly="readonly"/>';
				htmlText += '	</td>';
				htmlText += '	<td>';
				htmlText += '		<button type="button" class="btn btn-light-gray" onclick="delItem(this);">삭제하기</button>';
				htmlText += '	</td>';
				htmlText += '</tr>';
			}
		}
	} //for
	
	$('#'+div+'itemListTbodyId').append(htmlText);
	initAutoNumeric();
}

// 품목 삭제.
function delItem(obj){
	var div = ($('div .full-desktop').css('display') == 'none') ? 'm' : '';
	
	$(obj).closest('tr').remove();
	var rowCnt = $('#'+div+'itemListTbodyId').find('tr.itemListTrClass').length; // 삭제 후 tr 개수.
	
	if(0 == rowCnt){
		$('#'+div+'noList').show();
		return;
	}
	
	// 새롭게 넘버링.
	$('#'+div+'itemListTbodyId').find('tr.itemListTrClass').each(function(i,e){
		$(e).find('.rowNumClass').empty();
		$(e).find('.rowNumClass').append(addComma(i+1));
	});
}

// 최근주소 불러오기.
function getRecentOrderAddress(obj){
	$(obj).prop('disabled', true);
	
	if(confirm('최근에 주문한 주소를 불러 오시겠습니까?')){
		$.ajax({
			async : false,
			url : '${url}/front/order/getRecentOrderAjax.lime',
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

// 2024-11-28 hsg German Suplex 중복 클릭을 막기 위해 setTimeout 함수를 이용하도록 수정
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

//주문상태 변경.
function dataIn2(obj, status){

	$(obj).prop('disabled', true);

	var postalCodeChk = false;
	var params = {r_zipcd : $('input[name="m_zipcd"]').val()};
	$.ajax({
		async : false,
		url : '${url}/front/order/getPostalCodeCount.lime',
		cache : false,
		type : 'POST',
		dataType: 'json',
		data : params,
		success : function(data){
			if(data.useFlag === 'Y') {
				postalCodeChk = true;
			}
		},
		error : function(request,status,error){	
		}
	});
	//debugger;

	if(!postalCodeChk) {
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
	if('00' == status && '00' == toStr('${param.m_statuscd}')){ 
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
	
	// 전송전에 PC인경우 => Mobile 품목리스트 remove / Mobile인경우 => PC 품목리스트 remove.
	//var div = ($('div .full-desktop').css('display') == 'none') ? 'm' : '';
	var divReverse = ($('div .full-desktop').css('display') == 'none') ? '' : 'm'; // remove 하기위한 반대임. 
	$('#'+divReverse+'itemListTbodyId').find('tr.itemListTrClass').empty();
	$('#'+divReverse+'noList').show();
	
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
		
		var trObj = $('#itemListTbodyId > tr');
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
			url : '${url}/front/order/insertCustOrderAjax.lime',
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
							url : '${url}/front/order/setQmsFirstOrderAjax.lime',
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
	} else {
		$(obj).prop('disabled', false);
	}
}


function dataQMS(){
	var trObj = $('#itemListTbodyId > tr');
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


// 유효성 체크.
function dataValidation(){
	var div = ($('div .full-desktop').css('display') == 'none') ? 'm' : '';
	var ckflag = true;


	if(ckflag) ckflag = validation($('input[name="m_zipcd"]')[0], '납품 주소 우편번호', 'value');
	if(ckflag) ckflag = validation($('input[name="m_add1"]')[0], '납품 주소', 'value');
	//if(ckflag) ckflag = validation($('input[name="m_add2"]')[0], '납품 상세 주소', 'value');
	//if(ckflag) ckflag = validation($('input[name="m_tel1"]')[0], '연락처', 'value,alltlp');
	//if(ckflag) ckflag = validation($('input[name="m_tel2"]')[0], '연락처2', 'alltlp');
	

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

	if($('input[name="m_zipcd"]').val().length != 5) {
		alert('우편번호는 5자리만 입력 가능합니다.');
		ckflag = false;
		return false;
	}

	if(ckflag) ckflag = validation($('input[name="v_requestdate"]')[0], '납품요청일', 'value', '선택해 주세요.');
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
		var trObj = $('#'+div+'itemListTbodyId').find('tr.itemListTrClass');
		var rowCnt = $(trObj).length;
		if(0 >= rowCnt){
			alert('품목을 선택해 주세요.');
			ckflag = false;
		}
		else{
			$(trObj).each(function(i,e){
				ckflag = validation($(e).find('input[name="m_quantity"]')[0], '품목 수량', 'value');
				if(!ckflag) return false;
			});
		}
	}
	
	return ckflag;
}

// 자재주문서 출력 팝업 띄우기.
function viewOrderPaper(obj){
	
}


// 2024-11-07 hsg otterBro 공지 상세(크나우프석고보드 배송안내서) 팝업 띄우기.
function boardViewPop(obj, bdSeq){

	if(!isApp()){
		// 팝업 세팅.
		var widthPx = 1200;// 2024-11-08 hsg otterBro 요청사항 : 팝업 사이즈 조절 (955 -> 1000 -> 1200(2024-11-11))
		var heightPx = 920;// 2024-11-08 hsg otterBro 요청사항 : 팝업 사이즈 조절 (720 -> 920)
		var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx;
	
		$('form[name="frmNoticePop"]').find('input[name="r_bdseq"]').val(bdSeq);
	
		window.open('', 'noticeViewPop', options);
		$('form[name="frmNoticePop"]').prop('action', '${url}/front/board/noticeViewPop.lime');
		$('form[name="frmNoticePop"]').submit();
		
	}
	else{
		// 모달팝업
		//$('#boardViewPopMId').modal('show');
		var link = '${url}/front/board/noticeViewPop.lime?r_bdseq='+bdSeq+'&layer_pop=Y&';
		// 부모창refresh
		$(document).on('hidden.bs.modal', function (e) {   // bootstrap modal refresh
	       $(e.target).removeData('bs.modal');
	    });
		$('#boardViewPopMId').modal({
			remote: link
		});
	}
}


function postPopOpen(zone_name, addr1_name, addr2_name, zip_name, max_byte){
	if(!isApp()){
		openPostPop2(zone_name, addr1_name, addr2_name, zip_name, max_byte);
	}
	else{
		openPostPop2_layer(zone_name, addr1_name, addr2_name, zip_name, max_byte);
 		//return false;
	}
}

function moveOrderList(){
	formGetSubmit('${url}/front/order/orderList.lime', '');
}

function limitInputLength(inField) {
	let inputField = document.getElementById(inField);
	let inputValue = inputField.value;
	if(inputValue.length > 40) {
		alert("글자수를 40자 이상 초과하여 입력할 수 없습니다.");
		inputField.value=inputValue.slice(0, 40);
	}
}

</script>
</head>

<body>
<div id="post_layer" style="display: none; position: fixed; top: 105px; /* overflow: hidden; */ z-index: 9999; -webkit-overflow-scrolling: touch;">
	<img src="//t1.daumcdn.net/postcode/resource/images/close.png" id="btnCloseLayer" style="cursor: pointer; position: absolute; right: 5px; top: -35px; z-index: 1; padding: 5px; background: #000; border-radius: 100%;" onclick="closeDaumPostcode()" alt="닫기 버튼" />
</div>

<div id="subWrap" class="subWrap">
	<div id="ajax_indicator" style="display:none;">
	    <p style="position: absolute; top: 50%; left: 50%; margin: -110px 0 0 -110px;">
	        <img src="${url}/include/images/common/loadingbar.gif" />
	    </p>
	</div>
	
	<%@ include file="/WEB-INF/views/include/front/header.jsp" %>
	
	<%-- 팝업 전송 form --%>
	<form name="frmPop" method="post" target="itemViewPop">
		<input name="pop" type="hidden" value="1" />
		<input name="r_itemcd" type="hidden" value="" />
	</form>

	<%-- 2024-11-07 hsg otterBro 공지 상세(크나우프석고보드 배송안내서) 팝업 띄우기. 팝업 배송안내서 form --%>
	<form name="frmNoticePop" method="post" target="noticeViewPop">
		<input name="pop" type="hidden" value="1" />
		<input name="r_bdseq" type="hidden" value="" />
	</form>

	<!-- Header -->

	<div class="container-fluid">
		<div class="full-content">
		
			<div class="row no-m">
				<div class="page-breadcrumb"><strong>주문등록</strong></div>
				
				<div class="page-location">
					<ul>
						<li><a href="${url}/front/index/index.lime"><img src="${url}/include/images/front/common/location_home.png" alt="img" /></a></li>
						<li><a href="${url}/front/order/orderList.lime">웹주문현황</a></li>
						<li>
							<select onchange="formGetSubmit(this.value, '');">
								<option value="${url}/front/order/orderAdd.lime" <c:if test="${fn:contains(requestScope['javax.servlet.forward.servlet_path'], '/front/order/orderAdd.lime')}">selected="selected"</c:if> >주문등록</option>
								<option value="${url}/front/order/orderList.lime" <c:if test="${fn:contains(requestScope['javax.servlet.forward.servlet_path'], '/front/order/orderList.lime')}">selected="selected"</c:if> >웹주문현황</option>
								<option value="${url}/front/order/salesOrderMainList.lime" <c:if test="${fn:contains(requestScope['javax.servlet.forward.servlet_path'], '/front/order/salesOrderMainList.lime')}">selected="selected"</c:if> >거래내역(주문)</option>
								<option value="${url}/front/order/salesOrderItemList.lime" <c:if test="${fn:contains(requestScope['javax.servlet.forward.servlet_path'], '/front/order/salesOrderItemList.lime')}">selected="selected"</c:if> >거래내역(품목)</option>
							</select>
						</li>
					</ul>
				</div>
			</div> <!-- Row -->
			
		</div> <!-- Full Content -->
	</div> <!-- Container Fluid -->
	
	<!-- Container -->
	<main class="container" id="container">
		<form name="frm" method="post" >
		<input type="hidden" name="r_reqno" value="${param.r_reqno}" /> <%-- 주문번호 --%>
		<input type="hidden" name="m_statuscd" value="" /> <%-- 상태값 99=임시저장,00=주문접수 --%>
		<input type="hidden" name="m_accessdevice" value="" /> <%-- 접근한 디바이스 1=PC웹, 2=모바일 --%>
		<input type="hidden" id="m_reqNo" name="m_reqNo" value="" /> <%-- 주문일자 --%>
		<input type="hidden" id="m_qmsTempId" name="m_qmsTempId" value="" /> <%-- 임시 QMS 번호 --%>
		
		<%-- Create an empty container for the picker popup. --%>
		<div id="dateTimePickerDivId"></div>
		
		<!-- Content -->
		<div class="content">
		
			<!-- Row -->
			<div class="row">
				<!-- Col-md-12 -->
				<div class="col-md-12">
				
					<div class="boardViewArea">
						<h2 class="title">
							<c:if test="${'EDIT' eq pageType}"><span class="state">${orderStatus[custOrderH.STATUS_CD]}</span></c:if>
							<em>주문정보입력</em>
							<div class="title-right little">
								<%-- <button type="button" class="btn btn-green" onclick="alert('Ready');">자재주문서</button> --%>
								<c:if test="${'00' eq param.m_statuscd}">
									<button type="button" class="btn btn-yellow" onclick="dataIn(this, '00');">수정</button> <%-- 주문접수 수정--%>
								</c:if>
								<c:if test="${'00' ne param.m_statuscd}">
									<button type="button" class="btn btn-green order-save-btn" onclick="dataIn(this, '00');">${orderStatus['00']}</button> <%-- 주문접수 --%>
									<c:if test="${'ADD' eq pageType}"><button type="button" class="btn btn-yellow qmspop-btn" style="display:none;" onclick="dataQMS();">QMS 입력</button></c:if> <%-- QMS 입력 --%>
									<c:if test="${'ADD' eq pageType}"><button type="button" class="btn btn-gray order-save-btn" onclick="dataIn(this, '99');">${orderStatus['99']}</button></c:if> <%-- 임시저장 --%>
									<c:if test="${'EDIT' eq pageType}"><button type="button" class="btn btn-yellow" onclick="dataIn(this, '99');">수정</button></c:if> <%-- 임시저장 수정 --%>
								</c:if>
								<%-- 2024-11-07 hsg otterBro 크나우프석고보드(bd_seq:2262) 배송안내서 팝업. 배송비 안내 버튼. --%>
								<button type="button" class="btn btn-green order-save-btn" onclick="boardViewPop(this, '2262');">배송안내서</button> <%-- 배송안내서 --%>
								<button type="button" class="btn-list" onclick="location.href='${url}/front/order/orderList.lime'"><img src="${url}/include/images/front/common/icon_list@2x.png" alt="img" /></button>
							</div>
						</h2>
						 
						<div class="boardView">
							<ul>
								<li class="half">
									<label class="view-h">거래처명</label>
									<div class="view-b">${sessionScope.loginDto.custNm}</div>
								</li>
								<li class="half">
									<label class="view-h">주문자 / 주문일자</label>
									<div class="view-b">${sessionScope.loginDto.userNm} / ${todayDate}</div>
								</li>
								<li>
									<label class="view-h">납품처</label>
									<div class="view-b">
										<c:set var="shiptoCd">${custOrderH.SHIPTO_CD}</c:set>
										<c:if test="${'0' eq shiptoCd}"><c:set var="shiptoCd" value="" /></c:if>
											
										<c:choose>
											<c:when test="${'CO' eq sessionScope.loginDto.authority}"><%-- 거래처 --%>
												<input type="text" class="form-control form-sm" name="m_shiptocd" placeholder="납품처코드" value="${shiptoCd}" readonly="readonly" onclick="openShiptoPop(this);" />
												<input type="text" class="form-control form-xl marR0 search" name="v_shiptonm" placeholder="납품처명" value="${custOrderH.SHIPTO_NM}" readonly="readonly" onclick="openShiptoPop(this);" /> <!-- 795, 652 -->
												<button type="button" class="btn btn-reset" onclick="setDefaultShipTo();">초기화</button>
											</c:when>
											<c:otherwise><%-- 납품처 --%>
												${sessionScope.loginDto.shiptoNm} (${sessionScope.loginDto.shiptoCd})
												<input type="hidden" name="m_shiptocd" value="${sessionScope.loginDto.shiptoCd}" />
											</c:otherwise>
										</c:choose>
									</div>
								</li>
								<li class="address">
									<label class="view-h">납품주소<i class="icon-necessary">*</i></label>
									<div class="view-b">
										<input type="text" class="form-control form-sm numberClass" name="m_zipcd" placeholder="우편번호" value="${custOrderH.ZIP_CD}" onkeyup="checkByte(this, '12')"; />
										<button type="button" class="btn btn-dark-gray" onclick="postPopOpen('m_zipcd', 'm_add1', 'm_add2', '', '40');">우편번호</button>
										
										<button type="button" class="btn btn-default" onclick="getRecentOrderAddress(this);">최근주소</button>
										<button type="button" class="btn btn-default" onclick="openOrderAddressBookmarkPop(this);">주소록</button> <!-- 955, 655 -->
										<div class="table-checkbox pull-right">
											<label class="lol-label-checkbox" for="checkbox">
												<input type="checkbox" id="checkbox" name="r_savebookmark" value="Y" <c:if test="${'Y' eq param.r_savebookmark}">checked="checked"</c:if>  />
												<span class="lol-text-checkbox">주소록저장</span>
											</label>
										</div>
										<input type="text" class="form-control form-lg" name="m_add1" placeholder="주소" value="${custOrderH.ADD1}" onkeyup="checkByte(this, '180')"; />
										<input type="text" class="form-control form-md" name="m_add2" placeholder="상세주소" value="${custOrderH.ADD2}" onkeyup="checkByte(this, '60')"; />
									</div>
								</li>
								<li class="half">
									<label class="view-h">인수자명</label>
									<div class="view-b">
										<input type="text" class="form-control" name="m_receiver" value="${custOrderH.RECEIVER}" onkeyup="checkByte(this, '40')"; />
									</div>
								</li>
								<li class="half">
									<label class="view-h">연락처<i class="icon-necessary">*</i> / 연락처2</label>
									<div class="view-b">
										<input type="text" class="form-control form-md" name="m_tel1" placeholder="숫자만 입력해 주세요." value="${custOrderH.TEL1}" onkeyup="checkByte(this, '40')"; />
										<c:if test="${!empty custOrderH.TEL1}"><span class="warning"><i class="icon-necessary">※</i>숫자만 입력해 주세요.</span></c:if>
										<input type="text" class="form-control form-md" name="m_tel2" placeholder="숫자만 입력해 주세요." value="${custOrderH.TEL2}" onkeyup="checkByte(this, '40')"; />
										<c:if test="${!empty custOrderH.TEL2}"><span class="warning"><i class="icon-necessary">※</i>숫자만 입력해 주세요.</span></c:if>
									</div>
								</li>
								<li class="half">
									<label class="view-h">납품요청일<i class="icon-necessary">*</i></label>
									<div class="view-b">
										<fmt:parseDate value="${custOrderH.REQUEST_DT}" var="requestDate" pattern="yyyyMMdd"/>
										<fmt:parseDate value="${custOrderH.REQUEST_TIME}" var="requestTime" pattern="HHmm"/>
										<c:set var="v_requestdatedt"><fmt:formatDate value="${requestDate}" pattern="yyyy-MM-dd"/> <fmt:formatDate value="${requestTime}" pattern="HH:mm"/></c:set>
										
										<input type="text" class="form-control calendar form-md bdStartDateClass" name="v_requestdate" data-field="datetime" data-startend="start" data-startendelem=".bdEndDateClass" value="${v_requestdatedt}" readonly="readonly" />
										<input type="hidden" name="m_requestdt" value="<fmt:formatDate value="${requestDate}" pattern="yyyyMMdd"/>" /> <%-- YYYYMMDD --%>
										<input type="hidden" name="m_requesttime" value="<fmt:formatDate value="${requestTime}" pattern="HHmm"/>" /> <%-- HHMM --%>
										<!-- 
										<select class="form-control form-xs">
											<option>00시</option>
										</select>
										<select class="form-control form-xs">
											<option>00분</option>
										</select>
										 -->
									</div>
								</li>
								<li class="half">
									<label class="view-h">운송수단</label>
									<div class="view-b">
										<div class="table-radio">
											<label class="lol-label-radio" for="radio4">
												<input type="radio" id="radio4" name="m_transty" value="AA" <c:if test="${empty custOrderH.TRANS_TY or 'AA' eq custOrderH.TRANS_TY}">checked="checked"</c:if> />
												<span class="lol-text-radio">기본운송</span>
											</label>
											<label class="lol-label-radio" for="radio5">
												<input type="radio" id="radio5" name="m_transty" value="AB" <c:if test="${'AB' eq custOrderH.TRANS_TY}">checked="checked"</c:if> />
												<span class="lol-text-radio">자차운송 (주문처운송)</span>
											</label>
										</div>
									</div>
								</li>
								<li>
									<label class="view-h">요청사항</label>
									<div class="view-b">
										<!-- <input type="text" class="form-control" name="m_remark" placeholder="한글 30자 이내로 적어주세요" value="${custOrderH.REMARK}" onkeyup="checkByte(this, '60');" /> -->
										<input type="text" class="form-control" name="m_remark" value="${custOrderH.REMARK}" onkeyup="checkByte(this, '40');" />
										<!-- <input type="text" class="form-control" name="m_remark" id="m_remark" placeholder="글자수를 40자 이내로 제한합니다." value="${custOrderH.REMARK}" 
											onkeyup="limitInputLength('m_remark')" /> -->
									</div>
								</li>
							</ul>
						</div> <!-- boardView -->
						
					</div> <!-- boardViewArea -->
					
					<div class="boardListArea">
						<h2 class="title">
							주문품목
							<div class="title-right little">
								<button type="button" class="btn btn-line" onclick="openItemPop(this);">품목선택</button> <!-- 955,655 -->
							</div>
						</h2>
						
						<div class="boardList">
							<!-- desktop -->
							<table class="full-desktop" width="100%" cellpadding="0" cellspacing="0" border="0">
								<colgroup>
									<col width="5%" />
									<col width="10%" />
									<col width="28%" />
									<col width="10%" />
									<col width="10%" />
									<col width="10%" />
									<col width="9%" />
									<col width="9%" />
									<col width="10%" />
								</colgroup>
								<thead>
									<tr>
										<th>NO</th>
										<th>품목코드</th>
										<th>품목명</th>
										<th>단위</th>
										<th>수량</th>
										<th>파레트적재단위</th>
										<th>내화구조</th>
										<th>관련품목</th>
										<th>기능</th>
									</tr>
								</thead>
								<tbody id="itemListTbodyId">
									<!-- ############################################### -->
									<%-- [주의] 수정시 자바스크립트 setItemList() 함수도 수정해 주세요. --%>
									<!-- ############################################### -->
									<c:forEach items="${custOrderD}" var="list" varStatus="status">
										<tr class="itemListTrClass">
											<td class="rowNumClass"><fmt:formatNumber value="${status.count}" pattern="#,###" /></td>
											<td class="text-left">
												${list.ITEM_CD}
												<input type="hidden" name="m_itemcd" value="${list.ITEM_CD}" />
											</td>
											<td class="text-left">${list.DESC1}</td>
											<td>
												<input type="text" class="form-control text-center" name="m_unit" value="${list.UNIT}" onkeyup="checkByte(this, 3);" readonly="readonly" />
											</td>
											<td>
												<input type="text" class="form-control text-right amountClass2" name="m_quantity" value="${list.QUANTITY}" />
											</td>
											<td class="text-right">
												<fmt:formatNumber value="${list.ITI_PALLET}" type="number" pattern="#,###.##" />
											</td>
											<td>
												<input type="text" class="form-control text-right" name="m_fireproof" value="${list.FIREPROOF_YN}" readonly="readonly"/>
											</td>
											<td>
												<c:choose>
													<c:when test="${0 eq cvt:toInt(list.RECOMMEND_ITEM_COUNT)}">-</c:when>
													<c:otherwise>
														<button type="button" class="btn btn-green" onclick="openRecommendItemPop(this, '${list.ITEM_CD}');">보기</button>
														<%-- <a href="javascript:;" onclick="openRecommendItemPop(this, '${list.ITEM_CD}')"><fmt:formatNumber value="${list.RECOMMEND_ITEM_COUNT}" pattern="#,###" /></a> --%>
													</c:otherwise>
												</c:choose>
											</td>
											<td>
												<button type="button" class="btn btn-light-gray" onclick="delItem(this);">삭제하기</button>
											</td>
										</tr>
									</c:forEach>
									
									
									<tr id="noList" <c:if test="${!empty custOrderD}">style="display:none;"</c:if>>
										<td colspan="8" class="list-empty">
											<img src="${url}/include/images/front/common/warning.png" alt="img" /><br />
											주문품목을 선택해 주세요.
										</td>
									</tr>
								
								</tbody>
							</table>
							
							<!-- mobile -->
							<table class="full-mobile" width="100%" cellpadding="0" cellspacing="0" border="0">
								<colgroup>
									<col width="10%" />
									<col width="40%" />
									<col width="15%" />
									<col width="15%" />
									<col width="20%" />
								</colgroup>
								<thead>
									<tr>
										<th>NO</th>
										<th>품목명</th>
										<th>단위</th>
										<th>수량</th>
										<th style="display:none">내화구조</th>
										<th>기능</th>
									</tr>
								</thead>
								<tbody id="mitemListTbodyId">
									<!-- ############################################### -->
									<%-- [주의] 수정시 자바스크립트 setItemList() 함수도 수정해 주세요. --%>
									<!-- ############################################### -->
									<c:forEach items="${custOrderD}" var="list" varStatus="status">
										<tr class="itemListTrClass">
											<td class="rowNumClass"><fmt:formatNumber value="${status.count}" pattern="#,###" /></td>
											<td class="text-left">
												${list.DESC1}
												<input type="hidden" name="m_itemcd" value="${list.ITEM_CD}" />
												<%-- <input type="hidden" class="form-control" name="m_unit" value="${list.UNIT}" /> --%>
											</td>
											<td>
												<input type="text" class="form-control text-center" name="m_unit" value="${list.UNIT}" onkeyup="checkByte(this, 3);" readonly="readonly" />
											</td>
											<td>
												<input type="text" class="form-control text-right amountClass2" name="m_quantity" value="${list.QUANTITY}" />
											</td>
											<td style="display:none">
												<input type="text" class="form-control text-right" name="m_fireproof" value="${list.FIREPROOF_YN}" readonly="readonly"/>
											</td>
											<td>
												<button type="button" class="btn btn-light-gray" onclick="delItem(this);">삭제하기</button>
											</td>
										</tr>
									</c:forEach>
									
									
									<tr id="mnoList" <c:if test="${!empty custOrderD}">style="display:none;"</c:if>>
										<td colspan="5" class="list-empty">
											<img src="${url}/include/images/front/common/warning.png" alt="img" /><br />
											주문품목을 선택해 주세요.
										</td>
									</tr>
									
								</tbody>
							</table>
							
							<h2>
								<p class="summary">※ 생산요청 제품을 선택하신 경우에는 <strong>요청사항</strong>란에 제품의 규격 및 세부사항을 반드시 입력해 주시기 바랍니다.</p>
								<div class="title-right little marT0">
									<%-- <button type="button" class="btn btn-green" onclick="alert('Ready');">자재주문서</button> --%>
									<c:if test="${'00' eq param.m_statuscd}">
										<button type="button" class="btn btn-yellow" onclick="dataIn(this, '00');">수정</button> <%-- 주문접수수정 --%>
									</c:if>
									<c:if test="${'00' ne param.m_statuscd}">
										<button type="button" class="btn btn-green order-save-btn" onclick="dataIn(this, '00');">${orderStatus['00']}</button> <%-- 주문접수 --%>
										<c:if test="${'ADD' eq pageType}"><button type="button" class="btn btn-yellow qmspop-btn" style="display:none;" onclick="dataQMS();">QMS 입력</button></c:if> <%-- QMS 입력 --%>
										<c:if test="${'ADD' eq pageType}"><button type="button" class="btn btn-gray order-save-btn" onclick="dataIn(this, '99');">${orderStatus['99']}</button></c:if> <%-- 임시저장 --%>
										<c:if test="${'EDIT' eq pageType}"><button type="button" class="btn btn-yellow" onclick="dataIn(this, '99');">수정</button></c:if> <%-- 임시저장 수정 --%>
									</c:if>
									<button type="button" class="btn-list" onclick="location.href='${url}/front/order/orderList.lime'"><img src="${url}/include/images/front/common/icon_list@2x.png" alt="img" /></button>
								</div>
							</h2>
						</div> <!-- boardList -->
						
					</div> <!-- boardListArea -->
					
					<section>
						<c:if test="${!empty main2BannerList}">
							<div class="banArea"><!-- 1300 * 220 -->
								<ul id="content-slider" class="content-slider">
									<c:forEach items="${main2BannerList}" var="bn2List" varStatus="stat">
										<li>
											<c:if test="${bn2List.BN_LINKUSE eq 'Y'}">
												<a <c:if test="${!empty bn2List.BN_LINK}">href="${bn2List.BN_LINK}" target="_blank"</c:if> <c:if test="${empty bn2List.BN_LINK}">href="javascript:;"</c:if> >
													<img class="hide-xxs" src="${url}/data/banner/${bn2List.BN_IMAGE}"  width="1300" height="220" onerror="this.src='${url}/include/images/front/content/none_product.png'" alt="${bn2List.BN_ALT}" />
													<img class="hide500" src="${url}/data/banner/${bn2List.BN_MOBILEIMAGE}"  width="500" height="150" onerror="this.src='${url}/include/images/front/content/none_product.png'" alt="${bn2List.BN_ALT}" />
												</a>
											</c:if>
											<c:if test="${bn2List.BN_LINKUSE eq 'N'}">
												<img class="hide-xxs" src="${url}/data/banner/${bn2List.BN_IMAGE}"  width="1300" height="220" onerror="this.src='${url}/include/images/front/content/none_product.png'" alt="${bn2List.BN_ALT}" />
												<img class="hide500" src="${url}/data/banner/${bn2List.BN_MOBILEIMAGE}"  width="500" height="150" onerror="this.src='${url}/include/images/front/content/none_product.png'" alt="${bn2List.BN_ALT}" />
											</c:if>
										</li>
									</c:forEach>
								</ul>
							</div>
						</c:if>
					</section>
					
				</div> <!-- Col-md-12 -->
			</div> <!-- Row -->
			
		</div> <!-- Content -->
		
		</form>
	</main> <!-- Container -->
	
	<%@ include file="/WEB-INF/views/include/front/bottom.jsp" %>
	
	<%@ include file="/WEB-INF/views/include/front/footer.jsp" %>
	
	<!-- Modal --> <!-- 납품처 선택 -->
	<div class="modal fade" id="shiptoListPopMId" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-dialog modal-lg">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><img src="${url}/include/images/front/common/total_menu_close_icon.png" width="20" height="20" alt="close" /></button>
			</div>
			<div class="modal-content">
				
			</div>
		</div>
	</div>
	
	<!-- Modal --> <!-- 주소록 선택 -->
	<div class="modal fade" id="openOrderAddressBookmarkPopMId" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-dialog modal-lg">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><img src="${url}/include/images/front/common/total_menu_close_icon.png" width="20" height="20" alt="close" /></button>
			</div>
			<div class="modal-content">
				
			</div>
		</div>
	</div>
	
	<!-- Modal --> <!-- 관련품목 보기 -->
	<div class="modal fade" id="openRecommendItemPopMId" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-dialog modal-lg">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><img src="${url}/include/images/front/common/total_menu_close_icon.png" width="20" height="20" alt="close" /></button>
			</div>
			<div class="modal-content">
				
			</div>
		</div>
	</div>
	
	<!-- Modal --> <!-- 품목선택 -->
	<div class="modal fade" id="openItemPopMId" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-dialog modal-lg">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><img src="${url}/include/images/front/common/total_menu_close_icon.png" width="20" height="20" alt="close" /></button>
			</div>
			<div class="modal-content">
				
			</div>
		</div>
	</div>
	
</div> <!-- Wrap -->

</body>
</html>