package com.evdealer.repository;

import com.evdealer.entity.DealerOrderItem;
import com.evdealer.enums.DealerOrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DealerOrderItemRepository extends JpaRepository<DealerOrderItem, UUID> {
    
    @Query("SELECT DISTINCT doi FROM DealerOrderItem doi LEFT JOIN FETCH doi.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH doi.color WHERE doi.dealerOrder.dealerOrderId = :dealerOrderId")
    List<DealerOrderItem> findByDealerOrderId(@Param("dealerOrderId") UUID dealerOrderId);
    
    @Query("SELECT doi FROM DealerOrderItem doi WHERE doi.variant.variantId = :variantId")
    List<DealerOrderItem> findByVariantId(@Param("variantId") Integer variantId);
    
    @Query("SELECT doi FROM DealerOrderItem doi WHERE doi.color.colorId = :colorId")
    List<DealerOrderItem> findByColorId(@Param("colorId") Integer colorId);
    
    @Query("SELECT doi FROM DealerOrderItem doi WHERE doi.status = :status")
    List<DealerOrderItem> findByStatus(@Param("status") DealerOrderItemStatus status);
    
    @Query("SELECT doi FROM DealerOrderItem doi WHERE doi.dealerOrder.dealerOrderId = :dealerOrderId AND doi.status = :status")
    List<DealerOrderItem> findByDealerOrderIdAndStatus(@Param("dealerOrderId") UUID dealerOrderId, @Param("status") DealerOrderItemStatus status);
    
    @Query("SELECT COUNT(doi) FROM DealerOrderItem doi WHERE doi.variant.variantId = :variantId AND doi.status IN (:statuses)")
    Long countPendingOrdersByVariant(@Param("variantId") Integer variantId, @Param("statuses") List<DealerOrderItemStatus> statuses);
    
    @Query("SELECT SUM(doi.quantity) FROM DealerOrderItem doi WHERE doi.variant.variantId = :variantId AND doi.status IN (:statuses)")
    Long sumPendingQuantityByVariant(@Param("variantId") Integer variantId, @Param("statuses") List<DealerOrderItemStatus> statuses);
}
