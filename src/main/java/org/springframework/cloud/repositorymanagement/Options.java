package org.springframework.cloud.repositorymanagement;

/**
 * Contains options required to connect to a password
 * management
 *
 * @author Marcin Grzejszczak
 * @since 0.0.1
 */
public class Options {
	public final String username, password, token, rootUrl;

	Options(String username, String password, String token, String rootUrl) {
		this.username = username;
		this.password = password;
		this.token = token;
		this.rootUrl = rootUrl;
	}
}
