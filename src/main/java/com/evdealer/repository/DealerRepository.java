package com.evdealer.repository;

import com.evdealer.entity.Dealer;
import com.evdealer.enums.DealerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, UUID> {
    
    @Query("SELECT d FROM Dealer d WHERE d.dealerCode = :dealerCode")
    Optional<Dealer> findByDealerCode(@Param("dealerCode") String dealerCode);
    
    List<Dealer> findByStatus(DealerStatus status);
    
    @Query("SELECT d FROM Dealer d WHERE d.status = :status")
    List<Dealer> findByStatusString(@Param("status") String status);
    
    @Query("SELECT d FROM Dealer d WHERE d.dealerType = :dealerType")
    List<Dealer> findByDealerType(@Param("dealerType") String dealerType);
    
    @Query("SELECT d FROM Dealer d WHERE d.city = :city")
    List<Dealer> findByCity(@Param("city") String city);
    
    @Query("SELECT d FROM Dealer d WHERE d.province = :province")
    List<Dealer> findByProvince(@Param("province") String province);
    
    @Query("SELECT d FROM Dealer d WHERE d.dealerName LIKE %:name%")
    List<Dealer> findByDealerNameContaining(@Param("name") String name);
    
    @Query("SELECT d FROM Dealer d WHERE d.dealerName = :dealerName")
    Optional<Dealer> findByDealerName(@Param("dealerName") String dealerName);
    
    @Query("SELECT d FROM Dealer d WHERE d.contactPerson LIKE %:contactPerson%")
    List<Dealer> findByContactPersonContaining(@Param("contactPerson") String contactPerson);
    
    @Query("SELECT d FROM Dealer d WHERE d.email = :email")
    Optional<Dealer> findByEmail(@Param("email") String email);
    
    @Query("SELECT d FROM Dealer d WHERE d.phone = :phone")
    Optional<Dealer> findByPhone(@Param("phone") String phone);
    
    @Query("SELECT d FROM Dealer d WHERE d.licenseNumber = :licenseNumber")
    Optional<Dealer> findByLicenseNumber(@Param("licenseNumber") String licenseNumber);
    
    @Query("SELECT d FROM Dealer d WHERE d.taxCode = :taxCode")
    Optional<Dealer> findByTaxCode(@Param("taxCode") String taxCode);
}

