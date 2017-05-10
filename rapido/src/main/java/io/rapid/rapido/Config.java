package io.rapid.rapido;


public class Config {

	// ==============
	// DEMO CLIENT ID
	// REPLACE THIS VALUE WITH YOUR CLIENT ID OBTAINED ON http://rapid.io
	public static final String RAPID_DEMO_CLIENT_ID = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9";
	// ==============

	/**
	 * Collection name based on Demo Client ID
	 */
	public static final String RAPID_TODO_COLLECTION_NAME = "demoapp-" + RAPID_DEMO_CLIENT_ID;

	/**
	 * Rapid.io Authorization token allowing unlimited access to collections named `demoapp-.*`
	 */
	public static final String RAPID_AUTH_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJydWxlcyI6W3siY29sbGVjdGlvbiI6ImRlbW9hcHAtLioiLCJyZWFkIjp0cnVlLCJjcmVhdGUiOnRydWUsInVwZGF0ZSI6dHJ1ZSwiZGVsZXRlIjp0cnVlfV19.9e1b1eT1cfoxz7QqydF0eiFRiFP6qvHRHsqHxJ_ymuo";
}
