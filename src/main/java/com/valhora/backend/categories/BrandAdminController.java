package com.valhora.backend.categories;

import com.valhora.backend.categories.dto.BrandRequest;
import com.valhora.backend.categories.dto.BrandResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/brands")
public class BrandAdminController {

    private final BrandService brandService;

    public BrandAdminController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BrandResponse create(@Valid @RequestBody BrandRequest request) {
        return brandService.create(request);
    }

    @PutMapping("/{id}")
    public BrandResponse update(@PathVariable UUID id, @Valid @RequestBody BrandRequest request) {
        return brandService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        brandService.delete(id);
    }

    @PostMapping(path = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BrandResponse uploadLogo(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return brandService.uploadLogo(id, file);
    }
}
