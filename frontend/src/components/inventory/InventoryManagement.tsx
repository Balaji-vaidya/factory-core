import React, { useState } from 'react';
import { 
  Boxes, 
  Search, 
  Filter, 
  History, 
  PlusCircle, 
  AlertOctagon, 
  EyeOff, 
  CheckCircle2, 
  Warehouse, 
  ArrowDownToLine,
  SlidersHorizontal,
  X
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { Badge } from '../common/Badge';

export const InventoryManagement: React.FC = () => {
  const { 
    inventory, 
    stockLedger, 
    currentUser, 
    shortages, 
    adjustInventoryStock,
    resolveShortageWithIncomingStock,
    setCurrentTab,
    showToast
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [activeSubTab, setActiveSubTab] = useState<'inventory' | 'ledger'>('inventory');
  
  // Adjustment modal
  const [showAdjustModal, setShowAdjustModal] = useState(false);
  const [selectedProductId, setSelectedProductId] = useState(inventory[0]?.productId || '');
  const [adjustQty, setAdjustQty] = useState<number>(1);
  const [adjustRemarks, setAdjustRemarks] = useState('');

  // Receive Factory Lot modal (Resolve Shortage)
  const [showInwardModal, setShowInwardModal] = useState(false);
  const [selectedShortageId, setSelectedShortageId] = useState(shortages.find(s => s.status === 'OPEN')?.id || '');
  const [inwardQty, setInwardQty] = useState<number>(2);

  const isOuterState = currentUser.role === 'OUTER_STATE_OFFICE';

  const filteredInventory = inventory.filter((item) => {
    const loc = item.storageLocation || item.warehouseLocation || '';
    const matchesSearch = 
      item.sku.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.productName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      loc.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || item.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const totalOnHand = inventory.reduce((acc, it) => acc + it.onHand, 0);
  const totalReserved = inventory.reduce((acc, it) => acc + it.reserved, 0);
  const totalAvailable = inventory.reduce((acc, it) => acc + Math.max(0, it.onHand - it.reserved), 0);

  const handleAdjustSubmit = () => {
    if (!adjustRemarks.trim()) {
      showToast('Adjustment remarks are mandatory for audit compliance', 'error');
      return;
    }
    adjustInventoryStock(selectedProductId, adjustQty, adjustRemarks);
    setShowAdjustModal(false);
    setAdjustRemarks('');
  };

  const handleInwardSubmit = () => {
    if (!selectedShortageId) {
      showToast('Select a shortage to fulfill', 'error');
      return;
    }
    resolveShortageWithIncomingStock(selectedShortageId, inwardQty);
    setShowInwardModal(false);
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100">
              {isOuterState ? 'Machinery Availability Status' : 'Godown Inventory & Stock Ledger'}
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
              Peenya Yard Bay #1-#6
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            {isOuterState
              ? 'Real-time production & reservation statuses for seed processing machinery.'
              : 'Physical stock counts, hard-reserved allocations against approved POs, and dynamic shortage detection.'}
          </p>
        </div>

        {!isOuterState && currentUser.role !== 'MAIN_OFFICE' && (
          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowInwardModal(true)}
              className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1.5 shadow-xs"
            >
              <ArrowDownToLine className="w-3.5 h-3.5" />
              <span>Receive Factory Lot (GRN)</span>
            </button>
            <button
              onClick={() => setShowAdjustModal(true)}
              className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded text-xs transition-colors flex items-center gap-1.5"
            >
              <SlidersHorizontal className="w-3.5 h-3.5" />
              <span>Stock Adjustment</span>
            </button>
          </div>
        )}
      </div>

      {/* Security Banner if Outer State Office */}
      {isOuterState && (
        <div className="bg-purple-950/30 border border-purple-800/50 p-3.5 rounded-lg flex items-start gap-3">
          <EyeOff className="w-4 h-4 text-purple-400 shrink-0 mt-0.5" />
          <div className="text-xs text-purple-200">
            <span className="font-semibold text-purple-100">Restricted Inventory View (§8): </span>
            Numerical warehouse quantities (On Hand, Reserved, Available) and the Stock Ledger are masked for Outer State Regional Offices. You can view real-time reservation and production readiness statuses to guide client commitments.
          </div>
        </div>
      )}

      {/* KPI Counters (Only shown to authorized roles per Rule §8!) */}
      {!isOuterState && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div className="bg-slate-900 border border-slate-800 p-3.5 rounded-lg">
            <div className="text-xs text-slate-400">Total Physical On-Hand</div>
            <div className="text-2xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
              {totalOnHand} <span className="text-xs font-normal text-slate-400">Machines</span>
            </div>
            <div className="text-[10px] text-slate-400 mt-0.5">Physical floor count</div>
          </div>

          <div className="bg-slate-900 border border-slate-800 p-3.5 rounded-lg">
            <div className="text-xs text-purple-300">Hard-Reserved (Locked)</div>
            <div className="text-2xl font-bold font-mono text-purple-300 mt-1 tabular-nums">
              {totalReserved} <span className="text-xs font-normal text-slate-400">Units</span>
            </div>
            <div className="text-[10px] text-purple-400/80 mt-0.5">Committed to approved POs</div>
          </div>

          <div className="bg-slate-900 border border-slate-800 p-3.5 rounded-lg">
            <div className="text-xs text-emerald-300">Free Stock Available</div>
            <div className="text-2xl font-bold font-mono text-emerald-300 mt-1 tabular-nums">
              {totalAvailable} <span className="text-xs font-normal text-slate-400">Units</span>
            </div>
            <div className="text-[10px] text-emerald-400/80 mt-0.5">Formula: On Hand - Reserved</div>
          </div>

          <div className="bg-slate-900 border border-slate-800 p-3.5 rounded-lg">
            <div className="text-xs text-rose-300">Active Stock Shortages</div>
            <div className="text-2xl font-bold font-mono text-rose-300 mt-1 tabular-nums">
              {shortages.filter(s => s.status === 'OPEN').length}
            </div>
            <div className="text-[10px] text-rose-400/80 mt-0.5">Requires factory assembly</div>
          </div>
        </div>
      )}

      {/* Sub Tabs (Inventory vs Ledger) */}
      {!isOuterState && (
        <div className="flex border-b border-slate-800 text-xs">
          <button
            onClick={() => setActiveSubTab('inventory')}
            className={`px-4 py-2 font-medium border-b-2 transition-colors ${
              activeSubTab === 'inventory'
                ? 'border-emerald-500 text-emerald-400 bg-slate-900/60'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            Inventory Stock Balances ({inventory.length} Models)
          </button>
          <button
            onClick={() => setActiveSubTab('ledger')}
            className={`px-4 py-2 font-medium border-b-2 transition-colors flex items-center gap-1.5 ${
              activeSubTab === 'ledger'
                ? 'border-emerald-500 text-emerald-400 bg-slate-900/60'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <History className="w-3.5 h-3.5" />
            <span>Stock Ledger Audit Log ({stockLedger.length})</span>
          </button>
        </div>
      )}

      {/* Main Table Area */}
      {activeSubTab === 'inventory' || isOuterState ? (
        <div className="space-y-3">
          {/* Filter Bar */}
          <div className="bg-slate-900 border border-slate-800 p-3 rounded-lg flex items-center gap-3">
            <div className="relative flex-1">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Filter by SKU (AG-GS-10), machinery name, location..."
                className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none"
              />
            </div>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded px-2.5 py-1.5 focus:outline-none"
            >
              <option value="ALL">All Statuses</option>
              <option value="AVAILABLE">Available</option>
              <option value="RESERVED">Reserved</option>
              <option value="LOW_STOCK">Low Stock</option>
              <option value="OUT_OF_STOCK">Out of Stock</option>
              <option value="INSUFFICIENT">Insufficient</option>
            </select>
          </div>

          {/* Inventory Table */}
          <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
                  <tr>
                    <th className="p-3">Model / SKU</th>
                    <th className="p-3">Machinery Name</th>
                    {/* CRITICAL §8 RULE: HIDE ALL NUMBER COLUMNS FROM OUTER STATE! */}
                    {!isOuterState ? (
                      <>
                        <th className="p-3 text-right">Physical On Hand</th>
                        <th className="p-3 text-right">Hard-Reserved</th>
                        <th className="p-3 text-right">Free Available</th>
                        <th className="p-3 text-right">Reorder Pt.</th>
                      </>
                    ) : (
                      <th className="p-3">Factory Availability</th>
                    )}
                    <th className="p-3">Stock Status</th>
                    <th className="p-3">Godown Bay</th>
                    <th className="p-3">Last Activity</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/80">
                  {filteredInventory.map((item) => {
                    const avail = Math.max(0, item.onHand - item.reserved);
                    return (
                      <tr key={item.productId} className="hover:bg-slate-850/50">
                        <td className="p-3 font-mono font-bold text-emerald-400">
                          {item.sku}
                        </td>
                        <td className="p-3">
                          <div className="font-semibold text-slate-100">{item.productName}</div>
                        </td>

                        {!isOuterState ? (
                          <>
                            <td className="p-3 text-right font-mono font-bold text-slate-200">
                              {item.onHand} {item.unit}
                            </td>
                            <td className="p-3 text-right font-mono font-semibold text-purple-400">
                              {item.reserved}
                            </td>
                            <td className="p-3 text-right font-mono font-bold text-emerald-400">
                              {avail}
                            </td>
                            <td className="p-3 text-right font-mono text-slate-400">
                              {item.reorderPoint}
                            </td>
                          </>
                        ) : (
                          <td className="p-3">
                            <span className="font-mono text-slate-300">
                              {avail > 0 ? 'Ready for Reservation' : 'Factory Production In-Progress'}
                            </span>
                          </td>
                        )}

                        <td className="p-3">
                          <Badge status={item.status} />
                        </td>
                        <td className="p-3 font-mono text-slate-300 text-[11px]">
                          {item.storageLocation || item.warehouseLocation}
                        </td>
                        <td className="p-3 font-mono text-slate-400 text-[11px]">
                          {item.lastUpdated}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      ) : (
        /* Stock Ledger View */
        <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden">
          <div className="p-3 border-b border-slate-800 flex items-center justify-between bg-slate-950/40">
            <span className="text-xs font-bold text-slate-200 uppercase tracking-wider font-mono">
              Stock Ledger & Reservation Audit Log
            </span>
            <span className="text-[11px] text-slate-400 font-mono">
              All physical moves and reservation transactions
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
                <tr>
                  <th className="p-2.5">Timestamp</th>
                  <th className="p-2.5">SKU</th>
                  <th className="p-2.5">Transaction Type</th>
                  <th className="p-2.5 text-right">Qty Change</th>
                  <th className="p-2.5 text-right">Balance On Hand</th>
                  <th className="p-2.5">Reference / PO / DC</th>
                  <th className="p-2.5">Authorized User</th>
                  <th className="p-2.5">Remarks</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/80 font-mono">
                {stockLedger.map((entry) => (
                  <tr key={entry.id} className="hover:bg-slate-850/50">
                    <td className="p-2.5 text-[11px] text-slate-400">{entry.timestamp}</td>
                    <td className="p-2.5 font-bold text-slate-200">{entry.sku}</td>
                    <td className="p-2.5 text-[11px]">
                      <span className="px-1.5 py-0.5 rounded bg-slate-800 border border-slate-700 text-emerald-400">
                        {entry.transactionType.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className={`p-2.5 text-right font-bold ${
                      entry.quantityChange > 0 
                        ? 'text-emerald-400' 
                        : (entry.quantityChange < 0 ? 'text-rose-400' : 'text-slate-400')
                    }`}>
                      {entry.quantityChange > 0 ? `+${entry.quantityChange}` : entry.quantityChange}
                    </td>
                    <td className="p-2.5 text-right font-bold text-slate-100">{entry.balanceOnHand}</td>
                    <td className="p-2.5 text-emerald-400 text-[11px]">
                      {entry.poNumber || entry.dcNumber || entry.referenceId}
                    </td>
                    <td className="p-2.5 text-slate-300 font-sans text-[11px]">{entry.userName}</td>
                    <td className="p-2.5 text-slate-400 font-sans text-[11px] max-w-xs truncate">
                      {entry.remarks}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Adjust Stock Modal */}
      {showAdjustModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-md w-full space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-2">
              <h3 className="text-sm font-bold text-slate-100">Manual Inventory Adjustment</h3>
              <button onClick={() => setShowAdjustModal(false)} className="text-slate-400 hover:text-slate-200">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">Product</label>
                <select
                  value={selectedProductId}
                  onChange={(e) => setSelectedProductId(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                >
                  {inventory.map((inv) => (
                    <option key={inv.productId} value={inv.productId}>
                      {inv.sku} - {inv.productName} (Current: {inv.onHand})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Quantity Adjustment (+ or -)</label>
                <input
                  type="number"
                  value={adjustQty}
                  onChange={(e) => setAdjustQty(parseInt(e.target.value) || 0)}
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono"
                />
                <span className="text-[10px] text-slate-400">Use positive number to add stock, negative to subtract.</span>
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Mandatory Audit Remarks *</label>
                <textarea
                  value={adjustRemarks}
                  onChange={(e) => setAdjustRemarks(e.target.value)}
                  placeholder="e.g. Physical godown audit discrepancy adjustment approved by factory manager..."
                  className="w-full h-20 bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-2 border-t border-slate-800">
              <button
                onClick={() => setShowAdjustModal(false)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded text-xs"
              >
                Cancel
              </button>
              <button
                onClick={handleAdjustSubmit}
                className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold"
              >
                Apply Adjustment
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Inward Factory Batch Modal */}
      {showInwardModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-md w-full space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-2">
              <h3 className="text-sm font-bold text-slate-100 flex items-center gap-1.5">
                <ArrowDownToLine className="w-4 h-4 text-emerald-400" />
                <span>Receive Factory Production Lot (GRN)</span>
              </h3>
              <button onClick={() => setShowInwardModal(false)} className="text-slate-400 hover:text-slate-200">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <p className="text-slate-400 text-[11px]">
                Simulate arrival of newly fabricated seed machines from the factory assembly floor. Incoming stock will automatically allocate and resolve shortages on approved POs.
              </p>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Target Shortage Order</label>
                <select
                  value={selectedShortageId}
                  onChange={(e) => setSelectedShortageId(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                >
                  {shortages.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.sku} - For {s.poNumber} (Shortage: {s.shortageQuantity} units)
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Lot Quantity Received</label>
                <input
                  type="number"
                  min="1"
                  value={inwardQty}
                  onChange={(e) => setInwardQty(parseInt(e.target.value) || 1)}
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-2 border-t border-slate-800">
              <button
                onClick={() => setShowInwardModal(false)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded text-xs"
              >
                Cancel
              </button>
              <button
                onClick={handleInwardSubmit}
                className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold"
              >
                Inward & Auto-Reserve
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
