import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  FileText, 
  Plus, 
  Search, 
  Eye, 
  Edit,
  Trash2,
  Filter,
  Send,
  ShoppingCart,
  AlertCircle
} from 'lucide-react';
import { 
  dealerQuotationAPI,
  dealerOrderAPI
} from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './DealerQuotationRequest.css';

const DealerQuotationRequest = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [quotations, setQuotations] = useState([]);
  const [orders, setOrders] = useState([]);
  const [filteredQuotations, setFilteredQuotations] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  
  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedQuotation, setSelectedQuotation] = useState(null);
  const [formData, setFormData] = useState({
    dealerOrderId: '',
    validUntil: '',
    notes: '',
    discountPercentage: 0,
    discountAmount: 0
  });

  // Role checks
  const isDealerManager = user?.role === 'dealer_manager' || user?.role === 'dealer_staff';
  const isAdmin = user?.role === 'admin';

  useEffect(() => {
    if (!isDealerManager && !isAdmin) {
      toast.error('Bạn không có quyền truy cập trang này');
      navigate('/admin/dashboard');
      return;
    }
    loadData();
  }, []);

  useEffect(() => {
    filterQuotations();
  }, [searchTerm, statusFilter, quotations]);

  const loadData = async () => {
    try {
      setLoading(true);
      const dealerId = user?.dealerId;
      
      // Load quotations cho đại lý
      const quotationsRes = dealerId 
        ? await dealerQuotationAPI.getQuotationsByDealer(dealerId)
        : await dealerQuotationAPI.getQuotations();
      
      // Load orders cho dropdown
      const ordersRes = dealerId
        ? await dealerOrderAPI.getDealerOrdersByDealer(dealerId)
        : await dealerOrderAPI.getDealerOrders();
      
      setQuotations(quotationsRes.data || []);
      setOrders(ordersRes.data || []);
    } catch (error) {
      console.error('Error loading data:', error);
      toast.error('Không thể tải dữ liệu');
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
    setFormData({
      dealerOrderId: '',
      validUntil: '',
      notes: '',
      discountPercentage: 0,
      discountAmount: 0
    });
    setShowCreateModal(true);
  };

  const handleEdit = (quotation) => {
    setSelectedQuotation(quotation);
    setFormData({
      dealerOrderId: quotation.dealerOrderId || '',
      validUntil: quotation.validUntil ? quotation.validUntil.split('T')[0] : '',
      notes: quotation.notes || '',
      discountPercentage: quotation.discountPercentage || 0,
      discountAmount: quotation.discountAmount || 0
    });
    setShowEditModal(true);
  };

  const handleSave = async () => {
    try {
      const data = {
        dealerOrderId: formData.dealerOrderId,
        ...(formData.validUntil ? { validUntil: formData.validUntil } : {}),
        ...(formData.notes ? { notes: formData.notes.trim() } : {}),
        ...(formData.discountPercentage ? { discountPercentage: parseFloat(formData.discountPercentage) } : {}),
        ...(formData.discountAmount ? { discountAmount: parseFloat(formData.discountAmount) } : {})
      };

      if (showEditModal && selectedQuotation) {
        await dealerQuotationAPI.updateQuotation(selectedQuotation.quotationId, data);
        toast.success('Cập nhật báo giá thành công');
      } else {
        await dealerQuotationAPI.createQuotation(data);
        toast.success('Tạo báo giá thành công');
      }
      
      setShowCreateModal(false);
      setShowEditModal(false);
      setSelectedQuotation(null);
      loadData();
    } catch (error) {
      console.error('Error saving quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể lưu báo giá');
    }
  };

  const handleDelete = async (quotation, e) => {
    e.stopPropagation();
    if (!window.confirm(`Bạn có chắc chắn muốn xóa yêu cầu báo giá "${quotation.quotationNumber || 'N/A'}"?`)) {
      return;
    }
    try {
      await dealerQuotationAPI.deleteQuotation(quotation.quotationId);
      toast.success('Xóa yêu cầu báo giá thành công');
      loadData();
    } catch (error) {
      console.error('Error deleting quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể xóa yêu cầu báo giá');
    }
  };

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
    if (!window.confirm(`Bạn có chắc chắn muốn đổi trạng thái thành "${statusLabel}"?`)) {
      return;
    }
    try {
      await dealerQuotationAPI.updateQuotationStatus(quotation.quotationId, newStatus);
      toast.success('Cập nhật trạng thái thành công');
      loadData();
    } catch (error) {
      console.error('Error updating status:', error);
      toast.error(error.response?.data?.error || 'Không thể cập nhật trạng thái');
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
          <button
            onClick={() => handleEdit(item)}
            className="btn-icon btn-edit"
            title="Chỉnh sửa"
          >
            <Edit size={18} />
          </button>
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
          <button
            onClick={(e) => handleDelete(item, e)}
            className="btn-icon btn-delete"
            title="Xóa"
          >
            <Trash2 size={18} />
          </button>
        </div>
      )
    }
  ];

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="dealer-quotation-request">
      <div className="page-header">
        <div className="page-title">
          <FileText className="title-icon" />
          <h1>Yêu cầu báo giá của tôi</h1>
        </div>
        <div className="page-actions">
          <button onClick={handleCreate} className="btn btn-primary">
            <Plus size={18} /> Tạo yêu cầu báo giá mới
          </button>
        </div>
      </div>

      {/* Info Banner */}
      <div className="info-banner">
        <AlertCircle size={20} />
        <span>Trang này hiển thị các yêu cầu báo giá mà bạn đã gửi. Bạn có thể xem, chỉnh sửa, xóa và cập nhật trạng thái.</span>
      </div>

      {/* Filters */}
      <div className="filters-section">
        <div className="filter-group">
          <div className="search-input-wrapper">
            <Search size={18} className="search-icon" />
            <input
              type="text"
              placeholder="Tìm kiếm theo số báo giá, đơn hàng..."
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
        searchPlaceholder="Tìm kiếm yêu cầu báo giá..."
        emptyMessage="Không có yêu cầu báo giá nào"
        onView={handleView}
        actions={false}
      />

      {/* Create/Edit Modal */}
      {(showCreateModal || showEditModal) && (
        <div className="modal-overlay" onClick={() => {
          setShowCreateModal(false);
          setShowEditModal(false);
          setSelectedQuotation(null);
        }}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>{showEditModal ? 'Chỉnh sửa yêu cầu báo giá' : 'Tạo yêu cầu báo giá mới'}</h3>
            <div className="modal-form">
              <div className="form-group">
                <label>Đơn hàng *</label>
                <select
                  value={formData.dealerOrderId}
                  onChange={(e) => setFormData(prev => ({ ...prev, dealerOrderId: e.target.value }))}
                  className="form-select"
                  required
                  disabled={showEditModal}
                >
                  <option value="">-- Chọn đơn hàng --</option>
                  {orders.map((order) => (
                    <option key={order.dealerOrderId} value={order.dealerOrderId}>
                      {order.dealerOrderNumber} - {order.totalQuantity || 0} xe
                    </option>
                  ))}
                </select>
              </div>
              
              <div className="form-group">
                <label>Hết hạn (tùy chọn)</label>
                <input
                  type="date"
                  value={formData.validUntil}
                  onChange={(e) => setFormData(prev => ({ ...prev, validUntil: e.target.value }))}
                  className="form-input"
                  min={new Date().toISOString().split('T')[0]}
                />
              </div>
              
              <div className="form-group">
                <label>Giảm giá % (tùy chọn)</label>
                <input
                  type="number"
                  value={formData.discountPercentage}
                  onChange={(e) => setFormData(prev => ({ ...prev, discountPercentage: e.target.value }))}
                  className="form-input"
                  min="0"
                  max="100"
                  step="0.1"
                />
              </div>
              
              <div className="form-group">
                <label>Giảm giá cố định VNĐ (tùy chọn)</label>
                <input
                  type="number"
                  value={formData.discountAmount}
                  onChange={(e) => setFormData(prev => ({ ...prev, discountAmount: e.target.value }))}
                  className="form-input"
                  min="0"
                />
              </div>
              
              <div className="form-group">
                <label>Ghi chú (tùy chọn)</label>
                <textarea
                  value={formData.notes}
                  onChange={(e) => setFormData(prev => ({ ...prev, notes: e.target.value }))}
                  className="form-textarea"
                  rows="3"
                  placeholder="Nhập ghi chú..."
                />
              </div>
              
              <div className="modal-actions">
                <button
                  onClick={() => {
                    setShowCreateModal(false);
                    setShowEditModal(false);
                    setSelectedQuotation(null);
                  }}
                  className="btn btn-secondary"
                >
                  Hủy
                </button>
                <button
                  onClick={handleSave}
                  className="btn btn-primary"
                  disabled={!formData.dealerOrderId}
                >
                  {showEditModal ? 'Cập nhật' : 'Tạo mới'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DealerQuotationRequest;

