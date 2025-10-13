package com.ssh.smartServiceHub.service;

import com.ssh.smartServiceHub.dto.ServiceRequestDTO;
import com.ssh.smartServiceHub.entity.Category;
import com.ssh.smartServiceHub.entity.ServiceRequest;
import com.ssh.smartServiceHub.entity.User;
import com.ssh.smartServiceHub.mapper.ServiceRequestMapper;
import com.ssh.smartServiceHub.repository.CategoryRepository;
import com.ssh.smartServiceHub.repository.ServiceRequestRepository;
import com.ssh.smartServiceHub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ServiceRequestDTO createRequest(Long userId, Long categoryId, ServiceRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        ServiceRequest entity = ServiceRequestMapper.toEntity(request, user, category);
        entity.setStatus("OPEN");

        ServiceRequest savedRequest = serviceRequestRepository.save(entity);
        return ServiceRequestMapper.toDTO(savedRequest);
    }

    public List<ServiceRequestDTO> getRequestsByUser(Long userId) {
        return serviceRequestRepository.findByUserId(userId)
                .stream()
                .map(ServiceRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ServiceRequestDTO updateStatus(Long id, String newStatus) {
        ServiceRequest req = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        req.setStatus(newStatus);
        ServiceRequest updatedRequest = serviceRequestRepository.save(req);
        return ServiceRequestMapper.toDTO(updatedRequest);
    }

    public List<ServiceRequestDTO> getAllRequests() {
        return serviceRequestRepository.findAll()
                .stream()
                .map(ServiceRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

}
