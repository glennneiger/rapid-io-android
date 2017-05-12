package io.rapid;


import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;


public class ListUpdate {
	@SuppressWarnings("WeakerAccess")
	public static final int NO_POSITION = -1;
	private Type mType;
	private int mOldPosition;
	private int mNewPosition;


	public enum Type {
		ADDED, MOVED, NEW_LIST, REMOVED, NEW_LIST_FROM_CACHE, UPDATED
	}


	ListUpdate(Type type, int oldPosition, int newPosition) {
		mType = type;
		mOldPosition = oldPosition;
		mNewPosition = newPosition;
	}


	@SuppressLint("DefaultLocale")
	@Override
	public String toString() {
		if(mType == Type.ADDED)
			return String.format("Item added to position %d", mNewPosition);
		if(mType == Type.MOVED)
			return String.format("Item moved from position %d to position %d", mOldPosition, mNewPosition);
		if(mType == Type.NEW_LIST)
			return "List was refreshed completely";
		if(mType == Type.NEW_LIST_FROM_CACHE)
			return "List was refreshed from cache completely";
		if(mType == Type.REMOVED)
			return String.format("Item from position %d was removed", mOldPosition);
		if(mType == Type.UPDATED)
			return String.format("Item at position %d was changed", mOldPosition);
		else
			return "UNKNOWN";
	}


	public int getOldPosition() {
		return mOldPosition;
	}


	public int getNewPosition() {
		return mNewPosition;
	}


	public void dispatchUpdateTo(RecyclerView.Adapter adapter) {
		if(mType == Type.ADDED)
			adapter.notifyItemInserted(mNewPosition);
		if(mType == Type.MOVED)
			adapter.notifyItemMoved(mOldPosition, mNewPosition);
		if(mType == Type.NEW_LIST || mType == Type.NEW_LIST_FROM_CACHE)
			adapter.notifyDataSetChanged();
		if(mType == Type.REMOVED)
			adapter.notifyItemRemoved(mOldPosition);
		if(mType == Type.UPDATED)
			adapter.notifyItemChanged(mOldPosition);
	}
}
