'use client';

import { useState } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { Menu, X } from 'lucide-react';
import { NavigationMenu } from './NavigationMenu';

interface NavigationSidebarProps {
  isCollapsed?: boolean;
  onToggle?: () => void;
}

export function NavigationSidebar({ isCollapsed = false, onToggle }: NavigationSidebarProps) {
  const { user } = useAuthStore();

  return (
    <aside 
      className={`
        bg-white border-r border-gray-200 flex flex-col h-full transition-all duration-200
        ${isCollapsed ? 'w-16' : 'w-64'}
      `}
    >
      {/* Header */}
      <div className="p-6 border-b border-gray-200">
        <div className="flex items-center justify-between">
          {!isCollapsed && (
            <div className="flex items-center">
              <h2 className="text-lg font-semibold text-gray-900">
                Inventory
              </h2>
            </div>
          )}
          <button
            onClick={onToggle}
            className="p-2 rounded-md hover:bg-gray-100 transition-all duration-200"
            title={isCollapsed ? "Expand sidebar" : "Collapse sidebar"}
          >
            {isCollapsed ? (
              <Menu className="h-5 w-5 text-gray-500" />
            ) : (
              <X className="h-5 w-5 text-gray-500" />
            )}
          </button>
        </div>
      </div>

      {/* Navigation Menu */}
      <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
        <NavigationMenu 
          isCollapsed={isCollapsed} 
          userRoles={user?.roles || []} 
          onForceExpand={() => onToggle?.()}
        />
      </nav>

      {/* Footer/User Info */}
      {!isCollapsed && user && (
        <div className="p-4 border-t border-gray-200">
          <div className="bg-gray-50 rounded-lg p-3">
            <div className="text-sm font-medium text-gray-700 truncate">
              {user.displayName}
            </div>
            <div className="text-xs text-gray-500 truncate">
              {user.roles.map(role => role.name).join(', ')}
            </div>
          </div>
        </div>
      )}
    </aside>
  );
}