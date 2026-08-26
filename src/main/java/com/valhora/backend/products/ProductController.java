package com.valhora.backend.products;

import com.valhora.backend.products.dto.ProductResponse;
import com.valhora.backend.products.dto.ProductSummaryResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<ProductSummaryResponse> search(
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Movement movement,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String strapMaterial,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Availability availability,
            @RequestParam(required = false) Boolean isNew,
            @RequestParam(required = false) Boolean isBestSeller,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ProductFilter filter = new ProductFilter(
                brandId, categoryId, gender, movement, material, strapMaterial, color, minPrice, maxPrice,
                availability, isNew, isBestSeller);
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));

        return productService.search(filter, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    @GetMapping("/{id}/related")
    public List<ProductSummaryResponse> findRelated(@PathVariable UUID id) {
        return productService.findRelated(id);
    }

    private Sort resolveSort(String sort) {
        return switch (sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "bestseller" -> Sort.by(Sort.Order.desc("isBestSeller"), Sort.Order.desc("createdAt"));
            default -> Sort.by("createdAt").descending();
        };
    }
}
