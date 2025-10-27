@echo off
REM ============================================================
REM Memory Optimizer - Installation Script
REM Version: 2.0
REM Description: 자동 설치 및 초기 설정
REM ============================================================

echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║         Memory Optimizer v2.0 설치 프로그램              ║
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

echo [1/6] 설치 위치 확인...
set INSTALL_DIR=C:\Tools\CLAUDE\MemoryOptimizer
set DATA_DIR=C:\ProgramData\CLAUDE\MemoryOptimizer

if exist "%INSTALL_DIR%" (
    echo   ⚠ 기존 설치 감지됨: %INSTALL_DIR%
    choice /C YN /M "기존 설치를 덮어쓰시겠습니까?"
    if %errorLevel% neq 1 (
        echo 설치 취소됨.
        pause
        exit /b 0
    )
    echo   정리 중...
    rd /s /q "%INSTALL_DIR%" 2>nul
)

echo   ✓ 설치 위치: %INSTALL_DIR%

echo.
echo [2/6] 디렉토리 생성 중...
mkdir "%INSTALL_DIR%\scripts" 2>nul
mkdir "%INSTALL_DIR%\config" 2>nul
mkdir "%INSTALL_DIR%\docs" 2>nul
mkdir "%DATA_DIR%\logs" 2>nul
mkdir "%DATA_DIR%\reports" 2>nul
mkdir "%DATA_DIR%\backup" 2>nul

if exist "%INSTALL_DIR%\scripts" (
    echo   ✓ 디렉토리 생성 완료
) else (
    echo   ✗ 디렉토리 생성 실패
    pause
    exit /b 1
)

echo.
echo [3/6] 파일 복사 중...

REM 현재 스크립트 위치에서 파일 복사
set SOURCE_DIR=%~dp0

copy "%SOURCE_DIR%scripts\*.ps1" "%INSTALL_DIR%\scripts\" >nul 2>&1
copy "%SOURCE_DIR%scripts\*.py" "%INSTALL_DIR%\scripts\" >nul 2>&1
copy "%SOURCE_DIR%scripts\*.bat" "%INSTALL_DIR%\scripts\" >nul 2>&1
copy "%SOURCE_DIR%config\*.json" "%INSTALL_DIR%\config\" >nul 2>&1
copy "%SOURCE_DIR%docs\*.md" "%INSTALL_DIR%\docs\" >nul 2>&1

if exist "%INSTALL_DIR%\scripts\QuickMemoryClean.ps1" (
    echo   ✓ PowerShell 스크립트 복사 완료
) else (
    echo   ✗ 파일 복사 실패
    pause
    exit /b 1
)

if exist "%INSTALL_DIR%\scripts\memory_optimizer.py" (
    echo   ✓ Python 자동화 엔진 복사 완료
)

if exist "%INSTALL_DIR%\config\config.json" (
    echo   ✓ 설정 파일 복사 완료
)

echo.
echo [4/6] Python 의존성 확인...

python --version >nul 2>&1
if %errorLevel% equ 0 (
    echo   ✓ Python 설치 확인됨
    
    echo   psutil 모듈 설치 중...
    python -m pip install psutil --break-system-packages --quiet >nul 2>&1
    if %errorLevel% equ 0 (
        echo   ✓ psutil 설치 완료
    ) else (
        echo   ⚠ psutil 설치 실패 (수동 설치 필요)
        echo     명령: pip install psutil --break-system-packages
    )
) else (
    echo   ⚠ Python 미설치 (Python 자동화 엔진 사용 불가)
    echo     https://www.python.org/downloads/ 에서 다운로드
)

echo.
echo [5/6] 바로가기 생성 중...

REM 바탕화면 바로가기
set DESKTOP=%USERPROFILE%\Desktop
powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%DESKTOP%\메모리 긴급 정리.lnk'); $Shortcut.TargetPath = 'powershell.exe'; $Shortcut.Arguments = '-ExecutionPolicy Bypass -File \"%INSTALL_DIR%\scripts\QuickMemoryClean.ps1\"'; $Shortcut.WindowStyle = 1; $Shortcut.IconLocation = 'C:\Windows\System32\shell32.dll,242'; $Shortcut.Save()"

if exist "%DESKTOP%\메모리 긴급 정리.lnk" (
    echo   ✓ 바탕화면 바로가기 생성 완료
)

REM 시작 메뉴 바로가기
set STARTMENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Memory Optimizer
mkdir "%STARTMENU%" 2>nul
powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%STARTMENU%\메모리 긴급 정리.lnk'); $Shortcut.TargetPath = 'powershell.exe'; $Shortcut.Arguments = '-ExecutionPolicy Bypass -File \"%INSTALL_DIR%\scripts\QuickMemoryClean.ps1\"'; $Shortcut.WindowStyle = 1; $Shortcut.Save()"

powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%STARTMENU%\긴급 복구.lnk'); $Shortcut.TargetPath = '%INSTALL_DIR%\scripts\restore.bat'; $Shortcut.WindowStyle = 1; $Shortcut.Save()"

echo   ✓ 시작 메뉴 바로가기 생성 완료

echo.
echo [6/6] 환경 변수 설정 중...

REM PATH에 추가
setx PATH "%PATH%;%INSTALL_DIR%\scripts" /M >nul 2>&1
if %errorLevel% equ 0 (
    echo   ✓ 환경 변수 설정 완료
) else (
    echo   ⚠ 환경 변수 설정 실패 (관리자 권한 필요)
)

echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║                    🎉 설치 완료!                        ║
echo ╚══════════════════════════════════════════════════════════╝
echo.

echo 📂 설치 위치:
echo    - 프로그램: %INSTALL_DIR%
echo    - 데이터: %DATA_DIR%
echo.

echo 🚀 바로 실행 방법:
echo.
echo    1. 긴급 수동 정리 (즉시 실행)
echo       - 바탕화면 "메모리 긴급 정리" 더블클릭
echo       또는
echo       - PowerShell에서: QuickMemoryClean.ps1
echo.
echo    2. 자동화 엔진 (백그라운드 실행)
echo       - PowerShell에서:
echo       cd "%INSTALL_DIR%\scripts"
echo       python memory_optimizer.py
echo.
echo    3. 긴급 복구 (문제 발생 시)
echo       - 시작 메뉴 ^> Memory Optimizer ^> 긴급 복구
echo.

echo 📖 자세한 사용법:
echo    %INSTALL_DIR%\docs\README.md
echo.

echo 컴퓨터를 재시작하면 환경 변수가 적용됩니다.
echo.

choice /C YN /M "지금 메모리 긴급 정리를 실행하시겠습니까?"
if %errorLevel% equ 1 (
    echo.
    echo 메모리 정리 실행 중...
    powershell -ExecutionPolicy Bypass -File "%INSTALL_DIR%\scripts\QuickMemoryClean.ps1"
)

echo.
echo 설치 프로그램을 종료하려면 아무 키나 누르세요...
pause >nul
