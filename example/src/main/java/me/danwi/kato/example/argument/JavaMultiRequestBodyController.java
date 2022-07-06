package me.danwi.kato.example.argument;

import me.danwi.kato.common.argument.MultiRequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wjy
 */
@RestController
public class JavaMultiRequestBodyController {
    @PostMapping("/multiRequestKotlinNullableJava")
    public TestEntity multiRequestKotlinNullableJava(@MultiRequestBody(required = false) Integer id) {
        return new TestEntity(id, null);
    }

    @PostMapping("/multiRequestKotlinNullableJavaErr")
    public TestEntity multiRequestKotlinNullableJavaErr(@MultiRequestBody Integer id) {
        return new TestEntity(id, null);
    }
}
