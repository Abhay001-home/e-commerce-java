import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useSearchParams } from 'react-router-dom';
import {
  fetchProducts,
  fetchCategories,
  fetchBrands,
  setFilter,
  setPage,
  resetFilters,
} from '../redux/slices/productSlice';
import ProductCard from '../components/ProductCard';
import { Filter, SlidersHorizontal, RotateCcw, Search } from 'lucide-react';

const CatalogPage = () => {
  const dispatch = useDispatch();
  const [searchParams] = useSearchParams();

  const { pagedResponse, categories, brands, loading, filters } = useSelector(
    (state) => state.products
  );

  // Sync URL query params if present on initial load
  useEffect(() => {
    dispatch(fetchCategories());
    dispatch(fetchBrands());

    const catParam = searchParams.get('categoryId');
    const featParam = searchParams.get('isFeatured');
    if (catParam) {
      dispatch(setFilter({ categoryId: catParam }));
    }
    if (featParam) {
      dispatch(setFilter({ isFeatured: featParam === 'true' }));
    }
  }, [dispatch, searchParams]);

  // Fetch products whenever filters or page change
  useEffect(() => {
    dispatch(fetchProducts(filters));
  }, [dispatch, filters]);

  const handleFilterChange = (key, value) => {
    dispatch(setFilter({ [key]: value }));
  };

  const handleSortChange = (e) => {
    const val = e.target.value;
    if (val === 'price_asc') {
      dispatch(setFilter({ sortBy: 'price', sortDir: 'asc' }));
    } else if (val === 'price_desc') {
      dispatch(setFilter({ sortBy: 'price', sortDir: 'desc' }));
    } else if (val === 'rating') {
      dispatch(setFilter({ sortBy: 'avgRating', sortDir: 'desc' }));
    } else {
      dispatch(setFilter({ sortBy: 'newest', sortDir: 'desc' }));
    }
  };

  const products = pagedResponse?.content || [];
  const totalPages = pagedResponse?.totalPages || 0;

  return (
    <div className="space-y-6 py-6">
      {/* Page Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800/80 pb-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
            <SlidersHorizontal className="w-6 h-6 text-indigo-400" /> Product Catalog
          </h1>
          <p className="text-xs text-slate-400">
            Showing {pagedResponse?.totalElements || 0} items
          </p>
        </div>

        {/* Sort Dropdown */}
        <div className="flex items-center gap-3">
          <label className="text-xs text-slate-400 font-medium">Sort By:</label>
          <select
            onChange={handleSortChange}
            className="px-3 py-1.5 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-200 focus:outline-none focus:border-indigo-500"
          >
            <option value="newest">Newest Arrivals</option>
            <option value="price_asc">Price: Low to High</option>
            <option value="price_desc">Price: High to Low</option>
            <option value="rating">Highest Rated</option>
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {/* Sidebar Filters */}
        <aside className="glass-panel rounded-2xl p-5 space-y-6 h-fit">
          <div className="flex items-center justify-between border-b border-slate-800 pb-3">
            <h3 className="text-sm font-bold text-slate-200 flex items-center gap-1.5">
              <Filter className="w-4 h-4 text-indigo-400" /> Filters
            </h3>
            <button
              onClick={() => dispatch(resetFilters())}
              className="text-xs text-pink-400 hover:underline flex items-center gap-1"
            >
              <RotateCcw className="w-3 h-3" /> Reset
            </button>
          </div>

          {/* Search Keyword */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-slate-300">Keyword</label>
            <div className="relative">
              <input
                type="text"
                placeholder="Search..."
                value={filters.keyword}
                onChange={(e) => handleFilterChange('keyword', e.target.value)}
                className="w-full pl-9 pr-3 py-1.5 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-100"
              />
              <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-2.5" />
            </div>
          </div>

          {/* Category Filter */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-slate-300">Category</label>
            <select
              value={filters.categoryId}
              onChange={(e) => handleFilterChange('categoryId', e.target.value)}
              className="w-full px-3 py-1.5 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-200"
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          {/* Brand Filter */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-slate-300">Brand</label>
            <select
              value={filters.brandId}
              onChange={(e) => handleFilterChange('brandId', e.target.value)}
              className="w-full px-3 py-1.5 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-200"
            >
              <option value="">All Brands</option>
              {brands.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name}
                </option>
              ))}
            </select>
          </div>

          {/* Price Range Filter */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-slate-300">Price Range (₹)</label>
            <div className="grid grid-cols-2 gap-2">
              <input
                type="number"
                placeholder="Min"
                value={filters.minPrice}
                onChange={(e) => handleFilterChange('minPrice', e.target.value)}
                className="w-full px-2.5 py-1.5 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-100"
              />
              <input
                type="number"
                placeholder="Max"
                value={filters.maxPrice}
                onChange={(e) => handleFilterChange('maxPrice', e.target.value)}
                className="w-full px-2.5 py-1.5 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-100"
              />
            </div>
          </div>
        </aside>

        {/* Product Grid & Pagination */}
        <main className="md:col-span-3 space-y-6">
          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {[1, 2, 3, 4, 5, 6].map((n) => (
                <div key={n} className="h-80 rounded-2xl bg-slate-800/40 animate-pulse" />
              ))}
            </div>
          ) : products.length > 0 ? (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {products.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>

              {/* Pagination controls */}
              {totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 pt-6">
                  <button
                    disabled={filters.page === 0}
                    onClick={() => dispatch(setPage(filters.page - 1))}
                    className="px-3 py-1.5 rounded-xl bg-slate-800 text-xs text-slate-200 disabled:opacity-40"
                  >
                    Previous
                  </button>
                  <span className="text-xs text-slate-400 px-2">
                    Page {filters.page + 1} of {totalPages}
                  </span>
                  <button
                    disabled={filters.page + 1 >= totalPages}
                    onClick={() => dispatch(setPage(filters.page + 1))}
                    className="px-3 py-1.5 rounded-xl bg-slate-800 text-xs text-slate-200 disabled:opacity-40"
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          ) : (
            <div className="glass-panel p-12 text-center rounded-2xl text-slate-400">
              No products found matching your criteria. Try adjusting your filters.
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default CatalogPage;
