package pt.unl.fct.di.adc.firstwebapp.util;

/**
 * Op9: {@code { "input": { "username", "oldPassword", "newPassword" }, "token": { ... } }} .
 */
public class ChangePasswordRequest {

	public ChangePasswordInput input;
	public AuthToken token;

	public ChangePasswordRequest() {
	}
}
