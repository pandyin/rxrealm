package com.intathep.android.rxrealm.model;

import com.intathep.rxrealm.RxRealm;

import io.realm.RealmObject;

@RxRealm
public class Car extends RealmObject {

    private String model;
    private String color;

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
