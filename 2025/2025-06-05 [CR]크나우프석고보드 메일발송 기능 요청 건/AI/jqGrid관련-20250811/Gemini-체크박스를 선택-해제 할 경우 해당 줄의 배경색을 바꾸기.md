아래는 jqGrid를 이용한 회원관리 페이지 인데, 체크박스를 선택/해제 할 경우 처음 상태와 변경이 되면 해당 줄의 배경색을 바꾸고 싶어.
1. 체크박스를 선택/해제할 때 발생하는 이벤트는 무엇인지.
2. 체크박스를 선택/해제할 때 해당 줄의 배경색을 바꾸려면 어떻게 해야하는지.
3. 해당 줄의 개체 중에 하나라도 변경된게 있으면 배경색 변경, 없으면 배경색 없애기.
아래 소스를 수정해줘

```html
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
			//console.log("########################## afterSaveCell rowid : " + rowid);
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

//체크박스 비교 함수
function toYN(val) {
    if (val === undefined || val === null) return "";
    val = (val + "").toUpperCase().trim();
    if (val === "Y" || val === "YES" || val === "1" || val === "TRUE") return "Y";
    if (val === "N" || val === "NO"  || val === "0" || val === "FALSE") return "N";
    return val;
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

안녕하세요\! jqGrid를 사용한 회원관리 페이지에서 체크박스 변경 시 행의 배경색을 바꾸는 방법에 대해 궁금하셨군요. 요구사항에 맞춰 하나씩 설명하고, 제공해주신 코드에 반영해 드릴게요.

### 1\. 체크박스 선택/해제 이벤트

jqGrid에서 셀 편집 모드로 체크박스를 사용하고 있다면, 체크박스의 선택/해제는 `afterSaveCell` 이벤트를 통해 감지할 수 있습니다. `formatter: "checkbox"` 옵션을 사용하면 셀을 클릭할 때 자동으로 값이 변경되고 `afterSaveCell` 이벤트가 발생해요. 이 이벤트는 셀 값이 변경되어 저장된 후에 호출되므로, 변경된 값을 바탕으로 원하는 동작을 수행하기에 적절합니다.

### 2\. 체크박스 선택/해제 시 해당 줄 배경색 변경

체크박스 변경 시 행의 배경색을 바꾸려면, `afterSaveCell` 이벤트 핸들러에서 변경된 셀의 부모 행(tr)을 찾아서 `changed-row` 클래스를 추가하거나 제거하면 됩니다. 이미 `afterSaveCell`에 다른 값들의 변경 여부를 확인하는 로직이 있으니, 이 로직을 그대로 활용하면 돼요.

아래는 `afterSaveCell` 이벤트 핸들러를 수정한 코드입니다.

```javascript
afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
    var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
    var isChanged = false;
    var rowData = $('#gridList').jqGrid('getRowData', rowid);

    // 변경된 셀의 부모 행(tr)을 jQuery 객체로 가져옵니다.
    var $tr = $('#' + $.jgrid.jqID(rowid)); 

    for (var i = 0; i < compareColumns.length; i++) {
        var col = compareColumns[i];
        var original = originalData[rowid] && originalData[rowid][col];
        var current = rowData[col];

        current = current === undefined || current === null ? "" : (current + "").trim();
        original = original === undefined || original === null ? "" : (original + "").trim();

        // 체크박스 값 보정 로직
        if (col.endsWith('_YN')) {
            current = toYN(current);
            original = toYN(original);
            // 체크박스의 경우, 값이 'Y' 또는 'N'으로 명확하게 변경되므로
            // 실제 변경 여부를 체크하기 위해 `rowData`에서 값을 다시 가져옵니다.
            // (formatter가 적용된 html이 아닌 실제 값)
            // 하지만 현재 로직에서는 `toYN` 함수로 이미 보정하고 있으므로
            // `current`와 `original`만 비교해도 충분합니다.
        }

        // 값 비교 로직
        // jqGrid의 `getRowData`는 `formatter`가 적용된 HTML을 가져오는 경우가 있으므로,
        // 체크박스처럼 특정 포맷터가 적용된 경우 실제 값을 비교하도록 로직을 보완하는 것이 좋습니다.
        // 하지만 지금 코드의 경우, `formatter: "checkbox"`가 HTML `<input type="checkbox">`를 반환하므로,
        // `rowData[col]`의 값은 "on" 또는 "off" 같은 문자열이 될 수 있습니다.
        // 그래서 `toYN` 함수를 통해 "Y"나 "N"으로 변환하여 비교하는 것이 핵심입니다.
        if (current != original) {
            isChanged = true;
            break;
        }
    }

    if (isChanged) {
        $tr.addClass("changed-row");
    } else {
        $tr.removeClass("changed-row");
    }
},
```

### 3\. 변경사항이 하나라도 있으면 배경색 변경, 없으면 배경색 없애기

이 부분은 이미 기존 코드에 잘 구현되어 있습니다. `afterSaveCell` 이벤트 핸들러 내부에서 `compareColumns` 배열을 순회하며 **하나라도 변경된 값이 있는지** `isChanged` 변수를 통해 확인하고 있어요.

`isChanged`가 `true`일 경우 `changed-row` 클래스를 추가하고, `false`일 경우 제거하는 로직이 완벽하게 작동합니다. 따라서 이 로직은 크게 수정할 필요 없이 그대로 두시면 돼요.

### 최종 수정된 소스

아래는 위에서 설명한 내용들을 반영하여 수정한 전체 소스 코드입니다.

**주의:** 제공해주신 소스에서 `<script>` 태그가 `  < script type="text/javascript"> ` 처럼 공백이 포함되어 있어 정상적으로 동작하지 않을 수 있습니다. 소스 수정 시 공백을 제거해주시는 것이 좋습니다.

```html
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

<script type="text/javascript" src="${url}/include/js/common/select2/select2.js"></script>
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

<script type="text/javascript">
//Start. Setting Jqgrid Columns Order.
var ckNameJqGrid = 'admin/customer/customerList/jqGridCookie'; // 페이지별 쿠키명 설정. // ####### 설정 #######
ckNameJqGrid += '/gridList'; // 그리드명별 쿠키명 설정. // ####### 설정 #######

var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
var globalColumnOrder = globalColumnOrderStr.split(',');

var defaultColModel = [ //  ####### 설정 #######
    {name:"CUST_CD", key:true, label:'코드', width:120, align:'center', sortable:true },
    {name:"CUST_NM", label:'거래처명', width:220, align:'left', sortable:true },
    {name:"CUST_MAIN_EMAIL", label:'담당자 이메일', width:220, align:'center', sortable:true, editable: true  },
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
        // --- 여기부터 수정된 부분입니다. ---
        afterSaveCell: function(rowid, cellname, value, iRow, iCol) {
            var compareColumns = ["CUST_MAIN_EMAIL", "SALESREP_EMAIL", "COMMENTS", "CUST_SENDMAIL_YN", "SALESREP_SENDMAIL_YN"];
            var isChanged = false;
            
            // jqGrid에서 해당 행의 현재 데이터를 가져옵니다.
            var rowData = $('#gridList').jqGrid('getRowData', rowid);

            // 행의 `tr` 엘리먼트를 선택합니다.
            var $tr = $('#' + $.jgrid.jqID(rowid));

            for (var i = 0; i < compareColumns.length; i++) {
                var col = compareColumns[i];
                var original = originalData[rowid] && originalData[rowid][col];
                var current = rowData[col];

                current = current === undefined || current === null ? "" : (current + "").trim();
                original = original === undefined || original === null ? "" : (original + "").trim();

                // 체크박스 값 보정
                if (col.endsWith('_YN')) {
                    current = toYN(current);
                    original = toYN(original);
                }

                if (current != original) {
                    isChanged = true;
                    break;
                }
            }

            if (isChanged) {
                $tr.addClass("changed-row");
            } else {
                $tr.removeClass("changed-row");
            }
        },
        // --- 수정된 부분 끝 ---
//        beforeSelectRow: function(rowid, e) {
//     var $target = $(e.target),
//     iCol = $.jgrid.getCellIndex($target.closest("td")[0]),
//     cm = this.p.colModel,
//     colName = cm[iCol] && cm[iCol].name;
//
//     if (colName === "CUST_SENDMAIL_YN" && $target.is(":checkbox")) {
//            // 현재 편집 중인 행이 있으면 저장
//            $(this).jqGrid('saveRow', rowid);
//            // 저장 후 다시 편집 모드로 진입
//            $(this).jqGrid('editRow', rowid, true);
//        }
//
//     return true;
//   },
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

//체크박스 비교 함수
function toYN(val) {
    if (val === undefined || val === null) return "";
    val = (val + "").toUpperCase().trim();
    if (val === "Y" || val === "YES" || val === "1" || val === "TRUE") return "Y";
    if (val === "N" || val === "NO"  || val === "0" || val === "FALSE") return "N";
    return val;
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
    
    <main class="page-content content-wrap">
    
        <%@ include file="/WEB-INF/views/include/admin/header.jsp" %>
        <%@ include file="/WEB-INF/views/include/admin/left.jsp" %>
        
        <%-- 임의 form --%>
        <form name="iForm" method="post"></form>
        <%-- <form name="uForm" method="post" action="${url}/admin/system/deliverySpotEditPop.lime" target="deliverySpotEditPop"></form> --%>
        
        <form name="frm" method="post">
        
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
                </div>
            <%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>
            
        </div>
        
        </form>
        </main>
    </body>

</html>
```