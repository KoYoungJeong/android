package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Self extends RealmObject {
    @PrimaryKey
    private long id;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Self{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}