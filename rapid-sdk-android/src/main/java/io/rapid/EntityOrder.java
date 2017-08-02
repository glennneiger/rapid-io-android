package io.rapid;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


class EntityOrder {
	private List<EntityOrderDetail> mOrderList = new ArrayList<>();


	static EntityOrder fromJson(JSONArray array) {
		if(array == null) return null;

		EntityOrder order = new EntityOrder();
		for(int i = 0; i < array.length(); i++) {
			JSONObject item = array.optJSONObject(i);
			String property = item.keys().hasNext() ? item.keys().next() : null;
			Sorting sorting = Sorting.fromKey(item.optString(property));
			order.putOrder(property, sorting);
		}
		return order;
	}


	void putOrder(final String property, Sorting sorting) {
		mOrderList = Stream.of(mOrderList).filter(new Predicate<EntityOrderDetail>() {
			@Override
			public boolean test(EntityOrderDetail o) {return !o.getProperty().equals(property);}
		}).collect(Collectors.<EntityOrderDetail>toList());
		mOrderList.add(new EntityOrderDetail(property, sorting));
	}


	List<EntityOrderDetail> getOrderList() {
		return mOrderList;
	}


	public JSONArray toJson() throws JSONException {
		JSONArray array = new JSONArray();
		for(EntityOrderDetail order : mOrderList) {
			JSONObject object = new JSONObject();
			object.put(order.getProperty(), order.getSorting().getKey());
			array.put(object);
		}

		return array;
	}
}
