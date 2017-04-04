package io.rapid;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import io.rapid.base.BaseTest;

import static io.rapid.FilterValue.PropertyValue.TYPE_EQUAL;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class MessageJsonTest extends BaseTest {

	@Test
	public void test_json2modelCon() throws Exception {
		Message msg = MessageParser.parse("{\"con\": {\"evt-id\": \"eventId\", \"con-id\": \"connectionId\"}}");
		assertTrue(msg instanceof Message.Con);

		Message.Con conMsg = (Message.Con) msg;
		assertEquals(conMsg.getConnectionId(), "connectionId");
		assertEquals(conMsg.getMessageType(), MessageType.CON);
		assertEquals(conMsg.getEventId(),"eventId");
	}


	@Test
	public void test_model2jsonCon() throws Exception {
		Message.Con conMsg = new Message.Con("connectionId", false);
		JSONAssert.assertEquals(conMsg.toJson().toString(), "{\"con\": {\"evt-id\": \""+conMsg.getEventId()+"\", \"con-id\": \"connectionId\"}}", false);
	}


	@Test
	public void test_json2modelDis() throws Exception {
		Message msg = MessageParser.parse("{\"dis\": {\"evt-id\": \"eventId\"}}");
		assertTrue(msg instanceof Message.Dis);

		Message.Dis disMsg = (Message.Dis) msg;
		assertEquals(disMsg.getMessageType(), MessageType.DIS);
		assertEquals(disMsg.getEventId(),"eventId");
	}


	@Test
	public void test_model2jsonDis() throws Exception {
		Message.Dis disMsg = new Message.Dis();
		JSONAssert.assertEquals(disMsg.toJson().toString(), "{\"dis\": {\"evt-id\": \""+disMsg.getEventId()+"\"}}", false);
	}


	@Test
	public void test_json2modelAck() throws Exception {
		Message msg = MessageParser.parse("{\"ack\": {\"evt-id\": \"eventId\"}}");
		assertTrue(msg instanceof Message.Ack);

		Message.Ack ackMsg = (Message.Ack) msg;
		assertEquals(ackMsg.getMessageType(), MessageType.ACK);
		assertEquals(ackMsg.getEventId(),"eventId");
	}


	@Test
	public void test_model2jsonAck() throws Exception {
		Message.Ack ackMsg = new Message.Ack("eventId");
		JSONAssert.assertEquals(ackMsg.toJson().toString(), "{\"ack\": {\"evt-id\": \"eventId\"}}", false);
	}


	@Test
	public void test_json2modelHb() throws Exception {
		Message msg = MessageParser.parse("{\"nop\": {\"evt-id\": \"eventId\"}}");
		assertTrue(msg instanceof Message.Nop);

		Message.Nop hbMsg = (Message.Nop) msg;
		assertEquals(hbMsg.getMessageType(), MessageType.NOP);
		assertEquals(hbMsg.getEventId(),"eventId");
	}


	@Test
	public void test_model2jsonHb() throws Exception {
		Message.Nop hbMsg = new Message.Nop();
		JSONAssert.assertEquals(hbMsg.toJson().toString(), "{\"nop\":{}}", false);
	}


	@Test
	public void test_json2modelMut() throws Exception {
		Message msg = MessageParser.parse("{\"mut\": {\"evt-id\": \"eventId\", \"col-id\":\"collection\", \"doc\": {}}}");
		assertTrue(msg instanceof Message.Mut);

		Message.Mut mutMsg = (Message.Mut) msg;
		assertEquals(mutMsg.getMessageType(), MessageType.MUT);
		assertEquals(mutMsg.getEventId(),"eventId");
		assertEquals(mutMsg.getCollectionId(),"collection");
		assertEquals(mutMsg.getDocument(),"{}");
	}


	@Test
	public void test_model2jsonMut() throws Exception {
		Message.Mut mutMsg = new Message.Mut("collection", "{}");
		JSONAssert.assertEquals(mutMsg.toJson().toString(), "{\"mut\": {\"evt-id\": \""+mutMsg.getEventId()+"\", \"col-id\":\"collection\", \"doc\": {}}}", false);
	}


	@Test
	public void test_json2modelMer() throws Exception {
		Message msg = MessageParser.parse("{\"mer\": {\"evt-id\": \"eventId\", \"col-id\":\"collection\", \"doc\": {}}}");
		assertTrue(msg instanceof Message.Mer);

		Message.Mer merMsg = (Message.Mer) msg;
		assertEquals(merMsg.getMessageType(), MessageType.MER);
		assertEquals(merMsg.getEventId(),"eventId");
		assertEquals(merMsg.getCollectionId(),"collection");
		assertEquals(merMsg.getDocument(),"{}");
	}


	@Test
	public void test_model2jsonMer() throws Exception {
		Message.Mer merMsg = new Message.Mer("collection", "{}");
		JSONAssert.assertEquals(merMsg.toJson().toString(), "{\"mer\": {\"evt-id\": \""+merMsg.getEventId()+"\", \"col-id\":\"collection\", \"doc\": {}}}", false);
	}


	@Test
	public void test_json2modelSub() throws Exception {
		Message msg = MessageParser.parse("{\"sub\": {\"evt-id\": \"eventId\", \"col-id\":\"collection\", " +
				"\"sub-id\":\"subscriptionId\", \"order\":[{\"manufacturer\":\"asc\"}," +
				"{\"model\":\"desc\"}], \"limit\":10,\"skip\":1}}");
		assertTrue(msg instanceof Message.Sub);

		Message.Sub subMsg = (Message.Sub) msg;
		assertEquals(subMsg.getMessageType(), MessageType.SUB);
		assertEquals(subMsg.getEventId(),"eventId");
		assertEquals(subMsg.getCollectionId(),"collection");
		assertEquals(subMsg.getSubscriptionId(),"subscriptionId");

		EntityOrder order = subMsg.getOrder();
		assertEquals(order.getOrderList().get(0).getProperty(), "manufacturer");
		assertEquals(order.getOrderList().get(0).getSorting(), Sorting.ASC);
		assertEquals(order.getOrderList().get(1).getProperty(), "model");
		assertEquals(order.getOrderList().get(1).getSorting(), Sorting.DESC);

		assertEquals(10, subMsg.getLimit());
		assertEquals(1, subMsg.getSkip());
	}


	@Test
	public void test_model2jsonSub() throws Exception {
		Message.Sub subMsg = new Message.Sub("collection", "subscriptionId");
		EntityOrder order = new EntityOrder();
		order.putOrder("manufacturer", Sorting.ASC);
		order.putOrder("model", Sorting.DESC);
		subMsg.setOrder(order);
		Filter.Group filter = new Filter.And();
		filter.add(new FilterValue("model", new FilterValue.StringPropertyValue(TYPE_EQUAL, "A5")));
		subMsg.setFilter(filter);
		subMsg.setLimit(10);
		subMsg.setSkip(0);
		JSONAssert.assertEquals(subMsg.toJson().toString(), "{\"sub\": {\"evt-id\": \""+subMsg.getEventId()+"\", \"col-id\":\"collection\", " +
				"\"sub-id\":\"subscriptionId\", \"filter\":{\"and\":[{\"model\":\"A5\"}]}, \"order\":[{\"manufacturer\":\"asc\"}," +
				"{\"model\":\"desc\"}], \"limit\":10,\"skip\":0}}", false);
	}


	@Test
	public void test_json2modelUns() throws Exception {
		Message msg = MessageParser.parse("{\"uns\": {\"evt-id\": \"eventId\", \"sub-id\":\"subscriptionId\"}}");
		assertTrue(msg instanceof Message.Uns);

		Message.Uns unsMsg = (Message.Uns) msg;
		assertEquals(unsMsg.getMessageType(), MessageType.UNS);
		assertEquals(unsMsg.getEventId(),"eventId");
		assertEquals(unsMsg.getSubscriptionId(),"subscriptionId");
	}


	@Test
	public void test_model2jsonUns() throws Exception {
		Message.Uns unsMsg = new Message.Uns("subscriptionId");
		JSONAssert.assertEquals(unsMsg.toJson().toString(), "{\"uns\": {\"evt-id\": \""+unsMsg.getEventId()+"\", \"sub-id\":\"subscriptionId\"}}", false);
	}


	@Test
	public void test_json2modelVal() throws Exception {
		Message msg = MessageParser.parse("{\"val\": {\"evt-id\": \"eventId\", \"sub-id\":\"subscriptionId\", \"col-id\":\"collection\", " +
				"\"doc\": {}}}");
		assertTrue(msg instanceof Message.Val);

		Message.Val valMsg = (Message.Val) msg;
		assertEquals(valMsg.getMessageType(), MessageType.VAL);
		assertEquals(valMsg.getEventId(),"eventId");
		assertEquals(valMsg.getSubscriptionId(),"subscriptionId");
		assertEquals(valMsg.getCollectionId(),"collection");
	}


	@Test
	public void test_json2modelUpd() throws Exception {
		Message msg = MessageParser.parse("{\"upd\": {\"evt-id\": \"eventId\", \"sub-id\":\"subscriptionId\", \"col-id\":\"collection\", " +
				"\"doc\": {}}}");
		assertTrue(msg instanceof Message.Upd);

		Message.Upd valMsg = (Message.Upd) msg;
		assertEquals(valMsg.getMessageType(), MessageType.UPD);
		assertEquals(valMsg.getEventId(),"eventId");
		assertEquals(valMsg.getSubscriptionId(),"subscriptionId");
		assertEquals(valMsg.getCollectionId(),"collection");
	}


	@Test
	public void test_json2modelBatch() throws Exception {
		Message msg = MessageParser.parse("{\"batch\":[{\"ack\": {\"evt-id\": \"eventId\"}}, {\"nop\": {}}]}");
		assertTrue(msg instanceof Message.Batch);

		Message.Batch batchMsg = (Message.Batch) msg;
		assertEquals(batchMsg.getMessageType(), MessageType.BATCH);
		Message.Ack ackMsg = (Message.Ack) batchMsg.getMessageList().get(0);
		assertEquals(ackMsg.getMessageType(), MessageType.ACK);
		assertEquals(ackMsg.getEventId(),"eventId");
		Message.Nop hbMsg = (Message.Nop) batchMsg.getMessageList().get(1);
		assertEquals(hbMsg.getMessageType(), MessageType.NOP);
	}


	@Test
	public void test_model2jsonBatch() throws Exception {
		Message.Batch batchMsg = new Message.Batch();
		batchMsg.addMessage(new Message.Ack("eventId"));
		batchMsg.addMessage(new Message.Nop());
		JSONAssert.assertEquals(batchMsg.toJson().toString(), "{\"batch\":[{\"ack\": {\"evt-id\": \"eventId\"}}, {\"nop\": {}}]}", false);
	}


	@Test
	public void test_json2modelErr() throws Exception {
		Message msg = MessageParser.parse("{\"err\": {\"evt-id\": \"eventId\", \"err-type\": \"connection-terminated\", \"err-msg\": " +
				"\"Something went wrong\"}}");
		assertTrue(msg instanceof Message.Err);

		Message.Err errMsg = (Message.Err) msg;
		assertEquals(errMsg.getMessageType(), MessageType.ERR);
		assertEquals(errMsg.getEventId(),"eventId");
		assertEquals(errMsg.getErrorType(), Message.Err.ErrorType.CONNECTION_TERMINATED);
		assertEquals(errMsg.getErrorMessage(), "Something went wrong");
	}


	@Test
	public void test_model2jsonErr() throws Exception {
		Message.Err errMsg = new Message.Err(Message.Err.ErrorType.CONNECTION_TERMINATED, "Something went wrong");
		JSONAssert.assertEquals(errMsg.toJson().toString(), "{\"err\": {\"evt-id\": \""+errMsg.getEventId()+"\", \"err-type\": \"connection-terminated\", " +
				"\"err-msg\":\"Something went wrong\"}}", false);
	}


	@Test
	public void test_json2modelUnknown() throws Exception {
		Message msg = MessageParser.parse("{\"asd\": {}}");
		assertTrue(msg instanceof Message.Unknown);

		Message.Unknown unknownMsg = (Message.Unknown) msg;
		assertEquals(unknownMsg.getMessageType(), MessageType.UNKNOWN);
	}

}