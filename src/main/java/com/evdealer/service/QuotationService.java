package com.evdealer.service;

import com.evdealer.dto.QuotationRequest;
import com.evdealer.entity.*;
import com.evdealer.repository.*;
import com.evdealer.enums.DealerQuotationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evdealer.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class QuotationService {
    
    @Autowired
    private QuotationRepository quotationRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    @Autowired
    private VehicleColorRepository vehicleColorRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    public List<Quotation> getAllQuotations() {
        // Dùng native query để tránh lỗi khi customer/user đã bị xóa
        try {
            List<Quotation> quotations = quotationRepository.findAllNative();
            System.out.println("QuotationService.getAllQuotations() - Found " + quotations.size() + " quotations (native query)");
            return quotations;
        } catch (Exception e) {
            System.err.println("QuotationService.getAllQuotations() - Native query failed: " + e.getMessage());
            e.printStackTrace();
            // Fallback: thử findAllWithRelationships
            try {
                List<Quotation> quotations = quotationRepository.findAllWithRelationships();
                System.out.println("QuotationService.getAllQuotations() - Found " + quotations.size() + " quotations (with relationships)");
                return quotations;
            } catch (Exception e2) {
                System.err.println("QuotationService.getAllQuotations() - findAllWithRelationships also failed: " + e2.getMessage());
                e2.printStackTrace();
                return new java.util.ArrayList<>();
            }
        }
    }
    
    public List<Quotation> getQuotationsByStatus(String status) {
        try {
            DealerQuotationStatus statusEnum = DealerQuotationStatus.fromString(status);
            return quotationRepository.findByStatus(statusEnum);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Quotation> getQuotationsByCustomer(UUID customerId) {
        try {
            return quotationRepository.findByCustomerCustomerId(customerId);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Quotation> getQuotationsByUser(UUID userId) {
        try {
            return quotationRepository.findByUserUserId(userId);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Quotation> getQuotationsByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            return quotationRepository.findByQuotationDateBetween(startDate, endDate);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Quotation> getExpiredQuotations() {
        try {
            LocalDate currentDate = LocalDate.now();
            return quotationRepository.findExpiredQuotations(currentDate, DealerQuotationStatus.PENDING);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public Optional<Quotation> getQuotationById(UUID quotationId) {
        return quotationRepository.findById(quotationId);
    }
    
    public Optional<Quotation> getQuotationByNumber(String quotationNumber) {
        return quotationRepository.findByQuotationNumber(quotationNumber);
    }
    
    public Quotation createQuotation(Quotation quotation) {
        if (quotationRepository.existsByQuotationNumber(quotation.getQuotationNumber())) {
            throw new RuntimeException("Quotation number already exists");
        }
        
        // Validate foreign keys
        if (quotation.getCustomer() != null && quotation.getCustomer().getCustomerId() != null) {
            quotation.setCustomer(customerRepository.findById(quotation.getCustomer().getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + quotation.getCustomer().getCustomerId())));
        }
        
        if (quotation.getUser() != null && quotation.getUser().getUserId() != null) {
            quotation.setUser(userRepository.findById(quotation.getUser().getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + quotation.getUser().getUserId())));
        }
        
        if (quotation.getVariant() != null && quotation.getVariant().getVariantId() != null) {
            quotation.setVariant(vehicleVariantRepository.findById(quotation.getVariant().getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found with id: " + quotation.getVariant().getVariantId())));
        }
        
        if (quotation.getColor() != null && quotation.getColor().getColorId() != null) {
            quotation.setColor(vehicleColorRepository.findById(quotation.getColor().getColorId())
                    .orElseThrow(() -> new RuntimeException("Color not found with id: " + quotation.getColor().getColorId())));
        }
        
        // normalize status using enum (nếu status là String từ DB cũ, sẽ được converter tự động convert)
        if (quotation.getStatus() == null) {
            quotation.setStatus(DealerQuotationStatus.PENDING);
        }
        return quotationRepository.save(quotation);
    }
    
    public Quotation createQuotationFromRequest(QuotationRequest request) {
        // Generate quotation number if not provided
        String quotationNumber = generateQuotationNumber();
        
        // Find related entities
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));
        }
        
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        }
        
        VehicleVariant variant = null;
        if (request.getVariantId() != null) {
            variant = vehicleVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Vehicle variant not found with ID: " + request.getVariantId()));
        }
        
        VehicleColor color = null;
        if (request.getColorId() != null) {
            color = vehicleColorRepository.findById(request.getColorId())
                    .orElseThrow(() -> new RuntimeException("Vehicle color not found with ID: " + request.getColorId()));
        }
        
        // Create quotation entity
        Quotation quotation = new Quotation();
        quotation.setQuotationNumber(quotationNumber);
        quotation.setCustomer(customer);
        quotation.setUser(user);
        quotation.setVariant(variant);
        quotation.setColor(color);
        quotation.setQuotationDate(request.getQuotationDate() != null ? request.getQuotationDate() : LocalDate.now());
        // VALIDATION: totalPrice và finalPrice không được null
        if (request.getTotalPrice() == null) {
            throw new RuntimeException("Total price is required");
        }
        if (request.getFinalPrice() == null) {
            throw new RuntimeException("Final price is required");
        }
        
        // VALIDATION: finalPrice phải <= totalPrice
        if (request.getFinalPrice().compareTo(request.getTotalPrice()) > 0) {
            throw new RuntimeException("Final price cannot be greater than total price");
        }
        
        quotation.setTotalPrice(request.getTotalPrice());
        quotation.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        quotation.setFinalPrice(request.getFinalPrice());
        quotation.setValidityDays(request.getValidityDays() != null ? request.getValidityDays() : 7);
        
        // expiryDate sẽ được tính tự động bởi @PrePersist/@PreUpdate
        
        if (request.getStatus() != null) {
            quotation.setStatus(DealerQuotationStatus.fromString(request.getStatus()));
        } else {
            quotation.setStatus(DealerQuotationStatus.PENDING);
        }
        quotation.setNotes(request.getNotes());
        
        return quotationRepository.save(quotation);
    }
    
    private String generateQuotationNumber() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        int maxAttempts = 10;
        
        // Retry mechanism với entropy cao hơn (6 chữ số thay vì 4)
        for (int i = 0; i < maxAttempts; i++) {
            String randomStr = String.format("%06d", (int) (Math.random() * 1000000));
            String quotationNumber = "QUO-" + dateStr + "-" + randomStr;
            
            // Check if quotation number already exists
            if (!quotationRepository.existsByQuotationNumber(quotationNumber)) {
                return quotationNumber;
            }
        }
        
        // Fallback: dùng UUID nếu vẫn trùng sau maxAttempts lần
        String uuidSuffix = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "QUO-" + dateStr + "-" + uuidSuffix;
    }
    
    public Quotation updateQuotation(UUID quotationId, Quotation quotationDetails) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        
        quotation.setQuotationNumber(quotationDetails.getQuotationNumber());
        quotation.setCustomer(quotationDetails.getCustomer());
        quotation.setUser(quotationDetails.getUser());
        quotation.setVariant(quotationDetails.getVariant());
        quotation.setColor(quotationDetails.getColor());
        quotation.setQuotationDate(quotationDetails.getQuotationDate());
        quotation.setTotalPrice(quotationDetails.getTotalPrice());
        quotation.setDiscountAmount(quotationDetails.getDiscountAmount());
        quotation.setFinalPrice(quotationDetails.getFinalPrice());
        quotation.setValidityDays(quotationDetails.getValidityDays());
        if (quotationDetails.getStatus() != null) {
            quotation.setStatus(quotationDetails.getStatus());
        }
        quotation.setNotes(quotationDetails.getNotes());
        
        return quotationRepository.save(quotation);
    }
    
    public Quotation updateQuotationFromRequest(UUID quotationId, QuotationRequest request) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        
        // Cho phép update khi:
        // - status = "pending" (chưa gửi)
        // - status = "sent" (đã gửi, đang đàm phán)
        // - status = "rejected" (khách từ chối, điều chỉnh lại)
        DealerQuotationStatus currentStatus = quotation.getStatus();
        if (currentStatus != DealerQuotationStatus.PENDING && 
            currentStatus != DealerQuotationStatus.SENT && 
            currentStatus != DealerQuotationStatus.REJECTED) {
            throw new RuntimeException(
                "Quotation can only be updated when status is 'pending', 'sent', or 'rejected'. " +
                "Current status: " + (currentStatus != null ? currentStatus.getValue() : "null")
            );
        }
        
        // Nếu đang ở status "rejected", sau khi update có thể reset về "pending" để gửi lại
        if (currentStatus == DealerQuotationStatus.REJECTED && request.getStatus() != null && 
            DealerQuotationStatus.fromString(request.getStatus()) == DealerQuotationStatus.PENDING) {
            quotation.setRejectedAt(null);
            quotation.setRejectionReason(null);
        }
        
        // Update customer if provided
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));
            quotation.setCustomer(customer);
        }
        
        // Update user if provided
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
            quotation.setUser(user);
        }
        
        // Update variant if provided
        if (request.getVariantId() != null) {
            VehicleVariant variant = vehicleVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Vehicle variant not found with ID: " + request.getVariantId()));
            quotation.setVariant(variant);
        }
        
        // Update color if provided
        if (request.getColorId() != null) {
            VehicleColor color = vehicleColorRepository.findById(request.getColorId())
                    .orElseThrow(() -> new RuntimeException("Vehicle color not found with ID: " + request.getColorId()));
            quotation.setColor(color);
        }
        
        // Update other fields
        if (request.getQuotationDate() != null) {
            quotation.setQuotationDate(request.getQuotationDate());
        }
        if (request.getTotalPrice() != null) {
            quotation.setTotalPrice(request.getTotalPrice());
        }
        if (request.getDiscountAmount() != null) {
            quotation.setDiscountAmount(request.getDiscountAmount());
        }
        if (request.getFinalPrice() != null) {
            quotation.setFinalPrice(request.getFinalPrice());
        }
        if (request.getValidityDays() != null) {
            quotation.setValidityDays(request.getValidityDays());
            // expiryDate sẽ được tính tự động bởi @PrePersist/@PreUpdate
        }
        
        // VALIDATION: Nếu update finalPrice, kiểm tra <= totalPrice
        if (request.getFinalPrice() != null) {
            BigDecimal totalPrice = quotation.getTotalPrice();
            if (request.getFinalPrice().compareTo(totalPrice) > 0) {
                throw new RuntimeException("Final price cannot be greater than total price");
            }
        }
        if (request.getStatus() != null) {
            quotation.setStatus(DealerQuotationStatus.fromString(request.getStatus()));
        }
        if (request.getNotes() != null) {
            quotation.setNotes(request.getNotes());
        }
        
        return quotationRepository.save(quotation);
    }
    
    /**
     * Tạo Quotation từ Order (yêu cầu mua bán)
     * DEALER_STAFF đánh giá nhu cầu và tạo báo giá
     */
    @Transactional
    public Quotation createQuotationFromOrder(UUID orderId, QuotationRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        // Kiểm tra Order status = "pending"
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Quotation can only be created from pending order. Current status: " + order.getStatus().getValue());
        }
        
        // Kiểm tra Order chưa có Quotation (hoặc Quotation đã rejected)
        if (order.getQuotation() != null) {
            DealerQuotationStatus quotationStatus = order.getQuotation().getStatus();
            if (quotationStatus != DealerQuotationStatus.REJECTED) {
                throw new RuntimeException("Order already has an active quotation. Current quotation status: " + 
                    (quotationStatus != null ? quotationStatus.getValue() : "null"));
            }
        }
        
        // Set customerId, variantId, colorId từ Order nếu chưa có trong request
        if (request.getCustomerId() == null && order.getCustomer() != null) {
            request.setCustomerId(order.getCustomer().getCustomerId());
        }
        
        // Lấy variantId và colorId từ Order.inventory nếu có
        if (request.getVariantId() == null && order.getInventory() != null && order.getInventory().getVariant() != null) {
            request.setVariantId(order.getInventory().getVariant().getVariantId());
        }
        if (request.getColorId() == null && order.getInventory() != null && order.getInventory().getColor() != null) {
            request.setColorId(order.getInventory().getColor().getColorId());
        }
        
        // Tạo Quotation với giá phù hợp (dựa trên chi phí, lợi nhuận, khuyến mãi)
        Quotation quotation = createQuotationFromRequest(request);
        
        // Link với Order
        order.setQuotation(quotation);
        order.setStatus(OrderStatus.QUOTED);
        orderRepository.save(order);
        
        return quotation;
    }
    
    /**
     * Gửi báo giá cho khách hàng
     * Báo giá công khai, trực tiếp, có thể kèm chính sách giảm giá
     */
    @Transactional
    public Quotation sendQuotation(UUID quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        // Kiểm tra status = "pending"
        if (quotation.getStatus() != DealerQuotationStatus.PENDING) {
            throw new RuntimeException("Quotation must be in 'pending' status to send. Current status: " + 
                (quotation.getStatus() != null ? quotation.getStatus().getValue() : "null"));
        }
        
        // Chuyển status thành "sent" (đã gửi cho khách)
        quotation.setStatus(DealerQuotationStatus.SENT);
        quotationRepository.save(quotation);
        
        // (Có thể gửi email/notification cho khách)
        
        return quotation;
    }
    
    /**
     * Khách hàng từ chối báo giá (phản hồi giá cao)
     * Có thể kèm lý do và yêu cầu điều chỉnh
     */
    @Transactional
    public Quotation rejectQuotation(UUID quotationId, String reason, String adjustmentRequest) {
        Quotation quotation = quotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        // Kiểm tra status = "sent"
        if (quotation.getStatus() != DealerQuotationStatus.SENT) {
            throw new RuntimeException("Quotation must be in 'sent' status to reject. Current status: " + 
                (quotation.getStatus() != null ? quotation.getStatus().getValue() : "null"));
        }
        
        // Cập nhật status
        quotation.setStatus(DealerQuotationStatus.REJECTED);
        quotation.setRejectedAt(LocalDateTime.now());
        if (reason != null && !reason.trim().isEmpty()) {
            quotation.setRejectionReason(reason);
        }
        
        // Lưu yêu cầu điều chỉnh vào notes (nếu có)
        if (adjustmentRequest != null && !adjustmentRequest.trim().isEmpty()) {
            String currentNotes = quotation.getNotes() != null ? quotation.getNotes() : "";
            quotation.setNotes(currentNotes + "\n[Khách yêu cầu điều chỉnh]: " + adjustmentRequest);
        }
        
        quotationRepository.save(quotation);
        
        // Tìm Order liên quan và cập nhật
        orderRepository.findByQuotationQuotationId(quotationId)
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.PENDING); // Quay lại pending để đàm phán lại
                    orderRepository.save(order);
                });
        
        return quotation;
    }
    
    /**
     * Khách hàng chấp nhận báo giá
     * Có thể đồng ý với điều kiện kèm theo
     */
    @Transactional
    public Order acceptQuotation(UUID quotationId, String conditions) {
        Quotation quotation = quotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        // Kiểm tra status = "sent"
        if (quotation.getStatus() != DealerQuotationStatus.SENT) {
            throw new RuntimeException("Quotation must be in 'sent' status to accept. Current status: " + 
                (quotation.getStatus() != null ? quotation.getStatus().getValue() : "null"));
        }
        
        // Kiểm tra hết hạn (kiểm tra cả trường hợp bằng ngày hôm nay)
        if (quotation.getExpiryDate() != null && 
            !quotation.getExpiryDate().isAfter(LocalDate.now())) {
            quotation.setStatus(DealerQuotationStatus.EXPIRED);
            quotationRepository.save(quotation);
            throw new RuntimeException("Quotation has expired. Expiry date: " + quotation.getExpiryDate());
        }
        
        // Lưu điều kiện kèm theo (nếu có)
        if (conditions != null && !conditions.trim().isEmpty()) {
            String currentNotes = quotation.getNotes() != null ? quotation.getNotes() : "";
            quotation.setNotes(currentNotes + "\n[Điều kiện khách đồng ý]: " + conditions);
        }
        
        // Tìm Order và chuyển thành order chính thức
        Order order = orderRepository.findByQuotationQuotationId(quotationId)
            .orElseThrow(() -> new RuntimeException("Order not found for quotation"));
        
        // VALIDATION: Kiểm tra finalPrice không null
        if (quotation.getFinalPrice() == null) {
            throw new RuntimeException("Quotation final price is required");
        }
        
        // Cập nhật Order trước
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(quotation.getFinalPrice());
        orderRepository.save(order);
        
        // Cập nhật Quotation status = "converted" (bỏ qua "accepted" để đơn giản)
        quotation.setStatus(DealerQuotationStatus.CONVERTED);
        quotation.setAcceptedAt(LocalDateTime.now());
        quotationRepository.save(quotation);
        
        return order;
    }
    
    /**
     * Khách hàng yêu cầu điều chỉnh báo giá
     * Để đạt thỏa thuận tối ưu
     */
    @Transactional
    public Quotation requestQuotationAdjustment(UUID quotationId, String adjustmentRequest) {
        Quotation quotation = quotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        // Kiểm tra status = "sent"
        if (quotation.getStatus() != DealerQuotationStatus.SENT) {
            throw new RuntimeException("Quotation must be in 'sent' status to request adjustment. Current status: " + 
                (quotation.getStatus() != null ? quotation.getStatus().getValue() : "null"));
        }
        
        // Lưu yêu cầu điều chỉnh
        String currentNotes = quotation.getNotes() != null ? quotation.getNotes() : "";
        quotation.setNotes(currentNotes + "\n[Yêu cầu điều chỉnh từ khách]: " + adjustmentRequest);
        
        // Giữ nguyên status "sent" để nhân viên xem xét và điều chỉnh
        
        quotationRepository.save(quotation);
        
        return quotation;
    }
    
    /**
     * Tạo báo giá mới sau khi khách từ chối (đàm phán lại)
     * Nhân viên điều chỉnh giá hoặc điều kiện
     */
    @Transactional
    public Quotation createNewQuotationAfterRejection(UUID rejectedQuotationId, QuotationRequest request) {
        Quotation rejectedQuotation = quotationRepository.findById(rejectedQuotationId)
            .orElseThrow(() -> new RuntimeException("Rejected quotation not found"));
        
        // Kiểm tra quotation đã bị reject
        if (rejectedQuotation.getStatus() != DealerQuotationStatus.REJECTED) {
            throw new RuntimeException("Can only create new quotation from rejected quotation. Current status: " + 
                (rejectedQuotation.getStatus() != null ? rejectedQuotation.getStatus().getValue() : "null"));
        }
        
        // Tìm Order liên quan
        Order order = orderRepository.findByQuotationQuotationId(rejectedQuotationId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Kiểm tra Order chưa có Quotation active khác (đồng bộ với createQuotationFromOrder)
        if (order.getQuotation() != null && !order.getQuotation().getQuotationId().equals(rejectedQuotationId)) {
            DealerQuotationStatus quotationStatus = order.getQuotation().getStatus();
            if (quotationStatus != DealerQuotationStatus.REJECTED) {
                throw new RuntimeException("Order already has an active quotation. Current quotation status: " + 
                    (quotationStatus != null ? quotationStatus.getValue() : "null"));
            }
        }
        
        // Set customerId từ Order nếu chưa có
        if (request.getCustomerId() == null && order.getCustomer() != null) {
            request.setCustomerId(order.getCustomer().getCustomerId());
        }
        
        // Lấy variantId và colorId từ Order.inventory nếu có
        if (request.getVariantId() == null && order.getInventory() != null && order.getInventory().getVariant() != null) {
            request.setVariantId(order.getInventory().getVariant().getVariantId());
        }
        if (request.getColorId() == null && order.getInventory() != null && order.getInventory().getColor() != null) {
            request.setColorId(order.getInventory().getColor().getColorId());
        }
        
        // Tạo quotation mới với giá/điều kiện đã điều chỉnh
        Quotation newQuotation = createQuotationFromRequest(request);
        
        // Link với Order (thay thế quotation cũ)
        order.setQuotation(newQuotation);
        order.setStatus(OrderStatus.QUOTED);
        orderRepository.save(order);
        
        // Lưu reference đến quotation cũ trong notes (để track lịch sử đàm phán)
        String currentNotes = newQuotation.getNotes() != null ? newQuotation.getNotes() : "";
        newQuotation.setNotes(currentNotes + "\n[Đàm phán lại từ quotation]: " + rejectedQuotation.getQuotationNumber());
        quotationRepository.save(newQuotation);
        
        return newQuotation;
    }
    
    public void deleteQuotation(UUID quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + quotationId));
        
        // Kiểm tra xem quotation có đang được Order sử dụng không
        orderRepository.findByQuotationQuotationId(quotationId)
                .ifPresent(order -> {
                    // Không cho phép xóa quotation đang được Order sử dụng
                    // Trừ khi Order đã cancelled hoặc rejected
                    if (order.getStatus() != OrderStatus.CANCELLED && 
                        order.getStatus() != OrderStatus.REJECTED) {
                        throw new RuntimeException(
                            "Cannot delete quotation. It is currently linked to an active order (Order ID: " + 
                            order.getOrderId() + ", Status: " + order.getStatus().getValue() + 
                            "). Please cancel or reject the order first."
                        );
                    }
                    // Nếu Order đã cancelled/rejected, unlink quotation trước khi xóa
                    order.setQuotation(null);
                    orderRepository.save(order);
                });
        
        quotationRepository.delete(quotation);
    }
    
    public Quotation updateQuotationStatus(UUID quotationId, String status) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        // Use DealerQuotationStatus enum for validation and normalization
        DealerQuotationStatus statusEnum = DealerQuotationStatus.fromString(status);
        quotation.setStatus(statusEnum);
        return quotationRepository.save(quotation);
    }
}
