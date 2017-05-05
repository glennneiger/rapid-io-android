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


	MessageType getMessageType() {
		return mMessageType;
	}


	String getEventId() {
		return mEventId;
	}


	protected JSONObject createJsonBody() throws JSONException {
		return new JSONObject();
	}


	protected void parseJsonBody(JSONObject jsonBody) {

	}


	static class Nop extends Message {
		Nop() {
			super(MessageType.NOP, "");
		}


		Nop(JSONObject json) throws JSONException {
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

		private Type mType;
		private String mErrorMessage;


		enum Type {
			INTERNAL_ERROR("internal-error"),
			PERMISSION_DENIED("permission-denied"),
			CONNECTION_TERMINATED("connection-terminated"),
			INVALID_AUTH_TOKEN("invalid-auth-token");


			private String mKey;


			Type(String key) {
				mKey = key;
			}


			static Type get(String key) {
				if(key == null) return INTERNAL_ERROR;

				for(Type item : Type.values()) {
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


		Err(Type type, String errorMessage) throws JSONException {
			super(MessageType.ERR);
			mType = type;
			mErrorMessage = errorMessage;
		}


		Err(JSONObject json) throws JSONException {
			super(MessageType.ERR, json);
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_ERR_TYPE, getType().getKey());
			body.put(ATTR_ERR_MSG, getErrorMessage());
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);
			mType = Type.get(jsonBody.optString(ATTR_ERR_TYPE));
			mErrorMessage = jsonBody.optString(ATTR_ERR_MSG);
		}


		public Type getType() {
			return mType;
		}


		public String getErrorMessage() {
			return mErrorMessage;
		}
	}


	static class Auth extends Message {
		private static final String ATTR_TOKEN = "token";

		private String mToken;


		public Auth(JSONObject json) throws JSONException {
			super(MessageType.AUTH, json);
		}


		Auth(String token) {
			super(MessageType.AUTH);
			mToken = token;
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_TOKEN, mToken);
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);
			mToken = jsonBody.optString(ATTR_TOKEN);
		}


		public String getToken() {
			return mToken;
		}
	}


	static class Deauth extends Message {
		public Deauth() {
			super(MessageType.DEAUTH);
		}


		public Deauth(JSONObject json) throws JSONException {
			super(MessageType.DEAUTH, json);
		}
	}


	static class Mut extends Message {
		private static final String ATTR_COL_ID = "col-id";
		private static final String ATTR_DOC = "doc";

		private String mCollectionId;
		private String mDocument;


		Mut(JSONObject json) throws JSONException {
			super(MessageType.MUT, json);
		}


		Mut(String collectionId, String document) {
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


		String getCollectionId() {
			return mCollectionId;
		}


		public String getDocument() {
			return mDocument;
		}
	}


	static class Con extends Message {
		private static final String ATTR_CON_ID = "con-id";

		private String mConnectionId;


		Con(String connectionId, boolean reconnect) {
			super(reconnect ? MessageType.REC : MessageType.CON);
			mConnectionId = connectionId;
		}


		Con(JSONObject json) throws JSONException {
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


		String getConnectionId() {
			return mConnectionId;
		}
	}


	static class Mer extends Message {
		private static final String ATTR_COL_ID = "col-id";
		private static final String ATTR_DOC = "doc";

		private String mCollectionId;
		private String mDocument;


		Mer(JSONObject json) throws JSONException {
			super(MessageType.MER, json);
		}


		Mer(String collectionId, String document) {
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


		String getCollectionId() {
			return mCollectionId;
		}


		public String getDocument() {
			return mDocument;
		}
	}


	static class Dis extends Message {


		Dis() {
			super(MessageType.DIS);
		}


		Dis(JSONObject json) throws JSONException {
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


		List<Message> getMessageList() {
			return mMessageList;
		}


		void addMessage(Message message) {
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


		Sub(String collectionId, String subscriptionId) {
			super(MessageType.SUB);

			mCollectionId = collectionId;
			mSubscriptionId = subscriptionId;
		}


		Sub(JSONObject json) throws JSONException {
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


		String getCollectionId() {
			return mCollectionId;
		}


		int getLimit() {
			return mLimit;
		}


		void setLimit(int limit) {
			mLimit = limit;
		}


		int getSkip() {
			return mSkip;
		}


		void setSkip(int skip) {
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


		Uns(String subscriptionId) {
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
		private static final String ATTR_DOC = "doc";

		private String mSubscriptionId;
		private String mCollectionId;
		private String mDocument;


		Upd(JSONObject json) throws JSONException {
			super(MessageType.UPD, json);
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_SUB_ID, getSubscriptionId());
			body.put(ATTR_COL_ID, getCollectionId());
			body.put(ATTR_DOC, getDocument());
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);

			mSubscriptionId = jsonBody.optString(ATTR_SUB_ID);
			mCollectionId = jsonBody.optString(ATTR_COL_ID);
			mDocument = jsonBody.optString(ATTR_DOC);
		}


		public String getSubscriptionId() {
			return mSubscriptionId;
		}


		String getCollectionId() {
			return mCollectionId;
		}


		public String getDocument() {
			return mDocument;
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


		String getCollectionId() {
			return mCollectionId;
		}


		String getDocuments() {
			return mDocuments;
		}
	}


	static class Ca extends Message {
		private static final String ATTR_SUB_ID = "sub-id";
		private static final String ATTR_COL_ID = "col-id";

		private String mSubscriptionId;
		private String mCollectionId;


		public Ca(String collectionId, String subscriptionId) {
			super(MessageType.CA);

			mCollectionId = collectionId;
			mSubscriptionId = subscriptionId;
		}


		Ca(JSONObject json) throws JSONException {
			super(MessageType.CA, json);
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_SUB_ID, mSubscriptionId);
			body.put(ATTR_COL_ID, mCollectionId);
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);
			mSubscriptionId = jsonBody.optString(ATTR_SUB_ID);
			mCollectionId = jsonBody.optString(ATTR_COL_ID);
		}


		public String getSubscriptionId() {
			return mSubscriptionId;
		}


		String getCollectionId() {
			return mCollectionId;
		}
	}


	static class Del extends Message {
		private static final String ATTR_COL_ID = "col-id";
		private static final String ATTR_DOC_ID = "doc-id";

		private String mCollectionId;
		private String mDocumentId;


		Del(JSONObject json) throws JSONException {
			super(MessageType.DEL, json);
		}


		Del(String collectionId, String documentId) {
			super(MessageType.DEL);
			mCollectionId = collectionId;
			mDocumentId = documentId;
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_COL_ID, getCollectionId());
			body.put(ATTR_DOC_ID, getDocumentId());
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);
			mCollectionId = jsonBody.optString(ATTR_COL_ID);
			mDocumentId = jsonBody.optString(ATTR_DOC_ID);
		}


		String getCollectionId() {
			return mCollectionId;
		}


		String getDocumentId() {
			return mDocumentId;
		}
	}


	static class Rm extends Message {
		private static final String ATTR_SUB_ID = "sub-id";
		private static final String ATTR_COL_ID = "col-id";
		private static final String ATTR_DOC = "doc";

		private String mSubscriptionId;
		private String mCollectionId;
		private String mDocument;


		Rm(JSONObject json) throws JSONException {
			super(MessageType.RM, json);
		}


		@Override
		protected JSONObject createJsonBody() throws JSONException {
			JSONObject body = super.createJsonBody();
			body.put(ATTR_SUB_ID, getSubscriptionId());
			body.put(ATTR_COL_ID, getCollectionId());
			body.put(ATTR_DOC, getDocument());
			return body;
		}


		@Override
		protected void parseJsonBody(JSONObject jsonBody) {
			super.parseJsonBody(jsonBody);

			mSubscriptionId = jsonBody.optString(ATTR_SUB_ID);
			mCollectionId = jsonBody.optString(ATTR_COL_ID);
			mDocument = jsonBody.optString(ATTR_DOC);
		}


		public String getSubscriptionId() {
			return mSubscriptionId;
		}


		String getCollectionId() {
			return mCollectionId;
		}


		String getDocument() {
			return mDocument;
		}
	}
}
