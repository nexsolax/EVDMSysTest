import React, { useState, useEffect } from 'react';
import { Users, Plus, Phone, Mail, MapPin } from 'lucide-react';
import { customerAPI } from '../services/api';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import CustomerModal from '../components/modals/CustomerModal';
import toast from 'react-hot-toast';
import './CustomerManagement.css';

const CustomerManagement = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [showCustomerModal, setShowCustomerModal] = useState(false);
  const [modalMode, setModalMode] = useState('view'); // 'view' or 'edit'

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    try {
      setLoading(true);
      const response = await customerAPI.getCustomers();
      setCustomers(response.data || []);
    } catch (error) {
      console.error('Error loading customers:', error);
      toast.error('Không thể tải danh sách khách hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (customer) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa khách hàng "${customer.firstName} ${customer.lastName}"?`)) {
      try {
        await customerAPI.deleteCustomer(customer.customerId);
        toast.success('Xóa khách hàng thành công');
        loadCustomers();
      } catch (error) {
        console.error('Error deleting customer:', error);
        toast.error('Không thể xóa khách hàng');
      }
    }
  };

  const handleEdit = (customer) => {
    setSelectedCustomer(customer);
    setModalMode('edit');
    setShowCustomerModal(true);
  };

  const handleView = (customer) => {
    setSelectedCustomer(customer);
    setModalMode('view');
    setShowCustomerModal(true);
  };

  const handleSaveCustomer = async (customerId, customerData) => {
    try {
      await customerAPI.updateCustomer(customerId, customerData);
      toast.success('Cập nhật khách hàng thành công');
      loadCustomers();
    } catch (error) {
      console.error('Error updating customer:', error);
      toast.error('Không thể cập nhật khách hàng');
      throw error;
    }
  };


  const filteredCustomers = customers.filter(customer => {
    const searchLower = searchTerm.toLowerCase();
    return (
      customer.firstName?.toLowerCase().includes(searchLower) ||
      customer.lastName?.toLowerCase().includes(searchLower) ||
      customer.email?.toLowerCase().includes(searchLower) ||
      customer.phone?.toLowerCase().includes(searchLower) ||
      customer.city?.toLowerCase().includes(searchLower)
    );
  });

  const columns = [
    { 
      key: 'name', 
      header: 'Họ tên',
      render: (customer) => (
        <div className="customer-name">
          <div className="customer-avatar">
            {customer.firstName?.charAt(0)}{customer.lastName?.charAt(0)}
          </div>
          <div>
            <div className="customer-fullname">
              {customer.firstName} {customer.lastName}
            </div>
            <div className="customer-id">ID: {customer.customerId}</div>
          </div>
        </div>
      )
    },
    { 
      key: 'email', 
      header: 'Email',
      render: (customer) => (
        <div className="contact-info">
          <Mail size={16} />
          {customer.email || 'N/A'}
        </div>
      )
    },
    { 
      key: 'phone', 
      header: 'Số điện thoại',
      render: (customer) => (
        <div className="contact-info">
          <Phone size={16} />
          {customer.phone || 'N/A'}
        </div>
      )
    },
    { 
      key: 'city', 
      header: 'Thành phố',
      render: (customer) => (
        <div className="contact-info">
          <MapPin size={16} />
          {customer.city || 'N/A'}
        </div>
      )
    },
    { 
      key: 'creditScore', 
      header: 'Điểm tín dụng',
      render: (customer) => (
        <span className={`credit-score ${customer.creditScore >= 700 ? 'good' : customer.creditScore >= 600 ? 'fair' : 'poor'}`}>
          {customer.creditScore || 'N/A'}
        </span>
      )
    },
    { 
      key: 'createdAt', 
      header: 'Ngày tạo',
      render: (customer) => customer.createdAt ? new Date(customer.createdAt).toLocaleDateString('vi-VN') : 'N/A'
    }
  ];

  if (loading) {
    return <LoadingSpinner text="Đang tải danh sách khách hàng..." />;
  }

  return (
    <div className="customer-management">
      <div className="page-header">
        <div className="page-title">
          <Users className="title-icon" />
          <h1>Quản lý khách hàng</h1>
        </div>
        <p>Quản lý thông tin khách hàng và lịch sử giao dịch</p>
      </div>

      <div className="content">
        <div className="section-header">
          <h2>Danh sách khách hàng</h2>
          <button className="btn btn-primary">
            <Plus size={20} />
            Thêm khách hàng
          </button>
        </div>

        <DataTable
          data={filteredCustomers}
          columns={columns}
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          searchPlaceholder="Tìm kiếm khách hàng..."
          emptyMessage="Không có khách hàng nào"
          onEdit={handleEdit}
          onDelete={handleDelete}
          onView={handleView}
        />

        {/* Customer Modal */}
        <CustomerModal
          customer={selectedCustomer}
          isOpen={showCustomerModal}
          mode={modalMode}
          onClose={() => {
            setShowCustomerModal(false);
            setSelectedCustomer(null);
            setModalMode('view');
          }}
          onSave={handleSaveCustomer}
        />
      </div>
    </div>
  );
};

export default CustomerManagement;
