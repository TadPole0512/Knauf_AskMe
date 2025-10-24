'2단계: 데이터베이스 설계 및 구축' 작업 내용을 체계적으로 문서화하기 위해 다음과 같이 정리했습니다.  
이 문서는 단계별 절차, 테이블 목록과 역할, 테이블 구조, SQL 생성문, 추가 고려사항, 그리고 보강/수정 사항 등 요청하신 모든 항목을 포함합니다.

***

## 1. 단계별 진행 절차

1.1 임시 테이블 구조 설계  
- 동적으로 생성될 임시 테이블의 필수 컬럼 정의 (SalesDocument, Sales Document Item, Reason for rejection, DS, OS)  
- 가변 컬럼 관리를 위한 메타데이터 테이블 설계  
- 임시 테이블 명명 규칙 정의 (예: 접두어 + 타임스탬프 + 세션ID 형식)  
- 자동 정리 정책 수립 (예: 생성 후 24시간 내 자동 삭제 등)  

1.2 진행률 추적 테이블 설계  
- 세션별 업로드 진행률을 관리할 테이블 구조 정의  
- 단계별 진행률 기록 방식 설계 (예: 전체, 현재 단계, 완료율 등)  
- 실시간 조회를 고려한 인덱스 설계  

1.3 오류 리포트 테이블 설계  
- 처리 실패 건을 유형별로 분류할 분류 체계 정의  
- 오류 상세 정보 저장용 테이블 구조 설계 (실패 유형, 상세 메시지, 발생 시간 등)  
- 리포트 조회를 위한 적절한 인덱스 설계  

***

## 2. 테이블 목록 및 역할 정의

| 테이블명                 | 역할 설명                                                             |
|-----------------------|-----------------------------------------------------------------|
| 임시테이블 메인 (TempOrders)    | 동적 가변 컬럼과 기본 컬럼(SalesDocument 등)으로 주문 상태 임시 저장                  |
| 메타데이터 테이블 (TempOrders_Metadata) | 임시테이블의 가변 컬럼 정보를 저장하여 구조의 유연성을 지원                             |
| 진행률 추적 테이블 (Upload_Progress)    | 각 사용자 세션별 업로드 상태 및 단계별 진행률 관리                                  |
| 오류 리포트 테이블 (Error_Report)       | 처리 실패 건의 유형별 오류 상세 정보 저장 및 조회 지원                              |

### 테이블 관계 (ERD 개념 수준)
- TempOrders_Metadata 가변 컬럼 정보는 TempOrders 데이터 구조를 동적으로 지원  
- Upload_Progress 와 Error_Report 는 각 세션(SessionID) 별로 관리되어 진행 상황과 에러를 모니터링  
- TempOrders 와 Upload_Progress, Error_Report 는 SessionID 를 통해 연관성을 가짐  

***

## 3. 테이블 구조(컬럼 정의)

### TempOrders (임시 주문 상태 테이블)
| 컬럼명               | 데이터 타입          | 제약조건               | 설명                          |
|-------------------|------------------|--------------------|-----------------------------|
| TempOrderID       | INT              | PK, IDENTITY(1,1)  | 임시 주문 고유 식별자                  |
| SalesDocument     | VARCHAR(50)      | NOT NULL           | 판매 문서 번호                    |
| SalesDocumentItem | VARCHAR(50)      | NOT NULL           | 판매 문서 항목 번호                 |
| ReasonForRejection| VARCHAR(255)     | NULL               | 반려 사유                       |
| DS                | VARCHAR(50)      | NULL               | DS (데이터 소스)                   |
| OS                | VARCHAR(50)      | NULL               | OS (운영 체계)                   |
| CreatedAt         | DATETIME         | DEFAULT GETDATE()  | 생성 일시                      |

### TempOrders_Metadata (임시 테이블 메타데이터)
| 컬럼명           | 데이터 타입      | 제약조건            | 설명                         |
|---------------|--------------|-----------------|----------------------------|
| MetadataID    | INT          | PK, IDENTITY(1,1) | 메타데이터 고유 식별자               |
| TempColumnName| VARCHAR(100) | NOT NULL        | 가변 컬럼명                     |
| DataType      | VARCHAR(50)  | NOT NULL        | 가변 컬럼 데이터 타입                |
| CreatedAt    | DATETIME     | DEFAULT GETDATE()| 생성 일시                     |

### Upload_Progress (업로드 진행률 추적 테이블)
| 컬럼명           | 데이터 타입      | 제약조건           | 설명                             |
|---------------|--------------|----------------|--------------------------------|
| SessionID     | VARCHAR(100) | PK             | 업로드 세션 고유ID                      |
| Step          | INT          | PK             | 진행 단계 (1, 2, 3 ...)                 |
| ProgressRate  | INT          | NOT NULL       | 진행률(%)                          |
| UpdatedAt     | DATETIME     | DEFAULT GETDATE()| 마지막 업데이트 일시                    |

### Error_Report (오류 리포트 테이블)
| 컬럼명           | 데이터 타입      | 제약조건           | 설명                             |
|---------------|--------------|----------------|--------------------------------|
| ErrorID       | INT          | PK, IDENTITY(1,1)| 오류 고유 식별자                      |
| SessionID     | VARCHAR(100) | NOT NULL       | 오류 발생 세션 ID                    |
| ErrorType     | VARCHAR(50)  | NOT NULL       | 오류 유형 (예: validation, system)       |
| ErrorMessage  | NVARCHAR(MAX)| NOT NULL       | 상세 오류 메시지                     |
| ErrorTime     | DATETIME     | DEFAULT GETDATE()| 오류 발생 시간                     |

***

## 4. 테이블 생성 쿼리(SQL)

```sql
-- TempOrders
CREATE TABLE dbo.TempOrders (
    TempOrderID INT IDENTITY(1,1) PRIMARY KEY,
    SalesDocument VARCHAR(50) NOT NULL,
    SalesDocumentItem VARCHAR(50) NOT NULL,
    ReasonForRejection VARCHAR(255) NULL,
    DS VARCHAR(50) NULL,
    OS VARCHAR(50) NULL,
    CreatedAt DATETIME DEFAULT GETDATE() NOT NULL
);
GO

-- TempOrders_Metadata
CREATE TABLE dbo.TempOrders_Metadata (
    MetadataID INT IDENTITY(1,1) PRIMARY KEY,
    TempColumnName VARCHAR(100) NOT NULL,
    DataType VARCHAR(50) NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE() NOT NULL
);
GO

-- Upload_Progress
CREATE TABLE dbo.Upload_Progress (
    SessionID VARCHAR(100) NOT NULL,
    Step INT NOT NULL,
    ProgressRate INT NOT NULL,
    UpdatedAt DATETIME DEFAULT GETDATE() NOT NULL,
    CONSTRAINT PK_Upload_Progress PRIMARY KEY (SessionID, Step)
);
GO

-- Error_Report
CREATE TABLE dbo.Error_Report (
    ErrorID INT IDENTITY(1,1) PRIMARY KEY,
    SessionID VARCHAR(100) NOT NULL,
    ErrorType VARCHAR(50) NOT NULL,
    ErrorMessage NVARCHAR(MAX) NOT NULL,
    ErrorTime DATETIME DEFAULT GETDATE() NOT NULL,
    INDEX IDX_ErrorReport_SessionID (SessionID),
    INDEX IDX_ErrorReport_ErrorType (ErrorType)
);
GO
```

***

## 5. 추가 고려사항

- 데이터 무결성: PK, FK 제약과 NOT NULL 제약 기본 적용. 임시 테이블 특성상 가변 컬럼 메타데이터 활용해 데이터 일관성 유지 관리  
- 성능 최적화: 진행률 및 오류 리포트 테이블에 인덱스 설계로 빠른 조회 지원, 임시 테이블은 자동 정리 정책으로 저장 공간 과다 사용 방지  
- 보안: 세션별 데이터 분리 정책 및 접근 제어, 민감 정보 암호화 필요 시 별도 고려  
- 확장성: 가변 컬럼 메타데이터 테이블로 유연한 컬럼 확장 가능, 신규 진행 단계나 오류 유형 추가 용이하도록 설계  

***

## 6. 보강/수정 사항 및 추가 질문

- 주문 상태 종류(예: 승인, 반려, 대기 등)와 해당 상태가 가지고 있어야 하는 추가 컬럼이 있는지?  
- 업로드 대상 엑셀 파일의 구조(컬럼명, 데이터 타입, 샘플 데이터) 상세 정보 요청  
- 기존 시스템 연계 여부 및 연계 방식 확인 필요 (API, DB 직접접근 등)  
- 동시 다중 사용자 처리 방안(예: 세션별 임시 테이블 분리 방식) 구체화 필요  
- 에러 처리 정책(재시도, 알림 등)과 상세 리포트 형태 및 조회 화면 요구사항  

필요한 추가 정보를 제공해주시면 반영하여 문서 보강 및 최종화하겠습니다.

[1](https://stackoverflow.com/questions/33216250/create-temporary-table-with-dynamic-number-of-columns)
[2](https://stackoverflow.com/questions/68619164/creating-temporary-table-with-dynamic-columns)
[3](https://www.sqlservercentral.com/forums/topic/is-it-possible-to-create-a-temp-table-with-dynamic-columns)
[4](https://learn.microsoft.com/en-us/answers/questions/190884/how-to-store-dynamic-sql-result-into-temporary-tab)
[5](https://celedonpartners.com/sql-server-how-to-a-create-temp-table-with-dynamic-column-names/)
[6](https://www.craft.do/templates/progress-tracker)
[7](https://forum.inductiveautomation.com/t/databases-errors-into-a-table/56391)
[8](https://www.geeksforgeeks.org/sql-server/dynamic-sql-and-temporary-tables-in-sql-server/)
[9](https://dribbble.com/search/progress-table)
[10](https://stackoverflow.com/questions/40684635/database-table-design-issues)
[11](https://docs.snowflake.com/ko/user-guide/tables-temp-transient)
[12](https://stackoverflow.com/questions/10038393/database-design-for-tracking-progress-over-time)
[13](https://www.red-gate.com/simple-talk/databases/sql-server/database-administration-sql-server/ten-common-database-design-mistakes/)
[14](https://www.mssqltips.com/sqlservertip/1285/modify-sql-server-database-tables-to-keep-similar-columns-together/)
[15](https://www.notion.com/ko/templates/your-progress-tracker)
[16](https://www.sqlservercentral.com/forums/topic/error-logging-table)
[17](https://create.microsoft.com/en-us/templates/planners-and-trackers)
[18](https://support.microsoft.com/en-us/office/database-design-basics-eb2159cf-1e30-401a-8084-bd4f9c9ca1f5)
[19](https://www.jotform.com/table-templates/category/tracker)
[20](https://vertabelo.com/blog/7-common-database-design-errors/)