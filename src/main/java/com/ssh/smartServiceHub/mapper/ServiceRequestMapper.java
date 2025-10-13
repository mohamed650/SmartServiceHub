package com.ssh.smartServiceHub.mapper;

import com.ssh.smartServiceHub.dto.ServiceRequestDTO;
import com.ssh.smartServiceHub.entity.Category;
import com.ssh.smartServiceHub.entity.ServiceRequest;
import com.ssh.smartServiceHub.entity.User;

public class ServiceRequestMapper {

    public static ServiceRequestDTO toDTO(ServiceRequest entity) {
        ServiceRequestDTO dto = new ServiceRequestDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
        }

        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
        }

        return dto;
    }

    public static ServiceRequest toEntity(ServiceRequestDTO dto, User user, Category category) {
        ServiceRequest entity = new ServiceRequest();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        entity.setUser(user);
        entity.setCategory(category);
        return entity;
    }

}
