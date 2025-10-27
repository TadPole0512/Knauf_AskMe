$ROOT = "C:\Tools\ChatGPT\MemoryOptimizer"
$DATA = Join-Path $ROOT "data"
$LOGS = Join-Path $ROOT "logs"
$ReportJson = Join-Path $DATA "report.json"
$ReportHtml = Join-Path $ROOT "memory_analysis.html"

# 1) 폴더 보장
New-Item -ItemType Directory -Force -Path $DATA,$LOGS | Out-Null

# 2) Top 30 프로세스/시작프로그램 수집  report.json 즉시 생성
$procs = Get-Process | ForEach-Object {
  try {
    [pscustomobject]@{
      name   = $_.ProcessName
      pid    = $_.Id
      rss_mb = [math]::Round($_.WorkingSet64/1MB)
    }
  } catch {}
} | Sort-Object -Property rss_mb -Descending | Select-Object -First 30

$startup = @()
$regPaths = @(
  "HKCU:\Software\Microsoft\Windows\CurrentVersion\Run",
  "HKLM:\Software\Microsoft\Windows\CurrentVersion\Run"
)
foreach($rp in $regPaths){
  if(Test-Path $rp){
    $item = Get-Item $rp
    foreach($val in $item.Property){
      $startup += [pscustomobject]@{ name=$val; state="Enabled" }
    }
  }
}

$mem = Get-CimInstance Win32_OperatingSystem
$total = [math]::Round(($mem.TotalVisibleMemorySize/1MB),2)
$usedP = [math]::Round((($mem.TotalVisibleMemorySize-$mem.FreePhysicalMemory)/$mem.TotalVisibleMemorySize)*100,1)
function Get-Level($p){ if($p -ge 99){'EMERGENCY'} elseif($p -ge 95){'CRITICAL'} elseif($p -ge 90){'CAUTION'} elseif($p -ge 85){'WARNING'} else {'INFO'} }

$payload = [pscustomobject]@{
  generated_at = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
  summary      = [pscustomobject]@{
    total_gb   = [math]::Round($total/1024,2)
    percent    = $usedP
    level_text = Get-Level $usedP
  }
  top_processes    = $procs
  startup_programs = $startup
  services         = @()
  temp_sizes       = @{}
}

$payload | ConvertTo-Json -Depth 5 | Out-File -Encoding UTF8 -FilePath $ReportJson -Force

# 3) HTML 없으면 기본 템플릿 생성
if(-not (Test-Path $ReportHtml)){
  $Html = @"
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
<h1> Memory Optimizer - 진단 리포트</h1>
<div class="card"><div class="h">요약 <span id="summaryBadge" class="badge">-</span></div><div id="summary"></div></div>
<div class="card"><div class="h">상위 메모리 프로세스 (Top 30)</div>
<table id="procs"><thead><tr><th>프로세스</th><th>PID</th><th>메모리(MB)</th></tr></thead><tbody></tbody></table></div>
<div class="card"><div class="h">시작프로그램 영향도</div>
<table id="startup"><thead><tr><th>항목</th><th>상태</th></tr></thead><tbody></tbody></table></div>
<div class="small">생성 시각: <span id="ts"></span></div>
<script>
fetch('file:///$($ReportJson.Replace('\','/'))').then(r=>r.json()).then(j=>{
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
  [IO.File]::WriteAllText($ReportHtml, $Html, [Text.Encoding]::UTF8)
}

# 4) 열기
Start-Process $ReportHtml

# 5) 상태 출력
schtasks /Query /TN "MemoryOptimizer" > $null 2>&1
$hasTask = ($LASTEXITCODE -eq 0)
Write-Host (" report.json 갱신 완료  | 스케줄러 등록: " + $hasTask) -ForegroundColor Green
Write-Host ("로그 경로: " + (Join-Path $ROOT "logs")) -ForegroundColor Cyan
