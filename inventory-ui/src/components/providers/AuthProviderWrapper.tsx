'use client';

import { AppLayout } from '@/components/layout/AppLayout';

interface AuthProviderWrapperProps {
  children: React.ReactNode;
}

export function AuthProviderWrapper({ children }: AuthProviderWrapperProps) {
  return (
    <AppLayout>
      {children}
    </AppLayout>
  );
}