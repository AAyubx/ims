'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { 
  PlusIcon, 
  MagnifyingGlassIcon,
  CogIcon,
  PencilIcon,
  TrashIcon,
  EyeIcon
} from '@heroicons/react/24/outline';

interface AttributeDefinition {
  id: number;
  code: string;
  name: string;
  description?: string;
  dataType: 'TEXT' | 'NUMBER' | 'BOOLEAN' | 'LIST' | 'DATE';
  isRequired: boolean;
  allowedValues?: string;
  createdAt: string;
  updatedAt: string;
}

export default function AttributesPage() {
  const [attributes, setAttributes] = useState<AttributeDefinition[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    // Mock data - in real implementation, this would fetch from API
    setTimeout(() => {
      setAttributes([
        {
          id: 1,
          code: 'COLOR',
          name: 'Color',
          description: 'Product color attribute',
          dataType: 'LIST',
          isRequired: false,
          allowedValues: 'Red,Blue,Green,Yellow,Black,White',
          createdAt: '2024-01-15T10:00:00Z',
          updatedAt: '2024-01-15T10:00:00Z'
        },
        {
          id: 2,
          code: 'SIZE',
          name: 'Size',
          description: 'Product size attribute',
          dataType: 'LIST',
          isRequired: true,
          allowedValues: 'XS,S,M,L,XL,XXL',
          createdAt: '2024-01-15T10:00:00Z',
          updatedAt: '2024-01-15T10:00:00Z'
        },
        {
          id: 3,
          code: 'WEIGHT',
          name: 'Weight (kg)',
          description: 'Product weight in kilograms',
          dataType: 'NUMBER',
          isRequired: false,
          createdAt: '2024-01-15T10:00:00Z',
          updatedAt: '2024-01-15T10:00:00Z'
        },
        {
          id: 4,
          code: 'ORGANIC',
          name: 'Organic',
          description: 'Whether the product is organic',
          dataType: 'BOOLEAN',
          isRequired: false,
          createdAt: '2024-01-15T10:00:00Z',
          updatedAt: '2024-01-15T10:00:00Z'
        }
      ]);
      setLoading(false);
    }, 1000);
  }, []);

  const filteredAttributes = attributes.filter(attr => 
    attr.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    attr.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (attr.description && attr.description.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const getDataTypeColor = (dataType: string) => {
    switch (dataType) {
      case 'TEXT': return 'bg-blue-100 text-blue-800';
      case 'NUMBER': return 'bg-green-100 text-green-800';
      case 'BOOLEAN': return 'bg-purple-100 text-purple-800';
      case 'LIST': return 'bg-orange-100 text-orange-800';
      case 'DATE': return 'bg-pink-100 text-pink-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="border-b border-gray-200 pb-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <CogIcon className="h-8 w-8 text-indigo-600" />
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Attributes</h1>
              <p className="mt-1 text-sm text-gray-600">
                Manage product attributes and their data types
              </p>
            </div>
          </div>
          <Link
            href="/catalog/attributes/new"
            className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700"
          >
            <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
            Add Attribute
          </Link>
        </div>
      </div>

      {/* Search */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="relative max-w-md">
          <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
          <input
            type="text"
            placeholder="Search attributes..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-indigo-500 focus:border-indigo-500"
          />
        </div>
      </div>

      {/* Attributes List */}
      <div className="bg-white shadow rounded-lg overflow-hidden">
        {loading ? (
          <div className="p-6">
            <div className="animate-pulse space-y-4">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="flex items-center space-x-4">
                  <div className="bg-gray-300 h-8 w-8 rounded"></div>
                  <div className="flex-1 space-y-2">
                    <div className="bg-gray-300 h-4 w-32 rounded"></div>
                    <div className="bg-gray-300 h-3 w-48 rounded"></div>
                  </div>
                  <div className="bg-gray-300 h-6 w-16 rounded"></div>
                </div>
              ))}
            </div>
          </div>
        ) : filteredAttributes.length === 0 ? (
          <div className="p-6 text-center">
            <CogIcon className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No attributes found</h3>
            <p className="mt-1 text-sm text-gray-500">
              {searchTerm
                ? 'Try adjusting your search criteria.'
                : 'Get started by creating a new attribute.'}
            </p>
            {!searchTerm && (
              <div className="mt-6">
                <Link
                  href="/catalog/attributes/new"
                  className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700"
                >
                  <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
                  Add Attribute
                </Link>
              </div>
            )}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Attribute
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Data Type
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Required
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Allowed Values
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Updated
                  </th>
                  <th className="relative px-6 py-3">
                    <span className="sr-only">Actions</span>
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredAttributes.map((attribute) => (
                  <tr key={attribute.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-8 w-8">
                          <div className="h-8 w-8 rounded bg-indigo-100 flex items-center justify-center">
                            <CogIcon className="h-5 w-5 text-indigo-600" />
                          </div>
                        </div>
                        <div className="ml-3">
                          <div className="text-sm font-medium text-gray-900">
                            {attribute.name}
                          </div>
                          <div className="text-sm text-gray-500">
                            Code: {attribute.code}
                          </div>
                          {attribute.description && (
                            <div className="text-xs text-gray-400 max-w-xs truncate">
                              {attribute.description}
                            </div>
                          )}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getDataTypeColor(attribute.dataType)}`}>
                        {attribute.dataType}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        attribute.isRequired 
                          ? 'bg-red-100 text-red-800' 
                          : 'bg-gray-100 text-gray-800'
                      }`}>
                        {attribute.isRequired ? 'Required' : 'Optional'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 max-w-xs truncate">
                      {attribute.allowedValues || '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(attribute.updatedAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center space-x-2">
                        <Link
                          href={`/catalog/attributes/${attribute.id}`}
                          className="text-indigo-600 hover:text-indigo-900"
                          title="View"
                        >
                          <EyeIcon className="h-4 w-4" />
                        </Link>
                        <Link
                          href={`/catalog/attributes/${attribute.id}/edit`}
                          className="text-gray-600 hover:text-gray-900"
                          title="Edit"
                        >
                          <PencilIcon className="h-4 w-4" />
                        </Link>
                        <button
                          className="text-red-600 hover:text-red-900"
                          title="Delete"
                        >
                          <TrashIcon className="h-4 w-4" />
                        </button>
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
      {!loading && filteredAttributes.length > 0 && (
        <div className="bg-gray-50 px-6 py-3 text-sm text-gray-600">
          Showing {filteredAttributes.length} attributes
        </div>
      )}
    </div>
  );
}