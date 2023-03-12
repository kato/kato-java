package me.danwi.kato.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danwi.kato.example.argument.TestEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wjy
 */
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MultiRequestBodyWithOutAnnoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void multiRequestPassByMethod() throws JsonProcessingException {
        final TestEntity test = new TestEntity(1, "test");
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/withOutAnno/multiRequestPassByMethod", objectMapper.writeValueAsString(test), TestEntity.class);
        assertThat(result.getBody()).isEqualTo(new TestEntity(null, null));
    }

    @Test
    void testMultiRequestNoJson() {
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/withOutAnno/multiRequest/123/123", null, TestEntity.class);
        assertThat(result.getBody()).isEqualTo(new TestEntity(123, "123"));
    }

    @Test
    void testMultiRequest() {
        final TestEntity test = new TestEntity(1, "test");
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/withOutAnno/multiRequest/1/test", null, TestEntity.class);
        assertThat(result.getBody()).isEqualTo(test);
    }

    @Test
    void testMultiRequestSingle() {
        final int id = 19;
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/withOutAnno/multiRequestSingle", id, TestEntity.class);
        assertThat(Objects.requireNonNull(result.getBody()).getId()).isEqualTo(id);
    }

    @Test
    void testMultiRequestObj() throws URISyntaxException {
        final TestEntity test = new TestEntity(1234, "tesdfst");
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final ResponseEntity<TestEntity> result = restTemplate.exchange(new RequestEntity<>(test, headers, HttpMethod.POST, new URI("/withOutAnno/multiRequestObj")), TestEntity.class);
        assertThat(test).isEqualTo(result.getBody());
    }

}
