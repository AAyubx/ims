'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { 
  FolderIcon,
  ArrowLeftIcon,
  CheckIcon,
  XMarkIcon
} from '@heroicons/react/24/outline';

interface CategoryForm {
  code: string;
  name: string;
  description: string;
  departmentId: string;
  parentId: string;
  sortOrder: number;
}

export default function NewCategoryPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [form, setForm] = useState<CategoryForm>({
    code: '',
    name: '',
    description: '',
    departmentId: '',
    parentId: '',
    sortOrder: 0
  });

  // Mock data for departments and categories
  const departments = [
    { id: 1, name: 'Electronics' },
    { id: 2, name: 'Clothing' },
    { id: 3, name: 'Home & Garden' }
  ];

  const parentCategories = [
    { id: 1, name: 'Mobile Phones', departmentId: 1 },
    { id: 2, name: 'Laptops', departmentId: 1 },
    { id: 3, name: 'Men\'s Clothing', departmentId: 2 },
    { id: 4, name: 'Women\'s Clothing', departmentId: 2 }
  ];

  const filteredParentCategories = parentCategories.filter(cat => 
    !form.departmentId || cat.departmentId === Number(form.departmentId)
  );

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    setForm(prev => ({
      ...prev,
      [name]: type === 'number' ? Number(value) : value
    }));
    
    // Clear parent category when department changes
    if (name === 'departmentId') {
      setForm(prev => ({ ...prev, parentId: '' }));
    }
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!form.code.trim()) {
      newErrors.code = 'Category code is required';
    } else if (form.code.length < 2 || form.code.length > 64) {
      newErrors.code = 'Code must be between 2 and 64 characters';
    } else if (!/^[A-Z0-9_-]+$/.test(form.code)) {
      newErrors.code = 'Code must contain only uppercase letters, numbers, hyphens, and underscores';
    }

    if (!form.name.trim()) {
      newErrors.name = 'Category name is required';
    } else if (form.name.length < 2 || form.name.length > 255) {
      newErrors.name = 'Name must be between 2 and 255 characters';
    }

    if (!form.departmentId) {
      newErrors.departmentId = 'Department is required';
    }

    if (form.description && form.description.length > 1000) {
      newErrors.description = 'Description cannot exceed 1000 characters';
    }

    if (form.sortOrder < 0) {
      newErrors.sortOrder = 'Sort order must be a positive number';
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
      router.push('/catalog/categories');
    } catch (error) {
      console.error('Error creating category:', error);
      setErrors({ general: 'Failed to create category. Please try again.' });
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
            href="/catalog/categories"
            className="inline-flex items-center text-sm text-gray-500 hover:text-gray-700"
          >
            <ArrowLeftIcon className="h-4 w-4 mr-1" />
            Back to Categories
          </Link>
        </div>
        <div className="flex items-center space-x-3 mt-2">
          <FolderIcon className="h-8 w-8 text-purple-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Create New Category</h1>
            <p className="mt-1 text-sm text-gray-600">
              Add a new category to organize your product catalog
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
            {/* Category Code */}
            <div>
              <label htmlFor="code" className="block text-sm font-medium text-gray-700">
                Category Code *
              </label>
              <input
                type="text"
                name="code"
                id="code"
                value={form.code}
                onChange={handleChange}
                placeholder="e.g., ELEC_PHONES, CLTH_MENS"
                className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 sm:text-sm ${
                  errors.code ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
                }`}
              />
              {errors.code && (
                <p className="mt-2 text-sm text-red-600">{errors.code}</p>
              )}
              <p className="mt-2 text-sm text-gray-500">
                Unique identifier for the category (uppercase letters, numbers, hyphens, underscores)
              </p>
            </div>

            {/* Category Name */}
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                Category Name *
              </label>
              <input
                type="text"
                name="name"
                id="name"
                value={form.name}
                onChange={handleChange}
                placeholder="e.g., Mobile Phones, Men's Clothing"
                className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 sm:text-sm ${
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
              placeholder="Brief description of the category..."
              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 sm:text-sm ${
                errors.description ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
              }`}
            />
            {errors.description && (
              <p className="mt-2 text-sm text-red-600">{errors.description}</p>
            )}
            <p className="mt-2 text-sm text-gray-500">
              Optional description to help identify this category
            </p>
          </div>

          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            {/* Department */}
            <div>
              <label htmlFor="departmentId" className="block text-sm font-medium text-gray-700">
                Department *
              </label>
              <select
                name="departmentId"
                id="departmentId"
                value={form.departmentId}
                onChange={handleChange}
                className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 sm:text-sm ${
                  errors.departmentId ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
                }`}
              >
                <option value="">Select a department</option>
                {departments.map(dept => (
                  <option key={dept.id} value={dept.id}>{dept.name}</option>
                ))}
              </select>
              {errors.departmentId && (
                <p className="mt-2 text-sm text-red-600">{errors.departmentId}</p>
              )}
              <p className="mt-2 text-sm text-gray-500">
                The department this category belongs to
              </p>
            </div>

            {/* Parent Category */}
            <div>
              <label htmlFor="parentId" className="block text-sm font-medium text-gray-700">
                Parent Category
              </label>
              <select
                name="parentId"
                id="parentId"
                value={form.parentId}
                onChange={handleChange}
                disabled={!form.departmentId}
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 sm:text-sm disabled:bg-gray-100 disabled:cursor-not-allowed"
              >
                <option value="">No parent (root category)</option>
                {filteredParentCategories.map(cat => (
                  <option key={cat.id} value={cat.id}>{cat.name}</option>
                ))}
              </select>
              <p className="mt-2 text-sm text-gray-500">
                Optional parent category for hierarchical organization
              </p>
            </div>
          </div>

          {/* Sort Order */}
          <div>
            <label htmlFor="sortOrder" className="block text-sm font-medium text-gray-700">
              Sort Order
            </label>
            <input
              type="number"
              name="sortOrder"
              id="sortOrder"
              value={form.sortOrder}
              onChange={handleChange}
              min="0"
              className={`mt-1 block w-full sm:w-32 rounded-md border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 sm:text-sm ${
                errors.sortOrder ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
              }`}
            />
            {errors.sortOrder && (
              <p className="mt-2 text-sm text-red-600">{errors.sortOrder}</p>
            )}
            <p className="mt-2 text-sm text-gray-500">
              Display order for this category (0 = first)
            </p>
          </div>

          {/* Form Actions */}
          <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
            <button
              type="button"
              onClick={handleCancel}
              className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 disabled:opacity-50 disabled:cursor-not-allowed"
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
                  Create Category
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}