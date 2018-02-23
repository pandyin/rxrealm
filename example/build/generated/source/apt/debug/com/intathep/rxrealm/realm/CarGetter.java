package com.intathep.rxrealm.realm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intathep.android.rxrealm.command.Command;
import com.intathep.android.rxrealm.command.EqualToCommand;
import com.intathep.android.rxrealm.command.GreaterThanCommand;
import com.intathep.android.rxrealm.command.InCommand;
import com.intathep.android.rxrealm.command.LessThanCommand;
import com.intathep.android.rxrealm.command.NotEqualToCommand;
import com.intathep.android.rxrealm.command.NotInCommand;
import com.intathep.android.rxrealm.command.OrCommand;
import com.intathep.android.rxrealm.executor.IntegerExecutor;
import com.intathep.android.rxrealm.executor.StringExecutor;
import com.intathep.android.rxrealm.helper.Realms;
import com.intathep.android.rxrealm.helper.Threads;
import com.intathep.android.rxrealm.model.Car;

import java.util.LinkedHashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.functions.Func0;
import rx.subjects.AsyncSubject;

public class CarGetter {
    private boolean acceptEmpty;

    private final List<Command> commands;

    private final LinkedHashMap<String, Sort> sortFields;

    private CarGetter() {
        this.commands = Lists.newArrayList();
        this.sortFields = Maps.newLinkedHashMap();
    }

    protected static CarGetter with() {
        return new CarGetter();
    }

    public CarGetter acceptEmpty() {
        this.acceptEmpty = true;
        return this;
    }

    public CarGetter or() {
        commands.add(new OrCommand());
        return this;
    }

    public CarSingleGetter first() {
        return CarSingleGetter.with(commands);
    }

    public CarSingleGetter idEqualTo(String id) {
        commands.add(new EqualToCommand("id", new StringExecutor(id)));
        return CarSingleGetter.with(commands);
    }

    public CarGetter idNotEqualTo(String id) {
        commands.add(new NotEqualToCommand("id", new StringExecutor(id)));
        return this;
    }

    public CarGetter idIn(String[] idArray) {
        commands.add(new InCommand("id", new StringExecutor(idArray)));
        return this;
    }

    public CarGetter idNotIn(String[] idArray) {
        commands.add(new NotInCommand("id", new StringExecutor(idArray)));
        return this;
    }

    public CarGetter sortById() {
        sortFields.put("id", Sort.ASCENDING);
        return this;
    }

    public CarGetter sortById(Sort sort) {
        sortFields.put("id", sort);
        return this;
    }

    public CarGetter colorEqualTo(String color) {
        commands.add(new EqualToCommand("color", new StringExecutor(color)));
        return this;
    }

    public CarGetter colorNotEqualTo(String color) {
        commands.add(new NotEqualToCommand("color", new StringExecutor(color)));
        return this;
    }

    public CarGetter colorIn(String[] colorArray) {
        commands.add(new InCommand("color", new StringExecutor(colorArray)));
        return this;
    }

    public CarGetter colorNotIn(String[] colorArray) {
        commands.add(new NotInCommand("color", new StringExecutor(colorArray)));
        return this;
    }

    public CarGetter sortByColor() {
        sortFields.put("color", Sort.ASCENDING);
        return this;
    }

    public CarGetter sortByColor(Sort sort) {
        sortFields.put("color", sort);
        return this;
    }

    public CarGetter modelEqualTo(int model) {
        commands.add(new EqualToCommand("model", new IntegerExecutor(model)));
        return this;
    }

    public CarGetter modelNotEqualTo(int model) {
        commands.add(new NotEqualToCommand("model", new IntegerExecutor(model)));
        return this;
    }

    public CarGetter modelIn(Integer[] modelArray) {
        commands.add(new InCommand("model", new IntegerExecutor(modelArray)));
        return this;
    }

    public CarGetter modelNotIn(Integer[] modelArray) {
        commands.add(new NotInCommand("model", new IntegerExecutor(modelArray)));
        return this;
    }

    public CarGetter modelLessThan(int value) {
        commands.add(new LessThanCommand("model", new IntegerExecutor(value)));
        return this;
    }

    public CarGetter modelGreaterThan(int value) {
        commands.add(new GreaterThanCommand("model", new IntegerExecutor(value)));
        return this;
    }

    public CarGetter sortByModel() {
        sortFields.put("model", Sort.ASCENDING);
        return this;
    }

    public CarGetter sortByModel(Sort sort) {
        sortFields.put("model", sort);
        return this;
    }

    public CarSetter edit() {
        return CarSetter.with(commands);
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
                        asyncSubject.onNext(applyCommandsToRealmQuery(realm).findAll().deleteAllFromRealm());
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
                    asyncSubject.onNext(applyCommandsToRealmQuery(realm).findAll().deleteAllFromRealm());
                    asyncSubject.onCompleted();
                }
            });
        }
        return asyncSubject;
    }

    public Observable<Long> countAsync() {
        return Observable.fromCallable(new Func0<Long>() {
            @Override
            public Long call() {
                return count();
            }
        });
    }

    private Long count() {
        Realm temp = null;
        try {
            temp = Realms.get();
            return count(applyCommandsToRealmQuery(temp));
        } catch (Exception e) {
            return 0l;
        } finally {
            Realms.close(temp);
        }
    }

    private Long count(RealmQuery<Car> realmQuery) {
        return realmQuery.count();
    }

    public Observable<RealmResults<Car>> getAsync(Realm realm) {
        if (acceptEmpty) {
            return getAsync(applyCommandsToRealmQuery(realm));
        } else {
            return getAsync(applyCommandsToRealmQuery(realm)).filter(Realms.<RealmResults<Car>>filterNotEmptyRealmResults());
        }
    }

    private Observable<RealmResults<Car>> getAsync(RealmQuery<Car> realmQuery) {
        if (sortFields.size() > 0) {
            if (Threads.isMainThread()) {
                return realmQuery.findAllSortedAsync(sortFields.keySet().toArray(new String[sortFields.size()]), sortFields.values().toArray(new Sort[sortFields.size()])).<Car>asObservable().filter(Realms.filterValidRealmResults());
            } else {
                return Observable.just(realmQuery.findAllSorted(sortFields.keySet().toArray(new String[sortFields.size()]), sortFields.values().toArray(new Sort[sortFields.size()])));
            }
        } else {
            if (Threads.isMainThread()) {
                return realmQuery.findAllAsync().<Car>asObservable().filter(Realms.filterValidRealmResults());
            } else {
                return Observable.just(realmQuery.findAll());
            }
        }
    }
}
