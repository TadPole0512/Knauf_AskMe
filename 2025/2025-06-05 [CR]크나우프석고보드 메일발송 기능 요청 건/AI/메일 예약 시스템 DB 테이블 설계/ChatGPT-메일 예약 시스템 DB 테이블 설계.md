# 아래는 특정 고객 담당자 및 담당 영업사원에게 D+1(익일착) 주문에 대하여 특정시간에 메일(내용+첨부파일(엑셀))을 전달하는 기능을 위한 DB 테이블 정보야.
아래의 내용을 검토해서 메일 예약 시스템 구축을 위한 DB 테이블 정보를 구성하는데 더 필요한 정보가 뭐가 있을까?

```diff
주문메일알람
  - 거래처코드, 담장자 이메일, 담당자 이메일 발송 여부, 영업 담당 이메일, 영업 담당 이메일 발송 여부, 비고, 생성자, 생성일, 수정자, 수정일

에약발송정보
  - 스케줄ID, 스케줄명, 스케줄시작일, 스케줄시각, 스케줄분, 사용여부, 비고, 생성자, 생성일, 수정자, 수정일

예약발송로그
  - 스케줄ID, 거래처 코드, 담당자 이메일, 발송여부, 발송시각, 발송 정보, 실패 코드, 실패 메시지, 생성자, 생성일
```
```
[개발환경]
- 운영체제 : 윈도우
- 개발툴 : STS4.5
- 자바 : Zulu-8
- WAS : Tomcat 9
- DB : mssql
- Maven
- MyBatis
- JQGrid
```

---------------------------------------------------------------------

좋아, 맥락은 “D+1(익일착) 주문 리스트를 특정 시각에 자동 메일(본문+엑셀 첨부)로 보내는 예약 발송 시스템”이지.
아래는 **지금 정의한 3개 테이블을 바탕으로 실제 운영에 필요한 누락/추가 항목**을 정리하고, **추천 스키마(필드+타입)**, **흐름/운영 체크리스트**까지 한 번에 잡아줄게.

---

# 1) 전제(Assumptions)

* 시간대는 KST(Asia/Seoul) 기준.
* “D+1 주문”은 오늘 기준 익일 도착 예정 주문(쿼리는 별도).
* 첨부는 **엑셀(XLSX)**, 파일은 서버 디스크에 생성 후 메일에 첨부.
* SMTP 발송(내/외부 SMTP), 실패 시 재시도 필요.
* 동일 고객/날짜/스케줄로 **중복 발송 방지** 필요.
* 템플릿(제목/본문)과 대상 선정 규칙(쿼리/프로시저)은 바뀔 수 있음 → **버전/룰 분리**가 안전.

---

# 2) 필수로 더 필요한 정보(요약)

1. **식별자/키**

   * 각 테이블에 **PK(대리키)** 필요(예: BIGINT IDENTITY).
   * 자연키(거래처코드+이메일)만으로는 변경/중복 처리 힘듦.

2. **수신자/대상 관리**

   * 담당자, 영업담당 **이메일 주소 외에 사용자ID**(사번/USERID) 보관 권장.
   * **CC/BCC**, **회신주소(Reply-To)**, **발신프로필(From, 발신명)**.

3. **스케줄 주기와 휴일/주말 정책**

   * DAILY/WEEKLY/CRON 유형, 요일 선택, **공휴일 처리(전/후 이동, 스킵)**.
   * **유효기간(ActiveFrom/To)**, **TimeZone**(기본 KST지만 필드로 남기기).

4. **템플릿/다국어/버전**

   * 제목/본문 **템플릿 테이블**(변수 바인딩용), 상태(Active), 버전.

5. **첨부 생성 정의**

   * 어떤 **쿼리/프로시저**로 파일을 만드는지, **파일명 패턴**, **시트명**, **열 헤더/순서**.
   * **생성 결과(경로/행수/상태) 로그**.

6. **발송 큐(Outbox) & 재시도/중복방지**

   * 상태(PENDING/SENDING/SENT/FAILED/SKIPPED), 시도횟수, **다음 재시도시각**, **중복해시**.
   * **배치 크기 제한**, 속도 제한(분당 N건).

7. **로그 세분화**

   * SMTP 응답ID, 실패코드(연결/인증/수신거부/첨부생성실패/템플릿에러 등 **표준화 코드**), 원문 메시지.
   * \*\*상관관계ID(Correlation ID)\*\*로 스케줄 실행 묶음 식별.

8. **컴플라이언스/옵트아웃**

   * **수신거부(OPT\_OUT) 테이블**, 개인정보 최소화, 마스킹 정책.

9. **인덱스/성능**

   * 스케줄 실행 시각, 상태, 거래처코드, 이메일에 적절한 인덱스.
   * D+1 조회 쿼리 성능 고려(필요 시 **스냅샷/머터리얼라이즈드 룰**).

---

# 3) 제안 스키마(필드 & 자료형: MS SQL Server 기준)

## 3.1 예약 스케줄

**MAIL\_SCHEDULE**

* `schedule_id` BIGINT IDENTITY PK
* `schedule_name` NVARCHAR(100) NOT NULL
* `schedule_type` VARCHAR(20) NOT NULL  -- DAILY/WEEKLY/CRON
* `cron_expr` VARCHAR(100) NULL        -- CRON일 때
* `by_hour` TINYINT NULL, `by_minute` TINYINT NULL  -- 간단 스케줄일 때
* `days_of_week` VARCHAR(20) NULL      -- “MON,TUE,...”
* `timezone` VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul'
* `active_from` DATE NULL, `active_to` DATE NULL
* `holiday_policy` VARCHAR(20) NULL     -- SKIP/ADVANCE/DELAY
* `max_retry` TINYINT NOT NULL DEFAULT 3
* `retry_backoff_sec` INT NOT NULL DEFAULT 300
* `batch_size` INT NOT NULL DEFAULT 200
* `use_yn` CHAR(1) NOT NULL DEFAULT 'Y'
* `remark` NVARCHAR(500) NULL
* `created_by` NVARCHAR(50), `created_at` DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
* `updated_by` NVARCHAR(50), `updated_at` DATETIME2 NULL

> 인덱스: (use\_yn, active\_from, active\_to), (schedule\_type, by\_hour, by\_minute), (cron\_expr)

## 3.2 템플릿

**MAIL\_TEMPLATE**

* `template_id` BIGINT IDENTITY PK
* `template_name` NVARCHAR(100) NOT NULL
* `locale` VARCHAR(10) NOT NULL DEFAULT 'ko-KR'
* `subject_tpl` NVARCHAR(300) NOT NULL   -- 예: “\[{CUST\_NM}] D+1 주문 {ORDER\_DT}”
* `body_html_tpl` NVARCHAR(MAX) NOT NULL -- 템플릿 변수 사용
* `body_text_tpl` NVARCHAR(MAX) NULL
* `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
* `version` INT NOT NULL DEFAULT 1
* 공통 감사컬럼들…

> 인덱스: (template\_name, status), (locale)

**SCHEDULE\_TEMPLATE\_MAP**

* `schedule_id` BIGINT FK -> MAIL\_SCHEDULE
* `template_id` BIGINT FK -> MAIL\_TEMPLATE
* PK(schedule\_id, template\_id)

## 3.3 첨부 생성 정의

**ATTACHMENT\_DEF**

* `attachment_id` BIGINT IDENTITY PK
* `attachment_name` NVARCHAR(100) NOT NULL
* `generator_type` VARCHAR(20) NOT NULL  -- SP/QUERY/JAVA
* `generator_ref` NVARCHAR(200) NOT NULL -- 프로시저명/쿼리ID/핸들러명
* `file_name_pattern` NVARCHAR(200) NOT NULL -- 예: “D+1\_{CUST\_CD}\_{YYYYMMDD}.xlsx”
* `format` VARCHAR(10) NOT NULL DEFAULT 'XLSX'
* `sheet_name` NVARCHAR(50) NULL
* `remark` NVARCHAR(500) NULL
* 감사컬럼…

**SCHEDULE\_ATTACHMENT\_MAP**

* `schedule_id` BIGINT FK
* `attachment_id` BIGINT FK
* `sort_no` INT NOT NULL DEFAULT 1
* PK(schedule\_id, attachment\_id)

**ATTACHMENT\_RUN\_LOG**

* `run_id` BIGINT IDENTITY PK
* `schedule_id` BIGINT, `attachment_id` BIGINT, `cust_cd` VARCHAR(20) NULL
* `generated_path` NVARCHAR(300) NULL
* `rows_count` INT NULL
* `status` VARCHAR(20) NOT NULL  -- STARTED/SUCCESS/FAILED
* `started_at` DATETIME2 NOT NULL, `finished_at` DATETIME2 NULL
* `error_code` VARCHAR(50) NULL, `error_message` NVARCHAR(1000) NULL

## 3.4 대상/알람 설정(기존 “주문메일알람” 확장)

**CUSTOMER\_MAIL\_ALERT**

* `alert_id` BIGINT IDENTITY PK
* `cust_cd` VARCHAR(20) NOT NULL
* `contact_user_id` VARCHAR(50) NULL
* `contact_email` VARCHAR(200) NULL
* `contact_send_yn` CHAR(1) NOT NULL DEFAULT 'Y'
* `salesrep_user_id` VARCHAR(50) NULL
* `salesrep_email` VARCHAR(200) NULL
* `salesrep_send_yn` CHAR(1) NOT NULL DEFAULT 'N'
* `cc` NVARCHAR(500) NULL, `bcc` NVARCHAR(500) NULL
* `reply_to` NVARCHAR(200) NULL
* `preferred_locale` VARCHAR(10) NULL DEFAULT 'ko-KR'
* `use_yn` CHAR(1) NOT NULL DEFAULT 'Y'
* `remark` NVARCHAR(500) NULL
* 감사컬럼…

> 인덱스: (cust\_cd), (use\_yn), (contact\_email), (salesrep\_email)

**SCHEDULE\_TARGET**  *(스케줄 대상 고객을 고정/동적 관리)*

* `schedule_id` BIGINT FK
* `cust_cd` VARCHAR(20) NULL
* `rule_type` VARCHAR(20) NOT NULL DEFAULT 'STATIC'  -- STATIC/PROCEDURE/QUERY
* `rule_ref` NVARCHAR(200) NULL     -- 프로시저/조회ID
* PK(schedule\_id, cust\_cd, rule\_type)  -- STATIC면 cust\_cd 필수, RULE이면 cust\_cd NULL

## 3.5 발송 큐/로그(기존 “예약발송로그” 확장)

**MAIL\_OUTBOX**  *(발송 큐 + 상태 추적)*

* `mail_id` BIGINT IDENTITY PK
* `schedule_id` BIGINT NOT NULL
* `correlation_id` UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID()  -- 실행 묶음
* `cust_cd` VARCHAR(20) NULL
* `to_email` NVARCHAR(500) NOT NULL
* `cc` NVARCHAR(500) NULL, `bcc` NVARCHAR(500) NULL
* `from_email` NVARCHAR(200) NOT NULL, `from_name` NVARCHAR(100) NULL
* `subject_rendered` NVARCHAR(500) NOT NULL
* `body_html_rendered` NVARCHAR(MAX) NULL
* `body_text_rendered` NVARCHAR(MAX) NULL
* `attachments` NVARCHAR(MAX) NULL   -- 경로 JSON 배열
* `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' -- PENDING/SENDING/SENT/FAILED/SKIPPED
* `attempt_count` TINYINT NOT NULL DEFAULT 0
* `next_attempt_at` DATETIME2 NULL
* `sent_at` DATETIME2 NULL
* `provider_message_id` NVARCHAR(200) NULL
* `dedupe_hash` CHAR(64) NULL  -- (schedule\_id + cust\_cd + yyyy-mm-dd 등)
* `error_code` VARCHAR(50) NULL, `error_message` NVARCHAR(1000) NULL
* `created_at` DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()

> 인덱스: (status, next\_attempt\_at), (schedule\_id, created\_at), (dedupe\_hash)

**MAIL\_EVENT\_LOG** *(세부 이벤트 기록 – 기존 “예약발송로그” 확장판)*

* `event_id` BIGINT IDENTITY PK
* `mail_id` BIGINT FK -> MAIL\_OUTBOX
* `event_type` VARCHAR(30) NOT NULL   -- QUEUED/SEND\_START/SENT/FAILED/RETRY/DEDUPE\_SKIP
* `event_at` DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
* `detail_code` VARCHAR(50) NULL
* `detail_message` NVARCHAR(1000) NULL

## 3.6 수신거부/블락

**MAIL\_OPT\_OUT**

* `email` NVARCHAR(200) PK
* `reason` NVARCHAR(300) NULL
* `unsubscribed_at` DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()

---

# 4) 동작 흐름(운영 시나리오)

1. **스케줄 선택**

   * 현재 KST 시각 기준 실행 대상 `MAIL_SCHEDULE` 검색(Active+시간일치+요일/휴일정책).
2. **대상 고객 결정**

   * `SCHEDULE_TARGET`: STATIC 목록 + RULE(PROCEDURE/QUERY) 결과 merge.
   * 고객별 `CUSTOMER_MAIL_ALERT`에서 실제 **수신자 구성(담당/영업/CC/BCC/Reply-To)**.
   * `MAIL_OPT_OUT`와 **수신 제외** 합집합 처리.
3. **첨부 생성**

   * 각 고객에 대해 `SCHEDULE_ATTACHMENT_MAP` 순서대로 `ATTACHMENT_DEF` 실행.
   * `ATTACHMENT_RUN_LOG`에 **경로/행수/상태** 기록.
4. **템플릿 바인딩**

   * `MAIL_TEMPLATE` 렌더링(변수: {CUST\_CD}, {CUST\_NM}, {ORDER\_DT}, 건수 등).
5. **중복방지 키 생성**

   * `dedupe_hash = SHA256(schedule_id + cust_cd + 대상날짜)` 등.
   * 동일 해시 존재 시 `DEDUPE_SKIP` 이벤트 기록 후 건너뜀.
6. **큐 적재 → 발송**

   * `MAIL_OUTBOX`에 PENDING 적재 → 워커(스레드/스케줄러)가 SMTP 발송.
   * 성공: SENT, `sent_at` 기록. 실패: FAILED, `attempt_count++`, `next_attempt_at` = now + backoff, `max_retry` 초과 시 최종 FAILED.
7. **모니터링/리포트**

   * `MAIL_EVENT_LOG` 기반 실패율, 재시도 현황, 고객별 발송 통계.

---

# 5) 인덱스/제약 체크리스트

* `MAIL_OUTBOX(status, next_attempt_at)` **필수**(워커가 가져갈 대상 검색).
* `MAIL_OUTBOX(dedupe_hash)` 유니크 인덱스(선택)로 **중복 방지 강화**.
* `CUSTOMER_MAIL_ALERT(cust_cd)`, `MAIL_SCHEDULE(use_yn, active_from, active_to)`.
* 외래키(FK)와 **ON DELETE/UPDATE 규칙** 명확히(보통 RESTRICT).

---

# 6) 예시 DDL 스니펫 (MSSQL)

```sql
CREATE TABLE MAIL_SCHEDULE (
  schedule_id BIGINT IDENTITY PRIMARY KEY,
  schedule_name NVARCHAR(100) NOT NULL,
  schedule_type VARCHAR(20) NOT NULL,
  cron_expr VARCHAR(100) NULL,
  by_hour TINYINT NULL,
  by_minute TINYINT NULL,
  days_of_week VARCHAR(20) NULL,
  timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul',
  active_from DATE NULL,
  active_to DATE NULL,
  holiday_policy VARCHAR(20) NULL,
  max_retry TINYINT NOT NULL DEFAULT 3,
  retry_backoff_sec INT NOT NULL DEFAULT 300,
  batch_size INT NOT NULL DEFAULT 200,
  use_yn CHAR(1) NOT NULL DEFAULT 'Y',
  remark NVARCHAR(500) NULL,
  created_by NVARCHAR(50) NULL,
  created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
  updated_by NVARCHAR(50) NULL,
  updated_at DATETIME2 NULL
);
CREATE INDEX IX_MAIL_SCHEDULE_ACTIVE ON MAIL_SCHEDULE(use_yn, active_from, active_to);

CREATE TABLE MAIL_OUTBOX (
  mail_id BIGINT IDENTITY PRIMARY KEY,
  schedule_id BIGINT NOT NULL,
  correlation_id UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
  cust_cd VARCHAR(20) NULL,
  to_email NVARCHAR(500) NOT NULL,
  cc NVARCHAR(500) NULL,
  bcc NVARCHAR(500) NULL,
  from_email NVARCHAR(200) NOT NULL,
  from_name NVARCHAR(100) NULL,
  subject_rendered NVARCHAR(500) NOT NULL,
  body_html_rendered NVARCHAR(MAX) NULL,
  body_text_rendered NVARCHAR(MAX) NULL,
  attachments NVARCHAR(MAX) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  attempt_count TINYINT NOT NULL DEFAULT 0,
  next_attempt_at DATETIME2 NULL,
  sent_at DATETIME2 NULL,
  provider_message_id NVARCHAR(200) NULL,
  dedupe_hash CHAR(64) NULL,
  error_code VARCHAR(50) NULL,
  error_message NVARCHAR(1000) NULL,
  created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
  CONSTRAINT FK_MAIL_OUTBOX_SCHEDULE FOREIGN KEY (schedule_id) REFERENCES MAIL_SCHEDULE(schedule_id)
);
CREATE INDEX IX_MAIL_OUTBOX_PICK ON MAIL_OUTBOX(status, next_attempt_at);
CREATE INDEX IX_MAIL_OUTBOX_SCH ON MAIL_OUTBOX(schedule_id, created_at);
CREATE INDEX IX_MAIL_OUTBOX_DEDUPE ON MAIL_OUTBOX(dedupe_hash);
```

> 나머지 테이블도 위 형태 그대로 확장하면 돼. 필요하면 전체 DDL도 만들어줄게.

---

# 7) 운영 고려사항(리얼 이슈 포인트)

* **타임존/서머타임**: 지금은 KST 고정이지만, 필드로 남겨두면 나중 확장 쉬움.
* **파일 보존/정리**: 첨부 파일 **보존기간/정리 배치**(예: 30일).
* **템플릿 변수 검증**: 누락 변수는 빌드 단계에서 실패 처리(명확한 에러코드).
* **SMTP 연결/인증 에러** 분류: 네트워크/계정/수신자/콘텐츠(스팸차단) 구분.
* **수신거부/바운스 처리**: 반송(Bounce) 피드백 루프 있으면 자동 등록.
* **Idempotency**: 발송 재시도 시 **같은 mail\_id** 유지, **dedupe\_hash**로 중복 차단.
* **JQGrid 관리 화면**: 스케줄/대상/템플릿/첨부/로그 필터, 재시도 버튼, 미리보기(샘플 렌더링) 제공.

---

# 8) 짤막 체크리스트

* [ ] 스케줄: 주기/휴일/시간대/활성기간 정의
* [ ] 대상: 정적 목록 + 규칙(SQL/프로시저) 병행
* [ ] 템플릿: 제목/HTML/TXT, 로케일/버전
* [ ] 첨부: 생성 규칙/파일명 패턴/시트/로그
* [ ] 큐: 상태/재시도/중복방지/속도제어
* [ ] 로그: 이벤트/실패코드/상관관계ID
* [ ] 컴플라이언스: OPT-OUT, 마스킹, 보존기간
* [ ] 인덱스/성능: PENDING 픽업, D+1 쿼리 최적화

---

## 끝으로, 딱 3가지 확인 질문

1. **템플릿**을 정적(거의 고정)으로 둘까, 아니면 **변수/버전**을 자주 바꿀 계획이 있을까?
2. D+1 주문 **대상 선정 방법**: 고정 고객 리스트 vs. **프로시저로 매번 동적** 선정 중 무엇이 맞아?
3. **중복 발송 기준**을 “스케줄ID+거래처+주문기준일”로 묶어 차단해도 문제없을까?

원하면, 위 스키마 전체 DDL이랑 **샘플 데이터 + 배치/워커 의사코드**까지 바로 만들어줄게.
