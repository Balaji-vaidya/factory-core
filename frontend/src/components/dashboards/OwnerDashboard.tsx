import React from 'react';
import { 
  FileText, 
  Clock, 
  CheckCircle, 
  Boxes, 
  AlertTriangle, 
  Truck, 
  TrendingUp, 
  ArrowUpRight, 
  AlertOctagon,
  Calendar,
  XCircle,
  Building,
  Package
} from 'lucide-react';
import { ResponsiveContainer, PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip } from 'recharts';
import { useApp } from '../../context/AppContext';
import { Badge } from '../common/Badge';

export const OwnerDashboard: React.FC = () => {
  const { 
    purchaseOrders, 
    inventory, 
    shortages, 
    deliveryChallans, 
    deliveries, 
    setSelectedPOId, 
    setCurrentTab,
    auditRecords
  } = useApp();

  // Calculate Metrics
  const activePOs = purchaseOrders.filter(p => !['DELIVERED', 'CANCELLED'].includes(p.status)).length;
  const pendingApprovals = purchaseOrders.filter(p => p.status === 'PENDING_APPROVAL').length;
  const approvedPOs = purchaseOrders.filter(p => p.status === 'APPROVED').length;
  const reservedPOs = purchaseOrders.filter(p => p.reservationStatus === 'STOCK_RESERVED').length;
  const soldNotDelivered = purchaseOrders.filter(p => ['APPROVED', 'STOCK_RESERVATION', 'NOT_DELIVERED'].includes(p.status)).length;
  const partiallyDelivered = purchaseOrders.filter(p => p.status === 'PARTIALLY_DELIVERED').length;
  const fullyDelivered = purchaseOrders.filter(p => p.status === 'DELIVERED').length;
  const cancelledPOs = purchaseOrders.filter(p => p.status === 'CANCELLED' || p.status === 'CANCEL_REQUESTED').length;
  const insufficientStockPOs = purchaseOrders.filter(p => p.reservationStatus === 'INSUFFICIENT_STOCK' || p.reservationStatus === 'PARTIALLY_RESERVED').length;
  const overduePOs = purchaseOrders.filter(p => p.priority === 'URGENT' && p.status !== 'DELIVERED').length;
  const openShortagesCount = shortages.filter(s => s.status === 'OPEN').length;

  // Chart data
  const statusPieData = [
    { name: 'Pending Approval', value: pendingApprovals || 1, color: '#D97706' }, // Warning
    { name: 'Approved / Reserved', value: (approvedPOs + reservedPOs) || 2, color: '#2563EB' }, // Primary / Info
    { name: 'Partial Delivery', value: partiallyDelivered || 1, color: '#3B82F6' }, // Primary 500
    { name: 'Completed', value: fullyDelivered || 1, color: '#15803D' }, // Success
    { name: 'Shortage / Insuff.', value: insufficientStockPOs || 1, color: '#DC2626' }, // Error
  ];

  const monthlyActivityData = [
    { month: 'Jun', pos: 8, delivered: 7 },
    { month: 'Jul', pos: 12, delivered: 10 },
    { month: 'Aug', pos: 15, delivered: 12 },
    { month: 'Sep (MTD)', pos: 18, delivered: 14 },
  ];

  const pendingApprovalList = purchaseOrders.filter(p => p.status === 'PENDING_APPROVAL' || p.status === 'CANCEL_REQUESTED');
  const recentDispatches = deliveryChallans.slice(0, 4);
  const recentDeliveries = deliveries.slice(0, 4);

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-h2 font-bold tracking-tight text-slate-100">
              Executive Management & Operations Dashboard
            </h1>
            <span className="text-label font-mono px-2 py-0.5 rounded-md bg-blue-950/80 text-[#2563EB] border border-blue-800/50">
              OWNER CONSOLE
            </span>
          </div>
          <p className="text-body-medium text-slate-400 mt-1">
            Indo American Equipment / Agrisep® seed processing machinery order pipelines, stock reservations, shortages and dispatches.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setCurrentTab('purchase_orders')}
            className="px-3 py-2 bg-[#2563EB] hover:bg-[#1D4ED8] text-white rounded-md text-label font-medium transition-colors flex items-center gap-1.5 shadow-xs"
          >
            <span>Review {pendingApprovals} Pending Approvals</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
        <div className="bg-slate-900 border border-slate-800 p-3 rounded-lg">
          <div className="text-[11px] text-slate-400 flex items-center justify-between">
            <span>Active POs</span>
            <FileText className="w-3.5 h-3.5 text-blue-400" />
          </div>
          <div className="text-xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {activePOs}
          </div>
          <div className="text-[10px] text-slate-400 mt-0.5">In fulfillment pipeline</div>
        </div>

        <div className={`p-3 rounded-lg border ${pendingApprovals > 0 ? 'bg-amber-950/20 border-amber-800/60' : 'bg-slate-900 border-slate-800'}`}>
          <div className="text-[11px] text-amber-300 flex items-center justify-between font-medium">
            <span>Pending Approvals</span>
            <Clock className="w-3.5 h-3.5 text-amber-400 animate-pulse" />
          </div>
          <div className="text-xl font-bold font-mono text-amber-300 mt-1 tabular-nums">
            {pendingApprovals}
          </div>
          <div className="text-[10px] text-amber-400/80 mt-0.5">Requires Owner action</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-3 rounded-lg">
          <div className="text-[11px] text-slate-400 flex items-center justify-between">
            <span>Reserved POs</span>
            <Boxes className="w-3.5 h-3.5 text-purple-400" />
          </div>
          <div className="text-xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {reservedPOs}
          </div>
          <div className="text-[10px] text-slate-400 mt-0.5">Godown inventory locked</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-3 rounded-lg">
          <div className="text-[11px] text-slate-400 flex items-center justify-between">
            <span>Sold / In Transit</span>
            <Truck className="w-3.5 h-3.5 text-indigo-400" />
          </div>
          <div className="text-xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {soldNotDelivered}
          </div>
          <div className="text-[10px] text-slate-400 mt-0.5">{partiallyDelivered} partial dispatches</div>
        </div>

        <div className={`p-3 rounded-lg border ${openShortagesCount > 0 ? 'bg-rose-950/20 border-rose-800/60' : 'bg-slate-900 border-slate-800'}`}>
          <div className="text-[11px] text-rose-300 flex items-center justify-between font-medium">
            <span>Stock Shortages</span>
            <AlertOctagon className="w-3.5 h-3.5 text-rose-400" />
          </div>
          <div className="text-xl font-bold font-mono text-rose-300 mt-1 tabular-nums">
            {openShortagesCount}
          </div>
          <div className="text-[10px] text-rose-400/80 mt-0.5">Pending factory build</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-3 rounded-lg">
          <div className="text-[11px] text-slate-400 flex items-center justify-between">
            <span>Completed POs</span>
            <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />
          </div>
          <div className="text-xl font-bold font-mono text-emerald-400 mt-1 tabular-nums">
            {fullyDelivered}
          </div>
          <div className="text-[10px] text-slate-400 mt-0.5">100% verified POD</div>
        </div>
      </div>

      {/* Main Dashboard Grid: Pending Approvals & Status Visuals */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left: Pending Owner Approvals (Primary Action Area) */}
        <div className="lg:col-span-8 space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden">
            <div className="p-4 border-b border-slate-800 flex items-center justify-between bg-slate-950/40">
              <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-amber-400" />
                <h2 className="text-sm font-bold text-slate-100">
                  Pending Owner Actions ({pendingApprovalList.length})
                </h2>
              </div>
              <span className="text-[11px] text-slate-400">
                Rule: PO does not affect stock before your approval
              </span>
            </div>

            {pendingApprovalList.length === 0 ? (
              <div className="p-8 text-center text-xs text-slate-400">
                All purchase orders are currently approved or processed. No pending approval actions.
              </div>
            ) : (
              <div className="divide-y divide-slate-800">
                {pendingApprovalList.map((po) => (
                  <div key={po.id} className="p-4 hover:bg-slate-850/60 transition-colors flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-mono font-bold text-emerald-400 text-sm">{po.poNumber}</span>
                        <Badge status={po.status} />
                        <Badge status={po.priority} type="priority" />
                      </div>

                      <div className="text-xs text-slate-200 font-medium mt-1">
                        {po.clientName}
                      </div>

                      <div className="text-[11px] text-slate-400 mt-0.5 flex flex-wrap items-center gap-x-3 gap-y-1 font-mono">
                        <span>Items: {po.items.map(i => `${i.quantity}x ${i.sku}`).join(', ')}</span>
                        <span>·</span>
                        <span>Amount: ₹{(po.totalAmountINR / 100000).toFixed(2)} Lakhs</span>
                        <span>·</span>
                        <span>Exp. Delivery: {po.expectedDeliveryDate}</span>
                      </div>

                      {po.notes && (
                        <div className="text-[11px] text-slate-400 italic mt-1 bg-slate-950/40 px-2 py-0.5 rounded border border-slate-800 inline-block">
                          Note: "{po.notes}"
                        </div>
                      )}
                    </div>

                    <div className="flex items-center gap-2 shrink-0">
                      <button
                        onClick={() => {
                          setSelectedPOId(po.id);
                          setCurrentTab('purchase_orders');
                        }}
                        className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold rounded transition-colors shadow-xs"
                      >
                        Review & Approve
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* PO Status Breakdown & Shortages Summary */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Active Shortages Card */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
                  <AlertOctagon className="w-3.5 h-3.5 text-rose-400" />
                  <span>Factory Stock Shortages</span>
                </h3>
                <button
                  onClick={() => setCurrentTab('stock_shortages')}
                  className="text-[11px] text-emerald-400 hover:underline"
                >
                  Manage All →
                </button>
              </div>

              {shortages.filter(s => s.status !== 'RESOLVED').length === 0 ? (
                <div className="py-6 text-center text-xs text-slate-400">
                  No active stock shortages. All reserved orders covered.
                </div>
              ) : (
                <div className="space-y-2.5">
                  {shortages.filter(s => s.status !== 'RESOLVED').map((sh) => (
                    <div key={sh.id} className="p-2.5 bg-slate-950 rounded border border-slate-800 text-xs">
                      <div className="flex items-center justify-between">
                        <span className="font-mono font-semibold text-rose-400">{sh.sku}</span>
                        <span className="text-[10px] font-mono text-rose-300 bg-rose-950/80 px-1.5 py-0.2 rounded border border-rose-800/60">
                          Shortage: {sh.shortageQuantity} units
                        </span>
                      </div>
                      <div className="text-slate-300 text-[11px] mt-1">
                        For PO: {sh.poNumber} (Needed: {sh.requiredQuantity}, Reserved: {sh.reservedQuantity})
                      </div>
                      <div className="text-[10px] text-slate-400 mt-1 line-clamp-1">
                        {sh.reason}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Overdue / High Priority Orders */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
                  <Calendar className="w-3.5 h-3.5 text-amber-400" />
                  <span>High Priority / Time Sensitive POs</span>
                </h3>
              </div>

              <div className="space-y-2">
                {purchaseOrders.filter(p => p.priority === 'URGENT' || p.priority === 'HIGH').slice(0, 3).map((po) => (
                  <div 
                    key={po.id}
                    onClick={() => { setSelectedPOId(po.id); setCurrentTab('purchase_orders'); }}
                    className="p-2.5 bg-slate-950 rounded border border-slate-800 hover:border-slate-700 cursor-pointer text-xs transition-colors"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-mono font-semibold text-slate-200">{po.poNumber}</span>
                      <Badge status={po.priority} type="priority" />
                    </div>
                    <div className="text-slate-300 text-[11px] mt-0.5 truncate">{po.clientName}</div>
                    <div className="text-[10px] text-slate-400 mt-1 flex justify-between font-mono">
                      <span>Delivery Target: {po.expectedDeliveryDate}</span>
                      <span className="text-emerald-400">₹{(po.totalAmountINR / 100000).toFixed(1)}L</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Right: Charts & Operational Activity */}
        <div className="lg:col-span-4 space-y-6">
          {/* Status Breakdown Pie */}
          <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
            <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider mb-2">
              Purchase Order Lifecycle Distribution
            </h3>
            <div className="h-44 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={statusPieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={45}
                    outerRadius={70}
                    paddingAngle={3}
                    dataKey="value"
                  >
                    {statusPieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#0F172A', borderColor: '#334155', fontSize: '11px', color: '#F8FAFC' }}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="grid grid-cols-2 gap-x-2 gap-y-1.5 mt-2 text-[11px] text-slate-300">
              {statusPieData.map((d) => (
                <div key={d.name} className="flex items-center gap-1.5">
                  <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: d.color }} />
                  <span className="truncate">{d.name} ({d.value})</span>
                </div>
              ))}
            </div>
          </div>

          {/* Recent Audit / Operational Activity */}
          <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider">
                Recent Audit Trail
              </h3>
              <button
                onClick={() => setCurrentTab('audit_history')}
                className="text-[11px] text-emerald-400 hover:underline"
              >
                Full Audit Log →
              </button>
            </div>

            <div className="space-y-3">
              {auditRecords.slice(0, 4).map((rec) => (
                <div key={rec.id} className="text-xs border-l-2 border-slate-700 pl-2.5 py-0.5">
                  <div className="flex items-center justify-between text-[10px] text-slate-400 font-mono">
                    <span className="font-semibold text-slate-300">{rec.userName}</span>
                    <span>{rec.timestamp.slice(11, 16)}</span>
                  </div>
                  <div className="text-emerald-400 text-[11px] font-mono mt-0.5">
                    {rec.action}
                  </div>
                  <div className="text-slate-400 text-[10px] truncate mt-0.5">
                    {rec.newValue}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Recent Dispatches & Deliveries Table */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
              <Truck className="w-3.5 h-3.5 text-blue-400" />
              <span>Recent Dispatches (Delivery Challans)</span>
            </h3>
            <button onClick={() => setCurrentTab('delivery_challans')} className="text-[11px] text-emerald-400 hover:underline">
              View All →
            </button>
          </div>

          <div className="divide-y divide-slate-800/80">
            {recentDispatches.map((dc) => (
              <div key={dc.id} className="py-2 text-xs flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-semibold text-cyan-400">{dc.dcNumber}</span>
                    <Badge status={dc.status} />
                  </div>
                  <div className="text-slate-300 text-[11px] mt-0.5">{dc.clientName}</div>
                  <div className="text-slate-400 text-[10px] font-mono mt-0.5">
                    Vehicle: {dc.vehicleNumber} · Transporter: {dc.transporterName}
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-mono font-semibold text-slate-200">
                    {dc.totalQuantity} Units
                  </span>
                  <div className="text-[10px] text-slate-400 font-mono mt-0.5">
                    E-Way: {dc.ewayBillNumber}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
              <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />
              <span>Recent Verified Deliveries (POD)</span>
            </h3>
            <button onClick={() => setCurrentTab('deliveries')} className="text-[11px] text-emerald-400 hover:underline">
              View All →
            </button>
          </div>

          <div className="divide-y divide-slate-800/80">
            {recentDeliveries.map((del) => (
              <div key={del.id} className="py-2 text-xs flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-semibold text-emerald-400">{del.dcNumber}</span>
                    <Badge status={del.status} />
                  </div>
                  <div className="text-slate-300 text-[11px] mt-0.5">{del.clientName}</div>
                  <div className="text-slate-400 text-[10px] mt-0.5">
                    Received by: {del.receivedBy} ({del.receiverContact})
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-mono font-semibold text-emerald-400">
                    {del.deliveredQuantity} Delivered
                  </span>
                  <div className="text-[10px] text-slate-400 font-mono mt-0.5">
                    Date: {del.deliveryDate}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
