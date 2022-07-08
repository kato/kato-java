package me.danwi.kato.example.rpc

import me.danwi.kato.client.KatoClient
import me.danwi.kato.example.TestData
import me.danwi.kato.example.argument.TestEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@KatoClient(value = "testParam", url = "http://localhost:8888")
interface ParamRpcClient {

    @PostMapping("/withOutAnno/multiRequest")
    fun multiRequest(id: Int, name: String): TestEntity

    @RequestMapping("/param")
    fun index(@RequestParam name: String): TestData
}