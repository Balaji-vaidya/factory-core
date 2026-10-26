import React, { useState } from 'react';
import { 
  CheckSquare, 
  Search, 
  Filter, 
  CheckCircle2, 
  Truck, 
  Calendar, 
  User, 
  Phone, 
  FileCheck, 
  ShieldCheck,
  X
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { Badge } from '../common/Badge';

export const DeliveriesManagement: React.FC = () => {
  const { 
    deliveries, 
    deliveryChallans, 
    purchaseOrders, 
    confirmDelivery, 
    setCurrentTab,
    showToast 
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [showConfirmModal, setShowConfirmModal] = useState(false);

  // In-transit DCs eligible for confirmation
  const inTransitDCs = deliveryChallans.filter((d) => d.status === 'DISPATCHED');

  const [selectedDCIdForConfirm, setSelectedDCIdForConfirm] = useState(inTransitDCs[0]?.id || '');
  const [deliveryDate, setDeliveryDate] = useState(new Date().toISOString().slice(0, 10));
  const [receivedBy, setReceivedBy] = useState('Anand Patil (Site Incharge)');
  const [receiverContact, setReceiverContact] = useState('+91 94221 88902');
  const [remarks, setRemarks] = useState('Machines unloaded at client seed plant bay. Physical inspection satisfactory without transit damage.');

  const filteredDeliveries = deliveries.filter((d) => {
    return (
      d.dcNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      d.poNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      d.clientName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      d.receivedBy.toLowerCase().includes(searchQuery.toLowerCase())
    );
  });

  const handleConfirmSubmit = () => {
    if (!selectedDCIdForConfirm) {
      showToast('Select an in-transit Delivery Challan to confirm', 'error');
      return;
    }
    if (!receivedBy.trim()) {
      showToast('Receiver name is mandatory for signed POD audit', 'error');
      return;
    }

    confirmDelivery({
      dcId: selectedDCIdForConfirm,
      deliveryDate,
      receivedBy,
      receiverContact,
      remarks,
    });

    setShowConfirmModal(false);
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
              <CheckSquare className="w-5 h-5 text-emerald-400" />
              <span>Deliveries & Proof of Delivery (POD)</span>
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-emerald-950/80 text-emerald-400 border border-emerald-800/50">
              {deliveries.length} Verified Deliveries
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Rules 8 & 11: Supports partial deliveries. Purchase Order lifecycle transitions to DELIVERED when 100% of line items are verified on site.
          </p>
        </div>

        {inTransitDCs.length > 0 && (
          <button
            onClick={() => {
              setSelectedDCIdForConfirm(inTransitDCs[0].id);
              setShowConfirmModal(true);
            }}
            className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1.5 shadow-xs shrink-0"
          >
            <FileCheck className="w-4 h-4" />
            <span>Confirm Delivery & Sign POD</span>
          </button>
        )}
      </div>

      {/* In-Transit Alert Strip */}
      <div className="bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <Truck className="w-4 h-4 text-cyan-400" />
            <span className="text-xs font-bold text-slate-200 uppercase tracking-wider font-mono">
              Consignments In-Transit on the Road ({inTransitDCs.length})
            </span>
          </div>
          <span className="text-[11px] text-slate-400 font-mono">
            Requires site arrival confirmation
          </span>
        </div>

        {inTransitDCs.length === 0 ? (
          <div className="text-xs text-slate-400 py-3">
            No trucks currently in transit. All dispatched consignments have been confirmed delivered.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {inTransitDCs.map((dc) => (
              <div key={dc.id} className="p-3 bg-slate-950 rounded border border-slate-800 text-xs flex items-center justify-between gap-3">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-bold text-cyan-400">{dc.dcNumber}</span>
                    <span className="text-slate-400 font-mono">PO: {dc.poNumber}</span>
                    <Badge status="DISPATCHED" />
                  </div>
                  <div className="text-slate-100 font-semibold mt-1">{dc.clientName}</div>
                  <div className="text-[11px] text-slate-400 font-mono mt-0.5">
                    Vehicle: {dc.vehicleNumber} ({dc.transporterName}) · {dc.totalQuantity} Units
                  </div>
                </div>

                <button
                  onClick={() => {
                    setSelectedDCIdForConfirm(dc.id);
                    setShowConfirmModal(true);
                  }}
                  className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold shrink-0 shadow-xs"
                >
                  Confirm Arrival
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Verified Delivery Records Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden shadow-xs">
        <div className="p-3 border-b border-slate-800 flex items-center justify-between bg-slate-950/40">
          <span className="text-xs font-bold text-slate-200 uppercase tracking-wider font-mono">
            Historical Proof of Delivery Records
          </span>
          <div className="relative w-64">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-2" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search deliveries..."
              className="w-full pl-8 pr-2.5 py-1 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 focus:outline-none"
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
              <tr>
                <th className="p-3">Delivery ID</th>
                <th className="p-3">DC Number</th>
                <th className="p-3">PO Number</th>
                <th className="p-3">Client Consignee</th>
                <th className="p-3 text-center">Delivered Qty</th>
                <th className="p-3">Delivery Date</th>
                <th className="p-3">Received By</th>
                <th className="p-3">Status</th>
                <th className="p-3">Verified By</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {filteredDeliveries.map((del) => (
                <tr key={del.id} className="hover:bg-slate-850/50">
                  <td className="p-3 font-mono font-bold text-emerald-400">{del.id}</td>
                  <td className="p-3 font-mono text-cyan-400">{del.dcNumber}</td>
                  <td className="p-3 font-mono text-slate-300">{del.poNumber}</td>
                  <td className="p-3 font-medium text-slate-100">{del.clientName}</td>
                  <td className="p-3 text-center font-mono font-bold text-slate-100">
                    {del.deliveredQuantity} Units
                  </td>
                  <td className="p-3 font-mono text-slate-300">{del.deliveryDate}</td>
                  <td className="p-3 text-slate-300">
                    <div>{del.receivedBy}</div>
                    <div className="text-[10px] text-slate-400 font-mono">{del.receiverContact}</div>
                  </td>
                  <td className="p-3"><Badge status={del.status} /></td>
                  <td className="p-3 font-mono text-slate-400 text-[11px]">{del.verifiedBy}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Confirm Delivery Modal */}
      {showConfirmModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-md w-full space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-2">
              <h3 className="text-sm font-bold text-slate-100 flex items-center gap-1.5">
                <FileCheck className="w-4 h-4 text-emerald-400" />
                <span>Confirm Delivery & Proof of Delivery (POD)</span>
              </h3>
              <button onClick={() => setShowConfirmModal(false)} className="text-slate-400 hover:text-slate-200">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">Select Delivery Challan *</label>
                <select
                  value={selectedDCIdForConfirm}
                  onChange={(e) => setSelectedDCIdForConfirm(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                >
                  {inTransitDCs.map((dc) => (
                    <option key={dc.id} value={dc.id}>
                      {dc.dcNumber} - {dc.clientName} ({dc.totalQuantity} units, {dc.vehicleNumber})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Actual Date of Delivery *</label>
                <input
                  type="date"
                  value={deliveryDate}
                  onChange={(e) => setDeliveryDate(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Received By (Consignee In-Charge) *</label>
                <input
                  type="text"
                  value={receivedBy}
                  onChange={(e) => setReceivedBy(e.target.value)}
                  placeholder="e.g. Ramesh Patil (Plant Engineer)"
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Receiver Contact Phone</label>
                <input
                  type="text"
                  value={receiverContact}
                  onChange={(e) => setReceiverContact(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Site Receipt Inspection Remarks</label>
                <textarea
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  className="w-full h-18 bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-2 border-t border-slate-800">
              <button
                onClick={() => setShowConfirmModal(false)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded text-xs"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmSubmit}
                className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold shadow-xs"
              >
                Verify & Complete Delivery
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
