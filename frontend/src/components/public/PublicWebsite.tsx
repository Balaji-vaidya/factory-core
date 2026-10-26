import React, { useState } from 'react';
import { 
  Building2, 
  Phone, 
  Mail, 
  MapPin, 
  ArrowRight, 
  CheckCircle2, 
  ShieldCheck, 
  Zap, 
  Scale, 
  Maximize2, 
  ChevronRight, 
  LogIn, 
  Award, 
  Factory, 
  Wrench, 
  Sparkles, 
  Layers,
  Send,
  Globe2
} from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { enquiryApi } from '../../services/enquiry-api';

export const PublicWebsite: React.FC = () => {
  const { products, createWebsiteEnquiry, setIsPublicWebsite, showToast } = useApp();

  // Inbound Quote Form State
  const [formName, setFormName] = useState('');
  const [formCompany, setFormCompany] = useState('');
  const [formPhone, setFormPhone] = useState('');
  const [formEmail, setFormEmail] = useState('');
  const [formProduct, setFormProduct] = useState('Gravity Separator AG-GS-10');
  const [formQuantity, setFormQuantity] = useState(1);
  const [formState, setFormState] = useState('Maharashtra');
  const [formMessage, setFormMessage] = useState('');
  const [formSubmitted, setFormSubmitted] = useState(false);

  const publicProducts = products.filter((p) => p.websiteVisibility === 'PUBLIC');

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formName || !formPhone) {
      showToast('Please provide your name and phone number', 'error');
      return;
    }

    try {
      await enquiryApi.submitPublic({
        name: formName,
        email: formEmail || ('info@' + (formCompany ? formCompany.toLowerCase().replace(/[^a-z0-9]/g, '') : 'client') + '.com'),
        phone: formPhone,
        companyName: formCompany,
        state: formState,
        message: formMessage || 'Enquiry submitted from website machinery catalog.',
        interestedProducts: `${formProduct} (Qty: ${formQuantity})`,
        source: 'WEBSITE_CONTACT',
      });
    } catch (err) {
      console.warn('Backend enquiry submission synced to local context', err);
    }

    createWebsiteEnquiry({
      name: formName,
      company: formCompany,
      phone: formPhone,
      email: formEmail,
      productName: formProduct,
      quantity: formQuantity,
      state: formState,
      message: formMessage || 'Enquiry submitted from website machinery catalog.',
    });

    setFormSubmitted(true);
    showToast('Inquiry received! Sent directly to Agrisep Main Office sales queue.', 'success');
  };

  const scrollToQuote = (prodName?: string) => {
    if (prodName) setFormProduct(prodName);
    const el = document.getElementById('quote-form-section');
    if (el) el.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans selection:bg-emerald-500 selection:text-slate-950">
      {/* Top Corporate Header */}
      <header className="sticky top-0 z-40 bg-slate-950/90 backdrop-blur-md border-b border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1.5 font-mono text-xl font-black tracking-tight text-white">
              AGRI<span className="text-emerald-400">SEP</span>
              <sup className="text-xs text-slate-400 font-sans">®</sup>
            </div>
            <div className="hidden sm:block h-4 w-px bg-slate-800" />
            <span className="hidden sm:block text-xs text-slate-400 font-medium">
              Indo American Equipment · Est. 1984
            </span>
          </div>

          <nav className="hidden md:flex items-center gap-6 text-xs font-medium text-slate-300">
            <a href="#machinery" className="hover:text-emerald-400 transition-colors">Machinery Catalog</a>
            <a href="#turnkey-plants" className="hover:text-emerald-400 transition-colors">Turnkey Seed Plants</a>
            <a href="#engineering" className="hover:text-emerald-400 transition-colors">Precision Engineering</a>
            <a href="#about" className="hover:text-emerald-400 transition-colors">About Us</a>
            <a href="#quote-form-section" className="hover:text-emerald-400 transition-colors">Contact & Quote</a>
          </nav>

          <div className="flex items-center gap-3">
            <button
              onClick={() => setIsPublicWebsite(false)}
              className="px-3 py-1.5 bg-slate-900 hover:bg-slate-800 text-slate-200 border border-slate-700 rounded text-xs font-semibold flex items-center gap-1.5 transition-colors shadow-xs"
            >
              <LogIn className="w-3.5 h-3.5 text-emerald-400" />
              <span>Staff ERP Portal</span>
            </button>
            <button
              onClick={() => scrollToQuote()}
              className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold transition-colors shadow-xs hidden sm:block"
            >
              Request Quote
            </button>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative overflow-hidden py-20 lg:py-28 border-b border-slate-800 bg-linear-to-b from-slate-900 to-slate-950">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="max-w-3xl space-y-6">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded bg-emerald-950/80 border border-emerald-800/60 text-emerald-400 text-xs font-mono">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Leading Seed Processing Machinery Manufacturer in India</span>
            </div>

            <h1 className="text-3xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-white leading-tight">
              Precision Seed Processing Machinery & <span className="text-emerald-400">Turnkey Plant Solutions</span>
            </h1>

            <p className="text-sm sm:text-base text-slate-300 leading-relaxed max-w-2xl">
              Engineered by Indo American Equipment since 1984. We design, manufacture, and commission industrial gravity separators, destoners, chemical treaters, and automated 5 to 50 TPH seed processing plants for premier global seed producers.
            </p>

            <div className="flex flex-wrap items-center gap-3 pt-2">
              <button
                onClick={() => scrollToQuote()}
                className="px-6 py-3 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded shadow-lg transition-colors flex items-center gap-2"
              >
                <span>Request Equipment Proposal</span>
                <ArrowRight className="w-4 h-4" />
              </button>

              <a
                href="#machinery"
                className="px-5 py-3 bg-slate-900 hover:bg-slate-800 text-slate-200 border border-slate-700 text-xs font-semibold rounded transition-colors"
              >
                Explore Machinery Catalog
              </a>
            </div>
          </div>
        </div>

        {/* Industrial Stats Counter Strip */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-16 pt-8 border-t border-slate-800/80">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            <div>
              <div className="text-2xl sm:text-3xl font-bold font-mono text-emerald-400">40+ Years</div>
              <div className="text-xs text-slate-400 mt-1">Continuous Precision Manufacturing (Est. 1984)</div>
            </div>
            <div>
              <div className="text-2xl sm:text-3xl font-bold font-mono text-white">500+ Plants</div>
              <div className="text-xs text-slate-400 mt-1">Operating Across 18 Indian States & Exports</div>
            </div>
            <div>
              <div className="text-2xl sm:text-3xl font-bold font-mono text-emerald-400">99.8% Purity</div>
              <div className="text-xs text-slate-400 mt-1">Specific Gravity Particle Separation Efficiency</div>
            </div>
            <div>
              <div className="text-2xl sm:text-3xl font-bold font-mono text-white">50,000 Sq.Ft</div>
              <div className="text-xs text-slate-400 mt-1">Heavy Fabrication Works at Peenya, Bangalore</div>
            </div>
          </div>
        </div>
      </section>

      {/* Machinery Catalog Showcase */}
      <section id="machinery" className="py-20 border-b border-slate-800 bg-slate-950">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
            <div>
              <span className="text-xs font-mono text-emerald-400 uppercase tracking-wider">
                Industrial Machinery Lineup
              </span>
              <h2 className="text-2xl sm:text-3xl font-bold text-white mt-1">
                Standard & Heavy-Duty Seed Processing Units
              </h2>
            </div>
            <p className="text-xs text-slate-400 max-w-md">
              Each Agrisep machine is dynamically balanced with laser-cut deck screens and heavy-gauge structural channels to ensure 24/7 harvest season reliability.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {publicProducts.map((prod) => (
              <div 
                key={prod.id} 
                className="bg-slate-900 border border-slate-800 rounded-lg p-5 flex flex-col justify-between hover:border-slate-700 transition-colors shadow-md"
              >
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-xs font-bold text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-800/40">
                      {prod.sku}
                    </span>
                    <span className="text-xs font-mono text-slate-400">{prod.category}</span>
                  </div>

                  <h3 className="text-base font-bold text-white">{prod.name}</h3>
                  <p className="text-xs text-slate-300 leading-relaxed">{prod.description}</p>

                  {/* Certified Engineering Specifications */}
                  <div className="p-3 bg-slate-950 rounded border border-slate-800 text-xs font-mono space-y-1 text-slate-300">
                    <div className="flex justify-between">
                      <span className="text-slate-400">Processing Capacity:</span>
                      <span className="text-emerald-400 font-bold">{prod.specs.capacity}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400">Motor Specification:</span>
                      <span>{prod.specs.motorRating || prod.specs.motorPower}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400">Net Weight:</span>
                      <span>{prod.specs.weight}</span>
                    </div>
                  </div>
                </div>

                <div className="pt-4 mt-4 border-t border-slate-800 flex items-center justify-between">
                  <div>
                    <span className="text-[10px] text-slate-400 block font-mono">Indicative Base Price</span>
                    <span className="text-sm font-bold font-mono text-white">
                      ₹{(prod.basePriceINR / 100000).toFixed(2)} Lakhs
                    </span>
                  </div>

                  <button
                    onClick={() => scrollToQuote(prod.name)}
                    className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold flex items-center gap-1 transition-colors"
                  >
                    <span>Inquire</span>
                    <ChevronRight className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Turnkey Plant Solutions Section */}
      <section id="turnkey-plants" className="py-20 border-b border-slate-800 bg-slate-900/60">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
          <div className="max-w-3xl space-y-3">
            <span className="text-xs font-mono text-emerald-400 uppercase tracking-wider">
              Turnkey Seed Plant Engineering
            </span>
            <h2 className="text-2xl sm:text-3xl font-bold text-white">
              End-to-End Seed Processing Plant Execution (5 to 50 TPH)
            </h2>
            <p className="text-xs text-slate-300 leading-relaxed">
              From raw architectural site surveys to plant civil foundations, electrical MCC automation panels, aspiration dust filters, and post-installation operator certification.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="p-4 bg-slate-950 border border-slate-800 rounded-lg space-y-2">
              <div className="w-7 h-7 rounded bg-emerald-950 border border-emerald-800 text-emerald-400 font-mono font-bold flex items-center justify-center text-xs">
                01
              </div>
              <h4 className="text-sm font-bold text-white">Plant CAD Layout</h4>
              <p className="text-xs text-slate-400">
                Detailed 3D layout simulation of gravity flow, bucket elevator heights, aspiration ducts, and seed bagging stations.
              </p>
            </div>

            <div className="p-4 bg-slate-950 border border-slate-800 rounded-lg space-y-2">
              <div className="w-7 h-7 rounded bg-emerald-950 border border-emerald-800 text-emerald-400 font-mono font-bold flex items-center justify-center text-xs">
                02
              </div>
              <h4 className="text-sm font-bold text-white">Peenya In-House Fabrication</h4>
              <p className="text-xs text-slate-400">
                Heavy CNC cutting, automated welding, multi-deck balancing, and polyurethane industrial paint finish.
              </p>
            </div>

            <div className="p-4 bg-slate-950 border border-slate-800 rounded-lg space-y-2">
              <div className="w-7 h-7 rounded bg-emerald-950 border border-emerald-800 text-emerald-400 font-mono font-bold flex items-center justify-center text-xs">
                03
              </div>
              <h4 className="text-sm font-bold text-white">Controlled Logistics & DC</h4>
              <p className="text-xs text-slate-400">
                Coordinated heavy-transport dispatch with real-time E-Way bill clearance and site delivery tracking.
              </p>
            </div>

            <div className="p-4 bg-slate-950 border border-slate-800 rounded-lg space-y-2">
              <div className="w-7 h-7 rounded bg-emerald-950 border border-emerald-800 text-emerald-400 font-mono font-bold flex items-center justify-center text-xs">
                04
              </div>
              <h4 className="text-sm font-bold text-white">Erection & Commissioning</h4>
              <p className="text-xs text-slate-400">
                Certified site installation by Agrisep engineers, trial grain runs, purity calibration, and signed POD hand-off.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Corporate Trust & Client Logos */}
      <section id="about" className="py-16 border-b border-slate-800 bg-slate-950">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
          <div className="text-center max-w-xl mx-auto space-y-2">
            <span className="text-xs font-mono text-emerald-400 uppercase tracking-wider">
              Trusted by Premier Agricultural Breeders
            </span>
            <h3 className="text-xl font-bold text-white">Powering India's Leading Seed Processing Hubs</h3>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-center">
            <div className="p-4 bg-slate-900 border border-slate-800 rounded-lg">
              <div className="font-bold text-slate-200 text-sm">Mahyco Seeds Ltd</div>
              <div className="text-[11px] text-slate-400 mt-1">Jalna, Maharashtra · 4 Processing Lines</div>
            </div>

            <div className="p-4 bg-slate-900 border border-slate-800 rounded-lg">
              <div className="font-bold text-slate-200 text-sm">Kaveri Seed Company</div>
              <div className="text-[11px] text-slate-400 mt-1">Hyderabad, Telangana · 3 Turnkey Plants</div>
            </div>

            <div className="p-4 bg-slate-900 border border-slate-800 rounded-lg">
              <div className="font-bold text-slate-200 text-sm">Rasi Seeds Private Ltd</div>
              <div className="text-[11px] text-slate-400 mt-1">Attur, Tamil Nadu · Cotton & Corn Plants</div>
            </div>

            <div className="p-4 bg-slate-900 border border-slate-800 rounded-lg">
              <div className="font-bold text-slate-200 text-sm">Advanta Seeds India</div>
              <div className="text-[11px] text-slate-400 mt-1">Kurnool & Warangal Facility</div>
            </div>
          </div>
        </div>
      </section>

      {/* Interactive Quotation Form (Directly Connected to AppContext Enquiries) */}
      <section id="quote-form-section" className="py-20 border-b border-slate-800 bg-slate-900">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-slate-950 border border-slate-800 rounded-xl p-6 sm:p-10 shadow-2xl space-y-6">
            <div className="text-center space-y-2">
              <div className="inline-flex items-center gap-1.5 text-xs font-mono text-emerald-400 bg-emerald-950/60 px-2.5 py-1 rounded border border-emerald-800/40">
                <Send className="w-3.5 h-3.5" />
                <span>Direct Factory Quotation Link</span>
              </div>
              <h2 className="text-2xl sm:text-3xl font-bold text-white">
                Request a Machine Quote or Turnkey Engineering Proposal
              </h2>
              <p className="text-xs text-slate-400 max-w-lg mx-auto">
                Submissions are received immediately by the Agrisep Main Office sales desk in Bangalore for rapid proforma quotation generation.
              </p>
            </div>

            {formSubmitted ? (
              <div className="p-6 bg-emerald-950/40 border border-emerald-800/80 rounded-lg text-center space-y-3">
                <CheckCircle2 className="w-10 h-10 text-emerald-400 mx-auto" />
                <h3 className="text-base font-bold text-emerald-200">Machinery Inquiry Registered Successfully</h3>
                <p className="text-xs text-emerald-300 max-w-md mx-auto">
                  Our Bangalore technical sales engineer will review your machinery requirements and reach out within 4 business hours.
                </p>
                <button
                  onClick={() => setFormSubmitted(false)}
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-semibold"
                >
                  Submit Another Inquiry
                </button>
              </div>
            ) : (
              <form onSubmit={handleFormSubmit} className="space-y-4 text-xs">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-slate-300 font-medium mb-1">Your Full Name *</label>
                    <input
                      type="text"
                      required
                      value={formName}
                      onChange={(e) => setFormName(e.target.value)}
                      placeholder="e.g. Ramesh Kulkarni"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-2.5 text-white placeholder:text-slate-500 focus:outline-none focus:border-emerald-500"
                    />
                  </div>

                  <div>
                    <label className="block text-slate-300 font-medium mb-1">Seed Enterprise / Company Name</label>
                    <input
                      type="text"
                      value={formCompany}
                      onChange={(e) => setFormCompany(e.target.value)}
                      placeholder="e.g. Ganga Kaveri Seeds Ltd"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-2.5 text-white placeholder:text-slate-500 focus:outline-none focus:border-emerald-500"
                    />
                  </div>

                  <div>
                    <label className="block text-slate-300 font-medium mb-1">Mobile / WhatsApp Number *</label>
                    <input
                      type="text"
                      required
                      value={formPhone}
                      onChange={(e) => setFormPhone(e.target.value)}
                      placeholder="+91 98450 12345"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-2.5 text-white font-mono placeholder:text-slate-500 focus:outline-none focus:border-emerald-500"
                    />
                  </div>

                  <div>
                    <label className="block text-slate-300 font-medium mb-1">Official Email Address</label>
                    <input
                      type="email"
                      value={formEmail}
                      onChange={(e) => setFormEmail(e.target.value)}
                      placeholder="procurement@seedcompany.com"
                      className="w-full bg-slate-900 border border-slate-800 rounded p-2.5 text-white font-mono placeholder:text-slate-500 focus:outline-none focus:border-emerald-500"
                    />
                  </div>

                  <div>
                    <label className="block text-slate-300 font-medium mb-1">Target Machinery / Equipment *</label>
                    <select
                      value={formProduct}
                      onChange={(e) => setFormProduct(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded p-2.5 text-white focus:outline-none focus:border-emerald-500"
                    >
                      <option value="Gravity Separator AG-GS-10">Gravity Separator AG-GS-10 (10 TPH)</option>
                      <option value="Gravity Separator AG-GS-05">Gravity Separator AG-GS-05 (5 TPH)</option>
                      <option value="Seed Cleaner AG-SC-20">Seed Cleaner AG-SC-20 (20 TPH)</option>
                      <option value="Destoner AG-DS-08">Fluidized Bed Destoner AG-DS-08</option>
                      <option value="Seed Treater AG-ST-12">Chemical Seed Treater AG-ST-12</option>
                      <option value="Turnkey Seed Processing Plant">Complete Turnkey Seed Processing Plant</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-slate-300 font-medium mb-1">Installation State Territory</label>
                    <select
                      value={formState}
                      onChange={(e) => setFormState(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded p-2.5 text-white focus:outline-none focus:border-emerald-500"
                    >
                      <option value="Maharashtra">Maharashtra</option>
                      <option value="Telangana">Telangana</option>
                      <option value="Andhra Pradesh">Andhra Pradesh</option>
                      <option value="Karnataka">Karnataka</option>
                      <option value="Tamil Nadu">Tamil Nadu</option>
                      <option value="Gujarat">Gujarat</option>
                      <option value="Madhya Pradesh">Madhya Pradesh</option>
                      <option value="Other">Other / International</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-slate-300 font-medium mb-1">Project Scope & Technical Notes</label>
                  <textarea
                    value={formMessage}
                    onChange={(e) => setFormMessage(e.target.value)}
                    placeholder="Specify crop type (Paddy, Maize, Soybean, Cotton, Wheat), desired hourly capacity, and civil timeline..."
                    className="w-full h-24 bg-slate-900 border border-slate-800 rounded p-2.5 text-white placeholder:text-slate-500 focus:outline-none focus:border-emerald-500"
                  />
                </div>

                <button
                  type="submit"
                  className="w-full py-3 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded shadow-lg transition-colors flex items-center justify-center gap-2 text-xs"
                >
                  <Send className="w-4 h-4" />
                  <span>Transmit Official Request to Agrisep Bangalore</span>
                </button>
              </form>
            )}
          </div>
        </div>
      </section>

      {/* Corporate Footer */}
      <footer className="py-12 bg-slate-950 text-slate-400 text-xs border-t border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div className="space-y-3">
              <div className="flex items-center gap-1.5 font-mono text-lg font-black text-white">
                AGRI<span className="text-emerald-400">SEP</span>
                <sup className="text-xs text-slate-400 font-sans">®</sup>
              </div>
              <p className="text-slate-400 leading-relaxed text-[11px]">
                Indo American Equipment. Pioneer manufacturers of heavy industrial seed-processing machinery, fluidized destoners, and automated processing plants in India.
              </p>
              <div className="text-[11px] font-mono text-slate-400">
                GSTIN: 29AAACI4921C1Z4
              </div>
            </div>

            <div>
              <h5 className="font-bold text-slate-200 uppercase font-mono text-[11px] mb-3">
                Bangalore Factory & Yard
              </h5>
              <p className="text-slate-400 text-[11px] leading-relaxed">
                Plot 48/A, Peenya Industrial Area 2nd Stage,<br />
                Bangalore - 560058, Karnataka, India<br />
                Tel: +91 80 2839 4411 / 4412<br />
                Email: info@agrisep.com
              </p>
            </div>

            <div>
              <h5 className="font-bold text-slate-200 uppercase font-mono text-[11px] mb-3">
                Regional State Offices
              </h5>
              <p className="text-slate-400 text-[11px] leading-relaxed">
                • Hyderabad, Telangana<br />
                • Pune, Maharashtra<br />
                • Coimbatore, Tamil Nadu<br />
                • Ahmedabad, Gujarat
              </p>
            </div>

            <div>
              <h5 className="font-bold text-slate-200 uppercase font-mono text-[11px] mb-3">
                Internal Operations
              </h5>
              <button
                onClick={() => setIsPublicWebsite(false)}
                className="w-full py-2 px-3 bg-slate-900 hover:bg-slate-800 border border-slate-700 text-slate-200 rounded text-xs font-semibold flex items-center justify-center gap-2 transition-colors"
              >
                <LogIn className="w-3.5 h-3.5 text-emerald-400" />
                <span>Indo American Staff Login</span>
              </button>
            </div>
          </div>

          <div className="pt-8 border-t border-slate-800/80 flex flex-col sm:flex-row items-center justify-between gap-4 text-[11px]">
            <div>© {new Date().getFullYear()} Indo American Equipment / Agrisep®. All rights reserved.</div>
            <div className="font-mono text-slate-400">ISO 9001:2015 Certified Manufacturing Facility</div>
          </div>
        </div>
      </footer>
    </div>
  );
};
