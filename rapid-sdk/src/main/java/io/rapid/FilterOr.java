package io.rapid;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


class FilterOr implements Filter{
	Set<Filter> filters = new HashSet<>();

	public FilterOr(Filter... filters) {
		this.filters.addAll(Arrays.asList(filters));
	}

	public void add(Filter filterValue) {
		filters.add(filterValue);
	}
}
