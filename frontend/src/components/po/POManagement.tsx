import React, { useState } from 'react';
import { 
  FileText, 
  Plus, 
  Search, 
  Filter, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  AlertTriangle, 
  Eye, 
  Truck, 
  Building, 
  Calendar, 
  Paperclip,
  Download,
  History,
  Layers,
  FileCheck,
  Send,
  X
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { PurchaseOrder, POStatus, Priority } from '../../types';
import { Badge } from '../common/Badge';

export const POManagement: React.FC = () => {
  const { 
    purchaseOrders, 
    clients, 
    products, 
    currentUser, 
    currentOffice,
    selectedPOId, 
    setSelectedPOId,
    createPurchaseOrder,
    approvePurchaseOrder,
    returnPurchaseOrder,
    requestCancelPurchaseOrder,
    approveCancellation,
    setCurrentTab,
    showToast
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [returnReason, setReturnReason] = useState('');
  const [showReturnModal, setShowReturnModal] = useState(false);
  const [cancelReason, setCancelReason] = useState('');
  const [showCancelModal, setShowCancelModal] = useState(false);

  // New PO Form State
  const [newPOClientId, setNewPOClientId] = useState(clients[0]?.id || '');
  const [newPOPriority, setNewPOPriority] = useState<Priority>('NORMAL');
  const [newPODeliveryDate, setNewPODeliveryDate] = useState('2026-10-30');
  const [newPONotes, setNewPONotes] = useState('');
  const [newPOItems, setNewPOItems] = useState<Array<{ productId: string; quantity: number; unitPriceINR: number; expectedDeliveryDate: string }>>([
    { productId: products[0]?.id || '', quantity: 1, unitPriceINR: products[0]?.basePriceINR || 450000, expectedDeliveryDate: '2026-10-30' }
  ]);

  // Selected PO
  const selectedPO = purchaseOrders.find((p) => p.id === selectedPOId);

  // Filtered List
  const filteredPOs = purchaseOrders.filter((po) => {
    // Outer State Office can only see own regional POs
    if (currentUser.role === 'OUTER_STATE_OFFICE' && po.officeId !== currentUser.officeId) {
      return false;
    }
    const matchesSearch = 
      po.poNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      po.clientName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      po.clientSnapshot.gstin.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesStatus = statusFilter === 'ALL' || po.status === statusFilter;
    const matchesPriority = priorityFilter === 'ALL' || po.priority === priorityFilter;

    return matchesSearch && matchesStatus && matchesPriority;
  });

  const handleAddItemToForm = () => {
    setNewPOItems([
      ...newPOItems,
      { productId: products[1]?.id || products[0]?.id, quantity: 1, unitPriceINR: products[1]?.basePriceINR || 320000, expectedDeliveryDate: newPODeliveryDate }
    ]);
  };

  const handleRemoveItemFromForm = (index: number) => {
    if (newPOItems.length > 1) {
      setNewPOItems(newPOItems.filter((_, i) => i !== index));
    }
  };

  const handleCreateSubmit = (submitForApproval: boolean) => {
    if (!newPOClientId) {
      showToast('Please select a client', 'error');
      return;
    }
    if (newPOItems.some(i => i.quantity <= 0)) {
      showToast('All items must have quantity >= 1', 'error');
      return;
    }

    const created = createPurchaseOrder({
      clientId: newPOClientId,
      officeId: currentOffice.id,
      priority: newPOPriority,
      expectedDeliveryDate: newPODeliveryDate,
      notes: newPONotes,
      items: newPOItems,
      attachments: [
        {
          id: `ATT-${Date.now()}`,
          name: `Purchase_Order_Agreement_${newPODeliveryDate}.pdf`,
          fileSize: '1.8 MB',
          uploadedAt: new Date().toISOString().slice(0, 10),
          uploadedBy: currentUser.name,
          category: 'PURCHASE_ORDER',
        }
      ],
      submitForApproval
    });

    setShowCreateModal(false);
    setSelectedPOId(created.id);
  };

  return (
    <div className="space-y-5">
      {/* Top Header & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100">
              Purchase Orders Management
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
              {filteredPOs.length} Records
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Complete lifecycle management: Draft → Owner Approval → Stock Reservation → Delivery Challans → Dispatches → Completed.
          </p>
        </div>

        {currentUser.role !== 'GODOWN_MANAGER' && (
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1.5 shadow-xs shrink-0"
          >
            <Plus className="w-4 h-4" />
            <span>Create Purchase Order</span>
          </button>
        )}
      </div>

      {/* Search & Filter Bar */}
      <div className="bg-slate-900 border border-slate-800 p-3 rounded-lg flex flex-col md:flex-row items-center gap-3">
        <div className="relative flex-1 w-full">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by PO number, client name, GSTIN..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-slate-700"
          />
        </div>

        <div className="flex items-center gap-2 w-full md:w-auto">
          <div className="flex items-center gap-1.5 text-xs text-slate-400">
            <Filter className="w-3.5 h-3.5" />
            <span>Status:</span>
          </div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded px-2.5 py-1.5 focus:outline-none"
          >
            <option value="ALL">All Statuses</option>
            <option value="DRAFT">Draft</option>
            <option value="PENDING_APPROVAL">Pending Approval</option>
            <option value="APPROVED">Approved</option>
            <option value="STOCK_RESERVATION">Stock Reservation / Shortage</option>
            <option value="NOT_DELIVERED">Not Delivered</option>
            <option value="PARTIALLY_DELIVERED">Partially Delivered</option>
            <option value="DELIVERED">Delivered</option>
            <option value="RETURNED">Returned</option>
            <option value="CANCELLED">Cancelled</option>
          </select>

          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded px-2.5 py-1.5 focus:outline-none"
          >
            <option value="ALL">All Priorities</option>
            <option value="URGENT">Urgent</option>
            <option value="HIGH">High</option>
            <option value="NORMAL">Normal</option>
          </select>
        </div>
      </div>

      {/* PO Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden shadow-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
              <tr>
                <th className="p-3">PO Number</th>
                <th className="p-3">Client</th>
                <th className="p-3">Office / State</th>
                <th className="p-3">Status</th>
                <th className="p-3">Stock Reservation</th>
                <th className="p-3">Delivery Status</th>
                <th className="p-3">Priority</th>
                <th className="p-3 text-right">Amount (INR)</th>
                <th className="p-3 text-right">Target Date</th>
                <th className="p-3 text-center">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {filteredPOs.length === 0 ? (
                <tr>
                  <td colSpan={10} className="p-8 text-center text-xs text-slate-400">
                    No purchase orders matching criteria.
                  </td>
                </tr>
              ) : (
                filteredPOs.map((po) => {
                  const isSelected = selectedPOId === po.id;
                  return (
                    <tr 
                      key={po.id} 
                      onClick={() => setSelectedPOId(po.id)}
                      className={`cursor-pointer transition-colors ${
                        isSelected ? 'bg-slate-800/90 ring-1 ring-emerald-500/40' : 'hover:bg-slate-850/50'
                      }`}
                    >
                      <td className="p-3 font-mono font-bold text-emerald-400">
                        {po.poNumber}
                      </td>
                      <td className="p-3">
                        <div className="font-semibold text-slate-100">{po.clientName}</div>
                        <div className="text-[10px] text-slate-400 font-mono truncate max-w-[160px]">
                          {po.clientSnapshot.gstin}
                        </div>
                      </td>
                      <td className="p-3 text-slate-300">
                        <div>{po.officeName.split(' ')[0]}</div>
                        <div className="text-[10px] text-slate-400 font-mono">{po.state}</div>
                      </td>
                      <td className="p-3"><Badge status={po.status} /></td>
                      <td className="p-3"><Badge status={po.reservationStatus} /></td>
                      <td className="p-3"><Badge status={po.deliveryStatus} /></td>
                      <td className="p-3"><Badge status={po.priority} type="priority" /></td>
                      <td className="p-3 text-right font-mono font-semibold text-slate-200">
                        ₹{(po.totalAmountINR / 100000).toFixed(2)}L
                      </td>
                      <td className="p-3 text-right font-mono text-slate-300">
                        {po.expectedDeliveryDate}
                      </td>
                      <td className="p-3 text-center">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedPOId(po.id);
                          }}
                          className="px-2 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded text-[11px] transition-colors"
                        >
                          Details
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* PO Detail Drawer / Modal */}
      {selectedPO && (
        <div className="bg-slate-900 border border-slate-800 rounded-lg p-5 space-y-5 shadow-xl">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-800 pb-4">
            <div>
              <div className="flex items-center gap-2.5">
                <span className="font-mono text-lg font-bold text-emerald-400">{selectedPO.poNumber}</span>
                <Badge status={selectedPO.status} />
                <Badge status={selectedPO.reservationStatus} />
                <Badge status={selectedPO.deliveryStatus} />
                <span className="text-xs text-slate-400 font-mono">v{selectedPO.currentVersion}</span>
              </div>
              <p className="text-xs text-slate-400 mt-1">
                Created on {selectedPO.poDate} by {selectedPO.createdBy} · Office: {selectedPO.officeName}
              </p>
            </div>

            <div className="flex items-center gap-2 flex-wrap">
              {/* Owner Approval Button */}
              {currentUser.role === 'OWNER' && selectedPO.status === 'PENDING_APPROVAL' && (
                <>
                  <button
                    onClick={() => approvePurchaseOrder(selectedPO.id)}
                    className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1.5 shadow-xs"
                  >
                    <CheckCircle2 className="w-4 h-4" />
                    <span>Approve PO & Check Stock</span>
                  </button>
                  <button
                    onClick={() => setShowReturnModal(true)}
                    className="px-3 py-1.5 bg-rose-900/60 hover:bg-rose-900 text-rose-200 border border-rose-700/60 rounded text-xs transition-colors flex items-center gap-1"
                  >
                    <XCircle className="w-3.5 h-3.5" />
                    <span>Return for Revision</span>
                  </button>
                </>
              )}

              {/* Owner Cancellation Approval */}
              {currentUser.role === 'OWNER' && selectedPO.status === 'CANCEL_REQUESTED' && (
                <button
                  onClick={() => approveCancellation(selectedPO.id)}
                  className="px-3 py-1.5 bg-rose-600 hover:bg-rose-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1"
                >
                  <XCircle className="w-3.5 h-3.5" />
                  <span>Approve Cancellation & Release Stock</span>
                </button>
              )}

              {/* Godown Manager: Create Delivery Challan */}
              {['APPROVED', 'STOCK_RESERVATION', 'PARTIALLY_DELIVERED', 'NOT_DELIVERED'].includes(selectedPO.status) && (
                <button
                  onClick={() => setCurrentTab('delivery_challans')}
                  className="px-3 py-1.5 bg-blue-600 hover:bg-blue-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1.5 shadow-xs"
                >
                  <Truck className="w-3.5 h-3.5" />
                  <span>Create Delivery Challan</span>
                </button>
              )}

              {/* Request Cancellation for Main / Regional */}
              {!['CANCELLED', 'DELIVERED', 'CANCEL_REQUESTED'].includes(selectedPO.status) && (
                <button
                  onClick={() => setShowCancelModal(true)}
                  className="px-2.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded text-xs transition-colors"
                >
                  Request Cancel
                </button>
              )}

              <button
                onClick={() => setSelectedPOId(null)}
                className="p-1.5 text-slate-400 hover:text-slate-200 bg-slate-800 rounded"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Client Snapshot & Delivery Terms Card */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-slate-950 p-3.5 rounded-lg border border-slate-800 text-xs">
              <div className="text-[11px] font-mono uppercase text-slate-400 mb-2 flex items-center justify-between">
                <span>Client Master Snapshot (Fixed at Creation)</span>
                <span className="text-emerald-400">Immutable</span>
              </div>
              <div className="font-bold text-slate-100 text-sm">{selectedPO.clientSnapshot.companyName}</div>
              <div className="text-slate-400 mt-1 font-mono text-[11px] space-y-0.5">
                <div>GSTIN: <span className="text-slate-300">{selectedPO.clientSnapshot.gstin}</span></div>
                <div>PAN: <span className="text-slate-300">{selectedPO.clientSnapshot.pan}</span></div>
                <div>Contact: <span className="text-slate-300">{selectedPO.clientSnapshot.primaryContact.name} ({selectedPO.clientSnapshot.primaryContact.phone})</span></div>
              </div>
            </div>

            <div className="bg-slate-950 p-3.5 rounded-lg border border-slate-800 text-xs">
              <div className="text-[11px] font-mono uppercase text-slate-400 mb-2">
                Consignee Delivery Site Address
              </div>
              <div className="font-semibold text-slate-200">
                {selectedPO.clientSnapshot.deliveryAddress.siteName || selectedPO.clientSnapshot.deliveryAddress.label}
              </div>
              <div className="text-slate-400 mt-1 text-[11px]">
                {selectedPO.clientSnapshot.deliveryAddress.addressLine1 || selectedPO.clientSnapshot.deliveryAddress.street}, {selectedPO.clientSnapshot.deliveryAddress.city}, {selectedPO.clientSnapshot.deliveryAddress.state} - {selectedPO.clientSnapshot.deliveryAddress.pincode}
              </div>
              <div className="text-[11px] text-slate-400 mt-1 font-mono">
                Contact: {selectedPO.clientSnapshot.deliveryAddress.contactPerson || selectedPO.clientSnapshot.primaryContact.name} ({selectedPO.clientSnapshot.deliveryAddress.phone || selectedPO.clientSnapshot.primaryContact.phone})
              </div>
            </div>
          </div>

          {/* Line Items Table with Ordered vs Reserved vs Dispatched vs Delivered */}
          <div className="bg-slate-950 rounded-lg border border-slate-800 overflow-hidden">
            <div className="p-3 border-b border-slate-800 flex items-center justify-between">
              <span className="text-xs font-bold text-slate-200 uppercase tracking-wider font-mono">
                Machinery Items & Fulfillment Pipeline
              </span>
              <span className="text-[11px] text-slate-400">
                Total Valuation: <strong className="text-emerald-400 font-mono">₹{(selectedPO.totalAmountINR / 100000).toFixed(2)} Lakhs</strong>
              </span>
            </div>

            <table className="w-full text-left text-xs">
              <thead className="bg-slate-900 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
                <tr>
                  <th className="p-2.5">SKU & Model</th>
                  <th className="p-2.5 text-right">Unit Price</th>
                  <th className="p-2.5 text-center">Ordered</th>
                  <th className="p-2.5 text-center">Reserved</th>
                  <th className="p-2.5 text-center">Dispatched</th>
                  <th className="p-2.5 text-center">Delivered</th>
                  <th className="p-2.5 text-right">Total (INR)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {selectedPO.items.map((it) => (
                  <tr key={it.id}>
                    <td className="p-2.5">
                      <div className="font-mono font-semibold text-slate-100">{it.sku}</div>
                      <div className="text-[11px] text-slate-400">{it.productName}</div>
                    </td>
                    <td className="p-2.5 text-right font-mono text-slate-300">
                      ₹{it.unitPriceINR.toLocaleString('en-IN')}
                    </td>
                    <td className="p-2.5 text-center font-mono font-bold text-slate-100">
                      {it.quantity} {it.unit}
                    </td>
                    <td className="p-2.5 text-center font-mono font-semibold text-purple-400">
                      {it.reservedQuantity}
                    </td>
                    <td className="p-2.5 text-center font-mono font-semibold text-blue-400">
                      {it.dispatchedQuantity}
                    </td>
                    <td className="p-2.5 text-center font-mono font-semibold text-emerald-400">
                      {it.deliveredQuantity}
                    </td>
                    <td className="p-2.5 text-right font-mono font-bold text-slate-200">
                      ₹{(it.quantity * it.unitPriceINR).toLocaleString('en-IN')}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Version History & Audit Trail for this PO */}
          <div className="bg-slate-950 p-4 rounded-lg border border-slate-800">
            <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider font-mono mb-2 flex items-center gap-1.5">
              <History className="w-3.5 h-3.5 text-emerald-400" />
              <span>Purchase Order Version History & Approvals</span>
            </h3>
            <div className="space-y-2">
              {selectedPO.versions.map((ver) => (
                <div key={ver.versionNumber} className="text-xs border-l-2 border-slate-700 pl-3 py-1">
                  <div className="flex items-center justify-between text-[11px] text-slate-400 font-mono">
                    <span className="font-bold text-slate-200">v{ver.versionNumber} - {ver.createdBy}</span>
                    <span>{ver.createdAt}</span>
                  </div>
                  <div className="text-slate-300 text-xs mt-0.5">{ver.reason}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Return for Revision Modal */}
      {showReturnModal && selectedPO && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-md w-full space-y-4">
            <h3 className="text-sm font-bold text-slate-100 flex items-center gap-2">
              <XCircle className="w-4 h-4 text-rose-400" />
              <span>Return PO {selectedPO.poNumber} for Revision</span>
            </h3>
            <p className="text-xs text-slate-400">
              Provide feedback or technical revision reasons to Main Office / Outer State sales team.
            </p>
            <textarea
              value={returnReason}
              onChange={(e) => setReturnReason(e.target.value)}
              placeholder="e.g. Please clarify electrical phase specification (415V 3-Phase vs 220V) with client..."
              className="w-full h-24 p-2.5 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-rose-500"
            />
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setShowReturnModal(false)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded text-xs"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  returnPurchaseOrder(selectedPO.id, returnReason || 'Returned by Owner');
                  setShowReturnModal(false);
                }}
                className="px-3 py-1.5 bg-rose-600 hover:bg-rose-500 text-white rounded text-xs font-semibold"
              >
                Submit Return
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Cancel Request Modal */}
      {showCancelModal && selectedPO && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-md w-full space-y-4">
            <h3 className="text-sm font-bold text-slate-100 flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-amber-400" />
              <span>Request Cancellation for {selectedPO.poNumber}</span>
            </h3>
            <p className="text-xs text-slate-400">
              Submit cancellation request. Note: Cancellation will release all locked physical inventory upon Owner approval.
            </p>
            <textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="Reason for cancellation (e.g. Client plant commissioning delayed by 6 months)..."
              className="w-full h-24 p-2.5 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none"
            />
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setShowCancelModal(false)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded text-xs"
              >
                Close
              </button>
              <button
                onClick={() => {
                  requestCancelPurchaseOrder(selectedPO.id, cancelReason || 'Cancellation requested');
                  setShowCancelModal(false);
                }}
                className="px-3 py-1.5 bg-amber-600 hover:bg-amber-500 text-white rounded text-xs font-semibold"
              >
                Send to Owner
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Create Purchase Order Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <h2 className="text-base font-bold text-slate-100">Create New Purchase Order</h2>
                <p className="text-xs text-slate-400">
                  Rule 1: PO will not affect godown stock before Owner approval.
                </p>
              </div>
              <button onClick={() => setShowCreateModal(false)} className="text-slate-400 hover:text-slate-200">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Select Client *</label>
                  <select
                    value={newPOClientId}
                    onChange={(e) => setNewPOClientId(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none"
                  >
                    {clients.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.companyName} ({c.gstin})
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-slate-300 font-medium mb-1">Priority</label>
                  <select
                    value={newPOPriority}
                    onChange={(e) => setNewPOPriority(e.target.value as Priority)}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none"
                  >
                    <option value="NORMAL">Normal</option>
                    <option value="HIGH">High Priority</option>
                    <option value="URGENT">Urgent (Express)</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Target Expected Delivery Date</label>
                <input
                  type="date"
                  value={newPODeliveryDate}
                  onChange={(e) => setNewPODeliveryDate(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono focus:outline-none"
                />
              </div>

              {/* Line Items */}
              <div className="space-y-2 pt-2 border-t border-slate-800">
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-slate-200">Machinery Line Items</span>
                  <button
                    type="button"
                    onClick={handleAddItemToForm}
                    className="text-emerald-400 hover:text-emerald-300 text-xs font-semibold"
                  >
                    + Add Another Product
                  </button>
                </div>

                {newPOItems.map((item, idx) => {
                  const prod = products.find((p) => p.id === item.productId);
                  return (
                    <div key={idx} className="p-3 bg-slate-950 rounded border border-slate-800 space-y-2">
                      <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                        <div className="sm:col-span-2">
                          <label className="text-[10px] text-slate-400">Product Model</label>
                          <select
                            value={item.productId}
                            onChange={(e) => {
                              const p = products.find(prod => prod.id === e.target.value);
                              const updated = [...newPOItems];
                              updated[idx].productId = e.target.value;
                              if (p) updated[idx].unitPriceINR = p.basePriceINR;
                              setNewPOItems(updated);
                            }}
                            className="w-full bg-slate-900 border border-slate-800 rounded p-1.5 text-slate-100 text-xs"
                          >
                            {products.map((p) => (
                              <option key={p.id} value={p.id}>
                                {p.sku} - {p.name} (₹{(p.basePriceINR / 100000).toFixed(2)}L)
                              </option>
                            ))}
                          </select>
                        </div>

                        <div>
                          <label className="text-[10px] text-slate-400">Quantity</label>
                          <input
                            type="number"
                            min="1"
                            value={item.quantity}
                            onChange={(e) => {
                              const updated = [...newPOItems];
                              updated[idx].quantity = parseInt(e.target.value) || 1;
                              setNewPOItems(updated);
                            }}
                            className="w-full bg-slate-900 border border-slate-800 rounded p-1.5 text-slate-100 font-mono text-xs"
                          />
                        </div>
                      </div>

                      <div className="flex items-center justify-between text-[11px] text-slate-400 font-mono">
                        <span>Unit Price: ₹{item.unitPriceINR.toLocaleString('en-IN')}</span>
                        <span>Item Total: ₹{(item.quantity * item.unitPriceINR).toLocaleString('en-IN')}</span>
                        {newPOItems.length > 1 && (
                          <button
                            type="button"
                            onClick={() => handleRemoveItemFromForm(idx)}
                            className="text-rose-400 hover:underline"
                          >
                            Remove
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Commercial / Technical Notes</label>
                <textarea
                  value={newPONotes}
                  onChange={(e) => setNewPONotes(e.target.value)}
                  placeholder="e.g. Turnkey installation included. Requires Siemens motor upgrade."
                  className="w-full h-16 bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none"
                />
              </div>
            </div>

            <div className="flex justify-between items-center pt-3 border-t border-slate-800">
              <span className="text-xs font-mono text-slate-300">
                Total: ₹
                {newPOItems
                  .reduce((acc, it) => acc + it.quantity * it.unitPriceINR, 0)
                  .toLocaleString('en-IN')}
              </span>

              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => handleCreateSubmit(false)}
                  className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded text-xs transition-colors"
                >
                  Save Draft
                </button>
                <button
                  type="button"
                  onClick={() => handleCreateSubmit(true)}
                  className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold transition-colors shadow-xs"
                >
                  Submit for Owner Approval
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
