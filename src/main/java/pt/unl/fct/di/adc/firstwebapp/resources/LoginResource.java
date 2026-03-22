package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.adc.firstwebapp.util.ApiErrorCodes;
import pt.unl.fct.di.adc.firstwebapp.util.ApiJsonResponse;
import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.LoginRequest;
import pt.unl.fct.di.adc.firstwebapp.util.SessionStore;

/**
 * Op2: {@code POST /rest/login} — body {@code { "input": { "username", "password" } }}
 */
@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final String LOG_MESSAGE_LOGIN_ATTEMP = "Login attempt by user: ";
	private static final String LOG_MESSAGE_LOGIN_SUCCESSFUL = "Login successful by user: ";
	private static final String LOG_MESSAGE_WRONG_PASSWORD = "Wrong password for: ";
	private static final String LOG_MESSAGE_UNKNOW_USER = "Failed login attempt for username: ";

	private static final String USER_PWD = "user_pwd";
	private static final String USER_ROLE = "user_role";

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final Gson GSON = new Gson();

	public LoginResource() {
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(LoginRequest body, @Context HttpServletRequest request, @Context HttpHeaders headers) {
		if (body == null || body.input == null || !body.input.valid()) {
			return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.INVALID_INPUT, ApiErrorCodes.MSG_INVALID_INPUT);
		}

		var data = body.input;
		LOG.fine(LOG_MESSAGE_LOGIN_ATTEMP + data.username);

		Key userKey = userKeyFactory.newKey(data.username);
		Key ctrsKey = datastore.newKeyFactory()
				.addAncestors(PathElement.of("User", data.username))
				.setKind("UserStats")
				.newKey("counters");
		Key logKey = datastore.allocateId(
				datastore.newKeyFactory()
						.addAncestors(PathElement.of("User", data.username))
						.setKind("UserLog").newKey());

		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if (user == null) {
				txn.rollback();
				LOG.warning(LOG_MESSAGE_UNKNOW_USER + data.username);
				return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.USER_NOT_FOUND, ApiErrorCodes.MSG_USER_NOT_FOUND);
			}

			Entity stats = txn.get(ctrsKey);
			if (stats == null) {
				stats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 0L)
						.set("user_stats_failed", 0L)
						.set("user_first_login", Timestamp.now())
						.set("user_last_login", Timestamp.now())
						.build();
			}

			String hashedPWD = user.getString(USER_PWD);
			if (!hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", stats.getLong("user_stats_logins"))
						.set("user_stats_failed", stats.getLong("user_stats_failed") + 1L)
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", stats.getTimestamp("user_last_login"))
						.set("user_last_attempt", Timestamp.now())
						.build();
				txn.put(ustats);
				txn.commit();
				LOG.warning(LOG_MESSAGE_WRONG_PASSWORD + data.username);
				return errorResponse(Status.UNAUTHORIZED, ApiErrorCodes.INVALID_CREDENTIALS,
						ApiErrorCodes.MSG_INVALID_CREDENTIALS);
			}

			String cityLatLong = headers.getHeaderString("X-AppEngine-CityLatLong");
			Entity log = Entity.newBuilder(logKey)
					.set("user_login_ip", request.getRemoteAddr())
					.set("user_login_host", request.getRemoteHost())
					.set("user_login_latlon", cityLatLong != null
							? StringValue.newBuilder(cityLatLong).setExcludeFromIndexes(true).build()
							: StringValue.newBuilder("").setExcludeFromIndexes(true).build())
					.set("user_login_city", headers.getHeaderString("X-AppEngine-City"))
					.set("user_login_country", headers.getHeaderString("X-AppEngine-Country"))
					.set("user_login_time", Timestamp.now())
					.build();

			Entity ustats = Entity.newBuilder(ctrsKey)
					.set("user_stats_logins", stats.getLong("user_stats_logins") + 1)
					.set("user_stats_failed", 0L)
					.set("user_first_login", stats.getTimestamp("user_first_login"))
					.set("user_last_login", Timestamp.now())
					.build();

			String role = "USER";
			if (user.contains(USER_ROLE)) {
				role = user.getString(USER_ROLE);
			}

			Entity activeSession = SessionStore.findActiveSession(txn, data.username);
			AuthToken token;
			if (activeSession != null) {
				token = SessionStore.toAuthToken(activeSession);
				txn.put(log, ustats);
			} else {
				token = new AuthToken(data.username, role);
				Entity sessionEntity = SessionStore.buildSessionEntity(datastore, token);
				txn.put(log, ustats, sessionEntity);
			}

			txn.commit();

			LOG.info(LOG_MESSAGE_LOGIN_SUCCESSFUL + data.username);
			ApiJsonResponse.LoginSuccess ok = new ApiJsonResponse.LoginSuccess(token);
			return Response.ok(GSON.toJson(ok)).build();
		} catch (Exception e) {
			if (txn.isActive()) {
				txn.rollback();
			}
			LOG.log(Level.SEVERE, "Login error", e);
			return errorResponse(Status.SERVICE_UNAVAILABLE, ApiErrorCodes.DATASTORE_ERROR,
					ApiErrorCodes.MSG_DATASTORE + " (" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()) + ")");
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	private static Response errorResponse(Status http, String code, String message) {
		return Response.status(http).entity(GSON.toJson(new ApiJsonResponse.ErrorBody(code, message))).build();
	}
}
