package me.danwi.kato.example

import me.danwi.kato.client.ImportKatoClients
import me.danwi.kato.common.exception.ExceptionExtraDataHolder
import me.danwi.kato.common.exception.KatoBusinessException
import me.danwi.kato.common.exception.KatoException
import me.danwi.kato.server.EnableKatoServer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@ImportKatoClients
@EnableKatoServer
class ExampleApplication

fun main(args: Array<String>) {
    runApplication<ExampleApplication>(*args)
}

data class TestData(val name: String)

@RestController
class TestController {
    @RequestMapping("/")
    fun index(): TestData {
        return TestData("kato")
    }

    @RequestMapping("/param")
    fun index(@RequestParam name: String, @RequestBody age: Int): Map<String, Any> {
        return mapOf("name" to name, "age" to age);
    }

    @RequestMapping("/katoBusinessException")
    fun katoBusinessException() {
        throw KatoBusinessException()
    }

    @RequestMapping("/exception")
    fun exception() {
        throw Exception("该方法不能调用")
    }

    class UserNotFoundException(private var userName: String) : KatoException("fat"), ExceptionExtraDataHolder {
        override fun toMap(): MutableMap<String, Any> {
            return mutableMapOf(Pair("userName", userName))
        }

        override fun loadFromMap(map: MutableMap<String, Any>) {
            this.userName = map["userName"] as String
        }
    }

    @RequestMapping("/fat-exception")
    fun fatException() {
        throw UserNotFoundException("kato-user")
    }
}