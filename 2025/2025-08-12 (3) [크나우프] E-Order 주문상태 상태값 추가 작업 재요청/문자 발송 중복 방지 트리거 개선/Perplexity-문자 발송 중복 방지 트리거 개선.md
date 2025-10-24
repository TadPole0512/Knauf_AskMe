## 새로운 문자 발송 요청이 발생했을 경우 중복 MMS 문자 발송 방지를 위해서 전화번호와 문자 메시지의 내용이 같을 경우 상태값(문자발송:3)으로 변경.
- 아래와 같이 트리거를 만들었는데, 문자 내용 중 일부(주소)가 달라서 문자가 발송됨.
- 문자 내용 중 중요한 정보(오더번호)가 같은 경우 문자 발송 차단을 위해 새로 들어온 문자 발송 요청의 상태값을 3으로 번경하려고 함.


아래는 클라이언트가 보내온 메일 내용
---------------------------------------------------------------------
조회를 해보니 상세주소가 알 수 없는 사유로 변경되어서 달라진 내용이기에 메시지가 전달 되었습니다
그렇기에 Create Trigger를 하기의 조건에 맞게 수정 요청 드립니다, 혹은 더 좋은 방법이 있다면 말씀 해주세요
```
    조건 값
­     -   SUBJECT가 ‘크나우프석고보드_보류오더 안내’ 일 때 MSG안에 ‘오더번호 XXXXXXXXX’이 중복된 경우 STATUS_CD 3으로 변경
­     -   SUBJECT가 ‘크나우프석고보드_오더접수’일 때 MSG안에 ‘오더번호: XXXXXXXXX’이 중복된 경우 STATUS_CD 3으로 변경
```
-----

문자 발송 요청시
  - 전화번호와 문자 내용을 제외한 다른 컬럼의 내용은 모두 같음.
  - 문자 발송 요청은 다른 곳에서 넘어오기 때문에 값 자체를 수정할 수 없음.


아래는 예를 보여주기 위해 첨부한 것으로 저장된 내용 중 일부(상세 주소)만 다름.
---------------------------------------------------------------------

[크나우프석고보드 오더접수내역]
- 고객사명: 미르티앤씨㈜
- 출  하  지: 구로구
- 오더번호: 400418728
- 상세주소: KR 군자 디지털과학고등학교 - 미르티앤씨(주)
경기 시흥시 군자로487번길 54 (거모동, 군 자디지
- 납 기 일: 2025-08-12 07:00
- 품목 및 수량:
방화 12.5*900*1800 평보드 (46.000 PC)
Sheetrock Gyptex2 9.5*300*600 (150.000 PAC)

[크나우프석고보드 오더접수내역]
- 고객사명: 미르티앤씨㈜
- 출  하  지: 구로구
- 오더번호: 400418728
- 상세주소: 미르티앤씨㈜
경기 시흥시 군자로487번길 54 (거모동, 군 자디지
- 납 기 일: 2025-08-12 07:00
- 품목 및 수량:
방화 12.5*900*1800 평보드 (46.000 PC)
Sheetrock Gyptex2 9.5*300*600 (150.000 PAC)

위의 내용을 검토한 다음, 클라이언트의 요구처럼 더 나은 방안을 찾기 위해 더 필요한 자료가 무엇인지 알려줘.

---------------------------------------------------------------------


좋아요, 지금 상황을 정리하면  

- 현재 **문자 발송 중복 방지 트리거**는 `전화번호 + 문자 전체 내용`이 같을 때만 상태값을 `3`으로 변경해서 발송 차단  
- 그런데 **주소 등 사소하게 달라지는 부분** 때문에, 사실상 같은 오더임에도 발송이 되고 있음  
- 클라이언트는 **"오더번호" 기준으로 중복 체크**하고 싶어 함  
- `SUBJECT` 값이 특정 키워드일 때만, `MSG`에서 **오더번호 추출 → 기존 요청 중 이미 존재하면 발송 차단** 하는 로직이 필요  

***

## 1. 현재 정보로 부족한 부분
더 나은 방안을 만들려면, 아래 추가 자료들이 필요합니다.

### (A) DB Schema 관련
- **문자 발송 요청 테이블 컬럼 구조**
  - `SUBJECT` 컬럼명과 실제 저장 예시 (한글/공백/언더바 여부)
  - `MSG` 컬럼명과 최대 길이
  - `STATUS_CD` 상태값 종류 표 (0,1,2,3 등 의미)
  - 요청 시간(`CREATE_DT` 같은 컬럼) 여부
  - `PHONE` 컬럼명 확인

### (B) 중복 판단 기준 확인
- **중복 판단 범위**: 오더번호가 같은 경우를 *언제까지* 중복으로 볼지  
  - 예) 같은 오더번호라도 하루 뒤에는 다시 발송해야 하는지
- 같은 번호로 여러 건 발송했을 때 **유효 시간 제한** 있는지

### (C) 현행 트리거 로직
- 현재 만든 트리거 SQL 전문
- 중복 체크 시 `WHERE` 절에서 어떤 조건을 쓰고 있는지
- MSG 비교가 LIKE/=`=`/INSTR/REGEXP 중 어떤 방식인지

### (D) 데이터 패턴
- `MSG` 본문에서 오더번호 패턴이 항상 `"오더번호: 123456789"`인지, `"오더번호 123456789"`처럼 띄어쓰기/콜론/하이픈이 바뀌는 경우가 있는지
- `SUBJECT` 값도 완전히 동일하게 들어오는지, 아니면 `(RE)` 같은 접두사가 붙는 경우가 있는지

***

## 2. 이유
이 자료들이 필요한 이유는:
- 트리거에서 **문구 일부를 추출해서 비교**하려면 정규식/문자열 함수가 필요하고, 패턴이 불안정하면 문제가 생김  
- 중복 시간 제한이 없으면, 과거 기록 모두 검색해야 해서 성능 이슈 가능  
- 현재 DB 구조를 모르면 업데이트 쿼리 작성이 불가능

***

## 3. 다음 단계 제안
1. **DB 구조/컬럼명 + 예시 데이터 수집**  
2. **MSG 내 오더번호 추출 패턴 확정** (정규식 가능 여부 포함)  
3. **중복 기간 설정 여부 결정** (시간 조건 포함할지)  
4. 현재 트리거 로직에 `"SUBJECT 조건 + 오더번호 추출"` 로직 삽입  
5. 테스트 데이터로 시뮬레이션  

***

### 📌 질문
그러면 제가 최적의 트리거 SQL 예시를 만들어 드리려면,  
먼저 다음 3가지를 알려주셔야 해요.

1. 문자 발송 요청 테이블 이름과 각 컬럼 이름  
2. `MSG`와 `SUBJECT` 필드의 실제 저장 형식 예시 2~3개  
3. 같은 오더번호를 중복 발송 차단해야 하는 **기간 제한** 여부  

***

원하시면, 이 자료 받는 즉시 제가  
**“주소 일부가 달라도, 같은 오더번호면 STATUS_CD=3으로 변경하는 트리거”**  
를 완전한 SQL로 작성해 드릴게요.  

그럼 이 3가지 먼저 확인해 주시겠어요?  
그걸 기초로 최적 로직을 드리겠습니다.


---------------------------------------------------------------------


# [크나우프석고보드 오더접수내역]과 [크나우프석고보드 보류오더 안내]의 문자 내용은 같은 형식이야.
DB 스키마나 전송되서 오는 문자 발송 요청 내용은 수정할 수 없어.

**1. 데이터베이스 구조 관련 정보**

[테이블 구조]
```sql
CREATE TABLE dbo.MMS_MSG (
MSGKEY numeric(11,0) NOT NULL,
SUBJECT varchar(120) NULL,
PHONE varchar(15) NULL,
CALLBACK varchar(15) NULL,
STATUS varchar(2) NULL,
REQDATE datetime2(0) NULL,
MSG varchar(4000) NULL,
FILE_CNT numeric(38,0) NULL,
FILE_CNT_REAL numeric(38,0) NULL,
FILE_PATH1 varchar(128) NULL,
FILE_PATH1_SIZ numeric(38,0) NULL,
FILE_PATH2 varchar(128) NULL,
FILE_PATH2_SIZ numeric(38,0) NULL,
FILE_PATH3 varchar(128) NULL,
FILE_PATH3_SIZ numeric(38,0) NULL,
FILE_PATH4 varchar(128) NULL,
FILE_PATH4_SIZ numeric(38,0) NULL,
FILE_PATH5 varchar(128) NULL,
FILE_PATH5_SIZ numeric(38,0) NULL,
EXPIRETIME varchar(10) NULL,
SENTDATE datetime2(0) NULL,
RSLTDATE datetime2(0) NULL,
REPORTDATE datetime2(0) NULL,
TERMINATEDDATE datetime2(0) NULL,
RSLT varchar(10) NULL,
REPCNT numeric(38,0) NULL,
[TYPE] varchar(2) NULL,
TELCOINFO varchar(10) NULL,
ID varchar(20) NULL,
POST varchar(20) NULL,
ETC1 varchar(64) NULL,
ETC2 varchar(32) NULL,
ETC3 varchar(32) NULL,
ETC4 numeric(38,0) NULL,
READDATE datetime2(0) NULL,
ROUTE_ID varchar(20) NULL,
MULTI_SEQ varchar(10) NULL,
KISA_ORIGCODE varchar(10) NULL,
REAL_ID varchar(10) NULL,
    CONSTRAINT CONSTR_MMS_MSG PRIMARY KEY (MSGKEY)
);
```
**2. 문자 내용에서 '오더번호'를 추출하는 규칙**
```diff
오더번호의 형식: '오더번호'의 형식은 항상 숫자로만 구성되어 있나요? 아니면 영문이나 특수문자가 포함될 수도 있나요? (예: 400418728)
    => 항상 숫자임
고정된 위치: '오더번호'라는 키워드는 항상 메시지 내용의 같은 위치에 있나요? 아니면 위치가 바뀔 수도 있나요?
    => 현재는 고정임. 추후는 모르겠음.
정규표현식(Regex) 필요 여부: 문자 내용이 항상 고정된 형식이라면 LIKE나 SUBSTRING으로 처리가 가능할 수도 있지만, 내용이 가변적이라면 정규표현식을 사용해야 정확하게 오더번호를 추출할 수 있습니다. 예를 들어, '오더번호: 400418728'에서 '400418728'만 추출하려면 정규표현식이 가장 안정적인 방법입니다.
    => 그걸 물어보는 거임.
```
**3. 현재 트리거의 상세 코드**
```sql
-- ===========================================================================================
-- 트리거명 : TRG_MMS_MSG_DUP_CHECK
-- 대상 테이블 : MMS_MSG
-- 동작 : INSERT된 데이터에 대해 중복(PHONE+MSG) 있으면 STATUS=3으로 자동 변경
--        (중복 기준: MMS_MSG 자기자신, MMS_LOG_현재월, MMS_LOG_이전월)
-- 주의 : 동적 쿼리에서는 inserted 테이블 대신 #ins 임시테이블 사용
-- ===========================================================================================

CREATE TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- [1] inserted 테이블 내용을 임시테이블로 복사 (동적 쿼리에서 사용하려면 필수)
    IF OBJECT_ID('tempdb..#ins') IS NOT NULL DROP TABLE #ins;
    SELECT * INTO #ins FROM inserted;

    -- [2] 현재월, 이전월 로그 테이블명 동적으로 생성 (YYYYMM 포맷)
    DECLARE @CURR_YM VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112);
    DECLARE @PREV_YM VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112);
    DECLARE @CURR_LOG_TBL NVARCHAR(64) = N'MMS_LOG_' + @CURR_YM;
    DECLARE @PREV_LOG_TBL NVARCHAR(64) = N'MMS_LOG_' + @PREV_YM;

    -- [3-1] MMS_MSG 자기자신에서 중복(PHONE+MSG, 자기 자신 제외)
    UPDATE M
    SET STATUS = '3'
    FROM MMS_MSG M
    INNER JOIN #ins I ON M.MSGKEY = I.MSGKEY
    WHERE EXISTS (
        SELECT 1 FROM MMS_MSG MM
        WHERE MM.PHONE = I.PHONE AND MM.MSG = I.MSG AND MM.MSGKEY <> I.MSGKEY
    );

    -- [3-2] 현재월 로그 테이블(MMS_LOG_YYYYMM)에서 중복
    IF OBJECT_ID(@CURR_LOG_TBL, 'U') IS NOT NULL
    BEGIN
        DECLARE @SQL_CURR NVARCHAR(MAX) = N'
            UPDATE M
            SET STATUS = ''3''
            FROM MMS_MSG M
            INNER JOIN #ins I ON M.MSGKEY = I.MSGKEY
            WHERE EXISTS (
                SELECT 1 FROM ' + QUOTENAME(@CURR_LOG_TBL) + ' L
                WHERE L.PHONE = I.PHONE AND L.MSG = I.MSG
            )
        ';
        EXEC sp_executesql @SQL_CURR;
    END

    -- [3-3] 이전월 로그 테이블(MMS_LOG_YYYYMM)에서 중복
    IF OBJECT_ID(@PREV_LOG_TBL, 'U') IS NOT NULL
    BEGIN
        DECLARE @SQL_PREV NVARCHAR(MAX) = N'
            UPDATE M
            SET STATUS = ''3''
            FROM MMS_MSG M
            INNER JOIN #ins I ON M.MSGKEY = I.MSGKEY
            WHERE EXISTS (
                SELECT 1 FROM ' + QUOTENAME(@PREV_LOG_TBL) + ' L
                WHERE L.PHONE = I.PHONE AND L.MSG = I.MSG
            )
        ';
        EXEC sp_executesql @SQL_PREV;
    END

    -- [마무리] 트리거 종료 시 임시테이블 자동 삭제 (tempdb 내에서만 유효)

END
GO
```

**2. 비즈니스 로직 상세**
```
같은 오더번호로 몇 번까지 발송이 허용되나? (1회만? 아니면 특정 횟수?)
    => 발송 요청이 테이블에 저장될 때마다 문자 발송됨. 제한 없음. 상태값이 0이면 발송됨
중복 판단 기간이 있나? (예: 24시간 이내만 체크)
    => 없음
다른 SUBJECT 유형도 있나?
    => 현재는 없음.
전화번호가 다르면 같은 오더번호라도 발송해야 하나?
    => 발송해야 함. 개별 건으로 봐야함
```

**3. 운영 환경**
```
동시 문자 발송 요청량 (TPS)
    => 10초 단위로 보면 10건 미만
데이터베이스 종류 (Oracle, MySQL, PostgreSQL 등)
    => mssql
문자 발송 테이블의 대략적인 데이터량
    => 발송된 이후 로그 테이블로 데이터 옮기기 때문에 몇 건 안됨
```
**4. 현재 트리거의 성능 이슈**
```
트리거 실행 시 응답시간
    => 빠름.
락 대기나 데드락 발생 여부
    => 없음.
```

---------------------------------------------------------------------


좋아요, 이제 주신 정보를 토대로 보면  
이건 **전화번호+MSG 완전 일치 기준** 대신,  
"SUBJECT 조건 + 오더번호 일치"로 비교하도록 트리거를 수정하면 되는 상황이에요.  
주소 등 일부 내용이 달라져도 오더번호만 같으면 STATUS=3으로 바꾸면 되죠.

***

## 1. 핵심 변경 포인트
현재 트리거는

```sql
WHERE MM.PHONE = I.PHONE AND MM.MSG = I.MSG
```

이 부분이 **문자 전체 일치** 기준이에요.  
우리는 여기서 MSG를 그대로 비교하지 않고, **MSG에서 오더번호만 추출해서 비교**해야 해요.

***

## 2. 오더번호 추출 방법 선택
주신 조건들을 보면:

- ‘오더번호’ 키워드는 고정돼 있음 (현재 기준)  
- 형식은 항상 숫자 (`[0-9]+`)  
- 위치는 고정이지만, 추후 변동 가능성 있음  

그렇다면 **SUBSTRING + CHARINDEX** 조합으로 하는 단순 추출법도 가능하고,  
좀 더 안전하게 하려면 **PATINDEX(정규표현 비슷)**를 쓰는 방법이 좋아요.  

MSSQL 표준 정규표현식은 없지만, 패턴 매칭은 가능합니다.

예:  
```sql
-- "오더번호: 123456789" 에서 숫자부분만 추출
SUBSTRING(MSG, CHARINDEX('오더번호', MSG) + 6, 
          PATINDEX('%[^0-9]%', SUBSTRING(MSG, CHARINDEX('오더번호', MSG) + 6, 20)) - 1)
```
이렇게 하면 "오더번호"라는 키워드 뒤의 첫 번째 숫자 덩어리만 가져옵니다.  

***

## 3. 변경된 트리거 로직 예시
아래는 현재 트리거를 오더번호 기준으로 수정한 예시예요.  
`SUBJECT` 조건(두 가지 케이스)에만 적용되게 하고,  
"전화번호 + 오더번호"가 같으면 STATUS=3으로 업데이트하게 합니다.

```sql
ALTER TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    IF OBJECT_ID('tempdb..#ins') IS NOT NULL DROP TABLE #ins;
    SELECT * INTO #ins FROM inserted;

    DECLARE @CURR_YM VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112);
    DECLARE @PREV_YM VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112);
    DECLARE @CURR_LOG_TBL NVARCHAR(64) = N'MMS_LOG_' + @CURR_YM;
    DECLARE @PREV_LOG_TBL NVARCHAR(64) = N'MMS_LOG_' + @PREV_YM;

    -------------------------------------------------------------------
    -- 함수: MSG에서 오더번호 추출 (첫 번째 숫자 덩어리)
    -------------------------------------------------------------------
    ;WITH I_EXT AS (
        SELECT *,
            CASE 
                WHEN SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수') THEN
                    SUBSTRING(MSG, 
                              CHARINDEX(N'오더번호', MSG) + 6, 
                              PATINDEX('%[^0-9]%', SUBSTRING(MSG, CHARINDEX(N'오더번호', MSG) + 6, 20)) - 1)
                ELSE NULL
            END AS ORDER_NO
        FROM #ins
    )
    -------------------------------------------------------------------
    -- [3-1] MMS_MSG에서 중복(전화번호+오더번호) 체크
    -------------------------------------------------------------------
    UPDATE M
    SET STATUS = '3'
    FROM MMS_MSG M
    JOIN I_EXT I ON M.MSGKEY = I.MSGKEY
    WHERE I.ORDER_NO IS NOT NULL
      AND EXISTS (
          SELECT 1 
          FROM MMS_MSG MM
          WHERE MM.PHONE = I.PHONE 
            AND SUBSTRING(MM.MSG, CHARINDEX(N'오더번호', MM.MSG) + 6, 
                   PATINDEX('%[^0-9]%', SUBSTRING(MM.MSG, CHARINDEX(N'오더번호', MM.MSG) + 6, 20)) - 1) = I.ORDER_NO
            AND MM.MSGKEY <> I.MSGKEY
      );

    -------------------------------------------------------------------
    -- [3-2] 현재월 로그
    -------------------------------------------------------------------
    IF OBJECT_ID(@CURR_LOG_TBL, 'U') IS NOT NULL
    BEGIN
        DECLARE @SQL_CURR NVARCHAR(MAX) = N'
            UPDATE M
            SET STATUS = ''3''
            FROM MMS_MSG M
            JOIN (
                SELECT MSGKEY, PHONE,
                       SUBSTRING(MSG, CHARINDEX(N'''오더번호''', MSG) + 6, 
                           PATINDEX(''%[^0-9]%'', SUBSTRING(MSG, CHARINDEX(N'''오더번호''', MSG) + 6, 20)) - 1) AS ORDER_NO
                FROM #ins
                WHERE SUBJECT IN (N''크나우프석고보드_보류오더 안내'', N''크나우프석고보드_오더접수'')
            ) I ON M.MSGKEY = I.MSGKEY
            WHERE EXISTS (
                SELECT 1 FROM ' + QUOTENAME(@CURR_LOG_TBL) + N' L
                WHERE L.PHONE = I.PHONE
                  AND SUBSTRING(L.MSG, CHARINDEX(N'''오더번호''', L.MSG) + 6, 
                      PATINDEX(''%[^0-9]%'', SUBSTRING(L.MSG, CHARINDEX(N'''오더번호''', L.MSG) + 6, 20)) - 1) = I.ORDER_NO
            )
        ';
        EXEC sp_executesql @SQL_CURR;
    END

    -------------------------------------------------------------------
    -- [3-3] 이전월 로그
    -------------------------------------------------------------------
    IF OBJECT_ID(@PREV_LOG_TBL, 'U') IS NOT NULL
    BEGIN
        DECLARE @SQL_PREV NVARCHAR(MAX) = N'
            UPDATE M
            SET STATUS = ''3''
            FROM MMS_MSG M
            JOIN (
                SELECT MSGKEY, PHONE,
                       SUBSTRING(MSG, CHARINDEX(N'''오더번호''', MSG) + 6, 
                           PATINDEX(''%[^0-9]%'', SUBSTRING(MSG, CHARINDEX(N'''오더번호''', MSG) + 6, 20)) - 1) AS ORDER_NO
                FROM #ins
                WHERE SUBJECT IN (N''크나우프석고보드_보류오더 안내'', N''크나우프석고보드_오더접수'')
            ) I ON M.MSGKEY = I.MSGKEY
            WHERE EXISTS (
                SELECT 1 FROM ' + QUOTENAME(@PREV_LOG_TBL) + N' L
                WHERE L.PHONE = I.PHONE
                  AND SUBSTRING(L.MSG, CHARINDEX(N'''오더번호''', L.MSG) + 6, 
                      PATINDEX(''%[^0-9]%'', SUBSTRING(L.MSG, CHARINDEX(N'''오더번호''', L.MSG) + 6, 20)) - 1) = I.ORDER_NO
            )
        ';
        EXEC sp_executesql @SQL_PREV;
    END
END
GO
```

***

## 4. 이렇게 하면
- `SUBJECT`가 두 가지 케이스 중 하나인 문자만 체크
- `전화번호 + 오더번호`가 기존 발송 건과 동일하면 `STATUS=3`로 변경 → 발송 차단
- 주소나 기타 내용이 달라도 오더번호만 동일하면 막히게 됨
- 전화번호가 다르면 중복으로 보지 않음

***

## 5. 추가 팁
- **오더번호 위치가 변동되면** 지금의 `SUBSTRING+CHARINDEX`는 실패 가능성이 있으니,  
  추후 변경 가능성 대비해서 `PATINDEX('%[0-9][0-9][0-9]%',...)`처럼 "첫 번째 숫자" 위치를 찾아 추출하는 로직을 쓰는 게 더 안전합니다.
- 오더번호가 8~10자리처럼 일정한 길이라면, `LEFT`로 자르거나 길이 검증 조건을 넣으면 더 깔끔합니다.

***

제가 봤을 땐 지금 구조로는 **정규식 함수 없이도** 충분히 동작 가능하지만,  
오더번호 위치가 고정이 아니면 `PATINDEX` 기반으로 바꾸는 게 안전해요.  

***

원하시면 제가 지금 바로 **"오더번호 위치 변동 가능성까지 대비한 버전"** 도 만들어 드릴까요?  
그러면 나중에 메시지 포맷이 조금 바뀌어도 쓸 수 있습니다.

---------------------------------------------------------------------

# mssql에서 select... into 로 테이블 생성 시에 case 문 오류가 나는 이유를 알려줘

---------------------------------------------------------------------

MSSQL에서 `SELECT ... INTO` 문을 사용할 때 `CASE` 문에서 오류가 나는 주요 이유와 해결 방법을 쉽게 설명할게요.

***

## 1. 오류가 나는 주요 원인

- **데이터 타입 불일치 문제**
  - `CASE` 문 내에서 `THEN` 절이나 `ELSE` 절에 반환하는 값들의 데이터 타입이 모두 일치하지 않을 경우 오류가 발생합니다.
  - 예를 들어, `THEN`에 숫자(예: 1)를 쓰고, `ELSE`에 문자열(예: 'test')을 같이 쓰면 데이터 타입 충돌로 오류가 납니다.
  - MSSQL은 `CASE` 결과 컬럼이 한 가지 데이터 타입이어야 하므로 자동 변환이 가능하도록 타입을 맞춰줘야 합니다.

- **`SELECT ... INTO`의 테이블 생성 특성**
  - `SELECT ... INTO`는 결과 컬럼을 보고 새 테이블을 생성하는데, `CASE` 결과값의 데이터 타입이 불명확하거나 컬럼마다 서로 다른 타입이 혼재하면 테이블 생성 시 오류가 나거나 의도치 않은 컬럼 타입이 만들어질 수 있습니다.

***

## 2. 해결법 및 주의사항

- `CASE` 문 안의 모든 `THEN`과 `ELSE` 값의 데이터 타입을 일치시킨다.
  - 숫자와 문자열이 섞일 경우에는 `CAST` 또는 `CONVERT` 함수를 써서 하나의 타입(보통 문자열)으로 맞추는 게 가장 쉽습니다.
  
  ```sql
  SELECT
    CASE 
      WHEN 조건 THEN CAST(숫자값 AS NVARCHAR(10))
      ELSE '문자열값'
    END AS 컬럼명
  INTO 새테이블명
  FROM 원본테이블;
  ```

- `SELECT ... INTO` 구문에서는 `CASE` 문뿐 아니라 모든 컬럼의 데이터가 일관된 타입으로 정해져야 새 테이블이 정상적으로 생성됩니다.

- 만약 오류 메시지가 있다면, 메시지를 바탕으로 어느 부분의 데이터 타입이 불일치하는지 확인해 보세요.

***

## 3. 예시 코드

```sql
SELECT
  EmployeeID,
  CASE 
    WHEN Score >= 80 THEN CAST('A' AS NVARCHAR(1))
    WHEN Score >= 60 THEN 'B'
    ELSE 'C'
  END AS Grade
INTO NewEmployeeGrades
FROM Employees;
```

이렇게 하면 Score 값에 따라 Grade 컬럼에 문자형 등급이 들어가고, 오류 없이 새 테이블이 생성됩니다.

***

필요하면 실제 오류 메시지와 함께 코드를 보여주시면 더 구체적으로 문제를 짚어드릴 수 있어요! 기본적으로는 `CASE` 문 안 데이터 타입을 맞추는 것이 핵심입니다. 부담 없이 질문 주세요!

[1] https://121202.tistory.com/46
[2] https://learn.microsoft.com/ko-kr/sql/relational-databases/errors-events/database-engine-events-and-errors-2000-to-2999?view=sql-server-ver17
[3] https://cobook.tistory.com/115
[4] https://learn.microsoft.com/ko-kr/sql/t-sql/queries/select-into-clause-transact-sql?view=sql-server-ver17
[5] https://halizy.tistory.com/11
[6] https://goldswan.tistory.com/14
[7] https://learn.microsoft.com/ko-kr/sql/relational-databases/errors-events/database-engine-events-and-errors-8000-to-8999?view=sql-server-ver17
[8] https://www.codeit.kr/community/questions/UXVlc3Rpb246NWY0MGNiZGY0ZWUyMjA2MGIwN2QwYTE4
[9] https://sosopro.tistory.com/57
[10] https://ttleader.tistory.com/47
[11] https://insusu.tistory.com/entry/MSSQL-Server-18452-%EC%98%A4%EB%A5%98
[12] https://suy379.tistory.com/110
[13] https://learn.microsoft.com/ko-kr/sql/relational-databases/errors-events/database-engine-events-and-errors-1000-to-1999?view=sql-server-ver17
[14] https://im-first-rate.tistory.com/61
[15] https://woogie-db.tistory.com/37
[16] https://sosopro.tistory.com/60
[17] https://woogie-db.tistory.com/66
[18] https://la-melagrana.tistory.com/26

---------------------------------------------------------------------

# 아래 트리거 생성 쿼리에서 case 문이 없으면 정상적으로 생성이 되는데, case 문이 있으면 문법 오류가 나는 이유를 알려줘.
```sql
ALTER TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- [1] inserted 테이블 내용을 임시테이블로 복사 (동적 쿼리에서 사용하려면 필수)

    /* [1] inserted → #ins (오더번호 추출 포함) */
    IF OBJECT_ID('tempdb..#ins') IS NOT NULL
        DROP TABLE #ins;


    SELECT
           I.*
         , CASE
               WHEN 12 > 0 THEN 12
               ELSE 13
           END AS ORDER_NO
    INTO #ins
    FROM inserted AS I
    WHERE I.SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수')
    ;

    -- [마무리] 트리거 종료 시 임시테이블 자동 삭제 (tempdb 내에서만 유효)

END
GO
```
---------------------------------------------------------------------

트리거 내부에서 `SELECT ... INTO` 구문에 `CASE` 문이 있으면 문법 오류가 나는 이유는 주로 다음과 같은 제약과 특성 때문입니다.

***

## 1. 트리거 내에서 `SELECT ... INTO`의 제한

- `SELECT ... INTO` 문은 결과 집합을 기반으로 새 테이블을 생성하는 구문입니다.
- 트리거 내부에서 임시 테이블을 생성하는 데 쓰일 수 있지만, 트리거는 저장 프로시저와 달리 실행 컨텍스트와 제약이 엄격합니다.
- 특히 트리거 내에서 데이터 타입이 불명확하거나 복잡한 표현식을 포함하면 실행에 문제가 생길 수 있습니다.

***

## 2. `CASE` 문과 데이터 타입 불일치 문제

- `CASE` 문 내 `THEN`과 `ELSE` 절에 반환하는 값들의 데이터 타입이 모두 일치해야 합니다.
- 만약 `WHEN` 조건별 값들의 타입이 다르면 컴파일 오류가 발생합니다.
- 임시 테이블을 생성하는 시점에 모든 컬럼의 타입이 결정되는데, `CASE` 문이 데이터를 혼합 타입으로 반환하면 정확한 타입 지정이 어려워 오류가 날 수 있습니다.

***

## 3. 해결 방안과 권장 사항

- `CASE` 문의 모든 반환 값이 같은 데이터 타입이 되도록 `CAST` 또는 `CONVERT` 함수로 명시적인 형 변환을 해줍니다.

  ```sql
  SELECT
      I.*,
      CASE
          WHEN 12 > 0 THEN CAST(12 AS INT)  -- 또는 VARCHAR 타입일 경우 모두 문자열로 통일
          ELSE CAST(13 AS INT)
      END AS ORDER_NO
  INTO #ins
  FROM inserted AS I
  WHERE I.SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수');
  ```

- 트리거 내에서는 가능한한 복잡한 동적 테이블 생성이나 다중 타입 혼합 표현은 피하고, 명확하게 타입을 맞춘 쿼리 구성을 권장합니다.

- 만약 여전히 문제 발생 시, 트리거 내부에서 임시테이블 생성 대신 일반 `DECLARE` 절의 테이블 변수 사용이나, 별도 저장 프로시저 호출 등 다른 방법을 고려하는 것도 방법입니다.

***

### 요약

- 트리거 내부 `SELECT ... INTO`에서 `CASE` 문을 사용할 때는 반환 타입이 모두 동일해야 함.
- 타입 불일치나 명확하지 않은 컬럼 타입 추론 때문에 문법 오류가 발생.
- `CAST` 또는 `CONVERT`로 명시적 타입 변환을 하여 일치시키면 해결 가능.
- 트리거 내 복잡한 동적 테이블 생성은 주의가 필요함.

필요하면 쿼리에서 `CASE` 부분을 타입 맞춤 예제로 보여줄게요!

[1] https://learn.microsoft.com/ko-kr/sql/relational-databases/errors-events/database-engine-events-and-errors-0-to-999?view=sql-server-ver17
[2] https://harui.tistory.com/132
[3] https://sseambong.tistory.com/210
[4] https://learn.microsoft.com/ko-kr/sql/relational-databases/errors-events/database-engine-events-and-errors-2000-to-2999?view=sql-server-ver17
[5] https://velog.io/@dankj1991/RDB-SQL-logic-CASE-FUNCTION-PROCEDURE-TRIGGER
[6] https://junjunrecord.tistory.com/49
[7] https://learn.microsoft.com/ko-kr/sql/t-sql/statements/disable-trigger-transact-sql?view=sql-server-ver16
[8] https://seodavi.tistory.com/65
[9] https://kimjh0727.tistory.com/entry/MSSQLSELECT-INTO-%EA%B5%AC%EB%AC%B8
[10] https://aurumguide.tistory.com/67
[11] https://developerking.tistory.com/29
[12] https://learn.microsoft.com/ko-kr/sql/t-sql/queries/select-into-clause-transact-sql?view=sql-server-ver17
[13] https://sosopro.tistory.com/293
[14] https://im-first-rate.tistory.com/61
[15] https://gameserverengineer-k.tistory.com/18
[16] https://support.microsoft.com/ko-kr/topic/kb2972996-fix-sql-server-2012%EC%97%90%EC%84%9C-%EA%B8%B4-case-%EB%AC%B8%EC%9D%B4-%ED%8F%AC%ED%95%A8-%EB%90%9C-transact-sql-%EC%BF%BC%EB%A6%AC%EB%A5%BC-%EC%8B%A4%ED%96%89-%ED%95%98%EB%8A%94-%EB%8F%99%EC%95%88-%EC%98%A4%EB%A5%98%EA%B0%80-%EB%B0%9C%EC%83%9D-%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4-3b98f46c-31a8-0e14-6946-c3abf0b63c37
[17] https://halizy.tistory.com/11
[18] https://junshock5.tistory.com/33
[19] https://gent.tistory.com/598
[20] https://learn.microsoft.com/ko-kr/sql/t-sql/queries/output-clause-transact-sql?view=sql-server-ver17


---------------------------------------------------------------------


# 위에서 알려준 대로 cast를 써서 cast(12 as int)등으로 수정해도 오류가 나는데

---------------------------------------------------------------------


트리거 안에서 `SELECT ... INTO` 구문에 `CASE` 문을 쓰고, 거기서 `CAST(12 AS INT)`처럼 명시적으로 타입을 맞춰도 오류가 난다면, 다음 사항들을 순서대로 점검해보는 게 좋아요.

***

## 1. 에러 메시지를 정확히 확인하기

- 발생하는 문법 오류나 실행 오류 메시지를 정확히 알려주면 원인 파악이 더 쉬워요.
- 오류 메시지에 `메타데이터`, `묵시적 변환 실패`, `열 이름 중복` 같은 내용이 있으면 그것도 참고해 주세요.

***

## 2. `SELECT ... INTO` 의 대상 컬럼 타입 및 중복 여부 확인

- `SELECT *` 와 함께 `CASE` 문 결과 컬럼을 추가할 경우, `inserted`에 같은 이름의 컬럼이 있는지 확인하세요.
- 예를 들어, `ORDER_NO` 컬럼이 이미 있으면 중복 에러가 납니다.
- 중복 컬럼명 있으면 새 컬럼명을 다른 이름으로 바꾸세요.

***

## 3. 임시 테이블이나 컬럼에 넣는 데이터 타입 명확히 지정

- `CASE` 결과를 `INT`로 캐스팅 했다면, `THEN`과 `ELSE` 부분 모두 꼭 `INT`형이어야 합니다.
- 숫자처럼 보이지만 타입이 달라서 문제 생기는 경우도 있으니 아래 예시처럼 다시 한번 확실히 해보세요.

```sql
SELECT
    I.*,
    CASE
        WHEN 12 > 0 THEN CAST(12 AS INT)
        ELSE CAST(13 AS INT)
    END AS ORDER_NO_INT
INTO #ins
FROM inserted AS I
WHERE I.SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수');
```

***

## 4. 트리거 내에서 임시 테이블이 아니라 테이블 변수를 써보기

- 임시테이블 생성에 문제가 계속되면, 트리거 안에서는 테이블 변수를 써서 같은 작업을 해보는 방법도 있어요.

```sql
DECLARE @ins TABLE (
    -- inserted 테이블 컬럼 정의 (필요 컬럼만 정확히 명시)
    컬럼1 데이터타입,
    컬럼2 데이터타입,
    ORDER_NO INT
);

INSERT INTO @ins
SELECT
    I.컬럼1,
    I.컬럼2,
    CASE WHEN 12 > 0 THEN 12 ELSE 13 END AS ORDER_NO
FROM inserted AS I
WHERE I.SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수');
```

***

## 5. 그 밖에 확인할 점

- `inserted` 테이블의 구조가 복잡하거나 특수한 컬럼이 있는 경우 (예: XML, 이미지 등) `SELECT *`가 문제일 수 있어요.
- 가능하다면 `SELECT *` 대신 필요한 컬럼만 명시적으로 지정하는 게 좋습니다.

***

### 요약 체크리스트

- [ ] 오류 메시지 정확히 확인 및 공유
- [ ] `ORDER_NO` 같은 컬럼명 중복 여부 점검
- [ ] `CASE` 문 내 `THEN`과 `ELSE` 모두 같은 타입으로 정확히 캐스팅
- [ ] 임시테이블 대신 테이블 변수로 대체 시도
- [ ] `SELECT *` 대신 컬럼 명시적으로 지정

***

필요하면 오류 메시지와 현재 수정한 쿼리 전체 내용을 같이 보여주세요. 좀 더 구체적으로 도움 드릴게요!

---------------------------------------------------------------------

# 오류 메시지는 Incorrect syntax near ';'  라고 나와

---------------------------------------------------------------------

트리거 안에 쓴 `SELECT ... INTO` 구문에서 `CASE` 문과 콤마(,) 또는 세미콜론(;) 때문에 `Incorrect syntax near ';'`라는 오류가 나올 수 있어. 이 오류는 사실 `CASE` 문 때문이 아니라, 쿼리에서 구문 구조가 약간 잘못됐을 때 자주 발생해. 아래에서 원인과 해결 방법을 단계별로 정리해 줄게!

***

## 1. 체크리스트: 세미콜론(;) 위치

- MSSQL에서 세미콜론은 보통 명령문 끝에만 쓰고, 쿼리 중간(column list, FROM 뒤 등)에 쓰면 문법 오류가 발생함.
- 예를 들어, 아래처럼 SELECT 컬럼 나열 중이나, FROM 옆에 세미콜론이 들어가면 오류가 발생한다.

```sql
SELECT 컬럼1, 컬럼2;    -- ❌ 이렇게 쓰면 안됨!
FROM 테이블명;
```

***

## 2. 올바른 쿼리 구조 예시

정상적으로 동작하는 코드는 아래처럼 세미콜론 없이 쓴다(명령문 끝에서만 사용):

```sql
SELECT
    I.*,
    CASE
        WHEN 12 > 0 THEN CAST(12 AS INT)
        ELSE CAST(13 AS INT)
    END AS ORDER_NO
INTO #ins
FROM inserted AS I
WHERE I.SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수');
```

> ⚡ **중요:** SELECT 절(컬럼 나열)에서 절대 세미콜론(;)을 쓰지 않고, FROM 전에 콤마(,)도 빠뜨리면 안 됨!

***

## 3. 실수하기 쉬운 포인트

- CASE 문 자체에는 문제가 없음. 오히려 실수하기 쉬운 부분은 SELECT 절 콤마(,) 누락이거나, 불필요한 세미콜론(;) 삽입이야.
- `I.*` 다음에 콤마가 꼭 있어야 하고, 마지막 컬럼 뒤에는 세미콜론 없이 FROM부터 이어져야 해.

***

## 4. 트리거 내부 전체 예제

```sql
ALTER TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    IF OBJECT_ID('tempdb..#ins') IS NOT NULL
        DROP TABLE #ins;

    SELECT
        I.*,
        CASE
            WHEN 12 > 0 THEN CAST(12 AS INT)
            ELSE CAST(13 AS INT)
        END AS ORDER_NO
    INTO #ins
    FROM inserted AS I
    WHERE I.SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수');
END
GO
```

***

## 5. 빠르게 점검해보기

- SELECT 컬럼 리스트에서 세미콜론(;)이 들어가 있지 않은지 확인
- 혹시 SELECT 뒤에 쓸데없는 세미콜론, FROM 앞뒤에 쓸데없는 세미콜론이 있는지 확인
- 콤마(,) 누락, 컬럼명 오타 확인

***

## 6. 만약 그대로 해도 안 될 때

- 에디터에서 코드 포매팅을 다시 하고, SELECT~FROM 줄의 모든 세미콜론을 삭제해봐.
- 오류가 나는 줄 번호를 함께 확인하면 더 빠르게 원인을 찾을 수 있어.

***

### 친구처럼 정리

이 오류는 대부분 쿼리 라인 중간에 세미콜론(;)이 들어가 있을 때 생겨. SELECT 컬럼을 나열할 땐 콤마만! 세미콜론은 명령문 맨 끝에만 딱 한 번! 쿼리 복사해서 붙일 때 무심코 붙은 ; 하나가 이런 에러의 주범이야.

혹시 코드 전체를 붙여줬는데도 계속 오류가 난다면, 쿼리 전체(트리거 전체 코드)를 공유해줘! 추가로 점검해서 딱 집어줄게.

---------------------------------------------------------------------


