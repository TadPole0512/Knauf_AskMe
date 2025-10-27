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
        )
    }
    "Chrome" = @{
        Enabled = $true
        Paths = @(
            @{ Source = "$env:LOCALAPPDATA\Google\Chrome\User Data\Default\Bookmarks"; Destination = "Chrome\" }
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
Write-Host " 백업 시작" -ForegroundColor Cyan
if (-not (Test-Path $BackupRootPath)) {
    New-Item -Path $BackupRootPath -ItemType Directory -Force | Out-Null
}
$todayFolder = Get-Date -Format "yyyy-MM-dd"
$backupFolder = Get-UniqueFolderName -BasePath $BackupRootPath -FolderName $todayFolder
New-Item -Path $backupFolder -ItemType Directory -Force | Out-Null
Write-Host " 백업: $backupFolder" -ForegroundColor Green
$stats = @{ Total = 0; Success = 0; Skipped = 0 }
foreach ($appName in $BackupSources.Keys) {
    $appConfig = $BackupSources[$appName]
    Write-Host "`n $appName" -ForegroundColor Magenta
    foreach ($pathInfo in $appConfig.Paths) {
        $stats.Total++
        $sourcePath = $pathInfo.Source
        if (-not (Test-Path $sourcePath)) {
            $stats.Skipped++
            Write-Host "   $(Split-Path $sourcePath -Leaf) (없음)" -ForegroundColor DarkGray
            continue
        }
        $destPath = Join-Path $backupFolder $pathInfo.Destination
        if (-not (Test-Path $destPath)) {
            New-Item -Path $destPath -ItemType Directory -Force | Out-Null
        }
        $fileName = Split-Path $sourcePath -Leaf
        $fullDestPath = Join-Path $destPath $fileName
        Copy-Item -Path $sourcePath -Destination $fullDestPath -Force
        $stats.Success++
        Write-Host "   $fileName" -ForegroundColor Green
    }
}
Write-Host "`n" -ForegroundColor Cyan
Write-Host "성공: $($stats.Success) / 총: $($stats.Total)" -ForegroundColor Green
if ($OpenBackupFolder) {
    Start-Process explorer.exe -ArgumentList $backupFolder
}
Write-Host "`n 완료!" -ForegroundColor Green
