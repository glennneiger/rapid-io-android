package io.rapid;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.rapid.base.BaseTest;
import io.rapid.utility.SortUtility;

import static org.junit.Assert.assertEquals;


public class SortUtilityTest extends BaseTest {
	@Test
	public void test_insertToSortedList_1() throws Exception {
		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
				createDoc(new SortingPair("aaaa", Sorting.ASC)),
				createDoc(new SortingPair("aabb", Sorting.ASC)),
				createDoc(new SortingPair("aabc", Sorting.ASC)),
				createDoc(new SortingPair("aabd", Sorting.ASC)),
				createDoc(new SortingPair("aaca", Sorting.ASC)),
				createDoc(new SortingPair("aadd", Sorting.ASC))
		));

		RapidDocument doc = createDoc(new SortingPair("aaba", Sorting.ASC));

		SortUtility.insertToSortedList(list, doc);

		assertEquals(list.indexOf(doc), 1);
	}


	private RapidDocument createDoc(SortingPair... sortingKey) {
		return new RapidDocument(UUID.randomUUID().toString(), Arrays.asList(sortingKey), 0, null);
	}


}