'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { AuthAPI } from '@/lib/api';
import { UserInfo } from '@/types/auth';
import { ChevronRight, Home } from 'lucide-react';

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const [isAuthorized, setIsAuthorized] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAdminAccess();
  }, []);

  const checkAdminAccess = async () => {
    try {
      const token = localStorage.getItem('inventory_access_token');
      if (!token) {
        router.push('/login');
        return;
      }

      const response = await AuthAPI.getCurrentUser();
      if (response.success && response.data) {
        const user: UserInfo = response.data;
        const hasAdminRole = user.roles.some(role => role.code === 'ADMIN');
        
        if (hasAdminRole) {
          setIsAuthorized(true);
        } else {
          router.push('/dashboard');
          return;
        }
      } else {
        router.push('/login');
        return;
      }
    } catch (error) {
      console.error('Admin access check failed:', error);
      router.push('/login');
      return;
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Checking access permissions...</p>
        </div>
      </div>
    );
  }

  if (!isAuthorized) {
    return null; // Will redirect via useEffect
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <h1 className="text-xl font-semibold text-gray-900">
                System Administration
              </h1>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={() => router.push('/dashboard')}
                className="text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium cursor-pointer"
              >
                Back to Dashboard
              </button>
            </div>
          </div>
        </div>
      </nav>
      
      {/* Breadcrumb */}
      <div className="bg-gray-50 border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center py-3 text-sm">
            <button
              onClick={() => router.push('/dashboard')}
              className="flex items-center text-gray-500 hover:text-gray-700 cursor-pointer"
            >
              <Home className="h-4 w-4 mr-1" />
              Dashboard
            </button>
            <ChevronRight className="h-4 w-4 text-gray-400 mx-2" />
            <span className="text-gray-900 font-medium">Administration</span>
          </div>
        </div>
      </div>
      
      <div className="max-w-7xl mx-auto">
        {children}
      </div>
    </div>
  );
}