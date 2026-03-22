package pt.unl.fct.di.adc.firstwebapp.util;


public final class ApiErrorCodes {

	public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
	public static final String INVALID_INPUT = "INVALID_INPUT";

	/** Op2 Login  */
	public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
	public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

	public static final String MSG_INVALID_CREDENTIALS =
			"The username-password pair is not valid";
	public static final String MSG_USER_NOT_FOUND =
			"The username referred in the operation doesn't exist in registered accounts";

	public static final String MSG_USER_ALREADY_EXISTS =
			"Error in creating an account because the username already exists";
	public static final String MSG_INVALID_INPUT =
			"The call is using input data not following the correct specification";

	/** Used when persistence fails (e.g. Datastore credentials / project / emulator). */
	public static final String DATASTORE_ERROR = "DATASTORE_ERROR";
	public static final String MSG_DATASTORE =
			"Could not save to the database. For local runs: gcloud auth application-default login, "
					+ "set GOOGLE_CLOUD_PROJECT to your GCP project id, or use the Datastore emulator.";

	/** Op3+ authenticated operations */
	public static final String INVALID_TOKEN = "INVALID_TOKEN";
	public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
	public static final String UNAUTHORIZED = "UNAUTHORIZED";
	public static final String FORBIDDEN = "FORBIDDEN";

	public static final String MSG_INVALID_TOKEN =
			"The operation is called with an invalid token (wrong format for example)";
	public static final String MSG_TOKEN_EXPIRED = "The operation is called with a token that is expired";
	public static final String MSG_UNAUTHORIZED = "The operation is not allowed for the user role";
	public static final String MSG_FORBIDDEN = "The operation generated a forbidden error by other reason";

	/** Op10 Logout — target user has no {@code Session} rows */
	public static final String NO_SESSION = "NO_SESSION";
	public static final String MSG_NO_SESSION = "There is no session for this user";

	private ApiErrorCodes() {
	}
}
