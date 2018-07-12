package com.jcabi.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.json.JsonObject;
import javax.xml.bind.DatatypeConverter;

import com.google.common.net.HttpHeaders;
import com.jcabi.http.Request;
import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.wire.AutoRedirectingWire;

/**
 * Since there are issues with the main {@link RtGithub}
 * I've provided my own impl
 */
public class MyRtGithub implements Github {

	/**
	 * Default request to start with.
	 */
	private static final Request REQUEST =
			new ApacheRequest("https://api.github.com")
					.header(HttpHeaders.ACCEPT, "application/json")
					.header(HttpHeaders.CONTENT_TYPE, "application/json")
					.through(AutoRedirectingWire.class);

	/**
	 * REST request.
	 */
	private final transient Request request;

	/**
	 * Public ctor, for anonymous access to Github.
	 * @since 0.4
	 */
	public MyRtGithub() {
		this(MyRtGithub.REQUEST);
	}

	/**
	 * Public ctor, for HTTP Basic Authentication.
	 * @param user User name
	 * @param pwd Password
	 * @since 0.4
	 */
	public MyRtGithub(
			final String user,
			final String pwd) {
		this(
				MyRtGithub.REQUEST.header(
						HttpHeaders.AUTHORIZATION,
						String.format(
								"Basic %s",
								DatatypeConverter.printBase64Binary(
										String.format("%s:%s", user, pwd)
												.getBytes(StandardCharsets.UTF_8)
								)
						)
				)
		);
	}

	/**
	 * Public ctor, for authentication with OAuth2 token.
	 * @param token OAuth token
	 */
	public MyRtGithub(
			final String token) {
		this(
				MyRtGithub.REQUEST.header(
						HttpHeaders.AUTHORIZATION,
						String.format("token %s", token)
				)
		);
	}

	/**
	 * Public ctor, with a custom request.
	 * @param req Request to start from
	 * @since 0.4
	 */
	public MyRtGithub(
			final Request req) {
		this.request = req;
	}

	@Override
	public Request entry() {
		return this.request;
	}

	@Override
	public Repos repos() {
		return new RtRepos(this, this.request);
	}

	@Override
	public Gists gists() {
		return new RtGists(this, this.request);
	}

	@Override
	public Users users() {
		return new RtUsers(this, this.request);
	}

	@Override
	public Organizations organizations() {
		return new RtOrganizations(this, this.request);
	}

	@Override
	public Limits limits() {
		return new RtLimits(this, this.request);
	}

	@Override
	public Search search() {
		return new RtSearch(this, this.request);
	}

	@Override
	public JsonObject meta() throws IOException {
		return this.request.uri().path("meta").back().fetch()
				.as(JsonResponse.class)
				.json().readObject();
	}

	@Override
	public JsonObject emojis() throws IOException {
		return this.request.uri().path("emojis").back().fetch()
				.as(JsonResponse.class)
				.json().readObject();
	}

	@Override
	public Gitignores gitignores() throws IOException {
		return new RtGitignores(this);
	}

	@Override
	public Markdown markdown() {
		return new RtMarkdown(this, this.request);
	}

}