

/* ***************************************************************************************************************************************** */
/* *********** 납품처 리스트 : /admin/order/getShiptoListAjax.lime *********** */


/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java *********** */
;


1182	/**
	 * 납품처 리스트 가져오기 Ajax.
	 * @작성일 : 2020. 5. 30.
	 * @작성자 : kkyu
	 */
	@ResponseBody
	@PostMapping(value="/admin/order/getShiptoListAjax")
	public Object getShiptoListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("where", "admin");

		// 내부사용자 웹주문현황	> 별도 권한 설정.
		orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);

		return orderSvc.getShipToList(params, req, loginDto);
	}





/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */
;


3150	/**
	 * QMS 납품처 가져오기
	 * @작성일 : 2021. 5. 24.
	 * @작성자 : jsh
	 */
	public Map<String, Object> getShipToList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();

		String where = Converter.toStr(params.get("where"));

		String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
		String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
		String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
		String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.
		//String r_requestsdt = Converter.toStr(params.get("r_requestsdt")); // 납품요청일 검색 시작일.
		//String r_requestedt = Converter.toStr(params.get("r_requestedt")); // 납품요청일; 검색 종료일.
		if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
		if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
		if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
		if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");
		//if(!StringUtils.equals("", r_requestsdt)) r_requestsdt = r_requestsdt.replaceAll("-", "");
		//if(!StringUtils.equals("", r_requestedt)) r_requestedt = r_requestedt.replaceAll("-", "");

		String rl_salesrepnm = Converter.toStr(params.get("rl_salesrepnm")); // 영업사원
		String rl_orderno = Converter.toStr(params.get("rl_orderno")); // 어다반허
		String r_custcd = Converter.toStr(params.get("r_custcd")); // 거래처
		String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 납품처

		params.put("r_ordersdt", r_ordersdt);
		params.put("r_orderedt", r_orderedt);
		params.put("r_actualshipsdt", r_actualshipsdt);
		params.put("r_actualshipedt", r_actualshipedt);
		params.put("rl_salesrepnm", rl_salesrepnm);
		params.put("rl_orderno", rl_orderno);
		params.put("r_custcd", r_custcd);
		params.put("r_shiptocd", r_shiptocd);

		// QMS 상태 조회
		String wherebody_status = "";
		String qms_status  = params.get("qms_status") !=null?params.get("qms_status").toString():"";
		String qms_status2 = params.get("qms_status2")!=null?params.get("qms_status2").toString():"";
		String qms_status3 = params.get("qms_status3")!=null?params.get("qms_status3").toString():"";

		//QMS 사전입력 여부
		String qms_preyn   = params.get("qms_preyn")!=null?params.get("qms_preyn").toString():"";

		if (!qms_status.equals("ALL")) {
			// QMS 생성 미완료
			if(qms_status.equals("N")) {
				wherebody_status += "SF_GETQMSID(ORDERNO,LINE_NO) IS NULL";
			}
			// QMS 생성완료
			if(qms_status.equals("Y")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " OR ";
				}
				wherebody_status += "SF_GETQMSID(ORDERNO,LINE_NO) IS NOT NULL";
			}
		}

		if (!qms_status2.equals("ALL")) {
			// MAIL 발송 미완료
			if(qms_status2.equals("N")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "SF_GETMAILYN(ORDERNO,LINE_NO) = 'N' ";
			}

			// MAIL 발송완료
			if(qms_status2.equals("Y")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "SF_GETMAILYN(ORDERNO,LINE_NO) = 'Y' ";
			}
		}

		if (!qms_status3.equals("ALL")) {
			// QMS 회신 미완료
			if(qms_status3.equals("N")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "SF_GETFILEYN(ORDERNO,LINE_NO) = 'N' ";
			}

			// QMS 회신완료
			if(qms_status3.equals("Y")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "SF_GETFILEYN(ORDERNO,LINE_NO) = 'Y' ";
			}
		}

		if (!qms_preyn.equals("ALL")) {
			// QMS 사전입력
			if(qms_preyn.equals("Y")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "Sf_getpreorderyn(SO.cust_po) = 'Y' ";
			}

			// QMS 사후입력건
			if(qms_preyn.equals("N")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "Sf_getpreorderyn(SO.cust_po) = 'N' ";
			}
		}

		if(!qms_status.equals("ALL") || !qms_status2.equals("ALL") || !qms_status3.equals("ALL") || !qms_preyn.equals("ALL")) {
			params.put("wherebody_status", wherebody_status);
		}


		// 페이징 없이 End.

		String r_orderby = "";
		String sidx = Converter.toStr(params.get("sidx")); //정렬기준컬럼
		String sord = Converter.toStr(params.get("sord")); //내림차순,오름차순
		r_orderby = sidx + " " + sord;
	//	2024-10-16 hsg 별칭 오류가 나서 수정. SO -> XX)
		if(StringUtils.equals("", sidx)) { r_orderby = "XX.ORDERNO DESC, XX.CUST_PO DESC "; } //디폴트 지정

		params.put("r_orderby", r_orderby);

		List<Map<String, Object>> list = this.getQmsOrderShiptoList(params);
		System.out.println("======================qms list==============================");
		//System.out.println(list);
		resMap.put("list", list);
		resMap.put("data", list);

		resMap.put("where", where);

		return resMap;
	}



/* ***************************************************************************************************************************************** */
/* *********** Get 현장 가져오기 : List<Map<String, Object>> list = this.getQmsOrderShiptoList(params); *********** */


/* *********** /eorder/src/main/java/com/limenets/eorder/svc/OrderSvc.java *********** */
;


3141	/**
	 * Get 현장 가져오기
	 * @작성일 : 2021. 5. 24.
	 * @작성자 : jsh
	 */
	public List<Map<String, Object>> getQmsOrderShiptoList(Map<String, Object> svcMap){
		return qmsOrderDao.getShipToList(svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java *********** */
;


196	   public List<Map<String, Object>> getShipToList(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_qmsorder.getShipToList", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_qmsorder.xml *********** */
;

1279	<select id="getShipToList" parameterType="hashmap" resultType="hashmap">
	/* eorder.o_qmsorder.getShipToList */
		SELECT YY.SHIPTO_CD, YY.SHIPTO_NM FROM (SELECT KK.* FROM (
			SELECT
			ROW_NUMBER() OVER(
				<choose>
					<when test = " r_orderby != null and r_orderby != '' ">ORDER BY ${r_orderby}</when>
					<otherwise>ORDER BY (SELECT 1)</otherwise>
				</choose>
			) AS ROWNUM
			, XX.* FROM (SELECT ORDERNO,LINE_NO
			,dbo.SF_GETQMSID(ORDERNO,LINE_NO)AS QMS_ARR
			,dbo.SF_GETQMSID(ORDERNO,LINE_NO)AS QMS_ARR_TXT
			,dbo.SF_GETQMSQTY(ORDERNO,LINE_NO)AS QMS_ARR_QTY
			,dbo.SF_GETQMSSHIPTO(ORDERNO,LINE_NO)AS QMS_ARR_SHIPTO
			,dbo.SF_GETMAILYN(ORDERNO,LINE_NO) AS MAIL_YN
			,dbo.SF_GETFILEYN(ORDERNO,LINE_NO) AS FILE_YN
			,ITEM_CD,ORDERTY ,CUST_PO ,CUST_CD ,CUST_NM,
			CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ORDER_DT, 0,4), '-'), SUBSTRING(ORDER_DT, 5, 2)), '-'), SUBSTRING(ORDER_DT, 7, 2)) AS ORDER_DT,
			CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ACTUAL_SHIP_DT, 0,4), '-'), SUBSTRING(ACTUAL_SHIP_DT, 5, 2)), '-'), SUBSTRING(ACTUAL_SHIP_DT, 7, 2)) AS ACTUAL_SHIP_DT,
			SHIPTO_CD, SHIPTO_NM, RTRIM(CONCAT(ADD1, ADD2)) AS ADDR, ITEM_DESC, LOTN, ORDER_QTY, UNIT, SALESREP_NM
		FROM qms_salesorder SO
		<where>
			<if test="wherebody_status != null and wherebody_status != '' ">
				${wherebody_status}
			</if>

			<if test="r_ordersdt != null and r_ordersdt != '' ">AND SO.ORDER_DT <![CDATA[>=]]> #{r_ordersdt}</if>
			<if test="r_orderedt != null and r_orderedt != '' ">AND SO.ORDER_DT <![CDATA[<=]]> #{r_orderedt}</if>
			<if test="r_actualshipsdt != null and r_actualshipsdt != '' ">AND SO.ACTUAL_SHIP_DT <![CDATA[>=]]> #{r_actualshipsdt}</if>
			<if test="r_actualshipedt != null and r_actualshipedt != '' ">AND SO.ACTUAL_SHIP_DT <![CDATA[<=]]> #{r_actualshipedt}</if>
			<if test="rl_salesrepnm != null and rl_salesrepnm != '' ">
				AND SO.SALESREP_NM LIKE '%' + #{rl_salesrepnm} + '%'
				<!-- AND SO.SALESREP_NM LIKE '%' + #{rl_salesrepnm} + '%' -->
			</if>
			<if test="rl_orderno != null and rl_orderno != '' ">AND SO.ORDERNO LIKE '%' + #{rl_orderno} + '%' </if>
			<if test="r_custcd != null and r_custcd != '' ">AND SO.CUST_CD	= #{r_custcd}</if>
			<!-- <if test="r_shiptocd != null and r_shiptocd != '' ">AND SO.SHIPTO_NM LIKE '%' + #{r_shiptocd} + '%'</if> -->
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
		) XX
		<where>
			<if test="r_endrow != null and r_endrow != '' and r_startrow != null and r_startrow != ''" >
				ROWNUM BETWEEN #{r_startrow} AND #{r_endrow}
			</if>
		</where>
		)KK ) YY GROUP BY YY.SHIPTO_CD, YY.SHIPTO_NM
	</select>




/* ***************************************************************************************************************************************** */
/* *********** 전체 QMS 리스트 : /admin/order/getQmsOrderListAjax.lime *********** */


/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java *********** */
;

483	   /**
	 * 전체 QMS 리스트 가져오기 Ajax.
	 * @작성일 : 2021. 3. 29.
	 * @작성자 : jihye lee
	 */
	@ResponseBody
	@PostMapping(value="getQmsOrderListAjax")
	public Object getQmsItemListAjax(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("where", "admin");

		// 내부사용자 웹주문현황	> 별도 권한 설정.
		orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);

		System.out.println("==================================================");
		System.out.println(params);
		System.out.println(loginDto);
		System.out.println(model);

		return orderSvc.getQmsOrderList(params, req, loginDto);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */
;

1458	/**
	 * QMS 리스트 가져오기. O_SALESORDER.
	 * @작성일 : 2021. 3. 29.
	 * @작성자 : jihye lee
	 * @param where : admin=관리자, excel=관리자 엑셀, front=거래처,납품처, frontexcel=거래처,납품처 엑셀.
	 */
	public Map<String, Object> getQmsOrderList(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
		Map<String, Object> resMap = new HashMap<>();

		String where = Converter.toStr(params.get("where"));

		String r_ordersdt = Converter.toStr(params.get("r_ordersdt")); // 주문일자 검색 시작일.
		String r_orderedt = Converter.toStr(params.get("r_orderedt")); // 주문일자 검색 종료일.
		String r_actualshipsdt = Converter.toStr(params.get("r_actualshipsdt")); // 출고일자 검색 시작일.
		String r_actualshipedt = Converter.toStr(params.get("r_actualshipedt")); // 출고일자; 검색 종료일.

		if(r_actualshipsdt.isEmpty() || r_actualshipedt.isEmpty())
			return null;

		//String r_requestsdt = Converter.toStr(params.get("r_requestsdt")); // 납품요청일 검색 시작일.
		//String r_requestedt = Converter.toStr(params.get("r_requestedt")); // 납품요청일; 검색 종료일.
		if(!StringUtils.equals("", r_ordersdt)) r_ordersdt = r_ordersdt.replaceAll("-", "");
		if(!StringUtils.equals("", r_orderedt)) r_orderedt = r_orderedt.replaceAll("-", "");
		if(!StringUtils.equals("", r_actualshipsdt)) r_actualshipsdt = r_actualshipsdt.replaceAll("-", "");
		if(!StringUtils.equals("", r_actualshipedt)) r_actualshipedt = r_actualshipedt.replaceAll("-", "");
		//if(!StringUtils.equals("", r_requestsdt)) r_requestsdt = r_requestsdt.replaceAll("-", "");
		//if(!StringUtils.equals("", r_requestedt)) r_requestedt = r_requestedt.replaceAll("-", "");

		String rl_salesrepnm = Converter.toStr(params.get("rl_salesrepnm")); // 영업사원
		String rl_orderno = Converter.toStr(params.get("rl_orderno")); // 오더번호
		String r_custcd = Converter.toStr(params.get("r_custcd")); // 거래처
		String r_shiptocd = Converter.toStr(params.get("r_shiptocd")); // 납품처

		params.put("r_ordersdt", r_ordersdt);
		params.put("r_orderedt", r_orderedt);
		params.put("r_actualshipsdt", r_actualshipsdt);
		params.put("r_actualshipedt", r_actualshipedt);
		params.put("rl_salesrepnm", rl_salesrepnm);
		params.put("rl_orderno", rl_orderno);
		params.put("r_custcd", r_custcd);
		params.put("r_shiptocd", r_shiptocd);

		// QMS 상태 조회
		String wherebody_status = "";
		String qms_status  = params.get("qms_status") !=null?params.get("qms_status").toString():"";
		String qms_status2 = params.get("qms_status2")!=null?params.get("qms_status2").toString():"";
		String qms_status3 = params.get("qms_status3")!=null?params.get("qms_status3").toString():"";

		//QMS 사전입력 여부
		String qms_preyn   = params.get("qms_preyn")!=null?params.get("qms_preyn").toString():"";

		if (!qms_status.equals("ALL")) {
			// QMS 생성 미완료
			if(qms_status.equals("N")) {
				wherebody_status += " CASE WHEN ORDER_QTY = SF_GETQMSQTY(ORDERNO,LINE_NO) THEN 'Y' ELSE 'N' END = 'N' ";
			}
			// QMS 생성완료
			if(qms_status.equals("Y")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " OR ";
				}
				wherebody_status += " CASE WHEN ORDER_QTY = SF_GETQMSQTY(ORDERNO,LINE_NO) THEN 'Y' ELSE 'N' END = 'Y' ";
			}
		}

		if (!qms_status2.equals("ALL")) {
			// MAIL 발송 미완료
			if(qms_status2.equals("N")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "SF_GETMAILYN(ORDERNO,LINE_NO) = 'N' ";
			}

			// MAIL 발송완료
			if(qms_status2.equals("Y")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "SF_GETMAILYN(ORDERNO,LINE_NO) = 'Y' ";
			}
		}

		if (!qms_status3.equals("ALL")) {
			// QMS 회신 미완료
			if(qms_status3.equals("N")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "SF_GETFILEYN(ORDERNO,LINE_NO) = 'N' ";
			}

			// QMS 회신완료
			if(qms_status3.equals("Y")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "SF_GETFILEYN(ORDERNO,LINE_NO) = 'Y' ";
			}
		}

		if (!qms_preyn.equals("ALL")) {
			// QMS 사전입력
			if(qms_preyn.equals("Y")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "Sf_getpreorderyn(SO.cust_po) = 'Y' ";
			}

			// QMS 사후입력건
			if(qms_preyn.equals("N")) {
				if(wherebody_status.length() > 1) {
					wherebody_status+= " AND ";
				}
				wherebody_status += "Sf_getpreorderyn(SO.cust_po) = 'N' ";
			}
		}


		wherebody_status += "AND NOT(SO.STATUS1 = '980') AND SO.STATUS2 >= '620'";

//		  if(!qms_status.equals("ALL") || !qms_status2.equals("ALL") || !qms_status3.equals("ALL") || !qms_preyn.equals("ALL")) {
//			  params.put("wherebody_status", wherebody_status);
//		  }
		params.put("wherebody_status", wherebody_status);

		System.out.println(params);
		int totalCnt = qmsOrderDao.cnt(params);
		System.out.println(totalCnt);

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
		//	2024-10-16 hsg 별칭 오류가 나서 수정. SO -> XX)
		if(StringUtils.equals("", sidx)) { r_orderby = "XX.ORDERNO DESC, XX.LINE_NO ASC"; } //디폴트 지정

		params.put("r_orderby", r_orderby);

		// 엑셀 다운로드.
		if(StringUtils.equals("excel", where) || StringUtils.equals("frontexcel", where) || StringUtils.equals("orderadd", where)) {
			params.remove("r_startrow");
			params.remove("r_endrow");
		}

		List<Map<String, Object>> list = this.getQmsOrderList(params);
		System.out.println("======================qms list==============================");
		//System.out.println(list);
		resMap.put("list", list);
		resMap.put("data", list);
		resMap.put("page", params.get("r_page"));

		resMap.put("where", where);

		return resMap;
	}



/* ***************************************************************************************************************************************** */
/* *********** int totalCnt = qmsOrderDao.cnt(params); *********** */


/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java *********** */
;

25	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_qmsorder.cnt", svcMap);
	}





/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_qmsorder.xml *********** */
;

38	<select id="cnt" parameterType="hashmap" resultType="int">
	/* eorder.o_qmsorder.cnt */
		SELECT COUNT(*)
		FROM qms_salesorder SO
		LEFT JOIN O_ITEM_NEW OIN
			ON OIN.ITEM_CD = SO.ITEM_CD
			/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시 조건을 걸었음 */
			and OIN.LINE_TY = 'Y'
		<where>
			/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시로 취치를 옮김 */
			/* AND OIN.LINE_TY = 'Y' */
			1 = 1
			<if test="wherebody_status != null and wherebody_status != '' ">
				${wherebody_status}
			</if>
			<!-- 2024-10-22 hsg 구분방법은 O_ITEM_NEW.SALES_CD3 항목의 값이 아래 목록에 해당하는 품목만 대상으로 합니다
			QMS 대상 품목 기준값 : 'DAP11400', 'DAP11500', 'DAP11600', 'DAP11700', 'DAP12400', 'DAP12500', 'DAP12800', 'DAP12900', 'DAP13000' -->
			AND		OIN.SALES_CD3 IN ('DAP11400', 'DAP11500', 'DAP11600', 'DAP11700', 'DAP12400', 'DAP12500', 'DAP12800', 'DAP12900', 'DAP13000')
			<if test="r_ordersdt != null and r_ordersdt != '' ">AND SO.ORDER_DT <![CDATA[>=]]> #{r_ordersdt}</if>
			<if test="r_orderedt != null and r_orderedt != '' ">AND SO.ORDER_DT <![CDATA[<=]]> #{r_orderedt}</if>
			<if test="r_actualshipsdt != null and r_actualshipsdt != '' ">AND SO.ACTUAL_SHIP_DT <![CDATA[>=]]> #{r_actualshipsdt}</if>
			<if test="r_actualshipedt != null and r_actualshipedt != '' ">AND SO.ACTUAL_SHIP_DT <![CDATA[<=]]> #{r_actualshipedt}</if>
			<if test="rl_salesrepnm != null and rl_salesrepnm != '' ">
				AND SO.SALESREP_NM LIKE '%' + #{rl_salesrepnm} + '%'
			</if>
			<if test="rl_orderno != null and rl_orderno != '' ">AND SO.ORDERNO LIKE '%' + #{rl_orderno} + '%' </if>
			<if test="r_custcd != null and r_custcd != '' ">AND SO.CUST_CD	= #{r_custcd}</if>
			<if test="r_shiptocd != null and r_shiptocd != '' ">AND SO.SHIPTO_CD = #{r_shiptocd}</if>
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
/* *********** List<Map<String, Object>> list = this.getQmsOrderList(params); *********** */


/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */
;

178	   /**
	 * Get QMS Order List.
	 * @작성일 : 2020. 4. 24.
	 * @작성자 : kkyu
	 */
	public List<Map<String, Object>> getQmsOrderList(Map<String, Object> svcMap){
		return qmsOrderDao.list(svcMap);
	}



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java *********** */
;

17	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_qmsorder.list", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_qmsorder.xml *********** */
;


93	<select id="list" parameterType="hashmap" resultType="hashmap">
	/* eorder.o_qmsorder.list */
		SELECT /*+ HASH(table) */ *
		  FROM (
		  SELECT
				ROW_NUMBER() OVER(
				<choose>
					<when test = " r_orderby != null and r_orderby != '' ">ORDER BY ${r_orderby}</when>
					<otherwise>ORDER BY (SELECT 1)</otherwise>
				</choose>
				) AS ROWNUM
				, XX.*
		   ,CASE
				WHEN PRE1 = 'Y' AND PRE2 = 'Y' /* 사전입력 완료 */
				THEN '사전'
				WHEN PRE1 = 'Y' AND PRE2 = 'N' /* 사전입력중 */
				THEN '사전'
				ELSE '사후' /* 사후입력 대상 */
			  END AS QMS_STEP
			FROM (SELECT
						dbo.Sf_getpreorderyn(SO.cust_po) AS PRE1
						,dbo.Sf_getpreqtyyn(SO.cust_po) AS PRE2
						,ORDERNO,LINE_NO
						,dbo.SF_GETQMSID_YYYY(ORDERNO,LINE_NO)AS QMS_ARR
						,dbo.SF_GETQMSID_YYYY(ORDERNO,LINE_NO)AS QMS_ARR_TXT
						,CASE WHEN (CONVERT(DECIMAL, dbo.SF_GETPREQTY(SO.cust_po)) >= CONVERT(DECIMAL, SO.ORDER_QTY) AND dbo.Sf_getpreorderyn(SO.cust_po) ='Y' AND dbo.Sf_getpreqtyyn(SO.cust_po) = 'Y')
							THEN CONVERT(DECIMAL, SO.ORDER_QTY)
							WHEN dbo.Sf_getpreorderyn(SO.cust_po) ='Y'
							THEN CONVERT(DECIMAL, dbo.SF_GETPREQTY(SO.cust_po))
							ELSE CONVERT(DECIMAL, dbo.Sf_getqmsqty(orderno, line_no))
						 END AS QMS_ARR_QTY
						,CASE WHEN ORDER_QTY = dbo.SF_GETQMSQTY(ORDERNO,LINE_NO) THEN '완료' ELSE '미완료' END AS QMS_STATUS
						,dbo.SF_GETQMSSHIPTO(ORDERNO,LINE_NO)AS QMS_ARR_SHIPTO
						,dbo.SF_GETMAILYN(ORDERNO,LINE_NO) AS MAIL_YN
						,dbo.SF_GETFILEYN(ORDERNO,LINE_NO) AS FILE_YN
						,SO.ITEM_CD
						,ORDERTY
						,CUST_PO
						,CUST_CD
						,CUST_NM
						,dbo.SF_GETQMSQUARTER(ACTUAL_SHIP_DT) AS ACTUAL_SHIP_QUARTER
						,dbo.SF_GETQMSACTIVEYN(dbo.SF_GETQMSQUARTER(ACTUAL_SHIP_DT)) AS ACTIVEYN
						<!-- 2024-12-18 hsg Cattle Mutilation MSSQL의 시작번호는 오라클과 달리 1부터 시작하기 때문에 SUBSTRING에 0이 아닌 1로 수정 -->
						,CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ORDER_DT, 1,4), '-'), SUBSTRING(ORDER_DT, 5, 2)), '-'), SUBSTRING(ORDER_DT, 7, 2)) AS ORDER_DT
						,CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ACTUAL_SHIP_DT, 1,4), '-'), SUBSTRING(ACTUAL_SHIP_DT, 5, 2)), '-'), SUBSTRING(ACTUAL_SHIP_DT, 7, 2)) AS ACTUAL_SHIP_DT
						,SHIPTO_CD,SHIPTO_NM, RTRIM(CONCAT(ADD1, ADD2)) AS ADDR, ITEM_DESC, LOTN, ORDER_QTY, UNIT, SALESREP_NM
					FROM qms_salesorder SO
					LEFT JOIN O_ITEM_NEW OIN
						ON OIN.ITEM_CD = SO.ITEM_CD
			/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시 조건을 걸었음 */
						AND OIN.LINE_TY = 'Y'
		<where>
			/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시로 취치를 옮김 */
			/* AND OIN.LINE_TY = 'Y' */
			1 = 1
			<if test="wherebody_status != null and wherebody_status != '' ">
				${wherebody_status}
			</if>
			<!-- 2024-10-22 hsg 구분방법은 O_ITEM_NEW.SALES_CD3 항목의 값이 아래 목록에 해당하는 품목만 대상으로 합니다
			QMS 대상 품목 기준값 : 'DAP11400', 'DAP11500', 'DAP11600', 'DAP11700', 'DAP12400', 'DAP12500', 'DAP12800', 'DAP12900', 'DAP13000' -->
			AND		OIN.SALES_CD3 IN ('DAP11400', 'DAP11500', 'DAP11600', 'DAP11700', 'DAP12400', 'DAP12500', 'DAP12800', 'DAP12900', 'DAP13000')
			<if test="r_ordersdt != null and r_ordersdt != '' ">AND SO.ORDER_DT <![CDATA[>=]]> #{r_ordersdt}</if>
			<if test="r_orderedt != null and r_orderedt != '' ">AND SO.ORDER_DT <![CDATA[<=]]> #{r_orderedt}</if>
			<if test="r_actualshipsdt != null and r_actualshipsdt != '' ">AND SO.ACTUAL_SHIP_DT <![CDATA[>=]]> #{r_actualshipsdt}</if>
			<if test="r_actualshipedt != null and r_actualshipedt != '' ">AND SO.ACTUAL_SHIP_DT <![CDATA[<=]]> #{r_actualshipedt}</if>
			<if test="rl_salesrepnm != null and rl_salesrepnm != '' ">
				AND SO.SALESREP_NM LIKE '%' + #{rl_salesrepnm} + '%'
			</if>
			<if test="rl_orderno != null and rl_orderno != '' ">AND SO.ORDERNO LIKE '%' + #{rl_orderno} + '%' </if>
			<if test="r_custcd != null and r_custcd != '' ">AND SO.CUST_CD	= #{r_custcd}</if>
			<if test="r_shiptocd != null and r_shiptocd != '' ">AND SO.SHIPTO_CD = #{r_shiptocd}</if>
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











