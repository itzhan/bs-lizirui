@echo off
chcp 65001 >nul
title 母婴乐园商城 — Docker 启动

echo =====================================
echo    母婴乐园商城 — Docker 一键启动
echo =====================================
echo.

docker compose up -d --build

echo.
echo =====================================
echo    所有服务已启动！
echo =====================================
echo.
echo   用户前端:  http://localhost:3000
echo   管理后台:  http://localhost:8848
echo   后端API:   http://localhost:8080
echo.
echo   管理员:  admin / admin123
echo   用户:    user1 / user123
echo.
pause
