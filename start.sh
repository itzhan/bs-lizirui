#!/bin/bash
# 母婴乐园商城 — 一键启动脚本 (macOS)
set -e
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'

echo -e "${CYAN}=====================================${NC}"
echo -e "${CYAN}   🍼 母婴乐园商城 — 一键启动      ${NC}"
echo -e "${CYAN}=====================================${NC}"

# ---------- 1. 环境检测 ----------
check_cmd() { command -v "$1" &>/dev/null; }

echo -e "\n${YELLOW}[1/5] 检测环境...${NC}"
for cmd in java mvn node pnpm mysql; do
  if check_cmd $cmd; then
    echo -e "  ✅ $cmd: $(command $cmd --version 2>&1 | head -1)"
  else
    echo -e "  ${RED}❌ $cmd 未安装${NC}"
    if [ "$cmd" = "pnpm" ]; then
      echo -e "  ${YELLOW}正在安装 pnpm...${NC}"
      npm install -g pnpm
    else
      echo -e "  ${RED}请先安装 $cmd 再运行此脚本${NC}"
      exit 1
    fi
  fi
done

# ---------- 2. 数据库检测与初始化 ----------
echo -e "\n${YELLOW}[2/5] 检测数据库...${NC}"
DB_EXISTS=$(mysql -u root -pab123168 -e "SHOW DATABASES LIKE 'babyshop';" 2>/dev/null | grep babyshop || true)
if [ -z "$DB_EXISTS" ]; then
  echo -e "  📦 数据库不存在，正在初始化..."
  mysql -u root -pab123168 < "$ROOT_DIR/sql/init.sql"
  mysql -u root -pab123168 babyshop < "$ROOT_DIR/sql/data.sql"
  echo -e "  ${GREEN}✅ 数据库初始化完成${NC}"
else
  echo -e "  ✅ 数据库 babyshop 已存在"
fi

# ---------- 3. 端口清理 ----------
echo -e "\n${YELLOW}[3/5] 清理端口...${NC}"
for port in 8080 5173 3000; do
  pid=$(lsof -ti :$port 2>/dev/null || true)
  if [ -n "$pid" ]; then
    echo -e "  ⚠️  端口 $port 被占用 (PID: $pid)，正在终止..."
    kill -9 $pid 2>/dev/null || true
  fi
done
echo -e "  ✅ 端口已清理"

# ---------- 4. 安装依赖 ----------
echo -e "\n${YELLOW}[4/5] 安装依赖...${NC}"
if [ ! -d "$ROOT_DIR/admin/node_modules" ]; then
  echo -e "  📦 安装 admin 依赖..."
  (cd "$ROOT_DIR/admin" && pnpm install --frozen-lockfile 2>/dev/null || pnpm install)
fi
if [ ! -d "$ROOT_DIR/frontend/node_modules" ]; then
  echo -e "  📦 安装 frontend 依赖..."
  (cd "$ROOT_DIR/frontend" && pnpm install)
fi
echo -e "  ✅ 依赖已就绪"

# ---------- 5. 启动服务 ----------
echo -e "\n${YELLOW}[5/5] 启动服务...${NC}"

# 后端
echo -e "  🚀 启动后端 (port 8080)..."
(cd "$ROOT_DIR/backend" && ./mvnw spring-boot:run -DskipTests > "$ROOT_DIR/logs/backend.log" 2>&1) &
BACKEND_PID=$!

# 等待后端就绪
echo -e "  ⏳ 等待后端启动..."
for i in $(seq 1 30); do
  if curl -s http://localhost:8080/api/banners > /dev/null 2>&1; then
    echo -e "  ${GREEN}✅ 后端已就绪${NC}"
    break
  fi
  sleep 2
done

# Admin
echo -e "  🚀 启动管理后台 (port 5173)..."
(cd "$ROOT_DIR/admin" && npx vite --port 5173 > "$ROOT_DIR/logs/admin.log" 2>&1) &
ADMIN_PID=$!

# Frontend
echo -e "  🚀 启动用户前端 (port 3000)..."
(cd "$ROOT_DIR/frontend" && npx vite --port 3000 > "$ROOT_DIR/logs/frontend.log" 2>&1) &
FRONTEND_PID=$!

sleep 3

echo -e "\n${GREEN}=====================================${NC}"
echo -e "${GREEN}   🎉 所有服务已启动！              ${NC}"
echo -e "${GREEN}=====================================${NC}"
echo -e ""
echo -e "  📱 用户前端:   ${CYAN}http://localhost:3000${NC}"
echo -e "  🔧 管理后台:   ${CYAN}http://localhost:5173${NC}"
echo -e "  🖥️  后端API:    ${CYAN}http://localhost:8080${NC}"
echo -e ""
echo -e "  ${YELLOW}测试账号：${NC}"
echo -e "  ┌──────────┬──────────┬──────────┐"
echo -e "  │  角色    │  用户名  │  密码    │"
echo -e "  ├──────────┼──────────┼──────────┤"
echo -e "  │  管理员  │  admin   │ admin123 │"
echo -e "  │  用户    │  user1   │ user123  │"
echo -e "  └──────────┴──────────┴──────────┘"
echo -e ""
echo -e "  ${YELLOW}按 Ctrl+C 停止所有服务${NC}"
echo -e ""

# 保存PID
echo "$BACKEND_PID $ADMIN_PID $FRONTEND_PID" > "$ROOT_DIR/logs/.pids"

# 等待退出
trap "echo -e '\n${YELLOW}正在停止服务...${NC}'; kill $BACKEND_PID $ADMIN_PID $FRONTEND_PID 2>/dev/null; echo -e '${GREEN}已停止${NC}'; exit 0" INT TERM
wait
