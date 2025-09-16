import apiClient from './api';
import { CreateStoreRequest, LocationResponse, TaxJurisdiction } from '@/types/store';

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  errors?: Record<string, string>;
}

export class StoreAPI {
  /**
   * Create a new store
   */
  static async createStore(storeData: CreateStoreRequest): Promise<ApiResponse<LocationResponse>> {
    try {
      const response = await apiClient.post('/v1/locations/stores', storeData);
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Failed to create store:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to create store',
        errors: error.response?.data?.errors
      };
    }
  }

  /**
   * Get all stores for a tenant
   */
  static async getStores(tenantId: number, page = 0, size = 20): Promise<ApiResponse<{
    content: LocationResponse[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }>> {
    try {
      const response = await apiClient.get('/v1/locations/stores', {
        params: { tenantId, page, size }
      });
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Failed to fetch stores:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to fetch stores'
      };
    }
  }

  /**
   * Get a store by ID
   */
  static async getStore(storeId: number): Promise<ApiResponse<LocationResponse>> {
    try {
      const response = await apiClient.get(`/v1/locations/stores/${storeId}`);
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Failed to fetch store:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to fetch store'
      };
    }
  }

  /**
   * Get stores by country
   */
  static async getStoresByCountry(tenantId: number, countryCode: string): Promise<ApiResponse<LocationResponse[]>> {
    try {
      const response = await apiClient.get(`/v1/locations/stores/by-country/${countryCode}`, {
        params: { tenantId }
      });
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Failed to fetch stores by country:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to fetch stores'
      };
    }
  }

  /**
   * Find stores within a geographical radius
   */
  static async getNearbyStores(
    tenantId: number, 
    latitude: number, 
    longitude: number, 
    radiusKm = 50
  ): Promise<ApiResponse<LocationResponse[]>> {
    try {
      const response = await apiClient.get('/v1/locations/stores/nearby', {
        params: { tenantId, latitude, longitude, radiusKm }
      });
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Failed to fetch nearby stores:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to fetch nearby stores'
      };
    }
  }

  /**
   * Get store hierarchy (children of a parent)
   */
  static async getStoreHierarchy(parentId: number): Promise<ApiResponse<LocationResponse[]>> {
    try {
      const response = await apiClient.get(`/v1/locations/stores/${parentId}/children`);
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Failed to fetch store hierarchy:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to fetch store hierarchy'
      };
    }
  }

  /**
   * Get tax jurisdictions for a tenant
   */
  static async getTaxJurisdictions(tenantId: number): Promise<ApiResponse<TaxJurisdiction[]>> {
    try {
      const response = await apiClient.get('/v1/tax-jurisdictions', {
        params: { tenantId }
      });
      return {
        success: true,
        data: response.data.content || response.data // Handle both paginated and non-paginated responses
      };
    } catch (error: any) {
      console.error('Failed to fetch tax jurisdictions:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to fetch tax jurisdictions'
      };
    }
  }

  /**
   * Update a store
   */
  static async updateStore(storeId: number, updateData: Partial<CreateStoreRequest>): Promise<ApiResponse<LocationResponse>> {
    try {
      const response = await apiClient.put(`/v1/locations/stores/${storeId}`, updateData);
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Failed to update store:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to update store',
        errors: error.response?.data?.errors
      };
    }
  }
}

// Helper function to get current tenant ID (you may need to adjust this based on your auth implementation)
export const getCurrentTenantId = (): number => {
  // This should be extracted from the current user's JWT token or auth context
  // For now, returning 1 as default for demo tenant
  return 1;
};

export default StoreAPI;