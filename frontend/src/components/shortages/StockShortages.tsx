import React, { useState } from 'react';
import { 
  AlertOctagon, 
  Search, 
  Filter, 
  ArrowRight, 
  CheckCircle2, 
  ArrowDownToLine, 
  FileText,
  Calendar,
  X
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { Badge } from '../common/Badge';

export const StockShortages: React.FC = () => {
  const { 
    shortages, 
    resolveShortageWithIncomingStock, 
    setSelectedPOId, 
    setCurrentTab,
    showToast 
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [resolvingShortageId, setResolvingShortageId] = useState<string | null>(null);
  const [resolveQty, setResolveQty] = useState<number>(1);

  const filteredShortages = shortages.filter((s) => {
    const matchesSearch = 
      s.poNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.sku.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.productName.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || s.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const openShortages = shortages.filter((s) => s.status === 'OPEN');
  const totalUnitsShort = shortages
    .filter((s) => s.status !== 'RESOLVED')
    .reduce((sum, s) => sum + s.shortageQuantity, 0);

  const handleResolveSubmit = (shortageId: string) => {
    resolveShortageWithIncomingStock(shortageId, resolveQty);
    setResolvingShortageId(null);
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
              <AlertOctagon className="w-5 h-5 text-rose-500" />
              <span>Stock Shortage Management</span>
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-rose-950/80 text-rose-400 border border-rose-800/50">
              {openShortages.length} Active Shortages ({totalUnitsShort} Units)
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Rule 4: Automatically logged whenever Owner approves a PO where required quantity exceeds free godown stock.
          </p>
        </div>
      </div>

      {/* Explanatory Rule Callout */}
      <div className="bg-slate-900 border border-slate-800 p-3.5 rounded-lg text-xs text-slate-300">
        <span className="font-semibold text-emerald-400">Automated Factory Allocation: </span>
        When production units arrive at Godown #1, click <strong className="text-slate-100">"Resolve with Inward Batch"</strong> to auto-allocate the received stock directly to the backordered PO.
      </div>

      {/* Search & Filter Bar */}
      <div className="bg-slate-900 border border-slate-800 p-3 rounded-lg flex items-center gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by PO number, SKU, machine model..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded px-2.5 py-1.5 focus:outline-none"
        >
          <option value="ALL">All Shortages</option>
          <option value="OPEN">Open</option>
          <option value="PARTIALLY_RESOLVED">Partially Resolved</option>
          <option value="RESOLVED">Resolved</option>
        </select>
      </div>

      {/* Shortages Grid / Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
              <tr>
                <th className="p-3">Shortage ID</th>
                <th className="p-3">Target PO #</th>
                <th className="p-3">Model / SKU</th>
                <th className="p-3 text-center">Required</th>
                <th className="p-3 text-center">Reserved</th>
                <th className="p-3 text-center">Shortage</th>
                <th className="p-3">Priority</th>
                <th className="p-3">Status</th>
                <th className="p-3">Delivery Target</th>
                <th className="p-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {filteredShortages.length === 0 ? (
                <tr>
                  <td colSpan={10} className="p-8 text-center text-xs text-slate-400">
                    No shortages found. All PO requirements are currently covered.
                  </td>
                </tr>
              ) : (
                filteredShortages.map((sh) => (
                  <tr key={sh.id} className="hover:bg-slate-850/50">
                    <td className="p-3 font-mono font-bold text-rose-400">{sh.id}</td>
                    <td className="p-3">
                      <button
                        onClick={() => {
                          setSelectedPOId(sh.poId);
                          setCurrentTab('purchase_orders');
                        }}
                        className="font-mono text-emerald-400 hover:underline flex items-center gap-1"
                      >
                        <FileText className="w-3.5 h-3.5" />
                        <span>{sh.poNumber}</span>
                      </button>
                    </td>
                    <td className="p-3">
                      <div className="font-mono font-semibold text-slate-100">{sh.sku}</div>
                      <div className="text-[11px] text-slate-400">{sh.productName}</div>
                    </td>
                    <td className="p-3 text-center font-mono font-bold text-slate-200">
                      {sh.requiredQuantity}
                    </td>
                    <td className="p-3 text-center font-mono font-semibold text-purple-400">
                      {sh.reservedQuantity}
                    </td>
                    <td className="p-3 text-center font-mono font-bold text-rose-400 bg-rose-950/20">
                      {sh.shortageQuantity}
                    </td>
                    <td className="p-3"><Badge status={sh.priority} type="priority" /></td>
                    <td className="p-3"><Badge status={sh.status} /></td>
                    <td className="p-3 font-mono text-slate-300 text-[11px]">
                      {sh.expectedDeliveryDate}
                    </td>
                    <td className="p-3 text-right">
                      {sh.status !== 'RESOLVED' ? (
                        <button
                          onClick={() => {
                            setResolvingShortageId(sh.id);
                            setResolveQty(sh.shortageQuantity);
                          }}
                          className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold transition-colors shadow-xs"
                        >
                          Resolve Batch
                        </button>
                      ) : (
                        <span className="text-[11px] text-slate-400 font-mono">
                          Resolved {sh.resolvedAt}
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Quick Resolve Modal */}
      {resolvingShortageId && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-sm w-full space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-2">
              <h3 className="text-sm font-bold text-slate-100 flex items-center gap-1.5">
                <ArrowDownToLine className="w-4 h-4 text-emerald-400" />
                <span>Receive Factory Lot & Fulfill Shortage</span>
              </h3>
              <button onClick={() => setResolvingShortageId(null)} className="text-slate-400 hover:text-slate-200">
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-xs text-slate-300">
              Specify the number of manufactured units received from factory assembly. This will increment Godown On-Hand stock and automatically lock the reservation for the waiting PO.
            </p>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Units Received from Factory
              </label>
              <input
                type="number"
                min="1"
                value={resolveQty}
                onChange={(e) => setResolveQty(parseInt(e.target.value) || 1)}
                className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono text-sm"
              />
            </div>

            <div className="flex justify-end gap-2 pt-2 border-t border-slate-800">
              <button
                onClick={() => setResolvingShortageId(null)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded text-xs"
              >
                Cancel
              </button>
              <button
                onClick={() => handleResolveSubmit(resolvingShortageId)}
                className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold"
              >
                Confirm Allocation
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
