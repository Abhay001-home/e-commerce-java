import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart, Heart, Eye } from 'lucide-react';
import { useDispatch, useSelector } from 'react-redux';
import { addToCart } from '../redux/slices/cartSlice';
import { addToWishlist, removeFromWishlist } from '../redux/slices/wishlistSlice';
import RatingStars from './RatingStars';

const ProductCard = ({ product }) => {
  const dispatch = useDispatch();
  const { isAuthenticated } = useSelector((state) => state.auth);
  const { wishlist } = useSelector((state) => state.wishlist);

  const isInWishlist = wishlist?.items?.some(
    (item) => item.productId === product.id
  );

  const handleCartClick = (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      alert('Please log in to add items to cart.');
      return;
    }
    dispatch(addToCart({ productId: product.id, quantity: 1 }));
  };

  const handleWishlistClick = (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      alert('Please log in to add items to wishlist.');
      return;
    }
    if (isInWishlist) {
      dispatch(removeFromWishlist(product.id));
    } else {
      dispatch(addToWishlist(product.id));
    }
  };

  const imageUrl = product.primaryImageUrl
    ? (product.primaryImageUrl.startsWith('http') ? product.primaryImageUrl : `/uploads/${product.primaryImageUrl.replace(/^\/uploads\//, '')}`)
    : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80';

  return (
    <div className="glass-card rounded-2xl overflow-hidden group flex flex-col justify-between">
      <div className="relative aspect-square overflow-hidden bg-slate-800/50">
        <img
          src={imageUrl}
          alt={product.name}
          className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
          onError={(e) => {
            e.target.src = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80';
          }}
        />

        {/* Badges */}
        <div className="absolute top-3 left-3 flex flex-col gap-1">
          {product.discountPct > 0 && (
            <span className="px-2.5 py-1 text-xs font-bold rounded-full bg-pink-500/90 text-white shadow-lg backdrop-blur-md">
              {Math.round(product.discountPct)}% OFF
            </span>
          )}
          {product.isFeatured && (
            <span className="px-2.5 py-1 text-xs font-bold rounded-full bg-indigo-500/90 text-white shadow-lg backdrop-blur-md">
              Featured
            </span>
          )}
        </div>

        {/* Wishlist Button */}
        <button
          onClick={handleWishlistClick}
          className="absolute top-3 right-3 p-2.5 rounded-full bg-slate-900/60 text-slate-300 hover:text-pink-500 hover:bg-slate-900/90 backdrop-blur-md transition-all shadow-md"
          title={isInWishlist ? 'Remove from Wishlist' : 'Add to Wishlist'}
        >
          <Heart className={`w-4 h-4 ${isInWishlist ? 'fill-pink-500 text-pink-500' : ''}`} />
        </button>

        {/* Hover Quick View Link */}
        <div className="absolute inset-0 bg-slate-950/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-3">
          <Link
            to={`/products/slug/${product.slug || product.id}`}
            className="p-3 rounded-full bg-white text-slate-900 font-medium text-xs flex items-center gap-1.5 shadow-xl hover:bg-indigo-50 transition-colors"
          >
            <Eye className="w-4 h-4" /> View Details
          </Link>
        </div>
      </div>

      {/* Product Content */}
      <div className="p-4 flex-1 flex flex-col justify-between gap-3">
        <div>
          <span className="text-xs text-indigo-400 font-semibold tracking-wider uppercase">
            {product.categoryName || 'Catalog'}
          </span>
          <Link to={`/products/slug/${product.slug || product.id}`}>
            <h3 className="text-sm font-semibold text-slate-100 line-clamp-2 hover:text-indigo-300 transition-colors mt-0.5">
              {product.name}
            </h3>
          </Link>
        </div>

        <div className="space-y-2">
          <RatingStars rating={product.avgRating || 0} count={product.reviewCount || 0} />

          <div className="flex items-baseline justify-between pt-1 border-t border-slate-800/60">
            <div className="flex items-baseline gap-2">
              <span className="text-lg font-bold text-slate-50">
                ₹{product.price?.toLocaleString()}
              </span>
              {product.mrp && product.mrp > product.price && (
                <span className="text-xs text-slate-400 line-through">
                  ₹{product.mrp?.toLocaleString()}
                </span>
              )}
            </div>

            <button
              onClick={handleCartClick}
              className="p-2.5 rounded-xl gradient-btn text-white shadow-md hover:scale-105 active:scale-95 transition-all flex items-center gap-1"
              title="Add to Cart"
            >
              <ShoppingCart className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductCard;
