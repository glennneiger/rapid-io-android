package io.rapid;


import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Options for document mutation
 * <p>
 * Provides a way to:
 * 1. expect a specific Etag value to be present on the backend (if the value does not match, operation will fail)
 * 2. get some properties filled by special server value such as server timestamp
 * <p>
 * Options object should be created using {@link Builder} class
 * Example:
 * <p>
 * <pre>
 * {@code
 * RapidMutateOptions options = new RapidMutateOptions.Builder()
 * 		.expectEtag(Etag.fromValue("my-etag"))
 * 		.fillPropertyWithServerTimestamp("timestamp")
 * 		.fillPropertyWithServerTimestamp("sub.timestamp")
 * 		.build();
 *
 * doc.mutate(body, options);
 * </pre>
 */
public class RapidMutateOptions {
	private Etag mExpectedEtag;
	private Collection<String> mFillWithTimestampProperties;


	private RapidMutateOptions(Etag expectedEtag, Collection<String> fillWithTimestampProperties) {
		mExpectedEtag = expectedEtag;
		mFillWithTimestampProperties = fillWithTimestampProperties;
	}


	Etag getExpectedEtag() {
		return mExpectedEtag;
	}


	void setExpectedEtag(Etag expectedEtag) {
		mExpectedEtag = expectedEtag;
	}


	Collection<String> getFillWithTimestampProperties() {
		return mFillWithTimestampProperties;
	}


	public static class Builder {
		private Etag mExpectedEtag;
		@NonNull private List<String> mFillWithTimestampProperties = new ArrayList<>();


		/**
		 * Expect a specific Etag value to be present on the backend (if the value does not match, operation will fail)
		 *
		 * @param expectedEtag Expected Etag value (can be one of Etag.fromValue("my-etag"), Etag.NO_ETAG, Etag.ANY_ETAG)
		 * @return builder itself
		 */
		@NonNull
		public Builder expectEtag(Etag expectedEtag) {
			mExpectedEtag = expectedEtag;
			return this;
		}


		/**
		 * Get some property filled by special server value such as server timestamp upon received at backend
		 *
		 * @param property property to be filled
		 * @return builder itself
		 */
		@NonNull
		public Builder fillPropertyWithServerTimestamp(String property) {
			mFillWithTimestampProperties.add(property);
			return this;
		}


		/**
		 * Build {@link RapidMutateOptions} object
		 *
		 * @return options object
		 */
		@NonNull
		public RapidMutateOptions build() {
			return new RapidMutateOptions(mExpectedEtag, mFillWithTimestampProperties);
		}
	}
}
