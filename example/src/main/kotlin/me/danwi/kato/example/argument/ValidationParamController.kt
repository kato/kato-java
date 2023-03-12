package me.danwi.kato.example.argument

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.Date

@Validated
@RestController
@RequestMapping("/validation")
class ValidationParamController {

    @PostMapping("/validationParam")
    @Valid
    fun validationParam(@RequestBody @Valid param: ValidationParamEntity): ValidationParamEntity {
        return param
    }

    @PostMapping("/validationResult")
    @Valid
    fun validationResult(@RequestBody param: ValidationParamEntity): ValidationParamEntity {
        return param.copy(age = -1)
    }

    @PostMapping("/validationParamWithoutAnno")
    @Valid
    fun validationParamWithoutAnno(@Valid param: ValidationParamEntity): ValidationParamEntity {
        return param
    }

    @PostMapping("/validSingleParam")
    @Valid
    fun validSingleParam(
       @RequestBody @Valid @Size(min = 2, max = 3) name: String,
       @RequestParam @Valid @Positive age: Int
    ): String {
        return name
    }

}

data class ValidationParamEntity(
    @field:Size(min = 1, max = 3)
    val len: String,
    @field:Positive
    val age: Int,
    @field:Size(min = 1)
    val agent: Set<String>,
    @field:FutureOrPresent
    val birthDay: Date,
    @field:Email
    val mail: String
)