package io.rapid;


public class RapidDocumentExecutor<T> {

	public interface Callback<T> {
		Result execute(RapidDocument<T> oldDocument);
	}


	/**
	 * Return this if you decided to delete the document based on the most recent data
	 */
	public static Result delete() {
		return new Result<>(Result.TYPE_MUTATE, null, null);
	}


	/**
	 * Return this if you decided to mutate the document based on the most recent data
	 */
	public static <T> Result mutate(T value) {
		return mutate(value, null);
	}


	/**
	 * Return this if you decided to mutate the document based on the most recent data
	 */
	public static <T> Result mutate(T value, RapidMutateOptions options) {
		return new Result<>(Result.TYPE_MUTATE, value, options);
	}


	/**
	 * Return this if you decided not to do anything with the document
	 */
	public static Result cancel() {
		return new Result<>(Result.TYPE_CANCEL, null, null);
	}


	public static class Result<T> {
		static final int TYPE_CANCEL = 0;
		static final int TYPE_MUTATE = 1;

		private int mType;
		private T mValue;
		private RapidMutateOptions mOptions;


		Result(int type, T value, RapidMutateOptions options) {
			mType = type;
			mValue = value;
			mOptions = options;
		}


		public RapidMutateOptions getOptions() {
			return mOptions;
		}


		int getType() {
			return mType;
		}


		T getValue() {
			return mValue;
		}
	}


}
