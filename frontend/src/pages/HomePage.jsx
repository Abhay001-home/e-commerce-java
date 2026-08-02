import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Sparkles, Zap, Flame, Award, ShieldCheck } from 'lucide-react';
import axiosClient from '../api/axiosClient';
import ProductCard from '../components/ProductCard';

const HomePage = () => {
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadHomeData = async () => {
      try {
        const [featRes, catRes] = await Promise.all([
          axiosClient.get('/products/featured'),
          axiosClient.get('/categories'),
        ]);

        if (featRes.data) setFeaturedProducts(featRes.data);
        if (catRes.data) setCategories(catRes.data);
      } catch (err) {
        console.error('Error loading homepage data:', err);
      } finally {
        setLoading(false);
      }
    };
    loadHomeData();
  }, []);

  return (
    <div className="space-y-16 py-6">
      {/* Hero Banner Section */}
      <section className="relative rounded-3xl overflow-hidden glass-panel p-8 sm:p-12 md:p-16 border border-slate-700/80 shadow-2xl">
        <div className="absolute top-0 right-0 -mr-20 -mt-20 w-96 h-96 bg-indigo-600/20 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute bottom-0 left-0 -ml-20 -mb-20 w-96 h-96 bg-purple-600/20 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10 max-w-2xl space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-indigo-950/80 border border-indigo-500/30 text-indigo-300 text-xs font-semibold shadow-inner">
            <Sparkles className="w-4 h-4 text-indigo-400" /> Premium Technology Collection 2026
          </div>

          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-white tracking-tight leading-tight">
            Next-Gen Tech For <span className="gradient-text">Modern Creators</span>
          </h1>

          <p className="text-base text-slate-300 leading-relaxed font-normal">
            Discover curated smartphones, high-performance laptops, and audio gear with instant discounts, free shipping, and verified warranty.
          </p>

          <div className="flex flex-wrap items-center gap-4 pt-2">
            <Link
              to="/catalog"
              className="px-6 py-3.5 rounded-2xl font-bold text-sm text-white gradient-btn shadow-xl hover:scale-105 active:scale-95 transition-all flex items-center gap-2"
            >
              Explore Catalog <ArrowRight className="w-4 h-4" />
            </Link>

            <Link
              to="/catalog?isFeatured=true"
              className="px-6 py-3.5 rounded-2xl font-semibold text-sm text-slate-200 bg-slate-800/80 border border-slate-700 hover:bg-slate-800 transition-colors flex items-center gap-2"
            >
              <Zap className="w-4 h-4 text-amber-400" /> Featured Deals
            </Link>
          </div>
        </div>
      </section>

      {/* Top Categories Showcase */}
      <section className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Flame className="w-5 h-5 text-amber-500" /> Shop By Category
          </h2>
          <Link to="/catalog" className="text-xs font-semibold text-indigo-400 hover:underline">
            View All →
          </Link>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-4">
          {categories.slice(0, 6).map((cat) => (
            <Link
              key={cat.id}
              to={`/catalog?categoryId=${cat.id}`}
              className="glass-card rounded-2xl p-4 text-center group flex flex-col items-center justify-center gap-2 hover:border-indigo-500/50"
            >
              <div className="w-12 h-12 rounded-xl bg-slate-800/80 group-hover:bg-indigo-600/20 text-indigo-400 group-hover:scale-110 transition-all flex items-center justify-center">
                <Award className="w-6 h-6" />
              </div>
              <span className="text-xs font-semibold text-slate-200 group-hover:text-indigo-300">
                {cat.name}
              </span>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured Products Section */}
      <section className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-indigo-400" /> Featured Products
            </h2>
            <p className="text-xs text-slate-400">Hand-picked flagship devices and top sellers</p>
          </div>

          <Link
            to="/catalog"
            className="px-4 py-2 rounded-xl bg-slate-800/60 border border-slate-700 text-xs font-semibold text-slate-200 hover:bg-slate-800 transition-colors"
          >
            Browse All Products
          </Link>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
            {[1, 2, 3, 4].map((n) => (
              <div key={n} className="h-80 rounded-2xl bg-slate-800/40 animate-pulse" />
            ))}
          </div>
        ) : featuredProducts.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
            {featuredProducts.slice(0, 8).map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        ) : (
          <div className="glass-panel p-8 text-center rounded-2xl text-slate-400 text-sm">
            No featured products available at the moment.
          </div>
        )}
      </section>
    </div>
  );
};

export default HomePage;
