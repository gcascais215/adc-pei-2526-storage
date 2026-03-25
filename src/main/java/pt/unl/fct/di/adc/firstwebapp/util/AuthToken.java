package pt.unl.fct.di.adc.firstwebapp.util;

import java.util.UUID;

/**
 * JSON token shape from the exercise spec:
 * tokenId, username, role (USER | BOFFICER | ADMIN), issuedAt, expiresAt (timestamps).
 */
public class AuthToken {

	public static final long EXPIRATION_TIME = 1000 * 60 * 15; // 15min

	public String tokenId;
	public String username;
	/** One of: USER, BOFFICER, ADMIN */
	public String role;
	public long issuedAt;
	public long expiresAt;

	public AuthToken() {
	}

	/**
	 * @param role must match the account in storage (USER, BOFFICER, or ADMIN).
	 */
	public AuthToken(String username, String role) {
		this.tokenId = UUID.randomUUID().toString();
		this.username = username;
		this.role = role;
		this.issuedAt = System.currentTimeMillis();
		this.expiresAt = this.issuedAt + EXPIRATION_TIME;
	}

	/**
	 * Used when no role is stored yet; defaults to USER.
	 */
	public AuthToken(String username) {
		this(username, "USER");
	}

}
