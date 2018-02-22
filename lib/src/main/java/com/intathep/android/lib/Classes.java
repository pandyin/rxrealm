package com.intathep.android.lib;

import com.squareup.javapoet.ClassName;

public class Classes {

    public static final ClassName TIMBER = ClassName.get("timber.log", "Timber");

    public static final ClassName NULLABLE = ClassName.get("android.support.annotation", "Nullable");

    public static final ClassName ANDROIDSCHEDULERS = ClassName.get("rx.android.schedulers", "AndroidSchedulers");
    public static final ClassName SCHEDULERS = ClassName.get("rx.schedulers", "Schedulers");

    public static final ClassName FUNC1 = ClassName.get("rx.functions", "Func1");

    public static final ClassName THROWABLE = ClassName.get("java.lang", "Throwable");
    public static final ClassName EXCEPTION = ClassName.get("java.lang", "Exception");

    public static final ClassName MAPS = ClassName.get("com.google.common.collect", "Maps");
    public static final ClassName LISTS = ClassName.get("com.google.common.collect", "Lists");

    public static final ClassName LINKED_HASH_MAP = ClassName.get("java.util", "LinkedHashMap");
    public static final ClassName HASH_MAP = ClassName.get("java.util", "HashMap");
    public static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");
    public static final ClassName LIST = ClassName.get("java.util", "List");
    public static final ClassName CALLABLE = ClassName.get("java.util.concurrent", "Callable");

    public static final ClassName JSON_OBJECT = ClassName.get("org.json", "JSONObject");

    public static final ClassName REALM_LOGGER = ClassName.get("com.ekoapp.App", "RealmLogger");
    public static final ClassName REALM_UTIL = ClassName.get("com.ekoapp.eko.Utils", "RealmUtil");
    public static final ClassName UTILITIES = ClassName.get("com.ekoapp.eko.Utils", "Utilities");
    public static final ClassName MODEL_FILTERS = ClassName.get("com.ekoapp.common.model", "ModelFilters");
    public static final ClassName BASE_OBSERVER = ClassName.get("com.ekoapp.common.rx", "BaseObserver");

    public static final ClassName OBSERVABLE = ClassName.get("rx", "Observable");
    public static final ClassName PUBLISH_SUBJECT = ClassName.get("rx.subjects", "PublishSubject");
    public static final ClassName ASYNC_SUBJECT = ClassName.get("rx.subjects", "AsyncSubject");

    public static final ClassName REALM_RESULTS = ClassName.get("io.realm", "RealmResults");
    public static final ClassName REALM = ClassName.get("io.realm", "Realm");
    public static final ClassName REALM_OBJECT = ClassName.get("io.realm", "RealmObject");
    public static final ClassName REALM_QUERY = ClassName.get("io.realm", "RealmQuery");
    public static final ClassName SORT = ClassName.get("io.realm", "Sort");
    public static final ClassName PRIMARY_KEY = ClassName.get("io.realm.annotations", "PrimaryKey");
}
