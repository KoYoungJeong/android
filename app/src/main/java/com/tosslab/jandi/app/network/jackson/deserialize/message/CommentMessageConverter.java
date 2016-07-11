package com.tosslab.jandi.app.network.jackson.deserialize.message;

import android.text.TextUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.dynamicl10n.DynamicMessageUtil;

public class CommentMessageConverter implements Converter<ResMessages.CommentMessage, ResMessages.CommentMessage> {
    @Override
    public ResMessages.CommentMessage convert(ResMessages.CommentMessage value) {

        if (value.isFormatted
                && !TextUtils.isEmpty(value.formatKey)
                && value.formatParams != null) {
            // 내부적으로 FormatParams.setTypeOf 가 지정됨 (sql <-> java 를 위함)
            value.formatMessage = DynamicMessageUtil.getFormatParam(value.formatKey, value.formatParams);
        }

        return value;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(ResMessages.CommentMessage.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(ResMessages.CommentMessage.class);
    }
}
