'use client';

import { useEffect } from 'react';
import { UseFormReturn } from 'react-hook-form';
import { ChevronRight, Building, Warehouse, MapPin } from 'lucide-react';
import { BasicInfoFormData } from '@/lib/validations';
import { LocationType, StoreFormData } from '@/types/store';

interface BasicInfoStepProps {
  form: UseFormReturn<BasicInfoFormData>;
  onNext: (data: BasicInfoFormData) => void;
  defaultValues?: Partial<StoreFormData>;
}

const locationTypeOptions = [
  {
    value: LocationType.STORE,
    label: 'Store',
    description: 'Retail location for customer sales',
    icon: Building
  },
  {
    value: LocationType.WAREHOUSE,
    label: 'Warehouse',
    description: 'Storage facility for inventory management',
    icon: Warehouse
  },
  {
    value: LocationType.DISTRIBUTION_CENTER,
    label: 'Distribution Center',
    description: 'Hub for inventory distribution',
    icon: MapPin
  }
];

export default function BasicInfoStep({ form, onNext, defaultValues }: BasicInfoStepProps) {
  const { register, handleSubmit, watch, setValue, formState: { errors, isValid, dirtyFields }, trigger } = form;

  // Populate form with default values if available and trigger validation
  useEffect(() => {
    if (defaultValues) {
      if (defaultValues.code) setValue('code', defaultValues.code);
      if (defaultValues.name) setValue('name', defaultValues.name);
      if (defaultValues.type) setValue('type', defaultValues.type);
      if (defaultValues.parentLocationId) setValue('parentLocationId', defaultValues.parentLocationId);
      if (defaultValues.storeManagerId) setValue('storeManagerId', defaultValues.storeManagerId);
    }
    // Trigger validation on mount
    trigger();
  }, [defaultValues, setValue, trigger]);

  const selectedType = watch('type');
  const formValues = watch(); // Watch all form values for debugging

  // Debug logging
  console.log('Form validation state:', { isValid, errors, formValues, dirtyFields });

  const handleNext = (data: BasicInfoFormData) => {
    onNext(data);
  };

  return (
    <div className="">
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-2">Basic Information</h2>
        <p className="text-gray-600">
          Enter the basic details for your new store location.
        </p>
      </div>

      <form onSubmit={handleSubmit(handleNext)} className="space-y-8">
        {/* Store Code */}
        <div>
          <label htmlFor="code" className="block text-sm font-medium text-gray-700 mb-2">
            Store Code <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="code"
            {...register('code')}
            placeholder="e.g., STORE-NYC-001"
            className={`
              w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-500
              ${errors.code ? 'border-red-300' : 'border-gray-300'}
            `}
          />
          {errors.code && (
            <p className="mt-1 text-sm text-red-600">{errors.code.message}</p>
          )}
          <p className="mt-1 text-xs text-gray-500">
            Unique identifier for this location (uppercase letters, numbers, hyphens, and underscores only)
          </p>
        </div>

        {/* Store Name */}
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
            Store Name <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="name"
            {...register('name')}
            placeholder="e.g., Manhattan Flagship Store"
            className={`
              w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-500
              ${errors.name ? 'border-red-300' : 'border-gray-300'}
            `}
          />
          {errors.name && (
            <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
          )}
        </div>

        {/* Location Type */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-4">
            Location Type <span className="text-red-500">*</span>
          </label>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {locationTypeOptions.map((option) => {
              const Icon = option.icon;
              const isSelected = selectedType === option.value;
              
              return (
                <label
                  key={option.value}
                  className={`
                    relative cursor-pointer p-4 border rounded-lg transition-all
                    ${isSelected 
                      ? 'border-blue-500 ring-2 ring-blue-200 bg-blue-50' 
                      : 'border-gray-300 hover:border-gray-400'
                    }
                  `}
                >
                  <input
                    type="radio"
                    {...register('type')}
                    value={option.value}
                    className="sr-only"
                  />
                  <div className="flex items-center space-x-3">
                    <Icon className={`h-6 w-6 ${isSelected ? 'text-blue-600' : 'text-gray-600'}`} />
                    <div>
                      <div className={`font-medium ${isSelected ? 'text-blue-900' : 'text-gray-900'}`}>
                        {option.label}
                      </div>
                      <div className={`text-sm ${isSelected ? 'text-blue-700' : 'text-gray-500'}`}>
                        {option.description}
                      </div>
                    </div>
                  </div>
                  {isSelected && (
                    <div className="absolute top-2 right-2">
                      <div className="w-3 h-3 bg-blue-600 rounded-full"></div>
                    </div>
                  )}
                </label>
              );
            })}
          </div>
          {errors.type && (
            <p className="mt-2 text-sm text-red-600">{errors.type.message}</p>
          )}
        </div>

        {/* Optional Fields Section */}
        <div className="pt-6 mt-6 border-t">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Additional Information (Optional)</h3>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Parent Location */}
            <div>
              <label htmlFor="parentLocationId" className="block text-sm font-medium text-gray-700 mb-2">
                Parent Location
              </label>
              <select
                id="parentLocationId"
                {...register('parentLocationId', { 
                  setValueAs: (value) => value === '' ? undefined : Number(value)
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900"
              >
                <option value="">No parent location</option>
                {/* TODO: Load parent locations from API */}
              </select>
              <p className="mt-1 text-xs text-gray-500">
                Select a parent location for hierarchy management
              </p>
            </div>

            {/* Store Manager */}
            <div>
              <label htmlFor="storeManagerId" className="block text-sm font-medium text-gray-700 mb-2">
                Store Manager
              </label>
              <select
                id="storeManagerId"
                {...register('storeManagerId', { 
                  setValueAs: (value) => value === '' ? undefined : Number(value)
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900"
              >
                <option value="">Select a manager</option>
                {/* TODO: Load managers from API */}
              </select>
              <p className="mt-1 text-xs text-gray-500">
                Assign a manager to this location
              </p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <div className="flex justify-end pt-6 border-t">
          <div className="flex flex-col items-end gap-2">
            {/* Debug info */}
            <div className="text-xs text-gray-500">
              Form valid: {isValid ? 'Yes' : 'No'} | Errors: {Object.keys(errors).length}
            </div>
            <button
              type="submit"
              disabled={!isValid}
              className={`
                inline-flex items-center gap-2 px-8 py-3 rounded-lg text-sm font-semibold transition-all duration-200 shadow-sm
                ${isValid
                  ? 'bg-blue-600 text-white hover:bg-blue-700 hover:shadow-md border border-blue-600 cursor-pointer'
                  : 'bg-gray-200 text-gray-400 cursor-not-allowed border border-gray-200'
                }
              `}
            >
              Continue
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}