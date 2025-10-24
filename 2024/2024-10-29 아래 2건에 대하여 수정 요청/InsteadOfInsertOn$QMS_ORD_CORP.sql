
ALTER TRIGGER dbo.InsteadOfInsertOn$QMS_ORD_CORP
   ON dbo.QMS_ORD_CORP
    INSTEAD OF INSERT
   AS
      BEGIN

         SET  NOCOUNT  ON

         /* column variables declaration*/
         DECLARE
            @NEW$0 uniqueidentifier,
            /*
            *   SSMA warning messages:
            *   O2SS0356: Conversion from NUMBER datatype can cause data loss.
            */

            @NEW$SHIPTO_SEQ float(53),
            @NEW$SHIPTO_CD varchar(100),
            @NEW$SHIPTO_NM varchar(300),
            @NEW$SHIPTO_ADDR varchar(300),
            @NEW$SHIPTO_TEL varchar(100),
            @NEW$SHIPTO_EMAIL varchar(100),
            @NEW$CNSTR_CD varchar(100),
            @NEW$CNSTR_NM varchar(100),
            @NEW$CNSTR_ADDR varchar(300),
            @NEW$CNSTR_BIZ_NO varchar(100),
            @NEW$CNSTR_TEL varchar(100),
            @NEW$CNSTR_EMAIL varchar(300),
            @NEW$SUPVS_CD varchar(100),
            @NEW$SUPVS_NM varchar(100),
            @NEW$SUPVS_ADDR varchar(300),
            @NEW$SUPVS_BIZ_NO varchar(100),
            @NEW$SUPVS_TEL varchar(100),
            @NEW$SUPVS_EMAIL varchar(100),
            @NEW$CREATEUSER varchar(100),
            @NEW$CREATETIME datetime2(0),
            @NEW$UPDATEUSER varchar(100),
            @NEW$UPDATETIME datetime2(0),
            @NEW$DELETEYN varchar(1),
            @NEW$SUPVS_QLF_NO varchar(50),
            @NEW$SUPVS_DEC_NO varchar(50)

         DECLARE
             ForEachInsertedRowTriggerCursor CURSOR LOCAL FORWARD_ONLY READ_ONLY FOR
               SELECT
                  ROWID,
                  SHIPTO_SEQ,
                  SHIPTO_CD,
                  SHIPTO_NM,
                  SHIPTO_ADDR,
                  SHIPTO_TEL,
                  SHIPTO_EMAIL,
                  CNSTR_CD,
                  CNSTR_NM,
                  CNSTR_ADDR,
                  CNSTR_BIZ_NO,
                  CNSTR_TEL,
                  CNSTR_EMAIL,
                  SUPVS_CD,
                  SUPVS_NM,
                  SUPVS_ADDR,
                  SUPVS_BIZ_NO,
                  SUPVS_TEL,
                  SUPVS_EMAIL,
                  CREATEUSER,
                  CREATETIME,
                  UPDATEUSER,
                  UPDATETIME,
                  DELETEYN,
                  SUPVS_QLF_NO,
                  SUPVS_DEC_NO
               FROM inserted

         OPEN ForEachInsertedRowTriggerCursor

         FETCH ForEachInsertedRowTriggerCursor
             INTO
               @NEW$0,
               @NEW$SHIPTO_SEQ,
               @NEW$SHIPTO_CD,
               @NEW$SHIPTO_NM,
               @NEW$SHIPTO_ADDR,
               @NEW$SHIPTO_TEL,
               @NEW$SHIPTO_EMAIL,
               @NEW$CNSTR_CD,
               @NEW$CNSTR_NM,
               @NEW$CNSTR_ADDR,
               @NEW$CNSTR_BIZ_NO,
               @NEW$CNSTR_TEL,
               @NEW$CNSTR_EMAIL,
               @NEW$SUPVS_CD,
               @NEW$SUPVS_NM,
               @NEW$SUPVS_ADDR,
               @NEW$SUPVS_BIZ_NO,
               @NEW$SUPVS_TEL,
               @NEW$SUPVS_EMAIL,
               @NEW$CREATEUSER,
               @NEW$CREATETIME,
               @NEW$UPDATEUSER,
               @NEW$UPDATETIME,
               @NEW$DELETEYN,
               @NEW$SUPVS_QLF_NO,
               @NEW$SUPVS_DEC_NO

         WHILE @@fetch_status = 0

            BEGIN

               /* row-level triggers implementation: begin*/
               BEGIN
                  BEGIN
                     /* 2024-10-29 HSG CoCoVenus Primary Key 인 'SHIPTO_SEQ'가 자동증가(IDENTITY) 설정이 되어 있지않아, 최대값(MAX)을 구하여 작업 진행 */
                     /* SELECT @NEW$SHIPTO_SEQ = NEXT VALUE FOR dbo.QMS_ORD_CORP_SEQ */
                     SELECT @NEW$SHIPTO_SEQ = MAX(SHIPTO_SEQ)+1 FROM QMS_ORD_CORP
                  END
               END
               /* row-level triggers implementation: end*/

               /* DML-operation emulation*/
               INSERT dbo.QMS_ORD_CORP(
                  ROWID,
                  SHIPTO_SEQ,
                  SHIPTO_CD,
                  SHIPTO_NM,
                  SHIPTO_ADDR,
                  SHIPTO_TEL,
                  SHIPTO_EMAIL,
                  CNSTR_CD,
                  CNSTR_NM,
                  CNSTR_ADDR,
                  CNSTR_BIZ_NO,
                  CNSTR_TEL,
                  CNSTR_EMAIL,
                  SUPVS_CD,
                  SUPVS_NM,
                  SUPVS_ADDR,
                  SUPVS_BIZ_NO,
                  SUPVS_TEL,
                  SUPVS_EMAIL,
                  CREATEUSER,
                  CREATETIME,
                  UPDATEUSER,
                  UPDATETIME,
                  DELETEYN,
                  SUPVS_QLF_NO,
                  SUPVS_DEC_NO)
                  VALUES (
                     @NEW$0,
                     @NEW$SHIPTO_SEQ,
                     @NEW$SHIPTO_CD,
                     @NEW$SHIPTO_NM,
                     @NEW$SHIPTO_ADDR,
                     @NEW$SHIPTO_TEL,
                     @NEW$SHIPTO_EMAIL,
                     @NEW$CNSTR_CD,
                     @NEW$CNSTR_NM,
                     @NEW$CNSTR_ADDR,
                     @NEW$CNSTR_BIZ_NO,
                     @NEW$CNSTR_TEL,
                     @NEW$CNSTR_EMAIL,
                     @NEW$SUPVS_CD,
                     @NEW$SUPVS_NM,
                     @NEW$SUPVS_ADDR,
                     @NEW$SUPVS_BIZ_NO,
                     @NEW$SUPVS_TEL,
                     @NEW$SUPVS_EMAIL,
                     @NEW$CREATEUSER,
                     @NEW$CREATETIME,
                     @NEW$UPDATEUSER,
                     @NEW$UPDATETIME,
                     @NEW$DELETEYN,
                     @NEW$SUPVS_QLF_NO,
                     @NEW$SUPVS_DEC_NO)

               FETCH ForEachInsertedRowTriggerCursor
                   INTO
                     @NEW$0,
                     @NEW$SHIPTO_SEQ,
                     @NEW$SHIPTO_CD,
                     @NEW$SHIPTO_NM,
                     @NEW$SHIPTO_ADDR,
                     @NEW$SHIPTO_TEL,
                     @NEW$SHIPTO_EMAIL,
                     @NEW$CNSTR_CD,
                     @NEW$CNSTR_NM,
                     @NEW$CNSTR_ADDR,
                     @NEW$CNSTR_BIZ_NO,
                     @NEW$CNSTR_TEL,
                     @NEW$CNSTR_EMAIL,
                     @NEW$SUPVS_CD,
                     @NEW$SUPVS_NM,
                     @NEW$SUPVS_ADDR,
                     @NEW$SUPVS_BIZ_NO,
                     @NEW$SUPVS_TEL,
                     @NEW$SUPVS_EMAIL,
                     @NEW$CREATEUSER,
                     @NEW$CREATETIME,
                     @NEW$UPDATEUSER,
                     @NEW$UPDATETIME,
                     @NEW$DELETEYN,
                     @NEW$SUPVS_QLF_NO,
                     @NEW$SUPVS_DEC_NO

            END

         CLOSE ForEachInsertedRowTriggerCursor

         DEALLOCATE ForEachInsertedRowTriggerCursor

      END
