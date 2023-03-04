package me.danwi.kato.example.exception

import me.danwi.kato.common.exception.KatoAccessDeniedException
import me.danwi.kato.common.exception.KatoAuthenticationException
import me.danwi.kato.common.exception.KatoBadRequestException
import org.springframework.security.access.AuthorizationServiceException
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author wjy
 */
@RestController
class KatoExceptionController {

    @RequestMapping("/katoAuth")
    fun katoAuth() {
        throw KatoAuthenticationException("katoAuth")
    }

    @RequestMapping("/katoAccessDenied")
    fun katoAccessDenied() {
        throw KatoAccessDeniedException("katoAccessDenied")
    }


    @RequestMapping("/springAuth")
    fun springAuth() {
        throw AccountExpiredException("springAuth")
    }


    @RequestMapping("/springAccessDenied")
    fun springAccessDenied() {
        throw AuthorizationServiceException("springAccessDenied")
    }

    @RequestMapping("/badRequest")
    fun badRequest() {
        throw KatoBadRequestException("badRequest")
    }

    @RequestMapping("/businessException")
    fun businessException() {
        throw KatoBadRequestException("businessException")
    }

    @RequestMapping("/undeclaredException")
    fun undeclaredException() {
        throw KatoBadRequestException("undeclaredException")
    }
}