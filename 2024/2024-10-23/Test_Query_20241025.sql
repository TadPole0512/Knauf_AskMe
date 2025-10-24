C:\GitHub\Knauf_Eorder_JDE\src\main\resources\sql\mssql\o_qmsorder.xml

49                <!-- 2024-10-24 hsg where 다음에 바로 and 가 나오는 오류 수정 -->
50                <!-- AND 1 = 1 -->
51                1 = 1


145               <!-- 2024-10-24 hsg where 다음에 바로 and 가 나오는 오류 수정 -->
146               <!-- AND 1 = 1 -->
147               1 = 1



/* eorder.o_qmsorder.cnt */
SELECT
		COUNT(*)
FROM	qms_salesorder SO
		LEFT JOIN O_ITEM_NEW OIN
				ON OIN.ITEM_CD = SO.ITEM_CD /*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시 조건을 걸었음 */
				and OIN.LINE_TY = 'Y'
WHERE      1 = 1
      /*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시로 취치를 옮김 */
      /* AND OIN.LINE_TY = 'Y' */
AND		SO.ACTUAL_SHIP_DT >= '20240726'
AND		SO.ACTUAL_SHIP_DT <= '20240726'
      
;



 /* eorder.o_qmsorder.list */
SELECT
	      /*+ HASH(table) */
	      *
FROM	(
		      SELECT
					ROW_NUMBER() OVER( ORDER BY ORDERNO DESC, LINE_NO ASC ) AS ROWNUM ,
					XX.* ,
					CASE
						WHEN PRE1 = 'Y' AND PRE2 = 'Y' /* 사전입력 완료 */ THEN '사전'
						WHEN PRE1 = 'Y' AND PRE2 = 'N' /* 사전입력중 */ THEN '사전'
						ELSE '사후' /* 사후입력 대상 */
					END AS QMS_STEP
			FROM	(
						SELECT
								dbo.Sf_getpreorderyn(SO.cust_po) AS PRE1 ,
								dbo.Sf_getpreqtyyn(SO.cust_po) AS PRE2 ,
								ORDERNO, LINE_NO ,
								dbo.SF_GETQMSID(ORDERNO, LINE_NO)AS QMS_ARR ,
								'20' + dbo.SF_GETQMSID(ORDERNO, LINE_NO)AS QMS_ARR_TXT ,
								CASE
									WHEN (CONVERT(DECIMAL, dbo.SF_GETPREQTY(SO.cust_po)) >= CONVERT(DECIMAL, SO.ORDER_QTY)
											AND dbo.Sf_getpreorderyn(SO.cust_po) = 'Y'
											AND dbo.Sf_getpreqtyyn(SO.cust_po) = 'Y') THEN CONVERT(DECIMAL, SO.ORDER_QTY)
									WHEN dbo.Sf_getpreorderyn(SO.cust_po) = 'Y' THEN CONVERT(DECIMAL, dbo.SF_GETPREQTY(SO.cust_po))
									ELSE CONVERT(DECIMAL, dbo.Sf_getqmsqty(orderno, line_no))
								END AS QMS_ARR_QTY ,
								CASE
									WHEN ORDER_QTY = dbo.SF_GETQMSQTY(ORDERNO, LINE_NO) THEN '완료'
									ELSE '미완료'
								END AS QMS_STATUS ,
								dbo.SF_GETQMSSHIPTO(ORDERNO, LINE_NO)AS QMS_ARR_SHIPTO ,
								dbo.SF_GETMAILYN(ORDERNO, LINE_NO) AS MAIL_YN ,
								dbo.SF_GETFILEYN(ORDERNO, LINE_NO) AS FILE_YN ,
								SO.ITEM_CD , ORDERTY , CUST_PO , CUST_CD , CUST_NM ,
								dbo.SF_GETQMSQUARTER(ACTUAL_SHIP_DT) AS ACTUAL_SHIP_QUARTER ,
								dbo.SF_GETQMSACTIVEYN(dbo.SF_GETQMSQUARTER(ACTUAL_SHIP_DT)) AS ACTIVEYN ,
								CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ORDER_DT, 0, 4), '-'), SUBSTRING(ORDER_DT, 5, 2)), '-'), SUBSTRING(ORDER_DT, 7, 2)) AS ORDER_DT ,
								CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ACTUAL_SHIP_DT, 0, 4), '-'), SUBSTRING(ACTUAL_SHIP_DT, 5, 2)), '-'), SUBSTRING(ACTUAL_SHIP_DT, 7, 2)) AS ACTUAL_SHIP_DT ,
								SHIPTO_CD, SHIPTO_NM, RTRIM(CONCAT(ADD1, ADD2)) AS ADDR, ITEM_DESC, LOTN, ORDER_QTY, UNIT, SALESREP_NM
						FROM	qms_salesorder SO 
								LEFT JOIN O_ITEM_NEW OIN
										ON OIN.ITEM_CD = SO.ITEM_CD /*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시 조건을 걸었음 */
										AND OIN.LINE_TY = 'Y'
						WHERE	1 = 1
						/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시로 취치를 옮김 */
						/* AND OIN.LINE_TY = 'Y' */
--						AND		SO.ACTUAL_SHIP_DT >= '20240726'
--						AND		SO.ACTUAL_SHIP_DT <= '20241024'
					) XX
		) S
WHERE	ROWNUM BETWEEN 1 AND 15

;

--20240726(String), 20241024(String), 1(Integer), 15(Integer)




SELECT *
FROM  O_ITEM_NEW

;




--INSERT
--	INTO
--	QMS_ORD_FRCN(QMS_ID,
--	QMS_SEQ,
--	QMS_FRCN_ID,
--	KEYCODE,
--	CREATEUSER,
--	CREATETIME,
--	UPDATEUSER,
--	UPDATETIME,
--	DELETEYN)
SELECT	?, ?, QMS_FRCN_ID, KEYCODE, CREATEUSER, CREATETIME, UPDATEUSER, UPDATETIME, DELETEYN
FROM	QMS_ORD_FRCN
WHERE	(QMS_ID, QMS_SEQ) IN (
								SELECT QMS_ID, QMS_SEQ
								FROM	(
											SELECT
													RANK() OVER(PARTITION BY A.CUST_CD, C.SHIPTO_CD ORDER BY A.QMS_ID DESC, A.QMS_SEQ DESC, B.QMS_DETL_ID DESC) AS RNUM ,
													A.*
											FROM	QMS_ORD_MAST A , QMS_ORD_DETL B , QMS_SALESORDER C
											WHERE	A.QMS_ID = B.QMS_ID
											AND		A.QMS_SEQ = B.QMS_SEQ
											AND		A.DELETEYN = 'N'
											AND		B.DELETEYN = 'N'
											AND		B.ORDERNO = C.ORDERNO
											AND		B.LINE_NO = C.LINE_NO
											AND		A.CUST_CD = ?
											AND		C.SHIPTO_CD = ?
										)
								WHERE	RNUM = 1
							)

;





/* eorder.o_qmsorder.cnt */
SELECT
--		COUNT(*)
		*
FROM	qms_salesorder SO
		LEFT JOIN O_ITEM_NEW OIN
				ON OIN.ITEM_CD = SO.ITEM_CD /*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시 조건을 걸었음 */
				and OIN.LINE_TY = 'Y'
WHERE	1 = 1
		/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시로 취치를 옮김 */
		/* AND OIN.LINE_TY = 'Y' */
AND		SO.ACTUAL_SHIP_DT >= '20220401'
AND		SO.ACTUAL_SHIP_DT <= '20230508'
--AND		SO.ORDERNO LIKE '%' + '33713106' + '%'
AND		SO.ORDERNO = '33713106'

;

--20220401(String), 20230508(String), 33713106(String), 231Q0299-1


select * from qms_salesorder where ORDERNO = '33713106'

/* eorder.o_qmsorder.list */
SELECT
		/*+ HASH(table) */
		*
FROM	(
			SELECT
					ROW_NUMBER() OVER( ORDER BY ORDERNO DESC, LINE_NO ASC ) AS ROWNUM ,
					XX.* ,
					CASE
						WHEN PRE1 = 'Y' AND PRE2 = 'Y' /* 사전입력 완료 */ THEN '사전'
						WHEN PRE1 = 'Y' AND PRE2 = 'N' /* 사전입력중 */ THEN '사전'
						ELSE '사후' /* 사후입력 대상 */
					END AS QMS_STEP
			FROM	(
						SELECT
								dbo.Sf_getpreorderyn(SO.cust_po) AS PRE1 ,
								dbo.Sf_getpreqtyyn(SO.cust_po) AS PRE2 ,
								ORDERNO,
								LINE_NO ,
								dbo.SF_GETQMSID(ORDERNO, LINE_NO)AS QMS_ARR ,
								dbo.SF_GETQMSID(ORDERNO, LINE_NO)AS QMS_ARR_TXT ,
								CASE
									WHEN (CONVERT(DECIMAL, dbo.SF_GETPREQTY(SO.cust_po)) >= CONVERT(DECIMAL, SO.ORDER_QTY)
											AND dbo.Sf_getpreorderyn(SO.cust_po) = 'Y'
											AND dbo.Sf_getpreqtyyn(SO.cust_po) = 'Y') THEN CONVERT(DECIMAL, SO.ORDER_QTY)
									WHEN dbo.Sf_getpreorderyn(SO.cust_po) = 'Y' THEN CONVERT(DECIMAL, dbo.SF_GETPREQTY(SO.cust_po))
									ELSE CONVERT(DECIMAL, dbo.Sf_getqmsqty(orderno, line_no))
								END AS QMS_ARR_QTY ,
								CASE WHEN ORDER_QTY = dbo.SF_GETQMSQTY(ORDERNO, LINE_NO) THEN '완료' ELSE '미완료' END AS QMS_STATUS ,
								dbo.SF_GETQMSSHIPTO(ORDERNO, LINE_NO)AS QMS_ARR_SHIPTO ,
								dbo.SF_GETMAILYN(ORDERNO, LINE_NO) AS MAIL_YN ,
								dbo.SF_GETFILEYN(ORDERNO, LINE_NO) AS FILE_YN ,
								SO.ITEM_CD , ORDERTY , CUST_PO , CUST_CD , CUST_NM ,
								dbo.SF_GETQMSQUARTER(ACTUAL_SHIP_DT) AS ACTUAL_SHIP_QUARTER ,
								dbo.SF_GETQMSACTIVEYN(dbo.SF_GETQMSQUARTER(ACTUAL_SHIP_DT)) AS ACTIVEYN ,
								CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ORDER_DT, 0, 4), '-'), SUBSTRING(ORDER_DT, 5, 2)), '-'), SUBSTRING(ORDER_DT, 7, 2)) AS ORDER_DT ,
								CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ACTUAL_SHIP_DT, 0, 4), '-'), SUBSTRING(ACTUAL_SHIP_DT, 5, 2)), '-'), SUBSTRING(ACTUAL_SHIP_DT, 7, 2)) AS ACTUAL_SHIP_DT ,
								SHIPTO_CD, SHIPTO_NM, RTRIM(CONCAT(ADD1, ADD2)) AS ADDR, ITEM_DESC, LOTN, ORDER_QTY, UNIT, SALESREP_NM
						FROM	qms_salesorder SO
								LEFT JOIN O_ITEM_NEW OIN
										ON OIN.ITEM_CD = SO.ITEM_CD /*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시 조건을 걸었음 */
										AND OIN.LINE_TY = 'Y'
						WHERE	1 = 1
						/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시로 취치를 옮김 */
						/* AND OIN.LINE_TY = 'Y' */
						AND		SO.ACTUAL_SHIP_DT >= '20220401'
						AND		SO.ACTUAL_SHIP_DT <= '20230508'
					) XX
			WHERE	XX.QMS_ARR_TXT IS NOT NULL
			AND 	XX.ORDERNO = 33713106
		) S
WHERE	ROWNUM BETWEEN 1 AND 15

;


--20230507(String), 20241024(String), 1(Integer), 15(Integer)

--20220401(String), 20230508(String), 33713106(String), 1(Integer), 15(Integer)








/* eorder.o_qmsorder.list */
SELECT
	/*+ HASH(table) */
	*
FROM
	(
	SELECT
		ROW_NUMBER() OVER(
	ORDER BY
		ORDERNO DESC,
		LINE_NO ASC ) AS ROWNUM ,
		XX.* ,
		CASE
			WHEN PRE1 = 'Y'
			AND PRE2 = 'Y' /* 사전입력 완료 */
			THEN '사전'
			WHEN PRE1 = 'Y'
			AND PRE2 = 'N' /* 사전입력중 */
			THEN '사전'
			ELSE '사후' /* 사후입력 대상 */
		END AS QMS_STEP
	FROM
		(
		SELECT
			dbo.Sf_getpreorderyn(SO.cust_po) AS PRE1 ,
			dbo.Sf_getpreqtyyn(SO.cust_po) AS PRE2 ,
			ORDERNO,
			LINE_NO ,
			dbo.SF_GETQMSID(ORDERNO,
			LINE_NO)AS QMS_ARR ,
			dbo.SF_GETQMSID(ORDERNO,
			LINE_NO)AS QMS_ARR_TXT ,
			CASE
				WHEN (CONVERT(DECIMAL,
				dbo.SF_GETPREQTY(SO.cust_po)) >= CONVERT(DECIMAL,
				SO.ORDER_QTY)
				AND dbo.Sf_getpreorderyn(SO.cust_po) = 'Y'
				AND dbo.Sf_getpreqtyyn(SO.cust_po) = 'Y') THEN CONVERT(DECIMAL,
				SO.ORDER_QTY)
				WHEN dbo.Sf_getpreorderyn(SO.cust_po) = 'Y' THEN CONVERT(DECIMAL,
				dbo.SF_GETPREQTY(SO.cust_po))
				ELSE CONVERT(DECIMAL,
				dbo.Sf_getqmsqty(orderno,
				line_no))
			END AS QMS_ARR_QTY ,
			CASE
				WHEN ORDER_QTY = dbo.SF_GETQMSQTY(ORDERNO,
				LINE_NO) THEN '완료'
				ELSE '미완료'
			END AS QMS_STATUS ,
			dbo.SF_GETQMSSHIPTO(ORDERNO,
			LINE_NO)AS QMS_ARR_SHIPTO ,
			dbo.SF_GETMAILYN(ORDERNO,
			LINE_NO) AS MAIL_YN ,
			dbo.SF_GETFILEYN(ORDERNO,
			LINE_NO) AS FILE_YN ,
			SO.ITEM_CD ,
			ORDERTY ,
			CUST_PO ,
			CUST_CD ,
			CUST_NM ,
			dbo.SF_GETQMSQUARTER(ACTUAL_SHIP_DT) AS ACTUAL_SHIP_QUARTER ,
			dbo.SF_GETQMSACTIVEYN(dbo.SF_GETQMSQUARTER(ACTUAL_SHIP_DT)) AS ACTIVEYN ,
			CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ORDER_DT, 0, 4), '-'), SUBSTRING(ORDER_DT, 5, 2)), '-'), SUBSTRING(ORDER_DT, 7, 2)) AS ORDER_DT ,
			CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING(ACTUAL_SHIP_DT, 0, 4), '-'), SUBSTRING(ACTUAL_SHIP_DT, 5, 2)), '-'), SUBSTRING(ACTUAL_SHIP_DT, 7, 2)) AS ACTUAL_SHIP_DT ,
			SHIPTO_CD,
			SHIPTO_NM,
			RTRIM(CONCAT(ADD1, ADD2)) AS ADDR,
			ITEM_DESC,
			LOTN,
			ORDER_QTY,
			UNIT,
			SALESREP_NM
		FROM
			qms_salesorder SO
		LEFT JOIN O_ITEM_NEW OIN ON
			OIN.ITEM_CD = SO.ITEM_CD /*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시 조건을 걸었음 */
			AND OIN.LINE_TY = 'Y'
		WHERE
			/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시로 취치를 옮김 */
			/* AND OIN.LINE_TY = 'Y' */
			1 = 1
			AND SO.ACTUAL_SHIP_DT >= ?
			AND SO.ACTUAL_SHIP_DT <= ?
			AND SO.ORDERNO LIKE '%' + ? + '%' ) XX ) S
WHERE
	ROWNUM BETWEEN ? AND ? 


--20220401(String), 20241024(String), 33713106(String), 1(Integer), 15(Integer)





/* eorder.o_qmsorder.getQmsPopMastList */
SELECT
		A.* ,
		CASE
			WHEN (
					SELECT
							COUNT(*)
					FROM	(SELECT QMS_ID, ORDERNO FROM QMS_ORD_DETL GROUP BY QMS_ID, ORDERNO) Q
					WHERE	Q.QMS_ID = A.QMS_ID
				) > 1 THEN 'N'
			ELSE 'Y'
		END AS QMS_SPLIT_YN ,
		B.CUST_NM ,
		B.ADD1 + ' ' + B.ADD2 + B.ADD3 + B.ADD4 AS CUST_ADDR ,
		dbo.SF_GETQMSBIZNO(B.TAX_ID) AS CUST_BIZ_NO ,
		B.ZIP_CD , B.SALESREP_CD , B.SALESREP_NM , B.TEAM_CD , B.TEAM_NM , B.MAILING_NM ,
		dbo.SF_GETQMSACTIVEYN(QMS_ID) AS ACTIVEYN
FROM	QMS_ORD_MAST A
		LEFT JOIN O_CUSTOMER B
				ON A.CUST_CD = B.CUST_CD
WHERE	A.QMS_ID = '20231Q0299'
AND		A.QMS_SEQ = 1

;



-- 231Q0299(String), 1(String)





CREATE FUNCTION [dbo].[SF_GETQMSID_YYYY] 
( 
   @IN_ORDERNO varchar(max),
   @IN_LINENO varchar(max)
)
RETURNS varchar(max)
AS 
	BEGIN

		DECLARE  @return_Val varchar(max)

		SELECT @return_Val = x.ARR_NAME
		FROM (
			SELECT 
			STUFF(
				(A.QMS_ID+'-'+ CONVERT(VARCHAR(100), A.QMS_SEQ)),  1, 0, ''  
				) AS ARR_NAME
			FROM QMS_ORD_MAST A
			LEFT JOIN QMS_ORD_DETL B ON  A.QMS_ID = B.QMS_ID  AND A.QMS_SEQ = B.QMS_SEQ
			WHERE B.ORDERNO = @IN_ORDERNO
				AND B.LINE_NO  = @IN_LINENO
				AND A.DELETEYN = 'N'
		) X;


		RETURN @return_Val

	END
;



/* eorder.o_qmsorder.cnt */
SELECT
		COUNT(*)
FROM	qms_salesorder SO
		LEFT JOIN O_ITEM_NEW OIN
				ON OIN.ITEM_CD = SO.ITEM_CD /*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시 조건을 걸었음 */
				and OIN.LINE_TY = 'Y'
WHERE	1 = 1
	/*2024-09-30 hsg LINE_TY에 'Y'값이 없어 죠회가 되지 않아. LEFT JOIN 시로 취치를 옮김 */
	/* AND OIN.LINE_TY = 'Y' */
AND		SO.ACTUAL_SHIP_DT >= '20220401'
AND		SO.ACTUAL_SHIP_DT <= '20241024'
AND		SO.ORDERNO = 33713106

;

--20220401(String), 20241024(String), 33713106(String), 1(Integer), 15(Integer)






