export type UserRole = 'OWNER' | 'ADMIN' | 'MAIN_OFFICE' | 'OUTER_STATE_OFFICE' | 'GODOWN_MANAGER';

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  officeId: string;
  officeName: string;
  state: string;
  phone: string;
  avatar?: string;
  status: 'ACTIVE' | 'INACTIVE';
  lastLogin: string;
  allowedOffices: string[];
}

export interface Office {
  id: string;
  name: string;
  type: 'HEADQUARTERS' | 'REGIONAL_OFFICE' | 'WAREHOUSE_GODOWN';
  state: string;
  city: string;
  address: string;
  gstin: string;
  phone: string;
  email: string;
  managerName: string;
  status: 'ACTIVE' | 'INACTIVE';
  assignedUsersCount: number;
}

export interface ClientAddress {
  id: string;
  label: string;
  street: string;
  city: string;
  state: string;
  pincode: string;
  isDefault: boolean;
  siteName?: string;
  addressLine1?: string;
  contactPerson?: string;
  phone?: string;
}

export interface ClientContact {
  name: string;
  designation: string;
  phone: string;
  email: string;
  isPrimary: boolean;
}

export interface Client {
  id: string;
  companyName: string;
  clientType: 'SEED_PRODUCER' | 'GOVT_CORPORATION' | 'AGRI_PROCESSOR' | 'EXPORTER';
  gstin: string;
  pan: string;
  state: string;
  primaryContact: ClientContact;
  billingContact?: ClientContact;
  additionalContacts: ClientContact[];
  deliveryAddresses: ClientAddress[];
  status: 'ACTIVE' | 'INACTIVE';
  creditDays: number;
  totalPOs: number;
  createdAt: string;
}

export interface ProductSpec {
  capacity: string;
  motorPower: string;
  motorRating?: string;
  dimensions: string;
  airVolume?: string;
  screenArea?: string;
  weight: string;
  seedCompatibility: string[];
}

export interface Product {
  id: string;
  sku: string;
  name: string;
  category: 'SEED_CLEANER' | 'GRAVITY_SEPARATOR' | 'DESTONER' | 'SEED_TREATER' | 'ELEVATOR' | 'COLOR_SORTER' | 'SPARE_PARTS' | string;
  unit: string;
  description: string;
  specs: ProductSpec;
  basePriceINR: number;
  internalStatus: 'ACTIVE' | 'INACTIVE';
  websiteVisibility: 'PUBLIC' | 'HIDDEN';
  brochureUrl?: string;
  leadTimeWeeks: number;
  updatedAt: string;
}

export interface InventoryItem {
  productId: string;
  sku: string;
  productName: string;
  unit: string;
  onHand: number;
  reserved: number;
  // available = onHand - reserved
  status: 'AVAILABLE' | 'RESERVED' | 'LOW_STOCK' | 'INSUFFICIENT' | 'OUT_OF_STOCK';
  reorderPoint: number;
  warehouseLocation: string;
  storageLocation?: string;
  lastUpdated: string;
}

export type POStatus = 
  | 'DRAFT'
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'STOCK_RESERVATION'
  | 'NOT_DELIVERED'
  | 'PARTIALLY_DELIVERED'
  | 'DELIVERED'
  | 'RETURNED'
  | 'CANCEL_REQUESTED'
  | 'CANCELLED';

export type ReservationStatus = 
  | 'PENDING_RESERVATION'
  | 'STOCK_RESERVED'
  | 'PARTIALLY_RESERVED'
  | 'INSUFFICIENT_STOCK'
  | 'RELEASED';

export type DeliveryStatus = 
  | 'NOT_DELIVERED'
  | 'PARTIALLY_DELIVERED'
  | 'DELIVERED';

export type Priority = 'NORMAL' | 'HIGH' | 'URGENT';

export interface POItem {
  id: string;
  productId: string;
  sku: string;
  productName: string;
  description: string;
  unit: string;
  quantity: number;
  reservedQuantity: number;
  dispatchedQuantity: number;
  deliveredQuantity: number;
  unitPriceINR: number;
  expectedDeliveryDate: string;
}

export interface Attachment {
  id: string;
  name?: string;
  filename?: string;
  fileType?: string;
  fileSize: string;
  version?: number;
  uploadedBy: string;
  uploadedAt: string;
  category: 'PURCHASE_ORDER' | 'TECHNICAL_SPEC' | 'EWAY_BILL' | 'PROOF_OF_DELIVERY' | 'GENERAL' | 'PO_DOCUMENT' | string;
  isDeleted?: boolean;
}

export interface POVersionChange {
  field: string;
  previousValue: string;
  newValue: string;
  changedBy: string;
  timestamp: string;
}

export interface POVersion {
  versionNumber: number;
  createdAt: string;
  createdBy: string;
  reason: string;
  changes: POVersionChange[];
  snapshot: any;
}

export interface PurchaseOrder {
  id: string;
  poNumber: string;
  poDate: string;
  clientId: string;
  clientName: string;
  clientSnapshot: {
    companyName: string;
    gstin: string;
    pan: string;
    primaryContact: ClientContact;
    deliveryAddress: ClientAddress;
  };
  officeId: string;
  officeName: string;
  state: string;
  priority: Priority;
  expectedDeliveryDate: string;
  status: POStatus;
  reservationStatus: ReservationStatus;
  deliveryStatus: DeliveryStatus;
  items: POItem[];
  totalAmountINR: number;
  attachments: Attachment[];
  versions: POVersion[];
  currentVersion: number;
  notes: string;
  createdBy: string;
  createdById: string;
  createdAt: string;
  updatedAt: string;
  approvedBy?: string;
  approvedAt?: string;
  rejectionReason?: string;
  cancellationReason?: string;
}

export interface StockShortage {
  id: string;
  poId: string;
  poNumber: string;
  productId: string;
  sku: string;
  productName: string;
  requiredQuantity: number;
  reservedQuantity: number;
  shortageQuantity: number;
  poDate: string;
  expectedDeliveryDate: string;
  priority: Priority;
  status: 'OPEN' | 'PARTIALLY_RESOLVED' | 'RESOLVED';
  reason: string;
  createdAt: string;
  resolvedAt?: string;
}

export type DCStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'DISPATCHED' | 'DELIVERED' | 'CANCELLED';

export interface DCItem {
  productId: string;
  sku: string;
  productName: string;
  orderedQuantity: number;
  reservedQuantity: number;
  dispatchQuantity: number;
  unit: string;
}

export interface DeliveryChallan {
  id: string;
  dcNumber: string;
  poId: string;
  poNumber: string;
  clientId: string;
  clientName: string;
  deliveryAddress: ClientAddress;
  status: DCStatus;
  items: DCItem[];
  totalQuantity: number;
  vehicleNumber: string;
  transporterName: string;
  driverName: string;
  driverPhone: string;
  ewayBillNumber: string;
  dispatchDate?: string;
  expectedArrivalDate: string;
  createdAt: string;
  createdBy: string;
  approvedBy?: string;
  approvedAt?: string;
  attachments: Attachment[];
  notes: string;
}

export interface DeliveryRecord {
  id: string;
  dcId: string;
  dcNumber: string;
  poId: string;
  poNumber: string;
  clientId: string;
  clientName: string;
  dispatchedQuantity: number;
  deliveredQuantity: number;
  deliveryDate: string;
  receivedBy: string;
  receiverContact: string;
  podAttachmentId?: string;
  status: DeliveryStatus;
  remarks: string;
  verifiedBy: string;
  verifiedAt: string;
}

export type StockTransactionType = 
  | 'INITIAL_STOCK'
  | 'PO_RESERVATION'
  | 'PO_UNRESERVATION'
  | 'DC_DISPATCH'
  | 'MANUAL_ADJUSTMENT'
  | 'INCOMING_PRODUCTION';

export interface StockLedgerEntry {
  id: string;
  timestamp: string;
  productId: string;
  sku: string;
  productName: string;
  transactionType: StockTransactionType;
  quantityChange: number; // positive or negative
  balanceOnHand: number;
  referenceId: string;
  poNumber?: string;
  dcNumber?: string;
  userName: string;
  remarks: string;
}

export interface WebsiteEnquiry {
  id: string;
  name: string;
  company?: string;
  phone: string;
  email: string;
  productId?: string;
  productName?: string;
  quantity?: number;
  message: string;
  state?: string;
  status: 'NEW' | 'CONTACTED' | 'FOLLOW_UP' | 'CONVERTED' | 'CLOSED';
  createdAt: string;
  assignedTo?: string;
  timeline: {
    timestamp: string;
    action: string;
    by: string;
    notes: string;
  }[];
}

export interface NotificationItem {
  id: string;
  type: 
    | 'PO_SUBMITTED'
    | 'PO_RETURNED'
    | 'PO_APPROVED'
    | 'STOCK_RESERVED'
    | 'INSUFFICIENT_STOCK'
    | 'CANCELLATION_REQUESTED'
    | 'DISPATCH_APPROVAL_REQUIRED'
    | 'DC_CREATED'
    | 'PARTIAL_DELIVERY'
    | 'DELIVERY_COMPLETED'
    | 'PO_OVERDUE'
    | 'STOCK_SHORTAGE_RESOLVED';
  title: string;
  message: string;
  relatedRecordType: 'PO' | 'DC' | 'SHORTAGE' | 'ENQUIRY' | 'INVENTORY';
  relatedRecordId: string;
  relatedRecordNumber: string;
  timestamp: string;
  isRead: boolean;
  priority: 'NORMAL' | 'HIGH';
}

export interface AuditRecord {
  id: string;
  correlationId: string;
  timestamp: string;
  userName: string;
  userRole: UserRole;
  action: string;
  resourceType: 'PURCHASE_ORDER' | 'INVENTORY' | 'DELIVERY_CHALLAN' | 'DELIVERY' | 'CLIENT' | 'PRODUCT' | 'USER';
  resourceId: string;
  resourceIdentifier: string;
  oldValue?: string;
  newValue?: string;
  ipAddress: string;
  deviceInfo: string;
}
