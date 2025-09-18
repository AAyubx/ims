'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Plus, MapPin, Building, Warehouse, Eye } from 'lucide-react';
import { StoreAPI, getCurrentTenantId } from '@/lib/storeApi';
import { LocationResponse, LocationType, LocationStatus } from '@/types/store';

export default function StoresPage() {
  const router = useRouter();
  const [stores, setStores] = useState<LocationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchStores();
  }, []);

  const fetchStores = async () => {
    try {
      setLoading(true);
      const response = await StoreAPI.getStores(getCurrentTenantId());
      
      if (response.success && response.data) {
        setStores(response.data.content);
      } else {
        setError(response.message || 'Failed to fetch stores');
      }
    } catch (error) {
      console.error('Failed to fetch stores:', error);
      setError('An unexpected error occurred');
    } finally {
      setLoading(false);
    }
  };

  const getLocationTypeIcon = (type: LocationType) => {
    switch (type) {
      case LocationType.STORE:
        return <Building className="h-5 w-5" />;
      case LocationType.WAREHOUSE:
        return <Warehouse className="h-5 w-5" />;
      case LocationType.DISTRIBUTION_CENTER:
        return <MapPin className="h-5 w-5" />;
      default:
        return <Building className="h-5 w-5" />;
    }
  };

  const getStatusColor = (status: LocationStatus) => {
    switch (status) {
      case LocationStatus.ACTIVE:
        return 'bg-green-100 text-green-800';
      case LocationStatus.INACTIVE:
        return 'bg-red-100 text-red-800';
      case LocationStatus.TEMPORARILY_CLOSED:
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatAddress = (store: LocationResponse) => {
    const parts = [];
    if (store.city) parts.push(store.city);
    if (store.stateProvince) parts.push(store.stateProvince);
    if (store.countryCode) parts.push(store.countryCode);
    return parts.join(', ') || 'No address';
  };

  if (loading) {
    return (
      <div className="py-6">
        <div className="px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">Loading stores...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="py-6">
        <div className="px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <p className="text-red-600">Error: {error}</p>
            <button
              onClick={fetchStores}
              className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 cursor-pointer"
            >
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="py-6">
      <div className="px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="sm:flex sm:items-center sm:justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Store Management</h1>
            <p className="mt-2 text-gray-600">
              Manage your store locations, warehouses, and distribution centers.
            </p>
          </div>
          <div className="mt-4 sm:mt-0">
            <button
              onClick={() => router.push('/admin/stores/create')}
              className="inline-flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors cursor-pointer"
            >
              <Plus className="h-4 w-4" />
              Create Store
            </button>
          </div>
        </div>

        {/* Stores Grid */}
        {stores.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-lg border">
            <Building className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No stores found</h3>
            <p className="text-gray-600 mb-6">Get started by creating your first store location.</p>
            <button
              onClick={() => router.push('/admin/stores/create')}
              className="inline-flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors cursor-pointer"
            >
              <Plus className="h-4 w-4" />
              Create First Store
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {stores.map((store) => (
              <div
                key={store.id}
                className="bg-white border rounded-lg shadow-sm hover:shadow-md transition-shadow"
              >
                <div className="p-6">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div className="text-gray-600">
                        {getLocationTypeIcon(store.type)}
                      </div>
                      <div>
                        <h3 className="font-semibold text-gray-900">{store.name}</h3>
                        <p className="text-sm text-gray-600">{store.code}</p>
                      </div>
                    </div>
                    <span className={`px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(store.status)}`}>
                      {store.status.replace('_', ' ')}
                    </span>
                  </div>

                  <div className="space-y-2 mb-4">
                    <div className="text-sm text-gray-600">
                      <span className="font-medium">Type:</span> {store.type.replace('_', ' ')}
                    </div>
                    <div className="text-sm text-gray-600">
                      <span className="font-medium">Location:</span> {formatAddress(store)}
                    </div>
                    {store.latitude && store.longitude && (
                      <div className="text-sm text-gray-600">
                        <span className="font-medium">Coordinates:</span> {store.latitude.toFixed(4)}, {store.longitude.toFixed(4)}
                      </div>
                    )}
                  </div>

                  <div className="flex items-center justify-between pt-4 border-t">
                    <div className="text-xs text-gray-500">
                      Created {new Date(store.createdAt).toLocaleDateString()}
                    </div>
                    <button
                      onClick={() => router.push(`/admin/stores/${store.id}`)}
                      className="inline-flex items-center gap-1 text-blue-600 hover:text-blue-700 text-sm font-medium cursor-pointer"
                    >
                      <Eye className="h-4 w-4" />
                      View Details
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}