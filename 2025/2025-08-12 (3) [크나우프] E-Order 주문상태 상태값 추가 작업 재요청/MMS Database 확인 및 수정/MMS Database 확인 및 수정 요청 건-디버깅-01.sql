-- ===========================================================================================
-- 트리거 디버깅 쿼리 - 실패 원인 분석
-- ===========================================================================================

PRINT '=== 1. 로그 테이블 존재 여부 확인 ==='

-- 현재월/이전월 계산
DECLARE @CURR_YM VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112)
DECLARE @PREV_YM VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)

PRINT '현재월: ' + @CURR_YM + ' (MMS_LOG_' + @CURR_YM + ')'
PRINT '이전월: ' + @PREV_YM + ' (MMS_LOG_' + @PREV_YM + ')'

-- 테이블 존재 확인
SELECT 
    'MMS_LOG_' + @CURR_YM AS 테이블명,
    CASE WHEN OBJECT_ID('MMS_LOG_' + @CURR_YM, 'U') IS NOT NULL THEN '존재' ELSE '없음' END AS 상태
UNION ALL
SELECT 
    'MMS_LOG_' + @PREV_YM AS 테이블명,
    CASE WHEN OBJECT_ID('MMS_LOG_' + @PREV_YM, 'U') IS NOT NULL THEN '존재' ELSE '없음' END AS 상태

PRINT ''
PRINT '=== 2. 로그 테이블 데이터 확인 ==='

-- 현재월 로그 데이터 확인
DECLARE @CHECK_CURR_SQL NVARCHAR(MAX) = N'
    SELECT ''현재월로그'' AS 구분, MSGKEY, PHONE, SUBJECT, MSG, REQDATE
    FROM MMS_LOG_' + @CURR_YM + N'
    WHERE MSGKEY BETWEEN 20001 AND 20002
    ORDER BY MSGKEY'

PRINT '현재월 로그 테이블 데이터:'
EXEC sp_executesql @CHECK_CURR_SQL

-- 이전월 로그 데이터 확인  
DECLARE @CHECK_PREV_SQL NVARCHAR(MAX) = N'
    SELECT ''이전월로그'' AS 구분, MSGKEY, PHONE, SUBJECT, MSG, REQDATE  
    FROM MMS_LOG_' + @PREV_YM + N'
    WHERE MSGKEY BETWEEN 20003 AND 20004
    ORDER BY MSGKEY'

PRINT '이전월 로그 테이블 데이터:'
EXEC sp_executesql @CHECK_PREV_SQL

PRINT ''
PRINT '=== 3. 실패한 테스트 데이터의 오더번호 추출 확인 ==='

-- 30003번 오더번호 추출 확인
SELECT 
    '30003번 분석' AS 구분,
    MSGKEY,
    PHONE,
    MSG,
    CHARINDEX(N'오더번호 ', MSG) AS 오더번호위치,
    CASE 
        WHEN CHARINDEX(N'오더번호 ', MSG) > 0 THEN
            LTRIM(RTRIM(SUBSTRING(MSG, 
                CHARINDEX(N'오더번호 ', MSG) + 4, 
                CASE 
                    WHEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호 ', MSG)) > 0 
                    THEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - 4
                    WHEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호 ', MSG)) > 0 
                    THEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - 4
                    WHEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호 ', MSG)) > 0 
                    THEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - 4
                    ELSE 15
                END)))
        ELSE NULL
    END AS 추출된오더번호
FROM MMS_MSG 
WHERE MSGKEY = 30003

-- 30007번 오더번호 추출 확인
SELECT 
    '30007번 분석' AS 구분,
    MSGKEY,
    PHONE, 
    MSG,
    CHARINDEX(N'오더번호 ', MSG) AS 오더번호위치,
    CASE 
        WHEN CHARINDEX(N'오더번호 ', MSG) > 0 THEN
            LTRIM(RTRIM(SUBSTRING(MSG, 
                CHARINDEX(N'오더번호 ', MSG) + 4, 
                CASE 
                    WHEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호 ', MSG)) > 0 
                    THEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - 4
                    WHEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호 ', MSG)) > 0 
                    THEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - 4
                    WHEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호 ', MSG)) > 0 
                    THEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - 4
                    ELSE 15
                END)))
        ELSE NULL
    END AS 추출된오더번호
FROM MMS_MSG 
WHERE MSGKEY = 30007

PRINT ''
PRINT '=== 4. 로그 테이블에서 중복 데이터 수동 검색 ==='

-- 30003번과 매칭되는 현재월 로그 검색
DECLARE @SEARCH_CURR_SQL NVARCHAR(MAX) = N'
    SELECT 
        ''현재월로그매칭'' AS 구분,
        L.MSGKEY, 
        L.PHONE, 
        L.MSG,
        CASE WHEN L.PHONE = ''010-5555-6666'' THEN ''전화번호매칭'' ELSE ''불일치'' END AS 전화번호체크,
        CASE WHEN CHARINDEX(N''ORD333777'', L.MSG) > 0 THEN ''오더번호포함'' ELSE ''불포함'' END AS 오더번호체크
    FROM MMS_LOG_' + @CURR_YM + N' L
    WHERE L.PHONE = ''010-5555-6666'' 
       OR CHARINDEX(N''ORD333777'', L.MSG) > 0'

EXEC sp_executesql @SEARCH_CURR_SQL

-- 30007번과 매칭되는 이전월 로그 검색
DECLARE @SEARCH_PREV_SQL NVARCHAR(MAX) = N'
    SELECT 
        ''이전월로그매칭'' AS 구분,
        L.MSGKEY,
        L.PHONE,
        L.MSG,
        CASE WHEN L.PHONE = ''010-9999-0000'' THEN ''전화번호매칭'' ELSE ''불일치'' END AS 전화번호체크,
        CASE WHEN CHARINDEX(N''ORD444888'', L.MSG) > 0 THEN ''오더번호포함'' ELSE ''불포함'' END AS 오더번호체크
    FROM MMS_LOG_' + @PREV_YM + N' L  
    WHERE L.PHONE = ''010-9999-0000''
       OR CHARINDEX(N''ORD444888'', L.MSG) > 0'

EXEC sp_executesql @SEARCH_PREV_SQL

PRINT ''
PRINT '=== 5. 트리거 동적 SQL 시뮬레이션 ==='

-- 현재월 로그 체크 동적 SQL 확인
DECLARE @SIM_CURR_SQL NVARCHAR(MAX) = N'
    SELECT 
        ''현재월시뮬레이션'' AS 구분,
        COUNT(*) AS 매칭건수
    FROM MMS_MSG M
    INNER JOIN (SELECT 30003 AS MSGKEY, ''010-5555-6666'' AS PHONE, ''ORD333777'' AS ORDER_NO) I 
        ON M.MSGKEY = I.MSGKEY
    WHERE I.ORDER_NO IS NOT NULL 
      AND LEN(I.ORDER_NO) > 0
      AND EXISTS (
            SELECT 1 FROM MMS_LOG_' + @CURR_YM + N' L
            WHERE L.PHONE = I.PHONE 
              AND L.SUBJECT IN (N''크나우프석고보드_보류오더 안내'', N''크나우프석고보드_오더접수'')
              AND (
                     (L.SUBJECT = N''크나우프석고보드_보류오더 안내'' AND CHARINDEX(N''오더번호 '' + I.ORDER_NO, L.MSG) > 0)
                     OR
                     (L.SUBJECT = N''크나우프석고보드_오더접수'' AND CHARINDEX(N''오더번호: '' + I.ORDER_NO, L.MSG) > 0)
                  )
          )'

EXEC sp_executesql @SIM_CURR_SQL

PRINT ''
PRINT '=== 6. 문제 해결 권장사항 ==='
PRINT '1. 로그 테이블에 데이터가 제대로 들어갔는지 확인'
PRINT '2. 오더번호 추출이 올바른지 확인' 
PRINT '3. 전화번호 매칭이 정확한지 확인'
PRINT '4. 동적 SQL의 CHARINDEX 패턴이 맞는지 확인'