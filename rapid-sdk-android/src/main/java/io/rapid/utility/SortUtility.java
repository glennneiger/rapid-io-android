package io.rapid.utility;


import android.support.annotation.NonNull;

import java.util.List;


public class SortUtility {

	public static <T extends Comparable<T>> int getInsertPosition(@NonNull List<T> list, @NonNull T item) {

		return getInsertPosition(list, item, 0, list.size() - 1);
	}


	private static <T extends Comparable<T>> int getInsertPosition(@NonNull List<T> list, @NonNull T item, int leftIndex, int rightIndex) {
		if(list.isEmpty())
			return 0;

		if(leftIndex == rightIndex) {
			if(item.compareTo(list.get(leftIndex)) > 0)
				return leftIndex + 1;
			else
				return leftIndex;
		}

		int middleIndex = leftIndex + (rightIndex - leftIndex) / 2;
		if(item.compareTo(list.get(middleIndex)) > 0) {
			return getInsertPosition(list, item, middleIndex + 1, rightIndex);
		} else {
			return getInsertPosition(list, item, leftIndex, Math.max(leftIndex, middleIndex - 1));
		}
	}
}
