package io.rapid;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


interface Filter {
	String toJson() throws JSONException;
	class And extends Group {

		public And(Filter... filters) {
			super(filters);
		}


		@Override
		protected String getKeyword() {
			return "and";
		}

	}


	abstract class Group implements Filter {
		List<Filter> filters = new ArrayList<>();


		public Group(Filter... filters) {
			this.filters.addAll(Arrays.asList(filters));
		}


		protected abstract String getKeyword();


		@Override
		public String toJson() throws JSONException {
			JSONObject root = new JSONObject();
			JSONArray jsonFilters = new JSONArray();
			for(Filter filter : filters) {
				jsonFilters.put(new JSONObject(filter.toJson()));
			}
			root.put(getKeyword(), jsonFilters);
			return root.toString();
		}


		public void add(Filter filter) {
			filters.add(filter);
		}

	}


	class Not extends Group {

		public Not(Filter... filters) {
			super(filters);
		}


		@Override
		protected String getKeyword() {
			return "not";
		}
	}


	class Or extends Group {

		public Or(Filter... filters) {
			super(filters);
		}


		@Override
		protected String getKeyword() {
			return "or";
		}
	}
}
