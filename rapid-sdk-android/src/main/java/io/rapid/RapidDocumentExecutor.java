package io.rapid;


public class RapidDocumentExecutor<T> {

	public interface Callback<T> {
		Result execute(RapidDocument<T> oldDocument);
	}


	/**
	 * Return this if you decided to delete the document based on the most recent data
	 */
	public static Result delete() {
		return new Result<>(Result.TYPE_MUTATE, null);
	}


	/**
	 * Return this if you decided to mutate the document based on the most recent data
	 */
	public static <T> Result mutate(T value) {
		return new Result<>(Result.TYPE_MUTATE, value);
	}


	/**
	 * Return this if you decided not to do anything with the document
	 */
	public static Result cancel() {
		return new Result<>(Result.TYPE_CANCEL, null);
	}


	public static class Result<T> {
		static final int TYPE_CANCEL = 0;
		static final int TYPE_MUTATE = 1;

		private int mType;
		private T mValue;


		Result(int type, T value) {
			mType = type;
			mValue = value;
		}


		int getType() {
			return mType;
		}


		T getValue() {
			return mValue;
		}
	}


}
