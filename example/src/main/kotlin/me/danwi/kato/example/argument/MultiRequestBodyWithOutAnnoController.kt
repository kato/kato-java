package me.danwi.kato.example.argument

import me.danwi.kato.server.PassByKato
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author wjy
 */
@RestController
@RequestMapping("/withOutAnno")
class MultiRequestBodyWithOutAnnoController {

    @PassByKato
    @PostMapping("/multiRequestPassByMethod")
    fun multiRequestPassByMethod(id: Int?, name: String?): TestEntity {
        return TestEntity(id, name)
    }

    @PostMapping("/multiRequestPassByParam")
    fun multiRequestPassByParam(id: Int?, @PassByKato name: String?): TestEntity {
        return TestEntity(id, name)
    }

    @PostMapping("/multiRequest")
    fun multiRequest(id: Int, name: String): TestEntity {
        return TestEntity(id, name)
    }

    @PostMapping("/multiRequestSingle")
    fun multiRequestSingle(id: Int): TestEntity {
        return TestEntity(id, null)
    }

    @PostMapping("/multiRequestObj")
    fun multiRequestObj(obj: TestEntity): TestEntity {
        return obj
    }

    @PostMapping("/multiRequestObj2")
    fun multiRequestObj2(
        obj: TestEntity,
        id: Int,
        obj2: TestEntity2
    ): TestEntity3 {
        return TestEntity3(id, obj)
    }

    @PostMapping("/multiRequestObj3")
    fun multiRequestObj3(
        obj: TestEntity2,
        id: Int,
        obj2: TestEntity3
    ): TestEntity3 {
        return obj2
    }
}
