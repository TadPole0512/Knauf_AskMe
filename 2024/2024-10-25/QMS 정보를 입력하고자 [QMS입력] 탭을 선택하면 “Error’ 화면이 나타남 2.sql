

C:\GitHub\Knauf_Eorder_NEW\src\main\webapp\WEB-INF\views\admin\order\qmsOrderPop.jsp

659	function qmsOrderSaves(obj){
757	url : '${url}/admin/order/setQmsOrderMastUpdate.lime',
773	url : '${url}/admin/order/setQmsOrderDetlUpdate.lime',
790	url : '${url}/admin/order/setQmsOrderFireproofInit.lime',
805	url : '${url}/admin/order/setQmsOrderFireproofUpdate.lime',



/* ***************************************************************************************************************************************** */
/* *********** QMS Master 입력 Ajax. *********** */
/* *********** qmsOrderPop.jsp *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\webapp\WEB-INF\views\admin\order\qmsOrderPop.jsp

757	url : '${url}/admin/order/setQmsOrderMastUpdate.lime',


/* *********** OrderCtrl.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java

791	* QMS Master 입력 Ajax.
796	@PostMapping(value="setQmsOrderMastUpdate")
797	public Object setQmsOrderMastUpdate(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {

810	//MAST HISTORY
811	orderSvc.setQmsOrderMastHistory(params);



/* *********** OrderSvc.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java

2081	/**
2082	* QMS 오더 마스터 UPDATE.
2083	* @작성일 : 2021. 5. 3.
2084	* @작성자 : jsh
2085	*/
2086	public int setQmsOrderMastHistory(Map<String, Object> params){
2087		return qmsOrderDao.setQmsOrderMastHistory(params);
2088	}


/* *********** QmsOrderDao.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java

69	public int setQmsOrderMastHistory(Map<String, Object> svcMap) {
70		return sqlSession.update("eorder.o_qmsorder.setQmsOrderMastHistory", svcMap);


/* *********** o_qmsorder.xml *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_qmsorder.xml

<!-- 2024-10-28 hsg Merge문을 종료할 때 세미콜론(;)을 붙임 -->

428	<insert id="setQmsOrderMastHistory" parameterType="hashmap" >
429	/* eorder.o_qmsorder.setQmsOrderMastHistory */
430	MERGE INTO QMS_ORD_CORP F
431		<!-- 2024-10-28 hsg MSSQL에서는 MERGE문에 DUAL만 단독으로 사용할 수 없어서 별칭으로 대체함. USING DUAL ON (F.SHIPTO_CD = #{shiptoCd}) -->
431		USING (SELECT 1 AS DUAL) AS T ON (F.SHIPTO_CD = #{shiptoCd})
432	WHEN MATCHED THEN
433		UPDATE SET F.SHIPTO_ADDR = #{shiptoAddr}
434				  ,F.SHIPTO_EMAIL = #{shiptoEmail}
435				  ,F.CNSTR_ADDR = #{cnstrAddr}
436				  ,F.CNSTR_BIZ_NO = #{cnstrBizNo}
437				  ,F.CNSTR_TEL = #{cnstrTel}
438				  ,F.SUPVS_ADDR = #{supvsAddr}
439				  <!--  ,F.SUPVS_BIZ_NO = #{supvsBizNo}  -->
440				  ,F.SUPVS_QLF_NO = #{supvsQlfNo}
441				  ,F.SUPVS_DEC_NO = #{supvsDecNo}
442				  ,F.SUPVS_TEL = #{supvsTel}
443				  ,F.UPDATEUSER = #{userId}
444				  ,F.UPDATETIME = GETDATE()
445	WHEN NOT MATCHED THEN
446		INSERT (SHIPTO_CD,SHIPTO_NM,SHIPTO_ADDR,SHIPTO_EMAIL,CNSTR_NM,CNSTR_ADDR,CNSTR_BIZ_NO,CNSTR_TEL
447				,SUPVS_NM,SUPVS_ADDR 
448				<!-- ,SUPVS_BIZ_NO  -->
449				,SUPVS_QLF_NO,SUPVS_DEC_NO,SUPVS_TEL,CREATEUSER,CREATETIME,UPDATEUSER,UPDATETIME,DELETEYN)
450		VALUES (#{shiptoCd}
451			  ,#{shiptoNm}
452			  ,#{shiptoAddr}
453			  ,#{shiptoEmail}
454			  ,#{cnstrNm}
455			  ,#{cnstrAddr}
456			  ,#{cnstrBizNo}
457			  ,#{cnstrTel}
458			  ,#{supvsNm}
459			  ,#{supvsAddr}
460			  <!--  ,#{supvsBizNo}  -->
461			  ,#{supvsQlfNo}
462			  ,#{supvsDecNo}
463			  ,#{supvsTel}
464			  ,#{userId}
465			  ,GETDATE()
466			  ,#{userId}
467			  ,GETDATE()
468			  ,'N');



/* ***************************************************************************************************************************************** */
/* *********** QMS 오더 마스터 UPDATE. *********** */
/* *********** OrderCtrl.java *********** */

812	//MAST SAVE 
813	return orderSvc.setQmsOrderMastUpdate(params);




/* *********** OrderSvc.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java

2063	/**
2064	* QMS 오더 마스터 UPDATE.
2065	* @작성일 : 2021. 5. 3.
2066	* @작성자 : jsh
2067	*/
2068	public int setQmsOrderMastUpdate(Map<String, Object> params){
2069		return qmsOrderDao.setQmsOrderMastUpdate(params);
2070	}




/* *********** QmsOrderDao.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java

61	public int setQmsOrderMastUpdate(Map<String, Object> svcMap) {
62		return sqlSession.update("eorder.o_qmsorder.setQmsOrderMastUpdate", svcMap);
63	}




/* *********** o_qmsorder.xml *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_qmsorder.xml

402	<insert id="setQmsOrderMastUpdate" parameterType="hashmap" >
403	/* eorder.o_qmsorder.setQmsOrderMastUpdate */
404		UPDATE QMS_ORD_MAST
405			SET SHIPTO_CD = #{shiptoCd}
406				,SHIPTO_NM = #{shiptoNm}
407				,SHIPTO_ADDR = #{shiptoAddr}
408				,SHIPTO_EMAIL = #{shiptoEmail}
409				<!-- ,CNSTR_CD = #{cnstrCd} -->
410				,CNSTR_NM = #{cnstrNm}
411				,CNSTR_ADDR = #{cnstrAddr}
412				,CNSTR_BIZ_NO = #{cnstrBizNo}
413				,CNSTR_TEL = #{cnstrTel}
414				<!-- ,SUPVS_CD = #{supvsCd} -->
415				,SUPVS_NM = #{supvsNm}
416				,SUPVS_ADDR = #{supvsAddr}
417				<!-- ,SUPVS_BIZ_NO = #{supvsBizNo} -->
418				,SUPVS_QLF_NO = #{supvsQlfNo}
419				,SUPVS_DEC_NO = #{supvsDecNo}
420				,SUPVS_TEL = #{supvsTel}
421				,UPDATEUSER = #{userId}
422				,UPDATETIME = GETDATE()
423				,DELETEYN = 'N'
424		WHERE QMS_ID = #{qmsId}
425			AND QMS_SEQ = #{qmsSeq}
426	</insert>







/* ***************************************************************************************************************************************** */
/* *********** QMS Detail 입력 Ajax. *********** */
/* *********** qmsOrderPop.jsp *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\webapp\WEB-INF\views\admin\order\qmsOrderPop.jsp

773	url : '${url}/admin/order/setQmsOrderDetlUpdate.lime',



/* *********** OrderCtrl.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java

836	* QMS Detail 입력 Ajax.
841	@PostMapping(value="setQmsOrderDetlUpdate")
842	public Object setQmsOrderDetlUpdate(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {

845	return orderSvc.setQmsOrderDetlUpdate(params);



/* *********** OrderSvc.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java

2126	/**
2127	* QMS 오더 디테일 UPDATE.
2128	* @작성일 : 2021. 5. 3.
2129	* @작성자 : jsh
2130	*/
2131	public int setQmsOrderDetlUpdate(Map<String, Object> params){
2132		return qmsOrderDao.setQmsOrderDetlUpdate(params);
2133	}


/* *********** QmsOrderDao.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java

90	public int setQmsOrderDetlUpdate(Map<String, Object> svcMap) {
91		return sqlSession.update("eorder.o_qmsorder.setQmsOrderDetlUpdate", svcMap);
92	}


/* *********** o_qmsorder.xml *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_qmsorder.xml

560	<insert id="setQmsOrderDetlUpdate" parameterType="hashmap" >
561	/* eorder.o_qmsorder.setQmsOrderDetlUpdate */
562	UPDATE QMS_ORD_DETL
563		SET QMS_ORD_QTY = #{QMS_ORD_QTY}
564			,QMS_REMARK  = #{QMS_REMARK}
565			,DELETEYN    = 'N'
566	WHERE QMS_ID      = #{QMS_ID}
567		AND QMS_SEQ     = #{QMS_SEQ}
568		AND QMS_DETL_ID = #{QMS_DETL_ID}
569	</insert>










/* ***************************************************************************************************************************************** */
/* *********** QMS Detail Fireproof Ajax. 초기화 *********** */
/* *********** qmsOrderPop.jsp *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\webapp\WEB-INF\views\admin\order\qmsOrderPop.jsp

790	url : '${url}/admin/order/setQmsOrderFireproofInit.lime',



/* *********** OrderCtrl.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java

850	* QMS Detail Fireproof Ajax. 초기화
855	@PostMapping(value="setQmsOrderFireproofInit")
856	public Object setQmsOrderFireproofInit(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {

859	return orderSvc.setQmsOrderFireproofInit(params);



/* *********** OrderSvc.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java

2144	/**
2145	* QMS 오더 Fireproof UPDATE 초기화.
2146	* @작성일 : 2021. 5. 3.
2147	* @작성자 : jsh
2148	*/
2149	public int setQmsOrderFireproofInit(Map<String, Object> params){
2150		return qmsOrderDao.setQmsOrderFireproofInit(params);
2151	}


/* *********** QmsOrderDao.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java

106	public int setQmsOrderFireproofInit(Map<String, Object> svcMap) {
107		return sqlSession.update("eorder.o_qmsorder.setQmsOrderFireproofInit", svcMap);
108	}


/* *********** o_qmsorder.xml *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_qmsorder.xml

571	<update id="setQmsOrderFireproofInit" parameterType="hashmap" >
572	/* eorder.o_qmsorder.setQmsOrderFireproofInit */
573		UPDATE QMS_ORD_FRCN 
574			SET DELETEYN = 'Y'
575		WHERE QMS_ID = #{qmsId}
576			AND QMS_SEQ = #{qmsSeq}
577	</update>








/* ***************************************************************************************************************************************** */
/* *********** QMS Detail Fireproof Ajax. *********** */
/* *********** qmsOrderPop.jsp *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\webapp\WEB-INF\views\admin\order\qmsOrderPop.jsp

805	url : '${url}/admin/order/setQmsOrderFireproofUpdate.lime',



/* *********** OrderCtrl.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java

863	* QMS Detail Fireproof Ajax.
868	@PostMapping(value="setQmsOrderFireproofUpdate")
869	public Object setQmsOrderFireproofUpdate(@RequestParam Map<String, Object> params, HttpServletRequest req, HttpServletResponse res, LoginDto loginDto, Model model) throws Exception {

872	return orderSvc.setQmsOrderFireproofUpdate(params);



/* *********** OrderSvc.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\svc\OrderSvc.java

2162	/**
2163	 * QMS 오더 Fireproof UPDATE 입력.
2164	 * @작성일 : 2021. 5. 3.
2165	 * @작성자 : jsh
2166	 */
2167	public int setQmsOrderFireproofUpdate(Map<String, Object> params){
2168		return qmsOrderDao.setQmsOrderFireproofUpdate(params);
2169	}


/* *********** QmsOrderDao.java *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\java\com\limenets\eorder\dao\QmsOrderDao.java

118	public int setQmsOrderFireproofUpdate(Map<String, Object> svcMap) {
119		return sqlSession.update("eorder.o_qmsorder.setQmsOrderFireproofUpdate", svcMap);
120	}


/* *********** o_qmsorder.xml *********** */

C:\GitHub\Knauf_Eorder_NEW\src\main\resources\sql\mssql\o_qmsorder.xml

<!-- 2024-10-28 hsg Merge문을 종료할 때 세미콜론(;)을 붙임 -->
628	<insert id="setQmsOrderFireproofUpdate" parameterType="hashmap" >
629	/* eorder.o_qmsorder.setQmsOrderFireproofUpdate */
630		MERGE INTO QMS_ORD_FRCN F
631		<!-- 2024-10-28 hsg MSSQL에서는 MERGE문에 DUAL만 단독으로 사용할 수 없어서 별칭으로 대체함. USING DUAL ON (F.QMS_ID = #{qmsId} AND F.QMS_SEQ = #{qmsSeq} AND F.KEYCODE = #{keyCode}) -->
631			USING (SELECT 1 AS DUAL) AS T ON (F.QMS_ID = #{qmsId} AND F.QMS_SEQ = #{qmsSeq} AND F.KEYCODE = #{keyCode})
632		WHEN MATCHED THEN
633			UPDATE SET F.DELETEYN = 'N'
634		WHEN NOT MATCHED THEN
635			INSERT (QMS_ID,QMS_SEQ,KEYCODE,CREATEUSER,CREATETIME,UPDATEUSER,UPDATETIME)
636			VALUES (#{qmsId},#{qmsSeq},#{keyCode},#{userId},GETDATE(),#{userId},GETDATE())
637	</insert>





USING (SELECT 1 AS DUAL) AS T ON (F.QMS_ID = #{qmsId} AND F.QMS_SEQ = #{qmsSeq} AND F.KEYCODE = #{keyCode})