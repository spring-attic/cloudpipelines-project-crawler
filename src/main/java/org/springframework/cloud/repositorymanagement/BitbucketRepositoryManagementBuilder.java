package org.springframework.cloud.repositorymanagement;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marcin Grzejszczak
 */
class BitbucketRepositoryManagementBuilder implements RepositoryManagementBuilder {

	private static final Logger log = LoggerFactory.getLogger(GitlabRepositoryManagementBuilder.class);

	@Override public RepositoryManagement build(Options options) {
		boolean applicable = isApplicable(options.rootUrl);
		if (applicable) {
			return createNewRepoManagement(options);
		}
		if (options.repository != Repositories.BITBUCKET) {
			return null;
		}
		return createNewRepoManagement(options);
	}

	RepositoryManagement createNewRepoManagement(Options options) {
		return new BitbucketRepositoryManagement(options);
	}

	private boolean isApplicable(String url) {
		boolean applicable = StringUtils.isNotBlank(url) && url.contains("bitbucket");
		if (log.isDebugEnabled()) {
			log.debug("URL [{}] is applicable [{}]", url, applicable);
		}
		return applicable;
	}
}

class BitbucketRepositoryManagement implements RepositoryManagement {

	private static final Logger log = LoggerFactory.getLogger(BitbucketRepositoryManagement.class);

	private final OkHttpClient client;
	private final Options options;
	private final ObjectMapper objectMapper = new ObjectMapper();

	BitbucketRepositoryManagement(Options options) {
		this.client = connect(options);
		this.options = options;
	}

	private OkHttpClient connect(Options options) {
		if (StringUtils.isNotBlank(options.token)) {
			return new OkHttpClient.Builder()
					.followRedirects(true)
					.followSslRedirects(true)
					.authenticator((route, response) -> {
						if (response.request().header("Authorization") != null) {
							return null; // Give up, we've already attempted to authenticate.
						}
						if (log.isDebugEnabled()) {
							log.debug("Authenticating for response: " + response);
							log.debug("Challenges: " + response.challenges());
						}
						return response.request().newBuilder()
								.header("Authorization Bearer", options.token)
								.build();
					})
					.build();
		} else if (StringUtils.isNotBlank(options.username)) {
			return new OkHttpClient.Builder()
					.followRedirects(true)
					.followSslRedirects(true)
					.authenticator((route, response) -> {
						if (response.request().header("Authorization") != null) {
							return null; // Give up, we've already attempted to authenticate.
						}
						if (log.isDebugEnabled()) {
							log.debug("Authenticating for response: " + response);
							log.debug("Challenges: " + response.challenges());
						}
						String credential = Credentials.basic(options.username, options.password);
						return response.request().newBuilder()
								.header("Authorization", credential)
								.build();
					})
					.build();
		}
		throw new IllegalStateException("Neither token, nor username and password passed");
	}

	@Override public List<Repository> repositories(String org) {
		try {
			Response execute = callRepositories(org);
			ResponseBody body = execute.body();
			if (execute.code() >= 400) {
				throw new IllegalStateException("Status code [" + execute.code() + "] and body [" + body
						+ "]");
			}
			String response = body != null ? body.string() : "";
			Map map = this.objectMapper.readValue(response, Map.class);
			List<Repository> repositories = allNonFilteredOutProjects((List<Map>) map.get("values"));
			return addManuallySetProjects(org, repositories);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	Response callRepositories(String org) throws IOException {
		return this.client.newCall(
				new Request.Builder().get().url(rootUrl() + "repositories/" + org)
						.build()).execute();
	}

	private String rootUrl() {
		String url = this.options.rootUrl.endsWith("/") ? this.options.rootUrl : this.options.rootUrl + "/";
		// we support only version 2.0 of the API
		return url + "2.0/";
	}

	private List<Repository> allNonFilteredOutProjects(List<Map> map) {
		return map.stream()
						.map(entry -> new Repository(
								options.projectName((String) entry.get("name")),
								url(entry, "ssh"),
								url(entry, "https"),
								"master"))
						.filter(repo -> !options.isIgnored(repo.name))
						.collect(Collectors.toList());
	}

	private String url(Map project, String name) {
		Map links = (Map) project.get("links");
		List<Map<String, String>> clone = (List<Map<String, String>>) links.get("clone");
		List<String> strings = clone.stream()
				.filter(map -> name.equals(map.get("name")))
				.map(map -> map.get("href"))
				.collect(Collectors.toList());
		return strings.get(0);
	}

	private List<Repository> addManuallySetProjects(String org, List<Repository> repositories) {
		repositories.addAll(this.options.projects
				.stream().map(pb -> new Repository(options.projectName(pb.projectName),
				sshKey(org, pb), cloneUrl(org, pb), pb.branch))
				.collect(Collectors.toSet()));
		return repositories;
	}

	private String sshKey(String org, ProjectAndBranch pb) {
		return "git@" + host() + ":" + org + "/" + pb.project + ".git";
	}

	private String host() {
		return URI.create(this.options.rootUrl).getHost();
	}

	private String cloneUrl(String org, ProjectAndBranch pb) {
		return "https://" + host() + "/" + org + "/" + pb.project + ".git";
	}

	@Override public String fileContent(String org, String repo,
			String branch, String filePath) {
		return getDescriptor(org, repo, branch, filePath);
	}

	String getDescriptor(String org, String repo, String branch,
			String filePath) {
		try {
			return this.client
					.newCall(new Request.Builder()
							.url(rootUrl() + "repositories/" + org + "/" + repo + "/src/" + branch + "/" + filePath)
							.get()
							.build()).execute().body().string();
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}


