

/* ***************************************************************************************************************************************** */
/* *********** https://neweorder.knaufapac.kr/eorder/admin/order/orderView.lime?r_reqno=101831352412105&qmsTempId=null *********** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\webapp\WEB-INF\views\admin\order\orderView.jsp *********** */
;





/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java *********** */
;

241	   /**
	 * OK.
	 * 웹주문현황 폼 > 웹주문상세 폼.
	 * @작성일 : 2020. 4. 10.
	 * @작성자 : kkyu
	 */
	@GetMapping(value="orderView")
	public String orderView(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		// 내부사용자 웹주문현황	> 별도 권한 설정.
		orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);

		// O_CUST_ORDER_Header 하나 가져오기.
		String r_reqno = Converter.toStr(params.get("r_reqno"));
		if(StringUtils.equals("", r_reqno)) throw new LimeBizException(MsgCode.DATA_REQUIRE_ERROR2);

		Map<String, Object> orderHeader = orderSvc.getCustOrderHOne(params); // 별도 권한을 위해 params로 넘기자.
		//Map<String, Object> orderHeader = orderSvc.getCustOrderHOne(r_reqno);
		if(CollectionUtils.isEmpty(orderHeader)) throw new LimeBizException(MsgCode.DATA_AUTH_ERROR);

		// O_CUST_ORDER_Detail 리스트 가져오기.
		// 2024-10-16 hsg 별칭 오류가 나서 수정. COD -> XX)
		List<Map<String, Object>> orderDetailList = orderSvc.getCustOrderDList(r_reqno, "", "", "XX.LINE_NO ASC ", 0, 0);

		// O_CONFIRM_ORDER_Detail 리스트 가져오기.
		List<Map<String, Object>> orderConfirmDetailList = orderSvc.getOrderConfirmDList(r_reqno, "", "", 0, 0);
		//logger.debug("orderConfirmDetailList : {}", orderConfirmDetailList);

		model.addAttribute("orderHeader", orderHeader);
		model.addAttribute("orderDetailList", orderDetailList);
		model.addAttribute("orderConfirmDetailList", orderConfirmDetailList);

		model.addAttribute("orderStatus", StatusUtil.ORDER.getMap()); // 주문상태 Map형태로 가져오기.
		model.addAttribute("orderStatusToJson", StatusUtil.ORDER.getMapToJson()); // 주문상태 JSON형태로 가져오기.

		return "admin/order/orderView";
	}




/* ***************************************************************************************************************************************** */
/* *********** O_CUST_ORDER_Header 하나 가져오기. : Map<String, Object> orderHeader = orderSvc.getCustOrderHOne(params); *********** */
;




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */
;

116	   public Map<String, Object> getCustOrderHOne(Map<String, Object> svcMap){
		return custOrderHDao.one(svcMap);
	}





/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\CustOrderHDao.java *********** */
;

31	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_cust_order_h.one", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_cust_order_h.xml *********** */
;

121	<select id="one" parameterType="hashmap" resultType="hashmap">
		SELECT COH.*
			, (SELECT USER_NM FROM O_USER WHERE RTRIM(USERID) = RTRIM(COH.USERID)) AS USER_NM
			, CU.CUST_NM
			, ST.SHIPTO_NM
			, ST.QUOTE_QT
			, US_SALES.USERID AS SALESUSERID, US_SALES.USER_NM AS SALESUSER_NM
			, (SELECT CSUSERID FROM O_CSSALESMAP WHERE SALESUSERID = US_SALES.USERID AND FIXEDYN = 'Y') AS CSUSERID
			, (SELECT SUB1.USER_NM FROM O_USER SUB1, O_CSSALESMAP SUB2 WHERE SUB1.USERID = SUB2.CSUSERID AND SUB2.SALESUSERID = US_SALES.USERID AND SUB2.FIXEDYN = 'Y') AS CSUSER_NM
			, (SELECT CC_NAME FROM COMMONCODE WHERE CC_PARENT='C01' AND CC_CODE = COH.RETURN_CD) AS RETURN_REASON
		FROM O_CUST_ORDER_H COH
			LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
			LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
			LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
		<where>
			<if test="r_userid != null and r_userid != '' ">AND RTRIM(COH.USERID) = RTRIM(#{r_userid})</if>
			<if test="r_reqno != null and r_reqno != '' ">AND COH.REQ_NO = #{r_reqno}</if>
			<if test="rl_reqno != null and rl_reqno != '' ">AND COH.REQ_NO LIKE '%' + #{rl_reqno} + '%'</if>
			<if test="r_custcd != null and r_custcd != '' ">AND COH.CUST_CD = #{r_custcd}</if>
			<if test="rl_custcd != null and rl_custcd != '' ">AND COH.CUST_CD LIKE '%' + #{rl_custcd} + '%'</if>
			<if test="r_custnm != null and r_custnm != '' ">AND CU.CUST_NM = #{r_custnm}</if>
			<if test="rl_custnm != null and rl_custnm != '' ">AND CU.CUST_NM LIKE '%' + #{rl_custnm} + '%'</if>
			<if test="r_shiptocd != null and r_shiptocd != '' ">AND COH.SHIPTO_CD = #{r_shiptocd}</if>
			<if test="rl_salesusernm != null and rl_salesusernm != '' ">AND US_SALES.USER_NM LIKE '%' + #{rl_salesusernm} + '%'</if>
			<if test="rl_receiver != null and rl_receiver != '' ">AND COH.RECEIVER LIKE '%' + #{rl_receiver} + '%'</if>
			<if test="r_csuserid != null and r_csuserid != '' ">
				AND US_SALES.USERID IN (SELECT SALESUSERID FROM O_CSSALESMAP WHERE CSUSERID = #{r_csuserid})
			</if>
			<if test="r_insdate != null and r_insdate != '' ">AND COH.INDATE <![CDATA[>=]]> CONVERT(DATE, #{r_insdate})</if>
			<if test="r_inedate != null and r_inedate != '' ">AND COH.INDATE <![CDATA[<=]]> CONVERT(DATE, #{r_inedate})</if>
			<if test="r_statuscd != null and r_statuscd != '' ">AND COH.STATUS_CD = #{r_statuscd}</if>
			<if test="ri_statuscd != null">
				AND COH.STATUS_CD IN <foreach collection="ri_statuscd" item="status_cd" separator="," open="(" close=")">#{status_cd}</foreach>
			</if>

			<!-- 관리자 권한에 따른 조건절 -->
			<if test="r_adminauthority != null and r_adminauthority != '' ">
				<if test='"AD".equals(r_adminauthority)'>
				</if>
				<if test='"CS".equals(r_adminauthority)'>
					AND CU.SALESREP_CD IN (SELECT SALESUSERID FROM O_CSSALESMAP WHERE CSUSERID = #{r_adminuserid})
				</if>
				<if test='"SH".equals(r_adminauthority) or "SM".equals(r_adminauthority) or "SR".equals(r_adminauthority)'>
					<if test='"SH".equals(r_adminauthority)'>
						AND CU.SALESREP_CD IN (SELECT USERID FROM O_USER WHERE USER_CATE2 = #{r_adminuserid})
					</if>
					<if test='"SM".equals(r_adminauthority)'>
						AND CU.SALESREP_CD IN (SELECT USERID FROM O_USER WHERE USER_CATE3 = #{r_adminuserid})
					</if>
					<if test='"SR".equals(r_adminauthority)'>
						AND CU.SALESREP_CD = #{r_adminuserid}
					</if>
				</if>
				<if test='"MK".equals(r_adminauthority)'>
				</if>
			</if>
		</where>
	</select>




/* ***************************************************************************************************************************************** */
/* *********** O_CUST_ORDER_Detail 리스트 가져오기 : List<Map<String, Object>> orderDetailList = orderSvc.getCustOrderDList(r_reqno, "", "", "XX.LINE_NO ASC ", 0, 0); *********** */
;



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */
;

129	   /**
	 * Get O_CUST_ORDER_D List.
	 * @작성일 : 2020. 4. 2.
	 * @작성자 : kkyu
	 */
	public List<Map<String, Object>> getCustOrderDList(String req_no, String cust_cd, String shipto_cd, String order_by, int start_row, int end_row){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_reqno", req_no);
		svcMap.put("r_custcd", cust_cd);
		svcMap.put("r_shiptocd", shipto_cd);
		svcMap.put("r_orderby", (StringUtils.equals("", order_by) ? "XX.REQ_NO DESC, XX.LINE_NO ASC " : order_by));
		svcMap.put("r_startrow", start_row);
		svcMap.put("r_endrow", end_row);
		return this.getCustOrderDList(svcMap);
	}
	public List<Map<String, Object>> getCustOrderDList(Map<String, Object> svcMap){
		return custOrderDDao.list(svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\CustOrderDDao.java *********** */
;

39	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_cust_order_d.list", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_cust_order_d.xml *********** */
;

94	<select id="list" parameterType="hashmap" resultType="hashmap">
		SELECT * FROM (
			SELECT ROW_NUMBER() OVER(
			<choose>
				<when test = " r_orderby != null and r_orderby != '' ">ORDER BY ${r_orderby}</when>
				<otherwise>ORDER BY (SELECT 1)</otherwise>
			</choose>
			) AS ROWNUM
			, XX.* FROM (
			SELECT COD.*
				, COH.CUST_CD, COH.SHIPTO_CD
				, IT.DESC1, IT.DESC2
				, ITI.*
				, (SELECT COUNT(*) FROM ITEMRECOMMEND WHERE ITR_ITEMCD = COD.ITEM_CD) AS RECOMMEND_ITEM_COUNT
				, CASE WHEN SALES_CD1 = 'A' AND (SALES_CD2 IN ('02','06','US1') OR (SALES_CD2 = '76' AND PLAN_FMLY = 'H15' ) ) THEN 'Y' ELSE 'N' END AS FIREPROOF_YN
			FROM O_CUST_ORDER_D COD
				LEFT JOIN O_CUST_ORDER_H COH ON COD.REQ_NO = COH.REQ_NO
				LEFT JOIN O_ITEM_NEW IT ON COD.ITEM_CD = IT.ITEM_CD
				LEFT JOIN ITEMINFO ITI ON COD.ITEM_CD = ITI.ITI_ITEMCD
			<where>
				<if test="r_reqno != null and r_reqno != ''">AND COD.REQ_NO = #{r_reqno}</if>
				<if test="r_custcd != null and r_custcd != ''">AND COH.CUST_CD = #{r_custcd}</if>
				<if test="r_shiptocd != null and r_shiptocd != ''">AND COH.SHIPTO_CD = #{r_shiptocd}</if>
				<if test="r_lineno != null and r_lineno != ''">AND COD.LINE_NO = #{r_lineno}</if>
				<if test="r_itemcd != null and r_itemcd != ''">AND IT.ITEM_CD = #{r_itemcd}</if>
				<if test="r_statuscd != null and r_statuscd != '' ">AND COH.STATUS_CD = #{r_statuscd}</if>
			</where>
			) XX
		) S
		<where>
			<if test="r_endrow != null and r_endrow != '' and r_startrow != null and r_startrow != ''" >
				ROWNUM BETWEEN #{r_startrow} AND #{r_endrow}
			</if>
		</where>
	</select>




/* ***************************************************************************************************************************************** */
/* *********** O_CONFIRM_ORDER_Detail 리스트 가져오기. : List<Map<String, Object>> orderConfirmDetailList = orderSvc.getOrderConfirmDList(r_reqno, "", "", 0, 0); *********** */





/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */
;

148	   /**
	 * Get O_ORDER_CONFIRM_D List.
	 * @작성일 : 2020. 4. 28.
	 * @작성자 : kkyu
	 */
	public List<Map<String, Object>> getOrderConfirmDList(String req_no, String cust_po, String order_by, int start_row, int end_row){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_reqno", req_no);
		svcMap.put("r_custcd", cust_po);
		// 2024-10-16 hsg 별칭 오류가 나서 수정. OCD -> XX)
		svcMap.put("r_orderby", (StringUtils.equals("", order_by) ? "XX.CUST_PO ASC, XX.LINE_NO ASC " : order_by));
		svcMap.put("r_startrow", start_row);
		svcMap.put("r_endrow", end_row);
		return this.getOrderConfirmDList(svcMap);
	}
	public List<Map<String, Object>> getOrderConfirmDList(String req_no, String cust_po, String not_status_cd, String order_by, int start_row, int end_row){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_reqno", req_no);
		svcMap.put("r_custcd", cust_po);
		svcMap.put("rn_statuscd", not_status_cd);
		// 2024-10-22 hsg 별칭 오류가 나서 수정. OCD -> XX)
		svcMap.put("r_orderby", (StringUtils.equals("", order_by) ? "XX.CUST_PO ASC, XX.LINE_NO ASC " : order_by));
		svcMap.put("r_startrow", start_row);
		svcMap.put("r_endrow", end_row);
		return this.getOrderConfirmDList(svcMap);
	}
	public List<Map<String, Object>> getOrderConfirmDList(Map<String, Object> svcMap){
		return orderConfirmDDao.list(svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\OrderConfirmDDao.java *********** */
;

27	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_order_confirm_d.list", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_order_confirm_d.xml *********** */
;

45	<select id="list" parameterType="hashmap" resultType="hashmap">
		SELECT * FROM (SELECT
			ROW_NUMBER() OVER(
			<choose>
				<when test = " r_orderby != null and r_orderby != '' ">ORDER BY ${r_orderby}</when>
				<otherwise>ORDER BY (SELECT 1)</otherwise>
			</choose>
			) AS ROWNUM
			, XX.* FROM (
			SELECT OCD.*
				, OCH.REQ_NO, OCH.COMPANY_CD, OCH.PICKING_DT, OCH.STATUS_CD, OCH.CUSTOMMATTER, OCH.REQUEST_DT, OCH.REQUEST_TIME, OCH.REMARK, OST.QUOTE_QT
				, (SELECT PT_NAME FROM PLANT WHERE WERKS = OCH.COMPANY_CD) AS PT_NAME
				, (SELECT DESC1 FROM O_ITEM_NEW WHERE ITEM_CD = OCD.ITEM_CD) AS ITEM_NAME
				, (SELECT QUANTITY FROM O_CUST_ORDER_D WHERE REQ_NO = OCH.REQ_NO AND LINE_NO = OCD.LINE_NO) AS COD_QUANTITY
				, (SELECT CC_NAME FROM COMMONCODE WHERE CC_PARENT='C01' AND CC_CODE = OCD.OCD_RETURNCD) AS ITEM_RETURN_REASON
				, UDC.DRDL01 AS DRDL01
				, UDC.DRDL01 AS ROUTE
				, SUBSTRING(UDC.DRSPHD,3,2) AS DRSPHD
			FROM O_ORDER_CONFIRM_D OCD
			LEFT JOIN O_ORDER_CONFIRM_H OCH ON OCD.CUST_PO = OCH.CUST_PO
			LEFT JOIN O_F0005 UDC ON UDC.DRKY = OCD.WEEK
			LEFT JOIN O_SHIPTO OST ON OCH.SHIPTO_CD = OST.SHIPTO_CD
			<where>
				<if test="r_reqno != null and r_reqno != ''">AND OCH.REQ_NO = #{r_reqno}</if>
				<if test="r_custpo != null and r_custpo != ''">AND OCD.CUST_PO = #{r_custpo}</if>
				<if test="rn_statuscd != null and rn_statuscd != ''">AND ISNULL(OCD.OCD_STATUSCD, ' ') != #{rn_statuscd}</if>
			</where>
			) XX
		) S
		<where>
			<if test="r_endrow != null and r_endrow != '' and r_startrow != null and r_startrow != ''" >
				ROWNUM BETWEEN #{r_startrow} AND #{r_endrow}
			</if>
		</where>
	</select>










































