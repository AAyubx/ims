'use client';

import { useEffect, useState } from 'react';
import { UseFormReturn } from 'react-hook-form';
import { ChevronLeft, MapPin, Globe } from 'lucide-react';
import { LocationFormData } from '@/lib/validations';
import { StoreFormData, Country, Currency } from '@/types/store';

interface LocationStepProps {
  form: UseFormReturn<LocationFormData>;
  onNext: (data: LocationFormData) => void;
  onPrevious: () => void;
  isSubmitting?: boolean;
  defaultValues?: Partial<StoreFormData>;
}

// Common countries for quick selection
const commonCountries: Country[] = [
  { code: 'US', name: 'United States' },
  { code: 'CA', name: 'Canada' },
  { code: 'GB', name: 'United Kingdom' },
  { code: 'DE', name: 'Germany' },
  { code: 'FR', name: 'France' },
  { code: 'AU', name: 'Australia' },
  { code: 'JP', name: 'Japan' },
];

// Common currencies
const commonCurrencies: Currency[] = [
  { code: 'USD', name: 'US Dollar', symbol: '$' },
  { code: 'EUR', name: 'Euro', symbol: '€' },
  { code: 'GBP', name: 'British Pound', symbol: '£' },
  { code: 'CAD', name: 'Canadian Dollar', symbol: 'C$' },
  { code: 'AUD', name: 'Australian Dollar', symbol: 'A$' },
  { code: 'JPY', name: 'Japanese Yen', symbol: '¥' },
];

export default function LocationStep({ 
  form, 
  onNext, 
  onPrevious, 
  isSubmitting = false, 
  defaultValues 
}: LocationStepProps) {
  const { register, handleSubmit, watch, setValue, formState: { errors, isValid }, trigger } = form;
  const [isDetectingLocation, setIsDetectingLocation] = useState(false);

  // Populate form with default values if available and trigger validation
  useEffect(() => {
    if (defaultValues) {
      if (defaultValues.addressLine1) setValue('addressLine1', defaultValues.addressLine1);
      if (defaultValues.addressLine2) setValue('addressLine2', defaultValues.addressLine2);
      if (defaultValues.city) setValue('city', defaultValues.city);
      if (defaultValues.stateProvince) setValue('stateProvince', defaultValues.stateProvince);
      if (defaultValues.postalCode) setValue('postalCode', defaultValues.postalCode);
      if (defaultValues.countryCode) setValue('countryCode', defaultValues.countryCode);
      if (defaultValues.latitude) setValue('latitude', defaultValues.latitude);
      if (defaultValues.longitude) setValue('longitude', defaultValues.longitude);
      if (defaultValues.timezone) setValue('timezone', defaultValues.timezone);
    }
    // Trigger validation on mount
    trigger();
  }, [defaultValues, setValue, trigger]);

  const selectedCountry = watch('countryCode');

  const handleNext = (data: LocationFormData) => {
    // Add primaryCurrencyCode based on country if not set
    const enhancedData = {
      ...data,
      primaryCurrencyCode: getDefaultCurrencyForCountry(data.countryCode),
    };
    onNext(enhancedData as LocationFormData);
  };

  const getDefaultCurrencyForCountry = (countryCode: string): string => {
    const currencyMap: Record<string, string> = {
      'US': 'USD',
      'CA': 'CAD',
      'GB': 'GBP',
      'DE': 'EUR',
      'FR': 'EUR',
      'AU': 'AUD',
      'JP': 'JPY',
    };
    return currencyMap[countryCode] || 'USD';
  };

  const handleDetectLocation = () => {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by this browser.');
      return;
    }

    setIsDetectingLocation(true);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setValue('latitude', position.coords.latitude);
        setValue('longitude', position.coords.longitude);
        setIsDetectingLocation(false);
      },
      (error) => {
        console.error('Error detecting location:', error);
        alert('Unable to detect your location. Please enter coordinates manually.');
        setIsDetectingLocation(false);
      }
    );
  };

  return (
    <div className="">
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-2">Location & Address</h2>
        <p className="text-gray-600">
          Enter the physical address and geographical information for this store.
        </p>
      </div>

      <form onSubmit={handleSubmit(handleNext)} className="space-y-8">
        {/* Address Section */}
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-4">Physical Address</h3>
          
          <div className="space-y-4">
            {/* Address Line 1 */}
            <div>
              <label htmlFor="addressLine1" className="block text-sm font-medium text-gray-700 mb-2">
                Address Line 1 <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="addressLine1"
                {...register('addressLine1')}
                placeholder="e.g., 123 Main Street"
                className={`
                  w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-500
                  ${errors.addressLine1 ? 'border-red-300' : 'border-gray-300'}
                `}
              />
              {errors.addressLine1 && (
                <p className="mt-1 text-sm text-red-600">{errors.addressLine1.message}</p>
              )}
            </div>

            {/* Address Line 2 */}
            <div>
              <label htmlFor="addressLine2" className="block text-sm font-medium text-gray-700 mb-2">
                Address Line 2
              </label>
              <input
                type="text"
                id="addressLine2"
                {...register('addressLine2')}
                placeholder="e.g., Suite 100, Apartment 2B (optional)"
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-500"
              />
            </div>

            {/* City, State, Postal Code */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label htmlFor="city" className="block text-sm font-medium text-gray-700 mb-2">
                  City <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  id="city"
                  {...register('city')}
                  placeholder="e.g., New York"
                  className={`
                    w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-500
                    ${errors.city ? 'border-red-300' : 'border-gray-300'}
                  `}
                />
                {errors.city && (
                  <p className="mt-1 text-sm text-red-600">{errors.city.message}</p>
                )}
              </div>

              <div>
                <label htmlFor="stateProvince" className="block text-sm font-medium text-gray-700 mb-2">
                  State/Province
                </label>
                <input
                  type="text"
                  id="stateProvince"
                  {...register('stateProvince')}
                  placeholder="e.g., NY, California"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-500"
                />
              </div>

              <div>
                <label htmlFor="postalCode" className="block text-sm font-medium text-gray-700 mb-2">
                  Postal Code
                </label>
                <input
                  type="text"
                  id="postalCode"
                  {...register('postalCode')}
                  placeholder="e.g., 10001"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-500"
                />
              </div>
            </div>

            {/* Country */}
            <div>
              <label htmlFor="countryCode" className="block text-sm font-medium text-gray-700 mb-2">
                Country <span className="text-red-500">*</span>
              </label>
              <select
                id="countryCode"
                {...register('countryCode')}
                className={`
                  w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900
                  ${errors.countryCode ? 'border-red-300' : 'border-gray-300'}
                `}
              >
                {commonCountries.map((country) => (
                  <option key={country.code} value={country.code}>
                    {country.name} ({country.code})
                  </option>
                ))}
              </select>
              {errors.countryCode && (
                <p className="mt-1 text-sm text-red-600">{errors.countryCode.message}</p>
              )}
            </div>
          </div>
        </div>

        {/* Geographical Coordinates Section */}
        <div className="pt-6 border-t">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium text-gray-900">GPS Coordinates (Optional)</h3>
            <button
              type="button"
              onClick={handleDetectLocation}
              disabled={isDetectingLocation}
              className="inline-flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 cursor-pointer"
            >
              <MapPin className="h-4 w-4" />
              {isDetectingLocation ? 'Detecting...' : 'Auto-detect Location'}
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="latitude" className="block text-sm font-medium text-gray-700 mb-2">
                Latitude
              </label>
              <input
                type="number"
                step="any"
                id="latitude"
                {...register('latitude', { 
                  setValueAs: (value) => value === '' ? undefined : Number(value)
                })}
                placeholder="e.g., 40.7128"
                className={`
                  w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-500
                  ${errors.latitude ? 'border-red-300' : 'border-gray-300'}
                `}
              />
              {errors.latitude && (
                <p className="mt-1 text-sm text-red-600">{errors.latitude.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="longitude" className="block text-sm font-medium text-gray-700 mb-2">
                Longitude
              </label>
              <input
                type="number"
                step="any"
                id="longitude"
                {...register('longitude', { 
                  setValueAs: (value) => value === '' ? undefined : Number(value)
                })}
                placeholder="e.g., -74.0060"
                className={`
                  w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-500
                  ${errors.longitude ? 'border-red-300' : 'border-gray-300'}
                `}
              />
              {errors.longitude && (
                <p className="mt-1 text-sm text-red-600">{errors.longitude.message}</p>
              )}
            </div>
          </div>

          <p className="mt-2 text-xs text-gray-500">
            GPS coordinates help with geographical searches and delivery planning.
          </p>
        </div>

        {/* Timezone Section */}
        <div className="pt-6 border-t">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Timezone (Optional)</h3>
          
          <div>
            <label htmlFor="timezone" className="block text-sm font-medium text-gray-700 mb-2">
              Timezone
            </label>
            <select
              id="timezone"
              {...register('timezone')}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900"
            >
              <option value="">Select timezone</option>
              <option value="America/New_York">Eastern Time (EST/EDT)</option>
              <option value="America/Chicago">Central Time (CST/CDT)</option>
              <option value="America/Denver">Mountain Time (MST/MDT)</option>
              <option value="America/Los_Angeles">Pacific Time (PST/PDT)</option>
              <option value="Europe/London">London (GMT/BST)</option>
              <option value="Europe/Paris">Central European Time</option>
              <option value="Asia/Tokyo">Japan Standard Time</option>
              <option value="Australia/Sydney">Australian Eastern Time</option>
            </select>
            <p className="mt-1 text-xs text-gray-500">
              Used for business hours and scheduling operations
            </p>
          </div>
        </div>

        {/* Navigation */}
        <div className="flex justify-between pt-6 border-t">
          <button
            type="button"
            onClick={onPrevious}
            className="inline-flex items-center gap-2 px-6 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 cursor-pointer"
          >
            <ChevronLeft className="h-4 w-4" />
            Previous
          </button>

          <button
            type="submit"
            disabled={!isValid || isSubmitting}
            className={`
              inline-flex items-center gap-2 px-6 py-2 rounded-md text-sm font-medium transition-colors
              ${isValid && !isSubmitting
                ? 'bg-blue-600 text-white hover:bg-blue-700 cursor-pointer'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
              }
            `}
          >
            {isSubmitting ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                Creating Store...
              </>
            ) : (
              <>
                <Globe className="h-4 w-4" />
                Create Store
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
}