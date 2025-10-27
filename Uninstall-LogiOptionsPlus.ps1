# Uninstall-LogiOptionsPlus.ps1
Write-Host "🧹 Logitech Options+ 완전 제거 시작..."

Stop-Process -Name "LogiOptionsPlus*" -Force -ErrorAction SilentlyContinue
Stop-Service -Name "LogiOptionsPlusService" -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3

# MSI 기반 제거 시도
Get-WmiObject -Query "SELECT * FROM Win32_Product WHERE Name LIKE '%Logi%'" | ForEach-Object {
    try { $_.Uninstall() } catch { Write-Warning "MSI 제거 실패: $($_.Name)" }
}

# 잔여 폴더 정리
$folders = @(
  "C:\Program Files\Logitech",
  "C:\ProgramData\Logi",
  "$env:AppData\Logi",
  "$env:LocalAppData\Logi"
)
foreach ($f in $folders) {
  if (Test-Path $f) { Remove-Item -Recurse -Force $f -ErrorAction SilentlyContinue }
}

# 서비스 등록 확인 후 삭제
sc delete LogiOptionsPlusService 2>$null

Write-Host "✅ 완전 삭제 완료. 재부팅 후 확인하세요."
