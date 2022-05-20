package me.danwi.kato.dev;

import me.danwi.kato.dev.stub.ApplicationStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StubController {

    @Autowired
    private IndexerFactory indexerFactory;

    @RequestMapping("/.kato/stub.json")
    public ApplicationStub index() {
        return indexerFactory.getIndexer().generateApplicationStub();
    }
}
