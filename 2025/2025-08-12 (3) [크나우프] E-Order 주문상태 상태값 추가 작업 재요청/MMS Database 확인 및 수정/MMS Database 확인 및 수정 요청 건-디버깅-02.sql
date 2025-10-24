-- ===========================================================================================
-- 정확한 문제 진단 - 단계별 디버깅
-- ===========================================================================================

PRINT '=== 정확한 문제 진단 시작 ==='

-- =====================================================================================
-- [1] 실제 테스트 데이터 확인
-- =====================================================================================
PRINT '[1] 실제 테스트 데이터 확인'

SELECT 
    '실제데이터' AS 구분,
    MSGKEY,
    PHONE,
    MSG,
    LEN(MSG) AS MSG길이,
    UNICODE(SUBSTRING(MSG, 1, 1)) AS 첫글자유니코드
FROM MMS_MSG 
WHERE MSGKEY IN (30003, 30007)

-- =====================================================================================
-- [2] CHARINDEX 단계별 테스트
-- =====================================================================================
PRINT '[2] CHARINDEX 단계별 테스트'

SELECT 
    '문자검색테스트' AS 구분,
    MSGKEY,
    MSG,
    CHARINDEX(N'오더번호', MSG) AS 오더번호위치,
    CHARINDEX(N'오더', MSG) AS 오더위치,
    CHARINDEX(N'번호', MSG) AS 번호위치,
    CASE WHEN CHARINDEX(N'오더번호', MSG) > 0 THEN '찾음' ELSE '못찾음' END AS 검색결과
FROM MMS_MSG 
WHERE MSGKEY IN (30003, 30007)

-- =====================================================================================
-- [3] 트리거와 동일한 PATINDEX 로직 테스트
-- =====================================================================================
PRINT '[3] 트리거 로직 정확히 따라해보기'

SELECT 
    '트리거로직테스트' AS 구분,
    X.MSGKEY,
    X.MSG,
    P.pos AS 오더번호위치,
    T.tail AS 추출된tail,
    D.dstart AS 숫자시작위치,
    ND.nextNonDigitPos AS 다음비숫자위치,
    CASE
        WHEN P.pos > 0 AND D.dstart > 0
            THEN SUBSTRING(
                    T.tail,
                    D.dstart,
                    CASE
                        WHEN ND.nextNonDigitPos = 0 THEN 50
                        ELSE ND.nextNonDigitPos - 1
                    END
                )
        ELSE NULL
    END AS 최종추출결과
FROM MMS_MSG AS X
CROSS APPLY ( SELECT CHARINDEX(N'오더번호', X.MSG) AS pos ) AS P
CROSS APPLY ( SELECT CASE WHEN P.pos > 0 THEN SUBSTRING(X.MSG, P.pos + LEN(N'오더번호'), 300) ELSE N'' END AS tail ) AS T
CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
CROSS APPLY ( SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND
WHERE MSGKEY IN (30003, 30007)

-- =====================================================================================
-- [4] 로그 테이블 상태 정밀 확인
-- =====================================================================================
PRINT '[4] 로그 테이블 상태 확인'

DECLARE @CURR_YM VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112)
DECLARE @PREV_YM VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)

-- 테이블 존재 확인
SELECT 
    'MMS_LOG_' + @CURR_YM AS 테이블명,
    CASE WHEN OBJECT_ID('MMS_LOG_' + @CURR_YM, 'U') IS NOT NULL THEN '존재함' ELSE '없음' END AS 존재여부,
    CASE WHEN OBJECT_ID('dbo.MMS_LOG_' + @CURR_YM, 'U') IS NOT NULL THEN '스키마포함존재' ELSE '스키마포함없음' END AS 스키마포함존재여부

UNION ALL

SELECT 
    'MMS_LOG_' + @PREV_YM AS 테이블명,
    CASE WHEN OBJECT_ID('MMS_LOG_' + @PREV_YM, 'U') IS NOT NULL THEN '존재함' ELSE '없음' END AS 존재여부,
    CASE WHEN OBJECT_ID('dbo.MMS_LOG_' + @PREV_YM, 'U') IS NOT NULL THEN '스키마포함존재' ELSE '스키마포함없음' END AS 스키마포함존재여부

-- 현재월 로그 데이터 직접 확인
DECLARE @CHECK_CURR NVARCHAR(MAX)
SET @CHECK_CURR = N'
    IF OBJECT_ID(''MMS_LOG_' + @CURR_YM + N''', ''U'') IS NOT NULL
    BEGIN
        SELECT TOP 5 ''현재월로그'' AS 구분, MSGKEY, PHONE, MSG FROM MMS_LOG_' + @CURR_YM + N'
    END
    ELSE
    BEGIN
        SELECT ''현재월로그테이블없음'' AS 구분, 0 AS MSGKEY, '''' AS PHONE, '''' AS MSG
    END'

EXEC sp_executesql @CHECK_CURR

-- 이전월 로그 데이터 직접 확인  
DECLARE @CHECK_PREV NVARCHAR(MAX)
SET @CHECK_PREV = N'
    IF OBJECT_ID(''MMS_LOG_' + @PREV_YM + N''', ''U'') IS NOT NULL
    BEGIN
        SELECT TOP 5 ''이전월로그'' AS 구분, MSGKEY, PHONE, MSG FROM MMS_LOG_' + @PREV_YM + N'
    END
    ELSE
    BEGIN
        SELECT ''이전월로그테이블없음'' AS 구분, 0 AS MSGKEY, '''' AS PHONE, '''' AS MSG
    END'

EXEC sp_executesql @CHECK_PREV

-- =====================================================================================
-- [5] 수동으로 로그 데이터 재삽입 시도
-- =====================================================================================
PRINT '[5] 로그 데이터 재삽입 시도'

-- 현재월 로그 데이터 강제 삽입
DECLARE @FORCE_INSERT_CURR NVARCHAR(MAX)
SET @FORCE_INSERT_CURR = N'
    IF OBJECT_ID(''MMS_LOG_' + @CURR_YM + N''', ''U'') IS NOT NULL
    BEGIN
        DELETE FROM MMS_LOG_' + @CURR_YM + N' WHERE MSGKEY BETWEEN 20001 AND 20004
        INSERT INTO MMS_LOG_' + @CURR_YM + N' (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
        VALUES 
            (20001, ''010-5555-6666'', N''크나우프석고보드_보류오더 안내'', N''오더번호333777보류처리'', ''1'', GETDATE()),
            (20002, ''010-7777-8888'', N''크나우프석고보드_오더접수'', N''오더번호111222접수완료'', ''1'', GETDATE())
        SELECT ''현재월삽입완료'' AS 결과, COUNT(*) AS 건수 FROM MMS_LOG_' + @CURR_YM + N' WHERE MSGKEY BETWEEN 20001 AND 20002
    END'

EXEC sp_executesql @FORCE_INSERT_CURR

-- 이전월 로그 데이터 강제 삽입
DECLARE @FORCE_INSERT_PREV NVARCHAR(MAX)
SET @FORCE_INSERT_PREV = N'
    IF OBJECT_ID(''MMS_LOG_' + @PREV_YM + N''', ''U'') IS NOT NULL
    BEGIN
        DELETE FROM MMS_LOG_' + @PREV_YM + N' WHERE MSGKEY BETWEEN 20003 AND 20004
        INSERT INTO MMS_LOG_' + @PREV_YM + N' (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
        VALUES 
            (20003, ''010-9999-0000'', N''크나우프석고보드_보류오더 안내'', N''오더번호444888이전월처리'', ''1'', DATEADD(MONTH, -1, GETDATE())),
            (20004, ''010-8888-7777'', N''크나우프석고보드_오더접수'', N''오더번호666999완료'', ''1'', DATEADD(MONTH, -1, GETDATE()))
        SELECT ''이전월삽입완료'' AS 결과, COUNT(*) AS 건수 FROM MMS_LOG_' + @PREV_YM + N' WHERE MSGKEY BETWEEN 20003 AND 20004
    END'

EXEC sp_executesql @FORCE_INSERT_PREV

-- =====================================================================================
-- [6] 트리거 활성화 상태 확인
-- =====================================================================================
PRINT '[6] 트리거 상태 확인'

SELECT 
    name AS 트리거명,
    is_disabled AS 비활성화여부,
    CASE WHEN is_disabled = 0 THEN '활성화됨' ELSE '비활성화됨' END AS 상태
FROM sys.triggers 
WHERE name = 'TRG_MMS_MSG_DUP_CHECK'

PRINT '=== 정확한 진단 완료 ==='