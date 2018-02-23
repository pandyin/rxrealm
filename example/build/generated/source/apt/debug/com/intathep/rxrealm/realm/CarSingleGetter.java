package com.intathep.rxrealm.realm;

import com.intathep.android.rxrealm.command.Command;
import com.intathep.android.rxrealm.helper.Realms;
import com.intathep.android.rxrealm.helper.Threads;
import com.intathep.android.rxrealm.model.Car;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Observable;
import rx.subjects.AsyncSubject;

public class CarSingleGetter {
    private final List<Command> commands;

    private CarSingleGetter(List<Command> commands) {
        this.commands = commands;
    }

    protected static CarSingleGetter with(List<Command> commands) {
        return new CarSingleGetter(commands);
    }

    public CarSingleSetter edit() {
        return CarSingleSetter.with(commands);
    }

    private RealmQuery<Car> applyCommandsToRealmQuery(Realm realm) {
        RealmQuery<Car> realmQuery = realm.where(Car.class);
        for (Command command : commands) {
            command.execute(realmQuery);
        }
        return realmQuery;
    }

    public Observable<Boolean> deleteAsync() {
        final AsyncSubject<Boolean> asyncSubject = AsyncSubject.create();
        if (Threads.isMainThread()) {
            Realm temp = null;
            try {
                temp = Realms.get();
                temp.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Car t = applyCommandsToRealmQuery(realm).findFirst();
                        if (t != null) {
                            asyncSubject.onNext(true);
                            t.deleteFromRealm();
                        } else {
                            asyncSubject.onNext(false);
                        }
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
                    Car t = applyCommandsToRealmQuery(realm).findFirst();
                    if (t != null) {
                        asyncSubject.onNext(true);
                        t.deleteFromRealm();
                    } else {
                        asyncSubject.onNext(false);
                    }
                    asyncSubject.onCompleted();
                }
            });
        }
        return asyncSubject;
    }

    public Observable<Car> getAsync(Realm realm) {
        return getAsync(applyCommandsToRealmQuery(realm)).filter(Realms.<Car>filterNotNullRealmObject());
    }

    private Observable<Car> getAsync(RealmQuery<Car> realmQuery) {
        if (Threads.isMainThread()) {
            return realmQuery.findFirstAsync().<Car>asObservable().filter(Realms.filterValidRealmObject());
        } else {
            return Observable.just(realmQuery.findFirst());
        }
    }
}
