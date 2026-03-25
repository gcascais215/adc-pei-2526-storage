package pt.unl.fct.di.adc.firstwebapp.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Op10: apenas {@code username} — conta cujo logout se pretende (própria ou, se ADMIN, outra).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogoutInput {

	public String username;

	public LogoutInput() {
	}

	public boolean valid() {
		return username != null && !username.isBlank();
	}
}
