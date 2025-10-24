

/* ***************************************************************************************************************************************** */
/* *********** /admin/order/getOrderHeaderListAjax.lime *********** */
/* ***************************************************************************************************************************************** */



/* *********** eorder.o_cust_order_h.cnt *********** */
;

	SELECT	COUNT(*)
	FROM	O_CUST_ORDER_H COH
			LEFT JOIN O_CUSTOMER CU		ON COH.CUST_CD = CU.CUST_CD
			LEFT JOIN O_SHIPTO ST		ON COH.SHIPTO_CD = ST.SHIPTO_CD
			LEFT JOIN O_USER US_SALES	ON CU.SALESREP_CD = US_SALES.USERID
	WHERE	COH.INDATE >= CONVERT(DATE, '2024-10-07')
	AND		COH.INDATE < DATEADD(day, 1,CONVERT(DATE, '2024-12-16'))
	AND		COH.STATUS_CD IN ('00')
;



/* *********** eorder.o_cust_order_h.list *********** */
;

	SELECT	*
	FROM	(
				SELECT
						  ROW_NUMBER() OVER( ORDER BY XX.INDATE DESC ) AS ROWNUM
						, XX.*
				FROM	(
							SELECT
									  COH.*
									, (SELECT USER_NM FROM O_USER WHERE RTRIM(USERID) = RTRIM(COH.USERID)) AS USER_NM
									, CU.CUST_NM
									, ST.SHIPTO_NM
									, US_SALES.USERID AS SALESUSERID, US_SALES.USER_NM AS SALESUSER_NM
									, (SELECT CSUSERID FROM O_CSSALESMAP WHERE SALESUSERID = US_SALES.USERID AND FIXEDYN = 'Y') AS CSUSERID
									, (
											SELECT	SUB1.USER_NM
											FROM	O_USER SUB1, O_CSSALESMAP SUB2
											WHERE	SUB1.USERID = SUB2.CSUSERID
											AND		SUB2.SALESUSERID = US_SALES.USERID
											AND		SUB2.FIXEDYN = 'Y'
									  ) AS CSUSER_NM
									, (SELECT COUNT(*) FROM O_CUST_ORDER_D WHERE REQ_NO = COH.REQ_NO) AS ITEM_CNT
									, (SELECT TOP 1 CONFIRM_DT FROM O_ORDER_CONFIRM_H WHERE REQ_NO = COH.REQ_NO) AS CONFIRM_DT2
									, QM.QMS_TEMP_ID
							FROM O_CUST_ORDER_H COH
							LEFT OUTER JOIN QMS_PRE_MAST QM ON QM.REQ_NO = COH.REQ_NO AND QM.DELETEYN = 'N'
							LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
							LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
							LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
							WHERE  COH.INDATE >= CONVERT(DATE, '2024-10-07')
							AND COH.INDATE < DATEADD(day, 1,CONVERT(DATE, '2024-12-16'))
							AND COH.STATUS_CD IN ('00')
						) XX
			) S
	WHERE	ROWNUM BETWEEN 1 AND 10
;



/* ***************************************************************************************************************************************** */
/* *********** /admin/order/checkOrderEditStatusAjax.lime *********** */
/* ***************************************************************************************************************************************** */


/* *********** eorder.o_cust_order_h.one *********** */
;

	SELECT
			  COH.*
			, (SELECT USER_NM FROM O_USER WHERE RTRIM(USERID) = RTRIM(COH.USERID)) AS USER_NM
			, CU.CUST_NM
			, ST.SHIPTO_NM
			, ST.QUOTE_QT
			, US_SALES.USERID AS SALESUSERID, US_SALES.USER_NM AS SALESUSER_NM
			, (SELECT CSUSERID FROM O_CSSALESMAP WHERE SALESUSERID = US_SALES.USERID AND FIXEDYN = 'Y') AS CSUSERID
			, (
					SELECT	SUB1.USER_NM
					FROM	O_USER SUB1, O_CSSALESMAP SUB2
					WHERE	SUB1.USERID = SUB2.CSUSERID
					AND SUB2.SALESUSERID = US_SALES.USERID
					AND SUB2.FIXEDYN = 'Y'
			  ) AS CSUSER_NM
			, (SELECT CC_NAME FROM COMMONCODE WHERE CC_PARENT='C01' AND CC_CODE = COH.RETURN_CD) AS RETURN_REASON
	FROM	O_CUST_ORDER_H COH
			LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
			LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
			LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
	WHERE	COH.REQ_NO = '101781702411263'
;




/* ***************************************************************************************************************************************** */
/* *********** /admin/order/orderEdit.lime *********** */
/* ***************************************************************************************************************************************** */


/* *********** eorder.o_cust_order_h.one *********** */
;

	SELECT
			  COH.*
			, (SELECT USER_NM FROM O_USER WHERE RTRIM(USERID) = RTRIM(COH.USERID)) AS USER_NM
			, CU.CUST_NM
			, ST.SHIPTO_NM
			, ST.QUOTE_QT
			, US_SALES.USERID AS SALESUSERID, US_SALES.USER_NM AS SALESUSER_NM
			, (SELECT CSUSERID FROM O_CSSALESMAP WHERE SALESUSERID = US_SALES.USERID AND FIXEDYN = 'Y') AS CSUSERID
			, (
					SELECT	SUB1.USER_NM
					FROM	O_USER SUB1, O_CSSALESMAP SUB2
					WHERE	SUB1.USERID = SUB2.CSUSERID
					AND SUB2.SALESUSERID = US_SALES.USERID
					AND SUB2.FIXEDYN = 'Y'
			  ) AS CSUSER_NM
			, (SELECT CC_NAME FROM COMMONCODE WHERE CC_PARENT='C01' AND CC_CODE = COH.RETURN_CD) AS RETURN_REASON
	FROM	O_CUST_ORDER_H COH
			LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
			LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
			LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
	WHERE	COH.REQ_NO = '101781702411263'
;




/* *********** eorder.orderHeaderHistory.oneByRecentList *********** */
;

	SELECT	*
	FROM	(
				SELECT
						ROW_NUMBER() OVER( ORDER BY XX.LINE_NO ASC	 ) AS ROWNUM
						, XX.*
				FROM	(
							SELECT
									COD.*
									, COH.CUST_CD, COH.SHIPTO_CD
									, IT.DESC1, IT.DESC2
									, ITI.*
									, (SELECT COUNT(*) FROM ITEMRECOMMEND WHERE ITR_ITEMCD = COD.ITEM_CD) AS RECOMMEND_ITEM_COUNT
									, CASE WHEN SALES_CD1 = 'A' AND (SALES_CD2 IN ('02','06','US1') OR (SALES_CD2 = '76' AND PLAN_FMLY = 'H15' ) ) THEN 'Y' ELSE 'N' END AS FIREPROOF_YN
							FROM	O_CUST_ORDER_D COD
									LEFT JOIN O_CUST_ORDER_H COH ON COD.REQ_NO = COH.REQ_NO
									LEFT JOIN O_ITEM_NEW IT ON COD.ITEM_CD = IT.ITEM_CD
									LEFT JOIN ITEMINFO ITI ON COD.ITEM_CD = ITI.ITI_ITEMCD
							WHERE	COD.REQ_NO = '101781702411263'
						) XX
			) S
;



/* *********** eorder.o_shipto.one *********** */
;

	SELECT	ST.*
	FROM	O_SHIPTO ST
	WHERE	ST.SHIPTO_CD = '9006047060'
;



/* *********** eorder.plant.list *********** */
;

	SELECT	p.WERKS AS PT_CODE, p.PT_NAME + '(' + p.WERKS + ')' AS PT_NAME
	FROM	PLANT p
	WHERE	p.PT_USE = 'Y'
	ORDER BY p.PT_SORT
;



/* *********** eorder.common.getHolyDayList *********** */
;

	SELECT	*
	FROM	(
				SELECT
						ROW_NUMBER() OVER( ORDER BY (SELECT 1)	) AS ROWNUM
						, XX.*
				FROM	( SELECT YYYYMMDD FROM O_DATE WHERE	 HOLIDAY_YN = 'Y'  ) XX
			) S
;





/* *********** eorder.common.getOrderWeekList *********** */
;

	SELECT	UDC.DRKY, UDC.DRDL01
	FROM	O_F0005 UDC
	ORDER BY UDC.DRKY
;



/* *********** eorder.commonCode.list *********** */
;

	SELECT	*
	FROM	(
				SELECT
						ROW_NUMBER() OVER(ORDER BY CC_SORT1, CC_SORT2, CC_SORT3) AS ROWNUM
						, XX.*
				FROM	(
							SELECT
									CC.* , (SELECT COUNT(*) FROM COMMONCODE WHERE CC_PARENT = CC.CC_CODE) CHILD_COUNT
							FROM	COMMONCODE CC
							WHERE	ISNULL(CC_PARENT, ' ') =  ISNULL('C05', ' ')
							AND		CC_USE = 'Y'
						) XX
			) S
;




/* ***************************************************************************************************************************************** */
/* *********** /admin/base/getItemMcuListAjax.lime *********** */
/* ***************************************************************************************************************************************** */



/* *********** eorder.o_item_new.listItemMcu *********** */
;

	SELECT	ITM.ITEM_CD
	FROM	O_ITEM_MCU ITM
			LEFT JOIN O_ITEM_NEW OIN  ON OIN.ITEM_CD = ITM.ITEM_CD
	WHERE	(UPPER(OIN.STOCK_TY) != 'N' OR OIN.STOCK_TY IS NULL)
	AND		ITM.ITEM_MCU = '4635'
	AND		REPLACE(ITM.DESC1,' ','') = REPLACE('방화 12.5*900*1800 평보드',' ','')
	GROUP BY ITM.ITEM_CD
;




/* ***************************************************************************************************************************************** */
/* *********** /admin/base/getItemStockAjax.lime *********** */
/* ***************************************************************************************************************************************** */




/* *********** eorder.o_item_new.one *********** */
;

	SELECT	IT.*, ITI.*
	FROM	O_ITEM_NEW IT
			LEFT JOIN ITEMINFO ITI ON IT.ITEM_CD = ITI.ITI_ITEMCD
	WHERE	ITEM_CD = '792119'
;






/* *********** eorder.o_item_new.one *********** */
;

	SELECT	IT.*, ITI.*
	FROM	O_ITEM_NEW IT
			LEFT JOIN ITEMINFO ITI ON IT.ITEM_CD = ITI.ITI_ITEMCD
	WHERE	ITEM_CD = '786204'
;



/* ***************************************************************************************************************************************** */
/* ***********	/admin/base/getItemMcuListAjax.lime *********** */
/* ***************************************************************************************************************************************** */



/* *********** eorder.o_item_new.listItemMcu *********** */
;


	SELECT	ITM.ITEM_CD
	FROM	O_ITEM_MCU ITM
			LEFT JOIN O_ITEM_NEW OIN  ON OIN.ITEM_CD = ITM.ITEM_CD
	WHERE	(UPPER(OIN.STOCK_TY) != 'N' OR OIN.STOCK_TY IS NULL)
	AND		ITM.ITEM_MCU = '4636'
	AND		REPLACE(ITM.DESC1,' ','') = REPLACE('일반 9.5*900*1800 평보드',' ','')
	GROUP BY ITM.ITEM_CD
;




/* ***************************************************************************************************************************************** */
/* *********** /admin/base/getItemStockAjax.lime *********** */



/* *********** eorder.o_item_new.one *********** */
;

	SELECT	IT.*, ITI.*
	FROM	O_ITEM_NEW IT
			LEFT JOIN ITEMINFO ITI ON IT.ITEM_CD = ITI.ITI_ITEMCD
	WHERE	ITEM_CD = '792119'
;




/* ***************************************************************************************************************************************** */
/* *********** /admin/order/getPostalCodeCount.lime *********** */
/* ***************************************************************************************************************************************** */



/* *********** eorder.o_postalcode_sap.cnt *********** */
;

	SELECT	COUNT(*)
	FROM	O_POSTALCODE_SAP S
	WHERE	S.ZIP_CD = '17045'
	AND		S.USE_F IN ('Y')
;




/* ***************************************************************************************************************************************** */
/* *********** /admin/order/insertOrderConfirmAjax.lime *********** */
/* ***************************************************************************************************************************************** */



/* ***********	eorder.o_shipto.one *********** */
;

	SELECT	ST.*
	FROM	O_SHIPTO ST
	WHERE	ST.SHIPTO_CD = '9006047060'
;




/* *********** eorder.o_cust_order_h.one *********** */
;

	SELECT
			  COH.*
			, (SELECT USER_NM FROM O_USER WHERE RTRIM(USERID) = RTRIM(COH.USERID)) AS USER_NM
			, CU.CUST_NM
			, ST.SHIPTO_NM
			, ST.QUOTE_QT
			, US_SALES.USERID AS SALESUSERID, US_SALES.USER_NM AS SALESUSER_NM
			, (SELECT CSUSERID FROM O_CSSALESMAP WHERE SALESUSERID = US_SALES.USERID AND FIXEDYN = 'Y') AS CSUSERID
			, (SELECT SUB1.USER_NM FROM O_USER SUB1, O_CSSALESMAP SUB2 WHERE SUB1.USERID = SUB2.CSUSERID AND SUB2.SALESUSERID = US_SALES.USERID AND SUB2.FIXEDYN = 'Y') AS CSUSER_NM
			, (SELECT CC_NAME FROM COMMONCODE WHERE CC_PARENT='C01' AND CC_CODE = COH.RETURN_CD) AS RETURN_REASON
	FROM	O_CUST_ORDER_H COH
			LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
			LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
			LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
	WHERE	COH.REQ_NO = '101781702411263'
;





/* *********** eorder.orderHeaderHistory.oneByRecentList *********** */
;

	SELECT	TOP 1 *
	FROM	ORDERHEADERHISTORY
	WHERE	OHH_REQNO = '101781702411263'
	ORDER BY OHH_SEQ DESC
;



/* *********** eorder.o_order_confirm_h.in *********** */
;

	INSERT INTO O_ORDER_CONFIRM_H(
				REQ_NO
				, CUST_PO
				, CUST_CD
				, USERID
				, STATUS_CD
				, ZIP_CD
				, ADD1
				, ADD2
				, REQUEST_DT
				, COMPANY_CD
				, TEL1
				, TEL2
				, BUILDING_TY
				, SHIPTO_CD
				, TRANS_TY
				, REQUEST_TIME
				, REMARK
				, CONFIRM_DT
				, PICKING_DT
				, RECEIVER
				, CONFIRMID
				, CANCEL_DT
				, CUSTOMMATTER
				, DUMMY
				, QUOTE_QT
				, INTERFACE_DT
				, INSERT_DT
				, INSERTID
			)VALUES(
				'101781702411263'
				, '101781702411263-1'
				, '10178170'
				, 'admin'
				, '07'
				, '17045'
				, '경기 용인시 처인구 역북동 89-25번지 일원'
				, '상세주소'
				, '20241227'
				, '4635'
				, '01023456789'
				, ''
				, NULL
				, '9006047060'
				, 'AA'
				, '1100'
				, '요청사항'
				, SUBSTRING(CONVERT(CHAR(19), GETDATE(), 20), 1, 16)
				, '20241217'
				, '인수자'
				, 'sorin123'
				, NULL
				, ''
				, NULL
				, '20490505	 '
				, NULL
				, SUBSTRING(CONVERT(CHAR(19), GETDATE(), 20), 1, 16)
				, 'sorin123'
			)
;



/* *********** eorder.o_order_confirm_d.in *********** */
;

	INSERT INTO O_ORDER_CONFIRM_D(
			CUST_PO
			, LINE_NO
			, ITEM_CD
			, UNIT
			, QUANTITY
			, PRICE
			, INSERT_DT
			, INSERTID
			, DUMMY
			, WEEK
			, OCD_RETURNCD
			, OCD_RETURNMSG
			, OCD_STATUSCD
		)VALUES(
			'101781702411263-1'
			, 1000
			, '792119'
			, 'PC'
			, '1340'
			, '0'
			, SUBSTRING(CONVERT(CHAR(19), GETDATE(), 20), 1, 16)
			, 'sorin123'
			, ''
			, '503		 '
			, ''
			, ''
			, ''
		)
;




	INSERT INTO O_ORDER_CONFIRM_D(
			CUST_PO
			, LINE_NO
			, ITEM_CD
			, UNIT
			, QUANTITY
			, PRICE
			, INSERT_DT
			, INSERTID
			, DUMMY
			, WEEK
			, OCD_RETURNCD
			, OCD_RETURNMSG
			, OCD_STATUSCD
		)VALUES(
			'101781702411263-1'
			, 2000
			, '786204'
			, 'PC'
			, '500'
			, '0'
			, SUBSTRING(CONVERT(CHAR(19), GETDATE(), 20), 1, 16)
			, 'sorin123'
			, ''
			, '503		 '
			, ''
			, ''
			, ''
		)
;




/* *********** eorder.o_order_confirm_h.in *********** */
;

	INSERT INTO O_ORDER_CONFIRM_H(
			REQ_NO
			, CUST_PO
			, CUST_CD
			, USERID
			, STATUS_CD
			, ZIP_CD
			, ADD1
			, ADD2
			, REQUEST_DT
			, COMPANY_CD
			, TEL1
			, TEL2
			, BUILDING_TY
			, SHIPTO_CD
			, TRANS_TY
			, REQUEST_TIME
			, REMARK
			, CONFIRM_DT
			, PICKING_DT
			, RECEIVER
			, CONFIRMID
			, CANCEL_DT
			, CUSTOMMATTER
			, DUMMY
			, QUOTE_QT
			, INTERFACE_DT
			, INSERT_DT
			, INSERTID
		)VALUES(
			'101781702411263'
			, '101781702411263-2'
			, '10178170'
			, 'admin'
			, '07'
			, '17045'
			, '경기 용인시 처인구 역북동 89-25번지 일원'
			, '상세주소'
			, '20241227'
			, '4636'
			, '01023456789'
			, ''
			, NULL
			, '9006047060'
			, 'AA'
			, '1100'
			, '요청사항'
			, SUBSTRING(CONVERT(CHAR(19), GETDATE(), 20), 1, 16)
			, '20241217'
			, '인수자'
			, 'sorin123'
			, NULL
			, ''
			, NULL
			, '20490505	 '
			, NULL
			, SUBSTRING(CONVERT(CHAR(19), GETDATE(), 20), 1, 16)
			, 'sorin123'
		)
;




/* *********** eorder.o_order_confirm_d.in *********** */
;

INSERT INTO O_ORDER_CONFIRM_D(
			CUST_PO
			, LINE_NO
			, ITEM_CD
			, UNIT
			, QUANTITY
			, PRICE
			, INSERT_DT
			, INSERTID
			, DUMMY
			, WEEK
			, OCD_RETURNCD
			, OCD_RETURNMSG
			, OCD_STATUSCD
		)VALUES(
			'101781702411263-2'
			, 1000
			, '792119'
			, 'PC'
			, '100'
			, '0'
			, SUBSTRING(CONVERT(CHAR(19), GETDATE(), 20), 1, 16)
			, 'sorin123'
			, ''
			, '503		 '
			, ''
			, ''
			, ''
		)
;



/* *********** eorder.o_cust_order_h.up *********** */
;

	UPDATE	O_CUST_ORDER_H
	SET		STATUS_CD = '07',
			UPDATEID = 'sorin123',
			UPDATE_DT = SUBSTRING(CONVERT(CHAR(19), GETDATE(), 20), 1, 16),
			MODATE = GETDATE()
	WHERE	REQ_NO = '101781702411263'
;




/* *********** eorder.orderHeaderHistory.in *********** */
;

	INSERT INTO ORDERHEADERHISTORY(
			OHH_SEQ,
			OHH_REQNO,
			OHH_REQNOREF,
			OHH_STATUSCD,
			OHH_MEMO,
			OHH_INID,
			OHH_INDATE
		)VALUES(
			627017
			, '101781702411263'
			, NULL
			, '07'
			, '주문확정(분리)'
			, 'sorin123'
			, GETDATE()
		)
;




/* *********** eorder.o_user.listForAppPush *********** */
;

	SELECT	*
	FROM	O_USER
	WHERE	( USER_APPPUSHKEY IS NOT NULL AND ISNULL(USER_APPPUSHKEY, ' ') != ' ' )
	AND		USERID = 'admin'
	AND		USER_APPPUSHYN1 = 'Y'
;





/* ***************************************************************************************************************************************** */
/* *********** /admin/order/orderList.lime *********** */
/* ***************************************************************************************************************************************** */



/* *********** eorder.o_user.list *********** */
;

	SELECT	*
	FROM	(
				SELECT
						ROW_NUMBER() OVER( ORDER BY USER_SORT2 ASC	 ) AS ROWNUM
						, XX.*
				FROM	(
							SELECT	LIST.*
							FROM	(
										SELECT
												US.*, CU.CUST_NM
												,	CASE
														WHEN (US.AUTHORITY = 'SH' OR US.AUTHORITY = 'SM' OR US.AUTHORITY = 'SR')
															THEN	(
																		SELECT
																				(
																					SELECT	USER_NM
																					FROM	O_USER
																					WHERE USERID = CSM.CSUSERID
																				)
																		FROM	O_CSSALESMAP CSM
																		WHERE CSM.SALESUSERID = US.USERID
																		AND CSM.FIXEDYN = 'Y'
																	)
														ELSE ''
													END CS_SALESUSER
												,	CASE
														WHEN (US.AUTHORITY = 'SH' OR US.AUTHORITY = 'SM' OR US.AUTHORITY = 'SR')
															THEN (SELECT COUNT(*) FROM O_CUSTOMER WHERE SALESREP_CD = US.USERID)
														ELSE -1
													END CUSTOMER_CNT
										FROM	O_USER US
												LEFT JOIN O_CUSTOMER CU ON CU.CUST_CD = US.CUST_CD
										WHERE	USERID NOT IN ('9', '1', '2','3', '4', '5', '6')
										AND		US.AUTHORITY = 'CS'
									) LIST
						) XX
			) S
;





/* ***************************************************************************************************************************************** */
/* *********** /admin/order/getOrderHeaderListAjax.lime *********** */
/* ***************************************************************************************************************************************** */



/* *********** eorder.o_cust_order_h.cnt *********** */
;

	SELECT	COUNT(*)
	FROM	O_CUST_ORDER_H COH
			LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
			LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
			LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
	WHERE	COH.INDATE >= CONVERT(DATE, '2024-10-07')
	AND		COH.INDATE < DATEADD(day, 1,CONVERT(DATE, '2024-12-16'))
	AND		COH.STATUS_CD IN ('00')
;




/* *********** eorder.o_cust_order_h.list *********** */
;

	SELECT	*
	FROM	(
				SELECT
						ROW_NUMBER() OVER( ORDER BY XX.INDATE DESC ) AS ROWNUM
						, XX.*
				FROM	(
							SELECT
									  COH.*
									, (SELECT USER_NM FROM O_USER WHERE RTRIM(USERID) = RTRIM(COH.USERID)) AS USER_NM
									, CU.CUST_NM
									, ST.SHIPTO_NM
									, US_SALES.USERID AS SALESUSERID, US_SALES.USER_NM AS SALESUSER_NM
									, (SELECT CSUSERID FROM O_CSSALESMAP WHERE SALESUSERID = US_SALES.USERID AND FIXEDYN = 'Y') AS CSUSERID
									, (SELECT SUB1.USER_NM FROM O_USER SUB1, O_CSSALESMAP SUB2 WHERE SUB1.USERID = SUB2.CSUSERID AND SUB2.SALESUSERID = US_SALES.USERID AND SUB2.FIXEDYN = 'Y') AS CSUSER_NM
									, (SELECT COUNT(*) FROM O_CUST_ORDER_D WHERE REQ_NO = COH.REQ_NO) AS ITEM_CNT
									, (SELECT TOP 1 CONFIRM_DT FROM O_ORDER_CONFIRM_H WHERE REQ_NO = COH.REQ_NO) AS CONFIRM_DT2
									, QM.QMS_TEMP_ID
							FROM	O_CUST_ORDER_H COH
									LEFT OUTER JOIN QMS_PRE_MAST QM ON QM.REQ_NO = COH.REQ_NO AND QM.DELETEYN = 'N'
									LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
									LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
									LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
							WHERE	COH.INDATE >= CONVERT(DATE, '2024-10-07')
							AND		COH.INDATE < DATEADD(day, 1,CONVERT(DATE, '2024-12-16'))
							AND		COH.STATUS_CD IN ('00')
						) XX
			) S
	WHERE	ROWNUM BETWEEN 1 AND 10
;





/* ***************************************************************************************************************************************** */
/* ***************************************************************************************************************************************** */
/* ***************************************************************************************************************************************** */


SELECT
		*
FROM	O_ORDER_CONFIRM_H A
WHERE	1 = 1
AND		REQ_NO = '101781702411263'
--GROUP BY
ORDER BY REQ_NO DESC
;


