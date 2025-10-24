<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>
<%@ include file="/WEB-INF/views/include/admin/commonpop.jsp" %>

<style>
 .ui-jqgrid .ui-jqgrid-hbox table, 
 .ui-jqgrid .ui-jqgrid-bdiv table {/* width: 100% !important; */}
</style>

<script>

var page_type = toStr('${param.page_type}'); // orderadd=영업사원 주문등록 개별선택.

var multi_select = toStr('${r_multiselect}');
multi_select = ('true' == multi_select) ? true : false;


//Start. Setting Jqgrid Columns Order.
var ckNameJqGrid = 'admin/base/shiptoListPop/jqGridCookie'; // 페이지별 쿠키명 설정. // ####### 설정 #######
ckNameJqGrid += '/gridList_pop'; // 그리드명별 쿠키명 설정. // ####### 설정 #######

var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
var globalColumnOrder = globalColumnOrderStr.split(',');

var defaultColModel = [ //  ####### 설정 ####### /* 2025-04-02 hsg Japanese Leg Roll Clutch : key:true를 납품처명에서 납품처코드로 이동 */
	{ name:"SHIPTO_CD", label:'납품처코드', key:true, sortable:true, width:100, align:'center'},
	{ name:"SHIPTO_NM", label:'납품처명', sortable:true, width:240, align:'left'},
	{ name:"V_ADD1", label:'주소1', sortable:false, width:350, align:'left', formatter:setAddr1 },
	{ name:"V_ADD2", label:'주소2', sortable:false, width:350, align:'left', formatter:setAddr2 },
	{ name:"QUOTE_QT", label:'Quotation QT', sortable:false, width:200, align:'left' },
	{ name:"BNDDT", label:'만료일', sortable:false, width:200, align:'center', formatter:setBnddt },
	{ name:"ADD1", label:'주소1', hidden:true },
	{ name:"ADD2", label:'주소2', hidden:true },
	{ name:"ADD3", label:'주소3', hidden:true },
	{ name:"ADD4", label:'주소4', hidden:true },
	{ name:"ZIP_CD", label:'우편번호', hidden:true },
	{ name:"CUST_CD", label:'우편번호', hidden:true },
];
if(!multi_select){
	defaultColModel.push({name:"", label:'선택', width:80, align:'center', sortable:false, formatter:setSelectBtnPop});
}

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
	getGridListPop();
});

function getGridListPop(){
	// grid init
	var searchData = getSearchDataPop();
	//$('#gridList_pop').jqGrid('GridUnload'); // 있어야 다시 호출하네...
	
	$("#gridList_pop").jqGrid({
		url: "${url}/admin/base/getShiptoListAjax.lime",
		//editurl: 'clientArray', //사용x
		datatype: "json",
		mtype: 'POST',
		postData: searchData,
		colModel: updateComModel,
		height: '360px',
		autowidth: false,
		multiselect: multi_select,
		rownumbers: true,
		rowNum : 10,
		rowList : ['10','30','50','100'],
		pagination: true,
		pager: "#pager",
		actions : true,
		pginput : true,
		//sortable: true,
		sortable: { // ####### 설정 #######
			update: function(relativeColumnOrder){
				var grid = $('#gridList_pop');
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
			
			var grid = $('#gridList_pop');
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
			root : 'list'
		},
		loadComplete: function(data){
			$('#listTotalCountSpanId').html(addComma(data.listTotalCount));
		},
		gridComplete: function(){ 
			// 조회된 데이터가 없을때
			var grid = $('#gridList_pop');
		    var emptyText = grid.getGridParam('emptyDataText'); //NO Data Text 가져오기.
		    var container = grid.parents('.ui-jqgrid-view'); //Find the Grid's Container
			if(0 == grid.getGridParam('records')){
				//container.find('.ui-jqgrid-hdiv, .ui-jqgrid-bdiv').hide(); //Hide The Column Headers And The Cells Below
		        //container.find('.ui-jqgrid-titlebar').after(''+emptyText+'');
            }	
		},
		emptyDataText: '조회된 데이터가 없습니다.', //NO Data Text
		//onSelectRow: editRow,
	});
}

// 만료일 색상지정(오늘 이전은 붉은 색으로 표시)
function setBnddt(cellVal, options, rowObj){
	const today = new Date();

	if (isNaN(rowObj.BNDDT)==true) {
		if(today > Date.parse(rowObj.BNDDT))
			return '<span style="color:red;">' + rowObj.BNDDT + '</span>';
		else
			return rowObj.BNDDT;
	}
	return '';
}

//납품처 주소1.
function setAddr1(cellVal, options, rowObj){
	var zipCd = toStr(rowObj.ZIP_CD) == '' ? '' : '['+ toStr(rowObj.ZIP_CD)+ ']';
	return toStr(zipCd) + ' ' + toStr(rowObj.ADD1);
}

// 납품처 주소2.
function setAddr2(cellVal, options, rowObj){
	return toStr(rowObj.ADD2);
}

// 선택버튼.
function setSelectBtnPop(cellval, options, rowObject){
	return '<button type="button"  class="btn btn-line btn-xs" onclick=\'dataSelect(this, "'+options.rowId+'", "2");\'>선택</button>'
}

// 선택.
function dataSelect(obj, rowId, div) { // rowId : 빈값=다중선택, !빈값=개별선택 / div : 1=선택후계속,2=선택후 닫기.
	$(obj).prop('disabled', true);
	
	var confirmText = '납품처를 선택 하시겠습니까?';


	if('' == rowId){ // 다중.
		var chk = $('#gridList_pop').jqGrid('getGridParam','selarrrow');
		chk += '';
		var chkArr = chk.split(",");
		if (chk == '') {
			alert('선택 후 진행해 주십시오.');
			$(obj).prop('disabled', false);
			return false;
		}

		var jsonArray = new Array();
		
		if('orderadd' == page_type){ // 주문등록.
			confirmText = '해당 납품처를 선택 하시겠습니까?';
			if(confirm(confirmText)){
				// 데이터 세팅.
				for(var i=0,j=chkArr.length; i<j; i++){
					var jsonData = new Object();
					var rowObj = $('#gridList_pop').jqGrid('getRowData', chkArr[i]);
					jsonData.SHIPTO_CD = chkArr[i];
					jsonData.SHIPTO_NM = rowObj.SHIPTO_NM;
					jsonData.QUOTE_QT = rowObj.QUOTE_QT;
					jsonData.BNDDT = rowObj.BNDDT;
					jsonData.CUST_CD = rowObj.CUST_CD;
					jsonData.ADD1 = rowObj.ADD1;
					jsonData.ADD2 = rowObj.ADD2;
					jsonData.ADD3 = rowObj.ADD3;
					jsonData.ADD4 = rowObj.ADD4;
					jsonData.ZIP_CD = rowObj.ZIP_CD;
					jsonArray.push(jsonData);
				}
				opener.setShiptoFromPop(jsonArray);
				$(obj).prop('disabled', false);
			}else{
				$(obj).prop('disabled', false);
				return;
			}
		}
		
		$(obj).prop('disabled', false);
	}
	else{ // 개별
		var rowData = $('#gridList_pop').getRowData(rowId);
		console.log(rowData);

		// ↓↓↓↓↓↓↓↓↓↓↓ 2025-03-06 hsg. Fisherman Suplex : 납품처 선택시 오늘 날짜와 비교하여 만료날짜가 도래 하였을 경우 선택 시, 선택 불가 메시지 출력 ↓↓↓↓↓↓↓↓↓↓↓ 
		if(!checkBnddt(rowData.BNDDT)){
			alert('만료된 납품처 입니다');
			return;
		}
		// ↑↑↑↑↑↑↑↑↑↑↑ 2025-03-06 hsg. Fisherman Suplex ↑↑↑↑↑↑↑↑↑↑↑

		var shipto = {
				SHIPTO_CD : rowData.SHIPTO_CD
				, SHIPTO_NM : rowData.SHIPTO_NM
				, CUST_CD : rowData.CUST_CD
				, QUOTE_QT : rowData.QUOTE_QT
				, BNDDT : rowData.BNDDT
				, ADD1 : rowData.ADD1
				, ADD2 : rowData.ADD2
				, ADD3 : rowData.ADD3
				, ADD4 : rowData.ADD4
				, ZIP_CD : rowData.ZIP_CD
			};
	
		if (page_type == 'orderadd' || page_type == 'factReport') {
			confirmText = '해당 납품처를 선택 하시겠습니까?';
			if(confirm(confirmText)){
				//shipto.salesuserids = '${param.ri_salesuserid}'; // Json Data 추가.
				opener.setShiptoFromPop(shipto);
				$(obj).prop('disabled', false);
			}else{
				$(obj).prop('disabled', false);
				return;
			}
		}
		
		$(obj).prop('disabled', false);
	}
	
	if('2' == div) window.open('about:blank', '_self').close();
}

// ↓↓↓↓↓↓↓↓↓↓↓ 2025-03-06 hsg. Fisherman Suplex : 오늘 이전의 날짜인 경우 False, 아닌 경우 True ↓↓↓↓↓↓↓↓↓↓↓ 
function checkBnddt(bnddt){
	const today = new Date(); // 오늘
	var dnddate = extractDate(bnddt); // 넘겨받은 문자열에서 날짜 구하기
	// 구해진 날짜가 오늘 이전이면 false, 아니면  true
	return (today > Date.parse(dnddate)) ? false : true;
}
function extractDate(dateInString){ 
    // 정규식: YYYY-MM-DD 형식의 날짜 찾기
    const datePattern = /\b\d{4}-\d{2}-\d{2}\b/;

    // 문자열에서 날짜 찾기
    const match = dateInString.match(datePattern);

    // 날짜가 있으면 반환, 없으면 null 반환
    return match ? match[0] : null;
}
// ↑↑↑↑↑↑↑↑↑↑↑ 2025-03-06 hsg. Fisherman Suplex ↑↑↑↑↑↑↑↑↑↑↑

function getSearchDataPop(){
	var r_custcd = toStr('${param.r_custcd}');
	var rl_shiptocd = $('input[name="rl_shiptocd"]').val();
	var rl_shiptonm = $('input[name="rl_shiptonm"]').val();
	var rl_insertdt = new Date().toISOString().slice(0, 10);
	rl_insertdt = rl_insertdt.replaceAll('-', '');
	
	var sData = {
		page_type : page_type
		, r_custcd : r_custcd
		, rl_shiptocd : rl_shiptocd
		, rl_shiptonm : rl_shiptonm
		, rl_insertdt : rl_insertdt
	};
	return sData;
}

function dataSearchPop(){
	var searchData = getSearchDataPop();
	//console.log('searchData : ', searchData);
	$('#gridList_pop').setGridParam({
		postData : searchData
	}).trigger('reloadGrid'); // 리로드후 현재 유지.
}

</script>
</head>

<body class="bg-n">
	
	<!-- Modal -->
	<form name="frmPop" method="post">
	
	<div class="modal fade in show">
		<div class="modal-dialog1">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="window.close();"><img src="${url}/include/images/front/common/total_menu_close_icon.png" width="20" height="20" alt="close" /></button>
					<h4 class="modal-title" id="myModalLabel">
						납품처 선택
						<div class="btnList">
							<a href="javascript:dataSearchPop();" class="btn btn-github" title="검색">검색</a>
						</div>
					</h4>
				</div>
				<div class="modal-body">
					<div class="tableSearch">
						<div class="topSearch">
							<ul>
								<li>
									<label class="search-h">납품처코드</label>
									<div class="search-c">
										<input type="text" class="search-input" name="rl_shiptocd" value="${param.rl_shiptocd}" onkeypress="if(event.keyCode == 13){dataSearchPop();}" />
									</div>
								</li>
								<li>
									<label class="search-h">납품처명</label>
									<div class="search-c">
										<input type="text" class="search-input" name="rl_shiptonm" value="${param.rl_shiptonm}" onkeypress="if(event.keyCode == 13){dataSearchPop();}" />
									</div>
								</li>
							</ul>
						</div>
					</div>
					
					<h5 class="table-title pull-left no-m-t">Total <span id="listTotalCountSpanId">0</span> EA</h5>
					<div class="btnList">
						<c:if test="${r_multiselect}">
							<button type="button" class="btn btn-primary" onclick="dataSelect(this, '', '1');">선택후 계속</button>
							<button type="button" class="btn btn-github" onclick="dataSelect(this, '', '2');">선택후 닫기</button>
							
						</c:if>
					</div>
					<div class="table-responsive in">
						<table id="gridList_pop" class="display table tableList nowrap" width="100%" border="0" cellpadding="0" cellspacing="0"></table> 
						<div id="pager"></div>
					</div>
				</div>
				
			</div>
		</div>
	</div>
	
	</form>
</body>
</html>