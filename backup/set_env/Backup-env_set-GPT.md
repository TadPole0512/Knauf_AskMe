# 🧰 환경설정 백업 올인원 스크립트 (Windows / PowerShell)

아래 **단일 파일 전체 소스**를 `C:\Tools\backup\set_env\Backup-env_set-GPT.ps1`로 저장한 뒤 실행하세요.
실행하면 `C:\Tools\backup\set_env\set_env\YYYY-MM-DD\` 형태로 오늘 날짜 폴더를 만들고(중복 시 `(1)`, `(2)` 자동 부여), VSCode / Notepad++ / Total Commander / DBeaver / Chrome / Edge(및 프로필) 설정과 즐겨찾기(그룹 포함 추적용 `Preferences`)를 복사합니다. 추가적으로 “Comet*” 이름을 가진 앱 데이터 폴더도 자동 탐지 시 백업합니다.

---

## ▶ 실행 방법

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File "C:\Tools\backup\set_env\Backup-env_set-GPT.ps1"
# 또는 Windows PowerShell:
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Tools\backup\set_env\Backup-env_set-GPT.ps1"
```

* 기본 백업 루트: `C:\Tools\backup\set_env\set_env\`
* 완료 후 요약 리포트: `summary.txt`, `summary.json` (백업 폴더 안)

---

## ✅ 포함 항목 (요약)

* **VSCode**: `%AppData%\Code\User\`(settings.json, keybindings.json, snippets 등), 확장 목록 `vscode-extensions.txt`, 확장 폴더 스냅샷
* **Notepad++**: `%AppData%\Notepad++\` 전부, (있으면) 레지스트리 `HKCU\Software\Notepad++` 내보내기
* **Total Commander**: 레지스트리에서 INI 경로 자동 조회(`wincmd.ini`, `wcx_ftp.ini`), `%AppData%\GHISLER\` 폴더
* **DBeaver**: `%AppData%\DBeaverData\`(신버전), `%UserProfile%\.dbeaver\`(구버전 호환)
* **Chrome / Edge**: 모든 프로필의 `Bookmarks`, `Bookmarks.bak`, `Preferences`, `Local State`
* **Comet***: `%AppData%`와 `%LocalAppData%` 하위의 `Comet*` 디렉터리 자동 탐지 백업(있을 경우)

---

## 🧪 예시 입/출력

* **입력**: (없음) – 스크립트가 표준 경로 자동 탐지
* **기대 출력**:

  * `C:\Tools\backup\set_env\set_env\2025-10-15\` (혹은 `2025-10-15 (1)`)
  * 하위에 각 앱별 폴더 및 파일 복사
  * `summary.txt`, `summary.json`
* **오류 케이스 예시**: VSCode 미설치 → 해당 경로 미발견으로 `summary.txt`에 `Missing` 표기(실패 아님, 스킵)

---

## ⚠️ 주의

* 열려있는 프로그램이 있으면 일부 파일 잠김으로 실패할 수 있어요. 가능하면 앱을 닫고 실행하세요.
  (잠김 파일은 `robocopy` 재시도로 최대한 복사하며, 실패는 리포트에 기록됩니다.)
* 이 스크립트는 **읽기 기반 백업**만 수행하며 시스템 변경은 하지 않습니다.

---

## 💻 전체 소스: `Backup-env_set-GPT.ps1`

```powershell
<# 
  Backup-env_set-GPT.ps1
  - Windows 10/11, PowerShell 5.1+ / 7+ 호환
  - 작성 목적: VSCode, Notepad++, Total Commander, DBeaver, Chrome, Edge, Comet* 관련
    환경설정/즐겨찾기/사용자 정의 파일을 날짜 폴더로 백업
#>

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# -----------------------
# 0) 공통 유틸
# -----------------------
function New-StampedFolderWithDupeSuffix {
    param(
        [Parameter(Mandatory)]
        [string]$BaseDir,
        [Parameter(Mandatory)]
        [string]$Stamp # e.g., 2025-10-15
    )
    if (-not (Test-Path -LiteralPath $BaseDir)) {
        New-Item -ItemType Directory -Path $BaseDir | Out-Null
    }

    $target = Join-Path $BaseDir $Stamp
    if (-not (Test-Path -LiteralPath $target)) {
        New-Item -ItemType Directory -Path $target | Out-Null
        return $target
    }

    $i = 1
    while ($true) {
        $candidate = Join-Path $BaseDir ("{0} ({1})" -f $Stamp, $i)
        if (-not (Test-Path -LiteralPath $candidate)) {
            New-Item -ItemType Directory -Path $candidate | Out-Null
            return $candidate
        }
        $i++
    }
}

function Ensure-Dir {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) {
        New-Item -ItemType Directory -Path $Path | Out-Null
    }
}

function SafeCopy {
    <#
      우선 robocopy를 사용(잠김 파일/긴 경로/속성 호환에 유리). 파일/폴더 모두 지원.
      Return: @{ Status = 'OK'|'Missing'|'Error'; Source=...; Dest=...; Note=... }
    #>
    param(
        [Parameter(Mandatory)][string]$Source,
        [Parameter(Mandatory)][string]$Dest
    )
    $result = [ordered]@{
        Status = ''
        Source = $Source
        Dest   = $Dest
        Note   = ''
    }
    try {
        if (-not (Test-Path -LiteralPath $Source)) {
            $result.Status = 'Missing'
            $result.Note   = 'Source not found'
            return $result
        }
        Ensure-Dir (Split-Path -LiteralPath $Dest -Parent)

        $srcIsDir = (Test-Path -LiteralPath $Source -PathType Container)
        if ($srcIsDir) {
            # /MIR는 위험하므로 사용 금지. 복사만 수행.
            $args = @(
                "`"$Source`"", "`"$Dest`"",
                "/E", "/COPY:DAT", "/R:2", "/W:2", "/NFL", "/NDL", "/NP", "/XJ"
            )
            $rc = Start-Process -FilePath robocopy.exe -ArgumentList $args -NoNewWindow -PassThru -Wait
            # robocopy 종료코드 0,1은 성공으로 간주
            if ($rc.ExitCode -le 1) {
                $result.Status = 'OK'
            } else {
                $result.Status = 'Error'
                $result.Note   = "Robocopy exit code: $($rc.ExitCode)"
            }
        } else {
            Copy-Item -LiteralPath $Source -Destination $Dest -Force -ErrorAction Stop
            $result.Status = 'OK'
        }
    }
    catch {
        $result.Status = 'Error'
        $result.Note   = $_.Exception.Message
    }
    return $result
}

function Add-Log {
    param(
        [Parameter(Mandatory)][System.Collections.Generic.List[hashtable]]$List,
        [Parameter(Mandatory)][hashtable]$Item
    )
    [void]$List.Add($Item)
}

# -----------------------
# 1) 백업 루트 & 날짜 폴더
# -----------------------
$now       = Get-Date
$stamp     = $now.ToString('yyyy-MM-dd')
$rootDir   = 'C:\Tools\backup\set_env\set_env'
$backupDir = New-StampedFolderWithDupeSuffix -BaseDir $rootDir -Stamp $stamp

# -----------------------
# 2) 경로 수집
# -----------------------
$envUserProfile = $env:USERPROFILE
$envAppData     = $env:APPDATA         # Roaming
$envLocalApp    = $env:LOCALAPPDATA    # Local

$tasks = New-Object 'System.Collections.Generic.List[hashtable]'

# --- VSCode (Roaming User)
$vscodeUser = Join-Path $envAppData 'Code\User'
Add-Log $tasks @{
    App='VSCode'; What='UserSettings'; Source=$vscodeUser; Dest=(Join-Path $backupDir 'VSCode\User'); Type='Dir'
}

# VSCode Extensions 목록(명령) → 텍스트 저장
$extListFile = Join-Path $backupDir 'VSCode\vscode-extensions.txt'
Ensure-Dir (Split-Path $extListFile -Parent)
try {
    $exts = & code --list-extensions 2>$null
    if ($LASTEXITCODE -eq 0 -and $exts) {
        $exts | Sort-Object | Set-Content -Encoding UTF8 -LiteralPath $extListFile
    } else {
        # code CLI가 없으면 폴더 스캔으로 대체
        $extDir = Join-Path $envUserProfile '.vscode\extensions'
        if (Test-Path $extDir) {
            Get-ChildItem $extDir -Directory -ErrorAction SilentlyContinue |
                Select-Object -ExpandProperty Name |
                Sort-Object | Set-Content -Encoding UTF8 -LiteralPath $extListFile
        } else {
            "No extensions found (no CLI and no .vscode\extensions)" | Set-Content -Encoding UTF8 -LiteralPath $extListFile
        }
    }
} catch {
    "Failed to list extensions: $($_.Exception.Message)" | Set-Content -Encoding UTF8 -LiteralPath $extListFile
}

# VSCode Extensions 폴더(스냅샷)
$extDir = Join-Path $envUserProfile '.vscode\extensions'
Add-Log $tasks @{
    App='VSCode'; What='ExtensionsFolder'; Source=$extDir; Dest=(Join-Path $backupDir 'VSCode\extensions'); Type='Dir'
}

# --- Notepad++
$npDir = Join-Path $envAppData 'Notepad++'
Add-Log $tasks @{
    App='Notepad++'; What='ConfigFolder'; Source=$npDir; Dest=(Join-Path $backupDir 'Notepad++'); Type='Dir'
}
# 레지스트리 내보내기(있으면)
$npRegFile = Join-Path $backupDir 'Notepad++\NotepadPP_HKCU.reg'
try {
    Ensure-Dir (Split-Path $npRegFile -Parent)
    $null = reg.exe query "HKCU\Software\Notepad++" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Start-Process reg.exe -ArgumentList @('export','HKCU\Software\Notepad++',"`"$npRegFile`"","/y") -NoNewWindow -Wait | Out-Null
    }
} catch { }

# --- Total Commander (INI 경로 탐지)
$tcReg = "HKCU:\Software\Ghisler"
$wincmdIni = $null; $ftpIni = $null
try {
    if (Test-Path $tcReg) {
        $wincmdIni = (Get-ItemProperty $tcReg).IniFileName  -as [string]
        $ftpIni    = (Get-ItemProperty $tcReg).FtpsIniName  -as [string]
    }
} catch { }

# 폴더 기본 위치도 함께 백업
$tcAppData = Join-Path $envAppData 'GHISLER'
Add-Log $tasks @{
    App='TotalCommander'; What='AppDataFolder'; Source=$tcAppData; Dest=(Join-Path $backupDir 'TotalCommander\AppData'); Type='Dir'
}
if ($wincmdIni) {
    Add-Log $tasks @{
        App='TotalCommander'; What='wincmd.ini'; Source=$wincmdIni; Dest=(Join-Path $backupDir 'TotalCommander\wincmd.ini'); Type='File'
    }
}
if ($ftpIni) {
    Add-Log $tasks @{
        App='TotalCommander'; What='wcx_ftp.ini'; Source=$ftpIni; Dest=(Join-Path $backupDir 'TotalCommander\wcx_ftp.ini'); Type='File'
    }
}
# 레지스트리 백업
$tcRegFile = Join-Path $backupDir 'TotalCommander\Ghisler_HKCU.reg'
try {
    Ensure-Dir (Split-Path $tcRegFile -Parent)
    $null = reg.exe query "HKCU\Software\Ghisler" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Start-Process reg.exe -ArgumentList @('export','HKCU\Software\Ghisler',"`"$tcRegFile`"","/y") -NoNewWindow -Wait | Out-Null
    }
} catch { }

# --- DBeaver (신/구 버전 호환)
$dbeaverNew = Join-Path $envAppData 'DBeaverData'
$dbeaverOld = Join-Path $envUserProfile '.dbeaver'
Add-Log $tasks @{
    App='DBeaver'; What='DBeaverData(Roaming)'; Source=$dbeaverNew; Dest=(Join-Path $backupDir 'DBeaver\DBeaverData'); Type='Dir'
}
Add-Log $tasks @{
    App='DBeaver'; What='.dbeaver(Legacy)'; Source=$dbeaverOld; Dest=(Join-Path $backupDir 'DBeaver\.dbeaver'); Type='Dir'
}

# --- Chrome (모든 프로필의 핵심 파일)
$chromeUserData = Join-Path $envLocalApp 'Google\Chrome\User Data'
if (Test-Path $chromeUserData) {
    $profiles = Get-ChildItem $chromeUserData -Directory | Where-Object { $_.Name -in @('Default') -or $_.Name -like 'Profile *' }
    foreach ($p in $profiles) {
        $dest = Join-Path $backupDir ("Chrome\{0}" -f $p.Name)
        foreach ($f in @('Bookmarks','Bookmarks.bak','Preferences')) {
            Add-Log $tasks @{
                App='Chrome'; What=$f; Source=(Join-Path $p.FullName $f); Dest=(Join-Path $dest $f); Type='File'
            }
        }
    }
    # Local State
    Add-Log $tasks @{
        App='Chrome'; What='Local State'; Source=(Join-Path $chromeUserData 'Local State'); Dest=(Join-Path $backupDir 'Chrome\Local State'); Type='File'
    }
}

# --- Edge (모든 프로필의 핵심 파일)
$edgeUserData = Join-Path $envLocalApp 'Microsoft\Edge\User Data'
if (Test-Path $edgeUserData) {
    $profiles = Get-ChildItem $edgeUserData -Directory | Where-Object { $_.Name -in @('Default') -or $_.Name -like 'Profile *' }
    foreach ($p in $profiles) {
        $dest = Join-Path $backupDir ("Edge\{0}" -f $p.Name)
        foreach ($f in @('Bookmarks','Bookmarks.bak','Preferences')) {
            Add-Log $tasks @{
                App='Edge'; What=$f; Source=(Join-Path $p.FullName $f); Dest=(Join-Path $dest $f); Type='File'
            }
        }
    }
    # Local State
    Add-Log $tasks @{
        App='Edge'; What='Local State'; Source=(Join-Path $edgeUserData 'Local State'); Dest=(Join-Path $backupDir 'Edge\Local State'); Type='File'
    }
}

# --- Comet* (추정/자동 탐지: Roaming, Local 모두)
foreach ($base in @($envAppData, $envLocalApp)) {
    if (Test-Path $base) {
        Get-ChildItem $base -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like 'Comet*' } |
            ForEach-Object {
                Add-Log $tasks @{
                    App='Comet*'; What=$_.Name; Source=$_.FullName; Dest=(Join-Path $backupDir ("Comet\{0}" -f $_.Name)); Type='Dir'
                }
            }
    }
}

# -----------------------
# 3) 복사 실행
# -----------------------
$report = New-Object 'System.Collections.Generic.List[hashtable]'

foreach ($t in $tasks) {
    $destPath = $t.Dest
    if ($t.Type -eq 'Dir') {
        $res = SafeCopy -Source $t.Source -Dest $destPath
    } else {
        # 개별 파일은 대상이 파일 경로일 수 있으므로 상위 폴더 보장 후 복사
        Ensure-Dir (Split-Path -LiteralPath $destPath -Parent)
        $res = SafeCopy -Source $t.Source -Dest $destPath
    }
    $entry = [ordered]@{
        App   = $t.App
        What  = $t.What
        Type  = $t.Type
        Source= $res.Source
        Dest  = $res.Dest
        Status= $res.Status
        Note  = $res.Note
    }
    [void]$report.Add($entry)
}

# -----------------------
# 4) 요약 리포트 저장
# -----------------------
$summaryTxt  = Join-Path $backupDir 'summary.txt'
$summaryJson = Join-Path $backupDir 'summary.json'

# Text
"Backup Date: $($now.ToString('yyyy-MM-dd HH:mm:ss'))" | Out-File -Encoding UTF8 -FilePath $summaryTxt
"Backup Folder: $backupDir" | Out-File -Encoding UTF8 -FilePath $summaryTxt -Append
"="*60 | Out-File -Encoding UTF8 -FilePath $summaryTxt -Append
($report | Sort-Object App, What | ForEach-Object {
    "{0,-15} | {1,-22} | {2,-5} | {3} -> {4} | {5} {6}" -f $_.App, $_.What, $_.Type, $_.Source, $_.Dest, $_.Status, (if ($_.Note) { "($($_.Note))" } else { "" })
}) | Out-File -Encoding UTF8 -FilePath $summaryTxt -Append

# JSON
$report | ConvertTo-Json -Depth 6 | Out-File -Encoding UTF8 -FilePath $summaryJson

Write-Host "✅ Backup complete: $backupDir"
Write-Host "   - summary.txt / summary.json 생성됨"
```

---

## 🔄 롤백/복원 팁 (요약)

> 이 스크립트는 **백업만** 수행합니다. 복원은 대상 앱을 종료한 뒤, 필요한 파일/폴더를 원위치로 덮어쓰면 됩니다.
> 브라우저(Chrome/Edge)의 `Bookmarks`/`Preferences`를 복원할 때는 **프로필 폴더(예: `Default`, `Profile 1`)** 경로가 맞는지 확인하세요.

---

## 🧯 발생 가능 오류 & 대응

* `robocopy`가 일부 항목을 건너뜀: 파일 잠김/권한 문제 → 앱 종료 후 재실행
* VSCode 확장 목록 추출 실패: `code` CLI 미설치 → 스크립트가 `.vscode\extensions` 폴더 스캔으로 대체
* Total Commander INI 미검출: 휴대/포터블 설치 등으로 경로 상이 → `summary.txt`에서 `Missing` 확인 후 직접 경로 추가 필요

---

원하는 앱이 더 있거나, 특정 경로를 추가/제외하고 싶으면 알려주세요. 스크립트에 **항목 1줄 추가**로 확장할 수 있게 설계해두었습니다.
