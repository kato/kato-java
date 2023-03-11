package me.danwi.kato.example.rpc

import me.danwi.kato.client.KatoClient
import me.danwi.kato.example.TestData
import org.springframework.web.bind.annotation.RequestMapping

@KatoClient("test", url = "http://localhost:8888")
interface ExceptionRpcClient {

    @RequestMapping("/")
    fun index(): TestData

    @RequestMapping("/katoBusinessException")
    fun katoBusinessException()


    @RequestMapping("/katoAuth")
    fun katoAuth()

    @RequestMapping("/katoAccessDenied")
    fun katoAccessDenied()

    @RequestMapping("/springAuth")
    fun springAuth()

    @RequestMapping("/springAccessDenied")
    fun springAccessDenied()

    @RequestMapping("/katoBusinessCodeException")
    fun katoBusinessCodeException()
}