

/* ***************************************************************************************************************************************** */
/* *********** 주소록 팝업 : https://eordertest.knaufapac.kr/eorder/front/base/pop/orderAddressBookmarkPop.lime *********** */
/* ***************************************************************************************************************************************** */



/* ***************************************************************************************************************************************** */
/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\webapp\WEB-INF\views\front\base\orderAddressBookmarkPop.jsp *********** */


139	// 조회.
	function dataSearch() {
		$('input[name="page"]').val('1');

		formPostSubmit('frm_pop', '${url}/front/base/pop/orderAddressBookmarkPop.lime');
	}


175	<!-- 2024-12-24 hsg Camel Clutch first 여가 시작. E-order front – 주문 등록 – 주소록 List에서 Keyword 서치 기능 추가 -->
	<!-- DropDown List(구분) 속성 : 납품 주소, 인수자명, 연락처, 연락처2 -->
	<div class="boardView marB30">
		<ul>
			<li class="half wide">
				<label class="view-h">품목분류</label>
				<div class="view-b" style="width:50%;">
					<select class="form-control form-sm" name="r_srchGbn" id="r_srchGbn" width="100px">
						<option value="">선택하세요</option>
						<option value="ADD1">납품 주소</option>
						<option value="RECEIVER">인수자명</option>
						<option value="TEL1">연락처</option>
						<option value="TEL2">연락처2</option>
					</select>
						<input type="text" class="form-control" style="width:50%;" name="rl_inputText" id="rl_inputText" value="${param.rl_rl_inputText}" onkeypress="if(event.keyCode == 13){dataSearch();}" />
					<span class="searchBtn" width="100px; align:right;">
						<button type="button" onclick="dataSearch();">Search</button>
					</span>
				</div>
			</li>
		</ul>
	</div> <!-- boardView -->
	<!-- 2024-12-24 hsg Camel Clutch first 여그가 끝. -->


/* *********** location	C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\front\FBaseCtrl.java *********** */
;

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




/* *********** location	C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */
;

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



/* *********** location	C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\OrderAddressBookmarkDao.java *********** */
;

31	public int cnt(Map<String, Object> svcMap) {
		return sqlSession.selectOne("eorder.orderAddressBookmark.cnt", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\orderAddressBookmark.xml *********** */
;

63	<select id="cnt" parameterType="hashmap" resultType="int">
		SELECT COUNT(*) FROM ORDERADDRESSBOOKMARK OAB
		<where>
			<if test="r_oabseq != null and r_oabseq != '' ">AND OAB_SEQ = #{r_oabseq}</if>
			<if test="r_oabuserid != null and r_oabuserid != '' ">AND OAB_USERID = #{r_oabuserid}</if>
		</where>
	</select>





/* ***************************************************************************************************************************************** */
/* *********** List<Map<String, Object>> list = orderAddressBookmarkDao.list(params); *********** */




/* *********** location	C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\OrderAddressBookmarkDao.java *********** */
;

35	public List<Map<String, Object>> list(Map<String, Object> svcMap) {
		return sqlSession.selectList("eorder.orderAddressBookmark.list", svcMap);
	}




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\orderAddressBookmark.xml *********** */
;

71	<select id="list" parameterType="hashmap" resultType="hashmap">
		SELECT * FROM (SELECT
			ROW_NUMBER() OVER(
			<choose>
				<when test = " r_orderby != null and r_orderby != '' ">ORDER BY ${r_orderby}</when>
				<otherwise>ORDER BY (SELECT 1)</otherwise>
			</choose>
			) AS ROWNUM
			, XX.* FROM (
		SELECT OAB.*
		FROM ORDERADDRESSBOOKMARK OAB
		<where>
			<if test="r_oabseq != null and r_oabseq != '' ">AND OAB_SEQ = #{r_oabseq}</if>
			<if test="r_oabuserid != null and r_oabuserid != '' ">AND OAB_USERID = #{r_oabuserid}</if>
		</where>
		) XX ) S
		<where>
			<if test="r_endrow != null and r_endrow != '' and r_startrow != null and r_startrow != ''" >
				ROWNUM BETWEEN #{r_startrow} AND #{r_endrow}
			</if>
		</where>
	</select>





















































