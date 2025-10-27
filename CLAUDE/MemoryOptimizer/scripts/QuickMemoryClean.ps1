#Requires -Version 5.1
#Requires -RunAsAdministrator

<#
.SYNOPSIS
    Windows 메모리 긴급 정리 스크립트
.DESCRIPTION
    16GB RAM 환경에서 메모리 90%→70% 수준으로 즉시 정리
    - 임시 파일 삭제
    - 브라우저 캐시 정리
    - 시스템 캐시 플러시
    - Windows 업데이트 캐시 정리
    - 비필수 프로세스 메모리 압축
.NOTES
    Version: 2.0
    Author: Memory Optimizer
    Date: 2025-10-15
#>

[CmdletBinding()]
param(
    [switch]$SkipBrowserClean,
    [switch]$SkipWindowsUpdate,
    [switch]$Verbose
)

$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

# ============================================================
# 헬퍼 함수
# ============================================================

function Get-MemoryUsage {
    $os = Get-CimInstance Win32_OperatingSystem
    $total = $os.TotalVisibleMemorySize / 1MB
    $free = $os.FreePhysicalMemory / 1MB
    $used = $total - $free
    $percent = [math]::Round(($used / $total) * 100, 1)
    
    return @{
        TotalGB = [math]::Round($total / 1024, 2)
        UsedGB = [math]::Round($used / 1024, 2)
        FreeGB = [math]::Round($free / 1024, 2)
        Percent = $percent
    }
}

function Write-StepHeader {
    param([string]$Message)
    Write-Host "`n[$(Get-Date -Format 'HH:mm:ss')] $Message" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message, [string]$Detail = "")
    Write-Host "  ✓ $Message" -ForegroundColor Green
    if ($Detail) { Write-Host "    $Detail" -ForegroundColor Gray }
}

function Write-Warning {
    param([string]$Message)
    Write-Host "  ⚠ $Message" -ForegroundColor Yellow
}

function Write-Info {
    param([string]$Message)
    Write-Host "    $Message" -ForegroundColor Gray
}

function Get-FolderSize {
    param([string]$Path)
    if (Test-Path $Path) {
        $size = (Get-ChildItem $Path -Recurse -ErrorAction SilentlyContinue | 
                 Measure-Object -Property Length -Sum -ErrorAction SilentlyContinue).Sum
        return [math]::Round($size / 1MB, 2)
    }
    return 0
}

function Remove-FolderContents {
    param(
        [string]$Path,
        [string]$Description
    )
    
    if (-not (Test-Path $Path)) {
        Write-Warning "$Description 경로 없음: $Path"
        return 0
    }
    
    $sizeBefore = Get-FolderSize $Path
    
    try {
        Get-ChildItem $Path -Recurse -Force -ErrorAction SilentlyContinue | 
            Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
        
        $sizeAfter = Get-FolderSize $Path
        $cleaned = $sizeBefore - $sizeAfter
        
        if ($cleaned -gt 0) {
            Write-Success "$Description 정리 완료" "$([math]::Round($cleaned, 2)) MB 확보"
        }
        return $cleaned
    }
    catch {
        Write-Warning "$Description 정리 중 오류: $($_.Exception.Message)"
        return 0
    }
}

# ============================================================
# 메인 로직
# ============================================================

Write-Host @"

╔══════════════════════════════════════════════════════════╗
║         Windows 메모리 긴급 정리 스크립트 v2.0          ║
║                  Memory Optimizer                        ║
╚══════════════════════════════════════════════════════════╝

"@ -ForegroundColor White

# 초기 메모리 상태
$memBefore = Get-MemoryUsage
Write-Host "📊 시작 시 메모리 상태" -ForegroundColor Yellow
Write-Host "   전체: $($memBefore.TotalGB) GB" -ForegroundColor White
Write-Host "   사용: $($memBefore.UsedGB) GB ($($memBefore.Percent)%)" -ForegroundColor $(if ($memBefore.Percent -ge 90) { "Red" } elseif ($memBefore.Percent -ge 85) { "Yellow" } else { "Green" })
Write-Host "   여유: $($memBefore.FreeGB) GB`n" -ForegroundColor White

$totalCleaned = 0

# ============================================================
# Step 1: Windows 임시 파일
# ============================================================
Write-StepHeader "🗑️  Step 1: Windows 임시 파일 삭제"

$tempPaths = @(
    @{ Path = $env:TEMP; Name = "사용자 Temp" },
    @{ Path = "C:\Windows\Temp"; Name = "시스템 Temp" },
    @{ Path = "C:\Windows\Prefetch"; Name = "Prefetch 캐시" }
)

foreach ($item in $tempPaths) {
    $cleaned = Remove-FolderContents -Path $item.Path -Description $item.Name
    $totalCleaned += $cleaned
}

# ============================================================
# Step 2: 브라우저 캐시
# ============================================================
if (-not $SkipBrowserClean) {
    Write-StepHeader "🌐 Step 2: 브라우저 캐시 정리"
    
    # Chrome 종료 및 캐시 삭제
    $chromeProcs = Get-Process chrome -ErrorAction SilentlyContinue
    if ($chromeProcs) {
        Write-Info "Chrome 프로세스 종료 중... ($($chromeProcs.Count)개)"
        Stop-Process -Name chrome -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
    }
    
    $chromeCachePaths = @(
        "$env:LOCALAPPDATA\Google\Chrome\User Data\Default\Cache",
        "$env:LOCALAPPDATA\Google\Chrome\User Data\Default\Code Cache",
        "$env:LOCALAPPDATA\Google\Chrome\User Data\ShaderCache"
    )
    
    foreach ($path in $chromeCachePaths) {
        $cleaned = Remove-FolderContents -Path $path -Description "Chrome 캐시"
        $totalCleaned += $cleaned
    }
    
    # Edge 종료 및 캐시 삭제
    $edgeProcs = Get-Process msedge -ErrorAction SilentlyContinue
    if ($edgeProcs) {
        Write-Info "Edge 프로세스 종료 중... ($($edgeProcs.Count)개)"
        Stop-Process -Name msedge -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
    }
    
    $edgeCachePaths = @(
        "$env:LOCALAPPDATA\Microsoft\Edge\User Data\Default\Cache",
        "$env:LOCALAPPDATA\Microsoft\Edge\User Data\Default\Code Cache"
    )
    
    foreach ($path in $edgeCachePaths) {
        $cleaned = Remove-FolderContents -Path $path -Description "Edge 캐시"
        $totalCleaned += $cleaned
    }
}
else {
    Write-StepHeader "🌐 Step 2: 브라우저 캐시 정리 [SKIPPED]"
}

# ============================================================
# Step 3: Windows 업데이트 캐시
# ============================================================
if (-not $SkipWindowsUpdate) {
    Write-StepHeader "🔄 Step 3: Windows Update 캐시 정리"
    
    try {
        $wuService = Get-Service wuauserv -ErrorAction Stop
        
        if ($wuService.Status -eq 'Running') {
            Write-Info "Windows Update 서비스 중지 중..."
            Stop-Service wuauserv -Force -ErrorAction Stop
            Start-Sleep -Seconds 2
        }
        
        $cleaned = Remove-FolderContents -Path "C:\Windows\SoftwareDistribution\Download" -Description "Windows Update 캐시"
        $totalCleaned += $cleaned
        
        Write-Info "Windows Update 서비스 재시작 중..."
        Start-Service wuauserv -ErrorAction SilentlyContinue
        Write-Success "Windows Update 서비스 복구 완료"
    }
    catch {
        Write-Warning "Windows Update 정리 중 오류: $($_.Exception.Message)"
    }
}
else {
    Write-StepHeader "🔄 Step 3: Windows Update 캐시 정리 [SKIPPED]"
}

# ============================================================
# Step 4: 시스템 캐시 플러시
# ============================================================
Write-StepHeader "💾 Step 4: 시스템 캐시 플러시"

try {
    # 휴지통 비우기
    Write-Info "휴지통 비우는 중..."
    Clear-RecycleBin -Force -ErrorAction SilentlyContinue
    Write-Success "휴지통 비우기 완료"
    
    # .NET 가비지 수집
    Write-Info ".NET 메모리 수집 중..."
    [System.GC]::Collect()
    [System.GC]::WaitForPendingFinalizers()
    [System.GC]::Collect()
    Write-Success ".NET 가비지 수집 완료"
    
    # DNS 캐시 플러시
    Write-Info "DNS 캐시 플러시 중..."
    ipconfig /flushdns | Out-Null
    Write-Success "DNS 캐시 플러시 완료"
}
catch {
    Write-Warning "시스템 캐시 플러시 중 오류: $($_.Exception.Message)"
}

# ============================================================
# Step 5: 작업 메모리 최적화
# ============================================================
Write-StepHeader "⚡ Step 5: 프로세스 메모리 최적화"

try {
    # 비필수 서비스 메모리 압축 (EmptyWorkingSet)
    $processes = Get-Process | Where-Object {
        $_.WorkingSet64 -gt 100MB -and
        $_.ProcessName -notin @('chrome', 'msedge', 'vscode', 'idea64', 'Teams', 'explorer', 'dwm')
    } | Sort-Object WorkingSet64 -Descending | Select-Object -First 10
    
    $optimizedCount = 0
    foreach ($proc in $processes) {
        try {
            $beforeMB = [math]::Round($proc.WorkingSet64 / 1MB, 2)
            $proc.ProcessorAffinity = $proc.ProcessorAffinity
            
            # EmptyWorkingSet API 호출 (메모리 압축)
            $source = @"
using System;
using System.Runtime.InteropServices;
public class MemoryOptimizer {
    [DllImport("psapi.dll")]
    public static extern int EmptyWorkingSet(IntPtr hwProc);
}
"@
            if (-not ([System.Management.Automation.PSTypeName]'MemoryOptimizer').Type) {
                Add-Type -TypeDefinition $source -ErrorAction SilentlyContinue
            }
            
            [MemoryOptimizer]::EmptyWorkingSet($proc.Handle) | Out-Null
            
            $proc.Refresh()
            $afterMB = [math]::Round($proc.WorkingSet64 / 1MB, 2)
            $saved = $beforeMB - $afterMB
            
            if ($saved -gt 0) {
                Write-Success "$($proc.ProcessName) 최적화" "$([math]::Round($saved, 2)) MB 절약"
                $optimizedCount++
            }
        }
        catch {
            # 액세스 거부 등 예외 무시
        }
    }
    
    if ($optimizedCount -eq 0) {
        Write-Info "최적화 가능한 프로세스 없음"
    }
}
catch {
    Write-Warning "프로세스 최적화 중 오류: $($_.Exception.Message)"
}

# ============================================================
# 최종 결과
# ============================================================
Start-Sleep -Seconds 2  # 시스템 안정화 대기

$memAfter = Get-MemoryUsage
$memoryFreed = $memAfter.FreeGB - $memBefore.FreeGB

Write-Host "`n╔══════════════════════════════════════════════════════════╗" -ForegroundColor White
Write-Host "║                    🎉 정리 완료!                        ║" -ForegroundColor White
Write-Host "╚══════════════════════════════════════════════════════════╝`n" -ForegroundColor White

Write-Host "📊 최종 메모리 상태" -ForegroundColor Yellow
Write-Host "   전체: $($memAfter.TotalGB) GB" -ForegroundColor White
Write-Host "   사용: $($memAfter.UsedGB) GB ($($memAfter.Percent)%)" -ForegroundColor $(if ($memAfter.Percent -ge 90) { "Red" } elseif ($memAfter.Percent -ge 85) { "Yellow" } else { "Green" })
Write-Host "   여유: $($memAfter.FreeGB) GB`n" -ForegroundColor White

Write-Host "📈 최적화 결과" -ForegroundColor Yellow
Write-Host "   메모리 사용률: $($memBefore.Percent)% → $($memAfter.Percent)% " -NoNewline
$improvement = $memBefore.Percent - $memAfter.Percent
if ($improvement -gt 0) {
    Write-Host "(-$([math]::Round($improvement, 1))%)" -ForegroundColor Green
}
else {
    Write-Host "(변화 없음)" -ForegroundColor Yellow
}

Write-Host "   확보된 메모리: $([math]::Round($memoryFreed, 2)) GB" -ForegroundColor White
Write-Host "   정리된 파일: $([math]::Round($totalCleaned, 2)) MB`n" -ForegroundColor White

if ($memAfter.Percent -le 70) {
    Write-Host "✅ 목표 달성! 메모리 사용률 70% 이하" -ForegroundColor Green
}
elseif ($memAfter.Percent -le 85) {
    Write-Host "✅ 정상 범위! 메모리 사용률 85% 이하" -ForegroundColor Green
}
else {
    Write-Host "⚠️  추가 최적화 권장! 수동으로 앱 종료 필요" -ForegroundColor Yellow
    Write-Host "`n   메모리 많이 사용 중인 프로세스 (상위 5개):" -ForegroundColor Gray
    
    Get-Process | 
        Where-Object { $_.WorkingSet64 -gt 50MB } |
        Sort-Object WorkingSet64 -Descending |
        Select-Object -First 5 |
        ForEach-Object {
            $memMB = [math]::Round($_.WorkingSet64 / 1MB, 0)
            Write-Host "   - $($_.ProcessName): ${memMB} MB" -ForegroundColor Gray
        }
}

Write-Host "`n로그 시간: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor DarkGray
Write-Host "`n스크립트 실행 완료. 창을 닫으려면 아무 키나 누르세요..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
