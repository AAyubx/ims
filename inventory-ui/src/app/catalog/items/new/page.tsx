'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { 
  CubeIcon,
  ArrowLeftIcon,
  CheckIcon,
  XMarkIcon
} from '@heroicons/react/24/outline';

interface ItemForm {
  sku: string;
  name: string;
  shortName: string;
  description: string;
  departmentId: string;
  categoryId: string;
  brandId: string;
  itemType: string;
  basePrice: string;
  isActive: boolean;
}

export default function NewItemPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [form, setForm] = useState<ItemForm>({
    sku: '',
    name: '',
    shortName: '',
    description: '',
    departmentId: '',
    categoryId: '',
    brandId: '',
    itemType: 'SIMPLE',
    basePrice: '',
    isActive: true
  });

  const departments = [
    { id: '1', name: 'Electronics' },
    { id: '2', name: 'Clothing' },
    { id: '3', name: 'Home & Garden' }
  ];

  const categories = [
    { id: '1', name: 'Mobile Phones', departmentId: '1' },
    { id: '2', name: 'Laptops', departmentId: '1' },
    { id: '3', name: 'Men\'s Clothing', departmentId: '2' },
    { id: '4', name: 'Women\'s Clothing', departmentId: '2' }
  ];

  const brands = [
    { id: '1', name: 'Apple' },
    { id: '2', name: 'Samsung' },
    { id: '3', name: 'Nike' },
    { id: '4', name: 'Adidas' }
  ];

  const itemTypes = [
    { value: 'SIMPLE', label: 'Simple Item' },
    { value: 'VARIANT', label: 'Variant Item' },
    { value: 'BUNDLE', label: 'Bundle' }
  ];

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
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

    if (!form.sku.trim()) {
      newErrors.sku = 'SKU is required';
    } else if (form.sku.length < 2) {
      newErrors.sku = 'SKU must be at least 2 characters';
    }

    if (!form.name.trim()) {
      newErrors.name = 'Name is required';
    } else if (form.name.length < 2) {
      newErrors.name = 'Name must be at least 2 characters';
    }

    if (!form.departmentId) {
      newErrors.departmentId = 'Department is required';
    }

    if (!form.categoryId) {
      newErrors.categoryId = 'Category is required';
    }

    if (form.basePrice && isNaN(Number(form.basePrice))) {
      newErrors.basePrice = 'Base price must be a valid number';
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
      // TODO: Replace with actual API call
      console.log('Creating item:', form);
      
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Navigate back to items list
      router.push('/catalog/items');
    } catch (error) {
      console.error('Error creating item:', error);
      setErrors({ submit: 'Failed to create item. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    router.push('/catalog/items');
  };

  const filteredCategories = categories.filter(cat => 
    !form.departmentId || cat.departmentId === form.departmentId
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <Link
            href="/catalog/items"
            className="p-2 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100"
          >
            <ArrowLeftIcon className="h-5 w-5" />
          </Link>
          <CubeIcon className="h-8 w-8 text-blue-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Create New Item</h1>
            <p className="mt-1 text-sm text-gray-600">
              Add a new item to your catalog
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className="bg-white shadow rounded-lg">
        <form onSubmit={handleSubmit} className="space-y-6 p-6">
          {errors.submit && (
            <div className="bg-red-50 border border-red-200 rounded-md p-4">
              <div className="flex">
                <XMarkIcon className="h-5 w-5 text-red-400" />
                <div className="ml-3">
                  <p className="text-sm text-red-800">{errors.submit}</p>
                </div>
              </div>
            </div>
          )}

          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            {/* SKU */}
            <div>
              <label htmlFor="sku" className="block text-sm font-medium text-gray-700">
                SKU *
              </label>
              <input
                type="text"
                id="sku"
                name="sku"
                value={form.sku}
                onChange={handleChange}
                className={`mt-1 block w-full rounded-md shadow-sm text-gray-900 placeholder-gray-500 px-3 py-2 ${
                  errors.sku 
                    ? 'border-red-300 focus:border-red-500 focus:ring-red-500' 
                    : 'border-gray-300 focus:border-blue-500 focus:ring-blue-500'
                }`}
                placeholder="Enter unique SKU"
              />
              {errors.sku && (
                <p className="mt-1 text-sm text-red-600">{errors.sku}</p>
              )}
            </div>

            {/* Name */}
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                Name *
              </label>
              <input
                type="text"
                id="name"
                name="name"
                value={form.name}
                onChange={handleChange}
                className={`mt-1 block w-full rounded-md shadow-sm text-gray-900 placeholder-gray-500 px-3 py-2 ${
                  errors.name 
                    ? 'border-red-300 focus:border-red-500 focus:ring-red-500' 
                    : 'border-gray-300 focus:border-blue-500 focus:ring-blue-500'
                }`}
                placeholder="Enter item name"
              />
              {errors.name && (
                <p className="mt-1 text-sm text-red-600">{errors.name}</p>
              )}
            </div>

            {/* Short Name */}
            <div>
              <label htmlFor="shortName" className="block text-sm font-medium text-gray-700">
                Short Name
              </label>
              <input
                type="text"
                id="shortName"
                name="shortName"
                value={form.shortName}
                onChange={handleChange}
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 text-gray-900 placeholder-gray-500 px-3 py-2"
                placeholder="Enter short name"
              />
            </div>

            {/* Item Type */}
            <div>
              <label htmlFor="itemType" className="block text-sm font-medium text-gray-700">
                Item Type *
              </label>
              <select
                id="itemType"
                name="itemType"
                value={form.itemType}
                onChange={handleChange}
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 text-gray-900 px-3 py-2"
              >
                {itemTypes.map(type => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Department */}
            <div>
              <label htmlFor="departmentId" className="block text-sm font-medium text-gray-700">
                Department *
              </label>
              <select
                id="departmentId"
                name="departmentId"
                value={form.departmentId}
                onChange={handleChange}
                className={`mt-1 block w-full rounded-md shadow-sm text-gray-900 px-3 py-2 ${
                  errors.departmentId 
                    ? 'border-red-300 focus:border-red-500 focus:ring-red-500' 
                    : 'border-gray-300 focus:border-blue-500 focus:ring-blue-500'
                }`}
              >
                <option value="">Select Department</option>
                {departments.map(dept => (
                  <option key={dept.id} value={dept.id}>
                    {dept.name}
                  </option>
                ))}
              </select>
              {errors.departmentId && (
                <p className="mt-1 text-sm text-red-600">{errors.departmentId}</p>
              )}
            </div>

            {/* Category */}
            <div>
              <label htmlFor="categoryId" className="block text-sm font-medium text-gray-700">
                Category *
              </label>
              <select
                id="categoryId"
                name="categoryId"
                value={form.categoryId}
                onChange={handleChange}
                disabled={!form.departmentId}
                className={`mt-1 block w-full rounded-md shadow-sm text-gray-900 px-3 py-2 ${
                  errors.categoryId 
                    ? 'border-red-300 focus:border-red-500 focus:ring-red-500' 
                    : 'border-gray-300 focus:border-blue-500 focus:ring-blue-500'
                } ${!form.departmentId ? 'bg-gray-100 cursor-not-allowed text-gray-500' : ''}`}
              >
                <option value="">Select Category</option>
                {filteredCategories.map(cat => (
                  <option key={cat.id} value={cat.id}>
                    {cat.name}
                  </option>
                ))}
              </select>
              {errors.categoryId && (
                <p className="mt-1 text-sm text-red-600">{errors.categoryId}</p>
              )}
            </div>

            {/* Brand */}
            <div>
              <label htmlFor="brandId" className="block text-sm font-medium text-gray-700">
                Brand
              </label>
              <select
                id="brandId"
                name="brandId"
                value={form.brandId}
                onChange={handleChange}
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 text-gray-900 px-3 py-2"
              >
                <option value="">Select Brand</option>
                {brands.map(brand => (
                  <option key={brand.id} value={brand.id}>
                    {brand.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Base Price */}
            <div>
              <label htmlFor="basePrice" className="block text-sm font-medium text-gray-700">
                Base Price
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span className="text-gray-500 sm:text-sm">$</span>
                </div>
                <input
                  type="text"
                  id="basePrice"
                  name="basePrice"
                  value={form.basePrice}
                  onChange={handleChange}
                  className={`block w-full pl-7 pr-3 py-2 rounded-md shadow-sm text-gray-900 placeholder-gray-500 ${
                    errors.basePrice 
                      ? 'border-red-300 focus:border-red-500 focus:ring-red-500' 
                      : 'border-gray-300 focus:border-blue-500 focus:ring-blue-500'
                  }`}
                  placeholder="0.00"
                />
              </div>
              {errors.basePrice && (
                <p className="mt-1 text-sm text-red-600">{errors.basePrice}</p>
              )}
            </div>
          </div>

          {/* Description */}
          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700">
              Description
            </label>
            <textarea
              id="description"
              name="description"
              value={form.description}
              onChange={handleChange}
              rows={3}
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 text-gray-900 placeholder-gray-500 px-3 py-2"
              placeholder="Enter item description"
            />
          </div>

          {/* Active Status */}
          <div className="flex items-center">
            <input
              type="checkbox"
              id="isActive"
              name="isActive"
              checked={form.isActive}
              onChange={handleChange}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label htmlFor="isActive" className="ml-2 block text-sm text-gray-900">
              Active (item is available for use)
            </label>
          </div>

          {/* Actions */}
          <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
            <button
              type="button"
              onClick={handleCancel}
              className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
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
                  <div className="animate-spin -ml-1 mr-3 h-5 w-5 border-2 border-white border-t-transparent rounded-full"></div>
                  Creating...
                </>
              ) : (
                <>
                  <CheckIcon className="-ml-1 mr-2 h-5 w-5" />
                  Create Item
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}