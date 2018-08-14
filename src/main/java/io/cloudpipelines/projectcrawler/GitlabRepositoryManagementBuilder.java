package io.cloudpipelines.projectcrawler;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marcin Grzejszczak
 */
class GitlabRepositoryManagementBuilder implements RepositoryManagementBuilder {

	private static final Logger log = LoggerFactory.getLogger(GitlabRepositoryManagementBuilder.class);

	@Override public RepositoryManagement build(Options options) {
		boolean applicable = isApplicable(options.rootUrl);
		if (applicable) {
			return createNewRepoManagement(options);
		}
		if (options.repository != Repositories.GITLAB) {
			return null;
		}
		return createNewRepoManagement(options);
	}

	RepositoryManagement createNewRepoManagement(Options options) {
		return new GitlabRepositoryManagement(options);
	}

	private boolean isApplicable(String url) {
		boolean applicable = StringUtils.isNotBlank(url) && url.contains("gitlab");
		if (log.isDebugEnabled()) {
			log.debug("URL [{}] is applicable [{}]", url, applicable);
		}
		return applicable;
	}
}

class GitlabRepositoryManagement implements RepositoryManagement {

	private final GitlabAPI gitlabApi;
	private final Options options;

	GitlabRepositoryManagement(Options options) {
		this.gitlabApi = connect(options);
		this.gitlabApi.setRequestTimeout(5000);
		this.options = options;
	}

	GitlabAPI connect(Options options) {
		if (StringUtils.isNotBlank(options.token)) {
			return GitlabAPI
					.connect(options.rootUrl, options.token);
		} else if (StringUtils.isNotBlank(options.username)) {
			try {
				GitlabSession session = GitlabAPI
						.connect(options.rootUrl, options.username, options.password);
				return GitlabAPI
						.connect(options.rootUrl, session.getPrivateToken());
			}
			catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		throw new IllegalStateException("Neither token, nor username and password passed");
	}

	GitlabRepositoryManagement(GitlabAPI gitlabApi, Options options) {
		this.gitlabApi = gitlabApi;
		this.options = options;
	}

	@Override public List<Repository> repositories(String org) {
		try {
			List<GitlabProject> gitlabProjects = groupRepos(org);
			if (gitlabProjects.isEmpty()) {
				throw new IllegalStateException("No projects found for group [" + org + "]");
			}
			List<Repository> repositories = allNonFilteredOutProjects(gitlabProjects);
			return addManuallySetProjects(org, repositories);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private List<Repository> allNonFilteredOutProjects(List<GitlabProject> map) {
		return map.stream()
						.map(entry -> new Repository(
								options.projectName(entry.getName()),
								entry.getSshUrl(),
								entry.getHttpUrl(),
								"master"))
						.filter(repo -> !options.isIgnored(repo.name))
						.collect(Collectors.toList());
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

	List<GitlabProject> groupRepos(String org) throws IOException {
		return this.gitlabApi
				.getGroupProjects(this.gitlabApi.getGroup(org));
	}

	@Override public String fileContent(String org, String repo,
			String branch, String filePath) {
		try {
			byte[] bytes = getDescriptor(org, repo, branch, filePath);
			return new String(bytes, "UTF-8");
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	byte[] getDescriptor(String org, String repo, String branch,
			String filePath) throws IOException {
		GitlabProject project = this.gitlabApi.getProject(org, repo);
		return this.gitlabApi.getRawFileContent(project, branch, URLEncoder.encode(filePath, "UTF-8"));
	}
}


