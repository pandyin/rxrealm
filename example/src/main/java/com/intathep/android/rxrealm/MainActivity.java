package com.intathep.android.rxrealm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.intathep.android.rxrealm.helper.Realms;
import com.intathep.android.rxrealm.model.Car;
import com.intathep.rxrealm.realm.RxCar;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Realm realm = Realms.get();

        try {
            final String id = "id";
            final String red = "red";
            final String pink = "pink";
            final String blueAndGreen[] = new String[]{"blue", "green"};
            final int y2018 = 2018;
            final int y2019 = 2019;

            //subscribe for specific id.
            RxCar.get().idEqualTo(id)
                    .getAsync(realm)
                    .subscribe(new Action1<Car>() {
                        @Override
                        public void call(Car car) {
                            //managed realm object.
                        }
                    });

            //subscribe for red color and 2018 model.
            RxCar.get().colorEqualTo(red)
                    .modelEqualTo(y2018)
                    .getAsync(realm)
                    .subscribe(new Action1<RealmResults<Car>>() {
                        @Override
                        public void call(RealmResults<Car> cars) {
                            //managed realm results.
                        }
                    });

            //subscribe for the first red color found.
            RxCar.get().colorEqualTo(red)
                    .first()
                    .getAsync(realm)
                    .subscribe(new Action1<Car>() {
                        @Override
                        public void call(Car car) {
                            //managed realm object.
                        }
                    });

            //subscribe for 2018- model and sort by model in descending order.
            RxCar.get().modelLessThan(y2018)
                    .sortByModel(Sort.DESCENDING)
                    .getAsync(realm)
                    .subscribe(new Action1<RealmResults<Car>>() {
                        @Override
                        public void call(RealmResults<Car> cars) {
                            //managed realm results.
                        }
                    });

            //subscribe for cars apart from blue and green colors and sort by model in ascending order.
            RxCar.get().colorNotIn(blueAndGreen)
                    .sortByModel()
                    .getAsync(realm)
                    .subscribe(new Action1<RealmResults<Car>>() {
                        @Override
                        public void call(RealmResults<Car> cars) {
                            //managed realm results.
                        }
                    });

            //update all cars with red color to pink color and 2019 model.
            RxCar.get().colorEqualTo(red)
                    .edit()
                    .setColor(pink)
                    .setModel(y2019)
                    .setAsync()
                    .subscribe(new Action1<List<Car>>() {
                        @Override
                        public void call(List<Car> cars) {
                            //un-managed realm results.
                        }
                    });

            //update a car with specific id to pink color and 2019 model.
            RxCar.get().idEqualTo(id)
                    .edit()
                    .setColor(pink)
                    .setModel(y2019)
                    .setAsync()
                    .subscribe(new Action1<Car>() {
                        @Override
                        public void call(Car car) {
                            //un-managed realm object.
                        }
                    });

            //update a car with specific id to pink color and 2019 model.
            RxCar.set(id)
                    .setColor(pink)
                    .setModel(y2019)
                    .setAsync()
                    .subscribe(new Action1<Car>() {
                        @Override
                        public void call(Car car) {
                            //un-managed realm object.
                        }
                    });

            //delete all cars with 2018+ model .
            RxCar.get().modelGreaterThan(y2018)
                    .deleteAsync()
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean deleted) {
                            //true if objects successfully deleted; false otherwise
                        }
                    });

            //delete a cars with specific id.
            RxCar.get().idEqualTo(id)
                    .deleteAsync()
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean deleted) {
                            //true if an object successfully deleted; false otherwise
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Realms.close(realm);
        }
    }
}
