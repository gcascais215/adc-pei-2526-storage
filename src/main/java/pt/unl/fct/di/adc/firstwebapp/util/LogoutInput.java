package pt.unl.fct.di.adc.firstwebapp.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Op10: apenas {@code userId} — conta cujo logout se pretende (própria ou, se ADMIN, outra).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogoutInput {

	public String userId;

	public LogoutInput() {
	}

	public boolean valid() {
		return userId != null && !userId.isBlank();
	}
}
