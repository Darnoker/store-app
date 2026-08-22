package com.github.darnoker.productservice.product.model;

public sealed interface ProductDetails permits BookDetails, SwordDetails {
}
