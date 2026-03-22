package pt.unl.fct.di.adc.firstwebapp.util;

import java.util.UUID;

/**
 * JSON token shape from the exercise spec:
 * tokenId, userId, role (USER | BOFFICER | ADMIN), issuedAt, expiresAt (timestamps).
 */
public class AuthToken {

	public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2; // 2h

	public String tokenId;
	public String userId;
	/** One of: USER, BOFFICER, ADMIN */
	public String role;
	public long issuedAt;
	public long expiresAt;

	public AuthToken() {
	}

	/**
	 * @param role must match the account in storage (USER, BOFFICER, or ADMIN).
	 */
	public AuthToken(String userId, String role) {
		this.tokenId = UUID.randomUUID().toString();
		this.userId = userId;
		this.role = role;
		this.issuedAt = System.currentTimeMillis();
		this.expiresAt = this.issuedAt + EXPIRATION_TIME;
	}

	/**
	 * Used when no role is stored yet; defaults to USER.
	 */
	public AuthToken(String userId) {
		this(userId, "USER");
	}

}
