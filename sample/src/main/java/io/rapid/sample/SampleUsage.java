package io.rapid.sample;


import android.util.Log;

import java.util.Map;

import io.rapid.ConnectionState;
import io.rapid.Rapid;
import io.rapid.RapidCollectionReference;
import io.rapid.RapidDocument;
import io.rapid.RapidDocumentReference;
import io.rapid.RapidError;
import io.rapid.Sorting;


public class SampleUsage {

	private static final String RAPID_API_KEY = "sdafh87923jweql2393rfksad";


	public static void sampleMethod() {


		// Initialization
		Rapid.initialize(RAPID_API_KEY);


		// Get Rapid Instance
		Rapid rapid = Rapid.getInstance();


		// Multiple Instances
		Rapid.initialize("abc");
		Rapid.initialize("xyz");

		Rapid rapidAbc = Rapid.getInstance("<abc_api_token>");
		Rapid rapidXyz = Rapid.getInstance("<xyz_api_token>");


		// Get Connection State
		if(Rapid.getInstance().getConnectionState() == ConnectionState.CONNECTING) {
			log("Client is connecting.");
		}
		if(Rapid.getInstance().getConnectionState() == ConnectionState.CONNECTED) {
			log("Client is connected.");
		}
		if(Rapid.getInstance().getConnectionState() == ConnectionState.DISCONNECTED) {
			log("Client is disconnected.");
		}


		// Add Connection State Listener
		Rapid.getInstance().addConnectionStateListener(state -> log(state.name()));


		// Referencing Collections and Documents
		RapidCollectionReference<Message> messages = Rapid.getInstance().collection("messages", Message.class);
		RapidDocumentReference<Message> messageAbc = messages.document("abc");
		RapidDocumentReference<Message> newMessage = messages.newDocument();


		// Subscriptions
		// collection subscription
		messages.subscribe((documents) -> {
			RapidDocument<Message> firstDoc = documents.get(0);
			String id = firstDoc.getId();
			Message body = firstDoc.getBody();
		}).onError(error -> {
			boolean isPermissionDenied = error.getType() == RapidError.ErrorType.PERMISSION_DENIED;
			error.printStackTrace();
		});

		// single document subscription
		messages.document("1").subscribe(value -> {
			Message message1 = value.getBody();
			log(message1.toString());
		});


		// Filtering, Ordering, Paging
		messages
				.equalTo("receiver", "carl01")
				.beginOr()
					.beginAnd()
						.equalTo("sender", "john123")
						.equalTo("sender", "john123")
					.endAnd()
					.equalTo("sender", "john123")
					.greaterOrEqualThan("urgency", 1)
				.endOr()
				.orderBy("sentDate", Sorting.DESC)
				.orderBy("urgency", Sorting.ASC)
				.limit(50)
				.skip(10)
				.subscribe(documents -> {
					log(documents.toString());
				})
				.onError(error -> {
					error.printStackTrace();
				});


		// Mutation

		messages.newDocument()
				.mutate(new Message("john123", "carl01", "Hello!"))
				.onSuccess(() -> {
					log("Message successfuly written.");
				})
				.onError(error -> {
					boolean isTimeout = error.getType() == RapidError.ErrorType.TIMEOUT;
					boolean isPermissionDenied = error.getType() == RapidError.ErrorType.PERMISSION_DENIED;
					error.printStackTrace();
				});


		// document subscription
		messages.document("asdfasdfasdf").subscribe(value -> {
			log(value.getBody().toString());
		});


		// error handling
		messages.subscribe((carCollection) -> log(carCollection.toString()))
				.onError(error -> log("Subscribe error"));


		// filtering
		messages.equalTo("type", "SUV")
				.between("price", 0, 45000)
				.subscribe((carCollection) -> {
					log(carCollection.toString());
				});


		// advanced filtering
		messages.between("price", 0, 45000)
				.beginOr()
					.beginAnd()
						.equalTo("type", "SUV")
						.equalTo("tyadsfasdfpe", "SUasdfasdfV")
					.endAnd()
					.equalTo("type", "sedan")
				.endOr()
				.orderBy("price", Sorting.ASC)
				.skip(20)
				.limit(20)
				.subscribe((carCollection) -> {
					log(carCollection.toString());
				});


		// basic adding
		RapidDocumentReference<Message> m = messages.newDocument();

		log(m.getId());

		m.mutate(new Message());


		// advanced adding
		messages.newDocument().mutate(new Message())
				.onSuccess(() -> {
					log("Mutation successful");
				})
				.onError(error -> {
					log("Mutation error");
					error.printStackTrace();
				});


		// editing
		messages.document("asfasdfwewqer").mutate(new Message());


		// mutate custom JSON converter
		Rapid.getInstance().setJsonConverter(new RapidJacksonConverter());


		// Reading collection as a Map<String, Object>
		Rapid.getInstance().collection("maps").subscribe(rapidDocuments -> {
			Map<String, Object> map = rapidDocuments.get(0).getBody();
		});

	}


	private static void log(String message) {
		Log.d("Rapid Sample", message);
	}
}
