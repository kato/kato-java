package me.danwi.kato.common.exception;

/**
 * 权限不足
 */
public class KatoAccessDeniedException extends KatoCommonException {
    public KatoAccessDeniedException() {
        super("权限不足");
    }
}
