package pt.unl.fct.di.adc.firstwebapp.util;

/**
 * Op4: {@code "input": { "userId": "..." }} .
 */
public class DeleteAccountInput {

	public String userId;

	public DeleteAccountInput() {
	}

	public boolean valid() {
		return userId != null && !userId.isBlank();
	}
}
