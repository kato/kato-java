package me.danwi.kato.example.response

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigInteger

@RestController
class WriteBodyStringController {

    @RequestMapping("/bigInteger")
    fun katoAccessDenied(): BigInteger {
        return BigInteger.ONE.negate()
    }
}