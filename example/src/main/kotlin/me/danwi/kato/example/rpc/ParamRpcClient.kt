package me.danwi.kato.example.rpc

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import me.danwi.kato.client.KatoClient
import me.danwi.kato.common.argument.MultiRequestBody
import me.danwi.kato.example.argument.TestEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Validated
@KatoClient(value = "testParam", url = "http://localhost:8888")
interface ParamRpcClient {

    @PostMapping("/withOutAnno/multiRequest")
    @Valid
    fun multiRequest(@Valid @Positive id: Int, @Valid @Size(min = 2, max = 3) name: String): TestEntity

    @PostMapping("/withOutAnno/multiRequest")
    @Valid
    fun multiRequestWhitAnno(@MultiRequestBody @Valid @Positive id: Int, @MultiRequestBody @Valid @Size(min = 2, max = 3) name: String): TestEntity

    @RequestMapping("/param")
    fun index(@RequestParam name: String, age: Int): Map<String, Any>
}