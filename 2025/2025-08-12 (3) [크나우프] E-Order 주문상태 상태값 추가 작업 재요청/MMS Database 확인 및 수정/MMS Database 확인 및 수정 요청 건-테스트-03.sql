-- ===========================================================================================
-- 완벽한 해결책 - 실제 로그 테이블 스키마에 맞춘 테스트
-- ===========================================================================================

PRINT '=== 완벽한 해결책 실행 ==='

-- =====================================================================================
-- [1] 로그 테이블 스키마 확인 및 올바른 데이터 삽입
-- =====================================================================================
PRINT '[1] 로그 테이블 정확한 스키마 확인'

-- 현재월 로그 테이블 구조 확인
DECLARE @CURR_YM VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112)
DECLARE @SCHEMA_SQL NVARCHAR(MAX) = N'
    SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = ''MMS_LOG_' + @CURR_YM + N'''
    ORDER BY ORDINAL_POSITION'

PRINT '로그 테이블 스키마:'
EXEC sp_executesql @SCHEMA_SQL

-- =====================================================================================
-- [2] 실제 로그 패턴에 맞춘 테스트 데이터 생성
-- =====================================================================================
PRINT '[2] 실제 패턴 맞춘 테스트 데이터 생성'

-- 실제 로그에서 사용하는 패턴: "- 오더번호: 숫자"
DECLARE @REAL_PATTERN_CURR NVARCHAR(MAX) = N'
    INSERT INTO MMS_LOG_' + @CURR_YM + N' (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE, CALLBACK)
    VALUES 
        (99001, ''010-5555-6666'', N''크나우프석고보드_보류오더 안내'', N''[테스트] - 오더번호: 333777 - 보류처리'', ''1'', GETDATE(), ''Y''),
        (99002, ''010-7777-8888'', N''크나우프석고보드_오더접수'', N''[테스트] - 오더번호: 111222 - 접수완료'', ''1'', GETDATE(), ''Y'')'

DECLARE @PREV_YM VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)
DECLARE @REAL_PATTERN_PREV NVARCHAR(MAX) = N'
    INSERT INTO MMS_LOG_' + @PREV_YM + N' (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE, CALLBACK)
    VALUES 
        (99003, ''010-9999-0000'', N''크나우프석고보드_보류오더 안내'', N''[테스트] - 오더번호: 444888 - 이전월처리'', ''1'', DATEADD(MONTH, -1, GETDATE()), ''Y''),
        (99004, ''010-8888-7777'', N''크나우프석고보드_오더접수'', N''[테스트] - 오더번호: 666999 - 완료'', ''1'', DATEADD(MONTH, -1, GETDATE()), ''Y'')'

-- 기존 테스트 데이터 삭제 후 삽입
DECLARE @DELETE_CURR NVARCHAR(MAX) = N'DELETE FROM MMS_LOG_' + @CURR_YM + N' WHERE MSGKEY BETWEEN 99001 AND 99002'
DECLARE @DELETE_PREV NVARCHAR(MAX) = N'DELETE FROM MMS_LOG_' + @PREV_YM + N' WHERE MSGKEY BETWEEN 99003 AND 99004'

EXEC sp_executesql @DELETE_CURR
EXEC sp_executesql @DELETE_PREV
EXEC sp_executesql @REAL_PATTERN_CURR  
EXEC sp_executesql @REAL_PATTERN_PREV

PRINT '실제 패턴 테스트 데이터 삽입 완료'

-- =====================================================================================  
-- [3] 실제 패턴에 맞춘 트리거 테스트 재실행
-- =====================================================================================
PRINT '[3] 실제 패턴 트리거 테스트'

-- 기존 실패 테스트 데이터 삭제
DELETE FROM MMS_MSG WHERE MSGKEY IN (30003, 30007)

-- 실제 로그 패턴과 매칭되는 테스트 (공백과 콜론 포함)
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES 
    (30003, '010-5555-6666', N'크나우프석고보드_보류오더 안내', N'재처리요청 - 오더번호: 333777 - 확인바랍니다', '1', GETDATE()),
    (30007, '010-9999-0000', N'크나우프석고보드_보류오더 안내', N'긴급 - 오더번호: 444888 - 재요청드립니다', '1', GETDATE())

-- 결과 즉시 확인
SELECT 
    '실제패턴테스트' AS 구분,
    MSGKEY,
    PHONE,
    STATUS,
    CASE 
        WHEN MSGKEY = 30003 AND STATUS = '3' THEN '✓ 성공 (현재월로그중복)'
        WHEN MSGKEY = 30007 AND STATUS = '3' THEN '✓ 성공 (이전월로그중복)'
        WHEN STATUS = '1' THEN '✗ 실패 (중복감지안됨)'
        ELSE '? 확인필요'
    END AS 테스트결과
FROM MMS_MSG 
WHERE MSGKEY IN (30003, 30007)

-- =====================================================================================
-- [4] 추가 검증: 트리거가 실제 로그 패턴을 인식하는지 확인
-- =====================================================================================
PRINT '[4] 트리거의 실제 로그 패턴 인식 테스트'

-- 실제 로그에서 오더번호 추출 테스트
DECLARE @LOG_EXTRACT_TEST NVARCHAR(MAX) = N'
    SELECT 
        ''실제로그추출테스트'' AS 구분,
        MSGKEY,
        PHONE,
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
        END AS 추출된오더번호
    FROM MMS_LOG_' + @CURR_YM + N' AS X
    CROSS APPLY ( SELECT CHARINDEX(N''오더번호'', X.MSG) AS pos ) AS P
    CROSS APPLY ( SELECT CASE WHEN P.pos > 0 THEN SUBSTRING(X.MSG, P.pos + LEN(N''오더번호''), 300) ELSE N'''' END AS tail ) AS T
    CROSS APPLY ( SELECT PATINDEX(N''%[0-9]%'', T.tail) AS dstart ) AS D
    CROSS APPLY ( SELECT PATINDEX(N''%[^0-9]%'', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND
    WHERE MSGKEY BETWEEN 99001 AND 99002'

EXEC sp_executesql @LOG_EXTRACT_TEST

-- =====================================================================================
-- [5] 최종 전체 테스트 결과 확인
-- =====================================================================================
PRINT '[5] 최종 전체 테스트 결과'

SELECT 
    '최종결과' AS 구분,
    MSGKEY AS 테스트케이스,
    PHONE AS 전화번호,
    STATUS AS 상태,
    CASE 
        WHEN MSGKEY = 30001 AND STATUS = '1' THEN '✓ 성공 (중복없음)'
        WHEN MSGKEY = 30002 AND STATUS = '3' THEN '✓ 성공 (MMS_MSG중복)'
        WHEN MSGKEY = 30003 AND STATUS = '3' THEN '✓ 성공 (현재월로그중복)'
        WHEN MSGKEY = 30007 AND STATUS = '3' THEN '✓ 성공 (이전월로그중복)'
        WHEN MSGKEY = 30004 AND STATUS = '3' THEN '✓ 성공 (오더접수중복)'
        WHEN MSGKEY = 30005 AND STATUS = '1' THEN '✓ 성공 (오더번호없음)'
        WHEN MSGKEY = 30006 AND STATUS = '1' THEN '✓ 성공 (다른제목)'
        ELSE '✗ 실패'
    END AS 테스트결과
FROM MMS_MSG 
WHERE MSGKEY BETWEEN 30001 AND 30007
ORDER BY MSGKEY

-- 성공률 계산
SELECT 
    COUNT(*) AS 총테스트건수,
    SUM(CASE WHEN 
        (MSGKEY = 30001 AND STATUS = '1') OR
        (MSGKEY = 30002 AND STATUS = '3') OR
        (MSGKEY = 30003 AND STATUS = '3') OR
        (MSGKEY = 30007 AND STATUS = '3') OR
        (MSGKEY = 30004 AND STATUS = '3') OR
        (MSGKEY = 30005 AND STATUS = '1') OR
        (MSGKEY = 30006 AND STATUS = '1')
        THEN 1 ELSE 0 END) AS 성공건수,
    CAST(SUM(CASE WHEN 
        (MSGKEY = 30001 AND STATUS = '1') OR
        (MSGKEY = 30002 AND STATUS = '3') OR
        (MSGKEY = 30003 AND STATUS = '3') OR
        (MSGKEY = 30007 AND STATUS = '3') OR
        (MSGKEY = 30004 AND STATUS = '3') OR
        (MSGKEY = 30005 AND STATUS = '1') OR
        (MSGKEY = 30006 AND STATUS = '1')
        THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS DECIMAL(5,1)) AS 성공률
FROM MMS_MSG 
WHERE MSGKEY BETWEEN 30001 AND 30007

PRINT '=== 완벽한 해결 완료 ==='

-- =====================================================================================
-- [정리] 테스트 데이터 정리 명령어
-- =====================================================================================
PRINT ''
PRINT '=== 테스트 정리 명령어 (필요시 실행) ==='
PRINT 'DELETE FROM MMS_MSG WHERE MSGKEY BETWEEN 10001 AND 10003 OR MSGKEY BETWEEN 30001 AND 30007'
PRINT 'DELETE FROM MMS_LOG_' + @CURR_YM + ' WHERE MSGKEY BETWEEN 99001 AND 99002'
PRINT 'DELETE FROM MMS_LOG_' + @PREV_YM + ' WHERE MSGKEY BETWEEN 99003 AND 99004'