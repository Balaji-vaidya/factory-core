import React, { useState } from 'react';
import { 
  Users, 
  ShieldCheck, 
  UserPlus, 
  Search, 
  Building2, 
  CheckCircle2, 
  Lock,
  X 
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { UserRole } from '../../types';

export const UsersRolesManagement: React.FC = () => {
  const { users, offices, setCurrentUserRole, showToast } = useApp();
  const [searchQuery, setSearchQuery] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);

  const filteredUsers = users.filter((u) => {
    return (
      u.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.role.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.officeName.toLowerCase().includes(searchQuery.toLowerCase())
    );
  });

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
              <Users className="w-5 h-5 text-blue-400" />
              <span>Users & Role-Based Access Control (RBAC)</span>
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
              {users.length} Internal Accounts
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Section 21: Manage personnel authorization across Owner, Administrator, Main Office, Outer State Offices, and Godown Managers.
          </p>
        </div>

        <button
          onClick={() => setShowAddModal(true)}
          className="px-3.5 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1.5 shadow-xs"
        >
          <UserPlus className="w-4 h-4" />
          <span>+ Add Internal User</span>
        </button>
      </div>

      {/* Role Hierarchy Matrix Reference */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-xs">
        <div className="p-3 bg-slate-900 border border-slate-800 rounded-lg">
          <div className="font-bold text-amber-300 flex items-center gap-1.5">
            <ShieldCheck className="w-3.5 h-3.5 text-amber-400" />
            <span>Owner Authority</span>
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            Sole authority for PO approval, cancellation confirmation, and dispatch authorization. Full visibility across all facilities and financial values.
          </p>
        </div>

        <div className="p-3 bg-slate-900 border border-slate-800 rounded-lg">
          <div className="font-bold text-blue-300 flex items-center gap-1.5">
            <Building2 className="w-3.5 h-3.5 text-blue-400" />
            <span>Main Office & Sales</span>
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            Creates POs, manages corporate client registry, processes inbound website enquiries, and tracks shipments. Cannot approve own POs.
          </p>
        </div>

        <div className="p-3 bg-slate-900 border border-slate-800 rounded-lg">
          <div className="font-bold text-purple-300 flex items-center gap-1.5">
            <Lock className="w-3.5 h-3.5 text-purple-400" />
            <span>Outer State (§8 Guardrail)</span>
          </div>
          <p className="text-[11px] text-slate-400 mt-1">
            Regional state sales and deliveries. Masked physical stock counts (status only). Restricted to assigned state territory data.
          </p>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden shadow-xs">
        <div className="p-3 border-b border-slate-800 flex items-center justify-between bg-slate-950/40">
          <span className="text-xs font-bold text-slate-200 uppercase tracking-wider font-mono">
            Active Staff Directory
          </span>
          <div className="relative w-64">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-2" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search user, role, email..."
              className="w-full pl-8 pr-2.5 py-1 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 focus:outline-none"
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
              <tr>
                <th className="p-3">Staff Name</th>
                <th className="p-3">Email</th>
                <th className="p-3">Assigned Role</th>
                <th className="p-3">Operating Office</th>
                <th className="p-3">Status</th>
                <th className="p-3">Last Active</th>
                <th className="p-3 text-right">Switch Perspective</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {filteredUsers.map((u) => (
                <tr key={u.id} className="hover:bg-slate-850/50">
                  <td className="p-3 font-semibold text-slate-100">{u.name}</td>
                  <td className="p-3 font-mono text-slate-400">{u.email}</td>
                  <td className="p-3">
                    <span className="font-mono text-[11px] text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-800/40">
                      {u.role.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="p-3 text-slate-300">{u.officeName}</td>
                  <td className="p-3">
                    <span className="px-1.5 py-0.5 rounded bg-emerald-950/50 text-emerald-300 text-[10px] border border-emerald-800/40">
                      {u.status}
                    </span>
                  </td>
                  <td className="p-3 font-mono text-slate-400 text-[11px]">{u.lastLogin}</td>
                  <td className="p-3 text-right">
                    <button
                      onClick={() => {
                        setCurrentUserRole(u.role);
                        showToast(`Switched active view to ${u.name}`, 'info');
                      }}
                      className="px-2 py-1 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded text-[11px] transition-colors"
                    >
                      Login As
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add User Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-md w-full space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-2">
              <h3 className="text-sm font-bold text-slate-100">Add Internal Staff User</h3>
              <button onClick={() => setShowAddModal(false)} className="text-slate-400 hover:text-slate-200">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">Full Name</label>
                <input
                  type="text"
                  placeholder="e.g. Vikram Joshi"
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Internal Corporate Email</label>
                <input
                  type="email"
                  placeholder="name@agrisep.com"
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Designated Role</label>
                <select className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100">
                  <option value="MAIN_OFFICE">Main Office / Sales</option>
                  <option value="OUTER_STATE_OFFICE">Outer State Office</option>
                  <option value="GODOWN_MANAGER">Godown Manager</option>
                  <option value="ADMIN">Administrator</option>
                </select>
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Assigned Facility</label>
                <select className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100">
                  {offices.map((o) => (
                    <option key={o.id} value={o.id}>{o.name}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-2 border-t border-slate-800">
              <button
                onClick={() => setShowAddModal(false)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded text-xs"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  showToast('New user account provisioned', 'success');
                  setShowAddModal(false);
                }}
                className="px-3.5 py-1.5 bg-blue-600 hover:bg-blue-500 text-white rounded text-xs font-semibold"
              >
                Create User
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
