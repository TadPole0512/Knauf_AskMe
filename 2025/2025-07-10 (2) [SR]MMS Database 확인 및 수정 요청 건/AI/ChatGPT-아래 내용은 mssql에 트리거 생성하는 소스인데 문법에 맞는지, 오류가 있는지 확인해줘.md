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



아래 소스는 **MySQL** 문법으로 작성되어 있고,
MSSQL(=SQL Server)에서는 **문법이 완전히 다르기 때문에** 바로 사용할 수 없습니다.

---

## 🔎 **오류 및 주요 문제점**

1. **DELIMITER**, `BEFORE INSERT ON`, `FOR EACH ROW`, `NEW.status = 3` 등은 MySQL 전용 문법입니다.
2. **MSSQL에서는** 트리거 내에서 변수 선언, 행 단위 작업, 그리고 동적 테이블명 사용 방식이 다릅니다.
3. **동적 테이블명**으로 SELECT하는 부분(MySQL의 PREPARE/EXECUTE)은 MSSQL에서는 `EXEC` 또는 `sp_executesql`로 처리해야 하며,
   트리거 내에서 이런 동적 쿼리 실행은 권장되지 않고, 구현도 까다롭습니다.
4. **MSSQL 트리거에서는** `inserted`/`deleted`라는 가상 테이블을 이용합니다.
   (행별 처리하려면 CURSOR를 써야 하며, 성능 저하에 유의해야 함)

---

## ✅ **MSSQL 트리거 형태로 재작성**

아래는 주석을 추가하여 **MSSQL에서 동작 가능한 형태로 최대한 변환**한 예시입니다.
**단, "월별로 테이블명 동적으로 지정"은 트리거에서 직접 지원이 거의 불가능**하므로,
여기서는 "이번달/지난달 테이블이 존재한다고 가정"하고,
테이블명을 직접 명시해서 구현 예시를 보여드립니다.
**동적 테이블명 처리(=월이 바뀌면 트리거도 재생성 또는 동적으로 처리할 외부 로직 필요!)**

---

```sql
-- ========================================
-- MMS_MSG 테이블에 INSERT가 발생할 때,
-- MMS_LOG_202407, MMS_LOG_202406 두 테이블에서
-- 중복(휴대폰, 메시지, status=3) 검사 후
-- 중복이면 status를 3으로 강제 설정
-- (MSSQL 버전, 동적 테이블명 불가, 월별 테이블 직접 명시)
-- ========================================

CREATE TRIGGER trg_handle_duplicate_mms
ON MMS_MSG
INSTEAD OF INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- inserted: 새로 추가될 데이터들이 담긴 테이블
    DECLARE @phone VARCHAR(20), @msg NVARCHAR(4000), @status INT;

    -- 삽입된 데이터 반복 처리(다중 행 처리, CURSOR 이용)
    DECLARE insert_cursor CURSOR FOR
        SELECT phone, msg, status
        FROM inserted;

    OPEN insert_cursor;
    FETCH NEXT FROM insert_cursor INTO @phone, @msg, @status;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        DECLARE @is_duplicate INT = 0;

        -- 1. 이번달 테이블(예: MMS_LOG_202407)에서 중복 검사
        IF EXISTS (
            SELECT 1
            FROM MMS_LOG_202407
            WHERE phone = @phone AND msg = @msg AND status = 3
        )
            SET @is_duplicate = 1;

        -- 2. 이번달에 없으면 지난달 테이블(예: MMS_LOG_202406)에서 중복 검사
        IF @is_duplicate = 0 AND EXISTS (
            SELECT 1
            FROM MMS_LOG_202406
            WHERE phone = @phone AND msg = @msg AND status = 3
        )
            SET @is_duplicate = 1;

        -- 3. 중복이면 status를 3으로 변경해서 INSERT
        IF @is_duplicate = 1
        BEGIN
            INSERT INTO MMS_MSG (phone, msg, status)
            VALUES (@phone, @msg, 3);
        END
        ELSE
        BEGIN
            INSERT INTO MMS_MSG (phone, msg, status)
            VALUES (@phone, @msg, @status);
        END

        FETCH NEXT FROM insert_cursor INTO @phone, @msg, @status;
    END

    CLOSE insert_cursor;
    DEALLOCATE insert_cursor;
END
GO
```

---

### ⚠️ **참고/주의사항**

* **월이 바뀌면 테이블명(MMS\_LOG\_202407 등)을 트리거 소스에서 직접 수정해야 함**
  (진짜 운영 환경에서는 동적 SQL이 필요하지만, 트리거에서 매우 비효율적이거나 불가능에 가까움)
* **CURSOR 사용은 성능 저하의 원인이 될 수 있으므로, 실제 데이터량이 많다면 다른 방식 권장**
* **실제로는 중복 체크를 뷰로 만들거나, 애플리케이션/프로시저에서 처리하는 것이 훨씬 좋음**
* **트리거 INSTEAD OF INSERT는 입력 값을 트리거 내부에서 직접 INSERT 해줘야 함**
  (AFTER INSERT 트리거는 inserted 값만 수정 불가)

---

필요하면,

* 동적 쿼리로 테이블명 바꾸는 샘플
* 혹은 트리거 대신 SP(저장 프로시저)로 구현
* 아니면 뷰/통합 테이블 방식 설계
  도 설명해줄 수 있으니, 궁금한 점 말해줘!
