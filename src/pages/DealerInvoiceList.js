import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  FileText, 
  Search, 
  Eye, 
  Filter,
  DollarSign
} from 'lucide-react';
import { dealerInvoiceAPI, dealerAPI } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './DealerInvoiceList.css';

const DealerInvoiceList = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [invoices, setInvoices] = useState([]);
  const [filteredInvoices, setFilteredInvoices] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  
  // Role checks
  const isEVMStaff = user?.role === 'evm_staff' || user?.role === 'admin';
  const isDealerManager = user?.role === 'dealer_manager' || user?.role === 'dealer_staff';
  const isAdmin = user?.role === 'admin';

  useEffect(() => {
    loadInvoices();
  }, []);

  useEffect(() => {
    filterInvoices();
  }, [searchTerm, statusFilter, invoices]);

  const loadInvoices = async () => {
    try {
      setLoading(true);
      let response;
      
      // Dealer users chỉ thấy invoices của mình
      if (isDealerManager && user?.dealerId) {
        response = await dealerInvoiceAPI.getInvoicesByDealer(user.dealerId);
      } else {
        response = await dealerInvoiceAPI.getInvoices();
      }
      
      console.log('Dealer invoices response:', response);
      console.log('Dealer invoices data:', response.data);
      
      // Normalize dealer data
      const normalizedInvoices = (response.data || []).map(invoice => {
        const normalizedInvoice = { ...invoice };
        
        // Handle dealer field
        if (normalizedInvoice.dealer && typeof normalizedInvoice.dealer === 'string') {
          try {
            normalizedInvoice.dealer = JSON.parse(normalizedInvoice.dealer);
          } catch (e) {
            console.warn('Failed to parse dealer JSON:', e);
            normalizedInvoice.dealer = null;
          }
        }
        
        return normalizedInvoice;
      });
      
      setInvoices(normalizedInvoices);
    } catch (error) {
      console.error('Error loading invoices:', error);
      
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
        
        if (error.response.data && error.response.data.error) {
          toast.error(`Lỗi server: ${error.response.data.error}`);
        } else if (error.response.status === 500) {
          toast.error('Lỗi server (500). Vui lòng kiểm tra backend logs.');
        } else {
          toast.error(`Không thể tải danh sách hóa đơn (${error.response.status})`);
        }
      } else {
        toast.error('Không thể tải danh sách hóa đơn');
      }
    } finally {
      setLoading(false);
    }
  };

  const filterInvoices = () => {
    let filtered = [...invoices];

    // Search filter
    if (searchTerm.trim()) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(invoice =>
        invoice.invoiceNumber?.toLowerCase().includes(searchLower) ||
        invoice.dealerOrderNumber?.toLowerCase().includes(searchLower) ||
        invoice.dealer?.dealerName?.toLowerCase().includes(searchLower) ||
        invoice.dealer?.dealerCode?.toLowerCase().includes(searchLower)
      );
    }

    // Status filter
    if (statusFilter !== 'all') {
      filtered = filtered.filter(invoice => invoice.status === statusFilter);
    }

    setFilteredInvoices(filtered);
  };

  const getStatusBadge = (status) => {
    // DealerInvoice status: lowercase, snake_case (theo DEALER_ORDER_API_FOR_FRONTEND.md)
    const statusMap = {
      'issued': { label: 'Đã phát hành', color: 'blue' },
      'partially_paid': { label: 'Đã thanh toán một phần', color: 'yellow' },
      'paid': { label: 'Đã thanh toán đủ', color: 'green' },
      'overdue': { label: 'Quá hạn', color: 'red' },
      'cancelled': { label: 'Đã hủy', color: 'gray' }
    };
    const config = statusMap[status] || { label: status, color: 'gray' };
    return (
      <span className={`status-badge status-${config.color}`}>
        {config.label}
      </span>
    );
  };

  const handleView = (invoice) => {
    navigate(`/admin/dealer-invoices/${invoice.invoiceId}`);
  };

  const columns = [
    {
      key: 'invoiceNumber',
      header: 'Số hóa đơn',
      renderCell: (item) => (
        <div className="invoice-number-cell">
          <strong>{item.invoiceNumber || 'N/A'}</strong>
        </div>
      )
    },
    {
      key: 'dealerOrderNumber',
      header: 'Đơn hàng',
      renderCell: (item) => (
        <div>
          <div className="font-medium">{item.dealerOrderNumber || 'N/A'}</div>
          {item.quotationNumber && (
            <div className="text-sm text-gray-500">Báo giá: {item.quotationNumber}</div>
          )}
        </div>
      )
    },
    {
      key: 'dealer',
      header: 'Đại lý',
      renderCell: (item) => {
        let dealer = item.dealer;
        if (typeof dealer === 'string') {
          try {
            dealer = JSON.parse(dealer);
          } catch (e) {
            dealer = null;
          }
        }
        const dealerName = dealer?.dealerName || dealer?.name || 'N/A';
        return <div className="font-medium">{dealerName}</div>;
      }
    },
    {
      key: 'invoiceDate',
      header: 'Ngày phát hành',
      renderCell: (item) => (
        <div>
          {item.invoiceDate ? new Date(item.invoiceDate).toLocaleDateString('vi-VN') : 'N/A'}
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
      key: 'dueDate',
      header: 'Hạn thanh toán',
      renderCell: (item) => (
        <div>
          {item.dueDate ? new Date(item.dueDate).toLocaleDateString('vi-VN') : 'N/A'}
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
        </div>
      )
    }
  ];

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="dealer-invoice-list">
      <div className="page-header">
        <div className="page-title">
          <FileText className="title-icon" />
          <h1>Danh sách hóa đơn đại lý</h1>
        </div>
      </div>

      {/* Filters */}
      <div className="filters-section">
        <div className="filter-group">
          <div className="search-input-wrapper">
            <Search size={18} className="search-icon" />
            <input
              type="text"
              placeholder="Tìm kiếm theo số hóa đơn, đơn hàng, đại lý..."
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
            <option value="issued">Đã phát hành</option>
            <option value="partially_paid">Đã thanh toán một phần</option>
            <option value="paid">Đã thanh toán đủ</option>
            <option value="overdue">Quá hạn</option>
            <option value="cancelled">Đã hủy</option>
          </select>
        </div>
      </div>

      {/* Data Table */}
      <DataTable
        data={filteredInvoices}
        columns={columns}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Tìm kiếm hóa đơn..."
        emptyMessage="Không có hóa đơn nào"
        onView={handleView}
        actions={false}
      />
    </div>
  );
};

export default DealerInvoiceList;

