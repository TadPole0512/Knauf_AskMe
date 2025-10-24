SELECT
        SUB.*
FROM    (
            SELECT
                    SO.ACTUAL_SHIP_DT ,
                    SO.ITEM_DESC ,
                    SO.ADD1 ,
                    (
                        CASE
                            WHEN OH.HEBE_UM IS NULL THEN ( SELECT DISTINCT UNIT FROM O_SALESORDER WHERE ITEM_DESC = SO.ITEM_DESC)
                            ELSE OH.HEBE_UM
                        END
                    ) AS UNIT ,
                    ROUND( SUM(CASE WHEN SO.STATUS1 = '980' THEN 0 ELSE ( CASE WHEN OH.ITEM_CD IS NOT NULL THEN SO.PRIMARY_QTY * OH.X / OH.Y ELSE SO.PRIMARY_QTY END ) END), 1) AS ORDER_QTY ,
                    SUBSTRING(SO.ITEM_CD, 1, 3) AS ITEM_CD_3 ,
                    SUM(CASE WHEN SO.STATUS1 = '980' THEN 0 ELSE ( CASE WHEN OH.ITEM_CD IS NOT NULL THEN SO.PRIMARY_QTY * OH.X / OH.Y ELSE SO.PRIMARY_QTY END ) END) AS AAAA ,
                    MAX(SO.STATUS1) AS BBBBB ,
                    CASE
                        WHEN MFG.MFG IS NULL THEN '크나우프 석고보드㈜'
                        ELSE MFG.MFG
                    END AS MANUFACT
            FROM    O_SALESORDER SO
                    LEFT JOIN O_ITEM_HEBE OH
                        ON  OH.ITEM_CD = SO.ITEM_CD
                    LEFT JOIN O_ITEM_MFG MFG
                        ON  SO.ITEM_CD = MFG.ITEM_CD
            WHERE   ACTUAL_SHIP_DT >= '20240901'
            AND     ACTUAL_SHIP_DT <= '20240930'
            AND     CUST_CD = '10178170'
            AND     SHIPTO_NM = TRIM('무안 오룡 우미건설-예술디자인(주)')
            AND     ITEM_DESC IN (TRIM('방균 9.5*900*2550 평보드'), TRIM('석고본드'), TRIM('일반 9.5*900*1800 평보드'))
            AND     SO.STATUS1 >= '580'
            AND     SO.STATUS1 <> '980'
            GROUP BY ACTUAL_SHIP_DT , ITEM_DESC , OH.HEBE_UM , ADD1 , SUBSTRING(SO.ITEM_CD, 1, 3) , MFG.MFG
        ) SUB
ORDER BY SUB.ACTUAL_SHIP_DT ASC, SUB.ITEM_DESC ASC
;

--20240901(String), 20240930(String), 10178170(String), 무안 오룡 우미건설-예술디자인(주)(String), 방균 9.5*900*2550 평보드(String), 석고본드(String), 일반 9.5*900*1800 평보드(String)



            SELECT
                    SO.ACTUAL_SHIP_DT ,
                    SO.ITEM_DESC ,
                    SO.ADD1 ,
                    (
                        CASE
                            WHEN OH.HEBE_UM IS NULL THEN ( SELECT DISTINCT UNIT FROM O_SALESORDER WHERE ITEM_DESC = SO.ITEM_DESC)
                            ELSE OH.HEBE_UM
                        END
                    ) AS UNIT ,
--                    ROUND( SUM(CASE WHEN SO.STATUS1 = '980' THEN 0 ELSE ( CASE WHEN OH.ITEM_CD IS NOT NULL THEN SO.PRIMARY_QTY * OH.X / OH.Y ELSE SO.PRIMARY_QTY END ) END), 1) AS ORDER_QTY ,
                    SUBSTRING(SO.ITEM_CD, 1, 3) AS ITEM_CD_3 ,
--                    SUM(CASE WHEN SO.STATUS1 = '980' THEN 0 ELSE ( CASE WHEN OH.ITEM_CD IS NOT NULL THEN SO.PRIMARY_QTY * OH.X / OH.Y ELSE SO.PRIMARY_QTY END ) END) AS AAAA ,
                    SO.STATUS1 , OH.ITEM_CD , SO.PRIMARY_QTY , OH.X , OH.Y ,SO.PRIMARY_QTY ,
                    CASE
                        WHEN MFG.MFG IS NULL THEN '크나우프 석고보드㈜'
                        ELSE MFG.MFG
                    END AS MANUFACT
            FROM    O_SALESORDER SO
                    LEFT JOIN O_ITEM_HEBE OH
                        ON  OH.ITEM_CD = SO.ITEM_CD
                    LEFT JOIN O_ITEM_MFG MFG
                        ON  SO.ITEM_CD = MFG.ITEM_CD
            WHERE   ACTUAL_SHIP_DT >= '20240901'
            AND     ACTUAL_SHIP_DT <= '20240930'
            AND     CUST_CD = '10178170'
            AND     SHIPTO_NM = TRIM('무안 오룡 우미건설-예술디자인(주)')
            AND     ITEM_DESC IN (TRIM('방균 9.5*900*2550 평보드'), TRIM('석고본드'), TRIM('일반 9.5*900*1800 평보드'))
            AND     SO.STATUS1 >= '580'
            AND     SO.STATUS1 <> '980'
--            GROUP BY ACTUAL_SHIP_DT , ITEM_DESC , OH.HEBE_UM , ADD1 , SUBSTRING(SO.ITEM_CD, 1, 3) , MFG.MFG

;


SELECT 100 * 2295 / 1000;
SELECT 240 * 162 / 100;
SELECT CAST(240 AS FLOAT) * CAST(162 AS FLOAT) / 100;
SELECT ROUND(CAST(240 AS FLOAT) * CAST(162 AS FLOAT) / 100, 0);
SELECT CAST(240 * 162 / 100 AS FLOAT);
SELECT FORMAT(240 * 162 / 100, '###,###');



SELECT
		SO.PRIMARY_QTY
FROM	O_SALESORDER SO
WHERE	1 = 1
--AND		
--GROUP BY 
--ORDER BY 
;



