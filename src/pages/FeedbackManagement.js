import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { feedbackAPI, customerAPI, orderAPI } from '../services/api';
import './FeedbackManagement.css';

const FeedbackManagement = () => {
  const [feedbacks, setFeedbacks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [customers, setCustomers] = useState([]);
  const [orders, setOrders] = useState([]);
  const [filterRating, setFilterRating] = useState('all');

  useEffect(() => {
    loadData();
    loadReferenceData();
  }, [filterRating]);

  const loadData = async () => {
    try {
      setLoading(true);
      let response;
      
      if (filterRating !== 'all') {
        response = await feedbackAPI.getFeedbacksByRating(filterRating);
      } else {
        response = await feedbackAPI.getFeedbacks();
      }
      
      setFeedbacks(response.data || []);
    } catch (error) {
      console.error('Error loading feedbacks:', error);
      toast.error('Không thể tải danh sách phản hồi');
    } finally {
      setLoading(false);
    }
  };

  const loadReferenceData = async () => {
    try {
      const [customersRes, ordersRes] = await Promise.all([
        customerAPI.getCustomers(),
        orderAPI.getOrders()
      ]);
      
      setCustomers(customersRes.data || []);
      setOrders(ordersRes.data || []);
    } catch (error) {
      console.error('Error loading reference data:', error);
    }
  };

  const getCustomerName = (customerId) => {
    const customer = customers.find(c => c.customerId === customerId);
    return customer ? `${customer.firstName} ${customer.lastName}` : 'N/A';
  };

  const getOrderNumber = (orderId) => {
    const order = orders.find(o => o.orderId === orderId);
    return order ? order.orderNumber || order.orderId : 'N/A';
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  const getRatingStars = (rating) => {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(
        <span 
          key={i} 
          className={`star ${i <= rating ? 'filled' : ''}`}
        >
          ★
        </span>
      );
    }
    return <div className="rating-stars">{stars}</div>;
  };

  const getRatingBadge = (rating) => {
    const ratingMap = {
      5: { text: 'Xuất sắc', class: 'badge-success' },
      4: { text: 'Tốt', class: 'badge-primary' },
      3: { text: 'Trung bình', class: 'badge-warning' },
      2: { text: 'Kém', class: 'badge-danger' },
      1: { text: 'Rất kém', class: 'badge-danger' }
    };
    
    const ratingInfo = ratingMap[rating] || { text: 'N/A', class: 'badge-secondary' };
    return <span className={`badge ${ratingInfo.class}`}>{ratingInfo.text}</span>;
  };

  const getStatusBadge = (status) => {
    const statusMap = {
      'pending': { text: 'Chờ xử lý', class: 'badge-warning' },
      'reviewed': { text: 'Đã xem', class: 'badge-info' },
      'responded': { text: 'Đã phản hồi', class: 'badge-success' },
      'resolved': { text: 'Đã giải quyết', class: 'badge-primary' },
      'closed': { text: 'Đã đóng', class: 'badge-secondary' }
    };
    
    const statusInfo = statusMap[status] || { text: status, class: 'badge-secondary' };
    return <span className={`badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const handleView = async (feedback) => {
    try {
      const response = await feedbackAPI.getFeedback(feedback.feedbackId);
      const feedbackData = response.data;
      
      // Show feedback details in a modal or alert
      const details = `
        Khách hàng: ${getCustomerName(feedbackData.customer?.customerId)}
        Đơn hàng: ${getOrderNumber(feedbackData.order?.orderId)}
        Đánh giá: ${feedbackData.rating}/5 sao
        Tiêu đề: ${feedbackData.title || 'N/A'}
        Nội dung: ${feedbackData.content || 'N/A'}
        Ngày gửi: ${formatDate(feedbackData.feedbackDate)}
        Trạng thái: ${feedbackData.status || 'N/A'}
        Phản hồi: ${feedbackData.response || 'Chưa có phản hồi'}
        Ghi chú: ${feedbackData.notes || 'Không có'}
      `;
      
      alert(details);
    } catch (error) {
      console.error('Error loading feedback details:', error);
      toast.error('Không thể tải thông tin phản hồi');
    }
  };

  const handleEdit = async (feedback) => {
    try {
      const response = await feedbackAPI.getFeedback(feedback.feedbackId);
      const feedbackData = response.data;
      
      // Open edit modal with feedbackData
      const editData = {
        title: feedbackData.title,
        content: feedbackData.content,
        rating: feedbackData.rating,
        status: feedbackData.status,
        response: feedbackData.response,
        notes: feedbackData.notes
      };
      
      console.log('Edit feedback data:', editData);
      toast.info('Chức năng chỉnh sửa sẽ được implement trong modal');
    } catch (error) {
      console.error('Error loading feedback for edit:', error);
      toast.error('Không thể tải thông tin phản hồi');
    }
  };

  const handleDelete = async (feedback) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa phản hồi "${feedback.title}"?`)) {
      try {
        await feedbackAPI.deleteFeedback(feedback.feedbackId);
        toast.success('Xóa phản hồi thành công');
        loadData();
      } catch (error) {
        console.error('Error deleting feedback:', error);
        toast.error('Không thể xóa phản hồi');
      }
    }
  };

  const handleUpdateStatus = async (feedback, newStatus) => {
    try {
      await feedbackAPI.updateFeedbackStatus(feedback.feedbackId, newStatus);
      toast.success('Cập nhật trạng thái thành công');
      loadData();
    } catch (error) {
      console.error('Error updating feedback status:', error);
      toast.error('Không thể cập nhật trạng thái');
    }
  };

  const columns = [
    {
      key: 'title',
      label: 'Tiêu đề',
      render: (value) => <strong>{value || 'N/A'}</strong>
    },
    {
      key: 'customer',
      label: 'Khách hàng',
      render: (value, row) => getCustomerName(row?.customer?.customerId)
    },
    {
      key: 'order',
      label: 'Đơn hàng',
      render: (value, row) => getOrderNumber(row?.order?.orderId)
    },
    {
      key: 'rating',
      label: 'Đánh giá',
      render: (value) => (
        <div className="rating-cell">
          {getRatingStars(value)}
          <span className="rating-text">({value}/5)</span>
        </div>
      )
    },
    {
      key: 'content',
      label: 'Nội dung',
      render: (value) => (
        <div className="content-preview">
          {value ? (value.length > 50 ? `${value.substring(0, 50)}...` : value) : 'N/A'}
        </div>
      )
    },
    {
      key: 'feedbackDate',
      label: 'Ngày gửi',
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
      label: 'Đã xem',
      value: 'reviewed',
      className: 'btn-info'
    },
    {
      label: 'Đã phản hồi',
      value: 'responded',
      className: 'btn-success'
    },
    {
      label: 'Đã giải quyết',
      value: 'resolved',
      className: 'btn-primary'
    },
    {
      label: 'Đóng',
      value: 'closed',
      className: 'btn-secondary'
    }
  ];

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="feedback-management">
      <div className="page-header">
        <h1>Quản lý phản hồi khách hàng</h1>
        <div className="header-actions">
          <button 
            className="btn btn-primary"
            onClick={() => toast.info('Chức năng tạo phản hồi mới sẽ được implement')}
          >
            <i className="fas fa-plus"></i> Tạo phản hồi mới
          </button>
        </div>
      </div>

      <div className="filter-section">
        <div className="filter-tabs">
          <button 
            className={`filter-tab ${filterRating === 'all' ? 'active' : ''}`}
            onClick={() => setFilterRating('all')}
          >
            Tất cả
          </button>
          <button 
            className={`filter-tab ${filterRating === '5' ? 'active' : ''}`}
            onClick={() => setFilterRating('5')}
          >
            ⭐⭐⭐⭐⭐ (5 sao)
          </button>
          <button 
            className={`filter-tab ${filterRating === '4' ? 'active' : ''}`}
            onClick={() => setFilterRating('4')}
          >
            ⭐⭐⭐⭐ (4 sao)
          </button>
          <button 
            className={`filter-tab ${filterRating === '3' ? 'active' : ''}`}
            onClick={() => setFilterRating('3')}
          >
            ⭐⭐⭐ (3 sao)
          </button>
          <button 
            className={`filter-tab ${filterRating === '2' ? 'active' : ''}`}
            onClick={() => setFilterRating('2')}
          >
            ⭐⭐ (2 sao)
          </button>
          <button 
            className={`filter-tab ${filterRating === '1' ? 'active' : ''}`}
            onClick={() => setFilterRating('1')}
          >
            ⭐ (1 sao)
          </button>
        </div>
      </div>

      <div className="data-section">
        <DataTable
          data={feedbacks}
          columns={columns}
          actions={actions}
          statusActions={statusActions}
          onStatusUpdate={handleUpdateStatus}
          searchable={true}
          searchPlaceholder="Tìm kiếm phản hồi..."
        />
      </div>
    </div>
  );
};

export default FeedbackManagement;