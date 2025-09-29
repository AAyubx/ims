'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { 
  CogIcon,
  ArrowLeftIcon,
  CheckIcon,
  XMarkIcon,
  PlusIcon,
  TrashIcon
} from '@heroicons/react/24/outline';

interface AttributeForm {
  code: string;
  name: string;
  description: string;
  dataType: string;
  isRequired: boolean;
  allowedValues: string[];
}

export default function NewAttributePage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [newAllowedValue, setNewAllowedValue] = useState('');
  const [form, setForm] = useState<AttributeForm>({
    code: '',
    name: '',
    description: '',
    dataType: 'TEXT',
    isRequired: false,
    allowedValues: []
  });

  const dataTypes = [
    { value: 'TEXT', label: 'Text' },
    { value: 'NUMBER', label: 'Number' },
    { value: 'BOOLEAN', label: 'Boolean (Yes/No)' },
    { value: 'LIST', label: 'List (Select from options)' },
    { value: 'DATE', label: 'Date' }
  ];

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    setForm(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
    }));
    
    // Clear allowed values if changing away from LIST type
    if (name === 'dataType' && value !== 'LIST') {
      setForm(prev => ({ ...prev, allowedValues: [] }));
    }
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const addAllowedValue = () => {
    if (newAllowedValue.trim() && !form.allowedValues.includes(newAllowedValue.trim())) {
      setForm(prev => ({
        ...prev,
        allowedValues: [...prev.allowedValues, newAllowedValue.trim()]
      }));
      setNewAllowedValue('');
    }
  };

  const removeAllowedValue = (index: number) => {
    setForm(prev => ({
      ...prev,
      allowedValues: prev.allowedValues.filter((_, i) => i !== index)
    }));
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!form.code.trim()) {
      newErrors.code = 'Code is required';
    } else if (form.code.length < 2) {
      newErrors.code = 'Code must be at least 2 characters';
    } else if (!/^[A-Z0-9_]+$/.test(form.code)) {
      newErrors.code = 'Code must contain only uppercase letters, numbers, and underscores';
    }

    if (!form.name.trim()) {
      newErrors.name = 'Name is required';
    } else if (form.name.length < 2) {
      newErrors.name = 'Name must be at least 2 characters';
    }

    if (form.dataType === 'LIST' && form.allowedValues.length === 0) {
      newErrors.allowedValues = 'At least one allowed value is required for LIST type';
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
      console.log('Creating attribute:', form);
      
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Navigate back to attributes list
      router.push('/catalog/attributes');
    } catch (error) {
      console.error('Error creating attribute:', error);
      setErrors({ submit: 'Failed to create attribute. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    router.push('/catalog/attributes');
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      addAllowedValue();
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <Link
            href="/catalog/attributes"
            className="p-2 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100"
          >
            <ArrowLeftIcon className="h-5 w-5" />
          </Link>
          <CogIcon className="h-8 w-8 text-indigo-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Create New Attribute</h1>
            <p className="mt-1 text-sm text-gray-600">
              Define a new product attribute for items
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
            {/* Code */}
            <div>
              <label htmlFor="code" className="block text-sm font-medium text-gray-700">
                Code *
              </label>
              <input
                type="text"
                id="code"
                name="code"
                value={form.code}
                onChange={handleChange}
                className={`mt-1 block w-full rounded-md shadow-sm text-gray-900 placeholder-gray-500 px-3 py-2 ${
                  errors.code 
                    ? 'border-red-300 focus:border-red-500 focus:ring-red-500' 
                    : 'border-gray-300 focus:border-indigo-500 focus:ring-indigo-500'
                }`}
                placeholder="e.g., COLOR, SIZE, MATERIAL"
                style={{ textTransform: 'uppercase' }}
              />
              {errors.code && (
                <p className="mt-1 text-sm text-red-600">{errors.code}</p>
              )}
              <p className="mt-1 text-xs text-gray-500">
                Use uppercase letters, numbers, and underscores only
              </p>
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
                    : 'border-gray-300 focus:border-indigo-500 focus:ring-indigo-500'
                }`}
                placeholder="e.g., Color, Size, Material"
              />
              {errors.name && (
                <p className="mt-1 text-sm text-red-600">{errors.name}</p>
              )}
            </div>

            {/* Data Type */}
            <div>
              <label htmlFor="dataType" className="block text-sm font-medium text-gray-700">
                Data Type *
              </label>
              <select
                id="dataType"
                name="dataType"
                value={form.dataType}
                onChange={handleChange}
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-gray-900 px-3 py-2"
              >
                {dataTypes.map(type => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Required */}
            <div className="flex items-center pt-6">
              <input
                type="checkbox"
                id="isRequired"
                name="isRequired"
                checked={form.isRequired}
                onChange={handleChange}
                className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
              />
              <label htmlFor="isRequired" className="ml-2 block text-sm text-gray-900">
                Required attribute
              </label>
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
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-gray-900 placeholder-gray-500 px-3 py-2"
              placeholder="Describe what this attribute represents"
            />
          </div>

          {/* Allowed Values (only for LIST type) */}
          {form.dataType === 'LIST' && (
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Allowed Values *
              </label>
              <div className="mt-1 space-y-2">
                <div className="flex space-x-2">
                  <input
                    type="text"
                    value={newAllowedValue}
                    onChange={(e) => setNewAllowedValue(e.target.value)}
                    onKeyPress={handleKeyPress}
                    className="flex-1 rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-gray-900 placeholder-gray-500 px-3 py-2"
                    placeholder="Enter a value and press Enter"
                  />
                  <button
                    type="button"
                    onClick={addAllowedValue}
                    className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                  >
                    <PlusIcon className="h-4 w-4" />
                  </button>
                </div>
                
                {form.allowedValues.length > 0 && (
                  <div className="bg-gray-50 rounded-md p-3">
                    <div className="flex flex-wrap gap-2">
                      {form.allowedValues.map((value, index) => (
                        <span
                          key={index}
                          className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800"
                        >
                          {value}
                          <button
                            type="button"
                            onClick={() => removeAllowedValue(index)}
                            className="ml-1 inline-flex items-center justify-center w-4 h-4 rounded-full text-indigo-600 hover:bg-indigo-200 hover:text-indigo-900 focus:outline-none"
                          >
                            <TrashIcon className="h-3 w-3" />
                          </button>
                        </span>
                      ))}
                    </div>
                  </div>
                )}
                
                {errors.allowedValues && (
                  <p className="text-sm text-red-600">{errors.allowedValues}</p>
                )}
              </div>
            </div>
          )}

          {/* Actions */}
          <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
            <button
              type="button"
              onClick={handleCancel}
              className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <div className="animate-spin -ml-1 mr-3 h-5 w-5 border-2 border-white border-t-transparent rounded-full"></div>
                  Creating...
                </>
              ) : (
                <>
                  <CheckIcon className="-ml-1 mr-2 h-5 w-5" />
                  Create Attribute
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}