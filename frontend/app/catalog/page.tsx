'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { 
  BuildingOfficeIcon, 
  TagIcon, 
  FolderIcon, 
  CubeIcon,
  ChartBarIcon,
  PlusIcon
} from '@heroicons/react/24/outline';

interface CatalogStats {
  departments: { total: number; active: number };
  brands: { total: number; active: number };
  categories: { total: number; active: number };
  items: { total: number; active: number };
}

export default function CatalogPage() {
  const [stats, setStats] = useState<CatalogStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // In a real implementation, this would fetch from the API
    setTimeout(() => {
      setStats({
        departments: { total: 8, active: 7 },
        brands: { total: 45, active: 42 },
        categories: { total: 156, active: 148 },
        items: { total: 2847, active: 2698 }
      });
      setLoading(false);
    }, 1000);
  }, []);

  const catalogModules = [
    {
      name: 'Departments',
      description: 'Manage organizational departments and divisions',
      href: '/catalog/departments',
      icon: BuildingOfficeIcon,
      color: 'bg-blue-500',
      stats: stats?.departments
    },
    {
      name: 'Brands',
      description: 'Manage product brands and manufacturers',
      href: '/catalog/brands',
      icon: TagIcon,
      color: 'bg-green-500',
      stats: stats?.brands
    },
    {
      name: 'Categories',
      description: 'Manage product categories and hierarchies',
      href: '/catalog/categories',
      icon: FolderIcon,
      color: 'bg-purple-500',
      stats: stats?.categories
    },
    {
      name: 'Items',
      description: 'Manage product catalog and variants',
      href: '/catalog/items',
      icon: CubeIcon,
      color: 'bg-orange-500',
      stats: stats?.items
    }
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="border-b border-gray-200 pb-4">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Catalog Management</h1>
            <p className="mt-1 text-sm text-gray-600">
              Manage your product catalog, departments, brands, and categories
            </p>
          </div>
          <div className="flex space-x-3">
            <Link
              href="/catalog/attributes"
              className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
            >
              <ChartBarIcon className="-ml-1 mr-2 h-5 w-5" />
              Attributes
            </Link>
          </div>
        </div>
      </div>

      {/* Statistics Overview */}
      {!loading && stats && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {catalogModules.map((module) => (
            <div key={module.name} className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center">
                <div className={`${module.color} rounded-md p-3`}>
                  <module.icon className="h-6 w-6 text-white" />
                </div>
                <div className="ml-4">
                  <h3 className="text-lg font-medium text-gray-900">{module.name}</h3>
                  <div className="text-sm text-gray-600">
                    {module.stats && (
                      <span>
                        {module.stats.active} active / {module.stats.total} total
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Module Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {catalogModules.map((module) => (
          <Link
            key={module.name}
            href={module.href}
            className="group relative bg-white rounded-lg border border-gray-300 p-6 hover:shadow-lg transition-shadow duration-200"
          >
            <div className="flex items-start space-x-4">
              <div className={`${module.color} rounded-lg p-3 group-hover:scale-110 transition-transform duration-200`}>
                <module.icon className="h-8 w-8 text-white" />
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="text-lg font-medium text-gray-900 group-hover:text-blue-600">
                  {module.name}
                </h3>
                <p className="mt-1 text-sm text-gray-500">{module.description}</p>
                {module.stats && (
                  <div className="mt-2 flex space-x-4 text-sm">
                    <span className="text-green-600 font-medium">
                      {module.stats.active} active
                    </span>
                    <span className="text-gray-500">
                      {module.stats.total} total
                    </span>
                  </div>
                )}
              </div>
              <div className="flex-shrink-0">
                <PlusIcon className="h-5 w-5 text-gray-400 group-hover:text-blue-500" />
              </div>
            </div>
          </Link>
        ))}
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <Link
            href="/catalog/departments/new"
            className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700"
          >
            <PlusIcon className="-ml-1 mr-2 h-4 w-4" />
            Add Department
          </Link>
          <Link
            href="/catalog/brands/new"
            className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-600 hover:bg-green-700"
          >
            <PlusIcon className="-ml-1 mr-2 h-4 w-4" />
            Add Brand
          </Link>
          <Link
            href="/catalog/categories/new"
            className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-purple-600 hover:bg-purple-700"
          >
            <PlusIcon className="-ml-1 mr-2 h-4 w-4" />
            Add Category
          </Link>
          <Link
            href="/catalog/items/new"
            className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-orange-600 hover:bg-orange-700"
          >
            <PlusIcon className="-ml-1 mr-2 h-4 w-4" />
            Add Item
          </Link>
        </div>
      </div>

      {/* Loading State */}
      {loading && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="bg-white rounded-lg border border-gray-300 p-6 animate-pulse">
              <div className="flex items-start space-x-4">
                <div className="bg-gray-300 rounded-lg h-14 w-14"></div>
                <div className="flex-1 space-y-2">
                  <div className="bg-gray-300 h-5 w-24 rounded"></div>
                  <div className="bg-gray-300 h-4 w-full rounded"></div>
                  <div className="bg-gray-300 h-4 w-16 rounded"></div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}