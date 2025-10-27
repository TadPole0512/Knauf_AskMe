@echo off
title MemoryOptimizer Restore
echo [*] Stopping scheduled task...
schtasks /End /TN "MemoryOptimizer" >NUL 2>&1
schtasks /Delete /TN "MemoryOptimizer" /F >NUL 2>&1
echo [*] Done. Check logs at C:\Tools\ChatGPT\MemoryOptimizer\logs
pause
