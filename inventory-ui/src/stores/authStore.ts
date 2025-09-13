import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { LoginCredentials, UserInfo, AuthError, ChangePasswordRequest } from '@/types/auth';
import { AuthAPI } from '@/lib/api';

interface AuthState {
  // Authentication status
  isAuthenticated: boolean;
  isLoading: boolean;
  
  // User information
  user: UserInfo | null;
  token: string | null;
  refreshToken: string | null;
  
  // Password change requirements
  mustChangePassword: boolean;
  passwordExpiresAt: string | null;
  
  // Error handling
  error: AuthError | null;
  
  // Actions
  login: (credentials: LoginCredentials) => Promise<{ mustChangePassword: boolean }>;
  logout: () => Promise<void>;
  changePassword: (request: ChangePasswordRequest) => Promise<void>;
  loadCurrentUser: () => Promise<void>;
  clearError: () => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      isAuthenticated: false,
      isLoading: false,
      user: null,
      token: null,
      refreshToken: null,
      mustChangePassword: false,
      passwordExpiresAt: null,
      error: null,

      // Login action
      login: async (credentials: LoginCredentials) => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await AuthAPI.login(credentials);
          
          if (response.success) {
            const { accessToken, refreshToken, user, mustChangePassword, passwordExpiresAt } = response.data;
            
            // Store tokens in localStorage
            localStorage.setItem('inventory_access_token', accessToken);
            localStorage.setItem('inventory_refresh_token', refreshToken);
            
            set({
              isAuthenticated: true,
              token: accessToken,
              refreshToken: refreshToken,
              user: user,
              mustChangePassword: mustChangePassword || false,
              passwordExpiresAt: passwordExpiresAt || null,
              isLoading: false,
              error: null,
            });
            
            return { mustChangePassword: mustChangePassword || false };
          } else {
            throw new Error('Login failed');
          }
        } catch (error: any) {
          const authError: AuthError = {
            code: error.response?.data?.error?.code || 'LOGIN_FAILED',
            message: error.response?.data?.error?.message || 'Invalid email or password',
          };
          
          set({
            isAuthenticated: false,
            token: null,
            refreshToken: null,
            user: null,
            mustChangePassword: false,
            passwordExpiresAt: null,
            isLoading: false,
            error: authError,
          });
          
          throw authError;
        }
      },

      // Change password action
      changePassword: async (request: ChangePasswordRequest) => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await AuthAPI.changePassword(request);
          
          if (response.success) {
            // Password changed successfully, clear mustChangePassword flag
            set({
              mustChangePassword: false,
              isLoading: false,
              error: null,
            });
          } else {
            throw new Error('Password change failed');
          }
        } catch (error: any) {
          const authError: AuthError = {
            code: error.response?.data?.error?.code || 'CHANGE_PASSWORD_FAILED',
            message: error.response?.data?.error?.message || 'Failed to change password',
          };
          
          set({
            isLoading: false,
            error: authError,
          });
          
          throw authError;
        }
      },

      // Logout action
      logout: async () => {
        set({ isLoading: true });
        
        try {
          await AuthAPI.logout();
        } catch (error) {
          // Continue with logout even if API call fails
          console.error('Logout API call failed:', error);
        } finally {
          // Always clear local state and storage
          localStorage.removeItem('inventory_access_token');
          localStorage.removeItem('inventory_refresh_token');
          
          set({
            isAuthenticated: false,
            token: null,
            refreshToken: null,
            user: null,
            mustChangePassword: false,
            passwordExpiresAt: null,
            isLoading: false,
            error: null,
          });
        }
      },

      // Clear error
      clearError: () => set({ error: null }),

      // Load current user
      loadCurrentUser: async () => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await AuthAPI.getCurrentUser();
          
          if (response.success && response.data) {
            set({
              user: response.data,
              isLoading: false,
              error: null,
            });
          } else {
            throw new Error('Failed to load user data');
          }
        } catch (error: any) {
          console.error('Load current user failed:', error);
          
          // If loading user fails, token might be invalid
          const authError: AuthError = {
            code: 'USER_LOAD_FAILED',
            message: 'Failed to load user information',
          };
          
          set({
            isLoading: false,
            error: authError,
          });
          
          throw authError;
        }
      },

      // Clear auth state
      clearAuth: () => {
        localStorage.removeItem('inventory_access_token');
        localStorage.removeItem('inventory_refresh_token');
        
        set({
          isAuthenticated: false,
          token: null,
          refreshToken: null,
          user: null,
          mustChangePassword: false,
          passwordExpiresAt: null,
          isLoading: false,
          error: null,
        });
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ 
        isAuthenticated: state.isAuthenticated,
        user: state.user,
        token: state.token,
        refreshToken: state.refreshToken,
        mustChangePassword: state.mustChangePassword,
        passwordExpiresAt: state.passwordExpiresAt,
      }),
    }
  )
);