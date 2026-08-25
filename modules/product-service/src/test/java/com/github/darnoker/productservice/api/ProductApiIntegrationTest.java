package com.github.darnoker.productservice.api;

import com.github.darnoker.productservice.generated.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = "ADMIN")
class ProductApiIntegrationTest extends BaseApiTest {

    @Test
    void createsAndFindsBook() throws Exception {
        CreateProductRequest request = new CreateProductRequest().name("The Witcher").description("Fantasy book").price(new BigDecimal("49.99"))
                .productType(ProductType.BOOK).details(Map.of("isbn", "978-83-0000-000-0", "pages", 320, "author", "Andrzej Sapkowski", "publisher", "Example Publisher", "language", "PL"));
        MvcResult result = mvc.perform(post("/products").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andExpect(header().string("Location", containsString("/products/"))).andReturn();
        ProductDTO created = jsonMapper.readValue(result.getResponse().getContentAsString(), ProductDTO.class);
        assertEquals("The Witcher", created.getName());
        assertEquals(ProductType.BOOK, created.getProductType());
        assertNotNull(created.getId());
        mvc.perform(get(URI.create(requireNonNull(result.getResponse().getHeader("Location"))))).andExpect(status().isOk())
                .andExpect(jsonPath("$.details.author").value("Andrzej Sapkowski"));
    }

    @Test
    void listsProductsFilteredByType() throws Exception {
        CreateProductRequest request = new CreateProductRequest().name("Steel Longsword").description("Two handed sword").price(new BigDecimal("299.99"))
                .productType(ProductType.SWORD).details(Map.of("damage", 42, "weight", 3.5, "length", 115.0, "material", "STEEL"));
        mvc.perform(post("/products").contentType("application/json").content(jsonMapper.writeValueAsString(request))).andExpect(status().isCreated());
        mvc.perform(get("/products").queryParam("type", "SWORD")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productType").value("SWORD"));
    }

    @Test
    void listsProductsFilteredByIds() throws Exception {
        CreateProductRequest firstRequest = new CreateProductRequest().name("The Witcher").description("Fantasy book").price(new BigDecimal("49.99"))
                .productType(ProductType.BOOK).details(Map.of("isbn", "978-83-0000-000-0", "pages", 320, "author", "Andrzej Sapkowski", "publisher", "Example Publisher", "language", "PL"));
        CreateProductRequest secondRequest = new CreateProductRequest().name("Steel Longsword").description("Two handed sword").price(new BigDecimal("299.99"))
                .productType(ProductType.SWORD).details(Map.of("damage", 42, "weight", 3.5, "length", 115.0, "material", "STEEL"));
        ProductDTO first = jsonMapper.readValue(mvc.perform(post("/products").contentType("application/json").content(jsonMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), ProductDTO.class);
        ProductDTO second = jsonMapper.readValue(mvc.perform(post("/products").contentType("application/json").content(jsonMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), ProductDTO.class);

        mvc.perform(get("/products").queryParam("ids", first.getId().toString(), second.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
