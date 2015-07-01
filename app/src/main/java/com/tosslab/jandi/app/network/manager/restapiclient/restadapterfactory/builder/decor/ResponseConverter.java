package com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.builder.decor;

import com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.converter.JacksonConverter;
import com.tosslab.jandi.app.network.spring.JacksonMapper;

import retrofit.RestAdapter;
import retrofit.converter.Converter;

public class ResponseConverter implements RestAdapterDecor {

    private Converter converter;

    public <T> ResponseConverter(Class<T> clazz) {
        converter = new JacksonConverter(JacksonMapper.getInstance().getObjectMapper());
    }

    @Override
    public RestAdapter.Builder addRestAdapterProperty(RestAdapter.Builder builder) {
        return builder.setConverter(converter);
    }
}
