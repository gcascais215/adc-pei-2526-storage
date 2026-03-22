package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.DatastoreOptions;

import pt.unl.fct.di.adc.firstwebapp.util.ApiErrorCodes;
import pt.unl.fct.di.adc.firstwebapp.util.ApiJsonResponse;
import pt.unl.fct.di.adc.firstwebapp.util.ApiJsonResponse.UserSummary;
import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.SessionStore;
import pt.unl.fct.di.adc.firstwebapp.util.ShowUsersRequest;

/**
 * Op3: {@code POST /rest/showusers} — only {@code ADMIN} and {@code BOFFICER}.
 */
@Path("/showusers")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowUsersResource {

	private static final String USER_KIND = "User";
	private static final String USER_EMAIL = "user_email";
	private static final String USER_ROLE = "user_role";

	private static final Logger LOG = Logger.getLogger(ShowUsersResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Gson GSON = new Gson();

	public ShowUsersResource() {
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response showUsers(ShowUsersRequest body) {
		if (body == null || body.token == null) {
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

		String role = session.getString(SessionStore.PROP_ROLE);
		if (!"ADMIN".equals(role) && !"BOFFICER".equals(role)) {
			return errorResponse(Status.FORBIDDEN, ApiErrorCodes.UNAUTHORIZED, ApiErrorCodes.MSG_UNAUTHORIZED);
		}

		try {
			Query<Entity> query = Query.newEntityQueryBuilder().setKind(USER_KIND).build();
			QueryResults<Entity> results = datastore.run(query);
			List<UserSummary> list = new ArrayList<>();
			while (results.hasNext()) {
				Entity u = results.next();
				String username = u.getKey().getName();
				UserSummary row = new UserSummary();
				row.userId = username;
				row.username = username;
				row.email = u.contains(USER_EMAIL) ? u.getString(USER_EMAIL) : "";
				row.role = u.contains(USER_ROLE) ? u.getString(USER_ROLE) : "USER";
				list.add(row);
			}
			ApiJsonResponse.ShowUsersSuccess ok = new ApiJsonResponse.ShowUsersSuccess(list);
			return Response.ok(GSON.toJson(ok)).build();
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "showUsers failed", e);
			return errorResponse(Status.SERVICE_UNAVAILABLE, ApiErrorCodes.DATASTORE_ERROR,
					ApiErrorCodes.MSG_DATASTORE + " (" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()) + ")");
		}
	}

	private static Response errorResponse(Status http, String code, String message) {
		return Response.status(http).entity(GSON.toJson(new ApiJsonResponse.ErrorBody(code, message))).build();
	}
}
