import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { Header } from './components/common/Header';
import { Sidebar } from './components/common/Sidebar';
import { GlobalSearchModal } from './components/common/GlobalSearchModal';
import { GuidedWorkflowModal } from './components/common/GuidedWorkflowModal';

// Dashboards
import { OwnerDashboard } from './components/dashboards/OwnerDashboard';
import { AdminDashboard } from './components/dashboards/AdminDashboard';
import { MainOfficeDashboard } from './components/dashboards/MainOfficeDashboard';
import { OuterStateDashboard } from './components/dashboards/OuterStateDashboard';
import { GodownDashboard } from './components/dashboards/GodownDashboard';

// Core Business Management Modules
import { POManagement } from './components/po/POManagement';
import { InventoryManagement } from './components/inventory/InventoryManagement';
import { StockShortages } from './components/shortages/StockShortages';
import { DeliveryChallanManagement } from './components/dc/DeliveryChallanManagement';
import { DispatchManagement } from './components/dispatch/DispatchManagement';
import { DeliveriesManagement } from './components/deliveries/DeliveriesManagement';
import { ClientManagement } from './components/clients/ClientManagement';
import { ProductManagement } from './components/products/ProductManagement';
import { EnquiriesManagement } from './components/enquiries/EnquiriesManagement';
import { AuditHistory } from './components/audit/AuditHistory';
import { ReportsManagement } from './components/reports/ReportsManagement';
import { UsersRolesManagement } from './components/admin/UsersRolesManagement';
import { OfficesManagement } from './components/admin/OfficesManagement';
import { DocumentsManagement } from './components/admin/DocumentsManagement';

// Auth and Public
import { LoginPage } from './components/auth/LoginPage';
import { PublicWebsite } from './components/public/PublicWebsite';

// Simple Settings Component
const SettingsView: React.FC = () => {
  const { showToast } = useApp();
  return (
    <div className="space-y-6 max-w-4xl">
      <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <h1 className="text-xl font-bold tracking-tight text-slate-100">
          Factory System & Business Configuration
        </h1>
        <p className="text-xs text-slate-400 mt-1">
          Master company legal parameters, GSTIN credentials, automated reservation rules, and notification defaults.
        </p>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-lg p-5 space-y-4 text-xs">
        <h2 className="text-sm font-bold text-slate-200 uppercase tracking-wider font-mono">
          Corporate Entity Profile
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-slate-400 mb-1 font-mono">Corporate Legal Name</label>
            <input
              type="text"
              readOnly
              value="Indo American Equipment"
              className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-200"
            />
          </div>

          <div>
            <label className="block text-slate-400 mb-1 font-mono">Brand Name</label>
            <input
              type="text"
              readOnly
              value="Agrisep®"
              className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-200 font-bold text-emerald-400"
            />
          </div>

          <div>
            <label className="block text-slate-400 mb-1 font-mono">Corporate GSTIN</label>
            <input
              type="text"
              readOnly
              value="29AAACI4921C1Z4"
              className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-200 font-mono"
            />
          </div>

          <div>
            <label className="block text-slate-400 mb-1 font-mono">PAN</label>
            <input
              type="text"
              readOnly
              value="AAACI4921C"
              className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-200 font-mono"
            />
          </div>
        </div>

        <div className="pt-4 border-t border-slate-800 flex justify-end">
          <button
            onClick={() => showToast('Configuration saved successfully', 'success')}
            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded font-semibold text-xs transition-colors"
          >
            Save System Preferences
          </button>
        </div>
      </div>
    </div>
  );
};

const MainContent: React.FC = () => {
  const { 
    isLoggedIn, 
    isPublicWebsite, 
    currentTab, 
    currentUser, 
    toastMessage,
    showGuidedTour,
    setShowGuidedTour
  } = useApp();

  const [isSearchOpen, setIsSearchOpen] = React.useState(false);

  // If viewing public corporate website
  if (isPublicWebsite) {
    return <PublicWebsite />;
  }

  // If not logged in, show industrial login screen
  if (!isLoggedIn) {
    return <LoginPage />;
  }

  const renderDashboard = () => {
    switch (currentUser.role) {
      case 'OWNER':
        return <OwnerDashboard />;
      case 'ADMIN':
        return <AdminDashboard />;
      case 'MAIN_OFFICE':
        return <MainOfficeDashboard />;
      case 'OUTER_STATE_OFFICE':
        return <OuterStateDashboard />;
      case 'GODOWN_MANAGER':
        return <GodownDashboard />;
      default:
        return <OwnerDashboard />;
    }
  };

  const renderActiveTab = () => {
    switch (currentTab) {
      case 'dashboard':
        return renderDashboard();
      case 'purchase_orders':
        return <POManagement />;
      case 'clients':
        return <ClientManagement />;
      case 'products':
        return <ProductManagement />;
      case 'inventory':
        return <InventoryManagement />;
      case 'stock_shortages':
        return <StockShortages />;
      case 'delivery_challans':
        return <DeliveryChallanManagement />;
      case 'dispatch':
        return <DispatchManagement />;
      case 'deliveries':
        return <DeliveriesManagement />;
      case 'enquiries':
        return <EnquiriesManagement />;
      case 'documents':
        return <DocumentsManagement />;
      case 'reports':
        return <ReportsManagement />;
      case 'audit':
        return <AuditHistory />;
      case 'users':
        return <UsersRolesManagement />;
      case 'offices':
        return <OfficesManagement />;
      case 'settings':
        return <SettingsView />;
      default:
        return renderDashboard();
    }
  };

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 font-sans overflow-hidden">
      {/* Sidebar */}
      <Sidebar />

      {/* Main Column */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top Header */}
        <Header onOpenSearch={() => setIsSearchOpen(true)} />

        {/* Scrollable View Content */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8 bg-slate-950">
          <div className="max-w-7xl mx-auto pb-12">
            {renderActiveTab()}
          </div>
        </main>
      </div>

      {/* Interactive Global Modals */}
      <GlobalSearchModal isOpen={isSearchOpen} onClose={() => setIsSearchOpen(false)} />
      <GuidedWorkflowModal isOpen={showGuidedTour} onClose={() => setShowGuidedTour(false)} />

      {/* Floating System Toast Alert */}
      {toastMessage && (
        <div className="fixed bottom-5 right-5 z-50 animate-in fade-in slide-in-from-bottom-5">
          <div className={`px-4 py-2.5 rounded-lg border text-xs font-semibold shadow-2xl flex items-center gap-2 ${
            toastMessage.type === 'success'
              ? 'bg-emerald-950 text-emerald-300 border-emerald-800'
              : (toastMessage.type === 'error'
                  ? 'bg-rose-950 text-rose-300 border-rose-800'
                  : 'bg-slate-900 text-slate-200 border-slate-700')
          }`}>
            <span className="w-2 h-2 rounded-full bg-current" />
            <span>{toastMessage.text}</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default function App() {
  return (
    <AppProvider>
      <MainContent />
    </AppProvider>
  );
}
