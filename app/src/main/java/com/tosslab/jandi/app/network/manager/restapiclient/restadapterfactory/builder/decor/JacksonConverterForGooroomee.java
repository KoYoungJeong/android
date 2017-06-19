package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosslab.jandi.app.network.json.JsonMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by tee on 2017. 6. 13..
 */

public class JacksonConverterForGooroomee {

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    public static Converter.Factory create() {
        return new Converter.Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                ObjectMapper objectMapper = JsonMapper.getInstance().getObjectMapper();
                return value -> {
                    JavaType javaType = objectMapper.getTypeFactory().constructType(type);
                    return objectMapper.readValue(value.byteStream(), javaType);
                };
            }

            @Override
            public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
                ObjectMapper objectMapper = JsonMapper.getInstance().getObjectMapper();
                return value -> RequestBody.create(MEDIA_TYPE, objectMapper.writeValueAsBytes(value));
            }

        };
    }

}
