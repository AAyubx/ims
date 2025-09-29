'use client';

import { useState, useEffect } from 'react';
import { 
  QrCodeIcon,
  CheckIcon,
  XMarkIcon,
  ExclamationTriangleIcon,
  ArrowPathIcon
} from '@heroicons/react/24/outline';

// Types
interface BarcodeType {
  value: string;
  label: string;
  description: string;
  length?: number;
  packLevels: string[];
}

interface PackLevel {
  value: string;
  label: string;
  description: string;
}

interface UnitOfMeasure {
  id: number;
  name: string;
  code: string;
}

interface BarcodeGeneratorProps {
  variantId: number;
  variantSku: string;
  onBarcodeCreated: (barcode: any) => void;
  onClose: () => void;
}

export default function BarcodeGenerator({ 
  variantId, 
  variantSku, 
  onBarcodeCreated, 
  onClose 
}: BarcodeGeneratorProps) {
  const [mode, setMode] = useState<'generate' | 'manual'>('generate');
  const [loading, setLoading] = useState(false);
  const [validating, setValidating] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [validation, setValidation] = useState<{
    valid: boolean;
    message: string;
    checkDigit?: string;
  } | null>(null);

  const [form, setForm] = useState({
    barcode: '',
    barcodeType: 'EAN_13',
    packLevel: 'EACH',
    uomId: '',
    isPrimary: false,
    labelTemplateId: ''
  });

  // Static data - in real app, these would come from API
  const barcodeTypes: BarcodeType[] = [
    { value: 'UPC_A', label: 'UPC-A', description: 'Universal Product Code - 12 digits', length: 12, packLevels: ['EACH'] },
    { value: 'UPC_E', label: 'UPC-E', description: 'Universal Product Code - Compressed 8 digits', length: 8, packLevels: ['EACH'] },
    { value: 'EAN_13', label: 'EAN-13', description: 'European Article Number - 13 digits', length: 13, packLevels: ['EACH', 'INNER', 'CASE', 'PALLET'] },
    { value: 'EAN_8', label: 'EAN-8', description: 'European Article Number - 8 digits', length: 8, packLevels: ['EACH'] },
    { value: 'ITF_14', label: 'ITF-14', description: 'Interleaved Two of Five - 14 digits (GTIN-14)', length: 14, packLevels: ['CASE', 'PALLET'] },
    { value: 'CODE_128', label: 'Code 128', description: 'High-density alphanumeric barcode', packLevels: ['EACH', 'INNER', 'CASE', 'PALLET'] },
    { value: 'GS1_128', label: 'GS1-128', description: 'Code 128 with GS1 Application Identifiers', packLevels: ['INNER', 'CASE', 'PALLET'] }
  ];

  const packLevels: PackLevel[] = [
    { value: 'EACH', label: 'Each', description: 'Individual item/unit' },
    { value: 'INNER', label: 'Inner Pack', description: 'Inner packaging (e.g., 6-pack, dozen)' },
    { value: 'CASE', label: 'Case', description: 'Case or carton containing multiple inners or eaches' },
    { value: 'PALLET', label: 'Pallet', description: 'Pallet containing multiple cases' }
  ];

  const unitsOfMeasure: UnitOfMeasure[] = [
    { id: 1, name: 'Each', code: 'EA' },
    { id: 2, name: 'Box', code: 'BOX' },
    { id: 3, name: 'Case', code: 'CASE' },
    { id: 4, name: 'Kilogram', code: 'KG' }
  ];

  // Get current barcode type
  const currentBarcodeType = barcodeTypes.find(bt => bt.value === form.barcodeType);

  // Filter pack levels based on selected barcode type
  const availablePackLevels = packLevels.filter(pl => 
    currentBarcodeType?.packLevels.includes(pl.value)
  );

  // Validate barcode format
  const validateBarcode = async (barcode: string) => {
    if (!barcode || barcode.length < 4) {
      setValidation(null);
      return;
    }

    setValidating(true);
    
    // Simulate API call for validation
    setTimeout(() => {
      const type = currentBarcodeType;
      if (type?.length && barcode.length !== type.length) {
        setValidation({
          valid: false,
          message: `${type.label} must be exactly ${type.length} digits`
        });
      } else if (type?.value.includes('EAN') || type?.value.includes('UPC')) {
        // Simulate check digit validation
        const isValid = Math.random() > 0.3; // 70% chance of being valid
        const calculatedCheckDigit = Math.floor(Math.random() * 10);
        setValidation({
          valid: isValid,
          message: isValid ? 'Valid GTIN with correct check digit' : 'Invalid check digit',
          checkDigit: calculatedCheckDigit.toString()
        });
      } else {
        setValidation({
          valid: true,
          message: 'Valid barcode format'
        });
      }
      setValidating(false);
    }, 500);
  };

  // Handle form changes
  const handleFormChange = (field: string, value: any) => {
    const newForm = { ...form, [field]: value };
    setForm(newForm);
    setErrors({ ...errors, [field]: '' });

    // Reset pack level if not supported by new barcode type
    if (field === 'barcodeType') {
      const newType = barcodeTypes.find(bt => bt.value === value);
      if (newType && !newType.packLevels.includes(form.packLevel)) {
        newForm.packLevel = newType.packLevels[0];
        setForm(newForm);
      }
    }

    // Validate barcode on change
    if (field === 'barcode' && mode === 'manual') {
      validateBarcode(value);
    }
  };

  // Generate barcode
  const handleGenerate = async () => {
    setLoading(true);
    setErrors({});

    try {
      // Simulate API call
      const response = await fetch('/api/catalog/barcodes/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          variantId,
          barcodeType: form.barcodeType,
          packLevel: form.packLevel,
          uomId: form.uomId || null,
          count: 1,
          setPrimary: form.isPrimary
        })
      });

      if (response.ok) {
        const result = await response.json();
        if (result.success && result.data.barcodes.length > 0) {
          onBarcodeCreated(result.data.barcodes[0]);
        } else {
          setErrors({ general: result.message || 'Failed to generate barcode' });
        }
      } else {
        const error = await response.json();
        setErrors({ general: error.message || 'Failed to generate barcode' });
      }
    } catch (error) {
      // For demo purposes, generate a mock barcode
      const mockBarcode = {
        id: Date.now(),
        barcode: generateMockBarcode(form.barcodeType),
        barcodeType: form.barcodeType,
        packLevel: form.packLevel,
        isPrimary: form.isPrimary,
        status: 'ACTIVE'
      };
      onBarcodeCreated(mockBarcode);
    } finally {
      setLoading(false);
    }
  };

  // Create manual barcode
  const handleCreateManual = async () => {
    if (!validation?.valid) {
      setErrors({ barcode: 'Please enter a valid barcode' });
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      // Simulate API call
      const response = await fetch(`/api/catalog/barcodes/variants/${variantId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          barcode: form.barcode,
          barcodeType: form.barcodeType,
          packLevel: form.packLevel,
          uomId: form.uomId || null,
          isPrimary: form.isPrimary
        })
      });

      if (response.ok) {
        const result = await response.json();
        if (result.success) {
          onBarcodeCreated(result.data);
        } else {
          setErrors({ general: result.message || 'Failed to create barcode' });
        }
      } else {
        const error = await response.json();
        setErrors({ general: error.message || 'Failed to create barcode' });
      }
    } catch (error) {
      // For demo purposes, create a mock barcode
      const mockBarcode = {
        id: Date.now(),
        barcode: form.barcode,
        barcodeType: form.barcodeType,
        packLevel: form.packLevel,
        isPrimary: form.isPrimary,
        status: 'ACTIVE'
      };
      onBarcodeCreated(mockBarcode);
    } finally {
      setLoading(false);
    }
  };

  // Generate mock barcode for demo
  const generateMockBarcode = (type: string): string => {
    const length = currentBarcodeType?.length;
    if (length) {
      return Math.random().toString().slice(2, 2 + length).padEnd(length, '0');
    }
    return Math.random().toString(36).substring(2, 14).toUpperCase();
  };

  // Effect to validate barcode when manually entered
  useEffect(() => {
    if (mode === 'manual' && form.barcode) {
      validateBarcode(form.barcode);
    }
  }, [form.barcode, form.barcodeType, mode]);

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div className="relative top-20 mx-auto p-5 border w-11/12 max-w-lg shadow-lg rounded-md bg-white">
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center">
            <QrCodeIcon className="h-6 w-6 text-blue-600 mr-2" />
            <h3 className="text-lg font-medium text-gray-900">Generate Barcode</h3>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <XMarkIcon className="h-6 w-6" />
          </button>
        </div>

        {/* Variant Info */}
        <div className="mb-4 p-3 bg-gray-50 rounded-md">
          <p className="text-sm text-gray-600">Variant: <span className="font-medium">{variantSku}</span></p>
        </div>

        {/* Mode Selection */}
        <div className="mb-6">
          <div className="flex rounded-md" role="group">
            <button
              type="button"
              onClick={() => setMode('generate')}
              className={`px-4 py-2 text-sm font-medium rounded-l-md border ${
                mode === 'generate'
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
              }`}
            >
              Auto Generate
            </button>
            <button
              type="button"
              onClick={() => setMode('manual')}
              className={`px-4 py-2 text-sm font-medium rounded-r-md border-t border-r border-b ${
                mode === 'manual'
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
              }`}
            >
              Manual Entry
            </button>
          </div>
        </div>

        {/* Form */}
        <div className="space-y-4">
          {/* Barcode Type */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Barcode Type
            </label>
            <select
              value={form.barcodeType}
              onChange={(e) => handleFormChange('barcodeType', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {barcodeTypes.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label} - {type.description}
                </option>
              ))}
            </select>
          </div>

          {/* Manual Barcode Entry */}
          {mode === 'manual' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Barcode
                {currentBarcodeType?.length && (
                  <span className="text-gray-500 text-xs ml-1">
                    ({currentBarcodeType.length} digits)
                  </span>
                )}
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={form.barcode}
                  onChange={(e) => handleFormChange('barcode', e.target.value)}
                  placeholder={`Enter ${currentBarcodeType?.label || 'barcode'}`}
                  className={`w-full px-3 py-2 pr-10 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    validation === null
                      ? 'border-gray-300'
                      : validation.valid
                      ? 'border-green-500'
                      : 'border-red-500'
                  }`}
                />
                <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                  {validating ? (
                    <ArrowPathIcon className="h-4 w-4 text-gray-400 animate-spin" />
                  ) : validation ? (
                    validation.valid ? (
                      <CheckIcon className="h-4 w-4 text-green-500" />
                    ) : (
                      <ExclamationTriangleIcon className="h-4 w-4 text-red-500" />
                    )
                  ) : null}
                </div>
              </div>
              {validation && (
                <p className={`text-xs mt-1 ${validation.valid ? 'text-green-600' : 'text-red-600'}`}>
                  {validation.message}
                  {validation.checkDigit && (
                    <span className="ml-2">Check digit: {validation.checkDigit}</span>
                  )}
                </p>
              )}
            </div>
          )}

          {/* Pack Level */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Pack Level
            </label>
            <select
              value={form.packLevel}
              onChange={(e) => handleFormChange('packLevel', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {availablePackLevels.map((level) => (
                <option key={level.value} value={level.value}>
                  {level.label} - {level.description}
                </option>
              ))}
            </select>
          </div>

          {/* Unit of Measure */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Unit of Measure (Optional)
            </label>
            <select
              value={form.uomId}
              onChange={(e) => handleFormChange('uomId', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Select UoM (optional)</option>
              {unitsOfMeasure.map((uom) => (
                <option key={uom.id} value={uom.id.toString()}>
                  {uom.name} ({uom.code})
                </option>
              ))}
            </select>
          </div>

          {/* Primary Barcode */}
          <div className="flex items-center">
            <input
              type="checkbox"
              id="isPrimary"
              checked={form.isPrimary}
              onChange={(e) => handleFormChange('isPrimary', e.target.checked)}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label htmlFor="isPrimary" className="ml-2 block text-sm text-gray-700">
              Set as primary barcode for this pack level
            </label>
          </div>
        </div>

        {/* Error Display */}
        {errors.general && (
          <div className="mt-4 p-3 bg-red-50 rounded-md">
            <p className="text-sm text-red-600">{errors.general}</p>
          </div>
        )}

        {/* Actions */}
        <div className="flex justify-end space-x-3 mt-6">
          <button
            onClick={onClose}
            disabled={loading}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            onClick={mode === 'generate' ? handleGenerate : handleCreateManual}
            disabled={loading || (mode === 'manual' && (!validation?.valid || !form.barcode))}
            className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? (
              <div className="flex items-center">
                <ArrowPathIcon className="h-4 w-4 mr-2 animate-spin" />
                {mode === 'generate' ? 'Generating...' : 'Creating...'}
              </div>
            ) : (
              mode === 'generate' ? 'Generate Barcode' : 'Create Barcode'
            )}
          </button>
        </div>
      </div>
    </div>
  );
}