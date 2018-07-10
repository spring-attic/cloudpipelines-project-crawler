package org.springframework.cloud.repositorymanagement;

import java.util.Objects;
import java.util.Set;

/**
 * Contains options required to connect to a password
 * management
 *
 * @author Marcin Grzejszczak
 * @since 0.0.1
 */
public class Options {
	public final String username, password, token, rootUrl;
	public final Repositories repositories;
	public final Set<ProjectAndBranch> projects;
	public final Set<String> excludedProjectsRegex;

	Options(String username, String password, String token, String rootUrl,
			Repositories repositories, Set<ProjectAndBranch> projects,
			Set<String> excludedProjectsRegex) {
		this.username = username;
		this.password = password;
		this.token = token;
		this.rootUrl = rootUrl;
		this.repositories = repositories;
		this.projects = projects;
		this.excludedProjectsRegex = excludedProjectsRegex;
	}

	public boolean isIgnored(String project) {
		return this.excludedProjectsRegex
				.stream()
				.anyMatch(project::matches);
	}
}

class ProjectAndBranch {
	final String project, projectName, branch;

	ProjectAndBranch(String project, String projectName, String branch) {
		this.project = project;
		this.projectName = projectName;
		this.branch = branch;
	}

	ProjectAndBranch(String project) {
		this(project, project, "master");
	}

	ProjectAndBranch(String project, String newProjectName) {
		this(project, newProjectName, "master");
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ProjectAndBranch that = (ProjectAndBranch) o;
		return Objects.equals(project, that.project) && Objects
				.equals(projectName, that.projectName) && Objects
				.equals(branch, that.branch);
	}

	@Override public int hashCode() {

		return Objects.hash(project, projectName, branch);
	}
}