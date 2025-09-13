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
    mustChangePassword?: boolean;
    passwordExpiresAt?: string;
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

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
  logoutAllSessions?: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: {
    code: string;
    message: string;
  };
}