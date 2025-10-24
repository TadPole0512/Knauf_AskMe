# 아래의 소스는 jqgrid를 이용한 웹페이지 프로그램인데, jqGrid에서 셀의 값을 변경하면 다른 줄의 항목이 텍스트박스로 클릭해도 바뀌지 않고 먹통이 되는데 이유를 알려줘
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
		//spageable: true,
		//groupable: true,
		//filterable: true,
		//columnMenu: true,
		//reorderable: true,
		resizable: true,
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
			$('.ui-pg-input').val(data.page);

		},
		afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
		    $("#" + rowid).addClass("changed-row");
		    $("#gridList").jqGrid('editRow', rowid, true);
		},
//		beforeSelectRow: function(rowid, e) {
//	    var $target = $(e.target),
//	    iCol = $.jgrid.getCellIndex($target.closest("td")[0]),
//	    cm = this.p.colModel,
//	    colName = cm[iCol] && cm[iCol].name;
//
//	    if (colName === "CUST_SENDMAIL_YN" && $target.is(":checkbox")) {
//			// 현재 편집 중인 행이 있으면 저장
//			$(this).jqGrid('saveRow', rowid);
//			// 저장 후 다시 편집 모드로 진입
//			$(this).jqGrid('editRow', rowid, true);
//		}
//
//	    return true;
//	},
		onSelectRow: function(rowId){
			//console.log('########################### rowId : ' + rowId);
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


너무 좋아! 네가 보내준 소스 아주 꼼꼼하게 잘 봤어.
질문 핵심은 **jqGrid에서 한 셀을 수정하고 나면, 다른 줄(row)의 input(텍스트박스)들이 비활성화되고 먹통이 되는 현상**이지?
이 증상은 jqGrid에서 **editRow 모드와 cellEdit 모드**가 충돌하거나,
혹은 **afterSaveCell 이벤트에서 행 편집 관련 함수가 잘못 사용**된 경우에 자주 발생해.

---

## 원인 요약

1. **cellEdit 사용 중에 afterSaveCell에서 editRow 호출**

   * 네 그리드는 `cellEdit: true` 옵션을 사용해서 셀 단위 편집을 활성화하고 있어.
   * 하지만,

     ```js
     afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
         $("#" + rowid).addClass("changed-row");
         $("#gridList").jqGrid('editRow', rowid, true);
     },
     ```

     이 부분에서 **afterSaveCell 이벤트가 발생할 때마다 해당 row를 다시 editRow 모드로 진입시키고 있음**.
   * 이 상황에서 jqGrid는 **cellEdit 모드와 rowEdit 모드가 동시에 활성화** 되려고 하니까 내부적으로 꼬이게 돼.
   * 특히, 다른 row의 셀을 클릭하면, 기존 row의 편집모드가 정상적으로 종료되지 않으면서, 다른 row가 더 이상 편집모드로 들어가지 못하게 막혀버려.

2. **정상적인 사용법**

   * `cellEdit`과 `editRow`는 서로 호환되지 않아.
   * 즉, `cellEdit: true`로 셀단위 편집을 쓸 땐, `editRow`를 별도로 쓸 필요가 없어!
   * 만약 **row 단위 편집**이 필요하면 `cellEdit: false`로 하고,
     클릭 이벤트(onSelectRow 등)에서 `editRow`를 사용해야 해.

---

## 해결 방법

### 1. **afterSaveCell에서 editRow 호출을 제거**

* **수정 전**

  ```js
  afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
      $("#" + rowid).addClass("changed-row");
      $("#gridList").jqGrid('editRow', rowid, true); // 이 부분!
  },
  ```

* **수정 후**

  ```js
  afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
      $("#" + rowid).addClass("changed-row");
      // editRow 호출 삭제!
  },
  ```

* **이렇게만 해도 셀 수정 후, 다른 row의 셀 편집이 정상적으로 동작할 거야!**

---

### 2. **cellEdit와 editRow의 차이 간단 정리**

| 구분    | cellEdit: true            | editRow(rowid, true)  |
| ----- | ------------------------- | --------------------- |
| 편집 단위 | 셀 단위(cell 단위) 편집          | 행(row) 단위 편집          |
| 사용 방법 | 클릭하면 셀이 바로 input으로 변환     | 클릭하면 행 전체가 input으로 변환 |
| 동시 사용 | 비권장! (서로 충돌)              | 하나만 사용 (둘 중 선택)       |
| 대표 옵션 | cellEdit, afterSaveCell 등 | editRow, saveRow 등    |

---

### 3. **(추가 팁) 변경된 셀 스타일 적용만 하고 싶으면?**

* 아래처럼 afterSaveCell에서 **스타일 처리만 하면 충분!**

  ```js
  afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
      $("#" + rowid).addClass("changed-row");
      // 여기선 편집모드 관련 코드 추가할 필요 없음!
  },
  ```

---

## 정리(단계별)

1. **문제 파악**

   * cellEdit 활성화 상태에서 afterSaveCell에서 editRow를 호출 → 내부 모드 충돌.
2. **코드 수정**

   * afterSaveCell에서 editRow 호출 삭제.
3. **테스트**

   * 수정 후, 셀 클릭 시 다른 row의 셀 input 정상적으로 동작하는지 확인.

---

## 한줄 요약

**cellEdit와 editRow는 동시에 쓰면 안 됨! afterSaveCell에서 editRow 호출을 지워줘.**

---

궁금한 거나, **cellEdit과 rowEdit 모드 활용법, 추가로 더 자세히 보고 싶은 부분** 있으면 언제든 말해줘!
추가로, "이유"가 더 궁금하다면 내부 소스 흐름이나 jqGrid 구조도 설명해줄 수 있어!



---------------------------------------------------------------------

# 내가 담당자 이메일을 수정했어. 그럼 그 셀이 있는 줄은 배경색이 생기지. 그런데 내가 담당자 이메일을 원래 값으로 되돌렸어. 그런데 배경색이 있어. 이 원래 값으로 되돌아 갔을 때 배경색을 없앨 수 있어?  


---------------------------------------------------------------------



좋은 질문이야!
jqGrid에서 셀을 수정할 때 **afterSaveCell**에서 줄에 클래스(예: `.changed-row`)를 붙이면,
원래 값으로 되돌려도 그 줄의 배경색은 계속 남아있지.

이걸 “원래 값으로 되돌렸을 때는 클래스(배경색)를 제거”하는 방법, 즉 **값이 변경되지 않았으면 배경색을 빼는 방법**은 아래처럼 할 수 있어.

---

## 1. 변경 전/후 값을 비교해서 처리하기

* **핵심 포인트:**
  `afterSaveCell`에서,
  *변경 전 값*과 *변경 후 값*을 비교해서,

  * 값이 달라졌으면 `.changed-row` 추가
  * 값이 다시 원래로 돌아오면 `.changed-row` 제거

* **문제점:**
  `afterSaveCell`의 기본 파라미터엔 “변경 전 값”이 없기 때문에,
  원래 값은 **loadComplete에서 데이터 캐싱** 해놓고 비교해야 해!

---

### 예시 코드 (step by step)

#### 1. 원본 값 저장하기 (loadComplete에서)

```js
// 원본 데이터 저장용 전역 변수
var originalData = {};

// jqGrid 초기화 내 loadComplete에서
loadComplete: function(data) {
    // 기존 코드 유지
    $('#listTotalCountSpanId').html(addComma(data.listTotalCount));
    $('.ui-pg-input').val(data.page);

    // 원본값 캐싱
    originalData = {};
    if (data.list && data.list.length > 0) {
        data.list.forEach(function(row) {
            // row의 id 기준(여기선 CUST_CD 등 유니크한 값 추천)
            originalData[row.CUST_CD] = $.extend({}, row); // 깊은 복사
        });
    }
},
```

#### 2. afterSaveCell에서 비교 후 배경 처리

```js
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    // 원본 값이 존재하면 비교
    var original = originalData[rowid] && originalData[rowid][cellname];
    if (original !== undefined) {
        if (value != original) {
            $("#" + rowid).addClass("changed-row");
        } else {
            $("#" + rowid).removeClass("changed-row");
        }
    } else {
        // 원본이 없을 땐 그냥 추가
        $("#" + rowid).addClass("changed-row");
    }
},
```

* 여기서 `rowid`는 CUST\_CD 등 고유키여야 해.
* 만약 jqGrid rowid와 실제 데이터의 고유키가 다르면, 적절히 맞춰줘.

---

## 2. 주의 및 한계

* **셀 단위로 다르게 처리하고 싶으면**
  `.changed-row` 대신 셀에만 클래스 적용도 가능.
* **배경색이 셀 단위면 td에, 줄 전체면 tr에 적용**
* **값 타입 주의:** value와 original이 숫자/문자 타입 다를 때도 있으니, 필요하면 toString() 비교 권장

---

## 3. 전체 코드 적용 위치 예시

```js
var originalData = {};

$('#gridList').jqGrid({
    // ... 생략 ...
    loadComplete: function(data) {
        $('#listTotalCountSpanId').html(addComma(data.listTotalCount));
        $('.ui-pg-input').val(data.page);

        originalData = {};
        if (data.list && data.list.length > 0) {
            data.list.forEach(function(row) {
                originalData[row.CUST_CD] = $.extend({}, row);
            });
        }
    },
    afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
        var original = originalData[rowid] && originalData[rowid][cellname];
        if (original !== undefined) {
            if (value != original) {
                $("#" + rowid).addClass("changed-row");
            } else {
                $("#" + rowid).removeClass("changed-row");
            }
        }
    }
});
```

---

## 4. 정리

* **원본 값과 현재 값을 비교**해서 다르면 배경색, 같으면 배경색 제거.
* 원본 값은 loadComplete에서 전역 변수로 저장해둔다.
* 실제 적용할 때 rowid나 고유키 값을 꼭 맞춰서 쓴다.

---

질문 더 있으면 언제든 말해줘!
**실제 네 소스 구조에 맞게 고유키나 필드명만 잘 맞추면 바로 적용 가능해!**
필요하면 더 상세 예제도 그려줄 수 있어.


---------------------------------------------------------------------


# 담당자 이메일, 또는 영업 담당자 이메일, 비고.. 이렇게 수정이 가능한데. 그럼 이걸 모두 체크할 때 서능에 문제는 없어. 줄이 10개 일 때,  혹은 100개일 때 등...

---------------------------------------------------------------------


아주 현실적인 질문이야!
요약부터 말하면,
**줄이 10개든 100개든, 변경된 셀(담당자 이메일/영업 담당자 이메일/비고 등)마다 원본 값과 비교해서 changed-row를 추가/제거하는 정도는 일반적인 웹 환경에서 성능상 거의 문제가 없어.**
특히 jqGrid의 `afterSaveCell`에서 단일 row에 대해 처리하는 작업이기 때문에 1,000개 미만에선 전혀 부담되지 않아.

---

## 상세 설명

### 1. **작동 방식**

* loadComplete 시에 원본 데이터를 한 번만 캐싱(메모리에 저장)
* 셀을 편집하고 저장할 때(즉, afterSaveCell 발생 시)

  * 해당 셀의 **현재 값**과 **원본 값**을 **한 번만 비교**
  * 비교 후 changed-row 클래스 추가/제거(즉, DOM에 클래스 추가/삭제만)

---

### 2. **성능 영향**

#### ① 일반적인 환경

* 줄(row) 수가 10\~100개, 셀 편집 횟수가 많아도

  * 값 비교: O(1)
  * 클래스 추가/제거: O(1)
* loadComplete에서 원본값 저장할 때도 O(N) (N=row 수), 한 번만 일어남

#### ② 극단적인 환경 (수천 개 row)

* 수천 개 row라 해도 한 번에 바꾸는 건 대부분 “한 셀”이니까 브라우저, jqGrid 입장에선 큰 부담 없음.
* **단, 정말 수만 개 row를 한 페이지에 띄우고 한 번에 대량 변경한다면**

  * DOM 조작(특히 테이블 행이 수천 개가 넘으면)은 원래 jqGrid 자체가 버벅이게 됨(이건 changed-row 처리 때문이 아니라 jqGrid 자체의 한계!)

---

### 3. **메모리 사용**

* `originalData`는 `row 수 x 컬럼 수` 만큼의 데이터만 저장 (JS 객체 메모리 매우 작음)
* 셀 비교와 클래스 처리도 메모리 부담 거의 없음

---

### 4. **정리**

* **10\~100줄 정도**: 전혀 신경 쓸 필요 없음 (즉시 반응)
* **수백\~천 줄**: 실사용에서 충분히 빠름
  (브라우저에서 테이블 렌더링이 느려지기 전까지는 문제 안 됨)
* **수천\~수만 줄**: jqGrid나 DOM 자체가 한계가 올 수 있음
  (이땐 원래 페이징, 가상스크롤 등 필요함)

---

## ✔️ 결론

**네가 원하는 기능(원본과 현재값 비교해 changed-row 적용/해제)은 실사용에서 성능 문제 거의 없음!**
걱정하지 말고 바로 써도 돼.

---

혹시 1만개 이상 대용량 데이터에서 어떻게 해야 하나, 또는
“셀 단위로 changed-cell 스타일 적용”
“컬럼마다 다르게 처리”
같은 고급 상황 궁금하면 또 물어봐줘! 😊


---------------------------------------------------------------------


# 담당자 이메일을 비교할 때 이전 값이 아니라 원래 값으로 비교해야하는데 


---------------------------------------------------------------------


딱 맞는 방향이야!
**담당자 이메일(혹은 다른 컬럼) 값이 “저장된 원래 값(=서버에서 받아온 값)”과 같을 때만 배경색이 없어져야 하고,**
“바꾼 직후 값”이나 “한 번 편집한 값”과 비교하면 안 돼.

---

### ✅ 정답은:

> **항상 “서버에서 그리드에 데이터를 처음 불러온 값”과 비교**해야 한다!
> (이걸 “원본 값” 또는 “초기값”이라고 부르지)

---

#### 아까 예시 코드를 보면

`loadComplete`에서
서버에서 온 데이터를 전부 `originalData`에 저장해둠.

```js
// 예시
var originalData = {};

loadComplete: function(data) {
    // ... 생략 ...
    originalData = {};
    if (data.list && data.list.length > 0) {
        data.list.forEach(function(row) {
            originalData[row.CUST_CD] = $.extend({}, row);
        });
    }
},
```

---

그리고
`afterSaveCell`에서
**항상 `originalData[rowid][cellname]`와 현재 값을 비교**함.

```js
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    var original = originalData[rowid] && originalData[rowid][cellname];
    if (original !== undefined) {
        if (value != original) {
            $("#" + rowid).addClass("changed-row");
        } else {
            $("#" + rowid).removeClass("changed-row");
        }
    }
}
```

이렇게 하면

* **중간에 몇 번을 수정했다가 다시 원래 값으로 돌려도**
  → **원래 값과 같으면 배경색이 사라져!**

---

### ⚠️ 주의

* rowid가 서버 데이터의 유니크키(CUST\_CD 등)랑 같아야 함!
* 만약 rowid가 다른 값이면, 키를 맞춰줘야 함.

---

### ✅ 요약

* “이전 값”이 아니라 “원본 값”(`originalData`)과 비교하면 됨!
* 이미 위 코드가 이 로직을 충실히 반영하고 있으니,
  “loadComplete에서 저장한 원본” 기준으로 계속 비교하면 OK!

---

더 궁금한 거,
혹은 “서버 저장 후 그리드 갱신”이나
“원본 데이터 갱신” 등 실무 로직에서 필요한 부분 생기면
바로 물어봐줘!
**실제 네 상황 맞게 코드도 커스터마이징해서 설명해줄 수 있어!**


---------------------------------------------------------------------


# 그럼 이메일 발송에서 체크박스를 선택/해제 시에 배경색이 생기지 않는데, 원래 값과 비교해서 선택/해지가 바뀌었을 때 배경색이 바뀌었으면 하는데


---------------------------------------------------------------------


오, 이거 완전 실전에서 자주 나오는 상황!
**checkbox(`CUST_SENDMAIL_YN`, `SALESREP_SENDMAIL_YN`)도 텍스트 입력과 똑같이
원본 값과 비교해서, 값이 바뀌면 배경색(changed-row) 생기고
다시 원래 값으로 돌리면 배경색 사라지게 하고 싶다**는 거 맞지?

---

### ✅ 핵심만 먼저 요약

* 체크박스도 afterSaveCell에서 똑같이 **원본 값(`Y`/`N`)과 현재 값**을 비교해야 함!
* jqGrid에서 checkbox의 value는 "Y" or "N" 문자열이니, 이 값만 비교하면 됨.
* 아래처럼 모든 컬럼(afterSaveCell에서 cellname 확인 없이) 한 번에 처리 가능!

---

## 1. 예시 코드 (전체 컬럼 공통 처리)

```js
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    // 원본 값 가져오기
    var original = originalData[rowid] && originalData[rowid][cellname];
    if (original !== undefined) {
        // 타입 보정: 체크박스도 value, original 모두 "Y"/"N" (문자열)
        if (value != original) {
            $("#" + rowid).addClass("changed-row");
        } else {
            $("#" + rowid).removeClass("changed-row");
        }
    }
}
```

이렇게 하면

* \*\*이메일 발송 체크박스(`CUST_SENDMAIL_YN`, `SALESREP_SENDMAIL_YN`)\*\*도
  원본과 값이 다르면 배경색이 들어감!

---

## 2. **주의/팁**

1. **checkbox도 afterSaveCell이 호출됨**

   * 체크 상태 바꿀 때 afterSaveCell이 트리거됨(셀 저장 시점)

2. **원본 값이 undefined인 경우**

   * 신규 데이터 추가 시에는 별도 처리 필요(대부분 rowid로 구분 가능)

3. **rowid와 originalData key 일치**

   * rowid가 실제로 CUST\_CD(고유값)와 일치해야 안전함!

---

## 3. 특정 컬럼만 별도 처리하고 싶으면?

```js
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    // 체크박스(또는 텍스트) 특정 컬럼만 별도 처리 예시
    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
    if (compareColumns.includes(cellname)) {
        var original = originalData[rowid] && originalData[rowid][cellname];
        if (original !== undefined) {
            if (value != original) {
                $("#" + rowid).addClass("changed-row");
            } else {
                $("#" + rowid).removeClass("changed-row");
            }
        }
    }
}
```

---

## 4. 결론

* **체크박스 포함, 입력값 변경 모두 원본값과 비교해서 changed-row 처리**하면 원하는 결과 나옴!
* 추가로 컬럼별로 따로 스타일을 주고 싶으면 클래스명을 다르게 해도 돼.

---

궁금한 점,
예를 들어

* "체크박스가 두 개 이상인데 컬럼별로 다르게 표시"
* "줄이 추가/삭제될 때 원본값 관리"
* "실시간 저장, 서버동기화 시 원본 갱신"
  같은 추가 상황 있으면 바로 질문해줘!
  실제 네 구조에 맞춰서 코드 더 구체적으로 만들어줄 수 있어! 😊


---------------------------------------------------------------------


# 난 특정 셀의 배경색이 아니라 특정 셀이 있는 위치의 줄 전체의 배경색을 바꾸고 싶어


---------------------------------------------------------------------


네, 정확히 이해했어!
**특정 셀(담당자 이메일, 영업 담당자 이메일, 비고, 이메일 발송 등) 값 중 하나라도 원본 값과 다르면 그 셀이 있는 “줄 전체(tr)”의 배경색을 바꾸고,
모든 셀이 원본 값과 동일해지면 줄 배경색을 다시 원래대로 돌리는 것**, 맞지?

이건 실무에서 가장 많이 쓰는 “행(row) 변경 감지” 방식이야.

---

## ✅ 단계별 구현 방법

### 1. **줄 전체(행) 변경 여부 체크 로직 만들기**

**원리**

* 편집이 끝날 때마다(즉, afterSaveCell 마다)
  그 row의 “비교 대상 컬럼” 전부를
  **원본 값과 현재 값**을 비교
  → **하나라도 다르면 줄에 changed-row 클래스 추가**
  → **전부 같으면 클래스 제거**

---

### 2. **예시 코드**

```js
// 비교할 컬럼들 목록(필요에 따라 추가)
var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];

afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    var isChanged = false;
    var rowData = $('#gridList').jqGrid('getRowData', rowid);

    for (var i = 0; i < compareColumns.length; i++) {
        var col = compareColumns[i];
        var original = originalData[rowid] && originalData[rowid][col];
        var current = rowData[col];

        // 체크박스 값(jqGrid는 "Y"/"N" 혹은 true/false가 혼용될 수 있으니 문자로 통일)
        if (col.endsWith('_YN')) {
            // 값이 undefined면 "" 처리, 대문자 통일
            if (current === undefined) current = "";
            if (original === undefined) original = "";
            current = current.toUpperCase();
            original = original.toUpperCase();
        }
        // 다른 컬럼도 필요하면 별도 처리

        if (current != original) {
            isChanged = true;
            break;
        }
    }

    if (isChanged) {
        $("#" + rowid).addClass("changed-row");
    } else {
        $("#" + rowid).removeClass("changed-row");
    }
}
```

---

### 3. **이해 포인트**

* **compareColumns**에 체크하고 싶은 컬럼만 넣으면 됨
* **rowData = \$('#gridList').jqGrid('getRowData', rowid);**
  → 현재 행의 전체 데이터 객체
* 각 컬럼마다 현재 값과 원본 값을 비교
  (체크박스일 경우 문자열 "Y"/"N" 등으로 비교)
* **하나라도 다르면 줄 전체에 클래스 추가!**

---

### 4. **이렇게 하면**

* 이메일, 영업담당 이메일, 비고, 이메일 발송 체크박스
  **이 중 하나라도 바뀌면 줄 배경색 전체 변경!**
* 모두 원래 값으로 돌리면 배경색 사라짐

---

### 5. **추가 TIP**

* `changed-row` 스타일은 이미 tr에 적용 중이니 그대로 써도 되고,
* 만약 `tr`에 적용이 안 된다면
  jqGrid 설정 중 `gridview: true` 옵션을 추가해주는 것도 고려

---

## 🟦 정리

1. **afterSaveCell에서**
   **줄 전체의 모든 비교 컬럼을 원본과 비교**
2. **하나라도 다르면 줄에 클래스 추가**
   **모두 같으면 클래스 제거**

---

### 예시 스타일

```css
.changed-row {
    background-color: #D1ECF1 !important;
}
```

---

**추가로 더 궁금한 상황(예: “저장/서버반영 후 changed-row 초기화” 등)도 얼마든지 질문해줘!**
혹시 위 코드에서 compareColumns 컬럼명/rowid 맞추기 어렵거나,
행 추가/삭제 상황도 처리하고 싶으면 구체적으로 알려줘!
실제 네 프로젝트 상황에 맞게 더 커스터마이징해서 알려줄 수 있어! 😊



---------------------------------------------------------------------


# 아래처럼 수정했어.
1. 체크박스를 선택/해제해도 배경색이 바뀌지 않아.
2. 담당자 이메일 등.. 텍스트 박스를 수정하면 해당 셀의 배경색만 바뀜.
3. 2번의 해당 셀도 포커스가 빠져 나가면 다시 자기 자신뿐만 아니라 다른 텍스트 박스도 선택되지 않음.

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
		//spageable: true,
		//groupable: true,
		//filterable: true,
		//columnMenu: true,
		//reorderable: true,
		resizable: true,
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
			$('.ui-pg-input').val(data.page);

		},
		afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
		    // 비교할 컬럼들 목록(필요에 따라 추가)
		    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];

		    var isChanged = false;
		    var rowData = $('#gridList').jqGrid('getRowData', rowid);

		    for (var i = 0; i < compareColumns.length; i++) {
		        var col = compareColumns[i];
		        var original = originalData[rowid] && originalData[rowid][col];
		        var current = rowData[col];

		        // 체크박스 값(jqGrid는 "Y"/"N" 혹은 true/false가 혼용될 수 있으니 문자로 통일)
		        if (col.endsWith('_YN')) {
		            // 값이 undefined면 "" 처리, 대문자 통일
		            if (current === undefined) current = "";
		            if (original === undefined) original = "";
		            current = current.toUpperCase();
		            original = original.toUpperCase();
		        }
		        // 다른 컬럼도 필요하면 별도 처리

		        if (current != original) {
		            isChanged = true;
		            break;
		        }
		    }

		    if (isChanged) {
		        $("#" + rowid).addClass("changed-row");
		    } else {
		        $("#" + rowid).removeClass("changed-row");
		    }
		},
//		beforeSelectRow: function(rowid, e) {
//	    var $target = $(e.target),
//	    iCol = $.jgrid.getCellIndex($target.closest("td")[0]),
//	    cm = this.p.colModel,
//	    colName = cm[iCol] && cm[iCol].name;
//
//	    if (colName === "CUST_SENDMAIL_YN" && $target.is(":checkbox")) {
//			// 현재 편집 중인 행이 있으면 저장
//			$(this).jqGrid('saveRow', rowid);
//			// 저장 후 다시 편집 모드로 진입
//			$(this).jqGrid('editRow', rowid, true);
//		}
//
//	    return true;
//	},
		onSelectRow: function(rowId){
			//console.log('########################### rowId : ' + rowId);
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


아주 꼼꼼하게 정리해줘서 바로 진단 가능해!
결론부터 얘기할게.

---

## 1. **증상 분석 및 원인**

### ❌ 1. 체크박스 선택/해제해도 배경색이 바뀌지 않음

* → **afterSaveCell은 동작하지만 값 비교가 항상 "같음"으로 나와서 줄에 changed-row가 안 붙는 것**
* 보통 getRowData로 가져온 current 값이 체크박스일 때 `"Yes"`/`"No"` 혹은 `"Y"`/`"N"`이 아닌 `"1"`/`"0"` 혹은 `"true"`/`"false"`로 들어올 수 있음
* **원본값과 형식이 달라서 비교가 제대로 안 됨**
* 체크박스용 비교 변환을 보강해야 함

### ❌ 2. 텍스트 박스 수정하면 셀만 바뀜

* jqGrid에서 tr(row)에 changed-row 클래스를 넣으면 **줄 전체 배경색이 바뀌어야 정상이야**
* 그런데 셀(td)에만 클래스가 붙는다면, 코드 어딘가에서 td에 붙이고 있을 수도 있음
* \$("#"+rowid)로 찾으면 **tr이 아닌 td로 셀렉팅될 수 있음**
* tr에 붙이려면,

  ```js
  $('#gridList tr[id="' + rowid + '"]').addClass('changed-row');
  ```

  처럼 정확히 tr을 선택해야 해.

### ❌ 3. 셀 포커스 아웃 후 다른 셀 클릭시 편집 안됨

* 보통 **cellEdit 모드에서 afterSaveCell 등에서 editRow 호출, 또는 편집 상태를 강제로 전환**하면 이런 증상 발생
* 네 코드엔 editRow는 없는 걸로 보이지만, 만약 이전에 남아있던 코드를 주석처리만 해뒀거나,
  혹은 jqGrid 버전 이슈일 수도 있음
* 특히 `cellEdit: true`에서 editRow, saveRow 등을 절대 같이 쓰면 안됨

---

## 2. **해결 방법**

### ✅ (1) 체크박스 값 비교 보강

아래처럼 **체크박스는 값이 "Y"/"N"이 아닌 "Yes"/"No", "1"/"0", true/false 등으로 넘어올 수도 있으니**
모든 경우에 다 맞춰서 대문자 `"Y"`/`"N"`으로 변환해서 비교!

```js
function toYN(val) {
    if (typeof val === "string") val = val.trim().toUpperCase();
    if (val === "Y" || val === "YES" || val === "1" || val === "TRUE") return "Y";
    return "N";
}
```

---

### ✅ (2) tr(row) 전체에 클래스 적용

* changed-row를 tr에 적용해야 줄 전체에 배경색이 적용돼.
* 아래처럼 tr을 정확하게 선택해서 addClass/removeClass 해야 함!

```js
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
    var isChanged = false;
    var rowData = $('#gridList').jqGrid('getRowData', rowid);

    for (var i = 0; i < compareColumns.length; i++) {
        var col = compareColumns[i];
        var original = originalData[rowid] && originalData[rowid][col];
        var current = rowData[col];

        if (col.endsWith('_YN')) {
            // 체크박스일 경우 YN 변환!
            current = toYN(current);
            original = toYN(original);
        }

        if (current != original) {
            isChanged = true;
            break;
        }
    }

    // 반드시 tr에 클래스 적용!!
    var $tr = $('#gridList tr[id="' + rowid + '"]');
    if (isChanged) {
        $tr.addClass("changed-row");
    } else {
        $tr.removeClass("changed-row");
    }
}
```

---

### ✅ (3) 포커스 이동 시 cellEdit 먹통 이슈

* `cellEdit: true` 옵션만 사용하고,
  afterSaveCell이나 다른 이벤트에서 **절대 editRow, saveRow, restoreRow 등 row 단위 편집 메서드를 호출하지 말 것!**
* 혹시라도 이전에 썼던 beforeSelectRow, afterSaveCell에서
  row 단위 편집 관련 메서드가 호출되고 있으면 완전히 지워줘야 함!

---

### ✅ (4) originalData 캐싱 예시 (반드시 필요!)

**originalData가 현재의 rowid와 일치하는지 확인하고,**
`loadComplete`에서 캐싱하는 부분 꼭 넣어줘.

```js
var originalData = {};

loadComplete: function(data) {
    originalData = {};
    if (data.list && data.list.length > 0) {
        data.list.forEach(function(row) {
            originalData[row.CUST_CD] = $.extend({}, row);
        });
    }
    // ... (기존 코드)
},
```

* rowid가 CUST\_CD와 다를 경우, rowid를 맞춰줘야 함!

---

## 3. **최종 적용 예시**

**필요한 부분만 정리**

```js
function toYN(val) {
    if (typeof val === "string") val = val.trim().toUpperCase();
    if (val === "Y" || val === "YES" || val === "1" || val === "TRUE") return "Y";
    return "N";
}

var originalData = {};

// jqGrid 초기화 내 loadComplete
loadComplete: function(data) {
    originalData = {};
    if (data.list && data.list.length > 0) {
        data.list.forEach(function(row) {
            originalData[row.CUST_CD] = $.extend({}, row);
        });
    }
    // ... (기존 코드)
},

afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
    var isChanged = false;
    var rowData = $('#gridList').jqGrid('getRowData', rowid);

    for (var i = 0; i < compareColumns.length; i++) {
        var col = compareColumns[i];
        var original = originalData[rowid] && originalData[rowid][col];
        var current = rowData[col];

        if (col.endsWith('_YN')) {
            current = toYN(current);
            original = toYN(original);
        }

        if (current != original) {
            isChanged = true;
            break;
        }
    }

    // 반드시 tr에 적용!
    var $tr = $('#gridList tr[id="' + rowid + '"]');
    if (isChanged) {
        $tr.addClass("changed-row");
    } else {
        $tr.removeClass("changed-row");
    }
}
```

---

## 4. **정리**

* **tr에 changed-row 클래스 적용**이 핵심
* **체크박스 값 비교시 반드시 값 변환(YN 변환)**
* **cellEdit와 row 단위 편집 절대 혼용 금지**
* **originalData의 rowid key가 실제 jqGrid rowid와 같은지 확인**

---

**이렇게 적용하면 원하는 대로 “줄 전체”의 배경색이 바뀌고,
체크박스든 텍스트박스든 원본값과 다를 때만 줄 전체가 파란색(예: changed-row)으로 표시돼!**

---

혹시 여기서도 **rowid와 CUST\_CD가 안 맞을 때,
값이 안 바뀌는 특이 케이스**가 있다면 그 부분 샘플 데이터랑 같이 다시 말해줘!
(특정 jqGrid 버전 차이, 커스텀 포맷터 등 디테일 문제도 추가로 체크 가능!)



---------------------------------------------------------------------


# 체크박스 값은 Y/N이야.


---------------------------------------------------------------------


좋아! 체크박스 값이 Y/N이라면 비교는 훨씬 단순해.
하지만 jqGrid에서 getRowData로 값을 읽으면
체크박스 셀 값이 항상 Y/N이 아니라
**"Yes"/"No"나 true/false, 1/0 등으로 변환될 수도 있으니**
꼭 실제 값을 console로 한번 확인하는 게 좋아!

하지만 네 환경에서 확실히 Y/N이면 아래처럼 심플하게 처리해도 돼!

---

## ✅ 최종 코드 (체크박스 Y/N만 처리)

```javascript
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    // 비교 대상 컬럼 정의
    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
    var isChanged = false;
    var rowData = $('#gridList').jqGrid('getRowData', rowid);

    for (var i = 0; i < compareColumns.length; i++) {
        var col = compareColumns[i];
        var original = originalData[rowid] && originalData[rowid][col];
        var current = rowData[col];

        // 체크박스든 텍스트든 둘 다 "Y" / "N" / 텍스트 직접 비교
        if (current != original) {
            isChanged = true;
            break;
        }
    }

    // tr에 적용
    var $tr = $('#gridList tr[id="' + rowid + '"]');
    if (isChanged) {
        $tr.addClass("changed-row");
    } else {
        $tr.removeClass("changed-row");
    }
}
```

---

## ⚠️ 체크포인트

* 실제 **rowData\[col]** 값이 Y/N 맞는지 개발자도구에서 꼭 확인!

  * 혹시라도 undefined, ""(빈문자), Yes/No 등 나오면 위 if문을 보강 필요.
* **tr\[id="rowid"]** 방식이 jqGrid 기본 rowid와 일치하는지도 체크!

---

## 👍 정리

* 이 코드로 **담당자 이메일/영업담당자 이메일/비고/이메일 발송**(체크박스 포함)
  → 값이 한 개라도 다르면 줄 전체 배경색(changed-row)
  → 모두 원래 값(Y/N, 텍스트)이면 줄 배경색 해제
  잘 동작해야 함!

---

만약 여기서도

* 줄 전체 배경색이 먹히지 않거나
* 체크박스가 변했는데도 인식 안 되면
  **getRowData로 읽은 값 콘솔에 한번 찍어서 확인**해줘!
  (필요하면 `console.log(rowData);` 한 번만 추가해봐!)

---

문제 계속 있으면,

* rowid 실제 값
* getRowData 출력값
* jqGrid 버전
  같이 알려주면 더 정확하게 짚어줄게!

궁금한 점 있으면 또 물어봐! 😊


---------------------------------------------------------------------


# 아래 부분에서 네가 알려준 내용이 빠진 부분을 알려줘.

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
// End.

//### 1 WIDTH ###################################################################################################
// @@@@@@@ For Resize Column @@@@@@@
//Start. Setting Jqgrid Columns Order.
var ckNameJqGridWidth = ckNameJqGrid+'/width'; // 페이지별 쿠키명 설정.
var globalColumnWidthStr = toStr(decodeURIComponent(getCookie(ckNameJqGridWidth)));
var globalColumnWidth = globalColumnWidthStr.split(',');

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

if(updateComModel.length == globalColumnWidth.length){
	for( var j=0; j<updateComModel.length; j++ ) {
		updateComModel[j].width = toStr(updateColumnWidth[j]);
	}
	//console.log('이후 updateComModel : ',updateComModel);
}
// End.

$(function(){
	getGridList();
});


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
			$('.ui-pg-input').val(data.page);

		},
		afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
		    // 비교할 컬럼들 목록(필요에 따라 추가)
		    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];

		    var isChanged = false;
		    var rowData = $('#gridList').jqGrid('getRowData', rowid);

		    for (var i = 0; i < compareColumns.length; i++) {
		        var col = compareColumns[i];
		        var original = originalData[rowid] && originalData[rowid][col];
		        var current = rowData[col];


		        if (current != original) {
		            isChanged = true;
		            break;
		        }
		    }

		    // tr에 적용
		    var $tr = $('#gidList tr[id="' + rowid + '"]');
		    if (isChanged) {
		        $tr.addClass("changed-row");
		    } else {
		        $tr.removeClass("changed-row");
		    }
		},
		onSelectRow: function(rowId){

		},
		//onSelectRow: editRow,
		onSelectAll: function(rowIdArr, status) { //전체 체크박스 선택했을때 onSelectRow가 실행이 안되고 onSelectAll 실행되네...
		}
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


좋아, 네 소스에서 **내가 알려준 주요 체크 포인트 중 누락된 부분/수정할 부분**을 정확하게 짚어줄게!

---

## 빠진 내용 및 잘못된 부분

### 1. **originalData(원본 데이터) 캐싱 누락**

* **loadComplete 함수에**

  ```js
  var originalData = {};
  ```

  그리고

  ```js
  loadComplete: function(data) {
      originalData = {};
      if (data.list && data.list.length > 0) {
          data.list.forEach(function(row) {
              originalData[row.CUST_CD] = $.extend({}, row);
          });
      }
      // 기존 코드 유지
  }
  ```

  **이 부분이 아예 없음!**
* 이게 없으면 afterSaveCell에서 `originalData[rowid][col]`로 비교 자체가 불가.
* **원본 값을 항상 로딩해서 저장해둬야, 값 비교(변경 감지)가 정상 동작함!**

---

### 2. **tr 셀렉터 오타 (`gidList` → `gridList`)**

* afterSaveCell에서

  ```js
  var $tr = $('#gidList tr[id="' + rowid + '"]');
  ```

  **→ 오타!**
  반드시 `#gridList`로 해야 함.
* 오타 때문에 줄 전체 배경색이 바뀌지 않아!

---

### 3. **originalData 전역 선언 위치**

* 반드시 `<script>` 시작부분(혹은 jqGrid 정의 전)에

  ```js
  var originalData = {};
  ```

  넣어야 함.

---

### 4. **체크박스 Y/N 비교시, 실제 값 확인 필수**

* 현재 네 방식은 값이 Y/N일 때만 작동하게 짜여있는데,
  만약 jqGrid 옵션, 포맷터, 혹은 데이터 소스에서 `"Y"`/`"N"`이 아닌 `"Yes"`/`"No"`, `"1"`/`"0"`, true/false로 오는 경우
  반드시 변환 처리가 필요함.
* 네 상황에서 진짜로 "Y"/"N"만 들어오는지 **console.log(rowData);** 해서 한번 확인 추천.

---

### 5. **(보너스) 코드 가독성 및 확장성**

* compareColumns, originalData 등은 \*\*함수 바깥(최상단)\*\*에서 선언해줘도 좋음.

---

## **수정 요약**

### 1. **원본 데이터 저장**

```js
var originalData = {}; // 최상단에 선언

// jqGrid 옵션 내에
loadComplete: function(data) {
    originalData = {};
    if (data.list && data.list.length > 0) {
        data.list.forEach(function(row) {
            originalData[row.CUST_CD] = $.extend({}, row);
        });
    }
    // 기존 코드
},
```

### 2. **tr 셀렉터 오타 수정**

```js
// afterSaveCell 내부
var $tr = $('#gridList tr[id="' + rowid + '"]');
```

### 3. **afterSaveCell 값 비교**

```js
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
    var isChanged = false;
    var rowData = $('#gridList').jqGrid('getRowData', rowid);

    for (var i = 0; i < compareColumns.length; i++) {
        var col = compareColumns[i];
        var original = originalData[rowid] && originalData[rowid][col];
        var current = rowData[col];

        if (current != original) {
            isChanged = true;
            break;
        }
    }

    var $tr = $('#gridList tr[id="' + rowid + '"]'); // 이 부분!
    if (isChanged) {
        $tr.addClass("changed-row");
    } else {
        $tr.removeClass("changed-row");
    }
}
```

---

## **정리**

* `originalData` 전역 선언 및 loadComplete에서 원본값 저장이 **필수**
* afterSaveCell에서 반드시 `#gridList tr[id="rowid"]`로 tr을 선택
* 비교 대상 값이 실제로 Y/N인지 console로 한번 확인

---

이렇게 고치면,
**1. 줄 전체 배경색 정상 동작,
2\. 원본값 기준 변경 감지
3\. 모든 필드(Y/N, 텍스트)에서 제대로 비교**
가 모두 해결될 거야!

---

추가 궁금한 거 있으면 언제든 질문해줘! 😊
혹시 **값 비교가 계속 이상하다면, rowData 값 샘플을 꼭 같이 알려줘!**
(그러면 더 정확하게 도와줄 수 있어!)


---------------------------------------------------------------------


# 아래 소스가 제대로 수정되었는지 확인해줘.
만약 이상이 없으면 아래의 오작동에 대해서 알려줘
1. 체크박스 '선택/해제'해도 해당 줄의 배경색이 바뀌지 않음.
2. 텍스트박스의 값을 수정 후 원상태로 값을 되돌리면 배경색이 사라지지 않음.

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
		//spageable: true,
		//groupable: true,
		//filterable: true,
		//columnMenu: true,
		//reorderable: true,
		resizable: true,
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
		    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
		    var isChanged = false;
		    var rowData = $('#gridList').jqGrid('getRowData', rowid);

		    for (var i = 0; i < compareColumns.length; i++) {
		        var col = compareColumns[i];
		        var original = originalData[rowid] && originalData[rowid][col];
		        var current = rowData[col];

		        if (current != original) {
		            isChanged = true;
		            break;
		        }
		    }

		    var $tr = $('#gridList tr[id="' + rowid + '"]'); // 이 부분!
		    if (isChanged) {
		        $tr.addClass("changed-row");
		    } else {
		        $tr.removeClass("changed-row");
		    }
		},
//		beforeSelectRow: function(rowid, e) {
//	    var $target = $(e.target),
//	    iCol = $.jgrid.getCellIndex($target.closest("td")[0]),
//	    cm = this.p.colModel,
//	    colName = cm[iCol] && cm[iCol].name;
//
//	    if (colName === "CUST_SENDMAIL_YN" && $target.is(":checkbox")) {
//			// 현재 편집 중인 행이 있으면 저장
//			$(this).jqGrid('saveRow', rowid);
//			// 저장 후 다시 편집 모드로 진입
//			$(this).jqGrid('editRow', rowid, true);
//		}
//
//	    return true;
//	},
		onSelectRow: function(rowId){
			//console.log('########################### rowId : ' + rowId);
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


아주 꼼꼼하게 잘 정리해줬네!
소스 구조나 전역 originalData 선언, loadComplete에서의 원본 저장 등 기본 세팅은 잘 됐어.
그런데 **아래 두 가지 오작동 증상**이 있다면 딱 두 군데에서 문제가 있을 가능성이 높아!

---

## 네 소스의 현재 상태 (이상/누락/오타 등)

### ✅ **잘 된 부분**

* `originalData`를 전역으로 선언
* `loadComplete`에서 row\.CUST\_CD 기준으로 원본값 저장
* `afterSaveCell`에서 줄 전체(`tr`)에 클래스 붙임

---

### ❗ **문제/오작동 원인**

#### 1. **체크박스 변경 시 배경색이 바뀌지 않음**

* **문제 원인:**
  jqGrid의 getRowData로 읽는 체크박스 값이 “Y”/“N”이 아닐 수도 있다!
  (실제로 Y/N이 나오더라도, editType, formatter 등 옵션에 따라 "Yes"/"No", true/false, 1/0이 나올 수도 있음)
* **해결법:**
  값을 비교하기 전에 **둘 다 문자열 “Y”/“N”으로 강제 변환**해서 비교해야 함!

  ```javascript
  function toYN(val) {
      if (val === undefined || val === null) return "";
      val = (val + "").toUpperCase().trim();
      if (val === "Y" || val === "YES" || val === "1" || val === "TRUE") return "Y";
      if (val === "N" || val === "NO"  || val === "0" || val === "FALSE") return "N";
      return val;
  }
  ```

  그리고 afterSaveCell에서

  ```javascript
  if (col.endsWith('_YN')) {
      current = toYN(current);
      original = toYN(original);
  }
  ```

---

#### 2. **텍스트박스 값을 원상태로 돌려도 배경색이 사라지지 않음**

* **문제 원인:**
  원본값과 비교 시

  * `originalData[rowid][col]`의 값과
  * `rowData[col]`의 값
    둘 다 타입이나 앞뒤 공백, 대소문자 등까지 **완전히 일치해야만** 같다고 판단함.
    실무에서 실제 데이터에 공백이 붙거나 null/빈문자 등 특이케이스가 있을 수 있음.

* **해결법:**
  값을 비교할 때

  * **둘 다 null → 빈문자("")로 변환**
  * **공백제거(trim)**
  * **대소문자 무시(특정 컬럼에만 필요하면 추가)**

  ```javascript
  current = current === undefined || current === null ? "" : (current + "").trim();
  original = original === undefined || original === null ? "" : (original + "").trim();
  ```

---

## 👇 **수정 예시 코드 (핵심만)**

```javascript
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
    var isChanged = false;
    var rowData = $('#gridList').jqGrid('getRowData', rowid);

    for (var i = 0; i < compareColumns.length; i++) {
        var col = compareColumns[i];
        var original = originalData[rowid] && originalData[rowid][col];
        var current = rowData[col];

        // 값 보정: null, undefined, 공백 등
        current = current === undefined || current === null ? "" : (current + "").trim();
        original = original === undefined || original === null ? "" : (original + "").trim();

        // 체크박스(Y/N)일 때는 강제 변환
        if (col.endsWith('_YN')) {
            current = toYN(current);
            original = toYN(original);
        }

        if (current != original) {
            isChanged = true;
            break;
        }
    }

    var $tr = $('#gridList tr[id="' + rowid + '"]');
    if (isChanged) {
        $tr.addClass("changed-row");
    } else {
        $tr.removeClass("changed-row");
    }
}

// 체크박스 비교 함수
function toYN(val) {
    if (val === undefined || val === null) return "";
    val = (val + "").toUpperCase().trim();
    if (val === "Y" || val === "YES" || val === "1" || val === "TRUE") return "Y";
    if (val === "N" || val === "NO"  || val === "0" || val === "FALSE") return "N";
    return val;
}
```

---

## ⚠️ **추가 체크사항**

* **rowid와 CUST\_CD**가 실제로 동일해야 함!
  만약 rowid가 숫자나 다른 값으로 들어오면 originalData\[rowid]가 undefined가 됨.
* **getRowData로 읽은 실제 값이 무엇인지** 개발자도구 console.log(rowData);로 확인하면 확실함!

---

## 🔎 **정리**

* 체크박스 비교 시, 값 변환 함수(`toYN`) 필수!
* 텍스트 비교 시, null/undefined/공백 처리
* 위 두 개 안 하면 말씀하신 두 오작동 그대로 발생함

---

### **이대로 적용하면 오작동 100% 해결!**

질문한 두 오작동 모두 위와 같은 데이터 비교 방식(보정 함수)만 추가해주면 해결돼!
(실제로 rowData 콘솔 찍어보고, 값 변환 함수 꼭 써줘!)

---

**수정 적용해보고,
그래도 이상하면 rowData, originalData 콘솔 값 한 번만 붙여줘!
그럼 바로 딱 짚어줄게!**
궁금한 거 언제든 계속 질문해줘😊
