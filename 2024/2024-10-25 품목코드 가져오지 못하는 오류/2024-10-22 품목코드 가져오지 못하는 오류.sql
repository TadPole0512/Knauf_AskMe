/* *********** 품목 코드 600(String), 일반 9.5*900*1800 평보드(String) *********** */

SELECT
        ITM.ITEM_CD
FROM    O_ITEM_MCU ITM
        LEFT JOIN O_ITEM_NEW OIN
                ON OIN.ITEM_CD = ITM.ITEM_CD
                AND ( UPPER(OIN.STOCK_TY) != 'N' OR OIN.STOCK_TY IS NULL)
WHERE   1 = 1
AND     ITM.ITEM_MCU = '600'
AND     REPLACE(ITM.DESC1, ' ', '') = REPLACE('일반 9.5*900*1800 평보드', ' ', '')
GROUP BY ITM. ITEM_CD

;



SELECT
        1 AS aaaa ,
        ITEM_MCU
FROM    O_ITEM_MCU ITM
WHERE   1 = 1
--AND     ITM.ITEM_MCU = '5587'
AND     REPLACE(ITM.DESC1, ' ', '') = REPLACE('일반 9.5*900*1800 평보드', ' ', '')

;


/* 출고지 관리 */
SELECT
        p.PT_CODE PT_CODE ,
        P.WERKS,
        p.PT_NAME AS PT_NAME
FROM    PLANT p
WHERE   p.PT_USE = 'Y'
ORDER BY  p.PT_SORT 
;






SELECT
        1 AS aaaa ,
        ITEM_MCU, 
        *
FROM    O_ITEM_MCU ITM
WHERE   1 = 1
AND     ITM.ITEM_MCU = '5587'
AND     EXISTS  (
                    SELECT  *
                    FROM    PLANT P
                    WHERE   1 = 1
                    AND     ITM.ITEM_MCU = P.PT_CODE
                )
;


