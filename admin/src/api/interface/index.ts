// 请求响应参数（不包含data）
export interface Result {
  code: number;
  message: string;
}

// 请求响应参数（包含data）
export interface ResultData<T = any> extends Result {
  data: T;
}

// 分页响应参数 — 对齐后端 PageResult
export interface ResPage<T> {
  records: T[];
  total: number;
  page: number;
  size: number;
}

// 分页请求参数
export interface ReqPage {
  page: number;
  size: number;
}

// 文件上传模块
export namespace Upload {
  export interface ResFileUrl {
    fileUrl: string;
  }
}

// 登录模块
export namespace Login {
  export interface ReqLoginForm {
    username: string;
    password: string;
  }
  export interface ResLogin {
    token: string;
    user: ResUserInfo;
  }
  export interface ResUserInfo {
    id: number;
    username: string;
    nickname: string;
    phone: string;
    email: string;
    avatar: string;
    gender: number;
    role: string;
    status: number;
  }
  export interface ResAuthButtons {
    [key: string]: string[];
  }
}
