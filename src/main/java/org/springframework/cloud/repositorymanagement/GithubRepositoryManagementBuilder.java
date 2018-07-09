package org.springframework.cloud.repositorymanagement;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.wire.RetryWire;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marcin Grzejszczak
 */
class GithubRepositoryManagementBuilder implements RepositoryManagementBuilder {

	private static final Logger log = LoggerFactory.getLogger(GithubRepositoryManagementBuilder.class);

	@Override public RepositoryManagement build(Options options) {
		boolean applicable = isApplicable(options.rootUrl);
		if (!applicable) {
			return null;
		}
		return createNewRepoManagement(options);
	}

	RepositoryManagement createNewRepoManagement(Options options) {
		return new GithubRepositoryManagement(options);
	}

	private boolean isApplicable(String url) {
		boolean applicable = StringUtils.isNotBlank(url) && url.contains("github");
		if (log.isDebugEnabled()) {
			log.debug("URL [{}] is applicable [{}]", applicable);
		}
		return applicable;
	}
}

class GithubRepositoryManagement implements RepositoryManagement {

	private static final Logger log = LoggerFactory.getLogger(GithubRepositoryManagement.class);

	private final Github github;
	private final ObjectMapper objectMapper = new ObjectMapper();

	GithubRepositoryManagement(Options options) {
		this.github = new RtGithub(github(options)
				.entry().through(RetryWire.class));
	}

	private RtGithub github(Options options) {
		if (StringUtils.isNotBlank(options.token)) {
			return new RtGithub(options.token);
		}
		if (StringUtils.isNotBlank(options.username)) {
			return new RtGithub(options.username, options.password);
		}
		throw new IllegalStateException("Token and username are blank. Pick either of them");
	}

	GithubRepositoryManagement(Github github) {
		this.github = github;
	}

	@Override public List<String> repositories(String org) {
		try {
			String response = orgRepos(org);
			List<Map> map = this.objectMapper.readValue(response, List.class);
			return map
					.stream()
					.map(entry -> entry.get("name").toString())
					.filter(name -> !name.endsWith(".github.io"))
					.collect(Collectors.toList());
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	String orgRepos(String org) throws IOException {
		return this.github.entry()
				.method("GET")
				.uri().path("orgs/" + org + "/repos").back().fetch().body();
	}

	@Override public String fileContent(String org, String repo,
			String branch, String filePath) {
		try {
			boolean descriptorExists = descriptorExists(org, repo, branch, filePath);
			if (log.isDebugEnabled()) {
				log.debug("Descriptor [{}] for branch [{}] org [{}] and repo [{}] exists [{}]",
						filePath, branch, org, repo, descriptorExists);
			}
			if (descriptorExists) {
				return new java.util.Scanner(getDescriptor(org, repo, branch, filePath))
						.useDelimiter("\\A").next();
			}
			return "";
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	InputStream getDescriptor(String org, String repo, String branch,
			String filePath) throws IOException {
		return this.github.repos().get(
				new Coordinates.Simple(org, repo))
				.contents().get(filePath, branch).raw();
	}

	boolean descriptorExists(String org, String repo, String branch,
			String filePath) throws IOException {
		return this.github.repos().get(
				new Coordinates.Simple(org, repo))
				.contents()
				.exists(filePath, branch);
	}
}
