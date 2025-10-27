<# =====================================================================
  backup_editors.ps1
  Windows 10/11 PowerShell 7+ (pwsh) 권장
  목적:
    - VS Code & Notepad++ 주요 설정/스니펫/플러그인 설정/매크로/단축키 백업
    - 대상 루트: C:\util\BurgerKing\setting_files\<timestamp>\
  옵션:
    -Zip     : 결과 폴더를 zip으로도 패키징
    -DryRun  : 실제 복사 대신 수행 예정 작업만 로그로 출력
  입력: (없음)
  출력:
    - 백업 폴더 + backup.log + system-info.txt + (선택) zip
===================================================================== #>

[CmdletBinding()]
param(
  [switch]$Zip,
  [switch]$DryRun
)

# --------------------- 공통 준비 ---------------------
$ErrorActionPreference = 'Stop'
function New-SafeDir([string]$Path) {
  if (-not (Test-Path -LiteralPath $Path)) {
    if ($DryRun) { Write-Host "DRYRUN mkdir $Path" -ForegroundColor Yellow }
    else { New-Item -ItemType Directory -Path $Path | Out-Null }
  }
}

function Copy-IfExists {
  param(
    [Parameter(Mandatory)][string]$Source,
    [Parameter(Mandatory)][string]$Dest,
    [switch]$Recurse,
    [string[]]$Include,
    [string[]]$Exclude
  )
  if (Test-Path -LiteralPath $Source) {
    New-SafeDir $Dest
    $params = @{
      Path        = $Source
      Destination = $Dest
      Force       = $true
      ErrorAction = 'Stop'
    }
    if ($Recurse) { $params['Recurse'] = $true }
    if ($Include) { $params['Include'] = $Include }
    if ($Exclude) { $params['Exclude'] = $Exclude }

    if ($DryRun) { Write-Host "DRYRUN copy $Source -> $Dest (Recurse=$Recurse)" -ForegroundColor Yellow }
    else { Copy-Item @params }
    Write-Log "OK    Copied: $Source -> $Dest"
  }
  else {
    Write-Log "MISS  Not Found: $Source"
  }
}

# 로그
$RootOut = 'C:\util\BurgerKing\setting_files'
$stamp   = Get-Date -Format 'yyyy-MM-dd_HH-mm-ss'
$OutDir  = Join-Path $RootOut $stamp
New-SafeDir $RootOut
New-SafeDir $OutDir

$LogFile = Join-Path $OutDir 'backup.log'
function Write-Log([string]$msg) {
  $line = ('[{0}] {1}' -f (Get-Date -Format 'HH:mm:ss'), $msg)
  Write-Host $line
  if (-not $DryRun) { Add-Content -Path $LogFile -Value $line }
}

Write-Log "Backup started → $OutDir"
Write-Log "Options: Zip=$($Zip.IsPresent) DryRun=$($DryRun.IsPresent)"

# 시스템 정보
$sysInfo = @()
$sysInfo += "UserName  : $env:USERNAME"
$sysInfo += "Computer  : $env:COMPUTERNAME"
$sysInfo += "OS        : $([System.Environment]::OSVersion.VersionString)"
$sysInfo += "PSVersion : $($PSVersionTable.PSVersion)"
$sysFile  = Join-Path $OutDir 'system-info.txt'
if (-not $DryRun) { $sysInfo | Set-Content -Path $sysFile -Encoding UTF8 }
Write-Log "Wrote system-info.txt"

# --------------------- VS Code ---------------------
Write-Log "=== VS Code ==="
$VSBaseAppData   = Join-Path $env:APPDATA 'Code'
$VSUserDir       = Join-Path $VSBaseAppData 'User'
$VSOut           = Join-Path $OutDir 'vscode'
New-SafeDir $VSOut

# 1) 핵심 파일
Copy-IfExists -Source (Join-Path $VSUserDir 'settings.json')   -Dest $VSOut
Copy-IfExists -Source (Join-Path $VSUserDir 'keybindings.json')-Dest $VSOut

# 2) snippets 폴더
Copy-IfExists -Source (Join-Path $VSUserDir 'snippets') -Dest (Join-Path $VSOut 'snippets') -Recurse

# 3) Project Manager (alefragnani.project-manager) 프로젝트 목록
#    일반적으로: %APPDATA%\Code\User\globalStorage\alefragnani.project-manager\projects.json
$PMJson = Join-Path $VSUserDir 'globalStorage\alefragnani.project-manager\projects.json'
$PMOut  = Join-Path $VSOut 'project-manager'
Copy-IfExists -Source $PMJson -Dest $PMOut

# 4) (선택) globalStorage 내 관련 설정 몇 가지 통째 백업(작아보관)
$GSDir = Join-Path $VSUserDir 'globalStorage'
if (Test-Path $GSDir) {
  # 많이 커질 수 있어 일부 선별 예시: project-manager 폴더 전체
  Copy-IfExists -Source (Join-Path $GSDir 'alefragnani.project-manager') -Dest (Join-Path $VSOut 'globalStorage\alefragnani.project-manager') -Recurse
} else { Write-Log "MISS  VS Code globalStorage dir"; }

# 5) 설치 확장 목록 추출
$extListPath = Join-Path $VSOut 'extensions-list.txt'

function Get-CodeCLI {
  # 우선 PATH의 code
  $candidates = @('code','code.cmd')
  foreach ($c in $candidates) {
    $p = (Get-Command $c -ErrorAction SilentlyContinue)
    if ($p) { return $p.Path }
  }
  # 기본 설치 경로 추정
  $fallback = Join-Path $env:LOCALAPPDATA 'Programs\Microsoft VS Code\bin\code.cmd'
  if (Test-Path $fallback) { return $fallback }
  return $null
}

$codeCli = Get-CodeCLI
if ($codeCli) {
  if ($DryRun) {
    Write-Host "DRYRUN `$codeCli --list-extensions > $extListPath" -ForegroundColor Yellow
    Write-Log   "INFO  VS Code CLI found: $codeCli (extensions list skipped due to DryRun)"
  } else {
    try {
      & $codeCli --list-extensions | Set-Content -Path $extListPath -Encoding UTF8
      Write-Log "OK    Saved extensions list -> $extListPath"
    } catch {
      Write-Log "WARN  Failed to list extensions via CLI: $($_.Exception.Message)"
    }
  }
} else {
  Write-Log "WARN  VS Code CLI(code) not found. Skipped extensions list."
}

# --------------------- Notepad++ ---------------------
Write-Log "=== Notepad++ ==="
$NPUserDir = Join-Path $env:APPDATA 'Notepad++'
$NPOut     = Join-Path $OutDir 'notepadpp'
New-SafeDir $NPOut

# 1) 핵심 설정 파일들
$npCoreFiles = @('config.xml','shortcuts.xml','stylers.xml','session.xml','contextMenu.xml','functionList.xml')
foreach ($f in $npCoreFiles) {
  Copy-IfExists -Source (Join-Path $NPUserDir $f) -Dest $NPOut
}

# 2) 사용자 정의 스니펫/언어/템플릿 위치(있을 때만)
Copy-IfExists -Source (Join-Path $NPUserDir 'userDefineLangs') -Dest (Join-Path $NPOut 'userDefineLangs') -Recurse
Copy-IfExists -Source (Join-Path $NPUserDir 'plugins\Config') -Dest (Join-Path $NPOut 'plugins\Config') -Recurse

# 3) MultiReplace 관련(플러그인 설정·패턴 파일 추정 경로 스캔)
#    일반적으로 plugins\Config 하위 혹은 이름에 MultiReplace 포함된 ini/xml 파일이 존재
$multiOut = Join-Path $NPOut 'multireplace'
New-SafeDir $multiOut
if (Test-Path $NPUserDir) {
  $multiFiles = Get-ChildItem -Path $NPUserDir -Recurse -ErrorAction SilentlyContinue `
                -Include *MultiReplace*.ini,*MultiReplace*.xml,*MultiReplace*.txt,*MultiReplace* -File
  if ($multiFiles) {
    foreach ($mf in $multiFiles) {
      Copy-IfExists -Source $mf.FullName -Dest $multiOut
    }
  } else {
    Write-Log "MISS  No MultiReplace-related files found under $NPUserDir"
  }
}

# 4) 설치된 플러그인 DLL/폴더(있으면 참고용 백업)
$NPProgDirs = @()
$NPProgDirs += Join-Path ${env:ProgramFiles} 'Notepad++\plugins'
$NPProgDirs += Join-Path ${env:ProgramFiles(x86)} 'Notepad++\plugins'
$NPProgDirs = $NPProgDirs | Where-Object { $_ -and (Test-Path $_) } | Select-Object -Unique
foreach ($pd in $NPProgDirs) {
  Copy-IfExists -Source $pd -Dest (Join-Path $NPOut "installed-plugins\$((Split-Path $pd -Leaf))") -Recurse
}

# --------------------- 마무리 & ZIP ---------------------
Write-Log "Backup completed."
if ($Zip) {
  $zipPath = "$OutDir.zip"
  if ($DryRun) {
    Write-Host "DRYRUN Compress-Archive $OutDir -> $zipPath" -ForegroundColor Yellow
    Write-Log "INFO  Zip skipped due to DryRun"
  } else {
    try {
      if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
      Compress-Archive -Path $OutDir -DestinationPath $zipPath -Force
      Write-Log "OK    Created zip: $zipPath"
    } catch {
      Write-Log "WARN  Zip failed: $($_.Exception.Message)"
    }
  }
}
# ==================================================================== #
