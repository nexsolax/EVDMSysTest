import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Package, Plus, Building2, Car } from 'lucide-react';
import { inventoryAPI, warehouseAPI, vehicleAPI } from '../services/api';
import { getStatusBadge as getStatusBadgeUtil, getActiveBadge } from '../utils/statusBadges';
import DataTable from '../components/common/DataTable';
import WarehouseModal from '../components/modals/WarehouseModal';
import InventoryItemModal from '../components/modals/InventoryItemModal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import toast from 'react-hot-toast';
import '../styles/common.css';
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
    { id: 'warehouses', label: 'Kho', path: '/admin/inventory/warehouses', icon: <Building2 size={20} /> },
    { id: 'vehicles', label: 'Tồn kho xe', path: '/admin/inventory/vehicles', icon: <Car size={20} /> }
  ];

  const getCurrentTab = () => {
    const currentPath = location.pathname;
    // Check for both /admin/inventory and /inventory paths
    if (currentPath === '/admin/inventory' || currentPath === '/inventory') {
      return 'warehouses';
    }
    const tab = tabs.find(t => t.path === currentPath);
    return tab ? tab.id : 'warehouses';
  };

  useEffect(() => {
    const newTab = getCurrentTab();
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
      switch (activeTab) {
        case 'warehouses':
          const warehousesResponse = await warehouseAPI.getWarehouses();
          setWarehouses(warehousesResponse.data || []);
          break;
        case 'vehicles':
          const inventoryResponse = await inventoryAPI.getInventory();
          let inventoryData = inventoryResponse.data || [];
          console.log('Loaded inventory data (raw):', inventoryData);
          
          // If items are missing relationships (variant/color/warehouse), try to fetch individual items
          // According to API_WAREHOUSE_INVENTORY_GUIDE.md, response only has relationship objects, not IDs
          // List endpoint may use lazy loading, but detail endpoint may use eager loading
          const itemsNeedingEnrichment = inventoryData.filter(item => 
            (!item.variant && !item.variantId) || 
            (!item.color && !item.colorId) || 
            (!item.warehouse && !item.warehouseId)
          );
          
          if (itemsNeedingEnrichment.length > 0) {
            console.log(`Found ${itemsNeedingEnrichment.length} items with missing relationships, fetching details...`);
            console.log('Items needing enrichment:', itemsNeedingEnrichment.map(i => ({ 
              inventoryId: i.inventoryId, 
              vin: i.vin,
              hasVariant: !!i.variant,
              hasColor: !!i.color,
              hasWarehouse: !!i.warehouse
            })));
            
            try {
              // Fetch full details for items missing relationships
              // Detail endpoint may have eager loaded relationships
              const enrichedItems = await Promise.all(
                itemsNeedingEnrichment.map(async (item) => {
                  try {
                    const detailRes = await inventoryAPI.getInventoryById(item.inventoryId);
                    const detailedItem = detailRes.data;
                    console.log(`Fetched detail for ${item.inventoryId}:`, {
                      hasVariant: !!detailedItem.variant,
                      hasColor: !!detailedItem.color,
                      hasWarehouse: !!detailedItem.warehouse,
                      variantId: detailedItem.variant?.variantId,
                      colorId: detailedItem.color?.colorId,
                      warehouseId: detailedItem.warehouse?.warehouseId
                    });
                    // Merge: prefer detailed item data (has relationships), but keep original fields
                    return { ...item, ...detailedItem };
                  } catch (err) {
                    console.warn('Failed to fetch details for item:', item.inventoryId, err);
                    return item; // Return original if fetch fails
                  }
                })
              );
              
              // Replace items in inventoryData with enriched versions
              enrichedItems.forEach(enrichedItem => {
                const index = inventoryData.findIndex(item => item.inventoryId === enrichedItem.inventoryId);
                if (index !== -1) {
                  inventoryData[index] = enrichedItem;
                }
              });
              
              console.log('Items after fetching details:', inventoryData.map(i => ({
                inventoryId: i.inventoryId,
                vin: i.vin,
                hasVariant: !!i.variant,
                hasColor: !!i.color,
                hasWarehouse: !!i.warehouse
              })));
            } catch (err) {
              console.error('Error fetching item details:', err);
            }
          }
          
          // Enrich inventory data with missing relationships if needed
          // Some API responses may not include nested variant, color, warehouse objects
          inventoryData = await enrichInventoryData(inventoryData);
          
          if (inventoryData.length > 0) {
            console.log('First inventory item (enriched):', inventoryData[0]);
            console.log('Variant:', inventoryData[0].variant);
            console.log('Color:', inventoryData[0].color);
            console.log('Warehouse:', inventoryData[0].warehouse);
          }
          setInventory(inventoryData);
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

  // Enrich inventory data with missing relationships
  // This function fetches variant, color, and warehouse data if they're missing from API response
  const enrichInventoryData = async (inventoryList) => {
    if (!inventoryList || inventoryList.length === 0) return inventoryList;

    try {
      // Fetch all variants, colors, warehouses, models, and brands in parallel
      const [variantsRes, colorsRes, warehousesRes, modelsRes, brandsRes] = await Promise.all([
        vehicleAPI.getVariants().catch(() => ({ data: [] })),
        vehicleAPI.getColors().catch(() => ({ data: [] })),
        warehouseAPI.getWarehouses().catch(() => ({ data: [] })),
        vehicleAPI.getModels().catch(() => ({ data: [] })),
        vehicleAPI.getBrands().catch(() => ({ data: [] }))
      ]);

      const variants = variantsRes.data || [];
      const colors = colorsRes.data || [];
      const warehouses = warehousesRes.data || [];
      const models = modelsRes.data || [];
      const brands = brandsRes.data || [];

      console.log('Enrichment data loaded:', {
        variantsCount: variants.length,
        colorsCount: colors.length,
        warehousesCount: warehouses.length,
        modelsCount: models.length,
        brandsCount: brands.length
      });

      // Create lookup maps for faster access
      // Use both string and number keys to handle type mismatches
      const variantMap = new Map();
      variants.forEach(v => {
        variantMap.set(v.variantId, v);
        variantMap.set(String(v.variantId), v);
        variantMap.set(Number(v.variantId), v);
      });
      
      const colorMap = new Map();
      colors.forEach(c => {
        colorMap.set(c.colorId, c);
        colorMap.set(String(c.colorId), c);
        colorMap.set(Number(c.colorId), c);
      });
      
      const warehouseMap = new Map();
      warehouses.forEach(w => {
        warehouseMap.set(w.warehouseId, w);
        warehouseMap.set(String(w.warehouseId), w);
        warehouseMap.set(Number(w.warehouseId), w);
      });
      
      const modelMap = new Map();
      models.forEach(m => {
        modelMap.set(m.modelId, m);
        modelMap.set(String(m.modelId), m);
        modelMap.set(Number(m.modelId), m);
      });
      
      const brandMap = new Map();
      brands.forEach(b => {
        brandMap.set(b.brandId, b);
        brandMap.set(String(b.brandId), b);
        brandMap.set(Number(b.brandId), b);
      });

      // Enrich each inventory item
      return inventoryList.map(item => {
        const enriched = { ...item };
        
        // Extract IDs from relationship objects if available (fallback)
        // Backend may not return variantId/colorId/warehouseId directly, but may return objects
        if (!enriched.variantId && enriched.variant?.variantId) {
          enriched.variantId = enriched.variant.variantId;
        }
        if (!enriched.colorId && enriched.color?.colorId) {
          enriched.colorId = enriched.color.colorId;
        }
        if (!enriched.warehouseId && enriched.warehouse?.warehouseId) {
          enriched.warehouseId = enriched.warehouse.warehouseId;
        }
        
        // Log raw item data for debugging
        console.log('Processing inventory item:', {
          inventoryId: enriched.inventoryId,
          vin: enriched.vin,
          hasVariant: !!enriched.variant,
          variantId: enriched.variantId,
          hasColor: !!enriched.color,
          colorId: enriched.colorId,
          hasWarehouse: !!enriched.warehouse,
          warehouseId: enriched.warehouseId
        });

        // If variant is missing but variantId exists, fetch from map
        if (!enriched.variant && enriched.variantId) {
          const variantIdValue = enriched.variantId;
          const variant = variantMap.get(variantIdValue) 
            || variantMap.get(String(variantIdValue)) 
            || variantMap.get(Number(variantIdValue));
          if (variant) {
            enriched.variant = variant;
            console.log('✓ Enriched variant for inventory', enriched.inventoryId, ':', variant.variantName);
          } else {
            console.warn('✗ Variant not found for variantId:', variantIdValue, {
              type: typeof variantIdValue,
              availableVariantIds: Array.from(variantMap.keys()).slice(0, 10),
              variantMapSize: variantMap.size
            });
          }
        } else if (!enriched.variant && !enriched.variantId) {
          console.warn('⚠ Inventory item has no variant and no variantId:', {
            inventoryId: enriched.inventoryId,
            vin: enriched.vin,
            message: 'Item may be missing variantId in database. Please check database foreign keys.'
          });
        }

        // If variant exists but model is missing, enrich it
        if (enriched.variant && !enriched.variant.model && enriched.variant.modelId) {
          const model = modelMap.get(enriched.variant.modelId) 
            || modelMap.get(String(enriched.variant.modelId)) 
            || modelMap.get(Number(enriched.variant.modelId));
          if (model) {
            enriched.variant.model = model;
            
            // If model exists but brand is missing, enrich it
            if (!model.brand && model.brandId) {
              const brand = brandMap.get(model.brandId) 
                || brandMap.get(String(model.brandId)) 
                || brandMap.get(Number(model.brandId));
              if (brand) {
                enriched.variant.model.brand = brand;
              }
            }
          }
        }

        // If color is missing but colorId exists, fetch from map
        if (!enriched.color && enriched.colorId) {
          const colorIdValue = enriched.colorId;
          const color = colorMap.get(colorIdValue) 
            || colorMap.get(String(colorIdValue)) 
            || colorMap.get(Number(colorIdValue));
          if (color) {
            enriched.color = color;
            console.log('✓ Enriched color for inventory', enriched.inventoryId, ':', color.colorName);
          } else {
            console.warn('✗ Color not found for colorId:', colorIdValue, {
              type: typeof colorIdValue,
              availableColorIds: Array.from(colorMap.keys()).slice(0, 10),
              colorMapSize: colorMap.size
            });
          }
        } else if (!enriched.color && !enriched.colorId) {
          console.warn('⚠ Inventory item has no color and no colorId:', {
            inventoryId: enriched.inventoryId,
            vin: enriched.vin,
            message: 'Item may be missing colorId in database. Please check database foreign keys.'
          });
        }

        // If warehouse is missing but warehouseId exists, fetch from map
        // Try multiple possible field names for warehouseId
        const warehouseIdValue = enriched.warehouseId || enriched.warehouse?.warehouseId;
        
        if (!enriched.warehouse && warehouseIdValue) {
          // Try to find warehouse by ID with multiple type attempts
          const warehouse = warehouseMap.get(warehouseIdValue) 
            || warehouseMap.get(String(warehouseIdValue)) 
            || warehouseMap.get(Number(warehouseIdValue));
          
          if (warehouse) {
            enriched.warehouse = warehouse;
            console.log('✓ Enriched warehouse for inventory', enriched.inventoryId, ':', warehouse.warehouseName);
          } else {
            console.warn('✗ Warehouse not found for ID:', warehouseIdValue, {
              type: typeof warehouseIdValue,
              availableWarehouseIds: Array.from(warehouseMap.keys()).slice(0, 10),
              warehouseMapSize: warehouseMap.size
            });
          }
        } else if (!enriched.warehouse && !warehouseIdValue) {
          console.warn('⚠ Inventory item has no warehouse and no warehouseId:', {
            inventoryId: enriched.inventoryId,
            vin: enriched.vin,
            message: 'Item may be missing warehouseId in database. Please check database foreign keys.'
          });
        }
        
        // Log final enriched state
        const finalState = {
          inventoryId: enriched.inventoryId,
          vin: enriched.vin,
          hasVariant: !!enriched.variant,
          variantName: enriched.variant?.variantName || 'N/A',
          hasColor: !!enriched.color,
          colorName: enriched.color?.colorName || 'N/A',
          hasWarehouse: !!enriched.warehouse,
          warehouseName: enriched.warehouse?.warehouseName || 'N/A'
        };
        
        // Check if item is still missing critical data
        if (!enriched.variant && !enriched.color && !enriched.warehouse) {
          console.error('❌ Item completely missing relationships (may need database fix):', finalState);
        } else if (!enriched.variant || !enriched.color) {
          console.warn('⚠ Item missing required relationships:', finalState);
        } else {
          console.log('✓ Final enriched item:', finalState);
        }

        return enriched;
      });
    } catch (error) {
      console.error('Error enriching inventory data:', error);
      // Return original data if enrichment fails
      return inventoryList;
    }
  };

  const handleDelete = async (item, type) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa ${getTabTitle()} này?`)) {
      try {
        // Map activeTab to correct delete type
        const deleteType = activeTab === 'vehicles' ? 'inventory' : activeTab === 'warehouses' ? 'warehouse' : type;
        
        switch (deleteType) {
          case 'warehouse':
            if (!item.warehouseId) {
              toast.error('Không tìm thấy ID của kho');
              return;
            }
            await warehouseAPI.deleteWarehouse(item.warehouseId);
            break;
          case 'inventory':
            if (!item.inventoryId && !item.id) {
              toast.error('Không tìm thấy ID của xe tồn kho');
              console.error('Item missing inventoryId:', item);
              return;
            }
            const inventoryId = item.inventoryId || item.id;
            console.log('Deleting inventory:', {
              inventoryId: inventoryId,
              vin: item.vin,
              fullItem: item
            });
            
            try {
              await inventoryAPI.deleteInventory(inventoryId);
              console.log('Delete request successful');
            } catch (deleteError) {
              console.error('Delete API error:', {
                status: deleteError.response?.status,
                statusText: deleteError.response?.statusText,
                data: deleteError.response?.data,
                message: deleteError.message
              });
              throw deleteError; // Re-throw to be caught by outer catch
            }
            break;
          default:
            console.warn('Unknown delete type:', deleteType, 'for activeTab:', activeTab);
            toast.error(`Không xác định được loại dữ liệu cần xóa`);
            return;
        }
        toast.success(`Xóa ${getTabTitle()} thành công`);
        loadData();
      } catch (error) {
        console.error(`Error deleting ${type}:`, error);
        const errorMessage = error.response?.data?.message || error.message || 'Không xác định';
        toast.error(`Không thể xóa ${getTabTitle()}: ${errorMessage}`);
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
        default:
          console.warn('Unknown tab for view:', activeTab);
          break;
      }
      
    } catch (error) {
      console.error(`Error getting ${activeTab} details:`, error);
      toast.error(`Không thể tải thông tin ${getTabTitle()}`);
    }
  };

  const handleCreate = () => {
    if (activeTab === 'warehouses') {
      setSelectedWarehouse(null);
      setModalMode('create');
      setShowWarehouseModal(true);
    } else if (activeTab === 'vehicles') {
      setSelectedInventoryItem(null);
      setModalMode('create');
      setShowInventoryModal(true);
    }
  };

  const handleSaveWarehouse = async (warehouseId, data) => {
    try {
      if (warehouseId) {
        // Update existing warehouse
        await warehouseAPI.updateWarehouse(warehouseId, data);
        toast.success('Cập nhật kho thành công');
      } else {
        // Create new warehouse
        await warehouseAPI.createWarehouse(data);
        toast.success('Tạo kho thành công');
      }
      loadData();
      setShowWarehouseModal(false);
    } catch (error) {
      console.error('Error saving warehouse:', error);
      const errorMessage = error.response?.data?.message || error.message;
      toast.error(errorMessage || 'Không thể lưu kho');
      throw error;
    }
  };

  const handleSaveInventoryItem = async (inventoryId, data) => {
    try {
      if (inventoryId) {
        // Update existing inventory
        await inventoryAPI.updateInventory(inventoryId, data);
        toast.success('Cập nhật tồn kho thành công');
      } else {
        // Create new inventory
        await inventoryAPI.createInventory(data);
        toast.success('Tạo tồn kho thành công');
      }
      loadData();
      setShowInventoryModal(false);
    } catch (error) {
      console.error('Error saving inventory item:', error);
      const errorMessage = error.response?.data?.message || error.message;
      toast.error(errorMessage || 'Không thể lưu tồn kho');
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
            render: (item) => {
              const variant = item.variant;
              
              // If variant is missing, try to show variantId at least
              if (!variant) {
                if (item.variantId) {
                  return <span className="text-gray-400">Variant ID: {item.variantId}</span>;
                }
                return <span className="text-gray-400">N/A</span>;
              }
              
              // Get variant image URL
              const getVariantImageUrl = (variant) => {
                if (variant?.variantImageUrl) {
                  // Nếu là URL tuyệt đối, dùng trực tiếp
                  if (variant.variantImageUrl.startsWith('http://') || variant.variantImageUrl.startsWith('https://')) {
                    return variant.variantImageUrl;
                  }
                  // Nếu là đường dẫn tương đối, thêm base URL
                  const baseUrl = process.env.REACT_APP_API_URL 
                    ? process.env.REACT_APP_API_URL.replace('/api', '') 
                    : 'http://localhost:8080';
                  const cleanPath = variant.variantImageUrl.startsWith('/') ? variant.variantImageUrl : `/${variant.variantImageUrl}`;
                  return `${baseUrl}${cleanPath}`;
                }
                if (variant?.variantImagePath) {
                  // Nếu là URL tuyệt đối
                  if (variant.variantImagePath.startsWith('http://') || variant.variantImagePath.startsWith('https://')) {
                    return variant.variantImagePath;
                  }
                  // Nếu là đường dẫn tương đối, thêm base URL
                  const baseUrl = process.env.REACT_APP_API_URL 
                    ? process.env.REACT_APP_API_URL.replace('/api', '') 
                    : 'http://localhost:8080';
                  const cleanPath = variant.variantImagePath.startsWith('/') ? variant.variantImagePath : `/${variant.variantImagePath}`;
                  return `${baseUrl}${cleanPath}`;
                }
                return null;
              };
              
              const imageUrl = getVariantImageUrl(variant);
              const variantName = variant.variantName || 'N/A';
              
              return (
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  {/* Variant Image */}
                  {imageUrl ? (
                    <img
                      src={imageUrl}
                      alt={variantName}
                      style={{
                        width: '60px',
                        height: '40px',
                        objectFit: 'cover',
                        borderRadius: '4px',
                        border: '1px solid #e5e7eb'
                      }}
                      onError={(e) => {
                        e.target.style.display = 'none';
                        if (e.target.nextElementSibling) {
                          e.target.nextElementSibling.style.display = 'flex';
                        }
                      }}
                    />
                  ) : null}
                  <div
                    style={{
                      display: imageUrl ? 'none' : 'flex',
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
                  
                  {/* Variant Name */}
                  <div className="font-medium">
                    {variantName}
                  </div>
                </div>
              );
            }
          },
          { 
            key: 'color', 
            header: 'Màu sắc',
            render: (item) => {
              const color = item.color;
              if (!color) return <span className="text-gray-400">N/A</span>;
              
              // Sử dụng colorCode hoặc colorSwatchUrl/Path từ entity
              const colorCode = color.colorCode;
              const swatchUrl = color.colorSwatchUrl || color.colorSwatchPath;
              const colorName = color.colorName || 'N/A';
              
              return (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  {swatchUrl ? (
                    <img 
                      src={swatchUrl} 
                      alt={colorName}
                      style={{ 
                        width: '20px', 
                        height: '20px', 
                        objectFit: 'cover',
                        border: '1px solid #e5e7eb',
                        borderRadius: '4px'
                      }}
                      onError={(e) => {
                        // Fallback to colorCode if image fails
                        e.target.style.display = 'none';
                        const fallback = e.target.nextElementSibling;
                        if (fallback) fallback.style.display = 'flex';
                      }}
                    />
                  ) : null}
                  <div 
                    style={{ 
                      display: swatchUrl ? 'none' : 'flex',
                      width: '20px', 
                      height: '20px', 
                      backgroundColor: colorCode || '#cccccc',
                      border: '1px solid #e5e7eb',
                      borderRadius: '4px'
                    }}
                  />
                  <span>{colorName}</span>
                </div>
              );
            }
          },
          { key: 'vin', header: 'VIN' },
          { 
            key: 'warehouse', 
            header: 'Kho',
            render: (item) => {
              // Try multiple ways to get warehouse data
              const warehouse = item.warehouse;
              const warehouseId = item.warehouseId || item.warehouse?.warehouseId;
              
              // If warehouse object exists, use it
              if (warehouse && warehouse.warehouseName) {
                return (
                  <div>
                    <div className="font-medium">{warehouse.warehouseName}</div>
                    {warehouse.warehouseCode && (
                      <div className="text-sm text-gray-500">{warehouse.warehouseCode}</div>
                    )}
                  </div>
                );
              }
              
              // If warehouseId exists but no warehouse object, show ID as fallback
              if (warehouseId) {
                return <span className="text-gray-400">Warehouse ID: {warehouseId}</span>;
              }
              
              return <span className="text-gray-400">N/A</span>;
            }
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
      (item.variant?.model?.brand?.brandName?.toLowerCase() || '').includes(searchLower) ||
      (item.variant?.model?.modelName?.toLowerCase() || '').includes(searchLower) ||
      (item.variant?.variantName?.toLowerCase() || '').includes(searchLower) ||
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
          <button className="btn btn-primary" onClick={handleCreate}>
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
          onDelete={(item) => handleDelete(item, activeTab)}
          onView={handleView}
          onReserveVehicle={activeTab === 'vehicles' ? async (item) => {
            try {
              await inventoryAPI.updateInventoryStatus(item.inventoryId, 'reserved');
              toast.success('Đặt trước xe thành công');
              loadData();
            } catch (error) {
              console.error('Error reserving vehicle:', error);
              toast.error('Không thể đặt trước xe');
            }
          } : undefined}
          onReleaseVehicle={activeTab === 'vehicles' ? async (item) => {
            try {
              await inventoryAPI.updateInventoryStatus(item.inventoryId, 'available');
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