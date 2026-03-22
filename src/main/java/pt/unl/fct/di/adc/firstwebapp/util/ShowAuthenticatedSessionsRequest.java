package pt.unl.fct.di.adc.firstwebapp.util;

import java.util.Map;

/**
 * Op6: {@code { "input": {}, "token": { ... } }} .
 */
public class ShowAuthenticatedSessionsRequest {

	public Map<String, Object> input;
	public AuthToken token;

	public ShowAuthenticatedSessionsRequest() {
	}
}
