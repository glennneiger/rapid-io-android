package io.rapid;

import java.util.HashMap;


/**
 * Created by Leos on 22.03.2017.
 */

class EntityOrder
{
	private HashMap<String, Sorting> mOrderList = new HashMap<>();


	public EntityOrder()
	{
	}


	public void putOrder(String property, Sorting sorting)
	{
		mOrderList.put(property, sorting);
	}
}
