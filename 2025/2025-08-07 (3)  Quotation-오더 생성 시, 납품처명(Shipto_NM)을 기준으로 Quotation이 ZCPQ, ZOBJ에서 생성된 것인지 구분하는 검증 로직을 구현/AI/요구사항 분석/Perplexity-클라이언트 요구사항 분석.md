# 아래 내용의 비지니스 로직을 정리해서 알려줘.

**개발 목적**
오더 생성 시, 납품처명(Shipto_NM)을 기준으로 Quotation이 ZCPQ(OneCRM)에서 생성된 것인지, ZOBJ(기존 시스템)에서 생성된 것인지 구분하여 검증 로직을 구현합니다.
1. **ZOBJ**: 기존 시스템에서 생성된 Quotation.
2. **ZCPQ**: 최근 도입된 OneCRM 시스템에서 생성된 Quotation.
 
 
**요구 사항**
**1. Quotation 검증 로직**
오더 생성 시, 납품처명(Shipto_NM)을 기준으로 Quotation의 출처를 구분하고, 이에 따라 검증 단계를 진행합니다.
* **조건 1: 납품처명에 'KR' 포함 여부 확인**
   * **Shipto_NM에 'KR'이 prefix로 포함된 경우:**
      * 해당 Quotation은 ZCPQ(OneCRM) 에서 생성된 것으로 간주합니다.
      * 품목 검증(2번) 을 진행합니다.
      * 참고: ZCPQ에서 생성된 모든 납품처명은 'KR'로 시작하며, 이는 필수값입니다.
      * **예시:**
         * Shipto_NM = **KR_수서지점 공사현장** → ZCPQ로 간주, 품목 검증 진행.
 
* **Shipto_NM에 'KR'이 포함되지 않은 경우:**
   * 해당 Quotation은 ZOBJ(기존 시스템)에서 생성된 것으로 간주합니다.
   * 품목 검증(2번) 없이 바로 주문 접수를 진행합니다.
   * **예시:**
      * Shipto_NM = **한국목재 인천지점** → ZOBJ로 간주, 품목 검증 생략.
 
 
***예외 처리***
특정 조건에 따라 Quotation 검증 로직에서 예외 처리를 적용합니다.
* **예외 1: ZOBJ Quotation인데 납품처명에 'KR'이 포함된 경우**
   * 예: Shipto_NM = **KR산업** (실제 업체명이 'KR'로 시작). -> *현재는 KR산업 1건만 해당*
   * 이 경우, 해당 Quotation은 **ZOBJ**로 간주하며, **품목 검증을 생략**합니다.
 
* **예외 2: 납품처명에 'KR'이 중복으로 포함된 경우**
   * 예: Shipto_NM = **KR_KR산업**.
   * 이 경우, 해당 Quotation은 **ZCPQ**로 간주하며, **품목 검증을 진행**합니다.
 
** **
~~**2. 품목 유무 검증**~~** **(해당 로직은 최근 개발 완료)
ZCPQ Quotation의 품목 유무를 확인하여 주문 접수 가능 여부를 결정합니다. **검증 방법:**
* **O_Quotation_Verification** 테이블을 사용하여 품목 검증을 진행합니다.
* **품목이 있는 경우:** 주문 접수를 진행합니다.
* **품목이 없는 경우:** 주문 접수가 불가합니다.

---------------------------------------------------------------------

좋아, 요청하신 내용을 명확하게 비즈니스 로직 형태로 정리해줄게요.
아래는 **오더 생성 시 Quotation 출처 구분 및 품목 검증 로직**을 구조화한 정리입니다.

***

## 1. 개발 목적

오더 생성 시 **납품처명(Shipto_NM)** 을 기준으로 Quotation이

- **ZCPQ (OneCRM)** 에서 생성된 것인지
- **ZOBJ (기존 시스템)** 에서 생성된 것인지
를 구분하여, 이에 따라 **품목 검증 여부**를 결정하는 로직 구현.

***

## 2. Quotation 출처 구분 및 검증 로직

### 📌 기본 규칙

| 조건 | 처리 방식 | 품목 검증 |
| :-- | :-- | :-- |
| Shipto_NM **'KR'** 로 시작 (**prefix**) | ZCPQ로 간주 | 품목 유무 검증 진행 |
| Shipto_NM에 'KR' 미포함 | ZOBJ로 간주 | 품목 검증 없음 |

**예시**

- `KR_수서지점 공사현장` → ZCPQ로 간주 → 품목 검증 진행
- `한국목재 인천지점` → ZOBJ로 간주 → 품목 검증 없음

***

### 📌 예외 처리

1. **예외 1** — 실제 업체명이 ‘KR’로 시작하는 경우 (특정 업체 한정)
    - 조건: ZOBJ Quotation인데 납품처명에 ‘KR’ prefix 존재
    - 예: `KR산업` (현재 해당 케이스 1건 존재)
    - 처리: **ZOBJ로 간주 \& 품목 검증 생략**
2. **예외 2** — ‘KR’이 중복 포함된 경우
    - 예: `KR_KR산업`
    - 처리: **ZCPQ로 간주 \& 품목 검증 진행**

***

## 3. 품목 유무 검증 (ZCPQ 전용)

- **검증 대상**: ZCPQ로 판정된 Quotation만
- **검증 방법**: `O_Quotation_Verification` 테이블 사용
    - 품목 있음 → 주문 접수 진행
    - 품목 없음 → 주문 접수 불가

***

## 4. 로직 플로우 (의사 코드)

```pseudo
IF Shipto_NM starts with 'KR' THEN
    IF Shipto_NM = 'KR산업' THEN
        // 예외 1
        System = ZOBJ
        ItemCheck = false
    ELSE
        System = ZCPQ
        ItemCheck = true
    ENDIF
ELSE
    IF Shipto_NM contains 'KR_KR' THEN
        // 예외 2
        System = ZCPQ
        ItemCheck = true
    ELSE
        System = ZOBJ
        ItemCheck = false
    ENDIF
ENDIF

IF ItemCheck = true THEN
    IF EXISTS(SELECT * FROM O_Quotation_Verification WHERE Quotation_ID = :ID)
        Proceed Order
    ELSE
        Reject Order
    ENDIF
ELSE
    Proceed Order
ENDIF
```


***

## 5. 핵심 요약

1. **판별 기준**: Shipto_NM의 ‘KR’ prefix 여부
2. **ZCPQ면 품목 검증 필수**
3. **ZOBJ면 품목 검증 없음**
4. 특수 케이스 → 예외 1, 예외 2 조건 적용

***

원하시면 이 로직을 **DB VIEW + STORED PROCEDURE** 형태로 구현해서,
오더 생성 시 자동 적용되게 할 수도 있습니다.

제가 다음 단계로 SQL 기반의 완전한 구현안을 짜드릴까요?
그렇게 하면 실제 시스템에 바로 반영 가능한 수준이 될 거예요.

---------------------------------------------------------------------

# 'KR\_'로 시작하면 'ZCPQ'로 분류하고, 그외 'KR'로 시작하는 경우는 'ZOBJ'인가?

---------------------------------------------------------------------


맞아요. 정리해보면 이렇게 됩니다.

***

## 📌 'KR' 관련 분류 규칙 정리

1. **`KR_`(언더바 포함)로 시작**
→ **ZCPQ** 로 간주 (OneCRM)
→ **품목 검증** 진행

예:
    - `KR_수서지점 공사현장` → **ZCPQ**, 품목 검증 진행
    - `KR_KR산업` → 예외2에 의해 **ZCPQ**, 품목 검증 진행

***

2. **'KR_'가 아니라 그냥 'KR'로 시작** (언더바 없음, 업체명 자체에 KR 포함)
→ 기본적으로 **ZOBJ** 로 간주 (기존 시스템)
→ 품목 검증 없음

예:
    - `KR산업` → **ZOBJ**, 품목 검증 없음 (**예외1 케이스**)
    - `KR무역` → ZOBJ, 품목 검증 없음 (예외1 확장 가능)

***

📌 **즉**

- `'KR_'` → 무조건 ZCPQ
- `'KR'` (언더바 없음) → 기본 ZOBJ, 예외1에 해당하면 품목 검증 생략

***

원하시면 제가 이 규칙을 **SQL CASE 문**으로 깔끔하게 만들어서 바로 쓸 수 있게 드릴까요?
그렇게 하면 DB에서 바로 ZCPQ/ZOBJ 분류가 가능합니다.

