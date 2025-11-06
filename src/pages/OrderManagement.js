import React, { useState, useEffect } from 'react';
import { ShoppingCart, Plus, DollarSign, Calendar, Package, User, FileText } from 'lucide-react';
import { orderAPI, quotationAPI, customerAPI, inventoryAPI, paymentAPI, vehicleAPI } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { getStatusBadge as getStatusBadgeUtil } from '../utils/statusBadges';
import DataTable from '../components/common/DataTable';
import LoadingSpinner from '../components/common/LoadingSpinner';
import OrderModal from '../components/modals/OrderModal';
import toast from 'react-hot-toast';
import '../styles/common.css';
import './OrderManagement.css';

const OrderManagement = () => {
  const { user, hasAnyRole } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Modal states
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showOrderModal, setShowOrderModal] = useState(false);
  const [modalMode, setModalMode] = useState('view'); // 'view' or 'edit'
  
  // Check if user is EVM_STAFF or ADMIN (can create quotations)
  const canCreateQuotation = hasAnyRole(['admin', 'evm_staff']);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const response = await orderAPI.getOrders();
      const ordersData = response.data || [];
      
      // Log để debug
      console.log('Orders loaded from API:', ordersData);
      if (ordersData.length > 0) {
        console.log('First order structure:', ordersData[0]);
        console.log('First order customer:', ordersData[0].customer);
        console.log('First order inventory:', ordersData[0].inventory);
        console.log('First order totalAmount:', ordersData[0].totalAmount);
      }
      
      // Enrich data nếu thiếu relationships
      // Nếu orders không có customer hoặc inventory được load, load chi tiết từng order
      const enrichedOrders = await Promise.all(
        ordersData.map(async (order) => {
          // Kiểm tra xem có thiếu relationships không
          const needsEnrichment = 
            (order.customerId && !order.customer) || 
            (order.inventoryId && !order.inventory) ||
            (!order.totalAmount && !order.orderAmount && !order.amount);
          
          if (needsEnrichment && order.orderId) {
            try {
              // Load chi tiết order để có đầy đủ relationships
              const detailResponse = await orderAPI.getOrder(order.orderId);
              const detailedOrder = detailResponse.data;
              console.log('Enriched order:', order.orderId, detailedOrder);
              
              // Nếu vẫn thiếu relationships, fetch riêng
              let customerData = detailedOrder.customer || order.customer;
              let inventoryData = detailedOrder.inventory || order.inventory;
              
              if (!customerData && detailedOrder.customerId) {
                try {
                  const customerResponse = await customerAPI.getCustomer(detailedOrder.customerId);
                  customerData = customerResponse.data;
                  console.log('Fetched customer for order:', order.orderId, customerData);
                } catch (error) {
                  console.warn('Could not fetch customer:', error);
                }
              }
              
              if (!inventoryData && detailedOrder.inventoryId) {
                try {
                  const inventoryResponse = await inventoryAPI.getInventoryById(detailedOrder.inventoryId);
                  inventoryData = inventoryResponse.data;
                  console.log('Fetched inventory for order:', order.orderId, inventoryData);
                } catch (error) {
                  console.warn('Could not fetch inventory:', error);
                }
              }
              
              // Tính totalAmount từ nhiều nguồn
              let totalAmount = detailedOrder.totalAmount || detailedOrder.orderAmount || detailedOrder.amount || order.totalAmount || order.orderAmount || order.amount;
              
              // Nếu vẫn không có, thử lấy từ quotation
              if (!totalAmount && detailedOrder.quotationId) {
                try {
                  const quotationResponse = await quotationAPI.getQuotation(detailedOrder.quotationId);
                  const quotation = quotationResponse.data;
                  totalAmount = quotation.finalPrice || quotation.totalPrice || 0;
                  console.log('Fetched totalAmount from quotation for order:', order.orderId, totalAmount);
                } catch (error) {
                  console.warn('Could not fetch quotation:', error);
                }
              }
              
              // Nếu vẫn không có, thử lấy từ inventory (ưu tiên variant.priceBase vì sellingPrice thường null)
              if (!totalAmount && inventoryData) {
                totalAmount = inventoryData.variant?.priceBase || inventoryData.sellingPrice || 0;
                if (totalAmount) {
                  console.log('Fetched totalAmount from inventory variant for order:', order.orderId, totalAmount);
                }
              }
              
              // Nếu vẫn không có và có variantId, thử fetch variant trực tiếp
              if (!totalAmount && (detailedOrder.variantId || inventoryData?.variantId)) {
                try {
                  const variantId = detailedOrder.variantId || inventoryData?.variantId;
                  console.log('Fetching variant for order:', order.orderId, 'variantId:', variantId);
                  const variantResponse = await vehicleAPI.getVariant(variantId);
                  const variant = variantResponse.data;
                  totalAmount = variant?.priceBase || 0;
                  if (totalAmount) {
                    console.log('Fetched totalAmount from variant for order:', order.orderId, totalAmount);
                    // Cập nhật inventoryData để có variant
                    if (inventoryData) {
                      inventoryData.variant = variant;
                    }
                  }
                } catch (error) {
                  console.warn('Could not fetch variant:', error);
                }
              }
              
              // Nếu vẫn không có, thử tính từ payments
              if (!totalAmount) {
                try {
                  const paymentsResponse = await paymentAPI.getPaymentsByOrder(order.orderId);
                  const payments = paymentsResponse.data || [];
                  if (payments.length > 0) {
                    totalAmount = payments
                      .filter(p => p.status === 'completed' || p.status === 'pending')
                      .reduce((sum, p) => sum + (parseFloat(p.amount || p.paymentAmount || 0)), 0);
                    if (totalAmount) {
                      console.log('Fetched totalAmount from payments for order:', order.orderId, totalAmount);
                    }
                  }
                } catch (error) {
                  console.warn('Could not fetch payments:', error);
                }
              }
              
              // Merge dữ liệu chi tiết vào order hiện tại
              return {
                ...order,
                ...detailedOrder,
                // Ưu tiên dữ liệu đã fetch
                customer: customerData || detailedOrder.customer || order.customer,
                inventory: inventoryData || detailedOrder.inventory || order.inventory,
                totalAmount: totalAmount || detailedOrder.totalAmount || order.totalAmount || detailedOrder.orderAmount || order.orderAmount
              };
            } catch (error) {
              console.warn('Could not load order details for:', order.orderId, error);
              return order;
            }
          }
          return order;
        })
      );
      
      setOrders(enrichedOrders);
    } catch (error) {
      console.error('Error loading orders:', error);
      console.error('Error response:', error.response);
      toast.error('Không thể tải danh sách đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (order) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa đơn hàng "${order.orderNumber}"?`)) {
      try {
        await orderAPI.deleteOrder(order.orderId);
        toast.success('Xóa đơn hàng thành công');
        loadOrders();
      } catch (error) {
        console.error('Error deleting order:', error);
        toast.error('Không thể xóa đơn hàng');
      }
    }
  };

  const handleEdit = async (order) => {
    try {
      setSelectedOrder(order);
      setModalMode('edit');
      setShowOrderModal(true);
    } catch (error) {
      console.error('Error getting order details:', error);
      toast.error('Không thể tải thông tin đơn hàng');
    }
  };

  const handleView = async (order) => {
    try {
      setSelectedOrder(order);
      setModalMode('view');
      setShowOrderModal(true);
    } catch (error) {
      console.error('Error getting order details:', error);
      toast.error('Không thể tải thông tin đơn hàng');
    }
  };


  // DISABLED: API không tồn tại trong /api/orders
  // const handleConvertToContract = async (order) => {
  //   if (window.confirm(`Bạn có chắc chắn muốn chuyển đổi đơn hàng "${order.orderNumber}" thành hợp đồng?`)) {
  //     try {
  //       await orderAPI.convertToContract(order.orderId);
  //       toast.success('Chuyển đổi thành hợp đồng thành công');
  //       loadOrders();
  //     } catch (error) {
  //       console.error('Error converting order to contract:', error);
  //       toast.error('Không thể chuyển đổi thành hợp đồng');
  //     }
  //   }
  // };

  // DISABLED: cancelOrder chỉ có trong publicOrderAPI, không có trong orderAPI
  // const handleCancelOrder = async (order) => {
  //   if (window.confirm(`Bạn có chắc chắn muốn hủy đơn hàng "${order.orderNumber}"?`)) {
  //     try {
  //       await orderAPI.cancelOrder(order.orderId);
  //       toast.success('Hủy đơn hàng thành công');
  //       loadOrders();
  //     } catch (error) {
  //       console.error('Error cancelling order:', error);
  //       toast.error('Không thể hủy đơn hàng');
  //     }
  //   }
  // };
  
  const handleConvertToContract = undefined; // API không tồn tại
  const handleCancelOrder = undefined; // API không tồn tại

  const handleExportPDF = async (order) => {
    try {
      const response = await orderAPI.exportOrderPDF(order.orderId);
      // Create blob and download
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `order-${order.orderNumber}.pdf`;
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

  // Bước 5: Tạo Quotation từ Order (EVM_STAFF, ADMIN)
  const handleCreateQuotation = async (order) => {
    if (!canCreateQuotation) {
      toast.error('Bạn không có quyền tạo báo giá');
      return;
    }

    if (order.status !== 'pending') {
      toast.error('Chỉ có thể tạo báo giá cho đơn hàng có trạng thái "pending"');
      return;
    }

    if (!order.inventory?.variantId) {
      toast.error('Đơn hàng không có thông tin xe. Vui lòng kiểm tra lại.');
      return;
    }

    if (!order.customer?.customerId) {
      toast.error('Đơn hàng không có thông tin khách hàng. Vui lòng kiểm tra lại.');
      return;
    }

    try {
      setLoading(true);
      
      // Calculate prices from inventory
      const basePrice = order.inventory?.sellingPrice || order.inventory?.variant?.priceBase || order.totalAmount || 0;
      
      // Create quotation according to guide
      // ⚠️ LƯU Ý: QuotationRequest không có orderId - Quotation độc lập với Order (guide line 389)
      // Required fields (theo FIELD_REFERENCE_GUIDE.md line 537-551)
      const quotationData = {
        variantId: order.inventory.variantId, // Required
        totalPrice: basePrice, // Required
        finalPrice: basePrice // Required
      };
      
      // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
      if (order.customer?.customerId) {
        quotationData.customerId = order.customer.customerId;
      }
      if (order.inventory.colorId || order.inventory.color?.colorId) {
        quotationData.colorId = parseInt(order.inventory.colorId || order.inventory.color?.colorId, 10);
      }
      if (order.notes?.trim()) {
        quotationData.notes = order.notes.trim();
      }
      if (basePrice > 0) {
        quotationData.discountAmount = 0; // Có thể set discountAmount nếu cần
      }
      quotationData.validityDays = 30; // Integer, số ngày hiệu lực (default: 7, guide line 350)

      const response = await quotationAPI.createQuotation(quotationData);
      const createdQuotation = response.data;
      
      toast.success(`Báo giá ${createdQuotation.quotationNumber || createdQuotation.quotationId} đã được tạo thành công!`);
      
      // ⚠️ LƯU Ý: Quotation KHÔNG tự động cập nhật Order.status thành "quoted" (guide line 374-375)
      // Nhân viên phải tự cập nhật Order.status thành "quoted" nếu cần
      try {
        await orderAPI.updateOrderStatus(order.orderId, 'quoted');
        toast.success('Đã cập nhật trạng thái đơn hàng thành "quoted"');
      } catch (statusError) {
        console.error('Error updating order status:', statusError);
        toast.error('Đã tạo báo giá nhưng không thể cập nhật trạng thái đơn hàng. Vui lòng cập nhật thủ công.');
      }
      
      // Reload orders to get updated status
      loadOrders();
      
      // Optionally navigate to quotation detail
      // navigate(`/admin/quotations/${createdQuotation.quotationId}`);
    } catch (error) {
      console.error('Error creating quotation:', error);
      toast.error(error.response?.data?.error || 'Không thể tạo báo giá');
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const statusInfo = getStatusBadgeUtil('order', status);
    return <span className={`badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const filteredOrders = orders.filter(order => {
    const searchLower = searchTerm.toLowerCase();
    return (
      order.orderNumber?.toLowerCase().includes(searchLower) ||
      order.customer?.firstName?.toLowerCase().includes(searchLower) ||
      order.customer?.lastName?.toLowerCase().includes(searchLower) ||
      order.customer?.email?.toLowerCase().includes(searchLower) ||
      order.status?.toLowerCase().includes(searchLower)
    );
  });

  const columns = [
    { 
      key: 'orderNumber', 
      header: 'Số đơn hàng',
      render: (order) => (
        <div className="order-number">
          <ShoppingCart size={16} />
          {order.orderNumber || order.orderId || 'N/A'}
        </div>
      )
    },
    { 
      key: 'customer', 
      header: 'Khách hàng',
      render: (order) => {
        const customer = order.customer;
        // Fallback: nếu có customerId nhưng không có customer object, hiển thị ID
        const customerId = order.customerId || customer?.customerId;
        
        const customerName = customer 
          ? `${customer.firstName || ''} ${customer.lastName || ''}`.trim()
          : null;
        const customerEmail = customer?.email;
        
        if (!customerName && !customerEmail && !customerId) {
          return (
            <div className="customer-info">
              <User size={16} />
              <span>N/A</span>
            </div>
          );
        }
        
        return (
          <div className="customer-info">
            <User size={16} />
            <div>
              {customerName ? (
                <>
                  <div className="customer-name">{customerName}</div>
                  {customerEmail && <div className="customer-email">{customerEmail}</div>}
                </>
              ) : customerId ? (
                <div className="customer-name">Customer ID: {customerId}</div>
              ) : (
                <span>N/A</span>
              )}
            </div>
          </div>
        );
      }
    },
    { 
      key: 'vehicle', 
      header: 'Xe đặt mua',
      render: (order) => {
        const inventory = order.inventory;
        const variant = inventory?.variant || order.variant;
        const model = variant?.model || order.model;
        const brand = model?.brand || order.brand;
        
        const brandName = brand?.brandName;
        const modelName = model?.modelName;
        const variantName = variant?.variantName;
        const vin = inventory?.vin || order.vin;
        
        // Fallback: hiển thị ID nếu có
        const inventoryId = order.inventoryId || inventory?.inventoryId;
        const variantId = order.variantId || variant?.variantId;
        
        if (!brandName && !modelName && !variantName && !vin && !inventoryId && !variantId) {
          return (
            <div className="vehicle-info">
              <Package size={16} />
              <span>N/A</span>
            </div>
          );
        }
        
        return (
          <div className="vehicle-info">
            <Package size={16} />
            <div>
              {(brandName || modelName) ? (
                <>
                  {(brandName || modelName) && (
                    <div className="vehicle-name">
                      {brandName} {modelName}
                    </div>
                  )}
                  {variantName && <div className="vehicle-variant">{variantName}</div>}
                  {vin && <div className="vehicle-vin">VIN: {vin}</div>}
                </>
              ) : inventoryId ? (
                <div className="vehicle-name">Inventory ID: {inventoryId}</div>
              ) : variantId ? (
                <div className="vehicle-name">Variant ID: {variantId}</div>
              ) : (
                <span>N/A</span>
              )}
            </div>
          </div>
        );
      }
    },
    { 
      key: 'totalAmount', 
      header: 'Tổng tiền',
      render: (order) => {
        // Debug logging chỉ khi thiếu dữ liệu
        if (!order.totalAmount && !order.orderAmount && !order.amount && !order.finalPrice) {
          console.warn('Order missing amount fields:', order.orderId);
        }
        
        const amount = order.totalAmount || order.orderAmount || order.amount || order.finalPrice;
        return (
          <div className="amount">
            <DollarSign size={16} />
            {amount && Number(amount) > 0 
              ? `${Number(amount).toLocaleString('vi-VN')} VNĐ` 
              : 'N/A'}
          </div>
        );
      }
    },
    { 
      key: 'status', 
      header: 'Trạng thái',
      render: (order) => getStatusBadge(order.status)
    },
    { 
      key: 'orderDate', 
      header: 'Ngày đặt hàng',
      render: (order) => {
        const orderDate = order.orderDate || order.createdAt;
        return (
          <div className="date">
            <Calendar size={16} />
            {orderDate ? new Date(orderDate).toLocaleDateString('vi-VN') : 'N/A'}
          </div>
        );
      }
    }
  ];

  const handleSaveOrder = async (orderId, orderData) => {
    try {
      await orderAPI.updateOrder(orderId, orderData);
      toast.success('Cập nhật đơn hàng thành công');
      loadOrders();
    } catch (error) {
      console.error('Error updating order:', error);
      toast.error('Không thể cập nhật đơn hàng');
      throw error;
    }
  };

  if (loading) {
    return <LoadingSpinner text="Đang tải danh sách đơn hàng..." />;
  }

  return (
    <div className="order-management">
      <div className="page-header">
        <div className="page-title">
          <ShoppingCart className="title-icon" />
          <h1>Quản lý đơn hàng</h1>
        </div>
        <p>Quản lý đơn hàng và trạng thái xử lý</p>
      </div>

      <div className="content">
        <div className="section-header">
          <h2>Danh sách đơn hàng</h2>
          <button className="btn btn-primary">
            <Plus size={20} />
            Tạo đơn hàng mới
          </button>
        </div>

        <DataTable
          data={filteredOrders}
          columns={columns}
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          searchPlaceholder="Tìm kiếm đơn hàng..."
          emptyMessage="Không có đơn hàng nào"
          onEdit={handleEdit}
          onDelete={handleDelete}
          onView={handleView}
          onConvertToContract={handleConvertToContract}
          onCancelOrder={handleCancelOrder}
          onExportPDF={handleExportPDF}
          onCreateQuotation={canCreateQuotation ? handleCreateQuotation : undefined}
        />

        {/* Order Modal */}
        <OrderModal
          order={selectedOrder}
          isOpen={showOrderModal}
          mode={modalMode}
          onClose={() => {
            setShowOrderModal(false);
            setSelectedOrder(null);
            setModalMode('view');
          }}
          onSave={handleSaveOrder}
        />
      </div>
    </div>
  );
};

export default OrderManagement;
