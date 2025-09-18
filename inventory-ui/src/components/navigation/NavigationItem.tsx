'use client';

import { useRouter, usePathname } from 'next/navigation';
import { LucideIcon, ChevronRight } from 'lucide-react';

export interface MenuItem {
  id: string;
  label: string;
  icon: LucideIcon;
  href?: string;
  children?: MenuItem[];
  requiredRoles: string[];
  isDisabled?: boolean;
}

interface NavigationItemProps {
  item: MenuItem;
  isCollapsed: boolean;
  hasPermission: boolean;
  level?: number;
}

export function NavigationItem({ item, isCollapsed, hasPermission, level = 0 }: NavigationItemProps) {
  const router = useRouter();
  const pathname = usePathname();
  
  const isActive = item.href ? pathname === item.href : false;
  const isDisabled = !hasPermission || item.isDisabled;

  const handleClick = () => {
    if (isDisabled) return;
    
    if (item.href) {
      router.push(item.href);
    }
  };

  const paddingLeft = level > 0 ? `pl-${8 + (level * 4)}` : 'pl-3';

  return (
    <button
      onClick={handleClick}
      className={`
        w-full flex items-center py-2 text-sm rounded-md transition-all duration-200
        ${paddingLeft} pr-3
        ${isActive
          ? 'bg-blue-50 text-blue-700'
          : isDisabled
            ? 'text-gray-400 cursor-not-allowed hover:bg-transparent'
            : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900 cursor-pointer'
        }
      `}
      disabled={isDisabled}
      title={isCollapsed ? item.label : undefined}
    >
      <item.icon className={`
        h-5 w-5 flex-shrink-0 
        ${isCollapsed ? '' : 'mr-3'}
        ${isActive ? 'text-blue-700' : ''}
      `} />
      
      {!isCollapsed && (
        <>
          <span className="truncate flex-1 text-left">{item.label}</span>
          {item.children && item.children.length > 0 && (
            <ChevronRight className="h-4 w-4 ml-auto flex-shrink-0" />
          )}
        </>
      )}
    </button>
  );
}