package pt.unl.fct.di.adc.firstwebapp.util;

/**
 * Fields inside {@code "input"} for Op2 Login 
 */
public class LoginInput {

	public String username;
	public String password;

	public LoginInput() {
	}

	public boolean valid() {
		return username != null && !username.isBlank()
				&& password != null && !password.isBlank();
	}
}
