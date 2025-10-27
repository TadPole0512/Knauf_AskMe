@echo off
chcp 65001 >nul 2>&1
REM ============================================================
REM Memory Optimizer - Installation Script
REM Version: 2.0
REM ============================================================

echo.
echo ====================================================================
echo          Memory Optimizer v2.0 Install Program
echo ====================================================================
echo.

REM Check admin rights
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [ERROR] Administrator privileges required.
    echo Right-click and select "Run as administrator"
    pause
    exit /b 1
)

echo [1/6] Checking installation path...
set INSTALL_DIR=C:\Tools\MemoryOptimizer_CLAUDE
set DATA_DIR=C:\ProgramData\MemoryOptimizer_CLAUDE

if exist "%INSTALL_DIR%" (
    echo   ! Existing installation detected: %INSTALL_DIR%
    choice /C YN /M "Overwrite existing installation?"
    if %errorLevel% neq 1 (
        echo Installation cancelled.
        pause
        exit /b 0
    )
    echo   Cleaning up...
    rd /s /q "%INSTALL_DIR%" 2>nul
)

echo   OK Installation path: %INSTALL_DIR%

echo.
echo [2/6] Creating directories...
mkdir "%INSTALL_DIR%\scripts" 2>nul
mkdir "%INSTALL_DIR%\config" 2>nul
mkdir "%INSTALL_DIR%\docs" 2>nul
mkdir "%DATA_DIR%\logs" 2>nul
mkdir "%DATA_DIR%\reports" 2>nul
mkdir "%DATA_DIR%\backup" 2>nul

if exist "%INSTALL_DIR%\scripts" (
    echo   OK Directories created
) else (
    echo   ERROR Failed to create directories
    pause
    exit /b 1
)

echo.
echo [3/6] Copying files...

set SOURCE_DIR=%~dp0

copy "%SOURCE_DIR%scripts\*.ps1" "%INSTALL_DIR%\scripts\" >nul 2>&1
copy "%SOURCE_DIR%scripts\*.py" "%INSTALL_DIR%\scripts\" >nul 2>&1
copy "%SOURCE_DIR%scripts\*.bat" "%INSTALL_DIR%\scripts\" >nul 2>&1
copy "%SOURCE_DIR%config\*.json" "%INSTALL_DIR%\config\" >nul 2>&1
copy "%SOURCE_DIR%docs\*.md" "%INSTALL_DIR%\docs\" >nul 2>&1

if exist "%INSTALL_DIR%\scripts\QuickMemoryClean.ps1" (
    echo   OK PowerShell script copied
) else (
    echo   ERROR File copy failed
    pause
    exit /b 1
)

if exist "%INSTALL_DIR%\scripts\memory_optimizer.py" (
    echo   OK Python automation engine copied
)

if exist "%INSTALL_DIR%\config\config.json" (
    echo   OK Configuration file copied
)

echo.
echo [4/6] Checking Python dependencies...

python --version >nul 2>&1
if %errorLevel% equ 0 (
    echo   OK Python installation detected
    
    echo   Installing psutil module...
    python -m pip install psutil --break-system-packages --quiet >nul 2>&1
    if %errorLevel% equ 0 (
        echo   OK psutil installed
    ) else (
        echo   ! psutil installation failed (manual install required)
        echo     Command: pip install psutil --break-system-packages
    )
) else (
    echo   ! Python not installed (automation engine unavailable)
    echo     Download from: https://www.python.org/downloads/
)

echo.
echo [5/6] Creating shortcuts...

set DESKTOP=%USERPROFILE%\Desktop
powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%DESKTOP%\MemoryClean.lnk'); $Shortcut.TargetPath = 'powershell.exe'; $Shortcut.Arguments = '-ExecutionPolicy Bypass -File \"%INSTALL_DIR%\scripts\QuickMemoryClean.ps1\"'; $Shortcut.WindowStyle = 1; $Shortcut.IconLocation = 'C:\Windows\System32\shell32.dll,242'; $Shortcut.Save()"

if exist "%DESKTOP%\MemoryClean.lnk" (
    echo   OK Desktop shortcut created
)

set STARTMENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Memory Optimizer
mkdir "%STARTMENU%" 2>nul
powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%STARTMENU%\MemoryClean.lnk'); $Shortcut.TargetPath = 'powershell.exe'; $Shortcut.Arguments = '-ExecutionPolicy Bypass -File \"%INSTALL_DIR%\scripts\QuickMemoryClean.ps1\"'; $Shortcut.WindowStyle = 1; $Shortcut.Save()"

powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%STARTMENU%\Restore.lnk'); $Shortcut.TargetPath = '%INSTALL_DIR%\scripts\restore.bat'; $Shortcut.WindowStyle = 1; $Shortcut.Save()"

echo   OK Start menu shortcuts created

echo.
echo [6/6] Setting environment variables...

setx PATH "%PATH%;%INSTALL_DIR%\scripts" /M >nul 2>&1
if %errorLevel% equ 0 (
    echo   OK Environment variables configured
) else (
    echo   ! Environment variable configuration failed (admin rights required)
)

echo.
echo ====================================================================
echo                    Installation Complete!
echo ====================================================================
echo.

echo Installation location:
echo    - Program: %INSTALL_DIR%
echo    - Data: %DATA_DIR%
echo.

echo Quick start:
echo.
echo    1. Emergency manual cleanup (immediate execution)
echo       - Desktop "MemoryClean" double-click
echo       or
echo       - PowerShell: QuickMemoryClean.ps1
echo.
echo    2. Automation engine (background execution)
echo       - PowerShell:
echo       cd "%INSTALL_DIR%\scripts"
echo       python memory_optimizer.py
echo.
echo    3. Emergency recovery (if problems occur)
echo       - Start menu  Memory Optimizer  Restore
echo.

echo For detailed usage:
echo    %INSTALL_DIR%\docs\README.md
echo.

echo Restart your computer to apply environment variables.
echo.

choice /C YN /M "Run memory cleanup now?"
if %errorLevel% equ 1 (
    echo.
    echo Running memory cleanup...
    powershell -ExecutionPolicy Bypass -File "%INSTALL_DIR%\scripts\QuickMemoryClean.ps1"
)

echo.
echo Press any key to close the installer...
pause >nul