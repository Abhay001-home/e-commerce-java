import React, { useEffect, useState } from 'react';
import { Package, Download, XCircle, ChevronRight, Clock, FileText } from 'lucide-react';
import axiosClient from '../api/axiosClient';

const OrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        setLoading(true);
        const res = await axiosClient.get('/orders');
        if (res.data) {
          setOrders(res.data.content || []);
        }
      } catch (err) {
        console.error('Error fetching orders:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, []);

  const handleDownloadInvoice = async (orderId, orderNumber) => {
    try {
      const response = await axiosClient.get(`/orders/${orderId}/invoice`, {
        responseType: 'blob',
      });
      const blob = new Blob([response], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `invoice-${orderNumber}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert('Failed to download invoice PDF');
    }
  };

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return;
    try {
      const res = await axiosClient.post(`/orders/${orderId}/cancel?reason=User requested cancellation`);
      if (res.success && res.data) {
        alert('Order cancelled successfully');
        setOrders(orders.map((o) => (o.id === orderId ? res.data : o)));
        if (selectedOrder?.id === orderId) setSelectedOrder(res.data);
      }
    } catch (err) {
      alert(err.message || 'Failed to cancel order');
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'DELIVERED':
        return 'bg-emerald-950 text-emerald-400 border-emerald-500/30';
      case 'SHIPPED':
      case 'PROCESSING':
        return 'bg-indigo-950 text-indigo-400 border-indigo-500/30';
      case 'CANCELLED':
      case 'REFUNDED':
        return 'bg-pink-950 text-pink-400 border-pink-500/30';
      default:
        return 'bg-amber-950 text-amber-400 border-amber-500/30';
    }
  };

  return (
    <div className="space-y-8 py-8 max-w-7xl mx-auto">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
          <Package className="w-6 h-6 text-indigo-400" /> My Orders & Invoices
        </h1>
        <p className="text-xs text-slate-400">Track order fulfillment, download official invoices, and manage orders</p>
      </div>

      {loading ? (
        <div className="space-y-4 animate-pulse">
          {[1, 2, 3].map((n) => (
            <div key={n} className="h-24 rounded-2xl bg-slate-800/40" />
          ))}
        </div>
      ) : orders.length > 0 ? (
        <div className="space-y-4">
          {orders.map((o) => (
            <div key={o.id} className="glass-card rounded-2xl p-5 space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-800/80 pb-3">
                <div>
                  <div className="flex items-center gap-3">
                    <span className="text-sm font-bold text-slate-100">{o.orderNumber}</span>
                    <span className={`text-[10px] font-extrabold px-2.5 py-0.5 rounded-full border ${getStatusBadge(o.orderStatus)}`}>
                      {o.orderStatus}
                    </span>
                  </div>
                  <span className="text-[10px] text-slate-400 block mt-1">
                    Placed on {new Date(o.createdAt).toLocaleString()}
                  </span>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleDownloadInvoice(o.id, o.orderNumber)}
                    className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-200 flex items-center gap-1.5 transition-colors"
                  >
                    <Download className="w-3.5 h-3.5 text-indigo-400" /> Invoice PDF
                  </button>

                  {['PENDING', 'PROCESSING'].includes(o.orderStatus) && (
                    <button
                      onClick={() => handleCancelOrder(o.id)}
                      className="px-3 py-1.5 rounded-xl bg-pink-950/60 hover:bg-pink-900/60 text-xs font-semibold text-pink-400 border border-pink-500/30 flex items-center gap-1 transition-colors"
                    >
                      <XCircle className="w-3.5 h-3.5" /> Cancel
                    </button>
                  )}

                  <button
                    onClick={() => setSelectedOrder(selectedOrder?.id === o.id ? null : o)}
                    className="px-3 py-1.5 rounded-xl gradient-btn text-xs font-semibold text-white flex items-center gap-1"
                  >
                    Details <ChevronRight className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              {/* Collapsible Details */}
              {selectedOrder?.id === o.id && (
                <div className="pt-2 space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
                    <div className="p-3 rounded-xl bg-slate-900/60 space-y-1">
                      <span className="font-bold text-slate-300 block">Shipping Address:</span>
                      <p className="text-slate-400 whitespace-pre-line">{o.shippingAddressSnapshot}</p>
                    </div>

                    <div className="p-3 rounded-xl bg-slate-900/60 space-y-1">
                      <span className="font-bold text-slate-300 block">Payment Info:</span>
                      <p className="text-slate-400">Method: {o.payment?.paymentMethod}</p>
                      <p className="text-slate-400">Status: {o.payment?.paymentStatus}</p>
                      <p className="text-slate-400">Txn ID: {o.payment?.transactionId || 'N/A'}</p>
                    </div>
                  </div>

                  {/* Line Items */}
                  <div className="space-y-2">
                    <span className="text-xs font-bold text-slate-300">Items:</span>
                    {o.items?.map((item) => (
                      <div key={item.id} className="flex justify-between items-center text-xs py-1.5 border-b border-slate-800/40">
                        <span className="text-slate-200">{item.productName} (x{item.quantity})</span>
                        <span className="font-bold text-slate-100">₹{item.totalPrice?.toLocaleString()}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="flex justify-between items-center pt-2 text-xs">
                <span className="text-slate-400">{o.items?.length || 0} line items</span>
                <span className="text-sm font-extrabold text-white">Grand Total: ₹{o.grandTotal?.toLocaleString()}</span>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="py-20 text-center glass-panel rounded-3xl p-8 space-y-3">
          <Package className="w-12 h-12 text-slate-500 mx-auto" />
          <h2 className="text-lg font-bold text-slate-100">No orders placed yet</h2>
          <p className="text-xs text-slate-400">Start exploring our catalog and place your first order!</p>
        </div>
      )}
    </div>
  );
};

export default OrdersPage;
