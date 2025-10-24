

/* ***************************************************************************************************************************************** */
/* *********** 웹주문현황 목록 : https://eordertest.knaufapac.kr/eorder/admin/order/orderList.lime *********** */
/* ***************************************************************************************************************************************** */


/* ***************************************************************************************************************************************** */
/* *********** /admin/order/getOrderHeaderListAjax.lime *********** */


/* *********** 2025-01-13 이전 eorder.o_cust_order_h.cnt *********** */
;

	-- SELECT	COUNT(*)
	  -- FROM	O_CUST_ORDER_H COH
			-- LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
			-- LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
			-- LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
	 -- WHERE	COH.INDATE >= CONVERT(DATE, '2025-01-09')
	   -- AND	COH.INDATE < DATEADD(day, 1,CONVERT(DATE, '2025-01-13'))
	   -- AND	COH.STATUS_CD IN ('00','01','02','03','05','07')
	   -- AND	COH.STATUS_CD IN ('00')
-- ;



/* *********** 2025-01-13 eorder.o_cust_order_h.cnt *********** */
;

	SELECT	COUNT(*)
	  FROM	O_CUST_ORDER_H COH
			LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
			LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
			LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
			LEFT JOIN O_SALESORDER OS ON COH.REQ_NO = OS.CUST_PO
	 WHERE	COH.INDATE >= CONVERT(DATE, '2025-01-09')
	   AND	COH.INDATE < DATEADD(day, 1,CONVERT(DATE, '2025-01-13'))
	   -- AND	COH.STATUS_CD IN ('00','01','02','03','05','07')
	   AND	COH.STATUS_CD IN ('00')
;




/* *********** 2025-01-13 이전 eorder.o_cust_order_h.list *********** */
;

	SELECT	*
	  FROM	(
				SELECT
						ROW_NUMBER() OVER( ORDER BY XX.INDATE DESC ) AS ROWNUM
						, XX.*
				  FROM	(
							SELECT	COH.*
									, (SELECT USER_NM FROM O_USER WHERE RTRIM(USERID) = RTRIM(COH.USERID)) AS USER_NM
									, CU.CUST_NM
									, ST.SHIPTO_NM
									, US_SALES.USERID AS SALESUSERID, US_SALES.USER_NM AS SALESUSER_NM
									, (SELECT CSUSERID FROM O_CSSALESMAP WHERE SALESUSERID = US_SALES.USERID AND FIXEDYN = 'Y') AS CSUSERID
									,	(
											SELECT	SUB1.USER_NM
											  FROM	O_USER SUB1, O_CSSALESMAP SUB2
											 WHERE	SUB1.USERID = SUB2.CSUSERID
											   AND	SUB2.SALESUSERID = US_SALES.USERID
											   AND	SUB2.FIXEDYN = 'Y'
										) AS CSUSER_NM
									, (SELECT COUNT(*) FROM O_CUST_ORDER_D WHERE REQ_NO = COH.REQ_NO) AS ITEM_CNT
									, (SELECT TOP 1 CONFIRM_DT FROM O_ORDER_CONFIRM_H WHERE REQ_NO = COH.REQ_NO) AS CONFIRM_DT2
 									, QM.QMS_TEMP_ID
							  FROM	O_CUST_ORDER_H COH
									LEFT OUTER JOIN QMS_PRE_MAST QM ON QM.REQ_NO = COH.REQ_NO AND QM.DELETEYN = 'N'
									LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
									LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
									LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
							 WHERE	COH.INDATE >= CONVERT(DATE, '2025-01-09')
							   AND	COH.INDATE < DATEADD(day, 1,CONVERT(DATE, '2025-01-13'))
							   -- AND	COH.STATUS_CD IN ('00','01','02','03','05','07')
							   AND	COH.STATUS_CD IN ('00')
						) XX
			) S
	 WHERE	ROWNUM BETWEEN 1 AND 10
;




/* *********** 2025-01-13 eorder.o_cust_order_h.list *********** */
;

	SELECT	*
	  FROM	(
				SELECT
						ROW_NUMBER() OVER( ORDER BY XX.INDATE DESC ) AS ROWNUM
						, XX.*
				  FROM	(
							SELECT	COH.*
									, (SELECT USER_NM FROM O_USER WHERE RTRIM(USERID) = RTRIM(COH.USERID)) AS USER_NM
									, CU.CUST_NM
									, ST.SHIPTO_NM
--									, US_SALES.USERID AS SALESUSERID, US_SALES.USER_NM AS SALESUSER_NM
									, OS.SALESREP_CD AS SALESUSERID, OS.SALESREP_NM AS SALESUSER_NM
									, (SELECT CSUSERID FROM O_CSSALESMAP WHERE SALESUSERID = US_SALES.USERID AND FIXEDYN = 'Y') AS CSUSERID
									,	(
											SELECT	SUB1.USER_NM
											  FROM	O_USER SUB1, O_CSSALESMAP SUB2
											 WHERE	SUB1.USERID = SUB2.CSUSERID
											   AND	SUB2.SALESUSERID = US_SALES.USERID
											   AND	SUB2.FIXEDYN = 'Y'
										) AS CSUSER_NM
									, (SELECT COUNT(*) FROM O_CUST_ORDER_D WHERE REQ_NO = COH.REQ_NO) AS ITEM_CNT
									, (SELECT TOP 1 CONFIRM_DT FROM O_ORDER_CONFIRM_H WHERE REQ_NO = COH.REQ_NO) AS CONFIRM_DT2
 									, QM.QMS_TEMP_ID
							  FROM	O_CUST_ORDER_H COH
									LEFT OUTER JOIN QMS_PRE_MAST QM ON QM.REQ_NO = COH.REQ_NO AND QM.DELETEYN = 'N'
									LEFT JOIN O_CUSTOMER CU ON COH.CUST_CD = CU.CUST_CD
									LEFT JOIN O_SHIPTO ST ON COH.SHIPTO_CD = ST.SHIPTO_CD
									LEFT JOIN O_USER US_SALES ON CU.SALESREP_CD = US_SALES.USERID
									LEFT JOIN O_SALESORDER OS ON COH.REQ_NO = OS.CUST_PO
							 WHERE	COH.INDATE >= CONVERT(DATE, '2025-01-09')
							   AND	COH.INDATE < DATEADD(day, 1,CONVERT(DATE, '2025-01-13'))
							   -- AND	COH.STATUS_CD IN ('00','01','02','03','05','07')
							   AND	COH.STATUS_CD IN ('00')
						) XX
			) S
	 WHERE	ROWNUM BETWEEN 1 AND 10
;




/* ***************************************************************************************************************************************** */


SELECT
		*
  FROM	O_CUST_ORDER_H A
 WHERE	1 = 1
--   AND
--GROUP BY
--ORDER BY
;


SELECT
		SALESREP_CD, SALESREP_NM
  FROM	O_SALESORDER os
 WHERE	1 = 1
   AND	SALESREP_NM IS NOT NULL
   AND	os.CUST_PO LIKE '101726652501091%'
--GROUP BY
--ORDER BY
;



SELECT
		*
  FROM	o_cust_order_h A
 WHERE	1 = 1
--   AND	SALESREP_CD IS NOT NULL
--GROUP BY
--ORDER BY
;


SELECT *
  FROM o_order_confirm_h
 WHERE cust_po = '100220402501201'
;

SELECT
		*
  FROM	o_customer A
 WHERE	1 = 1
--   AND	
--GROUP BY 
--ORDER BY 
;


SELECT	*
  FROM	o_order_confirm_h oc
		LEFT OUTER JOIN o_cust_order_h co ON oc.REQ_NO = co.REQ_NO
 WHERE	oc.cust_po = '100220402501201'
;




/* ***************************************************************************************************************************************** */
/* *********** 2025-01-20 E-order 웹주문현황 데이터 수정 요청 *********** */



/* *********** 2025-01-20 eorder.o_cust_order_h.cnt *********** */
;

	SELECT	Count(*)
	  FROM	o_cust_order_h COH
			LEFT JOIN o_customer CU ON COH.cust_cd = CU.cust_cd
			LEFT JOIN o_shipto ST ON COH.shipto_cd = ST.shipto_cd
			LEFT JOIN o_user US_SALES ON CU.salesrep_cd = US_SALES.userid
	 WHERE	COH.req_no LIKE '%' + '100220402501201' + '%'
	   AND	COH.indate >= CONVERT(DATE, '2025-01-16')
	   AND	COH.indate < Dateadd(day, 1, CONVERT(DATE, '2025-01-22'))
	   AND	COH.status_cd IN ( '00', '01', '02', '03', '05', '07' )
	--    AND	COH.STATUS_CD IN ('05')
;




/* *********** 2025-01-20 이전 eorder.o_cust_order_h.list *********** */
;

-- SELECT	*
--   FROM	(
-- 			SELECT	Row_number() OVER( ORDER BY XX.indate DESC ) AS ROWNUM,
-- 					XX.*
-- 			FROM	(
-- 						SELECT	COH.*,
-- 								( SELECT user_nm FROM   o_user WHERE  Rtrim(userid) = Rtrim(COH.userid) ) AS USER_NM,
-- 								CU.cust_nm,
-- 								ST.shipto_nm,
-- 								CU.salesrep_cd AS SALESUSERID,
-- 								CU.salesrep_nm AS SALESUSER_NM,
-- 								( SELECT csuserid FROM o_cssalesmap WHERE salesuserid = US_SALES.userid AND fixedyn = 'Y') AS CSUSERID,
-- 								(
-- 									SELECT	SUB1.user_nm
-- 									  FROM 	o_user SUB1, o_cssalesmap SUB2
-- 									 WHERE	SUB1.userid = SUB2.csuserid
-- 									   AND	SUB2.salesuserid = US_SALES.userid
-- 									   AND	SUB2.fixedyn = 'Y'
-- 								) AS CSUSER_NM,
-- 								( SELECT Count(*) FROM o_cust_order_d WHERE req_no = COH.req_no ) AS ITEM_CNT,
-- 								( SELECT TOP 1 confirm_dt FROM o_order_confirm_h WHERE req_no = COH.req_no ) AS CONFIRM_DT2 ,
-- 								QM.qms_temp_id
-- 						  FROM	o_cust_order_h COH
-- 								LEFT OUTER JOIN qms_pre_mast QM ON QM.req_no = COH.req_no AND QM.deleteyn = 'N'
-- 								LEFT JOIN o_customer CU ON COH.cust_cd = CU.cust_cd
-- 								LEFT JOIN o_shipto ST ON COH.shipto_cd = ST.shipto_cd
-- 								LEFT JOIN o_user US_SALES ON CU.salesrep_cd = US_SALES.userid
-- 						 WHERE	COH.req_no LIKE '%' + '100220402501201' + '%'
-- 						   AND	COH.indate >= CONVERT(DATE, '2025-01-16')
-- 						   AND	COH.indate < Dateadd(day, 1, CONVERT(DATE, '2025-01-22'))
-- 						   AND	COH.status_cd IN ( '00', '01', '02', '03', '05', '07' )
-- 					) XX
-- 		) S
--  WHERE	rownum BETWEEN 1 AND 10
-- ;





/* *********** 2025-01-20 eorder.o_cust_order_h.list *********** */
;

SELECT	*
  FROM	(
			SELECT	Row_number() OVER( ORDER BY XX.indate DESC ) AS ROWNUM,
					XX.*
			FROM	(
						SELECT	COH.*,
								( SELECT user_nm FROM   o_user WHERE  Rtrim(userid) = Rtrim(COH.userid) ) AS USER_NM,
								CU.cust_nm,
								ST.shipto_nm,
--								CU.salesrep_cd AS SALESUSERID,
--								CU.salesrep_nm AS SALESUSER_NM,
								CASE
									WHEN ( SELECT salesrep_cd FROM o_order_confirm_h oc WHERE COH.REQ_NO = oc.REQ_NO) IS NULL THEN CU.salesrep_cd
									ELSE ( SELECT salesrep_cd FROM o_order_confirm_h oc WHERE COH.REQ_NO = oc.REQ_NO)
								END AS SALESUSERID,
								CASE
									WHEN ( SELECT salesrep_nm FROM o_order_confirm_h oc WHERE COH.REQ_NO = oc.REQ_NO) IS NULL THEN CU.salesrep_nm
									ELSE ( SELECT salesrep_nm FROM o_order_confirm_h oc WHERE COH.REQ_NO = oc.REQ_NO)
								END AS SALESUSER_NM,
								( SELECT csuserid FROM o_cssalesmap WHERE salesuserid = US_SALES.userid AND fixedyn = 'Y') AS CSUSERID,
								(
									SELECT	SUB1.user_nm
									  FROM 	o_user SUB1, o_cssalesmap SUB2
									 WHERE	SUB1.userid = SUB2.csuserid
									   AND	SUB2.salesuserid = US_SALES.userid
									   AND	SUB2.fixedyn = 'Y'
								) AS CSUSER_NM,
								( SELECT Count(*) FROM o_cust_order_d WHERE req_no = COH.req_no ) AS ITEM_CNT,
								( SELECT TOP 1 confirm_dt FROM o_order_confirm_h WHERE req_no = COH.req_no ) AS CONFIRM_DT2 ,
								QM.qms_temp_id
						  FROM	o_cust_order_h COH
								LEFT OUTER JOIN qms_pre_mast QM ON QM.req_no = COH.req_no AND QM.deleteyn = 'N'
								LEFT JOIN o_customer CU ON COH.cust_cd = CU.cust_cd
								LEFT JOIN o_shipto ST ON COH.shipto_cd = ST.shipto_cd
								LEFT JOIN o_user US_SALES ON CU.salesrep_cd = US_SALES.userid
						 WHERE	COH.req_no LIKE '%' + '100220402501201' + '%'
						   AND	COH.indate >= CONVERT(DATE, '2025-01-16')
						   AND	COH.indate < Dateadd(day, 1, CONVERT(DATE, '2025-01-22'))
						   AND	COH.status_cd IN ( '00', '01', '02', '03', '05', '07' )
					) XX
		) S
 WHERE	rownum BETWEEN 1 AND 10
;











