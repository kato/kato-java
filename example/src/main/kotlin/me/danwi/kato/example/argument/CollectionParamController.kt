package me.danwi.kato.example.argument

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/collection")
class CollectionParamController {

    @PostMapping("/listParam")
    fun multiRequest(@RequestBody list: List<TestEntityAll>): String {
        var result = ""
        list.forEach { result += it.id }
        return result
    }
}