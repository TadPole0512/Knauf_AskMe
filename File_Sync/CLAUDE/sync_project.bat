@echo off
echo ========================================
echo IntelliJ 프로젝트 동기화
echo ========================================
echo.

cd C:\WORK\scripts

python sync_folders.py --source "c:\staybymeerp-Intellij\" --target "c:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\"

echo.
echo 동기화 완료!
pause