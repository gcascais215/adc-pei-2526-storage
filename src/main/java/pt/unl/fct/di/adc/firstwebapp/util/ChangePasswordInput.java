package pt.unl.fct.di.adc.firstwebapp.util;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



/**

 * Op9: {@code "input": { "username", "oldPassword", "newPassword" }} .

 */

@JsonIgnoreProperties(ignoreUnknown = true)

public class ChangePasswordInput {



	public String username;

	public String oldPassword;

	public String newPassword;



	public ChangePasswordInput() {

	}



	public boolean valid() {

		return username != null && !username.isBlank()

				&& oldPassword != null && !oldPassword.isBlank()

				&& newPassword != null && !newPassword.isBlank();

	}

}

