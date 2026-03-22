package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.DatastoreOptions;

import pt.unl.fct.di.adc.firstwebapp.util.ApiErrorCodes;
import pt.unl.fct.di.adc.firstwebapp.util.ApiJsonResponse;
import pt.unl.fct.di.adc.firstwebapp.util.CreateAccountInput;
import pt.unl.fct.di.adc.firstwebapp.util.CreateAccountRequest;

@Path("/createaccount")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class CreateAccountResource {

	private static final String USER_ROLE = "user_role";
	private static final String USER_PHONE = "user_phone";
	private static final String USER_ADDRESS = "user_address";

	private static final Logger LOG = Logger.getLogger(CreateAccountResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Gson GSON = new Gson();

	public CreateAccountResource() {
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createAccount(CreateAccountRequest body) {
		if (body == null || body.input == null) {
			return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.INVALID_INPUT, ApiErrorCodes.MSG_INVALID_INPUT);
		}

		CreateAccountInput in = body.input;
		LOG.fine("Attempt to create account: " + in.username);

		if (!in.valid()) {
			return errorResponse(Status.BAD_REQUEST, ApiErrorCodes.INVALID_INPUT, ApiErrorCodes.MSG_INVALID_INPUT);
		}

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(in.username);
			Entity existing = txn.get(userKey);

			if (existing != null) {
				txn.rollback();
				return errorResponse(Status.CONFLICT, ApiErrorCodes.USER_ALREADY_EXISTS,
						ApiErrorCodes.MSG_USER_ALREADY_EXISTS);
			}

			Entity user = Entity.newBuilder(userKey)
					.set("user_pwd", DigestUtils.sha512Hex(in.password))
					.set("user_email", in.email)
					.set(USER_PHONE, in.phone)
					.set(USER_ADDRESS, in.address)
					.set(USER_ROLE, in.role)
					.set("user_creation_time", Timestamp.now())
					.build();
			txn.put(user);
			txn.commit();

			LOG.info("Account created for " + in.username);
			ApiJsonResponse.CreateAccountSuccess ok = new ApiJsonResponse.CreateAccountSuccess(in.username, in.role);
			return Response.ok(GSON.toJson(ok)).build();
		} catch (Exception e) {
			if (txn.isActive()) {
				txn.rollback();
			}
			LOG.log(Level.SEVERE, "Error creating account", e);
			String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
			return errorResponse(Status.SERVICE_UNAVAILABLE, ApiErrorCodes.DATASTORE_ERROR,
					ApiErrorCodes.MSG_DATASTORE + " (" + detail + ")");
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	private static Response errorResponse(Status http, String code, String message) {
		ApiJsonResponse.ErrorBody err = new ApiJsonResponse.ErrorBody(code, message);
		return Response.status(http).entity(GSON.toJson(err)).build();
	}
}
