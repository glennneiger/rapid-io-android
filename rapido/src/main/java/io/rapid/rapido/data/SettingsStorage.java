package io.rapid.rapido.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.rapid.Sorting;


public class SettingsStorage {
	private static final String KEY_ORDER_PROPERTY = "order_property";
	private static final String KEY_ORDER_SORTING = "order_sorting";

	private final Context mContext;


	public SettingsStorage(Context context) {
		mContext = context.getApplicationContext();
	}


	public String getOrderProperty() {
		return getPrefs().getString(KEY_ORDER_PROPERTY, "done");
	}


	public void setOrderProperty(String property) {
		getPrefs().edit().putString(KEY_ORDER_PROPERTY, property).apply();
	}


	public void setOrderSorting(Sorting sorting) {
		getPrefs().edit().putString(KEY_ORDER_SORTING, sorting.name()).apply();
	}


	public Sorting getOrderSorting() {
		return Sorting.valueOf(getPrefs().getString(KEY_ORDER_SORTING, Sorting.ASC.name()));
	}


	private SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(mContext);
	}
}
