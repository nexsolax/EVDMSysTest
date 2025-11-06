package com.evdealer.service;

import com.evdealer.dto.DealerRequest;
import com.evdealer.entity.Dealer;
import com.evdealer.repository.DealerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DealerService {
    
    @Autowired
    private DealerRepository dealerRepository;
    
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
        
        try {
            dealerRepository.delete(dealer);
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete dealer: " + e.getMessage() + ". Dealer may be referenced by other records (users, orders, contracts, etc.).");
        }
    }
    
    public Dealer updateDealerStatus(UUID dealerId, String status) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer not found with id: " + dealerId));
        dealer.setStatus(com.evdealer.enums.DealerStatus.valueOf(status.toUpperCase()));
        return dealerRepository.save(dealer);
    }
}

