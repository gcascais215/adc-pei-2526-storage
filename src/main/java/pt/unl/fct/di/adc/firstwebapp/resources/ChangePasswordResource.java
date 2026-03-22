package pt.unl.fct.di.adc.firstwebapp.resources;



import java.util.logging.Level;

import java.util.logging.Logger;



import org.apache.commons.codec.digest.DigestUtils;



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

import pt.unl.fct.di.adc.firstwebapp.util.ChangePasswordRequest;

import pt.unl.fct.di.adc.firstwebapp.util.SessionStore;



/**

 * Op9: {@code POST /rest/changeuserpassword} — authenticated caller; {@code input.userId} is the account

 * whose password is updated after {@code oldPassword} matches the stored hash.

 */

@Path("/changeuserpwd")

@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")

public class ChangePasswordResource {



	private static final String USER_KIND = "User";

	private static final String USER_PWD = "user_pwd";



	private static final Logger LOG = Logger.getLogger(ChangePasswordResource.class.getName());

	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private static final Gson GSON = new Gson();



	public ChangePasswordResource() {

	}



	@POST

	@Consumes(MediaType.APPLICATION_JSON)

	public Response changePassword(ChangePasswordRequest body) {

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



		String targetId = body.input.userId.trim();

		Key userKey = datastore.newKeyFactory().setKind(USER_KIND).newKey(targetId);

		String newHash = DigestUtils.sha512Hex(body.input.newPassword);

		String oldHash = DigestUtils.sha512Hex(body.input.oldPassword);



		try {

			Entity user = datastore.get(userKey);

			if (user == null) {

				return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.USER_NOT_FOUND, ApiErrorCodes.MSG_USER_NOT_FOUND);

			}



			String stored = user.getString(USER_PWD);

			if (!stored.equals(oldHash)) {

				return errorResponse(Status.UNAUTHORIZED, ApiErrorCodes.INVALID_CREDENTIALS,

						ApiErrorCodes.MSG_INVALID_CREDENTIALS);

			}



			Entity updated = Entity.newBuilder(user).set(USER_PWD, newHash).build();

			datastore.put(updated);



			LOG.info("Password changed for " + targetId + " by " + session.getString(SessionStore.PROP_USER_ID));

			return Response.ok(GSON.toJson(new ApiJsonResponse.ChangePasswordSuccess(targetId))).build();

		} catch (Exception e) {

			LOG.log(Level.SEVERE, "changePassword failed", e);

			return errorResponse(Status.SERVICE_UNAVAILABLE, ApiErrorCodes.DATASTORE_ERROR,

					ApiErrorCodes.MSG_DATASTORE + " (" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()) + ")");

		}

	}



	private static Response errorResponse(Status http, String code, String message) {

		return Response.status(http).entity(GSON.toJson(new ApiJsonResponse.ErrorBody(code, message))).build();

	}

}

