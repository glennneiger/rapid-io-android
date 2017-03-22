package io.rapid;


public enum Sorting
{
	ASC("asc"), DESC("desc");

	private String mKey;


	Sorting(String key)
	{
		mKey = key;
	}


	public String getKey()
	{
		return mKey;
	}
}
