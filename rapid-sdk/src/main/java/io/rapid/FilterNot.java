package io.rapid;


class FilterNot extends FilterGroup {

	public FilterNot(Filter... filters) {
		super(filters);
	}


	@Override
	protected String getKeyword() {
		return "not";
	}
}
