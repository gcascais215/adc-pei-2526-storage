package pt.unl.fct.di.adc.firstwebapp.resources;



import java.util.logging.Level;

import java.util.logging.Logger;



import jakarta.ws.rs.Consumes;

import jakarta.ws.rs.POST;

import jakarta.ws.rs.Path;

import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;

import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.core.Response.Status;



import com.google.gson.Gson;

import com.google.cloud.datastore.Datastore;

import com.google.cloud.datastore.Entity;

import com.google.cloud.datastore.Key;

import com.google.cloud.datastore.DatastoreOptions;



import pt.unl.fct.di.adc.firstwebapp.util.ApiErrorCodes;

import pt.unl.fct.di.adc.firstwebapp.util.ApiJsonResponse;

import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;

import pt.unl.fct.di.adc.firstwebapp.util.LogoutRequest;

import pt.unl.fct.di.adc.firstwebapp.util.SessionStore;



/**

 * Op10: {@code POST /rest/logout}.

 * USER/BOFFICER: {@code input.userId} must be themselves. ADMIN may target another {@code userId} (revoke all sessions).

 */

@Path("/logout")

@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")

public class LogoutResource {


	private static final String USER_KIND = "User";

	private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());

	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private static final Gson GSON = new Gson();

	public LogoutResource() {

	}

	@POST

	@Consumes(MediaType.APPLICATION_JSON)

	public Response logout(LogoutRequest body) {

		if (body == null || body.token == null || body.input == null || !body.input.valid()) {

			return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.INVALID_INPUT, ApiErrorCodes.MSG_INVALID_INPUT);

		}

		AuthToken t = body.token;

		if (t.tokenId == null || t.tokenId.isBlank()) {

			return errorResponse(Status.UNAUTHORIZED, ApiErrorCodes.INVALID_TOKEN, ApiErrorCodes.MSG_INVALID_TOKEN);

		}

		Entity callerSession = datastore.get(SessionStore.sessionKey(datastore, t.tokenId));

		if (callerSession == null) {

			return errorResponse(Status.UNAUTHORIZED, ApiErrorCodes.INVALID_TOKEN, ApiErrorCodes.MSG_INVALID_TOKEN);

		}

		long now = System.currentTimeMillis();

		if (callerSession.getLong(SessionStore.PROP_EXPIRES_AT) <= now) {

			return errorResponse(Status.UNAUTHORIZED, ApiErrorCodes.TOKEN_EXPIRED, ApiErrorCodes.MSG_TOKEN_EXPIRED);

		}

		if (!tokenPayloadMatchesSession(t, callerSession)) {

			return errorResponse(Status.UNAUTHORIZED, ApiErrorCodes.INVALID_TOKEN, ApiErrorCodes.MSG_INVALID_TOKEN);

		}



		String callerUserId = callerSession.getString(SessionStore.PROP_USER_ID);

		String callerRole = callerSession.getString(SessionStore.PROP_ROLE);

		String inputUserId = body.input.userId.trim();



		try {

			if ("ADMIN".equals(callerRole) && !inputUserId.equals(callerUserId)) {

				Key userKey = datastore.newKeyFactory().setKind(USER_KIND).newKey(inputUserId);

				Entity user = datastore.get(userKey);

				if (user == null) {

					return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.USER_NOT_FOUND, ApiErrorCodes.MSG_USER_NOT_FOUND);

				}

				if (!SessionStore.hasAnySession(datastore, inputUserId)) {

					return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.NO_SESSION, ApiErrorCodes.MSG_NO_SESSION);

				}

				SessionStore.deleteAllSessionsForUser(datastore, inputUserId);

				LOG.info("Logout: ADMIN revoked all sessions for " + inputUserId + " (by " + callerUserId + ")");

				return Response.ok(GSON.toJson(new ApiJsonResponse.LogoutSuccess())).build();

			}



			if (!"ADMIN".equals(callerRole) && !inputUserId.equals(callerUserId)) {

				return errorResponse(Status.FORBIDDEN, ApiErrorCodes.UNAUTHORIZED, ApiErrorCodes.MSG_UNAUTHORIZED);

			}



			Key targetKey = SessionStore.sessionKey(datastore, t.tokenId.trim());

			Entity targetSession = datastore.get(targetKey);

			if (targetSession == null) {

				return errorResponse(Status.UNAUTHORIZED, ApiErrorCodes.INVALID_TOKEN, ApiErrorCodes.MSG_INVALID_TOKEN);

			}



			String targetUserId = targetSession.getString(SessionStore.PROP_USER_ID);

			if (!inputUserId.equals(targetUserId)) {

				return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.INVALID_INPUT, ApiErrorCodes.MSG_INVALID_INPUT);

			}



			datastore.delete(targetKey);

			LOG.info("Logout: session " + t.tokenId.trim() + " removed for " + inputUserId);

			return Response.ok(GSON.toJson(new ApiJsonResponse.LogoutSuccess())).build();

		} catch (Exception e) {

			LOG.log(Level.SEVERE, "logout failed", e);

			return errorResponse(Status.SERVICE_UNAVAILABLE, ApiErrorCodes.DATASTORE_ERROR,

					ApiErrorCodes.MSG_DATASTORE + " (" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()) + ")");

		}

	}



	private static boolean tokenPayloadMatchesSession(AuthToken t, Entity session) {

		if (t.userId != null && !t.userId.isBlank()) {

			if (!session.getString(SessionStore.PROP_USER_ID).equals(t.userId.trim())) {

				return false;

			}

		}

		if (t.role != null && !t.role.isBlank()) {

			if (!session.getString(SessionStore.PROP_ROLE).equals(t.role.trim())) {

				return false;

			}

		}

		return true;

	}



	private static Response errorResponse(Status http, String code, String message) {

		return Response.status(http).entity(GSON.toJson(new ApiJsonResponse.ErrorBody(code, message))).build();

	}

}

