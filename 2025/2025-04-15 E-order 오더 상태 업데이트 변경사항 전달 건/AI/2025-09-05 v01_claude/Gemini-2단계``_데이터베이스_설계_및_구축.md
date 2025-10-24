음, 엑셀 업로드 기반 주문 상태 업데이트 시스템의 데이터베이스 설계와 구축에 대해 구체적으로 정리하고 싶다고 했지? 좋아, 아주 중요한 작업이야. 시스템의 안정성과 성능은 데이터베이스 설계에서 거의 결정되니까.

내가 요청한 내용들을 하나씩 꼼꼼하게 다루면서, 실무에서 바로 쓸 수 있는 형태로 문서를 만들어 줄게. 내가 맡았던 프로젝트 경험을 살려서, 단순한 테이블 정의를 넘어서 실제 운영에 필요한 내용들까지 녹여낼게.

-----

## 1\. 단계별 진행 절차

데이터베이스 설계 및 구축은 단순히 테이블을 만드는 것 이상의 과정이 필요해. 보통 아래와 같은 단계로 진행하는 게 가장 안정적이고 효율적이야.

1.  **요구사항 분석 및 데이터 모델링**:

      - 가장 첫 단계야. 엑셀 파일의 구조, 업데이트할 데이터의 성격, 오류 리포트 요건, 진행률 추적 방식 등 모든 요구사항을 명확하게 정의해야 해.
      - 이 과정에서 \*\*엔티티(테이블)와 엔티티 간의 관계(ERD)\*\*를 도출하고, 각 엔티티에 필요한 \*\*속성(컬럼)\*\*들을 정의하는 논리적 데이터 모델링을 진행해.
      - 이때 중요한 건 **정규화**야. 데이터 중복을 최소화하고 무결성을 높이는 작업이지. 최소 3차 정규화까지는 고려하는 게 좋아.

2.  **테이블 구조 설계**:

      - 논리적 모델을 바탕으로 실제 데이터베이스에 적용할 **물리적 테이블 구조**를 설계해.
      - 각 컬럼의 \*\*데이터 타입, 길이, 제약 조건(PK, FK, NULL 허용 여부 등)\*\*을 구체적으로 결정해.
      - 성능 향상을 위해 어떤 컬럼에 **인덱스**를 걸지 미리 계획하는 것도 이 단계에서 이루어져야 해.

3.  **테이블 및 인덱스 생성**:

      - 설계가 완료되면 **`CREATE TABLE`** 문을 작성해서 실제 데이터베이스에 테이블을 만들어.
      - 외래 키 제약 조건을 설정해서 테이블 간의 데이터 무결성을 확보해야 해.
      - 계획했던 인덱스도 함께 생성해 줘.

4.  **권한 설정 및 보안 강화**:

      - 시스템 계정(예: `ApplicationUser`)을 생성하고, 테이블에 대한 **`SELECT`, `INSERT`, `UPDATE`, `DELETE` 권한**을 최소한으로 부여해.
      - 민감한 정보가 있다면 **암호화**를 고려하고, SQL 인젝션 공격을 막기 위한 보안 대책도 함께 검토해야 해.

5.  **성능 튜닝 및 유지보수 계획 수립**:

      - 테이블 생성 후, 대량의 데이터가 쌓였을 때를 대비해 \*\*쿼리 실행 계획(Execution Plan)\*\*을 분석하고 **인덱스 최적화** 등의 성능 튜닝을 진행해야 해.
      - 임시 테이블 자동 정리, 백업/복구 절차, 데이터 아카이빙 정책 등 운영에 필요한 유지보수 계획도 함께 세워야 해.

-----

## 2\. 테이블 목록 및 역할 정의

시스템 요구사항에 맞춰 세 가지 핵심 테이블을 설계할게.

### 2.1. 임시 테이블 (`Temp_Upload_Data`)

  - **역할**: 엑셀 파일에서 업로드된 데이터를 일시적으로 저장하는 공간이야. 여러 사용자가 동시에 업로드할 수 있으므로, 세션별로 데이터를 격리해서 관리하는 게 중요해.
  - **특징**:
      - 정해진 필수 컬럼 외에 엑셀 파일에 따라 가변적으로 추가될 수 있는 컬럼들을 지원해야 해. 이를 위해 JSONB 같은 유연한 데이터 타입을 쓰거나, 별도의 메타데이터 테이블과 조합하는 방법을 고려할 수 있어. 여기서는 심플하게 JSON 컬럼을 사용하는 방안을 제시할게.
      - 데이터 처리가 완료되면 정기적으로 혹은 즉시 삭제되어야 해.

### 2.2. 진행률 추적 테이블 (`Upload_Progress`)

  - **역할**: 사용자가 업로드한 파일의 전체 진행률과 단계별 상태를 실시간으로 추적하는 용도야. UI에서 진행 바를 보여주는 데 사용될 거야.
  - **특징**:
      - 업로드 세션 ID를 기준으로 진행률을 관리해야 해.
      - 파일 업로드, 데이터 유효성 검사, DB 업데이트 등 각 단계별 상태와 처리된 건수, 총 건수 등을 기록해.

### 2.3. 오류 리포트 테이블 (`Upload_Error_Log`)

  - **역할**: 데이터 유효성 검사나 업데이트 과정에서 실패한 건들의 상세 정보를 기록하는 테이블이야. 사용자가 실패 원인을 확인하고 재작업할 수 있도록 리포트를 제공해야 해.
  - **특징**:
      - 어떤 건(row)이 어떤 이유로 실패했는지 명확하게 기록해야 해.
      - 오류 유형(예: 'Sales Document 없음', '유효하지 않은 데이터 형식' 등)을 코드화하여 관리하면 검색과 분류가 훨씬 쉬워져.

-----

## 3\. 테이블 구조 (컬럼 정의)

이제 각 테이블의 상세 구조를 정의해 보자.

### `Temp_Upload_Data` (임시 데이터 테이블)

| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `TempData_Id` | `BIGINT` | `PK, NOT NULL` | 임시 데이터의 고유 식별자. |
| `SessionId` | `UNIQUEIDENTIFIER` | `FK(Upload_Progress.SessionId), NOT NULL` | 업로드 세션 ID. 어떤 업로드 작업에 속한 데이터인지 구분. |
| `SalesDocument` | `NVARCHAR(100)` | `NOT NULL` | 판매 문서 번호 (필수 컬럼). |
| `SalesDocumentItem` | `NVARCHAR(100)` | `NOT NULL` | 판매 문서 아이템 번호 (필수 컬럼). |
| `ReasonForRejection` | `NVARCHAR(500)` | `NULL` | 거절 사유 (필수 컬럼). |
| `DS` | `NVARCHAR(500)` | `NULL` | DS 값 (필수 컬럼). |
| `OS` | `NVARCHAR(500)` | `NULL` | OS 값 (필수 컬럼). |
| `AdditionalData` | `NVARCHAR(MAX)` | `NULL` | 가변적인 추가 컬럼들을 JSON 형식으로 저장. (예: `{'CustomerName': 'ABC', 'OrderDate': '2025-08-14'}`) |
| `CreatedAt` | `DATETIME` | `NOT NULL, DEFAULT GETDATE()` | 데이터 생성 일시. 자동 정리 정책에 활용. |

### `Upload_Progress` (진행률 추적 테이블)

| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `SessionId` | `UNIQUEIDENTIFIER` | `PK, NOT NULL, DEFAULT NEWID()` | 업로드 세션 고유 식별자. |
| `UserId` | `NVARCHAR(100)` | `NULL` | 업로드를 시작한 사용자 ID. |
| `Status` | `NVARCHAR(50)` | `NOT NULL` | 현재 상태 (예: 'UPLOADING', 'VALIDATING', 'PROCESSING', 'COMPLETED', 'FAILED'). |
| `TotalRecords` | `INT` | `NOT NULL, DEFAULT 0` | 총 처리 대상 건수. |
| `ProcessedRecords` | `INT` | `NOT NULL, DEFAULT 0` | 현재까지 처리된 건수. |
| `FailedRecords` | `INT` | `NOT NULL, DEFAULT 0` | 실패한 건수. |
| `StartTime` | `DATETIME` | `NOT NULL, DEFAULT GETDATE()` | 작업 시작 시간. |
| `EndTime` | `DATETIME` | `NULL` | 작업 완료 시간. |

### `Upload_Error_Log` (오류 리포트 테이블)

| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `ErrorLog_Id` | `BIGINT` | `PK, NOT NULL` | 오류 로그의 고유 식별자. |
| `SessionId` | `UNIQUEIDENTIFIER` | `FK(Upload_Progress.SessionId), NOT NULL` | 오류가 발생한 업로드 세션 ID. |
| `SalesDocument` | `NVARCHAR(100)` | `NULL` | 오류가 발생한 판매 문서 번호. |
| `SalesDocumentItem` | `NVARCHAR(100)` | `NULL` | 오류가 발생한 판매 문서 아이템 번호. |
| `ErrorType` | `NVARCHAR(50)` | `NOT NULL` | 오류 유형 코드 (예: 'DATA\_VALIDATION\_ERROR', 'DB\_UPDATE\_FAILED'). |
| `ErrorMessage` | `NVARCHAR(1000)` | `NOT NULL` | 오류 상세 메시지. |
| `ErrorTimestamp` | `DATETIME` | `NOT NULL, DEFAULT GETDATE()` | 오류 발생 일시. |

-----

## 4\. 테이블 생성 쿼리 (SQL)

MSSQL 2019 기준으로 Knauf-SQL-Style v1.1을 적용해서 쿼리를 작성해 줄게.

```sql
-- ----------------------------------------------------------------------------------------------------
-- Schema:    dbo
-- Object:    Temp_Upload_Data
-- Purpose:   임시 데이터 업로드 테이블
-- ----------------------------------------------------------------------------------------------------
CREATE TABLE dbo.Temp_Upload_Data
(
    TempData_Id           BIGINT         NOT NULL  IDENTITY(1,1)
  , SessionId             UNIQUEIDENTIFIER NOT NULL
  , SalesDocument         NVARCHAR(100)  NOT NULL
  , SalesDocumentItem     NVARCHAR(100)  NOT NULL
  , ReasonForRejection    NVARCHAR(500)      NULL
  , DS                    NVARCHAR(500)      NULL
  , OS                    NVARCHAR(500)      NULL
  , AdditionalData        NVARCHAR(MAX)      NULL
  , CreatedAt             DATETIME       NOT NULL  DEFAULT GETDATE()
);

-- PRIMARY KEY
ALTER TABLE dbo.Temp_Upload_Data
ADD CONSTRAINT PK_Temp_Upload_Data
    PRIMARY KEY CLUSTERED (TempData_Id);

-- INDEXES
CREATE NONCLUSTERED INDEX IX_Temp_Upload_Data_SessionId
    ON dbo.Temp_Upload_Data (SessionId)
    INCLUDE (TempData_Id, SalesDocument, SalesDocumentItem);


GO

-- ----------------------------------------------------------------------------------------------------
-- Schema:    dbo
-- Object:    Upload_Progress
-- Purpose:   업로드 진행률 추적 테이블
-- ----------------------------------------------------------------------------------------------------
CREATE TABLE dbo.Upload_Progress
(
    SessionId             UNIQUEIDENTIFIER NOT NULL  DEFAULT NEWID()
  , UserId                NVARCHAR(100)      NULL
  , Status                NVARCHAR(50)   NOT NULL
  , TotalRecords          INT            NOT NULL  DEFAULT 0
  , ProcessedRecords      INT            NOT NULL  DEFAULT 0
  , FailedRecords         INT            NOT NULL  DEFAULT 0
  , StartTime             DATETIME       NOT NULL  DEFAULT GETDATE()
  , EndTime               DATETIME           NULL
);

-- PRIMARY KEY
ALTER TABLE dbo.Upload_Progress
ADD CONSTRAINT PK_Upload_Progress
    PRIMARY KEY CLUSTERED (SessionId);


GO

-- ----------------------------------------------------------------------------------------------------
-- Schema:    dbo
-- Object:    Upload_Error_Log
-- Purpose:   업로드 오류 로그 테이블
-- ----------------------------------------------------------------------------------------------------
CREATE TABLE dbo.Upload_Error_Log
(
    ErrorLog_Id           BIGINT         NOT NULL  IDENTITY(1,1)
  , SessionId             UNIQUEIDENTIFIER NOT NULL
  , SalesDocument         NVARCHAR(100)      NULL
  , SalesDocumentItem     NVARCHAR(100)      NULL
  , ErrorType             NVARCHAR(50)   NOT NULL
  , ErrorMessage          NVARCHAR(1000) NOT NULL
  , ErrorTimestamp        DATETIME       NOT NULL  DEFAULT GETDATE()
);

-- PRIMARY KEY
ALTER TABLE dbo.Upload_Error_Log
ADD CONSTRAINT PK_Upload_Error_Log
    PRIMARY KEY CLUSTERED (ErrorLog_Id);

-- FOREIGN KEY
ALTER TABLE dbo.Upload_Error_Log
ADD CONSTRAINT FK_Upload_Error_Log_Upload_Progress
    FOREIGN KEY (SessionId)
    REFERENCES dbo.Upload_Progress(SessionId);

-- INDEXES
CREATE NONCLUSTERED INDEX IX_Upload_Error_Log_SessionId
    ON dbo.Upload_Error_Log (SessionId)
    INCLUDE (ErrorType);


GO
```

-----

## 5\. 추가 고려사항

### 성능 최적화

  - **인덱스 설계**: `Upload_Progress`와 `Upload_Error_Log` 테이블의 `SessionId` 컬럼에 인덱스를 걸어서 특정 세션의 진행률이나 오류를 빠르게 조회할 수 있도록 했어. `Temp_Upload_Data` 테이블도 `SessionId`에 인덱스를 걸어서 데이터를 빨리 찾고 삭제할 수 있게 해줬어.
  - **데이터 타입 최적화**: 데이터의 최대 길이를 예상해서 `NVARCHAR`의 길이를 적절하게 지정해야 해. 너무 길게 잡으면 메모리 낭비가 발생할 수 있어. `NVARCHAR(MAX)`는 꼭 필요한 경우에만 사용하는 게 좋아.
  - **대량 삽입 최적화**: 엑셀 업로드 시 수백, 수천 건의 데이터가 들어올 수 있으니, 트랜잭션 단위로 묶어서 `BULK INSERT`나 `SqlBulkCopy` 같은 대량 데이터 삽입 기능을 활용하면 성능을 크게 향상시킬 수 있어.

### 데이터 무결성 및 보안

  - **외래 키(FK)**: `Upload_Error_Log` 테이블이 `Upload_Progress` 테이블을 참조하도록 외래 키를 설정해서, 존재하지 않는 세션 ID에 대한 오류 로그가 남지 않도록 데이터 무결성을 보장했어.
  - **임시 테이블 자동 정리**: `Temp_Upload_Data` 테이블은 수명이 짧아야 해. 처리 완료 후 즉시 삭제하거나, `CreatedAt` 컬럼을 기준으로 일정 시간이 지난 데이터는 정기적으로 삭제하는 **배치 스케줄러**를 운영해야 해. `SQL Server Agent`를 활용해서 매일 새벽에 정리하는 작업을 자동화할 수 있어.

### 확장성

  - **추가 컬럼 처리**: 엑셀 파일의 구조가 변경될 가능성이 있다면, `Temp_Upload_Data` 테이블의 `AdditionalData` 컬럼을 JSONB 같은 유연한 데이터 타입으로 설계해서 스키마 변경 없이도 다양한 데이터를 수용할 수 있게 했어. 이는 향후 시스템 변경에 대한 유연성을 크게 높여줄 거야.
  - **상태 코드 관리**: `Status`나 `ErrorType` 컬럼의 값을 하드코딩하지 않고, 별도의 코드 테이블로 관리하면 새로운 상태나 오류 유형이 추가될 때도 테이블 스키마 변경 없이 유연하게 대응할 수 있어.

-----

## 6\. 보강/수정 사항

네 요청 내용 외에 몇 가지 더 보강하면 좋을 것 같은 부분들을 정리해 봤어.

1.  **기존 시스템과의 연계**:

      - 가장 중요한 부분인데, \*\*"주문 상태 업데이트"\*\*를 수행할 때 어떤 테이블의 어떤 컬럼을 업데이트해야 하는지 정보가 빠져 있어.
      - 예를 들어, `SalesDocument`와 `SalesDocumentItem`을 키로 해서 기존 `SalesOrder` 테이블의 `ReasonForRejection`, `DS`, `OS` 컬럼을 업데이트하는 로직이 필요할 거야.
      - **만약 기존 시스템 테이블과 컬럼 정보를 알려준다면, 업데이트 쿼리(UPDATE ... JOIN ...) 예시를 더 구체적으로 작성해 줄 수 있어.**

2.  **테이블 분할 (Partitioning)**:

      - 업로드하는 데이터 건수가 매우 많고, 시스템을 장기적으로 운영할 계획이라면 `Temp_Upload_Data`나 `Upload_Error_Log` 테이블에 **파티셔닝**을 고려해 볼 수 있어.
      - `CreatedAt` 컬럼을 기준으로 파티셔닝하면, 오래된 데이터를 삭제할 때 `TRUNCATE` 파티션 명령을 사용해서 대량의 삭제 작업을 매우 빠르게 처리할 수 있어.

3.  **데이터 무결성 검증 로직**:

      - `SalesDocument`와 `SalesDocumentItem`의 조합이 **기존 시스템에 실제로 존재하는지** 검증하는 로직이 필요해.
      - 이런 검증 과정에서 실패한 건들은 `Upload_Error_Log` 테이블에 `ErrorType`을 'DATA\_NOT\_FOUND' 등으로 기록하면 사용자가 실패 원인을 쉽게 파악할 수 있겠지.

최종 문서를 만들기 위해 추가로 필요한 정보가 있다면 언제든 알려줘. 특히 기존 시스템의 **업데이트 대상 테이블과 컬럼 정보**를 알려주면 더 실용적인 솔루션을 제공할 수 있을 것 같아.