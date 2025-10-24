-- ===========================================================================================
-- 트리거명 : TRG_MMS_MSG_DUP_CHECK (범용 파싱 + v1.1 포맷)
-- 대상 테이블 : dbo.MMS_MSG
-- 동작 :
--   - INSERT된 MSG에서 '오더번호' 뒤의 숫자 토큰 추출(PATINDEX 기반, 콜론/공백/개행/하이픈 무관)
--   - PHONE + ORDER_NO 기준으로 MMS_MSG(자기 자신 제외), MMS_LOG_현재월/이전월에 존재하면 STATUS='3'
-- 제한 :
--   - 스키마/전송 포맷/인덱스 변경 없음
--   - SUBJECT는 두 유형만 처리
-- ===========================================================================================
ALTER TRIGGER dbo.TRG_MMS_MSG_DUP_CHECK
ON dbo.MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        /* [1] inserted → #ins (대상 SUBJECT만) */
        IF OBJECT_ID('tempdb..#ins', 'U') IS NOT NULL
        BEGIN
            DROP TABLE #ins;
        END;

        SELECT
               I.MSGKEY, I.PHONE, I.SUBJECT, I.MSG
          INTO #ins
          FROM inserted AS I
         WHERE I.SUBJECT IN ( N'크나우프석고보드_보류오더 안내' , N'크나우프석고보드_오더접수' );

        IF @@ROWCOUNT = 0
            RETURN;

        /* [2] inserted 파싱 → #ins_norm (오더번호 범용 추출) */
        IF OBJECT_ID('tempdb..#ins_norm', 'U') IS NOT NULL
        BEGIN
            DROP TABLE #ins_norm;
        END;

        SELECT
               X.MSGKEY
             , X.PHONE
             , X.SUBJECT
             , X.MSG
             , CASE
                   WHEN P.pos > 0 AND D.dstart > 0
                        THEN SUBSTRING(
                                         T.tail
                                       , D.dstart
                                       , CASE
                                             WHEN ND.nextNonDigitPos = 0 THEN 50
                                             ELSE ND.nextNonDigitPos - 1
                                         END
                                      )
                   ELSE NULL
               END AS ORDER_NO
          INTO #ins_norm
          FROM #ins AS X
         CROSS APPLY ( SELECT CHARINDEX(N'오더번호', X.MSG) AS pos ) AS P
         CROSS APPLY ( SELECT CASE WHEN P.pos > 0 THEN SUBSTRING(X.MSG, P.pos + LEN(N'오더번호'), 300) ELSE N'' END AS tail ) AS T
         CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
         CROSS APPLY ( SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND

        IF NOT EXISTS ( SELECT 1 FROM #ins_norm WHERE ORDER_NO IS NOT NULL AND LEN(ORDER_NO) > 0 )
            RETURN

        /* [3] 로그 테이블 명 준비(DECLARE 후 SET로 초기화) */
        DECLARE
              @CURR_YM   VARCHAR(6)
            , @PREV_YM   VARCHAR(6)
            , @CURR_RAW  NVARCHAR(128)
            , @PREV_RAW  NVARCHAR(128)
            , @CURR_Q    NVARCHAR(300)
            , @PREV_Q    NVARCHAR(300)
            , @SQL       NVARCHAR(MAX)

        SET @CURR_YM  = CONVERT(VARCHAR(6), GETDATE(), 112)
        SET @PREV_YM  = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)

        SET @CURR_RAW = N'MMS_LOG_' + @CURR_YM
        SET @PREV_RAW = N'MMS_LOG_' + @PREV_YM

        SET @CURR_Q   = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @CURR_YM)
        SET @PREV_Q   = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @PREV_YM)

        /* [4] MMS_MSG 중복 체크 (자기 자신 제외) */
        UPDATE M
           SET STATUS = N'3'
          FROM dbo.MMS_MSG AS M
          JOIN #ins_norm   AS I
            ON M.MSGKEY = I.MSGKEY
         WHERE I.ORDER_NO IS NOT NULL
           AND LEN(I.ORDER_NO) > 0
           AND EXISTS (
                         SELECT
                                1
                           FROM dbo.MMS_MSG AS MM
                          CROSS APPLY ( SELECT CHARINDEX(N'오더번호', MM.MSG) AS pos ) AS P
                          CROSS APPLY ( SELECT CASE WHEN P.pos > 0 THEN SUBSTRING(MM.MSG, P.pos + LEN(N'오더번호'), 300) ELSE N'' END AS tail ) AS T
                          CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
                          CROSS APPLY ( SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND
                          CROSS APPLY (
                                         SELECT CASE
                                                    WHEN P.pos > 0 AND D.dstart > 0
                                                         THEN SUBSTRING(
                                                                          T.tail
                                                                        , D.dstart
                                                                        , CASE
                                                                              WHEN ND.nextNonDigitPos = 0 THEN 50
                                                                              ELSE ND.nextNonDigitPos - 1
                                                                          END
                                                                       )
                                                    ELSE NULL
                                                END AS ORDER_NO
                                      ) AS O
                          WHERE MM.PHONE    = I.PHONE
                            AND MM.MSGKEY  <> I.MSGKEY
                            AND MM.SUBJECT IN ( N'크나우프석고보드_보류오더 안내' , N'크나우프석고보드_오더접수' )
                            AND O.ORDER_NO  = I.ORDER_NO
                      )

        /* [5] 현재월 로그 검사 */
        IF OBJECT_ID(@CURR_RAW, N'U') IS NOT NULL
        BEGIN
            SET @SQL = N'
                          UPDATE M
                             SET STATUS = N''3''
                            FROM dbo.MMS_MSG AS M
                                 JOIN #ins_norm   AS I
                                   ON M.MSGKEY = I.MSGKEY
                           WHERE I.ORDER_NO IS NOT NULL
                             AND LEN(I.ORDER_NO) > 0
                             AND EXISTS (
                                           SELECT 1
                                             FROM ' + @CURR_Q + N' AS L
                                            CROSS APPLY ( SELECT CHARINDEX(N''오더번호'', L.MSG) AS pos ) AS P
                                            CROSS APPLY (
                                                           SELECT CASE
                                                                      WHEN P.pos > 0 THEN SUBSTRING(L.MSG, P.pos + LEN(N''오더번호''), 300)
                                                                      ELSE N''''
                                                                  END AS tail
                                                        ) AS T
                                            CROSS APPLY ( SELECT PATINDEX(N''%[0-9]%'', T.tail) AS dstart ) AS D
                                            CROSS APPLY ( SELECT PATINDEX(N''%[^0-9]%'', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND
                                            CROSS APPLY (
                                                           SELECT CASE
                                                                      WHEN P.pos > 0 AND D.dstart > 0
                                                                           THEN SUBSTRING(
                                                                                    T.tail
                                                                                  , D.dstart
                                                                                  , CASE
                                                                                        WHEN ND.nextNonDigitPos = 0 THEN 50
                                                                                        ELSE ND.nextNonDigitPos - 1
                                                                                    END
                                                                                )
                                                                      ELSE NULL
                                                                  END AS ORDER_NO
                                                        ) AS O
                                            WHERE L.PHONE    = I.PHONE
                                              AND L.SUBJECT IN ( N''크나우프석고보드_보류오더 안내'', N''크나우프석고보드_오더접수'' )
                                              AND O.ORDER_NO  = I.ORDER_NO
                                        )'
            EXEC sys.sp_executesql @SQL
        END

        /* [6] 이전월 로그 검사 */
        IF OBJECT_ID(@PREV_RAW, N'U') IS NOT NULL
        BEGIN
            SET @SQL = N'
                          UPDATE M
                             SET STATUS = N''3''
                            FROM dbo.MMS_MSG AS M
                                 JOIN #ins_norm   AS I
                                   ON M.MSGKEY = I.MSGKEY
                           WHERE I.ORDER_NO IS NOT NULL
                             AND LEN(I.ORDER_NO) > 0
                             AND EXISTS (
                                           SELECT 1
                                             FROM ' + @PREV_Q + N' AS L
                                            CROSS APPLY ( SELECT CHARINDEX(N''오더번호'', L.MSG) AS pos ) AS P
                                            CROSS APPLY (
                                                           SELECT CASE
                                                                      WHEN P.pos > 0 THEN SUBSTRING(L.MSG, P.pos + LEN(N''오더번호''), 300)
                                                                      ELSE N''''
                                                                  END AS tail
                                                        ) AS T
                                            CROSS APPLY ( SELECT PATINDEX(N''%[0-9]%'', T.tail) AS dstart ) AS D
                                            CROSS APPLY ( SELECT PATINDEX(N''%[^0-9]%'', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND
                                            CROSS APPLY (
                                                            SELECT CASE
                                                                       WHEN P.pos > 0 AND D.dstart > 0
                                                                            THEN SUBSTRING(
                                                                                             T.tail
                                                                                           , D.dstart
                                                                                           , CASE
                                                                                                 WHEN ND.nextNonDigitPos = 0 THEN 50
                                                                                                 ELSE ND.nextNonDigitPos - 1
                                                                                             END
                                                                                          )
                                                                       ELSE NULL
                                                                   END AS ORDER_NO
                                                        ) AS O
                                            WHERE L.PHONE    = I.PHONE
                                              AND L.SUBJECT IN ( N''크나우프석고보드_보류오더 안내'' , N''크나우프석고보드_오더접수'' )
                                              AND O.ORDER_NO  = I.ORDER_NO
                                        )'
            EXEC sys.sp_executesql @SQL
        END
    END TRY
    BEGIN CATCH
        RETURN
    END CATCH
END;
GO
