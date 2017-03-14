package rapid.io.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Collection;

import io.rapid.GsonConverter;
import io.rapid.Rapid;
import io.rapid.RapidCollection;
import io.rapid.RapidSubscription;
import io.rapid.Sorting;


public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set JSON converter
		Rapid.getInstance().setJsonConverter(new GsonConverter());


		// get Rapid collection
		RapidCollection<Car> cars = Rapid.getInstance().collection("cars", Car.class);


		// simple subscription
		RapidSubscription<Collection<Car>> carsSubscription = cars.subscribe(carCollection -> {
			System.out.println(carCollection);
		});

		// unsubscribe when not needed anymore
		carsSubscription.unsubscribe();


		// filtering
		cars.query().equalTo("type", "SUV").between("price", 0, 45000).subscribe(carCollection -> {
			System.out.println(carCollection);
		});


		// advanced filtering
		cars.query()
				.between("price", 0, 45000)
				.beginGroup()
				.equalTo("type", "SUV")
				.or()
				.equalTo("type", "sedan")
				.endGroup()
				.orderBy("price", Sorting.ASC)
				.skip(20)
				.limit(20)
				.subscribe(carCollection -> {
					System.out.println(carCollection);
				});

		// basic mutation
		cars.mutate(new Car());

	}
}
