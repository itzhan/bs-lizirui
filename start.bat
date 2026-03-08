@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

:: ============================================
:: 母婴商城系统 - Windows 一键启动脚本
:: ============================================

echo.
echo ╔═══════════════════════════════════════════╗
echo ║       母婴商城系统 · 一键启动 (Win)      ║
echo ╚═══════════════════════════════════════════╝
echo.

:: ----------- 获取脚本所在目录 -----------
set "PROJECT_DIR=%~dp0"

:: ============================================
:: 1. 清理旧进程
:: ============================================
echo [1/3] 清理旧进程...
taskkill /F /IM "java.exe" /T >nul 2>&1
if %errorlevel%==0 (
    echo   √ 已清理旧的 Java 进程
) else (
    echo   √ 无需清理
)
echo.

:: ============================================
:: 2. 安装前端依赖（如需要）
:: ============================================
echo [2/3] 检查前端依赖...
if not exist "%PROJECT_DIR%admin\node_modules" (
    echo   → 安装前端依赖 (pnpm install)...
    cd /d "%PROJECT_DIR%admin" && pnpm install --frozen-lockfile
    echo   √ 前端依赖安装完成
) else (
    echo   √ 前端依赖已安装
)
echo.

:: ============================================
:: 3. 启动服务
:: ============================================
echo [3/3] 启动服务...

:: --- 启动后端 ---
echo   → 启动后端服务 (Spring Boot)...
start "BabyShop-Backend" cmd /k "cd /d %PROJECT_DIR%backend && mvn spring-boot:run"
echo   √ 后端服务窗口已打开

:: --- 启动管理前端 ---
echo   → 启动管理前端 (Vite)...
start "BabyShop-Admin" cmd /k "cd /d %PROJECT_DIR%admin && pnpm dev"
echo   √ 管理前端窗口已打开

:: ============================================
:: 信息面板
:: ============================================
echo.
echo ═══════════════════════════════════════════════════════
echo         母婴商城系统 · 启动完成
echo ═══════════════════════════════════════════════════════
echo.
echo   后端 API :  http://localhost:8080
echo   管理后台 :  http://localhost:3100
echo.
echo ───────────────────────────────────────────────────────
echo   角色          用户名        密码
echo ───────────────────────────────────────────────────────
echo   管理员        admin         admin123
echo   张妈妈        user1         user123
echo   李爸爸        user2         user123
echo   王妈妈        user3         user123
echo   赵爸爸        user4         user123
echo   刘妈妈        user5         user123
echo   陈妈妈        user6         user123
echo   孙爸爸        user7         user123
echo   周妈妈        user8         user123 (已禁用)
echo   吴妈妈        user9         user123
echo ───────────────────────────────────────────────────────
echo.
echo   提示: 后端和前端已在独立窗口中启动
echo   关闭对应窗口即可停止对应服务
echo.
echo ═══════════════════════════════════════════════════════
echo.

pause
