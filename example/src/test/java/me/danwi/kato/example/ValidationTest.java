package me.danwi.kato.example;

import me.danwi.kato.common.exception.KatoBadRequestException;
import me.danwi.kato.common.exception.KatoUndeclaredException;
import me.danwi.kato.example.argument.ValidationParamEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.sql.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ValidationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void validationParam() {
        final ValidationParamEntity errorAge = new ValidationParamEntity("123", -10, Collections.singleton("a"), new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24), "7777@qq.com");
        final RequestEntity<ValidationParamEntity> body = RequestEntity.method(HttpMethod.POST, "/validation/validationParam")
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorAge);
        final ResponseEntity<Map> response = restTemplate.exchange(body, Map.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(KatoBadRequestException.class.getName(), response.getBody().get("exception"));
    }

    @Test
    void validationParamWithoutAnno() {
        final ValidationParamEntity errorAge = new ValidationParamEntity("123", -10, Collections.singleton("a"), new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24), "7777@qq.com");
        final RequestEntity<ValidationParamEntity> body = RequestEntity.method(HttpMethod.POST, "/validation/validationParamWithoutAnno")
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorAge);
        final ResponseEntity<Map> response = restTemplate.exchange(body, Map.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(KatoBadRequestException.class.getName(), response.getBody().get("exception"));
    }

    @Test
    void validationResult() {
        final ValidationParamEntity errorAge = new ValidationParamEntity("123", -10, Collections.singleton("a"), new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24), "7777@qq.com");
        final RequestEntity<ValidationParamEntity> body = RequestEntity.method(HttpMethod.POST, "/validation/validationResult")
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorAge);
        final ResponseEntity<Map> response = restTemplate.exchange(body, Map.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertEquals(KatoUndeclaredException.class.getName(), response.getBody().get("exception"));
    }

    @Test
    void validSingleParam() {
        final HashMap<String, String> param = new HashMap<>();
        param.put("name", "1");
        param.put("age", "-1");
        final RequestEntity<Map<String, String>> body = RequestEntity.method(HttpMethod.POST, "/validation/validSingleParam")
                .contentType(MediaType.APPLICATION_JSON)
                .body(param);
        final ResponseEntity<Map> response = restTemplate.exchange(body, Map.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(KatoBadRequestException.class.getName(), response.getBody().get("exception"));
    }
}
