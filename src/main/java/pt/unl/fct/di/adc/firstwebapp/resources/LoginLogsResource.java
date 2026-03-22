package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
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
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import pt.unl.fct.di.adc.firstwebapp.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginLogsResource {

	private static final String MESSAGE_INVALID_CREDENTIALS = "Incorrect username or password.";
	private static final String MESSAGE_NEXT_PARAMETER_INVALID = "Request parameter 'next' must be greater or equal to 0.";
	private static final String USER_PWD = "user_pwd";
	private static final String USER_LOGIN_TIME = "user_login_time";

	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private final Gson g = new Gson();

	public LoginLogsResource() {
	}

	@POST
	@Path("user/login-logs/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLatestLogins(LoginData data) {
		Key userKey = userKeyFactory.newKey(data.username);
		Entity user = datastore.get(userKey);
		if (user != null && user.getString(USER_PWD).equals(DigestUtils.sha512Hex(data.password))) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1);
			Timestamp yesterday = Timestamp.of(cal.getTime());

			Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("UserLog")
					.setFilter(
							CompositeFilter.and(
									PropertyFilter.hasAncestor(
											datastore.newKeyFactory().setKind("User").newKey(data.username)),
									PropertyFilter.ge(USER_LOGIN_TIME, yesterday)))
					.setOrderBy(OrderBy.desc(USER_LOGIN_TIME))
					.setLimit(3)
					.build();
			QueryResults<Entity> logs = datastore.run(query);

			List<Date> loginDates = new ArrayList<>();
			logs.forEachRemaining(userlog -> loginDates.add(userlog.getTimestamp(USER_LOGIN_TIME).toDate()));

			return Response.ok(g.toJson(loginDates)).build();
		}
		return Response.status(Status.FORBIDDEN).entity(MESSAGE_INVALID_CREDENTIALS).build();
	}

	@POST
	@Path("user/login-logs/pagination")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLatestLoginsPaginated(@QueryParam("next") String nextParam, LoginData data) {
		int next;
		try {
			next = Integer.parseInt(nextParam);
			if (next < 0) {
				return Response.status(Status.BAD_REQUEST).entity(MESSAGE_NEXT_PARAMETER_INVALID).build();
			}
		} catch (NumberFormatException e) {
			return Response.status(Status.BAD_REQUEST).entity(MESSAGE_NEXT_PARAMETER_INVALID).build();
		}

		Key userKey = userKeyFactory.newKey(data.username);
		Entity user = datastore.get(userKey);
		if (user != null && user.getString(USER_PWD).equals(DigestUtils.sha512Hex(data.password))) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1);
			Timestamp yesterday = Timestamp.of(cal.getTime());

			Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("UserLog")
					.setFilter(
							CompositeFilter.and(
									PropertyFilter.hasAncestor(
											datastore.newKeyFactory().setKind("User").newKey(data.username)),
									PropertyFilter.ge(USER_LOGIN_TIME, yesterday)))
					.setOrderBy(OrderBy.desc(USER_LOGIN_TIME))
					.setLimit(3)
					.setOffset(next)
					.build();
			QueryResults<Entity> logs = datastore.run(query);

			List<Date> loginDates = new ArrayList<>();
			logs.forEachRemaining(userlog -> loginDates.add(userlog.getTimestamp(USER_LOGIN_TIME).toDate()));

			return Response.ok(g.toJson(loginDates)).build();
		}
		return Response.status(Status.FORBIDDEN).entity(MESSAGE_INVALID_CREDENTIALS).build();
	}
}
