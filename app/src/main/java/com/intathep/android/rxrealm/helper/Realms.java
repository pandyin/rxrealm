package com.intathep.android.rxrealm.helper;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.functions.Func1;

public class Realms {

    public static Realm get() {
        return Realm.getDefaultInstance();
    }

    public static void close(Realm realm) {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    public static <T extends RealmObject> Func1<T, Boolean> filterValidRealmObject() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T realmObject) {
                return realmObject.isValid();
            }
        };
    }

    public static <T extends RealmResults> Func1<T, Boolean> filterValidRealmResults() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T realmResults) {
                return realmResults.isValid();
            }
        };
    }

    public static <T extends RealmResults> Func1<T, Boolean> filterLoadedRealmResults() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T realmResults) {
                return realmResults.isLoaded();
            }
        };
    }

    public static <T extends RealmResults> Func1<T, Boolean> filterNotEmptyRealmResults() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T realmResults) {
                return !realmResults.isEmpty();
            }
        };
    }

    public static <T extends RealmObject> Func1<T, Boolean> filterNotNullRealmObject() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T realmObject) {
                return realmObject != null;
            }
        };
    }

    public static <T extends RealmObject> Func1<T, T> copyFromRealm(final Realm realm) {
        return new Func1<T, T>() {
            @Override
            public T call(T realmObject) {
                return realm.copyFromRealm(realmObject);
            }
        };
    }

    public static void executeTransaction(BetterTransaction transaction) {
        Realm realm = null;
        try {
            realm = get();
            realm.beginTransaction();
            transaction.execute(realm);
            realm.commitTransaction();
        } catch (Exception e) {
            if (realm != null && realm.isInTransaction()) {
                realm.cancelTransaction();
            }
        } finally {
            close(realm);
        }
    }

    public static void executeTransactionAsync(BetterTransaction transaction) {
        Realm realm = null;
        try {
            realm = get();
            realm.executeTransactionAsync(new TransactionAdapter(transaction));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(realm);
        }
    }

    public interface BetterTransaction {
        void execute(Realm realm) throws Exception;
    }

    private static class TransactionAdapter implements Realm.Transaction {

        private final BetterTransaction transaction;

        private TransactionAdapter(BetterTransaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public void execute(Realm realm) {
            try {
                transaction.execute(realm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
