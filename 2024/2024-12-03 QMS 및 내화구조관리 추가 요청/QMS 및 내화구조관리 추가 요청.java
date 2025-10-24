/* ***************************************************************************************************************************************** */
/* *********** QMS 조회 및 출력 : https://neweorder.knaufapac.kr/eorder/admin/report/deliveryReport.lime *********** */


942 /**
    * QMS 오더 뷰 화면
    */
    function qmsOrderPopOpen(qmsId){
        // POST 팝업 열기.
        var widthPx = 1050;
        var heightPx = 800;
        var options = 'toolbar=no, location=no, status=no, directories=no, channelmode=no, menubar=no, scrollbars=yes, resizable=yes, width='+widthPx+', height='+heightPx;
        var popup = window.open('qmsOrderPopView.lime', 'qmsOrderPop', options);
        $frmPopSubmit = $('form[name="frmPopSubmit"]');
        //$frmPopSubmit.attr('action', '${url}/admin/order/qmsOrderPop.lime');
        $frmPopSubmit.attr('action', '${url}/admin/order/qmsOrderPopView.lime?qmsId='+qmsId+"&work=mod");
        $frmPopSubmit.attr('method', 'post');
        $frmPopSubmit.attr('target', 'qmsOrderPop');
        $frmPopSubmit.submit();
        popup.focus();
    }






/* ***************************************************************************************************************************************** */
/* *********** QMS오더상세 : https://neweorder.knaufapac.kr/eorder/admin/order/qmsOrderPopView.lime?qmsId=20243Q0037-1&work=mod *********** */


/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java *********** */


644    /**
     * QMS 미리보기 팝업 
     * @작성일 : 2021. 4. 26.
     * @작성자 : jsh
     */
    @RequestMapping(value="qmsOrderPopView" ,method= {RequestMethod.GET,RequestMethod.POST})
    public Object qmsOrderPopView(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        String qmsId = (params.get("qmsId") == null) ? "" : (String) params.get("qmsId");
        // QMS 시퀀스와 함께 입력된 경우 처리
        if(qmsId.indexOf("-") > 0) {
            String[] qmsArr = qmsId.split("-");
            params.put("qmsId", qmsArr[0]);
            params.put("qmsSeq", qmsArr[1]);
        }else {
          //기본 qms 시퀀스 입력
          params.put("qmsSeq",params.get("qmsSeq")!=null?params.get("qmsSeq"):1);
        }
        
        List<Map<String, Object>> getQmsPopMastList = orderSvc.getQmsPopMastList(params);
        model.addAttribute("qmsMastList", getQmsPopMastList);
        	
        Map<String, Object> tempMast = null;
        if(getQmsPopMastList.size() > 0) {
            tempMast = getQmsPopMastList.get(0);
            model.addAttribute("createUser", tempMast.get("CREATEUSER"));
            model.addAttribute("qmsSplitYn", tempMast.get("QMS_SPLIT_YN"));
        }else {
            model.addAttribute("qmsSplitYn",'N');
        }
        
        List<Map<String, Object>> getQmsPopDetlList = orderSvc.getQmsPopDetlList(params);
        model.addAttribute("qmsDetlList", getQmsPopDetlList);
        
        List<Map<String, Object>> getQmsFireproofList = orderSvc.getQmsFireproofList(params);
        model.addAttribute("qmsFireproofList", getQmsFireproofList);
        
        model.addAttribute("qmsId", params.get("qmsId"));
        model.addAttribute("qmsSeq", params.get("qmsSeq"));
        return "admin/order/qmsOrderPopView";
    }
;




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */



2180    /**
     * QMS 오더 신규 입력 목록
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsPopMastList(Map<String, Object> params){
        
        List<Map<String, Object>> qmsPopList = qmsOrderDao.getQmsPopMastList(params);
        return qmsPopList;
    }




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java *********** */


126    public List<Map<String, Object>> getQmsPopMastList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsPopMastList", svcMap);
    }




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_qmsorder.xml *********** */


731		<select id="getQmsPopMastList" parameterType="hashmap" resultType="hashmap">
		/* eorder.o_qmsorder.getQmsPopMastList */
		SELECT A.* 
		      ,CASE WHEN (SELECT COUNT(*) FROM (SELECT QMS_ID,ORDERNO FROM QMS_ORD_DETL GROUP BY QMS_ID,ORDERNO) Q WHERE Q.QMS_ID = A.QMS_ID) > 1 THEN 'N' ELSE 'Y' END AS QMS_SPLIT_YN
		      ,B.CUST_NM
		      <!-- 2024-10-24 HSG QMS 내화구조 품질관리서 수정. ,B.ADD1 +' '+ B.ADD2 + B.ADD3 + B.ADD4 AS CUST_ADDR -->
		      , B.ADD1 AS CUST_ADDR
		      ,dbo.SF_GETQMSBIZNO(B.TAX_ID) AS CUST_BIZ_NO
		      ,B.ZIP_CD
		      ,B.SALESREP_CD
		      ,B.SALESREP_NM
		      ,B.TEAM_CD
		      ,B.TEAM_NM
		      ,B.MAILING_NM
		      ,dbo.SF_GETQMSACTIVEYN(QMS_ID) AS ACTIVEYN
		  FROM QMS_ORD_MAST A
		 LEFT JOIN O_CUSTOMER B ON A.CUST_CD = B.CUST_CD
		 WHERE A.QMS_ID = #{qmsId}
		    <if test="work != null and work.equals( 'mod' )">
		    AND A.QMS_SEQ = #{qmsSeq}
		    </if>
		    <if test="work != null and work.equals( 'split' )">
		    AND A.DELETEYN <![CDATA[ <> ]]> 'Y'
		    </if>
		    <if test="work != null and work.equals( 'write' )">
		    AND A.DELETEYN <![CDATA[ <> ]]> 'Y'
		    </if>
		    <if test="work == null">
		    AND A.DELETEYN = 'N'
		    </if>
		    <!-- AND DELETEYN = 'N' -->
	</select>






/* ***************************************************************************************************************************************** */
/* *********** QMS 오더 신규 입력 목록 : List<Map<String, Object>> getQmsPopDetlList = orderSvc.getQmsPopDetlList(params); *********** */



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */


2191    /**
     * QMS 오더 신규 입력 목록
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsPopDetlList(Map<String, Object> params){
        
        List<Map<String, Object>> qmsPopList = qmsOrderDao.getQmsPopDetlList(params);
        return qmsPopList;
    }




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java *********** */


130    public List<Map<String, Object>> getQmsPopDetlList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsPopDetlList", svcMap);
    }




764	<select id="getQmsPopDetlList" parameterType="hashmap" resultType="hashmap">
		/* eorder.o_qmsorder.getQmsPopDetlList */
		SELECT B.*
		  FROM QMS_ORD_MAST A
		      ,QMS_ORD_DETL B
		 WHERE A.QMS_ID = B.QMS_ID
		   AND A.QMS_ID = #{qmsId}
		   <if test="work != null">
		   	AND A.QMS_SEQ = #{qmsSeq}
		   </if>
		   <if test="work != null and work.equals( 'split' )">
		    AND A.DELETEYN <![CDATA[ <> ]]> 'Y'
		   </if>
		   <if test="work != null and work.equals( 'write' )">
		    AND A.DELETEYN <![CDATA[ <> ]]> 'Y'
		   </if>
		   <if test="work == null">
		    AND A.DELETEYN = 'N'
		   </if>
	</select>


/* *********** QMS 오더 내화구조 입력 목록:  List<Map<String, Object>> getQmsFireproofList = orderSvc.getQmsFireproofList(params); *********** */



2220    /**
     * QMS 오더 내화구조 입력 목록
     * @작성일 : 2021. 5. 3.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsFireproofList(Map<String, Object> params){
        
        List<Map<String, Object>> qmsPopList = qmsOrderDao.getQmsFireproofList(params);
        return qmsPopList;
    }





/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java *********** */

134    public List<Map<String, Object>> getQmsFireproofList(Map<String, Object> svcMap) {
        return sqlSession.selectList("eorder.o_qmsorder.getQmsFireproofList", svcMap);
    }



/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_qmsorder.xmlaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa *********** */


786	<select id="getQmsFireproofList" parameterType="hashmap" resultType="hashmap">
		/* eorder.o_qmsorder.getQmsFireproofList */
		SELECT A.KEYCODE
		      ,A.FIREPROOFTYPE
		      <!-- 2024-11-29 HSG Tomstone piledriver FLOAT 형식으로 소수점 2자리까지 리턴하기 때문에 소스에서 비교시 매칭되는게 없음. 소수점 1자리까지 리턴 -->
		      ,FORMAT(A.FIRETIME, '0.#') AS FIRETIME
		      ,A.FILENAME
		      ,CEILING((SELECT COUNT(*) FROM O_FIREPROOFMASTER F WHERE F.FIRETIME = A.FIRETIME AND F.ACTIVE = 'Y' ) / 4) AS ROWSPAN_CNT
		      ,ROW_NUMBER() OVER (PARTITION BY A.FIRETIME ORDER BY A.KEYCODE ASC) AS RNUM
		      ,ROW_NUMBER() OVER (ORDER BY A.KEYCODE ASC) AS RCNT
		      ,(SELECT COUNT(F.FIRETIME) FROM O_FIREPROOFMASTER F WHERE F.ACTIVE = 'Y') AS RLAST
		      ,CASE WHEN DENSE_RANK() OVER (PARTITION BY A.FIRETIME ORDER BY A.KEYCODE DESC) = 1 THEN 'Y' ELSE 'N' END AS LAST_YN
              ,CASE WHEN (B.KEYCODE IS NOT NULL AND B.DELETEYN='N') THEN 'Y' ELSE 'N' END AS CHK_YN
		  FROM O_FIREPROOFMASTER A
          LEFT OUTER JOIN QMS_ORD_FRCN B
            ON A.KEYCODE = B.KEYCODE
           AND B.QMS_ID  = #{qmsId}
           AND B.QMS_SEQ = #{qmsSeq}
		 WHERE A.ACTIVE = 'Y'
		 ORDER BY A.FIRETIME,A.DISPLAYORDER
;







/* ***************************************************************************************************************************************** */
/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java *********** */

504    /**
     * 전체 QMS 디테일 항목 리스트 가져오기 Ajax.
     * @작성일 : 2021. 3. 29.
     * @작성자 : jihye lee
     */
    @ResponseBody
    @PostMapping(value="getQmsPopDetlGridList")
    public Object getQmsPopDetlGridList(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {
        params.put("where", "admin");
        
        //페이징 무력화
        params.put("page", null);
        params.put("rows", null);
        List<Map<String, Object>> list = orderSvc.getQmsPopDetlGridList(params);
        
        // 내부사용자 웹주문현황  > 별도 권한 설정.
        orderSvc.setParamsForAdminOrderList(params, req, loginDto, model);
        
        return list;
    }   




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java *********** */

187    /**
     * Get QMS getQmsPopDetlGridList.
     * @작성일 : 2020. 5. 4.
     * @작성자 : jsh
     */
    public List<Map<String, Object>> getQmsPopDetlGridList(Map<String, Object> svcMap){
        return qmsOrderDao.getQmsPopDetlGridList(svcMap);
    }




/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java *********** */

21	public List<Map<String, Object>> getQmsPopDetlGridList(Map<String, Object> svcMap) {
	    return sqlSession.selectList("eorder.o_qmsorder.getQmsPopDetlGridList", svcMap);
	}





/* *********** C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_qmsorder.xml *********** */


	<select id="getQmsPopDetlGridList" parameterType="hashmap" resultType="hashmap">
		/* eorder.o_qmsorder.getQmsPopDetlGridList */
		SELECT /*ROWNUM RR,*/ XX.* ,B.QMS_ID, B.QMS_SEQ ,B.QMS_REMARK ,B.QMS_DETL_ID ,B.QMS_ID +'-'+ CONVERT(VARCHAR, B.QMS_SEQ) AS QMS_ORD_NO
		        ,(SELECT CASE WHEN COUNT(Q.QMS_ORD_QTY) > 0 THEN SUM(Q.QMS_ORD_QTY) ELSE 0 END 
					  FROM QMS_ORD_DETL Q , QMS_ORD_MAST M
					 WHERE Q.QMS_ID = M.QMS_ID AND Q.QMS_SEQ = M.QMS_SEQ AND Q.DELETEYN = 'N' AND M.DELETEYN = 'N'
					   AND Q.ORDERNO = XX.ORDERNO AND Q.LINE_NO = XX.LINE_NO AND Q.ITEM_CD = XX.ITEM_CD AND Q.LOTNO = XX.LOTN ) AS QMS_ORD_BALANCE
                 ,CASE WHEN ISNULL(B.QMS_ORD_QTY,0) = 0 
                      THEN XX.ORDER_QTY - (SELECT CASE WHEN COUNT(Q.QMS_ORD_QTY) > 0 THEN SUM(Q.QMS_ORD_QTY) ELSE 0 END 
											  FROM QMS_ORD_DETL Q , QMS_ORD_MAST M
											 WHERE Q.QMS_ID = M.QMS_ID AND Q.QMS_SEQ = M.QMS_SEQ AND Q.DELETEYN = 'N' AND M.DELETEYN = 'N'
											   AND Q.ORDERNO = XX.ORDERNO AND Q.LINE_NO = XX.LINE_NO AND Q.ITEM_CD = XX.ITEM_CD AND Q.LOTNO = XX.LOTN )
                      ELSE B.QMS_ORD_QTY END AS QMS_ORD_QTY
		        ,CASE WHEN (SELECT COUNT(*) FROM (SELECT QMS_ID,ORDERNO FROM QMS_ORD_DETL GROUP BY QMS_ID,ORDERNO) Q WHERE Q.QMS_ID = A.QMS_ID) > 1 THEN 'N' ELSE 'Y' END AS QMS_SPLIT_YN
		  FROM (SELECT ORDERNO,LINE_NO ,ITEM_CD,ORDERTY , CUST_PO, CUST_NM, 
		    CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ORDER_DT, 0,4), '-'), SUBSTRING(ORDER_DT, 5, 2)), '-'), SUBSTRING(ORDER_DT, 7, 2)) AS ORDER_DT, 
		    CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ACTUAL_SHIP_DT, 0,4), '-'), SUBSTRING(ACTUAL_SHIP_DT, 5, 2)), '-'), SUBSTRING(ACTUAL_SHIP_DT, 7, 2)) AS ACTUAL_SHIP_DT,
		    SHIPTO_NM, RTRIM(CONCAT(ADD1, ADD2)) AS ADDR, ITEM_DESC, LOTN, ORDER_QTY, UNIT, SALESREP_NM
		  FROM qms_salesorder SO) XX 
		      ,QMS_ORD_MAST A
		      ,QMS_ORD_DETL B
		 WHERE A.QMS_ID   = B.QMS_ID
		   AND A.QMS_SEQ  = B.QMS_SEQ
		   AND B.ORDERNO  = XX.ORDERNO
		   AND B.LINE_NO  = XX.LINE_NO
		   AND A.QMS_ID   = #{qmsId}
		   AND A.QMS_SEQ  = #{qmsSeq}
	</select>













