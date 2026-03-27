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



import pt.unl.fct.di.adc.firstwebapp.util.AccountAttributes;

import pt.unl.fct.di.adc.firstwebapp.util.ApiErrorCodes;

import pt.unl.fct.di.adc.firstwebapp.util.ApiJsonResponse;

import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;

import pt.unl.fct.di.adc.firstwebapp.util.ModifyAccountInput;

import pt.unl.fct.di.adc.firstwebapp.util.ModifyAccountRequest;

import pt.unl.fct.di.adc.firstwebapp.util.SessionStore;



/**

 * Op5: {@code POST /rest/modaccount} — modify account attributes (username / key is immutable).

 */

@Path("/modaccount")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyAccountResource {

	private static final String USER_KIND = "User";
	private static final String USER_PHONE = "user_phone";
	private static final String USER_ADDRESS = "user_address";
	private static final String USER_ROLE = "user_role";

	private static final Logger LOG = Logger.getLogger(ModifyAccountResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Gson GSON = new Gson();

	public ModifyAccountResource() {
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyAccount(ModifyAccountRequest body) {

		if (body == null || body.input == null || !body.input.valid() || body.token == null) {
			return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.INVALID_INPUT, ApiErrorCodes.MSG_INVALID_INPUT);
		}

		AuthToken t = body.token;

		if (t.tokenId == null || t.tokenId.isBlank()) {
			return errorResponse(Status.UNAUTHORIZED, ApiErrorCodes.INVALID_TOKEN, ApiErrorCodes.MSG_INVALID_TOKEN);
		}

		Entity session = datastore.get(SessionStore.sessionKey(datastore, t.tokenId));

		if (session == null) {
			return errorResponse(Status.UNAUTHORIZED, ApiErrorCodes.INVALID_TOKEN, ApiErrorCodes.MSG_INVALID_TOKEN);
		}

		long now = System.currentTimeMillis();

		if (session.getLong(SessionStore.PROP_EXPIRES_AT) <= now) {
			return errorResponse(Status.UNAUTHORIZED, ApiErrorCodes.TOKEN_EXPIRED, ApiErrorCodes.MSG_TOKEN_EXPIRED);
		}

		String callerId = session.getString(SessionStore.PROP_USER_ID);
		String callerRole = session.getString(SessionStore.PROP_ROLE);

		ModifyAccountInput in = body.input;
		String targetId = in.username.trim();
		AccountAttributes attrs = in.attributes;

		if (attrs.username != null && !attrs.username.isBlank() && !attrs.username.equals(targetId)) {
			return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.INVALID_INPUT, ApiErrorCodes.MSG_INVALID_INPUT);
		}

		if (attrs.role != null && !attrs.role.isBlank()) {
			return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.INVALID_INPUT, ApiErrorCodes.MSG_INVALID_INPUT);
		}

		Key userKey = datastore.newKeyFactory().setKind(USER_KIND).newKey(targetId);

		try {
			Entity user = datastore.get(userKey);
			if (user == null) {
				return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.USER_NOT_FOUND, ApiErrorCodes.MSG_USER_NOT_FOUND);
			}

			String targetRole = user.contains(USER_ROLE) ? user.getString(USER_ROLE) : "USER";

			if (!canModifyAccount(callerId, callerRole, targetId, targetRole)) {
				return errorResponse(Status.FORBIDDEN, ApiErrorCodes.UNAUTHORIZED, ApiErrorCodes.MSG_UNAUTHORIZED);
			}

			Entity.Builder b = Entity.newBuilder(user);
			boolean changed = false;

			if (attrs.phone != null && !attrs.phone.isBlank()) {
				b.set(USER_PHONE, attrs.phone.trim());
				changed = true;
			}

			if (attrs.address != null && !attrs.address.isBlank()) {
				b.set(USER_ADDRESS, attrs.address.trim());
				changed = true;
			}

			if (!changed) {
				return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.INVALID_INPUT, ApiErrorCodes.MSG_INVALID_INPUT);
			}

			Entity updated = b.build();
			datastore.put(updated);
			LOG.info("Account updated: " + targetId + " by " + callerId);
			return Response.ok(GSON.toJson(new ApiJsonResponse.ModifyAccountSuccess())).build();

		} catch (Exception e) {
			LOG.log(Level.SEVERE, "modifyAccount failed", e);
			return errorResponse(Status.SERVICE_UNAVAILABLE, ApiErrorCodes.DATASTORE_ERROR,
					ApiErrorCodes.MSG_DATASTORE + " (" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()) + ")");
		}
	}

	/**
	 * USER: own account only.
	 * BOFFICER: own account, or any account whose role is USER.
	 * ADMIN: any account.
	 */

	private static boolean canModifyAccount(String callerId, String callerRole, String targetId, String targetRole) {

		if ("ADMIN".equals(callerRole)) {
			return true;
		}

		if (callerId.equals(targetId)) {
			return true;
		}

		if ("USER".equals(callerRole)) {
			return false;
		}

		if ("BOFFICER".equals(callerRole)) {
			return "USER".equals(targetRole);
		}

		return false;
	}

	private static Response errorResponse(Status http, String code, String message) {
		return Response.status(http).entity(GSON.toJson(new ApiJsonResponse.ErrorBody(code, message))).build();
	}

}

