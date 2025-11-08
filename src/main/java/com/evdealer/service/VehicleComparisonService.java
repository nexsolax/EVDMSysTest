package com.evdealer.service;

import com.evdealer.dto.VehicleComparisonRequest;
import com.evdealer.dto.VehicleComparisonResponse;
import com.evdealer.entity.VehicleVariant;
import com.evdealer.entity.VehicleInventory;
import com.evdealer.enums.VehicleStatus;
import com.evdealer.repository.VehicleVariantRepository;
import com.evdealer.repository.VehicleInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class VehicleComparisonService {
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    @Autowired
    private VehicleInventoryRepository vehicleInventoryRepository;
    
    public VehicleComparisonResponse compareVehicles(VehicleComparisonRequest request) {
        // Validate input
        if (request.getVariantIds() == null || request.getVariantIds().isEmpty()) {
            throw new RuntimeException("At least one vehicle variant ID is required");
        }
        
        if (request.getVariantIds().size() > 5) {
            throw new RuntimeException("Maximum 5 vehicles can be compared at once");
        }
        
        // Get vehicle variants
        List<VehicleVariant> variants = vehicleVariantRepository.findAllById(request.getVariantIds());
        
        if (variants.size() != request.getVariantIds().size()) {
            throw new RuntimeException("Some vehicle variants not found");
        }
        
        // Create comparison items
        List<VehicleComparisonResponse.VehicleComparisonItem> comparisonItems = new ArrayList<>();
        
        for (VehicleVariant variant : variants) {
            VehicleComparisonResponse.VehicleComparisonItem item = createComparisonItem(variant, request);
            comparisonItems.add(item);
        }
        
        // Calculate comparison scores and rankings
        calculateComparisonScores(comparisonItems);
        
        // Create comparison summary
        VehicleComparisonResponse.ComparisonSummary summary = createComparisonSummary(comparisonItems);
        
        // Create response
        VehicleComparisonResponse response = new VehicleComparisonResponse();
        response.setVehicles(comparisonItems);
        response.setSummary(summary);
        response.setComparisonCriteria(request.getComparisonCriteria());
        response.setComparisonTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return response;
    }
    
    private VehicleComparisonResponse.VehicleComparisonItem createComparisonItem(VehicleVariant variant, VehicleComparisonRequest request) {
        VehicleComparisonResponse.VehicleComparisonItem item = new VehicleComparisonResponse.VehicleComparisonItem();
        
        // Basic information
        item.setVariantId(variant.getVariantId());
        item.setVariantName(variant.getVariantName());
        item.setBasePrice(variant.getPriceBase());
        item.setBatteryCapacity(variant.getBatteryCapacity());
        item.setRangeKm(variant.getRangeKm());
        item.setPowerKw(variant.getPowerKw());
        item.setAcceleration0100(variant.getAcceleration0100());
        item.setTopSpeed(variant.getTopSpeed());
        item.setChargingTimeFast(variant.getChargingTimeFast());
        item.setChargingTimeSlow(variant.getChargingTimeSlow());
        item.setVariantImageUrl(variant.getVariantImageUrl());
        
        // Brand and model information
        if (variant.getModel() != null) {
            item.setModelName(variant.getModel().getModelName());
            if (variant.getModel().getBrand() != null) {
                item.setBrandName(variant.getModel().getBrand().getBrandName());
            }
        }
        
        // Availability information
        if (request.getIncludeAvailability() != null && request.getIncludeAvailability()) {
            List<VehicleInventory> inventory = vehicleInventoryRepository.findByVariantVariantId(variant.getVariantId());
            long availableCount = inventory.stream()
                    .filter(inv -> inv.getStatus() == VehicleStatus.AVAILABLE)
                    .count();
            
            item.setAvailableQuantity((int) availableCount);
            item.setAvailabilityStatus(availableCount > 0 ? VehicleStatus.AVAILABLE.getValue() : "out_of_stock");
        }
        
        return item;
    }
    
    private void calculateComparisonScores(List<VehicleComparisonResponse.VehicleComparisonItem> items) {
        if (items.size() < 2) {
            return; // No comparison needed for single item
        }
        
        // Calculate scores for each criterion
        Map<String, List<BigDecimal>> criteriaValues = new HashMap<>();
        
        for (VehicleComparisonResponse.VehicleComparisonItem item : items) {
            // Price score (lower is better)
            if (item.getBasePrice() != null) {
                criteriaValues.computeIfAbsent("price", k -> new ArrayList<>()).add(item.getBasePrice());
            }
            
            // Range score (higher is better)
            if (item.getRangeKm() != null) {
                criteriaValues.computeIfAbsent("range", k -> new ArrayList<>()).add(BigDecimal.valueOf(item.getRangeKm()));
            }
            
            // Power score (higher is better)
            if (item.getPowerKw() != null) {
                criteriaValues.computeIfAbsent("power", k -> new ArrayList<>()).add(item.getPowerKw());
            }
            
            // Acceleration score (lower is better)
            if (item.getAcceleration0100() != null) {
                criteriaValues.computeIfAbsent("acceleration", k -> new ArrayList<>()).add(item.getAcceleration0100());
            }
        }
        
        // Calculate normalized scores for each item
        for (VehicleComparisonResponse.VehicleComparisonItem item : items) {
            int totalScore = 0;
            int criteriaCount = 0;
            
            // Price score (inverted - lower price gets higher score)
            if (item.getBasePrice() != null && criteriaValues.containsKey("price")) {
                List<BigDecimal> prices = criteriaValues.get("price");
                BigDecimal minPrice = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maxPrice = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                
                if (maxPrice.compareTo(minPrice) > 0) {
                    BigDecimal normalizedScore = maxPrice.subtract(item.getBasePrice())
                            .divide(maxPrice.subtract(minPrice), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(25));
                    totalScore += normalizedScore.intValue();
                } else {
                    totalScore += 25; // All same price
                }
                criteriaCount++;
            }
            
            // Range score (higher range gets higher score)
            if (item.getRangeKm() != null && criteriaValues.containsKey("range")) {
                List<BigDecimal> ranges = criteriaValues.get("range");
                BigDecimal minRange = ranges.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maxRange = ranges.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                
                if (maxRange.compareTo(minRange) > 0) {
                    BigDecimal normalizedScore = BigDecimal.valueOf(item.getRangeKm()).subtract(minRange)
                            .divide(maxRange.subtract(minRange), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(25));
                    totalScore += normalizedScore.intValue();
                } else {
                    totalScore += 25; // All same range
                }
                criteriaCount++;
            }
            
            // Power score (higher power gets higher score)
            if (item.getPowerKw() != null && criteriaValues.containsKey("power")) {
                List<BigDecimal> powers = criteriaValues.get("power");
                BigDecimal minPower = powers.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maxPower = powers.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                
                if (maxPower.compareTo(minPower) > 0) {
                    BigDecimal normalizedScore = item.getPowerKw().subtract(minPower)
                            .divide(maxPower.subtract(minPower), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(25));
                    totalScore += normalizedScore.intValue();
                } else {
                    totalScore += 25; // All same power
                }
                criteriaCount++;
            }
            
            // Acceleration score (inverted - lower acceleration time gets higher score)
            if (item.getAcceleration0100() != null && criteriaValues.containsKey("acceleration")) {
                List<BigDecimal> accelerations = criteriaValues.get("acceleration");
                BigDecimal minAcceleration = accelerations.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maxAcceleration = accelerations.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                
                if (maxAcceleration.compareTo(minAcceleration) > 0) {
                    BigDecimal normalizedScore = maxAcceleration.subtract(item.getAcceleration0100())
                            .divide(maxAcceleration.subtract(minAcceleration), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(25));
                    totalScore += normalizedScore.intValue();
                } else {
                    totalScore += 25; // All same acceleration
                }
                criteriaCount++;
            }
            
            // Set final score
            if (criteriaCount > 0) {
                item.setComparisonScore(Math.min(100, Math.max(0, totalScore / criteriaCount)));
            } else {
                item.setComparisonScore(50); // Default score
            }
        }
        
        // Set rankings based on scores
        items.sort((a, b) -> Integer.compare(b.getComparisonScore(), a.getComparisonScore()));
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setComparisonRank(i + 1);
        }
    }
    
    private VehicleComparisonResponse.ComparisonSummary createComparisonSummary(List<VehicleComparisonResponse.VehicleComparisonItem> items) {
        VehicleComparisonResponse.ComparisonSummary summary = new VehicleComparisonResponse.ComparisonSummary();
        
        summary.setTotalVehicles(items.size());
        
        // Price range
        VehicleComparisonResponse.PriceRange priceRange = new VehicleComparisonResponse.PriceRange();
        List<BigDecimal> prices = items.stream()
                .map(VehicleComparisonResponse.VehicleComparisonItem::getBasePrice)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (!prices.isEmpty()) {
            priceRange.setMinPrice(prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            priceRange.setMaxPrice(prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            priceRange.setAveragePrice(prices.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP));
        }
        summary.setPriceRange(priceRange);
        
        // Range comparison
        VehicleComparisonResponse.RangeComparison rangeComparison = new VehicleComparisonResponse.RangeComparison();
        List<Integer> ranges = items.stream()
                .map(VehicleComparisonResponse.VehicleComparisonItem::getRangeKm)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (!ranges.isEmpty()) {
            rangeComparison.setMinRange(ranges.stream().mapToInt(Integer::intValue).min().orElse(0));
            rangeComparison.setMaxRange(ranges.stream().mapToInt(Integer::intValue).max().orElse(0));
            rangeComparison.setAverageRange((int) ranges.stream().mapToInt(Integer::intValue).average().orElse(0));
        }
        summary.setRangeComparison(rangeComparison);
        
        // Power comparison
        VehicleComparisonResponse.PowerComparison powerComparison = new VehicleComparisonResponse.PowerComparison();
        List<BigDecimal> powers = items.stream()
                .map(VehicleComparisonResponse.VehicleComparisonItem::getPowerKw)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (!powers.isEmpty()) {
            powerComparison.setMinPower(powers.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            powerComparison.setMaxPower(powers.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            powerComparison.setAveragePower(powers.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(powers.size()), 2, RoundingMode.HALF_UP));
        }
        summary.setPowerComparison(powerComparison);
        
        // Recommendations
        if (!items.isEmpty()) {
            // Best value (highest score)
            VehicleComparisonResponse.VehicleComparisonItem bestValue = items.stream()
                    .max(Comparator.comparing(VehicleComparisonResponse.VehicleComparisonItem::getComparisonScore))
                    .orElse(items.get(0));
            summary.setBestValueRecommendation(bestValue.getBrandName() + " " + bestValue.getModelName() + " " + bestValue.getVariantName());
            
            // Performance leader (highest power)
            VehicleComparisonResponse.VehicleComparisonItem performanceLeader = items.stream()
                    .filter(item -> item.getPowerKw() != null)
                    .max(Comparator.comparing(VehicleComparisonResponse.VehicleComparisonItem::getPowerKw))
                    .orElse(items.get(0));
            summary.setPerformanceLeader(performanceLeader.getBrandName() + " " + performanceLeader.getModelName() + " " + performanceLeader.getVariantName());
            
            // Range leader (highest range)
            VehicleComparisonResponse.VehicleComparisonItem rangeLeader = items.stream()
                    .filter(item -> item.getRangeKm() != null)
                    .max(Comparator.comparing(VehicleComparisonResponse.VehicleComparisonItem::getRangeKm))
                    .orElse(items.get(0));
            summary.setRangeLeader(rangeLeader.getBrandName() + " " + rangeLeader.getModelName() + " " + rangeLeader.getVariantName());
        }
        
        return summary;
    }
    
    public List<VehicleVariant> getAvailableVariantsForComparison() {
        return vehicleVariantRepository.findByIsActiveTrue();
    }
    
    public VehicleComparisonResponse quickCompare(List<Integer> variantIds) {
        VehicleComparisonRequest request = new VehicleComparisonRequest(variantIds);
        return compareVehicles(request);
    }
}
