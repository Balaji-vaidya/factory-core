import React, { useState } from 'react';
import { 
  X, 
  CheckCircle2, 
  ArrowRight, 
  Play, 
  Sparkles, 
  ShieldCheck, 
  Boxes, 
  Truck, 
  FileText, 
  CheckSquare,
  Building,
  RefreshCw
} from 'lucide-react';
import { useApp } from '../../context/AppContext';

interface GuidedWorkflowModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const GuidedWorkflowModal: React.FC<GuidedWorkflowModalProps> = ({ isOpen, onClose }) => {
  const { 
    purchaseOrders, 
    inventory, 
    deliveryChallans, 
    setCurrentUserRole, 
    setCurrentTab, 
    setSelectedPOId,
    setSelectedDCId,
    approvePurchaseOrder,
    createDeliveryChallan,
    approveDispatch,
    confirmDelivery,
    showToast
  } = useApp();

  const [activeStep, setActiveStep] = useState<number>(1);

  if (!isOpen) return null;

  const steps = [
    {
      num: 1,
      title: 'Review Clients & Product Master',
      role: 'MAIN_OFFICE' as const,
      tab: 'clients',
      desc: 'Browse seed-producing clients (Mahyco, Kaveri) and check technical specifications for Agrisep® seed processing machinery.',
      actionLabel: 'Switch to Main Office & View Clients',
      execute: () => {
        setCurrentUserRole('MAIN_OFFICE');
        setCurrentTab('clients');
        setActiveStep(2);
        onClose();
      }
    },
    {
      num: 2,
      title: 'Create & Submit Purchase Order',
      role: 'MAIN_OFFICE' as const,
      tab: 'purchase_orders',
      desc: 'Create a new PO with client snapshot and product line items. Rule 1: PO does not lock or affect inventory before Owner approval.',
      actionLabel: 'Go to PO List & Create PO',
      execute: () => {
        setCurrentUserRole('MAIN_OFFICE');
        setCurrentTab('purchase_orders');
        setActiveStep(3);
        onClose();
      }
    },
    {
      num: 3,
      title: 'Owner PO Approval & Stock Check',
      role: 'OWNER' as const,
      tab: 'purchase_orders',
      desc: 'Suresh Patel (Owner) reviews PO-2026-000101. Approving automatically checks Godown inventory. Stock is reserved; if insufficient, shortage is logged.',
      actionLabel: 'Switch to Owner & Approve PO-000101',
      execute: () => {
        setCurrentUserRole('OWNER');
        setCurrentTab('purchase_orders');
        setSelectedPOId('PO-000101');
        setActiveStep(4);
        onClose();
      }
    },
    {
      num: 4,
      title: 'Inspect Stock Reservation & Shortages',
      role: 'GODOWN_MANAGER' as const,
      tab: 'inventory',
      desc: 'Examine Available vs Reserved stock (Available = On Hand - Reserved). Notice PO-000102 has partial reservation & shortage SH-801.',
      actionLabel: 'View Warehouse Inventory & Shortages',
      execute: () => {
        setCurrentUserRole('GODOWN_MANAGER');
        setCurrentTab('inventory');
        setActiveStep(5);
        onClose();
      }
    },
    {
      num: 5,
      title: 'Create Delivery Challan (DC)',
      role: 'GODOWN_MANAGER' as const,
      tab: 'delivery_challans',
      desc: 'Warehouse manager prepares DC for approved PO-000104. Dispatch quantity is strictly validated against reserved quantity & remaining PO quantity.',
      actionLabel: 'Go to Delivery Challans',
      execute: () => {
        setCurrentUserRole('GODOWN_MANAGER');
        setCurrentTab('delivery_challans');
        setActiveStep(6);
        onClose();
      }
    },
    {
      num: 6,
      title: 'Dispatch Approval & Physical Deduction',
      role: 'OWNER' as const,
      tab: 'dispatch',
      desc: 'Owner authorizes DC-2026-042 for transit with carrier. Stock ledger records DC_DISPATCH and deducts physical balance on hand.',
      actionLabel: 'Review Pending Dispatches as Owner',
      execute: () => {
        setCurrentUserRole('OWNER');
        setCurrentTab('dispatch');
        setActiveStep(7);
        onClose();
      }
    },
    {
      num: 7,
      title: 'Confirm Delivery with Proof of Delivery',
      role: 'GODOWN_MANAGER' as const,
      tab: 'deliveries',
      desc: 'Consignee receives machine at site. Enter receiver name, delivery date, signed POD reference. PO lifecycle transitions to DELIVERED!',
      actionLabel: 'Track Deliveries & Confirm POD',
      execute: () => {
        setCurrentUserRole('GODOWN_MANAGER');
        setCurrentTab('deliveries');
        setActiveStep(1);
        onClose();
      }
    }
  ];

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
      <div 
        className="w-full max-w-3xl bg-slate-900 border border-slate-700 rounded-lg shadow-2xl overflow-hidden flex flex-col max-h-[85vh] animate-in fade-in zoom-in-95 duration-150"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="p-4 border-b border-slate-800 flex items-center justify-between bg-slate-950/60">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded bg-indigo-600/30 border border-indigo-500/50 flex items-center justify-center text-indigo-400">
              <Sparkles className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-slate-100">
                End-to-End Factory Workflow Simulator
              </h2>
              <p className="text-[11px] text-slate-400">
                Agrisep® Golden Order Lifecycle: PO Creation → Owner Approval → Reservation → DC → Dispatch → Delivery
              </p>
            </div>
          </div>
          <button onClick={onClose} className="p-1 text-slate-400 hover:text-slate-200">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-4 space-y-3">
          <div className="bg-slate-950 p-3 rounded-md border border-slate-800 text-xs text-slate-300">
            <span className="font-semibold text-emerald-400">Business Integrity Rules: </span>
            The system strictly implements automatic stock availability checks upon Owner approval, partial stock reservations, automatic shortage tracking, and DC dispatch quantity safeguards.
          </div>

          <div className="space-y-2">
            {steps.map((st) => (
              <div 
                key={st.num}
                className={`p-3.5 rounded-lg border transition-all ${
                  activeStep === st.num 
                    ? 'bg-slate-800/90 border-indigo-500/60 shadow-md ring-1 ring-indigo-500/30' 
                    : 'bg-slate-950/40 border-slate-800/80 hover:bg-slate-850/50'
                }`}
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-start gap-3">
                    <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-mono font-bold shrink-0 mt-0.5 ${
                      activeStep === st.num 
                        ? 'bg-indigo-600 text-white' 
                        : 'bg-slate-800 text-slate-400 border border-slate-700'
                    }`}>
                      {st.num}
                    </div>

                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-semibold text-slate-100">{st.title}</span>
                        <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-slate-800 text-emerald-400 border border-slate-700">
                          Role: {st.role.replace(/_/g, ' ')}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-400 mt-1 leading-relaxed">
                        {st.desc}
                      </p>
                    </div>
                  </div>

                  <button
                    onClick={st.execute}
                    className={`px-3 py-1.5 rounded text-xs font-medium shrink-0 flex items-center gap-1.5 transition-colors whitespace-nowrap ${
                      activeStep === st.num
                        ? 'bg-indigo-600 hover:bg-indigo-500 text-white shadow-xs'
                        : 'bg-slate-800 hover:bg-slate-750 text-slate-300 border border-slate-700'
                    }`}
                  >
                    <span>{st.actionLabel}</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Footer */}
        <div className="p-3 bg-slate-950 border-t border-slate-800 flex items-center justify-between text-xs text-slate-400">
          <span>You can switch roles anytime using the top bar badge.</span>
          <button
            onClick={onClose}
            className="px-3 py-1 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded text-xs transition-colors"
          >
            Close Simulator
          </button>
        </div>
      </div>
    </div>
  );
};
