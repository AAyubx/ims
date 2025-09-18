import axios from "axios";
import { LoginCredentials, LoginResponse, ForgotPasswordRequest, ResetPasswordRequest, ChangePasswordRequest, ApiResponse } from "@/types/auth";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8082/api";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("inventory_access_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem("inventory_access_token");
      localStorage.removeItem("inventory_refresh_token");
      // Redirect to login will be handled by the auth store
    }
    return Promise.reject(error);
  }
);

export class AuthAPI {
  static async login(credentials: LoginCredentials): Promise<LoginResponse> {
    const response = await apiClient.post("/auth/login", credentials);
    return response.data;
  }

  static async logout(): Promise<void> {
    await apiClient.post("/auth/logout");
  }

  static async getCurrentUser() {
    const response = await apiClient.get("/auth/me");
    return response.data;
  }

  static async forgotPassword(request: ForgotPasswordRequest): Promise<ApiResponse<void>> {
    const response = await apiClient.post("/auth/forgot-password", request);
    return response.data;
  }

  static async validateResetToken(token: string): Promise<ApiResponse<void>> {
    const response = await apiClient.post("/auth/validate-token", { token });
    return response.data;
  }

  static async resetPassword(request: ResetPasswordRequest): Promise<ApiResponse<void>> {
    const response = await apiClient.post("/auth/reset-password", request);
    return response.data;
  }

  static async changePassword(request: ChangePasswordRequest): Promise<ApiResponse<void>> {
    const response = await apiClient.post("/auth/change-password", request);
    return response.data;
  }
}

export default apiClient;
