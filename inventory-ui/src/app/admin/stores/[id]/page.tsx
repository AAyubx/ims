'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { ArrowLeft, MapPin, Building, Globe, Clock, User, Settings } from 'lucide-react';
import { StoreAPI } from '@/lib/storeApi';
import { LocationResponse } from '@/types/store';

export default function StoreDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const storeId = params.id as string;
  
  const [store, setStore] = useState<LocationResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStoreDetails = async () => {
      if (!storeId) return;
      
      try {
        setLoading(true);
        const response = await StoreAPI.getStore(Number(storeId));
        
        if (response.success && response.data) {
          setStore(response.data);
        } else {
          setError(response.message || 'Failed to fetch store details');
        }
      } catch (err) {
        console.error('Error fetching store details:', err);
        setError('Failed to fetch store details');
      } finally {
        setLoading(false);
      }
    };

    fetchStoreDetails();
  }, [storeId]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-2 text-gray-600">Loading store details...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-600 mb-4">
            <Building className="h-12 w-12 mx-auto mb-2" />
            <p className="text-lg font-medium">Error Loading Store</p>
            <p className="text-sm">{error}</p>
          </div>
          <button
            onClick={() => router.back()}
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
          >
            Go Back
          </button>
        </div>
      </div>
    );
  }

  if (!store) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <Building className="h-12 w-12 text-gray-400 mx-auto mb-2" />
          <p className="text-gray-600">Store not found</p>
          <button
            onClick={() => router.back()}
            className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
          >
            Go Back
          </button>
        </div>
      </div>
    );
  }

  const getLocationTypeIcon = (type: string) => {
    switch (type) {
      case 'WAREHOUSE':
        return <Building className="h-5 w-5" />;
      case 'DISTRIBUTION_CENTER':
        return <MapPin className="h-5 w-5" />;
      default:
        return <Building className="h-5 w-5" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'INACTIVE':
        return 'bg-red-100 text-red-800';
      case 'TEMPORARILY_CLOSED':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between py-6">
            <div className="flex items-center space-x-4">
              <button
                onClick={() => router.back()}
                className="inline-flex items-center gap-2 text-gray-600 hover:text-gray-900"
              >
                <ArrowLeft className="h-5 w-5" />
                Back to Stores
              </button>
              <div className="h-6 border-l border-gray-300"></div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">{store.name}</h1>
                <p className="text-sm text-gray-600">{store.code}</p>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(store.status)}`}>
                {store.status}
              </span>
              <button className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 text-sm font-medium">
                <Settings className="h-4 w-4 inline mr-2" />
                Edit Store
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          {/* Main Information */}
          <div className="lg:col-span-2 space-y-6">
            
            {/* Basic Information */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-lg font-medium text-gray-900 mb-4">Basic Information</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-500">Store Code</label>
                  <p className="mt-1 text-sm text-gray-900">{store.code}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-500">Store Name</label>
                  <p className="mt-1 text-sm text-gray-900">{store.name}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-500">Type</label>
                  <div className="mt-1 flex items-center gap-2">
                    {getLocationTypeIcon(store.type)}
                    <span className="text-sm text-gray-900">{store.type.replace('_', ' ')}</span>
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-500">Status</label>
                  <span className={`mt-1 inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(store.status)}`}>
                    {store.status}
                  </span>
                </div>
              </div>
            </div>

            {/* Address Information */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-lg font-medium text-gray-900 mb-4">Address</h2>
              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-gray-500">Street Address</label>
                  <p className="mt-1 text-sm text-gray-900">
                    {store.addressLine1}
                    {store.addressLine2 && <><br />{store.addressLine2}</>}
                  </p>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-500">City</label>
                    <p className="mt-1 text-sm text-gray-900">{store.city}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-500">State/Province</label>
                    <p className="mt-1 text-sm text-gray-900">{store.stateProvince || 'N/A'}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-500">Postal Code</label>
                    <p className="mt-1 text-sm text-gray-900">{store.postalCode || 'N/A'}</p>
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-500">Country</label>
                  <p className="mt-1 text-sm text-gray-900">{store.countryCode}</p>
                </div>
              </div>
            </div>

            {/* GPS Coordinates */}
            {(store.latitude && store.longitude) && (
              <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-lg font-medium text-gray-900 mb-4">GPS Coordinates</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-500">Latitude</label>
                    <p className="mt-1 text-sm text-gray-900">{store.latitude}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-500">Longitude</label>
                    <p className="mt-1 text-sm text-gray-900">{store.longitude}</p>
                  </div>
                </div>
                <div className="mt-4">
                  <a
                    href={`https://maps.google.com?q=${store.latitude},${store.longitude}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 text-sm"
                  >
                    <Globe className="h-4 w-4" />
                    View on Google Maps
                  </a>
                </div>
              </div>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            
            {/* Quick Info */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Quick Info</h3>
              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-gray-500">Created</label>
                  <p className="mt-1 text-sm text-gray-900">
                    {new Date(store.createdAt).toLocaleDateString()}
                  </p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-500">Last Updated</label>
                  <p className="mt-1 text-sm text-gray-900">
                    {new Date(store.updatedAt).toLocaleDateString()}
                  </p>
                </div>
                {store.timezone && (
                  <div>
                    <label className="block text-sm font-medium text-gray-500">Timezone</label>
                    <div className="mt-1 flex items-center gap-2">
                      <Clock className="h-4 w-4 text-gray-400" />
                      <span className="text-sm text-gray-900">{store.timezone}</span>
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Management */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Management</h3>
              <div className="space-y-3">
                {store.storeManagerId && (
                  <div>
                    <label className="block text-sm font-medium text-gray-500">Store Manager</label>
                    <div className="mt-1 flex items-center gap-2">
                      <User className="h-4 w-4 text-gray-400" />
                      <span className="text-sm text-gray-900">Manager ID: {store.storeManagerId}</span>
                    </div>
                  </div>
                )}
                {store.parentLocationId && (
                  <div>
                    <label className="block text-sm font-medium text-gray-500">Parent Location</label>
                    <p className="mt-1 text-sm text-gray-900">ID: {store.parentLocationId}</p>
                  </div>
                )}
                {store.taxJurisdictionId && (
                  <div>
                    <label className="block text-sm font-medium text-gray-500">Tax Jurisdiction</label>
                    <p className="mt-1 text-sm text-gray-900">ID: {store.taxJurisdictionId}</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}