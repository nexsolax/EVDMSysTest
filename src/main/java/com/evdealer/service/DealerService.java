package com.evdealer.service;

import com.evdealer.dto.DealerRequest;
import com.evdealer.entity.Dealer;
import com.evdealer.repository.DealerRepository;
import com.evdealer.repository.DealerOrderRepository;
import com.evdealer.repository.DealerQuotationRepository;
import com.evdealer.repository.DealerContractRepository;
import com.evdealer.repository.UserRepository;
import com.evdealer.repository.VehicleInventoryRepository;
import com.evdealer.repository.DealerTargetRepository;
import com.evdealer.repository.PricingPolicyRepository;
import com.evdealer.repository.InstallmentPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DealerService {
    
    @Autowired
    private DealerRepository dealerRepository;
    
    @Autowired
    private DealerOrderRepository dealerOrderRepository;
    
    @Autowired
    private DealerQuotationRepository dealerQuotationRepository;
    
    @Autowired
    private DealerContractRepository dealerContractRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleInventoryRepository vehicleInventoryRepository;
    
    @Autowired
    private DealerTargetRepository dealerTargetRepository;
    
    @Autowired
    private PricingPolicyRepository pricingPolicyRepository;
    
    @Autowired
    private InstallmentPlanRepository installmentPlanRepository;
    
    public List<Dealer> getAllDealers() {
        try {
            return dealerRepository.findAll();
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    public Optional<Dealer> getDealerById(UUID dealerId) {
        return dealerRepository.findById(dealerId);
    }
    
    public Optional<Dealer> getDealerByCode(String dealerCode) {
        return dealerRepository.findByDealerCode(dealerCode);
    }
    
    public List<Dealer> getDealersByStatus(String status) {
        return dealerRepository.findByStatusString(status);
    }
    
    public List<Dealer> getDealersByType(String dealerType) {
        return dealerRepository.findByDealerType(dealerType);
    }
    
    public List<Dealer> getDealersByCity(String city) {
        return dealerRepository.findByCity(city);
    }
    
    public List<Dealer> getDealersByProvince(String province) {
        return dealerRepository.findByProvince(province);
    }
    
    public List<Dealer> getDealersByName(String name) {
        return dealerRepository.findByDealerNameContaining(name);
    }
    
    public List<Dealer> getDealersByContactPerson(String contactPerson) {
        return dealerRepository.findByContactPersonContaining(contactPerson);
    }
    
    public Optional<Dealer> getDealerByEmail(String email) {
        return dealerRepository.findByEmail(email);
    }
    
    public Optional<Dealer> getDealerByPhone(String phone) {
        return dealerRepository.findByPhone(phone);
    }
    
    public Optional<Dealer> getDealerByLicenseNumber(String licenseNumber) {
        return dealerRepository.findByLicenseNumber(licenseNumber);
    }
    
    public Optional<Dealer> getDealerByTaxCode(String taxCode) {
        return dealerRepository.findByTaxCode(taxCode);
    }
    
    public Dealer createDealer(Dealer dealer) {
        // Check if dealer code already exists
        if (dealerRepository.findByDealerCode(dealer.getDealerCode()).isPresent()) {
            throw new RuntimeException("Dealer with code " + dealer.getDealerCode() + " already exists");
        }
        
        // Check if email already exists
        if (dealer.getEmail() != null && dealerRepository.findByEmail(dealer.getEmail()).isPresent()) {
            throw new RuntimeException("Dealer with email " + dealer.getEmail() + " already exists");
        }
        
        // Check if phone already exists
        if (dealer.getPhone() != null && dealerRepository.findByPhone(dealer.getPhone()).isPresent()) {
            throw new RuntimeException("Dealer with phone " + dealer.getPhone() + " already exists");
        }
        
        return dealerRepository.save(dealer);
    }
    
    public Dealer createDealerFromRequest(DealerRequest request) {
        // Check if dealer code already exists
        if (dealerRepository.findByDealerCode(request.getDealerCode()).isPresent()) {
            throw new RuntimeException("Dealer with code " + request.getDealerCode() + " already exists");
        }
        
        // Check if email already exists
        if (request.getEmail() != null && dealerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Dealer with email " + request.getEmail() + " already exists");
        }
        
        // Check if phone already exists
        if (request.getPhone() != null && dealerRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Dealer with phone " + request.getPhone() + " already exists");
        }
        
        // Create new dealer
        Dealer dealer = new Dealer();
        dealer.setDealerCode(request.getDealerCode());
        dealer.setDealerName(request.getDealerName());
        dealer.setDealerType(request.getDealerType());
        dealer.setContactPerson(request.getContactPerson());
        dealer.setEmail(request.getEmail());
        dealer.setPhone(request.getPhone());
        dealer.setAddress(request.getAddress());
        dealer.setCity(request.getCity());
        dealer.setProvince(request.getProvince());
        dealer.setPostalCode(request.getPostalCode());
        dealer.setLicenseNumber(request.getLicenseNumber());
        dealer.setTaxCode(request.getTaxCode());
        dealer.setCommissionRate(request.getCommissionRate());
        dealer.setStatus(request.getStatus() != null ? request.getStatus() : com.evdealer.enums.DealerStatus.ACTIVE);
        dealer.setNotes(request.getNotes());
        
        return dealerRepository.save(dealer);
    }
    
    public Dealer updateDealer(UUID dealerId, Dealer dealerDetails) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer not found with id: " + dealerId));
        
        // Check if dealer code already exists (excluding current dealer)
        if (!dealer.getDealerCode().equals(dealerDetails.getDealerCode())) {
            if (dealerRepository.findByDealerCode(dealerDetails.getDealerCode()).isPresent()) {
                throw new RuntimeException("Dealer with code " + dealerDetails.getDealerCode() + " already exists");
            }
        }
        
        // Check if email already exists (excluding current dealer)
        if (dealerDetails.getEmail() != null && !dealerDetails.getEmail().equals(dealer.getEmail())) {
            if (dealerRepository.findByEmail(dealerDetails.getEmail()).isPresent()) {
                throw new RuntimeException("Dealer with email " + dealerDetails.getEmail() + " already exists");
            }
        }
        
        // Check if phone already exists (excluding current dealer)
        if (dealerDetails.getPhone() != null && !dealerDetails.getPhone().equals(dealer.getPhone())) {
            if (dealerRepository.findByPhone(dealerDetails.getPhone()).isPresent()) {
                throw new RuntimeException("Dealer with phone " + dealerDetails.getPhone() + " already exists");
            }
        }
        
        dealer.setDealerCode(dealerDetails.getDealerCode());
        dealer.setDealerName(dealerDetails.getDealerName());
        dealer.setContactPerson(dealerDetails.getContactPerson());
        dealer.setEmail(dealerDetails.getEmail());
        dealer.setPhone(dealerDetails.getPhone());
        dealer.setAddress(dealerDetails.getAddress());
        dealer.setCity(dealerDetails.getCity());
        dealer.setProvince(dealerDetails.getProvince());
        dealer.setPostalCode(dealerDetails.getPostalCode());
        dealer.setDealerType(dealerDetails.getDealerType());
        dealer.setLicenseNumber(dealerDetails.getLicenseNumber());
        dealer.setTaxCode(dealerDetails.getTaxCode());
        dealer.setBankAccount(dealerDetails.getBankAccount());
        dealer.setBankName(dealerDetails.getBankName());
        dealer.setCommissionRate(dealerDetails.getCommissionRate());
        dealer.setStatus(dealerDetails.getStatus());
        dealer.setNotes(dealerDetails.getNotes());
        
        return dealerRepository.save(dealer);
    }
    
    public void deleteDealer(UUID dealerId) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer not found with id: " + dealerId));
        
        // Check for dependencies
        List<String> dependencies = new ArrayList<>();
        
        // Check DealerOrder
        List<com.evdealer.entity.DealerOrder> dealerOrders = dealerOrderRepository.findByDealerId(dealerId);
        if (!dealerOrders.isEmpty()) {
            dependencies.add("DealerOrder (" + dealerOrders.size() + " record(s))");
        }
        
        // Check DealerQuotation
        List<com.evdealer.entity.DealerQuotation> dealerQuotations = dealerQuotationRepository.findByDealerDealerId(dealerId);
        if (!dealerQuotations.isEmpty()) {
            dependencies.add("DealerQuotation (" + dealerQuotations.size() + " record(s))");
        }
        
        // Check DealerContract - need to check all and filter
        List<com.evdealer.entity.DealerContract> allContracts = dealerContractRepository.findAll();
        long contractCount = allContracts.stream()
                .filter(contract -> contract.getDealer() != null && contract.getDealer().getDealerId().equals(dealerId))
                .count();
        if (contractCount > 0) {
            dependencies.add("DealerContract (" + contractCount + " record(s))");
        }
        
        // Check User (excluding admin)
        List<com.evdealer.entity.User> users = userRepository.findByDealerDealerId(dealerId);
        long nonAdminUserCount = users.stream()
                .filter(user -> user.getUsername() == null || !user.getUsername().equals("admin"))
                .count();
        if (nonAdminUserCount > 0) {
            dependencies.add("User (" + nonAdminUserCount + " record(s), excluding admin)");
        }
        
        // Check VehicleInventory (reserved_for_dealer) - need to check all and filter
        List<com.evdealer.entity.VehicleInventory> allInventory = vehicleInventoryRepository.findAll();
        long reservedInventoryCount = allInventory.stream()
                .filter(inv -> inv.getReservedForDealer() != null && inv.getReservedForDealer().getDealerId().equals(dealerId))
                .count();
        if (reservedInventoryCount > 0) {
            dependencies.add("VehicleInventory (reserved_for_dealer: " + reservedInventoryCount + " record(s))");
        }
        
        // Check DealerTarget
        List<com.evdealer.entity.DealerTarget> dealerTargets = dealerTargetRepository.findByDealerDealerId(dealerId);
        if (!dealerTargets.isEmpty()) {
            dependencies.add("DealerTarget (" + dealerTargets.size() + " record(s))");
        }
        
        // Check PricingPolicy
        List<com.evdealer.entity.PricingPolicy> pricingPolicies = pricingPolicyRepository.findByDealerId(dealerId);
        if (!pricingPolicies.isEmpty()) {
            dependencies.add("PricingPolicy (" + pricingPolicies.size() + " record(s))");
        }
        
        // Check InstallmentPlan
        List<com.evdealer.entity.InstallmentPlan> installmentPlans = installmentPlanRepository.findByDealerDealerId(dealerId);
        if (!installmentPlans.isEmpty()) {
            dependencies.add("InstallmentPlan (" + installmentPlans.size() + " record(s))");
        }
        
        if (!dependencies.isEmpty()) {
            throw new RuntimeException("Cannot delete dealer '" + dealer.getDealerName() + "' (ID: " + dealerId + ", Code: " + dealer.getDealerCode() + "). " +
                    "Dealer is referenced by: " + String.join(", ", dependencies) +
                    ". Please delete or update these records first.");
        }
        
        try {
            dealerRepository.delete(dealer);
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete dealer: " + e.getMessage());
        }
    }
    
    public Dealer updateDealerStatus(UUID dealerId, String status) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer not found with id: " + dealerId));
        dealer.setStatus(com.evdealer.enums.DealerStatus.valueOf(status.toUpperCase()));
        return dealerRepository.save(dealer);
    }
}

