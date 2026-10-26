import React, { useState } from 'react';
import { 
  Building2, 
  Search, 
  Plus, 
  MapPin, 
  FileText, 
  X,
  Loader2,
  Check,
  AlertCircle
} from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApp } from '../../context/AppContext';
import { 
  clientApi, 
  ClientCreateDto, 
  ClientResponseDto, 
  ClientAddressDto 
} from '../../services/client-api';

const INDIAN_STATES = [
  'Maharashtra',
  'Telangana',
  'Andhra Pradesh',
  'Gujarat',
  'Karnataka',
  'Madhya Pradesh',
  'Tamil Nadu',
  'Rajasthan',
  'Punjab',
  'Haryana',
  'Uttar Pradesh',
  'West Bengal'
];

export const ClientManagement: React.FC = () => {
  const { 
    purchaseOrders, 
    selectedClientId, 
    setSelectedClientId, 
    setSelectedPOId, 
    setCurrentTab,
    showToast 
  } = useApp();

  const queryClient = useQueryClient();

  const [searchQuery, setSearchQuery] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showAddAddressModal, setShowAddAddressModal] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  // New Client Form State
  const [formData, setFormData] = useState<ClientCreateDto>({
    companyName: '',
    clientType: 'SEED_PRODUCER',
    gstin: '',
    pan: '',
    state: 'Maharashtra',
    primaryContactName: '',
    primaryContactDesignation: 'Procurement Officer',
    primaryContactPhone: '',
    primaryContactEmail: '',
    creditDays: 30,
    initialAddress: {
      siteName: 'Main Processing Plant',
      addressLine1: '',
      city: '',
      state: 'Maharashtra',
      pincode: '',
      contactPerson: '',
      phone: ''
    }
  });

  // New Address Form State
  const [addressData, setAddressData] = useState<ClientAddressDto>({
    siteName: '',
    addressLine1: '',
    city: '',
    state: 'Maharashtra',
    pincode: '',
    contactPerson: '',
    phone: '',
    isDefault: false
  });

  // TanStack Query: Fetch Clients from Backend
  const { data: clientsData, isLoading } = useQuery({
    queryKey: ['clients', searchQuery, stateFilter],
    queryFn: async () => {
      try {
        const res = await clientApi.getClients({
          query: searchQuery || undefined,
          state: stateFilter === 'ALL' ? undefined : stateFilter,
          page: 0,
          size: 50
        });
        return res.items;
      } catch {
        // Return null to allow fallback to context mock data if server is offline
        return null;
      }
    }
  });

  const { clients: contextClients } = useApp();

  // Combine live server data with context mock clients as resilient fallback
  const clientList: (ClientResponseDto | any)[] = clientsData && clientsData.length > 0
    ? clientsData
    : contextClients.filter((c) => {
        const matchesSearch = 
          c.companyName.toLowerCase().includes(searchQuery.toLowerCase()) ||
          c.gstin.toLowerCase().includes(searchQuery.toLowerCase()) ||
          c.pan.toLowerCase().includes(searchQuery.toLowerCase()) ||
          c.primaryContact.name.toLowerCase().includes(searchQuery.toLowerCase());
        const matchesState = stateFilter === 'ALL' || c.state === stateFilter;
        return matchesSearch && matchesState;
      });

  const selectedClient = clientList.find((c) => c.id === selectedClientId) || clientList[0];

  // TanStack Mutation: Create Client
  const createClientMutation = useMutation({
    mutationFn: (newClient: ClientCreateDto) => clientApi.createClient(newClient),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      setSelectedClientId(data.id);
      setShowCreateModal(false);
      setFormError(null);
      showToast(`Client ${data.companyName} registered successfully!`, 'success');
      // Reset form
      setFormData({
        companyName: '',
        clientType: 'SEED_PRODUCER',
        gstin: '',
        pan: '',
        state: 'Maharashtra',
        primaryContactName: '',
        primaryContactDesignation: 'Procurement Officer',
        primaryContactPhone: '',
        primaryContactEmail: '',
        creditDays: 30,
        initialAddress: {
          siteName: 'Main Processing Plant',
          addressLine1: '',
          city: '',
          state: 'Maharashtra',
          pincode: '',
          contactPerson: '',
          phone: ''
        }
      });
    },
    onError: (error: any) => {
      const serverMsg = error?.response?.data?.message || error?.message || 'Failed to register client';
      setFormError(serverMsg);
      showToast(serverMsg, 'error');
    }
  });

  // TanStack Mutation: Add Delivery Address
  const addAddressMutation = useMutation({
    mutationFn: (data: { clientId: string; address: ClientAddressDto }) =>
      clientApi.addDeliveryAddress(data.clientId, data.address),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      setShowAddAddressModal(false);
      showToast('New delivery site registered successfully', 'success');
      setAddressData({
        siteName: '',
        addressLine1: '',
        city: '',
        state: selectedClient?.state || 'Maharashtra',
        pincode: '',
        contactPerson: '',
        phone: '',
        isDefault: false
      });
    },
    onError: (error: any) => {
      showToast(error?.response?.data?.message || 'Failed to add delivery site', 'error');
    }
  });

  // TanStack Mutation: Set Default Address
  const setDefaultAddressMutation = useMutation({
    mutationFn: (data: { clientId: string; addressId: string }) =>
      clientApi.setDefaultAddress(data.clientId, data.addressId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      showToast('Default delivery site updated', 'success');
    },
    onError: (error: any) => {
      showToast(error?.response?.data?.message || 'Failed to set default site', 'error');
    }
  });

  // TanStack Mutation: Toggle Client Status
  const toggleStatusMutation = useMutation({
    mutationFn: (data: { clientId: string; status: 'ACTIVE' | 'INACTIVE' }) =>
      clientApi.updateStatus(data.clientId, data.status),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      showToast(`Client account status changed to ${data.status}`, 'info');
    },
    onError: (error: any) => {
      showToast(error?.response?.data?.message || 'Failed to update client status', 'error');
    }
  });

  const clientPOs = purchaseOrders.filter((po) => po.clientId === selectedClient?.id);

  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!formData.companyName.trim()) {
      setFormError('Company Registered Name is mandatory.');
      return;
    }
    if (!formData.primaryContactName.trim()) {
      setFormError('Primary Contact Name is mandatory.');
      return;
    }
    if (!formData.primaryContactPhone.trim()) {
      setFormError('Primary Contact Phone is mandatory.');
      return;
    }
    if (!formData.primaryContactEmail.trim()) {
      setFormError('Primary Contact Email is mandatory.');
      return;
    }

    createClientMutation.mutate(formData);
  };

  const handleAddAddressSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedClient) return;

    if (!addressData.siteName.trim() || !addressData.addressLine1.trim() || !addressData.city.trim() || !addressData.pincode.trim()) {
      showToast('Please fill all mandatory site address fields.', 'warning');
      return;
    }

    addAddressMutation.mutate({
      clientId: selectedClient.id,
      address: addressData
    });
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
              <Building2 className="w-5 h-5 text-purple-400" />
              <span>Client Master Registry</span>
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
              {clientList.length} Accounts
            </span>
            {isLoading && <Loader2 className="w-3.5 h-3.5 text-purple-400 animate-spin" />}
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Indo American Equipment / Agrisep®: Verified Indian corporate clients, GSTIN & PAN registry, delivery sites, and order tracking.
          </p>
        </div>

        <button
          onClick={() => {
            setFormError(null);
            setShowCreateModal(true);
          }}
          className="px-3.5 py-2 bg-purple-600 hover:bg-purple-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1.5 shadow-xs shrink-0 cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>+ Add Corporate Client</span>
        </button>
      </div>

      {/* Search & State Filter */}
      <div className="bg-slate-900 border border-slate-800 p-3 rounded-lg flex items-center gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by client company name, GSTIN, PAN, primary contact..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-purple-500"
          />
        </div>

        <select
          value={stateFilter}
          onChange={(e) => setStateFilter(e.target.value)}
          className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded px-2.5 py-1.5 focus:outline-none focus:border-purple-500"
        >
          <option value="ALL">All States</option>
          {INDIAN_STATES.map((st) => (
            <option key={st} value={st}>{st}</option>
          ))}
        </select>
      </div>

      {/* Two-Column Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Left: Client List */}
        <div className="lg:col-span-5 space-y-2">
          {clientList.length === 0 ? (
            <div className="p-8 text-center bg-slate-900 border border-slate-800 rounded-lg text-slate-400 text-xs">
              No clients found matching the search criteria.
            </div>
          ) : (
            clientList.map((client) => {
              const isSelected = selectedClient?.id === client.id;
              const contactName = client.primaryContact?.name || client.primaryContactName || 'N/A';
              const contactPhone = client.primaryContact?.phone || client.primaryContactPhone || 'N/A';
              const isActive = client.status === 'ACTIVE';

              return (
                <div
                  key={client.id}
                  onClick={() => setSelectedClientId(client.id)}
                  className={`p-3.5 rounded-lg border cursor-pointer transition-all ${
                    isSelected
                      ? 'bg-slate-800/90 border-purple-500/60 shadow-md ring-1 ring-purple-500/30'
                      : 'bg-slate-900 border-slate-800 hover:bg-slate-850'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-slate-100 text-xs">{client.companyName}</span>
                        {!isActive && (
                          <span className="text-[9px] px-1.5 py-0.2 rounded bg-rose-950/80 text-rose-300 border border-rose-800/40">
                            INACTIVE
                          </span>
                        )}
                      </div>
                      <div className="text-[11px] text-slate-400 font-mono mt-0.5">
                        GSTIN: <span className="text-slate-300">{client.gstin || 'UNREGISTERED'}</span>
                      </div>
                    </div>
                    <span className="text-[10px] font-mono text-purple-400 bg-purple-950/60 px-1.5 py-0.5 rounded border border-purple-800/40">
                      {client.state}
                    </span>
                  </div>

                  <div className="mt-2 text-[11px] text-slate-400 flex items-center gap-3">
                    <span>Contact: {contactName}</span>
                    <span>·</span>
                    <span className="font-mono">{contactPhone}</span>
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Right: Selected Client Profile & PO History */}
        {selectedClient ? (
          <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-lg p-5 space-y-5">
            {/* Header Info */}
            <div className="border-b border-slate-800 pb-4 flex items-start justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-[10px] font-mono uppercase text-purple-400 tracking-wider">
                    {selectedClient.clientType || 'Corporate Client Account'}
                  </span>
                  <span className={`text-[10px] font-mono px-2 py-0.5 rounded border ${
                    selectedClient.status === 'ACTIVE'
                      ? 'bg-emerald-950/60 text-emerald-300 border-emerald-800/40'
                      : 'bg-rose-950/60 text-rose-300 border-rose-800/40'
                  }`}>
                    {selectedClient.status || 'ACTIVE'}
                  </span>
                </div>
                <h2 className="text-base font-bold text-slate-100 mt-1">
                  {selectedClient.companyName}
                </h2>
                <div className="text-xs text-slate-400 mt-1 flex flex-wrap gap-x-4 gap-y-1 font-mono">
                  <span>GSTIN: <strong className="text-slate-200">{selectedClient.gstin || 'N/A'}</strong></span>
                  <span>PAN: <strong className="text-slate-200">{selectedClient.pan || 'N/A'}</strong></span>
                  <span>State: <strong className="text-slate-200">{selectedClient.state}</strong></span>
                  <span>Credit: <strong className="text-slate-200">{selectedClient.creditDays || 30} Days</strong></span>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => {
                    const newStatus = selectedClient.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
                    toggleStatusMutation.mutate({ clientId: selectedClient.id, status: newStatus });
                  }}
                  disabled={toggleStatusMutation.isPending}
                  className="px-2.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded text-xs font-medium border border-slate-700 transition-colors cursor-pointer"
                >
                  {selectedClient.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                </button>

                <button
                  onClick={() => {
                    setCurrentTab('purchase_orders');
                    showToast(`Creating Purchase Order for ${selectedClient.companyName}`, 'info');
                  }}
                  className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold shadow-xs transition-colors cursor-pointer"
                >
                  + New PO
                </button>
              </div>
            </div>

            {/* Contacts */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
              <div className="p-3 bg-slate-950 rounded border border-slate-800">
                <div className="font-semibold text-slate-200 uppercase tracking-wider text-[10px] mb-1.5">
                  Primary Commercial Contact
                </div>
                <div className="font-medium text-slate-100">
                  {selectedClient.primaryContact?.name || selectedClient.primaryContactName || 'N/A'}
                </div>
                <div className="text-slate-400 text-[11px]">
                  {selectedClient.primaryContact?.designation || selectedClient.primaryContactDesignation || 'Procurement Officer'}
                </div>
                <div className="text-slate-300 font-mono text-[11px] mt-1">
                  {selectedClient.primaryContact?.phone || selectedClient.primaryContactPhone || 'N/A'}
                </div>
                <div className="text-slate-400 font-mono text-[11px]">
                  {selectedClient.primaryContact?.email || selectedClient.primaryContactEmail || 'N/A'}
                </div>
              </div>

              <div className="p-3 bg-slate-950 rounded border border-slate-800">
                <div className="font-semibold text-slate-200 uppercase tracking-wider text-[10px] mb-1.5">
                  Accounts & Billing Contact
                </div>
                <div className="font-medium text-slate-100">
                  {selectedClient.billingContact?.name || selectedClient.billingContactName || selectedClient.primaryContact?.name || 'Same as Primary'}
                </div>
                <div className="text-slate-400 text-[11px]">
                  {selectedClient.billingContact?.designation || selectedClient.billingContactDesignation || 'Accounts Department'}
                </div>
                <div className="text-slate-300 font-mono text-[11px] mt-1">
                  {selectedClient.billingContact?.phone || selectedClient.billingContactPhone || selectedClient.primaryContact?.phone || ''}
                </div>
                <div className="text-slate-400 font-mono text-[11px]">
                  {selectedClient.billingContact?.email || selectedClient.billingContactEmail || selectedClient.primaryContact?.email || ''}
                </div>
              </div>
            </div>

            {/* Multiple Delivery Sites */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider font-mono flex items-center gap-1.5">
                  <MapPin className="w-3.5 h-3.5 text-purple-400" />
                  <span>Authorized Delivery Sites & Consignee Locations ({selectedClient.deliveryAddresses?.length || 0})</span>
                </h3>

                <button
                  onClick={() => {
                    setAddressData({
                      siteName: '',
                      addressLine1: '',
                      city: '',
                      state: selectedClient.state || 'Maharashtra',
                      pincode: '',
                      contactPerson: selectedClient.primaryContact?.name || '',
                      phone: selectedClient.primaryContact?.phone || '',
                      isDefault: (selectedClient.deliveryAddresses?.length || 0) === 0
                    });
                    setShowAddAddressModal(true);
                  }}
                  className="px-2 py-1 bg-purple-950/60 hover:bg-purple-900/60 text-purple-300 border border-purple-800/40 rounded text-[11px] font-medium flex items-center gap-1 transition-colors cursor-pointer"
                >
                  <Plus className="w-3 h-3" />
                  <span>Add Site</span>
                </button>
              </div>

              <div className="space-y-2">
                {(!selectedClient.deliveryAddresses || selectedClient.deliveryAddresses.length === 0) ? (
                  <div className="p-3 bg-slate-950 rounded border border-slate-800 text-xs text-slate-400">
                    No delivery sites recorded. Click "+ Add Site" to configure consignee delivery address.
                  </div>
                ) : (
                  selectedClient.deliveryAddresses.map((addr: any) => (
                    <div key={addr.id} className="p-3 bg-slate-950 rounded border border-slate-800 text-xs">
                      <div className="flex items-center justify-between">
                        <span className="font-semibold text-slate-100">{addr.siteName || addr.label}</span>
                        <div className="flex items-center gap-2">
                          {addr.isDefault ? (
                            <span className="text-[10px] font-mono text-emerald-400 bg-emerald-950/60 px-1.5 py-0.2 rounded border border-emerald-800/40 flex items-center gap-1">
                              <Check className="w-3 h-3" />
                              DEFAULT SITE
                            </span>
                          ) : (
                            <button
                              onClick={() => setDefaultAddressMutation.mutate({ clientId: selectedClient.id, addressId: addr.id })}
                              disabled={setDefaultAddressMutation.isPending}
                              className="text-[10px] font-mono text-slate-400 hover:text-slate-200 underline cursor-pointer"
                            >
                              Set as Default
                            </button>
                          )}
                        </div>
                      </div>
                      <div className="text-slate-400 text-[11px] mt-0.5">
                        {addr.addressLine1 || addr.street}, {addr.city}, {addr.state} - {addr.pincode}
                      </div>
                      <div className="text-[11px] text-slate-400 mt-1 font-mono">
                        Site Contact: {addr.contactPerson || 'N/A'} ({addr.phone || 'N/A'})
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>

            {/* Linked Purchase Orders */}
            <div>
              <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider mb-2 font-mono flex items-center gap-1.5">
                <FileText className="w-3.5 h-3.5 text-blue-400" />
                <span>Order History ({clientPOs.length} Purchase Orders)</span>
              </h3>

              {clientPOs.length === 0 ? (
                <div className="text-xs text-slate-400 py-3 bg-slate-950/50 rounded border border-slate-800/60 p-3">
                  No orders recorded yet for this client account.
                </div>
              ) : (
                <div className="space-y-2">
                  {clientPOs.map((po) => (
                    <div
                      key={po.id}
                      onClick={() => {
                        setSelectedPOId(po.id);
                        setCurrentTab('purchase_orders');
                      }}
                      className="p-2.5 bg-slate-950 rounded border border-slate-800 hover:border-slate-700 cursor-pointer flex items-center justify-between text-xs transition-colors"
                    >
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-mono font-bold text-emerald-400">{po.poNumber}</span>
                          <span className="text-slate-400 font-mono text-[11px]">{po.poDate}</span>
                        </div>
                        <div className="text-[11px] text-slate-300 mt-0.5 font-mono">
                          Total: ₹{(po.totalAmountINR / 100000).toFixed(2)} Lakhs · {po.items?.length || 0} Line Items
                        </div>
                      </div>

                      <div className="text-right">
                        <span className="px-2 py-0.5 text-[10px] rounded bg-slate-800 text-slate-300 font-mono">
                          {po.status}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        ) : (
          <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-lg p-8 text-center text-slate-400 text-xs">
            Select a client account to inspect full master profile, sites, and purchase orders.
          </div>
        )}
      </div>

      {/* Add Client Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-xl w-full space-y-4 shadow-2xl my-8">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <h3 className="text-sm font-bold text-slate-100 flex items-center gap-2">
                  <Building2 className="w-4 h-4 text-purple-400" />
                  <span>Register Corporate Client Account</span>
                </h3>
                <p className="text-[11px] text-slate-400 mt-0.5">
                  Full corporate master setup with GSTIN validation, commercial contacts, and default site.
                </p>
              </div>
              <button 
                onClick={() => setShowCreateModal(false)} 
                className="text-slate-400 hover:text-slate-200 cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {formError && (
              <div className="p-3 rounded bg-rose-950/50 border border-rose-800/80 text-rose-200 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 text-rose-400 shrink-0" />
                <span>{formError}</span>
              </div>
            )}

            <form onSubmit={handleCreateSubmit} className="space-y-4 text-xs">
              {/* Row 1: Company Name & Type */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="sm:col-span-2">
                  <label className="block text-slate-300 font-medium mb-1">Company Registered Name *</label>
                  <input
                    type="text"
                    required
                    value={formData.companyName}
                    onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
                    placeholder="e.g. Mahyco Private Limited"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none focus:border-purple-500"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Client Type *</label>
                  <select
                    value={formData.clientType}
                    onChange={(e) => setFormData({ ...formData, clientType: e.target.value as any })}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none focus:border-purple-500"
                  >
                    <option value="SEED_PRODUCER">Seed Producer</option>
                    <option value="GOVT_CORPORATION">Govt Corporation</option>
                    <option value="AGRI_PROCESSOR">Agri Processor</option>
                    <option value="EXPORTER">Exporter</option>
                    <option value="PRIVATE_COMPANY">Private Company</option>
                  </select>
                </div>
              </div>

              {/* Row 2: GSTIN, PAN & State */}
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">GSTIN (15 chars)</label>
                  <input
                    type="text"
                    maxLength={15}
                    value={formData.gstin}
                    onChange={(e) => setFormData({ ...formData, gstin: e.target.value.toUpperCase() })}
                    placeholder="27AABCM1234F1Z5"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono uppercase focus:outline-none focus:border-purple-500"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">PAN (10 chars)</label>
                  <input
                    type="text"
                    maxLength={10}
                    value={formData.pan}
                    onChange={(e) => setFormData({ ...formData, pan: e.target.value.toUpperCase() })}
                    placeholder="AABCM1234F"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono uppercase focus:outline-none focus:border-purple-500"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Registered State *</label>
                  <select
                    value={formData.state}
                    onChange={(e) => {
                      const st = e.target.value;
                      setFormData({
                        ...formData,
                        state: st,
                        initialAddress: formData.initialAddress ? { ...formData.initialAddress, state: st } : undefined
                      });
                    }}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none focus:border-purple-500"
                  >
                    {INDIAN_STATES.map((st) => (
                      <option key={st} value={st}>{st}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Row 3: Primary Commercial Contact */}
              <div className="p-3 bg-slate-950/70 rounded border border-slate-800/80 space-y-2">
                <span className="font-semibold text-slate-300 uppercase tracking-wider text-[10px] block">
                  Primary Commercial Contact
                </span>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-slate-400 mb-1">Contact Name *</label>
                    <input
                      type="text"
                      required
                      value={formData.primaryContactName}
                      onChange={(e) => setFormData({ ...formData, primaryContactName: e.target.value })}
                      placeholder="e.g. Rajesh Patil"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-1.5 text-slate-100"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-400 mb-1">Designation</label>
                    <input
                      type="text"
                      value={formData.primaryContactDesignation}
                      onChange={(e) => setFormData({ ...formData, primaryContactDesignation: e.target.value })}
                      placeholder="e.g. Head Procurement"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-1.5 text-slate-100"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-slate-400 mb-1">Phone Number *</label>
                    <input
                      type="tel"
                      required
                      value={formData.primaryContactPhone}
                      onChange={(e) => setFormData({ ...formData, primaryContactPhone: e.target.value })}
                      placeholder="+91 98220 11223"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-1.5 text-slate-100 font-mono"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-400 mb-1">Email Address *</label>
                    <input
                      type="email"
                      required
                      value={formData.primaryContactEmail}
                      onChange={(e) => setFormData({ ...formData, primaryContactEmail: e.target.value })}
                      placeholder="rajesh@example.com"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-1.5 text-slate-100 font-mono"
                    />
                  </div>
                </div>
              </div>

              {/* Row 4: Credit Days & Initial Site */}
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Credit Limit (Days)</label>
                  <input
                    type="number"
                    min={0}
                    max={365}
                    value={formData.creditDays}
                    onChange={(e) => setFormData({ ...formData, creditDays: parseInt(e.target.value) || 0 })}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono focus:outline-none"
                  />
                </div>
                <div className="col-span-2">
                  <label className="block text-slate-300 font-medium mb-1">Initial Consignee Site Name</label>
                  <input
                    type="text"
                    value={formData.initialAddress?.siteName || ''}
                    onChange={(e) => setFormData({
                      ...formData,
                      initialAddress: { ...formData.initialAddress!, siteName: e.target.value }
                    })}
                    placeholder="e.g. Jalna Processing Unit #1"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div className="col-span-2">
                  <label className="block text-slate-400 mb-1">Site Address Line 1</label>
                  <input
                    type="text"
                    value={formData.initialAddress?.addressLine1 || ''}
                    onChange={(e) => setFormData({
                      ...formData,
                      initialAddress: { ...formData.initialAddress!, addressLine1: e.target.value }
                    })}
                    placeholder="Plot / Street / MIDC Phase"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-1.5 text-slate-100"
                  />
                </div>
                <div>
                  <label className="block text-slate-400 mb-1">City</label>
                  <input
                    type="text"
                    value={formData.initialAddress?.city || ''}
                    onChange={(e) => setFormData({
                      ...formData,
                      initialAddress: { ...formData.initialAddress!, city: e.target.value }
                    })}
                    placeholder="City"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-1.5 text-slate-100"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-3.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded text-xs cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createClientMutation.isPending}
                  className="px-4 py-1.5 bg-purple-600 hover:bg-purple-500 text-white rounded text-xs font-semibold flex items-center gap-1.5 cursor-pointer disabled:opacity-50"
                >
                  {createClientMutation.isPending && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
                  <span>Save Client Account</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Delivery Address Modal */}
      {showAddAddressModal && selectedClient && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-md w-full space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-2">
              <h3 className="text-sm font-bold text-slate-100 flex items-center gap-2">
                <MapPin className="w-4 h-4 text-purple-400" />
                <span>Add Delivery Consignee Site</span>
              </h3>
              <button 
                onClick={() => setShowAddAddressModal(false)} 
                className="text-slate-400 hover:text-slate-200 cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleAddAddressSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">Site Name *</label>
                <input
                  type="text"
                  required
                  value={addressData.siteName}
                  onChange={(e) => setAddressData({ ...addressData, siteName: e.target.value })}
                  placeholder="e.g. Hyderabad Logistics Hub"
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none focus:border-purple-500"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Address Line 1 *</label>
                <input
                  type="text"
                  required
                  value={addressData.addressLine1}
                  onChange={(e) => setAddressData({ ...addressData, addressLine1: e.target.value })}
                  placeholder="Plot / Industrial Estate"
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none focus:border-purple-500"
                />
              </div>

              <div className="grid grid-cols-3 gap-2">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">City *</label>
                  <input
                    type="text"
                    required
                    value={addressData.city}
                    onChange={(e) => setAddressData({ ...addressData, city: e.target.value })}
                    placeholder="City"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">State *</label>
                  <select
                    value={addressData.state}
                    onChange={(e) => setAddressData({ ...addressData, state: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                  >
                    {INDIAN_STATES.map((st) => (
                      <option key={st} value={st}>{st}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">PIN Code *</label>
                  <input
                    type="text"
                    required
                    maxLength={6}
                    value={addressData.pincode}
                    onChange={(e) => setAddressData({ ...addressData, pincode: e.target.value })}
                    placeholder="500072"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Contact Person</label>
                  <input
                    type="text"
                    value={addressData.contactPerson}
                    onChange={(e) => setAddressData({ ...addressData, contactPerson: e.target.value })}
                    placeholder="Site Manager Name"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Phone</label>
                  <input
                    type="tel"
                    value={addressData.phone}
                    onChange={(e) => setAddressData({ ...addressData, phone: e.target.value })}
                    placeholder="+91"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono"
                  />
                </div>
              </div>

              <div className="flex items-center gap-2 pt-1">
                <input
                  type="checkbox"
                  id="isDefaultAddr"
                  checked={addressData.isDefault}
                  onChange={(e) => setAddressData({ ...addressData, isDefault: e.target.checked })}
                  className="rounded bg-slate-950 border-slate-800 text-purple-600 focus:ring-0"
                />
                <label htmlFor="isDefaultAddr" className="text-slate-300 text-xs">
                  Set as default consignee delivery site
                </label>
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowAddAddressModal(false)}
                  className="px-3 py-1.5 bg-slate-800 text-slate-300 rounded text-xs cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={addAddressMutation.isPending}
                  className="px-4 py-1.5 bg-purple-600 hover:bg-purple-500 text-white rounded text-xs font-semibold flex items-center gap-1.5 cursor-pointer disabled:opacity-50"
                >
                  {addAddressMutation.isPending && <Loader2 className="w-3 h-3 animate-spin" />}
                  <span>Save Site</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
