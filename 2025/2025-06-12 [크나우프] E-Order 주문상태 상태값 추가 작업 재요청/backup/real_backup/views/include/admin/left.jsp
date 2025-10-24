<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript">

//Left-Menu 높이 계산.
var sidebarAndContentHeight_sub = function (){
	var content = $('.page-inner')
	, sidebar = $('.page-sidebar')
	, body = $('body')
	, height
	, footerHeight = $('.page-footer').outerHeight()
	, pageContentHeight = $('.page-content').height();
        
	content.attr('style', 'min-height:' + sidebar.height() + 'px !important');
        
	if (body.hasClass('page-sidebar-fixed')) {
		height = sidebar.height() + footerHeight;
	} else {
		height = sidebar.height() + footerHeight;
		if (height  < $(window).height()) {
			height = $(window).height();
		}
	}
        
	if (height >= content.height()) {
		content.attr('style', 'min-height:' + height + 'px');
	}
};

var menuSeq = '${requestScope.mnSeq}';
var ss_menu = '${sessionScope.loginDto.menu}';
var isWriteAuth = true; //스크립트에서 제어할 경우 사용.
$(function() {
	// 권한이 있는 메뉴 show && 쓰기 버튼 및 영역 hide.
	var ss_menuarr = ss_menu.split('|');
	var value1 = '', value2 = '';
	$.each(ss_menuarr, function(i,e){
		var temp = e.split(":");
		value1 = temp[0];
		value2 = temp[1];
		
		if('' != value1){
			// 권한이 있는 메뉴 show.
			var mnObj = $('li[mnSeqAttr="'+value1+'"]');
			mnObj.show();
			mnObj.parent().closest('li').show();
			mnObj.parent().closest('.left1MenuClass').show();
			
			// 쓰기 버튼 및 영역 hide.
			if(value1 == menuSeq && value2 == 'R'){
				$('.writeObjectClass').hide();
				isWriteAuth = false;
			}
		}
	});
	
	// 현재 메뉴에 open 클래스 추가 && 
	var nowObj = $('li[mnSeqAttr="'+menuSeq+'"]');
	var nowMenuDepth = nowObj.attr('mnDepthAttr');
	if('3' == nowMenuDepth){ // 선택한 메뉴가 3 Depth인 경우, 2 Depth 펼치기, 1 Depth 펼치기
		nowObj.closest('.left2MenuClass').addClass('open');
		nowObj.closest('ul').show();
		nowObj.parent().closest('.left1MenuClass').addClass('open');
		nowObj.parent().parent().parent().show();
		nowObj.addClass('open');
		sidebarAndContentHeight_sub(); // min-height 다시 구하기.
	}
	if('2' == nowMenuDepth){ // 선택한 메뉴가 2 Depth인 경우, 1 Depth 펼치기
		nowObj.parent().parent('.left1MenuClass').addClass('open');
		nowObj.parent().show();
		nowObj.addClass('open');
		sidebarAndContentHeight_sub(); // min-height 다시 구하기.
	}
	
	// 메뉴닫기
	var mnBtn = getCookie('mnBtn');
	if(mnBtn == 'Y'){
		$('body').addClass("small-sidebar");
	}
});

// Left Banner Image
$(window).on('ready load resize', function() {
	var menuLiImg = $('.menu').children(':first').find('img'),
		heightMenuLi = menuLiImg.height();
	if(heightMenuLi > 50) {
		menuLiImg.css('margin-top', -(heightMenuLi-50)/2);
	}
	$('.sidebar-toggle').on('click', function() {
		var menuLiImg = $('.menu').children(':first').find('img'),
			widthMenuLi = menuLiImg.width(),
			isSmall = $('body').hasClass('small-sidebar');
		if(isSmall && widthMenuLi > 65) {
			menuLiImg.css('margin-left', -(widthMenuLi-65)/2);
		} else {
			menuLiImg.css('margin-left', 0);
		}
	});
});
</script>

<div class="page-sidebar sidebar">
	<!-- Page Sidebar Inner -->
	<div class="page-sidebar-inner slimscroll">
		<ul class="menu accordion-menu">
			<li class="droplink">
				<img class="" src="${url}/data/config/left-img.jpg" alt="" onerror="this.src='${url}/include/images/admin/left-img.jpg'" />
			</li>

			<li class="dropdown mo">
				<a href="#" class="dropdown-toggle waves-effect waves-button waves-classic" data-toggle="dropdown">
					<span class="user-name">${sessionScope.loginDto.userNm}<i class="fa fa-angle-down m-l-xxs"></i></span>
					<img class="img-circle avatar pull-right no-m" src="${url}/data/user/${sessionScope.loginDto.userFile}" width="25" height="25" alt="" onerror="this.src='${url}/include/images/admin/img.jpg'" />
				</a>
				<ul class="dropdown-menu dropdown-list" role="menu">
					<li role="presentation"><a href="${url}/admin/mypage/myInformationView.lime"><i class="fa fa-user"></i>나의정보</a></li>
					<li role="presentation"><a href="${url}/admin/mypage/csSalesUserList.lime"><i class="fa fa-sitemap"></i>조직설정</a></li>
					<li role="presentation"><a href="javascript:logout();"><i class="fa fa-sign-out m-r-xs"></i>Logout</a></li>
				</ul>
			</li>

			<!-- <li class="droplink mo">
				<a href="javascript:();" onclick="location.href='${url}/admin/mypage/basketList.lime'" class="waves-effect waves-button">
					<span class="menu-icon fa fa-comments"></span>
					<p class="task-details">장바구니</p>
					<div class="task-icon badge badge-warning">1</div>
				</a>
			</li> -->
			<!-- <li class="droplink mo">
				<a class="waves-effect waves-button">
					<span class="menu-icon fa fa-comments"></span>
					<p class="task-details">채팅</p>
					<div class="task-icon badge badge-info">1</div>
				</a>
			</li> -->
			<%-- <li class="droplink mo b-b b-green">
				<a class="waves-effect waves-button"><span class="menu-icon fa fa-bell"></span><p>알림</p><div class="task-icon badge badge-warning">0</div></a>
				<ul class="sub-menu">
					<li><a href="${url}/admin/customer/customerList.lime">거래처현황<div class="task-icon badge badge-warning">0</div></a></li>
					<li><a href="${url}/admin/order/orderList.lime">웹주문현황<div class="task-icon badge badge-warning">0</div></a></li>
					<li><a href="${url}/admin/item/itemList.lime">품목현황<div class="task-icon badge badge-warning">0</div></a></li>
					<li><a href="${url}/admin/promotion/promotionList.lime">이벤트현황<div class="task-icon badge badge-warning">0</div></a></li>
				</ul>
			</li> --%>
			
			<!-- ### Start ### -->
			
			<li class="droplink left1MenuClass" style="display:none;" mnSeqAttr="2" mnDepthAttr="1">
				<a class="waves-effect waves-button"><span class="menu-icon glyphicon glyphicon-cog"></span><p>시스템</p><span class="arrow"></span></a>
				<ul class="sub-menu">
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="3" mnDepthAttr="2"><a href="${url}/admin/system/systemConfig.lime">환경설정</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="4" mnDepthAttr="2" ><a href="${url}/admin/system/adminUserConfig.lime">사용자현황</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="46" mnDepthAttr="2" ><a href="${url}/admin/system/postalCodeManager.lime">우편번호관리</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="5" mnDepthAttr="2" ><a href="${url}/admin/system/plantConfig.lime">출고지관리</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="6" mnDepthAttr="2" ><a href="${url}/admin/system/commonCodeConfig.lime">공통코드</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="7" mnDepthAttr="2" ><a href="${url}/admin/system/authorityConfig.lime">권한관리</a></li>
				</ul>
			</li>
			
			<li class="droplink left1MenuClass" style="display:none;" mnSeqAttr="8" mnDepthAttr="1">
				<a class="waves-effect waves-button"><span class="menu-icon glyphicon glyphicon-bookmark"></span><p>거래처관리</p><span class="arrow"></span></a>
				<ul class="sub-menu">
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="9" mnDepthAttr="2"><a href="${url}/admin/customer/customerList.lime">거래처현황</a></li>
					<%-- <li class="left2MenuClass" style="display:none;" mnSeqAttr="10" mnDepthAttr="2" ><a href="${url}/admin/customer/customerUserList.lime">계정관리</a></li> --%>
				</ul>
			</li>
			
			<li class="droplink left1MenuClass" style="display:none;" mnSeqAttr="11" mnDepthAttr="1">
				<a class="waves-effect waves-button"><span class="menu-icon glyphicon glyphicon-shopping-cart"></span><p>주문관리</p><span class="arrow"></span></a>
				<ul class="sub-menu">
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="12" mnDepthAttr="2"><a href="${url}/admin/order/orderAdd.lime">주문등록</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="13" mnDepthAttr="2"><a href="${url}/admin/order/orderList.lime">웹주문현황</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="14" mnDepthAttr="2" ><a href="${url}/admin/order/salesOrderList.lime">전체주문현황</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="49" mnDepthAttr="2" ><a href="${url}/admin/order/orderStateUpdateExcel.lime">오더 상태 업데이트</a></li><!-- 2025-03-28 hsg Sunset Flip : Xlsx 혹은 Csv 파일 형태를 E-order의 ‘오더 상태 업데이트’에 업로드. -->
				</ul>
			</li>
			
			<li class="droplink left1MenuClass" style="display:none;" mnSeqAttr="11" mnDepthAttr="1">
				<a class="waves-effect waves-button"><span class="menu-icon glyphicon glyphicon glyphicon-th-large"></span><p>QMS관리</p><span class="arrow"></span></a>
				<ul class="sub-menu">
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="37" mnDepthAttr="2" ><a href="${url}/admin/order/qmsOrderList.lime">QMS 조회 및 등록</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="42" mnDepthAttr="2" ><a href="${url}/admin/system/qmsConfig.lime">QMS 설정</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="38" mnDepthAttr="2" ><a href="${url}/admin/system/qmsDepartment.lime">QMS 조직설정</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="41" mnDepthAttr="2" ><a href="${url}/admin/system/qmsDedalines.lime">QMS 마감</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="39" mnDepthAttr="2" ><a href="${url}/admin/system/fireproofStructure.lime">내화구조관리</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="40" mnDepthAttr="2" ><a href="${url}/admin/system/qmsStastics.lime">QMS 집계</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="44" mnDepthAttr="2" ><a href="${url}/admin/system/qmsRawStastics.lime">QMS 발급대장</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="47" mnDepthAttr="2" ><a href="${url}/admin/system/qmsItemManagement.lime">QMS 품목관리</a></li>
				</ul>
			</li>
			<li class="droplink left1MenuClass" style="display:none;" mnSeqAttr="15" mnDepthAttr="1">
				<a class="waves-effect waves-button"><span class="menu-icon glyphicon glyphicon-file"></span><p>리포트</p><span class="arrow"></span></a>
				<ul class="sub-menu">
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="16" mnDepthAttr="2"><a href="${url}/admin/report/deliveryReport.lime">납품확인서</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="17" mnDepthAttr="2" ><a href="${url}/admin/report/factReport.lime">거래사실확인서</a></li>
					<%-- <li class="left2MenuClass" style="display:none;" mnSeqAttr="18" mnDepthAttr="2" ><a href="${url}/admin/report/bondReport.lime">채권확인서/잔액확인서</a></li> --%>
				</ul>
			</li>
			
			<li class="droplink left1MenuClass" style="display:none;" mnSeqAttr="19" mnDepthAttr="1">
				<a class="waves-effect waves-button"><span class="menu-icon glyphicon glyphicon-share"></span><p>정보공유</p><span class="arrow"></span></a>
				<ul class="sub-menu">
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="20" mnDepthAttr="2"><a href="${url}/admin/board/noticeList.lime">공지사항</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="21" mnDepthAttr="2" ><a href="${url}/admin/board/faqList.lime">FAQ</a></li>
					<%-- <li class="left2MenuClass" style="display:none;" mnSeqAttr="21" mnDepthAttr="2" ><a href="${url}/admin/board/QnAList.lime">QnA</a></li> --%>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="22" mnDepthAttr="2" ><a href="${url}/admin/board/referenceList.lime">자료실</a></li>
					<li class="left2MenuClass"  mnSeqAttr="45" mnDepthAttr="2" ><a href="${url}/admin/board/sampleList.lime">샘플요청</a></li>
					<!-- 샘플 요청 mnSeqAttr 일부러 맨 끝으로함 ,, style="display:none; 가 풀리지 않아서 일단 지움.    45가 아닌 37로 햇을때는 문저 없었는데, 데이터베이스에 맞추려고 45로 하니까 style display none 이 계속 떠있는 현상-->
					
					
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="23" mnDepthAttr="2" ><a href="${url}/admin/board/tosConfigList.lime">약관관리</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="24" mnDepthAttr="2" ><a href="${url}/admin/board/scheduleList.lime">월간일정</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="25" mnDepthAttr="2" ><a href="${url}/admin/board/popupList.lime">팝업관리</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="26" mnDepthAttr="2" ><a href="${url}/admin/board/bannerList.lime">배너관리</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="48" mnDepthAttr="2" ><a href="${url}/admin/board/chatFeedback.lime">채팅 피드백</a></li>
				</ul>
			</li>
			
			<li class="droplink left1MenuClass" style="display:none;" mnSeqAttr="27" mnDepthAttr="1">
				<a class="waves-effect waves-button"><span class="menu-icon glyphicon glyphicon-check"></span><p>품목관리</p><span class="arrow"></span></a>
				<ul class="sub-menu">
					<%-- <li class="left2MenuClass" style="display:none;" mnSeqAttr="28" mnDepthAttr="2"><a href="${url}/admin/item/categoryList.lime">품목분류</a></li> --%>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="29" mnDepthAttr="2" ><a href="${url}/admin/item/itemList.lime">품목현황</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="30" mnDepthAttr="2" ><a href="${url}/admin/item/itemEditExcel.lime">일괄관리</a></li>
					<!-- 쿼테이션 번호 검증 기능 도입을 위한 쿼테이션 번호 및 품목 코드 일괄 저장 화면 추가 2025-05-30 ijy -->
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="49" mnDepthAttr="2" ><a href="${url}/admin/quotation/itemEditQuotationListExcel.lime">쿼테이션관리</a></li>
				</ul>
			</li>
			
			<li class="droplink left1MenuClass" style="display:none;" mnSeqAttr="31" mnDepthAttr="1" >
				<a class="waves-effect waves-button"><span class="menu-icon glyphicon glyphicon-gift"></span><p>프로모션</p><span class="arrow"></span></a>
				<ul class="sub-menu">
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="32" mnDepthAttr="2" ><a href="${url}/admin/promotion/promotionList.lime">이벤트현황</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="33" mnDepthAttr="2" ><a href="${url}/admin/promotion/promotionAdd.lime">이벤트등록</a></li>
				</ul>
			</li>
			
			<li class="droplink left1MenuClass" style="display:none;" mnSeqAttr="34" mnDepthAttr="1" >
				<a class="waves-effect waves-button"><span class="menu-icon glyphicon glyphicon-user"></span><p>마이페이지</p><span class="arrow"></span></a>
				<ul class="sub-menu">
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="35" mnDepthAttr="2" ><a href="${url}/admin/mypage/csSalesUserList.lime">조직설정</a></li>
					<li class="left2MenuClass" style="display:none;" mnSeqAttr="36" mnDepthAttr="2" ><a href="${url}/admin/mypage/myInformationView.lime">나의정보</a></li>
				</ul>
			</li>
			
			<!-- ### END. ### -->
			
		</ul>
	</div>
	<!-- //Page Sidebar Inner -->
</div>