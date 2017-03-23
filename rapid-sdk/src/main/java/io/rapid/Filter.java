package io.rapid;


import org.json.JSONException;


interface Filter {
	String toJson() throws JSONException;
}
