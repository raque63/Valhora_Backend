package com.valhora.backend.products;

import com.valhora.backend.categories.Brand;
import com.valhora.backend.categories.BrandRepository;
import com.valhora.backend.categories.Category;
import com.valhora.backend.categories.CategoryRepository;
import com.valhora.backend.products.dto.ProductImportResult;
import com.valhora.backend.products.dto.ProductImportRowError;
import com.valhora.backend.products.dto.ProductRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImportService {

    private static final Map<String, Gender> GENDER_ALIASES = Map.of(
            "HOMBRE", Gender.MEN,
            "MUJER", Gender.WOMEN);

    private static final Map<String, Movement> MOVEMENT_ALIASES = Map.of(
            "CUARZO", Movement.QUARTZ,
            "AUTOMATICO", Movement.AUTOMATIC,
            "AUTOMÁTICO", Movement.AUTOMATIC,
            "MECANICO", Movement.MECHANICAL,
            "MECÁNICO", Movement.MECHANICAL);

    private static final Map<String, Availability> AVAILABILITY_ALIASES = Map.of(
            "INMEDIATA", Availability.IMMEDIATE,
            "EN_CAMINO", Availability.IN_TRANSIT,
            "POR_ENCARGO", Availability.BY_ORDER);

    private final ProductService productService;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final Validator validator;

    public ProductImportService(
            ProductService productService,
            BrandRepository brandRepository,
            CategoryRepository categoryRepository,
            Validator validator) {
        this.productService = productService;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.validator = validator;
    }

    public ProductImportResult importCsv(MultipartFile file) {
        List<ProductImportRowError> errors = new ArrayList<>();
        int created = 0;

        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreSurroundingSpaces(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)) {

            int rowNumber = 2;
            for (CSVRecord record : parser) {
                try {
                    ProductRequest request = toRequest(record);
                    validate(request);
                    productService.create(request);
                    created++;
                } catch (Exception ex) {
                    errors.add(new ProductImportRowError(rowNumber, ex.getMessage()));
                }
                rowNumber++;
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo leer el archivo CSV: " + ex.getMessage());
        }

        return new ProductImportResult(created, errors.size(), errors);
    }

    private ProductRequest toRequest(CSVRecord record) {
        String brandName = get(record, "marca");
        Brand brand = brandRepository.findByNameIgnoreCase(brandName)
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + brandName));

        String categoryName = get(record, "categoria");
        UUID categoryId = null;
        if (!categoryName.isBlank()) {
            Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + categoryName));
            categoryId = category.getId();
        }

        return new ProductRequest(
                get(record, "coleccion"),
                get(record, "sku"),
                getOrNull(record, "descripcion"),
                parsePrice(get(record, "precio")),
                brand.getId(),
                categoryId,
                parseGender(get(record, "genero")),
                parseMovement(get(record, "movimiento")),
                get(record, "material"),
                get(record, "correa"),
                get(record, "color"),
                getOrNull(record, "detalle_movimiento"),
                getOrNull(record, "calibre"),
                getOrNull(record, "reserva_marcha"),
                getOrNull(record, "diametro_caja"),
                getOrNull(record, "grosor"),
                getOrNull(record, "cristal"),
                getOrNull(record, "resistencia_agua"),
                parseStock(get(record, "stock")),
                parseAvailability(get(record, "disponibilidad")),
                parseBoolean(getOrNull(record, "nuevo")),
                parseBoolean(getOrNull(record, "mas_vendido")));
    }

    private void validate(ProductRequest request) {
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Datos inválidos");
            throw new IllegalArgumentException(message);
        }
    }

    private String get(CSVRecord record, String column) {
        if (!record.isMapped(column) || record.get(column) == null) {
            return "";
        }
        return record.get(column).trim();
    }

    private String getOrNull(CSVRecord record, String column) {
        String value = get(record, column);
        return value.isBlank() ? null : value;
    }

    private BigDecimal parsePrice(String raw) {
        try {
            return new BigDecimal(raw.replace(",", "."));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Precio inválido: " + raw);
        }
    }

    private int parseStock(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Stock inválido: " + raw);
        }
    }

    private boolean parseBoolean(String raw) {
        if (raw == null) {
            return false;
        }
        String normalized = raw.trim().toUpperCase();
        return normalized.equals("TRUE") || normalized.equals("SI") || normalized.equals("SÍ") || normalized.equals("1");
    }

    private Gender parseGender(String raw) {
        String normalized = raw.trim().toUpperCase();
        Gender alias = GENDER_ALIASES.get(normalized);
        if (alias != null) {
            return alias;
        }
        try {
            return Gender.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Género inválido: " + raw);
        }
    }

    private Movement parseMovement(String raw) {
        String normalized = raw.trim().toUpperCase();
        Movement alias = MOVEMENT_ALIASES.get(normalized);
        if (alias != null) {
            return alias;
        }
        try {
            return Movement.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Movimiento inválido: " + raw);
        }
    }

    private Availability parseAvailability(String raw) {
        String normalized = raw.trim().toUpperCase();
        Availability alias = AVAILABILITY_ALIASES.get(normalized);
        if (alias != null) {
            return alias;
        }
        try {
            return Availability.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Disponibilidad inválida: " + raw);
        }
    }
}
