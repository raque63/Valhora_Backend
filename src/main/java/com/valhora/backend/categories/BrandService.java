package com.valhora.backend.categories;

import com.valhora.backend.categories.dto.BrandRequest;
import com.valhora.backend.categories.dto.BrandResponse;
import com.valhora.backend.common.exception.DuplicateResourceException;
import com.valhora.backend.common.exception.ResourceNotFoundException;
import com.valhora.backend.common.storage.CloudinaryService;
import com.valhora.backend.common.util.Slugs;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final CloudinaryService cloudinaryService;

    public BrandService(BrandRepository brandRepository, BrandMapper brandMapper, CloudinaryService cloudinaryService) {
        this.brandRepository = brandRepository;
        this.brandMapper = brandMapper;
        this.cloudinaryService = cloudinaryService;
    }

    public List<BrandResponse> findAll() {
        return brandRepository.findAll().stream()
                .map(brandMapper::toResponse)
                .toList();
    }

    @Transactional
    public BrandResponse create(BrandRequest request) {
        if (brandRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Ya existe una marca con ese nombre");
        }
        Brand brand = Brand.builder()
                .name(request.name())
                .slug(Slugs.slugify(request.name()))
                .build();
        return brandMapper.toResponse(brandRepository.save(brand));
    }

    @Transactional
    public BrandResponse update(UUID id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));
        brand.setName(request.name());
        brand.setSlug(Slugs.slugify(request.name()));
        return brandMapper.toResponse(brandRepository.save(brand));
    }

    @Transactional
    public void delete(UUID id) {
        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Marca no encontrada");
        }
        brandRepository.deleteById(id);
    }

    @Transactional
    public BrandResponse uploadLogo(UUID id, MultipartFile file) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));
        brand.setLogoUrl(cloudinaryService.uploadBrandLogo(file));
        return brandMapper.toResponse(brandRepository.save(brand));
    }
}
