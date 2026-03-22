package pt.unl.fct.di.adc.firstwebapp.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Op9: {@code "input": { "userId", "oldPassword", "newPassword" }} .
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangePasswordInput {

	public String userId;
	public String oldPassword;
	public String newPassword;

	public ChangePasswordInput() {
	}

	public boolean valid() {
		return userId != null && !userId.isBlank()
				&& oldPassword != null && !oldPassword.isBlank()
				&& newPassword != null && !newPassword.isBlank();
	}
}
