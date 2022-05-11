package me.danwi.kato.server;

import me.danwi.kato.common.exception.KatoAccessDeniedException;
import me.danwi.kato.common.exception.KatoAuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class KatoResponseBodyAdviceWithSecurity extends KatoResponseBodyAdvice {
    @ExceptionHandler(AccessDeniedException.class)
    KatoAccessDeniedException katoAccessDeniedExceptionHandler(AccessDeniedException exception) {
        return new KatoAccessDeniedException(exception);
    }

    @ExceptionHandler(AuthenticationException.class)
    KatoAuthenticationException katoAuthenticationExceptionHandler(AuthenticationException exception) {
        return new KatoAuthenticationException(exception);
    }
}
