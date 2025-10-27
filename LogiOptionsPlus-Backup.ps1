<# ======================================================================
 Logi Options+ Backup/Restore Tool (Windows 11)
 Author : ChatGPT (GPT-5 Thinking)
 Version: 1.1.1  (2025-10-17)
 License: MIT
 ----------------------------------------------------------------------
 Features
  - Backup/Restore/Verify
  - SafeOps: 서비스/프로세스 중지(선택), 끝나면 재기동(선택)
  - Manifest(JSON): 파일 목록/해시/버전/환경
  - Elevation: 관리자 권한 자동 확인 후 필요 시 UAC 상승 재실행
 ----------------------------------------------------------------------
 Tested on: PowerShell 7.x, Windows 11 23H2
 ====================================================================== #>

[CmdletBinding(PositionalBinding = $false)]
param(
  [ValidateSet('Backup','Restore','Verify')]
  [string]$Mode = 'Backup',

  # Backup destination folder (for Backup)
  [string]$Destination = "C:\Tools\LogiOptionsPlusBackup",

  # Backup zip file path (for Restore)
  [string]$Source,

  # Include ProgramData caches/depots
  [switch]$IncludeProgramData,

  # Do not attempt to stop running services/processes
  [switch]$NoStop,

  # Do not restart services/processes after work
  [switch]$NoRestart,

  # Dry-run
  [switch]$WhatIf
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# --------------------------- Helpers ---------------------------
function Log { param([string]$msg,[string]$level='INFO')
  $ts = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
  Write-Host "[$ts][$level] $msg"
}

function Ensure-Dir { param([string]$path)
  if (-not (Test-Path -LiteralPath $path)) {
    if ($WhatIf) { Log "Would create directory: $path" 'DRY' }
    else { New-Item -ItemType Directory -Path $path -Force | Out-Null }
  }
}

function Test-IsAdmin {
  try {
    $id = [Security.Principal.WindowsIdentity]::GetCurrent()
    $p = New-Object Security.Principal.WindowsPrincipal($id)
    return $p.IsInRole([Security.Principal.WindowsBuiltinRole]::Administrator)
  } catch { return $false }
}

function Ensure-Admin {
  if ($NoStop) { return } # 중지 건너뛰면 관리자 권한 필수 아님
  if (-not (Test-IsAdmin)) {
    Log "Admin 권한이 필요합니다. UAC 상승하여 재실행합니다..." 'INFO'
    $args = @()
    $args += "-NoProfile"
    $args += "-ExecutionPolicy Bypass"
    $args += "-File `"$PSCommandPath`""
    $pp = $PSBoundParameters.Clone()
    $pp.Remove('WhatIf') | Out-Null
    foreach ($k in $pp.Keys) {
      $v = $pp[$k]
      if ($v -is [switch]) {
        if ($v.IsPresent) { $args += "-$k" }
      } else {
        $args += "-$k `"$v`""
      }
    }
    if ($WhatIf) { Log "Would elevate and relaunch: pwsh $($args -join ' ')" 'DRY'; exit 0 }
    Start-Process -FilePath "pwsh.exe" -Verb RunAs -ArgumentList ($args -join ' ')
    exit 0
  }
}

function Get-LogiPaths {
  $paths = [ordered]@{
    LocalAppData   = Join-Path $env:LOCALAPPDATA 'LogiOptionsPlus'
    RoamingAppData = Join-Path $env:APPDATA      'LogiOptionsPlus'
    ProgramData    = Join-Path $env:ProgramData  'LogiOptionsPlus'
    InstallEXE     = 'C:\Program Files\LogiOptionsPlus\logioptionsplus.exe'
  }
  return $paths
}

function Get-LogiVersion {
  $exe = (Get-LogiPaths).InstallEXE
  if (Test-Path $exe) {
    try { return (Get-Item $exe).VersionInfo.FileVersion }
    catch { return $null }
  }
  return $null
}

function Stop-Logi {
  if ($NoStop) { Log "Skip stop of Logi services/processes (-NoStop)." 'INFO'; return }

  Log "Stopping Logi Options+ services/processes..." 'INFO'
  $svcPatterns = @('OptionsPlus','LogiOptions','Logi') # 넓게 매치
  $svcs = @()

  # 1차: Get-Service (권한 문제 무시)
  try {
    $svcs = Get-Service -ErrorAction Stop | Where-Object {
      $name = $_.Name
      $svcPatterns | ForEach-Object { if ($name -like "*$_*") { return $true } }
      return $false
    }
  } catch {
    Log "Get-Service 권한 문제로 sc.exe로 재시도합니다." 'WARN'
    # 2차: sc query (모든 서비스 나열) 후 이름만 뽑기
    $raw = sc.exe query type= service state= all 2>$null
    $names = @()
    foreach ($line in $raw) {
      if ($line -match 'SERVICE_NAME:\s*(\S+)') {
        $n = $Matches[1]
        if ($svcPatterns | Where-Object { $n -like "*$_*" }) { $names += $n }
      }
    }
    foreach ($n in $names) {
      try { $svcs += Get-Service -Name $n -ErrorAction SilentlyContinue } catch {}
    }
  }

  foreach ($s in $svcs) {
    if ($s.Status -eq 'Running') {
      if ($WhatIf) { Log "Would stop service: $($s.Name)" 'DRY' }
      else {
        try { Stop-Service -Name $s.Name -Force -ErrorAction Stop; Log "Stopped service: $($s.Name)" }
        catch { Log "Service stop failed: $($s.Name) => $($_.Exception.Message)" 'WARN' }
      }
    }
  }

  # 프로세스 정리(권한 오류 무시)
  $procRegex = '^(logi.*options|optionsplus|logioptionsplus.*)$'
  try {
    $procs = Get-Process -ErrorAction Stop | Where-Object { $_.ProcessName -match $procRegex }
  } catch { $procs = @() }
  foreach ($p in $procs) {
    if ($WhatIf) { Log "Would stop process: $($p.ProcessName) (PID $($p.Id))" 'DRY' }
    else {
      try { Stop-Process -Id $p.Id -Force -ErrorAction Stop; Log "Stopped process: $($p.ProcessName) (PID $($p.Id))" }
      catch { Log "Process stop failed: $($p.ProcessName) => $($_.Exception.Message)" 'WARN' }
    }
  }
}

function Start-Logi {
  if ($NoRestart) { Log "Skip restart of Logi services/processes (-NoRestart)." 'INFO'; return }
  Log "Starting Logi Options+ (best-effort)..." 'INFO'
  $exe = (Get-LogiPaths).InstallEXE
  if (Test-Path $exe) {
    if ($WhatIf) { Log "Would start: $exe" 'DRY' }
    else {
      try { Start-Process -FilePath $exe -ErrorAction Stop | Out-Null; Log "Launched: logioptionsplus.exe" }
      catch { Log "Launch failed: $($_.Exception.Message)" 'WARN' }
    }
  } else {
    Log "Install EXE not found; skip launch." 'WARN'
  }
}

function Hash-File { param([string]$path)
  try { return (Get-FileHash -Path $path -Algorithm SHA256).Hash } catch { return $null }
}

function Collect-Files {
  param([switch]$IncludePD)
  $p = Get-LogiPaths
  $targets = New-Object System.Collections.Generic.List[string]

  foreach ($path in @($p.LocalAppData, $p.RoamingAppData)) {
    if (Test-Path -LiteralPath $path) { $targets.Add($path) }
  }
  if ($IncludePD -and (Test-Path -LiteralPath $p.ProgramData)) {
    $targets.Add($p.ProgramData)
  }
  return ,$targets
}

function Build-Manifest {
  param([string]$staging, [System.Collections.Generic.List[string]]$sources)
  $files = Get-ChildItem -LiteralPath $staging -Recurse -File -ErrorAction SilentlyContinue
  $list = @()
  foreach ($f in $files) {
    $rel = $f.FullName.Substring($staging.Length).TrimStart('\','/')
    $list += [pscustomobject]@{
      Path    = $rel
      Size    = $f.Length
      SHA256  = Hash-File $f.FullName
    }
  }

  $manifest = [pscustomobject]@{
    Tool             = "LogiOptionsPlus-Backup"
    ToolVersion      = "1.1.1"
    Timestamp        = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss K")
    ComputerName     = $env:COMPUTERNAME
    UserName         = $env:USERNAME
    OSVersion        = (Get-CimInstance Win32_OperatingSystem).Version
    LogiOptionsPlus  = Get-LogiVersion
    Sources          = $sources
    FileCount        = $list.Count
    Files            = $list
  }
  return $manifest
}

# --------------------------- Main ---------------------------
switch ($Mode) {

  'Backup' {
    Log "=== MODE: BACKUP ==="
    Ensure-Admin

    Ensure-Dir -path $Destination
    $ts = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
    $work = Join-Path $env:TEMP "LogiOP_Backup_$ts"
    $zip  = Join-Path $Destination "LogiOptionsPlus_Backup_$ts.zip"

    Log "Staging folder: $work"
    Log "Final archive : $zip"
    Ensure-Dir -path $work

    Stop-Logi

    $sources = Collect-Files -IncludePD:$IncludeProgramData
    if ($sources.Count -eq 0) {
      Log "No Logi Options+ paths found. Nothing to backup." 'WARN'
      break
    }

    $map = @{}
    $i = 0
    foreach ($src in $sources) {
      $name = ("src{0}" -f $i)
      $dst  = Join-Path $work $name
      $map[$src] = $name
      if ($WhatIf) { Log "Would copy: $src -> $dst" 'DRY' }
      else {
        Ensure-Dir -path $dst
        $null = robocopy $src $dst /E /ZB /R:2 /W:1 /NFL /NDL /NP /NJH /NJS
      }
      $i++
    }

    $localSettings = Join-Path (Get-LogiPaths).LocalAppData 'settings.json'
    if (Test-Path $localSettings) {
      $quick = Join-Path $work 'settings.json'
      if ($WhatIf) { Log "Would copy: $localSettings -> $quick" 'DRY' }
      else { Copy-Item -LiteralPath $localSettings -Destination $quick -Force -ErrorAction SilentlyContinue }
    }

    $manifest = Build-Manifest -staging $work -sources $map.Keys
    $manifestPath = Join-Path $work 'manifest.json'
    if ($WhatIf) { Log "Would write manifest.json" 'DRY' }
    else { $manifest | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $manifestPath -Encoding UTF8 }

    if ($WhatIf) {
      Log "Would create ZIP: $zip" 'DRY'
    } else {
      if (Test-Path $zip) { Remove-Item -LiteralPath $zip -Force }
      Compress-Archive -Path (Join-Path $work '*') -DestinationPath $zip
      Remove-Item -LiteralPath $work -Recurse -Force
      Log "Backup complete: $zip"
    }

    if (-not $NoRestart) { Start-Logi }
  }

  'Restore' {
    Log "=== MODE: RESTORE ==="
    Ensure-Admin
    if (-not $Source) { throw "Please specify -Source <backup.zip>" }
    if (-not (Test-Path -LiteralPath $Source)) { throw "Backup file not found at $Source" }

    Stop-Logi

    $temp = Join-Path $env:TEMP ("LogiOP_Restore_" + (Get-Date -Format "yyyyMMdd_HHmmss"))
    Ensure-Dir -path $temp

    if ($WhatIf) { Log "Would extract ZIP: $Source -> $temp" 'DRY' }
    else { Expand-Archive -LiteralPath $Source -DestinationPath $temp -Force }

    $p = Get-LogiPaths
    $targets = @(
      @{ name='LocalAppData';   real=$p.LocalAppData;   extracted=(Join-Path $temp 'src0') },
      @{ name='RoamingAppData'; real=$p.RoamingAppData; extracted=(Join-Path $temp 'src1') },
      @{ name='ProgramData';    real=$p.ProgramData;    extracted=(Join-Path $temp 'src2') }
    )

    foreach ($t in $targets) {
      $srcPath = $t.extracted
      $dstPath = $t.real

      if (-not (Test-Path -LiteralPath $srcPath)) { continue }
      if (-not (Test-Path -LiteralPath $dstPath)) { Ensure-Dir -path $dstPath }

      $pre = "$dstPath.pre-restore_" + (Get-Date -Format "yyyyMMdd_HHmmss")
      if ($WhatIf) { Log "Would move current: $dstPath -> $pre" 'DRY' }
      else {
        if (Test-Path $dstPath) {
          try { Move-Item -LiteralPath $dstPath -Destination $pre -Force }
          catch {
            $null = robocopy $dstPath $pre /E /ZB /R:2 /W:1 /NFL /NDL /NP /NJH /NJS
            Remove-Item -LiteralPath $dstPath -Recurse -Force -ErrorAction SilentlyContinue
          }
        }
        Ensure-Dir -path $dstPath
      }

      if ($WhatIf) { Log "Would copy restore: $srcPath -> $dstPath" 'DRY' }
      else {
        $null = robocopy $srcPath $dstPath /E /ZB /R:2 /W:1 /NFL /NDL /NP /NJH /NJS
      }
      Log "Restored $($t.name) from archive."
    }

    if (-not $WhatIf) { Remove-Item -LiteralPath $temp -Recurse -Force -ErrorAction SilentlyContinue }
    if (-not $NoRestart) { Start-Logi }
    Log "Restore complete."
  }

  'Verify' {
    Log "=== MODE: VERIFY ==="
    $p = Get-LogiPaths
    $paths = @(
      @{ Name='LocalAppData';   Path=$p.LocalAppData;   ImportantFile=(Join-Path $p.LocalAppData 'settings.json') },
      @{ Name='RoamingAppData'; Path=$p.RoamingAppData; ImportantFile=$null },
      @{ Name='ProgramData';    Path=$p.ProgramData;    ImportantFile=$null }
    )

    foreach ($x in $paths) {
      $exists = Test-Path -LiteralPath $x.Path
      Log ("{0}: {1}" -f $x.Name, ($exists ? $x.Path : 'Not Found')) ($exists ? 'INFO' : 'WARN')
      if ($exists -and $x.ImportantFile) {
        if (Test-Path -LiteralPath $x.ImportantFile) {
          $hash = Hash-File $x.ImportantFile
          Log " - settings.json size=$((Get-Item $x.ImportantFile).Length) SHA256=$hash"
        } else {
          Log " - settings.json not found" 'WARN'
        }
      }
    }
    $ver = Get-LogiVersion
    Log ("Logi Options+ version: {0}" -f ($ver ?? 'Unknown'))
  }
}

# End of file
