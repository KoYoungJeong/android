package com.tosslab.jandi.app.network.jackson.deserialize.start;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.InitialInfo;

import java.util.List;

public class InitializeInfoConverter implements Converter<InitialInfo, InitialInfo> {
    @Override
    public InitialInfo convert(InitialInfo value) {

        long myId = value.getSelf().getId();

        List<Human> humans = value.getMembers();
        if (humans != null && !humans.isEmpty()) {
            for (Human human : humans) {
                if (human.getId() == myId) {
                    human.setIsStarred(false);
                }
            }

        }

        return value;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(InitialInfo.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(InitialInfo.class);
    }
}
