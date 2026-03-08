import { Login } from "@/api/interface/index";
import authMenuList from "@/assets/json/authMenuList.json";
import authButtonList from "@/assets/json/authButtonList.json";
import http from "@/api";

/**
 * @name 登录模块
 */
// 用户登录
export const loginApi = (params: Login.ReqLoginForm) => {
  return http.post<Login.ResLogin>(`/api/auth/login`, params, { loading: false });
};

// 获取菜单列表 — 使用本地 JSON 菜单
export const getAuthMenuListApi = () => {
  // return http.get<Menu.MenuOptions[]>(`/api/menu/list`, {}, { loading: false });
  return authMenuList;
};

// 获取按钮权限 — 使用本地 JSON 权限
export const getAuthButtonListApi = () => {
  // return http.get<Login.ResAuthButtons>(`/api/auth/buttons`, {}, { loading: false });
  return authButtonList;
};

// 用户退出登录
export const logoutApi = () => {
  // 后端无 logout 接口，前端清除 token 即可
  return Promise.resolve();
};

// 获取用户信息
export const getUserInfoApi = () => {
  return http.get<Login.ResUserInfo>(`/api/user/info`, {}, { loading: false });
};
