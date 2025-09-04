# UI Tech Stack — Modern Store Inventory Management System
_Last updated: 2025-09-03_

## Frontend Framework
- **React 18.x** with TypeScript for type safety and developer experience
- **Next.js 14.x** for SSR/SSG, API routes, and optimized performance
- **Tailwind CSS 3.x** for utility-first styling and consistent design system

## State Management & Data Fetching
- **TanStack Query (React Query) v5** for server state management and caching
- **Zustand** for lightweight client-side state management
- **React Hook Form** with **Zod** for form validation and type-safe schemas

## UI Component Library
- **Shadcn/ui** built on Radix UI primitives for accessibility-first components
- **Lucide React** for consistent icon system
- **React Hot Toast** for notifications and alerts

## Development Tools & Build System
- **Vite 5.x** for fast development server and optimized builds
- **ESLint** with TypeScript rules and React hooks plugin
- **Prettier** for code formatting consistency
- **Husky** with **lint-staged** for pre-commit hooks

## Testing Framework
- **Vitest** for unit testing (faster Jest alternative)
- **React Testing Library** for component testing
- **Playwright** for E2E testing across browsers
- **MSW (Mock Service Worker)** for API mocking during development

## Performance & Monitoring
- **React DevTools** for debugging and performance profiling
- **Lighthouse CI** for automated performance auditing
- **Sentry** for error tracking and performance monitoring
- **Web Vitals** monitoring integration

## Authentication & Security
- **NextAuth.js** for authentication with OIDC/OAuth2 support
- **JWT** token handling with automatic refresh
- **HTTPS** enforcement and secure headers configuration

---

## Prerequisites

### System Requirements
- **Node.js 20.x LTS** (recommended via nvm/fnm)
- **npm 10.x** or **pnpm 9.x** (preferred for faster installs)
- **Git 2.40+**

### VS Code Setup

#### Required Extensions
```json
{
  "recommendations": [
    "bradlc.vscode-tailwindcss",
    "ms-vscode.vscode-typescript-next",
    "esbenp.prettier-vscode",
    "ms-playwright.playwright",
    "ms-vscode.vscode-eslint",
    "usernamehw.errorlens",
    "ms-vscode.vscode-json",
    "formulahendry.auto-rename-tag",
    "christian-kohler.path-intellisense",
    "ms-vscode.vscode-thunder-client"
  ]
}
```

#### VS Code Settings Configuration
Add to your workspace `.vscode/settings.json`:

```json
{
  "typescript.preferences.preferTypeOnlyAutoImports": true,
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": "explicit",
    "source.organizeImports": "explicit"
  },
  "tailwindCSS.experimental.classRegex": [
    "cn\\(([^)]*)\\)",
    "cva\\(([^)]*)\\)"
  ],
  "typescript.preferences.includePackageJsonAutoImports": "auto",
  "emmet.includeLanguages": {
    "typescript": "html",
    "typescriptreact": "html"
  },
  "files.associations": {
    "*.css": "tailwindcss"
  },
  "editor.quickSuggestions": {
    "strings": true
  }
}
```

#### Debugging Configuration
Add to `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Next.js: debug server-side",
      "type": "node",
      "request": "attach",
      "port": 9229,
      "restart": true,
      "cwd": "${workspaceFolder}"
    },
    {
      "name": "Next.js: debug client-side",
      "type": "chrome",
      "request": "launch",
      "url": "http://localhost:3000"
    }
  ]
}
```

---

## Project Setup Commands

### Initial Setup
```bash
# Create Next.js project with TypeScript
npx create-next-app@latest inventory-ui --typescript --tailwind --eslint --app

# Navigate to project
cd inventory-ui

# Install additional dependencies
npm install @tanstack/react-query @tanstack/react-query-devtools
npm install zustand react-hook-form @hookform/resolvers zod
npm install @radix-ui/react-toast @radix-ui/react-dialog @radix-ui/react-dropdown-menu
npm install lucide-react react-hot-toast
npm install class-variance-authority clsx tailwind-merge

# Install dev dependencies
npm install -D @types/node
npm install -D vitest @vitest/ui @testing-library/react @testing-library/jest-dom
npm install -D playwright @playwright/test
npm install -D msw
npm install -D husky lint-staged
npm install -D @next/bundle-analyzer
```

### Development Scripts
Add to `package.json`:

```json
{
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "lint:fix": "next lint --fix",
    "type-check": "tsc --noEmit",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:e2e": "playwright test",
    "test:e2e:ui": "playwright test --ui",
    "analyze": "ANALYZE=true npm run build"
  }
}
```

---

## Architecture Patterns

### Component Organization
```
src/
├── app/                    # Next.js 14 app directory
│   ├── (auth)/            # Route groups for auth pages
│   ├── dashboard/         # Main application routes
│   ├── globals.css        # Global styles and Tailwind imports
│   └── layout.tsx         # Root layout
├── components/
│   ├── ui/                # Shadcn/ui components
│   ├── forms/             # Form components
│   ├── charts/            # Data visualization
│   └── layout/            # Navigation and layout components
├── lib/
│   ├── api.ts             # API client configuration
│   ├── auth.ts            # Authentication utilities
│   ├── utils.ts           # Shared utilities
│   └── validations.ts     # Zod schemas
├── hooks/                 # Custom React hooks
├── stores/                # Zustand stores
└── types/                 # TypeScript type definitions
```

### API Integration Pattern
```typescript
// lib/api.ts
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
  timeout: 10000,
});

// hooks/use-inventory.ts
export const useInventoryQuery = (params: InventoryParams) => {
  return useQuery({
    queryKey: ['inventory', params],
    queryFn: () => api.get('/inventory', { params }),
    staleTime: 30000,
  });
};
```

---

## Performance Optimization

### Bundle Optimization
- Code splitting with dynamic imports for routes and heavy components
- Image optimization with Next.js Image component
- Font optimization using next/font
- Bundle analyzer for monitoring build size

### Runtime Performance
- React.memo for expensive components
- useMemo/useCallback for complex computations
- Virtual scrolling for large data tables
- Progressive loading for dashboard widgets

### Caching Strategy
- TanStack Query for API response caching
- Next.js static generation for reports and analytics
- Service worker for offline inventory scanning

---

## Development Workflow

### Quality Gates
1. TypeScript compilation must pass
2. ESLint rules must pass (zero warnings in production)
3. Unit tests must achieve 80%+ coverage for utilities and hooks
4. E2E tests must pass for critical user journeys
5. Lighthouse performance score > 90

### Code Review Checklist
- [ ] TypeScript strict mode compliance
- [ ] Accessibility requirements met (ARIA labels, keyboard navigation)
- [ ] Performance impact assessed (bundle size, runtime performance)
- [ ] Security considerations reviewed (XSS prevention, input validation)
- [ ] Error boundaries and loading states implemented
- [ ] Responsive design tested on mobile and desktop

### Environment Configuration
```bash
# .env.local
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_ENV=development
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=your-development-secret
```

---

## Integration with Java Backend

### API Communication
- RESTful API calls using axios with automatic retry logic
- WebSocket connections for real-time inventory updates
- Server-Sent Events for notifications and alerts
- File upload handling for bulk imports (CSV, Excel)

### Authentication Flow
1. OIDC/OAuth2 integration with Java Spring Security backend
2. JWT tokens stored securely in httpOnly cookies
3. Automatic token refresh handling
4. Role-based UI component rendering

### Error Handling
- Global error boundary for unexpected errors
- API error standardization with backend error response format
- User-friendly error messages with action suggestions
- Retry mechanisms for transient failures

This tech stack prioritizes developer experience, type safety, performance, and maintainability while ensuring the UI can handle the complexity of a world-class inventory management system.