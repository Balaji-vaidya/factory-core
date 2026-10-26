import React, { useState } from 'react';
import { 
  Send, 
  CheckCircle2, 
  XCircle, 
  Truck, 
  FileText, 
  ShieldCheck, 
  AlertTriangle,
  Building,
  UserCheck,
  Calendar,
  X
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { Badge } from '../common/Badge';

export const DispatchManagement: React.FC = () => {
  const { 
    deliveryChallans, 
    currentUser, 
    approveDispatch, 
    rejectDispatch, 
    setCurrentTab,
    setSelectedDCId,
    showToast 
  } = useApp();

  const [rejectReason, setRejectReason] = useState('');
  const [rejectingDCId, setRejectingDCId] = useState<string | null>(null);

  const pendingDispatches = deliveryChallans.filter((d) => d.status === 'PENDING_APPROVAL');
  const pastDispatches = deliveryChallans.filter((d) => d.status !== 'PENDING_APPROVAL');

  const isOwner = currentUser.role === 'OWNER';

  const handleApprove = (dcId: string) => {
    approveDispatch(dcId);
  };

  const handleReject = () => {
    if (!rejectingDCId) return;
    rejectDispatch(rejectingDCId, rejectReason || 'Dispatch authorization declined by Owner');
    setRejectingDCId(null);
    setRejectReason('');
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
              <Send className="w-5 h-5 text-emerald-400" />
              <span>Dispatch Approvals & Gate Pass Clearance</span>
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-amber-950/80 text-amber-300 border border-amber-800/50">
              {pendingDispatches.length} Pending Authorization
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Section 15 & Rule 12: Owner authorization triggers physical inventory deduction and creates irrevocable DC_DISPATCH ledger entry.
          </p>
        </div>

        <button
          onClick={() => setCurrentTab('delivery_challans')}
          className="px-3.5 py-1.5 bg-slate-800 hover:bg-slate-750 text-slate-200 border border-slate-700 rounded text-xs transition-colors flex items-center gap-1.5"
        >
          <Truck className="w-3.5 h-3.5" />
          <span>View All Delivery Challans</span>
        </button>
      </div>

      {/* Role Authority Callout */}
      {!isOwner && (
        <div className="bg-amber-950/30 border border-amber-800/50 p-3 rounded-lg flex items-center gap-2.5 text-xs text-amber-200">
          <ShieldCheck className="w-4 h-4 text-amber-400 shrink-0" />
          <span>
            Dispatch authorization requires Owner credentials (Suresh Patel). You are currently logged in as <strong className="text-amber-100">{currentUser.name}</strong> ({currentUser.role}). Use the Top Bar role switcher to test Owner authorization.
          </span>
        </div>
      )}

      {/* Pending Dispatches List */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden shadow-xs">
        <div className="p-4 border-b border-slate-800 flex items-center justify-between bg-slate-950/40">
          <h2 className="text-xs font-bold text-slate-200 uppercase tracking-wider font-mono flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-amber-400 animate-ping" />
            <span>Delivery Challans Awaiting Gate Clearance ({pendingDispatches.length})</span>
          </h2>
          <span className="text-[11px] text-slate-400 font-mono">
            Check vehicle and E-Way bill before clearance
          </span>
        </div>

        {pendingDispatches.length === 0 ? (
          <div className="p-10 text-center text-xs text-slate-400">
            No delivery challans currently awaiting dispatch approval. Factory loading bay is clear.
          </div>
        ) : (
          <div className="divide-y divide-slate-800">
            {pendingDispatches.map((dc) => (
              <div key={dc.id} className="p-4 hover:bg-slate-850/60 transition-colors flex flex-col lg:flex-row lg:items-center justify-between gap-4">
                <div className="space-y-1.5">
                  <div className="flex items-center gap-2.5">
                    <span className="font-mono font-bold text-cyan-400 text-sm">{dc.dcNumber}</span>
                    <Badge status={dc.status} />
                    <span className="text-xs font-mono text-emerald-400">PO: {dc.poNumber}</span>
                  </div>

                  <div className="text-xs font-semibold text-slate-100">
                    Consignee: {dc.clientName} ({dc.deliveryAddress.city}, {dc.deliveryAddress.state})
                  </div>

                  <div className="text-[11px] text-slate-400 font-mono flex flex-wrap items-center gap-x-4 gap-y-1">
                    <span>Vehicle: <strong className="text-slate-200">{dc.vehicleNumber}</strong></span>
                    <span>Transporter: <strong className="text-slate-200">{dc.transporterName}</strong></span>
                    <span>Driver: {dc.driverName} ({dc.driverPhone})</span>
                    <span>E-Way: <strong className="text-emerald-400">{dc.ewayBillNumber}</strong></span>
                  </div>

                  <div className="text-xs text-slate-300 font-mono pt-1">
                    Machinery Units to Dispatch: {dc.items.map(i => `${i.dispatchQuantity}x ${i.sku} (${i.productName})`).join(', ')}
                  </div>
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  <button
                    onClick={() => {
                      setSelectedDCId(dc.id);
                      setCurrentTab('delivery_challans');
                    }}
                    className="px-3 py-1.5 bg-slate-800 hover:bg-slate-750 text-slate-300 rounded text-xs transition-colors"
                  >
                    Inspect DC
                  </button>

                  <button
                    onClick={() => setRejectingDCId(dc.id)}
                    className="px-3 py-1.5 bg-rose-950/60 hover:bg-rose-900 text-rose-300 border border-rose-800/60 rounded text-xs transition-colors"
                  >
                    Reject
                  </button>

                  <button
                    onClick={() => handleApprove(dc.id)}
                    className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold flex items-center gap-1.5 shadow-xs transition-colors"
                  >
                    <CheckCircle2 className="w-4 h-4" />
                    <span>Authorize Dispatch & Deduct Stock</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Dispatched & In-Transit Records */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
        <h2 className="text-xs font-bold text-slate-200 uppercase tracking-wider mb-3">
          Recently Dispatched & In-Transit Consignments
        </h2>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
              <tr>
                <th className="p-2.5">DC Number</th>
                <th className="p-2.5">PO Number</th>
                <th className="p-2.5">Client</th>
                <th className="p-2.5">Vehicle</th>
                <th className="p-2.5">Transporter</th>
                <th className="p-2.5">Dispatch Date</th>
                <th className="p-2.5">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {pastDispatches.slice(0, 5).map((dc) => (
                <tr key={dc.id} className="hover:bg-slate-850/50">
                  <td className="p-2.5 font-mono font-bold text-cyan-400">{dc.dcNumber}</td>
                  <td className="p-2.5 font-mono text-emerald-400">{dc.poNumber}</td>
                  <td className="p-2.5 font-medium text-slate-200">{dc.clientName}</td>
                  <td className="p-2.5 font-mono text-slate-300">{dc.vehicleNumber}</td>
                  <td className="p-2.5 text-slate-300">{dc.transporterName}</td>
                  <td className="p-2.5 font-mono text-slate-400 text-[11px]">{dc.dispatchDate || dc.createdAt}</td>
                  <td className="p-2.5"><Badge status={dc.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Rejection Modal */}
      {rejectingDCId && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-sm w-full space-y-4 shadow-2xl">
            <h3 className="text-sm font-bold text-slate-100 flex items-center gap-1.5">
              <XCircle className="w-4 h-4 text-rose-400" />
              <span>Decline Dispatch Authorization</span>
            </h3>
            <p className="text-xs text-slate-400">
              State the reason for rejecting vehicle gate pass / loading clearance.
            </p>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="e.g. Carrier insurance certificate expired. Please assign alternate transporter."
              className="w-full h-20 bg-slate-950 border border-slate-800 rounded p-2 text-xs text-slate-100"
            />
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setRejectingDCId(null)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded text-xs"
              >
                Cancel
              </button>
              <button
                onClick={handleReject}
                className="px-3 py-1.5 bg-rose-600 hover:bg-rose-500 text-white rounded text-xs font-semibold"
              >
                Confirm Rejection
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
