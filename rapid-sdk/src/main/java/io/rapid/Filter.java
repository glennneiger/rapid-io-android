package io.rapid;


interface Filter {
	public static void sample(){
		FilterOr or = new FilterOr();
		or.add(new FilterValue("sender", new FilterValue.StringPropertyValue("john123")));
		or.add(new FilterValue("sender", new FilterValue.GreaterThanPropertyValue(1)));
		or.add(new FilterAnd(new FilterValue("bla", new FilterValue.StringPropertyValue("hello"))));
	}
}
