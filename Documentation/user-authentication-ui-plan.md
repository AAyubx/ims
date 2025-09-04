# User Authentication UI Implementation Plan

_Last updated: 2025-09-04_

## Overview

This document outlines the comprehensive plan for implementing the user authentication UI for the Inventory Management System. The UI will integrate seamlessly with the existing Java Spring Boot backend authentication system, providing a modern, secure, and user-friendly interface for all authentication-related functionality.

---

# Backend Integration Analysis

## Current Authentication System

### Available Endpoints
- **Public Endpoints**: `/auth/login`, `/auth/forgot-password`, `/auth/reset-password`, `/auth/validate-token`
- **Authenticated Endpoints**: `/auth/logout`, `/auth/refresh`, `/auth/me`, `/auth/change-password`, `/auth/sessions`

### Authentication Features Implemented
- JWT token-based authentication (access + refresh tokens)
- Account lockout after failed attempts
- Password policy enforcement with history tracking
- Multi-tenant support
- Session management with concurrent session limits
- Comprehensive audit logging

### User Account Features
- **Fields**: email, employeeCode, displayName, status, roles, tenant
- **Security**: BCrypt password hashing, password expiry, failed login tracking
- **Roles**: ADMIN, MANAGER, CLERK, VIEWER with hierarchical permissions
- **Sessions**: IP tracking, user agent logging, session timeout

---

# UI Component Architecture

## Authentication Pages & Components

### 1. Login Page (`/login`)
**Route**: `/login`
**Component**: `LoginPage`
**Features**:
- Email/password form with validation
- "Remember me" functionality
- Forgot password link
- Account lockout messaging
- Loading states and error handling
- Tenant selection (if multi-tenant)
- Role-based redirect after login

**Form Fields**:
```typescript
interface LoginForm {
  email: string;           // Required, email validation
  password: string;        // Required, minimum length
  rememberMe: boolean;     // Optional, extends session
  tenantCode?: string;     // Optional, for multi-tenant
}
```

**Error Handling**:
- Invalid credentials
- Account locked (with unlock time)
- Password expired (redirect to change password)
- Tenant not found/inactive
- Rate limiting exceeded

### 2. Forgot Password Page (`/forgot-password`)
**Route**: `/forgot-password`
**Component**: `ForgotPasswordPage`
**Features**:
- Email input with validation
- Security message (no email enumeration)
- Success confirmation
- Back to login link

**Form Fields**:
```typescript
interface ForgotPasswordForm {
  email: string;          // Required, email validation
}
```

### 3. Reset Password Page (`/reset-password`)
**Route**: `/reset-password?token={token}`
**Component**: `ResetPasswordPage`
**Features**:
- Token validation on page load
- New password form with strength indicator
- Password policy compliance
- Success confirmation with login redirect

**Form Fields**:
```typescript
interface ResetPasswordForm {
  token: string;           // From URL parameter
  password: string;        // Required, policy validation
  confirmPassword: string; // Required, must match password
}
```

### 4. Change Password Page (`/profile/change-password`)
**Route**: `/profile/change-password`
**Component**: `ChangePasswordPage`
**Features**:
- Current password verification
- New password with strength indicator
- Password policy display
- Success notification
- Session invalidation option

**Form Fields**:
```typescript
interface ChangePasswordForm {
  currentPassword: string;    // Required
  newPassword: string;       // Required, policy validation
  confirmNewPassword: string; // Required, must match
  logoutAllSessions: boolean; // Optional
}
```

### 5. User Profile Page (`/profile`)
**Route**: `/profile`
**Component**: `UserProfilePage`
**Features**:
- User information display
- Active sessions management
- Password change access
- Account security status
- Role and permission display

## Shared Components

### 1. AuthLayout Component
**Component**: `AuthLayout`
**Features**:
- Consistent layout for all auth pages
- Branded header with logo
- Responsive design
- Loading overlay support
- Error boundary

### 2. PasswordStrengthIndicator Component
**Component**: `PasswordStrengthIndicator`
**Features**:
- Real-time password strength calculation
- Visual strength meter (weak/fair/good/strong)
- Policy requirement checklist
- Color-coded feedback

### 3. SessionManager Component
**Component**: `SessionManager`
**Features**:
- List of active sessions
- Session details (IP, browser, last activity)
- Terminate individual sessions
- Terminate all sessions except current
- Refresh session information

### 4. AccountSecurityStatus Component
**Component**: `AccountSecurityStatus`
**Features**:
- Password expiry warning
- Account lockout status
- Failed login attempts count
- Last login information
- Security recommendations

---

# State Management Architecture

## Authentication Store (Zustand)

### AuthStore Interface
```typescript
interface AuthState {
  // Authentication status
  isAuthenticated: boolean;
  isLoading: boolean;
  
  // User information
  user: UserInfo | null;
  token: string | null;
  refreshToken: string | null;
  
  // Session management
  sessions: UserSession[];
  currentSessionId: string | null;
  
  // Security status
  accountStatus: AccountStatus;
  passwordExpiresAt: Date | null;
  mustChangePassword: boolean;
  
  // Actions
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: (sessionId?: string) => Promise<void>;
  refreshTokens: () => Promise<void>;
  changePassword: (data: ChangePasswordData) => Promise<void>;
  loadSessions: () => Promise<void>;
  updateProfile: () => Promise<void>;
  clearAuth: () => void;
}
```

### User Information Types
```typescript
interface UserInfo {
  id: number;
  email: string;
  displayName: string;
  employeeCode?: string;
  roles: Role[];
  tenant: Tenant;
  lastLoginAt: string;
  accountStatus: UserStatus;
  passwordExpiresAt?: string;
  mustChangePassword: boolean;
}

interface Role {
  id: number;
  code: string;
  name: string;
  description: string;
}

interface Tenant {
  id: number;
  code: string;
  name: string;
  status: TenantStatus;
}

interface UserSession {
  id: string;
  ipAddress: string;
  userAgent: string;
  lastActivityAt: string;
  expiresAt: string;
  isCurrent: boolean;
}
```

## API Integration (TanStack Query)

### Authentication Mutations
```typescript
// Login mutation
const useLoginMutation = () => {
  return useMutation({
    mutationFn: authApi.login,
    onSuccess: (data) => {
      authStore.setAuthData(data);
      queryClient.invalidateQueries({ queryKey: ['user'] });
    },
    onError: handleAuthError,
  });
};

// Change password mutation
const useChangePasswordMutation = () => {
  return useMutation({
    mutationFn: authApi.changePassword,
    onSuccess: () => {
      toast.success('Password changed successfully');
      // Optionally logout other sessions
    },
    onError: handleAuthError,
  });
};

// Session management
const useSessionsQuery = () => {
  return useQuery({
    queryKey: ['sessions'],
    queryFn: authApi.getSessions,
    enabled: authStore.isAuthenticated,
    refetchInterval: 30000, // Refresh every 30 seconds
  });
};
```

### API Client Configuration
```typescript
// lib/api/auth.ts
class AuthAPI {
  private client = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
    timeout: 10000,
  });

  constructor() {
    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor - add JWT token
    this.client.interceptors.request.use((config) => {
      const token = authStore.token;
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Response interceptor - handle token refresh
    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        if (error.response?.status === 401) {
          try {
            await authStore.refreshTokens();
            return this.client.request(error.config);
          } catch {
            authStore.logout();
            router.push('/login');
          }
        }
        return Promise.reject(error);
      }
    );
  }

  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    const response = await this.client.post('/auth/login', credentials);
    return response.data;
  }

  async logout(sessionId?: string): Promise<void> {
    await this.client.post('/auth/logout', { sessionId });
  }

  async refreshToken(): Promise<TokenRefreshResponse> {
    const response = await this.client.post('/auth/refresh', {
      refreshToken: authStore.refreshToken,
    });
    return response.data;
  }

  // Additional methods for password management, sessions, etc.
}
```

---

# Form Validation & User Experience

## Password Policy Validation

### Zod Schema for Password Validation
```typescript
const passwordSchema = z
  .string()
  .min(8, 'Password must be at least 8 characters')
  .regex(/[A-Z]/, 'Password must contain uppercase letter')
  .regex(/[a-z]/, 'Password must contain lowercase letter')
  .regex(/[0-9]/, 'Password must contain number')
  .regex(/[^A-Za-z0-9]/, 'Password must contain special character');

const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(1, 'Password is required'),
  rememberMe: z.boolean().default(false),
});

const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Current password required'),
    newPassword: passwordSchema,
    confirmNewPassword: z.string(),
    logoutAllSessions: z.boolean().default(false),
  })
  .refine(data => data.newPassword === data.confirmNewPassword, {
    message: "Passwords don't match",
    path: ["confirmNewPassword"],
  });
```

### Real-time Validation Features
- **Email Format**: Instant email validation with visual feedback
- **Password Strength**: Real-time strength meter with policy requirements
- **Confirm Password**: Match validation with visual confirmation
- **Account Status**: Display lockout status and remaining time
- **Error Recovery**: Clear error messages with actionable suggestions

## Loading States & Error Handling

### Loading State Management
```typescript
interface LoadingState {
  login: boolean;
  logout: boolean;
  changePassword: boolean;
  resetPassword: boolean;
  loadingSessions: boolean;
}

// Component usage
const { mutate: login, isPending: isLoggingIn } = useLoginMutation();
```

### Error Handling Strategy
```typescript
interface AuthError {
  code: string;
  message: string;
  field?: string;
  details?: Record<string, any>;
}

const errorMessages = {
  INVALID_CREDENTIALS: 'Invalid email or password',
  ACCOUNT_LOCKED: 'Account is temporarily locked. Try again later.',
  PASSWORD_EXPIRED: 'Your password has expired. Please change it.',
  TOO_MANY_ATTEMPTS: 'Too many failed attempts. Please wait before trying again.',
  TOKEN_EXPIRED: 'Your session has expired. Please log in again.',
  WEAK_PASSWORD: 'Password does not meet security requirements',
};
```

---

# Security Implementation

## Token Management

### Secure Token Storage
```typescript
class TokenManager {
  private static readonly ACCESS_TOKEN_KEY = 'inventory_access_token';
  private static readonly REFRESH_TOKEN_KEY = 'inventory_refresh_token';
  private static readonly TOKEN_EXPIRY_KEY = 'inventory_token_expiry';

  static setTokens(accessToken: string, refreshToken: string, expiresIn: number) {
    const expiresAt = Date.now() + (expiresIn * 1000);
    
    localStorage.setItem(this.ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
    localStorage.setItem(this.TOKEN_EXPIRY_KEY, expiresAt.toString());
  }

  static getAccessToken(): string | null {
    const token = localStorage.getItem(this.ACCESS_TOKEN_KEY);
    const expiry = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
    
    if (!token || !expiry) return null;
    
    if (Date.now() > parseInt(expiry)) {
      this.clearTokens();
      return null;
    }
    
    return token;
  }

  static shouldRefresh(): boolean {
    const expiry = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
    if (!expiry) return false;
    
    const refreshMargin = 5 * 60 * 1000; // 5 minutes
    return Date.now() > (parseInt(expiry) - refreshMargin);
  }

  static clearTokens() {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.TOKEN_EXPIRY_KEY);
  }
}
```

## Route Protection

### Authentication Guards
```typescript
// components/guards/AuthGuard.tsx
interface AuthGuardProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  allowedRoles?: string[];
  fallback?: React.ReactNode;
}

const AuthGuard: React.FC<AuthGuardProps> = ({
  children,
  requireAuth = true,
  allowedRoles,
  fallback = <LoginRedirect />
}) => {
  const { isAuthenticated, user, isLoading } = useAuthStore();

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (requireAuth && !isAuthenticated) {
    return fallback;
  }

  if (allowedRoles && user && !hasAnyRole(user.roles, allowedRoles)) {
    return <UnauthorizedPage />;
  }

  return <>{children}</>;
};

// Usage in Next.js app directory
export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AuthGuard allowedRoles={['ADMIN', 'MANAGER', 'CLERK', 'VIEWER']}>
      {children}
    </AuthGuard>
  );
}
```

## Session Security

### Automatic Session Cleanup
```typescript
// hooks/useSessionCleanup.ts
export const useSessionCleanup = () => {
  const { logout, sessions } = useAuthStore();
  
  useEffect(() => {
    const cleanup = setInterval(() => {
      // Check for expired sessions
      const now = new Date();
      sessions.forEach(session => {
        if (new Date(session.expiresAt) <= now) {
          logout(session.id);
        }
      });
    }, 60000); // Check every minute

    return () => clearInterval(cleanup);
  }, [sessions, logout]);
};
```

---

# UI/UX Design Specifications

## Visual Design System

### Color Scheme for Authentication
- **Primary Blue**: `#0066CC` for login buttons and links
- **Success Green**: `#10B981` for successful actions
- **Warning Amber**: `#F59E0B` for password expiry warnings
- **Error Red**: `#EF4444` for validation errors and account lockout
- **Neutral Gray**: `#64748B` for secondary text and borders

### Typography & Spacing
- **Page Titles**: 24px semibold, 32px line height
- **Form Labels**: 14px medium, 20px line height
- **Input Text**: 16px regular, 24px line height (prevents zoom on iOS)
- **Error Messages**: 14px regular, red color
- **Help Text**: 12px regular, gray color

### Component Dimensions
- **Input Fields**: 48px height for touch accessibility
- **Buttons**: 48px height, 12px border radius
- **Form Spacing**: 24px between form groups
- **Page Margins**: 24px on mobile, 32px on desktop

## Responsive Design

### Breakpoint Specifications
```css
/* Mobile: 320px - 767px */
.auth-container {
  padding: 16px;
  max-width: 100%;
}

/* Tablet: 768px - 1023px */
@media (min-width: 768px) {
  .auth-container {
    padding: 32px;
    max-width: 480px;
    margin: 0 auto;
  }
}

/* Desktop: 1024px+ */
@media (min-width: 1024px) {
  .auth-container {
    padding: 48px;
    max-width: 560px;
  }
}
```

### Mobile-First Considerations
- **Touch Targets**: Minimum 44px for buttons and input fields
- **Keyboard Behavior**: Proper focus management and input types
- **Viewport Meta**: Prevents zoom on input focus
- **Loading States**: Clear visual feedback for slow connections

## Accessibility Features

### WCAG 2.1 AA Compliance
- **Keyboard Navigation**: Full keyboard accessibility for all forms
- **Screen Reader Support**: Proper ARIA labels and announcements
- **Color Contrast**: 4.5:1 minimum ratio for all text
- **Focus Indicators**: Clear visual focus states
- **Error Identification**: Descriptive error messages linked to form fields

### Implementation Details
```tsx
// Example: Accessible form input
<div className="form-group">
  <label htmlFor="email" className="form-label">
    Email Address
  </label>
  <input
    id="email"
    type="email"
    className="form-input"
    aria-describedby="email-error"
    aria-invalid={!!errors.email}
    autoComplete="email"
    {...register('email')}
  />
  {errors.email && (
    <div id="email-error" className="form-error" role="alert">
      {errors.email.message}
    </div>
  )}
</div>
```

---

# Implementation Roadmap

## Phase 1: Core Authentication (Week 1-2)

### Sprint 1.1: Foundation Setup
- [ ] Create Next.js project with TypeScript
- [ ] Install and configure dependencies (TanStack Query, Zustand, Axios)
- [ ] Set up project structure and component organization
- [ ] Configure ESLint, Prettier, and pre-commit hooks
- [ ] Create authentication API client with interceptors

### Sprint 1.2: Login & Logout
- [ ] Implement login page with form validation
- [ ] Create authentication store with Zustand
- [ ] Set up JWT token management and storage
- [ ] Implement automatic token refresh
- [ ] Create logout functionality with session cleanup
- [ ] Add loading states and error handling

### Sprint 1.3: Route Protection
- [ ] Create AuthGuard component for protected routes
- [ ] Implement role-based access control
- [ ] Set up automatic redirects for unauthorized access
- [ ] Create login redirect with return URL handling
- [ ] Add session timeout with automatic logout

## Phase 2: Password Management (Week 3)

### Sprint 2.1: Password Policies
- [ ] Implement password strength indicator component
- [ ] Create password policy validation with Zod
- [ ] Add real-time password strength feedback
- [ ] Display password policy requirements
- [ ] Implement password history prevention

### Sprint 2.2: Password Operations
- [ ] Create change password page and functionality
- [ ] Implement forgot password flow
- [ ] Create reset password page with token validation
- [ ] Add password expiry warnings and enforcement
- [ ] Implement forced password change on login

## Phase 3: Session Management (Week 4)

### Sprint 3.1: Session Tracking
- [ ] Create user profile page with session information
- [ ] Implement active sessions display component
- [ ] Add session details (IP, browser, last activity)
- [ ] Create session termination functionality
- [ ] Add bulk session termination option

### Sprint 3.2: Security Features
- [ ] Implement account lockout status display
- [ ] Add failed login attempt tracking
- [ ] Create security status dashboard
- [ ] Implement suspicious activity alerts
- [ ] Add last login information display

## Phase 4: Polish & Testing (Week 5)

### Sprint 4.1: UI/UX Refinement
- [ ] Implement responsive design for all screen sizes
- [ ] Add micro-animations and loading states
- [ ] Create consistent error messaging system
- [ ] Implement accessibility features (ARIA, keyboard navigation)
- [ ] Add internationalization support structure

### Sprint 4.2: Testing & Documentation
- [ ] Write unit tests for all components
- [ ] Create E2E tests for authentication flows
- [ ] Add integration tests for API interactions
- [ ] Write component documentation and stories
- [ ] Perform security audit and penetration testing

---

# Testing Strategy

## Unit Testing with Vitest

### Component Testing
```typescript
// tests/components/LoginForm.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { LoginForm } from '@/components/auth/LoginForm';

describe('LoginForm', () => {
  it('validates email format', async () => {
    render(<LoginForm />);
    
    const emailInput = screen.getByLabelText(/email/i);
    fireEvent.change(emailInput, { target: { value: 'invalid-email' } });
    fireEvent.blur(emailInput);
    
    await waitFor(() => {
      expect(screen.getByText(/invalid email address/i)).toBeInTheDocument();
    });
  });

  it('displays account lockout message', () => {
    const error = { code: 'ACCOUNT_LOCKED', message: 'Account is locked' };
    render(<LoginForm error={error} />);
    
    expect(screen.getByText(/account is locked/i)).toBeInTheDocument();
  });
});
```

### Store Testing
```typescript
// tests/stores/authStore.test.ts
import { authStore } from '@/stores/authStore';

describe('AuthStore', () => {
  beforeEach(() => {
    authStore.clearAuth();
  });

  it('sets authentication state on successful login', () => {
    const mockUser = { id: 1, email: 'test@example.com' };
    const mockToken = 'mock-jwt-token';
    
    authStore.setAuthData({ user: mockUser, token: mockToken });
    
    expect(authStore.isAuthenticated).toBe(true);
    expect(authStore.user).toEqual(mockUser);
    expect(authStore.token).toBe(mockToken);
  });
});
```

## E2E Testing with Playwright

### Authentication Flow Tests
```typescript
// tests/e2e/auth.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test('successful login redirects to dashboard', async ({ page }) => {
    await page.goto('/login');
    
    await page.fill('[data-testid=email-input]', 'admin@example.com');
    await page.fill('[data-testid=password-input]', 'validPassword123!');
    await page.click('[data-testid=login-button]');
    
    await expect(page).toHaveURL('/dashboard');
    await expect(page.locator('[data-testid=user-menu]')).toBeVisible();
  });

  test('handles account lockout gracefully', async ({ page }) => {
    // Simulate multiple failed login attempts
    await page.goto('/login');
    
    for (let i = 0; i < 5; i++) {
      await page.fill('[data-testid=email-input]', 'user@example.com');
      await page.fill('[data-testid=password-input]', 'wrongPassword');
      await page.click('[data-testid=login-button]');
      await page.waitForTimeout(1000);
    }
    
    await expect(page.locator('[data-testid=lockout-message]')).toBeVisible();
    await expect(page.locator('[data-testid=login-button]')).toBeDisabled();
  });
});
```

## API Mocking with MSW

### Mock Handlers
```typescript
// mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.post('/api/v1/auth/login', async ({ request }) => {
    const { email, password } = await request.json();
    
    if (email === 'admin@example.com' && password === 'password123') {
      return HttpResponse.json({
        success: true,
        data: {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token',
          user: {
            id: 1,
            email: 'admin@example.com',
            displayName: 'Admin User',
            roles: [{ code: 'ADMIN', name: 'Administrator' }],
          },
        },
      });
    }
    
    return HttpResponse.json(
      {
        success: false,
        error: {
          code: 'INVALID_CREDENTIALS',
          message: 'Invalid email or password',
        },
      },
      { status: 401 }
    );
  }),
  
  // Additional handlers for other endpoints
];
```

---

# Performance Optimization

## Code Splitting Strategy

### Route-Level Splitting
```typescript
// app/login/page.tsx
import { lazy, Suspense } from 'react';

const LoginPage = lazy(() => import('@/components/auth/LoginPage'));

export default function Login() {
  return (
    <Suspense fallback={<AuthLoadingSkeleton />}>
      <LoginPage />
    </Suspense>
  );
}
```

### Component-Level Splitting
```typescript
// Lazy load heavy components
const SessionManager = lazy(() => import('./SessionManager'));
const PasswordStrengthIndicator = lazy(() => import('./PasswordStrengthIndicator'));
```

## Caching Strategy

### API Response Caching
```typescript
const useUserProfileQuery = () => {
  return useQuery({
    queryKey: ['user', 'profile'],
    queryFn: authAPI.getProfile,
    staleTime: 5 * 60 * 1000,    // 5 minutes
    gcTime: 10 * 60 * 1000,      // 10 minutes
    retry: 2,
  });
};
```

### Image and Asset Optimization
```typescript
// next.config.js
const nextConfig = {
  images: {
    domains: ['your-cdn-domain.com'],
    formats: ['image/webp', 'image/avif'],
  },
  experimental: {
    optimizePackageImports: ['lucide-react'],
  },
};
```

---

# Security Considerations

## Input Sanitization

### XSS Prevention
```typescript
import DOMPurify from 'dompurify';

const sanitizeInput = (input: string): string => {
  return DOMPurify.sanitize(input, {
    ALLOWED_TAGS: [],
    ALLOWED_ATTR: [],
  });
};

// Usage in form handling
const handleInput = (value: string) => {
  const sanitizedValue = sanitizeInput(value);
  setValue(sanitizedValue);
};
```

### CSRF Protection
```typescript
// API client with CSRF token
const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
  withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
  const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
  if (csrfToken) {
    config.headers['X-CSRF-Token'] = csrfToken;
  }
  return config;
});
```

## Content Security Policy

### CSP Configuration
```typescript
// next.config.js
const nextConfig = {
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'Content-Security-Policy',
            value: [
              "default-src 'self'",
              "script-src 'self' 'unsafe-inline' 'unsafe-eval'",
              "style-src 'self' 'unsafe-inline'",
              "img-src 'self' data: https:",
              "connect-src 'self' " + process.env.NEXT_PUBLIC_API_BASE_URL,
              "font-src 'self' data:",
            ].join('; '),
          },
        ],
      },
    ];
  },
};
```

---

# Deployment & Production Readiness

## Environment Configuration

### Production Environment Variables
```bash
# .env.production
NEXT_PUBLIC_API_BASE_URL=https://api.inventory.company.com/v1
NEXT_PUBLIC_APP_ENV=production
NEXT_PUBLIC_JWT_REFRESH_MARGIN=300000
NEXT_PUBLIC_SESSION_TIMEOUT=28800000
NEXT_PUBLIC_SENTRY_DSN=https://your-sentry-dsn
```

### Build Optimization
```json
{
  "scripts": {
    "build": "next build",
    "start": "next start",
    "build:analyze": "ANALYZE=true npm run build",
    "build:production": "NODE_ENV=production npm run build"
  }
}
```

## Monitoring & Analytics

### Error Tracking with Sentry
```typescript
// lib/sentry.ts
import * as Sentry from '@sentry/nextjs';

Sentry.init({
  dsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
  environment: process.env.NEXT_PUBLIC_APP_ENV,
  integrations: [
    new Sentry.BrowserTracing(),
  ],
  tracesSampleRate: process.env.NODE_ENV === 'production' ? 0.1 : 1.0,
});

// Usage in authentication flows
const handleLoginError = (error: AuthError) => {
  Sentry.captureException(error, {
    tags: {
      section: 'authentication',
      action: 'login',
    },
    user: {
      email: formData.email,
    },
  });
};
```

### Performance Monitoring
```typescript
// lib/analytics.ts
export const trackAuthEvent = (event: string, properties: Record<string, any>) => {
  if (process.env.NODE_ENV === 'production') {
    // Track authentication events
    analytics.track(event, {
      ...properties,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
    });
  }
};

// Usage
trackAuthEvent('login_success', {
  method: 'email_password',
  hasRememberMe: formData.rememberMe,
});
```

---

## Success Metrics

### Key Performance Indicators
- **Login Success Rate**: > 98%
- **Page Load Time**: < 2 seconds on 3G
- **Form Completion Rate**: > 95%
- **Error Recovery Rate**: > 80%
- **Accessibility Score**: 100% WCAG 2.1 AA compliance

### Security Metrics
- **Account Lockout Rate**: < 1% of login attempts
- **Password Strength Compliance**: > 95%
- **Session Security**: Zero session hijacking incidents
- **XSS/CSRF Prevention**: 100% protection rate

---

This comprehensive UI plan ensures the authentication system provides enterprise-grade security while maintaining exceptional user experience. The implementation will be fully integrated with the existing Java Spring Boot backend and follow modern React development best practices.