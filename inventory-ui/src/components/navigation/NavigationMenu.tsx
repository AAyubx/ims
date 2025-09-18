'use client';

import { useMemo } from 'react';
import { NavigationGroup } from './NavigationGroup';
import { MenuItem } from './NavigationItem';
import { MENU_CONFIG } from '@/config/menuConfig';

interface Role {
  id: number;
  code: string;
  name: string;
}

interface NavigationMenuProps {
  isCollapsed: boolean;
  userRoles: Role[];
  onForceExpand?: () => void;
}

export function NavigationMenu({ isCollapsed, userRoles, onForceExpand }: NavigationMenuProps) {
  // Helper function to check if user has required role
  const hasRequiredRole = (requiredRoles: string[]): boolean => {
    if (!userRoles || userRoles.length === 0) return false;
    
    const userRoleCodes = userRoles.map(role => role.code);
    return requiredRoles.some(role => userRoleCodes.includes(role));
  };

  // Filter menu items based on user roles
  const filteredMenuItems = useMemo(() => {
    const filterMenuItems = (items: MenuItem[]): MenuItem[] => {
      return items
        .map(item => {
          const hasPermission = hasRequiredRole(item.requiredRoles);
          
          // Filter children if they exist
          const filteredChildren = item.children 
            ? filterMenuItems(item.children)
            : undefined;

          return {
            ...item,
            children: filteredChildren,
            isDisabled: !hasPermission
          };
        })
        .filter(item => {
          // Keep item if:
          // 1. User has permission, OR
          // 2. Item has visible children (even if user doesn't have permission for parent)
          const hasPermission = hasRequiredRole(item.requiredRoles);
          const hasVisibleChildren = item.children && item.children.length > 0;
          
          return hasPermission || hasVisibleChildren;
        });
    };

    return filterMenuItems(MENU_CONFIG);
  }, [userRoles]);

  if (filteredMenuItems.length === 0) {
    return (
      <div className="text-center text-gray-500 text-sm">
        No menu items available
      </div>
    );
  }

  return (
    <>
      {filteredMenuItems.map((item) => (
        <NavigationGroup
          key={item.id}
          item={item}
          isCollapsed={isCollapsed}
          hasPermission={hasRequiredRole(item.requiredRoles)}
          onForceExpand={onForceExpand}
        />
      ))}
    </>
  );
}