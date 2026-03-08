#!/bin/bash
# ============================================
# 母婴商城系统 — Mac 一键启动脚本
# ============================================

set -e

# ----------- 配置区 -----------
MYSQL_USER="root"
MYSQL_PASS="ab123168"
DB_NAME="babyshop"
BACKEND_PORT=8080
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SQL_DIR="$PROJECT_DIR/sql"
BACKEND_DIR="$PROJECT_DIR/backend"
ADMIN_DIR="$PROJECT_DIR/admin"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
BOLD='\033[1m'
NC='\033[0m'

# PID 追踪
BACKEND_PID=""
ADMIN_PID=""

# ============================================
# 清理函数（Ctrl+C 退出时调用）
# ============================================
cleanup() {
    echo ""
    echo -e "${YELLOW}┌─────────────────────────────────────────┐${NC}"
    echo -e "${YELLOW}│       正在关闭所有服务...               │${NC}"
    echo -e "${YELLOW}└─────────────────────────────────────────┘${NC}"

    if [ -n "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null && echo -e "${GREEN}  ✔ 后端服务已停止${NC}" || true
    fi
    if [ -n "$ADMIN_PID" ]; then
        kill $ADMIN_PID 2>/dev/null && echo -e "${GREEN}  ✔ 管理前端已停止${NC}" || true
    fi
    # 清理可能残留的 8080 端口进程
    lsof -ti :$BACKEND_PORT | xargs kill -9 2>/dev/null || true

    echo -e "${GREEN}${BOLD}  全部服务已关闭，再见！${NC}"
    exit 0
}

trap cleanup SIGINT SIGTERM

# ============================================
# 1. 依赖检查
# ============================================
echo ""
echo -e "${CYAN}${BOLD}╔═══════════════════════════════════════════╗${NC}"
echo -e "${CYAN}${BOLD}║       🍼 母婴商城系统 · 一键启动         ║${NC}"
echo -e "${CYAN}${BOLD}╚═══════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}[1/4] 检查运行环境...${NC}"

MISSING=0
for cmd in java mvn node pnpm mysql; do
    if command -v $cmd &>/dev/null; then
        VERSION=$($cmd --version 2>&1 | head -1)
        echo -e "  ${GREEN}✔${NC} $cmd  →  $VERSION"
    else
        echo -e "  ${RED}✘ 缺少依赖: $cmd${NC}"
        MISSING=1
    fi
done

if [ $MISSING -eq 1 ]; then
    echo -e "${RED}${BOLD}请先安装缺失的依赖后再运行此脚本！${NC}"
    exit 1
fi
echo ""

# ============================================
# 2. 清理旧端口
# ============================================
echo -e "${BLUE}[2/4] 清理旧进程...${NC}"
OLD_PID=$(lsof -ti :$BACKEND_PORT 2>/dev/null || true)
if [ -n "$OLD_PID" ]; then
    echo "$OLD_PID" | xargs kill -9 2>/dev/null || true
    echo -e "  ${YELLOW}⚠ 已清理端口 $BACKEND_PORT 上的旧进程${NC}"
else
    echo -e "  ${GREEN}✔ 端口 $BACKEND_PORT 已空闲${NC}"
fi
echo ""

# ============================================
# 3. 数据库导入
# ============================================
echo -e "${BLUE}[3/4] 检查数据库...${NC}"

# 检测数据库是否存在
DB_EXISTS=$(mysql -u"$MYSQL_USER" -p"$MYSQL_PASS" -e "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='$DB_NAME'" 2>/dev/null | grep "$DB_NAME" || true)

if [ -n "$DB_EXISTS" ]; then
    echo -e "  ${GREEN}✔ 数据库 '${DB_NAME}' 已存在，跳过导入${NC}"
else
    echo -e "  ${YELLOW}➜ 数据库 '${DB_NAME}' 不存在，开始导入...${NC}"

    # 导入结构
    if [ -f "$SQL_DIR/init.sql" ]; then
        mysql -u"$MYSQL_USER" -p"$MYSQL_PASS" < "$SQL_DIR/init.sql" 2>/dev/null
        echo -e "  ${GREEN}✔ 数据库结构导入完成 (init.sql)${NC}"
    else
        echo -e "  ${RED}✘ 找不到 sql/init.sql${NC}"
        exit 1
    fi

    # 导入数据
    if [ -f "$SQL_DIR/data.sql" ]; then
        mysql -u"$MYSQL_USER" -p"$MYSQL_PASS" < "$SQL_DIR/data.sql" 2>/dev/null
        echo -e "  ${GREEN}✔ 测试数据导入完成 (data.sql)${NC}"
    else
        echo -e "  ${RED}✘ 找不到 sql/data.sql${NC}"
        exit 1
    fi

    echo -e "  ${GREEN}${BOLD}✔ 数据库初始化完成！${NC}"
fi
echo ""

# ============================================
# 4. 启动服务
# ============================================
echo -e "${BLUE}[4/4] 启动服务...${NC}"

# --- 安装前端依赖 ---
if [ ! -d "$ADMIN_DIR/node_modules" ]; then
    echo -e "  ${YELLOW}➜ 安装前端依赖 (pnpm install)...${NC}"
    cd "$ADMIN_DIR" && pnpm install --frozen-lockfile 2>&1 | tail -3
    echo -e "  ${GREEN}✔ 前端依赖安装完成${NC}"
fi

# --- 启动后端 ---
echo -e "  ${CYAN}➜ 启动后端服务 (Spring Boot)...${NC}"
cd "$BACKEND_DIR" && mvn spring-boot:run -q 2>&1 &
BACKEND_PID=$!
echo -e "  ${GREEN}✔ 后端服务启动中 (PID: $BACKEND_PID)${NC}"

# --- 启动管理前端 ---
echo -e "  ${CYAN}➜ 启动管理前端 (Vite)...${NC}"
cd "$ADMIN_DIR" && pnpm dev 2>&1 &
ADMIN_PID=$!
echo -e "  ${GREEN}✔ 管理前端启动中 (PID: $ADMIN_PID)${NC}"

# --- 等待服务启动 ---
echo ""
echo -e "${YELLOW}  ⏳ 等待服务启动（约15秒）...${NC}"
sleep 15

# ============================================
# 信息面板
# ============================================
echo ""
echo -e "${CYAN}${BOLD}╔═══════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}${BOLD}║              🍼 母婴商城系统 · 启动完成              ║${NC}"
echo -e "${CYAN}${BOLD}╠═══════════════════════════════════════════════════════╣${NC}"
echo -e "${CYAN}${BOLD}║                                                       ║${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${BLUE}📡 后端 API :${NC}  http://localhost:8080               ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${BLUE}🖥  管理后台 :${NC}  http://localhost:3100               ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║                                                       ║${NC}"
echo -e "${CYAN}${BOLD}╠═══════════════════════════════════════════════════════╣${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${PURPLE}${BOLD}角色          用户名        密码${NC}                    ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}╠═══════════════════════════════════════════════════════╣${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${GREEN}👑 管理员${NC}     admin         admin123                ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${BLUE}👤 张妈妈${NC}     user1         user123                 ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${BLUE}👤 李爸爸${NC}     user2         user123                 ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${BLUE}👤 王妈妈${NC}     user3         user123                 ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${BLUE}👤 赵爸爸${NC}     user4         user123                 ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${BLUE}👤 刘妈妈${NC}     user5         user123                 ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${BLUE}👤 陈妈妈${NC}     user6         user123                 ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${BLUE}👤 孙爸爸${NC}     user7         user123                 ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${YELLOW}🚫 周妈妈${NC}     user8         user123 ${RED}(已禁用)${NC}       ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${BLUE}👤 吴妈妈${NC}     user9         user123                 ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}║                                                       ║${NC}"
echo -e "${CYAN}${BOLD}╠═══════════════════════════════════════════════════════╣${NC}"
echo -e "${CYAN}${BOLD}║${NC}  ${YELLOW}按 Ctrl+C 可安全退出所有服务${NC}                        ${CYAN}${BOLD}║${NC}"
echo -e "${CYAN}${BOLD}╚═══════════════════════════════════════════════════════╝${NC}"
echo ""

# ============================================
# 实时日志（等待子进程结束）
# ============================================
echo -e "${PURPLE}${BOLD}══════════════ 实时日志输出 ══════════════${NC}"
echo ""

wait $BACKEND_PID $ADMIN_PID 2>/dev/null
