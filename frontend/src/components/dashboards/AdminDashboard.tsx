import React from 'react';
import { 
  Users, 
  Building2, 
  Package, 
  ShieldAlert, 
  Activity, 
  History, 
  CheckCircle2, 
  ArrowRight,
  Server,
  KeyRound
} from 'lucide-react';
import { useApp } from '../../context/AppContext';

export const AdminDashboard: React.FC = () => {
  const { 
    users, 
    offices, 
    products, 
    inventory, 
    auditRecords, 
    purchaseOrders, 
    setCurrentTab,
    showToast
  } = useApp();

  const activeUsers = users.filter((u) => u.status === 'ACTIVE').length;
  const activeOffices = offices.filter((o) => o.status === 'ACTIVE').length;
  const activeProducts = products.filter((p) => p.internalStatus === 'ACTIVE').length;
  const publicWebProducts = products.filter((p) => p.websiteVisibility === 'PUBLIC').length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-h2 font-bold tracking-tight text-slate-100">
              System Administration & Operational Governance
            </h1>
            <span className="text-label font-mono px-2 py-0.5 rounded-md bg-blue-950/80 text-[#2563EB] border border-blue-800/50">
              ADMINISTRATOR CONSOLE
            </span>
          </div>
          <p className="text-body-medium text-slate-400 mt-1">
            User access control, regional office assignments, master product catalogue, and system audit history.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setCurrentTab('users_roles')}
            className="px-3 py-1.5 bg-blue-600 hover:bg-blue-500 text-white rounded text-xs font-medium transition-colors"
          >
            Manage Users & Permissions
          </button>
        </div>
      </div>

      {/* Admin Notice */}
      <div className="bg-amber-950/30 border border-amber-800/50 p-3 rounded-lg flex items-start gap-2.5 text-xs text-amber-200">
        <ShieldAlert className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
        <div>
          <span className="font-semibold text-amber-300">Administrative Governance Policy: </span>
          Per company SOP, purchase order approval and dispatch authority remain reserved for the Owner (Suresh Patel). As Administrator, you manage master data, user accounts, and operational system health.
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>System Users</span>
            <Users className="w-4 h-4 text-blue-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {users.length}
          </div>
          <div className="text-[11px] text-emerald-400 mt-0.5">{activeUsers} active logins</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Offices & Hubs</span>
            <Building2 className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {offices.length}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">Across 4 Indian States</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Product Master</span>
            <Package className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {products.length}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">{publicWebProducts} visible on public web</div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Audited Actions</span>
            <History className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100 mt-1 tabular-nums">
            {auditRecords.length}
          </div>
          <div className="text-[11px] text-slate-400 mt-0.5">Immutable event trail</div>
        </div>
      </div>

      {/* Operational Administration Sections */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Active Users Table */}
        <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
              <Users className="w-3.5 h-3.5 text-blue-400" />
              <span>Active Internal Staff & Roles</span>
            </h2>
            <button
              onClick={() => setCurrentTab('users_roles')}
              className="text-[11px] text-emerald-400 hover:underline"
            >
              Configure Roles →
            </button>
          </div>

          <div className="divide-y divide-slate-800">
            {users.map((u) => (
              <div key={u.id} className="py-2.5 flex items-center justify-between text-xs">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-slate-100">{u.name}</span>
                    <span className="font-mono text-[10px] text-emerald-400 bg-emerald-950/60 px-1.5 py-0.2 rounded border border-emerald-800/40">
                      {u.role.replace(/_/g, ' ')}
                    </span>
                  </div>
                  <div className="text-[11px] text-slate-400 mt-0.5">
                    {u.email} · {u.officeName}
                  </div>
                </div>
                <div className="text-right text-[10px] text-slate-400 font-mono">
                  <span>Last: {u.lastLogin}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Regional Offices */}
        <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
              <Building2 className="w-3.5 h-3.5 text-purple-400" />
              <span>Operating State Facilities & Hubs</span>
            </h2>
            <button
              onClick={() => setCurrentTab('offices_states')}
              className="text-[11px] text-emerald-400 hover:underline"
            >
              Manage Offices →
            </button>
          </div>

          <div className="divide-y divide-slate-800">
            {offices.map((off) => (
              <div key={off.id} className="py-2.5 flex items-center justify-between text-xs">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-slate-100">{off.name}</span>
                    <span className="text-[10px] text-slate-400 font-mono">({off.city})</span>
                  </div>
                  <div className="text-[11px] text-slate-400 mt-0.5">
                    Manager: {off.managerName} · GSTIN: {off.gstin}
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-mono font-semibold text-slate-200">
                    {off.assignedUsersCount} Staff
                  </span>
                  <div className="text-[10px] text-slate-400">{off.state}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* System Audit Activity */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg p-4">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
            <Activity className="w-3.5 h-3.5 text-emerald-400" />
            <span>Recent System & Security Audit Logs</span>
          </h2>
          <button
            onClick={() => setCurrentTab('audit_history')}
            className="text-[11px] text-emerald-400 hover:underline"
          >
            Open Full Ledger →
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
              <tr>
                <th className="p-2.5">Correlation ID</th>
                <th className="p-2.5">User</th>
                <th className="p-2.5">Action</th>
                <th className="p-2.5">Resource</th>
                <th className="p-2.5">Details</th>
                <th className="p-2.5">Timestamp</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/70">
              {auditRecords.slice(0, 5).map((log) => (
                <tr key={log.id} className="hover:bg-slate-850/50">
                  <td className="p-2.5 font-mono text-emerald-400 text-[11px]">{log.correlationId}</td>
                  <td className="p-2.5 font-medium text-slate-200">{log.userName}</td>
                  <td className="p-2.5 font-mono text-[11px] text-blue-400">{log.action}</td>
                  <td className="p-2.5 font-mono text-[11px] text-slate-300">{log.resourceIdentifier}</td>
                  <td className="p-2.5 text-slate-400 text-[11px] max-w-xs truncate">{log.newValue}</td>
                  <td className="p-2.5 font-mono text-slate-400 text-[10px]">{log.timestamp}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
