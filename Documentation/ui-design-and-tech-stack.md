# UI Design Guidelines and Technology Stack

_Last updated: 2025-09-16_

## Overview

This document provides comprehensive guidelines for the user interface design and frontend technology stack for the modern Inventory Management System. It covers design principles, component specifications, development setup, and integration patterns with the Java backend.

---

# Design Principles and Guidelines

## Core Design Philosophy

### Design Principles
- **Speed First**: Every interaction should feel instantaneous with sub-200ms perceived performance
- **Cognitive Load Reduction**: Minimize decision fatigue through progressive disclosure and smart defaults
- **Task-Oriented Design**: Workflows match real-world inventory operations exactly
- **Error Prevention**: Proactive validation and clear feedback prevent costly mistakes

### User Experience Pillars
1. **Efficiency**: Common tasks require minimal clicks and keystrokes
2. **Clarity**: Information hierarchy guides users naturally through complex workflows
3. **Consistency**: Predictable patterns reduce learning curve
4. **Accessibility**: Usable by all team members regardless of technical expertise

## Layout & Navigation

### Dashboard Layout Structure
- **Left Sidebar Navigation**: Fixed, collapsible, with role-based menu items
- **Top Bar**: Global search, notifications, user menu, and breadcrumbs
- **Main Content**: Context-aware with consistent spacing and typography
- **Action Bar**: Floating or sticky for primary actions in current context

### Navigation Hierarchy
```
Dashboard (Overview)
├── Inventory
│   ├── Stock Levels
│   ├── Movements
│   ├── Adjustments
│   └── Cycle Counts
├── Catalog
│   ├── Items
│   ├── Categories
│   └── Attributes
├── Purchasing
│   ├── Purchase Orders
│   ├── Suppliers
│   └── Receipts
├── Orders
│   ├── Active Orders
│   ├── Reservations
│   └── Fulfillment
├── Reports
│   ├── Analytics
│   ├── KPIs
│   └── Exports
└── Admin
    ├── Locations
    ├── Users
    └── Settings
```

### Responsive Design Breakpoints
- **Desktop**: 1440px+ (primary focus for power users)
- **Tablet**: 768px-1439px (management and reporting)
- **Mobile**: 320px-767px (scanning and basic operations)

## Component Design System

### Color Palette
- **Primary**: Blue (#0066CC) for actions and navigation
- **Secondary**: Gray (#64748B) for secondary actions
- **Success**: Green (#10B981) for confirmations and positive states
- **Warning**: Amber (#F59E0B) for caution and pending states
- **Danger**: Red (#EF4444) for errors and destructive actions
- **Neutral**: Gray scale (#F8FAFC to #0F172A) for text and backgrounds

### Typography System
- **Headings**: Inter font family, semibold (600)
- **Body Text**: Inter font family, regular (400)
- **Code/Data**: JetBrains Mono for SKUs, quantities, and technical data
- **Scale**: 12px (caption) → 14px (body) → 16px (heading) → 20px+ (page titles)

### Spacing System
- **Base Unit**: 4px
- **Common Spacings**: 8px, 12px, 16px, 24px, 32px, 48px, 64px
- **Component Padding**: 12px-16px internal, 24px between sections
- **Page Margins**: 24px minimum, 32px preferred

## Interaction Patterns

### Speed Optimizations
- **Keyboard Shortcuts**: Global hotkeys for common actions (Ctrl+K for search, Ctrl+N for new item)
- **Bulk Operations**: Multi-select with batch actions for efficiency
- **Smart Autocomplete**: Predictive search with fuzzy matching for SKUs and names
- **Instant Feedback**: Optimistic updates with rollback on error
- **Prefetching**: Load likely next actions and data in background

### Data Entry Patterns
- **Smart Forms**: Auto-focus, tab order, enter-to-submit
- **Inline Editing**: Click-to-edit for quick updates
- **Barcode Scanning**: Camera integration with manual fallback
- **Bulk Import**: Drag-and-drop CSV/Excel with validation preview
- **Templates**: Pre-filled forms for common operations

### Feedback & Validation
- **Real-time Validation**: Immediate feedback as users type
- **Progressive Enhancement**: Show validation states without blocking progress
- **Contextual Help**: Tooltips and inline guidance for complex fields
- **Error Recovery**: Clear error messages with suggested actions

## Module-Specific UI Guidelines

### Inventory Management
- **Stock Level Grid**: Real-time updates, color-coded status (low/out of stock)
- **Movement History**: Filterable timeline with expandable details
- **Quick Actions**: One-click transfer, adjust, and count initiation
- **Location Picker**: Visual warehouse map with bin-level navigation

### Catalog Management
- **Item Cards**: Image thumbnail, key details, status indicators
- **Bulk Editing**: Multi-select properties panel for batch updates
- **Category Tree**: Drag-and-drop organization with live preview
- **Attribute Builder**: Dynamic form generation for custom properties

### Purchasing Workflow
- **PO Builder**: Smart supplier suggestions, auto-calculated quantities
- **Approval Pipeline**: Visual progress indicator with role-based actions
- **Receipt Matching**: Side-by-side comparison with variance highlighting
- **Vendor Performance**: Embedded charts and KPI cards

### Order Fulfillment
- **Pick List View**: Optimized route display with item locations
- **Pack Station**: Barcode verification with visual confirmation
- **Reservation Board**: Kanban-style workflow with drag-and-drop
- **Shipping Interface**: Carrier integration with label printing

### Reporting & Analytics
- **Dashboard Widgets**: Customizable KPI tiles with drill-down capability
- **Interactive Charts**: Clickable time series and breakdowns
- **Report Builder**: Drag-and-drop query interface with live preview
- **Export Center**: Scheduled reports with format options

## Performance Requirements

### Loading Standards
- **Initial Page Load**: < 1.5 seconds on 3G connection
- **Navigation**: < 100ms between pages
- **Data Tables**: < 200ms for up to 10,000 rows with virtual scrolling
- **Search Results**: < 300ms for filtered results
- **Form Submission**: < 500ms with optimistic updates

### Data Handling
- **Virtual Scrolling**: For tables with 1000+ items
- **Pagination**: Server-side with intelligent prefetching
- **Caching**: 5-minute cache for reference data, 30-second cache for inventory
- **Offline Support**: Critical workflows available offline with sync queue

## Accessibility Requirements

### WCAG 2.1 AA Compliance
- **Keyboard Navigation**: All functionality accessible via keyboard
- **Screen Reader Support**: Proper ARIA labels and announcements
- **Color Contrast**: Minimum 4.5:1 ratio for normal text
- **Focus Management**: Clear focus indicators and logical tab order

### Inclusive Design
- **Language Support**: i18n-ready with RTL text support
- **Device Flexibility**: Touch-friendly targets (44px minimum)
- **Vision Impairment**: High contrast mode and zoom support up to 200%
- **Motor Impairment**: Large click targets and forgiving interaction zones

## Design Tokens

### Component Specifications
- **Buttons**: 40px height, 12px border radius, medium font weight
- **Input Fields**: 44px height, 8px border radius, 12px padding
- **Cards**: 12px border radius, subtle shadow, 1px border
- **Tables**: Alternating row colors, sticky headers, compact/comfortable density options
- **Modals**: Max 90% viewport, centered, backdrop blur effect

### Animation Guidelines
- **Micro-interactions**: 150ms ease-out for hovers and state changes
- **Page Transitions**: 250ms slide/fade for navigation
- **Loading States**: Skeleton screens and progressive loading
- **Success Feedback**: Subtle animations for completed actions

### Data Visualization
- **Charts**: Consistent color mapping across modules
- **KPI Cards**: Status-driven color coding with trend indicators
- **Progress Bars**: Clear completion states with percentages
- **Status Indicators**: Universal iconography for states (pending, complete, error)

## UI Component Standards & Implementation Guide

### Font Colors & Text Styling

#### Input Fields & Form Elements

**Required Pattern:**
```jsx
// All input fields must include these classes for proper text visibility:
className="... text-gray-900 placeholder-gray-500"

// Example:
<input
  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-500"
  placeholder="Enter value here"
/>
```

**Select Dropdowns:**
```jsx
// Select elements need text color but not placeholder color:
className="... text-gray-900"

// Example:
<select className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900">
  <option value="">Select option</option>
</select>
```

#### Required Field Indicators

**Standard Pattern:**
```jsx
// Always use red asterisk in a span for required fields:
<label className="block text-sm font-medium text-gray-700 mb-2">
  Field Name <span className="text-red-500">*</span>
</label>
```

**NEVER use plain asterisk:**
```jsx
// ❌ Wrong:
<label>Field Name *</label>

// ✅ Correct:
<label>Field Name <span className="text-red-500">*</span></label>
```

### Form Validation Standards

#### React Hook Form Configuration

**Standard Setup:**
```jsx
const form = useForm<FormData>({
  resolver: zodResolver(validationSchema),
  mode: 'all', // Enables real-time validation
  defaultValues: {
    // Define all default values here
  }
});
```

**Validation Triggering:**
```jsx
// Always trigger validation on component mount:
useEffect(() => {
  // Set default values if needed
  // ...
  
  // Trigger validation on mount
  trigger();
}, [trigger]);
```

### Button Styling Standards

#### Primary Action Buttons

**Enabled State:**
```jsx
className="bg-blue-600 text-white hover:bg-blue-700 hover:shadow-md border border-blue-600"
```

**Disabled State:**
```jsx
className="bg-gray-200 text-gray-400 cursor-not-allowed border border-gray-200"
```

**Complete Example:**
```jsx
<button
  disabled={!isValid}
  className={`
    inline-flex items-center gap-2 px-8 py-3 rounded-lg text-sm font-semibold transition-all duration-200 shadow-sm
    ${isValid
      ? 'bg-blue-600 text-white hover:bg-blue-700 hover:shadow-md border border-blue-600'
      : 'bg-gray-200 text-gray-400 cursor-not-allowed border border-gray-200'
    }
  `}
>
  Button Text
</button>
```

### Error Handling & Validation Display

**Standard Pattern:**
```jsx
// Input with error styling:
<input
  className={`
    w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-500
    ${errors.fieldName ? 'border-red-300' : 'border-gray-300'}
  `}
/>

// Error message display:
{errors.fieldName && (
  <p className="mt-1 text-sm text-red-600">{errors.fieldName.message}</p>
)}
```

### Layout Standards

#### Content Container

**Standard Wrapper:**
```jsx
// Content box with proper styling:
<div className="bg-white rounded-lg shadow-sm border">
  <div className="p-6">
    {/* Content here */}
  </div>
</div>
```

#### Grid Layouts

**Standard Responsive Grids:**
```jsx
// Two-column responsive:
<div className="grid grid-cols-1 md:grid-cols-2 gap-4">

// Three-column responsive:
<div className="grid grid-cols-1 md:grid-cols-3 gap-4">
```

### Extended Color Palette

#### Text Colors
- Primary text: `text-gray-900`
- Secondary text: `text-gray-600`
- Placeholder text: `placeholder-gray-500`
- Error text: `text-red-600`
- Required indicators: `text-red-500`

#### Background Colors
- Primary button: `bg-blue-600` (hover: `bg-blue-700`)
- Disabled button: `bg-gray-200`
- Content background: `bg-white`
- Selected states: `bg-blue-50`

#### Border Colors
- Default border: `border-gray-300`
- Error border: `border-red-300`
- Focus border: `border-blue-500`
- Selected border: `border-blue-500`

## Multi-Step Form/Wizard Layout Standards

### **Template for Multi-Tab UI Requirements**

This is the **approved template design** for all future multi-step forms and wizard interfaces.

#### **Container Structure**
```jsx
// Main container with proper spacing and width
<div className="max-w-5xl mx-auto px-6 py-8">
  
  {/* Step Indicator Section */}
  <div className="mb-8 mt-6">
    <div className="flex items-center justify-between">
      {/* Step indicators here */}
    </div>
  </div>

  {/* Content Container - Creates "floating" effect */}
  <div className="bg-white rounded-lg shadow-sm border min-h-[600px] p-6">
    {/* Step content here */}
  </div>
</div>
```

#### **Step Indicator Standards**
```jsx
// Large, prominent step circles
<div className="w-12 h-12 rounded-full flex items-center justify-center text-base font-semibold">
  
// Step text with proper sizing and spacing  
<div className="ml-4">
  <p className="text-base font-medium">Step Title</p>
  <p className="text-sm text-gray-500">Step Description</p>
</div>

// Check icon for completed steps
<Check className="h-6 w-6" />
```

#### **Content Layout Principles**

**1. Floating Content Design:**
- Content padding applied to **outer container** (`p-6`), not individual components
- Creates uniform margin on all sides (top, bottom, left, right)
- Content "floats" within the white container instead of touching edges

**2. Spacing Hierarchy:**
- **Container**: `max-w-5xl` for optimal width, `px-6 py-8` for page margins
- **Step indicators**: `mt-6 mb-8` for proper separation from page edges
- **Content sections**: `space-y-8` between major form sections
- **Minimum height**: `min-h-[600px]` ensures consistent container size

**3. Step Indicator Positioning:**
- **Size**: `w-12 h-12` circles (prominent and accessible)
- **Typography**: `text-base` for titles, `text-sm` for descriptions  
- **Spacing**: `ml-4` between circle and text, `mt-6` from page top

#### **Implementation Pattern**

**Main Wizard Component:**
```jsx
export default function MultiStepWizard() {
  return (
    <div className="max-w-5xl mx-auto px-6 py-8">
      {/* Step Indicator */}
      <div className="mb-8 mt-6">
        {/* Step circles and connecting lines */}
      </div>

      {/* Content Container */}
      <div className="bg-white rounded-lg shadow-sm border min-h-[600px] p-6">
        {/* Individual step components */}
      </div>
    </div>
  );
}
```

**Individual Step Components:**
```jsx
export default function StepComponent() {
  return (
    <div className="">  {/* No padding - handled by parent */}
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-2">Step Title</h2>
        <p className="text-gray-600">Step description</p>
      </div>

      <form className="space-y-8">
        {/* Form content with generous spacing */}
      </form>
    </div>
  );
}
```

#### **Visual Design Goals Achieved**
- ✅ **Professional floating content** - Not stuck to container edges
- ✅ **Prominent step indicators** - Easy to read and navigate
- ✅ **Consistent spacing** - Uniform margins and padding throughout
- ✅ **Scalable design** - Works for 2+ steps, adapts to content length
- ✅ **Accessibility** - Large touch targets, clear visual hierarchy
- ✅ **Responsive layout** - Maintains proportions across screen sizes

#### **Key Measurements**
- **Container width**: `max-w-5xl` (optimal reading width)
- **Step circles**: `w-12 h-12` (accessible size)
- **Content padding**: `p-6` (uniform floating effect)
- **Section spacing**: `space-y-8` (comfortable separation)
- **Minimum height**: `min-h-[600px]` (prevents layout shifts)

### Common Issues to Avoid

1. **❌ Missing text color on inputs** - Always include `text-gray-900`
2. **❌ Plain asterisk for required fields** - Always use `<span className="text-red-500">*</span>`
3. **❌ Wrong form validation mode** - Use `mode: 'all'` for real-time validation
4. **❌ Missing validation trigger** - Always call `trigger()` on component mount
5. **❌ Insufficient spacing** - Follow the spacing standards above
6. **❌ Inconsistent button styling** - Use the standard button patterns

### Development Checklists

#### **Checklist for New Forms**
- [ ] Input fields have `text-gray-900 placeholder-gray-500`
- [ ] Select fields have `text-gray-900`
- [ ] Required fields use red span asterisk
- [ ] Form uses `mode: 'all'` validation
- [ ] Validation triggers on mount
- [ ] Buttons follow standard enabled/disabled styling
- [ ] Proper spacing between sections
- [ ] Error messages use `text-red-600`
- [ ] Content wrapped in proper container with padding

#### **Checklist for Multi-Step Forms**
- [ ] Main container uses `max-w-5xl mx-auto px-6 py-8`
- [ ] Step indicators have `mb-8 mt-6` spacing
- [ ] Step circles are `w-12 h-12` with `text-base` font
- [ ] Content container has `bg-white rounded-lg shadow-sm border min-h-[600px] p-6`
- [ ] Individual step components have NO padding (handled by parent)
- [ ] Form sections use `space-y-8` for generous spacing
- [ ] Step titles use `text-xl font-semibold text-gray-900`
- [ ] Content "floats" within container (doesn't touch edges)
- [ ] Navigation buttons positioned with proper spacing
- [ ] Responsive design maintained across screen sizes

---

# Frontend Technology Stack

## Core Technologies

### Frontend Framework
- **React 19.x** with TypeScript for type safety and developer experience
- **Next.js 15.x** for SSR/SSG, API routes, and optimized performance
- **Tailwind CSS 3.x** for utility-first styling and consistent design system

### State Management & Data Fetching
- **TanStack Query (React Query) v5** for server state management and caching
- **Zustand** for lightweight client-side state management
- **React Hook Form** with **Zod** for form validation and type-safe schemas

### UI Component Library
- **Shadcn/ui** built on Radix UI primitives for accessibility-first components
- **Lucide React** for consistent icon system
- **React Hot Toast** for notifications and alerts

### Development Tools & Build System
- **Vite 5.x** for fast development server and optimized builds
- **ESLint** with TypeScript rules and React hooks plugin
- **Prettier** for code formatting consistency
- **Husky** with **lint-staged** for pre-commit hooks

### Testing Framework
- **Vitest** for unit testing (faster Jest alternative)
- **React Testing Library** for component testing
- **Playwright** for E2E testing across browsers
- **MSW (Mock Service Worker)** for API mocking during development

### Performance & Monitoring
- **React DevTools** for debugging and performance profiling
- **Lighthouse CI** for automated performance auditing
- **Sentry** for error tracking and performance monitoring
- **Web Vitals** monitoring integration

### Authentication & Security
- **Custom JWT Authentication** integration with Java Spring Boot backend
- **Axios Interceptors** for JWT token handling with automatic refresh
- **HTTPS** enforcement and secure headers configuration
- **Token Storage** in secure localStorage with automatic cleanup

## Prerequisites & Setup

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
npm install axios js-cookie @types/js-cookie

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

## Architecture Patterns

### Component Organization
```
src/
├── app/                    # Next.js 15 app directory
│   ├── (auth)/            # Route groups for auth pages
│   │   ├── login/         # Login page
│   │   ├── forgot-password/  # Password reset request page
│   │   └── reset-password/   # Password reset completion page
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

## Performance Optimization

### Bundle Optimization
- **Code Splitting**: Dynamic imports for routes and heavy components
- **Image Optimization**: Next.js Image component with lazy loading
- **Font Optimization**: Using next/font for optimal font loading
- **Bundle Analysis**: Monitor build size with @next/bundle-analyzer

### Runtime Performance
- **React.memo**: For expensive components that don't change often
- **useMemo/useCallback**: For complex computations and stable references
- **Virtual Scrolling**: For large data tables with 1000+ rows
- **Progressive Loading**: For dashboard widgets and complex charts

### Caching Strategy
- **TanStack Query**: API response caching with intelligent invalidation
- **Next.js Static Generation**: For reports and analytics pages
- **Service Worker**: Offline inventory scanning capabilities
- **Browser Caching**: Static assets with appropriate cache headers

## Development Workflow

### Quality Gates
1. **TypeScript Compilation**: Must pass with strict mode enabled
2. **ESLint Rules**: Zero warnings in production builds
3. **Unit Tests**: 80%+ coverage for utilities and custom hooks
4. **E2E Tests**: Critical user journeys must pass
5. **Lighthouse Performance**: Score > 90 for production builds

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
NEXT_PUBLIC_JWT_REFRESH_MARGIN=300000
NEXT_PUBLIC_SESSION_TIMEOUT=28800000
```

## Quality Assurance

### Testing Strategy
- **Unit Tests**: 80%+ coverage for utilities and custom hooks
- **Component Tests**: Key user interactions and edge cases
- **Integration Tests**: API integration and state management
- **E2E Tests**: Critical business workflows from start to finish
- **Visual Regression**: Screenshot testing for consistent appearance

### Browser Support
- **Primary**: Chrome 120+, Firefox 120+, Safari 17+, Edge 120+
- **Mobile**: iOS Safari 17+, Android Chrome 120+
- **Graceful Degradation**: Core functionality works in older browsers

### Performance Monitoring
- **Core Web Vitals**: LCP < 2.5s, FID < 100ms, CLS < 0.1
- **Custom Metrics**: Time to interactive for each module
- **Error Tracking**: Sub-1% error rate for production workflows
- **User Analytics**: Heatmaps and user journey analysis

## Integration with Java Backend

### API Communication
- **RESTful APIs**: Axios-based client with automatic retry logic
- **WebSocket Connections**: Real-time inventory updates and notifications
- **Server-Sent Events**: Live notifications and system alerts
- **File Upload**: Handling bulk imports (CSV, Excel) with progress tracking

### Authentication Flow
1. **Custom JWT Authentication**: Direct integration with Java Spring Boot `/auth` endpoints
2. **JWT Tokens**: Access and refresh tokens stored in secure localStorage
3. **Automatic Refresh**: Token renewal via `/auth/refresh` endpoint with axios interceptors
4. **Role-Based Rendering**: UI components based on user permissions and roles
5. **Session Management**: Active session tracking with logout from `/auth/sessions`
6. **Password Reset System**: Complete self-service reset flow with email verification
7. **Account Security**: Password change, account lockout, and failed attempt handling

### Error Handling Strategy
- **Global Error Boundary**: Catches unexpected React errors
- **API Error Standardization**: Consistent error response format with backend
- **User-Friendly Messages**: Clear error messages with actionable suggestions
- **Retry Mechanisms**: Automatic retry for transient network failures

## Implementation Guidelines

### Development Workflow
1. **Design Review**: Figma mockups approved before implementation
2. **Component Development**: Build in isolation with Storybook
3. **Integration Testing**: Test with real API data in staging environment
4. **Performance Audit**: Lighthouse CI validation in pull request checks
5. **Accessibility Review**: Automated and manual testing before merge

### Code Quality Standards
- **Component Props**: TypeScript interfaces with comprehensive JSDoc comments
- **Error Boundaries**: Wrap each major section with error handling
- **Loading States**: Consistent skeleton and spinner patterns
- **Empty States**: Helpful illustrations and call-to-action guidance
- **Progressive Enhancement**: Core functionality works without JavaScript

### Security Considerations
- **XSS Prevention**: Sanitize user inputs and use dangerouslySetInnerHTML carefully
- **CSRF Protection**: Implement CSRF tokens for state-changing operations
- **Content Security Policy**: Restrict resource loading to trusted sources
- **Input Validation**: Client-side validation with server-side verification

## Deployment & Production

### Build Optimization
```bash
# Production build
npm run build

# Analyze bundle size
npm run analyze

# Type checking
npm run type-check

# Run all tests
npm run test && npm run test:e2e
```

### Deployment Configuration
```javascript
// next.config.js
const nextConfig = {
  experimental: {
    optimizePackageImports: ['@radix-ui/react-icons'],
  },
  images: {
    domains: ['your-cdn-domain.com'],
  },
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'X-XSS-Protection',
            value: '1; mode=block',
          },
        ],
      },
    ];
  },
};
```

### Production Monitoring
- **Error Tracking**: Sentry integration for real-time error monitoring
- **Performance Monitoring**: Core Web Vitals and custom metrics
- **User Analytics**: Anonymized usage patterns and feature adoption
- **Security Monitoring**: CSP violations and suspicious activity detection

---

This comprehensive guide ensures the inventory management UI delivers enterprise-grade usability while maintaining the speed and simplicity required for daily warehouse operations. The technology stack prioritizes developer experience, type safety, performance, and maintainability while ensuring seamless integration with the Java backend.

## References

This document consolidates information from:
- ui_design_guidelines.md
- ui_tech_stack.md
- ui-standards.md (merged 2025-09-16)

### Document History
- **2025-09-16**: Merged ui-standards.md content including comprehensive UI component standards, multi-step form templates, font color specifications, form validation patterns, and development checklists. Added detailed multi-tab UI design template approved for all future wizard interfaces.