@echo off
REM ============================================================
REM Memory Optimizer - Emergency Restore Script
REM Version: 2.0
REM Description: 최적화로 인한 문제 발생 시 시스템 복원
REM ============================================================

echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║         Memory Optimizer 긴급 복구 스크립트              ║
echo ║                  Version 2.0                             ║
echo ╚══════════════════════════════════════════════════════════╝
echo.

REM 관리자 권한 확인
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [오류] 관리자 권한이 필요합니다.
    echo 마우스 우클릭 후 "관리자 권한으로 실행"을 선택하세요.
    pause
    exit /b 1
)

echo [1/6] Windows Update 서비스 복원 중...
net start wuauserv >nul 2>&1
if %errorLevel% equ 0 (
    echo   ✓ Windows Update 서비스 시작 완료
) else (
    echo   ⚠ Windows Update 서비스 이미 실행 중이거나 시작 실패
)

echo.
echo [2/6] SuperFetch/SysMain 서비스 복원 중...
net start SysMain >nul 2>&1
if %errorLevel% equ 0 (
    echo   ✓ SysMain 서비스 시작 완료
) else (
    echo   ⚠ SysMain 서비스 이미 실행 중이거나 시작 실패
)

echo.
echo [3/6] Windows Search 서비스 복원 중...
net start WSearch >nul 2>&1
if %errorLevel% equ 0 (
    echo   ✓ Windows Search 서비스 시작 완료
) else (
    echo   ⚠ Windows Search 서비스 이미 실행 중이거나 시작 실패
)

echo.
echo [4/6] Windows Defender 서비스 복원 중...
net start WinDefend >nul 2>&1
if %errorLevel% equ 0 (
    echo   ✓ Windows Defender 서비스 시작 완료
) else (
    echo   ⚠ Windows Defender 서비스 이미 실행 중이거나 시작 실패
)

echo.
echo [5/6] 시작 프로그램 확인...
echo   작업 관리자 ^> 시작프로그램 탭에서 수동 확인 필요
echo   필요한 프로그램을 "사용"으로 변경하세요.

echo.
echo [6/6] 시스템 파일 무결성 검사...
echo   (이 작업은 5~10분 소요될 수 있습니다)
echo.
sfc /scannow
if %errorLevel% equ 0 (
    echo   ✓ 시스템 파일 무결성 검사 완료
) else (
    echo   ⚠ 시스템 파일 검사 중 문제 발생
)

echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║                    복구 완료!                            ║
echo ╚══════════════════════════════════════════════════════════╝
echo.
echo 📋 추가 복구 작업이 필요한 경우:
echo.
echo 1. 레지스트리 백업 복원:
echo    - regedit 실행 ^> 파일 ^> 가져오기
echo    - C:\ProgramData\CLAUDE\MemoryOptimizer\backup\registry_backup.reg
echo.
echo 2. 시작 프로그램 복원:
echo    - 작업 관리자 (Ctrl+Shift+Esc)
echo    - 시작프로그램 탭에서 필요한 항목 "사용" 설정
echo.
echo 3. 시스템 복원 지점 사용:
echo    - 제어판 ^> 시스템 ^> 시스템 보호
echo    - 시스템 복원 버튼 클릭
echo.
echo 4. 로그 파일 확인:
echo    - C:\ProgramData\CLAUDE\MemoryOptimizer\logs\
echo.
echo 컴퓨터를 재시작하면 변경사항이 완전히 적용됩니다.
echo.

choice /C YN /M "지금 재시작하시겠습니까?"
if %errorLevel% equ 1 (
    echo 재시작 중...
    shutdown /r /t 10 /c "Memory Optimizer 복구 완료 - 재시작"
) else (
    echo 나중에 수동으로 재시작하세요.
)

echo.
echo 복구 스크립트 실행 완료.
echo 창을 닫으려면 아무 키나 누르세요...
pause >nul
