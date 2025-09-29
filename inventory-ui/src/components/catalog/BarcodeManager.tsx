'use client';

import { useState, useEffect } from 'react';
import { 
  QrCodeIcon,
  PlusIcon,
  PencilIcon,
  TrashIcon,
  EyeIcon,
  CheckBadgeIcon,
  ExclamationTriangleIcon,
  EllipsisVerticalIcon
} from '@heroicons/react/24/outline';
import BarcodeGenerator from './BarcodeGenerator';

// Types
interface Barcode {
  id: number;
  barcode: string;
  barcodeType: string;
  barcodeTypeDisplay: string;
  packLevel: string;
  packLevelDisplay: string;
  isPrimary: boolean;
  status: 'RESERVED' | 'ACTIVE' | 'DEPRECATED' | 'BLOCKED';
  statusDisplay: string;
  uomName?: string;
  createdAt: string;
  displayFormat: string;
  isValidFormat: boolean;
  canBePrimary: boolean;
}

interface BarcodeManagerProps {
  variantId: number;
  variantSku: string;
  barcodes: Barcode[];
  onUpdate: () => void;
}

export default function BarcodeManager({ 
  variantId, 
  variantSku, 
  barcodes = [], 
  onUpdate 
}: BarcodeManagerProps) {
  const [showGenerator, setShowGenerator] = useState(false);
  const [loading, setLoading] = useState(false);
  const [selectedBarcodes, setSelectedBarcodes] = useState<number[]>([]);
  const [actionMenuOpen, setActionMenuOpen] = useState<number | null>(null);

  // Status color mapping
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'RESERVED':
        return 'bg-yellow-100 text-yellow-800';
      case 'DEPRECATED':
        return 'bg-gray-100 text-gray-800';
      case 'BLOCKED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  // Pack level color mapping
  const getPackLevelColor = (level: string) => {
    switch (level) {
      case 'EACH':
        return 'bg-blue-100 text-blue-800';
      case 'INNER':
        return 'bg-purple-100 text-purple-800';
      case 'CASE':
        return 'bg-orange-100 text-orange-800';
      case 'PALLET':
        return 'bg-indigo-100 text-indigo-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  // Handle barcode creation
  const handleBarcodeCreated = (newBarcode: Barcode) => {
    setShowGenerator(false);
    onUpdate(); // Refresh the list
  };

  // Handle checkbox selection
  const handleSelectBarcode = (barcodeId: number) => {
    setSelectedBarcodes(prev => 
      prev.includes(barcodeId)
        ? prev.filter(id => id !== barcodeId)
        : [...prev, barcodeId]
    );
  };

  // Handle select all
  const handleSelectAll = () => {
    if (selectedBarcodes.length === barcodes.length) {
      setSelectedBarcodes([]);
    } else {
      setSelectedBarcodes(barcodes.map(b => b.id));
    }
  };

  // Handle set primary
  const handleSetPrimary = async (barcodeId: number, packLevel: string) => {
    setLoading(true);
    try {
      const response = await fetch(`/api/catalog/barcodes/${barcodeId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ isPrimary: true })
      });

      if (response.ok) {
        onUpdate();
      } else {
        console.error('Failed to set primary barcode');
      }
    } catch (error) {
      console.error('Error setting primary barcode:', error);
      // For demo purposes, just update the UI
      onUpdate();
    } finally {
      setLoading(false);
    }
  };

  // Handle status change
  const handleStatusChange = async (barcodeId: number, newStatus: string) => {
    setLoading(true);
    try {
      const response = await fetch(`/api/catalog/barcodes/${barcodeId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: newStatus })
      });

      if (response.ok) {
        onUpdate();
      } else {
        console.error('Failed to update barcode status');
      }
    } catch (error) {
      console.error('Error updating barcode status:', error);
      // For demo purposes, just update the UI
      onUpdate();
    } finally {
      setLoading(false);
    }
  };

  // Handle delete
  const handleDelete = async (barcodeId: number) => {
    if (!confirm('Are you sure you want to delete this barcode?')) {
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(`/api/catalog/barcodes/${barcodeId}`, {
        method: 'DELETE'
      });

      if (response.ok) {
        onUpdate();
      } else {
        console.error('Failed to delete barcode');
      }
    } catch (error) {
      console.error('Error deleting barcode:', error);
      // For demo purposes, just update the UI
      onUpdate();
    } finally {
      setLoading(false);
    }
  };

  // Handle bulk actions
  const handleBulkStatusChange = async (newStatus: string) => {
    if (selectedBarcodes.length === 0) return;

    setLoading(true);
    try {
      // In a real app, you'd have a bulk update endpoint
      const promises = selectedBarcodes.map(id =>
        fetch(`/api/catalog/barcodes/${id}`, {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ status: newStatus })
        })
      );

      await Promise.all(promises);
      onUpdate();
      setSelectedBarcodes([]);
    } catch (error) {
      console.error('Error bulk updating barcodes:', error);
      onUpdate(); // For demo purposes
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white shadow rounded-lg">
      {/* Header */}
      <div className="px-4 py-5 sm:px-6 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <QrCodeIcon className="h-6 w-6 text-gray-400 mr-3" />
            <div>
              <h3 className="text-lg leading-6 font-medium text-gray-900">
                Barcodes
              </h3>
              <p className="mt-1 max-w-2xl text-sm text-gray-500">
                Manage barcodes for variant {variantSku}
              </p>
            </div>
          </div>
          <button
            onClick={() => setShowGenerator(true)}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            <PlusIcon className="h-4 w-4 mr-2" />
            Add Barcode
          </button>
        </div>

        {/* Bulk Actions */}
        {selectedBarcodes.length > 0 && (
          <div className="mt-4 p-3 bg-blue-50 rounded-md">
            <div className="flex items-center justify-between">
              <p className="text-sm text-blue-700">
                {selectedBarcodes.length} barcode(s) selected
              </p>
              <div className="space-x-2">
                <button
                  onClick={() => handleBulkStatusChange('ACTIVE')}
                  disabled={loading}
                  className="text-sm text-blue-600 hover:text-blue-800 disabled:opacity-50"
                >
                  Activate
                </button>
                <button
                  onClick={() => handleBulkStatusChange('DEPRECATED')}
                  disabled={loading}
                  className="text-sm text-blue-600 hover:text-blue-800 disabled:opacity-50"
                >
                  Deprecate
                </button>
                <button
                  onClick={() => setSelectedBarcodes([])}
                  className="text-sm text-gray-600 hover:text-gray-800"
                >
                  Clear
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Table */}
      <div className="overflow-hidden">
        {barcodes.length === 0 ? (
          <div className="text-center py-12">
            <QrCodeIcon className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No barcodes</h3>
            <p className="mt-1 text-sm text-gray-500">
              Get started by creating a new barcode for this variant.
            </p>
            <div className="mt-6">
              <button
                onClick={() => setShowGenerator(true)}
                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                <PlusIcon className="h-4 w-4 mr-2" />
                Add Barcode
              </button>
            </div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th scope="col" className="relative w-12 px-6 sm:w-16 sm:px-8">
                    <input
                      type="checkbox"
                      checked={selectedBarcodes.length === barcodes.length && barcodes.length > 0}
                      onChange={handleSelectAll}
                      className="absolute left-4 top-1/2 -mt-2 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                    />
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Type
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Barcode
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Pack Level
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    UoM
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Primary
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th scope="col" className="relative px-6 py-3">
                    <span className="sr-only">Actions</span>
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {barcodes.map((barcode) => (
                  <tr key={barcode.id} className="hover:bg-gray-50">
                    <td className="relative w-12 px-6 sm:w-16 sm:px-8">
                      <input
                        type="checkbox"
                        checked={selectedBarcodes.includes(barcode.id)}
                        onChange={() => handleSelectBarcode(barcode.id)}
                        className="absolute left-4 top-1/2 -mt-2 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                      />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {barcode.barcodeTypeDisplay}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="text-sm font-mono text-gray-900">
                          {barcode.barcode}
                        </div>
                        {!barcode.isValidFormat && (
                          <ExclamationTriangleIcon className="h-4 w-4 text-yellow-500 ml-2" title="Invalid format" />
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getPackLevelColor(barcode.packLevel)}`}>
                        {barcode.packLevelDisplay}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {barcode.uomName || '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {barcode.isPrimary ? (
                        <div className="flex items-center text-green-600">
                          <CheckBadgeIcon className="h-4 w-4 mr-1" />
                          <span className="text-xs font-medium">Primary</span>
                        </div>
                      ) : (
                        <button
                          onClick={() => handleSetPrimary(barcode.id, barcode.packLevel)}
                          disabled={!barcode.canBePrimary || loading}
                          className="text-xs text-blue-600 hover:text-blue-800 disabled:text-gray-400 disabled:cursor-not-allowed"
                        >
                          Set Primary
                        </button>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(barcode.status)}`}>
                        {barcode.statusDisplay}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="relative">
                        <button
                          onClick={() => setActionMenuOpen(actionMenuOpen === barcode.id ? null : barcode.id)}
                          className="text-gray-400 hover:text-gray-600"
                        >
                          <EllipsisVerticalIcon className="h-5 w-5" />
                        </button>
                        
                        {actionMenuOpen === barcode.id && (
                          <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg z-10 border border-gray-200">
                            <div className="py-1">
                              <button
                                onClick={() => {
                                  // View barcode details
                                  setActionMenuOpen(null);
                                }}
                                className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 w-full text-left"
                              >
                                <EyeIcon className="h-4 w-4 mr-2" />
                                View Details
                              </button>
                              
                              {barcode.status === 'RESERVED' && (
                                <button
                                  onClick={() => {
                                    handleStatusChange(barcode.id, 'ACTIVE');
                                    setActionMenuOpen(null);
                                  }}
                                  className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 w-full text-left"
                                >
                                  <CheckBadgeIcon className="h-4 w-4 mr-2" />
                                  Activate
                                </button>
                              )}
                              
                              {barcode.status === 'ACTIVE' && (
                                <button
                                  onClick={() => {
                                    handleStatusChange(barcode.id, 'DEPRECATED');
                                    setActionMenuOpen(null);
                                  }}
                                  className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 w-full text-left"
                                >
                                  <ExclamationTriangleIcon className="h-4 w-4 mr-2" />
                                  Deprecate
                                </button>
                              )}
                              
                              <hr className="my-1" />
                              
                              <button
                                onClick={() => {
                                  handleDelete(barcode.id);
                                  setActionMenuOpen(null);
                                }}
                                className="flex items-center px-4 py-2 text-sm text-red-700 hover:bg-red-50 w-full text-left"
                              >
                                <TrashIcon className="h-4 w-4 mr-2" />
                                Delete
                              </button>
                            </div>
                          </div>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Summary */}
      {barcodes.length > 0 && (
        <div className="px-6 py-3 border-t border-gray-200 bg-gray-50">
          <div className="flex items-center justify-between text-sm text-gray-500">
            <div>
              Total: {barcodes.length} barcode(s)
            </div>
            <div className="flex space-x-4">
              <span>Active: {barcodes.filter(b => b.status === 'ACTIVE').length}</span>
              <span>Reserved: {barcodes.filter(b => b.status === 'RESERVED').length}</span>
              <span>Deprecated: {barcodes.filter(b => b.status === 'DEPRECATED').length}</span>
            </div>
          </div>
        </div>
      )}

      {/* Barcode Generator Modal */}
      {showGenerator && (
        <BarcodeGenerator
          variantId={variantId}
          variantSku={variantSku}
          onBarcodeCreated={handleBarcodeCreated}
          onClose={() => setShowGenerator(false)}
        />
      )}
    </div>
  );
}