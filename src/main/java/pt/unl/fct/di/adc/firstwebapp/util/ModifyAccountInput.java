package pt.unl.fct.di.adc.firstwebapp.util;

/**
 * Op5: {@code { "input": { "username", "attributes" }, "token": { ... } }} .
 */
public class ModifyAccountInput {

	public String username;
	public AccountAttributes attributes;

	public ModifyAccountInput() {
	}

	public boolean valid() {
		return username != null && !username.isBlank()
				&& attributes != null
				&& attributes.hasSomething();
	}
}
