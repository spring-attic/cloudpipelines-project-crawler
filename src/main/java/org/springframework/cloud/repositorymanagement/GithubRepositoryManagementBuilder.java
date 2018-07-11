package org.springframework.cloud.repositorymanagement;

import java.io.IOException;
import java.io.InputStream;
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
		if (applicable) {
			return createNewRepoManagement(options);
		}
		if (options.repositories != Repositories.GITHUB) {
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
			log.debug("URL [{}] is applicable [{}]", url, applicable);
		}
		return applicable;
	}
}

class GithubRepositoryManagement implements RepositoryManagement {

	private static final Logger log = LoggerFactory.getLogger(GithubRepositoryManagement.class);

	private final Github github;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Options options;

	GithubRepositoryManagement(Options options) {
		this.github = new RtGithub(github(options)
				.entry().through(RetryWire.class));
		this.options = options;
	}

	GithubRepositoryManagement(Github github, Options options) {
		this.github = github;
		this.options = options;
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

	@Override public List<Repository> repositories(String org) {
		try {
			String response = orgRepos(org);
			List<Map> map = this.objectMapper.readValue(response, List.class);
			List<Repository> repositories = allNonFilteredOutProjects(map);
			return addManuallySetProjects(org, repositories);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private List<Repository> allNonFilteredOutProjects(List<Map> map) {
		return map.stream()
						.map(entry -> new Repository(
								options.projectName(entry.get("name").toString()),
								entry.get("ssh_url").toString(),
								entry.get("clone_url").toString()))
						.filter(repo -> !options.isIgnored(repo.name))
						.collect(Collectors.toList());
	}

	private List<Repository> addManuallySetProjects(String org, List<Repository> repositories) {
		repositories.addAll(this.options.projects
				.stream().map(pb -> new Repository(options.projectName(pb.projectName),
				sshKey(org, pb), cloneUrl(org, pb)))
				.collect(Collectors.toSet()));
		return repositories;
	}

	private String sshKey(String org, ProjectAndBranch pb) {
		return "git@github.com:" + org + "/" + pb.project + ".git";
	}

	private String cloneUrl(String org, ProjectAndBranch pb) {
		return "https://github.com/" + org + "/" + pb.project + ".git";
	}

	String orgRepos(String org) throws IOException {
		return this.github.entry()
				.method("GET")
				.uri().path("orgs/" + org + "/repos")
				.back().fetch().body();
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
				return new java.util.Scanner(
						getDescriptor(org, repo, branch, filePath))
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
