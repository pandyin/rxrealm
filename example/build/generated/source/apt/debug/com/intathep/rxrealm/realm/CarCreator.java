package com.intathep.rxrealm.realm;

import com.google.common.collect.Maps;
import com.intathep.android.rxrealm.helper.Realms;
import com.intathep.android.rxrealm.helper.Threads;
import com.intathep.android.rxrealm.model.Car;

import org.json.JSONObject;

import java.util.LinkedHashMap;

import io.realm.Realm;
import rx.Observable;
import rx.subjects.AsyncSubject;

public class CarCreator {
    private final LinkedHashMap<String, Object> data;

    private CarCreator() {
        this.data = Maps.newLinkedHashMap();
    }

    protected static CarCreator with() {
        return new CarCreator();
    }

    public CarCreator setId(String id) {
        this.data.put("id", id);
        return this;
    }

    public CarCreator setColor(String color) {
        this.data.put("color", color);
        return this;
    }

    public CarCreator setModel(int model) {
        this.data.put("model", model);
        return this;
    }

    public Observable<Car> createAsync() {
        final AsyncSubject<Car> asyncSubject = AsyncSubject.create();
        if (Threads.isMainThread()) {
            Realm temp = null;
            try {
                temp = Realms.get();
                temp.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        asyncSubject.onNext(realm.copyFromRealm(realm.createObjectFromJson(Car.class, new JSONObject(data))));
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
                    asyncSubject.onNext(realm.createObjectFromJson(Car.class, new JSONObject(data)));
                    asyncSubject.onCompleted();
                }
            });
        }
        return asyncSubject;
    }

    public Observable<Car> createOrUpdateAsync() {
        final AsyncSubject<Car> asyncSubject = AsyncSubject.create();
        if (Threads.isMainThread()) {
            Realm temp = null;
            try {
                temp = Realms.get();
                temp.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        asyncSubject.onNext(realm.copyFromRealm(realm.createOrUpdateObjectFromJson(Car.class, new JSONObject(data))));
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
                    asyncSubject.onNext(realm.createOrUpdateObjectFromJson(Car.class, new JSONObject(data)));
                    asyncSubject.onCompleted();
                }
            });
        }
        return asyncSubject;
    }
}
