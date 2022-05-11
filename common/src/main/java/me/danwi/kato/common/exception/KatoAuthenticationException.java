package me.danwi.kato.common.exception;

/**
 * 认证异常
 */
public class KatoAuthenticationException extends KatoCommonException {
    public KatoAuthenticationException() {
        super("认证错误");
    }
}
