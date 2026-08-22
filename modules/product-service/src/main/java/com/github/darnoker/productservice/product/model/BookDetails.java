package com.github.darnoker.productservice.product.model;

public record BookDetails(String isbn, Integer pages, String author, String publisher,
                          String language) implements ProductDetails {
}
