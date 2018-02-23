package com.intathep.rxrealm.realm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intathep.android.rxrealm.command.Command;
import com.intathep.android.rxrealm.helper.Realms;
import com.intathep.android.rxrealm.helper.Threads;
import com.intathep.android.rxrealm.model.Car;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Observable;
import rx.subjects.AsyncSubject;

public class CarSetter {
    private final LinkedHashMap<String, Object> data;

    private List<Command> commands;

    private CarSetter(List<Command> commands) {
        this.data = Maps.newLinkedHashMap();
        this.commands = commands;
    }

    protected static CarSetter with(List<Command> commands) {
        return new CarSetter(commands);
    }

    public CarSetter setColor(String color) {
        this.data.put("color", color);
        return this;
    }

    public CarSetter setModel(int model) {
        this.data.put("model", model);
        return this;
    }

    public Observable<List<Car>> setAsync() {
        final AsyncSubject<List<Car>> asyncSubject = AsyncSubject.create();
        if (Threads.isMainThread()) {
            Realm temp = null;
            try {
                temp = Realms.get();
                temp.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        asyncSubject.onNext(applyChanges(realm));
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        asyncSubject.onCompleted();
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        asyncSubject.onError(error);
                    }
                });
            } catch (Exception e) {
                asyncSubject.onError(e);
            } finally {
                Realms.close(temp);
            }
        } else {
            Realms.executeTransaction(new Realms.BetterTransaction() {
                @Override
                public void execute(Realm realm) throws Exception {
                    asyncSubject.onNext(applyChanges(realm));
                    asyncSubject.onCompleted();
                }
            });
        }
        return asyncSubject;
    }

    private List<Car> applyChanges(Realm realm) {
        RealmQuery<Car> realmQuery = realm.where(Car.class);
        for (Command command : commands) {
            command.execute(realmQuery);
        }
        List<Car> realmList = Lists.newArrayList();
        for (Car db : realmQuery.findAll()) {
            data.put("id", db.getId());
            JSONObject json = new JSONObject(data);
            realmList.add(realm.copyFromRealm(realm.createOrUpdateObjectFromJson(Car.class, json)));
        }
        return realmList;
    }
}
