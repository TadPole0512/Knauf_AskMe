#Requires -Version 5.1
param(
    [string]$BackupRootPath = "C:\Tools\backup\set_env",
    [switch]$OpenBackupFolder
)
$ErrorActionPreference = "Stop"
$BackupSources = @{
    "VSCode" = @{
        Enabled = $true
        Paths = @(
            @{ Source = "$env:APPDATA\Code\User\settings.json"; Destination = "VSCode\" }
            @{ Source = "$env:APPDATA\Code\User\keybindings.json"; Destination = "VSCode\" }
            @{ Source = "$env:APPDATA\Code\User\globalStorage\alefragnani.project-manager"; Destination = "VSCode\" ; IsDirectory = $true }
            @{ Source = "$env:APPDATA\Code\User\snippets"; Destination = "VSCode\"; IsDirectory = $true }
        )
    }
    "Notepad++" = @{
        Enabled = $true
        Paths = @(
            @{ Source = "$env:APPDATA\Notepad++\config.xml"; Destination = "Notepad++\" }
            @{ Source = "$env:APPDATA\Notepad++\shortcuts.xml"; Destination = "Notepad++\" }
            @{ Source = "$env:APPDATA\Notepad++\stylers.xml"; Destination = "Notepad++\" }
            @{ Source = "$env:APPDATA\Notepad++\langs.xml"; Destination = "Notepad++\" }
        )
    }
    "TotalCommander" = @{
        Enabled = $true
        Paths = @(
            @{ Source = "$env:APPDATA\GHISLER\wincmd.ini"; Destination = "TotalCommander\" }
            @{ Source = "$env:APPDATA\GHISLER\wcx_ftp.ini"; Destination = "TotalCommander\" }
        )
    }
    "DBeaver" = @{
        Enabled = $true
        Paths = @(
            @{ Source = "$env:APPDATA\DBeaverData\workspace6\General\.dbeaver"; Destination = "DBeaver\"; IsDirectory = $true }
        )
    }
    "Chrome" = @{
        Enabled = $true
        Paths = @(
            @{ Source = "$env:LOCALAPPDATA\Google\Chrome\User Data\Default\Bookmarks"; Destination = "Chrome\" }
            @{ Source = "$env:LOCALAPPDATA\Google\Chrome\User Data\Default\Preferences"; Destination = "Chrome\" }
        )
    }
    "Edge" = @{
        Enabled = $true
        Paths = @(
            @{ Source = "$env:LOCALAPPDATA\Microsoft\Edge\User Data\Default\Bookmarks"; Destination = "Edge\" }
            @{ Source = "$env:LOCALAPPDATA\Microsoft\Edge\User Data\Default\Preferences"; Destination = "Edge\" }
        )
    }
}
function Get-UniqueFolderName {
    param([string]$BasePath, [string]$FolderName)
    $targetPath = Join-Path $BasePath $FolderName
    if (-not (Test-Path $targetPath)) { return $targetPath }
    $counter = 1
    do {
        $targetPath = Join-Path $BasePath "$FolderName ($counter)"
        $counter++
    } while (Test-Path $targetPath)
    return $targetPath
}
Write-Host "🔧 환경 설정 백업 시작" -ForegroundColor Cyan
if (-not (Test-Path $BackupRootPath)) {
    New-Item -Path $BackupRootPath -ItemType Directory -Force | Out-Null
}
$todayFolder = Get-Date -Format "yyyy-MM-dd"
$backupFolder = Get-UniqueFolderName -BasePath $BackupRootPath -FolderName $todayFolder
New-Item -Path $backupFolder -ItemType Directory -Force | Out-Null
Write-Host "📂 백업 위치: $backupFolder`n" -ForegroundColor Green
$stats = @{ Total = 0; Success = 0; Skipped = 0 }
foreach ($appName in $BackupSources.Keys) {
    $appConfig = $BackupSources[$appName]
    Write-Host "━━━ 📦 $appName" -ForegroundColor Magenta
    foreach ($pathInfo in $appConfig.Paths) {
        $stats.Total++
        $sourcePath = $pathInfo.Source
        $fileName = Split-Path $sourcePath -Leaf
        if (-not (Test-Path $sourcePath)) {
            $stats.Skipped++
            Write-Host "  ⏭️  $fileName (없음)" -ForegroundColor DarkGray
            continue
        }
        $destPath = Join-Path $backupFolder $pathInfo.Destination
        if (-not (Test-Path $destPath)) {
            New-Item -Path $destPath -ItemType Directory -Force | Out-Null
        }
        $fullDestPath = Join-Path $destPath $fileName
        if ($pathInfo.IsDirectory) {
            Copy-Item -Path $sourcePath -Destination $fullDestPath -Recurse -Force
        } else {
            Copy-Item -Path $sourcePath -Destination $fullDestPath -Force
        }
        $stats.Success++
        Write-Host "  ✅ $fileName" -ForegroundColor Green
    }
}
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "📊 백업 완료 통계" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "총 항목:  $($stats.Total)" -ForegroundColor White
Write-Host "성공:     $($stats.Success)" -ForegroundColor Green
Write-Host "건너뜀:   $($stats.Skipped)" -ForegroundColor Yellow
Write-Host "`n📁 백업 위치: $backupFolder" -ForegroundColor Cyan
if ($OpenBackupFolder) {
    Start-Process explorer.exe -ArgumentList $backupFolder
}
Write-Host "`n✅ 백업이 완료되었습니다!" -ForegroundColor Green
