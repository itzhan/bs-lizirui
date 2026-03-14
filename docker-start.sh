#!/bin/bash
# ============================================
# 母婴乐园商城 — Docker 一键启动脚本
# ============================================
# 解决中文路径导致 Docker BuildKit gRPC 报错的问题
# 原理：创建 ASCII 路径的软链接，在软链路径下执行 docker compose
set -e

CYAN='\033[0;36m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
LINK_PATH="$HOME/.babyshop-docker-build"

echo -e "${CYAN}=====================================${NC}"
echo -e "${CYAN}   🍼 母婴乐园商城 — Docker 启动     ${NC}"
echo -e "${CYAN}=====================================${NC}"

# 1. 创建 ASCII 路径软链（避免中文路径 gRPC 报错）
echo -e "\n${YELLOW}[1/3] 准备构建环境...${NC}"
ln -sfn "$ROOT_DIR" "$LINK_PATH"
echo -e "  ✅ 构建路径就绪: $LINK_PATH"

# 2. 构建并启动
echo -e "\n${YELLOW}[2/3] 构建并启动 Docker 容器...${NC}"
echo -e "  ⏳ 首次构建可能需要 5-10 分钟，请耐心等待..."
cd "$LINK_PATH"
docker compose up -d --build

# 3. 等待服务就绪
echo -e "\n${YELLOW}[3/3] 等待服务就绪...${NC}"
echo -e "  ⏳ 等待后端启动 (最多 90 秒)..."
for i in $(seq 1 18); do
  if curl -s http://localhost:8080/api/banners > /dev/null 2>&1; then
    echo -e "  ${GREEN}✅ 后端已就绪${NC}"
    break
  fi
  if [ $i -eq 18 ]; then
    echo -e "  ${YELLOW}⚠️  后端尚未完全启动，请稍后手动检查${NC}"
    echo -e "  ${YELLOW}   运行 docker compose logs backend 查看日志${NC}"
  fi
  sleep 5
done

echo -e "\n${GREEN}=====================================${NC}"
echo -e "${GREEN}   🎉 Docker 启动完成！              ${NC}"
echo -e "${GREEN}=====================================${NC}"
echo -e ""
echo -e "  📱 用户前端:   ${CYAN}http://localhost:3000${NC}"
echo -e "  🔧 管理后台:   ${CYAN}http://localhost:8848${NC}"
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
echo -e "  ${YELLOW}常用命令：${NC}"
echo -e "  停止服务:  cd $LINK_PATH && docker compose down"
echo -e "  查看日志:  cd $LINK_PATH && docker compose logs -f"
echo -e "  清除数据:  cd $LINK_PATH && docker compose down -v"
echo -e ""
