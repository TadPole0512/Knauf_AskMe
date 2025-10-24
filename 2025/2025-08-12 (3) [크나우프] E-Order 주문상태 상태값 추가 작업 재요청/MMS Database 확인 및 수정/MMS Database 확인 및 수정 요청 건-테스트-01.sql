-- ===========================================================================================
-- MMS_MSG 트리거 테스트 데이터 및 시나리오
-- 테스트 순서: 1) 기본 데이터 생성 → 2) 트리거 동작 테스트 → 3) 결과 확인
-- ===========================================================================================

-- =====================================================================================
-- [STEP 1] 테스트용 기본 데이터 생성 (트리거 동작 안 함)
-- =====================================================================================

-- 1-1) 기존 중복 데이터 생성 (트리거가 체크할 대상)
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES 
    (10001, '010-1234-5678', N'크나우프석고보드_보류오더 안내', N'안녕하세요. 오더번호 ORD123456 관련하여 보류 안내드립니다.', '1', GETDATE()),
    (10002, '010-9876-5432', N'크나우프석고보드_오더접수', N'오더접수 완료되었습니다. 오더번호: ORD789012 감사합니다.', '1', GETDATE()),
    (10003, '010-1111-2222', N'크나우프석고보드_보류오더 안내', N'오더번호 ORD555999 처리 지연 안내', '1', GETDATE())

        -- 1-2) 기존 로그 테이블에 중복 체크용 데이터 추가
        DECLARE @CURR_YM VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112)
        DECLARE @PREV_YM VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)
        DECLARE @CURR_LOG_TBL NVARCHAR(128) = QUOTENAME(N'MMS_LOG_' + @CURR_YM)
        DECLARE @PREV_LOG_TBL NVARCHAR(128) = QUOTENAME(N'MMS_LOG_' + @PREV_YM)

        -- 현재월 로그 테이블(MMS_LOG_202508)에 테스트 데이터 추가
        DECLARE @INSERT_CURR_LOG_SQL NVARCHAR(MAX) = N'
            INSERT INTO ' + @CURR_LOG_TBL + N' (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
            VALUES 
                (20001, ''010-5555-6666'', N''크나우프석고보드_보류오더 안내'', N''오더번호 ORD333777 보류 처리되었습니다.'', ''1'', GETDATE()),
                (20002, ''010-7777-8888'', N''크나우프석고보드_오더접수'', N''주문이 접수되었습니다. 오더번호: ORD111222'', ''1'', GETDATE())'

        EXEC sp_executesql @INSERT_CURR_LOG_SQL

        -- 이전월 로그 테이블(MMS_LOG_202507)에 테스트 데이터 추가  
        DECLARE @INSERT_PREV_LOG_SQL NVARCHAR(MAX) = N'
            INSERT INTO ' + @PREV_LOG_TBL + N' (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
            VALUES 
                (20003, ''010-9999-0000'', N''크나우프석고보드_보류오더 안내'', N''오더번호 ORD444888 이전월 보류 안내'', ''1'', DATEADD(MONTH, -1, GETDATE())),
                (20004, ''010-8888-7777'', N''크나우프석고보드_오더접수'', N''이전월 주문: 오더번호: ORD666999'', ''1'', DATEADD(MONTH, -1, GETDATE()))'

        EXEC sp_executesql @INSERT_PREV_LOG_SQL

        PRINT '=== 기본 데이터 생성 완료 ==='
        PRINT '- MMS_MSG 테이블: 3건'
        PRINT '- ' + @CURR_LOG_TBL + ' 테이블: 2건 추가'
        PRINT '- ' + @PREV_LOG_TBL + ' 테이블: 2건 추가'

-- =====================================================================================
-- [STEP 2] 트리거 동작 테스트 시나리오
-- =====================================================================================

PRINT ''
PRINT '=== 트리거 테스트 시작 ==='

-- 2-1) 테스트 케이스 1: 중복 없는 새로운 데이터 (STATUS 변경 안 됨)
PRINT ''
PRINT '[테스트 1] 중복 없는 새로운 데이터 삽입 - STATUS는 1 유지되어야 함'
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES (30001, '010-0000-1111', N'크나우프석고보드_보류오더 안내', N'새로운 오더번호 ORD999888 안내드립니다.', '1', GETDATE())

-- 결과 확인
SELECT '테스트1 결과' AS 테스트, MSGKEY, PHONE, STATUS, 
       CASE WHEN STATUS = '1' THEN '성공(중복없음)' ELSE '실패' END AS 결과
FROM MMS_MSG WHERE MSGKEY = 30001

-- 2-2) 테스트 케이스 2: MMS_MSG 테이블 내 중복 (STATUS → 3 변경됨)
PRINT ''
PRINT '[테스트 2] MMS_MSG 테이블 내 중복 - STATUS가 3으로 변경되어야 함'
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES (30002, '010-1234-5678', N'크나우프석고보드_보류오더 안내', N'재발송: 오더번호 ORD123456 보류 안내', '1', GETDATE())

-- 결과 확인  
SELECT '테스트2 결과' AS 테스트, MSGKEY, PHONE, STATUS,
       CASE WHEN STATUS = '3' THEN '성공(중복감지)' ELSE '실패' END AS 결과
FROM MMS_MSG WHERE MSGKEY = 30002

-- 2-3) 테스트 케이스 3: 로그 테이블과 중복 (STATUS → 3 변경됨)
PRINT ''
PRINT '[테스트 3] 로그 테이블과 중복 - STATUS가 3으로 변경되어야 함'
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES (30003, '010-5555-6666', N'크나우프석고보드_보류오더 안내', N'오더번호 ORD333777 재처리 안내', '1', GETDATE())

-- 결과 확인
SELECT '테스트3 결과' AS 테스트, MSGKEY, PHONE, STATUS,
       CASE WHEN STATUS = '3' THEN '성공(로그중복감지)' ELSE '실패' END AS 결과  
FROM MMS_MSG WHERE MSGKEY = 30003

-- 2-4) 테스트 케이스 4: 오더접수 타입 중복 테스트
PRINT ''
PRINT '[테스트 4] 오더접수 타입 중복 - STATUS가 3으로 변경되어야 함'
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES (30004, '010-9876-5432', N'크나우프석고보드_오더접수', N'중복 주문입니다. 오더번호: ORD789012', '1', GETDATE())

-- 결과 확인
SELECT '테스트4 결과' AS 테스트, MSGKEY, PHONE, STATUS,
       CASE WHEN STATUS = '3' THEN '성공(오더접수중복감지)' ELSE '실패' END AS 결과
FROM MMS_MSG WHERE MSGKEY = 30004

-- 2-5) 테스트 케이스 5: 오더번호 추출 안 되는 경우 (처리 안 함)
PRINT ''
PRINT '[테스트 5] 오더번호 없는 메시지 - STATUS는 1 유지되어야 함'
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)  
VALUES (30005, '010-1234-5678', N'크나우프석고보드_보류오더 안내', N'오더번호가 없는 일반 안내 메시지입니다.', '1', GETDATE())

-- 결과 확인
SELECT '테스트5 결과' AS 테스트, MSGKEY, PHONE, STATUS,
       CASE WHEN STATUS = '1' THEN '성공(오더번호없음)' ELSE '실패' END AS 결과
FROM MMS_MSG WHERE MSGKEY = 30005

        -- 2-6) 테스트 케이스 6: 다른 제목 (트리거 동작 안 함)
        PRINT ''
        PRINT '[테스트 6] 다른 제목 - STATUS는 1 유지되어야 함 (트리거 대상 아님)'
        INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
        VALUES (30006, '010-1234-5678', N'일반 안내 메시지', N'오더번호 ORD123456 관련 안내', '1', GETDATE())

        -- 결과 확인
        SELECT '테스트6 결과' AS 테스트, MSGKEY, PHONE, STATUS,
               CASE WHEN STATUS = '1' THEN '성공(대상아님)' ELSE '실패' END AS 결과
        FROM MMS_MSG WHERE MSGKEY = 30006

        -- 2-7) 테스트 케이스 7: 이전월 로그와 중복 (STATUS → 3 변경됨)  
        PRINT ''
        PRINT '[테스트 7] 이전월 로그 테이블과 중복 - STATUS가 3으로 변경되어야 함'
        INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
        VALUES (30007, '010-9999-0000', N'크나우프석고보드_보류오더 안내', N'오더번호 ORD444888 재처리 요청', '1', GETDATE())

        -- 결과 확인
        SELECT '테스트7 결과' AS 테스트, MSGKEY, PHONE, STATUS,
               CASE WHEN STATUS = '3' THEN '성공(이전월로그중복감지)' ELSE '실패' END AS 결과
        FROM MMS_MSG WHERE MSGKEY = 30007

-- =====================================================================================
-- [STEP 3] 전체 테스트 결과 요약
-- =====================================================================================

PRINT ''
PRINT '=== 전체 테스트 결과 요약 ==='

SELECT 
    '전체 결과' AS 구분,
    COUNT(*) AS 총테스트건수,
    SUM(CASE WHEN 
        (MSGKEY = 30001 AND STATUS = '1') OR  -- 중복없음
        (MSGKEY = 30002 AND STATUS = '3') OR  -- MMS_MSG 중복  
        (MSGKEY = 30003 AND STATUS = '3') OR  -- 현재월로그 중복
        (MSGKEY = 30004 AND STATUS = '3') OR  -- 오더접수 중복
        (MSGKEY = 30005 AND STATUS = '1') OR  -- 오더번호없음
        (MSGKEY = 30006 AND STATUS = '1') OR  -- 대상아님
        (MSGKEY = 30007 AND STATUS = '3')     -- 이전월로그 중복
        THEN 1 ELSE 0 END) AS 성공건수,
    SUM(CASE WHEN 
        (MSGKEY = 30001 AND STATUS != '1') OR  
        (MSGKEY = 30002 AND STATUS != '3') OR  
        (MSGKEY = 30003 AND STATUS != '3') OR  
        (MSGKEY = 30004 AND STATUS != '3') OR  
        (MSGKEY = 30005 AND STATUS != '1') OR  
        (MSGKEY = 30006 AND STATUS != '1') OR
        (MSGKEY = 30007 AND STATUS != '3')
        THEN 1 ELSE 0 END) AS 실패건수
FROM MMS_MSG 
WHERE MSGKEY BETWEEN 30001 AND 30007

-- 상세 결과
SELECT 
    MSGKEY AS 테스트케이스,
    PHONE AS 전화번호,
    STATUS AS 상태,
    CASE 
        WHEN MSGKEY = 30001 AND STATUS = '1' THEN '✓ 성공 (중복없음)'
        WHEN MSGKEY = 30002 AND STATUS = '3' THEN '✓ 성공 (MMS_MSG중복감지)'
        WHEN MSGKEY = 30003 AND STATUS = '3' THEN '✓ 성공 (현재월로그중복감지)'
        WHEN MSGKEY = 30004 AND STATUS = '3' THEN '✓ 성공 (오더접수중복감지)'
        WHEN MSGKEY = 30005 AND STATUS = '1' THEN '✓ 성공 (오더번호없음)'
        WHEN MSGKEY = 30006 AND STATUS = '1' THEN '✓ 성공 (대상아님)'
        WHEN MSGKEY = 30007 AND STATUS = '3' THEN '✓ 성공 (이전월로그중복감지)'
        ELSE '✗ 실패'
    END AS 테스트결과
FROM MMS_MSG 
WHERE MSGKEY BETWEEN 30001 AND 30007
ORDER BY MSGKEY

-- =====================================================================================
-- [STEP 4] 테스트 데이터 정리 (선택사항)
-- =====================================================================================

PRINT ''
PRINT '=== 테스트 정리 명령어 (필요시 실행) ==='
PRINT '-- 테스트 데이터 삭제'
PRINT 'DELETE FROM MMS_MSG WHERE MSGKEY BETWEEN 10001 AND 10003 OR MSGKEY BETWEEN 30001 AND 30007'
PRINT '-- 로그 테이블 테스트 데이터 삭제'
PRINT 'DELETE FROM MMS_LOG_202508 WHERE MSGKEY BETWEEN 20001 AND 20002'
PRINT 'DELETE FROM MMS_LOG_202507 WHERE MSGKEY BETWEEN 20003 AND 20004'

-- =====================================================================================
-- [추가] 오더번호 추출 결과 확인 쿼리
-- =====================================================================================

PRINT ''
PRINT '=== 오더번호 추출 확인 쿼리 ==='
PRINT '-- 아래 쿼리로 오더번호가 제대로 추출되는지 확인 가능'
PRINT ''

SELECT 
    MSGKEY,
    SUBJECT,
    MSG,
    CASE SUBJECT
        WHEN N'크나우프석고보드_보류오더 안내' THEN 
            CASE WHEN CHARINDEX(N'오더번호 ', MSG) > 0 THEN
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
            END
        WHEN N'크나우프석고보드_오더접수' THEN 
            CASE WHEN CHARINDEX(N'오더번호: ', MSG) > 0 THEN
                LTRIM(RTRIM(SUBSTRING(MSG, 
                    CHARINDEX(N'오더번호: ', MSG) + 5, 
                    CASE 
                        WHEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호: ', MSG)) > 0 
                        THEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호: ', MSG)) - CHARINDEX(N'오더번호: ', MSG) - 5
                        WHEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호: ', MSG)) > 0 
                        THEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호: ', MSG)) - CHARINDEX(N'오더번호: ', MSG) - 5
                        WHEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호: ', MSG)) > 0 
                        THEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호: ', MSG)) - CHARINDEX(N'오더번호: ', MSG) - 5
                        ELSE 15
                    END)))
                ELSE NULL
            END
        ELSE NULL
    END AS 추출된오더번호
FROM MMS_MSG 
WHERE SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수')
ORDER BY REQDATE DESC