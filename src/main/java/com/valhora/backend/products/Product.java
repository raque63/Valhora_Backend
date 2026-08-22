package com.valhora.backend.products;

import com.valhora.backend.categories.Brand;
import com.valhora.backend.categories.Category;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Movement movement;

    @Column(nullable = false)
    private String material;

    @Column(name = "strap_material", nullable = false)
    private String strapMaterial;

    @Column(nullable = false)
    private String color;

    /** Línea/serie dentro de la marca, ej. "Seiko 5 Sports". */
    private String collection;

    /** Referencia del fabricante, ej. "SSK019" (distinta del SKU interno). */
    private String reference;

    @Column(name = "movement_detail")
    private String movementDetail;

    private String caliber;

    @Column(name = "power_reserve")
    private String powerReserve;

    @Column(name = "case_diameter")
    private String caseDiameter;

    private String thickness;

    private String crystal;

    @Column(name = "water_resistance")
    private String waterResistance;

    @Column(nullable = false)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Availability availability;

    @Column(name = "is_new", nullable = false)
    private boolean isNew;

    @Column(name = "is_best_seller", nullable = false)
    private boolean isBestSeller;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "display_order")
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
