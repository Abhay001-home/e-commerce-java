import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { ShoppingCart, Heart, ShieldCheck, Truck, RotateCcw, Send } from 'lucide-react';
import axiosClient from '../api/axiosClient';
import { addToCart } from '../redux/slices/cartSlice';
import { addToWishlist, removeFromWishlist } from '../redux/slices/wishlistSlice';
import RatingStars from '../components/RatingStars';

const ProductDetailPage = () => {
  const { id, slug } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const [product, setProduct] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [reviewSummary, setReviewSummary] = useState(null);
  const [selectedVariant, setSelectedVariant] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [selectedImage, setSelectedImage] = useState('');
  const [loading, setLoading] = useState(true);

  // Review Form state
  const [newRating, setNewRating] = useState(5);
  const [newTitle, setNewTitle] = useState('');
  const [newComment, setNewComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);

  const { isAuthenticated } = useSelector((state) => state.auth);
  const { wishlist } = useSelector((state) => state.wishlist);

  useEffect(() => {
    const fetchDetail = async () => {
      try {
        setLoading(true);
        const endpoint = slug ? `/products/slug/${slug}` : `/products/${id}`;
        const prodRes = await axiosClient.get(endpoint);
        const prodData = prodRes.data;
        setProduct(prodData);
        setSelectedImage(prodData.primaryImageUrl);

        if (prodData.variants && prodData.variants.length > 0) {
          setSelectedVariant(prodData.variants[0]);
        }

        // Fetch reviews & summary
        const [revRes, sumRes] = await Promise.all([
          axiosClient.get(`/products/${prodData.id}/reviews`),
          axiosClient.get(`/products/${prodData.id}/reviews/summary`),
        ]);

        if (revRes.data) setReviews(revRes.data.content || []);
        if (sumRes.data) setReviewSummary(sumRes.data);
      } catch (err) {
        console.error('Error fetching product details:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDetail();
  }, [id, slug]);

  const isInWishlist = wishlist?.items?.some(
    (item) => item.productId === product?.id
  );

  const handleAddToCart = () => {
    if (!isAuthenticated) {
      alert('Please log in to add items to cart.');
      return;
    }
    dispatch(
      addToCart({
        productId: product.id,
        variantId: selectedVariant?.id || null,
        quantity,
      })
    );
  };

  const handleBuyNow = () => {
    handleAddToCart();
    navigate('/cart');
  };

  const handleWishlist = () => {
    if (!isAuthenticated) {
      alert('Please log in to manage wishlist.');
      return;
    }
    if (isInWishlist) {
      dispatch(removeFromWishlist(product.id));
    } else {
      dispatch(addToWishlist(product.id));
    }
  };

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      alert('Please log in to submit a review.');
      return;
    }
    try {
      setSubmittingReview(true);
      await axiosClient.post(`/products/${product.id}/reviews`, {
        rating: newRating,
        title: newTitle,
        comment: newComment,
      });

      setNewTitle('');
      setNewComment('');
      alert('Review submitted successfully!');

      // Refresh reviews
      const [revRes, sumRes] = await Promise.all([
        axiosClient.get(`/products/${product.id}/reviews`),
        axiosClient.get(`/products/${product.id}/reviews/summary`),
      ]);

      if (revRes.data) setReviews(revRes.data.content || []);
      if (sumRes.data) setReviewSummary(sumRes.data);
    } catch (err) {
      alert(err.message || 'Failed to submit review');
    } finally {
      setSubmittingReview(false);
    }
  };

  if (loading) {
    return (
      <div className="py-12 max-w-6xl mx-auto space-y-8 animate-pulse">
        <div className="h-96 rounded-3xl bg-slate-800/40" />
      </div>
    );
  }

  if (!product) {
    return (
      <div className="py-20 text-center text-slate-400">
        Product not found.
      </div>
    );
  }

  const currentPrice = selectedVariant?.price || product.price;
  const currentMrp = selectedVariant?.mrp || product.mrp;
  const inStock = product.inventory?.quantity > 0;

  return (
    <div className="space-y-12 py-8 max-w-7xl mx-auto">
      {/* Top Product Section */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
        {/* Images Gallery */}
        <div className="space-y-4">
          <div className="aspect-square rounded-3xl overflow-hidden glass-panel border border-slate-700/80 bg-slate-900/60 p-4 flex items-center justify-center">
            <img
              src={selectedImage ? (selectedImage.startsWith('http') ? selectedImage : `/uploads/${selectedImage.replace(/^\/uploads\//, '')}`) : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'}
              alt={product.name}
              className="max-h-full max-w-full object-contain"
              onError={(e) => {
                e.target.src = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600';
              }}
            />
          </div>

          {/* Thumbnails */}
          {product.images && product.images.length > 0 && (
            <div className="flex items-center gap-3 overflow-x-auto pb-2">
              {product.images.map((img, idx) => (
                <button
                  key={idx}
                  onClick={() => setSelectedImage(img.imageUrl)}
                  className={`w-16 h-16 rounded-xl overflow-hidden glass-card p-1 border ${
                    selectedImage === img.imageUrl ? 'border-indigo-500 scale-105' : 'border-slate-800'
                  }`}
                >
                  <img src={img.imageUrl} alt="" className="w-full h-full object-cover rounded-lg" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Product Info & Buy Box */}
        <div className="space-y-6">
          <div>
            <span className="text-xs font-bold text-indigo-400 uppercase tracking-widest">
              {product.categoryName} • {product.brandName}
            </span>
            <h1 className="text-3xl font-extrabold text-slate-100 mt-1">{product.name}</h1>
            <div className="mt-2 flex items-center gap-3">
              <RatingStars rating={product.avgRating || 0} count={product.reviewCount || 0} />
              <span className={`text-xs font-semibold px-2.5 py-0.5 rounded-full ${inStock ? 'bg-emerald-950 text-emerald-400 border border-emerald-500/30' : 'bg-pink-950 text-pink-400 border border-pink-500/30'}`}>
                {inStock ? 'In Stock' : 'Out of Stock'}
              </span>
            </div>
          </div>

          {/* Price Box */}
          <div className="p-4 rounded-2xl glass-panel space-y-1">
            <div className="flex items-baseline gap-3">
              <span className="text-3xl font-extrabold text-white">₹{currentPrice?.toLocaleString()}</span>
              {currentMrp && currentMrp > currentPrice && (
                <span className="text-base text-slate-400 line-through">₹{currentMrp?.toLocaleString()}</span>
              )}
              {product.discountPct > 0 && (
                <span className="text-xs font-bold px-2 py-1 rounded-md bg-pink-500/20 text-pink-400 border border-pink-500/30">
                  Save {Math.round(product.discountPct)}%
                </span>
              )}
            </div>
            <p className="text-xs text-slate-400">Inclusive of all taxes (GST 18%)</p>
          </div>

          {/* Variants Selector */}
          {product.variants && product.variants.length > 0 && (
            <div className="space-y-2">
              <label className="text-xs font-semibold text-slate-300">Select Variant:</label>
              <div className="flex flex-wrap gap-2">
                {product.variants.map((v) => (
                  <button
                    key={v.id}
                    onClick={() => {
                      setSelectedVariant(v);
                      if (v.imageUrl) setSelectedImage(v.imageUrl);
                    }}
                    className={`px-3 py-2 rounded-xl text-xs font-medium border transition-all ${
                      selectedVariant?.id === v.id
                        ? 'border-indigo-500 bg-indigo-950/60 text-indigo-300 shadow-md'
                        : 'border-slate-800 bg-slate-900/60 text-slate-400 hover:border-slate-700'
                    }`}
                  >
                    {v.variantName}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Quantity Counter & CTA Buttons */}
          <div className="space-y-4 pt-2">
            <div className="flex items-center gap-3">
              <label className="text-xs font-semibold text-slate-300">Quantity:</label>
              <div className="flex items-center rounded-xl bg-slate-900 border border-slate-700">
                <button
                  disabled={quantity <= 1}
                  onClick={() => setQuantity(quantity - 1)}
                  className="px-3 py-1 text-slate-300 hover:text-white disabled:opacity-30"
                >
                  -
                </button>
                <span className="px-3 text-xs font-bold text-slate-100">{quantity}</span>
                <button
                  onClick={() => setQuantity(quantity + 1)}
                  className="px-3 py-1 text-slate-300 hover:text-white"
                >
                  +
                </button>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-3">
              <button
                disabled={!inStock}
                onClick={handleAddToCart}
                className="flex-1 py-3.5 px-6 rounded-2xl font-bold text-sm text-white gradient-btn shadow-lg hover:scale-102 active:scale-98 transition-all flex items-center justify-center gap-2 disabled:opacity-40"
              >
                <ShoppingCart className="w-4 h-4" /> Add to Cart
              </button>

              <button
                disabled={!inStock}
                onClick={handleBuyNow}
                className="py-3.5 px-6 rounded-2xl font-semibold text-sm text-slate-100 bg-indigo-950/80 border border-indigo-500/40 hover:bg-indigo-900 transition-colors disabled:opacity-40"
              >
                Buy Now
              </button>

              <button
                onClick={handleWishlist}
                className="p-3.5 rounded-2xl glass-card text-slate-300 hover:text-pink-500"
              >
                <Heart className={`w-5 h-5 ${isInWishlist ? 'fill-pink-500 text-pink-500' : ''}`} />
              </button>
            </div>
          </div>

          {/* Trust Guarantees */}
          <div className="grid grid-cols-3 gap-3 pt-4 border-t border-slate-800/80 text-center">
            <div className="p-2 rounded-xl bg-slate-900/40">
              <Truck className="w-4 h-4 mx-auto text-indigo-400 mb-1" />
              <span className="text-[10px] text-slate-400 block">Free Shipping</span>
            </div>
            <div className="p-2 rounded-xl bg-slate-900/40">
              <ShieldCheck className="w-4 h-4 mx-auto text-purple-400 mb-1" />
              <span className="text-[10px] text-slate-400 block">1 Year Warranty</span>
            </div>
            <div className="p-2 rounded-xl bg-slate-900/40">
              <RotateCcw className="w-4 h-4 mx-auto text-pink-400 mb-1" />
              <span className="text-[10px] text-slate-400 block">Easy Returns</span>
            </div>
          </div>
        </div>
      </div>

      {/* Specifications Table */}
      {product.specifications && product.specifications.length > 0 && (
        <section className="glass-panel rounded-3xl p-6 space-y-4">
          <h3 className="text-lg font-bold text-slate-100 border-b border-slate-800 pb-3">
            Product Specifications
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {product.specifications.map((spec) => (
              <div key={spec.id} className="flex justify-between py-2 border-b border-slate-800/60 text-xs">
                <span className="text-slate-400 font-medium">{spec.specKey}</span>
                <span className="text-slate-100 font-semibold">{spec.specValue}</span>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* Customer Reviews Section */}
      <section className="glass-panel rounded-3xl p-6 space-y-6">
        <h3 className="text-lg font-bold text-slate-100 border-b border-slate-800 pb-3">
          Customer Reviews & Ratings
        </h3>

        {/* Rating Breakdown & Star Distribution */}
        {reviewSummary && (
          <div className="flex flex-col md:flex-row items-center gap-8 p-4 rounded-2xl bg-slate-900/60">
            <div className="text-center">
              <span className="text-4xl font-extrabold text-white">{reviewSummary.averageRating}</span>
              <RatingStars rating={reviewSummary.averageRating} />
              <span className="text-xs text-slate-400 block mt-1">Based on {reviewSummary.totalReviews} reviews</span>
            </div>

            <div className="flex-1 w-full space-y-1.5">
              {[5, 4, 3, 2, 1].map((star) => {
                const count = reviewSummary.ratingDistribution?.[star] || 0;
                const pct = reviewSummary.totalReviews > 0 ? (count / reviewSummary.totalReviews) * 100 : 0;
                return (
                  <div key={star} className="flex items-center gap-3 text-xs">
                    <span className="w-10 text-slate-400 font-medium">{star} Star</span>
                    <div className="flex-1 h-2 rounded-full bg-slate-800 overflow-hidden">
                      <div className="h-full bg-amber-400 rounded-full" style={{ width: `${pct}%` }} />
                    </div>
                    <span className="w-8 text-right text-slate-400">{count}</span>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Write Review Form */}
        {isAuthenticated ? (
          <form onSubmit={handleReviewSubmit} className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-3">
            <h4 className="text-xs font-bold text-slate-200">Write a Review</h4>
            <div className="flex items-center gap-2">
              <span className="text-xs text-slate-400">Your Rating:</span>
              <RatingStars rating={newRating} interactive onRatingChange={setNewRating} />
            </div>
            <input
              type="text"
              placeholder="Review headline / title"
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
              required
              className="w-full px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-100"
            />
            <textarea
              placeholder="Write your detailed product feedback..."
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              required
              rows={3}
              className="w-full px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-100"
            />
            <button
              disabled={submittingReview}
              type="submit"
              className="px-4 py-2 rounded-xl gradient-btn text-white text-xs font-bold flex items-center gap-1.5 shadow-md"
            >
              <Send className="w-3.5 h-3.5" /> Submit Review
            </button>
          </form>
        ) : (
          <p className="text-xs text-slate-400 italic">Please sign in to post a product review.</p>
        )}

        {/* Reviews List */}
        <div className="space-y-4">
          {reviews.length > 0 ? (
            reviews.map((r) => (
              <div key={r.id} className="p-4 rounded-2xl glass-card space-y-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-slate-200">{r.userName}</span>
                    {r.isVerifiedPurchase && (
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-950 text-emerald-400 border border-emerald-500/30">
                        Verified Purchase
                      </span>
                    )}
                  </div>
                  <span className="text-[10px] text-slate-500">
                    {new Date(r.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <RatingStars rating={r.rating} />
                <h5 className="text-xs font-bold text-slate-100">{r.title}</h5>
                <p className="text-xs text-slate-300 leading-relaxed">{r.comment}</p>
              </div>
            ))
          ) : (
            <p className="text-xs text-slate-400 text-center py-6">No customer reviews yet. Be the first to review!</p>
          )}
        </div>
      </section>
    </div>
  );
};

export default ProductDetailPage;
