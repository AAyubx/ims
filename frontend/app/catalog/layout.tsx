import { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Catalog Management',
  description: 'Manage departments, brands, categories, and product attributes',
};

export default function CatalogLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="p-6">
        {children}
      </div>
    </div>
  );
}