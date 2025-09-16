import { z } from 'zod';
import { LocationType } from '@/types/store';

export const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Please enter a valid email address'),
  password: z
    .string()
    .min(1, 'Password is required'),
  rememberMe: z.boolean().default(false),
});

export const forgotPasswordSchema = z.object({
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Please enter a valid email address'),
});

export const resetPasswordSchema = z.object({
  token: z.string().min(1, 'Reset token is required'),
  newPassword: z
    .string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
    .regex(/[0-9]/, 'Password must contain at least one number')
    .regex(/[^A-Za-z0-9]/, 'Password must contain at least one special character'),
  confirmPassword: z.string().min(1, 'Please confirm your password'),
}).refine(data => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

// Store creation validation schemas
export const basicInfoSchema = z.object({
  code: z
    .string()
    .min(1, 'Store code is required')
    .max(64, 'Store code must be 64 characters or less')
    .regex(/^[A-Z0-9_-]+$/, 'Store code must contain only uppercase letters, numbers, hyphens, and underscores'),
  name: z
    .string()
    .min(1, 'Store name is required')
    .max(255, 'Store name must be 255 characters or less'),
  type: z.nativeEnum(LocationType, {
    errorMap: () => ({ message: 'Please select a valid store type' })
  }),
  parentLocationId: z.number().optional(),
  storeManagerId: z.number().optional(),
});

export const locationSchema = z.object({
  addressLine1: z
    .string()
    .min(1, 'Address line 1 is required')
    .max(255, 'Address line 1 must be 255 characters or less'),
  addressLine2: z
    .string()
    .max(255, 'Address line 2 must be 255 characters or less')
    .optional(),
  city: z
    .string()
    .min(1, 'City is required')
    .max(100, 'City must be 100 characters or less'),
  stateProvince: z
    .string()
    .max(100, 'State/Province must be 100 characters or less')
    .optional(),
  postalCode: z
    .string()
    .max(20, 'Postal code must be 20 characters or less')
    .optional(),
  countryCode: z
    .string()
    .length(2, 'Country code must be exactly 2 characters')
    .regex(/^[A-Z]{2}$/, 'Country code must be 2 uppercase letters'),
  latitude: z
    .number()
    .min(-90, 'Latitude must be between -90 and 90')
    .max(90, 'Latitude must be between -90 and 90')
    .optional(),
  longitude: z
    .number()
    .min(-180, 'Longitude must be between -180 and 180')
    .max(180, 'Longitude must be between -180 and 180')
    .optional(),
  timezone: z
    .string()
    .max(50, 'Timezone must be 50 characters or less')
    .optional(),
});

export const taxCurrencySchema = z.object({
  taxJurisdictionId: z.number().optional(),
  primaryCurrencyCode: z
    .string()
    .length(3, 'Currency code must be exactly 3 characters')
    .regex(/^[A-Z]{3}$/, 'Currency code must be 3 uppercase letters')
    .optional(),
});

export const storeConfigSchema = z.object({
  businessHours: z.record(
    z.object({
      open: z.string(),
      close: z.string(),
      isClosed: z.boolean(),
    })
  ).optional(),
  capabilities: z.object({
    onlinePickup: z.boolean().default(false),
    delivery: z.boolean().default(false),
    returns: z.boolean().default(false),
    phoneOrders: z.boolean().default(false),
    reservations: z.boolean().default(false),
  }).optional(),
});

export const completeStoreSchema = basicInfoSchema
  .merge(locationSchema)
  .merge(taxCurrencySchema)
  .merge(storeConfigSchema);

// Type exports
export type LoginFormData = z.infer<typeof loginSchema>;
export type ForgotPasswordFormData = z.infer<typeof forgotPasswordSchema>;
export type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>;
export type BasicInfoFormData = z.infer<typeof basicInfoSchema>;
export type LocationFormData = z.infer<typeof locationSchema>;
export type TaxCurrencyFormData = z.infer<typeof taxCurrencySchema>;
export type StoreConfigFormData = z.infer<typeof storeConfigSchema>;
export type CompleteStoreFormData = z.infer<typeof completeStoreSchema>;