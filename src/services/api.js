import axios from 'axios';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Create public axios instance (no auth required)
const publicApi = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api/public',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});


// Export public API for use in components
export const publicAPI = publicApi;

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    const isValidToken =
      typeof token === 'string' &&
      token !== 'null' &&
      token !== 'undefined' &&
      token.trim() !== '';
    if (isValidToken) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  validate: () => api.post('/auth/validate'),
  logout: () => api.post('/auth/logout'),
};

// Vehicle API (Authenticated)
export const vehicleAPI = {
  // Brands
  getBrands: () => api.get('/vehicles/brands'),
  getActiveBrands: () => api.get('/vehicles/brands/active'),
  getBrand: (id) => api.get(`/vehicles/brands/${id}`),
  createBrand: (data) => api.post('/vehicles/brands', data),
  updateBrand: (id, data) => api.put(`/vehicles/brands/${id}`, data),
  deleteBrand: (id) => api.delete(`/vehicles/brands/${id}`),

  // Models
  getModels: () => api.get('/vehicles/models'),
  getActiveModels: () => api.get('/vehicles/models/active'),
  getModel: (id) => api.get(`/vehicles/models/${id}`),
  createModel: (data) => api.post('/vehicles/models', data),
  updateModel: (id, data) => api.put(`/vehicles/models/${id}`, data),
  deleteModel: (id) => api.delete(`/vehicles/models/${id}`),

  // Variants
  getVariants: () => api.get('/vehicles/variants'),
  getActiveVariants: () => api.get('/vehicles/variants/active'),
  getVariant: (id) => api.get(`/vehicles/variants/${id}`),
  createVariant: (data) => api.post('/vehicles/variants', data),
  updateVariant: (id, data) => api.put(`/vehicles/variants/${id}`, data),
  deleteVariant: (id) => api.delete(`/vehicles/variants/${id}`),

  // Colors
  getColors: () => api.get('/vehicles/colors'),
  getActiveColors: () => api.get('/vehicles/colors/active'),
  getColor: (id) => api.get(`/vehicles/colors/${id}`),
  createColor: (data) => api.post('/vehicles/colors', data),
  updateColor: (id, data) => api.put(`/vehicles/colors/${id}`, data),
  deleteColor: (id) => api.delete(`/vehicles/colors/${id}`),

  // Vehicle Comparison
  getAvailableVehicles: () => api.get('/vehicles/compare/available'),
  quickCompare: (variantIds) => api.get(`/vehicles/compare/quick?variantIds=${variantIds.join(',')}`),
  detailedCompare: (data) => api.post('/vehicles/compare', data),
  compareTwoVehicles: (id1, id2) => api.post(`/vehicles/compare/${id1}/vs/${id2}`),
  getComparisonCriteria: () => api.get('/vehicles/compare/criteria'),

  // Complete Vehicle Creation (Tạo xe hoàn chỉnh từ đầu)
  createFullVehicle: (data) => api.post('/vehicle-creation/create-full-vehicle', data, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }),

  // Create Vehicle From Existing Data (Tạo xe từ dữ liệu có sẵn)
  createVehicleFromExisting: (data) => api.post('/vehicle-creation-from-existing/create', data, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }),
  
  // Create Vehicle From Existing Data (JSON version - easier for frontend)
  createVehicleFromExistingJson: (data) => api.post('/vehicles/create-from-existing-json', data, {
    headers: {
      'Content-Type': 'application/json',
    },
  }),
  getCreateFromExistingBrands: () => api.get('/vehicle-creation-from-existing/brands'),
  getCreateFromExistingModelsByBrand: (brandId) => api.get(`/vehicle-creation-from-existing/models/${brandId}`),
  getCreateFromExistingColors: () => api.get('/vehicle-creation-from-existing/colors'),
  getCreateFromExistingWarehouses: () => api.get('/vehicle-creation-from-existing/warehouses'),
};

// Customer API
export const customerAPI = {
  getCustomers: () => api.get('/customers'),
  getCustomer: (id) => api.get(`/customers/${id}`),
  getCustomerByEmail: (email) => api.get(`/customers/email/${email}`),
  searchCustomers: (name) => api.get(`/customers/search?name=${name}`),
  searchCustomersByEmail: (email) => api.get(`/customers/search?email=${email}`),
  createCustomer: (data) => api.post('/customers', data),
  updateCustomer: (id, data) => api.put(`/customers/${id}`, data),
  activateCustomer: (id) => api.put(`/customers/${id}/activate`),
  deactivateCustomer: (id) => api.put(`/customers/${id}/deactivate`),
  deleteCustomer: (id) => api.delete(`/customers/${id}`),
};

// Warehouse API
export const warehouseAPI = {
  getWarehouses: () => api.get('/warehouses'),
  getActiveWarehouses: () => api.get('/warehouses/active'),
  getWarehouse: (id) => api.get(`/warehouses/${id}`),
  createWarehouse: (data) => api.post('/warehouses', data),
  updateWarehouse: (id, data) => api.put(`/warehouses/${id}`, data),
  activateWarehouse: (id) => api.put(`/warehouses/${id}/activate`),
  deactivateWarehouse: (id) => api.put(`/warehouses/${id}/deactivate`),
  transferWarehouse: (id, data) => api.post(`/warehouses/${id}/transfer`, data),
  deleteWarehouse: (id) => api.delete(`/warehouses/${id}`),
};

// Vehicle Inventory API (Authenticated)
export const inventoryAPI = {
  getInventory: () => api.get('/vehicle-inventory'),
  getAllInventory: () => api.get('/vehicle-inventory/all'), // New endpoint for all statuses
  getInventoryById: (id) => api.get(`/vehicle-inventory/${id}`),
  getAvailableInventory: () => api.get('/vehicle-inventory/available'),
  getInventoryByBrand: (brandId) => api.get(`/vehicle-inventory/brand/${brandId}`),
  getInventoryByModel: (modelId) => api.get(`/vehicle-inventory/model/${modelId}`),
  getInventoryByVariant: (variantId) => api.get(`/vehicle-inventory/variant/${variantId}`),
  getInventoryByColor: (colorId) => api.get(`/vehicle-inventory/color/${colorId}`),
  getInventoryByPriceRange: (minPrice, maxPrice) => 
    api.get(`/vehicle-inventory/price-range?minPrice=${minPrice}&maxPrice=${maxPrice}`),
  createInventory: (data) => api.post('/vehicle-inventory', data),
  updateInventory: (id, data) => api.put(`/vehicle-inventory/${id}`, data),
  deleteInventory: (id) => api.delete(`/vehicle-inventory/${id}`),
};

// Quotation API
export const quotationAPI = {
  getQuotations: () => api.get('/quotations'),
  getQuotation: (id) => api.get(`/quotations/${id}`),
  getQuotationsByCustomer: (customerId) => api.get(`/quotations/customer/${customerId}`),
  getQuotationsByStatus: (status) => api.get(`/quotations/status/${status}`),
  createQuotation: (data) => api.post('/quotations', data),
  updateQuotation: (id, data) => api.put(`/quotations/${id}`, data),
  updateQuotationStatus: (id, status) => api.put(`/quotations/${id}/status?status=${status}`),
  sendQuotation: (id) => api.post(`/quotations/${id}/send`),
  convertToOrder: (id) => api.post(`/quotations/${id}/convert-to-order`),
  exportQuotationPDF: (id) => api.get(`/quotations/${id}/pdf`),
  deleteQuotation: (id) => api.delete(`/quotations/${id}`),
};

// Order API
export const orderAPI = {
  getOrders: () => api.get('/orders'),
  getOrder: (id) => api.get(`/orders/${id}`),
  getOrdersByCustomer: (customerId) => api.get(`/orders/customer/${customerId}`),
  getOrdersByStatus: (status) => api.get(`/orders/status/${status}`),
  createOrder: (data) => api.post('/orders', data),
  updateOrder: (id, data) => api.put(`/orders/${id}`, data),
  updateOrderStatus: (id, status) => api.put(`/orders/${id}/status?status=${status}`),
  convertToContract: (id) => api.post(`/orders/${id}/convert-to-contract`),
  cancelOrder: (id) => api.post(`/orders/${id}/cancel`),
  exportOrderPDF: (id) => api.get(`/orders/${id}/pdf`),
  deleteOrder: (id) => api.delete(`/orders/${id}`),
};

// Sales Contract API
export const contractAPI = {
  getContracts: () => api.get('/sales-contracts'),
  getContract: (id) => api.get(`/sales-contracts/${id}`),
  getContractsByOrder: (orderId) => api.get(`/sales-contracts/order/${orderId}`),
  getContractsByCustomer: (customerId) => api.get(`/sales-contracts/customer/${customerId}`),
  createContract: (data) => api.post('/sales-contracts', data),
  updateContract: (id, data) => api.put(`/sales-contracts/${id}`, data),
  updateContractStatus: (id, status) => api.put(`/sales-contracts/${id}/status?status=${status}`),
  signContract: (id, signedDate) => api.put(`/sales-contracts/${id}/sign?signedDate=${signedDate}`),
  sendContract: (id) => api.post(`/sales-contracts/${id}/send`),
  exportContractPDF: (id) => api.get(`/sales-contracts/${id}/pdf`),
  terminateContract: (id) => api.post(`/sales-contracts/${id}/terminate`),
  deleteContract: (id) => api.delete(`/sales-contracts/${id}`),
};

// Vehicle Delivery API
export const deliveryAPI = {
  getDeliveries: () => api.get('/vehicle-deliveries'),
  getDelivery: (id) => api.get(`/vehicle-deliveries/${id}`),
  getDeliveriesByOrder: (orderId) => api.get(`/vehicle-deliveries/order/${orderId}`),
  getDeliveriesByCustomer: (customerId) => api.get(`/vehicle-deliveries/customer/${customerId}`),
  createDelivery: (data) => api.post('/vehicle-deliveries', data),
  updateDelivery: (id, data) => api.put(`/vehicle-deliveries/${id}`, data),
  updateDeliveryStatus: (id, status) => api.put(`/vehicle-deliveries/${id}/status?status=${status}`),
  scheduleDelivery: (id) => api.post(`/vehicle-deliveries/${id}/schedule`),
  completeDelivery: (id) => api.post(`/vehicle-deliveries/${id}/complete`),
  getDeliveryTracking: (id) => api.get(`/vehicle-deliveries/${id}/tracking`),
  deleteDelivery: (id) => api.delete(`/vehicle-deliveries/${id}`),
};

// Payment API
export const paymentAPI = {
  getPayments: () => api.get('/customer-payments'),
  getPayment: (id) => api.get(`/customer-payments/${id}`),
  getPaymentsByOrder: (orderId) => api.get(`/customer-payments/order/${orderId}`),
  getPaymentsByCustomer: (customerId) => api.get(`/customer-payments/customer/${customerId}`),
  createPayment: (data) => api.post('/customer-payments', data),
  updatePayment: (id, data) => api.put(`/customer-payments/${id}`, data),
  updatePaymentStatus: (id, status) => api.put(`/customer-payments/${id}/status?status=${status}`),
  processPayment: (id) => api.post(`/customer-payments/${id}/process`),
  refundPayment: (id) => api.post(`/customer-payments/${id}/refund`),
  exportPaymentReceipt: (id) => api.get(`/customer-payments/${id}/receipt`),
  deletePayment: (id) => api.delete(`/customer-payments/${id}`),
};

// User API
export const userAPI = {
  getUsers: () => api.get('/users'),
  getActiveUsers: () => api.get('/users/active'),
  getUser: (id) => api.get(`/users/${id}`),
  getUserByUsername: (username) => api.get(`/users/username/${username}`),
  getUserByEmail: (email) => api.get(`/users/email/${email}`),
  getUsersByRole: (roleName) => api.get(`/users/role/${roleName}`),
  getUsersByDealer: (dealerId) => api.get(`/users/dealer/${dealerId}`),
  getUsersByRoleString: (roleString) => api.get(`/users/role-string/${roleString}`),
  getDealerStaff: () => api.get('/users/dealer-staff'),
  getDealerManagers: () => api.get('/users/dealer-managers'),
  getEvmStaff: () => api.get('/users/evm-staff'),
  getAdmins: () => api.get('/users/admins'),
  searchUsers: (name) => api.get(`/users/search?name=${name}`),
  createUser: (data) => api.post('/users/dto', data),
  updateUser: (id, data) => api.put(`/users/${id}`, data),
  activateUser: (id) => api.put(`/users/${id}/activate`),
  deactivateUser: (id) => api.put(`/users/${id}/deactivate`),
  deleteUser: (id) => api.delete(`/users/${id}`),
  resetPassword: (id, newPassword) => api.post(`/users/${id}/reset-password?newPassword=${newPassword}`),
  changePassword: (id, oldPassword, newPassword) => api.put(`/users/${id}/change-password`, { oldPassword, newPassword }),
  // Role management
  getRoles: () => api.get('/users/roles'),
  getRole: (id) => api.get(`/users/roles/${id}`),
  getRoleByName: (roleName) => api.get(`/users/roles/name/${roleName}`),
  createRole: (data) => api.post('/users/roles', data),
  updateRole: (id, data) => api.put(`/users/roles/${id}`, data),
  deleteRole: (id) => api.delete(`/users/roles/${id}`),
  // Bulk operations
  bulkResetPassword: (userIds) => api.post('/users/bulk-reset-password', { userIds }),
};

// Report API
export const reportAPI = {
  getSalesSummary: (startDate, endDate) => 
    api.get(`/reports/sales-summary?startDate=${startDate}&endDate=${endDate}`),
  getInventoryTurnover: () => api.get('/reports/inventory-turnover'),
  getCustomerDebt: () => api.get('/reports/customer-debt'),
  getMonthlySales: () => api.get('/reports/monthly-sales'),
  getDealerPerformance: () => api.get('/reports/dealer-performance'),
};

// Appointment API
export const appointmentAPI = {
  getAppointments: () => api.get('/appointments'),
  getAppointment: (id) => api.get(`/appointments/${id}`),
  getAppointmentsByCustomer: (customerId) => api.get(`/appointments/customer/${customerId}`),
  getAppointmentsByStaff: (staffId) => api.get(`/appointments/staff/${staffId}`),
  getAppointmentsByStatus: (status) => api.get(`/appointments/status/${status}`),
  getAppointmentsByType: (appointmentType) => api.get(`/appointments/type/${appointmentType}`),
  getAppointmentsByVariant: (variantId) => api.get(`/appointments/variant/${variantId}`),
  getTestDriveAppointments: () => api.get('/appointments/test-drives'),
  getUpcomingAppointments: () => api.get('/appointments/upcoming'),
  searchAppointments: (title) => api.get(`/appointments/search?title=${title}`),
  getAppointmentsByDateRange: (startDate, endDate) => 
    api.get(`/appointments/date-range?startDate=${startDate}&endDate=${endDate}`),
  createAppointment: (data) => api.post('/appointments', data),
  updateAppointment: (id, data) => api.put(`/appointments/${id}`, data),
  updateAppointmentStatus: (id, status) => api.put(`/appointments/${id}/status?status=${status}`),
  deleteAppointment: (id) => api.delete(`/appointments/${id}`),
};

// Pricing Policy API
export const pricingPolicyAPI = {
  getPricingPolicies: () => api.get('/pricing-policies'),
  getPricingPolicy: (id) => api.get(`/pricing-policies/${id}`),
  getPricingPoliciesByVariant: (variantId) => api.get(`/pricing-policies/variant/${variantId}`),
  getPricingPoliciesByStatus: (status) => api.get(`/pricing-policies/status/${status}`),
  getPricingPoliciesByType: (policyType) => api.get(`/pricing-policies/type/${policyType}`),
  getPricingPoliciesByCustomerType: (customerType) => api.get(`/pricing-policies/customer-type/${customerType}`),
  getPricingPoliciesByRegion: (region) => api.get(`/pricing-policies/region/${region}`),
  getPricingPoliciesByDealer: (dealerId) => api.get(`/pricing-policies/dealer/${dealerId}`),
  getPricingPoliciesByScope: (scope) => api.get(`/pricing-policies/scope/${scope}`),
  getGlobalPricingPolicies: () => api.get('/pricing-policies/global'),
  getDealerSpecificPricingPolicies: () => api.get('/pricing-policies/dealer-specific'),
  getActivePricingPolicies: () => api.get('/pricing-policies/active'),
  getActivePricingPoliciesByDate: (date) => api.get(`/pricing-policies/active/date/${date}`),
  getActivePricingPoliciesByVariant: (variantId) => api.get(`/pricing-policies/active/variant/${variantId}`),
  getActivePricingPoliciesByVariantAndDate: (variantId, date) => 
    api.get(`/pricing-policies/active/variant/${variantId}/date/${date}`),
  getActivePricingPoliciesByVariantAndCustomerType: (variantId, customerType) => 
    api.get(`/pricing-policies/active/variant/${variantId}/customer-type/${customerType}`),
  searchPricingPolicies: (name) => api.get(`/pricing-policies/search?name=${name}`),
  createPricingPolicy: (data) => api.post('/pricing-policies', data),
  updatePricingPolicy: (id, data) => api.put(`/pricing-policies/${id}`, data),
  updatePricingPolicyStatus: (id, status) => api.put(`/pricing-policies/${id}/status?status=${status}`),
  deletePricingPolicy: (id) => api.delete(`/pricing-policies/${id}`),
};

// Installment Plan API
export const installmentPlanAPI = {
  getInstallmentPlans: () => api.get('/installment-plans'),
  getInstallmentPlan: (planId) => api.get(`/installment-plans/${planId}`),
  getInstallmentPlansByContract: (contractNumber) => api.get(`/installment-plans/contract/${contractNumber}`),
  getInstallmentPlansByStatus: (status) => api.get(`/installment-plans/status/${status}`),
  getInstallmentPlansByCustomer: (customerId) => api.get(`/installment-plans/customer/${customerId}`),
  getInstallmentPlansByOrder: (orderId) => api.get(`/installment-plans/order/${orderId}`),
  getInstallmentPlansByInvoice: (invoiceId) => api.get(`/installment-plans/invoice/${invoiceId}`),
  getInstallmentPlansByDealer: (dealerId) => api.get(`/installment-plans/dealer/${dealerId}`),
  getInstallmentPlansByPlanType: (planType) => api.get(`/installment-plans/plan-type/${planType}`),
  getCustomerInstallmentPlans: () => api.get('/installment-plans/customer-plans'),
  getDealerInstallmentPlans: () => api.get('/installment-plans/dealer-plans'),
  getInstallmentPlansByFinanceCompany: (financeCompany) => 
    api.get(`/installment-plans/finance-company/${financeCompany}`),
  createInstallmentPlan: (data) => api.post('/installment-plans', data),
  updateInstallmentPlan: (planId, data) => api.put(`/installment-plans/${planId}`, data),
  updateInstallmentPlanStatus: (planId, status) => api.put(`/installment-plans/${planId}/status?status=${status}`),
  deleteInstallmentPlan: (planId) => api.delete(`/installment-plans/${planId}`),
};

// Dealer Target API
export const dealerTargetAPI = {
  getDealerTargets: () => api.get('/dealer-targets'),
  getDealerTarget: (targetId) => api.get(`/dealer-targets/${targetId}`),
  getDealerTargetsByYear: (targetYear) => api.get(`/dealer-targets/year/${targetYear}`),
  getDealerTargetsByMonth: (targetMonth) => api.get(`/dealer-targets/month/${targetMonth}`),
  getDealerTargetsByType: (targetType) => api.get(`/dealer-targets/type/${targetType}`),
  getDealerTargetsByStatus: (targetStatus) => api.get(`/dealer-targets/status/${targetStatus}`),
  getDealerTargetsByDealer: (dealerId) => api.get(`/dealer-targets/dealer/${dealerId}`),
  getDealerTargetsByScope: (targetScope) => api.get(`/dealer-targets/scope/${targetScope}`),
  getDealerSpecificTargets: () => api.get('/dealer-targets/dealer-specific'),
  getDealerTargetsByYearAndMonth: (targetYear, targetMonth) => 
    api.get(`/dealer-targets/year/${targetYear}/month/${targetMonth}`),
  getDealerTargetsByYearAndType: (targetYear, targetType) => 
    api.get(`/dealer-targets/year/${targetYear}/type/${targetType}`),
  getDealerTargetsByDealerAndYear: (dealerId, targetYear) => 
    api.get(`/dealer-targets/dealer/${dealerId}/year/${targetYear}`),
  getDealerTargetsByAchievementRateMin: (minRate) => 
    api.get(`/dealer-targets/achievement-rate/min/${minRate}`),
  getDealerTargetsByAchievementRateMax: (maxRate) => 
    api.get(`/dealer-targets/achievement-rate/max/${maxRate}`),
  createDealerTarget: (data) => api.post('/dealer-targets', data),
  updateDealerTarget: (targetId, data) => api.put(`/dealer-targets/${targetId}`, data),
  updateDealerTargetStatus: (targetId, status) => api.put(`/dealer-targets/${targetId}/status?status=${status}`),
  updateDealerTargetAchievement: (targetId, achievement) => 
    api.put(`/dealer-targets/${targetId}/achievement`, { achievement }),
  deleteDealerTarget: (targetId) => api.delete(`/dealer-targets/${targetId}`),
};

// Dealer API
export const dealerAPI = {
  getDealers: () => api.get('/dealers'),
  getDealer: (id) => api.get(`/dealers/${id}`),
  getActiveDealers: () => api.get('/dealers/active'),
  searchDealers: (name) => api.get(`/dealers/search?name=${name}`),
  createDealer: (data) => api.post('/dealers', data),
  updateDealer: (id, data) => api.put(`/dealers/${id}`, data),
  activateDealer: (id) => api.put(`/dealers/${id}/activate`),
  deactivateDealer: (id) => api.put(`/dealers/${id}/deactivate`),
  deleteDealer: (id) => api.delete(`/dealers/${id}`),
};

// Promotion API
export const promotionAPI = {
  getPromotions: () => api.get('/promotions'),
  getPromotion: (id) => api.get(`/promotions/${id}`),
  getActivePromotions: () => api.get('/promotions/active'),
  getPromotionsByType: (promotionType) => api.get(`/promotions/type/${promotionType}`),
  getPromotionsByStatus: (status) => api.get(`/promotions/status/${status}`),
  getPromotionsByDateRange: (startDate, endDate) => 
    api.get(`/promotions/date-range?startDate=${startDate}&endDate=${endDate}`),
  searchPromotions: (name) => api.get(`/promotions/search?name=${name}`),
  createPromotion: (data) => api.post('/promotions', data),
  updatePromotion: (id, data) => api.put(`/promotions/${id}`, data),
  updatePromotionStatus: (id, status) => api.put(`/promotions/${id}/status?status=${status}`),
  deletePromotion: (id) => api.delete(`/promotions/${id}`),
};

// Customer Feedback API
export const feedbackAPI = {
  getFeedbacks: () => api.get('/customer-feedbacks'),
  getFeedback: (id) => api.get(`/customer-feedbacks/${id}`),
  getFeedbacksByCustomer: (customerId) => api.get(`/customer-feedbacks/customer/${customerId}`),
  getFeedbacksByOrder: (orderId) => api.get(`/customer-feedbacks/order/${orderId}`),
  getFeedbacksByRating: (rating) => api.get(`/customer-feedbacks/rating/${rating}`),
  getFeedbacksByStatus: (status) => api.get(`/customer-feedbacks/status/${status}`),
  searchFeedbacks: (keyword) => api.get(`/customer-feedbacks/search?keyword=${keyword}`),
  createFeedback: (data) => api.post('/customer-feedbacks', data),
  updateFeedback: (id, data) => api.put(`/customer-feedbacks/${id}`, data),
  updateFeedbackStatus: (id, status) => api.put(`/customer-feedbacks/${id}/status?status=${status}`),
  deleteFeedback: (id) => api.delete(`/customer-feedbacks/${id}`),
};

// Public API services (no authentication required)
export const publicVehicleAPI = {
  // Vehicle Brands
  getBrands: () => publicApi.get('/vehicle-brands'),
  getBrandById: (id) => publicApi.get(`/vehicle-brands/${id}`),
  
  // Vehicle Models
  getModels: () => publicApi.get('/vehicle-models'),
  getModelById: (id) => publicApi.get(`/vehicle-models/${id}`),
  
  // Vehicle Variants
  getVariants: () => publicApi.get('/vehicle-variants'),
  getVariantById: (id) => publicApi.get(`/vehicle-variants/${id}`),
  
  // Vehicle Colors
  getColors: () => publicApi.get('/vehicle-colors'),
  getColorById: (id) => publicApi.get(`/vehicle-colors/${id}`),
};

export const publicInventoryAPI = {
  getInventory: () => publicApi.get('/vehicle-inventory'),
  getInventoryById: (id) => publicApi.get(`/vehicle-inventory/${id}`),
  getInventoryByBrand: (brandId) => publicApi.get(`/vehicle-inventory/brand/${brandId}`),
  getInventoryByModel: (modelId) => publicApi.get(`/vehicle-inventory/model/${modelId}`),
  getInventoryByVariant: (variantId) => publicApi.get(`/vehicle-inventory/variant/${variantId}`),
  getInventoryByColor: (colorId) => publicApi.get(`/vehicle-inventory/color/${colorId}`),
  getInventoryByPriceRange: (minPrice, maxPrice) => 
    publicApi.get(`/vehicle-inventory/price-range?minPrice=${minPrice}&maxPrice=${maxPrice}`),
  getAvailableInventory: () => publicApi.get('/vehicle-inventory/available'),
};

export const publicPromotionAPI = {
  getPromotions: () => publicApi.get('/promotions'),
  getPromotionById: (id) => publicApi.get(`/promotions/${id}`),
};

export const publicQuotationAPI = {
  getQuotations: () => publicApi.get('/quotations'),
  getQuotationById: (id) => publicApi.get(`/quotations/${id}`),
  createQuotation: (data) => publicApi.post('/quotations', data),
};

export const publicOrderAPI = {
  createOrder: (data) => publicApi.post('/orders', data),
  getOrderById: (id) => publicApi.get(`/orders/${id}`),
  getOrderByNumber: (orderNumber) => publicApi.get(`/orders/order-number/${orderNumber}`),
  cancelOrder: (id, reason) => publicApi.put(`/orders/${id}/cancel?reason=${encodeURIComponent(reason)}`),
  getOrderStatus: (id) => publicApi.get(`/orders/${id}/status`),
  trackOrder: (orderNumber) => publicApi.get(`/orders/track/${orderNumber}`),
};

export const publicPaymentAPI = {
  createDeposit: (data) => publicApi.post('/payments/deposit', data),
  createFullPayment: (data) => publicApi.post('/payments/full-payment', data),
  getPaymentStatus: (id) => publicApi.get(`/payments/${id}/status`),
  getPaymentsByOrder: (orderId) => publicApi.get(`/payments/order/${orderId}`),
  getPaymentMethods: () => publicApi.get('/payments/methods'),
};

export const publicAppointmentAPI = {
  createAppointment: (data) => publicApi.post('/appointments', data),
  getAppointmentById: (id) => publicApi.get(`/appointments/${id}`),
  updateAppointment: (id, data) => publicApi.put(`/appointments/${id}`, data),
  cancelAppointment: (id) => publicApi.delete(`/appointments/${id}`),
  getAvailableSlots: () => publicApi.get('/appointments/available-slots'),
  getAppointmentTypes: () => publicApi.get('/appointments/types'),
};

export const publicFeedbackAPI = {
  submitFeedback: (data) => publicApi.post('/feedbacks', data),
  getFeedbackById: (id) => publicApi.get(`/feedbacks/${id}`),
  getFeedbacksByVehicle: (inventoryId) => publicApi.get(`/feedbacks/vehicle/${inventoryId}`),
  getFeedbacksByRating: (rating) => publicApi.get(`/feedbacks/rating/${rating}`),
};

export const publicVehicleComparisonAPI = {
  quickCompare: (variantIds) => publicApi.get(`/vehicle-compare/quick?variantIds=${variantIds.join(',')}`),
  detailedCompare: (data) => publicApi.post('/vehicle-compare', data),
  getAvailableForCompare: () => publicApi.get('/vehicle-compare/available'),
  compareTwoVehicles: (id1, id2) => publicApi.post(`/vehicle-compare/${id1}/vs/${id2}`),
  getComparisonCriteria: () => publicApi.get('/vehicle-compare/criteria'),
};

export const publicContractAPI = {
  getContractTemplate: () => publicApi.get('/contracts/template'),
  generateContract: (data) => publicApi.post('/contracts/generate', data),
};

// Notification API
export const notificationAPI = {
  getNotifications: () => api.get('/notifications'),
  getNotification: (id) => api.get(`/notifications/${id}`),
  getUnreadNotifications: () => api.get('/notifications/unread'),
  markAsRead: (id) => api.put(`/notifications/${id}/read`),
  markAllAsRead: () => api.put('/notifications/read-all'),
  deleteNotification: (id) => api.delete(`/notifications/${id}`),
  createNotification: (data) => api.post('/notifications', data),
};

// Image Management APIs
export const imageAPI = {
  // READ APIs
  getImageList: (page = 0, size = 20, sortBy = 'uploadDate', sortDir = 'desc') => 
    api.get(`/images/list?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`),
  
  getImagesByCategory: (category, page = 0, size = 20) => 
    api.get(`/images/list/${category}?page=${page}&size=${size}`),
  
  getImageInfo: (category, filename) => 
    api.get(`/images/info/${category}/${filename}`),
  
  searchImages: (query, category = null, page = 0, size = 20) => {
    let url = `/images/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`;
    if (category) url += `&category=${category}`;
    return api.get(url);
  },
  
  getImageStats: () => api.get('/images/stats'),
  
  getCategoryStats: (category) => api.get(`/images/stats/${category}`),
  
  getImageConfig: () => api.get('/images/info'),
  
  // UPDATE APIs
  updateImage: (category, filename, data) => 
    api.put(`/images/update/${category}/${filename}`, data),
  
  renameImage: (category, oldFilename, newFilename) => 
    api.put(`/images/rename/${category}/${oldFilename}`, { newFilename }),
  
  moveImage: (oldCategory, filename, newCategory) => 
    api.put(`/images/move/${oldCategory}/${filename}`, { newCategory }),
  
  // DELETE APIs
  deleteImage: (category, filename) => 
    api.delete(`/images/delete/${category}/${filename}`),
  
  deleteCategory: (category) => 
    api.delete(`/images/delete-category/${category}`),
  
  // BULK OPERATIONS
  bulkDeleteImages: (imageIds) => 
    api.post('/images/bulk-delete', { imageIds }),
  
  bulkMoveImages: (imageIds, newCategory) => 
    api.post('/images/bulk-move', { imageIds, newCategory }),
  
  // UPLOAD APIs for Vehicle Creation
  uploadVehicleBrandImage: (formData) => 
    api.post('/images/upload-vehicle-brand', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
  
  uploadModelImage: (formData) => 
    api.post('/images/upload-model-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
  
  uploadVariantImage: (formData) => 
    api.post('/images/upload-variant-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
  
  uploadInventoryImages: (formData) => 
    api.post('/images/upload-inventory-images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
  
  // UPLOAD
  uploadImage: (formData, config) => 
    api.post('/images/upload', formData, config),
};

export default api;
