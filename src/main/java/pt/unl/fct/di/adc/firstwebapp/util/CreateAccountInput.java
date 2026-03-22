package pt.unl.fct.di.adc.firstwebapp.util;


public class CreateAccountInput {

	public String username;
	public String password;
	public String confirmation;
	public String email;
	public String phone;
	public String address;
	/** USER | BOFFICER | ADMIN */
	public String role;

	public CreateAccountInput() {
	}

	private boolean nonEmptyOrBlank(String s) {
		return s != null && !s.isBlank();
	}

	public boolean validRole() {
		if (role == null || role.isBlank()) {
			return false;
		}
		return role.equals("USER") || role.equals("BOFFICER") || role.equals("ADMIN");
	}

	
	public boolean valid() {
		if (!nonEmptyOrBlank(username) || !nonEmptyOrBlank(password) || !nonEmptyOrBlank(confirmation)) {
			return false;
		}
		if (!nonEmptyOrBlank(email) || !email.contains("@")) {
			return false;
		}
		if (!nonEmptyOrBlank(phone) || !nonEmptyOrBlank(address)) {
			return false;
		}
		if (!password.equals(confirmation)) {
			return false;
		}
		return validRole();
	}
}
