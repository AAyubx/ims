'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { LogOut, User, Users, Settings, Shield, MapPin } from 'lucide-react';
import { useAuthStore } from '@/stores/authStore';
import { AuthAPI } from '@/lib/api';

export default function DashboardPage() {
  const { isAuthenticated, user, logout, loadCurrentUser, isLoading } = useAuthStore();
  const router = useRouter();

  // Redirect to login if not authenticated and load user data
  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
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
  }, [isAuthenticated, isLoading, user, router, loadCurrentUser]);

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

  if (!isAuthenticated) {
    return null; // Will redirect via useEffect
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center">
              <h1 className="text-2xl font-bold text-gray-900">
                Inventory Management Dashboard
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

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        {/* Debug Info - Remove after fixing */}
        {process.env.NODE_ENV === 'development' && (
          <div className="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded">
            <h3 className="font-medium text-yellow-800">Debug Info:</h3>
            <p className="text-sm text-yellow-700">User: {JSON.stringify(user?.roles, null, 2)}</p>
            <p className="text-sm text-yellow-700">Has Admin: {user?.roles?.some(role => role.code === 'ADMIN') ? 'Yes' : 'No'}</p>
          </div>
        )}
        
        {/* Admin Navigation - Only show for admin users */}
        {user?.roles?.some(role => role.code === 'ADMIN') && (
          <div className="mb-6">
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <div className="flex items-center mb-3">
                <Shield className="h-5 w-5 text-blue-600 mr-2" />
                <h3 className="text-lg font-medium text-blue-900">
                  System Administration
                </h3>
              </div>
              <p className="text-blue-700 text-sm mb-4">
                Manage system users, roles, and settings
              </p>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                <button
                  onClick={() => router.push('/admin/users')}
                  className="flex items-center p-4 bg-white border border-blue-200 rounded-md hover:border-blue-300 hover:bg-blue-50 transition-colors duration-200 cursor-pointer"
                >
                  <Users className="h-6 w-6 text-blue-600 mr-3" />
                  <div className="text-left">
                    <div className="font-medium text-blue-900">User Management</div>
                    <div className="text-sm text-blue-700">Manage users, roles, and permissions</div>
                  </div>
                </button>
                
                <button
                  onClick={() => router.push('/admin/stores')}
                  className="flex items-center p-4 bg-white border border-blue-200 rounded-md hover:border-blue-300 hover:bg-blue-50 transition-colors duration-200 cursor-pointer"
                >
                  <MapPin className="h-6 w-6 text-blue-600 mr-3" />
                  <div className="text-left">
                    <div className="font-medium text-blue-900">Store Management</div>
                    <div className="text-sm text-blue-700">Manage stores and locations</div>
                  </div>
                </button>
                
                <button
                  onClick={() => toast.info('Feature coming soon')}
                  className="flex items-center p-4 bg-white border border-blue-200 rounded-md hover:border-blue-300 hover:bg-blue-50 transition-colors duration-200 cursor-pointer"
                >
                  <Settings className="h-6 w-6 text-blue-600 mr-3" />
                  <div className="text-left">
                    <div className="font-medium text-blue-900">System Settings</div>
                    <div className="text-sm text-blue-700">Configure system parameters</div>
                  </div>
                </button>
                
                <button
                  onClick={() => toast.info('Feature coming soon')}
                  className="flex items-center p-4 bg-white border border-blue-200 rounded-md hover:border-blue-300 hover:bg-blue-50 transition-colors duration-200 cursor-pointer"
                >
                  <Shield className="h-6 w-6 text-blue-600 mr-3" />
                  <div className="text-left">
                    <div className="font-medium text-blue-900">Security Audit</div>
                    <div className="text-sm text-blue-700">View security logs and events</div>
                  </div>
                </button>
              </div>
            </div>
          </div>
        )}

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="px-6 py-8">
            <h2 className="text-lg font-medium text-gray-900 mb-4">
              Welcome to the Inventory Management System
            </h2>
            
            <div className="bg-green-50 border border-green-200 rounded-md p-4">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg
                    className="h-5 w-5 text-green-400"
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                  >
                    <path
                      fillRule="evenodd"
                      d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                      clipRule="evenodd"
                    />
                  </svg>
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-green-800">
                    Login Successful!
                  </h3>
                  <p className="mt-1 text-sm text-green-700">
                    You have successfully logged into the inventory management system.
                  </p>
                </div>
              </div>
            </div>

            {user && (
              <div className="mt-6">
                <h3 className="text-base font-medium text-gray-900 mb-3">
                  Account Information
                </h3>
                <dl className="grid grid-cols-1 gap-x-4 gap-y-3 sm:grid-cols-2">
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Email</dt>
                    <dd className="mt-1 text-sm text-gray-900">{user.email}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Display Name</dt>
                    <dd className="mt-1 text-sm text-gray-900">{user.displayName}</dd>
                  </div>
                  {user.employeeCode && (
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Employee Code</dt>
                      <dd className="mt-1 text-sm text-gray-900">{user.employeeCode}</dd>
                    </div>
                  )}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Role</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {user.roles.map(role => role.name).join(', ')}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Tenant</dt>
                    <dd className="mt-1 text-sm text-gray-900">{user.tenant.name}</dd>
                  </div>
                  {user.lastLoginAt && (
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Last Login</dt>
                      <dd className="mt-1 text-sm text-gray-900">
                        {new Date(user.lastLoginAt).toLocaleString()}
                      </dd>
                    </div>
                  )}
                </dl>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}