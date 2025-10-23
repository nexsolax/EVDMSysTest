import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { pricingPolicyAPI, dealerAPI, vehicleAPI } from '../services/api';
import './PricingManagement.css';

const PricingManagement = () => {
  const [pricingPolicies, setPricingPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dealers, setDealers] = useState([]);
  const [variants, setVariants] = useState([]);
  const [filterScope, setFilterScope] = useState('all');

  useEffect(() => {
    loadData();
    loadReferenceData();
  }, [filterScope]);

  const loadData = async () => {
    try {
      setLoading(true);
      let response;
      
      switch (filterScope) {
        case 'global':
          response = await pricingPolicyAPI.getGlobalPricingPolicies();
          break;
        case 'dealer-specific':
          response = await pricingPolicyAPI.getDealerSpecificPricingPolicies();
          break;
        case 'active':
          response = await pricingPolicyAPI.getActivePricingPolicies();
          break;
        default:
          response = await pricingPolicyAPI.getPricingPolicies();
      }
      
      setPricingPolicies(response.data || []);
    } catch (error) {
      console.error('Error loading pricing policies:', error);
      toast.error('Không thể tải danh sách chính sách giá');
    } finally {
      setLoading(false);
    }
  };

  const loadReferenceData = async () => {
    try {
      const [dealersRes, variantsRes] = await Promise.all([
        dealerAPI.getDealers(),
        vehicleAPI.getVariants()
      ]);
      
      setDealers(dealersRes.data || []);
      setVariants(variantsRes.data || []);
    } catch (error) {
      console.error('Error loading reference data:', error);
    }
  };

  const getDealerName = (dealerId) => {
    const dealer = dealers.find(d => d.dealerId === dealerId);
    return dealer ? dealer.dealerName : 'N/A';
  };

  const getVariantName = (variantId) => {
    const variant = variants.find(v => v.variantId === variantId);
    return variant ? variant.variantName : 'N/A';
  };

  const formatCurrency = (amount) => {
    if (!amount) return 'N/A';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  const getStatusBadge = (status) => {
    const statusMap = {
      'active': { text: 'Hoạt động', class: 'badge-success' },
      'inactive': { text: 'Không hoạt động', class: 'badge-secondary' },
      'draft': { text: 'Bản nháp', class: 'badge-warning' },
      'expired': { text: 'Hết hạn', class: 'badge-danger' }
    };
    
    const statusInfo = statusMap[status] || { text: status, class: 'badge-secondary' };
    return <span className={`badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const getScopeBadge = (scope) => {
    const scopeMap = {
      'global': { text: 'Toàn hệ thống', class: 'badge-primary' },
      'dealer': { text: 'Đại lý', class: 'badge-info' },
      'region': { text: 'Khu vực', class: 'badge-warning' }
    };
    
    const scopeInfo = scopeMap[scope] || { text: scope, class: 'badge-secondary' };
    return <span className={`badge ${scopeInfo.class}`}>{scopeInfo.text}</span>;
  };

  const getPolicyTypeBadge = (type) => {
    const typeMap = {
      'discount': { text: 'Giảm giá', class: 'badge-success' },
      'markup': { text: 'Tăng giá', class: 'badge-danger' },
      'fixed': { text: 'Cố định', class: 'badge-primary' },
      'percentage': { text: 'Phần trăm', class: 'badge-info' }
    };
    
    const typeInfo = typeMap[type] || { text: type, class: 'badge-secondary' };
    return <span className={`badge ${typeInfo.class}`}>{typeInfo.text}</span>;
  };

  const handleView = async (policy) => {
    try {
      const response = await pricingPolicyAPI.getPricingPolicy(policy.policyId);
      const policyData = response.data;
      
      // Show policy details in a modal or alert
      const details = `
        Tên chính sách: ${policyData.policyName || 'N/A'}
        Loại: ${policyData.policyType || 'N/A'}
        Phạm vi: ${policyData.scope || 'N/A'}
        Đại lý: ${getDealerName(policyData.dealer?.dealerId)}
        Xe: ${getVariantName(policyData.variant?.variantId)}
        Giá trị: ${formatCurrency(policyData.policyValue)}
        Ngày bắt đầu: ${formatDate(policyData.startDate)}
        Ngày kết thúc: ${formatDate(policyData.endDate)}
        Trạng thái: ${policyData.status || 'N/A'}
        Mô tả: ${policyData.description || 'Không có'}
      `;
      
      alert(details);
    } catch (error) {
      console.error('Error loading policy details:', error);
      toast.error('Không thể tải thông tin chính sách giá');
    }
  };

  const handleEdit = async (policy) => {
    try {
      const response = await pricingPolicyAPI.getPricingPolicy(policy.policyId);
      const policyData = response.data;
      
      // Open edit modal with policyData
      const editData = {
        policyName: policyData.policyName,
        policyType: policyData.policyType,
        scope: policyData.scope,
        dealerId: policyData.dealer?.dealerId,
        variantId: policyData.variant?.variantId,
        policyValue: policyData.policyValue,
        startDate: policyData.startDate,
        endDate: policyData.endDate,
        status: policyData.status,
        description: policyData.description
      };
      
      console.log('Edit policy data:', editData);
      toast.info('Chức năng chỉnh sửa sẽ được implement trong modal');
    } catch (error) {
      console.error('Error loading policy for edit:', error);
      toast.error('Không thể tải thông tin chính sách giá');
    }
  };

  const handleDelete = async (policy) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa chính sách giá "${policy.policyName}"?`)) {
      try {
        await pricingPolicyAPI.deletePricingPolicy(policy.policyId);
        toast.success('Xóa chính sách giá thành công');
        loadData();
      } catch (error) {
        console.error('Error deleting policy:', error);
        toast.error('Không thể xóa chính sách giá');
      }
    }
  };

  const handleUpdateStatus = async (policy, newStatus) => {
    try {
      await pricingPolicyAPI.updatePricingPolicyStatus(policy.policyId, newStatus);
      toast.success('Cập nhật trạng thái thành công');
      loadData();
    } catch (error) {
      console.error('Error updating policy status:', error);
      toast.error('Không thể cập nhật trạng thái');
    }
  };

  const columns = [
    {
      key: 'policyName',
      label: 'Tên chính sách',
      render: (value) => <strong>{value || 'N/A'}</strong>
    },
    {
      key: 'policyType',
      label: 'Loại',
      render: (value) => getPolicyTypeBadge(value)
    },
    {
      key: 'scope',
      label: 'Phạm vi',
      render: (value) => getScopeBadge(value)
    },
    {
      key: 'dealer',
      label: 'Đại lý',
      render: (value, row) => getDealerName(row.dealer?.dealerId)
    },
    {
      key: 'variant',
      label: 'Xe',
      render: (value, row) => getVariantName(row.variant?.variantId)
    },
    {
      key: 'policyValue',
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
    <div className="pricing-management">
      <div className="page-header">
        <h1>Quản lý chính sách giá</h1>
        <div className="header-actions">
          <button 
            className="btn btn-primary"
            onClick={() => toast.info('Chức năng tạo chính sách giá mới sẽ được implement')}
          >
            <i className="fas fa-plus"></i> Tạo chính sách mới
          </button>
        </div>
      </div>

      <div className="filter-section">
        <div className="filter-tabs">
          <button 
            className={`filter-tab ${filterScope === 'all' ? 'active' : ''}`}
            onClick={() => setFilterScope('all')}
          >
            Tất cả
          </button>
          <button 
            className={`filter-tab ${filterScope === 'global' ? 'active' : ''}`}
            onClick={() => setFilterScope('global')}
          >
            Toàn hệ thống
          </button>
          <button 
            className={`filter-tab ${filterScope === 'dealer-specific' ? 'active' : ''}`}
            onClick={() => setFilterScope('dealer-specific')}
          >
            Đại lý cụ thể
          </button>
          <button 
            className={`filter-tab ${filterScope === 'active' ? 'active' : ''}`}
            onClick={() => setFilterScope('active')}
          >
            Đang hoạt động
          </button>
        </div>
      </div>

      <div className="data-section">
        <DataTable
          data={pricingPolicies}
          columns={columns}
          actions={actions}
          statusActions={statusActions}
          onStatusUpdate={handleUpdateStatus}
          searchable={true}
          searchPlaceholder="Tìm kiếm chính sách giá..."
        />
      </div>
    </div>
  );
};

export default PricingManagement;