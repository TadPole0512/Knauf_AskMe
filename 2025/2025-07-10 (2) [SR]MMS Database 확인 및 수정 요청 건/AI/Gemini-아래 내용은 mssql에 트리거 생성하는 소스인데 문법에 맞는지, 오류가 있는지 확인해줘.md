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

결론부터 말하면, 그 코드는 MySQL이나 MariaDB에서 사용하는 문법이야. MSSQL에서는 바로 쓸 수 없어. 문법이 꽤 다르거든.

내가 MSSQL 문법에 맞게 고치고, 주석도 달아서 새로 만들어 봤어. 아래 코드를 한번 봐봐!

MSSQL 용 트리거 소스 (수정 및 주석 추가)
이 코드는 MMS_MSG 테이블에 데이터가 들어오기 전에, 현재 월과 이전 월의 로그 테이블을 확인해서 중복된 내용이면 status 값을 3으로 바꿔서 저장해 주는 역할을 해.


```sql

-- =============================================
-- 설명: MMS_MSG 테이블에 INSERT 시 중복 메시지를 확인하는 트리거
-- 동작:
-- 1. INSERT될 데이터와 동일한 내용(phone, msg)이면서
--    전송 성공(status = 3)한 기록이 있는지 확인합니다.
-- 2. 확인 대상은 현재 월과 이전 월의 로그 테이블('MMS_LOG_YYYYMM') 입니다.
-- 3. 중복이 발견되면 새로 INSERT될 데이터의 status를 3으로 변경하고,
--    중복이 아니면 원래 status 값 그대로 INSERT 합니다.
-- =============================================
CREATE TRIGGER trg_handle_duplicate_mms
ON MMS_MSG       -- 이 트리거가 적용될 테이블
INSTEAD OF INSERT  -- INSERT 작업을 가로채서 아래에 정의된 작업으로 대체함
AS
BEGIN
    -- SET NOCOUNT ON은 불필요한 메시지(예: '(1개 행 적용됨)')가
    -- 클라이언트로 전송되는 것을 막아서 성능을 약간 향상시켜 줘.
    SET NOCOUNT ON;

    -- 변수 선언
    DECLARE @is_duplicate BIT = 0; -- 중복 여부를 저장할 변수 (0: 중복 아님, 1: 중복)
    DECLARE @current_archive_table NVARCHAR(255); -- 현재 월 로그 테이블 이름
    DECLARE @previous_archive_table NVARCHAR(255); -- 이전 월 로그 테이블 이름
    DECLARE @sql_check NVARCHAR(MAX); -- 동적 쿼리를 저장할 변수
    DECLARE @params NVARCHAR(MAX); -- 동적 쿼리의 파라미터를 정의할 변수

    -- INSERT 하려는 데이터 가져오기
    -- MSSQL 트리거는 여러 행이 한 번에 INSERT 될 수 있지만,
    -- 원본 코드의 로직을 따라 한 행씩 처리한다고 가정하고 변수에 담을게.
    -- 만약 여러 행을 한 번에 처리해야 한다면 로직이 더 복잡해져. (예: CURSOR 사용)
    DECLARE @p_phone NVARCHAR(50);
    DECLARE @p_msg NVARCHAR(4000);
    DECLARE @p_status INT;

    SELECT
        @p_phone = phone,
        @p_msg = msg,
        @p_status = status
    FROM inserted; -- INSERT 하려는 데이터는 'inserted' 임시 테이블에 들어있어.

    -- 1. 현재 월과 이전 월의 보관 테이블 이름 생성
    SET @current_archive_table = 'MMS_LOG_' + FORMAT(GETDATE(), 'yyyyMM');
    SET @previous_archive_table = 'MMS_LOG_' + FORMAT(DATEADD(MONTH, -1, GETDATE()), 'yyyyMM');

    -- 2. 현재 월 보관 테이블에서 중복 검사
    -- (테이블이 존재할 경우에만 동적 쿼리 실행)
    IF OBJECT_ID(@current_archive_table, 'U') IS NOT NULL
    BEGIN
        SET @sql_check = N'SELECT @found = 1 FROM ' + QUOTENAME(@current_archive_table) +
                         N' WHERE phone = @phone AND msg = @msg AND status = 3';
        SET @params = N'@phone NVARCHAR(50), @msg NVARCHAR(4000), @found BIT OUTPUT';

        EXEC sp_executesql @sql_check, @params, @phone = @p_phone, @msg = @p_msg, @found = @is_duplicate OUTPUT;
    END

    -- 3. 현재 월에 중복이 없었다면, 이전 월 보관 테이블에서 중복 검사
    IF @is_duplicate = 0
    BEGIN
        -- (테이블이 존재할 경우에만 동적 쿼리 실행)
        IF OBJECT_ID(@previous_archive_table, 'U') IS NOT NULL
        BEGIN
            SET @sql_check = N'SELECT @found = 1 FROM ' + QUOTENAME(@previous_archive_table) +
                             N' WHERE phone = @phone AND msg = @msg AND status = 3';
            SET @params = N'@phone NVARCHAR(50), @msg NVARCHAR(4000), @found BIT OUTPUT';

            EXEC sp_executesql @sql_check, @params, @phone = @p_phone, @msg = @p_msg, @found = @is_duplicate OUTPUT;
        END
    END

    -- 4. 최종적으로 데이터 INSERT
    -- 중복이 발견되었다면 status를 3으로 변경하고, 아니면 원래 status 값으로 INSERT
    IF @is_duplicate = 1
    BEGIN
        -- 중복이므로 status를 3으로 변경해서 INSERT
        INSERT INTO MMS_MSG (phone, msg, status /*, 다른 컬럼들... */)
        SELECT phone, msg, 3 /*, 다른 컬럼들... */ FROM inserted;
    END
    ELSE
    BEGIN
        -- 중복이 아니므로 원래 값 그대로 INSERT
        INSERT INTO MMS_MSG (phone, msg, status /*, 다른 컬럼들... */)
        SELECT phone, msg, status /*, 다른 컬럼들... */ FROM inserted;
    END
END
GO
```


원래 코드와 뭐가 다른지 정리해 줄게!

트리거 실행 시점 변경:
  - MySQL: BEFORE INSERT (데이터가 들어가기 직전에 실행, NEW 키워드로 새 데이터 조작)
  - MSSQL: INSTEAD OF INSERT (INSERT 명령 자체를 가로채서, 트리거 안에 있는 코드로 대체함)

새로운 데이터 접근 방식:
  - MySQL: NEW.컬럼명으로 앞으로 들어올 데이터에 접근했어.
  - MSSQL: inserted라는 임시 테이블에 들어올 데이터가 담겨. 그래서 SELECT 컬럼명 FROM inserted 방식으로 접근해야 해.

날짜 함수 및 문자열 합치기:
  - MySQL: DATE_FORMAT, NOW(), CONCAT()
  - MSSQL: FORMAT, GETDATE(), + 연산자

테이블 존재 여부 확인:
  - MySQL: information_schema.tables를 조회했어.
  - MSSQL: OBJECT_ID('테이블명', 'U') IS NOT NULL 구문이 더 일반적이고 빨라.

동적 쿼리 실행:
  - MySQL: PREPARE, EXECUTE, DEALLOCATE를 순서대로 사용했어.
  - MSSQL: 시스템 저장 프로시저인 sp_executesql을 사용해. 이게 더 안전하고 효율적이야.

궁금한 점이 있으면 또 물어봐!