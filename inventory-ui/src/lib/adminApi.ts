import apiClient from './api';
import {
  UserResponseDto,
  CreateUserRequest,
  UpdateUserRequest,
  BulkActionRequest,
  BulkActionResponse,
  UserSessionDto,
  UserFilters,
  PaginatedResponse,
  RoleDto,
} from '@/types/admin';
import { ApiResponse } from '@/types/auth';

export class AdminUserAPI {
  static async getUsers(
    page = 0,
    size = 20,
    sortBy = 'createdAt',
    sortDir = 'desc',
    filters: UserFilters = {}
  ): Promise<ApiResponse<PaginatedResponse<UserResponseDto>>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir,
    });

    // Add filters
    if (filters.search) params.append('search', filters.search);
    if (filters.firstName) params.append('firstName', filters.firstName);
    if (filters.emailAddress) params.append('emailAddress', filters.emailAddress);
    if (filters.status) params.append('status', filters.status);

    const response = await apiClient.get(`/admin/users?${params}`);
    return response.data;
  }

  static async getUserById(id: number): Promise<ApiResponse<UserResponseDto>> {
    const response = await apiClient.get(`/admin/users/${id}`);
    return response.data;
  }

  static async createUser(request: CreateUserRequest): Promise<ApiResponse<any>> {
    // Transform frontend format to backend format
    const backendRequest = {
      employeeCode: request.employeeCode,
      email: request.email,
      displayName: request.displayName,
      roleIds: request.roleIds, // Backend expects Set<Long> but axios will serialize array correctly
      initialPassword: request.initialPassword || undefined,
      mustChangePassword: request.mustChangePassword ?? true
    };
    
    console.log('Sending create user request:', backendRequest);
    const response = await apiClient.post('/admin/users', backendRequest);
    return response.data;
  }

  static async updateUser(id: number, request: UpdateUserRequest): Promise<ApiResponse<UserResponseDto>> {
    const response = await apiClient.put(`/admin/users/${id}`, request);
    return response.data;
  }

  static async deactivateUser(id: number): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`/admin/users/${id}`);
    return response.data;
  }

  static async activateUser(id: number): Promise<ApiResponse<void>> {
    const response = await apiClient.post(`/admin/users/${id}/activate`);
    return response.data;
  }

  static async resetUserPassword(id: number): Promise<ApiResponse<any>> {
    const response = await apiClient.post(`/admin/users/${id}/reset-password`);
    return response.data;
  }

  static async unlockUserAccount(id: number): Promise<ApiResponse<void>> {
    const response = await apiClient.post(`/admin/users/${id}/unlock`);
    return response.data;
  }

  static async getUserSessions(id: number): Promise<ApiResponse<UserSessionDto[]>> {
    const response = await apiClient.get(`/admin/users/${id}/sessions`);
    return response.data;
  }

  static async terminateUserSession(userId: number, sessionId: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`/admin/users/${userId}/sessions/${sessionId}`);
    return response.data;
  }

  static async terminateAllUserSessions(id: number): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`/admin/users/${id}/sessions`);
    return response.data;
  }

  static async performBulkActions(request: BulkActionRequest): Promise<ApiResponse<BulkActionResponse>> {
    const response = await apiClient.post('/admin/users/bulk-actions', request);
    return response.data;
  }

  static async getRoles(): Promise<ApiResponse<RoleDto[]>> {
    const response = await apiClient.get('/admin/users/roles');
    return response.data;
  }
}