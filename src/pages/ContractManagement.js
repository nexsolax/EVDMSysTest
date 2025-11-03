import React, { useState, useEffect } from 'react';
import { FileText, Plus, DollarSign, Calendar, PenTool, User, Package } from 'lucide-react';
import { contractAPI } from '../services/api';
import { getStatusBadge as getStatusBadgeUtil } from '../utils/statusBadges';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ContractModal from '../components/modals/ContractModal';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './ContractManagement.css';

const ContractManagement = () => {
  const [contracts, setContracts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Modal states
  const [selectedContract, setSelectedContract] = useState(null);
  const [showContractModal, setShowContractModal] = useState(false);
  const [modalMode, setModalMode] = useState('view'); // 'view' or 'edit'

  useEffect(() => {
    loadContracts();
  }, []);

  const loadContracts = async () => {
    try {
      setLoading(true);
      const response = await contractAPI.getContracts();
      setContracts(response.data || []);
    } catch (error) {
      console.error('Error loading contracts:', error);
      toast.error('Không thể tải danh sách hợp đồng');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (contract) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa hợp đồng "${contract.contractNumber}"?`)) {
      try {
        await contractAPI.deleteContract(contract.contractId);
        toast.success('Xóa hợp đồng thành công');
        loadContracts();
      } catch (error) {
        console.error('Error deleting contract:', error);
        toast.error('Không thể xóa hợp đồng');
      }
    }
  };

  const handleEdit = async (contract) => {
    try {
      setSelectedContract(contract);
      setModalMode('edit');
      setShowContractModal(true);
    } catch (error) {
      console.error('Error getting contract details:', error);
      toast.error('Không thể tải thông tin hợp đồng');
    }
  };

  const handleView = async (contract) => {
    try {
      setSelectedContract(contract);
      setModalMode('view');
      setShowContractModal(true);
    } catch (error) {
      console.error('Error getting contract details:', error);
      toast.error('Không thể tải thông tin hợp đồng');
    }
  };


  const handleSignContract = async (contract) => {
    try {
      const signedDate = new Date().toISOString();
      await contractAPI.signContract(contract.contractId, signedDate);
      toast.success('Ký hợp đồng thành công');
      loadContracts();
    } catch (error) {
      console.error('Error signing contract:', error);
      toast.error('Không thể ký hợp đồng');
    }
  };

  const handleSendContract = async (contract) => {
    try {
      await contractAPI.sendContract(contract.contractId);
      toast.success('Gửi hợp đồng thành công');
      loadContracts();
    } catch (error) {
      console.error('Error sending contract:', error);
      toast.error('Không thể gửi hợp đồng');
    }
  };

  const handleExportPDF = async (contract) => {
    try {
      const response = await contractAPI.exportContractPDF(contract.contractId);
      // Create blob and download
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `contract-${contract.contractNumber}.pdf`;
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

  const handleTerminateContract = async (contract) => {
    if (window.confirm(`Bạn có chắc chắn muốn chấm dứt hợp đồng "${contract.contractNumber}"?`)) {
      try {
        await contractAPI.terminateContract(contract.contractId);
        toast.success('Chấm dứt hợp đồng thành công');
        loadContracts();
      } catch (error) {
        console.error('Error terminating contract:', error);
        toast.error('Không thể chấm dứt hợp đồng');
      }
    }
  };

  const getStatusBadge = (status) => {
    const statusInfo = getStatusBadgeUtil('contract', status);
    return <span className={`badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const filteredContracts = contracts.filter(contract => {
    const searchLower = searchTerm.toLowerCase();
    return (
      contract.contractNumber?.toLowerCase().includes(searchLower) ||
      contract.customer?.firstName?.toLowerCase().includes(searchLower) ||
      contract.customer?.lastName?.toLowerCase().includes(searchLower) ||
      contract.customer?.email?.toLowerCase().includes(searchLower) ||
      contract.status?.toLowerCase().includes(searchLower)
    );
  });

  const columns = [
    { 
      key: 'contractNumber', 
      header: 'Số hợp đồng',
      render: (contract) => (
        <div className="contract-number">
          <FileText size={16} />
          {contract.contractNumber}
        </div>
      )
    },
    { 
      key: 'customer', 
      header: 'Khách hàng',
      render: (contract) => (
        <div className="customer-info">
          <User size={16} />
          <div>
            <div className="customer-name">
              {contract.customer?.firstName} {contract.customer?.lastName}
            </div>
            <div className="customer-email">{contract.customer?.email}</div>
          </div>
        </div>
      )
    },
    { 
      key: 'vehicle', 
      header: 'Xe',
      render: (contract) => (
        <div className="vehicle-info">
          <Package size={16} />
          <div>
            <div className="vehicle-name">
              {contract.order?.inventory?.variant?.model?.brand?.brandName || contract.vehicle?.variant?.model?.brand?.brandName} {contract.order?.inventory?.variant?.model?.modelName || contract.vehicle?.variant?.model?.modelName}
            </div>
            <div className="vehicle-variant">{contract.order?.inventory?.variant?.variantName || contract.vehicle?.variant?.variantName}</div>
            {(contract.order?.inventory?.vin || contract.vehicle?.vin) && <div className="vehicle-vin">VIN: {contract.order?.inventory?.vin || contract.vehicle?.vin}</div>}
          </div>
        </div>
      )
    },
    { 
      key: 'totalAmount', 
      header: 'Tổng tiền',
      render: (contract) => (
        <div className="amount">
          <DollarSign size={16} />
          {contract.totalAmount ? `${contract.totalAmount.toLocaleString('vi-VN')} VNĐ` : 'N/A'}
        </div>
      )
    },
    { 
      key: 'status', 
      header: 'Trạng thái',
      render: (contract) => getStatusBadge(contract.status)
    },
    { 
      key: 'signedDate', 
      header: 'Ngày ký',
      render: (contract) => (
        <div className="date">
          <PenTool size={16} />
          {contract.signedDate ? new Date(contract.signedDate).toLocaleDateString('vi-VN') : 'Chưa ký'}
        </div>
      )
    },
    { 
      key: 'createdAt', 
      header: 'Ngày tạo',
      render: (contract) => (
        <div className="date">
          <Calendar size={16} />
          {contract.createdAt ? new Date(contract.createdAt).toLocaleDateString('vi-VN') : 'N/A'}
        </div>
      )
    }
  ];

  const handleSaveContract = async (contractId, contractData) => {
    try {
      await contractAPI.updateContract(contractId, contractData);
      toast.success('Cập nhật hợp đồng thành công');
      loadContracts();
    } catch (error) {
      console.error('Error updating contract:', error);
      toast.error('Không thể cập nhật hợp đồng');
      throw error;
    }
  };

  if (loading) {
    return <LoadingSpinner text="Đang tải danh sách hợp đồng..." />;
  }

  return (
    <div className="contract-management">
      <div className="page-header">
        <div className="page-title">
          <FileText className="title-icon" />
          <h1>Quản lý hợp đồng</h1>
        </div>
        <p>Quản lý hợp đồng mua bán xe</p>
      </div>

      <div className="content">
        <div className="section-header">
          <h2>Danh sách hợp đồng</h2>
          <button className="btn btn-primary">
            <Plus size={20} />
            Tạo hợp đồng mới
          </button>
        </div>

        <DataTable
          data={filteredContracts}
          columns={columns}
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          searchPlaceholder="Tìm kiếm hợp đồng..."
          emptyMessage="Không có hợp đồng nào"
          onEdit={handleEdit}
          onDelete={handleDelete}
          onView={handleView}
          onSignContract={handleSignContract}
          onSendContract={handleSendContract}
          onExportPDF={handleExportPDF}
          onTerminateContract={handleTerminateContract}
        />

        {/* Contract Modal */}
        <ContractModal
          contract={selectedContract}
          isOpen={showContractModal}
          mode={modalMode}
          onClose={() => {
            setShowContractModal(false);
            setSelectedContract(null);
            setModalMode('view');
          }}
          onSave={handleSaveContract}
        />
      </div>
    </div>
  );
};

export default ContractManagement;
