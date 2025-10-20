// Centralized status and badge mappings

// Domain-specific status maps
const ORDER_STATUS = {
  PENDING: { class: 'badge-warning', text: 'Chờ xử lý' },
  CONFIRMED: { class: 'badge-info', text: 'Đã xác nhận' },
  PROCESSING: { class: 'badge-info', text: 'Đang xử lý' },
  SHIPPED: { class: 'badge-success', text: 'Đã giao hàng' },
  DELIVERED: { class: 'badge-success', text: 'Đã giao' },
  CANCELLED: { class: 'badge-danger', text: 'Đã hủy' },
  REFUNDED: { class: 'badge-gray', text: 'Đã hoàn tiền' }
};

const CONTRACT_STATUS = {
  DRAFT: { class: 'badge-gray', text: 'Nháp' },
  PENDING_SIGNATURE: { class: 'badge-warning', text: 'Chờ ký' },
  SIGNED: { class: 'badge-success', text: 'Đã ký' },
  ACTIVE: { class: 'badge-info', text: 'Có hiệu lực' },
  COMPLETED: { class: 'badge-success', text: 'Hoàn thành' },
  CANCELLED: { class: 'badge-danger', text: 'Đã hủy' },
  EXPIRED: { class: 'badge-warning', text: 'Hết hạn' }
};

const DELIVERY_STATUS = {
  SCHEDULED: { class: 'badge-info', text: 'Đã lên lịch' },
  IN_TRANSIT: { class: 'badge-warning', text: 'Đang giao' },
  DELIVERED: { class: 'badge-success', text: 'Đã giao' },
  FAILED: { class: 'badge-danger', text: 'Giao thất bại' },
  CANCELLED: { class: 'badge-gray', text: 'Đã hủy' }
};

const PAYMENT_STATUS = {
  PENDING: { class: 'badge-warning', text: 'Chờ xử lý' },
  PROCESSING: { class: 'badge-info', text: 'Đang xử lý' },
  COMPLETED: { class: 'badge-success', text: 'Hoàn thành' },
  FAILED: { class: 'badge-danger', text: 'Thất bại' },
  CANCELLED: { class: 'badge-gray', text: 'Đã hủy' },
  REFUNDED: { class: 'badge-gray', text: 'Đã hoàn tiền' }
};

const QUOTATION_STATUS = {
  DRAFT: { class: 'badge-gray', text: 'Nháp' },
  SENT: { class: 'badge-info', text: 'Đã gửi' },
  ACCEPTED: { class: 'badge-success', text: 'Đã chấp nhận' },
  REJECTED: { class: 'badge-danger', text: 'Từ chối' },
  EXPIRED: { class: 'badge-warning', text: 'Hết hạn' }
};

const INVENTORY_STATUS = {
  AVAILABLE: { class: 'badge-success', text: 'Có sẵn' },
  RESERVED: { class: 'badge-warning', text: 'Đã đặt' },
  SOLD: { class: 'badge-info', text: 'Đã bán' },
  MAINTENANCE: { class: 'badge-danger', text: 'Bảo trì' }
};

// Roles
const ROLE_BADGES = {
  admin: { class: 'badge-danger', text: 'Admin' },
  evm_staff: { class: 'badge-info', text: 'EVM Staff' },
  dealer_manager: { class: 'badge-warning', text: 'Dealer Manager' },
  dealer_staff: { class: 'badge-success', text: 'Dealer Staff' }
};

// Active/inactive used by users, vehicles, warehouses
export const getActiveBadge = (isActive) => ({
  class: isActive ? 'badge-success' : 'badge-danger',
  text: isActive ? 'Hoạt động' : 'Không hoạt động'
});

export const getRoleBadge = (roleName) => {
  return ROLE_BADGES[roleName] || { class: 'badge-gray', text: roleName || 'N/A' };
};

const DOMAIN_MAP = {
  order: ORDER_STATUS,
  contract: CONTRACT_STATUS,
  delivery: DELIVERY_STATUS,
  payment: PAYMENT_STATUS,
  quotation: QUOTATION_STATUS,
  inventory: INVENTORY_STATUS
};

export const getStatusBadge = (domain, status) => {
  const map = DOMAIN_MAP[domain] || {};
  return map[status] || { class: 'badge-gray', text: status || 'N/A' };
};



