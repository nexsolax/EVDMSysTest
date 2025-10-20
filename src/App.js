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
import Profile from './pages/Profile';
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
              <Route path="/login" element={<Login />} />
              <Route path="/unauthorized" element={<Unauthorized />} />
              
              {/* Protected Routes */}
              <Route path="/" element={
                <ProtectedRoute>
                  <Layout />
                </ProtectedRoute>
              }>
                <Route index element={<Navigate to="/dashboard" replace />} />
                <Route path="dashboard" element={<Dashboard />} />
                
                {/* Vehicle Management */}
                <Route path="vehicles" element={<VehicleManagement />} />
                <Route path="vehicles/brands" element={<VehicleManagement />} />
                <Route path="vehicles/models" element={<VehicleManagement />} />
                <Route path="vehicles/variants" element={<VehicleManagement />} />
                <Route path="vehicles/colors" element={<VehicleManagement />} />
                
                {/* Customer Management */}
                <Route path="customers" element={<CustomerManagement />} />
                
                {/* Sales Process */}
                <Route path="quotations" element={<QuotationManagement />} />
                <Route path="orders" element={<OrderManagement />} />
                <Route path="contracts" element={<ContractManagement />} />
                <Route path="deliveries" element={<DeliveryManagement />} />
                <Route path="payments" element={<PaymentManagement />} />
                
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
                
                {/* User Management */}
                <Route path="users" element={
                  <ProtectedRoute requiredRoles={['admin', 'evm_staff']}>
                    <UserManagement />
                  </ProtectedRoute>
                } />
                
                {/* Profile */}
                <Route path="profile" element={<Profile />} />
                {process.env.NODE_ENV !== 'production' && (
                  <Route path="dev/api-audit" element={<ApiAudit />} />
                )}
              </Route>
              
              {/* Catch all route */}
              <Route path="*" element={<Navigate to="/dashboard" replace />} />
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
