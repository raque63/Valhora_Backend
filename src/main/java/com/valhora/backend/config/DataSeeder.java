package com.valhora.backend.config;

import com.valhora.backend.categories.Brand;
import com.valhora.backend.categories.BrandRepository;
import com.valhora.backend.categories.Category;
import com.valhora.backend.categories.CategoryRepository;
import com.valhora.backend.common.util.Slugs;
import com.valhora.backend.products.Gender;
import com.valhora.backend.products.Movement;
import com.valhora.backend.products.Product;
import com.valhora.backend.products.ProductRepository;
import com.valhora.backend.users.Role;
import com.valhora.backend.users.User;
import com.valhora.backend.users.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Datos de arranque para desarrollo local: un usuario ADMIN de prueba y un catálogo
 * de ejemplo (marcas, categorías, productos) para poder probar filtros y CRUD sin
 * depender de fotografía real todavía. Cada paso es idempotente (no duplica datos
 * si ya existen). Se desactiva con app.seed.enabled=false.
 */
@Configuration
public class DataSeeder {

    private record SeedProduct(
            String name,
            String sku,
            BigDecimal price,
            Gender gender,
            Movement movement,
            String material,
            String strap,
            String color,
            boolean isNew,
            boolean isBestSeller) {
    }

    @Bean
    CommandLineRunner seedData(
            @Value("${app.seed.enabled}") boolean enabled,
            @Value("${app.seed.admin-email}") String adminEmail,
            @Value("${app.seed.admin-password}") String adminPassword,
            UserRepository userRepository,
            BrandRepository brandRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (!enabled) {
                return;
            }
            seedAdmin(userRepository, passwordEncoder, adminEmail, adminPassword);
            List<Brand> brands = seedBrands(brandRepository);
            List<Category> categories = seedCategories(categoryRepository);
            seedProducts(productRepository, brands, categories);
        };
    }

    private void seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User admin = User.builder()
                .name("Admin Valhora")
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
    }

    private List<Brand> seedBrands(BrandRepository brandRepository) {
        if (brandRepository.count() > 0) {
            return brandRepository.findAll();
        }
        return List.of("Solvane", "Kronoss", "Meridian", "Alto Norte").stream()
                .map(name -> brandRepository.save(Brand.builder().name(name).slug(Slugs.slugify(name)).build()))
                .toList();
    }

    private List<Category> seedCategories(CategoryRepository categoryRepository) {
        if (categoryRepository.count() > 0) {
            return categoryRepository.findAll();
        }
        return List.of("Relojes Clásicos", "Relojes Deportivos").stream()
                .map(name -> categoryRepository.save(Category.builder().name(name).slug(Slugs.slugify(name)).build()))
                .toList();
    }

    private void seedProducts(ProductRepository productRepository, List<Brand> brands, List<Category> categories) {
        if (productRepository.count() > 0) {
            return;
        }

        List<SeedProduct> seeds = List.of(
                new SeedProduct("Solvane Meridian Automatic", "SOL-AUTO-001", new BigDecimal("890.00"),
                        Gender.MEN, Movement.AUTOMATIC, "Acero inoxidable", "Metal", "Plata", true, false),
                new SeedProduct("Kronoss Chrono Sport", "KRO-CHR-002", new BigDecimal("650.00"),
                        Gender.MEN, Movement.QUARTZ, "Titanio", "Caucho", "Negro", false, true),
                new SeedProduct("Meridian Rose Classic", "MER-CLA-003", new BigDecimal("540.00"),
                        Gender.WOMEN, Movement.QUARTZ, "Acero inoxidable", "Cuero", "Rosa", true, false),
                new SeedProduct("Alto Norte Heritage", "ALT-HER-004", new BigDecimal("1200.00"),
                        Gender.UNISEX, Movement.MECHANICAL, "Oro rosa", "Cuero", "Marrón", false, true),
                new SeedProduct("Solvane Diver Pro", "SOL-DIV-005", new BigDecimal("980.00"),
                        Gender.MEN, Movement.AUTOMATIC, "Cerámica", "Metal", "Azul", true, true),
                new SeedProduct("Kronoss Minimal", "KRO-MIN-006", new BigDecimal("420.00"),
                        Gender.WOMEN, Movement.QUARTZ, "Acero inoxidable", "Metal", "Blanco", false, false)
        );

        for (int i = 0; i < seeds.size(); i++) {
            SeedProduct seed = seeds.get(i);
            Brand brand = brands.get(i % brands.size());
            Category category = categories.get(i % categories.size());
            String placeholderImage = "https://placehold.co/600x600/0d0d0f/f7f3ea?text="
                    + seed.name().replace(" ", "+");

            Product product = Product.builder()
                    .name(seed.name())
                    .slug(Slugs.slugify(seed.name()))
                    .sku(seed.sku())
                    .description("Pieza de la colección " + brand.getName()
                            + ", diseñada con precisión y materiales de alta gama.")
                    .price(seed.price())
                    .brand(brand)
                    .category(category)
                    .gender(seed.gender())
                    .movement(seed.movement())
                    .material(seed.material())
                    .strapMaterial(seed.strap())
                    .color(seed.color())
                    .stock(15)
                    .isNew(seed.isNew())
                    .isBestSeller(seed.isBestSeller())
                    .imageUrls(new ArrayList<>(List.of(placeholderImage)))
                    .build();
            productRepository.save(product);
        }
    }
}
