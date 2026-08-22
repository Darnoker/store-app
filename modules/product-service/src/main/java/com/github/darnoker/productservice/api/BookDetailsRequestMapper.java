package com.github.darnoker.productservice.api;

import com.github.darnoker.productservice.product.model.BookDetails;

import java.util.Map;

final class BookDetailsRequestMapper implements ProductDetailsRequestMapper {

    @Override
    public BookDetails toDomain(Map<String, Object> details) {
        return new BookDetails(
                string(details, "isbn"),
                integer(details, "pages"),
                string(details, "author"),
                string(details, "publisher"),
                string(details, "language"));
    }
}
