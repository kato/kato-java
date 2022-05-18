package me.danwi.kato.dev;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StubController {
    @RequestMapping("/.kato/stub.json")
    public String index() {
        return "stub.json content";
    }
}
