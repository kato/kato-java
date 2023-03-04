package me.danwi.kato.common.exception;

import org.springframework.http.HttpStatus;

public interface HttpStatusHolder {

    HttpStatus getHttpStatus();
}
