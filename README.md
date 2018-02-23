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


OPERATIONS
========

query operation platern:
--------

```fieldName + OperationName```

for example:

```idEqualTo```, ```nameEqualTo```, ```ageGreaterThan```

available query operations:
--------

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

update operation platern:
--------

```OperationName + fieldName```

for example:

```setId```, ```setName```, ```setAge```

available update operations:
--------

```set```


EXAMPLE
========

[MainActivity.java](https://github.com/pandyin/rxrealm/blob/master/example/src/main/java/com/intathep/android/rxrealm/MainActivity.java)
