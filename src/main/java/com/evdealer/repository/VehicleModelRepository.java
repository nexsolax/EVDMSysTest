package com.evdealer.repository;

import com.evdealer.entity.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Integer> {
    
    @Query("SELECT DISTINCT vm FROM VehicleModel vm LEFT JOIN FETCH vm.brand")
    List<VehicleModel> findAllWithBrand();
    
    @Query("SELECT DISTINCT vm FROM VehicleModel vm LEFT JOIN FETCH vm.brand WHERE vm.brand.brandId = :brandId")
    List<VehicleModel> findByBrandBrandId(@Param("brandId") Integer brandId);
    
    @Query("SELECT DISTINCT vm FROM VehicleModel vm LEFT JOIN FETCH vm.brand WHERE vm.isActive = true")
    List<VehicleModel> findActiveModelsWithBrand();
    
    @Query("SELECT DISTINCT vm FROM VehicleModel vm LEFT JOIN FETCH vm.brand WHERE vm.brand.brandId = :brandId AND vm.isActive = true")
    List<VehicleModel> findActiveByBrandId(@Param("brandId") Integer brandId);
    
    @Query("SELECT DISTINCT vm FROM VehicleModel vm LEFT JOIN FETCH vm.brand WHERE vm.modelName LIKE %:name%")
    List<VehicleModel> findByModelNameContaining(@Param("name") String name);
    
    @Query("SELECT DISTINCT vm FROM VehicleModel vm LEFT JOIN FETCH vm.brand WHERE vm.vehicleType = :vehicleType")
    List<VehicleModel> findByVehicleType(@Param("vehicleType") String vehicleType);
    
    @Query("SELECT DISTINCT vm FROM VehicleModel vm LEFT JOIN FETCH vm.brand WHERE vm.modelYear = :year")
    List<VehicleModel> findByModelYear(@Param("year") Integer year);
}

