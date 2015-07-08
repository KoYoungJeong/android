package com.tosslab.jandi.lib.sprinkler.domain.property;

import com.tosslab.jandi.lib.sprinkler.domain.property.key.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.domain.property.value.PropertyValue;

/**
 * Created by tonyjs on 15. 7. 8..
 */
public class Property {
    private PropertyKey key;
    private PropertyValue value;

    private Property(PropertyKey key, PropertyValue value) {
        this.key = key;
        this.value = value;
    }

    public static Property create(PropertyKey key, PropertyValue value) {
        return new Property(key, value);
    }

    public String getKey() {
        return key.getName();
    }

    public String getValue() {
        return value.getValue();
    }
}
