# 아래 소스에 위에서 말한 검증 로직을 추가해줘.
```html
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

<!-- ↓↓↓↓↓↓↓↓↓ 2025-04-18 hsg Italian Stretch No.32 : E-Order Admin 주문등록 Page에서 주문등록 버튼 클릭 시 한번 더 확인 후 주문 접수 할 수 있도록 변경하기 위해 모달 팝업 추가 ↓↓↓↓↓↓↓↓↓ -->
<style>
/* 반투명 배경 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 1000;
}

/* 모달팝업에 쓰이는 클래스명이 modern.css 에 정의된 클래스명과 동일, 불필요한 영향으로 오작동, 클래스명 변경. 2025-05-30 ijy */
/* 모달팝업 높이가 브라우저에 따라 조절됨. 브라우저 영향을 없애고 본문 내용에 따라 길어짐. 최대 길이 넘어서면 스크롤바 2025-05-30 ijy */
.modal2 {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 450px;
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
    flex-shrink: 0; /* 헤더는 고정 높이 유지 */
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
/* label안에 텍스트를 글자수에 관계없이 양끝선에 맞게 정렬되도록 온갖 css 써봤으나 결국 실패.. &nbsp;로 처리 */
.modal-body2 .modal-label {
  width: 75px;
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
#gridList .errorRow {
	color: red;
}

/*
#weatherDiv {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
}
#weatherDiv img {
    width: 24px;
    height: 24px;
    vertical-align: middle;
}
*/
/* 
#weatherDiv {
  box-sizing: border-box;
  text-align: justify;
  flex: 1;
  white-space: pre-wrap;
}
 */
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
    margin: 0 10px;                     /* 좌우 10px씩 여백 */
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
    width: 150px;
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
    gap: 16px; /* 도시명과 날씨 사이 간격 */
    flex-wrap: nowrap; /* 줄바꿈 방지 */
}
#cityName {
    font-weight: bold;
    font-size: 16px;
    white-space: nowrap; /* 도시명 줄바꿈 방지 */
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
  width: 1652px;
  text-align: right;
  font-size: 12px;
  color: #444;
  margin: 0 auto;
}


</style>

<script>
function showModal() {
	  // 1) 폼의 input 값 읽어서 모달 스팬에 채우기
	  document.getElementById('modalCustNm').textContent   = document.querySelector('input[name="v_custnm"]').value;
	  document.getElementById('modalShipTo').textContent   = document.querySelector('input[name="v_shiptonm"]').value;
	  document.getElementById('modalShipAddr').textContent = document.querySelector('input[name="m_add1"]').value + ' ' + document.querySelector('input[name="m_add2"]').value;
	  document.getElementById('modalShipDt').textContent   = document.querySelector('input[name="v_requestdate"]').value;
	  document.getElementById('modalPhone').textContent    = document.querySelector('input[name="m_tel1"]').value;
	  //document.getElementById('modalItem').textContent     = document.querySelector('input[name="v_item"]').value;
	  document.getElementById('modalRequest').textContent  = document.querySelector('input[name="m_remark"]').value;
	
	  // 1) <ul> 비우기
	  var itemsUl = document.getElementById('modalItems');
	  itemsUl.innerHTML = '';
	  // 1) span#modalItems 초기화
	  var modalItems = document.getElementById('modalItems');               // 2025-05-13 hsg span 요소 초기화
	  modalItems.innerHTML = '';

	  // 2) jqGrid 에서 선택된(추가된) 모든 행 ID 가져오기
	  var grid = $("#gridList");
	  var rowIds = grid.jqGrid('getDataIDs');

	  // 3) 각 행의 컬럼값을 꺼내서 <li>로 추가
/* 	  rowIds.forEach(function(id) {
	    var row = grid.jqGrid('getRowData', id);
	    var $tr = grid.find('tr#' + id);
	    // 예: colModel 에 name: 'ITEM_NAME' 으로 정의했다면:
	    var itemName = row.DESC1;  
	    var itemCd   = row.ITEM_CD;    // 필요하면 코드도 같이 출력
	    //var quantity = row.QUANTITY // 수량
	    var quantity = $tr.find('input[name="m_quantity"]').val();

	    var li = document.createElement('li');
	    li.textContent = itemName + " / 수량 : " + quantity;
	    //li.textContent = itemName;
	    itemsUl.appendChild(li);
	  });
 */
   // 3) 각 행의 컬럼값을 꺼내서 텍스트 + 줄바꿈 형태로 추가
   rowIds.forEach(function(id, idx) {
     var row      = grid.jqGrid('getRowData', id);
     var $tr      = grid.find('tr#' + id);
     var text     = row.DESC1 + " / 수량 : " + $tr.find('input[name=\"m_quantity\"]').val();
 
     // 2025-05-13 hsg: 두 번째 줄부터 <br> 만 붙이면, CSS 가 margin-left 로 들여쓰기 처리해 줌
     if (idx > 0) modalItems.innerHTML += '<br>';
     modalItems.innerHTML += text;
 });
   
	//납품처 정보가 없으면 모달 팝업에서도 안보여주기 2025-05-22 ijy
	var shiptoVal = document.querySelector('input[name="v_shiptonm"]').value;
	if(shiptoVal == null || shiptoVal == ''){
		document.getElementById('modalShipToRow').style.display = 'none';
	} else {
		document.getElementById('modalShipToRow').style.display = 'flex';
	}

	  // 2) 모달 열기
	  document.getElementById('modalOverlay').style.display = 'block';
	  document.getElementById('modalLayer').style.display   = 'flex';
	  document.body.style.overflow = 'hidden';  // 스크롤 잠금
}

// 모달 닫기
function closeModal() {
  document.getElementById('modalOverlay').style.display = 'none';
  document.getElementById('modalLayer').style.display = 'none';
  document.body.style.overflow = '';
}

// 오버레이 클릭 시에도 닫히게 하고 싶으면
document.getElementById('modalOverlay').addEventListener('click', closeModal);


	// 모달 외부 클릭 시에도 닫기
	document.getElementById('modalOverlay').addEventListener('click', closeModal);

	// 주문 접수 처리 예시
	function confirmOrder() {
	  // 예: form 전송
	  document.getElementById('mainForm').submit();
	}
</script>
<!-- ↑↑↑↑↑↑↑↑↑ 2025-04-18 hsg Italian Stretch No.32 : E-Order Admin 주문등록 Page에서 주문등록 버튼 클릭 시 한번 더 확인 후 주문 접수 할 수 있도록 변경하기 위해 모달 팝업 추가 ↑↑↑↑↑↑↑↑↑ -->


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

//납품처 선택 시 해당 납품처에서 사용했던 모든 품목 조회. 납품처 사용 품목 정의. 2025-05-22 ijy
var reLoadFlag = false; //jqGrid 데이터 초기화 여부 설정. jqGrid 재호출(납품처 변경) 시 그리드 초기화 필요. row 데이터가 없어도 초기화 해야 정상 동작함. 전역으로 설정, 한번이라도 호출 시 true로 변경.
var shipToItemColModel = [
	{name:"ITI_FILE1",		label:'이미지',		width:100,	align:'center',	sortable:false, formatter:setItiImage1},
	{name:"ITEM_CD",		label:'품목코드',		width:100,	align:'center',	sortable:false, key:true},
	{name:"DESC1",			label:'품목명1',		width:305,	align:'left',	sortable:false},
	{name:"DESC2",			label:'품목명2',		width:305,	align:'left',	sortable:false},
	{name:"THICK_NM",		label:'두께',			width:90,	align:'right',	sortable:false},
	{name:"WIDTH_NM",		label:'폭',			width:90,	align:'right',	sortable:false},
	{name:"LENGTH_NM",		label:'길이',			width:90,	align:'right',	sortable:false},
	{name:"UNIT4",			label:'구매단위',		width:90,	align:'center',	sortable:false},
	{name:"ITI_PALLET",		label:'파레트적재단위',	width:125,	align:'right',	sortable:false, formatter:setPallet},
	{name:"FIREPROOF_YN",	label:'내화구조',		width:90,	align:'center',	sortable:false},
	{name:"",				label:'기능',			width:150,	align:'center',	sortable:false,	formatter:addItemButton }
];
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
		
		//납품처 선택 시 해당 납품처에서 사용했던 모든 품목 조회. 수정화면에선 품목 바로 조회. 2025-05-22 ijy
		getShiptoCustOrderAllItemListAjax('${custOrderH.SHIPTO_CD}');
	}

	weekWeatherForecastApi();
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
	
	//납품처 선택 시 해당 납품처에서 사용했던 모든 품목 조회. 납품처 초기화시 품목 그리드도 초기화. 2025-05-22 ijy
	$("#shiptoUseGridList").jqGrid('clearGridData', true);

	setAddressShipTo();
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

	$('btn btn-black btn-xs writeObjectClass').prop('disabled', b);
	*/
	$('.orderadd button').prop('disabled', b);
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
	
	setAddressShipTo();

	//납품처 선택 시 해당 납품처에서 사용했던 모든 품목 조회. 2025-05-22 ijy
	getShiptoCustOrderAllItemListAjax(shiptoCd);
}

// 품목 선택 팝업 띄우기.
function openItemPop(obj){
	//납품처 미선택시 품목 검색 팝업 사용 불가 2025-05-22 ijy > 제거 요청. 2025-05-27 ijy
// 	var selectedShiptoCd = toStr($('input[name="m_shiptocd"]').val());
// 	if('' == selectedShiptoCd){
// 		alert('납품처를 선택해주세요.');
// 		return;
// 	}
	
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
	
	
	//납품처는 필수값이 아님. 쿼테이션 번호는 납품처를 선택해야 조회됨. 쿼테이션 번호와 품목코드로 주문 가능한 품목인지 체크해야 되는데..
	//2025-06-02 ijy. 일단 쿼테이션 번호가 있을때만 주문접수 전에 쿼테이션 번호와 품목코드로 주문접수가 가능한 품목인지 체크
	var quoteQt = $('input[name="v_shiptoqt"]').val();
	if(quoteQt != null && quoteQt != '' ){
		var flag = quotationVerification();
		if(!flag){
			return;
		}
	}
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
	closeModal();

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
	
	
	//납품처는 필수값이 아님. 쿼테이션 번호는 납품처를 선택해야 조회됨. 쿼테이션 번호와 품목코드로 주문 가능한 품목인지 체크해야 되는데..
	//2025-06-02 ijy. 일단 쿼테이션 번호가 있을때만 주문접수 전에 쿼테이션 번호와 품목코드로 주문접수가 가능한 품목인지 체크
	var quoteQt = $('input[name="v_shiptoqt"]').val();
	if(quoteQt != null && quoteQt != '' ){
		var flag = quotationVerification();
		if(!flag){
			$(obj).prop('disabled', false);
			return;
		}
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


//납품처 선택 시 해당 납품처에서 사용했던 모든 품목 조회. 2025-05-22 ijy
function getShiptoCustOrderAllItemListAjax(shiptoCd){
	if(shiptoCd == null || shiptoCd == '' || shiptoCd == '0'){
		console.log('getShiptoCustOrderAllItemListAjax shiptoCd null ');
		return false;
	}
	
	var orderByType = ''; //cnt:주문 수량 많은순, dt:최근 주문일순, itemCd:품목 코드, itemNm:품목명, 없으면 품목검색 팝업과 동일 방식 정렬
	
	//jqGrid 재호출 시 그리드 초기화 필요.
	if(reLoadFlag){
		console.log('getShiptoCustOrderAllItemListAjax 그리드 초기화');
		$("#shiptoUseGridList").jqGrid('clearGridData', true); //납품처 변경시 기존 데이터 초기화 필요.
		
		$("#shiptoUseGridList").jqGrid('setGridParam', { //초기화 후 재설정
			datatype: "json",
			url: "${url}/admin/base/getShiptoCustOrderAllItemListAjax.lime",
			postData: {
				m_shiptocd : shiptoCd
				, orderBy : orderByType
			},
			mtype: 'POST',
			loadonce: false
		}).trigger('reloadGrid');
		return false;
	}
	
	$('#shiptoUseItemDiv').show(); //납품처 코드 조회 후 보여주기
	$("#shiptoUseGridList").jqGrid({
		url: "${url}/admin/base/getShiptoCustOrderAllItemListAjax.lime",
		datatype: "json",
		mtype: 'POST',
		postData: {
			m_shiptocd : shiptoCd
			, orderBy : orderByType
		},
		colModel: shipToItemColModel,
		loadonce: false, //1회만 로드 설정. 이걸 꺼야 재호출 가능.
		height: '360px',
		autowidth: false,
		rownumbers: true,
		rowNum: 1000,
		actions : true,
		pginput : true,
		multiselect: true,
		beforeSelectRow: function(rowid, e){
			//ROW 내 선택버튼 클릭시 바로 하단에 추가됨. 선택 버튼 클릭시 다중 선택되지 않도록 제한.
			const $target = $(e.target);
			if(e.target.tagName === 'BUTTON'){
				return false;
			}
			return true;
		},
		jsonReader : {
			root : 'list'
		},
		loadComplete: function(data){
			initAutoNumeric();
		},
		gridComplete: function(){
			reLoadFlag = true; //초기화 여부 설정. row 데이터가 없어도 재호출 시 그리드 초기화 해야 정상 동작함.
			
			var grid = $('#shiptoUseGridList');
		    
		    var records = grid.getGridParam('records');
			if(records > 0){
				$('#shiptoUseItemDiv .ui-jqgrid, #shiptoUseItemDiv .ui-jqgrid-view').show();
				$('#shiptoUseNoList').hide();
            } else {
            	//조회 품목 없을 시 noList div랑 겹쳐서 두꺼운 줄로 보여짐. 보더라인까지 숨김 처리.
            	$('#shiptoUseItemDiv .ui-jqgrid, #shiptoUseItemDiv .ui-jqgrid-view').hide();
				$('#shiptoUseNoList').show();
            }
		},
		loadError: function(xhr, status, error){
			reLoadFlag = true;
			console.error('status: ' + status);
			console.error('error: ' + error);
		}
	});
	
}


//납품처 선택 시 사용했던 모든 품목 조회. 조회된 품목 추가 기능. 팝업에서 추가하는것과 동일 기능 2025-05-22 ijy
function addItem(rowId) { //rowId : 빈값=다중선택, !빈값=개별선택
	var jsonArray = new Array();
	
	if('' == rowId){
		//다중 선택 후 선택추가 버튼 클릭시
		var chk = $('#shiptoUseGridList').jqGrid('getGridParam','selarrrow');
		chk += '';
		var chkArr = chk.split(",");
		if (chk == '') {
			alert('품목 선택 후 진행해 주십시오.');
			return false;
		}
		
		for(var i=0,j=chkArr.length; i<j; i++){
			var jsonData = new Object();
			var rowObj = $('#shiptoUseGridList').jqGrid('getRowData', chkArr[i]);
			
			jsonData.ITEM_CD = chkArr[i];
			jsonData.DESC1 = rowObj.DESC1;
			jsonData.UNIT = rowObj.UNIT4;
			jsonData.QUANTITY = rowObj.QUANTITY;
			jsonData.ITI_PALLET = toFloat(rowObj.ITI_PALLET.replaceAll(',', ''));
			jsonData.FIREPROOF_YN = rowObj.FIREPROOF_YN;
			jsonArray.push(jsonData);
		}
		
	} else {
		//각 ROW의 선택 버튼 클릭시
		var jsonData = new Object();
		var rowObj = $('#shiptoUseGridList').getRowData(rowId);
		
		jsonData.ITEM_CD = rowObj.ITEM_CD;
		jsonData.DESC1 = rowObj.DESC1;
		jsonData.UNIT = rowObj.UNIT4;
		jsonData.QUANTITY = rowObj.QUANTITY;
		jsonData.ITI_PALLET = toFloat(rowObj.ITI_PALLET.replaceAll(',', ''));
		jsonData.FIREPROOF_YN = rowObj.FIREPROOF_YN;
		jsonArray.push(jsonData);
	}
	
	setItemFromPop(jsonArray);
}


//납품처 선택 시 사용했던 모든 품목 조회. 그리드 내 모양 함수 2025-05-22 ijy
function setItiImage1(cellValue, options, rowdata) { //이미지
	return '<img src="${url}/data/item/' + cellValue + '" onerror="this.src=\'${url}/include/images/admin/list_noimg.gif\'" width="30" alt="image" />';
}
function setPallet(cellval, options, rowObject){ //파레트 적재 단위
	return addComma(cellval);
}
function addItemButton(cellValue, options, rowdata) { //선택 버튼
	return '<button type="button" class="btn btn-info writeObjectClass order-save-btn" onclick=\'addItem("'+options.rowId+'");\'>선택</button>';
}


//2025-06-02 ijy. 쿼테이션 번호와 품목코드로 주문접수가 가능한 품목인지 체크하고 등록되지 않은 품목은 붉은색으로 표기.
function quotationVerification(){
	var returnFlag = false;
	var quoteQt    = $('input[name="v_shiptoqt"]').val();
	var itemList   = $('input[name="m_itemcd"]').map(function(){
		return $(this).val();
	}).get();
	var itemCd  = itemList.join(',');
	
	$.ajax({
		async : false,
		url : '${url}/admin/quotation/checkQuotationItemListAjax.lime',
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
					
					var grid = $("#gridList");
					var rowIds = grid.jqGrid('getDataIDs');
					
					rowIds.forEach(function(id, idx) {
						var rowData = grid.jqGrid('getRowData', id);
						
						for(var i = 0; i < missingArr.length; i++) {
							if(rowData.ITEM_CD && rowData.ITEM_CD.startsWith(missingArr[i])){
								grid.find('tr[id="'+id+'"]').addClass('errorRow'); //등록되지 않은 품목은 글자를 붉은색으로 표기
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





//상단 기상 정보 조회 2025.06.16 hsg
/*
function weekWeatherForecastApi(){
	let forecastType = "2"
	
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
			//debugger
			if(data.resultCode == '00') {
				//debugger
				
				// 첫 번째 도시의 데이터만 사용 (또는 선택한 도시)
				var selectedCityData = data.weatherList[0]; // 첫 번째 도시 선택
				var forecasts = selectedCityData.cityWeatherList;
				//debugger

				var html = '';

				// 7일간의 날씨 데이터 처리
				forecasts.slice(0, 7).forEach(function(forecast, index) {
				    var date = forecast.weather_date;
				    var dayOfWeek = forecast.day_of_week;
				    var minTemp = Math.round(forecast.am_temp);
				    var maxTemp = Math.round(forecast.pm_temp);
				    var amWeather = forecast.am_weather; // 오전 날씨 또는 대표 날씨
				    var pmWeather = forecast.pm_weather; // 오후 날씨 또는 대표 날씨
				    var amPop = forecast.am_pop || 0; // 오전 강수확률
				    var pmPop = forecast.pm_pop || 0; // 오후 강수확률
				    
				    // 평균 강수확률 계산
				    var avgPop = Math.round((amPop + pmPop) / 2);
				    
				    // 대표 날씨 아이콘 선택 (오후 날씨 우선)
				    var mainWeather = pmWeather || amWeather;
				    
				    // 날짜 포맷팅 (MM/DD 형식)
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
				    
				    // 오늘/내일 표시
				    var dayLabel = '';
				    if (index === 0) {
				        dayLabel = '오늘';
				    } else if (index === 1) {
				        dayLabel = '내일';
				    } else {
				        dayLabel = dayOfWeek;
				    }
				    
				    html += '<span class="weather-day-card' + (index === 0 ? ' today' : '') + '">';
				    html += '  <span class="weather-date">' + formattedDate + '</span>';
				    html += '  <span class="weather-day">' + dayLabel + '</span>';
				    
				    html += '  <span class="weather-icon-container">';
				    html += '    <img src="${url}/data/weather/날씨_' + mainWeather + '.png" ';
				    html += '         onerror="this.src=\'${url}/data/weather/날씨_맑음.png\'" ';
				    html += '         class="weather-icon" alt="날씨" />';
				    html += '  </span>';
				    
				    html += '  <span class="weather-temps">';
				    html += '    <span class="weather-temp-high">' + maxTemp + '°</span>';
				    html += '    <span class="weather-temp-low">' + minTemp + '°</span>';
				    html += '  </span>';
				    
				    if (avgPop > 0) {
				        html += '  <span class="weather-rain">' + avgPop + '%</span>';
				    }
				    
				    html += '</span>';
				});

			    $("#cityName").text(selectedCityData.city || '');
				$("#weekForecast").html(html);

				// 10초마다 다음 도시로 순환, 마지막 도시 후엔 다시 처음부터 반복
				setInterval(function() {
				    cityIndex = (cityIndex + 1) % cityList.length;
				    weekWeatherForecastApi(cityList[cityIndex]);
				}, 10000);
				
			} else {
				
			}
		},
		error : function(request,status,error){
		}
	});

	// 10초마다 도시 변경 및 날씨 불러오기
	//setTimeOut(weekWeatherForecastApi, 10000, current = (current + 1) % cities.length);
}
 */


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
		url : '${url}/admin/base/getWeatherForecastApiAjax.lime',
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
				//html += '<span style="margin-right:12px;">' + dayOfWeek + '</span>';
/* 
				html += '<span style="margin-right:10px;">오전 ' + amPop + '%</span>';
				html += '<img src="${url}/data/weather/날씨_' + amWeather + '.png" onerror="this.src=\'${url}/data/weather/날씨_맑음.png\'" width="24" height="24" style="vertical-align:middle;margin-right:4px;" alt="am" />';
				html += '<img src="${url}/data/weather/날씨_' + pmWeather + '.png" onerror="this.src=\'${url}/data/weather/날씨_맑음.png\'" width="24" height="24" style="vertical-align:middle;margin-right:4px;" alt="pm" />';
				html += '<span style="margin-left:10px;">오후 ' + pmPop + '%</span>';
 */
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

				$("#weatherDiv").html(html);
				
			} else {
				
			}
		},
		error : function(request,status,error){
		}
	});
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
    <!-- 한 줄 flex row! -->
    <div class="header-row-flex">
      <h3 class="header-title">
        <c:choose>
          <c:when test="${'ADD' eq pageType}">주문등록</c:when>
          <c:when test="${'EDIT' eq pageType}">주문등록</c:when>
          <c:otherwise></c:otherwise>
        </c:choose>
      </h3>
      <div class="weather-header-row">
        <div id="cityName"></div>
        <div class="weather-container">
          <div class="weather-week-forecast" id="weekForecast"></div>
        </div>
      </div>
					<div class="page-right">
						<button type="button" class="btn btn-line f-black" title="목록" onclick="document.location.href='${url}/admin/order/orderList.lime'"><i class="fa fa-list-ul"></i><em>목록</em></button>
						<button type="button" class="btn btn-line f-black" title="새로고침" onclick="window.location.reload();"><i class="fa fa-refresh"></i><em>새로고침</em></button>
					</div>
				</h3>
			</div>
      </div>

			        <div class="weather-source">
			          * 자료출처 (날씨정보:기상청, 지역좌표:카카오)
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
									<button type="button" class="btn btn-info writeObjectClass order-save-btn" onclick="weatherForecastApi();"><c:out value="날씨정보" /></button>
									<c:choose>
										<c:when test="${empty custOrderH.REQ_NO}">
											<button type="button" class="btn btn-info writeObjectClass order-save-btn" onclick="showModal();" /><c:out value="${orderStatus['00']}" /></button> <%-- 주문접수 --%>
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
													
													<input type="text" onchange="javascript:weatherForecastApi();" class="w-40 p-r-md bdStartDateClass" name="v_requestdate" data-field="datetime" data-startend="start" data-startendelem=".bdEndDateClass" value="${v_requestdatedt}" readonly="readonly" /><i class="fa fa-calendar i-calendar"></i>
													<input type="hidden" name="m_requestdt" value="<fmt:formatDate value="${requestDate}" pattern="yyyyMMdd"/>" /> <%-- YYYYMMDD --%>
													<input type="hidden" name="m_requesttime" value="<fmt:formatDate value="${requestTime}" pattern="HHmm"/>" /> <%-- HHMM --%>
													
													
													<!-- 2025.06.12 ijy 날씨 아이콘 -->
													<div id="weatherDiv"></div>
													
													
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
								
							<!-- 납품처 선택 시 해당 납품처에서 사용했던 모든 품목 조회 및 선택 기능 제공. 2025-05-22 ijy -->
							<div class="panel-body" id="shiptoUseItemDiv" style="display:none;">
								<h5 class="table-title">납품처 사용 품목 기록 </h5>
								<div class="btnList writeObjectClass">
									<a href="javascript:;" onclick="addItem('');" class="btn btn-line">선택추가</a>
								</div>
								<div class="table-responsive in min">
									<table id="shiptoUseGridList" class="display table tableList nowrap" width="100%" border="0" cellpadding="0" cellspacing="0">
									</table>
									
									<table id="shiptoUseNoList" class="display table noList" width="100%" border="0" cellpadding="0" cellspacing="0" style="height:150px;">
										<tbody>
											<tr>
												<td>사용 품목 기록이 없습니다.</td>
											</tr>
										</tbody>
									</table>
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
				<!-- ↓↓↓↓↓↓↓↓↓ 2025-04-18 hsg Italian Stretch No.32 : E-Order Admin 주문등록 Page에서 주문등록 버튼 클릭 시 한번 더 확인 후 주문 접수 할 수 있도록 변경하기 위해 모달 팝업 추가 ↓↓↓↓↓↓↓↓↓ -->
				<%!  // JSP 선언부에 스크립틀릿 대신 선언부를 쓰는 걸 권장함 %>
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
				    <%-- 예를 들어 JSP 변수나 EL로 데이터 넣기 --%>
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
				    <button type="button" class="btn-execute" onclick="dataIn(this, '00', '${custOrderH.REQ_NO}')"><c:out value="${orderStatus['00']}" /></button>
				    <button type="button" class="btn-cancel" onclick="closeModal()">실행 취소</button>
				  </div>
				</div>
				
				<!-- ↑↑↑↑↑↑↑↑↑ 2025-04-18 hsg Italian Stretch No.32 : E-Order Admin 주문등록 Page에서 주문등록 버튼 클릭 시 한번 더 확인 후 주문 접수 할 수 있도록 변경하기 위해 모달 팝업 추가 ↑↑↑↑↑↑↑↑↑ -->
				
				
			</div>
			<!-- //Main Wrapper -->
			
			<%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>
			
		</div>
		<!-- //Page Inner -->
	</main>
	<!-- //Page Content -->
	
</body>
</html>
```

---------------------------------------------------------------------


좋아. **주문 접수 전**에 `Shipto_NM`(납품처명)으로 **ZCPQ/ZOBJ 판별 → ZCPQ면 품목검증**으로 이어지게 JS 로직을 추가/수정해줄게.
아래 코드 그대로 넣으면 돼. 위치와 변경 지점도 콕 집어 줄게.

---

# 1) “검증 유틸리티” 함수들 추가

아래 블록을 **기존 `<script type="text/javascript">` (Order ADD) 블록 안**에 넣어줘.
추천 위치: `console.log('Order ADD');` 아래나 `dataIn2` 정의 위.

```javascript
/** =======================
 *  Quotation 판별 유틸
 *  정책:
 *   - KR_ 로 시작  => ZCPQ
 *   - KR 로 시작(언더스코어 없음) => ZOBJ
 *   - 그 외 => ZOBJ
 *  + 예외(화이트리스트)는 ZOBJ 강제
 * ======================= */

// 서버에서 내려줄 수 있으면 AJAX 로딩(권장). 없으면 fallback 사용.
var ZOBJ_EXCEPTION_SHIPTO = new Set();  // 정확일치 명단 (예: 'KR산업')

function loadZobjExceptionList() {
  // 1) 서버에서 관리 가능하면 여기 활성화
  $.ajax({
    async: true,
    url: '${url}/admin/quotation/getZobjExceptionShiptoList.lime', // 예: [{shiptoNm:'KR산업'}, ...]
    type: 'POST',
    dataType: 'json',
    success: function (data) {
      if (data && Array.isArray(data.list)) {
        data.list.forEach(function (r) {
          if (r.shiptoNm) ZOBJ_EXCEPTION_SHIPTO.add(String(r.shiptoNm).trim());
        });
      }
    },
    error: function () {
      // 2) 서버 미구현/오류 시 fallback
      // 현재 요구사항: KR산업 1건만 해당
      ZOBJ_EXCEPTION_SHIPTO.add('KR산업');
    }
  });
}

// 대소문자, 공백 정리
function normalizeShiptoNm(s) {
  if (!s) return '';
  return String(s).trim();
}

// 예외(화이트리스트) 여부
function isZobjException(shiptoNm) {
  var s = normalizeShiptoNm(shiptoNm);
  return ZOBJ_EXCEPTION_SHIPTO.has(s);
}

// KR_ 로 시작?
function isStartWithKRUnderscore(shiptoNm) {
  var s = normalizeShiptoNm(shiptoNm);
  // 대소문자 무시
  return /^kr_/.test(s.toLowerCase());
}

// KR 로 시작하지만 언더스코어 없이 붙음?
function isStartWithKRNoUnderscore(shiptoNm) {
  var s = normalizeShiptoNm(shiptoNm).toLowerCase();
  if (!s.startsWith('kr')) return false;
  // 'kr' 다음 문자가 '_' 가 아니면 true (예: 'KR산업', 'KR건축')
  return s.length > 2 && s.charAt(2) !== '_';
}

/** 최종 판별: 'ZCPQ' | 'ZOBJ' */
function decideQuotationSourceByShiptoNm(shiptoNm) {
  var s = normalizeShiptoNm(shiptoNm);

  // 1) 예외 우선 (정확일치)
  if (isZobjException(s)) return 'ZOBJ';

  // 2) KR_ 로 시작 → ZCPQ
  if (isStartWithKRUnderscore(s)) return 'ZCPQ';

  // 3) KR 로 시작(언더스코어 없음) → ZOBJ
  if (isStartWithKRNoUnderscore(s)) return 'ZOBJ';

  // 4) 그 외 전부 ZOBJ
  return 'ZOBJ';
}

// 페이지 로드시 예외목록 한 번 로드
$(document).ready(function () {
  loadZobjExceptionList();
});
```

> **포인트**
>
> * 서버에서 예외명단을 관리할 수 있게 AJAX 훅을 열어두었고, 실패 시 **fallback** 으로 `KR산업` 을 기본 등록.
> * 정책 변경이 필요하면 정규식 함수 두 군데만 손보면 됨.

---

# 2) \*\*주문접수 전 로직(dataIn2)\*\*에 “판별 → 검증” 추가

`dataIn2(obj, status, reqNo)` 함수 안에서 **우편번호 체크 직후**, 기존 유효성 검사 `dataValidation()` 통과 후, 품목검증을 `Shipto_NM` 기준으로 강제하도록 변경한다.

### 변경 전(일부)

```javascript
// ...
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

// 납품처는 필수값이 아님...
// 2025-06-02 ijy. 일단 쿼테이션 번호가 있을때만 ...
var quoteQt = $('input[name="v_shiptoqt"]').val();
if(quoteQt != null && quoteQt != '' ){
  var flag = quotationVerification();
  if(!flag){
    $(obj).prop('disabled', false);
    return;
  }
}
```

### 변경 후(교체)

```javascript
// ...
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

/** =======================
 *  [신규] Shipto_NM 기반 출처 결정 → ZCPQ면 품목검증 강제
 * ======================= */
var shiptoNm = $('input[name="v_shiptonm"]').val();
var quoteQt  = $('input[name="v_shiptoqt"]').val(); // ZCPQ면 필수(검증 키로 사용)

var quotationSrc = decideQuotationSourceByShiptoNm(shiptoNm); // 'ZCPQ' | 'ZOBJ'

// ZCPQ 이면: 품목 유무 검증 필수
if (quotationSrc === 'ZCPQ') {
  if (!quoteQt) {
    alert("OneCRM(ZCPQ) 납품처로 판정되었습니다.\nQuation QT(견적번호)가 없으면 주문접수가 불가합니다.");
    $(obj).prop('disabled', false);
    return;
  }
  var ok = quotationVerification(); // (기존) quoteQt + itemCd로 검증, 실패 시 붉은색 표시
  if (!ok) {
    // quotationVerification 내부에서 붉은색 마킹됨
    alert("견적(QT)에 포함되지 않은 품목이 있어 주문접수가 불가합니다.\n붉은색 표시 품목을 확인해 주세요.");
    $(obj).prop('disabled', false);
    return;
  }
} else {
  // ZOBJ → 품목검증 스킵 (기존 정책 유지)
  // 아무 것도 하지 않음
}
```

> **바뀐 점**
>
> * **기존**: *견적번호가 있을 때만* 검증
> * **변경**: **`Shipto_NM` 판정이 ZCPQ면** → 무조건 검증(= 견적번호 없으면 막음)
> * **ZOBJ면**: 기존처럼 검증 생략

---

# 3) (선택) 출처를 화면/로그에 남기고 싶다면

운영 추적을 위해 hidden 필드 하나 두고 저장해도 좋아.

### JSP 폼 hidden 추가

```html
<input type="hidden" id="m_quotation_src" name="m_quotation_src" value="" />
```

### dataIn2 내 판별 직후 세팅

```javascript
$('#m_quotation_src').val(quotationSrc); // 'ZCPQ' | 'ZOBJ'
```

> 서버 단에서 저장하면, 사후 분석(오분류/예외 추가 등)에 유용함.

---

# 4) 동작 요약

* 납품처명 `KR_` 시작 → **ZCPQ**로 간주 → **견적(QT) 필수 + 품목검증 강제**
* 납품처명 `KR` 시작(언더스코어 없음) → **ZOBJ**
* 예외명단(정확일치)은 **무조건 ZOBJ**
* 그 외 → **ZOBJ**
* **검증 실패 시**: 기존 `quotationVerification()`이 붉은색 마킹, 접수 차단

---

# 5) 체크리스트

* [ ] 서버에 예외목록 API가 있으면 `getZobjExceptionShiptoList.lime` 구현/연동
  응답 예: `{ list: [ {shiptoNm:'KR산업'}, ... ] }`
  없다면 현재처럼 fallback 세팅 유지
* [ ] `quotationVerification()`은 기존 그대로 사용(응답 코드 `0000`/`0360` 처리 유지)
* [ ] 납품처 선택 팝업에서 값 세팅 후 별도 액션은 필요 없음(접수 시 최종 판단)

---

필요하면 **전체 삽입 위치가 표시된 패치본**도 만들어 줄게. 지금은 핵심 변경지점만 정리했어.

---------------------------------------------------------------------

# 어떤 식으로 테스트를 진행해야 하는지 테스트 시나리오를 작성해줘

---------------------------------------------------------------------


좋아. **납품처명 기반 ZCPQ/ZOBJ 판별 + ZCPQ 시 품목검증 강제** 로직에 대해, 현 개발환경(jQuery/jqGrid, JSP, AJAX)을 기준으로 **테스트 시나리오**를 단계별로 정리했어.
아래 순서대로 진행하면 “정책 일치 여부 → 화면 동작 → 서버 응답 변조 → 예외관리”까지 깔끔히 검증 가능해.

---

# 0) 사전 준비(테스트 데이터 & 훅)

### 0-1. 예외목록 API 더미 준비(선택)

* 엔드포인트: `/admin/quotation/getZobjExceptionShiptoList.lime`
* 정상 응답(JSON):

```json
{ "list": [ { "shiptoNm": "KR산업" } ] }
```

* 오류 응답(네트워크/500)을 한 번 발생시켜 **fallback 로직**(KR산업 자동등록)도 확인.

### 0-2. 품목검증 API 더미 준비(필수)

* 엔드포인트: `/admin/quotation/checkQuotationItemListAjax.lime`
* 요청 파라미터: `quoteQt`, `itemCd(콤마구분)`
* 응답 패턴:

  * **성공(모든 품목 포함)**:

    ```json
    { "RES_CODE": "0000" }
    ```
  * **누락 품목 존재**:

    ```json
    { "RES_CODE": "0360", "missingItem": "KNAUF001,KNAUF003" }
    ```
  * **기타 오류**:

    ```json
    { "RES_CODE": "9999", "message": "error ..." }
    ```

### 0-3. 화면 입력공통

* 납품처명 input: `name="v_shiptonm"`
* 견적번호(QT) input: `name="v_shiptoqt"`
* 품목그리드: `#gridList` (ITEM\_CD, DESC1, QUANTITY 등)
* 접수 버튼 동작: `dataIn(this, '00', REQ_NO)`

---

# 1) 기능별 테스트 케이스 매트릭스

## 1-1. 출처 판별(Shipto\_NM)만으로

| TC | Shipto\_NM 입력값 | 기대 판정 | 비고                     |
| -- | -------------- | ----- | ---------------------- |
| S1 | `KR_수서지점`      | ZCPQ  | **KR\_** prefix        |
| S2 | `kr_강남`        | ZCPQ  | 소문자 케이스                |
| S3 | `KR_KR산업`      | ZCPQ  | KR\_로 시작               |
| S4 | `KR산업`         | ZOBJ  | 예외목록에 등록됨(또는 fallback) |
| S5 | `KR건축`         | ZOBJ  | KR 시작이나 `_` 없음         |
| S6 | `한국목재 인천지점`    | ZOBJ  | KR 미포함                 |
| S7 | `한국KR상사`       | ZOBJ  | KR이 중간                 |
| S8 | `  KR_용산  `    | ZCPQ  | 앞뒤 공백(trim) 적용 확인      |
| S9 | (빈값) \`\`      | ZOBJ  | 납품처 미선택 시 기본 ZOBJ      |

> 확인 포인트: 각 케이스에서 `decideQuotationSourceByShiptoNm()` 반환값이 기대와 일치하는지 콘솔 로그/브레이크포인트로 확인.

## 1-2. ZCPQ인 경우 “품목검증 강제”

| TC | Shipto\_NM |     QT입력 | 품목구성                | API 응답                     | 기대 동작                                |
| -- | ---------- | -------: | ------------------- | -------------------------- | ------------------------------------ |
| Z1 | `KR_수서지점`  |       없음 | 1개 이상               | -                          | **접수 차단 + “QT 필요” 알림**               |
| Z2 | `KR_수서지점`  | `QT1001` | `KNAUF001`          | `0000`                     | 접수 진행                                |
| Z3 | `KR_수서지점`  | `QT1001` | `KNAUF001,KNAUF003` | `0360`, missing=`KNAUF003` | **접수 차단 + 누락품목 행에 `.errorRow`(붉은색)** |
| Z4 | `KR_수서지점`  | `QT1001` | 없음                  | (검증 전 dataValidation에서 차단) | **“품목 선택” 알림**                       |

## 1-3. ZOBJ인 경우 “품목검증 생략”

| TC | Shipto\_NM  |     QT입력 | 품목구성  | API 호출 기대  | 기대 동작 |
| -- | ----------- | -------: | ----- | ---------- | ----- |
| O1 | `KR산업`      |       없음 | 1개 이상 | **호출 안 함** | 접수 진행 |
| O2 | `KR건축`      | `QT1001` | 1개 이상 | **호출 안 함** | 접수 진행 |
| O3 | `한국목재 인천지점` |      임의값 | 1개 이상 | **호출 안 함** | 접수 진행 |

> 확인 포인트: ZOBJ일 때 기존 코드에 있던 **“QT가 있을 때만 검증”** 경로가 제거/우회되었는지 반드시 확인(=호출 없어야 함).

## 1-4. 예외목록 동작

| TC | 예외 API 상태      | Shipto\_NM | 기대 판정 | 기대 동작                   |
| -- | -------------- | ---------- | ----- | ----------------------- |
| E1 | 정상 응답(\[KR산업]) | `KR산업`     | ZOBJ  | 검증 생략                   |
| E2 | 500/네트워크 에러    | `KR산업`     | ZOBJ  | fallback 로 ZOBJ 처리      |
| E3 | 정상 응답(\[KR산업]) | `KR_산업`    | ZCPQ  | 예외는 **정확일치**만 적용 → ZCPQ |

---

# 2) 단계별 테스트 절차 (수동 시나리오)

## 2-1. 기본 판별 로직 확인(콘솔)

1. 페이지 로드 → 콘솔에서 `loadZobjExceptionList()` 완료 확인
2. 아래를 순차 실행하고 반환값 확인:

```js
decideQuotationSourceByShiptoNm('KR_수서지점'); // ZCPQ
decideQuotationSourceByShiptoNm('KR산업');     // ZOBJ
decideQuotationSourceByShiptoNm('KR건축');     // ZOBJ
decideQuotationSourceByShiptoNm('한국목재');   // ZOBJ
```

## 2-2. ZCPQ + QT 누락 차단

1. 납품처명: `KR_수서지점` 입력
2. QT 빈값, 품목 1개 추가
3. “주문접수” 클릭 → **“QT 필요” 알림** 및 접수 불가 확인

## 2-3. ZCPQ + 검증 성공

1. 납품처명: `KR_수서지점`
2. QT: `QT1001`, 품목: `KNAUF001`
3. API 응답을 `0000`으로 세팅
4. “주문접수” → **정상 접수** 확인

## 2-4. ZCPQ + 검증 실패(누락 표시)

1. 납품처명: `KR_수서지점`
2. QT: `QT1001`, 품목: `KNAUF001, KNAUF003`
3. API 응답을 `0360` + `missingItem="KNAUF003"`로 세팅
4. “주문접수” → **접수 차단** + 그리드에서 `KNAUF003` 행 **붉은색(`.errorRow`)** 적용 확인

## 2-5. ZOBJ(예외) 동작

1. 납품처명: `KR산업`
2. QT 없어도 **접수 진행**되는지 확인(품목만 있으면 됨)
3. 네트워크 오류 등으로 예외 API 실패 시에도 동일 동작 확인(fallback)

## 2-6. ZOBJ(일반) 동작

1. 납품처명: `KR건축`
2. QT가 있든 없든 **검증 호출 없이 접수**되는지 확인

## 2-7. 경계/정규화

1. 납품처명: `'  kr_용산  '`(공백/소문자) → ZCPQ로 판단되는지
2. 납품처명: `kr산업`(예외 정확일치 아님) → ZOBJ로 판단되는지
3. 납품처명: 빈값 → ZOBJ로 판단되는지

---

# 3) 에러/예외 상황 테스트

## 3-1. 예외목록 API 실패

* API 500/네트워크 오류 발생 → **fallback 로직** 동작 여부 확인

  * `KR산업` 입력 시 ZOBJ 판정되는지

## 3-2. 품목검증 API 실패

* ZCPQ 케이스에서 검증 API를 의도적으로 500/타임아웃 처리

  * 현재 코드는 `error` 시 `returnFlag = false` 처리 → **접수 차단** 동작 확인
  * 사용자 메시지 보강 필요 시(선택): “검증 서버 오류” 안내 메시지 추가 고려

## 3-3. 누락품목 표시 중복

* 같은 품목을 여러 행에 넣고 `missingItem`에 포함시킴

  * 모든 해당 행에 `.errorRow` 적용되는지 확인
  * 재시도 시 이전 `.errorRow` 초기화 로직 필요 여부 점검(필요하면 검증 시작 전에 `$('#gridList tr').removeClass('errorRow')` 추가)

---

# 4) 자동화 테스트(선택)

## 4-1. 단위테스트(판별함수)

* 테스트 러너(Jest 등) 사용 가능 시, 아래 케이스를 함수 단위로 검증

```js
expect(decideQuotationSourceByShiptoNm('KR_수서')).toBe('ZCPQ');
expect(decideQuotationSourceByShiptoNm('KR산업')).toBe('ZOBJ');
expect(decideQuotationSourceByShiptoNm('KR건축')).toBe('ZOBJ');
expect(decideQuotationSourceByShiptoNm('한국KR상사')).toBe('ZOBJ');
expect(decideQuotationSourceByShiptoNm('')).toBe('ZOBJ');
```

## 4-2. E2E(Cypress)

* 시나리오 Z1~~Z4, O1~~O3를 버튼 클릭/그리드 DOM 검증으로 자동화
* `.errorRow` 클래스 존재 유무, alert 메시지 캡쳐, AJAX 스텁 응답에 따라 동작 확인

---

# 5) 회귀 테스트 포인트(필수)

* **기존 “QT가 있으면 검증” 로직**이 **ZOBJ에도 발동하지 않는지**(=이제는 “ZCPQ일 때만 검증”)
* **dataValidation()**(주소/전화/납품요청일/품목수량 등)과의 충돌 없음
* 모달 접수/바로 접수(EDIT) 등 **진입 경로가 달라도 동일 정책** 적용
* **클릭 중복 방지(clickCnt)** 로 인한 이중 호출 없음
* **QMS 연동**(내화구조 포함 시) 경로 영향 없음

---

# 6) 테스트 데이터 제안(샘플)

### 6-1. 견적/품목(서버 측)

* `QT1001` → 포함 품목: `KNAUF001, KNAUF002`
* `QT1002` → 포함 품목: `KNAUF010`
* 검증 API는 요청 `itemCd` 교차 대비로 `0000 / 0360` 응답 컨트롤

### 6-2. 화면 입력 샘플

* Shipto\_NM:

  * ZCPQ: `KR_수서지점`, `kr_강남`
  * ZOBJ: `KR산업`(예외), `KR건축`, `한국목재 인천지점`, `한국KR상사`
* QT: `QT1001`, 빈값
* 품목: `KNAUF001`, `KNAUF003`(누락 유도), `KNAUF010`

---

# 7) 버그 예방 체크리스트

* [ ] 검증 시작 전에 `.errorRow` 초기화
* [ ] `decideQuotationSourceByShiptoNm()` 대소문자·공백 정규화 일관성
* [ ] 예외목록 “정확일치”만 적용됨(부분일치 금지)
* [ ] 접수 버튼 비활성화/활성화 타이밍 정확(차단 시 재활성)
* [ ] alert 메시지 한글 UX 문구 최종 확정
* [ ] 네트워크 오류 시 사용자 가이드 메시지 필요 여부

---

필요하면 **체크리스트용 Excel**이나 **Cypress 스펙 파일 템플릿**까지 만들어줄게. 원하는 형태 알려줘!


---------------------------------------------------------------------

# 아래의 O\_SHIPTO 테이블 조회 내용을 기준으로 "1-1. 출처 판별(Shipto\_NM)만으로"의 케이스별 insert 문을 만들어 줘.

CUST\_CD	CUST\_NM	SHIPTO\_CD	SHIPTO\_NM	ADD1	ADD2	ADD3	ADD4	ZIP\_CD	BUILDING\_TY	BUILDING\_NM	BUSINESS\_TY	BUSINESS\_NM	INSERT\_DT	DUMMY	DIST\_CLASS	QUOTE\_QT	ANGDT	BNDDT	CUST\_CD	Comment	STOCK\_NY
10178299	벽산건재	9005335149	화개초등학교 - 벽산건재	경상남도 하동군 화개면 화개로 139	\[NULL]	01038630279	\[NULL]	52303	\[NULL]	\[NULL]	\[NULL]	\[NULL]	20240220161057	\[NULL]	\[NULL]	20419272  	20240220	20250131	10178299	\[NULL]	\[NULL]
10177506	벽산에이엔디	9005501849	강원도 홍천군 용가리캠핑장 - 벽산에이엔디	강원도 홍천군 설악로 2692	\[NULL]	\[NULL]	\[NULL]	25156	\[NULL]	\[NULL]	\[NULL]	\[NULL]	20240429152743	\[NULL]	\[NULL]	20433414  	20240429	20251231	10177506	\[NULL]	\[NULL]
10178299	벽산건재	9006173620	진주중앙고등학교 천장 텍스 보수공사 - 벽산건재	경상남도 진주시 하대로 59	\[NULL]	01090559269	\[NULL]	52733	\[NULL]	\[NULL]	\[NULL]	\[NULL]	20250113110355	\[NULL]	\[NULL]	20504602  	20250113	20260131	10178299	\[NULL]	\[NULL]

---------------------------------------------------------------------


좋아. 아래는 **O\_SHIPTO**에 “1-1 출처 판별(Shipto\_NM)만으로”의 9가지 케이스(S1\~S9)를 바로 넣어볼 수 있는 **테스트 데이터 INSERT문**이야.

* 규칙: **KR\_ 시작 → ZCPQ**, **KR 시작(언더스코어 없음) → ZOBJ**, 그 외 → ZOBJ, **예외(예: KR산업)** → ZOBJ
* 포맷: **Knauf-SQL-Style v1.1**(스키마 한정 `dbo.`, 한글 `N''`, 컬럼 1줄 1개, 선행 콤마 정렬)
* 컬럼은 제공된 조회 결과 기준으로 합리적으로 구성했어. 실제 스키마와 다르면 컬럼명만 맞춰서 실행하면 돼.

> 주의: `SHIPTO_CD`는 테스트용으로 **서로 다른 값**을 부여했어(충돌 방지).
> `INSERT_DT`/`ANGDT`/`BNDDT`는 샘플 문자열(CHAR)로 넣었고, `NULL` 표기는 실제 \[NULL] 대신 **NULL**로 처리했어.
> 일부 컬럼명(`COMMENT`)은 충돌 방지를 위해 대괄호로 감쌌어.

```sql
/* =========================================================
   O_SHIPTO 테스트 데이터 (출처 판별 S1~S9)
   - S1,S3: 1번 행(벽산건재) 기반
   - S2,S4,S5,S6: 2번 행(벽산에이엔디) 기반
   - S7,S8,S9: 3번 행(벽산건재) 기반
   ========================================================= */

-- S1: KR_로 시작 → ZCPQ (예: KR_수서지점)  [기대: ZCPQ]
INSERT INTO dbo.O_SHIPTO (
       CUST_CD
     , CUST_NM
     , SHIPTO_CD
     , SHIPTO_NM
     , ADD1
     , ADD2
     , ADD3
     , ADD4
     , ZIP_CD
     , BUILDING_TY
     , BUILDING_NM
     , BUSINESS_TY
     , BUSINESS_NM
     , INSERT_DT
     , DUMMY
     , DIST_CLASS
     , QUOTE_QT
     , ANGDT
     , BNDDT
     , [COMMENT]
     , STOCK_NY
)
VALUES (
       '10178299'
     , N'벽산건재'
     , '9900001001'
     , N'KR_화개초등학교 - 벽산건재'
     , N'경상남도 하동군 화개면 화개로 139'
     , NULL
     , '01038630279'
     , NULL
     , '52303'
     , NULL
     , NULL
     , NULL
     , NULL
     , '20250814090100'
     , NULL
     , NULL
     , '20419272'
     , '20240220'
     , '20250131'
     , NULL
     , NULL
);

-- S2: kr_ (소문자)로 시작 → ZCPQ  [기대: ZCPQ]
INSERT INTO dbo.O_SHIPTO (
       CUST_CD
     , CUST_NM
     , SHIPTO_CD
     , SHIPTO_NM
     , ADD1
     , ADD2
     , ADD3
     , ADD4
     , ZIP_CD
     , BUILDING_TY
     , BUILDING_NM
     , BUSINESS_TY
     , BUSINESS_NM
     , INSERT_DT
     , DUMMY
     , DIST_CLASS
     , QUOTE_QT
     , ANGDT
     , BNDDT
     , [COMMENT]
     , STOCK_NY
)
VALUES (
       '10177506'
     , N'벽산에이엔디'
     , '9900001002'
     , N'kr_용가리캠핑장 - 벽산에이엔디'
     , N'강원도 홍천군 설악로 2692'
     , NULL
     , NULL
     , NULL
     , '25156'
     , NULL
     , NULL
     , NULL
     , NULL
     , '20250814090200'
     , NULL
     , NULL
     , '20433414'
     , '20240429'
     , '20251231'
     , NULL
     , NULL
);

-- S3: KR_KR… (중복 KR, 선두 KR_ 존재) → ZCPQ  [기대: ZCPQ]
INSERT INTO dbo.O_SHIPTO (
       CUST_CD
     , CUST_NM
     , SHIPTO_CD
     , SHIPTO_NM
     , ADD1
     , ADD2
     , ADD3
     , ADD4
     , ZIP_CD
     , BUILDING_TY
     , BUILDING_NM
     , BUSINESS_TY
     , BUSINESS_NM
     , INSERT_DT
     , DUMMY
     , DIST_CLASS
     , QUOTE_QT
     , ANGDT
     , BNDDT
     , [COMMENT]
     , STOCK_NY
)
VALUES (
       '10178299'
     , N'벽산건재'
     , '9900001003'
     , N'KR_KR산업 - 벽산건재'
     , N'경상남도 하동군 화개면 화개로 139'
     , NULL
     , '01038630279'
     , NULL
     , '52303'
     , NULL
     , NULL
     , NULL
     , NULL
     , '20250814090300'
     , NULL
     , NULL
     , '20419272'
     , '20240220'
     , '20250131'
     , NULL
     , NULL
);

-- S4: KR로 시작하지만 '_' 없음 → ZOBJ (예외: KR산업)  [기대: ZOBJ]
INSERT INTO dbo.O_SHIPTO (
       CUST_CD
     , CUST_NM
     , SHIPTO_CD
     , SHIPTO_NM
     , ADD1
     , ADD2
     , ADD3
     , ADD4
     , ZIP_CD
     , BUILDING_TY
     , BUILDING_NM
     , BUSINESS_TY
     , BUSINESS_NM
     , INSERT_DT
     , DUMMY
     , DIST_CLASS
     , QUOTE_QT
     , ANGDT
     , BNDDT
     , [COMMENT]
     , STOCK_NY
)
VALUES (
       '10177506'
     , N'벽산에이엔디'
     , '9900002004'
     , N'KR산업'
     , N'강원도 홍천군 설악로 2692'
     , NULL
     , NULL
     , NULL
     , '25156'
     , NULL
     , NULL
     , NULL
     , NULL
     , '20250814090400'
     , NULL
     , NULL
     , '20433414'
     , '20240429'
     , '20251231'
     , NULL
     , NULL
);

-- S5: KR로 시작(언더스코어 없음) → ZOBJ  [기대: ZOBJ]
INSERT INTO dbo.O_SHIPTO (
       CUST_CD
     , CUST_NM
     , SHIPTO_CD
     , SHIPTO_NM
     , ADD1
     , ADD2
     , ADD3
     , ADD4
     , ZIP_CD
     , BUILDING_TY
     , BUILDING_NM
     , BUSINESS_TY
     , BUSINESS_NM
     , INSERT_DT
     , DUMMY
     , DIST_CLASS
     , QUOTE_QT
     , ANGDT
     , BNDDT
     , [COMMENT]
     , STOCK_NY
)
VALUES (
       '10177506'
     , N'벽산에이엔디'
     , '9900002005'
     , N'KR건축 - 벽산에이엔디'
     , N'강원도 홍천군 설악로 2692'
     , NULL
     , NULL
     , NULL
     , '25156'
     , NULL
     , NULL
     , NULL
     , NULL
     , '20250814090500'
     , NULL
     , NULL
     , '20433414'
     , '20240429'
     , '20251231'
     , NULL
     , NULL
);

-- S6: KR 미포함 → ZOBJ  [기대: ZOBJ]
INSERT INTO dbo.O_SHIPTO (
       CUST_CD
     , CUST_NM
     , SHIPTO_CD
     , SHIPTO_NM
     , ADD1
     , ADD2
     , ADD3
     , ADD4
     , ZIP_CD
     , BUILDING_TY
     , BUILDING_NM
     , BUSINESS_TY
     , BUSINESS_NM
     , INSERT_DT
     , DUMMY
     , DIST_CLASS
     , QUOTE_QT
     , ANGDT
     , BNDDT
     , [COMMENT]
     , STOCK_NY
)
VALUES (
       '10177506'
     , N'벽산에이엔디'
     , '9900002006'
     , N'강원도 홍천군 용가리캠핑장 - 테스트S6'
     , N'강원도 홍천군 설악로 2692'
     , NULL
     , NULL
     , NULL
     , '25156'
     , NULL
     , NULL
     , NULL
     , NULL
     , '20250814090600'
     , NULL
     , NULL
     , '20433414'
     , '20240429'
     , '20251231'
     , NULL
     , NULL
);

-- S7: KR이 중간에만 포함 → ZOBJ  [기대: ZOBJ]
INSERT INTO dbo.O_SHIPTO (
       CUST_CD
     , CUST_NM
     , SHIPTO_CD
     , SHIPTO_NM
     , ADD1
     , ADD2
     , ADD3
     , ADD4
     , ZIP_CD
     , BUILDING_TY
     , BUILDING_NM
     , BUSINESS_TY
     , BUSINESS_NM
     , INSERT_DT
     , DUMMY
     , DIST_CLASS
     , QUOTE_QT
     , ANGDT
     , BNDDT
     , [COMMENT]
     , STOCK_NY
)
VALUES (
       '10178299'
     , N'벽산건재'
     , '9900003007'
     , N'한국KR상사 - 테스트S7'
     , N'경상남도 진주시 하대로 59'
     , NULL
     , '01090559269'
     , NULL
     , '52733'
     , NULL
     , NULL
     , NULL
     , NULL
     , '20250814090700'
     , NULL
     , NULL
     , '20504602'
     , '20250113'
     , '20260131'
     , NULL
     , NULL
);

-- S8: 앞뒤 공백 + KR_ 시작 → ZCPQ (trim 적용 기대)  [기대: ZCPQ]
INSERT INTO dbo.O_SHIPTO (
       CUST_CD
     , CUST_NM
     , SHIPTO_CD
     , SHIPTO_NM
     , ADD1
     , ADD2
     , ADD3
     , ADD4
     , ZIP_CD
     , BUILDING_TY
     , BUILDING_NM
     , BUSINESS_TY
     , BUSINESS_NM
     , INSERT_DT
     , DUMMY
     , DIST_CLASS
     , QUOTE_QT
     , ANGDT
     , BNDDT
     , [COMMENT]
     , STOCK_NY
)
VALUES (
       '10178299'
     , N'벽산건재'
     , '9900003008'
     , N'  KR_용산 공사현장  '
     , N'경상남도 진주시 하대로 59'
     , NULL
     , '01090559269'
     , NULL
     , '52733'
     , NULL
     , NULL
     , NULL
     , NULL
     , '20250814090800'
     , NULL
     , NULL
     , '20504602'
     , '20250113'
     , '20260131'
     , NULL
     , NULL
);

-- S9: 빈 문자열(또는 NULL) → ZOBJ  [기대: ZOBJ]
INSERT INTO dbo.O_SHIPTO (
       CUST_CD
     , CUST_NM
     , SHIPTO_CD
     , SHIPTO_NM
     , ADD1
     , ADD2
     , ADD3
     , ADD4
     , ZIP_CD
     , BUILDING_TY
     , BUILDING_NM
     , BUSINESS_TY
     , BUSINESS_NM
     , INSERT_DT
     , DUMMY
     , DIST_CLASS
     , QUOTE_QT
     , ANGDT
     , BNDDT
     , [COMMENT]
     , STOCK_NY
)
VALUES (
       '10178299'
     , N'벽산건재'
     , '9900003009'
     , N''   -- 또는 NULL 허용이면 NULL 로 입력
     , N'경상남도 진주시 하대로 59'
     , NULL
     , '01090559269'
     , NULL
     , '52733'
     , NULL
     , NULL
     , NULL
     , NULL
     , '20250814090900'
     , NULL
     , NULL
     , '20504602'
     , '20250113'
     , '20260131'
     , NULL
     , NULL
);
```

## 실행 팁

* 스키마/컬럼이 실제와 다르면 **컬럼 목록만 맞춰서** 사용해.
* 동일 `SHIPTO_CD`가 이미 있으면 충돌나니, 위처럼 **테스트 전용 코드**(9900… 계열)를 유지해.
* S8처럼 공백이 들어간 케이스는 **판별 함수에서 trim**이 적용되는지 확인에 좋다.
* S4(`KR산업`)은 \*\*예외목록(ZOBJ 화이트리스트)\*\*이 세팅되어 있다는 전제에서 테스트해.

---------------------------------------------------------------------


# 아래 쿼리를 위의 검증 방법을 적용해서 작성해줘
```sql
CREATE TABLE EORDER.dbo.O_SHIPTO (
	SHIPTO_CD varchar(10) COLLATE Korean_Wansung_CI_AS NOT NULL,
	SHIPTO_NM varchar(80) COLLATE Korean_Wansung_CI_AS NULL,
	ADD1 varchar(180) COLLATE Korean_Wansung_CI_AS NULL,
	ADD2 varchar(80) COLLATE Korean_Wansung_CI_AS NULL,
	ADD3 varchar(80) COLLATE Korean_Wansung_CI_AS NULL,
	ADD4 varchar(80) COLLATE Korean_Wansung_CI_AS NULL,
	ZIP_CD varchar(12) COLLATE Korean_Wansung_CI_AS NULL,
	BUILDING_TY varchar(3) COLLATE Korean_Wansung_CI_AS NULL,
	BUILDING_NM varchar(30) COLLATE Korean_Wansung_CI_AS NULL,
	BUSINESS_TY varchar(3) COLLATE Korean_Wansung_CI_AS NULL,
	BUSINESS_NM varchar(50) COLLATE Korean_Wansung_CI_AS NULL,
	INSERT_DT varchar(20) COLLATE Korean_Wansung_CI_AS NULL,
	DUMMY varchar(30) COLLATE Korean_Wansung_CI_AS NULL,
	DIST_CLASS varchar(5) COLLATE Korean_Wansung_CI_AS NULL,
	QUOTE_QT char(10) COLLATE Korean_Wansung_CI_AS NULL,
	ANGDT varchar(8) COLLATE Korean_Wansung_CI_AS DEFAULT NULL NULL,
	BNDDT varchar(8) COLLATE Korean_Wansung_CI_AS DEFAULT NULL NULL,
	CUST_CD varchar(10) COLLATE Korean_Wansung_CI_AS NULL,
	Comment varchar(80) COLLATE Korean_Wansung_CI_AS NULL,
	STOCK_NY varchar(20) COLLATE Korean_Wansung_CI_AS NULL,
	CONSTRAINT XPKO_SHIPTO PRIMARY KEY (SHIPTO_CD)
);
```
```sql
CREATE TABLE EORDER.dbo.O_QUOTATION_VERIFICATION (
	QUOTE_QT char(10) COLLATE Korean_Wansung_CI_AS NOT NULL,
	ITEM_CD varchar(25) COLLATE Korean_Wansung_CI_AS NOT NULL,
	INSERTID varchar(10) COLLATE Korean_Wansung_CI_AS NULL,
	INDATE datetime2(0) NULL,
	UPDATEID varchar(10) COLLATE Korean_Wansung_CI_AS NULL,
	MODATE datetime2(0) NULL,
	CONSTRAINT XPKO_O_QUOTATION_VERIFICATION PRIMARY KEY (QUOTE_QT,ITEM_CD)
);
```
---------------------------------------------------------------------
```sql
SELECT TRIM(A.QUOTE_QT) AS QUOTE_QT
     , TRIM(A.ITEM_CD) AS ITEM_CD
  FROM O_QUOTATION_VERIFICATION A
 WHERE A.QUOTE_QT = '204192721'
   AND A.ITEM_CD IN ( '792119', '786601', '792123' )
;
```
---------------------------------------------------------------------


좋아. **납품처명(Shipto\_NM) → 출처(ZCPQ/ZOBJ) 판별 → ZCPQ일 때만 품목검증**을 **단일 SQL**로 녹여서 작성했어.
아래 쿼리는 **`QUOTE_QT` 하나만 입력**하면, `O_SHIPTO`에서 `SHIPTO_NM`을 찾아 출처를 판별하고, 요청한 품목 집합에 대해 **OK/MISSING/SKIP** 상태를 돌려줘.

* **ZCPQ**(`SHIPTO_NM`이 `KR_`로 시작) → `O_QUOTATION_VERIFICATION`에서 존재 여부 확인
* **ZOBJ**(그 외 + 예외명단) → 검증 생략(**SKIP**)
* **예외(화이트리스트)**: `KR산업` 1건 기본 포함(운영 시 테이블로 교체 권장)

---

## A. 상세 결과(요청 품목별 상태)

```sql
/* =========================================================
   Quotation 검증(Shipto_NM 판별 연동) - 상세 결과
   - 입력: @QUOTE_QT, ITEM 집합(VALUES)
   - Shipto_NM 판별: O_SHIPTO에서 TOP(1) (최근 INSERT_DT)
   - 규칙: KR_ 시작 → ZCPQ, 그 외 → ZOBJ, 예외(KR산업) → ZOBJ
   - 결과: 각 ITEM_CD에 대해 OK/MISSING/SKIP
   ========================================================= */
DECLARE @QUOTE_QT NVARCHAR(50) = N'204192721';

WITH REQ_ITEMS AS (  -- 검증 대상 품목 목록
    SELECT '792119' AS ITEM_CD
    UNION ALL SELECT '786601'
    UNION ALL SELECT '792123'
),
SHIPTO AS (          -- QUOTE_QT로 납품처명 추출 (최신 1건)
    SELECT TOP (1)
           TRIM(S.SHIPTO_NM) AS SHIPTO_NM
    FROM   dbo.O_SHIPTO AS S
    WHERE  TRIM(S.QUOTE_QT) = TRIM(@QUOTE_QT)
    ORDER  BY S.INSERT_DT DESC
),
EXC AS (             -- 예외(화이트리스트): 운영 시 테이블로 교체 권장
    SELECT N'KR산업' AS SHIPTO_NM
),
SRC AS (             -- 출처 판별
    SELECT
        CASE
            WHEN EXISTS (SELECT 1 FROM EXC E JOIN SHIPTO T ON E.SHIPTO_NM = T.SHIPTO_NM)
                 THEN N'ZOBJ'
            WHEN EXISTS (SELECT 1 FROM SHIPTO T WHERE UPPER(T.SHIPTO_NM) LIKE 'KR[_]%')
                 THEN N'ZCPQ'     -- KR_ 로 시작하면 ZCPQ
            WHEN EXISTS (SELECT 1 FROM SHIPTO T WHERE UPPER(T.SHIPTO_NM) LIKE 'KR%')
                 THEN N'ZOBJ'     -- KR로 시작(언더스코어 없음) → ZOBJ
            ELSE N'ZOBJ'
        END AS QUOTATION_SRC
)
SELECT
       TRIM(@QUOTE_QT)                    AS QUOTE_QT
     , R.ITEM_CD
     , S.QUOTATION_SRC
     , CASE
           WHEN S.QUOTATION_SRC = N'ZCPQ' AND V.ITEM_CD IS NOT NULL THEN N'OK'       -- ZCPQ + 존재
           WHEN S.QUOTATION_SRC = N'ZCPQ' AND V.ITEM_CD IS NULL     THEN N'MISSING'  -- ZCPQ + 미존재
           WHEN S.QUOTATION_SRC = N'ZOBJ'                           THEN N'SKIP'     -- ZOBJ는 검증 생략
       END AS VERIFY_STATUS
FROM   REQ_ITEMS AS R
CROSS  JOIN SRC AS S
LEFT   JOIN dbo.O_QUOTATION_VERIFICATION AS V
       ON TRIM(V.QUOTE_QT) = TRIM(@QUOTE_QT)
      AND TRIM(V.ITEM_CD)  = TRIM(R.ITEM_CD)
ORDER  BY R.ITEM_CD;
```

---

## B. 요약 결과(API 응답 형태: RES\_CODE + 누락목록)

> 프론트가 원하는 **`0000/0360` + `missingItem`** 형태가 필요하면 아래를 같이 쓰면 돼.

```sql
/* =========================================================
   Quotation 검증(Shipto_NM 판별 연동) - 요약 결과
   - RES_CODE: ZOBJ 또는(ZCPQ & 미누락) → 0000, ZCPQ & 누락 존재 → 0360
   - missingItem: 누락 품목 콤마결합
   ========================================================= */
DECLARE @QUOTE_QT NVARCHAR(50) = N'204192721';

WITH REQ_ITEMS AS (
    SELECT '792119' AS ITEM_CD
    UNION ALL SELECT '786601'
    UNION ALL SELECT '792123'
),
SHIPTO AS (
    SELECT TOP (1)
           TRIM(S.SHIPTO_NM) AS SHIPTO_NM
    FROM   dbo.O_SHIPTO AS S
    WHERE  TRIM(S.QUOTE_QT) = TRIM(@QUOTE_QT)
    ORDER  BY S.INSERT_DT DESC
),
EXC AS (
    SELECT N'KR산업' AS SHIPTO_NM
),
SRC AS (
    SELECT
        CASE
            WHEN EXISTS (SELECT 1 FROM EXC E JOIN SHIPTO T ON E.SHIPTO_NM = T.SHIPTO_NM)
                 THEN N'ZOBJ'
            WHEN EXISTS (SELECT 1 FROM SHIPTO T WHERE UPPER(T.SHIPTO_NM) LIKE 'KR[_]%')
                 THEN N'ZCPQ'
            WHEN EXISTS (SELECT 1 FROM SHIPTO T WHERE UPPER(T.SHIPTO_NM) LIKE 'KR%')
                 THEN N'ZOBJ'
            ELSE N'ZOBJ'
        END AS QUOTATION_SRC
)
, CHECKED AS (
    SELECT
           R.ITEM_CD
         , S.QUOTATION_SRC
         , CASE
               WHEN S.QUOTATION_SRC = N'ZCPQ' AND V.ITEM_CD IS NULL THEN 1
               ELSE 0
           END AS IS_MISSING
    FROM   REQ_ITEMS AS R
    CROSS  JOIN SRC AS S
    LEFT   JOIN dbo.O_QUOTATION_VERIFICATION AS V
           ON TRIM(V.QUOTE_QT) = TRIM(@QUOTE_QT)
          AND TRIM(V.ITEM_CD)  = TRIM(R.ITEM_CD)
)
SELECT
       CASE
           WHEN MIN(QUOTATION_SRC) = N'ZOBJ'                   THEN N'0000'
           WHEN SUM(IS_MISSING) = 0                            THEN N'0000'
           ELSE N'0360'
       END                                                     AS RES_CODE
     , STRING_AGG(CASE WHEN IS_MISSING = 1 THEN ITEM_CD END, N',')
                                                            AS missingItem
FROM   CHECKED;
```

---

### 메모

* `KR_` 매칭은 `LIKE 'KR[_]%'`로 **언더스코어를 리터럴**로 잡았어.
* `O_SHIPTO`에 `QUOTE_QT` 여러 건이 있을 수 있으면 **최신(INSERT\_DT DESC) 1건** 기준으로 판별했어. 필요하면 조건/정렬 바꿔.
* 운영에서는 **예외목록**을 따로 테이블로 관리하는 걸 권장(예: `dbo.O_Quotation_Exception(SHIPTO_NM)`), 위 `EXC` CTE만 테이블참조로 바꾸면 됨.
* 네이티브 `TRIM()`을 썼는데, 구버전이면 `LTRIM(RTRIM())`로 치환 가능.


