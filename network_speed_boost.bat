@echo off
echo ==============================
echo 네트워크 속도 향상 초기화 스크립트
echo ==============================
echo.
echo 1. DNS 캐시 초기화 중...
ipconfig /flushdns
echo.
echo 2. IP 해제 및 재할당 중...
ipconfig /release
ipconfig /renew
echo.
echo 3. TCP/IP 스택 초기화 중...
netsh int ip reset
echo.
echo 4. Winsock 초기화 중...
netsh winsock reset
echo.
echo 모든 작업이 완료되었습니다.
echo PC를 재부팅하면 변경 사항이 적용됩니다.
pause
