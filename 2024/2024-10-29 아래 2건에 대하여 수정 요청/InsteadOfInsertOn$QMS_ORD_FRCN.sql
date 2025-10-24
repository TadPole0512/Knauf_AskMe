
CREATE TRIGGER dbo.InsteadOfInsertOn$QMS_ORD_FRCN
   ON dbo.QMS_ORD_FRCN
    INSTEAD OF INSERT
   AS 
      BEGIN

         SET  NOCOUNT  ON

         /* column variables declaration*/
         DECLARE
            @NEW$0 uniqueidentifier, 
            @NEW$QMS_ID varchar(100), 
            /*
            *   SSMA warning messages:
            *   O2SS0356: Conversion from NUMBER datatype can cause data loss.
            */

            @NEW$QMS_SEQ float(53), 
            /*
            *   SSMA warning messages:
            *   O2SS0356: Conversion from NUMBER datatype can cause data loss.
            */

            @NEW$QMS_FRCN_ID float(53), 
            @NEW$KEYCODE varchar(6), 
            @NEW$CREATEUSER varchar(20), 
            @NEW$CREATETIME datetime2(0), 
            @NEW$UPDATEUSER varchar(20), 
            @NEW$UPDATETIME datetime2(0), 
            @NEW$DELETEYN varchar(1)

         DECLARE
             ForEachInsertedRowTriggerCursor CURSOR LOCAL FORWARD_ONLY READ_ONLY FOR 
               SELECT 
                  ROWID, 
                  QMS_ID, 
                  QMS_SEQ, 
                  QMS_FRCN_ID, 
                  KEYCODE, 
                  CREATEUSER, 
                  CREATETIME, 
                  UPDATEUSER, 
                  UPDATETIME, 
                  DELETEYN
               FROM inserted

         OPEN ForEachInsertedRowTriggerCursor

         FETCH ForEachInsertedRowTriggerCursor
             INTO 
               @NEW$0, 
               @NEW$QMS_ID, 
               @NEW$QMS_SEQ, 
               @NEW$QMS_FRCN_ID, 
               @NEW$KEYCODE, 
               @NEW$CREATEUSER, 
               @NEW$CREATETIME, 
               @NEW$UPDATEUSER, 
               @NEW$UPDATETIME, 
               @NEW$DELETEYN

         WHILE @@fetch_status = 0
         
            BEGIN

               /* row-level triggers implementation: begin*/
               BEGIN
                  BEGIN
                     /* 2024-10-31 HSG CoCoVenus Primary Key 인 'SHIPTO_SEQ'가 자동증가(IDENTITY) 설정이 되어 있지않아, 최대값(MAX)을 구하여 작업 진행 */
                     /* SELECT @NEW$QMS_FRCN_ID = NEXT VALUE FOR dbo.QMS_ORD_FRCN_SEQ */
                     SELECT @NEW$QMS_FRCN_ID = MAX(QMS_FRCN_ID)+1 FROM QMS_ORD_FRCN
                  END
               END
               /* row-level triggers implementation: end*/

               /* DML-operation emulation*/
               INSERT dbo.QMS_ORD_FRCN(
                  ROWID, 
                  QMS_ID, 
                  QMS_SEQ, 
                  QMS_FRCN_ID, 
                  KEYCODE, 
                  CREATEUSER, 
                  CREATETIME, 
                  UPDATEUSER, 
                  UPDATETIME, 
                  DELETEYN)
                  VALUES (
                     @NEW$0, 
                     @NEW$QMS_ID, 
                     @NEW$QMS_SEQ, 
                     @NEW$QMS_FRCN_ID, 
                     @NEW$KEYCODE, 
                     @NEW$CREATEUSER, 
                     @NEW$CREATETIME, 
                     @NEW$UPDATEUSER, 
                     @NEW$UPDATETIME, 
                     @NEW$DELETEYN)

               FETCH ForEachInsertedRowTriggerCursor
                   INTO 
                     @NEW$0, 
                     @NEW$QMS_ID, 
                     @NEW$QMS_SEQ, 
                     @NEW$QMS_FRCN_ID, 
                     @NEW$KEYCODE, 
                     @NEW$CREATEUSER, 
                     @NEW$CREATETIME, 
                     @NEW$UPDATEUSER, 
                     @NEW$UPDATETIME, 
                     @NEW$DELETEYN

            END

         CLOSE ForEachInsertedRowTriggerCursor

         DEALLOCATE ForEachInsertedRowTriggerCursor

      END
