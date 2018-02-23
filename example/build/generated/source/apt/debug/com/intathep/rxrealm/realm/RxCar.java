package com.intathep.rxrealm.realm;

public class RxCar {
    public static CarGetter get() {
        return CarGetter.with();
    }

    public static CarSingleSetter set(String id) {
        return CarGetter.with().idEqualTo(id).edit();
    }

    public static CarCreator create() {
        return CarCreator.with();
    }
}
