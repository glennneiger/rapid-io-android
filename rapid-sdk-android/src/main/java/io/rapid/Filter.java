package io.rapid;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


interface Filter {
	String toJson() throws JSONException;


	abstract class Group implements Filter {
		@NonNull List<Filter> filters = new ArrayList<>();


		Group(Filter... filters) {
			this.filters.addAll(Arrays.asList(filters));
		}


		@Nullable
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


	class Single extends Group {

		Single(Filter... filters) {
			super(filters);
		}


		@Nullable
		@Override
		protected String getKeyword() {
			return null;
		}


		@Override
		public String toJson() throws JSONException {
			return filters.get(filters.size() - 1).toJson();
		}
	}


	class And extends Group {

		And(Filter... filters) {
			super(filters);
		}


		@NonNull
		@Override
		protected String getKeyword() {
			return "and";
		}

	}


	class Not extends Group {

		Not(Filter... filters) {
			super(filters);
		}


		@NonNull
		@Override
		protected String getKeyword() {
			return "not";
		}


		@Override
		public String toJson() throws JSONException {
			JSONObject root = new JSONObject();
			if(filters.size() > 1) {
				root.put(getKeyword(), new JSONObject(new And(filters.toArray(new Filter[]{})).toJson()));
			} else if(filters.size() == 1) {
				root.put(getKeyword(), new JSONObject(filters.get(0).toJson()));
			}
			return root.toString();
		}
	}


	class Or extends Group {

		Or(Filter... filters) {
			super(filters);
		}


		@NonNull
		@Override
		protected String getKeyword() {
			return "or";
		}
	}
}
