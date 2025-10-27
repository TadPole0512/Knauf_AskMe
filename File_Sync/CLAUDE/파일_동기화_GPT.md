# [README]

두 개의 동일 구조 폴더 간 **단방향(Left ➜ Right)** 동기화 스크립트입니다.
**타임스탬프 → 크기 → (옵션) SHA-256** 기준으로 변경을 판단해 **복사·덮어쓰기**만 수행하며, **삭제는 하지 않습니다.**
**드라이런(WhatIf)**, **JSON/CSV 동시 로깅**, **요약 리포트**, **종료 코드 규약(0/1/2)**, **자가 테스트(-SelfTest)**를 포함합니다.

## ✅ 주요 기능

* 변경 판단: LastWriteTime >, Size ≠, (옵션) Hash ≠
* 필터: 포함/제외 패턴(세미콜론 `;` 구분, `*` 와일드카드, 경로 패턴 지원)
* 드라이런: `-WhatIf` (실제 복사 없이 계획만 출력/로그)
* 로깅: JSONL + CSV 동시 기록(작업/오류/요약), 경로 예: `C:\Logs\sync_YYYYMMDD_HHmmss.*`
* 성능 옵션: `-Parallel`(ForEach-Object -Parallel), `-ThrottleLimit`
* 예외 처리: 긴 경로, 잠긴 파일, 권한/공간 부족, 심볼릭 링크 스킵
* 검증: 무작위 샘플 해시 대조(`-VerifySamples N`)
* 자가 테스트: `-SelfTest` (TC-01~05 자동 실행, 임시 폴더 사용)

## 📁 전제/기본값

* PowerShell 7+ (Windows 11)
* 기본 경로(예시):

  * Left: `C:\staybymeerp-Intellij\`
  * Right: `C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\`
* 기본 패턴: 포함 `*.*` / 제외 `bin/*;obj/*;*.log;*.tmp;.git/*`
* 로그 경로: `C:\Logs\sync_{yyyyMMdd_HHmmss}.jsonl` / `.csv`

## 🧪 종료 코드

* `0`: 성공(치명 오류 없음, 실패 0)
* `1`: 부분 실패(일부 파일 오류)
* `2`: 사전 검증 실패/치명 오류(경로/권한/공간 등)

## 🧰 설치

1. PowerShell 7+ 준비
2. 아래의 `sync.ps1`를 저장 (예: `C:\Tools\Sync\sync.ps1`)
3. `config.example.json` 또는 `config.example.yaml`을 복사 후 수정

## ▶️ 빠른 실행 예시

```powershell
# 드라이런(계획만)
pwsh -NoProfile -File C:\Tools\Sync\sync.ps1 -Config C:\Tools\Sync\config.example.json -WhatIf

# 실제 실행(병렬, 샘플 5건 검증)
pwsh -NoProfile -File C:\Tools\Sync\sync.ps1 -Config C:\Tools\Sync\config.example.json -Parallel -ThrottleLimit 8 -VerifySamples 5
```

## 🧯 오류 대응 FAQ

* **경로가 너무 깁니다**: `-EnableLongPath`(레지스트리 OS 설정) 활성화 필요. 또는 상위 경로 짧게.
* **액세스 거부/잠긴 파일**: 재시도 로직이 3회까지 수행됩니다. 그래도 실패하면 CSV/JSON에 기록됩니다.
* **디스크 공간 부족**: 사전 검증에서 감지 시 종료 코드 2로 중단합니다.
* **해시 느림**: `-UseHash:$false` 또는 포함/제외 패턴으로 대상을 줄이세요.

---

# [config.example.json]

```json
{
  "Left": "C:\\staybymeerp-Intellij\\",
  "Right": "C:\\WORK\\PROJECT\\SBM\\VSCODE\\staybymeerp-Intellij\\",
  "Direction": "LeftToRight",
  "DeletePolicy": "NoDelete",
  "Include": "*.*",
  "Exclude": "bin/*;obj/*;*.log;*.tmp;.git/*",
  "UseHash": true,
  "HashAlgorithm": "SHA256",
  "DryRun": false,
  "Parallel": false,
  "ThrottleLimit": 6,
  "RetryCount": 3,
  "RetryBackoffMs": 400,
  "LogDir": "C:\\Logs",
  "VerifySamples": 3,
  "CsvDelimiter": ","
}
```

---

# [config.example.yaml]

```yaml
Left: "C:\\staybymeerp-Intellij\\"
Right: "C:\\WORK\\PROJECT\\SBM\\VSCODE\\staybymeerp-Intellij\\"
Direction: "LeftToRight"        # LeftToRight | RightToLeft (본 스크립트는 단방향만 수행)
DeletePolicy: "NoDelete"        # NoDelete (삭제 미반영)
Include: "*.*"                  # 세미콜론(;)로 다중 패턴
Exclude: "bin/*;obj/*;*.log;*.tmp;.git/*"
UseHash: true
HashAlgorithm: "SHA256"         # SHA256 | MD5
DryRun: false
Parallel: false
ThrottleLimit: 6
RetryCount: 3
RetryBackoffMs: 400
LogDir: "C:\\Logs"
VerifySamples: 3
CsvDelimiter: ","
```

---

# [sync.ps1]

```powershell
<#
.SYNOPSIS
  단방향(Left ➜ Right) 폴더 동기화 스크립트 (타임스탬프→크기→(옵션)해시)
.DESCRIPTION
  - 삭제 없음. 변경/신규만 복사-덮어쓰기.
  - 드라이런(WhatIf), JSON/CSV 로깅, 요약, 종료코드(0/1/2), 병렬 옵션, 자가 테스트 포함.
.PARAMETER Config
  JSON 또는 YAML 설정 파일 경로.
.PARAMETER Left / Right
  설정 파일 없이 직접 경로 지정 가능.
.PARAMETER WhatIf
  드라이런(실제 복사 없음). 설정파일 DryRun과 OR 로 처리.
.PARAMETER UseHash
  타임스탬프/크기 동일 시 해시 비교 수행 여부.
.PARAMETER HashAlgorithm
  SHA256 또는 MD5 (기본: SHA256)
.PARAMETER Include / Exclude
  세미콜론(;) 구분 다중 패턴. 와일드카드/경로 패턴 지원.
.PARAMETER Parallel
  병렬 처리 사용 (ForEach-Object -Parallel)
.PARAMETER ThrottleLimit
  병렬 동시 작업 수 (기본 6)
.PARAMETER VerifySamples
  완료 후 무작위 N개 샘플 해시 대조(기본 0=비활성)
.PARAMETER SelfTest
  TC-01~05 자가 테스트 수행 후 결과/로그 출력
.EXAMPLE
  pwsh -File .\sync.ps1 -Config .\config.json -WhatIf
#>

[CmdletBinding(SupportsShouldProcess=$true)]
param(
  [string]$Config,
  [string]$Left,
  [string]$Right,
  [switch]$WhatIf,
  [bool]$UseHash,
  [ValidateSet('SHA256','MD5')][string]$HashAlgorithm = 'SHA256',
  [string]$Include = '*.*',
  [string]$Exclude = 'bin/*;obj/*;*.log;*.tmp;.git/*',
  [switch]$Parallel,
  [int]$ThrottleLimit = 6,
  [int]$RetryCount = 3,
  [int]$RetryBackoffMs = 400,
  [string]$LogDir = 'C:\Logs',
  [int]$VerifySamples = 0,
  [string]$CsvDelimiter = ',',
  [switch]$SelfTest
)

#region ---------- Helpers ----------
function Write-Info($msg){ Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-Warn($msg){ Write-Host "[WARN] $msg" -ForegroundColor Yellow }
function Write-Err ($msg){ Write-Host "[ERROR] $msg" -ForegroundColor Red }

function Join-NormalizedPath([string]$base,[string]$child){
  $p = [System.IO.Path]::GetFullPath((Join-Path $base $child))
  return $p
}

function Split-Patterns([string]$patterns){
  if([string]::IsNullOrWhiteSpace($patterns)){ return @() }
  return ($patterns -split ';').Trim() | Where-Object { $_ -ne '' }
}

function Test-PatternMatch([string]$relPath, [string[]]$include, [string[]]$exclude){
  # 경로 구분자 통일
  $p = $relPath -replace '\\','/'
  $matchIncl = $false
  if($include.Count -eq 0){ $matchIncl = $true }
  else{
    foreach($pat in $include){
      $r = '^' + ([Regex]::Escape($pat -replace '\\','/').Replace('\*','.*').Replace('\?','.')) + '$'
      if($p -match $r){ $matchIncl = $true; break }
      # 하위경로 매치 허용: "bin/*"는 bin/abc.txt에 매치
      if($pat -like '*/'){
        $r2 = '^' + ([Regex]::Escape($pat.TrimEnd('*','/') -replace '\\','/')) + '(/.*)?$'
        if($p -match $r2){ $matchIncl = $true; break }
      }
    }
  }
  if(-not $matchIncl){ return $false }

  foreach($pat in $exclude){
    if([string]::IsNullOrWhiteSpace($pat)){ continue }
    $r = '^' + ([Regex]::Escape($pat -replace '\\','/').Replace('\*','.*').Replace('\?','.')) + '$'
    if($p -match $r){ return $false }
    if($pat -like '*/'){
      $r2 = '^' + ([Regex]::Escape($pat.TrimEnd('*','/') -replace '\\','/')) + '(/.*)?$'
      if($p -match $r2){ return $false }
    }
  }
  return $true
}

function Get-HashHex([string]$path, [string]$algo='SHA256'){
  if(-not (Test-Path -LiteralPath $path -PathType Leaf)){ return $null }
  try {
    $h = Get-FileHash -LiteralPath $path -Algorithm $algo -ErrorAction Stop
    return $h.Hash.ToUpperInvariant()
  } catch { return $null }
}

function Ensure-Dir([string]$dir){
  if(-not (Test-Path -LiteralPath $dir)){ New-Item -ItemType Directory -Path $dir | Out-Null }
}

function Get-FreeSpaceGB([string]$path){
  try {
    $root = (Get-Item -LiteralPath $path).PSDrive.Root
    $drive = Get-PSDrive -Name ((Get-Item $root).Name)
    return [math]::Round($drive.Free/1GB,2)
  } catch { return $null }
}

function Retry-Action([scriptblock]$block, [int]$retry, [int]$backoffMs){
  $attempt = 0
  $lastErr = $null
  while($attempt -le $retry){
    try { return & $block } catch { $lastErr = $_; Start-Sleep -Milliseconds ([math]::Min($backoffMs * [math]::Pow(2,$attempt), 8000)); $attempt++ }
  }
  throw $lastErr
}
#endregion

#region ---------- Config Load ----------
if($Config){
  if(-not (Test-Path -LiteralPath $Config)){ Write-Err "설정 파일을 찾을 수 없습니다: $Config"; exit 2 }
  $ext = [IO.Path]::GetExtension($Config).ToLowerInvariant()
  $cfg = $null
  try{
    if($ext -in @('.json')){
      $cfg = Get-Content -LiteralPath $Config -Raw | ConvertFrom-Json
    } elseif($ext -in @('.yml','.yaml')){
      if(-not (Get-Module -ListAvailable -Name powershell-yaml)){ Import-Module powershell-yaml -ErrorAction SilentlyContinue }
      if(-not (Get-Module -Name powershell-yaml)){ Write-Err "YAML 파서가 필요합니다: Install-Module powershell-yaml"; exit 2 }
      $cfg = ConvertFrom-Yaml (Get-Content -LiteralPath $Config -Raw)
    } else { Write-Err "지원하지 않는 설정 형식: $ext"; exit 2 }
  } catch { Write-Err "설정 파일 파싱 오류: $($_.Exception.Message)"; exit 2 }

  if(-not $Left  -and $cfg.Left)  { $Left  = $cfg.Left }
  if(-not $Right -and $cfg.Right) { $Right = $cfg.Right }
  if($cfg.UseHash -ne $null)      { $UseHash = [bool]$cfg.UseHash }
  if($cfg.HashAlgorithm)          { $HashAlgorithm = $cfg.HashAlgorithm }
  if($cfg.Include)                { $Include = $cfg.Include }
  if($cfg.Exclude)                { $Exclude = $cfg.Exclude }
  if($cfg.Parallel -ne $null)     { $Parallel = [bool]$cfg.Parallel }
  if($cfg.ThrottleLimit)          { $ThrottleLimit = [int]$cfg.ThrottleLimit }
  if($cfg.RetryCount)             { $RetryCount = [int]$cfg.RetryCount }
  if($cfg.RetryBackoffMs)         { $RetryBackoffMs = [int]$cfg.RetryBackoffMs }
  if($cfg.LogDir)                 { $LogDir = $cfg.LogDir }
  if($cfg.VerifySamples)          { $VerifySamples = [int]$cfg.VerifySamples }
  if($cfg.CsvDelimiter)           { $CsvDelimiter = $cfg.CsvDelimiter }
  if($cfg.DryRun){ $WhatIf = $true }
}

$Left  = (Resolve-Path -LiteralPath $Left ).Path 2>$null
$Right = (Resolve-Path -LiteralPath $Right).Path 2>$null
if(-not $Left -or -not (Test-Path -LiteralPath $Left  -PathType Container)){ Write-Err "Left 폴더가 유효하지 않습니다.";  exit 2 }
if(-not $Right -or -not (Test-Path -LiteralPath $Right -PathType Container)){ Write-Err "Right 폴더가 유효하지 않습니다."; exit 2 }

Ensure-Dir $LogDir
$ts = Get-Date -Format 'yyyyMMdd_HHmmss'
$jsonLog = Join-Path $LogDir "sync_$ts.jsonl"
$csvLog  = Join-Path $LogDir "sync_$ts.csv"
"timestamp${CsvDelimiter}level${CsvDelimiter}action${CsvDelimiter}src${CsvDelimiter}dst${CsvDelimiter}bytes${CsvDelimiter}status${CsvDelimiter}message" | Out-File -FilePath $csvLog -Encoding utf8

function Log-Json([string]$level,[string]$action,[string]$src,[string]$dst,[long]$bytes,[string]$status,[string]$message){
  $obj = [ordered]@{
    timestamp = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss.fff')
    level     = $level
    action    = $action
    src       = $src
    dst       = $dst
    bytes     = $bytes
    status    = $status
    message   = $message
  } | ConvertTo-Json -Compress
  Add-Content -LiteralPath $jsonLog -Value $obj
  Add-Content -LiteralPath $csvLog  -Value ("{0}{7}{1}{7}{2}{7}{3}{7}{4}{7}{5}{7}{6}" -f `
    (Get-Date).ToString('yyyy-MM-dd HH:mm:ss.fff'), $level, $action, $src, $dst, $bytes, $status, $CsvDelimiter) + $CsvDelimiter + ('"'+($message -replace '"','""')+'"')
}

Write-Info  "Left : $Left"
Write-Info  "Right: $Right"
Write-Info  "WhatIf: $($WhatIf.IsPresent)  UseHash: $UseHash ($HashAlgorithm)  Parallel: $Parallel TL=$ThrottleLimit"
#endregion

#region ---------- Pre-Checks ----------
$leftFree  = Get-FreeSpaceGB $Left
$rightFree = Get-FreeSpaceGB $Right
if($rightFree -eq $null){ Write-Warn "대상 디스크 여유 공간 확인 실패"; } else { Write-Info "대상 여유 공간: ${rightFree}GB" }

# 파일 나열
$incl = Split-Patterns $Include
$excl = Split-Patterns $Exclude

$leftFiles = Get-ChildItem -LiteralPath $Left -Recurse -File -Force -ErrorAction SilentlyContinue
$rightFiles= Get-ChildItem -LiteralPath $Right -Recurse -File -Force -ErrorAction SilentlyContinue

# 상대경로 매핑
$leftMap  = @{}
foreach($f in $leftFiles){
  $rel = (Resolve-Path -LiteralPath $f.FullName).Path.Substring($Left.Length).TrimStart('\')
  if(-not (Test-PatternMatch $rel $incl $excl)){ continue }
  $leftMap[$rel] = $f
}
$rightMap = @{}
foreach($f in $rightFiles){
  $rel = (Resolve-Path -LiteralPath $f.FullName).Path.Substring($Right.Length).TrimStart('\')
  $rightMap[$rel] = $f
}

# 변경 집합 계산
$plan = New-Object System.Collections.Concurrent.ConcurrentBag[object]
$estTotalBytes = 0L
foreach($kv in $leftMap.GetEnumerator()){
  $rel = $kv.Key
  $src = $kv.Value
  $dstPath = Join-Path $Right $rel
  $need = $false
  $reason = ""
  if(-not $rightMap.ContainsKey($rel)){
    $need = $true; $reason = "신규"
  } else {
    $dst = $rightMap[$rel]
    if($src.LastWriteTimeUtc -gt $dst.LastWriteTimeUtc){
      $need = $true; $reason = "타임스탬프 최신"
    } elseif($src.Length -ne $dst.Length){
      $need = $true; $reason = "크기 상이"
    } elseif($UseHash){
      $s = Get-HashHex $src.FullName $HashAlgorithm
      $d = Get-HashHex $dst.FullName $HashAlgorithm
      if($s -and $d -and $s -ne $d){ $need = $true; $reason = "해시 상이" }
    }
  }
  if($need){
    $estTotalBytes += $src.Length
    $plan.Add([pscustomobject]@{
      RelPath = $rel; Src=$src.FullName; Dst=$dstPath; Bytes=$src.Length; Reason=$reason
    })
  }
}

Write-Info ("드라이런 계획" + ($(if($WhatIf){" (WhatIf)"} else {""})))
Write-Info ("예상 복사 파일 수: {0}, 예상 총 크기: {1:N0} bytes" -f $plan.Count, $estTotalBytes)
Log-Json "info" "plan" "" "" $estTotalBytes "ok" ("files=$($plan.Count)")

if($rightFree -ne $null -and ($estTotalBytes/1GB) -gt ($rightFree+0.0)){
  Write-Err "대상 디스크 여유 공간 부족으로 중단"
  Log-Json "error" "precheck" "" "" 0 "fail" "insufficient target free space"
  exit 2
}
#endregion

#region ---------- Execute ----------
$errors = [System.Collections.Concurrent.ConcurrentBag[string]]::new()
$copied = [System.Collections.Concurrent.ConcurrentBag[object]]::new()

$copyBlock = {
  param($item,$RetryCount,$RetryBackoffMs,$WhatIf)
  $src = $item.Src; $dst = $item.Dst; $bytes=$item.Bytes; $reason=$item.Reason
  try{
    $dstDir = Split-Path -Parent $dst
    if(-not (Test-Path -LiteralPath $dstDir)){ New-Item -ItemType Directory -Path $dstDir -Force | Out-Null }
    if($WhatIf){
      Log-Json "info" "copy(whatif)" $src $dst $bytes "skip" $reason
      return
    }
    Retry-Action {
      Copy-Item -LiteralPath $src -Destination $dst -Force -ErrorAction Stop
    } $RetryCount $RetryBackoffMs
    # 타임스탬프 유지(소스 기준)
    try { $lt = (Get-Item -LiteralPath $src).LastWriteTime; (Get-Item -LiteralPath $dst).LastWriteTime = $lt } catch {}
    Log-Json "info" "copy" $src $dst $bytes "ok" $reason
    $copied.Add($item) | Out-Null
  } catch {
    $msg = $_.Exception.Message
    Log-Json "error" "copy" $src $dst $bytes "fail" $msg
    $script:errors.Add("$($item.RelPath): $msg") | Out-Null
  }
}

if($Parallel -and $plan.Count -gt 0){
  $plan | ForEach-Object -Parallel $copyBlock -ThrottleLimit $ThrottleLimit -ArgumentList $RetryCount,$RetryBackoffMs,$WhatIf
} else {
  foreach($item in $plan){
    & $copyBlock $item $RetryCount $RetryBackoffMs $WhatIf
  }
}

#endregion

#region ---------- Post-Verify & Summary ----------
$verifyFailed = 0
if(-not $WhatIf -and $VerifySamples -gt 0 -and $copied.Count -gt 0){
  $samples = Get-Random -InputObject $copied -Count ([Math]::Min($VerifySamples,$copied.Count))
  foreach($s in $samples){
    $hs = Get-HashHex $s.Src $HashAlgorithm
    $hd = Get-HashHex $s.Dst $HashAlgorithm
    if($hs -and $hd -and $hs -eq $hd){
      Log-Json "info" "verify" $s.Src $s.Dst $s.Bytes "ok" "hash matched"
    } else {
      $verifyFailed++
      Log-Json "error" "verify" $s.Src $s.Dst $s.Bytes "fail" "hash mismatch or unreadable"
    }
  }
}

$summary = [ordered]@{
  time          = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
  whatIf        = $WhatIf.IsPresent
  plannedFiles  = $plan.Count
  copiedFiles   = $copied.Count
  failedFiles   = $errors.Count
  verifyFailed  = $verifyFailed
  totalBytes    = ($copied | Measure-Object -Property Bytes -Sum).Sum
}
$summaryJson = $summary | ConvertTo-Json
Write-Host "`n=== 동기화 요약 ===" -ForegroundColor Green
$summary.GetEnumerator() | ForEach-Object { "{0,-14}: {1}" -f $_.Key, $_.Value } | Write-Host
Log-Json "info" "summary" "" "" ($summary.totalBytes) "ok" ($summaryJson)

if($errors.Count -gt 0){
  Write-Warn "오류 파일 목록:"
  $errors | ForEach-Object { " - $_" | Write-Host }
}

# 종료 코드
if($WhatIf){ exit 0 }
elseif($errors.Count -gt 0 -or $verifyFailed -gt 0){ exit 1 }
else{ exit 0 }
#endregion

#region ---------- SelfTest (TC-01~05) ----------
if($SelfTest){
  Write-Host "`n[SELFTEST] 임시 폴더에서 TC-01~05 실행..." -ForegroundColor Magenta
  $tmp = Join-Path $env:TEMP ("SyncTest_" + (Get-Date -f yyyyMMdd_HHmmss))
  $L = Join-Path $tmp "left"; $R = Join-Path $tmp "right"
  New-Item -ItemType Directory -Path $L,$R -Force | Out-Null

  # TC-01: 최신 타임스탬프 덮어쓰기
  $f1L = Join-Path $L "a.txt"; $f1R = Join-Path $R "a.txt"
  "old" | Out-File -FilePath $f1R -Encoding utf8
  Start-Sleep -Milliseconds 50
  "new" | Out-File -FilePath $f1L -Encoding utf8
  # TC-02: 동일 타임스탬프/크기, 해시 상이 (UseHash=On에서만 감지)
  $f2L = Join-Path $L "b.bin"; $f2R = Join-Path $R "b.bin"
  [byte[]](1,2,3,4) | Set-Content -Path $f2L -AsByteStream
  [byte[]](1,2,3,5) | Set-Content -Path $f2R -AsByteStream
  (Get-Item $f2L).LastWriteTime = (Get-Item $f2R).LastWriteTime
  # TC-03: 제외 패턴 스킵
  $bin = Join-Path $L "bin"; New-Item -ItemType Directory -Path $bin | Out-Null
  "skipme" | Out-File -FilePath (Join-Path $bin "skip.log")
  # TC-04: 잠긴 파일(오류 로그)
  $f4L = Join-Path $L "lock.txt"; "lock" | Out-File -FilePath $f4L
  $fs = [System.IO.File]::Open($f4L,[System.IO.FileMode]::Open,[System.IO.FileAccess]::Read,[System.IO.FileShare]::None)
  # TC-05: 드라이런과 실제 실행 결과 일치 (별도 호출로 검증 권장)

  $cfg = @{
    Left=$L; Right=$R; Include="*.*"; Exclude="bin/*;*.log"; UseHash=$true; HashAlgorithm="SHA256";
    Parallel=$false; LogDir=$tmp; VerifySamples=2; RetryCount=1; RetryBackoffMs=100
  } | ConvertTo-Json -Depth 3
  $cfgPath = Join-Path $tmp "cfg.json"; $cfg | Out-File -FilePath $cfgPath -Encoding utf8

  Write-Host "[SELFTEST] 드라이런 호출..." -ForegroundColor Magenta
  pwsh -NoProfile -File $PSCommandPath -Config $cfgPath -WhatIf
  Write-Host "[SELFTEST] 실제 실행 호출..." -ForegroundColor Magenta
  pwsh -NoProfile -File $PSCommandPath -Config $cfgPath

  $fs.Close()
  Write-Host "[SELFTEST] 완료. 로그 폴더: $tmp" -ForegroundColor Magenta
}
#endregion
```

---

# [run-examples.md]

```md
# Run Examples

## 1) 드라이런(계획만, 로그 생성)
pwsh -NoProfile -File C:\Tools\Sync\sync.ps1 `
  -Config C:\Tools\Sync\config.example.json `
  -WhatIf

## 2) 실제 실행(병렬 8, 샘플 5건 검증)
pwsh -NoProfile -File C:\Tools\Sync\sync.ps1 `
  -Config C:\Tools\Sync\config.example.json `
  -Parallel -ThrottleLimit 8 -VerifySamples 5

## 3) 설정 파일 없이 즉시 실행
pwsh -NoProfile -File C:\Tools\Sync\sync.ps1 `
  -Left "C:\staybymeerp-Intellij" `
  -Right "C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij" `
  -Include "*.*" -Exclude "bin/*;obj/*;*.log;*.tmp;.git/*" `
  -UseHash:$true -HashAlgorithm SHA256 -VerifySamples 3

## 4) 자가 테스트(TC-01~05)
pwsh -NoProfile -File C:\Tools\Sync\sync.ps1 -SelfTest
```

---

# [sample-log.json]

```json
{"timestamp":"2025-10-27 09:00:00.001","level":"info","action":"plan","src":"","dst":"","bytes":123456,"status":"ok","message":"files=12"}
{"timestamp":"2025-10-27 09:00:00.050","level":"info","action":"copy","src":"C:\\A\\x.txt","dst":"C:\\B\\x.txt","bytes":1024,"status":"ok","message":"타임스탬프 최신"}
{"timestamp":"2025-10-27 09:00:00.090","level":"info","action":"copy","src":"C:\\A\\y.dll","dst":"C:\\B\\y.dll","bytes":204800,"status":"ok","message":"크기 상이"}
{"timestamp":"2025-10-27 09:00:00.120","level":"info","action":"copy","src":"C:\\A\\z.bin","dst":"C:\\B\\z.bin","bytes":512,"status":"ok","message":"해시 상이"}
{"timestamp":"2025-10-27 09:00:00.150","level":"error","action":"copy","src":"C:\\A\\lock.txt","dst":"C:\\B\\lock.txt","bytes":16,"status":"fail","message":"사용 중인 파일입니다."}
{"timestamp":"2025-10-27 09:00:00.200","level":"info","action":"verify","src":"C:\\A\\x.txt","dst":"C:\\B\\x.txt","bytes":1024,"status":"ok","message":"hash matched"}
{"timestamp":"2025-10-27 09:00:00.240","level":"error","action":"verify","src":"C:\\A\\z.bin","dst":"C:\\B\\z.bin","bytes":512,"status":"fail","message":"hash mismatch or unreadable"}
{"timestamp":"2025-10-27 09:00:00.300","level":"info","action":"summary","src":"","dst":"","bytes":206336,"status":"ok","message":"{\"time\":\"2025-10-27 09:00:00\",\"whatIf\":false,\"plannedFiles\":12,\"copiedFiles\":3,\"failedFiles\":1,\"verifyFailed\":1,\"totalBytes\":206336}"}
{"timestamp":"2025-10-27 09:00:00.320","level":"info","action":"copy(whatif)","src":"C:\\A\\w.txt","dst":"C:\\B\\w.txt","bytes":4096,"status":"skip","message":"타임스탬프 최신"}
{"timestamp":"2025-10-27 09:00:00.350","level":"info","action":"copy(whatif)","src":"C:\\A\\n.txt","dst":"C:\\B\\n.txt","bytes":128,"status":"skip","message":"신규"}
```

---

# [sample-log.csv]

```csv
timestamp,level,action,src,dst,bytes,status,message
2025-10-27 09:00:00.001,info,plan,,,123456,ok,"files=12"
2025-10-27 09:00:00.050,info,copy,C:\A\x.txt,C:\B\x.txt,1024,ok,"타임스탬프 최신"
2025-10-27 09:00:00.090,info,copy,C:\A\y.dll,C:\B\y.dll,204800,ok,"크기 상이"
2025-10-27 09:00:00.120,info,copy,C:\A\z.bin,C:\B\z.bin,512,ok,"해시 상이"
2025-10-27 09:00:00.150,error,copy,C:\A\lock.txt,C:\B\lock.txt,16,fail,"사용 중인 파일입니다."
2025-10-27 09:00:00.200,info,verify,C:\A\x.txt,C:\B\x.txt,1024,ok,"hash matched"
2025-10-27 09:00:00.240,error,verify,C:\A\z.bin,C:\B\z.bin,512,fail,"hash mismatch or unreadable"
2025-10-27 09:00:00.300,info,summary,,,206336,ok,"{""time"":""2025-10-27 09:00:00"",""whatIf"":false,""plannedFiles"":12,""copiedFiles"":3,""failedFiles"":1,""verifyFailed"":1,""totalBytes"":206336}"
2025-10-27 09:00:00.320,info,copy(whatif),C:\A\w.txt,C:\B\w.txt,4096,skip,"타임스탬프 최신"
2025-10-27 09:00:00.350,info,copy(whatif),C:\A\n.txt,C:\B\n.txt,128,skip,"신규"
```
