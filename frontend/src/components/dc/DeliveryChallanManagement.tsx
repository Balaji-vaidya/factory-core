import React, { useState } from 'react';
import { 
  Truck, 
  Plus, 
  Search, 
  Filter, 
  CheckCircle2, 
  Clock, 
  FileText, 
  Send, 
  AlertTriangle, 
  Printer, 
  Building,
  User,
  Phone,
  ShieldCheck,
  X
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { DeliveryChallan } from '../../types';
import { Badge } from '../common/Badge';

export const DeliveryChallanManagement: React.FC = () => {
  const { 
    deliveryChallans, 
    purchaseOrders, 
    currentUser, 
    createDeliveryChallan, 
    selectedDCId, 
    setSelectedDCId,
    setCurrentTab,
    showToast 
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [showCreateModal, setShowCreateModal] = useState(false);

  // DC Creation State
  const eligiblePOs = purchaseOrders.filter((po) => 
    ['APPROVED', 'STOCK_RESERVATION', 'NOT_DELIVERED', 'PARTIALLY_DELIVERED'].includes(po.status) &&
    po.items.some((i) => i.reservedQuantity > 0 && i.quantity > i.dispatchedQuantity)
  );

  const [selectedPOIdForDC, setSelectedPOIdForDC] = useState<string>(eligiblePOs[0]?.id || purchaseOrders[0]?.id || '');
  const [vehicleNumber, setVehicleNumber] = useState('KA-04-AB-8192');
  const [transporterName, setTransporterName] = useState('VRL Logistics Agro Division');
  const [driverName, setDriverName] = useState('Manjunath Gowda');
  const [driverPhone, setDriverPhone] = useState('+91 94812 34567');
  const [ewayBillNumber, setEwayBillNumber] = useState(`981244589${Math.floor(100 + Math.random() * 899)}`);
  const [expectedArrivalDate, setExpectedArrivalDate] = useState('2026-10-15');
  const [dcNotes, setDcNotes] = useState('');

  // Items to dispatch (prefilled from PO)
  const activePOForDC = purchaseOrders.find((p) => p.id === selectedPOIdForDC);
  const [dispatchItemQuantities, setDispatchItemQuantities] = useState<Record<string, number>>({});

  // When PO changes in form, reset quantities
  const handlePOSelectChange = (poId: string) => {
    setSelectedPOIdForDC(poId);
    const po = purchaseOrders.find((p) => p.id === poId);
    if (po) {
      const initialQtys: Record<string, number> = {};
      po.items.forEach((item) => {
        const remaining = item.quantity - item.dispatchedQuantity;
        // Default to what is reserved and not dispatched
        initialQtys[item.productId] = Math.min(item.reservedQuantity, remaining);
      });
      setDispatchItemQuantities(initialQtys);
    }
  };

  const selectedDC = deliveryChallans.find((d) => d.id === selectedDCId);

  const filteredDCs = deliveryChallans.filter((dc) => {
    const matchesSearch = 
      dc.dcNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      dc.poNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      dc.clientName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      dc.vehicleNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      dc.ewayBillNumber.includes(searchQuery);
    const matchesStatus = statusFilter === 'ALL' || dc.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleCreateDCSubmit = () => {
    if (!activePOForDC) {
      showToast('Select an approved purchase order first', 'error');
      return;
    }

    const itemsToDispatch = Object.entries(dispatchItemQuantities).map(([productId, qty]) => ({
      productId,
      dispatchQuantity: qty,
    })).filter(it => it.dispatchQuantity > 0);

    if (itemsToDispatch.length === 0) {
      showToast('Specify at least one item with dispatch quantity > 0', 'error');
      return;
    }

    const res = createDeliveryChallan({
      poId: activePOForDC.id,
      items: itemsToDispatch,
      vehicleNumber,
      transporterName,
      driverName,
      driverPhone,
      ewayBillNumber,
      expectedArrivalDate,
      notes: dcNotes,
    });

    if (res.success && res.dc) {
      setShowCreateModal(false);
      setSelectedDCId(res.dc.id);
    } else {
      showToast(res.message, 'error');
    }
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
              <Truck className="w-5 h-5 text-blue-400" />
              <span>Delivery Challans (DC) & Shipping Consignments</span>
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
              {deliveryChallans.length} Challans Generated
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Rules 7, 9 & 10: Supports multiple partial DCs per PO. Dispatch quantity is strictly restricted to reserved quantity.
          </p>
        </div>

        <button
          onClick={() => {
            setShowCreateModal(true);
            if (activePOForDC) handlePOSelectChange(activePOForDC.id);
          }}
          className="px-3.5 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1.5 shadow-xs shrink-0"
        >
          <Plus className="w-4 h-4" />
          <span>+ Create Delivery Challan</span>
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
            placeholder="Search by DC number, PO #, client, vehicle number, E-way bill..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded px-2.5 py-1.5 focus:outline-none"
        >
          <option value="ALL">All DC Statuses</option>
          <option value="PENDING_APPROVAL">Pending Dispatch Approval</option>
          <option value="DISPATCHED">Dispatched / In-Transit</option>
          <option value="DELIVERED">Delivered</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
      </div>

      {/* DC Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden shadow-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
              <tr>
                <th className="p-3">DC Number</th>
                <th className="p-3">Target PO #</th>
                <th className="p-3">Client Consignee</th>
                <th className="p-3 text-center">Dispatch Qty</th>
                <th className="p-3">Vehicle Number</th>
                <th className="p-3">Transporter</th>
                <th className="p-3">E-Way Bill #</th>
                <th className="p-3">Status</th>
                <th className="p-3 text-right">Target Date</th>
                <th className="p-3 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {filteredDCs.length === 0 ? (
                <tr>
                  <td colSpan={10} className="p-8 text-center text-xs text-slate-400">
                    No delivery challans found.
                  </td>
                </tr>
              ) : (
                filteredDCs.map((dc) => (
                  <tr 
                    key={dc.id} 
                    onClick={() => setSelectedDCId(dc.id)}
                    className={`cursor-pointer transition-colors ${
                      selectedDCId === dc.id ? 'bg-slate-800/90 ring-1 ring-blue-500/40' : 'hover:bg-slate-850/50'
                    }`}
                  >
                    <td className="p-3 font-mono font-bold text-cyan-400">{dc.dcNumber}</td>
                    <td className="p-3 font-mono text-emerald-400">{dc.poNumber}</td>
                    <td className="p-3 font-medium text-slate-100">{dc.clientName}</td>
                    <td className="p-3 text-center font-mono font-bold text-slate-100">
                      {dc.totalQuantity} Units
                    </td>
                    <td className="p-3 font-mono text-slate-300">{dc.vehicleNumber}</td>
                    <td className="p-3 text-slate-300">{dc.transporterName}</td>
                    <td className="p-3 font-mono text-slate-400 text-[11px]">{dc.ewayBillNumber}</td>
                    <td className="p-3"><Badge status={dc.status} /></td>
                    <td className="p-3 text-right font-mono text-slate-300">{dc.expectedArrivalDate}</td>
                    <td className="p-3 text-center">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedDCId(dc.id);
                        }}
                        className="px-2 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded text-[11px]"
                      >
                        Inspect
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* DC Printable Preview Card */}
      {selectedDC && (
        <div className="bg-slate-900 border border-slate-800 rounded-lg p-5 space-y-4 shadow-xl">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-800 pb-3">
            <div>
              <div className="flex items-center gap-2">
                <span className="font-mono text-lg font-bold text-cyan-400">{selectedDC.dcNumber}</span>
                <Badge status={selectedDC.status} />
                <span className="text-xs text-slate-400 font-mono">PO: {selectedDC.poNumber}</span>
              </div>
              <p className="text-xs text-slate-400 mt-1">
                Generated by {selectedDC.createdBy} on {selectedDC.createdAt}
              </p>
            </div>

            <div className="flex items-center gap-2">
              {currentUser.role === 'OWNER' && selectedDC.status === 'PENDING_APPROVAL' && (
                <button
                  onClick={() => setCurrentTab('dispatch')}
                  className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold flex items-center gap-1.5 shadow-xs"
                >
                  <Send className="w-3.5 h-3.5" />
                  <span>Review in Dispatch Queue</span>
                </button>
              )}

              <button
                onClick={() => showToast('Printing Official Agrisep Delivery Challan (3 Copies: Consignor, Consignee, Transporter)...', 'info')}
                className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded text-xs flex items-center gap-1.5"
              >
                <Printer className="w-3.5 h-3.5" />
                <span>Print Challan</span>
              </button>

              <button
                onClick={() => setSelectedDCId(null)}
                className="p-1.5 text-slate-400 hover:text-slate-200 bg-slate-800 rounded"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Consignor, Consignee & Transporter Info */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
            <div className="p-3 bg-slate-950 rounded border border-slate-800">
              <div className="text-[10px] font-mono uppercase text-slate-400 mb-1">
                Consignor (Dispatch Godown)
              </div>
              <div className="font-bold text-slate-100">Indo American Equipment / Agrisep®</div>
              <div className="text-slate-400 text-[11px] mt-0.5">
                Plot 48/A, Peenya Industrial Area 2nd Stage, Bangalore - 560058
              </div>
              <div className="text-slate-400 text-[11px] font-mono mt-1">
                GSTIN: 29AAACI4921C1Z4
              </div>
            </div>

            <div className="p-3 bg-slate-950 rounded border border-slate-800">
              <div className="text-[10px] font-mono uppercase text-slate-400 mb-1">
                Consignee Delivery Site
              </div>
              <div className="font-bold text-slate-100">{selectedDC.clientName}</div>
              <div className="text-slate-400 text-[11px] mt-0.5">
                {selectedDC.deliveryAddress.siteName || selectedDC.deliveryAddress.street || selectedDC.deliveryAddress.label}
              </div>
              <div className="text-slate-400 text-[11px]">
                {selectedDC.deliveryAddress.city}, {selectedDC.deliveryAddress.state} - {selectedDC.deliveryAddress.pincode}
              </div>
            </div>

            <div className="p-3 bg-slate-950 rounded border border-slate-800">
              <div className="text-[10px] font-mono uppercase text-slate-400 mb-1">
                Transport & Logistics Details
              </div>
              <div className="font-mono text-slate-200">Vehicle: <strong className="text-emerald-400">{selectedDC.vehicleNumber}</strong></div>
              <div className="text-slate-300 mt-0.5">Carrier: {selectedDC.transporterName}</div>
              <div className="text-slate-400 font-mono text-[11px] mt-0.5">Driver: {selectedDC.driverName} ({selectedDC.driverPhone})</div>
              <div className="text-slate-400 font-mono text-[11px]">E-Way Bill: {selectedDC.ewayBillNumber}</div>
            </div>
          </div>

          {/* DC Line Items */}
          <div className="bg-slate-950 rounded border border-slate-800 overflow-hidden">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-900 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
                <tr>
                  <th className="p-2.5">Model / SKU</th>
                  <th className="p-2.5">Description</th>
                  <th className="p-2.5 text-center">PO Quantity</th>
                  <th className="p-2.5 text-center">Reserved</th>
                  <th className="p-2.5 text-center font-bold text-cyan-400">Dispatched in this DC</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {selectedDC.items.map((it) => (
                  <tr key={it.productId}>
                    <td className="p-2.5 font-mono font-bold text-slate-100">{it.sku}</td>
                    <td className="p-2.5 text-slate-300">{it.productName}</td>
                    <td className="p-2.5 text-center font-mono text-slate-400">{it.orderedQuantity}</td>
                    <td className="p-2.5 text-center font-mono text-purple-400">{it.reservedQuantity}</td>
                    <td className="p-2.5 text-center font-mono font-bold text-cyan-400 text-sm">
                      {it.dispatchQuantity} {it.unit}s
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Create Delivery Challan Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <h2 className="text-base font-bold text-slate-100 flex items-center gap-2">
                  <Truck className="w-4 h-4 text-blue-400" />
                  <span>Generate Delivery Challan</span>
                </h2>
                <p className="text-xs text-slate-400">
                  Rules 9 & 10: Dispatch cannot exceed reserved quantity or remaining PO quantity.
                </p>
              </div>
              <button onClick={() => setShowCreateModal(false)} className="text-slate-400 hover:text-slate-200">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-4 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">
                  Target Approved Purchase Order *
                </label>
                <select
                  value={selectedPOIdForDC}
                  onChange={(e) => handlePOSelectChange(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none"
                >
                  {eligiblePOs.map((po) => (
                    <option key={po.id} value={po.id}>
                      {po.poNumber} - {po.clientName} (Status: {po.status} | Res: {po.reservationStatus})
                    </option>
                  ))}
                </select>
              </div>

              {/* Items for this PO */}
              {activePOForDC && (
                <div className="bg-slate-950 p-3 rounded border border-slate-800 space-y-2">
                  <span className="font-semibold text-slate-200">Machinery Allocation to Dispatch</span>
                  <div className="space-y-2">
                    {activePOForDC.items.map((item) => {
                      const remaining = item.quantity - item.dispatchedQuantity;
                      const maxDispatchable = Math.min(item.reservedQuantity, remaining);
                      const currentVal = dispatchItemQuantities[item.productId] ?? 0;

                      return (
                        <div key={item.productId} className="p-2 bg-slate-900 rounded border border-slate-800 flex items-center justify-between gap-3">
                          <div>
                            <div className="font-mono font-bold text-slate-100">{item.sku}</div>
                            <div className="text-[11px] text-slate-400 font-mono">
                              Ordered: {item.quantity} | Dispatched so far: {item.dispatchedQuantity} | Reserved: <strong className="text-purple-400">{item.reservedQuantity}</strong>
                            </div>
                          </div>

                          <div className="flex items-center gap-2">
                            <label className="text-[11px] text-slate-400">Qty to Dispatch:</label>
                            <input
                              type="number"
                              min="0"
                              max={maxDispatchable}
                              value={currentVal}
                              onChange={(e) => {
                                const v = parseInt(e.target.value) || 0;
                                setDispatchItemQuantities({
                                  ...dispatchItemQuantities,
                                  [item.productId]: Math.min(maxDispatchable, Math.max(0, v)),
                                });
                              }}
                              className="w-20 bg-slate-950 border border-slate-700 rounded p-1 text-center font-mono text-sm text-cyan-400 font-bold"
                            />
                            <span className="text-[10px] text-slate-400 font-mono">(Max {maxDispatchable})</span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* Transport and Carrier Fields */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Vehicle Number *</label>
                  <input
                    type="text"
                    value={vehicleNumber}
                    onChange={(e) => setVehicleNumber(e.target.value)}
                    placeholder="e.g. KA-04-AB-8192"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono uppercase"
                  />
                </div>

                <div>
                  <label className="block text-slate-300 font-medium mb-1">Transporter / Logistics Firm *</label>
                  <input
                    type="text"
                    value={transporterName}
                    onChange={(e) => setTransporterName(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                  />
                </div>

                <div>
                  <label className="block text-slate-300 font-medium mb-1">Driver Name</label>
                  <input
                    type="text"
                    value={driverName}
                    onChange={(e) => setDriverName(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                  />
                </div>

                <div>
                  <label className="block text-slate-300 font-medium mb-1">Driver Phone</label>
                  <input
                    type="text"
                    value={driverPhone}
                    onChange={(e) => setDriverPhone(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono"
                  />
                </div>

                <div>
                  <label className="block text-slate-300 font-medium mb-1">E-Way Bill Number *</label>
                  <input
                    type="text"
                    value={ewayBillNumber}
                    onChange={(e) => setEwayBillNumber(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono"
                  />
                </div>

                <div>
                  <label className="block text-slate-300 font-medium mb-1">Expected Site Arrival Date</label>
                  <input
                    type="date"
                    value={expectedArrivalDate}
                    onChange={(e) => setExpectedArrivalDate(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Gate Pass / Loading Notes</label>
                <textarea
                  value={dcNotes}
                  onChange={(e) => setDcNotes(e.target.value)}
                  placeholder="e.g. Tarpaulin cover inspected. Includes standard tool box and spare drive belts."
                  className="w-full h-16 bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
              <button
                type="button"
                onClick={() => setShowCreateModal(false)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded text-xs"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleCreateDCSubmit}
                className="px-4 py-1.5 bg-blue-600 hover:bg-blue-500 text-white rounded text-xs font-semibold shadow-xs"
              >
                Generate DC & Request Dispatch Clearance
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
