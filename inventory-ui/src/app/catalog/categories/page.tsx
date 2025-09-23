'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { 
  PlusIcon, 
  MagnifyingGlassIcon,
  FolderIcon,
  FolderOpenIcon,
  PencilIcon,
  TrashIcon,
  EyeIcon,
  ChevronRightIcon,
  ChevronDownIcon
} from '@heroicons/react/24/outline';

interface Category {
  id: number;
  code: string;
  name: string;
  description?: string;
  department?: string;
  departmentId?: number;
  parentId?: number;
  isLeaf: boolean;
  sortOrder: number;
  itemCount?: number;
  children?: Category[];
  createdAt: string;
  updatedAt: string;
}

export default function CategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDepartment, setSelectedDepartment] = useState<number | null>(null);
  const [expandedCategories, setExpandedCategories] = useState<Set<number>>(new Set());

  useEffect(() => {
    // Mock data - in real implementation, this would fetch from API
    setTimeout(() => {
      setCategories([
        {
          id: 1,
          code: 'ELEC_PHONES',
          name: 'Mobile Phones',
          description: 'Smartphones and mobile devices',
          department: 'Electronics',
          departmentId: 1,
          isLeaf: false,
          sortOrder: 1,
          itemCount: 45,
          children: [
            {
              id: 11,
              code: 'ELEC_PHONES_SMART',
              name: 'Smartphones',
              department: 'Electronics',
              departmentId: 1,
              parentId: 1,
              isLeaf: true,
              sortOrder: 1,
              itemCount: 35,
              createdAt: '2024-01-15T10:00:00Z',
              updatedAt: '2024-01-15T10:00:00Z'
            },
            {
              id: 12,
              code: 'ELEC_PHONES_BASIC',
              name: 'Basic Phones',
              department: 'Electronics',
              departmentId: 1,
              parentId: 1,
              isLeaf: true,
              sortOrder: 2,
              itemCount: 10,
              createdAt: '2024-01-15T10:00:00Z',
              updatedAt: '2024-01-15T10:00:00Z'
            }
          ],
          createdAt: '2024-01-15T10:00:00Z',
          updatedAt: '2024-01-15T10:00:00Z'
        },
        {
          id: 2,
          code: 'ELEC_LAPTOPS',
          name: 'Laptops',
          description: 'Portable computers and notebooks',
          department: 'Electronics',
          departmentId: 1,
          isLeaf: true,
          sortOrder: 2,
          itemCount: 67,
          createdAt: '2024-01-15T10:00:00Z',
          updatedAt: '2024-01-15T10:00:00Z'
        },
        {
          id: 3,
          code: 'CLTH_MENS',
          name: 'Men\'s Clothing',
          description: 'Clothing items for men',
          department: 'Clothing',
          departmentId: 2,
          isLeaf: false,
          sortOrder: 1,
          itemCount: 123,
          children: [
            {
              id: 31,
              code: 'CLTH_MENS_SHIRTS',
              name: 'Shirts',
              department: 'Clothing',
              departmentId: 2,
              parentId: 3,
              isLeaf: true,
              sortOrder: 1,
              itemCount: 45,
              createdAt: '2024-01-15T10:00:00Z',
              updatedAt: '2024-01-15T10:00:00Z'
            },
            {
              id: 32,
              code: 'CLTH_MENS_PANTS',
              name: 'Pants',
              department: 'Clothing',
              departmentId: 2,
              parentId: 3,
              isLeaf: true,
              sortOrder: 2,
              itemCount: 38,
              createdAt: '2024-01-15T10:00:00Z',
              updatedAt: '2024-01-15T10:00:00Z'
            }
          ],
          createdAt: '2024-01-15T10:00:00Z',
          updatedAt: '2024-01-15T10:00:00Z'
        },
        {
          id: 4,
          code: 'CLTH_WOMENS',
          name: 'Women\'s Clothing',
          description: 'Clothing items for women',
          department: 'Clothing',
          departmentId: 2,
          isLeaf: true,
          sortOrder: 2,
          itemCount: 89,
          createdAt: '2024-01-15T10:00:00Z',
          updatedAt: '2024-01-15T10:00:00Z'
        }
      ]);
      setLoading(false);
    }, 1000);
  }, []);

  const departments = [
    { id: 1, name: 'Electronics' },
    { id: 2, name: 'Clothing' },
    { id: 3, name: 'Home & Garden' }
  ];

  const flattenCategories = (cats: Category[]): Category[] => {
    const result: Category[] = [];
    cats.forEach(cat => {
      result.push(cat);
      if (cat.children) {
        result.push(...flattenCategories(cat.children));
      }
    });
    return result;
  };

  const filteredCategories = categories.filter(cat => {
    const matchesSearch = cat.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         cat.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (cat.description && cat.description.toLowerCase().includes(searchTerm.toLowerCase()));
    
    const matchesDepartment = selectedDepartment === null || cat.departmentId === selectedDepartment;
    
    return matchesSearch && matchesDepartment;
  });

  const toggleExpanded = (categoryId: number) => {
    setExpandedCategories(prev => {
      const newSet = new Set(prev);
      if (newSet.has(categoryId)) {
        newSet.delete(categoryId);
      } else {
        newSet.add(categoryId);
      }
      return newSet;
    });
  };

  const renderCategory = (category: Category, level: number = 0): JSX.Element[] => {
    const isExpanded = expandedCategories.has(category.id);
    const hasChildren = category.children && category.children.length > 0;

    const rows: JSX.Element[] = [];
    
    // Main category row
    rows.push(
      <tr key={category.id} className="hover:bg-gray-50">
        <td className="px-6 py-4 whitespace-nowrap">
          <div className="flex items-center" style={{ marginLeft: `${level * 20}px` }}>
            {hasChildren && (
              <button
                onClick={() => toggleExpanded(category.id)}
                className="mr-2 p-1 rounded hover:bg-gray-100"
              >
                {isExpanded ? (
                  <ChevronDownIcon className="h-4 w-4 text-gray-400" />
                ) : (
                  <ChevronRightIcon className="h-4 w-4 text-gray-400" />
                )}
              </button>
            )}
            <div className="flex-shrink-0 h-8 w-8">
              <div className="h-8 w-8 rounded bg-purple-100 flex items-center justify-center">
                {hasChildren ? (
                  <FolderOpenIcon className="h-5 w-5 text-purple-600" />
                ) : (
                  <FolderIcon className="h-5 w-5 text-purple-600" />
                )}
              </div>
            </div>
            <div className="ml-3">
              <div className="text-sm font-medium text-gray-900">
                {category.name}
              </div>
              <div className="text-sm text-gray-500">
                Code: {category.code}
              </div>
              {category.description && (
                <div className="text-xs text-gray-400 max-w-xs truncate">
                  {category.description}
                </div>
              )}
            </div>
          </div>
        </td>
        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
          {category.department || '-'}
        </td>
        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
            {category.itemCount || 0}
          </span>
        </td>
        <td className="px-6 py-4 whitespace-nowrap">
          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
            category.isLeaf 
              ? 'bg-blue-100 text-blue-800' 
              : 'bg-gray-100 text-gray-800'
          }`}>
            {category.isLeaf ? 'Leaf' : 'Parent'}
          </span>
        </td>
        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
          {new Date(category.updatedAt).toLocaleDateString()}
        </td>
        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
          <div className="flex items-center space-x-2">
            <Link
              href={`/catalog/categories/${category.id}`}
              className="text-purple-600 hover:text-purple-900"
              title="View"
            >
              <EyeIcon className="h-4 w-4" />
            </Link>
            <Link
              href={`/catalog/categories/${category.id}/edit`}
              className="text-gray-600 hover:text-gray-900"
              title="Edit"
            >
              <PencilIcon className="h-4 w-4" />
            </Link>
            <button
              className="text-red-600 hover:text-red-900"
              title="Delete"
            >
              <TrashIcon className="h-4 w-4" />
            </button>
          </div>
        </td>
      </tr>
    );

    // Add child rows if expanded
    if (isExpanded && hasChildren && category.children) {
      category.children.forEach(child => {
        rows.push(...renderCategory(child, level + 1));
      });
    }

    return rows;
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="border-b border-gray-200 pb-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <FolderIcon className="h-8 w-8 text-purple-600" />
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Categories</h1>
              <p className="mt-1 text-sm text-gray-600">
                Manage product categories and hierarchies
              </p>
            </div>
          </div>
          <Link
            href="/catalog/categories/new"
            className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-purple-600 hover:bg-purple-700"
          >
            <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
            Add Category
          </Link>
        </div>
      </div>

      {/* Filters and Search */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
          <div className="relative flex-1 max-w-md">
            <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search categories..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-purple-500 focus:border-purple-500"
            />
          </div>
          <div className="flex items-center space-x-3">
            <select
              value={selectedDepartment || ''}
              onChange={(e) => setSelectedDepartment(e.target.value ? Number(e.target.value) : null)}
              className="block w-full sm:w-auto rounded-md border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
            >
              <option value="">All Departments</option>
              {departments.map(dept => (
                <option key={dept.id} value={dept.id}>{dept.name}</option>
              ))}
            </select>
            <button
              onClick={() => setExpandedCategories(new Set(categories.map(c => c.id)))}
              className="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
            >
              Expand All
            </button>
            <button
              onClick={() => setExpandedCategories(new Set())}
              className="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
            >
              Collapse All
            </button>
          </div>
        </div>
      </div>

      {/* Category List */}
      <div className="bg-white shadow rounded-lg overflow-hidden">
        {loading ? (
          <div className="p-6">
            <div className="animate-pulse space-y-4">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="flex items-center space-x-4">
                  <div className="bg-gray-300 h-8 w-8 rounded"></div>
                  <div className="flex-1 space-y-2">
                    <div className="bg-gray-300 h-4 w-32 rounded"></div>
                    <div className="bg-gray-300 h-3 w-48 rounded"></div>
                  </div>
                  <div className="bg-gray-300 h-6 w-16 rounded"></div>
                </div>
              ))}
            </div>
          </div>
        ) : filteredCategories.length === 0 ? (
          <div className="p-6 text-center">
            <FolderIcon className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No categories found</h3>
            <p className="mt-1 text-sm text-gray-500">
              {searchTerm || selectedDepartment
                ? 'Try adjusting your search or filter criteria.'
                : 'Get started by creating a new category.'}
            </p>
            {(!searchTerm && !selectedDepartment) && (
              <div className="mt-6">
                <Link
                  href="/catalog/categories/new"
                  className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-purple-600 hover:bg-purple-700"
                >
                  <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
                  Add Category
                </Link>
              </div>
            )}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Category
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Department
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Items
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Type
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Updated
                  </th>
                  <th className="relative px-6 py-3">
                    <span className="sr-only">Actions</span>
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white">
                {filteredCategories.flatMap((category) => renderCategory(category))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Summary */}
      {!loading && filteredCategories.length > 0 && (
        <div className="bg-gray-50 px-6 py-3 text-sm text-gray-600">
          Showing {filteredCategories.length} categories
          {selectedDepartment && (
            <span> in {departments.find(d => d.id === selectedDepartment)?.name}</span>
          )}
        </div>
      )}
    </div>
  );
}