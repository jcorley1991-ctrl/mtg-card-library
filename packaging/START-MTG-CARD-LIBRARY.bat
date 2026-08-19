@echo off
setlocal
cd /d "%~dp0"
echo Starting MTG Card Library...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0mtg-server.ps1"
if errorlevel 1 (
  echo.
  echo MTG Card Library could not start.
  echo See the error above, then press any key to close.
  pause >nul
)
