package com.evdealer.service;

import com.evdealer.entity.VehicleBrand;
import com.evdealer.entity.VehicleModel;
import com.evdealer.entity.VehicleVariant;
import com.evdealer.entity.VehicleColor;
import com.evdealer.dto.VehicleModelRequest;
import com.evdealer.dto.VehicleVariantRequest;
import com.evdealer.dto.VehicleBrandRequest;
import com.evdealer.dto.VehicleColorRequest;
import com.evdealer.repository.VehicleBrandRepository;
import com.evdealer.repository.VehicleModelRepository;
import com.evdealer.repository.VehicleVariantRepository;
import com.evdealer.repository.VehicleColorRepository;
import com.evdealer.repository.VehicleInventoryRepository;
import com.evdealer.repository.DealerOrderItemRepository;
import com.evdealer.repository.DealerQuotationItemRepository;
import com.evdealer.repository.QuotationRepository;
import com.evdealer.repository.PromotionRepository;
import com.evdealer.repository.PricingPolicyRepository;
import com.evdealer.repository.DealerDiscountPolicyRepository;
import com.evdealer.repository.AppointmentRepository;
import com.evdealer.repository.TestDriveScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VehicleService {
    
    @Autowired
    private VehicleBrandRepository vehicleBrandRepository;
    
    @Autowired
    private VehicleModelRepository vehicleModelRepository;
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    @Autowired
    private VehicleColorRepository vehicleColorRepository;
    
    @Autowired
    private VehicleInventoryRepository vehicleInventoryRepository;
    
    @Autowired
    private DealerOrderItemRepository dealerOrderItemRepository;
    
    @Autowired
    private DealerQuotationItemRepository dealerQuotationItemRepository;
    
    @Autowired
    private QuotationRepository quotationRepository;
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    @Autowired
    private PricingPolicyRepository pricingPolicyRepository;
    
    @Autowired
    private DealerDiscountPolicyRepository dealerDiscountPolicyRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private TestDriveScheduleRepository testDriveScheduleRepository;
    
    // Vehicle Brand methods
    public List<VehicleBrand> getAllBrands() {
        return vehicleBrandRepository.findAll();
    }
    
    public List<VehicleBrand> getActiveBrands() {
        return vehicleBrandRepository.findByIsActiveTrue();
    }
    
    public Optional<VehicleBrand> getBrandById(Integer brandId) {
        return vehicleBrandRepository.findById(brandId);
    }
    
    public Optional<VehicleBrand> getBrandByName(String brandName) {
        return vehicleBrandRepository.findByBrandName(brandName);
    }
    
    public List<VehicleBrand> getBrandsByCountry(String country) {
        return vehicleBrandRepository.findByCountry(country);
    }
    
    public List<VehicleBrand> searchBrandsByName(String name) {
        return vehicleBrandRepository.findByBrandNameContaining(name);
    }
    
    public VehicleBrand createBrand(VehicleBrand brand) {
        if (vehicleBrandRepository.existsByBrandName(brand.getBrandName())) {
            throw new RuntimeException("Brand already exists: " + brand.getBrandName());
        }
        return vehicleBrandRepository.save(brand);
    }
    
    public VehicleBrand createBrandFromRequest(VehicleBrandRequest request) {
        // Validate brandName
        if (request.getBrandName() == null || request.getBrandName().trim().isEmpty()) {
            throw new RuntimeException("Brand name is required");
        }
        
        // Check if brand already exists
        if (vehicleBrandRepository.existsByBrandName(request.getBrandName())) {
            throw new RuntimeException("Brand already exists: " + request.getBrandName());
        }
        
        // Create VehicleBrand entity
        VehicleBrand brand = new VehicleBrand();
        brand.setBrandName(request.getBrandName().trim());
        brand.setCountry(request.getCountry());
        brand.setFoundedYear(request.getFoundedYear());
        brand.setBrandLogoUrl(request.getBrandLogoUrl());
        brand.setBrandLogoPath(request.getBrandLogoPath());
        brand.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        return vehicleBrandRepository.save(brand);
    }
    
    public VehicleBrand updateBrand(Integer brandId, VehicleBrand brandDetails) {
        VehicleBrand brand = vehicleBrandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + brandId));
        
        if (!brand.getBrandName().equals(brandDetails.getBrandName()) && 
            vehicleBrandRepository.existsByBrandName(brandDetails.getBrandName())) {
            throw new RuntimeException("Brand already exists: " + brandDetails.getBrandName());
        }
        
        brand.setBrandName(brandDetails.getBrandName());
        brand.setCountry(brandDetails.getCountry());
        brand.setFoundedYear(brandDetails.getFoundedYear());
        brand.setBrandLogoUrl(brandDetails.getBrandLogoUrl());
        brand.setBrandLogoPath(brandDetails.getBrandLogoPath());
        brand.setIsActive(brandDetails.getIsActive());
        
        return vehicleBrandRepository.save(brand);
    }
    
    public VehicleBrand updateBrandFromRequest(Integer brandId, VehicleBrandRequest request) {
        VehicleBrand brand = vehicleBrandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + brandId));
        
        // Validate brandName if provided
        if (request.getBrandName() != null && !request.getBrandName().trim().isEmpty()) {
            if (!brand.getBrandName().equals(request.getBrandName()) && 
                vehicleBrandRepository.existsByBrandName(request.getBrandName())) {
                throw new RuntimeException("Brand already exists: " + request.getBrandName());
            }
            brand.setBrandName(request.getBrandName().trim());
        }
        
        // Update other fields
        if (request.getCountry() != null) {
            brand.setCountry(request.getCountry());
        }
        if (request.getFoundedYear() != null) {
            brand.setFoundedYear(request.getFoundedYear());
        }
        if (request.getBrandLogoUrl() != null) {
            brand.setBrandLogoUrl(request.getBrandLogoUrl());
        }
        if (request.getBrandLogoPath() != null) {
            brand.setBrandLogoPath(request.getBrandLogoPath());
        }
        if (request.getIsActive() != null) {
            brand.setIsActive(request.getIsActive());
        }
        
        return vehicleBrandRepository.save(brand);
    }
    
    public void deleteBrand(Integer brandId) {
        VehicleBrand brand = vehicleBrandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + brandId));
        
        // Check for dependencies: VehicleModel
        List<VehicleModel> models = vehicleModelRepository.findByBrandBrandId(brandId);
        if (!models.isEmpty()) {
            List<String> modelNames = new ArrayList<>();
            for (VehicleModel model : models) {
                modelNames.add(model.getModelName() + " (ID: " + model.getModelId() + ")");
            }
            throw new RuntimeException("Cannot delete brand '" + brand.getBrandName() + "' (ID: " + brandId + "). " +
                    "Brand is referenced by " + models.size() + " vehicle model(s): " + String.join(", ", modelNames) +
                    ". Please delete or update these models first.");
        }
        
        try {
            vehicleBrandRepository.delete(brand);
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete brand: " + e.getMessage());
        }
    }
    
    // Vehicle Model methods
    public List<VehicleModel> getAllModels() {
        try {
            // Use JOIN FETCH to eagerly load brand relationship
            return vehicleModelRepository.findAllWithBrand();
        } catch (Exception e) {
            // Log error and return empty list
            return new java.util.ArrayList<>();
        }
    }
    
    public List<VehicleModel> getActiveModels() {
        // Use JOIN FETCH to eagerly load brand relationship
        return vehicleModelRepository.findActiveModelsWithBrand();
    }
    
    public Optional<VehicleModel> getModelById(Integer modelId) {
        return vehicleModelRepository.findById(modelId);
    }
    
    public List<VehicleModel> getModelsByBrand(Integer brandId) {
        return vehicleModelRepository.findByBrandBrandId(brandId);
    }
    
    public List<VehicleModel> getActiveModelsByBrand(Integer brandId) {
        return vehicleModelRepository.findActiveByBrandId(brandId);
    }
    
    public List<VehicleModel> searchModelsByName(String name) {
        return vehicleModelRepository.findByModelNameContaining(name);
    }
    
    public List<VehicleModel> getModelsByType(String vehicleType) {
        return vehicleModelRepository.findByVehicleType(vehicleType);
    }
    
    public List<VehicleModel> getModelsByYear(Integer year) {
        return vehicleModelRepository.findByModelYear(year);
    }
    
    public VehicleModel createModel(VehicleModel model) {
        return vehicleModelRepository.save(model);
    }
    
    public VehicleModel createModelFromRequest(VehicleModelRequest request) {
        // Validate brandId
        if (request.getBrandId() == null) {
            throw new RuntimeException("Brand ID is required");
        }
        
        // Validate modelName
        if (request.getModelName() == null || request.getModelName().trim().isEmpty()) {
            throw new RuntimeException("Model name is required");
        }
        
        // Get VehicleBrand from brandId
        VehicleBrand brand = vehicleBrandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + request.getBrandId()));
        
        // Create VehicleModel entity
        VehicleModel model = new VehicleModel();
        model.setBrand(brand);
        model.setModelName(request.getModelName());
        
        // Use effective modelYear (from year if modelYear is null)
        Integer modelYear = request.getEffectiveModelYear();
        if (modelYear == null) {
            throw new RuntimeException("Model year is required");
        }
        model.setModelYear(modelYear);
        
        model.setVehicleType(request.getVehicleType());
        model.setDescription(request.getDescription());
        
        // Handle image fields (empty string -> null)
        if (request.getModelImageUrl() != null) {
            String imageUrl = request.getModelImageUrl().trim();
            model.setModelImageUrl(imageUrl.isEmpty() ? null : imageUrl);
        }
        if (request.getModelImagePath() != null) {
            String imagePath = request.getModelImagePath().trim();
            model.setModelImagePath(imagePath.isEmpty() ? null : imagePath);
        }
        
        model.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        return vehicleModelRepository.save(model);
    }
    
    public VehicleModel updateModelFromRequest(Integer modelId, VehicleModelRequest request) {
        VehicleModel model = vehicleModelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Model not found with id: " + modelId));
        
        // Update brand if brandId provided
        if (request.getBrandId() != null) {
            VehicleBrand brand = vehicleBrandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found with id: " + request.getBrandId()));
            model.setBrand(brand);
        }
        
        // Update other fields
        if (request.getModelName() != null && !request.getModelName().trim().isEmpty()) {
            model.setModelName(request.getModelName().trim());
        }
        
        Integer modelYear = request.getEffectiveModelYear();
        if (modelYear != null) {
            model.setModelYear(modelYear);
        }
        
        if (request.getVehicleType() != null) {
            model.setVehicleType(request.getVehicleType());
        }
        
        if (request.getDescription() != null) {
            model.setDescription(request.getDescription());
        }
        
        // Handle image fields (empty string -> null)
        if (request.getModelImageUrl() != null) {
            String imageUrl = request.getModelImageUrl().trim();
            model.setModelImageUrl(imageUrl.isEmpty() ? null : imageUrl);
        }
        if (request.getModelImagePath() != null) {
            String imagePath = request.getModelImagePath().trim();
            model.setModelImagePath(imagePath.isEmpty() ? null : imagePath);
        }
        
        if (request.getIsActive() != null) {
            model.setIsActive(request.getIsActive());
        }
        
        return vehicleModelRepository.save(model);
    }
    
    public VehicleModel updateModel(Integer modelId, VehicleModel modelDetails) {
        VehicleModel model = vehicleModelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Model not found with id: " + modelId));
        
        model.setBrand(modelDetails.getBrand());
        model.setModelName(modelDetails.getModelName());
        model.setModelYear(modelDetails.getModelYear());
        model.setVehicleType(modelDetails.getVehicleType());
        model.setDescription(modelDetails.getDescription());
        model.setSpecifications(modelDetails.getSpecifications());
        model.setModelImageUrl(modelDetails.getModelImageUrl());
        model.setModelImagePath(modelDetails.getModelImagePath());
        model.setIsActive(modelDetails.getIsActive());
        
        return vehicleModelRepository.save(model);
    }
    
    public void deleteModel(Integer modelId) {
        VehicleModel model = vehicleModelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Model not found with id: " + modelId));
        
        // Check for dependencies: VehicleVariant
        List<VehicleVariant> variants = vehicleVariantRepository.findByModelModelId(modelId);
        if (!variants.isEmpty()) {
            List<String> variantNames = new ArrayList<>();
            for (VehicleVariant variant : variants) {
                variantNames.add(variant.getVariantName() + " (ID: " + variant.getVariantId() + ")");
            }
            throw new RuntimeException("Cannot delete model '" + model.getModelName() + "' (ID: " + modelId + "). " +
                    "Model is referenced by " + variants.size() + " vehicle variant(s): " + String.join(", ", variantNames) +
                    ". Please delete or update these variants first.");
        }
        
        try {
            vehicleModelRepository.delete(model);
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete model: " + e.getMessage());
        }
    }
    
    // Vehicle Variant methods
    public List<VehicleVariant> getAllVariants() {
        try {
            // Use JOIN FETCH to eagerly load model relationship
            return vehicleVariantRepository.findAllWithModel();
        } catch (Exception e) {
            // Log error and return empty list
            return new java.util.ArrayList<>();
        }
    }
    
    public List<VehicleVariant> getActiveVariants() {
        // Use JOIN FETCH to eagerly load model relationship
        return vehicleVariantRepository.findByIsActiveTrue();
    }
    
    public Optional<VehicleVariant> getVariantById(Integer variantId) {
        // Use JOIN FETCH to eagerly load model and brand relationships
        return vehicleVariantRepository.findByIdWithModel(variantId);
    }
    
    public List<VehicleVariant> getVariantsByModel(Integer modelId) {
        return vehicleVariantRepository.findByModelModelId(modelId);
    }
    
    public List<VehicleVariant> getActiveVariantsByModel(Integer modelId) {
        return vehicleVariantRepository.findActiveByModelId(modelId);
    }
    
    public List<VehicleVariant> searchVariantsByName(String name) {
        return vehicleVariantRepository.findByVariantNameContaining(name);
    }
    
    public List<VehicleVariant> getVariantsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return vehicleVariantRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    public List<VehicleVariant> getVariantsByMinRange(Integer minRange) {
        return vehicleVariantRepository.findByMinRange(minRange);
    }
    
    public VehicleVariant createVariant(VehicleVariant variant) {
        return vehicleVariantRepository.save(variant);
    }
    
    public VehicleVariant createVariantFromRequest(VehicleVariantRequest request) {
        // Validate modelId
        if (request.getModelId() == null) {
            throw new RuntimeException("Model ID is required");
        }
        
        // Validate variantName
        if (request.getVariantName() == null || request.getVariantName().trim().isEmpty()) {
            throw new RuntimeException("Variant name is required");
        }
        
        // Validate basePrice
        if (request.getBasePrice() == null) {
            throw new RuntimeException("Base price is required");
        }
        
        // Get VehicleModel from modelId
        VehicleModel model = vehicleModelRepository.findById(request.getModelId())
                .orElseThrow(() -> new RuntimeException("Model not found with id: " + request.getModelId()));
        
        // Create VehicleVariant entity
        VehicleVariant variant = new VehicleVariant();
        variant.setModel(model);
        variant.setVariantName(request.getVariantName().trim());
        variant.setPriceBase(request.getBasePrice());
        
        // Set batteryCapacity (already BigDecimal from request)
        if (request.getBatteryCapacity() != null) {
            variant.setBatteryCapacity(request.getBatteryCapacity());
        }
        
        // Set rangeKm
        variant.setRangeKm(request.getRangeKm());
        
        // Convert powerKw from Integer to BigDecimal if provided
        if (request.getPowerKw() != null) {
            variant.setPowerKw(BigDecimal.valueOf(request.getPowerKw()));
        }
        
        // Set acceleration0100
        variant.setAcceleration0100(request.getAcceleration0100());
        
        // Set topSpeed
        variant.setTopSpeed(request.getTopSpeed());
        
        // Set charging times
        variant.setChargingTimeFast(request.getChargingTimeFast());
        variant.setChargingTimeSlow(request.getChargingTimeSlow());
        
        // Set isActive (default to true if not provided)
        variant.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        // Handle image fields (empty string -> null)
        if (request.getVariantImageUrl() != null) {
            String imageUrl = request.getVariantImageUrl().trim();
            variant.setVariantImageUrl(imageUrl.isEmpty() ? null : imageUrl);
        }
        
        if (request.getVariantImagePath() != null) {
            String imagePath = request.getVariantImagePath().trim();
            variant.setVariantImagePath(imagePath.isEmpty() ? null : imagePath);
        }
        
        // Save variant
        VehicleVariant savedVariant = vehicleVariantRepository.save(variant);
        
        // Fetch again with JOIN FETCH to eagerly load model relationship for response
        return vehicleVariantRepository.findByIdWithModel(savedVariant.getVariantId())
                .orElseThrow(() -> new RuntimeException("Failed to fetch created variant"));
    }
    
    public VehicleVariant updateVariantFromRequest(Integer variantId, VehicleVariantRequest request) {
        VehicleVariant variant = vehicleVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with id: " + variantId));
        
        // Update model if modelId provided
        if (request.getModelId() != null) {
            VehicleModel model = vehicleModelRepository.findById(request.getModelId())
                    .orElseThrow(() -> new RuntimeException("Model not found with id: " + request.getModelId()));
            variant.setModel(model);
        }
        
        // Update other fields
        if (request.getVariantName() != null && !request.getVariantName().trim().isEmpty()) {
            variant.setVariantName(request.getVariantName().trim());
        }
        
        if (request.getBasePrice() != null) {
            variant.setPriceBase(request.getBasePrice());
        }
        
        if (request.getBatteryCapacity() != null) {
            variant.setBatteryCapacity(request.getBatteryCapacity());
        }
        
        if (request.getRangeKm() != null) {
            variant.setRangeKm(request.getRangeKm());
        }
        
        if (request.getPowerKw() != null) {
            variant.setPowerKw(BigDecimal.valueOf(request.getPowerKw()));
        }
        
        if (request.getAcceleration0100() != null) {
            variant.setAcceleration0100(request.getAcceleration0100());
        }
        
        if (request.getTopSpeed() != null) {
            variant.setTopSpeed(request.getTopSpeed());
        }
        
        if (request.getChargingTimeFast() != null) {
            variant.setChargingTimeFast(request.getChargingTimeFast());
        }
        
        if (request.getChargingTimeSlow() != null) {
            variant.setChargingTimeSlow(request.getChargingTimeSlow());
        }
        
        if (request.getIsActive() != null) {
            variant.setIsActive(request.getIsActive());
        }
        
        // Update image fields if provided (handle empty strings)
        if (request.getVariantImageUrl() != null) {
            String imageUrl = request.getVariantImageUrl().trim();
            variant.setVariantImageUrl(imageUrl.isEmpty() ? null : imageUrl);
        }
        
        if (request.getVariantImagePath() != null) {
            String imagePath = request.getVariantImagePath().trim();
            variant.setVariantImagePath(imagePath.isEmpty() ? null : imagePath);
        }
        
        // Save variant
        vehicleVariantRepository.save(variant);
        
        // Fetch again with JOIN FETCH to eagerly load model relationship
        return vehicleVariantRepository.findByIdWithModel(variantId)
                .orElseThrow(() -> new RuntimeException("Failed to fetch updated variant"));
    }
    
    public VehicleVariant updateVariant(Integer variantId, VehicleVariant variantDetails) {
        VehicleVariant variant = vehicleVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with id: " + variantId));
        
        variant.setModel(variantDetails.getModel());
        variant.setVariantName(variantDetails.getVariantName());
        variant.setBatteryCapacity(variantDetails.getBatteryCapacity());
        variant.setRangeKm(variantDetails.getRangeKm());
        variant.setPowerKw(variantDetails.getPowerKw());
        variant.setAcceleration0100(variantDetails.getAcceleration0100());
        variant.setTopSpeed(variantDetails.getTopSpeed());
        variant.setChargingTimeFast(variantDetails.getChargingTimeFast());
        variant.setChargingTimeSlow(variantDetails.getChargingTimeSlow());
        variant.setPriceBase(variantDetails.getPriceBase());
        variant.setVariantImageUrl(variantDetails.getVariantImageUrl());
        variant.setVariantImagePath(variantDetails.getVariantImagePath());
        variant.setIsActive(variantDetails.getIsActive());
        
        return vehicleVariantRepository.save(variant);
    }
    
    public void deleteVariant(Integer variantId) {
        VehicleVariant variant = vehicleVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with id: " + variantId));
        
        // Check for dependencies
        List<String> dependencies = new ArrayList<>();
        
        // Check VehicleInventory
        List<com.evdealer.entity.VehicleInventory> inventory = vehicleInventoryRepository.findByVariantVariantId(variantId);
        if (!inventory.isEmpty()) {
            dependencies.add("VehicleInventory (" + inventory.size() + " record(s))");
        }
        
        // Check DealerOrderItem
        List<com.evdealer.entity.DealerOrderItem> orderItems = dealerOrderItemRepository.findByVariantId(variantId);
        if (!orderItems.isEmpty()) {
            dependencies.add("DealerOrderItem (" + orderItems.size() + " record(s))");
        }
        
        // Check DealerQuotationItem - need to check all items
        List<com.evdealer.entity.DealerQuotationItem> allQuotationItems = dealerQuotationItemRepository.findAll();
        long quotationItemCount = allQuotationItems.stream()
                .filter(item -> item.getVariant() != null && item.getVariant().getVariantId().equals(variantId))
                .count();
        if (quotationItemCount > 0) {
            dependencies.add("DealerQuotationItem (" + quotationItemCount + " record(s))");
        }
        
        // Check Quotation
        List<com.evdealer.entity.Quotation> quotations = quotationRepository.findByVariantVariantId(variantId);
        if (!quotations.isEmpty()) {
            dependencies.add("Quotation (" + quotations.size() + " record(s))");
        }
        
        // Check Promotion
        List<com.evdealer.entity.Promotion> promotions = promotionRepository.findByVariantVariantId(variantId);
        if (!promotions.isEmpty()) {
            dependencies.add("Promotion (" + promotions.size() + " record(s))");
        }
        
        // Check PricingPolicy
        List<com.evdealer.entity.PricingPolicy> pricingPolicies = pricingPolicyRepository.findByVariantId(variantId);
        if (!pricingPolicies.isEmpty()) {
            dependencies.add("PricingPolicy (" + pricingPolicies.size() + " record(s))");
        }
        
        // Check DealerDiscountPolicy
        List<com.evdealer.entity.DealerDiscountPolicy> discountPolicies = dealerDiscountPolicyRepository.findByVariantId(variantId);
        if (!discountPolicies.isEmpty()) {
            dependencies.add("DealerDiscountPolicy (" + discountPolicies.size() + " record(s))");
        }
        
        // Check Appointment
        List<com.evdealer.entity.Appointment> appointments = appointmentRepository.findByVariantId(variantId);
        if (!appointments.isEmpty()) {
            dependencies.add("Appointment (" + appointments.size() + " record(s))");
        }
        
        // Check TestDriveSchedule - need to check all and filter by variantId (repository method has wrong signature)
        List<com.evdealer.entity.TestDriveSchedule> allTestDrives = testDriveScheduleRepository.findAll();
        long testDriveCount = allTestDrives.stream()
                .filter(td -> td.getVariant() != null && td.getVariant().getVariantId().equals(variantId))
                .count();
        if (testDriveCount > 0) {
            dependencies.add("TestDriveSchedule (" + testDriveCount + " record(s))");
        }
        
        if (!dependencies.isEmpty()) {
            throw new RuntimeException("Cannot delete variant '" + variant.getVariantName() + "' (ID: " + variantId + "). " +
                    "Variant is referenced by: " + String.join(", ", dependencies) +
                    ". Please delete or update these records first.");
        }
        
        try {
            vehicleVariantRepository.delete(variant);
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete variant: " + e.getMessage());
        }
    }
    
    // Vehicle Color methods
    public List<VehicleColor> getAllColors() {
        return vehicleColorRepository.findAll();
    }
    
    public List<VehicleColor> getActiveColors() {
        return vehicleColorRepository.findByIsActiveTrue();
    }
    
    public Optional<VehicleColor> getColorById(Integer colorId) {
        return vehicleColorRepository.findById(colorId);
    }
    
    public Optional<VehicleColor> getColorByName(String colorName) {
        return vehicleColorRepository.findByColorName(colorName);
    }
    
    public Optional<VehicleColor> getColorByCode(String colorCode) {
        return vehicleColorRepository.findByColorCode(colorCode);
    }
    
    public List<VehicleColor> searchColorsByName(String name) {
        return vehicleColorRepository.findByColorNameContainingIgnoreCase(name);
    }
    
    public VehicleColor createColor(VehicleColor color) {
        if (vehicleColorRepository.existsByColorName(color.getColorName())) {
            throw new RuntimeException("Color already exists: " + color.getColorName());
        }
        return vehicleColorRepository.save(color);
    }
    
    public VehicleColor createColorFromRequest(VehicleColorRequest request) {
        // Validate colorName
        if (request.getColorName() == null || request.getColorName().trim().isEmpty()) {
            throw new RuntimeException("Color name is required");
        }
        
        // Check if color already exists
        if (vehicleColorRepository.existsByColorName(request.getColorName())) {
            throw new RuntimeException("Color already exists: " + request.getColorName());
        }
        
        // Create VehicleColor entity
        VehicleColor color = new VehicleColor();
        color.setColorName(request.getColorName().trim());
        color.setColorCode(request.getColorCode());
        
        // Handle image fields (empty string -> null)
        if (request.getColorSwatchUrl() != null) {
            String swatchUrl = request.getColorSwatchUrl().trim();
            color.setColorSwatchUrl(swatchUrl.isEmpty() ? null : swatchUrl);
        }
        if (request.getColorSwatchPath() != null) {
            String swatchPath = request.getColorSwatchPath().trim();
            color.setColorSwatchPath(swatchPath.isEmpty() ? null : swatchPath);
        }
        
        color.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        return vehicleColorRepository.save(color);
    }
    
    public VehicleColor updateColor(Integer colorId, VehicleColor colorDetails) {
        VehicleColor color = vehicleColorRepository.findById(colorId)
                .orElseThrow(() -> new RuntimeException("Color not found with id: " + colorId));
        
        if (!color.getColorName().equals(colorDetails.getColorName()) && 
            vehicleColorRepository.existsByColorName(colorDetails.getColorName())) {
            throw new RuntimeException("Color already exists: " + colorDetails.getColorName());
        }
        
        color.setColorName(colorDetails.getColorName());
        color.setColorCode(colorDetails.getColorCode());
        color.setColorSwatchUrl(colorDetails.getColorSwatchUrl());
        color.setColorSwatchPath(colorDetails.getColorSwatchPath());
        color.setIsActive(colorDetails.getIsActive());
        
        return vehicleColorRepository.save(color);
    }
    
    public VehicleColor updateColorFromRequest(Integer colorId, VehicleColorRequest request) {
        VehicleColor color = vehicleColorRepository.findById(colorId)
                .orElseThrow(() -> new RuntimeException("Color not found with id: " + colorId));
        
        // Validate colorName if provided
        if (request.getColorName() != null && !request.getColorName().trim().isEmpty()) {
            if (!color.getColorName().equals(request.getColorName()) && 
                vehicleColorRepository.existsByColorName(request.getColorName())) {
                throw new RuntimeException("Color already exists: " + request.getColorName());
            }
            color.setColorName(request.getColorName().trim());
        }
        
        // Update other fields
        if (request.getColorCode() != null) {
            color.setColorCode(request.getColorCode());
        }
        
        // Handle image fields (empty string -> null)
        if (request.getColorSwatchUrl() != null) {
            String swatchUrl = request.getColorSwatchUrl().trim();
            color.setColorSwatchUrl(swatchUrl.isEmpty() ? null : swatchUrl);
        }
        if (request.getColorSwatchPath() != null) {
            String swatchPath = request.getColorSwatchPath().trim();
            color.setColorSwatchPath(swatchPath.isEmpty() ? null : swatchPath);
        }
        
        if (request.getIsActive() != null) {
            color.setIsActive(request.getIsActive());
        }
        
        return vehicleColorRepository.save(color);
    }
    
    public void deleteColor(Integer colorId) {
        VehicleColor color = vehicleColorRepository.findById(colorId)
                .orElseThrow(() -> new RuntimeException("Color not found with id: " + colorId));
        
        // Check for dependencies
        List<String> dependencies = new ArrayList<>();
        
        // Check VehicleInventory
        List<com.evdealer.entity.VehicleInventory> inventory = vehicleInventoryRepository.findByColorColorId(colorId);
        if (!inventory.isEmpty()) {
            dependencies.add("VehicleInventory (" + inventory.size() + " record(s))");
        }
        
        // Check DealerOrderItem
        List<com.evdealer.entity.DealerOrderItem> orderItems = dealerOrderItemRepository.findByColorId(colorId);
        if (!orderItems.isEmpty()) {
            dependencies.add("DealerOrderItem (" + orderItems.size() + " record(s))");
        }
        
        // Check DealerQuotationItem - need to check all items
        List<com.evdealer.entity.DealerQuotationItem> allQuotationItems = dealerQuotationItemRepository.findAll();
        long quotationItemCount = allQuotationItems.stream()
                .filter(item -> item.getColor() != null && item.getColor().getColorId().equals(colorId))
                .count();
        if (quotationItemCount > 0) {
            dependencies.add("DealerQuotationItem (" + quotationItemCount + " record(s))");
        }
        
        // Check Quotation
        List<com.evdealer.entity.Quotation> quotations = quotationRepository.findByColorColorId(colorId);
        if (!quotations.isEmpty()) {
            dependencies.add("Quotation (" + quotations.size() + " record(s))");
        }
        
        if (!dependencies.isEmpty()) {
            throw new RuntimeException("Cannot delete color '" + color.getColorName() + "' (ID: " + colorId + "). " +
                    "Color is referenced by: " + String.join(", ", dependencies) +
                    ". Please delete or update these records first.");
        }
        
        try {
            vehicleColorRepository.delete(color);
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete color: " + e.getMessage());
        }
    }
}

