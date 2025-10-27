param(
  [ValidateSet('Level1','Level2','Level3')] [string]$Level = 'Level1',
  [string]$ConfigPath = 'C:\Tools\ChatGPT\MemoryOptimizer\config.json'
)
$ErrorActionPreference = 'SilentlyContinue'
function Log($msg){
  try{
    $cfg = Get-Content $ConfigPath -Raw | ConvertFrom-Json
    $logPath = $cfg.paths.logs
    if(-not (Test-Path $logPath)){ New-Item -ItemType Directory -Force -Path $logPath | Out-Null }
    $stamp = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
    ('['+$stamp+'] '+$msg) | Out-File -FilePath (Join-Path $logPath 'optimize.log') -Encoding UTF8 -Append
  }catch{}
}
function Clear-Temp {
  try{
    Remove-Item "$env:TEMP\*" -Recurse -Force -ErrorAction SilentlyContinue
    Remove-Item "C:\Windows\Temp\*" -Recurse -Force -ErrorAction SilentlyContinue
    Get-ChildItem "C:\Windows\Prefetch" -ErrorAction SilentlyContinue | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
    Log 'Clear-Temp: OK'
  }catch{ Log "Clear-Temp: $_" }
}
function Clear-BrowserCache {
  try{
    $chrome = "$env:LOCALAPPDATA\Google\Chrome\User Data\Default\Cache"
    $edge   = "$env:LOCALAPPDATA\Microsoft\Edge\User Data\Default\Cache"
    Stop-Process -Name chrome,msedge -Force -ErrorAction SilentlyContinue
    Start-Sleep 2
    if(Test-Path $chrome){ Remove-Item "$chrome\*" -Recurse -Force -ErrorAction SilentlyContinue }
    if(Test-Path $edge){ Remove-Item "$edge\*" -Recurse -Force -ErrorAction SilentlyContinue }
    Log 'Clear-BrowserCache: OK'
  }catch{ Log "Clear-BrowserCache: $_" }
}
function Flush-SystemCache {
  try{
    $procs = Get-Process | Where-Object { $_.Responding -and $_.MainWindowHandle -eq 0 }
    foreach($p in $procs){ try{ $null = (Get-Process -Id $p.Id).Handle; [System.GC]::Collect() }catch{} }
    Log 'Flush-SystemCache: Soft trim attempted'
  }catch{ Log "Flush-SystemCache: $_" }
}
function Lower-BackgroundPriority {
  try{
    foreach($p in Get-Process){ try{ if($p.MainWindowHandle -eq 0){ $p.PriorityClass = 'BelowNormal' } }catch{} }
    Log 'Lower-BackgroundPriority: OK'
  }catch{ Log "Lower-BackgroundPriority: $_" }
}
function Pause-NonEssentialServices {
  try{
    $candidates = 'DiagTrack','RetailDemo','WerSvc','MapsBroker'
    foreach($s in $candidates){
      Get-Service $s -ErrorAction SilentlyContinue | Where-Object {$_.Status -eq 'Running'} | Stop-Service -Force -ErrorAction SilentlyContinue
    }
    Log 'Pause-NonEssentialServices: OK'
  }catch{ Log "Pause-NonEssentialServices: $_" }
}
function Kill-NonWhitelisted {
  param([string[]]$Whitelist)
  try{
    foreach($p in Get-Process){
      $name = ($p.Name + '.exe').ToLower()
      if($Whitelist -notcontains $name){
        if($p.WorkingSet64 -gt 100MB){ try{ $p.Kill() }catch{} }
      }
    }
    Log 'Kill-NonWhitelisted: OK'
  }catch{ Log "Kill-NonWhitelisted: $_" }
}
$cfg = Get-Content $ConfigPath -Raw | ConvertFrom-Json
$whitelist = ($cfg.whitelist | ForEach-Object { $_.ToLower() })
switch($Level){
  'Level1' { Clear-Temp; Clear-BrowserCache; Flush-SystemCache }
  'Level2' { Clear-Temp; Clear-BrowserCache; Flush-SystemCache; Lower-BackgroundPriority; Pause-NonEssentialServices }
  'Level3' { Clear-Temp; Clear-BrowserCache; Flush-SystemCache; Lower-BackgroundPriority; Pause-NonEssentialServices; Kill-NonWhitelisted -Whitelist $whitelist }
}
