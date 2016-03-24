package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor;

import com.tosslab.jandi.app.network.json.JacksonMapper;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class ResponseConverter implements RestAdapterDecor {

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

    private final ObjectMapper objectMapper;

    public ResponseConverter() {

        objectMapper = JacksonMapper.getInstance().getObjectMapper();
    }

    @Override
    public Retrofit.Builder addRestAdapterProperty(Retrofit.Builder builder) {
        return builder.addConverterFactory(new Converter.Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                return value -> {
                    JavaType javaType = objectMapper.getTypeFactory().constructType(type);
                    return objectMapper.readValue(value.byteStream(), javaType);
                };
            }

            @Override
            public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
                return value -> RequestBody.create(MEDIA_TYPE, objectMapper.writeValueAsBytes(value));
            }

        });
    }
}
