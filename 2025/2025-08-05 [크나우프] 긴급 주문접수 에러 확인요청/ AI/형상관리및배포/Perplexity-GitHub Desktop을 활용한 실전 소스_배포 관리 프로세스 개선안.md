<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# GitHub Desktop을 활용한 실전 소스/배포 관리 프로세스 개선안

## 💡 핵심 문제점 및 해결책 요약

**현재 가장 큰 문제**: 테스트 서버에 모든 개발 기능이 섞여서 운영에 반영되지 않을 소스까지 포함되어 서비스 오작동 발생

**해결 방향**: 브랜치 전략 개선 + 릴리즈 브랜치 도입을 통한 운영 배포용 소스 분리 관리

## 📋 개선된 브랜치 전략 및 워크플로우

### 1. 기본 브랜치 구조 (간소화된 Git Flow)

```
main (운영 배포용)
├── release (운영 반영 준비)
├── develop (개발 통합)
└── feature/* (개별 기능 개발)
```

**각 브랜치 역할**[^1][^2][^3]:

- **main**: 운영 서버에 실제 배포되는 안정적인 코드만 포함
- **release**: 운영 반영이 확정된 기능들만 모아서 최종 테스트
- **develop**: 개발 중인 모든 기능들이 통합되는 브랜치 (기존 테스트 서버용)
- **feature/기능명**: 개별 기능 개발용 브랜치

![Git Flow 브랜치 전략의 흐름도]

![Git Flow 브랜치 전략의 흐름도](https://user-gen-media-assets.s3.amazonaws.com/gpt4o_images/1418eb54-7264-48b3-a3a4-c6724779eaf0.png)

Git Flow 브랜치 전략의 흐름도

### 2. GitHub Desktop 기반 브랜치 관리 매뉴얼

![GitHub Desktop의 브랜치 관리 화면]

![GitHub Desktop의 브랜치 관리 화면](https://user-gen-media-assets.s3.amazonaws.com/gpt4o_images/ea80710f-aa92-4466-b31f-eca3bc444e0f.png)

GitHub Desktop의 브랜치 관리 화면

#### 2.1 브랜치 생성하기

**Step 1: 새 기능 브랜치 생성**[^4][^5][^6]

1. GitHub Desktop에서 `Current branch` 클릭
2. `New branch` 버튼 클릭
3. 브랜치명 입력: `feature/기능명` (예: `feature/user-login`)
4. `Create branch` 클릭

**Step 2: 작업 후 커밋**[^7][^8]

1. 파일 변경 후 GitHub Desktop의 `Changes` 탭 확인
2. 변경사항 검토 후 Summary에 커밋 메시지 작성
3. `Commit to feature/기능명` 클릭
4. `Publish branch` 클릭하여 원격에 푸시

#### 2.2 브랜치 병합하기

**develop 브랜치로 병합**[^6][^9]

1. `Current branch`에서 `develop` 선택
2. 상단 메뉴 `Branch` → `Merge into current branch` 클릭
3. 병합할 브랜치 선택 후 `Create a merge commit` 클릭
4. `Push origin` 클릭

### 3. 충돌 해결 방법

**충돌 발생 시 해결 단계**[^10][^11][^12][^13]:

1. **충돌 감지**: GitHub Desktop에서 'Resolve conflicts before merging' 메시지 표시
2. **VS Code에서 해결**: `Open in Visual Studio Code` 클릭
3. **충돌 구분 확인**:

```
<<<<<<< HEAD (현재 브랜치)
현재 브랜치의 코드
=======
병합하려는 브랜치의 코드
>>>>>>> feature/branch-name
```

4. **해결 방법 선택**:
    - `Accept Current Change`: 현재 브랜치 코드 유지
    - `Accept Incoming Change`: 들어오는 브랜치 코드 선택
    - `Accept Both Changes`: 두 코드 모두 유지
5. **마커 제거**: `<<<<<<<`, `=======`, `>>>>>>>` 모두 삭제
6. **저장 후 계속**: GitHub Desktop으로 돌아가서 `Continue merge` 클릭

## 🚀 개선된 배포 프로세스

### 1. 개발 → 테스트 서버 배포

**기존 방식 유지** (develop 브랜치 → 테스트 서버)

- 모든 개발 기능이 develop에 병합되어 테스트 서버에 자동 반영
- 클라이언트가 전체 기능을 테스트할 수 있는 환경 제공


### 2. 테스트 → 운영 서버 배포 (핵심 개선사항)

**새로운 릴리즈 브랜치 활용 방식**[^14][^15][^16]:

#### Step 1: 릴리즈 브랜치 생성

1. GitHub Desktop에서 `develop` 브랜치 선택
2. `New branch` → `release/버전명` (예: `release/v1.2.0`) 생성

#### Step 2: 운영 반영 기능만 선별적으로 병합

```bash
# 선별적 병합 방법 (GitHub Desktop에서는 체리픽 사용)
1. release 브랜치에서 필요한 feature 브랜치만 병합
2. 불필요한 기능은 병합하지 않음
```


#### Step 3: WAR 파일 빌드 및 배포

**STS4에서 WAR 파일 생성**[^17][^18][^19]:

1. 프로젝트 우클릭 → `Run As` → `Maven clean`
2. 프로젝트 우클릭 → `Run As` → `Maven install`
3. `target` 폴더에서 생성된 WAR 파일 확인
4. 파일명을 적절히 변경 후 VDI를 통해 서버에 업로드

#### Step 4: 운영 서버 배포

1. VDI 원격 접속
2. 기존 애플리케이션 중지
3. WAR 파일을 Tomcat webapps 폴더에 업로드
4. 서버 재시작 후 동작 확인

### 3. 배포 전략 비교

![개발, 테스트, 운영 서버 배포 전략 비교]

![개발, 테스트, 운영 서버 배포 전략 비교](https://user-gen-media-assets.s3.amazonaws.com/gpt4o_images/7bf15926-d3be-4fb7-a6e6-9b9b29f16aa0.png)

개발, 테스트, 운영 서버 배포 전략 비교

## 🔧 실무 적용을 위한 단계별 가이드

### Phase 1: 브랜치 전략 도입 (1-2주)

**1주차: 기본 브랜치 설정**

1. 현재 소스를 `main` 브랜치로 정리
2. `develop` 브랜치 생성 (기존 테스트 서버용)
3. 팀원들에게 GitHub Desktop 사용법 교육

**2주차: 새로운 워크플로우 적용**

1. 새 기능은 반드시 `feature/기능명` 브랜치에서 개발
2. 완료된 기능만 `develop`에 병합
3. 운영 반영 시에는 `release` 브랜치 활용

### Phase 2: 배포 프로세스 개선 (2-3주)

**릴리즈 브랜치 도입**[^14][^15]:

1. 운영 반영이 확정된 기능들만 `release` 브랜치에 선별적 병합
2. `release` 브랜치에서 WAR 파일 빌드
3. 최종 테스트 후 `main` 브랜치로 병합 및 운영 배포

### Phase 3: 고도화 (4주차 이후)

**추가 안전장치 도입**:

1. Pull Request 검토 프로세스 도입
2. 브랜치 보호 규칙 설정
3. 배포 체크리스트 작성

## ⚠️ 현재 프로세스의 비효율/위험요소 분석

### 1. 주요 위험요소

**소스 혼입 위험**[^14]:

- 테스트용 기능이 운영에 잘못 반영될 가능성
- 수동 소스 삭제 과정에서의 휴먼 에러
- 브랜치 관리 부재로 인한 코드 추적 어려움

**협업 충돌**[^10][^11]:

- 동시 수정 시 병합 충돌 발생
- 롤백 시점 불명확
- 개발자별 작업 내역 추적 어려움


### 2. 비효율성 요소

**배포 복잡성**:

- 운영 반영 시 매번 수동 소스 선별 작업
- WAR 파일 수동 빌드 및 업로드
- 서버별 소스 불일치 가능성

**관리 부담**:

- 브랜치 전략 부재로 인한 소스 관리 복잡성
- 버전 관리 어려움
- 배포 이력 추적 불가


## 🎯 구체적 개선 효과

### 1. 안전성 향상

**소스 오염 방지**: 릴리즈 브랜치를 통한 운영 배포용 소스 완전 분리[^14][^15]
**롤백 용이성**: 브랜치별 명확한 버전 관리로 빠른 롤백 가능[^1][^3]
**충돌 최소화**: 기능별 브랜치 분리로 개발자 간 충돌 감소[^4][^7]

### 2. 효율성 증대

**배포 시간 단축**: 사전 정의된 릴리즈 브랜치로 배포 프로세스 표준화
**관리 부담 감소**: GitHub Desktop의 직관적 인터페이스로 학습 곡선 최소화[^4][^8]
**추적성 향상**: 모든 변경사항의 명확한 이력 관리

### 3. 협업 개선

**명확한 역할 분담**: 브랜치별 명확한 용도 정의
**코드 리뷰 활성화**: Pull Request 기반 협업 문화 구축 가능
**지식 공유**: 모든 변경사항의 투명한 공유

## 📝 실행 체크리스트

### 즉시 적용 가능한 개선사항

- [ ] GitHub Desktop으로 브랜치 관리 시작
- [ ] feature/기능명 브랜치 규칙 도입
- [ ] develop/release/main 브랜치 구조 설정
- [ ] WAR 파일 빌드 프로세스 표준화


### 단계적 도입 권장사항

- [ ] 팀원 교육 및 가이드라인 수립
- [ ] 릴리즈 브랜치 기반 운영 배포 프로세스 정착
- [ ] 충돌 해결 및 롤백 매뉴얼 작성
- [ ] 배포 자동화 검토 (장기 과제)

이 프로세스를 통해 **운영 서버 오작동 위험을 크게 줄이고**, **개발 효율성을 높일 수 있을 거야**. 특히 릴리즈 브랜치 도입이 가장 중요한 개선점이니까, 이 부분부터 차근차근 적용해보는 것을 추천해!

<div style="text-align: center">⁂</div>

[^1]: https://lee-ding.tistory.com/117

[^2]: https://soo-develop.tistory.com/20

[^3]: https://hasumang.tistory.com/59

[^4]: https://dev-hyonie.tistory.com/31

[^5]: https://infomationtree.tistory.com/entry/GitHub-협업의-모든-것-기능-워크플로우-팁까지-알아보기

[^6]: https://rootdev.tistory.com/22

[^7]: https://bloodstrawberry.tistory.com/807

[^8]: https://playinlion.tistory.com/28

[^9]: https://night-knight.tistory.com/entry/깃허브-데스크탑-내가-충돌-만들고-처리해보기-🚀5분컷-Vscode

[^10]: https://ddongyeop.tistory.com/14

[^11]: https://playinlion.tistory.com/30

[^12]: https://study-ce.tistory.com/58

[^13]: https://docs.github.com/ko/desktop/making-changes-in-a-branch/managing-branches-in-github-desktop

[^14]: https://velog.io/@ksnughnu/팝콘개발기록-협업-워크플로우

[^15]: https://meorimori.tistory.com/90

[^16]: https://staticclass.tistory.com/130

[^17]: https://daadh.tistory.com/36

[^18]: https://docs.github.com/ko/pull-requests/collaborating-with-pull-requests/addressing-merge-conflicts/resolving-a-merge-conflict-on-github

[^19]: https://velog.io/@ty-oh/프로젝트를-위한-Github-Desktop-사용법

[^20]: https://andamiro25.tistory.com/193

[^21]: https://wonsss.github.io/etc/git-branch-strategy/

[^22]: https://velog.io/@kimcno3/프로젝트-1-GitHub-flow를-통한-브랜치-관리

[^23]: https://ecogeo.tistory.com/77

[^24]: https://devocean.sk.com/blog/techBoardDetail.do?ID=165571\&boardType=techBlog

[^25]: https://gist.github.com/ljlm0402/5f69a1e831d8a50bb1b9f4f1fba71f7f

[^26]: https://yongc.tistory.com/28

[^27]: https://adjh54.tistory.com/368

[^28]: https://blog.hwahae.co.kr/all/tech/9507

[^29]: https://velog.io/@dlawjddn/무중단배포-무중단배포-2-롤링배포로-무중단-배포-적용해보기

[^30]: https://inpa.tistory.com/entry/GIT-⚡️-github-flow-git-flow-📈-브랜치-전략

[^31]: https://waspro.tistory.com/670

[^32]: https://loosie.tistory.com/781

[^33]: https://dev-district.tistory.com/23

[^34]: https://gist.github.com/ihoneymon/a28138ee5309c73e94f9

[^35]: https://groups.google.com/g/ksug/c/JBtQJAMuCbE

[^36]: https://blog.hwahae.co.kr/all/tech/14184

[^37]: https://blog.banksalad.com/tech/become-an-organization-that-deploys-1000-times-a-day/

[^38]: https://www.inflearn.com/community/questions/869624/실운영-서버에-배포-관련-질문-드립니다

[^39]: https://jtm0609.tistory.com/179

[^40]: https://velog.io/@eeeasy-code/배포-전략-종류-롤링-블루-그린-카나리

[^41]: https://nwblog06.tistory.com/580

[^42]: https://devpad.tistory.com/127

[^43]: https://velog.io/@zedy_dev/CICD-%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-EC2-%EC%84%9C%EB%B2%84-%EC%88%98%EB%8F%99-%EB%B0%B0%ED%8F%AC-%EB%B0%A9%EB%B2%95

[^44]: https://velog.io/@suhyun_zip/%EB%B9%8C%EB%93%9C-%EC%9B%B9-%EB%B0%B0%ED%8F%AC

[^45]: https://dololak.tistory.com/31

[^46]: https://docs.aws.amazon.com/ko_kr/prescriptive-guidance/latest/patterns/automatically-build-and-deploy-a-java-application-to-amazon-eks-using-a-ci-cd-pipeline.html

[^47]: https://www.lifencoding.com/web/28

[^48]: https://iwbtbitj.tistory.com/8

[^49]: https://cloud.google.com/compute/docs/import/import-existing-image?hl=ko

[^50]: https://ccambo.tistory.com/entry/TOMCAT-수동으로-배포해-보기

[^51]: https://inforyou.tistory.com/2

[^52]: https://learn.microsoft.com/ko-kr/microsoftteams/vdi-2

[^53]: https://heidong.tistory.com/229

[^54]: https://mindols.tistory.com/160

[^55]: https://docs.redhat.com/ko/documentation/red_hat_enterprise_linux/7/html-single/virtualization_deployment_and_administration_guide/index

[^56]: https://maeryo88.tistory.com/201

[^57]: https://nullgoyatten.tistory.com/10

[^58]: https://docs.automationanywhere.com/ko-KR/bundle/enterprise-v2019/page/enterprise-cloud/topics/developer/cloud-package-project-setup.html

[^59]: https://exploreryun.tistory.com/594

[^60]: https://vibeee.tistory.com/105

