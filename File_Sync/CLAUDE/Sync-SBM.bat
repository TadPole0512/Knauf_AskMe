@echo off
setlocal

REM === 경로 설정 ===
set SRC=C:\staybymeerp-Intellij
set DST=C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-IntelliJ
set LOGDIR=C:\WORK\PROJECT\SBM\VSCODE\Logs

if not exist "%LOGDIR%" mkdir "%LOGDIR%"
for /f "tokens=1-4 delims=/:. " %%a in ("%date% %time%") do set TS=%%a%%b%%c_%%d
set LOG=%LOGDIR%\sync_sbm_%TS%.log

echo [INFO] SOURCE: %SRC%
echo [INFO] TARGET: %DST%
echo [INFO] LOG   : %LOG%
echo.

REM 1) 미리보기
python "%~dp0sync_folders_gpt.py" -s "%SRC%" -t "%DST%" --plan-only
if errorlevel 2 goto :ERR

echo.
choice /M "위 변경을 적용할까요?" /T 10 /D Y >nul
if errorlevel 2 goto :END

REM 2) 실제 적용 (백업+확인 스킵)
python "%~dp0sync_folders_gpt.py" -s "%SRC%" -t "%DST%" --backup -y --log "%LOG%"
if errorlevel 1 goto :ERR

echo.
echo [DONE] 동기화 완료.
goto :END

:ERR
echo [ERROR] 오류가 발생했습니다. 로그를 확인하세요: %LOG%
exit /b 1

:END
endlocal
