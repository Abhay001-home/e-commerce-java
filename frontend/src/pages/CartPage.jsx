import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingCart, Trash2, ArrowRight, Tag, Truck, CheckCircle, RotateCcw } from 'lucide-react';
import {
  fetchCart,
  updateCartItemQty,
  removeCartItem,
  applyCoupon,
  removeCoupon,
} from '../redux/slices/cartSlice';

const CartPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const { cart, loading } = useSelector((state) => state.cart);
  const { isAuthenticated } = useSelector((state) => state.auth);

  const [couponCode, setCouponCode] = useState('');
  const [couponError, setCouponError] = useState('');

  useEffect(() => {
    if (isAuthenticated) {
      dispatch(fetchCart());
    }
  }, [dispatch, isAuthenticated]);

  const handleApplyCoupon = async (e) => {
    e.preventDefault();
    setCouponError('');
    if (!couponCode.trim()) return;

    try {
      const result = await dispatch(applyCoupon(couponCode.trim())).unwrap();
      setCouponCode('');
    } catch (err) {
      setCouponError(err);
    }
  };

  const handleRemoveCoupon = () => {
    dispatch(removeCoupon());
  };

  if (!isAuthenticated) {
    return (
      <div className="py-20 text-center glass-panel rounded-3xl p-8 max-w-lg mx-auto space-y-4 my-12">
        <ShoppingCart className="w-12 h-12 text-slate-500 mx-auto" />
        <h2 className="text-xl font-bold text-slate-100">Sign in to view your cart</h2>
        <p className="text-xs text-slate-400">Save items, apply promo codes, and proceed to checkout.</p>
        <Link to="/login" className="inline-block px-6 py-2.5 rounded-xl gradient-btn text-white text-xs font-bold shadow-lg">
          Sign In
        </Link>
      </div>
    );
  }

  const activeItems = cart?.items?.filter((i) => !i.savedForLater) || [];
  const savedItems = cart?.items?.filter((i) => i.savedForLater) || [];

  const subtotal = cart?.subtotal || 0;
  const freeShippingThreshold = 999;
  const progressPct = Math.min(100, (subtotal / freeShippingThreshold) * 100);

  return (
    <div className="space-y-8 py-8 max-w-7xl mx-auto">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
          <ShoppingCart className="w-6 h-6 text-indigo-400" /> Shopping Cart
        </h1>
        <p className="text-xs text-slate-400">{activeItems.length} items in your active cart</p>
      </div>

      {activeItems.length > 0 ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Active Items Table */}
          <div className="lg:col-span-2 space-y-4">
            {/* Free Shipping Progress Indicator */}
            <div className="p-4 rounded-2xl glass-panel space-y-2 border border-indigo-500/30">
              <div className="flex items-center justify-between text-xs">
                <span className="font-semibold text-indigo-300 flex items-center gap-1.5">
                  <Truck className="w-4 h-4 text-indigo-400" /> Free Shipping Threshold
                </span>
                <span className="text-slate-400 font-medium">
                  {subtotal >= freeShippingThreshold
                    ? '🎉 Free Shipping Unlocked!'
                    : `Add ₹${(freeShippingThreshold - subtotal).toFixed(2)} more for Free Shipping`}
                </span>
              </div>
              <div className="h-2 rounded-full bg-slate-800 overflow-hidden">
                <div className="h-full bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full transition-all duration-500" style={{ width: `${progressPct}%` }} />
              </div>
            </div>

            {/* Cart Items List */}
            <div className="space-y-3">
              {activeItems.map((item) => (
                <div key={item.id} className="p-4 rounded-2xl glass-card flex flex-col sm:flex-row items-center justify-between gap-4">
                  <div className="flex items-center gap-4 w-full sm:w-auto">
                    <img
                      src={item.productImageUrl ? (item.productImageUrl.startsWith('http') ? item.productImageUrl : `/uploads/${item.productImageUrl.replace(/^\/uploads\//, '')}`) : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=300'}
                      alt={item.productName}
                      className="w-16 h-16 rounded-xl object-cover bg-slate-900"
                    />
                    <div>
                      <Link to={`/products/slug/${item.productSlug || item.productId}`} className="text-sm font-semibold text-slate-100 hover:text-indigo-300">
                        {item.productName}
                      </Link>
                      {item.variantName && <span className="text-xs text-slate-400 block">{item.variantName}</span>}
                      <span className="text-xs font-bold text-indigo-400 mt-1 block">₹{item.unitPrice?.toLocaleString()}</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-4 w-full sm:w-auto justify-between sm:justify-end border-t sm:border-0 border-slate-800/80 pt-2 sm:pt-0">
                    {/* Quantity Selector */}
                    <div className="flex items-center rounded-xl bg-slate-900 border border-slate-700">
                      <button
                        onClick={() => dispatch(updateCartItemQty({ itemId: item.id, quantity: item.quantity - 1 }))}
                        disabled={item.quantity <= 1}
                        className="px-2.5 py-1 text-slate-300 hover:text-white disabled:opacity-30"
                      >
                        -
                      </button>
                      <span className="px-2.5 text-xs font-bold text-slate-100">{item.quantity}</span>
                      <button
                        onClick={() => dispatch(updateCartItemQty({ itemId: item.id, quantity: item.quantity + 1 }))}
                        className="px-2.5 py-1 text-slate-300 hover:text-white"
                      >
                        +
                      </button>
                    </div>

                    <span className="text-sm font-bold text-slate-50 w-24 text-right">
                      ₹{item.totalPrice?.toLocaleString()}
                    </span>

                    <button
                      onClick={() => dispatch(removeCartItem(item.id))}
                      className="p-2 rounded-xl text-slate-400 hover:text-pink-500 hover:bg-pink-950/30 transition-colors"
                      title="Remove Item"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Order Summary & Coupon Card */}
          <div className="space-y-6">
            <div className="glass-panel rounded-3xl p-6 space-y-5 border border-slate-700/80">
              <h3 className="text-base font-bold text-slate-100 border-b border-slate-800 pb-3">Order Summary</h3>

              {/* Coupon Form */}
              <div className="space-y-2">
                <label className="text-xs font-semibold text-slate-300 flex items-center gap-1">
                  <Tag className="w-3.5 h-3.5 text-indigo-400" /> Apply Coupon Code
                </label>

                {cart?.appliedCouponCode ? (
                  <div className="p-3 rounded-xl bg-emerald-950/60 border border-emerald-500/40 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <CheckCircle className="w-4 h-4 text-emerald-400" />
                      <span className="text-xs font-bold text-emerald-300">{cart.appliedCouponCode}</span>
                    </div>
                    <button onClick={handleRemoveCoupon} className="text-xs text-pink-400 hover:underline">
                      Remove
                    </button>
                  </div>
                ) : (
                  <form onSubmit={handleApplyCoupon} className="flex gap-2">
                    <input
                      type="text"
                      placeholder="e.g. SUMMER20"
                      value={couponCode}
                      onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                      className="w-full px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-100 placeholder-slate-500 uppercase"
                    />
                    <button type="submit" className="px-4 py-2 rounded-xl bg-indigo-600 text-white text-xs font-bold hover:bg-indigo-500 whitespace-nowrap">
                      Apply
                    </button>
                  </form>
                )}
                {couponError && <p className="text-[10px] text-pink-400 font-medium">{couponError}</p>}
              </div>

              {/* Totals Breakdown */}
              <div className="space-y-2.5 text-xs text-slate-300 pt-3 border-t border-slate-800">
                <div className="flex justify-between">
                  <span>Subtotal</span>
                  <span className="font-semibold text-slate-100">₹{cart?.subtotal?.toLocaleString()}</span>
                </div>

                <div className="flex justify-between">
                  <span>Tax (GST 18%)</span>
                  <span className="font-semibold text-slate-100">₹{cart?.taxAmount?.toLocaleString()}</span>
                </div>

                <div className="flex justify-between">
                  <span>Shipping</span>
                  <span className="font-semibold text-slate-100">
                    {cart?.shippingAmount === 0 ? <span className="text-emerald-400 font-bold">FREE</span> : `₹${cart?.shippingAmount}`}
                  </span>
                </div>

                {cart?.discountAmount > 0 && (
                  <div className="flex justify-between text-pink-400 font-semibold">
                    <span>Discount</span>
                    <span>-₹{cart?.discountAmount?.toLocaleString()}</span>
                  </div>
                )}

                <div className="flex justify-between text-base font-extrabold text-white pt-3 border-t border-slate-800">
                  <span>Grand Total</span>
                  <span className="gradient-text">₹{cart?.grandTotal?.toLocaleString()}</span>
                </div>
              </div>

              <button
                onClick={() => navigate('/checkout')}
                className="w-full py-3.5 rounded-2xl font-bold text-sm text-white gradient-btn shadow-xl hover:scale-102 active:scale-98 transition-all flex items-center justify-center gap-2"
              >
                Proceed to Checkout <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div className="py-20 text-center glass-panel rounded-3xl p-8 space-y-4">
          <ShoppingCart className="w-12 h-12 text-slate-500 mx-auto" />
          <h2 className="text-lg font-bold text-slate-100">Your cart is empty</h2>
          <p className="text-xs text-slate-400">Explore our tech catalog and add your favorite items.</p>
          <Link to="/catalog" className="inline-block px-6 py-2.5 rounded-xl gradient-btn text-white text-xs font-bold shadow-lg">
            Start Shopping
          </Link>
        </div>
      )}
    </div>
  );
};

export default CartPage;
