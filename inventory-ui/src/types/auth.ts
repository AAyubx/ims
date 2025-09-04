export interface LoginCredentials {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResponse {
  success: boolean;
  data: {
    accessToken: string;
    refreshToken: string;
    expiresIn: number;
    user: UserInfo;
    sessionId: string;
  };
}

export interface UserInfo {
  id: number;
  email: string;
  displayName: string;
  employeeCode?: string;
  roles: Role[];
  tenant: Tenant;
  lastLoginAt?: string;
  accountStatus: string;
}

export interface Role {
  id: number;
  code: string;
  name: string;
  description: string;
}

export interface Tenant {
  id: number;
  code: string;
  name: string;
  status: string;
}

export interface AuthError {
  code: string;
  message: string;
  field?: string;
  details?: Record<string, any>;
}