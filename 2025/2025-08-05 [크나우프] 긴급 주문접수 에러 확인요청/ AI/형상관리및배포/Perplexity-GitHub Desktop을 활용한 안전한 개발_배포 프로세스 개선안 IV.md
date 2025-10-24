<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

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


# GitHub Desktop을 활용한 안전한 개발/배포 프로세스 개선안

안녕하세요! 여러분이 겪고 있는 소스 관리와 배포 문제들을 GitHub Desktop만으로도 충분히 해결할 수 있는 실무적인 방법을 단계별로 설명드릴게요.

## 현재 상황 분석과 핵심 문제점

여러분의 환경을 정리하면:

- 개발환경: STS4.5, Zulu-8, Tomcat 9, Maven, MSSQL, MyBatis
- 협업: 2명 개발자, GitHub 경험 부족
- 서버: 같은 서버에 DEV/REAL 톰캣 분리 운영
- 배포: 수동 WAR 빌드 → VDI 접속 → webapps 폴더 복사

![Git-Flow branch strategy diagram showing feature, develop, release, hotfix, and master branches with key workflow events and tagging.](https://pplx-res.cloudinary.com/image/upload/v1754471426/pplx_project_search_images/04bc09b83cb9adffbe0314ffc36b7eecf25a3f2b.jpg)

Git-Flow branch strategy diagram showing feature, develop, release, hotfix, and master branches with key workflow events and tagging.

가장 큰 문제는 **모든 기능이 develop에 혼재되어 운영 배포시 불필요한 기능까지 섞여 들어가는 것**입니다.

![현재 프로세스 문제점과 개선방안 비교표](https://ppl-ai-code-interpreter-files.s3.amazonaws.com/web/direct-files/6a3193b6f2736930a359ad372bb50559/adcfc898-9340-4848-93ca-bf9cc0ade5c5/80bf3997.png)

현재 프로세스 문제점과 개선방안 비교표

## 1단계: 개선된 브랜치 전략 (3브랜치 구조)

### 브랜치 역할 정의

- **feature 브랜치**: 개별 기능 개발 (예: `feature/로그인개선`, `feature/결제모듈`)
- **develop 브랜치**: 개발 완료된 모든 기능 통합 → DEV 톰캣 배포용
- **main 브랜치**: 운영 승인받은 기능만 → REAL 톰캣 배포용

![개선된 GitHub Desktop 기반 브랜치 관리 전략 프로세스](https://ppl-ai-code-interpreter-files.s3.amazonaws.com/web/direct-files/6a3193b6f2736930a359ad372bb50559/d7dfcd8e-5a0c-4aa3-b71c-5d6be1624708/b3a0b2b5.png)

개선된 GitHub Desktop 기반 브랜치 관리 전략 프로세스

### GitHub Desktop에서 브랜치 생성하기

1. **Current Branch** 클릭 → **New Branch** 선택
2. 브랜치명 입력: `feature/회원가입수정` (기능명 명확히)
3. **Create Branch** 클릭하여 생성
4. **Publish Branch**로 원격에 업로드

## 2단계: 일상적인 개발 워크플로우

### 기능 개발 프로세스

1. **feature 브랜치에서 작업**
    - STS4에서 코딩 완료
    - GitHub Desktop에서 변경사항 확인
    - 커밋 메시지 구체적으로 작성: "회원가입 시 이메일 중복체크 추가"
2. **develop으로 통합**
    - develop 브랜치로 전환
    - **Branch** 메뉴 → **Merge into current branch**
    - feature 브랜치 선택하여 병합
3. **충돌 해결 (중요!)**
    - 충돌 발생시 **Open in Visual Studio Code** 클릭
    - `<<<<<<<`, `=======`, `>>>>>>>` 표시 부분 수정
    - 저장 후 GitHub Desktop에서 **Continue merge**

![GitHub Desktop interface showing merge conflicts in a source file with options to resolve and commit changes to the master branch.](https://pplx-res.cloudinary.com/image/upload/v1754471426/pplx_project_search_images/a157646706736d831c5a7bfb5eea68a783b2df21.jpg)

GitHub Desktop interface showing merge conflicts in a source file with options to resolve and commit changes to the master branch.

## 3단계: 테스트 서버 배포 (DEV 톰캣)

### develop 브랜치 기준 배포

1. develop 브랜치로 체크아웃 확인
2. STS4에서 Maven 빌드:

```
프로젝트 우클릭 → Run As → Maven clean
성공 후 → Run As → Maven install
```

3. `target` 폴더의 WAR 파일을 DEV 톰캣 `webapps`에 복사
4. 클라이언트에게 테스트 요청

## 4단계: 운영 배포를 위한 선택적 반영

### Cherry-pick으로 필요한 기능만 main에 반영

**GitHub Desktop에서 Cherry-pick 방법:**

1. **History** 탭에서 운영 반영할 커밋 선택
2. 우클릭 → **Cherry-pick commit in...**
3. main 브랜치 선택
4. 충돌 발생시 동일하게 해결

[^1]

**중요한 점**: 전체 develop을 main에 병합하지 말고, **승인받은 기능의 커밋만** cherry-pick으로 가져오세요!

## 5단계: 운영 서버 배포 (REAL 톰캣)

1. main 브랜치로 체크아웃
2. Maven clean → install로 WAR 빌드
3. REAL 톰캣 `webapps`에 복사
4. 배포 완료 후 서비스 정상 동작 확인

## 협업 및 실수 방지 전략

### 충돌 예방법

- 작은 단위로 자주 커밋
- **Fetch origin** 자주 클릭하여 최신 상태 유지
- 병합 전 동료와 코드 리뷰


### 롤백 방법

- **History** 탭에서 문제된 커밋 우클릭
- **Revert this commit** 선택
- 이전 상태로 안전하게 되돌리기


### 브랜치 관리 팁

```
현재 브랜치 확인: GitHub Desktop 상단의 Current Branch
브랜치 전환: Current Branch 클릭 후 원하는 브랜치 선택
브랜치 삭제: Branch 메뉴 → Delete 선택 (병합 완료 후)
```


## 실무 체크리스트

## 비효율 요소와 개선 효과

### 현재 방식의 문제

- 수동 소스 복사/삭제로 인한 실수
- 테스트되지 않은 기능의 운영 반영
- 변경 이력 추적 어려움


### 개선 후 효과

- Git 이력으로 모든 변경사항 추적 가능
- 브랜치별 배포로 안정성 확보
- 롤백/복구 작업 간편화


## 점진적 도입 방안

### 1주차: 브랜치 구조 익히기

- feature → develop 병합 연습
- 충돌 해결 연습


### 2주차: Cherry-pick 활용

- main 브랜치에 선택적 반영 연습
- 운영 배포 프로세스 정착


### 3주차: 완전 정착

- 모든 개발/배포를 새로운 방식으로 진행
- 문제점 보완 및 최적화


## 마무리: 성공을 위한 핵심 포인트

1. **절대 develop을 main에 직접 병합하지 마세요** - Cherry-pick으로 필요한 것만!
2. **커밋 메시지를 구체적으로** - 나중에 추적하기 쉽게
3. **작은 단위로 자주 커밋** - 실수해도 부분만 롤백
4. **배포 전 반드시 빌드 테스트** - BUILD SUCCESS 확인
5. **동료와 크로스 체크** - 운영 배포는 2명이 확인

이렇게 하면 자동화 도구 없이도 GitHub Desktop만으로 안전하고 체계적인 소스 관리가 가능합니다. 처음에는 복잡해 보여도 2-3번 반복하면 자연스러워질 거예요!

혹시 특정 단계에서 막히거나 더 자세한 설명이 필요한 부분이 있으면 언제든 질문해 주세요. 실제 작업하면서 생기는 문제들도 하나씩 해결해 나갈 수 있어요! 😊

<div style="text-align: center">⁂</div>

[^1]: https://docs.github.com/ko/desktop/managing-commits/cherry-picking-a-commit-in-github-desktop

[^2]: https://codegear.tistory.com/37

[^3]: https://lee-ding.tistory.com/117

[^4]: https://wlsdn629.tistory.com/entry/Git-Merge할-때-Resolve-Conflicts-버튼이-비활성화-될-때

[^5]: https://docs.github.com/ko/desktop/configuring-and-customizing-github-desktop/configuring-basic-settings-in-github-desktop

[^6]: https://dev-hyonie.tistory.com/31

[^7]: https://hasumang.tistory.com/59

[^8]: https://docs.github.com/ko/desktop/overview/getting-started-with-github-desktop

[^9]: https://ddongyeop.tistory.com/14

[^10]: https://zel0rd.tistory.com/126

[^11]: https://gobae.tistory.com/17

[^12]: https://bloodstrawberry.tistory.com/807

[^13]: https://docs.github.com/ko/enterprise-cloud@latest/pull-requests/collaborating-with-pull-requests/addressing-merge-conflicts/resolving-a-merge-conflict-using-the-command-line

[^14]: https://docs.github.com/ko/desktop

[^15]: https://velog.io/@yeonbikim/GitHub-Desktop에-Branch-생성-Main에-merge하는-법

[^16]: https://night-knight.tistory.com/entry/깃허브-데스크탑-내가-충돌-만들고-처리해보기-🚀5분컷-Vscode

[^17]: https://velog.io/@lazysia/git-github-desktop-사용하기

[^18]: https://docs.github.com/ko/desktop/making-changes-in-a-branch/managing-branches-in-github-desktop

[^19]: https://rootdev.tistory.com/22

[^20]: https://ddongyeop.tistory.com/12

[^21]: https://staticclass.tistory.com/130

[^22]: https://nwblog06.tistory.com/580

[^23]: https://techhans.tistory.com/112

[^24]: https://jd-vlog.tistory.com/8

[^25]: https://peachcong.tistory.com/42

[^26]: https://cokadood.tistory.com/69

[^27]: https://bomz.tistory.com/46

[^28]: https://www.lifencoding.com/web/28

[^29]: https://ggimggim.tistory.com/128

[^30]: https://adjh54.tistory.com/364

[^31]: https://ccambo.tistory.com/entry/TOMCAT-수동으로-배포해-보기

[^32]: https://jaeyoungb.tistory.com/254

[^33]: https://dev-district.tistory.com/23

[^34]: http://trandent.com/article/etc/detail/320818

[^35]: https://root-ca.tistory.com/198

[^36]: https://velog.io/@hxeyexn/Git-Branch-Strategy

[^37]: https://recordsoflife.tistory.com/1484

[^38]: https://ccambo.blogspot.com/2014/10/tomcat.html

[^39]: https://blog.hwahae.co.kr/all/tech/14184

[^40]: https://velog.io/@tjdtn0219/Spring-Boottomcat9-Maven을-이용한-war-배포

[^41]: https://help.sap.com/docs/SAP_BUSINESSOBJECTS_BUSINESS_INTELLIGENCE_PLATFORM/b5c0acee766744a9bbc897356f33b962/46f92ac16e041014910aba7db0e91070.html?locale=ko-KR

[^42]: https://itprogramming119.tistory.com/entry/Git-Cherry-pick-체리픽으로-특정-커밋-선택하여-새-브랜치에-적용하기

[^43]: https://docs.github.com/ko/desktop/working-with-your-remote-repository-on-github-or-github-enterprise/syncing-your-branch-in-github-desktop

[^44]: https://codegym.cc/ko/groups/posts/ko.454.hondong-eobsneun-tim-wokeu-git-ui-bungi-jeonlyag-ihae

[^45]: https://wlsdn629.tistory.com/entry/Git-Cherry-Pick에-대해

[^46]: https://adjh54.tistory.com/665

[^47]: https://news.hada.io/topic?id=18433

[^48]: https://jhklee-coder.tistory.com/45

[^49]: https://my-univers.tistory.com/58

[^50]: https://hayeon1549.tistory.com/13

[^51]: https://zo0oz.tistory.com/211

[^52]: https://bloodstrawberry.tistory.com/983

[^53]: https://best-study-day.tistory.com/6

[^54]: https://study-ce.tistory.com/58

[^55]: https://bloodstrawberry.tistory.com/981

[^56]: https://gracelove91.tistory.com/52

[^57]: https://ppl-ai-code-interpreter-files.s3.amazonaws.com/web/direct-files/6a3193b6f2736930a359ad372bb50559/1eefc403-99b2-4cd2-abb2-d41e5e20b4c3/2e7e110d.csv

[^58]: https://ppl-ai-code-interpreter-files.s3.amazonaws.com/web/direct-files/6a3193b6f2736930a359ad372bb50559/1eefc403-99b2-4cd2-abb2-d41e5e20b4c3/dcd1016c.csv

