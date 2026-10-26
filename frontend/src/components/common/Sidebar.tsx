import React from 'react';
import { 
  LayoutDashboard, 
  FileText, 
  Users, 
  Package, 
  Boxes, 
  AlertOctagon, 
  Truck, 
  Send, 
  CheckSquare, 
  MessageSquare, 
  FolderArchive, 
  BarChart3, 
  Bell, 
  History, 
  UserCheck, 
  MapPin, 
  Settings,
  ChevronRight
} from 'lucide-react';
import { useApp } from '../../context/AppContext';

export const Sidebar: React.FC = () => {
  const { 
    currentTab, 
    setCurrentTab, 
    currentUser, 
    purchaseOrders, 
    shortages, 
    deliveryChallans, 
    websiteEnquiries, 
    notifications 
  } = useApp();

  // Badges & Counters
  const pendingPOCount = purchaseOrders.filter(p => p.status === 'PENDING_APPROVAL').length;
  const openShortageCount = shortages.filter(s => s.status === 'OPEN').length;
  const pendingDispatchCount = deliveryChallans.filter(d => d.status === 'PENDING_APPROVAL').length;
  const newEnquiriesCount = websiteEnquiries.filter(e => e.status === 'NEW').length;
  const unreadNotifCount = notifications.filter(n => !n.isRead).length;

  const role = currentUser.role;

  // Filter navigation by role permissions
  interface NavItem {
    id: string;
    label: string;
    icon: React.ElementType;
    badge?: number;
    badgeColor?: string;
    allowedRoles: string[];
    subtext?: string;
  }

  const navItems: NavItem[] = [
    {
      id: 'dashboard',
      label: 'Dashboard',
      icon: LayoutDashboard,
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE', 'GODOWN_MANAGER'],
    },
    {
      id: 'purchase_orders',
      label: 'Purchase Orders',
      icon: FileText,
      badge: pendingPOCount,
      badgeColor: 'badge-warning',
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE', 'GODOWN_MANAGER'],
    },
    {
      id: 'clients',
      label: 'Clients',
      icon: Users,
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE'],
    },
    {
      id: 'products',
      label: 'Products',
      icon: Package,
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE', 'GODOWN_MANAGER'],
    },
    {
      id: 'inventory',
      label: role === 'OUTER_STATE_OFFICE' ? 'Stock Status' : 'Inventory',
      icon: Boxes,
      subtext: role === 'OUTER_STATE_OFFICE' ? 'Status Only (Restricted)' : undefined,
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE', 'GODOWN_MANAGER'],
    },
    {
      id: 'stock_shortages',
      label: 'Stock Shortages',
      icon: AlertOctagon,
      badge: openShortageCount,
      badgeColor: 'badge-error',
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'GODOWN_MANAGER'],
    },
    {
      id: 'delivery_challans',
      label: 'Delivery Challans',
      icon: Truck,
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'GODOWN_MANAGER'],
    },
    {
      id: 'dispatch',
      label: 'Dispatch',
      icon: Send,
      badge: pendingDispatchCount,
      badgeColor: 'badge-info',
      allowedRoles: ['OWNER', 'ADMIN', 'GODOWN_MANAGER'],
    },
    {
      id: 'deliveries',
      label: 'Deliveries',
      icon: CheckSquare,
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE', 'GODOWN_MANAGER'],
    },
    {
      id: 'enquiries',
      label: 'Website Enquiries',
      icon: MessageSquare,
      badge: newEnquiriesCount,
      badgeColor: 'badge-success',
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE'],
    },
    {
      id: 'documents',
      label: 'Documents',
      icon: FolderArchive,
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE', 'GODOWN_MANAGER'],
    },
    {
      id: 'reports',
      label: 'Reports',
      icon: BarChart3,
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'GODOWN_MANAGER'],
    },
    {
      id: 'notifications',
      label: 'Notifications',
      icon: Bell,
      badge: unreadNotifCount,
      badgeColor: 'badge-warning',
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE', 'GODOWN_MANAGER'],
    },
    {
      id: 'audit_history',
      label: 'Audit History',
      icon: History,
      allowedRoles: ['OWNER', 'ADMIN'],
    },
    {
      id: 'users_roles',
      label: 'Users & Roles',
      icon: UserCheck,
      allowedRoles: ['OWNER', 'ADMIN'],
    },
    {
      id: 'offices_states',
      label: 'Offices / States',
      icon: MapPin,
      allowedRoles: ['OWNER', 'ADMIN'],
    },
    {
      id: 'settings',
      label: 'Settings',
      icon: Settings,
      allowedRoles: ['OWNER', 'ADMIN', 'MAIN_OFFICE', 'OUTER_STATE_OFFICE', 'GODOWN_MANAGER'],
    },
  ];

  const visibleItems = navItems.filter((item) => item.allowedRoles.includes(role));

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 text-slate-300 flex flex-col shrink-0 select-none">
      {/* Current User Badge in Sidebar */}
      <div className="p-3 border-b border-slate-800/80 bg-slate-950/40">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-md bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-blue-400 text-xs font-mono">
            {role.slice(0, 2)}
          </div>
          <div className="overflow-hidden">
            <div className="text-body-medium font-semibold text-slate-100 truncate">
              {currentUser.name}
            </div>
            <div className="text-label text-slate-400 font-mono flex items-center gap-1 truncate">
              <span className="w-1.5 h-1.5 rounded-full bg-blue-500 inline-block" />
              <span>{role.replace(/_/g, ' ')}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Navigation Menu */}
      <nav className="flex-1 overflow-y-auto p-2 space-y-1">
        {visibleItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setCurrentTab(item.id)}
              className={`w-full flex items-center justify-between px-3 py-2 text-xs font-medium rounded-md transition-colors group text-left ${
                isActive
                  ? 'bg-blue-950/50 text-white font-semibold shadow-xs border-l-2 border-[#2563EB]'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <div className="flex items-center gap-2.5 truncate">
                <Icon
                  className={`w-4 h-4 shrink-0 transition-colors ${
                    isActive ? 'text-[#2563EB]' : 'text-slate-500 group-hover:text-slate-300'
                  }`}
                />
                <div className="truncate">
                  <span className="truncate">{item.label}</span>
                  {item.subtext && (
                    <span className="block text-[9px] text-amber-400/80 font-mono leading-none">
                      {item.subtext}
                    </span>
                  )}
                </div>
              </div>

              {item.badge !== undefined && item.badge > 0 && (
                <span
                  className={`text-[10px] font-mono px-1.5 py-0.2 rounded font-semibold ${
                    item.badgeColor || 'bg-slate-800 text-slate-300'
                  }`}
                >
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      {/* Industrial Hardware Version & Plant Yard Marker */}
      <div className="p-3 border-t border-slate-800 bg-slate-950/60 text-[10px] text-slate-400 font-mono">
        <div className="flex items-center justify-between">
          <span>Agrisep Industrial</span>
          <span className="text-slate-400">Release 2026.4</span>
        </div>
        <div className="text-[9px] text-slate-400 truncate mt-0.5">
          Indo American Equipment Plant 1
        </div>
      </div>
    </aside>
  );
};
