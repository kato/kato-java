package me.danwi.kato.example.argument

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import me.danwi.kato.server.PassByKato
import org.springframework.web.bind.annotation.*

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

    @PostMapping("/multiRequest/{id}/{name}")
    fun multiRequest(@PathVariable @Valid @Positive id: Int, @PathVariable @Valid @Size(min = 2, max = 3) name: String): TestEntity {
        return TestEntity(id, name)
    }

    @PostMapping("/multiRequest2")
    fun multiRequest2(@RequestBody id: Int): TestEntity {
        return TestEntity(id, id.toString())
    }

    @PostMapping("/multiRequestSingle")
    fun multiRequestSingle(@RequestBody id: Int): TestEntity {
        return TestEntity(id, null)
    }

    @PostMapping("/multiRequestObj")
    fun multiRequestObj(@RequestBody obj: TestEntity): TestEntity {
        return obj
    }

    @RequestMapping("/multiRequestObj2")
    fun multiRequestObj2(obj: TestEntity): TestEntity3 {
        return TestEntity3(obj.id, obj)
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
