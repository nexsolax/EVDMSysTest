import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  ShoppingCart, 
  Plus, 
  Search, 
  Eye, 
  CheckCircle, 
  XCircle,
  FileText,
  Filter,
  ArrowRight
} from 'lucide-react';
import { dealerOrderAPI, dealerAPI } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './DealerOrderList.css';

const DealerOrderList = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [dealerOrders, setDealerOrders] = useState([]);
  const [filteredOrders, setFilteredOrders] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [approvalFilter, setApprovalFilter] = useState('all');
  
  // Role checks
  const isEVMStaff = user?.role === 'evm_staff' || user?.role === 'admin';
  const isDealerManager = user?.role === 'dealer_manager' || user?.role === 'dealer_staff';
  const isAdmin = user?.role === 'admin';

  useEffect(() => {
    loadDealerOrders();
  }, []);

  useEffect(() => {
    filterOrders();
  }, [searchTerm, statusFilter, approvalFilter, dealerOrders]);

  const loadDealerOrders = async () => {
    try {
      setLoading(true);
      let response;
      
      // Dealer users chỉ thấy orders của mình
      if (isDealerManager && user?.dealerId) {
        console.log('Loading dealer orders for dealer:', user.dealerId);
        response = await dealerOrderAPI.getDealerOrdersByDealer(user.dealerId);
      } else {
        console.log('Loading all dealer orders...');
        response = await dealerOrderAPI.getDealerOrders();
      }
      
      console.log('Dealer orders response:', response);
      console.log('Dealer orders data:', response.data);
      
      // Normalize dealer data - ensure dealer is an object, not a JSON string
      const normalizedOrders = (response.data || []).map(order => {
        // Create a new object to avoid mutating the original
        const normalizedOrder = { ...order };
        
        // Handle dealer field - could be string, object, or null
        if (normalizedOrder.dealer) {
          if (typeof normalizedOrder.dealer === 'string') {
            try {
              normalizedOrder.dealer = JSON.parse(normalizedOrder.dealer);
            } catch (e) {
              console.warn('Failed to parse dealer JSON for order:', normalizedOrder.dealerOrderNumber, e);
              normalizedOrder.dealer = null;
            }
          }
          // If dealer is already an object, keep it as is
        }
        
        return normalizedOrder;
      });
      
      console.log('Normalized orders:', normalizedOrders);
      setDealerOrders(normalizedOrders);
    } catch (error) {
      console.error('Error loading dealer orders:', error);
      
      // Log detailed error information
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
        console.error('Response headers:', error.response.headers);
        
        // Show specific error message
        if (error.response.data && error.response.data.error) {
          toast.error(`Lỗi server: ${error.response.data.error}`);
        } else if (error.response.data && error.response.data.message) {
          toast.error(`Lỗi: ${error.response.data.message}`);
        } else if (error.response.status === 500) {
          toast.error('Lỗi server (500). Vui lòng kiểm tra backend logs hoặc liên hệ quản trị viên.');
        } else if (error.response.status === 401) {
          toast.error('Không có quyền truy cập. Vui lòng đăng nhập lại.');
        } else if (error.response.status === 403) {
          toast.error('Bạn không có quyền xem danh sách đơn hàng này.');
        } else {
          toast.error(`Không thể tải danh sách đơn hàng (${error.response.status})`);
        }
      } else if (error.request) {
        console.error('Request was made but no response received:', error.request);
        toast.error('Không có phản hồi từ server. Vui lòng kiểm tra kết nối.');
      } else {
        console.error('Error setting up request:', error.message);
        toast.error(`Lỗi: ${error.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const filterOrders = () => {
    let filtered = [...dealerOrders];

    // Search filter
    if (searchTerm.trim()) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(order =>
        order.dealerOrderNumber?.toLowerCase().includes(searchLower) ||
        order.dealer?.dealerName?.toLowerCase().includes(searchLower) ||
        order.dealer?.dealerCode?.toLowerCase().includes(searchLower)
      );
    }

    // Status filter
    if (statusFilter !== 'all') {
      filtered = filtered.filter(order => order.status === statusFilter);
    }

    // Approval filter
    if (approvalFilter !== 'all') {
      filtered = filtered.filter(order => order.approvalStatus === approvalFilter);
    }

    setFilteredOrders(filtered);
  };

  const getStatusBadge = (status) => {
    // DealerOrder status: UPPERCASE (theo DEALER_ORDER_API_FOR_FRONTEND.md)
    const statusMap = {
      'PENDING': { label: 'Chờ duyệt', color: 'yellow' },
      'APPROVED': { label: 'Đã duyệt', color: 'green' },
      'REJECTED': { label: 'Bị từ chối', color: 'red' },
      'CONFIRMED': { label: 'Đã xác nhận', color: 'blue' },
      'IN_PRODUCTION': { label: 'Đang sản xuất', color: 'blue' },
      'READY_FOR_DELIVERY': { label: 'Sẵn sàng giao', color: 'purple' },
      'DELIVERED': { label: 'Đã giao', color: 'green' },
      'CANCELLED': { label: 'Đã hủy', color: 'gray' }
    };
    const config = statusMap[status] || { label: status, color: 'gray' };
    return (
      <span className={`status-badge status-${config.color}`}>
        {config.label}
      </span>
    );
  };

  const getApprovalBadge = (approvalStatus) => {
    // ApprovalStatus: UPPERCASE (theo DEALER_ORDER_API_FOR_FRONTEND.md)
    const statusMap = {
      'PENDING': { label: 'Chờ duyệt', color: 'yellow' },
      'APPROVED': { label: 'Đã duyệt', color: 'green' },
      'REJECTED': { label: 'Bị từ chối', color: 'red' }
    };
    const config = statusMap[approvalStatus] || { label: approvalStatus, color: 'gray' };
    return (
      <span className={`status-badge status-${config.color}`}>
        {config.label}
      </span>
    );
  };

  const handleView = (order) => {
    navigate(`/admin/dealer-orders/${order.dealerOrderId}`);
  };

  const handleCreate = () => {
    navigate('/admin/dealer-orders/create');
  };

  const columns = [
    {
      key: 'dealerOrderNumber',
      header: 'Số đơn hàng',
      renderCell: (item) => (
        <div className="order-number-cell">
          <strong>{item.dealerOrderNumber || 'N/A'}</strong>
        </div>
      )
    },
    {
      key: 'dealer',
      header: 'Đại lý',
      renderCell: (item) => {
        // Handle case where dealer might be a JSON string
        let dealer = item.dealer;
        if (typeof dealer === 'string') {
          try {
            dealer = JSON.parse(dealer);
          } catch (e) {
            console.warn('Failed to parse dealer JSON:', e);
            dealer = null;
          }
        }
        
        // Chỉ hiển thị tên đại lý
        const dealerName = dealer?.dealerName || dealer?.name || 'N/A';
        
        return <div className="font-medium">{dealerName}</div>;
      }
    },
    {
      key: 'orderDate',
      header: 'Ngày đặt',
      renderCell: (item) => (
        <div>{item.orderDate ? new Date(item.orderDate).toLocaleDateString('vi-VN') : 'N/A'}</div>
      )
    },
    {
      key: 'totalQuantity',
      header: 'Số lượng',
      renderCell: (item) => (
        <div className="text-center">{item.totalQuantity || 0} xe</div>
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
      key: 'approvalStatus',
      header: 'Duyệt',
      renderCell: (item) => getApprovalBadge(item.approvalStatus)
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
    <div className="dealer-order-list">
      <div className="page-header">
        <div className="page-title">
          <ShoppingCart className="title-icon" />
          <h1>Danh sách đơn hàng đại lý</h1>
        </div>
        <div className="page-actions">
          <button onClick={handleCreate} className="btn btn-primary">
            <Plus size={18} /> Tạo đơn hàng mới
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
              placeholder="Tìm kiếm theo số đơn, đại lý..."
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
            <option value="PENDING">Chờ xử lý</option>
            <option value="APPROVED">Đã duyệt</option>
            <option value="PROCESSING">Đang xử lý</option>
            <option value="COMPLETED">Hoàn tất</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </div>

        <div className="filter-group">
          <select
            value={approvalFilter}
            onChange={(e) => setApprovalFilter(e.target.value)}
            className="form-select"
          >
            <option value="all">Tất cả phê duyệt</option>
            <option value="PENDING">Chờ duyệt</option>
            <option value="APPROVED">Đã duyệt</option>
            <option value="REJECTED">Đã từ chối</option>
          </select>
        </div>
      </div>

      {/* Data Table */}
      <DataTable
        data={filteredOrders}
        columns={columns}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Tìm kiếm đơn hàng..."
        emptyMessage="Không có đơn hàng nào"
      />
    </div>
  );
};

export default DealerOrderList;

