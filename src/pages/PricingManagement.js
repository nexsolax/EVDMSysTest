import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import PricingModal from '../components/modals/PricingModal';
import { pricingPolicyAPI, dealerAPI, vehicleAPI } from '../services/api';
import './PricingManagement.css';

const PricingManagement = () => {
  const [pricingPolicies, setPricingPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedPolicy, setSelectedPolicy] = useState(null);
  const [showPricingModal, setShowPricingModal] = useState(false);
  const [modalMode, setModalMode] = useState('view');
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

  const handleView = (policy) => {
    setSelectedPolicy(policy);
    setModalMode('view');
    setShowPricingModal(true);
  };

  const handleEdit = (policy) => {
    setSelectedPolicy(policy);
    setModalMode('edit');
    setShowPricingModal(true);
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

  const handleCreate = () => {
    setSelectedPolicy(null);
    setModalMode('create');
    setShowPricingModal(true);
  };

  const handleSavePolicy = async (policyId, policyData) => {
    try {
      if (policyId) {
        // Update existing policy
        await pricingPolicyAPI.updatePricingPolicy(policyId, policyData);
        toast.success('Cập nhật chính sách giá thành công');
      } else {
        // Create new policy
        await pricingPolicyAPI.createPricingPolicy(policyData);
        toast.success('Tạo chính sách giá thành công');
      }
      loadData();
      setShowPricingModal(false);
    } catch (error) {
      console.error('Error saving pricing policy:', error);
      throw error; // Re-throw to be handled by modal
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
      render: (value, row) => getDealerName(row?.dealer?.dealerId)
    },
    {
      key: 'variant',
      label: 'Xe',
      render: (value, row) => getVariantName(row?.variant?.variantId)
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
            onClick={handleCreate}
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
          searchable={true}
          searchPlaceholder="Tìm kiếm chính sách giá..."
        />
      </div>

      {/* Pricing Modal */}
      <PricingModal
        policy={selectedPolicy}
        isOpen={showPricingModal}
        mode={modalMode}
        onClose={() => {
          setShowPricingModal(false);
          setSelectedPolicy(null);
          setModalMode('view');
        }}
        onSave={handleSavePolicy}
      />
    </div>
  );
};

export default PricingManagement;