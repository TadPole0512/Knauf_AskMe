-- ===========================================================================================
-- 수정된 트리거 테스트 시나리오 (PATINDEX 버전)
-- ===========================================================================================

PRINT '=== 트리거 테스트 시작 (PATINDEX 버전) ==='

-- =====================================================================================
-- [STEP 1] 기존 테스트 데이터 정리
-- =====================================================================================
PRINT '[STEP 1] 기존 테스트 데이터 정리'

-- 기존 테스트 데이터 삭제
DELETE FROM MMS_MSG WHERE MSGKEY BETWEEN 10001 AND 10003 OR MSGKEY BETWEEN 30001 AND 30007

-- 로그 테이블 테스트 데이터 삭제
DECLARE @CURR_YM VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112)
DECLARE @PREV_YM VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)

DECLARE @CLEAN_CURR_SQL NVARCHAR(MAX) = N'DELETE FROM MMS_LOG_' + @CURR_YM + N' WHERE MSGKEY BETWEEN 20001 AND 20004'
DECLARE @CLEAN_PREV_SQL NVARCHAR(MAX) = N'DELETE FROM MMS_LOG_' + @PREV_YM + N' WHERE MSGKEY BETWEEN 20001 AND 20004'

EXEC sp_executesql @CLEAN_CURR_SQL
EXEC sp_executesql @CLEAN_PREV_SQL

PRINT '기존 데이터 정리 완료'

-- =====================================================================================
-- [STEP 2] 새로운 기본 데이터 생성 (트리거가 체크할 대상)
-- =====================================================================================
PRINT '[STEP 2] 기본 데이터 생성'

-- 기본 중복 체크용 데이터 (다양한 패턴으로 테스트)
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES 
    (10001, '010-1234-5678', N'크나우프석고보드_보류오더 안내', N'안녕하세요. 오더번호 123456 보류 안내드립니다.', '1', GETDATE()),
    (10002, '010-9876-5432', N'크나우프석고보드_오더접수', N'오더접수: 오더번호789012 처리완료', '1', GETDATE()),
    (10003, '010-1111-2222', N'크나우프석고보드_보류오더 안내', N'오더번호: 555999번 지연안내', '1', GETDATE())

-- 로그 테이블 데이터 생성
DECLARE @INSERT_CURR_LOG NVARCHAR(MAX) = N'
    INSERT INTO MMS_LOG_' + @CURR_YM + N' (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
    VALUES 
        (20001, ''010-5555-6666'', N''크나우프석고보드_보류오더 안내'', N''오더번호 333777 보류처리'', ''1'', GETDATE()),
        (20002, ''010-7777-8888'', N''크나우프석고보드_오더접수'', N''오더번호:111222 접수완료'', ''1'', GETDATE())'

DECLARE @INSERT_PREV_LOG NVARCHAR(MAX) = N'
    INSERT INTO MMS_LOG_' + @PREV_YM + N' (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
    VALUES 
        (20003, ''010-9999-0000'', N''크나우프석고보드_보류오더 안내'', N''오더번호444888이전월처리'', ''1'', DATEADD(MONTH, -1, GETDATE())),
        (20004, ''010-8888-7777'', N''크나우프석고보드_오더접수'', N''오더번호: 666999 완료'', ''1'', DATEADD(MONTH, -1, GETDATE()))'

EXEC sp_executesql @INSERT_CURR_LOG
EXEC sp_executesql @INSERT_PREV_LOG

PRINT '기본 데이터 생성 완료'

-- =====================================================================================
-- [STEP 3] PATINDEX 오더번호 추출 테스트
-- =====================================================================================
PRINT '[STEP 3] PATINDEX 오더번호 추출 로직 검증'

-- 추출 로직 테스트 (트리거와 동일한 로직)
SELECT 
    '추출테스트' AS 구분,
    MSGKEY,
    MSG,
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
FROM MMS_MSG AS X
CROSS APPLY ( SELECT CHARINDEX(N'오더번호', X.MSG) AS pos ) AS P
CROSS APPLY ( SELECT CASE WHEN P.pos > 0 THEN SUBSTRING(X.MSG, P.pos + LEN(N'오더번호'), 300) ELSE N'' END AS tail ) AS T
CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
CROSS APPLY ( SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND
WHERE MSGKEY BETWEEN 10001 AND 10003
ORDER BY MSGKEY

-- =====================================================================================
-- [STEP 4] 트리거 테스트 실행
-- =====================================================================================
PRINT '[STEP 4] 트리거 테스트 시작'

-- 테스트 1: 중복 없는 새 데이터
PRINT '테스트 1: 중복 없음 (STATUS=1 유지)'
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES (30001, '010-0000-1111', N'크나우프석고보드_보류오더 안내', N'새로운 오더번호 999888', '1', GETDATE())

SELECT '테스트1' AS 결과, MSGKEY, STATUS, 
       CASE WHEN STATUS = '1' THEN '✓ 성공' ELSE '✗ 실패' END AS 판정
FROM MMS_MSG WHERE MSGKEY = 30001

-- 테스트 2: MMS_MSG 내 중복 (123456)
PRINT '테스트 2: MMS_MSG 중복 (STATUS=3 변경)'
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES (30002, '010-1234-5678', N'크나우프석고보드_보류오더 안내', N'재발송 오더번호 123456 안내', '1', GETDATE())

SELECT '테스트2' AS 결과, MSGKEY, STATUS,
       CASE WHEN STATUS = '3' THEN '✓ 성공' ELSE '✗ 실패' END AS 판정
FROM MMS_MSG WHERE MSGKEY = 30002

-- 테스트 3: 현재월 로그와 중복 (333777)
PRINT '테스트 3: 현재월 로그 중복 (STATUS=3 변경)'
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES (30003, '010-5555-6666', N'크나우프석고보드_보류오더 안내', N'오더번호333777재처리', '1', GETDATE())

SELECT '테스트3' AS 결과, MSGKEY, STATUS,
       CASE WHEN STATUS = '3' THEN '✓ 성공' ELSE '✗ 실패' END AS 판정
FROM MMS_MSG WHERE MSGKEY = 30003

-- 테스트 4: 이전월 로그와 중복 (444888)
PRINT '테스트 4: 이전월 로그 중복 (STATUS=3 변경)'
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES (30007, '010-9999-0000', N'크나우프석고보드_보류오더 안내', N'오더번호444888 재요청', '1', GETDATE())

SELECT '테스트4' AS 결과, MSGKEY, STATUS,
       CASE WHEN STATUS = '3' THEN '✓ 성공' ELSE '✗ 실패' END AS 판정
FROM MMS_MSG WHERE MSGKEY = 30007

-- 테스트 5: 오더접수 타입 중복 (789012)
PRINT '테스트 5: 오더접수 타입 중복 (STATUS=3 변경)'  
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES (30004, '010-9876-5432', N'크나우프석고보드_오더접수', N'중복주문 오더번호789012', '1', GETDATE())

SELECT '테스트5' AS 결과, MSGKEY, STATUS,
       CASE WHEN STATUS = '3' THEN '✓ 성공' ELSE '✗ 실패' END AS 판정
FROM MMS_MSG WHERE MSGKEY = 30004

-- 테스트 6: 오더번호 없는 메시지
PRINT '테스트 6: 오더번호 없음 (STATUS=1 유지)'
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES (30005, '010-1234-5678', N'크나우프석고보드_보류오더 안내', N'일반 안내 메시지입니다', '1', GETDATE())

SELECT '테스트6' AS 결과, MSGKEY, STATUS,
       CASE WHEN STATUS = '1' THEN '✓ 성공' ELSE '✗ 실패' END AS 판정
FROM MMS_MSG WHERE MSGKEY = 30005

-- 테스트 7: 다른 제목 (트리거 대상 아님)
PRINT '테스트 7: 다른 제목 (STATUS=1 유지)'
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES (30006, '010-1234-5678', N'일반 안내', N'오더번호 123456 관련', '1', GETDATE())

SELECT '테스트7' AS 결과, MSGKEY, STATUS,
       CASE WHEN STATUS = '1' THEN '✓ 성공' ELSE '✗ 실패' END AS 판정
FROM MMS_MSG WHERE MSGKEY = 30006

-- =====================================================================================
-- [STEP 5] 전체 테스트 결과 요약
-- =====================================================================================
PRINT '[STEP 5] 전체 결과 요약'

SELECT 
    '전체결과' AS 구분,
    COUNT(*) AS 총테스트건수,
    SUM(CASE WHEN 
        (MSGKEY = 30001 AND STATUS = '1') OR  -- 중복없음
        (MSGKEY = 30002 AND STATUS = '3') OR  -- MMS_MSG중복
        (MSGKEY = 30003 AND STATUS = '3') OR  -- 현재월로그중복
        (MSGKEY = 30007 AND STATUS = '3') OR  -- 이전월로그중복
        (MSGKEY = 30004 AND STATUS = '3') OR  -- 오더접수중복
        (MSGKEY = 30005 AND STATUS = '1') OR  -- 오더번호없음
        (MSGKEY = 30006 AND STATUS = '1')     -- 다른제목
        THEN 1 ELSE 0 END) AS 성공건수,
    SUM(CASE WHEN 
        (MSGKEY = 30001 AND STATUS != '1') OR  
        (MSGKEY = 30002 AND STATUS != '3') OR  
        (MSGKEY = 30003 AND STATUS != '3') OR  
        (MSGKEY = 30007 AND STATUS != '3') OR
        (MSGKEY = 30004 AND STATUS != '3') OR  
        (MSGKEY = 30005 AND STATUS != '1') OR  
        (MSGKEY = 30006 AND STATUS != '1')
        THEN 1 ELSE 0 END) AS 실패건수
FROM MMS_MSG 
WHERE MSGKEY BETWEEN 30001 AND 30007

-- 상세 결과
SELECT 
    MSGKEY AS 테스트케이스,
    PHONE AS 전화번호,
    SUBJECT AS 제목,
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

PRINT '=== 테스트 완료 ==='