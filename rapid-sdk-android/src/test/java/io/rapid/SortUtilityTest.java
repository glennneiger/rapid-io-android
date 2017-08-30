package io.rapid;

import android.support.annotation.Nullable;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.rapid.base.BaseTest;
import io.rapid.utility.SortUtility;

import static org.junit.Assert.assertEquals;


public class SortUtilityTest extends BaseTest {

	// TODO: temp commented due to temp fix for multiple-level sorting
//	@Test
//	public void test_insertToSortedList_1() throws Exception {
//		EntityOrder order = new EntityOrder();
//		order.putOrder("a", Sorting.ASC);
//		order.putOrder("b", Sorting.DESC);
//		order.putOrder("c", Sorting.DESC);
//
//		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
//				createDoc(order, "aaaa", "9", "zzzz"),
//				createDoc(order, "aaaa", "5", "zzzz"),
//				createDoc(order, "aaaa", "5", "zzzz"),
//				createDoc(order, "aaaa", "3", "vvvv"),
//				createDoc(order, "aaaa", "3", "vvvv"),
//				createDoc(order, "aaaa", "3", "bbbb")
//		));
//
//		RapidDocument doc = createDoc(order, "aaaa", "3", "bbbb");
//
//		int pos = SortUtility.getInsertPosition(list, doc);
//
//		assertEquals(6, pos);
//	}


	@Test
	public void test_insertToSortedList_2() throws Exception {
		EntityOrder order = new EntityOrder();
		order.putOrder("a", Sorting.ASC);
		order.putOrder("b", Sorting.DESC);
		order.putOrder("c", Sorting.DESC);

		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
				createDoc(order, "aaaa", "9", "zzzz"),
				createDoc(order, "aaaa", "5", "zzzz"),
				createDoc(order, "aaaa", "5", "zzzz"),
				createDoc(order, "aaaa", "3", "vvvv"),
				createDoc(order, "aaaa", "3", "vvvv"),
				createDoc(order, "aaaa", "3", "bbbb")
		));

		RapidDocument doc = createDoc(order, "aaaa", "3", "cccc");

		int pos = SortUtility.getInsertPosition(list, doc);

		assertEquals(5, pos);
	}


	@Test
	public void test_insertToSortedList_3() throws Exception {
		EntityOrder order = new EntityOrder();
		order.putOrder("a", Sorting.ASC);
		order.putOrder("b", Sorting.DESC);
		order.putOrder("c", Sorting.DESC);

		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
				createDoc(order, "aaaa", "9", "zzzz"),
				createDoc(order, "aaaa", "5", "zzzz"),
				createDoc(order, "aaaa", "5", "zzzz"),
				createDoc(order, "aaaa", "3", "vvvv"),
				createDoc(order, "aaaa", "3", "vvvv"),
				createDoc(order, "aaaa", "3", "bbbb")
		));

		RapidDocument doc = createDoc(order, "aaaa", "3", "zzzz");

		int pos = SortUtility.getInsertPosition(list, doc);

		assertEquals(3, pos);
	}


	@Test
	public void test_insertToSortedList_4() throws Exception {
		EntityOrder order = new EntityOrder();
		order.putOrder("a", Sorting.ASC);
		order.putOrder("b", Sorting.DESC);
		order.putOrder("c", Sorting.DESC);

		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
				createDoc(order, "aaaa", "09", "zzzz"),
				createDoc(order, "aaaa", "05", "zzzz"),
				createDoc(order, "aaaa", "05", "zzzz"),
				createDoc(order, "aaaa", "03", "vvvv"),
				createDoc(order, "aaaa", "03", "vvvv"),
				createDoc(order, "aaaa", "03", "bbbb")
		));

		RapidDocument doc = createDoc(order, "aaaa", "10", "zzzz");

		int pos = SortUtility.getInsertPosition(list, doc);

		assertEquals(0, pos);
	}


	@Test
	public void test_insertToSortedList_5() throws Exception {
		EntityOrder order = new EntityOrder();
		order.putOrder("a", Sorting.ASC);
		order.putOrder("b", Sorting.DESC);
		order.putOrder("c", Sorting.DESC);

		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
				createDoc(order, "aaaa", "9", "zzzz"),
				createDoc(order, "aaaa", "5", "zzzz"),
				createDoc(order, "aaaa", "5", "zzzz"),
				createDoc(order, "aaaa", "3", "vvvv"),
				createDoc(order, "aaaa", "3", "vvvv"),
				createDoc(order, "aaaa", "3", "bbbb")
		));

		RapidDocument doc = createDoc(order, "aaaa", "1", "zzzz");

		int pos = SortUtility.getInsertPosition(list, doc);

		assertEquals(6, pos);
	}


	@Test
	public void test_insertToSortedList_6() throws Exception {
		EntityOrder order = new EntityOrder();
		order.putOrder("a", Sorting.ASC);
		order.putOrder("b", Sorting.DESC);
		order.putOrder("c", Sorting.DESC);

		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
				createDoc(order, "aaaa", "9", "zzzz"),
				createDoc(order, "aaaa", "5", "zzzz"),
				createDoc(order, "aaaa", "5", "zzzz"),
				createDoc(order, "aaaa", "3", "vvvv"),
				createDoc(order, "aaaa", "3", "mmmm"),
				createDoc(order, "aaaa", "3", "bbbb")
		));

		RapidDocument doc = createDoc(order, "aaaa", "3", "llll");

		int pos = SortUtility.getInsertPosition(list, doc);

		assertEquals(5, pos);
	}


	@Test
	public void test_insertToSortedList_7() throws Exception {
		EntityOrder order = new EntityOrder();
		order.putOrder("a", Sorting.ASC);
		order.putOrder("b", Sorting.ASC);
		order.putOrder("c", Sorting.ASC);

		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
				createDoc(order, "cccc", "03", "cccc"),
				createDoc(order, "cccc", "05", "gggg"),
				createDoc(order, "cccc", "05", "kkkk"),
				createDoc(order, "cccc", "08", "nnnn"),
				createDoc(order, "cccc", "12", "pppp"),
				createDoc(order, "cccc", "19", "rrrr")
		));

		RapidDocument doc = createDoc(order, "aaaa", "3", "llll");

		int pos = SortUtility.getInsertPosition(list, doc);

		assertEquals(0, pos);
	}


	@Test
	public void test_insertToSortedList_8() throws Exception {
		EntityOrder order = new EntityOrder();
		order.putOrder("a", Sorting.ASC);
		order.putOrder("b", Sorting.ASC);
		order.putOrder("c", Sorting.ASC);

		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
				createDoc(order, "cccc", "03", "cccc"),
				createDoc(order, "cccc", "05", "gggg"),
				createDoc(order, "cccc", "05", "kkkk"),
				createDoc(order, "cccc", "08", "nnnn"),
				createDoc(order, "cccc", "12", "pppp"),
				createDoc(order, "cccc", "19", "rrrr")
		));

		RapidDocument doc = createDoc(order, "dddd", "3", "llll");

		int pos = SortUtility.getInsertPosition(list, doc);

		assertEquals(6, pos);
	}


	@Test
	public void test_insertToSortedList_9() throws Exception {
		EntityOrder order = new EntityOrder();
		order.putOrder("a", Sorting.ASC);
		order.putOrder("b", Sorting.ASC);
		order.putOrder("c", Sorting.ASC);

		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
				createDoc(order, "cccc", "03", "cccc"),
				createDoc(order, "cccc", "05", "gggg"),
				createDoc(order, "cccc", "05", "kkkk"),
				createDoc(order, "cccc", "08", "nnnn"),
				createDoc(order, "cccc", "12", "pppp"),
				createDoc(order, "cccc", "19", "rrrr")
		));

		RapidDocument doc = createDoc(order, "cccc", "05", "llll");

		int pos = SortUtility.getInsertPosition(list, doc);

		assertEquals(3, pos);
	}


	// TODO: temp commented due to temp fix for multiple-level sorting
//	@Test
//	public void test_insertToSortedList_10() throws Exception {
//		EntityOrder order = new EntityOrder();
//		order.putOrder("a", Sorting.ASC);
//		order.putOrder("b", Sorting.ASC);
//		order.putOrder("c", Sorting.ASC);
//
//		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
//				createDoc(order, "cccc", "03", "cccc"),
//				createDoc(order, "cccc", "05", "gggg"),
//				createDoc(order, "cccc", "05", "kkkk"),
//				createDoc(order, "cccc", "08", "nnnn"),
//				createDoc(order, "cccc", "12", "pppp"),
//				createDoc(order, "cccc", "19", "rrrr")
//		));
//
//		RapidDocument doc = createDoc(order, "cccc", "05", "kkkk");
//
//		int pos = SortUtility.getInsertPosition(list, doc);
//
//		assertEquals(3, pos);
//	}


	@Test
	public void test_insertToSortedList_11() throws Exception {
		EntityOrder order = new EntityOrder();
		order.putOrder("a", Sorting.ASC);
		order.putOrder("b", Sorting.ASC);
		order.putOrder("c", Sorting.ASC);

		RapidDocument doc = createDoc(order, "cccc", "05", "kkkk");

		List<RapidDocument> list = new ArrayList<>(Arrays.asList(
				createDoc(order, "cccc", "03", "cccc"),
				createDoc(order, "cccc", "05", "gggg"),
				createDoc(order, "cccc", "05", "kkkk"),
				createDoc(order, "cccc", "08", "nnnn"),
				createDoc(order, "cccc", "12", "pppp"),
				createDoc(order, "cccc", "19", "rrrr")
		));

		int pos = SortUtility.getInsertPosition(list, doc);

		assertEquals(2, pos);
	}


	@Nullable
	private RapidDocument createDoc(EntityOrder order, String... sortingKey) {
		RapidDocument doc = new RapidDocument(UUID.randomUUID().toString(), new ArrayList<>(Arrays.asList(sortingKey)), "timestamp", Etag.NO_ETAG, null);
		doc.setOrder(order);
		return doc;
	}


}