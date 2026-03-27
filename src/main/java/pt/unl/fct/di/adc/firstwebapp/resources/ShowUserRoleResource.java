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

import pt.unl.fct.di.adc.firstwebapp.util.SessionStore;

import pt.unl.fct.di.adc.firstwebapp.util.ShowUserRoleRequest;



/**

 * Op7: {@code POST /rest/showuserrole} — only {@code ADMIN} and {@code BOFFICER}.

 */

@Path("/showuserrole")

@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")

public class ShowUserRoleResource {

	private static final String USER_KIND = "User";
	private static final String USER_ROLE = "user_role";

	private static final Logger LOG = Logger.getLogger(ShowUserRoleResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Gson GSON = new Gson();

	public ShowUserRoleResource() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response showUserRole(ShowUserRoleRequest body) {

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

		String callerRole = session.getString(SessionStore.PROP_ROLE);

		if (!"ADMIN".equals(callerRole) && !"BOFFICER".equals(callerRole)) {
			return errorResponse(Status.FORBIDDEN, ApiErrorCodes.UNAUTHORIZED, ApiErrorCodes.MSG_UNAUTHORIZED);
		}

		String targetId = body.input.username.trim();
		Key userKey = datastore.newKeyFactory().setKind(USER_KIND).newKey(targetId);

		try {
			Entity user = datastore.get(userKey);

			if (user == null) {
				return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.USER_NOT_FOUND, ApiErrorCodes.MSG_USER_NOT_FOUND);
			}

			String role = user.contains(USER_ROLE) ? user.getString(USER_ROLE) : "USER";
			ApiJsonResponse.ShowUserRoleSuccess ok = new ApiJsonResponse.ShowUserRoleSuccess(targetId, role);
			return Response.ok(GSON.toJson(ok)).build();

		} catch (Exception e) {
			LOG.log(Level.SEVERE, "showUserRole failed", e);
			return errorResponse(Status.SERVICE_UNAVAILABLE, ApiErrorCodes.DATASTORE_ERROR,
					ApiErrorCodes.MSG_DATASTORE + " (" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()) + ")");
		}

	}


	private static Response errorResponse(Status http, String code, String message) {
		return Response.status(http).entity(GSON.toJson(new ApiJsonResponse.ErrorBody(code, message))).build();
	}

}

