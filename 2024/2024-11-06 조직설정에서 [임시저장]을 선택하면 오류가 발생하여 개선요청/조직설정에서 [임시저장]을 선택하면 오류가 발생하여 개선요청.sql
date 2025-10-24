

/* *********** eorder.o_cssalesmap.mergea *********** */


MERGE INTO O_CSSALESMAP -- USING DUAL ON (CSUSERID = ? AND SALESUSERID = ?)
	USING	(SELECT 1 AS dual) AS D ON (CSUSERID = ? AND SALESUSERID = ?)
    WHEN MATCHED THEN
		UPDATE SET SALESUSERNM = (
									SELECT
											USER_NM
									FROM	O_USER
									WHERE	USERID = ?
								) ,
								DUMMY = ? ,
								FIXEDYN = ? ,
								UPDATE_DT = FORMAT(GETDATE(), 'YYYY-MM-DD') ,
								UPDATEID = ?
	WHEN NOT MATCHED THEN
		INSERT	(
					CSUSERID ,
					SALESUSERID ,
					SALESUSERNM ,
					DUMMY ,
					FIXEDYN ,
					INSERTID ,
					INSERT_DT
				)
		VALUES	(
					? ,
					? ,
					( SELECT USER_NM FROM O_USER WHERE USERID = ?) ,
					? ,
					? ,
					? ,
					FORMAT(GETDATE(), 'YYYY-MM-DD')
				))
;



/* *********** eorder.o_cssalesmap.cnt *********** */

SELECT
    COUNT(*)
FROM
    O_CSSALESMAP CSM
WHERE
    CSUSERID = 'develop'
    AND SALESUSERID = '0080002173'
    AND FIXEDYN = 'Y'
;

--develop(String), 0080002173(String), Y(String)

/* *********** eorder.o_cssalesmap.merge *********** */


MERGE INTO O_CSSALESMAP -- USING DUAL ON (CSUSERID = ? AND SALESUSERID = ?)
    USING   (SELECT 1 AS dual) AS D ON (CSUSERID = 'develop' AND SALESUSERID = '0080002173')
    WHEN MATCHED THEN
        UPDATE SET SALESUSERNM = (
                                    SELECT
                                            USER_NM
                                    FROM    O_USER
                                    WHERE   USERID = '0080002173'
                                ) ,
                                DUMMY = '' ,
                                FIXEDYN = 'N' ,
                                UPDATE_DT = FORMAT(GETDATE(), 'YYYY-MM-DD') ,
                                UPDATEID = 'develop'
    WHEN NOT MATCHED THEN
        INSERT  (
                    CSUSERID ,
                    SALESUSERID ,
                    SALESUSERNM ,
                    DUMMY ,
                    FIXEDYN ,
                    INSERTID ,
                    INSERT_DT
                )
        VALUES  (
                    'develop' ,
                    '0080002173' ,
                    ( SELECT USER_NM FROM O_USER WHERE USERID = '0080002173') ,
                    '' ,
                    'N' ,
                    'develop' ,
                    FORMAT(GETDATE(), 'YYYY-MM-DD')
                )
;

-- mi_salesuserid=0080002173, m_fixedyn=N, m_csuserid=develop, m_insertid=develop, r_csuserid=develop, r_salesuserid=0080002173, r_fixedyn=Y





/* ***************************************************************************************************************************************** */


SELECT
		*
FROM	O_CSSALESMAP A
WHERE	1 = 1
AND		CSUSERID = 'develop'
--GROUP BY 
--ORDER BY 
;

SELECT
		*
FROM	O_USER ou
WHERE	1 = 1
AND		USERID = 'develop'
--GROUP BY 
--ORDER BY 
;

SELECT FORMAT(GETDATE(), 'yyyy-MM-dd')
;

SELECT CONVERT(DATE, GETDATE(), 102);

SELECT LEN(CONVERT(nvarchar(19), GETDATE(), 120));

SELECT LEFT (CONVERT(nvarchar(19), GETDATE(), 120), LEN(CONVERT(nvarchar(19), GETDATE(), 120))-3)
;




