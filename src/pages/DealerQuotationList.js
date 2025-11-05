import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { 
  FileText, 
  Plus, 
  Search, 
  Eye, 
  Send,
  CheckCircle,
  XCircle,
  Filter,
  ArrowRight,
  Edit,
  Trash2
} from 'lucide-react';
import { dealerQuotationAPI, dealerAPI } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './DealerQuotationList.css';

const DealerQuotationList = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [quotations, setQuotations] = useState([]);
  const [filteredQuotations, setFilteredQuotations] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  
  // Role checks
  const isEVMStaff = user?.role === 'evm_staff' || user?.role === 'admin';
  const isDealerManager = user?.role === 'dealer_manager' || user?.role === 'dealer_staff';
  const isAdmin = user?.role === 'admin';
  
  // Check if coming from order detail page
  const orderId = searchParams.get('orderId');

  useEffect(() => {
    loadQuotations();
  }, []);

  useEffect(() => {
    filterQuotations();
  }, [searchTerm, statusFilter, quotations]);

  const loadQuotations = async () => {
    try {
      setLoading(true);
      let response;
      
      // Dealer users chỉ thấy quotations của mình
      if (isDealerManager && user?.dealerId) {
        response = await dealerQuotationAPI.getQuotationsByDealer(user.dealerId);
      } else {
        response = await dealerQuotationAPI.getQuotations();
      }
      
      setQuotations(response.data || []);
    } catch (error) {
      console.error('Error loading quotations:', error);
      toast.error('Không thể tải danh sách báo giá');
    } finally {
      setLoading(false);
    }
  };

  const filterQuotations = () => {
    let filtered = [...quotations];

    // Search filter
    if (searchTerm.trim()) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(quotation =>
        quotation.quotationNumber?.toLowerCase().includes(searchLower) ||
        quotation.dealer?.dealerName?.toLowerCase().includes(searchLower) ||
        quotation.dealer?.dealerCode?.toLowerCase().includes(searchLower) ||
        quotation.dealerOrder?.dealerOrderNumber?.toLowerCase().includes(searchLower)
      );
    }

    // Status filter
    if (statusFilter !== 'all') {
      filtered = filtered.filter(quotation => quotation.status === statusFilter);
    }

    setFilteredQuotations(filtered);
  };

  const getStatusBadge = (status) => {
    // DealerQuotation status: lowercase (theo DEALER_ORDER_API_FOR_FRONTEND.md)
    const statusMap = {
      'pending': { label: 'Nháp', color: 'gray' },
      'sent': { label: 'Đã gửi', color: 'blue' },
      'accepted': { label: 'Đã chấp nhận', color: 'green' },
      'rejected': { label: 'Đã từ chối', color: 'red' },
      'expired': { label: 'Hết hạn', color: 'yellow' },
      'converted': { label: 'Đã chuyển đổi', color: 'purple' }
    };
    const config = statusMap[status] || { label: status, color: 'gray' };
    return (
      <span className={`status-badge status-${config.color}`}>
        {config.label}
      </span>
    );
  };

  const handleView = (quotation) => {
    navigate(`/admin/dealer-quotations/${quotation.quotationId}`);
  };

  const handleCreate = () => {
    if (orderId) {
      navigate(`/admin/dealer-quotations/create?orderId=${orderId}`);
    } else {
      navigate('/admin/dealer-quotations/create');
    }
  };

  // Bước 16: Gửi báo giá (EVM_STAFF, ADMIN)
  const handleSendQuotation = async (quotationId, e) => {
    e.stopPropagation();
    if (!window.confirm('Bạn có chắc chắn muốn gửi báo giá này cho đại lý?')) {
      return;
    }
    try {
      await dealerQuotationAPI.sendQuotation(quotationId);
      toast.success('Đã gửi báo giá thành công');
      loadQuotations();
    } catch (error) {
      console.error('Error sending quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể gửi báo giá');
    }
  };

  // Chỉnh sửa status
  const handleUpdateStatus = async (quotation, newStatus) => {
    const statusLabels = {
      'pending': 'Nháp',
      'sent': 'Đã gửi',
      'accepted': 'Đã chấp nhận',
      'rejected': 'Đã từ chối',
      'expired': 'Hết hạn',
      'converted': 'Đã chuyển đổi'
    };
    const statusLabel = statusLabels[newStatus] || newStatus;
    if (!window.confirm(`Bạn có chắc chắn muốn đổi trạng thái báo giá "${quotation.quotationNumber}" thành "${statusLabel}"?`)) {
      return;
    }
    try {
      await dealerQuotationAPI.updateQuotationStatus(quotation.quotationId, newStatus);
      toast.success('Cập nhật trạng thái thành công');
      loadQuotations();
    } catch (error) {
      console.error('Error updating status:', error);
      toast.error(error.response?.data?.error || 'Không thể cập nhật trạng thái');
    }
  };

  // Xóa báo giá
  const handleDelete = async (quotation, e) => {
    e.stopPropagation();
    if (!window.confirm(`Bạn có chắc chắn muốn xóa báo giá "${quotation.quotationNumber}"?`)) {
      return;
    }
    try {
      await dealerQuotationAPI.deleteQuotation(quotation.quotationId);
      toast.success('Xóa báo giá thành công');
      loadQuotations();
    } catch (error) {
      console.error('Error deleting quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể xóa báo giá');
    }
  };

  const columns = [
    {
      key: 'quotationNumber',
      header: 'Số báo giá',
      renderCell: (item) => (
        <div className="quotation-number-cell">
          <strong>{item.quotationNumber || 'N/A'}</strong>
        </div>
      )
    },
    {
      key: 'dealerOrder',
      header: 'Đơn hàng',
      renderCell: (item) => (
        <div>
          <div className="font-medium">{item.dealerOrder?.dealerOrderNumber || 'N/A'}</div>
          {item.dealerOrder?.orderDate && (
            <div className="text-sm text-gray-500">
              {new Date(item.dealerOrder.orderDate).toLocaleDateString('vi-VN')}
            </div>
          )}
        </div>
      )
    },
    {
      key: 'dealer',
      header: 'Đại lý',
      renderCell: (item) => (
        <div>
          <div className="font-medium">{item.dealer?.dealerName || 'N/A'}</div>
          {item.dealer?.dealerCode && (
            <div className="text-sm text-gray-500">{item.dealer.dealerCode}</div>
          )}
        </div>
      )
    },
    {
      key: 'totalAmount',
      header: 'Tổng tiền',
      renderCell: (item) => (
        <div className="font-medium">
          {item.totalAmount ? item.totalAmount.toLocaleString('vi-VN') : '0'} VNĐ
        </div>
      )
    },
    {
      key: 'status',
      header: 'Trạng thái',
      renderCell: (item) => getStatusBadge(item.status)
    },
    {
      key: 'createdAt',
      header: 'Ngày tạo',
      renderCell: (item) => (
        <div>
          {item.createdAt ? new Date(item.createdAt).toLocaleDateString('vi-VN') : 'N/A'}
        </div>
      )
    },
    {
      key: 'actions',
      header: 'Thao tác',
      renderCell: (item) => (
        <div className="action-buttons">
          <button
            onClick={() => handleView(item)}
            className="btn-icon btn-view"
            title="Xem chi tiết"
          >
            <Eye size={18} />
          </button>
          {/* Chỉnh sửa status - chỉ EVM_STAFF/ADMIN */}
          {isEVMStaff && (
            <select
              value={item.status}
              onChange={(e) => handleUpdateStatus(item, e.target.value)}
              onClick={(e) => e.stopPropagation()}
              className="status-select"
              title="Chỉnh sửa trạng thái"
            >
              <option value="pending">Nháp</option>
              <option value="sent">Đã gửi</option>
              <option value="accepted">Đã chấp nhận</option>
              <option value="rejected">Đã từ chối</option>
              <option value="expired">Hết hạn</option>
              <option value="converted">Đã chuyển đổi</option>
            </select>
          )}
          {/* Bước 16: Gửi báo giá - chỉ hiện khi status = pending và là EVM_STAFF/ADMIN */}
          {isEVMStaff && item.status === 'pending' && (
            <button
              onClick={(e) => handleSendQuotation(item.quotationId, e)}
              className="btn-icon btn-send"
              title="Gửi báo giá"
            >
              <Send size={18} />
            </button>
          )}
          {/* Xóa - chỉ EVM_STAFF/ADMIN */}
          {isEVMStaff && (
            <button
              onClick={(e) => handleDelete(item, e)}
              className="btn-icon btn-delete"
              title="Xóa báo giá"
            >
              <Trash2 size={18} />
            </button>
          )}
        </div>
      )
    }
  ];

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="dealer-quotation-list">
      <div className="page-header">
        <div className="page-title">
          <FileText className="title-icon" />
          <h1>Danh sách báo giá từ đại lý</h1>
        </div>
        <div className="page-actions">
          <button onClick={handleCreate} className="btn btn-primary">
            <Plus size={18} /> Tạo báo giá mới
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="filters-section">
        <div className="filter-group">
          <div className="search-input-wrapper">
            <Search size={18} className="search-icon" />
            <input
              type="text"
              placeholder="Tìm kiếm theo số báo giá, đơn hàng, đại lý..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="form-input"
            />
          </div>
        </div>

        <div className="filter-group">
          <Filter size={18} />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="form-select"
          >
            <option value="all">Tất cả trạng thái</option>
            <option value="pending">Nháp</option>
            <option value="sent">Đã gửi</option>
            <option value="accepted">Đã chấp nhận</option>
            <option value="rejected">Đã từ chối</option>
            <option value="expired">Hết hạn</option>
            <option value="converted">Đã chuyển đổi</option>
          </select>
        </div>
      </div>

      {/* Data Table */}
      <DataTable
        data={filteredQuotations}
        columns={columns}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Tìm kiếm báo giá..."
        emptyMessage="Không có báo giá nào"
        onView={handleView}
        actions={false}
      />
    </div>
  );
};

export default DealerQuotationList;

