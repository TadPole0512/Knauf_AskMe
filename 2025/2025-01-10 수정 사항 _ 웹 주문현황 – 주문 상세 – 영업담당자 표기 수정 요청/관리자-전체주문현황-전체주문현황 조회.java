

/* ***************************************************************************************************************************************** */
/* *********** 전체주문현황 > 전체주문현황 : https://eordertest.knaufapac.kr/eorder/admin/order/salesOrderList.lime *********** */

/* *********** /admin/base/getCategoryListAjax.lime *********** */

/* *********** /admin/base/getCategoryListAjax.lime *********** */

/* ***************************************************************************************************************************************** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\webapp\WEB-INF\views\admin\order\salesOrderList.jsp *********** */
;


170	$(function(){
		getGridList();
	});


248	function getGridList(){
		// grid init
		var searchData = getSearchData();
		$("#gridList").jqGrid({
			url: "${url}/admin/order/getSalesOrderListAjax.lime",
			//editurl: 'clientArray', //사용x
			datatype: "json",
			mtype: 'POST',
			postData: searchData,
			colModel: updateComModel,
			height: '360px',
			autowidth: false,
			rowNum : 10,
			rowList : ['10','30','50','100'],
			multiselect: true,
			rownumbers: true,
			pagination: true,
			pager: "#pager",
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
			sortorder: 'desc',
			jsonReader : {
				root : 'list'
			},
			loadComplete: function(data){
				$('#listTotalCountSpanId').html(addComma(data.listTotalCount));
				$('.ui-pg-input').val(data.page);
			},
			gridComplete: function(){
				// 조회된 데이터가 없을때
				var grid = $('#gridList');
				var emptyText = grid.getGridParam('emptyDataText'); //NO Data Text 가져오기.
				var container = grid.parents('.ui-jqgrid-view'); //Find the Grid`s Container
				if(0 == grid.getGridParam('records')){
					//container.find('.ui-jqgrid-hdiv, .ui-jqgrid-bdiv').hide(); //Hide The Column Headers And The Cells Below
					//container.find('.ui-jqgrid-titlebar').after(''+emptyText+'');
				}
			},
			emptyDataText: '조회된 데이터가 없습니다.', //NO Data Text
			//onSelectRow: editRow,
		});
	}



629	function getSearchData(){
		if($('input[name="rl_salesrepnm"]').val() === '') {
			$('input[name="rl_salesrepid"]').val('');
		}

		var r_ordersdt = $('input[name="r_ordersdt"]').val();
		var r_orderedt = $('input[name="r_orderedt"]').val();
		var rl_orderno = $('input[name="rl_orderno"]').val();
		var r_actualshipsdt = $('input[name="r_actualshipsdt"]').val();
		var r_actualshipedt = $('input[name="r_actualshipedt"]').val();
		var r_requestsdt = $('input[name="r_requestsdt"]').val();
		var r_requestedt = $('input[name="r_requestedt"]').val();
		var rl_custpo = $('input[name="rl_custpo"]').val();
		var rl_add1 = $('input[name="rl_add1"]').val();
		var rl_itemdesc = $('input[name="rl_itemdesc"]').val();
		//var rl_receiver = $('input[name="rl_receiver"]').val();
		var r_custcd = $('input[name="r_custcd"]').val();
		//var r_shiptocd = $('select[name="r_shiptocd"] option:selected').val();
		var rl_shiptonm = $('select[name="r_shiptocd"] option:selected').val();
		var rl_salesrepnm = $('input[name="rl_salesrepnm"]').val();
		var rl_salesrepid = $('input[name="rl_salesrepid"]').val();
		var rl_ordertp = $('input[name="rl_ordertp"]').val();

		var ri_status2 = '';
		//if($('input:checkbox[name="vi_status2"]').length != $('input:checkbox[name="vi_status2"]:checked').length){ // 상태값을 전체 개수와 선택된 개수가 동일하면 빈값으로 세팅.
			$('input:checkbox[name="vi_status2"]:checked').each(function(i,e) {
				if($(e).val() !== '') {
					if(i === 0) ri_status2 = $(e).val();
					else ri_status2 += ','+$(e).val();
				}
			});
		//}

		$('input[name="ri_status2"]').val(ri_status2); // Use For ExcelDownload.

		var sData = {
			r_ordersdt : r_ordersdt
			, r_orderedt : r_orderedt
			, rl_orderno : rl_orderno
			, r_actualshipsdt : r_actualshipsdt
			, r_actualshipedt : r_actualshipedt
			, r_requestsdt : r_requestsdt
			, r_requestedt : r_requestedt
			, rl_custpo : rl_custpo
			, rl_add1 : rl_add1
			, rl_itemdesc : rl_itemdesc
			//, rl_receiver : rl_receiver
			, r_custcd : r_custcd
			//, r_shiptocd : r_shiptocd
			, rl_shiptonm : rl_shiptonm
			, rl_salesrepnm : rl_salesrepnm
			, rl_salesrepid : rl_salesrepid
			, ri_status2 : ri_status2
			, rl_ordertp : rl_ordertp
		};
		//debugger;
		return sData;
	}



688	// 조회
	function dataSearch() {
		//$('#detailListDivId').hide();

		var searchData = getSearchData();
		$('#gridList').setGridParam({
			postData : searchData
		}).trigger("reloadGrid");
	}




1027	<button type="button" class="btn btn-line f-black" title="검색" onclick="dataSearch();"><i class="fa fa-search"></i><em>검색</em></button>


/* *********** location	C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java *********** */
;

386    /**
     * 전체주문현황 리스트 폼.
     * @작성일 : 2020. 4. 25.
     * @작성자 : kkyu
     */
    @GetMapping(value="salesOrderList")
    public String salesOrderList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        //model.addAttribute("salesOrderStatus", StatusUtil.SALESORDER.getMap()); // 출하상태 Map형태로 가져오기.
        //model.addAttribute("salesOrderStatusToJson", StatusUtil.SALESORDER.getMapToJson()); // 출하상태 JSON형태로 가져오기.
        model.addAttribute("salesOrderStatusList", StatusUtil.SALESORDER.getList()); // 출하상태 List<Map>형태로 가져오기.

        // 주문일 세팅.
        String toDay = Converter.dateToStr("yyyy-MM-dd",new Date());
        String fromDay = Converter.dateToStr("yyyy-MM-dd");
        model.addAttribute("ordersdt", toDay);
        model.addAttribute("orderedt", fromDay);

        return "admin/order/salesOrderList";
    }





/* ***************************************************************************************************************************************** */
/* *********** /admin/order/getSalesOrderListAjax.lime *********** */



/* *********** location	C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java *********** */
;


406    /**
     * 전체주문현황 리스트 가져오기 Ajax.
     * @작성일 : 2020. 4. 25.
     * @작성자 : kkyu
     */
    @ResponseBody
    @PostMapping(value="getSalesOrderListAjax")
    public Object getSalesOrderListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");

        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);

        return orderSvc.getSalesOrderList(params, req, loginDto);
    }




/* *********** location	C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */
;

1320    /**
     * 전체주문현황 리스트 가져오기. O_SALESORDER.
     * @작성일 : 2020. 4. 24.
     * @작성자 : kkyu
     * @param where : admin=관리자, excel=관리자 엑셀, front=거래처,납품처, frontexcel=거래처,납품처 엑셀.
     */
    public Map<String, Object> getSalesOrderList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();

        String where = Converter.toStr(params.get("where"));

        String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
        String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
        String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
        String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
        String r_requestsdt = Converter.toStr(params.get("r_requestsdt")); // 납품요청일 검색 시작일.
        String r_requestedt = Converter.toStr(params.get("r_requestedt")); // 납품요청일; 검색 종료일.
        if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");
        if(!StringUtils.equals("", r_requestsdt)) r_requestsdt = r_requestsdt.replaceAll("-", "");
        if(!StringUtils.equals("", r_requestedt)) r_requestedt = r_requestedt.replaceAll("-", "");
        params.put("r_ordersdt", r_ordersdt);
        params.put("r_orderedt", r_orderedt);
        params.put("r_actualshipsdt", r_actualshipsdt);
        params.put("r_actualshipedt", r_actualshipedt);
        params.put("r_requestsdt", r_requestsdt);
        params.put("r_requestedt", r_requestedt);

        // 상태값 검색조건 재정의.
        String statuss1 = "";
        String wherebody_status = "";
        String ri_status2 = Converter.toStr(params.get("ri_status2")); // 상태값으로  ,로 구분.
        boolean bShip = false;
        boolean bDeliv = false;
        boolean bCard = false;
        int cntDType = 0;
        if(!StringUtils.equals("", ri_status2)) {
            String[] ri_status2arr = ri_status2.split(",", -1);
            //String today = Converter.dateToStr("yyyyMMdd");
            //String statuss2 = ""; // for 출하완료
            //String statuss3 = ""; // for 배송완료
            cntDType = ri_status2arr.length;
            if( (cntDType == 1) && (ri_status2arr[0].equalsIgnoreCase("525")) ) {
            	bCard = true;
            } else {
	            for(String status2 : ri_status2arr) {
	                if(560 > Integer.parseInt(status2)) {
	                    if(StringUtils.equals("", statuss1)) {
	                        statuss1 += status2;
	                    }else {
	                        statuss1 += ","+status2;
	                    }
	                }
	                // 출하완료 560.
	                // >>> 출하완료는 텍스트로 검색한다.
	                /*else if(560 == Integer.parseInt(status2)){
	                    statuss2 += "(STATUS_DESC = '출하완료')";
	                    //statuss2 += "(STATUS2 >= "+status2+" AND REQUEST_DT <= "+today+")";
	                }
	                // 배송완료 800 > 800은 정의되지 않은 상태값으로 임의로 정의한값이다. > 출하완료 560 이상 & 날짜(요청일자) 지남 > 보랄측에서 새롭게 정의한 사항.
	                // >>> 다시 요청하기로... 배송완료는 텍스르로 검색한다.
	                else {
	                    statuss3 += "(STATUS_DESC = '배송완료')";
	                    //statuss3 += "(STATUS2 >= "+status2+" AND REQUEST_DT > "+today+")";
	                }*/
	                else {
	                	if(!statuss1.contains("560")) {
	                    	if(!StringUtils.equals("", statuss1)) {
	                            statuss1 += ",";
	                        }

	                		statuss1 += "560,580,620";
	                	}

	                	if(status2.contains("560")) {
	                		bShip = true;
	                	} else if(status2.contains("800")) {
	                		bDeliv = true;
	                	}
	                }
	            }
            }
        }

        if(StringUtils.equals("", statuss1)) {
        	if( bCard ) {
        		wherebody_status = "AND (SO.HOLD_CODE = 'C1' AND SO.STATUS2 <> '999')";
        	} else {
        		wherebody_status += "NOT (SO.STATUS1 = '980')";
        		//wherebody_status += "AND SO.STATUS2 = '0'";
        	}
        } else {
        	wherebody_status += "SO.STATUS2 IN ("+statuss1+")";
        	if( (cntDType==1) && bShip ) {
        	//  2024-10-21 hsg MS-SQL에서 TO_CHAR 함수를 사용할 수 없어 FORMAT으로 변경
//        		wherebody_status += " AND ((SO.STATUS1=580 AND SO.STATUS2=620) AND SO.REQUEST_DT >= TO_CHAR(SYSDATE, 'yyyymmdd'))";
        		wherebody_status += " AND ((SO.STATUS1=580 AND SO.STATUS2=620) AND SO.REQUEST_DT >= FORMAT(GETDATE(), 'yyyymmdd'))";
        	} else if( (cntDType==1) && bDeliv ) {
        	//  2024-10-21 hsg MS-SQL에서 TO_CHAR 함수를 사용할 수 없어 FORMAT으로 변경
//        		wherebody_status += " AND ((SO.STATUS1=580 AND SO.STATUS2=620) AND SO.REQUEST_DT < TO_CHAR(SYSDATE, 'yyyymmdd'))";
        		wherebody_status += " AND ((SO.STATUS1=580 AND SO.STATUS2=620) AND SO.REQUEST_DT < FORMAT(GETDATE(), 'yyyymmdd'))";
        	}
        }

        wherebody_status += " AND SO.ORDERTY <> 'KL'";

        System.out.println("wherebody_status : s" +  wherebody_status);
        params.put("wherebody_status", wherebody_status);

        int totalCnt = salesOrderDao.cnt(params);

        Pager pager = new Pager();
        pager.gridSetInfo(totalCnt, params, req);
        resMap.put("total", Converter.toInt(params.get("totpage")));
        resMap.put("listTotalCount", totalCnt);

        // Start. Define Only For Form-Paging.
        resMap.put("startnumber", params.get("startnumber"));
        resMap.put("r_page", params.get("r_page"));
        resMap.put("startpage", params.get("startpage"));
        resMap.put("endpage", params.get("endpage"));
        resMap.put("r_limitrow", params.get("r_limitrow"));
        // End.

        String r_orderby = "";
        String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
        String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
        r_orderby = sidx + " " + sord;
        //  2024-10-16 hsg 별칭 오류가 나서 수정. SO -> XX)
        if(StringUtils.equals("", sidx)) { r_orderby = "XX.ORDERNO DESC, XX.LINE_NO ASC"; } //디폴트 지정

        params.put("r_orderby", r_orderby);


        // 엑셀 다운로드.
        if(StringUtils.equals("excel", where) || StringUtils.equals("frontexcel", where) || StringUtils.equals("orderadd", where)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }

        List<Map<String, Object>> list = this.getSalesOrderList(params);
        resMap.put("list", list);
        resMap.put("data", list);
        resMap.put("page", params.get("r_page"));

        resMap.put("where", where);

        return resMap;
    }




/* ***************************************************************************************************************************************** */
/* *********** int totalCnt = salesOrderDao.cnt(params); *********** */




/* *********** location	C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\SalesOrderDao.java *********** */
;

20	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_salesorder.cnt", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_salesorder.xml *********** */
;

38	<select id="cnt" parameterType="hashmap" resultType="int">
		SELECT COUNT(*)
		FROM O_SALESORDER SO
		<where>
			<if test="wherebody_status != null and wherebody_status != '' ">
				${wherebody_status}
			</if>
			<if test="r_ordersdt != null and r_ordersdt != '' ">AND SO.ORDER_DT <![CDATA[>=]]> #{r_ordersdt}</if>
			<if test="r_orderedt != null and r_orderedt != '' ">AND SO.ORDER_DT <![CDATA[<=]]> #{r_orderedt}</if>
			<if test="r_actualshipsdt != null and r_actualshipsdt != '' ">AND SO.ACTUAL_SHIP_DT <![CDATA[>=]]> #{r_actualshipsdt}</if>
			<if test="r_actualshipedt != null and r_actualshipedt != '' ">AND SO.ACTUAL_SHIP_DT <![CDATA[<=]]> #{r_actualshipedt}</if>
			<if test="r_requestsdt != null and r_requestsdt != '' ">AND SO.REQUEST_DT <![CDATA[>=]]> #{r_requestsdt}</if>
			<if test="r_requestedt != null and r_requestedt != '' ">AND SO.REQUEST_DT <![CDATA[<=]]> #{r_requestedt}</if>
			<if test="r_orderno != null and r_orderno != '' ">AND SO.ORDERNO = #{r_orderno}</if>
			<if test="rl_orderno != null and rl_orderno != '' ">AND SO.ORDERNO LIKE '%' + #{rl_orderno} + '%'</if> <!-- 오더번호 -->
			<if test="rl_custpo != null and rl_custpo != '' ">AND SO.CUST_PO LIKE '%' + #{rl_custpo} + '%'</if> <!-- 고객주문번호 -->
			<if test="r_truckno != null and r_truckno != '' ">AND SO.TRUCK_NO = #{r_truckno}</if>
			<if test="rl_itemdesc != null and rl_itemdesc != '' ">AND SO.ITEM_DESC LIKE '%' + #{rl_itemdesc} + '%'</if>
			<if test="rl_receiver != null and rl_receiver != '' ">AND SO.RECEIVER LIKE '%' + #{rl_receiver} + '%'</if>
			<if test="r_custcd != null and r_custcd != '' ">AND SO.CUST_CD = #{r_custcd}</if>
			<if test="r_shiptocd != null and r_shiptocd != '' ">AND SO.SHIPTO_CD = #{r_shiptocd}</if>
			<if test="rl_shiptocd != null and rl_shiptocd != '' ">AND SO.SHIPTO_CD LIKE '%' + #{rl_shiptocd} + '%'</if>
			<if test="rl_shiptonm != null and rl_shiptonm != '' ">AND SO.SHIPTO_NM LIKE '%' + #{rl_shiptonm} + '%'</if>
			<if test="rl_add1 != null and rl_add1 != '' ">AND (SO.ADD1 + SO.ADD2) LIKE '%' + #{rl_add1} + '%'</if>
			<if test="rl_salesrepnm != null and rl_salesrepnm != '' ">
				AND SO.CUST_CD IN (SELECT CUST_CD FROM O_CUSTOMER WHERE SALESREP_NM LIKE '%' + #{rl_salesrepnm} + '%')
			</if>

			<if test="r_insertsdt != null and r_insertsdt != '' ">AND SO.INSERT_DT <![CDATA[>=]]> #{r_insertsdt}</if>
			<if test="r_insertedt != null and r_insertedt != '' ">AND SO.INSERT_DT <![CDATA[<=]]> #{r_insertedt}</if>
			<!-- 관리자 권한에 따른 조건절 -->
 			<if test="r_adminauthority != null and r_adminauthority != '' ">
 				<if test='"AD".equals(r_adminauthority)'>
 				</if>
 				<if test='"CS".equals(r_adminauthority)'>
 					AND SO.SALESREP_CD IN (SELECT SALESUSERID FROM O_CSSALESMAP WHERE CSUSERID = #{r_adminuserid})
 				</if>
 				<if test='"SH".equals(r_adminauthority) or "SM".equals(r_adminauthority) or "SR".equals(r_adminauthority)'>
 					<if test='"SH".equals(r_adminauthority)'>
 						AND SO.SALESREP_CD IN (SELECT USERID FROM O_USER WHERE USER_CATE2 = #{r_adminuserid})
 					</if>
 					<if test='"SM".equals(r_adminauthority)'>
 						AND SO.SALESREP_CD IN (SELECT USERID FROM O_USER WHERE USER_CATE3 = #{r_adminuserid})
 					</if>
 					<if test='"SR".equals(r_adminauthority)'>
 						AND SO.SALESREP_CD = #{r_adminuserid}
 					</if>
 				</if>
 				<if test='"MK".equals(r_adminauthority)'>

 				</if>
 			</if>
 			<!-- End. -->
		</where>
	</select>





/* ***************************************************************************************************************************************** */
/* *********** List<Map<String, Object>> list = this.getSalesOrderList(params); *********** */



/* *********** location	C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */
;

196    /**
     * Get O_SALESORDER List.
     * @작성일 : 2020. 4. 24.
     * @작성자 : kkyu
     */
    public List<Map<String, Object>> getSalesOrderList(Map<String, Object> svcMap){
        return salesOrderDao.list(svcMap);
    }




/* *********** location	C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\SalesOrderDao.java *********** */
;

24	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_salesorder.list", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_salesorder.xml *********** */


94	<select id="list" parameterType="hashmap" resultType="hashmap">
		SELECT * FROM (SELECT
			ROW_NUMBER() OVER(
			<choose>
				<when test = " r_orderby != null and r_orderby != '' ">ORDER BY ${r_orderby}</when>
				<otherwise>ORDER BY (SELECT 1)</otherwise>
			</choose>
			) AS ROWNUM
			, XX.* FROM (
		SELECT
			SO.ORDERNO, SO.ORDERTY, SO.LINE_NO, SO.ORDER_DT, SO.REQUEST_DT, SO.ACTUAL_SHIP_DT, SO.CUST_CD, SO.CUST_NM, SO.SHIPTO_CD, SO.SHIPTO_NM
        	, SO.ADD1, SO.ADD2, SO.ADD4, SO.ZIP_CD, SO.COMPANY, SO.PLANT_CD, SO.PLANT_DESC, SO.ITEM_CD, SO.ITEM_DESC, SO.ORDER_QTY, SO.UNIT, (CAST(SO.PRIMARY_QTY AS FLOAT) * CAST(OH.X AS FLOAT) / OH.Y) AS PRIMARY_QTY
        	, OH.HEBE_UM AS UNIT1, SO.SECOND_QTY, SO.UNIT2, SO.WEIGHT, SO.WEIGHT_UNIT, SO.AMOUNT, SO.STATUS1, SO.STATUS2, SO.STATUS_DESC, SO.DRIVER_PHONE
        	<!-- 2024-10-15 HSG 주석 처리 후 아래 코드 삽입 , SO.SHIPMENT_CD_3PL, SO.CUST_PO, SO.ORDER_TAKER, SO.HOLD_CODE, SO.TRUCK_NO, SO.SALESREP_CD, SO.SALESREP_NM, SO.TEAM_CD, SO.TEAM_NM, SO.BUILDING_TYPE, SO.INSERT_DT-->
        	/*, SO.SHIPMENT_CD_3PL*/, SO.CUST_PO, SO.ORDER_TAKER, SO.HOLD_CODE, SO.TRUCK_NO
			, SO.SALESREP_CD, SO.SALESREP_NM, SO.TEAM_CD, SO.TEAM_NM, SO.BUILDING_TYPE, SO.INSERT_DT
			<!-- 2025-01-13 hsg Belly to Belly Suplex : 주석처리 , SO.SALESREP_CD, SO.SALESREP_NM -->, SO.TEAM_CD, SO.TEAM_NM, SO.BUILDING_TYPE, SO.INSERT_DT
			, SO.PRICE, SO.ADD3, SO.REQUEST_TIME, SO.DUMMY
			 <!-- 2025-01-13 hsg Belly to Belly Suplex : SALESREP_CD와 SALESREP_NM을 구할 때 웹주문현황과 동일하게 가져오도록 수정 -->
			,	(
					SELECT	US_SALES.USERID
					  FROM	O_CUSTOMER CU
							LEFT OUTER JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
					 WHERE	SO.CUST_CD = CU.CUST_CD
				) AS SALESREP_CD
			,	(
					SELECT	US_SALES.USER_NM
					  FROM	O_CUSTOMER CU
							LEFT OUTER JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
					 WHERE	SO.CUST_CD = CU.CUST_CD
				) AS SALESREP_NM
		FROM O_SALESORDER SO
			LEFT JOIN O_ITEM_HEBE OH ON OH.ITEM_CD = SO.ITEM_CD
			LEFT JOIN O_ITEM_MFG MFG ON SO.ITEM_CD = MFG.ITEM_CD
		<where>
			<if test="wherebody_status != null and wherebody_status != '' ">
				${wherebody_status}
			</if>
			<if test="r_ordersdt != null and r_ordersdt != '' ">AND SO.ORDER_DT <![CDATA[>=]]> #{r_ordersdt}</if>
			<if test="r_orderedt != null and r_orderedt != '' ">AND SO.ORDER_DT <![CDATA[<=]]> #{r_orderedt}</if>
			<if test="r_actualshipsdt != null and r_actualshipsdt != '' ">AND SO.ACTUAL_SHIP_DT <![CDATA[>=]]> #{r_actualshipsdt}</if>
			<if test="r_actualshipedt != null and r_actualshipedt != '' ">AND SO.ACTUAL_SHIP_DT <![CDATA[<=]]> #{r_actualshipedt}</if>
			<if test="r_requestsdt != null and r_requestsdt != '' ">AND SO.REQUEST_DT <![CDATA[>=]]> #{r_requestsdt}</if>
			<if test="r_requestedt != null and r_requestedt != '' ">AND SO.REQUEST_DT <![CDATA[<=]]> #{r_requestedt}</if>
			<if test="r_orderno != null and r_orderno != '' ">AND SO.ORDERNO = #{r_orderno}</if>
			<if test="rl_orderno != null and rl_orderno != '' ">AND SO.ORDERNO LIKE '%' + #{rl_orderno} + '%'</if> <!-- 오더번호 -->
			<if test="rl_custpo != null and rl_custpo != '' ">AND SO.CUST_PO LIKE '%' + #{rl_custpo} + '%'</if> <!-- 고객주문번호 -->
			<if test="rl_itemdesc != null and rl_itemdesc != '' ">AND SO.ITEM_DESC LIKE '%' + #{rl_itemdesc} + '%'</if> <!-- 품목명 -->
			<if test="r_salescd1nm != null and r_salescd1nm != '' ">
				AND SO.ITEM_CD IN ( SELECT ITEM_CD FROM O_ITEM_NEW WHERE SALES_CD1_NM = #{r_salescd1nm} )
			</if> <!-- 품목분류 코드1 -->
			<if test="r_salescd2nm != null and r_salescd2nm != '' ">
				AND SO.ITEM_CD IN ( SELECT ITEM_CD FROM O_ITEM_NEW WHERE SALES_CD2_NM = #{r_salescd2nm} )
			</if> <!-- 품목분류 코드2 -->
			<if test="r_salescd3nm != null and r_salescd3nm != '' ">
				AND SO.ITEM_CD IN ( SELECT ITEM_CD FROM O_ITEM_NEW WHERE SALES_CD3_NM = #{r_salescd3nm} )
			</if> <!-- 품목분류 코드3 -->
			<if test="rl_receiver != null and rl_receiver != '' ">AND SO.RECEIVER LIKE '%' + #{rl_receiver} + '%'</if>
			<if test="r_custcd != null and r_custcd != '' ">AND SO.CUST_CD = #{r_custcd}</if>
			<if test="r_truckno != null and r_truckno != '' ">AND SO.TRUCK_NO = #{r_truckno}</if>
			<if test="rl_itemdesc != null and rl_itemdesc != '' ">AND SO.ITEM_DESC LIKE '%' + #{rl_itemdesc} + '%'</if>
			<if test="rl_receiver != null and rl_receiver != '' ">AND SO.RECEIVER LIKE '%' + #{rl_receiver} + '%'</if>
			<if test="r_custcd != null and r_custcd != '' ">AND SO.CUST_CD = #{r_custcd}</if>
			<if test="r_shiptocd != null and r_shiptocd != '' ">AND SO.SHIPTO_CD = #{r_shiptocd}</if>
			<if test="rl_shiptocd != null and rl_shiptocd != '' ">AND SO.SHIPTO_CD LIKE '%' + #{rl_shiptocd} + '%'</if>
			<if test="rl_shiptonm != null and rl_shiptonm != '' ">AND SO.SHIPTO_NM LIKE '%' + #{rl_shiptonm} + '%'</if>
			<if test="rl_salesrepnm != null and rl_salesrepnm != '' ">
				AND SO.CUST_CD IN (SELECT CUST_CD FROM O_CUSTOMER WHERE SALESREP_NM LIKE '%' + #{rl_salesrepnm} + '%')
			</if>
			<if test="rl_add1 != null and rl_add1 != '' ">AND (SO.ADD1 + SO.ADD2) LIKE '%' + #{rl_add1} + '%'</if>
			<if test="r_insertsdt != null and r_insertsdt != '' ">AND SO.INSERT_DT <![CDATA[>=]]> #{r_insertsdt}</if>
			<if test="r_insertedt != null and r_insertedt != '' ">AND SO.INSERT_DT <![CDATA[<=]]> #{r_insertedt}</if>
			<!-- 관리자 권한에 따른 조건절 -->
 			<if test="r_adminauthority != null and r_adminauthority != '' ">
 				<if test='"AD".equals(r_adminauthority)'>
 				</if>
 				<if test='"CS".equals(r_adminauthority)'>
 					AND SO.SALESREP_CD IN (SELECT SALESUSERID FROM O_CSSALESMAP WHERE CSUSERID = #{r_adminuserid})
 				</if>
 				<if test='"SH".equals(r_adminauthority) or "SM".equals(r_adminauthority) or "SR".equals(r_adminauthority)'>
 					<if test='"SH".equals(r_adminauthority)'>
 						AND SO.SALESREP_CD IN (SELECT USERID FROM O_USER WHERE USER_CATE2 = #{r_adminuserid})
 					</if>
 					<if test='"SM".equals(r_adminauthority)'>
 						AND SO.SALESREP_CD IN (SELECT USERID FROM O_USER WHERE USER_CATE3 = #{r_adminuserid})
 					</if>
 					<if test='"SR".equals(r_adminauthority)'>
 						AND SO.SALESREP_CD = #{r_adminuserid}
 					</if>
 				</if>
 				<if test='"MK".equals(r_adminauthority)'>

 				</if>
 			</if>
 			<!-- End. -->
		</where>

		) XX ) S
		<where>
			<if test="r_endrow != null and r_endrow != '' and r_startrow != null and r_startrow != ''" >
				ROWNUM BETWEEN #{r_startrow} AND #{r_endrow}
			</if>
		</where>
	</select>







































