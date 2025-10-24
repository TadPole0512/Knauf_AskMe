<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/admin/commonimport.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/admin/commonhead.jsp" %>

<script src="${url}/include/js/common/dropzone/dropzone.min.js"></script>
<link href="${url}/include/js/common/dropzone/dropzone.min.css" rel="stylesheet" type="text/css" />

<script type="text/javascript">
$(function(){
	//쿼테이션(CPQ) 번호 검증 기능 도입 및 관련 엑셀 업로드 기능 개발
});

//Dropzone
$(document).ready(function() {
	Dropzone.autoDiscover = false;
	
	$("#frm").dropzone({
		url: "./updateItemQuotationListExcelAjax.lime",
		params: {
			headerList : "Sales Document,Material"
		},
		maxFiles: 1,
		acceptedFiles: ".xls,.XLS,.xlsx,.XLSX",
		maxFilesize: 30,
		
		init: function() {
			// maxFiles 카운터를 초과하면 경고창
			this.on("maxfilesexceeded", function(data) {
				$('#ajax_indicator').fadeOut();
				
				this.removeFile(data);
				alert('최대 업로드 파일 수는 1개 입니다.');
			});

			// 등록시 바로 처리하기 위한 컨펌창
			this.on("addedfile", function(data) {
				$('#ajax_indicator').show().fadeIn('fast');
				
				if (!confirm('쿼테이션 번호 및 품목 일괄 등록을 진행 하시겠습니까?')) {
					$('#ajax_indicator').fadeOut();
					this.removeFile(data);
				}
			});
			
			// 성공시
			this.on("success", function(data, response) {
				var result;
				var json = response;

				if('object' === typeof json) result = json;
				else result = JSON.parse(json);
				
				var resCode = result.RES_CODE;
				var resMsg = result.RES_MSG;
				
				$('#resultContentPId').empty();
				$('#resultContentPId').show();
				// if('0000' != resCode) $('#resultContentPId').append('ERROR MASSAGE<br />');
				if('0000' != resCode) $('#resultContentPId').append('MASSAGE<br />');
				$('#resultContentPId').append('<span style="white-space: pre;">'+resMsg+'</span>');
				
				$('#ajax_indicator').fadeOut();
				this.removeFile(data);
			});
			
			// 에러시
			this.on("error", function(data, response, xhr) {
				console.log('data = '+data);
			    $('#resultContentPId').show();
				$('#resultContentPId').html('ERROR');
				
				$('#ajax_indicator').fadeOut();
				this.removeFile(data);
			});
		},
	});
});

// 샘플파일다운로드
function fileDown(){
	var fileFormHtml = '';
	fileFormHtml += '<form name="frm_filedown" method="post" action="${url}/admin/base/sampleFileDown.lime">';
	fileFormHtml += '	<input type="hidden" name="r_filename" value="QuotationsListExcel.xlsx" />';
	fileFormHtml += '</form>';
	fileFormHtml += '<iframe name="fileDownLoadIf" style="display:none;"></iframe>';
	$.download('frm_filedown', fileFormHtml); // common.js 위치.
}

</script>
</head>

<body class="page-header-fixed compact-menu">
	<div id="ajax_indicator" style="display:none;">
	    <p style="position: absolute; top: 50%; left: 50%; margin: -110px 0 0 -110px;">
	        <img src="${url}/include/images/common/loadingbar.gif" />
	    </p>
	</div>

	<!-- Page Content -->
	<main class="page-content content-wrap">
	
		<%@ include file="/WEB-INF/views/include/admin/header.jsp" %>
		<%@ include file="/WEB-INF/views/include/admin/left.jsp" %>
		
		<form name="frm_excel" method="post"></form>
		
		<!-- Page Inner -->
		<div class="page-inner">
			<div class="page-title">
				<h3>
					쿼테이션품목일괄관리
					<div class="page-right">
						<button type="button" class="btn btn-line f-black" title="목록" onclick="location.href ='${url}/admin/item/itemList.lime'"><i class="fa fa-list-ul"></i><em>목록</em></button>
					</div>
				</h3>
			</div>
			
			<!-- Main Wrapper -->
			<div id="main-wrapper">
				<!-- Row -->
				<div class="row">
					<div class="col-md-12">
						<div class="panel panel-white">
							<div class="panel-body">
								<h5 class="table-title no-title"></h5>
								<div class="btnList writeObjectClass">
									<button type="button" class="btn btn-info" onclick="fileDown();">샘플파일 다운로드</button>
								</div>
								
								<form class="dropzone pull-left full-width" name="frm" id="frm" enctype="multipart/form-data">
									<div class="fallback">
										<input type="file" class="dropify" />
										<input type="button" class="dropify-btn btn-github hide" value="파일업로드" onclick="fileUpload();" /> <!-- IE9 -->
									</div>
								</form>
								
								<p class="dropzone-white pull-left m-t-lg full-width" id="resultContentPId" style="display: none;">
									<!-- 
									INSERT MASSAGE<br />
									<span>success</span>
									 -->
								</p>
							</div>
						</div>
					</div>
				</div>
				<!-- //Row -->
			</div>
			<!-- //Main Wrapper -->
			
			<%@ include file="/WEB-INF/views/include/admin/footer.jsp" %>
			
		</div>
		<!-- //Page Inner -->
	</main>
	<!-- //Page Content -->
	
</body>

</html>