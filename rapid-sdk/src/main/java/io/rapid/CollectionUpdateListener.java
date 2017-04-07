package io.rapid;


public interface CollectionUpdateListener {
	void onItemRemoved(int position);
	void onItemAdded(int position);
	void onItemMoved(int oldPosition, int newPosition);
}
