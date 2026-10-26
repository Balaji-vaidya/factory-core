import React from 'react';
import { 
  Building2, 
  FileText, 
  Clock, 
  Truck, 
  CheckCircle2, 
  ShieldAlert, 
  ArrowRight,
  EyeOff,
  Package
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { Badge } from '../common/Badge';

export const OuterStateDashboard: React.FC = () => {
  const { 
    currentUser, 
    currentOffice, 
    purchaseOrders, 
    clients, 
    setSelectedPOId, 
    setCurrentTab 
  } = useApp();

  // Filter POs to this Outer State Office
  const regionalPOs = purchaseOrders.filter(
    (p) => p.officeId === currentUser.officeId || p.state === currentOffice.state
  );

  const pendingApproval = regionalPOs.filter((p) => p.status === 'PENDING_APPROVAL').length;
  const inTransit = regionalPOs.filter((p) => p.status === 'NOT_DELIVERED' || p.status === 'PARTIALLY_DELIVERED').length;
  const delivered = regionalPOs.filter((p) => p.status === 'DELIVERED').length;
  const regionalClients = clients.filter((c) => c.state === currentOffice.state);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-h2 font-bold tracking-tight text-slate-100">
              Regional State Operations Hub ({currentOffice.state} Territory)
            </h1>
            <span className="text-label font-mono px-2 py-0.5 rounded-md bg-blue-950/80 text-[#2563EB] border border-blue-800/50">
              OUTER STATE OFFICE
            </span>
          </div>
          <p className="text-body-medium text-slate-400 mt-1">
            Regional client accounts, PO submission, central godown reservation statuses, and delivery verification for {currentOffice.city}.
          </p>
        </div>

        <button
          onClick={() => setCurrentTab('purchase_orders')}
          className="px-3.5 py-2 bg-[#2563EB] hover:bg-[#1D4ED8] text-white rounded-md text-label font-semibold transition-colors shadow-xs"
        >
          + Create Regional Purchase Order
        </button>
      </div>

      {/* Strict Security Policy Notice */}
      <div className="bg-slate-900/90 border border-slate-800 p-4 rounded-lg flex items-start gap-3">
        <div className="p-1.5 rounded-md bg-slate-800 text-slate-400 shrink-0">
          <EyeOff className="w-4 h-4 text-[#D97706]" />
        </div>
        <div className="text-xs text-slate-300">
          <div className="font-semibold text-slate-100 flex items-center gap-2">
            <span>Restricted Stock Visibility Active</span>
            <span className="text-[10px] font-mono text-amber-400 bg-amber-950/60 px-1.5 py-0.2 rounded border border-amber-800/40">
              SECURITY PROTOCOL §8
            </span>
          </div>
          <p className="text-slate-400 mt-0.5 leading-relaxed">
            Per company privacy policy, physical godown inventory counts (On Hand, Quantity Reserved, and Central Ledger) are restricted. You have access to real-time reservation statuses (Stock Reserved, Partially Reserved, Insufficient Stock) for your territory's orders.
          </p>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Regional POs</span>
            <FileText className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {regionalPOs.length}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">{currentOffice.state} territory</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Awaiting Owner Approval</span>
            <Clock className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-amber-300 mt-1 tabular-nums">
            {pendingApproval}
          </div>
          <div className="text-[11px] text-amber-400/80 mt-0.5">Submitted to Suresh Patel</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Dispatched / In Transit</span>
            <Truck className="w-4 h-4 text-blue-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {inTransit}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">Dispatched from Peenya Godown</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Delivered & Verified</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-emerald-400 mt-1 tabular-nums">
            {delivered}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">POD accepted</div>
        </div>
      </div>

      {/* Regional POs List */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xs font-bold text-slate-200 uppercase tracking-wider">
            {currentOffice.state} Territory Purchase Orders
          </h2>
          <span className="text-[11px] text-slate-400 font-mono">
            Location: {currentOffice.name}
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
              <tr>
                <th className="p-2.5">PO Number</th>
                <th className="p-2.5">Client</th>
                <th className="p-2.5">Approval Status</th>
                <th className="p-2.5">Stock Reservation Status</th>
                <th className="p-2.5">Delivery Status</th>
                <th className="p-2.5">Expected Date</th>
                <th className="p-2.5">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {regionalPOs.length === 0 ? (
                <tr>
                  <td colSpan={7} className="p-6 text-center text-xs text-slate-400">
                    No purchase orders in this state territory yet. Click "+ Create Regional Purchase Order" above.
                  </td>
                </tr>
              ) : (
                regionalPOs.map((po) => (
                  <tr key={po.id} className="hover:bg-slate-850/50">
                    <td className="p-2.5 font-mono font-semibold text-emerald-400">{po.poNumber}</td>
                    <td className="p-2.5 font-medium text-slate-200">{po.clientName}</td>
                    <td className="p-2.5"><Badge status={po.status} /></td>
                    <td className="p-2.5">
                      {/* CRITICAL: ONLY STATUS BADGE, NO NUMBERS */}
                      <Badge status={po.reservationStatus} />
                    </td>
                    <td className="p-2.5"><Badge status={po.deliveryStatus} /></td>
                    <td className="p-2.5 font-mono text-slate-300">{po.expectedDeliveryDate}</td>
                    <td className="p-2.5">
                      <button
                        onClick={() => {
                          setSelectedPOId(po.id);
                          setCurrentTab('purchase_orders');
                        }}
                        className="text-emerald-400 hover:underline font-mono text-[11px]"
                      >
                        View PO →
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Regional Clients & Quick Action */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
            <Building2 className="w-3.5 h-3.5 text-purple-400" />
            <span>Assigned Regional Clients ({currentOffice.state})</span>
          </h2>
          <button
            onClick={() => setCurrentTab('clients')}
            className="text-[11px] text-emerald-400 hover:underline"
          >
            All Clients →
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {regionalClients.map((client) => (
            <div key={client.id} className="p-3 bg-slate-950 rounded border border-slate-800 text-xs">
              <div className="font-semibold text-slate-100">{client.companyName}</div>
              <div className="text-[11px] text-slate-400 font-mono mt-0.5">
                GSTIN: {client.gstin} · State: {client.state}
              </div>
              <div className="text-[11px] text-slate-300 mt-1">
                Contact: {client.primaryContact.name} ({client.primaryContact.phone})
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
