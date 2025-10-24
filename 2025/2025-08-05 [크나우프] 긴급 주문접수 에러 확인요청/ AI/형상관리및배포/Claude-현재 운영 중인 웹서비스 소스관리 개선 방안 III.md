아래는 우리 회사의 실제 개발 및 배포 프로세스와 겪고 있는 문제 사례입니다.
---
[현재 프로세스]
- GitHub Desktop 프로그램을 사용해 소스 이력/협업을 관리하고 있습니다.
- 개발은 각자 feature 브랜치에서 진행, develop 브랜치로 병합(merge) 후 테스트 서버에 반영합니다.
- 테스트 서버에는 지금까지 개발된 모든 기능이 반영되어, 클라이언트가 테스트합니다.
- 운영 서버에는 클라이언트가 “운영 반영”을 요청한 기능만 따로 추려서, 필요 없는 소스는 삭제 후 수동으로 배포합니다.
- 자동화 배포, 서버 Git 연동, SSH/토큰 인증 등은 보안 정책상 모두 불가합니다.
- war 파일을 직접 빌드해서, 서버에 원격접속(VDI) 후 수동 업로드 방식만 사용 가능합니다.
[실제 겪은 문제/불편]
- 테스트 서버에 모든 개발 기능이 섞여 올라가, 운영에는 반영되지 않아야 할 소스(기능)까지 섞여 들어가 서비스 오작동 및 민원이 발생했습니다.
- 운영에 일부 기능만 반영하려다 소스 삭제/추리기 과정에서 실수 발생.
- 브랜치 병합(Merge), 롤백(복구), 협업 충돌 관리도 어렵습니다.
[보충설명]
1. 운영/테스트 서버의 구조
   운영 서버와 테스트 서버는 각각 어떤 방식으로 관리되나요? (예: 물리적으로 다른 서버인지, 같은 Tomcat 환경 내에서 포트만 다른지 등)
   => 같은 서버에 DEV 톰캣과 REAL 톰캣으로 따로 톰캣서버가 존재.해당 톰캣 webapp 폴더 밑에 war 파일을 붙여넣고 서비스. 아파치 웹서버와 연동. 각각의 도메인으로 접속.
2. 현재 배포 방식에서의 워크플로
   WAR 파일을 빌드해서 수동 업로드한다고 하셨는데, 이 때 정확히 어떤 디렉토리에 배포하나요?
   => 개발 서버는 DEV 톰캣 폴더 하위의 webapp 폴도, 운영 서버는 REAL 톰캣 폴더 하위 webapp 폴더에 배포
  테스트 서버에는 어떤 기준으로 반영되며, 배포 시점은 누가/언제 결정하나요?
  => 개발자가 개발이 완료된 이후 클라이언트에게 테스트 요청하기 전에 개발 서버에서 war파일을 만들어 복사 붙여넣기함.
3. 현재 브랜치 전략 관련
  "운영에 반영해야 할 기능만 따로 추리는" 작업은 어떤 식으로 하고 있나요? 예: develop에서 작업된 기능 중 일부만 main에 cherry-pick 하는 식인지, 아니면 수동 복붙?
  => 개발서버에서 개발된 소스만 관리. 운영 및 테스트에는 개발자기 직접 수동으로 복붙.
4. 협업 인원
   몇 명이 동시에 개발에 참여하고 있는지, GitHub 사용 경험은 모두 있는지 알고 싶어요.
   => 2명이 참여. GitHub에 대한 경험은 없음.
4. GitHub Desktop 사용 범위
   오직 GitHub Desktop만 사용하고 있는지, CLI나 VSCode Git 플러그인은 아예 사용하지 않는 건가요?
   => 오직 GitHub만 사용. STS4에서 작업한 다음 GitHub Desktop 프로그램을 실행하여 수정 및 추가한 소스를 push함.
---
**이런 상황에서,**
1. **GitHub Desktop 프로그램을 기준으로,**  
   개발/테스트/운영 서버 소스 관리와 war 파일 배포 프로세스를  
   **초보자도 이해할 수 있게, 한글로 단계별로 재정의**해 주세요.
2. 특히,
    - feature/develop/main 브랜치 관리 및 테스트/운영 서버 분리 배포 전략
    - 운영에 일부 기능만 반영 시 소스 혼입/오작동 방지법
    - 협업 중 실수, 충돌, 롤백(복구) 등 문제 해결 전략
    - 자동화/서버 Git/배포툴 없이 GitHub Desktop만으로 안전하게 협업/관리하는 방법
    - (개발환경: STS4.5, Zulu-8, Tomcat 9, Maven, MSSQL, MyBatis, JQGrid 등)
3. 그리고, 현재 프로세스의 **비효율/위험요소**와,  
   **우리 환경에서 실현 가능한 구체적 개선/최적화 절차**도 같이 안내해 주세요.
---
**GitHub Desktop을 활용한 “실전 소스/배포 관리 프로세스 개선안”**을  
보다 자세하고 꼼꼼하게 
**한글로, 단계별로, 예시/팁/가능하면 관련 이미지까지 포함해서** 설명해 주세요.

---------------------------------------------------------------------


# GitHub Desktop 실전 소스/배포 관리 프로세스 완전 개선안

## 🎯 현재 상황 정확한 문제점 분석

### 치명적 위험요소 
1. **소스 관리의 일원화 부재**: 개발서버 소스만 Git 관리, 테스트/운영은 수동 복붙
2. **배포 혼선**: develop 브랜치의 모든 기능이 테스트에 반영되어 운영 반영 시 혼입 위험
3. **추적 불가능**: 운영서버에 어떤 코드가 실제로 배포되었는지 Git으로 추적 불가
4. **롤백 어려움**: 운영 문제 시 어떤 버전으로 돌아가야 할지 불분명
5. **협업 리스크**: 2명이 같은 파일 수정 시 충돌 해결 방법 부족

---

## 📈 완전히 새로운 브랜치 전략 및 배포 프로세스

### 🌿 새로운 브랜치 구조
```
main (운영서버와 1:1 대응)
├── release/2025-02-01 (운영 배포 대기)
├── develop (테스트서버와 1:1 대응)  
│   ├── feature/member-login (개발자A - 로그인 기능)
│   ├── feature/board-crud (개발자B - 게시판 CRUD)
│   ├── feature/excel-export (개발자A - 엑셀 다운로드)
│   └── hotfix/login-bug (긴급 수정)
```

### 🎯 각 브랜치별 역할과 서버 매핑
| 브랜치 | 역할 | 대응 서버 | WAR 파일 위치 |
|--------|------|-----------|---------------|
| `main` | 운영 안정화 코드 | REAL 톰캣 | `/REAL톰캣/webapps/` |
| `develop` | 테스트 통합 코드 | DEV 톰캣 | `/DEV톰캣/webapps/` |
| `feature/*` | 개별 기능 개발 | 로컬 개발만 | 해당 없음 |
| `release/*` | 운영 배포 준비 | 운영 배포 직전 검증 | 검증 후 main 병합 |

---

## 📝 GitHub Desktop 완전 실무 가이드

### 🚀 Step 1: 초기 환경 설정

#### 1-1. GitHub 리포지토리 구조 생성
```
프로젝트명: company-webapp
├── .gitignore (Java/Maven 용)
├── README.md
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   ├── resources/
│   │   └── webapp/
│   └── test/
├── docs/ (배포 매뉴얼 등)
└── scripts/ (빌드 스크립트)
```

#### 1-2. GitHub Desktop 기본 설정
1. **GitHub Desktop 실행**
2. **File → Options → Git** 
   - Name: `홍길동`
   - Email: `hong@company.com`
3. **File → Clone repository**
   - GitHub.com 탭 선택
   - 리포지토리 URL 입력
   - Local path: `C:\workspace\company-webapp`

#### 1-3. 초기 브랜치 생성 (팀장이 1회 수행)
1. GitHub Desktop에서 `main` 브랜치 확인
2. **Current branch** 클릭 → **New branch**
3. `develop` 입력 후 **Create branch**
4. **Publish branch** 클릭하여 원격에 생성

---

### 🔄 Step 2: 일일 개발 워크플로우

#### 2-1. 새로운 기능 개발 시작하기

##### 📌 개발자 A가 "회원 로그인" 기능 개발 시작
1. **GitHub Desktop 실행**
2. **Repository → Fetch origin** (최신 상태 확인)
3. **develop** 브랜치로 전환
   - Current branch → `develop` 선택
   - **Switch to develop** 클릭
4. **Repository → Pull origin** (최신 코드 받기)
5. **새 기능 브랜치 생성**
   - Current branch → **New branch**
   - 브랜치명: `feature/member-login-20250206`
   - **Create branch** 클릭

##### 📝 브랜치 이름 규칙
```
feature/기능명-날짜
예시:
- feature/member-login-20250206
- feature/board-crud-20250207  
- feature/excel-export-20250208
- hotfix/login-error-20250206
```

#### 2-2. STS4에서 개발 작업

##### 개발 진행
1. **STS4에서 프로젝트 열기**
2. **현재 브랜치 확인** (STS4 하단 상태바)
3. **코드 개발 진행**
   - 컨트롤러 작성
   - 서비스 로직 구현
   - MyBatis 매퍼 수정
   - JSP 화면 개발

#### 2-3. GitHub Desktop에서 커밋하기

##### 변경사항 확인 및 커밋
1. **GitHub Desktop으로 돌아가기**
2. **Changes 탭** 확인
3. **변경된 파일들** 왼쪽 패널에서 확인
4. **커밋할 파일 선택** (체크박스)
5. **커밋 메시지 작성**
   - Summary: `회원 로그인 컨트롤러 추가`
   - Description: 
   ```
   - LoginController.java 신규 추가
   - 로그인 폼 검증 로직 구현
   - 세션 처리 기능 추가
   - 로그인 실패 시 에러 메시지 표시
   ```
6. **Commit to feature/member-login-20250206** 클릭

##### 원격 저장소에 백업
1. **Push origin** 버튼 클릭
2. 첫 푸시라면 **Publish branch** 클릭

---

### 🧪 Step 3: 테스트 서버 배포 프로세스

#### 3-1. feature → develop 병합

##### 개발 완료 후 테스트 서버 반영
1. **develop 브랜치로 전환**
2. **Repository → Pull origin** (다른 개발자 작업 내용 받기)
3. **Branch → Merge into current branch**
4. **feature/member-login-20250206** 선택
5. **Merge feature/member-login-20250206 into develop** 클릭

#### 3-2. 테스트용 WAR 빌드 및 배포

##### Maven 빌드 수행
1. **STS4에서 develop 브랜치로 전환 확인**
2. **프로젝트 우클릭 → Run As → Maven build**
3. **Goals 입력**: `clean package -DskipTests=true`
4. **Run** 클릭

##### WAR 파일 배포
1. **target 폴더에서 생성된 WAR 파일 확인**
   - 예: `company-webapp-1.0.war`
2. **WAR 파일을 DEV 톰캣에 배포**
   - 복사: `target/company-webapp-1.0.war`
   - 붙여넣기: `/DEV톰캣/webapps/company-webapp.war`
3. **톰캣 재시작** (필요 시)
4. **테스트 서버 접속 확인**

---

### 🚀 Step 4: 운영 서버 배포 프로세스 (핵심!)

#### 4-1. 클라이언트 승인 기능만 선별

##### 승인된 기능 리스트 확인
```
클라이언트 승인 현황 (예시):
✅ 회원 로그인 기능 (feature/member-login-20250206)
✅ 게시판 목록 조회 (feature/board-list-20250205)  
❌ 엑셀 다운로드 (feature/excel-export-20250208) - 보류
❌ 관리자 권한 관리 (feature/admin-role-20250207) - 다음 배포
```

#### 4-2. 운영 배포용 브랜치 생성

##### release 브랜치 생성 및 선별 병합
1. **main 브랜치로 전환**
2. **Repository → Pull origin**
3. **새 브랜치 생성**
   - 이름: `release/2025-02-06-v1.2`
   - **Create branch** 클릭

##### 승인된 기능만 선별적으로 병합
1. **Branch → Merge into current branch**
2. **feature/member-login-20250206** 병합
3. **Branch → Merge into current branch**  
4. **feature/board-list-20250205** 병합
5. **승인되지 않은 기능은 병합하지 않음!**

#### 4-3. 운영용 WAR 빌드 및 배포

##### 최종 검증 후 main 병합
1. **release 브랜치에서 빌드 테스트**
2. **문제없으면 main으로 전환**
3. **Branch → Merge into current branch**
4. **release/2025-02-06-v1.2** 선택하여 병합

##### 운영용 WAR 생성
1. **main 브랜치 상태에서 Maven 빌드**
2. **생성된 WAR 파일을 운영서버에 배포**
   - 복사: `target/company-webapp-1.0.war`
   - 붙여넣기: `/REAL톰캣/webapps/company-webapp.war`

##### 운영 배포 태그 생성 (중요!)
1. **Repository → Create tag**
2. **태그명**: `v1.2-production-20250206`
3. **Description**: `회원로그인, 게시판목록 기능 운영 배포`
4. **Create tag** 클릭

---

## 🤝 Step 5: 협업 충돌 관리 완전 가이드

### 5-1. 충돌 예방 전략

#### 일일 동기화 루틴
```
매일 오전 9시 (작업 시작 전)
1. GitHub Desktop 실행
2. develop 브랜치로 전환
3. Repository → Fetch origin
4. Repository → Pull origin  
5. 내 feature 브랜치로 전환
6. Branch → Merge into current branch
7. develop 선택하여 병합 (충돌 시 해결)
```

#### 같은 파일 작업 시 조율 방법
```
협업 규칙:
- 같은 Controller 작업 시 미리 공지
- 공통 파일(Config, Util) 수정 시 팀 회의 후 진행
- JSP 파일은 화면별로 분할 작업
- SQL 매퍼는 기능별로 분리
```

### 5-2. 충돌 해결 상세 방법

#### 병합 충돌 발생 시 해결
1. **GitHub Desktop에서 충돌 파일 확인**
   - Conflicted files 섹션에 표시됨
2. **Open in External Editor** 클릭
3. **STS4에서 충돌 파일 열기**

##### 충돌 마커 해결 예시
```java
// 충돌 발생 예시
<<<<<<< HEAD (내 코드)
@RequestMapping("/login")
public String login(@RequestParam String userId, Model model) {
    // 내가 작성한 로그인 로직
    UserDto user = userService.login(userId);
=======
@RequestMapping("/login")  
public String memberLogin(@RequestParam String memberId, Model model) {
    // 동료가 작성한 로그인 로직
    MemberDto member = memberService.authenticate(memberId);
>>>>>>> feature/other-branch

// 해결 후
@RequestMapping("/login")
public String login(@RequestParam String userId, Model model) {
    // 협의 후 통합된 로직
    UserDto user = userService.authenticateUser(userId);
```

4. **충돌 마커 제거 후 저장**
5. **GitHub Desktop으로 돌아가서**
   - **Mark as resolved** 클릭
6. **충돌 해결 커밋 생성**
   - Summary: `Merge 충돌 해결: 로그인 기능 통합`

### 5-3. 롤백 및 복구 전략

#### 특정 커밋으로 되돌리기
1. **History 탭** 클릭
2. **되돌리고 싶은 커밋** 우클릭
3. **Revert changes in this commit** 선택
4. **자동으로 되돌림 커밋 생성됨**

#### 운영 서버 긴급 롤백
1. **이전 배포 태그로 체크아웃**
   - Repository → Switch to tag
   - `v1.1-production-20250130` 선택
2. **해당 버전으로 WAR 다시 빌드**
3. **운영서버에 재배포**

#### 브랜치 완전 초기화 (위험!)
```
⚠️ 주의: 작업 내용이 완전히 사라짐!
1. Branch → Reset current branch
2. 되돌릴 커밋 선택  
3. Hard 옵션 선택
4. Reset branch 클릭
```

---

## 🔧 Step 6: Maven 환경별 빌드 설정 개선

### 6-1. Profile을 활용한 환경 분리

#### pom.xml 설정 추가
```xml
<profiles>
    <!-- 개발 환경 -->
    <profile>
        <id>dev</id>
        <properties>
            <db.url>jdbc:sqlserver://dev-db:1433;databaseName=devdb</db.url>
            <db.username>dev_user</db.username>
            <db.password>dev_password</db.password>
        </properties>
    </profile>
    
    <!-- 운영 환경 -->  
    <profile>
        <id>prod</id>
        <properties>
            <db.url>jdbc:sqlserver://prod-db:1433;databaseName=proddb</db.url>
            <db.username>prod_user</db.username>
            <db.password>prod_password</db.password>
        </properties>
    </profile>
</profiles>
```

### 6-2. 환경별 빌드 명령어

#### 테스트 서버용 빌드
```bash
# STS4에서 Maven build Goals에 입력
clean package -Pdev -DskipTests=true
```

#### 운영 서버용 빌드
```bash
# STS4에서 Maven build Goals에 입력  
clean package -Pprod -DskipTests=true
```

---

## 📊 Step 7: 일일/주간 워크플로우 정착

### 7-1. 개발자별 체크리스트

#### 🌅 오전 작업 시작 시
- [ ] GitHub Desktop 실행
- [ ] Fetch origin으로 최신 상태 확인
- [ ] develop 브랜치에서 Pull origin  
- [ ] feature 브랜치 생성 또는 기존 브랜치로 전환
- [ ] develop 최신 내용을 feature에 병합
- [ ] STS4에서 개발 작업 시작

#### 🌆 오후 작업 완료 시  
- [ ] 작업 내용 GitHub Desktop에서 커밋
- [ ] Push origin으로 백업
- [ ] 완료된 기능이면 develop에 병합
- [ ] 테스트 서버 배포 여부 결정
- [ ] 내일 작업 계획 브랜치 준비

### 7-2. 팀 차원 정기 업무

#### 📅 매주 월요일 (주간 계획)
- [ ] 이번 주 개발 기능 목록 확인
- [ ] 브랜치별 담당자 지정
- [ ] 공통 파일 수정 계획 논의  
- [ ] 클라이언트 테스트 일정 확인

#### 📅 매주 금요일 (주간 정리)
- [ ] 완료된 feature들 develop에 병합
- [ ] 테스트 서버 최종 배포
- [ ] 클라이언트 테스트 요청 발송
- [ ] 다음 주 운영 배포 계획 수립

#### 📅 운영 배포일 (승인 후)
- [ ] 승인된 기능 목록 최종 확인
- [ ] release 브랜치 생성
- [ ] 선별적 기능 병합
- [ ] main 브랜치에 최종 병합
- [ ] 운영용 WAR 빌드 및 배포
- [ ] 배포 태그 생성
- [ ] 배포 완료 보고

---

## ⚡ 긴급상황 대응 매뉴얼

### 🚨 운영 서버 오류 발생 시

#### 즉시 롤백 절차
1. **이전 안정 버전 태그 확인**
2. **해당 태그로 체크아웃**
3. **운영용 WAR 재빌드**  
4. **운영 서버 재배포**
5. **서비스 정상화 확인**

#### 핫픽스 개발 및 배포
1. **main 브랜치에서 hotfix 브랜치 생성**
   - `hotfix/urgent-login-fix-20250206`
2. **긴급 수정 작업**
3. **테스트 후 바로 main에 병합**
4. **운영 배포 및 태그 생성**
5. **develop에도 동일 수정사항 반영**

### 🔥 GitHub Desktop 오류 시

#### 동기화 문제 해결
```
1. Repository → Repository settings
2. Remote 탭에서 URL 확인
3. 문제 시 Remove 후 다시 Add
4. Fetch origin 재시도
```

#### 로컬 리포지토리 손상 시
```
1. 현재 작업내용 별도 백업
2. File → Clone repository  
3. 새로운 경로에 다시 클론
4. 백업한 작업내용 복원
```

---

## 💡 추가 최적화 및 개선 제안

### 🏃‍♂️ 즉시 적용 가능한 개선사항

#### 1. 배포 체크리스트 문서화
```
배포 전 필수 확인사항:
□ 클라이언트 승인 여부 확인
□ 데이터베이스 스크립트 준비  
□ 설정파일 환경별 분리 확인
□ 테스트 서버 검증 완료
□ 롤백 계획 수립
□ 배포 시간 공지
```

#### 2. 브랜치 네이밍 표준화
```
브랜치 이름 규칙:
- feature/기능명-YYYYMMDD
- hotfix/문제명-YYYYMMDD  
- release/YYYY-MM-DD-v버전
- 한글 가능하지만 영문 권장
```

#### 3. 커밋 메시지 컨벤션
```
커밋 메시지 형식:
[타입] 제목 (50자 이내)

본문 (필요 시)
- 변경된 내용 상세
- 변경 이유  
- 주의사항

타입:
- feat: 새로운 기능 추가
- fix: 버그 수정
- docs: 문서 수정
- refactor: 코드 리팩토링
```

### 🚀 단계별 발전 계획

#### Phase 1 (1개월): 브랜치 전략 정착
- [x] 브랜치 구조 재정의
- [x] 팀원 교육 및 실습
- [x] 배포 프로세스 표준화

#### Phase 2 (2-3개월): 자동화 도입 검토
- [ ] Maven 빌드 스크립트 작성
- [ ] 환경별 설정 자동화
- [ ] 배포 스크립트 개발 (보안 정책 내)

#### Phase 3 (3-6개월): 고도화
- [ ] 코드 리뷰 프로세스 도입
- [ ] 테스트 자동화 검토
- [ ] CI/CD 도구 도입 검토

---

## 📋 최종 체크포인트

### ✅ 이 가이드로 해결되는 문제들
- [x] 소스 혼입 방지: 브랜치별 명확한 역할 분리
- [x] 운영 배포 안정화: release 브랜치 통한 선별 배포  
- [x] 협업 충돌 최소화: 일일 동기화 루틴 정착
- [x] 롤백 용이성: 태그 기반 버전 관리
- [x] 추적 가능성: 모든 배포 내역 Git 기록

### ⚠️ 주의해야 할 사항들
- 브랜치 전환 전 반드시 커밋 또는 스태시
- main 브랜치에서 직접 개발 작업 금지
- 승인되지 않은 기능 운영 반영 절대 금지
- 충돌 해결 시 동료와 충분한 소통
- 배포 전 반드시 백업 및 롤백 계획 수립

이제 이 가이드대로 단계별로 적용해보면, 너희가 겪던 소스 혼입이나 배포 실수 문제들이 확실히 줄어들 거야!

----

너희 상황을 좀 더 자세히 파악했어! 특히 "개발서버에서 개발된 소스만 관리하고 운영/테스트는 수동 복붙"이라는 부분이 핵심 문제네. 이거 완전히 개선해보자!완전히 새로운 관점에서 다시 정리해봤어! 특히 너희 상황에서 가장 중요한 포인트들을 강조했지:

**🎯 핵심 개선사항:**

1. **브랜치-서버 1:1 매핑**: 
   - `main` ↔ REAL 톰캣 (운영서버)
   - `develop` ↔ DEV 톰캣 (테스트서버)

2. **소스 혼입 완전 차단**:
   - `release` 브랜치로 승인된 기능만 선별
   - 운영 배포 시 태그로 정확한 버전 추적

3. **수동 복붙 → Git 기반 관리**:
   - 모든 환경의 소스를 Git으로 추적 가능
   - 언제든 정확한 버전으로 롤백 가능

**💡 바로 시작할 수 있는 것들:**

1. **오늘부터**: 새로운 브랜치 구조 적용
2. **이번 주부터**: release 브랜치 활용한 선별 배포
3. **다음 주부터**: Maven Profile로 환경별 빌드 분리

혹시 어떤 부분부터 시작하고 싶어? 아니면 GitHub Desktop에서 구체적인 조작 방법이 궁금한 게 있나?

실제로 따라해보다가 막히는 부분이 있으면 언제든 물어봐! 스크린샷이나 에러 메시지 같은 것도 보여주면 더 정확히 도와줄 수 있어.