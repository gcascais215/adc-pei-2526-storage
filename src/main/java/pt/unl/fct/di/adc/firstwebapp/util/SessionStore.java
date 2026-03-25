package pt.unl.fct.di.adc.firstwebapp.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;

public final class SessionStore {

	public static final String KIND = "Session";

	public static final String PROP_USER_ID = "user_id";
	public static final String PROP_ROLE = "role";
	public static final String PROP_ISSUED_AT = "issued_at";
	public static final String PROP_EXPIRES_AT = "expires_at";

	private SessionStore() {
	}

	public static Key sessionKey(Datastore datastore, String tokenId) {
		return datastore.newKeyFactory().setKind(KIND).newKey(tokenId);
	}

	public static Entity buildSessionEntity(Datastore datastore, AuthToken token) {
		Key key = sessionKey(datastore, token.tokenId);
		return Entity.newBuilder(key)
				.set(PROP_USER_ID, token.username)
				.set(PROP_ROLE, token.role)
				.set(PROP_ISSUED_AT, token.issuedAt)
				.set(PROP_EXPIRES_AT, token.expiresAt)
				.build();
	}

	/**
	 * Returns a non-expired session for this user, if any.
	 * Uses only equality on {@link #PROP_USER_ID} (automatic index) and filters {@code expires_at} in memory
	 * so no composite index is required.
	 */
	public static Entity findActiveSession(Transaction txn, String userId) {
		long now = System.currentTimeMillis();
		Query<Entity> q = Query.newEntityQueryBuilder()
				.setKind(KIND)
				.setFilter(PropertyFilter.eq(PROP_USER_ID, userId))
				.build();
		QueryResults<Entity> r = txn.run(q);
		Entity best = null;
		long bestExpires = Long.MIN_VALUE;
		while (r.hasNext()) {
			Entity e = r.next();
			long exp = e.getLong(PROP_EXPIRES_AT);
			if (exp > now && exp >= bestExpires) {
				best = e;
				bestExpires = exp;
			}
		}
		return best;
	}

	/**
	 * All {@link #KIND} rows for this user (active and expired), newest {@link #PROP_ISSUED_AT} first.
	 */
	public static List<Entity> listSessionsForUser(Datastore datastore, String userId) {
		Query<Entity> q = Query.newEntityQueryBuilder()
				.setKind(KIND)
				.setFilter(PropertyFilter.eq(PROP_USER_ID, userId))
				.build();
		QueryResults<Entity> r = datastore.run(q);
		List<Entity> list = new ArrayList<>();
		while (r.hasNext()) {
			list.add(r.next());
		}
		list.sort(Comparator.comparingLong((Entity e) -> e.getLong(PROP_ISSUED_AT)).reversed());
		return list;
	}

	/** {@code true} if at least one {@link #KIND} row exists for this {@link #PROP_USER_ID}. */
	public static boolean hasAnySession(Datastore datastore, String userId) {
		Query<Entity> q = Query.newEntityQueryBuilder()
				.setKind(KIND)
				.setFilter(PropertyFilter.eq(PROP_USER_ID, userId))
				.setLimit(1)
				.build();
		return datastore.run(q).hasNext();
	}

	/**
	 * Every {@link #KIND} row in the database (all users), newest {@link #PROP_ISSUED_AT} first.
	 */
	public static List<Entity> listAllSessions(Datastore datastore) {
		Query<Entity> q = Query.newEntityQueryBuilder().setKind(KIND).build();
		QueryResults<Entity> r = datastore.run(q);
		List<Entity> list = new ArrayList<>();
		while (r.hasNext()) {
			list.add(r.next());
		}
		list.sort(Comparator.comparingLong((Entity e) -> e.getLong(PROP_ISSUED_AT)).reversed());
		return list;
	}

	/** Rebuilds the token returned to the client from a stored {@link Entity}. */
	public static AuthToken toAuthToken(Entity session) {
		AuthToken t = new AuthToken();
		t.tokenId = session.getKey().getName();
		t.username = session.getString(PROP_USER_ID);
		t.role = session.getString(PROP_ROLE);
		t.issuedAt = session.getLong(PROP_ISSUED_AT);
		t.expiresAt = session.getLong(PROP_EXPIRES_AT);
		return t;
	}

	/**
	 * Sets {@link #PROP_ROLE} on every {@link #KIND} row for this user (active or expired).
	 * Call from “change role” (Op8) so tokens already issued stay aligned with the account.
	 */
	public static void updateRoleForAllSessionsOfUser(Datastore datastore, String userId, String newRole) {
		Query<Entity> q = Query.newEntityQueryBuilder()
				.setKind(KIND)
				.setFilter(PropertyFilter.eq(PROP_USER_ID, userId))
				.build();
		QueryResults<Entity> r = datastore.run(q);
		List<Entity> updated = new ArrayList<>();
		while (r.hasNext()) {
			Entity e = r.next();
			updated.add(Entity.newBuilder(e).set(PROP_ROLE, newRole).build());
		}
		if (!updated.isEmpty()) {
			datastore.put(updated.toArray(new Entity[0]));
		}
	}

	/** Removes every {@link #KIND} row for this user (e.g. before deleting the account). */
	public static void deleteAllSessionsForUser(Datastore datastore, String userId) {
		Query<Entity> q = Query.newEntityQueryBuilder()
				.setKind(KIND)
				.setFilter(PropertyFilter.eq(PROP_USER_ID, userId))
				.build();
		QueryResults<Entity> r = datastore.run(q);
		while (r.hasNext()) {
			datastore.delete(r.next().getKey());
		}
	}
}
