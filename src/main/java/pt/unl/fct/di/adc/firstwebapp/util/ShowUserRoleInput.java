package pt.unl.fct.di.adc.firstwebapp.util;

/**
 * Op7: {@code "input": { "userId": "..." }} .
 */
public class ShowUserRoleInput {

	public String userId;

	public ShowUserRoleInput() {
	}

	public boolean valid() {
		return userId != null && !userId.isBlank();
	}
}
