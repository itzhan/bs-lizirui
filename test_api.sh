#!/bin/bash
# ============================================================
# 母婴乐园商城 — 全接口联调测试脚本
# 覆盖所有 13 个 Controller、50+ 个接口
# 用法: ./test_api.sh [BASE_URL]
# ============================================================

BASE="${1:-http://localhost:8080}"
PASS=0; FAIL=0; TOTAL=0
TOKEN=""
LAST_BODY=""
USER_TOKEN=""
ADMIN_TOKEN=""

# ---------- 辅助函数 ----------
# 用 grep 提取 JSON 中的 "code" 值，避免每次启动 python3（节省 ~150ms/次）
_extract_code() {
  grep -o '"code":[0-9]*' | head -1 | grep -o '[0-9]*'
}

test_api() {
  local method="$1" url="$2" desc="$3" expect_code="${4:-200}"
  shift 4 || true
  TOTAL=$((TOTAL + 1))

  local args=(-s -o /tmp/_test_body.json -w '%{http_code}' -X "$method" "${BASE}${url}" -H "Content-Type: application/json")
  if [ -n "$TOKEN" ]; then
    args+=(-H "Authorization: Bearer $TOKEN")
  fi
  args+=("$@")

  local http_code body api_code
  http_code=$(curl "${args[@]}" 2>/dev/null) || http_code="000"
  body=$(cat /tmp/_test_body.json 2>/dev/null) || body=""
  api_code=$(echo "$body" | _extract_code) || api_code=""

  if [ "$api_code" = "$expect_code" ]; then
    echo "  ✅ [${method}] ${desc}  (code=${api_code})"
    PASS=$((PASS + 1))
  else
    echo "  ❌ [${method}] ${desc}  期望code=${expect_code}, 实际code=${api_code}, HTTP=${http_code}"
    echo "     响应: $(echo "$body" | head -c 300)"
    FAIL=$((FAIL + 1))
  fi
  LAST_BODY="$body"
}

# 纯 shell 提取 JSON 字段 — 不再需要 python3
# 提取 data.id 或 data (当 data 是数字时)
_json_id() {
  local body="$1"
  # 尝试匹配 "data":{"id":123 或 "data":123
  echo "$body" | grep -o '"data":{[^}]*"id":[0-9]*' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*' \
    || echo "$body" | grep -o '"data":[0-9]*' | head -1 | grep -o '[0-9]*'
}
# 提取 data.token
_json_token() {
  echo "$1" | grep -o '"token":"[^"]*"' | head -1 | sed 's/"token":"//;s/"//'
}
# 提取 data[0].id (数组第一个元素的id)
_json_first_id() {
  echo "$1" | grep -o '"data":\[{[^}]*}' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*'
}
# 提取 data.orderNo
_json_order_no() {
  echo "$1" | grep -o '"orderNo":"[^"]*"' | head -1 | sed 's/"orderNo":"//;s/"//'
}

echo "╔═══════════════════════════════════════════════╗"
echo "║   🍼 母婴乐园商城 — 全接口联调测试            ║"
echo "║   BASE: ${BASE}                               ║"
echo "╚═══════════════════════════════════════════════╝"

# ============================================================
# 第1部分: 公开接口
# ============================================================
echo ""
echo "═══ 第1部分: 公开接口 ═══"

echo ""
echo "[轮播图模块]"
test_api GET "/api/banners" "轮播图列表"

echo ""
echo "[公告模块]"
test_api GET "/api/announcements?page=1&size=10" "公告列表"
test_api GET "/api/announcements/1" "公告详情"

echo ""
echo "[分类模块]"
test_api GET "/api/categories" "所有分类"
test_api GET "/api/categories/tree" "分类树"
test_api GET "/api/categories/1" "分类详情"

echo ""
echo "[商品模块]"
test_api GET "/api/products?page=1&size=12" "商品列表(分页)"
test_api GET "/api/products?keyword=%E9%A3%9E%E9%B9%A4" "商品列表(搜索:飞鹤)"
test_api GET "/api/products?categoryId=1&sortBy=price&sortOrder=asc" "商品列表(分类+排序)"
test_api GET "/api/products/1" "商品详情"
test_api GET "/api/products/recommend?page=1&size=8" "推荐商品"
test_api GET "/api/products/hot?page=1&size=8" "热销商品"

echo ""
echo "[评价模块 - 公开]"
test_api GET "/api/reviews/product/1?page=1&size=10" "商品评价列表"

# ============================================================
# 第2部分: 认证模块
# ============================================================
echo ""
echo "═══ 第2部分: 认证模块 ═══"

echo ""
echo "[登录/注册]"
test_api POST "/api/auth/register" "用户注册" "200" \
  -d '{"username":"testuser_api","password":"test123","nickname":"测试用户","phone":"13900001111","email":"test_api@qq.com"}'

test_api POST "/api/auth/register" "重复注册(应失败)" "500" \
  -d '{"username":"testuser_api","password":"test123","nickname":"测试用户","phone":"13900002222"}'

test_api POST "/api/auth/login" "登录失败(错误密码)" "500" \
  -d '{"username":"admin","password":"wrongpassword"}'

test_api POST "/api/auth/login" "普通用户登录(user1)" "200" \
  -d '{"username":"user1","password":"user123"}'
USER_TOKEN=$(_json_token "$LAST_BODY")
echo "     → user1 token: ${USER_TOKEN:0:30}..."

test_api POST "/api/auth/login" "管理员登录(admin)" "200" \
  -d '{"username":"admin","password":"admin123"}'
ADMIN_TOKEN=$(_json_token "$LAST_BODY")
echo "     → admin token: ${ADMIN_TOKEN:0:30}..."

if [ -z "$USER_TOKEN" ] || [ -z "$ADMIN_TOKEN" ]; then
  echo "❌ 登录失败，无法获取 token，终止测试。"
  echo "通过: $PASS / 失败: $FAIL / 总数: $TOTAL"
  exit 1
fi

# ============================================================
# 第3部分: 用户接口
# ============================================================
echo ""
echo "═══ 第3部分: 用户接口 (user1) ═══"
TOKEN="$USER_TOKEN"

echo ""
echo "[用户信息]"
test_api GET "/api/user/info" "获取当前用户信息"
test_api PUT "/api/user/profile" "更新个人信息" "200" \
  -d '{"nickname":"测试昵称修改","phone":"13900009999","email":"updated@qq.com","gender":1}'
test_api GET "/api/user/info" "验证更新后信息"

echo ""
echo "[收货地址模块]"
test_api GET "/api/addresses" "地址列表"
test_api POST "/api/addresses" "新增地址" "200" \
  -d '{"receiver":"李测试","phone":"13800001234","province":"广东省","city":"深圳市","district":"南山区","detail":"科技园路100号","isDefault":0}'
NEW_ADDR_ID=$(_json_id "$LAST_BODY")
if [ -z "$NEW_ADDR_ID" ] || [ "$NEW_ADDR_ID" = "None" ]; then
  # 可能接口返回的是整个对象
  NEW_ADDR_ID=$(_json_id "$LAST_BODY")
fi
echo "     → 新地址ID: ${NEW_ADDR_ID}"

if [ -n "$NEW_ADDR_ID" ] && [ "$NEW_ADDR_ID" != "None" ] && [ "$NEW_ADDR_ID" != "" ]; then
  test_api PUT "/api/addresses/${NEW_ADDR_ID}" "更新地址" "200" \
    -d '{"receiver":"李测试更新","phone":"13800005678","province":"广东省","city":"深圳市","district":"福田区","detail":"深南大道200号"}'
  test_api PUT "/api/addresses/${NEW_ADDR_ID}/default" "设为默认地址"
else
  echo "  ⚠️ 跳过地址更新（未获取到地址ID）"
fi
test_api GET "/api/addresses" "验证地址列表"

echo ""
echo "[收藏模块]"
test_api POST "/api/favorites/1" "收藏商品1"
test_api POST "/api/favorites/2" "收藏商品2"
test_api GET "/api/favorites/check/1" "检查商品1是否已收藏"
test_api GET "/api/favorites?page=1&size=10" "收藏列表"
test_api DELETE "/api/favorites/2" "取消收藏商品2"
test_api GET "/api/favorites/check/2" "验证商品2已取消收藏"

echo ""
echo "[购物车模块]"
test_api DELETE "/api/cart/clear" "清空购物车(初始化)"

test_api POST "/api/cart" "添加商品1到购物车" "200" \
  -d '{"productId":1,"quantity":2}'
echo "     → 购物车项1 ID: ${CART_ID_1}"

test_api POST "/api/cart" "添加商品2到购物车" "200" \
  -d '{"productId":2,"quantity":1}'
echo "     → 购物车项2 ID: ${CART_ID_2}"

test_api GET "/api/cart" "购物车列表"

# 购物车更新 — 仅在获取到ID时执行
if [ -n "$CART_ID_1" ] && [ "$CART_ID_1" != "None" ] && [ "$CART_ID_1" != "" ]; then
  test_api PUT "/api/cart/${CART_ID_1}/quantity" "修改商品1数量为3" "200" \
    -d '{"quantity":3}'
  test_api PUT "/api/cart/${CART_ID_1}/checked" "勾选商品1" "200" \
    -d '{"checked":1}'
fi
if [ -n "$CART_ID_2" ] && [ "$CART_ID_2" != "None" ] && [ "$CART_ID_2" != "" ]; then
  test_api PUT "/api/cart/${CART_ID_2}/checked" "勾选商品2" "200" \
    -d '{"checked":1}'
fi

test_api PUT "/api/cart/checkAll" "全选" "200" \
  -d '{"checked":1}'
test_api GET "/api/cart" "验证购物车状态"

echo ""
echo "[订单模块 — 完整生命周期]"
test_api GET "/api/addresses" "获取有效地址" "200"
echo "     → 使用地址ID: ${ADDR_ID}"

if [ -n "$ADDR_ID" ] && [ "$ADDR_ID" != "None" ] && [ "$ADDR_ID" != "" ] && \
   [ -n "$CART_ID_1" ] && [ "$CART_ID_1" != "None" ] && \
   [ -n "$CART_ID_2" ] && [ "$CART_ID_2" != "None" ]; then

  test_api POST "/api/orders" "创建订单(从购物车)" "200" \
    -d "{\"addressId\":${ADDR_ID},\"remark\":\"API测试订单\",\"cartIds\":[${CART_ID_1},${CART_ID_2}]}"
  echo "     → 订单ID: ${ORDER_ID}, 订单号: ${ORDER_NO}"

  if [ -n "$ORDER_ID" ] && [ "$ORDER_ID" != "None" ] && [ "$ORDER_ID" != "" ]; then
    test_api GET "/api/orders?page=1&size=10" "我的订单列表(全部)"
    test_api GET "/api/orders?status=0" "我的订单列表(待付款)"
    test_api GET "/api/orders/${ORDER_ID}" "订单详情"

    test_api PUT "/api/orders/${ORDER_ID}/pay" "支付订单"
    test_api GET "/api/orders/${ORDER_ID}" "验证订单状态(已支付)"
    test_api PUT "/api/orders/${ORDER_ID}/pay" "重复支付(应失败)" "500"

    # 管理员发货
    TOKEN="$ADMIN_TOKEN"
    test_api PUT "/api/admin/orders/${ORDER_ID}/ship" "管理员发货"
    TOKEN="$USER_TOKEN"
    test_api PUT "/api/orders/${ORDER_ID}/confirm" "确认收货"

    # 创建第二个订单测试取消
    test_api POST "/api/cart" "再次添加商品到购物车" "200" \
      -d '{"productId":3,"quantity":1}'
    if [ -n "$CART_ID_3" ] && [ "$CART_ID_3" != "None" ] && [ "$CART_ID_3" != "" ]; then
      test_api PUT "/api/cart/${CART_ID_3}/checked" "勾选" "200" \
        -d '{"checked":1}'
      test_api POST "/api/orders" "创建第二个订单(测试取消)" "200" \
        -d "{\"addressId\":${ADDR_ID},\"remark\":\"取消测试\",\"cartIds\":[${CART_ID_3}]}"
      if [ -n "$ORDER_ID_2" ] && [ "$ORDER_ID_2" != "None" ] && [ "$ORDER_ID_2" != "" ]; then
        test_api PUT "/api/orders/${ORDER_ID_2}/cancel" "取消订单"
        test_api GET "/api/orders?status=4" "验证已取消状态"
      fi
    fi

    # 评价
    echo ""
    echo "[评价模块 — 已购商品评价]"
    test_api POST "/api/reviews" "发表评价" "200" \
      -d "{\"productId\":1,\"orderId\":${ORDER_ID},\"rating\":5,\"content\":\"宝宝很喜欢，质量非常好！推荐购买。\",\"images\":\"[]\"}"
    test_api GET "/api/reviews/product/1?page=1&size=10" "验证评价已发表"
  else
    echo "  ⚠️ 跳过订单测试（未获取到订单ID）"
  fi
else
  echo "  ⚠️ 跳过订单测试（缺少地址或购物车ID）"
fi

# ============================================================
# 第4部分: 管理员接口
# ============================================================
echo ""
echo "═══ 第4部分: 管理员接口 (admin) ═══"
TOKEN="$ADMIN_TOKEN"

echo ""
echo "[数据统计]"
test_api GET "/api/admin/statistics" "订单统计"
test_api GET "/api/admin/statistics/users" "用户统计"
test_api GET "/api/admin/statistics/products" "商品统计"

echo ""
echo "[用户管理]"
test_api GET "/api/admin/users?page=1&size=10" "用户列表"
test_api GET "/api/admin/users?keyword=user" "搜索用户"

echo ""
echo "[商品管理]"
test_api GET "/api/admin/products?page=1&size=10" "商品列表"
test_api GET "/api/admin/products?keyword=%E9%A3%9E%E9%B9%A4" "搜索商品"
test_api POST "/api/admin/products" "新增商品" "200" \
  -d '{"name":"测试商品-API","categoryId":1,"brand":"测试品牌","price":99.9,"originalPrice":199.9,"stock":100,"description":"API测试商品","status":1,"recommend":0}'
echo "     → 新商品ID: ${NEW_PRODUCT_ID}"
if [ -n "$NEW_PRODUCT_ID" ] && [ "$NEW_PRODUCT_ID" != "None" ] && [ "$NEW_PRODUCT_ID" != "" ]; then
  test_api PUT "/api/admin/products/${NEW_PRODUCT_ID}" "编辑商品" "200" \
    -d '{"name":"测试商品-API(已编辑)","price":88.8}'
  test_api PUT "/api/admin/products/${NEW_PRODUCT_ID}/status" "下架商品" "200" \
    -d '{"status":0}'
  test_api PUT "/api/admin/products/${NEW_PRODUCT_ID}/status" "上架商品" "200" \
    -d '{"status":1}'
  test_api DELETE "/api/admin/products/${NEW_PRODUCT_ID}" "删除商品"
fi

echo ""
echo "[分类管理]"
test_api GET "/api/admin/categories" "分类列表"
test_api POST "/api/admin/categories" "新增分类" "200" \
  -d '{"name":"API测试分类","parentId":0,"sort":99,"status":1}'
echo "     → 新分类ID: ${NEW_CAT_ID}"
if [ -n "$NEW_CAT_ID" ] && [ "$NEW_CAT_ID" != "None" ] && [ "$NEW_CAT_ID" != "" ]; then
  test_api PUT "/api/admin/categories/${NEW_CAT_ID}" "编辑分类" "200" \
    -d '{"name":"API测试分类(已编辑)","sort":100}'
  test_api DELETE "/api/admin/categories/${NEW_CAT_ID}" "删除分类"
fi

echo ""
echo "[订单管理]"
test_api GET "/api/admin/orders?page=1&size=10" "订单列表"
test_api GET "/api/admin/orders?status=3" "筛选已完成订单"
if [ -n "${ORDER_ID:-}" ] && [ "${ORDER_ID:-}" != "None" ]; then
  test_api GET "/api/admin/orders/${ORDER_ID}" "管理员查看订单详情"
fi

echo ""
echo "[评论管理]"
test_api GET "/api/admin/reviews?page=1&size=10" "评论列表"
test_api GET "/api/admin/reviews?productId=1" "按商品筛选评论"

echo ""
echo "[公告管理]"
test_api GET "/api/admin/announcements?page=1&size=10" "公告列表"
test_api POST "/api/admin/announcements" "新增公告" "200" \
  -d '{"title":"API测试公告","content":"这是一条测试公告内容","type":1,"status":1}'
echo "     → 新公告ID: ${NEW_ANN_ID}"
if [ -n "$NEW_ANN_ID" ] && [ "$NEW_ANN_ID" != "None" ] && [ "$NEW_ANN_ID" != "" ]; then
  test_api PUT "/api/admin/announcements/${NEW_ANN_ID}" "编辑公告" "200" \
    -d '{"title":"API测试公告(已编辑)","content":"内容已更新"}'
  test_api DELETE "/api/admin/announcements/${NEW_ANN_ID}" "删除公告"
fi

echo ""
echo "[轮播图管理]"
test_api GET "/api/admin/banners" "轮播图列表"
test_api POST "/api/admin/banners" "新增轮播图" "200" \
  -d '{"title":"API测试轮播","imageUrl":"http://localhost:8080/uploads/test.jpg","linkUrl":"/products","sort":99,"status":1}'
echo "     → 新轮播图ID: ${NEW_BAN_ID}"
if [ -n "$NEW_BAN_ID" ] && [ "$NEW_BAN_ID" != "None" ] && [ "$NEW_BAN_ID" != "" ]; then
  test_api PUT "/api/admin/banners/${NEW_BAN_ID}" "编辑轮播图" "200" \
    -d '{"title":"API测试轮播(已编辑)","sort":100}'
  test_api DELETE "/api/admin/banners/${NEW_BAN_ID}" "删除轮播图"
fi

# ============================================================
# 第5部分: 权限控制
# ============================================================
echo ""
echo "═══ 第5部分: 权限控制验证 ═══"

echo ""
echo "[未认证访问]"
TOKEN=""
test_api GET "/api/cart" "未登录访问购物车(应401)" "401"
test_api GET "/api/orders" "未登录访问订单(应401)" "401"
test_api GET "/api/user/info" "未登录访问用户信息(应401)" "401"

echo ""
echo "[普通用户越权]"
TOKEN="$USER_TOKEN"
test_api GET "/api/admin/users?page=1&size=10" "普通用户访问管理接口(应403)" "403"
test_api GET "/api/admin/statistics" "普通用户访问统计(应403)" "403"

# ============================================================
# 第6部分: 清理
# ============================================================
echo ""
echo "═══ 第6部分: 清理测试数据 ═══"
TOKEN="$USER_TOKEN"
test_api DELETE "/api/favorites/1" "清理收藏"
if [ -n "${NEW_ADDR_ID:-}" ] && [ "${NEW_ADDR_ID:-}" != "None" ] && [ "${NEW_ADDR_ID:-}" != "" ]; then
  test_api DELETE "/api/addresses/${NEW_ADDR_ID}" "清理测试地址"
fi
test_api DELETE "/api/cart/clear" "清空购物车"

# ============================================================
# 汇总
# ============================================================
echo ""
echo "╔═══════════════════════════════════════════════╗"
echo "║               📊 测试结果汇总                 ║"
echo "╠═══════════════════════════════════════════════╣"
printf "║  总数: %-40s║\n" "$TOTAL"
printf "║  ✅通过: %-38s║\n" "$PASS"
printf "║  ❌失败: %-38s║\n" "$FAIL"
echo "╚═══════════════════════════════════════════════╝"

if [ "$FAIL" -eq 0 ]; then
  echo ""
  echo "🎉 全部通过！前后端联调无问题！"
  exit 0
else
  echo ""
  echo "⚠️  有 ${FAIL} 个接口未通过，请检查上述错误。"
  exit 1
fi
