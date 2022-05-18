package me.danwi.kato.dev;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("me.danwi.kato.dev.StubController")
public class StubController {
    @RequestMapping("/.well-known/kato/stub.json")
    public String index() {
        return "stub.json content";
    }
}
