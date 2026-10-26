import React, { useState } from 'react';
import { 
  MessageSquare, 
  Search, 
  Filter, 
  PhoneCall, 
  Mail, 
  Building, 
  ArrowRight, 
  Send, 
  Clock, 
  CheckCircle2, 
  UserCheck,
  History,
  X
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { WebsiteEnquiry } from '../../types';
import { Badge } from '../common/Badge';

export const EnquiriesManagement: React.FC = () => {
  const { 
    websiteEnquiries, 
    updateEnquiryStatus, 
    setCurrentTab, 
    showToast 
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedEnquiryId, setSelectedEnquiryId] = useState<string>(websiteEnquiries[0]?.id || '');
  const [followupNote, setFollowupNote] = useState('');

  const selectedEnquiry = websiteEnquiries.find((e) => e.id === selectedEnquiryId) || websiteEnquiries[0];

  const filteredEnquiries = websiteEnquiries.filter((enq) => {
    const matchesSearch = 
      enq.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (enq.company && enq.company.toLowerCase().includes(searchQuery.toLowerCase())) ||
      enq.phone.includes(searchQuery) ||
      enq.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (enq.productName && enq.productName.toLowerCase().includes(searchQuery.toLowerCase()));
    const matchesStatus = statusFilter === 'ALL' || enq.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleStatusChange = (status: WebsiteEnquiry['status']) => {
    if (!selectedEnquiry) return;
    updateEnquiryStatus(selectedEnquiry.id, status, followupNote || `Status updated to ${status}`);
    setFollowupNote('');
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
              <MessageSquare className="w-5 h-5 text-emerald-400" />
              <span>Inbound Public Website Enquiries & Lead Pipeline</span>
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-emerald-950/80 text-emerald-400 border border-emerald-800/50">
              {websiteEnquiries.filter(e => e.status === 'NEW').length} New Leads
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Section 17: Seamlessly connected to www.agrisep.com. Enquiries submitted on the public website arrive here in real time for Main Office follow-up.
          </p>
        </div>
      </div>

      {/* Search & Filter */}
      <div className="bg-slate-900 border border-slate-800 p-3 rounded-lg flex items-center gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by customer name, company, phone, email, machine..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded px-2.5 py-1.5 focus:outline-none"
        >
          <option value="ALL">All Enquiries</option>
          <option value="NEW">New Inbound</option>
          <option value="CONTACTED">Contacted</option>
          <option value="FOLLOW_UP">Follow Up</option>
          <option value="CONVERTED">Converted to PO</option>
          <option value="CLOSED">Closed</option>
        </select>
      </div>

      {/* Two Column Layout: Enquiries List & Detail Workspace */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Left: Enquiries List */}
        <div className="lg:col-span-5 space-y-2.5">
          {filteredEnquiries.map((enq) => {
            const isSelected = selectedEnquiry?.id === enq.id;
            return (
              <div
                key={enq.id}
                onClick={() => setSelectedEnquiryId(enq.id)}
                className={`p-3.5 rounded-lg border cursor-pointer transition-all ${
                  isSelected
                    ? 'bg-slate-800/90 border-emerald-500/60 shadow-md ring-1 ring-emerald-500/30'
                    : 'bg-slate-900 border-slate-800 hover:bg-slate-850'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-mono font-bold text-emerald-400 text-xs">{enq.id}</span>
                      <Badge status={enq.status} />
                    </div>
                    <div className="font-bold text-slate-100 text-sm mt-1">{enq.name}</div>
                    {enq.company && (
                      <div className="text-[11px] text-slate-400">{enq.company}</div>
                    )}
                  </div>
                  <span className="text-[10px] text-slate-400 font-mono">{enq.createdAt.slice(0, 10)}</span>
                </div>

                <div className="text-[11px] text-slate-300 mt-2 font-mono flex items-center gap-3">
                  <span>Target: {enq.productName || 'Seed Processing Plants'}</span>
                  {enq.quantity && <span>({enq.quantity} Units)</span>}
                </div>

                <p className="text-[11px] text-slate-400 italic line-clamp-1 mt-1">
                  "{enq.message}"
                </p>
              </div>
            );
          })}
        </div>

        {/* Right: Enquiry Detail & Actions */}
        {selectedEnquiry && (
          <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-lg p-5 space-y-4">
            <div className="border-b border-slate-800 pb-3 flex items-start justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-mono font-bold text-emerald-400 text-sm">{selectedEnquiry.id}</span>
                  <Badge status={selectedEnquiry.status} />
                  <span className="text-xs text-slate-400 font-mono">State: {selectedEnquiry.state}</span>
                </div>
                <h2 className="text-base font-bold text-slate-100 mt-1">
                  {selectedEnquiry.name} {selectedEnquiry.company ? `(${selectedEnquiry.company})` : ''}
                </h2>
              </div>

              <button
                onClick={() => {
                  setCurrentTab('purchase_orders');
                  updateEnquiryStatus(selectedEnquiry.id, 'CONVERTED', 'Converted enquiry into active Purchase Order');
                  showToast(`Converting enquiry ${selectedEnquiry.id} to Purchase Order`, 'success');
                }}
                className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold flex items-center gap-1.5 shadow-xs"
              >
                <Send className="w-3.5 h-3.5" />
                <span>Convert to Purchase Order</span>
              </button>
            </div>

            {/* Direct Contact Information */}
            <div className="grid grid-cols-2 gap-3 text-xs">
              <div className="p-3 bg-slate-950 rounded border border-slate-800">
                <span className="text-[10px] text-slate-400 uppercase font-mono block mb-1">Telephone</span>
                <span className="font-mono font-bold text-slate-200 text-sm">{selectedEnquiry.phone}</span>
              </div>
              <div className="p-3 bg-slate-950 rounded border border-slate-800">
                <span className="text-[10px] text-slate-400 uppercase font-mono block mb-1">Email Address</span>
                <span className="font-mono text-slate-200">{selectedEnquiry.email}</span>
              </div>
            </div>

            {/* Machinery Requirement Message */}
            <div className="p-3.5 bg-slate-950 rounded border border-slate-800 text-xs">
              <div className="text-[10px] text-slate-400 font-mono uppercase mb-1">
                Requested Machinery / Project Scope:
              </div>
              <div className="font-semibold text-emerald-400 text-sm mb-1">
                {selectedEnquiry.productName || 'Complete Turnkey Seed Processing Plant'} 
                {selectedEnquiry.quantity ? ` (Quantity: ${selectedEnquiry.quantity} Units)` : ''}
              </div>
              <p className="text-slate-300 leading-relaxed text-xs">
                "{selectedEnquiry.message}"
              </p>
            </div>

            {/* Status Quick Switcher & Note */}
            <div className="p-3.5 bg-slate-950 rounded border border-slate-800 space-y-2">
              <div className="text-xs font-semibold text-slate-200">Update Enquiry Status</div>
              <div className="flex flex-wrap gap-2">
                {(['NEW', 'CONTACTED', 'FOLLOW_UP', 'CLOSED'] as WebsiteEnquiry['status'][]).map((st) => (
                  <button
                    key={st}
                    onClick={() => handleStatusChange(st)}
                    className={`px-3 py-1 rounded text-xs font-medium transition-colors ${
                      selectedEnquiry.status === st
                        ? 'bg-emerald-600 text-white font-bold'
                        : 'bg-slate-800 hover:bg-slate-700 text-slate-300'
                    }`}
                  >
                    {st.replace(/_/g, ' ')}
                  </button>
                ))}
              </div>

              <div className="flex gap-2 pt-2">
                <input
                  type="text"
                  value={followupNote}
                  onChange={(e) => setFollowupNote(e.target.value)}
                  placeholder="Add communication note (e.g. Sent proforma quotation for 10 TPH plant)..."
                  className="flex-1 bg-slate-900 border border-slate-800 rounded px-2.5 py-1 text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none"
                />
                <button
                  onClick={() => handleStatusChange(selectedEnquiry.status)}
                  className="px-3 py-1 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded text-xs shrink-0"
                >
                  Log Note
                </button>
              </div>
            </div>

            {/* Interaction Timeline */}
            <div>
              <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider font-mono mb-2 flex items-center gap-1.5">
                <History className="w-3.5 h-3.5 text-emerald-400" />
                <span>Enquiry Activity Timeline</span>
              </h3>
              <div className="space-y-2">
                {selectedEnquiry.timeline.map((item, idx) => (
                  <div key={idx} className="text-xs border-l-2 border-slate-700 pl-3 py-0.5">
                    <div className="flex items-center justify-between text-[10px] text-slate-400 font-mono">
                      <span className="font-bold text-slate-200">{item.action}</span>
                      <span>{item.timestamp}</span>
                    </div>
                    <div className="text-slate-400 text-[11px] mt-0.5">
                      By: {item.by} · {item.notes}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
