'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import toast from 'react-hot-toast';
import { Eye, EyeOff, Key, CheckCircle, AlertCircle, ArrowLeft } from 'lucide-react';
import { cn } from '@/lib/utils';
import { AuthAPI } from '@/lib/api';
import { resetPasswordSchema, ResetPasswordFormData } from '@/lib/validations';

export default function ResetPasswordPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [tokenValid, setTokenValid] = useState<boolean | null>(null);
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get('token');

  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
  } = useForm<ResetPasswordFormData>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      token: token || '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const newPassword = watch('newPassword');

  // Validate token on component mount
  useEffect(() => {
    const validateToken = async () => {
      if (!token) {
        setTokenValid(false);
        return;
      }

      try {
        await AuthAPI.validateResetToken(token);
        setTokenValid(true);
      } catch (error) {
        console.error('Token validation error:', error);
        setTokenValid(false);
      }
    };

    validateToken();
  }, [token]);

  const onSubmit = async (data: ResetPasswordFormData) => {
    setIsLoading(true);
    
    try {
      await AuthAPI.resetPassword({
        token: data.token,
        newPassword: data.newPassword,
        confirmPassword: data.confirmPassword,
      });
      
      setIsSuccess(true);
      toast.success('Password reset successfully!');
    } catch (error: any) {
      console.error('Reset password error:', error);
      toast.error(error.response?.data?.error?.message || 'Failed to reset password. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  // Token validation loading state
  if (tokenValid === null) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
        <div className="bg-white p-8 rounded-lg shadow-lg">
          <div className="flex items-center justify-center">
            <svg
              className="animate-spin h-8 w-8 text-blue-600"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              ></circle>
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              ></path>
            </svg>
          </div>
          <p className="text-center text-gray-600 mt-4">Validating reset token...</p>
        </div>
      </div>
    );
  }

  // Invalid token state
  if (!tokenValid) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-8">
          <div className="text-center">
            <div className="mx-auto h-12 w-12 text-red-500">
              <AlertCircle className="h-12 w-12" />
            </div>
            <h2 className="mt-4 text-2xl font-semibold text-gray-900">
              Invalid or Expired Link
            </h2>
            <p className="mt-2 text-sm text-gray-600">
              This password reset link is invalid or has expired. Please request a new one.
            </p>
          </div>

          <div className="bg-white py-8 px-6 shadow-lg rounded-lg text-center space-y-4">
            <Link
              href="/forgot-password"
              className="inline-flex justify-center items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Request New Link
            </Link>
            
            <div>
              <Link
                href="/login"
                className="inline-flex items-center text-sm text-blue-600 hover:text-blue-500 hover:underline"
              >
                <ArrowLeft className="h-4 w-4 mr-1" />
                Back to Sign In
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Success state
  if (isSuccess) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-8">
          <div className="text-center">
            <div className="mx-auto h-12 w-12 text-green-500">
              <CheckCircle className="h-12 w-12" />
            </div>
            <h2 className="mt-4 text-2xl font-semibold text-gray-900">
              Password Reset Successful
            </h2>
            <p className="mt-2 text-sm text-gray-600">
              Your password has been successfully reset. You can now sign in with your new password.
            </p>
          </div>

          <div className="bg-white py-8 px-6 shadow-lg rounded-lg text-center">
            <Link
              href="/login"
              className="inline-flex justify-center items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Sign In Now
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // Password strength indicator component
  const PasswordStrengthIndicator = ({ password }: { password: string }) => {
    const requirements = [
      { test: (p: string) => p.length >= 8, label: 'At least 8 characters' },
      { test: (p: string) => /[A-Z]/.test(p), label: 'One uppercase letter' },
      { test: (p: string) => /[a-z]/.test(p), label: 'One lowercase letter' },
      { test: (p: string) => /[0-9]/.test(p), label: 'One number' },
      { test: (p: string) => /[^A-Za-z0-9]/.test(p), label: 'One special character' },
    ];

    const passedCount = requirements.filter(req => req.test(password)).length;
    const strength = passedCount / requirements.length;

    return (
      <div className="mt-2">
        <div className="flex items-center justify-between mb-1">
          <span className="text-xs text-gray-500">Password strength</span>
          <span className="text-xs text-gray-500">{Math.round(strength * 100)}%</span>
        </div>
        <div className="w-full bg-gray-200 rounded-full h-1.5">
          <div
            className={cn(
              "h-1.5 rounded-full transition-all duration-300",
              strength < 0.3 ? "bg-red-500" :
              strength < 0.7 ? "bg-yellow-500" : "bg-green-500"
            )}
            style={{ width: `${strength * 100}%` }}
          ></div>
        </div>
        {password && (
          <ul className="mt-2 text-xs space-y-1">
            {requirements.map((req, index) => (
              <li
                key={index}
                className={cn(
                  "flex items-center",
                  req.test(password) ? "text-green-600" : "text-gray-400"
                )}
              >
                <span className="mr-2">{req.test(password) ? "✓" : "○"}</span>
                {req.label}
              </li>
            ))}
          </ul>
        )}
      </div>
    );
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        {/* Header */}
        <div className="text-center">
          <h1 className="text-3xl font-bold text-gray-900">
            Inventory Management
          </h1>
          <h2 className="mt-6 text-2xl font-semibold text-gray-700">
            Set New Password
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            Please enter your new password below
          </p>
        </div>

        {/* Form */}
        <div className="bg-white py-8 px-6 shadow-lg rounded-lg">
          <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
            <input type="hidden" {...register('token')} />

            {/* New Password Field */}
            <div>
              <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700">
                New Password
              </label>
              <div className="mt-1 relative">
                <input
                  {...register('newPassword')}
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  className={cn(
                    "appearance-none relative block w-full px-3 py-2 pr-10 border rounded-md placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm",
                    errors.newPassword ? "border-red-300" : "border-gray-300"
                  )}
                  placeholder="Enter your new password"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4 text-gray-400" />
                  ) : (
                    <Eye className="h-4 w-4 text-gray-400" />
                  )}
                </button>
                {errors.newPassword && (
                  <p className="mt-1 text-sm text-red-600">
                    {errors.newPassword.message}
                  </p>
                )}
                <PasswordStrengthIndicator password={newPassword} />
              </div>
            </div>

            {/* Confirm Password Field */}
            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
                Confirm New Password
              </label>
              <div className="mt-1 relative">
                <input
                  {...register('confirmPassword')}
                  type={showConfirmPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  className={cn(
                    "appearance-none relative block w-full px-3 py-2 pr-10 border rounded-md placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm",
                    errors.confirmPassword ? "border-red-300" : "border-gray-300"
                  )}
                  placeholder="Confirm your new password"
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
                {errors.confirmPassword && (
                  <p className="mt-1 text-sm text-red-600">
                    {errors.confirmPassword.message}
                  </p>
                )}
              </div>
            </div>

            {/* Submit Button */}
            <div>
              <button
                type="submit"
                disabled={isLoading}
                className={cn(
                  "group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500",
                  isLoading
                    ? "bg-blue-400 cursor-not-allowed"
                    : "bg-blue-600 hover:bg-blue-700"
                )}
              >
                {isLoading ? (
                  <>
                    <svg
                      className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                      xmlns="http://www.w3.org/2000/svg"
                      fill="none"
                      viewBox="0 0 24 24"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      ></circle>
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      ></path>
                    </svg>
                    Resetting Password...
                  </>
                ) : (
                  <>
                    <Key className="h-4 w-4 mr-2" />
                    Reset Password
                  </>
                )}
              </button>
            </div>

            {/* Back to Login */}
            <div className="text-center">
              <Link 
                href="/login"
                className="inline-flex items-center text-sm text-blue-600 hover:text-blue-500 hover:underline"
              >
                <ArrowLeft className="h-4 w-4 mr-1" />
                Back to Sign In
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}