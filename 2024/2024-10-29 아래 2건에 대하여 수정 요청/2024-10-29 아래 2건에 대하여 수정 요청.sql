


SELECT *
FROM QMS_ORD_CORP F
WHERE 1 = 1
--AND F.SHIPTO_CD = '10183135'
ORDER BY F.SHIPTO_SEQ DESC
;


/* ***************************************************************************************************************************************** */


/* eorder.o_qmsorder.setQmsOrderMastHistory */
MERGE INTO QMS_ORD_CORP F
	USING ( SELECT 1 AS DUAL) AS T ON (F.SHIPTO_CD = '10183135')
WHEN MATCHED THEN
	UPDATE
	SET
		SHIPTO_ADDR = '광주광역시, 광산구, 첨단중앙로170번길 17' ,
		SHIPTO_EMAIL = 'admin@knauf.com' ,
		CNSTR_ADDR = '' ,
		CNSTR_BIZ_NO = '' ,
		CNSTR_TEL = '' ,
		SUPVS_ADDR = '' ,
		SUPVS_QLF_NO = '' ,
		SUPVS_DEC_NO = '' ,
		SUPVS_TEL = '' ,
		UPDATEUSER = 'develop' ,
		UPDATETIME = GETDATE()
WHEN NOT MATCHED THEN
	INSERT	(
				SHIPTO_CD,
				SHIPTO_NM,
				SHIPTO_ADDR,
				SHIPTO_EMAIL,
				CNSTR_NM,
				CNSTR_ADDR,
				CNSTR_BIZ_NO,
				CNSTR_TEL ,
				SUPVS_NM,
				SUPVS_ADDR ,
				SUPVS_QLF_NO,
				SUPVS_DEC_NO,
				SUPVS_TEL,
				CREATEUSER,
				CREATETIME,
				UPDATEUSER,
				UPDATETIME,
				DELETEYN
			)
	VALUES	(
				'10183135' ,
				'아산 배방 생활숙박시설 - (주)한화' ,
				'광주광역시, 광산구, 첨단중앙로170번길 17' ,
				'admin@knauf.com' ,
				'시공회사' ,
				'' ,
				'' ,
				'' ,
				'감리회사' ,
				'' ,
				'' ,
				'' ,
				'' ,
				'develop' ,
				GETDATE() ,
				'develop' ,
				GETDATE() ,
				'N'
			)
;



-- 10183135(String), 광주광역시, 광산구, 첨단중앙로170번길 17(String), admin@knauf.com(String), (String), (String), (String), (String), (String), (String), (String), develop(String), 10183135(String), 아산 배방 생활숙박시설 - (주)한화(String), 광주광역시, 광산구, 첨단중앙로170번길 17(String), admin@knauf.com(String), 시공회사 (String), (String), (String), (String), 감리회사 (String), (String), (String), (String), (String), develop(String), develop(String)
-- 18168884(String), 4000(String), 786232(String), 502409155(String), 18168884(String), 4000(String), 786232(String), 502409155(String), 20243Q0112(String), 1(String)






/* eorder.o_qmsorder.setQmsOrderMastHistory */  MERGE INTO QMS_ORD_CORP F          USING (SELECT 1 AS DUAL) AS T        ON (F.SHIPTO_CD = ?)     WHEN MATCHED THEN         UPDATE SET F.SHIPTO_ADDR = ?                   ,F.SHIPTO_EMAIL = ?                   ,F.CNSTR_ADDR = ?                   ,F.CNSTR_BIZ_NO = ?                   ,F.CNSTR_TEL = ?                   ,F.SUPVS_ADDR = ?                                      ,F.SUPVS_QLF_NO = ?            ,F.SUPVS_DEC_NO = ?                   ,F.SUPVS_TEL = ?                   ,F.UPDATEUSER = ?                   ,F.UPDATETIME = GETDATE()     WHEN NOT MATCHED THEN               INSERT (SHIPTO_SEQ, SHIPTO_CD,SHIPTO_NM,SHIPTO_ADDR,SHIPTO_EMAIL,CNSTR_NM,CNSTR_ADDR,CNSTR_BIZ_NO,CNSTR_TEL                 ,SUPVS_NM,SUPVS_ADDR                                   ,SUPVS_QLF_NO,SUPVS_DEC_NO,SUPVS_TEL,CREATEUSER,CREATETIME,UPDATEUSER,UPDATETIME,DELETEYN)         VALUES (?         ,(?         ,?         ,?         ,?         ,?         ,?         ,?         ,?         ,?         ,?                  ,?         ,?         ,?         ,?      ,GETDATE()      ,?      ,GETDATE()      ,'N') ;




/* ***************************************************************************************************************************************** */


SELECT
		*
FROM	QMS_ORD_CORP A
WHERE	1 = 1
--AND
--GROUP BY
ORDER BY SHIPTO_SEQ DESC
;



/* eorder.o_qmsorder.setQmsOrderMastHistory */
MERGE
INTO
    QMS_ORD_CORP F
        USING (
    SELECT
        1 AS DUAL) AS T ON
    (F.SHIPTO_CD = ?)
    WHEN MATCHED THEN
UPDATE
SET
    F.SHIPTO_ADDR = ? ,
    F.SHIPTO_EMAIL = ? ,
    F.CNSTR_ADDR = ? ,
    F.CNSTR_BIZ_NO = ? ,
    F.CNSTR_TEL = ? ,
    F.SUPVS_ADDR = ? ,
    F.SUPVS_QLF_NO = ? ,
    F.SUPVS_DEC_NO = ? ,
    F.SUPVS_TEL = ? ,
    F.UPDATEUSER = ? ,
    F.UPDATETIME = GETDATE()
    WHEN NOT MATCHED THEN
INSERT
    (SHIPTO_SEQ,
    SHIPTO_CD,
    SHIPTO_NM,
    SHIPTO_ADDR,
    SHIPTO_EMAIL,
    CNSTR_NM,
    CNSTR_ADDR,
    CNSTR_BIZ_NO,
    CNSTR_TEL ,
    SUPVS_NM,
    SUPVS_ADDR ,
    SUPVS_QLF_NO,
    SUPVS_DEC_NO,
    SUPVS_TEL,
    CREATEUSER,
    CREATETIME,
    UPDATEUSER,
    UPDATETIME,
    DELETEYN)
VALUES (? ,
(? ,
? ,
? ,
? ,
? ,
? ,
? ,
? ,
? ,
? ,
? ,
? ,
? ,
? ,
GETDATE() ,
? ,
GETDATE() ,
'N') ;


/* ***************************************************************************************************************************************** */


/* eorder.o_qmsorder.getQmsOrderQtyCheck */
SELECT
        ((
            SELECT
                    CASE
                        WHEN COUNT(Q.QMS_ORD_QTY) > 0 THEN SUM(Q.QMS_ORD_QTY)
                        ELSE 0
                    END AS QMS_BALANCE
            FROM    QMS_ORD_DETL Q , QMS_ORD_MAST M
            WHERE   Q.QMS_ID = M.QMS_ID
            AND     Q.QMS_SEQ = M.QMS_SEQ
            AND     Q.ORDERNO = 18168884
            AND     Q.LINE_NO = 4000
            AND     Q.ITEM_CD = '786232'
            AND     Q.LOTNO = '502409155'
            AND     M.DELETEYN = 'N'
        ) - (
                SELECT
                    CASE
                        WHEN COUNT(Q.QMS_ORD_QTY) > 0 THEN SUM(Q.QMS_ORD_QTY)
                        ELSE 0
                    END AS QMS_BALANCE
                FROM    QMS_ORD_DETL Q , QMS_ORD_MAST M
                WHERE   Q.QMS_ID = M.QMS_ID
                AND     Q.QMS_SEQ = M.QMS_SEQ
                AND     Q.ORDERNO = 18168884
                AND     Q.LINE_NO = 4000
                AND     Q.ITEM_CD = '786232'
                AND     Q.LOTNO = '502409155'
                AND     Q.QMS_ID = '20243Q0115'
                AND     Q.QMS_SEQ = 1
                AND     M.DELETEYN = 'N'
            )) AS QMS_BALANCE
    -- FROM DUAL
;

-- 18168884(String), 4000(String), 786232(String), 502409155(String), 18168884(String), 4000(String), 786232(String), 502409155(String), 20243Q0115(String), 1(String)


SELECT MAX(T.SHIPTO_SEQ)+1 FROM QMS_ORD_CORP T
;


--10183135(String), 광주광역시, 광산구, 첨단중앙로170번길 17(String), admin@knauf.com(String), (String), (String), (String), (String), (String), (String), (String), develop(String), 5504(Long), 10183135(String), 아산 배방 생활숙박시설 - (주)한화(String), 광주광역시, 광산구, 첨단중앙로170번길 17(String), admin@knauf.com(String), 시공회사 (String), (String), (String), (String), 감리회사 (String), (String), (String), (String), (String), develop(String), develop(String)



;
/* eorder.o_qmsorder.setQmsOrderMastHistory */
MERGE INTO QMS_ORD_CORP as F
    USING (VALUES (1)) AS S(Number)
       ON (F.SHIPTO_CD = '10183135')
    WHEN MATCHED THEN
        UPDATE
        SET
           F.UPDATEUSER = 'develop' ,
            F.UPDATETIME = GETDATE()
    WHEN NOT MATCHED THEN
        INSERT  (
                    SHIPTO_SEQ,
                    DELETEYN,
                    ROWID
                )
        VALUES  (
                    5504 ,
                    'N'
                    , 'ABDDDDSD'
                )
;





    MERGE INTO QMS_ORD_CORP AS a
        USING (SELECT 1 AS dual) AS b ON (a.SHIPTO_CD = '10183135')
        WHEN MATCHED THEN
            UPDATE
            SET
            F.SHIPTO_ADDR = '광주광역시, 광산구, 첨단중앙로170번길 17' ,
            F.SHIPTO_EMAIL = 'admin@knauf.com' ,
            F.CNSTR_ADDR = '' ,
            F.CNSTR_BIZ_NO = '' ,
            F.CNSTR_TEL = '' ,
            F.SUPVS_ADDR = '' ,
            F.SUPVS_QLF_NO = '' ,
            F.SUPVS_DEC_NO = '' ,
            F.SUPVS_TEL = '' ,
            F.UPDATEUSER = 'develop' ,
            F.UPDATETIME = GETDATE()
        WHEN NOT MATCHED THEN
        INSERT  (
                    SHIPTO_SEQ,
                    DELETEYN,
                    ROWID
                )
        VALUES  (
                    5504 ,
                    'N'
                    , 'ABDDDDSD'
                )
        ;
;




/* eorder.o_qmsorder.setQmsOrderMastHistory */
MERGE INTO QMS_ORD_CORP AS F
    USING ( SELECT 1 AS dual) AS b
        ON (F.SHIPTO_CD = '10183135')
    WHEN MATCHED THEN
        UPDATE SET
            F.SHIPTO_ADDR = '광주광역시, 광산구, 첨단중앙로170번길 17' ,
            F.SHIPTO_EMAIL = 'admin@knauf.com' ,
            F.CNSTR_ADDR = '' ,
            F.CNSTR_BIZ_NO = '' ,
            F.CNSTR_TEL = '' ,
            F.SUPVS_ADDR = '' ,
            F.SUPVS_QLF_NO = '' ,
            F.SUPVS_DEC_NO = '' ,
            F.SUPVS_TEL = '' ,
            F.UPDATEUSER = 'develop' ,
            F.UPDATETIME = GETDATE()
    WHEN NOT MATCHED THEN

MERGE INTO QMS_ORD_CORP AS F
    USING (SELECT 1 AS dual) AS b ON (F.SHIPTO_CD = '10183135')
WHEN MATCHED THEN
    UPDATE SET             F.SHIPTO_ADDR = '광주광역시, 광산구, 첨단중앙로170번길 17' ,
            F.SHIPTO_EMAIL = 'admin@knauf.com' ,
            F.CNSTR_ADDR = '' ,
            F.CNSTR_BIZ_NO = '' ,
            F.CNSTR_TEL = '' ,
            F.SUPVS_ADDR = '' ,
            F.SUPVS_QLF_NO = '' ,
            F.SUPVS_DEC_NO = '' ,
            F.SUPVS_TEL = '' ,
            F.UPDATEUSER = 'develop' ,
            F.UPDATETIME = GETDATE()
WHEN NOT MATCHED THEN
        INSERT  (
                    SHIPTO_SEQ,
                    SHIPTO_CD,
                    SHIPTO_NM,
                    SHIPTO_ADDR,
                    SHIPTO_EMAIL,
                    CNSTR_NM,
                    CNSTR_ADDR,
                    CNSTR_BIZ_NO,
                    CNSTR_TEL ,
                    SUPVS_NM,
                    SUPVS_ADDR ,
                    SUPVS_QLF_NO,
                    SUPVS_DEC_NO,
                    SUPVS_TEL,
                    CREATEUSER,
                    CREATETIME,
                    UPDATEUSER,
                    UPDATETIME,
                    DELETEYN
                )
        VALUES (
                    '5504' ,
                    '10183135' ,
                    '아산 배방 생활숙박시설 - (주)한화(String)' ,
                    '광주광역시, 광산구, 첨단중앙로170번길 17' ,
                    'admin@knauf.com' ,
                    '시공회사' ,
                    '' ,
                    '' ,
                    '' ,
                    '감리회사' ,
                    '' ,
                    '' ,
                    '' ,
                    '' ,
                    'develop' ,
                    GETDATE() ,
                    'develop' ,
                    GETDATE() ,
                    'N'
                )
                ;

























