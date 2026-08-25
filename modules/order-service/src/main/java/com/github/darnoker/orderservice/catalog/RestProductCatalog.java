package com.github.darnoker.orderservice.catalog;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
class RestProductCatalog implements ProductCatalog {

    private final RestClient restClient;

    private static final String PRODUCT_NOT_FOUND_MESSAGE = "product not found: %s";

    RestProductCatalog(@Value("${product-catalog.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    @SneakyThrows
    public List<ProductSnapshot> getProducts(List<UUID> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }

        final var response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/products")
                        .queryParam("ids", productIds.toArray())
                        .build())
                .retrieve()
                .body(ProductResponse[].class);

        if (response == null) {
            throw new ProductNotFoundException(PRODUCT_NOT_FOUND_MESSAGE.formatted(productIds));
        }

        List<ProductSnapshot> products = Arrays.stream(response)
                .map(productResponse ->
                        new ProductSnapshot(productResponse.id, productResponse.name, productResponse.productType, productResponse.price))
                .toList();

        Set<UUID> foundIds = products.stream().map(ProductSnapshot::id).collect(Collectors.toSet());
        UUID missingId = productIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElse(null);
        if (missingId != null) {
            throw new ProductNotFoundException(PRODUCT_NOT_FOUND_MESSAGE.formatted(missingId));
        }
        return products;
    }

    private record ProductResponse(UUID id, String name, String productType, BigDecimal price) {
    }
}
