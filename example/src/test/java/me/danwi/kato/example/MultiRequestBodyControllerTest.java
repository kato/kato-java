package me.danwi.kato.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danwi.kato.example.controller.TestEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wjy
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MultiRequestBodyControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testMultiRequest() throws JsonProcessingException {
        final TestEntity test = new TestEntity(1, "test");
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/multiRequest", objectMapper.writeValueAsString(test), TestEntity.class);
        assertThat(result.getBody()).isEqualTo(test);
    }


    @Test
    void testMultiRequestSingle() {
        final int id = 19;
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/multiRequestSingle", id, TestEntity.class);
        assertThat(Objects.requireNonNull(result.getBody()).getId()).isEqualTo(id);
    }


    @Test
    void testMultiRequestObj() throws JsonProcessingException {
        final TestEntity test = new TestEntity(1234, "tesdfst");
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/multiRequestObj", objectMapper.writeValueAsString(test), TestEntity.class);
        assertThat(result.getBody()).isEqualTo(test);
    }
}
