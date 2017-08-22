package io.rapid;


import java.util.List;


public class RapidCallback {
	public interface Collection<T> {
		void onValueChanged(List<RapidDocument<T>> documents);
	}


	public interface CollectionUpdates<T> {
		void onValueChanged(List<RapidDocument<T>> documents, ListUpdate listUpdate);
	}


	public interface Document<T> {
		void onValueChanged(RapidDocument<T> document);
	}


	public interface Message<T> {
		void onMessageReceived(RapidMessage<T> message);
	}


	public interface CollectionMapped<T> {
		void onValueChanged(List<T> items);
	}


	public interface Error {
		void onError(RapidError error);
	}


	public interface TimeOffset {
		void onTimeOffsetReceived(long timeOffsetMs);
	}
}
