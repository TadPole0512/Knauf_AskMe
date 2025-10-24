


SELECT
		COH.*
		,	(
				SELECT
						USER_NM
				FROM	O_USER
				WHERE	RTRIM(USERID) = RTRIM(COH.USERID)
			) AS USER_NM
		, CU.CUST_NM
		, ST.SHIPTO_NM
		--	, ST.QUOTE_QT
		, US_SALES.USERID AS SALESUSERID
		, US_SALES.USER_NM AS SALESUSER_NM
		,	(
				SELECT
						CSUSERID
				FROM	O_CSSALESMAP
				WHERE	SALESUSERID = US_SALES.USERID
				AND		FIXEDYN = 'Y'
			) AS CSUSERID
		,	(
				SELECT
						SUB1.USER_NM
				FROM	O_USER SUB1, O_CSSALESMAP SUB2
				WHERE	SUB1.USERID = SUB2.CSUSERID
				AND		SUB2.SALESUSERID = US_SALES.USERID
				AND		SUB2.FIXEDYN = 'Y'
			) AS CSUSER_NM
		,	(
				SELECT
						CC_NAME
				FROM	COMMONCODE
				WHERE	CC_PARENT = 'C01'
				AND		CC_CODE = COH.RETURN_CD
			) AS RETURN_REASON
FROM	O_CUST_ORDER_H COH
		LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
		LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
		LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
WHERE	COH.REQ_NO =  '101448132306161'
;





SELECT
		COH.*
		, ( SELECT USER_NM FROM O_USER WHERE RTRIM(USERID) = RTRIM(COH.USERID) ) AS USER_NM
		, CU.CUST_NM
		, ST.SHIPTO_NM
		--	, ST.QUOTE_QT
		, US_SALES.USERID AS SALESUSERID
		, US_SALES.USER_NM AS SALESUSER_NM
		, ( SELECT CSUSERID FROM O_CSSALESMAP WHERE SALESUSERID = US_SALES.USERID AND FIXEDYN = 'Y' ) AS CSUSERID
		, ( SELECT SUB1.USER_NM FROM O_USER SUB1, O_CSSALESMAP SUB2 WHERE SUB1.USERID = SUB2.CSUSERID AND SUB2.SALESUSERID = US_SALES.USERID AND SUB2.FIXEDYN = 'Y' ) AS CSUSER_NM
		, ( SELECT CC_NAME FROM COMMONCODE WHERE CC_PARENT = 'C01' AND CC_CODE = COH.RETURN_CD ) AS RETURN_REASON
FROM	O_CUST_ORDER_H COH
		LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
		LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
		LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
WHERE	COH.REQ_NO =  '101448132306161'
;




/* ***************************************************************************************************************************************** */
/* *********** O_SHIPTO 테이블에 컬럼 추가 *********** */

-- ALTER TABLE dbo.O_SHIPTO ADD DIST_CLASS VARCHAR(5) NULL;
-- ALTER TABLE dbo.O_SHIPTO ADD QUOTE_QT VARCHAR(10) NULL;
-- ALTER TABLE dbo.O_SHIPTO ADD ANGDT VARCHAR(8) DEFAULT NULL NULL;
-- ALTER TABLE dbo.O_SHIPTO ADD BNDDT VARCHAR(8) DEFAULT NULL NULL;
-- ALTER TABLE dbo.O_SHIPTO ADD CUST_CD VARCHAR(10) NULL;


-- C:\GitHub\Knauf_Eorder_JDE\src\main\java\com\limenets\eorder\ctrl\admin\OrderCtrl.java



SELECT
		*
FROM	O_SHIPTO A
WHERE	1 = 1
--AND		
--GROUP BY 
--ORDER BY 
;







/* ***************************************************************************************************************************************** */
/* *********** /eorder/admin/order/orderView.lime *********** */

-- Error querying database.  Cause: com.microsoft.sqlserver.jdbc.SQLServerException: The multi-part identifier "COD.LINE_NO" could not be bound.

/* *********** SELECT *********** */
;

/*  eorder.o_cust_order_d.list */



SELECT
		*
FROM	(
			SELECT
					ROW_NUMBER() OVER( ORDER BY XX.LINE_NO ASC ) AS ROWNUM , XX.* -- 별칭 수정(COD/XX)
			FROM	(
						SELECT
								COD.* , COH.CUST_CD , COH.SHIPTO_CD , IT.DESC1 , IT.DESC2 , ITI.*
								, ( SELECT COUNT(*) FROM ITEMRECOMMEND WHERE ITR_ITEMCD = COD.ITEM_CD ) AS RECOMMEND_ITEM_COUNT
								, CASE
										WHEN SALES_CD1 = 'A' AND ( SALES_CD2 IN ( '02', '06', 'US1' ) OR ( SALES_CD2 = '76' AND PLAN_FMLY = 'H15' ) ) THEN 'Y'
										ELSE 'N'
								END AS FIREPROOF_YN
						FROM	O_CUST_ORDER_D COD
								LEFT JOIN O_CUST_ORDER_H COH ON COD.REQ_NO = COH.REQ_NO
								LEFT JOIN O_ITEM_NEW IT ON COD.ITEM_CD = IT.ITEM_CD
								LEFT JOIN ITEMINFO ITI ON COD.ITEM_CD = ITI.ITI_ITEMCD
						WHERE	COD.REQ_NO = ?
			) XX
	) S
;



SELECT
		*
FROM	O_CUST_ORDER_D A
WHERE	1 = 1
--AND		
--GROUP BY 
--ORDER BY 
;





/* ***************************************************************************************************************************************** */
/* *********** /admin/order/orderView.lime *********** */

-- Error querying database.  Cause: com.microsoft.sqlserver.jdbc.SQLServerException: Invalid column name 'WERKS'.


/* *********** eorder.o_order_confirm_d.list *********** */
;

SELECT
		*
FROM	(
			SELECT
					ROW_NUMBER() OVER( ORDER BY XX.CUST_PO ASC , XX.LINE_NO ASC ) AS ROWNUM , XX.* -- -- 별칭 수정(OCD/XX)
			FROM	(
						SELECT
								OCD.* , OCH.REQ_NO , OCH.COMPANY_CD , OCH.PICKING_DT , OCH.STATUS_CD , OCH.CUSTOMMATTER , OCH.REQUEST_DT
								, OCH.REQUEST_TIME , OCH.REMARK , OST.QUOTE_QT
								, ( SELECT PT_NAME FROM PLANT WHERE WERKS = OCH.COMPANY_CD ) AS PT_NAME
								, ( SELECT DESC1 FROM O_ITEM_NEW WHERE ITEM_CD = OCD.ITEM_CD ) AS ITEM_NAME
								, ( SELECT QUANTITY FROM O_CUST_ORDER_D WHERE REQ_NO = OCH.REQ_NO AND LINE_NO = OCD.LINE_NO ) AS COD_QUANTITY
								, ( SELECT CC_NAME FROM COMMONCODE WHERE CC_PARENT = 'C01' AND CC_CODE = OCD.OCD_RETURNCD ) AS ITEM_RETURN_REASON
								, UDC.DRDL01 AS DRDL01 , UDC.DRDL01 AS ROUTE , SUBSTRING(UDC.DRSPHD, 3, 2) AS DRSPHD
						FROM	O_ORDER_CONFIRM_D OCD
						LEFT JOIN O_ORDER_CONFIRM_H OCH ON OCD.CUST_PO = OCH.CUST_PO
						LEFT JOIN O_F0005 UDC ON UDC.DRKY = OCD.WEEK
						LEFT JOIN O_SHIPTO OST ON OCH.SHIPTO_CD = OST.SHIPTO_CD
						WHERE OCH.REQ_NO = '15402301307'
					) XX
		) S
;



/* *********** 컬럼 추가 *********** */
;

-- ALTER TABLE dbo.PLANT ADD WERKS VARCHAR(4) DEFAULT NULL NULL;

-- CREATE  UNIQUE NONCLUSTERED INDEX SYS_C005164 ON dbo.PLANT (  WERKS ASC  )

-- C:\GitHub\Knauf_Eorder_JDE\src\main\java\com\limenets\eorder\svc\OrderSvc.java
















































































































































































































