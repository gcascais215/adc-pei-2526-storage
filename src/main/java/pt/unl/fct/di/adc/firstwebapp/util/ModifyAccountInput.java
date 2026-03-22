package pt.unl.fct.di.adc.firstwebapp.util;

/**
 * Op5: {@code { "input": { "userId", "attributes" }, "token": { ... } }} .
 */
public class ModifyAccountInput {

	public String userId;
	public AccountAttributes attributes;

	public ModifyAccountInput() {
	}

	public boolean valid() {
		return userId != null && !userId.isBlank()
				&& attributes != null
				&& attributes.hasSomething();
	}
}
