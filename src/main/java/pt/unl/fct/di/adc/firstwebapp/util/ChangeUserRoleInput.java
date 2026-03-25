package pt.unl.fct.di.adc.firstwebapp.util;



/**

 * Op8: {@code "input": { "username", "newRole" }} — {@code newRole} is the new value (USER | BOFFICER | ADMIN).

 */

public class ChangeUserRoleInput {



	public String username;

	public String newRole;



	public ChangeUserRoleInput() {

	}



	public boolean valid() {

		return username != null && !username.isBlank() && validRole(newRole);

	}



	private static boolean validRole(String r) {

		if (r == null || r.isBlank()) {

			return false;

		}

		return "USER".equals(r) || "BOFFICER".equals(r) || "ADMIN".equals(r);

	}

}

