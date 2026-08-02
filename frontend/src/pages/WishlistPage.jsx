import React, { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import { Heart, ShoppingCart, Trash2, ArrowRight } from 'lucide-react';
import { fetchWishlist, removeFromWishlist } from '../redux/slices/wishlistSlice';
import { addToCart } from '../redux/slices/cartSlice';

const WishlistPage = () => {
  const dispatch = useDispatch();
  const { wishlist, loading } = useSelector((state) => state.wishlist);
  const { isAuthenticated } = useSelector((state) => state.auth);

  useEffect(() => {
    if (isAuthenticated) {
      dispatch(fetchWishlist());
    }
  }, [dispatch, isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <div className="py-20 text-center glass-panel rounded-3xl p-8 max-w-lg mx-auto space-y-4 my-12">
        <Heart className="w-12 h-12 text-slate-500 mx-auto" />
        <h2 className="text-xl font-bold text-slate-100">Sign in to view your Wishlist</h2>
        <p className="text-xs text-slate-400">Save products for later and sync across devices.</p>
        <Link to="/login" className="inline-block px-6 py-2.5 rounded-xl gradient-btn text-white text-xs font-bold shadow-lg">
          Sign In
        </Link>
      </div>
    );
  }

  const items = wishlist?.items || [];

  return (
    <div className="space-y-8 py-8 max-w-7xl mx-auto">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
          <Heart className="w-6 h-6 text-pink-400 fill-pink-400" /> My Saved Wishlist
        </h1>
        <p className="text-xs text-slate-400">{items.length} saved products</p>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6 animate-pulse">
          {[1, 2, 3, 4].map((n) => (
            <div key={n} className="h-72 rounded-2xl bg-slate-800/40" />
          ))}
        </div>
      ) : items.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
          {items.map((item) => (
            <div key={item.id} className="glass-card rounded-2xl p-4 flex flex-col justify-between space-y-4">
              <div className="relative aspect-square rounded-xl overflow-hidden bg-slate-900">
                <img
                  src={item.productImageUrl ? (item.productImageUrl.startsWith('http') ? item.productImageUrl : `/uploads/${item.productImageUrl.replace(/^\/uploads\//, '')}`) : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=300'}
                  alt={item.productName}
                  className="w-full h-full object-cover"
                />
                <button
                  onClick={() => dispatch(removeFromWishlist(item.productId))}
                  className="absolute top-2 right-2 p-2 rounded-full bg-slate-950/80 text-pink-400 hover:bg-slate-950 transition-colors"
                  title="Remove"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>

              <div className="space-y-2">
                <Link to={`/products/slug/${item.productSlug || item.productId}`} className="text-xs font-semibold text-slate-100 line-clamp-1 hover:text-indigo-300">
                  {item.productName}
                </Link>

                <div className="flex items-baseline gap-2">
                  <span className="text-sm font-bold text-white">₹{item.price?.toLocaleString()}</span>
                  {item.mrp && item.mrp > item.price && (
                    <span className="text-[10px] text-slate-500 line-through">₹{item.mrp?.toLocaleString()}</span>
                  )}
                </div>

                <span className={`text-[10px] font-semibold block ${item.inStock ? 'text-emerald-400' : 'text-pink-400'}`}>
                  {item.inStock ? 'In Stock' : 'Out of Stock'}
                </span>
              </div>

              <button
                disabled={!item.inStock}
                onClick={() => {
                  dispatch(addToCart({ productId: item.productId, quantity: 1 }));
                  dispatch(removeFromWishlist(item.productId));
                }}
                className="w-full py-2 rounded-xl gradient-btn text-white text-xs font-bold flex items-center justify-center gap-1.5 shadow-md disabled:opacity-40"
              >
                <ShoppingCart className="w-3.5 h-3.5" /> Move to Cart
              </button>
            </div>
          ))}
        </div>
      ) : (
        <div className="py-20 text-center glass-panel rounded-3xl p-8 space-y-3">
          <Heart className="w-12 h-12 text-slate-500 mx-auto" />
          <h2 className="text-lg font-bold text-slate-100">Your Wishlist is empty</h2>
          <p className="text-xs text-slate-400">Save items while browsing to easily buy them later.</p>
          <Link to="/catalog" className="inline-block px-6 py-2.5 rounded-xl gradient-btn text-white text-xs font-bold shadow-lg">
            Explore Catalog
          </Link>
        </div>
      )}
    </div>
  );
};

export default WishlistPage;
