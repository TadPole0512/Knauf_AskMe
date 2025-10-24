
/* ***************************************************************************************************************************************** */
/* *********** 엑셀다운로드 : http://localhost:8080/eorder/admin/order/salesOrderList.lime *********** */
/* ***************************************************************************************************************************************** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\webapp\WEB-INF\views\admin\order\salesOrderList.jsp *********** */
;

689	// 엑셀다운로드.
// jqgrid 검색 조건 증 체크박스 주의.
	function excelDown(obj) {
		$('#ajax_indicator').show().fadeIn('fast');
		var colSortStr = toStr(decodeURIComponent(getCookie(ckNameJqGrid)));
		if('' == colSortStr) colSortStr = defaultColumnOrder;
		
		var token = getFileToken('excel');
		$('form[name="frm"]').append('<input type="hidden" name="filetoken" value="'+token+'" />');
		$('form[name="frm"]').append('<input type="hidden" name="r_colsortstr" value="'+colSortStr+'" />');
		
		formPostSubmit('frm', '${url}/admin/order/salesOrderExcelDown.lime');
		$('form[name="frm"]').attr('action', '');

		$('input[name="filetoken"]').remove();
		$('input[name="r_colsortstr"]').remove();
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



/* ***************************************************************************************************************************************** */
/* *********** formPostSubmit('frm', '${url}/admin/order/salesOrderExcelDown.lime'); *********** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java *********** */
;

551    /**
     * 전체주문현황 리스트 > 엑셀다운로드
     * @작성일 : 2020. 4. 26.
     * @작성자 : an
     */
    @PostMapping(value="salesOrderExcelDown")
    public ModelAndView salesOrderExcelDown(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        // Start. 뷰단에서 넘어온 파일 쿠키 put.
        String fileToken = Converter.toStr(params.get("filetoken"));
        resMap.put("fileToken", fileToken);
        // End.
        
        params.put("where", "excel");
        resMap.putAll(orderSvc.getSalesOrderList(params, req, loginDto));
        return new ModelAndView(new SalesOrderExcel(), resMap);
    }































































