# MMS 메시지 중복 체크 트리거

## 📋 프로젝트 개요

* **트리거명**: `TRG_MMS_MSG_DUP_CHECK`
* **테스트 서버**: `개발(KR-EOD-DEV)서버`
* **대상 테이블**: `dbo.MMS_MSG`
* **목적**: SMS 메시지 발송 시 동일한 전화번호+오더번호 조합의 중복 발송 방지

---

## 🎯 주요 기능

### 1. 오더번호 자동 추출
- **대상 메시지**: '크나우프석고보드_보류오더 안내', '크나우프석고보드_오더접수'
- **추출 로직**: PATINDEX 기반 범용 파싱 (콜론/공백/개행 무관)
- **추출 패턴**:
  - `오더번호 123456` → `123456`
  - `오더번호: 789012` → `789012`
  - `- 오더번호: 400096386 -` → `400096386`

### 2. 중복 검사 범위
- **MMS_MSG 테이블**: 자기 자신 제외한 기존 메시지
- **현재월 로그**: `MMS_LOG_YYYYMM` (예: MMS_LOG_202508)
- **이전월 로그**: `MMS_LOG_YYYYMM` (예: MMS_LOG_202507)

### 3. 중복 처리 방식
- **중복 발견 시**: STATUS = '3' (중복 상태로 변경)
- **중복 없음**: STATUS = '1' (정상 상태 유지)

---

## 💻 완성된 트리거 소스코드

```sql
-- ===========================================================================================
-- 트리거명 : TRG_MMS_MSG_DUP_CHECK (범용 파싱 + v1.1 포맷)
-- 대상 테이블 : dbo.MMS_MSG
-- 동작 :
--   - INSERT된 MSG에서 '오더번호' 뒤의 숫자 토큰 추출(PATINDEX 기반, 콜론/공백/개행/하이픈 무관)
--   - PHONE + ORDER_NO 기준으로 MMS_MSG(자기 자신 제외), MMS_LOG_현재월/이전월에 존재하면 STATUS='3'
-- 제한 :
--   - 스키마/전송 포맷/인덱스 변경 없음
--   - SUBJECT는 두 유형만 처리
-- ===========================================================================================

-- 기존 트리거 삭제
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'TRG_MMS_MSG_DUP_CHECK')
    DROP TRIGGER dbo.TRG_MMS_MSG_DUP_CHECK
GO

-- 새 트리거 생성
CREATE TRIGGER dbo.TRG_MMS_MSG_DUP_CHECK
ON dbo.MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON

    BEGIN TRY
        /* [1] inserted → #ins (대상 SUBJECT만) */
        IF OBJECT_ID('tempdb..#ins', 'U') IS NOT NULL
        BEGIN
            DROP TABLE #ins
        END

        SELECT
               I.MSGKEY, I.PHONE, I.SUBJECT, I.MSG
          INTO #ins
          FROM inserted AS I
         WHERE I.SUBJECT IN ( N'크나우프석고보드_보류오더 안내' , N'크나우프석고보드_오더접수' )

        IF @@ROWCOUNT = 0
            RETURN

        /* [2] inserted 파싱 → #ins_norm (오더번호 범용 추출) */
        IF OBJECT_ID('tempdb..#ins_norm', 'U') IS NOT NULL
        BEGIN
            DROP TABLE #ins_norm
        END

        SELECT
               X.MSGKEY
             , X.PHONE
             , X.SUBJECT
             , X.MSG
             , CASE
                   WHEN P.pos > 0 AND D.dstart > 0
                        THEN SUBSTRING(
                                         T.tail
                                       , D.dstart
                                       , CASE
                                             WHEN ND.nextNonDigitPos = 0 THEN 50
                                             ELSE ND.nextNonDigitPos - 1
                                         END
                                      )
                   ELSE NULL
               END AS ORDER_NO
          INTO #ins_norm
          FROM #ins AS X
         CROSS APPLY ( SELECT CHARINDEX(N'오더번호', X.MSG) AS pos ) AS P
         CROSS APPLY ( SELECT CASE WHEN P.pos > 0 THEN SUBSTRING(X.MSG, P.pos + LEN(N'오더번호'), 300) ELSE N'' END AS tail ) AS T
         CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
         CROSS APPLY ( SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND

        IF NOT EXISTS ( SELECT 1 FROM #ins_norm WHERE ORDER_NO IS NOT NULL AND LEN(ORDER_NO) > 0 )
            RETURN

        /* [3] 로그 테이블 명 준비(DECLARE 후 SET로 초기화) */
        DECLARE
              @CURR_YM   VARCHAR(6)
            , @PREV_YM   VARCHAR(6)
            , @CURR_RAW  NVARCHAR(128)
            , @PREV_RAW  NVARCHAR(128)
            , @CURR_Q    NVARCHAR(300)
            , @PREV_Q    NVARCHAR(300)
            , @SQL       NVARCHAR(MAX)

        SET @CURR_YM  = CONVERT(VARCHAR(6), GETDATE(), 112)
        SET @PREV_YM  = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)

        SET @CURR_RAW = N'MMS_LOG_' + @CURR_YM
        SET @PREV_RAW = N'MMS_LOG_' + @PREV_YM

        SET @CURR_Q   = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @CURR_YM)
        SET @PREV_Q   = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @PREV_YM)

        /* [4] MMS_MSG 중복 체크 (자기 자신 제외) */
        UPDATE M
           SET STATUS = N'3'
          FROM dbo.MMS_MSG AS M
          JOIN #ins_norm   AS I
            ON M.MSGKEY = I.MSGKEY
         WHERE I.ORDER_NO IS NOT NULL
           AND LEN(I.ORDER_NO) > 0
           AND EXISTS (
                         SELECT
                                1
                           FROM dbo.MMS_MSG AS MM
                          CROSS APPLY ( SELECT CHARINDEX(N'오더번호', MM.MSG) AS pos ) AS P
                          CROSS APPLY ( SELECT CASE WHEN P.pos > 0 THEN SUBSTRING(MM.MSG, P.pos + LEN(N'오더번호'), 300) ELSE N'' END AS tail ) AS T
                          CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
                          CROSS APPLY ( SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND
                          CROSS APPLY (
                                         SELECT CASE
                                                    WHEN P.pos > 0 AND D.dstart > 0
                                                         THEN SUBSTRING(
                                                                          T.tail
                                                                        , D.dstart
                                                                        , CASE
                                                                              WHEN ND.nextNonDigitPos = 0 THEN 50
                                                                              ELSE ND.nextNonDigitPos - 1
                                                                          END
                                                                       )
                                                    ELSE NULL
                                                END AS ORDER_NO
                                      ) AS O
                          WHERE MM.PHONE    = I.PHONE
                            AND MM.MSGKEY  <> I.MSGKEY
                            AND MM.SUBJECT IN ( N'크나우프석고보드_보류오더 안내' , N'크나우프석고보드_오더접수' )
                            AND O.ORDER_NO  = I.ORDER_NO
                      )

        /* [5] 현재월 로그 검사 */
        IF OBJECT_ID(@CURR_RAW, N'U') IS NOT NULL
        BEGIN
            SET @SQL = N'
                          UPDATE M
                             SET STATUS = N''3''
                            FROM dbo.MMS_MSG AS M
                                 JOIN #ins_norm   AS I
                                   ON M.MSGKEY = I.MSGKEY
                           WHERE I.ORDER_NO IS NOT NULL
                             AND LEN(I.ORDER_NO) > 0
                             AND EXISTS (
                                           SELECT 1
                                             FROM ' + @CURR_Q + N' AS L
                                            CROSS APPLY ( SELECT CHARINDEX(N''오더번호'', L.MSG) AS pos ) AS P
                                            CROSS APPLY (
                                                           SELECT CASE
                                                                      WHEN P.pos > 0 THEN SUBSTRING(L.MSG, P.pos + LEN(N''오더번호''), 300)
                                                                      ELSE N''''
                                                                  END AS tail
                                                        ) AS T
                                            CROSS APPLY ( SELECT PATINDEX(N''%[0-9]%'', T.tail) AS dstart ) AS D
                                            CROSS APPLY ( SELECT PATINDEX(N''%[^0-9]%'', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND
                                            CROSS APPLY (
                                                           SELECT CASE
                                                                      WHEN P.pos > 0 AND D.dstart > 0
                                                                           THEN SUBSTRING(
                                                                                    T.tail
                                                                                  , D.dstart
                                                                                  , CASE
                                                                                        WHEN ND.nextNonDigitPos = 0 THEN 50
                                                                                        ELSE ND.nextNonDigitPos - 1
                                                                                    END
                                                                                )
                                                                      ELSE NULL
                                                                  END AS ORDER_NO
                                                        ) AS O
                                            WHERE L.PHONE    = I.PHONE
                                              AND L.SUBJECT IN ( N''크나우프석고보드_보류오더 안내'', N''크나우프석고보드_오더접수'' )
                                              AND O.ORDER_NO  = I.ORDER_NO
                                        )'
            EXEC sys.sp_executesql @SQL
        END

        /* [6] 이전월 로그 검사 */
        IF OBJECT_ID(@PREV_RAW, N'U') IS NOT NULL
        BEGIN
            SET @SQL = N'
                          UPDATE M
                             SET STATUS = N''3''
                            FROM dbo.MMS_MSG AS M
                                 JOIN #ins_norm   AS I
                                   ON M.MSGKEY = I.MSGKEY
                           WHERE I.ORDER_NO IS NOT NULL
                             AND LEN(I.ORDER_NO) > 0
                             AND EXISTS (
                                           SELECT 1
                                             FROM ' + @PREV_Q + N' AS L
                                            CROSS APPLY ( SELECT CHARINDEX(N''오더번호'', L.MSG) AS pos ) AS P
                                            CROSS APPLY (
                                                           SELECT CASE
                                                                      WHEN P.pos > 0 THEN SUBSTRING(L.MSG, P.pos + LEN(N''오더번호''), 300)
                                                                      ELSE N''''
                                                                  END AS tail
                                                        ) AS T
                                            CROSS APPLY ( SELECT PATINDEX(N''%[0-9]%'', T.tail) AS dstart ) AS D
                                            CROSS APPLY ( SELECT PATINDEX(N''%[^0-9]%'', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND
                                            CROSS APPLY (
                                                            SELECT CASE
                                                                       WHEN P.pos > 0 AND D.dstart > 0
                                                                            THEN SUBSTRING(
                                                                                             T.tail
                                                                                           , D.dstart
                                                                                           , CASE
                                                                                                 WHEN ND.nextNonDigitPos = 0 THEN 50
                                                                                                 ELSE ND.nextNonDigitPos - 1
                                                                                             END
                                                                                          )
                                                                       ELSE NULL
                                                                   END AS ORDER_NO
                                                        ) AS O
                                            WHERE L.PHONE    = I.PHONE
                                              AND L.SUBJECT IN ( N''크나우프석고보드_보류오더 안내'' , N''크나우프석고보드_오더접수'' )
                                              AND O.ORDER_NO  = I.ORDER_NO
                                        )'
            EXEC sys.sp_executesql @SQL
        END
    END TRY
    BEGIN CATCH
        RETURN
    END CATCH
END
GO
```

---

## 🧪 테스트 시나리오

### 테스트 환경 설정
```sql
-- 테스트 데이터 준비 (기본 중복 체크용 데이터)
INSERT INTO MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE)
VALUES
    (10001, '010-1234-5678', N'크나우프석고보드_보류오더 안내', N'안녕하세요. 오더번호 123456 보류 안내드립니다.', '1', GETDATE()),
    (10002, '010-9876-5432', N'크나우프석고보드_오더접수', N'오더접수: 오더번호789012 처리완료', '1', GETDATE()),
    (10003, '010-1111-2222', N'크나우프석고보드_보류오더 안내', N'오더번호: 555999번 지연안내', '1', GETDATE())

-- 로그 테이블 테스트 데이터 (실제 스키마에 맞춤)
-- 현재월: MMS_LOG_202508
INSERT INTO MMS_LOG_202508 (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE, CALLBACK)
VALUES
    (99001, '010-5555-6666', N'크나우프석고보드_보류오더 안내', N'[테스트] - 오더번호: 333777 - 보류처리', '1', GETDATE(), 'Y'),
    (99002, '010-7777-8888', N'크나우프석고보드_오더접수', N'[테스트] - 오더번호: 111222 - 접수완료', '1', GETDATE(), 'Y')

-- 이전월: MMS_LOG_202507
INSERT INTO MMS_LOG_202507 (MSGKEY, PHONE, SUBJECT, MSG, STATUS, REQDATE, CALLBACK)
VALUES
    (99003, '010-9999-0000', N'크나우프석고보드_보류오더 안내', N'[테스트] - 오더번호: 444888 - 이전월처리', '1', DATEADD(MONTH, -1, GETDATE()), 'Y'),
    (99004, '010-8888-7777', N'크나우프석고보드_오더접수', N'[테스트] - 오더번호: 666999 - 완료', '1', DATEADD(MONTH, -1, GETDATE()), 'Y')
```

### 테스트 케이스 및 결과

| 테스트 | 시나리오 | 입력 데이터 | 예상 결과 | 실제 결과 |
|--------|----------|-------------|-----------|-----------|
| **Test 1** | 중복 없는 새 데이터 | PHONE: 010-0000-1111<br/>오더번호: 999888 | STATUS = 1 | ✅ **성공** |
| **Test 2** | MMS_MSG 내 중복 | PHONE: 010-1234-5678<br/>오더번호: 123456 | STATUS = 3 | ✅ **성공** |
| **Test 3** | 현재월 로그 중복 | PHONE: 010-5555-6666<br/>오더번호: 333777 | STATUS = 3 | ✅ **성공** |
| **Test 4** | 이전월 로그 중복 | PHONE: 010-9999-0000<br/>오더번호: 444888 | STATUS = 3 | ✅ **성공** |
| **Test 5** | 오더접수 타입 중복 | PHONE: 010-9876-5432<br/>오더번호: 789012 | STATUS = 3 | ✅ **성공** |
| **Test 6** | 오더번호 없는 메시지 | MSG: "일반 안내 메시지" | STATUS = 1 | ✅ **성공** |
| **Test 7** | 다른 제목 (대상 외) | SUBJECT: "일반 안내" | STATUS = 1 | ✅ **성공** |

**🎯 최종 테스트 성공률: 100% (7/7)**

---

## ⚡ 주요 기술적 특징

### 1. PATINDEX 기반 범용 파싱
```sql
-- 기존 방식: 구분자별 개별 처리
CASE
    WHEN CHARINDEX(CHAR(13), MSG) > 0 THEN ...
    WHEN CHARINDEX(CHAR(10), MSG) > 0 THEN ...
    WHEN CHARINDEX('-', MSG) > 0 THEN ...
END

-- 개선된 방식: 숫자 패턴 직접 인식
PATINDEX('%[0-9]%', tail)  -- 첫 번째 숫자 위치
PATINDEX('%[^0-9]%', substring)  -- 숫자가 아닌 문자 위치
```

### 2. CROSS APPLY 최적화
- **장점**: 단계별 계산 결과를 명확히 분리
- **성능**: 복잡한 중첩 함수 대신 단계별 처리
- **가독성**: 각 단계별 결과를 명확히 확인 가능

### 3. 동적 SQL 보안
```sql
-- SQL 인젝션 방지
SET @CURR_Q = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @CURR_YM)
-- 결과: [dbo].[MMS_LOG_202508]
```

---

## 🛡️ 안전성 및 호환성

### 호환성
- ✅ **SQL Server 2008+** 모든 버전 지원
- ✅ **기존 스키마 무변경** (테이블 구조 그대로 유지)
- ✅ **세미콜론 파싱 문제** 해결 (구 버전 호환)

### 안전성
- ✅ **TRY-CATCH 블록**: 오류 발생 시 안전한 종료
- ✅ **임시 테이블 정리**: 메모리 누수 방지
- ✅ **NULL 체크**: 오더번호 추출 실패 시 안전 처리

### 성능
- ✅ **조기 종료**: 대상 메시지가 없으면 즉시 RETURN
- ✅ **인덱스 활용**: MSGKEY 기반 JOIN 사용
- ✅ **동적 SQL 최소화**: 필요시에만 실행

---

## 📈 운영 효과

### 기대 효과
1. **중복 발송 방지**: 동일 오더번호의 중복 SMS 발송 차단
2. **고객 만족도 향상**: 불필요한 중복 메시지로 인한 고객 불편 해소
3. **발송 비용 절약**: 중복 발송으로 인한 SMS 요금 절약
4. **시스템 안정성**: 자동화된 중복 체크로 운영 효율성 증대

### 모니터링 방법
```sql
-- 중복 처리 현황 조회
SELECT
    COUNT(*) AS 총발송건수,
    SUM(CASE WHEN STATUS = '3' THEN 1 ELSE 0 END) AS 중복처리건수,
    CAST(SUM(CASE WHEN STATUS = '3' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS DECIMAL(5,2)) AS 중복비율
FROM MMS_MSG
WHERE SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수')
  AND REQDATE >= DATEADD(DAY, -1, GETDATE())
```

---

## 🔧 유지보수 가이드

### 정기 점검 항목
1. **월별 로그 테이블 생성**: 새 달 시작 시 `MMS_LOG_YYYYMM` 테이블 생성 확인
2. **트리거 활성화 상태**: `SELECT name, is_disabled FROM sys.triggers WHERE name = 'TRG_MMS_MSG_DUP_CHECK'`
3. **성능 모니터링**: 대량 INSERT 시 처리 시간 모니터링

### 문제 해결
```sql
-- 트리거 비활성화 (긴급시)
DISABLE TRIGGER TRG_MMS_MSG_DUP_CHECK ON MMS_MSG

-- 트리거 재활성화
ENABLE TRIGGER TRG_MMS_MSG_DUP_CHECK ON MMS_MSG

-- 수동 중복 체크 (트리거 우회)
UPDATE MMS_MSG SET STATUS = '3'
WHERE MSGKEY IN (SELECT 중복된_MSGKEY FROM 수동조회결과)
```

---

