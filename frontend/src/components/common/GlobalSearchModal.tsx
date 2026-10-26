import React, { useState, useEffect, useRef } from 'react';
import { Search, X, FileText, Users, Package, Truck, ArrowRight } from 'lucide-react';
import { useApp } from '../../context/AppContext';

interface GlobalSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const GlobalSearchModal: React.FC<GlobalSearchModalProps> = ({ isOpen, onClose }) => {
  const [query, setQuery] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);
  const { 
    purchaseOrders, 
    clients, 
    products, 
    deliveryChallans, 
    setSelectedPOId, 
    setSelectedClientId, 
    setSelectedProductId, 
    setSelectedDCId,
    setCurrentTab,
    currentUser
  } = useApp();

  useEffect(() => {
    if (isOpen) {
      setTimeout(() => inputRef.current?.focus(), 50);
    } else {
      setQuery('');
    }
  }, [isOpen]);

  // Keyboard shortcut listener
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        if (isOpen) onClose();
      } else if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const cleanQ = query.trim().toLowerCase();

  // Search POs
  const matchedPOs = cleanQ
    ? purchaseOrders.filter((po) => {
        // Outer State Office can only see own/assigned POs
        if (currentUser.role === 'OUTER_STATE_OFFICE' && po.officeId !== currentUser.officeId) {
          return false;
        }
        return (
          po.poNumber.toLowerCase().includes(cleanQ) ||
          po.clientName.toLowerCase().includes(cleanQ) ||
          po.clientSnapshot.gstin.toLowerCase().includes(cleanQ) ||
          po.clientSnapshot.primaryContact.phone.includes(cleanQ) ||
          po.items.some((i) => i.sku.toLowerCase().includes(cleanQ) || i.productName.toLowerCase().includes(cleanQ))
        );
      })
    : [];

  // Search Clients
  const matchedClients = cleanQ
    ? clients.filter(
        (c) =>
          c.companyName.toLowerCase().includes(cleanQ) ||
          c.gstin.toLowerCase().includes(cleanQ) ||
          c.pan.toLowerCase().includes(cleanQ) ||
          c.primaryContact.name.toLowerCase().includes(cleanQ) ||
          c.primaryContact.phone.includes(cleanQ)
      )
    : [];

  // Search Products
  const matchedProducts = cleanQ
    ? products.filter(
        (p) =>
          p.sku.toLowerCase().includes(cleanQ) ||
          p.name.toLowerCase().includes(cleanQ) ||
          p.category.toLowerCase().includes(cleanQ)
      )
    : [];

  // Search DCs
  const matchedDCs = cleanQ
    ? deliveryChallans.filter(
        (dc) =>
          dc.dcNumber.toLowerCase().includes(cleanQ) ||
          dc.poNumber.toLowerCase().includes(cleanQ) ||
          dc.vehicleNumber.toLowerCase().includes(cleanQ) ||
          dc.transporterName.toLowerCase().includes(cleanQ) ||
          dc.ewayBillNumber.includes(cleanQ)
      )
    : [];

  const totalResults = matchedPOs.length + matchedClients.length + matchedProducts.length + matchedDCs.length;

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-start justify-center pt-16 px-4">
      <div 
        className="w-full max-w-2xl bg-slate-900 border border-slate-700/80 rounded-lg shadow-2xl overflow-hidden flex flex-col max-h-[75vh] animate-in fade-in zoom-in-95 duration-150"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Search Input Bar */}
        <div className="p-3 border-b border-slate-800 flex items-center gap-3 bg-slate-950/60">
          <Search className="w-5 h-5 text-emerald-400 shrink-0" />
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search PO #, Client, SKU, DC #, GSTIN, Phone, Vehicle number..."
            className="w-full bg-transparent text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none"
          />
          {query && (
            <button
              onClick={() => setQuery('')}
              className="p-1 text-slate-400 hover:text-slate-200"
            >
              <X className="w-4 h-4" />
            </button>
          )}
          <button
            onClick={onClose}
            className="px-2 py-1 text-xs font-mono bg-slate-800 text-slate-400 rounded hover:text-slate-200"
          >
            ESC
          </button>
        </div>

        {/* Search Results Area */}
        <div className="flex-1 overflow-y-auto p-3 divide-y divide-slate-800/60">
          {!query && (
            <div className="py-8 text-center text-xs text-slate-400">
              <p className="font-medium text-slate-400 mb-1">Universal Factory & Inventory Search</p>
              <p className="text-[11px] text-slate-400">
                Type a Purchase Order (PO-2026...), Client name (Mahyco, Kaveri), SKU (AG-GS-10), Vehicle (KA-04...), or GSTIN.
              </p>
            </div>
          )}

          {query && totalResults === 0 && (
            <div className="py-8 text-center text-xs text-slate-400">
              No matching records found for "{query}".
            </div>
          )}

          {/* PO Results */}
          {matchedPOs.length > 0 && (
            <div className="py-2">
              <div className="text-[10px] font-mono uppercase text-slate-400 px-2 mb-1 flex items-center gap-1.5">
                <FileText className="w-3 h-3 text-blue-400" />
                <span>Purchase Orders ({matchedPOs.length})</span>
              </div>
              {matchedPOs.map((po) => (
                <div
                  key={po.id}
                  onClick={() => {
                    setSelectedPOId(po.id);
                    setCurrentTab('purchase_orders');
                    onClose();
                  }}
                  className="px-3 py-2 rounded-md hover:bg-slate-800/80 cursor-pointer flex items-center justify-between text-xs group transition-colors"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-mono font-semibold text-emerald-400">{po.poNumber}</span>
                      <span className="text-slate-200 font-medium">{po.clientName}</span>
                      <span className="text-[10px] px-1.5 py-0.2 rounded bg-slate-800 border border-slate-700 text-slate-400 font-mono">
                        {po.status.replace(/_/g, ' ')}
                      </span>
                    </div>
                    <div className="text-[11px] text-slate-400 mt-0.5">
                      Items: {po.items.map((i) => `${i.quantity}x ${i.sku}`).join(', ')} · Total: ₹{(po.totalAmountINR / 100000).toFixed(2)}L
                    </div>
                  </div>
                  <ArrowRight className="w-4 h-4 text-slate-600 group-hover:text-emerald-400 group-hover:translate-x-0.5 transition-all" />
                </div>
              ))}
            </div>
          )}

          {/* Client Results */}
          {matchedClients.length > 0 && (
            <div className="py-2">
              <div className="text-[10px] font-mono uppercase text-slate-400 px-2 mb-1 flex items-center gap-1.5">
                <Users className="w-3 h-3 text-purple-400" />
                <span>Clients ({matchedClients.length})</span>
              </div>
              {matchedClients.map((client) => (
                <div
                  key={client.id}
                  onClick={() => {
                    setSelectedClientId(client.id);
                    setCurrentTab('clients');
                    onClose();
                  }}
                  className="px-3 py-2 rounded-md hover:bg-slate-800/80 cursor-pointer flex items-center justify-between text-xs group transition-colors"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-slate-200">{client.companyName}</span>
                      <span className="text-[10px] font-mono text-slate-400 bg-slate-800 px-1 rounded">
                        GSTIN: {client.gstin}
                      </span>
                    </div>
                    <div className="text-[11px] text-slate-400 mt-0.5">
                      Contact: {client.primaryContact.name} ({client.primaryContact.phone}) · State: {client.state}
                    </div>
                  </div>
                  <ArrowRight className="w-4 h-4 text-slate-600 group-hover:text-purple-400 group-hover:translate-x-0.5 transition-all" />
                </div>
              ))}
            </div>
          )}

          {/* Product Results */}
          {matchedProducts.length > 0 && (
            <div className="py-2">
              <div className="text-[10px] font-mono uppercase text-slate-400 px-2 mb-1 flex items-center gap-1.5">
                <Package className="w-3 h-3 text-amber-400" />
                <span>Machinery & Products ({matchedProducts.length})</span>
              </div>
              {matchedProducts.map((prod) => (
                <div
                  key={prod.id}
                  onClick={() => {
                    setSelectedProductId(prod.id);
                    setCurrentTab('products');
                    onClose();
                  }}
                  className="px-3 py-2 rounded-md hover:bg-slate-800/80 cursor-pointer flex items-center justify-between text-xs group transition-colors"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-mono font-semibold text-amber-400">{prod.sku}</span>
                      <span className="text-slate-200 font-medium">{prod.name}</span>
                    </div>
                    <div className="text-[11px] text-slate-400 mt-0.5">
                      Capacity: {prod.specs.capacity} · Base: ₹{(prod.basePriceINR / 100000).toFixed(2)}L
                    </div>
                  </div>
                  <ArrowRight className="w-4 h-4 text-slate-600 group-hover:text-amber-400 group-hover:translate-x-0.5 transition-all" />
                </div>
              ))}
            </div>
          )}

          {/* Delivery Challans */}
          {matchedDCs.length > 0 && (
            <div className="py-2">
              <div className="text-[10px] font-mono uppercase text-slate-400 px-2 mb-1 flex items-center gap-1.5">
                <Truck className="w-3 h-3 text-cyan-400" />
                <span>Delivery Challans & Transit ({matchedDCs.length})</span>
              </div>
              {matchedDCs.map((dc) => (
                <div
                  key={dc.id}
                  onClick={() => {
                    setSelectedDCId(dc.id);
                    setCurrentTab('delivery_challans');
                    onClose();
                  }}
                  className="px-3 py-2 rounded-md hover:bg-slate-800/80 cursor-pointer flex items-center justify-between text-xs group transition-colors"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-mono font-semibold text-cyan-400">{dc.dcNumber}</span>
                      <span className="text-slate-200 font-medium">{dc.clientName}</span>
                      <span className="text-[10px] font-mono text-slate-400 bg-slate-800 px-1 rounded">
                        Vehicle: {dc.vehicleNumber}
                      </span>
                    </div>
                    <div className="text-[11px] text-slate-400 mt-0.5">
                      PO: {dc.poNumber} · Status: {dc.status} · E-Way Bill: {dc.ewayBillNumber}
                    </div>
                  </div>
                  <ArrowRight className="w-4 h-4 text-slate-600 group-hover:text-cyan-400 group-hover:translate-x-0.5 transition-all" />
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-2.5 bg-slate-950 border-t border-slate-800 text-[11px] text-slate-400 flex items-center justify-between font-mono">
          <span>Results filtered by: {currentUser.role.replace(/_/g, ' ')}</span>
          <span>Press ESC to dismiss</span>
        </div>
      </div>
    </div>
  );
};
