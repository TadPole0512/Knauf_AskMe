# MMS 중복 방지 시스템 완전 가이드

## 목차
1. [현재 상황 분석](#1-현재-상황-분석)
2. [현재 MMS 동작 프로세스](#2-현재-mms-동작-프로세스)
3. [제안하는 중복 방지 프로세스](#3-제안하는-중복-방지-프로세스)
4. [개선된 전체 프로세스](#4-개선된-전체-프로세스)
5. [기대 효과](#5-기대-효과)
6. [주의사항](#6-주의사항)
7. [CREATE TRIGGER 구현](#7-create-trigger-구현)
8. [성능 최적화 방안](#8-성능-최적화-방안)
9. [테스트 가이드](#9-테스트-가이드)

---

## 1. 현재 상황 분석

### 문제점
- **근본 원인**: SAP에서 알 수 없는 이유로 MMS DB에 중복 문자 전송 데이터가 생성됨
- **고객 영향**: 동일한 오더접수/보류오더 MMS가 여러 번 전송되는 상황 발생
- **데이터 정합성**: E-Order와 같은 DB를 활용하는 과정에서 데이터 일관성 문제

### 대응 방향
- **장기 해결책**: SAP 원인 검증 및 데이터 수정 요청 중 (근본적 해결책)
- **단기 해결책**: 임시 해결책으로 중복 검증 옵션 추가 (Create Trigger 활용)
- **서비스 연속성**: 고객 서비스 중단 없이 문제 해결

---

## 2. 현재 MMS 동작 프로세스

### 단계별 업무 흐름
```
Step 1: SAP 오더 확정
   ↓
Step 2: MMS_MSG Table에 오더 정보 전송
   ↓
Step 3: MMS 전송 처리
   ↓
Step 4: 전송 완료 시 STATUS = 3으로 변경
   ↓
Step 5: MMS_LOG_YYYYMM Table로 데이터 이동
```

### 테이블 구조 및 역할
- **MMS_MSG Table**: 현재 처리 중인 MMS 데이터 저장
- **MMS_LOG_YYYYMM Table**: 전송 완료된 MMS 데이터의 월별 이력 관리
- **STATUS 컬럼**: 처리 상태 관리 (3 = 전송 완료/스킵)

---

## 3. 제안하는 중복 방지 프로세스

### Create Trigger 동작 조건 및 프로세스

#### 3-1. 트리거 발동 조건
- **발동 시점**: MMS_MSG Table에 새로운 데이터(INSERT) 생성 시
- **검증 대상**: phone, msg 컬럼의 동일성 확인
- **처리 방식**: 중복 발견 시 STATUS = 3으로 자동 설정

#### 3-2. 중복 검증 프로세스

**Step 1: 신규 데이터 INSERT 감지**
- MMS_MSG Table에 새로운 레코드 삽입 시 트리거 실행
- AFTER INSERT 트리거로 데이터 삽입 후 검증 수행

**Step 2: 현재 테이블 내 중복 검증**
```sql
조건: MMS_MSG Table 내에서 
- phone 컬럼 값이 동일 AND
- msg 컬럼 값이 동일한 기존 데이터 존재 여부 확인
- 자기 자신(방금 INSERT된 레코드) 제외
```
- **결과**: 중복 발견 시 → 신규 데이터의 STATUS = 3으로 설정

**Step 3: 이력 테이블 중복 검증**
```sql
조건: MMS_LOG_YYYYMM Table에서
- phone 컬럼 값이 동일 AND  
- msg 컬럼 값이 동일한 데이터 존재 여부 확인
- 검증 범위: 당월 테이블 + 전월 테이블
```
- **결과**: 중복 발견 시 → 신규 데이터의 STATUS = 3으로 설정

#### 3-3. 월경계 처리 로직
- **특수 상황**: 6월 30일 오더가 7월 1일에 반복 전송될 가능성 고려
- **검증 테이블**: MMS_LOG_202406 + MMS_LOG_202407 동시 확인
- **동적 테이블명**: 현재월(YYYYMM) + 이전월(YYYY(MM-1)) 형태로 처리
- **안정성**: 테이블이 존재하지 않아도 처리 중단되지 않도록 예외 처리

---

## 4. 개선된 전체 프로세스

### 수정된 업무 흐름
```
Step 1: SAP 오더 확정
   ↓
Step 2: MMS_MSG Table에 오더 정보 전송
   ↓
Step 3: [NEW] Create Trigger 실행
   ├─ 현재 MMS_MSG Table 중복 검증 (phone + msg)
   ├─ MMS_LOG_YYYYMM Table 중복 검증 (당월 + 전월)
   └─ 중복 발견 시 STATUS = 3 설정
   ↓
Step 4: STATUS 확인
   ├─ STATUS ≠ 3: MMS 전송 처리
   └─ STATUS = 3: 전송 스킵 (중복으로 판단)
   ↓
Step 5: 전송 완료 시 STATUS = 3으로 변경
   ↓
Step 6: MMS_LOG_YYYYMM Table로 데이터 이동
```

### 프로세스 개선 포인트
- **자동화**: 수동 중복 확인 작업 불필요
- **실시간**: INSERT 즉시 중복 검증 수행
- **포괄적**: 현재 데이터 + 이력 데이터 모두 검증
- **안정성**: 오류 발생 시에도 서비스 중단 없음

---

## 5. 기대 효과

### 즉시 효과
- **중복 전송 방지**: 동일한 phone + msg 조합의 중복 전송 완전 차단
- **고객 만족도 향상**: 중복 문자 수신으로 인한 고객 불편 최소화
- **운영 효율성**: 수동 모니터링 및 수정 작업 감소

### 장기 효과
- **서비스 안정성**: SAP 근본 원인 해결 시까지 안정적인 서비스 제공
- **데이터 정합성 향상**: 중복 데이터로 인한 부정확한 통계 방지
- **시스템 신뢰성**: 자동화된 중복 방지 메커니즘으로 시스템 안정성 확보

### 비용 절감 효과
- **통신비 절약**: 불필요한 중복 MMS 전송 비용 절감
- **인력 절약**: 수동 모니터링 및 데이터 정정 작업 감소
- **고객 지원 비용**: 중복 전송 관련 고객 문의 감소

---

## 6. 주의사항

### 트리거 구현 시 고려사항
- **성능 영향**: 빠른 검색을 위한 인덱스 활용 필수
- **동적 테이블명**: 월경계 처리 시 테이블명 동적 생성 로직 안정성 확보
- **예외 처리**: 트리거 실행 실패 시에도 INSERT 작업이 중단되지 않도록 처리
- **로깅**: 트리거 실행 결과 모니터링을 위한 적절한 로깅 구현

### 운영 관리 포인트
- **정기 모니터링**: 트리거 실행 상태 및 중복 방지 효과 정기 확인
- **성능 모니터링**: INSERT 성능에 미치는 영향 지속적 관찰
- **데이터 검증**: 중복 방지 로직이 정상적으로 작동하는지 주기적 검증

---

## 7. CREATE TRIGGER 구현

### 7-1. 메인 트리거 코드

```sql
-- =====================================================
-- MMS 중복 전송 방지 CREATE TRIGGER
-- 테이블: MMS_MSG
-- 목적: phone + msg 조합 중복 시 STATUS = 3으로 자동 설정
-- =====================================================

CREATE OR REPLACE TRIGGER TRG_MMS_DUPLICATE_CHECK
    AFTER INSERT ON MMS_MSG
    FOR EACH ROW
DECLARE
    v_duplicate_count NUMBER := 0;
    v_current_month VARCHAR2(6);
    v_previous_month VARCHAR2(6);
    v_sql VARCHAR2(4000);
    v_log_count NUMBER := 0;
BEGIN
    -- 현재 년월과 이전 년월 계산
    v_current_month := TO_CHAR(SYSDATE, 'YYYYMM');
    v_previous_month := TO_CHAR(ADD_MONTHS(SYSDATE, -1), 'YYYYMM');
    
    -- 1단계: MMS_MSG 테이블 내 중복 검증 (자기 자신 제외)
    SELECT COUNT(*)
    INTO v_duplicate_count
    FROM MMS_MSG 
    WHERE phone = :NEW.phone 
      AND msg = :NEW.msg
      AND ROWID != :NEW.ROWID;  -- 방금 INSERT된 자기 자신 제외
    
    -- MMS_MSG 테이블에서 중복 발견 시 STATUS = 3으로 업데이트
    IF v_duplicate_count > 0 THEN
        UPDATE MMS_MSG 
        SET status = 3 
        WHERE ROWID = :NEW.ROWID;
        
        -- 로그 출력 (선택사항)
        DBMS_OUTPUT.PUT_LINE('중복 발견 - MMS_MSG 테이블: phone=' || :NEW.phone || ', msg=' || SUBSTR(:NEW.msg, 1, 50));
        RETURN; -- 중복 발견 시 더 이상 검사하지 않음
    END IF;
    
    -- 2단계: 현재월 MMS_LOG 테이블 중복 검증
    BEGIN
        v_sql := 'SELECT COUNT(*) FROM MMS_LOG_' || v_current_month || 
                ' WHERE phone = :1 AND msg = :2';
        
        EXECUTE IMMEDIATE v_sql INTO v_log_count USING :NEW.phone, :NEW.msg;
        
        IF v_log_count > 0 THEN
            UPDATE MMS_MSG 
            SET status = 3 
            WHERE ROWID = :NEW.ROWID;
            
            DBMS_OUTPUT.PUT_LINE('중복 발견 - MMS_LOG_' || v_current_month || ': phone=' || :NEW.phone);
            RETURN;
        END IF;
        
    EXCEPTION
        WHEN OTHERS THEN
            -- 현재월 테이블이 존재하지 않는 경우 무시
            DBMS_OUTPUT.PUT_LINE('MMS_LOG_' || v_current_month || ' 테이블이 존재하지 않습니다.');
    END;
    
    -- 3단계: 이전월 MMS_LOG 테이블 중복 검증 (월경계 처리)
    BEGIN
        v_sql := 'SELECT COUNT(*) FROM MMS_LOG_' || v_previous_month || 
                ' WHERE phone = :1 AND msg = :2';
        
        EXECUTE IMMEDIATE v_sql INTO v_log_count USING :NEW.phone, :NEW.msg;
        
        IF v_log_count > 0 THEN
            UPDATE MMS_MSG 
            SET status = 3 
            WHERE ROWID = :NEW.ROWID;
            
            DBMS_OUTPUT.PUT_LINE('중복 발견 - MMS_LOG_' || v_previous_month || ': phone=' || :NEW.phone);
            RETURN;
        END IF;
        
    EXCEPTION
        WHEN OTHERS THEN
            -- 이전월 테이블이 존재하지 않는 경우 무시
            DBMS_OUTPUT.PUT_LINE('MMS_LOG_' || v_previous_month || ' 테이블이 존재하지 않습니다.');
    END;
    
    -- 중복이 발견되지 않은 경우 정상 처리
    DBMS_OUTPUT.PUT_LINE('정상 처리 - 중복 없음: phone=' || :NEW.phone);
    
EXCEPTION
    WHEN OTHERS THEN
        -- 트리거 실행 중 오류 발생 시 로그 출력
        DBMS_OUTPUT.PUT_LINE('트리거 실행 오류: ' || SQLERRM);
        -- 오류가 발생해도 INSERT는 정상 진행되도록 함
END TRG_MMS_DUPLICATE_CHECK;
/
```

### 7-2. 트리거 코드 상세 설명

#### 변수 선언부
- `v_duplicate_count`: MMS_MSG 테이블 내 중복 개수 저장
- `v_current_month`: 현재 년월 (YYYYMM 형식)
- `v_previous_month`: 이전 년월 (YYYYMM 형식)
- `v_sql`: 동적 SQL 쿼리 저장
- `v_log_count`: MMS_LOG 테이블 내 중복 개수 저장

#### 1단계: MMS_MSG 테이블 중복 검증
```sql
SELECT COUNT(*)
INTO v_duplicate_count
FROM MMS_MSG 
WHERE phone = :NEW.phone 
  AND msg = :NEW.msg
  AND ROWID != :NEW.ROWID;
```
- 방금 INSERT된 레코드(:NEW)와 동일한 phone, msg 조합 검색
- ROWID 조건으로 자기 자신은 제외
- 중복 발견 시 즉시 STATUS = 3으로 설정 후 리턴

#### 2단계: 현재월 MMS_LOG 테이블 검증
```sql
v_sql := 'SELECT COUNT(*) FROM MMS_LOG_' || v_current_month || 
        ' WHERE phone = :1 AND msg = :2';
EXECUTE IMMEDIATE v_sql INTO v_log_count USING :NEW.phone, :NEW.msg;
```
- 동적 SQL로 현재월 테이블명 생성
- EXECUTE IMMEDIATE로 동적 쿼리 실행
- 테이블이 존재하지 않으면 예외 처리로 무시

#### 3단계: 이전월 MMS_LOG 테이블 검증
- 현재월과 동일한 로직으로 이전월 테이블 검증
- 월경계 처리를 위한 필수 단계
- 6월 30일 → 7월 1일 케이스 대응

---

## 8. 성능 최적화 방안

### 8-1. 인덱스 생성

```sql
-- =====================================================
-- 성능 향상을 위한 인덱스 생성 (권장)
-- =====================================================

-- MMS_MSG 테이블용 복합 인덱스
CREATE INDEX IDX_MMS_MSG_PHONE_MSG ON MMS_MSG(phone, msg);

-- MMS_LOG 테이블용 복합 인덱스 (월별로 생성 필요)
-- 예시: 현재월과 이전월
CREATE INDEX IDX_MMS_LOG_202407_PHONE_MSG ON MMS_LOG_202407(phone, msg);
CREATE INDEX IDX_MMS_LOG_202406_PHONE_MSG ON MMS_LOG_202406(phone, msg);

-- 새로운 월이 시작되면 해당 월의 인덱스도 생성 필요
-- CREATE INDEX IDX_MMS_LOG_202408_PHONE_MSG ON MMS_LOG_202408(phone, msg);
```

### 8-2. 인덱스 관리 방안

#### 자동 인덱스 생성 스크립트
```sql
-- 새로운 MMS_LOG 테이블 생성 시 자동으로 인덱스도 생성하는 프로시저
CREATE OR REPLACE PROCEDURE CREATE_MMS_LOG_INDEX(p_year_month VARCHAR2)
IS
    v_index_sql VARCHAR2(500);
    v_table_name VARCHAR2(50);
    v_index_name VARCHAR2(50);
BEGIN
    v_table_name := 'MMS_LOG_' || p_year_month;
    v_index_name := 'IDX_MMS_LOG_' || p_year_month || '_PHONE_MSG';
    
    v_index_sql := 'CREATE INDEX ' || v_index_name || 
                   ' ON ' || v_table_name || '(phone, msg)';
    
    EXECUTE IMMEDIATE v_index_sql;
    
    DBMS_OUTPUT.PUT_LINE('인덱스 생성 완료: ' || v_index_name);
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('인덱스 생성 실패: ' || SQLERRM);
END;
/
```

### 8-3. 성능 모니터링 쿼리

```sql
-- 트리거 실행 성능 모니터링
SELECT 
    trigger_name,
    trigger_type,
    triggering_event,
    status
FROM user_triggers 
WHERE trigger_name = 'TRG_MMS_DUPLICATE_CHECK';

-- 인덱스 사용률 확인
SELECT 
    table_name,
    index_name,
    uniqueness,
    status
FROM user_indexes 
WHERE table_name IN ('MMS_MSG', 'MMS_LOG_202407', 'MMS_LOG_202406');

-- 테이블별 레코드 수 확인
SELECT 
    'MMS_MSG' as table_name,
    COUNT(*) as record_count
FROM MMS_MSG
UNION ALL
SELECT 
    'MMS_LOG_202407' as table_name,
    COUNT(*) as record_count
FROM MMS_LOG_202407;
```

---

## 9. 테스트 가이드

### 9-1. 테스트 시나리오

#### 테스트 1: 정상 INSERT (중복 없음)
```sql
-- 테스트 데이터 INSERT
INSERT INTO MMS_MSG (phone, msg, status, created_date) 
VALUES ('010-1234-5678', '주문이 접수되었습니다.', 1, SYSDATE);

-- 결과 확인: status가 1로 유지되어야 함
SELECT phone, msg, status FROM MMS_MSG WHERE phone = '010-1234-5678';
```

#### 테스트 2: MMS_MSG 테이블 내 중복 INSERT
```sql
-- 첫 번째 INSERT (정상)
INSERT INTO MMS_MSG (phone, msg, status, created_date) 
VALUES ('010-9999-8888', '보류 주문이 처리되었습니다.', 1, SYSDATE);

-- 두 번째 INSERT (중복, status = 3으로 자동 변경되어야 함)
INSERT INTO MMS_MSG (phone, msg, status, created_date) 
VALUES ('010-9999-8888', '보류 주문이 처리되었습니다.', 1, SYSDATE);

-- 결과 확인: 두 번째 레코드의 status가 3이어야 함
SELECT phone, msg, status, created_date 
FROM MMS_MSG 
WHERE phone = '010-9999-8888' 
ORDER BY created_date;
```

#### 테스트 3: MMS_LOG 테이블과 중복되는 INSERT
```sql
-- 먼저 MMS_LOG_202407에 테스트 데이터 INSERT
INSERT INTO MMS_LOG_202407 (phone, msg, status, created_date) 
VALUES ('010-7777-6666', '기존 로그에 있는 메시지', 3, SYSDATE-1);

-- MMS_MSG에 동일한 데이터 INSERT (status = 3으로 자동 변경되어야 함)
INSERT INTO MMS_MSG (phone, msg, status, created_date) 
VALUES ('010-7777-6666', '기존 로그에 있는 메시지', 1, SYSDATE);

-- 결과 확인: MMS_MSG의 status가 3이어야 함
SELECT phone, msg, status FROM MMS_MSG WHERE phone = '010-7777-6666';
```

### 9-2. 테스트 결과 확인 쿼리

```sql
-- 전체 중복 처리 결과 확인
SELECT 
    phone, 
    msg, 
    status, 
    created_date,
    CASE 
        WHEN status = 3 THEN '중복으로 스킵됨'
        ELSE '정상 처리'
    END as process_result
FROM MMS_MSG 
ORDER BY created_date DESC;

-- 중복 처리된 레코드만 확인
SELECT 
    phone, 
    SUBSTR(msg, 1, 30) as msg_preview,
    status, 
    created_date
FROM MMS_MSG 
WHERE status = 3 
ORDER BY created_date DESC;

-- 트리거 실행 통계
SELECT 
    COUNT(*) as total_records,
    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as duplicate_blocked,
    SUM(CASE WHEN status != 3 THEN 1 ELSE 0 END) as normal_processed,
    ROUND(SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) / COUNT(*) * 100, 2) as block_rate_percent
FROM MMS_MSG;
```

### 9-3. 트리거 관리 쿼리

```sql
-- 트리거 상태 확인
SELECT 
    trigger_name,
    status,
    trigger_type,
    triggering_event,
    table_name
FROM user_triggers 
WHERE trigger_name = 'TRG_MMS_DUPLICATE_CHECK';

-- 트리거 비활성화 (필요시)
-- ALTER TRIGGER TRG_MMS_DUPLICATE_CHECK DISABLE;

-- 트리거 활성화
-- ALTER TRIGGER TRG_MMS_DUPLICATE_CHECK ENABLE;

-- 트리거 삭제 (필요시)
-- DROP TRIGGER TRG_MMS_DUPLICATE_CHECK;
```

---

## 10. 운영 가이드

### 10-1. 일상 모니터링

#### 일별 중복 방지 효과 확인
```sql
-- 오늘 처리된 MMS 현황
SELECT 
    TO_CHAR(created_date, 'YYYY-MM-DD HH24') as hour_block,
    COUNT(*) as total_count,
    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as blocked_count,
    SUM(CASE WHEN status != 3 THEN 1 ELSE 0 END) as sent_count
FROM MMS_MSG 
WHERE created_date >= TRUNC(SYSDATE)
GROUP BY TO_CHAR(created_date, 'YYYY-MM-DD HH24')
ORDER BY hour_block;
```

#### 주별 트렌드 분석
```sql
-- 주별 중복 차단율 추이
SELECT 
    TO_CHAR(created_date, 'YYYY-WW') as week_num,
    COUNT(*) as total_records,
    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as blocked_records,
    ROUND(SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) / COUNT(*) * 100, 2) as block_rate
FROM MMS_MSG 
WHERE created_date >= SYSDATE - 30
GROUP BY TO_CHAR(created_date, 'YYYY-WW')
ORDER BY week_num;
```

### 10-2. 정기 점검 항목

#### 월별 점검 (매월 1일 실행)
1. **새 월의 MMS_LOG 테이블 인덱스 생성**
2. **이전 월 성능 통계 리포트 생성**
3. **트리거 성능 영향 분석**

#### 분기별 점검
1. **전체 시스템 성능 리뷰**
2. **중복 방지 정책 효과성 평가**
3. **SAP 근본 원인 해결 진행 상황 점검**

### 10-3. 장애 대응 절차

#### 트리거 오류 발생 시
1. **즉시 대응**: 트리거 비활성화로 서비스 정상화
2. **원인 분석**: 오류 로그 분석 및 근본 원인 파악
3. **수정 적용**: 트리거 코드 수정 후 재활성화
4. **사후 검증**: 정상 동작 확인 후 모니터링 강화

#### 성능 저하 발생 시
1. **임시 조치**: 트리거 비활성화 검토
2. **원인 분석**: 인덱스 상태, 테이블 통계 정보 확인
3. **최적화**: 인덱스 재생성, 통계 정보 업데이트
4. **성능 검증**: 개선 효과 측정 및 지속 모니터링

---

## 11. 결론 및 향후 계획

### 구현 완료 시 달성 목표
- **중복 전송 0%**: phone + msg 조합 중복 전송 완전 차단
- **고객 만족도 향상**: 중복 문자로 인한 고객 불만 해소
- **운영 효율성**: 수동 모니터링 작업 90% 감소
- **시스템 안정성**: 24시간 무중단 자동 중복 방지 서비스

### 단계별 구현 계획
1. **1단계 (1주차)**: 개발/테스트 환경에서 트리거 구현 및 검증
2. **2단계 (2주차)**: 운영 환경 적용 및 모니터링 체계 구축
3. **3단계 (3-4주차)**: 성능 최적화 및 안정화 작업
4. **4단계 (지속)**: 정기 모니터링 및 개선사항 반영

### 장기 로드맵
- **단기 (1-3개월)**: 트리거 기반 중복 방지 시스템 안정화
- **중기 (3-6개월)**: SAP 근본 원인 해결 및 트리거 정책 재검토
- **장기 (6개월 이상)**: 전체 MMS 시스템 아키텍처 개선 검토

이 가이드를 통해 MMS 중복 전송 문제를 체계적으로 해결하고, 안정적인 고객 서비스를 제공할 수 있을 것입니다.