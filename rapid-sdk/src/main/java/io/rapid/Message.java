package io.rapid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


abstract class Message {
	private static final String ATTR_EVENT_ID = "evt-id";

	private MessageType mMessageType;
	private String mEventId;

	public Message(MessageType messageType, JSONObject json) throws JSONException {
		mMessageType = messageType;
		fromJson(json);
	}


	public Message(MessageType messageType) {
		this(messageType, IdProvider.getNewEventId());
	}


	public Message(MessageType messageType, String eventId) {
		mMessageType = messageType;
		mEventId = eventId;
	}


	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		JSONObject body = createJsonBody();
		if(mEventId != null && !mEventId.isEmpty())
			body.put(ATTR_EVENT_ID, mEventId);
		json.put(mMessageType.getKey(), body);
		return json;
	}


	public void fromJson(JSONObject json) throws JSONException {
		if(json != null) {
			mEventId = json.optJSONObject(mMessageType.getKey()).optString(ATTR_EVENT_ID);
			parseJsonBody(json.optJSONObject(mMessageType.getKey()));
		}
	}


	public MessageType getMessageType() {
		return mMessageType;
	}


	public String getEventId() {
		return mEventId;
	}


	protected JSONObject createJsonBody() throws JSONException {
		return new JSONObject();
	}


	protected void parseJsonBody(JSONObject jsonBody) {

	}


	static class Nop extends Message {
		public Nop() {
			super(MessageType.NOP, "");
		}


		public Nop(JSONObject json) throws JSONException {
			super(MessageType.NOP, json);
		}
	}


	static class Ack extends Message {

		Ack(String eventId) {
			super(MessageType.ACK, eventId);
		}


		Ack(JSONObject json) throws JSONException {
			super(MessageType.ACK, json);
		}
	}


	static class Err extends Message {
		private static final String ATTR_ERR_TYPE = "err-type";
		private static final String ATTR_ERR_MSG = "err-msg";

		private ErrorType mErrorType;
		private String mErrorMessage;


		enum ErrorType {
			INTERNAL_ERROR("internal-error"), PERMISSION_DENIED("permission-denied"), CONNECTION_TERMINATED("connection-terminated");


			private String mKey;


			ErrorType(String key) {
				mKey = key;
			}


			static ErrorType get(String key) {
				if(key == null) return INTERNAL_ERROR;

				for(ErrorType item : ErrorType.values()) {
					if(item.getKey().equalsIgnoreCase(key)) {
						return item;
					}
				}
				return INTERNAL_ERROR;
			}


			String getKey() {
				return mKey;
			}
		}


		Err(ErrorType errorType, String errorMessage) throws JSONException {
			super(MessageType.ERR);
			mErrorType = errorType;
			mErrorMessage = errorMessage;
		}


		Err(JSONObject json) throws JSONException {
			super(MessageType.ERR, json);
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_ERR_TYPE, getErrorType().getKey());
			body.put(ATTR_ERR_MSG, getErrorMessage());
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);
			mErrorType = ErrorType.get(jsonBody.optString(ATTR_ERR_TYPE));
			mErrorMessage = jsonBody.optString(ATTR_ERR_MSG);
		}


		public ErrorType getErrorType() {
			return mErrorType;
		}


		public String getErrorMessage() {
			return mErrorMessage;
		}
	}


	static class Mut extends Message {
		private static final String ATTR_COL_ID = "col-id";
		private static final String ATTR_DOC = "doc";

		private String mCollectionId;
		private String mDocument;


		public Mut(JSONObject json) throws JSONException {
			super(MessageType.MUT, json);
		}


		public Mut(String collectionId, String document) {
			super(MessageType.MUT);
			mCollectionId = collectionId;
			mDocument = document;
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_COL_ID, getCollectionId());
			body.put(ATTR_DOC, new JSONObject(getDocument()));
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);
			mCollectionId = jsonBody.optString(ATTR_COL_ID);
			mDocument = jsonBody.optString(ATTR_DOC);
		}


		public String getCollectionId() {
			return mCollectionId;
		}


		public String getDocument() {
			return mDocument;
		}
	}


	static class Con extends Message {
		private static final String ATTR_CON_ID = "con-id";

		private String mConnectionId;


		public Con(String connectionId, boolean reconnect) {
			super(reconnect ? MessageType.REC : MessageType.CON);
			mConnectionId = connectionId;
		}


		public Con(JSONObject json) throws JSONException {
			super(MessageType.CON, json);
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_CON_ID, getConnectionId());
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);
			mConnectionId = jsonBody.optString(ATTR_CON_ID);
		}


		public String getConnectionId() {
			return mConnectionId;
		}
	}


	static class Mer extends Message {
		private static final String ATTR_COL_ID = "col-id";
		private static final String ATTR_DOC = "doc";

		private String mCollectionId;
		private String mDocument;


		public Mer(JSONObject json) throws JSONException {
			super(MessageType.MER, json);
		}


		public Mer(String collectionId, String document) {
			super(MessageType.MER);
			mCollectionId = collectionId;
			mDocument = document;
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_COL_ID, getCollectionId());
			body.put(ATTR_DOC, new JSONObject(getDocument()));
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);
			mCollectionId = jsonBody.optString(ATTR_COL_ID);
			mDocument = jsonBody.optString(ATTR_DOC);
		}


		public String getCollectionId() {
			return mCollectionId;
		}


		public String getDocument() {
			return mDocument;
		}
	}


	static class Dis extends Message {


		public Dis() {
			super(MessageType.DIS);
		}


		public Dis(JSONObject json) throws JSONException {
			super(MessageType.DIS, json);
		}
	}


	static class Batch extends Message {
		private List<Message> mMessageList;


		Batch() {
			super(MessageType.BATCH);
		}


		Batch(JSONObject json) throws JSONException {
			super(MessageType.BATCH, json);
		}


		@Override
		public JSONObject toJson() throws JSONException {
			JSONObject json = new JSONObject();
			JSONArray array = new JSONArray();
			for(Message message : mMessageList) {
				array.put(message.toJson());
			}

			json.put(getMessageType().getKey(), array);
			return json;
		}


		@Override
		public void fromJson(JSONObject json) throws JSONException {
			if(mMessageList == null) {
				mMessageList = new ArrayList<>();
			}
			JSONArray array = json.optJSONArray(getMessageType().getKey());
			for(int i = 0; i < array.length(); i++) {
				mMessageList.add(MessageParser.parse(array.optString(i)));
			}

		}


		public List<Message> getMessageList() {
			return mMessageList;
		}


		public void addMessage(Message message) {
			if(mMessageList == null) {
				mMessageList = new ArrayList<>();
			}
			mMessageList.add(message);
		}
	}


	static class Unknown extends Message {
		Unknown() {
			super(MessageType.UNKNOWN);
		}
	}


	static class Sub extends Message {
		private static final String ATTR_SUB_ID = "sub-id";
		private static final String ATTR_COL_ID = "col-id";
		private static final String ATTR_LIMIT = "limit";
		private static final String ATTR_FILTER = "filter";
		private static final String ATTR_SKIP = "skip";
		private static final String ATTR_ORDER = "order";

		private String mSubscriptionId;
		private String mCollectionId;
		private int mLimit;
		private int mSkip;
		private EntityOrder mOrder;
		private Filter mFilter;


		public Sub(String collectionId, String subscriptionId) {
			super(MessageType.SUB);

			mCollectionId = collectionId;
			mSubscriptionId = subscriptionId;
		}


		public Sub(JSONObject json) throws JSONException {
			super(MessageType.SUB, json);
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_SUB_ID, mSubscriptionId);
			body.put(ATTR_COL_ID, mCollectionId);
			body.put(ATTR_LIMIT, mLimit);
			body.put(ATTR_SKIP, mSkip);
			if(mFilter != null) body.put(ATTR_FILTER, new JSONObject(mFilter.toJson()));
			if(mOrder != null && !mOrder.getOrderList().isEmpty()) body.put(ATTR_ORDER, mOrder.toJson());
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);
			mLimit = Config.DEFAULT_LIMIT;
			mSkip = 0;
			mSubscriptionId = jsonBody.optString(ATTR_SUB_ID);
			mCollectionId = jsonBody.optString(ATTR_COL_ID);
			mLimit = jsonBody.optInt(ATTR_LIMIT);
			mSkip = jsonBody.optInt(ATTR_SKIP);
			mOrder = EntityOrder.fromJson(jsonBody.optJSONArray(ATTR_ORDER));
		}


		public String getSubscriptionId() {
			return mSubscriptionId;
		}


		public String getCollectionId() {
			return mCollectionId;
		}


		public int getLimit() {
			return mLimit;
		}


		public void setLimit(int limit) {
			mLimit = limit;
		}


		public int getSkip() {
			return mSkip;
		}


		public void setSkip(int skip) {
			mSkip = skip;
		}


		public EntityOrder getOrder() {
			return mOrder;
		}


		public void setOrder(EntityOrder order) {
			mOrder = order;
		}


		public Filter getFilter() {
			return mFilter;
		}


		public void setFilter(Filter filter) {
			mFilter = filter;
		}
	}


	static class Uns extends Message {
		private static final String ATTR_SUB_ID = "sub-id";

		private String mSubscriptionId;


		public Uns(String subscriptionId) {
			super(MessageType.UNS);

			mSubscriptionId = subscriptionId;
		}


		Uns(JSONObject json) throws JSONException {
			super(MessageType.UNS, json);
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_SUB_ID, getSubscriptionId());
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);
			mSubscriptionId = jsonBody.optString(ATTR_SUB_ID);
		}


		public String getSubscriptionId() {
			return mSubscriptionId;
		}
	}


	static class Upd extends Message {
		private static final String ATTR_SUB_ID = "sub-id";
		private static final String ATTR_COL_ID = "col-id";
		private static final String ATTR_PSIB_ID = "psib-id";
		private static final String ATTR_DOC = "doc";

		private String mSubscriptionId;
		private String mCollectionId;
		private String mPreviousSiblingId;
		private String mDocument;


		Upd(JSONObject json) throws JSONException {
			super(MessageType.UPD, json);
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_SUB_ID, getSubscriptionId());
			body.put(ATTR_COL_ID, getCollectionId());
			body.put(ATTR_PSIB_ID, getPreviousSiblingId());
			body.put(ATTR_DOC, getDocument());
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);

			mSubscriptionId = jsonBody.optString(ATTR_SUB_ID);
			mCollectionId = jsonBody.optString(ATTR_COL_ID);
			mPreviousSiblingId = jsonBody.optString(ATTR_PSIB_ID);
			mDocument = jsonBody.optString(ATTR_DOC);
		}


		public String getSubscriptionId() {
			return mSubscriptionId;
		}


		public String getCollectionId() {
			return mCollectionId;
		}


		public String getDocument() {
			return mDocument;
		}


		public String getPreviousSiblingId()
		{
			return mPreviousSiblingId;
		}
	}


	static class Val extends Message {
		private static final String ATTR_SUB_ID = "sub-id";
		private static final String ATTR_COL_ID = "col-id";
		private static final String ATTR_DOCS = "docs";

		private String mSubscriptionId;
		private String mCollectionId;
		private String mDocuments;


		Val(JSONObject json) throws JSONException {
			super(MessageType.VAL, json);
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_SUB_ID, getSubscriptionId());
			body.put(ATTR_COL_ID, getCollectionId());
			body.put(ATTR_DOCS, getDocuments());
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);
			mSubscriptionId = jsonBody.optString(ATTR_SUB_ID);
			mCollectionId = jsonBody.optString(ATTR_COL_ID);
			mDocuments = jsonBody.optString(ATTR_DOCS);
		}


		public String getSubscriptionId() {
			return mSubscriptionId;
		}


		public String getCollectionId() {
			return mCollectionId;
		}


		public String getDocuments() {
			return mDocuments;
		}
	}
}
