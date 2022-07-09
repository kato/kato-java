package me.danwi.kato.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danwi.kato.example.argument.TestEntity;
import me.danwi.kato.example.argument.TestEntity3;
import me.danwi.kato.example.argument.TestEntityAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wjy
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MultiRequestBodyWithOutAnnoControllerTest {
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
    void multiRequestPassByParam() throws JsonProcessingException {
        final TestEntity test = new TestEntity(1, "test");
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/withOutAnno/multiRequestPassByParam", objectMapper.writeValueAsString(test), TestEntity.class);
        assertThat(result.getBody()).isEqualTo(new TestEntity(test.getId(), null));
    }

    @Test
    void testMultiRequestNoJson() throws JsonProcessingException {
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/withOutAnno/multiRequest", 123, TestEntity.class);
        assertThat(new TestEntity(123, "123")).isEqualTo(result.getBody());
    }

    @Test
    void testMultiRequest() throws JsonProcessingException {
        final TestEntity test = new TestEntity(1, "test");
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/withOutAnno/multiRequest", objectMapper.writeValueAsString(test), TestEntity.class);
        assertThat(result.getBody()).isEqualTo(test);
    }

    @Test
    void testMultiRequestSingle() {
        final int id = 19;
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/withOutAnno/multiRequestSingle", id, TestEntity.class);
        assertThat(Objects.requireNonNull(result.getBody()).getId()).isEqualTo(id);
    }

    @Test
    void testMultiRequestObj() throws JsonProcessingException {
        final TestEntity test = new TestEntity(1234, "tesdfst");
        final ResponseEntity<TestEntity> result = restTemplate.postForEntity("/withOutAnno/multiRequestObj", objectMapper.writeValueAsString(test), TestEntity.class);
        assertThat(result.getBody()).isEqualTo(test);
    }

    @Test
    void multiRequestObj2() throws JsonProcessingException {
        final TestEntityAll test = new TestEntityAll(1234, null, "unknow", "name");
        final ResponseEntity<TestEntity3> result = restTemplate.postForEntity("/withOutAnno/multiRequestObj2", objectMapper.writeValueAsString(test), TestEntity3.class);
        assertThat(new TestEntity3(test.getId(), new TestEntity(test.getId(), test.getName()))).isEqualTo(result.getBody());
    }

    @Test
    void multiRequestObjLessParam() throws JsonProcessingException {
        final TestEntityAll test = new TestEntityAll(null, null, "unknow", null);
        final ResponseEntity<Map> result = restTemplate.postForEntity("/withOutAnno/multiRequestObj2", objectMapper.writeValueAsString(test), Map.class);
        Assertions.assertTrue(result.getBody().get("message").toString().contains("缺少 id 参数"));
    }


    // 不使用注解

}
