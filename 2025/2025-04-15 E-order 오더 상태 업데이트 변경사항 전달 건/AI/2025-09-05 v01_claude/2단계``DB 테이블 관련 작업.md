위의 내용에서 "2단계: 데이터베이스 설계 및 구축"을 보다 체계적이고 구체적으로 진행하고 싶어. 해당 내용과 관련해서 자세히 문서화 해줘.  각 단계의 설명과 단계별로 생성해야 할 테이블, 각 테이블의 구조 등 내가 알아야할 더 많은 정보 등을 담아서 문서화 해줘.(THINK HARD)(THINK STEP-BY-STEP)

---------------------------------------------------------------------

**사고 과정 적용:**

이 질문을 위해 다음 공식들을 조합하여 접근하겠습니다:

1. **복잡성 해결 매트릭스(CS)**: DB 설계를 하위 시스템(임시테이블, 진행률, 오류관리, 메타데이터)으로 분해
2. **다차원 분석 프레임워크(MDA)**: 시간적(생성→사용→정리), 계층적(물리→논리→응용), 인과적(요구사항→설계→구현) 차원 분석
3. **천재적 통찰 공식(GI)**: 기존 방식의 한계를 관찰하고, 동적 스키마와 메타데이터 기반 접근의 연결점 발견
4. **통합 지혜 공식(IW)**: DBA 지식과 실무 경험을 바탕으로 운영 가능한 설계 도출

---

# 2단계: 데이터베이스 설계 및 구축 상세 문서

## 2.1 전체 DB 설계 개념 및 아키텍처

### 2.1.1 설계 철학 및 원칙

**핵심 설계 원칙:**
- **동적 스키마 관리**: 가변적인 엑셀 구조에 대응하는 유연한 테이블 설계
- **메타데이터 중심**: 테이블 구조 정보를 별도 관리하여 런타임 동적 처리
- **성능 최적화**: 대용량 데이터 처리를 위한 인덱스 및 파티셔닝 전략
- **데이터 무결성**: 트랜잭션 및 제약조건을 통한 데이터 일관성 보장
- **운영 효율성**: 자동 정리 및 모니터링을 고려한 설계

**아키텍처 구성요소:**

| 구성요소 | 역할 | 특징 |
|----------|------|------|
| 임시 데이터 저장소 | 엑셀 원본 데이터 임시 보관 | 동적 스키마, 세션별 격리 |
| 메타데이터 관리소 | 테이블 구조 및 매핑 정보 관리 | 스키마 버전 관리, 컬럼 매핑 |
| 진행률 추적소 | 처리 진행 상황 실시간 관리 | 세션별 상태 추적, 실시간 조회 |
| 오류 관리소 | 실패 건 및 오류 정보 저장 | 상세 오류 분류, 리포트 생성 |
| 감사 로그소 | 모든 작업 이력 기록 | 추적성, 규정 준수 |

### 2.1.2 테이블 분류 체계

**테이블 유형별 분류:**

| 분류 | 테이블명 패턴 | 생명주기 | 용도 |
|------|---------------|----------|------|
| 메타데이터 | META_* | 영구 | 시스템 구조 정보 관리 |
| 임시 데이터 | TEMP_* | 세션별 | 처리 중 임시 데이터 저장 |
| 진행률 추적 | PROGRESS_* | 세션별 | 실시간 진행률 모니터링 |
| 오류 관리 | ERROR_* | 보존 정책 | 오류 정보 및 리포트 |
| 감사 로그 | AUDIT_* | 보존 정책 | 작업 이력 추적 |

## 2.2 메타데이터 관리 테이블 설계

### 2.2.1 업로드 세션 관리 테이블

**테이블명:** `META_UPLOAD_SESSION`

**용도:** 각 엑셀 업로드 세션의 기본 정보를 관리

**테이블 구조:**

| 컬럼명 | 데이터타입 | 제약조건 | 설명 |
|--------|------------|----------|------|
| SESSION_ID | VARCHAR(50) | PK, NOT NULL | 세션 고유 식별자 (UUID) |
| USER_ID | VARCHAR(50) | NOT NULL | 업로드 사용자 ID |
| ORIGINAL_FILE_NAME | NVARCHAR(255) | NOT NULL | 원본 파일명 |
| FILE_SIZE | BIGINT | NOT NULL | 파일 크기 (바이트) |
| TOTAL_ROWS | INT | NULL | 총 데이터 행 수 |
| STATUS | VARCHAR(20) | NOT NULL | 세션 상태 (UPLOADING/PROCESSING/COMPLETED/FAILED) |
| START_TIME | DATETIME2 | NOT NULL | 시작 시간 |
| END_TIME | DATETIME2 | NULL | 종료 시간 |
| TEMP_TABLE_NAME | VARCHAR(100) | NULL | 생성된 임시 테이블명 |
| ERROR_MESSAGE | NVARCHAR(MAX) | NULL | 오류 메시지 |
| CREATED_DATE | DATETIME2 | NOT NULL, DEFAULT GETDATE() | 생성일시 |
| UPDATED_DATE | DATETIME2 | NULL | 수정일시 |

**인덱스 설계:**
- `IX_META_UPLOAD_SESSION_USER_STATUS`: (USER_ID, STATUS) - 사용자별 세션 조회
- `IX_META_UPLOAD_SESSION_STATUS_TIME`: (STATUS, START_TIME) - 상태별 시간 조회
- `IX_META_UPLOAD_SESSION_CREATED`: (CREATED_DATE) - 정리 작업용

### 2.2.2 컬럼 메타데이터 테이블

**테이블명:** `META_COLUMN_MAPPING`

**용도:** 엑셀 파일의 컬럼 구조 및 매핑 정보 관리

**테이블 구조:**

| 컬럼명 | 데이터타입 | 제약조건 | 설명 |
|--------|------------|----------|------|
| MAPPING_ID | BIGINT | PK, IDENTITY | 매핑 고유 식별자 |
| SESSION_ID | VARCHAR(50) | FK, NOT NULL | 세션 참조 |
| COLUMN_NAME | NVARCHAR(255) | NOT NULL | 엑셀 컬럼명 |
| COLUMN_INDEX | INT | NOT NULL | 엑셀에서의 컬럼 순서 (0부터 시작) |
| DATA_TYPE | VARCHAR(50) | NOT NULL | 추정 데이터 타입 |
| MAX_LENGTH | INT | NULL | 최대 길이 |
| IS_REQUIRED | BIT | NOT NULL, DEFAULT 0 | 필수 컬럼 여부 |
| BUSINESS_MEANING | VARCHAR(100) | NULL | 비즈니스 의미 (SALES_DOCUMENT/SALES_ITEM/REASON/DS/OS) |
| SAMPLE_VALUES | NVARCHAR(MAX) | NULL | 샘플 데이터 (최대 10개, JSON 형태) |
| CREATED_DATE | DATETIME2 | NOT NULL, DEFAULT GETDATE() | 생성일시 |

**인덱스 설계:**
- `IX_META_COLUMN_MAPPING_SESSION`: (SESSION_ID, COLUMN_INDEX) - 세션별 컬럼 조회
- `IX_META_COLUMN_MAPPING_BUSINESS`: (BUSINESS_MEANING) - 비즈니스 컬럼 조회

### 2.2.3 임시 테이블 스키마 정의 테이블

**테이블명:** `META_TEMP_TABLE_SCHEMA`

**용도:** 동적으로 생성된 임시 테이블의 스키마 정보 저장

**테이블 구조:**

| 컬럼명 | 데이터타입 | 제약조건 | 설명 |
|--------|------------|----------|------|
| SCHEMA_ID | BIGINT | PK, IDENTITY | 스키마 고유 식별자 |
| SESSION_ID | VARCHAR(50) | FK, NOT NULL | 세션 참조 |
| TABLE_NAME | VARCHAR(100) | NOT NULL | 임시 테이블명 |
| COLUMN_NAME | VARCHAR(100) | NOT NULL | 컬럼명 |
| DATA_TYPE | VARCHAR(50) | NOT NULL | SQL 데이터 타입 |
| MAX_LENGTH | INT | NULL | 최대 길이 |
| IS_NULLABLE | BIT | NOT NULL | NULL 허용 여부 |
| COLUMN_ORDER | INT | NOT NULL | 컬럼 순서 |
| CREATED_DATE | DATETIME2 | NOT NULL, DEFAULT GETDATE() | 생성일시 |

**인덱스 설계:**
- `IX_META_TEMP_SCHEMA_TABLE`: (TABLE_NAME, COLUMN_ORDER) - 테이블별 스키마 조회
- `IX_META_TEMP_SCHEMA_SESSION`: (SESSION_ID) - 세션별 스키마 조회

## 2.3 임시 데이터 저장 테이블 설계

### 2.3.1 동적 임시 테이블 생성 전략

**명명 규칙:**
- 패턴: `TEMP_UPLOAD_[SESSION_ID]`
- 예시: `TEMP_UPLOAD_A1B2C3D4-E5F6-7G8H-9I0J-K1L2M3N4O5P6`

**기본 컬럼 구조:**

| 컬럼명 | 데이터타입 | 설명 |
|--------|------------|------|
| ROW_ID | BIGINT IDENTITY | 행 고유 식별자 |
| ROW_NUMBER | INT | 엑셀에서의 행 번호 |
| PROCESSING_STATUS | VARCHAR(20) | 처리 상태 (PENDING/PROCESSED/FAILED) |
| ERROR_MESSAGE | NVARCHAR(MAX) | 오류 메시지 |
| CREATED_DATE | DATETIME2 | 생성일시 |
| [동적 컬럼들] | [가변 타입] | 엑셀 데이터 컬럼들 |

**필수 비즈니스 컬럼 (항상 포함):**

| 컬럼명 | 데이터타입 | 설명 |
|--------|------------|------|
| SALES_DOCUMENT | VARCHAR(50) | 판매 문서 번호 |
| SALES_DOCUMENT_ITEM | VARCHAR(50) | 판매 문서 항목 |
| REASON_FOR_REJECTION | NVARCHAR(255) | 거부 사유 |
| DELIVERY_STATUS | VARCHAR(10) | 배송 상태 (DS) |
| OVERALL_STATUS | VARCHAR(10) | 전체 상태 (OS) |

### 2.3.2 임시 테이블 인덱스 전략

**기본 인덱스:**
- 클러스터드 인덱스: `ROW_ID`
- 비즈니스 키 인덱스: `(SALES_DOCUMENT, SALES_DOCUMENT_ITEM)`
- 처리 상태 인덱스: `(PROCESSING_STATUS)`

**파티셔닝 고려사항:**
- 13,000건 이상 대용량 데이터 처리를 위한 파티션 테이블 고려
- ROW_NUMBER 기준 범위 파티셔닝 적용 검토

## 2.4 진행률 추적 테이블 설계

### 2.4.1 진행률 마스터 테이블

**테이블명:** `PROGRESS_UPLOAD_MASTER`

**용도:** 업로드 세션별 전체 진행률 관리

**테이블 구조:**

| 컬럼명 | 데이터타입 | 제약조건 | 설명 |
|--------|------------|----------|------|
| SESSION_ID | VARCHAR(50) | PK, FK | 세션 참조 |
| CURRENT_STEP | VARCHAR(50) | NOT NULL | 현재 단계 |
| TOTAL_STEPS | INT | NOT NULL | 전체 단계 수 |
| OVERALL_PROGRESS | DECIMAL(5,2) | NOT NULL | 전체 진행률 (0.00-100.00) |
| CURRENT_STEP_PROGRESS | DECIMAL(5,2) | NOT NULL | 현재 단계 진행률 |
| PROCESSED_ROWS | INT | NOT NULL, DEFAULT 0 | 처리된 행 수 |
| TOTAL_ROWS | INT | NOT NULL | 전체 행 수 |
| SUCCESS_ROWS | INT | NOT NULL, DEFAULT 0 | 성공 행 수 |
| FAILED_ROWS | INT | NOT NULL, DEFAULT 0 | 실패 행 수 |
| ESTIMATED_COMPLETION | DATETIME2 | NULL | 예상 완료 시간 |
| LAST_UPDATED | DATETIME2 | NOT NULL, DEFAULT GETDATE() | 최종 업데이트 시간 |

**인덱스 설계:**
- `IX_PROGRESS_MASTER_SESSION`: (SESSION_ID) - 세션별 조회
- `IX_PROGRESS_MASTER_UPDATED`: (LAST_UPDATED) - 최신 업데이트 조회

### 2.4.2 진행률 상세 테이블

**테이블명:** `PROGRESS_UPLOAD_DETAIL`

**용도:** 단계별 상세 진행률 이력 관리

**테이블 구조:**

| 컬럼명 | 데이터타입 | 제약조건 | 설명 |
|--------|------------|----------|------|
| DETAIL_ID | BIGINT | PK, IDENTITY | 상세 고유 식별자 |
| SESSION_ID | VARCHAR(50) | FK, NOT NULL | 세션 참조 |
| STEP_NAME | VARCHAR(50) | NOT NULL | 단계명 |
| STEP_ORDER | INT | NOT NULL | 단계 순서 |
| STATUS | VARCHAR(20) | NOT NULL | 단계 상태 (PENDING/RUNNING/COMPLETED/FAILED) |
| START_TIME | DATETIME2 | NULL | 시작 시간 |
| END_TIME | DATETIME2 | NULL | 종료 시간 |
| PROGRESS_PERCENT | DECIMAL(5,2) | NOT NULL, DEFAULT 0 | 진행률 |
| PROCESSED_COUNT | INT | NOT NULL, DEFAULT 0 | 처리 건수 |
| SUCCESS_COUNT | INT | NOT NULL, DEFAULT 0 | 성공 건수 |
| FAILED_COUNT | INT | NOT NULL, DEFAULT 0 | 실패 건수 |
| ERROR_MESSAGE | NVARCHAR(MAX) | NULL | 오류 메시지 |
| ADDITIONAL_INFO | NVARCHAR(MAX) | NULL | 추가 정보 (JSON 형태) |
| CREATED_DATE | DATETIME2 | NOT NULL, DEFAULT GETDATE() | 생성일시 |

**인덱스 설계:**
- `IX_PROGRESS_DETAIL_SESSION_ORDER`: (SESSION_ID, STEP_ORDER) - 세션별 단계 조회
- `IX_PROGRESS_DETAIL_STATUS`: (STATUS, START_TIME) - 상태별 조회

### 2.4.3 처리 단계 정의

**표준 처리 단계:**

| 단계 순서 | 단계명 | 단계 설명 | 예상 비중 |
|-----------|--------|-----------|----------|
| 1 | FILE_UPLOAD | 파일 업로드 | 5% |
| 2 | FILE_VALIDATION | 파일 검증 | 5% |
| 3 | SCHEMA_ANALYSIS | 스키마 분석 | 10% |
| 4 | TEMP_TABLE_CREATE | 임시 테이블 생성 | 5% |
| 5 | DATA_IMPORT | 데이터 임포트 | 30% |
| 6 | DATA_VALIDATION | 데이터 검증 | 15% |
| 7 | BUSINESS_PROCESSING | 비즈니스 로직 처리 | 25% |
| 8 | CLEANUP | 정리 작업 | 5% |

## 2.5 오류 관리 테이블 설계

### 2.5.1 오류 분류 테이블

**테이블명:** `ERROR_CATEGORY`

**용도:** 오류 유형 분류 및 관리

**테이블 구조:**

| 컬럼명 | 데이터타입 | 제약조건 | 설명 |
|--------|------------|----------|------|
| CATEGORY_ID | INT | PK, IDENTITY | 분류 고유 식별자 |
| CATEGORY_CODE | VARCHAR(20) | UNIQUE, NOT NULL | 분류 코드 |
| CATEGORY_NAME | NVARCHAR(100) | NOT NULL | 분류명 |
| SEVERITY_LEVEL | VARCHAR(10) | NOT NULL | 심각도 (LOW/MEDIUM/HIGH/CRITICAL) |
| IS_RECOVERABLE | BIT | NOT NULL | 복구 가능 여부 |
| DESCRIPTION | NVARCHAR(500) | NULL | 설명 |
| SOLUTION_GUIDE | NVARCHAR(MAX) | NULL | 해결 가이드 |
| IS_ACTIVE | BIT | NOT NULL, DEFAULT 1 | 활성 여부 |
| CREATED_DATE | DATETIME2 | NOT NULL, DEFAULT GETDATE() | 생성일시 |

**기본 오류 분류:**

| 분류 코드 | 분류명 | 심각도 | 설명 |
|-----------|--------|---------|------|
| FILE_FORMAT | 파일 형식 오류 | HIGH | 지원하지 않는 파일 형식 |
| FILE_SIZE | 파일 크기 오류 | MEDIUM | 허용 크기 초과 |
| COLUMN_MISSING | 필수 컬럼 누락 | HIGH | 필수 컬럼이 존재하지 않음 |
| DATA_TYPE | 데이터 타입 오류 | MEDIUM | 잘못된 데이터 타입 |
| DATA_LENGTH | 데이터 길이 오류 | LOW | 허용 길이 초과 |
| BUSINESS_RULE | 비즈니스 규칙 위반 | MEDIUM | 비즈니스 로직 검증 실패 |
| DB_CONNECTION | DB 연결 오류 | CRITICAL | 데이터베이스 연결 실패 |
| SYSTEM_ERROR | 시스템 오류 | CRITICAL | 예상치 못한 시스템 오류 |

### 2.5.2 오류 로그 테이블

**테이블명:** `ERROR_LOG`

**용도:** 발생한 모든 오류의 상세 정보 저장

**테이블 구조:**

| 컬럼명 | 데이터타입 | 제약조건 | 설명 |
|--------|------------|----------|------|
| ERROR_ID | BIGINT | PK, IDENTITY | 오류 고유 식별자 |
| SESSION_ID | VARCHAR(50) | FK, NOT NULL | 세션 참조 |
| CATEGORY_ID | INT | FK, NOT NULL | 오류 분류 참조 |
| ROW_NUMBER | INT | NULL | 오류 발생 행 번호 |
| COLUMN_NAME | VARCHAR(100) | NULL | 오류 발생 컬럼명 |
| ORIGINAL_VALUE | NVARCHAR(MAX) | NULL | 원본 값 |
| ERROR_MESSAGE | NVARCHAR(MAX) | NOT NULL | 오류 메시지 |
| STACK_TRACE | NVARCHAR(MAX) | NULL | 스택 트레이스 |
| CONTEXT_DATA | NVARCHAR(MAX) | NULL | 컨텍스트 정보 (JSON) |
| IS_RESOLVED | BIT | NOT NULL, DEFAULT 0 | 해결 여부 |
| RESOLUTION_NOTE | NVARCHAR(MAX) | NULL | 해결 방법 |
| OCCURRED_AT | DATETIME2 | NOT NULL, DEFAULT GETDATE() | 발생 시간 |
| RESOLVED_AT | DATETIME2 | NULL | 해결 시간 |

**인덱스 설계:**
- `IX_ERROR_LOG_SESSION`: (SESSION_ID, OCCURRED_AT) - 세션별 오류 조회
- `IX_ERROR_LOG_CATEGORY`: (CATEGORY_ID, OCCURRED_AT) - 분류별 오류 조회
- `IX_ERROR_LOG_STATUS`: (IS_RESOLVED, OCCURRED_AT) - 해결 상태별 조회

### 2.5.3 오류 통계 테이블

**테이블명:** `ERROR_STATISTICS`

**용도:** 오류 발생 통계 및 트렌드 분석

**테이블 구조:**

| 컬럼명 | 데이터타입 | 제약조건 | 설명 |
|--------|------------|----------|------|
| STAT_ID | BIGINT | PK, IDENTITY | 통계 고유 식별자 |
| SESSION_ID | VARCHAR(50) | FK, NOT NULL | 세션 참조 |
| CATEGORY_ID | INT | FK, NOT NULL | 오류 분류 참조 |
| ERROR_COUNT | INT | NOT NULL | 오류 발생 건수 |
| TOTAL_AFFECTED_ROWS | INT | NOT NULL | 영향받은 총 행 수 |
| FIRST_OCCURRENCE | DATETIME2 | NOT NULL | 최초 발생 시간 |
| LAST_OCCURRENCE | DATETIME2 | NOT NULL | 최종 발생 시간 |
| RESOLUTION_RATE | DECIMAL(5,2) | NOT NULL, DEFAULT 0 | 해결률 |
| AVERAGE_RESOLUTION_TIME | INT | NULL | 평균 해결 시간 (분) |
| CREATED_DATE | DATETIME2 | NOT NULL, DEFAULT GETDATE() | 생성일시 |

## 2.6 감사 로그 테이블 설계

### 2.6.1 작업 이력 테이블

**테이블명:** `AUDIT_UPLOAD_HISTORY`

**용도:** 모든 업로드 작업의 이력 추적

**테이블 구조:**

| 컬럼명 | 데이터타입 | 제약조건 | 설명 |
|--------|------------|----------|------|
| AUDIT_ID | BIGINT | PK, IDENTITY | 감사 고유 식별자 |
| SESSION_ID | VARCHAR(50) | FK, NOT NULL | 세션 참조 |
| ACTION_TYPE | VARCHAR(50) | NOT NULL | 액션 유형 |
| ACTION_DETAIL | NVARCHAR(500) | NULL | 액션 상세 |
| TARGET_TABLE | VARCHAR(100) | NULL | 대상 테이블 |
| AFFECTED_ROWS | INT | NULL | 영향받은 행 수 |
| OLD_VALUES | NVARCHAR(MAX) | NULL | 변경 전 값 (JSON) |
| NEW_VALUES | NVARCHAR(MAX) | NULL | 변경 후 값 (JSON) |
| USER_ID | VARCHAR(50) | NOT NULL | 사용자 ID |
| IP_ADDRESS | VARCHAR(45) | NULL | IP 주소 |
| USER_AGENT | NVARCHAR(500) | NULL | 사용자 에이전트 |
| EXECUTION_TIME | INT | NULL | 실행 시간 (밀리초) |
| SUCCESS | BIT | NOT NULL | 성공 여부 |
| ERROR_MESSAGE | NVARCHAR(MAX) | NULL | 오류 메시지 |
| CREATED_DATE | DATETIME2 | NOT NULL, DEFAULT GETDATE() | 생성일시 |

**인덱스 설계:**
- `IX_AUDIT_HISTORY_SESSION`: (SESSION_ID, CREATED_DATE) - 세션별 이력
- `IX_AUDIT_HISTORY_USER`: (USER_ID, CREATED_DATE) - 사용자별 이력
- `IX_AUDIT_HISTORY_ACTION`: (ACTION_TYPE, CREATED_DATE) - 액션별 이력

## 2.7 시스템 설정 및 관리 테이블

### 2.7.1 시스템 설정 테이블

**테이블명:** `SYSTEM_CONFIG`

**용도:** 시스템 전체 설정 관리

**테이블 구조:**

| 컬럼명 | 데이터타입 | 제약조건 | 설명 |
|--------|------------|----------|------|
| CONFIG_ID | INT | PK, IDENTITY | 설정 고유 식별자 |
| CONFIG_GROUP | VARCHAR(50) | NOT NULL | 설정 그룹 |
| CONFIG_KEY | VARCHAR(100) | NOT NULL | 설정 키 |
| CONFIG_VALUE | NVARCHAR(MAX) | NOT NULL | 설정 값 |
| DATA_TYPE | VARCHAR(20) | NOT NULL | 데이터 타입 |
| DESCRIPTION | NVARCHAR(500) | NULL | 설명 |
| IS_EDITABLE | BIT | NOT NULL, DEFAULT 1 | 편집 가능 여부 |
| IS_ACTIVE | BIT | NOT NULL, DEFAULT 1 | 활성 여부 |
| CREATED_DATE | DATETIME2 | NOT NULL, DEFAULT GETDATE() | 생성일시 |
| UPDATED_DATE | DATETIME2 | NULL | 수정일시 |

**기본 설정값:**

| 그룹 | 키 | 값 | 설명 |
|------|----|----|------|
| FILE | MAX_FILE_SIZE | 52428800 | 최대 파일 크기 (50MB) |
| FILE | ALLOWED_EXTENSIONS | .xlsx,.xls | 허용 파일 확장자 |
| PROCESSING | BATCH_SIZE | 1000 | 배치 처리 크기 |
| PROCESSING | MAX_CONCURRENT_SESSIONS | 10 | 최대 동시 세션 수 |
| TEMP_TABLE | RETENTION_HOURS | 24 | 임시 테이블 보존 시간 |
| PROGRESS | UPDATE_INTERVAL_MS | 1000 | 진행률 업데이트 간격 |

### 2.7.2 데이터 정리 작업 관리 테이블

**테이블명:** `CLEANUP_JOB_LOG`

**용도:** 자동 정리 작업 이력 관리

**테이블 구조:**

| 컬럼명 | 데이터타입 | 제약조건 | 설명 |
|--------|------------|----------|------|
| JOB_ID | BIGINT | PK, IDENTITY | 작업 고유 식별자 |
| JOB_TYPE | VARCHAR(50) | NOT NULL | 작업 유형 |
| TARGET_TABLE | VARCHAR(100) | NULL | 대상 테이블 |
| CRITERIA | NVARCHAR(500) | NULL | 정리 기준 |
| START_TIME | DATETIME2 | NOT NULL | 시작 시간 |
| END_TIME | DATETIME2 | NULL | 종료 시간 |
| AFFECTED_ROWS | INT | NULL | 영향받은 행 수 |
| SUCCESS | BIT | NOT NULL, DEFAULT 0 | 성공 여부 |
| ERROR_MESSAGE | NVARCHAR(MAX) | NULL | 오류 메시지 |
| EXECUTION_TIME | INT | NULL | 실행 시간 (초) |
| CREATED_DATE | DATETIME2 | NOT NULL, DEFAULT GETDATE() | 생성일시 |

## 2.8 데이터베이스 제약조건 및 관계 설계

### 2.8.1 외래키 관계 정의

**주요 참조 관계:**

```
META_UPLOAD_SESSION (1) ←→ (N) META_COLUMN_MAPPING
META_UPLOAD_SESSION (1) ←→ (N) META_TEMP_TABLE_SCHEMA
META_UPLOAD_SESSION (1) ←→ (1) PROGRESS_UPLOAD_MASTER
META_UPLOAD_SESSION (1) ←→ (N) PROGRESS_UPLOAD_DETAIL
META_UPLOAD_SESSION (1) ←→ (N) ERROR_LOG
META_UPLOAD_SESSION (1) ←→ (N) AUDIT_UPLOAD_HISTORY
ERROR_CATEGORY (1) ←→ (N) ERROR_LOG
ERROR_CATEGORY (1) ←→ (N) ERROR_STATISTICS
```

### 2.8.2 체크 제약조건

**주요 체크 제약조건:**

| 테이블 | 컬럼 | 제약조건 | 설명 |
|--------|------|----------|------|
| META_UPLOAD_SESSION | STATUS | IN ('UPLOADING','PROCESSING','COMPLETED','FAILED') | 유효한 상태값만 허용 |
| PROGRESS_UPLOAD_MASTER | OVERALL_PROGRESS | BETWEEN 0.00 AND 100.00 | 진행률 범위 제한 |
| ERROR_CATEGORY | SEVERITY_LEVEL | IN ('LOW','MEDIUM','HIGH','CRITICAL


**사고 과정 적용:**

이전 내용의 연속성을 유지하면서 **복잡성 해결 매트릭스(CS)**를 활용하여 나머지 제약조건, 성능 최적화, 보안 설계를 체계적으로 완성하겠습니다.

---

### 2.8.2 체크 제약조건 (계속)

**주요 체크 제약조건:**

| 테이블 | 컬럼 | 제약조건 | 설명 |
|--------|------|----------|------|
| META_UPLOAD_SESSION | STATUS | IN ('UPLOADING','PROCESSING','COMPLETED','FAILED') | 유효한 상태값만 허용 |
| PROGRESS_UPLOAD_MASTER | OVERALL_PROGRESS | BETWEEN 0.00 AND 100.00 | 진행률 범위 제한 |
| ERROR_CATEGORY | SEVERITY_LEVEL | IN ('LOW','MEDIUM','HIGH','CRITICAL') | 심각도 레벨 제한 |
| PROGRESS_UPLOAD_DETAIL | STATUS | IN ('PENDING','RUNNING','COMPLETED','FAILED') | 단계 상태 제한 |
| PROGRESS_UPLOAD_DETAIL | PROGRESS_PERCENT | BETWEEN 0.00 AND 100.00 | 단계별 진행률 범위 |
| META_COLUMN_MAPPING | COLUMN_INDEX | >= 0 | 컬럼 인덱스 음수 방지 |
| SYSTEM_CONFIG | DATA_TYPE | IN ('STRING','INTEGER','DECIMAL','BOOLEAN','JSON') | 지원 데이터 타입 제한 |

### 2.8.3 트리거 설계

**자동 업데이트 트리거:**

| 트리거명 | 대상 테이블 | 유형 | 목적 |
|----------|-------------|------|------|
| TRG_SESSION_UPDATE_TIME | META_UPLOAD_SESSION | UPDATE | UPDATED_DATE 자동 갱신 |
| TRG_PROGRESS_MASTER_UPDATE | PROGRESS_UPLOAD_MASTER | UPDATE | LAST_UPDATED 자동 갱신 |
| TRG_ERROR_STATISTICS_INSERT | ERROR_LOG | INSERT | ERROR_STATISTICS 자동 갱신 |
| TRG_AUDIT_SESSION_STATUS | META_UPLOAD_SESSION | UPDATE | 상태 변경 감사 로그 생성 |

## 2.9 성능 최적화 설계

### 2.9.1 인덱스 전략

**성능 최적화 인덱스:**

| 테이블 | 인덱스명 | 컬럼 | 유형 | 목적 |
|--------|----------|------|------|------|
| META_UPLOAD_SESSION | IX_SESSION_STATUS_TIME | (STATUS, START_TIME) | NONCLUSTERED | 상태별 시간 조회 최적화 |
| ERROR_LOG | IX_ERROR_SESSION_TIME | (SESSION_ID, OCCURRED_AT) | NONCLUSTERED | 세션별 오류 조회 |
| PROGRESS_UPLOAD_DETAIL | IX_PROGRESS_SESSION_STEP | (SESSION_ID, STEP_ORDER) | NONCLUSTERED | 진행률 단계 조회 |
| AUDIT_UPLOAD_HISTORY | IX_AUDIT_USER_DATE | (USER_ID, CREATED_DATE) | NONCLUSTERED | 사용자별 이력 조회 |
| TEMP_* (동적) | IX_TEMP_BUSINESS_KEY | (SALES_DOCUMENT, SALES_DOCUMENT_ITEM) | NONCLUSTERED | 비즈니스 키 조회 |

### 2.9.2 파티셔닝 전략

**대용량 테이블 파티셔닝:**

| 테이블 | 파티션 키 | 파티션 유형 | 파티션 기준 | 목적 |
|--------|-----------|-------------|-------------|------|
| ERROR_LOG | OCCURRED_AT | RANGE RIGHT | 월별 | 이력 데이터 분산 관리 |
| AUDIT_UPLOAD_HISTORY | CREATED_DATE | RANGE RIGHT | 월별 | 감사 로그 분산 저장 |
| TEMP_* (13,000건 이상) | ROW_NUMBER | RANGE RIGHT | 5,000건 단위 | 대용량 임시 데이터 분산 |

### 2.9.3 통계 정보 관리

**자동 통계 업데이트 설정:**

| 설정 항목 | 값 | 설명 |
|-----------|----|----|
| AUTO_CREATE_STATISTICS | ON | 자동 통계 생성 |
| AUTO_UPDATE_STATISTICS | ON | 자동 통계 업데이트 |
| AUTO_UPDATE_STATISTICS_ASYNC | ON | 비동기 통계 업데이트 |
| STATISTICS_INCREMENTAL | ON | 파티션별 통계 관리 |

## 2.10 보안 및 권한 설계

### 2.10.1 사용자 역할 정의

**데이터베이스 역할:**

| 역할명 | 권한 범위 | 설명 |
|--------|-----------|------|
| db_upload_admin | 모든 테이블 DDL/DML | 시스템 관리자 |
| db_upload_processor | 임시 테이블 생성/삭제, 데이터 처리 | 업로드 처리 서비스 |
| db_upload_reader | 진행률, 결과 조회 | 웹 애플리케이션 조회 |
| db_upload_cleaner | 정리 작업 전용 | 정리 작업 서비스 |

### 2.10.2 행 수준 보안 (RLS) 설계

**보안 정책:**

| 테이블 | 정책명 | 조건 | 목적 |
|--------|--------|------|------|
| META_UPLOAD_SESSION | POLICY_USER_SESSION | USER_ID = USER_NAME() | 사용자별 세션 격리 |
| PROGRESS_UPLOAD_MASTER | POLICY_USER_PROGRESS | SESSION_ID IN (사용자 세션) | 진행률 조회 제한 |
| ERROR_LOG | POLICY_USER_ERROR | SESSION_ID IN (사용자 세션) | 오류 로그 조회 제한 |

### 2.10.3 데이터 암호화

**민감 데이터 보호:**

| 테이블 | 컬럼 | 암호화 방법 | 설명 |
|--------|------|-------------|------|
| META_UPLOAD_SESSION | ORIGINAL_FILE_NAME | TDE | 파일명 투명 암호화 |
| ERROR_LOG | ORIGINAL_VALUE | COLUMN ENCRYPTION | 민감 데이터 컬럼 암호화 |
| AUDIT_UPLOAD_HISTORY | IP_ADDRESS | COLUMN ENCRYPTION | 개인정보 보호 |

## 2.11 데이터 보존 및 정리 정책

### 2.11.1 데이터 생명주기 관리

**테이블별 보존 정책:**

| 테이블 분류 | 보존 기간 | 정리 조건 | 아카이브 여부 |
|-------------|-----------|-----------|---------------|
| 임시 테이블 (TEMP_*) | 24시간 | 세션 완료 후 | 없음 |
| 진행률 테이블 | 7일 | 완료된 세션 | 없음 |
| 오류 로그 | 3개월 | 해결된 오류 | 월별 아카이브 |
| 감사 로그 | 1년 | 정기 정리 | 분기별 아카이브 |
| 메타데이터 | 6개월 | 참조되지 않는 세션 | 년별 아카이브 |

### 2.11.2 자동 정리 작업 설계

**정리 작업 스케줄:**

| 작업명 | 실행 주기 | 대상 | 조건 |
|--------|-----------|------|------|
| CLEANUP_TEMP_TABLES | 매시간 | TEMP_* 테이블 | 생성 후 24시간 경과 |
| CLEANUP_PROGRESS_DATA | 매일 02:00 | 진행률 테이블 | 완료 후 7일 경과 |
| CLEANUP_ERROR_LOGS | 매주 일요일 03:00 | ERROR_LOG | 해결 후 90일 경과 |
| CLEANUP_AUDIT_LOGS | 매월 1일 04:00 | AUDIT_UPLOAD_HISTORY | 생성 후 365일 경과 |

## 2.12 모니터링 및 알림 설계

### 2.12.1 성능 모니터링 뷰

**시스템 모니터링 뷰:**

| 뷰명 | 목적 | 주요 지표 |
|------|------|-----------|
| VW_SESSION_PERFORMANCE | 세션별 성능 분석 | 처리 시간, 처리량, 오류율 |
| VW_SYSTEM_HEALTH | 시스템 전체 상태 | 동시 세션 수, 리소스 사용량 |
| VW_ERROR_TREND | 오류 발생 트렌드 | 오류 유형별 발생률, 해결률 |
| VW_TEMP_TABLE_USAGE | 임시 테이블 사용률 | 테이블 수, 용량, 보존 기간 |

### 2.12.2 알림 임계값 설정

**알림 조건:**

| 지표 | 경고 임계값 | 위험 임계값 | 조치 방안 |
|------|-------------|-------------|-----------|
| 동시 세션 수 | 8개 | 10개 | 신규 업로드 제한 |
| 오류율 | 10% | 25% | 자동 처리 중단 |
| 임시 테이블 용량 | 10GB | 20GB | 강제 정리 작업 |
| 평균 처리 시간 | 30분 | 60분 | 성능 분석 요청 |

## 2.13 백업 및 복구 전략

### 2.13.1 백업 정책

**백업 유형별 전략:**

| 백업 유형 | 주기 | 대상 | 보존 기간 | 목적 |
|-----------|------|------|-----------|------|
| 전체 백업 | 주 1회 (일요일) | 전체 DB | 4주 | 장애 복구 |
| 차등 백업 | 일 1회 | 변경 데이터 | 1주 | 빠른 복구 |
| 로그 백업 | 15분마다 | 트랜잭션 로그 | 24시간 | 포인트 인 타임 복구 |
| 테이블 백업 | 처리 전후 | 업데이트 대상 테이블 | 72시간 | 롤백 지원 |

### 2.13.2 재해 복구 계획

**복구 시나리오별 대응:**

| 장애 유형 | 복구 목표 시간 (RTO) | 복구 목표 지점 (RPO) | 복구 절차 |
|-----------|---------------------|---------------------|-----------|
| 하드웨어 장애 | 4시간 | 15분 | 백업에서 복원 + 로그 복구 |
| 데이터 손상 | 2시간 | 처리 시작 시점 | 테이블 백업에서 복원 |
| 사용자 실수 | 30분 | 실수 직전 | 포인트 인 타임 복구 |
| 전체 시스템 장애 | 8시간 | 1시간 | 재해 복구 사이트 전환 |

## 2.14 데이터베이스 구축 체크리스트

### 2.14.1 구축 전 준비사항

**환경 준비 체크리스트:**

- [ ] **DB 서버 리소스 확인**
  - [ ] CPU: 최소 4코어 이상
  - [ ] 메모리: 최소 16GB 이상
  - [ ] 저장공간: 최소 500GB 이상 (임시 데이터 고려)
  - [ ] 네트워크: 1Gbps 이상

- [ ] **SQL Server 설정 확인**
  - [ ] 데이터 파일 초기 크기 및 증가율 설정
  - [ ] 로그 파일 초기 크기 및 증가율 설정
  - [ ] tempdb 설정 최적화
  - [ ] 최대 메모리 설정

- [ ] **보안 설정 확인**
  - [ ] TDE (투명 데이터 암호화) 활성화
  - [ ] 백업 암호화 설정
  - [ ] 로그인 감사 설정
  - [ ] 방화벽 규칙 설정

### 2.14.2 테이블 생성 순서

**생성 순서 체크리스트:**

1. **기준 테이블 생성**
   - [ ] SYSTEM_CONFIG
   - [ ] ERROR_CATEGORY

2. **메타데이터 테이블 생성**
   - [ ] META_UPLOAD_SESSION
   - [ ] META_COLUMN_MAPPING
   - [ ] META_TEMP_TABLE_SCHEMA

3. **진행률 관리 테이블 생성**
   - [ ] PROGRESS_UPLOAD_MASTER
   - [ ] PROGRESS_UPLOAD_DETAIL

4. **오류 관리 테이블 생성**
   - [ ] ERROR_LOG
   - [ ] ERROR_STATISTICS

5. **감사 테이블 생성**
   - [ ] AUDIT_UPLOAD_HISTORY
   - [ ] CLEANUP_JOB_LOG

### 2.14.3 인덱스 및 제약조건 생성

**인덱스 생성 체크리스트:**

- [ ] **기본 키 인덱스 확인**
  - [ ] 모든 테이블의 클러스터드 인덱스 생성 확인

- [ ] **외래 키 인덱스 생성**
  - [ ] 참조 무결성 제약조건 생성
  - [ ] 외래 키 컬럼에 대한 비클러스터드 인덱스 생성

- [ ] **성능 최적화 인덱스 생성**
  - [ ] 조회 패턴 기반 복합 인덱스 생성
  - [ ] 필터링 인덱스 생성 (필요시)

- [ ] **체크 제약조건 생성**
  - [ ] 도메인 값 제한 제약조건
  - [ ] 범위 제한 제약조건

### 2.14.4 초기 데이터 설정

**기본 데이터 입력 체크리스트:**

- [ ] **시스템 설정 데이터**
  - [ ] 파일 업로드 제한 설정
  - [ ] 처리 배치 크기 설정
  - [ ] 정리 작업 주기 설정

- [ ] **오류 분류 데이터**
  - [ ] 기본 오류 카테고리 등록
  - [ ] 심각도 레벨 설정
  - [ ] 해결 가이드 등록

### 2.14.5 권한 및 보안 설정

**보안 설정 체크리스트:**

- [ ] **사용자 및 역할 생성**
  - [ ] 애플리케이션 사용자 계정 생성
  - [ ] 서비스별 역할 정의 및 할당
  - [ ] 최소 권한 원칙 적용

- [ ] **행 수준 보안 활성화**
  - [ ] 보안 정책 생성
  - [ ] 보안 함수 정의
  - [ ] 테이블별 RLS 활성화

- [ ] **암호화 설정**
  - [ ] 컬럼 수준 암호화 키 생성
  - [ ] 민감 데이터 컬럼 암호화 적용

### 2.14.6 모니터링 설정

**모니터링 구성 체크리스트:**

- [ ] **성능 모니터링**
  - [ ] 확장 이벤트 세션 생성
  - [ ] 성능 카운터 설정
  - [ ] 대기 통계 수집 활성화

- [ ] **용량 모니터링**
  - [ ] 데이터 파일 자동 증가 모니터링
  - [ ] 임시 테이블 용량 추적 설정
  - [ ] 로그 파일 크기 모니터링

- [ ] **알림 설정**
  - [ ] SQL Server Agent 작업 생성
  - [ ] 임계값 초과 시 알림 설정
  - [ ] 오류 발생 시 자동 알림

## 2.15 성능 테스트 및 검증

### 2.15.1 성능 테스트 시나리오

**테스트 케이스:**

| 테스트 유형 | 데이터 크기 | 동시 세션 | 예상 처리 시간 | 검증 항목 |
|-------------|-------------|-----------|----------------|-----------|
| 소규모 테스트 | 1,000건 | 1개 | 30초 이내 | 기본 기능 동작 |
| 중규모 테스트 | 5,000건 | 3개 | 2분 이내 | 동시 처리 성능 |
| 대규모 테스트 | 13,000건 | 1개 | 5분 이내 | 대용량 처리 성능 |
| 부하 테스트 | 10,000건 | 10개 | 10분 이내 | 최대 부하 처리 |

### 2.15.2 검증 기준

**성능 검증 기준:**

| 지표 | 목표값 | 허용 한계 | 측정 방법 |
|------|--------|-----------|-----------|
| 처리 속도 | 200건/초 이상 | 100건/초 | 완료 시간 측정 |
| 메모리 사용량 | 8GB 이하 | 12GB | 성능 모니터 |
| CPU 사용률 | 80% 이하 | 95% | 성능 카운터 |
| 동시 세션 | 10개 | 15개 | 세션 수 추적 |

이로써 "2단계: 데이터베이스 설계 및 구축"의 상세 문서화가 완료되었습니다. 각 단계별로 구체적인 테이블 구조, 제약조건, 성능 최적화 방안, 보안 설계, 그리고 구축 체크리스트까지 포함하여 실제 개발과 운영에 필요한 모든 정보를 담았습니다.