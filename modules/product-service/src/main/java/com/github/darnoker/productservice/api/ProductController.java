package com.github.darnoker.productservice.api;

import com.github.darnoker.productservice.generated.api.ProductsApi;
import com.github.darnoker.productservice.generated.model.CreateProductRequest;
import com.github.darnoker.productservice.generated.model.ProductDTO;
import com.github.darnoker.productservice.generated.model.ProductType;
import com.github.darnoker.productservice.product.ProductService;
import com.github.darnoker.productservice.product.model.BookDetails;
import com.github.darnoker.productservice.product.model.Product;
import com.github.darnoker.productservice.product.model.ProductDetails;
import com.github.darnoker.productservice.product.model.SwordDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductsApi {

    private final ProductService productService;

    @Override
    public ResponseEntity<ProductDTO> createProduct(CreateProductRequest request) {
        return created(productService.create(ProductApiMapper.toCommand(request)));
    }

    @Override
    public ResponseEntity<ProductDTO> getProduct(UUID productId) {
        return productService.findById(productId)
                .map(product -> ResponseEntity.ok(toDto(product)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<ProductDTO>> listProducts(List<UUID> ids, ProductType type, BigDecimal minPrice, BigDecimal maxPrice) {
        com.github.darnoker.productservice.product.ProductType domainType = type == null ? null : com.github.darnoker.productservice.product.ProductType.valueOf(type.getValue());
        return ResponseEntity.ok(productService.findAll(ids, domainType, minPrice, maxPrice).stream().map(this::toDto).toList());
    }

    private ResponseEntity<ProductDTO> created(Product product) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{productId}").buildAndExpand(product.id()).toUri();
        return ResponseEntity.created(location).body(toDto(product));
    }

    private ProductDTO toDto(Product product) {
        return new ProductDTO().id(product.id()).name(product.name()).description(product.description()).price(product.price())
                .productType(ProductType.fromValue(product.productType().name())).details(details(product.details()))
                .createdAt(OffsetDateTime.ofInstant(product.createdAt(), ZoneOffset.UTC)).updatedAt(OffsetDateTime.ofInstant(product.updatedAt(), ZoneOffset.UTC));
    }

    private Map<String, Object> details(ProductDetails details) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (details instanceof BookDetails book) {
            values.put("isbn", book.isbn());
            values.put("pages", book.pages());
            values.put("author", book.author());
            values.put("publisher", book.publisher());
            values.put("language", book.language());
        } else if (details instanceof SwordDetails sword) {
            values.put("damage", sword.damage());
            values.put("weight", sword.weight());
            values.put("length", sword.length());
            values.put("material", sword.material());
        }
        return values;
    }
}
