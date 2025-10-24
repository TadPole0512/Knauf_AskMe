<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/front/commonimport.jsp" %>
<!DOCTYPE HTML>
<!--[if IE 8]><html lang="ko" class="ie8"><![endif]-->
<!--[if gt IE 8]><!--><html lang="ko"><!--<![endif]-->
<head>
<c:set var="isLayerPop" value="false" />
<c:if test="${'Y' eq param.layer_pop}"><c:set var="isLayerPop" value="true" /></c:if>

<c:if test="${!isLayerPop}">
	<%@ include file="/WEB-INF/views/include/front/commonhead.jsp" %>
	<%@ include file="/WEB-INF/views/include/front/commonpop.jsp" %>
</c:if>

<!-- 2025-05-07 hsg. Nuclear lunch Detected :  오늘 날짜를 “날짜만” yyyy-MM-dd 형태로 만든 뒤 다시 Date 객체로 변환 -->
<!-- 1) scriptlet로 Date 생성 -->
<c:set var="today" value="<%= new java.util.Date() %>" />
<!-- 2) Date → yyyy-MM-dd 문자열 -->
<fmt:formatDate value="${today}" pattern="yyyy-MM-dd" var="todayString" />

<script type="text/javascript">
var page_type = toStr('${param.page_type}'); // orderadd


// var multi_select = toStr('${r_multiselect}');
// multi_select = ('true' == multi_select) ? true : false;

$(function(){
	
});

// 선택. 개별만.
function dataSelect(obj, div) { // div : 1=선택후계속,2=선택후 닫기.
	$(obj).prop('disabled', true);
	
	var confirmText = '납품처를 선택 하시겠습니까?';
	
	// 개별
	var rowObj = $(obj).closest('tr');

	let shipto_cd = rowObj.attr('shiptoCdAttr').toString().trim();
	let shipto_nm = rowObj.attr('shiptoNmAttr').toString().trim();
	let cust_cd = rowObj.attr('custCdAttr').toString().trim();
	let quote_qt = rowObj.attr('quoteQtAttr').toString().trim(); //2025-06-04 ijy. 쿼테이션 검증을 위한 쿼테이션 번호 추가

	// ↓↓↓↓↓↓↓↓↓↓↓ 2025-05-07 hsg. Nuclear lunch Detected : 납품처의 유효기간이 표기 및 유효기간만료된 현장은 선택이 되지 않게 수정 요청 => 납품처 선택시 오늘 날짜와 비교하여 만료날짜가 도래 하였을 경우 선택 시, 선택 불가 메시지 출력 ↓↓↓↓↓↓↓↓↓↓↓
	let bnddt = rowObj.attr('bnddtAttr').toString().trim();
	//alert('${todayString} : ' + bnddt);
	if('${todayString}' > bnddt){
		alert('만료된 납품처 입니다');
		return;
	}
	// ↑↑↑↑↑↑↑↑↑↑↑ 2025-05-07 hsg. Nuclear lunch Detected : 납품처의 유효기간이 표기 및 유효기간만료된 현장은 선택이 되지 않게 수정 요청 => 납품처 선택시 오늘 날짜와 비교하여 만료날짜가 도래 하였을 경우 선택 시, 선택 불가 메시지 출력 ↑↑↑↑↑↑↑↑↑↑↑



	if( (rowObj.attr('quoteQtAttr') === '') && (shipto_cd === cust_cd) ) {
		shipto_cd = '';
		shipto_nm = '';
	} 
	
	var shipto = {
			SHIPTO_CD : shipto_cd //rowObj.attr('shiptoCdAttr')
			, SHIPTO_NM : shipto_nm //rowObj.attr('shiptoNmAttr')
			, CUST_CD : rowObj.attr('custCdAttr')
			, ADD1 : rowObj.attr('add1Attr')
			, ADD2 : rowObj.attr('add2Attr')
			, ADD3 : rowObj.attr('add3Attr')
			, ADD4 : rowObj.attr('add4Attr')
			, ZIP_CD : rowObj.attr('zipCdAttr')
			, QUOTE_QT : quote_qt  //2025-06-04 ijy. 쿼테이션 검증을 위한 쿼테이션 번호 추가
		};

	if (page_type == 'orderadd' || page_type == 'factreport') {
		confirmText = '해당 납품처를 선택 하시겠습니까?';
		if(confirm(confirmText)){
			opener.setShiptoFromPop(shipto);
			$(obj).prop('disabled', false);
		}else{
			$(obj).prop('disabled', false);
			return;
		}
	}
	
	$(obj).prop('disabled', false);
	
	if('2' == div) window.open('about:blank', '_self').close();
}

//페이징.
function pager(curpage) {
	$('input[name="page"]').val(curpage);
	formPostSubmit('frm_pop', '${url}/front/base/pop/shiptoListPop.lime');
}

// 조회.
function dataSearchPop() {
	if($('input[name="r_stbchkyn"]:checked').val() == 'Y'){
		$('input[name="where"]').val('pop');
	}
	
	$('input[name="page"]').val('1');
	formPostSubmit('frm_pop', '${url}/front/base/pop/shiptoListPop.lime');
}

//즐겨찾기 추가/삭제
function setBookmarkAjax(obj, r_shiptocd){
	$(obj).prop('disabled', true);
	$.ajax({
		async : false,
		data : {
			r_stbshiptocd : r_shiptocd
		},
		type : 'POST',
		url : '${url}/front/base/setShiptoBookmarkAjax.lime',
		success : function(data) {
			if(data.inCnt > 0){
				$(obj).html('<img class="favorites" src="${url}/include/images/front/common/icon_star@2x_active.png" width="20" height="20" alt="img" />');
			}else{
				$(obj).html('<img class="favorites" src="${url}/include/images/front/common/icon_star@2x.png" width="20" height="20" alt="img" />');
			}
			$(obj).prop('disabled', false);
		},
		error : function(request,status,error){
			alert('Error');
			$(obj).prop('disabled', false);
		}
	});
}

</script>
</head>

<body> <!-- 팝업사이즈 795 * 652 -->

<!-- Wrap -->
<div id="wrap" class="popup-wrapper">

<form name="frm_pop" method="post" >
<input type="hidden" name="pop" value="${param.pop}" />
<input type="hidden" name="page_type" value="${param.page_type}" />
<input type="hidden" name="r_multiselect" value="${param.r_multiselect}" />
<input type="hidden" name="where" value="" />

<%-- Start. Use For Paging --%>
<input name="page" type="hidden" value="${r_page}" /><%-- r_page --%>
<input name="rows" type="hidden" value="${r_limitrow}" /><%-- r_limitrow --%>
<%-- End. --%>

	<!-- Container Fluid -->
	<div class="container-fluid product-search">
	
		<button type="button" class="close" onclick="window.close();"><img src="${url}/include/images/front/common/total_menu_close_icon.png" width="20" height="20" alt="close" /></button>
		
		<div class="panel-title">
			<h2>
				납품처선택
				<div class="searchBtn">
					<button type="button" onclick="dataSearchPop();">Search</button>
				</div>
			</h2>
		</div>
		
		<div class="boardView">
			<ul>
				<li class="half wide">
					<label class="view-h">납품처코드</label>
					<div class="view-b">
						<input type="text" class="form-control" name="rl_shiptocd" value="${param.rl_shiptocd}" onkeypress="if(event.keyCode == 13){dataSearchPop();}" />
					</div>
				</li>
				<li class="half narrow one">
					<label class="view-h">즐겨찾기</label>
					<div class="view-b">
						<div class="table-checkbox">
							<label class="lol-label-checkbox" for="checkbox">
								<input type="checkbox" id="checkbox" name="r_stbchkyn" value="Y" onclick="dataSearchPop();" <c:if test="${param.r_stbchkyn eq 'Y'}">checked</c:if> />
								<span class="lol-text-checkbox">납품처 즐겨찾기 불러오기</span>
							</label>
						</div>
					</div>
				</li>
				<li>
					<label class="view-h">납품처명</label>
					<div class="view-b">
						<input type="text" class="form-control" name="rl_shiptonm" value="${param.rl_shiptonm}" onkeypress="if(event.keyCode == 13){dataSearchPop();}" />
					</div>
				</li>
			</ul>
		</div> <!-- boardView -->
		
		<div class="boardListArea">
			<h2>
				Total <strong><fmt:formatNumber value="${cvt:toInt(listTotalCount)}" pattern="#,###" /></strong>EA
			</h2>
			
			<div class="boardList">
				<table width="100%" cellpadding="0" cellspacing="0" border="0">
					<colgroup>
<!-- 2025-05-07 hsg. Nuclear lunch Detected : 납품처의 유효기간이 표기 및 유효기간만료된 현장은 선택이 되지 않게 수정 요청 => 납품처의 유효기간이 표기로 인해 주석
	 					<col width="7%" />
						<col width="15%" />
						<col width="45%" />
						<col width="15%" />
						<col width="15%" />
 -->
 						<col width="7%" />
						<col width="11%" />
						<col width="45%" />
						<col width="15%" />
						<col width="11%" />
						<col width="11%" />
 					</colgroup>
					<thead>
						<tr>
							<th>NO</th>
							<th>납품처코드</th>
							<th>납품처명</th>
							<th>만료일</th><!-- 2025-05-07 hsg. Nuclear lunch Detected : 납품처의 유효기간이 표기 및 유효기간만료된 현장은 선택이 되지 않게 수정 요청 => 납품처의 유효기간이 표기 -->
							<th>기능</th>
							<th>즐겨찾기</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${list}" var="list" varStatus="status">
							<!-- 2025-05-07 hsg. Nuclear lunch Detected : 납품처의 유효기간이 표기 및 유효기간만료된 현장은 선택이 되지 않게 수정 요청 => 납품처의 유효기간을 확인하기 위해 tr에 bnddtAttr="${list.BNDDT}" 추가 -->
							<tr shiptoCdAttr="${list.SHIPTO_CD}" shiptoNmAttr="${list.SHIPTO_NM}" custCdAttr="${list.CUST_CD}" add1Attr="${list.ADD1}" add2Attr="${list.ADD2}" add3Attr="${list.ADD3}" add4Attr="${list.ADD4}" zipCdAttr="${list.ZIP_CD}" quoteQtAttr="${list.QUOTE_QT}" bnddtAttr="${list.BNDDT}">
								<td><fmt:formatNumber value="${cvt:toInt(startnumber)}" pattern="#,###" /></td>
								<td class="text-left">${list.SHIPTO_CD}</td>
								<td class="text-left"><p class="">${list.SHIPTO_NM}</p></td>
								<!-- ↓↓↓↓↓↓↓↓↓↓↓ 2025-05-07 hsg. Nuclear lunch Detected : 납품처의 유효기간이 표기 및 유효기간만료된 현장은 선택이 되지 않게 수정 요청 => 오늘 이전의 날짜인 경우 빨강, 아닌 경우 검정 ↓↓↓↓↓↓↓↓↓↓↓ -->
								<td class="text-left">
								<c:if test="${not empty list.BNDDT}">
									<!-- Date.getTime() 프로퍼티를 이용해 밀리초 비교 -->
									<c:choose>
									  <c:when test="${list.BNDDT lt todayString}">
									    <!-- 비교 문자열이 오늘보다 이전일 때 -->
									    <span style="color:red;">${list.BNDDT}</span>
									  </c:when>
									  <c:otherwise>
									    <!-- 오늘 이후일 때 -->
									    <span class="">${list.BNDDT}</span>
									  </c:otherwise>
									</c:choose>
								</c:if>
								</td>
								<!-- ↑↑↑↑↑↑↑↑↑↑↑ 2025-05-07 hsg. Nuclear lunch Detected : 납품처의 유효기간이 표기 및 유효기간만료된 현장은 선택이 되지 않게 수정 요청 => 오늘 이전의 날짜인 경우 빨강, 아닌 경우 검정 ↑↑↑↑↑↑↑↑↑↑↑ -->
								<td><button type="button" class="btn btn-green" onclick="dataSelect(this, '2');" >선택</button></td>
								<td>
									<c:choose>
										<c:when test="${empty list.STB_SHIPTOCD}">
											<button type="button" onclick="setBookmarkAjax(this, '${list.SHIPTO_CD}');"><img class="favorites" src="${url}/include/images/front/common/icon_star@2x.png" width="20" height="20" alt="img" /></button>
										</c:when>
										<c:otherwise>
											<button type="button" onclick="setBookmarkAjax(this, '${list.SHIPTO_CD}');"><img class="favorites" src="${url}/include/images/front/common/icon_star@2x_active.png" width="20" height="20" alt="img" /></button>
										</c:otherwise>
									</c:choose>
								</td>
							</tr>
							<c:set var="startnumber" value="${startnumber-1}" />
						</c:forEach>
						
						<c:if test="${empty list}">
							<tr>
								<td colspan="5" class="list-empty">
									<img src="${url}/include/images/front/common/icon_empty.png" alt="img" /><br />
									등록된 납품처가 없습니다.
								</td>
							</tr>
						</c:if>
					</tbody>
				</table>
				
			</div> <!-- boardList -->
			
			<!-- BEGIN paginate -->
			<c:if test="${!empty list}">
				<%@ include file="/WEB-INF/views/include/front/pager.jsp" %>
			</c:if>
			<!-- END paginate -->
			
		</div> <!-- boardListArea -->
		
	</div> <!-- Container Fluid -->

</form>
	
</div> <!-- Wrap -->

</body>
</html>