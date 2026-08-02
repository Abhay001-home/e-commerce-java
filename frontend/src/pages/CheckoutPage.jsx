import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { MapPin, CreditCard, Banknote, ShieldCheck, CheckCircle2, Plus } from 'lucide-react';
import axiosClient from '../api/axiosClient';
import { fetchCart } from '../redux/slices/cartSlice';

const CheckoutPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const { cart } = useSelector((state) => state.cart);
  const { user } = useSelector((state) => state.auth);

  const [addresses, setAddresses] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('CASH_ON_DELIVERY');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  // New Address Form Modal
  const [showAddAddress, setShowAddAddress] = useState(false);
  const [newAddress, setNewAddress] = useState({
    fullName: user?.firstName ? `${user.firstName} ${user.lastName}` : '',
    phone: user?.phone || '',
    street: '',
    city: '',
    state: '',
    zipCode: '',
    country: 'India',
    addressType: 'HOME',
    isDefault: true,
  });

  useEffect(() => {
    const loadAddresses = async () => {
      try {
        setLoading(true);
        const res = await axiosClient.get('/addresses');
        if (res.data) {
          setAddresses(res.data);
          const defaultAddr = res.data.find((a) => a.isDefault) || res.data[0];
          if (defaultAddr) setSelectedAddressId(defaultAddr.id);
        }
      } catch (err) {
        console.error('Error fetching addresses:', err);
      } finally {
        setLoading(false);
      }
    };

    loadAddresses();
    dispatch(fetchCart());
  }, [dispatch]);

  const handleAddAddressSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await axiosClient.post('/addresses', newAddress);
      if (res.data) {
        setAddresses([...addresses, res.data]);
        setSelectedAddressId(res.data.id);
        setShowAddAddress(false);
      }
    } catch (err) {
      alert(err.message || 'Failed to save address');
    }
  };

  const handlePlaceOrder = async () => {
    if (!selectedAddressId) {
      alert('Please select or add a shipping address.');
      return;
    }

    try {
      setSubmitting(true);
      const payload = {
        shippingAddressId: selectedAddressId,
        paymentMethod,
        notes,
      };

      const res = await axiosClient.post('/orders/checkout', payload);
      if (res.success && res.data) {
        dispatch(fetchCart());
        navigate(`/orders/${res.data.id}`);
      }
    } catch (err) {
      alert(err.message || 'Checkout failed');
    } finally {
      setSubmitting(false);
    }
  };

  const activeItems = cart?.items?.filter((i) => !i.savedForLater) || [];

  return (
    <div className="space-y-8 py-8 max-w-7xl mx-auto">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
          <ShieldCheck className="w-6 h-6 text-emerald-400" /> Secure Checkout
        </h1>
        <p className="text-xs text-slate-400">Review address, select payment strategy, and place your order</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Main Steps */}
        <div className="lg:col-span-2 space-y-6">
          {/* Step 1: Select Shipping Address */}
          <div className="glass-panel rounded-3xl p-6 space-y-4 border border-slate-700/80">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h2 className="text-sm font-bold text-slate-100 flex items-center gap-2">
                <MapPin className="w-4 h-4 text-indigo-400" /> 1. Shipping Address
              </h2>
              <button
                onClick={() => setShowAddAddress(!showAddAddress)}
                className="text-xs font-semibold text-indigo-400 hover:underline flex items-center gap-1"
              >
                <Plus className="w-3.5 h-3.5" /> Add New Address
              </button>
            </div>

            {/* Add Address Form */}
            {showAddAddress && (
              <form onSubmit={handleAddAddressSubmit} className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-3">
                <h4 className="text-xs font-bold text-slate-200">New Address Details</h4>
                <div className="grid grid-cols-2 gap-3">
                  <input
                    type="text"
                    placeholder="Full Name"
                    value={newAddress.fullName}
                    onChange={(e) => setNewAddress({ ...newAddress, fullName: e.target.value })}
                    required
                    className="px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-100"
                  />
                  <input
                    type="text"
                    placeholder="Phone Number"
                    value={newAddress.phone}
                    onChange={(e) => setNewAddress({ ...newAddress, phone: e.target.value })}
                    required
                    className="px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-100"
                  />
                </div>

                <input
                  type="text"
                  placeholder="Street Address / House No."
                  value={newAddress.street}
                  onChange={(e) => setNewAddress({ ...newAddress, street: e.target.value })}
                  required
                  className="w-full px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-100"
                />

                <div className="grid grid-cols-3 gap-3">
                  <input
                    type="text"
                    placeholder="City"
                    value={newAddress.city}
                    onChange={(e) => setNewAddress({ ...newAddress, city: e.target.value })}
                    required
                    className="px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-100"
                  />
                  <input
                    type="text"
                    placeholder="State"
                    value={newAddress.state}
                    onChange={(e) => setNewAddress({ ...newAddress, state: e.target.value })}
                    required
                    className="px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-100"
                  />
                  <input
                    type="text"
                    placeholder="ZIP Code"
                    value={newAddress.zipCode}
                    onChange={(e) => setNewAddress({ ...newAddress, zipCode: e.target.value })}
                    required
                    className="px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-100"
                  />
                </div>

                <div className="flex justify-end gap-2 pt-2">
                  <button
                    type="button"
                    onClick={() => setShowAddAddress(false)}
                    className="px-3 py-1.5 rounded-xl bg-slate-800 text-xs text-slate-300"
                  >
                    Cancel
                  </button>
                  <button type="submit" className="px-4 py-1.5 rounded-xl gradient-btn text-white text-xs font-bold">
                    Save Address
                  </button>
                </div>
              </form>
            )}

            {/* Address List */}
            {addresses.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {addresses.map((addr) => (
                  <div
                    key={addr.id}
                    onClick={() => setSelectedAddressId(addr.id)}
                    className={`p-4 rounded-2xl border cursor-pointer transition-all ${
                      selectedAddressId === addr.id
                        ? 'border-indigo-500 bg-indigo-950/40 shadow-lg'
                        : 'border-slate-800 bg-slate-900/40 hover:border-slate-700'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-xs font-bold text-slate-100">{addr.fullName}</span>
                      {selectedAddressId === addr.id && <CheckCircle2 className="w-4 h-4 text-indigo-400" />}
                    </div>
                    <p className="text-xs text-slate-400 leading-relaxed">
                      {addr.street}, {addr.city}, {addr.state} {addr.zipCode}
                    </p>
                    <span className="text-[10px] text-slate-500 mt-2 block">Phone: {addr.phone}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-slate-400 italic">No saved addresses found. Please add one above.</p>
            )}
          </div>

          {/* Step 2: Select Payment Method */}
          <div className="glass-panel rounded-3xl p-6 space-y-4 border border-slate-700/80">
            <h2 className="text-sm font-bold text-slate-100 border-b border-slate-800 pb-3 flex items-center gap-2">
              <CreditCard className="w-4 h-4 text-indigo-400" /> 2. Payment Method Strategy
            </h2>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div
                onClick={() => setPaymentMethod('CASH_ON_DELIVERY')}
                className={`p-4 rounded-2xl border cursor-pointer transition-all flex flex-col items-center justify-center text-center gap-2 ${
                  paymentMethod === 'CASH_ON_DELIVERY'
                    ? 'border-indigo-500 bg-indigo-950/40 shadow-lg'
                    : 'border-slate-800 bg-slate-900/40 hover:border-slate-700'
                }`}
              >
                <Banknote className="w-6 h-6 text-emerald-400" />
                <span className="text-xs font-bold text-slate-200">Cash On Delivery</span>
                <span className="text-[10px] text-slate-400">Pay when order arrives</span>
              </div>

              <div
                onClick={() => setPaymentMethod('CREDIT_CARD')}
                className={`p-4 rounded-2xl border cursor-pointer transition-all flex flex-col items-center justify-center text-center gap-2 ${
                  paymentMethod === 'CREDIT_CARD'
                    ? 'border-indigo-500 bg-indigo-950/40 shadow-lg'
                    : 'border-slate-800 bg-slate-900/40 hover:border-slate-700'
                }`}
              >
                <CreditCard className="w-6 h-6 text-indigo-400" />
                <span className="text-xs font-bold text-slate-200">Credit / Debit Card</span>
                <span className="text-[10px] text-slate-400">Instant Simulated Gateway</span>
              </div>

              <div
                onClick={() => setPaymentMethod('UPI')}
                className={`p-4 rounded-2xl border cursor-pointer transition-all flex flex-col items-center justify-center text-center gap-2 ${
                  paymentMethod === 'UPI'
                    ? 'border-indigo-500 bg-indigo-950/40 shadow-lg'
                    : 'border-slate-800 bg-slate-900/40 hover:border-slate-700'
                }`}
              >
                <span className="text-base font-extrabold text-purple-400">UPI</span>
                <span className="text-xs font-bold text-slate-200">GPay / PhonePe</span>
                <span className="text-[10px] text-slate-400">Instant QR Payment</span>
              </div>
            </div>

            {/* Optional Order Notes */}
            <div className="pt-2 space-y-1">
              <label className="text-xs font-semibold text-slate-300">Delivery Notes / Special Instructions:</label>
              <textarea
                placeholder="e.g. Leave package at security gate..."
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                rows={2}
                className="w-full px-3 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs text-slate-100"
              />
            </div>
          </div>
        </div>

        {/* Order Items & Grand Total Summary */}
        <div className="space-y-6">
          <div className="glass-panel rounded-3xl p-6 space-y-4 border border-slate-700/80">
            <h3 className="text-base font-bold text-slate-100 border-b border-slate-800 pb-3">
              Items in Order ({activeItems.length})
            </h3>

            <div className="space-y-3 max-h-60 overflow-y-auto pr-1">
              {activeItems.map((item) => (
                <div key={item.id} className="flex items-center justify-between text-xs py-1 border-b border-slate-800/60">
                  <div>
                    <span className="font-semibold text-slate-200 block line-clamp-1">{item.productName}</span>
                    <span className="text-slate-400 text-[10px]">Qty: {item.quantity}</span>
                  </div>
                  <span className="font-bold text-slate-100">₹{item.totalPrice?.toLocaleString()}</span>
                </div>
              ))}
            </div>

            <div className="space-y-2 text-xs text-slate-300 pt-3 border-t border-slate-800">
              <div className="flex justify-between">
                <span>Subtotal</span>
                <span>₹{cart?.subtotal?.toLocaleString()}</span>
              </div>
              <div className="flex justify-between">
                <span>GST (18%)</span>
                <span>₹{cart?.taxAmount?.toLocaleString()}</span>
              </div>
              <div className="flex justify-between">
                <span>Shipping</span>
                <span>{cart?.shippingAmount === 0 ? 'FREE' : `₹${cart?.shippingAmount}`}</span>
              </div>
              {cart?.discountAmount > 0 && (
                <div className="flex justify-between text-pink-400 font-semibold">
                  <span>Discount</span>
                  <span>-₹{cart?.discountAmount?.toLocaleString()}</span>
                </div>
              )}
              <div className="flex justify-between text-base font-extrabold text-white pt-3 border-t border-slate-800">
                <span>Total Amount</span>
                <span className="gradient-text">₹{cart?.grandTotal?.toLocaleString()}</span>
              </div>
            </div>

            <button
              disabled={submitting || !selectedAddressId}
              onClick={handlePlaceOrder}
              className="w-full py-4 rounded-2xl font-bold text-sm text-white gradient-btn shadow-xl hover:scale-102 active:scale-98 transition-all disabled:opacity-40"
            >
              {submitting ? 'Placing Order...' : 'Confirm & Place Order'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CheckoutPage;
