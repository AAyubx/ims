'use client';

import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import { AdminUserAPI } from '@/lib/adminApi';
import { 
  UserResponseDto, 
  UserFilters, 
  BulkActionRequest, 
  CreateUserRequest, 
  UpdateUserRequest,
  RoleDto
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
  const [createFormData, setCreateFormData] = useState<CreateUserRequest>({
    employeeCode: '',
    email: '',
    displayName: '',
    roleIds: [],
    initialPassword: '',
    mustChangePassword: true
  });
  const [availableRoles, setAvailableRoles] = useState<RoleDto[]>([]);
  const [formErrors, setFormErrors] = useState<{
    employeeCode?: string;
    email?: string;
    displayName?: string;
    roleIds?: string;
    initialPassword?: string;
    mustChangePassword?: string;
  }>({});
  const [formTouched, setFormTouched] = useState<{
    employeeCode?: boolean;
    email?: boolean;
    displayName?: boolean;
    roleIds?: boolean;
    initialPassword?: boolean;
    mustChangePassword?: boolean;
  }>({});
  const [showConfirmModal, setShowConfirmModal] = useState<{
    show: boolean;
    action: string;
    userIds: number[];
    message: string;
  }>({ show: false, action: '', userIds: [], message: '' });

  useEffect(() => {
    loadUsers();
  }, [pagination.page, pagination.size, sortConfig, filters]);

  useEffect(() => {
    loadRoles();
  }, []);

  // Validation functions based on ui-plan specifications
  const validateEmail = (email: string): string => {
    if (!email) return 'Email is required';
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) return 'Invalid email address';
    return '';
  };

  const validateEmployeeCode = (code: string): string => {
    if (!code) return 'Employee code is required';
    const codeRegex = /^[A-Z0-9]{3,32}$/;
    if (!codeRegex.test(code)) return 'Employee code must be 3-32 alphanumeric characters (uppercase)';
    return '';
  };

  const validateDisplayName = (name: string): string => {
    if (!name) return 'Display name is required';
    if (name.length < 2) return 'Display name must be at least 2 characters';
    if (name.length > 50) return 'Display name must be less than 50 characters';
    return '';
  };

  const validatePassword = (password: string): { error: string; strength: number } => {
    if (!password) return { error: '', strength: 0 }; // Optional field
    
    const checks = {
      minLength: password.length >= 8,
      hasUppercase: /[A-Z]/.test(password),
      hasLowercase: /[a-z]/.test(password),
      hasNumbers: /\d/.test(password),
      hasSpecialChars: /[!@#$%^&*(),.?":{}|<>]/.test(password)
    };
    
    const strength = Object.values(checks).filter(Boolean).length;
    
    let error = '';
    if (password.length > 0 && password.length < 8) error = 'Password must be at least 8 characters';
    else if (password.length > 0 && !checks.hasUppercase) error = 'Password must contain at least one uppercase letter';
    else if (password.length > 0 && !checks.hasLowercase) error = 'Password must contain at least one lowercase letter';
    else if (password.length > 0 && !checks.hasNumbers) error = 'Password must contain at least one number';
    else if (password.length > 0 && !checks.hasSpecialChars) error = 'Password must contain at least one special character';
    
    return { error, strength };
  };

  const validateRoles = (roleIds: number[]): string => {
    if (!roleIds || roleIds.length === 0) return 'Please select a role';
    return '';
  };

  const validateField = (field: keyof CreateUserRequest, value: any) => {
    let error = '';
    
    switch (field) {
      case 'email':
        error = validateEmail(value as string);
        break;
      case 'employeeCode':
        error = validateEmployeeCode(value as string);
        break;
      case 'displayName':
        error = validateDisplayName(value as string);
        break;
      case 'roleIds':
        error = validateRoles(value as number[]);
        break;
      case 'initialPassword':
        error = validatePassword(value as string).error;
        break;
    }
    
    setFormErrors(prev => ({ ...prev, [field]: error }));
    return error === '';
  };

  const handleFieldChange = (field: keyof CreateUserRequest, value: any) => {
    setCreateFormData(prev => ({ ...prev, [field]: value }));
    
    // Real-time validation on change for immediate feedback
    if (formTouched[field]) {
      validateField(field, value);
    }
  };

  const handleFieldBlur = (field: keyof CreateUserRequest) => {
    setFormTouched(prev => ({ ...prev, [field]: true }));
    validateField(field, createFormData[field]);
  };

  const isFormValid = () => {
    const emailError = validateEmail(createFormData.email);
    const codeError = validateEmployeeCode(createFormData.employeeCode);
    const nameError = validateDisplayName(createFormData.displayName);
    const roleError = validateRoles(createFormData.roleIds);
    const passwordError = validatePassword(createFormData.initialPassword || '').error;
    
    return !emailError && !codeError && !nameError && !roleError && !passwordError;
  };

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

  const loadRoles = async () => {
    try {
      const response = await AdminUserAPI.getRoles();
      if (response.success && response.data) {
        setAvailableRoles(response.data);
      }
    } catch (error) {
      console.error('Load roles error:', error);
      toast.error('Failed to load roles');
    }
  };

  const handleCreateUser = async () => {
    try {
      console.log('Creating user with data:', createFormData);
      const response = await AdminUserAPI.createUser(createFormData);
      console.log('Create user response:', response);
      if (response.success) {
        toast.success('User created successfully');
        setShowCreateModal(false);
        setCreateFormData({
          employeeCode: '',
          email: '',
          displayName: '',
          roleIds: [],
          initialPassword: '',
          mustChangePassword: true
        });
        setFormErrors({});
        setFormTouched({});
        loadUsers();
      }
    } catch (error: any) {
      console.error('Create user error:', error);
      
      // Extract backend error message if available
      const errorMessage = error?.response?.data?.message || 
                          error?.response?.data?.error || 
                          error?.message || 
                          'Failed to create user';
      
      toast.error(errorMessage);
      
      // Log detailed error info for debugging
      if (error?.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
      }
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
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">User Management</h1>
          <p className="text-gray-600">Manage system users and their access</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 flex items-center space-x-2 cursor-pointer"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          <span>Create User</span>
        </button>
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
          className="px-4 py-2 text-sm text-blue-600 hover:text-blue-800 cursor-pointer"
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
              className="px-3 py-1 text-sm bg-green-600 text-white rounded hover:bg-green-700 cursor-pointer"
            >
              Activate
            </button>
            <button
              onClick={() => handleBulkAction('DEACTIVATE')}
              className="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700 cursor-pointer"
            >
              Deactivate
            </button>
            <button
              onClick={() => handleBulkAction('RESET_PASSWORD')}
              className="px-3 py-1 text-sm bg-yellow-600 text-white rounded hover:bg-yellow-700 cursor-pointer"
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
                            className="text-green-600 hover:text-green-900 cursor-pointer"
                          >
                            Activate
                          </button>
                        ) : (
                          <button
                            onClick={() => handleUserAction(user.id, 'deactivate')}
                            className="text-red-600 hover:text-red-900 cursor-pointer"
                          >
                            Deactivate
                          </button>
                        )}
                        <button
                          onClick={() => handleUserAction(user.id, 'resetPassword')}
                          className="text-yellow-600 hover:text-yellow-900 cursor-pointer"
                        >
                          Reset Password
                        </button>
                        {user.accountLockedUntil && (
                          <button
                            onClick={() => handleUserAction(user.id, 'unlock')}
                            className="text-blue-600 hover:text-blue-900 cursor-pointer"
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
                  className={`relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 ${pagination.page === 0 ? 'cursor-not-allowed' : 'cursor-pointer'}`}
                >
                  Previous
                </button>
                <button
                  onClick={() => setPagination(prev => ({ ...prev, page: Math.min(prev.totalPages - 1, prev.page + 1) }))}
                  disabled={pagination.page >= pagination.totalPages - 1}
                  className={`ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 ${pagination.page >= pagination.totalPages - 1 ? 'cursor-not-allowed' : 'cursor-pointer'}`}
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
                      className={`relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 ${pagination.page === 0 ? 'cursor-not-allowed' : 'cursor-pointer'}`}
                    >
                      Previous
                    </button>
                    <button
                      onClick={() => setPagination(prev => ({ ...prev, page: Math.min(prev.totalPages - 1, prev.page + 1) }))}
                      disabled={pagination.page >= pagination.totalPages - 1}
                      className={`relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 ${pagination.page >= pagination.totalPages - 1 ? 'cursor-not-allowed' : 'cursor-pointer'}`}
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

      {/* Create User Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-10 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">Create New User</h3>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Employee Code <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={createFormData.employeeCode}
                    onChange={(e) => handleFieldChange('employeeCode', e.target.value)}
                    onBlur={() => handleFieldBlur('employeeCode')}
                    className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 text-gray-900 ${
                      formErrors.employeeCode && formTouched.employeeCode
                        ? 'border-red-500 focus:ring-red-500' 
                        : formTouched.employeeCode && !formErrors.employeeCode
                        ? 'border-green-500 focus:ring-green-500'
                        : 'border-gray-300 focus:ring-blue-500'
                    }`}
                    placeholder="e.g. EMP001"
                    required
                  />
                  {formErrors.employeeCode && formTouched.employeeCode && (
                    <p className="text-red-500 text-sm mt-1">{formErrors.employeeCode}</p>
                  )}
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Email <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="email"
                    value={createFormData.email}
                    onChange={(e) => handleFieldChange('email', e.target.value)}
                    onBlur={() => handleFieldBlur('email')}
                    className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 text-gray-900 ${
                      formErrors.email && formTouched.email
                        ? 'border-red-500 focus:ring-red-500' 
                        : formTouched.email && !formErrors.email
                        ? 'border-green-500 focus:ring-green-500'
                        : 'border-gray-300 focus:ring-blue-500'
                    }`}
                    placeholder="user@example.com"
                    required
                  />
                  {formErrors.email && formTouched.email && (
                    <p className="text-red-500 text-sm mt-1">{formErrors.email}</p>
                  )}
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Display Name <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={createFormData.displayName}
                    onChange={(e) => handleFieldChange('displayName', e.target.value)}
                    onBlur={() => handleFieldBlur('displayName')}
                    className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 text-gray-900 ${
                      formErrors.displayName && formTouched.displayName
                        ? 'border-red-500 focus:ring-red-500' 
                        : formTouched.displayName && !formErrors.displayName
                        ? 'border-green-500 focus:ring-green-500'
                        : 'border-gray-300 focus:ring-blue-500'
                    }`}
                    placeholder="John Doe"
                    required
                  />
                  {formErrors.displayName && formTouched.displayName && (
                    <p className="text-red-500 text-sm mt-1">{formErrors.displayName}</p>
                  )}
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Role <span className="text-red-500">*</span>
                  </label>
                  <select
                    value={createFormData.roleIds[0] || ''}
                    onChange={(e) => {
                      const roleId = e.target.value ? [Number(e.target.value)] : [];
                      handleFieldChange('roleIds', roleId);
                    }}
                    onBlur={() => handleFieldBlur('roleIds')}
                    className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 text-gray-900 ${
                      formErrors.roleIds && formTouched.roleIds
                        ? 'border-red-500 focus:ring-red-500' 
                        : formTouched.roleIds && !formErrors.roleIds
                        ? 'border-green-500 focus:ring-green-500'
                        : 'border-gray-300 focus:ring-blue-500'
                    }`}
                    required
                  >
                    <option value="">Select a role...</option>
                    {availableRoles.map(role => (
                      <option key={role.id} value={role.id}>
                        {role.name}
                      </option>
                    ))}
                  </select>
                  {formErrors.roleIds && formTouched.roleIds && (
                    <p className="text-red-500 text-sm mt-1">{formErrors.roleIds}</p>
                  )}
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Initial Password</label>
                  <input
                    type="password"
                    value={createFormData.initialPassword}
                    onChange={(e) => handleFieldChange('initialPassword', e.target.value)}
                    onBlur={() => handleFieldBlur('initialPassword')}
                    className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 text-gray-900 ${
                      formErrors.initialPassword && formTouched.initialPassword
                        ? 'border-red-500 focus:ring-red-500' 
                        : formTouched.initialPassword && !formErrors.initialPassword && createFormData.initialPassword
                        ? 'border-green-500 focus:ring-green-500'
                        : 'border-gray-300 focus:ring-blue-500'
                    }`}
                    placeholder="Leave blank to auto-generate"
                  />
                  {formErrors.initialPassword && formTouched.initialPassword && (
                    <p className="text-red-500 text-sm mt-1">{formErrors.initialPassword}</p>
                  )}
                  {createFormData.initialPassword && (
                    <div className="mt-2">
                      <div className="flex items-center space-x-2">
                        <div className="flex space-x-1">
                          {[1, 2, 3, 4, 5].map((level) => {
                            const strength = validatePassword(createFormData.initialPassword).strength;
                            return (
                              <div
                                key={level}
                                className={`h-2 w-4 rounded ${
                                  level <= strength
                                    ? strength <= 2
                                      ? 'bg-red-500'
                                      : strength <= 3
                                      ? 'bg-yellow-500'
                                      : strength <= 4
                                      ? 'bg-blue-500'
                                      : 'bg-green-500'
                                    : 'bg-gray-200'
                                }`}
                              />
                            );
                          })}
                        </div>
                        <span className="text-xs text-gray-600">
                          {validatePassword(createFormData.initialPassword).strength <= 2
                            ? 'Weak'
                            : validatePassword(createFormData.initialPassword).strength <= 3
                            ? 'Fair'
                            : validatePassword(createFormData.initialPassword).strength <= 4
                            ? 'Good'
                            : 'Strong'}
                        </span>
                      </div>
                    </div>
                  )}
                  <p className="text-xs text-gray-500 mt-1">Leave blank to auto-generate a secure password</p>
                </div>
                
                <div>
                  <label className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      checked={createFormData.mustChangePassword}
                      onChange={(e) => handleFieldChange('mustChangePassword', e.target.checked)}
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                    <span className="text-sm text-gray-700">Require password change on first login</span>
                  </label>
                </div>
              </div>
              
              <div className="flex justify-end space-x-3 mt-6">
                <button
                  onClick={() => {
                    setShowCreateModal(false);
                    setCreateFormData({
                      employeeCode: '',
                      email: '',
                      displayName: '',
                      roleIds: [],
                      initialPassword: '',
                      mustChangePassword: true
                    });
                    setFormErrors({});
                    setFormTouched({});
                  }}
                  className="px-4 py-2 bg-gray-300 text-gray-700 text-sm font-medium rounded-md hover:bg-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-300 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  onClick={handleCreateUser}
                  disabled={!isFormValid()}
                  className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Create User
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

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
                  className="px-4 py-2 bg-red-500 text-white text-base font-medium rounded-md w-24 mr-3 hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-300 cursor-pointer"
                >
                  Confirm
                </button>
                <button
                  onClick={() => setShowConfirmModal({ show: false, action: '', userIds: [], message: '' })}
                  className="px-4 py-2 bg-gray-500 text-white text-base font-medium rounded-md w-24 hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-300 cursor-pointer"
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