import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { QueryClient, QueryClientProvider } from 'react-query';

import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import Layout from './components/layout/Layout';
import Login from './components/auth/Login';
import Dashboard from './pages/Dashboard';
import VehicleManagement from './pages/VehicleManagement';
import CustomerManagement from './pages/CustomerManagement';
import QuotationManagement from './pages/QuotationManagement';
import OrderManagement from './pages/OrderManagement';
import ContractManagement from './pages/ContractManagement';
import DeliveryManagement from './pages/DeliveryManagement';
import PaymentManagement from './pages/PaymentManagement';
import InventoryManagement from './pages/InventoryManagement';
import ReportManagement from './pages/ReportManagement';
import UserManagement from './pages/UserManagement';
import PromotionManagement from './pages/PromotionManagement';
import AppointmentManagement from './pages/AppointmentManagement';
import FeedbackManagement from './pages/FeedbackManagement';
import DealerManagement from './pages/DealerManagement';
import PricingManagement from './pages/PricingManagement';
import InstallmentPlanManagement from './pages/InstallmentPlanManagement';
import DealerTargetManagement from './pages/DealerTargetManagement';
import VehicleComparison from './pages/VehicleComparison';
import SalesPage from './pages/SalesPage';
import PublicSalesPage from './pages/PublicSalesPage';
import VehicleImageManagement from './pages/VehicleImageManagement';
import Profile from './pages/Profile';
import Settings from './pages/Settings';
import RoleManagement from './pages/RoleManagement';
import ApiAudit from './pages/ApiAudit';
import Unauthorized from './pages/Unauthorized';

import './App.css';

// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <Router>
          <div className="App">
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<PublicSalesPage />} />
              <Route path="/login" element={<Login />} />
              <Route path="/unauthorized" element={<Unauthorized />} />
              
              {/* Protected Routes */}
              <Route path="/admin" element={
                <ProtectedRoute>
                  <Layout />
                </ProtectedRoute>
              }>
                <Route index element={<Navigate to="/admin/dashboard" replace />} />
                <Route path="dashboard" element={<Dashboard />} />
                
                {/* Vehicle Management */}
                <Route path="vehicles" element={<VehicleManagement />} />
                <Route path="vehicles/brands" element={<VehicleManagement />} />
                <Route path="vehicles/models" element={<VehicleManagement />} />
                <Route path="vehicles/variants" element={<VehicleManagement />} />
                <Route path="vehicles/colors" element={<VehicleManagement />} />
                <Route path="vehicle-comparison" element={<VehicleComparison />} />
                
                {/* Customer Management */}
                <Route path="customers" element={<CustomerManagement />} />
                
                {/* Sales Process */}
                <Route path="sales" element={<SalesPage />} />
                <Route path="quotations" element={<QuotationManagement />} />
                <Route path="orders" element={<OrderManagement />} />
                <Route path="contracts" element={<ContractManagement />} />
                <Route path="deliveries" element={<DeliveryManagement />} />
                <Route path="payments" element={<PaymentManagement />} />
                
                {/* Additional Features */}
                <Route path="promotions" element={<PromotionManagement />} />
                <Route path="appointments" element={<AppointmentManagement />} />
                <Route path="feedbacks" element={<FeedbackManagement />} />
                
                {/* Inventory Management */}
                <Route path="inventory" element={<InventoryManagement />} />
                <Route path="inventory/warehouses" element={<InventoryManagement />} />
                <Route path="inventory/vehicles" element={<InventoryManagement />} />
                
                {/* Reports */}
                <Route path="reports" element={
                  <ProtectedRoute requiredRoles={['admin', 'evm_staff', 'dealer_manager']}>
                    <ReportManagement />
                  </ProtectedRoute>
                } />
                
                {/* Dealer & Pricing Management */}
                <Route path="dealers" element={
                  <ProtectedRoute requiredRoles={['admin', 'evm_staff']}>
                    <DealerManagement />
                  </ProtectedRoute>
                } />
                <Route path="pricing" element={
                  <ProtectedRoute requiredRoles={['admin', 'evm_staff']}>
                    <PricingManagement />
                  </ProtectedRoute>
                } />
                <Route path="installment-plans" element={
                  <ProtectedRoute requiredRoles={['admin', 'evm_staff', 'dealer_manager']}>
                    <InstallmentPlanManagement />
                  </ProtectedRoute>
                } />
                <Route path="dealer-targets" element={
                  <ProtectedRoute requiredRoles={['admin', 'evm_staff']}>
                    <DealerTargetManagement />
                  </ProtectedRoute>
                } />
                
                {/* User Management */}
                <Route path="users" element={
                  <ProtectedRoute requiredRoles={['admin', 'evm_staff']}>
                    <UserManagement />
                  </ProtectedRoute>
                } />
                
                {/* Profile & Settings */}
                <Route path="profile" element={<Profile />} />
                <Route path="settings" element={<Settings />} />
                
                {/* Role Management */}
                <Route path="roles" element={
                  <ProtectedRoute requiredRoles={['admin']}>
                    <RoleManagement />
                  </ProtectedRoute>
                } />
                
                {/* Vehicle Image Management */}
                <Route path="vehicle-images" element={
                  <ProtectedRoute requiredRoles={['admin', 'evm_staff']}>
                    <VehicleImageManagement />
                  </ProtectedRoute>
                } />
                
                {process.env.NODE_ENV !== 'production' && (
                  <Route path="dev/api-audit" element={<ApiAudit />} />
                )}
              </Route>
              
              {/* Catch all route */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
            
            <Toaster
              position="top-right"
              toastOptions={{
                duration: 4000,
                style: {
                  background: '#363636',
                  color: '#fff',
                },
                success: {
                  duration: 3000,
                  iconTheme: {
                    primary: '#10b981',
                    secondary: '#fff',
                  },
                },
                error: {
                  duration: 5000,
                  iconTheme: {
                    primary: '#ef4444',
                    secondary: '#fff',
                  },
                },
              }}
            />
          </div>
        </Router>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
