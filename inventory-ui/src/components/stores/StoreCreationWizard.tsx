'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ChevronLeft, ChevronRight, Check } from 'lucide-react';
import { 
  basicInfoSchema, 
  locationSchema, 
  CompleteStoreFormData,
  BasicInfoFormData,
  LocationFormData 
} from '@/lib/validations';
import { StoreFormData, LocationType } from '@/types/store';
import BasicInfoStep from './BasicInfoStep';
import LocationStep from './LocationStep';

interface StoreCreationWizardProps {
  onSubmit: (data: StoreFormData) => Promise<void>;
  isSubmitting?: boolean;
}

const steps = [
  {
    id: 1,
    name: 'Basic Information',
    description: 'Store name, code, and type'
  },
  {
    id: 2,
    name: 'Location & Address',
    description: 'Physical address and geographical data'
  }
];

export default function StoreCreationWizard({ onSubmit, isSubmitting = false }: StoreCreationWizardProps) {
  const [currentStep, setCurrentStep] = useState(1);
  const [formData, setFormData] = useState<Partial<StoreFormData>>({});

  // Form for Basic Info step
  const basicInfoForm = useForm<BasicInfoFormData>({
    resolver: zodResolver(basicInfoSchema),
    mode: 'all',
    defaultValues: {
      code: '',
      name: '',
      type: LocationType.STORE,
    }
  });

  // Form for Location step
  const locationForm = useForm<LocationFormData>({
    resolver: zodResolver(locationSchema),
    mode: 'all',
    defaultValues: {
      addressLine1: '',
      city: '',
      countryCode: 'US',
    }
  });

  const handleBasicInfoNext = async (data: BasicInfoFormData) => {
    setFormData(prev => ({ ...prev, ...data }));
    setCurrentStep(2);
  };

  const handleLocationNext = async (data: LocationFormData) => {
    const finalData: StoreFormData = {
      ...formData,
      ...data,
    } as StoreFormData;

    await onSubmit(finalData);
  };

  const handlePrevious = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const isStepCompleted = (stepId: number) => {
    if (stepId === 1) {
      return Object.keys(formData).length > 0 && formData.code && formData.name;
    }
    return false;
  };

  return (
    <div className="max-w-5xl mx-auto px-6 py-8">
      {/* Step Indicator */}
      <div className="mb-8 mt-6">
        <div className="flex items-center justify-between">
          {steps.map((step, index) => (
            <div key={step.id} className="flex items-center">
              <div className="flex items-center">
                <div
                  className={`
                    w-12 h-12 rounded-full flex items-center justify-center text-base font-semibold
                    ${currentStep >= step.id
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-200 text-gray-600'
                    }
                    ${isStepCompleted(step.id) && currentStep > step.id
                      ? 'bg-green-600 text-white'
                      : ''
                    }
                  `}
                >
                  {isStepCompleted(step.id) && currentStep > step.id ? (
                    <Check className="h-6 w-6" />
                  ) : (
                    step.id
                  )}
                </div>
                <div className="ml-4">
                  <p className={`text-base font-medium ${
                    currentStep >= step.id ? 'text-blue-600' : 'text-gray-500'
                  }`}>
                    {step.name}
                  </p>
                  <p className="text-sm text-gray-500">{step.description}</p>
                </div>
              </div>
              {index < steps.length - 1 && (
                <div className={`
                  w-16 h-0.5 mx-4
                  ${currentStep > step.id ? 'bg-blue-600' : 'bg-gray-200'}
                `} />
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Step Content */}
      <div className="bg-white rounded-lg shadow-sm border min-h-[600px] p-6">
        {currentStep === 1 && (
          <BasicInfoStep
            form={basicInfoForm}
            onNext={handleBasicInfoNext}
            defaultValues={formData}
          />
        )}

        {currentStep === 2 && (
          <LocationStep
            form={locationForm}
            onNext={handleLocationNext}
            onPrevious={handlePrevious}
            isSubmitting={isSubmitting}
            defaultValues={formData}
          />
        )}
      </div>
    </div>
  );
}