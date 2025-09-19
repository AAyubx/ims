'use client';

import { useAuthStore } from '@/stores/authStore';
import { Users, Settings, Shield, MapPin } from 'lucide-react';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { DateTimeUtil } from '@/utils/dateTime';

export default function DashboardPage() {
  const { user } = useAuthStore();
  const router = useRouter();

  return (
    <>
      {/* Quick Actions Card */}
      <div className="bg-white overflow-hidden shadow rounded-lg mb-6">
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
                  {user?.lastLoginAt && (
                    <><br />{DateTimeUtil.formatLastLoginMessage(user.lastLoginAt)}</>
                  )}
                  {!user?.lastLoginAt && (
                    <><br />Welcome! This is your first login.</>
                  )}
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
                      {(() => {
                        const { relative, absolute, timezone } = DateTimeUtil.formatLastLogin(user.lastLoginAt);
                        return `${relative} (${absolute})`;
                      })()}
                    </dd>
                  </div>
                )}
              </dl>
            </div>
          )}
        </div>
      </div>
    </>
  );
}