@echo off
chcp 65001 >nul
title Baby Shop - Startup

echo =====================================
echo    Baby Shop - Starting Services
echo =====================================

set ROOT_DIR=%~dp0

:: Create logs directory
if not exist "%ROOT_DIR%logs" mkdir "%ROOT_DIR%logs"

:: Kill existing processes on ports
echo [1/3] Clearing ports...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" ^| findstr "LISTENING"') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":5173" ^| findstr "LISTENING"') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":3000" ^| findstr "LISTENING"') do taskkill /PID %%a /F >nul 2>&1
echo   Done.

:: Start backend
echo [2/3] Starting backend (port 8080)...
start /B cmd /c "cd /d %ROOT_DIR%backend && mvnw.cmd spring-boot:run -DskipTests > %ROOT_DIR%logs\backend.log 2>&1"
timeout /t 15 /nobreak >nul

:: Start admin
echo [3/3] Starting admin (port 5173) and frontend (port 3000)...
start /B cmd /c "cd /d %ROOT_DIR%admin && npx vite --port 5173 > %ROOT_DIR%logs\admin.log 2>&1"
start /B cmd /c "cd /d %ROOT_DIR%frontend && npx vite --port 3000 > %ROOT_DIR%logs\frontend.log 2>&1"
timeout /t 5 /nobreak >nul

echo.
echo =====================================
echo    All services started!
echo =====================================
echo.
echo   Frontend:  http://localhost:3000
echo   Admin:     http://localhost:5173
echo   Backend:   http://localhost:8080
echo.
echo   Admin:  admin / admin123
echo   User:   user1 / user123
echo.
echo   Press Ctrl+C to stop.
echo.
pause
