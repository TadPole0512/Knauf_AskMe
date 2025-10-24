<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" class="logo" width="120"/>

## 💾 아래의 내용을 검토한 다음 DB 작업 및 프로그램 작업을 나누어 각 작업별 프로세스를 설명해줘.


필수컬럼 => 코드/거래처명/담당자이메일/이메일발송여부/영업 담당 이메일/이메일발송여부
```sql
CUST_CD    VARCHAR(10)
CUST_MAIN_EMAIL    VARCHAR(100)
CUST_SENDMAIL_YN    VARCHAR(1)
SALESREP_EMAIL    VARCHAR(100)
SALESREP_SENDMAIL_YN VARCHAR(1)
INID    VARCHAR(10)
INDATE    DATETIME2
MOID    VARCHAR(10)
MODATE    DATETIME2
CONSTRAINT PR_CUST_CD PRIMARY KEY(CUST_CD)
```

## 💻  E-Order Admin – 거래처관리 – 주문 메일 알람(New)
특정 고객 담당자 및 담당 영업사원에게 D+1(익일착) 주문에 대하여 특정시간에 메일(내용+첨부파일(엑셀))을 전달하는 기능(Daily)

* **O_CUSTOMER의 정보를 그대로 전달 받을 수 있도록 하는 신규 테이블 생성**
	* 담당자 이메일 / 영업 담당 이메일이 수정을 할 수 있어야 함(필요에 따라 O_CUSTOMER와 달라질 수 있음(담당자가 여러명인 거래처 등))
	* ( O_CUSTOMER의 테이블에 신규 정보 입력 시 그대로 가져오도록 필요 => 동기화 시점 고려)
­
* **체크박스를 제공하여 체크 된 담당자/영업담당자에게만 메일이 전송 되도록 처리 필요**
	* 정렬은 체크된 거래처가 우선으로 보이게 함(체크된 거래처 -> 코드순 정렬 / 가장 상위로 변경)
	* 체크 박스가 두개인 이유는 필요에 따라 담당자 혹은 영업담당자만 메일을 받아야 할 수도 있기 때문

* **메일 형식은 제공된 메일 및 엑셀과 동일하게 전송 필요**
	* 제목 및 내용은 형식에 맞춰서 전송 필요

* **메일 발송 시각 설정**
	* 메일 발송 시각은 필요에 따라 변경 될 수 있기에 유저가 능동적으로 입력 할 수 있도록 설정(1시간 단위)**
	* 메일 발송 시각의 관리 방법이 필요

* **메일 발송 로그 기록**
	* 발송 성공 여부 등



---------------------------------------------------------------------

네, 제시된 내용을 검토하고 **DB 작업**과 **프로그램 작업**을 나누어 각 업무별 프로세스를 체계적으로 설명드리겠습니다.

## 1. DB 작업 프로세스

### (A) 신규 테이블 설계 및 생성

#### 1. 메일 발송 대상 관리 테이블 생성

* O_CUSTOMER에서 주요 정보를 **동기화**하여 생성 (별도 테이블)
* 담당자/영업담당자 이메일 각각, 이메일 발송 여부 체크 컬럼 포함
* 예시)

```sql
CREATE TABLE CUSTOMER_MAIL_ALERT (
    CUST_CD VARCHAR(10) PRIMARY KEY,
    CUST_NM VARCHAR(100),
    CUST_MAIN_EMAIL VARCHAR(100),
    CUST_SENDMAIL_YN VARCHAR(1),         -- 담당자 메일 발송여부 (Y/N)
    SALESREP_EMAIL VARCHAR(100),
    SALESREP_SENDMAIL_YN VARCHAR(1),     -- 영업담당 메일 발송여부 (Y/N)
    LAST_SYNC_DT DATETIME,               -- 동기화 일시
    REG_DT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPD_DT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```


#### 2. 메일 발송 시간 관리 테이블

* 관리자가 설정한 특정 발송 시각 저장

```sql
CREATE TABLE MAIL_ALERT_SETTING (
    SETTING_ID INT AUTO_INCREMENT PRIMARY KEY,
    ALERT_HOUR INT,       -- 발송시각 (24시간제, ex: 8, 10, 13...)
    REG_DT DATETIME DEFAULT CURRENT_TIMESTAMP
);
```


#### 3. 메일 발송 로그 테이블

* 발송 결과 및 상세 기록 (성공/실패 구분, 시간, 오류메시지 등)

```sql
CREATE TABLE MAIL_ALERT_LOG (
    LOG_ID INT AUTO_INCREMENT PRIMARY KEY,
    CUST_CD VARCHAR(10),
    EMAIL VARCHAR(100),
    SEND_TYPE VARCHAR(20),           -- ex) '담당자', '영업'
    SEND_RESULT VARCHAR(10),         -- ex) 'SUCCESS', 'FAIL'
    ERR_MSG VARCHAR(500),            -- 실패시 오류메시지
    SEND_DT DATETIME
);
```


#### 4. 동기화 프로세스 (O_CUSTOMER → CUSTOMER_MAIL_ALERT)

* 신규 고객 생성/수정시 트리거 또는 별도 배치로 정보 복제/업데이트
* 필요시 담당자정보, 영업담당자 정보만 동기화 (메일/이름 등)


## 2. 프로그램 작업 프로세스

### (B) 사용자 UI/UX 및 관리 기능 개발

#### 1. 알람 메일 대상 및 체크박스 관리 화면

* E-Order Admin → 거래처관리 → 주문메일알람 화면 구현
* 거래처 리스트 출력 (CUSTOMER_MAIL_ALERT 기준)
* 담당자/영업담당자 각각에 **체크박스** 제공 (메일 발송여부 수정 가능)
* O_CUSTOMER의 필수정보를 처음에는 불러오고, 필요한 경우 개별 수정 가능
    - 이메일 주소 수정 UI 제공
* 정렬:
    - ① 체크된(발신동의 Y) 거래처를 상단에
    - ② 코드순 또는 이름순 정렬


#### 2. 메일 발송 시간 설정 기능

* 관리자가 원하는 시간대(1시간 단위)로 설정 및 저장가능 (MAIL_ALERT_SETTING)
* UI로 설정값 입력/수정
* 배치 서비스/스케줄러가 해당 시간에 데이터를 참조


#### 3. 이메일 발송 배치/스케줄러 개발

* 설정된 발송 시각에 맞춰 자동 실행되는 프로그램
    - D+1(익일착) 주문 추출
* 거래처별 체크된 이메일(담당자/영업담당)만 추출
* 메일 제목, 본문, 첨부(엑셀)파일 생성 및 전송
* 전송 케이스
    - 담당자만 선택 → 담당자에게만 발송
    - 영업담당만 선택 → 영업담당에게만 발송
    - 둘다 체크 → 모두에게 발송


#### 4. 메일 발송 로그 기록

* 메일 보낼 때마다 MAIL_ALERT_LOG에 결과 기록 (성공/실패/오류 등)


#### 5. 기타

* O_CUSTOMER 동기화 로직 개발(신규 거래처 추가시 CUSTOMER_MAIL_ALERT에도 자동 반영)
* 담당자/영업담당자 정보 여러명 지원 필요시 별도 구조 설계 (단일 이메일로 제한시 위 테이블로 충분)


## 3. 전체 흐름 (요약)

1. **DB에서**
    - 거래처 및 알람 메일 대상 테이블 신설 및 동기화
    - 메일 발송 시간/로그 테이블 관리
2. **프로그램에서**
    - 거래처의 담당자/영업담당자 이메일 및 발송여부 체크/수정 UI 제공(체크박스)
    - 발송대상 정렬, 메일발송시간 설정
    - 스케줄러세팅 및 발송 프로그램
    - 메일발송 후 결과로그 저장

필요에 따라 데이터 모델 및 UI 세부 사항을 조정하면 됩니다!
자세한 플로우도나 ERD, 배치예시 소스 등 필요시 요청해 주세요.

