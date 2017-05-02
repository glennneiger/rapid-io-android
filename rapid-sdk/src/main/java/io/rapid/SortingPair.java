package io.rapid;


class SortingPair
{
	private String value;
	private Sorting sorting;


	public SortingPair(String value, Sorting sorting)
	{
		this.value = value;
		this.sorting = sorting;
	}


	public String getValue()
	{
		return value;
	}


	public Sorting getSorting()
	{
		return sorting;
	}
}
