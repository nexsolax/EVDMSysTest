import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { 
  Target, 
  Plus, 
  Search, 
  Filter, 
  Edit, 
  Trash2, 
  Eye,
  Calendar,
  TrendingUp,
  Building2,
  CheckCircle,
  XCircle,
  Clock,
  BarChart3
} from 'lucide-react';
import { dealerTargetAPI } from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import DataTable from '../components/common/DataTable';
import './DealerTargetManagement.css';

const DealerTargetManagement = () => {
  const [dealerTargets, setDealerTargets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [typeFilter, setTypeFilter] = useState('all');
  const [yearFilter, setYearFilter] = useState('all');
  const [scopeFilter, setScopeFilter] = useState('all');
  const [showModal, setShowModal] = useState(false);
  const [selectedTarget, setSelectedTarget] = useState(null);
  const [modalMode, setModalMode] = useState('create'); // create, edit, view

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const response = await dealerTargetAPI.getDealerTargets();
      setDealerTargets(response.data || []);
    } catch (error) {
      console.error('Error loading dealer targets:', error);
      toast.error('Lỗi khi tải danh sách mục tiêu đại lý');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setSelectedTarget(null);
    setModalMode('create');
    setShowModal(true);
  };

  const handleEdit = (target) => {
    setSelectedTarget(target);
    setModalMode('edit');
    setShowModal(true);
  };

  const handleView = (target) => {
    setSelectedTarget(target);
    setModalMode('view');
    setShowModal(true);
  };

  const handleDelete = async (targetId) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa mục tiêu này?')) {
      try {
        await dealerTargetAPI.deleteDealerTarget(targetId);
        toast.success('Xóa mục tiêu thành công');
        loadData();
      } catch (error) {
        console.error('Error deleting dealer target:', error);
        toast.error('Lỗi khi xóa mục tiêu');
      }
    }
  };

  const handleStatusUpdate = async (targetId, newStatus) => {
    try {
      await dealerTargetAPI.updateDealerTargetStatus(targetId, newStatus);
      toast.success('Cập nhật trạng thái thành công');
      loadData();
    } catch (error) {
      console.error('Error updating status:', error);
      toast.error('Lỗi khi cập nhật trạng thái');
    }
  };

  const handleAchievementUpdate = async (targetId, achievement) => {
    try {
      await dealerTargetAPI.updateDealerTargetAchievement(targetId, achievement);
      toast.success('Cập nhật thành tích thành công');
      loadData();
    } catch (error) {
      console.error('Error updating achievement:', error);
      toast.error('Lỗi khi cập nhật thành tích');
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      active: { class: 'status-active', icon: CheckCircle, text: 'Đang hoạt động' },
      completed: { class: 'status-completed', icon: CheckCircle, text: 'Hoàn thành' },
      cancelled: { class: 'status-cancelled', icon: XCircle, text: 'Đã hủy' },
      pending: { class: 'status-pending', icon: Clock, text: 'Chờ xử lý' }
    };

    const config = statusConfig[status] || statusConfig.pending;
    const Icon = config.icon;

    return (
      <span className={`status-badge ${config.class}`}>
        <Icon size={14} />
        {config.text}
      </span>
    );
  };

  const getTypeBadge = (type) => {
    const typeConfig = {
      sales: { class: 'type-sales', text: 'Bán hàng' },
      revenue: { class: 'type-revenue', text: 'Doanh thu' },
      customer: { class: 'type-customer', text: 'Khách hàng' },
      inventory: { class: 'type-inventory', text: 'Tồn kho' }
    };

    const config = typeConfig[type] || { class: 'type-default', text: type };

    return (
      <span className={`type-badge ${config.class}`}>
        {config.text}
      </span>
    );
  };

  const getScopeBadge = (scope) => {
    const scopeConfig = {
      global: { class: 'scope-global', text: 'Toàn hệ thống' },
      dealer: { class: 'scope-dealer', text: 'Đại lý' }
    };

    const config = scopeConfig[scope] || { class: 'scope-default', text: scope };

    return (
      <span className={`scope-badge ${config.class}`}>
        {config.text}
      </span>
    );
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const formatNumber = (number) => {
    return new Intl.NumberFormat('vi-VN').format(number);
  };

  const getAchievementRate = (target) => {
    if (!target.targetValue || target.targetValue === 0) return 0;
    return Math.round((target.actualValue / target.targetValue) * 100);
  };

  const getAchievementBadge = (rate) => {
    let className = 'achievement-badge';
    if (rate >= 100) className += ' achievement-excellent';
    else if (rate >= 80) className += ' achievement-good';
    else if (rate >= 60) className += ' achievement-average';
    else className += ' achievement-poor';

    return (
      <span className={className}>
        {rate}%
      </span>
    );
  };

  const filteredTargets = dealerTargets.filter(target => {
    const matchesSearch = 
      target.targetName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      target.dealer?.dealerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      target.description?.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus = statusFilter === 'all' || target.targetStatus === statusFilter;
    const matchesType = typeFilter === 'all' || target.targetType === typeFilter;
    const matchesYear = yearFilter === 'all' || target.targetYear?.toString() === yearFilter;
    const matchesScope = scopeFilter === 'all' || target.targetScope === scopeFilter;

    return matchesSearch && matchesStatus && matchesType && matchesYear && matchesScope;
  });

  const columns = [
    {
      key: 'targetName',
      title: 'Tên mục tiêu',
      render: (target) => (
        <div className="target-info">
          <div className="target-name">{target.targetName}</div>
          <div className="target-description">{target.description}</div>
        </div>
      )
    },
    {
      key: 'dealer',
      title: 'Đại lý',
      render: (target) => (
        <div className="dealer-info">
          <div className="dealer-name">
            {target.dealer?.dealerName || 'Toàn hệ thống'}
          </div>
          <div className="dealer-code">{target.dealer?.dealerCode}</div>
        </div>
      )
    },
    {
      key: 'targetType',
      title: 'Loại mục tiêu',
      render: (target) => getTypeBadge(target.targetType)
    },
    {
      key: 'targetScope',
      title: 'Phạm vi',
      render: (target) => getScopeBadge(target.targetScope)
    },
    {
      key: 'period',
      title: 'Thời gian',
      render: (target) => (
        <div className="period-info">
          <div className="period-year">{target.targetYear}</div>
          <div className="period-month">
            {target.targetMonth ? `Tháng ${target.targetMonth}` : 'Cả năm'}
          </div>
        </div>
      )
    },
    {
      key: 'targetValue',
      title: 'Mục tiêu',
      render: (target) => (
        <div className="value-info">
          <div className="target-value">
            {target.targetType === 'revenue' || target.targetType === 'sales' 
              ? formatCurrency(target.targetValue)
              : formatNumber(target.targetValue)
            }
          </div>
          <div className="target-unit">
            {target.targetType === 'revenue' || target.targetType === 'sales' 
              ? 'VND' 
              : target.targetUnit || 'đơn vị'
            }
          </div>
        </div>
      )
    },
    {
      key: 'actualValue',
      title: 'Thực tế',
      render: (target) => (
        <div className="value-info">
          <div className="actual-value">
            {target.targetType === 'revenue' || target.targetType === 'sales' 
              ? formatCurrency(target.actualValue || 0)
              : formatNumber(target.actualValue || 0)
            }
          </div>
          <div className="achievement-rate">
            {getAchievementBadge(getAchievementRate(target))}
          </div>
        </div>
      )
    },
    {
      key: 'status',
      title: 'Trạng thái',
      render: (target) => getStatusBadge(target.targetStatus)
    },
    {
      key: 'actions',
      title: 'Thao tác',
      render: (target) => (
        <div className="action-buttons">
          <button
            className="btn btn-sm btn-info"
            onClick={() => handleView(target)}
            title="Xem chi tiết"
          >
            <Eye size={16} />
          </button>
          <button
            className="btn btn-sm btn-warning"
            onClick={() => handleEdit(target)}
            title="Chỉnh sửa"
          >
            <Edit size={16} />
          </button>
          <button
            className="btn btn-sm btn-danger"
            onClick={() => handleDelete(target.targetId)}
            title="Xóa"
          >
            <Trash2 size={16} />
          </button>
        </div>
      )
    }
  ];

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="dealer-target-management">
      <div className="page-header">
        <div className="header-content">
          <h1>
            <Target className="header-icon" />
            Quản lý mục tiêu đại lý
          </h1>
          <p>Quản lý các mục tiêu và thành tích của đại lý</p>
        </div>
        <button className="btn btn-primary" onClick={handleCreate}>
          <Plus size={20} />
          Tạo mục tiêu mới
        </button>
      </div>

      <div className="filters-section">
        <div className="search-box">
          <Search size={20} />
          <input
            type="text"
            placeholder="Tìm kiếm theo tên mục tiêu, đại lý, mô tả..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filter-controls">
          <div className="filter-group">
            <Filter size={16} />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="all">Tất cả trạng thái</option>
              <option value="active">Đang hoạt động</option>
              <option value="completed">Hoàn thành</option>
              <option value="cancelled">Đã hủy</option>
              <option value="pending">Chờ xử lý</option>
            </select>
          </div>

          <div className="filter-group">
            <select
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
            >
              <option value="all">Tất cả loại</option>
              <option value="sales">Bán hàng</option>
              <option value="revenue">Doanh thu</option>
              <option value="customer">Khách hàng</option>
              <option value="inventory">Tồn kho</option>
            </select>
          </div>

          <div className="filter-group">
            <select
              value={yearFilter}
              onChange={(e) => setYearFilter(e.target.value)}
            >
              <option value="all">Tất cả năm</option>
              <option value="2024">2024</option>
              <option value="2023">2023</option>
              <option value="2022">2022</option>
            </select>
          </div>

          <div className="filter-group">
            <select
              value={scopeFilter}
              onChange={(e) => setScopeFilter(e.target.value)}
            >
              <option value="all">Tất cả phạm vi</option>
              <option value="global">Toàn hệ thống</option>
              <option value="dealer">Đại lý</option>
            </select>
          </div>
        </div>
      </div>

      <div className="content-section">
        <DataTable
          data={filteredTargets}
          columns={columns}
          loading={loading}
          emptyMessage="Không có mục tiêu nào"
        />
      </div>

      {/* Modal sẽ được implement sau */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>
                {modalMode === 'create' && 'Tạo mục tiêu mới'}
                {modalMode === 'edit' && 'Chỉnh sửa mục tiêu'}
                {modalMode === 'view' && 'Chi tiết mục tiêu'}
              </h3>
              <button
                className="btn btn-close"
                onClick={() => setShowModal(false)}
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <p>Modal content sẽ được implement sau...</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DealerTargetManagement;
