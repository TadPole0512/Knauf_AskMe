<#
  Backup-env_set-GPT.ps1 (v3 - StrictMode Safe + Path.GetDirectoryName)
  - VSCode, Notepad++, Total Commander, DBeaver, Chrome, Edge, Comet* 백업
  - 날짜 폴더 중복 시 (1),(2)... 자동
  - summary.txt / summary.json 생성
#>
param(
    [switch]$OpenAfter
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# -----------------------
# 0) 공통 유틸
# -----------------------
function New-StampedFolderWithDupeSuffix {
    param(
        [Parameter(Mandatory)][string]$BaseDir,
        [Parameter(Mandatory)][string]$Stamp
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
    if ($Path -and -not (Test-Path -LiteralPath $Path)) {
        New-Item -ItemType Directory -Path $Path | Out-Null
    }
}

function Ensure-ParentDir {
    param([string]$ChildPath)
    $parent = [System.IO.Path]::GetDirectoryName($ChildPath)
    if ($parent) { Ensure-Dir $parent }
}

function SafeCopy {
    <#
      폴더/파일 모두 지원. 폴더는 robocopy(잠김/긴경로 내성) 사용.
      Return: @{ Status='OK'|'Missing'|'Error'; Source; Dest; Note }
    #>
    param(
        [Parameter(Mandatory)][string]$Source,
        [Parameter(Mandatory)][string]$Dest
    )
    $result = [ordered]@{ Status=''; Source=$Source; Dest=$Dest; Note='' }
    try {
        if (-not (Test-Path -LiteralPath $Source)) {
            $result.Status = 'Missing'
            $result.Note   = 'Source not found'
            return $result
        }
        Ensure-ParentDir -ChildPath $Dest
        $srcIsDir = (Test-Path -LiteralPath $Source -PathType Container)
        if ($srcIsDir) {
            $args = @("`"$Source`"", "`"$Dest`"", "/E", "/COPY:DAT", "/R:2", "/W:2", "/NFL", "/NDL", "/NP", "/XJ")
            $rc = Start-Process -FilePath robocopy.exe -ArgumentList $args -NoNewWindow -PassThru -Wait
            if ($rc.ExitCode -le 1) { $result.Status = 'OK' }
            else { $result.Status = 'Error'; $result.Note = "Robocopy exit code: $($rc.ExitCode)" }
        } else {
            Copy-Item -LiteralPath $Source -Destination $Dest -Force -ErrorAction Stop
            $result.Status = 'OK'
        }
    } catch {
        $result.Status = 'Error'
        $result.Note   = $_.Exception.Message
    }
    return $result
}

function Add-Log {
    param(
        [Parameter(Mandatory)][System.Collections.IList]$List,  # IList로 완화
        [Parameter(Mandatory)][hashtable]$Item
    )
    [void]$List.Add($Item)
}

# -----------------------
# 1) 백업 루트 & 날짜 폴더
# -----------------------
$now       = Get-Date
$stamp     = $now.ToString('yyyy-MM-dd')
$rootDir   = 'C:\Tools\backup\set_env'
$backupDir = New-StampedFolderWithDupeSuffix -BaseDir $rootDir -Stamp $stamp

# -----------------------
# 2) 작업 리스트 초기화(StrictMode 안전)
# -----------------------
$tasksVar = Get-Variable -Name tasks -Scope Script -ErrorAction SilentlyContinue
if (-not $tasksVar) {
    Set-Variable -Name tasks -Scope Script -Value ([System.Collections.Generic.List[hashtable]]::new())
}
$tasks = $script:tasks

# -----------------------
# 3) 경로 수집
# -----------------------
$envUserProfile = $env:USERPROFILE
$envAppData     = $env:APPDATA      # Roaming
$envLocalApp    = $env:LOCALAPPDATA # Local

# --- VSCode
$vscodeUser = Join-Path $envAppData 'Code\User'
Add-Log $tasks @{ App='VSCode'; What='UserSettings'; Source=$vscodeUser; Dest=(Join-Path $backupDir 'VSCode\User'); Type='Dir' }

# 확장 목록 저장
$extListFile = Join-Path $backupDir 'VSCode\vscode-extensions.txt'
Ensure-ParentDir $extListFile
try {
    $exts = & code --list-extensions 2>$null
    if ($LASTEXITCODE -eq 0 -and $exts) {
        $exts | Sort-Object | Set-Content -Encoding UTF8 -LiteralPath $extListFile
    } else {
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
# 확장 폴더 스냅샷
$extDir = Join-Path $envUserProfile '.vscode\extensions'
Add-Log $tasks @{ App='VSCode'; What='ExtensionsFolder'; Source=$extDir; Dest=(Join-Path $backupDir 'VSCode\extensions'); Type='Dir' }

# --- Notepad++
$npDir = Join-Path $envAppData 'Notepad++'
Add-Log $tasks @{ App='Notepad++'; What='ConfigFolder'; Source=$npDir; Dest=(Join-Path $backupDir 'Notepad++'); Type='Dir' }
# 레지스트리 백업
$npRegFile = Join-Path $backupDir 'Notepad++\NotepadPP_HKCU.reg'
try {
    Ensure-ParentDir $npRegFile
    $null = reg.exe query "HKCU\Software\Notepad++" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Start-Process reg.exe -ArgumentList @('export','HKCU\Software\Notepad++',"`"$npRegFile`"","/y") -NoNewWindow -Wait | Out-Null
    }
} catch { }

# --- Total Commander
$tcReg = "HKCU:\Software\Ghisler"
$wincmdIni = $null; $ftpIni = $null
try {
    if (Test-Path $tcReg) {
        $wincmdIni = (Get-ItemProperty $tcReg).IniFileName  -as [string]
        $ftpIni    = (Get-ItemProperty $tcReg).FtpsIniName  -as [string]
    }
} catch { }
$tcAppData = Join-Path $envAppData 'GHISLER'
Add-Log $tasks @{ App='TotalCommander'; What='AppDataFolder'; Source=$tcAppData; Dest=(Join-Path $backupDir 'TotalCommander\AppData'); Type='Dir' }
if ($wincmdIni) { Add-Log $tasks @{ App='TotalCommander'; What='wincmd.ini';  Source=$wincmdIni; Dest=(Join-Path $backupDir 'TotalCommander\wincmd.ini');   Type='File' } }
if ($ftpIni)    { Add-Log $tasks @{ App='TotalCommander'; What='wcx_ftp.ini'; Source=$ftpIni;    Dest=(Join-Path $backupDir 'TotalCommander\wcx_ftp.ini'); Type='File' } }
# 레지스트리 백업
$tcRegFile = Join-Path $backupDir 'TotalCommander\Ghisler_HKCU.reg'
try {
    Ensure-ParentDir $tcRegFile
    $null = reg.exe query "HKCU\Software\Ghisler" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Start-Process reg.exe -ArgumentList @('export','HKCU\Software\Ghisler',"`"$tcRegFile`"","/y") -NoNewWindow -Wait | Out-Null
    }
} catch { }

# --- DBeaver (신/구)
$dbeaverNew = Join-Path $envAppData 'DBeaverData'
$dbeaverOld = Join-Path $envUserProfile '.dbeaver'
Add-Log $tasks @{ App='DBeaver'; What='DBeaverData(Roaming)'; Source=$dbeaverNew; Dest=(Join-Path $backupDir 'DBeaver\DBeaverData'); Type='Dir' }
Add-Log $tasks @{ App='DBeaver'; What='.dbeaver(Legacy)';     Source=$dbeaverOld; Dest=(Join-Path $backupDir 'DBeaver\.dbeaver');   Type='Dir' }

# --- Chrome
$chromeUserData = Join-Path $envLocalApp 'Google\Chrome\User Data'
if (Test-Path $chromeUserData) {
    $profiles = Get-ChildItem $chromeUserData -Directory | Where-Object { $_.Name -in @('Default') -or $_.Name -like 'Profile *' }
    foreach ($p in $profiles) {
        $dest = Join-Path $backupDir ("Chrome\{0}" -f $p.Name)
        foreach ($f in @('Bookmarks','Bookmarks.bak','Preferences')) {
            Add-Log $tasks @{ App='Chrome'; What=$f; Source=(Join-Path $p.FullName $f); Dest=(Join-Path $dest $f); Type='File' }
        }
    }
    Add-Log $tasks @{ App='Chrome'; What='Local State'; Source=(Join-Path $chromeUserData 'Local State'); Dest=(Join-Path $backupDir 'Chrome\Local State'); Type='File' }
}

# --- Edge
$edgeUserData = Join-Path $envLocalApp 'Microsoft\Edge\User Data'
if (Test-Path $edgeUserData) {
    $profiles = Get-ChildItem $edgeUserData -Directory | Where-Object { $_.Name -in @('Default') -or $_.Name -like 'Profile *' }
    foreach ($p in $profiles) {
        $dest = Join-Path $backupDir ("Edge\{0}" -f $p.Name)
        foreach ($f in @('Bookmarks','Bookmarks.bak','Preferences')) {
            Add-Log $tasks @{ App='Edge'; What=$f; Source=(Join-Path $p.FullName $f); Dest=(Join-Path $dest $f); Type='File' }
        }
    }
    Add-Log $tasks @{ App='Edge'; What='Local State'; Source=(Join-Path $edgeUserData 'Local State'); Dest=(Join-Path $backupDir 'Edge\Local State'); Type='File' }
}

# --- Comet* (Roaming/Local 자동 탐지)
foreach ($base in @($envAppData, $envLocalApp)) {
    if (Test-Path $base) {
        Get-ChildItem $base -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like 'Comet*' } |
            ForEach-Object {
                Add-Log $tasks @{ App='Comet*'; What=$_.Name; Source=$_.FullName; Dest=(Join-Path $backupDir ("Comet\{0}" -f $_.Name)); Type='Dir' }
            }
    }
}

# -----------------------
# 4) 복사 실행
# -----------------------
$report = [System.Collections.Generic.List[hashtable]]::new()
foreach ($t in $tasks) {
    $destPath = $t.Dest
    if ($t.Type -eq 'Dir') {
        $res = SafeCopy -Source $t.Source -Dest $destPath
    } else {
        Ensure-ParentDir -ChildPath $destPath
        $res = SafeCopy -Source $t.Source -Dest $destPath
    }
    $entry = [ordered]@{
        App=$t.App; What=$t.What; Type=$t.Type; Source=$res.Source; Dest=$res.Dest; Status=$res.Status; Note=$res.Note
    }
    [void]$report.Add($entry)
}

# -----------------------
# 5) 요약 리포트 저장
# -----------------------
$summaryTxt  = Join-Path $backupDir 'summary.txt'
$summaryJson = Join-Path $backupDir 'summary.json'

"Backup Date: $($now.ToString('yyyy-MM-dd HH:mm:ss'))" | Out-File -Encoding UTF8 -FilePath $summaryTxt
"Backup Folder: $backupDir"                              | Out-File -Encoding UTF8 -FilePath $summaryTxt -Append
"="*60                                                  | Out-File -Encoding UTF8 -FilePath $summaryTxt -Append

# ✅ 인라인 if 대신 변수 사용(PS 5.1/7 모두 안전)
$lines = foreach ($row in ($report | Sort-Object App, What)) {
    $note =
        if ([string]::IsNullOrWhiteSpace($row.Note)) {
            ""
        } else {
            "($($row.Note))"
        }
    "{0,-15} | {1,-22} | {2,-5} | {3} -> {4} | {5} {6}" -f `
        $row.App, $row.What, $row.Type, $row.Source, $row.Dest, $row.Status, $note
}
$lines | Out-File -Encoding UTF8 -FilePath $summaryTxt -Append

$report | ConvertTo-Json -Depth 6 | Out-File -Encoding UTF8 -FilePath $summaryJson

Write-Host "✅ Backup complete: $backupDir"
Write-Host "   - summary.txt / summary.json 생성됨"

if ($OpenAfter) {
    try { Start-Process explorer.exe $backupDir } catch {}
}
