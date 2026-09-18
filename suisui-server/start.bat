@echo off
chcp 65001 >nul
title 碎碎念 服务端

echo ========================================
echo   碎碎念 Suisui - Spring Boot 服务端
echo ========================================
echo.

echo [1/3] 检查端口占用...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    set PID=%%a
)
if defined PID (
    echo [提示] 端口 8080 已被进程 PID=%PID% 占用，正在关闭...
    powershell -Command "Stop-Process -Id %PID% -Force" 2>nul
    timeout /t 2 /nobreak >nul
    echo [OK] 旧进程已关闭
) else (
    echo [OK] 端口 8080 空闲
)

echo.
echo [2/3] 检查 MySQL 是否在运行...
tasklist /fi "imagename eq mysqld.exe" 2>nul | find /i "mysqld.exe" >nul
if %errorlevel% neq 0 (
    echo [失败] 未检测到 MySQL 进程，请确认 MySQL 已启动！
    echo.
    pause
    exit /b 1
)
echo [OK] MySQL 正在运行

echo.
echo [3/3] 启动 Spring Boot 服务...
echo.
echo   服务地址: http://localhost:8080
echo   真机调试：用 ipconfig 查看电脑局域网 IP，填到 local.properties 的 suisui.serverUrl
echo.
echo   按 Ctrl+C 停止服务
echo ========================================
echo.

java -jar target/suisui-server-1.0.0.jar
pause
