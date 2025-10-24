<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" class="logo" width="120"/>

### MMS 중복 전송 방지 트리거 설계안

아래는 오더접수/보류오더 MMS 중복 전송 차단을 위한 DB 트리거 설계 예시입니다. **MSSQL(또는 지원되는 DBMS 기준) 기준**으로 제시하였으며, 실제 환경의 테이블명 및 구조에 맞게 수정이 필요합니다.

#### 1. 트리거 설계 개요

- **대상 테이블**: MMS_MSG (신규 데이터 INSERT 시 동작)
- **주요 로직**
    - MMS_MSG 내부 중복 검사: 동일한 `phone`, `msg` 존재 여부
    - 최근 MMS_LOG_YYYYMM 테이블들(1~2개월): 동일한 `phone`, `msg` 존재 여부
    - 두 조건 중 하나라도 중복 시 status=3(전송완료)로 INSERT


#### 2. 테이블 구조 예시

| 컬럼 | 설명 |
| :-- | :-- |
| msg_id | 메시지 일련번호 |
| phone | 수신자 번호 |
| msg | 메시지 본문 |
| status | 처리 상태(1~3) |
| ... | 기타 필요 컬럼 |

#### 3. 트리거 구현 예시 (MSSQL)

```sql
CREATE TRIGGER TRG_MMS_MSG_DUP_CHECK
ON MMS_MSG
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE M
    SET status = 3
    FROM MMS_MSG M
    INNER JOIN inserted I ON M.msg_id = I.msg_id
    WHERE
        EXISTS (
            SELECT 1
            FROM MMS_MSG
            WHERE phone = I.phone AND msg = I.msg AND msg_id <> I.msg_id
        )
        OR
        (
            EXISTS (
                SELECT 1 
                FROM MMS_LOG_202506 L
                WHERE L.phone = I.phone AND L.msg = I.msg
            )
            OR
            EXISTS (
                SELECT 1 
                FROM MMS_LOG_202507 L
                WHERE L.phone = I.phone AND L.msg = I.msg
            )
        );
END
```

**주요 설명:**

- `inserted` 테이블은 trigger 문맥에서 새로 입력된 레코드를 참조합니다.
- MMS_MSG duplicate: 같은 번호, 같은 메시지의 과거 row 확인
- MMS_LOG_YYYYMM: 최근 2개월(예시: 202506, 202507) 테이블에서 중복 탐색
- 중복이 존재하면 status를 3으로 업데이트(전송 완료 처리)


#### 4. 디자인 참고 사항

- **MMS_LOG_YYYYMM 테이블 참조**: 매월 테이블이 바뀌는 환경에선, 트리거 내에서 최근 1~2개월 테이블을 동적으로 정의해야 함(동적 SQL 또는 정기 수정).
- **성능**: 트리거가 자주 실행되고, 대상 테이블이 클 경우 인덱스 최적화 및 검증 필요.
- **테스트**: 운영 적용 전 충분한 테스트 필요.
- **DBMS별 문법 차이**: 환경이 Oracle/MySQL 등인 경우 해당 DBMS에 맞는 트리거 구문 별도 설계 필요.


#### 5. 최종 업무 흐름 요약

1. **MMS_MSG 테이블 Insert 발생**
2. 트리거에서 중복 여부 즉시 검사
    - MMS_MSG(자신 포함), 최근 1~2개월 MMS_LOG_YYYYMM
3. 중복 발견 시 status=3(전송완료)로 처리
4. 실제 전송은 차단됨

필요시 환경에 맞춘 트리거 구체화 작업 및 테스트 쿼리도 추가로 안내드릴 수 있습니다.

