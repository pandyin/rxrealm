DOWNLOAD
========

project build.gradle:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

module build.gradle:
```
dependencies {
  compile 'com.github.pandyin:rxrealm:1.1.2'
}
```



USAGE
========

- [RealmObject](https://realm.io/docs/java/4.3.3/api/io/realm/RealmObject.html) must have ```@RxRealm``` annotation

- [RealmObject](https://realm.io/docs/java/4.3.3/api/io/realm/RealmObject.html) must define a primary key by using ```@PrimaryKey```

example:

```
@RxRealm
public class Car extends RealmObject {

    @PrimaryKey
    private String id;
    private String color;
```


OPERATIONS
========

query operation:
--------

```fieldName + OperationName```

example:

```idEqualTo```, ```nameEqualTo```, ```ageGreaterThan```

operations:

```equalTo```

```notEqualTo```

```in```

```notIn```

```lessThan```

```greaterThan```

```sortBy```

```or```

```edit```

```first```

update operation:
--------

```OperationName + fieldName```

example:

```setId```, ```setName```, ```setAge```

operation:

```set```


EXAMPLE
========

[MainActivity.java](https://github.com/pandyin/rxrealm/blob/master/example/src/main/java/com/intathep/android/rxrealm/MainActivity.java)
