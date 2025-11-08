package com.evdealer.service;

import com.evdealer.dto.CreateDealerOrderRequest;
import com.evdealer.dto.CreateDealerOrderResponse;
import com.evdealer.entity.DealerOrder;
import com.evdealer.enums.DealerOrderStatus;
import com.evdealer.enums.DealerOrderType;
import com.evdealer.enums.Priority;
import com.evdealer.enums.ApprovalStatus;
import com.evdealer.enums.DealerOrderItemStatus;
import com.evdealer.entity.DealerOrderItem;
import com.evdealer.entity.Dealer;
import com.evdealer.entity.User;
import com.evdealer.entity.VehicleVariant;
import com.evdealer.entity.VehicleColor;
import com.evdealer.repository.DealerOrderRepository;
import com.evdealer.repository.DealerRepository;
import com.evdealer.repository.UserRepository;
import com.evdealer.repository.VehicleVariantRepository;
import com.evdealer.repository.VehicleColorRepository;
import com.evdealer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DealerOrderService {
    
    @Autowired
    private DealerOrderRepository dealerOrderRepository;
    
    @Autowired
    private DealerRepository dealerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    @Autowired
    private VehicleColorRepository vehicleColorRepository;
    
    @Autowired
    private DealerOrderItemService dealerOrderItemService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @Autowired
    private PricingPolicyService pricingPolicyService;
    
    @Transactional(readOnly = true)
    public List<DealerOrder> getAllDealerOrders() {
        try {
            // Filter by dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID dealerId = currentUser.getDealer().getDealerId();
                    // Use findByDealerId which already has LEFT JOIN FETCH
                    return dealerOrderRepository.findByDealerId(dealerId);
                }
            }
            // Use findAllWithDetails which already has LEFT JOIN FETCH for dealer
            return dealerOrderRepository.findAllWithDetails();
        } catch (Exception e) {
            // Log error and return empty list
            return new java.util.ArrayList<>();
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<DealerOrder> getDealerOrderById(UUID dealerOrderId) {
        // Use findByIdWithDetails to eagerly load dealer and evmStaff
        return dealerOrderRepository.findByIdWithDetails(dealerOrderId);
    }
    
    public Optional<DealerOrder> getDealerOrderByOrderNumber(String orderNumber) {
        return dealerOrderRepository.findByDealerOrderNumber(orderNumber);
    }
    
    public List<DealerOrder> getDealerOrdersByEvmStaff(UUID evmStaffId) {
        return dealerOrderRepository.findByEvmStaffUserId(evmStaffId);
    }
    
    @Transactional(readOnly = true)
    public List<DealerOrder> getDealerOrdersByStatus(String status) {
        try {
            // Convert string to enum for validation
            DealerOrderStatus statusEnum = DealerOrderStatus.fromString(status);
            List<DealerOrder> orders = dealerOrderRepository.findByStatus(statusEnum);
            return orders;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get orders by status: " + e.getMessage(), e);
        }
    }
    
    public List<DealerOrder> getDealerOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return dealerOrderRepository.findByOrderDateBetween(startDate, endDate);
    }
    
    public DealerOrder createDealerOrder(DealerOrder dealerOrder) {
        if (dealerOrderRepository.existsByDealerOrderNumber(dealerOrder.getDealerOrderNumber())) {
            throw new RuntimeException("Dealer order number already exists: " + dealerOrder.getDealerOrderNumber());
        }
        return dealerOrderRepository.save(dealerOrder);
    }
    
    public DealerOrder updateDealerOrder(UUID dealerOrderId, DealerOrder dealerOrderDetails) {
        DealerOrder dealerOrder = dealerOrderRepository.findById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer order not found with id: " + dealerOrderId));
        
        // Check for duplicate order number (excluding current order)
        if (!dealerOrder.getDealerOrderNumber().equals(dealerOrderDetails.getDealerOrderNumber()) && 
            dealerOrderRepository.existsByDealerOrderNumber(dealerOrderDetails.getDealerOrderNumber())) {
            throw new RuntimeException("Dealer order number already exists: " + dealerOrderDetails.getDealerOrderNumber());
        }
        
        dealerOrder.setDealerOrderNumber(dealerOrderDetails.getDealerOrderNumber());
        dealerOrder.setEvmStaff(dealerOrderDetails.getEvmStaff());
        dealerOrder.setOrderDate(dealerOrderDetails.getOrderDate());
        dealerOrder.setExpectedDeliveryDate(dealerOrderDetails.getExpectedDeliveryDate());
        dealerOrder.setTotalQuantity(dealerOrderDetails.getTotalQuantity());
        dealerOrder.setTotalAmount(dealerOrderDetails.getTotalAmount());
        dealerOrder.setStatus(dealerOrderDetails.getStatus());
        dealerOrder.setPriority(dealerOrderDetails.getPriority());
        dealerOrder.setNotes(dealerOrderDetails.getNotes());
        
        return dealerOrderRepository.save(dealerOrder);
    }
    
    public void deleteDealerOrder(UUID dealerOrderId) {
        DealerOrder dealerOrder = dealerOrderRepository.findById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer order not found with id: " + dealerOrderId));
        dealerOrderRepository.delete(dealerOrder);
    }
    
    public DealerOrder updateDealerOrderStatus(UUID dealerOrderId, String status) {
        DealerOrder dealerOrder = dealerOrderRepository.findById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer order not found with id: " + dealerOrderId));
        DealerOrderStatus statusEnum = DealerOrderStatus.fromString(status);
        dealerOrder.setStatus(statusEnum);
        return dealerOrderRepository.save(dealerOrder);
    }
    
    // ==================== NEW IMPROVED METHODS ====================
    
    public CreateDealerOrderResponse createDetailedDealerOrder(CreateDealerOrderRequest request) {
        CreateDealerOrderResponse response = new CreateDealerOrderResponse();
        
        try {
            // Validate dealer exists
            Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + request.getDealerId()));
            
            // Validate EVM staff exists (if provided)
            User evmStaff = null;
            if (request.getEvmStaffId() != null) {
                evmStaff = userRepository.findById(request.getEvmStaffId())
                    .orElseThrow(() -> new RuntimeException("EVM staff not found with ID: " + request.getEvmStaffId()));
            }
            
            // Create dealer order
            DealerOrder dealerOrder = new DealerOrder();
            dealerOrder.setDealerOrderNumber(generateOrderNumber());
            dealerOrder.setDealer(dealer);
            dealerOrder.setEvmStaff(evmStaff);
            dealerOrder.setOrderDate(request.getOrderDate());
            dealerOrder.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
            
            // Set orderType using enum (with validation)
            if (request.getOrderType() != null) {
                DealerOrderType orderType = DealerOrderType.fromString(request.getOrderType());
                if (!DealerOrderType.isValid(request.getOrderType())) {
                    throw new RuntimeException("Invalid orderType: " + request.getOrderType() + ". Must be PURCHASE, RESERVE, or SAMPLE");
                }
                dealerOrder.setOrderType(orderType);
            } else {
                dealerOrder.setOrderType(DealerOrderType.PURCHASE);
            }
            
            // Set priority using enum (with validation)
            if (request.getPriority() != null) {
                Priority priority = Priority.fromString(request.getPriority());
                if (!Priority.isValid(request.getPriority())) {
                    throw new RuntimeException("Invalid priority: " + request.getPriority() + ". Must be LOW, NORMAL, HIGH, or URGENT");
                }
                dealerOrder.setPriority(priority);
            } else {
                dealerOrder.setPriority(Priority.NORMAL);
            }
            if (request.getPaymentTerms() != null) {
                dealerOrder.setPaymentTerms(request.getPaymentTerms());
            }
            if (request.getDeliveryTerms() != null) {
                dealerOrder.setDeliveryTerms(request.getDeliveryTerms());
            }
            dealerOrder.setNotes(request.getNotes());
            dealerOrder.setStatus(DealerOrderStatus.PENDING);
            dealerOrder.setApprovalStatus(ApprovalStatus.PENDING);
            
            // Initialize totals
            dealerOrder.setTotalQuantity(0);
            dealerOrder.setTotalAmount(BigDecimal.ZERO);
            
            // Save order first
            DealerOrder savedOrder = dealerOrderRepository.save(dealerOrder);
            
            // Create order items
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalQuantity = 0;
            
            for (CreateDealerOrderRequest.DealerOrderItemRequest itemRequest : request.getItems()) {
                // Validate variant exists
                VehicleVariant variant = vehicleVariantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found with ID: " + itemRequest.getVariantId()));
                
                // Validate color exists
                VehicleColor color = vehicleColorRepository.findById(itemRequest.getColorId())
                    .orElseThrow(() -> new RuntimeException("Color not found with ID: " + itemRequest.getColorId()));
                
                // Create order item
                DealerOrderItem item = new DealerOrderItem();
                item.setDealerOrder(savedOrder);
                item.setVariant(variant);
                item.setColor(color);
                item.setQuantity(itemRequest.getQuantity());
                
                // Tích hợp PricingPolicy: Tự động áp dụng giá và chiết khấu từ hãng
                java.util.Optional<com.evdealer.entity.PricingPolicy> policyOpt = 
                    pricingPolicyService.getActivePolicyForVariantAndDealer(variant.getVariantId(), dealer.getDealerId());
                
                if (policyOpt.isPresent()) {
                    com.evdealer.entity.PricingPolicy policy = policyOpt.get();
                    // Áp dụng basePrice từ policy nếu có, nếu không dùng giá từ request hoặc variant
                    if (policy.getBasePrice() != null && policy.getBasePrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
                        item.setUnitPrice(itemRequest.getUnitPrice() != null ? itemRequest.getUnitPrice() : policy.getBasePrice());
                    } else {
                        item.setUnitPrice(itemRequest.getUnitPrice() != null ? itemRequest.getUnitPrice() : variant.getPriceBase());
                    }
                    
                    // Áp dụng discount từ policy nếu request không có discount
                    if (itemRequest.getDiscountPercentage() == null && policy.getDiscountPercent() != null) {
                        item.setDiscountPercentage(policy.getDiscountPercent());
                    } else {
                        item.setDiscountPercentage(itemRequest.getDiscountPercentage());
                    }
                } else {
                    // Không có policy, dùng giá từ request hoặc variant
                    item.setUnitPrice(itemRequest.getUnitPrice() != null ? itemRequest.getUnitPrice() : variant.getPriceBase());
                    item.setDiscountPercentage(itemRequest.getDiscountPercentage());
                }
                
                item.setNotes(itemRequest.getNotes());
                item.setStatus(DealerOrderItemStatus.PENDING);
                
                // Calculate prices
                item.calculatePrices();
                
                // Save item
                dealerOrderItemService.createDealerOrderItem(item);
                
                totalAmount = totalAmount.add(item.getFinalPrice());
                totalQuantity += item.getQuantity();
            }
            
            // Update order totals
            savedOrder.setTotalQuantity(totalQuantity);
            savedOrder.setTotalAmount(totalAmount);
            dealerOrderRepository.save(savedOrder);
            
            // Create response
            response.setSuccess(true);
            response.setMessage("Dealer order created successfully");
            response.setDealerOrder(mapToDealerOrderInfo(savedOrder));
            response.setTotalQuantity(totalQuantity);
            response.setTotalAmount(totalAmount);
            response.setItems(mapToItemInfoList(dealerOrderItemService.getItemsByDealerOrderId(savedOrder.getDealerOrderId())));
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Failed to create dealer order: " + e.getMessage());
            throw new RuntimeException("Failed to create dealer order: " + e.getMessage());
        }
        
        return response;
    }
    
    public void recalculateOrderTotals(UUID dealerOrderId) {
        DealerOrder dealerOrder = dealerOrderRepository.findById(dealerOrderId)
            .orElseThrow(() -> new RuntimeException("Dealer order not found with ID: " + dealerOrderId));
        
        List<DealerOrderItem> items = dealerOrderItemService.getItemsByDealerOrderId(dealerOrderId);
        
        BigDecimal totalAmount = dealerOrderItemService.calculateTotalAmount(items);
        Integer totalQuantity = dealerOrderItemService.calculateTotalQuantity(items);
        
        dealerOrder.setTotalAmount(totalAmount);
        dealerOrder.setTotalQuantity(totalQuantity);
        
        dealerOrderRepository.save(dealerOrder);
    }
    
    public DealerOrder approveDealerOrder(UUID dealerOrderId, UUID approvedBy) {
        // Use findByIdWithDetails to eagerly load dealer and evmStaff
        DealerOrder dealerOrder = dealerOrderRepository.findByIdWithDetails(dealerOrderId)
            .orElseThrow(() -> new RuntimeException("Dealer order not found with ID: " + dealerOrderId));
        
        if (dealerOrder.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Order is not in pending status for approval");
        }
        
        dealerOrder.setApprovalStatus(ApprovalStatus.APPROVED);
        dealerOrder.setApprovedBy(approvedBy);
        dealerOrder.setApprovedAt(LocalDateTime.now());
        dealerOrder.setStatus(DealerOrderStatus.CONFIRMED);
        
        // Update all items to CONFIRMED
        List<DealerOrderItem> items = dealerOrderItemService.getItemsByDealerOrderId(dealerOrderId);
        for (DealerOrderItem item : items) {
            item.setStatus(DealerOrderItemStatus.CONFIRMED);
            dealerOrderItemService.updateDealerOrderItem(item.getItemId(), item);
        }
        
        return dealerOrderRepository.save(dealerOrder);
    }
    
    public DealerOrder rejectDealerOrder(UUID dealerOrderId, String rejectionReason) {
        DealerOrder dealerOrder = dealerOrderRepository.findById(dealerOrderId)
            .orElseThrow(() -> new RuntimeException("Dealer order not found with ID: " + dealerOrderId));
        
        if (dealerOrder.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Order is not in pending status for rejection");
        }
        
        dealerOrder.setApprovalStatus(ApprovalStatus.REJECTED);
        dealerOrder.setRejectionReason(rejectionReason);
        dealerOrder.setStatus(DealerOrderStatus.CANCELLED);
        
        // Update all items to CANCELLED
        List<DealerOrderItem> items = dealerOrderItemService.getItemsByDealerOrderId(dealerOrderId);
        for (DealerOrderItem item : items) {
            item.setStatus(DealerOrderItemStatus.CANCELLED);
            dealerOrderItemService.updateDealerOrderItem(item.getItemId(), item);
        }
        
        return dealerOrderRepository.save(dealerOrder);
    }
    
    public Map<String, Object> getDealerOrderSummary(UUID dealerOrderId) {
        DealerOrder dealerOrder = dealerOrderRepository.findById(dealerOrderId)
            .orElseThrow(() -> new RuntimeException("Dealer order not found with ID: " + dealerOrderId));
        
        List<DealerOrderItem> items = dealerOrderItemService.getItemsByDealerOrderId(dealerOrderId);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("dealerOrderId", dealerOrder.getDealerOrderId());
        summary.put("dealerOrderNumber", dealerOrder.getDealerOrderNumber());
        summary.put("dealerName", dealerOrder.getDealer().getDealerName());
        summary.put("orderDate", dealerOrder.getOrderDate());
        summary.put("expectedDeliveryDate", dealerOrder.getExpectedDeliveryDate());
        summary.put("status", dealerOrder.getStatus() != null ? dealerOrder.getStatus().getValue() : null);
        summary.put("approvalStatus", dealerOrder.getApprovalStatus() != null ? dealerOrder.getApprovalStatus().getValue() : null);
        summary.put("totalQuantity", dealerOrder.getTotalQuantity());
        summary.put("totalAmount", dealerOrder.getTotalAmount());
        summary.put("itemCount", items.size());
        summary.put("priority", dealerOrder.getPriority() != null ? dealerOrder.getPriority().getValue() : null);
        summary.put("orderType", dealerOrder.getOrderType() != null ? dealerOrder.getOrderType().getValue() : null);
        
        return summary;
    }
    
    @Transactional(readOnly = true)
    public List<DealerOrder> getOrdersByApprovalStatus(ApprovalStatus approvalStatus) {
        try {
            List<DealerOrder> orders = dealerOrderRepository.findByApprovalStatus(approvalStatus);
            return orders;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get orders by approval status: " + e.getMessage(), e);
        }
    }
    
    // Overloaded method for backward compatibility (accepts String)
    @Transactional(readOnly = true)
    public List<DealerOrder> getOrdersByApprovalStatus(String approvalStatus) {
        ApprovalStatus statusEnum = ApprovalStatus.fromString(approvalStatus);
        return getOrdersByApprovalStatus(statusEnum);
    }
    
    private String generateOrderNumber() {
        return "DO-" + System.currentTimeMillis();
    }
    
    private CreateDealerOrderResponse.DealerOrderInfo mapToDealerOrderInfo(DealerOrder order) {
        CreateDealerOrderResponse.DealerOrderInfo info = new CreateDealerOrderResponse.DealerOrderInfo();
        info.setDealerOrderId(order.getDealerOrderId());
        info.setDealerOrderNumber(order.getDealerOrderNumber());
        info.setDealer(mapToDealerInfo(order.getDealer()));
        if (order.getEvmStaff() != null) {
            info.setEvmStaff(mapToEvmStaffInfo(order.getEvmStaff()));
        }
        info.setOrderDate(order.getOrderDate());
        info.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        info.setStatus(order.getStatus() != null ? order.getStatus().getValue() : null);
        info.setPriority(order.getPriority() != null ? order.getPriority().getValue() : null);
        info.setOrderType(order.getOrderType() != null ? order.getOrderType().getValue() : null);
        info.setApprovalStatus(order.getApprovalStatus() != null ? order.getApprovalStatus().getValue() : null);
        info.setRejectionReason(order.getRejectionReason());
        info.setCreatedAt(order.getCreatedAt());
        return info;
    }
    
    private CreateDealerOrderResponse.DealerInfo mapToDealerInfo(Dealer dealer) {
        CreateDealerOrderResponse.DealerInfo info = new CreateDealerOrderResponse.DealerInfo();
        info.setDealerId(dealer.getDealerId());
        info.setDealerName(dealer.getDealerName());
        info.setDealerCode(dealer.getDealerCode());
        info.setAddress(dealer.getAddress());
        info.setPhone(dealer.getPhone());
        info.setEmail(dealer.getEmail());
        return info;
    }
    
    private CreateDealerOrderResponse.EvmStaffInfo mapToEvmStaffInfo(User user) {
        CreateDealerOrderResponse.EvmStaffInfo info = new CreateDealerOrderResponse.EvmStaffInfo();
        info.setUserId(user.getUserId());
        info.setFullName(user.getFirstName() + " " + user.getLastName());
        info.setEmail(user.getEmail());
        info.setPhone(user.getPhone());
        return info;
    }
    
    private List<CreateDealerOrderResponse.DealerOrderItemInfo> mapToItemInfoList(List<DealerOrderItem> items) {
        return items.stream()
            .map(this::mapToItemInfo)
            .toList();
    }
    
    private CreateDealerOrderResponse.DealerOrderItemInfo mapToItemInfo(DealerOrderItem item) {
        CreateDealerOrderResponse.DealerOrderItemInfo info = new CreateDealerOrderResponse.DealerOrderItemInfo();
        info.setItemId(item.getItemId());
        info.setVariant(mapToVariantInfo(item.getVariant()));
        info.setColor(mapToColorInfo(item.getColor()));
        info.setQuantity(item.getQuantity());
        info.setUnitPrice(item.getUnitPrice());
        info.setTotalPrice(item.getTotalPrice());
        info.setDiscountPercentage(item.getDiscountPercentage());
        info.setDiscountAmount(item.getDiscountAmount());
        info.setFinalPrice(item.getFinalPrice());
        info.setStatus(item.getStatus() != null ? item.getStatus().getValue() : null);
        info.setNotes(item.getNotes());
        return info;
    }
    
    private CreateDealerOrderResponse.VariantInfo mapToVariantInfo(VehicleVariant variant) {
        CreateDealerOrderResponse.VariantInfo info = new CreateDealerOrderResponse.VariantInfo();
        info.setVariantId(variant.getVariantId());
        info.setVariantName(variant.getVariantName());
        info.setPriceBase(variant.getPriceBase());
        info.setBatteryCapacity(variant.getBatteryCapacity());
        info.setRangeKm(variant.getRangeKm());
        info.setPowerKw(variant.getPowerKw());
        info.setModel(mapToModelInfo(variant.getModel()));
        return info;
    }
    
    private CreateDealerOrderResponse.ModelInfo mapToModelInfo(com.evdealer.entity.VehicleModel model) {
        CreateDealerOrderResponse.ModelInfo info = new CreateDealerOrderResponse.ModelInfo();
        info.setModelId(model.getModelId());
        info.setModelName(model.getModelName());
        info.setVehicleType(model.getVehicleType());
        info.setModelYear(model.getModelYear());
        info.setBrand(mapToBrandInfo(model.getBrand()));
        return info;
    }
    
    private CreateDealerOrderResponse.BrandInfo mapToBrandInfo(com.evdealer.entity.VehicleBrand brand) {
        CreateDealerOrderResponse.BrandInfo info = new CreateDealerOrderResponse.BrandInfo();
        info.setBrandId(brand.getBrandId());
        info.setBrandName(brand.getBrandName());
        info.setCountry(brand.getCountry());
        return info;
    }
    
    private CreateDealerOrderResponse.ColorInfo mapToColorInfo(VehicleColor color) {
        CreateDealerOrderResponse.ColorInfo info = new CreateDealerOrderResponse.ColorInfo();
        info.setColorId(color.getColorId());
        info.setColorName(color.getColorName());
        info.setColorCode(color.getColorCode());
        return info;
    }
}

