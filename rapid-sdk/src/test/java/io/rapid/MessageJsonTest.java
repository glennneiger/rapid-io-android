package io.rapid;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import io.rapid.base.BaseTest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class MessageJsonTest extends BaseTest {

	@Test
	public void test_json2modelCon() throws Exception {
		MessageBase msg = MessageParser.parse("{\"con\": {\"evt-id\": \"eventId\", \"con-id\": \"connectionId\"}}");
		assertTrue(msg instanceof MessageCon);

		MessageCon conMsg = (MessageCon) msg;
		assertEquals(conMsg.getConnectionId(), "connectionId");
		assertEquals(conMsg.getMessageType(), MessageBase.MessageType.CON);
		assertEquals(conMsg.getEventId(),"eventId");
	}


	@Test
	public void test_model2jsonCon() throws Exception {
		MessageCon conMsg = new MessageCon("eventId", "connectionId");
		JSONAssert.assertEquals(conMsg.toJson().toString(), "{\"con\": {\"evt-id\": \"eventId\", \"con-id\": \"connectionId\"}}", false);
	}


	@Test
	public void test_json2modelDis() throws Exception {
		MessageBase msg = MessageParser.parse("{\"dis\": {\"evt-id\": \"eventId\"}}");
		assertTrue(msg instanceof MessageDis);

		MessageDis disMsg = (MessageDis) msg;
		assertEquals(disMsg.getMessageType(), MessageBase.MessageType.DIS);
		assertEquals(disMsg.getEventId(),"eventId");
	}


	@Test
	public void test_model2jsonDis() throws Exception {
		MessageDis disMsg = new MessageDis("eventId");
		JSONAssert.assertEquals(disMsg.toJson().toString(), "{\"dis\": {\"evt-id\": \"eventId\"}}", false);
	}


	@Test
	public void test_json2modelAck() throws Exception {
		MessageBase msg = MessageParser.parse("{\"ack\": {\"evt-id\": \"eventId\"}}");
		assertTrue(msg instanceof MessageAck);

		MessageAck disMsg = (MessageAck) msg;
		assertEquals(disMsg.getMessageType(), MessageBase.MessageType.ACK);
		assertEquals(disMsg.getEventId(),"eventId");
	}


	@Test
	public void test_model2jsonAck() throws Exception {
		MessageAck disMsg = new MessageAck("eventId");
		JSONAssert.assertEquals(disMsg.toJson().toString(), "{\"ack\": {\"evt-id\": \"eventId\"}}", false);
	}


	@Test
	public void test_json2modelHb() throws Exception {
		MessageBase msg = MessageParser.parse("{\"hb\": {\"evt-id\": \"eventId\"}}");
		assertTrue(msg instanceof MessageHb);

		MessageHb hbMsg = (MessageHb) msg;
		assertEquals(hbMsg.getMessageType(), MessageBase.MessageType.HB);
		assertEquals(hbMsg.getEventId(),"eventId");
	}


	@Test
	public void test_model2jsonHb() throws Exception {
		MessageHb hbMsg = new MessageHb("eventId");
		JSONAssert.assertEquals(hbMsg.toJson().toString(), "{\"hb\": {\"evt-id\": \"eventId\"}}", false);
	}


	@Test
	public void test_json2modelMut() throws Exception {
		MessageBase msg = MessageParser.parse("{\"mut\": {\"evt-id\": \"eventId\", \"col-id\":\"collection\", \"doc\": {}}}");
		assertTrue(msg instanceof MessageMut);

		MessageMut mutMsg = (MessageMut) msg;
		assertEquals(mutMsg.getMessageType(), MessageBase.MessageType.MUT);
		assertEquals(mutMsg.getEventId(),"eventId");
		assertEquals(mutMsg.getCollectionId(),"collection");
		assertEquals(mutMsg.getDocument(),"{}");
	}


	@Test
	public void test_model2jsonMut() throws Exception {
		MessageMut mutMsg = new MessageMut("eventId", "collection", "{}");
		JSONAssert.assertEquals(mutMsg.toJson().toString(), "{\"mut\": {\"evt-id\": \"eventId\", \"col-id\":\"collection\", \"doc\": {}}}", false);
	}


	@Test
	public void test_json2modelMer() throws Exception {
		MessageBase msg = MessageParser.parse("{\"mer\": {\"evt-id\": \"eventId\", \"col-id\":\"collection\", \"doc\": {}}}");
		assertTrue(msg instanceof MessageMer);

		MessageMer merMsg = (MessageMer) msg;
		assertEquals(merMsg.getMessageType(), MessageBase.MessageType.MER);
		assertEquals(merMsg.getEventId(),"eventId");
		assertEquals(merMsg.getCollectionId(),"collection");
		assertEquals(merMsg.getDocument(),"{}");
	}


	@Test
	public void test_model2jsonMer() throws Exception {
		MessageMer merMsg = new MessageMer("eventId", "collection", "{}");
		JSONAssert.assertEquals(merMsg.toJson().toString(), "{\"mer\": {\"evt-id\": \"eventId\", \"col-id\":\"collection\", \"doc\": {}}}", false);
	}


	@Test
	public void test_json2modelSub() throws Exception {
		MessageBase msg = MessageParser.parse("{\"sub\": {\"evt-id\": \"eventId\", \"col-id\":\"collection\", " +
				"\"sub-id\":\"subscriptionId\", \"filter\":[], \"order\":[], \"limit\":10, \"skip\":0}}");
		assertTrue(msg instanceof MessageSub);

		MessageSub subMsg = (MessageSub) msg;
		assertEquals(subMsg.getMessageType(), MessageBase.MessageType.SUB);
		assertEquals(subMsg.getEventId(),"eventId");
		assertEquals(subMsg.getCollectionId(),"collection");
		assertEquals(subMsg.getSubscriptionId(),"subscriptionId");
		assertEquals(subMsg.getLimit(), 10);
		assertEquals(subMsg.getSkip(), 0);
	}


	@Test
	public void test_model2jsonSub() throws Exception {
		MessageSub subMsg = new MessageSub("eventId", "collection", "subscriptionId");
		EntityOrder order = new EntityOrder();
		order.putOrder("manufacturer", Sorting.ASC);
		order.putOrder("model", Sorting.DESC);
		subMsg.setOrder(order);
		FilterGroup filter = new FilterAnd();
		filter.add(new FilterValue("model", new FilterValue.StringComparePropertyValue(FilterValue.PropertyValue.TYPE_EQUAL, "A5")));
		subMsg.setFilter(filter);
		subMsg.setLimit(10);
		subMsg.setSkip(0);
		JSONAssert.assertEquals(subMsg.toJson().toString(), "{\"sub\": {\"evt-id\": \"eventId\", \"col-id\":\"collection\", " +
				"\"sub-id\":\"subscriptionId\", \"filter\":{\"and\":[{\"model\":\"A5\"}]}, \"order\":[{\"manufacturer\":\"asc\"}," +
				"{\"model\":\"desc\"}], \"limit\":10,\"skip\":0}}", false);
	}


	@Test
	public void test_json2modelUns() throws Exception {
		MessageBase msg = MessageParser.parse("{\"uns\": {\"evt-id\": \"eventId\", \"sub-id\":\"subscriptionId\"}}");
		assertTrue(msg instanceof MessageUns);

		MessageUns unsMsg = (MessageUns) msg;
		assertEquals(unsMsg.getMessageType(), MessageBase.MessageType.UNS);
		assertEquals(unsMsg.getEventId(),"eventId");
		assertEquals(unsMsg.getSubscriptionId(),"subscriptionId");
	}


	@Test
	public void test_model2jsonUns() throws Exception {
		MessageUns unsMsg = new MessageUns("eventId", "subscriptionId");
		JSONAssert.assertEquals(unsMsg.toJson().toString(), "{\"uns\": {\"evt-id\": \"eventId\", \"sub-id\":\"subscriptionId\"}}", false);
	}


	@Test
	public void test_json2modelVal() throws Exception {
		MessageBase msg = MessageParser.parse("{\"val\": {\"evt-id\": \"eventId\", \"sub-id\":\"subscriptionId\", \"col-id\":\"collection\", " +
				"\"doc\": {}}}");
		assertTrue(msg instanceof MessageVal);

		MessageVal valMsg = (MessageVal) msg;
		assertEquals(valMsg.getMessageType(), MessageBase.MessageType.VAL);
		assertEquals(valMsg.getEventId(),"eventId");
		assertEquals(valMsg.getSubscriptionId(),"subscriptionId");
		assertEquals(valMsg.getCollectionId(),"collection");
	}


	@Test
	public void test_json2modelUpd() throws Exception {
		MessageBase msg = MessageParser.parse("{\"upd\": {\"evt-id\": \"eventId\", \"sub-id\":\"subscriptionId\", \"col-id\":\"collection\", " +
				"\"doc\": {}}}");
		assertTrue(msg instanceof MessageUpd);

		MessageUpd valMsg = (MessageUpd) msg;
		assertEquals(valMsg.getMessageType(), MessageBase.MessageType.UPD);
		assertEquals(valMsg.getEventId(),"eventId");
		assertEquals(valMsg.getSubscriptionId(),"subscriptionId");
		assertEquals(valMsg.getCollectionId(),"collection");
	}

}