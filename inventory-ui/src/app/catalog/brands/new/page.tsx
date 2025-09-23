'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { 
  TagIcon,
  ArrowLeftIcon,
  CheckIcon,
  XMarkIcon
} from '@heroicons/react/24/outline';

interface BrandForm {
  code: string;
  name: string;
  description: string;
  vendor: string;
  logoUrl: string;
  isActive: boolean;
}

export default function NewBrandPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [form, setForm] = useState<BrandForm>({
    code: '',
    name: '',
    description: '',
    vendor: '',
    logoUrl: '',
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
      newErrors.code = 'Brand code is required';
    } else if (form.code.length < 2 || form.code.length > 64) {
      newErrors.code = 'Code must be between 2 and 64 characters';
    } else if (!/^[A-Z0-9_-]+$/.test(form.code)) {
      newErrors.code = 'Code must contain only uppercase letters, numbers, hyphens, and underscores';
    }

    if (!form.name.trim()) {
      newErrors.name = 'Brand name is required';
    } else if (form.name.length < 2 || form.name.length > 255) {
      newErrors.name = 'Name must be between 2 and 255 characters';
    }

    if (form.description && form.description.length > 1000) {
      newErrors.description = 'Description cannot exceed 1000 characters';
    }

    if (form.vendor && form.vendor.length > 255) {
      newErrors.vendor = 'Vendor name cannot exceed 255 characters';
    }

    if (form.logoUrl && form.logoUrl.length > 512) {
      newErrors.logoUrl = 'Logo URL cannot exceed 512 characters';
    }

    // Basic URL validation if logoUrl is provided
    if (form.logoUrl && form.logoUrl.trim()) {
      try {
        new URL(form.logoUrl);
      } catch {
        newErrors.logoUrl = 'Please enter a valid URL';
      }
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
      router.push('/catalog/brands');
    } catch (error) {
      console.error('Error creating brand:', error);
      setErrors({ general: 'Failed to create brand. Please try again.' });
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
            href="/catalog/brands"
            className="inline-flex items-center text-sm text-gray-500 hover:text-gray-700"
          >
            <ArrowLeftIcon className="h-4 w-4 mr-1" />
            Back to Brands
          </Link>
        </div>
        <div className="flex items-center space-x-3 mt-2">
          <TagIcon className="h-8 w-8 text-green-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Create New Brand</h1>
            <p className="mt-1 text-sm text-gray-600">
              Add a new brand to organize your product catalog
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
            {/* Brand Code */}
            <div>
              <label htmlFor="code" className="block text-sm font-medium text-gray-700">
                Brand Code *
              </label>
              <input
                type="text"
                name="code"
                id="code"
                value={form.code}
                onChange={handleChange}
                placeholder="e.g., APPLE, SAMSUNG, NIKE"
                className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-green-500 focus:ring-green-500 sm:text-sm ${
                  errors.code ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
                }`}
              />
              {errors.code && (
                <p className="mt-2 text-sm text-red-600">{errors.code}</p>
              )}
              <p className="mt-2 text-sm text-gray-500">
                Unique identifier for the brand (uppercase letters, numbers, hyphens, underscores)
              </p>
            </div>

            {/* Brand Name */}
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                Brand Name *
              </label>
              <input
                type="text"
                name="name"
                id="name"
                value={form.name}
                onChange={handleChange}
                placeholder="e.g., Apple, Samsung, Nike"
                className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-green-500 focus:ring-green-500 sm:text-sm ${
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
              placeholder="Brief description of the brand..."
              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-green-500 focus:ring-green-500 sm:text-sm ${
                errors.description ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
              }`}
            />
            {errors.description && (
              <p className="mt-2 text-sm text-red-600">{errors.description}</p>
            )}
            <p className="mt-2 text-sm text-gray-500">
              Optional description to help identify this brand
            </p>
          </div>

          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            {/* Vendor */}
            <div>
              <label htmlFor="vendor" className="block text-sm font-medium text-gray-700">
                Vendor/Manufacturer
              </label>
              <input
                type="text"
                name="vendor"
                id="vendor"
                value={form.vendor}
                onChange={handleChange}
                placeholder="e.g., Apple Inc., Samsung Electronics"
                className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-green-500 focus:ring-green-500 sm:text-sm ${
                  errors.vendor ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
                }`}
              />
              {errors.vendor && (
                <p className="mt-2 text-sm text-red-600">{errors.vendor}</p>
              )}
              <p className="mt-2 text-sm text-gray-500">
                Name of the company that manufactures this brand
              </p>
            </div>

            {/* Logo URL */}
            <div>
              <label htmlFor="logoUrl" className="block text-sm font-medium text-gray-700">
                Logo URL
              </label>
              <input
                type="url"
                name="logoUrl"
                id="logoUrl"
                value={form.logoUrl}
                onChange={handleChange}
                placeholder="https://example.com/logo.png"
                className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-green-500 focus:ring-green-500 sm:text-sm ${
                  errors.logoUrl ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''
                }`}
              />
              {errors.logoUrl && (
                <p className="mt-2 text-sm text-red-600">{errors.logoUrl}</p>
              )}
              <p className="mt-2 text-sm text-gray-500">
                Optional URL to the brand logo image
              </p>
            </div>
          </div>

          {/* Active Status */}
          <div className="flex items-center">
            <input
              id="isActive"
              name="isActive"
              type="checkbox"
              checked={form.isActive}
              onChange={handleChange}
              className="h-4 w-4 text-green-600 focus:ring-green-500 border-gray-300 rounded"
            />
            <label htmlFor="isActive" className="ml-2 block text-sm text-gray-900">
              Active Brand
            </label>
          </div>
          <p className="text-sm text-gray-500">
            Active brands are available for product assignment and catalog operations
          </p>

          {/* Form Actions */}
          <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
            <button
              type="button"
              onClick={handleCancel}
              className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-50 disabled:cursor-not-allowed"
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
                  Create Brand
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}