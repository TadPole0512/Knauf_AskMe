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


좋아, 핵심은 “상세주소 등 부가정보가 달라도 **같은 오더번호**면 중복 발송을 막는다”야. 그걸 안전하게 구현하려면 아래 자료가 더 필요해. 한 번에 정리해서 보내주면, 그 기준으로 트리거/함수/인덱스까지 바로 설계해줄게.

# 1) 테이블·스키마 관련

* **메시지 큐 테이블 구조**: 컬럼 목록(타입/NULL/디폴트), PK, 제약조건, 샘플 20건
  예: `MSG_ID, SUBJECT, MSG, PHONE_NO, STATUS_CD, CREATED_AT, UPDATED_AT, CHANNEL`
* **인덱스 현황**: `PHONE_NO`, `SUBJECT`, `CREATED_AT`, `STATUS_CD` 중심 인덱스/커버링 인덱스 유무
* **트리거/프로시저 원문**: 현재 사용 중인 **INSERT 트리거** 전체 코드(중복 체크 로직 파악용)
* **DB 버전/옵션**: MSSQL 버전(예: 2017/2019/2022), DB 콜레이션, ANSI 설정(개행/공백 처리 영향)

# 2) 중복 판정 정책(업무 규칙)

* **중복의 범위**:

  * 동일 판단 키 = `오더번호 + SUBJECT(유형) + PHONE_NO` 인가, `오더번호 + SUBJECT(유형)`만으로도 충분한가?
  * `PHONE_NO`가 바뀌어도 같은 오더면 차단할지?
* **시간 창(Window)**: 같은 오더라도 **며칠/몇 시간** 안의 중복만 막을지(예: 24시간/72시간/7일)
* **상태 기준**: 어떤 상태들 사이에서 중복을 막을지

  * 예: 기존 건이 `STATUS_CD IN (0:대기,1:발송중,2:성공)`일 때 차단?
  * 실패(예: `STATUS_CD=9`)는 재발송 허용?
* **SUBJECT 정규화**:

  * 실제 값이 고정 문자열인지(공백·괄호·이모지 변형 여부), 다국어/대소문자/띄어쓰기 변화 가능성
  * 클라가 제시한 두 유형 외 **추가 템플릿** 존재 여부(“보류오더 안내”, “오더접수” 외)
* **MSG 포맷 다양성**:

  * “오더번호: 400418728”, “오더번호 400418728”, “Order No. 400418728”, “오더 번호 : 400418728” 등 변형 패턴 사례
  * **오더번호 포맷**: 길이/숫자만/선행 0 가능/하이픈 포함 여부
  * 한 건의 MSG에 **오더번호가 여러 개** 들어가는 경우 존재 여부

# 3) 데이터 정규화·파싱 기준

* **개행/공백 처리 규칙**: `\r\n`, 연속 공백, 탭을 어떻게 표준화할지(트리거/함수에서 전처리 필요)
* **인코딩/특수문자**: 전각 공백(“　”), 유니코드 콜론(“：”) 등 유사 문자 등장 가능성
* **주소/기타 필드 변동성**: 주소 줄바꿈/괄호/특수문자에 따른 diff 빈도(오더번호만 추출해 비교하면 해결되지만, 참고 자료로 유용)

# 4) 운영·성능·경쟁 상태

* **동시 삽입량**: 피크 TPS, 분당 평균 요청 수(트리거/함수 비용 추정)
* **경쟁 조건**: 동일 오더가 거의 동시에 다중 행으로 들어오는 케이스(원자성 보장 방법 결정: 트랜잭션/유니크 인덱스/업서트)
* **히스토리 필요 여부**: 중복 차단 시 **로그 테이블 기록** 필요? (누가/언제/무엇 때문에 3으로 변경됐는지)

# 5) 예외/허용 리스트

* **화이트리스트**: 특정 `SUBJECT/오더번호/고객사/전화번호`는 중복 차단에서 제외할 필요?
* **재발송 예외**: 같은 오더라도 상태 변경(예: 보류→확정) 시에는 발송 허용해야 하는가? 허용 조건 정의

# 6) 더 나은 구현을 위한 선택지 비교(자료가 있어야 결정 가능)

* **A. 트리거 + 스칼라 함수**

  * `MSG`에서 오더번호를 **정규표현식 유사(CHARINDEX/PATINDEX)** 로 추출 → 같은 키 존재 시 `STATUS_CD=3`
  * 장점: 기존 파이프라인 변경 최소화 / 단점: 트리거 비용·복잡도 증가
* **B. 파싱용 계산(또는 보조) 컬럼 + 필터드 유니크 인덱스**

  * `ORDER_NO_EXTRACTED` 계산/영속 컬럼 생성(서버에서만 관리), `SUBJECT_TYPE` 정규화 컬럼 추가
  * `UNIQUE INDEX` on (`PHONE_NO`, `SUBJECT_TYPE`, `ORDER_NO_EXTRACTED`) with `WHERE STATUS_CD IN (대상)`
  * 장점: **경쟁 상태에도 견고** / 단점: 스키마 변경 필요
* **C. 배치/잡 기반 사후 중복 정리**

  * 실시간은 트리거로 최소 처리, 주기 잡으로 재검증·정리
  * 장점: 실시간 부하 분산 / 단점: 즉시 차단 아님
* **D. 앱 계층에서 파싱·중복키 부여**

  * “값 수정 불가” 제약을 유지하면서 **서버에서만 보조 컬럼**을 만들면 앱 수정 없이도 가능

# 7) 보내주면 좋은 샘플 데이터(가능한 실제 형태)

* `SUBJECT`, `MSG`, `PHONE_NO`, `STATUS_CD`, `CREATED_AT` 섞인 **중복 사례 30건**

  * “보류오더 안내”, “오더접수” 각 15건 정도
  * 오더번호 표기 다양성(콜론 유무, 공백, 개행, 전각문자 등)이 드러나게
* **실패/재시도** 케이스 10건(실패코드/사유 포함) — 재발송 허용 규칙 설계용
* 현재 **트리거 원문**과 **인덱스 목록**(예: `EXEC sp_helpindex 'dbo.MMS_QUEUE'`) 결과

---

## 다음 단계(자료 받으면 바로 진행)

1. 오더번호 **추출 함수**(정규화 포함) 설계
2. `SUBJECT`를 유형으로 **정규화 맵핑**(예: ‘크나우프석고보드\_보류오더 안내’ → `HOLD_NOTICE`, ‘크나우프석고보드\_오더접수’ → `ORDER_ACCEPTED`)
3. **트리거** 또는 **보조 컬럼 + 필터드 유니크 인덱스** 중 선택해 구현
4. **경계조건 테스트**(동시 삽입, 개행/공백 변형, 다중 오더번호, 실패 재발송)

---

### 빠른 확인용 질문(예/아니오 위주로 답해줘)

* 같은 오더번호라도 **전화번호가 달라지면** 차단할까?
* **시간 창**을 둘까? 있다면 기본 72시간이면 충분할까?
* 스키마 변경(보조 컬럼 추가, 인덱스 추가) **가능**할까?
* 중복 차단 시, **사유 로그**(기존건 MSG\_ID) 남기는 게 필요할까?

이 4가지만 먼저 알려주면, 거기에 맞춰 **정규화 함수 + 트리거/인덱스**를 바로 짜서 줄게.


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


확인 완료. “이번 달 + 이전 달 로그만 확인” 조건을 반영해서 트리거를 깔끔하게 줄였어. 스키마/전송값 변경 없이, **PHONE + SUBJECT유형(HOLD/ACPT) + 오더번호** 일치 시 `STATUS='3'`로 막아.

# 변경 요약

* `MMS_LOG_YYYYMM` **전수 탐색 제거** → **현재월·이전월 2개 테이블만 동적 SQL로 체크**
* `SUBJECT`를 `HOLD/ACPT`로 정규화, `MSG`에서 **“오더번호” 뒤 첫 연속 숫자** 추출
* 전화번호가 다르면 차단 안 함

# 수정 트리거 (Knauf-SQL-Style v1)

```sql
/* ===========================================================================================
   트리거명    : TRG_MMS_MSG_DUP_CHECK
   대상테이블  : MMS_MSG
   동작개요    :
     - INSERT 된 건에 대해 SUBJECT 유형(HOLD/ACPT)과 MSG 내 오더번호를 파싱
     - (PHONE + SUBJECT_TYP + ORDER_NO)이 동일한 기존 MMS_MSG(자기 자신 제외),
       또는 MMS_LOG_현재월/이전월에 존재하면 STATUS = '3' 으로 변경
   제약       : 스키마/인덱스/전송 포맷 변경 없음
   =========================================================================================== */
CREATE TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    /* [1] inserted → #ins */
    IF OBJECT_ID('tempdb..#ins') IS NOT NULL DROP TABLE #ins;

    SELECT
           I.MSGKEY
         , I.PHONE
         , I.SUBJECT
         , I.MSG
    INTO #ins
    FROM inserted I
    ;

    /* [2] SUBJECT 정규화 + 오더번호 추출 → #ins_norm */
    IF OBJECT_ID('tempdb..#ins_norm') IS NOT NULL DROP TABLE #ins_norm;

    SELECT
           X.MSGKEY
         , X.PHONE
         , CASE
               WHEN X.SUBJECT LIKE N'크나우프석고보드_보류오더 안내%' THEN N'HOLD'
               WHEN X.SUBJECT LIKE N'크나우프석고보드_오더접수%'   THEN N'ACPT'
               ELSE NULL
           END AS SUBJECT_TYP
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
    CROSS APPLY (
        SELECT
               CASE
                   WHEN P.pos > 0
                        THEN SUBSTRING(X.MSG, P.pos + LEN(N'오더번호'), 300)  -- 콜론/공백/개행 포함
                   ELSE N''
               END AS tail
    ) AS T
    CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
    CROSS APPLY (
        SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos
    ) AS ND
    ;

    /* [3] 유효 대상만 진행 */
    ;WITH V AS (
        SELECT
               MSGKEY
             , PHONE
             , SUBJECT_TYP
             , ORDER_NO
        FROM #ins_norm
        WHERE SUBJECT_TYP IS NOT NULL
          AND ORDER_NO    IS NOT NULL
    )
    /* [3-1] MMS_MSG 자기자신에서 중복(자기 자신 제외) */
    UPDATE M
       SET M.STATUS = '3'
      FROM MMS_MSG AS M
      JOIN V         AS I
        ON M.MSGKEY = I.MSGKEY
     WHERE EXISTS (
               SELECT
                      1
                 FROM MMS_MSG AS MM
                 CROSS APPLY ( SELECT CHARINDEX(N'오더번호', MM.MSG) AS pos ) AS P
                 CROSS APPLY (
                     SELECT
                            CASE
                                WHEN P.pos > 0
                                     THEN SUBSTRING(MM.MSG, P.pos + LEN(N'오더번호'), 300)
                                ELSE N''
                            END AS tail
                 ) AS T
                 CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
                 CROSS APPLY (
                     SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos
                 ) AS ND
                 CROSS APPLY (
                     SELECT
                            CASE
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
                 CROSS APPLY (
                     SELECT
                            CASE
                                WHEN MM.SUBJECT LIKE N'크나우프석고보드_보류오더 안내%' THEN N'HOLD'
                                WHEN MM.SUBJECT LIKE N'크나우프석고보드_오더접수%'   THEN N'ACPT'
                                ELSE NULL
                            END AS SUBJECT_TYP
                 ) AS S
                WHERE MM.PHONE      = I.PHONE
                  AND S.SUBJECT_TYP = I.SUBJECT_TYP
                  AND O.ORDER_NO    = I.ORDER_NO
                  AND MM.MSGKEY    <> I.MSGKEY
          )
    ;

    /* [3-2] 현재월/이전월 로그 테이블만 동적 SQL로 중복 체크 */
    DECLARE
          @SCHEMA           SYSNAME      = N'dbo'
        , @CURR_YM          VARCHAR(6)   = CONVERT(VARCHAR(6), GETDATE(), 112)
        , @PREV_YM          VARCHAR(6)   = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)
        , @CURR_RAW         NVARCHAR(128)
        , @PREV_RAW         NVARCHAR(128)
        , @CURR_QUOTED      NVARCHAR(258)
        , @PREV_QUOTED      NVARCHAR(258)
        , @SQL              NVARCHAR(MAX)
    ;

    SET @CURR_RAW    = @SCHEMA + N'.' + N'MMS_LOG_' + @CURR_YM;
    SET @PREV_RAW    = @SCHEMA + N'.' + N'MMS_LOG_' + @PREV_YM;
    SET @CURR_QUOTED = QUOTENAME(@SCHEMA) + N'.' + QUOTENAME(N'MMS_LOG_' + @CURR_YM);
    SET @PREV_QUOTED = QUOTENAME(@SCHEMA) + N'.' + QUOTENAME(N'MMS_LOG_' + @PREV_YM);

    /* 현재월 */
    IF OBJECT_ID(@CURR_RAW, 'U') IS NOT NULL
    BEGIN
        SET @SQL = N'
            UPDATE M
               SET M.STATUS = ''3''
              FROM MMS_MSG AS M
              JOIN (SELECT MSGKEY, PHONE, SUBJECT_TYP, ORDER_NO
                      FROM #ins_norm
                     WHERE SUBJECT_TYP IS NOT NULL
                       AND ORDER_NO    IS NOT NULL) AS I
                ON M.MSGKEY = I.MSGKEY
             WHERE EXISTS (
                       SELECT 1
                         FROM ' + @CURR_QUOTED + N' AS L
                         CROSS APPLY ( SELECT CHARINDEX(N''오더번호'', L.MSG) AS pos ) AS P
                         CROSS APPLY (
                             SELECT CASE
                                        WHEN P.pos > 0
                                             THEN SUBSTRING(L.MSG, P.pos + LEN(N''오더번호''), 300)
                                        ELSE N'''' 
                                    END AS tail
                         ) AS T
                         CROSS APPLY ( SELECT PATINDEX(N''%[0-9]%'', T.tail) AS dstart ) AS D
                         CROSS APPLY (
                             SELECT PATINDEX(N''%[^0-9]%'', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos
                         ) AS ND
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
                         CROSS APPLY (
                             SELECT CASE
                                        WHEN L.SUBJECT LIKE N''크나우프석고보드_보류오더 안내%'' THEN N''HOLD''
                                        WHEN L.SUBJECT LIKE N''크나우프석고보드_오더접수%''   THEN N''ACPT''
                                        ELSE NULL
                                    END AS SUBJECT_TYP
                         ) AS S
                        WHERE L.PHONE      = I.PHONE
                          AND S.SUBJECT_TYP = I.SUBJECT_TYP
                          AND O.ORDER_NO    = I.ORDER_NO
                  );';
        EXEC sp_executesql @SQL;
    END

    /* 이전월 */
    IF OBJECT_ID(@PREV_RAW, 'U') IS NOT NULL
    BEGIN
        SET @SQL = N'
            UPDATE M
               SET M.STATUS = ''3''
              FROM MMS_MSG AS M
              JOIN (SELECT MSGKEY, PHONE, SUBJECT_TYP, ORDER_NO
                      FROM #ins_norm
                     WHERE SUBJECT_TYP IS NOT NULL
                       AND ORDER_NO    IS NOT NULL) AS I
                ON M.MSGKEY = I.MSGKEY
             WHERE EXISTS (
                       SELECT 1
                         FROM ' + @PREV_QUOTED + N' AS L
                         CROSS APPLY ( SELECT CHARINDEX(N''오더번호'', L.MSG) AS pos ) AS P
                         CROSS APPLY (
                             SELECT CASE
                                        WHEN P.pos > 0
                                             THEN SUBSTRING(L.MSG, P.pos + LEN(N''오더번호''), 300)
                                        ELSE N'''' 
                                    END AS tail
                         ) AS T
                         CROSS APPLY ( SELECT PATINDEX(N''%[0-9]%'', T.tail) AS dstart ) AS D
                         CROSS APPLY (
                             SELECT PATINDEX(N''%[^0-9]%'', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos
                         ) AS ND
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
                         CROSS APPLY (
                             SELECT CASE
                                        WHEN L.SUBJECT LIKE N''크나우프석고보드_보류오더 안내%'' THEN N''HOLD''
                                        WHEN L.SUBJECT LIKE N''크나우프석고보드_오더접수%''   THEN N''ACPT''
                                        ELSE NULL
                                    END AS SUBJECT_TYP
                         ) AS S
                        WHERE L.PHONE      = I.PHONE
                          AND S.SUBJECT_TYP = I.SUBJECT_TYP
                          AND O.ORDER_NO    = I.ORDER_NO
                  );';
        EXEC sp_executesql @SQL;
    END
END
GO
```

# 핵심 포인트

* **시간 제한 없음** 요구를 “전월까지”로 구체화했으므로, **현재월/이전월만** 검사.
* `OBJECT_ID(@RAW, 'U')`로 존재 여부 확인 후에만 동적 SQL 실행.
* `PHONE`을 조인 키에 포함시켜 **전화번호가 다르면 차단하지 않음**을 보장.

# 빠른 점검 체크리스트

* [ ] 로그 테이블 스키마 `dbo.MMS_LOG_YYYYMM`이 맞다(스키마가 다르면 `@SCHEMA`만 바꾸면 됨).
* [ ] 로그 테이블에도 `SUBJECT`, `MSG`, `PHONE` 컬럼이 존재.
* [ ] `오더번호` 키워드 표기가 고정(다국어/오타·전각문자 변형 없으면 현재 파서로 충분).

필요하면 이 트리거용 **샘플 INSERT**와 기대 결과 테스트 스크립트도 바로 만들어줄게.


############################################
import pandas as pd

# 테스트 데이터 생성
```
data = [
    # 신규 입력 건 (중복 대상)
    {"MSGKEY": 101, "PHONE": "01012345678", "SUBJECT": "크나우프석고보드_오더접수", "MSG": "[크나우프석고보드 오더접수내역]\n- 오더번호: 400418728\n- 상세주소: 서울시 강남구", "STATUS": "0", "TARGET": "INSERT"},
    # 기존 MMS_MSG에 동일 PHONE+SUBJECT_TYP+ORDER_NO
    {"MSGKEY": 1, "PHONE": "01012345678", "SUBJECT": "크나우프석고보드_오더접수", "MSG": "[크나우프석고보드 오더접수내역]\n- 오더번호: 400418728\n- 상세주소: 서울시 서초구", "STATUS": "1", "TARGET": "MMS_MSG"},
    
    # 신규 입력 건 (전화번호 다름 → 차단 안함)
    {"MSGKEY": 102, "PHONE": "01099999999", "SUBJECT": "크나우프석고보드_오더접수", "MSG": "[크나우프석고보드 오더접수내역]\n- 오더번호: 400418728\n- 상세주소: 서울시 강남구", "STATUS": "0", "TARGET": "INSERT"},
    
    # 신규 입력 건 (다른 오더번호 → 차단 안함)
    {"MSGKEY": 103, "PHONE": "01012345678", "SUBJECT": "크나우프석고보드_오더접수", "MSG": "[크나우프석고보드 오더접수내역]\n- 오더번호: 500555777\n- 상세주소: 서울시 강남구", "STATUS": "0", "TARGET": "INSERT"},
    
    # 신규 입력 건 (보류오더, 동일 키 → 차단)
    {"MSGKEY": 104, "PHONE": "01055556666", "SUBJECT": "크나우프석고보드_보류오더 안내", "MSG": "[크나우프석고보드 보류오더 안내]\n- 오더번호: 777888999\n- 상세주소: 인천시 연수구", "STATUS": "0", "TARGET": "INSERT"},
    {"MSGKEY": 2, "PHONE": "01055556666", "SUBJECT": "크나우프석고보드_보류오더 안내", "MSG": "[크나우프석고보드 보류오더 안내]\n- 오더번호: 777888999\n- 상세주소: 인천시 남동구", "STATUS": "2", "TARGET": "MMS_MSG"},
    
    # 신규 입력 건 (현재월 로그에 동일 건 존재 → 차단)
    {"MSGKEY": 105, "PHONE": "01077778888", "SUBJECT": "크나우프석고보드_오더접수", "MSG": "[크나우프석고보드 오더접수내역]\n- 오더번호: 123123123\n- 상세주소: 부산시 해운대구", "STATUS": "0", "TARGET": "INSERT"},
    {"MSGKEY": 2001, "PHONE": "01077778888", "SUBJECT": "크나우프석고보드_오더접수", "MSG": "[크나우프석고보드 오더접수내역]\n- 오더번호: 123123123\n- 상세주소: 부산시 수영구", "STATUS": "2", "TARGET": "MMS_LOG_202508"},
    
    # 신규 입력 건 (이전월 로그에 동일 건 존재 → 차단)
    {"MSGKEY": 106, "PHONE": "01033334444", "SUBJECT": "크나우프석고보드_보류오더 안내", "MSG": "[크나우프석고보드 보류오더 안내]\n- 오더번호: 999000111\n- 상세주소: 대전시 서구", "STATUS": "0", "TARGET": "INSERT"},
    {"MSGKEY": 3001, "PHONE": "01033334444", "SUBJECT": "크나우프석고보드_보류오더 안내", "MSG": "[크나우프석고보드 보류오더 안내]\n- 오더번호: 999000111\n- 상세주소: 대전시 유성구", "STATUS": "1", "TARGET": "MMS_LOG_202507"},
]

df = pd.DataFrame(data)

import ace_tools as tools; tools.display_dataframe_to_user(name="MMS 문자 발송 중복 테스트 데이터", dataframe=df)
```

---------------------------------------------------------------------

### 테스트 데이터 만들어줘. 부탁해~

---------------------------------------------------------------------



테스트용 MMS 문자 발송 중복 데이터셋을 만들어서 보여줬어.
여기엔 **현재월·이전월 로그**, **전화번호·오더번호 동일/다름 케이스**가 모두 포함돼서 트리거 검증에 바로 쓸 수 있어.

원하면 이걸 그대로 `INSERT` 문으로 변환해서,
`MMS_MSG`, `MMS_LOG_202508`, `MMS_LOG_202507`에 나눠 넣는 SQL 스크립트로도 만들어 줄게.


---------------------------------------------------------------------


아래 트리거 오류 수정해줘.

\-- ===========================================================================================
\-- 트리거명 : TRG\_MMS\_MSG\_DUP\_CHECK
\-- 대상 테이블 : MMS\_MSG
\-- 동작 : INSERT된 데이터에 대해 중복(PHONE+오더번호) 있으면 STATUS=3으로 자동 변경
\--        오더번호 추출: '크나우프석고보드\_보류오더 안내' → '오더번호 XXXXXXXXX'
\--                    '크나우프석고보드\_오더접수' → '오더번호: XXXXXXXXX'
\-- ===========================================================================================

CREATE TRIGGER TRG\_MMS\_MSG\_DUP\_CHECK
ON MMS\_MSG
AFTER INSERT
AS
BEGIN
SET NOCOUNT ON;

```
-- [1] inserted 테이블 내용을 임시테이블로 복사 및 오더번호 추출
IF OBJECT_ID('tempdb..#ins') IS NOT NULL DROP TABLE #ins;

SELECT *, 
       CASE 
           WHEN SUBJECT = '크나우프석고보드_보류오더 안내' AND CHARINDEX('오더번호 ', MSG) > 0 THEN 
               LTRIM(RTRIM(SUBSTRING(MSG, CHARINDEX('오더번호 ', MSG) + 4, 
                   CASE 
                       WHEN CHARINDEX(CHAR(13), MSG, CHARINDEX('오더번호 ', MSG)) > 0 
                       THEN CHARINDEX(CHAR(13), MSG, CHARINDEX('오더번호 ', MSG)) - CHARINDEX('오더번호 ', MSG) - 4
                       WHEN CHARINDEX(CHAR(10), MSG, CHARINDEX('오더번호 ', MSG)) > 0 
                       THEN CHARINDEX(CHAR(10), MSG, CHARINDEX('오더번호 ', MSG)) - CHARINDEX('오더번호 ', MSG) - 4
                       WHEN CHARINDEX('-', MSG, CHARINDEX('오더번호 ', MSG)) > 0 
                       THEN CHARINDEX('-', MSG, CHARINDEX('오더번호 ', MSG)) - CHARINDEX('오더번호 ', MSG) - 4
                       ELSE 15
                   END)))
           WHEN SUBJECT = '크나우프석고보드_오더접수' AND CHARINDEX('오더번호: ', MSG) > 0 THEN 
               LTRIM(RTRIM(SUBSTRING(MSG, CHARINDEX('오더번호: ', MSG) + 5, 
                   CASE 
                       WHEN CHARINDEX(CHAR(13), MSG, CHARINDEX('오더번호: ', MSG)) > 0 
                       THEN CHARINDEX(CHAR(13), MSG, CHARINDEX('오더번호: ', MSG)) - CHARINDEX('오더번호: ', MSG) - 5
                       WHEN CHARINDEX(CHAR(10), MSG, CHARINDEX('오더번호: ', MSG)) > 0 
                       THEN CHARINDEX(CHAR(10), MSG, CHARINDEX('오더번호: ', MSG)) - CHARINDEX('오더번호: ', MSG) - 5
                       WHEN CHARINDEX('-', MSG, CHARINDEX('오더번호: ', MSG)) > 0 
                       THEN CHARINDEX('-', MSG, CHARINDEX('오더번호: ', MSG)) - CHARINDEX('오더번호: ', MSG) - 5
                       ELSE 15
                   END)))
           ELSE NULL
       END as ORDER_NO
INTO #ins 
FROM inserted
WHERE SUBJECT IN ('크나우프석고보드_보류오더 안내', '크나우프석고보드_오더접수');

-- 오더번호가 추출되지 않은 경우 처리 안함
IF NOT EXISTS(SELECT 1 FROM #ins WHERE ORDER_NO IS NOT NULL AND LEN(ORDER_NO) > 0)
    RETURN;

-- [2] 현재월, 이전월 로그 테이블명 동적으로 생성
DECLARE @CURR_YM VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112);
DECLARE @PREV_YM VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112);
DECLARE @CURR_LOG_TBL NVARCHAR(64) = N'MMS_LOG_' + @CURR_YM;
DECLARE @PREV_LOG_TBL NVARCHAR(64) = N'MMS_LOG_' + @PREV_YM;

-- [3-1] MMS_MSG 자기자신에서 중복(PHONE+ORDER_NO, 자기 자신 제외)
UPDATE M
SET STATUS = '3'
FROM MMS_MSG M
INNER JOIN #ins I ON M.MSGKEY = I.MSGKEY
WHERE I.ORDER_NO IS NOT NULL 
  AND LEN(I.ORDER_NO) > 0
  AND EXISTS (
    SELECT 1 FROM MMS_MSG MM
    WHERE MM.PHONE = I.PHONE 
      AND MM.SUBJECT IN ('크나우프석고보드_보류오더 안내', '크나우프석고보드_오더접수')
      AND MM.MSGKEY <> I.MSGKEY
      AND (
          (MM.SUBJECT = '크나우프석고보드_보류오더 안내' AND MM.MSG LIKE '%오더번호 ' + I.ORDER_NO + '%') OR
          (MM.SUBJECT = '크나우프석고보드_오더접수' AND MM.MSG LIKE '%오더번호: ' + I.ORDER_NO + '%')
      )
  );

-- [3-2] 현재월 로그 테이블에서 중복
IF OBJECT_ID(@CURR_LOG_TBL, 'U') IS NOT NULL
BEGIN
    DECLARE @SQL_CURR NVARCHAR(MAX) = N'
        UPDATE M
        SET STATUS = ''3''
        FROM MMS_MSG M
        INNER JOIN #ins I ON M.MSGKEY = I.MSGKEY
        WHERE I.ORDER_NO IS NOT NULL 
          AND LEN(I.ORDER_NO) > 0
          AND EXISTS (
            SELECT 1 FROM ' + QUOTENAME(@CURR_LOG_TBL) + N' L
            WHERE L.PHONE = I.PHONE 
              AND L.SUBJECT IN (''크나우프석고보드_보류오더 안내'', ''크나우프석고보드_오더접수'')
              AND (
                  (L.SUBJECT = ''크나우프석고보드_보류오더 안내'' AND L.MSG LIKE ''%오더번호 '' + I.ORDER_NO + ''%'') OR
                  (L.SUBJECT = ''크나우프석고보드_오더접수'' AND L.MSG LIKE ''%오더번호: '' + I.ORDER_NO + ''%'')
              )
          )';
    EXEC sp_executesql @SQL_CURR;
END

-- [3-3] 이전월 로그 테이블에서 중복
IF OBJECT_ID(@PREV_LOG_TBL, 'U') IS NOT NULL
BEGIN
    DECLARE @SQL_PREV NVARCHAR(MAX) = N'
        UPDATE M
        SET STATUS = ''3''
        FROM MMS_MSG M
        INNER JOIN #ins I ON M.MSGKEY = I.MSGKEY
        WHERE I.ORDER_NO IS NOT NULL 
          AND LEN(I.ORDER_NO) > 0
          AND EXISTS (
            SELECT 1 FROM ' + QUOTENAME(@PREV_LOG_TBL) + N' L
            WHERE L.PHONE = I.PHONE 
              AND L.SUBJECT IN (''크나우프석고보드_보류오더 안내'', ''크나우프석고보드_오더접수'')
              AND (
                  (L.SUBJECT = ''크나우프석고보드_보류오더 안내'' AND L.MSG LIKE ''%오더번호 '' + I.ORDER_NO + ''%'') OR
                  (L.SUBJECT = ''크나우프석고보드_오더접수'' AND L.MSG LIKE ''%오더번호: '' + I.ORDER_NO + ''%'')
              )
          )';
    EXEC sp_executesql @SQL_PREV;
END
```

END


---------------------------------------------------------------------


좋아. 아래 3가지가 핵심 오류였어.

1. 오더번호 추출 위치 보정값이 잘못됨

   * `'오더번호 '`의 길이는 5, `'오더번호: '`는 6인데 `+4/+5`로 계산해 **한 글자 씩 밀림** → `LEN(N'문자열')`로 교체.

2. 한글 리터럴에 `N` 접두어 누락

   * `N'…'` 없이 쓰면 비교/부분검색이 엇나갈 수 있어 전부 `N` 접두어로 통일.

3. 로그 테이블 존재 여부 판단 시 스키마 미지정

   * `OBJECT_ID(@CURR_LOG_TBL, 'U')` 대신 `OBJECT_ID(N'dbo.' + @CURR_LOG_TBL, 'U')` 형태로 **스키마 명시**.

아래는 위를 반영해 **최소 수정**으로 안정화한 트리거야. (Knauf-SQL-Style v1)

```sql
/* ===========================================================================================
   트리거명 : TRG_MMS_MSG_DUP_CHECK
   대상 테이블 : MMS_MSG
   동작 :
     - INSERT된 데이터에서 SUBJECT/MSG로 오더번호 추출
     - (PHONE + 오더번호) 기준으로
       · MMS_MSG (자기 자신 제외)
       · MMS_LOG_현재월 / MMS_LOG_이전월
       중복 존재 시 STATUS='3' 설정
   비고 :
     - 스키마/전송 포맷 변경 없음
     - 오더번호 추출 오프셋을 LEN()으로 보정
     - 한글 리터럴은 모두 N'' 사용
   =========================================================================================== */
CREATE TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    /* [1] inserted → #ins (오더번호 추출 포함) */
    IF OBJECT_ID('tempdb..#ins') IS NOT NULL
        DROP TABLE #ins;

    SELECT
           I.*
         , CASE
               WHEN I.SUBJECT = N'크나우프석고보드_보류오더 안내'
                    AND CHARINDEX(N'오더번호 ', I.MSG) > 0
               THEN LTRIM(RTRIM(
                        SUBSTRING(
                            I.MSG
                          , CHARINDEX(N'오더번호 ', I.MSG) + LEN(N'오더번호 ')
                          , CASE
                                WHEN CHARINDEX(CHAR(13), I.MSG, CHARINDEX(N'오더번호 ', I.MSG)) > 0
                                     THEN CHARINDEX(CHAR(13), I.MSG, CHARINDEX(N'오더번호 ', I.MSG)) - CHARINDEX(N'오더번호 ', I.MSG) - LEN(N'오더번호 ')
                                WHEN CHARINDEX(CHAR(10), I.MSG, CHARINDEX(N'오더번호 ', I.MSG)) > 0
                                     THEN CHARINDEX(CHAR(10), I.MSG, CHARINDEX(N'오더번호 ', I.MSG)) - CHARINDEX(N'오더번호 ', I.MSG) - LEN(N'오더번호 ')
                                WHEN CHARINDEX('-', I.MSG, CHARINDEX(N'오더번호 ', I.MSG)) > 0
                                     THEN CHARINDEX('-', I.MSG, CHARINDEX(N'오더번호 ', I.MSG)) - CHARINDEX(N'오더번호 ', I.MSG) - LEN(N'오더번호 ')
                                ELSE 15
                            END
                        )
                    ))
               WHEN I.SUBJECT = N'크나우프석고보드_오더접수'
                    AND CHARINDEX(N'오더번호: ', I.MSG) > 0
               THEN LTRIM(RTRIM(
                        SUBSTRING(
                            I.MSG
                          , CHARINDEX(N'오더번호: ', I.MSG) + LEN(N'오더번호: ')
                          , CASE
                                WHEN CHARINDEX(CHAR(13), I.MSG, CHARINDEX(N'오더번호: ', I.MSG)) > 0
                                     THEN CHARINDEX(CHAR(13), I.MSG, CHARINDEX(N'오더번호: ', I.MSG)) - CHARINDEX(N'오더번호: ', I.MSG) - LEN(N'오더번호: ')
                                WHEN CHARINDEX(CHAR(10), I.MSG, CHARINDEX(N'오더번호: ', I.MSG)) > 0
                                     THEN CHARINDEX(CHAR(10), I.MSG, CHARINDEX(N'오더번호: ', I.MSG)) - CHARINDEX(N'오더번호: ', I.MSG) - LEN(N'오더번호: ')
                                WHEN CHARINDEX('-', I.MSG, CHARINDEX(N'오더번호: ', I.MSG)) > 0
                                     THEN CHARINDEX('-', I.MSG, CHARINDEX(N'오더번호: ', I.MSG)) - CHARINDEX(N'오더번호: ', I.MSG) - LEN(N'오더번호: ')
                                ELSE 15
                            END
                        )
                    ))
               ELSE NULL
           END AS ORDER_NO
    INTO #ins
    FROM inserted AS I
    WHERE I.SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수')
    ;

    /* 오더번호가 없으면 처리 종료 */
    IF NOT EXISTS (
        SELECT 1
        FROM #ins
        WHERE ORDER_NO IS NOT NULL
          AND LEN(ORDER_NO) > 0
    )
        RETURN;

    /* [2] 현재월 / 이전월 로그 테이블명 준비 (스키마 포함) */
    DECLARE
          @CURR_YM       VARCHAR(6)  = CONVERT(VARCHAR(6), GETDATE(), 112)
        , @PREV_YM       VARCHAR(6)  = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)
        , @CURR_LOG_NAME NVARCHAR(128)
        , @PREV_LOG_NAME NVARCHAR(128)
        , @SQL           NVARCHAR(MAX)
    ;

    SET @CURR_LOG_NAME = N'dbo.' + N'MMS_LOG_' + @CURR_YM;
    SET @PREV_LOG_NAME = N'dbo.' + N'MMS_LOG_' + @PREV_YM;

    /* [3-1] MMS_MSG 자기자신에서 중복 (자기 자신 제외) */
    UPDATE M
       SET M.STATUS = '3'
      FROM MMS_MSG AS M
      JOIN #ins     AS I
        ON M.MSGKEY = I.MSGKEY
     WHERE I.ORDER_NO IS NOT NULL
       AND LEN(I.ORDER_NO) > 0
       AND EXISTS (
               SELECT 1
                 FROM MMS_MSG AS MM
                WHERE MM.PHONE   = I.PHONE
                  AND MM.SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수')
                  AND MM.MSGKEY <> I.MSGKEY
                  AND (
                          (MM.SUBJECT = N'크나우프석고보드_보류오더 안내' AND MM.MSG LIKE N'%오더번호 '  + I.ORDER_NO + N'%')
                       OR (MM.SUBJECT = N'크나우프석고보드_오더접수'     AND MM.MSG LIKE N'%오더번호: ' + I.ORDER_NO + N'%')
                  )
           )
    ;

    /* [3-2] 현재월 로그 테이블에서 중복 */
    IF OBJECT_ID(@CURR_LOG_NAME, 'U') IS NOT NULL
    BEGIN
        SET @SQL = N'
            UPDATE M
               SET M.STATUS = ''3''
              FROM MMS_MSG AS M
              JOIN #ins     AS I
                ON M.MSGKEY = I.MSGKEY
             WHERE I.ORDER_NO IS NOT NULL
               AND LEN(I.ORDER_NO) > 0
               AND EXISTS (
                       SELECT 1
                         FROM ' + QUOTENAME(PARSENAME(@CURR_LOG_NAME, 2)) + N'.' + QUOTENAME(PARSENAME(@CURR_LOG_NAME, 1)) + N' AS L
                        WHERE L.PHONE   = I.PHONE
                          AND L.SUBJECT IN (N''크나우프석고보드_보류오더 안내'', N''크나우프석고보드_오더접수'')
                          AND (
                                  (L.SUBJECT = N''크나우프석고보드_보류오더 안내'' AND L.MSG LIKE N''%오더번호 ''  + I.ORDER_NO + N''%'')
                               OR (L.SUBJECT = N''크나우프석고보드_오더접수''     AND L.MSG LIKE N''%오더번호: '' + I.ORDER_NO + N''%'')
                              )
                   );';
        EXEC sp_executesql @SQL;
    END;

    /* [3-3] 이전월 로그 테이블에서 중복 */
    IF OBJECT_ID(@PREV_LOG_NAME, 'U') IS NOT NULL
    BEGIN
        SET @SQL = N'
            UPDATE M
               SET M.STATUS = ''3''
              FROM MMS_MSG AS M
              JOIN #ins     AS I
                ON M.MSGKEY = I.MSGKEY
             WHERE I.ORDER_NO IS NOT NULL
               AND LEN(I.ORDER_NO) > 0
               AND EXISTS (
                       SELECT 1
                         FROM ' + QUOTENAME(PARSENAME(@PREV_LOG_NAME, 2)) + N'.' + QUOTENAME(PARSENAME(@PREV_LOG_NAME, 1)) + N' AS L
                        WHERE L.PHONE   = I.PHONE
                          AND L.SUBJECT IN (N''크나우프석고보드_보류오더 안내'', N''크나우프석고보드_오더접수'')
                          AND (
                                  (L.SUBJECT = N''크나우프석고보드_보류오더 안내'' AND L.MSG LIKE N''%오더번호 ''  + I.ORDER_NO + N''%'')
                               OR (L.SUBJECT = N''크나우프석고보드_오더접수''     AND L.MSG LIKE N''%오더번호: '' + I.ORDER_NO + N''%'')
                              )
                   );';
        EXEC sp_executesql @SQL;
    END;
END;
GO
```

### 요약 체크리스트

* [ ] `+4/+5` → `+ LEN(N'...')`, 길이 차감도 동일하게 수정됨
* [ ] 한글 리터럴 모두 `N''`로 표기
* [ ] `OBJECT_ID`는 `dbo.` 스키마 포함하여 검사
* [ ] 동적 SQL에서 `dbo.테이블`을 `QUOTENAME`로 안전하게 조합

원하면, 여기에 **간단한 INSERT 테스트 시나리오**(중복/비중복 케이스)도 바로 붙여줄게.


---------------------------------------------------------------------

# 일단 네가 짜준 트리거와 이 트리거의 차이를 알려줘.  

---------------------------------------------------------------------

좋아. 네 트리거(원본) vs 내가 수정한 트리거의 **핵심 차이**를 한 번에 볼 수 있게 정리했어.

# 한눈에 보는 차이 요약

| 항목             | 네 트리거(원본)                                | 내가 수정한 트리거                               | 영향                          |
| -------------- | ---------------------------------------- | ---------------------------------------- | --------------------------- |
| 오더번호 추출 오프셋    | `+4`, `+5` 고정값                           | `+ LEN(N'오더번호 ')`, `+ LEN(N'오더번호: ')`    | 첫 자리 수 누락/잘림 방지, 포맷 변화에도 안전 |
| 한글 리터럴 처리      | `'크나우프…'` (N 없이)                         | `N'크나우프…'` (모두 N 접두)                     | 한글 깨짐·비교 실패 방지              |
| LIKE 패턴 리터럴    | `'%오더번호 ' + I.ORDER_NO + '%'` 등 (N 없이)   | `N'%오더번호 ' + I.ORDER_NO + N'%'`          | 한글 패턴 매칭 안정성 향상             |
| 로그 테이블 존재 확인   | `OBJECT_ID(@CURR_LOG_TBL,'U')` (스키마 미지정) | `OBJECT_ID(N'dbo.' + @CURR_LOG_TBL,'U')` | 스키마 기본값이 다를 때 오탐 방지         |
| 동적 SQL에서 테이블 명 | `QUOTENAME(@CURR_LOG_TBL)` (스키마 없음)      | `QUOTENAME(schema).QUOTENAME(table)` 구성  | 스키마/특수문자 안전, 정확한 테이블 참조     |
| 문자열 리터럴 타입     | 기본 `'...'`                               | 전부 `N'...'`                              | NVARCHAR 컬럼과의 비교 일관성 확보     |
| 스타일·방어코드       | 기본                                       | 세미콜론/가독성·방어 로직 약간 강화                     | 유지보수성 향상                    |

# 상세 설명

1. **오더번호 추출 위치 보정**

* 원본: `'오더번호 '` 길이가 5인데 `+4`, `'오더번호: '` 길이 6인데 `+5` 사용 → **첫 숫자 하나가 잘리거나** 반대로 공백이 끼는 경우 발생 가능.
* 수정: `LEN(N'오더번호 ')`, `LEN(N'오더번호: ')`로 정확히 계산 → **포맷이 조금 달라도 안전**.

2. **유니코드(N 접두) 통일**

* 원본: `SUBJECT` 비교, `LIKE` 패턴 모두 N 없이 작성.
* 수정: 모든 한글 리터럴에 `N'…'` 적용, 동적 SQL 내부도 `N''` 사용 → **한글 깨짐/매칭 실패 방지**.

3. **로그 테이블 존재 검사 시 스키마 명시**

* 원본: `OBJECT_ID(@CURR_LOG_TBL,'U')`는 `dbo`가 아닌 기본 스키마일 경우 **존재 안 하는 걸로 판단될 위험**.
* 수정: `OBJECT_ID(N'dbo.' + @CURR_LOG_TBL,'U')`로 **명시적 스키마** 사용.

4. **동적 SQL 테이블 식별자 안전화**

* 원본: `QUOTENAME(@CURR_LOG_TBL)`만 사용 → 스키마가 포함되지 않음.
* 수정: `PARSENAME` + `QUOTENAME`로 `schema.table` 형태를 **정확하게 조합** → 스키마 분리/특수문자에도 안전.

5. **LIKE 패턴의 유니코드 처리**

* 원본: `L.MSG LIKE '%오더번호 ' + I.ORDER_NO + '%'`
* 수정: `L.MSG LIKE N'%오더번호 ' + I.ORDER_NO + N'%'`
* 효과: **NVARCHAR 컬럼과의 비교 일관성**, 한글 포함 패턴 매칭 신뢰도 상승.

6. **그 외 자잘한 부분**

* 세미콜론, 들여쓰기, 별칭 정리 등으로 **가독성/유지보수성 강화**.
* 기능 요구사항(현재월·이전월만 검사, PHONE+오더번호 기준)과 동작 흐름은 동일하게 유지.

---

필요하면, 이 차이가 실제로 어떤 레코드에서 결과를 바꾸는지 확인할 **미니 테스트 케이스**(INSERT 예제 4\~6건)도 바로 만들어줄게.

---------------------------------------------------------------------

# 네가 짜준 트리거에서도 아래 부분에 ;에서 오류라는데
```
    /* [1] inserted → #ins */
    IF OBJECT_ID('tempdb..#ins') IS NOT NULL DROP TABLE #ins;
```

---------------------------------------------------------------------

좋아. 저 줄은 문법상 문제없어야 하는데, 실행 도구/파서에서 `;`를 문장 종료로 엄격 처리하면서 트리거 본문에서 헷갈릴 때가 있어. 안전하게 **BEGIN…END**로 감싸거나, 버전이 2016 이상이면 **DROP TABLE IF EXISTS** 를 쓰면 깔끔하게 해결돼.

### 권장 수정 1 (모든 버전 안전)

```sql
-- [1] inserted → #ins
IF OBJECT_ID('tempdb..#ins', 'U') IS NOT NULL
BEGIN
    DROP TABLE #ins;
END;

SELECT
       I.*
     , /* …여기 ORDER_NO 추출 CASE … */
INTO #ins
FROM inserted AS I
WHERE I.SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수');
```

### 권장 수정 2 (SQL Server 2016+)

```sql
-- [1] inserted → #ins
DROP TABLE IF EXISTS #ins;

SELECT
       I.*
     , /* …여기 ORDER_NO 추출 CASE … */
INTO #ins
FROM inserted AS I
WHERE I.SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수');
```

추가 체크 포인트

* 한글 리터럴은 **반드시 N 접두**(예: `N'오더번호 '`)로 유지.
* 바로 뒤 `SELECT … INTO #ins` 구문과 **같은 배치**에서 실행돼야 함(중간에 `GO` 금지).
* 드라이버가 세미콜론 처리에 민감하면, 트리거 본문에서는 **문장 끝 `;`를 아예 빼도 무방**해.

---------------------------------------------------------------------


# 아래 소스가 내가 위에서 요청한 내용과 부합하는지 확인해줘.
```sql
-- ===========================================================================================
-- 트리거명 : TRG_MMS_MSG_DUP_CHECK (단계별 분리 버전)
-- 대상 테이블 : MMS_MSG
-- 
-- 기능 설명:
--   1. INSERT된 SMS 메시지에서 오더번호를 추출
--   2. 동일한 전화번호 + 오더번호 조합이 기존에 있으면 STATUS=3으로 변경 (중복 처리)
--   3. 중복 검사 대상: MMS_MSG 테이블, 현재월/이전월 로그 테이블
--
-- 오더번호 추출 규칙:
--   - '크나우프석고보드_보류오더 안내': MSG에서 '오더번호 XXXXX' 패턴 찾기
--   - '크나우프석고보드_오더접수': MSG에서 '오더번호: XXXXX' 패턴 찾기
--   - 구분자: 줄바꿈(CR/LF), 하이픈(-), 또는 최대 15자
--
-- 세미콜론 문제: 복잡한 CASE문과 SELECT INTO 조합에서 파싱 오류 발생하여 제거
-- ===========================================================================================

ALTER TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON

    BEGIN TRY
        -- =====================================================================================
        -- [STEP 1] 기본 데이터 복사 - 대상 메시지만 필터링
        -- =====================================================================================
        -- 임시테이블이 이미 있으면 삭제 (이전 실행 잔여물 정리)
        IF OBJECT_ID('tempdb..#ins') IS NOT NULL
            DROP TABLE #ins
        
        -- inserted 테이블에서 크나우프 관련 메시지만 복사
        -- 동적 쿼리에서 inserted 테이블을 직접 참조할 수 없어서 임시테이블 필요
        SELECT * INTO #ins FROM inserted 
        WHERE SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수')
        
        -- 대상 데이터가 없으면 트리거 종료
        IF @@ROWCOUNT = 0
            RETURN
        
        -- =====================================================================================
        -- [STEP 2] 오더번호 컬럼 추가
        -- =====================================================================================
        -- 오더번호를 저장할 컬럼을 임시테이블에 추가
        ALTER TABLE #ins ADD ORDER_NO NVARCHAR(50)
        
        -- =====================================================================================
        -- [STEP 3-1] 보류오더 안내 메시지에서 오더번호 추출
        -- =====================================================================================
        -- 패턴: '오더번호 XXXXX' (공백 있음)
        UPDATE #ins 
           SET ORDER_NO = CASE 
                             -- MSG에서 '오더번호 ' 패턴이 있는지 확인
                             WHEN CHARINDEX(N'오더번호 ', MSG) > 0 
                                 THEN LTRIM(
                                               RTRIM(
                                                       SUBSTRING( MSG
                                                                -- '오더번호 ' 다음 위치부터 시작 (+4는 '오더번호 ' 길이)
                                                                , CHARINDEX(N'오더번호 ', MSG) + 4
                                                                -- 오더번호 끝 위치 찾기 (구분자별 우선순위)
                                                                , CASE
                                                                      -- 1순위: 캐리지리턴(CR, CHAR(13)) 찾기
                                                                      WHEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호 ', MSG)) > 0
                                                                          THEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - 4
                                                                      -- 2순위: 라인피드(LF, CHAR(10)) 찾기
                                                                      WHEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호 ', MSG)) > 0
                                                                          THEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - 4
                                                                      -- 3순위: 하이픈(-) 찾기
                                                                      WHEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호 ', MSG)) > 0
                                                                          THEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - 4
                                                                      -- 기본값: 최대 15자까지
                                                                      ELSE 15
                                                                  END
                                                                )
                                                    )
                                            )
                             ELSE NULL
                         END
         WHERE SUBJECT = N'크나우프석고보드_보류오더 안내'

        -- =====================================================================================
        -- [STEP 3-2] 오더접수 메시지에서 오더번호 추출
        -- =====================================================================================
        -- 패턴: '오더번호: XXXXX' (콜론 있음)
        UPDATE #ins 
           SET ORDER_NO = CASE 
                             -- MSG에서 '오더번호: ' 패턴이 있는지 확인
                             WHEN CHARINDEX(N'오더번호: ', MSG) > 0 
                                 THEN LTRIM(
                                              RTRIM(
                                                      SUBSTRING( MSG
                                                               -- '오더번호: ' 다음 위치부터 시작 (+5는 '오더번호: ' 길이)
                                                               , CHARINDEX(N'오더번호: ', MSG) + 5
                                                               -- 오더번호 끝 위치 찾기 (구분자별 우선순위)
                                                               , CASE
                                                                     -- 1순위: 캐리지리턴(CR) 찾기
                                                                     WHEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호: ', MSG)) > 0
                                                                         THEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호: ', MSG)) - CHARINDEX(N'오더번호: ', MSG) - 5
                                                                     -- 2순위: 라인피드(LF) 찾기
                                                                     WHEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호: ', MSG)) > 0
                                                                         THEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호: ', MSG)) - CHARINDEX(N'오더번호: ', MSG) - 5
                                                                     -- 3순위: 하이픈(-) 찾기
                                                                     WHEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호: ', MSG)) > 0
                                                                         THEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호: ', MSG)) - CHARINDEX(N'오더번호: ', MSG) - 5
                                                                     -- 기본값: 최대 15자까지
                                                                     ELSE 15
                                                                 END
                                                               )
                                                   )
                                           )
                             ELSE NULL
                         END
         WHERE SUBJECT = N'크나우프석고보드_오더접수'

        -- [4] 오더번호가 추출되지 않은 경우 종료
        IF NOT EXISTS(SELECT 1 FROM #ins WHERE ORDER_NO IS NOT NULL AND LEN(ORDER_NO) > 0)
            RETURN;

        -- [5] 테이블명 변수
        DECLARE @CURR_YM VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112);
        DECLARE @PREV_YM VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112);
        DECLARE @CURR_LOG_TBL NVARCHAR(128) = QUOTENAME(N'MMS_LOG_' + @CURR_YM);
        DECLARE @PREV_LOG_TBL NVARCHAR(128) = QUOTENAME(N'MMS_LOG_' + @PREV_YM);

        -- [6-1] MMS_MSG 테이블 중복 체크
        UPDATE M
           SET STATUS = N'3'
          FROM MMS_MSG M
               INNER JOIN #ins I
                       ON M.MSGKEY = I.MSGKEY
         WHERE I.ORDER_NO IS NOT NULL
           AND LEN(I.ORDER_NO) > 0
           AND EXISTS (
                         SELECT 1
                           FROM MMS_MSG MM
                          WHERE MM.PHONE = I.PHONE
                            AND MM.SUBJECT IN (N'크나우프석고보드_보류오더 안내', N'크나우프석고보드_오더접수')
                            AND MM.MSGKEY <> I.MSGKEY
                            AND (
                                  (MM.SUBJECT = N'크나우프석고보드_보류오더 안내' AND CHARINDEX(N'오더번호 ' + I.ORDER_NO, MM.MSG) > 0)
                                  OR
                                  (MM.SUBJECT = N'크나우프석고보드_오더접수' AND CHARINDEX(N'오더번호: ' + I.ORDER_NO, MM.MSG) > 0)
                                )
                      );

        -- [6-2] 현재월 로그 테이블 중복 체크
        IF OBJECT_ID(@CURR_LOG_TBL, N'U') IS NOT NULL
        BEGIN
            DECLARE @SQL_CURR NVARCHAR(MAX) = N'
                UPDATE M
                   SET STATUS = N''3''
                  FROM MMS_MSG M
                       INNER JOIN #ins I
                               ON M.MSGKEY = I.MSGKEY
                 WHERE I.ORDER_NO IS NOT NULL
                   AND LEN(I.ORDER_NO) > 0
                   AND EXISTS (
                                 SELECT 1
                                   FROM ' + @CURR_LOG_TBL + N' L
                                  WHERE L.PHONE = I.PHONE
                                    AND L.SUBJECT IN (N''크나우프석고보드_보류오더 안내'', N''크나우프석고보드_오더접수'')
                                    AND (
                                           (L.SUBJECT = N''크나우프석고보드_보류오더 안내'' AND CHARINDEX(N''오더번호 '' + I.ORDER_NO, L.MSG) > 0)
                                           OR
                                           (L.SUBJECT = N''크나우프석고보드_오더접수'' AND CHARINDEX(N''오더번호: '' + I.ORDER_NO, L.MSG) > 0)
                                        )
                              )';
            EXEC sp_executesql @SQL_CURR;
        END

        -- [6-3] 이전월 로그 테이블 중복 체크
        IF OBJECT_ID(@PREV_LOG_TBL, N'U') IS NOT NULL
        BEGIN
            DECLARE @SQL_PREV NVARCHAR(MAX) = N'
                UPDATE M
                   SET STATUS = N''3''
                  FROM MMS_MSG M
                       INNER JOIN #ins I
                               ON M.MSGKEY = I.MSGKEY
                 WHERE I.ORDER_NO IS NOT NULL
                   AND LEN(I.ORDER_NO) > 0
                   AND EXISTS (
                                 SELECT 1
                                   FROM ' + @PREV_LOG_TBL + N' L
                                  WHERE L.PHONE = I.PHONE
                                    AND L.SUBJECT IN (N''크나우프석고보드_보류오더 안내'', N''크나우프석고보드_오더접수'')
                                    AND (
                                           (L.SUBJECT = N''크나우프석고보드_보류오더 안내'' AND CHARINDEX(N''오더번호 '' + I.ORDER_NO, L.MSG) > 0)
                                           OR
                                           (L.SUBJECT = N''크나우프석고보드_오더접수'' AND CHARINDEX(N''오더번호: '' + I.ORDER_NO, L.MSG) > 0)
                                        )
                              )';
            EXEC sp_executesql @SQL_PREV;
        END

    END TRY
    BEGIN CATCH
        -- 오류 발생 시 조용히 종료
        RETURN;
    END CATCH

END
GO
```

---------------------------------------------------------------------

좋아. 결론부터 말하면 **핵심 요구사항은 대부분 충족**하지만, 아래 3가지는 **오작동 가능성이 높으니 꼭 보정**해야 해.

# 합치/불일치 요약

* ✅ **차단 기준**: `PHONE + ORDER_NO` 일치 시 차단 → 충족
* ✅ **대상 SUBJECT**: 2종(보류오더 안내, 오더접수)만 처리 → 충족
* ✅ **로그 범위**: 이번 달 + 이전 달만 검사 → 충족
* ✅ **스키마/전송값 변경 없음** → 충족
* ⚠️ **오더번호 추출 오프셋**: `+4`, `+5` 고정 사용 → **첫 자리 잘림 위험**(불일치)
* ⚠️ **로그 테이블 존재 확인/참조**: 스키마 미지정(`OBJECT_ID`) + `QUOTENAME`만 사용 → **존재 판정/참조 실패 가능**(개선 필요)
* ⚠️ **부분일치 오탐**: `CHARINDEX('오더번호 ' + ORDER_NO, MSG)` → **‘123’가 ‘12345’에 매칭되는 오탐** 가능(개선 권장)

---

# 꼭 고쳐야 할 최소 패치

## 1) 오더번호 추출 오프셋을 고정값 → `LEN()` 기반으로

지금 `+4`, `-4`(보류), `+5`, `-5`(접수)인데, 실제 접두 문자열 길이와 어긋나 첫 문자 누락 위험이 있어. 아래처럼 **길이 계산을 LEN()으로 통일**해.

```sql
-- 보류오더 안내
..., CHARINDEX(N'오더번호 ', MSG) + LEN(N'오더번호 ')
, CASE
    WHEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호 ', MSG)) > 0
      THEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - LEN(N'오더번호 ')
    WHEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호 ', MSG)) > 0
      THEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - LEN(N'오더번호 ')
    WHEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호 ', MSG)) > 0
      THEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호 ', MSG)) - CHARINDEX(N'오더번호 ', MSG) - LEN(N'오더번호 ')
    ELSE 15
  END
...

-- 오더접수
..., CHARINDEX(N'오더번호: ', MSG) + LEN(N'오더번호: ')
, CASE
    WHEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호: ', MSG)) > 0
      THEN CHARINDEX(CHAR(13), MSG, CHARINDEX(N'오더번호: ', MSG)) - CHARINDEX(N'오더번호: ', MSG) - LEN(N'오더번호: ')
    WHEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호: ', MSG)) > 0
      THEN CHARINDEX(CHAR(10), MSG, CHARINDEX(N'오더번호: ', MSG)) - CHARINDEX(N'오더번호: ', MSG) - LEN(N'오더번호: ')
    WHEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호: ', MSG)) > 0
      THEN CHARINDEX(N'-', MSG, CHARINDEX(N'오더번호: ', MSG)) - CHARINDEX(N'오더번호: ', MSG) - LEN(N'오더번호: ')
    ELSE 15
  END
...
```

## 2) 로그 테이블 체크: **스키마 포함** + 안전한 동적 참조

`OBJECT_ID(@CURR_LOG_TBL, 'U')`에서 `@CURR_LOG_TBL`이 `[MMS_LOG_YYYYMM]`(스키마 없음)이라 **NULL**이 나올 수 있어. 다음처럼 **스키마 포함 이름**과 **인용된 이름**을 분리해 써.

```sql
DECLARE
      @CURR_YM       VARCHAR(6) = CONVERT(VARCHAR(6), GETDATE(), 112)
    , @PREV_YM       VARCHAR(6) = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)
    , @CURR_RAW      NVARCHAR(128) = N'dbo.MMS_LOG_' + @CURR_YM   -- OBJECT_ID용(스키마 포함)
    , @PREV_RAW      NVARCHAR(128) = N'dbo.MMS_LOG_' + @PREV_YM
    , @CURR_Q        NVARCHAR(300) = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @CURR_YM) -- 동적SQL용
    , @PREV_Q        NVARCHAR(300) = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @PREV_YM)
    , @SQL           NVARCHAR(MAX);

IF OBJECT_ID(@CURR_RAW, N'U') IS NOT NULL
BEGIN
    SET @SQL = N'
        UPDATE M
           SET STATUS = N''3''
          FROM MMS_MSG M
          JOIN #ins I ON M.MSGKEY = I.MSGKEY
         WHERE I.ORDER_NO IS NOT NULL
           AND LEN(I.ORDER_NO) > 0
           AND EXISTS (
                 SELECT 1
                   FROM ' + @CURR_Q + N' L
                  WHERE L.PHONE = I.PHONE
                    AND L.SUBJECT IN (N''크나우프석고보드_보류오더 안내'', N''크나우프석고보드_오더접수'')
                    AND (
                          (L.SUBJECT = N''크나우프석고보드_보류오더 안내'' AND CHARINDEX(N''오더번호 ''  + I.ORDER_NO, L.MSG) > 0)
                       OR (L.SUBJECT = N''크나우프석고보드_오더접수''     AND CHARINDEX(N''오더번호: '' + I.ORDER_NO, L.MSG) > 0)
                    )
             );';
    EXEC sp_executesql @SQL;
END;

IF OBJECT_ID(@PREV_RAW, N'U') IS NOT NULL
BEGIN
    -- 동일 패턴으로 @PREV_Q 사용
END;
```

## 3) 부분일치 오탐 방지(권장)

현재 `CHARINDEX(N'오더번호: ' + I.ORDER_NO, MSG) > 0`은
`오더번호: 12345`에 대해 `ORDER_NO=123`도 **매칭**돼. 뒤 문자가 **숫자가 아닌 것**(개행/공백/끝)인지 한 번 더 확인하면 좋아.

```sql
-- 예: MMS_MSG 검사 EXISTS 안에서 후속 조건 추가
AND (
      (
        MM.SUBJECT = N'크나우프석고보드_오더접수'
    AND CHARINDEX(N'오더번호: ' + I.ORDER_NO, MM.MSG) > 0
    AND PATINDEX(N'%[^0-9]%', SUBSTRING(
           MM.MSG
         , CHARINDEX(N'오더번호: ' + I.ORDER_NO, MM.MSG) + LEN(N'오더번호: ') + LEN(I.ORDER_NO)
         , 1
      ) + N'X') = 1  -- 다음 문자가 숫자가 아니어야 함(끝이면 'X'로 대체되어 항상 1)
     )
   OR (
        MM.SUBJECT = N'크나우프석고보드_보류오더 안내'
    AND CHARINDEX(N'오더번호 ' + I.ORDER_NO, MM.MSG) > 0
    AND PATINDEX(N'%[^0-9]%', SUBSTRING(
           MM.MSG
         , CHARINDEX(N'오더번호 ' + I.ORDER_NO, MM.MSG) + LEN(N'오더번호 ') + LEN(I.ORDER_NO)
         , 1
      ) + N'X') = 1
     )
    )
```

> 위 보강은 선택이지만, **숫자 경계** 체크를 넣으면 중복 판정 정확도가 높아져.

---

# 그 밖의 좋은 포인트(유지)

* `TRY/CATCH`로 안전 종료 → 운영 친화적
* `SELECT * INTO #ins` 후 `@@ROWCOUNT` 체크 → 불필요 실행 줄임
* 임시테이블에 `ORDER_NO` 컬럼 추가 후 단계별 `UPDATE` → 디버깅 용이
* 모든 한글 리터럴 `N''` 사용 → 비교 안정성 확보

---

# 최종 판단

* 요구사항 취지에는 **대체로 부합**.
* 위 **3개 패치** 적용 시, 실운영에서의 **경계 케이스/스키마 이슈/오탐**까지 커버돼서 안정적으로 굴러간다.
* 원하면, 네 소스에 바로 적용 가능한 **완성본 트리거**로 묶어서 제공할게.

---------------------------------------------------------------------

# 아~ 그리고 방금  보낸 트리거에 쿼리들의 정렬을 규칙들을 네가 가지고 있는 쿼리 정렬 규칙에 적용해줘. 네가 가지고 있는 쿼리 정렬 규칙 업데이트해야 해. 
그리고, 네말대로 숫자의 위치는 문제의 소지가 있어 보이는데 그럼  +4, +5 말고 다른 방법은 뭐가 있어?

---------------------------------------------------------------------


좋아. 아래 두 가지 진행했어.

1. 네가 보낸 트리거 전체에 **Knauf-SQL-Style v1.1** 포맷을 적용해서 재정렬
2. `+4`, `+5` 없이도 안전하게 동작하는 **대안 파싱 방법**까지 정리

---

# ✅ Knauf-SQL-Style v1.1 적용본 (오류 보정 + 경계검사 + 스키마 명시)

```sql
-- ===========================================================================================
-- 트리거명 : TRG_MMS_MSG_DUP_CHECK (단계별 분리 + 포맷 적용)
-- 대상 테이블 : MMS_MSG
--
-- 기능:
--   1) INSERT된 메시지에서 오더번호 추출
--   2) (PHONE + ORDER_NO) 중복 시 STATUS = '3' 처리
--   3) 대상: MMS_MSG, MMS_LOG_현재월, MMS_LOG_이전월
--
-- 규칙:
--   - 보류오더 안내 : '오더번호 XXXXX'
--   - 오더접수     : '오더번호: XXXXX'
--   - 구분: CR/LF/하이픈 또는 최대 15자
--   - 유니코드 리터럴 N'' 사용, 오프셋은 LEN() 사용
--   - 숫자 경계검사(다음 문자가 숫자가 아닌지 확인) 추가
-- ===========================================================================================
ALTER TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON

    BEGIN TRY
        -- [STEP 1] inserted → #ins (대상 SUBJECT만)
        IF OBJECT_ID('tempdb..#ins', 'U') IS NOT NULL
        BEGIN
            DROP TABLE #ins
        END

        SELECT
               I.*
        INTO #ins
        FROM inserted AS I
        WHERE I.SUBJECT IN (
                  N'크나우프석고보드_보류오더 안내'
                , N'크나우프석고보드_오더접수'
              )

        IF @@ROWCOUNT = 0
            RETURN

        -- [STEP 2] 오더번호 컬럼 추가
        ALTER TABLE #ins
            ADD ORDER_NO NVARCHAR(50)

        -- [STEP 3-1] 보류오더 안내: '오더번호 ' 뒤에서 추출 (LEN 사용)
        UPDATE I
           SET ORDER_NO = LTRIM(RTRIM(SUBSTRING(
                               I.MSG
                             , CHARINDEX(N'오더번호 ', I.MSG) + LEN(N'오더번호 ')
                             , CASE
                                   WHEN CHARINDEX(CHAR(13), I.MSG, CHARINDEX(N'오더번호 ', I.MSG)) > 0
                                        THEN CHARINDEX(CHAR(13), I.MSG, CHARINDEX(N'오더번호 ', I.MSG))
                                           - CHARINDEX(N'오더번호 ', I.MSG)
                                           - LEN(N'오더번호 ')
                                   WHEN CHARINDEX(CHAR(10), I.MSG, CHARINDEX(N'오더번호 ', I.MSG)) > 0
                                        THEN CHARINDEX(CHAR(10), I.MSG, CHARINDEX(N'오더번호 ', I.MSG))
                                           - CHARINDEX(N'오더번호 ', I.MSG)
                                           - LEN(N'오더번호 ')
                                   WHEN CHARINDEX(N'-', I.MSG, CHARINDEX(N'오더번호 ', I.MSG)) > 0
                                        THEN CHARINDEX(N'-', I.MSG, CHARINDEX(N'오더번호 ', I.MSG))
                                           - CHARINDEX(N'오더번호 ', I.MSG)
                                           - LEN(N'오더번호 ')
                                   ELSE 15
                               END
                           )))
        FROM #ins AS I
        WHERE I.SUBJECT = N'크나우프석고보드_보류오더 안내'
          AND CHARINDEX(N'오더번호 ', I.MSG) > 0

        -- [STEP 3-2] 오더접수: '오더번호: ' 뒤에서 추출 (LEN 사용)
        UPDATE I
           SET ORDER_NO = LTRIM(RTRIM(SUBSTRING(
                               I.MSG
                             , CHARINDEX(N'오더번호: ', I.MSG) + LEN(N'오더번호: ')
                             , CASE
                                   WHEN CHARINDEX(CHAR(13), I.MSG, CHARINDEX(N'오더번호: ', I.MSG)) > 0
                                        THEN CHARINDEX(CHAR(13), I.MSG, CHARINDEX(N'오더번호: ', I.MSG))
                                           - CHARINDEX(N'오더번호: ', I.MSG)
                                           - LEN(N'오더번호: ')
                                   WHEN CHARINDEX(CHAR(10), I.MSG, CHARINDEX(N'오더번호: ', I.MSG)) > 0
                                        THEN CHARINDEX(CHAR(10), I.MSG, CHARINDEX(N'오더번호: ', I.MSG))
                                           - CHARINDEX(N'오더번호: ', I.MSG)
                                           - LEN(N'오더번호: ')
                                   WHEN CHARINDEX(N'-', I.MSG, CHARINDEX(N'오더번호: ', I.MSG)) > 0
                                        THEN CHARINDEX(N'-', I.MSG, CHARINDEX(N'오더번호: ', I.MSG))
                                           - CHARINDEX(N'오더번호: ', I.MSG)
                                           - LEN(N'오더번호: ')
                                   ELSE 15
                               END
                           )))
        FROM #ins AS I
        WHERE I.SUBJECT = N'크나우프석고보드_오더접수'
          AND CHARINDEX(N'오더번호: ', I.MSG) > 0

        -- [STEP 4] 오더번호 미추출 시 종료
        IF NOT EXISTS (
               SELECT
                      1
               FROM #ins
               WHERE ORDER_NO IS NOT NULL
                 AND LEN(ORDER_NO) > 0
           )
            RETURN

        -- [STEP 5] 로그 테이블 이름 준비(스키마 포함 / 동적SQL용 인용명)
        DECLARE
              @CURR_YM        VARCHAR(6)  = CONVERT(VARCHAR(6), GETDATE(), 112)
            , @PREV_YM        VARCHAR(6)  = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)
            , @CURR_RAW       NVARCHAR(128) = N'dbo.MMS_LOG_' + @CURR_YM
            , @PREV_RAW       NVARCHAR(128) = N'dbo.MMS_LOG_' + @PREV_YM
            , @CURR_Q         NVARCHAR(300) = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @CURR_YM)
            , @PREV_Q         NVARCHAR(300) = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @PREV_YM)
            , @SQL            NVARCHAR(MAX)

        -- [STEP 6-1] MMS_MSG 중복 검사(자기 자신 제외) + 숫자 경계검사
        UPDATE M
           SET STATUS = N'3'
          FROM MMS_MSG AS M
          JOIN #ins     AS I
            ON M.MSGKEY = I.MSGKEY
         WHERE I.ORDER_NO IS NOT NULL
           AND LEN(I.ORDER_NO) > 0
           AND EXISTS (
                   SELECT
                          1
                     FROM MMS_MSG AS MM
                    WHERE MM.PHONE   = I.PHONE
                      AND MM.SUBJECT IN (
                              N'크나우프석고보드_보류오더 안내'
                            , N'크나우프석고보드_오더접수'
                          )
                      AND MM.MSGKEY <> I.MSGKEY
                      AND (
                              (
                                  MM.SUBJECT = N'크나우프석고보드_보류오더 안내'
                              AND CHARINDEX(N'오더번호 '  + I.ORDER_NO, MM.MSG) > 0
                              AND PATINDEX(
                                      N'%[^0-9]%'
                                    , SUBSTRING(
                                          MM.MSG
                                        , CHARINDEX(N'오더번호 ' + I.ORDER_NO, MM.MSG)
                                          + LEN(N'오더번호 ')
                                          + LEN(I.ORDER_NO)
                                        , 1
                                      ) + N'X'
                                  ) = 1
                              )
                           OR (
                                  MM.SUBJECT = N'크나우프석고보드_오더접수'
                              AND CHARINDEX(N'오더번호: ' + I.ORDER_NO, MM.MSG) > 0
                              AND PATINDEX(
                                      N'%[^0-9]%'
                                    , SUBSTRING(
                                          MM.MSG
                                        , CHARINDEX(N'오더번호: ' + I.ORDER_NO, MM.MSG)
                                          + LEN(N'오더번호: ')
                                          + LEN(I.ORDER_NO)
                                        , 1
                                      ) + N'X'
                                  ) = 1
                              )
                          )
               )

        -- [STEP 6-2] 현재월 로그 검사
        IF OBJECT_ID(@CURR_RAW, N'U') IS NOT NULL
        BEGIN
            SET @SQL = N'
                UPDATE M
                   SET STATUS = N''3''
                  FROM MMS_MSG AS M
                  JOIN #ins     AS I
                    ON M.MSGKEY = I.MSGKEY
                 WHERE I.ORDER_NO IS NOT NULL
                   AND LEN(I.ORDER_NO) > 0
                   AND EXISTS (
                           SELECT 1
                             FROM ' + @CURR_Q + N' AS L
                            WHERE L.PHONE   = I.PHONE
                              AND L.SUBJECT IN (
                                      N''크나우프석고보드_보류오더 안내''
                                    , N''크나우프석고보드_오더접수''
                                  )
                              AND (
                                      (
                                          L.SUBJECT = N''크나우프석고보드_보류오더 안내''
                                      AND CHARINDEX(N''오더번호 ''  + I.ORDER_NO, L.MSG) > 0
                                      AND PATINDEX(
                                              N''%[^0-9]%''
                                            , SUBSTRING(
                                                  L.MSG
                                                , CHARINDEX(N''오더번호 '' + I.ORDER_NO, L.MSG)
                                                  + LEN(N''오더번호 '')
                                                  + LEN(I.ORDER_NO)
                                                , 1
                                              ) + N''X''
                                          ) = 1
                                      )
                                   OR (
                                          L.SUBJECT = N''크나우프석고보드_오더접수''
                                      AND CHARINDEX(N''오더번호: '' + I.ORDER_NO, L.MSG) > 0
                                      AND PATINDEX(
                                              N''%[^0-9]%''
                                            , SUBSTRING(
                                                  L.MSG
                                                , CHARINDEX(N''오더번호: '' + I.ORDER_NO, L.MSG)
                                                  + LEN(N''오더번호: '')
                                                  + LEN(I.ORDER_NO)
                                                , 1
                                              ) + N''X''
                                          ) = 1
                                      )
                                  )
                       )'
            EXEC sp_executesql @SQL
        END

        -- [STEP 6-3] 이전월 로그 검사
        IF OBJECT_ID(@PREV_RAW, N'U') IS NOT NULL
        BEGIN
            SET @SQL = N'
                UPDATE M
                   SET STATUS = N''3''
                  FROM MMS_MSG AS M
                  JOIN #ins     AS I
                    ON M.MSGKEY = I.MSGKEY
                 WHERE I.ORDER_NO IS NOT NULL
                   AND LEN(I.ORDER_NO) > 0
                   AND EXISTS (
                           SELECT 1
                             FROM ' + @PREV_Q + N' AS L
                            WHERE L.PHONE   = I.PHONE
                              AND L.SUBJECT IN (
                                      N''크나우프석고보드_보류오더 안내''
                                    , N''크나우프석고보드_오더접수''
                                  )
                              AND (
                                      (
                                          L.SUBJECT = N''크나우프석고보드_보류오더 안내''
                                      AND CHARINDEX(N''오더번호 ''  + I.ORDER_NO, L.MSG) > 0
                                      AND PATINDEX(
                                              N''%[^0-9]%''
                                            , SUBSTRING(
                                                  L.MSG
                                                , CHARINDEX(N''오더번호 '' + I.ORDER_NO, L.MSG)
                                                  + LEN(N''오더번호 '')
                                                  + LEN(I.ORDER_NO)
                                                , 1
                                              ) + N''X''
                                          ) = 1
                                      )
                                   OR (
                                          L.SUBJECT = N''크나우프석고보드_오더접수''
                                      AND CHARINDEX(N''오더번호: '' + I.ORDER_NO, L.MSG) > 0
                                      AND PATINDEX(
                                              N''%[^0-9]%''
                                            , SUBSTRING(
                                                  L.MSG
                                                , CHARINDEX(N''오더번호: '' + I.ORDER_NO, L.MSG)
                                                  + LEN(N''오더번호: '')
                                                  + LEN(I.ORDER_NO)
                                                , 1
                                              ) + N''X''
                                          ) = 1
                                      )
                                  )
                       )'
            EXEC sp_executesql @SQL
        END
    END TRY
    BEGIN CATCH
        RETURN
    END CATCH
END
GO
```

---

# 🔁 `+4`, `+5` 없이 파싱하는 대안(더 견고)

아래 방법은 **콜론(:) 유무, 공백/개행 변화**와 무관하게 작동해.
핵심은 `"오더번호"` 이후 꼬리(tail)에서 **첫 숫자 시작 위치**와 **첫 비숫자 위치**를 `PATINDEX`로 찾아서 **숫자만 슬라이스**하는 것.

## 대안 A) 한 번의 UPDATE로 두 SUBJECT 모두 처리

```sql
-- [STEP 3] (대체안) 오더번호 범용 추출: '오더번호' 다음 첫 숫자 ~ 다음 비숫자 전까지
UPDATE I
   SET ORDER_NO =
       CASE
           WHEN P.pos > 0 AND D.dstart > 0
                THEN SUBSTRING(
                         T.tail
                       , D.dstart
                       , CASE
                             WHEN ND.nextNonDigitPos = 0
                                  THEN 50                     -- 상한선
                             ELSE ND.nextNonDigitPos - 1
                         END
                     )
           ELSE NULL
       END
FROM #ins AS I
CROSS APPLY (
    SELECT CHARINDEX(N'오더번호', I.MSG) AS pos
) AS P
CROSS APPLY (
    SELECT CASE
               WHEN P.pos > 0
                    THEN SUBSTRING(I.MSG, P.pos + LEN(N'오더번호'), 300)
               ELSE N''
           END AS tail
) AS T
CROSS APPLY (
    SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart
) AS D
CROSS APPLY (
    SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos
) AS ND
```

* 장점: `+4`, `+5` 같은 **매직 넘버 제거**, 콜론/공백/개행/하이픈 변화에도 튼튼.
* 성능: inserted 건수 규모가 작다면 충분히 빠름.

## 대안 B) 추출은 위처럼 하고, 비교는 **정확한 숫자 경계**만 허용

* 지금 적용한 **경계검사(PATINDEX)** 그대로 유지하면 `ORDER_NO=123`이 `12345`에 잘못 매칭되는 문제를 방지.

---

# 📌 규칙 업데이트(반영됨)

* 유니코드 리터럴은 **항상 `N''`**.
* SUBSTRING 오프셋은 **`LEN('접두문자열')`** 사용.
* 동적 테이블 참조는 **스키마 포함** + `QUOTENAME` 사용, `OBJECT_ID('dbo.Table','U')`로 존재 확인.
* 숫자 토큰 비교 시 **경계검사** 추가: `PATINDEX('%[^0-9]%', nextChar + 'X') = 1`.
* 트리거/배치 혼용 환경에서는 **세미콜론 생략 허용**(단, CTE 앞에는 필요).

원하면 위 **대안 A**로 파싱 부분을 바꾼 버전도 바로 만들어줄게.

---------------------------------------------------------------------

만들어줘.

---------------------------------------------------------------------


좋아. 요청대로 **매직 넘버(+4/+5) 없이** `PATINDEX` 기반 범용 파싱으로 바꾸고, **Knauf-SQL-Style v1.1** 포맷을 적용한 최종 트리거를 작성했어.
핵심: `inserted`와 비교 대상(MMS\_MSG, 로그테이블) 모두에서 **“오더번호” 뒤 첫 숫자부터 다음 비숫자 전까지**를 추출해 **PHONE + ORDER\_NO**로 중복 판단해 `STATUS='3'`.

---

## 최종 트리거 (범용 파싱 + v1.1 포맷)

```sql
-- ===========================================================================================
-- 트리거명 : TRG_MMS_MSG_DUP_CHECK (범용 파싱 + v1.1 포맷)
-- 대상 테이블 : MMS_MSG
-- 동작 :
--   - INSERT된 행의 MSG에서 '오더번호' 이후의 숫자 토큰을 추출(콜론/공백/개행/하이픈 변동 무관)
--   - PHONE + ORDER_NO 기준으로 MMS_MSG(자기 자신 제외), MMS_LOG_현재월/이전월에 존재하면 STATUS='3'
-- 제한 :
--   - 스키마/전송 포맷 변경 없음
--   - SUBJECT는 두 유형만 처리: 보류오더 안내 / 오더접수
-- ===========================================================================================
ALTER TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON

    BEGIN TRY
        -- [1] inserted → #ins (대상 SUBJECT만)
        IF OBJECT_ID('tempdb..#ins', 'U') IS NOT NULL
        BEGIN
            DROP TABLE #ins
        END

        SELECT
               I.MSGKEY
             , I.PHONE
             , I.SUBJECT
             , I.MSG
        INTO #ins
        FROM inserted AS I
        WHERE I.SUBJECT IN (
                  N'크나우프석고보드_보류오더 안내'
                , N'크나우프석고보드_오더접수'
              )

        IF @@ROWCOUNT = 0
            RETURN

        -- [2] inserted 파싱 → #ins_norm (오더번호 범용 추출)
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
                                     WHEN ND.nextNonDigitPos = 0
                                          THEN 50    -- 안전 상한
                                     ELSE ND.nextNonDigitPos - 1
                                 END
                             )
                   ELSE NULL
               END AS ORDER_NO
        INTO #ins_norm
        FROM #ins AS X
        CROSS APPLY ( SELECT CHARINDEX(N'오더번호', X.MSG) AS pos ) AS P
        CROSS APPLY (
            SELECT CASE
                       WHEN P.pos > 0
                            THEN SUBSTRING(X.MSG, P.pos + LEN(N'오더번호'), 300)
                       ELSE N''
                   END AS tail
        ) AS T
        CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
        CROSS APPLY ( SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND

        -- 대상 없음 종료
        IF NOT EXISTS (
               SELECT 1
               FROM #ins_norm
               WHERE ORDER_NO IS NOT NULL
                 AND LEN(ORDER_NO) > 0
           )
            RETURN

        -- [3] 변수: 로그 테이블(스키마 포함 / 동적 SQL용 인용명)
        DECLARE
              @CURR_YM   VARCHAR(6)   = CONVERT(VARCHAR(6), GETDATE(), 112)
            , @PREV_YM   VARCHAR(6)   = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)
            , @CURR_RAW  NVARCHAR(128) = N'dbo.MMS_LOG_' + @CURR_YM
            , @PREV_RAW  NVARCHAR(128) = N'dbo.MMS_LOG_' + @PREV_YM
            , @CURR_Q    NVARCHAR(300) = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @CURR_YM)
            , @PREV_Q    NVARCHAR(300) = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @PREV_YM)
            , @SQL       NVARCHAR(MAX)

        -- [4] MMS_MSG 중복 체크 (자기 자신 제외) : PHONE + ORDER_NO 일치
        UPDATE M
           SET STATUS = N'3'
          FROM MMS_MSG    AS M
          JOIN #ins_norm  AS I
            ON M.MSGKEY = I.MSGKEY
         WHERE I.ORDER_NO IS NOT NULL
           AND LEN(I.ORDER_NO) > 0
           AND EXISTS (
                   SELECT
                          1
                     FROM MMS_MSG AS MM
                     CROSS APPLY ( SELECT CHARINDEX(N'오더번호', MM.MSG) AS pos ) AS P
                     CROSS APPLY (
                         SELECT CASE
                                    WHEN P.pos > 0
                                         THEN SUBSTRING(MM.MSG, P.pos + LEN(N'오더번호'), 300)
                                    ELSE N''
                                END AS tail
                     ) AS T
                     CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
                     CROSS APPLY (
                         SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos
                     ) AS ND
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
                      AND MM.SUBJECT IN (
                              N'크나우프석고보드_보류오더 안내'
                            , N'크나우프석고보드_오더접수'
                          )
                      AND O.ORDER_NO  = I.ORDER_NO
               )

        -- [5] MMS_LOG_현재월 중복 체크
        IF OBJECT_ID(@CURR_RAW, N'U') IS NOT NULL
        BEGIN
            SET @SQL = N'
                UPDATE M
                   SET STATUS = N''3''
                  FROM MMS_MSG   AS M
                  JOIN #ins_norm AS I
                    ON M.MSGKEY = I.MSGKEY
                 WHERE I.ORDER_NO IS NOT NULL
                   AND LEN(I.ORDER_NO) > 0
                   AND EXISTS (
                           SELECT 1
                             FROM ' + @CURR_Q + N' AS L
                             CROSS APPLY ( SELECT CHARINDEX(N''오더번호'', L.MSG) AS pos ) AS P
                             CROSS APPLY (
                                 SELECT CASE
                                            WHEN P.pos > 0
                                                 THEN SUBSTRING(L.MSG, P.pos + LEN(N''오더번호''), 300)
                                            ELSE N''''
                                        END AS tail
                             ) AS T
                             CROSS APPLY ( SELECT PATINDEX(N''%[0-9]%'', T.tail) AS dstart ) AS D
                             CROSS APPLY (
                                 SELECT PATINDEX(N''%[^0-9]%'', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos
                             ) AS ND
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
                              AND L.SUBJECT IN (
                                      N''크나우프석고보드_보류오더 안내''
                                    , N''크나우프석고보드_오더접수''
                                  )
                              AND O.ORDER_NO  = I.ORDER_NO
                       )'
            EXEC sp_executesql @SQL
        END

        -- [6] MMS_LOG_이전월 중복 체크
        IF OBJECT_ID(@PREV_RAW, N'U') IS NOT NULL
        BEGIN
            SET @SQL = N'
                UPDATE M
                   SET STATUS = N''3''
                  FROM MMS_MSG   AS M
                  JOIN #ins_norm AS I
                    ON M.MSGKEY = I.MSGKEY
                 WHERE I.ORDER_NO IS NOT NULL
                   AND LEN(I.ORDER_NO) > 0
                   AND EXISTS (
                           SELECT 1
                             FROM ' + @PREV_Q + N' AS L
                             CROSS APPLY ( SELECT CHARINDEX(N''오더번호'', L.MSG) AS pos ) AS P
                             CROSS APPLY (
                                 SELECT CASE
                                            WHEN P.pos > 0
                                                 THEN SUBSTRING(L.MSG, P.pos + LEN(N''오더번호''), 300)
                                            ELSE N''''
                                        END AS tail
                             ) AS T
                             CROSS APPLY ( SELECT PATINDEX(N''%[0-9]%'', T.tail) AS dstart ) AS D
                             CROSS APPLY (
                                 SELECT PATINDEX(N''%[^0-9]%'', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos
                             ) AS ND
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
                              AND L.SUBJECT IN (
                                      N''크나우프석고보드_보류오더 안내''
                                    , N''크나우프석고보드_오더접수''
                                  )
                              AND O.ORDER_NO  = I.ORDER_NO
                       )'
            EXEC sp_executesql @SQL
        END
    END TRY
    BEGIN CATCH
        RETURN
    END CATCH
END
GO
```

---

## 포인트 요약

* `+4/+5` 제거 → **`오더번호` 토큰 뒤 꼬리에서** `PATINDEX`로 **첫 숫자 시작/비숫자 종료**를 계산해 슬라이스.
* 비교 시에도 대상 쪽(MMS\_MSG/LOG)에서 **동일 방식으로 추출** → `O.ORDER_NO = I.ORDER_NO` 정확 비교, 숫자 경계 이슈 제거.
* `OBJECT_ID('dbo.Table','U')` + `QUOTENAME('dbo') + '.' + QUOTENAME('Table')`로 **존재 확인/동적 참조** 분리.
* **N''** 일관 적용, **Knauf-SQL-Style v1.1** 정렬(키워드 대문자, 컬럼 1줄 1개, ON/AND 줄 첫머리).


---------------------------------------------------------------------


# 우와아~~ 오류가 나타났다.

SQL Error [137] [S0002]: Must declare the scalar variable "@CURR_YM".
SQL Error [137] [S0002]: Must declare the scalar variable "@CURR_YM".
  Must declare the scalar variable "@CURR_YM".
  Must declare the scalar variable "@CURR_YM".
    Must declare the scalar variable "@CURR_RAW".
    Must declare the scalar variable "@CURR_Q".
    Must declare the scalar variable "@SQL".
    Must declare the scalar variable "@PREV_RAW".
    Must declare the scalar variable "@PREV_Q".
    Must declare the scalar variable "@SQL".
  Must declare the scalar variable "@CURR_RAW".
  Must declare the scalar variable "@CURR_Q".
  Must declare the scalar variable "@SQL".
  Must declare the scalar variable "@PREV_RAW".
  Must declare the scalar variable "@PREV_Q".
  Must declare the scalar variable "@SQL".

---------------------------------------------------------------------


오류 원인 먼저 짚자.

* 메시지 `Must declare the scalar variable '@CURR_YM'`는 **해당 변수가 선언 범위를 벗어난 상태에서 실행**될 때 나와.
  대표 케이스는:

  1. 트리거 전체가 아닌 **일부만 선택 실행**(특히 동적 SQL 부분만)
  2. 변수 선언/초기화를 **한 줄 선언 초기화(DECLARE @v = …)** 로 썼는데, 호환성 수준/버전 이슈
  3. 동적 SQL 문자열 안에 **@변수 토큰이 그대로 들어간 경우**(이번 스크립트는 해당 없음)

안전하게 가기 위해 다음을 적용했어:

* 모든 변수는 **DECLARE 후 SET**으로 초기화(호환성↑)
* 전체를 **한 배치로 실행**(중간 `GO` 금지, 끝에만 `GO`)
* 동적 SQL은 **스키마 포함 인용명**만 문자열로 삽입(@변수 토큰 없음)

아래 스크립트를 **통으로 실행**해봐. (트리거 존재 여부 상관없이 동작하도록 `CREATE OR ALTER` 사용)

---

## 최종 트리거 (범용 파싱 + 경계검사 + v1.1 포맷, 변수 DECLARE/SET 분리)

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
CREATE OR ALTER TRIGGER dbo.TRG_MMS_MSG_DUP_CHECK
ON dbo.MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        /* [1] inserted → #ins (대상 SUBJECT만) */
        IF OBJECT_ID('tempdb..#ins', 'U') IS NOT NULL
        BEGIN
            DROP TABLE #ins;
        END;

        SELECT
               I.MSGKEY
             , I.PHONE
             , I.SUBJECT
             , I.MSG
        INTO #ins
        FROM inserted AS I
        WHERE I.SUBJECT IN (
                  N'크나우프석고보드_보류오더 안내'
                , N'크나우프석고보드_오더접수'
              );

        IF @@ROWCOUNT = 0
            RETURN;

        /* [2] inserted 파싱 → #ins_norm (오더번호 범용 추출) */
        IF OBJECT_ID('tempdb..#ins_norm', 'U') IS NOT NULL
        BEGIN
            DROP TABLE #ins_norm;
        END;

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
        CROSS APPLY (
            SELECT CASE
                       WHEN P.pos > 0 THEN SUBSTRING(X.MSG, P.pos + LEN(N'오더번호'), 300)
                       ELSE N''
                   END AS tail
        ) AS T
        CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
        CROSS APPLY ( SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND;

        IF NOT EXISTS (
               SELECT 1
               FROM #ins_norm
               WHERE ORDER_NO IS NOT NULL
                 AND LEN(ORDER_NO) > 0
           )
            RETURN;

        /* [3] 로그 테이블 명 준비(DECLARE 후 SET로 초기화) */
        DECLARE
              @CURR_YM   VARCHAR(6)
            , @PREV_YM   VARCHAR(6)
            , @CURR_RAW  NVARCHAR(128)
            , @PREV_RAW  NVARCHAR(128)
            , @CURR_Q    NVARCHAR(300)
            , @PREV_Q    NVARCHAR(300)
            , @SQL       NVARCHAR(MAX);

        SET @CURR_YM  = CONVERT(VARCHAR(6), GETDATE(), 112);
        SET @PREV_YM  = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112);

        SET @CURR_RAW = N'dbo.MMS_LOG_' + @CURR_YM;
        SET @PREV_RAW = N'dbo.MMS_LOG_' + @PREV_YM;

        SET @CURR_Q   = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @CURR_YM);
        SET @PREV_Q   = QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @PREV_YM);

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
                     CROSS APPLY (
                         SELECT CASE
                                    WHEN P.pos > 0 THEN SUBSTRING(MM.MSG, P.pos + LEN(N'오더번호'), 300)
                                    ELSE N''
                                END AS tail
                     ) AS T
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
                      AND MM.SUBJECT IN (
                              N'크나우프석고보드_보류오더 안내'
                            , N'크나우프석고보드_오더접수'
                          )
                      AND O.ORDER_NO  = I.ORDER_NO
               );

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
                              AND L.SUBJECT IN (
                                      N''크나우프석고보드_보류오더 안내''
                                    , N''크나우프석고보드_오더접수''
                                  )
                              AND O.ORDER_NO  = I.ORDER_NO
                       )';
            EXEC sys.sp_executesql @SQL;
        END;

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
                              AND L.SUBJECT IN (
                                      N''크나우프석고보드_보류오더 안내''
                                    , N''크나우프석고보드_오더접수''
                                  )
                              AND O.ORDER_NO  = I.ORDER_NO
                       )';
            EXEC sys.sp_executesql @SQL;
        END;
    END TRY
    BEGIN CATCH
        RETURN;
    END CATCH;
END;
GO
```

---

## 실행 체크리스트

1. **전체 스크립트**를 한 번에 실행(부분 선택 실행 금지).
2. `GO`는 **맨 마지막에만** 둔다.
3. 만약 여전히 같은 오류가 보이면, 트리거 내부 `BEGIN TRY…END TRY` 블록을 잠시 주석 처리하고 실행해서 **정확한 라인**을 보자.
4. 테스트는 간단히:

```sql
INSERT INTO dbo.MMS_MSG (MSGKEY, PHONE, SUBJECT, MSG)
VALUES (123, N'01012345678', N'크나우프석고보드_오더접수',
        N'[크나우프석고보드 오더접수내역]' + CHAR(13)+CHAR(10) + N'- 오더번호: 400418728' );

-- 같은 PHONE + 같은 오더번호 다시 INSERT → 새 행 STATUS가 3으로 바뀌어야 함
```

필요하면, **테스트 데이터 세트**(중복/비중복/경계)도 만들어서 같이 점검해줄게.

---------------------------------------------------------------------


# MMS_MSG 트리거 테스트 데이터 및 시나리오를 만들어줘.
테스트 순서: 1) 기본 데이터 생성 → 2) 트리거 동작 테스트 → 3) 결과 확인 순으로 진행하면 될 거 같은데, 누락되거나 틀린 점은 수정, 보완해서 작성해줘.

---------------------------------------------------------------------


좋아. **재현 가능한 전체 테스트 스크립트**를 준비했어.
트리거 로직(오더번호 범용 파싱 + 전화번호+오더번호 중복 차단)을 검증하도록 **시나리오별 데이터**를 넣고, **검증 SELECT**까지 포함했어.
기본 순서는 네가 원한 대로: **① 기본 데이터/환경 준비 → ② 트리거 동작 테스트 → ③ 결과 확인**이야.
실DB 오염 방지를 위해 **트랜잭션 랩핑(ROLLBACK 기본)** 으로 제공해.

> ⚠️ 가정: `dbo.MMS_MSG` 컬럼에 최소 `MSGKEY(IDENTITY 또는 수동지정 가능), PHONE, SUBJECT, MSG, STATUS` 가 존재.
> 로그 테이블은 `dbo.MMS_LOG_YYYYMM (PHONE, SUBJECT, MSG …)` 구조.
> 필요 시 컬럼명만 맞춰서 실행해.

---

# 0) 실행 방법

* 그대로 복붙해서 **한 번에** 실행.
* 기본은 `ROLLBACK TRAN`으로 끝남(깨끗하게 되돌림). 유지하고 싶으면 마지막 줄을 `COMMIT TRAN`으로 바꿔.

---

# ① 기본 데이터/환경 준비

```sql
BEGIN TRAN
SET NOCOUNT ON

PRINT N'[준비] 테스트용 현재/이전월 로그 테이블 준비'

DECLARE
      @CURR_YM     VARCHAR(6)
    , @PREV_YM     VARCHAR(6)
    , @CURR_RAW    NVARCHAR(128)
    , @PREV_RAW    NVARCHAR(128)
    , @SQL         NVARCHAR(MAX)

SET @CURR_YM  = CONVERT(VARCHAR(6), GETDATE(), 112)
SET @PREV_YM  = CONVERT(VARCHAR(6), DATEADD(MONTH, -1, GETDATE()), 112)
SET @CURR_RAW = N'dbo.MMS_LOG_' + @CURR_YM
SET @PREV_RAW = N'dbo.MMS_LOG_' + @PREV_YM

/* 현재월 로그 테이블 없으면 생성 */
IF OBJECT_ID(@CURR_RAW, N'U') IS NULL
BEGIN
    SET @SQL = N'
        CREATE TABLE ' + QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @CURR_YM) + N' (
               LOG_ID     INT IDENTITY(1,1) PRIMARY KEY
             , PHONE      NVARCHAR(20)   NOT NULL
             , SUBJECT    NVARCHAR(200)  NOT NULL
             , MSG        NVARCHAR(MAX)  NOT NULL
             , CREATED_AT DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME()
        )'
    EXEC sys.sp_executesql @SQL
END

/* 이전월 로그 테이블 없으면 생성 */
IF OBJECT_ID(@PREV_RAW, N'U') IS NULL
BEGIN
    SET @SQL = N'
        CREATE TABLE ' + QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @PREV_YM) + N' (
               LOG_ID     INT IDENTITY(1,1) PRIMARY KEY
             , PHONE      NVARCHAR(20)   NOT NULL
             , SUBJECT    NVARCHAR(200)  NOT NULL
             , MSG        NVARCHAR(MAX)  NOT NULL
             , CREATED_AT DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME()
        )'
    EXEC sys.sp_executesql @SQL
END

PRINT N'[준비] 테스트 식별 토큰(각 케이스 메시지 말미에 부착)'
DECLARE
      @TID1 NVARCHAR(36) = CONVERT(NVARCHAR(36), NEWID())  -- 케이스1
    , @TID2 NVARCHAR(36) = CONVERT(NVARCHAR(36), NEWID())  -- 케이스2
    , @TID3 NVARCHAR(36) = CONVERT(NVARCHAR(36), NEWID())  -- 케이스3
    , @TID4 NVARCHAR(36) = CONVERT(NVARCHAR(36), NEWID())  -- 서브스트링 오탐 방지
    , @TID5 NVARCHAR(36) = CONVERT(NVARCHAR(36), NEWID())  -- 현재월 로그 중복
    , @TID6 NVARCHAR(36) = CONVERT(NVARCHAR(36), NEWID())  -- 이전월 로그 중복
    , @TID7 NVARCHAR(36) = CONVERT(NVARCHAR(36), NEWID())  -- 교차 SUBJECT 중복
    , @TID8 NVARCHAR(36) = CONVERT(NVARCHAR(36), NEWID())  -- 전각 콜론 케이스

/* 테스트용 전화번호 / 오더번호 세트 */
DECLARE
      @P1 NVARCHAR(20) = N'01011112222'  -- 동일폰+동일오더 → 차단 기대
    , @P2 NVARCHAR(20) = N'01099998888'  -- 다른폰 → 허용 기대
    , @P3 NVARCHAR(20) = N'01033334444'  -- 현재월 로그 중복
    , @P4 NVARCHAR(20) = N'01044445555'  -- 이전월 로그 중복
    , @P5 NVARCHAR(20) = N'01055556666'  -- 교차 SUBJECT 중복
    , @P6 NVARCHAR(20) = N'01066667777'  -- 전각 콜론 케이스

DECLARE
      @ORD_SAME     NVARCHAR(20) = N'400418728'
    , @ORD_DIFF     NVARCHAR(20) = N'400418729'
    , @ORD_LONG     NVARCHAR(20) = N'12345'
    , @ORD_SHORT    NVARCHAR(20) = N'123'
    , @ORD_CURRLOG  NVARCHAR(20) = N'500000001'
    , @ORD_PREVLOG  NVARCHAR(20) = N'600000001'
    , @ORD_CROSS    NVARCHAR(20) = N'700000001'
    , @ORD_ZENKAKU  NVARCHAR(20) = N'800000001'  -- 전각 콜론 뒤 숫자
```

---

# ② 트리거 동작 테스트 (시나리오별 INSERT)

```sql
PRINT N'=============================================================='
PRINT N'[케이스 1] MMS_MSG 내부 중복: 동일 PHONE + 동일 오더번호 → 차단(STATUS=3) 기대'
/* (1-A) 선행건(기존 발송) 삽입: SUBJECT=오더접수, 오더번호: @ORD_SAME */
INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P1
    , N'크나우프석고보드_오더접수'
    , N'[크나우프석고보드 오더접수내역]' + CHAR(13)+CHAR(10)
      + N'- 오더번호: ' + @ORD_SAME + N' [TEST ' + @TID1 + N' BASE]'
    , N'0'
)

/* (1-B) 신규건: 동일 PHONE + 동일 오더번호(주소만 다름) → 트리거로 STATUS=3 */
INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P1
    , N'크나우프석고보드_오더접수'
    , N'[크나우프석고보드 오더접수내역]' + CHAR(13)+CHAR(10)
      + N'- 상세주소: 다른 주소' + CHAR(13)+CHAR(10)
      + N'- 오더번호: ' + @ORD_SAME + N' [TEST ' + @TID1 + N' NEW]'
    , N'0'
)

PRINT N'[케이스 2] 다른 전화번호 → 허용(STATUS!=3) 기대'
INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P2
    , N'크나우프석고보드_오더접수'
    , N'[크나우프석고보드 오더접수내역]' + CHAR(13)+CHAR(10)
      + N'- 오더번호: ' + @ORD_SAME + N' [TEST ' + @TID2 + N']'
    , N'0'
)

PRINT N'[케이스 3] 다른 오더번호 → 허용(STATUS!=3) 기대'
INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P1
    , N'크나우프석고보드_오더접수'
    , N'[크나우프석고보드 오더접수내역]' + CHAR(13)+CHAR(10)
      + N'- 오더번호: ' + @ORD_DIFF + N' [TEST ' + @TID3 + N']'
    , N'0'
)

PRINT N'[케이스 4] 서브스트링 오탐 방지: 123 vs 12345 → 차단 금지 기대'
/* 선행건: 12345 */
INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P1
    , N'크나우프석고보드_오더접수'
    , N'- 오더번호: ' + @ORD_LONG + N' [TEST ' + @TID4 + N' BASE]'
    , N'0'
)
/* 신규건: 123 → 서로 달라서 차단 금지 */
INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P1
    , N'크나우프석고보드_오더접수'
    , N'- 오더번호: ' + @ORD_SHORT + N' [TEST ' + @TID4 + N' NEW]'
    , N'0'
)

PRINT N'[케이스 5] 현재월 로그 중복: 로그에 동일 PHONE+오더번호가 있으면 차단 기대'
SET @SQL = N'
    INSERT INTO ' + QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @CURR_YM) + N' (PHONE, SUBJECT, MSG)
    VALUES (N''' + @P3 + N''', N''크나우프석고보드_보류오더 안내'', N''- 오더번호 ' + @ORD_CURRLOG + N' [TEST ' + @TID5 + N' LOG]'')'
EXEC sys.sp_executesql @SQL

/* 신규건: SUBJECT가 오더접수여도, PHONE+오더번호만 같으면 차단 */
INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P3
    , N'크나우프석고보드_오더접수'
    , N'- 오더번호: ' + @ORD_CURRLOG + N' [TEST ' + @TID5 + N' NEW]'
    , N'0'
)

PRINT N'[케이스 6] 이전월 로그 중복: 차단 기대'
SET @SQL = N'
    INSERT INTO ' + QUOTENAME(N'dbo') + N'.' + QUOTENAME(N'MMS_LOG_' + @PREV_YM) + N' (PHONE, SUBJECT, MSG)
    VALUES (N''' + @P4 + N''', N''크나우프석고보드_오더접수'', N''- 오더번호: ' + @ORD_PREVLOG + N' [TEST ' + @TID6 + N' LOG]'')'
EXEC sys.sp_executesql @SQL

INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P4
    , N'크나우프석고보드_보류오더 안내'
    , N'- 오더번호 ' + @ORD_PREVLOG + N' [TEST ' + @TID6 + N' NEW]'
    , N'0'
)

PRINT N'[케이스 7] 교차 SUBJECT 중복: (보류 → 접수) 서로 다른 SUBJECT라도 차단 기대'
/* 선행건: 보류오더 안내 + 오더번호 @ORD_CROSS */
INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P5
    , N'크나우프석고보드_보류오더 안내'
    , N'- 오더번호 ' + @ORD_CROSS + N' [TEST ' + @TID7 + N' BASE]'
    , N'0'
)
/* 신규건: 오더접수 + 같은 오더번호 → 차단 */
INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P5
    , N'크나우프석고보드_오더접수'
    , N'- 오더번호: ' + @ORD_CROSS + N' [TEST ' + @TID7 + N' NEW]'
    , N'0'
)

PRINT N'[케이스 8] 전각 콜론(：) 등 변형: 범용 파싱이므로 차단 기대(선행+신규로 비교)'
/* 선행건: 전각 콜론 사용 */
INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P6
    , N'크나우프석고보드_오더접수'
    , N'- 오더번호： ' + @ORD_ZENKAKU + N' [TEST ' + @TID8 + N' BASE]'
    , N'0'
)
/* 신규건: 일반 콜론 → 같은 오더번호 → 차단 */
INSERT INTO dbo.MMS_MSG (PHONE, SUBJECT, MSG, STATUS)
VALUES (
      @P6
    , N'크나우프석고보드_오더접수'
    , N'- 오더번호: ' + @ORD_ZENKAKU + N' [TEST ' + @TID8 + N' NEW]'
    , N'0'
)
```

---

# ③ 결과 확인 (추출 오더번호까지 확인용 뷰 SELECT)

아래 SELECT는 트리거의 파싱과 동일한 방식으로 **오더번호를 뽑아 보여줘**.
`STATUS = '3'`이 기대되는 행에 제대로 찍혔는지 한 눈에 확인.

```sql
PRINT N'=============================================================='
PRINT N'[검증] TEST 토큰이 포함된 행만 조회 (오더번호 추출 포함)'

;WITH X AS (
    SELECT
           M.MSGKEY
         , M.PHONE
         , M.SUBJECT
         , M.STATUS
         , M.MSG
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
           END AS EXTRACTED_ORDER_NO
    FROM dbo.MMS_MSG AS M
    CROSS APPLY ( SELECT CHARINDEX(N'[TEST ', M.MSG) AS hasTest ) AS TK
    CROSS APPLY ( SELECT CHARINDEX(N'오더번호', M.MSG) AS pos ) AS P
    CROSS APPLY (
        SELECT CASE
                   WHEN P.pos > 0 THEN SUBSTRING(M.MSG, P.pos + LEN(N'오더번호'), 300)
                   ELSE N''
               END AS tail
    ) AS T
    CROSS APPLY ( SELECT PATINDEX(N'%[0-9]%', T.tail) AS dstart ) AS D
    CROSS APPLY ( SELECT PATINDEX(N'%[^0-9]%', SUBSTRING(T.tail, D.dstart, 50)) AS nextNonDigitPos ) AS ND
    WHERE TK.hasTest > 0
)
SELECT
       X.MSGKEY
     , X.PHONE
     , X.SUBJECT
     , X.EXTRACTED_ORDER_NO
     , X.STATUS
     , LEFT(X.MSG, 200) AS MSG_HEAD
FROM X
ORDER BY
       X.MSGKEY DESC
```

---

## ✅ 기대 결과 요약

| 케이스   | 설명                       | 기대 STATUS |
| ----- | ------------------------ | --------- |
| 1-B   | 동일폰 + 동일오더 (MMS\_MSG 내부) | `3`       |
| 2     | 다른폰                      | `3` 아님    |
| 3     | 다른 오더번호                  | `3` 아님    |
| 4 NEW | 123 vs 12345 (부분일치 방지)   | `3` 아님    |
| 5 NEW | 현재월 로그에 동일폰+오더           | `3`       |
| 6 NEW | 이전월 로그에 동일폰+오더           | `3`       |
| 7 NEW | SUBJECT 달라도 동일폰+오더       | `3`       |
| 8 NEW | 전각 콜론/일반 콜론 혼용           | `3`       |

---

## 🔚 마무리 (정리/롤백)

```sql
-- 기본은 ROLLBACK (DB 원복). 유지하려면 COMMIT으로 바꾸기.
ROLLBACK TRAN
-- COMMIT TRAN
```

---

필요하면 위 스크립트를 **COMMIT 버전**으로 바꿔서 제공하거나,
실제 스키마(컬럼 타입/제약) 맞춤형으로 **컬럼 목록 명시 INSERT** 버전도 만들어줄게.

