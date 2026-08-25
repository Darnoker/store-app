package com.github.darnoker.productservice.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
abstract class BaseApiTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JsonMapper jsonMapper;

}
