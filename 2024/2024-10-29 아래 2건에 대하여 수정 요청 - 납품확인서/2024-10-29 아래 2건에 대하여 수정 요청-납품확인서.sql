


SELECT
		SHIPTO_CD,
		SHIPTO_NM
FROM	O_SALESORDER
WHERE	ACTUAL_SHIP_DT >= '20241001'
AND		ACTUAL_SHIP_DT <= '20241029'
AND		CUST_CD = '10178170'
GROUP BY SHIPTO_NM, SHIPTO_CD
ORDER BY SHIPTO_NM ASC 

;



--20241001(String), 20241029(String), 10178170(String)


SELECT
        SHIPTO_CD,
        SHIPTO_NM
FROM    O_SALESORDER
WHERE   ACTUAL_SHIP_DT >= '20241001'
AND     ACTUAL_SHIP_DT <= '20241029'
AND     CUST_CD = '10178170'
AND     STATUS1 >= '580'
AND     STATUS1 <> '980'
GROUP BY SHIPTO_NM, SHIPTO_CD
ORDER BY SHIPTO_NM ASC 

;







/* ***************************************************************************************************************************************** */


SELECT
		IT.*
FROM	(
			SELECT
					ITEM_DESC
			FROM	O_SALESORDER
			WHERE	ACTUAL_SHIP_DT >= '20241001'
			AND		ACTUAL_SHIP_DT <= '20241029'
			AND		CUST_CD = '10178170'
			AND		SHIPTO_NM = TRIM('전북한림익산_예술디자인')
			GROUP BY ITEM_DESC
		) IT
ORDER BY IT.ITEM_DESC ASC 


--20241001(String), 20241029(String), 10178170(String), 전북한림익산_예술디자인(String)

























