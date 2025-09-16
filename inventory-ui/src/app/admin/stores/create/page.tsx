'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'react-hot-toast';
import StoreCreationWizard from '@/components/stores/StoreCreationWizard';
import { StoreFormData } from '@/types/store';
import { StoreAPI, getCurrentTenantId } from '@/lib/storeApi';
import { ArrowLeft } from 'lucide-react';

export default function CreateStorePage() {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleStoreCreate = async (formData: StoreFormData) => {
    setIsSubmitting(true);
    try {
      const createRequest = {
        tenantId: getCurrentTenantId(),
        code: formData.code,
        name: formData.name,
        type: formData.type,
        addressLine1: formData.addressLine1,
        addressLine2: formData.addressLine2,
        city: formData.city,
        stateProvince: formData.stateProvince,
        postalCode: formData.postalCode,
        countryCode: formData.countryCode,
        latitude: formData.latitude,
        longitude: formData.longitude,
        timezone: formData.timezone,
        parentLocationId: formData.parentLocationId,
        storeManagerId: formData.storeManagerId,
        taxJurisdictionId: formData.taxJurisdictionId,
        primaryCurrencyCode: formData.primaryCurrencyCode,
        businessHoursJson: formData.businessHours ? JSON.stringify(formData.businessHours) : undefined,
        capabilitiesJson: formData.capabilities ? JSON.stringify(formData.capabilities) : undefined,
      };

      const response = await StoreAPI.createStore(createRequest);

      if (response.success && response.data) {
        toast.success('Store created successfully!');
        router.push('/admin/stores');
      } else {
        toast.error(response.message || 'Failed to create store');
      }
    } catch (error) {
      console.error('Store creation error:', error);
      toast.error('An unexpected error occurred');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="py-6">
      <div className="px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-6">
          <div className="flex items-center gap-4 mb-4">
            <button
              onClick={() => router.back()}
              className="p-2 hover:bg-gray-100 rounded-md transition-colors"
            >
              <ArrowLeft className="h-5 w-5" />
            </button>
            <h1 className="text-2xl font-bold text-gray-900">Create New Store</h1>
          </div>
          <p className="text-gray-600">
            Add a new store location to your inventory management system. Complete all required fields to create the store.
          </p>
        </div>

        {/* Wizard */}
        <div className="bg-white shadow-sm border rounded-lg">
          <StoreCreationWizard 
            onSubmit={handleStoreCreate}
            isSubmitting={isSubmitting}
          />
        </div>
      </div>
    </div>
  );
}