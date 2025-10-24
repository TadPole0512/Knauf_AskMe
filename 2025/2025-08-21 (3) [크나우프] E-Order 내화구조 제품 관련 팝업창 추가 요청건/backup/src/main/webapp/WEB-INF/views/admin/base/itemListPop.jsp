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

var page_type = toStr('${param.page_type}'); //recommend=추천상품 다중선택 / orderadd=영업사원 주문등록 다중선택 / orderedit=CS주문처리 개별선택.

var multi_select = toStr('${r_multiselect}');
multi_select = ('true' == multi_select) ? true : false;


//Start. Setting Jqgrid Columns Order.
var ckNameJqGrid = 'admin/base/itemListPop/jqGridCookie'; // 페이지별 쿠키명 설정. // ####### 설정 #######
ckNameJqGrid += '/gridList_pop'; // 그리드명별 쿠키명 설정. // ####### 설정 #######

var globalColumnOrderStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
var globalColumnOrder = globalColumnOrderStr.split(',');

var defaultColModel = [ //  ####### 설정 #######
	{name:"ITI_FILE1", label:'이미지', width:65, align:'center', sortable:true, formatter:setItiImage1},
	{name:"ITEM_CD", key:true, label:'품목코드', width:100, align:'center', sortable:true},
	{name:"DESC1", label:'품목명1', width:190, align:'left', sortable:true},
	{name:"DESC2", label:'품목명2', width:190, align:'left', sortable:true},
	{name:"THICK_NM", label:'두께', width:80, align:'right', sortable:true},
	{name:"WIDTH_NM", label:'폭', width:80, align:'right', sortable:true},
	{name:"LENGTH_NM", label:'길이', width:80, align:'right', sortable:true},
	{name:"UNIT4", label:'구매단위', width:90, align:'center', sortable:true},
	//{name:"SEARCH_TEXT", label:'SEARCH_TEXT', width:20, align:'left', sortable:false},
	
	{name:"ITI_PALLET", label:'파레트적재단위', width:120, align:'right', sortable:true, formatter:setPallet},
	{name:"FIREPROOF_YN", label:'내화구조', width:60, align:'center', sortable:true},
	{name:"FIREPROOF_ITEM_YN", label:'내화구조', width:60, align:'center', sortable:true, hidden:true}, // 2025-08-20 hsg 내화구조 제품 관련 팝업창 추가
	{name:"BOOKMARK_YN", label:'즐겨찾기', width:90, align:'center', sortable:true, formatter:setBookmarkImage},
	{name:"ITB_SORT", hidden:true},
];
if(!multi_select){
	defaultColModel.push({name:"", label:'기능', width:100, align:'center', sortable:false, formatter:setSelectBtnPop});
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
	categorySelect('1');
});

function getGridListPop(){
	// grid init
	var searchData = getSearchDataPop();
	//$('#gridList_pop').jqGrid('GridUnload'); // 있어야 다시 호출하네...
	
	$("#gridList_pop").jqGrid({
		url: "${url}/admin/base/getItemListAjax.lime",
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

/**
 * 즐겨찾기 여부
 */
function setBookmarkImage(cellValue, options, rowdata) {
	var bookmarkYn = cellValue,
		bookmarkImage = '';

	if('Y' == bookmarkYn){
		bookmarkImage += '<button type="button" onclick="setBookmark(this,\''+rowdata.ITEM_CD+'\','+rowdata.ITB_SORT+',\'DEL\');">';
		bookmarkImage += '<img src="${url}/include/images/front/common/icon_star@2x_active.png" width="20" height="20" alt="img" style="border : 0px;" />';
		bookmarkImage += '</button>';
	}else{
		bookmarkImage += '<button type="button" onclick="setBookmark(this,\''+rowdata.ITEM_CD+'\','+rowdata.ITB_SORT+',\'IN\');">';
		bookmarkImage += '<img src="${url}/include/images/front/common/icon_star@2x.png" width="20" height="20" alt="img" style="border : 0px;" />'
		bookmarkImage += '</button>';
	}

	return bookmarkImage;
}

//즐겨찾기 추가/삭제.
function setBookmark(obj, itemCd, itemSt, proc_type){
	$(obj).prop('disabled', true);

	$.ajax({
		async : false,
		data : {
			r_bookmarkprocesstype : proc_type
			, ri_itbitemcd : itemCd
			, ri_itbsort : itemSt
		},
		type : 'POST',
		url : '${url}/admin/base/setItemBookmarkAjax.lime',
		success : function(data) {
			$('#gridList_pop').trigger('reloadGrid');
			$(obj).prop('disabled', false);
			return;
		},
		error : function(request,status,error){
			alert('Error');
			$(obj).prop('disabled', false);
		}
	});
}

function setItiImage1(cellValue, options, rowdata) {
	return '<img src="${url}/data/item/' + cellValue + '" onerror="this.src=\'${url}/include/images/admin/list_noimg.gif\'" width="30" alt="image" />';
}

function setPallet(cellval, options, rowObject){
	return addComma(cellval);
}

function setSelectBtnPop(cellval, options, rowObject){
	return '<button type="button" class="btn btn-default btn-xs" onclick=\'dataSelect(this, "'+options.rowId+'", "2")\' >선택</button>';
}

// 선택.
function dataSelect(obj, rowId, div) { // rowId : 빈값=다중선택, !빈값=개별선택 / div : 1=선택후계속,2=선택후 닫기.
	$(obj).prop('disabled', true);
	
	var confirmText = '품목을 선택 하시겠습니까?';
	
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
		
		if ('recommend' == page_type) { // 관련품목 등록.
			var rowCnt = opener.getItemRow();
			var nowCnt = chkArr.length;
			//alert('rowCnt : '+rowCnt+'\nnowCnt : '+nowCnt);
			if(10 < rowCnt+nowCnt){
				alert('관련품목은 10개까지 등록 가능 합니다.');
				$(obj).prop('disabled', false);
				return;
			}
			
			confirmText = '해당 품목을 관련품목으로 등록 하시겠습니까?';
			if(confirm(confirmText)){
				// 데이터 세팅.
				for(var i=0,j=chkArr.length; i<j; i++){
					var jsonData = new Object();
					var rowObj = $('#gridList_pop').jqGrid('getRowData', chkArr[i]);
					//alert('item_cd : '+chkArr[i]+'\ndesc1 : '+rowObj.DESC1+'\ndesc2 : '+rowObj.DESC2);
					jsonData.ITEM_CD = chkArr[i];
					jsonData.DESC1 = rowObj.DESC1;
					jsonData.DESC2 = rowObj.DESC2;
					jsonArray.push(jsonData);
				}
				opener.setItemFromPop(jsonArray);
				$(obj).prop('disabled', false);
			}else{
				$(obj).prop('disabled', false);
				return;
			}
		}
		else if('orderadd' == page_type){ // 주문등록.
			confirmText = '해당 품목을 선택 하시겠습니까?';
			if(confirm(confirmText)){
				// 데이터 세팅.
				for(var i=0,j=chkArr.length; i<j; i++){
					var jsonData = new Object();
					var rowObj = $('#gridList_pop').jqGrid('getRowData', chkArr[i]);
					//alert('item_cd : '+chkArr[i]+'\ndesc1 : '+rowObj.DESC1+'\ndesc2 : '+rowObj.DESC2);
					jsonData.ITEM_CD = chkArr[i];
					jsonData.DESC1 = rowObj.DESC1;
					jsonData.UNIT = rowObj.UNIT4;
					jsonData.QUANTITY = rowObj.QUANTITY;
					jsonData.ITI_PALLET = toFloat(rowObj.ITI_PALLET.replaceAll(',', ''));
					jsonData.FIREPROOF_YN = rowObj.FIREPROOF_YN;
					jsonData.FIREPROOF_ITEM_YN = rowObj.FIREPROOF_ITEM_YN;
					jsonArray.push(jsonData);
				}
				opener.setItemFromPop(jsonArray);
				$(obj).prop('disabled', false);
			}else{
				$(obj).prop('disabled', false);
				return;
			}
			
		}else if('promotionadd' == page_type){ // 이벤트 품목 등록.
			var chkArrLen = chkArr.length;
			var parentsGridCnt = opener.getGridReccount();

			if(parentsGridCnt > 0){
				parentsGridCnt += chkArrLen;
			}
			//품목 최대 개수 제한
			if(parentsGridCnt >= 10) {
				alert('이벤트 품목은 최대 10개까지만 등록할 수 있습니다.');
				$(obj).prop('disabled', false);
				return;
			}

			confirmText = '해당 품목을 선택 하시겠습니까?';
			if(confirm(confirmText)){

				for(var i=0,j=chkArrLen; i<j; i++){
					var jsonData = new Object();
					var rowObj = $('#gridList_pop').jqGrid('getRowData', chkArr[i]);
					//alert('item_cd : '+chkArr[i]+'\ndesc1 : '+rowObj.DESC1+'\ndesc2 : '+rowObj.DESC2);

					jsonData.ITEM_CD = chkArr[i];
					jsonData.ITI_FILE1 = rowObj.ITI_FILE1;
					jsonData.DESC1 = rowObj.DESC1;
					jsonData.DESC2 = rowObj.DESC2;
					jsonData.UNIT = rowObj.UNIT4;
					jsonData.QUANTITY = rowObj.QUANTITY;
					jsonData.ITI_PALLET = toFloat(rowObj.ITI_PALLET.replaceAll(',', ''));
					jsonArray.push(jsonData);
				}

				opener.setItemFromPop(jsonArray);
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
	
		if('orderedit' == page_type){ // 주문처리 개별.
			confirmText = '해당 품목을 선택 하시겠습니까?';
			if(confirm(confirmText)){
				// 데이터 세팅.
				var setTrId = toStr('${param.r_settrid}');
				if('' != setTrId){
					var itemData = {
						DESC1 : rowData.DESC1
						, UNIT : rowData.UNIT4
						, WEIGHT : rowData.WEIGHT
					};
					
					opener.setItemFromPop(itemData, setTrId);
				}
				
				$(obj).prop('disabled', false);
				
			}else{
				$(obj).prop('disabled', false);
				return;
			}
			
		}
		
		/* 
		var adminUser = {
			csuserid : rowData.USERID
		};
	
		if (page_type == 'recommend') {
			//confirmText = '';
			if(confirm(confirmText)){
				adminUser.salesuserids = '${param.ri_salesuserid}'; // Json Data 추가.
				opener.setFixedSales(adminUser);
				$(obj).prop('disabled', false);
			}else{
				$(obj).prop('disabled', false);
			}
		}
	 	*/	
	}
	
	if('2' == div) window.open('about:blank', '_self').close();
}

function getSearchDataPop(){
	var r_salescd1nm = $('select[name="r_salescd1nm"] option:selected').val();
	var r_salescd2nm = $('select[name="r_salescd2nm"] option:selected').val();
	var r_salescd3nm = $('select[name="r_salescd3nm"] option:selected').val();
	var r_salescd4nm = $('select[name="r_salescd4nm"] option:selected').val();
	var rl_desc1 = $('input[name="rl_desc1"]').val();
	var rl_desc2 = $('input[name="rl_desc2"]').val();
	var rl_searchtext = $('input[name="rl_searchtext"]').val();
	var rl_itemcd = $('input[name="rl_itemcd"]').val();
	var rl_thicknm = $('input[name="rl_thicknm"]').val();
	var rl_widthnm = $('input[name="rl_widthnm"]').val();
	var rl_lengthnm = $('input[name="rl_lengthnm"]').val();
	// var r_checkitembookmark = $('input[name="r_checkitembookmark"]:checked').val(); //즐겨찾기
	var r_checkitembookmark = $('input[name="r_checkitembookmark"]').is(':checked') == true ? 'Y' : 'N' ; //즐겨찾기
	/*
	var ri_existimageynarr = '';
	$('input:checkbox[name="ri_existimageyn"]:checked').each(function(i,e) {
		if(i==0) ri_existimageynarr = $(e).val();
		else ri_existimageynarr += ','+$(e).val();
	});
	
	var ri_existrecommendarr = '';
	$('input:checkbox[name="ri_existrecommend"]:checked').each(function(i,e) {
		if(i==0) ri_existrecommendarr = $(e).val();
		else ri_existrecommendarr += ','+$(e).val();
	});
	*/

	var sData = {
		r_itemdeliverytype : toStr('${param.r_itemdeliverytype}') // Y=배송비 아이템만 출력, 빈값=배송비 아이템 제외하고 출력, ALL=모두출력.
		, r_salescd1nm : r_salescd1nm
		, r_salescd2nm : r_salescd2nm
		, r_salescd3nm : r_salescd3nm
		, r_salescd4nm : r_salescd4nm
		, rl_desc1 : rl_desc1
		, rl_desc2 : rl_desc2
		, rl_searchtext : rl_searchtext
		, rl_itemcd : rl_itemcd
		, rl_thicknm : rl_thicknm
		, rl_widthnm : rl_widthnm
		, rl_lengthnm : rl_lengthnm
		, r_checkitembookmark : r_checkitembookmark
		//, ri_existimageynarr : ri_existimageynarr
		//, ri_existrecommendarr : ri_existrecommendarr
	};
	return sData;
}

function dataSearchPop(){
	var searchData = getSearchDataPop();
	//console.log('searchData : ', searchData);
	$('#gridList_pop').setGridParam({
		postData : searchData
	}).trigger('reloadGrid',{page:1}); // 리로드후 현재 유지.
}

function categorySelect(ct_level) {
	var r_salescd1nm = $('select[name="r_salescd1nm"] option:selected').val();
	var r_salescd2nm = $('select[name="r_salescd2nm"] option:selected').val();
	var r_salescd3nm = $('select[name="r_salescd3nm"] option:selected').val();
	//var r_salescd4nm = $('select[name="r_salescd4nm"] option:selected').val();
	
	var params = 'r_ctlevel='+ct_level+'&';
	if('2' == ct_level) params += 'r_salescd1nm='+r_salescd1nm+'&';
	if('3' == ct_level) params += 'r_salescd1nm='+r_salescd1nm+'&r_salescd2nm='+r_salescd2nm+'&';
	if('4' == ct_level) params += 'r_salescd1nm='+r_salescd1nm+'&r_salescd2nm='+r_salescd2nm+'&r_salescd3nm='+r_salescd3nm+'&';
	
	$.ajax({
		async : false,
		data : params,
		type : 'POST',
		url : '${url}/admin/base/getCategoryListAjax.lime',
		success : function(data) {
			var htmlText = '<option value="">선택하세요.</option>';
			if('1' == ct_level){
				$('select[name="r_salescd4nm"]').empty();
				$('select[name="r_salescd4nm"]').append(htmlText);
				$('select[name="r_salescd3nm"]').empty();
				$('select[name="r_salescd3nm"]').append(htmlText);
				$('select[name="r_salescd2nm"]').empty();
				$('select[name="r_salescd2nm"]').append(htmlText);
				
				$('select[name="r_salescd1nm"]').empty();
				$(data).each(function(i,e){
					htmlText += '<option value="'+e.CATEGORY_NAME+'">'+e.CATEGORY_NAME+'</option>';
				});
				$('select[name="r_salescd1nm"]').append(htmlText);
			}
			if('2' == ct_level){
				$('select[name="r_salescd4nm"]').empty();
				$('select[name="r_salescd4nm"]').append(htmlText);
				$('select[name="r_salescd3nm"]').empty();
				$('select[name="r_salescd3nm"]').append(htmlText);
				
				$('select[name="r_salescd2nm"]').empty();
				$(data).each(function(i,e){
					htmlText += '<option value="'+e.CATEGORY_NAME+'">'+e.CATEGORY_NAME+'</option>';
				});
				$('select[name="r_salescd2nm"]').append(htmlText);
			}
			if('3' == ct_level){
				$('select[name="r_salescd4nm"]').empty();
				$('select[name="r_salescd4nm"]').append(htmlText);
				
				$('select[name="r_salescd3nm"]').empty();
				$(data).each(function(i,e){
					htmlText += '<option value="'+e.CATEGORY_NAME+'">'+e.CATEGORY_NAME+'</option>';
				});
				$('select[name="r_salescd3nm"]').append(htmlText);
			}
			if('4' == ct_level){
				$('select[name="r_salescd4nm"]').empty();
				
				$('select[name="r_salescd4nm"]').empty();
				$(data).each(function(i,e){
					htmlText += '<option value="'+e.CATEGORY_NAME+'">'+e.CATEGORY_NAME+'</option>';
				});
				$('select[name="r_salescd4nm"]').append(htmlText);
			}
		},
		error : function(request,status,error){
			alert('Error');
		}
	});
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
						품목 선택
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
									<label class="search-h">품목분류</label>
									<div class="search-c">
										<select class="form-control form-sm" name="r_salescd1nm" id="r_salescd1nm" onchange="categorySelect(2); dataSearchPop();">
											<option value="">선택하세요</option>
										</select>
										<select class="form-control form-sm" name="r_salescd2nm" id="r_salescd2nm" onchange="categorySelect(3); dataSearchPop();">
											<option value="">선택하세요</option>
										</select>
										<select class="form-control form-sm" name="r_salescd3nm" id="r_salescd3nm" onchange="dataSearchPop();">
											<option value="">선택하세요</option>
										</select>
<!-- 										<select class="form-control form-sm" name="r_salescd4nm" id="r_salescd4nm" onchange="dataSearchPop();"> 
											<option value="">선택하세요</option>
										</select> -->
									</div>
								</li>
								<li>
									<label class="search-h">두께</label>
									<div class="search-c">
										<input type="text" class="search-input" name="rl_thicknm" onkeypress="if(event.keyCode == 13){dataSearchPop();}" />
									</div>
								</li>
								<li>
									<label class="search-h">폭</label>
									<div class="search-c">
										<input type="text" class="search-input" name="rl_widthnm" onkeypress="if(event.keyCode == 13){dataSearchPop();}" />
									</div>
								</li>
								<li>
									<label class="search-h">길이</label>
									<div class="search-c">
										<input type="text" class="search-input" name="rl_lengthnm" onkeypress="if(event.keyCode == 13){dataSearchPop();}" />
									</div>
								</li>
 								<!-- <li> 
									<label class="search-h">SEARCH-TEXT</label>
									<div class="search-c">
										<input type="text" class="search-input" name="rl_searchtext" onkeypress="if(event.keyCode == 13){dataSearchPop();}" />
									</div>
								</li> -->
								<li>
									<label class="search-h">품목코드</label>
									<div class="search-c">
										<input type="text" class="search-input" name="rl_itemcd" onkeypress="if(event.keyCode == 13){dataSearchPop();}" />
									</div>
								</li>
								<li>
									<label class="search-h">품목명검색1</label>
									<div class="search-c">
										<input type="text" class="search-input" name="rl_desc1" onkeypress="if(event.keyCode == 13){dataSearchPop();}" />
									</div>
								</li>
								<li>
									<label class="search-h">품목명검색2</label>
									<div class="search-c">
										<input type="text" class="search-input" name="rl_desc2" onkeypress="if(event.keyCode == 13){dataSearchPop();}" />
									</div>
								</li>
								<li>
									<label class="search-h">즐겨찾기 품목</label>
									<div class="search-c">
										<label class="no-p-r"><input type="checkbox" name="r_checkitembookmark" onclick="dataSearchPop();" value="Y" ${checked} />즐겨찾기</label>
									</div>
								</li>

								<!-- 
								<li>
									<label class="search-h">이미지</label>
									<div class="search-c checkbox">
										<label><input type="checkbox" name="ri_existimageyn" value="N" onclick="dataSearchPop();" />N</label>
									</div>
								</li>
								<li>
									<label class="search-h">관련품목</label>
									<div class="search-c checkbox">
										<label><input type="checkbox" name="ri_existrecommend" value="N" onclick="dataSearchPop();" />N</label>
									</div>
								</li>
								-->
							</ul>
						</div>
					</div>
					
					<h5 class="table-title pull-left no-m-t">Total <span id="listTotalCountSpanId">0</span> EA</h5>
					<div class="btnList">
						<c:if test="${r_multiselect}">
							<button type="button" class="btn btn-warning" onclick="dataSelect(this, '', '1');">선택후 계속</button>
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