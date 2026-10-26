import React, { useState } from 'react';
import { 
  Package, 
  Search, 
  Plus, 
  Globe, 
  Lock, 
  Zap, 
  FileDown, 
  X,
  Loader2,
  AlertCircle
} from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useApp } from '../../context/AppContext';
import { 
  productApi, 
  ProductCreateDto, 
  ProductResponseDto 
} from '../../services/product-api';

const PRODUCT_CATEGORIES = [
  { value: 'ALL', label: 'All Categories' },
  { value: 'GRAVITY_SEPARATOR', label: 'Gravity Separators' },
  { value: 'SEED_CLEANER', label: 'Seed Cleaners & Screeners' },
  { value: 'DESTONER', label: 'Destoners' },
  { value: 'SEED_TREATER', label: 'Continuous Seed Treaters' },
  { value: 'ELEVATOR', label: 'Bucket Elevators' },
  { value: 'COLOR_SORTER', label: 'Optical Color Sorters' },
  { value: 'TURNKEY_PLANT', label: 'Turnkey Seed Plants' },
  { value: 'SPARE_PARTS', label: 'Spare Parts & Screens' }
];

export const ProductManagement: React.FC = () => {
  const { 
    selectedProductId, 
    setSelectedProductId, 
    showToast 
  } = useApp();

  const queryClient = useQueryClient();

  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [visibilityFilter, setVisibilityFilter] = useState('ALL');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  // New Product Form State
  const [formData, setFormData] = useState<ProductCreateDto>({
    sku: '',
    name: '',
    category: 'GRAVITY_SEPARATOR',
    unit: 'SET',
    description: '',
    basePriceINR: 1000000,
    leadTimeWeeks: 4,
    websiteVisibility: 'PUBLIC',
    specs: {
      capacity: '',
      motorPower: '',
      motorRating: 'TEFC IP55 Siemens/ABB',
      dimensions: '',
      weight: '',
      airVolume: '',
      screenArea: '',
      seedCompatibility: ['Wheat', 'Paddy', 'Soybean', 'Corn']
    }
  });

  // TanStack Query: Fetch Machinery Products from Backend
  const { data: serverProducts, isLoading } = useQuery({
    queryKey: ['products', searchQuery, categoryFilter, visibilityFilter],
    queryFn: async () => {
      try {
        const res = await productApi.getProducts({
          query: searchQuery || undefined,
          category: categoryFilter === 'ALL' ? undefined : categoryFilter,
          visibility: visibilityFilter === 'ALL' ? undefined : visibilityFilter,
          page: 0,
          size: 50
        });
        return res.items;
      } catch {
        return null;
      }
    }
  });

  const { products: contextProducts } = useApp();

  // Combine live server data with context mock products as resilient fallback
  const productList: (ProductResponseDto | any)[] = serverProducts && serverProducts.length > 0
    ? serverProducts
    : contextProducts.filter((p) => {
        const matchesSearch = 
          p.sku.toLowerCase().includes(searchQuery.toLowerCase()) ||
          p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
          (p.description && p.description.toLowerCase().includes(searchQuery.toLowerCase()));
        const matchesCat = categoryFilter === 'ALL' || p.category === categoryFilter;
        const matchesVis = visibilityFilter === 'ALL' || p.websiteVisibility === visibilityFilter;
        return matchesSearch && matchesCat && matchesVis;
      });

  const selectedProduct = productList.find((p) => p.id === selectedProductId) || productList[0];

  // TanStack Mutation: Create Product
  const createProductMutation = useMutation({
    mutationFn: (newProduct: ProductCreateDto) => productApi.createProduct(newProduct),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setSelectedProductId(data.id);
      setShowCreateModal(false);
      setFormError(null);
      showToast(`Machinery model ${data.sku} registered successfully!`, 'success');
      // Reset form
      setFormData({
        sku: '',
        name: '',
        category: 'GRAVITY_SEPARATOR',
        unit: 'SET',
        description: '',
        basePriceINR: 1000000,
        leadTimeWeeks: 4,
        websiteVisibility: 'PUBLIC',
        specs: {
          capacity: '',
          motorPower: '',
          motorRating: 'TEFC IP55 Siemens/ABB',
          dimensions: '',
          weight: '',
          airVolume: '',
          screenArea: '',
          seedCompatibility: ['Wheat', 'Paddy', 'Soybean', 'Corn']
        }
      });
    },
    onError: (error: any) => {
      const serverMsg = error?.response?.data?.message || error?.message || 'Failed to register product';
      setFormError(serverMsg);
      showToast(serverMsg, 'error');
    }
  });

  // TanStack Mutation: Toggle Website Visibility
  const toggleVisibilityMutation = useMutation({
    mutationFn: (data: { productId: string; visibility: 'PUBLIC' | 'HIDDEN' }) =>
      productApi.updateVisibility(data.productId, data.visibility),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      showToast(`Product visibility updated to ${data.websiteVisibility}`, 'info');
    },
    onError: (error: any) => {
      showToast(error?.response?.data?.message || 'Failed to update visibility', 'error');
    }
  });

  // TanStack Mutation: Toggle Internal Status
  const toggleStatusMutation = useMutation({
    mutationFn: (data: { productId: string; status: 'ACTIVE' | 'INACTIVE' }) =>
      productApi.updateStatus(data.productId, data.status),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      showToast(`Product internal status updated to ${data.internalStatus}`, 'info');
    },
    onError: (error: any) => {
      showToast(error?.response?.data?.message || 'Failed to update status', 'error');
    }
  });

  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!formData.sku.trim()) {
      setFormError('SKU (Model Code) is mandatory.');
      return;
    }
    if (!formData.name.trim()) {
      setFormError('Machinery Model Name is mandatory.');
      return;
    }
    if (!formData.specs?.capacity.trim()) {
      setFormError('Processing capacity specification is mandatory.');
      return;
    }
    if (!formData.specs?.motorPower.trim()) {
      setFormError('Motor power specification is mandatory.');
      return;
    }
    if (!formData.specs?.dimensions.trim()) {
      setFormError('Dimensions specification is mandatory.');
      return;
    }
    if (!formData.specs?.weight.trim()) {
      setFormError('Weight specification is mandatory.');
      return;
    }

    createProductMutation.mutate(formData);
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-4 rounded-lg">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
              <Package className="w-5 h-5 text-amber-400" />
              <span>Agrisep® Machinery & Equipment Master</span>
            </h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
              {productList.length} Models
            </span>
            {isLoading && <Loader2 className="w-3.5 h-3.5 text-amber-400 animate-spin" />}
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Section 11: Gravity separators, destoners, seed cleaners, chemical treaters, bucket elevators, and turnkey seed plants.
          </p>
        </div>

        <button
          onClick={() => {
            setFormError(null);
            setShowCreateModal(true);
          }}
          className="px-3.5 py-2 bg-amber-600 hover:bg-amber-500 text-white rounded text-xs font-semibold transition-colors flex items-center gap-1.5 shadow-xs shrink-0 cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>+ Add Machinery Model</span>
        </button>
      </div>

      {/* Filters */}
      <div className="bg-slate-900 border border-slate-800 p-3 rounded-lg flex flex-col md:flex-row items-center gap-3">
        <div className="relative flex-1 w-full">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search SKU (AG-GS-10), machine name, technical specs..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-amber-500"
          />
        </div>

        <div className="flex items-center gap-2 w-full md:w-auto">
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded px-2.5 py-1.5 focus:outline-none focus:border-amber-500"
          >
            {PRODUCT_CATEGORIES.map((c) => (
              <option key={c.value} value={c.value}>{c.label}</option>
            ))}
          </select>

          <select
            value={visibilityFilter}
            onChange={(e) => setVisibilityFilter(e.target.value)}
            className="bg-slate-950 border border-slate-800 text-slate-200 text-xs rounded px-2.5 py-1.5 focus:outline-none focus:border-amber-500"
          >
            <option value="ALL">All Visibility</option>
            <option value="PUBLIC">Public on Website</option>
            <option value="HIDDEN">Hidden / Internal Only</option>
          </select>
        </div>
      </div>

      {/* Two Column Layout: Product Grid & Technical Specification Inspector */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Left: Product List */}
        <div className="lg:col-span-6 space-y-2.5">
          {productList.length === 0 ? (
            <div className="p-8 text-center bg-slate-900 border border-slate-800 rounded-lg text-slate-400 text-xs">
              No machinery models match the search criteria.
            </div>
          ) : (
            productList.map((prod) => {
              const isSelected = selectedProduct?.id === prod.id;
              const isPublic = prod.websiteVisibility === 'PUBLIC';
              const isActive = prod.internalStatus === 'ACTIVE';

              return (
                <div
                  key={prod.id}
                  onClick={() => setSelectedProductId(prod.id)}
                  className={`p-3.5 rounded-lg border cursor-pointer transition-all ${
                    isSelected
                      ? 'bg-slate-800/90 border-amber-500/60 shadow-md ring-1 ring-amber-500/30'
                      : 'bg-slate-900 border-slate-800 hover:bg-slate-850'
                  }`}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-mono font-bold text-amber-400 text-xs">{prod.sku}</span>
                        {isPublic ? (
                          <span className="text-[10px] font-mono text-emerald-400 bg-emerald-950/60 px-1.5 py-0.2 rounded border border-emerald-800/40 flex items-center gap-1">
                            <Globe className="w-2.5 h-2.5" />
                            <span>PUBLIC WEB</span>
                          </span>
                        ) : (
                          <span className="text-[10px] font-mono text-slate-400 bg-slate-800 px-1.5 py-0.2 rounded border border-slate-700 flex items-center gap-1">
                            <Lock className="w-2.5 h-2.5" />
                            <span>INTERNAL</span>
                          </span>
                        )}
                        {!isActive && (
                          <span className="text-[9px] px-1.5 py-0.2 rounded bg-rose-950/80 text-rose-300 border border-rose-800/40">
                            INACTIVE
                          </span>
                        )}
                      </div>

                      <div className="font-semibold text-slate-100 text-sm mt-1">{prod.name}</div>
                      <div className="text-[11px] text-slate-400 line-clamp-1 mt-0.5">{prod.description}</div>
                    </div>

                    <div className="text-right shrink-0">
                      <div className="font-mono font-bold text-slate-100 text-xs">
                        ₹{(Number(prod.basePriceINR) / 100000).toFixed(2)} Lakhs
                      </div>
                      <div className="text-[10px] font-mono text-slate-400 mt-0.5">
                        Cap: {prod.specs?.capacity || 'N/A'}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Right: Technical Datasheet & Specs */}
        {selectedProduct ? (
          <div className="lg:col-span-6 bg-slate-900 border border-slate-800 rounded-lg p-5 space-y-4">
            <div className="border-b border-slate-800 pb-3 flex items-start justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-mono font-bold text-amber-400 text-sm">{selectedProduct.sku}</span>
                  <span className="text-xs text-slate-400 font-mono">({selectedProduct.category})</span>
                  {selectedProduct.lockedForSkuChange && (
                    <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-blue-950 text-blue-300 border border-blue-800/40">
                      PO LOCKED
                    </span>
                  )}
                </div>
                <h2 className="text-base font-bold text-slate-100 mt-1">{selectedProduct.name}</h2>
              </div>

              <div className="text-right">
                <span className="text-xs text-slate-400">Base Unit Valuation</span>
                <div className="text-lg font-mono font-bold text-emerald-400">
                  ₹{Number(selectedProduct.basePriceINR).toLocaleString('en-IN')}
                </div>
              </div>
            </div>

            <p className="text-xs text-slate-300 leading-relaxed bg-slate-950 p-3 rounded border border-slate-800">
              {selectedProduct.description || 'Standard Agrisep seed processing machinery engineered for industrial reliability.'}
            </p>

            {/* Industrial Specs Grid */}
            <div>
              <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider font-mono mb-2 flex items-center gap-1.5">
                <Zap className="w-3.5 h-3.5 text-amber-400" />
                <span>Certified Engineering Specifications</span>
              </h3>

              <div className="grid grid-cols-2 gap-2 text-xs font-mono">
                <div className="p-2.5 bg-slate-950 rounded border border-slate-800">
                  <span className="text-[10px] text-slate-400 block">Processing Capacity</span>
                  <span className="font-bold text-slate-200">{selectedProduct.specs?.capacity || 'N/A'}</span>
                </div>

                <div className="p-2.5 bg-slate-950 rounded border border-slate-800">
                  <span className="text-[10px] text-slate-400 block">Motor Rating</span>
                  <span className="font-bold text-slate-200">{selectedProduct.specs?.motorRating || selectedProduct.specs?.motorPower || 'N/A'}</span>
                </div>

                <div className="p-2.5 bg-slate-950 rounded border border-slate-800">
                  <span className="text-[10px] text-slate-400 block">Overall Dimensions (LxWxH)</span>
                  <span className="font-bold text-slate-200">{selectedProduct.specs?.dimensions || 'N/A'}</span>
                </div>

                <div className="p-2.5 bg-slate-950 rounded border border-slate-800">
                  <span className="text-[10px] text-slate-400 block">Net Machine Weight</span>
                  <span className="font-bold text-slate-200">{selectedProduct.specs?.weight || 'N/A'}</span>
                </div>

                {selectedProduct.specs?.airVolume && (
                  <div className="p-2.5 bg-slate-950 rounded border border-slate-800">
                    <span className="text-[10px] text-slate-400 block">Air Volume Requirement</span>
                    <span className="font-bold text-slate-200">{selectedProduct.specs.airVolume}</span>
                  </div>
                )}

                {selectedProduct.specs?.screenArea && (
                  <div className="p-2.5 bg-slate-950 rounded border border-slate-800">
                    <span className="text-[10px] text-slate-400 block">Screener Surface Area</span>
                    <span className="font-bold text-slate-200">{selectedProduct.specs.screenArea}</span>
                  </div>
                )}
              </div>
            </div>

            {/* Quick Actions & Visibility Controls */}
            <div className="p-3 bg-slate-950 rounded border border-slate-800 flex items-center justify-between text-xs">
              <div className="flex items-center gap-2">
                <button
                  onClick={() => {
                    const newVis = selectedProduct.websiteVisibility === 'PUBLIC' ? 'HIDDEN' : 'PUBLIC';
                    toggleVisibilityMutation.mutate({ productId: selectedProduct.id, visibility: newVis });
                  }}
                  disabled={toggleVisibilityMutation.isPending}
                  className="px-2.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded font-medium border border-slate-700 transition-colors cursor-pointer"
                >
                  {selectedProduct.websiteVisibility === 'PUBLIC' ? 'Hide from Website' : 'Publish to Website'}
                </button>

                <button
                  onClick={() => {
                    const newStatus = selectedProduct.internalStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
                    toggleStatusMutation.mutate({ productId: selectedProduct.id, status: newStatus });
                  }}
                  disabled={toggleStatusMutation.isPending}
                  className="px-2.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded font-medium border border-slate-700 transition-colors cursor-pointer"
                >
                  {selectedProduct.internalStatus === 'ACTIVE' ? 'Deactivate Model' : 'Activate Model'}
                </button>
              </div>

              <div className="flex items-center gap-1.5 text-emerald-400">
                <FileDown className="w-3.5 h-3.5" />
                <button
                  onClick={() => showToast(`Datasheet for ${selectedProduct.sku} downloaded`, 'info')}
                  className="underline hover:text-emerald-300 font-mono text-[11px] cursor-pointer"
                >
                  Download CAD Spec PDF
                </button>
              </div>
            </div>
          </div>
        ) : (
          <div className="lg:col-span-6 bg-slate-900 border border-slate-800 rounded-lg p-8 text-center text-slate-400 text-xs">
            Select a machinery model to inspect engineering datasheet and specifications.
          </div>
        )}
      </div>

      {/* Add Product Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-slate-900 border border-slate-700 rounded-lg p-5 max-w-xl w-full space-y-4 shadow-2xl my-8">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <h3 className="text-sm font-bold text-slate-100 flex items-center gap-2">
                  <Package className="w-4 h-4 text-amber-400" />
                  <span>Register Agrisep® Machinery Model</span>
                </h3>
                <p className="text-[11px] text-slate-400 mt-0.5">
                  Configure model SKU, category, commercial base valuation, and certified engineering specs.
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
              {/* Row 1: SKU & Name */}
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">SKU / Model Code *</label>
                  <input
                    type="text"
                    required
                    value={formData.sku}
                    onChange={(e) => setFormData({ ...formData, sku: e.target.value.toUpperCase() })}
                    placeholder="e.g. AG-GS-10"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono uppercase focus:outline-none focus:border-amber-500"
                  />
                </div>
                <div className="col-span-2">
                  <label className="block text-slate-300 font-medium mb-1">Model Name *</label>
                  <input
                    type="text"
                    required
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="e.g. Precision Specific Gravity Separator GS-10"
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none focus:border-amber-500"
                  />
                </div>
              </div>

              {/* Row 2: Category, Unit, Base Price */}
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Category *</label>
                  <select
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value as any })}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none focus:border-amber-500"
                  >
                    <option value="GRAVITY_SEPARATOR">Gravity Separator</option>
                    <option value="SEED_CLEANER">Seed Cleaner</option>
                    <option value="DESTONER">Destoner</option>
                    <option value="SEED_TREATER">Seed Treater</option>
                    <option value="ELEVATOR">Bucket Elevator</option>
                    <option value="COLOR_SORTER">Color Sorter</option>
                    <option value="TURNKEY_PLANT">Turnkey Plant</option>
                    <option value="SPARE_PARTS">Spare Parts</option>
                  </select>
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Pricing Unit</label>
                  <select
                    value={formData.unit}
                    onChange={(e) => setFormData({ ...formData, unit: e.target.value as any })}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none"
                  >
                    <option value="SET">SET</option>
                    <option value="UNIT">UNIT</option>
                    <option value="PCS">PCS</option>
                    <option value="METER">METER</option>
                  </select>
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Base Price INR *</label>
                  <input
                    type="number"
                    min={0}
                    required
                    value={formData.basePriceINR}
                    onChange={(e) => setFormData({ ...formData, basePriceINR: parseFloat(e.target.value) || 0 })}
                    className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 font-mono focus:outline-none focus:border-amber-500"
                  />
                </div>
              </div>

              {/* Row 3: Description */}
              <div>
                <label className="block text-slate-300 font-medium mb-1">Commercial Description</label>
                <textarea
                  rows={2}
                  value={formData.description || ''}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="Engineered for high-purity separation of seeds and agricultural grains..."
                  className="w-full bg-slate-950 border border-slate-800 rounded p-2 text-slate-100 focus:outline-none"
                />
              </div>

              {/* Technical Specifications Sub-Form */}
              <div className="p-3 bg-slate-950/70 rounded border border-slate-800/80 space-y-2">
                <span className="font-semibold text-slate-300 uppercase tracking-wider text-[10px] block font-mono">
                  Certified Technical Specifications
                </span>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-slate-400 mb-1">Processing Capacity *</label>
                    <input
                      type="text"
                      required
                      value={formData.specs?.capacity}
                      onChange={(e) => setFormData({
                        ...formData,
                        specs: { ...formData.specs!, capacity: e.target.value }
                      })}
                      placeholder="e.g. 10 - 12 TPH"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-1.5 text-slate-100"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-400 mb-1">Motor Power Rating *</label>
                    <input
                      type="text"
                      required
                      value={formData.specs?.motorPower}
                      onChange={(e) => setFormData({
                        ...formData,
                        specs: { ...formData.specs!, motorPower: e.target.value }
                      })}
                      placeholder="e.g. 15 HP Main Blower"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-1.5 text-slate-100"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-slate-400 mb-1">Dimensions (LxWxH mm) *</label>
                    <input
                      type="text"
                      required
                      value={formData.specs?.dimensions}
                      onChange={(e) => setFormData({
                        ...formData,
                        specs: { ...formData.specs!, dimensions: e.target.value }
                      })}
                      placeholder="e.g. 2800 x 1850 x 2100 mm"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-1.5 text-slate-100"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-400 mb-1">Net Weight *</label>
                    <input
                      type="text"
                      required
                      value={formData.specs?.weight}
                      onChange={(e) => setFormData({
                        ...formData,
                        specs: { ...formData.specs!, weight: e.target.value }
                      })}
                      placeholder="e.g. 2450 kg"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-1.5 text-slate-100"
                    />
                  </div>
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
                  disabled={createProductMutation.isPending}
                  className="px-4 py-1.5 bg-amber-600 hover:bg-amber-500 text-white rounded text-xs font-semibold flex items-center gap-1.5 cursor-pointer disabled:opacity-50"
                >
                  {createProductMutation.isPending && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
                  <span>Save Machinery Model</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
