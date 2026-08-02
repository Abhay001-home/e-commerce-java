import React, { useEffect, useState } from 'react';
import {
  ShieldAlert,
  TrendingUp,
  Package,
  ShoppingBag,
  Users,
  AlertTriangle,
  Tag,
  MessageSquare,
  CheckCircle,
  Truck,
  Plus,
  Trash2,
  Edit,
} from 'lucide-react';
import axiosClient from '../api/axiosClient';

const AdminDashboardPage = () => {
  const [activeTab, setActiveTab] = useState('overview');

  // Overview Data
  const [summary, setSummary] = useState(null);
  const [salesTrend, setSalesTrend] = useState([]);

  // Orders Tab Data
  const [adminOrders, setAdminOrders] = useState([]);
  const [selectedOrderStatus, setSelectedOrderStatus] = useState('');
  const [updatingOrderId, setUpdatingOrderId] = useState(null);
  const [targetStatus, setTargetStatus] = useState('PROCESSING');
  const [statusRemarks, setStatusRemarks] = useState('');

  // Shipment Update Form
  const [shipmentCarrier, setShipmentCarrier] = useState('');
  const [shipmentTracking, setShipmentTracking] = useState('');

  // Coupons Tab Data
  const [coupons, setCoupons] = useState([]);
  const [showCouponModal, setShowCouponModal] = useState(false);
  const [newCoupon, setNewCoupon] = useState({
    code: '',
    discountType: 'PERCENTAGE',
    discountValue: 10,
    maxDiscountAmount: 500,
    minOrderAmount: 499,
    usageLimit: 100,
    isActive: true,
  });

  // Users Tab Data
  const [adminUsers, setAdminUsers] = useState([]);

  // Reviews Tab Data
  const [adminReviews, setAdminReviews] = useState([]);

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, [activeTab]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);

      if (activeTab === 'overview') {
        const [sumRes, trendRes] = await Promise.all([
          axiosClient.get('/admin/dashboard/summary'),
          axiosClient.get('/admin/dashboard/sales-trend'),
        ]);
        if (sumRes.data) setSummary(sumRes.data);
        if (trendRes.data) setSalesTrend(trendRes.data);
      } else if (activeTab === 'orders') {
        const query = selectedOrderStatus ? `?status=${selectedOrderStatus}` : '';
        const res = await axiosClient.get(`/admin/orders${query}`);
        if (res.data) setAdminOrders(res.data.content || []);
      } else if (activeTab === 'coupons') {
        const res = await axiosClient.get('/coupons');
        if (res.data) setCoupons(res.data);
      } else if (activeTab === 'users') {
        const res = await axiosClient.get('/admin/users');
        if (res.data) setAdminUsers(res.data.content || []);
      } else if (activeTab === 'reviews') {
        const res = await axiosClient.get('/admin/reviews');
        if (res.data) setAdminReviews(res.data.content || []);
      }
    } catch (err) {
      console.error('Error loading admin tab data:', err);
    } finally {
      setLoading(false);
    }
  };

  // State Pattern Order Status Transition
  const handleUpdateOrderStatus = async (orderId) => {
    try {
      const res = await axiosClient.put(`/admin/orders/${orderId}/status`, {
        status: targetStatus,
        remarks: statusRemarks || 'Updated by Admin',
      });
      if (res.success && res.data) {
        alert(`Order #${res.data.orderNumber} status updated to ${targetStatus}`);
        setUpdatingOrderId(null);
        setStatusRemarks('');
        loadDashboardData();
      }
    } catch (err) {
      alert(err.message || 'Failed to update order status');
    }
  };

  // Shipment Tracker Update
  const handleUpdateShipment = async (orderId) => {
    if (!shipmentCarrier || !shipmentTracking) {
      alert('Please provide carrier name and tracking number.');
      return;
    }
    try {
      const res = await axiosClient.put(`/admin/orders/${orderId}/shipment`, {
        carrierName: shipmentCarrier,
        trackingNumber: shipmentTracking,
        shipmentStatus: 'DISPATCHED',
      });
      if (res.success) {
        alert('Shipment tracking updated');
        setShipmentCarrier('');
        setShipmentTracking('');
        loadDashboardData();
      }
    } catch (err) {
      alert(err.message || 'Failed to update shipment');
    }
  };

  // Create Coupon
  const handleCreateCoupon = async (e) => {
    e.preventDefault();
    try {
      const res = await axiosClient.post('/coupons', newCoupon);
      if (res.success && res.data) {
        alert('Coupon created successfully');
        setShowCouponModal(false);
        setCoupons([...coupons, res.data]);
      }
    } catch (err) {
      alert(err.message || 'Failed to create coupon');
    }
  };

  // Delete Coupon
  const handleDeleteCoupon = async (id) => {
    if (!window.confirm('Delete this coupon?')) return;
    try {
      await axiosClient.delete(`/coupons/${id}`);
      setCoupons(coupons.filter((c) => c.id !== id));
    } catch (err) {
      alert('Failed to delete coupon');
    }
  };

  // Toggle User Active Status
  const handleToggleUserStatus = async (userId, currentStatus) => {
    try {
      const res = await axiosClient.put(`/admin/users/${userId}/status`, {
        isActive: !currentStatus,
      });
      if (res.success && res.data) {
        setAdminUsers(adminUsers.map((u) => (u.id === userId ? res.data : u)));
      }
    } catch (err) {
      alert('Failed to update user status');
    }
  };

  // Delete Review Moderation
  const handleDeleteReview = async (reviewId) => {
    if (!window.confirm('Moderate & delete this review?')) return;
    try {
      await axiosClient.delete(`/admin/reviews/${reviewId}`);
      setAdminReviews(adminReviews.filter((r) => r.id !== reviewId));
    } catch (err) {
      alert('Failed to delete review');
    }
  };

  return (
    <div className="space-y-8 py-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="border-b border-slate-800 pb-4 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
            <ShieldAlert className="w-6 h-6 text-purple-400" /> Admin Control Center
          </h1>
          <p className="text-xs text-slate-400">Manage store orders, products, coupons, user roles, & sales analytics</p>
        </div>
      </div>

      {/* Tabs Bar */}
      <div className="flex gap-2 border-b border-slate-800/80 overflow-x-auto pb-2">
        {[
          { id: 'overview', label: 'Store Overview', icon: TrendingUp },
          { id: 'orders', label: 'Orders & Fulfillment', icon: ShoppingBag },
          { id: 'coupons', label: 'Coupons Manager', icon: Tag },
          { id: 'users', label: 'User Accounts', icon: Users },
          { id: 'reviews', label: 'Review Moderation', icon: MessageSquare },
        ].map((tab) => {
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-2 rounded-xl text-xs font-semibold flex items-center gap-2 whitespace-nowrap transition-all ${
                activeTab === tab.id
                  ? 'bg-purple-600 text-white shadow-lg'
                  : 'bg-slate-900/60 text-slate-400 hover:bg-slate-800'
              }`}
            >
              <Icon className="w-4 h-4" /> {tab.label}
            </button>
          );
        })}
      </div>

      {/* Tab 1: OVERVIEW */}
      {activeTab === 'overview' && (
        <div className="space-y-8">
          {/* Stat Cards Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="glass-card rounded-2xl p-5 space-y-2 border border-purple-500/20">
              <span className="text-xs text-slate-400 font-medium">Total Store Revenue</span>
              <h2 className="text-3xl font-extrabold text-white">₹{summary?.totalRevenue?.toLocaleString() || '0'}</h2>
              <span className="text-[10px] text-emerald-400 font-semibold">From completed orders</span>
            </div>

            <div className="glass-card rounded-2xl p-5 space-y-2 border border-indigo-500/20">
              <span className="text-xs text-slate-400 font-medium">Total Orders Placed</span>
              <h2 className="text-3xl font-extrabold text-white">{summary?.totalOrders || 0}</h2>
              <span className="text-[10px] text-indigo-300 font-semibold">{summary?.pendingOrders || 0} pending</span>
            </div>

            <div className="glass-card rounded-2xl p-5 space-y-2 border border-pink-500/20">
              <span className="text-xs text-slate-400 font-medium">Registered Customers</span>
              <h2 className="text-3xl font-extrabold text-white">{summary?.totalCustomers || 0}</h2>
              <span className="text-[10px] text-slate-400">Active customer base</span>
            </div>

            <div className="glass-card rounded-2xl p-5 space-y-2 border border-amber-500/20">
              <span className="text-xs text-slate-400 font-medium">Active Catalog Products</span>
              <h2 className="text-3xl font-extrabold text-white">{summary?.totalProducts || 0}</h2>
              <span className="text-[10px] text-amber-400 font-semibold">{summary?.lowStockAlerts?.length || 0} low stock</span>
            </div>
          </div>

          {/* Low Stock Alerts & Sales Trend */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Low Stock Alerts */}
            <div className="glass-panel rounded-3xl p-6 space-y-4 border border-slate-700/80">
              <h3 className="text-sm font-bold text-slate-100 flex items-center gap-2 border-b border-slate-800 pb-3">
                <AlertTriangle className="w-4 h-4 text-amber-400" /> Low Stock Alerts
              </h3>

              {summary?.lowStockAlerts && summary.lowStockAlerts.length > 0 ? (
                <div className="space-y-2">
                  {summary.lowStockAlerts.map((inv) => (
                    <div key={inv.id} className="p-3 rounded-xl bg-amber-950/30 border border-amber-500/30 flex justify-between items-center text-xs">
                      <span className="font-semibold text-slate-200">{inv.productName}</span>
                      <span className="font-bold text-amber-400 px-2 py-0.5 rounded-full bg-amber-950">
                        {inv.quantity} left
                      </span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-slate-400 italic">All products have sufficient stock levels.</p>
              )}
            </div>

            {/* Periodic Sales Trend */}
            <div className="glass-panel rounded-3xl p-6 space-y-4 border border-slate-700/80">
              <h3 className="text-sm font-bold text-slate-100 flex items-center gap-2 border-b border-slate-800 pb-3">
                <TrendingUp className="w-4 h-4 text-emerald-400" /> Sales Trend Report
              </h3>

              {salesTrend.length > 0 ? (
                <div className="space-y-2">
                  {salesTrend.map((st, idx) => (
                    <div key={idx} className="p-3 rounded-xl bg-slate-900/60 flex justify-between items-center text-xs">
                      <span className="font-semibold text-slate-300">{st.period}</span>
                      <div className="text-right">
                        <span className="font-bold text-emerald-400 block">₹{st.revenue?.toLocaleString()}</span>
                        <span className="text-[10px] text-slate-400">{st.orderCount} orders</span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-slate-400 italic">No sales trend data yet.</p>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: ORDERS & FULFILLMENT */}
      {activeTab === 'orders' && (
        <div className="space-y-6">
          <div className="flex items-center gap-3">
            <span className="text-xs text-slate-300 font-semibold">Filter Status:</span>
            <select
              value={selectedOrderStatus}
              onChange={(e) => {
                setSelectedOrderStatus(e.target.value);
                loadDashboardData();
              }}
              className="px-3 py-1.5 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-200"
            >
              <option value="">All Orders</option>
              <option value="PENDING">PENDING</option>
              <option value="PROCESSING">PROCESSING</option>
              <option value="SHIPPED">SHIPPED</option>
              <option value="DELIVERED">DELIVERED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>

          <div className="space-y-4">
            {adminOrders.map((o) => (
              <div key={o.id} className="glass-card rounded-2xl p-5 space-y-4">
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2 border-b border-slate-800 pb-3">
                  <div>
                    <span className="text-sm font-bold text-slate-100">{o.orderNumber}</span>
                    <span className="text-xs text-slate-400 block">{o.userFullName} ({o.userEmail})</span>
                  </div>

                  <div className="flex items-center gap-3">
                    <span className="text-xs font-bold text-white">₹{o.grandTotal?.toLocaleString()}</span>
                    <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-purple-950 text-purple-300 border border-purple-500/30">
                      {o.orderStatus}
                    </span>
                  </div>
                </div>

                {/* State Pattern Status Transitions */}
                <div className="flex flex-wrap items-center gap-3 bg-slate-900/60 p-3 rounded-xl">
                  <span className="text-xs font-semibold text-slate-300">State Transition:</span>
                  <select
                    value={targetStatus}
                    onChange={(e) => setTargetStatus(e.target.value)}
                    className="px-2.5 py-1 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-200"
                  >
                    <option value="PROCESSING">PROCESSING</option>
                    <option value="SHIPPED">SHIPPED</option>
                    <option value="DELIVERED">DELIVERED</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>

                  <input
                    type="text"
                    placeholder="Remarks..."
                    value={updatingOrderId === o.id ? statusRemarks : ''}
                    onChange={(e) => {
                      setUpdatingOrderId(o.id);
                      setStatusRemarks(e.target.value);
                    }}
                    className="px-3 py-1 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-200 flex-1 min-w-[150px]"
                  />

                  <button
                    onClick={() => handleUpdateOrderStatus(o.id)}
                    className="px-3 py-1 rounded-xl gradient-btn text-white text-xs font-bold shadow-md"
                  >
                    Update State
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Tab 3: COUPONS */}
      {activeTab === 'coupons' && (
        <div className="space-y-6">
          <div className="flex justify-between items-center">
            <h2 className="text-base font-bold text-slate-100">Active Promo Coupons</h2>
            <button
              onClick={() => setShowCouponModal(true)}
              className="px-4 py-2 rounded-xl gradient-btn text-white text-xs font-bold flex items-center gap-1.5 shadow-lg"
            >
              <Plus className="w-4 h-4" /> Create New Coupon
            </button>
          </div>

          {/* Coupon Modal */}
          {showCouponModal && (
            <form onSubmit={handleCreateCoupon} className="glass-panel p-6 rounded-3xl space-y-4 max-w-lg border border-purple-500/40">
              <h3 className="text-sm font-bold text-slate-100">Create Coupon Code</h3>
              <div className="grid grid-cols-2 gap-3">
                <input
                  type="text"
                  placeholder="Code (e.g. SAVE20)"
                  value={newCoupon.code}
                  onChange={(e) => setNewCoupon({ ...newCoupon, code: e.target.value.toUpperCase() })}
                  required
                  className="px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-100 uppercase"
                />
                <select
                  value={newCoupon.discountType}
                  onChange={(e) => setNewCoupon({ ...newCoupon, discountType: e.target.value })}
                  className="px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-100"
                >
                  <option value="PERCENTAGE">PERCENTAGE (%)</option>
                  <option value="FIXED_AMOUNT">FIXED AMOUNT (₹)</option>
                </select>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <input
                  type="number"
                  placeholder="Value"
                  value={newCoupon.discountValue}
                  onChange={(e) => setNewCoupon({ ...newCoupon, discountValue: parseFloat(e.target.value) })}
                  required
                  className="px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-100"
                />
                <input
                  type="number"
                  placeholder="Max Cap (₹)"
                  value={newCoupon.maxDiscountAmount}
                  onChange={(e) => setNewCoupon({ ...newCoupon, maxDiscountAmount: parseFloat(e.target.value) })}
                  className="px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-100"
                />
                <input
                  type="number"
                  placeholder="Min Order (₹)"
                  value={newCoupon.minOrderAmount}
                  onChange={(e) => setNewCoupon({ ...newCoupon, minOrderAmount: parseFloat(e.target.value) })}
                  className="px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-slate-100"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowCouponModal(false)} className="px-3 py-1.5 rounded-xl bg-slate-800 text-xs text-slate-300">
                  Cancel
                </button>
                <button type="submit" className="px-4 py-1.5 rounded-xl gradient-btn text-white text-xs font-bold">
                  Save Coupon
                </button>
              </div>
            </form>
          )}

          {/* Coupon List Table */}
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            {coupons.map((c) => (
              <div key={c.id} className="glass-card p-4 rounded-2xl space-y-2 relative">
                <div className="flex justify-between items-center">
                  <span className="text-sm font-extrabold text-indigo-400 uppercase">{c.code}</span>
                  <button onClick={() => handleDeleteCoupon(c.id)} className="text-pink-400 hover:text-pink-300">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
                <p className="text-xs text-slate-300">
                  {c.discountType === 'PERCENTAGE' ? `${c.discountValue}% OFF` : `₹${c.discountValue} FLAT OFF`}
                </p>
                <span className="text-[10px] text-slate-500 block">Min order: ₹{c.minOrderAmount || 0} • Used: {c.usedCount} times</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Tab 4: USER ACCOUNTS */}
      {activeTab === 'users' && (
        <div className="space-y-4">
          <h2 className="text-base font-bold text-slate-100">User Account Administration</h2>
          <div className="glass-panel rounded-2xl overflow-hidden border border-slate-800">
            <table className="w-full text-left text-xs text-slate-300">
              <thead className="bg-slate-900 text-slate-400 font-semibold border-b border-slate-800">
                <tr>
                  <th className="p-3">User</th>
                  <th className="p-3">Email</th>
                  <th className="p-3">Roles</th>
                  <th className="p-3">Status</th>
                  <th className="p-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {adminUsers.map((u) => (
                  <tr key={u.id}>
                    <td className="p-3 font-semibold text-slate-100">{u.firstName} {u.lastName}</td>
                    <td className="p-3 text-slate-400">{u.email}</td>
                    <td className="p-3">
                      {Array.from(u.roles || []).map((r) => (
                        <span key={r} className="px-2 py-0.5 rounded-full bg-purple-950 text-purple-300 border border-purple-500/30 text-[10px] mr-1">
                          {r}
                        </span>
                      ))}
                    </td>
                    <td className="p-3">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] ${u.isActive ? 'bg-emerald-950 text-emerald-400' : 'bg-pink-950 text-pink-400'}`}>
                        {u.isActive ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td className="p-3 text-right">
                      <button
                        onClick={() => handleToggleUserStatus(u.id, u.isActive)}
                        className="px-3 py-1 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-200"
                      >
                        {u.isActive ? 'Disable' : 'Enable'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 5: REVIEWS MODERATION */}
      {activeTab === 'reviews' && (
        <div className="space-y-4">
          <h2 className="text-base font-bold text-slate-100">Review Moderation</h2>
          <div className="space-y-3">
            {adminReviews.map((r) => (
              <div key={r.id} className="glass-card p-4 rounded-2xl flex items-center justify-between gap-4">
                <div className="space-y-1">
                  <span className="text-xs font-bold text-slate-100">{r.userName} on {r.productName}</span>
                  <p className="text-xs text-slate-300">{r.comment}</p>
                </div>
                <button onClick={() => handleDeleteReview(r.id)} className="p-2 rounded-xl bg-pink-950/60 text-pink-400 hover:bg-pink-900/60">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboardPage;
