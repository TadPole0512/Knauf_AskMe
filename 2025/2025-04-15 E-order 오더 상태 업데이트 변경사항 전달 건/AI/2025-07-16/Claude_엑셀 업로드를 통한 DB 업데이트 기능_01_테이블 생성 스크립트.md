위에서 정리해준 내용으로 생성해준 테이블 생성 쿼리에 대한 코멘트도 만들어줘.

CREATE TABLE EXCEL_PROCESS_LOG (
    PROCESS_ID VARCHAR(50) PRIMARY KEY,
    USER_ID VARCHAR(50),
    FILE_NAME VARCHAR(255),
    TOTAL_ROWS INT,
    PROCESSED_ROWS INT,
    STATUS VARCHAR(20), -- PROCESSING, COMPLETED, FAILED
    START_TIME DATETIME,
    END_TIME DATETIME,
    ERROR_MESSAGE TEXT
);

CREATE TABLE EXCEL_PROCESS_ERRORS (
    ERROR_ID INT IDENTITY(1,1) PRIMARY KEY,
    PROCESS_ID VARCHAR(50),
    ROW_NUMBER INT,
    SALES_DOCUMENT VARCHAR(50),
    SALES_DOCUMENT_ITEM VARCHAR(50),
    ERROR_TYPE VARCHAR(50),
    ERROR_MESSAGE VARCHAR(500),
    FOREIGN KEY (PROCESS_ID) REFERENCES EXCEL_PROCESS_LOG(PROCESS_ID)
);




---------------------------------------------------------------------



-- =====================================================
-- 엑셀 업로드 시스템 테이블 생성 스크립트
-- 개발환경: MS SQL Server
-- 작성일: 2025-07-16
-- 목적: 엑셀 파일 업로드 및 처리 과정 관리
-- =====================================================

-- 1. 엑셀 처리 과정 로그 테이블
-- 용도: 엑셀 파일 업로드 및 처리 과정의 전체 상태를 추적
CREATE TABLE EXCEL_PROCESS_LOG (
    PROCESS_ID VARCHAR(50) PRIMARY KEY,           -- 처리 과정 고유 식별자 (UUID 형태)
    USER_ID VARCHAR(50),                          -- 업로드한 사용자 ID
    FILE_NAME VARCHAR(255),                       -- 업로드된 엑셀 파일명
    TOTAL_ROWS INT,                               -- 엑셀 파일의 총 행 수 (헤더 제외)
    PROCESSED_ROWS INT,                           -- 현재까지 처리된 행 수
    STATUS VARCHAR(20),                           -- 처리 상태 (PROCESSING, COMPLETED, FAILED)
    START_TIME DATETIME,                          -- 처리 시작 시간
    END_TIME DATETIME,                            -- 처리 완료 시간
    ERROR_MESSAGE TEXT                            -- 전체 프로세스 실패 시 에러 메시지
);

-- 2. 엑셀 처리 에러 상세 테이블
-- 용도: 개별 행 처리 실패에 대한 상세 정보 저장
CREATE TABLE EXCEL_PROCESS_ERRORS (
    ERROR_ID INT IDENTITY(1,1) PRIMARY KEY,      -- 에러 고유 식별자 (자동 증가)
    PROCESS_ID VARCHAR(50),                      -- 처리 과정 ID (EXCEL_PROCESS_LOG 참조)
    ROW_NUMBER INT,                              -- 에러 발생한 엑셀 행 번호
    SALES_DOCUMENT VARCHAR(50),                  -- 에러 발생한 Sales Document 값
    SALES_DOCUMENT_ITEM VARCHAR(50),             -- 에러 발생한 Sales Document Item 값
    ERROR_TYPE VARCHAR(50),                      -- 에러 유형 (VALIDATION, DB_UPDATE, PARSING 등)
    ERROR_MESSAGE VARCHAR(500),                  -- 상세 에러 메시지
    FOREIGN KEY (PROCESS_ID) REFERENCES EXCEL_PROCESS_LOG(PROCESS_ID)
);

-- =====================================================
-- 인덱스 생성 (성능 최적화)
-- =====================================================

-- 1. 처리 상태별 조회 최적화
CREATE INDEX IX_EXCEL_PROCESS_LOG_STATUS ON EXCEL_PROCESS_LOG(STATUS);

-- 2. 사용자별 처리 이력 조회 최적화
CREATE INDEX IX_EXCEL_PROCESS_LOG_USER_TIME ON EXCEL_PROCESS_LOG(USER_ID, START_TIME DESC);

-- 3. 에러 조회 최적화
CREATE INDEX IX_EXCEL_PROCESS_ERRORS_PROCESS_ID ON EXCEL_PROCESS_ERRORS(PROCESS_ID);

-- 4. 에러 타입별 집계 최적화
CREATE INDEX IX_EXCEL_PROCESS_ERRORS_TYPE ON EXCEL_PROCESS_ERRORS(ERROR_TYPE);

-- =====================================================
-- 테이블 코멘트 추가 (MS SQL Server 확장 속성)
-- =====================================================

-- 테이블 설명 추가
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'엑셀 파일 업로드 및 처리 과정의 전체 상태를 관리하는 테이블', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'EXCEL_PROCESS_LOG';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'엑셀 처리 과정에서 발생한 개별 에러 정보를 저장하는 테이블', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'EXCEL_PROCESS_ERRORS';

-- 컬럼 설명 추가
EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'처리 과정 고유 식별자 (UUID)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'EXCEL_PROCESS_LOG', 
    @level2type = N'COLUMN', @level2name = N'PROCESS_ID';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'처리 상태 (PROCESSING: 처리중, COMPLETED: 완료, FAILED: 실패)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'EXCEL_PROCESS_LOG', 
    @level2type = N'COLUMN', @level2name = N'STATUS';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', @value = N'에러 유형 (VALIDATION: 유효성검사, DB_UPDATE: DB업데이트, PARSING: 파싱에러)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'EXCEL_PROCESS_ERRORS', 
    @level2type = N'COLUMN', @level2name = N'ERROR_TYPE';

-- =====================================================
-- 샘플 데이터 (테스트용)
-- =====================================================

-- 처리 완료된 샘플 로그
INSERT INTO EXCEL_PROCESS_LOG (
    PROCESS_ID, USER_ID, FILE_NAME, TOTAL_ROWS, PROCESSED_ROWS, 
    STATUS, START_TIME, END_TIME, ERROR_MESSAGE
) VALUES (
    'sample-process-001', 'admin', 'sales_data_202507.xlsx', 
    15000, 15000, 'COMPLETED', 
    '2025-07-16 09:00:00', '2025-07-16 09:15:30', NULL
);

-- 에러가 발생한 샘플 로그
INSERT INTO EXCEL_PROCESS_LOG (
    PROCESS_ID, USER_ID, FILE_NAME, TOTAL_ROWS, PROCESSED_ROWS, 
    STATUS, START_TIME, END_TIME, ERROR_MESSAGE
) VALUES (
    'sample-process-002', 'user001', 'order_update_202507.xlsx', 
    8500, 8450, 'COMPLETED', 
    '2025-07-16 10:00:00', '2025-07-16 10:12:45', NULL
);

-- 샘플 에러 데이터
INSERT INTO EXCEL_PROCESS_ERRORS (
    PROCESS_ID, ROW_NUMBER, SALES_DOCUMENT, SALES_DOCUMENT_ITEM, 
    ERROR_TYPE, ERROR_MESSAGE
) VALUES 
('sample-process-002', 1205, 'SO-2025-001', '10', 'VALIDATION', 'Sales Document Item 값이 숫자가 아님'),
('sample-process-002', 3450, 'SO-2025-002', '20', 'DB_UPDATE', 'ORDERNO를 찾을 수 없음'),
('sample-process-002', 5678, 'SO-2025-003', '30', 'DB_UPDATE', 'LINE_NO 매칭 실패'),
('sample-process-002', 7890, '', '40', 'VALIDATION', 'Sales Document 값이 비어있음'),
('sample-process-002', 8100, 'SO-2025-004', 'ABC', 'VALIDATION', 'Sales Document Item은 숫자여야 함');

-- =====================================================
-- 데이터 정리 프로시저 (선택사항)
-- =====================================================

-- 30일 이상 된 완료된 로그 삭제 프로시저
CREATE PROCEDURE sp_cleanup_excel_logs
AS
BEGIN
    -- 30일 이상 된 완료된 처리 로그와 관련 에러 삭제
    DELETE FROM EXCEL_PROCESS_ERRORS 
    WHERE PROCESS_ID IN (
        SELECT PROCESS_ID 
        FROM EXCEL_PROCESS_LOG 
        WHERE STATUS = 'COMPLETED' 
        AND END_TIME < DATEADD(DAY, -30, GETDATE())
    );
    
    DELETE FROM EXCEL_PROCESS_LOG 
    WHERE STATUS = 'COMPLETED' 
    AND END_TIME < DATEADD(DAY, -30, GETDATE());
    
    PRINT '30일 이상 된 처리 로그가 정리되었습니다.';
END;

-- =====================================================
-- 통계 조회 뷰 (선택사항)
-- =====================================================

-- 처리 통계 뷰
CREATE VIEW VW_EXCEL_PROCESS_STATISTICS AS
SELECT 
    USER_ID,
    COUNT(*) AS TOTAL_PROCESSES,
    SUM(CASE WHEN STATUS = 'COMPLETED' THEN 1 ELSE 0 END) AS COMPLETED_PROCESSES,
    SUM(CASE WHEN STATUS = 'FAILED' THEN 1 ELSE 0 END) AS FAILED_PROCESSES,
    SUM(TOTAL_ROWS) AS TOTAL_ROWS_PROCESSED,
    AVG(DATEDIFF(SECOND, START_TIME, END_TIME)) AS AVG_PROCESSING_TIME_SEC
FROM EXCEL_PROCESS_LOG
WHERE START_TIME >= DATEADD(DAY, -30, GETDATE())
GROUP BY USER_ID;

-- 에러 통계 뷰
CREATE VIEW VW_EXCEL_ERROR_STATISTICS AS
SELECT 
    ERROR_TYPE,
    COUNT(*) AS ERROR_COUNT,
    COUNT(DISTINCT PROCESS_ID) AS AFFECTED_PROCESSES
FROM EXCEL_PROCESS_ERRORS e
INNER JOIN EXCEL_PROCESS_LOG l ON e.PROCESS_ID = l.PROCESS_ID
WHERE l.START_TIME >= DATEADD(DAY, -30, GETDATE())
GROUP BY ERROR_TYPE;

-- =====================================================
-- 사용 방법 안내
-- =====================================================

/*
1. 테이블 생성 순서:
   - 먼저 EXCEL_PROCESS_LOG 테이블 생성
   - 그 다음 EXCEL_PROCESS_ERRORS 테이블 생성 (외래키 참조)

2. 주요 사용 패턴:
   - 엑셀 업로드 시작: EXCEL_PROCESS_LOG에 PROCESSING 상태로 레코드 생성
   - 진행률 업데이트: PROCESSED_ROWS 컬럼 업데이트
   - 에러 발생: EXCEL_PROCESS_ERRORS에 에러 정보 저장
   - 처리 완료: STATUS를 COMPLETED로 변경, END_TIME 설정

3. 모니터링 쿼리:
   - 현재 처리 중인 작업: SELECT * FROM EXCEL_PROCESS_LOG WHERE STATUS = 'PROCESSING'
   - 최근 에러 현황: SELECT * FROM VW_EXCEL_ERROR_STATISTICS
   - 사용자별 처리 통계: SELECT * FROM VW_EXCEL_PROCESS_STATISTICS

4. 정리 작업:
   - 정기적으로 sp_cleanup_excel_logs 프로시저 실행
   - 또는 스케줄러를 통해 자동 실행 설정
*/





테이블 생성 쿼리에 대한 상세한 코멘트를 포함한 완전한 스크립트를 작성했습니다. 

**주요 특징:**

1. **상세한 컬럼 설명**: 각 컬럼의 목적과 사용 방법을 명확히 설명
2. **성능 최적화**: 자주 사용되는 조회 패턴에 맞는 인덱스 추가
3. **MS SQL Server 확장 속성**: 테이블과 컬럼에 대한 메타데이터 추가
4. **샘플 데이터**: 테스트와 이해를 위한 예시 데이터 제공
5. **유지보수 도구**: 정리 프로시저와 통계 뷰 추가
6. **사용 가이드**: 실제 운영 시 참고할 수 있는 사용 방법 안내

**테이블 설계 포인트:**

- **EXCEL_PROCESS_LOG**: 전체 처리 과정의 상태 관리
- **EXCEL_PROCESS_ERRORS**: 개별 행 단위 에러 정보 저장
- **외래키 관계**: 데이터 무결성 보장
- **자동 정리**: 오래된 로그 자동 삭제 기능

이 스크립트를 사용하면 엑셀 업로드 시스템의 모든 처리 과정을 효과적으로 추적하고 관리할 수 있습니다.