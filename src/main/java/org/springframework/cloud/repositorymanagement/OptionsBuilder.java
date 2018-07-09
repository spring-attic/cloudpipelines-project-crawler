package org.springframework.cloud.repositorymanagement;

/**
 * @author Marcin Grzejszczak
 * @since 0.0.1
 */
public class OptionsBuilder {
	private String username;
	private String password;
	private String token;
	private String rootUrl;

	public static OptionsBuilder builder() {
		return new OptionsBuilder();
	}

	public OptionsBuilder username(String username) {
		this.username = username;
		return this;
	}

	public OptionsBuilder password(String password) {
		this.password = password;
		return this;
	}

	public OptionsBuilder token(String token) {
		this.token = token;
		return this;
	}

	public OptionsBuilder rootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
		return this;
	}

	public Options build() {
		return new Options(this.username, this.password,
				this.token, this.rootUrl);
	}
}