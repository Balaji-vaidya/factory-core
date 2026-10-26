import React from 'react';
import { POStatus, ReservationStatus, DeliveryStatus, DCStatus } from '../../types';

interface BadgeProps {
  status: string;
  type?: 'po' | 'reservation' | 'delivery' | 'dc' | 'priority' | 'role' | 'general';
  size?: 'sm' | 'md';
}

export const Badge: React.FC<BadgeProps> = ({ status, type = 'general', size = 'sm' }) => {
  const getBadgeStyle = () => {
    switch (status) {
      // Success Statuses (#15803D)
      case 'APPROVED':
      case 'STOCK_RESERVED':
      case 'AVAILABLE':
      case 'DELIVERED':
      case 'ACTIVE':
      case 'RESOLVED':
      case 'CONVERTED':
        return 'badge-success';

      // Warning Statuses (#D97706)
      case 'PENDING_APPROVAL':
      case 'PENDING_OWNER_APPROVAL':
      case 'PARTIALLY_DELIVERED':
      case 'PARTIALLY_RESERVED':
      case 'LOW_STOCK':
      case 'HIGH':
      case 'CONTACTED':
      case 'FOLLOW_UP':
      case 'PARTIALLY_RESOLVED':
        return 'badge-warning';

      // Error Statuses (#DC2626)
      case 'RETURNED':
      case 'CANCEL_REQUESTED':
      case 'CANCELLED':
      case 'INSUFFICIENT_STOCK':
      case 'INSUFFICIENT':
      case 'OUT_OF_STOCK':
      case 'URGENT':
      case 'INACTIVE':
        return 'badge-error';

      // Info Statuses (#2563EB)
      case 'OPEN':
      case 'NEW':
      case 'DISPATCHED':
      case 'STOCK_RESERVATION':
      case 'NOT_DELIVERED':
      case 'QUALIFIED':
        return 'badge-info';

      // Neutral Statuses (Neutral 700 / 600 / 500)
      case 'DRAFT':
      case 'PENDING_RESERVATION':
      case 'RELEASED':
      case 'NORMAL':
      case 'CLOSED':
      default:
        return 'badge-neutral';
    }
  };

  const formatText = (text: string) => {
    return text.replace(/_/g, ' ');
  };

  const sizeClasses = size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-3 py-1 text-xs';

  return (
    <span
      className={`inline-flex items-center text-label border rounded-md whitespace-nowrap tracking-wide ${sizeClasses} ${getBadgeStyle()}`}
    >
      {formatText(status)}
    </span>
  );
};
