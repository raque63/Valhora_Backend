package com.valhora.backend.products.dto;

import java.util.List;

public record ProductImportResult(int created, int failed, List<ProductImportRowError> errors) {
}
