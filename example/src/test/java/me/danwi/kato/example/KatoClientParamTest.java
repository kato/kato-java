package me.danwi.kato.example;

import feign.FeignException;
import me.danwi.kato.common.exception.KatoCommonException;
import me.danwi.kato.example.argument.TestEntity;
import me.danwi.kato.example.rpc.ExceptionRpcClient;
import me.danwi.kato.example.rpc.NoUseKatoClient;
import me.danwi.kato.example.rpc.ParamRpcClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        final TestEntity entity2 = paramRpcClient.multiRequest(entity.getId(), entity.getName());
        Assertions.assertEquals(entity, entity2);
    }

    @Test
    public void test2() {
        final TestEntity entity = new TestEntity(1, "name");
        final TestEntity entity2 = noUseKatoClient.multiRequest(entity.getId());
        Assertions.assertEquals(new TestEntity(entity.getId(), entity.getId().toString()), entity2);
    }

    @Test
    public void useKatoClientTest() {
        try {
            exceptionRpcClient.commonException();
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof KatoCommonException);
        }
    }

    @Test
    public void noUseKatoClientTest() {
        try {
            noUseKatoClient.exception();
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof FeignException);
        }
    }

    @Test
    public void requestParamTest() {
        TestData test = new TestData("test");
        TestData index = paramRpcClient.index(test.getName());
        Assertions.assertEquals(test, index);
    }
}
