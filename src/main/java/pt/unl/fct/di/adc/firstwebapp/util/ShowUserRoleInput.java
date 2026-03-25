package pt.unl.fct.di.adc.firstwebapp.util;

/**
 * Op7: {@code "input": { "username": "..." }} .
 */
public class ShowUserRoleInput {

	public String username;

	public ShowUserRoleInput() {
	}

	public boolean valid() {
		return username != null && !username.isBlank();
	}
}
