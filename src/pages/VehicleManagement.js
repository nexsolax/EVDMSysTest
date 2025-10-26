import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Car, Plus, Search, Filter, Package, Palette, Settings } from 'lucide-react';
import { vehicleAPI } from '../services/api';
import { getActiveBadge } from '../utils/statusBadges';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import VehicleBrandModal from '../components/modals/VehicleBrandModal';
import VehicleModelModal from '../components/modals/VehicleModelModal';
import VehicleVariantModal from '../components/modals/VehicleVariantModal';
import VehicleColorModal from '../components/modals/VehicleColorModal';
import toast from 'react-hot-toast';
import './VehicleManagement.css';

const VehicleManagement = () => {
  const location = useLocation();
  const [activeTab, setActiveTab] = useState('brands');
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Data states
  const [brands, setBrands] = useState([]);
  const [models, setModels] = useState([]);
  const [variants, setVariants] = useState([]);
  const [colors, setColors] = useState([]);
  
  // Modal states
  const [selectedBrand, setSelectedBrand] = useState(null);
  const [selectedModel, setSelectedModel] = useState(null);
  const [selectedVariant, setSelectedVariant] = useState(null);
  const [selectedColor, setSelectedColor] = useState(null);
  const [showBrandModal, setShowBrandModal] = useState(false);
  const [showModelModal, setShowModelModal] = useState(false);
  const [showVariantModal, setShowVariantModal] = useState(false);
  const [showColorModal, setShowColorModal] = useState(false);
  const [modalMode, setModalMode] = useState('view'); // 'view' or 'edit'

  const tabs = [
    { id: 'brands', label: 'Thương hiệu', path: '/vehicles/brands', icon: <Car size={20} /> },
    { id: 'models', label: 'Dòng xe', path: '/vehicles/models', icon: <Package size={20} /> },
    { id: 'variants', label: 'Phiên bản', path: '/vehicles/variants', icon: <Settings size={20} /> },
    { id: 'colors', label: 'Màu sắc', path: '/vehicles/colors', icon: <Palette size={20} /> }
  ];

  const getCurrentTab = () => {
    const currentPath = location.pathname;
    console.log('Current path:', currentPath);
    
    // If path is just /vehicles, default to brands
    if (currentPath === '/vehicles') {
      console.log('Defaulting to brands tab');
      return 'brands';
    }
    
    const tab = tabs.find(t => t.path === currentPath);
    console.log('Found tab:', tab);
    return tab ? tab.id : 'brands';
  };

  useEffect(() => {
    const newTab = getCurrentTab();
    console.log('Setting activeTab to:', newTab);
    setActiveTab(newTab);
  }, [location.pathname]);

  useEffect(() => {
    loadData();
  }, [activeTab]);

  const loadData = async () => {
    try {
      setLoading(true);
      console.log('Loading data for tab:', activeTab);
      switch (activeTab) {
        case 'brands':
          console.log('Fetching brands...');
          const brandsResponse = await vehicleAPI.getBrands();
          console.log('Brands response:', brandsResponse);
          setBrands(brandsResponse.data || []);
          break;
        case 'models':
          const modelsResponse = await vehicleAPI.getModels();
          setModels(modelsResponse.data || []);
          break;
        case 'variants':
          const variantsResponse = await vehicleAPI.getVariants();
          setVariants(variantsResponse.data || []);
          break;
        case 'colors':
          const colorsResponse = await vehicleAPI.getColors();
          setColors(colorsResponse.data || []);
          break;
        default:
          break;
      }
    } catch (error) {
      console.error(`Error loading ${activeTab}:`, error);
      console.error('Error details:', error.response?.data || error.message);
      toast.error(`Không thể tải dữ liệu ${getTabTitle()}`);
    } finally {
      setLoading(false);
    }
  };

  const getTabTitle = () => {
    switch (activeTab) {
      case 'brands': return 'thương hiệu';
      case 'models': return 'dòng xe';
      case 'variants': return 'phiên bản';
      case 'colors': return 'màu sắc';
      default: return 'dữ liệu';
    }
  };

  const handleDelete = async (item, type) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa ${getTabTitle()} này?`)) {
      try {
        switch (type) {
          case 'brand':
            await vehicleAPI.deleteBrand(item.brandId);
            break;
          case 'model':
            await vehicleAPI.deleteModel(item.modelId);
            break;
          case 'variant':
            await vehicleAPI.deleteVariant(item.variantId);
            break;
          case 'color':
            await vehicleAPI.deleteColor(item.colorId);
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
               case 'brands':
                 setSelectedBrand(item);
                 setModalMode('edit');
                 setShowBrandModal(true);
                 break;
               case 'models':
                 setSelectedModel(item);
                 setModalMode('edit');
                 setShowModelModal(true);
                 break;
               case 'variants':
                 setSelectedVariant(item);
                 setModalMode('edit');
                 setShowVariantModal(true);
                 break;
               case 'colors':
                 setSelectedColor(item);
                 setModalMode('edit');
                 setShowColorModal(true);
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
               case 'brands':
                 setSelectedBrand(item);
                 setModalMode('view');
                 setShowBrandModal(true);
                 break;
               case 'models':
                 setSelectedModel(item);
                 setModalMode('view');
                 setShowModelModal(true);
                 break;
               case 'variants':
                 setSelectedVariant(item);
                 setModalMode('view');
                 setShowVariantModal(true);
                 break;
               case 'colors':
                 setSelectedColor(item);
                 setModalMode('view');
                 setShowColorModal(true);
                 break;
             }
           } catch (error) {
             console.error(`Error getting ${activeTab} details:`, error);
             toast.error(`Không thể tải thông tin ${getTabTitle()}`);
           }
         };


  const handleSaveBrand = async (brandId, brandData) => {
    try {
      await vehicleAPI.updateBrand(brandId, brandData);
      toast.success('Cập nhật thương hiệu thành công');
      loadData();
    } catch (error) {
      console.error('Error updating brand:', error);
      toast.error('Không thể cập nhật thương hiệu');
      throw error;
    }
  };

  const handleSaveModel = async (modelId, modelData) => {
    try {
      await vehicleAPI.updateModel(modelId, modelData);
      toast.success('Cập nhật dòng xe thành công');
      loadData();
    } catch (error) {
      console.error('Error updating model:', error);
      toast.error('Không thể cập nhật dòng xe');
      throw error;
    }
  };

  const handleSaveVariant = async (variantId, variantData) => {
    try {
      await vehicleAPI.updateVariant(variantId, variantData);
      toast.success('Cập nhật phiên bản thành công');
      loadData();
    } catch (error) {
      console.error('Error updating variant:', error);
      toast.error('Không thể cập nhật phiên bản');
      throw error;
    }
  };

  const handleSaveColor = async (colorId, colorData) => {
    try {
      await vehicleAPI.updateColor(colorId, colorData);
      toast.success('Cập nhật màu sắc thành công');
      loadData();
    } catch (error) {
      console.error('Error updating color:', error);
      toast.error('Không thể cập nhật màu sắc');
      throw error;
    }
  };

  const getColumns = () => {
    switch (activeTab) {
      case 'brands':
        return [
          { key: 'brandName', header: 'Tên thương hiệu' },
          { key: 'country', header: 'Quốc gia' },
          { key: 'foundedYear', header: 'Năm thành lập' },
          { 
            key: 'isActive', 
            header: 'Trạng thái',
            render: (item) => { const b = getActiveBadge(item.isActive); return (<span className={`badge ${b.class}`}>{b.text}</span>); }
          }
        ];
      case 'models':
        return [
          { 
            key: 'brand', 
            header: 'Thương hiệu',
            render: (item) => {
              console.log('Model item:', item);
              return item.brand?.brandName || item.brand?.name || item.brandName || 'N/A';
            }
          },
          { key: 'modelName', header: 'Tên dòng xe' },
          { key: 'year', header: 'Năm sản xuất' },
          { key: 'vehicleType', header: 'Loại xe' },
          { 
            key: 'isActive', 
            header: 'Trạng thái',
            render: (item) => { const b = getActiveBadge(item.isActive); return (<span className={`badge ${b.class}`}>{b.text}</span>); }
          }
        ];
      case 'variants':
        return [
          { 
            key: 'model', 
            header: 'Dòng xe',
            render: (item) => {
              console.log('Variant item:', item);
              return item.model?.modelName || item.model?.name || item.modelName || 'N/A';
            }
          },
          { key: 'variantName', header: 'Tên phiên bản' },
          { 
            key: 'basePrice', 
            header: 'Giá cơ bản',
            render: (item) => item.basePrice ? `${item.basePrice.toLocaleString('vi-VN')} VNĐ` : 'N/A'
          },
          { key: 'range', header: 'Tầm hoạt động (km)' },
          { key: 'batteryCapacity', header: 'Dung lượng pin (kWh)' }
        ];
      case 'colors':
        return [
          { key: 'colorName', header: 'Tên màu' },
          { 
            key: 'hexCode', 
            header: 'Mã màu',
            render: (item) => (
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <div 
                  style={{ 
                    width: '20px', 
                    height: '20px', 
                    backgroundColor: item.hexCode,
                    border: '1px solid #e5e7eb',
                    borderRadius: '4px'
                  }}
                />
                {item.hexCode}
              </div>
            )
          },
          { key: 'description', header: 'Mô tả' },
          { 
            key: 'isActive', 
            header: 'Trạng thái',
            render: (item) => { const b = getActiveBadge(item.isActive); return (<span className={`badge ${b.class}`}>{b.text}</span>); }
          }
        ];
      default:
        return [];
    }
  };

  const getCurrentData = () => {
    switch (activeTab) {
      case 'brands': 
        console.log('Getting brands data:', brands);
        return brands;
      case 'models': return models;
      case 'variants': return variants;
      case 'colors': return colors;
      default: return [];
    }
  };

  const currentData = getCurrentData();
  console.log('Current data for', activeTab, ':', currentData);
  
  const filteredData = currentData.filter(item => {
    const searchLower = searchTerm.toLowerCase();
    return (
      item.name?.toLowerCase().includes(searchLower) ||
      (item.brandName?.toLowerCase() || '').includes(searchLower) ||
      (item.modelName?.toLowerCase() || '').includes(searchLower) ||
      (item.variantName?.toLowerCase() || '').includes(searchLower) ||
      (item.colorName?.toLowerCase() || '').includes(searchLower) ||
      (item.description?.toLowerCase() || '').includes(searchLower) ||
      (item.country?.toLowerCase() || '').includes(searchLower) ||
      (item.vehicleType?.toLowerCase() || '').includes(searchLower) ||
      (item.brand?.brandName && item.brand.brandName.toLowerCase().includes(searchLower)) ||
      (item.brand?.name && item.brand.name.toLowerCase().includes(searchLower)) ||
      (item.model?.modelName && item.model.modelName.toLowerCase().includes(searchLower)) ||
      (item.model?.name && item.model.name.toLowerCase().includes(searchLower))
    );
  });
  
  console.log('Filtered data for', activeTab, ':', filteredData);

  const renderContent = () => {
    if (loading) {
      return <LoadingSpinner text={`Đang tải danh sách ${getTabTitle()}...`} />;
    }

    return (
      <>
        <div className="section-header">
          <h2>Quản lý {getTabTitle()}</h2>
          <div style={{ display: 'flex', gap: '10px' }}>
            <button 
              className="btn btn-outline" 
              onClick={async () => {
                try {
                  console.log('Testing brands API...');
                  const response = await vehicleAPI.getBrands();
                  console.log('Direct API test result:', response);
                } catch (error) {
                  console.error('Direct API test error:', error);
                }
              }}
            >
              Test API
            </button>
            <button 
              className="btn btn-outline" 
              onClick={() => {
                console.log('Setting test data...');
                if (activeTab === 'brands') {
                  setBrands([
                    { brandId: 1, brandName: 'Test Brand 1', country: 'Vietnam', foundedYear: 2020, isActive: true },
                    { brandId: 2, brandName: 'Test Brand 2', country: 'Japan', foundedYear: 2019, isActive: false }
                  ]);
                } else if (activeTab === 'models') {
                  setModels([
                    { 
                      modelId: 1, 
                      modelName: 'Test Model 1', 
                      brand: { brandId: 1, brandName: 'Tesla' },
                      year: 2023,
                      vehicleType: 'SUV',
                      isActive: true 
                    },
                    { 
                      modelId: 2, 
                      modelName: 'Test Model 2', 
                      brand: { brandId: 2, brandName: 'BYD' },
                      year: 2022,
                      vehicleType: 'Sedan',
                      isActive: false 
                    }
                  ]);
                }
              }}
            >
              Set Test Data
            </button>
            <button className="btn btn-primary">
              <Plus size={20} />
              Thêm {getTabTitle()}
            </button>
          </div>
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
               />
        
        {/* Debug info */}
        <div style={{ marginTop: '20px', padding: '10px', background: '#f0f0f0', borderRadius: '5px' }}>
          <h4>Debug Info:</h4>
          <p>Active Tab: {activeTab}</p>
          <p>Loading: {loading ? 'Yes' : 'No'}</p>
          <p>Current Data Length: {currentData.length}</p>
          <p>Filtered Data Length: {filteredData.length}</p>
          <p>Columns: {JSON.stringify(getColumns().map(col => col.key))}</p>
          <p>Search Term: "{searchTerm}"</p>
          <p>Sample Data: {JSON.stringify(currentData.slice(0, 2))}</p>
        </div>
      </>
    );
  };

  return (
    <div className="vehicle-management">
      <div className="page-header">
        <div className="page-title">
          <Car className="title-icon" />
          <h1>Quản lý xe</h1>
        </div>
        <p>Quản lý thông tin xe, thương hiệu, dòng xe và màu sắc</p>
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

        {/* Brand Modal */}
        <VehicleBrandModal
          brand={selectedBrand}
          isOpen={showBrandModal}
          mode={modalMode}
          onClose={() => {
            setShowBrandModal(false);
            setSelectedBrand(null);
            setModalMode('view');
          }}
          onSave={handleSaveBrand}
        />

        {/* Model Modal */}
        <VehicleModelModal
          model={selectedModel}
          isOpen={showModelModal}
          mode={modalMode}
          onClose={() => {
            setShowModelModal(false);
            setSelectedModel(null);
            setModalMode('view');
          }}
          onSave={handleSaveModel}
        />

        {/* Variant Modal */}
        <VehicleVariantModal
          variant={selectedVariant}
          isOpen={showVariantModal}
          mode={modalMode}
          onClose={() => {
            setShowVariantModal(false);
            setSelectedVariant(null);
            setModalMode('view');
          }}
          onSave={handleSaveVariant}
        />

        {/* Color Modal */}
        <VehicleColorModal
          color={selectedColor}
          isOpen={showColorModal}
          mode={modalMode}
          onClose={() => {
            setShowColorModal(false);
            setSelectedColor(null);
            setModalMode('view');
          }}
          onSave={handleSaveColor}
        />
      </div>
    </div>
  );
};


export default VehicleManagement;
