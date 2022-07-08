package me.danwi.kato.client;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

import java.lang.reflect.Type;

public class KatoEncoder implements Encoder {

    private final Encoder delegate;

    public KatoEncoder(Encoder delegate) {
        this.delegate = delegate;
    }


    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {

    }
}
