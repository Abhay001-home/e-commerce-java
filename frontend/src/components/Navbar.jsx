import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import {
  ShoppingCart,
  Heart,
  User,
  Search,
  LogOut,
  ShieldAlert,
  Package,
  Layers,
  Menu,
  X,
} from 'lucide-react';
import { logout } from '../redux/slices/authSlice';
import { fetchCart } from '../redux/slices/cartSlice';
import { fetchWishlist } from '../redux/slices/wishlistSlice';
import { setFilter } from '../redux/slices/productSlice';

const Navbar = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [searchTerm, setSearchTerm] = useState('');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  const { isAuthenticated, user } = useSelector((state) => state.auth);
  const { cart } = useSelector((state) => state.cart);
  const { wishlist } = useSelector((state) => state.wishlist);

  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  useEffect(() => {
    if (isAuthenticated) {
      dispatch(fetchCart());
      dispatch(fetchWishlist());
    }
  }, [isAuthenticated, dispatch]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      dispatch(setFilter({ keyword: searchTerm }));
      navigate('/catalog');
    }
  };

  const handleLogout = () => {
    dispatch(logout());
    setIsProfileOpen(false);
    navigate('/login');
  };

  const activeCartCount = cart?.totalItems || cart?.items?.length || 0;
  const wishlistCount = wishlist?.totalItems || wishlist?.items?.length || 0;

  return (
    <header className="sticky top-0 z-50 glass-panel border-b border-slate-800/80">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
        {/* Brand Logo */}
        <Link to="/" className="flex items-center gap-2 font-bold text-xl tracking-tight">
          <div className="w-9 h-9 rounded-xl gradient-btn flex items-center justify-center text-white shadow-lg">
            <Package className="w-5 h-5" />
          </div>
          <span className="gradient-text font-extrabold text-2xl">NovaStore</span>
        </Link>

        {/* Search Bar */}
        <form onSubmit={handleSearchSubmit} className="hidden md:flex flex-1 max-w-md relative">
          <input
            type="text"
            placeholder="Search products, brands, categories..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 rounded-xl bg-slate-900/80 border border-slate-700/80 text-sm text-slate-100 placeholder-slate-400 focus:outline-none focus:border-indigo-500 transition-colors"
          />
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
        </form>

        {/* Desktop Navigation & Actions */}
        <div className="hidden md:flex items-center gap-5">
          <Link
            to="/catalog"
            className="text-sm font-medium text-slate-300 hover:text-indigo-400 transition-colors flex items-center gap-1.5"
          >
            <Layers className="w-4 h-4" /> Catalog
          </Link>

          {/* Wishlist Link */}
          <Link
            to="/wishlist"
            className="relative text-slate-300 hover:text-pink-400 transition-colors p-2 rounded-lg hover:bg-slate-800/50"
            title="Wishlist"
          >
            <Heart className="w-5 h-5" />
            {wishlistCount > 0 && (
              <span className="absolute -top-1 -right-1 w-5 h-5 rounded-full bg-pink-500 text-white text-xs font-bold flex items-center justify-center shadow-md animate-pulse">
                {wishlistCount}
              </span>
            )}
          </Link>

          {/* Cart Link */}
          <Link
            to="/cart"
            className="relative text-slate-300 hover:text-indigo-400 transition-colors p-2 rounded-lg hover:bg-slate-800/50"
            title="Cart"
          >
            <ShoppingCart className="w-5 h-5" />
            {activeCartCount > 0 && (
              <span className="absolute -top-1 -right-1 w-5 h-5 rounded-full bg-indigo-500 text-white text-xs font-bold flex items-center justify-center shadow-md animate-pulse">
                {activeCartCount}
              </span>
            )}
          </Link>

          {/* Admin Dashboard Pill */}
          {isAdmin && (
            <Link
              to="/admin/dashboard"
              className="px-3 py-1.5 rounded-xl bg-purple-900/40 text-purple-300 border border-purple-500/30 text-xs font-semibold hover:bg-purple-800/60 transition-colors flex items-center gap-1.5 shadow-sm"
            >
              <ShieldAlert className="w-3.5 h-3.5 text-purple-400" /> Admin Panel
            </Link>
          )}

          {/* Auth Menu */}
          {isAuthenticated ? (
            <div className="relative">
              <button
                onClick={() => setIsProfileOpen(!isProfileOpen)}
                className="flex items-center gap-2 p-1.5 rounded-xl hover:bg-slate-800/60 transition-colors"
              >
                <div className="w-8 h-8 rounded-full bg-indigo-600/80 text-white font-semibold text-xs flex items-center justify-center border border-indigo-400/40">
                  {user?.firstName ? user.firstName[0].toUpperCase() : 'U'}
                </div>
                <span className="text-sm font-medium text-slate-200">{user?.firstName}</span>
              </button>

              {/* Profile Dropdown */}
              {isProfileOpen && (
                <div className="absolute right-0 mt-2 w-48 glass-panel rounded-2xl p-2 shadow-2xl border border-slate-700/80 flex flex-col gap-1 z-50">
                  <Link
                    to="/orders"
                    onClick={() => setIsProfileOpen(false)}
                    className="px-3 py-2 text-sm text-slate-200 hover:bg-slate-800/80 rounded-xl flex items-center gap-2 transition-colors"
                  >
                    <Package className="w-4 h-4 text-indigo-400" /> My Orders
                  </Link>

                  <button
                    onClick={handleLogout}
                    className="w-full text-left px-3 py-2 text-sm text-pink-400 hover:bg-pink-950/40 rounded-xl flex items-center gap-2 transition-colors"
                  >
                    <LogOut className="w-4 h-4" /> Logout
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="px-4 py-2 text-sm font-medium text-slate-300 hover:text-white transition-colors"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                className="px-4 py-2 text-sm font-medium text-white gradient-btn rounded-xl shadow-md hover:scale-105 transition-all"
              >
                Get Started
              </Link>
            </div>
          )}
        </div>

        {/* Mobile Hamburger Toggle */}
        <button
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          className="md:hidden p-2 text-slate-300 hover:text-white"
        >
          {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Mobile Drawer */}
      {isMobileMenuOpen && (
        <div className="md:hidden glass-panel border-t border-slate-800 p-4 space-y-3">
          <form onSubmit={handleSearchSubmit} className="relative">
            <input
              type="text"
              placeholder="Search products..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-slate-100"
            />
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
          </form>

          <Link
            to="/catalog"
            onClick={() => setIsMobileMenuOpen(false)}
            className="block text-sm font-medium text-slate-300 py-2"
          >
            Catalog
          </Link>
          <Link
            to="/cart"
            onClick={() => setIsMobileMenuOpen(false)}
            className="block text-sm font-medium text-slate-300 py-2"
          >
            Cart ({activeCartCount})
          </Link>
          <Link
            to="/wishlist"
            onClick={() => setIsMobileMenuOpen(false)}
            className="block text-sm font-medium text-slate-300 py-2"
          >
            Wishlist ({wishlistCount})
          </Link>
          {isAdmin && (
            <Link
              to="/admin/dashboard"
              onClick={() => setIsMobileMenuOpen(false)}
              className="block text-sm font-medium text-purple-400 py-2"
            >
              Admin Dashboard
            </Link>
          )}

          {isAuthenticated ? (
            <button
              onClick={handleLogout}
              className="w-full text-left text-sm text-pink-400 py-2 flex items-center gap-2"
            >
              <LogOut className="w-4 h-4" /> Logout
            </button>
          ) : (
            <div className="flex flex-col gap-2 pt-2 border-t border-slate-800">
              <Link
                to="/login"
                onClick={() => setIsMobileMenuOpen(false)}
                className="text-center py-2 text-sm text-slate-300"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                onClick={() => setIsMobileMenuOpen(false)}
                className="text-center py-2 text-sm text-white gradient-btn rounded-xl"
              >
                Register
              </Link>
            </div>
          )}
        </div>
      )}
    </header>
  );
};

export default Navbar;
