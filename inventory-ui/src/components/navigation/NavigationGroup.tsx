'use client';

import { useState } from 'react';
import { usePathname } from 'next/navigation';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { NavigationItem, MenuItem } from './NavigationItem';

interface NavigationGroupProps {
  item: MenuItem;
  isCollapsed: boolean;
  hasPermission: boolean;
  level?: number;
  onForceExpand?: () => void;
}

export function NavigationGroup({ item, isCollapsed, hasPermission, level = 0, onForceExpand }: NavigationGroupProps) {
  const [isExpanded, setIsExpanded] = useState(true);
  const pathname = usePathname();

  const toggleExpanded = () => {
    if (isCollapsed && hasChildren) {
      // Force sidebar expansion when clicking collapsed group with children
      onForceExpand?.();
      setIsExpanded(true);
    } else if (!isCollapsed) {
      setIsExpanded(!isExpanded);
    }
  };

  const hasChildren = item.children && item.children.length > 0;

  // Check if this group or any of its children are active
  const isGroupActive = () => {
    if (item.href && pathname === item.href) return true;
    
    if (hasChildren) {
      const checkChildrenActive = (children: MenuItem[]): boolean => {
        return children.some(child => {
          if (child.href && pathname === child.href) return true;
          if (child.children) return checkChildrenActive(child.children);
          return false;
        });
      };
      return checkChildrenActive(item.children);
    }
    
    return false;
  };

  const isActive = isGroupActive();

  // If item has a direct href, render as NavigationItem
  if (item.href && !hasChildren) {
    return (
      <NavigationItem 
        item={item} 
        isCollapsed={isCollapsed} 
        hasPermission={hasPermission}
        level={level}
      />
    );
  }

  const paddingLeft = level > 0 ? `pl-${8 + (level * 4)}` : 'pl-3';

  return (
    <div className="mb-1">
      {/* Group Header */}
      {hasChildren && (
        <button
          onClick={toggleExpanded}
          className={`
            w-full flex items-center justify-between py-2 text-sm font-medium rounded-md
            transition-all duration-200 ${paddingLeft} pr-3 cursor-pointer
            ${isActive
              ? 'bg-blue-50 text-blue-700'
              : hasPermission 
                ? 'text-gray-700 hover:bg-gray-100 hover:text-gray-900' 
                : 'text-gray-400 cursor-not-allowed'
            }
          `}
          disabled={!hasPermission}
          title={isCollapsed ? item.label : undefined}
        >
          <div className="flex items-center">
            <item.icon className={`
              h-5 w-5 flex-shrink-0 
              ${isCollapsed ? '' : 'mr-3'}
              ${isActive ? 'text-blue-700' : ''}
            `} />
            {!isCollapsed && (
              <span className="truncate">{item.label}</span>
            )}
          </div>
          
          {!isCollapsed && hasChildren && (
            <div className="flex-shrink-0">
              {isExpanded ? (
                <ChevronDown className="h-4 w-4" />
              ) : (
                <ChevronRight className="h-4 w-4" />
              )}
            </div>
          )}
        </button>
      )}

      {/* Group Children */}
      {hasChildren && (!isCollapsed && isExpanded) && (
        <div className="mt-1 space-y-1">
          {item.children?.map((child) => (
            <NavigationGroup
              key={child.id}
              item={child}
              isCollapsed={isCollapsed}
              hasPermission={hasPermission}
              level={level + 1}
              onForceExpand={onForceExpand}
            />
          ))}
        </div>
      )}
    </div>
  );
}