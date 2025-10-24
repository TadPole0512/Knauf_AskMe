아래 내용은 mssql에 트리거 생성하는 소스인데 문법에 맞는지, 오류가 있는지 확인해줘.
그리고 MSSQL 버전으로 주석을 추가하여 재작성해줘
작성할 때 다음과 같은 사항을 반영해줘.
- 친구처럼 편하게 말히기.
- 모든 설명은 반드시 한글을 우선시 하기
- 복잡한 요청은 단계별로 간략하게 정리해서 질문하기
- 모르면 모른다고 말하기
- 모든 답변은 가독성있게 정리하기


```sql

DELIMITER $$
 
CREATE TRIGGER trg_handle_duplicate_mms
BEFORE INSERT ON MMS_MSG
FOR EACH ROW
BEGIN
    -- 변수 선언
    DECLARE v_is_duplicate INT DEFAULT 0;
    DECLARE v_current_archive_table VARCHAR(255);
    DECLARE v_previous_archive_table VARCHAR(255);
 
    -- 1. 현재 월과 이전 월의 보관 테이블 이름 생성
    SET v_current_archive_table = CONCAT('MMS_LOG_', DATE_FORMAT(NOW(), '%Y%m'));
    SET v_previous_archive_table = CONCAT('MMS_LOG_', DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y%m'));
 
    -- 2. 현재 월 보관 테이블에서 중복 검사
    -- (테이블이 존재할 경우에만 동적 쿼리 실행)
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = v_current_archive_table) THEN
        SET @sql_current = CONCAT('SELECT EXISTS(SELECT 1 FROM `', v_current_archive_table, '` WHERE phone = ? AND msg = ? AND status = 3) INTO @found');
        PREPARE stmt_current FROM @sql_current;
        SET @p_phone = NEW.phone;
        SET @p_msg = NEW.msg;
        EXECUTE stmt_current USING @p_phone, @p_msg;
        DEALLOCATE PREPARE stmt_current;
 
        IF @found > 0 THEN
            SET v_is_duplicate = 1;
        END IF;
    END IF;
 
    -- 3. 현재 월에 중복이 없었다면, 이전 월 보관 테이블에서 중복 검사
    IF v_is_duplicate = 0 THEN
        -- (테이블이 존재할 경우에만 동적 쿼리 실행)
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = v_previous_archive_table) THEN
            SET @sql_previous = CONCAT('SELECT EXISTS(SELECT 1 FROM `', v_previous_archive_table, '` WHERE phone = ? AND msg = ? AND status = 3) INTO @found');
            PREPARE stmt_previous FROM @sql_previous;
            EXECUTE stmt_previous USING @p_phone, @p_msg;
            DEALLOCATE PREPARE stmt_previous;
 
            IF @found > 0 THEN
                SET v_is_duplicate = 1;
            END IF;
        END IF;
    END IF;
 
    -- 4. 중복이 발견되었다면, 새로 INSERT될 데이터의 status를 3으로 변경
    IF v_is_duplicate = 1 THEN
        SET NEW.status = 3;
    END IF;
    -- 중복이 아니면 NEW.status는 원래 값(예: 2) 그대로 INSERT 됩니다.
END$$
 
DELIMITER ;
```
----

안녕! 이 소스 살펴봤는데, **MySQL 문법으로 작성되어 있어서 MSSQL에서는 실행이 안 될 거야**. 

주요 문제점들을 정리해보면:

## 🚨 문법 오류들
- `DELIMITER $$` → MSSQL에서는 사용 안 함
- `FOR EACH ROW` → MSSQL은 행 단위 트리거 없음  
- `DECLARE ... DEFAULT` → MSSQL 문법 다름
- `CONCAT()`, `DATE_FORMAT()` → MSSQL 함수명 다름
- `information_schema.tables` 조회 방식 → MSSQL 방식 다름

## 📝 MSSQL 버전으로 다시 작성해줄게!


```sql

-- MMS 메시지 중복 처리 트리거 (MSSQL 버전)
-- 기능: MMS_MSG 테이블에 데이터 삽입 시 중복 메시지 검사 후 상태값 변경
CREATE TRIGGER trg_handle_duplicate_mms
ON MMS_MSG
INSTEAD OF INSERT  -- MSSQL에서는 INSTEAD OF 사용 (BEFORE INSERT 대신)
AS
BEGIN
    -- 에러 처리 설정
    SET NOCOUNT ON;
    
    -- 변수 선언 (MSSQL 방식)
    DECLARE @v_is_duplicate INT = 0;
    DECLARE @v_current_archive_table NVARCHAR(255);
    DECLARE @v_previous_archive_table NVARCHAR(255);
    DECLARE @sql NVARCHAR(MAX);
    DECLARE @found INT = 0;
    DECLARE @params NVARCHAR(500);
    
    -- 현재 처리할 데이터를 임시로 저장할 변수들
    DECLARE @phone NVARCHAR(50);
    DECLARE @msg NVARCHAR(MAX);
    DECLARE @original_status INT;
    
    -- 삽입된 데이터에서 값 가져오기 (MSSQL에서는 inserted 테이블 사용)
    SELECT 
        @phone = phone,
        @msg = msg,
        @original_status = status
    FROM inserted;
    
    -- 1. 현재 월과 이전 월의 보관 테이블 이름 생성
    SET @v_current_archive_table = 'MMS_LOG_' + FORMAT(GETDATE(), 'yyyyMM');
    SET @v_previous_archive_table = 'MMS_LOG_' + FORMAT(DATEADD(MONTH, -1, GETDATE()), 'yyyyMM');
    
    -- 2. 현재 월 보관 테이블에서 중복 검사
    -- 테이블 존재 여부 확인 후 실행
    IF OBJECT_ID(@v_current_archive_table, 'U') IS NOT NULL
    BEGIN
        -- 동적 쿼리로 중복 검사 실행
        SET @sql = N'SELECT @found_out = COUNT(*) FROM ' + QUOTENAME(@v_current_archive_table) + 
                   N' WHERE phone = @phone_param AND msg = @msg_param AND status = 3';
        SET @params = N'@phone_param NVARCHAR(50), @msg_param NVARCHAR(MAX), @found_out INT OUTPUT';
        
        EXEC sp_executesql @sql, @params, 
             @phone_param = @phone, 
             @msg_param = @msg, 
             @found_out = @found OUTPUT;
        
        -- 중복 발견시 플래그 설정
        IF @found > 0
        BEGIN
            SET @v_is_duplicate = 1;
        END
    END
    
    -- 3. 현재 월에 중복이 없었다면, 이전 월 보관 테이블에서 중복 검사
    IF @v_is_duplicate = 0
    BEGIN
        -- 이전 월 테이블 존재 여부 확인
        IF OBJECT_ID(@v_previous_archive_table, 'U') IS NOT NULL
        BEGIN
            -- 동적 쿼리로 이전 월 테이블에서 중복 검사
            SET @sql = N'SELECT @found_out = COUNT(*) FROM ' + QUOTENAME(@v_previous_archive_table) + 
                       N' WHERE phone = @phone_param AND msg = @msg_param AND status = 3';
            SET @params = N'@phone_param NVARCHAR(50), @msg_param NVARCHAR(MAX), @found_out INT OUTPUT';
            
            EXEC sp_executesql @sql, @params, 
                 @phone_param = @phone, 
                 @msg_param = @msg, 
                 @found_out = @found OUTPUT;
            
            -- 중복 발견시 플래그 설정
            IF @found > 0
            BEGIN
                SET @v_is_duplicate = 1;
            END
        END
    END
    
    -- 4. 최종 데이터 삽입 (중복 여부에 따라 상태값 결정)
    INSERT INTO MMS_MSG (phone, msg, status /* 여기에 다른 컬럼들도 추가 필요 */)
    SELECT 
        phone,
        msg,
        CASE 
            WHEN @v_is_duplicate = 1 THEN 3  -- 중복인 경우 상태를 3으로 변경
            ELSE status                      -- 중복이 아니면 원래 상태값 유지
        END
    FROM inserted;
    
END;
```


## 🔧 주요 변경사항 설명

**1. 트리거 타입 변경**
- `BEFORE INSERT` → `INSTEAD OF INSERT` (MSSQL 방식)

**2. 변수 선언 방식**  
- `DECLARE v_name TYPE DEFAULT value` → `DECLARE @v_name TYPE = value`

**3. 날짜 함수 변경**
- `DATE_FORMAT(NOW(), '%Y%m')` → `FORMAT(GETDATE(), 'yyyyMM')`
- `NOW() - INTERVAL 1 MONTH` → `DATEADD(MONTH, -1, GETDATE())`

**4. 테이블 존재 확인**
- `information_schema.tables` → `OBJECT_ID()` 함수 사용

**5. 동적 쿼리 실행**
- `PREPARE/EXECUTE` → `sp_executesql` 사용

## ⚠️ 주의사항

1. **컬럼 정보 부족**: `INSERT INTO MMS_MSG`에서 실제 테이블의 모든 컬럼을 명시해야 해
2. **인덱스 필요**: `phone`, `msg`, `status` 컬럼에 적절한 인덱스가 있는지 확인해
3. **성능 고려**: 동적 쿼리 2번 실행하니까 대용량 데이터에서는 성능 이슈 있을 수 있어

혹시 실제 테이블 구조나 다른 궁금한 점 있으면 언제든 물어봐! 😊