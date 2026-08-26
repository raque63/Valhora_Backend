package com.valhora.backend.products;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

final class ProductSpecifications {

    private ProductSpecifications() {
    }

    static Specification<Product> hasBrand(UUID brandId) {
        return (root, query, cb) -> brandId == null ? null : cb.equal(root.get("brand").get("id"), brandId);
    }

    static Specification<Product> hasCategory(UUID categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    static Specification<Product> hasGender(Gender gender) {
        return (root, query, cb) -> gender == null ? null : cb.equal(root.get("gender"), gender);
    }

    static Specification<Product> hasMovement(Movement movement) {
        return (root, query, cb) -> movement == null ? null : cb.equal(root.get("movement"), movement);
    }

    static Specification<Product> hasMaterial(String material) {
        return (root, query, cb) -> material == null
                ? null
                : cb.equal(cb.lower(root.get("material")), material.toLowerCase());
    }

    static Specification<Product> hasStrapMaterial(String strapMaterial) {
        return (root, query, cb) -> strapMaterial == null
                ? null
                : cb.equal(cb.lower(root.get("strapMaterial")), strapMaterial.toLowerCase());
    }

    static Specification<Product> hasColor(String color) {
        return (root, query, cb) -> color == null
                ? null
                : cb.equal(cb.lower(root.get("color")), color.toLowerCase());
    }

    static Specification<Product> priceGreaterThanOrEqual(BigDecimal min) {
        return (root, query, cb) -> min == null ? null : cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    static Specification<Product> priceLessThanOrEqual(BigDecimal max) {
        return (root, query, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("price"), max);
    }

    static Specification<Product> hasAvailability(Availability availability) {
        return (root, query, cb) -> availability == null ? null : cb.equal(root.get("availability"), availability);
    }

    static Specification<Product> isNew(Boolean isNew) {
        return (root, query, cb) -> isNew == null ? null : cb.equal(root.get("isNew"), isNew);
    }

    static Specification<Product> isBestSeller(Boolean isBestSeller) {
        return (root, query, cb) -> isBestSeller == null ? null : cb.equal(root.get("isBestSeller"), isBestSeller);
    }
}
