import React, { createContext, useContext, useState, useEffect } from 'react';
import { 
  User, 
  UserRole, 
  Office, 
  Client, 
  Product, 
  InventoryItem, 
  PurchaseOrder, 
  DeliveryChallan, 
  DeliveryRecord, 
  StockShortage, 
  StockLedgerEntry, 
  WebsiteEnquiry, 
  NotificationItem, 
  AuditRecord,
  POItem,
  DCItem,
  Attachment,
  Priority
} from '../types';
import {
  INITIAL_USERS,
  INITIAL_OFFICES,
  INITIAL_PRODUCTS,
  INITIAL_CLIENTS,
  INITIAL_INVENTORY,
  INITIAL_PURCHASE_ORDERS,
  INITIAL_SHORTAGES,
  INITIAL_DELIVERY_CHALLANS,
  INITIAL_DELIVERIES,
  INITIAL_STOCK_LEDGER,
  INITIAL_WEBSITE_ENQUIRIES,
  INITIAL_NOTIFICATIONS,
  INITIAL_AUDIT_RECORDS
} from '../data/mockData';

interface AppContextType {
  // Navigation & Role State
  appMode: 'internal' | 'public';
  setAppMode: (mode: 'internal' | 'public') => void;
  currentUser: User;
  setCurrentUserRole: (role: UserRole) => void;
  currentOffice: Office;
  setCurrentOffice: (office: Office) => void;
  currentTab: string;
  setCurrentTab: (tab: string) => void;
  publicNav: string;
  setPublicNav: (page: string) => void;
  isLoggedIn: boolean;
  setIsLoggedIn: (val: boolean) => void;

  // Data Collections
  users: User[];
  offices: Office[];
  products: Product[];
  clients: Client[];
  inventory: InventoryItem[];
  purchaseOrders: PurchaseOrder[];
  shortages: StockShortage[];
  deliveryChallans: DeliveryChallan[];
  deliveries: DeliveryRecord[];
  stockLedger: StockLedgerEntry[];
  websiteEnquiries: WebsiteEnquiry[];
  notifications: NotificationItem[];
  auditRecords: AuditRecord[];

  // Selected Record Modals / Actions
  selectedPOId: string | null;
  setSelectedPOId: (id: string | null) => void;
  selectedClientId: string | null;
  setSelectedClientId: (id: string | null) => void;
  selectedProductId: string | null;
  setSelectedProductId: (id: string | null) => void;
  selectedDCId: string | null;
  setSelectedDCId: (id: string | null) => void;

  // Business Action Functions
  createPurchaseOrder: (poData: {
    clientId: string;
    officeId: string;
    priority: Priority;
    expectedDeliveryDate: string;
    notes: string;
    items: { productId: string; quantity: number; unitPriceINR: number; expectedDeliveryDate: string; notes?: string }[];
    attachments: Attachment[];
    submitForApproval: boolean;
  }) => PurchaseOrder;

  approvePurchaseOrder: (poId: string, notes?: string) => { success: boolean; message: string };
  returnPurchaseOrder: (poId: string, reason: string) => void;
  requestCancelPurchaseOrder: (poId: string, reason: string) => void;
  approveCancellation: (poId: string) => void;
  
  createDeliveryChallan: (dcData: {
    poId: string;
    items: { productId: string; dispatchQuantity: number }[];
    vehicleNumber: string;
    transporterName: string;
    driverName: string;
    driverPhone: string;
    ewayBillNumber: string;
    expectedArrivalDate: string;
    notes: string;
  }) => { success: boolean; message: string; dc?: DeliveryChallan };

  approveDispatch: (dcId: string) => void;
  rejectDispatch: (dcId: string, reason: string) => void;
  confirmDelivery: (deliveryData: {
    dcId: string;
    deliveryDate: string;
    receivedBy: string;
    receiverContact: string;
    remarks: string;
  }) => void;

  resolveShortageWithIncomingStock: (shortageId: string, quantityAdded: number) => void;
  adjustInventoryStock: (productId: string, adjustmentQty: number, remarks: string) => void;
  
  submitWebsiteEnquiry: (enquiry: {
    name: string;
    company?: string;
    phone: string;
    email: string;
    productId?: string;
    productName?: string;
    quantity?: number;
    message: string;
    state?: string;
  }) => void;

  updateEnquiryStatus: (enquiryId: string, status: WebsiteEnquiry['status'], notes?: string) => void;
  markNotificationAsRead: (id: string) => void;
  markAllNotificationsRead: () => void;
  addNewAuditEntry: (action: string, resourceType: AuditRecord['resourceType'], resourceId: string, resourceIdentifier: string, oldValue?: string, newValue?: string) => void;
  
  // UI Helpers & Fast Guided Walkthrough
  showGuidedTour: boolean;
  setShowGuidedTour: (val: boolean) => void;
  toastMessage: { text: string; type: 'success' | 'warning' | 'error' | 'info' } | null;
  showToast: (text: string, type?: 'success' | 'warning' | 'error' | 'info') => void;
  isPublicWebsite: boolean;
  setIsPublicWebsite: (val: boolean) => void;
  createWebsiteEnquiry: (enquiry: {
    name: string;
    company?: string;
    phone: string;
    email: string;
    productId?: string;
    productName?: string;
    quantity?: number;
    message: string;
    state?: string;
  }) => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [appMode, setAppMode] = useState<'internal' | 'public'>('internal');
  const [users] = useState<User[]>(INITIAL_USERS);
  const [offices] = useState<Office[]>(INITIAL_OFFICES);
  const [currentUser, setCurrentUser] = useState<User>(INITIAL_USERS[0]); // Default: Suresh Patel (Owner)
  const [currentOffice, setCurrentOffice] = useState<Office>(INITIAL_OFFICES[0]);
  const [currentTab, setCurrentTab] = useState<string>('dashboard');
  const [publicNav, setPublicNav] = useState<string>('home');
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(true);

  // Collections
  const [products, setProducts] = useState<Product[]>(INITIAL_PRODUCTS);
  const [clients, setClients] = useState<Client[]>(INITIAL_CLIENTS);
  const [inventory, setInventory] = useState<InventoryItem[]>(INITIAL_INVENTORY);
  const [purchaseOrders, setPurchaseOrders] = useState<PurchaseOrder[]>(INITIAL_PURCHASE_ORDERS);
  const [shortages, setShortages] = useState<StockShortage[]>(INITIAL_SHORTAGES);
  const [deliveryChallans, setDeliveryChallans] = useState<DeliveryChallan[]>(INITIAL_DELIVERY_CHALLANS);
  const [deliveries, setDeliveries] = useState<DeliveryRecord[]>(INITIAL_DELIVERIES);
  const [stockLedger, setStockLedger] = useState<StockLedgerEntry[]>(INITIAL_STOCK_LEDGER);
  const [websiteEnquiries, setWebsiteEnquiries] = useState<WebsiteEnquiry[]>(INITIAL_WEBSITE_ENQUIRIES);
  const [notifications, setNotifications] = useState<NotificationItem[]>(INITIAL_NOTIFICATIONS);
  const [auditRecords, setAuditRecords] = useState<AuditRecord[]>(INITIAL_AUDIT_RECORDS);

  // Modals / Selected
  const [selectedPOId, setSelectedPOId] = useState<string | null>(null);
  const [selectedClientId, setSelectedClientId] = useState<string | null>(null);
  const [selectedProductId, setSelectedProductId] = useState<string | null>(null);
  const [selectedDCId, setSelectedDCId] = useState<string | null>(null);
  const [showGuidedTour, setShowGuidedTour] = useState<boolean>(false);
  const [toastMessage, setToastMessage] = useState<{ text: string; type: 'success' | 'warning' | 'error' | 'info' } | null>(null);

  const showToast = (text: string, type: 'success' | 'warning' | 'error' | 'info' = 'info') => {
    setToastMessage({ text, type });
    setTimeout(() => {
      setToastMessage((prev) => (prev?.text === text ? null : prev));
    }, 4500);
  };

  const setCurrentUserRole = (role: UserRole) => {
    const matched = users.find((u) => u.role === role);
    if (matched) {
      setCurrentUser(matched);
      const matchedOffice = offices.find((o) => o.id === matched.officeId);
      if (matchedOffice) {
        setCurrentOffice(matchedOffice);
      }
      showToast(`Switched active view to ${matched.name} (${role.replace(/_/g, ' ')})`, 'info');
    }
  };

  const addNewAuditEntry = (
    action: string,
    resourceType: AuditRecord['resourceType'],
    resourceId: string,
    resourceIdentifier: string,
    oldValue?: string,
    newValue?: string
  ) => {
    const newEntry: AuditRecord = {
      id: `AUD-${Date.now().toString().slice(-4)}`,
      correlationId: `CORR-${Math.floor(1000 + Math.random() * 9000)}-${resourceType.slice(0, 2)}`,
      timestamp: new Date().toISOString().replace('T', ' ').slice(0, 19),
      userName: currentUser.name,
      userRole: currentUser.role,
      action,
      resourceType,
      resourceId,
      resourceIdentifier,
      oldValue: oldValue || 'N/A',
      newValue: newValue || 'N/A',
      ipAddress: '192.168.10.42 (Internal LAN)',
      deviceInfo: 'Industrial Workstation Terminal / Chrome 128',
    };
    setAuditRecords((prev) => [newEntry, ...prev]);
  };

  // Helper: Create PO
  const createPurchaseOrder = (poData: {
    clientId: string;
    officeId: string;
    priority: Priority;
    expectedDeliveryDate: string;
    notes: string;
    items: { productId: string; quantity: number; unitPriceINR: number; expectedDeliveryDate: string; notes?: string }[];
    attachments: Attachment[];
    submitForApproval: boolean;
  }): PurchaseOrder => {
    const client = clients.find((c) => c.id === poData.clientId)!;
    const defaultAddress = client.deliveryAddresses.find((a) => a.isDefault) || client.deliveryAddresses[0];
    const poNumCounter = 101 + purchaseOrders.length;
    const poNumber = `PO-2026-${poNumCounter.toString().padStart(6, '0')}`;
    const office = offices.find((o) => o.id === poData.officeId) || currentOffice;

    const items: POItem[] = poData.items.map((it, idx) => {
      const prod = products.find((p) => p.id === it.productId)!;
      return {
        id: `POI-${poNumCounter}-${idx + 1}`,
        productId: prod.id,
        sku: prod.sku,
        productName: prod.name,
        description: prod.description,
        unit: prod.unit,
        quantity: it.quantity,
        reservedQuantity: 0,
        dispatchedQuantity: 0,
        deliveredQuantity: 0,
        unitPriceINR: it.unitPriceINR || prod.basePriceINR,
        expectedDeliveryDate: it.expectedDeliveryDate || poData.expectedDeliveryDate,
      };
    });

    const totalAmount = items.reduce((sum, item) => sum + item.quantity * item.unitPriceINR, 0);
    const initialStatus = poData.submitForApproval ? 'PENDING_APPROVAL' : 'DRAFT';

    const newPO: PurchaseOrder = {
      id: `PO-${poNumCounter}`,
      poNumber,
      poDate: new Date().toISOString().slice(0, 10),
      clientId: client.id,
      clientName: client.companyName,
      clientSnapshot: {
        companyName: client.companyName,
        gstin: client.gstin,
        pan: client.pan,
        primaryContact: client.primaryContact,
        deliveryAddress: defaultAddress,
      },
      officeId: office.id,
      officeName: office.name,
      state: defaultAddress.state,
      priority: poData.priority,
      expectedDeliveryDate: poData.expectedDeliveryDate,
      status: initialStatus,
      reservationStatus: 'PENDING_RESERVATION', // RULE 1: PO does not affect inventory before Owner approval
      deliveryStatus: 'NOT_DELIVERED',
      items,
      totalAmountINR: totalAmount,
      attachments: poData.attachments,
      versions: [
        {
          versionNumber: 1,
          createdAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
          createdBy: `${currentUser.name} (${currentUser.role})`,
          reason: poData.submitForApproval ? 'Initial creation and submission for Owner Approval' : 'Draft created',
          changes: [
            {
              field: 'Status',
              previousValue: 'NONE',
              newValue: initialStatus,
              changedBy: currentUser.name,
              timestamp: new Date().toISOString().replace('T', ' ').slice(0, 16),
            },
          ],
          snapshot: {},
        },
      ],
      currentVersion: 1,
      notes: poData.notes,
      createdBy: currentUser.name,
      createdById: currentUser.id,
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
      updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
    };

    setPurchaseOrders((prev) => [newPO, ...prev]);

    addNewAuditEntry(
      'CREATE_PURCHASE_ORDER',
      'PURCHASE_ORDER',
      newPO.id,
      newPO.poNumber,
      'NONE',
      `Created ${newPO.poNumber} with status ${initialStatus} for ${client.companyName}`
    );

    if (poData.submitForApproval) {
      // Add notification for Owner
      const notif: NotificationItem = {
        id: `NOTIF-${Date.now().toString().slice(-4)}`,
        type: 'PO_SUBMITTED',
        title: `New PO ${newPO.poNumber} Awaiting Owner Approval`,
        message: `${currentUser.name} submitted ${newPO.poNumber} for ${client.companyName} (Total: ₹${(newPO.totalAmountINR / 100000).toFixed(2)} Lakhs).`,
        relatedRecordType: 'PO',
        relatedRecordId: newPO.id,
        relatedRecordNumber: newPO.poNumber,
        timestamp: 'Just now',
        isRead: false,
        priority: newPO.priority === 'URGENT' ? 'HIGH' : 'NORMAL',
      };
      setNotifications((prev) => [notif, ...prev]);
    }

    showToast(
      poData.submitForApproval 
        ? `Purchase Order ${poNumber} submitted for Owner approval!` 
        : `Purchase Order ${poNumber} saved as Draft.`,
      'success'
    );

    return newPO;
  };

  // Helper: Approve PO (Owner Approval Workflow & Automatic Stock Check / Reservation)
  // RULE 1: PO does not affect inventory before Owner approval
  // RULE 2: After approval, inventory availability is checked
  // RULE 3: Partial reservation is allowed
  // RULE 4: Shortage records are created automatically when stock is insufficient
  // RULE 5: Owner cannot override insufficient stock
  const approvePurchaseOrder = (poId: string, notes?: string): { success: boolean; message: string } => {
    const po = purchaseOrders.find((p) => p.id === poId);
    if (!po) return { success: false, message: 'PO not found' };

    let hasShortage = false;
    let allReserved = true;
    const newShortages: StockShortage[] = [];
    const updatedInventory = [...inventory];
    const newLedgerEntries: StockLedgerEntry[] = [];

    const updatedItems = po.items.map((item) => {
      const invIndex = updatedInventory.findIndex((inv) => inv.productId === item.productId);
      if (invIndex === -1) {
        return item;
      }

      const inv = updatedInventory[invIndex];
      const available = Math.max(0, inv.onHand - inv.reserved);
      const needed = item.quantity;
      let reservedForThis = 0;

      if (available >= needed) {
        // Full reservation
        reservedForThis = needed;
        updatedInventory[invIndex] = {
          ...inv,
          reserved: inv.reserved + reservedForThis,
          status: (inv.onHand - (inv.reserved + reservedForThis)) <= 0 ? 'RESERVED' : inv.status,
          lastUpdated: new Date().toISOString().slice(0, 10),
        };
      } else {
        // Partial reservation or complete shortage
        reservedForThis = available;
        const shortageQty = needed - available;
        hasShortage = true;
        allReserved = false;

        updatedInventory[invIndex] = {
          ...inv,
          reserved: inv.reserved + reservedForThis,
          status: 'INSUFFICIENT',
          lastUpdated: new Date().toISOString().slice(0, 10),
        };

        const shortageRecord: StockShortage = {
          id: `SH-${Math.floor(800 + Math.random() * 900)}`,
          poId: po.id,
          poNumber: po.poNumber,
          productId: item.productId,
          sku: item.sku,
          productName: item.productName,
          requiredQuantity: needed,
          reservedQuantity: reservedForThis,
          shortageQuantity: shortageQty,
          poDate: po.poDate,
          expectedDeliveryDate: item.expectedDeliveryDate,
          priority: po.priority,
          status: 'OPEN',
          reason: `Stock shortage upon Owner approval: Required ${needed} ${item.unit}s, Available on hand ${available}. Shortage of ${shortageQty} units logged for factory production assembly.`,
          createdAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
        };

        newShortages.push(shortageRecord);
      }

      // Add Stock Ledger record for reservation
      newLedgerEntries.push({
        id: `LED-${Date.now().toString().slice(-4)}-${item.sku}`,
        timestamp: new Date().toISOString().replace('T', ' ').slice(0, 19),
        productId: item.productId,
        sku: item.sku,
        productName: item.productName,
        transactionType: 'PO_RESERVATION',
        quantityChange: 0,
        balanceOnHand: updatedInventory[invIndex].onHand,
        referenceId: po.poNumber,
        poNumber: po.poNumber,
        userName: `${currentUser.name} (Approval Engine)`,
        remarks: `Reserved ${reservedForThis}/${needed} units upon Owner approval of ${po.poNumber}.`,
      });

      return {
        ...item,
        reservedQuantity: reservedForThis,
      };
    });

    const newReservationStatus = hasShortage
      ? (updatedItems.some(i => i.reservedQuantity > 0) ? 'PARTIALLY_RESERVED' : 'INSUFFICIENT_STOCK')
      : 'STOCK_RESERVED';

    const newPOStatus = hasShortage ? 'STOCK_RESERVATION' : 'APPROVED';

    const updatedPO: PurchaseOrder = {
      ...po,
      status: newPOStatus,
      reservationStatus: newReservationStatus,
      items: updatedItems,
      approvedBy: currentUser.name,
      approvedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
      updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
      versions: [
        ...po.versions,
        {
          versionNumber: po.versions.length + 1,
          createdAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
          createdBy: `${currentUser.name} (${currentUser.role})`,
          reason: notes || 'Owner Approval and Stock Reservation Check',
          changes: [
            {
              field: 'Status',
              previousValue: po.status,
              newValue: newPOStatus,
              changedBy: currentUser.name,
              timestamp: new Date().toISOString().replace('T', ' ').slice(0, 16),
            },
            {
              field: 'Reservation Status',
              previousValue: po.reservationStatus,
              newValue: newReservationStatus,
              changedBy: 'System (Inventory Engine)',
              timestamp: new Date().toISOString().replace('T', ' ').slice(0, 16),
            },
          ],
          snapshot: {},
        },
      ],
      currentVersion: po.versions.length + 1,
    };

    setPurchaseOrders((prev) => prev.map((p) => (p.id === poId ? updatedPO : p)));
    setInventory(updatedInventory);
    setStockLedger((prev) => [...newLedgerEntries, ...prev]);

    if (newShortages.length > 0) {
      setShortages((prev) => [...newShortages, ...prev]);
    }

    addNewAuditEntry(
      'APPROVE_PURCHASE_ORDER',
      'PURCHASE_ORDER',
      po.id,
      po.poNumber,
      `Status: ${po.status}`,
      `Status: ${newPOStatus} | Reservation: ${newReservationStatus} ${hasShortage ? `(${newShortages.length} shortage records created)` : ''}`
    );

    // Notification
    const notif: NotificationItem = {
      id: `NOTIF-${Date.now().toString().slice(-4)}`,
      type: hasShortage ? 'INSUFFICIENT_STOCK' : 'PO_APPROVED',
      title: hasShortage ? `Stock Shortage on ${po.poNumber}` : `PO ${po.poNumber} Approved by Owner`,
      message: hasShortage
        ? `PO ${po.poNumber} approved with partial reservation. Shortage registered for ${newShortages.map(s => `${s.shortageQuantity}x ${s.sku}`).join(', ')}.`
        : `PO ${po.poNumber} has been approved. All inventory successfully reserved in Peenya Godown #1. Ready for Delivery Challan.`,
      relatedRecordType: hasShortage ? 'SHORTAGE' : 'PO',
      relatedRecordId: po.id,
      relatedRecordNumber: po.poNumber,
      timestamp: 'Just now',
      isRead: false,
      priority: 'HIGH',
    };
    setNotifications((prev) => [notif, ...prev]);

    const msg = hasShortage
      ? `PO approved with Partial Stock Reservation. Shortage logged for factory production assembly.`
      : `PO approved! Full stock reserved. Godown team can create Delivery Challan.`;

    showToast(msg, hasShortage ? 'warning' : 'success');
    return { success: true, message: msg };
  };

  const returnPurchaseOrder = (poId: string, reason: string) => {
    const po = purchaseOrders.find((p) => p.id === poId);
    if (!po) return;

    const updatedPO: PurchaseOrder = {
      ...po,
      status: 'RETURNED',
      rejectionReason: reason,
      updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
      versions: [
        ...po.versions,
        {
          versionNumber: po.versions.length + 1,
          createdAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
          createdBy: `${currentUser.name} (${currentUser.role})`,
          reason: `Returned for revision: ${reason}`,
          changes: [
            {
              field: 'Status',
              previousValue: po.status,
              newValue: 'RETURNED',
              changedBy: currentUser.name,
              timestamp: new Date().toISOString().replace('T', ' ').slice(0, 16),
            },
          ],
          snapshot: {},
        },
      ],
      currentVersion: po.versions.length + 1,
    };

    setPurchaseOrders((prev) => prev.map((p) => (p.id === poId ? updatedPO : p)));

    addNewAuditEntry(
      'RETURN_PURCHASE_ORDER',
      'PURCHASE_ORDER',
      po.id,
      po.poNumber,
      `Status: ${po.status}`,
      `Status: RETURNED | Reason: ${reason}`
    );

    const notif: NotificationItem = {
      id: `NOTIF-${Date.now().toString().slice(-4)}`,
      type: 'PO_RETURNED',
      title: `PO ${po.poNumber} Returned by Owner`,
      message: `Owner ${currentUser.name} returned PO with remark: "${reason}". Please revise and resubmit.`,
      relatedRecordType: 'PO',
      relatedRecordId: po.id,
      relatedRecordNumber: po.poNumber,
      timestamp: 'Just now',
      isRead: false,
      priority: 'HIGH',
    };
    setNotifications((prev) => [notif, ...prev]);
    showToast(`PO ${po.poNumber} returned for revision`, 'warning');
  };

  const requestCancelPurchaseOrder = (poId: string, reason: string) => {
    const po = purchaseOrders.find((p) => p.id === poId);
    if (!po) return;

    setPurchaseOrders((prev) =>
      prev.map((p) =>
        p.id === poId
          ? {
              ...p,
              status: 'CANCEL_REQUESTED',
              cancellationReason: reason,
              updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
            }
          : p
      )
    );

    addNewAuditEntry(
      'REQUEST_PO_CANCELLATION',
      'PURCHASE_ORDER',
      po.id,
      po.poNumber,
      `Status: ${po.status}`,
      `Status: CANCEL_REQUESTED | Reason: ${reason}`
    );

    const notif: NotificationItem = {
      id: `NOTIF-${Date.now().toString().slice(-4)}`,
      type: 'CANCELLATION_REQUESTED',
      title: `Cancellation Requested for ${po.poNumber}`,
      message: `${currentUser.name} submitted cancellation request for ${po.poNumber}. Reason: "${reason}". Requires Owner Approval.`,
      relatedRecordType: 'PO',
      relatedRecordId: po.id,
      relatedRecordNumber: po.poNumber,
      timestamp: 'Just now',
      isRead: false,
      priority: 'HIGH',
    };
    setNotifications((prev) => [notif, ...prev]);
    showToast(`Cancellation request submitted for ${po.poNumber}`, 'info');
  };

  const approveCancellation = (poId: string) => {
    const po = purchaseOrders.find((p) => p.id === poId);
    if (!po) return;

    // Release any reserved stock
    const updatedInventory = [...inventory];
    const newLedgerEntries: StockLedgerEntry[] = [];

    po.items.forEach((item) => {
      if (item.reservedQuantity > 0) {
        const invIndex = updatedInventory.findIndex((i) => i.productId === item.productId);
        if (invIndex !== -1) {
          const inv = updatedInventory[invIndex];
          updatedInventory[invIndex] = {
            ...inv,
            reserved: Math.max(0, inv.reserved - item.reservedQuantity),
            lastUpdated: new Date().toISOString().slice(0, 10),
          };

          newLedgerEntries.push({
            id: `LED-${Date.now().toString().slice(-4)}-REL-${item.sku}`,
            timestamp: new Date().toISOString().replace('T', ' ').slice(0, 19),
            productId: item.productId,
            sku: item.sku,
            productName: item.productName,
            transactionType: 'PO_UNRESERVATION',
            quantityChange: 0,
            balanceOnHand: updatedInventory[invIndex].onHand,
            referenceId: po.poNumber,
            poNumber: po.poNumber,
            userName: `${currentUser.name} (Owner Cancellation)`,
            remarks: `Unreserved ${item.reservedQuantity} units upon approved cancellation of ${po.poNumber}.`,
          });
        }
      }
    });

    setInventory(updatedInventory);
    setStockLedger((prev) => [...newLedgerEntries, ...prev]);

    setPurchaseOrders((prev) =>
      prev.map((p) =>
        p.id === poId
          ? {
              ...p,
              status: 'CANCELLED',
              reservationStatus: 'RELEASED',
              updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
            }
          : p
      )
    );

    addNewAuditEntry(
      'APPROVE_PO_CANCELLATION',
      'PURCHASE_ORDER',
      po.id,
      po.poNumber,
      'Status: CANCEL_REQUESTED',
      'Status: CANCELLED | Released all reserved stock back to available pool'
    );

    showToast(`PO ${po.poNumber} cancelled. Reserved stock released.`, 'info');
  };

  // Helper: Create Delivery Challan
  // RULE 7: A PO can have multiple DCs
  // RULE 9: Dispatch cannot exceed reserved quantity
  // RULE 10: Dispatch cannot exceed remaining PO quantity
  const createDeliveryChallan = (dcData: {
    poId: string;
    items: { productId: string; dispatchQuantity: number }[];
    vehicleNumber: string;
    transporterName: string;
    driverName: string;
    driverPhone: string;
    ewayBillNumber: string;
    expectedArrivalDate: string;
    notes: string;
  }): { success: boolean; message: string; dc?: DeliveryChallan } => {
    const po = purchaseOrders.find((p) => p.id === dcData.poId);
    if (!po) return { success: false, message: 'Purchase Order not found.' };

    const dcNumber = `DC-2026-${(40 + deliveryChallans.length + 1).toString().padStart(3, '0')}`;
    const dcItems: DCItem[] = [];

    // STRICT VALIDATION: Dispatch cannot exceed reserved quantity OR remaining PO quantity!
    for (const reqItem of dcData.items) {
      if (reqItem.dispatchQuantity <= 0) continue;
      const poItem = po.items.find((i) => i.productId === reqItem.productId);
      if (!poItem) {
        return { success: false, message: `Product not found in PO items.` };
      }

      const remainingPOQuantity = poItem.quantity - poItem.dispatchedQuantity;
      if (reqItem.dispatchQuantity > remainingPOQuantity) {
        return {
          success: false,
          message: `Dispatch quantity (${reqItem.dispatchQuantity}) cannot exceed remaining PO quantity (${remainingPOQuantity}) for ${poItem.productName}.`,
        };
      }

      if (reqItem.dispatchQuantity > poItem.reservedQuantity) {
        return {
          success: false,
          message: `Dispatch quantity (${reqItem.dispatchQuantity}) cannot exceed reserved quantity (${poItem.reservedQuantity}) for ${poItem.productName}.`,
        };
      }

      dcItems.push({
        productId: poItem.productId,
        sku: poItem.sku,
        productName: poItem.productName,
        orderedQuantity: poItem.quantity,
        reservedQuantity: poItem.reservedQuantity,
        dispatchQuantity: reqItem.dispatchQuantity,
        unit: poItem.unit,
      });
    }

    if (dcItems.length === 0) {
      return { success: false, message: 'Please specify at least one product with dispatch quantity > 0.' };
    }

    const totalQty = dcItems.reduce((acc, it) => acc + it.dispatchQuantity, 0);

    const newDC: DeliveryChallan = {
      id: `DC-${(deliveryChallans.length + 1).toString().padStart(3, '0')}`,
      dcNumber,
      poId: po.id,
      poNumber: po.poNumber,
      clientId: po.clientId,
      clientName: po.clientName,
      deliveryAddress: po.clientSnapshot.deliveryAddress,
      status: 'PENDING_APPROVAL', // Requires Owner / Delegated Admin dispatch approval!
      items: dcItems,
      totalQuantity: totalQty,
      vehicleNumber: dcData.vehicleNumber,
      transporterName: dcData.transporterName,
      driverName: dcData.driverName,
      driverPhone: dcData.driverPhone,
      ewayBillNumber: dcData.ewayBillNumber,
      expectedArrivalDate: dcData.expectedArrivalDate,
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
      createdBy: `${currentUser.name} (${currentUser.role})`,
      attachments: [],
      notes: dcData.notes,
    };

    setDeliveryChallans((prev) => [newDC, ...prev]);

    addNewAuditEntry(
      'CREATE_DELIVERY_CHALLAN',
      'DELIVERY_CHALLAN',
      newDC.id,
      newDC.dcNumber,
      'NONE',
      `Created ${dcNumber} for PO ${po.poNumber} (${totalQty} units). Awaiting Dispatch Approval.`
    );

    const notif: NotificationItem = {
      id: `NOTIF-${Date.now().toString().slice(-4)}`,
      type: 'DISPATCH_APPROVAL_REQUIRED',
      title: `DC ${dcNumber} Requires Dispatch Approval`,
      message: `${currentUser.name} created Delivery Challan ${dcNumber} for ${po.clientName}. Total items: ${totalQty}. Awaiting Owner approval.`,
      relatedRecordType: 'DC',
      relatedRecordId: newDC.id,
      relatedRecordNumber: dcNumber,
      timestamp: 'Just now',
      isRead: false,
      priority: 'HIGH',
    };
    setNotifications((prev) => [notif, ...prev]);

    showToast(`Delivery Challan ${dcNumber} created! Awaiting Owner Dispatch Approval.`, 'success');
    return { success: true, message: `Delivery Challan ${dcNumber} created.`, dc: newDC };
  };

  // Helper: Approve Dispatch
  // Deducts physical inventory (onHand and reserved), updates stock ledger with DC_DISPATCH
  const approveDispatch = (dcId: string) => {
    const dc = deliveryChallans.find((d) => d.id === dcId);
    if (!dc) return;

    const po = purchaseOrders.find((p) => p.id === dc.poId);
    if (!po) return;

    const updatedInventory = [...inventory];
    const newLedgerEntries: StockLedgerEntry[] = [];

    // Deduct physical stock
    dc.items.forEach((dcItem) => {
      const invIndex = updatedInventory.findIndex((i) => i.productId === dcItem.productId);
      if (invIndex !== -1) {
        const inv = updatedInventory[invIndex];
        const newOnHand = Math.max(0, inv.onHand - dcItem.dispatchQuantity);
        const newReserved = Math.max(0, inv.reserved - dcItem.dispatchQuantity);

        updatedInventory[invIndex] = {
          ...inv,
          onHand: newOnHand,
          reserved: newReserved,
          status: newOnHand === 0 ? 'OUT_OF_STOCK' : (newOnHand <= inv.reorderPoint ? 'LOW_STOCK' : 'AVAILABLE'),
          lastUpdated: new Date().toISOString().slice(0, 10),
        };

        newLedgerEntries.push({
          id: `LED-${Date.now().toString().slice(-4)}-DISP-${dcItem.sku}`,
          timestamp: new Date().toISOString().replace('T', ' ').slice(0, 19),
          productId: dcItem.productId,
          sku: dcItem.sku,
          productName: dcItem.productName,
          transactionType: 'DC_DISPATCH',
          quantityChange: -dcItem.dispatchQuantity,
          balanceOnHand: newOnHand,
          referenceId: dc.dcNumber,
          poNumber: po.poNumber,
          dcNumber: dc.dcNumber,
          userName: `${currentUser.name} (${currentUser.role})`,
          remarks: `Physical stock dispatched via ${dc.vehicleNumber} (E-Way: ${dc.ewayBillNumber}).`,
        });
      }
    });

    // Update PO dispatchedQuantity
    const updatedPOItems = po.items.map((poItem) => {
      const dcItem = dc.items.find((i) => i.productId === poItem.productId);
      if (!dcItem) return poItem;
      return {
        ...poItem,
        dispatchedQuantity: poItem.dispatchedQuantity + dcItem.dispatchQuantity,
        reservedQuantity: Math.max(0, poItem.reservedQuantity - dcItem.dispatchQuantity),
      };
    });

    const allDispatched = updatedPOItems.every((i) => i.dispatchedQuantity >= i.quantity);

    const updatedPO: PurchaseOrder = {
      ...po,
      items: updatedPOItems,
      status: allDispatched ? 'NOT_DELIVERED' : 'PARTIALLY_DELIVERED',
      updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
    };

    setPurchaseOrders((prev) => prev.map((p) => (p.id === po.id ? updatedPO : p)));
    setInventory(updatedInventory);
    setStockLedger((prev) => [...newLedgerEntries, ...prev]);

    setDeliveryChallans((prev) =>
      prev.map((d) =>
        d.id === dcId
          ? {
              ...d,
              status: 'DISPATCHED',
              dispatchDate: new Date().toISOString().replace('T', ' ').slice(0, 16),
              approvedBy: `${currentUser.name} (${currentUser.role})`,
              approvedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
            }
          : d
      )
    );

    addNewAuditEntry(
      'APPROVE_DISPATCH',
      'DELIVERY_CHALLAN',
      dc.id,
      dc.dcNumber,
      'Status: PENDING_APPROVAL',
      `Status: DISPATCHED | Physical stock deducted for ${dc.totalQuantity} items | Transporter: ${dc.transporterName}`
    );

    showToast(`Dispatch Approved for ${dc.dcNumber}! Truck loaded and cleared for transit.`, 'success');
  };

  const rejectDispatch = (dcId: string, reason: string) => {
    setDeliveryChallans((prev) =>
      prev.map((d) =>
        d.id === dcId
          ? {
              ...d,
              status: 'CANCELLED',
              notes: `${d.notes ? d.notes + ' | ' : ''}Rejected: ${reason}`,
            }
          : d
      )
    );

    addNewAuditEntry(
      'REJECT_DISPATCH',
      'DELIVERY_CHALLAN',
      dcId,
      dcId,
      'Status: PENDING_APPROVAL',
      `Status: CANCELLED | Reason: ${reason}`
    );

    showToast(`Dispatch rejected for Delivery Challan.`, 'warning');
  };

  // Helper: Confirm Delivery
  // RULE 8: Partial delivery is supported
  // RULE 11: Delivery becomes complete only after all quantities are dispatched and delivery is confirmed
  const confirmDelivery = (deliveryData: {
    dcId: string;
    deliveryDate: string;
    receivedBy: string;
    receiverContact: string;
    remarks: string;
  }) => {
    const dc = deliveryChallans.find((d) => d.id === deliveryData.dcId);
    if (!dc) return;

    const po = purchaseOrders.find((p) => p.id === dc.poId);
    if (!po) return;

    // Update PO deliveredQuantity
    const updatedPOItems = po.items.map((poItem) => {
      const dcItem = dc.items.find((i) => i.productId === poItem.productId);
      if (!dcItem) return poItem;
      return {
        ...poItem,
        deliveredQuantity: poItem.deliveredQuantity + dcItem.dispatchQuantity,
      };
    });

    const isFullyDelivered = updatedPOItems.every((i) => i.deliveredQuantity >= i.quantity);
    const newDeliveryStatus = isFullyDelivered ? 'DELIVERED' : 'PARTIALLY_DELIVERED';
    const newPOStatus = isFullyDelivered ? 'DELIVERED' : 'PARTIALLY_DELIVERED';

    const newDeliveryRecord: DeliveryRecord = {
      id: `DEL-${Date.now().toString().slice(-4)}`,
      dcId: dc.id,
      dcNumber: dc.dcNumber,
      poId: po.id,
      poNumber: po.poNumber,
      clientId: po.clientId,
      clientName: po.clientName,
      dispatchedQuantity: dc.totalQuantity,
      deliveredQuantity: dc.totalQuantity,
      deliveryDate: deliveryData.deliveryDate,
      receivedBy: deliveryData.receivedBy,
      receiverContact: deliveryData.receiverContact,
      status: newDeliveryStatus,
      remarks: deliveryData.remarks,
      verifiedBy: `${currentUser.name} (${currentUser.role})`,
      verifiedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
    };

    setDeliveries((prev) => [newDeliveryRecord, ...prev]);

    // Update DC status
    setDeliveryChallans((prev) =>
      prev.map((d) => (d.id === dc.id ? { ...d, status: 'DELIVERED' } : d))
    );

    // Update PO status
    setPurchaseOrders((prev) =>
      prev.map((p) =>
        p.id === po.id
          ? {
              ...p,
              items: updatedPOItems,
              deliveryStatus: newDeliveryStatus,
              status: newPOStatus,
              reservationStatus: isFullyDelivered ? 'RELEASED' : p.reservationStatus,
              updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
            }
          : p
      )
    );

    addNewAuditEntry(
      'CONFIRM_DELIVERY',
      'DELIVERY',
      newDeliveryRecord.id,
      dc.dcNumber,
      'Delivery Status: DISPATCHED',
      `Delivery Status: ${newDeliveryStatus} | Received by: ${deliveryData.receivedBy} | PO ${po.poNumber} is ${isFullyDelivered ? 'COMPLETED' : 'PARTIAL'}`
    );

    const notif: NotificationItem = {
      id: `NOTIF-${Date.now().toString().slice(-4)}`,
      type: isFullyDelivered ? 'DELIVERY_COMPLETED' : 'PARTIAL_DELIVERY',
      title: isFullyDelivered ? `PO ${po.poNumber} Fully Delivered!` : `Partial Delivery Verified: ${dc.dcNumber}`,
      message: isFullyDelivered
        ? `All items on PO ${po.poNumber} delivered to ${po.clientName}. Project lifecycle complete.`
        : `${dc.totalQuantity} items under ${dc.dcNumber} confirmed received by ${deliveryData.receivedBy}.`,
      relatedRecordType: 'PO',
      relatedRecordId: po.id,
      relatedRecordNumber: po.poNumber,
      timestamp: 'Just now',
      isRead: false,
      priority: isFullyDelivered ? 'HIGH' : 'NORMAL',
    };
    setNotifications((prev) => [notif, ...prev]);

    showToast(
      isFullyDelivered 
        ? `Delivery Verified! PO ${po.poNumber} is now 100% Completed!` 
        : `Partial Delivery confirmed for ${dc.dcNumber}.`,
      'success'
    );
  };

  // Helper: Stock Shortage Resolution with Incoming Production Batch
  const resolveShortageWithIncomingStock = (shortageId: string, quantityAdded: number) => {
    const shortage = shortages.find((s) => s.id === shortageId);
    if (!shortage) return;

    const po = purchaseOrders.find((p) => p.id === shortage.poId);
    const updatedInventory = [...inventory];
    const invIndex = updatedInventory.findIndex((i) => i.productId === shortage.productId);

    if (invIndex === -1) return;

    // Incoming stock arrives at godown
    const currentInv = updatedInventory[invIndex];
    const newOnHand = currentInv.onHand + quantityAdded;
    
    // Automatically allocate to the shortage PO
    const qtyToAllocate = Math.min(quantityAdded, shortage.shortageQuantity);
    const newReserved = currentInv.reserved + qtyToAllocate;
    const remainingShortage = shortage.shortageQuantity - qtyToAllocate;

    updatedInventory[invIndex] = {
      ...currentInv,
      onHand: newOnHand,
      reserved: newReserved,
      status: (newOnHand - newReserved) > 0 ? 'AVAILABLE' : 'RESERVED',
      lastUpdated: new Date().toISOString().slice(0, 10),
    };

    // Update shortage status
    setShortages((prev) =>
      prev.map((s) =>
        s.id === shortageId
          ? {
              ...s,
              reservedQuantity: s.reservedQuantity + qtyToAllocate,
              shortageQuantity: remainingShortage,
              status: remainingShortage === 0 ? 'RESOLVED' : 'PARTIALLY_RESOLVED',
              resolvedAt: remainingShortage === 0 ? new Date().toISOString().slice(0, 10) : undefined,
            }
          : s
      )
    );

    // Update PO item
    if (po) {
      const updatedPOItems = po.items.map((item) => {
        if (item.productId === shortage.productId) {
          return {
            ...item,
            reservedQuantity: item.reservedQuantity + qtyToAllocate,
          };
        }
        return item;
      });

      const allItemsReserved = updatedPOItems.every((i) => i.reservedQuantity >= i.quantity);

      setPurchaseOrders((prev) =>
        prev.map((p) =>
          p.id === po.id
            ? {
                ...p,
                items: updatedPOItems,
                reservationStatus: allItemsReserved ? 'STOCK_RESERVED' : 'PARTIALLY_RESERVED',
                status: allItemsReserved ? 'APPROVED' : p.status,
                updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
              }
            : p
        )
      );
    }

    setInventory(updatedInventory);

    // Stock ledger transactions: INCOMING_PRODUCTION + PO_RESERVATION
    const newLedgers: StockLedgerEntry[] = [
      {
        id: `LED-${Date.now().toString().slice(-4)}-INC`,
        timestamp: new Date().toISOString().replace('T', ' ').slice(0, 19),
        productId: shortage.productId,
        sku: shortage.sku,
        productName: shortage.productName,
        transactionType: 'INCOMING_PRODUCTION',
        quantityChange: quantityAdded,
        balanceOnHand: newOnHand,
        referenceId: `GRN-${Date.now().toString().slice(-4)}`,
        userName: `${currentUser.name} (Godown Inward)`,
        remarks: `Factory production lot received into Godown #1 (${quantityAdded} units).`,
      },
      {
        id: `LED-${Date.now().toString().slice(-4)}-RES`,
        timestamp: new Date().toISOString().replace('T', ' ').slice(0, 19),
        productId: shortage.productId,
        sku: shortage.sku,
        productName: shortage.productName,
        transactionType: 'PO_RESERVATION',
        quantityChange: 0,
        balanceOnHand: newOnHand,
        referenceId: shortage.poNumber,
        poNumber: shortage.poNumber,
        userName: 'System (Auto-Allocation Engine)',
        remarks: `Auto-allocated ${qtyToAllocate} incoming units to resolve shortage on ${shortage.poNumber}.`,
      },
    ];
    setStockLedger((prev) => [...newLedgers, ...prev]);

    addNewAuditEntry(
      'RESOLVE_SHORTAGE',
      'INVENTORY',
      shortage.productId,
      shortage.sku,
      `Shortage: ${shortage.shortageQuantity}`,
      `Added ${quantityAdded} units. Allocated ${qtyToAllocate} to ${shortage.poNumber}. Remaining shortage: ${remainingShortage}`
    );

    const notif: NotificationItem = {
      id: `NOTIF-${Date.now().toString().slice(-4)}`,
      type: 'STOCK_SHORTAGE_RESOLVED',
      title: remainingShortage === 0 ? `Shortage Fully Resolved: ${shortage.sku}` : `Partial Shortage Allocation: ${shortage.sku}`,
      message: `${qtyToAllocate} units allocated to ${shortage.poNumber}. ${remainingShortage === 0 ? 'PO is now fully reserved!' : `${remainingShortage} still needed.`}`,
      relatedRecordType: 'SHORTAGE',
      relatedRecordId: shortage.id,
      relatedRecordNumber: shortage.poNumber,
      timestamp: 'Just now',
      isRead: false,
      priority: 'NORMAL',
    };
    setNotifications((prev) => [notif, ...prev]);

    showToast(
      remainingShortage === 0
        ? `Shortage resolved! ${qtyToAllocate} units auto-reserved for ${shortage.poNumber}.`
        : `Allocated ${qtyToAllocate} units. Remaining shortage: ${remainingShortage}`,
      'success'
    );
  };

  // Helper: Manual Inventory Stock Adjustment
  const adjustInventoryStock = (productId: string, adjustmentQty: number, remarks: string) => {
    const updatedInventory = [...inventory];
    const idx = updatedInventory.findIndex((i) => i.productId === productId);
    if (idx === -1) return;

    const item = updatedInventory[idx];
    const newOnHand = Math.max(item.reserved, item.onHand + adjustmentQty);

    updatedInventory[idx] = {
      ...item,
      onHand: newOnHand,
      status: (newOnHand - item.reserved) <= 0 ? 'RESERVED' : (newOnHand <= item.reorderPoint ? 'LOW_STOCK' : 'AVAILABLE'),
      lastUpdated: new Date().toISOString().slice(0, 10),
    };

    setInventory(updatedInventory);

    const ledger: StockLedgerEntry = {
      id: `LED-${Date.now().toString().slice(-4)}-ADJ`,
      timestamp: new Date().toISOString().replace('T', ' ').slice(0, 19),
      productId: item.productId,
      sku: item.sku,
      productName: item.productName,
      transactionType: 'MANUAL_ADJUSTMENT',
      quantityChange: adjustmentQty,
      balanceOnHand: newOnHand,
      referenceId: `ADJ-${Date.now().toString().slice(-4)}`,
      userName: `${currentUser.name} (${currentUser.role})`,
      remarks: remarks || 'Manual stock adjustment verified by warehouse in-charge.',
    };
    setStockLedger((prev) => [ledger, ...prev]);

    addNewAuditEntry(
      'MANUAL_STOCK_ADJUSTMENT',
      'INVENTORY',
      item.productId,
      item.sku,
      `On Hand: ${item.onHand}`,
      `On Hand: ${newOnHand} (Change: ${adjustmentQty > 0 ? '+' : ''}${adjustmentQty}) | Reason: ${remarks}`
    );

    showToast(`Inventory adjusted for ${item.sku}. New On Hand: ${newOnHand}`, 'info');
  };

  // Helper: Submit Website Enquiry from Public Website
  const submitWebsiteEnquiry = (enquiryData: {
    name: string;
    company?: string;
    phone: string;
    email: string;
    productId?: string;
    productName?: string;
    quantity?: number;
    message: string;
    state?: string;
  }) => {
    const newEnq: WebsiteEnquiry = {
      id: `ENQ-${Math.floor(910 + Math.random() * 89)}`,
      name: enquiryData.name,
      company: enquiryData.company,
      phone: enquiryData.phone,
      email: enquiryData.email,
      productId: enquiryData.productId,
      productName: enquiryData.productName,
      quantity: enquiryData.quantity,
      message: enquiryData.message,
      state: enquiryData.state || 'Karnataka',
      status: 'NEW',
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
      assignedTo: 'Rajesh Verma (Main Office)',
      timeline: [
        {
          timestamp: new Date().toISOString().replace('T', ' ').slice(0, 16),
          action: 'Inbound Web Portal Submission',
          by: 'System (Public Corporate Website)',
          notes: 'Customer submitted machinery quote request online.',
        },
      ],
    };

    setWebsiteEnquiries((prev) => [newEnq, ...prev]);

    // Push notification to Main Office
    const notif: NotificationItem = {
      id: `NOTIF-${Date.now().toString().slice(-4)}`,
      type: 'PO_SUBMITTED',
      title: `New Website Enquiry: ${newEnq.name}`,
      message: `${newEnq.company || newEnq.name} enquired about ${newEnq.productName || 'Seed Processing Plants'}. Phone: ${newEnq.phone}`,
      relatedRecordType: 'ENQUIRY',
      relatedRecordId: newEnq.id,
      relatedRecordNumber: newEnq.id,
      timestamp: 'Just now',
      isRead: false,
      priority: 'HIGH',
    };
    setNotifications((prev) => [notif, ...prev]);
  };

  const updateEnquiryStatus = (enquiryId: string, status: WebsiteEnquiry['status'], notes?: string) => {
    setWebsiteEnquiries((prev) =>
      prev.map((e) =>
        e.id === enquiryId
          ? {
              ...e,
              status,
              timeline: [
                ...e.timeline,
                {
                  timestamp: new Date().toISOString().replace('T', ' ').slice(0, 16),
                  action: `Status changed to ${status}`,
                  by: `${currentUser.name} (${currentUser.role})`,
                  notes: notes || `Follow-up updated in system.`,
                },
              ],
            }
          : e
      )
    );
    showToast(`Enquiry ${enquiryId} status updated to ${status}`, 'info');
  };

  const markNotificationAsRead = (id: string) => {
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)));
  };

  const markAllNotificationsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    showToast('All notifications marked as read', 'info');
  };

  return (
    <AppContext.Provider
      value={{
        appMode,
        setAppMode,
        currentUser,
        setCurrentUserRole,
        currentOffice,
        setCurrentOffice,
        currentTab,
        setCurrentTab,
        publicNav,
        setPublicNav,
        isLoggedIn,
        setIsLoggedIn,
        users,
        offices,
        products,
        clients,
        inventory,
        purchaseOrders,
        shortages,
        deliveryChallans,
        deliveries,
        stockLedger,
        websiteEnquiries,
        notifications,
        auditRecords,
        selectedPOId,
        setSelectedPOId,
        selectedClientId,
        setSelectedClientId,
        selectedProductId,
        setSelectedProductId,
        selectedDCId,
        setSelectedDCId,
        createPurchaseOrder,
        approvePurchaseOrder,
        returnPurchaseOrder,
        requestCancelPurchaseOrder,
        approveCancellation,
        createDeliveryChallan,
        approveDispatch,
        rejectDispatch,
        confirmDelivery,
        resolveShortageWithIncomingStock,
        adjustInventoryStock,
        submitWebsiteEnquiry,
        updateEnquiryStatus,
        markNotificationAsRead,
        markAllNotificationsRead,
        addNewAuditEntry,
        showGuidedTour,
        setShowGuidedTour,
        toastMessage,
        showToast,
        isPublicWebsite: appMode === 'public',
        setIsPublicWebsite: (val: boolean) => setAppMode(val ? 'public' : 'internal'),
        createWebsiteEnquiry: submitWebsiteEnquiry,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
