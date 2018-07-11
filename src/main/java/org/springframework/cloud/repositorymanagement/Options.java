package org.springframework.cloud.repositorymanagement;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Contains options required to connect to a password
 * management
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class Options {
	public final String username, password, token, rootUrl;
	public final Repositories repositories;
	public final Set<ProjectAndBranch> projects;
	public final Set<ProjectAndBranch> renamedProjects;
	public final Set<String> excludedProjectsRegex;

	Options(String username, String password, String token, String rootUrl,
			Repositories repositories, Set<ProjectAndBranch> projects,
			Set<ProjectAndBranch> renamedProjects,
			Set<String> excludedProjectsRegex) {
		this.username = username;
		this.password = password;
		this.token = token;
		this.rootUrl = rootUrl;
		this.repositories = repositories;
		this.projects = projects;
		this.renamedProjects = renamedProjects;
		this.excludedProjectsRegex = excludedProjectsRegex;
	}

	public boolean isIgnored(String project) {
		return this.excludedProjectsRegex
				.stream()
				.anyMatch(project::matches);
	}

	public String projectName(String projectName) {
		return this.renamedProjects.stream()
				.filter(renamedPb -> renamedPb.project.equals(projectName))
						.findFirst()
						.map(optionalPb -> optionalPb.projectName)
						.orElse(projectName);
	}
}