네\~!
지금까지의 **세션 전체 대화 내용**을 마크다운(.md) 형식으로 정리해서 제공해줄게.
아래 내용을 복사해서 `.md` 파일로 저장하면 되고, 필요하면 PDF로 변환해서도 사용할 수 있어!

---

````markdown
# ChatGPT 세션 대화 내용 (2025-07-24)

---

## 1. 요청 내용

> 아래의 내용을 단계별 작업으로 정리하여 작업의 흐름(프로세스)을 알 수 있게 정리해줘.

현재 고객에게 오더접수/보류오더 MMS전송을 E-Order와 같은 DB에서 활용하고 있습니다  
이 과정에서 SAP에서 알 수 없는 이유로 MMS DB에 여러 번의 문자 전송 데이터를 생성하고 있습니다  
SAP에 원인 검증 및 데이터 수정 요청을 해놓은 상태이나, 이것을 임시적으로 해결하기 위하여 중복 검증 옵션을 추가하려 합니다

먼저 MMS의 동작 Table은 하기와 같습니다  
1. SAP에서 오더 확정  
2. MMS_MSG Table에 오더 및 정보 전송  
3. 전송 완료 되면 STATUS 3으로 변경 한 뒤 MMS_LOG_YYYYMM Table로 이동

위의 과정에서 MMS_MSG Table의 새로운 Data가 생성될 때 Create Trigger를 생성 할 수 있는지 검토 요청 드립니다  
MMS_MSG Table에 새로운 데이터가 생성 되었을 때, MMS_MSG Table의 phone, msg 컬럼이 동일한 게 있다면, 이 경우에는 status값이 3으로 바뀌어서 저장해야 합니다.  
조건은 MMS_MSG Table에 새로운 데이터가 생성 되었을 때, MMS_LOG_YYYYMM Table의 phone, msg 컬럼이 동일한게 있다면 STATUS 3로 바로 변경 처리 입니다.  
(이 과정에서 MMS_LOG_YYYYMM Table은 이전 달의 테이블도 같이 체크 되어야 합니다 *6월 30일 오더의 경우 7월 1일이 되었을 때 반복 전송 될 수도 있음)

---

## 2. 1차 답변

### 전체 MMS 발송 프로세스 요약

#### 📌 현재 구조

1. SAP에서 오더 확정
2. ➜ MMS_MSG 테이블에 전송 정보 저장
3. ➜ 전송 후 STATUS 값을 3으로 변경
4. ➜ MMS_LOG_YYYYMM 테이블로 이동 (이관)

---

#### 문제 발생 흐름

| 구분   | 설명                                                          |
| ------ | ------------------------------------------------------------- |
| ❌ 문제  | SAP에서 알 수 없는 이유로 같은 MMS가 여러 번 생성됨            |
| 🎯 목표  | 중복 전송 방지를 위한 트리거(Trigger) 로직 추가               |
| 🔄 임시대응 | SAP 쪽 수정 전까지 DB 레벨에서 중복 차단 로직 선처리          |

---

#### 해결을 위한 작업 흐름 정리 (트리거 설계 기준)

| 단계   | 설명                                                                        |
| ------ | --------------------------------------------------------------------------- |
| 1단계  | SAP가 새로운 MMS 전송 정보를 MMS_MSG 테이블에 INSERT 시도                   |
| 2단계  | INSERT 전, 트리거(BEFORE INSERT) 작동                                       |
| 3단계  | 해당 레코드의 PHONE, MSG 값을 기준으로 중복 여부 확인                       |
| 4단계  | MMS_LOG_현재월, MMS_LOG_이전월 테이블에서 중복 존재 여부 체크               |
| 5단계  | 중복 발견 시 → STATUS 값을 3으로 설정하여 INSERT 처리, 없으면 그대로 진행   |
| 6단계  | 전송 완료 처리 로직은 기존과 동일하게 동작함                                |

---

#### 정리된 핵심 요건

| 항목           | 설명                                                    |
| -------------- | ------------------------------------------------------- |
| 트리거 위치    | MMS_MSG 테이블에 BEFORE INSERT 트리거                   |
| 중복 판단 기준 | PHONE, MSG 값이 동일한 경우                             |
| 확인 테이블    | MMS_LOG_현재월, MMS_LOG_이전월                          |
| 중복 처리 방식 | STATUS = 3 으로 설정 후 INSERT                          |
| 기타           | MMS_LOG_YYYYMM 테이블은 매월 달라지므로 동적 테이블명 필요 |

---

필요하면 MSSQL용 트리거 예시 코드도 만들어줄 수 있다고 안내함.

---

## 3. 추가 요청 및 수정

> 확인 테이블은 셋이야. MMS_MSG 테이블을 넣어줘.

### 반영 후 작업 흐름

| 단계   | 설명                                                                                              |
| ------ | ------------------------------------------------------------------------------------------------- |
| 1단계  | SAP가 새로운 MMS 전송 정보를 MMS_MSG 테이블에 INSERT 시도                                         |
| 2단계  | INSERT 전, 트리거(BEFORE INSERT) 작동                                                             |
| 3단계  | 해당 레코드의 PHONE, MSG 값을 기준으로 **중복 여부 확인**                                          |
| 4단계  | MMS_MSG, MMS_LOG_현재월, MMS_LOG_이전월 세 테이블에서 중복 존재 여부 확인                         |
| 5단계  | 중복 존재 시 STATUS = 3 으로 설정 후 INSERT, 중복 없으면 원래대로 INSERT 진행                     |
| 6단계  | 이후 전송 완료 시 기존 로직대로 MMS_LOG_YYYYMM 으로 이관                                          |

### 핵심 요건

- 트리거 위치: MMS_MSG 테이블 (BEFORE INSERT)
- 중복 판단 기준: PHONE, MSG 컬럼 값이 동일
- 확인 대상 테이블: MMS_MSG, MMS_LOG_현재월, MMS_LOG_이전월
- 중복 처리 방식: STATUS = 3 으로 설정 후 INSERT
- 기타: MMS_LOG_YYYYMM 은 매월 생성(동적 테이블명 필요)

---

## 4. MSSQL 트리거 소스 (주석 포함)

```sql
-- =======================================
-- MMS_MSG 테이블 중복 검증용 트리거
-- =======================================
-- 목적: 동일한 PHONE, MSG 값이 MMS_MSG, MMS_LOG_현재월, MMS_LOG_이전월 중
--      어디든 이미 있으면, STATUS 값을 3으로 자동 변경
-- =======================================

CREATE TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- [1] 변수 선언
    DECLARE @PHONE    VARCHAR(15);
    DECLARE @MSG      VARCHAR(4000);
    DECLARE @MSGKEY   NUMERIC(11,0);

    -- [2] 새로 삽입된 데이터에서 값 꺼내오기 (여러건 처리 필요시 커서 or JOIN 처리)
    DECLARE cur CURSOR FOR
        SELECT MSGKEY, PHONE, MSG
        FROM inserted;

    OPEN cur;
    FETCH NEXT FROM cur INTO @MSGKEY, @PHONE, @MSG;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        -- [3] 현재월/이전월 구하기 (YYYYMM 포맷)
        DECLARE @CURR_YM VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112);
        DECLARE @PREV_YM VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112);

        -- [4] 동적 테이블명 조합
        DECLARE @CURR_LOG_TBL NVARCHAR(64) = N'MMS_LOG_' + @CURR_YM;
        DECLARE @PREV_LOG_TBL NVARCHAR(64) = N'MMS_LOG_' + @PREV_YM;

        -- [5] 중복 존재 여부 확인
        DECLARE @DUP_CNT INT = 0;
        DECLARE @SQL NVARCHAR(MAX);

        -- MMS_MSG(자기 자신 테이블)
        IF EXISTS (SELECT 1 FROM MMS_MSG WHERE PHONE = @PHONE AND MSG = @MSG AND MSGKEY <> @MSGKEY)
            SET @DUP_CNT = 1;

        -- MMS_LOG_현재월 (동적 쿼리)
        IF @DUP_CNT = 0 AND EXISTS (
            SELECT 1 FROM sys.tables WHERE name = @CURR_LOG_TBL
        )
        BEGIN
            SET @SQL = N'SELECT TOP 1 1 FROM ' + @CURR_LOG_TBL + 
                       ' WHERE PHONE = @PHONE AND MSG = @MSG';
            IF EXISTS (
                EXEC sp_executesql @SQL, N'@PHONE VARCHAR(15), @MSG VARCHAR(4000)', @PHONE=@PHONE, @MSG=@MSG
            )
                SET @DUP_CNT = 1;
        END

        -- MMS_LOG_이전월 (동적 쿼리)
        IF @DUP_CNT = 0 AND EXISTS (
            SELECT 1 FROM sys.tables WHERE name = @PREV_LOG_TBL
        )
        BEGIN
            SET @SQL = N'SELECT TOP 1 1 FROM ' + @PREV_LOG_TBL +
                       ' WHERE PHONE = @PHONE AND MSG = @MSG';
            IF EXISTS (
                EXEC sp_executesql @SQL, N'@PHONE VARCHAR(15), @MSG VARCHAR(4000)', @PHONE=@PHONE, @MSG=@MSG
            )
                SET @DUP_CNT = 1;
        END

        -- [6] 중복이면 STATUS를 3으로 업데이트
        IF @DUP_CNT = 1
        BEGIN
            UPDATE MMS_MSG
            SET STATUS = '3'
            WHERE MSGKEY = @MSGKEY;
        END

        FETCH NEXT FROM cur INTO @MSGKEY, @PHONE, @MSG;
    END

    CLOSE cur;
    DEALLOCATE cur;
END
GO
````

---

### 주요 설명

* inserted: 트리거 실행 시, 새로 INSERT된 레코드 집합(1건 이상 가능)
* CURSOR: 여러 건 INSERT 시에도 모두 처리
* 동적 테이블명: 월별 로그테이블(MMS\_LOG\_YYYYMM)을 동적으로 지정
* EXISTS + sp\_executesql: 로그테이블이 실제 존재할 때만, 해당 테이블 내 중복 검색
* STATUS = '3': 중복 발견 시 상태값을 3(발송완료/중복처리 등)으로 변경
* MSGKEY <> @MSGKEY: 자기 자신과는 비교하지 않음

---

궁금한 점, 혹은 소스에 추가·수정할 내용 있으면 언제든 얘기해달라고 안내함.
실제 적용 전에는 **테스트 환경에서 꼭 검증** 권장.

```

---

필요하면 이 파일을 복사해서 `.md` 파일로 저장하고,  
**PDF 변환**이 필요하면 PDF로 바로 변환도 가능해!

원하는 파일 형태(마크다운, PDF)나 추가할 내용 있으면 말해줘!  
파일이 실제로 필요하면 "파일로 다운로드" 요청해줘~
```
