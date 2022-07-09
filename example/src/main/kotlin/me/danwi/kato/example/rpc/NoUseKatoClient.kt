package me.danwi.kato.example.rpc

import me.danwi.kato.example.argument.TestEntity
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@FeignClient(value = "testParamFeign", url = "http://localhost:8888")
interface NoUseKatoClient {

    @PostMapping("/withOutAnno/multiRequest2")
    fun multiRequest(id: Int): TestEntity;

    @RequestMapping("/exception")
    fun exception()
}