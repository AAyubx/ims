import { 
  Home, 
  Package, 
  BarChart3, 
  TrendingUp, 
  Edit, 
  RefreshCcw,
  BookOpen,
  Tags,
  Settings as SettingsIcon,
  ShoppingCart,
  Truck,
  Users,
  MapPin,
  FileText,
  PieChart,
  Download,
  Shield,
  Store
} from 'lucide-react';

import { MenuItem } from '@/components/navigation/NavigationItem';

export const MENU_CONFIG: MenuItem[] = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    icon: Home,
    href: '/dashboard',
    requiredRoles: ['ADMIN', 'MANAGER', 'CLERK', 'VIEWER']
  },
  {
    id: 'inventory',
    label: 'Inventory',
    icon: Package,
    requiredRoles: ['ADMIN', 'MANAGER', 'CLERK'],
    children: [
      {
        id: 'stock-levels',
        label: 'Stock Levels',
        icon: BarChart3,
        href: '/inventory/stock-levels',
        requiredRoles: ['ADMIN', 'MANAGER', 'CLERK']
      },
      {
        id: 'movements',
        label: 'Movements',
        icon: TrendingUp,
        href: '/inventory/movements',
        requiredRoles: ['ADMIN', 'MANAGER', 'CLERK']
      },
      {
        id: 'adjustments',
        label: 'Adjustments',
        icon: Edit,
        href: '/inventory/adjustments',
        requiredRoles: ['ADMIN', 'MANAGER']
      },
      {
        id: 'cycle-counts',
        label: 'Cycle Counts',
        icon: RefreshCcw,
        href: '/inventory/cycle-counts',
        requiredRoles: ['ADMIN', 'MANAGER']
      }
    ]
  },
  {
    id: 'catalog',
    label: 'Catalog',
    icon: BookOpen,
    requiredRoles: ['ADMIN', 'MANAGER', 'CLERK'],
    children: [
      {
        id: 'items',
        label: 'Items',
        icon: Package,
        href: '/catalog/items',
        requiredRoles: ['ADMIN', 'MANAGER', 'CLERK']
      },
      {
        id: 'categories',
        label: 'Categories',
        icon: Tags,
        href: '/catalog/categories',
        requiredRoles: ['ADMIN', 'MANAGER']
      },
      {
        id: 'attributes',
        label: 'Attributes',
        icon: SettingsIcon,
        href: '/catalog/attributes',
        requiredRoles: ['ADMIN', 'MANAGER']
      }
    ]
  },
  {
    id: 'purchasing',
    label: 'Purchasing',
    icon: ShoppingCart,
    requiredRoles: ['ADMIN', 'MANAGER'],
    children: [
      {
        id: 'purchase-orders',
        label: 'Purchase Orders',
        icon: ShoppingCart,
        href: '/purchasing/orders',
        requiredRoles: ['ADMIN', 'MANAGER']
      },
      {
        id: 'suppliers',
        label: 'Suppliers',
        icon: Truck,
        href: '/purchasing/suppliers',
        requiredRoles: ['ADMIN', 'MANAGER']
      },
      {
        id: 'receipts',
        label: 'Receipts',
        icon: FileText,
        href: '/purchasing/receipts',
        requiredRoles: ['ADMIN', 'MANAGER']
      }
    ]
  },
  {
    id: 'orders',
    label: 'Orders',
    icon: FileText,
    requiredRoles: ['ADMIN', 'MANAGER', 'CLERK'],
    children: [
      {
        id: 'active-orders',
        label: 'Active Orders',
        icon: FileText,
        href: '/orders/active',
        requiredRoles: ['ADMIN', 'MANAGER', 'CLERK']
      },
      {
        id: 'reservations',
        label: 'Reservations',
        icon: Package,
        href: '/orders/reservations',
        requiredRoles: ['ADMIN', 'MANAGER', 'CLERK']
      },
      {
        id: 'fulfillment',
        label: 'Fulfillment',
        icon: Truck,
        href: '/orders/fulfillment',
        requiredRoles: ['ADMIN', 'MANAGER', 'CLERK']
      }
    ]
  },
  {
    id: 'transfers',
    label: 'Transfers',
    icon: Truck,
    requiredRoles: ['ADMIN', 'MANAGER'],
    children: [
      {
        id: 'inter-store',
        label: 'Inter-store Transfers',
        icon: Truck,
        href: '/transfers/inter-store',
        requiredRoles: ['ADMIN', 'MANAGER']
      },
      {
        id: 'bin-to-bin',
        label: 'Bin-to-bin Transfers',
        icon: Package,
        href: '/transfers/bin-to-bin',
        requiredRoles: ['ADMIN', 'MANAGER']
      },
      {
        id: 'transfer-history',
        label: 'Transfer History',
        icon: FileText,
        href: '/transfers/history',
        requiredRoles: ['ADMIN', 'MANAGER', 'CLERK']
      }
    ]
  },
  {
    id: 'reports',
    label: 'Reports',
    icon: PieChart,
    requiredRoles: ['ADMIN', 'MANAGER', 'VIEWER'],
    children: [
      {
        id: 'analytics',
        label: 'Analytics',
        icon: BarChart3,
        href: '/reports/analytics',
        requiredRoles: ['ADMIN', 'MANAGER', 'VIEWER']
      },
      {
        id: 'kpis',
        label: 'KPIs',
        icon: TrendingUp,
        href: '/reports/kpis',
        requiredRoles: ['ADMIN', 'MANAGER']
      },
      {
        id: 'exports',
        label: 'Exports',
        icon: Download,
        href: '/reports/exports',
        requiredRoles: ['ADMIN', 'MANAGER']
      }
    ]
  },
  {
    id: 'admin',
    label: 'Administration',
    icon: Shield,
    requiredRoles: ['ADMIN'],
    children: [
      {
        id: 'users',
        label: 'User Management',
        icon: Users,
        href: '/admin/users',
        requiredRoles: ['ADMIN']
      },
      {
        id: 'stores',
        label: 'Store Management',
        icon: Store,
        href: '/admin/stores',
        requiredRoles: ['ADMIN']
      },
      {
        id: 'locations',
        label: 'Locations',
        icon: MapPin,
        href: '/admin/locations',
        requiredRoles: ['ADMIN']
      },
      {
        id: 'settings',
        label: 'System Settings',
        icon: SettingsIcon,
        href: '/admin/settings',
        requiredRoles: ['ADMIN']
      }
    ]
  }
];