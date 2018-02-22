package com.intathep.android.rxrealm.model;

import com.intathep.rxrealm.RxRealm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@RxRealm
public class Car extends RealmObject {

    @PrimaryKey
    private String id;
    private String model;
    private String color;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
