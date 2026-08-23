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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImportService {

    private static final String[] TEMPLATE_HEADERS = {
        "coleccion", "referencia", "descripcion", "precio", "marca", "categoria", "genero", "movimiento",
        "material", "material_brazalete", "color", "reserva_marcha",
        "diametro_caja", "material_cristal", "resistencia_agua", "stock", "disponibilidad",
        "nuevo", "mas_vendido"
    };

    private static final String[] TEMPLATE_EXAMPLE = {
        "Seiko 5 Sports SSK019", "SSK019", "Reloj automático con caja de acero inoxidable.", "85000",
        "Seiko", "", "MEN", "AUTOMATIC", "Acero inoxidable", "Acero inoxidable", "Gris carbón",
        "Aprox. 41 horas", "42,5 mm",
        "Hardlex con lupa", "100 m / 10 bar", "5", "IMMEDIATE", "false", "false"
    };

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

    public ProductImportResult importExcel(MultipartFile file) {
        List<ProductImportRowError> errors = new ArrayList<>();
        int created = 0;

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalArgumentException("El archivo no tiene encabezados");
            }
            Map<String, Integer> columnIndex = readHeader(headerRow);

            for (int rowNum = headerRow.getRowNum() + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null || isRowBlank(row, columnIndex)) {
                    continue;
                }
                try {
                    ProductRequest request = toRequest(row, columnIndex);
                    validate(request);
                    productService.create(request);
                    created++;
                } catch (Exception ex) {
                    errors.add(new ProductImportRowError(rowNum + 1, ex.getMessage()));
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo leer el archivo Excel: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("El archivo no es un Excel (.xlsx) válido");
        }

        return new ProductImportResult(created, errors.size(), errors);
    }

    public byte[] buildTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos");

            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
                header.createCell(i).setCellValue(TEMPLATE_HEADERS[i]);
            }

            Row example = sheet.createRow(1);
            for (int i = 0; i < TEMPLATE_EXAMPLE.length; i++) {
                example.createCell(i).setCellValue(TEMPLATE_EXAMPLE[i]);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar la plantilla", ex);
        }
    }

    private Map<String, Integer> readHeader(Row headerRow) {
        Map<String, Integer> columnIndex = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = cellToString(cell).toLowerCase(Locale.ROOT);
            if (!header.isBlank()) {
                columnIndex.put(header, cell.getColumnIndex());
            }
        }
        return columnIndex;
    }

    private boolean isRowBlank(Row row, Map<String, Integer> columnIndex) {
        for (int index : columnIndex.values()) {
            if (!cellToString(row.getCell(index)).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private ProductRequest toRequest(Row row, Map<String, Integer> columnIndex) {
        String brandName = get(row, columnIndex, "marca");
        Brand brand = brandRepository.findByNameIgnoreCase(brandName)
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + brandName));

        String categoryName = get(row, columnIndex, "categoria");
        UUID categoryId = null;
        if (!categoryName.isBlank()) {
            Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + categoryName));
            categoryId = category.getId();
        }

        return new ProductRequest(
                get(row, columnIndex, "coleccion"),
                get(row, columnIndex, "referencia"),
                getOrNull(row, columnIndex, "descripcion"),
                parsePrice(get(row, columnIndex, "precio")),
                brand.getId(),
                categoryId,
                parseGender(get(row, columnIndex, "genero")),
                parseMovement(get(row, columnIndex, "movimiento")),
                get(row, columnIndex, "material"),
                get(row, columnIndex, "material_brazalete"),
                get(row, columnIndex, "color"),
                getOrNull(row, columnIndex, "reserva_marcha"),
                getOrNull(row, columnIndex, "diametro_caja"),
                getOrNull(row, columnIndex, "material_cristal"),
                getOrNull(row, columnIndex, "resistencia_agua"),
                parseStock(get(row, columnIndex, "stock")),
                parseAvailability(get(row, columnIndex, "disponibilidad")),
                parseBoolean(getOrNull(row, columnIndex, "nuevo")),
                parseBoolean(getOrNull(row, columnIndex, "mas_vendido")));
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

    private String get(Row row, Map<String, Integer> columnIndex, String column) {
        Integer index = columnIndex.get(column);
        if (index == null) {
            return "";
        }
        return cellToString(row.getCell(index));
    }

    private String getOrNull(Row row, Map<String, Integer> columnIndex, String column) {
        String value = get(row, columnIndex, column);
        return value.isBlank() ? null : value;
    }

    private String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> formatNumeric(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private String formatNumeric(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
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
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("TRUE") || normalized.equals("SI") || normalized.equals("SÍ") || normalized.equals("1");
    }

    private Gender parseGender(String raw) {
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
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
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
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
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
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
