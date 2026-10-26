import React from 'react';
import { 
  FilePlus, 
  MessageSquare, 
  Users, 
  Clock, 
  ArrowRight, 
  CheckCircle2, 
  AlertTriangle,
  Building,
  PhoneCall,
  Mail,
  Send
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { Badge } from '../common/Badge';

export const MainOfficeDashboard: React.FC = () => {
  const { 
    purchaseOrders, 
    clients, 
    websiteEnquiries, 
    setCurrentTab, 
    setSelectedPOId,
    updateEnquiryStatus,
    showToast
  } = useApp();

  const assignedPOs = purchaseOrders; // Main office oversees all company accounts
  const pendingApprovalPOs = purchaseOrders.filter((p) => p.status === 'PENDING_APPROVAL');
  const activeEnquiries = websiteEnquiries.filter((e) => e.status !== 'CLOSED');
  const newEnquiries = websiteEnquiries.filter((e) => e.status === 'NEW');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-h2 font-bold tracking-tight text-slate-100">
              Main Office Operations & Sales Console
            </h1>
            <span className="text-label font-mono px-2 py-0.5 rounded-md bg-blue-950/80 text-[#2563EB] border border-blue-800/50">
              JALNA HQ
            </span>
          </div>
          <p className="text-body-medium text-slate-400 mt-1">
            Order management, client communications, incoming web machinery enquiries, and delivery tracking.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setCurrentTab('purchase_orders')}
            className="px-3.5 py-2 bg-[#2563EB] hover:bg-[#1D4ED8] text-white rounded-md text-label font-semibold transition-colors flex items-center gap-1.5 shadow-xs"
          >
            <FilePlus className="w-3.5 h-3.5" />
            <span>Create New Purchase Order</span>
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Managed POs</span>
            <Users className="w-4 h-4 text-blue-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {assignedPOs.length}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">{pendingApprovalPOs.length} awaiting owner approval</div>
        </div>

        <div className={`p-4 rounded-lg border ${newEnquiries.length > 0 ? 'bg-emerald-950/20 border-emerald-800/60' : 'bg-slate-900 border-slate-800'}`}>
          <div className="flex items-center justify-between text-xs text-emerald-300 font-medium">
            <span>New Web Enquiries</span>
            <MessageSquare className="w-4 h-4 text-emerald-400 animate-pulse" />
          </div>
          <div className="text-2xl font-bold font-mono text-emerald-300 mt-1 tabular-nums">
            {newEnquiries.length}
          </div>
          <div className="text-[11px] text-emerald-400/80 mt-0.5">Submitted via www.agrisep.com</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Active Clients</span>
            <Building className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {clients.length}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">Leading seed corporations</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Pending PO Submissions</span>
            <Clock className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {pendingApprovalPOs.length}
          </div>
          <div className="text-[11px] text-amber-400 mt-0.5">Awaiting Owner verification</div>
        </div>
      </div>

      {/* PROMINENT SECTION: Website Enquiries (Mandatory Requirement) */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden shadow-xs">
        <div className="p-4 border-b border-slate-800 flex items-center justify-between bg-slate-950/60">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
            <h2 className="text-sm font-bold text-slate-100 flex items-center gap-2">
              <MessageSquare className="w-4 h-4 text-emerald-400" />
              <span>Inbound Public Website Machinery Enquiries</span>
            </h2>
          </div>
          <button
            onClick={() => setCurrentTab('enquiries')}
            className="text-xs text-emerald-400 hover:underline flex items-center gap-1"
          >
            <span>Open Enquiries Pipeline ({websiteEnquiries.length})</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="divide-y divide-slate-800">
          {activeEnquiries.slice(0, 3).map((enq) => (
            <div key={enq.id} className="p-4 hover:bg-slate-850/60 transition-colors flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div>
                <div className="flex items-center gap-2.5">
                  <span className="font-mono font-semibold text-emerald-400 text-xs">{enq.id}</span>
                  <span className="font-bold text-slate-100 text-sm">{enq.name}</span>
                  {enq.company && (
                    <span className="text-xs text-slate-400">({enq.company})</span>
                  )}
                  <Badge status={enq.status} />
                </div>

                <div className="flex items-center gap-4 text-[11px] text-slate-400 mt-1 font-mono">
                  <span className="flex items-center gap-1">
                    <PhoneCall className="w-3 h-3 text-slate-400" />
                    {enq.phone}
                  </span>
                  <span className="flex items-center gap-1">
                    <Mail className="w-3 h-3 text-slate-400" />
                    {enq.email}
                  </span>
                  <span>Target: {enq.productName || 'Seed Processing Plants'}</span>
                </div>

                <p className="text-xs text-slate-300 mt-1.5 italic bg-slate-950/50 p-2 rounded border border-slate-800 max-w-3xl">
                  "{enq.message}"
                </p>
              </div>

              <div className="flex items-center gap-2 shrink-0">
                {enq.status === 'NEW' && (
                  <button
                    onClick={() => updateEnquiryStatus(enq.id, 'CONTACTED', 'Contacted via telephone from Main Office')}
                    className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded text-xs transition-colors"
                  >
                    Mark Contacted
                  </button>
                )}
                <button
                  onClick={() => {
                    setCurrentTab('purchase_orders');
                    showToast(`Prefilled client from enquiry ${enq.id}`, 'info');
                  }}
                  className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1.5 shadow-xs"
                >
                  <Send className="w-3 h-3" />
                  <span>Convert to PO</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* PO Status & Assigned Orders Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xs font-bold text-slate-200 uppercase tracking-wider">
            Assigned Purchase Orders & Fulfillment Status
          </h2>
          <button
            onClick={() => setCurrentTab('purchase_orders')}
            className="text-[11px] text-emerald-400 hover:underline"
          >
            All Purchase Orders →
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
              <tr>
                <th className="p-2.5">PO Number</th>
                <th className="p-2.5">Client</th>
                <th className="p-2.5">Status</th>
                <th className="p-2.5">Stock Reservation</th>
                <th className="p-2.5">Delivery Status</th>
                <th className="p-2.5">Amount (INR)</th>
                <th className="p-2.5">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {purchaseOrders.slice(0, 5).map((po) => (
                <tr key={po.id} className="hover:bg-slate-850/50">
                  <td className="p-2.5 font-mono font-semibold text-emerald-400">{po.poNumber}</td>
                  <td className="p-2.5 font-medium text-slate-200">{po.clientName}</td>
                  <td className="p-2.5"><Badge status={po.status} /></td>
                  <td className="p-2.5"><Badge status={po.reservationStatus} /></td>
                  <td className="p-2.5"><Badge status={po.deliveryStatus} /></td>
                  <td className="p-2.5 font-mono text-slate-300">₹{(po.totalAmountINR / 100000).toFixed(2)}L</td>
                  <td className="p-2.5">
                    <button
                      onClick={() => {
                        setSelectedPOId(po.id);
                        setCurrentTab('purchase_orders');
                      }}
                      className="text-emerald-400 hover:underline font-mono text-[11px]"
                    >
                      View Details →
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
