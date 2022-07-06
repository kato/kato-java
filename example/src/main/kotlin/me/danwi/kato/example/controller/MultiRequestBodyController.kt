package me.danwi.kato.example.controller

import me.danwi.kato.common.argument.MultiRequestBody
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author wjy
 */
@RestController
class MultiRequestBodyController {

    @PostMapping("/multiRequest")
    fun multiRequest(@MultiRequestBody id: Int, @MultiRequestBody(required = false) name: String): TestEntity {
        return TestEntity(id, name)
    }

    @PostMapping("/multiRequestSingle")
    fun multiRequestSingle(@MultiRequestBody id: Int): TestEntity {
        return TestEntity(id, null)
    }

    @PostMapping("/multiRequestObj")
    fun multiRequestObj(@MultiRequestBody obj: TestEntity): TestEntity {
        return obj
    }

    @PostMapping("/multiRequestObj2")
    fun multiRequestObj2(
        @MultiRequestBody obj: TestEntity,
        @MultiRequestBody id: Int,
        @MultiRequestBody obj2: TestEntity2
    ): TestEntity {
        return obj
    }
}

data class TestEntity(val id: Int, val name: String?)
data class TestEntity2(val id: Int, val unkonw: String?)