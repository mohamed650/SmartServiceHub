package com.ssh.smartServiceHub.repository;

import com.ssh.smartServiceHub.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByStatus(String status);
    List<ServiceRequest> findByUserId(Long userId);

    @Query("SELECT s FROM ServiceRequest s WHERE s.status = :status AND s.user.email = :email")
    List<ServiceRequest> findRequestsByStatusAndUserEmail(@Param("status") String status, @Param("email") String email);

    @Query(name = "ServiceRequest.findByCategoryName")
    List<ServiceRequest> findByCategoryName(@Param("name") String name);
}
