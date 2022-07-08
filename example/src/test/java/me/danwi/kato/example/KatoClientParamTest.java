package me.danwi.kato.example;

import me.danwi.kato.example.argument.TestEntity;
import me.danwi.kato.example.rpc.ParamRpcClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class KatoClientParamTest {
    @Autowired
    private ParamRpcClient paramRpcClient;

    @Test
    public void test1() {
        final TestEntity entity = new TestEntity(1, "name");
        final TestEntity entity2 = paramRpcClient.multiRequest(entity.getId(), entity.getName());
        Assertions.assertEquals(entity, entity2);
    }
}
