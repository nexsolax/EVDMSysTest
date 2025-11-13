package com.evdealer.service;

import com.evdealer.entity.*;
import com.evdealer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.*;

@Service
@Transactional
public class DataExportService {
    
    // Inject all repositories
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private QuotationRepository quotationRepository;
    @Autowired
    private DealerRepository dealerRepository;
    @Autowired
    private DealerOrderRepository dealerOrderRepository;
    @Autowired
    private DealerQuotationRepository dealerQuotationRepository;
    @Autowired
    private DealerInvoiceRepository dealerInvoiceRepository;
    @Autowired
    private DealerPaymentRepository dealerPaymentRepository;
    @Autowired
    private CustomerPaymentRepository customerPaymentRepository;
    @Autowired
    private VehicleDeliveryRepository vehicleDeliveryRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private SalesContractRepository salesContractRepository;
    @Autowired
    private VehicleInventoryRepository vehicleInventoryRepository;
    @Autowired
    private InstallmentPlanRepository installmentPlanRepository;
    @Autowired
    private InstallmentScheduleRepository installmentScheduleRepository;
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private PricingPolicyRepository pricingPolicyRepository;
    @Autowired
    private DealerTargetRepository dealerTargetRepository;
    @Autowired
    private DealerContractRepository dealerContractRepository;
    @Autowired
    private CustomerFeedbackRepository customerFeedbackRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VehicleBrandRepository vehicleBrandRepository;
    @Autowired
    private VehicleModelRepository vehicleModelRepository;
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    @Autowired
    private VehicleColorRepository vehicleColorRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private DealerDiscountPolicyRepository dealerDiscountPolicyRepository;
    @Autowired
    private DealerInstallmentScheduleRepository dealerInstallmentScheduleRepository;
    @Autowired
    private DealerOrderItemRepository dealerOrderItemRepository;
    @Autowired
    private DealerQuotationItemRepository dealerQuotationItemRepository;
    @Autowired
    private TestDriveScheduleRepository testDriveScheduleRepository;
    @Autowired
    private DealerInstallmentPlanRepository dealerInstallmentPlanRepository;
    
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> getAllData() {
        Map<String, Object> allData = new HashMap<>();
        Map<String, Object> errors = new HashMap<>();
        
        // Get data from each entity independently with error handling
        allData.put("Customer", getDataSafely("Customer", () -> {
            try {
                return customerRepository.findAll();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }));
        
        allData.put("Order", getDataSafely("Order", () -> {
            try {
                List<Order> orders = orderRepository.findAllNative();
                return orders != null ? orders : orderRepository.findAll();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }));
        
        allData.put("Quotation", getDataSafely("Quotation", () -> {
            try {
                List<Quotation> quotations = quotationRepository.findAllNative();
                return quotations != null ? quotations : quotationRepository.findAll();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }));
        
        allData.put("Dealer", getDataSafely("Dealer", () -> dealerRepository.findAll()));
        allData.put("DealerOrder", getDataSafely("DealerOrder", () -> dealerOrderRepository.findAll()));
        allData.put("DealerQuotation", getDataSafely("DealerQuotation", () -> dealerQuotationRepository.findAll()));
        allData.put("DealerInvoice", getDataSafely("DealerInvoice", () -> dealerInvoiceRepository.findAll()));
        allData.put("DealerPayment", getDataSafely("DealerPayment", () -> dealerPaymentRepository.findAll()));
        
        allData.put("CustomerPayment", getDataSafely("CustomerPayment", () -> {
            try {
                List<CustomerPayment> payments = customerPaymentRepository.findAllNative();
                return payments != null ? payments : customerPaymentRepository.findAll();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }));
        
        allData.put("VehicleDelivery", getDataSafely("VehicleDelivery", () -> {
            try {
                List<VehicleDelivery> deliveries = vehicleDeliveryRepository.findAllNative();
                return deliveries != null ? deliveries : vehicleDeliveryRepository.findAll();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }));
        
        allData.put("Appointment", getDataSafely("Appointment", () -> {
            try {
                return appointmentRepository.findAllSimple();
            } catch (Exception e) {
                try {
                    return appointmentRepository.findAll();
                } catch (Exception e2) {
                    return new ArrayList<>();
                }
            }
        }));
        
        allData.put("SalesContract", getDataSafely("SalesContract", () -> {
            try {
                List<SalesContract> contracts = salesContractRepository.findAllNative();
                return contracts != null ? contracts : salesContractRepository.findAll();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }));
        
        allData.put("VehicleInventory", getDataSafely("VehicleInventory", () -> vehicleInventoryRepository.findAll()));
        allData.put("InstallmentPlan", getDataSafely("InstallmentPlan", () -> installmentPlanRepository.findAll()));
        allData.put("InstallmentSchedule", getDataSafely("InstallmentSchedule", () -> installmentScheduleRepository.findAll()));
        allData.put("Promotion", getDataSafely("Promotion", () -> promotionRepository.findAll()));
        allData.put("PricingPolicy", getDataSafely("PricingPolicy", () -> pricingPolicyRepository.findAll()));
        allData.put("DealerTarget", getDataSafely("DealerTarget", () -> dealerTargetRepository.findAll()));
        allData.put("DealerContract", getDataSafely("DealerContract", () -> dealerContractRepository.findAll()));
        allData.put("CustomerFeedback", getDataSafely("CustomerFeedback", () -> customerFeedbackRepository.findAll()));
        allData.put("User", getDataSafely("User", () -> {
            try {
                return userRepository.findAllWithDetails();
            } catch (Exception e) {
                return userRepository.findAll();
            }
        }));
        allData.put("VehicleBrand", getDataSafely("VehicleBrand", () -> vehicleBrandRepository.findAll()));
        allData.put("VehicleModel", getDataSafely("VehicleModel", () -> vehicleModelRepository.findAll()));
        allData.put("VehicleVariant", getDataSafely("VehicleVariant", () -> vehicleVariantRepository.findAll()));
        allData.put("VehicleColor", getDataSafely("VehicleColor", () -> vehicleColorRepository.findAll()));
        allData.put("Warehouse", getDataSafely("Warehouse", () -> warehouseRepository.findAll()));
        allData.put("DealerDiscountPolicy", getDataSafely("DealerDiscountPolicy", () -> dealerDiscountPolicyRepository.findAll()));
        allData.put("DealerInstallmentSchedule", getDataSafely("DealerInstallmentSchedule", () -> {
            try {
                return dealerInstallmentScheduleRepository.findAllWithDetails();
            } catch (Exception e) {
                return dealerInstallmentScheduleRepository.findAll();
            }
        }));
        allData.put("DealerOrderItem", getDataSafely("DealerOrderItem", () -> dealerOrderItemRepository.findAll()));
        allData.put("DealerQuotationItem", getDataSafely("DealerQuotationItem", () -> dealerQuotationItemRepository.findAll()));
        allData.put("TestDriveSchedule", getDataSafely("TestDriveSchedule", () -> {
            try {
                List<TestDriveSchedule> schedules = testDriveScheduleRepository.findAllNative();
                return schedules != null ? schedules : testDriveScheduleRepository.findAll();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }));
        allData.put("DealerInstallmentPlan", getDataSafely("DealerInstallmentPlan", () -> {
            try {
                return dealerInstallmentPlanRepository.findAllWithDetails();
            } catch (Exception e) {
                return dealerInstallmentPlanRepository.findAll();
            }
        }));
        
        // Add errors if any
        if (!errors.isEmpty()) {
            allData.put("errors", errors);
        }
        
        return allData;
    }
    
    private <T> List<T> getDataSafely(String entityName, java.util.function.Supplier<List<T>> supplier) {
        try {
            List<T> data = supplier.get();
            if (data == null) {
                System.err.println("DataExportService: " + entityName + " returned null, using empty list");
                return new ArrayList<>();
            }
            return data;
        } catch (Exception e) {
            System.err.println("DataExportService: Error getting " + entityName + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> getDataStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        Map<String, Map<String, Object>> entityStats = new HashMap<>();
        List<String> entitiesWithData = new ArrayList<>();
        List<String> entitiesWithoutData = new ArrayList<>();
        
        // Count records for each entity
        String[] entityNames = {
            "Customer", "Order", "Quotation", "Dealer", "DealerOrder", "DealerQuotation",
            "DealerInvoice", "DealerPayment", "CustomerPayment", "VehicleDelivery",
            "Appointment", "SalesContract", "VehicleInventory", "InstallmentPlan",
            "InstallmentSchedule", "Promotion", "PricingPolicy", "DealerTarget",
            "DealerContract", "CustomerFeedback", "User", "VehicleBrand",
            "VehicleModel", "VehicleVariant", "VehicleColor", "Warehouse",
            "DealerDiscountPolicy", "DealerInstallmentSchedule", "DealerOrderItem",
            "DealerQuotationItem", "TestDriveSchedule", "DealerInstallmentPlan"
        };
        
        for (String entityName : entityNames) {
            try {
                long count = getEntityCount(entityName);
                boolean hasData = count > 0;
                
                Map<String, Object> entityStat = new HashMap<>();
                entityStat.put("count", count);
                entityStat.put("hasData", hasData);
                entityStats.put(entityName, entityStat);
                
                if (hasData) {
                    entitiesWithData.add(entityName);
                } else {
                    entitiesWithoutData.add(entityName);
                }
            } catch (Exception e) {
                System.err.println("DataExportService: Error counting " + entityName + ": " + e.getMessage());
                Map<String, Object> entityStat = new HashMap<>();
                entityStat.put("count", 0);
                entityStat.put("hasData", false);
                entityStat.put("error", e.getMessage());
                entityStats.put(entityName, entityStat);
                entitiesWithoutData.add(entityName);
            }
        }
        
        statistics.put("totalEntities", entityNames.length);
        statistics.put("entitiesWithData", entitiesWithData);
        statistics.put("entitiesWithoutData", entitiesWithoutData);
        statistics.put("statistics", entityStats);
        statistics.put("totalRecordsWithData", entitiesWithData.size());
        statistics.put("totalRecordsWithoutData", entitiesWithoutData.size());
        
        return statistics;
    }
    
    private long getEntityCount(String entityName) {
        switch (entityName) {
            case "Customer": return customerRepository.count();
            case "Order": return orderRepository.count();
            case "Quotation": return quotationRepository.count();
            case "Dealer": return dealerRepository.count();
            case "DealerOrder": return dealerOrderRepository.count();
            case "DealerQuotation": return dealerQuotationRepository.count();
            case "DealerInvoice": return dealerInvoiceRepository.count();
            case "DealerPayment": return dealerPaymentRepository.count();
            case "CustomerPayment": return customerPaymentRepository.count();
            case "VehicleDelivery": return vehicleDeliveryRepository.count();
            case "Appointment": return appointmentRepository.count();
            case "SalesContract": return salesContractRepository.count();
            case "VehicleInventory": return vehicleInventoryRepository.count();
            case "InstallmentPlan": return installmentPlanRepository.count();
            case "InstallmentSchedule": return installmentScheduleRepository.count();
            case "Promotion": return promotionRepository.count();
            case "PricingPolicy": return pricingPolicyRepository.count();
            case "DealerTarget": return dealerTargetRepository.count();
            case "DealerContract": return dealerContractRepository.count();
            case "CustomerFeedback": return customerFeedbackRepository.count();
            case "User": return userRepository.count();
            case "VehicleBrand": return vehicleBrandRepository.count();
            case "VehicleModel": return vehicleModelRepository.count();
            case "VehicleVariant": return vehicleVariantRepository.count();
            case "VehicleColor": return vehicleColorRepository.count();
            case "Warehouse": return warehouseRepository.count();
            case "DealerDiscountPolicy": return dealerDiscountPolicyRepository.count();
            case "DealerInstallmentSchedule": return dealerInstallmentScheduleRepository.count();
            case "DealerOrderItem": return dealerOrderItemRepository.count();
            case "DealerQuotationItem": return dealerQuotationItemRepository.count();
            case "TestDriveSchedule": return testDriveScheduleRepository.count();
            case "DealerInstallmentPlan": return dealerInstallmentPlanRepository.count();
            default: return 0;
        }
    }
    
    @Transactional
    public Map<String, Object> deleteAllData() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Long> deletedCounts = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // 1. Child records (có nhiều foreign keys)
            deletedCounts.put("CustomerPayment", deleteSafely("CustomerPayment", () -> customerPaymentRepository.deleteAll()));
            deletedCounts.put("DealerPayment", deleteSafely("DealerPayment", () -> dealerPaymentRepository.deleteAll()));
            deletedCounts.put("VehicleDelivery", deleteSafely("VehicleDelivery", () -> vehicleDeliveryRepository.deleteAll()));
            deletedCounts.put("Appointment", deleteSafely("Appointment", () -> appointmentRepository.deleteAll()));
            deletedCounts.put("SalesContract", deleteSafely("SalesContract", () -> salesContractRepository.deleteAll()));
            deletedCounts.put("CustomerFeedback", deleteSafely("CustomerFeedback", () -> customerFeedbackRepository.deleteAll()));
            deletedCounts.put("InstallmentSchedule", deleteSafely("InstallmentSchedule", () -> installmentScheduleRepository.deleteAll()));
            deletedCounts.put("DealerInstallmentSchedule", deleteSafely("DealerInstallmentSchedule", () -> dealerInstallmentScheduleRepository.deleteAll()));
            deletedCounts.put("InstallmentPlan", deleteSafely("InstallmentPlan", () -> installmentPlanRepository.deleteAll()));
            deletedCounts.put("DealerInstallmentPlan", deleteSafely("DealerInstallmentPlan", () -> dealerInstallmentPlanRepository.deleteAll()));
            deletedCounts.put("TestDriveSchedule", deleteSafely("TestDriveSchedule", () -> testDriveScheduleRepository.deleteAll()));
            
            // 2. Items
            deletedCounts.put("DealerOrderItem", deleteSafely("DealerOrderItem", () -> dealerOrderItemRepository.deleteAll()));
            deletedCounts.put("DealerQuotationItem", deleteSafely("DealerQuotationItem", () -> dealerQuotationItemRepository.deleteAll()));
            
            // 3. Intermediate tables
            deletedCounts.put("DealerInvoice", deleteSafely("DealerInvoice", () -> dealerInvoiceRepository.deleteAll()));
            deletedCounts.put("DealerQuotation", deleteSafely("DealerQuotation", () -> dealerQuotationRepository.deleteAll()));
            deletedCounts.put("Quotation", deleteSafely("Quotation", () -> quotationRepository.deleteAll()));
            deletedCounts.put("Order", deleteSafely("Order", () -> orderRepository.deleteAll()));
            deletedCounts.put("DealerOrder", deleteSafely("DealerOrder", () -> dealerOrderRepository.deleteAll()));
            
            // 4. Parent tables
            deletedCounts.put("VehicleInventory", deleteSafely("VehicleInventory", () -> vehicleInventoryRepository.deleteAll()));
            deletedCounts.put("Promotion", deleteSafely("Promotion", () -> promotionRepository.deleteAll()));
            deletedCounts.put("PricingPolicy", deleteSafely("PricingPolicy", () -> pricingPolicyRepository.deleteAll()));
            deletedCounts.put("DealerTarget", deleteSafely("DealerTarget", () -> dealerTargetRepository.deleteAll()));
            deletedCounts.put("DealerContract", deleteSafely("DealerContract", () -> dealerContractRepository.deleteAll()));
            deletedCounts.put("Customer", deleteSafely("Customer", () -> customerRepository.deleteAll()));
            deletedCounts.put("Dealer", deleteSafely("Dealer", () -> dealerRepository.deleteAll()));
            
            // 5. User (trừ admin) - Xóa tất cả users trừ username = "admin"
            long userCountBefore = userRepository.count();
            try {
                List<User> allUsers = userRepository.findAll();
                for (User user : allUsers) {
                    if (user.getUsername() != null && !user.getUsername().equals("admin")) {
                        userRepository.delete(user);
                    }
                }
                long userCountAfter = userRepository.count();
                deletedCounts.put("User", userCountBefore - userCountAfter);
            } catch (Exception e) {
                String errorMsg = "Error deleting users: " + e.getMessage();
                errors.add(errorMsg);
                System.err.println("DataExportService.deleteAllData() - " + errorMsg);
                deletedCounts.put("User", 0L);
            }
            
            // 6. Vehicle
            deletedCounts.put("VehicleVariant", deleteSafely("VehicleVariant", () -> vehicleVariantRepository.deleteAll()));
            deletedCounts.put("VehicleModel", deleteSafely("VehicleModel", () -> vehicleModelRepository.deleteAll()));
            deletedCounts.put("VehicleBrand", deleteSafely("VehicleBrand", () -> vehicleBrandRepository.deleteAll()));
            deletedCounts.put("VehicleColor", deleteSafely("VehicleColor", () -> vehicleColorRepository.deleteAll()));
            
            // 7. Warehouse
            deletedCounts.put("Warehouse", deleteSafely("Warehouse", () -> warehouseRepository.deleteAll()));
            
            // 8. DealerDiscountPolicy
            deletedCounts.put("DealerDiscountPolicy", deleteSafely("DealerDiscountPolicy", () -> dealerDiscountPolicyRepository.deleteAll()));
            
        } catch (Exception e) {
            String errorMsg = "Error in deleteAllData: " + e.getMessage();
            errors.add(errorMsg);
            System.err.println("DataExportService.deleteAllData() - " + errorMsg);
            e.printStackTrace();
        }
        
        result.put("deletedCounts", deletedCounts);
        result.put("totalDeleted", deletedCounts.values().stream().mapToLong(Long::longValue).sum());
        result.put("errors", errors);
        result.put("adminUserPreserved", userRepository.findByUsername("admin").isPresent());
        
        return result;
    }
    
    private long deleteSafely(String entityName, Runnable deleteAction) {
        try {
            long countBefore = getEntityCount(entityName);
            deleteAction.run();
            long countAfter = getEntityCount(entityName);
            long deleted = countBefore - countAfter;
            System.out.println("DataExportService: Deleted " + deleted + " records from " + entityName);
            return deleted;
        } catch (Exception e) {
            System.err.println("DataExportService: Error deleting " + entityName + ": " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }
}

