package me.danwi.kato.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danwi.kato.example.argument.TestEntityAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CollectionParamTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void multiRequestKotlinNullableJava() throws JsonProcessingException {
        final ArrayList<TestEntityAll> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new TestEntityAll(i, null, "a" + i, "b" + i));
        }
        final ResponseEntity<String> result = restTemplate.postForEntity("/collection/listParam", objectMapper.writeValueAsString(list), String.class);
        assertThat(objectMapper.readValue(result.getBody(), String.class)).isEqualTo(list.stream().map(l -> "" + l.getId()).collect(Collectors.joining("")));
    }

}
