-- ===========================================================================================
-- 트리거명 : TRG_MMS_MSG_DUP_CHECK
-- 대상 테이블 : MMS_MSG
-- 동작 : INSERT된 데이터에 대해 중복(PHONE+MSG) 있으면 STATUS=3으로 자동 변경
--        (중복 기준: MMS_MSG 자기자신, MMS_LOG_현재월, MMS_LOG_이전월)
-- 주의 : 동적 쿼리에서는 inserted 테이블 대신 #ins 임시테이블 사용
-- ===========================================================================================

CREATE TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- [1] inserted 테이블 내용을 임시테이블로 복사 (동적 쿼리에서 사용하려면 필수)
    IF OBJECT_ID('tempdb..#ins') IS NOT NULL DROP TABLE #ins;
    SELECT * INTO #ins FROM inserted;

    -- [2] 현재월, 이전월 로그 테이블명 동적으로 생성 (YYYYMM 포맷)
    DECLARE @CURR_YM VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112);
    DECLARE @PREV_YM VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112);
    DECLARE @CURR_LOG_TBL NVARCHAR(64) = N'MMS_LOG_' + @CURR_YM;
    DECLARE @PREV_LOG_TBL NVARCHAR(64) = N'MMS_LOG_' + @PREV_YM;

    -- [3-1] MMS_MSG 자기자신에서 중복(PHONE+MSG, 자기 자신 제외)
    UPDATE M
    SET STATUS = '3'
    FROM MMS_MSG M
    INNER JOIN #ins I ON M.MSGKEY = I.MSGKEY
    WHERE EXISTS (
        SELECT 1 FROM MMS_MSG MM
        WHERE MM.PHONE = I.PHONE AND MM.MSG = I.MSG AND MM.MSGKEY <> I.MSGKEY
    );

    -- [3-2] 현재월 로그 테이블(MMS_LOG_YYYYMM)에서 중복
    IF OBJECT_ID(@CURR_LOG_TBL, 'U') IS NOT NULL
    BEGIN
        DECLARE @SQL_CURR NVARCHAR(MAX) = N'
            UPDATE M
            SET STATUS = ''3''
            FROM MMS_MSG M
            INNER JOIN #ins I ON M.MSGKEY = I.MSGKEY
            WHERE EXISTS (
                SELECT 1 FROM ' + QUOTENAME(@CURR_LOG_TBL) + ' L
                WHERE L.PHONE = I.PHONE AND L.MSG = I.MSG
            )
        ';
        EXEC sp_executesql @SQL_CURR;
    END

    -- [3-3] 이전월 로그 테이블(MMS_LOG_YYYYMM)에서 중복
    IF OBJECT_ID(@PREV_LOG_TBL, 'U') IS NOT NULL
    BEGIN
        DECLARE @SQL_PREV NVARCHAR(MAX) = N'
            UPDATE M
            SET STATUS = ''3''
            FROM MMS_MSG M
            INNER JOIN #ins I ON M.MSGKEY = I.MSGKEY
            WHERE EXISTS (
                SELECT 1 FROM ' + QUOTENAME(@PREV_LOG_TBL) + ' L
                WHERE L.PHONE = I.PHONE AND L.MSG = I.MSG
            )
        ';
        EXEC sp_executesql @SQL_PREV;
    END

    -- [마무리] 트리거 종료 시 임시테이블 자동 삭제 (tempdb 내에서만 유효)

END
GO
