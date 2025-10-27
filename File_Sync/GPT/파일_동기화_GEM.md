## 1\) [README.md]

````markdown
# 📂 폴더 동기화 스크립트 (Sync-Folder.ps1)

`Left` 폴더(원본)의 변경 사항을 `Right` 폴더(대상)로 단방향 동기화하는 PowerShell 7+ 스크립트입니다.

## 🚀 주요 기능

-   **단방향 동기화 (Left ➜ Right)**: 원본을 기준으로 대상에 파일을 추가하거나 덮어씁니다.
-   **삭제 미지원 (안전성)**: 원본에 파일이 없다고 해서 대상의 파일을 삭제하지 않습니다.
-   **다중 비교 기준**: 1) 타임스탬프 ➜ 2) 파일 크기 ➜ 3) (선택) SHA-256 해시
-   **유연한 설정**: `config.json` 파일을 통해 경로, 필터, 옵션을 쉽게 관리
-   **안전 모드 (DryRun)**: 실제 복사/덮어쓰기 없이 실행 결과를 미리 리포트
-   **상세 로깅**: 모든 작업 내역을 `JSON` 및 `CSV` 파일로 동시 저장

## 📋 요구 사항

-   Windows 10 / 11
-   **PowerShell 7.0 이상**

## ⚙️ 설치 (최초 1회)

1.  스크립트(`sync.ps1`)와 설정 파일(`config.json`)을 원하는 폴더에 저장합니다.
2.  PowerShell을 **관리자 권한**으로 실행합니다.
3.  스크립트 실행 정책을 변경합니다. (필요시)
    ```powershell
    # 현재 사용자에 대해서만 스크립트 실행 허용
    Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
    ```

## 🏃‍♀️ 실행 방법

PowerShell 7 터미널에서 스크립트가 있는 폴더로 이동 후 실행합니다.

```powershell
# 1. 기본 실행 (config.json 설정 사용)
.\sync.ps1

# 2. 드라이런(DryRun) 모드로 시뮬레이션
.\sync.ps1 -DryRun

# 3. 해시(SHA-256) 검사 포함하여 실행
.\sync.ps1 -UseHash

# 4. 다른 설정 파일 지정하여 실행
.\sync.ps1 -ConfigPath 'C:\MyConfigs\project_b_sync.json'

# 5. 경로를 직접 지정하여 실행 (설정 파일보다 우선 적용됨)
.\sync.ps1 -LeftPath 'D:\Source' -RightPath 'E:\Backup'
````

## 🔧 설정 (config.json)

스크립트는 기본적으로 `.\config.json` 파일을 읽어옵니다.

  - `LeftPath`: (필수) 원본 폴더 경로
  - `RightPath`: (필수) 대상 폴더 경로
  - `ExcludePatterns`: (필수) 동기화에서 제외할 패턴 (PowerShell Wildcard)
  - `LogDirectory`: (필수) 로그 파일이 저장될 폴더
  - `LogLevel`: (선택) 콘솔 출력 레벨 (`Debug`, `Info`, `Warn`, `Error` - 기본값: `Info`)
  - `UseHash`: (선택) 타임스탬프/크기가 같아도 해시 비교 수행 여부 (기본값: `false`)
  - `DryRun`: (선택) 드라이런 모드 활성화 여부 (기본값: `false`)

## 🚦 종료 코드 (Exit Codes)

스크립트는 실행 결과에 따라 다음 종료 코드를 반환합니다.

  - `0`: 동기화 성공 (오류 없음)
  - `1`: 부분 성공 (일부 파일 복사 실패 등 비치명적 오류 발생)
  - `2`: 치명적 실패 (경로 없음, 설정 오류 등 실행 불가)

## ⚠️ 복구 가이드 (중요)

이 스크립트는 **덮어쓰기**를 수행하며 **롤백(Rollback) 기능이 없습니다.**

1.  **첫 실행은 `DryRun`**: `-DryRun` 옵션으로 어떤 파일이 변경될지 반드시 먼저 확인하세요.
2.  **백업 생활화**: 중요한 데이터는 이 스크립트에만 의존하지 말고, Windows 파일 히스토리나 별도 백업 솔루션을 사용하세요.
3.  **로그 확인**: 작업 후 `summary` 로그를 확인하여 실패(`Error`) 항목이 있는지 검토하세요.
4.  **휴지통**: 덮어써진 파일은 복구가 어렵습니다. `Right` 폴더에 대해 VSS(볼륨 섀도 복사본)를 활성화하는 것을 권장합니다.
5.  **삭제 정책 없음**: 이 스크립트는 파일을 삭제하지 않습니다. `Right` 폴더에 불필요한 파일이 쌓일 수 있습니다.

<!-- end list -->

````

---

## 2) [config.example.json]

```json
{
  "Settings": {
    "LeftPath": "C:\\staybymeerp-Intellij\\",
    "RightPath": "C:\\WORK\\PROJECT\\SBM\\VSCODE\\staybymeerp-Intellij\\",
    "ExcludePatterns": [
      "bin/*",
      "obj/*",
      "*.log",
      "*.tmp",
      ".git/*",
      "node_modules/*"
    ],
    "LogDirectory": "C:\\Logs",
    "LogLevel": "Info",
    "UseHash": false,
    "DryRun": false
  }
}
````

-----

## 3\) [config.example.yaml]

```yaml
# 참고: 이 스크립트는 YAML을 직접 읽지 않으며, JSON 설정 예시의 YAML 버전입니다.
Settings:
  LeftPath: C:\staybymeerp-Intellij\
  RightPath: C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\
  ExcludePatterns:
    - "bin/*"
    - "obj/*"
    - "*.log"
    - "*.tmp"
    - ".git/*"
    - "node_modules/*"
  LogDirectory: C:\Logs
  LogLevel: Info
  UseHash: false
  DryRun: false
```

-----

## 4\) [sync.ps1]

```powershell
<#
.SYNOPSIS
    두 폴더 간의 단방향(Left->Right) 동기화를 수행합니다.
    (프로젝트 지침 A1)

.DESCRIPTION
    지정된 Left(원본) 폴더를 스캔하여 Right(대상) 폴더와 비교합니다.
    파일이 없거나, 타임스탬프/크기/해시(옵션)가 다른 경우에만 파일을 복사/덮어쓰기합니다.
    대상 폴더의 파일은 삭제하지 않습니다.

.NOTES
    Version: 1.0.0 (2025-10-27)
    Author: 프로그램 도우미 (AI)
    Requires: PowerShell 7.0+
#>

# =============================================================================
# 스크립트 설정 및 매개변수 (지침 A1, F1, F2)
# =============================================================================
[CmdletBinding()]
param (
    [string]$LeftPath,
    [string]$RightPath,
    [string]$ConfigPath = ".\config.json",
    [string]$LogDirectory,
    [switch]$UseHash,
    [switch]$DryRun,
    [ValidateSet('Debug', 'Info', 'Warn', 'Error')]
    [string]$LogLevel
)

# --- 전역 오류 처리 (지침 A2) ---
$ErrorActionPreference = "Stop"

# --- 전역 변수 ---
$Global:ExitCode = 0 # 0:성공, 1:부분실패, 2:치명적실패
$Global:LogEntries = [System.Collections.Generic.List[PSObject]]::new()
$Global:LogTimestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$Global:JsonLogPath = ""
$Global:CsvLogPath = ""
$Global:ScriptStartTime = Get-Date
$Global:LogLevelNumeric = @{
    'Debug' = 1
    'Info'  = 2
    'Warn'  = 3
    'Error' = 4
}
$Global:CurrentLogLevel = 2 # 기본값 Info

# =============================================================================
# 헬퍼 함수
# =============================================================================

function Initialize-Config {
    <#
    .SYNOPSIS
    설정 파일을 로드하고 매개변수와 병합합니다. (지침 F1)
    #>
    Write-Log "Debug" "설정 초기화 시작..."
    
    $defaultConfig = @{
        LeftPath        = "C:\staybymeerp-Intellij\"
        RightPath       = "C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\"
        ExcludePatterns = @("bin/*", "obj/*", "*.log", "*.tmp", ".git/*")
        LogDirectory    = "C:\Logs"
        LogLevel        = "Info"
        UseHash         = $false
        DryRun          = $false
    }

    $configFromFile = @{}
    if (Test-Path $ConfigPath -PathType Leaf) {
        try {
            Write-Log "Info" "설정 파일 로드 중: $ConfigPath"
            $configFromFile = (Get-Content $ConfigPath -Raw | ConvertFrom-Json).Settings
        } catch {
            Write-Log "Error" "설정 파일($ConfigPath)을 읽는 중 오류 발생: $($_.Exception.Message)"
            throw "설정 파일 파싱 실패"
        }
    } else {
        Write-Log "Warn" "설정 파일($ConfigPath)을 찾을 수 없습니다. 기본값으로 실행합니다."
    }

    # 우선순위: CLI 매개변수 > 설정 파일 > 기본값 (지침 F1)
    $finalConfig = $defaultConfig.Clone()
    $configFromFile.GetEnumerator() | ForEach-Object { $finalConfig[$_.Name] = $_.Value }
    
    # CLI 매개변수가 제공된 경우 덮어쓰기
    if ($PSBoundParameters.ContainsKey('LeftPath')) { $finalConfig.LeftPath = $LeftPath }
    if ($PSBoundParameters.ContainsKey('RightPath')) { $finalConfig.RightPath = $RightPath }
    if ($PSBoundParameters.ContainsKey('LogDirectory')) { $finalConfig.LogDirectory = $LogDirectory }
    if ($PSBoundParameters.ContainsKey('UseHash')) { $finalConfig.UseHash = $UseHash }
    if ($PSBoundParameters.ContainsKey('DryRun')) { $finalConfig.DryRun = $DryRun }
    if ($PSBoundParameters.ContainsKey('LogLevel')) { $finalConfig.LogLevel = $LogLevel }

    # 전역 로그 레벨 설정
    $Global:CurrentLogLevel = $Global:LogLevelNumeric[$finalConfig.LogLevel]

    # 로그 파일 경로 설정 (지침 E1)
    if (-not (Test-Path $finalConfig.LogDirectory)) {
        Write-Log "Warn" "로그 디렉토리($($finalConfig.LogDirectory))가 없습니다. 생성을 시도합니다."
        try {
            New-Item -Path $finalConfig.LogDirectory -ItemType Directory -Force | Out-Null
        } catch {
            Write-Error "로그 디렉토리($($finalConfig.LogDirectory))를 생성할 수 없습니다. 스크립트를 종료합니다."
            throw
        }
    }
    $logBaseName = "sync_$($Global:LogTimestamp)"
    $Global:JsonLogPath = Join-Path -Path $finalConfig.LogDirectory -ChildPath "$logBaseName.json"
    $Global:CsvLogPath = Join-Path -Path $finalConfig.LogDirectory -ChildPath "$logBaseName.csv"
    
    Write-Log "Info" "로그 파일(JSON): $($Global:JsonLogPath)"
    Write-Log "Info" "로그 파일(CSV): $($Global:CsvLogPath)"

    # DryRun 모드이면 경고 출력
    if ($finalConfig.DryRun) {
        Write-Log "Warn" "*** 드라이런(DryRun) 모드로 실행됩니다. 실제 파일 작업은 수행되지 않습니다. ***"
    }

    return $finalConfig
}

function Write-Log {
    <#
    .SYNOPSIS
    콘솔과 로그 파일에 메시지를 기록합니다. (지침 E2, E3)
    #>
    param (
        [Parameter(Mandatory)]
        [ValidateSet('Debug', 'Info', 'Warn', 'Error')]
        [string]$Level,
        
        [Parameter(Mandatory)]
        [string]$Message
    )

    $numericLevel = $Global:LogLevelNumeric[$Level]
    if ($numericLevel -lt $Global:CurrentLogLevel) {
        return # 설정된 로그 레벨보다 낮으면 콘솔에 출력 안 함
    }
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logLine = "[$timestamp] [$Level] $Message"

    # 콘솔 출력
    switch ($Level) {
        "Debug" { Write-Host $logLine -ForegroundColor Gray }
        "Info"  { Write-Host $logLine -ForegroundColor White }
        "Warn"  { Write-Warning $logLine }
        "Error" { Write-Error $logLine }
    }
}

function Add-LogEntry {
    <#
    .SYNOPSIS
    로그 항목을 전역 리스트에 추가합니다. (지침 E1, E3)
    #>
    param(
        [Parameter(Mandatory)]
        [string]$Status, # Copied, Skipped, Error, DryRun_Copy, DryRun_Skip
        [Parameter(Mandatory)]
        [string]$Reason,
        [string]$RelativePath,
        [string]$SourcePath,
        [string]$TargetPath,
        [long]$SourceSize,
        [long]$TargetSize
    )

    $logEntry = [PSCustomObject]@{
        Timestamp    = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        Status       = $Status
        Reason       = $Reason
        RelativePath = $RelativePath
        SourcePath   = $SourcePath
        TargetPath   = $TargetPath
        SourceSizeMB = if ($SourceSize) { [math]::Round($SourceSize / 1MB, 3) } else { 0 }
        TargetSizeMB = if ($TargetSize) { [math]::Round($TargetSize / 1MB, 3) } else { 0 }
    }
    
    $Global:LogEntries.Add($logEntry)
    
    # 에러 발생 시 종료 코드 업데이트 (지침 QoS)
    if ($Status -eq 'Error') {
        $Global:ExitCode = 1 # 부분 실패
    }
}

function Get-FileHashSafe {
    <#
    .SYNOPSIS
    파일 해시(SHA256)를 안전하게 계산합니다. (지침 C2, H1)
    #>
    param (
        [Parameter(Mandatory)]
        [string]$FilePath
    )

    try {
        # 대용량 파일 처리를 위해 스트림 사용
        $stream = [System.IO.File]::OpenRead($FilePath)
        $sha256 = [System.Security.Cryptography.SHA256]::Create()
        $hashBytes = $sha256.ComputeHash($stream)
        $hashString = [System.BitConverter]::ToString($hashBytes).Replace('-', '').ToLower()
        return $hashString
    } catch {
        Write-Log "Error" "해시 계산 실패: $FilePath. 오류: $($_.Exception.Message)"
        return $null
    } finally {
        if ($stream) { $stream.Dispose() }
        if ($sha256) { $sha256.Dispose() }
    }
}

# =============================================================================
# 메인 실행 로직 (지침 A2)
# =============================================================================
try {
    # ------------------------------------------------
    # 1. 설정 및 검증 (Procedure 1)
    # ------------------------------------------------
    $Config = Initialize-Config
    $Counters = @{
        Checked   = 0
        Copied    = 0
        Skipped   = 0
        Errors    = 0
    }
    
    Write-Log "Info" "--- 동기화 작업 시작 ---"
    Write-Log "Info" "원본(Left): $($Config.LeftPath)"
    Write-Log "Info" "대상(Right): $($Config.RightPath)"
    Write-Log "Info" "해시 검사: $($Config.UseHash)"
    Write-Log "Info" "드라이런: $($Config.DryRun)"

    # 경로 정규화 및 검증
    try {
        $normalizedLeftPath = (Resolve-Path $Config.LeftPath).Path
        if (-not $normalizedLeftPath.EndsWith('\')) { $normalizedLeftPath += '\' }
    } catch {
        Write-Log "Error" "원본(Left) 경로를 찾을 수 없습니다: $($Config.LeftPath)"
        throw
    }

    try {
        if (-not (Test-Path $Config.RightPath)) {
            Write-Log "Warn" "대상(Right) 경로를 찾을 수 없습니다. 자동 생성합니다: $($Config.RightPath)"
            if (-not $Config.DryRun) {
                New-Item -Path $Config.RightPath -ItemType Directory -Force | Out-Null
            }
        }
        $normalizedRightPath = (Resolve-Path $Config.RightPath).Path
        if (-not $normalizedRightPath.EndsWith('\')) { $normalizedRightPath += '\' }
    } catch {
        Write-Log "Error" "대상(Right) 경로를 생성하거나 접근할 수 없습니다: $($Config.RightPath)"
        throw
    }

    # ------------------------------------------------
    # 2. 스캔 단계 (Procedure 2, C1)
    # ------------------------------------------------
    Write-Log "Info" "원본(Left) 폴더 스캔 중..."
    
    # 필터 패턴을 Regex로 변환 (지침 H1 - 경로 문제 해결)
    $excludeRegexPatterns = $Config.ExcludePatterns.ForEach({
        # 와일드카드를 정규식으로 변환하고 경로 구분자(\) 이스케이프
        $pattern = $_.Replace('/', '\')
        $regex = [WildcardPattern]::Get($pattern, [WildcardOptions]::IgnoreCase).ToRegex()
        # 경로 시작(^)을 명시하여 'bin/'이 'test/bin/'에도 매칭되도록 함
        # 단, '*.log' 같은 패턴은 경로 시작이 아니어야 함
        if ($pattern.Contains('\')) {
             # '\'로 시작하거나(절대경로 필터) '\'를 포함하면(하위 디렉토리)
             if ($pattern.StartsWith('\')) {
                 "^" + $regex.Substring(1) # 맨 앞 '\'에 대한 ^ 대체
             } else {
                 # 'bin/*' 같은 패턴은 '\bin\' 또는 '^bin\'에 매칭되어야 함
                 "\\" + $regex + "|" + "^" + $regex
             }
        } else {
             $regex # '*.log' 같은 파일 패턴
        }
    })
    
    $allLeftFiles = Get-ChildItem -Path $normalizedLeftPath -Recurse -File -ErrorAction SilentlyContinue

    $leftFiles = $allLeftFiles | Where-Object {
        $relativePath = $_.FullName.Substring($normalizedLeftPath.Length)
        $isExcluded = $false
        foreach ($regex in $excludeRegexPatterns) {
            if ($relativePath -match $regex) {
                $isExcluded = $true
                break
            }
        }
        if ($isExcluded) {
            Write-Log "Debug" "제외됨 (패턴 매칭): $relativePath"
        }
        -not $isExcluded
    }

    $totalFiles = $leftFiles.Count
    Write-Log "Info" "총 $($allLeftFiles.Count)개 파일 발견, 제외 패턴 적용 후 $($totalFiles)개 파일 처리 대상."

    if ($totalFiles -eq 0) {
        Write-Log "Warn" "처리할 파일이 없습니다."
    }

    # ------------------------------------------------
    # 3. 비교 및 실행 (Procedure 3, 4, C3)
    # ------------------------------------------------
    foreach ($file in $leftFiles) {
        $Counters.Checked++
        $relativePath = $file.FullName.Substring($normalizedLeftPath.Length)
        $targetPath = Join-Path -Path $normalizedRightPath -ChildPath $relativePath
        
        Write-Progress -Activity "파일 동기화 중" -Status "($($Counters.Checked)/$totalFiles) $relativePath" -PercentComplete (($Counters.Checked / $totalFiles) * 100)

        try {
            $targetFile = Get-Item -Path $targetPath -ErrorAction SilentlyContinue
            
            $shouldCopy = $false
            $reason = ""
            $logStatus = "Skipped"

            if (-not $targetFile) {
                $shouldCopy = $true
                $reason = "NewFile"
            } elseif ($file.LastWriteTime -gt $targetFile.LastWriteTime) {
                $shouldCopy = $true
                $reason = "TimestampNewer"
            } elseif ($file.Length -ne $targetFile.Length) {
                $shouldCopy = $true
                $reason = "SizeMismatch"
            } elseif ($Config.UseHash) {
                Write-Log "Debug" "해시 비교 수행: $relativePath"
                $leftHash = Get-FileHashSafe $file.FullName
                $rightHash = Get-FileHashSafe $targetFile.FullName

                if ($null -eq $leftHash -or $null -eq $rightHash) {
                    $reason = "HashCheckError"
                    $logStatus = "Error"
                    $Counters.Errors++
                } elseif ($leftHash -ne $rightHash) {
                    $shouldCopy = $true
                    $reason = "HashMismatch"
                } else {
                    $reason = "Identical (Hash)"
                }
            } else {
                $reason = "Identical (Timestamp/Size)"
            }

            # 4. 복사 수행 (Procedure 4)
            if ($shouldCopy) {
                Write-Log "Info" "복사 대상: $relativePath (이유: $reason)"
                if ($Config.DryRun) {
                    $logStatus = "DryRun_Copy"
                    $Counters.Copied++ # DryRun에서도 카운트는 함
                    Write-Log "Warn" "[DryRun] 복사 실행: $($file.FullName) -> $targetPath"
                } else {
                    # (지침 A3) 외부 프로세스 대신 네이티브 명령 사용
                    $targetDir = Split-Path -Path $targetPath -Parent
                    if (-not (Test-Path $targetDir)) {
                        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
                    }
                    Copy-Item -Path $file.FullName -Destination $targetPath -Force -ErrorAction Stop
                    $logStatus = "Copied"
                    $Counters.Copied++
                }
            } else {
                if ($logStatus -ne "Error") {
                    $Counters.Skipped++
                    Write-Log "Debug" "스킵: $relativePath (이유: $reason)"
                }
            }

            Add-LogEntry -Status $logStatus -Reason $reason -RelativePath $relativePath `
                -SourcePath $file.FullName -TargetPath $targetPath `
                -SourceSize $file.Length -TargetSize $targetFile.Length

        } catch {
            # (지침 A2) 개별 파일 오류 처리
            $Counters.Errors++
            $errorMessage = $_.Exception.Message
            Write-Log "Error" "파일 처리 실패: $relativePath. 오류: $errorMessage"
            Add-LogEntry -Status "Error" -Reason $errorMessage -RelativePath $relativePath `
                -SourcePath $file.FullName -TargetPath $targetPath
        }
    } # end foreach file
    
    Write-Progress -Activity "파일 동기화" -Completed

} catch {
    # ------------------------------------------------
    # 5. 치명적 오류 처리 (Procedure 6)
    # ------------------------------------------------
    $Global:ExitCode = 2 # 치명적 실패
    Write-Log "Error" "스크립트 실행 중 치명적인 오류가 발생했습니다: $($_.Exception.Message)"
    Write-Log "Error" $_.ScriptStackTrace
    
} finally {
    # ------------------------------------------------
    # 6. 로깅 및 요약 (Procedure 5)
    # ------------------------------------------------
    $endTime = Get-Date
    $duration = New-TimeSpan -Start $ScriptStartTime -End $endTime
    
    $summary = @"

--- 동기화 요약 (종료 코드: $Global:ExitCode) ---
시작 시간: $($ScriptStartTime.ToString('yyyy-MM-dd HH:mm:ss'))
종료 시간: $($endTime.ToString('yyyy-MM-dd HH:mm:ss'))
총 소요 시간: $($duration.ToString('g'))

총 확인 파일: $($Counters.Checked)
복사/덮어쓰기: $($Counters.Copied)
스킵 (동일): $($Counters.Skipped)
오류 발생: $($Counters.Errors)

로그 파일 (JSON): $Global:JsonLogPath
로그 파일 (CSV): $Global:CsvLogPath
------------------------------------
"@
    
    Write-Log "Info" $summary

    # (지침 E1) 로그 파일 저장
    if ($Global:LogEntries.Count -gt 0) {
        try {
            Write-Log "Info" "JSON 로그 파일 저장 중..."
            $Global:LogEntries | ConvertTo-Json | Set-Content -Path $Global:JsonLogPath -Encoding UTF8
            
            Write-Log "Info" "CSV 로그 파일 저장 중..."
            $Global:LogEntries | Export-Csv -Path $Global:CsvLogPath -NoTypeInformation -Encoding UTF8
        } catch {
            Write-Log "Error" "로그 파일 저장 실패: $($_.Exception.Message)"
            if ($Global:ExitCode -eq 0) { $Global:ExitCode = 1 }
        }
    } else {
        Write-Log "Info" "기록할 로그 항목이 없습니다."
    }
    
    Write-Log "Info" "동기화 세션 종료."
    exit $Global:ExitCode
}
```

-----

## 5\) [run-examples.md]

````markdown
# 🚀 실행 예시

(PowerShell 7 터미널에서 `sync.ps1` 파일이 있는 디렉토리에서 실행)

## 1. 기본 실행 (권장)

`config.json` 파일의 설정을 읽어 실행합니다.

```powershell
.\sync.ps1
````

## 2\. 드라이런 (DryRun) 모드 (필수 확인)

실제 파일 복사 없이, 어떤 파일이 복사/덮어쓰기될지 시뮬레이션합니다.
**처음 실행 시 반드시 이 모드로 확인하세요.**

```powershell
.\sync.ps1 -DryRun
```

## 3\. 강력한 해시(SHA-256) 검사 모드

타임스탬프와 파일 크기가 같더라도, 파일 내용(해시)까지 비교하여 다르면 덮어씁니다.
속도가 느릴 수 있습니다.

```powershell
.\sync.ps1 -UseHash
```

## 4\. 디버그(Debug) 모드

스킵되는 파일 등 모든 상세 정보를 콘솔에 출력합니다.

```powershell
.\sync.ps1 -LogLevel Debug
```

## 5\. 모든 옵션 조합

드라이런 + 해시 검사 + 디버그 모드로 실행

```powershell
.\sync.ps1 -DryRun -UseHash -LogLevel Debug
```

## 6\. 특정 설정 파일 사용

`config.json` 대신 다른 설정 파일을 지정합니다.

```powershell
.\sync.ps1 -ConfigPath "D:\Configs\my_project_sync.json"
```

## 7\. CLI 매개변수로 설정 덮어쓰기

`config.json`의 경로 대신, CLI에서 직접 경로를 지정합니다.
(CLI 매개변수가 설정 파일보다 항상 우선합니다.)

```powershell
.\sync.ps1 -LeftPath "D:\SourceCode" -RightPath "Z:\Backup\SourceCode"
```

````

---

## 6) [sample-log.json]

(10행 내외 샘플)

```json
[
  {
    "Timestamp": "2025-10-27 09:30:01",
    "Status": "Copied",
    "Reason": "NewFile",
    "RelativePath": "README.md",
    "SourcePath": "C:\\staybymeerp-Intellij\\README.md",
    "TargetPath": "C:\\WORK\\PROJECT\\SBM\\VSCODE\\staybymeerp-Intellij\\README.md",
    "SourceSizeMB": 0.005,
    "TargetSizeMB": 0
  },
  {
    "Timestamp": "2025-10-27 09:30:02",
    "Status": "Copied",
    "Reason": "TimestampNewer",
    "RelativePath": "src\\main.js",
    "SourcePath": "C:\\staybymeerp-Intellij\\src\\main.js",
    "TargetPath": "C:\\WORK\\PROJECT\\SBM\\VSCODE\\staybymeerp-Intellij\\src\\main.js",
    "SourceSizeMB": 1.25,
    "TargetSizeMB": 1.25
  },
  {
    "Timestamp": "2025-10-27 09:30:03",
    "Status": "Skipped",
    "Reason": "Identical (Timestamp/Size)",
    "RelativePath": "src\\style.css",
    "SourcePath": "C:\\staybymeerp-Intellij\\src\\style.css",
    "TargetPath": "C:\\WORK\\PROJECT\\SBM\\VSCODE\\staybymeerp-Intellij\\src\\style.css",
    "SourceSizeMB": 0.15,
    "TargetSizeMB": 0.15
  },
  {
    "Timestamp": "2025-10-27 09:30:04",
    "Status": "Copied",
    "Reason": "SizeMismatch",
    "RelativePath": "assets\\logo.png",
    "SourcePath": "C:\\staybymeerp-Intellij\\assets\\logo.png",
    "TargetPath": "C:\\WORK\\PROJECT\\SBM\\VSCODE\\staybymeerp-Intellij\\assets\\logo.png",
    "SourceSizeMB": 0.5,
    "TargetSizeMB": 0.45
  },
  {
    "Timestamp": "2025-10-27 09:30:05",
    "Status": "Copied",
    "Reason": "HashMismatch",
    "RelativePath": "config\\settings.json",
    "SourcePath": "C:\\staybymeerp-Intellij\\config\\settings.json",
    "TargetPath": "C:\\WORK\\PROJECT\\SBM\\VSCODE\\staybymeerp-Intellij\\config\\settings.json",
    "SourceSizeMB": 0.01,
    "TargetSizeMB": 0.01
  },
  {
    "Timestamp": "2025-10-27 09:30:06",
    "Status": "Error",
    "Reason": "파일이 다른 프로세스에 의해 사용되고 있으므로 프로세스에서 파일에 액세스할 수 없습니다.",
    "RelativePath": "data\\cache.db",
    "SourcePath": "C:\\staybymeerp-Intellij\\data\\cache.db",
    "TargetPath": "C:\\WORK\\PROJECT\\SBM\\VSCODE\\staybymeerp-Intellij\\data\\cache.db",
    "SourceSizeMB": 10.5,
    "TargetSizeMB": 10.5
  },
  {
    "Timestamp": "2025-10-27 09:30:07",
    "Status": "DryRun_Copy",
    "Reason": "NewFile",
    "RelativePath": "new_feature\\index.html",
    "SourcePath": "C:\\staybymeerp-Intellij\\new_feature\\index.html",
    "TargetPath": "C:\\WORK\\PROJECT\\SBM\\VSCODE\\staybymeerp-Intellij\\new_feature\\index.html",
    "SourceSizeMB": 0.02,
    "TargetSizeMB": 0
  }
]
````

-----

## 7\) [sample-log.csv]

(10행 내외 샘플)

```csv
"Timestamp","Status","Reason","RelativePath","SourcePath","TargetPath","SourceSizeMB","TargetSizeMB"
"2025-10-27 09:30:01","Copied","NewFile","README.md","C:\staybymeerp-Intellij\README.md","C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\README.md","0.005","0"
"2025-10-27 09:30:02","Copied","TimestampNewer","src\main.js","C:\staybymeerp-Intellij\src\main.js","C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\src\main.js","1.25","1.25"
"2025-10-27 09:30:03","Skipped","Identical (Timestamp/Size)","src\style.css","C:\staybymeerp-Intellij\src\style.css","C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\src\style.css","0.15","0.15"
"2025-10-27 09:30:04","Copied","SizeMismatch","assets\logo.png","C:\staybymeerp-Intellij\assets\logo.png","C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\assets\logo.png","0.5","0.45"
"2025-10-27 09:30:05","Copied","HashMismatch","config\settings.json","C:\staybymeerp-Intellij\config\settings.json","C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\config\settings.json","0.01","0.01"
"2025-10-27 09:30:06","Error","파일이 다른 프로세스에 의해 사용되고 있으므로 프로세스에서 파일에 액세스할 수 없습니다.","data\cache.db","C:\staybymeerp-Intellij\data\cache.db","C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\data\cache.db","10.5","10.5"
"2025-10-27 09:30:07","DryRun_Copy","NewFile","new_feature\index.html","C:\staybymeerp-Intellij\new_feature\index.html","C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\new_feature\index.html","0.02","0"
```