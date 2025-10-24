
/* ***************************************************************************************************************************************** */
/* *********** O_SALESORDER *********** */

-- O_SALESORDER
	-- ORDERNO bigint NOT NULL,
	-- ORDERTY varchar(4) COLLATE Korean_Wansung_CI_AS NOT NULL,
	-- LINE_NO bigint NOT NULL,

	-- ORDER_QTY bigint NULL,
	-- PRIMARY_QTY bigint NULL,
	-- SECOND_QTY bigint NULL,
	-- WEIGHT bigint NULL,
	-- PRICE bigint NULL,
	-- AMOUNT bigint NULL,


/* *********** 컬럼변경 *********** */
;

-- ALTER TABLE dbo.O_SALESORDER ALTER COLUMN ORDER_QTY bigint;
-- ALTER TABLE dbo.O_SALESORDER ALTER COLUMN PRIMARY_QTY bigint;
-- ALTER TABLE dbo.O_SALESORDER ALTER COLUMN SECOND_QTY bigint;
-- ALTER TABLE dbo.O_SALESORDER ALTER COLUMN PRICE bigint;
-- ALTER TABLE dbo.O_SALESORDER ALTER COLUMN AMOUNT bigint;


ALTER TABLE dbo.O_SALESORDER ALTER COLUMN WEIGHT float;


/* *********** 오라클 조회 *********** */
;

SELECT
		ORDERNO, ORDERTY, LINE_NO, WEIGHT
FROM	O_SALESORDER
WHERE	1 = 1
;


SELECT
		*
--INTO	O_SALESORDER_20241206
FROM	O_SALESORDER 
WHERE	1 = 1
;



/* ***************************************************************************************************************************************** */
/* *********** QMS_SALESORDER *********** */


-- QMS_SALESORDER
	-- ORDERNO bigint NOT NULL,
	-- ORDERTY varchar(4) COLLATE Korean_Wansung_CI_AS NOT NULL,
	-- LINE_NO bigint NOT NULL,

	-- ORDER_QTY bigint NULL,
	-- PRIMARY_QTY bigint NULL,
	-- SECOND_QTY bigint NULL,
	-- WEIGHT bigint NULL,
	-- PRICE bigint NULL,
	-- AMOUNT bigint NULL,


/* *********** 컬럼변경 *********** */
;

-- ALTER TABLE dbo.QMS_SALESORDER ALTER COLUMN ORDER_QTY bigint;
-- ALTER TABLE dbo.QMS_SALESORDER ALTER COLUMN PRIMARY_QTY bigint;
-- ALTER TABLE dbo.QMS_SALESORDER ALTER COLUMN SECOND_QTY bigint;
-- ALTER TABLE dbo.QMS_SALESORDER ALTER COLUMN PRICE bigint;
-- ALTER TABLE dbo.QMS_SALESORDER ALTER COLUMN AMOUNT bigint;

ALTER TABLE dbo.QMS_SALESORDER ALTER COLUMN WEIGHT float;



/* *********** 오라클 조회 *********** */
;

SELECT
		ORDERNO, ORDERTY, LINE_NO, WEIGHT
FROM	QMS_SALESORDER
WHERE	1 = 1
;



SELECT
		*
-- INTO	QMS_SALESORDER_20241206
FROM	QMS_SALESORDER 
WHERE	1 = 1
;




/* ***************************************************************************************************************************************** */
/* *********** Work *********** */



WITH TB AS	(
				SELECT
						ROW_NUMBER() OVER(ORDER BY ORDERNO ASC, ORDERTY ASC, LINE_NO ASC) AS RNUM
						, ORDERNO, ORDERTY, LINE_NO, WEIGHT
				  FROM	QMS_SALESORDER
				 WHERE	1 = 1
			)
SELECT	ORDERNO, ORDERTY, LINE_NO, WEIGHT
  FROM	TB A
 WHERE	1 = 1
   AND	RNUM BETWEEN 1 AND 200000
;







WITH TB AS	(
				SELECT
						ROW_NUMBER() OVER(ORDER BY ORDERNO ASC, ORDERTY ASC, LINE_NO ASC) AS RNUM
						, ORDERNO, ORDERTY, LINE_NO, WEIGHT
				  FROM	O_SALESORDER
				 WHERE	1 = 1
			)
SELECT	ORDERNO, ORDERTY, LINE_NO, WEIGHT
  FROM	TB A
 WHERE	1 = 1
   AND	RNUM BETWEEN 1 AND 200000
;





SELECT
		*
-- INTO	QMS_SALESORDER_20241206
FROM	QMS_SALESORDER 
WHERE	1 = 1
;






SELECT
		*
-- INTO	QMS_SALESORDER_20241206
FROM	O_SALESORDER 
WHERE	1 = 1
;



