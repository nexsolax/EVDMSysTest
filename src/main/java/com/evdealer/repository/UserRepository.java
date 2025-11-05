package com.evdealer.repository;

import com.evdealer.entity.User;
import com.evdealer.enums.UserType;
import com.evdealer.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.dealer")
    List<User> findAllWithDetails();
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    // Query to get user with dealer eagerly loaded
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.dealer WHERE u.userId = :userId")
    Optional<User> findByIdWithDealer(@Param("userId") UUID userId);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.dealer WHERE u.username = :username")
    Optional<User> findByUsernameWithDealer(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.status = com.evdealer.enums.UserStatus.ACTIVE")
    List<User> findByIsActiveTrue();
    
    List<User> findByUserType(UserType userType);
    
    List<User> findByStatus(UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.status = :status")
    List<User> findByUserTypeAndStatus(@Param("userType") UserType userType, @Param("status") UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.dealer.dealerId = :dealerId")
    List<User> findByDealerDealerId(@Param("dealerId") UUID dealerId);
    
    // Legacy methods for backward compatibility
    @Query("SELECT u FROM User u WHERE u.userType = :userType")
    List<User> findByRoleString(@Param("userType") com.evdealer.enums.UserType userType);
    @Query("SELECT u FROM User u WHERE u.userType = :userType")
    List<User> findByRoleName(@Param("userType") com.evdealer.enums.UserType userType);
    
    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}

