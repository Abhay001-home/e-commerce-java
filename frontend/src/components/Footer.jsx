import React from 'react';
import { Package, ShieldCheck, Truck, RotateCcw, Headset } from 'lucide-react';
import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer className="bg-slate-950 border-t border-slate-800/80 text-slate-400 mt-20">
      {/* Value Proposition Highlights Banner */}
      <div className="max-w-7xl mx-auto px-4 py-8 border-b border-slate-900 grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="flex items-center gap-3.5 p-3 rounded-2xl bg-slate-900/40">
          <div className="p-3 rounded-xl bg-indigo-950/60 text-indigo-400 border border-indigo-500/20">
            <Truck className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-xs font-semibold text-slate-200">Free Shipping</h4>
            <p className="text-xs text-slate-400">On all orders above ₹999</p>
          </div>
        </div>

        <div className="flex items-center gap-3.5 p-3 rounded-2xl bg-slate-900/40">
          <div className="p-3 rounded-xl bg-purple-950/60 text-purple-400 border border-purple-500/20">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-xs font-semibold text-slate-200">Secure Payments</h4>
            <p className="text-xs text-slate-400">256-Bit Encrypted Checkout</p>
          </div>
        </div>

        <div className="flex items-center gap-3.5 p-3 rounded-2xl bg-slate-900/40">
          <div className="p-3 rounded-xl bg-pink-950/60 text-pink-400 border border-pink-500/20">
            <RotateCcw className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-xs font-semibold text-slate-200">Easy Returns</h4>
            <p className="text-xs text-slate-400">14-Day No-Questions Return</p>
          </div>
        </div>

        <div className="flex items-center gap-3.5 p-3 rounded-2xl bg-slate-900/40">
          <div className="p-3 rounded-xl bg-amber-950/60 text-amber-400 border border-amber-500/20">
            <Headset className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-xs font-semibold text-slate-200">24/7 Support</h4>
            <p className="text-xs text-slate-400">Dedicated Customer Assistance</p>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-12 grid grid-cols-1 md:grid-cols-4 gap-8">
        <div className="space-y-3">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg gradient-btn flex items-center justify-center text-white">
              <Package className="w-4 h-4" />
            </div>
            <span className="gradient-text font-bold text-xl">NovaStore</span>
          </div>
          <p className="text-xs leading-relaxed text-slate-400">
            Your premier destination for high-end electronics, gadgets, and luxury tech lifestyle products.
          </p>
        </div>

        <div>
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-200 mb-3">Shop Categories</h4>
          <ul className="space-y-2 text-xs">
            <li><Link to="/catalog" className="hover:text-indigo-400 transition-colors">Smartphones</Link></li>
            <li><Link to="/catalog" className="hover:text-indigo-400 transition-colors">Laptops & PCs</Link></li>
            <li><Link to="/catalog" className="hover:text-indigo-400 transition-colors">Audio & Headphones</Link></li>
            <li><Link to="/catalog" className="hover:text-indigo-400 transition-colors">Wearables</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-200 mb-3">Account & Orders</h4>
          <ul className="space-y-2 text-xs">
            <li><Link to="/orders" className="hover:text-indigo-400 transition-colors">Track Orders</Link></li>
            <li><Link to="/wishlist" className="hover:text-indigo-400 transition-colors">Wishlist</Link></li>
            <li><Link to="/cart" className="hover:text-indigo-400 transition-colors">View Cart</Link></li>
            <li><Link to="/login" className="hover:text-indigo-400 transition-colors">Sign In</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-200 mb-3">Stay Updated</h4>
          <p className="text-xs text-slate-400 mb-3">Subscribe for exclusive discount codes and tech news.</p>
          <form onSubmit={(e) => e.preventDefault()} className="flex gap-2">
            <input
              type="email"
              placeholder="Enter your email"
              className="w-full px-3 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-indigo-500"
            />
            <button className="px-3 py-2 text-xs gradient-btn text-white font-semibold rounded-xl whitespace-nowrap">
              Join
            </button>
          </form>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-4 border-t border-slate-900 text-center text-xs text-slate-500">
        © {new Date().getFullYear()} NovaStore Inc. All rights reserved. Spring Boot 3.2 + React.js E-Commerce Platform.
      </div>
    </footer>
  );
};

export default Footer;
