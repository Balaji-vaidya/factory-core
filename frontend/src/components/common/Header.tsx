import React, { useState } from 'react';
import { 
  Search, 
  Bell, 
  Building2, 
  ShieldCheck, 
  ExternalLink, 
  PlayCircle, 
  CheckCircle2, 
  LogOut, 
  ChevronDown,
  Warehouse,
  AlertTriangle,
  User as UserIcon
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { UserRole } from '../../types';

interface HeaderProps {
  onOpenSearch: () => void;
}

export const Header: React.FC<HeaderProps> = ({ onOpenSearch }) => {
  const { 
    currentUser, 
    setCurrentUserRole, 
    currentOffice, 
    setCurrentOffice, 
    offices, 
    notifications, 
    markNotificationAsRead, 
    markAllNotificationsRead,
    setAppMode,
    setSelectedPOId,
    setCurrentTab,
    setShowGuidedTour,
    setIsLoggedIn,
    showToast
  } = useApp();

  const [showNotifications, setShowNotifications] = useState(false);
  const [showRoleMenu, setShowRoleMenu] = useState(false);
  const [showOfficeMenu, setShowOfficeMenu] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  const unreadCount = notifications.filter(n => !n.isRead).length;

  const roles: { role: UserRole; label: string; desc: string }[] = [
    { role: 'OWNER', label: 'Owner (Suresh Patel)', desc: 'Executive oversight, PO approvals, dispatch authorization' },
    { role: 'ADMIN', label: 'Admin (Anita Sharma)', desc: 'System administration, user access, products, operations' },
    { role: 'MAIN_OFFICE', label: 'Main Office (Rajesh Verma)', desc: 'Client relations, PO creation, website enquiries' },
    { role: 'OUTER_STATE_OFFICE', label: 'Outer State (Kiran Reddy)', desc: 'Regional state POs, restricted inventory visibility' },
    { role: 'GODOWN_MANAGER', label: 'Godown Manager (Ramesh Yadav)', desc: 'Warehouse inventory, DC creation, dispatch, POD' },
  ];

  const handleRoleChange = (role: UserRole) => {
    setCurrentUserRole(role);
    setShowRoleMenu(false);
  };

  const handleNotificationClick = (notif: typeof notifications[0]) => {
    markNotificationAsRead(notif.id);
    setShowNotifications(false);
    if (notif.relatedRecordType === 'PO') {
      setSelectedPOId(notif.relatedRecordId);
      setCurrentTab('purchase_orders');
    } else if (notif.relatedRecordType === 'DC') {
      setCurrentTab('delivery_challans');
    } else if (notif.relatedRecordType === 'SHORTAGE') {
      setCurrentTab('stock_shortages');
    } else if (notif.relatedRecordType === 'ENQUIRY') {
      setCurrentTab('enquiries');
    }
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    showToast('Signed out of Agrisep Internal Management System', 'info');
  };

  return (
    <header className="h-14 bg-slate-900 border-b border-slate-800 text-slate-100 px-4 md:px-6 flex items-center justify-between sticky top-0 z-30 select-none">
      {/* Brand & Mode Indicator */}
      <div className="flex items-center gap-3">
        <a 
          href="#dashboard" 
          onClick={(e) => { e.preventDefault(); setCurrentTab('dashboard'); }}
          className="flex items-center gap-2 group cursor-pointer"
        >
          <div className="w-8 h-8 rounded-md bg-[#1E3A8A] border border-[#2563EB]/40 flex items-center justify-center font-bold text-white text-base tracking-wider shadow-sm">
            A
          </div>
          <div className="flex flex-col">
            <span className="text-base font-bold tracking-tight text-white group-hover:text-blue-400 transition-colors leading-none">
              Agrisep®
            </span>
            <span className="text-[10px] text-slate-400 font-mono tracking-wider uppercase leading-tight">
              Indo American Equipment
            </span>
          </div>
        </a>

        <div className="hidden lg:flex items-center gap-2 ml-4 pl-4 border-l border-slate-800">
          <span className="text-label text-slate-400">Environment:</span>
          <span className="text-label font-mono text-[#2563EB] bg-[#2563EB]/10 px-2 py-0.5 rounded-md border border-[#2563EB]/30">
            app.agrisep.com
          </span>
        </div>
      </div>

      {/* Center: Global Search & Quick Workflow Action */}
      <div className="flex items-center gap-2 max-w-md w-full mx-4">
        <button
          onClick={onOpenSearch}
          className="flex-1 hidden sm:flex items-center justify-between px-3 py-1.5 bg-slate-950/80 hover:bg-slate-950 text-slate-400 hover:text-slate-200 border border-slate-800 rounded-md text-xs transition-colors"
          title="Global Search across POs, Clients, Products, DCs, Vehicle numbers"
        >
          <div className="flex items-center gap-2 truncate">
            <Search className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span className="truncate">Search PO, Client, SKU, DC, Vehicle...</span>
          </div>
          <kbd className="hidden md:inline-block px-1.5 py-0.5 text-[10px] font-mono bg-slate-900 border border-slate-700 rounded text-slate-400">
            Ctrl+K
          </kbd>
        </button>

        {/* Golden Flow Guided Simulator */}
        <button
          onClick={() => setShowGuidedTour(true)}
          className="hidden xl:flex items-center gap-1.5 px-2.5 py-1.5 bg-blue-950/60 hover:bg-blue-900/80 border border-[#2563EB]/50 text-blue-300 rounded-md text-xs font-medium transition-colors whitespace-nowrap"
          title="Interactive walkthrough demonstrating PO Creation -> Owner Approval -> Stock Reservation -> Delivery Challan -> Dispatch -> Delivery"
        >
          <PlayCircle className="w-3.5 h-3.5 text-[#2563EB]" />
          <span>Flow Simulator</span>
        </button>
      </div>

      {/* Right Controls: Role Switcher, Office Switcher, Notifications, Public Web, User */}
      <div className="flex items-center gap-2">
        {/* Role Switcher */}
        <div className="relative">
          <button
            onClick={() => { setShowRoleMenu(!showRoleMenu); setShowOfficeMenu(false); setShowNotifications(false); }}
            className="flex items-center gap-1.5 px-2.5 py-1 bg-slate-800 hover:bg-slate-750 border border-slate-700 rounded-md text-xs font-medium text-slate-200 transition-colors"
            title="Switch User Role to test permissions"
          >
            <ShieldCheck className="w-3.5 h-3.5 text-[#2563EB]" />
            <span className="hidden sm:inline font-mono uppercase text-[11px] text-[#2563EB] font-bold">
              {currentUser.role.replace(/_/g, ' ')}
            </span>
            <ChevronDown className="w-3 h-3 text-slate-400" />
          </button>

          {showRoleMenu && (
            <div className="absolute right-0 mt-2 w-72 bg-slate-900 border border-slate-800 rounded-lg shadow-xl py-2 z-50 animate-in fade-in zoom-in-95">
              <div className="px-3 py-1.5 border-b border-slate-800 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                Switch Role / Permission View
              </div>
              <div className="py-1">
                {roles.map((r) => (
                  <button
                    key={r.role}
                    onClick={() => handleRoleChange(r.role)}
                    className={`w-full text-left px-3 py-2 text-xs hover:bg-slate-800/80 flex items-start gap-2.5 transition-colors ${
                      currentUser.role === r.role ? 'bg-blue-950/60 text-white' : 'text-slate-300'
                    }`}
                  >
                    <div className="w-2 h-2 rounded-full bg-[#2563EB] mt-1 shrink-0" />
                    <div>
                      <div className="font-medium text-slate-100">{r.label}</div>
                      <div className="text-[11px] text-slate-400 leading-snug">{r.desc}</div>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Office Context Switcher */}
        <div className="relative hidden md:block">
          <button
            onClick={() => { setShowOfficeMenu(!showOfficeMenu); setShowRoleMenu(false); setShowNotifications(false); }}
            className="flex items-center gap-1.5 px-2.5 py-1 bg-slate-800 hover:bg-slate-750 border border-slate-700 rounded text-xs text-slate-300 transition-colors"
            title="Current Operating Office / Godown"
          >
            <Building2 className="w-3.5 h-3.5 text-slate-400" />
            <span className="max-w-[120px] truncate">{currentOffice.city}</span>
            <ChevronDown className="w-3 h-3 text-slate-400" />
          </button>

          {showOfficeMenu && (
            <div className="absolute right-0 mt-2 w-64 bg-slate-900 border border-slate-800 rounded-lg shadow-xl py-2 z-50 animate-in fade-in zoom-in-95">
              <div className="px-3 py-1.5 border-b border-slate-800 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                Select Active Office / Location
              </div>
              {offices.map((off) => (
                <button
                  key={off.id}
                  onClick={() => {
                    setCurrentOffice(off);
                    setShowOfficeMenu(false);
                    showToast(`Office context switched to ${off.name}`, 'info');
                  }}
                  className={`w-full text-left px-3 py-2 text-xs hover:bg-slate-800 flex items-center justify-between transition-colors ${
                    currentOffice.id === off.id ? 'bg-slate-800 text-emerald-400 font-medium' : 'text-slate-300'
                  }`}
                >
                  <div className="truncate">
                    <div>{off.name}</div>
                    <div className="text-[10px] text-slate-400">{off.state} · {off.type.replace(/_/g, ' ')}</div>
                  </div>
                  {currentOffice.id === off.id && <CheckCircle2 className="w-3.5 h-3.5 shrink-0 text-emerald-400" />}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Notifications Dropdown */}
        <div className="relative">
          <button
            onClick={() => { setShowNotifications(!showNotifications); setShowRoleMenu(false); setShowOfficeMenu(false); }}
            className="p-1.5 text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-750 border border-slate-700 rounded relative transition-colors"
            title="System & Workflow Notifications"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 w-4 h-4 bg-rose-600 text-white rounded-full text-[10px] font-bold flex items-center justify-center font-mono animate-pulse">
                {unreadCount}
              </span>
            )}
          </button>

          {showNotifications && (
            <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-slate-900 border border-slate-800 rounded-lg shadow-2xl py-2 z-50 max-h-[80vh] flex flex-col">
              <div className="px-3 py-2 border-b border-slate-800 flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-200">
                  Notifications ({unreadCount} unread)
                </span>
                {unreadCount > 0 && (
                  <button
                    onClick={markAllNotificationsRead}
                    className="text-[11px] text-emerald-400 hover:text-emerald-300 transition-colors"
                  >
                    Mark all read
                  </button>
                )}
              </div>

              <div className="overflow-y-auto divide-y divide-slate-800/60 max-h-80">
                {notifications.length === 0 ? (
                  <div className="p-4 text-center text-xs text-slate-400">No notifications</div>
                ) : (
                  notifications.map((n) => (
                    <div
                      key={n.id}
                      onClick={() => handleNotificationClick(n)}
                      className={`p-3 text-xs hover:bg-slate-850 cursor-pointer transition-colors ${
                        !n.isRead ? 'bg-slate-850/60 border-l-2 border-l-emerald-500' : 'opacity-70'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-1">
                        <span className="font-semibold text-slate-200">{n.title}</span>
                        <span className="text-[10px] text-slate-400 font-mono">{n.timestamp}</span>
                      </div>
                      <p className="text-slate-300 text-[11px] leading-relaxed mb-1">{n.message}</p>
                      <div className="flex items-center gap-2">
                        <span className="text-[10px] font-mono text-emerald-400 bg-emerald-950/60 px-1.5 py-0.5 rounded border border-emerald-800/40">
                          {n.relatedRecordNumber}
                        </span>
                        <span className="text-[10px] text-slate-400">Click to view record →</span>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        {/* Switch to Public Website */}
        <button
          onClick={() => {
            setAppMode('public');
            showToast('Switched to Public Corporate Website (www.agrisep.com)', 'info');
          }}
          className="flex items-center gap-1.5 px-2.5 py-1 bg-slate-800 hover:bg-slate-750 border border-slate-700 text-slate-200 hover:text-white rounded text-xs transition-colors whitespace-nowrap"
          title="Open Public Website (www.agrisep.com)"
        >
          <ExternalLink className="w-3.5 h-3.5 text-blue-400" />
          <span className="hidden sm:inline">Public Site</span>
        </button>

        {/* User Profile & Sign Out */}
        <div className="relative">
          <button
            onClick={() => { setShowUserMenu(!showUserMenu); setShowRoleMenu(false); setShowNotifications(false); }}
            className="flex items-center gap-2 pl-2 border-l border-slate-800 text-xs text-slate-300 hover:text-white"
          >
            <div className="w-7 h-7 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-slate-300 text-xs">
              {currentUser.name.split(' ').map(n => n[0]).join('')}
            </div>
            <span className="hidden xl:inline text-xs font-medium text-slate-200 truncate max-w-[100px]">
              {currentUser.name.split(' ')[0]}
            </span>
          </button>

          {showUserMenu && (
            <div className="absolute right-0 mt-2 w-56 bg-slate-900 border border-slate-800 rounded-lg shadow-xl py-2 z-50 animate-in fade-in zoom-in-95">
              <div className="px-3 py-2 border-b border-slate-800">
                <div className="font-semibold text-xs text-slate-200">{currentUser.name}</div>
                <div className="text-[11px] text-slate-400 font-mono truncate">{currentUser.email}</div>
                <div className="text-[10px] text-emerald-400 mt-1 font-mono uppercase font-semibold">
                  Role: {currentUser.role.replace(/_/g, ' ')}
                </div>
              </div>
              <div className="py-1">
                <button
                  onClick={() => { setCurrentTab('settings'); setShowUserMenu(false); }}
                  className="w-full text-left px-3 py-1.5 text-xs text-slate-300 hover:bg-slate-800 hover:text-white transition-colors"
                >
                  Account Settings
                </button>
                <button
                  onClick={handleLogout}
                  className="w-full text-left px-3 py-1.5 text-xs text-rose-400 hover:bg-rose-950/40 hover:text-rose-300 flex items-center gap-2 transition-colors"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  <span>Sign Out</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
