<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/front/commonimport.jsp" %>
<!DOCTYPE HTML>
<!--[if IE 8]><html lang="ko" class="ie8"><![endif]-->
<!--[if gt IE 8]><!--><html lang="ko"><!--<![endif]-->
<head>
<%@ include file="/WEB-INF/views/include/front/commonhead.jsp" %>
<!-- ↓↓↓↓↓↓↓↓↓ 2025-04-18 hsg Italian Stretch No.32 : E-Order Admin 주문등록 Page에서 주문등록 버튼 클릭 시 한번 더 확인 후 주문 접수 할 수 있도록 변경하기 위해 모달 팝업 추가 ↓↓↓↓↓↓↓↓↓ -->
<style>
  /* 반투명 배경 */
  #modalOverlay {
    position: fixed; top:0; left:0; right:0; bottom:0;
    background: rgba(0,0,0,0.5); z-index:1000;
    display:none;
  }

/* 모달팝업에 쓰이는 클래스명이 modern.css 에 정의된 클래스명과 동일, 불필요한 영향으로 오작동, 클래스명 변경. 2025-05-30 ijy */
/* 모달팝업 높이가 브라우저에 따라 조절됨. 브라우저 영향을 없애고 본문 내용에 따라 길어짐. 최대 길이 넘어서면 스크롤바 2025-05-30 ijy */
.modal2 {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 550px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.2);
    z-index: 1001;
    overflow: hidden;
    font-family: Arial, sans-serif;
    padding-bottom: 60px; /* 푸터 공간 확보 */
    display: flex; /* Flexbox 컨테이너로 설정 */
    flex-direction: column; /* 자식 요소들을 세로로 정렬 */
    max-height: 80vh; /* 뷰포트 높이의 80%를 최대 높이로 제한 */
    /* 만약 내용이 짧으면 80vh보다 작아집니다. */
}
.modal-header2 {
    padding: 10px 15px;
    background: #f5f5f5;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.modal-header2 H3 {
	font-size: 21px;
}
.modal-body2 {
    padding: 10px 15px 15px 15px;
    overflow-y: auto; /* 내용이 넘칠 경우 세로 스크롤바 생성 */
    flex-grow: 1; /* 남은 공간을 모두 차지하도록 설정 */
    /* 내용이 짧으면 modal-body2 높이도 줄어듭니다. */
}

/* 2025-05-13 hsg: modal 내 품목 텍스트 정렬용 */
#modalItems {
  display: inline-block;
  width: calc(100% - 60px); /* 레이블 폭만큼 공간 비워두기 */
  vertical-align: top;
}
/* 줄바꿈(<br>) 이후 자동으로 레이블 폭만큼 들여쓰기 */
#modalItems br {
  display: block;
  margin-left: 60px;      /* 레이블(‘품목 :’) 실제 너비에 맞춰 조절 */
}

/* 모달팝업 각 항목별 좌우 간격 일치화 2025-05-30 ijy */
.modal-body2 .modal-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 5px;
}
.modal-body2 .modal-label {
  width: 80px;
  box-sizing: border-box;
  text-align: justify;
  padding-right: 10px;
  font-weight: bold;
}
.modal-body2 .modal-value {
  flex: 1;
  white-space: pre-wrap;
  word-break: break-word;
}
.modal-body2 .modal-row-last {
  display: flex;
  align-items: flex-start;
  margin-top: 20px;
}

.modal-footer2 {
    /* 기존 스타일 유지 */
    position: absolute;
    bottom: 0;
    left: 0; right: 0;
    height: 60px;
    background: #f5f5f5;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 12px;
    flex-shrink: 0; /* 푸터는 고정 높이 유지 */
}

/* 닫기 버튼 스타일 */
/* 실행 버튼 (파랑) */
.btn-execute {
  background-color: #007bff;
  color: #fff;
  border: 1px solid #0056b3;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
}

/* 취소 버튼 (회색) */
.btn-cancel {
  background-color: #6c757d;
  color: #fff;
  border: 1px solid #5a6268;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
}

/* 닫기 아이콘(×) */
.close-btn {
  cursor: pointer;
  font-size: 20px;
  line-height: 1;
}

/* 2025-06-04 ijy. 쿼테이션 검증 진행후 주문접수가 불가한 품목은 붉은색으로 표기 */
#itemListTbodyId .errorRow td{color: red;}
#mitemListTbodyId .errorRow td{color: red;}
#itemListTbodyId .errorRow td input{color: red;}
#mitemListTbodyId .errorRow td input{color: red;}




#weatherDiv {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex: 1;
  white-space: nowrap;
}

.weather-period {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
}

.weather-period .period {
  font-size: 12px;
  line-height: 1;
  margin: 3;
  padding: 3;
}

.weather-period .percent {
  font-size: 14px;
  font-weight: bold;
  line-height: 2;
  margin: 3;
  padding: 3;
}







/* 상단 D+7 날씨 정보 */

.weather-container {
    display: inline-flex;               /* 인라인 요소처럼 흐름에 맞게 배치되면서 내부는 flexbox 레이아웃 사용 */
    align-items: center;                /* 내부 아이템을 수직(교차축) 방향 가운데 정렬 */
    height: 40px;                       /* 전체 높이 고정 */
    margin: 7px 10px;                     /* 좌우 10px씩 여백 */
    padding: 0;                         /* 안쪽 여백 없음 */
    vertical-align: middle;             /* 인라인 요소끼리 수직 가운데 정렬 */
    align-content: center;
    max-width: none;                    /* 최대 너비 제한 없음 */
    overflow: hidden;                   /* 넘치는 내용은 숨김 */
}


.weather-week-forecast {
    display: flex;                      /* flexbox로 배치 */
    gap: 6px;                           /* 각 요일 카드 사이 간격 6px */
    align-items: center;                /* 세로 가운데 정렬 */
    padding: 0;                         /* 안쪽 여백 없음 */
    height: 100%;                       /* 부모 컨테이너 높이만큼 */
    margin: 0;                          /* 바깥 여백 없음 */
    overflow-x: auto;                   /* 가로로 넘칠 경우 스크롤 가능 */
}


.weather-day-card {
    flex: 0 0 auto;
    width: 130px;
    background: linear-gradient(135deg, #c7cfd1, #d6dbdd);
    border-radius: 10px;
    padding: 6px 2px 4px 2px;
    text-align: center;
    color: black;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    font-size: 10px;
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    align-content: center;
    align-items: center;
    justify-content: space-between;
    height: 40px; /* 높이 조정 */
}

.weather-day-card.today {
    background: linear-gradient(135deg, #ffd96b, #ffe07a);
}

.weather-day {
    font-size: 10px;
    margin-bottom: 2px;
    font-weight: bold;
    line-height: 1.1;
}

.weather-date {
    font-size: 10px;
    color: #000;
    margin-bottom: 2px;
}

.weather-pop {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: 2px;
}

.weather-pop-label {
    font-size: 10px;
    /*color: #dfe6e9;*/
    color: #000;
    line-height: 1;
}

.weather-pop-value {
    font-size: 11px;
    font-weight: bold;
    color: #000;
    line-height: 1.1;
}

.weather-main-row {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px; /* 아이콘과 온도 사이 간격 */
    margin-bottom: 2px;
}

.weather-icon-container {
    margin: 10px 0;
}

.weather-icon-row {
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    gap: 4px; /* 아이콘 사이 간격 */
    margin: 4px 0;
}

.weather-icon {
    width: 21px;
    height: 21px;
    display: block;
}

.weather-temps {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 1px;
}

.weather-temp-high, .weather-temp-low {
    font-size: 10px;
    line-height: 1.1;
}

.weather-rain {
    font-size: 9px;
    margin-top: 1px;
}

.weather-header-row {
    display: flex;
    align-items: center;
    gap: 6px; /* 도시명과 날씨 사이 간격 */
    flex-wrap: nowrap; /* 줄바꿈 방지 */
}
#cityName {
    font-weight: bold;
    font-size: 16px;
    white-space: nowrap; /* 도시명 줄바꿈 방지 */
    padding-left: 10px;
}
.weather-container {
    flex-shrink: 1;
    min-width: 0;
}

.header-row-flex {
    display: flex;
    align-items: center;
    gap: 24px; /* 제목-날씨-버튼 간격 */
    flex-wrap: nowrap;
    width: 100%;
    min-width: 0;
}

.header-title {
    margin: 0;
    font-size: 20px;
    font-weight: bold;
    white-space: nowrap; /* 제목 줄바꿈 방지 */
}

.page-right {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: auto; /* 오른쪽 끝으로 밀기 */
}


@media (max-width: 768px) {
    .weather-day-card {
        width: 100px;
        padding: 12px 8px;
    }
}

.weather-source {
    display: flex;
    flex-direction: row-reverse;
    align-items: flex-end;
    gap: 6px; /* 아이콘과 온도 사이 간격 */
  width: 1534px;
  text-align: right;
  font-size: 12px;
  color: #444;
  margin: 0 auto;
}

</style>

<!--  ↓↓↓↓↓↓↓ 2025-08-21 hsg 내화구조 제품 관련 팝업창 추가 : 내화구조 팝업 스타일 추가  ↓↓↓↓↓↓↓ -->
<!-- 내화구조 팝업 스타일 추가 -->
<style>
/* 내화구조 팝업 스타일 */
.fireproof-modal {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 600px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.2);
    z-index: 1001;
    overflow: hidden;
    font-family: Arial, sans-serif;
    padding-bottom: 70px;
    display: flex;
    flex-direction: column;
    max-height: 85vh;
}

.fireproof-modal-header {
    padding: 15px 20px;
    background: #f8f9fa;
    border-bottom: 1px solid #dee2e6;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.fireproof-modal-header h3 {
    font-size: 18px;
    font-weight: bold;
    margin: 0;
    color: #dc3545;
}

.fireproof-modal-body {
    padding: 20px;
    overflow-y: auto;
    flex-grow: 1;
    line-height: 1.6;
}

.fireproof-modal-body .notice-text {
    font-size: 14px;
    color: #333;
    margin-bottom: 15px;
}

.fireproof-modal-body .warning-text {
    font-size: 13px;
    color: #dc3545;
    margin-bottom: 20px;
    font-weight: bold;
}

.fireproof-modal-body .content-list {
    margin: 0;
    padding-left: 0;
    list-style: none;
}

.fireproof-modal-body .content-list li {
    margin-bottom: 12px;
    padding-left: 20px;
    position: relative;
    font-size: 13px;
    line-height: 1.5;
}

.fireproof-modal-body .content-list li:before {
    content: attr(data-num);
    position: absolute;
    left: 0;
    top: 0;
    font-weight: bold;
    color: #dc3545;
}

.fireproof-checkbox-area {
    margin-top: 20px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 5px;
    text-align: center;
}

.fireproof-checkbox-area label {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: bold;
    color: #495057;
    cursor: pointer;
}

.fireproof-checkbox-area input[type="checkbox"] {
    margin-right: 8px;
    transform: scale(1.2);
}

.fireproof-modal-footer {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 70px;
    background: #f8f9fa;
    border-top: 1px solid #dee2e6;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 15px;
    flex-shrink: 0;
}

.fireproof-btn-close {
    background-color: #6c757d;
    color: #fff;
    border: 1px solid #6c757d;
    border-radius: 4px;
    padding: 10px 20px;
    cursor: pointer;
    font-size: 14px;
    font-weight: bold;
}

.fireproof-btn-close:hover {
    background-color: #5a6268;
    border-color: #545b62;
}

/* 모바일 대응 */
@media (max-width: 768px) {
    .fireproof-modal {
        width: 90%;
        max-width: none;
    }

    .fireproof-modal-header h3 {
        font-size: 16px;
    }

    .fireproof-modal-body {
        padding: 15px;
    }
}
</style>
<!-- ↑↑↑↑↑↑↑  2025-08-21 hsg 내화구조 제품 관련 팝업창 추가 : 내화구조 팝업 스타일 추가 ↑↑↑↑↑↑↑  -->

<script>
  // 모달 열기
  function showModal(){
	  // 1) 폼의 input 값 읽어서 모달 스팬에 채우기
	  document.getElementById('modalCustNm').textContent   = '${sessionScope.loginDto.custNm}';
	  document.getElementById('modalShipTo').textContent   = document.querySelector('input[name="v_shiptonm"]').value;
	  document.getElementById('modalShipAddr').textContent = document.querySelector('input[name="m_add1"]').value + ' ' + document.querySelector('input[name="m_add2"]').value;
	  document.getElementById('modalShipDt').textContent   = document.querySelector('input[name="v_requestdate"]').value;
	  document.getElementById('modalPhone').textContent    = document.querySelector('input[name="m_tel1"]').value;
	  //document.getElementById('modalItem').textContent     = document.querySelector('input[name="v_item"]').value;
	  document.getElementById('modalRequest').textContent  = document.querySelector('input[name="m_remark"]').value;


	  // 2) <ul> 비우기
	  var itemsUl = document.getElementById('modalItems');
	  itemsUl.innerHTML = '';


	  // 3) 동적 테이블(#itemListTbodyId)에서 tr.itemListTrClass 순회
/* 	  document.querySelectorAll('#itemListTbodyId tr.itemListTrClass').forEach(function(tr) {
	    // 2번째 <td> 텍스트가 품목명, 숨겨진 input[name="m_itemcd"] 에 코드가 들어 있다고 가정
	    var itemName = tr.cells[2].textContent.trim();
	    var itemCd   = tr.querySelector('input[name="m_itemcd"]').value;

	    var li = document.createElement('li');
	    li.textContent = itemName + " [" + itemCd + "]";
	    itemsUl.appendChild(li);
	  });
 */
   document.querySelectorAll('#itemListTbodyId tr.itemListTrClass').forEach(function(tr, idx) {
	     var itemName = tr.cells[2].textContent.trim();
	     var quantity = tr.querySelector('input[name="m_quantity"]').value;

	     // 2025-05-13 hsg: 둘째 줄부터 줄바꿈만 찍어주면 CSS 가 들여쓰기 처리
	     if (idx > 0) itemsUl.innerHTML += '<br>';
	     // 2025-05-13 hsg: “품목명 / 수량 : xx” 형태로 출력
	     itemsUl.innerHTML += itemName + ' / 수량 : ' + quantity;
	   });

	//납품처 정보가 없으면 모달 팝업에서도 안보여주기 2025-05-22 ijy
	var shiptoVal = document.querySelector('input[name="v_shiptonm"]').value;
	if(shiptoVal == null || shiptoVal == ''){
		document.getElementById('modalShipToRow').style.display = 'none';
	} else {
		document.getElementById('modalShipToRow').style.display = 'flex';
	}

	  // 4) 모달 열기
    document.getElementById('modalOverlay').style.display = 'block';
    document.getElementById('modalLayer').style.display   = 'flex';
    document.body.style.overflow = 'hidden';

  }
  // 모달 닫기
  function closeModal(){
    document.getElementById('modalOverlay').style.display = 'none';
    document.getElementById('modalLayer').style.display   = 'none';
    document.body.style.overflow = '';
  }
  // 오버레이 클릭해도 닫기
  document.addEventListener('DOMContentLoaded', function(){
    document.getElementById('modalOverlay')
            .addEventListener('click', closeModal);
  });
  // 실제 주문 처리
  function confirmOrder(){
    closeModal();
    //document.forms['frm'].submit(); // 또는 dataIn 호출
    dataIn(this, '00');
  }
</script>
<!-- ↑↑↑↑↑↑↑↑↑ 2025-04-18 hsg Italian Stretch No.32 : E-Order Admin 주문등록 Page에서 주문등록 버튼 클릭 시 한번 더 확인 후 주문 접수 할 수 있도록 변경하기 위해 모달 팝업 추가 ↑↑↑↑↑↑↑↑↑ -->

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

	} else if ('EDIT' == pageType){
		//납품처 선택 시 해당 납품처에서 사용했던 모든 품목 조회. 수정화면 진입시 바로 품목 조회. 2025-05-22 ijy
		getShiptoCustOrderAllItemListAjax($('input[name="m_shiptocd"]').val());
	}

	weekWeatherForecastApi();
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
	setShipto(toStr(jsonData.SHIPTO_CD), toStr(jsonData.SHIPTO_NM), toStr(jsonData.ZIP_CD), toStr(jsonData.ADD1), toStr(jsonData.ADD2), toStr(jsonData.ADD3), toStr(jsonData.QUOTE_QT));
}
function setShipto(shipto_cd, shipto_nm, zip_cd, add1, add2, add3, quote_qt){
	$('input[name="m_shiptocd"]').val(shipto_cd);
	$('input[name="v_shiptonm"]').val(shipto_nm);
	$('input[name="m_zipcd"]').val(zip_cd);
	$('input[name="m_add1"]').val(add1);
	$('input[name="m_add2"]').val('');
	//$('input[name="m_add2"]').val(add2);
	$('input[name="v_shiptoqt"]').val(quote_qt); //2025-06-04 ijy. 쿼테이션 검증을 위한 쿼테이션 번호 추가

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

	setAddressShipTo();

	//납품처 선택 시 해당 납품처에서 사용했던 모든 품목 조회. 2025-05-22 ijy
	getShiptoCustOrderAllItemListAjax(shipto_cd);
}

//납품처 초기화.
function setDefaultShipTo(){
	$('input[name="m_shiptocd"]').val('');
	$('input[name="v_shiptonm"]').val('');
	$('input[name="v_shiptoqt"]').val(''); //2025-06-04 ijy. 쿼테이션 검증을 위한 쿼테이션 번호 추가

	setAddressShipTo();

	//납품처 초기화 시 사용 품목 기록도 초기화. 2025-05-22 ijy
	shiptoAllItemReset();
}

//주소 초기화.
function setAddressShipTo(){

	let b = ($('input[name="m_shiptocd"]').val().length > 0);

	if(!b){
		$('input[name="m_zipcd"]').val('');
		$('input[name="m_add1"]').val('');
		$('input[name="m_add2"]').val('');
		$('input[name="m_tel1"]').val('');
		$('input[name="m_tel2"]').val('');
	}

	setActivateShipTo(b);
}


//주소 활성화.
function setActivateShipTo(b){

	$('input[name="m_zipcd"]').prop('readonly', b);
	/*
	$('input[name="m_add1"]').prop('readonly', b);
	$('input[name="m_add2"]').prop('readonly', b);
	$('input[name="m_tel1"]').prop('readonly', b);
	$('input[name="m_tel2"]').prop('readonly', b);

	$('btn btn-dark-gray').prop('disabled', b);
	*/
	$('.address .view-b button').prop('disabled', b);
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

//2025-08-14 hsg Apple-Pie : 쿼테이션 시스템 구분 함수 추가
function classifyQuotationSystem(shiptoNm) {
    if (!shiptoNm) {
        return 'ZOBJ'; // 납품처명이 없으면 기존 시스템으로 간주
    }

    if (shiptoNm.trim().toUpperCase().startsWith('KR') && shiptoNm != 'KR산업') {
        return 'ZCPQ'; // OneCRM 시스템
    } else {
        return 'ZOBJ'; // 기존 시스템
    }
}

// 2025-08-14 hsg Apple-Pie : 쿼테이션 검증 필요 여부 확인 함수
function needQuotationVerification() {
    var shiptoNm = $('input[name="v_shiptonm"]').val();
    var quoteQt = $('input[name="v_shiptoqt"]').val();

    // 납품처명 기준으로 시스템 구분
    var systemType = classifyQuotationSystem(shiptoNm);

    console.log('납품처명:', shiptoNm, '시스템구분:', systemType);

    // ZCPQ 시스템이고 쿼테이션 번호가 있을 때만 검증 진행
    if (systemType === 'ZCPQ' && quoteQt != null && quoteQt != '') {
        return true;
    }

    return false;
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

	//납품처 미선택시 품목 검색 팝업 사용 불가 2025-05-22 ijy > 제거 요청. 2025-05-27 ijy
// 	var selectedShiptoCd = toStr($('input[name="m_shiptocd"]').val());
// 	if('' == selectedShiptoCd){
// 		alert('납품처를 선택해주세요.');
// 		return;
// 	}

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
				htmlText += '		<input type="hidden" class="form-control text-right" name="m_fireproof_item" value="'+jsonArray[i]['FIREPROOF_ITEM_YN']+'" readonly="readonly"/>'; // 2025-08-21 hsg 내화구조 제품 관련 팝업창 추가
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
				htmlText += '		<button type="button" class="btn btn-light-gray" onclick="delItem(this);">삭제</button>';
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
				htmlText += '		<input type="hidden" class="form-control text-right" name="m_fireproof_item" value="'+jsonArray[i]['FIREPROOF_ITEM_YN']+'" readonly="readonly"/>'; // 2025-08-21 hsg 내화구조 제품 관련 팝업창 추가
				htmlText += '	</td>';
				htmlText += '	<td>';
				htmlText += '		<button type="button" class="btn btn-light-gray" onclick="delItem(this);">삭제</button>';
				htmlText += '	</td>';
				htmlText += '</tr>';
			}
		}
	} //for

	$('#'+div+'itemListTbodyId').append(htmlText);
	initAutoNumeric();


	//납품처는 필수값이 아님. 쿼테이션 번호는 납품처를 선택해야 조회됨. 쿼테이션 번호와 품목코드로 주문 가능한 품목인지 체크해야 되는데..
	//2025-06-04 ijy. 일단 쿼테이션 번호가 있을때만 주문접수 전에 쿼테이션 번호와 품목코드로 주문접수가 가능한 품목인지 체크
	/*
	var quoteQt = $('input[name="v_shiptoqt"]').val();
	if(quoteQt != null && quoteQt != '' ){
		var flag = quotationVerification();
		if(!flag){
			return;
		}
	}
	*/
    // 2025-08-14 hsg Apple-Pie : ⭐ 수정된 부분: 납품처명 기준으로 검증 여부 결정
    if ( needQuotationVerification() ) {
        var flag = quotationVerification();
        if(!flag){
            return;
        }
    }
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
// 납품처 사용 품목 기록 삭제 데이터 추가
function setShiptoUseAjax(obj, r_itemcd, r_shiptocd){
	$(obj).prop('disabled', true); //이거 확인
	$.ajax({
		async : false,
		data : {
			m_shiptocd : r_shiptocd,
			m_itemcd : r_itemcd
		},
		type : 'POST',
		url : '${url}/front/order/setShiptoUseAjax.lime',
		success : function(data) {
			$(obj).prop('disabled', false);
		},
		error : function(request,status,error){
			alert('Error');
			$(obj).prop('disabled', false);
		}
	});

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
	closeModal();

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


	//납품처는 필수값이 아님. 쿼테이션 번호는 납품처를 선택해야 조회됨. 쿼테이션 번호와 품목코드로 주문 가능한 품목인지 체크해야 되는데..
	//2025-06-04 ijy. 일단 쿼테이션 번호가 있을때만 주문접수 전에 쿼테이션 번호와 품목코드로 주문접수가 가능한 품목인지 체크
	/*
	var quoteQt = $('input[name="v_shiptoqt"]').val();
	if(quoteQt != null && quoteQt != '' ){
		var flag = quotationVerification();
		if(!flag){
			$(obj).prop('disabled', false);
			return;
		}
	}
	*/
    // 2025-08-14 hsg Apple-Pie : ⭐ 수정된 부분: 납품처명 기준으로 검증 여부 결정
    if ( needQuotationVerification() ) {
        var flag = quotationVerification();
        if(!flag){
            return;
        }
    }



	var insertFlag = true;

	var confirmText = '주문접수 하시겠습니까?';
	var isConfirmed = true;

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

	if(isConfirmed){
		//var m_transty = $('input:radio[name="m_transty"]:checked').val();
		//if('AB' == m_transty){ //운송수단이 자차운송인 경우는 우편번호를 90000으로 픽스.
		//	$('input[name="m_zipcd"]').val('90000');
		//}

		$('#ajax_indicator').show().fadeIn('fast');

		var trObj = $('#itemListTbodyId > tr');
		var fireproofFlag = false;
		var fireproofItemFlag = false; //2025-08-21 hsg 내화구조 제품 관련 팝업창 추가

		$(trObj).each(function(i,e){
			if(0 != i){ // i==0 class="jqgfirstrow"로 실제 데이터가 아님.
				var fireproofYn = $($(e).find('input[name="m_fireproof"]')[0]).val();
				if(fireproofYn=='Y'){
					fireproofFlag = true;
				}
				// 2025-08-21 hsg 내화구조 제품 관련 팝업창 추가
/* 				var fireproofItemYn = $($(e).find('input[name="m_fireproof_item"]')[0]).val();
				if(fireproofItemYn=='Y'){
					fireproofItemFlag = true;
				} */
			}
		});

		/* *********** ↓↓↓↓↓↓↓  2025-08-21 hsg 내화구조 제품 관련 팝업창 추가 : 내화구조 팝업 ↓↓↓↓↓↓↓  *********** */
		/* if(fireproofItemFlag){
			confirmFireproofModal();
		}
 */

		/* ***********  ↑↑↑↑↑↑↑ 2025-08-21 hsg 내화구조 제품 관련 팝업창 추가 : 내화구조 팝업 ↑↑↑↑↑↑↑  *********** */

		// fireproofItemFlag
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



//납품처 선택 시 해당 납품처에서 사용했던 모든 품목 조회. 2025-05-22 ijy
function getShiptoCustOrderAllItemListAjax(shiptoCd){
	if(shiptoCd == null || shiptoCd == '' || shiptoCd == '0'){
		console.log('getShiptoCustOrderAllItemListAjax shiptoCd null ');
		return false;
	}

	$('#shiptoUseGridArea').show();

	var orderByType = ''; //cnt:주문 수량 많은순, dt:최근 주문일순, itemCd:품목 코드, itemNm:품목명, 없으면 품목검색 팝업과 동일 방식 정렬
	$.ajax({
		async : false,
		url: "${url}/front/base/getShiptoCustOrderAllItemListAjax.lime",
		cache : false,
		type : 'POST',
		dataType: 'json',
		data : {
			m_shiptocd : shiptoCd,
			orderBy : orderByType
		},
		success : function(data){
			if(data == null || data.list == null){
				//데이터 조회 실패.
				return false;
			}

			//var mFlag = $('input[name="m_accessdevice"]').val(); //1: PC, 2: Mobile > 폰 브라우저로 확인해도 모바일 구분 안됨
			var div = ($('div .full-desktop').css('display') == 'none') ? 'm' : ''; //m: mobile, '': PC

			var list = data.list;
			var listTotalCount = list.length;

			var htmlText = '';

			if(div != 'm'){ //PC
				$('#shiptoUseGridTbody').empty();

				if(listTotalCount == null || listTotalCount == 0){
					htmlText += '<tr>';
					htmlText += '	<td colspan="7" class="list-empty">';
					htmlText += '		<img src="${url}/include/images/front/common/icon_empty.png" alt="img" /><br />';
					htmlText += '		사용 품목 기록이 없습니다.';
					htmlText += '	</td>';
					htmlText += '</tr>';
				} else {

					$(list).each(function(i,e){
						//2025-08-21 hsg 내화구조 제품 관련 팝업창 추가
						htmlText += '<tr itemCdAttr="'+e.ITEM_CD+'" itemNmAttr="'+e.DESC1+'" itemUnitAttr="'+e.UNIT4+'" fireproofYnAttr="'+e.FIREPROOF_YN+'" fireproofItemYnAttrㅇ="'+e.FIREPROOF_ITEM_YN+'" itemPalletAttr="'+addComma(e.ITI_PALLET)+'" recommendItemCountAttr="'+e.RECOMMEND_ITEM_COUNT+'">';
						htmlText += '	<td>';
						htmlText += '		<div class="basic-checkbox">';
						htmlText += '			<input type="checkbox" class="lol-checkbox" name="c_itemcd" id="checkbox_'+i+'" value="'+e.ITEM_CD+'" autocomplete="off"/>';
						htmlText += '			<label class="lol-label-checkbox" for="checkbox_'+i+'"></label>';
						htmlText += '		</div>';
						htmlText += '	</td>';
						htmlText += '	<td><img src="${url}/data/item/'+e.ITI_FILE1+'" onerror="this.src=\'${url}/include/images/front/common/icon_img@2x.png\'" width="30" height="30" alt="img" /></td>';
						htmlText += '	<td>'+e.ITEM_CD+'</td>';
						htmlText += '	<td class="text-left"><p class="nowrap">'+e.DESC1+'</p></td>';
						htmlText += '	<td class="text-center">'+e.UNIT4+'</td>';
						htmlText += '	<td class="text-center">'+e.FIREPROOF_YN+'</td>';
						htmlText += '	<td class="text-center">';
						if(e.ITI_LINK != null && e.ITI_LINK != ''){
							htmlText += '		<button type="button" class="btn btn-light-gray" onclick="location.href=\''+e.ITI_LINK+'\'">제품정보</button>';
						} else {
							htmlText += '		<button type="button" class="btn btn-light-gray">제품정보</button>';
						}
						htmlText += '	</td>';
						htmlText += '	<td class="text-center">';
						htmlText += '		<button type="button" style="margin-right:5px;" class="btn btn-green" onclick="addItem(this, \''+i+'\');">선택</button>';
						htmlText += '		<button type="button" class="btn btn-light-gray" onclick="setShiptoUseAjax(this,\''+e.ITEM_CD+'\',\''+shiptoCd+'\');">삭제</button>';
						htmlText += '	</td>';
						htmlText += '</tr>';
					});
				}

				$('#shiptoUseGridTbody').append(htmlText);


			} else { //mobile

				$('#shiptoUseGridTbodyMobile').empty();

				if(listTotalCount == null || listTotalCount == 0){
					htmlText += '<tr>';
					htmlText += '	<td colspan="5" class="list-empty">';
					htmlText += '		<img src="${url}/include/images/front/common/icon_empty.png" alt="img" /><br />';
					htmlText += '		사용 품목 기록이 없습니다.';
					htmlText += '	</td>';
					htmlText += '</tr>';
				} else {

					$(list).each(function(i,e){

						htmlText += '<tr itemCdAttr="'+e.ITEM_CD+'" itemNmAttr="'+e.DESC1+'" itemUnitAttr="'+e.UNIT4+'" fireproofYnAttr="'+e.FIREPROOF_YN+'" fireproofItemYnAttrㅇ="'+e.FIREPROOF_ITEM_YN+'" itemPalletAttr="'+addComma(e.ITI_PALLET)+'" recommendItemCountAttr="'+e.RECOMMEND_ITEM_COUNT+'">';
						htmlText += '	<td>';
						htmlText += '		<div class="basic-checkbox">';
						htmlText += '			<input type="checkbox" class="lol-checkbox" name="c_mitemcd" id="mcheckbox_'+i+'" value="'+e.ITEM_CD+'"/>';
						htmlText += '			<label class="lol-label-checkbox" for="mcheckbox_'+i+'"></label>';
						htmlText += '		</div>';
						htmlText += '	</td>';
						htmlText += '	<td class="text-left">'+e.ITEM_CD+'</td>';
						htmlText += '	<td class="text-left"><p class="">'+e.DESC1+'</p></td>';
						htmlText += '	<td class="text-center">';
						if(e.ITI_LINK != null && e.ITI_LINK != ''){
							htmlText += '		<button type="button" class="btn btn-light-gray" onclick="location.href=\''+e.ITI_LINK+'\'">제품정보</button>';
						} else {
							htmlText += '		<button type="button" class="btn btn-light-gray">제품정보</button>';
						}
						htmlText += '	</td>';
						htmlText += '	<td class="text-center">';
						htmlText += '		<button type="button" class="btn btn-green" onclick="addItem(this, \''+i+'\');">선택</button>';
						htmlText += '	</td>';
						htmlText += '</tr>';
					});
				}

				$('#shiptoUseGridTbodyMobile').append(htmlText);
			}

		},
		error : function(request,status,error){
			console.log(error);
		}
	});

}

//납품처 선택 시 사용했던 모든 품목 조회. 조회된 품목 추가 기능. 팝업에서 추가하는것과 동일 기능 2025-05-22 ijy
function addItem(obj, rowId) {
	$(obj).prop('disabled', true);

	var dv = ($('div .full-desktop').css('display') == 'none') ? 'm' : '';

	if('' == rowId){ // 다중.
		var itemCdObj = $('input:checkbox[name="c_'+dv+'itemcd"]:checked');

		if(0 >= $(itemCdObj).length){
			alert('선택 후 진행해 주세요.');
			$(obj).prop('disabled', false);
			return;
		}

		var jsonArray = new Array();

		if(confirm('해당 품목을 선택 하시겠습니까?')){

			$(itemCdObj).each(function(i,e){
				var jsonData = new Object();
				var rowObj = $(e).closest('tr');

				jsonData.ITEM_CD = $(rowObj).attr('itemCdAttr');
				jsonData.DESC1 = $(rowObj).attr('itemNmAttr');
				jsonData.UNIT = $(rowObj).attr('itemUnitAttr');
				jsonData.FIREPROOF_YN = $(rowObj).attr('fireproofYnAttr');
				jsonData.FIREPROOF_ITEM_YN = $(rowObj).attr('fireproofItemYnAttr');
				jsonData.ITI_PALLET = toFloat($(rowObj).attr('itemPalletAttr').replaceAll(',', ''));
				jsonData.RECOMMEND_ITEM_COUNT = $(rowObj).attr('recommendItemCountAttr');
				jsonArray.push(jsonData);
			});

			setItemList(jsonArray);
			$(obj).prop('disabled', false);
		}else{
			$(obj).prop('disabled', false);
			return;
		}

		$(obj).prop('disabled', false);

	} else {

		var jsonArray = new Array();
		var jsonData = new Object();
		var rowObj = $(obj).closest('tr');

		jsonData.ITEM_CD = $(rowObj).attr('itemCdAttr');
		jsonData.DESC1 = $(rowObj).attr('itemNmAttr');
		jsonData.UNIT = $(rowObj).attr('itemUnitAttr');
		jsonData.FIREPROOF_YN = $(rowObj).attr('fireproofYnAttr');
		jsonData.FIREPROOF_ITEM_YN = $(rowObj).attr('fireproofItemYnAttr');
		jsonData.ITI_PALLET = toFloat($(rowObj).attr('itemPalletAttr').replaceAll(',', ''));
		jsonData.RECOMMEND_ITEM_COUNT = $(rowObj).attr('recommendItemCountAttr');
		jsonArray.push(jsonData);

		setItemList(jsonArray);
		$(obj).prop('disabled', false);

	}

}

//납품처 사용 품목 기록 초기화. 2025-05-22 ijy
function shiptoAllItemReset(){
	//var mFlag = $('input[name="m_accessdevice"]').val(); //1: PC, 2: Mobile > 폰 브라우저로 확인해도 모바일 구분 안됨
	var div = ($('div .full-desktop').css('display') == 'none') ? 'm' : ''; //m: mobile, '': PC
	var htmlText = '';

	if(div != 'm'){
		$('#shiptoUseGridTbody').empty();

		htmlText += '<tr>';
		htmlText += '	<td colspan="7" class="list-empty">';
		htmlText += '		<img src="${url}/include/images/front/common/icon_empty.png" alt="img" /><br />';
		htmlText += '		사용 품목 기록이 없습니다.';
		htmlText += '	</td>';
		htmlText += '</tr>';

		$('#shiptoUseGridTbody').append(htmlText);

	} else {
		$('#shiptoUseGridTbodyMobile').empty();

		htmlText += '<tr>';
		htmlText += '	<td colspan="5" class="list-empty">';
		htmlText += '		<img src="${url}/include/images/front/common/icon_empty.png" alt="img" /><br />';
		htmlText += '		사용 품목 기록이 없습니다.';
		htmlText += '	</td>';
		htmlText += '</tr>';

		$('#shiptoUseGridTbodyMobile').append(htmlText);
	}
}


//2025-06-04 ijy. 쿼테이션 번호와 품목코드로 주문접수가 가능한 품목인지 체크하고 등록되지 않은 품목은 붉은색으로 표기.
function quotationVerification(){
	var returnFlag = false;
	var div        = ($('div .full-desktop').css('display') == 'none') ? 'm' : '';
	var quoteQt    = $('input[name="v_shiptoqt"]').val();
	var itemList   = $('#'+div+'itemListTbodyId').find('input[name="m_itemcd"]').map(function(){
		return $(this).val();
	}).get();
	var itemCd  = itemList.join(',');

	$.ajax({
		async : false,
		url: "${url}/front/base/checkQuotationItemListAjax.lime",
		cache : false,
		type : 'POST',
		dataType: 'json',
		data: {
			quoteQt : quoteQt,
			itemCd  : itemCd
		},
		success : function(data){
			if(data.RES_CODE == '0000') {
				returnFlag = true;
			} else if(data.RES_CODE == '0360') {

				if(data.missingItem != null && data.missingItem != ''){
					var missingArr  = data.missingItem.split(',');

					$('#'+div+'itemListTbodyId tr').each(function(){
						var itemCd = $(this).find('input[name="m_itemcd"]').val();

						for(var i = 0; i < missingArr.length; i++) {
							if(itemCd === missingArr[i]){
								$(this).addClass('errorRow'); //등록되지 않은 품목 붉은색으로 표기.
							}
						}
					});
				}

				returnFlag = false;
			} else {
				returnFlag = false;
			}
		},
		error : function(request,status,error){
			returnFlag =  false;
		}
	});

	return returnFlag;
}




//도시 리스트와 인덱스를 전역으로 선언
var cityList = [];
var cityIndex = 0;
var intervalId = null;

function weekWeatherForecastApi() {
    let forecastType = "2";

    $.ajax({
        async : false,
        url : '${url}/admin/base/getWeatherForecastApiAjax.lime',
        cache : false,
        type : 'POST',
        dataType: 'json',
        data: {
            addr : '',
            selDate : '',
            forecastType : forecastType
        },
        success : function(data){
            if(data.resultCode == '00') {
                // 전체 도시 데이터 저장 (최초 1회만)
                if (cityList.length === 0) {
                    cityList = data.weatherList;
                    cityIndex = 0;
                }

                showCityWeather(cityList[cityIndex]);

                // 이미 인터벌 실행 중이면 중복 실행 방지
                if (intervalId === null) {
                    intervalId = setInterval(function() {
                        cityIndex = (cityIndex + 1) % cityList.length;
                        showCityWeather(cityList[cityIndex]);
                    }, 10000);
                }
            }
        },
        error : function(request,status,error){}
    });
}

// 도시별 날씨 보여주기 함수
function showCityWeather(selectedCityData) {
   var forecasts = selectedCityData.cityWeatherList;
   var html = '';

   forecasts.slice(0, 7).forEach(function(forecast, index) {
       var date = forecast.weather_date;
       var dayOfWeek = forecast.day_of_week;
       var minTemp = Math.round(forecast.am_temp);
       var maxTemp = Math.round(forecast.pm_temp);
       var amWeather = forecast.am_weather;
       var pmWeather = forecast.pm_weather;
       var amPop = forecast.am_pop || 0;
       var pmPop = forecast.pm_pop || 0;
       var minHumi = forecast.am_humi || '-';   // 최저습도
       var maxHumi = forecast.pm_humi || '-';   // 최고습도

       // 평균 강수확률(사용 안함)
       // var avgPop = Math.round((amPop + pmPop) / 2);

       var formattedDate = '';
       if (date && date.length == 8) {
           var month = date.substring(4, 6);
           var day = date.substring(6, 8);
           formattedDate = month + '/' + day;
       } else {
           var month = date.substring(5, 7);
           var day = date.substring(8, 10);
           formattedDate = month + '/' + day;
       }

       // 요일 표시 (오늘/내일/요일)
       var dayLabel = '';
       if (index === 0) {
           dayLabel = '오늘';
       } else if (index === 1) {
           dayLabel = '내일';
       } else {
           dayLabel = dayOfWeek;
       }

       if(20 <= amPop && amPop < 40){
    	   amWeather = "구름많음";
       } else if(40 <= amPop && amPop < 60){
    	   amWeather = "흐림";
       } else if(60 <= amPop){
    	   amWeather = "비";
       }

       if(20 <= pmPop && pmPop < 40){
    	   pmWeather = "구름많음";
       } else if(40 <= pmPop && pmPop < 60){
    	   pmWeather = "흐림";
       } else if(60 <= pmPop){
    	   pmWeather = "비";
       }

       // 1줄차: 요일, 최저기온, 아이콘, 아이콘, 최고기온
       html += '<div class="weather-day-card' + (index === 0 ? ' today' : '') + '">';
       html += '  <div class="weather-temps">';
       html += '    <span class="weather-temp-high">' + dayLabel + '</span>';
       html += '    <span class="weather-temp-low">' + formattedDate + '</span>';
       html += '  </div>';

       html += '  <div class="weather-temps">';
       html += '    <span class="weather-temp-high">' + minTemp + '°</span>';
       html += '    <span class="weather-temp-low">' + amPop + '%</span>';
       html += '  </div>';

       html += '  <div class="weather-icon-row">';
       html += '    <img src="${url}/data/weather/날씨_' + amWeather + '.png" ';
       html += '         onerror="this.src=\'${url}/data/weather/날씨_맑음.png\'" ';
       html += '         class="weather-icon" alt="날씨" />';
       html += '    <img src="${url}/data/weather/날씨_' + pmWeather + '.png" ';
       html += '         onerror="this.src=\'${url}/data/weather/날씨_맑음.png\'" ';
       html += '         class="weather-icon" alt="날씨" />';
       html += '  </div>';

       //if (avgPop > 0) {
           html += '  <div class="weather-temps">';
           html += '    <span class="weather-temp-high">' + maxTemp + '°</span>';
           html += '    <span class="weather-temp-low">' + pmPop + '%</span>';
           html += '  </div>';
       //}

       html += '</div>';
   });

   $("#cityName").text(selectedCityData.city || '');
   $("#weekForecast").html(html);
}













function getFormattedDate(date) {
	  const year = date.getFullYear();
	  const month = String(date.getMonth() + 1).padStart(2, '0');
	  const day = String(date.getDate()).padStart(2, '0');
	  return `${year}${month}${day}`;
}
function getWeekday(date) {
	  const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
	  return weekdays[date.getDay()] + '요일';
}


function isValidCheckWeatherForecast(addr, selDate){

	if(addr == null || (typeof addr === 'string' && addr.trim() ==="")){
		return false;
	}
	if(selDate == null || (typeof selDate === 'string' && selDate.trim() ==="")){
		return false;
	}

	return true;
}

//기상청 API 날씨정보 조회 2025.06.12 ijy
function weatherForecastApi(){
	let addr    = $('input[name="m_add1"]').val();
	let selDate = $('input[name="v_requestdate"]').val();
	let forecastType = "1";

	//addr
	//selDate
	//forecastType

	if(!isValidCheckWeatherForecast(addr, selDate)){
		return;
	}


	$.ajax({
		async : false,
		url : '${url}/front/base/getWeatherForecastApiAjax.lime',
		cache : false,
		type : 'POST',
		dataType: 'json',
		data: {
			addr : addr,
			selDate : selDate,
			forecastType : forecastType
		},
		success : function(data){
			//debugger
			if(data.resultCode == '00') {


				//console.log(data.resultCode);
				//console.log(data.weatherList);
				//weatherList : [{date=20250615, pmWeather=비, maxTemp=28, pmPop=100, amWeather=흐림, amPop=30, minTemp=22}]
				var date = data.weatherList[0].date;
				var dayOfWeek = data.weatherList[0].dayOfWeek;
				var minTemp = data.weatherList[0].minTemp;
				var maxTemp = data.weatherList[0].maxTemp;
				var amWeather = data.weatherList[0].amWeather;
				var pmWeather = data.weatherList[0].pmWeather;
				var amPop = data.weatherList[0].amPop;
				var pmPop = data.weatherList[0].pmPop;

				$("#weatherDiv").show();
				var html = '';
				//html += dayOfWeek + ' ';
				html += '<div class="weather-period">';
				html += '<div class="period">오전</div>';
				html += '<div class="percent">' + amPop + '%</div>';
				html += '</div>';
				html += '<img src="${url}/data/weather/날씨_' + amWeather + '.png" onerror="this.src=\'${url}/data/weather/날씨_맑음.png\'" width="40" height="40" alt="image" />';
				html += '<img src="${url}/data/weather/날씨_' + pmWeather + '.png" onerror="this.src=\'${url}/data/weather/날씨_맑음.png\'" width="40" height="40" alt="image" />';
				html += '<div class="weather-period">';
				html += '<div class="period">오후</div>';
				html += '<div class="percent">' + pmPop + '%</div>';
				html += '</div>';
				//html += ' 최저 '+ minTemp + 'º / 최고 ' + maxTemp+ 'º';
				$("#weatherDiv").html(html);

			} else {

			}
		},
		error : function(request,status,error){
		}
	});
}



//도시 리스트와 인덱스를 전역으로 선언
var cityList = [];
var cityIndex = 0;
var intervalId = null;

function weekWeatherForecastApi() {
    let forecastType = "2";

    $.ajax({
        async : false,
        url : '${url}/front/base/getWeatherForecastApiAjax.lime',
        cache : false,
        type : 'POST',
        dataType: 'json',
        data: {
            addr : '',
            selDate : '',
            forecastType : forecastType
        },
        success : function(data){
            if(data.resultCode == '00') {
                // 전체 도시 데이터 저장 (최초 1회만)
                if (cityList.length === 0) {
                    cityList = data.weatherList;
                    cityIndex = 0;
                }

                showCityWeather(cityList[cityIndex]);

                // 이미 인터벌 실행 중이면 중복 실행 방지
                if (intervalId === null) {
                    intervalId = setInterval(function() {
                        cityIndex = (cityIndex + 1) % cityList.length;
                        showCityWeather(cityList[cityIndex]);
                    }, 10000);
                }
            }
        },
        error : function(request,status,error){}
    });
}

// 도시별 날씨 보여주기 함수
function showCityWeather(selectedCityData) {
   var forecasts = selectedCityData.cityWeatherList;
   var html = '';

   forecasts.slice(0, 7).forEach(function(forecast, index) {
       var date = forecast.weather_date;
       var dayOfWeek = forecast.day_of_week;
       var minTemp = Math.round(forecast.am_temp);
       var maxTemp = Math.round(forecast.pm_temp);
       var amWeather = forecast.am_weather;
       var pmWeather = forecast.pm_weather;
       var amPop = forecast.am_pop || 0;
       var pmPop = forecast.pm_pop || 0;
       var minHumi = forecast.am_humi || '-';   // 최저습도
       var maxHumi = forecast.pm_humi || '-';   // 최고습도

       // 평균 강수확률(사용 안함)
       // var avgPop = Math.round((amPop + pmPop) / 2);

       var formattedDate = '';
       if (date && date.length == 8) {
           var month = date.substring(4, 6);
           var day = date.substring(6, 8);
           formattedDate = month + '/' + day;
       } else {
           var month = date.substring(5, 7);
           var day = date.substring(8, 10);
           formattedDate = month + '/' + day;
       }

       // 요일 표시 (오늘/내일/요일)
       var dayLabel = '';
       if (index === 0) {
           dayLabel = '오늘';
       } else if (index === 1) {
           dayLabel = '내일';
       } else {
           dayLabel = dayOfWeek;
       }


       if(20 <= amPop && amPop < 40){
    	   amWeather = "구름많음";
       } else if(40 <= amPop && amPop < 60){
    	   amWeather = "흐림";
       } else if(60 <= amPop){
    	   amWeather = "비";
       }

       if(20 <= pmPop && pmPop < 40){
    	   pmWeather = "구름많음";
       } else if(40 <= pmPop && pmPop < 60){
    	   pmWeather = "흐림";
       } else if(60 <= pmPop){
    	   pmWeather = "비";
       }


       // 1줄차: 요일, 최저기온, 아이콘, 아이콘, 최고기온
       html += '<div class="weather-day-card' + (index === 0 ? ' today' : '') + '">';
       html += '  <div class="weather-temps">';
       html += '    <span class="weather-temp-high">' + dayLabel + '</span>';
       html += '    <span class="weather-temp-low">' + formattedDate + '</span>';
       html += '  </div>';

       html += '  <div class="weather-temps">';
       html += '    <span class="weather-temp-high">' + minTemp + '°</span>';
       html += '    <span class="weather-temp-low">' + amPop + '%</span>';
       html += '  </div>';

       html += '  <div class="weather-icon-row">';
       html += '    <img src="${url}/data/weather/날씨_' + amWeather + '.png" ';
       html += '         onerror="this.src=\'${url}/data/weather/날씨_맑음.png\'" ';
       html += '         class="weather-icon" alt="날씨" />';
       html += '    <img src="${url}/data/weather/날씨_' + pmWeather + '.png" ';
       html += '         onerror="this.src=\'${url}/data/weather/날씨_맑음.png\'" ';
       html += '         class="weather-icon" alt="날씨" />';
       html += '  </div>';

       //if (avgPop > 0) {
           html += '  <div class="weather-temps">';
           html += '    <span class="weather-temp-high">' + maxTemp + '°</span>';
           html += '    <span class="weather-temp-low">' + pmPop + '%</span>';
           html += '  </div>';
       //}

       html += '</div>';
   });

   $("#cityName").text(selectedCityData.city || '');
   $("#weekForecast").html(html);
}





/* *********** ↓↓↓↓↓↓↓  2025-08-21 hsg 내화구조 제품 관련 팝업창 추가 : 내화구조 팝업 (계정별 관리) ↓↓↓↓↓↓↓  *********** */

//사용자 ID 가져오기 (JSP 세션에서)
var currentUserId = '${sessionScope.loginDto.userId}' || '${sessionScope.loginDto.custCd}' || 'anonymous';

//사용자별 세션 키 생성
var fireproofSessionKey = 'fireproofPopupShownDate_' + currentUserId;

//세션 기반 팝업 표시 여부 관리 변수
var fireproofPopupShownToday = false; // 오늘 이미 표시되었는지 여부
var fireproofCheckboxConfirmed = false; // 체크박스 확인 여부 (실행 취소 대응용)

//페이지 로드 시 사용자별 세션 스토리지에서 오늘 팝업 표시 여부 확인
$(document).ready(function() {
 var today = new Date().toDateString();
 var lastShownDate = sessionStorage.getItem(fireproofSessionKey);
 
 console.log('현재 사용자:', currentUserId);
 console.log('세션 키:', fireproofSessionKey);
 console.log('저장된 날짜:', lastShownDate);
 console.log('오늘 날짜:', today);
 
 if (lastShownDate === today) {
     fireproofPopupShownToday = true;
     console.log('오늘 이미 팝업이 표시되었음');
 }
});

//내화구조 제품 포함 여부 체크
function checkFireproofProducts() {
 var div = ($('div .full-desktop').css('display') == 'none') ? 'm' : '';
 var hasFireproofProduct = false;

 // 현재 선택된 품목들의 m_fireproof_item 값 체크
 $('#'+div+'itemListTbodyId').find('tr.itemListTrClass').each(function() {
     var fireproofItemYn = $(this).find('input[name="m_fireproof_item"]').val();
     if(fireproofItemYn == 'Y'){
         hasFireproofProduct = true;
         return false; // break 역할
     }
 });

 return hasFireproofProduct;
}

//기존 showModal 함수 수정
function showModal(){
 console.log('showModal 호출됨');
 console.log('내화구조 제품 체크:', checkFireproofProducts());
 console.log('오늘 팝업 표시됨:', fireproofPopupShownToday);
 console.log('체크박스 확인됨:', fireproofCheckboxConfirmed);
 
 // 내화구조 제품 체크
 if (checkFireproofProducts()) {
     // 오늘 이미 표시되었거나 체크박스가 이미 확인된 경우
     if (fireproofPopupShownToday || fireproofCheckboxConfirmed) {
         console.log('내화구조 팝업 건너뛰고 주문확인 모달 표시');
         showOrderConfirmModal();
         return;
     }
     
     // 내화구조 제품이 포함된 경우 내화구조 안내 팝업 먼저 표시
     console.log('내화구조 안내 팝업 표시');
     showFireproofModal();
     return;
 }

 // 내화구조 제품이 없는 경우 바로 주문확인 모달 표시
 console.log('내화구조 제품 없음, 바로 주문확인 모달 표시');
 showOrderConfirmModal();
}

//내화구조 안내 팝업 표시
function showFireproofModal() {
 document.getElementById('fireproofModalOverlay').style.display = 'block';
 document.getElementById('fireproofModalLayer').style.display = 'flex';
 document.body.style.overflow = 'hidden';

 // 체크박스 초기화
 document.getElementById('fireproofConfirmCheck').checked = false;
}

//내화구조 안내 팝업 닫기
function closeFireproofModal() {
 document.getElementById('fireproofModalOverlay').style.display = 'none';
 document.getElementById('fireproofModalLayer').style.display = 'none';
 document.body.style.overflow = '';
}

//내화구조 팝업 닫기 처리
function handleFireproofClose() {
 var checkbox = document.getElementById('fireproofConfirmCheck');

 if (checkbox.checked) {
     // 체크박스가 선택된 경우
     fireproofCheckboxConfirmed = true;
     
     // 사용자별로 오늘 날짜 저장 (하루 1번만 표시)
     var today = new Date().toDateString();
     sessionStorage.setItem(fireproofSessionKey, today);
     fireproofPopupShownToday = true;
     
     console.log('사용자별 세션 저장됨:', fireproofSessionKey, '=', today);

     // 팝업 닫고 주문 확인 모달 표시
     closeFireproofModal();
     showOrderConfirmModal();
 } else {
     // 체크박스 미선택 시 그냥 팝업만 닫기
     console.log('체크박스 미선택으로 팝업만 닫기');
     closeFireproofModal();
	}
}

//기존 주문 확인 모달 표시 (기존 showModal 로직을 분리)
function showOrderConfirmModal() {
 // 1) 폼의 input 값 읽어서 모달 스팬에 채우기
 document.getElementById('modalCustNm').textContent   = '${sessionScope.loginDto.custNm}';
 document.getElementById('modalShipTo').textContent   = document.querySelector('input[name="v_shiptonm"]').value;
 document.getElementById('modalShipAddr').textContent = document.querySelector('input[name="m_add1"]').value + ' ' + document.querySelector('input[name="m_add2"]').value;
 document.getElementById('modalShipDt').textContent   = document.querySelector('input[name="v_requestdate"]').value;
 document.getElementById('modalPhone').textContent    = document.querySelector('input[name="m_tel1"]').value;
 document.getElementById('modalRequest').textContent  = document.querySelector('input[name="m_remark"]').value;

 // 2) <ul> 비우기
 var itemsUl = document.getElementById('modalItems');
 itemsUl.innerHTML = '';

 // 3) 동적 테이블에서 품목 정보 가져오기
 document.querySelectorAll('#itemListTbodyId tr.itemListTrClass').forEach(function(tr, idx) {
     var itemName = tr.cells[2].textContent.trim();
     var quantity = tr.querySelector('input[name="m_quantity"]').value;

     // 2025-05-13 hsg: 둘째 줄부터 줄바꿈만 찍어주면 CSS 가 들여쓰기 처리
     if (idx > 0) itemsUl.innerHTML += '<br>';
     // 2025-05-13 hsg: "품목명 / 수량 : xx" 형태로 출력
     itemsUl.innerHTML += itemName + ' / 수량 : ' + quantity;
 });

 //납품처 정보가 없으면 모달 팝업에서도 안보여주기 2025-05-22 ijy
 var shiptoVal = document.querySelector('input[name="v_shiptonm"]').value;
 if(shiptoVal == null || shiptoVal == ''){
     document.getElementById('modalShipToRow').style.display = 'none';
 } else {
     document.getElementById('modalShipToRow').style.display = 'flex';
 }

 // 4) 모달 열기
 document.getElementById('modalOverlay').style.display = 'block';
 document.getElementById('modalLayer').style.display   = 'flex';
 document.body.style.overflow = 'hidden';
}

//기존 closeModal 함수 수정 - 실행 취소 시 플래그 초기화하지 않음
function closeModal(){
 document.getElementById('modalOverlay').style.display = 'none';
 document.getElementById('modalLayer').style.display   = 'none';
 document.body.style.overflow = '';
 
 // 실행 취소 시에는 fireproofCheckboxConfirmed 플래그를 유지
 // 다음 주문접수 시 내화구조 팝업을 건너뛰기 위함
}

//실제 주문 처리 함수 수정
function confirmOrder(){
 closeModal();
 
 // 주문이 실제로 처리되는 경우에만 플래그 초기화
 fireproofCheckboxConfirmed = false;
 
 //document.forms['frm'].submit(); // 또는 dataIn 호출
 dataIn(this, '00');
}

//테스트용 세션 삭제 함수 (개발 시에만 사용)
function clearFireproofSession() {
 sessionStorage.removeItem(fireproofSessionKey);
 fireproofPopupShownToday = false;
 fireproofCheckboxConfirmed = false;
 console.log('사용자별 내화구조 팝업 세션이 초기화되었습니다.');
 console.log('사용자:', currentUserId);
 console.log('삭제된 키:', fireproofSessionKey);
 alert('사용자별 내화구조 팝업 세션이 초기화되었습니다. (사용자: ' + currentUserId + ')');
}

//현재 사용자의 모든 관련 세션 확인 (디버깅용)
function checkUserSessions() {
 console.log('=== 사용자별 세션 상태 확인 ===');
 console.log('현재 사용자 ID:', currentUserId);
 console.log('세션 키:', fireproofSessionKey);
 console.log('저장된 날짜:', sessionStorage.getItem(fireproofSessionKey));
 console.log('오늘 날짜:', new Date().toDateString());
 console.log('오늘 표시됨:', fireproofPopupShownToday);
 console.log('체크박스 확인됨:', fireproofCheckboxConfirmed);
 
 // 모든 내화구조 관련 세션 키 찾기
 console.log('=== 모든 내화구조 관련 세션 키 ===');
 for(let i = 0; i < sessionStorage.length; i++) {
     let key = sessionStorage.key(i);
     if(key.includes('fireproofPopupShownDate')) {
         console.log(key + ': ' + sessionStorage.getItem(key));
     }
 }
}

//오버레이 클릭 이벤트 수정
document.addEventListener('DOMContentLoaded', function(){
 // 기존 주문 확인 모달 오버레이 클릭
 document.getElementById('modalOverlay').addEventListener('click', closeModal);

 // 내화구조 모달 오버레이 클릭 - 체크박스 확인 없이 닫기
 document.getElementById('fireproofModalOverlay').addEventListener('click', function() {
     closeFireproofModal();
     // 체크박스 확인 없이 닫혔으므로 플래그 설정하지 않음
 });
 
 // 내화구조 모달의 X 버튼 클릭도 동일하게 처리
 document.querySelector('#fireproofModalLayer .close-btn').addEventListener('click', function() {
     closeFireproofModal();
     // 체크박스 확인 없이 닫혔으므로 플래그 설정하지 않음
 });
});

/* ***********  ↑↑↑↑↑↑↑ 2025-08-21 hsg 내화구조 제품 관련 팝업창 추가 : 내화구조 팝업 (계정별 관리) ↑↑↑↑↑↑↑  *********** */


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
					<div class="weather-header-row">
						<div id="cityName"></div>
						<div class="weather-container">
							<div class="weather-week-forecast" id="weekForecast"></div>
						</div>
					</div>
			        <div class="weather-source">
			          * 자료출처 (날씨정보:기상청, 지역좌표:카카오)
			        </div>
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

					<div class="boardViewArea" style="margin-top: 20px;">
						<h2 class="title">
							<c:if test="${'EDIT' eq pageType}"><span class="state">${orderStatus[custOrderH.STATUS_CD]}</span></c:if>
							<em>주문정보입력</em>
							<div class="title-right little">
								<%-- <button type="button" class="btn btn-green" onclick="alert('Ready');">자재주문서</button> --%>
								<c:if test="${'00' eq param.m_statuscd}">
									<button type="button" class="btn btn-yellow" onclick="dataIn(this, '00');">수정</button> <%-- 주문접수 수정--%>
								</c:if>
								<c:if test="${'00' ne param.m_statuscd}">
									<button type="button" class="btn btn-green order-save-btn" onclick="showModal();">${orderStatus['00']}</button> <%-- 주문접수 --%>
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
												<input type="hidden" name="v_shiptoqt" value="${custOrderH.QUOTE_QT.trim()}" /> <!-- 2025-06-04 ijy. 쿼테이션 검증을 위해 쿼테이션 번호 추가 -->
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

										<input type="text" onchange="javascript:weatherForecastApi();" class="form-control calendar form-md bdStartDateClass" name="v_requestdate" data-field="datetime" data-startend="start" data-startendelem=".bdEndDateClass" value="${v_requestdatedt}" readonly="readonly" />
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

													<!-- 2025.06.12 ijy 날씨 아이콘 -->
													<div id="weatherDiv" style="display: none;">
													</div>

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


					<!-- 납품처 선택 시 해당 납품처에서 사용했던 모든 품목 조회 및 선택 기능 제공. 2025-05-22 ijy -->
					<div class="boardListArea" id="shiptoUseGridArea" style="display:none;">
						<h2 class="title">
							납품처 사용 품목 기록
							<div class="title-right little">
								<button type="button" class="btn btn-line btn-lookup" onclick="addItem(this, '');">선택추가</button>
							</div>
						</h2>

						<div class="boardList itemList">
							<!-- desktop -->
							<table id="shiptoUseGridTable" class="full-desktop" width="100%" cellpadding="0" cellspacing="0" border="0">
								<colgroup>
									<col width="5%" />
									<col width="7%" />
									<col width="10%" />
									<col width="*" />
									<col width="10%" />
									<col width="10%" />
									<col width="10%" />
									<col width="20%" />
								</colgroup>
								<thead>
									<tr>
										<th>
											<div class="basic-checkbox">
												<input type="checkbox" class="lol-checkbox" id="allCheck" name="allCheck" onclick="checkAll2(this, 'c_itemcd');" />
												<label class="lol-label-checkbox" for="allCheck"></label>
											</div>
										</th>
										<th>이미지</th>
										<th>품목코드</th>
										<th>품목명</th>
										<th>구매단위</th>
										<th>내화구조</th>
										<th>링크</th>
			 							<th>기능</th>
									</tr>
								</thead>
								<tbody id="shiptoUseGridTbody">
								</tbody>
							</table>

							<!-- mobile -->
							<table class="full-mobile" width="100%" cellpadding="0" cellspacing="0" border="0">
								<colgroup>
									<col width="5%" />
									<col width="20%" />
									<col width="34%" />
									<col width="26%" />
									<col width="15%" />
								</colgroup>
								<thead>
									<tr>
										<th>
											<div class="basic-checkbox">
												<input type="checkbox" class="lol-checkbox" id="mallCheck" name="mallCheck" onclick="checkAll2(this, 'c_mitemcd');" />
												<label class="lol-label-checkbox" for="mallCheck"></label>
											</div>
										</th>
										<th>품목코드</th>
										<th>품목명</th>
										<th>링크</th>
										<th>즐겨찾기</th>
									</tr>
								</thead>
								<tbody id="shiptoUseGridTbodyMobile">
								</tbody>
							</table>
						</div> <!-- boardList -->


					</div> <!-- boardListArea -->


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
												<input type="hidden" class="form-control text-right" name="m_fireproof_item" value="${list.FIREPROOF_ITEM_YN}" readonly="readonly"/>
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
												<button type="button" class="btn btn-light-gray" onclick="delItem(this);">삭제</button>
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
												<input type="hidden" class="form-control text-right" name="m_fireproof_item" value="${list.FIREPROOF_ITEM_YN}" readonly="readonly"/>
											</td>
											<td>
												<button type="button" class="btn btn-light-gray" onclick="delItem(this);">삭제</button>
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
										<button type="button" class="btn btn-green order-save-btn" onclick="showModal();">${orderStatus['00']}</button> <%-- 주문접수 --%>
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



<%-- 모달 오버레이(반투명 배경) --%>
<div id="modalOverlay" class="modal-overlay" style="display:none;"></div>

<!-- 모달팝업에 쓰이는 클래스명이 modern.css 에 정의된 클래스명과 동일, 불필요한 영향으로 오작동, 클래스명 변경. 2025-05-30 ijy -->
<!-- 항목별 좌우 간격 일치화 2025-05-30 ijy -->
<%-- 실제 모달 레이어 --%>
<div id="modalLayer" class="modal2" style="display:none;">
  <div class="modal-header2">
    <h3>주문 내용을 확인해 주십시오</h3>
    <span class="close-btn" onclick="closeModal()">×</span>
  </div>

  <div class="modal-body2">

    <div class="modal-row">
      <div class="modal-label">거&nbsp;&nbsp;래&nbsp;&nbsp;처 :</div>
      <div class="modal-value" id="modalCustNm"></div>
    </div>
    <div class="modal-row"  id="modalShipToRow">
      <div class="modal-label">납&nbsp;&nbsp;품&nbsp;&nbsp;처 :</div>
      <div class="modal-value" id="modalShipTo"></div>
    </div>
    <div class="modal-row">
      <div class="modal-label">납품주소 :</div>
      <div class="modal-value" id="modalShipAddr"></div>
    </div>
    <div class="modal-row">
      <div class="modal-label">납품일시 :</div>
      <div class="modal-value" id="modalShipDt"></div>
    </div>
    <div class="modal-row">
      <div class="modal-label">연&nbsp;&nbsp;락&nbsp;&nbsp;처 :</div>
      <div class="modal-value" id="modalPhone"></div>
    </div>
    <div class="modal-row">
      <div class="modal-label">품&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;목 :</div>
      <div class="modal-value" id="modalItems"></div>
    </div>
    <div class="modal-row">
      <div class="modal-label">요청사항 :</div>
      <div class="modal-value" id="modalRequest"></div>
    </div>
    <div class="modal-row-last">
      주문 내용이 맞다면 '주문 접수' 버튼을 눌러주세요
    </div>

  </div>

  <div class="modal-footer2">
    <!-- <button type="button" class="btn-execute" onclick="dataIn(this, '00', '${custOrderH.REQ_NO}')"><c:out value="${orderStatus['00']}" /></button> -->
    <button type="button" class="btn-execute" onclick="confirmOrder()"><c:out value="${orderStatus['00']}" /></button>
    <button type="button" class="btn-cancel" onclick="closeModal()">실행 취소</button>
  </div>

</div>



<!--  ↓↓↓↓↓↓↓ 2025-08-21 hsg 내화구조 제품 관련 팝업창 추가 : 내화구조 안내 팝업 ↓↓↓↓↓↓↓  -->
<div id="fireproofModalOverlay" class="modal-overlay" style="display:none;"></div>

<div id="fireproofModalLayer" class="fireproof-modal" style="display:none;">
  <div class="fireproof-modal-header">
    <h3>내화구조 적용에 관한 법적 안내사항</h3>
    <span class="close-btn" onclick="closeFireproofModal()">×</span>
  </div>

  <div class="fireproof-modal-body">
    <div class="notice-text">
      내화구조 인정용 제품(방화, 방화방수, 아쿠아락E)은 내화구조로 적용시 아래 법적 사항들을 준수해야 함을 알려 드립니다.
    </div>

    <div class="warning-text">
      납품 현장의 건설사/내장/수장업체 측에 하기 내용이 전달될 수 있도록 조치 바랍니다. (미 이행시 법적 분쟁의 가능성이 있습니다)
    </div>

    <ul class="content-list">
      <li data-num="①">
        당사 내화구조는 당사 제품만을 사용하여 시공되어야 하며, 타사 제품을 사용하거나 혼용 시공할 경우 내화구조 인정이 유효하지 않습니다.
      </li>
      <li data-num="②">
        세부인정내용 내 도면/시방서/구성자재 스펙을 준수해야 하며, 이를 지키지 않을 경우 내화구조 인정이 유효하지 않음을 알려드립니다.
      </li>
      <li data-num="③">
        최근 국토부/건설기술연구원의 현장 점검이 강화되고 있으며, 석고보드 제품/스터드 형상/나사못 간격 위반 등 적발 사례가 다수 확인되고 있습니다.
      </li>
      <li data-num="④">
        위반사례 적발시 해당 현장 준공 승인이 불가능하며, 특히 품질관리서 미제출 또는 허위작성시 벌금/실형의 처벌이 가능합니다.
      </li>
    </ul>

    <div class="fireproof-checkbox-area">
      <label for="fireproofConfirmCheck">
        <input type="checkbox" id="fireproofConfirmCheck" name="fireproofConfirmCheck" />
        상기 내용을 확인했습니다
      </label>
    </div>
  </div>

  <div class="fireproof-modal-footer">
    <button type="button" class="fireproof-btn-close" onclick="handleFireproofClose()">닫기</button>
  </div>
</div>
<!--  ↑↑↑↑↑↑↑ 2025-08-21 hsg 내화구조 제품 관련 팝업창 추가 : 내화구조 안내 팝업 ↑↑↑↑↑↑↑  -->



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