import React, { useState } from 'react';
import { 
  Building, 
  MapPin, 
  Phone, 
  Mail, 
  Plus, 
  CheckCircle2, 
  Globe2,
  X 
} from 'lucide-react';
import { useApp } from '../../context/AppContext';

export const OfficesManagement: React.FC = () => {
  const { offices, showToast } = useApp();
  const [showAddModal, setShowAddModal] = useState(false);

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
              <Building className="w-5 h-5 text-emerald-400" />
              <span>Offices, Factories & Regional State Facilities</span>
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
              {offices.length} Facilities Active
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Section 22: Operating enterprise centers: Head Office Bangalore, Peenya Godown Yard, and Outer State branches in Hyderabad, Pune, and Coimbatore.
          </p>
        </div>

        <button
          onClick={() => setShowAddModal(true)}
          className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1.5 shadow-xs"
        >
          <Plus className="w-4 h-4" />
          <span>+ Add State Office</span>
        </button>
      </div>

      {/* Offices Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {offices.map((office) => (
          <div key={office.id} className="bg-slate-900 border border-slate-800 rounded-lg p-4 space-y-3">
            <div className="flex items-start justify-between">
              <div>
                <span className="text-[10px] font-mono uppercase text-emerald-400 font-bold bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-800/40">
                  {office.type.replace(/_/g, ' ')}
                </span>
                <h2 className="text-base font-bold text-slate-100 mt-2">{office.name}</h2>
                <div className="text-xs text-slate-400 font-mono mt-0.5">
                  State Territory: <strong className="text-slate-200">{office.state}</strong>
                </div>
              </div>

              <span className="text-xs text-slate-400 font-mono">{office.id}</span>
            </div>

            <div className="p-3 bg-slate-950 rounded border border-slate-800 text-xs space-y-1.5">
              <div className="flex items-center gap-2 text-slate-300">
                <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                <span>{office.address}</span>
              </div>
              <div className="flex items-center gap-2 text-slate-400 font-mono text-[11px]">
                <Phone className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                <span>{office.phone}</span>
              </div>
              <div className="flex items-center gap-2 text-slate-400 font-mono text-[11px]">
                <Mail className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                <span>{office.email}</span>
              </div>
            </div>

            <div className="flex items-center justify-between text-[11px] text-slate-400 font-mono pt-1">
              <span>GSTIN: <strong className="text-slate-300">{office.gstin}</strong></span>
              <span className="text-emerald-400 flex items-center gap-1">
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Operational</span>
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Add Office Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-md w-full space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-2">
              <h3 className="text-sm font-bold text-slate-100">Add Regional State Office</h3>
              <button onClick={() => setShowAddModal(false)} className="text-slate-400 hover:text-slate-200">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">Facility Name</label>
                <input
                  type="text"
                  placeholder="e.g. Agrisep Regional Office - Ahmedabad"
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Operating State Jurisdiction</label>
                <input
                  type="text"
                  placeholder="e.g. Gujarat"
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">State GSTIN</label>
                <input
                  type="text"
                  placeholder="24AAACI4921C1Z1"
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono uppercase"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Physical Address</label>
                <textarea
                  placeholder="Street, City, Pincode"
                  className="w-full h-16 bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                />
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
                  showToast('Regional office registered', 'success');
                  setShowAddModal(false);
                }}
                className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold"
              >
                Save Facility
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
