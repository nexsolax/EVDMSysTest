import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Package, Plus, Building2, Car } from 'lucide-react';
import { inventoryAPI, warehouseAPI } from '../services/api';
import { getStatusBadge as getStatusBadgeUtil, getActiveBadge } from '../utils/statusBadges';
import DataTable from '../components/common/DataTable';
import WarehouseModal from '../components/modals/WarehouseModal';
import InventoryItemModal from '../components/modals/InventoryItemModal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import './InventoryManagement.css';

const InventoryManagement = () => {
  const location = useLocation();
  const [activeTab, setActiveTab] = useState('warehouses');
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedWarehouse, setSelectedWarehouse] = useState(null);
  const [selectedInventoryItem, setSelectedInventoryItem] = useState(null);
  const [showWarehouseModal, setShowWarehouseModal] = useState(false);
  const [showInventoryModal, setShowInventoryModal] = useState(false);
  const [modalMode, setModalMode] = useState('view');

  // Data states
  const [warehouses, setWarehouses] = useState([]);
  const [inventory, setInventory] = useState([]);

  const tabs = [
    { id: 'warehouses', label: 'Kho', path: '/inventory/warehouses', icon: <Building2 size={20} /> },
    { id: 'vehicles', label: 'Tồn kho xe', path: '/inventory/vehicles', icon: <Car size={20} /> }
  ];

  const getCurrentTab = () => {
    const currentPath = location.pathname;
    if (currentPath === '/inventory') {
      return 'warehouses';
    }
    const tab = tabs.find(t => t.path === currentPath);
    return tab ? tab.id : 'warehouses';
  };

  useEffect(() => {
    const newTab = getCurrentTab();
    setActiveTab(newTab);
  }, [location.pathname]);

  useEffect(() => {
    loadData();
  }, [activeTab]);

  const loadData = async () => {
    try {
      setLoading(true);
      switch (activeTab) {
        case 'warehouses':
          const warehousesResponse = await warehouseAPI.getWarehouses();
          setWarehouses(warehousesResponse.data || []);
          break;
        case 'vehicles':
          const inventoryResponse = await inventoryAPI.getInventory();
          setInventory(inventoryResponse.data || []);
          break;
        default:
          break;
      }
    } catch (error) {
      console.error(`Error loading ${activeTab}:`, error);
      toast.error(`Không thể tải dữ liệu ${getTabTitle()}`);
    } finally {
      setLoading(false);
    }
  };

  const getTabTitle = () => {
    switch (activeTab) {
      case 'warehouses': return 'kho';
      case 'vehicles': return 'tồn kho xe';
      default: return 'dữ liệu';
    }
  };

  const handleDelete = async (item, type) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa ${getTabTitle()} này?`)) {
      try {
        switch (type) {
          case 'warehouse':
            await warehouseAPI.deleteWarehouse(item.warehouseId);
            break;
          case 'inventory':
            await inventoryAPI.deleteInventory(item.inventoryId);
            break;
        }
        toast.success(`Xóa ${getTabTitle()} thành công`);
        loadData();
      } catch (error) {
        console.error(`Error deleting ${type}:`, error);
        toast.error(`Không thể xóa ${getTabTitle()}`);
      }
    }
  };

  const handleEdit = async (item) => {
    try {
      switch (activeTab) {
        case 'warehouses':
          setSelectedWarehouse(item);
          setModalMode('edit');
          setShowWarehouseModal(true);
          break;
        case 'vehicles':
          setSelectedInventoryItem(item);
          setModalMode('edit');
          setShowInventoryModal(true);
          break;
      }
      
    } catch (error) {
      console.error(`Error getting ${activeTab} details:`, error);
      toast.error(`Không thể tải thông tin ${getTabTitle()}`);
    }
  };

  const handleView = async (item) => {
    try {
      switch (activeTab) {
        case 'warehouses':
          setSelectedWarehouse(item);
          setModalMode('view');
          setShowWarehouseModal(true);
          break;
        case 'vehicles':
          setSelectedInventoryItem(item);
          setModalMode('view');
          setShowInventoryModal(true);
          break;
      }
      
    } catch (error) {
      console.error(`Error getting ${activeTab} details:`, error);
      toast.error(`Không thể tải thông tin ${getTabTitle()}`);
    }
  };

  const handleSaveWarehouse = async (warehouseId, data) => {
    try {
      await warehouseAPI.updateWarehouse(warehouseId, data);
      toast.success('Cập nhật kho thành công');
      loadData();
    } catch (error) {
      console.error('Error updating warehouse:', error);
      toast.error('Không thể cập nhật kho');
      throw error;
    }
  };

  const handleSaveInventoryItem = async (inventoryId, data) => {
    try {
      await inventoryAPI.updateInventory(inventoryId, data);
      toast.success('Cập nhật tồn kho thành công');
      loadData();
    } catch (error) {
      console.error('Error updating inventory item:', error);
      toast.error('Không thể cập nhật tồn kho');
      throw error;
    }
  };


  const getColumns = () => {
    switch (activeTab) {
      case 'warehouses':
        return [
          { key: 'warehouseName', header: 'Tên kho' },
          { key: 'address', header: 'Địa chỉ' },
          { key: 'city', header: 'Thành phố' },
          { key: 'capacity', header: 'Sức chứa' },
          { 
            key: 'isActive', 
            header: 'Trạng thái',
            render: (item) => {
              const badge = getActiveBadge(item.isActive);
              return (
                <span className={`badge ${badge.class}`}>
                  {badge.text}
                </span>
              );
            }
          }
        ];
      case 'vehicles':
        return [
          { 
            key: 'vehicle', 
            header: 'Xe',
            render: (item) => (
              <div>
                <div className="font-medium">
                  {item.vehicle?.brand?.brandName} {item.vehicle?.model?.modelName}
                </div>
                <div className="text-sm text-gray-500">
                  {item.vehicle?.variant?.variantName}
                </div>
              </div>
            )
          },
          { 
            key: 'color', 
            header: 'Màu sắc',
            render: (item) => (
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <div 
                  style={{ 
                    width: '20px', 
                    height: '20px', 
                    backgroundColor: item.color?.hexCode,
                    border: '1px solid #e5e7eb',
                    borderRadius: '4px'
                  }}
                />
                {item.color?.colorName}
              </div>
            )
          },
          { key: 'vin', header: 'VIN' },
          { 
            key: 'warehouse', 
            header: 'Kho',
            render: (item) => (
              <div>
                <div className="font-medium">{item.warehouse?.warehouseName || 'N/A'}</div>
                {item.warehouse?.warehouseCode && (
                  <div className="text-sm text-gray-500">{item.warehouse.warehouseCode}</div>
                )}
              </div>
            )
          },
          { 
            key: 'status', 
            header: 'Trạng thái',
            render: (item) => {
              const statusInfo = getStatusBadgeUtil('inventory', item.status);
              return (
                <span className={`badge ${statusInfo.class}`}>
                  {statusInfo.text}
                </span>
              );
            }
          }
        ];
      default:
        return [];
    }
  };

  const getCurrentData = () => {
    switch (activeTab) {
      case 'warehouses': return warehouses;
      case 'vehicles': return inventory;
      default: return [];
    }
  };

  const currentData = getCurrentData();
  
  const filteredData = currentData.filter(item => {
    const searchLower = searchTerm.toLowerCase();
    return (
      item.warehouseName?.toLowerCase().includes(searchLower) ||
      item.address?.toLowerCase().includes(searchLower) ||
      item.city?.toLowerCase().includes(searchLower) ||
      item.vin?.toLowerCase().includes(searchLower) ||
      (item.vehicle?.brand?.brandName?.toLowerCase() || '').includes(searchLower) ||
      (item.vehicle?.model?.modelName?.toLowerCase() || '').includes(searchLower) ||
      (item.color?.colorName?.toLowerCase() || '').includes(searchLower)
    );
  });

  const renderContent = () => {
    if (loading) {
      return <LoadingSpinner text={`Đang tải danh sách ${getTabTitle()}...`} />;
    }

    return (
      <>
        <div className="section-header">
          <h2>Quản lý {getTabTitle()}</h2>
          <button className="btn btn-primary">
            <Plus size={20} />
            Thêm {getTabTitle()}
          </button>
        </div>
        
        <DataTable
          data={filteredData}
          columns={getColumns()}
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          searchPlaceholder={`Tìm kiếm ${getTabTitle()}...`}
          emptyMessage={`Không có ${getTabTitle()} nào`}
          onEdit={handleEdit}
          onDelete={(item) => handleDelete(item, activeTab.slice(0, -1))}
          onView={handleView}
          onReserveVehicle={activeTab === 'vehicles' ? async (item) => {
            try {
              await inventoryAPI.reserveVehicle(item.inventoryId);
              toast.success('Đặt trước xe thành công');
              loadData();
            } catch (error) {
              console.error('Error reserving vehicle:', error);
              toast.error('Không thể đặt trước xe');
            }
          } : undefined}
          onReleaseVehicle={activeTab === 'vehicles' ? async (item) => {
            try {
              await inventoryAPI.releaseVehicle(item.inventoryId);
              toast.success('Giải phóng xe thành công');
              loadData();
            } catch (error) {
              console.error('Error releasing vehicle:', error);
              toast.error('Không thể giải phóng xe');
            }
          } : undefined}
          onGetVehicleHistory={activeTab === 'vehicles' ? async (item) => {
            try {
              const response = await inventoryAPI.getVehicleHistory(item.inventoryId);
              console.log('Vehicle history:', response.data);
              toast('Đã tải lịch sử xe', { icon: '📜' });
            } catch (error) {
              console.error('Error getting vehicle history:', error);
              toast.error('Không thể tải lịch sử xe');
            }
          } : undefined}
          onViewInventory={activeTab === 'warehouses' ? async (item) => {
            try {
              const response = await warehouseAPI.getWarehouseInventory(item.warehouseId);
              console.log('Warehouse inventory:', response.data);
              toast('Đã tải hàng tồn kho của kho', { icon: '📦' });
            } catch (error) {
              console.error('Error loading warehouse inventory:', error);
              toast.error('Không thể tải hàng tồn kho của kho');
            }
          } : undefined}
          onTransferWarehouse={activeTab === 'warehouses' ? async (item) => {
            try {
              // For demo: prompt minimal payload info
              const toWarehouseId = window.prompt('Nhập ID kho đích:');
              if (!toWarehouseId) return;
              await warehouseAPI.transferWarehouse(item.warehouseId, { toWarehouseId });
              toast.success('Chuyển kho thành công');
              loadData();
            } catch (error) {
              console.error('Error transferring warehouse:', error);
              toast.error('Không thể chuyển kho');
            }
          } : undefined}
        />

        {/* Warehouse Modal */}
        <WarehouseModal
          warehouse={selectedWarehouse}
          isOpen={showWarehouseModal}
          mode={modalMode}
          onClose={() => {
            setShowWarehouseModal(false);
            setSelectedWarehouse(null);
            setModalMode('view');
          }}
          onSave={handleSaveWarehouse}
        />

        {/* Inventory Item Modal */}
        <InventoryItemModal
          item={selectedInventoryItem}
          isOpen={showInventoryModal}
          mode={modalMode}
          onClose={() => {
            setShowInventoryModal(false);
            setSelectedInventoryItem(null);
            setModalMode('view');
          }}
          onSave={handleSaveInventoryItem}
        />
      </>
    );
  };

  return (
    <div className="inventory-management">
      <div className="page-header">
        <div className="page-title">
          <Package className="title-icon" />
          <h1>Quản lý kho</h1>
        </div>
        <p>Quản lý kho và tồn kho xe</p>
      </div>

      <div className="content">
        <div className="tabs-container">
          <div className="tabs">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                className={`tab ${activeTab === tab.id ? 'active' : ''}`}
                onClick={() => setActiveTab(tab.id)}
              >
                {tab.icon}
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {renderContent()}
      </div>
    </div>
  );
};

export default InventoryManagement;