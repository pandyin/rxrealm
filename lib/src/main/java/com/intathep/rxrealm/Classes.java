package com.intathep.rxrealm;

import com.squareup.javapoet.ClassName;

public class Classes {

    protected static final ClassName NULLABLE = ClassName.get("android.support.annotation", "Nullable");
    protected static final ClassName DEPRECATED = ClassName.get("java.lang", "Deprecated");

    protected static final ClassName THROWABLE = ClassName.get("java.lang", "Throwable");
    protected static final ClassName EXCEPTION = ClassName.get("java.lang", "Exception");

    protected static final ClassName MAPS = ClassName.get("com.google.common.collect", "Maps");
    protected static final ClassName LISTS = ClassName.get("com.google.common.collect", "Lists");

    protected static final ClassName LINKED_HASH_MAP = ClassName.get("java.util", "LinkedHashMap");
    protected static final ClassName LIST = ClassName.get("java.util", "List");

    protected static final ClassName JSON_OBJECT = ClassName.get("org.json", "JSONObject");

    protected static final ClassName REALMS = ClassName.get("com.intathep.android.rxrealm", "Realms");
    protected static final ClassName THREADS = ClassName.get("com.intathep.android.rxrealm", "Threads");

    protected static final ClassName FUNC0 = ClassName.get("rx.functions", "Func0");

    protected static final ClassName OBSERVABLE = ClassName.get("rx", "Observable");
    protected static final ClassName ASYNC_SUBJECT = ClassName.get("rx.subjects", "AsyncSubject");

    protected static final ClassName REALM_RESULTS = ClassName.get("io.realm", "RealmResults");
    protected static final ClassName REALM = ClassName.get("io.realm", "Realm");
    protected static final ClassName REALM_QUERY = ClassName.get("io.realm", "RealmQuery");
    protected static final ClassName SORT = ClassName.get("io.realm", "Sort");
    protected static final ClassName PRIMARY_KEY = ClassName.get("io.realm.annotations", "PrimaryKey");
}
