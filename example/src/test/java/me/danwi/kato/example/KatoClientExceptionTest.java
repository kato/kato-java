package me.danwi.kato.example;

import me.danwi.kato.common.exception.KatoCommonException;
import me.danwi.kato.example.rpc.ExceptionRpcClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class KatoClientExceptionTest {
    @Autowired
    private ExceptionRpcClient exceptionRpcClient;

    @Test
    public void test1() {
        final TestData index = exceptionRpcClient.index();
        Assertions.assertEquals(new TestData("kato"), index);
    }

    @Test
    public void test2() {
        try {
            exceptionRpcClient.commonException();
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof KatoCommonException);
        }
    }

}
