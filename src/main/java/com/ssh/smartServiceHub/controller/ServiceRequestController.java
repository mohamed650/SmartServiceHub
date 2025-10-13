package com.ssh.smartServiceHub.controller;

import com.ssh.smartServiceHub.dto.ServiceRequestDTO;
import com.ssh.smartServiceHub.entity.ServiceRequest;
import com.ssh.smartServiceHub.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @PostMapping("/{userId}/{categoryId}")
    public ResponseEntity<ServiceRequestDTO> createRequest(
            @PathVariable Long userId,
            @PathVariable Long categoryId,
            @RequestBody ServiceRequestDTO request) {
        ServiceRequestDTO created = serviceRequestService.createRequest(userId, categoryId, request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<ServiceRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(serviceRequestService.getAllRequests());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ServiceRequestDTO>> getRequestsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(serviceRequestService.getRequestsByUser(userId));
    }

    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<ServiceRequestDTO> updateStatus(
            @PathVariable Long id, @PathVariable String status) {
        ServiceRequestDTO updated = serviceRequestService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

}
