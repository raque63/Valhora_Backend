package com.valhora.backend.products;

import com.valhora.backend.categories.Brand;
import com.valhora.backend.categories.BrandRepository;
import com.valhora.backend.categories.Category;
import com.valhora.backend.categories.CategoryRepository;
import com.valhora.backend.common.exception.DuplicateResourceException;
import com.valhora.backend.common.exception.ResourceNotFoundException;
import com.valhora.backend.common.storage.CloudinaryService;
import com.valhora.backend.common.util.Slugs;
import com.valhora.backend.products.dto.ProductRequest;
import com.valhora.backend.products.dto.ProductResponse;
import com.valhora.backend.products.dto.ProductSummaryResponse;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductService {

    private static final SecureRandom SUFFIX_RANDOM = new SecureRandom();

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;

    public ProductService(
            ProductRepository productRepository,
            BrandRepository brandRepository,
            CategoryRepository categoryRepository,
            ProductMapper productMapper,
            CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.cloudinaryService = cloudinaryService;
    }

    public Page<ProductSummaryResponse> search(ProductFilter filter, Pageable pageable) {
        Specification<Product> spec = Specification.where(ProductSpecifications.hasBrand(filter.brandId()))
                .and(ProductSpecifications.hasCategory(filter.categoryId()))
                .and(ProductSpecifications.hasGender(filter.gender()))
                .and(ProductSpecifications.hasMovement(filter.movement()))
                .and(ProductSpecifications.hasMaterial(filter.material()))
                .and(ProductSpecifications.hasStrapMaterial(filter.strapMaterial()))
                .and(ProductSpecifications.hasColor(filter.color()))
                .and(ProductSpecifications.priceGreaterThanOrEqual(filter.minPrice()))
                .and(ProductSpecifications.priceLessThanOrEqual(filter.maxPrice()))
                .and(ProductSpecifications.hasAvailability(filter.availability()));

        return productRepository.findAll(spec, pageable).map(productMapper::toSummary);
    }

    public ProductResponse findById(UUID id) {
        return productMapper.toResponse(getOrThrow(id));
    }

    public List<ProductSummaryResponse> findRelated(UUID id) {
        Product product = getOrThrow(id);
        return productRepository.findTop4ByBrand_IdAndIdNotOrderByCreatedAtDesc(product.getBrand().getId(), id).stream()
                .map(productMapper::toSummary)
                .toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new DuplicateResourceException("Ya existe un producto con ese SKU");
        }

        Product product = Product.builder()
                .name(request.name())
                .slug(generateUniqueSlug(request.name()))
                .sku(request.sku())
                .description(request.description())
                .price(request.price())
                .brand(findBrand(request.brandId()))
                .category(findCategory(request.categoryId()))
                .gender(request.gender())
                .movement(request.movement())
                .material(request.material())
                .strapMaterial(request.strapMaterial())
                .color(request.color())
                .stock(request.stock())
                .availability(request.availability())
                .isNew(request.isNew())
                .isBestSeller(request.isBestSeller())
                .build();

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getOrThrow(id);

        if (!product.getSku().equalsIgnoreCase(request.sku()) && productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new DuplicateResourceException("Ya existe un producto con ese SKU");
        }

        product.setName(request.name());
        product.setSku(request.sku());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setBrand(findBrand(request.brandId()));
        product.setCategory(findCategory(request.categoryId()));
        product.setGender(request.gender());
        product.setMovement(request.movement());
        product.setMaterial(request.material());
        product.setStrapMaterial(request.strapMaterial());
        product.setColor(request.color());
        product.setStock(request.stock());
        product.setAvailability(request.availability());
        product.setNew(request.isNew());
        product.setBestSeller(request.isBestSeller());

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado");
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductResponse duplicate(UUID id) {
        Product original = getOrThrow(id);

        Product copy = Product.builder()
                .name(original.getName() + " (copia)")
                .slug(generateUniqueSlug(original.getName() + " copia"))
                .sku(generateUniqueSku(original.getSku()))
                .description(original.getDescription())
                .price(original.getPrice())
                .brand(original.getBrand())
                .category(original.getCategory())
                .gender(original.getGender())
                .movement(original.getMovement())
                .material(original.getMaterial())
                .strapMaterial(original.getStrapMaterial())
                .color(original.getColor())
                .stock(original.getStock())
                .availability(original.getAvailability())
                .isNew(original.isNew())
                .isBestSeller(false)
                .imageUrls(new ArrayList<>(original.getImageUrls()))
                .build();

        return productMapper.toResponse(productRepository.save(copy));
    }

    @Transactional
    public ProductResponse addImage(UUID id, MultipartFile file) {
        Product product = getOrThrow(id);
        String url = cloudinaryService.upload(file);
        product.getImageUrls().add(url);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse removeImage(UUID id, String imageUrl) {
        Product product = getOrThrow(id);
        product.getImageUrls().remove(imageUrl);
        return productMapper.toResponse(productRepository.save(product));
    }

    private Product getOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    private Brand findBrand(UUID brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));
    }

    private Category findCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
    }

    private String generateUniqueSlug(String name) {
        String base = Slugs.slugify(name);
        String candidate = base;
        while (productRepository.existsBySlugIgnoreCase(candidate)) {
            candidate = base + "-" + randomSuffix();
        }
        return candidate;
    }

    private String generateUniqueSku(String baseSku) {
        String candidate = baseSku + "-" + randomSuffix();
        while (productRepository.existsBySkuIgnoreCase(candidate)) {
            candidate = baseSku + "-" + randomSuffix();
        }
        return candidate;
    }

    private String randomSuffix() {
        return Integer.toHexString(SUFFIX_RANDOM.nextInt(0xFFFF));
    }
}
