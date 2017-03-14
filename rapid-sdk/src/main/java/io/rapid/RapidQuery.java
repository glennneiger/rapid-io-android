package io.rapid;


import java.util.Collection;


// see https://realm.io/docs/java/3.0.0/api/ for API ideas
public class RapidQuery<T> {
	public RapidQuery(String collectionName) {}


	public RapidQuery<T> equalTo(String property, String value) {
		return this;
	}


	public RapidQuery<T> not(String property, String value) {
		return this;
	}


	public RapidQuery<T> lessThan(String property, String value) {
		return this;
	}


	public RapidQuery<T> greaterThan(String property, String value) {
		return this;
	}


	public RapidQuery<T> beginGroup() {
		return this;
	}


	public RapidQuery<T> endGroup() {
		return this;
	}


	public RapidQuery<T> or() {
		return this;
	}


	public RapidQuery<T> between(String property, int from, int to) {
		return this;
	}


	public RapidQuery<T> limit(int limit) {
		return this;
	}


	public RapidQuery<T> skip(int skip) {
		return this;
	}


	public RapidQuery<T> first() {
		return limit(1);
	}


	public RapidQuery<T> orderBy(String property, Sorting sorting) {
		return this;
	}


	public RapidQuery<T> orderBy(String property) {
		return orderBy(property, Sorting.ASC);
	}


	public RapidSubscription<Collection<T>> subscribe(RapidObjectCallback<Collection<T>> callback) {
		return new RapidSubscription<>();
	}
}
