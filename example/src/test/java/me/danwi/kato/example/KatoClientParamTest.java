package me.danwi.kato.example;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import me.danwi.kato.common.exception.KatoBusinessException;
import me.danwi.kato.example.argument.TestEntity;
import me.danwi.kato.example.rpc.ExceptionRpcClient;
import me.danwi.kato.example.rpc.NoUseKatoClient;
import me.danwi.kato.example.rpc.ParamRpcClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class KatoClientParamTest {
    @Autowired
    private ParamRpcClient paramRpcClient;

    @Autowired
    private ExceptionRpcClient exceptionRpcClient;

    @Autowired
    private NoUseKatoClient noUseKatoClient;

    @Test
    public void test1() {
        final TestEntity entity = new TestEntity(1, "name");
        Assertions.assertThrowsExactly(ConstraintViolationException.class, () -> paramRpcClient.multiRequest(entity.getId(), entity.getName()));
        final TestEntity entity2 = entity.copy(2, "ss");
        Assertions.assertEquals(entity2, paramRpcClient.multiRequest(entity2.getId(), entity2.getName()));
    }

    @Test
    public void multiRequestWhitAnno() {
        final TestEntity entity = new TestEntity(1, "name");
        Assertions.assertThrowsExactly(ConstraintViolationException.class, () -> paramRpcClient.multiRequestWhitAnno(entity.getId(), entity.getName()));
        final TestEntity entity2 = entity.copy(2, "ss");
        Assertions.assertEquals(entity2, paramRpcClient.multiRequestWhitAnno(entity2.getId(), entity2.getName()));
    }

    @Test
    public void test2() {
        final TestEntity entity = new TestEntity(1, "name");
        final TestEntity entity2 = noUseKatoClient.multiRequest(entity.getId());
        Assertions.assertEquals(new TestEntity(entity.getId(), entity.getId().toString()), entity2);
    }

    @Test
    public void useKatoClientTest() {
        Assertions.assertThrowsExactly(KatoBusinessException.class, () -> exceptionRpcClient.katoBusinessException());
    }

    @Test
    public void noUseKatoClientTest() {
        Assertions.assertThrowsExactly(FeignException.InternalServerError.class, () -> noUseKatoClient.exception());
    }

    @Test
    public void requestParamTest() {
        Map<String, Object> index = paramRpcClient.index("test", 10);
        Assertions.assertEquals("test", index.get("name"));
        Assertions.assertEquals(10, index.get("age"));
    }
}
