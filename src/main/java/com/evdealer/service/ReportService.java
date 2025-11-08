package com.evdealer.service;

import com.evdealer.dto.*;
import com.evdealer.entity.*;
import com.evdealer.enums.OrderStatus;
import com.evdealer.enums.VehicleStatus;
import com.evdealer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class ReportService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleInventoryRepository vehicleInventoryRepository;
    
    @Autowired
    private DealerTargetRepository dealerTargetRepository;
    
    @Autowired
    private VehicleDeliveryRepository vehicleDeliveryRepository;
    
    @Autowired
    private InstallmentPlanRepository installmentPlanRepository;

    // Map Order -> OrderDTO (local mapper for report outputs)
    private OrderDTO toOrderDTO(Order o) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(o.getOrderId());
        dto.setOrderNumber(o.getOrderNumber());
        dto.setCustomerId(o.getCustomer() != null ? o.getCustomer().getCustomerId() : null);
        dto.setUserId(o.getUser() != null ? o.getUser().getUserId() : null);
        dto.setInventoryId(o.getInventory() != null ? o.getInventory().getInventoryId() : null);
        dto.setOrderDate(o.getOrderDate());
        dto.setStatus(o.getStatus() != null ? o.getStatus().getValue() : null);
        dto.setTotalAmount(o.getTotalAmount());
        return dto;
    }
    
    // Sales Report by Staff - Using actual Order and User data
    public List<SalesByStaffItemDTO> getSalesReportByStaff() {
        List<User> salesStaff = userRepository.findByUserType(com.evdealer.enums.UserType.DEALER_STAFF);
        List<SalesByStaffItemDTO> items = new ArrayList<>();

        for (User staff : salesStaff) {
            List<Order> staffOrders = orderRepository.findByUserId(staff.getUserId());
            BigDecimal totalSales = staffOrders.stream()
                .map(Order::getTotalAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            SalesByStaffItemDTO dto = new SalesByStaffItemDTO(
                staff.getUserId(),
                staff.getFirstName() + " " + staff.getLastName(),
                staff.getUserType() != null ? staff.getUserType().toString() : "UNKNOWN",
                staffOrders.size(),
                totalSales
            );
            items.add(dto);
        }

        items.sort(Comparator.comparing(SalesByStaffItemDTO::getTotalSales).reversed());
        return items;
    }
    
    public List<SalesByStaffItemDTO> getSalesReportByRole(String roleString) {
        try {
            com.evdealer.enums.UserType userType = com.evdealer.enums.UserType.valueOf(roleString.toUpperCase());
            List<User> users = userRepository.findByRoleString(userType);
            List<SalesByStaffItemDTO> items = new ArrayList<>();

            for (User user : users) {
                List<Order> userOrders = orderRepository.findByUserId(user.getUserId());
                BigDecimal totalSales = userOrders.stream()
                    .map(Order::getTotalAmount)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                SalesByStaffItemDTO dto = new SalesByStaffItemDTO(
                    user.getUserId(),
                    user.getFirstName() + " " + user.getLastName(),
                    user.getUserType() != null ? user.getUserType().toString() : "UNKNOWN",
                    userOrders.size(),
                    totalSales
                );
                items.add(dto);
            }

            items.sort(Comparator.comparing(SalesByStaffItemDTO::getTotalSales).reversed());
            return items;
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }
    
    // Customer Debt Report - Using InstallmentPlan data
    public List<CustomerDebtItemDTO> getCustomerDebtReport() {
        List<InstallmentPlan> activePlans = installmentPlanRepository.findByPlanStatus("active");
        List<CustomerDebtItemDTO> items = new ArrayList<>();

        for (InstallmentPlan plan : activePlans) {
            if (plan.getCustomer() != null) {
                CustomerDebtItemDTO dto = new CustomerDebtItemDTO();
                dto.setCustomerId(plan.getCustomer().getCustomerId());
                dto.setCustomerName(plan.getCustomer().getFirstName() + " " + plan.getCustomer().getLastName());
                dto.setTotalAmount(plan.getTotalAmount());
                dto.setPaidAmount(plan.getDownPaymentAmount());
                dto.setRemainingAmount(plan.getTotalAmount().subtract(plan.getDownPaymentAmount()));
                dto.setInstallmentCount(plan.getLoanTermMonths());
                dto.setPlanType(plan.getPlanType());
                items.add(dto);
            }
        }
        return items;
    }
    
    public List<InstallmentPlan> getCustomersWithActiveInstallments() {
        return installmentPlanRepository.findByPlanStatus("active");
    }
    
    // Inventory Turnover Report - Using VehicleInventory data
    public InventoryTurnoverReportDTO getInventoryTurnoverReport() {
        List<VehicleInventory> allInventory = vehicleInventoryRepository.findAll();
        InventoryTurnoverReportDTO dto = new InventoryTurnoverReportDTO();

        long availableCount = allInventory.stream()
            .filter(inv -> inv.getStatus() == VehicleStatus.AVAILABLE)
            .count();
        
        long soldCount = allInventory.stream()
            .filter(inv -> inv.getStatus() == VehicleStatus.SOLD)
            .count();
        
        long reservedCount = allInventory.stream()
            .filter(inv -> inv.getStatus() == VehicleStatus.RESERVED)
            .count();
        
        dto.setTotalInventory(allInventory.size());
        dto.setAvailableCount(availableCount);
        dto.setSoldCount(soldCount);
        dto.setReservedCount(reservedCount);
        dto.setTurnoverRate(allInventory.size() > 0 ? (double) soldCount / allInventory.size() : 0.0);

        return dto;
    }
    
    public List<VehicleInventory> getAvailableInventory() {
        return vehicleInventoryRepository.findByStatus(VehicleStatus.AVAILABLE);
    }
    
    public List<VehicleInventory> getSoldInventory() {
        return vehicleInventoryRepository.findByStatus(VehicleStatus.SOLD);
    }
    
    // Dealer Performance Report - Using DealerTarget data
    public List<DealerPerformanceItemDTO> getDealerPerformanceReport() {
        List<DealerTarget> allTargets = dealerTargetRepository.findAll();
        List<DealerPerformanceItemDTO> items = new ArrayList<>();
        
        for (DealerTarget target : allTargets) {
            if (target.getDealer() != null) {
                DealerPerformanceItemDTO dto = new DealerPerformanceItemDTO();
                dto.setDealerId(target.getDealer().getDealerId());
                dto.setDealerName(target.getDealer().getDealerName());
                dto.setTargetYear(target.getTargetYear());
                dto.setTargetRevenue(target.getTargetAmount());
                
                // Calculate achievement rate (simplified)
                // Get all users from this dealer and sum their orders
                List<User> dealerUsers = userRepository.findByDealerDealerId(target.getDealer().getDealerId());
                BigDecimal actualSales = BigDecimal.ZERO;
                for (User dealerUser : dealerUsers) {
                    List<Order> userOrders = orderRepository.findByUserId(dealerUser.getUserId());
                    BigDecimal userSales = userOrders.stream()
                        .map(Order::getTotalAmount)
                        .filter(v -> v != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    actualSales = actualSales.add(userSales);
                }
                
                double achievementRate = target.getTargetAmount().doubleValue() > 0 
                    ? (actualSales.doubleValue() / target.getTargetAmount().doubleValue()) * 100 
                    : 0.0;
                
                dto.setActualSales(actualSales);
                dto.setAchievementRate(achievementRate);
                items.add(dto);
            }
        }
        return items;
    }
    
    public List<DealerTarget> getPerformanceByYear(Integer year) {
        return dealerTargetRepository.findByTargetYear(year);
    }
    
    // Monthly Sales Summary - Using Order data
    public MonthlySalesSummaryDTO getMonthlySalesSummary(Integer year, Integer month) {
        List<Order> orders = orderRepository.findAll();
        MonthlySalesSummaryDTO summary = new MonthlySalesSummaryDTO();
        
        long monthlyOrders = orders.stream()
            .filter(order -> order.getOrderDate() != null 
                && order.getOrderDate().getYear() == year 
                && order.getOrderDate().getMonthValue() == month)
            .count();
        
        BigDecimal monthlyRevenue = orders.stream()
            .filter(order -> order.getOrderDate() != null 
                && order.getOrderDate().getYear() == year 
                && order.getOrderDate().getMonthValue() == month)
            .map(Order::getTotalAmount)
            .filter(v -> v != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summary.setYear(year);
        summary.setMonth(month);
        summary.setTotalOrders(monthlyOrders);
        summary.setTotalRevenue(monthlyRevenue);

        return summary;
    }
    
    public YearRangeSalesSummaryDTO getSalesByYearRange(Integer startYear, Integer endYear) {
        List<Order> orders = orderRepository.findAll();
        YearRangeSalesSummaryDTO summary = new YearRangeSalesSummaryDTO();
        
        long totalOrders = orders.stream()
            .filter(order -> order.getOrderDate() != null 
                && order.getOrderDate().getYear() >= startYear 
                && order.getOrderDate().getYear() <= endYear)
            .count();
        
        BigDecimal totalRevenue = orders.stream()
            .filter(order -> order.getOrderDate() != null 
                && order.getOrderDate().getYear() >= startYear 
                && order.getOrderDate().getYear() <= endYear)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summary.setStartYear(startYear);
        summary.setEndYear(endYear);
        summary.setTotalOrders(totalOrders);
        summary.setTotalRevenue(totalRevenue);
        
        return summary;
    }
    
    // Delivery Tracking Report - Using VehicleDelivery data
    public List<VehicleDelivery> getAllDeliveryTrackingReports() {
        return vehicleDeliveryRepository.findAll();
    }
    
    public List<VehicleDeliveryDTO> getDeliveriesByStatus(String status) {
        com.evdealer.enums.VehicleDeliveryStatus statusEnum = com.evdealer.enums.VehicleDeliveryStatus.fromString(status);
        return vehicleDeliveryRepository.findByDeliveryStatus(statusEnum)
                .stream().map(this::toDeliveryDTO).toList();
    }
    
    public List<VehicleDeliveryDTO> getDeliveriesByDate(LocalDate date) {
        return vehicleDeliveryRepository.findByDeliveryDate(date)
                .stream().map(this::toDeliveryDTO).toList();
    }
    
    public List<VehicleDeliveryDTO> getDeliveriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return vehicleDeliveryRepository.findByDeliveryDateBetween(startDate, endDate)
                .stream().map(this::toDeliveryDTO).toList();
    }
    
    public List<VehicleDeliveryDTO> getDeliveriesByCustomer(String customerName) {
        return vehicleDeliveryRepository.findByCustomerNameContainingIgnoreCase(customerName)
                .stream().map(this::toDeliveryDTO).toList();
    }

    // Walk-in purchases: Orders without quotations (assumed walk-in) and with a linked customer
    public List<OrderDTO> getWalkInPurchases() {
        return orderRepository.findWalkInOrders().stream().map(this::toOrderDTO).toList();
    }

    public List<OrderDTO> getWalkInPurchases(LocalDate startDate, LocalDate endDate, String status) {
        OrderStatus statusEnum = (status != null && !status.trim().isEmpty()) 
            ? OrderStatus.fromString(status) 
            : null;
        return orderRepository.findWalkInOrdersFiltered(startDate, endDate, statusEnum)
                .stream().map(this::toOrderDTO).toList();
    }

    public Page<OrderDTO> getWalkInPurchasesPaged(LocalDate startDate, LocalDate endDate, String status, Pageable pageable) {
        OrderStatus statusEnum = (status != null && !status.trim().isEmpty()) 
            ? OrderStatus.fromString(status) 
            : null;
        return orderRepository.findWalkInOrdersFiltered(startDate, endDate, statusEnum, pageable)
                .map(this::toOrderDTO);
    }

    private VehicleDeliveryDTO toDeliveryDTO(VehicleDelivery d) {
        VehicleDeliveryDTO dto = new VehicleDeliveryDTO();
        dto.setDeliveryId(d.getDeliveryId());
        dto.setOrderId(d.getOrder() != null ? d.getOrder().getOrderId() : null);
        dto.setInventoryId(d.getInventory() != null ? d.getInventory().getInventoryId() : null);
        dto.setCustomerId(d.getCustomer() != null ? d.getCustomer().getCustomerId() : null);
        dto.setDeliveryDate(d.getDeliveryDate());
        dto.setDeliveryStatus(d.getDeliveryStatus() != null ? d.getDeliveryStatus().getValue() : null);
        dto.setDeliveryAddress(d.getDeliveryAddress());
        dto.setDeliveryContactName(d.getDeliveryContactName());
        dto.setDeliveryContactPhone(d.getDeliveryContactPhone());
        dto.setDeliveredBy(d.getDeliveredBy() != null ? d.getDeliveredBy().getUserId() : null);
        dto.setCreatedAt(d.getCreatedAt());
        dto.setUpdatedAt(d.getUpdatedAt());
        return dto;
    }
}
