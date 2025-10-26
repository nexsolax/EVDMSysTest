import React, { useState, useEffect } from 'react';
import { ShoppingCart, Plus, DollarSign, Calendar, Package, User } from 'lucide-react';
import { orderAPI } from '../services/api';
import { getStatusBadge as getStatusBadgeUtil } from '../utils/statusBadges';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import OrderModal from '../components/modals/OrderModal';
import toast from 'react-hot-toast';
import './OrderManagement.css';

const OrderManagement = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Modal states
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showOrderModal, setShowOrderModal] = useState(false);
  const [modalMode, setModalMode] = useState('view'); // 'view' or 'edit'

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const response = await orderAPI.getOrders();
      setOrders(response.data || []);
    } catch (error) {
      console.error('Error loading orders:', error);
      toast.error('Không thể tải danh sách đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (order) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa đơn hàng "${order.orderNumber}"?`)) {
      try {
        await orderAPI.deleteOrder(order.orderId);
        toast.success('Xóa đơn hàng thành công');
        loadOrders();
      } catch (error) {
        console.error('Error deleting order:', error);
        toast.error('Không thể xóa đơn hàng');
      }
    }
  };

  const handleEdit = async (order) => {
    try {
      setSelectedOrder(order);
      setModalMode('edit');
      setShowOrderModal(true);
    } catch (error) {
      console.error('Error getting order details:', error);
      toast.error('Không thể tải thông tin đơn hàng');
    }
  };

  const handleView = async (order) => {
    try {
      setSelectedOrder(order);
      setModalMode('view');
      setShowOrderModal(true);
    } catch (error) {
      console.error('Error getting order details:', error);
      toast.error('Không thể tải thông tin đơn hàng');
    }
  };


  const handleConvertToContract = async (order) => {
    if (window.confirm(`Bạn có chắc chắn muốn chuyển đổi đơn hàng "${order.orderNumber}" thành hợp đồng?`)) {
      try {
        await orderAPI.convertToContract(order.orderId);
        toast.success('Chuyển đổi thành hợp đồng thành công');
        loadOrders();
      } catch (error) {
        console.error('Error converting order to contract:', error);
        toast.error('Không thể chuyển đổi thành hợp đồng');
      }
    }
  };

  const handleCancelOrder = async (order) => {
    if (window.confirm(`Bạn có chắc chắn muốn hủy đơn hàng "${order.orderNumber}"?`)) {
      try {
        await orderAPI.cancelOrder(order.orderId);
        toast.success('Hủy đơn hàng thành công');
        loadOrders();
      } catch (error) {
        console.error('Error cancelling order:', error);
        toast.error('Không thể hủy đơn hàng');
      }
    }
  };

  const handleExportPDF = async (order) => {
    try {
      const response = await orderAPI.exportOrderPDF(order.orderId);
      // Create blob and download
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `order-${order.orderNumber}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success('Xuất PDF thành công');
    } catch (error) {
      console.error('Error exporting PDF:', error);
      toast.error('Không thể xuất PDF');
    }
  };

  const getStatusBadge = (status) => {
    const statusInfo = getStatusBadgeUtil('order', status);
    return <span className={`badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const filteredOrders = orders.filter(order => {
    const searchLower = searchTerm.toLowerCase();
    return (
      order.orderNumber?.toLowerCase().includes(searchLower) ||
      order.customer?.firstName?.toLowerCase().includes(searchLower) ||
      order.customer?.lastName?.toLowerCase().includes(searchLower) ||
      order.customer?.email?.toLowerCase().includes(searchLower) ||
      order.status?.toLowerCase().includes(searchLower)
    );
  });

  const columns = [
    { 
      key: 'orderNumber', 
      header: 'Số đơn hàng',
      render: (order) => (
        <div className="order-number">
          <ShoppingCart size={16} />
          {order.orderNumber}
        </div>
      )
    },
    { 
      key: 'customer', 
      header: 'Khách hàng',
      render: (order) => (
        <div className="customer-info">
          <User size={16} />
          <div>
            <div className="customer-name">
              {order.customer?.firstName} {order.customer?.lastName}
            </div>
            <div className="customer-email">{order.customer?.email}</div>
          </div>
        </div>
      )
    },
    { 
      key: 'vehicle', 
      header: 'Xe đặt mua',
      render: (order) => (
        <div className="vehicle-info">
          <Package size={16} />
          <div>
            <div className="vehicle-name">
              {order.vehicle?.variant?.model?.brand?.name} {order.vehicle?.variant?.model?.name}
            </div>
            <div className="vehicle-variant">{order.vehicle?.variant?.name}</div>
          </div>
        </div>
      )
    },
    { 
      key: 'totalAmount', 
      header: 'Tổng tiền',
      render: (order) => (
        <div className="amount">
          <DollarSign size={16} />
          {order.totalAmount ? `${order.totalAmount.toLocaleString('vi-VN')} VNĐ` : 'N/A'}
        </div>
      )
    },
    { 
      key: 'status', 
      header: 'Trạng thái',
      render: (order) => getStatusBadge(order.status)
    },
    { 
      key: 'orderDate', 
      header: 'Ngày đặt hàng',
      render: (order) => (
        <div className="date">
          <Calendar size={16} />
          {order.orderDate ? new Date(order.orderDate).toLocaleDateString('vi-VN') : 'N/A'}
        </div>
      )
    }
  ];

  const handleSaveOrder = async (orderId, orderData) => {
    try {
      await orderAPI.updateOrder(orderId, orderData);
      toast.success('Cập nhật đơn hàng thành công');
      loadOrders();
    } catch (error) {
      console.error('Error updating order:', error);
      toast.error('Không thể cập nhật đơn hàng');
      throw error;
    }
  };

  if (loading) {
    return <LoadingSpinner text="Đang tải danh sách đơn hàng..." />;
  }

  return (
    <div className="order-management">
      <div className="page-header">
        <div className="page-title">
          <ShoppingCart className="title-icon" />
          <h1>Quản lý đơn hàng</h1>
        </div>
        <p>Quản lý đơn hàng và trạng thái xử lý</p>
      </div>

      <div className="content">
        <div className="section-header">
          <h2>Danh sách đơn hàng</h2>
          <button className="btn btn-primary">
            <Plus size={20} />
            Tạo đơn hàng mới
          </button>
        </div>

        <DataTable
          data={filteredOrders}
          columns={columns}
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          searchPlaceholder="Tìm kiếm đơn hàng..."
          emptyMessage="Không có đơn hàng nào"
          onEdit={handleEdit}
          onDelete={handleDelete}
          onView={handleView}
          onConvertToContract={handleConvertToContract}
          onCancelOrder={handleCancelOrder}
          onExportPDF={handleExportPDF}
        />

        {/* Order Modal */}
        <OrderModal
          order={selectedOrder}
          isOpen={showOrderModal}
          mode={modalMode}
          onClose={() => {
            setShowOrderModal(false);
            setSelectedOrder(null);
            setModalMode('view');
          }}
          onSave={handleSaveOrder}
        />
      </div>
    </div>
  );
};

export default OrderManagement;
