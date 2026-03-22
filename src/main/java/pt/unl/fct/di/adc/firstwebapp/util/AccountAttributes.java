package pt.unl.fct.di.adc.firstwebapp.util;



/**

 * Op5 {@code input.attributes} — username is only echoed for validation (cannot rename the account key).

 */

public class AccountAttributes {



	public String email;

	public String phone;

	public String address;

	/** If present, must equal the account id (key name); a different value is {@code INVALID_INPUT}. */

	public String username;

	/**
	 * Role is not modifiable in Op5 (use the dedicated change-role operation). If present and non-blank,
	 * the request is rejected with {@code INVALID_INPUT}.
	 */
	public String role;

	public AccountAttributes() {
	}

	/** At least one field that can trigger an update (besides redundant username echo). */

	public boolean hasSomething() {

		return (email != null && !email.isBlank())

				|| (phone != null && !phone.isBlank())

				|| (address != null && !address.isBlank());

	}

}

