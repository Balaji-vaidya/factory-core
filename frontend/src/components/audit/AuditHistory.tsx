import React, { useState } from 'react';
import { 
  History, 
  Search, 
  Filter, 
  Download, 
  ShieldCheck, 
  FileText, 
  Clock, 
  User 
} from 'lucide-react';
import { useApp } from '../../context/AppContext';

export const AuditHistory: React.FC = () => {
  const { auditRecords, showToast } = useApp();
  const [searchQuery, setSearchQuery] = useState('');
  const [resourceFilter, setResourceFilter] = useState('ALL');

  const filteredLogs = auditRecords.filter((log) => {
    const matchesSearch = 
      log.correlationId.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.userName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.action.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.resourceIdentifier.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (log.newValue && log.newValue.toLowerCase().includes(searchQuery.toLowerCase()));
    const matchesResource = resourceFilter === 'ALL' || log.resourceType === resourceFilter;
    return matchesSearch && matchesResource;
  });

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
              <History className="w-5 h-5 text-emerald-400" />
              <span>Immutable System Audit History & Compliance Trail</span>
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
              {auditRecords.length} Audited Events
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Section 20: Tamper-evident operational ledger recording PO approvals, stock reservations, DC generation, dispatches, and delivery sign-offs.
          </p>
        </div>

        <button
          onClick={() => showToast('Exporting cryptographic audit log (CSV/JSON)...', 'info')}
          className="px-3.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded text-xs transition-colors flex items-center gap-1.5"
        >
          <Download className="w-3.5 h-3.5" />
          <span>Export Audit Ledger</span>
        </button>
      </div>

      {/* Search & Filter */}
      <div className="bg-slate-900 border border-slate-800 p-3 rounded-lg flex items-center gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by Correlation ID, User, Action, PO #, DC #, Details..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none"
          />
        </div>

        <select
          value={resourceFilter}
          onChange={(e) => setResourceFilter(e.target.value)}
          className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded px-2.5 py-1.5 focus:outline-none"
        >
          <option value="ALL">All Resource Types</option>
          <option value="PURCHASE_ORDER">Purchase Orders</option>
          <option value="INVENTORY">Inventory</option>
          <option value="DELIVERY_CHALLAN">Delivery Challans</option>
          <option value="DELIVERY">Deliveries</option>
          <option value="SYSTEM">System & Auth</option>
        </select>
      </div>

      {/* Audit Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden shadow-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs font-mono">
            <thead className="bg-slate-950 text-slate-400 text-[10px] uppercase border-b border-slate-800">
              <tr>
                <th className="p-3">Correlation ID</th>
                <th className="p-3">Timestamp</th>
                <th className="p-3">Actor / User</th>
                <th className="p-3">Role</th>
                <th className="p-3">Action</th>
                <th className="p-3">Resource Target</th>
                <th className="p-3">Changes & Event Payload</th>
                <th className="p-3">Workstation IP</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {filteredLogs.map((log) => (
                <tr key={log.id} className="hover:bg-slate-850/50">
                  <td className="p-3 font-bold text-emerald-400 text-[11px]">{log.correlationId}</td>
                  <td className="p-3 text-slate-400 text-[11px]">{log.timestamp}</td>
                  <td className="p-3 font-sans font-semibold text-slate-200">{log.userName}</td>
                  <td className="p-3">
                    <span className="px-1.5 py-0.2 rounded bg-slate-800 text-slate-300 text-[10px] border border-slate-700">
                      {log.userRole}
                    </span>
                  </td>
                  <td className="p-3 text-blue-400 font-bold text-[11px]">{log.action}</td>
                  <td className="p-3 text-slate-100 font-bold">{log.resourceIdentifier}</td>
                  <td className="p-3 font-sans text-slate-300 text-[11px] max-w-sm truncate">
                    {log.newValue}
                  </td>
                  <td className="p-3 text-slate-400 text-[10px]">{log.ipAddress}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
