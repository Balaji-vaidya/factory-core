import React, { useState } from 'react';
import { 
  FileText, 
  Search, 
  Filter, 
  Download, 
  Eye, 
  FileCheck, 
  Truck, 
  Paperclip, 
  Upload,
  Calendar,
  X 
} from 'lucide-react';
import { useApp } from '../../context/AppContext';

export const DocumentsManagement: React.FC = () => {
  const { showToast } = useApp();
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');

  const documents = [
    { id: 'DOC-1001', name: 'PO_PO-2026-0891_Mahyco_Agreement.pdf', category: 'PURCHASE_ORDER', size: '1.8 MB', date: '2026-09-18', ref: 'PO-2026-0891', uploader: 'Vikram Joshi' },
    { id: 'DOC-1002', name: 'DeliveryChallan_DC-2026-0412_Signed.pdf', category: 'DELIVERY_CHALLAN', size: '2.4 MB', date: '2026-09-22', ref: 'DC-2026-0412', uploader: 'Ramesh Yadav' },
    { id: 'DOC-1003', name: 'Proof_of_Delivery_POD-2026-0301_SiteSignoff.pdf', category: 'POD', size: '3.1 MB', date: '2026-09-23', ref: 'DEL-2026-0301', uploader: 'Anand Patil' },
    { id: 'DOC-1004', name: 'CAD_Engineering_Spec_AG-GS-10_Rev3.dwg', category: 'TECHNICAL_SPEC', size: '8.5 MB', date: '2026-08-10', ref: 'AG-GS-10', uploader: 'Engineering Dept' },
    { id: 'DOC-1005', name: 'Tax_Invoice_INV-2026-0944_KaveriSeeds.pdf', category: 'TAX_INVOICE', size: '1.2 MB', date: '2026-09-15', ref: 'PO-2026-0888', uploader: 'Accounts Office' },
    { id: 'DOC-1006', name: 'EWay_Bill_981244589102_KA_to_MH.pdf', category: 'EWAY_BILL', size: '640 KB', date: '2026-09-22', ref: 'DC-2026-0412', uploader: 'Ramesh Yadav' },
    { id: 'DOC-1007', name: 'Agrisep_Corporate_Machinery_Brochure_2026.pdf', category: 'BROCHURE', size: '14.2 MB', date: '2026-07-01', ref: 'MARKETING', uploader: 'Main Office' },
  ];

  const filteredDocs = documents.filter((doc) => {
    const matchesSearch = 
      doc.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      doc.ref.toLowerCase().includes(searchQuery.toLowerCase()) ||
      doc.uploader.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCat = categoryFilter === 'ALL' || doc.category === categoryFilter;
    return matchesSearch && matchesCat;
  });

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
              <FileText className="w-5 h-5 text-emerald-400" />
              <span>Digital Document Vault & Attachment Repository</span>
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
              {documents.length} Encrypted Files
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Section 19: Centralized repository for Purchase Order contracts, signed Delivery Challans, E-Way bills, tax invoices, and proof-of-delivery receipts.
          </p>
        </div>

        <button
          onClick={() => showToast('Select PO or DC to attach supporting document', 'info')}
          className="px-3.5 py-2 bg-slate-800 hover:bg-slate-750 text-slate-200 border border-slate-700 rounded text-xs transition-colors flex items-center gap-1.5 shadow-xs"
        >
          <Upload className="w-4 h-4" />
          <span>Upload Document</span>
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
            placeholder="Search document name, PO #, DC #, uploader..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none"
          />
        </div>

        <select
          value={categoryFilter}
          onChange={(e) => setCategoryFilter(e.target.value)}
          className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded px-2.5 py-1.5 focus:outline-none"
        >
          <option value="ALL">All Categories</option>
          <option value="PURCHASE_ORDER">Purchase Orders</option>
          <option value="DELIVERY_CHALLAN">Delivery Challans</option>
          <option value="POD">Proof of Delivery (POD)</option>
          <option value="TAX_INVOICE">Tax Invoices</option>
          <option value="EWAY_BILL">E-Way Bills</option>
          <option value="TECHNICAL_SPEC">Technical CAD Specs</option>
        </select>
      </div>

      {/* Documents Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden shadow-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 font-mono text-[10px] uppercase border-b border-slate-800">
              <tr>
                <th className="p-3">File Name</th>
                <th className="p-3">Category</th>
                <th className="p-3">Linked Ref #</th>
                <th className="p-3 text-right">Size</th>
                <th className="p-3">Upload Date</th>
                <th className="p-3">Uploaded By</th>
                <th className="p-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {filteredDocs.map((doc) => (
                <tr key={doc.id} className="hover:bg-slate-850/50">
                  <td className="p-3 font-medium text-slate-200 flex items-center gap-2">
                    <Paperclip className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                    <span className="font-mono text-[11px] truncate max-w-sm">{doc.name}</span>
                  </td>
                  <td className="p-3 font-mono text-[10px]">
                    <span className="px-1.5 py-0.5 rounded bg-slate-800 border border-slate-700 text-slate-300">
                      {doc.category.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="p-3 font-mono text-emerald-400">{doc.ref}</td>
                  <td className="p-3 text-right font-mono text-slate-400 text-[11px]">{doc.size}</td>
                  <td className="p-3 font-mono text-slate-300 text-[11px]">{doc.date}</td>
                  <td className="p-3 text-slate-300">{doc.uploader}</td>
                  <td className="p-3 text-right">
                    <button
                      onClick={() => showToast(`Downloading ${doc.name}`, 'info')}
                      className="p-1 text-slate-400 hover:text-emerald-400 transition-colors inline-block"
                      title="Download"
                    >
                      <Download className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
