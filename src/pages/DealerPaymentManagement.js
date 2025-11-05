import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  CreditCard, 
  Search, 
  Eye,
  Filter,
  DollarSign,
  ArrowLeft,
  Calendar,
  BarChart3
} from 'lucide-react';
import { dealerPaymentAPI, dealerInvoiceAPI } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './DealerPaymentManagement.css';

const DealerPaymentManagement = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [payments, setPayments] = useState([]);
  const [filteredPayments, setFilteredPayments] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [typeFilter, setTypeFilter] = useState('all');
  const [invoiceFilter, setInvoiceFilter] = useState('all');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [invoices, setInvoices] = useState([]);
  const [statistics, setStatistics] = useState(null);
  
  // Role checks
  const isEVMStaff = user?.role === 'evm_staff' || user?.role === 'admin';
  const isDealerManager = user?.role === 'dealer_manager' || user?.role === 'dealer_staff';
  const isAdmin = user?.role === 'admin';

  useEffect(() => {
    loadPayments();
    loadStatistics();
    if (isEVMStaff || isAdmin) {
      loadInvoices();
    }
  }, []);

  useEffect(() => {
    // Reload payments when filters change (API-based filters)
    if (statusFilter !== 'all' || typeFilter !== 'all' || startDate || endDate) {
      loadPayments();
    } else {
      filterPayments();
    }
  }, [statusFilter, typeFilter, startDate, endDate]);

  useEffect(() => {
    // Client-side filtering for search and invoice
    filterPayments();
  }, [searchTerm, invoiceFilter, payments]);

  const loadPayments = async () => {
    try {
      setLoading(true);
      let response;
      
      // Priority: Date range filter
      if (startDate || endDate) {
        response = await dealerPaymentAPI.getPaymentsByDateRange(startDate, endDate);
      }
      // Type filter
      else if (typeFilter !== 'all') {
        response = await dealerPaymentAPI.getPaymentsByType(typeFilter);
      }
      // Status filter
      else if (statusFilter !== 'all') {
        response = await dealerPaymentAPI.getPaymentsByStatus(statusFilter);
      }
      // Dealer users chỉ thấy payments của mình
      else if (isDealerManager && user?.dealerId) {
        response = await dealerPaymentAPI.getPaymentsByDealer(user.dealerId);
      }
      // Default: all payments
      else {
        response = await dealerPaymentAPI.getPayments();
      }
      
      console.log('Dealer payments response:', response);
      console.log('Dealer payments data:', response.data);
      
      setPayments(response.data || []);
    } catch (error) {
      console.error('Error loading payments:', error);
      
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
        
        if (error.response.data && error.response.data.error) {
          toast.error(`Lỗi server: ${error.response.data.error}`);
        } else if (error.response.status === 500) {
          toast.error('Lỗi server (500). Vui lòng kiểm tra backend logs.');
        } else {
          toast.error(`Không thể tải danh sách thanh toán (${error.response.status})`);
        }
      } else {
        toast.error('Không thể tải danh sách thanh toán');
      }
    } finally {
      setLoading(false);
    }
  };

  const loadStatistics = async () => {
    try {
      const response = await dealerPaymentAPI.getPaymentStatistics();
      setStatistics(response.data);
    } catch (error) {
      console.warn('Could not load statistics:', error);
    }
  };

  const loadInvoices = async () => {
    try {
      const response = await dealerInvoiceAPI.getInvoices();
      setInvoices(response.data || []);
    } catch (error) {
      console.warn('Could not load invoices for filter:', error);
    }
  };

  const filterPayments = () => {
    let filtered = [...payments];

    // Search filter
    if (searchTerm.trim()) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(payment =>
        payment.paymentNumber?.toLowerCase().includes(searchLower) ||
        payment.invoiceNumber?.toLowerCase().includes(searchLower) ||
        payment.referenceNumber?.toLowerCase().includes(searchLower)
      );
    }

    // Status filter
    if (statusFilter !== 'all') {
      filtered = filtered.filter(payment => payment.status === statusFilter);
    }

    // Invoice filter
    if (invoiceFilter !== 'all') {
      filtered = filtered.filter(payment => payment.invoiceId === invoiceFilter);
    }

    setFilteredPayments(filtered);
  };

  const getStatusBadge = (status) => {
    // DealerPayment status: lowercase
    const statusMap = {
      'pending': { label: 'Chờ xử lý', color: 'yellow' },
      'completed': { label: 'Hoàn thành', color: 'green' },
      'failed': { label: 'Thất bại', color: 'red' },
      'refunded': { label: 'Đã hoàn tiền', color: 'gray' }
    };
    const config = statusMap[status] || { label: status, color: 'gray' };
    return (
      <span className={`status-badge status-${config.color}`}>
        {config.label}
      </span>
    );
  };

  const handleView = (payment) => {
    // Navigate to invoice detail if available
    if (payment.invoiceId) {
      navigate(`/admin/dealer-invoices/${payment.invoiceId}`);
    } else {
      toast.error('Không tìm thấy hóa đơn liên quan');
    }
  };

  const columns = [
    {
      key: 'paymentNumber',
      header: 'Số thanh toán',
      renderCell: (item) => (
        <div className="payment-number-cell">
          <strong>{item.paymentNumber || 'N/A'}</strong>
        </div>
      )
    },
    {
      key: 'invoiceNumber',
      header: 'Hóa đơn',
      renderCell: (item) => (
        <div>
          <div className="font-medium">{item.invoiceNumber || 'N/A'}</div>
          {item.invoiceId && (
            <button
              onClick={() => navigate(`/admin/dealer-invoices/${item.invoiceId}`)}
              className="link-button"
            >
              Xem hóa đơn
            </button>
          )}
        </div>
      )
    },
    {
      key: 'paymentAmount',
      header: 'Số tiền',
      renderCell: (item) => (
        <div className="font-medium">
          {item.paymentAmount ? item.paymentAmount.toLocaleString('vi-VN') : '0'} VNĐ
        </div>
      )
    },
    {
      key: 'paymentMethod',
      header: 'Phương thức',
      renderCell: (item) => {
        const methodMap = {
          'BANK_TRANSFER': 'Chuyển khoản',
          'CASH': 'Tiền mặt',
          'CREDIT_CARD': 'Thẻ tín dụng',
          'CHEQUE': 'Séc'
        };
        return <div>{methodMap[item.paymentMethod] || item.paymentMethod || 'N/A'}</div>;
      }
    },
    {
      key: 'paymentDate',
      header: 'Ngày thanh toán',
      renderCell: (item) => (
        <div>
          {item.paymentDate ? new Date(item.paymentDate).toLocaleDateString('vi-VN') : 'N/A'}
        </div>
      )
    },
    {
      key: 'status',
      header: 'Trạng thái',
      renderCell: (item) => getStatusBadge(item.status)
    },
    {
      key: 'referenceNumber',
      header: 'Số tham chiếu',
      renderCell: (item) => (
        <div className="text-sm">{item.referenceNumber || '-'}</div>
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
        </div>
      )
    }
  ];

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="dealer-payment-management">
      <div className="page-header">
        <div className="page-title">
          <CreditCard className="title-icon" />
          <h1>Quản lý thanh toán đại lý</h1>
        </div>
      </div>

      {/* Statistics */}
      {statistics && (
        <div className="statistics-section">
          <BarChart3 size={20} />
          <div className="stat-item">
            <span className="stat-label">Tổng số thanh toán:</span>
            <span className="stat-value">{statistics.totalPayments || 0}</span>
          </div>
          {statistics.totalAmount && (
            <div className="stat-item">
              <span className="stat-label">Tổng tiền:</span>
              <span className="stat-value">{statistics.totalAmount.toLocaleString('vi-VN')} VNĐ</span>
            </div>
          )}
          {statistics.completedPayments && (
            <div className="stat-item">
              <span className="stat-label">Đã hoàn thành:</span>
              <span className="stat-value">{statistics.completedPayments}</span>
            </div>
          )}
        </div>
      )}

      {/* Filters */}
      <div className="filters-section">
        <div className="filter-group">
          <div className="search-input-wrapper">
            <Search size={18} className="search-icon" />
            <input
              type="text"
              placeholder="Tìm kiếm theo số thanh toán, hóa đơn, số tham chiếu..."
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
            <option value="pending">Chờ xử lý</option>
            <option value="completed">Hoàn thành</option>
            <option value="failed">Thất bại</option>
            <option value="refunded">Đã hoàn tiền</option>
          </select>
        </div>

        <div className="filter-group">
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="form-select"
          >
            <option value="all">Tất cả phương thức</option>
            <option value="BANK_TRANSFER">Chuyển khoản</option>
            <option value="CASH">Tiền mặt</option>
            <option value="CREDIT_CARD">Thẻ tín dụng</option>
            <option value="CHEQUE">Séc</option>
          </select>
        </div>

        <div className="filter-group">
          <Calendar size={18} />
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="form-input"
            placeholder="Từ ngày"
          />
        </div>

        <div className="filter-group">
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="form-input"
            placeholder="Đến ngày"
            min={startDate}
          />
        </div>

        {(isEVMStaff || isAdmin) && invoices.length > 0 && (
          <div className="filter-group">
            <select
              value={invoiceFilter}
              onChange={(e) => setInvoiceFilter(e.target.value)}
              className="form-select"
            >
              <option value="all">Tất cả hóa đơn</option>
              {invoices.map((invoice) => (
                <option key={invoice.invoiceId} value={invoice.invoiceId}>
                  {invoice.invoiceNumber} - {invoice.totalAmount?.toLocaleString('vi-VN')} VNĐ
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      {/* Data Table */}
      <DataTable
        data={filteredPayments}
        columns={columns}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Tìm kiếm thanh toán..."
        emptyMessage="Không có thanh toán nào"
        onView={handleView}
        actions={false}
      />
    </div>
  );
};

export default DealerPaymentManagement;

