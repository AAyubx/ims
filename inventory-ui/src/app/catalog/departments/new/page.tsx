'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { 
  BuildingOfficeIcon,
  ArrowLeftIcon,
  CheckIcon,
  XMarkIcon
} from '@heroicons/react/24/outline';

interface DepartmentForm {
  code: string;
  name: string;
  description: string;
  manager: string;
  isActive: boolean;
}

export default function NewDepartmentPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [form, setForm] = useState<DepartmentForm>({
    code: '',
    name: '',
    description: '',
    manager: '',
    isActive: true
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    setForm(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
    }));
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!form.code.trim()) {
      newErrors.code = 'Department code is required';
    } else if (form.code.length < 2 || form.code.length > 64) {
      newErrors.code = 'Code must be between 2 and 64 characters';
    } else if (!/^[A-Z0-9_-]+$/.test(form.code)) {
      newErrors.code = 'Code must contain only uppercase letters, numbers, hyphens, and underscores';
    }

    if (!form.name.trim()) {
      newErrors.name = 'Department name is required';
    } else if (form.name.length < 2 || form.name.length > 255) {
      newErrors.name = 'Name must be between 2 and 255 characters';
    }

    if (form.description && form.description.length > 1000) {
      newErrors.description = 'Description cannot exceed 1000 characters';
    }

    if (form.manager && form.manager.length > 255) {
      newErrors.manager = 'Manager name cannot exceed 255 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    
    try {
      // Mock API call - in real implementation, this would call the actual API
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Simulate success
      router.push('/catalog/departments');
    } catch (error) {
      console.error('Error creating department:', error);
      setErrors({ general: 'Failed to create department. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    router.back();
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="border-b border-gray-200 pb-4">
        <div className="flex items-center space-x-3">
          <Link
            href="/catalog/departments"
            className="inline-flex items-center text-sm text-gray-500 hover:text-gray-700"
          >
            <ArrowLeftIcon className="h-4 w-4 mr-1" />
            Back to Departments
          </Link>
        </div>
        <div className="flex items-center space-x-3 mt-2">
          <BuildingOfficeIcon className="h-8 w-8 text-blue-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Create New Department</h1>
            <p className="mt-1 text-sm text-gray-600">
              Add a new department to organize your product catalog
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className="bg-white shadow rounded-lg">
        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {/* General Error */}
          {errors.general && (
            <div className="rounded-md bg-red-50 p-4">
              <div className="flex">
                <XMarkIcon className="h-5 w-5 text-red-400" />
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-red-800">Error</h3>
                  <div className="mt-2 text-sm text-red-700">{errors.general}</div>
                </div>
              </div>
            </div>
          )}

          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            {/* Department Code */}
            <div>
              <label htmlFor="code" className="block text-sm font-medium text-gray-700">
                Department Code *
              </label>
              <input
                type="text"
                name="code"
                id="code"
                value={form.code}
                onChange={handleChange}
                placeholder="e.g., ELEC, CLTH, HOME"
                className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm text-gray-900 placeholder-gray-500 px-3 py-2 ${
                  errors.code ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
                }`}
              />
              {errors.code && (
                <p className="mt-2 text-sm text-red-600">{errors.code}</p>
              )}
              <p className="mt-2 text-sm text-gray-500">
                Unique identifier for the department (uppercase letters, numbers, hyphens, underscores)
              </p>
            </div>

            {/* Department Name */}
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                Department Name *
              </label>
              <input
                type="text"
                name="name"
                id="name"
                value={form.name}
                onChange={handleChange}
                placeholder="e.g., Electronics, Clothing, Home & Garden"
                className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm text-gray-900 placeholder-gray-500 px-3 py-2 ${
                  errors.name ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
                }`}
              />
              {errors.name && (
                <p className="mt-2 text-sm text-red-600">{errors.name}</p>
              )}
            </div>
          </div>

          {/* Description */}
          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700">
              Description
            </label>
            <textarea
              name="description"
              id="description"
              rows={3}
              value={form.description}
              onChange={handleChange}
              placeholder="Brief description of the department..."
              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm text-gray-900 placeholder-gray-500 px-3 py-2 ${
                errors.description ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
              }`}
            />
            {errors.description && (
              <p className="mt-2 text-sm text-red-600">{errors.description}</p>
            )}
            <p className="mt-2 text-sm text-gray-500">
              Optional description to help identify this department
            </p>
          </div>

          {/* Manager */}
          <div>
            <label htmlFor="manager" className="block text-sm font-medium text-gray-700">
              Department Manager
            </label>
            <input
              type="text"
              name="manager"
              id="manager"
              value={form.manager}
              onChange={handleChange}
              placeholder="e.g., John Smith"
              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm text-gray-900 placeholder-gray-500 px-3 py-2 ${
                errors.manager ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
              }`}
            />
            {errors.manager && (
              <p className="mt-2 text-sm text-red-600">{errors.manager}</p>
            )}
            <p className="mt-2 text-sm text-gray-500">
              Name of the person responsible for this department
            </p>
          </div>

          {/* Active Status */}
          <div className="flex items-center">
            <input
              id="isActive"
              name="isActive"
              type="checkbox"
              checked={form.isActive}
              onChange={handleChange}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label htmlFor="isActive" className="ml-2 block text-sm text-gray-900">
              Active Department
            </label>
          </div>
          <p className="text-sm text-gray-500">
            Active departments are available for category assignment and catalog operations
          </p>

          {/* Form Actions */}
          <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
            <button
              type="button"
              onClick={handleCancel}
              className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Creating...
                </>
              ) : (
                <>
                  <CheckIcon className="-ml-1 mr-2 h-4 w-4" />
                  Create Department
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}