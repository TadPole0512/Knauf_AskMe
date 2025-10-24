
--구 eOrder DB Migration에 따른 출력자료 이슈에 대하여 개선요청


/* ***************************************************************************************************************************************** */
/* *********** http://localhost:8080/eorder/admin/report/deliveryPaperPop.lime *********** */


/* *********** eorder.config.one *********** */
;

SELECT * FROM CONFIG WHERE CF_ID = 'CEOSEAL'
;


/* *********** eorder.o_salesorder.getCntReportFor1020 *********** */

SELECT
		COUNT(*)
FROM	(
			SELECT
					SO.ACTUAL_SHIP_DT , SO.ITEM_DESC , SO.ADD1 , SO.UNIT , SUM(SO.ORDER_QTY) AS ORDER_QTY
			FROM	O_SALESORDER SO
			WHERE	ACTUAL_SHIP_DT >= '20230201'
			AND		ACTUAL_SHIP_DT <= '20230228'
			AND		CUST_CD = '1795'
			AND		ITEM_DESC IN (
									TRIM('방화 15*900*1800 테파드'), TRIM('방화 15*900*1800 평보드'), TRIM('방화방수 15*900*1800 평보드')
									, TRIM('아트사운드12.5*1200*2400LR12-2'), TRIM('아트사운드12.5*900*1800 SR12-2'), TRIM('아트사운드12.5*900*1800 SR15-8')
									, TRIM('일반 12.5*900*1800 평보드'), TRIM('일반 9.5*900*1800 평보드'), TRIM('Sheetrock Gyptex2 9.5*300*600'), TRIM('Tectopanel (M1)')
								)
			GROUP BY ACTUAL_SHIP_DT , ITEM_DESC , UNIT , ADD1
		) SUB
;




/* *********** eorder.o_salesorder.getReportFor1020 *********** */
;

SELECT
		SUB.*
FROM	(
			SELECT
					SO.ACTUAL_SHIP_DT , SO.ITEM_DESC , SO.ADD1 , SO.UNIT , SUM(SO.ORDER_QTY) AS ORDER_QTY , SUBSTRING(ITEM_CD, 1, 3) AS ITEM_CD_3
			FROM	O_SALESORDER SO
			WHERE	ACTUAL_SHIP_DT >= '20230201'
			AND		ACTUAL_SHIP_DT <= '20230228'
			AND		CUST_CD = '1795'
			AND		ITEM_DESC IN (
									TRIM('방화 15*900*1800 테파드'), TRIM('방화 15*900*1800 평보드'), TRIM('방화방수 15*900*1800 평보드')
									, TRIM('아트사운드12.5*1200*2400LR12-2'), TRIM('아트사운드12.5*900*1800 SR12-2'), TRIM('아트사운드12.5*900*1800 SR15-8')
									, TRIM('일반 12.5*900*1800 평보드'), TRIM('일반 9.5*900*1800 평보드'), TRIM('Sheetrock Gyptex2 9.5*300*600'), TRIM('Tectopanel (M1)')
								)
			GROUP BY ACTUAL_SHIP_DT , ITEM_DESC , UNIT , ADD1 , SUBSTRING(ITEM_CD, 1, 3)
		) SUB
ORDER BY SUB.ACTUAL_SHIP_DT ASC , SUB.ITEM_DESC ASC 





/* *********** eorder.o_salesorder.getReportPeriodDate *********** */
;


SELECT
		MIN(ACTUAL_SHIP_DT) AS START_DATE
		, MAX(ACTUAL_SHIP_DT) AS END_DATE
FROM	O_SALESORDER SO
WHERE	ACTUAL_SHIP_DT >= '20230201'
AND 	ACTUAL_SHIP_DT <= '20230228'
AND 	CUST_CD = '1795'
AND 	ITEM_DESC IN	(
							TRIM('방화 15*900*1800 테파드'), TRIM('방화 15*900*1800 평보드'), TRIM('방화방수 15*900*1800 평보드')
							, TRIM('아트사운드12.5*1200*2400LR12-2'), TRIM('아트사운드12.5*900*1800 SR12-2'), TRIM('아트사운드12.5*900*1800 SR15-8')
							, TRIM('일반 12.5*900*1800 평보드'), TRIM('일반 9.5*900*1800 평보드'), TRIM('Sheetrock Gyptex2 9.5*300*600'), TRIM('Tectopanel (M1)')
						)
;




/* *********** eorder.o_salesorder.getCustInfoForReport *********** */
;

SELECT
		CUST_NM, SALESREP_NM
FROM	O_CUSTOMER
WHERE	CUST_CD = '1795'
;


















































