import React, { useState, useEffect } from 'react';
import { FileText, Plus, DollarSign, Calendar, User } from 'lucide-react';
import { quotationAPI } from '../services/api';
import { getStatusBadge as getStatusBadgeUtil } from '../utils/statusBadges';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import QuotationModal from '../components/modals/QuotationModal';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './QuotationManagement.css';

const QuotationManagement = () => {
  const [quotations, setQuotations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Modal states
  const [selectedQuotation, setSelectedQuotation] = useState(null);
  const [showQuotationModal, setShowQuotationModal] = useState(false);
  const [modalMode, setModalMode] = useState('view'); // 'view' or 'edit'

  useEffect(() => {
    loadQuotations();
  }, []);

  const loadQuotations = async () => {
    try {
      setLoading(true);
      const response = await quotationAPI.getQuotations();
      setQuotations(response.data || []);
    } catch (error) {
      console.error('Error loading quotations:', error);
      toast.error('Không thể tải danh sách báo giá');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (quotation) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa báo giá "${quotation.quotationNumber}"?`)) {
      try {
        await quotationAPI.deleteQuotation(quotation.quotationId);
        toast.success('Xóa báo giá thành công');
        loadQuotations();
      } catch (error) {
        console.error('Error deleting quotation:', error);
        toast.error('Không thể xóa báo giá');
      }
    }
  };

  const handleEdit = async (quotation) => {
    try {
      setSelectedQuotation(quotation);
      setModalMode('edit');
      setShowQuotationModal(true);
    } catch (error) {
      console.error('Error getting quotation details:', error);
      toast.error('Không thể tải thông tin báo giá');
    }
  };

  const handleView = async (quotation) => {
    try {
      setSelectedQuotation(quotation);
      setModalMode('view');
      setShowQuotationModal(true);
    } catch (error) {
      console.error('Error getting quotation details:', error);
      toast.error('Không thể tải thông tin báo giá');
    }
  };

  const handleUpdateStatus = async (quotation, newStatus) => {
    try {
      await quotationAPI.updateQuotationStatus(quotation.quotationId, newStatus);
      toast.success(`Cập nhật trạng thái báo giá thành công`);
      loadQuotations();
    } catch (error) {
      console.error('Error updating quotation status:', error);
      toast.error('Không thể cập nhật trạng thái báo giá');
    }
  };

  const handleSendQuotation = async (quotation) => {
    try {
      await quotationAPI.sendQuotation(quotation.quotationId);
      toast.success('Gửi báo giá thành công');
      loadQuotations();
    } catch (error) {
      console.error('Error sending quotation:', error);
      toast.error('Không thể gửi báo giá');
    }
  };

  const handleConvertToOrder = async (quotation) => {
    if (window.confirm(`Bạn có chắc chắn muốn chuyển đổi báo giá "${quotation.quotationNumber}" thành đơn hàng?`)) {
      try {
        await quotationAPI.convertToOrder(quotation.quotationId);
        toast.success('Chuyển đổi thành đơn hàng thành công');
        loadQuotations();
      } catch (error) {
        console.error('Error converting quotation to order:', error);
        toast.error('Không thể chuyển đổi thành đơn hàng');
      }
    }
  };

  const handleExportPDF = async (quotation) => {
    try {
      const response = await quotationAPI.exportQuotationPDF(quotation.quotationId);
      // Create blob and download
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `quotation-${quotation.quotationNumber}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success('Xuất PDF thành công');
    } catch (error) {
      console.error('Error exporting PDF:', error);
      toast.error('Không thể xuất PDF');
    }
  };

  const getStatusBadge = (status) => {
    const statusInfo = getStatusBadgeUtil('quotation', status);
    return <span className={`badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const filteredQuotations = quotations.filter(quotation => {
    const searchLower = searchTerm.toLowerCase();
    return (
      quotation.quotationNumber?.toLowerCase().includes(searchLower) ||
      quotation.customer?.firstName?.toLowerCase().includes(searchLower) ||
      quotation.customer?.lastName?.toLowerCase().includes(searchLower) ||
      quotation.customer?.email?.toLowerCase().includes(searchLower) ||
      quotation.status?.toLowerCase().includes(searchLower) ||
      quotation.variant?.variantName?.toLowerCase().includes(searchLower) ||
      quotation.variant?.model?.modelName?.toLowerCase().includes(searchLower) ||
      quotation.variant?.model?.brand?.brandName?.toLowerCase().includes(searchLower)
    );
  });

  const columns = [
    { 
      key: 'quotationNumber', 
      header: 'Số báo giá',
      render: (quotation) => (
        <div className="quotation-number">
          <FileText size={16} />
          {quotation.quotationNumber}
        </div>
      )
    },
    { 
      key: 'customer', 
      header: 'Khách hàng',
      render: (quotation) => (
        <div className="customer-info">
          <User size={16} />
          <div>
            <div className="customer-name">
              {quotation.customer?.firstName || ''} {quotation.customer?.lastName || ''}
            </div>
            <div className="customer-email">{quotation.customer?.email || 'N/A'}</div>
          </div>
        </div>
      )
    },
    { 
      key: 'variant', 
      header: 'Xe',
      render: (quotation) => (
        <div className="vehicle-info">
          <div>
            <div className="vehicle-name">
              {quotation.variant?.model?.brand?.brandName || ''} {quotation.variant?.model?.modelName || ''}
            </div>
            <div className="vehicle-variant">{quotation.variant?.variantName || 'N/A'}</div>
          </div>
        </div>
      )
    },
    { 
      key: 'finalPrice', 
      header: 'Giá cuối cùng',
      render: (quotation) => (
        <div className="amount">
          <DollarSign size={16} />
          {quotation.finalPrice ? `${Number(quotation.finalPrice).toLocaleString('vi-VN')} VNĐ` : 'N/A'}
        </div>
      )
    },
    { 
      key: 'totalPrice', 
      header: 'Giá gốc',
      render: (quotation) => (
        <div className="amount">
          {quotation.totalPrice ? `${Number(quotation.totalPrice).toLocaleString('vi-VN')} VNĐ` : 'N/A'}
        </div>
      )
    },
    { 
      key: 'status', 
      header: 'Trạng thái',
      render: (quotation) => getStatusBadge(quotation.status)
    },
    { 
      key: 'quotationDate', 
      header: 'Ngày báo giá',
      render: (quotation) => (
        <div className="date">
          <Calendar size={16} />
          {quotation.quotationDate ? new Date(quotation.quotationDate).toLocaleDateString('vi-VN') : 'N/A'}
        </div>
      )
    },
    { 
      key: 'expiryDate', 
      header: 'Hết hạn',
      render: (quotation) => {
        // Calculate expiry date from quotationDate + validityDays
        if (quotation.quotationDate && quotation.validityDays) {
          const expiryDate = new Date(quotation.quotationDate);
          expiryDate.setDate(expiryDate.getDate() + quotation.validityDays);
          return (
            <div className="date">
              <Calendar size={16} />
              {expiryDate.toLocaleDateString('vi-VN')}
            </div>
          );
        }
        return (
          <div className="date">
            <Calendar size={16} />
            {quotation.expiryDate ? new Date(quotation.expiryDate).toLocaleDateString('vi-VN') : 'N/A'}
          </div>
        );
      }
    },
    { 
      key: 'createdAt', 
      header: 'Ngày tạo',
      render: (quotation) => quotation.createdAt ? new Date(quotation.createdAt).toLocaleDateString('vi-VN') : 'N/A'
    }
  ];

  const handleSaveQuotation = async (quotationId, quotationData) => {
    try {
      await quotationAPI.updateQuotation(quotationId, quotationData);
      toast.success('Cập nhật báo giá thành công');
      loadQuotations();
    } catch (error) {
      console.error('Error updating quotation:', error);
      toast.error('Không thể cập nhật báo giá');
      throw error;
    }
  };

  if (loading) {
    return <LoadingSpinner text="Đang tải danh sách báo giá..." />;
  }

  return (
    <div className="quotation-management">
      <div className="page-header">
        <div className="page-title">
          <FileText className="title-icon" />
          <h1>Quản lý báo giá từ khách</h1>
        </div>
        <p>Quản lý báo giá cho khách hàng</p>
      </div>

      <div className="content">
        <div className="section-header">
          <h2>Danh sách báo giá từ khách</h2>
          <button className="btn btn-primary">
            <Plus size={20} />
            Tạo báo giá mới
          </button>
        </div>

        <DataTable
          data={filteredQuotations}
          columns={columns}
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          searchPlaceholder="Tìm kiếm báo giá..."
          emptyMessage="Không có báo giá nào"
          onEdit={handleEdit}
          onDelete={handleDelete}
          onView={handleView}
          onUpdateStatus={handleUpdateStatus}
          onSendQuotation={handleSendQuotation}
          onConvertToOrder={handleConvertToOrder}
          onExportPDF={handleExportPDF}
        />

        {/* Quotation Modal */}
        <QuotationModal
          quotation={selectedQuotation}
          isOpen={showQuotationModal}
          mode={modalMode}
          onClose={() => {
            setShowQuotationModal(false);
            setSelectedQuotation(null);
            setModalMode('view');
          }}
          onSave={handleSaveQuotation}
        />
      </div>
    </div>
  );
};

export default QuotationManagement;
