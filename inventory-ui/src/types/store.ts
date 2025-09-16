export interface CreateStoreRequest {
  tenantId: number;
  code: string;
  name: string;
  type: LocationType;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  stateProvince?: string;
  postalCode?: string;
  countryCode?: string;
  latitude?: number;
  longitude?: number;
  timezone?: string;
  parentLocationId?: number;
  storeManagerId?: number;
  taxJurisdictionId?: number;
  businessHoursJson?: string;
  capabilitiesJson?: string;
  primaryCurrencyCode?: string;
}

export interface LocationResponse {
  id: number;
  tenantId: number;
  code: string;
  name: string;
  type: LocationType;
  status: LocationStatus;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  stateProvince?: string;
  postalCode?: string;
  countryCode?: string;
  latitude?: number;
  longitude?: number;
  timezone?: string;
  parentLocationId?: number;
  storeManagerId?: number;
  taxJurisdictionId?: number;
  createdAt: string;
  updatedAt: string;
}

export enum LocationType {
  STORE = 'STORE',
  WAREHOUSE = 'WAREHOUSE',
  DISTRIBUTION_CENTER = 'DISTRIBUTION_CENTER'
}

export enum LocationStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  TEMPORARILY_CLOSED = 'TEMPORARILY_CLOSED'
}

export interface TaxJurisdiction {
  id: number;
  tenantId: number;
  code: string;
  name: string;
  countryCode: string;
  stateProvince?: string;
  taxRate: number;
  taxType: TaxType;
  effectiveDate: string;
  expiryDate?: string;
}

export enum TaxType {
  VAT = 'VAT',
  GST = 'GST',
  SALES_TAX = 'SALES_TAX',
  INCOME_TAX = 'INCOME_TAX',
  NONE = 'NONE'
}

export interface StoreFormData {
  // Basic Information
  code: string;
  name: string;
  type: LocationType;
  parentLocationId?: number;
  storeManagerId?: number;

  // Address & Geography
  addressLine1: string;
  addressLine2?: string;
  city: string;
  stateProvince?: string;
  postalCode?: string;
  countryCode: string;
  latitude?: number;
  longitude?: number;
  timezone?: string;

  // Tax & Currency
  taxJurisdictionId?: number;
  primaryCurrencyCode?: string;

  // Configuration
  businessHours?: BusinessHours;
  capabilities?: StoreCapabilities;
}

export interface BusinessHours {
  [key: string]: {
    open: string;
    close: string;
    isClosed: boolean;
  };
}

export interface StoreCapabilities {
  onlinePickup: boolean;
  delivery: boolean;
  returns: boolean;
  phoneOrders: boolean;
  reservations: boolean;
}

export interface Country {
  code: string;
  name: string;
}

export interface Currency {
  code: string;
  name: string;
  symbol: string;
}

export interface Timezone {
  id: string;
  name: string;
  offset: string;
}