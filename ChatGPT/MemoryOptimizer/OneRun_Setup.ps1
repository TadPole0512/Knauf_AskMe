<# =====================================================================
 OneRun_Setup.ps1  (v2.0.4)
 목적: Windows 메모리 자동 최적화(요구사양 v2.0) 전체 구성 자동화
 변경: 모든 산출물 ROOT 하위 사용 + Python venv 생성 로직 안정화(런처/인자 분리)
 --------------------------------------------------------------------- #>

# -------------------- 공통/경로 --------------------
$ErrorActionPreference = 'Stop'

$ROOT = 'C:\Tools\ChatGPT\MemoryOptimizer'
$DATA = Join-Path $ROOT 'data'
$LOGS = Join-Path $ROOT 'logs'
$REPS = Join-Path $ROOT 'reports'

$ConfigPath        = Join-Path $ROOT 'config.json'
$WhitelistPath     = Join-Path $ROOT 'whitelist.txt'
$OptimizePs1Path   = Join-Path $ROOT 'optimize.ps1'
$MonitorPyPath     = Join-Path $ROOT 'monitor.py'
$QuickCleanPs1Path = Join-Path $ROOT 'QuickMemoryClean.ps1'
$RestoreBatPath    = Join-Path $ROOT 'restore.bat'
$ReportOutputPath  = Join-Path $ROOT 'memory_analysis.html'
$ReportJsonPath    = Join-Path $DATA 'report.json'
$SetupFlag         = Join-Path $ROOT '.setup_done'

$VENV    = Join-Path $ROOT 'venv'
$PyExe   = Join-Path $VENV 'Scripts\python.exe'
$PywExe  = Join-Path $VENV 'Scripts\pythonw.exe'

New-Item -ItemType Directory -Force -Path $ROOT,$DATA,$LOGS,$REPS | Out-Null

# -------------------- 관리자 확인 --------------------
$principal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
  Write-Host "⚠️ 관리자 권한으로 다시 실행해주세요." -ForegroundColor Yellow
  throw "AdminRequired"
}
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force

# -------------------- config.json --------------------
@"
{
  "monitoring": {
    "interval_seconds": 10,
    "thresholds": { "warning": 85, "caution": 90, "critical": 95, "emergency": 99 }
  },
  "paths": {
    "root": "$($ROOT.Replace('\','\\'))",
    "data": "$($DATA.Replace('\','\\'))",
    "logs": "$($LOGS.Replace('\','\\'))",
    "reports": "$($REPS.Replace('\','\\'))",
    "report_json": "$($ReportJsonPath.Replace('\','\\'))"
  },
  "whitelist": [
    "vscode.exe","idea64.exe","dbeaver.exe","mstsc.exe",
    "Teams.exe","chrome.exe","msedge.exe","explorer.exe","dwm.exe"
  ],
  "auto_optimize": {
    "clear_temp": true, "suspend_tabs": true, "reduce_visuals": true,
    "limit_background": false, "disable_telemetry": false
  },
  "schedule": { "deep_clean_time": "02:00", "report_interval": "daily" },
  "special_modes": { "remote_desktop_pause": true, "ide_build_protection": true, "teams_meeting_boost": true },
  "consent_flags": { "one_time_approval_done": false }
}
"@ | Out-File -Encoding UTF8 -FilePath $ConfigPath -Force

# -------------------- whitelist.txt --------------------
@'
vscode.exe
idea64.exe
dbeaver.exe
mstsc.exe
Teams.exe
chrome.exe
msedge.exe
explorer.exe
dwm.exe
'@ | Out-File -Encoding ASCII -FilePath $WhitelistPath -Force

# -------------------- optimize.ps1 --------------------
@"
param(
  [ValidateSet('Level1','Level2','Level3')] [string]`$Level = 'Level1',
  [string]`$ConfigPath = '$ConfigPath'
)
`$ErrorActionPreference = 'SilentlyContinue'
function Log(`$msg){
  try{
    `$cfg = Get-Content `$ConfigPath -Raw | ConvertFrom-Json
    `$logPath = `$cfg.paths.logs
    if(-not (Test-Path `$logPath)){ New-Item -ItemType Directory -Force -Path `$logPath | Out-Null }
    `$stamp = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
    ('['+`$stamp+'] '+`$msg) | Out-File -FilePath (Join-Path `$logPath 'optimize.log') -Encoding UTF8 -Append
  }catch{}
}
function Clear-Temp {
  try{
    Remove-Item "`$env:TEMP\*" -Recurse -Force -ErrorAction SilentlyContinue
    Remove-Item "C:\Windows\Temp\*" -Recurse -Force -ErrorAction SilentlyContinue
    Get-ChildItem "C:\Windows\Prefetch" -ErrorAction SilentlyContinue | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
    Log 'Clear-Temp: OK'
  }catch{ Log "Clear-Temp: `$_" }
}
function Clear-BrowserCache {
  try{
    `$chrome = "`$env:LOCALAPPDATA\Google\Chrome\User Data\Default\Cache"
    `$edge   = "`$env:LOCALAPPDATA\Microsoft\Edge\User Data\Default\Cache"
    Stop-Process -Name chrome,msedge -Force -ErrorAction SilentlyContinue
    Start-Sleep 2
    if(Test-Path `$chrome){ Remove-Item "`$chrome\*" -Recurse -Force -ErrorAction SilentlyContinue }
    if(Test-Path `$edge){ Remove-Item "`$edge\*" -Recurse -Force -ErrorAction SilentlyContinue }
    Log 'Clear-BrowserCache: OK'
  }catch{ Log "Clear-BrowserCache: `$_" }
}
function Flush-SystemCache {
  try{
    `$procs = Get-Process | Where-Object { `$_.Responding -and `$_.MainWindowHandle -eq 0 }
    foreach(`$p in `$procs){ try{ `$null = (Get-Process -Id `$p.Id).Handle; [System.GC]::Collect() }catch{} }
    Log 'Flush-SystemCache: Soft trim attempted'
  }catch{ Log "Flush-SystemCache: `$_" }
}
function Lower-BackgroundPriority {
  try{
    foreach(`$p in Get-Process){ try{ if(`$p.MainWindowHandle -eq 0){ `$p.PriorityClass = 'BelowNormal' } }catch{} }
    Log 'Lower-BackgroundPriority: OK'
  }catch{ Log "Lower-BackgroundPriority: `$_" }
}
function Pause-NonEssentialServices {
  try{
    `$candidates = 'DiagTrack','RetailDemo','WerSvc','MapsBroker'
    foreach(`$s in `$candidates){
      Get-Service `$s -ErrorAction SilentlyContinue | Where-Object `{`$_.Status -eq 'Running'`} | Stop-Service -Force -ErrorAction SilentlyContinue
    }
    Log 'Pause-NonEssentialServices: OK'
  }catch{ Log "Pause-NonEssentialServices: `$_" }
}
function Kill-NonWhitelisted {
  param([string[]]`$Whitelist)
  try{
    foreach(`$p in Get-Process){
      `$name = (`$p.Name + '.exe').ToLower()
      if(`$Whitelist -notcontains `$name){
        if(`$p.WorkingSet64 -gt 100MB){ try{ `$p.Kill() }catch{} }
      }
    }
    Log 'Kill-NonWhitelisted: OK'
  }catch{ Log "Kill-NonWhitelisted: `$_" }
}
`$cfg = Get-Content `$ConfigPath -Raw | ConvertFrom-Json
`$whitelist = (`$cfg.whitelist | ForEach-Object { `$_.ToLower() })
switch(`$Level){
  'Level1' { Clear-Temp; Clear-BrowserCache; Flush-SystemCache }
  'Level2' { Clear-Temp; Clear-BrowserCache; Flush-SystemCache; Lower-BackgroundPriority; Pause-NonEssentialServices }
  'Level3' { Clear-Temp; Clear-BrowserCache; Flush-SystemCache; Lower-BackgroundPriority; Pause-NonEssentialServices; Kill-NonWhitelisted -Whitelist `$whitelist }
}
"@ | Out-File -Encoding UTF8 -FilePath $OptimizePs1Path -Force

# -------------------- QuickMemoryClean.ps1 --------------------
@'
# 관리자 권한 필수
Remove-Item "$env:TEMP\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item "C:\Windows\Temp\*" -Recurse -Force -ErrorAction SilentlyContinue
Stop-Process -Name chrome,msedge -Force -ErrorAction SilentlyContinue
Start-Sleep 2
Remove-Item "$env:LOCALAPPDATA\Google\Chrome\User Data\Default\Cache\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item "$env:LOCALAPPDATA\Microsoft\Edge\User Data\Default\Cache\*" -Recurse -Force -ErrorAction SilentlyContinue
Stop-Service wuauserv -Force -ErrorAction SilentlyContinue
Remove-Item "C:\Windows\SoftwareDistribution\Download\*" -Recurse -Force -ErrorAction SilentlyContinue
Start-Service wuauserv -ErrorAction SilentlyContinue
Clear-RecycleBin -Force -ErrorAction SilentlyContinue
[System.GC]::Collect()
$mem = Get-CimInstance Win32_OperatingSystem
$usedPercent = [math]::Round((($mem.TotalVisibleMemorySize - $mem.FreePhysicalMemory) / $mem.TotalVisibleMemorySize) * 100, 1)
Write-Host "✅ 정리 완료! 현재 메모리: $usedPercent%" -ForegroundColor Green
'@ | Out-File -Encoding UTF8 -FilePath $QuickCleanPs1Path -Force

# -------------------- restore.bat --------------------
@"
@echo off
title MemoryOptimizer Restore
echo [*] Stopping scheduled task...
schtasks /End /TN "MemoryOptimizer" >NUL 2>&1
schtasks /Delete /TN "MemoryOptimizer" /F >NUL 2>&1
echo [*] Done. Check logs at $LOGS
pause
"@ | Out-File -Encoding ASCII -FilePath $RestoreBatPath -Force

# -------------------- HTML 리포트 --------------------
$HtmlTemplateContent = @"
<!doctype html>
<html lang="ko"><head><meta charset="utf-8">
<title>Memory Optimizer Report</title>
<style>
body{font-family:Segoe UI,Arial,sans-serif;margin:24px;background:#0f1115;color:#e6e6e6}
.card{background:#151822;border:1px solid #2a2f3a;border-radius:14px;padding:18px;margin:12px 0}
.h{font-size:20px;margin-bottom:8px}
.badge{display:inline-block;padding:2px 8px;border-radius:999px;border:1px solid #3a3f4a;margin-left:8px}
table{width:100%;border-collapse:collapse}
th,td{border-bottom:1px solid #2a2f3a;padding:8px;text-align:left;font-size:13px}
th{background:#1b1f2a}
.small{opacity:.8;font-size:12px}
</style></head>
<body>
<h1>📊 Memory Optimizer - 진단 리포트</h1>
<div class="card"><div class="h">요약 <span id="summaryBadge" class="badge">-</span></div><div id="summary"></div></div>
<div class="card"><div class="h">상위 메모리 프로세스 (Top 30)</div>
<table id="procs"><thead><tr><th>프로세스</th><th>PID</th><th>메모리(MB)</th></tr></thead><tbody></tbody></table></div>
<div class="card"><div class="h">시작프로그램 영향도</div>
<table id="startup"><thead><tr><th>항목</th><th>상태</th></tr></thead><tbody></tbody></table></div>
<div class="small">생성 시각: <span id="ts"></span></div>
<script>
fetch('file:///$($ReportJsonPath.Replace('\','/'))').then(r=>r.json()).then(j=>{
  document.getElementById('ts').innerText = j.generated_at;
  document.getElementById('summary').innerText = `총 메모리: ${j.summary.total_gb} GB, 사용률: ${j.summary.percent}%`;
  document.getElementById('summaryBadge').innerText = j.summary.level_text;
  const tb = document.querySelector('#procs tbody');
  j.top_processes.forEach(p=>{
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${p.name}</td><td>${p.pid}</td><td>${p.rss_mb}</td>`;
    tb.appendChild(tr);
  });
  const ts = document.querySelector('#startup tbody');
  j.startup_programs.forEach(s=>{
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${s.name}</td><td>${s.state}</td>`;
    ts.appendChild(tr);
  });
});
</script>
</body></html>
"@
[IO.File]::WriteAllText($ReportOutputPath, $HtmlTemplateContent, [Text.Encoding]::UTF8)

# -------------------- Python/venv & psutil (런처/인자 분리) --------------------
function Get-PythonInvoker {
  # 우선순위: py.exe -3 → python3 → python
  $candidates = @()
  try { $py = (Get-Command py -ErrorAction Stop).Source; $candidates += @{ Path=$py; Args=@('-3') } } catch {}
  try { $p3 = (Get-Command python3 -ErrorAction Stop).Source; $candidates += @{ Path=$p3; Args=@() } } catch {}
  try { $p  = (Get-Command python  -ErrorAction Stop).Source; $candidates += @{ Path=$p;  Args=@() } } catch {}
  foreach($c in $candidates){
    if(Test-Path $c.Path){ return $c }
  }
  return $null
}

$pyInvoker = Get-PythonInvoker
if(-not (Test-Path $VENV)){
  if(-not $pyInvoker){ throw "Python 3.x 런처(py/python)가 없습니다. 설치 후 다시 실행하세요." }
  Write-Host "📦 Python venv 생성 중..."
  $args = @($pyInvoker.Args + @('-m','venv',$VENV))
  # Start-Process 로 안전 실행(경로/공백 대응)
  $p = Start-Process -FilePath $pyInvoker.Path -ArgumentList $args -PassThru -Wait -NoNewWindow
  if($p.ExitCode -ne 0){ throw "venv 생성 실패 (ExitCode=$($p.ExitCode))" }
}
# pip 업그레이드 / psutil 설치
& $PyExe -m pip install --upgrade pip --quiet
& $PyExe -m pip install psutil --quiet
if(!(Test-Path $PywExe)){ throw "pythonw.exe가 없습니다. venv 생성이 비정상입니다: $VENV" }

# -------------------- monitor.py --------------------
@"
import os, json, time, psutil, subprocess, datetime, traceback, shutil

def now(): return datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
def log(msg, cfg):
    try:
        lp = cfg['paths']['logs']; os.makedirs(lp, exist_ok=True)
        with open(os.path.join(lp,'monitor.log'), 'a', encoding='utf-8') as f:
            f.write(f'[{now()}] {msg}\n')
    except: pass
def read_cfg(p):
    with open(p, 'r', encoding='utf-8') as f: return json.load(f)
def write_json(data, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    tmp = path + '.tmp'
    with open(tmp,'w',encoding='utf-8') as f: json.dump(data, f, ensure_ascii=False, indent=2)
    os.replace(tmp, path)

def collect_report(cfg):
    mem = psutil.virtual_memory()
    total_gb = round(mem.total/1024/1024/1024,2); used = mem.percent
    procs = []
    for p in psutil.process_iter(['pid','name','memory_info']):
        try:
            rss = p.info['memory_info'].rss if p.info['memory_info'] else 0
            procs.append({'name':p.info['name'] or '?', 'pid':p.info['pid'], 'rss_mb': round(rss/1024/1024)})
        except: pass
    procs.sort(key=lambda x:x['rss_mb'], reverse=True); top = procs[:30]
    startup = []
    try:
        import winreg
        for hive, path in [(winreg.HKEY_CURRENT_USER, r'Software\\Microsoft\\Windows\\CurrentVersion\\Run'),
                           (winreg.HKEY_LOCAL_MACHINE, r'Software\\Microsoft\\Windows\\CurrentVersion\\Run')]:
            try:
                k = winreg.OpenKey(hive, path); i=0
                while True:
                    try: name, val, _ = winreg.EnumValue(k, i); startup.append({'name':name, 'state':'Enabled'}); i+=1
                    except OSError: break
            except: pass
    except: pass
    th = cfg['monitoring']['thresholds']
    if used >= th['emergency']: lvl='EMERGENCY'
    elif used >= th['critical']: lvl='CRITICAL'
    elif used >= th['caution']:  lvl='CAUTION'
    elif used >= th['warning']:  lvl='WARNING'
    else: lvl='INFO'
    rep = {'generated_at': now(),'summary':{'total_gb': total_gb, 'percent': used, 'level_text': lvl},
           'top_processes': top,'startup_programs': startup,'services': [],'temp_sizes': {}}
    write_json(rep, cfg['paths']['report_json']); return used

def run_optimize(level, cfg):
    ps1 = os.path.join(cfg['paths']['root'], 'optimize.ps1')
    shell = shutil.which('pwsh') or 'powershell'
    try:
        subprocess.run([shell,'-NoProfile','-ExecutionPolicy','Bypass','-File',ps1,'-Level',level,'-ConfigPath',os.path.join(cfg['paths']['root'],'config.json')],
                       check=False, capture_output=True, text=True, timeout=180)
    except Exception as e: log(f'optimize error: {e}', cfg)

def is_remote_desktop(): return os.environ.get('SESSIONNAME','').upper().startswith('RDP-')
def is_teams_meeting():
    for p in psutil.process_iter(['name','cpu_percent']):
        if (p.info['name'] or '').lower()=='teams.exe' and (p.info['cpu_percent'] or 0)>1: return True
    return False
def is_building():
    suspects={'msbuild.exe','node.exe','java.exe','gradle.exe','dotnet.exe'}
    for p in psutil.process_iter(['name','cpu_percent']):
        if (p.info['name'] or '').lower() in suspects and (p.info['cpu_percent'] or 0)>10: return True
    return False

def main():
    cfg_path = r'$ConfigPath'; cfg = read_cfg(cfg_path)
    interval = int(cfg['monitoring']['interval_seconds']); th = cfg['monitoring']['thresholds']
    log('Monitor start', cfg)
    while True:
        try:
            used = collect_report(cfg)
            if cfg['special_modes']['remote_desktop_pause'] and is_remote_desktop(): log(f'RDP active -> pause ({used}%)', cfg); time.sleep(interval); continue
            if cfg['special_modes']['ide_build_protection'] and is_building():      log(f'Build active -> skip ({used}%)', cfg); time.sleep(interval); continue
            if cfg['special_modes']['teams_meeting_boost'] and is_teams_meeting():  log(f'Teams boost -> no kill ({used}%)', cfg)
            if used >= th['emergency']: log(f'EMERGENCY({used}%) -> L3', cfg); run_optimize('Level3', cfg)
            elif used >= th['critical']:log(f'CRITICAL({used}%) -> L2', cfg); run_optimize('Level2', cfg)
            elif used >= th['caution']: log(f'CAUTION({used}%) -> L1', cfg); run_optimize('Level1', cfg)
            elif used >= th['warning']: log(f'WARNING({used}%) -> log only', cfg)
            time.sleep(interval)
        except Exception as e:
            log('loop error: '+str(e), cfg); import traceback; log(traceback.format_exc(), cfg); time.sleep(interval)
if __name__=='__main__': main()
"@ | Out-File -Encoding UTF8 -FilePath $MonitorPyPath -Force

# -------------------- 초기 report.json --------------------
@{
  generated_at = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
  summary = @{ total_gb = 0; percent = 0; level_text = 'INFO' }
  top_processes = @(); startup_programs = @(); services = @(); temp_sizes = @{}
} | ConvertTo-Json -Depth 6 | Out-File -Encoding UTF8 -FilePath $ReportJsonPath -Force

# -------------------- 작업 스케줄러 등록 --------------------
$taskName = 'MemoryOptimizer'
try { schtasks /End /TN $taskName > $null 2>&1 } catch {}
try { schtasks /Delete /TN $taskName /F > $null 2>&1 } catch {}
$action    = New-ScheduledTaskAction -Execute $PywExe -Argument "`"$MonitorPyPath`""
$trigger   = New-ScheduledTaskTrigger -AtLogOn
$principal = New-ScheduledTaskPrincipal -UserId "$env:USERNAME" -RunLevel Highest
Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Principal $principal | Out-Null
Start-ScheduledTask -TaskName $taskName

# -------------------- 바탕화면 바로가기 --------------------
$desktop = [Environment]::GetFolderPath('Desktop')
"[InternetShortcut]
URL=file:///$($ReportOutputPath)
IconIndex=0" | Out-File -Encoding ASCII -FilePath "$desktop\Memory Report.url" -Force
Copy-Item $RestoreBatPath "$desktop\Memory Restore.bat" -Force
Copy-Item $QuickCleanPs1Path "$desktop\QuickMemoryClean.ps1" -Force

# -------------------- 완료 --------------------
'ok' | Out-File -Encoding ASCII -FilePath $SetupFlag -Force
Write-Host "✅ 설치 완료: $ROOT" -ForegroundColor Green
Write-Host "   - 자동 실행: 작업 스케줄러 'MemoryOptimizer' (로그온 시, 최고 권한)"
Write-Host "   - 리포트: $ReportOutputPath  (데이터: $ReportJsonPath)"
Write-Host "   - 로그:   $LOGS"
