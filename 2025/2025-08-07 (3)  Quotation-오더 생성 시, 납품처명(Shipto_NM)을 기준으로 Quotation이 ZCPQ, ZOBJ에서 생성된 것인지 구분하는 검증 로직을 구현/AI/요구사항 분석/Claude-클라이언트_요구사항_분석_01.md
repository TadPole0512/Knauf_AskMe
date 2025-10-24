# 아래의 내용을 좀 더 명확하게 하기 위해 질문을 한다면 어떤 것들을 물어보면 좋을까?

개발 목적

오더 생성 시, 납품처명(Shipto_NM)을 기준으로 Quotation이 ZCPQ(OneCRM)에서 생성된 것인지, ZOBJ(기존 시스템)에서 생성된 것인지 구분하여 검증 로직을 구현합니다.

ZOBJ: 기존 시스템에서 생성된 Quotation.
ZCPQ: 최근 도입된 OneCRM 시스템에서 생성된 Quotation.
 

 

요구 사항

1. Quotation 검증 로직

오더 생성 시, 납품처명(Shipto_NM)을 기준으로 Quotation의 출처를 구분하고, 이에 따라 검증 단계를 진행합니다.

조건 1: 납품처명에 'KR' 포함 여부 확인
Shipto_NM에 'KR'이 prefix로 포함된 경우:
해당 Quotation은 ZCPQ(OneCRM) 에서 생성된 것으로 간주합니다.
품목 검증(2번) 을 진행합니다.
참고: ZCPQ에서 생성된 모든 납품처명은 'KR'로 시작하며, 이는 필수값입니다.
예시:
Shipto_NM = KR_수서지점 공사현장 → ZCPQ로 간주, 품목 검증 진행.
 

Shipto_NM에 'KR'이 포함되지 않은 경우:
해당 Quotation은 ZOBJ(기존 시스템)에서 생성된 것으로 간주합니다.
품목 검증(2번) 없이 바로 주문 접수를 진행합니다.
예시:
Shipto_NM = 한국목재 인천지점 → ZOBJ로 간주, 품목 검증 생략.
 

 

*예외 처리*

특정 조건에 따라 Quotation 검증 로직에서 예외 처리를 적용합니다.

예외 1: ZOBJ Quotation인데 납품처명에 'KR'이 포함된 경우
예: Shipto_NM = KR산업 (실제 업체명이 'KR'로 시작). -> 현재는 KR산업 1건만 해당
이 경우, 해당 Quotation은 ZOBJ로 간주하며, 품목 검증을 생략합니다.
 

예외 2: 납품처명에 'KR'이 중복으로 포함된 경우
예: Shipto_NM = KR_KR산업.
이 경우, 해당 Quotation은 ZCPQ로 간주하며, 품목 검증을 진행합니다.
 

 

2. 품목 유무 검증 (해당 로직은 최근 개발 완료)

ZCPQ Quotation의 품목 유무를 확인하여 주문 접수 가능 여부를 결정합니다.
검증 방법:

O_Quotation_Verification 테이블을 사용하여 품목 검증을 진행합니다.
품목이 있는 경우: 주문 접수를 진행합니다.
품목이 없는 경우: 주문 접수가 불가합니다.

---------------------------------------------------------------------


좋은 요구사항이네! 하지만 개발하기 전에 몇 가지 애매한 부분들을 명확히 해야겠어. 이런 질문들을 해보면 좋을 것 같아:

## 🔍 핵심 로직 관련

**1. 'KR' prefix 정확한 정의**
- `KR_수서지점`처럼 정확히 'KR_'로 시작하는 건지?
- 아니면 단순히 'KR'로 시작하면 되는 건지?
- 대소문자 구분하는지? (kr, Kr, KR 모두 동일 처리?)

**2. 예외 처리 로직**
- 'KR산업' 같은 예외 업체는 어떻게 식별할 건지?
- 하드코딩으로 예외 리스트 관리? 아니면 별도 테이블?
- 앞으로 'KR산업' 외에 추가 예외 업체가 생길 가능성?

## 📊 데이터 구조 관련

**3. 테이블 구조 정보**
- Quotation 정보는 어떤 테이블에서 가져오는지?
- Shipto_NM 외에 필요한 컬럼들은?
- O_Quotation_Verification 테이블 구조는?

**4. 데이터 품질**
- Shipto_NM이 NULL이거나 빈값인 경우 어떻게 처리?
- 특수문자나 공백이 섞여있을 때 처리 방법?

## ⚡ 성능 및 운영 관련

**5. 성능 고려사항**
- 동시에 몇 개 정도의 오더가 생성되는지?
- 검증 로직 실행 시간 제약이 있는지?

**6. 에러 처리**
- 품목 검증 실패 시 사용자에게 어떤 메시지 표시?
- 시스템 오류 시 기본 동작은?

## 🧪 테스트 케이스

**7. 엣지 케이스**
```
- "KR_KR산업" (중복 KR + 예외업체)
- "kr_소문자" 
- "KR산업_추가문자"
- "앞글자KR_중간"
- "KR" (정확히 2글자)
```

**8. 비즈니스 규칙 변화**
- ZCPQ 시스템으로 완전 이전 예정인지?
- 향후 ZOBJ 검증도 추가될 가능성?

이 정도 질문들로 요구사항을 더 구체화하면, 견고하고 유지보수 쉬운 로직을 만들 수 있을 거야!

특히 **1번, 2번, 6번**은 꼭 확인해야 할 것 같아. 이게 명확하지 않으면 나중에 버그 터질 가능성이 높거든.

---------------------------------------------------------------------


# 아래 쿼리를 위의 검증 방법을 적용해서 작성해줘
```sql
CREATE TABLE EORDER.dbo.O_SHIPTO (
	SHIPTO_CD varchar(10) COLLATE Korean_Wansung_CI_AS NOT NULL,
	SHIPTO_NM varchar(80) COLLATE Korean_Wansung_CI_AS NULL,
	ADD1 varchar(180) COLLATE Korean_Wansung_CI_AS NULL,
	ADD2 varchar(80) COLLATE Korean_Wansung_CI_AS NULL,
	ADD3 varchar(80) COLLATE Korean_Wansung_CI_AS NULL,
	ADD4 varchar(80) COLLATE Korean_Wansung_CI_AS NULL,
	ZIP_CD varchar(12) COLLATE Korean_Wansung_CI_AS NULL,
	BUILDING_TY varchar(3) COLLATE Korean_Wansung_CI_AS NULL,
	BUILDING_NM varchar(30) COLLATE Korean_Wansung_CI_AS NULL,
	BUSINESS_TY varchar(3) COLLATE Korean_Wansung_CI_AS NULL,
	BUSINESS_NM varchar(50) COLLATE Korean_Wansung_CI_AS NULL,
	INSERT_DT varchar(20) COLLATE Korean_Wansung_CI_AS NULL,
	DUMMY varchar(30) COLLATE Korean_Wansung_CI_AS NULL,
	DIST_CLASS varchar(5) COLLATE Korean_Wansung_CI_AS NULL,
	QUOTE_QT char(10) COLLATE Korean_Wansung_CI_AS NULL,
	ANGDT varchar(8) COLLATE Korean_Wansung_CI_AS DEFAULT NULL NULL,
	BNDDT varchar(8) COLLATE Korean_Wansung_CI_AS DEFAULT NULL NULL,
	CUST_CD varchar(10) COLLATE Korean_Wansung_CI_AS NULL,
	Comment varchar(80) COLLATE Korean_Wansung_CI_AS NULL,
	STOCK_NY varchar(20) COLLATE Korean_Wansung_CI_AS NULL,
	CONSTRAINT XPKO_SHIPTO PRIMARY KEY (SHIPTO_CD)
);
```
```sql
CREATE TABLE EORDER.dbo.O_QUOTATION_VERIFICATION (
	QUOTE_QT char(10) COLLATE Korean_Wansung_CI_AS NOT NULL,
	ITEM_CD varchar(25) COLLATE Korean_Wansung_CI_AS NOT NULL,
	INSERTID varchar(10) COLLATE Korean_Wansung_CI_AS NULL,
	INDATE datetime2(0) NULL,
	UPDATEID varchar(10) COLLATE Korean_Wansung_CI_AS NULL,
	MODATE datetime2(0) NULL,
	CONSTRAINT XPKO_O_QUOTATION_VERIFICATION PRIMARY KEY (QUOTE_QT,ITEM_CD)
);
```
---------------------------------------------------------------------
```sql
SELECT TRIM(A.QUOTE_QT) AS QUOTE_QT
     , TRIM(A.ITEM_CD) AS ITEM_CD
  FROM O_QUOTATION_VERIFICATION A
 WHERE A.QUOTE_QT = '204192721'
   AND A.ITEM_CD IN ( '792119', '786601', '792123' )
;
```
---------------------------------------------------------------------




