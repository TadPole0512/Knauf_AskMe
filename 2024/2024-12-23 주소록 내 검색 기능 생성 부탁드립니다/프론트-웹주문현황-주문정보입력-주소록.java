


/* ***************************************************************************************************************************************** */
/* *********** http://localhost:8080/eorder/front/order/orderAdd.lime *********** */
/* ***************************************************************************************************************************************** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\front\FOrderCtrl.java *********** */


170	/**
	 * 주문등록 폼.
	 * @작성일 : 2020. 4. 3.
	 * @작성자 : kkyu
	 */
     @GetMapping(value="orderAdd")
     public String orderAdd(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
         commonSvc.getFrontCommonData(params, req, model, loginDto);
         
         String copyReqNo = Converter.toStr(params.get("copy_reqno")); // 다른 주문건의 주문번호를 받아와 그 주문정보를 그대로 입력한다.
         
         String reqNo = Converter.toStr(params.get("r_reqno")); // 임시저장 상태의 건을 수정 또는 주문접수로 넘길때 파라미터.
         
         //String userId = loginDto.getUserId();
         String custCd = loginDto.getCustCd();
         String shiptoCd = loginDto.getShiptoCd();
         String authority = loginDto.getAuthority(); // CO=거래처,CT=납풉처. 
         
         //params.put("where", "front");
         //params.put("r_custcd", loginDto.getCustCd());
         //if(!StringUtils.equals("CO", loginDto.getAuthority())) {
         //	params.put("r_shiptocd", loginDto.getShiptoCd());
         //}
         
         // 웹주문현황 리스트 폼에서 복사 폼.
         if(!StringUtils.equals("", copyReqNo)) {
             Map<String, Object> custOrderH = orderSvc.getCustOrderHOne(loginDto, copyReqNo);
             if(CollectionUtils.isEmpty(custOrderH)) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
             
             model.addAttribute("pageType", "COPY");
             model.addAttribute("custOrderH", custOrderH);
             model.addAttribute("custOrderD", orderSvc.getCustOrderDList(copyReqNo, custCd, shiptoCd, "LINE_NO ASC ", 0, 0));
         }
         
         // 임시저장(99) 상태에서 수정 폼.
         // 주문접수(00) 상태에서 수정 폼 이동 추가.
         if(!StringUtils.equals("", reqNo)) {
             String m_statuscd = Converter.toStr(params.get("m_statuscd"));
             logger.debug("m_statuscd : {}", m_statuscd);
             
             Map<String, Object> custOrderH = orderSvc.getCustOrderHOne(loginDto, reqNo);
             if(CollectionUtils.isEmpty(custOrderH)) throw new LimeBizException(MsgCode.DATA_NOT_FOUND_ERROR);
             
             // 접근가능한 상태인지 체크.
             if(!StatusUtil.ORDER.statusCheck(Converter.toStr(custOrderH.get("STATUS_CD")), m_statuscd)) { 
                 req.setAttribute("resultAjax", "<script>alert('"+MsgCode.DATA_STATUS_ERROR.getMessage()+"'); history.back();</script>");
                 //req.setAttribute("resultAjax", "<script>window.open('about:blank','_self').close(); alert('"+MsgCode.DATA_STATUS_ERROR.getMessage()+"');</script>");
                 return "textAjax";
             }
             
             model.addAttribute("pageType", "EDIT");
             model.addAttribute("custOrderH", custOrderH);
             model.addAttribute("custOrderD", orderSvc.getCustOrderDList(reqNo, custCd, shiptoCd, "LINE_NO ASC ", 0, 0));
         }else {
             model.addAttribute("pageType", "ADD");
             if(StringUtils.equals("CT", authority)) model.addAttribute("shipto", customerSvc.getShipTo(shiptoCd));
         }
         
         model.addAttribute("todayDate", Converter.dateToStr("yyyy-MM-dd"));
         model.addAttribute("orderStatus", StatusUtil.ORDER.getMap()); // 주문상태 Map형태로 가져오기.
         model.addAttribute("orderStatusToJson", StatusUtil.ORDER.getMapToJson()); // 주문상태 JSON형태로 가져오기.
         
         model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10));
         
         return "front/order/orderAdd";
     }
 



/* ***************************************************************************************************************************************** */
/* *********** commonSvc.getFrontCommonData(params, req, model, loginDto); *********** */


/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\CommonSvc.java *********** */


266	/**
	 * 프론트 로그인 이후 시점, 공통으로 사용하는 메소드.
	 * 프론트 컨트롤러 모든 폼에 해당 함수 호출해야 함.
	 * 
	 * config 테이블에서 list로 받아와서 필요한 것만 람다식으로 model에 담았지만, 리스트에서 많은 데이터를 필요로 한다면 for문 사용이 나을듯하네...
	 * 
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : kkyu
	 */
	public void getFrontCommonData(Map<String, Object> params, HttpServletRequest req, Model model, LoginDto loginDto) throws LimeBizException{
		Map<String, Object> svcMap = new HashMap<>();
		List<Map<String, Object>> configList = configDao.list(params);
		
		// header >>> 로고 이미지 가져오기. => 필요없네...
		Map<String, Object> config1 = configList.stream().filter(x -> x.get("CF_ID").equals("SYSTEMLOGO")).findFirst().get();
		logger.debug("config logo map : {}", config1);
		model.addAttribute("logo", config1.get("CF_VALUE"));
		
		// header >>> 임시저장(99) 개수 가져오기.
		String today = Converter.dateToStr("yyyy-MM-dd");
		String[] ri_statuscd = {"99"};
		svcMap.put("r_insdate", today);
		svcMap.put("r_inedate", today);
		svcMap.put("r_userid", loginDto.getUserId());
		svcMap.put("ri_statuscd", ri_statuscd);
		model.addAttribute("orderStatus99Cnt", custOrderHDao.cnt(svcMap));
		svcMap.clear();
		
		// bottom >>> 영역 영업사원, CS담당자 정보 가져오기
		Map<String, Object> ctMap = customerSvc.getCustomer(loginDto.getCustCd());
		model.addAttribute("ctMap", ctMap);
		
		// bottom >>> 거래처 가상계좌 번호 가져오기.
		//params.put("r_ayan8", loginDto.getCustCd());
		//model.addAttribute("custVAccount", commonDao.getCustVAcount(params));
	}



/* ***************************************************************************************************************************************** */
/* *********** List<Map<String, Object>> configList = configDao.list(params); *********** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\ConfigDao.java *********** */

19	public List<Map<String, Object>> list(Map<String, Object> svcMap){
		return sqlSession.selectList("eorder.config.list", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\config.xml *********** */

5	<select id="list" parameterType="hashmap" resultType="hashmap">
		SELECT * FROM (
			SELECT ROW_NUMBER() OVER(
			<choose>
				<when test = " r_orderby != null and r_orderby != '' ">ORDER BY ${r_orderby}</when>
				<otherwise>ORDER BY (SELECT 1)</otherwise>
			</choose>
			) AS ROWNUM
			, XX.* 
		FROM (
			SELECT * FROM CONFIG 
			<where>
				<if test="r_cfid != null and r_cfid != ''">AND CF_ID = #{r_cfid}</if>
				<if test="ri_cfid != null and ri_cfid != ''">
					AND CF_ID IN <foreach collection="ri_cfid" item="cf_id" separator="," open="(" close=")">#{cf_id}</foreach>
				</if>
			</where>
			) XX  
		) S
		<where>
			<if test="r_endrow != null and r_endrow != '' and r_startrow != null and r_startrow != ''" >	
				ROWNUM BETWEEN #{r_startrow} AND #{r_endrow}
			</if>
		</where>	
	</select>
	
	<select id="one" parameterType="hashmap" resultType="hashmap">
		SELECT * FROM CONFIG 
		<where>
			CF_ID = #{r_cfid}
		</where>	
	</select>





/* ***************************************************************************************************************************************** */
/* *********** header >>> 임시저장(99) 개수 가져오기. : model.addAttribute("orderStatus99Cnt", custOrderHDao.cnt(svcMap)); *********** */


/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\CustOrderHDao.java *********** */


35	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_cust_order_h.cnt", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_cust_order_h.xml *********** */


180	<select id="cnt" parameterType="hashmap" resultType="int">
		SELECT COUNT(*) 
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
				<!-- 2024-10-18 HSG 조회조건 변경 : 종료일 이전이 아니라 종료일 다음날 이전으로 변경 -->
				<if test="r_inedate != null and r_inedate != '' ">AND COH.INDATE <![CDATA[<]]> DATEADD(day, 1,CONVERT(DATE, #{r_inedate}))</if>
			<!-- <if test="r_inedate != null and r_inedate != '' ">AND COH.INDATE <![CDATA[<=]]> CONVERT(DATE, #{r_inedate})</if> -->
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
 			<!-- End. -->
		</where>
	</select>




/* ***************************************************************************************************************************************** */
/* *********** bottom >>> 영역 영업사원, CS담당자 정보 가져오기 : Map<String, Object> ctMap = customerSvc.getCustomer(loginDto.getCustCd()); *********** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\CustomerSvc.java *********** */


49	public Map<String, Object> getCustomer(String r_custcd){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_custcd", r_custcd);
		return this.getCustomer(svcMap);
	}
	public Map<String, Object> getCustomer(Map<String, Object> svcMap){
		return customerDao.one(svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\CustomerDao.java *********** */


22	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_customer.one", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_customer.xml *********** */


63	<select id="one" parameterType="hashmap" resultType="hashmap">
		SELECT 
			CU.*, US.USER_NM, US.AUTHORITY, US.USER_FILE, US.CELL_NO, US.USER_POSITION
			, (SELECT COUNT(*) FROM O_USER WHERE CUST_CD = CU.CUST_CD AND AUTHORITY = 'CO') AS CUSTOMER_USER_CNT
 			, (SELECT COUNT(*) FROM O_USER WHERE CUST_CD = CU.CUST_CD AND AUTHORITY = 'CT') AS SHIPTO_USER_CNT
			, (SELECT COUNT(*) FROM O_SHIPTO WHERE CUST_CD = CU.CUST_CD) AS SHIPTO_CNT
			, US2.USER_NM AS CSUSER_NM
			, US2.TEL_NO AS CSUSER_TEL
			, US2.CELL_NO AS CSUSER_CELL
			, US2.USER_FILE AS CSUSER_FILE
			, US2.USER_POSITION AS CSUSER_POSITION
		FROM O_CUSTOMER CU
			LEFT JOIN O_USER US ON CU.SALESREP_CD = US.USERID
			LEFT JOIN O_CSSALESMAP CSM ON CU.SALESREP_CD = CSM.SALESUSERID AND CSM.FIXEDYN='Y' <!-- CS USER를 가져오기 위한 -->
    		LEFT JOIN O_USER US2 ON CSM.CSUSERID = US2.USERID
		<where>
			<if test="r_custcd != null and r_custcd != ''">AND CU.CUST_CD = #{r_custcd}</if>
			<if test="rl_custcd != null and rl_custcd != '' ">AND CU.CUST_CD LIKE '%' + #{rl_custcd} + '%'</if>
			<if test="r_custnm != null and r_custnm != ''">AND CU.CUST_NM = #{r_custnm}</if>
			<if test="rl_custnm != null and rl_custnm != '' ">AND CU.CUST_NM LIKE '%' + #{rl_custnm} + '%'</if>
			<if test="r_salesrepcd != null and r_salesrepcd != ''">AND SALESREP_CD = #{r_salesrepcd}</if>
			<if test="rl_salesrepcd != null and rl_salesrepcd != '' ">AND SALESREP_CD LIKE '%' + #{rl_salesrepcd} + '%'</if>
			<if test="r_salesrepnm != null and r_salesrepnm != ''">AND SALESREP_NM = #{r_salesrepnm}</if>
			<if test="rl_salesrepnm != null and rl_salesrepnm != '' ">AND SALESREP_NM LIKE '%' + #{rl_salesrepnm} + '%'</if>
			<if test="r_teamcd != null and r_teamcd != ''">AND TEAM_CD = #{r_teamcd}</if>
			<if test="rl_teamcd != null and rl_teamcd != '' ">AND TEAM_CD LIKE '%' + #{rl_teamcd} + '%'</if>
			<if test="r_teamnm != null and r_teamnm != ''">AND TEAM_NM = #{r_teamnm}</if>
			<if test="rl_teamnm != null and rl_teamnm != '' ">AND TEAM_NM LIKE '%' + #{rl_teamnm} + '%'</if>
			<if test="r_taxid != null and r_taxid != ''">AND TAX_ID = #{r_taxid}</if>
			<if test="rl_taxid != null and rl_taxid != '' ">AND TAX_ID LIKE '%' + #{rl_taxid} + '%'</if>
			<if test="r_mailingnm != null and r_mailingnm != ''">AND MAILING_NM = #{r_mailingnm}</if>
			<if test="rl_mailingnm != null and rl_mailingnm != '' ">AND MAILING_NM LIKE '%' + #{rl_mailingnm} + '%'</if>
			<if test="r_add1 != null and r_add1 != ''">AND ADD1 = #{r_add1}</if>
			<if test="rl_add1 != null and rl_add1 != '' ">AND ADD1 LIKE '%' + #{rl_add1} + '%'</if>
			<if test="r_add2 != null and r_add2 != ''">AND ADD2 = #{r_add2}</if>
			<if test="rl_add2 != null and rl_add2 != '' ">AND ADD2 LIKE '%' + #{rl_add2} + '%'</if>
			<if test="r_add3 != null and r_add3 != ''">AND ADD3 = #{r_add3}</if>
			<if test="rl_add3 != null and rl_add3 != '' ">AND ADD3 LIKE '%' + #{rl_add3} + '%'</if>
			<if test="r_add4 != null and r_add4 != ''">AND ADD4 = #{r_add4}</if>
			<if test="rl_add4 != null and rl_add4 != '' ">AND ADD4 LIKE '%' + #{rl_add4} + '%'</if>
			<if test="r_zipcd != null and r_zipcd != ''">AND ZIP_CD = #{r_zipcd}</if>
			<if test="rl_zipcd != null and rl_zipcd != '' ">AND ZIP_CD LIKE '%' + #{rl_zipcd} + '%'</if>
			<if test="r_buildingty != null and r_buildingty != ''">AND BUILDING_TY = #{r_buildingty}</if>
			<if test="rl_buildingty != null and rl_buildingty != '' ">AND BUILDING_TY LIKE '%' + #{rl_buildingty} + '%'</if>
			<if test="r_buildingnm != null and r_buildingnm != ''">AND BUILDING_NM = #{r_buildingnm}</if>
			<if test="rl_buildingnm != null and rl_buildingnm != '' ">AND BUILDING_NM LIKE '%' + #{rl_buildingnm} + '%'</if>
			<if test="r_businessty != null and r_businessty != ''">AND BUSINESS_TY = #{r_businessty}</if>
			<if test="rl_businessty != null and rl_businessty != '' ">AND BUSINESS_TY LIKE '%' + #{rl_businessty} + '%'</if>
			<if test="r_businessnm != null and r_businessnm != ''">AND BUSINESS_NM = #{r_businessnm}</if>
			<if test="rl_businessnm != null and rl_businessnm != '' ">AND BUSINESS_NM LIKE '%' + #{rl_businessnm} + '%'</if>
			<if test="r_dummy != null and r_dummy != ''">AND DUMMY = #{r_dummy}</if>
			<if test="rl_dummy != null and rl_dummy != '' ">AND DUMMY LIKE '%' + #{rl_dummy} + '%'</if>
			<if test="ri_custcd != null">
				AND CUST_CD IN <foreach collection="ri_custcd" item="custcd" separator="," open="(" close=")">#{custcd}</foreach>
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
/* *********** 웹주문현황 리스트 폼에서 복사 폼. : Map<String, Object> custOrderH = orderSvc.getCustOrderHOne(loginDto, copyReqNo); *********** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */


92    /**
     * Get O_CUST_ORDER_H One.
     * @작성일 : 2020. 4. 2.
     * @작성자 : kkyu
     */
    public Map<String, Object> getCustOrderHOne(String req_no){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_reqno", req_no);
        return this.getCustOrderHOne(svcMap);
    }
    public Map<String, Object> getCustOrderHOne(String req_no, String userid, String status_cd){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_reqno", req_no);
        svcMap.put("r_userid", userid);
        svcMap.put("r_statuscd", status_cd);
        return this.getCustOrderHOne(svcMap);
    }
    public Map<String, Object> getCustOrderHOne(LoginDto loginDto, String req_no){
        Map<String, Object> svcMap = new HashMap<>();
        svcMap.put("r_reqno", req_no);
        svcMap.put("r_custcd", loginDto.getCustCd());
        svcMap.put("r_shiptocd", loginDto.getShiptoCd());
        return this.getCustOrderHOne(svcMap);
    }
    public Map<String, Object> getCustOrderHOne(Map<String, Object> svcMap){
        return custOrderHDao.one(svcMap);
    }



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\CustOrderHDao.java *********** */

31	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_cust_order_h.one", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_cust_order_h.xml *********** */


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
/* *********** model.addAttribute("custOrderD", orderSvc.getCustOrderDList(copyReqNo, custCd, shiptoCd, "LINE_NO ASC ", 0, 0)); *********** */


/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */

129    /**
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

39	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.o_cust_order_d.list", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_cust_order_d.xml *********** */


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
/* *********** if(StringUtils.equals("CT", authority)) model.addAttribute("shipto", customerSvc.getShipTo(shiptoCd)); *********** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\CustomerSvc.java *********** */

58	public Map<String, Object> getShipTo(String r_shiptocd){
		Map<String, Object> svcMap = new HashMap<>();
		svcMap.put("r_shiptocd", r_shiptocd);
		return shipToDao.one(svcMap);
	}


/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\ShipToDao.java *********** */


15	public Map<String, Object> one(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.o_shipto.one", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_shipto.xml *********** */

5	<select id="one" parameterType="hashmap" resultType="hashmap">
		SELECT ST.*
		FROM O_SHIPTO ST
		<where>
			<if test="r_shiptocd != null and r_shiptocd != '' ">AND ST.SHIPTO_CD = #{r_shiptocd}</if>
			<if test="r_custcd != null and r_custcd != '' ">AND ST.CUST_CD = #{r_custcd}</if>
			<if test="rl_shiptonm != null and rl_shiptonm != '' ">AND ST.SHIPTO_NM = #{rl_shiptonm}</if>
		</where>
	</select>





/* ***************************************************************************************************************************************** */
/* *********** model.addAttribute("main2BannerList",boardSvc.getBannerListForFront("3",10)); *********** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\BoardSvc.java *********** */

733	/**
	 * 프론트(로그인,인덱스)에 출력할 배너리스트
	 * @param bn_type 1: 로그인메인배너 , 2: 메인1 배너(상단), 3: 메인2 배너  (중단)
	 * @param limitrow 출력개수
	 * @return
	 */
	public List<Map<String ,Object>> getBannerListForFront(String bn_type,int limitrow){
		Map<String, Object> svcMap = new HashMap<String, Object>();

		svcMap.put("r_bntype", bn_type);
		svcMap.put("r_orderby", "BN_INDATE DESC");
		svcMap.put("r_limitrow", limitrow);
		return bannerDao.listForFront(svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\BannerDao.java *********** */

41	public List<Map<String, Object>> listForFront(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.banner.listForFront", svcMap);
	}






/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\banner.xml *********** */

115    <select id="listForFront" parameterType="hashmap" resultType="hashmap">
        SELECT *
        FROM (
            SELECT ROW_NUMBER() OVER(
            	/*ORDER BY (SELECT 1)*/
            	<choose>
					<when test = " r_orderby != null and r_orderby != '' ">ORDER BY ${r_orderby}</when>
					<otherwise>ORDER BY (SELECT 1)</otherwise>
				</choose>
            ) AS ROWNUM
            , *
            FROM BANNER
            <where>
                BN_USEYN = 'Y'
                <if test=" r_bntype != null and r_bntype != '' ">AND BN_TYPE = #{ r_bntype }</if>
            </where>
        ) XX 
        <where>
            <if test="r_limitrow != null and r_limitrow != ''">ROWNUM <![CDATA[<=]]> #{ r_limitrow }</if>
        </where>
        <!-- <if test=" r_orderby != null and r_orderby != '' ">ORDER BY ${ r_orderby }</if>  -->

    </select>








/* ***************************************************************************************************************************************** */
/* *********** O_CUST_ORDER_Header 주소록 폼. : http://localhost:8080/eorder/front/base/pop/orderAddressBookmarkPop.lime *********** */



/* ***************************************************************************************************************************************** */
/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\front\FBaseCtrl.java *********** */


162	/**
	 * O_CUST_ORDER_Header 주소록 폼.
	 * @작성일 : 2020. 4. 6.
	 * @작성자 : kkyu
	 */
	@RequestMapping(value="/front/base/pop/orderAddressBookmarkPop", method={RequestMethod.GET, RequestMethod.POST})
	public String orderAddressBookmarkPop(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
		params.put("r_oabuserid", loginDto.getUserId());
		Map<String, Object> resMap = orderSvc.getOrderAddressBookmark(params, req, loginDto);
		model.addAllAttributes(resMap);
		
		return "front/base/orderAddressBookmarkPop";
	}



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */

383    /**
     * O_CUST_ORDER_Header 주소록 리스트 가져오기.
     * @작성일 : 2020. 4. 1.
     * @작성자 : kkyu
     */
    public Map<String, Object> getOrderAddressBookmark(Map<String, Object> params, HttpServletRequest req, LoginDto loginDto) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        
        String layer_pop = Converter.toStr(params.get("layer_pop")); // Y=모바일앱 레이어팝업 여부.
        logger.debug("layer_pop : {}", layer_pop);
        
        int totalCnt = orderAddressBookmarkDao.cnt(params);

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
        if(StringUtils.equals("", sidx)) { r_orderby = "OAB_INDATE DESC "; } //디폴트 지정
        params.put("r_orderby", r_orderby);

        // Y=모바일앱 레이어팝업인 경우는 페이징 처리 제거.
        if(StringUtils.equals("Y", layer_pop)) {
            params.remove("r_startrow");
            params.remove("r_endrow");
        }
        
        List<Map<String, Object>> list = orderAddressBookmarkDao.list(params);
        resMap.put("list", list);
        resMap.put("data", list);
        
        return resMap;
    }




/* ***************************************************************************************************************************************** */
/* *********** int totalCnt = orderAddressBookmarkDao.cnt(params); *********** */


/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\OrderAddressBookmarkDao.java *********** */


31	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.orderAddressBookmark.cnt", svcMap);
	}



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\orderAddressBookmark.xml *********** */

63	<select id="cnt" parameterType="hashmap" resultType="int">
		SELECT COUNT(*) FROM ORDERADDRESSBOOKMARK OAB
		<where>
			<if test="r_oabseq != null and r_oabseq != '' ">AND OAB_SEQ = #{r_oabseq}</if>
			<if test="r_oabuserid != null and r_oabuserid != '' ">AND OAB_USERID = #{r_oabuserid}</if>
		</where>
	</select>



/* ***************************************************************************************************************************************** */
/* *********** 페이징 처리 : pager.gridSetInfo(totalCnt, params, req); *********** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\common\util\Pager.java *********** */


11	/**
	 * 그리드 페이징 & 폼 페이징
	 * page:페이지, rows:레코드수
	 */
	public void gridSetInfo(int totalCnt, Map<String, Object> params, HttpServletRequest req) {
		Device device = DeviceUtils.getCurrentDevice(req);
		
		int r_page = Converter.toInt(params.get("page"), 1); // 페이지
		int r_limitrow = Converter.toInt(params.get("rows"), 10); // 한페이지에 보여줄 레코드수
		
		if (r_page == 0) r_page = 1;
		if (r_limitrow == 0) r_limitrow = 10;
		
		if (r_limitrow > 0) {
			int totalPage = ( totalCnt % r_limitrow == 0 ) ? totalCnt / r_limitrow : totalCnt / r_limitrow + 1; // 전체페이지 수 
			if (r_page > totalPage) r_page = 1;

			int r_endrow = r_page * r_limitrow; // 끝지점
			int r_startrow = r_endrow - r_limitrow + 1; // 시작지점
			
			// 파라미터 설정 For Form & Grid.  
			params.put("r_startrow", r_startrow);
			params.put("r_endrow", r_endrow);
			params.put("totpage", totalPage);
			
			// 파라미터 설정 For Only Form. 
			int startnumber = totalCnt - ( r_limitrow * ( r_page - 1 ) ); // 일련번호 설정
			int viewpageea = (device.isMobile() || device.isTablet()) ? 5 : 10; // 페이징처리시 보여지는 페이지수 > 접속기기가 모바일(태블릿)=5, PC=10.
			//int viewpageea = Converter.toInt(params.get("r_viewpageea"), 10);
			int startpage = r_page - (r_page - 1) % viewpageea; // 페이징처리시 처음 보여지는 페이지수
			int endpage = startpage + viewpageea - 1; // 페이징처리시 마지막에 보여지는 페이지수
			if(totalPage < endpage) endpage = totalPage;
			params.put("startnumber", startnumber);
			params.put("r_page", r_page);
			params.put("startpage", startpage);
			params.put("endpage", endpage);
			params.put("r_limitrow", r_limitrow);
			params.put("limitrow"+r_limitrow, "selected='selected'");
			
		}
		else { // r_limitrow = -1 로 설정하여 페이징 안타게 처리.
			params.put("r_startrow", "");
			params.put("r_endrow", "");			
		}
	}




/* ***************************************************************************************************************************************** */
/* *********** List<Map<String, Object>> list = orderAddressBookmarkDao.list(params); *********** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\OrderAddressBookmarkDao.java *********** */

35	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.orderAddressBookmark.list", svcMap);
	}




