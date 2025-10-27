# 🎯 환경 설정 백업 스크립트

요청하신 각종 프로그램의 설정 파일을 자동으로 백업하는 PowerShell 스크립트를 작성하겠습니다.

---

## 📋 백업 대상 분석

### 확인된 프로그램별 설정 경로

| 프로그램 | 주요 설정 위치 | 백업 대상 |
|---------|--------------|----------|
| **VSCode** | `%APPDATA%\Code\User` | settings.json, keybindings.json, snippets/ |
| **Notepad++** | `%APPDATA%\Notepad++` | config.xml, shortcuts.xml, stylers.xml |
| **Total Commander** | `%APPDATA%\GHISLER` | wincmd.ini, wcx_ftp.ini |
| **DBeaver** | `%APPDATA%\DBeaverData\workspace6` | .metadata/, General/ |
| **Chrome** | `%LOCALAPPDATA%\Google\Chrome\User Data\Default` | Bookmarks, Preferences |
| **Edge** | `%LOCALAPPDATA%\Microsoft\Edge\User Data\Default` | Bookmarks, Preferences |

---

## 💻 백업 스크립트 (PowerShell)---

## 🚀 사용 방법

### 1️⃣ 기본 실행
```powershell
# 스크립트 실행 (기본 경로: C:\Tools\backup\set_env)
.\Backup-env_set-Claude.ps1

# 백업 후 폴더 자동 열기
.\Backup-env_set-Claude.ps1 -OpenBackupFolder

# 상세 로그 출력
.\Backup-env_set-Claude.ps1 -Verbose
```

### 2️⃣ 커스텀 경로 지정
```powershell
# 백업 경로 변경
.\Backup-env_set-Claude.ps1 -BackupRootPath "C:\Tools\backup\set_env\settings"
```

### 3️⃣ 작업 스케줄러 등록 (선택)
```powershell
# 매일 오전 9시 자동 백업 설정
$action = New-ScheduledTaskAction -Execute "PowerShell.exe" `
    -Argument "-ExecutionPolicy Bypass -File C:\Tools\backup\set_env\Backup-env_set-Claude.ps1"

$trigger = New-ScheduledTaskTrigger -Daily -At 9am

Register-ScheduledTask -TaskName "환경설정 백업" `
    -Action $action -Trigger $trigger -Description "개발 환경 설정 자동 백업"
```

---

## 📂 백업 결과 구조

```
C:\Tools\backup\set_env\
├── 2025-10-15\
│   ├── _backup_info.json          ← 백업 메타데이터
│   ├── VSCode\
│   │   ├── settings.json
│   │   ├── keybindings.json
│   │   └── snippets\
│   ├── Notepad++\
│   │   ├── config.xml
│   │   └── shortcuts.xml
│   ├── TotalCommander\
│   │   └── wincmd.ini
│   ├── DBeaver\
│   │   ├── settings\
│   │   └── connections\
│   ├── Chrome\
│   │   └── Bookmarks
│   └── Edge\
│       └── Bookmarks
├── 2025-10-15 (1)\                ← 같은 날 재실행 시
└── 2025-10-15 (2)\
```

---

## ⚙️ 커스터마이징 가이드

### 🔧 A. 프로그램 추가/제거

스크립트 상단의 `$script:BackupSources` 섹션 수정:

```powershell
# 예시: Sublime Text 추가
"SublimeText" = @{
    Enabled = $true
    Paths = @(
        @{
            Source = "$env:APPDATA\Sublime Text\Packages\User\Preferences.sublime-settings"
            Destination = "SublimeText\"
        }
    )
}

# 프로그램 비활성화 (삭제하지 않고)
"DBeaver" = @{
    Enabled = $false  # ← 이 줄만 변경
    Paths = @(...)
}
```

### 🔧 B. 추가 설정 파일 백업

```powershell
# VSCode 확장 목록 추가
@{
    Source = "$env:APPDATA\Code\User\extensions"
    Destination = "VSCode\"
    IsDirectory = $true
}

# Git 설정 추가
"Git" = @{
    Enabled = $true
    Paths = @(
        @{
            Source = "$env:USERPROFILE\.gitconfig"
            Destination = "Git\"
        }
    )
}
```

---

## 📊 실행 결과 예시

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔧 환경 설정 백업 스크립트 v1.0
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📂 백업 대상 폴더: C:\Tools\backup\set_env\2025-10-15

━━━ 📦 VSCode 백업 중...
  ✅ settings.json
  ✅ keybindings.json
  ✅ snippets

━━━ 📦 Chrome 백업 중...
  ✅ Bookmarks
  ✅ Preferences

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 백업 완료 통계
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
총 항목:    18
성공:       15
실패:       0
건너뜀:     3

📁 백업 위치: C:\Tools\backup\set_env\2025-10-15

✅ 백업이 완료되었습니다!
```

---

## ⚠️ 주의사항 및 제한사항

| 항목 | 설명 | 해결책 |
|------|------|--------|
| **권한** | 일부 시스템 폴더 접근 불가 | 관리자 권한으로 실행 |
| **실행 정책** | PowerShell 스크립트 차단 | `Set-ExecutionPolicy RemoteSigned` |
| **브라우저 실행 중** | Bookmarks 파일 잠김 가능 | 브라우저 종료 후 백업 |
| **대용량 폴더** | DBeaver workspace 전체는 큼 | 필요한 `.dbeaver` 폴더만 선택 |

---

## 🔍 검증 방법

```powershell
# 1. 백업된 파일 개수 확인
Get-ChildItem "C:\Tools\backup\set_env\2025-10-15" -Recurse -File | Measure-Object

# 2. 메타데이터 확인
Get-Content "C:\Tools\backup\set_env\2025-10-15\_backup_info.json" | ConvertFrom-Json

# 3. 특정 프로그램 설정 비교
fc.exe "$env:APPDATA\Code\User\settings.json" `
       "C:\Tools\backup\set_env\2025-10-15\VSCode\settings.json"
```

---

## 📥 스크립트 다운로드[View your script](computer:///mnt/user-data/outputs/Backup-env_set-Claude.ps1)

---

## ✅ 완료 체크리스트

- [x] **날짜 폴더 생성**: `yyyy-MM-dd` 형식
- [x] **중복 처리**: `(1)`, `(2)` 자동 추가
- [x] **6가지 프로그램 지원**: VSCode, Notepad++, Total Commander, DBeaver, Chrome, Edge
- [x] **안전한 복사**: 오류 처리 및 로깅
- [x] **메타데이터 저장**: JSON 형식 백업 정보
- [x] **통계 리포트**: 성공/실패/건너뜀 개수 표시
- [x] **확장 가능성**: 프로그램 추가/제거 용이

---

💡 **다음 단계 제안**:
1. 스크립트를 `C:\Scripts\` 등에 저장
2. 실행 정책 설정: `Set-ExecutionPolicy RemoteSigned -Scope CurrentUser`
3. 첫 백업 실행 후 결과 확인
4. 필요시 작업 스케줄러 등록 (자동화)

궁금한 점이나 추가 기능이 필요하시면 말씀해 주세요! 🚀