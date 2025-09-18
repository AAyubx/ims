'use client';

import { useState, useEffect } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { LogOut, User } from 'lucide-react';
import { NavigationSidebar } from '@/components/navigation/NavigationSidebar';
import toast from 'react-hot-toast';

interface AppLayoutProps {
  children: React.ReactNode;
}

export function AppLayout({ children }: AppLayoutProps) {
  const { isAuthenticated, user, logout, loadCurrentUser, isLoading } = useAuthStore();
  const router = useRouter();
  const pathname = usePathname();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  // Check if we should show the navigation (not on login page)
  const showNavigation = isAuthenticated && pathname !== '/login';

  // Redirect to login if not authenticated and load user data
  useEffect(() => {
    if (!isLoading && !isAuthenticated && pathname !== '/login') {
      router.push('/login');
    } else if (isAuthenticated && !user) {
      // If authenticated but no user data, try to load current user
      const loadUser = async () => {
        try {
          await loadCurrentUser();
        } catch (error) {
          console.error('Failed to load current user:', error);
          // If user loading fails, probably token is invalid
          router.push('/login');
        }
      };
      loadUser();
    }
  }, [isAuthenticated, isLoading, user, router, loadCurrentUser, pathname]);

  // Handle redirect from login page when authenticated
  useEffect(() => {
    if (isAuthenticated && pathname === '/login') {
      router.push('/dashboard');
    }
  }, [isAuthenticated, pathname, router]);

  const handleLogout = async () => {
    try {
      await logout();
      toast.success('Logged out successfully');
      router.push('/login');
    } catch (error) {
      toast.error('Logout failed. Please try again.');
      // Still redirect to login even if logout API call fails
      router.push('/login');
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <svg
            className="animate-spin h-8 w-8 mx-auto text-blue-600"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            ></circle>
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            ></path>
          </svg>
          <p className="mt-2 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  // If not authenticated and not loading, show children (login page)
  if (!isAuthenticated) {
    return <>{children}</>;
  }

  // If authenticated but on login page, let useEffect handle the redirect
  if (pathname === '/login') {
    return null;
  }

  // Show layout with navigation
  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Navigation Sidebar */}
      {showNavigation && (
        <NavigationSidebar 
          isCollapsed={sidebarCollapsed}
          onToggle={() => setSidebarCollapsed(!sidebarCollapsed)}
        />
      )}

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        {showNavigation && (
          <header className="bg-white shadow-sm border-b border-gray-200">
            <div className="px-4 sm:px-6 lg:px-8">
              <div className="flex justify-between items-center py-4">
                <div className="flex items-center">
                  <h1 className="text-2xl font-bold text-gray-900">
                    Inventory Management System
                  </h1>
                </div>
                
                <div className="flex items-center space-x-4">
                  {/* User Info */}
                  <div className="flex items-center space-x-2">
                    <User className="h-5 w-5 text-gray-500" />
                    <span className="text-sm font-medium text-gray-700">
                      {user?.displayName || user?.email}
                    </span>
                    {user?.roles && user.roles.length > 0 && (
                      <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        {user.roles[0].name}
                      </span>
                    )}
                  </div>

                  {/* Logout Button */}
                  <button
                    onClick={handleLogout}
                    className="inline-flex items-center px-3 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 cursor-pointer"
                  >
                    <LogOut className="h-4 w-4 mr-2" />
                    Logout
                  </button>
                </div>
              </div>
            </div>
          </header>
        )}

        {/* Main Content */}
        <main className={`flex-1 overflow-y-auto ${showNavigation ? 'px-4 sm:px-6 lg:px-8 py-6' : ''}`}>
          {children}
        </main>
      </div>
    </div>
  );
}