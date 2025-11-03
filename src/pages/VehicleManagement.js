import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Car, Plus, Package, Palette, Settings } from 'lucide-react';
import { vehicleAPI, productAPI } from '../services/api';
import { getActiveBadge } from '../utils/statusBadges';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import VehicleBrandModal from '../components/modals/VehicleBrandModal';
import VehicleModelModal from '../components/modals/VehicleModelModal';
import VehicleVariantModal from '../components/modals/VehicleVariantModal';
import VehicleColorModal from '../components/modals/VehicleColorModal';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './VehicleManagement.css';

// Component để hiển thị logo trong bảng
const BrandLogoCell = ({ logoUrl, brandName }) => {
  const [imageError, setImageError] = useState(false);

  if (logoUrl && !imageError) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <img
          src={logoUrl}
          alt={brandName || 'Brand Logo'}
          style={{
            width: '40px',
            height: '40px',
            objectFit: 'contain',
            borderRadius: '4px',
            border: '1px solid #e5e7eb'
          }}
          onError={() => setImageError(true)}
        />
      </div>
    );
  }

  return (
    <div
      style={{
        display: 'flex',
        width: '40px',
        height: '40px',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#f3f4f6',
        borderRadius: '4px',
        color: '#9ca3af',
        fontSize: '10px',
        textAlign: 'center',
        padding: '4px'
      }}
      title={logoUrl ? 'Không thể tải logo' : 'Chưa có logo'}
    >
      No Logo
    </div>
  );
};

// Component để hiển thị color swatch trong bảng (giống BrandLogoCell)
const ColorSwatchCell = ({ swatchUrl, colorCode, colorName }) => {
  const [imageError, setImageError] = useState(false);

  if (swatchUrl && !imageError) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
        <img
          src={swatchUrl}
          alt={colorName || 'Color Swatch'}
          style={{
            width: '48px',
            height: '48px',
            objectFit: 'cover',
            borderRadius: '4px',
            border: '1px solid #e5e7eb',
            boxShadow: '0 1px 2px rgba(0,0,0,0.1)'
          }}
          onError={() => {
            console.log('Image error for swatch URL:', swatchUrl);
            setImageError(true);
          }}
        />
        {colorCode && <span>{colorCode}</span>}
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
      <div
        style={{
          display: 'flex',
          width: '48px',
          height: '48px',
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: '#f3f4f6',
          borderRadius: '4px',
          color: '#6b7280',
          fontSize: '10px',
          textAlign: 'center',
          padding: '4px',
          border: '1px solid #e5e7eb',
          boxShadow: '0 1px 2px rgba(0,0,0,0.1)'
        }}
        title={swatchUrl ? 'Không thể tải hình ảnh màu' : 'Chưa có hình ảnh màu'}
      >
        {colorCode || 'N/A'}
      </div>
      {colorCode && <span>{colorCode}</span>}
    </div>
  );
};

// Component để hiển thị hình ảnh variant trong bảng
const VariantImageCell = ({ imageUrl, variantName }) => {
  const [imageError, setImageError] = useState(false);

  if (imageUrl && !imageError) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <img
          src={imageUrl}
          alt={variantName || 'Variant Image'}
          style={{
            width: '60px',
            height: '40px',
            objectFit: 'cover',
            borderRadius: '4px',
            border: '1px solid #e5e7eb'
          }}
          onError={() => setImageError(true)}
        />
      </div>
    );
  }

  return (
    <div
      style={{
        display: 'flex',
        width: '60px',
        height: '40px',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#f3f4f6',
        borderRadius: '4px',
        color: '#9ca3af',
        fontSize: '10px',
        textAlign: 'center',
        padding: '4px'
      }}
      title={imageUrl ? 'Không thể tải hình ảnh' : 'Chưa có hình ảnh'}
    >
      No Image
    </div>
  );
};

const VehicleManagement = () => {
  const location = useLocation();
  const [activeTab, setActiveTab] = useState('brands');
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [error, setError] = useState(null);
  
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.pathname]);

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null); // Clear previous error
      console.log('Loading data for tab:', activeTab);
      switch (activeTab) {
        case 'brands':
          console.log('Fetching brands...');
          const brandsResponse = await vehicleAPI.getBrands();
          console.log('Brands response:', brandsResponse);
          setBrands(brandsResponse.data || []);
          break;
        case 'models':
          console.log('Fetching models...');
          try {
            const modelsResponse = await vehicleAPI.getModels();
            console.log('Models response:', modelsResponse);
            setModels(modelsResponse.data || []);
          } catch (modelsError) {
            console.error('Error with getModels, trying getActiveModels as fallback:', modelsError);
            // Thử fallback endpoint nếu endpoint chính lỗi
            try {
              const activeModelsResponse = await vehicleAPI.getActiveModels();
              console.log('Active models response (fallback):', activeModelsResponse);
              setModels(activeModelsResponse.data || []);
              toast.error('Chỉ tải được danh sách dòng xe đang hoạt động. Vui lòng kiểm tra backend để sửa lỗi.');
            } catch (fallbackError) {
              console.error('Fallback also failed:', fallbackError);
              throw modelsError; // Throw original error
            }
          }
          break;
        case 'variants':
          console.log('Fetching variants...');
          try {
            const variantsResponse = await vehicleAPI.getVariants();
            console.log('Variants response:', variantsResponse);
            const variantsData = variantsResponse.data || [];
            console.log('Variants data:', variantsData);
            console.log('First variant sample:', variantsData[0]);
            setVariants(variantsData);
          } catch (variantsError) {
            console.error('Error with getVariants, trying getActiveVariants as fallback:', variantsError);
            // Thử fallback endpoint nếu endpoint chính lỗi
            try {
              const activeVariantsResponse = await vehicleAPI.getActiveVariants();
              console.log('Active variants response (fallback):', activeVariantsResponse);
              const variantsData = activeVariantsResponse.data || [];
              console.log('Active variants data:', variantsData);
              setVariants(variantsData);
              toast.error('Chỉ tải được danh sách phiên bản đang hoạt động. Vui lòng kiểm tra backend để sửa lỗi.');
            } catch (fallbackError) {
              console.error('Fallback also failed:', fallbackError);
              throw variantsError; // Throw original error
            }
          }
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
      console.error('Error response:', error.response);
      console.error('Error status:', error.response?.status);
      console.error('Error data:', error.response?.data);
      console.error('Error message:', error.message);
      
      // Hiển thị lỗi chi tiết hơn
      let errorMessage = `Không thể tải dữ liệu ${getTabTitle()}`;
      if (error.response?.status === 500) {
        errorMessage = `Lỗi server khi tải ${getTabTitle()}. Vui lòng kiểm tra backend.`;
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      toast.error(errorMessage);
      setError({
        message: errorMessage,
        status: error.response?.status,
        details: error.response?.data
      });
      // Set empty array để tránh crash UI
      switch (activeTab) {
        case 'brands': setBrands([]); break;
        case 'models': setModels([]); break;
        case 'variants': setVariants([]); break;
        case 'colors': setColors([]); break;
        default: break;
      }
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
            await productAPI.deleteBrand(item.brandId);
            break;
          case 'model':
            await productAPI.deleteModel(item.modelId);
            break;
          case 'variant':
            await productAPI.deleteVariant(item.variantId);
            break;
          case 'color':
            await productAPI.deleteColor(item.colorId);
            break;
          default:
            console.warn('Unknown delete type:', type);
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
               default:
                 console.warn('Unknown tab for edit:', activeTab);
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
               default:
                 console.warn('Unknown tab for view:', activeTab);
                 break;
             }
           } catch (error) {
             console.error(`Error getting ${activeTab} details:`, error);
             toast.error(`Không thể tải thông tin ${getTabTitle()}`);
           }
         };


  const handleCreateBrand = async (brandData) => {
    try {
      await productAPI.createBrand(brandData);
      toast.success('Thêm thương hiệu thành công');
      loadData();
    } catch (error) {
      console.error('Error creating brand:', error);
      toast.error('Không thể thêm thương hiệu');
      throw error;
    }
  };

  const handleSaveBrand = async (brandId, brandData) => {
    try {
      console.log('Updating brand with data:', brandData);
      const response = await productAPI.updateBrand(brandId, brandData);
      console.log('Update brand response:', response);
      toast.success('Cập nhật thương hiệu thành công');
      
      // Reload data để có thông tin mới nhất
      await loadData();
      
      // Cập nhật lại selectedBrand từ danh sách mới nếu đang mở modal
      if (showBrandModal && selectedBrand?.brandId === brandId) {
        const updatedBrand = brands.find(b => b.brandId === brandId);
        if (updatedBrand) {
          // Load full details từ API để có description
          try {
            const brandDetailResponse = await vehicleAPI.getBrand(brandId);
            setSelectedBrand(brandDetailResponse.data);
          } catch (error) {
            console.warn('Could not reload brand details, using updated brand from list:', error);
            setSelectedBrand(updatedBrand);
          }
        }
      }
    } catch (error) {
      console.error('Error updating brand:', error);
      console.error('Error response:', error.response);
      const errorMessage = error.response?.data?.message || 'Không thể cập nhật thương hiệu';
      toast.error(errorMessage);
      throw error;
    }
  };

  const handleCreateModel = async (modelData) => {
    try {
      console.log('Creating model with data:', modelData);
      const response = await productAPI.createModel(modelData);
      console.log('Create model response:', response);
      toast.success('Thêm dòng xe thành công');
      loadData();
    } catch (error) {
      console.error('Error creating model:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Không thể thêm dòng xe';
      toast.error(errorMessage);
      throw error;
    }
  };

  const handleSaveModel = async (modelId, modelData) => {
    try {
      await productAPI.updateModel(modelId, modelData);
      toast.success('Cập nhật dòng xe thành công');
      loadData();
    } catch (error) {
      console.error('Error updating model:', error);
      toast.error('Không thể cập nhật dòng xe');
      throw error;
    }
  };

  const handleCreateVariant = async (variantData) => {
    try {
      console.log('Creating variant with data:', variantData);
      console.log('Variant data type check:', {
        modelId: typeof variantData.modelId,
        priceBase: typeof variantData.priceBase,
        batteryCapacity: typeof variantData.batteryCapacity
      });
      const response = await productAPI.createVariant(variantData);
      console.log('Create variant response:', response);
      toast.success('Thêm phiên bản thành công');
      loadData();
    } catch (error) {
      console.error('Error creating variant:', error);
      console.error('Error response:', error.response);
      console.error('Error data:', error.response?.data);
      const errorMessage = error.response?.data?.message || error.message || 'Không thể thêm phiên bản';
      toast.error(errorMessage);
      throw error;
    }
  };

  const handleSaveVariant = async (variantId, variantData) => {
    try {
      console.log('Updating variant with data:', variantData);
      console.log('Variant ID:', variantId);
      console.log('Variant data type check:', {
        modelId: typeof variantData.modelId,
        priceBase: typeof variantData.priceBase,
        batteryCapacity: typeof variantData.batteryCapacity
      });
      const response = await productAPI.updateVariant(variantId, variantData);
      console.log('Update variant response:', response);
      toast.success('Cập nhật phiên bản thành công');
      loadData();
    } catch (error) {
      console.error('Error updating variant:', error);
      console.error('Error response:', error.response);
      console.error('Error status:', error.response?.status);
      console.error('Error data:', error.response?.data);
      const errorMessage = error.response?.data?.message || error.message || 'Không thể cập nhật phiên bản';
      toast.error(errorMessage);
      throw error;
    }
  };

  const handleCreateColor = async (colorData) => {
    try {
      await productAPI.createColor(colorData);
      toast.success('Thêm màu sắc thành công');
      loadData();
    } catch (error) {
      console.error('Error creating color:', error);
      toast.error('Không thể thêm màu sắc');
      throw error;
    }
  };

  const handleSaveColor = async (colorId, colorData) => {
    try {
      await productAPI.updateColor(colorId, colorData);
      toast.success('Cập nhật màu sắc thành công');
      loadData();
    } catch (error) {
      console.error('Error updating color:', error);
      toast.error('Không thể cập nhật màu sắc');
      throw error;
    }
  };

  // Helper function để lấy logo URL từ brand
  const getBrandLogoUrl = (brand) => {
    if (brand?.brandLogoUrl) {
      return brand.brandLogoUrl;
    }
    if (brand?.brandLogoPath) {
      // Nếu là URL tuyệt đối
      if (brand.brandLogoPath.startsWith('http://') || brand.brandLogoPath.startsWith('https://')) {
        return brand.brandLogoPath;
      }
      // Nếu là đường dẫn tương đối, thêm prefix nếu cần
      return brand.brandLogoPath.startsWith('/') ? brand.brandLogoPath : `/${brand.brandLogoPath}`;
    }
    return null;
  };

  // Helper function để lấy color swatch URL
  const getColorSwatchUrl = (color) => {
    if (color?.colorSwatchUrl) {
      return color.colorSwatchUrl;
    }
    if (color?.colorSwatchPath) {
      // Nếu là URL tuyệt đối
      if (color.colorSwatchPath.startsWith('http://') || color.colorSwatchPath.startsWith('https://')) {
        return color.colorSwatchPath;
      }
      // Nếu là đường dẫn tương đối, thêm base URL của backend (giống variant image)
      const baseUrl = process.env.REACT_APP_API_URL 
        ? process.env.REACT_APP_API_URL.replace('/api', '') 
        : 'http://localhost:8080';
      const cleanPath = color.colorSwatchPath.startsWith('/') ? color.colorSwatchPath : `/${color.colorSwatchPath}`;
      return `${baseUrl}${cleanPath}`;
    }
    return null;
  };

  // Helper function để lấy hình ảnh URL từ variant (giống brand logo)
  const getVariantImageUrl = (variant) => {
    if (variant?.variantImageUrl) {
      return variant.variantImageUrl;
    }
    if (variant?.variantImagePath) {
      // Nếu là URL tuyệt đối
      if (variant.variantImagePath.startsWith('http://') || variant.variantImagePath.startsWith('https://')) {
        return variant.variantImagePath;
      }
      // Nếu là đường dẫn tương đối, thêm base URL của backend (giống brand logo)
      const baseUrl = process.env.REACT_APP_API_URL 
        ? process.env.REACT_APP_API_URL.replace('/api', '') 
        : 'http://localhost:8080';
      const cleanPath = variant.variantImagePath.startsWith('/') ? variant.variantImagePath : `/${variant.variantImagePath}`;
      return `${baseUrl}${cleanPath}`;
    }
    return null;
  };

  const getColumns = () => {
    switch (activeTab) {
      case 'brands':
        return [
          {
            key: 'logo',
            header: 'Logo',
            render: (item) => {
              const logoUrl = getBrandLogoUrl(item);
              
              return (
                <BrandLogoCell logoUrl={logoUrl} brandName={item.brandName} />
              );
            }
          },
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
              return item.brand?.brandName || item.brand?.name || item.brandName || 'N/A';
            }
          },
          { key: 'modelName', header: 'Tên dòng xe' },
          { 
            key: 'modelYear', 
            header: 'Năm sản xuất',
            render: (item) => {
              const year = item.modelYear || item.year; // Fallback cho dữ liệu cũ
              return year ? year.toString() : 'N/A';
            }
          },
          { 
            key: 'vehicleType', 
            header: 'Loại xe',
            render: (item) => {
              return item.vehicleType || item.bodyType || 'N/A';
            }
          },
          { 
            key: 'isActive', 
            header: 'Trạng thái',
            render: (item) => { const b = getActiveBadge(item.isActive); return (<span className={`badge ${b.class}`}>{b.text}</span>); }
          }
        ];
      case 'variants':
        return [
          {
            key: 'image',
            header: 'Hình ảnh',
            render: (item) => {
              const imageUrl = getVariantImageUrl(item);
              return (
                <VariantImageCell imageUrl={imageUrl} variantName={item.variantName} />
              );
            }
          },
          { 
            key: 'model', 
            header: 'Dòng xe',
            render: (item) => {
              return item.model?.modelName || item.model?.name || item.modelName || 'N/A';
            }
          },
          { key: 'variantName', header: 'Tên phiên bản' },
          { 
            key: 'priceBase', 
            header: 'Giá cơ bản (VNĐ)',
            render: (item) => {
              // Backend trả về priceBase
              const price = item.priceBase || item.basePrice; // Fallback cho dữ liệu cũ
              // Kiểm tra cả null, undefined và 0 (0 là giá trị hợp lệ)
              return (price !== null && price !== undefined && price !== '') 
                ? `${Number(price).toLocaleString('vi-VN')} VNĐ` 
                : 'N/A';
            }
          },
          { 
            key: 'rangeKm', 
            header: 'Tầm hoạt động (km)',
            render: (item) => {
              const range = item.rangeKm || item.range;
              return range ? range.toString() : 'N/A';
            }
          },
          { 
            key: 'batteryCapacity', 
            header: 'Dung lượng pin (kWh)',
            render: (item) => {
              const capacity = item.batteryCapacity;
              return capacity ? capacity.toString() : 'N/A';
            }
          }
        ];
      case 'colors':
        return [
          { key: 'colorName', header: 'Tên màu' },
          { 
            key: 'colorSwatch', 
            header: 'Hình ảnh màu',
            render: (item) => {
              const swatchUrl = getColorSwatchUrl(item);
              return (
                <ColorSwatchCell 
                  swatchUrl={swatchUrl} 
                  colorCode={item.colorCode} 
                  colorName={item.colorName} 
                />
              );
            }
          },
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
      (item.model?.modelName && item.model.modelName.toLowerCase().includes(searchLower))
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
            {error && (
              <button 
                className="btn btn-outline"
                onClick={() => loadData()}
                title="Thử lại tải dữ liệu"
              >
                🔄 Thử lại
              </button>
            )}
            <button 
              className="btn btn-primary"
              onClick={() => {
                switch (activeTab) {
                  case 'brands':
                    setSelectedBrand(null);
                    setModalMode('create');
                    setShowBrandModal(true);
                    break;
                  case 'models':
                    setSelectedModel(null);
                    setModalMode('create');
                    setShowModelModal(true);
                    break;
                  case 'variants':
                    setSelectedVariant(null);
                    setModalMode('create');
                    setShowVariantModal(true);
                    break;
                  case 'colors':
                    setSelectedColor(null);
                    setModalMode('create');
                    setShowColorModal(true);
                    break;
                  default:
                    break;
                }
              }}
            >
              <Plus size={20} />
              Thêm {getTabTitle()}
            </button>
          </div>
        </div>

        {error && (
          <div style={{ 
            marginBottom: '20px', 
            padding: '16px', 
            background: '#fee2e2', 
            border: '1px solid #fca5a5',
            borderRadius: '8px',
            color: '#991b1b'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <strong>⚠️ Lỗi khi tải dữ liệu</strong>
              <button 
                onClick={() => setError(null)}
                style={{ 
                  background: 'none', 
                  border: 'none', 
                  fontSize: '20px', 
                  cursor: 'pointer',
                  color: '#991b1b'
                }}
                title="Đóng"
              >
                ×
              </button>
            </div>
            <p style={{ margin: '4px 0' }}>{error.message}</p>
            {error.status === 500 && (
              <div style={{ marginTop: '8px', fontSize: '13px', opacity: 0.8 }}>
                <strong>Hướng dẫn sửa lỗi backend:</strong>
                <ul style={{ margin: '8px 0', paddingLeft: '20px' }}>
                  <li>Kiểm tra log server để xem lỗi cụ thể</li>
                  <li>Đảm bảo không có circular reference trong serialization (Model ↔ Brand)</li>
                  <li>Thêm <code>@JsonIgnore</code> hoặc <code>@JsonManagedReference/@JsonBackReference</code></li>
                  <li>Kiểm tra xem có exception không được catch trong Controller/Service</li>
                </ul>
              </div>
            )}
            {error.details && (
              <details style={{ marginTop: '8px', fontSize: '12px' }}>
                <summary style={{ cursor: 'pointer' }}>Chi tiết lỗi (click để xem)</summary>
                <pre style={{ 
                  marginTop: '8px', 
                  padding: '8px', 
                  background: '#fff', 
                  borderRadius: '4px',
                  overflow: 'auto',
                  maxHeight: '200px'
                }}>
                  {JSON.stringify(error.details, null, 2)}
                </pre>
              </details>
            )}
          </div>
        )}
        
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
          onSave={modalMode === 'create' ? handleCreateBrand : handleSaveBrand}
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
          onSave={modalMode === 'create' ? handleCreateModel : handleSaveModel}
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
          onSave={modalMode === 'create' ? handleCreateVariant : handleSaveVariant}
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
          onSave={modalMode === 'create' ? handleCreateColor : handleSaveColor}
        />
      </div>
    </div>
  );
};


export default VehicleManagement;
