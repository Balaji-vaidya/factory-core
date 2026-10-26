import React from 'react';
import { 
  Boxes, 
  Truck, 
  Send, 
  AlertOctagon, 
  CheckSquare, 
  PlusCircle, 
  ArrowRight,
  ClipboardList,
  History,
  CheckCircle2
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { Badge } from '../common/Badge';

export const GodownDashboard: React.FC = () => {
  const { 
    inventory, 
    shortages, 
    deliveryChallans, 
    deliveries, 
    setCurrentTab, 
    stockLedger,
    showToast
  } = useApp();

  const totalOnHand = inventory.reduce((sum, item) => sum + item.onHand, 0);
  const totalReserved = inventory.reduce((sum, item) => sum + item.reserved, 0);
  const totalAvailable = inventory.reduce((sum, item) => sum + Math.max(0, item.onHand - item.reserved), 0);
  const openShortages = shortages.filter((s) => s.status !== 'RESOLVED');
  const pendingDispatch = deliveryChallans.filter((d) => d.status === 'PENDING_APPROVAL');
  const inTransitDCs = deliveryChallans.filter((d) => d.status === 'DISPATCHED');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-h2 font-bold tracking-tight text-slate-100">
              Godown #1 Operations & Warehouse Yard Console
            </h1>
            <span className="text-label font-mono px-2 py-0.5 rounded-md bg-blue-950/80 text-[#2563EB] border border-blue-800/50">
              JALNA INDUSTRIAL YARD
            </span>
          </div>
          <p className="text-body-medium text-slate-400 mt-1">
            Physical stock monitoring, line reservation allocations, delivery challan generation, and vehicle loading dock.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            onClick={() => setCurrentTab('delivery_challans')}
            className="px-3.5 py-2 bg-[#2563EB] hover:bg-[#1D4ED8] text-white rounded-md text-label font-semibold transition-colors flex items-center gap-1.5 shadow-xs"
          >
            <Truck className="w-3.5 h-3.5" />
            <span>+ Create Delivery Challan</span>
          </button>
        </div>
      </div>

      {/* Warehouse Yard Counters */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Physical On-Hand</span>
            <Boxes className="w-4 h-4 text-blue-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {totalOnHand} <span className="text-xs font-normal text-slate-400">Machines</span>
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">Godown bays #1 through #6</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Hard Reserved (Locked)</span>
            <ClipboardList className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-purple-300 mt-1 tabular-nums">
            {totalReserved} <span className="text-xs font-normal text-slate-400">Units</span>
          </div>
          <div className="text-[11px] text-purple-400/80 mt-0.5">Allocated to approved POs</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Free Stock Available</span>
            <CheckSquare className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-emerald-300 mt-1 tabular-nums">
            {totalAvailable} <span className="text-xs font-normal text-slate-400">Units</span>
          </div>
          <div className="text-[11px] text-emerald-400/80 mt-0.5">Ready for new PO commitments</div>
        </div>

        <div className={`p-4 rounded-lg border ${openShortages.length > 0 ? 'bg-rose-950/20 border-rose-800/60' : 'bg-slate-900 border-slate-800'}`}>
          <div className="flex items-center justify-between text-xs text-rose-300 font-medium">
            <span>Active Shortages</span>
            <AlertOctagon className="w-4 h-4 text-rose-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-rose-300 mt-1 tabular-nums">
            {openShortages.length}
          </div>
          <div className="text-[11px] text-rose-400/80 mt-0.5">Awaiting plant assembly lot</div>
        </div>
      </div>

      {/* Two Column Layout: Current Physical Stock & Pending Loading Dock Dispatches */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left: Inventory Breakdown by SKU */}
        <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-lg p-4">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
              <Boxes className="w-3.5 h-3.5 text-blue-400" />
              <span>Current Machinery Inventory (Godown #1)</span>
            </h2>
            <button
              onClick={() => setCurrentTab('inventory')}
              className="text-[11px] text-emerald-400 hover:underline"
            >
              Full Inventory Grid →
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
                <tr>
                  <th className="p-2.5">SKU / Model</th>
                  <th className="p-2.5 text-right">On Hand</th>
                  <th className="p-2.5 text-right">Reserved</th>
                  <th className="p-2.5 text-right">Available</th>
                  <th className="p-2.5">Status</th>
                  <th className="p-2.5">Location</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/80">
                {inventory.map((inv) => {
                  const avail = Math.max(0, inv.onHand - inv.reserved);
                  return (
                    <tr key={inv.productId} className="hover:bg-slate-850/50">
                      <td className="p-2.5">
                        <div className="font-mono font-semibold text-slate-100">{inv.sku}</div>
                        <div className="text-[11px] text-slate-400 truncate max-w-xs">{inv.productName}</div>
                      </td>
                      <td className="p-2.5 text-right font-mono font-bold text-slate-200">{inv.onHand}</td>
                      <td className="p-2.5 text-right font-mono text-purple-400">{inv.reserved}</td>
                      <td className="p-2.5 text-right font-mono font-bold text-emerald-400">{avail}</td>
                      <td className="p-2.5"><Badge status={inv.status} /></td>
                      <td className="p-2.5 font-mono text-slate-400 text-[11px]">{inv.storageLocation || inv.warehouseLocation}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>

        {/* Right: Pending Loading Dock & In-Transit DCs */}
        <div className="lg:col-span-5 space-y-6">
          {/* Pending Dispatches */}
          <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
                <Send className="w-3.5 h-3.5 text-amber-400" />
                <span>Pending Dispatch Approvals ({pendingDispatch.length})</span>
              </h2>
              <button
                onClick={() => setCurrentTab('dispatch')}
                className="text-[11px] text-emerald-400 hover:underline"
              >
                Dispatch Queue →
              </button>
            </div>

            {pendingDispatch.length === 0 ? (
              <div className="py-6 text-center text-xs text-slate-400">
                No Delivery Challans awaiting dispatch approval. Loading bay is clear.
              </div>
            ) : (
              <div className="space-y-2.5">
                {pendingDispatch.map((dc) => (
                  <div key={dc.id} className="p-3 bg-slate-950 rounded border border-slate-800 text-xs">
                    <div className="flex items-center justify-between">
                      <span className="font-mono font-bold text-amber-400">{dc.dcNumber}</span>
                      <span className="font-mono text-[10px] text-slate-400">PO: {dc.poNumber}</span>
                    </div>
                    <div className="text-slate-200 font-medium mt-1">{dc.clientName}</div>
                    <div className="text-[11px] text-slate-400 mt-0.5">
                      Vehicle: <span className="font-mono text-slate-300">{dc.vehicleNumber}</span> ({dc.transporterName})
                    </div>
                    <div className="text-[10px] text-amber-400/90 mt-1 font-mono">
                      Quantity: {dc.totalQuantity} Units · Requires Owner Clearance
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Recent Stock Ledger Transactions */}
          <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
                <History className="w-3.5 h-3.5 text-emerald-400" />
                <span>Godown Stock Ledger</span>
              </h2>
              <button
                onClick={() => setCurrentTab('inventory')}
                className="text-[11px] text-emerald-400 hover:underline"
              >
                Audit Trail →
              </button>
            </div>

            <div className="space-y-2 text-xs">
              {stockLedger.slice(0, 4).map((entry) => (
                <div key={entry.id} className="p-2 bg-slate-950/80 rounded border border-slate-800/80 flex items-center justify-between">
                  <div>
                    <div className="flex items-center gap-1.5">
                      <span className="font-mono font-semibold text-slate-200">{entry.sku}</span>
                      <span className="text-[10px] font-mono text-emerald-400">
                        {entry.transactionType.replace(/_/g, ' ')}
                      </span>
                    </div>
                    <div className="text-[10px] text-slate-400 truncate max-w-[220px]">
                      {entry.remarks}
                    </div>
                  </div>
                  <div className="text-right">
                    <span className={`font-mono font-bold text-xs ${
                      entry.quantityChange > 0 
                        ? 'text-emerald-400' 
                        : (entry.quantityChange < 0 ? 'text-rose-400' : 'text-slate-400')
                    }`}>
                      {entry.quantityChange > 0 ? `+${entry.quantityChange}` : entry.quantityChange}
                    </span>
                    <div className="text-[9px] text-slate-400 font-mono">
                      Bal: {entry.balanceOnHand}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
