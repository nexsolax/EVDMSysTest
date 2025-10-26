import React, { useState, useEffect } from 'react';
import { Truck, Plus, Calendar, Package, MapPin, User } from 'lucide-react';
import { deliveryAPI } from '../services/api';
import { getStatusBadge as getStatusBadgeUtil } from '../utils/statusBadges';
import DataTable from '../components/common/DataTable';
import DeliveryModal from '../components/modals/DeliveryModal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import './DeliveryManagement.css';

const DeliveryManagement = () => {
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  // Modal states
  const [selectedDelivery, setSelectedDelivery] = useState(null);
  const [showDeliveryModal, setShowDeliveryModal] = useState(false);
  const [modalMode, setModalMode] = useState('view');

  useEffect(() => {
    loadDeliveries();
  }, []);

  const loadDeliveries = async () => {
    try {
      setLoading(true);
      const response = await deliveryAPI.getDeliveries();
      setDeliveries(response.data || []);
    } catch (error) {
      console.error('Error loading deliveries:', error);
      toast.error('Không thể tải danh sách giao xe');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (delivery) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa giao xe "${delivery.deliveryNumber}"?`)) {
      try {
        await deliveryAPI.deleteDelivery(delivery.deliveryId);
        toast.success('Xóa giao xe thành công');
        loadDeliveries();
      } catch (error) {
        console.error('Error deleting delivery:', error);
        toast.error('Không thể xóa giao xe');
      }
    }
  };

  const handleEdit = async (delivery) => {
    try {
      setSelectedDelivery(delivery);
      setModalMode('edit');
      setShowDeliveryModal(true);
    } catch (error) {
      console.error('Error getting delivery details:', error);
      toast.error('Không thể tải thông tin giao xe');
    }
  };

  const handleView = async (delivery) => {
    try {
      setSelectedDelivery(delivery);
      setModalMode('view');
      setShowDeliveryModal(true);
    } catch (error) {
      console.error('Error getting delivery details:', error);
      toast.error('Không thể tải thông tin giao xe');
    }
  };

  const handleSaveDelivery = async (deliveryId, deliveryData) => {
    try {
      await deliveryAPI.updateDelivery(deliveryId, deliveryData);
      toast.success('Cập nhật giao xe thành công');
      loadDeliveries();
    } catch (error) {
      console.error('Error updating delivery:', error);
      toast.error('Không thể cập nhật giao xe');
      throw error;
    }
  };


  const handleSchedule = async (delivery) => {
    try {
      await deliveryAPI.scheduleDelivery(delivery.deliveryId);
      toast.success('Lên lịch giao xe thành công');
      loadDeliveries();
    } catch (error) {
      console.error('Error scheduling delivery:', error);
      toast.error('Không thể lên lịch giao xe');
    }
  };

  const handleComplete = async (delivery) => {
    try {
      await deliveryAPI.completeDelivery(delivery.deliveryId);
      toast.success('Hoàn thành giao xe thành công');
      loadDeliveries();
    } catch (error) {
      console.error('Error completing delivery:', error);
      toast.error('Không thể hoàn thành giao xe');
    }
  };

  const handleTracking = async (delivery) => {
    try {
      const response = await deliveryAPI.getDeliveryTracking(delivery.deliveryId);
      console.log('Tracking info:', response.data);
      toast('Đã tải theo dõi giao xe', { icon: '📍' });
    } catch (error) {
      console.error('Error getting tracking:', error);
      toast.error('Không thể tải thông tin theo dõi');
    }
  };

  const getStatusBadge = (status) => {
    const statusInfo = getStatusBadgeUtil('delivery', status);
    return <span className={`badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const filteredDeliveries = deliveries.filter(delivery => {
    const searchLower = searchTerm.toLowerCase();
    return (
      delivery.deliveryNumber?.toLowerCase().includes(searchLower) ||
      delivery.customer?.firstName?.toLowerCase().includes(searchLower) ||
      delivery.customer?.lastName?.toLowerCase().includes(searchLower) ||
      delivery.customer?.email?.toLowerCase().includes(searchLower) ||
      delivery.status?.toLowerCase().includes(searchLower)
    );
  });

  const columns = [
    { 
      key: 'deliveryNumber', 
      header: 'Số giao xe',
      render: (delivery) => (
        <div className="delivery-number">
          <Truck size={16} />
          {delivery.deliveryNumber}
        </div>
      )
    },
    { 
      key: 'customer', 
      header: 'Khách hàng',
      render: (delivery) => (
        <div className="customer-info">
          <User size={16} />
          <div>
            <div className="customer-name">
              {delivery.customer?.firstName} {delivery.customer?.lastName}
            </div>
            <div className="customer-email">{delivery.customer?.email}</div>
          </div>
        </div>
      )
    },
    { 
      key: 'vehicle', 
      header: 'Xe',
      render: (delivery) => (
        <div className="vehicle-info">
          <Package size={16} />
          <div>
            <div className="vehicle-name">
              {delivery.vehicle?.variant?.model?.brand?.name} {delivery.vehicle?.variant?.model?.name}
            </div>
            <div className="vehicle-variant">{delivery.vehicle?.variant?.name}</div>
          </div>
        </div>
      )
    },
    { 
      key: 'deliveryAddress', 
      header: 'Địa chỉ giao',
      render: (delivery) => (
        <div className="address">
          <MapPin size={16} />
          {delivery.deliveryAddress || 'N/A'}
        </div>
      )
    },
    { 
      key: 'status', 
      header: 'Trạng thái',
      render: (delivery) => getStatusBadge(delivery.status)
    },
    { 
      key: 'scheduledDate', 
      header: 'Ngày giao dự kiến',
      render: (delivery) => (
        <div className="date">
          <Calendar size={16} />
          {delivery.scheduledDate ? new Date(delivery.scheduledDate).toLocaleDateString('vi-VN') : 'N/A'}
        </div>
      )
    }
  ];

  if (loading) {
    return <LoadingSpinner text="Đang tải danh sách giao xe..." />;
  }

  return (
    <div className="delivery-management">
      <div className="page-header">
        <div className="page-title">
          <Truck className="title-icon" />
          <h1>Quản lý giao xe</h1>
        </div>
        <p>Quản lý lịch trình và trạng thái giao xe</p>
      </div>

      <div className="content">
        <div className="section-header">
          <h2>Danh sách giao xe</h2>
          <button className="btn btn-primary">
            <Plus size={20} />
            Lên lịch giao xe
          </button>
        </div>

        <DataTable
          data={filteredDeliveries}
          columns={columns}
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          searchPlaceholder="Tìm kiếm giao xe..."
          emptyMessage="Không có giao xe nào"
          onEdit={handleEdit}
          onDelete={handleDelete}
          onView={handleView}
          onScheduleDelivery={handleSchedule}
          onCompleteDelivery={handleComplete}
          onGetTracking={handleTracking}
        />

        {/* Delivery Modal */}
        <DeliveryModal
          delivery={selectedDelivery}
          isOpen={showDeliveryModal}
          mode={modalMode}
          onClose={() => {
            setShowDeliveryModal(false);
            setSelectedDelivery(null);
            setModalMode('view');
          }}
          onSave={handleSaveDelivery}
        />
      </div>
    </div>
  );
};

export default DeliveryManagement;
