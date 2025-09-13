'use client';

import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import { AdminUserAPI } from '@/lib/adminApi';
import { 
  UserResponseDto, 
  UserFilters, 
  BulkActionRequest, 
  CreateUserRequest, 
  UpdateUserRequest 
} from '@/types/admin';
import { useRouter } from 'next/navigation';

export default function AdminUsersPage() {
  const router = useRouter();
  const [users, setUsers] = useState<UserResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedUsers, setSelectedUsers] = useState<number[]>([]);
  const [filters, setFilters] = useState<UserFilters>({});
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0
  });
  const [sortConfig, setSortConfig] = useState({
    sortBy: 'createdAt',
    sortDir: 'desc' as 'asc' | 'desc'
  });
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState<{
    show: boolean;
    action: string;
    userIds: number[];
    message: string;
  }>({ show: false, action: '', userIds: [], message: '' });

  useEffect(() => {
    loadUsers();
  }, [pagination.page, pagination.size, sortConfig, filters]);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const response = await AdminUserAPI.getUsers(
        pagination.page,
        pagination.size,
        sortConfig.sortBy,
        sortConfig.sortDir,
        filters
      );
      
      if (response.success && response.data) {
        setUsers(response.data.content);
        setPagination(prev => ({
          ...prev,
          totalElements: response.data!.totalElements,
          totalPages: response.data!.totalPages
        }));
      }
    } catch (error) {
      toast.error('Failed to load users');
      console.error('Load users error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (key: keyof UserFilters, value: string) => {
    setFilters(prev => ({
      ...prev,
      [key]: value || undefined
    }));
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  const clearFilters = () => {
    setFilters({});
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  const handleSort = (field: string) => {
    setSortConfig(prev => ({
      sortBy: field,
      sortDir: prev.sortBy === field && prev.sortDir === 'asc' ? 'desc' : 'asc'
    }));
  };

  const handleSelectUser = (userId: number, checked: boolean) => {
    if (checked) {
      setSelectedUsers(prev => [...prev, userId]);
    } else {
      setSelectedUsers(prev => prev.filter(id => id !== userId));
    }
  };

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedUsers(users.map(user => user.id));
    } else {
      setSelectedUsers([]);
    }
  };

  const handleBulkAction = async (action: 'ACTIVATE' | 'DEACTIVATE' | 'RESET_PASSWORD') => {
    if (selectedUsers.length === 0) {
      toast.error('Please select users first');
      return;
    }

    const actionMessages = {
      ACTIVATE: 'activate selected users',
      DEACTIVATE: 'deactivate selected users',
      RESET_PASSWORD: 'reset passwords for selected users'
    };

    setShowConfirmModal({
      show: true,
      action,
      userIds: selectedUsers,
      message: `Are you sure you want to ${actionMessages[action]}?`
    });
  };

  const confirmBulkAction = async () => {
    const { action, userIds } = showConfirmModal;
    
    try {
      const request: BulkActionRequest = {
        userIds,
        action: action as 'ACTIVATE' | 'DEACTIVATE' | 'RESET_PASSWORD'
      };
      
      const response = await AdminUserAPI.performBulkActions(request);
      
      if (response.success && response.data) {
        const { successful, failed, errors, passwordResets } = response.data;
        
        // Show appropriate success message based on action type
        if (action === 'RESET_PASSWORD') {
          toast.success(`Password reset links sent to ${successful} user${successful !== 1 ? 's' : ''}${failed > 0 ? `, ${failed} failed` : ''}`);
        } else {
          toast.success(`${successful} users processed successfully${failed > 0 ? `, ${failed} failed` : ''}`);
        }
        
        if (errors.length > 0) {
          errors.forEach(error => toast.error(error));
        }
        
        loadUsers();
        setSelectedUsers([]);
      }
    } catch (error) {
      toast.error('Bulk action failed');
      console.error('Bulk action error:', error);
    } finally {
      setShowConfirmModal({ show: false, action: '', userIds: [], message: '' });
    }
  };

  const handleUserAction = async (userId: number, action: 'activate' | 'deactivate' | 'resetPassword' | 'unlock') => {
    try {
      switch (action) {
        case 'activate':
          await AdminUserAPI.activateUser(userId);
          toast.success('User activated successfully');
          break;
        case 'deactivate':
          await AdminUserAPI.deactivateUser(userId);
          toast.success('User deactivated successfully');
          break;
        case 'resetPassword':
          const response = await AdminUserAPI.resetUserPassword(userId);
          if (response.success) {
            toast.success('Password reset successfully');
          }
          break;
        case 'unlock':
          await AdminUserAPI.unlockUserAccount(userId);
          toast.success('Account unlocked successfully');
          break;
      }
      loadUsers();
    } catch (error) {
      toast.error(`Failed to ${action} user`);
      console.error(`${action} user error:`, error);
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Never';
    return new Date(dateString).toLocaleString();
  };

  const getStatusBadge = (status: string, isLocked?: boolean) => {
    if (isLocked) {
      return <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-800">Locked</span>;
    }
    
    if (status === 'ACTIVE') {
      return <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">Active</span>;
    } else {
      return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">Inactive</span>;
    }
  };

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">User Management</h1>
        <p className="text-gray-600">Manage system users and their access</p>
      </div>

      {/* Filters */}
      <div className="mb-6 p-4 bg-gray-50 rounded-lg">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
          <input
            type="text"
            placeholder="Search users..."
            value={filters.search || ''}
            onChange={(e) => handleFilterChange('search', e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="text"
            placeholder="Filter by first name..."
            value={filters.firstName || ''}
            onChange={(e) => handleFilterChange('firstName', e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="text"
            placeholder="Filter by email..."
            value={filters.emailAddress || ''}
            onChange={(e) => handleFilterChange('emailAddress', e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <select
            value={filters.status || ''}
            onChange={(e) => handleFilterChange('status', e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">All Status</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </div>
        <button
          onClick={clearFilters}
          className="px-4 py-2 text-sm text-blue-600 hover:text-blue-800"
        >
          Clear All Filters
        </button>
      </div>

      {/* Bulk Actions */}
      {selectedUsers.length > 0 && (
        <div className="mb-4 p-3 bg-blue-50 rounded-lg flex items-center justify-between">
          <span className="text-sm text-blue-800">
            {selectedUsers.length} user{selectedUsers.length !== 1 ? 's' : ''} selected
          </span>
          <div className="space-x-2">
            <button
              onClick={() => handleBulkAction('ACTIVATE')}
              className="px-3 py-1 text-sm bg-green-600 text-white rounded hover:bg-green-700"
            >
              Activate
            </button>
            <button
              onClick={() => handleBulkAction('DEACTIVATE')}
              className="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700"
            >
              Deactivate
            </button>
            <button
              onClick={() => handleBulkAction('RESET_PASSWORD')}
              className="px-3 py-1 text-sm bg-yellow-600 text-white rounded hover:bg-yellow-700"
            >
              Reset Passwords
            </button>
          </div>
        </div>
      )}

      {/* Users Table */}
      <div className="bg-white shadow rounded-lg overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  <input
                    type="checkbox"
                    checked={selectedUsers.length === users.length && users.length > 0}
                    onChange={(e) => handleSelectAll(e.target.checked)}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                </th>
                <th
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                  onClick={() => handleSort('displayName')}
                >
                  Name {sortConfig.sortBy === 'displayName' && (sortConfig.sortDir === 'asc' ? '↑' : '↓')}
                </th>
                <th
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                  onClick={() => handleSort('email')}
                >
                  Email {sortConfig.sortBy === 'email' && (sortConfig.sortDir === 'asc' ? '↑' : '↓')}
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Employee Code
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Roles
                </th>
                <th
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                  onClick={() => handleSort('lastLoginAt')}
                >
                  Last Login {sortConfig.sortBy === 'lastLoginAt' && (sortConfig.sortDir === 'asc' ? '↑' : '↓')}
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {loading ? (
                <tr>
                  <td colSpan={8} className="px-6 py-4 text-center text-gray-500">
                    Loading users...
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={8} className="px-6 py-4 text-center text-gray-500">
                    No users found
                  </td>
                </tr>
              ) : (
                users.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <input
                        type="checkbox"
                        checked={selectedUsers.includes(user.id)}
                        onChange={(e) => handleSelectUser(user.id, e.target.checked)}
                        className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                      />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {user.displayName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {user.email}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {user.employeeCode || '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getStatusBadge(user.status, !!user.accountLockedUntil)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {user.roles.map(role => role.name).join(', ')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatDate(user.lastLoginAt)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <div className="flex space-x-2">
                        {user.status === 'INACTIVE' ? (
                          <button
                            onClick={() => handleUserAction(user.id, 'activate')}
                            className="text-green-600 hover:text-green-900"
                          >
                            Activate
                          </button>
                        ) : (
                          <button
                            onClick={() => handleUserAction(user.id, 'deactivate')}
                            className="text-red-600 hover:text-red-900"
                          >
                            Deactivate
                          </button>
                        )}
                        <button
                          onClick={() => handleUserAction(user.id, 'resetPassword')}
                          className="text-yellow-600 hover:text-yellow-900"
                        >
                          Reset Password
                        </button>
                        {user.accountLockedUntil && (
                          <button
                            onClick={() => handleUserAction(user.id, 'unlock')}
                            className="text-blue-600 hover:text-blue-900"
                          >
                            Unlock
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {pagination.totalPages > 1 && (
          <div className="px-6 py-3 bg-gray-50 border-t border-gray-200">
            <div className="flex items-center justify-between">
              <div className="flex-1 flex justify-between sm:hidden">
                <button
                  onClick={() => setPagination(prev => ({ ...prev, page: Math.max(0, prev.page - 1) }))}
                  disabled={pagination.page === 0}
                  className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                >
                  Previous
                </button>
                <button
                  onClick={() => setPagination(prev => ({ ...prev, page: Math.min(prev.totalPages - 1, prev.page + 1) }))}
                  disabled={pagination.page >= pagination.totalPages - 1}
                  className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                >
                  Next
                </button>
              </div>
              <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm text-gray-700">
                    Showing {pagination.page * pagination.size + 1} to{' '}
                    {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of{' '}
                    {pagination.totalElements} results
                  </p>
                </div>
                <div>
                  <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                    <button
                      onClick={() => setPagination(prev => ({ ...prev, page: Math.max(0, prev.page - 1) }))}
                      disabled={pagination.page === 0}
                      className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                    >
                      Previous
                    </button>
                    <button
                      onClick={() => setPagination(prev => ({ ...prev, page: Math.min(prev.totalPages - 1, prev.page + 1) }))}
                      disabled={pagination.page >= pagination.totalPages - 1}
                      className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                    >
                      Next
                    </button>
                  </nav>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Confirmation Modal */}
      {showConfirmModal.show && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3 text-center">
              <h3 className="text-lg leading-6 font-medium text-gray-900">Confirm Action</h3>
              <div className="mt-2 px-7 py-3">
                <p className="text-sm text-gray-500">{showConfirmModal.message}</p>
              </div>
              <div className="items-center px-4 py-3">
                <button
                  onClick={confirmBulkAction}
                  className="px-4 py-2 bg-red-500 text-white text-base font-medium rounded-md w-24 mr-3 hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-300"
                >
                  Confirm
                </button>
                <button
                  onClick={() => setShowConfirmModal({ show: false, action: '', userIds: [], message: '' })}
                  className="px-4 py-2 bg-gray-500 text-white text-base font-medium rounded-md w-24 hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-300"
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}