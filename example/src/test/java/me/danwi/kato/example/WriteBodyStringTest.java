package me.danwi.kato.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WriteBodyStringTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MockMvc mvc;

    @Test
    void bigInteger() throws Exception {
        final ResponseEntity<Object> result = restTemplate.postForEntity("/bigInteger", null, Object.class);
        Assertions.assertEquals("-1", result.getBody().toString());

        mvc.perform(post("/bigInteger")).andExpect(
                result1 -> Assertions.assertEquals("-1", result1.getResponse().getContentAsString())
        );
    }

}
