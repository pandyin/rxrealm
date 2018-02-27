1 [DOWNLOAD](https://github.com/pandyin/rxrealm#download)

2 [USAGE](https://github.com/pandyin/rxrealm#usage)

3 [GENERATED CLASS](https://github.com/pandyin/rxrealm#generated-class)

4 [OPERATIONS](https://github.com/pandyin/rxrealm#operations)



DOWNLOAD
========

<b>project build.gradle:</b>
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

<b>module build.gradle:</b>
```
dependencies {
  compile 'com.github.pandyin:rxrealm:1.2.0'
}
```



USAGE
========

- [RealmObject](https://realm.io/docs/java/4.3.3/api/io/realm/RealmObject.html) must have ```@RxRealm``` annotation

- [RealmObject](https://realm.io/docs/java/4.3.3/api/io/realm/RealmObject.html) must define a primary key by using ```@PrimaryKey```

<b>example:</b>

[Car.java](https://github.com/pandyin/rxrealm/blob/master/example/src/main/java/com/intathep/android/rxrealm/model/Car.java)

```
@RxRealm
public class Car extends RealmObject {

    @PrimaryKey
    private String id;
    private String color;
    private int model;
```



AUTO GENERATED CLASS
========

...

<b>example:</b>

[RxCar.java](https://github.com/pandyin/rxrealm/blob/master/example/build/generated/source/apt/debug/com/intathep/rxrealm/realm/RxCar.java)



OPERATIONS
========

get operation:
--------

<b>format:</b>

```fieldName + condition```

<b>example:</b>

[MainActivity.java](https://github.com/pandyin/rxrealm/blob/master/example/src/main/java/com/intathep/android/rxrealm/MainActivity.java)

```
RxCar.get().idEqualTo(id) //condition
              .colorEqualTo(color)  //condition
              .modelGreaterThan(model) //condition
              .getAysnc(); //execute
```

<b>condition operations:</b>

```equalTo``` Equal-to comparison.

```notEqualTo``` Not-equal-to comparison.

```in``` In comparison.

```notIn``` Not-in comparison.

```lessThan``` Less-than comparison.

```greaterThan``` Greater-than comparison.

```sortBy``` Sorted by specific field name.

```or``` Logical-or two conditions.

```first``` Finds the first object that fulfills the query conditions.

<b>execute operations:</b>

```getAysnc``` Finds objects that fulfill the query conditions.

```deleteAsync``` Deletes objects that fulfill the query conditions.

```countAsync``` Counts objects that fulfill the query conditions.

<b>other operation:</b>

```edit```  Updates objects that fulfill the query conditions with [Set operation](https://github.com/pandyin/rxrealm/#set-operation)

set operation:
--------

<b>format:</b>

```"set" + fieldName```

<b>example:</b>

[MainActivity.java](https://github.com/pandyin/rxrealm/blob/master/example/src/main/java/com/intathep/android/rxrealm/MainActivity.java)

```
RxCar.set(id)
        .setColor(pink)
        .setModel(2019)
        .setAsync(); //execute
```

<b>execute operation:</b>

```setAsync``` Updates objects that fulfill the query conditions with provided data.
