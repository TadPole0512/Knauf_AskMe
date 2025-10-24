아래 문서는 “엑셀 업로드 기반 주문 상태 업데이트 시스템”의 **2단계: 데이터베이스 설계 및 구축**을 실제 운영 가능한 수준으로 정리한 것입니다.
(MSSQL 2019, 표준 에디션/64bit, 애플리케이션은 Java+MyBatis 가정)

---

# 먼저, 어떻게 생각해서 이렇게 설계했는지 (요청하신 사고 프레임 적용 요약)

* **Genius Insight (GI)**:
  관찰 O=8 (요구 핵심인 업로드/검증/진행률/오류를 포착), 연결 C=8 (EAV+고정컬럼 혼합), 패턴 P=9 (대용량 업로드의 보편 패턴: 세션·이벤트·오류로그·정리), 종합 S=8, 편견 A=2, 바이어스 B=2 → **GI = (8×8×9×8)/(2+2) ≈ 115.2**.
  → 고정 필수컬럼 + 메타기반 가변컬럼 + 세션/오류/진행률 4축으로 설계.

* **MDA(다차원 분석)**:
  D1 시간축(과거 오류 재현/향후 확장) W=0.3, D2 공간(업로드 세션 단위 격리) W=0.25, D3 추상(코드/메타데이터) W=0.15, D4 인과(검증→적용→리포트) W=0.2, D5 계층(레퍼런스/스테이징/옵스) W=0.1 → 균형 있게 반영.

* **Creative Connection (CC)**:
  “동적 컬럼” ↔ “임시 테이블” 직접 연결 + “EAV/메타” 간접 연결을 병행 제시(권장안과 대안안을 **동시 제공**).

* **Problem Redefinition (PR)**:
  ‘동적으로 테이블을 매번 생성’ 문제를 180° 전환 → ‘**세션키로 격리되는 단일 스테이징 + 메타값 테이블**’로 재정의, 운영 복잡도↓.

* **IS(혁신해법)**:
  조합(고정+EAV), 참신성(필터드 인덱스/세션별 RLS 옵션), 실현성(표준 T-SQL), 가치(운영 편의/확장성), 리스크(동적컬럼 검색성) → 리스크는 **메타 기반 인덱싱/캐싱 필드**로 완화.

---

# 1) 단계별 진행 절차

1. **스키마/명명 규칙 확정**

   * 스키마 구분: `ref`(코드/사전), `stg`(업로드 스테이징), `ops`(세션/진행률/오류).
   * 명명: 단수형 테이블, `snake_case`, 제약/인덱스 접두사 `PK_/FK_/IX_/CK_/DF_`.

2. **필수 컬럼 정의(고정 컬럼)**

   * `sales_document`, `sales_document_item`, `reason_for_rejection_code`, `ds_code`, `os_code`를 **행 단위 필수**로 설계.

3. **가변 컬럼 설계(메타/EAV)**

   * `stg.flex_field_def`(필드 정의) ↔ `stg.stage_order_flex_value`(값 저장).
   * 데이터 타입 안전성: `CHECK` + **타입별 저장 슬롯**(int/decimal/date/datetime/bit/text) + **필터드 인덱스**.

4. **스테이징 테이블 설계**

   * `stg.stage_order_fixed`(한 행 = 업로드 한 줄) + 세션키/행번호/원시 텍스트/정규화 상태.

5. **세션/진행률 추적 설계**

   * `ops.upload_session`(세션 헤더)
   * `ops.upload_step_progress`(단계 이벤트 로그, 실시간 인덱스)
   * **활성 세션 필터드 인덱스**로 실시간 대시보드 최적화.

6. **오류 리포트 설계**

   * `ops.error_report` + `ref.failure_type`(분류 체계).
   * 행/필드/값/단계/심각도/메시지/코드 저장, 다각도 조회 인덱스.

7. **코드/사전 테이블**

   * `ref.reason_for_rejection`, `ref.status_code`(도메인: DS/OS 등)로 FK 보장.

8. **임시 테이블 운용 규칙 & 자동 정리**

   * 권장: **세션 키 기반 단일 스테이징**.
   * 대안(선택): 세션별 물리 임시 테이블 명명 `#stg_order_upload_{yyyyMMddHHmmss}_{spid}`.
   * **자동 정리**: 완료/실패 후 N일 경과 데이터 배치 삭제.

9. **권한/보안/감사**

   * 최소 권한 롤, RLS(옵션)로 세션 소유자만 조회.

10. **성능/운영 점검 체크리스트**

* 배치 삭제 LOOP, 통계 업데이트, 오류 유형/빈도 리포트 뷰.

---

# 2) 테이블 목록 및 역할 정의 + 관계(ERD 개념)

## 스키마: `ref` (참조/사전)

* **ref.reason\_for\_rejection**: 거절사유 코드 사전.
* **ref.status\_code**: 상태 코드(DS/OS) 사전, 도메인 구분.
* **ref.failure\_type**: 오류 유형 분류 사전.

## 스키마: `stg` (스테이징)

* **stg.stage\_order\_fixed**: 업로드 원시 한 줄 + 필수 고정 컬럼 + 세션키.
* **stg.flex\_field\_def**: 가변 필드 메타(코드/라벨/유형/필수 여부 등).
* **stg.stage\_order\_flex\_value**: EAV 값 저장(행기반 슬롯).

## 스키마: `ops` (운영/로그)

* **ops.upload\_session**: 업로드 세션 헤더(상태, 소유자, 파일명, 시각).
* **ops.upload\_step\_progress**: 단계별 진행 이벤트 로그(퍼센트/메시지).
* **ops.error\_report**: 오류 리포트(행/필드/유형/메시지/값/단계).

### ERD 개념(텍스트)

```
ref.status_code (domain: 'DS'/'OS') ─┐
ref.reason_for_rejection ────────────┼──< stg.stage_order_fixed >──┐
ref.failure_type ─────────────────────┘                            │
stg.flex_field_def ───< stg.stage_order_flex_value >───────────────┤
ops.upload_session ───< stg.stage_order_fixed (by session_id)      │
ops.upload_session ───< ops.upload_step_progress                   │
ops.upload_session ───< ops.error_report                           │
ops.error_report ──── FK failure_type / field_code(meta optional) ─┘
```

---

# 3) 테이블 구조(컬럼/타입/제약/설명)

> 시간 필드 기본은 **UTC** 저장(`datetime2(3)` + `SYSUTCDATETIME()`), 앱에서 KST 변환.

## ref.reason\_for\_rejection

* `reason_for_rejection_code` NVARCHAR(4) PK — 사유 코드
* `reason_for_rejection_name` NVARCHAR(100) NOT NULL — 명칭
* `is_active` BIT NOT NULL DEFAULT 1
* `created_at`/`updated_at` DATETIME2(3) — 관리용

## ref.status\_code

* `status_code_id` BIGINT IDENTITY PK
* `domain` NVARCHAR(10) NOT NULL — ‘DS’(Delivery Status), ‘OS’(Order Status)
* `status_code` NVARCHAR(10) NOT NULL UNIQUE(domain+status\_code)
* `status_name` NVARCHAR(100) NOT NULL
* `is_active` BIT NOT NULL DEFAULT 1
* 타당성: `CHECK (domain IN ('DS','OS'))`

## ref.failure\_type

* `failure_type_code` NVARCHAR(32) PK — 예: VALIDATION, FK\_MISS, FORMAT, APPLY\_FAIL
* `failure_type_name` NVARCHAR(100) NOT NULL

## ops.upload\_session

* `session_id` UNIQUEIDENTIFIER PK (업로드 세션 키)
* `session_no` BIGINT IDENTITY UNIQUE — 가독성 번호
* `owner_user_id` NVARCHAR(64) NOT NULL
* `source_file_name` NVARCHAR(255) NULL
* `total_rows` INT NULL
* `status` CHAR(1) NOT NULL DEFAULT 'R' — R\:Running, C\:Completed, F\:Failed, X\:Canceled
* `started_at`/`ended_at` DATETIME2(3)
* `last_heartbeat_at` DATETIME2(3)
* `note` NVARCHAR(4000) NULL
* `CK`: status in (‘R’,’C’,’F’,’X’)

## ops.upload\_step\_progress

* `progress_id` BIGINT IDENTITY PK
* `session_id` UNIQUEIDENTIFIER FK → ops.upload\_session
* `step_code` NVARCHAR(32) NOT NULL — UPLOAD/PARSE/VALIDATE/APPLY/FINALIZE
* `status` CHAR(1) NOT NULL DEFAULT 'R' — R/P/C/F
* `progress_percent` TINYINT NOT NULL DEFAULT 0 (0\~100)
* `message` NVARCHAR(1000) NULL
* `created_at` DATETIME2(3) NOT NULL DEFAULT SYSUTCDATETIME()

> 실시간 조회 인덱스:
>
> * 활성 진행 세션용 필터드 인덱스 `WHERE status IN ('R','P')`
> * 세션 타임라인용 (session\_id, created\_at)

## ops.error\_report

* `error_id` BIGINT IDENTITY PK
* `session_id` UNIQUEIDENTIFIER FK
* `row_no` INT NULL — 업로드 원본 행번호
* `field_code` NVARCHAR(64) NULL — 메타필드 코드(가변컬럼인 경우)
* `failure_type_code` NVARCHAR(32) FK → ref.failure\_type
* `stage` NVARCHAR(32) NOT NULL — PARSE/VALIDATE/APPLY 등
* `severity` TINYINT NOT NULL DEFAULT 2 — 1\:Info,2\:Warn,3\:Error
* `error_code` NVARCHAR(32) NULL — 내부/도메인 오류 코드
* `error_message` NVARCHAR(1000) NOT NULL
* `raw_value` NVARCHAR(4000) NULL
* `created_at` DATETIME2(3) NOT NULL DEFAULT SYSUTCDATETIME()
* 인덱스: (session\_id, row\_no), (failure\_type\_code), (stage, severity)

## stg.flex\_field\_def

* `field_code` NVARCHAR(64) PK — EX: CUSTOM\_NOTE, REQUESTED\_DATE
* `field_label` NVARCHAR(128) NOT NULL
* `data_type` NVARCHAR(16) NOT NULL — NVARCHAR/INT/NUMERIC/DATE/DATETIME/BIT
* `max_length` INT NULL — 문자형 길이
* `numeric_precision` TINYINT NULL, `numeric_scale` TINYINT NULL
* `is_required` BIT NOT NULL DEFAULT 0
* `is_searchable` BIT NOT NULL DEFAULT 0
* `is_active` BIT NOT NULL DEFAULT 1
* `created_at`/`updated_at` DATETIME2(3)
* `CHECK` 제약으로 타입 값 한정

## stg.stage\_order\_fixed

* `staging_id` BIGINT IDENTITY PK
* `session_id` UNIQUEIDENTIFIER FK
* `row_no` INT NOT NULL
* **필수 컬럼(요구사항)**

  * `sales_document` NVARCHAR(20) NOT NULL
  * `sales_document_item` NVARCHAR(6) NOT NULL
  * `reason_for_rejection_code` NVARCHAR(4) NULL FK → ref.reason\_for\_rejection
  * `ds_code` NVARCHAR(10) NULL FK → ref.status\_code(domain='DS')
  * `os_code` NVARCHAR(10) NULL FK → ref.status\_code(domain='OS')
* **보조 컬럼**

  * `raw_line_hash` BINARY(20) PERSISTED (SHA1 해시; 생성식은 뷰/트리거/ETL에서 세팅)
  * `parsed_ok` BIT NOT NULL DEFAULT 0
  * `validated_ok` BIT NOT NULL DEFAULT 0
  * `applied_ok` BIT NOT NULL DEFAULT 0
  * `created_at` DATETIME2(3) DEFAULT SYSUTCDATETIME()
* 유니크 후보: (session\_id, row\_no)

## stg.stage\_order\_flex\_value

* `flex_value_id` BIGINT IDENTITY PK
* `staging_id` BIGINT FK → stg.stage\_order\_fixed
* `field_code` NVARCHAR(64) FK → stg.flex\_field\_def
* **값 슬롯 (하나만 사용)**

  * `text_value` NVARCHAR(4000) NULL
  * `int_value` INT NULL
  * `decimal_value` DECIMAL(38,10) NULL
  * `date_value` DATE NULL
  * `datetime_value` DATETIME2(3) NULL
  * `bit_value` BIT NULL
* `created_at` DATETIME2(3) DEFAULT SYSUTCDATETIME()
* 인덱스:

  * (staging\_id)
  * (field\_code)
  * **필터드 인덱스**: (int\_value) WHERE int\_value IS NOT NULL, …(decimal\_value), …(date\_value) 등.

---

# 4) 테이블 생성 쿼리(SQL, MSSQL 2019, Knauf-SQL-Style v1.1 준수)

> 스타일 가정: 대문자 키워드, 스키마 접두, 스네이크 케이스, 제약/인덱스 명시, 4스페이스 들여쓰기, 배치 구분 `GO`.

```sql
/* ===== 스키마 구성 ===== */
IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'ref')
    EXEC('CREATE SCHEMA ref');
IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'stg')
    EXEC('CREATE SCHEMA stg');
IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'ops')
    EXEC('CREATE SCHEMA ops');
GO

/* ===== ref.reason_for_rejection ===== */
CREATE TABLE ref.reason_for_rejection
(
    reason_for_rejection_code   NVARCHAR(4)  NOT NULL,
    reason_for_rejection_name   NVARCHAR(100) NOT NULL,
    is_active                   BIT           NOT NULL CONSTRAINT DF_rfr_is_active DEFAULT (1),
    created_at                  DATETIME2(3)  NOT NULL CONSTRAINT DF_rfr_created_at DEFAULT (SYSUTCDATETIME()),
    updated_at                  DATETIME2(3)  NULL,
    CONSTRAINT PK_reason_for_rejection PRIMARY KEY (reason_for_rejection_code)
);
GO

/* ===== ref.status_code ===== */
CREATE TABLE ref.status_code
(
    status_code_id  BIGINT        NOT NULL IDENTITY(1,1),
    domain          NVARCHAR(10)  NOT NULL,
    status_code     NVARCHAR(10)  NOT NULL,
    status_name     NVARCHAR(100) NOT NULL,
    is_active       BIT           NOT NULL CONSTRAINT DF_rsc_is_active DEFAULT (1),
    created_at      DATETIME2(3)  NOT NULL CONSTRAINT DF_rsc_created_at DEFAULT (SYSUTCDATETIME()),
    updated_at      DATETIME2(3)  NULL,
    CONSTRAINT PK_status_code PRIMARY KEY (status_code_id),
    CONSTRAINT UQ_status_code_domain_code UNIQUE (domain, status_code),
    CONSTRAINT CK_status_code_domain CHECK (domain IN ('DS','OS'))
);
GO

/* ===== ref.failure_type ===== */
CREATE TABLE ref.failure_type
(
    failure_type_code NVARCHAR(32)  NOT NULL,
    failure_type_name NVARCHAR(100) NOT NULL,
    CONSTRAINT PK_failure_type PRIMARY KEY (failure_type_code)
);
GO

/* ===== ops.upload_session ===== */
CREATE TABLE ops.upload_session
(
    session_id          UNIQUEIDENTIFIER NOT NULL,
    session_no          BIGINT           NOT NULL IDENTITY(1000,1),
    owner_user_id       NVARCHAR(64)     NOT NULL,
    source_file_name    NVARCHAR(255)    NULL,
    total_rows          INT              NULL,
    status              CHAR(1)          NOT NULL CONSTRAINT DF_ous_status DEFAULT ('R'),
    started_at          DATETIME2(3)     NOT NULL CONSTRAINT DF_ous_started_at DEFAULT (SYSUTCDATETIME()),
    ended_at            DATETIME2(3)     NULL,
    last_heartbeat_at   DATETIME2(3)     NULL,
    note                NVARCHAR(4000)   NULL,
    CONSTRAINT PK_upload_session PRIMARY KEY (session_id),
    CONSTRAINT UQ_upload_session_no UNIQUE (session_no),
    CONSTRAINT CK_upload_session_status CHECK (status IN ('R','C','F','X'))
);
GO

/* ===== ops.upload_step_progress ===== */
CREATE TABLE ops.upload_step_progress
(
    progress_id       BIGINT         NOT NULL IDENTITY(1,1),
    session_id        UNIQUEIDENTIFIER NOT NULL,
    step_code         NVARCHAR(32)   NOT NULL,
    status            CHAR(1)        NOT NULL CONSTRAINT DF_oup_status DEFAULT ('R'),
    progress_percent  TINYINT        NOT NULL CONSTRAINT DF_oup_percent DEFAULT (0),
    message           NVARCHAR(1000) NULL,
    created_at        DATETIME2(3)   NOT NULL CONSTRAINT DF_oup_created_at DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_upload_step_progress PRIMARY KEY (progress_id),
    CONSTRAINT FK_oup_session FOREIGN KEY (session_id)
        REFERENCES ops.upload_session(session_id),
    CONSTRAINT CK_oup_status CHECK (status IN ('R','P','C','F')),
    CONSTRAINT CK_oup_percent CHECK (progress_percent BETWEEN 0 AND 100)
);
GO

/* 실시간 조회 인덱스 */
CREATE INDEX IX_oup_session_created
    ON ops.upload_step_progress (session_id, created_at);
GO

CREATE INDEX IX_oup_active
    ON ops.upload_step_progress (session_id, step_code)
    WHERE status IN ('R','P');
GO

/* ===== ops.error_report ===== */
CREATE TABLE ops.error_report
(
    error_id           BIGINT          NOT NULL IDENTITY(1,1),
    session_id         UNIQUEIDENTIFIER NOT NULL,
    row_no             INT             NULL,
    field_code         NVARCHAR(64)    NULL,
    failure_type_code  NVARCHAR(32)    NOT NULL,
    stage              NVARCHAR(32)    NOT NULL,
    severity           TINYINT         NOT NULL CONSTRAINT DF_oer_severity DEFAULT (2),
    error_code         NVARCHAR(32)    NULL,
    error_message      NVARCHAR(1000)  NOT NULL,
    raw_value          NVARCHAR(4000)  NULL,
    created_at         DATETIME2(3)    NOT NULL CONSTRAINT DF_oer_created_at DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_error_report PRIMARY KEY (error_id),
    CONSTRAINT FK_oer_session FOREIGN KEY (session_id)
        REFERENCES ops.upload_session(session_id),
    CONSTRAINT FK_oer_failure_type FOREIGN KEY (failure_type_code)
        REFERENCES ref.failure_type(failure_type_code),
    CONSTRAINT CK_oer_severity CHECK (severity IN (1,2,3))
);
GO

CREATE INDEX IX_oer_session_row
    ON ops.error_report (session_id, row_no);
GO

CREATE INDEX IX_oer_failure_type
    ON ops.error_report (failure_type_code);
GO

CREATE INDEX IX_oer_stage_severity
    ON ops.error_report (stage, severity);
GO

/* ===== stg.flex_field_def ===== */
CREATE TABLE stg.flex_field_def
(
    field_code         NVARCHAR(64)  NOT NULL,
    field_label        NVARCHAR(128) NOT NULL,
    data_type          NVARCHAR(16)  NOT NULL,
    max_length         INT           NULL,
    numeric_precision  TINYINT       NULL,
    numeric_scale      TINYINT       NULL,
    is_required        BIT           NOT NULL CONSTRAINT DF_sfd_is_required DEFAULT (0),
    is_searchable      BIT           NOT NULL CONSTRAINT DF_sfd_is_searchable DEFAULT (0),
    is_active          BIT           NOT NULL CONSTRAINT DF_sfd_is_active DEFAULT (1),
    created_at         DATETIME2(3)  NOT NULL CONSTRAINT DF_sfd_created_at DEFAULT (SYSUTCDATETIME()),
    updated_at         DATETIME2(3)  NULL,
    CONSTRAINT PK_flex_field_def PRIMARY KEY (field_code),
    CONSTRAINT CK_sfd_data_type CHECK (data_type IN ('NVARCHAR','INT','NUMERIC','DATE','DATETIME','BIT'))
);
GO

/* ===== stg.stage_order_fixed ===== */
CREATE TABLE stg.stage_order_fixed
(
    staging_id                 BIGINT           NOT NULL IDENTITY(1,1),
    session_id                 UNIQUEIDENTIFIER NOT NULL,
    row_no                     INT              NOT NULL,
    sales_document             NVARCHAR(20)     NOT NULL,
    sales_document_item        NVARCHAR(6)      NOT NULL,
    reason_for_rejection_code  NVARCHAR(4)      NULL,
    ds_code                    NVARCHAR(10)     NULL,
    os_code                    NVARCHAR(10)     NULL,
    raw_line_hash              BINARY(20)       NULL,
    parsed_ok                  BIT              NOT NULL CONSTRAINT DF_sof_parsed DEFAULT (0),
    validated_ok               BIT              NOT NULL CONSTRAINT DF_sof_validated DEFAULT (0),
    applied_ok                 BIT              NOT NULL CONSTRAINT DF_sof_applied DEFAULT (0),
    created_at                 DATETIME2(3)     NOT NULL CONSTRAINT DF_sof_created_at DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_stage_order_fixed PRIMARY KEY (staging_id),
    CONSTRAINT UQ_sof_session_row UNIQUE (session_id, row_no),
    CONSTRAINT FK_sof_session FOREIGN KEY (session_id)
        REFERENCES ops.upload_session(session_id),
    CONSTRAINT FK_sof_reason FOREIGN KEY (reason_for_rejection_code)
        REFERENCES ref.reason_for_rejection(reason_for_rejection_code)
        ON UPDATE NO ACTION ON DELETE NO ACTION
);
GO

/* 상태코드 도메인 FK를 뷰/트리거/검증에서 보장하거나, 아래처럼 FK + 컴퓨티드 도메인키로 엄격 보장(선택) */
/* 간단히 인덱스만 부여 */
CREATE INDEX IX_sof_sales_key
    ON stg.stage_order_fixed (sales_document, sales_document_item);
GO

CREATE INDEX IX_sof_status
    ON stg.stage_order_fixed (ds_code, os_code)
    INCLUDE (reason_for_rejection_code);
GO

/* ===== stg.stage_order_flex_value ===== */
CREATE TABLE stg.stage_order_flex_value
(
    flex_value_id  BIGINT        NOT NULL IDENTITY(1,1),
    staging_id     BIGINT        NOT NULL,
    field_code     NVARCHAR(64)  NOT NULL,
    text_value     NVARCHAR(4000) NULL,
    int_value      INT           NULL,
    decimal_value  DECIMAL(38,10) NULL,
    date_value     DATE          NULL,
    datetime_value DATETIME2(3)  NULL,
    bit_value      BIT           NULL,
    created_at     DATETIME2(3)  NOT NULL CONSTRAINT DF_sfv_created_at DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_stage_order_flex_value PRIMARY KEY (flex_value_id),
    CONSTRAINT FK_sfv_staging FOREIGN KEY (staging_id)
        REFERENCES stg.stage_order_fixed(staging_id)
        ON DELETE CASCADE,
    CONSTRAINT FK_sfv_field FOREIGN KEY (field_code)
        REFERENCES stg.flex_field_def(field_code)
);
GO

CREATE INDEX IX_sfv_staging
    ON stg.stage_order_flex_value (staging_id);
GO

CREATE INDEX IX_sfv_field
    ON stg.stage_order_flex_value (field_code);
GO

/* 타입별 필터드 인덱스 */
CREATE INDEX IX_sfv_int
    ON stg.stage_order_flex_value (field_code, int_value)
    WHERE int_value IS NOT NULL;
GO
CREATE INDEX IX_sfv_decimal
    ON stg.stage_order_flex_value (field_code, decimal_value)
    WHERE decimal_value IS NOT NULL;
GO
CREATE INDEX IX_sfv_date
    ON stg.stage_order_flex_value (field_code, date_value)
    WHERE date_value IS NOT NULL;
GO
CREATE INDEX IX_sfv_datetime
    ON stg.stage_order_flex_value (field_code, datetime_value)
    WHERE datetime_value IS NOT NULL;
GO
CREATE INDEX IX_sfv_bit
    ON stg.stage_order_flex_value (field_code, bit_value)
    WHERE bit_value IS NOT NULL;
GO
```

> **대안안(선택, 권장 X)**: 세션별 물리 임시 테이블을 `PROC`에서 생성하는 경우
> 명명 규칙: `#stg_order_upload_{format( sysdatetime(), 'yyyyMMddHHmmss' )}_{@@SPID}`
> 단, 인덱스/통계/권한/감사가 불편하고, 분기 처리 복잡도 ↑. 가능하면 본 문서의 **메타+단일 스테이징** 패턴을 사용하세요.

---

# 5) 추가 고려사항 (무결성/성능/보안/확장)

## 무결성

* **코드 참조 무결성**: `reason_for_rejection_code`, `ds_code`, `os_code`는 사전 테이블로 관리.
  DS/OS는 `ref.status_code(domain)`로 관리(엄격 FK가 필요하면 별도 뷰/체크/트리거로 도메인 보강).
* **데이터 타입 유효성**: 가변 컬럼은 `flex_field_def.data_type`에 따라 로더에서 **단일 슬롯만 채움**(TRY\_CONVERT 검사 + 오류로그).
* **중복 방지**: (session\_id, row\_no) 유니크. 추가로 `raw_line_hash`로 원시중복 탐지 가능.

## 성능

* **필터드 인덱스**로 활성 진행/타입별 조회 최적화.
* **배치 삭제**: 오래된 세션/스테이징/오류 로그는 TOP N 루프 방식으로 청소.
* **통계/인덱스 유지**: 주 단위 리빌드/리오가나이즈 정책.
* **격리수준**: 데이터베이스 옵션 `READ_COMMITTED_SNAPSHOT ON` 권장 → 업로드/대시보드 간 락 경합 완화.

```sql
ALTER DATABASE CURRENT SET READ_COMMITTED_SNAPSHOT ON WITH ROLLBACK IMMEDIATE;
```

## 보안

* **최소 권한 롤**: `db_role_app_uploader` 생성 → 필요한 테이블만 `SELECT/INSERT/UPDATE`.
* **행 수준 보안(RLS, 선택)**: 세션 소유자만 본인 세션 조회 허용.

```sql
-- 예시: 세션 행 보안
CREATE FUNCTION ops.fn_rls_session_owner (@session_id UNIQUEIDENTIFIER)
RETURNS TABLE WITH SCHEMABINDING
AS RETURN
    SELECT 1 AS allow_result
    FROM ops.upload_session s
    WHERE s.session_id = @session_id
      AND s.owner_user_id = SUSER_SNAME(); -- 또는 애플리케이션 보안 컨텍스트

CREATE SECURITY POLICY ops.security_policy_session
ADD FILTER PREDICATE ops.fn_rls_session_owner(session_id) ON ops.upload_session,
ADD FILTER PREDICATE ops.fn_rls_session_owner(session_id) ON ops.upload_step_progress,
ADD FILTER PREDICATE ops.fn_rls_session_owner(session_id) ON ops.error_report,
ADD FILTER PREDICATE ops.fn_rls_session_owner(session_id) ON stg.stage_order_fixed
WITH (STATE = ON);
```

## 확장성

* **가변 필드 추가**: `stg.flex_field_def`에 행 추가만으로 확장, DDL 불필요.
* **규모 확장**: 세션 파티셔닝 키로 보관주기 분리, 필요 시 아카이브 DB로 이관.
* **검색 최적화**: 자주 조회되는 가변필드는 **머티리얼라이즈드 컬럼(계산/동기화)** 또는 **뷰**로 노출.

## 자동 정리(예시 배치)

```sql
/* 완료/실패 세션 7일 이후 청소 */
DECLARE @batch INT = 5000;

WHILE 1=1
BEGIN
    ;WITH old_sessions AS
    (
        SELECT TOP (@batch) session_id
        FROM ops.upload_session
        WHERE status IN ('C','F','X')
          AND ISNULL(ended_at, started_at) < DATEADD(DAY, -7, SYSUTCDATETIME())
        ORDER BY ISNULL(ended_at, started_at)
    )
    DELETE FROM ops.error_report WHERE session_id IN (SELECT session_id FROM old_sessions);

    DELETE fv
    FROM stg.stage_order_flex_value fv
    JOIN stg.stage_order_fixed f ON fv.staging_id = f.staging_id
    WHERE f.session_id IN (SELECT session_id FROM old_sessions);

    DELETE FROM stg.stage_order_fixed
    WHERE session_id IN (SELECT session_id FROM old_sessions);

    DELETE FROM ops.upload_step_progress
    WHERE session_id IN (SELECT session_id FROM old_sessions);

    DELETE FROM ops.upload_session
    WHERE session_id IN (SELECT session_id FROM old_sessions);

    IF @@ROWCOUNT = 0 BREAK;
END
```

---

# 6) 보강/수정 사항 + 추가 질문

## 보강 제안

1. **상태코드 도메인 FK 강화**
   `ds_code`/`os_code` → `ref.status_code`의 (domain, status\_code) 복합키를 참조하도록 별도 키 테이블을 두거나, **컴퓨티드 컬럼으로 domain 고정 + FK**(고급) 적용 가능.
2. **적용 결과 로깅**
   운영 테이블 업데이트 결과(몇 행 업데이트/스킵/거절) 요약을 `ops.upload_step_progress.message`에 JSON으로 남기면 대시보드 구성에 유리.
3. **유효성 템플릿**
   `flex_field_def`에 `regex` 또는 `allowed_values_json`을 추가해 **정규식/셋** 검증 자동화 가능.
4. **샘플 뷰**
   에러 요약 뷰, 세션 요약 뷰를 만들어 JQGrid에 바로 바인딩.

## 필요한 추가 정보(주시면 반영해 최종 문서 갱신)

* **DS/OS 코드 체계**: 코드 목록/설명(예: DS=Delivered/InTransit…, OS=Open/Closed…)과 길이/포맷.
* **엑셀 컬럼 구성**: 필수/선택, 가변 컬럼 후보, 데이터 타입, 최대 길이.
* **업데이트 대상 운영 테이블**: 실제 주문/주문아이템 테이블 명/PK/업데이트 규칙(UPSERT? PARTIAL UPDATE?).
* **동시 접속/건수 규모**: 동시 세션 수, 1세션 최대 행 수(인덱스/배치 파라미터 튜닝).
* **정리 보관기간**: 에러/진행률 로그 보존일수(기본 7일 제안, 규정 준수 필요 시 조정).
* **권한 모델**: AD 계정/애플리케이션 로그인/역할 구분.

---

## 부록: 임시 테이블 명명 규칙(요구 반영)

* **로컬 임시**: `#stg_order_upload_{yyyyMMddHHmmss}_{@@SPID}`
  예: `#stg_order_upload_20250905_132455_57`

  * 생성 위치: 파싱/검증용 내부 작업에서만 사용, 트랜잭션 종료와 함께 자동 삭제.
  * **권장 사용 범위**: 일시 어그리게이션, 중간 계산, 중복 탐지.
    최종 데이터는 반드시 `stg.*`/`ops.*` 영속 테이블로 귀결.

---

필요하신 코드/뷰/프로시저(세션 생성, 진행률 업데이트, 에러 기록, 파서 샘플)도 바로 이어서 작성해줄 수 있어요.
위 **추가 정보** 알려주시면, DS/OS 도메인 FK 강화/뷰/프로시저까지 포함한 **완성본**으로 한번 더 정리해 드릴게요.
