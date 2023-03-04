package me.danwi.kato.example;

import me.danwi.kato.common.exception.KatoAccessDeniedException;
import me.danwi.kato.common.exception.KatoAuthenticationException;
import me.danwi.kato.common.exception.KatoBusinessException;
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
        Assertions.assertThrowsExactly(KatoBusinessException.class, () -> exceptionRpcClient.katoBusinessException());
    }

    @Test
    public void testKatoAuth() {
        Assertions.assertThrowsExactly(KatoAuthenticationException.class, () -> exceptionRpcClient.katoAuth());
    }

    @Test
    public void testKatoAccessDenied() {
        Assertions.assertThrowsExactly(KatoAccessDeniedException.class, () -> exceptionRpcClient.katoAccessDenied());
    }


    @Test
    public void testSpringAuth() {
        Assertions.assertThrowsExactly(KatoAuthenticationException.class, () -> exceptionRpcClient.springAuth());
    }

    @Test
    public void testSpringAccessDenied() {
        Assertions.assertThrowsExactly(KatoAccessDeniedException.class, () -> exceptionRpcClient.springAccessDenied());
    }

}
