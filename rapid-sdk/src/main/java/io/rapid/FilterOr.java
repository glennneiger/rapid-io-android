package io.rapid;


class FilterOr extends FilterGroup {

	public FilterOr(Filter... filters) {
		super(filters);
	}


	@Override
	protected String getKeyword() {
		return "or";
	}
}
