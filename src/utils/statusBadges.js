// Centralized status and badge mappings

// Domain-specific status maps
const ORDER_STATUS = {
  pending: { class: 'badge-warning', text: 'Chờ xử lý' },
  quoted: { class: 'badge-info', text: 'Đã có báo giá' },
  confirmed: { class: 'badge-info', text: 'Đã xác nhận' },
  paid: { class: 'badge-success', text: 'Đã thanh toán' },
  delivered: { class: 'badge-success', text: 'Đã giao hàng' },
  completed: { class: 'badge-success', text: 'Hoàn tất' },
  rejected: { class: 'badge-danger', text: 'Đã từ chối' }
};

const CONTRACT_STATUS = {
  draft: { class: 'badge-gray', text: 'Nháp' },
  pending: { class: 'badge-warning', text: 'Chờ xử lý' },
  signed: { class: 'badge-success', text: 'Đã ký' },
  active: { class: 'badge-info', text: 'Có hiệu lực' },
  completed: { class: 'badge-success', text: 'Hoàn thành' },
  cancelled: { class: 'badge-danger', text: 'Đã hủy' },
  expired: { class: 'badge-warning', text: 'Hết hạn' }
};

const DELIVERY_STATUS = {
  pending: { class: 'badge-warning', text: 'Chờ giao hàng' },
  scheduled: { class: 'badge-info', text: 'Đã lên lịch' },
  in_transit: { class: 'badge-warning', text: 'Đang giao' },
  delivered: { class: 'badge-success', text: 'Đã giao' },
  cancelled: { class: 'badge-gray', text: 'Đã hủy' }
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
  pending: { class: 'badge-warning', text: 'Chờ phản hồi' },
  sent: { class: 'badge-info', text: 'Đã gửi' },
  accepted: { class: 'badge-success', text: 'Đã chấp nhận' },
  rejected: { class: 'badge-danger', text: 'Đã từ chối' },
  expired: { class: 'badge-warning', text: 'Hết hạn' }
};

const INVENTORY_STATUS = {
  available: { class: 'badge-success', text: 'Có sẵn' },
  reserved: { class: 'badge-warning', text: 'Đã đặt' },
  sold: { class: 'badge-info', text: 'Đã bán' },
  maintenance: { class: 'badge-danger', text: 'Bảo trì' },
  damaged: { class: 'badge-danger', text: 'Hư hỏng' },
  in_transit: { class: 'badge-info', text: 'Đang vận chuyển' },
  pending_delivery: { class: 'badge-warning', text: 'Chờ giao hàng' }
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
  // If roleName is null, undefined, or empty, return N/A badge
  if (!roleName || roleName === 'null' || roleName === 'undefined') {
    return { class: 'badge-gray', text: 'N/A' };
  }
  
  // Normalize roleName to lowercase for comparison
  const normalizedRole = String(roleName).toLowerCase().trim();
  
  // Check if we have a badge for this role
  if (ROLE_BADGES[normalizedRole]) {
    return ROLE_BADGES[normalizedRole];
  }
  
  // Fallback: return formatted role name
  return { 
    class: 'badge-gray', 
    text: normalizedRole.charAt(0).toUpperCase() + normalizedRole.slice(1).replace('_', ' ') || 'N/A' 
  };
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



