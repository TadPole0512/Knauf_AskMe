
/* ***************************************************************************************************************************************** */
/* *********** /admin/order/getShiptoListAjax.lime(String) *********** */




/* *********** eorder.o_qmsorder.getShipToList *********** */
;

/* eorder.o_qmsorder.getShipToList */
		SELECT
				YY.SHIPTO_CD, YY.SHIPTO_NM
		FROM	(
					SELECT	KK.*
					FROM	(
								SELECT
										ROW_NUMBER() OVER( ORDER BY XX.ORDERNO DESC, XX.CUST_PO DESC   ) AS ROWNUM
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
								FROM	qms_salesorder SO
								WHERE	SO.ACTUAL_SHIP_DT >= '20240301'
								AND		SO.ACTUAL_SHIP_DT <= '20240930'
							) XX
				)KK ) YY
		GROUP BY YY.SHIPTO_CD, YY.SHIPTO_NM

;



/* *********** eorder.o_qmsorder.cnt *********** */
;

/* eorder.o_qmsorder.cnt */
		SELECT	COUNT(*)
		FROM	qms_salesorder SO
				LEFT JOIN O_ITEM_NEW OIN
						ON OIN.ITEM_CD = SO.ITEM_CD
						/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 조회가 되지 않아. LEFT JOIN 시 조건을 걸었음 */
						and OIN.LINE_TY = 'Y'
		WHERE	/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 조회가 되지 않아. LEFT JOIN 시로 취치를 옮김 */
				/* AND OIN.LINE_TY = 'Y' */
				1 = 1
		AND		NOT(SO.STATUS1 = '980') AND SO.STATUS2 >= '620'
		AND		OIN.SALES_CD3 IN ('DAP11400', 'DAP11500', 'DAP11600', 'DAP11700', 'DAP12400', 'DAP12500', 'DAP12800', 'DAP12900', 'DAP13000')
		AND		SO.ACTUAL_SHIP_DT >= '20240301'
		AND		SO.ACTUAL_SHIP_DT <= '20240930'

;





/* *********** eorder.o_qmsorder.list *********** */
;

/* eorder.o_qmsorder.list */
		SELECT /*+ HASH(table) */ *
		FROM	(
					SELECT
							ROW_NUMBER() OVER( ORDER BY XX.ORDERNO DESC, XX.LINE_NO ASC ) AS ROWNUM
							, XX.*
							,CASE
								WHEN PRE1 = 'Y' AND PRE2 = 'Y' /* 사전입력 완료 */ THEN '사전'
								WHEN PRE1 = 'Y' AND PRE2 = 'N' /* 사전입력중 */ THEN '사전'
								ELSE '사후' /* 사후입력 대상 */
							END AS QMS_STEP
					FROM	(
								SELECT
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
										,CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ORDER_DT, 0,4), '-'), SUBSTRING(ORDER_DT, 5, 2)), '-'), SUBSTRING(ORDER_DT, 7, 2)) AS ORDER_DT
										,CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ACTUAL_SHIP_DT, 0,4), '-'), SUBSTRING(ACTUAL_SHIP_DT, 5, 2)), '-'), SUBSTRING(ACTUAL_SHIP_DT, 7, 2)) AS ACTUAL_SHIP_DT
										,SHIPTO_CD,SHIPTO_NM, RTRIM(CONCAT(ADD1, ADD2)) AS ADDR, ITEM_DESC, LOTN, ORDER_QTY, UNIT, SALESREP_NM
								FROM	qms_salesorder SO
										LEFT JOIN O_ITEM_NEW OIN ON OIN.ITEM_CD = SO.ITEM_CD
									/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 조회가 되지 않아. LEFT JOIN 시 조건을 걸었음 */
												AND OIN.LINE_TY = 'Y'
								WHERE /*2024-09-30 hsg LINE_TY에 'Y'값이 없어 조회가 되지 않아. LEFT JOIN 시로 취치를 옮김 */
										/* AND OIN.LINE_TY = 'Y' */
								AND NO		(SO.STATUS1 = '980') AND SO.STATUS2 >= '620'
								AND		OIN.SALES_CD3 IN ('DAP11400', 'DAP11500', 'DAP11600', 'DAP11700', 'DAP12400', 'DAP12500', 'DAP12800', 'DAP12900', 'DAP13000')
								AND		SO.ACTUAL_SHIP_DT <= '20240930'
								AND		SO.ACTUAL_SHIP_DT >= '20240301'
							) XX
				) S
		 WHERE ROWNUM BETWEEN 151 AND 165
;




/* ***************************************************************************************************************************************** */
/* *********** /admin/order/qmsOrderPopView.lime *********** */


/* *********** eorder.o_qmsorder.getQmsPopMastList *********** */
;

/* eorder.o_qmsorder.getQmsPopMastList */
		SELECT
				A.*
				,CASE WHEN (SELECT COUNT(*) FROM (SELECT QMS_ID,ORDERNO FROM QMS_ORD_DETL GROUP BY QMS_ID,ORDERNO) Q WHERE Q.QMS_ID = A.QMS_ID) > 1 THEN 'N' ELSE 'Y' END AS QMS_SPLIT_YN
				,B.CUST_NM
				, B.ADD1 AS CUST_ADDR
				,dbo.SF_GETQMSBIZNO(B.TAX_ID) AS CUST_BIZ_NO
				,B.ZIP_CD
				,B.SALESREP_CD
				,B.SALESREP_NM
				,B.TEAM_CD
				,B.TEAM_NM
				,B.MAILING_NM
				,dbo.SF_GETQMSACTIVEYN(QMS_ID) AS ACTIVEYN
		FROM	QMS_ORD_MAST A
				LEFT JOIN O_CUSTOMER B ON A.CUST_CD = B.CUST_CD
		WHERE	A.QMS_ID = '20243Q0037'
		AND		A.QMS_SEQ = '1'

;




/* *********** eorder.o_qmsorder.getQmsPopDetlList *********** */
;

/* eorder.o_qmsorder.getQmsPopDetlList */
		SELECT	B.*
		FROM	QMS_ORD_MAST A
				,QMS_ORD_DETL B
		WHERE	A.QMS_ID = B.QMS_ID
		AND		A.QMS_ID = '20243Q0037'
		AND		A.QMS_SEQ = '1'

;




/* *********** eorder.o_qmsorder.getQmsFireproofList *********** */
;

/* eorder.o_qmsorder.getQmsFireproofList */
		SELECT
				A.KEYCODE
				,A.FIREPROOFTYPE
				,FORMAT(A.FIRETIME, '0.#') AS FIRETIME
				,A.FILENAME
				,CEILING((SELECT COUNT(*) FROM O_FIREPROOFMASTER F WHERE F.FIRETIME = A.FIRETIME AND F.ACTIVE = 'Y' ) / 4) AS ROWSPAN_CNT
				,ROW_NUMBER() OVER (PARTITION BY A.FIRETIME ORDER BY A.KEYCODE ASC) AS RNUM
				,ROW_NUMBER() OVER (ORDER BY A.KEYCODE ASC) AS RCNT
				,(SELECT COUNT(F.FIRETIME) FROM O_FIREPROOFMASTER F WHERE F.ACTIVE = 'Y') AS RLAST
				,CASE WHEN DENSE_RANK() OVER (PARTITION BY A.FIRETIME ORDER BY A.KEYCODE DESC) = 1 THEN 'Y' ELSE 'N' END AS LAST_YN
				,CASE WHEN (B.KEYCODE IS NOT NULL AND B.DELETEYN='N') THEN 'Y' ELSE 'N' END AS CHK_YN
		FROM	O_FIREPROOFMASTER A
				LEFT OUTER JOIN QMS_ORD_FRCN B
						ON A.KEYCODE = B.KEYCODE
						AND B.QMS_ID	 = '20243Q0037'
						AND B.QMS_SEQ = '1'
		WHERE	A.ACTIVE = 'Y'
		ORDER BY A.FIRETIME,A.DISPLAYORDER

;





/* ***************************************************************************************************************************************** */
/* *********** /admin/order/getQmsPopDetlGridList.lime *********** */


/* *********** eorder.o_qmsorder.getQmsPopDetlGridList *********** */
;

	/* eorder.o_qmsorder.getQmsPopDetlGridList */
		SELECT /*ROWNUM RR,*/
				XX.* ,B.QMS_ID, B.QMS_SEQ ,B.QMS_REMARK ,B.QMS_DETL_ID ,B.QMS_ID +'-'+ CONVERT(VARCHAR, B.QMS_SEQ) AS QMS_ORD_NO
				,	(
						SELECT	CASE WHEN COUNT(Q.QMS_ORD_QTY) > 0 THEN SUM(Q.QMS_ORD_QTY) ELSE 0 END
						FROM	QMS_ORD_DETL Q , QMS_ORD_MAST M
						WHERE	Q.QMS_ID = M.QMS_ID AND Q.QMS_SEQ = M.QMS_SEQ AND Q.DELETEYN = 'N' AND M.DELETEYN = 'N'
						AND		Q.ORDERNO = XX.ORDERNO AND Q.LINE_NO = XX.LINE_NO AND Q.ITEM_CD = XX.ITEM_CD AND Q.LOTNO = XX.LOTN
					) AS QMS_ORD_BALANCE
				 ,	CASE
						WHEN ISNULL(B.QMS_ORD_QTY,0) = 0
							THEN XX.ORDER_QTY - (
													SELECT	CASE WHEN COUNT(Q.QMS_ORD_QTY) > 0 THEN SUM(Q.QMS_ORD_QTY) ELSE 0 END
													FROM	QMS_ORD_DETL Q , QMS_ORD_MAST M
													WHERE	Q.QMS_ID = M.QMS_ID AND Q.QMS_SEQ = M.QMS_SEQ AND Q.DELETEYN = 'N' AND M.DELETEYN = 'N'
													AND		Q.ORDERNO = XX.ORDERNO AND Q.LINE_NO = XX.LINE_NO AND Q.ITEM_CD = XX.ITEM_CD AND Q.LOTNO = XX.LOTN
												)
						ELSE B.QMS_ORD_QTY
					END AS QMS_ORD_QTY
				,	CASE
						WHEN (SELECT COUNT(*) FROM (SELECT QMS_ID,ORDERNO FROM QMS_ORD_DETL GROUP BY QMS_ID,ORDERNO) Q WHERE Q.QMS_ID = A.QMS_ID) > 1
							THEN 'N'
						ELSE 'Y'
					END AS QMS_SPLIT_YN
		FROM	(
					SELECT
							ORDERNO,LINE_NO ,ITEM_CD,ORDERTY , CUST_PO, CUST_NM,
							CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ORDER_DT, 1,4), '-'), SUBSTRING(ORDER_DT, 5, 2)), '-'), SUBSTRING(ORDER_DT, 7, 2)) AS ORDER_DT,
							CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ACTUAL_SHIP_DT, 1,4), '-'), SUBSTRING(ACTUAL_SHIP_DT, 5, 2)), '-'), SUBSTRING(ACTUAL_SHIP_DT, 7, 2)) AS ACTUAL_SHIP_DT,
							SHIPTO_NM, RTRIM(CONCAT(ADD1, ADD2)) AS ADDR, ITEM_DESC, LOTN, ORDER_QTY, UNIT, SALESREP_NM
					FROM	qms_salesorder SO
				) XX
				,QMS_ORD_MAST A
				,QMS_ORD_DETL B
		WHERE	A.QMS_ID	  = B.QMS_ID
		AND		A.QMS_SEQ  = B.QMS_SEQ
		AND		B.ORDERNO  = XX.ORDERNO
		AND		B.LINE_NO  = XX.LINE_NO
		AND		A.QMS_ID	  = '20243Q0037'
		AND		A.QMS_SEQ  = '1'
;














