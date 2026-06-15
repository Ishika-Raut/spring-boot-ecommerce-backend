package com.ecommerce.backend.catalog.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ecommerce.backend.enums.AttributeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "attribute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attribute_id")
    private Long attributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;                    // e.g., "RAM", "Color", "Screen Size", "Material"

    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type", nullable = false, length = 30)
    private AttributeType attributeType;    // TEXT, SELECT, NUMBER, BOOLEAN, etc.

    @Builder.Default
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Builder.Default
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
