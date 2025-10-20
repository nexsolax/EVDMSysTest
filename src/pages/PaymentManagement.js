import React, { useState, useEffect } from 'react';
import { CreditCard, Plus } from 'lucide-react';
import { paymentAPI } from '../services/api';
import { getStatusBadge as getStatusBadgeUtil } from '../utils/statusBadges';
import DataTable from '../components/common/DataTable';
import PaymentModal from '../components/modals/PaymentModal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import './PaymentManagement.css';

const PaymentManagement = () => {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [modalMode, setModalMode] = useState('view');

  useEffect(() => {
    loadPayments();
  }, []);

  const loadPayments = async () => {
    try {
      setLoading(true);
      const response = await paymentAPI.getPayments();
      setPayments(response.data || []);
    } catch (error) {
      console.error('Error loading payments:', error);
      toast.error('Không thể tải danh sách thanh toán');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (payment) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa thanh toán "${payment.paymentNumber}"?`)) {
      try {
        await paymentAPI.deletePayment(payment.paymentId);
        toast.success('Xóa thanh toán thành công');
        loadPayments();
      } catch (error) {
        console.error('Error deleting payment:', error);
        toast.error('Không thể xóa thanh toán');
      }
    }
  };

  const handleEdit = async (payment) => {
    try {
      setSelectedPayment(payment);
      setModalMode('edit');
      setShowPaymentModal(true);
    } catch (error) {
      console.error('Error getting payment details:', error);
      toast.error('Không thể tải thông tin thanh toán');
    }
  };

  const handleView = async (payment) => {
    try {
      setSelectedPayment(payment);
      setModalMode('view');
      setShowPaymentModal(true);
    } catch (error) {
      console.error('Error getting payment details:', error);
      toast.error('Không thể tải thông tin thanh toán');
    }
  };

  const handleSavePayment = async (paymentId, paymentData) => {
    try {
      await paymentAPI.updatePayment(paymentId, paymentData);
      toast.success('Cập nhật thanh toán thành công');
      loadPayments();
    } catch (error) {
      console.error('Error updating payment:', error);
      toast.error('Không thể cập nhật thanh toán');
      throw error;
    }
  };

  const handleUpdateStatus = async (payment) => {
    const statusOptions = [
      { value: 'PENDING', label: 'Chờ xử lý' },
      { value: 'PROCESSING', label: 'Đang xử lý' },
      { value: 'COMPLETED', label: 'Hoàn thành' },
      { value: 'FAILED', label: 'Thất bại' },
      { value: 'CANCELLED', label: 'Đã hủy' },
      { value: 'REFUNDED', label: 'Đã hoàn tiền' }
    ];

    const currentStatus = statusOptions.find(s => s.value === payment.status);
    const availableStatuses = statusOptions.filter(s => s.value !== payment.status);

    if (availableStatuses.length === 0) {
      toast('Thanh toán đã ở trạng thái cuối cùng', { icon: 'ℹ️' });
      return;
    }

    const newStatus = window.prompt(
      `Cập nhật trạng thái thanh toán "${payment.paymentNumber}"\n\n` +
      `Trạng thái hiện tại: ${currentStatus?.label || payment.status}\n\n` +
      `Chọn trạng thái mới:\n` +
      availableStatuses.map((s, i) => `${i + 1}. ${s.label}`).join('\n') +
      `\n\nNhập số thứ tự (1-${availableStatuses.length}):`
    );

    if (newStatus === null) return; // User cancelled

    const statusIndex = parseInt(newStatus) - 1;
    if (isNaN(statusIndex) || statusIndex < 0 || statusIndex >= availableStatuses.length) {
      toast.error('Lựa chọn không hợp lệ');
      return;
    }

    const selectedStatus = availableStatuses[statusIndex].value;

    try {
      await paymentAPI.updatePaymentStatus(payment.paymentId, selectedStatus);
      toast.success('Cập nhật trạng thái thanh toán thành công');
      loadPayments();
    } catch (error) {
      console.error('Error updating payment status:', error);
      toast.error('Không thể cập nhật trạng thái thanh toán');
    }
  };

  const handleProcess = async (payment) => {
    try {
      await paymentAPI.processPayment(payment.paymentId);
      toast.success('Xử lý thanh toán thành công');
      loadPayments();
    } catch (error) {
      console.error('Error processing payment:', error);
      toast.error('Không thể xử lý thanh toán');
    }
  };

  const handleRefund = async (payment) => {
    if (!window.confirm(`Hoàn tiền cho thanh toán "${payment.paymentNumber}"?`)) return;
    try {
      await paymentAPI.refundPayment(payment.paymentId);
      toast.success('Hoàn tiền thành công');
      loadPayments();
    } catch (error) {
      console.error('Error refunding payment:', error);
      toast.error('Không thể hoàn tiền');
    }
  };

  const handleReceipt = async (payment) => {
    try {
      const response = await paymentAPI.exportPaymentReceipt(payment.paymentId);
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `receipt-${payment.paymentNumber}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success('Xuất hóa đơn thành công');
    } catch (error) {
      console.error('Error exporting receipt:', error);
      toast.error('Không thể xuất hóa đơn');
    }
  };

  const getStatusBadge = (status) => {
    return getStatusBadgeUtil('payment', status);
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  const filteredPayments = payments.filter(payment => {
    const searchLower = searchTerm.toLowerCase();
    return (
      payment.paymentNumber?.toLowerCase().includes(searchLower) ||
      payment.customer?.firstName?.toLowerCase().includes(searchLower) ||
      payment.customer?.lastName?.toLowerCase().includes(searchLower) ||
      payment.order?.orderNumber?.toLowerCase().includes(searchLower) ||
      payment.paymentMethod?.toLowerCase().includes(searchLower)
    );
  });

  const columns = [
    { 
      key: 'paymentNumber', 
      header: 'Mã thanh toán',
      render: (item) => (
        <span className="font-mono font-semibold">#{item.paymentNumber}</span>
      )
    },
    { 
      key: 'customer', 
      header: 'Khách hàng',
      render: (item) => (
        <div>
          <div className="font-medium">
            {item.customer?.firstName} {item.customer?.lastName}
          </div>
          <div className="text-sm text-gray-500">{item.customer?.email}</div>
        </div>
      )
    },
    { 
      key: 'order', 
      header: 'Đơn hàng',
      render: (item) => (
        <span className="font-mono">#{item.order?.orderNumber}</span>
      )
    },
    { 
      key: 'amount', 
      header: 'Số tiền',
      render: (item) => (
        <span className="font-semibold text-green-600">
          {formatCurrency(item.amount)}
        </span>
      )
    },
    { key: 'paymentMethod', header: 'Phương thức' },
    { 
      key: 'status', 
      header: 'Trạng thái',
      render: (item) => {
        const statusInfo = getStatusBadge(item.status);
        return (
          <span className={`badge ${statusInfo.class}`}>
            {statusInfo.text}
          </span>
        );
      }
    },
    { 
      key: 'paymentDate', 
      header: 'Ngày thanh toán',
      render: (item) => formatDate(item.paymentDate)
    }
  ];

  if (loading) {
    return <LoadingSpinner text="Đang tải danh sách thanh toán..." />;
  }

  return (
    <div className="payment-management">
      <div className="page-header">
        <div className="page-title">
          <CreditCard className="title-icon" />
          <h1>Quản lý thanh toán</h1>
        </div>
        <p>Quản lý thanh toán của khách hàng</p>
      </div>

      <div className="content">
        <div className="section-header">
          <h2>Danh sách thanh toán</h2>
          <button className="btn btn-primary">
            <Plus size={20} />
            Thêm thanh toán
          </button>
        </div>

        <DataTable
          data={filteredPayments}
          columns={columns}
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          searchPlaceholder="Tìm kiếm thanh toán..."
          emptyMessage="Không có thanh toán nào"
          onEdit={handleEdit}
          onDelete={handleDelete}
          onView={handleView}
          onUpdateStatus={handleUpdateStatus}
          onProcessPayment={handleProcess}
          onRefundPayment={handleRefund}
          onExportReceipt={handleReceipt}
        />

        {/* Payment Modal */}
        <PaymentModal
          payment={selectedPayment}
          isOpen={showPaymentModal}
          mode={modalMode}
          onClose={() => {
            setShowPaymentModal(false);
            setSelectedPayment(null);
            setModalMode('view');
          }}
          onSave={handleSavePayment}
        />
      </div>
    </div>
  );
};

export default PaymentManagement;