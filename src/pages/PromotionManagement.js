import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { promotionAPI } from '../services/api';
import '../styles/common.css';
import '../styles/filters.css';
import './PromotionManagement.css';

const PromotionManagement = () => {
  const [promotions, setPromotions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('all');

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterStatus]);

  const loadData = async () => {
    try {
      setLoading(true);
      let response;
      
      switch (filterStatus) {
        case 'active':
          response = await promotionAPI.getActivePromotions();
          break;
        default:
          response = await promotionAPI.getPromotions();
      }
      
      setPromotions(response.data || []);
    } catch (error) {
      console.error('Error loading promotions:', error);
      toast.error('Không thể tải danh sách khuyến mãi');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  const formatCurrency = (amount) => {
    if (!amount) return 'N/A';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const getStatusBadge = (status) => {
    const statusMap = {
      'active': { text: 'Hoạt động', class: 'badge-success' },
      'inactive': { text: 'Không hoạt động', class: 'badge-secondary' },
      'draft': { text: 'Bản nháp', class: 'badge-warning' },
      'expired': { text: 'Hết hạn', class: 'badge-danger' },
      'scheduled': { text: 'Đã lên lịch', class: 'badge-info' }
    };
    
    const statusInfo = statusMap[status] || { text: status, class: 'badge-secondary' };
    return <span className={`badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const getTypeBadge = (type) => {
    const typeMap = {
      'discount_percentage': { text: 'Giảm %', class: 'badge-success' },
      'discount_amount': { text: 'Giảm tiền', class: 'badge-primary' },
      'free_gift': { text: 'Quà tặng', class: 'badge-info' },
      'cashback': { text: 'Hoàn tiền', class: 'badge-warning' },
      'installment': { text: 'Trả góp', class: 'badge-secondary' }
    };
    
    const typeInfo = typeMap[type] || { text: type, class: 'badge-secondary' };
    return <span className={`badge ${typeInfo.class}`}>{typeInfo.text}</span>;
  };

  const handleView = async (promotion) => {
    try {
      const response = await promotionAPI.getPromotion(promotion.promotionId);
      const promotionData = response.data;
      
      // Show promotion details in a modal or alert
      const details = `
        Tên khuyến mãi: ${promotionData.promotionName || 'N/A'}
        Loại: ${promotionData.promotionType || 'N/A'}
        Giá trị: ${formatCurrency(promotionData.promotionValue)}
        Mô tả: ${promotionData.description || 'N/A'}
        Điều kiện: ${promotionData.conditions || 'N/A'}
        Ngày bắt đầu: ${formatDate(promotionData.startDate)}
        Ngày kết thúc: ${formatDate(promotionData.endDate)}
        Trạng thái: ${promotionData.status || 'N/A'}
        Số lượng tối đa: ${promotionData.maxUsage || 'Không giới hạn'}
        Số lượng đã sử dụng: ${promotionData.usedCount || 0}
        Ghi chú: ${promotionData.notes || 'Không có'}
      `;
      
      alert(details);
    } catch (error) {
      console.error('Error loading promotion details:', error);
      toast.error('Không thể tải thông tin khuyến mãi');
    }
  };

  const handleEdit = async (promotion) => {
    try {
      const response = await promotionAPI.getPromotion(promotion.promotionId);
      const promotionData = response.data;
      
      // Open edit modal with promotionData
      const editData = {
        promotionName: promotionData.promotionName,
        promotionType: promotionData.promotionType,
        promotionValue: promotionData.promotionValue,
        description: promotionData.description,
        conditions: promotionData.conditions,
        startDate: promotionData.startDate,
        endDate: promotionData.endDate,
        status: promotionData.status,
        maxUsage: promotionData.maxUsage,
        notes: promotionData.notes
      };
      
      console.log('Edit promotion data:', editData);
      toast.info('Chức năng chỉnh sửa sẽ được implement trong modal');
    } catch (error) {
      console.error('Error loading promotion for edit:', error);
      toast.error('Không thể tải thông tin khuyến mãi');
    }
  };

  const handleDelete = async (promotion) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa khuyến mãi "${promotion.promotionName}"?`)) {
      try {
        await promotionAPI.deletePromotion(promotion.promotionId);
        toast.success('Xóa khuyến mãi thành công');
        loadData();
      } catch (error) {
        console.error('Error deleting promotion:', error);
        toast.error('Không thể xóa khuyến mãi');
      }
    }
  };

  const handleUpdateStatus = async (promotion, newStatus) => {
    try {
      await promotionAPI.updatePromotionStatus(promotion.promotionId, newStatus);
      toast.success('Cập nhật trạng thái thành công');
      loadData();
    } catch (error) {
      console.error('Error updating promotion status:', error);
      toast.error('Không thể cập nhật trạng thái');
    }
  };

  const columns = [
    {
      key: 'promotionId',
      label: 'ID',
      render: (value) => {
        console.log('promotionId render:', value, typeof value);
        return value || 'N/A';
      }
    },
    {
      key: 'promotionName',
      label: 'Tên khuyến mãi',
      render: (value) => <strong>{value || 'N/A'}</strong>
    },
    {
      key: 'variant',
      label: 'Xe áp dụng',
      render: (value, row) => {
        console.log('variant render:', value, typeof value, row);
        if (typeof value === 'object' && value !== null) {
          return value.variantName || value.name || 'N/A';
        }
        return value || 'N/A';
      }
    },
    {
      key: 'promotionType',
      label: 'Loại',
      render: (value) => getTypeBadge(value)
    },
    {
      key: 'title',
      label: 'Tiêu đề',
      render: (value) => value || 'N/A'
    },
    {
      key: 'description',
      label: 'Mô tả',
      render: (value) => value ? (value.length > 50 ? value.substring(0, 50) + '...' : value) : 'N/A'
    },
    {
      key: 'discountPercent',
      label: 'Giảm %',
      render: (value) => value ? `${value}%` : 'N/A'
    },
    {
      key: 'discountAmount',
      label: 'Giảm tiền',
      render: (value) => formatCurrency(value)
    },
    {
      key: 'promotionValue',
      label: 'Giá trị',
      render: (value) => formatCurrency(value)
    },
    {
      key: 'startDate',
      label: 'Bắt đầu',
      render: (value) => formatDate(value)
    },
    {
      key: 'endDate',
      label: 'Kết thúc',
      render: (value) => formatDate(value)
    },
    {
      key: 'maxUsage',
      label: 'Số lượng',
      render: (value) => value ? value : 'Không giới hạn'
    },
    {
      key: 'status',
      label: 'Trạng thái',
      render: (value) => getStatusBadge(value)
    }
  ];

  const actions = [
    {
      label: 'Xem',
      className: 'btn-info',
      onClick: handleView
    },
    {
      label: 'Sửa',
      className: 'btn-warning',
      onClick: handleEdit
    },
    {
      label: 'Xóa',
      className: 'btn-danger',
      onClick: handleDelete
    }
  ];

  const statusActions = [
    {
      label: 'Kích hoạt',
      value: 'active',
      className: 'btn-success'
    },
    {
      label: 'Vô hiệu hóa',
      value: 'inactive',
      className: 'btn-secondary'
    },
    {
      label: 'Hết hạn',
      value: 'expired',
      className: 'btn-danger'
    }
  ];

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="promotion-management">
      <div className="page-header">
        <h1>Quản lý khuyến mãi</h1>
        <div className="header-actions">
          <button 
            className="btn btn-primary"
            onClick={() => toast.info('Chức năng tạo khuyến mãi mới sẽ được implement')}
          >
            <i className="fas fa-plus"></i> Tạo khuyến mãi mới
          </button>
        </div>
      </div>

      <div className="filter-section">
        <div className="filter-tabs">
          <button 
            className={`filter-tab ${filterStatus === 'all' ? 'active' : ''}`}
            onClick={() => setFilterStatus('all')}
          >
            Tất cả
          </button>
          <button 
            className={`filter-tab ${filterStatus === 'active' ? 'active' : ''}`}
            onClick={() => setFilterStatus('active')}
          >
            Đang hoạt động
          </button>
        </div>
      </div>

      <div className="data-section">
        <DataTable
          data={promotions}
          columns={columns}
          actions={actions}
          statusActions={statusActions}
          onStatusUpdate={handleUpdateStatus}
          searchable={true}
          searchPlaceholder="Tìm kiếm khuyến mãi..."
        />
      </div>
    </div>
  );
};

export default PromotionManagement;