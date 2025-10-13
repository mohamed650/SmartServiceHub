package com.ssh.smartServiceHub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service_requests",
        indexes = { @Index(name = "idx_request_status", columnList = "status") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedQuery(
        name = "ServiceRequest.findByCategoryName",
        query = "SELECT s FROM ServiceRequest s WHERE s.category.name = :name"
)
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String status; // OPEN, IN_PROGRESS, CLOSED

    // Many requests can belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Many requests can belong to one category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
