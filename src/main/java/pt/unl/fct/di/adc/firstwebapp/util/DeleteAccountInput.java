package pt.unl.fct.di.adc.firstwebapp.util;

/**
 * Op4: {@code "input": { "username": "..." }} .
 */
public class DeleteAccountInput {

	public String username;

	public DeleteAccountInput() {
	}

	public boolean valid() {
		return username != null && !username.isBlank();
	}
}
