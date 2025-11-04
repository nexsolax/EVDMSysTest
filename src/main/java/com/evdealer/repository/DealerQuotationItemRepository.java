package com.evdealer.repository;

import com.evdealer.entity.DealerQuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DealerQuotationItemRepository extends JpaRepository<DealerQuotationItem, UUID> {
    
    List<DealerQuotationItem> findByQuotationQuotationId(UUID quotationId);
    
    void deleteByQuotationQuotationId(UUID quotationId);
}

