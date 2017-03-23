package io.rapid;


class FilterAnd extends FilterGroup {

	public FilterAnd(Filter... filters) {
		super(filters);
	}


	@Override
	protected String getKeyword() {
		return "and";
	}

}
