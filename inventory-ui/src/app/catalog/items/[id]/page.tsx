'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { 
  ArrowLeftIcon,
  PencilIcon,
  CubeIcon,
  QrCodeIcon,
  EyeIcon,
  TagIcon,
  CurrencyDollarIcon,
  ArchiveBoxIcon
} from '@heroicons/react/24/outline';
import BarcodeManager from '../../../../components/catalog/BarcodeManager';

interface Item {
  id: number;
  sku: string;
  name: string;
  shortName?: string;
  description?: string;
  department: string;
  category: string;
  brand: string;
  status: 'DRAFT' | 'ACTIVE' | 'DISCONTINUED';
  itemType: string;
  basePrice?: number;
  stockLevel?: number;
  variants: ItemVariant[];
  createdAt: string;
  updatedAt: string;
}

interface ItemVariant {
  id: number;
  variantSku: string;
  name: string;
  attributes: Record<string, any>;
  price?: number;
  stockLevel?: number;
  isDefault: boolean;
  status: string;
  barcodes: any[];
}

export default function ItemDetailPage() {
  const params = useParams();
  const itemId = params.id;
  
  const [item, setItem] = useState<Item | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'details' | 'variants' | 'barcodes'>('details');
  const [selectedVariant, setSelectedVariant] = useState<ItemVariant | null>(null);
  const [showBarcodeManager, setShowBarcodeManager] = useState(false);

  useEffect(() => {
    // Mock data - in real implementation, this would fetch from API
    setTimeout(() => {
      const mockItem: Item = {
        id: Number(itemId),
        sku: 'ELEC-001',
        name: 'iPhone 15 Pro',
        shortName: 'iPhone 15 Pro',
        description: 'Latest iPhone with Pro features including titanium design, advanced camera system, and A17 Pro chip',
        department: 'Electronics',
        category: 'Mobile Phones',
        brand: 'Apple',
        status: 'ACTIVE',
        itemType: 'VARIABLE',
        basePrice: 999.99,
        stockLevel: 45,
        variants: [
          {
            id: 1,
            variantSku: 'ELEC-001-128GB-BLK',
            name: '128GB Black',
            attributes: { color: 'Black', storage: '128GB' },
            price: 999.99,
            stockLevel: 20,
            isDefault: true,
            status: 'ACTIVE',
            barcodes: [
              {
                id: 1,
                barcode: '1234567890123',
                barcodeType: 'EAN_13',
                barcodeTypeDisplay: 'EAN-13',
                packLevel: 'EACH',
                packLevelDisplay: 'Each',
                isPrimary: true,
                status: 'ACTIVE',
                statusDisplay: 'Active',
                displayFormat: 'EAN-13 (1234567890123)',
                isValidFormat: true,
                canBePrimary: true,
                createdAt: '2024-01-15T10:00:00Z'
              }
            ]
          },
          {
            id: 2,
            variantSku: 'ELEC-001-256GB-BLK',
            name: '256GB Black',
            attributes: { color: 'Black', storage: '256GB' },
            price: 1099.99,
            stockLevel: 15,
            isDefault: false,
            status: 'ACTIVE',
            barcodes: []
          },
          {
            id: 3,
            variantSku: 'ELEC-001-128GB-WHT',
            name: '128GB White',
            attributes: { color: 'White', storage: '128GB' },
            price: 999.99,
            stockLevel: 10,
            isDefault: false,
            status: 'ACTIVE',
            barcodes: []
          }
        ],
        createdAt: '2024-01-15T10:00:00Z',
        updatedAt: '2024-01-15T10:00:00Z'
      };
      setItem(mockItem);
      setSelectedVariant(mockItem.variants[0]); // Select first variant by default
      setLoading(false);
    }, 500);
  }, [itemId]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'DRAFT':
        return 'bg-yellow-100 text-yellow-800';
      case 'DISCONTINUED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const handleBarcodeUpdate = () => {
    // In real app, this would refetch the item data
    // For demo purposes, we'll just simulate an update
    console.log('Barcode updated, refreshing data...');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!item) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900">Item not found</h2>
          <p className="mt-2 text-gray-600">The item you're looking for doesn't exist.</p>
          <Link 
            href="/catalog/items"
            className="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
          >
            Back to Items
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow">
        <div className="px-4 py-6 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <Link 
                href="/catalog/items"
                className="mr-4 text-gray-400 hover:text-gray-600"
              >
                <ArrowLeftIcon className="h-6 w-6" />
              </Link>
              <div className="flex items-center">
                <CubeIcon className="h-8 w-8 text-blue-600 mr-3" />
                <div>
                  <h1 className="text-2xl font-bold text-gray-900">{item.name}</h1>
                  <p className="text-sm text-gray-500">SKU: {item.sku}</p>
                </div>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <span className={`inline-flex px-3 py-1 text-sm font-semibold rounded-full ${getStatusColor(item.status)}`}>
                {item.status}
              </span>
              <button className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
                <PencilIcon className="h-4 w-4 mr-2" />
                Edit
              </button>
            </div>
          </div>
          
          {/* Tab Navigation */}
          <div className="mt-6">
            <nav className="flex space-x-8">
              <button
                onClick={() => setActiveTab('details')}
                className={`py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'details'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Details
              </button>
              <button
                onClick={() => setActiveTab('variants')}
                className={`py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'variants'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Variants ({item.variants.length})
              </button>
              <button
                onClick={() => setActiveTab('barcodes')}
                className={`py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'barcodes'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <QrCodeIcon className="h-4 w-4 mr-1 inline" />
                Barcodes
              </button>
            </nav>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="px-4 py-6 sm:px-6 lg:px-8">
        {activeTab === 'details' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Main Info */}
            <div className="lg:col-span-2">
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Item Information</h3>
                <dl className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Name</dt>
                    <dd className="mt-1 text-sm text-gray-900">{item.name}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Short Name</dt>
                    <dd className="mt-1 text-sm text-gray-900">{item.shortName || '-'}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">SKU</dt>
                    <dd className="mt-1 text-sm text-gray-900 font-mono">{item.sku}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Type</dt>
                    <dd className="mt-1 text-sm text-gray-900">{item.itemType}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Department</dt>
                    <dd className="mt-1 text-sm text-gray-900">{item.department}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Category</dt>
                    <dd className="mt-1 text-sm text-gray-900">{item.category}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Brand</dt>
                    <dd className="mt-1 text-sm text-gray-900">{item.brand}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Base Price</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {item.basePrice ? `$${item.basePrice.toFixed(2)}` : '-'}
                    </dd>
                  </div>
                  <div className="sm:col-span-2">
                    <dt className="text-sm font-medium text-gray-500">Description</dt>
                    <dd className="mt-1 text-sm text-gray-900">{item.description || '-'}</dd>
                  </div>
                </dl>
              </div>
            </div>

            {/* Quick Actions */}
            <div>
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Quick Actions</h3>
                <div className="space-y-3">
                  <button 
                    onClick={() => setActiveTab('barcodes')}
                    className="w-full flex items-center px-4 py-3 text-sm font-medium text-gray-700 bg-gray-50 rounded-md hover:bg-gray-100 transition-colors"
                  >
                    <QrCodeIcon className="h-5 w-5 mr-3 text-blue-600" />
                    Manage Barcodes
                  </button>
                  <button className="w-full flex items-center px-4 py-3 text-sm font-medium text-gray-700 bg-gray-50 rounded-md hover:bg-gray-100 transition-colors">
                    <TagIcon className="h-5 w-5 mr-3 text-green-600" />
                    Update Pricing
                  </button>
                  <button className="w-full flex items-center px-4 py-3 text-sm font-medium text-gray-700 bg-gray-50 rounded-md hover:bg-gray-100 transition-colors">
                    <ArchiveBoxIcon className="h-5 w-5 mr-3 text-purple-600" />
                    Check Inventory
                  </button>
                  <button className="w-full flex items-center px-4 py-3 text-sm font-medium text-gray-700 bg-gray-50 rounded-md hover:bg-gray-100 transition-colors">
                    <EyeIcon className="h-5 w-5 mr-3 text-indigo-600" />
                    View Reports
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'variants' && (
          <div className="bg-white shadow rounded-lg">
            <div className="px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">Item Variants</h3>
              <p className="mt-1 text-sm text-gray-500">
                Manage different variations of this item
              </p>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Variant
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Attributes
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Price
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Stock
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Barcodes
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {item.variants.map((variant) => (
                    <tr key={variant.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div>
                          <div className="text-sm font-medium text-gray-900">
                            {variant.name}
                            {variant.isDefault && (
                              <span className="ml-2 inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                                Default
                              </span>
                            )}
                          </div>
                          <div className="text-sm text-gray-500 font-mono">{variant.variantSku}</div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {Object.entries(variant.attributes).map(([key, value]) => (
                            <span key={key} className="inline-flex px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800 mr-1">
                              {key}: {value}
                            </span>
                          ))}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {variant.price ? `$${variant.price.toFixed(2)}` : '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {variant.stockLevel || 0}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <span className="text-sm text-gray-900">{variant.barcodes.length}</span>
                          <button
                            onClick={() => {
                              setSelectedVariant(variant);
                              setActiveTab('barcodes');
                            }}
                            className="ml-2 text-blue-600 hover:text-blue-800 text-xs"
                          >
                            Manage
                          </button>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <button className="text-blue-600 hover:text-blue-900 mr-3">Edit</button>
                        <button
                          onClick={() => {
                            setSelectedVariant(variant);
                            setActiveTab('barcodes');
                          }}
                          className="text-green-600 hover:text-green-900"
                        >
                          Barcodes
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {activeTab === 'barcodes' && selectedVariant && (
          <div>
            {/* Variant Selection */}
            <div className="mb-6 bg-white shadow rounded-lg p-4">
              <h3 className="text-lg font-medium text-gray-900 mb-3">Select Variant for Barcode Management</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                {item.variants.map((variant) => (
                  <button
                    key={variant.id}
                    onClick={() => setSelectedVariant(variant)}
                    className={`p-3 text-left rounded-md border-2 transition-all ${
                      selectedVariant.id === variant.id
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <div className="font-medium text-gray-900">{variant.name}</div>
                    <div className="text-sm text-gray-500 font-mono">{variant.variantSku}</div>
                    <div className="text-xs text-gray-400 mt-1">
                      {variant.barcodes.length} barcode(s)
                    </div>
                  </button>
                ))}
              </div>
            </div>

            {/* Barcode Manager */}
            <BarcodeManager
              variantId={selectedVariant.id}
              variantSku={selectedVariant.variantSku}
              barcodes={selectedVariant.barcodes}
              onUpdate={handleBarcodeUpdate}
            />
          </div>
        )}
      </div>
    </div>
  );
}