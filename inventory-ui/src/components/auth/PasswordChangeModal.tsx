'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, AlertCircle, CheckCircle, Lock } from 'lucide-react';
import { cn } from '@/lib/utils';
import { useAuthStore } from '@/stores/authStore';
import { ChangePasswordRequest } from '@/types/auth';

const changePasswordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z
    .string()
    .min(8, 'New password must be at least 8 characters')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
           'Password must contain uppercase, lowercase, number, and special character'),
  confirmPassword: z.string().min(1, 'Please confirm your new password'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

type ChangePasswordFormData = z.infer<typeof changePasswordSchema>;

interface PasswordChangeModalProps {
  isOpen: boolean;
  onClose?: () => void;
  isRequired?: boolean;
}

export default function PasswordChangeModal({ 
  isOpen, 
  onClose, 
  isRequired = false 
}: PasswordChangeModalProps) {
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  
  const { changePassword, isLoading, error, clearError } = useAuthStore();

  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
    reset,
  } = useForm<ChangePasswordFormData>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const newPassword = watch('newPassword');

  // Password strength indicator
  const getPasswordStrength = (password: string) => {
    let strength = 0;
    const checks = [
      /.{8,}/, // At least 8 characters
      /[a-z]/, // Lowercase
      /[A-Z]/, // Uppercase
      /\d/,    // Number
      /[@$!%*?&]/ // Special character
    ];
    
    checks.forEach(check => {
      if (check.test(password)) strength++;
    });
    
    return strength;
  };

  const passwordStrength = getPasswordStrength(newPassword || '');

  const onSubmit = async (data: ChangePasswordFormData) => {
    try {
      clearError();
      const request: ChangePasswordRequest = {
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
        confirmPassword: data.confirmPassword,
        logoutAllSessions: true,
      };
      
      await changePassword(request);
      setIsSuccess(true);
      
      // Close modal after successful change if not required
      if (!isRequired) {
        setTimeout(() => {
          onClose?.();
          reset();
          setIsSuccess(false);
        }, 2000);
      } else {
        // For required password changes, show success for 2 seconds then redirect
        setTimeout(() => {
          window.location.href = '/dashboard';
        }, 2000);
      }
    } catch (err) {
      // Error is already set in the store
      console.error('Password change failed:', err);
    }
  };

  if (!isOpen) return null;

  if (isSuccess) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg p-8 max-w-md w-full">
          <div className="text-center">
            <CheckCircle className="mx-auto h-16 w-16 text-green-500 mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              Password Changed Successfully
            </h3>
            <p className="text-sm text-gray-600">
              {isRequired 
                ? "Your password has been updated. You will be redirected to the dashboard."
                : "Your password has been updated successfully."
              }
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="flex items-center">
            <Lock className="h-5 w-5 text-blue-600 mr-2" />
            <h3 className="text-lg font-medium text-gray-900">
              {isRequired ? 'Password Change Required' : 'Change Password'}
            </h3>
          </div>
          {isRequired && (
            <p className="mt-1 text-sm text-orange-600">
              For security reasons, you must change your password before continuing.
            </p>
          )}
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="px-6 py-4 space-y-4">
          {/* Global Error Message */}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-md p-3">
              <div className="flex">
                <AlertCircle className="h-4 w-4 text-red-400 mt-0.5" />
                <div className="ml-2">
                  <p className="text-sm text-red-700">{error.message}</p>
                </div>
              </div>
            </div>
          )}

          {/* Current Password */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Current Password *
            </label>
            <div className="relative">
              <input
                {...register('currentPassword')}
                type={showCurrentPassword ? 'text' : 'password'}
                className={cn(
                  "w-full px-3 py-2 pr-10 border rounded-md text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500",
                  errors.currentPassword ? "border-red-300" : "border-gray-300"
                )}
                placeholder="Enter current password"
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center"
                onClick={() => setShowCurrentPassword(!showCurrentPassword)}
              >
                {showCurrentPassword ? (
                  <EyeOff className="h-4 w-4 text-gray-400" />
                ) : (
                  <Eye className="h-4 w-4 text-gray-400" />
                )}
              </button>
            </div>
            {errors.currentPassword && (
              <p className="mt-1 text-sm text-red-600">
                {errors.currentPassword.message}
              </p>
            )}
          </div>

          {/* New Password */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              New Password *
            </label>
            <div className="relative">
              <input
                {...register('newPassword')}
                type={showNewPassword ? 'text' : 'password'}
                className={cn(
                  "w-full px-3 py-2 pr-10 border rounded-md text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500",
                  errors.newPassword ? "border-red-300" : "border-gray-300"
                )}
                placeholder="Enter new password"
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center"
                onClick={() => setShowNewPassword(!showNewPassword)}
              >
                {showNewPassword ? (
                  <EyeOff className="h-4 w-4 text-gray-400" />
                ) : (
                  <Eye className="h-4 w-4 text-gray-400" />
                )}
              </button>
            </div>
            
            {/* Password Strength Indicator */}
            {newPassword && (
              <div className="mt-2">
                <div className="flex items-center space-x-1">
                  <div className="flex-1 bg-gray-200 rounded-full h-1">
                    <div 
                      className={cn(
                        "h-1 rounded-full transition-all",
                        passwordStrength === 1 && "w-1/5 bg-red-500",
                        passwordStrength === 2 && "w-2/5 bg-orange-500",
                        passwordStrength === 3 && "w-3/5 bg-yellow-500",
                        passwordStrength === 4 && "w-4/5 bg-blue-500",
                        passwordStrength === 5 && "w-full bg-green-500"
                      )}
                    />
                  </div>
                  <span className={cn(
                    "text-xs font-medium",
                    passwordStrength <= 2 && "text-red-600",
                    passwordStrength === 3 && "text-yellow-600",
                    passwordStrength === 4 && "text-blue-600",
                    passwordStrength === 5 && "text-green-600"
                  )}>
                    {passwordStrength <= 2 && "Weak"}
                    {passwordStrength === 3 && "Fair"}
                    {passwordStrength === 4 && "Good"}
                    {passwordStrength === 5 && "Strong"}
                  </span>
                </div>
              </div>
            )}
            
            {errors.newPassword && (
              <p className="mt-1 text-sm text-red-600">
                {errors.newPassword.message}
              </p>
            )}
          </div>

          {/* Confirm New Password */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Confirm New Password *
            </label>
            <div className="relative">
              <input
                {...register('confirmPassword')}
                type={showConfirmPassword ? 'text' : 'password'}
                className={cn(
                  "w-full px-3 py-2 pr-10 border rounded-md text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500",
                  errors.confirmPassword ? "border-red-300" : "border-gray-300"
                )}
                placeholder="Confirm new password"
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              >
                {showConfirmPassword ? (
                  <EyeOff className="h-4 w-4 text-gray-400" />
                ) : (
                  <Eye className="h-4 w-4 text-gray-400" />
                )}
              </button>
            </div>
            {errors.confirmPassword && (
              <p className="mt-1 text-sm text-red-600">
                {errors.confirmPassword.message}
              </p>
            )}
          </div>

          {/* Password Requirements */}
          <div className="bg-blue-50 rounded-md p-3">
            <p className="text-xs font-medium text-blue-800 mb-2">
              Password Requirements:
            </p>
            <ul className="text-xs text-blue-700 space-y-1">
              <li className="flex items-center">
                <span className="w-1 h-1 bg-blue-400 rounded-full mr-2"></span>
                At least 8 characters long
              </li>
              <li className="flex items-center">
                <span className="w-1 h-1 bg-blue-400 rounded-full mr-2"></span>
                Contains uppercase and lowercase letters
              </li>
              <li className="flex items-center">
                <span className="w-1 h-1 bg-blue-400 rounded-full mr-2"></span>
                Contains at least one number
              </li>
              <li className="flex items-center">
                <span className="w-1 h-1 bg-blue-400 rounded-full mr-2"></span>
                Contains at least one special character
              </li>
            </ul>
          </div>
        </form>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
          {!isRequired && (
            <button
              type="button"
              onClick={() => {
                onClose?.();
                reset();
                clearError();
              }}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
            >
              Cancel
            </button>
          )}
          <button
            type="submit"
            form="password-change-form"
            disabled={isLoading}
            onClick={handleSubmit(onSubmit)}
            className={cn(
              "px-4 py-2 text-sm font-medium text-white rounded-md transition-colors",
              isLoading
                ? "bg-blue-400 cursor-not-allowed"
                : "bg-blue-600 hover:bg-blue-700"
            )}
          >
            {isLoading ? 'Changing Password...' : 'Change Password'}
          </button>
        </div>
      </div>
    </div>
  );
}