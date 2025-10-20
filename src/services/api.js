import axios from 'axios';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

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

// Vehicle API
export const vehicleAPI = {
  // Brands
  getBrands: () => api.get('/vehicles/brands'),
  getActiveBrands: () => api.get('/vehicles/brands/active'),
  getBrand: (id) => api.get(`/vehicles/brands/${id}`),
  createBrand: (data) => api.post('/vehicles/brands', data),
  updateBrand: (id, data) => api.put(`/vehicles/brands/${id}`, data),
  activateBrand: (id) => api.put(`/vehicles/brands/${id}/activate`),
  deactivateBrand: (id) => api.put(`/vehicles/brands/${id}/deactivate`),
  deleteBrand: (id) => api.delete(`/vehicles/brands/${id}`),

  // Models
  getModels: () => api.get('/vehicles/models'),
  getModelsByBrand: (brandId) => api.get(`/vehicles/models/brand/${brandId}`),
  getModel: (id) => api.get(`/vehicles/models/${id}`),
  createModel: (data) => api.post('/vehicles/models', data),
  updateModel: (id, data) => api.put(`/vehicles/models/${id}`, data),
  activateModel: (id) => api.put(`/vehicles/models/${id}/activate`),
  deactivateModel: (id) => api.put(`/vehicles/models/${id}/deactivate`),
  deleteModel: (id) => api.delete(`/vehicles/models/${id}`),

  // Variants
  getVariants: () => api.get('/vehicles/variants'),
  getVariantsByModel: (modelId) => api.get(`/vehicles/variants/model/${modelId}`),
  getVariant: (id) => api.get(`/vehicles/variants/${id}`),
  createVariant: (data) => api.post('/vehicles/variants', data),
  updateVariant: (id, data) => api.put(`/vehicles/variants/${id}`, data),
  activateVariant: (id) => api.put(`/vehicles/variants/${id}/activate`),
  deactivateVariant: (id) => api.put(`/vehicles/variants/${id}/deactivate`),
  deleteVariant: (id) => api.delete(`/vehicles/variants/${id}`),

  // Colors
  getColors: () => api.get('/vehicles/colors'),
  getColor: (id) => api.get(`/vehicles/colors/${id}`),
  createColor: (data) => api.post('/vehicles/colors', data),
  updateColor: (id, data) => api.put(`/vehicles/colors/${id}`, data),
  activateColor: (id) => api.put(`/vehicles/colors/${id}/activate`),
  deactivateColor: (id) => api.put(`/vehicles/colors/${id}/deactivate`),
  deleteColor: (id) => api.delete(`/vehicles/colors/${id}`),
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
  getWarehouseInventory: (id) => api.get(`/warehouses/${id}/inventory`),
  transferWarehouse: (id, data) => api.post(`/warehouses/${id}/transfer`, data),
  deleteWarehouse: (id) => api.delete(`/warehouses/${id}`),
};

// Vehicle Inventory API
export const inventoryAPI = {
  getInventory: () => api.get('/vehicle-inventory'),
  getInventoryByStatus: (status) => api.get(`/vehicle-inventory/status/${status}`),
  getInventoryByVariant: (variantId) => api.get(`/vehicle-inventory/variant/${variantId}`),
  getInventoryByWarehouse: (warehouseId) => api.get(`/vehicle-inventory/warehouse/${warehouseId}`),
  getInventoryItem: (id) => api.get(`/vehicle-inventory/${id}`),
  createInventory: (data) => api.post('/vehicle-inventory', data),
  updateInventory: (id, data) => api.put(`/vehicle-inventory/${id}`, data),
  updateInventoryStatus: (id, status) => api.put(`/vehicle-inventory/${id}/status?status=${status}`),
  reserveVehicle: (id) => api.post(`/vehicle-inventory/${id}/reserve`),
  releaseVehicle: (id) => api.post(`/vehicle-inventory/${id}/release`),
  getVehicleHistory: (id) => api.get(`/vehicle-inventory/${id}/history`),
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
  getUsersByRole: (roleName) => api.get(`/users/role/${roleName}`),
  searchUsers: (name) => api.get(`/users/search?name=${name}`),
  createUser: (data) => api.post('/users', data),
  updateUser: (id, data) => api.put(`/users/${id}`, data),
  activateUser: (id) => api.put(`/users/${id}/activate`),
  deactivateUser: (id) => api.put(`/users/${id}/deactivate`),
  deleteUser: (id) => api.delete(`/users/${id}`),
  resetPassword: (id, newPassword) => api.post(`/users/${id}/reset-password?newPassword=${newPassword}`),
  changePassword: (id, oldPassword, newPassword) => api.put(`/users/${id}/change-password`, { oldPassword, newPassword }),
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

export default api;
