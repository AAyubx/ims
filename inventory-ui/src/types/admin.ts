export interface UserResponseDto {
  id: number;
  employeeCode?: string;
  email: string;
  displayName: string;
  status: 'ACTIVE' | 'INACTIVE';
  roles: RoleDto[];
  failedLoginAttempts: number;
  accountLockedUntil?: string;
  lastLoginAt?: string;
  passwordExpiresAt?: string;
  mustChangePassword: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RoleDto {
  id: number;
  code: string;
  name: string;
}

export interface CreateUserRequest {
  email: string;
  employeeCode?: string;
  displayName: string;
  roleIds: number[];
  initialPassword?: string;
  mustChangePassword: boolean;
}

export interface UpdateUserRequest {
  displayName?: string;
  employeeCode?: string;
  roleIds?: number[];
  status?: 'ACTIVE' | 'INACTIVE';
  mustChangePassword?: boolean;
}

export interface BulkActionRequest {
  userIds: number[];
  action: 'ACTIVATE' | 'DEACTIVATE' | 'RESET_PASSWORD';
}

export interface BulkActionResponse {
  totalRequested: number;
  successful: number;
  failed: number;
  errors: string[];
  passwordResets: PasswordResetInfo[];
}

export interface PasswordResetInfo {
  userId: number;
  email: string;
  temporaryPassword: string;
}

export interface UserSessionDto {
  id: string;
  ipAddress: string;
  userAgent: string;
  loginTime: string;
  lastActivity: string;
  active: boolean;
}

export interface UserFilters {
  search?: string;
  firstName?: string;
  emailAddress?: string;
  status?: 'ACTIVE' | 'INACTIVE';
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}