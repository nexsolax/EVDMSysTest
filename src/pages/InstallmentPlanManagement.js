import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { 
  CreditCard, 
  Plus, 
  Search, 
  Filter, 
  Edit, 
  Trash2, 
  Eye,
  Calendar,
  DollarSign,
  User,
  FileText,
  CheckCircle,
  XCircle,
  Clock
} from 'lucide-react';
import { installmentPlanAPI } from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import DataTable from '../components/common/DataTable';
import './InstallmentPlanManagement.css';

const InstallmentPlanManagement = () => {
  const [installmentPlans, setInstallmentPlans] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [planTypeFilter, setPlanTypeFilter] = useState('all');
  const [showModal, setShowModal] = useState(false);
  const [selectedPlan, setSelectedPlan] = useState(null);
  const [modalMode, setModalMode] = useState('create'); // create, edit, view

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const response = await installmentPlanAPI.getInstallmentPlans();
      setInstallmentPlans(response.data || []);
    } catch (error) {
      console.error('Error loading installment plans:', error);
      toast.error('Lỗi khi tải danh sách kế hoạch trả góp');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setSelectedPlan(null);
    setModalMode('create');
    setShowModal(true);
  };

  const handleEdit = (plan) => {
    setSelectedPlan(plan);
    setModalMode('edit');
    setShowModal(true);
  };

  const handleView = (plan) => {
    setSelectedPlan(plan);
    setModalMode('view');
    setShowModal(true);
  };

  const handleDelete = async (planId) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa kế hoạch trả góp này?')) {
      try {
        await installmentPlanAPI.deleteInstallmentPlan(planId);
        toast.success('Xóa kế hoạch trả góp thành công');
        loadData();
      } catch (error) {
        console.error('Error deleting installment plan:', error);
        toast.error('Lỗi khi xóa kế hoạch trả góp');
      }
    }
  };

  const handleStatusUpdate = async (planId, newStatus) => {
    try {
      await installmentPlanAPI.updateInstallmentPlanStatus(planId, newStatus);
      toast.success('Cập nhật trạng thái thành công');
      loadData();
    } catch (error) {
      console.error('Error updating status:', error);
      toast.error('Lỗi khi cập nhật trạng thái');
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

  const getPlanTypeBadge = (planType) => {
    const typeConfig = {
      customer: { class: 'type-customer', text: 'Khách hàng' },
      dealer: { class: 'type-dealer', text: 'Đại lý' }
    };

    const config = typeConfig[planType] || { class: 'type-default', text: planType };

    return (
      <span className={`type-badge ${config.class}`}>
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

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  const filteredPlans = installmentPlans.filter(plan => {
    const matchesSearch = 
      plan.contractNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      plan.customer?.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      plan.customer?.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      plan.financeCompany?.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus = statusFilter === 'all' || plan.status === statusFilter;
    const matchesPlanType = planTypeFilter === 'all' || plan.planType === planTypeFilter;

    return matchesSearch && matchesStatus && matchesPlanType;
  });

  const columns = [
    {
      key: 'contractNumber',
      title: 'Số hợp đồng',
      render: (plan) => (
        <div className="contract-info">
          <div className="contract-number">{plan.contractNumber}</div>
          <div className="contract-date">{formatDate(plan.contractDate)}</div>
        </div>
      )
    },
    {
      key: 'customer',
      title: 'Khách hàng',
      render: (plan) => (
        <div className="customer-info">
          <div className="customer-name">
            {plan.customer?.firstName} {plan.customer?.lastName}
          </div>
          <div className="customer-phone">{plan.customer?.phone}</div>
        </div>
      )
    },
    {
      key: 'planType',
      title: 'Loại kế hoạch',
      render: (plan) => getPlanTypeBadge(plan.planType)
    },
    {
      key: 'totalAmount',
      title: 'Tổng số tiền',
      render: (plan) => formatCurrency(plan.totalAmount)
    },
    {
      key: 'monthlyPayment',
      title: 'Trả hàng tháng',
      render: (plan) => formatCurrency(plan.monthlyPayment)
    },
    {
      key: 'financeCompany',
      title: 'Công ty tài chính',
      render: (plan) => plan.financeCompany || 'N/A'
    },
    {
      key: 'status',
      title: 'Trạng thái',
      render: (plan) => getStatusBadge(plan.status)
    },
    {
      key: 'actions',
      title: 'Thao tác',
      render: (plan) => (
        <div className="action-buttons">
          <button
            className="btn btn-sm btn-info"
            onClick={() => handleView(plan)}
            title="Xem chi tiết"
          >
            <Eye size={16} />
          </button>
          <button
            className="btn btn-sm btn-warning"
            onClick={() => handleEdit(plan)}
            title="Chỉnh sửa"
          >
            <Edit size={16} />
          </button>
          <button
            className="btn btn-sm btn-danger"
            onClick={() => handleDelete(plan.planId)}
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
    <div className="installment-plan-management">
      <div className="page-header">
        <div className="header-content">
          <h1>
            <CreditCard className="header-icon" />
            Quản lý kế hoạch trả góp
          </h1>
          <p>Quản lý các kế hoạch trả góp của khách hàng và đại lý</p>
        </div>
        <button className="btn btn-primary" onClick={handleCreate}>
          <Plus size={20} />
          Tạo kế hoạch mới
        </button>
      </div>

      <div className="filters-section">
        <div className="search-box">
          <Search size={20} />
          <input
            type="text"
            placeholder="Tìm kiếm theo số hợp đồng, tên khách hàng, công ty tài chính..."
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
              value={planTypeFilter}
              onChange={(e) => setPlanTypeFilter(e.target.value)}
            >
              <option value="all">Tất cả loại</option>
              <option value="customer">Khách hàng</option>
              <option value="dealer">Đại lý</option>
            </select>
          </div>
        </div>
      </div>

      <div className="content-section">
        <DataTable
          data={filteredPlans}
          columns={columns}
          loading={loading}
          emptyMessage="Không có kế hoạch trả góp nào"
        />
      </div>

      {/* Modal sẽ được implement sau */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>
                {modalMode === 'create' && 'Tạo kế hoạch trả góp mới'}
                {modalMode === 'edit' && 'Chỉnh sửa kế hoạch trả góp'}
                {modalMode === 'view' && 'Chi tiết kế hoạch trả góp'}
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

export default InstallmentPlanManagement;
