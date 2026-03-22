package pt.unl.fct.di.adc.firstwebapp.util;


public class ApiJsonResponse {

	public static class ErrorBody {
		public String status;
		public String data;

		public ErrorBody(String status, String data) {
			this.status = status;
			this.data = data;
		}
	}

	public static class CreateAccountSuccess {
		public String status = "success";
		public CreateAccountSuccessData data;

		public CreateAccountSuccess(String username, String role) {
			this.data = new CreateAccountSuccessData();
			this.data.username = username;
			this.data.role = role;
		}
	}

	public static class CreateAccountSuccessData {
		public String username;
		public String role;
	}

	/**
	 * Op2 Login success 
	 */
	public static class LoginSuccess {
		public String status = "success";
		public LoginSuccessData data;

		public LoginSuccess(AuthToken token) {
			this.data = new LoginSuccessData();
			this.data.token = token;
		}
	}

	public static class LoginSuccessData {
		public AuthToken token;
	}

	/** Op3 ShowUsers success  */
	public static class ShowUsersSuccess {
		public String status = "success";
		public ShowUsersData data;

		public ShowUsersSuccess(java.util.List<UserSummary> users) {
			this.data = new ShowUsersData();
			this.data.users = users;
		}
	}

	public static class ShowUsersData {
		public java.util.List<UserSummary> users;
	}

	public static class UserSummary {
		public String userId;
		public String username;
		public String email;
		public String role;
	}

	/** Op4 DeleteAccount success  */
	public static class DeleteAccountSuccess {
		public String status = "success";
		public DeleteAccountSuccessData data = new DeleteAccountSuccessData();
	}

	public static class DeleteAccountSuccessData {
		public String message = "Account deleted successfully";
	}

	/** Op5 ModifyAccountAttributes success  */
	public static class ModifyAccountSuccess {
		public String status = "success";
		public ModifyAccountSuccessData data = new ModifyAccountSuccessData();
	}

	public static class ModifyAccountSuccessData {
		public String message = "Updated successfully";
	}

	/** Op6 ShowAuthenticatedSessions success  */
	public static class ShowAuthenticatedSessionsSuccess {
		public String status = "success";
		public ShowAuthenticatedSessionsData data;

		public ShowAuthenticatedSessionsSuccess(java.util.List<AuthenticatedSessionRow> sessions) {
			this.data = new ShowAuthenticatedSessionsData();
			this.data.sessions = sessions;
		}
	}

	public static class ShowAuthenticatedSessionsData {
		public java.util.List<AuthenticatedSessionRow> sessions;
	}

	/** One stored {@link pt.unl.fct.di.adc.firstwebapp.util.SessionStore#KIND} row. */
	public static class AuthenticatedSessionRow {
		public String tokenId;
		public String userId;
		public String role;
		public long issuedAt;
		public long expiresAt;
	}

	/** Op7 ShowUserRole success  */
	public static class ShowUserRoleSuccess {
		public String status = "success";
		public ShowUserRoleSuccessData data;

		public ShowUserRoleSuccess(String userId, String role) {
			this.data = new ShowUserRoleSuccessData();
			this.data.userId = userId;
			this.data.role = role;
		}
	}

	public static class ShowUserRoleSuccessData {
		public String userId;
		public String role;
	}

	/** Op8 ChangeUserRole success  */
	public static class ChangeUserRoleSuccess {
		public String status = "success";
		public ChangeUserRoleSuccessData data;

		public ChangeUserRoleSuccess(String userId, String role) {
			this.data = new ChangeUserRoleSuccessData();
			this.data.userId = userId;
			this.data.role = role;
		}
	}

	public static class ChangeUserRoleSuccessData {
		public String userId;
		public String role;
	}

	/** Op9 ChangePassword success  */
	public static class ChangePasswordSuccess {
		public String status = "success";
		public ChangePasswordSuccessData data;

		public ChangePasswordSuccess(String userId) {
			this.data = new ChangePasswordSuccessData();
			this.data.userId = userId;
			this.data.message = "Password updated successfully";
		}
	}

	public static class ChangePasswordSuccessData {
		public String userId;
		public String message;
	}

	/** Op10 Logout success  */
	public static class LogoutSuccess {
		public String status = "success";
		public LogoutSuccessData data = new LogoutSuccessData();
	}

	public static class LogoutSuccessData {
		public String message = "Logged out successfully";
	}
}
