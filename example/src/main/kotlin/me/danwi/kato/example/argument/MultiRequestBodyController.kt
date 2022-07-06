package me.danwi.kato.example.argument

import me.danwi.kato.common.argument.MultiRequestBody
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author wjy
 */
@RestController
class MultiRequestBodyController {

    @PostMapping("/multiRequest")
    fun multiRequest(@MultiRequestBody id: Int, name: String): TestEntity {
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
    ): TestEntity3 {
        return TestEntity3(id, obj)
    }

    @PostMapping("/multiRequestObj3")
    fun multiRequestObj3(
        @MultiRequestBody obj: TestEntity2,
        @MultiRequestBody id: Int,
        @MultiRequestBody obj2: TestEntity3
    ): TestEntity3 {
        return obj2
    }
}
