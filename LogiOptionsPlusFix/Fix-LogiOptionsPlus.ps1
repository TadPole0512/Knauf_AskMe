<# 
Fix-LogiOptionsPlus.ps1
Version: 1.2.4
Purpose:
  Diagnose and fix "endless loading" in Logitech Logi Options+ (aka Optimizer+).
  Safe-by-default, rollback-friendly, single-file automation (no inline-if, no here-string).
#>

[CmdletBinding()]
param(
  [switch]$DiagnoseOnly,
  [ValidateSet(0,1,2,3)][int]$FixLevel = 1,
  [string]$OutDir = "$env:ProgramData\LogiOptionsPlusFix\Logs"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# ----------- Constants -----------
$ProcNames = @('LogiOptionsPlus','LogiOptionsPlusAgent','OptionsPlus','LogiOverlay')
$ServiceNames = @('LogiOptionsPlus Service','LogiBoltService','LogiOverlayService','LogiOptionsPlusAgent')
$AppPaths = @(
  "$env:LOCALAPPDATA\Programs\LogiOptionsPlus\LogiOptionsPlus.exe",
  "$env:ProgramFiles\Logi\LogiOptionsPlus\LogiOptionsPlus.exe",
  "$env:LOCALAPPDATA\Logi\LogiOptionsPlus\LogiOptionsPlus.exe"
)
$DataRoots = @(
  "$env:APPDATA\LogiOptionsPlus",
  "$env:LOCALAPPDATA\LogiOptionsPlus",
  "$env:LOCALAPPDATA\Packages\Logi*",
  "$env:PROGRAMDATA\LogiOptionsPlus"
)
$ReportTime = (Get-Date).ToString("yyyyMMdd_HHmmss")
$SessionId = [Guid]::NewGuid().ToString()
$WorkRoot = Join-Path $OutDir $ReportTime
$LogFile   = Join-Path $WorkRoot "FixLog.txt"
$ReportMd  = Join-Path $WorkRoot "FixReport.md"
$BackupZip = Join-Path $WorkRoot "LOP_Backup.zip"

# ----------- Helpers -----------
function New-Dir($p){ if(-not (Test-Path $p)){ New-Item -ItemType Directory -Path $p -Force | Out-Null } }
function Log($msg){ $stamp = (Get-Date).ToString("HH:mm:ss"); "$stamp  $msg" | Tee-Object -FilePath $LogFile -Append | Out-Null }
function Stop-Quiet($name){
  Get-Process -Name $name -ErrorAction SilentlyContinue | ForEach-Object {
    try{ $_ | Stop-Process -Force -ErrorAction Stop; Log "Killed: $($_.ProcessName) PID=$($_.Id)" }catch{}
  }
}
function Zip-Folder($source, $zipPath){
  Add-Type -AssemblyName 'System.IO.Compression.FileSystem'
  if(Test-Path $zipPath){ Remove-Item $zipPath -Force -ErrorAction SilentlyContinue }
  [System.IO.Compression.ZipFile]::CreateFromDirectory($source,$zipPath)
}
function Safe-OwnerAcl($path){
  try{ Get-Acl -Path $path -ErrorAction Stop | Out-Null; return $true }catch{
    try{
      $sid = New-Object System.Security.Principal.SecurityIdentifier "S-1-5-32-544"
      $rule = New-Object System.Security.AccessControl.FileSystemAccessRule("Administrators","FullControl","ContainerInherit, ObjectInherit","None","Allow")
      $acl  = New-Object System.Security.AccessControl.DirectorySecurity
      $acl.SetOwner($sid); $acl.AddAccessRule($rule); Set-Acl -Path $path -AclObject $acl
      return $true
    }catch{ return $false }
  }
}

# ----------- Init -----------
New-Dir $WorkRoot
"Session: $SessionId`nTime: $(Get-Date) `nUser: $env:USERNAME `n" | Out-File -FilePath $LogFile -Encoding utf8
Log "WorkRoot: $WorkRoot"

# Optional Restore Point (safe skip if disabled)
try {
  $srKey = 'HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\SystemRestore'
  if(Test-Path $srKey){
    try {
      Checkpoint-Computer -Description "LOP_Fix_$ReportTime" -RestorePointType "MODIFY_SETTINGS" | Out-Null
      Log "Restore Point created."
    } catch {
      Log "Restore Point skipped (disabled/policy): $($_.Exception.Message)"
    }
  } else {
    Log "Restore Point not available on this system."
  }
} catch { Log "Restore Point check failed: $($_.Exception.Message)" }

# ----------- Discovery / Diagnose -----------
$Diag = [ordered]@{}

# System
$sysProps = (Get-ComputerInfo -Property OsName,OsVersion,WindowsVersion)
$osString = ($sysProps.PSObject.Properties | ForEach-Object { $_.Value }) -join ' '
$build = (Get-ComputerInfo -Property OsBuildNumber).OsBuildNumber
$isAdmin = ([bool](New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())).IsInRole([Security.Principal.WindowsBuiltinRole]::Administrator))
$Diag.System = @{ OS=$osString; Build=$build; IsAdmin=$isAdmin }

# Process
$Diag.Process = @()
foreach($p in $ProcNames){
  Get-Process -Name $p -ErrorAction SilentlyContinue | ForEach-Object {
    $Diag.Process += [ordered]@{ Name=$_.ProcessName; Id=$_.Id; CPU=$_.CPU; WS_MB=[math]::Round($_.WS/1MB,1); StartTime=$_.StartTime }
  }
}

# Services
$Diag.Services = @()
foreach($s in $ServiceNames){
  $svc = Get-Service -Name $s -ErrorAction SilentlyContinue
  if($svc){
    $startMode = (Get-CimInstance Win32_Service -Filter "Name='$($svc.Name)'" -ErrorAction SilentlyContinue).StartMode
    $Diag.Services += [ordered]@{ Name=$svc.Name; Status=$svc.Status; StartType=$startMode }
  }
}

# App version
$Diag.App = @()
foreach($ap in $AppPaths){
  if(Test-Path $ap){
    $ver = "Unknown"
    try{ $ver = (Get-Item $ap).VersionInfo.ProductVersion }catch{}
    $Diag.App += [ordered]@{ Path=$ap; Version=$ver }
  }
}

# Network/Proxy (null-safe)
$proxy = Get-ItemProperty -Path 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Internet Settings' -ErrorAction SilentlyContinue
$proxyEnabled = $null
$proxyServer  = $null
if($proxy){
  if($proxy.PSObject.Properties['ProxyEnable']){ $proxyEnabled = $proxy.ProxyEnable }
  if($proxy.PSObject.Properties['ProxyServer']){ $proxyServer  = $proxy.ProxyServer }
}
$Diag.Network = [ordered]@{ ProxyEnabled=$proxyEnabled; ProxyServer=$proxyServer }

# Policies (hints)
$pol = @{}
$polKeys = @(
  'HKLM:\SOFTWARE\Policies\Microsoft\Windows\CurrentVersion\Internet Settings',
  'HKLM:\SOFTWARE\Policies\Microsoft\Windows\Safer\CodeIdentifiers',
  'HKLM:\SOFTWARE\Policies\Microsoft\Windows\Installer'
)
foreach($k in $polKeys){ $pol[$k] = Test-Path $k }
$Diag.Policies = $pol

# Event Logs (Application, recent Logi)
$ev = Get-WinEvent -LogName Application -MaxEvents 200 -ErrorAction SilentlyContinue | Where-Object {
  $_.ProviderName -like "*Logi*" -or $_.Message -like "*Logi*" -or $_.Message -like "*Options+*"
}
$Diag.EventsRecent = $ev | Select-Object TimeCreated, ProviderName, Id, LevelDisplayName, Message

# Files snapshot
$FileDiag = @()
foreach($root in $DataRoots){
  $items = Get-ChildItem -Path $root -ErrorAction SilentlyContinue -Force
  foreach($it in $items){
    $size = 0
    try{ $size = [math]::Round((Get-ChildItem $it.FullName -Recurse -Force -ErrorAction SilentlyContinue | Measure-Object -Sum Length).Sum/1MB,1) }catch{}
    $FileDiag += [ordered]@{ Path=$it.FullName; Exists=(Test-Path $it.FullName); SizeMB=$size }
  }
}
$Diag.Files = $FileDiag

# Serialize diagnostics
New-Dir $WorkRoot
$DiagPath = Join-Path $WorkRoot "Diagnostics.json"
$Diag | ConvertTo-Json -Depth 6 | Out-File -FilePath $DiagPath -Encoding utf8
Log "Diagnostics captured: $DiagPath"

# ----------- Backup (DataRoots) -----------
$tempBackup = Join-Path $WorkRoot "Backup"
New-Dir $tempBackup
foreach($root in $DataRoots){
  Get-ChildItem -Path $root -Force -ErrorAction SilentlyContinue | ForEach-Object {
    try{
      $relTarget = (Join-Path $tempBackup ($_.FullName -replace "[:\\]","_"))
      if($_.PSIsContainer){ Copy-Item $_.FullName -Destination $relTarget -Recurse -Force -ErrorAction Stop }
      else { New-Dir (Split-Path $relTarget); Copy-Item $_.FullName -Destination $relTarget -Force -ErrorAction Stop }
    }catch{ Log "Backup warn: $($_.FullName) => $($_.Exception.Message)" }
  }
}
try{ Zip-Folder -source $tempBackup -zipPath $BackupZip; Log "Backup zip: $BackupZip" }catch{ Log "Backup zip failed: $($_.Exception.Message)" }

# ----------- Fix pipeline -----------
if($DiagnoseOnly){
  $FixSummary = "진단만 수행. 백업: $BackupZip"
}else{
  Log "FixLevel = $FixLevel"

  # Stop procs
  foreach($n in $ProcNames){ Stop-Quiet $n }

  # Stop services (best-effort)
  foreach($s in $ServiceNames){
    try{
      $svc = Get-Service -Name $s -ErrorAction SilentlyContinue
      if($svc -and $svc.Status -ne 'Stopped'){ Stop-Service -Name $s -Force -ErrorAction SilentlyContinue; Start-Sleep -Seconds 2; Log "Stopped service: $s" }
    }catch{}
  }

  if($FixLevel -ge 1){
    # L1: clear volatile caches
    foreach($root in $DataRoots){
      foreach($cacheName in @('Cache','GPUCache','Code Cache','Network')){
        $target = Join-Path $root $cacheName
        Get-ChildItem -Path $target -Recurse -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
      }
    }
    Log "L1: Core caches cleared."

    # restart services
    foreach($s in $ServiceNames){
      try{ $svc = Get-Service -Name $s -ErrorAction SilentlyContinue; if($svc){ Start-Service -Name $s -ErrorAction SilentlyContinue; Log "Started service: $s" } }catch{}
    }
  }

  if($FixLevel -ge 2){
    # L2: Reset IndexedDB/Local Storage + ACL + locks
    foreach($root in $DataRoots){
      foreach($name in @('IndexedDB','Local Storage','blob_storage','Session Storage')){
        $target = Join-Path $root $name
        if(Test-Path $target){
          Safe-OwnerAcl $target | Out-Null
          Get-ChildItem -Path $target -Recurse -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
          Log "L2: Reset $target"
        }
      }
      Get-ChildItem -Path $root -Recurse -Force -Filter "*.lock" -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
    }
    # Roaming json states
    Get-ChildItem -Path "$env:APPDATA\LogiOptionsPlus\*" -Filter "*.json" -ErrorAction SilentlyContinue | ForEach-Object {
      if($_.Name -match 'state|session|cache|prefs'){ Remove-Item $_.FullName -Force -ErrorAction SilentlyContinue; Log "L2: Remove json state: $($_.Name)" }
    }
    # Legacy autorun
    foreach($rk in @('HKCU:\Software\Microsoft\Windows\CurrentVersion\Run','HKLM:\Software\Microsoft\Windows\CurrentVersion\Run')){
      Get-ItemProperty -Path $rk -ErrorAction SilentlyContinue | ForEach-Object {
        $_.PSObject.Properties | Where-Object { $_.Name -match 'Logitech Options' } | ForEach-Object {
          try{ Remove-ItemProperty -Path $rk -Name $_.Name -Force -ErrorAction Stop; Log "Removed legacy autorun: $($_.Name)" }catch{}
        }
      }
    }
    # WinHTTP proxy reset (safe)
    try{ netsh winhttp reset proxy | Out-Null; Log "WinHTTP proxy reset." }catch{}
  }

  if($FixLevel -ge 3){
    # L3: Reinstall via winget
    $winget = Get-Command winget -ErrorAction SilentlyContinue
    if($winget){
      try{
        Log "L3: winget upgrade Logitech.OptionsPlus"
        winget upgrade --id "Logitech.OptionsPlus" --silent --accept-source-agreements --accept-package-agreements | Tee-Object -FilePath $LogFile -Append | Out-Null
      }catch{
        try{
          Log "L3: winget install Logitech.OptionsPlus"
          winget install --id "Logitech.OptionsPlus" --silent --accept-source-agreements --accept-package-agreements | Tee-Object -FilePath $LogFile -Append | Out-Null
        }catch{ Log "Winget reinstall failed: $($_.Exception.Message)" }
      }
    }else{ Log "winget not available; skip reinstall." }
  }

  # Relaunch
  $exe = $AppPaths | Where-Object { Test-Path $_ } | Select-Object -First 1
  if($exe){ Start-Process -FilePath $exe; Log "App relaunched: $exe" } else { Log "App not found to relaunch." }

  # Fix summary text (no inline-if)
  $fixSuffix = ""
  if($FixLevel -ge 2){ $fixSuffix += " + 심화 초기화" }
  if($FixLevel -ge 3){ $fixSuffix += " + 재설치 시도" }
  $FixSummary = "FixLevel $FixLevel 완료. 캐시 초기화 및 서비스 재시작 수행$fixSuffix"
}

# ----------- Report (no inline-if; no here-strings) -----------
$recentEvents = ($Diag.EventsRecent | Select-Object -First 10 | ForEach-Object {
  "- [$($_.TimeCreated)] $($_.ProviderName) #$($_.Id) $($_.LevelDisplayName) : " + ($_.Message -replace "`r|`n"," ")
}) -join "`n"

$dataRootsTop = ($Diag.Files | Sort-Object -Property SizeMB -Descending | Select-Object -First 10 | ForEach-Object {
  "- $($_.Path) (Exists=$($_.Exists)) Size=$($_.SizeMB)MB"
}) -join "`n"

$servicesList = ($Diag.Services | ForEach-Object {
  "- $($_.Name): $($_.Status) (StartType: $($_.StartType))"
}) -join "`n"

# app list no inline-if
$appList = "- Not Found"
if($Diag.App.Count -gt 0){
  $appLines = @()
  foreach($a in $Diag.App){
    $appLines += ("- Path: {0}`n  - Version: {1}" -f $a.Path, $a.Version)
  }
  $appList = ($appLines -join "`n")
}

$policyList = ($Diag.Policies.GetEnumerator() | ForEach-Object {
  "- PolicyKey: $($_.Key) => Exists=$($_.Value)"
}) -join "`n"

$modeStr = 'Fix'
if($DiagnoseOnly){ $modeStr = 'DiagnoseOnly' }

$lines = @()
$lines += "# Logi Options+ Endless Loading - Diagnose & Fix Report"
$lines += "- Session: $SessionId"
$lines += "- Time: $(Get-Date)"
$lines += "- Mode: $modeStr"
$lines += "- FixLevel: $FixLevel"
$lines += ""
$lines += "## System"
$lines += "- OS: $($Diag.System.OS)"
$lines += "- Build: $($Diag.System.Build)"
$lines += "- Admin: $($Diag.System.IsAdmin)"
$lines += ""
$lines += "## App"
$lines += "$appList"
$lines += ""
$lines += "## Services"
$lines += "$servicesList"
$lines += ""
$lines += "## Network & Policy Hints"
$lines += "- ProxyEnabled: $($Diag.Network.ProxyEnabled)"
$lines += "- ProxyServer : $($Diag.Network.ProxyServer)"
$lines += "$policyList"
$lines += ""
$lines += "## Recent App Events (Top 10)"
$lines += "$recentEvents"
$lines += ""
$lines += "## Data Roots Snapshot"
$lines += "$dataRootsTop"
$lines += ""
$lines += "## Summary"
$lines += "- $FixSummary"
$lines += "- Backup: $BackupZip"
$lines += "- Logs: $LogFile"
$lines += ""
$lines += "## Next Steps (if still looping)"
$lines += "1) 회사 네트워크/보안 정책 간섭 가능성 점검(프록시/SSL 인스펙션/EDR)."
$lines += "2) FixLevel 2로 재시도 후, 3(재설치) 시도."
$lines += "3) 보안/IT팀 문의 포인트:"
$lines += "   - 초기 통신 차단 여부(`logi*`, `options+`, Electron 앱 초기화)"
$lines += "   - 사용자 Proxy vs WinHTTP Proxy 차이, SSL MITM 여부"
$lines += "4) Application 이벤트 로그에서 `Options+`, `Logi` 오류ID 상세"

$md = $lines -join "`r`n"
$md | Out-File -FilePath $ReportMd -Encoding utf8
Log "Report written: $ReportMd"

Write-Host "`nDone. Report: $ReportMd`nLog: $LogFile`n"
