<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/front/commonimport.jsp" %>
<!DOCTYPE HTML>
<!--[if IE 8]><html lang="ko" class="ie8"><![endif]-->
<!--[if gt IE 8]><!--><html lang="ko"><!--<![endif]-->
<head>
<%@ include file="/WEB-INF/views/include/front/commonhead.jsp" %>

<script type="text/javascript">
var categorySelectedNum = 0; // 2025-03-27 hsg Dragonrana : 대,중,소 유형분류에서 어떤 유행을 선택했는지 기록용 변수. 1:대, 2:중, 3:소



$(function(){
	//bdtypeInit(); // 2025-03-27 hsg Dragonrana : 대,중,소 유형분류 초기화
});

$(document).ready(function() {

});

// 자료실 상세 팝업
function boardViewPop(obj, bdSeq){
	if(!isApp()){
		// 팝업 세팅.
		var widthPx = 955;
		var heightPx = 772;
		var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx;
	
		$('form[name="frmPop"]').find('input[name="r_bdseq"]').val(bdSeq);
	
		window.open('', 'referenceViewPop', options);
		$('form[name="frmPop"]').prop('action', '${url}/front/board/referenceViewPop.lime');
		$('form[name="frmPop"]').submit();
	}
	else{
		// 모달팝업
		//$('#boardViewPopMId').modal('show');
		var link = '${url}/front/board/referenceViewPop.lime?r_bdseq='+bdSeq+'&layer_pop=Y&';
		// 부모창refresh
		$(document).on('hidden.bs.modal', function (e) {   // bootstrap modal refresh
	       $(e.target).removeData('bs.modal');
	    });
		$('#boardViewPopMId').modal({
			remote: link
		});
	}
}

// 페이징.
function pager(curpage) {
	$('input[name="page"]').val(curpage);
	formPostSubmit('', '${url}/front/board/referenceList.lime');
}

// 조회.
function dataSearch() {
	$('input[name="page"]').val('1');
	debugger;
	formPostSubmit('', '${url}/front/board/referenceList.lime');
}

/**
 * Category select
 * 2025-03-27 hsg Dragonrana : 대,중,소 유형분류 초기화
 */
function bdtypeInit(){
	//debugger;
	var textHtml = '';


	$('select[name="r_bdtype2"]').empty();
	$('select[name="r_bdtype3"]').empty();

	textHtml += '<option value="">선택해 주세요.</option>';

	$('select[name="r_bdtype2"]').append(textHtml);
	$('select[name="r_bdtype3"]').append(textHtml);



}

/**
 * Category select
 * 2025-03-27 hsg Dragonrana : 유형분류를 선택했을 때 하위 유형분류 설정. categorySelected 함수 추가
 */
function categorySelected(listType,listValue){
	//alert('listType:'+listType);

	$.ajax({
		async : true,
		data : {r_bdtype:listValue , depth:listType},
		type : 'POST',
		url : '${url}/front/board/eorderReferenceCategoryList.lime',
		success : function( data ){
			var categ = data.referenceCategoryList;
			
			var textHtml = '';

			if(listType == 1) {
				$('select[name="r_bdtype2"]').empty();
				$('select[name="r_bdtype3"]').empty();

				textHtml += '<option value="">선택해 주세요.</option>';
				$(categ).each(function(i,e){
					textHtml += '<option value="'+e.CC_CODE+'">'+e.CC_NAME+'</option>';
				});
				
				$('select[name="r_bdtype2"]').append(textHtml);

				textHtml = '';
				textHtml += '<option value="">선택해 주세요.</option>';
				$('select[name="r_bdtype3"]').append(textHtml);
			} else if(listType == 2) {
				$('select[name="r_bdtype3"]').empty();

				textHtml = '';
				textHtml += '<option value="">선택해 주세요.</option>';
				$(categ).each(function(i,e){
					textHtml += '<option value="'+e.CC_CODE+'">'+e.CC_NAME+'</option>';
				});
				
				$('select[name="r_bdtype3"]').append(textHtml);

			}
		},
		error : function(request,status,error){
			alert('Error:'+status+'// error:'+error.toString());
		}
	});

}

</script>
</head>
<body>

<div id="subWrap" class="subWrap">

	<!-- Header -->
	<%@ include file="/WEB-INF/views/include/front/header.jsp" %>

	<%-- 팝업 전송 form --%>
	<form name="frmPop" method="post" target="referenceViewPop">
		<input name="pop" type="hidden" value="1" />
		<input name="r_bdseq" type="hidden" value="" />
	</form>

	<div class="container-fluid">
		<div class="full-content">

			<div class="row no-m">
				<div class="page-breadcrumb"><strong>자료실</strong></div>

				<div class="page-location">
					<ul>
						<li><a href="${url}/front/index/index.lime"><img src="${url}/include/images/front/common/location_home.png" alt="img" /></a></li>
						<li><a href="${url}/front/board/noticeList.lime">정보공유</a></li>
						<li>
							<select onchange="formGetSubmit(this.value, '');">
								<option value="${url}/front/board/noticeList.lime" <c:if test="${fn:contains(requestScope['javax.servlet.forward.servlet_path'], '/front/board/noticeList.lime')}">selected="selected"</c:if> >공지사항</option>
								<option value="${url}/front/board/faqList.lime" <c:if test="${fn:contains(requestScope['javax.servlet.forward.servlet_path'], '/front/board/faqList.lime')}">selected="selected"</c:if> >FAQ</option>
								<option value="${url}/front/board/referenceList.lime" <c:if test="${fn:contains(requestScope['javax.servlet.forward.servlet_path'], '/front/board/referenceList.lime')}">selected="selected"</c:if> >자료실</option>
								<option value="${url}/front/board/sampleList.lime" <c:if test="${fn:contains(requestScope['javax.servlet.forward.servlet_path'], '/front/board/sampleList.lime')}">selected="selected"</c:if> >샘플요청</option>
								<option value="${url}/front/board/chatList.lime" <c:if test="${fn:contains(requestScope['javax.servlet.forward.servlet_path'], '/front/board/chatList.lime')}">selected="selected"</c:if> >채팅 피드백</option>
							</select>
						</li>
					</ul>
				</div>
			</div> <!-- Row -->

		</div> <!-- Full Content -->
	</div> <!-- Container Fluid -->

	<!-- Container -->
	<main class="container" id="container">

		<!-- Content -->
		<div class="content">
			<form name="frm" method="post" >

				<%-- Start. Use For Paging --%>
				<input name="page" type="hidden" value="${r_page}" /><%-- r_page --%>
				<input name="rows" type="hidden" value="${r_limitrow}" /><%-- r_limitrow --%>
				<%-- End. --%>
			<!-- Row -->
			<div class="row">
				<!-- Col-md-12 -->
				<div class="col-md-12">

					<div class="searchArea">
						<div class="col-md-4"><!-- 2025-03-27 hsg Dragonrana : 관리자 페이지와 같이 대,중,소 유형으로 변경을 위해 제목의 길이를 줄임. col-md-6 -> col-md-4 -->
							<em>제목</em>
							<input type="text" class="form-control" name="rl_bdtitle" value="${param.rl_bdtitle}" />
						</div>
						<div class="col-md-6 right"><!-- 2025-03-27 hsg Dragonrana : 관리자 페이지와 같이 대,중,소 유형으로 변경을 위해 제목의 길이를 늘임. col-md-4 -> col-md-6 -->
							<em class="high">유형</em>
							<!-- 2025-03-27 hsg Dragonrana 시작 : E-Order Front 자료실메뉴에 유형분류를 admin의 자료실 유형분류처럼 대중소로 구성  -->
							<table style="table-layout:fixed; width:75%;">
							<tr>
								<td style="width:33%;">
									<select class="form-control form-sm" name="r_bdtype" onchange="categorySelected(1, this.value)">
										<c:forEach items="${referenceCategoryList}" var="list" varStatus="stat">
											<c:if test="${stat.first}">
												<option value="">선택하세요</option>
											</c:if>
											<option value="${list.CC_CODE}" <c:if test="${param.r_bdtype eq list.CC_CODE }">selected</c:if>>${list.CC_NAME}</option>
										</c:forEach>
									</select>
									</td>
									<td style="width:33%;">
									<select class="form-control" name="r_bdtype2" onchange="categorySelected(2, this.value)">
										<c:forEach items="${referenceCategory2List}" var="list" varStatus="stat">
											<c:if test="${stat.first}">
												<option value="">선택하세요</option>
											</c:if>
											<option value="${list.CC_CODE}" <c:if test="${param.r_bdtype2 eq list.CC_CODE }">selected</c:if>>${list.CC_NAME}</option>
										</c:forEach>
									</select>
									</td>
									<td  style="width:33%;">
									<select class="form-control" name="r_bdtype3" onchange="categorySelected(3, this.value)">
										<c:forEach items="${referenceCategory3List}" var="list" varStatus="stat">
											<c:if test="${stat.first}">
												<option value="">선택하세요</option>
											</c:if>
											<option value="${list.CC_CODE}"<c:if test="${param.r_bdtype3 eq list.CC_CODE }">selected</c:if>>${list.CC_NAME}</option>
										</c:forEach>
									</select>
								</td
							</tr>
							</table>
							<!-- 2025-03-27 hsg Dragonrana 끝 : E-Order Front 자료실메뉴에 유형분류를 admin의 자료실 유형분류처럼 대중소로 구성  -->
						</div>
						<div class="col-md-1 empty searchBtn">
							<button type="button" onclick="dataSearch();">Search</button>
						</div>

					</div> <!-- searchArea -->
					<div class="searchBtn full-tablet">
						<button type="button" class="full-width" onclick="dataSearch();">Search</button>
					</div>

					<div class="boardListArea">
						<h2>
							Total <strong>${listTotalCount}</strong>EA
							<div class="title-right little">
<%--								<button type="button" class="btn-excel"><img src="${url}/include/images/front/common//icon_excel@2x.png" alt="img" /></button>--%>
							</div>
						</h2>

						<div class="boardList">
							<!-- desktop -->
							<table class="full-desktop" width="100%" cellpadding="0" cellspacing="0" border="0">
								<colgroup>
									<col width="7%" />
									<col width="8%" />
									<col width="12%" />
									<col width="43%" />
									<col width="15%" />
									<col width="15%" />
								</colgroup>
								<thead>
								<tr>
									<th>NO</th>
									<th>유형</th>
									<th>첨부파일</th>
									<th>제목</th>
									<th>등록자</th>
									<th>등록일</th>
								</tr>
								</thead>
								<tbody>
								<c:forEach items="${list}" var="list" varStatus="status">
									<tr>
										<td>
											<c:if test="${list.BD_NOTICEYN eq 'Y'}"><span class="noticeBox">공지</span></c:if>
											<c:if test="${list.BD_NOTICEYN eq 'N'}">${listTotalCount -((r_page-1) * r_limitrow + status.index)}</c:if>
										</td>
										<td>${list.BD_TYPENM}</td>
										<td><c:if test="${list.BD_FILEYN eq 'Y'}"><img src="${url}/include/images/front/common/icon_file@2x.png" width="22" height="22" alt="file" /></c:if></td>
										<td class="text-left"><a href="#" onclick="boardViewPop(this, '${list.BD_SEQ}');" class="nowrap">${list.BD_TITLE}</a></td>
										<td>${list.USER_NM}</td>
										<td>${fn:substring(list.BD_MODATE==null?list.BD_INDATE:list.BD_MODATE,0,10)}</td>
									</tr>
								</c:forEach>
								</tbody>
							</table>
							
							<!-- mobile -->
							<table class="full-mobile" width="100%" cellpadding="0" cellspacing="0" border="0">
								<colgroup>
									<col width="10%" />
									<col width="15%" />
									<col width="20%" />
									<col width="55%" />
								</colgroup>
								<thead>
								<tr>
									<th>NO</th>
									<th>유형</th>
									<th>첨부파일</th>
									<th>제목</th>
								</tr>
								</thead>
								<tbody>
								<c:forEach items="${list}" var="list" varStatus="status">
									<tr>
										<td>
											<c:if test="${list.BD_NOTICEYN eq 'Y'}">공지</c:if>
											<c:if test="${list.BD_NOTICEYN eq 'N'}">${listTotalCount -((r_page-1) * r_limitrow + status.index)}</c:if>
										</td>
										<td>${list.BD_TYPENM}</td>
										<td><c:if test="${list.BD_FILEYN eq 'Y'}"><img src="${url}/include/images/front/common/icon_file@2x.png" width="22" height="22" alt="file" /></c:if></td>
										<td class="text-left"><a href="#" onclick="boardViewPop(this, '${list.BD_SEQ}');" class="nowrap">${list.BD_TITLE}</a></td>
									</tr>
								</c:forEach>
								</tbody>
							</table>
						</div> <!-- boardList -->

						<!-- BEGIN paginate -->
						<c:if test="${!empty list}">
							<%@ include file="/WEB-INF/views/include/front/pager.jsp" %>
						</c:if>
						<!-- END paginate -->

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

			</form>
		</div> <!-- Content -->
	</main> <!-- Container -->

	<%@ include file="/WEB-INF/views/include/front/bottom.jsp" %>

	<%@ include file="/WEB-INF/views/include/front/footer.jsp" %>
	
	<!-- Modal -->
	<div class="modal fade" id="boardViewPopMId" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
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