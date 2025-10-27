# ================================
# Clear-Memory-Native.ps1
# 관리자 권한 필요, 외부 도구 없음 (P/Invoke)
# 기능: Standby/LowPriorityStandby/Modified 리스트 비우기 + 모든 프로세스 워킹셋 축소
# ================================

# 0) 관리자 권한 확인 & 자동 상승
$curr = [Security.Principal.WindowsIdentity]::GetCurrent()
$princ = New-Object Security.Principal.WindowsPrincipal($curr)
if (-not $princ.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Start-Process -FilePath "powershell.exe" -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" -Verb RunAs
    exit
}

# 1) Win32 API 바인딩 (NtSetSystemInformation / EmptyWorkingSet 등)
Add-Type -Language CSharp -TypeDefinition @"
using System;
using System.Runtime.InteropServices;

public static class NativeMem {
    // --- ntdll!NtSetSystemInformation ---
    [DllImport("ntdll.dll")]
    private static extern int NtSetSystemInformation(
        int SystemInformationClass,
        IntPtr SystemInformation,
        int SystemInformationLength
    );

    // SystemInformationClass 값: SystemMemoryListInformation = 80(0x50)
    private const int SystemMemoryListInformation = 80;

    // MEMORY_LIST_COMMAND
    // 0:MemoryCaptureAccessedBits, 1:MemoryCaptureAndReset, 2:MemoryEmptyWorkingSets,
    // 3:MemoryFlushModifiedList, 4:MemoryPurgeStandbyList, 5:MemoryPurgeLowPriorityStandbyList
    public enum MemListCmd : int {
        MemoryCaptureAccessedBits = 0,
        MemoryCaptureAndReset = 1,
        MemoryEmptyWorkingSets = 2,
        MemoryFlushModifiedList = 3,
        MemoryPurgeStandbyList = 4,
        MemoryPurgeLowPriorityStandbyList = 5
    }

    public static int InvokeMemoryListCommand(MemListCmd cmd) {
        IntPtr p = Marshal.AllocHGlobal(sizeof(int));
        try {
            Marshal.WriteInt32(p, (int)cmd);
            return NtSetSystemInformation(SystemMemoryListInformation, p, sizeof(int));
        } finally {
            Marshal.FreeHGlobal(p);
        }
    }

    // --- psapi!EmptyWorkingSet + 핸들 관리 ---
    [DllImport("psapi.dll")]
    private static extern bool EmptyWorkingSet(IntPtr hProcess);

    [DllImport("kernel32.dll")]
    private static extern IntPtr OpenProcess(uint access, bool inherit, int pid);

    [DllImport("kernel32.dll")]
    private static extern bool CloseHandle(IntPtr hObject);

    // 접근 권한
    private const uint PROCESS_QUERY_INFORMATION = 0x0400;
    private const uint PROCESS_SET_QUOTA        = 0x0100;

    public static bool EmptyProcessWorkingSet(int pid) {
        IntPtr h = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_SET_QUOTA, false, pid);
        if (h == IntPtr.Zero) return false;
        try {
            return EmptyWorkingSet(h);
        } finally {
            CloseHandle(h);
        }
    }
}
"@

function Get-MemInfo {
    $os = Get-CimInstance Win32_OperatingSystem
    $totalMB = [math]::Round($os.TotalVisibleMemorySize/1024,1)
    $freeMB  = [math]::Round($os.FreePhysicalMemory/1024,1)
    $usedMB  = [math]::Round($totalMB - $freeMB,1)
    $usedPct = [math]::Round(($usedMB/$totalMB)*100,1)
    [PSCustomObject]@{
        TotalMB = $totalMB
        UsedMB  = $usedMB
        FreeMB  = $freeMB
        UsedPct = "$usedPct`%"
    }
}

Write-Host "`n=== 메모리 정리(네이티브) 시작 ===" -ForegroundColor Cyan
$before = Get-MemInfo
Write-Host ("정리 전 ->  총: {0} MB, 사용: {1} MB({2}), 여유: {3} MB" -f $before.TotalMB,$before.UsedMB,$before.UsedPct,$before.FreeMB)

# 2) Standby / LowPriorityStandby / Modified 리스트 정리
#   - 실패 시에도 계속 진행 (일부 환경 권한/커널 정책으로 반환코드가 다를 수 있음)
$null = [NativeMem]::InvokeMemoryListCommand([NativeMem+MemListCmd]::MemoryPurgeStandbyList)
$null = [NativeMem]::InvokeMemoryListCommand([NativeMem+MemListCmd]::MemoryPurgeLowPriorityStandbyList)
$null = [NativeMem]::InvokeMemoryListCommand([NativeMem+MemListCmd]::MemoryFlushModifiedList)

# 3) 모든 프로세스 워킹셋 축소 (일시적으로 프리징 느껴질 수 있음)
Get-Process | ForEach-Object {
    try   { [void][NativeMem]::EmptyProcessWorkingSet($_.Id) } 
    catch { }
}

Start-Sleep -Seconds 2

$after = Get-MemInfo
Write-Host ("정리 후 ->  총: {0} MB, 사용: {1} MB({2}), 여유: {3} MB" -f $after.TotalMB,$after.UsedMB,$after.UsedPct,$after.FreeMB)

# 4) 로그 (선택)
$logDir = "C:\Tools\Logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$stamp = (Get-Date).ToString("yyyy-MM-dd_HH-mm-ss")
[PSCustomObject]@{
    Time     = (Get-Date)
    BeforeMB = $before.FreeMB
    AfterMB  = $after.FreeMB
} | Export-Csv -Path (Join-Path $logDir "ClearMemory_Native_$stamp.csv") -NoTypeInformation -Encoding UTF8

Write-Host "로그 저장: $logDir" -ForegroundColor DarkGray
Write-Host "=== 메모리 정리 완료 ===`n" -ForegroundColor Green
