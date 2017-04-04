package io.rapid;

import org.json.JSONException;
import org.json.JSONObject;


class MessageSub extends MessageBase {
	private static final String ATTR_SUB_ID = "sub-id";
	private static final String ATTR_COL_ID = "col-id";
	private static final String ATTR_LIMIT = "limit";
	private static final String ATTR_FILTER = "filter";
	private static final String ATTR_SKIP = "skip";
	private static final String ATTR_ORDER = "order";

	private String mSubscriptionId;
	private String mCollectionId;
	private int mLimit = Config.DEFAULT_LIMIT;
	private int mSkip = 0;
	private EntityOrder mOrder;
	private Filter mFilter;


	public MessageSub(String collectionId, String subscriptionId) {
		super(MessageType.SUB);

		mCollectionId = collectionId;
		mSubscriptionId = subscriptionId;
	}


	public MessageSub(JSONObject json) throws JSONException {
		super(MessageType.SUB, json);
	}


	@Override
	protected JSONObject createJsonBody() throws JSONException {
		JSONObject body = super.createJsonBody();
		body.put(ATTR_SUB_ID, mSubscriptionId);
		body.put(ATTR_COL_ID, mCollectionId);
		body.put(ATTR_LIMIT, mLimit);
		body.put(ATTR_SKIP, mSkip);
		body.put(ATTR_FILTER, new JSONObject(mFilter.toJson()));
		if(mOrder != null && !mOrder.getOrderList().isEmpty()) body.put(ATTR_ORDER, mOrder.toJson());
		return body;
	}


	@Override
	protected void parseJsonBody(JSONObject jsonBody) {
		super.parseJsonBody(jsonBody);
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
